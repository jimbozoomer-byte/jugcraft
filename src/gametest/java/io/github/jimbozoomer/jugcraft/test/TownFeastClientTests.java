package io.github.jimbozoomer.jugcraft.test;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.agriculture.FeastTableBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FeastTableBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.GiantTurkeyFeastBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HarvestFestival;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.finance.FinanceService;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.finance.Ledger;
import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.TownFeast;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import io.github.jimbozoomer.jugcraft.town.TownState;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
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
 * <li>right-clicks as a survival player through the server's own interaction path, where Fabric's UseBlockCallback and so
 * TownProtection.useBlock run (see {@link #usePath}): both hands empty eats exactly one serving or portion; another's
 * claim over the feast, food in the main hand or the offhand, sneaking (taking a dish, packing the turkey), an owner's
 * feast table elsewhere in town, and serving onto the town's emptied dish are refused, changing nothing. THESE USE-PATH ASSERTIONS NEED CODEX'S TOWNPROTECTION WIRING (the feast hook before
 * publicBlock, ANDed with the claim check): they are meant for the integrated tree and fail on the feast branch alone;</li>
 * <li>re-runs the service, reloads its saved ledger and closes and reopens the world, asserting after each that no serving
 * or portion count ever rises.</li>
 * </ul>
 * Not run where this was written (no Minecraft build there).
 */
public class TownFeastClientTests implements FabricClientGameTest {
	private static final LocalDate OPEN = LocalDate.of(2026, 11, 10);
	private static final int SEASON = 2026;
	/** E2's first dish, eaten out one serving at a time through the use path. */
	private static final int SERVINGS_EATEN_BY_USE = TownFeast.SERVINGS;

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

			// Real right-clicks by a survival player, standing south of E2 and the turkey. Integrated tree only (see usePath).
			server.runCommand("gamemode survival @p");
			server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f 180 30", o.getX() + 105.5, o.getY() + 1, o.getZ() + 92.5));
			context.waitTicks(20);
			int[] beforeUse = eaten;
			eaten = server.computeOnServer(minecraft -> usePath(minecraft, o, beforeUse));
			check(eaten[0] == beforeUse[0] - SERVINGS_EATEN_BY_USE && eaten[1] == beforeUse[1] - 1,
					"The use path ate exactly four servings and one portion: " + eaten[0] + ", " + eaten[1]);
			server.runCommand("gamemode creative @p");
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
			emptyHands(player);
			check(TownFeast.mayPublicEat(TownFeast.ledger(level), origin, level, at, player), "The eat-only hook would allow eating at " + at + hands(player));
			level.getBlockState(at).useWithoutItem(level, player,
					new BlockHitResult(new Vec3(at.getX() + 0.25, at.getY() + 0.8, at.getZ() + 0.5), Direction.UP, at, false));
		}
		emptyHands(player);
		return counts(level, origin);
	}

	/**
	 * Right-clicks by survival players who own nothing here (the world's host, and for the claim check a visitor with no
	 * permissions) through the server's own interaction path:
	 * {@code ServerPlayerGameMode.useItemOn}, which the use-item-on packet handler calls and where Fabric's
	 * UseBlockCallback, and so TownProtection.useBlock, runs first. Returns the counts after.
	 * <p>
	 * THESE ASSERTIONS NEED THE INTEGRATED TREE. They pass only with Codex's wiring: TownProtection.useBlock asking
	 * TownFeast.managed and mayPublicEat, ANDed with LandRegistry.mayBuild, before its publicBlock fallback. On the feast
	 * branch alone nothing calls those hooks, the town refuses the empty-hand meal at a table, and the first eat check
	 * fails by design.
	 * <p>
	 * The town origin is set for this one server task only and forgotten before it ends, so no server tick runs while it
	 * is set: the real town's builder, decor, townsfolk and feast ticks never act on this test world.
	 */
	private static int[] usePath(MinecraftServer minecraft, BlockPos origin, int[] before) {
		ServerLevel level = minecraft.overworld();
		ServerPlayer player = minecraft.getPlayerList().getPlayers().getFirst();
		TownState town = TownState.get(level);
		check(town.origin() == null, "The flat test world has no town of its own: " + town.origin());
		BlockPos table = origin.offset(new BlockPos(106, 1, 89)); // E2's first length; dish 0 holds turkey slices
		BlockPos turkey = origin.offset(new BlockPos(103, 1, 90)); // the turkey's master
		BlockPos owners = origin.offset(new BlockPos(100, 1, 94)); // an owner's table elsewhere on the square
		Item slice = JugcraftAgriculture.item("turkey_slice");
		Item packed = HarvestFestival.GIANT_TURKEY.asItem();
		town.place(origin);
		try {
			check(TownProtection.enabled() && Town.isProtected(level, table) && Town.isProtected(level, turkey) && Town.isProtected(level, owners),
					"Town protection is on and covers the feast and the owner's table");
			check(player.gameMode() == GameType.SURVIVAL && !TownProtection.exempt(player)
					&& TownProtection.denies(player, level, table), "A survival player, not exempt, whom the town refuses by default");
			level.setBlock(owners, JugcraftAgriculture.block("feast_table").defaultBlockState()
					.setValue(FeastTableBlock.RUNNER, FeastTableBlock.Runner.LINEN), Block.UPDATE_ALL);
			check(level.getBlockEntity(owners) instanceof FeastTableBlockEntity own && own.serve(0, new ItemStack(Items.BREAD, 4), true) == 4,
					"The owner's table holds their bread");
			emptyHands(player);
			check(TownFeast.managed(level, table) && TownFeast.managed(level, turkey) && !TownFeast.managed(level, owners)
					&& TownFeast.mayPublicEat(level, table, player) && TownFeast.mayPublicEat(level, turkey, player)
					&& !TownFeast.mayPublicEat(level, owners, player),
					"The service allows this empty-handed player at the feast, not at the owner's table" + hands(player));
			int[] start = counts(level, origin);
			check(start[0] == before[0] && start[1] == before[1] && dish(level, table) == TownFeast.SERVINGS,
					"The use path starts from the eaten counts, E2's first dish full: " + start[0] + ", " + start[1] + ", " + dish(level, table));

			// Both hands empty: exactly one serving at the table, exactly one portion at the turkey.
			InteractionResult ate = use(player, level, InteractionHand.MAIN_HAND, table);
			int[] now = counts(level, origin);
			check(dish(level, table) == TownFeast.SERVINGS - 1 && now[0] == start[0] - 1 && now[1] == start[1],
					"Both hands empty, a right-click eats exactly one serving (" + ate + "): dish " + dish(level, table) + ", counts " + now[0]
					+ ", " + now[1] + "; needs the integrated TownProtection feast hook" + hands(player));
			emptyHands(player);
			ate = use(player, level, InteractionHand.MAIN_HAND, turkey);
			now = counts(level, origin);
			check(now[1] == start[1] - 1 && now[0] == start[0] - 1,
					"Both hands empty, a right-click eats exactly one portion (" + ate + "): counts " + now[0] + ", " + now[1] + hands(player));
			emptyHands(player);

			// The claim check stays ANDed: on land someone else has claimed, the town's say-so is not enough. Claims never
			// bind an admin (LandRegistry.mayBuild), and the host of this world is an operator, so a plain survival visitor
			// with no permissions (a Fabric FakePlayer) makes these clicks.
			ServerPlayer visitor = FakePlayer.get(level, new GameProfile(UUID.randomUUID(), "FeastVisitor"));
			emptyHands(visitor);
			check(!FinanceService.isAdmin(visitor) && !TownProtection.exempt(visitor) && TownProtection.denies(visitor, level, table)
					&& LandRegistry.mayBuild(visitor, level, table) && TownFeast.mayPublicEat(level, table, visitor),
					"A visitor with no permissions, on unclaimed town land, whom the town alone would let eat" + hands(visitor));
			LandRegistry land = LandRegistry.get(minecraft);
			var book = Ledger.get(minecraft).book();
			Set<Long> claimed = new HashSet<>(List.of(LandRegistry.keyOf(table), LandRegistry.keyOf(turkey)));
			try {
				for (long key : claimed) {
					check(land.claim(key) == null && land.assign(book, key, book.account(UUID.randomUUID(), "FeastNeighbour"), 0).ok(),
							"The claim fixture over the feast");
				}
				check(TownFeast.mayPublicEat(level, table, visitor) && TownFeast.mayPublicEat(level, turkey, visitor)
						&& !LandRegistry.mayBuild(visitor, level, table) && !LandRegistry.mayBuild(visitor, level, turkey),
						"The town alone would allow the meal; the claim does not" + hands(visitor));
				InteractionResult claimedTable = use(visitor, level, InteractionHand.MAIN_HAND, table);
				InteractionResult claimedTurkey = use(visitor, level, InteractionHand.MAIN_HAND, turkey);
				check(!claimedTable.consumesAction() && !claimedTurkey.consumesAction() && unchanged(now, counts(level, origin))
						&& dish(level, table) == TownFeast.SERVINGS - 1,
						"On another's claim the feast is refused (" + claimedTable + ", " + claimedTurkey + "), changing nothing" + hands(visitor));
			} finally {
				for (long key : claimed) {
					land.abandon(player, book, key, true, 0);
				}
			}
			emptyHands(player);

			// Food in the main hand (serving onto the table): refused, nothing changes.
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 3));
			InteractionResult atTable = use(player, level, InteractionHand.MAIN_HAND, table);
			InteractionResult atTurkey = use(player, level, InteractionHand.MAIN_HAND, turkey);
			check(!atTable.consumesAction() && !atTurkey.consumesAction() && unchanged(now, counts(level, origin))
					&& dish(level, table) == TownFeast.SERVINGS - 1 && player.getMainHandItem().is(Items.BREAD) && player.getMainHandItem().getCount() == 3,
					"Food in the main hand is refused at the table (" + atTable + ") and the turkey (" + atTurkey + "), changing nothing" + hands(player));

			// Food in the offhand: refused, whichever hand the click is for.
			emptyHands(player);
			player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.BREAD, 3));
			List<InteractionResult> offhand = List.of(use(player, level, InteractionHand.MAIN_HAND, table), use(player, level, InteractionHand.OFF_HAND, table),
					use(player, level, InteractionHand.MAIN_HAND, turkey), use(player, level, InteractionHand.OFF_HAND, turkey));
			check(offhand.stream().noneMatch(InteractionResult::consumesAction) && unchanged(now, counts(level, origin))
					&& dish(level, table) == TownFeast.SERVINGS - 1 && player.getOffhandItem().is(Items.BREAD) && player.getOffhandItem().getCount() == 3,
					"Food in the offhand is refused, changing nothing: " + offhand + hands(player));

			// Sneaking: refused (it would take the dish away, or pack the turkey up).
			emptyHands(player);
			int slices = player.getInventory().countItem(slice);
			int turkeys = player.getInventory().countItem(packed);
			player.setShiftKeyDown(true);
			atTable = use(player, level, InteractionHand.MAIN_HAND, table);
			atTurkey = use(player, level, InteractionHand.MAIN_HAND, turkey);
			String sneaking = hands(player);
			player.setShiftKeyDown(false);
			check(!atTable.consumesAction() && !atTurkey.consumesAction() && unchanged(now, counts(level, origin))
					&& dish(level, table) == TownFeast.SERVINGS - 1 && player.getInventory().countItem(slice) == slices
					&& player.getInventory().countItem(packed) == turkeys,
					"Sneaking is refused at the table (" + atTable + ") and the turkey (" + atTurkey + "): no dish taken, no turkey packed" + sneaking);

			// An owner's feast table elsewhere in town: refused, both hands empty.
			emptyHands(player);
			InteractionResult atOwners = use(player, level, InteractionHand.MAIN_HAND, owners);
			check(!atOwners.consumesAction() && level.getBlockEntity(owners) instanceof FeastTableBlockEntity own
					&& own.dish(0).is(Items.BREAD) && own.dish(0).getCount() == 4,
					"An owner's feast table elsewhere in town is refused (" + atOwners + ") and keeps its bread" + hands(player));

			// The town's table can't be served onto: eat the dish out, one serving a click, then offer it food.
			for (int left = dish(level, table); left > 0; left--) {
				emptyHands(player);
				ate = use(player, level, InteractionHand.MAIN_HAND, table);
				check(dish(level, table) == left - 1, "Each click eats exactly one serving (" + ate + "): " + left + " -> " + dish(level, table) + hands(player));
			}
			emptyHands(player);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 2));
			InteractionResult served = use(player, level, InteractionHand.MAIN_HAND, table);
			check(!served.consumesAction() && dish(level, table) == 0 && player.getMainHandItem().is(Items.BREAD) && player.getMainHandItem().getCount() == 2,
					"Nothing can be served onto the town's emptied dish (" + served + ")" + hands(player));
			emptyHands(player);
			return counts(level, origin);
		} finally {
			town.forget();
		}
	}

	/** One right-click on the west half (dish 0) of a cell, hungry, through ServerPlayerGameMode.useItemOn. */
	private static InteractionResult use(ServerPlayer player, ServerLevel level, InteractionHand hand, BlockPos at) {
		player.getFoodData().setFoodLevel(2);
		return player.gameMode.useItemOn(player, level, player.getItemInHand(hand), hand,
				new BlockHitResult(new Vec3(at.getX() + 0.25, at.getY() + 0.8, at.getZ() + 0.5), Direction.UP, at, false));
	}

	/** The servings in dish 0 of the table at {@code at}, or -1 if there is no table. */
	private static int dish(ServerLevel level, BlockPos at) {
		return level.getBlockEntity(at) instanceof FeastTableBlockEntity table ? table.dish(0).getCount() : -1;
	}

	private static boolean unchanged(int[] before, int[] after) {
		return before[0] == after[0] && before[1] == after[1];
	}

	private static void emptyHands(ServerPlayer player) {
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
	}

	/** What {@code player} holds and whether they sneak, for every eat-rule diagnostic. */
	private static String hands(ServerPlayer player) {
		return " [main hand: " + player.getMainHandItem() + ", offhand: " + player.getOffhandItem() + ", sneaking: "
				+ player.isSecondaryUseActive() + "]";
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
