package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.guests.GuestActivity;
import io.github.jimbozoomer.jugcraft.town.guests.GuestBalance;
import io.github.jimbozoomer.jugcraft.town.guests.GuestDialogue;
import io.github.jimbozoomer.jugcraft.town.guests.GuestDialogueLines;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFavorTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFavors;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFeature;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedger;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedgerTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestMenu;
import io.github.jimbozoomer.jugcraft.town.guests.GuestPersonality;
import io.github.jimbozoomer.jugcraft.town.guests.GuestPersonalityErrands;
import io.github.jimbozoomer.jugcraft.town.guests.GuestPersonalityTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestResult;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRewards;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRivalries;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRoster;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRosterTesting;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * JZE guest expansion (docs/plans/jze-guest-expansion-nov11.md item 2): worker B's server tests for personality and
 * town comedy. Registered by the coordinator.
 *
 * <p>Every test uses its own fresh survival players and pins each player's day through {@link GuestFavorTesting}
 * (never the world's clock). Guests are synthetic {@code test_b_*} ids from {@link GuestRosterTesting}, so no real
 * roster id is bound; each test forgets its ids in the ledger first, binds its own canonical entities and discards
 * them when it ends. Every rivalry a test starts is ended in the same test, and nothing is left holding a guest.
 */
public class GuestPersonalityGameTests {
	private static final String REACT = GuestRosterTesting.guest("test_b_react", false);
	private static final String CHAT = GuestRosterTesting.guest("test_b_chat", false);
	private static final String BUDGET = GuestRosterTesting.guest("test_b_budget", false);
	private static final String ERRAND = GuestRosterTesting.guest("test_b_errand", false);
	private static final String MENU = GuestRosterTesting.guest("test_b_menu", false);
	private static final String BUSY_A = GuestRosterTesting.guest("test_b_busy_a", false);
	private static final String BUSY_B = GuestRosterTesting.guest("test_b_busy_b", false);
	private static final String END_A = GuestRosterTesting.guest("test_b_end_a", false);
	private static final String END_B = GuestRosterTesting.guest("test_b_end_b", false);
	private static final String PRIZE_A = GuestRosterTesting.guest("test_b_prize_a", false);
	private static final String PRIZE_B = GuestRosterTesting.guest("test_b_prize_b", false);
	private static final String HOLD_A = GuestRosterTesting.guest("test_b_hold_a", false);
	private static final String HOLD_B = GuestRosterTesting.guest("test_b_hold_b", false);
	private static final String HOLD_C = GuestRosterTesting.guest("test_b_hold_c", false);
	private static final String ENTRY = GuestRosterTesting.guest("test_b_entry", false);
	private static final String ENTRY_OTHER = GuestRosterTesting.guest("test_b_entry_other", false);
	/** May not marry: romance must never reach it, even with an adult's line table borrowed. */
	private static final String HELD = GuestRosterTesting.guest("test_b_held", false);
	/** May marry: the control for {@link #HELD}. */
	private static final String WED = GuestRosterTesting.guest("test_b_wed", true);

	/** The day pinned for each test's player: far from the overworld's own and from the favors tests' day. */
	private static final long DAY = 7_000L;
	/** Ticks between menu clicks: more than {@link GuestBalance#ACTION_GAP_TICKS}. */
	private static final int GAP = GuestBalance.ACTION_GAP_TICKS + 2;
	/** Words no line outside spouse banter may use, and no real person's line at all. */
	private static final List<String> ROMANCE = List.of("darling", "sweetheart", "kiss", "crush", "date", "flirt", "romance", "romantic",
			"marry", "married", "wedding", "husband", "wife", "boyfriend", "girlfriend", "partner", "dear", "love", "beloved");
	/** Phrases that would make a claim about a real person's own life, opinions or habits. */
	private static final List<String> PERSONAL = List.of("stream", "streaming", "twitch", "youtube", "subscribe", "followers", "politics",
			"political", "election", "congress", "vote", "my family", "my mom", "my dad", "my mother", "my father", "i always", "i love",
			"my favourite", "my favorite", "my job", "my channel", "my fans", "i grew up");

	// ---------------------------------------------------------------- gift reactions

	@GameTest(maxTicks = 20)
	public void giftReactionsNeverChangeAffectionOrItems(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 2);
		UUID me = player.getUUID();
		String guest = REACT;
		GuestLedgerTesting.reset(server, guest);
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		GuestPersonalityTesting.resetCooldowns(player);
		try {
			Townsfolk npc = place(h, guest, 2, 2, made);
			GuestLedger ledger = GuestLedger.get(server);
			Item flower = ordinaryGift(guest);
			int value = GuestFavorTesting.giftValue(guest, flower);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(flower, 5));
			ItemStack hand = player.getMainHandItem();

			// A real gift: the favors take one item and add its value; the GIFT_GIVEN listener only speaks.
			h.assertTrue(GuestFavors.gift(player, npc, guest).done(), "The gift is taken");
			String said = GuestPersonalityTesting.lastSaid(player);
			h.assertTrue(said != null && reaction(said), "The guest reacts with one gift line: " + said);
			h.assertTrue(hand.getCount() == 4 && ledger.affection(me, guest) == value,
					"One item and the gift's own value only: " + hand.getCount() + ", " + ledger.affection(me, guest));

			// A second gift at once is taken as usual, but the reaction is rate-limited.
			GuestPersonalityTesting.clearSaid(player);
			h.assertTrue(GuestFavors.gift(player, npc, guest).done(), "A second gift is taken");
			h.assertTrue(GuestPersonalityTesting.lastSaid(player) == null, "but draws no second reaction within the gap");
			h.assertTrue(hand.getCount() == 3 && ledger.affection(me, guest) == 2 * value, "Again one item and the gift's value only");

			// The listener called directly, for every slot and with a made-up gain: messages only.
			int affection = ledger.affection(me, guest);
			String record = GuestFavorTesting.record(player, guest);
			int gifts = GuestFavors.giftsLeft(player, guest);
			int left = GuestRewards.left(player, guest);
			for (String slot : List.of("favourite", "ordinary", "rare")) {
				GuestPersonalityTesting.resetCooldowns(player);
				GuestPersonalityTesting.forceReaction(player, slot);
				GuestPersonalityTesting.react(player, npc, guest, new ItemStack(flower), slot.equals("favourite"), 999);
				String line = GuestPersonalityTesting.lastSaid(player);
				h.assertTrue(line != null && line.contains("." + slot + "."), "A " + slot + " reaction is said: " + line);
			}
			h.assertTrue(ledger.affection(me, guest) == affection && hand.getCount() == 3 && plainCount(player, flower) == 3
					&& player.getMainHandItem() == hand, "Reactions changed no affection and no item");
			h.assertTrue(record.equals(GuestFavorTesting.record(player, guest)) && GuestFavors.giftsLeft(player, guest) == gifts
					&& GuestRewards.left(player, guest) == left, "nor the day's gifts, favors record or shared budget");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
			GuestPersonalityTesting.resetCooldowns(player);
		}
	}

	// ---------------------------------------------------------------- banter and chat

	@GameTest(maxTicks = 20)
	public void chatAndGreetingsHaveCooldownsAndChatGrantsOnceADay(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 2);
		UUID me = player.getUUID();
		String guest = CHAT;
		GuestLedgerTesting.reset(server, guest);
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		GuestPersonalityTesting.resetCooldowns(player);
		try {
			Townsfolk npc = place(h, guest, 2, 2, made);
			GuestLedger ledger = GuestLedger.get(server);
			int left = GuestRewards.left(player, guest);
			h.assertTrue(left == GuestFavors.DAILY_AFFECTION_CAP && ledger.affection(me, guest) == 0, "Precondition: a fresh player and day");

			GuestResult first = GuestDialogue.chat(player, npc, guest);
			String said = GuestPersonalityTesting.lastSaid(player);
			h.assertTrue(first.done() && said != null && said.contains(".banter."), "A chat says one banter line: " + said);
			h.assertTrue(ledger.affection(me, guest) == GuestDialogue.CHAT_AFFECTION && GuestRewards.left(player, guest) == left - GuestDialogue.CHAT_AFFECTION
					&& GuestPersonalityTesting.chatGranted(player, guest), "The first chat of the day grants its small amount, from the shared budget");

			GuestPersonalityTesting.clearSaid(player);
			h.assertFalse(GuestDialogue.chat(player, npc, guest).done(), "A chat straight after is refused by the cooldown");
			h.assertTrue(GuestPersonalityTesting.lastSaid(player) == null && ledger.affection(me, guest) == GuestDialogue.CHAT_AFFECTION,
					"and says and grants nothing");

			GuestPersonalityTesting.resetCooldowns(player);
			h.assertTrue(GuestPersonalityTesting.feature().available(player, npc, guest, "chat"), "The chat option is offered beside the guest");
			GuestResult again = GuestPersonalityTesting.feature().act(player, npc, guest, "chat");
			h.assertTrue(again.done() && GuestPersonalityTesting.lastSaid(player) != null, "After the cooldown the chat option speaks again");
			h.assertTrue(ledger.affection(me, guest) == GuestDialogue.CHAT_AFFECTION, "but grants nothing more the same day");

			// Greetings when talked to: one line, then none within the greeting cooldown; never any affection.
			GuestPersonalityTesting.resetCooldowns(player);
			GuestPersonalityTesting.greet(player, npc, guest);
			h.assertTrue(GuestPersonalityTesting.lastSaid(player) != null, "Talking to the guest draws a greeting");
			GuestPersonalityTesting.clearSaid(player);
			GuestPersonalityTesting.greet(player, npc, guest);
			h.assertTrue(GuestPersonalityTesting.lastSaid(player) == null, "but not a second within the greeting cooldown");
			h.assertTrue(ledger.affection(me, guest) == GuestDialogue.CHAT_AFFECTION, "Greetings grant nothing");

			// The next day the chat grants once more; a clock moved back to the earlier day grants nothing.
			GuestFavorTesting.pinDay(player, DAY + 1);
			GuestPersonalityTesting.resetCooldowns(player);
			h.assertTrue(!GuestPersonalityTesting.chatGranted(player, guest) && GuestDialogue.chat(player, npc, guest).done(), "A chat on the next day");
			h.assertTrue(ledger.affection(me, guest) == 2 * GuestDialogue.CHAT_AFFECTION, "grants once more");
			GuestFavorTesting.pinDay(player, DAY);
			GuestPersonalityTesting.resetCooldowns(player);
			h.assertTrue(GuestPersonalityTesting.day(player) == DAY + 1 && GuestDialogue.chat(player, npc, guest).done()
					&& ledger.affection(me, guest) == 2 * GuestDialogue.CHAT_AFFECTION, "A clock moved back grants no second chat that day");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
			GuestPersonalityTesting.resetCooldowns(player);
		}
	}

	@GameTest(maxTicks = 20)
	public void aGiftThatUsesTheDailyBudgetLeavesChatGrantingNothing(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 2);
		UUID me = player.getUUID();
		String guest = BUDGET;
		GuestLedgerTesting.reset(server, guest);
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		GuestPersonalityTesting.resetCooldowns(player);
		try {
			Townsfolk npc = place(h, guest, 2, 2, made);
			GuestLedger ledger = GuestLedger.get(server);
			GuestFavorTesting.gainedToday(player, guest, GuestFavors.DAILY_AFFECTION_CAP - 1);
			Item flower = ordinaryGift(guest);
			h.assertTrue(GuestFavorTesting.giftValue(guest, flower) > 1, "Precondition: the gift is worth more than is left of the budget");
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(flower, 2));
			h.assertTrue(GuestFavors.gift(player, npc, guest).done(), "A gift takes the last of today's budget");
			h.assertTrue(ledger.affection(me, guest) == 1 && GuestRewards.left(player, guest) == 0, "Precondition: the budget is used");

			GuestResult chat = GuestDialogue.chat(player, npc, guest);
			h.assertTrue(chat.done() && GuestPersonalityTesting.lastSaid(player) != null, "The guest still chats");
			h.assertTrue(ledger.affection(me, guest) == 1 && GuestRewards.left(player, guest) == 0, "but the chat grants nothing beyond the budget");
			h.assertFalse(GuestPersonalityTesting.chatGranted(player, guest), "and its daily grant is not used up by granting nothing");

			GuestFavorTesting.pinDay(player, DAY + 1);
			GuestPersonalityTesting.resetCooldowns(player);
			h.assertTrue(GuestDialogue.chat(player, npc, guest).done() && ledger.affection(me, guest) == 1 + GuestDialogue.CHAT_AFFECTION,
					"On the next day's fresh budget the chat grants again");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
			GuestPersonalityTesting.resetCooldowns(player);
		}
	}

	// ---------------------------------------------------------------- errands

	@GameTest(maxTicks = 20)
	public void anErrandTakesExactlyTheCountOnce(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 2);
		UUID me = player.getUUID();
		String guest = ERRAND;
		GuestLedgerTesting.reset(server, guest);
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		GuestPersonalityTesting.resetCooldowns(player);
		try {
			Townsfolk npc = place(h, guest, 2, 2, made);
			GuestLedger ledger = GuestLedger.get(server);
			ItemStack wanted = GuestPersonalityErrands.wanted(player, guest);
			Item item = wanted.getItem();
			int count = wanted.getCount();
			h.assertTrue(!wanted.isEmpty() && count >= 4 && "OFFERED".equals(GuestPersonalityTesting.errandState(player, guest)),
					"Today's errand is offered: " + wanted);
			player.getInventory().setItem(9, new ItemStack(item, count - 1));
			ItemStack few = player.getInventory().getItem(9);

			h.assertFalse(GuestPersonalityErrands.turnIn(player, npc, guest).done(), "Nothing is turned in before the errand is taken up");
			h.assertTrue(few.getCount() == count - 1, "and nothing is taken");
			h.assertTrue(GuestPersonalityErrands.accept(player, npc, guest).done() && "ACTIVE".equals(GuestPersonalityTesting.errandState(player, guest)),
					"The errand is taken up");
			h.assertTrue(same(GuestPersonalityErrands.wanted(player, guest), wanted), "It is the errand that was offered");
			h.assertFalse(GuestPersonalityErrands.accept(player, npc, guest).done(), "Taking it up again changes nothing");
			String record = GuestPersonalityTesting.errandRecord(player, guest);

			h.assertFalse(GuestPersonalityErrands.turnIn(player, npc, guest).done(), "One short is refused");
			h.assertTrue(few.getCount() == count - 1 && ledger.affection(me, guest) == 0 && record.equals(GuestPersonalityTesting.errandRecord(player, guest)),
					"and takes nothing, grants nothing and moves nothing on");

			// count + 3 plain, across two main stacks and the offhand, and a named keepsake that is never counted or taken.
			player.getInventory().setItem(10, new ItemStack(item, 2));
			player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(item, 2));
			ItemStack keepsake = new ItemStack(item, 5);
			keepsake.set(DataComponents.CUSTOM_NAME, Component.literal("Keepsake"));
			player.getInventory().setItem(35, keepsake);
			ItemStack kept = player.getInventory().getItem(35);
			h.assertTrue(plainCount(player, item) == count + 3, "Precondition: three more than asked, plain");
			int left = GuestRewards.left(player, guest);

			h.assertTrue(GuestPersonalityErrands.turnIn(player, npc, guest).done(), "The turn-in is taken");
			h.assertTrue(plainCount(player, item) == 3 && kept.getCount() == 5 && kept.has(DataComponents.CUSTOM_NAME),
					"Exactly the count was taken; the keepsake is untouched: " + plainCount(player, item));
			h.assertTrue(ledger.affection(me, guest) == GuestPersonalityErrands.ERRAND_REWARD
					&& GuestRewards.left(player, guest) == left - GuestPersonalityErrands.ERRAND_REWARD, "Its reward is granted once, from the shared budget");
			h.assertTrue("DONE".equals(GuestPersonalityTesting.errandState(player, guest)), "The errand is done for the day");

			h.assertFalse(GuestPersonalityErrands.turnIn(player, npc, guest).done(), "A replayed turn-in is refused");
			h.assertFalse(GuestPersonalityErrands.accept(player, npc, guest).done(), "and no second errand is offered today");
			h.assertTrue(plainCount(player, item) == 3 && ledger.affection(me, guest) == GuestPersonalityErrands.ERRAND_REWARD, "Neither took or granted anything");

			GuestFavorTesting.pinDay(player, DAY - 1);
			h.assertTrue("DONE".equals(GuestPersonalityTesting.errandState(player, guest)) && !GuestPersonalityErrands.accept(player, npc, guest).done(),
					"A clock moved back offers no earlier day's errand");

			// The next day: a fresh errand. With the day's shared budget used, a turn-in takes nothing and the errand waits.
			GuestFavorTesting.pinDay(player, DAY + 1);
			h.assertTrue("OFFERED".equals(GuestPersonalityTesting.errandState(player, guest)), "The next day offers a new errand");
			h.assertTrue(GuestPersonalityErrands.accept(player, npc, guest).done(), "and it is taken up");
			ItemStack next = GuestPersonalityErrands.wanted(player, guest);
			player.getInventory().setItem(20, next.copy());
			int carried = plainCount(player, next.getItem());
			GuestFavorTesting.gainedToday(player, guest, GuestFavors.DAILY_AFFECTION_CAP);
			int affection = ledger.affection(me, guest);
			h.assertFalse(GuestPersonalityErrands.turnIn(player, npc, guest).done(), "With the day's budget used, a turn-in is refused");
			h.assertTrue(plainCount(player, next.getItem()) == carried && ledger.affection(me, guest) == affection
					&& "ACTIVE".equals(GuestPersonalityTesting.errandState(player, guest)), "and takes nothing");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	@GameTest(maxTicks = 100)
	public void aReplayedErrandClickTakesNothing(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 2);
		UUID me = player.getUUID();
		String guest = MENU;
		GuestLedgerTesting.reset(server, guest);
		List<Townsfolk> made = new ArrayList<>();
		Townsfolk npc = place(h, guest, 2, 2, made);
		GuestLedger ledger = GuestLedger.get(server);
		GuestFavorTesting.pinDay(player, DAY);
		ItemStack wanted = GuestPersonalityErrands.wanted(player, guest);
		Item item = wanted.getItem();
		int count = wanted.getCount();
		player.getInventory().setItem(9, new ItemStack(item, count + 4));
		int accept = GuestPersonalityTesting.optionIndex("errand");
		int turnIn = GuestPersonalityTesting.optionIndex("errand_turn_in");

		GuestMenu.open(player, npc, guest);
		h.assertTrue(player.containerMenu instanceof GuestMenu, "Talking opens the guest's menu");
		GuestMenu menu = (GuestMenu) player.containerMenu;
		h.assertTrue(menu.optionAvailable(accept) && !menu.optionAvailable(turnIn), "The errand is offered, not yet to hand over");
		h.assertTrue(menu.clickMenuButton(player, GuestMenu.option(accept, menu.get(GuestMenu.REVISION))), "The errand is taken up through the menu");
		h.assertTrue("ACTIVE".equals(GuestPersonalityTesting.errandState(player, guest)), "and is active");

		int[] click = new int[1];
		h.runAfterDelay(GAP, () -> {
			h.assertTrue(menu.optionAvailable(turnIn) && !menu.optionAvailable(accept), "Now it can be handed over");
			click[0] = GuestMenu.option(turnIn, menu.get(GuestMenu.REVISION));
			h.assertTrue(menu.clickMenuButton(player, click[0]), "The turn-in through the menu");
			h.assertTrue(player.getInventory().getItem(9).getCount() == 4 && "DONE".equals(GuestPersonalityTesting.errandState(player, guest)),
					"takes exactly the count and finishes the errand");
			h.assertFalse(menu.clickMenuButton(player, click[0]), "The same click sent again is refused");
			h.assertTrue(player.getInventory().getItem(9).getCount() == 4, "and takes nothing");
		});
		h.runAfterDelay(2L * GAP, () -> {
			h.assertFalse(menu.clickMenuButton(player, click[0]), "The same click replayed later is refused");
			player.closeContainer();
			GuestMenu.open(player, npc, guest);
			h.assertTrue(player.containerMenu instanceof GuestMenu && player.containerMenu != menu, "A new menu");
			GuestMenu again = (GuestMenu) player.containerMenu;
			h.assertFalse(again.clickMenuButton(player, click[0]), "The old click on the new menu is refused");
			h.assertFalse(again.clickMenuButton(player, GuestMenu.option(turnIn, again.get(GuestMenu.REVISION))),
					"and a fresh turn-in click finds the errand done");
			h.assertTrue(player.getInventory().getItem(9).getCount() == 4 && ledger.affection(me, guest) == GuestPersonalityErrands.ERRAND_REWARD,
					"Items were taken and affection granted exactly once");
			player.closeContainer();
			clear(made);
			GuestFavorTesting.unpin(me);
			h.succeed();
		});
	}

	// ---------------------------------------------------------------- rivalries

	@GameTest(maxTicks = 20)
	public void aRivalryIsRefusedWhileEitherGuestIsBusy(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 3, 4);
		UUID me = player.getUUID();
		GuestLedgerTesting.reset(server, BUSY_A, BUSY_B);
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		GuestActivity activity = GuestActivity.get(server);
		UUID someone = UUID.randomUUID();
		try {
			Townsfolk a = place(h, BUSY_A, 1, 2, made);
			Townsfolk b = place(h, BUSY_B, 5, 2, made);
			GuestFeature feature = GuestPersonalityTesting.feature();

			h.assertTrue(activity.begin(BUSY_B, new GuestActivity.Activity("outing", someone, 0L, h.getLevel().dimension(), b.blockPosition())),
					"Precondition: the rival is out on an outing");
			h.assertFalse(GuestPersonalityTesting.startRivalry(player, a, BUSY_A, "RACE", b).done(), "No rivalry with a busy rival");
			h.assertTrue(activity.current(BUSY_A) == null && "outing".equals(kind(activity, BUSY_B)) && !GuestPersonalityTesting.running(player),
					"Nothing is held, and the outing is untouched");
			h.assertTrue(activity.end(BUSY_B, "outing"), "The outing ends");

			h.assertTrue(activity.begin(BUSY_A, new GuestActivity.Activity("visit", someone, 0L, h.getLevel().dimension(), a.blockPosition())),
					"Precondition: the challenger is on a visit");
			h.assertFalse(feature.available(player, a, BUSY_A, "rivalry"), "The rivalry option is not offered for a busy guest");
			h.assertFalse(GuestPersonalityTesting.startRivalry(player, a, BUSY_A, "STARE", b).done(), "No rivalry for a busy challenger");
			h.assertTrue("visit".equals(kind(activity, BUSY_A)) && activity.current(BUSY_B) == null && !GuestPersonalityTesting.running(player),
					"Nothing is held, and the visit is untouched");
			h.assertTrue(activity.end(BUSY_A, "visit"), "The visit ends");

			h.assertTrue(feature.available(player, a, BUSY_A, "rivalry"), "Both free: the option is offered");
			h.assertTrue(GuestPersonalityTesting.startRivalry(player, a, BUSY_A, "STARE", b).done(), "and a rivalry starts");
			h.assertTrue(GuestRivalries.KIND.equals(kind(activity, BUSY_A)) && GuestRivalries.KIND.equals(kind(activity, BUSY_B))
					&& me.equals(Objects.requireNonNull(activity.current(BUSY_A)).player()), "Both guests are held for the player");
			h.assertFalse(feature.available(player, a, BUSY_A, "rivalry"), "No second rivalry is offered while one runs");
			h.assertFalse(GuestPersonalityTesting.startRivalry(player, a, BUSY_A, "RACE", b).done(), "nor started");

			ServerPlayer other = visitor(h, 4, 4);
			GuestFavorTesting.pinDay(other, DAY);
			h.assertFalse(GuestPersonalityTesting.startRivalry(other, b, BUSY_B, "RACE", a).done(), "Another player cannot start one with the held guests");
			h.assertTrue(GuestRivalries.KIND.equals(kind(activity, BUSY_A)) && me.equals(Objects.requireNonNull(activity.current(BUSY_B)).player())
					&& !GuestPersonalityTesting.running(other), "and the first rivalry still holds both");
			GuestFavorTesting.unpin(other.getUUID());
			h.succeed();
		} finally {
			GuestPersonalityTesting.stop(server, me);
			activity.end(BUSY_A, "outing");
			activity.end(BUSY_A, "visit");
			activity.end(BUSY_B, "outing");
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	/**
	 * November 12 review: a player spending time with one guest (an outing, a visit, a ceremony) does not also start a
	 * rivalry with others, as outings already require (one at a time). Refused and not offered, holding nothing, until
	 * that activity ends.
	 */
	@GameTest(maxTicks = 20)
	public void aRivalryWaitsWhileThePlayerSpendsTimeWithAnotherGuest(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 3, 4);
		UUID me = player.getUUID();
		GuestLedgerTesting.reset(server, HOLD_A, HOLD_B, HOLD_C);
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		GuestActivity activity = GuestActivity.get(server);
		try {
			Townsfolk a = place(h, HOLD_A, 1, 2, made);
			Townsfolk b = place(h, HOLD_B, 5, 2, made);
			Townsfolk c = place(h, HOLD_C, 3, 6, made);
			GuestFeature feature = GuestPersonalityTesting.feature();

			h.assertTrue(activity.begin(HOLD_C, new GuestActivity.Activity("outing", me, 0L, h.getLevel().dimension(), c.blockPosition())),
					"Precondition: the player is on an outing with a third guest");
			h.assertFalse(feature.available(player, a, HOLD_A, "rivalry"), "No rivalry is offered while the player spends time with another guest");
			GuestResult refused = GuestPersonalityTesting.startRivalry(player, a, HOLD_A, "STARE", b);
			h.assertFalse(refused.done(), "nor started: " + refused.message().getString());
			h.assertTrue(activity.current(HOLD_A) == null && activity.current(HOLD_B) == null && "outing".equals(kind(activity, HOLD_C))
					&& !GuestPersonalityTesting.running(player), "Nothing is held, and the outing is untouched");

			h.assertTrue(activity.end(HOLD_C, "outing"), "The outing ends");
			h.assertTrue(feature.available(player, a, HOLD_A, "rivalry"), "Free again: the option is offered");
			h.assertTrue(GuestPersonalityTesting.startRivalry(player, a, HOLD_A, "STARE", b).done(), "and the rivalry starts");
			h.succeed();
		} finally {
			GuestPersonalityTesting.stop(server, me);
			activity.end(HOLD_C, "outing");
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	@GameTest(maxTicks = 20)
	public void cancelTimeoutUnloadAndLogoutEndTheRivalry(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 3, 4);
		UUID me = player.getUUID();
		GuestLedgerTesting.reset(server, END_A, END_B);
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		GuestActivity activity = GuestActivity.get(server);
		GuestLedger ledger = GuestLedger.get(server);
		try {
			Townsfolk a = place(h, END_A, 1, 2, made);
			Townsfolk b = place(h, END_B, 5, 2, made);
			GuestFeature feature = GuestPersonalityTesting.feature();

			// Cancelled through the option: both released, nothing granted.
			h.assertTrue(GuestPersonalityTesting.startRivalry(player, a, END_A, "RACE", b).done(), "A race starts");
			h.assertTrue(feature.available(player, a, END_A, "rivalry_cancel") && feature.available(player, b, END_B, "rivalry_cancel"),
					"Calling it off is offered beside either guest");
			h.assertTrue(feature.act(player, b, END_B, "rivalry_cancel").done(), "and it is called off");
			released(h, activity, player, "after cancelling");
			h.assertTrue(ledger.affection(me, END_A) == 0 && ledger.affection(me, END_B) == 0 && !GuestPersonalityTesting.rivalryRewarded(player),
					"A cancelled rivalry grants nothing");

			// A race nobody finishes ends at its time limit.
			h.assertTrue(GuestPersonalityTesting.startRivalry(player, a, END_A, "RACE", b).done(), "A second race starts");
			GuestPersonalityTesting.tick(server);
			h.assertTrue(GuestPersonalityTesting.running(player), "Precondition: still running before its time is up (nobody has arrived)");
			GuestPersonalityTesting.expire(player);
			GuestPersonalityTesting.tick(server);
			released(h, activity, player, "after the time limit");

			// A guest removed (unloaded) mid-contest ends it.
			h.assertTrue(GuestPersonalityTesting.startRivalry(player, a, END_A, "CREDIT", b).done(), "A credit contest starts");
			b.discard();
			GuestPersonalityTesting.tick(server);
			released(h, activity, player, "after the rival was removed");
			Townsfolk b2 = place(h, END_B, 5, 2, made);

			// Disconnecting ends it (the disconnect listener's work), and so does a player no longer in the game.
			h.assertTrue(GuestPersonalityTesting.startRivalry(player, a, END_A, "STARE", b2).done(), "A staring contest starts");
			GuestPersonalityTesting.loggedOut(server, me);
			released(h, activity, player, "after the disconnect");
			h.assertTrue(GuestPersonalityTesting.startRivalry(player, a, END_A, "STARE", b2).done(), "Another staring contest starts");
			TestPlayers.leave(player);
			GuestPersonalityTesting.tick(server);
			released(h, activity, player, "after the player left the game");

			// After a restart nothing is running, and any saved rivalry hold is ended; other activities are not.
			UUID someone = UUID.randomUUID();
			h.assertTrue(activity.begin(END_A, new GuestActivity.Activity(GuestRivalries.KIND, someone, 0L, h.getLevel().dimension(), a.blockPosition()))
					&& activity.begin(END_B, new GuestActivity.Activity("outing", someone, 0L, h.getLevel().dimension(), b2.blockPosition())),
					"Precondition: a stale rivalry hold and an outing, as a restart might find them");
			GuestPersonalityTesting.recover(server);
			h.assertTrue(activity.current(END_A) == null && "outing".equals(kind(activity, END_B)), "Recovery ends the stale rivalry only");
			activity.end(END_B, "outing");
			h.assertTrue(ledger.affection(me, END_A) == 0 && ledger.affection(me, END_B) == 0, "None of these endings granted anything");
			h.succeed();
		} finally {
			GuestPersonalityTesting.stop(server, me);
			activity.end(END_A, GuestRivalries.KIND);
			activity.end(END_B, GuestRivalries.KIND);
			activity.end(END_B, "outing");
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	@GameTest(maxTicks = 20)
	public void aRivalryRewardIsSmallAndOncePerDay(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 3, 4);
		UUID me = player.getUUID();
		GuestLedgerTesting.reset(server, PRIZE_A, PRIZE_B);
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		GuestActivity activity = GuestActivity.get(server);
		GuestLedger ledger = GuestLedger.get(server);
		try {
			Townsfolk a = place(h, PRIZE_A, 1, 2, made);
			Townsfolk b = place(h, PRIZE_B, 5, 2, made);
			int reward = GuestRivalries.REWARD;
			h.assertTrue(reward > 0 && reward <= 5, "The reward is small: " + reward);

			h.assertTrue(GuestPersonalityTesting.startRivalry(player, a, PRIZE_A, "STARE", b).done(), "A staring contest starts");
			GuestPersonalityTesting.expire(player);
			GuestPersonalityTesting.tick(server);
			released(h, activity, player, "after it finished");
			h.assertTrue(ledger.affection(me, PRIZE_A) == reward && ledger.affection(me, PRIZE_B) == reward
					&& GuestRewards.left(player, PRIZE_A) == GuestFavors.DAILY_AFFECTION_CAP - reward, "Each guest grants the small reward once, from the budget");

			h.assertTrue(GuestPersonalityTesting.startRivalry(player, a, PRIZE_A, "CREDIT", b).done(), "A second rivalry the same day");
			GuestPersonalityTesting.expire(player);
			GuestPersonalityTesting.tick(server);
			released(h, activity, player, "after the second finished");
			h.assertTrue(ledger.affection(me, PRIZE_A) == reward && ledger.affection(me, PRIZE_B) == reward, "grants nothing more");

			GuestFavorTesting.pinDay(player, DAY + 1);
			GuestFavorTesting.gainedToday(player, PRIZE_B, GuestFavors.DAILY_AFFECTION_CAP - 1);
			h.assertTrue(GuestPersonalityTesting.startRivalry(player, a, PRIZE_A, "RACE", b).done(), "A race the next day");
			GuestPersonalityTesting.expire(player);
			GuestPersonalityTesting.tick(server);
			released(h, activity, player, "after the race");
			h.assertTrue(ledger.affection(me, PRIZE_A) == 2 * reward && ledger.affection(me, PRIZE_B) == reward + 1,
					"grants again, but never past the shared budget: " + ledger.affection(me, PRIZE_B));
			h.succeed();
		} finally {
			GuestPersonalityTesting.stop(server, me);
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	// ---------------------------------------------------------------- entry points

	@GameTest(maxTicks = 20)
	public void entryPointsRefuseFromAfarDuplicatesAndTheWrongGuest(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		ServerPlayer player = visitor(h, 4, 3);
		UUID me = player.getUUID();
		GuestLedgerTesting.reset(server, ENTRY, ENTRY_OTHER);
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		GuestPersonalityTesting.resetCooldowns(player);
		try {
			Townsfolk npc = place(h, ENTRY, 2, 2, made);
			Townsfolk other = place(h, ENTRY_OTHER, 2, 5, made);
			GuestLedger ledger = GuestLedger.get(server);
			GuestActivity activity = GuestActivity.get(server);
			h.assertTrue(GuestPersonalityErrands.accept(player, npc, ENTRY).done(), "Precondition: an errand is active beside the guest");
			ItemStack wanted = GuestPersonalityErrands.wanted(player, ENTRY);
			player.getInventory().setItem(9, new ItemStack(wanted.getItem(), wanted.getCount() + 2));
			GuestPersonalityTesting.resetCooldowns(player);
			GuestPersonalityTesting.clearSaid(player);
			String before = state(player, ledger, activity, wanted.getItem());

			// Out of range.
			Vec3 home = player.position();
			Vec3 far = centre(h, 24, 3);
			player.snapTo(far.x, far.y, far.z, 90.0F, 0.0F);
			refusedAll(h, player, npc, ENTRY, other, ENTRY_OTHER, "out of range");
			h.assertTrue(before.equals(state(player, ledger, activity, wanted.getItem())), "Out of range: nothing changed");
			player.snapTo(home.x, home.y, home.z, 90.0F, 0.0F);

			// A spectator.
			player.setGameMode(GameType.SPECTATOR);
			refusedAll(h, player, npc, ENTRY, other, ENTRY_OTHER, "as a spectator");
			player.setGameMode(GameType.SURVIVAL);
			h.assertTrue(before.equals(state(player, ledger, activity, wanted.getItem())), "A spectator: nothing changed");

			// Duplicates: entities carrying the ids that are not the canonical ones.
			Townsfolk copy = stand(h, ENTRY, 5, 5, made);
			Townsfolk otherCopy = stand(h, ENTRY_OTHER, 5, 1, made);
			h.assertTrue(!copy.getUUID().equals(ledger.canonical(ENTRY)) && !otherCopy.getUUID().equals(ledger.canonical(ENTRY_OTHER)),
					"Precondition: the copies are not canonical");
			refusedAll(h, player, copy, ENTRY, otherCopy, ENTRY_OTHER, "with a duplicate");
			h.assertTrue(before.equals(state(player, ledger, activity, wanted.getItem())), "A duplicate: nothing changed");

			// The wrong guest id: each canonical entity asked as the other guest.
			refusedAll(h, player, other, ENTRY, npc, ENTRY_OTHER, "with the wrong guest id");
			h.assertTrue(before.equals(state(player, ledger, activity, wanted.getItem())), "The wrong guest id: nothing changed");

			// The control: beside the canonical guest, a chat is taken.
			h.assertTrue(GuestDialogue.chat(player, npc, ENTRY).done() && GuestPersonalityTesting.lastSaid(player) != null
					&& ledger.affection(me, ENTRY) == GuestDialogue.CHAT_AFFECTION, "Beside the canonical guest the chat is taken");
			h.succeed();
		} finally {
			GuestPersonalityTesting.stop(server, me);
			clear(made);
			GuestFavorTesting.unpin(me);
			GuestPersonalityTesting.resetCooldowns(player);
		}
	}

	// ---------------------------------------------------------------- content

	@GameTest(maxTicks = 20)
	public void realPeopleSpeakOnlyGenericLinesAndPendingGuestsNone(GameTestHelper h) {
		Set<String> tokens = nameTokens();
		h.assertTrue(tokens.contains("emiru") && tokens.contains("frieren") && tokens.contains("rooni"), "Precondition: every roster name is checked");
		int real = 0;
		for (GuestRoster.Guest guest : GuestRoster.ALL) {
			String id = guest.id();
			if (guest.pending()) {
				h.assertTrue(GuestPersonality.profile(id) == null && !GuestPersonalityTesting.authored().contains(id),
						"A pending guest has no profile and no lines: " + id);
			} else if (guest.origin() == GuestRoster.Origin.REAL_PERSON) {
				real++;
				h.assertTrue(GuestPersonalityTesting.usesGeneric(id) && !GuestPersonalityTesting.authored().contains(id),
						"A real person uses exactly the generic lines, errands and profile: " + id);
				h.assertFalse(GuestPersonalityTesting.mayCompete(id), "A real person never takes part in a rivalry: " + id);
			} else {
				h.assertTrue(GuestPersonality.profile(id) != null && GuestPersonalityTesting.authored().contains(id)
						&& GuestPersonalityTesting.mayCompete(id), "A fictional guest has a profile and lines of its own: " + id);
			}
		}
		h.assertTrue(real == 6, "The six real people are checked: " + real);
		h.assertTrue(GuestPersonality.GENERIC.generic() && GuestPersonality.GENERIC.rivals().isEmpty(), "The generic profile names no rival");

		List<GuestDialogueLines.Line> generic = GuestPersonalityTesting.genericLines();
		h.assertTrue(generic.size() >= 20, "Precondition: the generic set is read: " + generic.size());
		for (GuestDialogueLines.Line line : generic) {
			String text = line.english().toLowerCase(Locale.ROOT);
			h.assertFalse(line.romantic() || line.when().contains(GuestDialogueLines.Cue.MARRIED), "No generic line is romantic: " + line.key());
			for (String token : tokens) {
				h.assertFalse(word(text, token), "A real person's line names nobody (" + token + "): " + line.key());
			}
			for (String word : ROMANCE) {
				h.assertFalse(word(text, word), "A real person's line has no romance (" + word + "): " + line.key());
			}
			for (String phrase : PERSONAL) {
				h.assertFalse(word(text, phrase), "A real person's line claims nothing personal (" + phrase + "): " + line.key());
			}
			h.assertFalse(text.indexOf('%') >= 0, "A real person's line carries no inserted text: " + line.key());
		}
		h.succeed();
	}

	@GameTest(maxTicks = 20)
	public void romanticLinesNeedMayMarryAndALedgerMarriage(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		Set<GuestDialogueLines.Cue> every = EnumSet.allOf(GuestDialogueLines.Cue.class);
		Set<GuestDialogueLines.Cue> unmarried = EnumSet.allOf(GuestDialogueLines.Cue.class);
		unmarried.remove(GuestDialogueLines.Cue.MARRIED);

		// Data: no guest that may not marry has a romantic line, and none can be chosen in any moment; nor for anyone,
		// outside spouse banter, a romance word.
		int held = 0;
		for (GuestRoster.Guest guest : GuestRoster.ALL) {
			if (!guest.available()) {
				continue;
			}
			String id = guest.id();
			for (GuestDialogueLines.Line line : GuestPersonalityTesting.allLines(id)) {
				h.assertTrue(line.romantic() == line.when().contains(GuestDialogueLines.Cue.MARRIED), "Romantic exactly when a spouse line: " + line.key());
				h.assertFalse(line.romantic() && !guest.mayMarry(), "No romantic line for a guest held from marriage: " + line.key());
				if (!line.romantic()) {
					String text = line.english().toLowerCase(Locale.ROOT);
					for (String word : ROMANCE) {
						h.assertFalse(word(text, word), "Only spouse banter speaks of romance (" + word + "): " + line.key());
					}
				}
			}
			if (!guest.mayMarry()) {
				held++;
				h.assertTrue(GuestPersonalityTesting.allCandidates(id, every).stream().noneMatch(GuestDialogueLines.Line::romantic),
						"Nothing romantic can be chosen for " + id);
			}
		}
		h.assertTrue(held >= 14, "The held guests are checked: " + held);

		// Synthetic guests speaking an adult's table: the gate holds by mayMarry and the MARRIED cue, not by the table.
		ServerPlayer player = visitor(h, 4, 3);
		UUID me = player.getUUID();
		GuestLedgerTesting.reset(server, HELD, WED);
		GuestPersonalityTesting.borrow(HELD, "misato_katsuragi");
		GuestPersonalityTesting.borrow(WED, "misato_katsuragi");
		List<Townsfolk> made = new ArrayList<>();
		GuestFavorTesting.pinDay(player, DAY);
		GuestPersonalityTesting.resetCooldowns(player);
		try {
			h.assertTrue(GuestPersonalityTesting.allLines(HELD).stream().anyMatch(GuestDialogueLines.Line::romantic),
					"Precondition: the borrowed table has a spouse line");
			for (Set<GuestDialogueLines.Cue> cues : List.<Set<GuestDialogueLines.Cue>>of(Set.of(), Set.of(GuestDialogueLines.Cue.MARRIED),
					EnumSet.of(GuestDialogueLines.Cue.EVENING, GuestDialogueLines.Cue.MARRIED), every, unmarried)) {
				h.assertTrue(GuestPersonalityTesting.allCandidates(HELD, cues).stream().noneMatch(GuestDialogueLines.Line::romantic),
						"Held from marriage: nothing romantic with " + cues);
				boolean spouse = GuestPersonalityTesting.banterCandidates(WED, cues).stream().anyMatch(GuestDialogueLines.Line::romantic);
				h.assertTrue(spouse == cues.contains(GuestDialogueLines.Cue.MARRIED), "May marry: spouse lines exactly when married, " + cues);
			}

			// Live: only the ledger's marriage makes the cue, and the ledger refuses the held guest.
			Townsfolk heldNpc = place(h, HELD, 2, 2, made);
			Townsfolk wedNpc = place(h, WED, 2, 5, made);
			GuestLedger ledger = GuestLedger.get(server);
			h.assertFalse(GuestPersonalityTesting.context(player, wedNpc, WED).contains(GuestDialogueLines.Cue.MARRIED), "Unmarried: no MARRIED cue");
			GuestLedgerTesting.setAffection(ledger, me, HELD, GuestBalance.MAX_AFFECTION);
			GuestLedgerTesting.setAffection(ledger, me, WED, GuestBalance.MAX_AFFECTION);
			h.assertFalse(ledger.marry(me, HELD), "The ledger refuses to marry a held guest");
			h.assertFalse(GuestPersonalityTesting.context(player, heldNpc, HELD).contains(GuestDialogueLines.Cue.MARRIED), "so it never has the cue");
			h.assertTrue(ledger.marry(me, WED), "Precondition: the marriage-eligible guest marries the player");
			h.assertTrue(GuestPersonalityTesting.context(player, wedNpc, WED).contains(GuestDialogueLines.Cue.MARRIED), "Married: the MARRIED cue");

			for (int i = 0; i < 40; i++) {
				GuestPersonalityTesting.resetCooldowns(player);
				h.assertTrue(GuestDialogue.chat(player, heldNpc, HELD).done(), "Chat " + i + " with the held guest");
				String said = GuestPersonalityTesting.lastSaid(player);
				h.assertTrue(said != null && !said.contains(".spouse."), "The held guest never says a spouse line: " + said);
			}
			boolean heard = false;
			for (int i = 0; i < 80 && !heard; i++) {
				GuestPersonalityTesting.resetCooldowns(player);
				h.assertTrue(GuestDialogue.chat(player, wedNpc, WED).done(), "Chat " + i + " with the spouse");
				String said = GuestPersonalityTesting.lastSaid(player);
				heard = said != null && said.contains(".spouse.");
			}
			h.assertTrue(heard, "The married, marriage-eligible guest does say its spouse line");
			h.succeed();
		} finally {
			GuestPersonalityTesting.unborrow(HELD);
			GuestPersonalityTesting.unborrow(WED);
			clear(made);
			GuestLedgerTesting.reset(server, HELD, WED);
			GuestFavorTesting.unpin(me);
			GuestPersonalityTesting.resetCooldowns(player);
		}
	}

	// ---------------------------------------------------------------- helpers

	/** Every entry point is refused for these entities: chat, both errand steps, starting and calling off a rivalry. */
	private static void refusedAll(GameTestHelper h, ServerPlayer player, Townsfolk npc, String guest, Townsfolk otherNpc, String other, String why) {
		GuestFeature feature = GuestPersonalityTesting.feature();
		h.assertFalse(GuestDialogue.chat(player, npc, guest).done(), "No chat " + why);
		h.assertFalse(GuestPersonalityErrands.turnIn(player, npc, guest).done(), "No errand turned in " + why);
		h.assertFalse(GuestPersonalityErrands.accept(player, otherNpc, other).done(), "No errand taken up " + why);
		h.assertFalse(GuestPersonalityTesting.startRivalry(player, npc, guest, "STARE", otherNpc).done(), "No rivalry started " + why);
		h.assertFalse(GuestRivalries.cancel(player, npc, guest).done(), "No rivalry called off " + why);
		for (String option : List.of("chat", "errand", "errand_turn_in", "rivalry", "rivalry_cancel")) {
			h.assertFalse(feature.available(player, npc, guest, option), "Option " + option + " not offered " + why);
		}
		h.assertTrue(GuestPersonalityTesting.lastSaid(player) == null, "Nothing was said " + why);
	}

	/** What a refused call must leave as it was: affection, items carried, errand records, holds and running rivalries. */
	private static String state(ServerPlayer player, GuestLedger ledger, GuestActivity activity, Item item) {
		UUID me = player.getUUID();
		return ledger.affection(me, ENTRY) + "/" + ledger.affection(me, ENTRY_OTHER) + "/" + plainCount(player, item) + "/"
				+ GuestPersonalityTesting.errandRecord(player, ENTRY) + "/" + GuestPersonalityTesting.errandRecord(player, ENTRY_OTHER) + "/"
				+ activity.current(ENTRY) + "/" + activity.current(ENTRY_OTHER) + "/" + GuestPersonalityTesting.running(player) + "/"
				+ GuestPersonalityTesting.chatGranted(player, ENTRY);
	}

	/** Both rivalry holds released and nothing running for the player. */
	private static void released(GameTestHelper h, GuestActivity activity, ServerPlayer player, String when) {
		boolean held = false;
		for (GuestActivity.Activity each : activity.all().values()) {
			held |= GuestRivalries.KIND.equals(each.kind()) && player.getUUID().equals(each.player());
		}
		h.assertFalse(held || GuestPersonalityTesting.running(player), "Nothing is held or running " + when);
	}

	private static String kind(GuestActivity activity, String guest) {
		GuestActivity.Activity held = activity.current(guest);
		return held == null ? "none" : held.kind();
	}

	private static boolean reaction(String key) {
		return key.contains(".favourite.") || key.contains(".ordinary.") || key.contains(".rare.");
	}

	/** Every word of every roster name (two letters or more), lower case: the names no real person's line may contain. */
	private static Set<String> nameTokens() {
		Set<String> tokens = new HashSet<>();
		for (GuestRoster.Guest guest : GuestRoster.ALL) {
			for (String part : guest.name().toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
				if (part.length() >= 2) {
					tokens.add(part);
				}
			}
		}
		// Placeholder words of pending entries' descriptions, not names.
		tokens.removeAll(Set.of("unnamed", "guest", "picks", "pop", "jakeey's"));
		tokens.add("jakeey");
		return tokens;
	}

	/** Whether {@code text} holds {@code phrase} as whole words. */
	private static boolean word(String text, String phrase) {
		return Pattern.compile("(?<![a-z0-9])" + Pattern.quote(phrase) + "(?![a-z0-9])").matcher(text).find();
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
			npc.discard();
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

	/** A gift the guest takes that is not one of its favourites. */
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
