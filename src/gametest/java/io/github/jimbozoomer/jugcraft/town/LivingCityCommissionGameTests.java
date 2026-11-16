package io.github.jimbozoomer.jugcraft.town;

import com.mojang.serialization.JsonOps;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

public class LivingCityCommissionGameTests {
    @GameTest public void harvestDisplayRunsAfterHalloweenUntilDecember(GameTestHelper h) {
        h.assertTrue(!TownDecor.harvestDisplayOn(java.time.LocalDate.of(2026,11,3)),"Halloween retains November 3");
        h.assertTrue(TownDecor.harvestDisplayOn(java.time.LocalDate.of(2026,11,4)),"Harvest starts November 4");
        h.assertTrue(TownDecor.harvestDisplayOn(java.time.LocalDate.of(2026,11,30)),"Harvest retains November 30");
        h.assertTrue(!TownDecor.harvestDisplayOn(java.time.LocalDate.of(2026,12,1)),"December can replace harvest");h.succeed();
    }
    @GameTest public void actualHarvestSceneMigratesFromHalloweenAndIntoDecember(GameTestHelper h) {
        var data=TownData.get();var level=h.getLevel();
        var site=data.sites.stream().filter(s->s.kind().equals("centerpiece")).findFirst().orElseThrow();
        h.assertTrue(data.centerpiece.containsKey("harvest_v2"),"Registered scene is packaged");
        var anchor=h.absolutePos(new BlockPos(1,1,1));
        int minX=java.util.Arrays.stream(site.blocks()).mapToInt(b->b[0]).min().orElseThrow();
        int minY=java.util.Arrays.stream(site.blocks()).mapToInt(b->b[1]).min().orElseThrow();
        int minZ=java.util.Arrays.stream(site.blocks()).mapToInt(b->b[2]).min().orElseThrow();
        var previous="halloween";
        for(var next:new String[]{"harvest_v2","harvest_v2","december"}) {
            var cells=new java.util.ArrayList<TownDecor.DecorCell>();
            for(var b:site.blocks()) {
                var pos=anchor.offset(b[0]-minX,b[1]-minY,b[2]-minZ);
                var expected=TownDecor.stateFor(data,site,previous,b[3]);
                if(previous.equals("halloween")) TownBuilder.set(level,pos,expected);
                cells.add(new TownDecor.DecorCell(pos,expected,TownDecor.stateFor(data,site,next,b[3])));
            }
            h.assertTrue(TownDecor.applyCells(level,cells),"Whole packaged scene transitions to "+next);
            for(var c:cells) h.assertTrue(level.getBlockState(c.pos())==c.wanted(),"All scene cells match "+next);
            previous=next;
        }
        h.assertTrue(level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
            new net.minecraft.world.phys.AABB(anchor).expandTowards(7,16,7)).isEmpty(),"No free drops from seasonal replacement");
        h.succeed();
    }
    private java.util.List<TownDecor.DecorCell> hornCells(GameTestHelper h, boolean removing) {
        var block=(io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock)net.minecraft.core.registries.BuiltInRegistries.BLOCK
            .getValue(net.minecraft.resources.Identifier.parse("jugcraft:giant_cornucopia"));
        var root=h.absolutePos(new BlockPos(4,1,2));
        var cells=new java.util.ArrayList<TownDecor.DecorCell>();
        for(int part=block.cells().length-1;part>=0;part--) {
            var state=block.defaultBlockState().setValue(block.partProperty(),part);
            var pos=block.partPos(root,net.minecraft.core.Direction.NORTH,part);
            cells.add(new TownDecor.DecorCell(pos,removing?state:Blocks.AIR.defaultBlockState(),
                removing?Blocks.AIR.defaultBlockState():state));
            TownBuilder.set(h.getLevel(),pos,removing?state:Blocks.AIR.defaultBlockState());
        }
        return cells;
    }
    @GameTest public void seasonalMultiblockRemovesRootWithoutDropsAndCanBeRepeated(GameTestHelper h) {
        var cells=hornCells(h,true);var level=h.getLevel();
        h.assertTrue(TownDecor.applyCells(level,cells),"Intact cornucopia migrates");
        for(var c:cells) h.assertTrue(level.getBlockState(c.pos()).isAir(),"Every old part removed");
        h.assertTrue(level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
            new net.minecraft.world.phys.AABB(h.absolutePos(new BlockPos(0,0,0))).inflate(10)).isEmpty(),"Migration creates no dropped props");
        h.assertTrue(TownDecor.applyCells(level,cells),"Repeated application is safe");h.succeed();
    }
    @GameTest public void seasonalMultiblockLanternToggleDoesNotFreezeTheSeason(GameTestHelper h) {
        var cells=hornCells(h,true);var level=h.getLevel();
        for(var c:cells) TownBuilder.set(level,c.pos(),c.expected().setValue(
            io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock.LIT,false));
        h.assertTrue(TownDecor.applyCells(level,cells),"Turning off authored lanterns does not count as a replacement");
        for(var c:cells) h.assertTrue(level.getBlockState(c.pos()).isAir(),"Unlit display still clears");h.succeed();
    }
    @GameTest public void seasonalMultiblockPreservesWholeSiteWhenOwnerEditsOneCell(GameTestHelper h) {
        var cells=hornCells(h,false);var level=h.getLevel();var owner=cells.get(0).pos();
        level.setBlockAndUpdate(owner,Blocks.GOLD_BLOCK.defaultBlockState());
        h.assertTrue(TownDecor.blockedDisplay(level,cells),"Preview flags entire display");
        h.assertTrue(!TownDecor.applyCells(level,cells),"Conflicting display is not partially placed");
        for(var c:cells) h.assertTrue(level.getBlockState(c.pos()).is(c.pos().equals(owner)?Blocks.GOLD_BLOCK:Blocks.AIR),"Owner state preserved");
        h.assertTrue(TownUpgrades.preservedDecor(level,cells.get(1).pos()),"Whole site remains protected in later seasons");h.succeed();
    }
    @GameTest public void seasonalMultiblockRejectsIncompleteFootprint(GameTestHelper h) {
        var cells=hornCells(h,false);cells.remove(0);
        h.assertTrue(!TownDecor.applyCells(h.getLevel(),cells),"Never place a prop outside authored site bounds");
        for(var c:cells) h.assertTrue(h.getLevel().getBlockState(c.pos()).isAir(),"No partial display placed");h.succeed();
    }
    @GameTest public void decorPreviewDoesNotClaimOrReplaceOwnerCells(GameTestHelper h) {
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(2,2,2));
        var expected=Blocks.OAK_PLANKS.defaultBlockState();var wanted=TownData.parse("minecraft:red_wool");
        level.setBlockAndUpdate(pos,Blocks.GOLD_BLOCK.defaultBlockState());
        var progress=TownUpgrades.progress(level);
        h.assertTrue(!progress.preservedDecor.contains(pos.asLong()), "Fixture begins unclaimed");
        var before=TownUpgrades.Progress.CODEC.encodeStart(JsonOps.INSTANCE,progress).getOrThrow();
        h.assertTrue(TownDecor.cellChange(level,pos,expected,wanted)==TownDecor.CellChange.PRESERVE,"Preview flags owner edit");
        h.assertTrue(!progress.preservedDecor.contains(pos.asLong()), "Preview does not persist an override");
        h.assertTrue(before.equals(TownUpgrades.Progress.CODEC.encodeStart(JsonOps.INSTANCE,progress).getOrThrow()),"Preview leaves migration ledger unchanged");
        h.assertTrue(level.getBlockState(pos).is(Blocks.GOLD_BLOCK), "Preview leaves owner block untouched");
        level.setBlockAndUpdate(pos,expected);
        h.assertTrue(TownDecor.cellChange(level,pos,expected,wanted)==TownDecor.CellChange.REPLACE,"Matching authored cell can change");
        h.assertTrue(TownDecor.updateAuthoredDecor(level,pos,expected,wanted),"Actual update uses same decision");
        h.assertTrue(TownDecor.cellChange(level,pos,expected,wanted)==TownDecor.CellChange.UNCHANGED,"Repeated update is unchanged");
        level.setBlockAndUpdate(pos,Blocks.CHEST.defaultBlockState());
        h.assertTrue(TownDecor.cellChange(level,pos,Blocks.CHEST.defaultBlockState(),wanted)==TownDecor.CellChange.PRESERVE,"Even matching container is protected");
        h.succeed();
    }
    @GameTest public void fullSizeNoticeboardPartsAndRemoval(GameTestHelper h) {
        var level=h.getLevel();var block=JugcraftTown.COMMISSION_BOARD;
        var root=h.absolutePos(new BlockPos(2,2,2));
        for(var facing:net.minecraft.core.Direction.Plane.HORIZONTAL) {
            var state=block.defaultBlockState().setValue(CommissionBoardBlock.FACING,facing);
            level.setBlockAndUpdate(root,state);
            block.setPlacedBy(level,root,state,null,new ItemStack(block));
            for(int part=0;part<9;part++) {
                var at=CommissionBoardBlock.partPos(root,facing,part);
                var actual=level.getBlockState(at);
                h.assertTrue(actual.is(block)&&actual.getValue(CommissionBoardBlock.PART)==part,"All nine tiles placed");
                h.assertTrue(CommissionBoardBlock.rootPos(at,actual).equals(root),"Every tile resolves to the same commission menu");
            }
            var replaced=CommissionBoardBlock.partPos(root,facing,8);
            level.setBlockAndUpdate(replaced,Blocks.GOLD_BLOCK.defaultBlockState());
            h.assertTrue(level.getBlockState(replaced).is(Blocks.GOLD_BLOCK),"Removal leaves replacement untouched");
            for(int part=0;part<8;part++)h.assertTrue(!level.getBlockState(CommissionBoardBlock.partPos(root,facing,part)).is(block),"No orphan board panels");
            level.removeBlock(replaced,false);
        }
        h.succeed();
    }
    @GameTest public void noticeboardPreflightsSpaceAndDropsOneItem(GameTestHelper h) {
        var level=h.getLevel();var block=JugcraftTown.COMMISSION_BOARD;
        var root=h.absolutePos(new BlockPos(2,2,2));var player=h.makeMockServerPlayerInLevel();
        player.setYRot(0);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(block));
        level.setBlockAndUpdate(root.below(),Blocks.STONE.defaultBlockState());
        var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(root.below()),net.minecraft.core.Direction.UP,root.below(),false);
        var context=new net.minecraft.world.item.context.BlockPlaceContext(new net.minecraft.world.item.context.UseOnContext(player,net.minecraft.world.InteractionHand.MAIN_HAND,hit));
        var state=block.getStateForPlacement(context);
        h.assertTrue(state!=null,"Clear nine-cell footprint accepts one board item");
        var corner=CommissionBoardBlock.partPos(root,state.getValue(CommissionBoardBlock.FACING),8);
        level.setBlockAndUpdate(corner,Blocks.CHEST.defaultBlockState());
        h.assertTrue(block.getStateForPlacement(context)==null,"An occupied upper corner rejects the entire placement");
        h.assertTrue(level.getBlockState(root).isAir()&&level.getBlockState(corner).is(Blocks.CHEST),"Failed preflight changes no cells");
        level.removeBlock(corner,false);
        level.setBlockAndUpdate(root,state);block.setPlacedBy(level,root,state,player,new ItemStack(block));
        level.destroyBlock(corner,true,player);
        int count=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(root).inflate(5),e->e.getItem().is(block.asItem()))
            .stream().mapToInt(e->e.getItem().getCount()).sum();
        h.assertTrue(count==1,"Breaking a remote panel drops exactly one noticeboard item");
        h.assertTrue(!level.getBlockState(root).is(block),"Panel break removes the master");
        h.succeed();
    }
    @GameTest public void deliveryPaysOnceAndSurvivesReload(GameTestHelper h){
        var player=h.makeMockServerPlayerInLevel(); var id=player.getUUID(); var data=Jugs.data(h.getLevel().getServer());
        player.getInventory().setItem(0,new ItemStack(Items.WHEAT,12));
        var named=new ItemStack(Items.WHEAT,32);named.set(DataComponents.CUSTOM_NAME,Component.literal("Keepsake wheat"));
        player.getInventory().setItem(1,named);
        h.assertTrue(TownCommissions.deliver(player,0),"Partial delivery accepted");
        h.assertTrue(TownCommissions.progress(data,id,0,TownCommissions.cycle(player)).count()==12,"Only plain wheat used");
        h.assertTrue(named.getCount()==32,"Personal named items are untouched");
        h.assertTrue(!TownCommissions.claim(data,id,0,TownCommissions.cycle(player)),"Incomplete delivery cannot pay");
        player.getInventory().setItem(0,new ItemStack(Items.WHEAT,32));
        h.assertTrue(TownCommissions.deliver(player,0),"Remainder accepted");
        h.assertTrue(player.getInventory().getItem(0).getCount()==12,"Excess goods retained");
        long before=data.balance(id);
        h.assertTrue(TownCommissions.claim(data,id,0,TownCommissions.cycle(player)),"Completed request pays");
        h.assertTrue(data.balance(id)==before+1,"32 wheat earns one Jug, not twelve");
        h.assertTrue(!TownCommissions.claim(data,id,0,TownCommissions.cycle(player)),"Replay cannot pay again");
        var encoded=Jugs.Data.CODEC.encodeStart(JsonOps.INSTANCE,data).getOrThrow();
        var restored=Jugs.Data.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow();
        h.assertTrue(restored.balance(id)==data.balance(id),"Balance survives reload");
        h.assertTrue(!TownCommissions.claim(restored,id,0,TownCommissions.cycle(player)),"Claim receipt survives reload with balance");
        h.assertTrue(!TownCommissions.deliver(player,-1)&&!TownCommissions.deliver(player,99),"Invalid IDs refused");
        h.succeed();
    }
    @GameTest public void quotaAndUnclaimedRewardRemainSafe(GameTestHelper h){
        h.assertTrue(TownCommissions.PERIOD==72_000L,"Personal quotas last one running-server hour");
        h.assertTrue(TownCommissions.ORDERS.stream().mapToInt(TownCommissions.Order::reward).sum()==5,
            "Completing every civic request pays at most five Jugs per quota cycle");
        var data=new Jugs.Data();var id=UUID.randomUUID();var order=TownCommissions.ORDERS.get(0);
        data.civic.put(TownCommissions.key(id,order),new TownCommissions.Delivery(4,32,false,""));
        h.assertTrue(TownCommissions.progress(data,id,0,5).count()==32,"Unclaimed delivery survives cycle change");
        data.add(id,TownShops.get().maxBalance);
        h.assertTrue(!TownCommissions.claim(data,id,0,5),"Full purse retains pending reward");
        data.take(id,1);h.assertTrue(TownCommissions.claim(data,id,0,5),"Claim after freeing exactly one Jug of purse space");
        h.assertTrue(TownCommissions.progress(data,id,0,5).paid(),"Same cycle stays complete");
        h.assertTrue(TownCommissions.progress(data,id,0,6).count()==0,"Next cycle permits new delivery");
        h.assertTrue(!TownCommissions.claim(data,id,0,4),"Clock rewind cannot duplicate payment");
        var legacy=new com.google.gson.JsonObject();legacy.add("balances",new com.google.gson.JsonObject());
        h.assertTrue(Jugs.Data.CODEC.parse(JsonOps.INSTANCE,legacy).getOrThrow().civic.isEmpty(),"Existing saves load without civic field");
        for(var request:TownCommissions.ORDERS)for(var shop:TownShops.get().shops.values())for(var sale:shop.sells()){
            if(sale.item().equals(request.item()))h.assertTrue((long)request.reward()*sale.count()<=(long)sale.price()*request.count(),
                "Buying supplies at "+shop.id()+" cannot generate a commission profit: "+request.id());
        }
        h.succeed();
    }
    @GameTest public void publicBoardAndOwnerFurnitureAreProtected(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(2,2,2));
        level.setBlockAndUpdate(pos,JugcraftTown.COMMISSION_BOARD.defaultBlockState());
        h.assertTrue(TownProtection.publicBlock(level,pos,level.getBlockState(pos)),"Public board can be opened");
        level.setBlockAndUpdate(pos,Blocks.CHEST.defaultBlockState());
        h.assertTrue(!TownProtection.publicBlock(level,pos,level.getBlockState(pos)),"Town chest stays private");
        h.assertTrue(!TownUpgrades.replaceAuthored(level,pos,Blocks.CHEST.defaultBlockState(),Blocks.AIR.defaultBlockState()),"Even matching authored container cannot be erased");
        level.setBlockAndUpdate(pos,Blocks.ENDER_CHEST.defaultBlockState());
        h.assertTrue(TownProtection.publicBlock(level,pos,level.getBlockState(pos)),"Personal ender chest is public");
        var slots=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse("jugcraft:slot_machine_classic"));
        level.setBlockAndUpdate(pos,slots.defaultBlockState());
        h.assertTrue(TownProtection.publicBlock(level,pos,level.getBlockState(pos)),"Slot machine's own authorized menu is reachable");
        var beside=pos.east();level.setBlockAndUpdate(beside,Blocks.AIR.defaultBlockState());
        h.assertTrue(!TownUpgrades.replaceAuthored(level,beside,Blocks.AIR.defaultBlockState(),Blocks.OAK_FENCE.defaultBlockState()),"Decoration cannot block slot-machine access");
        h.succeed();
    }
    @GameTest public void seasonalEditsRemainProtectedAcrossLaterSeasons(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(2,2,2));
        level.setBlockAndUpdate(pos,Blocks.GOLD_BLOCK.defaultBlockState());
        h.assertTrue(!TownDecor.updateAuthoredDecor(level,pos,Blocks.OAK_PLANKS.defaultBlockState(),TownData.parse("minecraft:red_wool")),"Owner edit is preserved");
        h.assertTrue(!TownDecor.updateAuthoredDecor(level,pos,Blocks.GOLD_BLOCK.defaultBlockState(),TownData.parse("minecraft:blue_wool")),"Later season cannot reinterpret edit as authored");
        var encoded=TownUpgrades.Progress.CODEC.encodeStart(JsonOps.INSTANCE,TownUpgrades.progress(level)).getOrThrow();
        var decoded=TownUpgrades.Progress.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow();
        h.assertTrue(decoded.preservedDecor.contains(pos.asLong()),"Owner override survives world reload");
        var other=pos.east(4);level.setBlockAndUpdate(other,Blocks.OAK_PLANKS.defaultBlockState());
        h.assertTrue(TownDecor.updateAuthoredDecor(level,other,Blocks.OAK_PLANKS.defaultBlockState(),TownData.parse("minecraft:red_wool")),"Untouched seasonal cell still updates");
        h.succeed();
    }
    @GameTest public void partyRecognitionDoesNotResetPersonalQuota(GameTestHelper h){
        var first=h.makeMockServerPlayerInLevel();var second=h.makeMockServerPlayerInLevel();
        var parties=io.github.jimbozoomer.jugcraft.party.JugcraftParties.manager();
        parties.create(first.getUUID());parties.invite(first.getUUID(),second.getUUID(),0);
        var party=parties.partyOf(first.getUUID()).orElseThrow();
        parties.accept(second.getUUID(),party.id(),1);
        try{
            first.getInventory().setItem(0,new ItemStack(Items.WHEAT,16));
            second.getInventory().setItem(0,new ItemStack(Items.WHEAT,8));
            TownCommissions.deliver(first,0);TownCommissions.deliver(second,0);
            h.assertTrue(TownCommissions.partyDelivered(first,0)==24,"Party sees real contributions from both members");
            parties.leave(second.getUUID());
            var data=Jugs.data(h.getLevel().getServer());
            h.assertTrue(TownCommissions.progress(data,second.getUUID(),0,TownCommissions.cycle(second)).count()==8,"Leaving keeps personal progress");
            h.assertTrue(!TownCommissions.claim(data,second.getUUID(),0,TownCommissions.cycle(second)),"Party totals do not create free rewards");
        }finally{parties.leave(second.getUUID());parties.leave(first.getUUID());}
        h.succeed();
    }
    @GameTest public void boardRejectsRemoteAndDestroyedBoardRequests(GameTestHelper h){
        var level=h.getLevel();var profile=new com.mojang.authlib.GameProfile(UUID.randomUUID(),"civic-visitor");
        var player=new net.minecraft.server.level.ServerPlayer(level.getServer(),level,profile,net.minecraft.server.level.ClientInformation.createDefault());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection,player,net.minecraft.server.network.CommonListenerCookie.createInitial(profile,false));
        io.github.jimbozoomer.jugcraft.test.TestPlayers.track(h,player);
        var pos=h.absolutePos(new BlockPos(2,2,2));
        try{
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            level.setBlockAndUpdate(pos,JugcraftTown.COMMISSION_BOARD.defaultBlockState());
            player.snapTo(pos.getX()+1.5,pos.getY(),pos.getZ()+.5,0,0);
            player.getInventory().setItem(0,new ItemStack(Items.WHEAT,32));
            CommissionMenu.open(player,pos);var menu=(CommissionMenu)player.containerMenu;
            h.assertTrue(menu.stillValid(player),"Nearby visitor can use board");
            h.assertTrue(!menu.clickMenuButton(player,99),"Forged order ID is rejected");
            player.snapTo(pos.getX()+100,pos.getY(),pos.getZ(),0,0);
            h.assertTrue(!menu.clickMenuButton(player,0),"Remote delivery is rejected");
            player.snapTo(pos.getX()+1.5,pos.getY(),pos.getZ()+.5,0,0);
            level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
            h.assertTrue(!menu.clickMenuButton(player,0),"Removed board cannot consume items");
            h.assertTrue(player.getInventory().getItem(0).getCount()==32,"Rejected requests never take supplies");
        }finally{player.closeContainer();level.getServer().getPlayerList().remove(player);}
        h.succeed();
    }
}
