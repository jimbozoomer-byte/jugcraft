package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jimbozoomer.jugcraft.town.JugcraftTown;
import io.github.jimbozoomer.jugcraft.town.CommissionBoardBlock;
import io.github.jimbozoomer.jugcraft.town.Jugs;
import io.github.jimbozoomer.jugcraft.town.ShopMenu;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.TownDecor;
import io.github.jimbozoomer.jugcraft.town.TownRoutine;
import io.github.jimbozoomer.jugcraft.town.TownShops;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

/**
 * The living spawn city (docs/features/living-spawn-city.md): the residents' day, their bedtime, the shops' hours with
 * the taverns' all-hours essentials, and the revision-4 blueprint's additions over the frozen revision 3.
 */
public class TownRoutineGameTests {
	/** The Church of Notch's whole plot, where the owner's slot machines stand: nothing may be added there. */
	private static final int CHURCH_X0 = 78, CHURCH_Z0 = 35, CHURCH_X1 = 114, CHURCH_Z1 = 87;

	@GameTest(maxTicks = 20)
	public void theTownKeepsItsDay(GameTestHelper h) {
		h.assertTrue(TownRoutine.period(0) == TownRoutine.Period.MORNING && TownRoutine.period(999) == TownRoutine.Period.MORNING
				&& TownRoutine.period(1000) == TownRoutine.Period.WORK && TownRoutine.period(11999) == TownRoutine.Period.WORK
				&& TownRoutine.period(12000) == TownRoutine.Period.EVENING && TownRoutine.period(17999) == TownRoutine.Period.EVENING
				&& TownRoutine.period(18000) == TownRoutine.Period.NIGHT && TownRoutine.period(23999) == TownRoutine.Period.NIGHT
				&& TownRoutine.period(24000 * 5 + 1000) == TownRoutine.Period.WORK && TownRoutine.period(-1) == TownRoutine.Period.NIGHT,
				"Dawn, work from 7am, evening from 6pm, night from midnight, on every day");
		h.assertTrue(Townsfolk.bedtime(18000) && !Townsfolk.bedtime(17999) && !Townsfolk.bedtime(0),
				"Bedtime and the night begin together, at midnight");
		h.assertTrue(TownRoutine.clock(1000).equals("7am") && TownRoutine.clock(12000).equals("6pm") && TownRoutine.clock(6000).equals("12pm")
				&& TownRoutine.clock(18000).equals("12am"), "Hours read as people say them");
		h.assertTrue(TownRoutine.activity("baker", "", TownRoutine.Period.WORK) == TownRoutine.Activity.KNEAD
				&& TownRoutine.activity("gardener", "", TownRoutine.Period.WORK) == TownRoutine.Activity.TEND
				&& TownRoutine.activity("porter", "tavern", TownRoutine.Period.EVENING) == TownRoutine.Activity.TOAST
				&& TownRoutine.activity("porter", "fountain", TownRoutine.Period.EVENING) == TownRoutine.Activity.CHAT
				&& TownRoutine.activity("busker", "tavern", TownRoutine.Period.EVENING) == TownRoutine.Activity.PLAY
				&& TownRoutine.activity("watch", "", TownRoutine.Period.NIGHT) == TownRoutine.Activity.WATCH
				&& TownRoutine.activity("innkeeper", "", TownRoutine.Period.NIGHT) == TownRoutine.Activity.DOZE
				&& TownRoutine.activity("baker", "", TownRoutine.Period.NIGHT) == TownRoutine.Activity.NONE,
				"Each job has its own work, and the evening its gatherings");
		h.assertTrue(TownRoutine.reach("watch", 20, TownRoutine.Period.NIGHT, true) == 20
				&& TownRoutine.reach("baker", 2, TownRoutine.Period.NIGHT, true) == 1
				&& TownRoutine.reach("sweeper", 70, TownRoutine.Period.WORK, true) == 10
				&& TownRoutine.reach("gardener", 70, TownRoutine.Period.EVENING, true) == 1
				&& TownRoutine.reach("decorator", 96, TownRoutine.Period.WORK, false) == 96,
				"The watch walks its beat all night; civilians keep close to their place");
		// Every line a resident can say about their day is in the shipped language file.
		JsonObject lang;
		try (InputStream raw = TownRoutineGameTests.class.getResourceAsStream("/assets/jugcraft/lang/en_us.json")) {
			h.assertTrue(raw != null, "The language file ships");
			lang = JsonParser.parseReader(new InputStreamReader(raw, StandardCharsets.UTF_8)).getAsJsonObject();
		} catch (java.io.IOException e) {
			throw new IllegalStateException(e);
		}
		for (var job : TownRoutine.TALK.entrySet()) {
			for (String part : job.getValue()) {
				for (int i = 0; i < 3; i++) {
					String key = "message.jugcraft.townsfolk.routine." + job.getKey() + "." + part + "." + i;
					h.assertTrue(lang.has(key), "Missing resident line " + key);
				}
			}
		}
		for (String key : List.of("message.jugcraft.townsfolk.asleep", "message.jugcraft.shop.hours", "message.jugcraft.shop.closed",
				"message.jugcraft.shop.closed_now", "screen.jugcraft.shop.essentials")) {
			h.assertTrue(lang.has(key), "Missing " + key);
		}
		for (TownRoutine.Period period : TownRoutine.Period.values()) {
			for (String job : List.of("baker", "watch", "innkeeper", "gardener", "shopkeeper")) {
				String key = TownRoutine.line(job, period, 2);
				h.assertTrue(key == null || lang.has(key), "A " + job + "'s line for the " + period.key + " exists: " + key);
			}
		}
		h.assertTrue(TownRoutine.line("watch", TownRoutine.Period.NIGHT, 0) != null && TownRoutine.line("baker", TownRoutine.Period.NIGHT, 0) == null
				&& TownRoutine.line("baker", TownRoutine.Period.MORNING, 1).contains(".townsfolk.morning."),
				"The watch talks at night; everyone shares the dawn greetings");
		h.succeed();
	}

