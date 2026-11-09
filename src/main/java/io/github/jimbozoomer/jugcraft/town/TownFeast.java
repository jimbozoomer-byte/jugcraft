package io.github.jimbozoomer.jugcraft.town;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.FeastTableBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FeastTableBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.GiantTurkeyFeastBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HarvestFestival;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

/**
 * The town square's Thanksgiving feast (docs/features/town-feast.md): two linen Feast Tables laid with the harvest dishes
 * and a Giant Turkey Feast in the north plaza, set out once a season while the town's theme is {@value #THEME} between
 * 4 and 30 November, and taken down afterwards. Everything is decided against a saved {@link Ledger}:
 * <ul>
 * <li>each site is placed whole or not at all, and only where the blueprint and the owner's marks say the ground is
 * still the town's own and open ({@link #preflight});</li>
 * <li>a site placed (or taken down) in a season is never placed again that season, so a serving or a portion eaten
 * stays eaten through reloads, theme changes, town upgrades and re-runs;</li>
 * <li>it is taken down only while it is still exactly what was set out, less what was eaten ({@link #removable}): food
 * or cloth a player added, or anything changed, is left alone and recorded as preserved; taking it down spills nothing.</li>
 * </ul>
 * The core ({@link #run}) takes the town's origin, theme, date, {@link Baseline} and ledger as parameters so it can be
 * tested without a built town; {@link #tick}, {@link #report}, {@link #managed} and {@link #mayPublicEat} read the live
 * town and are what the town's tick, command and protection call. Nothing here registers itself. Eating at a managed
 * cell needs {@link #mayPublicEat} AND the land claim check: a managed cell never bypasses a claim.
 */
public final class TownFeast {
	/** The logical theme the feast belongs to (a later centrepiece layout does not change it). */
	public static final String THEME = "harvest";
	/** The season window, inclusive, in the calendar year that keys the season. */
	public static final int FIRST_DAY = 4, LAST_DAY = 30;
	/** Servings laid on each dish (a Feast Table dish holds up to {@link FeastTableBlockEntity#SERVINGS}). */
	public static final int SERVINGS = 4;
	/** How far round a site the ground must still be the blueprint's, in blocks. */
	public static final int MARGIN = 2;
	/** How often (server ticks) the live wrapper looks. */
	public static final int CHECK_TICKS = 100;
	/**
	 * The private mark on every serving the town lays ({@link #stamped}): its season and site. A player's own food of the
	 * same kind has no mark, so it never merges with the town's on a dish and is never mistaken for it.
	 */
	public static final String MARK = "jugcraft_town_feast";
	/** A preflight problem worth only waiting for (a chunk not loaded, someone standing in the way). */
	private static final String WAIT = "waiting: ";

	public static final String PLACED = "placed", REMOVED = "removed", PRESERVED = "preserved", SKIPPED = "skipped";

	/** What a site is. */
	public enum Kind {
		TABLE, TURKEY
	}

	/**
	 * A site, in town coordinates: a linen table of {@code lengths} lengths running east from {@code start} along x, its
	 * dishes two to a length (west half first), or a Giant Turkey Feast with its master at {@code start} facing
	 * {@code facing}.
	 */
	public record Site(String id, Kind kind, BlockPos start, int lengths, List<String> dishes, Direction facing) {
		static Site table(String id, BlockPos start, int lengths, List<String> dishes) {
			if (dishes.size() != lengths * FeastTableBlockEntity.DISHES) {
				throw new IllegalArgumentException(id + ": two dishes to a length");
			}
			return new Site(id, Kind.TABLE, start, lengths, List.copyOf(dishes), Direction.NORTH);
		}

		static Site turkey(String id, BlockPos master, Direction facing) {
			return new Site(id, Kind.TURKEY, master, 0, List.of(), facing);
		}

