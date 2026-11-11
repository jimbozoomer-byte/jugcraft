package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Milestones, keepsakes, the church ceremony and opt-in notices (worker D; design in
 * docs/plans/jze-guest-milestones-design.md). Called once from {@link GuestService#register}.
 *
 * <p><b>Milestones</b>, per player and guest, each recorded once for good in {@link GuestMilestoneState} and rewarded
 * once ({@link GuestKeepsakes}):
 * <ul>
 * <li>{@code first_gift}, {@code first_quest}: from {@link GuestEvents#GIFT_GIVEN} and {@link GuestEvents#QUEST_TURNED_IN};</li>
 * <li>{@code first_outing}: an {@code outing} activity seen in {@link GuestActivity} that has ended (worker C's classes are
 * never called);</li>
 * <li>{@code affection_250}, {@code _500}, {@code _750}, {@code _1000} ({@link #THRESHOLDS}): read from the ledger after a
 * gift, a quest, a talk, a meal, and every {@link #CHECK_TICKS} ticks for online players, so affection other features add
 * through {@link GuestRewards} is noticed too;</li>
 * <li>{@code ceremony_<marriage>} ({@link GuestCeremonies}) and {@code anniversary_<marriage>_<days>} ({@link #ANNIVERSARIES}
 * game days, then every {@link #YEAR}): romance, only while the guest's {@code mayMarry()} is true and the ledger has the
 * two married ({@link GuestKeepsakes#romance}). Of the anniversaries due, only the latest not yet reached fires, so a long
 * absence brings one letter, not a backlog.</li>
 * </ul>
 * A reward waits in the saved data until the player is online and alive, then goes into their inventory or, if it is
 * full, at their feet. This feature never changes affection except through {@link GuestRewards#grant} (the meal), and
 * never changes the ledger or the roster.
 */
public final class GuestMilestones {
	/** The affection milestones. */
	public static final List<Integer> THRESHOLDS = List.of(250, 500, 750, 1000);
	/** The first anniversaries, in game days; then every {@link #YEAR}. */
	public static final List<Integer> ANNIVERSARIES = List.of(7, 30, 100, 365);
	public static final int YEAR = 365;
	/** Ticks between two checks of every online player's milestones (5 seconds). */
	static final int CHECK_TICKS = 100;

	/** The outings seen held a second ago, by guest. */
	private static final Map<String, GuestActivity.Activity> OUTINGS = new HashMap<>();
	private static int ticks;

	private GuestMilestones() {
	}

	/**
	 * The {@code milestones} guest feature, the event listeners, the server tick, and the player commands
	 * {@code /guests notices ...} and {@code /guests ceremony ...}. Their {@code /guests} root carries no requirement
	 * (Brigadier keeps the first root registration's, which would close every other {@code /guests} subcommand); the
	 * player requirement is on each subcommand node and is checked again when it runs.
	 */
	public static void register() {
		GuestFeatures.register(new GuestMilestoneFeature());
		GuestEvents.GIFT_GIVEN.register((player, npc, guest, gift, favourite, gained) -> safely(() -> {
			reach(player.level().getServer(), player.getUUID(), guest, "first_gift");
			check(player, guest);
		}));
		GuestEvents.QUEST_TURNED_IN.register((player, npc, guest, delivered, gained) -> safely(() -> {
			reach(player.level().getServer(), player.getUUID(), guest, "first_quest");
			check(player, guest);
		}));
		GuestEvents.MARRIED.register((player, npc, guest) -> safely(() -> {
			MinecraftServer server = player.level().getServer();
			GuestMilestoneState.get(server).startMarriage(player.getUUID(), guest, day(server));
		}));
		GuestEvents.DIVORCED.register((player, guest) -> safely(() -> {
			MinecraftServer server = player.level().getServer();
			GuestMilestoneState state = GuestMilestoneState.get(server);
			GuestMilestoneState.Marriage marriage = state.marriage(player.getUUID());
			if (marriage != null && marriage.guest.equals(guest)) {
				state.endMarriage(player.getUUID());
			}
			GuestCeremonies.divorced(server, player.getUUID());
		}));
		GuestEvents.TALKED.register((player, npc, guest) -> safely(() -> check(player, guest)));
		ServerTickEvents.END_SERVER_TICK.register(server -> safely(() -> tick(server)));
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> safely(() -> GuestCeremonies.stopping(server)));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			ticks = 0;
			OUTINGS.clear();
			GuestCeremonies.clear();
			GuestNotices.clear();
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> safely(() -> GuestCeremonies.left(server, handler.player.getUUID())));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			GuestNotices.register(dispatcher);
			GuestCeremonies.register(dispatcher);
		});
	}

	// ---------------------------------------------------------------- the tick

	static void tick(MinecraftServer server) {
		ticks++;
		GuestCeremonies.tick(server);
		if (ticks % 20 == 0) {
			Map<String, GuestActivity.Activity> held = GuestActivity.get(server).all();
			observe(server, held);
			GuestCeremonies.recover(server, held);
			GuestNotices.tick(server, held);
		}
		if (ticks % CHECK_TICKS == 0) {
			for (ServerPlayer player : List.copyOf(server.getPlayerList().getPlayers())) {
				checkAll(player);
			}
		}
	}

	/** Outings held a second ago and not now have ended: each may be the player's first outing with that guest. */
	static void observe(MinecraftServer server, Map<String, GuestActivity.Activity> held) {
		List<Map.Entry<String, GuestActivity.Activity>> ended = new ArrayList<>();
		for (Map.Entry<String, GuestActivity.Activity> was : OUTINGS.entrySet()) {
			if (!was.getValue().equals(held.get(was.getKey()))) {
				ended.add(Map.entry(was.getKey(), was.getValue()));
			}
		}
		OUTINGS.clear();
		held.forEach((guest, activity) -> {
			if ("outing".equals(activity.kind())) {
				OUTINGS.put(guest, activity);
			}
		});
		for (Map.Entry<String, GuestActivity.Activity> outing : ended) {
			GuestRoster.Guest entry = GuestRoster.get(outing.getKey());
			if (entry != null && entry.available()) {
				reach(server, outing.getValue().player(), outing.getKey(), "first_outing");
			}
		}
	}

	// ---------------------------------------------------------------- milestones

	/** The player's affection milestones with {@code guest}, their anniversaries, then anything waiting for them. */
	static void check(ServerPlayer player, String guest) {
		MinecraftServer server = player.level().getServer();
		affection(server, player.getUUID(), guest);
		anniversaries(player, day(server));
		deliver(player);
	}

	/** As {@link #check}, for every guest the player has any affection with. */
	static void checkAll(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();
		for (String guest : GuestLedger.get(server).affections(player.getUUID()).keySet()) {
			affection(server, player.getUUID(), guest);
		}
		anniversaries(player, day(server));
		deliver(player);
	}

	/** Every affection threshold the player has reached with the guest fires, once each. */
	static void affection(MinecraftServer server, UUID player, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available()) {
			return;
		}
		int value = GuestLedger.get(server).affection(player, guest);
		for (int threshold : THRESHOLDS) {
			if (value >= threshold) {
				reach(server, player, guest, "affection_" + threshold);
			}
		}
	}

	/**
	 * The latest anniversary due on game day {@code day} of the player's current marriage fires, if it has not: only while
	 * the guest may marry and the ledger has them married ({@link GuestKeepsakes#romance}).
	 */
	static void anniversaries(ServerPlayer player, long day) {
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		GuestMilestoneState.Marriage marriage = marriage(server, me);
		if (marriage == null || !GuestKeepsakes.romance(server, me, marriage.guest)) {
			return;
		}
		long due = due(day - marriage.since);
		if (due > 0) {
			reach(server, me, marriage.guest, "anniversary_" + marriage.serial + "_" + due);
		}
	}

	/** The latest anniversary (in days) reached after {@code days} married; 0 before the first. */
	static long due(long days) {
		if (days >= 2L * YEAR) {
			return days / YEAR * YEAR;
		}
		long due = 0;
		for (int anniversary : ANNIVERSARIES) {
			if (days >= anniversary) {
				due = anniversary;
			}
		}
		return due;
	}

	/**
	 * Records milestone {@code key} of the player with the guest (once, for good), announces it to opted-in players if
	 * the player shares, and hands the reward over if they are online. False when it was reached before.
	 */
	static boolean reach(MinecraftServer server, UUID player, String guest, String key) {
		if (!GuestMilestoneState.get(server).reach(player, guest, key)) {
			return false;
		}
		GuestNotices.milestone(server, player, guest, key);
		ServerPlayer online = server.getPlayerList().getPlayer(player);
		if (online != null) {
			deliver(online);
		}
		return true;
	}

	/**
	 * Hands over every reward waiting for the player (alive and in the game), each taken off the list before it is given,
	 * so each is given once. A romance reward whose marriage has since ended is not made; its milestone stays reached.
	 */
	static void deliver(ServerPlayer player) {
		if (!player.isAlive() || player.isRemoved()) {
			return;
		}
		MinecraftServer server = player.level().getServer();
		GuestMilestoneState state = GuestMilestoneState.get(server);
		for (GuestMilestoneState.Waiting waiting : state.waiting(player.getUUID())) {
			if (!state.handOver(player.getUUID(), waiting)) {
				continue;
			}
			ItemStack reward = GuestKeepsakes.reward(server, player, waiting.guest(), waiting.key());
			if (reward.isEmpty()) {
				continue;
			}
			Component what = reward.getHoverName();
			GuestKeepsakes.give(player, reward);
			player.sendSystemMessage(Component.translatableWithFallback("message.jugcraft.guest.milestone.reward",
					"A keepsake of your time with %s: %s.", name(waiting.guest()), what));
		}
	}

	// ---------------------------------------------------------------- the marriage record

	/**
	 * The player's current marriage as this feature numbers it, reconciled with the ledger (which alone decides): no
	 * ledger marriage clears the record (an operator's forced divorce has no event); a ledger marriage without a record
	 * (made before this feature) is numbered now, starting today. Null when unmarried or the record is frozen.
	 */
	static GuestMilestoneState.@Nullable Marriage marriage(MinecraftServer server, UUID player) {
		GuestMilestoneState state = GuestMilestoneState.get(server);
		if (state.frozen(player)) {
			return null;
		}
		GuestLedger ledger = GuestLedger.get(server);
		String spouse = ledger.spouseOf(player);
		GuestMilestoneState.Marriage marriage = state.marriage(player);
		if (spouse == null || !player.equals(ledger.spouseOf(spouse))) {
			if (marriage != null) {
				state.endMarriage(player);
			}
			return null;
		}
		if (marriage == null || !marriage.guest.equals(spouse)) {
			marriage = state.startMarriage(player, spouse, day(server));
		}
		return marriage;
	}

	/** The game day: the overworld's game time in whole days (never the time of day, which sleeping moves). */
	static long day(MinecraftServer server) {
		return Math.floorDiv(server.overworld().getGameTime(), GuestFavors.DAY_TICKS);
	}

	// ---------------------------------------------------------------- shared checks

	/**
	 * Why the player may not act with this guest through {@code npc} now, or null: the guest in the roster and available;
	 * the player alive, not a spectator, in the npc's level within {@link GuestBalance#INTERACT_RANGE}; the npc alive,
	 * carrying this guest, and its canonical entity. Every entry point of this feature asks this before anything else.
	 */
	static @Nullable GuestResult beside(ServerPlayer player, @Nullable Townsfolk npc, @Nullable String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available() || guest == null) {
			return GuestResult.refused("message.jugcraft.guest.unavailable", "%s is not receiving visitors yet.",
					Component.literal(entry != null ? entry.name() : String.valueOf(guest)));
		}
		double reach = GuestBalance.INTERACT_RANGE;
		boolean here = npc != null && player.isAlive() && !player.isSpectator() && npc.isAlive() && !npc.isRemoved()
				&& guest.equals(npc.guest()) && npc.level() == player.level()
				&& npc.getBoundingBox().distanceToSqr(player.getEyePosition()) <= reach * reach
				&& npc.getUUID().equals(GuestLedger.get(player.level().getServer()).canonical(guest));
		return here ? null : GuestResult.refused("message.jugcraft.guest.not_here", "%s is not here to talk to.", Component.literal(entry.name()));
	}

	/** The guest's shown name. */
	static Component name(String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		return Component.literal(entry == null ? guest : entry.name());
	}

	/** Runs a listener's work, logging rather than throwing into the caller (which has already committed its own work). */
	private static void safely(Runnable work) {
		try {
			work.run();
		} catch (RuntimeException e) {
			Jugcraft.LOGGER.error("[guests] Milestones failed: {}", e.toString(), e);
		}
	}
}
