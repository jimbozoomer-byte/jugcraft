package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public class WinterTrimmingsGameTests {
    @GameTest public void winterMarketSnowBanksMatchTheFootingAndTurnWithThePlayer(GameTestHelper h) {
        var level=h.getLevel();
        var at=h.absolutePos(new BlockPos(1,3,1));
        for (String id : new String[]{"wind_carved_snowdrift","shovelled_snow_pile"}) {
            var block=BuiltInRegistries.BLOCK.getValue(Jugcraft.id(id));
            level.setBlockAndUpdate(at.below(),Blocks.STONE.defaultBlockState());
            var north=block.defaultBlockState();
            var east=north.setValue(HorizontalDirectionalBlock.FACING,Direction.EAST);
            var shape=north.getCollisionShape(level,at);
            int peak=(int)Math.round(16*shape.bounds().maxY);
            h.assertTrue(peak==(id.startsWith("wind") ? 5 : 9),"Walkable snow height matches model");
            h.assertTrue(!shape.toAabbs().equals(east.getCollisionShape(level,at).toAabbs()),"Snow profile rotates");
            level.setBlockAndUpdate(at,north);
            level.removeBlock(at.below(),false);
            h.assertTrue(level.isEmptyBlock(at),"Snow cannot float");
        }
        h.succeed();
    }

    @GameTest public void winterMarketIciclesNeedAnEaveAndAllowPassage(GameTestHelper h) {
        var level=h.getLevel();
        var at=h.absolutePos(new BlockPos(2,2,2));
        var fringe=BuiltInRegistries.BLOCK.getValue(Jugcraft.id("eave_icicle_fringe")).defaultBlockState();
        h.assertTrue(!fringe.canSurvive(level,at),"Fringe needs an eave");
        level.setBlockAndUpdate(at.above(),Blocks.SPRUCE_PLANKS.defaultBlockState());
        level.setBlockAndUpdate(at,fringe);
        h.assertTrue(fringe.canSurvive(level,at) && fringe.getCollisionShape(level,at).isEmpty(),"Walk below the fringe");
        level.removeBlock(at.above(),false);
        h.assertTrue(level.isEmptyBlock(at),"Fringe drops with removed eave");
        h.succeed();
    }
}
