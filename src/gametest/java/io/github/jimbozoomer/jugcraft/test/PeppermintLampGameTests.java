package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.PeppermintLamps;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class PeppermintLampGameTests {
    @GameTest public void winterMarketBothLampsTurnTheirCrooksLightAndDropOnce(GameTestHelper h) {
        var level = h.getLevel();
        var foot = h.absolutePos(new BlockPos(3, 2, 3));
        var player = player(h, foot);
        level.setBlockAndUpdate(foot.below(), Blocks.STONE.defaultBlockState());
        for (String id : List.of(PeppermintLamps.LANE, PeppermintLamps.BOULEVARD)) {
            var lamp = block(id);
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                player.setYRot(facing.getOpposite().toYRot());
                place(level, player, foot, new ItemStack(lamp));
                h.assertTrue(player.getMainHandItem().isEmpty(), "Survival placement spends one complete lamp");
                int glassPart = lamp.cells().length - 2;
                BlockPos glass = lamp.partPos(foot, facing, glassPart);
                for (int part = 0; part < lamp.cells().length; part++) {
                    var at = lamp.partPos(foot, facing, part);
                    var state = level.getBlockState(at);
                    h.assertTrue(state.is(lamp) && lamp.part(state) == part && state.getValue(PeppermintLamps.FACING) == facing,
                            id+" has the correct part at "+at+" when facing "+facing);
                    h.assertTrue(!state.getShape(level, at).isEmpty(), "Every reserved cell contains selectable geometry");
                    h.assertTrue(state.getLightEmission() == (part == glassPart ? 15 : 0), "Only the glass casts light");
                }
                h.assertTrue(level.getBlockState(foot.relative(facing.getCounterClockWise())).isAir(), "Clear ground beneath crook");
                var hit = new BlockHitResult(Vec3.atCenterOf(glass), Direction.UP, glass, false);
                player.gameMode.useItemOn(player, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit);
                h.assertTrue(!level.getBlockState(foot).getValue(PeppermintLamps.LIT)
                        && level.getBlockState(glass).getLightEmission() == 0, "Clicking the lantern snuffs the whole post");
                level.destroyBlock(glass, true);
                for (int i=0; i<lamp.cells().length; i++)
                    h.assertTrue(level.getBlockState(lamp.partPos(foot, facing, i)).isAir(), "Breaking the lantern takes every part down");
                var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(foot).inflate(6), e -> e.getItem().is(lamp.asItem()));
                h.assertTrue(drops.stream().mapToInt(e -> e.getItem().getCount()).sum() == 1, "Exactly one whole lamp drops");
                drops.forEach(ItemEntity::discard);
            }
        }
        h.succeed();
    }

    @GameTest public void winterMarketChecksAllOccupiedCellsAndTheFooting(GameTestHelper h) {
        var level = h.getLevel();
        var foot = h.absolutePos(new BlockPos(3, 2, 3));
        var player = player(h, foot);
        player.setYRot(0);
        var facing = Direction.NORTH;
        for (String name : List.of(PeppermintLamps.LANE, PeppermintLamps.BOULEVARD)) {
            var lamp = block(name);
            var end = lamp.partPos(foot, facing, lamp.cells().length - 1);
            level.setBlockAndUpdate(foot.below(), Blocks.STONE.defaultBlockState());
            for (var obstruction : List.of(Blocks.OAK_PLANKS, Blocks.WATER)) {
                level.setBlockAndUpdate(end, obstruction.defaultBlockState());
                place(level, player, foot, new ItemStack(lamp));
                h.assertTrue(player.getMainHandItem().getCount() == 1 && level.getBlockState(end).is(obstruction)
                        && level.getBlockState(foot).isAir(), "A blocked crook spends and changes nothing");
                level.setBlockAndUpdate(end, Blocks.AIR.defaultBlockState());
            }
            place(level, player, foot, new ItemStack(lamp));
            h.assertTrue(level.getBlockState(foot).is(lamp), "Footing holds the lamp");
            level.destroyBlock(foot.below(), false);
            h.assertTrue(level.getBlockState(foot).isAir() && level.getBlockState(end).isAir(), "Removing the footing clears the lamp");
        }
        h.succeed();
    }

    private static PeppermintLamps block(String name) {
        return (PeppermintLamps) BuiltInRegistries.BLOCK.getValue(Jugcraft.id(name));
    }

    private static ServerPlayer player(GameTestHelper h, BlockPos foot) {
        var player = h.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(foot.getX() + 2.5, foot.getY(), foot.getZ() + 2.5);
        return player;
    }

    private static void place(ServerLevel level, ServerPlayer player, BlockPos foot, ItemStack stack) {
        var below = foot.below();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(below).relative(Direction.UP,.5), Direction.UP, below, false)));
    }
}

