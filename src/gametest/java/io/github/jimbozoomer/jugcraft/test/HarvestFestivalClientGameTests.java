package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.GiantCornucopiaBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client game test for the Harvest Festival Grounds (decor set 10): part 1's Giant Cornucopia on the grass, facing the
 * camera, photographed by day from the front, from the west into its mouth and the harvest spilling out of it, and from
 * the north-east over its tail (CI job {@code client}). They go into docs/features/harvest-festival-grounds.md.
 */
public class HarvestFestivalClientGameTests implements FabricClientGameTest {
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
			server.runCommand("gamerule minecraft:advance_time false");
			server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 7, y - 1, z - 6, x + 6, y - 1, z + 6));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 7, y, z - 6, x + 6, y + 5, z + 6));
			server.runOnServer(minecraft -> cornucopia(minecraft.overworld(), origin.offset(1, 0, 1)));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			shoot(context, singleplayer, x, y + 1, z - 4, 0, 20, "jugcraft_harvest_festival_cornucopia");
			shoot(context, singleplayer, x - 5, y, z + 2, -90, 15, "jugcraft_harvest_festival_cornucopia_mouth");
			shoot(context, singleplayer, x + 4, y + 2, z - 3, 45, 30, "jugcraft_harvest_festival_cornucopia_tail");
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

	/** The Giant Cornucopia, its first block (its tail's) at {@code master}, facing north: its mouth to the west. */
	private static void cornucopia(ServerLevel level, BlockPos master) {
		GiantCornucopiaBlock block = (GiantCornucopiaBlock) JugcraftAgriculture.block("giant_cornucopia");
		BlockState state = block.defaultBlockState().setValue(GiantCornucopiaBlock.FACING, Direction.NORTH);
		level.setBlock(master, state, Block.UPDATE_ALL);
		block.setPlacedBy(level, master, state, null, new ItemStack(block));
	}
}