	@GameTest(maxTicks = 20)
	public void revisionFourOnlyAddsToAuthoredAir(GameTestHelper h) {
		TownData old = TownData.frozen(3), next = TownData.get();
		h.assertTrue(old.size == next.size && old.height == next.height && old.yMin == next.yMin, "The town keeps its footprint");
		int added = 0;
		BlockPos board = null;
		int boardParts = 0;
		for (int y = next.yMin; y < next.yMin + next.height; y++) {
			for (int z = 0; z < next.size; z++) {
				for (int x = 0; x < next.size; x++) {
					String before = old.paletteText.get(old.index(x, y, z)), after = next.paletteText.get(next.index(x, y, z));
					if (before.equals(after)) {
						continue;
					}
					added++;
					String where = " at " + x + " " + y + " " + z;
					h.assertTrue(before.equals("minecraft:air"), "Revision 4 only fills authored air, not " + before + where);
					h.assertTrue(x < CHURCH_X0 || x > CHURCH_X1 || z < CHURCH_Z0 || z > CHURCH_Z1,
							"Nothing is added on the Church of Notch's plot: " + after + where);
					h.assertTrue(next.mask(x, z) == TownData.INSIDE, "Additions stay inside the wall: " + after + where);
					h.assertTrue(!next.state(next.index(x, y, z)).hasBlockEntity(), "No block entity or container is added: " + after + where);
					if (after.startsWith("jugcraft:town_commission_board")) {
						boardParts++;
						var state = next.state(next.index(x, y, z));
						BlockPos root = CommissionBoardBlock.rootPos(new BlockPos(x, y, z), state);
						h.assertTrue(new BlockPos(104, 1, 112).equals(root), "Every board panel belongs to the southern square noticeboard");
						if (state.getValue(CommissionBoardBlock.PART) == 0) board = root;
					}
				}
			}
		}
		h.assertTrue(added > 20 && added < 200, "Revision 4 adds street furniture, not a new town: " + added + " cells");
		h.assertTrue(boardParts == 9 && new BlockPos(104, 1, 112).equals(board),
				"The nine-panel noticeboard stands at the southern square edge, clear of the market stalls: " + board);
		h.assertTrue(next.spots.size() >= old.spots.size(), "No resident is dropped");
		for (int i = 0; i < old.spots.size(); i++) {
			TownData.Spot a = old.spots.get(i), b = next.spots.get(i);
			h.assertTrue(a.role().equals(b.role()) && a.name().equals(b.name()) && a.skin().equals(b.skin()) && a.pos().equals(b.pos())
					&& java.util.Objects.equals(a.shop(), b.shop()) && java.util.Objects.equals(old.beds.get(i), next.beds.get(i)),
					"Resident " + i + " (" + a.name() + ") keeps their identity, place and bed");
		}
		h.assertTrue(old.sites.size() == next.sites.size() && old.atms.equals(next.atms) && old.signs.size() == next.signs.size(),
				"Decor sites, Jug Tellers and signs stay where they were");
		for (int i = 0; i < old.sites.size(); i++) {
			h.assertTrue(old.sites.get(i).kind().equals(next.sites.get(i).kind()) && old.sites.get(i).anchor().equals(next.sites.get(i).anchor()),
					"Decor site " + i + " keeps its index, kind and place");
		}
		for (int i = 0; i < old.signs.size(); i++) {
			h.assertTrue(old.signs.get(i).pos().equals(next.signs.get(i).pos()), "Sign " + i + " stays where it was");
		}
		for (TownData.Spot spot : next.spots) {
			TownRoutine.Places places = next.routines.get(spot.index());
			h.assertTrue(places != null, spot.name() + " has a day in the town");
			for (BlockPos at : new BlockPos[] {places.work(), places.evening()}) {
				if (at != null) {
					h.assertTrue(next.mask(at.getX(), at.getZ()) == TownData.INSIDE
							&& (at.getX() < CHURCH_X0 || at.getX() > CHURCH_X1 || at.getZ() < CHURCH_Z0 || at.getZ() > CHURCH_Z1),
							spot.name() + "'s places are in the town, off the church plot: " + at);
				}
			}
		}
		h.assertTrue(next.spots.stream().filter(s -> s.role().equals("townsfolk")).allMatch(s -> next.routines.get(s.index()).work() != null),
				"Every street resident has work to go to");
		h.assertTrue(next.spots.stream().filter(s -> next.routines.get(s.index()).evening() != null).count() >= 15,
				"Most of the town gathers in the evening");
		h.succeed();
	}

