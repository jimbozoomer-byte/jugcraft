package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.season.SeasonCalendar;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client game test for the wood repaint: every tree the mod adds, grown side by side in summer, with a sample of its wood
 * (log, stripped log, planks, log ends, slab, stairs, fence and gate) on a wall before them, and the seasonal trees again
 * in autumn. Screenshots only, for the owner to judge the textures in game; each tree's logs and leaves are logged (CI
 * job {@code client}).
 */
public class WoodClientGameTests implements FabricClientGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger("jugcraft-test");
	/** Each wood, the tree feature that grows it and that tree's leaves. */
	private static final List<String[]> TREES = List.of(
			new String[] {"chestnut", "chestnut", "chestnut_leaves"},
			new String[] {"larch", "larch", "larch_needles"},
			new String[] {"maple", "maple", "maple_leaves"},
			new String[] {"aspen", "aspen", "aspen_leaves"},
			new String[] {"fir", "fir", "fir_needles"},
			new String[] {"dead", "dead_tree", ""},
			new String[] {"jacaranda", "jacaranda", "jacaranda_leaves"},
			new String[] {"willow", "willow", "willow_leaves"},
			new String[] {"palm", "palm", "palm_fronds"},
			new String[] {"cypress", "cypress", "cypress_leaves"},
			new String[] {"redwood", "redwood", "redwood_needles"},
			new String[] {"eucalyptus", "eucalyptus", "eucalyptus_leaves"},
			new String[] {"mahogany", "mahogany", "mahogany_leaves"});
	/** The trees whose leaves turn, grown again in autumn. */
	private static final List<String[]> AUTUMN = List.of(TREES.get(1), TREES.get(2), TREES.get(3), TREES.get(7));
	private static final int SPACING = 8;

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
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("gamerule minecraft:advance_time false");
			server.runCommand("gamerule minecraft:random_tick_speed 0");
			// A grass field, cleared in slices (each fill within the block limit).
			int west = x - 54;
			int east = x + 50;
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(west, y - 1, z - 28, east, y - 1, z + 50));
			for (int sx = west; sx <= east; sx += 8) {
				server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(sx, y, z - 28, Math.min(east, sx + 7), y + 29, z + 50));
			}
			context.waitTicks(10);

			// Every tree in summer, in a row along z - 20.
			SeasonCalendar.Mode before = server.computeOnServer(minecraft -> JugcraftSeasons.settings().mode());
			server.runOnServer(minecraft -> JugcraftSeasons.setMode(minecraft, SeasonCalendar.Mode.SUMMER));
			int row = z - 20;
			int first = x - 50;
			for (int i = 0; i < TREES.size(); i++) {
				server.runCommand("place feature jugcraft:%s %d %d %d".formatted(TREES.get(i)[1], first + i * SPACING, y, row));
			}
			// The seasonal trees again in autumn, in a row along z + 30.
			server.runOnServer(minecraft -> JugcraftSeasons.setMode(minecraft, SeasonCalendar.Mode.AUTUMN));
			int autumnRow = z + 30;
			int autumnFirst = x + 10;
			for (int i = 0; i < AUTUMN.size(); i++) {
				server.runCommand("place feature jugcraft:%s %d %d %d".formatted(AUTUMN.get(i)[1], autumnFirst + i * SPACING, y, autumnRow));
			}
			server.runOnServer(minecraft -> JugcraftSeasons.setMode(minecraft, before));
			for (int i = 0; i < TREES.size(); i++) {
				String[] tree = TREES.get(i);
				BlockPos trunk = new BlockPos(first + i * SPACING, y, row);
				int[] counts = server.computeOnServer(minecraft -> count(minecraft.overworld(), trunk, tree));
				LOGGER.info("[wood] {} grown: {} logs, {} leaves", tree[0], counts[0], counts[1]);
			}

			// A sample of each wood on a wall facing the cameras, along z + 6: log, stripped log and planks up the face,
			// a log end, a stripped end and a slab on top, and stairs, a fence and a gate before it.
			int wall = z + 6;
			for (int i = 0; i < TREES.size(); i++) {
				String wood = TREES.get(i)[0];
				int at = first + i * 4;
				for (int up = 0; up < 2; up++) {
					set(server, at, y + up, wall, wood + "_log[axis=y]");
					set(server, at + 1, y + up, wall, "stripped_" + wood + "_log[axis=y]");
					set(server, at + 2, y + up, wall, wood + "_planks");
				}
				set(server, at, y + 2, wall, wood + "_log[axis=z]");
				set(server, at + 1, y + 2, wall, "stripped_" + wood + "_log[axis=z]");
				set(server, at + 2, y + 2, wall, wood + "_slab[type=bottom]");
				set(server, at, y, wall + 1, wood + "_stairs[facing=north]");
				set(server, at + 1, y, wall + 1, wood + "_fence");
				set(server, at + 2, y, wall + 1, wood + "_fence_gate[facing=south]");
			}
			context.waitTicks(20);

			context.getInput().pressKey(options -> options.keyToggleGui);
			// The summer trees, three or so to a shot, from in front and a little below their crowns.
			int[][] groups = {{0, 2}, {3, 5}, {6, 8}, {9, 10}, {11, 12}};
			for (int g = 0; g < groups.length; g++) {
				int middle = first + (groups[g][0] + groups[g][1]) * SPACING / 2;
				shoot(context, singleplayer, middle, y + 6, row + 18, 180, -10, "jugcraft_wood_trees_" + (g + 1));
			}
			// The wood samples close up, five walls (or four, or four) to a shot.
			int[][] samples = {{0, 4}, {5, 8}, {9, 12}};
			for (int s = 0; s < samples.length; s++) {
				int middle = first + (samples[s][0] + samples[s][1]) * 4 / 2 + 1;
				shoot(context, singleplayer, middle, y + 2, wall + 8, 180, 8, "jugcraft_wood_samples_" + (s + 1));
			}
			// The seasonal trees in autumn.
			shoot(context, singleplayer, autumnFirst + 12, y + 6, autumnRow + 18, 180, -10, "jugcraft_wood_autumn");
			context.getInput().pressKey(options -> options.keyToggleGui);
		}
	}

	private static void set(TestServerContext server, int x, int y, int z, String block) {
		server.runCommand("setblock %d %d %d jugcraft:%s".formatted(x, y, z, block));
	}

	/** Logs and leaves of `tree` within its reach of the trunk. */
	private static int[] count(ServerLevel level, BlockPos trunk, String[] tree) {
		Block log = JugcraftAgriculture.block(tree[0] + "_log");
		Block leaves = tree[2].isEmpty() ? null : JugcraftAgriculture.block(tree[2]);
		int[] counts = new int[2];
		for (BlockPos pos : BlockPos.betweenClosed(trunk.offset(-4, 0, -4), trunk.offset(4, 26, 4))) {
			BlockState state = level.getBlockState(pos);
			counts[0] += state.is(log) ? 1 : 0;
			counts[1] += leaves != null && state.is(leaves) ? 1 : 0;
		}
		return counts;
	}

	private static void shoot(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z, int yaw,
			int pitch, String name) {
		TestServerContext server = singleplayer.getServer();
		server.runCommand("setblock %d %d %d minecraft:barrier".formatted(x, y - 1, z));
		server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f %d %d", x + 0.5, y, z + 0.5, yaw, pitch));
		context.waitTicks(40);
		singleplayer.getConnection().waitForChunksRender();
		context.takeScreenshot(name);
	}
}
