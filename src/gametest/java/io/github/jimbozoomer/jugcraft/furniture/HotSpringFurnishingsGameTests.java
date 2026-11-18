package io.github.jimbozoomer.jugcraft.furniture;

import io.github.jimbozoomer.jugcraft.agriculture.Seat;
import io.github.jimbozoomer.jugcraft.tower.FurnitureBlock;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class HotSpringFurnishingsGameTests {
    @GameTest public void winterMarketKitOpensTogetherOrStaysPacked(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        var inventory = player.getInventory();
        inventory.clearContent();
        for (int i = 0; i < inventory.getNonEquipmentItems().size(); i++)
            inventory.setItem(i, new ItemStack(Items.COBBLESTONE, 64));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(HotSpringFurnishings.CHANGING_KIT));
        HotSpringFurnishings.CHANGING_KIT.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        h.assertTrue(player.getMainHandItem().is(HotSpringFurnishings.CHANGING_KIT), "A full bag retains the intact kit");
        inventory.setItem(10, ItemStack.EMPTY);
        inventory.setItem(11, ItemStack.EMPTY);
        HotSpringFurnishings.CHANGING_KIT.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        for (var block : List.of(HotSpringFurnishings.BENCH, HotSpringFurnishings.PEG_RAIL, HotSpringFurnishings.BOOT_TRAY))
            h.assertTrue(inventory.countItem(block.asItem()) == 1, "The changing kit delivers one " + block);
        h.assertTrue(inventory.countItem(HotSpringFurnishings.CHANGING_KIT) == 0, "One kit was consumed");
        inventory.clearContent();
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(HotSpringFurnishings.POOL_KIT));
        HotSpringFurnishings.POOL_KIT.use(h.getLevel(), player, InteractionHand.OFF_HAND);
        for (var block : List.of(HotSpringFurnishings.SPOUT, HotSpringFurnishings.PAIL, HotSpringFurnishings.LANTERN))
            h.assertTrue(inventory.countItem(block.asItem()) == 1, "The pool kit delivers one " + block);
        h.assertTrue(player.getOffhandItem().isEmpty(), "Offhand unpack consumes the pool kit");
        h.succeed();
    }

    @GameTest public void winterMarketSpringhouseBenchSeatsAndThePoolLanternLights(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(2,2,2));
        var level = h.getLevel();
        var player = h.makeMockServerPlayerInLevel();
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        var bench = HotSpringFurnishings.BENCH.defaultBlockState();
        level.setBlockAndUpdate(pos, bench);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        bench.useWithoutItem(level, player, hit);
        h.assertTrue(player.getVehicle() instanceof Seat, "The slatted bench is a working seat");
        player.stopRiding();
        level.setBlockAndUpdate(pos, HotSpringFurnishings.LANTERN.defaultBlockState());
        h.assertTrue(level.getBlockState(pos).getLightEmission() == 13, "The pool lantern lights the water's edge");
        level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
        h.assertTrue(level.getBlockState(pos).isAir(), "The unsupported lantern comes down");
        h.succeed();
    }

    @GameTest public void winterMarketInletFeedsOnlyTheBackOfAnOpenValve(GameTestHelper h) {
        var level = h.getLevel();
        var pos = h.absolutePos(new BlockPos(2,2,2));
        var player = h.makeMockServerPlayerInLevel();
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        var state = HotSpringFurnishings.SPOUT.defaultBlockState().setValue(FurnitureBlock.FACING, Direction.WEST);
        level.setBlockAndUpdate(pos, state);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        state.useWithoutItem(level, player, hit);
        h.assertTrue(level.getBlockState(pos).getValue(HotSpringFurnishings.OverflowSpout.OPEN), "The valve turns");
        level.setBlockAndUpdate(pos.north(), Blocks.WATER.defaultBlockState());
        h.assertTrue(!level.getBlockState(pos).getValue(HotSpringFurnishings.OverflowSpout.FLOWING), "Side water is not an inlet");
        level.setBlockAndUpdate(pos.east(), Blocks.WATER.defaultBlockState());
        h.assertTrue(level.getBlockState(pos).getValue(HotSpringFurnishings.OverflowSpout.FLOWING), "Source behind the west-facing tap makes a stream");
        level.setBlockAndUpdate(pos.east(), Blocks.AIR.defaultBlockState());
        h.assertTrue(!level.getBlockState(pos).getValue(HotSpringFurnishings.OverflowSpout.FLOWING), "A cut supply dries the stream");
        level.setBlockAndUpdate(pos.east(), Blocks.WATER.defaultBlockState());
        level.getBlockState(pos).useWithoutItem(level, player, hit);
        h.assertTrue(!level.getBlockState(pos).getValue(HotSpringFurnishings.OverflowSpout.FLOWING)
                && level.getBlockState(pos.east()).is(Blocks.WATER), "Closing the valve leaves the water supply in place");
        h.succeed();
    }
}

