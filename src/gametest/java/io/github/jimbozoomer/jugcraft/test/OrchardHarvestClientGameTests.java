package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.AppleBinBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FruitStoreBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PickingBagBlock;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Client game test for Orchard & Apple Harvest, part 1 (decor set 2): an apple tree with the Orchard Ladder against it,
 * an Apple Bin full of apples, another half full of pears, and a Picking Bag of apples hung on the trunk, built in a real
 * client and photographed from the front and the side by day (CI job {@code client}). The pictures go into
 * docs/features/orchard-apple-harvest.md.
 */
public class OrchardHarvestClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			int x = origin.getX();
			int y = origin.getY();
			int z = origin.getZ();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 6, y - 1, z - 8, x + 18, y - 1, z + 16));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 6, y, z - 8, x + 18, y + 12, z + 16));
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			shoot(context, singleplayer, x + 6, y + 2, z + 12, 180, 15, "jugcraft_orchard_harvest");
			shoot(context, singleplayer, x + 15, y + 3, z + 2, 90, 20, "jugcraft_orchard_harvest_side");
		}
	}

	/** Stands the player at a spot (on an invisible barrier) and takes a screenshot. */
	private static void shoot(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z,
			int yaw, int pitch, String name) {
		TestServerContext server = singleplayer.getServer();
		server.runCommand("setblock %d %d %d minecraft:barrier".formatted(x, y - 1, z));
		server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f %d %d", x + 0.5, y, z + 0.5, yaw, pitch));
		context.waitTicks(40);
		singleplayer.getConnection().waitForChunksRender();
		context.takeScreenshot(name);
	}

	private static void set(ServerLevel level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state, Block.UPDATE_ALL);
	}

	/** Places a prop facing south (towards the camera) with its first block at {@code master}. */
	private static void prop(ServerLevel level, String id, BlockPos master) {
		MultiDecorationBlock block = (MultiDecorationBlock) JugcraftAgriculture.block(id);
		BlockState state = block.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.SOUTH);
		set(level, master, state);
		block.setPlacedBy(level, master, state, null, new ItemStack(block));
	}

	private static void build(ServerLevel level, BlockPos origin) {
		// The apple tree: a trunk four logs tall under a crown of apple leaves.
		BlockPos trunk = origin.offset(6, 0, -1);
		for (int up = 0; up < 4; up++) {
			set(level, trunk.above(up), Blocks.OAK_LOG.defaultBlockState());
		}
		// (kept, so none decay before the pictures)
		BlockState leaves = JugcraftAgriculture.block("apple_leaves").defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				for (int up = 3; up <= 5; up++) {
					BlockPos at = trunk.offset(dx, up, dz);
					if ((Math.abs(dx) + Math.abs(dz) < 4 || up == 4) && level.getBlockState(at).isAir()) {
						set(level, at, leaves);
					}
				}
			}
		}
		// The ladder in front of it, its pole leaning back towards the trunk.
		prop(level, "orchard_ladder", origin.offset(6, 0, 2));
		// A bin full of apples, and one half full of pears.
		prop(level, "apple_bin", origin.offset(1, 0, 4));
		prop(level, "apple_bin", origin.offset(10, 0, 4));
		fill(level, origin.offset(1, 0, 4), new ItemStack(Items.APPLE, 64), 4);
		fill(level, origin.offset(10, 0, 4), new ItemStack(JugcraftAgriculture.item("pear"), 64), 2);
		// The picking bag on the trunk's east side, a few apples in it.
		BlockPos bag = trunk.offset(1, 1, 0);
		set(level, bag, JugcraftAgriculture.block("picking_bag").defaultBlockState().setValue(PickingBagBlock.FACING, Direction.EAST));
		if (level.getBlockEntity(bag) instanceof FruitStoreBlockEntity store) {
			store.add(new ItemStack(Items.APPLE, 12));
		}
	}

	private static void fill(ServerLevel level, BlockPos master, ItemStack stack, int stacks) {
		AppleBinBlock bin = (AppleBinBlock) JugcraftAgriculture.block("apple_bin");
		FruitStoreBlockEntity store = bin.store(level, master, level.getBlockState(master));
		for (int i = 0; store != null && i < stacks; i++) {
			store.add(stack.copy());
		}
	}
}
