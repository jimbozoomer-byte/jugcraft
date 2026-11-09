package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.FeastTableBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FeastTableBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.GiantTurkeyFeastBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HarvestFestival;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.finance.ExchangeBlocks;
import io.github.jimbozoomer.jugcraft.town.TownFeast;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The town square's feast service (TownFeast; docs/features/town-feast.md), driven directly with a test origin, theme,
 * date, blueprint and ledger, as the town's tick would drive it with the live ones. The arena (33 x 8 x 16, all air)
 * stands for the north plaza: town x 83..111 and z 84..99 at arena x 0..28 and z 0..15, town y 0 (the square's floor,
 * laid in stone here) at arena y 1.
 */
public final class TownFeastGameTests {
	private static final String ARENA = "jugcraft-test:claim_combat_arena";
	private static final LocalDate OPEN = LocalDate.of(2026, 11, 10), CLOSED = LocalDate.of(2026, 12, 1);
	private static final int SEASON = 2026;

	private static BlockPos origin(GameTestHelper h) {
		return h.absolutePos(new BlockPos(-83, 1, -84));
	}

	/** Lays the square's floor (town y 0) over the arena, with air above. */
	private static void square(GameTestHelper h) {
		for (int x = 0; x < 33; x++) {
			for (int z = 0; z < 16; z++) {
				h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
				for (int y = 2; y < 7; y++) {
					h.setBlock(new BlockPos(x, y, z), Blocks.AIR);
				}
			}
		}
	}

	/** The test's blueprint: stone floor at town y 0, open air above; {@code marks} are recorded owner overrides. */
	private static TownFeast.Baseline blueprint(Set<Long> marks) {
		return new TownFeast.Baseline() {
			@Override
			public BlockState expected(BlockPos relative) {
				int y = relative.getY();
				return y == 0 ? Blocks.STONE.defaultBlockState() : y > 0 && y < 6 ? Blocks.AIR.defaultBlockState() : null;
			}

			@Override
			public boolean preserved(BlockPos world) {
				return marks.contains(world.asLong());
			}
		};
	}

	private static List<String> run(GameTestHelper h, TownFeast.Ledger ledger, String theme, LocalDate date) {
		return TownFeast.run(h.getLevel(), origin(h), theme, date, blueprint(Set.of()), ledger, false);
	}

	private static TownFeast.Site site(String id) {
		return TownFeast.LAYOUT.stream().filter(site -> site.id().equals(id)).findFirst().orElseThrow();
	}

	private static BlockPos world(GameTestHelper h, BlockPos relative) {
		return origin(h).offset(relative);
	}

	private static FeastTableBlockEntity table(GameTestHelper h, String site, int length) {
		return (FeastTableBlockEntity) h.getLevel().getBlockEntity(world(h, site(site).cells().get(length)));
	}

	/** Every serving on both tables. */
	private static int servings(GameTestHelper h) {
		int total = 0;
		for (String id : List.of("W1", "E2")) {
			for (BlockPos cell : site(id).cells()) {
				if (h.getLevel().getBlockEntity(world(h, cell)) instanceof FeastTableBlockEntity table) {
					for (int dish = 0; dish < FeastTableBlockEntity.DISHES; dish++) {
						total += table.dish(dish).getCount();
					}
				}
			}
		}
		return total;
	}

	private static BlockState turkey(GameTestHelper h) {
		return h.getLevel().getBlockState(world(h, site("T").cells().getFirst()));
	}

	private static int drops(GameTestHelper h) {
		AABB area = new AABB(h.absolutePos(BlockPos.ZERO)).expandTowards(33, 8, 16).inflate(2.0);
		return h.getLevel().getEntitiesOfClass(ItemEntity.class, area, entity -> true).size();
	}

	private static String status(TownFeast.Ledger ledger, int season, String site) {
		TownFeast.Ledger.Entry entry = ledger.get(season, site);
		return entry == null ? "none" : entry.status();
	}

	private static boolean allAir(GameTestHelper h, String site) {
		return site(site).cells().stream().allMatch(cell -> h.getLevel().getBlockState(world(h, cell)).isAir());
	}

