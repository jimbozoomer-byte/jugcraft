package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.tools.*;
import io.github.jimbozoomer.jugcraft.jimtafacts.industry.IndustrialGear;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/** Survival breaks and independent building-preservation/JE checks, including actual sapling generation. */
public final class ChainsawTreeGameTests {
    private static final BlockPos ROOT=new BlockPos(3,2,3);
    static void canopy(GameTestHelper h,BlockPos top,boolean persistent) {
        for(int x=-2;x<=2;x++)for(int y=-1;y<=1;y++)for(int z=-2;z<=2;z++) {
            var at=top.offset(x,y,z);
            if(!h.getBlockState(at).is(BlockTags.LOGS))h.setBlock(at,Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT,persistent).setValue(LeavesBlock.DISTANCE,1));
        }
    }
    private static void clear(GameTestHelper h) {
        for(var pos:BlockPos.betweenClosed(ROOT.offset(-3,-1,-3),ROOT.offset(3,8,3)))h.setBlock(pos,Blocks.AIR);
    }
    private static void tree(GameTestHelper h,boolean persistent) {
        clear(h);h.setBlock(ROOT.below(),Blocks.DIRT);
        for(int y=0;y<5;y++)h.setBlock(ROOT.above(y),Blocks.OAK_LOG);
        h.setBlock(ROOT.offset(1,3,1),Blocks.OAK_LOG);
        canopy(h,ROOT.above(4),persistent);
    }
    private static ServerPlayer player(GameTestHelper h) {
        var p=h.makeMockServerPlayerInLevel();p.setGameMode(GameType.SURVIVAL);
        p.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
        p.setPos(Vec3.atCenterOf(h.absolutePos(ROOT)).add(0,0,-3));return p;
    }
    private static ItemStack saw(ServerPlayer p,Item item,long energy,int efficiency) {
        var stack=new ItemStack(item);stack.set(JugcraftTools.EFFICIENCY,efficiency);Chargeable.setEnergy(stack,energy);
        p.setItemInHand(InteractionHand.MAIN_HAND,stack);return stack;
    }
    private static Map<BlockPos,BlockState> snapshot(GameTestHelper h) {
        var out=new HashMap<BlockPos,BlockState>();
        for(var pos:BlockPos.betweenClosed(ROOT.offset(-2,0,-2),ROOT.offset(2,6,2)))
            if(h.getBlockState(pos).is(BlockTags.LOGS))out.put(pos.immutable(),h.getBlockState(pos));
        return out;
    }
    private static void onlyOne(GameTestHelper h,ServerPlayer p) {
        var before=snapshot(h);var stack=saw(p,JugcraftTools.CHAINSAW,1000,0);
        h.assertTrue(ChainsawTree.targets(h.getLevel(),h.absolutePos(ROOT),h.getBlockState(ROOT)).size()==1,"Unsafe timber offered an area preview");
        p.gameMode.destroyBlock(h.absolutePos(ROOT));
        h.assertTrue(h.getBlockState(ROOT).isAir()&&Chargeable.energy(stack)==960,"Single fallback cost is wrong");
        before.forEach((at,state)->{if(!at.equals(ROOT))h.assertTrue(h.getBlockState(at).equals(state),"Building timber was felled: "+at);});
    }

    @GameTest public void bothChainsawsFellRootedBranchedTrees(GameTestHelper h) {
        var p=player(h);
        for(Item item:new Item[]{JugcraftTools.CHAINSAW,IndustrialGear.ITEMS.get("jimta_industrial_chainsaw")}) {
            tree(h,false);var logs=snapshot(h);var stack=saw(p,item,1000,2);
            h.assertTrue(ChainsawTree.targets(h.getLevel(),h.absolutePos(ROOT),h.getBlockState(ROOT)).size()==6,"Natural tree was not recognized");
            h.assertTrue(snapshot(h).equals(logs)&&Chargeable.energy(stack)==1000,"Recognition changed the tree or charge");
            p.gameMode.destroyBlock(h.absolutePos(ROOT));
            logs.keySet().forEach(at->h.assertTrue(h.getBlockState(at).isAir(),"Natural trunk/branch remained"));
            h.assertTrue(Chargeable.energy(stack)==832,"Six logs must cost 168 JE at Efficiency II");
            h.assertBlockPresent(Blocks.DIRT,ROOT.below());
        }h.succeed();
    }

    @GameTest public void bareLogCabinsAndDecorativeCanopiesStaySingle(GameTestHelper h) {
        var p=player(h);
        for(int type=0;type<3;type++) {
            tree(h,type!=2);
            if(type==0)for(var pos:BlockPos.betweenClosed(ROOT.offset(-2,2,-2),ROOT.offset(2,6,2)))
                if(h.getBlockState(pos).is(BlockTags.LEAVES))h.setBlock(pos,Blocks.AIR);
            if(type==2)for(var pos:BlockPos.betweenClosed(ROOT.offset(-2,2,-2),ROOT.offset(2,6,2)))
                if(h.getBlockState(pos).is(BlockTags.LEAVES))h.setBlock(pos,h.getBlockState(pos).setValue(LeavesBlock.DISTANCE,7));
            // Dirt foundations and log walls must not suffice, even beside a decorative/decaying roof.
            if(type==0){h.setBlock(ROOT.east(),Blocks.OAK_LOG);h.setBlock(ROOT.east().below(),Blocks.DIRT);}
            onlyOne(h,p);
        }h.succeed();
    }

    @GameTest public void workedTimberHorizontalCutsAndStoneFootingsStaySingle(GameTestHelper h) {
        var p=player(h);
        for(int type=0;type<4;type++) {
            tree(h,false);
            if(type<2)for(var at:snapshot(h).keySet())h.setBlock(at,(type==0?Blocks.STRIPPED_OAK_LOG:Blocks.OAK_WOOD).defaultBlockState());
            if(type==2)h.setBlock(ROOT,h.getBlockState(ROOT).setValue(BlockStateProperties.AXIS,Direction.Axis.X));
            if(type==3)h.setBlock(ROOT.below(),Blocks.STONE_BRICKS);
            onlyOne(h,p);
        }h.succeed();
    }

    @GameTest public void buildingsAndBeamsTouchingLivingTreesRejectBulkFelling(GameTestHelper h) {
        var p=player(h);
        for(Block building:new Block[]{Blocks.OAK_PLANKS,Blocks.OAK_STAIRS,Blocks.GLASS,Blocks.CHEST,Blocks.BIRCH_LOG}) {
            tree(h,false);h.setBlock(ROOT.above(2).east(),building);onlyOne(h,p);
            h.assertBlockPresent(building,ROOT.above(2).east());
        }
        tree(h,false);h.setBlock(ROOT.east(),Blocks.OAK_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));
        onlyOne(h,p);
        // A three-log tree's low crown must not make a beam at the second trunk layer look like a branch.
        clear(h);h.setBlock(ROOT.below(),Blocks.DIRT);for(int y=0;y<3;y++)h.setBlock(ROOT.above(y),Blocks.OAK_LOG);
        canopy(h,ROOT.above(2),false);
        h.setBlock(ROOT.above().east(),Blocks.OAK_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));
        onlyOne(h,p);h.succeed();
    }

    @GameTest public void largeTrunksRespectTheWholeTreeLimit(GameTestHelper h) {
        var p=player(h);
        for(int height:new int[]{32,33}) {
            for(var at:BlockPos.betweenClosed(ROOT.offset(-2,-1,-2),ROOT.offset(3,35,3)))h.setBlock(at,Blocks.AIR);
            for(int x=0;x<2;x++)for(int z=0;z<2;z++) {
                h.setBlock(ROOT.offset(x,-1,z),Blocks.DIRT);
                for(int y=0;y<height;y++)h.setBlock(ROOT.offset(x,y,z),Blocks.SPRUCE_LOG);
            }
            canopy(h,ROOT.above(height-1),false);var stack=saw(p,JugcraftTools.CHAINSAW,10000,2);
            int expected=height==32?128:1;
            h.assertTrue(ChainsawTree.targets(h.getLevel(),h.absolutePos(ROOT),h.getBlockState(ROOT)).size()==expected,"Tree limit did not fail closed");
            p.gameMode.destroyBlock(h.absolutePos(ROOT));int cut=0;
            for(int x=0;x<2;x++)for(int z=0;z<2;z++)for(int y=0;y<height;y++)if(h.getBlockState(ROOT.offset(x,y,z)).isAir())cut++;
            h.assertTrue(cut==expected&&Chargeable.energy(stack)==10000-28L*expected,"Limit partially felled ambiguous timber or billed incorrectly");
        }h.succeed();
    }

    @GameTest public void singleModeAndSneakingStillCutOneLog(GameTestHelper h) {
        var p=player(h);
        for(int type=0;type<2;type++) {
            tree(h,false);var stack=saw(p,JugcraftTools.CHAINSAW,1000,0);
            EquipmentSettings.setNumber(stack,"saw_mode",type==0?1:0);p.setShiftKeyDown(type==1);
            p.gameMode.destroyBlock(h.absolutePos(ROOT));h.assertBlockPresent(Blocks.OAK_LOG,ROOT.above());
            h.assertTrue(Chargeable.energy(stack)==960,"Single override billed bulk felling");
        }h.succeed();
    }

    @GameTest public void lowChargeAndMidTrunkCutsKeepTheirLimits(GameTestHelper h) {
        var p=player(h);tree(h,false);var logs=snapshot(h);var stack=saw(p,JugcraftTools.CHAINSAW,56,2);
        p.gameMode.destroyBlock(h.absolutePos(ROOT));
        long cut=logs.keySet().stream().filter(at->h.getBlockState(at).isAir()).count();
        h.assertTrue(cut==2&&Chargeable.energy(stack)==0,"Low charge felled unpaid logs");
        tree(h,false);stack=saw(p,JugcraftTools.CHAINSAW,1000,0);p.gameMode.destroyBlock(h.absolutePos(ROOT.above(2)));
        h.assertBlockPresent(Blocks.OAK_LOG,ROOT);h.assertBlockPresent(Blocks.OAK_LOG,ROOT.above());
        h.assertTrue(Chargeable.energy(stack)==840,"Mid-trunk cut changed upward-only felling");h.succeed();
    }

    @GameTest public void protectedTrunkStopsFellingWithoutExtraCharges(GameTestHelper h) {
        var p=player(h);tree(h,false);var stack=saw(p,JugcraftTools.CHAINSAW,1000,0);var veto=new AtomicBoolean(true);
        var blocked=h.absolutePos(ROOT.above());
        PlayerBlockBreakEvents.BEFORE.register((level,actor,pos,state,entity)->!(veto.get()&&actor==p&&pos.equals(blocked)));
        try {
            p.gameMode.destroyBlock(h.absolutePos(ROOT));h.assertBlockPresent(Blocks.OAK_LOG,ROOT.above());h.assertBlockPresent(Blocks.OAK_LOG,ROOT.above(4));
            h.assertTrue(Chargeable.energy(stack)==960,"Rejected extra blocks consumed JE");
        }finally{veto.set(false);}h.succeed();
    }

    @GameTest public void realSaplingGrownTreesAreRecognized(GameTestHelper h) {
        var level=h.getLevel();var at=new BlockPos(97000,100,-97000);var p=player(h);
        TreeGrower[] growers={TreeGrower.OAK,TreeGrower.BIRCH,TreeGrower.SPRUCE};Block[] saplings={Blocks.OAK_SAPLING,Blocks.BIRCH_SAPLING,Blocks.SPRUCE_SAPLING};
        for(int type=0;type<3;type++) {
            for(var pos:BlockPos.betweenClosed(at.offset(-5,0,-5),at.offset(5,20,5)))level.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
            level.setBlockAndUpdate(at.below(),Blocks.DIRT.defaultBlockState());level.setBlockAndUpdate(at,saplings[type].defaultBlockState());
            h.assertTrue(growers[type].growTree(level,level.getChunkSource().getGenerator(),at,level.getBlockState(at),RandomSource.create(10+type)),"Sapling did not grow");
            var targets=ChainsawTree.targets(level,at,level.getBlockState(at));h.assertTrue(targets.size()>1,"Real sapling tree was rejected: "+saplings[type]);
            var stack=saw(p,JugcraftTools.CHAINSAW,10000,0);p.gameMode.destroyBlock(at);
            targets.forEach(pos->h.assertTrue(level.getBlockState(pos).isAir(),"Grown tree log remained"));
            h.assertTrue(Chargeable.energy(stack)==10000-40L*targets.size(),"Generated tree JE mismatch");
        }h.succeed();
    }

    @GameTest public void unloadedBoundaryFallsBackWithoutLoadingTerrain(GameTestHelper h) {
        var level=h.getLevel();var at=new BlockPos(700015,90,700008);int flags=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
        level.setBlock(at.below(),Blocks.DIRT.defaultBlockState(),flags);
        for(int y=0;y<4;y++)level.setBlock(at.above(y),Blocks.OAK_LOG.defaultBlockState(),flags);
        h.assertTrue(!level.hasChunkAt(at.east()),"Unloaded-edge fixture already loaded its neighbor");
        h.assertTrue(ChainsawTree.targets(level,at,level.getBlockState(at)).size()==1,"Incomplete terrain offered bulk targets");
        h.assertTrue(!level.hasChunkAt(at.east()),"Detection loaded a neighboring chunk");h.succeed();
    }
}
