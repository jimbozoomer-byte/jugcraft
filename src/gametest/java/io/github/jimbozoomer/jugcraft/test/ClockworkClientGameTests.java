package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.AnimatronicWerewolfBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ClockworkRavenBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FlyingCrankGhostBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.OrganGrinderSkeletonBlock;
import io.github.jimbozoomer.jugcraft.kinetic.HandCrankBlock;
import io.github.jimbozoomer.jugcraft.kinetic.HandCrankBlockEntity;
import io.github.jimbozoomer.jugcraft.kinetic.JugcraftKinetics;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Client game test for the Clockwork Animatronics, parts 1 to 4 (decor set 9): on a dark oak floor, two Organ-Grinder
 * Skeletons, one facing the camera (north) and one facing east, and an Animatronic Werewolf facing the camera, each with a
 * hand crank against the back of its lower block. Photographed at rest from the front; then with their cranks turned
 * and their clockwork running, from the front and from the east, where the organ's crank is and the skeleton's right
 * hand on it (drawn by client/OrganGrinderSkeletonRenderer); and the werewolf, its turntable turning, in its howl
 * (client/AnimatronicWerewolfRenderer); and, part 3, a Flying Crank Ghost facing the camera, its crank behind it, at
 * rest and then flying, its arms up in turn and its eyes lit (client/FlyingCrankGhostRenderer); and, part 4, a
 * Clockwork Raven facing the camera, its crank behind it, at rest and then in its first burst of caws, its beak open
 * and its wings up (client/ClockworkRavenRenderer; CI job {@code client}). They go into
 * docs/features/clockwork-animatronics.md.
 */
public class ClockworkClientGameTests implements FabricClientGameTest {
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
			server.runCommand("fill %d %d %d %d %d %d minecraft:dark_oak_planks".formatted(x - 5, y - 1, z - 9, x + 5, y - 1, z + 5));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 5, y, z - 9, x + 5, y + 4, z + 5));
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			// At rest, from the front.
			shoot(context, singleplayer, x, y, z - 3, 0, 10, "jugcraft_clockwork_organ_grinder");
			// Running: their cranks turned, from the front and then from the east of the one facing north.
			server.runOnServer(minecraft -> crank(minecraft.overworld(), origin));
			shoot(context, singleplayer, x, y, z - 3, 0, 10, "jugcraft_clockwork_organ_grinder_playing");
			shoot(context, singleplayer, x + 3, y + 1, z + 1, 90, 20, "jugcraft_clockwork_organ_grinder_crank");
			// The werewolf in its first howl, its arms up and its head back (it howls from 112 to 160 ticks into its run).
			context.waitTicks(Math.max(0, AnimatronicWerewolfBlock.howlStart() + AnimatronicWerewolfBlock.RAISE_TICKS - 120));
			shoot(context, singleplayer, x + 3, y, z - 3, 0, 5, "jugcraft_clockwork_werewolf_howl");
			// The ghost, at rest; then flying, its crank turned and its eyes lit, from the front and, half a turn on, from the
			// side.
			server.runOnServer(minecraft -> place(minecraft.overworld(), origin.offset(0, 0, -4), Direction.NORTH, "flying_crank_ghost"));
			shoot(context, singleplayer, x, y + 1, z - 8, 0, 10, "jugcraft_clockwork_crank_ghost");
			server.runOnServer(minecraft -> turn(minecraft.overworld(), origin.offset(0, 0, -3)));
			shoot(context, singleplayer, x, y + 1, z - 8, 0, 10, "jugcraft_clockwork_crank_ghost_flying");
			context.waitTicks(FlyingCrankGhostBlock.CRANK_TICKS / 2);
			shoot(context, singleplayer, x - 3, y + 1, z - 7, -45, 15, "jugcraft_clockwork_crank_ghost_side");
			// The raven, at rest; then in its first burst of caws, a caw in, its beak open and its wings up.
			server.runOnServer(minecraft -> place(minecraft.overworld(), origin.offset(-3, 0, -4), Direction.NORTH, "clockwork_raven"));
			shoot(context, singleplayer, x - 3, y + 1, z - 6, 0, 20, "jugcraft_clockwork_raven");
			server.runOnServer(minecraft -> turn(minecraft.overworld(), origin.offset(-3, 0, -3)));
			context.waitTicks(ClockworkRavenBlock.BURST - 40);
			shoot(context, singleplayer, x - 3, y + 1, z - 6, 0, 20, "jugcraft_clockwork_raven_caw");
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

	/** The two figures, and a hand crank against the back of each one's lower block, facing into it. */
	private static void build(ServerLevel level, BlockPos origin) {
		place(level, origin.offset(0, 0, 1), Direction.NORTH, "organ_grinder_skeleton");
		place(level, origin.offset(-3, 0, 1), Direction.EAST, "organ_grinder_skeleton");
		place(level, origin.offset(3, 0, 1), Direction.NORTH, "animatronic_werewolf");
	}

	private static void place(ServerLevel level, BlockPos lower, Direction facing, String id) {
		BlockState figure = JugcraftAgriculture.block(id).defaultBlockState().setValue(OrganGrinderSkeletonBlock.FACING, facing);
		level.setBlock(lower, figure, Block.UPDATE_ALL);
		level.setBlock(lower.above(), figure.setValue(OrganGrinderSkeletonBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
		level.setBlock(lower.relative(facing.getOpposite()), JugcraftKinetics.HAND_CRANK.defaultBlockState().setValue(HandCrankBlock.FACING, facing),
				Block.UPDATE_ALL);
	}

	/** Sets the figures' hand cranks turning, as long as a crank keeps going. */
	private static void crank(ServerLevel level, BlockPos origin) {
		for (BlockPos at : new BlockPos[] {origin.offset(0, 0, 2), origin.offset(-4, 0, 1), origin.offset(3, 0, 2)}) {
			turn(level, at);
		}
	}

	private static void turn(ServerLevel level, BlockPos at) {
		if (level.getBlockEntity(at) instanceof HandCrankBlockEntity crank) {
			crank.addTurns(HandCrankBlockEntity.MAX_TICKS);
		}
	}
}