	/** A hit on the west (dish 0) or east (dish 1) half of a table length, or the middle of a turkey part. */
	private static BlockHitResult hit(BlockPos world, double along) {
		return new BlockHitResult(new Vec3(world.getX() + along, world.getY() + 0.8, world.getZ() + 0.5), Direction.UP, world, false);
	}

	private static ServerPlayer diner(GameTestHelper h) {
		ServerPlayer player = TestPlayers.survivor(h);
		player.snapTo(h.absoluteVec(new Vec3(16.5, 2, 13.5)));
		return player;
	}

	/**
	 * Eats one serving or portion at a cell the way the town's eat-only hook lets a player: hungry, with an empty hand (a
	 * bowl meal's bowl lands in the empty main-hand slot, so it is put aside after each serving).
	 */
	private static void eat(GameTestHelper h, ServerPlayer player, BlockPos world, double along) {
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.getFoodData().setFoodLevel(2);
		h.getLevel().getBlockState(world).useWithoutItem(h.getLevel(), player, hit(world, along));
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	/** The food laid at a site's dish slot, as a plain stack of {@code count} (what a player's own would be). */
	private static ItemStack plain(String site, int slot, int count) {
		return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(site(site).dishes().get(slot))), count);
	}

	/** Saves a table length's block entity and reads it back in place, as a chunk reload does. */
	private static void reloadTable(GameTestHelper h, String site, int length) {
		FeastTableBlockEntity table = table(h, site, length);
		var saved = table.saveWithoutMetadata(h.getLevel().registryAccess());
		table.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved));
	}

	private static TownFeast.Ledger reload(TownFeast.Ledger ledger) {
		var saved = TownFeast.Ledger.CODEC.encodeStart(NbtOps.INSTANCE, ledger).getOrThrow();
		return TownFeast.Ledger.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
	}

	@GameTest(structure = ARENA)
	public void setsOutTheWholeFeastOnceWithExactlyItsServings(GameTestHelper h) {
		square(h);
		TownFeast.Ledger ledger = new TownFeast.Ledger();
		List<String> dry = TownFeast.run(h.getLevel(), origin(h), TownFeast.THEME, OPEN, blueprint(Set.of()), ledger, true);
		h.assertTrue(allAir(h, "W1") && allAir(h, "E2") && allAir(h, "T") && ledger.get(SEASON, "W1") == null,
				"A dry run changes nothing: " + dry);
		List<String> lines = run(h, ledger, TownFeast.THEME, OPEN);
		for (TownFeast.Site site : TownFeast.LAYOUT) {
			h.assertTrue(status(ledger, SEASON, site.id()).equals(TownFeast.PLACED), site.id() + " is set out: " + lines);
		}
		for (String id : List.of("W1", "E2")) {
			TownFeast.Site site = site(id);
			for (int i = 0; i < site.lengths(); i++) {
				BlockState state = h.getLevel().getBlockState(world(h, site.cells().get(i)));
				FeastTableBlock.Part part = i == 0 ? FeastTableBlock.Part.START : i == site.lengths() - 1 ? FeastTableBlock.Part.END
						: FeastTableBlock.Part.MIDDLE;
				h.assertTrue(state.getBlock() instanceof FeastTableBlock && state.getValue(FeastTableBlock.AXIS) == Direction.Axis.X
						&& state.getValue(FeastTableBlock.RUNNER) == FeastTableBlock.Runner.LINEN && state.getValue(FeastTableBlock.PART) == part,
						id + " length " + i + " is a joined linen table: " + state);
				for (int dish = 0; dish < FeastTableBlockEntity.DISHES; dish++) {
					ItemStack laid = table(h, id, i).dish(dish);
					String wanted = site.dishes().get(i * FeastTableBlockEntity.DISHES + dish);
					h.assertTrue(BuiltInRegistries.ITEM.getKey(laid.getItem()).toString().equals(wanted) && laid.getCount() == TownFeast.SERVINGS,
							id + " length " + i + " dish " + dish + " holds four " + wanted + ": " + laid);
					h.assertTrue(ItemStack.isSameItemSameComponents(laid, TownFeast.stamped(new ItemStack(laid.getItem()), SEASON, id))
							&& !ItemStack.isSameItemSameComponents(laid, new ItemStack(laid.getItem())), id + " servings carry the town's mark");
				}
			}
		}
		h.assertTrue(servings(h) == 80, "Eighty table servings: " + servings(h));
		TownFeast.Site t = site("T");
		h.assertTrue(t.cells().getFirst().equals(new BlockPos(103, 1, 90)), "The turkey's master is at (103, 1, 90)");
		for (int part = 0; part < t.cells().size(); part++) {
			BlockPos cell = t.cells().get(part);
			BlockState state = h.getLevel().getBlockState(world(h, cell));
			h.assertTrue(cell.getX() >= 103 && cell.getX() <= 105 && cell.getZ() >= 88 && cell.getZ() <= 90 && cell.getY() >= 1 && cell.getY() <= 2,
					"Turkey part " + part + " inside x 103-105, z 88-90: " + cell);
			h.assertTrue(state.is(HarvestFestival.GIANT_TURKEY) && state.getValue(GiantTurkeyFeastBlock.PART) == part
					&& state.getValue(MultiDecorationBlock.FACING) == Direction.SOUTH
					&& state.getValue(GiantTurkeyFeastBlock.REMAINING) == GiantTurkeyFeastBlock.PORTIONS, "Turkey part " + part + ": " + state);
		}
		h.assertTrue(drops(h) == 0, "Setting out drops nothing");
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void eatenServingsAreNeverRefilledOnAnyReEntry(GameTestHelper h) {
		square(h);
		TownFeast.Ledger ledger = new TownFeast.Ledger();
		run(h, ledger, TownFeast.THEME, OPEN);
		ServerPlayer player = diner(h);
		BlockPos firstLength = world(h, site("W1").cells().getFirst());
		for (int i = 0; i < 3; i++) {
			eat(h, player, firstLength, 0.25);
		}
		BlockPos master = world(h, site("T").cells().getFirst());
		for (int i = 0; i < 5; i++) {
			eat(h, player, master, 0.5);
		}
		h.assertTrue(servings(h) == 77 && table(h, "W1", 0).dish(0).getCount() == 1, "Three servings eaten: " + servings(h));
		h.assertTrue(turkey(h).getValue(GiantTurkeyFeastBlock.REMAINING) == 19, "Five portions eaten: " + turkey(h));
		// Every way back in: a re-run, a dry run, a reload of the saved ledger, a town revision upgrade (another blueprint).
		run(h, ledger, TownFeast.THEME, OPEN);
		TownFeast.run(h.getLevel(), origin(h), TownFeast.THEME, OPEN, blueprint(Set.of()), ledger, true);
		ledger = reload(ledger);
		run(h, ledger, TownFeast.THEME, OPEN.plusDays(5));
		TownFeast.Baseline upgraded = new TownFeast.Baseline() {
			@Override
			public BlockState expected(BlockPos relative) {
				return Blocks.SMOOTH_STONE.defaultBlockState();
			}

			@Override
			public boolean preserved(BlockPos world) {
				return false;
			}
		};
		TownFeast.run(h.getLevel(), origin(h), TownFeast.THEME, OPEN, upgraded, ledger, false);
		h.assertTrue(servings(h) == 77 && turkey(h).getValue(GiantTurkeyFeastBlock.REMAINING) == 19,
				"Nothing is refilled: " + servings(h) + " servings, " + turkey(h));
		// The theme leaving and coming back within the season takes the feast down and never sets it out again.
		run(h, ledger, "december", OPEN);
		h.assertTrue(allAir(h, "W1") && allAir(h, "E2") && allAir(h, "T") && drops(h) == 0, "Taken down, spilling nothing");
		run(h, ledger, TownFeast.THEME, OPEN);
		run(h, reload(ledger), TownFeast.THEME, OPEN.plusDays(1));
		for (TownFeast.Site site : TownFeast.LAYOUT) {
			h.assertTrue(allAir(h, site.id()) && status(ledger, SEASON, site.id()).equals(TownFeast.REMOVED),
					site.id() + " is not set out again this season");
		}
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void consumedStaysConsumedAndIsStillTakenDown(GameTestHelper h) {
		square(h);
		TownFeast.Ledger ledger = new TownFeast.Ledger();
		run(h, ledger, TownFeast.THEME, OPEN);
		FeastTableBlockEntity first = table(h, "W1", 0);
		for (int i = 0; i < TownFeast.SERVINGS; i++) {
			first.takeServing(0);
		}
		for (BlockPos cell : site("T").cells()) {
			BlockPos at = world(h, cell);
			h.getLevel().setBlock(at, h.getLevel().getBlockState(at).setValue(GiantTurkeyFeastBlock.REMAINING, 0), 3);
		}
		run(h, ledger, TownFeast.THEME, OPEN);
		run(h, reload(ledger), TownFeast.THEME, OPEN.plusDays(10));
		h.assertTrue(table(h, "W1", 0).dish(0).isEmpty() && turkey(h).getValue(GiantTurkeyFeastBlock.REMAINING) == 0,
				"An emptied dish and an eaten turkey stay so");
		run(h, ledger, TownFeast.THEME, CLOSED);
		for (TownFeast.Site site : TownFeast.LAYOUT) {
			h.assertTrue(allAir(h, site.id()) && status(ledger, SEASON, site.id()).equals(TownFeast.REMOVED), site.id() + " is taken down");
		}
		h.assertTrue(drops(h) == 0, "Taking down the leftovers spills nothing: " + drops(h));
		run(h, ledger, TownFeast.THEME, OPEN.plusDays(15));
		h.assertTrue(allAir(h, "W1") && allAir(h, "T"), "Not set out again in the same season");
		run(h, ledger, TownFeast.THEME, LocalDate.of(2027, 11, 10));
		h.assertTrue(servings(h) == 80 && turkey(h).getValue(GiantTurkeyFeastBlock.REMAINING) == GiantTurkeyFeastBlock.PORTIONS
				&& status(ledger, 2027, "W1").equals(TownFeast.PLACED), "Next November's feast is a new one");
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void anOwnerOrFixtureChangeSkipsTheWholeSite(GameTestHelper h) {
		square(h);
		ServerLevel level = h.getLevel();
		TownFeast.Ledger ledger = new TownFeast.Ledger();
		Set<Long> marks = new HashSet<>();
		BlockPos ownerBlock = world(h, new BlockPos(84, 1, 89));
		level.setBlock(ownerBlock, Blocks.BARREL.defaultBlockState(), 3);
		BlockPos casino = world(h, new BlockPos(111, 1, 90));
		level.setBlock(casino, ExchangeBlocks.CASINO.values().iterator().next().defaultBlockState(), 3);
		marks.add(world(h, site("T").cells().get(4)).asLong());
		List<String> lines = TownFeast.run(level, origin(h), TownFeast.THEME, OPEN, blueprint(marks), ledger, false);
		for (TownFeast.Site site : TownFeast.LAYOUT) {
			h.assertTrue(allAir(h, site.id()) && status(ledger, SEASON, site.id()).equals(TownFeast.SKIPPED),
					site.id() + " is skipped whole: " + lines);
		}
		h.assertTrue(ledger.get(SEASON, "W1").reason().contains("changed from the blueprint"), "W1: " + ledger.get(SEASON, "W1"));
		h.assertTrue(ledger.get(SEASON, "E2").reason().contains("casino"), "E2: " + ledger.get(SEASON, "E2"));
		h.assertTrue(ledger.get(SEASON, "T").reason().contains("owner override"), "T: " + ledger.get(SEASON, "T"));
		h.assertTrue(level.getBlockState(ownerBlock).is(Blocks.BARREL) && level.getBlockState(casino).getBlock() instanceof io.github.jimbozoomer.jugcraft.finance.CasinoBlock,
				"The owner's blocks are untouched");
		// Once the owner's things are gone, a site never set out may still be set out this season.
		level.setBlock(ownerBlock, Blocks.AIR.defaultBlockState(), 3);
		level.setBlock(casino, Blocks.AIR.defaultBlockState(), 3);
		TownFeast.run(level, origin(h), TownFeast.THEME, OPEN, blueprint(Set.of()), ledger, false);
		for (TownFeast.Site site : TownFeast.LAYOUT) {
			h.assertTrue(status(ledger, SEASON, site.id()).equals(TownFeast.PLACED), site.id() + " is set out once the ground is clear");
		}
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void takingDownSpillsNothingAndKeepsPlayersFoodAndCloth(GameTestHelper h) {
		square(h);
		ServerLevel level = h.getLevel();
		TownFeast.Ledger ledger = new TownFeast.Ledger();
		run(h, ledger, TownFeast.THEME, OPEN);
		// W1: a player's bread on a dish whose rolls were eaten. E2: a player's leaf runner over the linen.
		FeastTableBlockEntity rolls = table(h, "W1", 1);
		for (int i = 0; i < TownFeast.SERVINGS; i++) {
			rolls.takeServing(1);
		}
		rolls.serve(1, new ItemStack(Items.BREAD, 2), true);
		BlockPos runner = world(h, site("E2").cells().getFirst());
		level.setBlock(runner, level.getBlockState(runner).setValue(FeastTableBlock.RUNNER, FeastTableBlock.Runner.LEAVES), 3);
		ServerPlayer player = diner(h);
		eat(h, player, world(h, site("T").cells().getFirst()), 0.5);
		eat(h, player, world(h, site("E2").cells().get(1)), 0.25);
		run(h, ledger, TownFeast.THEME, CLOSED);
		h.assertTrue(status(ledger, SEASON, "W1").equals(TownFeast.PRESERVED) && ledger.get(SEASON, "W1").reason().contains("food added"),
				"W1 with a player's bread is preserved: " + ledger.get(SEASON, "W1"));
		h.assertTrue(status(ledger, SEASON, "E2").equals(TownFeast.PRESERVED) && ledger.get(SEASON, "E2").reason().contains("table changed"),
				"E2 under a player's runner is preserved: " + ledger.get(SEASON, "E2"));
		h.assertTrue(status(ledger, SEASON, "T").equals(TownFeast.REMOVED) && allAir(h, "T"), "The partly eaten turkey is taken down");
		h.assertTrue(table(h, "W1", 1).dish(1).is(Items.BREAD) && table(h, "W1", 1).dish(1).getCount() == 2
				&& table(h, "W1", 0).dish(0).getCount() == TownFeast.SERVINGS, "W1 keeps the bread and the town's food");
		h.assertTrue(level.getBlockState(runner).getValue(FeastTableBlock.RUNNER) == FeastTableBlock.Runner.LEAVES
				&& table(h, "E2", 1).dish(0).getCount() == TownFeast.SERVINGS - 1, "E2 keeps its runner and dishes");
		h.assertTrue(drops(h) == 0, "Nothing is spilled: " + drops(h));
		run(h, ledger, TownFeast.THEME, CLOSED.plusDays(1));
		h.assertTrue(status(ledger, SEASON, "W1").equals(TownFeast.PRESERVED) && !allAir(h, "W1"), "A preserved site is left alone after");
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void theTurkeyGoesMasterFirst(GameTestHelper h) {
		square(h);
		ServerLevel level = h.getLevel();
		TownFeast.Ledger ledger = new TownFeast.Ledger();
		run(h, ledger, TownFeast.THEME, OPEN);
		run(h, ledger, TownFeast.THEME, CLOSED);
		h.assertTrue(allAir(h, "T") && drops(h) == 0, "The service takes the turkey down master first, dropping nothing");
		// Why the order matters: removing any other part first makes the master drop the packed feast.
		GiantTurkeyFeastBlock block = HarvestFestival.GIANT_TURKEY;
		BlockPos master = world(h, new BlockPos(93, 1, 88));
		for (int part = 0; part < block.cells().length; part++) {
			level.setBlock(block.partPos(master, Direction.SOUTH, part), block.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.SOUTH)
					.setValue(GiantTurkeyFeastBlock.PART, part), 3);
		}
		level.destroyBlock(block.partPos(master, Direction.SOUTH, 5), false);
		h.runAfterDelay(2, () -> {
			h.assertTrue(drops(h) == 1, "A part removed first drops the feast once from the master: " + drops(h));
			h.succeed();
		});
	}

	@GameTest(structure = ARENA)
	public void onlyManagedFeastCellsArePubliclyEdible(GameTestHelper h) {
		square(h);
		ServerLevel level = h.getLevel();
		TownFeast.Ledger ledger = new TownFeast.Ledger();
		BlockPos origin = origin(h);
		// The owner's own feast table elsewhere on the square stays private.
		BlockPos owners = world(h, new BlockPos(97, 1, 91));
		level.setBlock(owners, JugcraftAgriculture.block("feast_table").defaultBlockState().setValue(FeastTableBlock.RUNNER, FeastTableBlock.Runner.LINEN), 3);
		((FeastTableBlockEntity) level.getBlockEntity(owners)).serve(0, new ItemStack(Items.BREAD, 4), true);
		run(h, ledger, TownFeast.THEME, OPEN);
		ServerPlayer player = diner(h);
		BlockPos length = world(h, site("W1").cells().getFirst());
		BlockPos master = world(h, site("T").cells().getFirst());
		h.assertTrue(TownFeast.managed(ledger, origin, length) && TownFeast.managed(ledger, origin, master) && !TownFeast.managed(ledger, origin, owners),
				"Only the feast's own cells are managed");
		h.assertTrue(TownFeast.mayPublicEat(ledger, origin, level, length, player), "An empty hand may eat at a managed table");
		h.assertTrue(TownFeast.mayPublicEat(ledger, origin, level, master, player), "and at the turkey");
		h.assertTrue(!TownFeast.mayPublicEat(ledger, origin, level, owners, player), "but not at the owner's table");
		// What the protection hook does when it may: the table's own eating, one serving.
		eat(h, player, length, 0.25);
		h.assertTrue(table(h, "W1", 0).dish(0).getCount() == TownFeast.SERVINGS - 1, "Eating takes one serving");
		player.setShiftKeyDown(true);
		h.assertTrue(!TownFeast.mayPublicEat(ledger, origin, level, length, player) && !TownFeast.mayPublicEat(ledger, origin, level, master, player),
				"Sneaking (taking a dish, packing the turkey) is refused");
		player.setShiftKeyDown(false);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD));
		h.assertTrue(!TownFeast.mayPublicEat(ledger, origin, level, length, player) && !TownFeast.mayPublicEat(ledger, origin, level, master, player),
				"An item in the main hand (serving onto the table) is refused");
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.BREAD));
		h.assertTrue(!TownFeast.mayPublicEat(ledger, origin, level, length, player) && !TownFeast.mayPublicEat(ledger, origin, level, master, player),
				"An item in the offhand is refused");
		player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		h.assertTrue(TownFeast.mayPublicEat(ledger, origin, level, length, player) && TownFeast.mayPublicEat(ledger, origin, level, master, player),
				"Both hands empty again: allowed at the table and the turkey");
		// A length someone refilled with their own food (even the same food, below the laid count) is no longer the town's.
		BlockPos second = world(h, site("W1").cells().get(1));
		FeastTableBlockEntity refilled = table(h, "W1", 1);
		h.assertTrue(refilled.serve(0, plain("W1", 2, 1), true) == 0, "A player's plain food does not merge into the town's stamped dish");
		refilled.clear(0);
		h.assertTrue(refilled.serve(0, plain("W1", 2, 3), true) == 3, "but can be laid on a dish they emptied");
		h.assertTrue(!TownFeast.mayPublicEat(ledger, origin, level, second, player) && TownFeast.mayPublicEat(ledger, origin, level, length, player),
				"Only the refilled length turns private");
		BlockPos third = world(h, site("W1").cells().get(2));
		FeastTableBlockEntity added = table(h, "W1", 2);
		added.clear(1);
		added.serve(1, new ItemStack(Items.BREAD, 2), true);
		h.assertTrue(!TownFeast.mayPublicEat(ledger, origin, level, third, player), "A length with other food added turns private");
		run(h, ledger, TownFeast.THEME, CLOSED);
		h.assertTrue(!TownFeast.managed(ledger, origin, master) && !TownFeast.mayPublicEat(ledger, origin, level, master, player),
				"Once taken down, nothing is managed");
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void aPlayersOwnFoodIsNeverMistakenForTheTowns(GameTestHelper h) {
		square(h);
		ServerLevel level = h.getLevel();
		TownFeast.Ledger ledger = new TownFeast.Ledger();
		run(h, ledger, TownFeast.THEME, OPEN);
		ServerPlayer player = diner(h);
		// Eat one stamped serving: it is ordinary food to the eater.
		BlockPos first = world(h, site("W1").cells().getFirst());
		player.getFoodData().setFoodLevel(2);
		eat(h, player, first, 0.25);
		h.assertTrue(table(h, "W1", 0).dish(0).getCount() == TownFeast.SERVINGS - 1 && player.getFoodData().getFoodLevel() > 2,
				"A stamped serving is eaten like any other: " + player.getFoodData().getFoodLevel());
		// The owner adds one plain serving of the same food: it cannot merge into the town's stamped dish.
		h.assertTrue(table(h, "W1", 0).serve(0, plain("W1", 0, 1), true) == 0 && table(h, "W1", 0).dish(0).getCount() == TownFeast.SERVINGS - 1,
				"Plain food of the same kind does not join the town's servings");
		// The owner takes back another dish and refills it, below the laid count, with plain food of the same kind.
		FeastTableBlockEntity cranberry = table(h, "W1", 2);
		cranberry.clear(0);
		h.assertTrue(cranberry.serve(0, plain("W1", 4, 2), true) == 2, "The owner's own cranberry sauce is laid");
		// The table and the ledger both survive a save and reload.
		reloadTable(h, "W1", 0);
		reloadTable(h, "W1", 2);
		ledger = reload(ledger);
		h.assertTrue(table(h, "W1", 2).dish(0).getCount() == 2
				&& ItemStack.isSameItemSameComponents(table(h, "W1", 2).dish(0), plain("W1", 4, 1)), "The owner's plain food is still plain after reload");
		h.assertTrue(ItemStack.isSameItemSameComponents(table(h, "W1", 0).dish(0), TownFeast.stamped(plain("W1", 0, 1), SEASON, "W1")),
				"The town's servings keep their mark after reload");
		run(h, ledger, TownFeast.THEME, CLOSED);
		h.assertTrue(status(ledger, SEASON, "W1").equals(TownFeast.PRESERVED) && ledger.get(SEASON, "W1").reason().contains("food added"),
				"The table with the owner's refill is preserved: " + ledger.get(SEASON, "W1"));
		h.assertTrue(table(h, "W1", 2).dish(0).getCount() == 2 && table(h, "W1", 0).dish(0).getCount() == TownFeast.SERVINGS - 1,
				"Cleanup leaves the owner's food (and the table) in place");
		h.assertTrue(status(ledger, SEASON, "E2").equals(TownFeast.REMOVED) && status(ledger, SEASON, "T").equals(TownFeast.REMOVED)
				&& drops(h) == 0, "The untouched sites go, spilling nothing");
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void onlyAPositivelyManagedNeighbourMayStandInAMargin(GameTestHelper h) {
		square(h);
		ServerLevel level = h.getLevel();
		TownFeast.Ledger ledger = new TownFeast.Ledger();
		// The owner's own feast table on one of the turkey's cells, inside E2's margin, before anything is set out.
		BlockPos owners = world(h, new BlockPos(104, 1, 89));
		level.setBlock(owners, JugcraftAgriculture.block("feast_table").defaultBlockState().setValue(FeastTableBlock.RUNNER, FeastTableBlock.Runner.LINEN), 3);
		List<String> lines = run(h, ledger, TownFeast.THEME, OPEN);
		h.assertTrue(status(ledger, SEASON, "E2").equals(TownFeast.SKIPPED) && ledger.get(SEASON, "E2").reason().contains("changed from the blueprint"),
				"An owner's feast table in E2's margin is not taken for a neighbour: " + lines);
		h.assertTrue(status(ledger, SEASON, "T").equals(TownFeast.SKIPPED) && allAir(h, "E2"), "Nothing is set out beside it: " + lines);
		h.assertTrue(status(ledger, SEASON, "W1").equals(TownFeast.PLACED), "W1, away from it, is set out");
		h.assertTrue(level.getBlockState(owners).getBlock() instanceof FeastTableBlock, "The owner's table is untouched");
		// Gone again, the turkey and E2 are set out side by side: each accepts the other, the ledger's own, in its margin.
		level.setBlock(owners, Blocks.AIR.defaultBlockState(), 3);
		lines = run(h, ledger, TownFeast.THEME, OPEN);
		h.assertTrue(status(ledger, SEASON, "E2").equals(TownFeast.PLACED) && status(ledger, SEASON, "T").equals(TownFeast.PLACED),
				"The managed neighbour does not stop the other: " + lines);
		h.succeed();
	}
}
