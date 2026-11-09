package io.github.jimbozoomer.jugcraft.test;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.agriculture.FeastTableBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.GiantTurkeyFeastBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HarvestFestival;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.TownFeast;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Rendered check of the town square feast (TownFeast; docs/features/town-feast.md), with the standalone service rather
 * than the town's hooks. On a flat test floor laid as the north plaza (town origin chosen so the player stands on the
 * main road at town (96, 1, 95)), with stand-ins at their true town positions for the Church of Notch's front wall
 * (z 84, its door at x 96-97), the church door path (x 95-98, z 85-92, in quartz) and the north-west and north-east
 * market stalls (x 73-79 and 113-119, z 88-92), it sets out W1, E2 and the turkey at their real positions, then:
 * <ul>
 * <li>checks the door path, the doorway and the stalls' front and side lanes are clear, and photographs them from eye
 * height: three screenshots;</li>
 * <li>eats three servings and five turkey portions as the town's eat-only hook lets a player (both hands empty);</li>
 * <li>re-runs the service, reloads its saved ledger and closes and reopens the world, asserting after each that no serving
 * or portion count ever rises.</li>
 * </ul>
 * Not run where this was written (no Minecraft build there).
 */
public class TownFeastClientTests implements FabricClientGameTest {
	private static final LocalDate OPEN = LocalDate.of(2026, 11, 10);
	private static final int SEASON = 2026;

	@Override
	public void runTest(ClientGameTestContext context) {
		TestWorldSave save;
		BlockPos origin;
		int[] eaten;
		try (TestSingleplayerContext world = context.worldBuilder().adjustSettings(creator -> {
			creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
			creator.setWorldType(new WorldCreationUiState.WorldTypeEntry(creator.getSettings().worldgenLoadContext()
					.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));
			creator.setGenerateStructures(false);
		}).create()) {
			world.getConnection().waitForChunksRender();
			TestServerContext server = world.getServer();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("gamerule minecraft:advance_time false");
			server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			BlockPos feet = context.computeOnClient(client -> client.player.blockPosition());
			origin = feet.offset(-96, -1, -95);
			BlockPos o = origin;
			List<String> lines = server.computeOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				plaza(level, o);
				return TownFeast.run(level, o, TownFeast.THEME, OPEN, blueprint(), TownFeast.ledger(level), false);
			});
			for (TownFeast.Site site : TownFeast.LAYOUT) {
				String status = server.computeOnServer(minecraft -> {
					TownFeast.Ledger.Entry entry = TownFeast.ledger(minecraft.overworld()).get(SEASON, site.id());
					return entry == null ? "none" : entry.status();
				});
				check(status.equals(TownFeast.PLACED), site.id() + " is set out at its real position: " + lines);
			}
			int[] full = server.computeOnServer(minecraft -> counts(minecraft.overworld(), o));
			check(full[0] == 80 && full[1] == GiantTurkeyFeastBlock.PORTIONS, "80 servings and 24 portions: " + full[0] + ", " + full[1]);
			String blocked = server.computeOnServer(minecraft -> blockedLanes(minecraft.overworld(), o));
			check(blocked.isEmpty(), "The church door path, its doorway and the stalls' lanes stay clear: " + blocked);

			context.getInput().pressKey(InputConstants.KEY_F1);
			// From the main road at eye height, looking north up the church door path between W1 and E2 and the turkey.
			shoot(context, world, o, 96.5, 96.5, 180, 6, "town_feast_church_door_path");
			// In front of the north-west stall, looking north up its side lane: the stall on the left, W1 on the right.
			shoot(context, world, o, 82.5, 97.5, 180, 6, "town_feast_nw_stall_access");
			// In front of the north-east stall, looking north up its side lane: E2 and the turkey on the left, the stall on the right.
			shoot(context, world, o, 111.5, 97.5, 180, 6, "town_feast_ne_stall_access");

