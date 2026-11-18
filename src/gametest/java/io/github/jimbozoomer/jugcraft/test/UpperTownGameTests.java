package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.*;
import java.util.HashMap;
import java.util.HashSet;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class UpperTownGameTests {
    @GameTest public void alignedStopsTravelBothWaysAndStopAtObstructions(GameTestHelper h) {
        var level=h.getLevel();
        var at=h.absolutePos(new BlockPos(2,5,2));
        var top=at.above(5);
        var p=h.makeMockServerPlayerInLevel();
        p.setGameMode(GameType.SURVIVAL);
        for(int dy=0;dy<=7;dy++)
            TownBuilder.set(level,at.above(dy),dy==0||dy==5?CounterweightLift.BLOCK.defaultBlockState():Blocks.AIR.defaultBlockState());
        p.snapTo(at.getX()+.5,at.getY()+1,at.getZ()+.5,0,0);
        h.assertTrue(TownProtection.publicBlock(level,at,level.getBlockState(at)),"The city makes the lift publicly usable");
        TownBuilder.set(level,at.above(3),Blocks.IRON_BLOCK.defaultBlockState());
        h.assertTrue(!CounterweightLift.ride(p,at,false) && p.blockPosition().equals(at.above()),"A filled shaft blocks travel");
        TownBuilder.set(level,at.above(3),Blocks.AIR.defaultBlockState());
        TownBuilder.set(level,top.above(2),Blocks.STONE.defaultBlockState());
        h.assertTrue(!CounterweightLift.ride(p,at,false),"Blocked headroom prevents arrival");
        TownBuilder.set(level,top.above(2),Blocks.AIR.defaultBlockState());
        h.assertTrue(CounterweightLift.ride(p,at,false) && p.blockPosition().equals(top.above()),"Ride to the next floor");
        h.assertTrue(!CounterweightLift.ride(p,top,false),"There is no floor above the top");
        h.assertTrue(CounterweightLift.ride(p,top,true) && p.blockPosition().equals(at.above()),"Return to the original floor");
        h.succeed();
    }

    @GameTest public void editedRoofAndDormerRiseOnceAndForeignStorageIsPreserved(GameTestHelper h) {
        var level=h.getLevel();
        var template=UpperTown.houses().getFirst();
        var house=new UpperTown.House("test_west_gate",template.minRoof(),template.maxRoof(),template.changes());
        var min=house.minRoof(); var max=house.maxRoof();
        var origin=h.absolutePos(new BlockPos(3,58,3)).subtract(min);
        var cells=new HashSet<BlockPos>(house.changes().keySet());
        for(var p:BlockPos.betweenClosed(min.offset(-3,-3,-3),max.offset(3,4,3))) cells.add(p.immutable());
        var before=new HashMap<BlockPos,BlockState>();
        var data=TownData.get();
        try {
            for(var p:cells) {
                var world=origin.offset(p); var key=ChunkPos.containing(world).pack();
                TownState.get(level).markBuilt(key); TownUpgrades.markCurrent(level,key);
                before.put(world,level.getBlockState(world));
                TownBuilder.set(level,world,data.state(data.index(p.getX(),p.getY(),p.getZ())));
            }
            var ridge=origin.offset(34,19,104); var skylight=origin.offset(35,20,104);
            var dormer=origin.offset(30,16,102); var storage=origin.offset(29,12,101);
            var cap=WesternDistrictPlan.parse("minecraft:oxidized_cut_copper_stairs[facing=west]");
            var copper=WesternDistrictPlan.parse("minecraft:cut_copper");
            TownBuilder.set(level,ridge,cap);
            TownBuilder.set(level,skylight,Blocks.AIR.defaultBlockState());
            for(int i=1;i<=2;i++) TownBuilder.set(level,ridge.above(i),Blocks.BRICKS.defaultBlockState());
            TownBuilder.set(level,dormer,copper);
            TownBuilder.set(level,dormer.above(),Blocks.GLASS.defaultBlockState());
            TownBuilder.set(level,storage,Blocks.BARREL.defaultBlockState());
            var barrel=(net.minecraft.world.Container)level.getBlockEntity(storage);
            barrel.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,9));
            var blocked=ridge.above(5);
            TownBuilder.set(level,blocked,Blocks.CHEST.defaultBlockState());
            var chest=(net.minecraft.world.Container)level.getBlockEntity(blocked);
            chest.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD,4));
            h.assertTrue(UpperTown.retrofitHouse(level,origin,house,true)!=null,"Separate storage blocks the move before any write");
            h.assertTrue(level.getBlockState(ridge)==cap && level.getBlockState(skylight).isAir()
                && chest.getItem(0).getCount()==4,"Obstruction and all rooftop edits survive refusal");
            chest.clearContent(); TownBuilder.set(level,blocked,Blocks.AIR.defaultBlockState());
            String issue=UpperTown.retrofitHouse(level,origin,house,true);
            h.assertTrue(issue==null,"Edited roof should upgrade: "+issue);
            h.assertTrue(level.getBlockState(ridge.above(5))==cap && level.getBlockState(skylight.above(5)).isAir(),"Directional tile and actual hole moved");
            h.assertTrue(level.getBlockState(ridge.above(7)).is(Blocks.BRICKS)
                && level.getBlockState(dormer.above(5))==copper
                && level.getBlockState(dormer.above(6)).is(Blocks.GLASS),"Added chimney and projecting dormer rose intact");
            h.assertTrue(level.getBlockState(dormer).isAir() && barrel.getItem(0).getCount()==9,"No duplicate eave; unrelated storage is intact");
            h.assertTrue(UpperTown.retrofitHouse(level,origin,house,true)==null && level.getBlockState(ridge.above(5))==cap
                && level.getBlockState(ridge.above(10)).isAir(),"Repeating the retrofit does not raise it twice");
            barrel.clearContent();
            h.succeed();
        } finally {
            before.forEach((p,s)->TownBuilder.set(level,p,s));
        }
    }
}