	@GameTest(maxTicks = 1300, padding = 16)
	public void residentsWorkByDayGatherInTheEveningAndStayPutAtNight(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		floor(h);
		long[] clock = {3000};
		BlockPos work = h.absolutePos(new BlockPos(0, 2, 0)), evening = h.absolutePos(new BlockPos(7, 2, 0));
		BlockPos start = h.absolutePos(new BlockPos(7, 2, 7));
		TownRoutine.Places plan = new TownRoutine.Places("baker", "fountain", work, work.east(), evening, evening.west());
		Townsfolk baker = new Townsfolk(JugcraftTown.TOWNSFOLK, level) {
			@Override
			protected long scheduleTime() {
				return clock[0];
			}

			@Override
			protected TownRoutine.Places places() {
				return plan;
			}
		};
		baker.setUp(new TownData.Spot(-1, "baker", null, "Test baker", "townsfolk_1", BlockPos.ZERO), start);
		baker.snapTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0, 0);
		level.addFreshEntity(baker);
		int[] phase = {0};
		int[] still = {0};
		Vec3[] rest = {Vec3.ZERO};
		h.onEachTick(() -> {
			switch (phase[0]) {
				case 0 -> {
					if (near(baker, work, 2.5) && baker.activity() == TownRoutine.Activity.KNEAD) {
						h.assertTrue(baker.getMainHandItem().is(Items.BREAD), "The baker keeps the bread they carry");
						clock[0] = 13000;
						phase[0] = 1;
					}
				}
				case 1 -> {
					if (near(baker, evening, 2.0) && baker.activity() == TownRoutine.Activity.CHAT) {
						clock[0] = 19000;
						phase[0] = 2;
					}
				}
				case 2 -> {
					if (near(baker, start, 2.5) && baker.activity() == TownRoutine.Activity.DOZE) {
						rest[0] = baker.position();
						phase[0] = 3;
					}
				}
				case 3 -> {
					h.assertTrue(!baker.isSleeping() && baker.position().distanceTo(rest[0]) < 1.0,
							"Without a bed, a resident rests where they are at night, not wandering: moved " + baker.position().distanceTo(rest[0]));
					if (++still[0] >= 100) {
						phase[0] = 4;
						baker.discard();
						h.succeed();
					}
				}
				default -> {
				}
			}
		});
		h.runAtTickTime(1250, () -> {
			if (phase[0] < 4) {
				baker.discard();
				h.fail("The baker's day stopped in phase " + phase[0] + " at " + baker.blockPosition() + " doing " + baker.activity());
			}
		});
	}

	@GameTest(maxTicks = 1000, padding = 16)
	public void aResidentKeptFromTheirBedRestsInsteadOfSleepwalking(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		floor(h);
		BlockPos foot = h.absolutePos(new BlockPos(6, 2, 5)), head = foot.relative(Direction.SOUTH);
		level.setBlockAndUpdate(foot, TownData.parse("minecraft:blue_bed").setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.FOOT));
		level.setBlockAndUpdate(head, TownData.parse("minecraft:blue_bed").setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.HEAD));
		// Walled in on every side, two high: the bed is there but out of reach.
		for (int x = 5; x <= 7; x++) {
			for (int z = 4; z <= 7; z++) {
				for (int y = 2; y <= 3; y++) {
					BlockPos at = h.absolutePos(new BlockPos(x, y, z));
					if (!at.equals(foot) && !at.equals(head)) {
						level.setBlockAndUpdate(at, Blocks.STONE.defaultBlockState());
					}
				}
			}
		}
		BlockPos start = h.absolutePos(new BlockPos(1, 2, 1));
		Townsfolk resident = new Townsfolk(JugcraftTown.TOWNSFOLK, level) {
			@Override
			protected long scheduleTime() {
				return 19000;
			}

			@Override
			public BlockPos bed() {
				return head;
			}

			@Override
			protected TownRoutine.Places places() {
				return new TownRoutine.Places("porter", "tavern", null, null, null, null);
			}
		};
		resident.setUp(new TownData.Spot(-1, "townsfolk", null, "Test porter", "townsfolk_1", BlockPos.ZERO), start);
		resident.snapTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0, 0);
		level.addFreshEntity(resident);
		int[] still = {0};
		Vec3[] rest = {null};
		h.onEachTick(() -> {
			h.assertTrue(!resident.isSleeping(), "Nobody sleeps in a bed they could not reach");
			if (rest[0] == null) {
				if (resident.activity() == TownRoutine.Activity.DOZE && resident.getNavigation().isDone()) {
					rest[0] = resident.position();
				}
				return;
			}
			h.assertTrue(resident.position().distanceTo(rest[0]) < 1.0, "Having given up on the bed, they stay put until dawn");
			if (++still[0] >= 100) {
				h.assertTrue(resident.position().distanceTo(Vec3.atCenterOf(head)) > 1.5, "They did not pass through the walls");
				resident.discard();
				h.succeed();
			}
		});
		h.runAtTickTime(980, () -> {
			if (still[0] < 100) {
				resident.discard();
				h.fail("A resident who cannot reach their bed should give up and rest: at " + resident.blockPosition() + " doing " + resident.activity());
			}
		});
	}

	@GameTest(maxTicks = 40, padding = 16)
	public void sleepersOutOfTurnGetUp(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		floor(h);
		BlockPos foot = h.absolutePos(new BlockPos(2, 2, 2)), head = foot.relative(Direction.SOUTH);
		level.setBlockAndUpdate(foot, TownData.parse("minecraft:blue_bed").setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.FOOT));
		level.setBlockAndUpdate(head, TownData.parse("minecraft:blue_bed").setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.HEAD));
		long[] clock = {6000};
		BlockPos[] own = {head};
		Townsfolk resident = new Townsfolk(JugcraftTown.TOWNSFOLK, level) {
			@Override
			protected long scheduleTime() {
				return clock[0];
			}

			@Override
			public BlockPos bed() {
				return own[0];
			}
		};
		resident.setUp(new TownData.Spot(-1, "townsfolk", null, "Test sleeper", "townsfolk_1", BlockPos.ZERO), foot.east());
		resident.snapTo(foot.getX() + 1.5, foot.getY(), foot.getZ() + 0.5, 0, 0);
		level.addFreshEntity(resident);
		// As if loaded from a save made at night, now noon: no bed goal is running to wake them.
		resident.startSleeping(head);
		h.runAfterDelay(5, () -> {
			h.assertTrue(!resident.isSleeping(), "A resident found asleep in daylight gets up");
			clock[0] = 19000;
			own[0] = head.east(3);
			resident.startSleeping(head);
		});
		h.runAfterDelay(10, () -> {
			h.assertTrue(!resident.isSleeping(), "A resident asleep in a bed that is no longer theirs gets up");
			resident.discard();
			h.succeed();
		});
	}

	@GameTest(maxTicks = 20)
	public void shopsKeepTheirHoursAndTavernsSellEssentialsAllNight(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		ServerPlayer player = h.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		long[] clock = {3000};
		Townsfolk keeper = keeper(level, clock, "shopkeeper", "general", player.position());
		Townsfolk innkeeper = keeper(level, clock, "innkeeper", null, player.position());
		TownShops.Shop general = TownShops.get().shop("general");
		h.assertTrue(general != null && general.openAt(3000) && !general.openAt(500) && !general.openAt(12000) && !general.openAt(20000),
				"The General Store keeps 7am to 6pm");
		for (TownShops.Shop shop : TownShops.get().shops.values()) {
			h.assertTrue(shop.openAt(6000) && !shop.openAt(13000) && !shop.openAt(19000), shop.name() + " is open at noon and shut in the evening and at night");
		}
		h.assertTrue(TownShops.get().essentialsShop() == general && general.essentials().containsAll(
				List.of("minecraft:torch", "minecraft:bread", "minecraft:cooked_beef", "minecraft:white_bed")),
				"Light, food and a bed are the essentials, from the General Store");
		ShopMenu full = ShopMenu.serve(1, player, keeper, false);
		h.assertTrue(full != null && full.sales().equals(general.sellsIn(TownDecor.theme())) && !full.purchases().isEmpty(),
				"By day the keeper's whole shop is listed");
		int torch = indexOf(full.sales(), "minecraft:torch");
		Jugs.add(server, player.getUUID(), 100);
		long before = Jugs.balance(server, player.getUUID());
		ShopMenu.clear();
		h.assertTrue(full.clickMenuButton(player, torch) && Jugs.balance(server, player.getUUID()) < before, "In its hours the shop sells");
		clock[0] = 13000;
		ShopMenu.clear();
		long evening = Jugs.balance(server, player.getUUID());
		h.assertTrue(!full.clickMenuButton(player, torch) && Jugs.balance(server, player.getUUID()) == evening,
				"After six the shop sells nothing, even with its screen left open");
		keeper.talkTo(player, InteractionHand.MAIN_HAND);
		h.assertTrue(!(player.containerMenu instanceof ShopMenu), "A closed shop's keeper says when it opens instead of trading");
		ShopMenu counter = ShopMenu.serve(2, player, innkeeper, true);
		List<String> listed = counter.sales().stream().map(TownShops.Offer::item).toList();
		h.assertTrue(listed.equals(general.essentials()) && counter.purchases().isEmpty(),
				"The tavern counter lists exactly the essentials and buys nothing: " + listed);
		clock[0] = 20000;
		int bread = indexOf(counter.sales(), "minecraft:bread");
		int had = player.getInventory().countItem(Items.BREAD);
		ShopMenu.clear();
		long night = Jugs.balance(server, player.getUUID());
		h.assertTrue(counter.clickMenuButton(player, bread) && Jugs.balance(server, player.getUUID()) < night
				&& player.getInventory().countItem(Items.BREAD) > had, "At night the tavern still sells bread");
		ShopMenu.clear();
		h.assertTrue(!counter.clickMenuButton(player, ShopMenu.SELL_BASE), "Nothing can be sold to the tavern counter");
		h.assertTrue(!new ShopMenu.Opening("general", "", 0).essentials(), "A shop opened the old way is the whole shop");
		ShopMenu.clear();
		keeper.discard();
		innkeeper.discard();
		h.succeed();
	}

	// ---------------------------------------------------------------- helpers

	private static void floor(GameTestHelper h) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	private static boolean near(Townsfolk who, BlockPos at, double distance) {
		return who.position().distanceTo(Vec3.atBottomCenterOf(at)) <= distance;
	}

	private static int indexOf(List<TownShops.Offer> offers, String item) {
		for (int i = 0; i < offers.size(); i++) {
			if (offers.get(i).item().equals(item)) {
				return i;
			}
		}
		throw new IllegalStateException(item + " is not listed");
	}

	/** A keeper standing by the player, on a test clock (not added to the world: only the menus are tested). */
	private static Townsfolk keeper(ServerLevel level, long[] clock, String role, String shop, Vec3 at) {
		Townsfolk keeper = new Townsfolk(JugcraftTown.TOWNSFOLK, level) {
			@Override
			protected long scheduleTime() {
				return clock[0];
			}
		};
		keeper.setUp(new TownData.Spot(-1, role, shop, "Test " + role, "townsfolk_1", BlockPos.ZERO), BlockPos.containing(at));
		keeper.snapTo(at.x, at.y, at.z, 0, 0);
		return keeper;
	}
}
