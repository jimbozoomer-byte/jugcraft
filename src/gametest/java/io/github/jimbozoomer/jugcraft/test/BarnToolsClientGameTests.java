package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.ButterChurnBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ButterChurnBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.HandWorkedBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.QuernBlock;
import io.github.jimbozoomer.jugcraft.agriculture.QuernBlockEntity;
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

/**
 * Client game test for Barn & Harvest Tools, part 2 (decor set 3): two Querns on a plank floor, one with wheat in its
 * eye, and two Butter Churns, one holding milk, built in a real client and photographed from the front and close by, by
 * day (CI job {@code client}). The moving parts (the quern's upper stone and peg, the churn's dasher) are drawn by their
 * renderer, so the pictures show it draws them. The pictures go into docs/features/barn-and-harvest-tools.md.
 */
public class BarnToolsClientGameTests implements FabricClientGameTest {
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
			server.runCommand("fill %d %d %d %d %d %d minecraft:spruce_planks".formatted(x - 4, y - 1, z - 4, x + 8, y - 1, z + 8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 4, y, z - 4, x + 8, y + 6, z + 8));
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			shoot(context, singleplayer, x + 2, y + 1, z + 6, 180, 25, "jugcraft_barn_tools");
			shoot(context, singleplayer, x + 1, y + 1, z + 3, 180, 40, "jugcraft_barn_tools_close");
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

	/** A station facing south, towards the camera. */
	private static BlockState facingSouth(String id) {
		return JugcraftAgriculture.block(id).defaultBlockState().setValue(HandWorkedBlock.FACING, Direction.SOUTH);
	}

	private static void build(ServerLevel level, BlockPos origin) {
		BlockPos loaded = origin.offset(0, 0, 1);
		level.setBlock(loaded, facingSouth("quern").setValue(QuernBlock.LOADED, true), Block.UPDATE_ALL);
		if (level.getBlockEntity(loaded) instanceof QuernBlockEntity quern) {
			quern.add(new ItemStack(Items.WHEAT, 12));
		}
		level.setBlock(origin.offset(1, 0, 1), facingSouth("quern"), Block.UPDATE_ALL);
		BlockPos full = origin.offset(3, 0, 1);
		level.setBlock(full, facingSouth("butter_churn").setValue(ButterChurnBlock.FULL, true), Block.UPDATE_ALL);
		if (level.getBlockEntity(full) instanceof ButterChurnBlockEntity churn) {
			churn.fill();
		}
		level.setBlock(origin.offset(4, 0, 1), facingSouth("butter_churn"), Block.UPDATE_ALL);
		// A hay bale and a barrel behind them, for scale.
		level.setBlock(origin.offset(2, 0, -1), Blocks.HAY_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(origin.offset(5, 0, -1), Blocks.BARREL.defaultBlockState(), Block.UPDATE_ALL);
	}
}