		/** Its cells, in town coordinates; for a turkey in part order (the master first). */
		public List<BlockPos> cells() {
			List<BlockPos> cells = new ArrayList<>();
			if (kind == Kind.TABLE) {
				for (int i = 0; i < lengths; i++) {
					cells.add(start.east(i));
				}
			} else {
				GiantTurkeyFeastBlock turkey = HarvestFestival.GIANT_TURKEY;
				for (int part = 0; part < turkey.cells().length; part++) {
					cells.add(turkey.partPos(start, facing, part));
				}
			}
			return cells;
		}
	}

	/**
	 * Layout A, approved 10 November 2026: the north plaza between the Church of Notch (front wall z 84) and the main
	 * road (z 93-97). Measured clearances are in the feature record; scripts/tests/test_town_feast_layout.py checks them
	 * against the blueprint. Twenty dishes of {@value #SERVINGS} servings (80) and the turkey's 24 portions.
	 */
	public static final List<Site> LAYOUT = List.of(
			Site.table("W1", new BlockPos(85, 1, 89), 6, List.of(
					"jugcraft:mashed_potatoes_and_gravy", "jugcraft:harvest_pea_casserole",
					"jugcraft:candied_sweet_potatoes", "jugcraft:dinner_roll",
					"jugcraft:cranberry_sauce", "jugcraft:cornbread_stuffing",
					"jugcraft:mashed_potatoes_and_gravy", "jugcraft:harvest_pea_casserole",
					"jugcraft:candied_sweet_potatoes", "jugcraft:dinner_roll",
					"jugcraft:cranberry_sauce", "jugcraft:cornbread_stuffing")),
			Site.table("E2", new BlockPos(106, 1, 89), 4, List.of(
					"jugcraft:turkey_slice", "jugcraft:dinner_roll",
					"jugcraft:cranberry_sauce", "jugcraft:cornbread_stuffing",
					"jugcraft:candied_sweet_potatoes", "jugcraft:turkey_slice",
					"minecraft:pumpkin_pie", "minecraft:pumpkin_pie")),
			Site.turkey("T", new BlockPos(103, 1, 90), Direction.SOUTH));

	/** What the town looked like as built, and the owner's marks. */
	public interface Baseline {
		/** The blueprint's state at a town position, or null where the blueprint does not own it (a decor site, KEEP). */
		@Nullable BlockState expected(BlockPos relative);

		/** Whether an owner override is recorded at this world position (TownDecor's preserved decor). */
		boolean preserved(BlockPos world);
	}

	private TownFeast() {
	}

	// ------------------------------------------------------------------------------------------------- live wrappers

	/** The tick entry: every {@value #CHECK_TICKS} ticks, sets out or takes down the feast for today. */
	public static void tick(ServerLevel level) {
		if (level.getServer().getTickCount() % CHECK_TICKS != 0 || level != level.getServer().overworld()) {
			return;
		}
		TownState state = TownState.get(level);
		BlockPos origin = state.origin();
		if (origin != null) {
			run(level, origin, state.theme(), today(), blueprint(level, origin), ledger(level), false);
		}
	}

	/** Lines for an operator: the saved state, and with {@code dryRun} what a run now would do (changing nothing). */
	public static List<String> report(ServerLevel level, boolean dryRun) {
		BlockPos origin = TownState.get(level).origin();
		if (origin == null) {
			return List.of("Town feast: this world has no town.");
		}
		List<String> lines = new ArrayList<>();
		String theme = TownState.get(level).theme();
		LocalDate date = today();
		lines.add("Town feast: theme " + theme + ", " + date + (inSeason(theme, date) ? " (in season)" : " (out of season)"));
		lines.addAll(ledger(level).lines());
		if (dryRun) {
			lines.add("Dry run (nothing changed):");
			lines.addAll(run(level, origin, theme, date, blueprint(level, origin), ledger(level), true));
		}
		return lines;
	}

	/** Whether {@code pos} is a cell of a feast site this town has set out and not taken down. */
	public static boolean managed(ServerLevel level, BlockPos pos) {
		BlockPos origin = TownState.get(level).origin();
		return origin != null && managed(ledger(level), origin, pos);
	}

