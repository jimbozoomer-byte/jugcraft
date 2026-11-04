package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.tools.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Real native START/STOP/ABORT actions, survival breaking and independent plane/JE assertions. */
public final class DrillFaceGameTests {
    private static final BlockPos ROOT = new BlockPos(3, 4, 3);

    private static ServerPlayer player(GameTestHelper h) {
        var p = h.makeMockServerPlayerInLevel();
        p.setGameMode(GameType.SURVIVAL);
        p.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
        p.setPos(Vec3.atCenterOf(h.absolutePos(ROOT)).add(0, 0, -3));
        p.setOnGround(true);
        p.setXRot(0); p.setYRot(0);
        return p;
    }

    private static ItemStack drill(ServerPlayer p, int reach, long energy) {
        var tool = new ItemStack(JugcraftTools.MINING_DRILL);
        tool.set(JugcraftTools.RANGE, reach == 2 ? 1 : 0);
        tool.set(JugcraftTools.DRILL_MODE, reach == 2 ? MiningDrillItem.AREA_LARGE : MiningDrillItem.AREA);
        tool.set(JugcraftTools.EFFICIENCY, 2);
        Chargeable.setEnergy(tool, energy);
        p.setItemInHand(InteractionHand.MAIN_HAND, tool);
        return tool;
    }

    private static void cube(GameTestHelper h) {
        for (int x=-2;x<=2;x++) for (int y=-2;y<=2;y++) for (int z=-2;z<=2;z++)
            h.setBlock(ROOT.offset(x,y,z), Blocks.STONE);
    }

    private static void action(ServerPlayer p, BlockPos pos, Action action, Direction face) {
        p.gameMode.handleBlockBreakAction(pos, action, face, p.level().getMaxY(), 0);
    }

    private static void finish(ServerPlayer p, BlockPos pos) {
        // STOP may report another face after the player moves their view. START's face must win.
        p.setXRot(80); p.setYRot(90);
        for (int tick=0;tick<20&&!p.level().getBlockState(pos).isAir();tick++) p.gameMode.tick();
        action(p,pos,Action.STOP_DESTROY_BLOCK,Direction.EAST);
        for (int tick=0;tick<100&&!p.level().getBlockState(pos).isAir();tick++) p.gameMode.tick();
        if (!p.level().getBlockState(pos).isAir()) throw new AssertionError("Native mining did not finish");
    }

    private static void allFaces(GameTestHelper h, int reach) {
        var p=player(h); var root=h.absolutePos(ROOT);
        for (Direction face:Direction.values()) {
            cube(h); p.setXRot(0); p.setYRot(0); p.setOnGround(true);
            int count=(2*reach+1)*(2*reach+1);
            var tool=drill(p,reach,count*42L);
            action(p,root,Action.START_DESTROY_BLOCK,face);
            finish(p,root);
            for(int x=-2;x<=2;x++) for(int y=-2;y<=2;y++) for(int z=-2;z<=2;z++) {
                boolean inside=Math.abs(x)<=reach&&Math.abs(y)<=reach&&Math.abs(z)<=reach;
                boolean plane=switch(face.getAxis()){case X->x==0;case Y->y==0;case Z->z==0;};
                h.assertTrue(h.getBlockState(ROOT.offset(x,y,z)).isAir()==(inside&&plane),
                    "Wrong layer/bounds for "+face+" at "+x+","+y+","+z);
            }
            h.assertTrue(Chargeable.energy(tool)==0,"Incorrect discounted area price for "+face);
        }
        h.succeed();
    }

    @GameTest public void threeByThreeUsesAllSixStartingFaces(GameTestHelper h) { allFaces(h,1); }
    @GameTest public void fiveByFiveUsesAllSixStartingFaces(GameTestHelper h) { allFaces(h,2); }

