package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.CottonCandyCartBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PopcornWagonBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ShootingGalleryBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ShootingGalleryBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.TicketBoothBlock;
import io.github.jimbozoomer.jugcraft.agriculture.WhackAZombieBlock;
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
 * Client game test for Haunted Midway, part 1 (decor set 6): on a strip of gravel midway, two Cotton-Candy Carts, one
 * running with its bowl full of floss (its spinner head drawn by client/CottonCandyCartRenderer) and one switched off and
 * empty, and two Caramel-Corn Wagons beside them, one lit with its case full of popcorn (its kettle drawn by
 * client/PopcornWagonRenderer) and one out and empty; and part 2's Ticket Booth with a full roll and a Whack-a-Zombie
 * cabinet to the west of them, a head up (drawn by client/WhackAZombieRenderer) and five lamps lit; and part 3's Shooting
 * Gallery to the east, a round on, its bulbs lit and three targets knocked down (the targets drawn by
 * client/ShootingGalleryRenderer); built in a real client and photographed from the midway, close by at each stall, at
 * the games, at the gallery and by night (CI job {@code client}).
 * They go into docs/features/haunted-midway.md.
 */
public class HauntedMidwayClientGameTests implements FabricClientGameTest {
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
			server.runCommand("fill %d %d %d %d %d %d minecraft:gravel".formatted(x - 8, y - 1, z - 3, x + 12, y - 1, z + 8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 8, y, z - 3, x + 12, y + 6, z + 8));
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			shoot(context, singleplayer, x + 2, y + 1, z + 6, 180, 10, "jugcraft_midway_stalls");
			shoot(context, singleplayer, x, y + 1, z + 2, 180, 25, "jugcraft_midway_cotton_candy");
			shoot(context, singleplayer, x + 4, y + 1, z + 2, 180, 20, "jugcraft_midway_popcorn");
			shoot(context, singleplayer, x - 5, y + 1, z + 3, 180, 25, "jugcraft_midway_games");
			shoot(context, singleplayer, x + 10, y + 1, z + 5, 180, 10, "jugcraft_midway_gallery");
			server.runCommand("time set midnight");
			shoot(context, singleplayer, x + 2, y + 1, z + 5, 180, 10, "jugcraft_midway_night");
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

	/** Stands a two-high stall at {@code at} facing south (the camera), each of its blocks set as {@code dressed} says. */
	private static void stall(ServerLevel level, MultiDecorationBlock block, BlockPos at, java.util.function.UnaryOperator<BlockState> dressed) {
		BlockState state = block.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.SOUTH);
		level.setBlock(at, state, Block.UPDATE_ALL);
		block.setPlacedBy(level, at, state, null, new ItemStack(block));
		for (int part = 0; part < block.cells().length; part++) {
			BlockPos there = block.partPos(at, Direction.SOUTH, part);
			level.setBlock(there, dressed.apply(level.getBlockState(there)), Block.UPDATE_ALL);
		}
	}

	private static void build(ServerLevel level, BlockPos origin) {
		CottonCandyCartBlock cart = (CottonCandyCartBlock) JugcraftAgriculture.block("cotton_candy_cart");
		PopcornWagonBlock wagon = (PopcornWagonBlock) JugcraftAgriculture.block("popcorn_wagon");
		// The carts to the west, one running and full of floss, one off and empty; the wagons to the east, one lit and full.
		stall(level, cart, origin.offset(0, 0, 0), state -> state.setValue(MultiDecorationBlock.LIT, true)
				.setValue(CottonCandyCartBlock.FLOSS_LEVEL, CottonCandyCartBlock.FLOSS));
		stall(level, cart, origin.offset(-2, 0, 0), state -> state);
		stall(level, wagon, origin.offset(4, 0, 0), state -> state.setValue(MultiDecorationBlock.LIT, true)
				.setValue(PopcornWagonBlock.POPCORN, PopcornWagonBlock.CAPACITY));
		stall(level, wagon, origin.offset(6, 0, 0), state -> state);
		// Part 2, to the west: the ticket booth with a full roll, and Whack-a-Zombie with a head up and five lamps lit.
		stall(level, (TicketBoothBlock) JugcraftAgriculture.block("ticket_booth"), origin.offset(-4, 0, 0),
				state -> state.setValue(TicketBoothBlock.ROLL_LEFT, TicketBoothBlock.ROLL));
		stall(level, (WhackAZombieBlock) JugcraftAgriculture.block("whack_a_zombie"), origin.offset(-6, 0, 0),
				state -> state.setValue(WhackAZombieBlock.HEAD, 2).setValue(WhackAZombieBlock.SCORE, 5));
		// Part 3, to the east: the shooting gallery (its blocks running east from its first), a round on and three targets down.
		BlockPos gallery = origin.offset(9, 0, 0);
		stall(level, (ShootingGalleryBlock) JugcraftAgriculture.block("shooting_gallery"), gallery, state -> state.setValue(MultiDecorationBlock.LIT, true));
		if (level.getBlockEntity(gallery) instanceof ShootingGalleryBlockEntity round) {
			round.start(new java.util.UUID(0L, 0L), level.getGameTime() + 100000L);
			for (int target : new int[] {1, 4, 6}) {
				round.knock(target);
			}
		}
	}
}
