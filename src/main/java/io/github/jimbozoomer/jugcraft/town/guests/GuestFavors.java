package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * Gifts and fetch quests (worker B): modest existing survival items, taken from the player's own inventory on the
 * server, exactly once; affection added only through {@link GuestLedger#addAffection}, capped per player and guest per
 * day. No currency is paid or made, and no item is ever given as a reward. Calls come from {@link GuestMenu} buttons
 * that have already passed its distance and revision checks, but each entry point checks again for itself before it
 * takes or adds anything: the player alive, not a spectator, in the guest's level within
 * {@link GuestBalance#INTERACT_RANGE} of it (eye to body, as the menu measures); the entity alive, carrying this guest
 * id, and the ledger's canonical entity for it; the guest in the roster and not pending. A duplicate, remote or stale
 * call takes no item and adds no affection. Before any item is taken (and before a quest is taken up) the ledger is
 * asked {@link GuestLedger#canGainAffection}: when a gain would not really be recorded (a damaged or unreadable entry for
 * the player or the guest, an unavailable guest, full affection) the action is refused and nothing is taken or advanced.
 *
 * <p><b>State.</b> Saved with the overworld as {@code jugcraft:town_guest_favors}: per player and guest, the day it
 * belongs to, the gifts given and affection gained that day, and that day's quest (item, count, state). A record from an
 * earlier day counts as nothing given and a new quest offered (an accepted quest not turned in lapses with its day, and
 * nothing was taken for it). Each player's high-water day (the latest day any of their records was written for) is
 * saved too, and the day used is never earlier than it: a game clock moved backwards (an edited or restored world)
 * neither resets a day nor offers an earlier day's quest again, even after that day's records were tidied away.
 * Opening, closing or reopening a menu, reconnecting and restarting the server reset none of it.
 *
 * <p><b>The day</b> is the overworld's game time divided by {@link #DAY_TICKS}: game time, not the time of day, so
 * sleeping through the night or {@code /time set} never starts a new day early; never earlier than the player's
 * high-water day.
 *
 * <p><b>Numbers</b> (first choices for owner review; {@link GuestBalance#MAX_AFFECTION} is 1000):
 * <ul>
 * <li>{@link #GIFTS_PER_DAY}: 3 gifts per player and guest per day, one item each, from the main hand.</li>
 * <li>{@link #DAILY_AFFECTION_CAP}: 75 affection per player and guest per day, from gifts and the quest together.</li>
 * <li>{@link #GIFTS}: dandelion 4, poppy 4, cornflower 5, pink tulip 5, blue orchid 6, lily of the valley 6,
 * sunflower 7; sweet berries 4, melon slice 4, apple 5, cookie 5, baked potato 5, bread 6, glow berries 6, cooked cod 6,
 * cooked salmon 7, honey bottle 8, pumpkin pie 10, cake 15; honeycomb 6, book 6, candle 6, amethyst shard 8. Only
 * plain stacks count (no custom name or other changed component). Nothing else is taken: no valuables, no currency.</li>
 * <li>{@link #FAVOURITES}: each guest has 3 favourites from that table, chosen by a fixed hash of the guest id (a game
 * mechanic, not a claim about anyone), each worth {@link #FAVOURITE_BONUS} (+10) more.</li>
 * <li>{@link #QUESTS}: one quest per player and guest per day, chosen by a fixed hash of guest, player and day: 16
 * wheat, 8 bread, 12 sugar cane, 6 apples, 8 cooked cod, 16 cobblestone, 12 carrots, 12 potatoes, 12 beetroots, 4
 * pumpkins, 16 melon slices, 8 oak logs, 12 sweet berries, 16 kelp, 8 paper, 6 string, 6 feathers, 4 flint, 12 clay
 * balls or 6 cooked salmon. Turning it in gives {@link #QUEST_REWARD} (30) affection and nothing else.</li>
 * </ul>
 * Pace: at the cap, 1000 / 75 is 14 in-game days (two in-game weeks); a typical day of three ordinary gifts (about 6
 * each) and the quest is 48, about 21 days; favourite gifts and the quest reach the cap. Any gain is clamped to what is
 * left of the day's cap and of the maximum; an action that would add nothing is refused and takes nothing.
 *
 * <p><b>Messages</b> (English fallbacks here; worker C adds them to en_us.json): {@code message.jugcraft.guest.}
 * {@code unavailable}, {@code not_here}, {@code record_locked}, {@code full_affection}, {@code cap_today}, {@code gift.empty_hand}, {@code gift.unwanted},
 * {@code gift.enough_today}, {@code gift.given}, {@code gift.favourite}, {@code quest.accepted},
 * {@code quest.already_active}, {@code quest.done_today}, {@code quest.not_accepted}, {@code quest.too_few},
 * {@code quest.turned_in}, {@code quest.none}.
 */
public final class GuestFavors {
	/** A player's fetch quest for a guest, today. */
	public enum QuestState {
		NONE, OFFERED, ACTIVE, DONE
	}

	/** One thing a guest takes as a gift, and the affection one of it is worth. */
	public record Gift(Item item, int value) {
	}

	/** One fetch quest: bring {@code count} of {@code item}. */
	public record Quest(Item item, int count) {
	}

	/** Game ticks in a day: the overworld's game time divided by this is the day the limits belong to. */
	public static final long DAY_TICKS = 24_000L;
	/** Gifts a player may give one guest per day. */
	public static final int GIFTS_PER_DAY = 3;
	/** Affection a player may gain with one guest per day, gifts and the quest together. */
	public static final int DAILY_AFFECTION_CAP = 75;
	/** What a favourite gift is worth on top of its table value. */
	public static final int FAVOURITE_BONUS = 10;
	/** How many of the gift table's items each guest favours. */
	public static final int FAVOURITES = 3;
	/** The affection a turned-in quest gives (subject to the caps); it gives nothing else. */
	public static final int QUEST_REWARD = 30;

	/** What guests take, in a fixed order (the favourites are picked from this order). */
	public static final List<Gift> GIFTS = List.of(
			new Gift(Items.DANDELION, 4),
			new Gift(Items.POPPY, 4),
			new Gift(Items.CORNFLOWER, 5),
			new Gift(Items.PINK_TULIP, 5),
			new Gift(Items.BLUE_ORCHID, 6),
			new Gift(Items.LILY_OF_THE_VALLEY, 6),
			new Gift(Items.SUNFLOWER, 7),
			new Gift(Items.SWEET_BERRIES, 4),
			new Gift(Items.MELON_SLICE, 4),
			new Gift(Items.APPLE, 5),
			new Gift(Items.COOKIE, 5),
			new Gift(Items.BAKED_POTATO, 5),
			new Gift(Items.BREAD, 6),
			new Gift(Items.GLOW_BERRIES, 6),
			new Gift(Items.COOKED_COD, 6),
			new Gift(Items.COOKED_SALMON, 7),
			new Gift(Items.HONEY_BOTTLE, 8),
			new Gift(Items.PUMPKIN_PIE, 10),
			new Gift(Items.CAKE, 15),
			new Gift(Items.HONEYCOMB, 6),
			new Gift(Items.BOOK, 6),
			new Gift(Items.CANDLE, 6),
			new Gift(Items.AMETHYST_SHARD, 8));

	/** The daily fetch quests, in a fixed order (the day's pick is an index into this). */
	public static final List<Quest> QUESTS = List.of(
			new Quest(Items.WHEAT, 16),
			new Quest(Items.BREAD, 8),
			new Quest(Items.SUGAR_CANE, 12),
			new Quest(Items.APPLE, 6),
			new Quest(Items.COOKED_COD, 8),
			new Quest(Items.COBBLESTONE, 16),
			new Quest(Items.CARROT, 12),
			new Quest(Items.POTATO, 12),
			new Quest(Items.BEETROOT, 12),
			new Quest(Items.PUMPKIN, 4),
			new Quest(Items.MELON_SLICE, 16),
			new Quest(Items.OAK_LOG, 8),
			new Quest(Items.SWEET_BERRIES, 12),
			new Quest(Items.KELP, 16),
			new Quest(Items.PAPER, 8),
			new Quest(Items.STRING, 6),
			new Quest(Items.FEATHER, 6),
			new Quest(Items.FLINT, 4),
			new Quest(Items.CLAY_BALL, 12),
			new Quest(Items.COOKED_SALMON, 6));

	/**
	 * Test seam: a day pinned per player in place of the overworld's, so game tests can move days without touching the
	 * world's clock (which every other test shares). Empty in play.
	 */
	static final Map<UUID, Long> PINNED_DAYS = new ConcurrentHashMap<>();

	private GuestFavors() {
	}

	/** Server hooks: none. Everything lives in the saved data and is read when asked for. Called once from {@link GuestService}. */
	public static void register() {
	}

	// ---------------------------------------------------------------- gifts

	/**
	 * Gives the guest one of the item in the player's main hand. Refused, taking nothing, when the guest is unknown or
	 * pending, the hand is empty or holds something the guest does not take, today's gifts or affection cap are used up,
	 * or the player's affection with the guest is already full. Otherwise exactly one item leaves that hand stack and the
	 * gift's value, clamped to what is left today and of the maximum, is added in the ledger.
	 */
	public static GuestResult gift(ServerPlayer player, Townsfolk npc, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		GuestResult away = check(player, npc, guest, entry);
		if (away != null) {
			return away;
		}
		Component name = name(npc, entry);
		ItemStack hand = player.getMainHandItem();
		if (hand.isEmpty()) {
			return GuestResult.refused("message.jugcraft.guest.gift.empty_hand", "Hold the gift in your main hand.");
		}
		int value = giftValue(guest, hand);
		if (value <= 0) {
			return GuestResult.refused("message.jugcraft.guest.gift.unwanted", "%s does not take %s as a gift.", name, hand.getHoverName());
		}
		MinecraftServer server = player.level().getServer();
		State state = State.get(server);
		UUID me = player.getUUID();
		long day = state.effective(me, day(player));
		Favor today = state.today(me, guest, day);
		if (today.gifts() >= GIFTS_PER_DAY) {
			return GuestResult.refused("message.jugcraft.guest.gift.enough_today",
					"%s has had all %s of your gifts for today. Come back tomorrow.", name, GIFTS_PER_DAY);
		}
		GuestLedger ledger = GuestLedger.get(server);
		int before = ledger.affection(me, guest);
		GuestResult refused = capped(name, today, before);
		if (refused != null) {
			return refused;
		}
		if (!ledger.canGainAffection(me, guest)) {
			return locked(name);   // a damaged or unreadable ledger entry: nothing is taken for a gain it would not record
		}
		int gain = Math.min(value, Math.min(DAILY_AFFECTION_CAP - today.affection(), GuestBalance.MAX_AFFECTION - before));
		Component given = hand.getHoverName();
		boolean favourite = favourites(guest).contains(hand.getItem());
		// Exactly one, from the very stack in the hand.
		hand.shrink(1);
		player.getInventory().setChanged();
		int added = Math.max(0, ledger.addAffection(me, guest, gain) - before);
		state.put(me, guest, today.withGift(added), day);
		return favourite
				? GuestResult.done("message.jugcraft.guest.gift.favourite", "You gave %s one %s, a favourite: +%s affection.", name, given, added)
				: GuestResult.done("message.jugcraft.guest.gift.given", "You gave %s one %s: +%s affection.", name, given, added);
	}

	/** What one of {@code stack} is worth to {@code guest} as a gift (a favourite's bonus included); 0 if it is not taken. */
	static int giftValue(String guest, ItemStack stack) {
		if (stack.isEmpty() || !plain(stack, stack.getItem())) {
			return 0;
		}
		int value = baseValue(stack.getItem());
		return value <= 0 ? 0 : value + (favourites(guest).contains(stack.getItem()) ? FAVOURITE_BONUS : 0);
	}

	/** The gift table's value of {@code item}; 0 if guests do not take it. */
	public static int baseValue(Item item) {
		for (Gift gift : GIFTS) {
			if (gift.item() == item) {
				return gift.value();
			}
		}
		return 0;
	}

	/**
	 * The guest's favourite gifts: {@link #FAVOURITES} distinct items of {@link #GIFTS}, each worth
	 * {@link #FAVOURITE_BONUS} more. These are gameplay preferences picked deterministically from the gift table by a fixed
	 * hash of the guest id, not claims about anyone. Pure: no server state, the same on every machine, safe on the client.
	 */
	public static List<Item> favourites(String guest) {
		List<Item> items = new ArrayList<>(GIFTS.size());
		for (Gift gift : GIFTS) {
			items.add(gift.item());
		}
		long seed = hash(guest);
		for (int i = 0; i < FAVOURITES; i++) {
			seed = mix(seed + 0x9E3779B97F4A7C15L);
			int pick = i + (int) Long.remainderUnsigned(seed, items.size() - i);
			Collections.swap(items, i, pick);
		}
		return List.copyOf(items.subList(0, FAVOURITES));
	}

	// ---------------------------------------------------------------- the fetch quest

	/** Takes up today's offered quest (OFFERED to ACTIVE). Refused once taken up or done, or while it could add nothing. */
	public static GuestResult acceptQuest(ServerPlayer player, Townsfolk npc, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		GuestResult away = check(player, npc, guest, entry);
		if (away != null) {
			return away;
		}
		Component name = name(npc, entry);
		MinecraftServer server = player.level().getServer();
		State state = State.get(server);
		UUID me = player.getUUID();
		long day = state.effective(me, day(player));
		Favor today = state.today(me, guest, day);
		if (today.quest() == QuestState.ACTIVE) {
			return GuestResult.refused("message.jugcraft.guest.quest.already_active", "You are already on today's errand for %s.", name);
		}
		if (today.quest() != QuestState.OFFERED) {
			return GuestResult.refused("message.jugcraft.guest.quest.done_today", "You have done today's errand for %s. Come back tomorrow.", name);
		}
		GuestLedger ledger = GuestLedger.get(server);
		GuestResult refused = capped(name, today, ledger.affection(me, guest));
		if (refused != null) {
			return refused;
		}
		if (!ledger.canGainAffection(me, guest)) {
			return locked(name);   // the quest does not move on for an entry the ledger would not change
		}
		Quest quest = offered(guest, me, day);
		state.put(me, guest, today.withQuest(QuestState.ACTIVE, BuiltInRegistries.ITEM.getKey(quest.item()).toString(), quest.count()), day);
		return GuestResult.done("message.jugcraft.guest.quest.accepted", "%s asks you to bring %s %s today.", name, quest.count(),
				new ItemStack(quest.item()).getHoverName());
	}

	/**
	 * Turns in today's active quest: refused, taking nothing, unless the player carries at least the count (plain
	 * stacks, anywhere in the main inventory or the offhand) and the reward could add something; otherwise exactly the
	 * count is taken, once, the quest is done for the day and its reward is added in the ledger.
	 */
	public static GuestResult turnIn(ServerPlayer player, Townsfolk npc, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		GuestResult away = check(player, npc, guest, entry);
		if (away != null) {
			return away;
		}
		Component name = name(npc, entry);
		MinecraftServer server = player.level().getServer();
		State state = State.get(server);
		UUID me = player.getUUID();
		long day = state.effective(me, day(player));
		Favor today = state.today(me, guest, day);
		if (today.quest() == QuestState.DONE) {
			return GuestResult.refused("message.jugcraft.guest.quest.done_today", "You have done today's errand for %s. Come back tomorrow.", name);
		}
		if (today.quest() != QuestState.ACTIVE) {
			return GuestResult.refused("message.jugcraft.guest.quest.not_accepted", "Accept today's errand for %s first.", name);
		}
		Item item = stored(today);
		int count = today.questCount();
		if (item == null || count <= 0) {
			return GuestResult.refused("message.jugcraft.guest.quest.none", "%s has no errand for you today.", name);
		}
		Component wanted = new ItemStack(item).getHoverName();
		int have = count(player, item);
		if (have < count) {
			return GuestResult.refused("message.jugcraft.guest.quest.too_few", "You carry %s of the %s %s needed.", have, count, wanted);
		}
		GuestLedger ledger = GuestLedger.get(server);
		int before = ledger.affection(me, guest);
		GuestResult refused = capped(name, today, before);
		if (refused != null) {
			return refused;
		}
		if (!ledger.canGainAffection(me, guest)) {
			return locked(name);   // a damaged or unreadable ledger entry: nothing is taken for a gain it would not record
		}
		int gain = Math.min(QUEST_REWARD, Math.min(DAILY_AFFECTION_CAP - today.affection(), GuestBalance.MAX_AFFECTION - before));
		// Counted and taken with the same test in one server tick, so exactly the count leaves.
		take(player, item, count);
		int added = Math.max(0, ledger.addAffection(me, guest, gain) - before);
		state.put(me, guest, today.withTurnIn(added), day);
		return GuestResult.done("message.jugcraft.guest.quest.turned_in", "You brought %s %s %s: +%s affection.", name, count, wanted, added);
	}

	/** Today's quest: NONE for an unknown or pending guest, else OFFERED, ACTIVE or DONE. */
	public static QuestState questState(ServerPlayer player, String guest) {
		if (!available(guest)) {
			return QuestState.NONE;
		}
		State state = State.get(player.level().getServer());
		return state.today(player.getUUID(), guest, state.effective(player.getUUID(), day(player))).quest();
	}

	/** The item and count today's quest asks for; empty when there is none. */
	public static ItemStack questItem(ServerPlayer player, String guest) {
		if (!available(guest)) {
			return ItemStack.EMPTY;
		}
		State state = State.get(player.level().getServer());
		long day = state.effective(player.getUUID(), day(player));
		Favor today = state.today(player.getUUID(), guest, day);
		if (today.quest() == QuestState.OFFERED) {
			Quest quest = offered(guest, player.getUUID(), day);
			return new ItemStack(quest.item(), quest.count());
		}
		Item item = stored(today);
		return item == null || today.questCount() <= 0 ? ItemStack.EMPTY : new ItemStack(item, today.questCount());
	}

	/** How many of the quest's item the player carries (plain stacks, main inventory and offhand). */
	public static int carried(ServerPlayer player, String guest) {
		ItemStack wanted = questItem(player, guest);
		return wanted.isEmpty() ? 0 : count(player, wanted.getItem());
	}

	/** Gifts the player may still give the guest today. */
	public static int giftsLeft(ServerPlayer player, String guest) {
		if (!available(guest)) {
			return 0;
		}
		State state = State.get(player.level().getServer());
		Favor today = state.today(player.getUUID(), guest, state.effective(player.getUUID(), day(player)));
		return Math.max(0, GIFTS_PER_DAY - today.gifts());
	}

	/** Affection the player may still gain with the guest today: what is left of the day's cap and of the maximum. */
	public static int affectionLeftToday(ServerPlayer player, String guest) {
		if (!available(guest)) {
			return 0;
		}
		MinecraftServer server = player.level().getServer();
		State state = State.get(server);
		Favor today = state.today(player.getUUID(), guest, state.effective(player.getUUID(), day(player)));
		int toMaximum = GuestBalance.MAX_AFFECTION - GuestLedger.get(server).affection(player.getUUID(), guest);
		return Math.max(0, Math.min(DAILY_AFFECTION_CAP - today.affection(), toMaximum));
	}

	/** The quest offered to {@code player} by {@code guest} on {@code day}: fixed by the three, the same on every call. */
	static Quest offered(String guest, UUID player, long day) {
		long seed = mix(hash(guest) ^ mix(player.getMostSignificantBits()));
		seed = mix(seed ^ mix(player.getLeastSignificantBits() + 0x9E3779B97F4A7C15L));
		seed = mix(seed ^ mix(day));
		return QUESTS.get((int) Long.remainderUnsigned(seed, QUESTS.size()));
	}

	// ---------------------------------------------------------------- shared checks

	/**
	 * The clock's day for the player: the overworld's game time in whole days (or a test's pinned day). The day used is
	 * {@link State#effective} of this: never earlier than the player's high-water day.
	 */
	static long day(ServerPlayer player) {
		Long pinned = PINNED_DAYS.get(player.getUUID());
		return pinned != null ? pinned : Math.floorDiv(player.level().getServer().overworld().getGameTime(), DAY_TICKS);
	}

	private static boolean available(@Nullable String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		return entry != null && entry.available();
	}

	/**
	 * Why the player may not act with this guest now, or null when they may: the guest in the roster and not pending;
	 * the player alive and not a spectator; the entity alive, not removed, carrying {@code guest}, in the player's level
	 * within {@link GuestBalance#INTERACT_RANGE} of the player's eyes (as {@link GuestMenu} measures), and the ledger's
	 * canonical entity for the guest. Every entry point asks this before it takes or adds anything.
	 */
	private static @Nullable GuestResult check(ServerPlayer player, @Nullable Townsfolk npc, @Nullable String guest,
			GuestRoster.@Nullable Guest entry) {
		if (entry == null || !entry.available() || guest == null) {
			Component name = npc != null ? npc.getDisplayName() : Component.literal(entry != null ? entry.name() : String.valueOf(guest));
			return GuestResult.refused("message.jugcraft.guest.unavailable", "%s is not receiving visitors yet.", name);
		}
		double reach = GuestBalance.INTERACT_RANGE;
		boolean here = npc != null && player.isAlive() && !player.isSpectator() && npc.isAlive() && !npc.isRemoved()
				&& guest.equals(npc.guest()) && npc.level() == player.level()
				&& npc.getBoundingBox().distanceToSqr(player.getEyePosition()) <= reach * reach
				&& npc.getUUID().equals(GuestLedger.get(player.level().getServer()).canonical(guest));
		return here ? null : GuestResult.refused("message.jugcraft.guest.not_here", "%s is not here to talk to.", Component.literal(entry.name()));
	}

	private static Component name(@Nullable Townsfolk npc, GuestRoster.Guest entry) {
		return npc != null ? npc.getDisplayName() : Component.literal(entry.name());
	}

	/** Refused because the ledger would not record a gain for this player and guest (a damaged or unreadable entry). */
	private static GuestResult locked(Component name) {
		return GuestResult.refused("message.jugcraft.guest.record_locked",
				"Your friendship with %s cannot change right now; an operator can check it.", name);
	}

	/** Why nothing more can be gained now (full affection, or today's cap used), or null when something can. */
	private static @Nullable GuestResult capped(Component name, Favor today, int affection) {
		if (affection >= GuestBalance.MAX_AFFECTION) {
			return GuestResult.refused("message.jugcraft.guest.full_affection", "%s is already as fond of you as can be.", name);
		}
		if (today.affection() >= DAILY_AFFECTION_CAP) {
			return GuestResult.refused("message.jugcraft.guest.cap_today",
					"You have earned all the affection you can with %s today. Come back tomorrow.", name);
		}
		return null;
	}

	/** A stack of {@code item} with nothing changed on it (no custom name, enchantment or other component). */
	private static boolean plain(ItemStack stack, Item item) {
		return !stack.isEmpty() && stack.is(item) && ItemStack.isSameItemSameComponents(stack, new ItemStack(item));
	}

	/** Plain {@code item} the player carries: the 36 main slots and the offhand (never armour). */
	static int count(ServerPlayer player, Item item) {
		Inventory inventory = player.getInventory();
		int total = 0;
		for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (plain(stack, item)) {
				total += stack.getCount();
			}
		}
		ItemStack offhand = player.getOffhandItem();
		return plain(offhand, item) ? total + offhand.getCount() : total;
	}

	/** Takes {@code count} plain {@code item} from the slots {@link #count} reads; returns how many it took. */
	private static int take(ServerPlayer player, Item item, int count) {
		Inventory inventory = player.getInventory();
		int remaining = count;
		for (int slot = 0; slot < Inventory.INVENTORY_SIZE && remaining > 0; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (plain(stack, item)) {
				int taken = Math.min(remaining, stack.getCount());
				stack.shrink(taken);
				remaining -= taken;
			}
		}
		ItemStack offhand = player.getOffhandItem();
		if (remaining > 0 && plain(offhand, item)) {
			int taken = Math.min(remaining, offhand.getCount());
			offhand.shrink(taken);
			remaining -= taken;
		}
		inventory.setChanged();
		return count - remaining;
	}

	/** The item a taken-up quest saved; null if it no longer exists. */
	private static @Nullable Item stored(Favor favor) {
		Identifier id = favor.questItem().isEmpty() ? null : Identifier.tryParse(favor.questItem());
		Item item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
		return item == null || item == Items.AIR ? null : item;
	}

	/** FNV-1a over the id's UTF-8 bytes: the same on every machine and run. */
	private static long hash(String text) {
		long hash = 0xCBF29CE484222325L;
		for (byte b : text.getBytes(StandardCharsets.UTF_8)) {
			hash ^= b & 0xFF;
			hash *= 0x100000001B3L;
		}
		return hash;
	}

	/** SplitMix64's finalizer. */
	private static long mix(long z) {
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

	// ---------------------------------------------------------------- saved state

	/**
	 * One player's day with one guest: the day, gifts given and affection gained that day, and that day's quest (state,
	 * and once taken up its item id and count).
	 */
	record Favor(long day, int gifts, int affection, QuestState quest, String questItem, int questCount) {
		static final Codec<QuestState> STATE_CODEC = Codec.STRING.xmap(Favor::readState, state -> state.name().toLowerCase(Locale.ROOT));
		static final Codec<Favor> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.LONG.optionalFieldOf("day", 0L).forGetter(Favor::day),
				Codec.INT.optionalFieldOf("gifts", 0).forGetter(Favor::gifts),
				Codec.INT.optionalFieldOf("affection", 0).forGetter(Favor::affection),
				STATE_CODEC.optionalFieldOf("quest", QuestState.OFFERED).forGetter(Favor::quest),
				Codec.STRING.optionalFieldOf("quest_item", "").forGetter(Favor::questItem),
				Codec.INT.optionalFieldOf("quest_count", 0).forGetter(Favor::questCount)
		).apply(i, Favor::new));

		Favor {
			gifts = Math.max(0, gifts);
			affection = Math.max(0, affection);
			quest = quest == null || quest == QuestState.NONE ? QuestState.DONE : quest;
			questItem = questItem == null ? "" : questItem;
			questCount = Math.max(0, questCount);
		}

		/** A day with nothing given and the quest offered. */
		static Favor fresh(long day) {
			return new Favor(day, 0, 0, QuestState.OFFERED, "", 0);
		}

		/** An unknown saved state reads as done: never a second reward. */
		private static QuestState readState(String saved) {
			return switch (saved) {
				case "offered" -> QuestState.OFFERED;
				case "active" -> QuestState.ACTIVE;
				default -> QuestState.DONE;
			};
		}

		Favor withGift(int gained) {
			return new Favor(day, gifts + 1, affection + gained, quest, questItem, questCount);
		}

		Favor withQuest(QuestState state, String item, int count) {
			return new Favor(day, gifts, affection, state, item, count);
		}

		Favor withTurnIn(int gained) {
			return new Favor(day, gifts, affection + gained, QuestState.DONE, questItem, questCount);
		}

		Favor withAffection(int gained) {
			return new Favor(day, gifts, gained, quest, questItem, questCount);
		}
	}

	/** Every player's favors with every guest, saved with the overworld as {@code jugcraft:town_guest_favors}. */
	static final class State extends SavedData {
		static final Codec<State> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.unboundedMap(Codec.STRING, Favor.CODEC))
						.optionalFieldOf("players", Map.of()).forGetter(State::players),
				Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.LONG).optionalFieldOf("high_water", Map.of()).forGetter(State::highWater)
		).apply(i, State::new));
		static final SavedDataType<State> TYPE = new SavedDataType<>(Jugcraft.id("town_guest_favors"), State::new, CODEC, null);

		final Map<UUID, Map<String, Favor>> players = new HashMap<>();
		/** Per player, the latest day any of their records was written for: the day used never goes below it. */
		final Map<UUID, Long> highWater = new HashMap<>();

		State() {
			this(Map.of(), Map.of());
		}

		State(Map<UUID, Map<String, Favor>> saved, Map<UUID, Long> water) {
			saved.forEach((player, guests) -> {
				players.put(player, new LinkedHashMap<>(guests));
				// Never below a day a record is kept for, even from a save written before the high-water mark.
				for (Favor favor : guests.values()) {
					highWater.merge(player, favor.day(), Long::max);
				}
			});
			water.forEach((player, day) -> highWater.merge(player, day, Long::max));
		}

		static State get(MinecraftServer server) {
			return server.overworld().getDataStorage().computeIfAbsent(TYPE);
		}

		Map<UUID, Map<String, Favor>> players() {
			return players;
		}

		Map<UUID, Long> highWater() {
			return highWater;
		}

		/** The day the player's limits belong to on the clock's {@code day}: never earlier than their high-water day. */
		long effective(UUID player, long day) {
			Long seen = highWater.get(player);
			return seen == null ? day : Math.max(day, seen);
		}

		/** The record for {@code day} (an effective day): the saved one if it is that day or later, else a fresh day. */
		Favor today(UUID player, String guest, long day) {
			Map<String, Favor> mine = players.get(player);
			Favor saved = mine == null ? null : mine.get(guest);
			return saved != null && saved.day() >= day ? saved : Favor.fresh(day);
		}

		/**
		 * Saves {@code favor} for the effective {@code day}, raising the player's high-water day to it and dropping their
		 * records of earlier days, which can never be current again.
		 */
		void put(UUID player, String guest, Favor favor, long day) {
			long current = effective(player, day);
			highWater.put(player, current);
			Map<String, Favor> mine = players.computeIfAbsent(player, id -> new LinkedHashMap<>());
			mine.values().removeIf(old -> old.day() < current);
			mine.put(guest, favor);
			setDirty();
		}
	}
}
