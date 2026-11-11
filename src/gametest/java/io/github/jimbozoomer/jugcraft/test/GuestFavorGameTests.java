package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.guests.GuestBalance;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFavorTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFavors;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedger;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedgerTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestMenu;
import io.github.jimbozoomer.jugcraft.town.guests.GuestResult;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRoster;
import java.util.ArrayList;
import java.util.List;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Town guests (docs/features/jze-town-relationships.md), worker B's tests: gifts and fetch quests: exact consumption once, caps, stale and replayed clicks across reopened menus, no item loss on refusal.
 * Registered in fabric.mod.json by the shared API.
 *
 * <p>Every test uses its own real survival players (fresh identities, so affection and favors start empty) and pins
 * each player's favors day through {@link GuestFavorTesting}, never the world's clock. Each test places its own guests,
 * bound in the ledger as their canonical entities (the favors act only beside the canonical guest), with ids no other
 * test here uses, and discards them when it ends, which frees the identities for the next binding. The ids bound:
 * frieren, makima, himeno, reze, zero_two, tifa_lockhart, yorha_2b, misato_katsuragi, lara_croft, ada_wong,
 * nicole_demara, kafka, miyabi, chun_li, megumin, rei_ayanami, asuka_langley, gold_ship and oguri_cap. The last two are
 * left damaged in the ledger by {@link #aDamagedGuestLedgerEntryTakesNothing} (worker A's seam) for the rest of the run.
 */
public class GuestFavorGameTests {
	/** The day pinned for each test's player: far from the overworld's own. */
	private static final long DAY = 4_000L;
	/** Ticks between menu clicks: more than {@link GuestBalance#ACTION_GAP_TICKS}, so no click is merely throttled. */
	private static final int GAP = GuestBalance.ACTION_GAP_TICKS + 2;

	@GameTest(maxTicks = 20)
	public void aGiftTakesExactlyOneItemFromTheHandStackOnce(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 2);
		UUID me = player.getUUID();
		String guest = "frieren";
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			Townsfolk npc = place(h, guest, 2, 2, made);
			GuestLedger ledger = GuestLedger.get(server);
			Item plain = ordinaryGift(guest);
			int value = GuestFavorTesting.giftValue(guest, plain);
			h.assertTrue(value == GuestFavors.baseValue(plain) && value > 0, "An ordinary gift is worth its table value: " + plain);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(plain, 5));
			ItemStack hand = player.getMainHandItem();
			player.getInventory().setItem(7, new ItemStack(plain, 7));
			ItemStack spare = player.getInventory().getItem(7);
			h.assertTrue(ledger.affection(me, guest) == 0, "A new player starts with no affection");

			GuestResult result = GuestFavors.gift(player, npc, guest);
			h.assertTrue(result.done(), "An ordinary gift is taken: " + result.message().getString());
			h.assertTrue(player.getMainHandItem() == hand && hand.getCount() == 4, "Exactly one item leaves the hand stack itself");
			h.assertTrue(spare.getCount() == 7 && player.getInventory().getItem(7) == spare, "The same item in another slot is untouched");
			h.assertTrue(ledger.affection(me, guest) == value, "The gift's value is added once: " + ledger.affection(me, guest));
			h.assertTrue(GuestFavors.giftsLeft(player, guest) == GuestFavors.GIFTS_PER_DAY - 1
					&& GuestFavors.affectionLeftToday(player, guest) == GuestFavors.DAILY_AFFECTION_CAP - value,
					"One gift and its affection are counted for today");

			// A favourite is worth its value and the bonus, and again only one item is taken.
			Item favourite = GuestFavorTesting.favourites(guest).getFirst();
			int bonus = GuestFavorTesting.giftValue(guest, favourite);
			h.assertTrue(GuestFavorTesting.favourites(guest).size() == GuestFavors.FAVOURITES
					&& bonus == GuestFavors.baseValue(favourite) + GuestFavors.FAVOURITE_BONUS, "A favourite carries the bonus: " + favourite);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(favourite, 1));
			ItemStack treat = player.getMainHandItem();
			h.assertTrue(GuestFavors.gift(player, npc, guest).done(), "A favourite is taken");
			h.assertTrue(treat.isEmpty() && player.getMainHandItem().isEmpty(), "The last one in the hand goes, and nothing else");
			h.assertTrue(ledger.affection(me, guest) == value + bonus && hand.getCount() == 4 && spare.getCount() == 7,
					"The favourite adds its value and bonus once; the earlier stacks are untouched");
			h.assertTrue(GuestFavors.giftsLeft(player, guest) == GuestFavors.GIFTS_PER_DAY - 2, "Two gifts are counted for today");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	@GameTest(maxTicks = 20)
	public void refusedGiftsTakeNothing(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			GuestLedger ledger = GuestLedger.get(server);
			int cap = GuestFavors.DAILY_AFFECTION_CAP;

			// An empty hand, or something the guest does not take: no valuables, nothing else, and no changed stack.
			String picky = "makima";
			Townsfolk makima = place(h, picky, 1, 1, made);
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			h.assertFalse(GuestFavors.gift(player, makima, picky).done(), "An empty hand gives nothing");
			ItemStack keepsake = new ItemStack(ordinaryGift(picky), 3);
			keepsake.set(DataComponents.CUSTOM_NAME, Component.literal("Pressed keepsake"));
			for (ItemStack unwanted : List.of(new ItemStack(Items.DIAMOND, 3), new ItemStack(Items.EMERALD, 3), new ItemStack(Items.DIRT, 3), keepsake)) {
				player.setItemInHand(InteractionHand.MAIN_HAND, unwanted);
				ItemStack held = player.getMainHandItem();
				h.assertFalse(GuestFavors.gift(player, makima, picky).done(), "Refused as a gift: " + held);
				h.assertTrue(player.getMainHandItem() == held && held.getCount() == 3, "and nothing is taken: " + held);
			}
			h.assertTrue(ledger.affection(me, picky) == 0 && GuestFavors.giftsLeft(player, picky) == GuestFavors.GIFTS_PER_DAY
					&& GuestFavors.affectionLeftToday(player, picky) == cap, "A refused gift counts for nothing");

			// The day's gifts used up.
			String busy = "himeno";
			Townsfolk himeno = place(h, busy, 3, 1, made);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ordinaryGift(busy), 8));
			ItemStack flowers = player.getMainHandItem();
			for (int i = 0; i < GuestFavors.GIFTS_PER_DAY; i++) {
				h.assertTrue(GuestFavors.gift(player, himeno, busy).done(), "Gift " + (i + 1) + " of the day is taken");
			}
			int given = ledger.affection(me, busy);
			h.assertTrue(flowers.getCount() == 8 - GuestFavors.GIFTS_PER_DAY && GuestFavors.giftsLeft(player, busy) == 0
					&& GuestFavors.affectionLeftToday(player, busy) > 0, "The day's gifts are used, the cap is not");
			h.assertFalse(GuestFavors.gift(player, himeno, busy).done(), "One more gift today is refused");
			h.assertTrue(flowers.getCount() == 8 - GuestFavors.GIFTS_PER_DAY && ledger.affection(me, busy) == given, "and takes nothing");

			// Today's affection cap reached (as earlier gains today would leave it).
			String capped = "reze";
			Townsfolk reze = place(h, capped, 5, 1, made);
			GuestFavorTesting.gainedToday(player, capped, cap);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ordinaryGift(capped), 4));
			ItemStack bunch = player.getMainHandItem();
			h.assertTrue(GuestFavors.affectionLeftToday(player, capped) == 0 && GuestFavors.giftsLeft(player, capped) == GuestFavors.GIFTS_PER_DAY,
					"Precondition: today's cap used, gifts left");
			h.assertFalse(GuestFavors.gift(player, reze, capped).done(), "At the day's cap a gift is refused");
			h.assertTrue(bunch.getCount() == 4 && ledger.affection(me, capped) == 0 && GuestFavors.giftsLeft(player, capped) == GuestFavors.GIFTS_PER_DAY,
					"and takes nothing and uses no gift");

			// Near the cap, a gift adds only what is left of it.
			String nearly = "zero_two";
			Townsfolk zeroTwo = place(h, nearly, 1, 5, made);
			GuestFavorTesting.gainedToday(player, nearly, cap - 2);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ordinaryGift(nearly), 4));
			ItemStack some = player.getMainHandItem();
			h.assertTrue(GuestFavorTesting.giftValue(nearly, some.getItem()) > 2, "Precondition: the gift is worth more than is left");
			h.assertTrue(GuestFavors.gift(player, zeroTwo, nearly).done(), "With 2 left today, a gift is still taken");
			h.assertTrue(some.getCount() == 3 && ledger.affection(me, nearly) == 2 && GuestFavors.affectionLeftToday(player, nearly) == 0,
					"one item, for exactly what was left of the cap");

			// Full affection.
			String fond = "tifa_lockhart";
			Townsfolk tifa = place(h, fond, 3, 5, made);
			ledger.addAffection(me, fond, GuestBalance.MAX_AFFECTION);
			h.assertTrue(ledger.affection(me, fond) == GuestBalance.MAX_AFFECTION && GuestFavors.affectionLeftToday(player, fond) == 0,
					"Precondition: full affection");
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ordinaryGift(fond), 4));
			ItemStack posies = player.getMainHandItem();
			h.assertFalse(GuestFavors.gift(player, tifa, fond).done(), "At full affection a gift is refused");
			h.assertTrue(posies.getCount() == 4 && GuestFavors.giftsLeft(player, fond) == GuestFavors.GIFTS_PER_DAY
					&& ledger.affection(me, fond) == GuestBalance.MAX_AFFECTION, "and takes nothing and uses no gift");

			// One short of full: a gift adds just that one.
			String almost = "yorha_2b";
			Townsfolk twoB = place(h, almost, 5, 5, made);
			ledger.addAffection(me, almost, GuestBalance.MAX_AFFECTION - 1);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ordinaryGift(almost), 4));
			ItemStack last = player.getMainHandItem();
			h.assertTrue(GuestFavors.gift(player, twoB, almost).done() && last.getCount() == 3
					&& ledger.affection(me, almost) == GuestBalance.MAX_AFFECTION, "One short of full, a gift fills it and takes one item");

			// A pending guest, an unknown one, and a guest who is not the one standing there.
			Townsfolk rooni = stand(h, "rooni", 1, 3, made);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(GuestFavors.GIFTS.getFirst().item(), 4));
			ItemStack offered = player.getMainHandItem();
			h.assertFalse(GuestFavors.gift(player, rooni, "rooni").done(), "A pending guest takes no gift");
			h.assertFalse(GuestFavors.gift(player, rooni, "no_such_guest").done(), "An unknown guest takes no gift");
			h.assertFalse(GuestFavors.gift(player, makima, "chun_li").done(), "A gift is only for the guest spoken to");
			h.assertTrue(offered.getCount() == 4 && player.getMainHandItem() == offered, "and none of them takes anything");
			h.assertTrue(GuestFavors.giftsLeft(player, "rooni") == 0 && GuestFavors.affectionLeftToday(player, "rooni") == 0
					&& GuestFavors.questState(player, "rooni") == GuestFavors.QuestState.NONE && GuestFavors.questItem(player, "rooni").isEmpty()
					&& GuestFavors.carried(player, "rooni") == 0, "A pending guest offers no gifts and no errand");
			h.assertFalse(GuestFavors.acceptQuest(player, rooni, "rooni").done() || GuestFavors.turnIn(player, rooni, "rooni").done(),
					"nor can one be taken up or turned in");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	@GameTest(maxTicks = 20)
	public void dailyLimitsResetOnTheNextDayAndNotBeforeAndSurviveASave(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 2);
		UUID me = player.getUUID();
		String guest = "misato_katsuragi";
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			Townsfolk npc = place(h, guest, 2, 2, made);
			GuestLedger ledger = GuestLedger.get(server);
			h.assertTrue(GuestFavorTesting.day(player) == DAY && GuestFavorTesting.effectiveDay(player) == DAY, "The test's day is pinned");
			Item cheap = ordinaryGift(guest);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(cheap, 16));
			ItemStack held = player.getMainHandItem();
			for (int i = 0; i < GuestFavors.GIFTS_PER_DAY; i++) {
				h.assertTrue(GuestFavors.gift(player, npc, guest).done(), "Gift " + (i + 1) + " of the day is taken");
			}
			ItemStack wanted = GuestFavors.questItem(player, guest);
			h.assertTrue(!wanted.isEmpty() && GuestFavors.questState(player, guest) == GuestFavors.QuestState.OFFERED, "Today's errand is offered");
			h.assertTrue(GuestFavors.acceptQuest(player, npc, guest).done(), "and taken up");
			player.getInventory().setItem(20, wanted.copy());
			h.assertTrue(GuestFavors.turnIn(player, npc, guest).done(), "and turned in");
			int affection = ledger.affection(me, guest);
			int left = GuestFavors.affectionLeftToday(player, guest);
			h.assertTrue(GuestFavors.giftsLeft(player, guest) == 0 && GuestFavors.questState(player, guest) == GuestFavors.QuestState.DONE
					&& left == GuestFavors.DAILY_AFFECTION_CAP - affection && left > 0, "The day's gifts and errand are used: " + affection);

			// The same day, and a day that moves backwards, reset nothing.
			h.assertFalse(GuestFavors.gift(player, npc, guest).done() || GuestFavors.acceptQuest(player, npc, guest).done(),
					"Later the same day, no more gifts and no second errand");
			GuestFavorTesting.pinDay(player, DAY - 1);
			h.assertTrue(GuestFavorTesting.effectiveDay(player) == DAY && GuestFavors.giftsLeft(player, guest) == 0
					&& GuestFavors.questState(player, guest) == GuestFavors.QuestState.DONE
					&& GuestFavors.affectionLeftToday(player, guest) == left, "A clock moved back resets nothing");
			h.assertFalse(GuestFavors.gift(player, npc, guest).done(), "and gives no extra gift");
			h.assertTrue(held.getCount() == 16 - GuestFavors.GIFTS_PER_DAY && ledger.affection(me, guest) == affection,
					"Nothing more was taken or added");
			GuestFavorTesting.pinDay(player, DAY);

			// Save and load: the codec keeps every count.
			Tag saved = GuestFavorTesting.save(server);
			GuestFavorTesting.forget(server, me);
			h.assertTrue(GuestFavors.giftsLeft(player, guest) == GuestFavors.GIFTS_PER_DAY
					&& GuestFavors.questState(player, guest) == GuestFavors.QuestState.OFFERED, "Precondition: without its record the day looks new");
			GuestFavorTesting.load(server, saved, me);
			ItemStack kept = GuestFavors.questItem(player, guest);
			h.assertTrue(GuestFavors.giftsLeft(player, guest) == 0 && GuestFavors.questState(player, guest) == GuestFavors.QuestState.DONE
					&& GuestFavors.affectionLeftToday(player, guest) == left && same(kept, wanted),
					"After a save and load the gifts, errand and affection gained today are all kept");
			h.assertFalse(GuestFavors.gift(player, npc, guest).done(), "and still no extra gift");

			// Reconnecting resets nothing.
			TestPlayers.leave(player);
			ServerPlayer back = TestPlayers.again(h, me);
			h.assertTrue(GuestFavors.giftsLeft(back, guest) == 0 && GuestFavors.questState(back, guest) == GuestFavors.QuestState.DONE
					&& GuestFavors.affectionLeftToday(back, guest) == left, "After reconnecting, today's limits still hold");

			// The next day: a fresh allowance and a new errand.
			GuestFavorTesting.pinDay(back, DAY + 1);
			h.assertTrue(GuestFavors.giftsLeft(back, guest) == GuestFavors.GIFTS_PER_DAY
					&& GuestFavors.questState(back, guest) == GuestFavors.QuestState.OFFERED
					&& GuestFavors.affectionLeftToday(back, guest) == GuestFavors.DAILY_AFFECTION_CAP, "The next day starts fresh");
			back.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(cheap, 2));
			ItemStack again = back.getMainHandItem();
			h.assertTrue(GuestFavors.gift(back, npc, guest).done() && again.getCount() == 1
					&& GuestFavors.giftsLeft(back, guest) == GuestFavors.GIFTS_PER_DAY - 1
					&& ledger.affection(me, guest) == affection + GuestFavorTesting.giftValue(guest, cheap), "and a gift is taken again, once");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	@GameTest(maxTicks = 20)
	public void aClockMovedBackNeverGrantsADayAgain(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 3);
		ServerPlayer reloaded = visitor(h, 4, 4);
		List<Townsfolk> made = new ArrayList<>();
		try {
			Townsfolk kafka = place(h, "kafka", 2, 2, made);
			Townsfolk miyabi = place(h, "miyabi", 2, 5, made);
			clockMovedBack(h, server, player, kafka, miyabi, false);
			clockMovedBack(h, server, reloaded, kafka, miyabi, true);
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(player.getUUID());
			GuestFavorTesting.unpin(reloaded.getUUID());
		}
	}

	@GameTest(maxTicks = 20)
	public void aTurnInTakesExactlyTheCountOnce(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 2);
		UUID me = player.getUUID();
		String guest = "lara_croft";
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			Townsfolk npc = place(h, guest, 2, 2, made);
			GuestLedger ledger = GuestLedger.get(server);
			ItemStack wanted = GuestFavors.questItem(player, guest);
			Item item = wanted.getItem();
			int count = wanted.getCount();
			h.assertTrue(!wanted.isEmpty() && count >= 4 && GuestFavors.questState(player, guest) == GuestFavors.QuestState.OFFERED,
					"Today's errand is offered: " + wanted);
			h.assertTrue(same(GuestFavors.questItem(player, guest), wanted) && same(wanted, GuestFavorTesting.offeredOn(guest, player, DAY)),
					"The same errand on every call, fixed by guest, player and day");
			player.getInventory().setItem(9, new ItemStack(item, count - 1));
			ItemStack few = player.getInventory().getItem(9);
			h.assertFalse(GuestFavors.turnIn(player, npc, guest).done(), "Nothing is turned in before the errand is taken up");
			h.assertTrue(GuestFavors.acceptQuest(player, npc, guest).done() && GuestFavors.questState(player, guest) == GuestFavors.QuestState.ACTIVE,
					"The errand is taken up");
			h.assertFalse(GuestFavors.acceptQuest(player, npc, guest).done(), "Taking it up again changes nothing");
			h.assertTrue(same(GuestFavors.questItem(player, guest), wanted) && GuestFavors.carried(player, guest) == count - 1,
					"The errand taken up is the one offered");

			h.assertFalse(GuestFavors.turnIn(player, npc, guest).done(), "One short is refused");
			h.assertTrue(few.getCount() == count - 1 && GuestFavors.questState(player, guest) == GuestFavors.QuestState.ACTIVE
					&& ledger.affection(me, guest) == 0, "and takes nothing");

			player.getInventory().setItem(10, new ItemStack(item, 4));
			h.assertTrue(GuestFavors.carried(player, guest) == count + 3, "Now the player carries three more than asked");
			h.assertTrue(GuestFavors.turnIn(player, npc, guest).done(), "The turn-in is taken");
			h.assertTrue(plainCount(player, item) == 3 && GuestFavors.carried(player, guest) == 3,
					"Exactly the count was taken: " + plainCount(player, item));
			h.assertTrue(GuestFavors.questState(player, guest) == GuestFavors.QuestState.DONE && ledger.affection(me, guest) == GuestFavors.QUEST_REWARD
					&& GuestFavors.affectionLeftToday(player, guest) == GuestFavors.DAILY_AFFECTION_CAP - GuestFavors.QUEST_REWARD,
					"The errand is done and its reward added once");

			h.assertFalse(GuestFavors.turnIn(player, npc, guest).done(), "A second turn-in is refused");
			h.assertTrue(plainCount(player, item) == 3 && ledger.affection(me, guest) == GuestFavors.QUEST_REWARD, "and takes nothing");
			h.assertFalse(GuestFavors.acceptQuest(player, npc, guest).done(), "No second errand today");
			h.assertTrue(GuestFavors.questState(player, guest) == GuestFavors.QuestState.DONE && plainCount(player, item) == 3,
					"The errand stays done and nothing more is taken");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	@GameTest(maxTicks = 20)
	public void aTurnInIsExactAcrossSeveralStacks(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 2);
		UUID me = player.getUUID();
		String guest = "ada_wong";
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			Townsfolk npc = place(h, guest, 2, 2, made);
			GuestLedger ledger = GuestLedger.get(server);
			ItemStack wanted = GuestFavors.questItem(player, guest);
			Item item = wanted.getItem();
			int count = wanted.getCount();
			h.assertTrue(!wanted.isEmpty() && count >= 4, "Today's errand is offered: " + wanted);
			h.assertTrue(GuestFavors.acceptQuest(player, npc, guest).done(), "and taken up");
			// count + 3 in three main stacks, 2 in the offhand, and a named keepsake that is never counted or taken.
			player.getInventory().setItem(0, new ItemStack(item, 1));
			player.getInventory().setItem(13, new ItemStack(item, count / 2));
			player.getInventory().setItem(27, new ItemStack(item, count - 1 - count / 2 + 3));
			player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(item, 2));
			ItemStack keepsake = new ItemStack(item, 5);
			keepsake.set(DataComponents.CUSTOM_NAME, Component.literal("Keepsake"));
			player.getInventory().setItem(35, keepsake);
			ItemStack kept = player.getInventory().getItem(35);
			h.assertTrue(GuestFavors.carried(player, guest) == count + 5 && plainCount(player, item) == count + 5,
					"Every plain stack counts, the offhand too, the named keepsake not: " + GuestFavors.carried(player, guest));

			h.assertTrue(GuestFavors.turnIn(player, npc, guest).done(), "The turn-in is taken");
			h.assertTrue(plainCount(player, item) == 5 && GuestFavors.carried(player, guest) == 5,
					"Exactly the count left across the stacks: " + plainCount(player, item));
			h.assertTrue(kept.getCount() == 5 && kept.has(DataComponents.CUSTOM_NAME) && player.getInventory().getItem(35) == kept,
					"The keepsake is untouched");
			h.assertTrue(ledger.affection(me, guest) == GuestFavors.QUEST_REWARD, "The reward is added once");

			h.assertFalse(GuestFavors.turnIn(player, npc, guest).done(), "A second turn-in is refused");
			h.assertTrue(plainCount(player, item) == 5 && kept.getCount() == 5 && ledger.affection(me, guest) == GuestFavors.QUEST_REWARD,
					"and takes nothing");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	@GameTest(maxTicks = 20)
	public void directCallsAwayFromTheCanonicalGuestTakeNothing(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 3);
		UUID me = player.getUUID();
		String guest = "chun_li";
		String offering = "megumin";
		UUID awayId = UUID.randomUUID();
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			Townsfolk npc = place(h, guest, 2, 2, made);
			Townsfolk other = place(h, offering, 2, 5, made);
			GuestLedger ledger = GuestLedger.get(server);
			h.assertTrue(GuestFavors.acceptQuest(player, npc, guest).done(), "Precondition: " + guest + "'s errand is active");
			ItemStack wanted = GuestFavors.questItem(player, guest);
			Item item = wanted.getItem();
			player.getInventory().setItem(9, new ItemStack(item, wanted.getCount() + 2));
			Item flower = ordinaryGift(guest);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(flower, 4));
			h.assertTrue(GuestFavors.questState(player, offering) == GuestFavors.QuestState.OFFERED, "Precondition: " + offering + " offers an errand");
			Snapshot before = Snapshot.of(player, ledger, guest, offering, flower, item);

			// Out of range.
			Vec3 home = player.position();
			Vec3 far = centre(h, 24, 3);
			player.snapTo(far.x, far.y, far.z, 90.0F, 0.0F);
			refusedAll(h, player, npc, guest, other, offering, "out of range");
			h.assertTrue(Snapshot.of(player, ledger, guest, offering, flower, item).equals(before), "Out of range: nothing taken or added");
			player.snapTo(home.x, home.y, home.z, 90.0F, 0.0F);

			// A spectator.
			player.setGameMode(GameType.SPECTATOR);
			refusedAll(h, player, npc, guest, other, offering, "as a spectator");
			player.setGameMode(GameType.SURVIVAL);
			h.assertTrue(Snapshot.of(player, ledger, guest, offering, flower, item).equals(before), "A spectator: nothing taken or added");

			// Duplicates: entities carrying the ids that are not the ledger's canonical ones.
			Townsfolk copy = stand(h, guest, 5, 5, made);
			Townsfolk otherCopy = stand(h, offering, 5, 1, made);
			h.assertTrue(!copy.getUUID().equals(ledger.canonical(guest)) && !otherCopy.getUUID().equals(ledger.canonical(offering)),
					"Precondition: the copies are not canonical");
			refusedAll(h, player, copy, guest, otherCopy, offering, "with a duplicate");
			h.assertTrue(Snapshot.of(player, ledger, guest, offering, flower, item).equals(before), "A duplicate: nothing taken or added");

			// Mismatched ids: each canonical entity asked as the other guest.
			refusedAll(h, player, other, guest, npc, offering, "with a mismatched id");
			h.assertTrue(Snapshot.of(player, ledger, guest, offering, flower, item).equals(before), "A mismatched id: nothing taken or added");

			// Another level: a player in the Nether at the guest's very coordinates. It is constructed there but never joins
			// the game, so no chunk is loaded for it; only the level differs.
			ServerLevel nether = server.getLevel(Level.NETHER);
			h.assertTrue(nether != null && nether != npc.level(), "The test server has a Nether");
			ServerPlayer away = new ServerPlayer(server, nether, new GameProfile(awayId, "guest-away"), ClientInformation.createDefault());
			away.snapTo(npc.getX(), npc.getY(), npc.getZ(), 90.0F, 0.0F);
			h.assertTrue(away.level() == nether && away.isAlive() && !away.isSpectator()
					&& npc.getBoundingBox().distanceToSqr(away.getEyePosition()) <= GuestBalance.INTERACT_RANGE * GuestBalance.INTERACT_RANGE,
					"Precondition: alive, not a spectator, within reach by the numbers, in another level");
			GuestFavorTesting.pinDay(away, DAY);
			GuestFavorTesting.takeUp(away, guest);
			away.getInventory().setItem(9, new ItemStack(item, wanted.getCount() + 2));
			away.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(flower, 4));
			h.assertTrue(GuestFavors.questState(away, guest) == GuestFavors.QuestState.ACTIVE
					&& GuestFavors.questState(away, offering) == GuestFavors.QuestState.OFFERED, "Precondition: an errand active, another offered");
			Snapshot elsewhere = Snapshot.of(away, ledger, guest, offering, flower, item);
			refusedAll(h, away, npc, guest, other, offering, "from another level");
			h.assertTrue(Snapshot.of(away, ledger, guest, offering, flower, item).equals(elsewhere) && away.getMainHandItem().getCount() == 4
					&& plainCount(away, item) == wanted.getCount() + 2, "Another level: nothing taken or added");
			h.assertTrue(Snapshot.of(player, ledger, guest, offering, flower, item).equals(before), "and the player beside the guest is untouched");

			// Dead (no health left, not yet removed).
			npc.setHealth(0.0F);
			other.setHealth(0.0F);
			h.assertTrue(!npc.isAlive() && !npc.isRemoved(), "Precondition: dead, not yet removed");
			refusedAll(h, player, npc, guest, other, offering, "with a dead guest");
			h.assertTrue(Snapshot.of(player, ledger, guest, offering, flower, item).equals(before), "A dead guest: nothing taken or added");
			npc.setHealth(npc.getMaxHealth());
			other.setHealth(other.getMaxHealth());

			// The control: in reach of the living canonical guest, the same call is taken.
			h.assertTrue(GuestFavors.gift(player, npc, guest).done() && player.getMainHandItem().getCount() == 3
					&& ledger.affection(me, guest) == GuestFavorTesting.giftValue(guest, flower), "Beside the canonical guest the gift is taken, once");
			before = Snapshot.of(player, ledger, guest, offering, flower, item);

			// Removed.
			npc.discard();
			other.discard();
			refusedAll(h, player, npc, guest, other, offering, "with a removed guest");
			h.assertTrue(Snapshot.of(player, ledger, guest, offering, flower, item).equals(before), "A removed guest: nothing taken or added");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
			GuestFavorTesting.unpin(awayId);
		}
	}

	@GameTest(maxTicks = 20)
	public void aDamagedPlayerLedgerEntryTakesNothing(GameTestHelper h) {
		damagedLedgerTakesNothing(h, "rei_ayanami", "asuka_langley", false);
	}

	@GameTest(maxTicks = 20)
	public void aDamagedGuestLedgerEntryTakesNothing(GameTestHelper h) {
		damagedLedgerTakesNothing(h, "gold_ship", "oguri_cap", true);
	}

	@GameTest(maxTicks = 100)
	public void aReplayedTurnInTakesNothingAfterTheMenuIsReopened(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 2);
		UUID me = player.getUUID();
		String guest = "nicole_demara";
		List<Townsfolk> made = new ArrayList<>();
		Townsfolk npc = place(h, guest, 2, 2, made);
		GuestLedger ledger = GuestLedger.get(server);
		GuestFavorTesting.pinDay(player, DAY);
		ItemStack wanted = GuestFavors.questItem(player, guest);
		Item item = wanted.getItem();
		int count = wanted.getCount();
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ordinaryGift(guest), 3));
		player.getInventory().setItem(9, new ItemStack(item, count + 4));

		GuestMenu.open(player, npc, guest);
		h.assertTrue(player.containerMenu instanceof GuestMenu, "Talking opens the guest's menu");
		GuestMenu menu = (GuestMenu) player.containerMenu;
		h.assertTrue(menu.stillValid(player), "The player stands beside the canonical guest");
		h.assertTrue(menu.clickMenuButton(player, GuestMenu.button(GuestMenu.Action.QUEST_ACCEPT, menu.get(GuestMenu.REVISION))),
				"The errand is taken up through the menu");
		h.assertTrue(GuestFavors.questState(player, guest) == GuestFavors.QuestState.ACTIVE, "and is active");

		h.runAfterDelay(GAP, () -> {
			h.assertTrue(menu.clickMenuButton(player, GuestMenu.button(GuestMenu.Action.GIFT, menu.get(GuestMenu.REVISION))),
					"A gift through the menu");
			h.assertTrue(player.getMainHandItem().getCount() == 2 && GuestFavors.giftsLeft(player, guest) == GuestFavors.GIFTS_PER_DAY - 1,
					"takes one item and one of the day's gifts");
		});
		int[] turnIn = new int[1];
		h.runAfterDelay(2L * GAP, () -> {
			turnIn[0] = GuestMenu.button(GuestMenu.Action.QUEST_TURN_IN, menu.get(GuestMenu.REVISION));
			h.assertTrue(menu.clickMenuButton(player, turnIn[0]), "The turn-in through the menu");
			h.assertTrue(player.getInventory().getItem(9).getCount() == 4 && GuestFavors.questState(player, guest) == GuestFavors.QuestState.DONE,
					"takes exactly the count and finishes the errand");
			h.assertFalse(menu.clickMenuButton(player, turnIn[0]), "The same click sent again is refused");
			h.assertTrue(player.getInventory().getItem(9).getCount() == 4, "and takes nothing");
		});
		GuestMenu[] reopened = new GuestMenu[1];
		int[] before = new int[3];
		h.runAfterDelay(3L * GAP, () -> {
			h.assertFalse(menu.clickMenuButton(player, turnIn[0]), "The same click replayed later is refused");
			before[0] = ledger.affection(me, guest);
			before[1] = GuestFavors.giftsLeft(player, guest);
			before[2] = GuestFavors.affectionLeftToday(player, guest);
			player.closeContainer();
			h.assertTrue(player.containerMenu == player.inventoryMenu, "The menu is closed");
			GuestMenu.open(player, npc, guest);
			h.assertTrue(player.containerMenu instanceof GuestMenu, "and opened again");
			GuestMenu again = (GuestMenu) player.containerMenu;
			h.assertTrue(again != menu && again.stillValid(player), "A new menu, beside the guest");
			reopened[0] = again;
			h.assertFalse(again.clickMenuButton(player, turnIn[0]), "The old click on the new menu is refused");
			h.assertFalse(menu.clickMenuButton(player, turnIn[0]), "and on the old one");
			h.assertTrue(player.getInventory().getItem(9).getCount() == 4, "and neither takes anything");
		});
		h.runAfterDelay(4L * GAP, () -> {
			GuestMenu again = reopened[0];
			h.assertFalse(again.clickMenuButton(player, turnIn[0]), "The old click replayed later is still refused");
			h.assertFalse(again.clickMenuButton(player, GuestMenu.button(GuestMenu.Action.QUEST_TURN_IN, again.get(GuestMenu.REVISION))),
					"A fresh, current turn-in click is refused too: today's errand is done on the server");
			h.assertTrue(player.getInventory().getItem(9).getCount() == 4 && player.getMainHandItem().getCount() == 2,
					"The items were taken once only");
			h.assertTrue(GuestFavors.questState(player, guest) == GuestFavors.QuestState.DONE && ledger.affection(me, guest) == before[0]
					&& GuestFavors.giftsLeft(player, guest) == before[1] && GuestFavors.affectionLeftToday(player, guest) == before[2],
					"Reopening reset no daily limit or errand");
			h.assertTrue(again.get(GuestMenu.QUEST_STATE) == GuestFavors.QuestState.DONE.ordinal()
					&& again.get(GuestMenu.GIFTS_LEFT) == before[1] && again.get(GuestMenu.AFFECTION_LEFT_TODAY) == before[2],
					"and the reopened menu shows the server's state");
			player.closeContainer();
			clear(made);
			GuestFavorTesting.unpin(me);
			h.succeed();
		});
	}

	// ---------------------------------------------------------------- helpers

	/**
	 * Codex's backwards-clock path for one player: A's errand done on day 10; on day 11 only a gift to B (which tidies
	 * the player's day-10 records away); the clock back to day 10. Day 11 stays in force: A offers no second day-10
	 * errand, only day 11's, which was not yet done, and going back grants nothing; and once the clock catches up there
	 * is no fresh errand or reward, so the two days give exactly one errand each. With {@code reload}, the favors are
	 * saved and read back with the codec (records and high-water day both) before the clock moves back.
	 */
	private static void clockMovedBack(GameTestHelper h, MinecraftServer server, ServerPlayer player, Townsfolk kafka, Townsfolk miyabi,
			boolean reload) {
		String a = "kafka";
		String b = "miyabi";
		String path = reload ? " (across a save and load)" : "";
		GuestLedger ledger = GuestLedger.get(server);
		UUID me = player.getUUID();
		GuestFavorTesting.pinDay(player, 10);
		h.assertTrue(errand(player, kafka, a), "Day 10: A's errand is done" + path);
		h.assertTrue(ledger.affection(me, a) == GuestFavors.QUEST_REWARD, "and rewarded once" + path);

		GuestFavorTesting.pinDay(player, 11);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ordinaryGift(b), 2));
		h.assertTrue(GuestFavors.gift(player, miyabi, b).done(), "Day 11: a gift to B" + path);
		ItemStack dayEleven = GuestFavors.questItem(player, a);
		h.assertTrue(GuestFavors.questState(player, a) == GuestFavors.QuestState.OFFERED && same(dayEleven, GuestFavorTesting.offeredOn(a, player, 11)),
				"On day 11 A offers day 11's errand" + path);
		if (reload) {
			Tag saved = GuestFavorTesting.save(server);
			GuestFavorTesting.forget(server, me);
			h.assertTrue(GuestFavorTesting.effectiveDay(player) == 11 && GuestFavors.giftsLeft(player, b) == GuestFavors.GIFTS_PER_DAY,
					"Precondition: without the saved data nothing is known");
			GuestFavorTesting.load(server, saved, me);
		}

		GuestFavorTesting.pinDay(player, 10);
		h.assertTrue(GuestFavorTesting.day(player) == 10 && GuestFavorTesting.effectiveDay(player) == 11,
				"The clock reads day 10, but day 11 stays in force" + path);
		h.assertTrue(GuestFavors.questState(player, a) == GuestFavors.QuestState.OFFERED && same(GuestFavors.questItem(player, a), dayEleven)
				&& ledger.affection(me, a) == GuestFavors.QUEST_REWARD,
				"A offers no second day-10 errand, only day 11's, and going back granted nothing" + path);
		h.assertTrue(GuestFavors.giftsLeft(player, b) == GuestFavors.GIFTS_PER_DAY - 1, "B's day-11 gift still counts" + path);
		h.assertTrue(errand(player, kafka, a), "Day 11's errand for A can be done, once" + path);
		h.assertFalse(GuestFavors.acceptQuest(player, kafka, a).done() || GuestFavors.turnIn(player, kafka, a).done(),
				"and then no other" + path);

		GuestFavorTesting.pinDay(player, 11);
		h.assertFalse(GuestFavors.acceptQuest(player, kafka, a).done() || GuestFavors.turnIn(player, kafka, a).done(),
				"When the clock catches up to day 11 there is no fresh errand and no reward" + path);
		h.assertTrue(GuestFavors.questState(player, a) == GuestFavors.QuestState.DONE && ledger.affection(me, a) == 2 * GuestFavors.QUEST_REWARD,
				"Days 10 and 11 gave exactly one errand each: " + ledger.affection(me, a) + path);
	}

	/**
	 * Codex's cross-worker regression: with the player's ledger entry damaged ({@code guestSide} false) or both guests'
	 * entries damaged (true), through worker A's seams, the ledger records no gain, so a gift, a turn-in of an active
	 * errand ({@code guest}) and taking up an offered one ({@code offering}) are all refused: no item taken, no affection
	 * added, and the favors' saved records (and the high-water day) unchanged.
	 */
	private static void damagedLedgerTakesNothing(GameTestHelper h, String guest, String offering, boolean guestSide) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 3);
		UUID me = player.getUUID();
		String side = guestSide ? " with the guests' ledger entries damaged" : " with the player's ledger entry damaged";
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			Townsfolk npc = place(h, guest, 2, 2, made);
			Townsfolk other = place(h, offering, 2, 5, made);
			GuestLedger ledger = GuestLedger.get(server);
			h.assertTrue(GuestFavors.acceptQuest(player, npc, guest).done(), "Precondition: " + guest + "'s errand is active");
			ItemStack wanted = GuestFavors.questItem(player, guest);
			Item item = wanted.getItem();
			player.getInventory().setItem(9, new ItemStack(item, wanted.getCount() + 2));
			Item flower = ordinaryGift(guest);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(flower, 4));
			h.assertTrue(GuestFavors.questState(player, offering) == GuestFavors.QuestState.OFFERED
					&& ledger.canGainAffection(me, guest) && ledger.canGainAffection(me, offering),
					"Precondition: an errand offered, and gains possible before the damage");

			if (guestSide) {
				GuestLedgerTesting.damageGuest(server, guest);
				GuestLedgerTesting.damageGuest(server, offering);
			} else {
				GuestLedgerTesting.damagePlayer(server, me);
			}
			h.assertFalse(ledger.canGainAffection(me, guest) || ledger.canGainAffection(me, offering), "The ledger records no gain" + side);
			Snapshot before = Snapshot.of(player, ledger, guest, offering, flower, item);
			String record = GuestFavorTesting.record(player, guest);
			String otherRecord = GuestFavorTesting.record(player, offering);

			refusedAll(h, player, npc, guest, other, offering, side);
			h.assertTrue(Snapshot.of(player, ledger, guest, offering, flower, item).equals(before),
					"No item taken and no affection granted" + side);
			h.assertTrue(player.getMainHandItem().getCount() == 4 && plainCount(player, item) == wanted.getCount() + 2,
					"The gift and the errand's items are all still carried" + side);
			h.assertTrue(record.equals(GuestFavorTesting.record(player, guest)) && otherRecord.equals(GuestFavorTesting.record(player, offering))
					&& GuestFavors.questState(player, guest) == GuestFavors.QuestState.ACTIVE
					&& GuestFavors.questState(player, offering) == GuestFavors.QuestState.OFFERED,
					"The favors did not move on: " + GuestFavorTesting.record(player, guest) + side);
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	/** Takes up and turns in today's errand for {@code guest}, with exactly its items; whether both were taken. */
	private static boolean errand(ServerPlayer player, Townsfolk npc, String guest) {
		if (!GuestFavors.acceptQuest(player, npc, guest).done()) {
			return false;
		}
		player.getInventory().setItem(20, GuestFavors.questItem(player, guest).copy());
		return GuestFavors.turnIn(player, npc, guest).done() && player.getInventory().getItem(20).isEmpty();
	}

	/** Every entry point is refused: a gift and a turn-in for {@code guest}, taking up {@code offering}'s errand. */
	private static void refusedAll(GameTestHelper h, ServerPlayer player, Townsfolk npc, String guest, Townsfolk offeringNpc, String offering,
			String why) {
		h.assertFalse(GuestFavors.gift(player, npc, guest).done(), "No gift " + why);
		h.assertFalse(GuestFavors.turnIn(player, npc, guest).done(), "No turn-in " + why);
		h.assertFalse(GuestFavors.acceptQuest(player, offeringNpc, offering).done(), "No errand taken up " + why);
	}

	/** What a refused call must leave as it was: the gift and quest items carried, affection, gifts left and both errands. */
	private record Snapshot(int flowers, int quest, int affection, int otherAffection, int giftsLeft, GuestFavors.QuestState state,
			GuestFavors.QuestState otherState) {
		static Snapshot of(ServerPlayer player, GuestLedger ledger, String guest, String other, Item flower, Item item) {
			return new Snapshot(plainCount(player, flower), plainCount(player, item), ledger.affection(player.getUUID(), guest),
					ledger.affection(player.getUUID(), other), GuestFavors.giftsLeft(player, guest), GuestFavors.questState(player, guest),
					GuestFavors.questState(player, other));
		}
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
		h.assertTrue(bound, "The test's " + guest + " is its canonical entity (no other loaded entity holds the identity)");
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

	/** A gift the guest takes that is not one of its favourites (one of the first four flowers of the table). */
	private static Item ordinaryGift(String guest) {
		List<Item> favourites = GuestFavorTesting.favourites(guest);
		for (GuestFavors.Gift gift : GuestFavors.GIFTS) {
			if (!favourites.contains(gift.item())) {
				return gift.item();
			}
		}
		throw new IllegalStateException("No ordinary gift for " + guest);
	}

	private static boolean same(ItemStack a, ItemStack b) {
		return !a.isEmpty() && a.is(b.getItem()) && a.getCount() == b.getCount();
	}

	/** Unnamed {@code item} in the player's main inventory and offhand. */
	private static int plainCount(ServerPlayer player, Item item) {
		int total = 0;
		for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.is(item) && !stack.has(DataComponents.CUSTOM_NAME)) {
				total += stack.getCount();
			}
		}
		ItemStack offhand = player.getOffhandItem();
		return offhand.is(item) && !offhand.has(DataComponents.CUSTOM_NAME) ? total + offhand.getCount() : total;
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
