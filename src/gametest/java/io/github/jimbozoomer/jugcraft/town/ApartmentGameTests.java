package io.github.jimbozoomer.jugcraft.town;

import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.finance.ClaimTransfers;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class ApartmentGameTests {
    @GameTest public void everyApartmentAppearsAcrossTheClickablePages(GameTestHelper h) {
        long now = 1_000L;
        var viewer = UUID.randomUUID();
        var rooms = new java.util.ArrayList<ApartmentBook.Room>();
        for (int i = 72; i > 0; i--) {
            var pos = new BlockPos(i * 3, 0, 0);
            rooms.add(new ApartmentBook.Room("room_" + i, pos, pos, 8, true));
        }
        var own = rooms.getFirst();
        own.tenant = viewer;
        own.until = now + ApartmentBook.WEEK;
        for (int page = 1; page <= 9; page++) {
            var lines = ApartmentCommands.listingPage(rooms, BlockPos.ZERO, viewer, now, page);
            h.assertTrue(lines.getFirst().getString().contains("Page " + page + " of 9"), "Page count matches all 72 units");
            for (int slot = 0; slot < 8; slot++) {
                int expected = (page - 1) * 8 + slot + 1;
                h.assertTrue(lines.get(2 + slot).getString().startsWith("room_" + expected + " · "), "Nearest first, without gaps or repeats");
            }
            long mine = lines.stream().filter(line -> line.getString().contains("room_72 · ")).count();
            h.assertTrue(mine == 1, "Own lease is always visible exactly once");
            var footer = lines.getLast();
            var clickTargets = footer.getSiblings().stream().map(link -> link.getStyle().getClickEvent()).toList();
            if (page > 1) h.assertTrue(footer.getString().contains("Previous · /apartment list " + (page - 1))
                && clickTargets.contains(new net.minecraft.network.chat.ClickEvent.RunCommand("/apartment list " + (page - 1))), "Previous page works by click or typed command");
            if (page < 9) h.assertTrue(footer.getString().contains("Next · /apartment list " + (page + 1))
                && clickTargets.contains(new net.minecraft.network.chat.ClickEvent.RunCommand("/apartment list " + (page + 1))), "Next page works by click or typed command");
        }
        h.assertTrue(ApartmentCommands.listingPage(rooms, BlockPos.ZERO, viewer, now, Integer.MAX_VALUE).getFirst().getString().contains("Page 9 of 9"),
            "An old link beyond the last page remains usable");
        h.succeed();
    }

    @GameTest public void operatorMarksAVacantInteriorAndVisitorsCannotRedesignIt(GameTestHelper h) throws Exception {
        var l=h.getLevel(); var player=h.makeMockServerPlayerInLevel(); var server=l.getServer();
        var commands=server.getCommands().getDispatcher();
        var op=server.createCommandSourceStack().withEntity(player).withSuppressedOutput();
        var visitor=player.createCommandSourceStack().withSuppressedOutput();
        var corner=h.absolutePos(new BlockPos(2,2,2)); var id="test_roof_"+corner.asLong();
        var book=ApartmentRentals.data(l).apartments;
        try {
            h.assertTrue(commands.parse("apartment define "+id+" 12",visitor).getReader().canRead(),"Definition subcommand needs operator permissions");
            commands.execute("apartment tool",op);
            h.assertTrue(player.getInventory().contains(s->s.is(Items.BLAZE_ROD)),"Operator receives the marked survey wand");
            player.snapTo(Vec3.atCenterOf(corner.above(2))); commands.execute("apartment pos1",op);
            h.assertTrue(commands.execute("apartment define "+id+" 12",op)==0,"A one-corner selection cannot be published");
            player.snapTo(Vec3.atCenterOf(corner.east(3))); commands.execute("apartment pos2",op);
            h.assertTrue(commands.execute("apartment define "+id+" 12",op)==1,"Reversed corners normalize into one priced room");
            var room=book.named(id);
            h.assertTrue(room!=null && room.price==12 && book.at(corner)==room && book.at(corner.below())!=room && book.at(corner.above(3))!=room,
                "Only the intended 3D interior is rentable");
            h.assertTrue(ApartmentRentals.customAt(l,corner),"City expansion sees this designation during survey");
            h.assertTrue(commands.execute("apartment define "+id+" 12",op)==0,"Completed tool selection is cleared");
        } finally { var room=book.named(id); if(room!=null) book.remove(room); }
        h.succeed();
    }

    @GameTest public void weeklyRentAndReceiptsSurviveSaveTogether(GameTestHelper h) {
        var wallet=new Jugs.Data(); var book=wallet.apartments;
        var room=new ApartmentBook.Room("suite",BlockPos.ZERO,new BlockPos(4,3,5),8,false);
        var other=new ApartmentBook.Room("loft",room.lo.above(5),room.hi.above(5),10,false);
        book.add(room); book.add(other); long now=1_000_000L;
        var tenant=UUID.randomUUID(); var rival=UUID.randomUUID();
        h.assertTrue(book.pay(wallet,room,tenant,false,now)!=null && room.tenant==null,"Insufficient funds reserve nothing");
        wallet.add(tenant,30); wallet.add(rival,30);
        h.assertTrue(book.pay(wallet,room,tenant,false,now)==null && wallet.balance(tenant)==22,"One week paid once");
        h.assertTrue(book.pay(wallet,room,rival,false,now)!=null && wallet.balance(rival)==30,"Second player cannot steal a paid room");
        h.assertTrue(book.pay(wallet,other,tenant,false,now)!=null,"One player cannot monopolize multiple rooms");
        h.assertTrue(book.pay(wallet,room,tenant,true,now)==null && room.until==now+2*ApartmentBook.WEEK,"Renewal preserves paid days");
        room.furniture.put(room.lo,"minecraft:barrel");
        var save=Jugs.Data.CODEC.encodeStart(JsonOps.INSTANCE,wallet).getOrThrow();
        var restored=Jugs.Data.CODEC.parse(JsonOps.INSTANCE,save).getOrThrow();
        var unit=restored.apartments.at(room.lo);
        h.assertTrue(unit!=null && tenant.equals(unit.tenant) && unit.until==room.until && unit.furniture.equals(room.furniture)
            && restored.balance(tenant)==14,"The index, lease, furniture receipts and charge reopen together");
        h.assertTrue(!unit.active(tenant,unit.until) && !unit.available(unit.until),"Expired furnished apartment is held for move-out");
        h.assertTrue(restored.apartments.pay(restored,unit,rival,false,unit.until)!=null,"Old furnishings never become a new renter's property");
        unit.furniture.clear();
        h.assertTrue(restored.apartments.pay(restored,unit,rival,false,unit.until)==null,"Cleared expired apartment can be let again");
        var oldSave=new com.google.gson.JsonObject(); oldSave.add("balances",new com.google.gson.JsonObject());
        h.assertTrue(Jugs.Data.CODEC.parse(JsonOps.INSTANCE,oldSave).getOrThrow().apartments.rooms().isEmpty(),"Old balances open without apartment data");
        h.succeed();
    }

    private static InteractionResult click(ServerPlayer p,BlockPos pos,Direction face) {
        return UseBlockCallback.EVENT.invoker().interact(p,p.level(),InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(pos).add(face.getStepX()*.5,face.getStepY()*.5,face.getStepZ()*.5),face,pos,false));
    }
    @GameTest public void tenantCanFurnishOnlyTheirInteriorAndRecoverAfterExpiry(GameTestHelper h) {
        var l=h.getLevel();
        var owner=h.makeMockServerPlayerInLevel(); var visitor=h.makeMockServerPlayerInLevel();
        owner.setGameMode(GameType.SURVIVAL); visitor.setGameMode(GameType.SURVIVAL);
        var floor=h.absolutePos(new BlockPos(3,1,3)); var inside=floor.above();
        var book=ApartmentRentals.data(l).apartments;
        var room=new ApartmentBook.Room("smoke_"+floor.asLong(),inside,inside.offset(2,2,2),8,false);
        book.add(room);
        try {
            owner.snapTo(Vec3.atCenterOf(inside.west(2))); visitor.snapTo(Vec3.atCenterOf(inside.west(3)));
            l.setBlockAndUpdate(floor,Blocks.STONE_BRICKS.defaultBlockState()); l.setBlockAndUpdate(inside,Blocks.AIR.defaultBlockState());
            var wallet=Jugs.data(l.getServer()); wallet.add(owner.getUUID(),8);
            h.assertTrue(ApartmentCommands.act(owner,"rent",room.id)==1,"Rent a nearby designated room through the player path");
            visitor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BARREL));
            click(visitor,floor,Direction.UP);
            h.assertTrue(l.getBlockState(inside).isAir(),"A visitor cannot place in the interior from the public floor");
            owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BARREL));
            click(owner,floor,Direction.UP);
            h.assertTrue(l.getBlockState(inside).is(Blocks.BARREL) && owner.getMainHandItem().isEmpty(),"Place real furniture and consume the item");
            h.assertTrue(ApartmentRentals.mayRetrieve(owner,l,inside) && !ApartmentRentals.mayRetrieve(visitor,l,inside),"The placement receipt belongs only to the tenant");
            h.assertTrue(!ClaimTransfers.allowed(l,inside.east(),inside),"Unattended machines cannot access the private barrel from inside the lease");
            visitor.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            h.assertTrue(click(visitor,inside,Direction.UP)==InteractionResult.FAIL,"Private barrel cannot open for a visitor");
            h.assertTrue(click(owner,inside,Direction.UP).consumesAction() && owner.containerMenu!=owner.inventoryMenu,"Tenant opens the private barrel"); owner.closeContainer();
            var fixture=inside.east(); l.setBlockAndUpdate(fixture,Blocks.BOOKSHELF.defaultBlockState());
            h.assertTrue(!PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(l,owner,fixture,l.getBlockState(fixture),null),"Landlord bookshelf stays fixed");
            owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Blocks.SPRUCE_DOOR.asItem()));
            click(owner,fixture,Direction.UP);
            h.assertTrue(l.getBlockState(fixture.above()).isAir(),"Multi-block items cannot cross a boundary");
            owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.LANTERN));
            var outside=inside.west(); l.setBlockAndUpdate(outside.below(),Blocks.STONE_BRICKS.defaultBlockState());
            click(owner,inside,Direction.WEST); h.assertTrue(!l.getBlockState(outside).is(Blocks.LANTERN),"A tenant cannot use a fixture to place outside");
            owner.closeContainer();
            var threshold=inside.south(); l.setBlockAndUpdate(threshold.below(),Blocks.STONE_BRICKS.defaultBlockState());
            var door=threshold.west(); l.setBlockAndUpdate(door.below(),Blocks.STONE_BRICKS.defaultBlockState());
            l.setBlock(door,Blocks.OAK_DOOR.defaultBlockState(),net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            click(owner,threshold.below(),Direction.UP);
            h.assertTrue(l.getBlockState(threshold).isAir(),"The interior doorway stays clear");
            room.until=1L;
            owner.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            h.assertTrue(click(owner,inside,Direction.UP)==InteractionResult.FAIL && owner.containerMenu==owner.inventoryMenu,"Expired tenant must pack or renew before continuing to use storage");
            h.assertTrue(ApartmentCommands.act(owner,"leave",room.id)==0,"Cannot abandon belongings to a stranger");
            owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BOOKSHELF));
            click(owner,fixture,Direction.UP); h.assertTrue(l.getBlockState(fixture.above()).isAir(),"An expired tenancy cannot add furniture");
            var state=l.getBlockState(inside);
            h.assertTrue(PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(l,owner,inside,state,l.getBlockEntity(inside)),"Expired tenant can recover property");
            owner.gameMode.destroyBlock(inside);
            h.assertTrue(room.furniture.isEmpty() && !l.getBlockState(inside).is(Blocks.BARREL),"Breaking the piece clears the saved receipt");
            h.assertTrue(ApartmentCommands.act(owner,"leave",room.id)==1 && room.available(System.currentTimeMillis()),"Cleared room returns to the market");
        } finally { book.remove(room); }
        h.succeed();
    }

    @GameTest public void generatedListingsInstallAndWithdrawAsABatch(GameTestHelper h) {
        var l=h.getLevel(); var origin=h.absolutePos(new BlockPos(1,2,1));
        var first=new WesternDistrictPlan.Room("west_test_1",new int[]{0,0,0,2,2,2},new BlockPos(-1,0,0));
        var bad=new WesternDistrictPlan.Room("west_test_2",new int[]{2,0,0,4,2,2},new BlockPos(4,0,-1));
        var rooms=List.of(first);
        var book=ApartmentRentals.data(l).apartments;
        h.assertTrue(!ApartmentRentals.installRooms(l,origin,List.of(first,bad)) && book.named(first.id())==null,"Conflict rejects the whole batch");
        h.assertTrue(ApartmentRentals.installRooms(l,origin,rooms) && ApartmentRentals.installRooms(l,origin,rooms),"Repeated install is idempotent");
        try {
            var unit=book.named(first.id()); unit.tenant=UUID.randomUUID(); unit.until=System.currentTimeMillis()+10000;
            h.assertTrue(!ApartmentRentals.removeRooms(l,origin,rooms),"Active lease blocks rollback");
            unit.until=0L; unit.furniture.put(origin,"minecraft:bookshelf");
            h.assertTrue(!ApartmentRentals.canRemoveRooms(l,origin,rooms),"Move-out rights block rollback");
            unit.furniture.clear();
            h.assertTrue(ApartmentRentals.removeRooms(l,origin,rooms) && ApartmentRentals.removeRooms(l,origin,rooms),"Cleared listings withdraw idempotently");
        } finally { var left=book.named(first.id()); if(left!=null) book.remove(left); }
        h.succeed();
    }

    @GameTest public void staffCanTurnOverAnAbandonedUnitAndItsOwnerAloneCanReclaimExactlyOnce(GameTestHelper h) {
        var l=h.getLevel();
        var previous=io.github.jimbozoomer.jugcraft.test.TestPlayers.survivor(h);
        var next=h.makeMockServerPlayerInLevel();
        var at=h.absolutePos(new BlockPos(2,2,2));
        previous.snapTo(Vec3.atCenterOf(at.west(2))); next.snapTo(Vec3.atCenterOf(at.north(2)));
        var wallet=ApartmentRentals.data(l); var book=wallet.apartments;
        var room=new ApartmentBook.Room("abandoned_"+at.asLong(),at,at.offset(2,2,2),8,false);
        book.add(room);
        try {
            room.tenant=previous.getUUID(); room.until=1; room.furniture.put(at,"minecraft:barrel");
            l.setBlockAndUpdate(at,Blocks.BARREL.defaultBlockState());
            var barrel=(net.minecraft.world.Container)l.getBlockEntity(at);
            var jewels=new ItemStack(Items.DIAMOND,3);
            jewels.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("From the old flat"));
            barrel.setItem(4,jewels.copy());
            // Actual offline former tenant. Packing cannot depend on an online inventory.
            io.github.jimbozoomer.jugcraft.test.TestPlayers.leave(previous);
            var error=ApartmentReclaim.pack(l,room);
            h.assertTrue(error==null && l.getBlockState(at).isAir() && room.available(System.currentTimeMillis()),"Whole barrel and items move to chunk escrow: "+error);
            h.assertTrue(l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(at).inflate(1),
                e->ItemStack.matches(e.getItem(),jewels)||e.getItem().is(Items.BARREL)).isEmpty(),"Packed fixture and contents create no duplicate ground drops");
            wallet.add(next.getUUID(),8); h.assertTrue(ApartmentCommands.act(next,"rent",room.id)==1,"A new tenant can rent the vacated room immediately");
            h.assertTrue(ApartmentReclaim.collect(next).startsWith("No packed"),"Next tenant cannot withdraw the former resident's goods");
            var owner=io.github.jimbozoomer.jugcraft.test.TestPlayers.again(h,previous.getUUID());
            owner.snapTo(Vec3.atCenterOf(at.west(2)));
            for(int i=0;i<36;i++) owner.getInventory().setItem(i,new ItemStack(Items.STONE,64));
            h.assertTrue(ApartmentReclaim.collect(owner).startsWith("Make space"),"A full inventory keeps the receipt outstanding");
            owner.getInventory().setItem(0,ItemStack.EMPTY); owner.getInventory().setItem(1,ItemStack.EMPTY);
            var sourceChunk=l.getChunkAt(at);
            var beforeWithdrawal=sourceChunk.getAttached(ApartmentReclaim.cargo);
            var result=ApartmentReclaim.collect(owner);
            h.assertTrue(result.startsWith("Recovered 2"),"Reclaim delivered the barrel and its single stack: "+result);
            h.assertTrue(owner.getInventory().getItem(0).is(Items.BARREL) && ItemStack.matches(owner.getInventory().getItem(1),jewels),"Container contents retain custom components and exact count");
            io.github.jimbozoomer.jugcraft.test.TestPlayers.leave(owner);
            var returned=io.github.jimbozoomer.jugcraft.test.TestPlayers.again(h,previous.getUUID());
            returned.snapTo(Vec3.atCenterOf(at.west(2)));
            h.assertTrue(ItemStack.matches(returned.getInventory().getItem(1),jewels),"Delivery persists across rejoin");
            // Simulate a process loss after the player file flush but before the chunk's final acknowledgement.
            sourceChunk.setAttached(ApartmentReclaim.cargo,beforeWithdrawal);
            returned.getInventory().setItem(2,ItemStack.EMPTY);
            h.assertTrue(ApartmentReclaim.collect(returned).startsWith("Previous delivery confirmed") && returned.getInventory().getItem(2).isEmpty(),
                "A replayed source chunk cannot duplicate items after the receiver's saved receipt");
            h.assertTrue(ApartmentReclaim.collect(returned).startsWith("No packed"),"The recovered receipt is now retired");
        } finally { book.remove(room); }
        h.succeed();
    }
}