	/**
	 * Whether a player the town's protection would otherwise refuse may eat at this managed feast cell now: both hands
	 * empty, not sneaking (sneaking would take a dish away or pack the turkey), and only the town's own food (a length
	 * holding food someone else added or refilled stays private).
	 * <p>
	 * This is one condition, never a way round the others: the caller (TownProtection.useBlock, before publicBlock) must
	 * still require the land claim check too, {@code mayPublicEat(...) && LandRegistry.mayBuild(player, level, pos)}, so a
	 * managed cell never bypasses a claim. When both hold it runs only the block's own empty-hand use
	 * ({@code useWithoutItem}), never an item's use; otherwise it refuses, with no fallback.
	 */
	public static boolean mayPublicEat(ServerLevel level, BlockPos pos, Player player) {
		BlockPos origin = TownState.get(level).origin();
		return origin != null && mayPublicEat(ledger(level), origin, level, pos, player);
	}

	/** The town's saved feast ledger. */
	public static Ledger ledger(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(Ledger.TYPE);
	}

	/** Today on the server's season calendar (the preview date when an operator set one). */
	public static LocalDate today() {
		return JugcraftSeasons.settings().effectiveDate(JugcraftSeasons.date());
	}

	/**
	 * The blueprint the town was built from at each position: the revision that position's chunk was last built or
	 * upgraded to (TownUpgrades.version and historical), as the town's upgrades read it. Decor sites change with the
	 * theme, so the blueprint does not own them. Owner overrides come from TownUpgrades.progress's preserved decor.
	 */
	public static Baseline blueprint(ServerLevel level, BlockPos origin) {
		Set<Long> preserved = TownUpgrades.progress(level).preservedDecor;
		Map<Integer, TownData> revisions = new HashMap<>();
		Map<TownData, Set<BlockPos>> decor = new HashMap<>();
		return new Baseline() {
			@Override
			public @Nullable BlockState expected(BlockPos relative) {
				int version = TownUpgrades.version(level, ChunkPos.containing(origin.offset(relative)).pack());
				TownData data = revisions.computeIfAbsent(version,
						v -> v >= TownUpgrades.CURRENT ? TownData.get() : TownUpgrades.historical(v));
				if (decor.computeIfAbsent(data, TownFeast::decorCells).contains(relative)) {
					return null;
				}
				int index = data.index(relative.getX(), relative.getY(), relative.getZ());
				return index == TownData.KEEP ? null : data.state(index);
			}

			@Override
			public boolean preserved(BlockPos world) {
				return preserved.contains(world.asLong());
			}
		};
	}

	private static Set<BlockPos> decorCells(TownData data) {
		Set<BlockPos> cells = new HashSet<>();
		for (TownData.Site site : data.sites) {
			for (int[] b : site.blocks()) {
				cells.add(new BlockPos(b[0], b[1], b[2]));
			}
		}
		return cells;
	}

	// ---------------------------------------------------------------------------------------------------------- core

	/** Whether the feast is set out on {@code date} under {@code theme}: the harvest theme, 4 to 30 November. */
	public static boolean inSeason(String theme, LocalDate date) {
		return THEME.equals(theme) && date.getMonth() == Month.NOVEMBER && date.getDayOfMonth() >= FIRST_DAY
				&& date.getDayOfMonth() <= LAST_DAY;
	}

