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
