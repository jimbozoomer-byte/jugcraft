package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.ToLongFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Proposal and divorce, on the server thread, from {@link GuestMenu} buttons that have already passed its distance and
 * revision checks. Both are two steps: asked for, then explicitly confirmed within {@link GuestBalance#CONFIRM_TICKS};
 * cancelling, or letting it lapse, changes nothing. The pending request is the player's, kept server side (not in the
 * menu), so reopening a menu neither confirms nor renews it.
 *
 * <ul>
 * <li>A proposal may be asked for and confirmed only while {@link GuestLedger#canMarry} allows it and the player holds
 * {@link GuestBalance#PROPOSAL_COUNT} {@link GuestBalance#PROPOSAL_ITEM}; every check is made again on confirmation, and
 * the item is taken only once the ledger has committed the marriage, so a refused or lapsed proposal costs nothing.</li>
 * <li>Confirmation is for the same guest the request named; a confirmation for another guest is refused.</li>
 * </ul>
 *
 * <p>Pending requests are kept per player UUID with the guest, the overworld game time it was asked at, and its expiry
 * ({@code asked + CONFIRM_TICKS}). A request is live only while the game time is at or after the time it was asked and
 * the highest game time seen since the server started (observed every tick, and on every read) is below its expiry: game
 * time moved backwards drops it rather than bringing a lapsed one back. They are held in memory
 * and dropped when the server stops ({@link GuestLedger#register}): losing one only means asking again. Each player has
 * at most one pending proposal and one pending divorce. Asking again for the same guest replaces the request (a new
 * explicit request, with a new expiry, and the player is told so); asking for another guest while one is pending is
 * refused until it is confirmed, cancelled or lapsed. A confirmation that reaches the re-checks uses its request up,
 * married or not.
 *
 * <p>The proposal entry points check for themselves, before anything else, what the menu checks too: the player alive,
 * not a spectator, in the npc's level and within {@link GuestBalance#INTERACT_RANGE} of it; the npc alive, carrying this
 * guest, and the guest's canonical entity; the guest available and {@code mayMarry}. A call failing any of them reads and
 * changes nothing (no request, no item, no ledger change).
 */
public final class GuestMarriage {
	private record Pending(String guest, long asked, long expires) {
		/** Live at game time {@code now}, the highest game time seen being {@code highest} (at least {@code now}). */
		boolean liveAt(long now, long highest) {
			return now >= asked && highest < expires;
		}
	}

	/** The overworld game time. Package-private test seam (to move game time backwards); the mod never changes it. */
	static ToLongFunction<MinecraftServer> gameTime = server -> server.overworld().getGameTime();

	private static final Map<UUID, Pending> PROPOSALS = new HashMap<>();
	private static final Map<UUID, Pending> DIVORCES = new HashMap<>();
	/** The highest game time seen (the expiry's high-water mark): a request that has lapsed stays lapsed. */
	private static long highest = Long.MIN_VALUE;

	private GuestMarriage() {
	}

	/** Drops every pending request (the server stopped). */
	static void clear() {
		PROPOSALS.clear();
		DIVORCES.clear();
		highest = Long.MIN_VALUE;
	}

	/** Notes the game time (every server tick, from {@link GuestLedger#register}), so a lapse is seen even unread. */
	static void observe(MinecraftServer server) {
		now(server);
	}

	// ---------------------------------------------------------------- proposal

	public static GuestResult requestProposal(ServerPlayer player, Townsfolk npc, String guest) {
		GuestResult away = beside(player, npc, guest);
		if (away != null) {
			return away;
		}
		MinecraftServer server = server(player);
		UUID me = player.getUUID();
		Pending pending = live(PROPOSALS, server, me);
		if (pending != null && !pending.guest().equals(guest)) {
			return GuestResult.refused("message.jugcraft.guest.propose.elsewhere",
					"You have already proposed to %s. Confirm or cancel that first.", name(pending.guest()));
		}
		GuestLedger ledger = GuestLedger.get(server);
		GuestLedger.Refusal why = ledger.canMarry(me, guest);
		if (why != null) {
			return refusal(why, ledger, me, guest);
		}
		if (held(player) < GuestBalance.PROPOSAL_COUNT) {
			return needItem();
		}
		prune(PROPOSALS, server);
		long now = now(server);
		PROPOSALS.put(me, new Pending(guest, now, now + GuestBalance.CONFIRM_TICKS));
		int seconds = GuestBalance.CONFIRM_TICKS / 20;
		if (pending != null) {
			return GuestResult.done("message.jugcraft.guest.propose.renewed",
					"You asked %s again: this request replaces the earlier one. Confirm within %s seconds, or cancel.", name(guest), seconds);
		}
		return GuestResult.done("message.jugcraft.guest.propose.asked",
				"You asked %s to marry you. Confirm within %s seconds, or cancel. Confirming gives %s %s.", name(guest), seconds,
				GuestBalance.PROPOSAL_COUNT, itemName());
	}

	public static GuestResult confirmProposal(ServerPlayer player, Townsfolk npc, String guest) {
		GuestResult away = beside(player, npc, guest);
		if (away != null) {
			return away;   // the request, if any, is left as it was
		}
		MinecraftServer server = server(player);
		UUID me = player.getUUID();
		Pending pending = live(PROPOSALS, server, me);
		if (pending == null) {
			return GuestResult.refused("message.jugcraft.guest.propose.none",
					"There is no proposal to %s waiting to be confirmed (it may have lapsed).", name(guest));
		}
		if (!pending.guest().equals(guest)) {
			return GuestResult.refused("message.jugcraft.guest.propose.other",
					"Your proposal was to %s, not to %s.", name(pending.guest()), name(guest));
		}
		PROPOSALS.remove(me);   // from here the request is used up, whether or not the marriage is made
		GuestLedger ledger = GuestLedger.get(server);
		GuestLedger.Refusal why = ledger.canMarry(me, guest);
		if (why != null) {
			return refusal(why, ledger, me, guest);
		}
		if (held(player) < GuestBalance.PROPOSAL_COUNT) {
			return needItem();
		}
		if (!ledger.marry(me, guest)) {
			GuestLedger.Refusal now = ledger.canMarry(me, guest);
			return now != null ? refusal(now, ledger, me, guest) : noMarriage(guest);
		}
		int taken = take(player, GuestBalance.PROPOSAL_COUNT);
		if (taken != GuestBalance.PROPOSAL_COUNT) {
			giveBack(player, taken);
			ledger.unmarry(me, guest);
			Jugcraft.LOGGER.warn("[guests] Took {} of {} proposal items from {}; the marriage to {} is undone", taken,
					GuestBalance.PROPOSAL_COUNT, me, guest);
			return GuestResult.refused("message.jugcraft.guest.propose.failed",
					"The %s could not be taken, so nothing changed.", itemName());
		}
		DIVORCES.remove(me);
		return GuestResult.done("message.jugcraft.guest.propose.married", "You and %s are now married.", name(guest));
	}

	public static GuestResult cancelProposal(ServerPlayer player) {
		Pending pending = live(PROPOSALS, server(player), player.getUUID());
		if (pending == null) {
			return GuestResult.refused("message.jugcraft.guest.propose.cancel_none", "There is no proposal to cancel.");
		}
		PROPOSALS.remove(player.getUUID());
		return GuestResult.done("message.jugcraft.guest.propose.cancelled", "You withdrew your proposal to %s.", name(pending.guest()));
	}

	/** The guest the player has asked to marry and not yet confirmed or cancelled (unexpired), or null. */
	public static @Nullable String proposalPending(ServerPlayer player) {
		Pending pending = live(PROPOSALS, server(player), player.getUUID());
		return pending == null ? null : pending.guest();
	}

	/** Whether a proposal now could be asked for and confirmed (every check, the item included); for the menu. */
	public static boolean mayPropose(ServerPlayer player, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.mayMarry()) {
			return false;
		}
		MinecraftServer server = server(player);
		Pending pending = live(PROPOSALS, server, player.getUUID());
		if (pending != null && !pending.guest().equals(guest)) {
			return false;
		}
		return GuestLedger.get(server).canMarry(player.getUUID(), guest) == null && held(player) >= GuestBalance.PROPOSAL_COUNT;
	}

	// ---------------------------------------------------------------- divorce

	public static GuestResult requestDivorce(ServerPlayer player, String guest) {
		MinecraftServer server = server(player);
		UUID me = player.getUUID();
		if (!guest.equals(GuestLedger.get(server).spouseOf(me))) {
			return GuestResult.refused("message.jugcraft.guest.divorce.not_married", "You are not married to %s.", name(guest));
		}
		Pending pending = live(DIVORCES, server, me);
		prune(DIVORCES, server);
		long now = now(server);
		DIVORCES.put(me, new Pending(guest, now, now + GuestBalance.CONFIRM_TICKS));
		int seconds = GuestBalance.CONFIRM_TICKS / 20;
		if (pending != null) {
			return GuestResult.done("message.jugcraft.guest.divorce.renewed",
					"You asked again to divorce %s: this request replaces the earlier one. Confirm within %s seconds, or cancel.",
					name(guest), seconds);
		}
		return GuestResult.done("message.jugcraft.guest.divorce.asked",
				"Do you want to divorce %s? Confirm within %s seconds, or cancel. Afterwards neither of you may marry for %s.",
				name(guest), seconds, duration(GuestBalance.REMARRIAGE_COOLDOWN_MILLIS));
	}

	public static GuestResult confirmDivorce(ServerPlayer player, String guest) {
		MinecraftServer server = server(player);
		UUID me = player.getUUID();
		Pending pending = live(DIVORCES, server, me);
		if (pending == null || !pending.guest().equals(guest)) {
			return GuestResult.refused("message.jugcraft.guest.divorce.none",
					"There is no divorce from %s waiting to be confirmed (it may have lapsed).", name(guest));
		}
		DIVORCES.remove(me);
		GuestLedger ledger = GuestLedger.get(server);
		if (!guest.equals(ledger.spouseOf(me)) || ledger.divorce(me) == null) {
			return GuestResult.refused("message.jugcraft.guest.divorce.not_married", "You are not married to %s.", name(guest));
		}
		return GuestResult.done("message.jugcraft.guest.divorce.done",
				"You and %s are divorced. Neither of you may marry for %s.", name(guest), duration(GuestBalance.REMARRIAGE_COOLDOWN_MILLIS));
	}

	public static GuestResult cancelDivorce(ServerPlayer player) {
		Pending pending = live(DIVORCES, server(player), player.getUUID());
		if (pending == null) {
			return GuestResult.refused("message.jugcraft.guest.divorce.cancel_none", "There is no divorce to cancel.");
		}
		DIVORCES.remove(player.getUUID());
		return GuestResult.done("message.jugcraft.guest.divorce.cancelled", "You stay married to %s.", name(pending.guest()));
	}

	/** Whether the player has asked for a divorce and not yet confirmed or cancelled it (unexpired). */
	public static boolean divorcePending(ServerPlayer player) {
		return live(DIVORCES, server(player), player.getUUID()) != null;
	}

	// ---------------------------------------------------------------- for the repair command and tests

	/** The guest of the player's live pending proposal, or null; by UUID, so an operator can ask about anyone. */
	static @Nullable String proposalPending(MinecraftServer server, UUID player) {
		Pending pending = live(PROPOSALS, server, player);
		return pending == null ? null : pending.guest();
	}

	/** The guest of the player's live pending divorce, or null. */
	static @Nullable String divorcePending(MinecraftServer server, UUID player) {
		Pending pending = live(DIVORCES, server, player);
		return pending == null ? null : pending.guest();
	}

	/** The game time at which the player's pending proposal lapses, as asked (not checked for being live), or -1 (tests). */
	static long proposalExpiry(UUID player) {
		Pending pending = PROPOSALS.get(player);
		return pending == null ? -1L : pending.expires();
	}

	/** The expiry high-water mark (tests save it before moving game time, and put it back after). */
	static long highestGameTime() {
		return highest;
	}

	static void highestGameTime(long value) {
		highest = value;
	}

	/** {@code millis} as days, hours and minutes (rounded up to the minute), for messages. */
	static String duration(long millis) {
		long minutes = Math.max(0L, (millis + 59_999L) / 60_000L);
		long days = minutes / (24 * 60);
		long hours = minutes / 60 % 24;
		long rest = minutes % 60;
		if (days > 0) {
			return days + "d " + hours + "h " + rest + "m";
		}
		return hours > 0 ? hours + "h " + rest + "m" : rest + "m";
	}

	// ---------------------------------------------------------------- helpers

	private static MinecraftServer server(ServerPlayer player) {
		return player.level().getServer();
	}

	private static long now(MinecraftServer server) {
		long now = gameTime.applyAsLong(server);
		if (now > highest) {
			highest = now;
		}
		return now;
	}

	/** The player's request if it is live; a lapsed one (or one from a game time now in the future) is dropped. */
	private static @Nullable Pending live(Map<UUID, Pending> requests, MinecraftServer server, UUID player) {
		Pending pending = requests.get(player);
		if (pending != null && !pending.liveAt(now(server), highest)) {
			requests.remove(player);
			return null;
		}
		return pending;
	}

	/** Drops every request that is not live, so the map holds only live ones (at most one per player). */
	private static void prune(Map<UUID, Pending> requests, MinecraftServer server) {
		long now = now(server);
		requests.values().removeIf(pending -> !pending.liveAt(now, highest));
	}

	/**
	 * Why the player cannot act on this guest through {@code npc} now (the checks the menu makes, made again here), or null
	 * when they can. Reads nothing but the roster, the two entities and the guest's canonical entity.
	 */
	private static @Nullable GuestResult beside(ServerPlayer player, Townsfolk npc, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available() || !entry.mayMarry()) {
			return noMarriage(guest);
		}
		if (!guest.equals(npc.guest())) {
			return notThisGuest(guest);
		}
		if (!player.isAlive() || player.isSpectator() || !npc.isAlive() || npc.isRemoved() || npc.level() != player.level()
				|| npc.getBoundingBox().distanceToSqr(player.getEyePosition()) > GuestBalance.INTERACT_RANGE * GuestBalance.INTERACT_RANGE) {
			return GuestResult.refused("message.jugcraft.guest.marriage.away", "You must be beside %s for that.", name(guest));
		}
		if (!npc.getUUID().equals(GuestLedger.get(server(player)).canonical(guest))) {
			return notThisGuest(guest);   // a duplicate, or an entity no longer the guest
		}
		return null;
	}

	/** How many proposal items the player holds anywhere in their inventory. */
	static int held(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		int total = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.is(GuestBalance.PROPOSAL_ITEM)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	/** Takes up to {@code wanted} proposal items from the inventory, in slot order; how many were taken. */
	private static int take(ServerPlayer player, int wanted) {
		Inventory inventory = player.getInventory();
		int taken = 0;
		for (int slot = 0; slot < inventory.getContainerSize() && taken < wanted; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.is(GuestBalance.PROPOSAL_ITEM)) {
				int n = Math.min(wanted - taken, stack.getCount());
				stack.shrink(n);
				taken += n;
			}
		}
		if (taken > 0) {
			inventory.setChanged();
		}
		return taken;
	}

	/** Returns items a failed taking removed: into the inventory, or dropped at the player's feet if it is full. */
	private static void giveBack(ServerPlayer player, int count) {
		if (count <= 0) {
			return;
		}
		ItemStack back = new ItemStack(GuestBalance.PROPOSAL_ITEM, count);
		if (!player.getInventory().add(back) && !back.isEmpty()) {
			player.drop(back, false, net.minecraft.util.Prediction.SERVER_ONLY);
		}
		player.getInventory().setChanged();
	}

	private static Component name(String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		return Component.literal(entry == null ? guest : entry.name());
	}

	private static Component itemName() {
		return new ItemStack(GuestBalance.PROPOSAL_ITEM).getHoverName();
	}

	private static GuestResult noMarriage(String guest) {
		return GuestResult.refused("message.jugcraft.guest.marriage.none",
				"%s is glad of your companionship, but a proposal is not possible.", name(guest));
	}

	private static GuestResult notThisGuest(String guest) {
		return GuestResult.refused("message.jugcraft.guest.marriage.elsewhere", "Speak to %s in person for that.", name(guest));
	}

	private static GuestResult needItem() {
		return GuestResult.refused("message.jugcraft.guest.propose.item", "A proposal needs %s %s in your inventory.",
				GuestBalance.PROPOSAL_COUNT, itemName());
	}

	/** What the player is told for a ledger's refusal. */
	static GuestResult refusal(GuestLedger.Refusal why, GuestLedger ledger, UUID player, String guest) {
		if ((why == GuestLedger.Refusal.PLAYER_MARRIED || why == GuestLedger.Refusal.PLAYER_COOLDOWN) && ledger.damaged(player)) {
			return GuestResult.refused("message.jugcraft.guest.marriage.damaged",
					"Your marriage record cannot be read just now. An operator must repair it before any proposal.");
		}
		return switch (why) {
			case NO_MARRIAGE -> noMarriage(guest);
			case NOT_FULL_AFFECTION -> GuestResult.refused("message.jugcraft.guest.marriage.affection",
					"Affection with %s must be full before a proposal (%s of %s).", name(guest), ledger.affection(player, guest),
					GuestBalance.MAX_AFFECTION);
			case PLAYER_MARRIED -> GuestResult.refused("message.jugcraft.guest.marriage.player_married", "You are already married.");
			case GUEST_MARRIED -> GuestResult.refused("message.jugcraft.guest.marriage.guest_married", "%s is already married.", name(guest));
			case PLAYER_COOLDOWN -> GuestResult.refused("message.jugcraft.guest.marriage.player_cooldown",
					"You may not marry again for %s.", duration(ledger.cooldownMillis(player)));
			case GUEST_COOLDOWN -> GuestResult.refused("message.jugcraft.guest.marriage.guest_cooldown",
					"%s may not marry again for %s.", name(guest), duration(ledger.cooldownMillis(guest)));
			case GUEST_MISSING -> GuestResult.refused("message.jugcraft.guest.marriage.missing", "%s cannot be found just now.", name(guest));
		};
	}
}