	/**
	 * One pass: first takes down what an earlier season (or, out of season, any season) left out, where it is still the
	 * town's own; then, in season, sets out each site not yet placed this season that passes its preflight. Returns a
	 * line per site. With {@code dryRun} nothing in the world or the ledger changes.
	 */
	public static List<String> run(ServerLevel level, BlockPos origin, String theme, LocalDate date, Baseline baseline,
			Ledger ledger, boolean dryRun) {
		List<String> lines = new ArrayList<>();
		boolean open = inSeason(theme, date);
		int season = date.getYear();
		if (!ledger.isFor(origin)) {
			if (dryRun) {
				ledger = new Ledger();
			} else {
				ledger.reset(origin);
			}
		}
		// First take down what is due everywhere, so a neighbour's leftovers never stand in a site's margin.
		for (Site site : LAYOUT) {
			for (Ledger.Entry entry : ledger.placed(site.id())) {
				if (open && entry.season() == season) {
					continue;
				}
				String problem = removable(level, origin, site, entry);
				if (problem == null) {
					if (!dryRun) {
						remove(level, origin, site);
						ledger.put(entry.with(REMOVED, ""));
					}
					lines.add(entry.season() + " " + site.id() + ": " + (dryRun ? "would take down" : "taken down"));
				} else if (problem.startsWith(WAIT)) {
					lines.add(entry.season() + " " + site.id() + ": " + problem);
				} else {
					if (!dryRun) {
						ledger.put(entry.with(PRESERVED, problem));
					}
					lines.add(entry.season() + " " + site.id() + ": preserved, " + problem);
				}
			}
		}
		for (Site site : LAYOUT) {
			if (!open) {
				break;
			}
			Ledger.Entry done = ledger.get(season, site.id());
			if (done != null && !done.status().equals(SKIPPED)) {
				lines.add(season + " " + site.id() + ": " + done.status() + " this season; never set out again");
				continue;
			}
			String problem = preflight(level, origin, site, baseline, ledger, season);
			if (problem == null) {
				if (!dryRun) {
					ledger.put(new Ledger.Entry(season, site.id(), PLACED, "", place(level, origin, site, season)));
				}
				lines.add(season + " " + site.id() + ": " + (dryRun ? "would set out" : "set out"));
			} else if (problem.startsWith(WAIT)) {
				lines.add(season + " " + site.id() + ": " + problem);
			} else {
				if (!dryRun) {
					ledger.put(new Ledger.Entry(season, site.id(), SKIPPED, problem, List.of()));
				}
				lines.add(season + " " + site.id() + ": skipped, " + problem);
			}
		}
		return lines;
	}

	/** The dish items, checked to be food a Feast Table takes; null and a problem when one is missing. */
	private static @Nullable List<Item> menu(Site site, List<String> problems) {
		List<Item> items = new ArrayList<>();
		for (String id : site.dishes()) {
			Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
			if (item == Items.AIR || !FeastTableBlockEntity.food(new ItemStack(item))) {
				problems.add("dish " + id + " is not a registered food");
				return null;
			}
			items.add(item);
		}
		return items;
	}

	/** The state a site sets at its {@code index}th cell. */
	private static BlockState wanted(Site site, int index) {
		if (site.kind() == Kind.TABLE) {
			FeastTableBlock.Part part = site.lengths() == 1 ? FeastTableBlock.Part.SINGLE
					: index == 0 ? FeastTableBlock.Part.START : index == site.lengths() - 1 ? FeastTableBlock.Part.END
					: FeastTableBlock.Part.MIDDLE;
			return JugcraftAgriculture.block("feast_table").defaultBlockState().setValue(FeastTableBlock.AXIS, Direction.Axis.X)
					.setValue(FeastTableBlock.RUNNER, FeastTableBlock.Runner.LINEN).setValue(FeastTableBlock.PART, part);
		}
		return HarvestFestival.GIANT_TURKEY.defaultBlockState().setValue(MultiDecorationBlock.FACING, site.facing())
				.setValue(GiantTurkeyFeastBlock.PART, index).setValue(GiantTurkeyFeastBlock.REMAINING, GiantTurkeyFeastBlock.PORTIONS);
	}

	private static boolean loaded(ServerLevel level, BlockPos world) {
		return level.getChunkSource().getChunkNow(world.getX() >> 4, world.getZ() >> 4) != null;
	}

