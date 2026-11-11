package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.JugcraftTown;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.TownShops;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.guests.GuestActivity;
import io.github.jimbozoomer.jugcraft.town.guests.GuestBalance;
import io.github.jimbozoomer.jugcraft.town.guests.GuestCeremonies;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFavorTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFavors;
import io.github.jimbozoomer.jugcraft.town.guests.GuestKeepsakes;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedger;
import io.github.jimbozoomer.jugcraft.town.guests.GuestMarriage;
import io.github.jimbozoomer.jugcraft.town.guests.GuestMilestoneMeals;
import io.github.jimbozoomer.jugcraft.town.guests.GuestMilestoneTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestResult;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRewards;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRoster;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRosterTesting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * JZE guest expansion, worker D (docs/plans/jze-guest-milestones-design.md): milestones, keepsakes, the shared meal, the
 * church ceremony and opt-in notices. Registered by the coordinator in the gametest fabric.mod.json.
 *
 * <p>Every test uses fresh survival players ({@link TestPlayers#survivor}) and its own synthetic guests
 * ({@link GuestRosterTesting}, ids {@code test_d_*}), each bound by one test only and discarded when it ends, so no test
 * shares a guest or a player with another. A ceremony test builds its own small church: a stand-in plot over its own
 * structure ({@link GuestMilestoneTesting#church}) with a real {@code priest} townsperson in it. Anything that would wait
 * for real time (a lapsed request, a long ceremony) is aged through the seam, never by moving the world's clock; each
 * test runs synchronously in one tick, so no server tick steps in between its checks.
 */
public class GuestMilestoneGameTests {
	private static final String ONCE = GuestRosterTesting.guest("test_d_once", true);
	private static final String KEEP = GuestRosterTesting.guest("test_d_keep", false);
	private static final String MEAL = GuestRosterTesting.guest("test_d_meal", false);
	private static final String WED = GuestRosterTesting.guest("test_d_wed", true);
	private static final String HELD = GuestRosterTesting.guest("test_d_held", false);
	private static final String FAR = GuestRosterTesting.guest("test_d_far", true);
	private static final String ANNIV = GuestRosterTesting.guest("test_d_anniv", true);
	private static final String NEWS = GuestRosterTesting.guest("test_d_news", false);
	private static final String OUTING = GuestRosterTesting.guest("test_d_outing", false);
	private static final String DOOR = GuestRosterTesting.guest("test_d_door", true);
	private static final String OTHER = GuestRosterTesting.guest("test_d_other", false);
	/** The favors day pinned for a test's player: far from the overworld's own. */
	private static final long DAY = 5_000L;
	private static final int MAX = GuestBalance.MAX_AFFECTION;

	// ---------------------------------------------------------------- milestones

	@GameTest(maxTicks = 20)
	public void eachMilestoneFiresOnceAndSurvivesASave(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		String guest = ONCE;
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			Townsfolk npc = place(h, guest, 2, 3, made);
			GuestLedger ledger = GuestLedger.get(server);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(GuestFavors.GIFTS.getFirst().item(), 5));
			h.assertTrue(!GuestMilestoneTesting.reached(server, me, guest, "first_gift"), "A new player has reached nothing");

			h.assertTrue(GuestFavors.gift(player, npc, guest).done(), "The first gift is taken");
			h.assertTrue(GuestMilestoneTesting.reached(server, me, guest, "first_gift") && keepsakes(player, "first_gift") == 1,
					"GIFT_GIVEN fires first_gift and hands over exactly one letter");
			ItemStack letter = find(player, "first_gift");
			h.assertTrue(letter.is(Items.WRITTEN_BOOK) && GuestKeepsakes.toneOf(letter) == GuestKeepsakes.Tone.FRIENDSHIP,
					"The first-gift letter is a written book of the friendship set");
			h.assertTrue(GuestFavors.gift(player, npc, guest).done() && keepsakes(player, "first_gift") == 1,
					"A second gift brings no second letter");

			ledger.addAffection(me, guest, 500 - ledger.affection(me, guest));
			GuestMilestoneTesting.check(player, guest);
			h.assertTrue(GuestMilestoneTesting.reached(server, me, guest, "affection_250") && GuestMilestoneTesting.reached(server, me, guest, "affection_500")
					&& !GuestMilestoneTesting.reached(server, me, guest, "affection_750"), "Affection 500 reaches the 250 and 500 milestones, not 750");
			h.assertTrue(keepsakes(player, "affection_250") == 1 && keepsakes(player, "affection_500") == 1, "Each is rewarded once");
			GuestMilestoneTesting.check(player, guest);
			GuestMilestoneTesting.check(player, guest);
			h.assertTrue(keepsakes(player, "affection_250") == 1 && keepsakes(player, "affection_500") == 1 && keepsakes(player, "first_gift") == 1
					&& GuestMilestoneTesting.waiting(server, me).isEmpty(), "Checking again fires nothing and nothing waits");

			// A save and load: the record comes back, and nothing fires again.
			Tag saved = GuestMilestoneTesting.save(server);
			GuestMilestoneTesting.forget(server, me);
			h.assertTrue(!GuestMilestoneTesting.reached(server, me, guest, "first_gift"), "(the live record is gone)");
			GuestMilestoneTesting.load(server, saved, me);
			for (String key : List.of("first_gift", "affection_250", "affection_500")) {
				h.assertTrue(GuestMilestoneTesting.reached(server, me, guest, key), "Read back from the save: " + key);
			}
			h.assertTrue(GuestFavors.gift(player, npc, guest).done(), "A third gift is taken");
			GuestMilestoneTesting.check(player, guest);
			h.assertTrue(keepsakes(player, "first_gift") == 1 && keepsakes(player, "affection_250") == 1 && keepsakes(player, "affection_500") == 1
					&& GuestMilestoneTesting.waiting(server, me).isEmpty(), "After the load nothing fires or is handed over again");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	@GameTest(maxTicks = 20)
	public void keepsakesAreModestAndNeverLostWhenTheInventoryIsFull(GameTestHelper h) {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		String guest = KEEP;
		List<ItemEntity> dropped = new ArrayList<>();
		try {
			Inventory inventory = player.getInventory();
			for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
				inventory.setItem(slot, new ItemStack(Items.STONE, 64));
			}
			GuestLedger.get(server).addAffection(me, guest, 250);
			GuestMilestoneTesting.check(player, guest);
			h.assertTrue(GuestMilestoneTesting.reached(server, me, guest, "affection_250") && GuestMilestoneTesting.waiting(server, me).isEmpty(),
					"The milestone fires and its reward is handed over though the inventory is full");
			for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
				h.assertTrue(inventory.getItem(slot).is(Items.STONE) && inventory.getItem(slot).getCount() == 64, "The full inventory is untouched");
			}
			dropped.addAll(level.getEntitiesOfClass(ItemEntity.class, area(h).inflate(4.0),
					item -> item.isAlive() && "affection_250".equals(GuestKeepsakes.keyOf(item.getItem()))));
			h.assertTrue(dropped.size() == 1 && dropped.getFirst().getItem().getCount() == 1, "The keepsake lies at the player's feet: nothing is lost");
			GuestMilestoneTesting.check(player, guest);
			h.assertTrue(level.getEntitiesOfClass(ItemEntity.class, area(h).inflate(4.0),
					item -> item.isAlive() && "affection_250".equals(GuestKeepsakes.keyOf(item.getItem()))).size() == 1, "and it is given once");

			// Every friendship reward: one modest vanilla item, marked, named or titled, with lore; never taken as a gift,
			// never enchanted, never valuable, never something the town shops buy.
			for (String key : List.of("first_gift", "first_quest", "first_outing", "affection_250", "affection_500", "affection_750", "affection_1000")) {
				ItemStack reward = GuestMilestoneTesting.reward(player, guest, key);
				h.assertTrue(!reward.isEmpty() && reward.getCount() == 1 && GuestKeepsakes.ITEMS.contains(reward.getItem()), "A modest vanilla item: " + key);
				h.assertTrue(key.equals(GuestKeepsakes.keyOf(reward)) && GuestKeepsakes.toneOf(reward) == GuestKeepsakes.Tone.FRIENDSHIP
						&& reward.has(DataComponents.LORE) && (reward.has(DataComponents.CUSTOM_NAME) || reward.has(DataComponents.WRITTEN_BOOK_CONTENT)),
						"Marked, named or titled, with lore, of the friendship set: " + key);
				h.assertTrue(!reward.isEnchanted() && GuestMilestoneTesting.giftValue(guest, reward) == 0, "Never enchanted, never taken as a gift: " + key);
				for (TownShops.Shop shop : TownShops.get().shops.values()) {
					for (TownShops.Offer offer : shop.buys()) {
						h.assertTrue(offer.itemType() != reward.getItem(), "No town shop buys a keepsake: " + key + " at " + shop.id());
					}
				}
				for (ItemStack valuable : List.of(new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD), new ItemStack(Items.GOLD_INGOT),
						new ItemStack(Items.NETHERITE_INGOT), new ItemStack(Items.GOLDEN_APPLE))) {
					h.assertFalse(reward.is(valuable.getItem()), "Never a valuable item: " + key);
				}
			}
			h.succeed();
		} finally {
			for (ItemEntity item : dropped) {
				item.discard();
			}
		}
	}

	@GameTest(maxTicks = 20)
	public void firstOutingFiresWhenAnOutingEnds(GameTestHelper h) {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		String guest = OUTING;
		GuestActivity activity = GuestActivity.get(server);
		BlockPos at = h.absolutePos(new BlockPos(3, 2, 3));
		try {
			h.assertTrue(activity.begin(guest, new GuestActivity.Activity("outing", me, level.getGameTime(), level.dimension(), at)),
					"(an outing holds the test guest, as worker C's would)");
			GuestMilestoneTesting.observe(server);
			h.assertTrue(!GuestMilestoneTesting.reached(server, me, guest, "first_outing"), "An outing under way is no milestone yet");
			h.assertTrue(activity.end(guest, "outing"), "(the outing ends)");
			GuestMilestoneTesting.observe(server);
			h.assertTrue(GuestMilestoneTesting.reached(server, me, guest, "first_outing") && keepsakes(player, "first_outing") == 1,
					"When it ends, first_outing fires and its sketch is handed over");
			h.assertTrue(activity.begin(guest, new GuestActivity.Activity("outing", me, level.getGameTime() + 1, level.dimension(), at)),
					"(a second outing)");
			GuestMilestoneTesting.observe(server);
			activity.end(guest, "outing");
			GuestMilestoneTesting.observe(server);
			h.assertTrue(keepsakes(player, "first_outing") == 1, "A second outing brings no second keepsake");
			h.succeed();
		} finally {
			activity.end(guest, "outing");
		}
	}

	// ---------------------------------------------------------------- the meal

	@GameTest(maxTicks = 20)
	public void theSharedMealTakesOneFoodOnceADayWithinTheSharedBudget(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		String guest = MEAL;
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			Townsfolk npc = place(h, guest, 2, 3, made);
			GuestLedger ledger = GuestLedger.get(server);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 3));
			ItemStack bread = player.getMainHandItem();
			int left = GuestRewards.left(player, guest);
			h.assertTrue(left == GuestFavors.DAILY_AFFECTION_CAP && GuestMilestoneTesting.option(player, npc, guest, "meal"), "A meal is offered");

			GuestResult first = GuestMilestoneTesting.act(player, npc, guest, "meal");
			h.assertTrue(first.done(), "The meal is shared: " + first.message().getString());
			h.assertTrue(player.getMainHandItem() == bread && bread.getCount() == 2, "Exactly one bread leaves the hand stack");
			h.assertTrue(ledger.affection(me, guest) == GuestMilestoneMeals.GRANT && GuestRewards.left(player, guest) == left - GuestMilestoneMeals.GRANT,
					"It adds the meal's grant, counted against the shared budget");
			Long recorded = GuestMilestoneTesting.mealDay(server, me, guest);
			h.assertTrue(recorded != null && recorded == DAY, "The day is recorded");

			h.assertFalse(GuestMilestoneTesting.option(player, npc, guest, "meal"), "No second meal is offered today");
			h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, "meal").done(), "A second meal today is refused");
			h.assertTrue(bread.getCount() == 2 && ledger.affection(me, guest) == GuestMilestoneMeals.GRANT, "and takes and adds nothing");

			// The next day, with only 3 left of the shared budget: one bread, and exactly 3.
			GuestFavorTesting.pinDay(player, DAY + 1);
			GuestFavorTesting.gainedToday(player, guest, GuestFavors.DAILY_AFFECTION_CAP - 3);
			h.assertTrue(GuestRewards.left(player, guest) == 3, "(3 left of today's shared budget)");
			h.assertTrue(GuestMilestoneTesting.act(player, npc, guest, "meal").done() && bread.getCount() == 1
					&& ledger.affection(me, guest) == GuestMilestoneMeals.GRANT + 3 && GuestRewards.left(player, guest) == 0,
					"The grant is bounded by the shared budget");

			// A day with the budget used: refused, nothing taken.
			GuestFavorTesting.pinDay(player, DAY + 2);
			GuestFavorTesting.gainedToday(player, guest, GuestFavors.DAILY_AFFECTION_CAP);
			h.assertFalse(GuestMilestoneTesting.option(player, npc, guest, "meal"), "No meal is offered with nothing left to gain");
			h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, "meal").done(), "and none is shared");
			h.assertTrue(bread.getCount() == 1 && ledger.affection(me, guest) == GuestMilestoneMeals.GRANT + 3, "and nothing is taken for nothing");

			// Something that is no modest plain food.
			GuestFavorTesting.pinDay(player, DAY + 3);
			ItemStack named = new ItemStack(Items.BREAD, 2);
			named.set(DataComponents.CUSTOM_NAME, Component.literal("Heirloom loaf"));
			for (ItemStack wrong : List.of(new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.GOLDEN_CARROT, 2), named)) {
				player.setItemInHand(InteractionHand.MAIN_HAND, wrong);
				ItemStack held = player.getMainHandItem();
				h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, "meal").done(), "Not a meal: " + held);
				h.assertTrue(player.getMainHandItem() == held && held.getCount() == 2, "and nothing is taken: " + held);
			}
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	// ---------------------------------------------------------------- the ceremony

	@GameTest(maxTicks = 20)
	public void theCeremonyIsCosmeticOncePerMarriageAndTouchesNoBlock(GameTestHelper h) {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		String guest = WED;
		List<Townsfolk> made = new ArrayList<>();
		Townsfolk priest = priest(h, 6, 6);
		try {
			Townsfolk npc = place(h, guest, 2, 3, made);
			GuestMilestoneTesting.church(guest, level, area(h));
			GuestLedger ledger = GuestLedger.get(server);
			h.assertTrue("FOUND".equals(GuestMilestoneTesting.venue(server, guest)), "The test church and its priest are found");

			// Not married: refused.
			h.assertFalse(GuestMilestoneTesting.option(player, npc, guest, "ceremony"), "No ceremony is offered before a marriage");
			h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, "ceremony").done(), "and one is refused");

			marry(h, player, npc, guest);
			h.assertTrue(GuestMilestoneTesting.marriage(server, me).startsWith(guest + "#1@"), "MARRIED numbered the marriage: "
					+ GuestMilestoneTesting.marriage(server, me));
			Map<BlockPos, BlockState> before = snapshot(h);
			long playerCooldown = ledger.cooldownMillis(me), guestCooldown = ledger.cooldownMillis(guest);
			h.assertTrue(GuestMilestoneTesting.option(player, npc, guest, "ceremony"), "Married: a ceremony is offered");

			// Request, then cancel.
			h.assertTrue(GuestMilestoneTesting.act(player, npc, guest, "ceremony").done(), "A ceremony is asked for");
			h.assertTrue(GuestMilestoneTesting.option(player, npc, guest, "ceremony_confirm")
					&& GuestActivity.get(server).current(guest) == null, "It waits for confirmation, holding nothing");
			h.assertTrue(GuestMilestoneTesting.act(player, npc, guest, "ceremony_cancel").done(), "It is withdrawn");
			h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, "ceremony_confirm").done(), "A withdrawn request cannot be confirmed");

			// Request, then let it lapse.
			h.assertTrue(GuestMilestoneTesting.act(player, npc, guest, "ceremony").done(), "Asked again");
			GuestMilestoneTesting.age(me, GuestCeremonies.CONFIRM_TICKS + 1);
			h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, "ceremony_confirm").done(), "A lapsed request cannot be confirmed");
			h.assertTrue(GuestActivity.get(server).current(guest) == null, "and nothing holds the guest");

			// Confirmed, then cancelled while running.
			confirmed(h, player, npc, guest);
			h.assertTrue(GuestMilestoneTesting.act(player, npc, guest, "ceremony_cancel").done(), "A running ceremony can be cancelled");
			h.assertTrue(GuestActivity.get(server).current(guest) == null && GuestMilestoneTesting.running(me).isEmpty()
					&& !GuestMilestoneTesting.marriage(server, me).endsWith("+ceremony"), "Cancelled: the activity ends and nothing is recorded");

			// Confirmed, then timed out.
			confirmed(h, player, npc, guest);
			GuestMilestoneTesting.age(me, GuestCeremonies.MAX_TICKS + 1);
			GuestMilestoneTesting.step(server, me);
			h.assertTrue(GuestActivity.get(server).current(guest) == null && GuestMilestoneTesting.running(me).isEmpty()
					&& !GuestMilestoneTesting.marriage(server, me).endsWith("+ceremony"), "Timed out: the activity ends and nothing is recorded");

			// Confirmed and held.
			confirmed(h, player, npc, guest);
			GuestMilestoneTesting.step(server, me);
			h.assertTrue(GuestMilestoneTesting.line(me) == 1, "The guest is at the church and the priest has begun: line "
					+ GuestMilestoneTesting.line(me));
			GuestMilestoneTesting.age(me, (long) GuestCeremonies.LINES * GuestCeremonies.LINE_TICKS);
			GuestMilestoneTesting.step(server, me);
			h.assertTrue(GuestMilestoneTesting.running(me).isEmpty() && GuestActivity.get(server).current(guest) == null,
					"The rite is over and the guest is free to go back to its post");
			h.assertTrue(GuestMilestoneTesting.marriage(server, me).endsWith("+ceremony")
					&& GuestMilestoneTesting.reached(server, me, guest, "ceremony_1"), "The ceremony is recorded for this marriage");
			ItemStack bouquet = find(player, "ceremony_1");
			h.assertTrue(bouquet.is(Items.LILY_OF_THE_VALLEY) && keepsakes(player, "ceremony_1") == 1
					&& GuestKeepsakes.toneOf(bouquet) == GuestKeepsakes.Tone.ROMANCE, "Its bouquet is handed over once");

			// Once per marriage.
			h.assertFalse(GuestMilestoneTesting.option(player, npc, guest, "ceremony"), "No second ceremony is offered for this marriage");
			h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, "ceremony").done(), "and one is refused");

			// The ledger and the church are untouched.
			h.assertTrue(guest.equals(ledger.spouseOf(me)) && me.equals(ledger.spouseOf(guest)) && ledger.cooldownMillis(me) == playerCooldown
					&& ledger.cooldownMillis(guest) == guestCooldown, "No marriage transaction: spouses and cooldowns are as they were");
			h.assertTrue(snapshot(h).equals(before), "No block of the church changed");

			// A divorce ends eligibility; a new marriage brings a new one.
			h.assertTrue(GuestMarriage.requestDivorce(player, guest).done() && GuestMarriage.confirmDivorce(player, guest).done(), "(divorced)");
			h.assertTrue("none".equals(GuestMilestoneTesting.marriage(server, me)), "DIVORCED cleared the marriage record");
			h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, "ceremony").done(), "Divorced: no ceremony");
			GuestMilestoneTesting.clearCooldowns(server, me, guest);
			marry(h, player, npc, guest);
			h.assertTrue(GuestMilestoneTesting.marriage(server, me).startsWith(guest + "#2@"), "The new marriage is numbered 2");
			h.assertTrue(GuestMilestoneTesting.option(player, npc, guest, "ceremony")
					&& GuestMilestoneTesting.act(player, npc, guest, "ceremony").done(), "A new marriage may hold its own ceremony");
			h.assertTrue(GuestMilestoneTesting.act(player, npc, guest, "ceremony_cancel").done(), "(withdrawn)");
			h.succeed();
		} finally {
			GuestMilestoneTesting.endCeremony(server, me);
			GuestMilestoneTesting.forgetChurch(guest);
			clear(made);
			priest.discard();
		}
	}

	@GameTest(maxTicks = 20)
	public void theCeremonyIsRefusedForHeldGuestsAndAnUnloadedChurch(GameTestHelper h) {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		ServerPlayer player = visitor(h, 3, 3);
		ServerPlayer couple = visitor(h, 4, 4);
		UUID me = player.getUUID();
		List<Townsfolk> made = new ArrayList<>();
		try {
			GuestLedger ledger = GuestLedger.get(server);

			// A guest held out of romance, at full affection: no marriage, no ceremony, no romantic letter.
			Townsfolk held = place(h, HELD, 2, 3, made);
			ledger.addAffection(me, HELD, MAX);
			h.assertTrue(!GuestRoster.get(HELD).mayMarry() && ledger.canMarry(me, HELD) == GuestLedger.Refusal.NO_MARRIAGE
					&& !GuestMarriage.mayPropose(player, HELD), "(the ledger cannot marry a held guest, even at full affection)");
			GuestMilestoneTesting.church(HELD, level, area(h));
			Townsfolk priest = priest(h, 6, 6);
			try {
				h.assertFalse(GuestMilestoneTesting.option(player, held, HELD, "ceremony"), "No ceremony is offered for a held guest");
				h.assertFalse(GuestMilestoneTesting.act(player, held, HELD, "ceremony").done(), "and one is refused");
				h.assertFalse(GuestMilestoneTesting.option(player, held, HELD, "ceremony_confirm"), "Nothing waits to be confirmed");
				h.assertTrue(GuestActivity.get(server).current(HELD) == null, "and nothing holds the guest");
			} finally {
				priest.discard();
				GuestMilestoneTesting.forgetChurch(HELD);
			}
			for (String key : List.of("ceremony_1", "anniversary_1_7", "anniversary_1_365")) {
				h.assertTrue(GuestMilestoneTesting.reward(player, HELD, key).isEmpty(), "No romantic keepsake or letter for a held guest: " + key);
			}
			GuestMilestoneTesting.anniversaries(player, GuestMilestoneTesting.day(server) + 400);
			h.assertTrue("none".equals(GuestMilestoneTesting.marriage(server, me))
					&& GuestMilestoneTesting.waiting(server, me).stream().noneMatch(w -> w.contains("anniversary")), "No anniversary without a marriage");
			GuestMilestoneTesting.check(player, HELD);
			h.assertTrue(keepsakes(player, "affection_500") == 1 && keepsakes(player, "affection_1000") == 1,
					"A held guest's friendship milestones are rewarded");
			Inventory inventory = player.getInventory();
			for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
				ItemStack stack = inventory.getItem(slot);
				h.assertTrue(GuestKeepsakes.toneOf(stack) != GuestKeepsakes.Tone.ROMANCE, "Only the friendship set for a held guest: " + stack);
			}

			// A marriageable guest married to the couple, but no church loaded, then no priest.
			Townsfolk far = place(h, FAR, 5, 2, made);
			marry(h, couple, far, FAR);
			BlockPos away = h.absolutePos(new BlockPos(40_000, 0, 40_000));
			int cx = away.getX() >> 4, cz = away.getZ() >> 4;
			h.assertFalse(level.hasChunk(cx, cz), "(the far church's chunk is not loaded)");
			GuestMilestoneTesting.church(FAR, level, new AABB(away.getX(), away.getY(), away.getZ(), away.getX() + 30, away.getY() + 20, away.getZ() + 30));
			h.assertTrue("NOT_LOADED".equals(GuestMilestoneTesting.venue(server, FAR)), "An unloaded church is found unloaded");
			GuestResult unloaded = GuestMilestoneTesting.act(couple, far, FAR, "ceremony");
			h.assertTrue(!unloaded.done() && unloaded.message().getString().contains("not loaded"), "and refused with a clear message: "
					+ unloaded.message().getString());
			h.assertFalse(level.hasChunk(cx, cz), "Asking loaded no chunk");
			GuestMilestoneTesting.church(FAR, level, area(h));
			h.assertTrue("NO_PRIEST".equals(GuestMilestoneTesting.venue(server, FAR)), "A church without its priest is found priestless");
			GuestResult priestless = GuestMilestoneTesting.act(couple, far, FAR, "ceremony");
			h.assertTrue(!priestless.done() && priestless.message().getString().contains("Priest of Notch"), "and refused with a clear message: "
					+ priestless.message().getString());
			h.assertTrue(FAR.equals(ledger.spouseOf(couple.getUUID())) && GuestActivity.get(server).current(FAR) == null,
					"The marriage stands and nothing holds the guest");
			h.succeed();
		} finally {
			GuestMilestoneTesting.forgetChurch(FAR);
			GuestMilestoneTesting.forgetChurch(HELD);
			GuestMilestoneTesting.endCeremony(server, couple.getUUID());
			clear(made);
		}
	}

	@GameTest(maxTicks = 20)
	public void anniversariesAreRomanceOnlyAndFireOnce(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 3, 3);
		ServerPlayer stranger = visitor(h, 4, 4);
		UUID me = player.getUUID();
		String guest = ANNIV;
		List<Townsfolk> made = new ArrayList<>();
		try {
			Townsfolk npc = place(h, guest, 2, 3, made);
			h.assertTrue(GuestMilestoneTesting.reward(player, guest, "anniversary_1_7").isEmpty(), "No romantic letter before a marriage");
			marry(h, player, npc, guest);
			long since = GuestMilestoneTesting.day(server);
			h.assertTrue(GuestMilestoneTesting.marriage(server, me).equals(guest + "#1@" + since), "(married today)");
			h.assertTrue(GuestMilestoneTesting.reward(stranger, guest, "anniversary_1_7").isEmpty(), "Nor for a player not married to the guest");
			ItemStack romantic = GuestMilestoneTesting.reward(player, guest, "anniversary_1_7");
			h.assertTrue(romantic.is(Items.WRITTEN_BOOK) && GuestKeepsakes.toneOf(romantic) == GuestKeepsakes.Tone.ROMANCE,
					"A married adult guest's anniversary letter is of the romance set");

			GuestMilestoneTesting.anniversaries(player, since + 6);
			h.assertTrue(keepsakes(player, "anniversary_1_7") == 0, "Not before seven days");
			GuestMilestoneTesting.anniversaries(player, since + 7);
			GuestMilestoneTesting.anniversaries(player, since + 7);
			h.assertTrue(keepsakes(player, "anniversary_1_7") == 1, "The seventh day's letter comes once");
			GuestMilestoneTesting.anniversaries(player, since + 400);
			h.assertTrue(keepsakes(player, "anniversary_1_365") == 1 && !GuestMilestoneTesting.reached(server, me, guest, "anniversary_1_30")
					&& !GuestMilestoneTesting.reached(server, me, guest, "anniversary_1_100"), "A long gap brings only the latest anniversary");
			h.assertTrue(GuestMilestoneTesting.due(6) == 0 && GuestMilestoneTesting.due(29) == 7 && GuestMilestoneTesting.due(364) == 100
					&& GuestMilestoneTesting.due(729) == 365 && GuestMilestoneTesting.due(800) == 730 && GuestMilestoneTesting.due(1100) == 1095,
					"The anniversaries: 7, 30, 100, 365, then every 365 days");

			h.assertTrue(GuestMarriage.requestDivorce(player, guest).done() && GuestMarriage.confirmDivorce(player, guest).done(), "(divorced)");
			GuestMilestoneTesting.anniversaries(player, since + 800);
			h.assertTrue(!GuestMilestoneTesting.reached(server, me, guest, "anniversary_1_730")
					&& GuestMilestoneTesting.reward(player, guest, "anniversary_1_7").isEmpty(), "No anniversaries or romantic letters after a divorce");
			h.succeed();
		} finally {
			clear(made);
		}
	}

	// ---------------------------------------------------------------- notices

	@GameTest(maxTicks = 20)
	public void noticesAreOffByDefaultAndNameOnlyPlayersWhoShare(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer subject = visitor(h, 3, 3);
		ServerPlayer reader = visitor(h, 3, 4);
		ServerPlayer quiet = visitor(h, 4, 3);
		UUID a = subject.getUUID(), b = reader.getUUID(), c = quiet.getUUID();
		String guest = NEWS;
		List<Townsfolk> made = new ArrayList<>();
		List<UUID> everyone = List.of(a, b, c);
		try {
			Townsfolk npc = place(h, guest, 2, 2, made);
			for (UUID player : everyone) {
				h.assertTrue(!GuestMilestoneTesting.receives(server, player) && !GuestMilestoneTesting.shares(server, player),
						"Notices and sharing are off by default");
				GuestMilestoneTesting.record(player);
			}
			h.assertTrue(GuestMilestoneTesting.option(reader, npc, guest, "notices_toggle")
					&& GuestMilestoneTesting.option(subject, npc, guest, "notices_share"), "Both settings are offered");

			h.assertTrue(GuestMilestoneTesting.act(reader, npc, guest, "notices_toggle").done(), "The reader turns notices on");
			h.assertTrue(GuestMilestoneTesting.receives(server, b) && !GuestMilestoneTesting.shares(server, b), "receiving only, not sharing");

			// The subject has not opted in: nobody hears of their milestone.
			h.assertTrue(GuestMilestoneTesting.reach(server, a, guest, "affection_250"), "(the subject reaches a milestone)");
			h.assertTrue(GuestMilestoneTesting.announce(server, a, guest, "first_gift").isEmpty(), "An unshared milestone is announced to nobody");
			for (UUID player : everyone) {
				h.assertTrue(GuestMilestoneTesting.recorded(player).isEmpty(), "and nobody was sent a notice naming the subject");
			}

			// The subject opts in to sharing: only the receiving reader hears, and the notice names only the subject.
			h.assertTrue(GuestMilestoneTesting.act(subject, npc, guest, "notices_share").done(), "The subject shares");
			h.assertTrue(GuestMilestoneTesting.shares(server, a) && !GuestMilestoneTesting.receives(server, a), "sharing only, not receiving");
			List<UUID> reached = GuestMilestoneTesting.announce(server, a, guest, "affection_500");
			h.assertTrue(reached.contains(b) && !reached.contains(c) && !reached.contains(a), "Only the opted-in reader hears: " + reached);
			h.assertTrue(GuestMilestoneTesting.recorded(b).size() == 1 && GuestMilestoneTesting.recorded(b).getFirst().named().equals(Set.of(a))
					&& GuestMilestoneTesting.recorded(c).isEmpty() && GuestMilestoneTesting.recorded(a).isEmpty(), "One notice, to the reader, naming the subject");
			h.assertTrue(GuestMilestoneTesting.unshared(server, everyone).isEmpty(), "Every notice sent names only players who share");

			// Gossip names no player; a real-person guest only gets the town-life lines.
			for (int pick = 0; pick < 10; pick++) {
				String line = GuestMilestoneTesting.gossip(guest, pick).getString();
				h.assertTrue(!line.isEmpty() && !line.contains(subject.getName().getString()), "Gossip names no player: " + line);
			}
			for (String key : GuestMilestoneTesting.gossipKeys("emiru")) {
				h.assertTrue(key.startsWith("message.jugcraft.guest.gossip.town."), "A real-person guest gets only generic town-life gossip: " + key);
			}
			h.assertTrue(GuestMilestoneTesting.gossipKeys(guest).size() > GuestMilestoneTesting.gossipKeys("emiru").size(),
					"(fictional guests also get the light town gossip)");

			// The subject stops sharing: nothing more is announced.
			h.assertTrue(GuestMilestoneTesting.act(subject, npc, guest, "notices_share").done() && !GuestMilestoneTesting.shares(server, a),
					"The subject stops sharing");
			h.assertTrue(GuestMilestoneTesting.announce(server, a, guest, "affection_750").isEmpty()
					&& GuestMilestoneTesting.recorded(b).size() == 1, "and nothing more names them");

			// The reader turns notices off: nothing reaches them.
			h.assertTrue(GuestMilestoneTesting.act(reader, npc, guest, "notices_toggle").done() && !GuestMilestoneTesting.receives(server, b),
					"The reader turns notices off");
			h.assertTrue(GuestMilestoneTesting.act(subject, npc, guest, "notices_share").done(), "(the subject shares again)");
			h.assertTrue(GuestMilestoneTesting.announce(server, a, guest, "affection_1000").stream().noneMatch(everyone::contains)
					&& GuestMilestoneTesting.recorded(b).size() == 1, "Nobody with notices off is sent one");
			h.succeed();
		} finally {
			for (UUID player : everyone) {
				GuestMilestoneTesting.stopRecording(player);
			}
			clear(made);
		}
	}

	// ---------------------------------------------------------------- entry points

	@GameTest(maxTicks = 20)
	public void everyEntryPointChecksForItself(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 7, 7);
		UUID me = player.getUUID();
		String guest = DOOR;
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			h.assertTrue(GuestMilestoneTesting.registered(), "The milestones feature is registered with its six options");
			Townsfolk npc = place(h, guest, 0, 0, made);
			GuestLedger ledger = GuestLedger.get(server);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 4));
			ItemStack bread = player.getMainHandItem();

			// Too far away (the menu's range, checked again here).
			h.assertTrue(npc.getBoundingBox().distanceToSqr(player.getEyePosition()) > GuestBalance.INTERACT_RANGE * GuestBalance.INTERACT_RANGE,
					"(the player stands out of reach)");
			refusedAll(h, player, npc, guest, bread, "out of reach");

			// Beside it, but a spectator.
			Vec3 near = centre(h, 1, 1);
			player.snapTo(near.x, near.y, near.z, 0.0F, 0.0F);
			player.setGameMode(GameType.SPECTATOR);
			refusedAll(h, player, npc, guest, bread, "a spectator");
			player.setGameMode(GameType.SURVIVAL);

			// Beside it, asking as another guest, or beside a copy that is not the canonical entity.
			refusedAll(h, player, npc, OTHER, bread, "asked as another guest");
			Townsfolk copy = stand(h, guest, 2, 0, made);
			h.assertTrue(!copy.getUUID().equals(ledger.canonical(guest)), "(the copy is not the canonical entity)");
			refusedAll(h, player, copy, guest, bread, "beside a copy");
			h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, "no_such_option").done(), "An unknown option is refused");

			// And beside the canonical guest, in survival, it works.
			GuestResult meal = GuestMilestoneTesting.act(player, npc, guest, "meal");
			h.assertTrue(meal.done() && bread.getCount() == 3, "Beside the canonical guest the meal is shared: " + meal.message().getString());
			h.succeed();
		} finally {
			GuestMilestoneTesting.endCeremony(server, me);
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	/** Every option that acts on the guest is refused from where the player stands, and nothing changes. */
	private static void refusedAll(GameTestHelper h, ServerPlayer player, Townsfolk npc, String guest, ItemStack bread, String why) {
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		int count = bread.getCount();
		boolean receives = GuestMilestoneTesting.receives(server, me), shares = GuestMilestoneTesting.shares(server, me);
		for (String option : List.of("meal", "ceremony", "ceremony_confirm", "notices_toggle", "notices_share")) {
			h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, option).done(), "Refused " + why + ": " + option);
		}
		h.assertTrue(bread.getCount() == count && GuestMilestoneTesting.mealDay(server, me, DOOR) == null, "Nothing eaten " + why);
		h.assertTrue(GuestMilestoneTesting.receives(server, me) == receives && GuestMilestoneTesting.shares(server, me) == shares
				&& GuestMilestoneTesting.running(me).isEmpty() && GuestActivity.get(server).current(DOOR) == null, "Nothing else changed " + why);
	}

	// ---------------------------------------------------------------- helpers

	/** Asks for a ceremony and confirms it: the ceremony activity then holds the guest. */
	private static void confirmed(GameTestHelper h, ServerPlayer player, Townsfolk npc, String guest) {
		MinecraftServer server = player.level().getServer();
		GuestResult asked = GuestMilestoneTesting.act(player, npc, guest, "ceremony");
		h.assertTrue(asked.done(), "A ceremony is asked for: " + asked.message().getString());
		GuestResult confirmed = GuestMilestoneTesting.act(player, npc, guest, "ceremony_confirm");
		h.assertTrue(confirmed.done(), "and confirmed: " + confirmed.message().getString());
		GuestActivity.Activity held = GuestActivity.get(server).current(guest);
		h.assertTrue(held != null && GuestCeremonies.KIND.equals(held.kind()) && player.getUUID().equals(held.player())
				&& guest.equals(GuestMilestoneTesting.running(player.getUUID())), "The ceremony activity holds the guest");
	}

	/** Marries the player and the guest the way players do: full affection, a diamond, a proposal asked and confirmed. */
	private static void marry(GameTestHelper h, ServerPlayer player, Townsfolk npc, String guest) {
		GuestLedger ledger = GuestLedger.get(h.getLevel().getServer());
		ledger.addAffection(player.getUUID(), guest, MAX);
		player.getInventory().add(new ItemStack(GuestBalance.PROPOSAL_ITEM, GuestBalance.PROPOSAL_COUNT));
		GuestResult asked = GuestMarriage.requestProposal(player, npc, guest);
		h.assertTrue(asked.done(), "(a proposal: " + asked.message().getString() + ")");
		GuestResult wed = GuestMarriage.confirmProposal(player, npc, guest);
		h.assertTrue(wed.done() && guest.equals(ledger.spouseOf(player.getUUID())), "(married: " + wed.message().getString() + ")");
	}

	/** The town's priest, as a real {@code priest} townsperson, standing still at (x, 2, z) of the test. */
	private static Townsfolk priest(GameTestHelper h, int x, int z) {
		Townsfolk priest = h.spawnWithNoFreeWill(JugcraftTown.TOWNSFOLK, new Vec3(x + 0.5, 2, z + 0.5));
		priest.setUp(new TownData.Spot(-1, "priest", null, "Test Priest of Notch", "priest_1", BlockPos.ZERO), priest.blockPosition());
		h.assertTrue("priest".equals(priest.role()) && priest.custom() == null && priest.guest() == null, "(the test's priest)");
		return priest;
	}

	/** The test's own 8 by 8 by 8 blocks (its floor, its players, its guests and its church), in world coordinates. */
	private static AABB area(GameTestHelper h) {
		BlockPos a = h.absolutePos(BlockPos.ZERO), b = h.absolutePos(new BlockPos(7, 7, 7));
		return new AABB(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()),
				Math.max(a.getX(), b.getX()) + 1, Math.max(a.getY(), b.getY()) + 1, Math.max(a.getZ(), b.getZ()) + 1);
	}

	/** Every block of the test's area. */
	private static Map<BlockPos, BlockState> snapshot(GameTestHelper h) {
		Map<BlockPos, BlockState> out = new HashMap<>();
		AABB box = area(h);
		for (BlockPos at : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ),
				BlockPos.containing(box.maxX - 1, box.maxY - 1, box.maxZ - 1))) {
			out.put(at.immutable(), h.getLevel().getBlockState(at));
		}
		return out;
	}

	/** How many of the player's items are the reward of milestone {@code key}. */
	private static int keepsakes(ServerPlayer player, String key) {
		Inventory inventory = player.getInventory();
		int count = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (key.equals(GuestKeepsakes.keyOf(stack))) {
				count += stack.getCount();
			}
		}
		return count;
	}

	private static ItemStack find(ServerPlayer player, String key) {
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (key.equals(GuestKeepsakes.keyOf(inventory.getItem(slot)))) {
				return inventory.getItem(slot);
			}
		}
		return ItemStack.EMPTY;
	}

	/** A real survival player standing on the floor at (x, 2, z) of the test. */
	private static ServerPlayer visitor(GameTestHelper h, int x, int z) {
		ServerPlayer player = TestPlayers.survivor(h);
		Vec3 at = centre(h, x, z);
		player.snapTo(at.x, at.y, at.z, 90.0F, 0.0F);
		return player;
	}

	/** The guest {@code guest} placed at (x, 2, z) of the test and bound in the ledger as its canonical entity. */
	private static Townsfolk place(GameTestHelper h, String guest, int x, int z, List<Townsfolk> made) {
		Townsfolk npc = stand(h, guest, x, z, made);
		GuestLedger ledger = GuestLedger.get(h.getLevel().getServer());
		boolean bound = ledger.bind(guest, npc.getUUID()) && npc.getUUID().equals(ledger.canonical(guest));
		if (!bound) {
			npc.discard();   // never left behind to claim the identity later
		}
		h.assertTrue(bound, "The test's " + guest + " is its canonical entity");
		return npc;
	}

	/** An entity carrying the guest id {@code guest} at (x, 2, z) of the test, not bound in the ledger. */
	private static Townsfolk stand(GameTestHelper h, String guest, int x, int z, List<Townsfolk> made) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		Townsfolk npc = CustomNpcs.create(h.getLevel(), centre(h, x, z), 0.0F, entry != null ? entry.name() : guest, CustomNpcs.NPC, null, guest);
		h.assertTrue(npc != null && guest.equals(npc.guest()), "An entity for " + guest + " stands in the test");
		made.add(npc);
		return npc;
	}

	/** Discards the test's guests (each canonical one is then lost in the ledger, free for the next binding). */
	private static void clear(List<Townsfolk> made) {
		for (Townsfolk npc : made) {
			if (!npc.isRemoved()) {
				npc.discard();
			}
		}
		made.clear();
	}

	private static Vec3 centre(GameTestHelper h, int x, int z) {
		BlockPos at = h.absolutePos(new BlockPos(x, 2, z));
		return new Vec3(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
	}

	private static void floor(GameTestHelper h) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}
}