    @GameTest public void absentAbortedOrReplacedInputFallsBackToOneBlock(GameTestHelper h) {
        var p=player(h);var root=h.absolutePos(ROOT);
        for(int scenario=0;scenario<5;scenario++) {
            cube(h); var tool=drill(p,1,1000);
            if(scenario>0) action(p,root,Action.START_DESTROY_BLOCK,Direction.UP);
            if(scenario==1) action(p,root,Action.ABORT_DESTROY_BLOCK,Direction.DOWN);
            if(scenario==2) {tool=tool.copy();p.setItemInHand(InteractionHand.MAIN_HAND,tool);}
            if(scenario==3) {
                var held=tool;p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.gameMode.tick();
                p.setItemInHand(InteractionHand.MAIN_HAND,held);
            }
            if(scenario==4) p.gameMode.setLevel(h.getLevel());
            p.gameMode.destroyBlock(root);
            int air=0;for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++)
                if(h.getBlockState(ROOT.offset(x,y,z)).isAir())air++;
            h.assertTrue(air==1&&Chargeable.energy(tool)==958,"Stale face survived scenario "+scenario);
        }
        h.succeed();
    }

    @GameTest public void instantBreakUsesItsFaceAndStillSkipsHarderBlocks(GameTestHelper h) {
        var p=player(h);var root=h.absolutePos(ROOT);var tool=drill(p,1,1000);
        tool.set(JugcraftTools.OVERCLOCK,2);p.setOnGround(true);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)h.setBlock(ROOT.offset(x,0,z),Blocks.DIRT);
        h.setBlock(ROOT.offset(1,0,1),Blocks.OBSIDIAN);
        h.setBlock(ROOT.offset(-1,0,-1),Blocks.CHEST);
        h.setBlock(ROOT.below(),Blocks.DIRT);
        action(p,root,Action.START_DESTROY_BLOCK,Direction.UP);
        h.assertTrue(h.getBlockState(ROOT).isAir(),"Fixture did not instant-mine");
        h.assertTrue(h.getBlockState(ROOT.offset(1,0,0)).isAir()&&h.getBlockState(ROOT.offset(0,0,1)).isAir(),"Instant face was lost");
        h.assertBlockPresent(Blocks.OBSIDIAN,ROOT.offset(1,0,1));h.assertBlockPresent(Blocks.CHEST,ROOT.offset(-1,0,-1));
        h.assertBlockPresent(Blocks.DIRT,ROOT.below());
        h.assertTrue(Chargeable.energy(tool)==1000-7*84,"Skipped blocks or instant break billed incorrectly");
        h.succeed();
    }

    @GameTest public void insufficientChargeStopsWithinTheChosenLayer(GameTestHelper h) {
        var p=player(h);cube(h);var root=h.absolutePos(ROOT);var tool=drill(p,1,3*42);
        action(p,root,Action.START_DESTROY_BLOCK,Direction.UP);finish(p,root);
        int air=0;for(int x=-2;x<=2;x++)for(int y=-2;y<=2;y++)for(int z=-2;z<=2;z++) {
            if(h.getBlockState(ROOT.offset(x,y,z)).isAir()) {air++;h.assertTrue(y==0,"Low JE rotated the plane");}
        }
        h.assertTrue(air==3&&Chargeable.energy(tool)==0,"Low JE broke unpaid blocks");h.succeed();
    }

    @GameTest public void concurrentPlayersKeepIndependentFaces(GameTestHelper h) {
        var one=player(h);var two=player(h);cube(h);var root=h.absolutePos(ROOT);
        var first=drill(one,1,1000);var second=drill(two,1,1000);
        action(one,root,Action.START_DESTROY_BLOCK,Direction.UP);
        action(two,root.above(),Action.START_DESTROY_BLOCK,Direction.EAST);
        finish(one,root);finish(two,root.above());
        for(int y=1;y<=2;y++)for(int z=-1;z<=1;z++)
            h.assertTrue(h.getBlockState(ROOT.offset(0,y,z)).isAir(),"Other player's face was lost");
        h.assertBlockPresent(Blocks.STONE,ROOT.offset(1,1,0));
        h.assertTrue(Chargeable.energy(first)==622&&Chargeable.energy(second)==748,"Players shared mining input or JE");
        h.succeed();
    }
}