	/**
	 * Null when the whole site may be set out now, else why not ({@value #WAIT}... for what will pass by itself). Never
	 * loads a chunk. Every cell of the site and of the ground {@value #MARGIN} blocks round it, from the floor below to
	 * a block above, must be loaded and still the blueprint's; the site's own cells must be open blueprint air on solid
	 * floor, without a block entity, an owner mark or casino furniture within two blocks, and nothing standing in them.
	 * Where the margin covers a neighbouring feast site, that site's blocks are accepted only when it is positively the
	 * town's own: set out this season by the {@code ledger}, and still exactly what was set out, less what was eaten
	 * ({@link #removable}). Any other feast table or turkey there is a change like any other.
	 */
	public static @Nullable String preflight(ServerLevel level, BlockPos origin, Site site, Baseline baseline, Ledger ledger, int season) {
		List<String> problems = new ArrayList<>();
		if (site.kind() == Kind.TABLE && menu(site, problems) == null) {
			return problems.getFirst();
		}
		List<BlockPos> cells = site.cells();
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for (BlockPos cell : cells) {
			minX = Math.min(minX, cell.getX());
			minY = Math.min(minY, cell.getY());
			minZ = Math.min(minZ, cell.getZ());
			maxX = Math.max(maxX, cell.getX());
			maxY = Math.max(maxY, cell.getY());
			maxZ = Math.max(maxZ, cell.getZ());
		}
		List<BlockPos> area = new ArrayList<>();
		for (int x = minX - MARGIN; x <= maxX + MARGIN; x++) {
			for (int y = minY - 1; y <= maxY + 1; y++) {
				for (int z = minZ - MARGIN; z <= maxZ + MARGIN; z++) {
					area.add(new BlockPos(x, y, z));
				}
			}
		}
		for (BlockPos relative : area) {
			if (!loaded(level, origin.offset(relative))) {
				return WAIT + "chunk at " + relative + " not loaded";
			}
		}
		for (int i = 0; i < cells.size(); i++) {
			BlockPos relative = cells.get(i);
			BlockPos world = origin.offset(relative);
			BlockState expected = baseline.expected(relative);
			if (expected == null || !expected.isAir()) {
				return "cell " + relative + " is not open blueprint ground";
			}
			if (level.getBlockState(world) != expected) {
				return "changed from the blueprint at " + relative + " (" + level.getBlockState(world) + ")";
			}
			if (baseline.preserved(world)) {
				return "owner override recorded at " + relative;
			}
			if (level.getBlockEntity(world) != null) {
				return "block entity at " + relative;
			}
			if (TownUpgrades.fixtureNearby(level, world)) {
				return "casino furniture within two blocks of " + relative;
			}
			BlockPos below = relative.below();
			if (!cells.contains(below) && !level.getBlockState(world.below()).isSolid()) {
				return "no solid floor under " + relative;
			}
		}
		Set<BlockPos> neighbours = new HashSet<>();
		for (Site other : LAYOUT) {
			Ledger.Entry entry = other == site ? null : ledger.get(season, other.id());
			if (entry != null && entry.status().equals(PLACED) && removable(level, origin, other, entry) == null) {
				neighbours.addAll(other.cells());
			}
		}
		for (BlockPos relative : area) {
			BlockPos world = origin.offset(relative);
			BlockState expected = baseline.expected(relative);
			if (baseline.preserved(world)) {
				return "owner override recorded at " + relative;
			}
			// A managed neighbour may already be set out there (E2 and T share their margins); nothing else may.
			BlockState actual = level.getBlockState(world);
			if (neighbours.contains(relative)) {
				continue;
			}
			if (expected != null && actual != expected) {
				return "changed from the blueprint at " + relative + " (" + level.getBlockState(world) + ")";
			}
		}
		for (int i = 0; i < cells.size(); i++) {
			if (!level.isUnobstructed(wanted(site, i), origin.offset(cells.get(i)), CollisionContext.empty())) {
				return WAIT + "something stands at " + cells.get(i);
			}
		}
		return null;
	}