			eaten = server.computeOnServer(minecraft -> eat(minecraft, o));
			check(eaten[0] == 77 && eaten[1] == GiantTurkeyFeastBlock.PORTIONS - 5, "Three servings and five portions eaten: " + eaten[0] + ", " + eaten[1]);
			int[] last = eaten;
			for (int round = 0; round < 3; round++) {
				int day = round;
				// The service runs again, as the town's tick would every hundred ticks.
				int[] rerun = server.computeOnServer(minecraft -> {
					ServerLevel level = minecraft.overworld();
					TownFeast.run(level, o, TownFeast.THEME, OPEN.plusDays(day), blueprint(), TownFeast.ledger(level), false);
					return counts(level, o);
				});
				last = never(last, rerun, "re-run " + round);
				// Its saved ledger, written and read back, runs again.
				int[] reloaded = server.computeOnServer(minecraft -> {
					ServerLevel level = minecraft.overworld();
					var saved = TownFeast.Ledger.CODEC.encodeStart(NbtOps.INSTANCE, TownFeast.ledger(level)).getOrThrow();
					TownFeast.Ledger copy = TownFeast.Ledger.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
					TownFeast.run(level, o, TownFeast.THEME, OPEN.plusDays(day), blueprint(), copy, false);
					return counts(level, o);
				});
				last = never(last, reloaded, "ledger reload " + round);
				context.waitTicks(5);
			}
			eaten = last;
			save = world.getWorldSave();
		}
		// Closed and reopened: the world's own saved ledger and tables come back, and a run sets nothing out again.
		BlockPos o = origin;
		try (TestSingleplayerContext world = save.open()) {
			world.getConnection().waitForChunksRender();
			TestServerContext server = world.getServer();
			int[] reopened = server.computeOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				TownFeast.run(level, o, TownFeast.THEME, OPEN, blueprint(), TownFeast.ledger(level), false);
				return counts(level, o);
			});
			never(eaten, reopened, "world reopened");
			String status = server.computeOnServer(minecraft -> {
				TownFeast.Ledger.Entry entry = TownFeast.ledger(minecraft.overworld()).get(SEASON, "W1");
				return entry == null ? "none" : entry.status();
			});
			check(status.equals(TownFeast.PLACED), "The saved ledger still has W1 set out after reopening: " + status);
		}
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	/** Asserts no count rose (they must stay exactly as eaten) and returns the new counts. */
	private static int[] never(int[] before, int[] after, String step) {
		check(after[0] == before[0] && after[1] == before[1],
				"Nothing is refilled after " + step + ": servings " + before[0] + " -> " + after[0] + ", portions " + before[1] + " -> " + after[1]);
		return after;
	}

	/** The stand-ins' blueprint: the quartz door path, stone elsewhere on the floor, open air above (as the plaza is built). */
	private static TownFeast.Baseline blueprint() {
		return new TownFeast.Baseline() {
			@Override
			public BlockState expected(BlockPos relative) {
				int x = relative.getX(), y = relative.getY(), z = relative.getZ();
				if (y == 0) {
					return x >= 95 && x <= 98 && z >= 85 && z <= 92 ? Blocks.SMOOTH_QUARTZ.defaultBlockState() : Blocks.STONE.defaultBlockState();
				}
				return y > 0 && y < 7 && z > 84 ? Blocks.AIR.defaultBlockState() : null;
			}

			@Override
			public boolean preserved(BlockPos world) {
				return false;
			}
		};
	}

	/** Lays the plaza floor and the stand-ins at their town positions. */
	private static void plaza(ServerLevel level, BlockPos origin) {
		for (int x = 70; x <= 122; x++) {
			for (int z = 82; z <= 100; z++) {
				boolean path = x >= 95 && x <= 98 && z >= 85 && z <= 92;
				set(level, origin, x, 0, z, path ? Blocks.SMOOTH_QUARTZ : Blocks.STONE);
				for (int y = 1; y <= 6; y++) {
					set(level, origin, x, y, z, Blocks.AIR);
				}
			}
		}
		// The church's front wall, its doorway open.
		for (int x = 84; x <= 108; x++) {
			for (int y = 1; y <= 5; y++) {
				boolean door = (x == 96 || x == 97) && y <= 2;
				set(level, origin, x, y, 84, door ? Blocks.AIR : Blocks.STONE_BRICKS);
			}
		}
		// The two north stalls: a counter, corner posts and an awning over their footprint.
		for (int x0 : new int[] {73, 113}) {
			for (int x = x0; x <= x0 + 6; x++) {
				for (int z = 88; z <= 92; z++) {
					set(level, origin, x, 1, z, Blocks.SPRUCE_PLANKS);
					set(level, origin, x, 3, z, TownData.parse("minecraft:orange_wool").getBlock());
					if ((x == x0 || x == x0 + 6) && (z == 88 || z == 92)) {
						set(level, origin, x, 2, z, Blocks.OAK_FENCE);
					}
				}
			}
		}
	}

	private static void set(ServerLevel level, BlockPos origin, int x, int y, int z, Block block) {
		level.setBlock(origin.offset(x, y, z), block.defaultBlockState(), Block.UPDATE_CLIENTS);
	}

	/** Every cell of the door path, the doorway and the stalls' front and side lanes, from the floor up two blocks, that is not air. */
	private static String blockedLanes(ServerLevel level, BlockPos origin) {
		StringBuilder blocked = new StringBuilder();
		int[][] lanes = {
				{95, 98, 85, 92}, // the church door path
				{96, 97, 84, 84}, // its doorway
				{73, 79, 93, 94}, // the north-west stall's front
				{80, 84, 88, 92}, // its side lane, east of it
				{113, 119, 93, 94}, // the north-east stall's front
				{110, 112, 88, 92}}; // its side lane, west of it
		for (int[] lane : lanes) {
			for (int x = lane[0]; x <= lane[1]; x++) {
				for (int z = lane[2]; z <= lane[3]; z++) {
					for (int y = 1; y <= 2; y++) {
						if (!level.getBlockState(origin.offset(x, y, z)).isAir()) {
							blocked.append(" (").append(x).append(", ").append(y).append(", ").append(z).append(")");
						}
					}
				}
			}
		}
		return blocked.toString();
	}

	/** Every serving on the two tables, and the turkey's portions (-1 when it is gone). */
	private static int[] counts(ServerLevel level, BlockPos origin) {
		int servings = 0;
		for (TownFeast.Site site : TownFeast.LAYOUT) {
			if (site.kind() != TownFeast.Kind.TABLE) {
				continue;
			}
			for (BlockPos cell : site.cells()) {
				if (level.getBlockEntity(origin.offset(cell)) instanceof FeastTableBlockEntity table) {
					for (int dish = 0; dish < FeastTableBlockEntity.DISHES; dish++) {
						servings += table.dish(dish).getCount();
					}
				}
			}
		}
		BlockState turkey = level.getBlockState(origin.offset(new BlockPos(103, 1, 90)));
		return new int[] {servings, turkey.is(HarvestFestival.GIANT_TURKEY) ? turkey.getValue(GiantTurkeyFeastBlock.REMAINING) : -1};
	}

	/** The player eats three servings at W1's first length and five turkey portions, as the eat-only hook allows. */
	private static int[] eat(MinecraftServer minecraft, BlockPos origin) {
		ServerLevel level = minecraft.overworld();
		ServerPlayer player = minecraft.getPlayerList().getPlayers().getFirst();
		BlockPos table = origin.offset(new BlockPos(85, 1, 89));
		BlockPos turkey = origin.offset(new BlockPos(103, 1, 90));
		for (int i = 0; i < 8; i++) {
			BlockPos at = i < 3 ? table : turkey;
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
			check(TownFeast.mayPublicEat(TownFeast.ledger(level), origin, level, at, player), "The eat-only hook would allow eating at " + at);
			level.getBlockState(at).useWithoutItem(level, player,
					new BlockHitResult(new Vec3(at.getX() + 0.25, at.getY() + 0.8, at.getZ() + 0.5), Direction.UP, at, false));
		}
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		return counts(level, origin);
	}

	/** Stands the player at a town position on the floor, at eye height, and takes a screenshot. */
	private static void shoot(ClientGameTestContext context, TestSingleplayerContext world, BlockPos origin, double x, double z,
			int yaw, int pitch, String name) {
		world.getServer().runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f %d %d",
				origin.getX() + x, origin.getY() + 1, origin.getZ() + z, yaw, pitch));
		context.waitTicks(30);
		world.getConnection().waitForChunksRender();
		context.takeScreenshot(name);
	}
}
