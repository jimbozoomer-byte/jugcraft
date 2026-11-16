package io.github.jimbozoomer.jugcraft.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import io.github.jimbozoomer.jugcraft.town.guests.GuestFeature;
import io.github.jimbozoomer.jugcraft.town.guests.GuestKeepsakes;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedger;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedgerTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestMarriage;
import io.github.jimbozoomer.jugcraft.town.guests.GuestMilestoneMeals;
import io.github.jimbozoomer.jugcraft.town.guests.GuestMilestoneTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestNotices;
import io.github.jimbozoomer.jugcraft.town.guests.GuestOutingTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestResult;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRewards;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRivalries;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRoster;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRosterTesting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.CommandSourceStack;
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
	private static final String RIVAL = GuestRosterTesting.guest("test_d_rival", false);
	private static final String RESTART = GuestRosterTesting.guest("test_d_restart", true);
	private static final String RITE = GuestRosterTesting.guest("test_d_rite", true);
	private static final String BUSY_WED = GuestRosterTesting.guest("test_d_busy_wed", true);
	private static final String BUSY_OTHER = GuestRosterTesting.guest("test_d_busy_other", false);
	private static final String FEAST = GuestRosterTesting.guest("test_d_feast", true);
	private static final String SINGLE = GuestRosterTesting.guest("test_d_single", true);
	private static final String SPOUSE = GuestRosterTesting.guest("test_d_spouse", true);
	private static final String FEAST_HELD = GuestRosterTesting.guest("test_d_feast_held", false);
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

	/**
	 * first_outing comes from the outing's own ending ({@code ACTIVITY_ENDED}, completed), at once and with no polling: an
	 * outing that begins and completes within the same tick fires it exactly once, and a later outing never again. A
	 * cancelled outing and one that runs its time without earning its reward never fire it.
	 */
	@GameTest(maxTicks = 20)
	public void firstOutingFiresOnceWhenAnOutingCompletesInTheSameTick(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 3, 3);
		ServerPlayer other = visitor(h, 3, 2);
		UUID me = player.getUUID(), them = other.getUUID();
		String guest = OUTING;
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		GuestFavorTesting.pinDay(other, DAY);
		GuestMilestoneTesting.recordActivities(guest);
		try {
			Townsfolk npc = place(h, guest, 1, 1, made);
			GuestFeature outings = GuestOutingTesting.feature();

			// Cancelled: never a first outing.
			h.assertTrue(GuestOutingTesting.invite(other, npc, guest, "fair", 3).done(), "(another player's fair outing begins)");
			h.assertTrue(outings.act(other, npc, guest, "end_outing").done(), "(and is cancelled)");
			h.assertTrue(!GuestMilestoneTesting.reached(server, them, guest, "first_outing") && keepsakes(other, "first_outing") == 0,
					"A cancelled outing is no first outing");

			// Run its time away from the guest, so it earns nothing: never a first outing.
			h.assertTrue(GuestOutingTesting.invite(other, npc, guest, "fair", 3).done(), "(it is tried again)");
			Vec3 aside = centre(h, 3, 2).add(20, 0, 0);
			other.snapTo(aside.x, aside.y, aside.z, 0.0F, 0.0F);
			GuestOutingTesting.step(server, guest, GuestOutingTesting.now(server) + GuestOutingTesting.duration("fair"));
			h.assertTrue(GuestActivity.get(server).current(guest) == null && !GuestOutingTesting.done(other, guest, "fair"),
					"(it ran its time and earned nothing)");
			h.assertTrue(!GuestMilestoneTesting.reached(server, them, guest, "first_outing") && keepsakes(other, "first_outing") == 0,
					"An outing short of its reward is no first outing");
			h.assertTrue(GuestMilestoneTesting.activityEvents(guest).equals(List.of("began:outing", "ended:outing:false", "began:outing",
					"ended:outing:false")), "Each began once and ended not completed: " + GuestMilestoneTesting.activityEvents(guest));

			// Begun and completed within this one tick: first_outing fires at once, from the completion itself.
			h.assertTrue(GuestOutingTesting.invite(player, npc, guest, "fair", 3).done(), "A fair outing begins");
			GuestOutingTesting.step(server, guest, GuestOutingTesting.now(server) + GuestOutingTesting.duration("fair"));
			h.assertTrue(GuestActivity.get(server).current(guest) == null && GuestOutingTesting.done(player, guest, "fair"),
					"and completes, rewarded, in the same tick");
			h.assertTrue(GuestMilestoneTesting.reached(server, me, guest, "first_outing") && keepsakes(player, "first_outing") == 1,
					"first_outing fires at once, with no poll in between, and its keepsake is handed over");

			// A second completed outing: no second keepsake.
			h.assertTrue(GuestOutingTesting.invite(player, npc, guest, "board_game", 3).done(), "A board game begins");
			GuestOutingTesting.step(server, guest, GuestOutingTesting.now(server) + GuestOutingTesting.duration("board_game"));
			h.assertTrue(GuestOutingTesting.done(player, guest, "board_game") && keepsakes(player, "first_outing") == 1,
					"A second completed outing brings no second keepsake");
			h.assertTrue(GuestMilestoneTesting.activityEvents(guest).equals(List.of("began:outing", "ended:outing:false", "began:outing",
					"ended:outing:false", "began:outing", "ended:outing:true", "began:outing", "ended:outing:true")),
					"Each outing fired exactly one beginning and one ending, completed only when rewarded: "
							+ GuestMilestoneTesting.activityEvents(guest));
			h.assertTrue(!GuestMilestoneTesting.reached(server, them, guest, "first_outing"), "The other player still has no first outing");
			h.succeed();
		} finally {
			GuestMilestoneTesting.stopRecordingActivities(guest);
			clear(made);
			GuestFavorTesting.unpin(me);
			GuestFavorTesting.unpin(them);
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

	/**
	 * The owner's 12 November exception: a couple the ledger already has married, at full affection, still shares one meal
	 * a day. Exactly one food is eaten, the day is recorded, no affection is added and the player is told so; a second
	 * meal that day is refused; a divorce ends the exception.
	 */
	@GameTest(maxTicks = 20)
	public void aMarriedCoupleAtFullAffectionSharesAMealForNoAffection(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		String guest = FEAST;
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			Townsfolk npc = place(h, guest, 2, 3, made);
			GuestLedger ledger = GuestLedger.get(server);
			marry(h, player, npc, guest);
			h.assertTrue(ledger.affection(me, guest) == MAX && !ledger.canGainAffection(me, guest), "(married, at full affection)");
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 3));
			ItemStack bread = player.getMainHandItem();
			int left = GuestRewards.left(player, guest);
			h.assertTrue(GuestMilestoneTesting.option(player, npc, guest, "meal"), "A married couple at full affection is offered a meal");

			GuestResult shared = GuestMilestoneTesting.act(player, npc, guest, "meal");
			h.assertTrue(shared.done() && shared.message().getString().contains("no affection"),
					"It is shared, and says plainly that no affection is gained: " + shared.message().getString());
			h.assertTrue(player.getMainHandItem() == bread && bread.getCount() == 2, "Exactly one food is eaten, from the hand's stack");
			h.assertTrue(ledger.affection(me, guest) == MAX && GuestRewards.left(player, guest) == left,
					"No affection is added and the shared budget is untouched");
			Long recorded = GuestMilestoneTesting.mealDay(server, me, guest);
			h.assertTrue(recorded != null && recorded == DAY, "The day is recorded");

			h.assertFalse(GuestMilestoneTesting.option(player, npc, guest, "meal"), "No second meal is offered today");
			h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, "meal").done(), "A second meal today is refused");
			h.assertTrue(bread.getCount() == 2 && ledger.affection(me, guest) == MAX, "and takes nothing");

			GuestFavorTesting.pinDay(player, DAY + 1);
			h.assertTrue(GuestMilestoneTesting.option(player, npc, guest, "meal") && GuestMilestoneTesting.act(player, npc, guest, "meal").done()
					&& bread.getCount() == 1 && ledger.affection(me, guest) == MAX, "The next day: one more meal, one more food, still no affection");

			h.assertTrue(GuestMarriage.requestDivorce(player, guest).done() && GuestMarriage.confirmDivorce(player, guest).done(), "(divorced)");
			GuestFavorTesting.pinDay(player, DAY + 2);
			h.assertTrue(ledger.affection(me, guest) == MAX && ledger.spouseOf(me) == null, "(still at full affection, no longer married)");
			h.assertFalse(GuestMilestoneTesting.option(player, npc, guest, "meal"), "Divorced at full affection: no meal is offered");
			h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, "meal").done(), "and none is shared");
			Long last = GuestMilestoneTesting.mealDay(server, me, guest);
			h.assertTrue(bread.getCount() == 1 && last != null && last == DAY + 1, "and nothing is taken or recorded for nothing");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	/**
	 * The exception is for a married couple only: at full affection an unmarried marriage-eligible guest, a guest while
	 * the player is married to someone else, and a held guest (whom the ledger can never marry) are all refused, and no
	 * food is taken.
	 */
	@GameTest(maxTicks = 20)
	public void aMealIsNeverTakenForNothingWithAnUnmarriedGuestAtFullAffection(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			GuestLedger ledger = GuestLedger.get(server);
			Townsfolk single = place(h, SINGLE, 2, 3, made);
			Townsfolk held = place(h, FEAST_HELD, 4, 3, made);
			ledger.addAffection(me, SINGLE, MAX);
			ledger.addAffection(me, FEAST_HELD, MAX);
			h.assertTrue(!GuestRoster.get(FEAST_HELD).mayMarry() && ledger.canMarry(me, FEAST_HELD) == GuestLedger.Refusal.NO_MARRIAGE,
					"(the held guest can never be married)");
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 3));
			ItemStack bread = player.getMainHandItem();

			unmarriedRefused(h, player, single, SINGLE, bread, "an unmarried guest at full affection");
			unmarriedRefused(h, player, held, FEAST_HELD, bread, "a held guest at full affection");

			Townsfolk spouse = place(h, SPOUSE, 3, 2, made);
			marry(h, player, spouse, SPOUSE);
			h.assertTrue(SPOUSE.equals(ledger.spouseOf(me)), "(the player is married to another guest)");
			unmarriedRefused(h, player, single, SINGLE, bread, "a guest at full affection while married to another");
			unmarriedRefused(h, player, held, FEAST_HELD, bread, "a held guest at full affection while married to another");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	/** No meal is offered or shared with {@code guest}, no food is taken, nothing is recorded, and affection stays full. */
	private static void unmarriedRefused(GameTestHelper h, ServerPlayer player, Townsfolk npc, String guest, ItemStack bread, String why) {
		MinecraftServer server = player.level().getServer();
		int count = bread.getCount();
		h.assertFalse(GuestMilestoneTesting.option(player, npc, guest, "meal"), "No meal is offered with " + why);
		GuestResult refused = GuestMilestoneTesting.act(player, npc, guest, "meal");
		h.assertFalse(refused.done(), "No meal is shared with " + why + ": " + refused.message().getString());
		h.assertTrue(player.getMainHandItem() == bread && bread.getCount() == count && GuestMilestoneTesting.mealDay(server, player.getUUID(), guest) == null
				&& GuestLedger.get(server).affection(player.getUUID(), guest) == MAX, "Nothing is taken or recorded for nothing: " + why);
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

	/**
	 * A restart leaves a saved ceremony activity with no ceremony running (that lived in memory): recovery ends it, as not
	 * completed, and records nothing for the marriage. It ends nothing but a ceremony.
	 */
	@GameTest(maxTicks = 20)
	public void aLeftoverCeremonyAfterARestartIsEndedWithNothingRecorded(GameTestHelper h) {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		String guest = RESTART;
		List<Townsfolk> made = new ArrayList<>();
		GuestActivity activity = GuestActivity.get(server);
		GuestMilestoneTesting.recordActivities(guest);
		try {
			Townsfolk npc = place(h, guest, 2, 3, made);
			marry(h, player, npc, guest);
			h.assertTrue(activity.begin(guest, new GuestActivity.Activity(GuestCeremonies.KIND, me, level.getGameTime(), level.dimension(),
					npc.blockPosition())) && GuestMilestoneTesting.running(me).isEmpty(),
					"(as a restart leaves it: the saved ceremony activity holds the guest, and no ceremony is running)");
			GuestMilestoneTesting.recoverCeremonies(server);
			h.assertTrue(activity.current(guest) == null, "Recovery ends the leftover ceremony");
			h.assertTrue(!GuestMilestoneTesting.marriage(server, me).endsWith("+ceremony") && !GuestMilestoneTesting.reached(server, me, guest, "ceremony_1")
					&& keepsakes(player, "ceremony_1") == 0, "and records nothing: no ceremony held, no milestone, no bouquet");
			h.assertTrue(GuestMilestoneTesting.activityEvents(guest).equals(List.of("began:ceremony", "ended:ceremony:false")),
					"It ended as not completed: " + GuestMilestoneTesting.activityEvents(guest));
			h.assertTrue(guest.equals(GuestLedger.get(server).spouseOf(me)) && GuestMilestoneTesting.option(player, npc, guest, "ceremony"),
					"The marriage stands, and its ceremony may still be held");

			h.assertTrue(activity.begin(guest, new GuestActivity.Activity("outing", UUID.randomUUID(), level.getGameTime(), level.dimension(),
					npc.blockPosition())), "(an outing activity, as a restart might also leave)");
			GuestMilestoneTesting.recoverCeremonies(server);
			h.assertTrue(activity.current(guest) != null && "outing".equals(activity.current(guest).kind()), "is not the ceremonies' to end");
			h.succeed();
		} finally {
			GuestMilestoneTesting.stopRecordingActivities(guest);
			activity.end(guest, GuestCeremonies.KIND);
			activity.end(guest, "outing");
			clear(made);
		}
	}

	/**
	 * A ceremony is called off from either path, with nothing recorded: the menu's cancel only beside the guest's own
	 * canonical entity (a copy or the wrong guest id is refused and the ceremony goes on), the player's
	 * {@code /guests ceremony cancel} from anywhere, for a request or a running ceremony. A divorce during a ceremony ends
	 * it, and so does an operator's forced divorce, which fires no event, at the ceremony's next step.
	 */
	@GameTest(maxTicks = 20)
	public void aCeremonyIsCalledOffFromEitherPathAndByADivorce(GameTestHelper h) throws CommandSyntaxException {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		String guest = RITE;
		List<Townsfolk> made = new ArrayList<>();
		Townsfolk priest = priest(h, 6, 6);
		CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
		CommandSourceStack mine = player.createCommandSourceStack().withSuppressedOutput();
		GuestMilestoneTesting.recordActivities(guest);
		try {
			Townsfolk npc = place(h, guest, 2, 3, made);
			GuestMilestoneTesting.church(guest, level, area(h));
			GuestLedger ledger = GuestLedger.get(server);
			marry(h, player, npc, guest);

			// The menu's cancel: only beside the canonical entity, as this guest.
			confirmed(h, player, npc, guest);
			Townsfolk copy = stand(h, guest, 4, 3, made);
			h.assertTrue(!copy.getUUID().equals(ledger.canonical(guest)), "(the copy is not the canonical entity)");
			h.assertFalse(GuestMilestoneTesting.act(player, copy, guest, "ceremony_cancel").done(), "A copy cannot call the ceremony off");
			h.assertFalse(GuestMilestoneTesting.act(player, npc, OTHER, "ceremony_cancel").done(), "nor can the wrong guest id");
			h.assertTrue(guest.equals(GuestMilestoneTesting.running(me)) && GuestActivity.get(server).current(guest) != null, "The ceremony goes on");
			copy.discard();
			h.assertTrue(GuestMilestoneTesting.act(player, npc, guest, "ceremony_cancel").done(), "Beside the guest itself it is called off");
			nothingHeld(h, server, me, guest, "after the menu's cancel");

			// The command: from anywhere, for a running ceremony and for a request.
			confirmed(h, player, npc, guest);
			Vec3 away = centre(h, 3, 3).add(12, 0, 0);
			player.snapTo(away.x, away.y, away.z, 0.0F, 0.0F);
			h.assertTrue(dispatcher.execute("guests ceremony cancel", mine) == 1, "/guests ceremony cancel calls a running ceremony off from afar");
			nothingHeld(h, server, me, guest, "after the command");
			Vec3 back = centre(h, 3, 3);
			player.snapTo(back.x, back.y, back.z, 90.0F, 0.0F);
			h.assertTrue(GuestMilestoneTesting.act(player, npc, guest, "ceremony").done(), "(a ceremony is asked for)");
			h.assertTrue(dispatcher.execute("guests ceremony cancel", mine) == 1, "and the command withdraws the request");
			h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, "ceremony_confirm").done(), "which then cannot be confirmed");
			h.assertTrue(dispatcher.execute("guests ceremony cancel", mine) == 0, "With nothing left, the command cancels nothing");

			// A divorce during a running ceremony ends it.
			confirmed(h, player, npc, guest);
			h.assertTrue(GuestMarriage.requestDivorce(player, guest).done() && GuestMarriage.confirmDivorce(player, guest).done(), "(divorced)");
			nothingHeld(h, server, me, guest, "after a divorce");

			// An operator's forced divorce fires no event; the ceremony's next step ends it.
			GuestMilestoneTesting.clearCooldowns(server, me, guest);
			marry(h, player, npc, guest);
			confirmed(h, player, npc, guest);
			h.assertTrue(me.equals(GuestLedgerTesting.forceDivorce(ledger, guest, false)), "(an operator forces a divorce)");
			GuestMilestoneTesting.step(server, me);
			nothingHeld(h, server, me, guest, "after a forced divorce");
			h.assertTrue(GuestMilestoneTesting.activityEvents(guest).stream().noneMatch(event -> event.equals("ended:ceremony:true")),
					"No ceremony ended as completed: " + GuestMilestoneTesting.activityEvents(guest));
			h.succeed();
		} finally {
			GuestMilestoneTesting.stopRecordingActivities(guest);
			GuestMilestoneTesting.endCeremony(server, me);
			GuestMilestoneTesting.forgetChurch(guest);
			clear(made);
			priest.discard();
		}
	}

	/** The ceremony's player commands need only a player: no operator, and the shared {@code /guests} root stays open. */
	@GameTest(maxTicks = 20)
	public void ceremonyAndNoticeCommandsNeedOnlyAPlayer(GameTestHelper h) throws CommandSyntaxException {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
		CommandSourceStack mine = player.createCommandSourceStack().withSuppressedOutput();
		CommandSourceStack console = server.createCommandSourceStack().withSuppressedOutput();
		try {
			h.assertFalse(CustomNpcs.isOperator(mine), "(the survivor is no operator)");
			h.assertTrue(dispatcher.execute("guests ceremony cancel", mine) == 0, "A player may run /guests ceremony cancel (nothing to cancel)");
			h.assertTrue(dispatcher.execute("guests notices on", mine) == 1 && GuestMilestoneTesting.receives(server, me), "and /guests notices on");
			h.assertTrue(dispatcher.execute("guests notices off", mine) == 1 && !GuestMilestoneTesting.receives(server, me), "and off");
			h.assertTrue(!parses(dispatcher, "guests ceremony cancel", console) && !parses(dispatcher, "guests notices on", console),
					"The console, which is no player, gets neither");
			h.assertFalse(parses(dispatcher, "guests place " + RITE, mine), "An operator's subcommand stays closed to the player");
			h.succeed();
		} finally {
			GuestMilestoneTesting.setNotices(server, me, false, false);
		}
	}

	/**
	 * November 12 review: a player spending time with another guest (an outing, a visit, a rivalry) is not offered a
	 * ceremony, and neither a request nor its confirmation goes through, until that ends; nothing holds the spouse.
	 */
	@GameTest(maxTicks = 20)
	public void aCeremonyWaitsWhileThePlayerSpendsTimeWithAnotherGuest(GameTestHelper h) {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		String guest = BUSY_WED;
		List<Townsfolk> made = new ArrayList<>();
		Townsfolk priest = priest(h, 6, 6);
		GuestActivity activity = GuestActivity.get(server);
		try {
			Townsfolk npc = place(h, guest, 2, 3, made);
			Townsfolk other = place(h, BUSY_OTHER, 4, 2, made);
			GuestMilestoneTesting.church(guest, level, area(h));
			marry(h, player, npc, guest);
			h.assertTrue(activity.begin(BUSY_OTHER, new GuestActivity.Activity("outing", me, level.getGameTime(), level.dimension(),
					other.blockPosition())), "(the player is on an outing with another guest)");
			h.assertFalse(GuestMilestoneTesting.option(player, npc, guest, "ceremony"), "No ceremony is offered meanwhile");
			GuestResult refused = GuestMilestoneTesting.act(player, npc, guest, "ceremony");
			h.assertTrue(!refused.done() && refused.message().getString().contains("already spending time"), "and one is refused: "
					+ refused.message().getString());
			h.assertTrue(activity.end(BUSY_OTHER, "outing"), "(the outing ends)");

			h.assertTrue(GuestMilestoneTesting.act(player, npc, guest, "ceremony").done(), "A ceremony is asked for");
			h.assertTrue(activity.begin(BUSY_OTHER, new GuestActivity.Activity("outing", me, level.getGameTime(), level.dimension(),
					other.blockPosition())), "(another outing begins before it is confirmed)");
			h.assertFalse(GuestMilestoneTesting.act(player, npc, guest, "ceremony_confirm").done(), "Its confirmation is refused");
			h.assertTrue(activity.current(guest) == null && GuestMilestoneTesting.running(me).isEmpty(), "and nothing holds the spouse");
			h.assertTrue(activity.end(BUSY_OTHER, "outing"), "(the outing ends)");
			confirmed(h, player, npc, guest);
			h.assertTrue(GuestMilestoneTesting.act(player, npc, guest, "ceremony_cancel").done(), "(free again: held, then withdrawn)");
			h.succeed();
		} finally {
			activity.end(BUSY_OTHER, "outing");
			GuestMilestoneTesting.endCeremony(server, me);
			GuestMilestoneTesting.forgetChurch(guest);
			clear(made);
			priest.discard();
		}
	}

	/** Nothing of the player's ceremony with {@code guest} is left, and nothing was recorded for it. */
	private static void nothingHeld(GameTestHelper h, MinecraftServer server, UUID player, String guest, String when) {
		h.assertTrue(GuestMilestoneTesting.running(player).isEmpty() && GuestActivity.get(server).current(guest) == null,
				"No ceremony runs and nothing holds the guest " + when);
		h.assertTrue(!GuestMilestoneTesting.marriage(server, player).endsWith("+ceremony") && !GuestMilestoneTesting.reached(server, player, guest, "ceremony_1")
				&& !GuestMilestoneTesting.reached(server, player, guest, "ceremony_2"), "and no ceremony was recorded " + when);
	}

	private static boolean parses(CommandDispatcher<CommandSourceStack> dispatcher, String command, CommandSourceStack source) {
		try {
			dispatcher.execute(command, source);
			return true;
		} catch (CommandSyntaxException refused) {
			return false;
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

	/**
	 * A rivalry is noticed from its own start ({@code ACTIVITY_BEGAN}), at once: one that begins and ends within the same
	 * tick is still noticed, once, to a player with notices on, naming no player; a second start within
	 * {@link GuestNotices#RIVALRY_TICKS} is not; and no other activity is a rivalry notice.
	 */
	@GameTest(maxTicks = 20)
	public void aRivalryIsNoticedTheMomentItBegins(GameTestHelper h) {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		ServerPlayer reader = visitor(h, 3, 3);
		UUID me = reader.getUUID();
		String guest = RIVAL;
		List<Townsfolk> made = new ArrayList<>();
		GuestActivity activity = GuestActivity.get(server);
		UUID someone = UUID.randomUUID();
		BlockPos at = h.absolutePos(new BlockPos(3, 2, 3));
		GuestMilestoneTesting.record(me);
		try {
			Townsfolk npc = place(h, guest, 2, 3, made);
			h.assertTrue(GuestMilestoneTesting.act(reader, npc, guest, "notices_toggle").done() && GuestMilestoneTesting.receives(server, me),
					"(the reader turns notices on)");
			h.assertTrue(activity.begin(guest, new GuestActivity.Activity(GuestRivalries.KIND, someone, level.getGameTime(), level.dimension(), at))
					&& activity.end(guest, GuestRivalries.KIND), "(a rivalry begins and ends within one tick)");
			var sent = GuestMilestoneTesting.recorded(me);
			h.assertTrue(sent.size() == 1 && sent.getFirst().named().isEmpty()
					&& sent.getFirst().text().getString().contains(GuestRoster.get(guest).name()),
					"It is noticed at once, naming the guest and no player: " + sent);

			h.assertTrue(activity.begin(guest, new GuestActivity.Activity(GuestRivalries.KIND, someone, level.getGameTime(), level.dimension(), at))
					&& activity.end(guest, GuestRivalries.KIND), "(another rivalry with the same guest at once)");
			h.assertTrue(GuestMilestoneTesting.recorded(me).size() == 1, "is not noticed again so soon");
			h.assertTrue(activity.begin(guest, new GuestActivity.Activity("outing", someone, level.getGameTime(), level.dimension(), at))
					&& activity.end(guest, "outing"), "(an outing)");
			h.assertTrue(GuestMilestoneTesting.recorded(me).size() == 1, "and no other activity is a rivalry notice");
			h.succeed();
		} finally {
			GuestMilestoneTesting.setNotices(server, me, false, false);
			GuestMilestoneTesting.stopRecording(me);
			activity.end(guest, GuestRivalries.KIND);
			activity.end(guest, "outing");
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