	/** Sets the site out; returns what was laid on each dish ("item*count"), for the removal check. */
	private static List<String> place(ServerLevel level, BlockPos origin, Site site, int season) {
		List<BlockPos> cells = site.cells();
		List<String> laid = new ArrayList<>();
		if (site.kind() == Kind.TURKEY) {
			// The master first, then every other part, each with its part number, as a scene sets a prop.
			for (int part = 0; part < cells.size(); part++) {
				TownBuilder.set(level, origin.offset(cells.get(part)), wanted(site, part));
			}
			return laid;
		}
		List<Item> menu = menu(site, new ArrayList<>());
		for (int i = 0; i < cells.size(); i++) {
			BlockPos world = origin.offset(cells.get(i));
			TownBuilder.set(level, world, wanted(site, i));
			FeastTableBlockEntity table = (FeastTableBlockEntity) level.getBlockEntity(world);
			for (int dish = 0; dish < FeastTableBlockEntity.DISHES; dish++) {
				Item item = menu.get(i * FeastTableBlockEntity.DISHES + dish);
				int served = table == null ? 0 : table.serve(dish, stamped(new ItemStack(item, SERVINGS), season, site.id()), true);
				laid.add(BuiltInRegistries.ITEM.getKey(item) + "*" + served);
			}
		}
		return laid;
	}

	/**
	 * Null when a placed site is still exactly what was set out, less what was eaten, so it may be taken down; else why
	 * not. Cells already cleared count as gone. A table length must still be a linen feast table along x, each dish empty
	 * or holding no more of the same food than was laid; the turkey every part in place, facing as set, with one
	 * portion count.
	 */
	public static @Nullable String removable(ServerLevel level, BlockPos origin, Site site, Ledger.Entry entry) {
		List<BlockPos> cells = site.cells();
		for (BlockPos relative : cells) {
			if (!loaded(level, origin.offset(relative))) {
				return WAIT + "chunk at " + relative + " not loaded";
			}
		}
		if (site.kind() == Kind.TURKEY) {
			int remaining = -1;
			int present = 0;
			for (int part = 0; part < cells.size(); part++) {
				BlockState state = level.getBlockState(origin.offset(cells.get(part)));
				if (state.isAir()) {
					continue;
				}
				if (!state.is(HarvestFestival.GIANT_TURKEY) || state.getValue(MultiDecorationBlock.FACING) != site.facing()
						|| state.getValue(GiantTurkeyFeastBlock.PART) != part
						|| remaining >= 0 && state.getValue(GiantTurkeyFeastBlock.REMAINING) != remaining) {
					return "turkey changed at " + cells.get(part);
				}
				remaining = state.getValue(GiantTurkeyFeastBlock.REMAINING);
				present++;
			}
			return present == 0 || present == cells.size() ? null : "turkey incomplete";
		}
		for (int i = 0; i < cells.size(); i++) {
			BlockPos world = origin.offset(cells.get(i));
			BlockState state = level.getBlockState(world);
			if (state.isAir()) {
				continue;
			}
			if (!(state.getBlock() instanceof FeastTableBlock) || state.getValue(FeastTableBlock.AXIS) != Direction.Axis.X
					|| state.getValue(FeastTableBlock.RUNNER) != FeastTableBlock.Runner.LINEN) {
				return "table changed at " + cells.get(i);
			}
			if (!(level.getBlockEntity(world) instanceof FeastTableBlockEntity table)) {
				return "table changed at " + cells.get(i);
			}
			for (int dish = 0; dish < FeastTableBlockEntity.DISHES; dish++) {
				if (!managedDish(table.dish(dish), entry, i * FeastTableBlockEntity.DISHES + dish)) {
					return "food added at " + cells.get(i);
				}
			}
		}
		return null;
	}

	/**
	 * A serving the town lays: the food with the private {@value #MARK} mark naming the season and site. The mark changes
	 * nothing else: it eats like any other serving, and a stamped serving taken anywhere is still only food. Because
	 * a Feast Table merges a dish only with the same item and components, a player's plain food never joins it.
	 */
	public static ItemStack stamped(ItemStack food, int season, String site) {
		CustomData.update(DataComponents.CUSTOM_DATA, food, tag -> tag.putString(MARK, season + "/" + site));
		return food;
	}

