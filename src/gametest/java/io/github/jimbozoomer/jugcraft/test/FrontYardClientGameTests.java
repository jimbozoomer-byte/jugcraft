package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.HauntedMailboxBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
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
 * Client game test for Front-Yard Haunt Scenes, part 1 (decor set 7): a front path of gravel across a lawn, a Haunted
 * Mailbox at the street end with its flag up, and three Pumpkin Path Lights along the path, lit, their lanterns swaying
 * (drawn by client/PumpkinPathLightRenderer). Photographed from along the path, from far enough that the mailbox stays
 * shut; from the street close by the mailbox, near enough that its door falls open and its eyes stare out
 * (client/HauntedMailboxRenderer); and by night (CI job {@code client}). They go into docs/features/front-yard-haunt.md.
 */
public class FrontYardClientGameTests implements FabricClientGameTest {
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
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 6, y - 1, z - 8, x + 6, y - 1, z + 12));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 6, y, z - 8, x + 6, y + 5, z + 12));
			server.runCommand("fill %d %d %d %d %d %d minecraft:gravel".formatted(x, y - 1, z - 8, x + 1, y - 1, z + 12));
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			shoot(context, singleplayer, x + 4, y + 1, z + 11, 160, 15, "jugcraft_front_yard_path");
			// From the street, three blocks from the mailbox: near enough for its door to fall open on the camera.
			shoot(context, singleplayer, x - 1, y + 1, z - 6, 0, 15, "jugcraft_front_yard_mailbox");
			server.runCommand("time set midnight");
			shoot(context, singleplayer, x + 4, y + 1, z + 11, 160, 15, "jugcraft_front_yard_night");
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

	/** Stands a two-high piece at {@code at} facing {@code facing}, each of its blocks set as {@code dressed} says. */
	private static void stand(ServerLevel level, MultiDecorationBlock block, BlockPos at, Direction facing,
			java.util.function.UnaryOperator<BlockState> dressed) {
		BlockState state = block.defaultBlockState().setValue(MultiDecorationBlock.FACING, facing);
		level.setBlock(at, state, Block.UPDATE_ALL);
		block.setPlacedBy(level, at, state, null, new ItemStack(block));
		for (int part = 0; part < block.cells().length; part++) {
			BlockPos there = block.partPos(at, facing, part);
			level.setBlock(there, dressed.apply(level.getBlockState(there)), Block.UPDATE_ALL);
		}
	}

	private static void build(ServerLevel level, BlockPos origin) {
		// The mailbox at the street end of the path (to the north), its door to the street, its flag up.
		stand(level, (MultiDecorationBlock) JugcraftAgriculture.block("haunted_mailbox"), origin.offset(-1, 0, -3), Direction.NORTH,
				state -> state.setValue(HauntedMailboxBlock.FLAG, true));
		// Three path lights down the path's east side, their crooks over it, lit.
		MultiDecorationBlock light = (MultiDecorationBlock) JugcraftAgriculture.block("pumpkin_path_light");
		for (int k = 0; k < 3; k++) {
			stand(level, light, origin.offset(2, 0, 1 + 4 * k), Direction.WEST, state -> state.setValue(MultiDecorationBlock.LIT, true));
		}
	}
}