	/**
	 * Whether {@code dish} is the town's own: empty, or only the stamped serving laid at {@code slot}, no more of it than
	 * was laid. Unstamped food (a player's own, even of the same kind and below the laid count) is not.
	 */
	private static boolean managedDish(ItemStack dish, Ledger.Entry entry, int slot) {
		if (dish.isEmpty()) {
			return true;
		}
		if (slot >= entry.dishes().size()) {
			return false;
		}
		String laid = entry.dishes().get(slot);
		int star = laid.lastIndexOf('*');
		Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(laid.substring(0, star)));
		int count = Integer.parseInt(laid.substring(star + 1));
		return ItemStack.isSameItemSameComponents(dish, stamped(new ItemStack(item), entry.season(), entry.site()))
				&& dish.getCount() <= count;
	}

	/**
	 * Takes a checked site down without dropping anything: a table's dishes are emptied in code before each length goes
	 * (a Feast Table spills its dishes when removed), and the turkey's master goes first, so its other parts go without
	 * a drop (removing another part first would drop the packed feast from the master).
	 */
	private static void remove(ServerLevel level, BlockPos origin, Site site) {
		List<BlockPos> cells = site.cells();
		BlockState air = Blocks.AIR.defaultBlockState();
		if (site.kind() == Kind.TURKEY) {
			for (BlockPos relative : cells) {
				BlockPos world = origin.offset(relative);
				if (level.getBlockState(world).is(HarvestFestival.GIANT_TURKEY)) {
					TownBuilder.set(level, world, air);
				}
			}
			return;
		}
		for (BlockPos relative : cells) {
			BlockPos world = origin.offset(relative);
			if (level.getBlockEntity(world) instanceof FeastTableBlockEntity table) {
				for (int dish = 0; dish < FeastTableBlockEntity.DISHES; dish++) {
					table.clear(dish);
				}
			}
			if (level.getBlockState(world).getBlock() instanceof FeastTableBlock) {
				TownBuilder.set(level, world, air);
			}
		}
	}

	/** The site {@code pos} is a cell of, given the town's origin, or null. */
	private static @Nullable Site siteAt(BlockPos origin, BlockPos pos) {
		BlockPos relative = pos.subtract(origin);
		for (Site site : LAYOUT) {
			if (site.cells().contains(relative)) {
				return site;
			}
		}
		return null;
	}

	/** {@link #managed(ServerLevel, BlockPos)} against a given ledger and origin. */
	public static boolean managed(Ledger ledger, BlockPos origin, BlockPos pos) {
		Site site = siteAt(origin, pos);
		return site != null && ledger.isFor(origin) && !ledger.placed(site.id()).isEmpty();
	}

	/**
	 * Whether {@code pos} holds the feast exactly as this town set it out, for checks that compare the town with its
	 * blueprint: not air, a cell of a site the town's ledger has {@value #PLACED}, and that whole site still passing the
	 * identity check the town takes it down by ({@link #removable}: linen tables along x holding only the town's stamped
	 * servings, no more than were laid; the turkey whole, facing as set). Anything else at a feast cell (an owner's block,
	 * added or refilled food, a changed table, a preserved or skipped site) is not.
	 */
	public static boolean authored(ServerLevel level, BlockPos pos) {
		BlockPos origin = TownState.get(level).origin();
		return origin != null && authored(ledger(level), origin, level, pos);
	}

	/** {@link #authored(ServerLevel, BlockPos)} against a given ledger and origin. */
	public static boolean authored(Ledger ledger, BlockPos origin, ServerLevel level, BlockPos pos) {
		Site site = siteAt(origin, pos);
		if (site == null || !ledger.isFor(origin) || level.getBlockState(pos).isAir()) {
			return false;
		}
		for (Ledger.Entry entry : ledger.placed(site.id())) {
			if (removable(level, origin, site, entry) == null) {
				return true;
			}
		}
		return false;
	}

	/** {@link #mayPublicEat(ServerLevel, BlockPos, Player)} against a given ledger and origin (claims still apply: AND). */
	public static boolean mayPublicEat(Ledger ledger, BlockPos origin, ServerLevel level, BlockPos pos, Player player) {
		Site site = siteAt(origin, pos);
		if (site == null || !ledger.isFor(origin) || player.isSecondaryUseActive() || !player.getMainHandItem().isEmpty()
				|| !player.getOffhandItem().isEmpty()) {
			return false;
		}
		List<Ledger.Entry> placed = ledger.placed(site.id());
		if (placed.isEmpty()) {
			return false;
		}
		if (site.kind() == Kind.TURKEY) {
			return level.getBlockState(pos).is(HarvestFestival.GIANT_TURKEY)
					&& removable(level, origin, site, placed.getFirst()) == null;
		}
		int length = site.cells().indexOf(pos.subtract(origin));
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof FeastTableBlock) || state.getValue(FeastTableBlock.RUNNER) != FeastTableBlock.Runner.LINEN
				|| !(level.getBlockEntity(pos) instanceof FeastTableBlockEntity table)) {
			return false;
		}
		for (int dish = 0; dish < FeastTableBlockEntity.DISHES; dish++) {
			if (!managedDish(table.dish(dish), placed.getFirst(), length * FeastTableBlockEntity.DISHES + dish)) {
				return false;
			}
		}
		return true;
	}

	// -------------------------------------------------------------------------------------------------------- ledger

	/**
	 * What the feast did, per season and site, saved with the world: {@value #PLACED} (set out, with what each dish was
	 * laid with), {@value #REMOVED}, {@value #PRESERVED} (left alone, and why) or {@value #SKIPPED} (not set out, and why;
	 * tried again while in season). A ledger belongs to one town origin; a new origin starts it afresh.
	 */
	public static final class Ledger extends SavedData {
		public record Entry(int season, String site, String status, String reason, List<String> dishes) {
			public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
					Codec.INT.fieldOf("season").forGetter(Entry::season),
					Codec.STRING.fieldOf("site").forGetter(Entry::site),
					Codec.STRING.fieldOf("status").forGetter(Entry::status),
					Codec.STRING.optionalFieldOf("reason", "").forGetter(Entry::reason),
					Codec.STRING.listOf().optionalFieldOf("dishes", List.of()).forGetter(Entry::dishes)
			).apply(i, Entry::new));

			Entry with(String newStatus, String newReason) {
				return new Entry(season, site, newStatus, newReason, dishes);
			}

			String key() {
				return season + "/" + site;
			}
		}

		public static final Codec<Ledger> CODEC = RecordCodecBuilder.create(i -> i.group(
				BlockPos.CODEC.optionalFieldOf("origin").forGetter(l -> l.origin),
				Entry.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(l -> List.copyOf(l.entries.values()))
		).apply(i, Ledger::new));
		static final SavedDataType<Ledger> TYPE = new SavedDataType<>(Jugcraft.id("town_feast"), Ledger::new, CODEC, null);

		private Optional<BlockPos> origin;
		private final Map<String, Entry> entries = new LinkedHashMap<>();

		public Ledger() {
			this(Optional.empty(), List.of());
		}

		private Ledger(Optional<BlockPos> origin, List<Entry> entries) {
			this.origin = origin;
			for (Entry entry : entries) {
				this.entries.put(entry.key(), entry);
			}
		}

		boolean isFor(BlockPos town) {
			return origin.isPresent() && origin.get().equals(town);
		}

		void reset(BlockPos town) {
			origin = Optional.of(town);
			entries.clear();
			setDirty();
		}

		public @Nullable Entry get(int season, String site) {
			return entries.get(season + "/" + site);
		}

		/** The entries of a site still set out, oldest first. */
		public List<Entry> placed(String site) {
			List<Entry> out = new ArrayList<>();
			for (Entry entry : entries.values()) {
				if (entry.site().equals(site) && entry.status().equals(PLACED)) {
					out.add(entry);
				}
			}
			return out;
		}

		void put(Entry entry) {
			entries.put(entry.key(), entry);
			setDirty();
		}

		/** One line an entry, for the status report. */
		public List<String> lines() {
			List<String> out = new ArrayList<>();
			for (Entry entry : entries.values()) {
				out.add(entry.season() + " " + entry.site() + ": " + entry.status() + (entry.reason().isEmpty() ? "" : ", " + entry.reason()));
			}
			return out;
		}
	}
}
