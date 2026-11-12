package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Worker D's test seams (GuestMilestoneGameTests only; nothing here is reachable in play): the milestone state and its
 * codec, a stand-in church per test guest, a running ceremony aged and stepped on demand, the notices a receiver was
 * sent, the activity events a guest's activities fired, and the ledger's cooldowns cleared so a divorced test couple
 * may marry again.
 */
public final class GuestMilestoneTesting {
	private GuestMilestoneTesting() {
	}

	// ---------------------------------------------------------------- milestones

	public static boolean reached(MinecraftServer server, UUID player, String guest, String key) {
		return GuestMilestoneState.get(server).reached(player, guest, key);
	}

	/** Fires milestone {@code key} as the feature does (once; announced if shared; delivered if online). */
	public static boolean reach(MinecraftServer server, UUID player, String guest, String key) {
		return GuestMilestones.reach(server, player, guest, key);
	}

	/** The player's affection milestones with {@code guest}, their anniversaries today, and their deliveries. */
	public static void check(ServerPlayer player, String guest) {
		GuestMilestones.check(player, guest);
	}

	/** The player's anniversaries as of game day {@code day}, then their deliveries. */
	public static void anniversaries(ServerPlayer player, long day) {
		GuestMilestones.anniversaries(player, day);
		GuestMilestones.deliver(player);
	}

	/** The game day the feature uses. */
	public static long day(MinecraftServer server) {
		return GuestMilestones.day(server);
	}

	/** The latest anniversary due after {@code days} married. */
	public static long due(long days) {
		return GuestMilestones.due(days);
	}

	/** The rewards waiting for the player, as "guest/key". */
	public static List<String> waiting(MinecraftServer server, UUID player) {
		List<String> out = new ArrayList<>();
		for (GuestMilestoneState.Waiting waiting : GuestMilestoneState.get(server).waiting(player)) {
			out.add(waiting.guest() + "/" + waiting.key());
		}
		return out;
	}

	// ---------------------------------------------------------------- activity events

	/** The activity events recorded per guest ({@link #recordActivities}), as "began:kind" and "ended:kind:completed". */
	private static final Map<String, List<String>> ACTIVITY_EVENTS = new ConcurrentHashMap<>();
	private static boolean listening;

	/**
	 * Keeps every {@link GuestEvents#ACTIVITY_BEGAN} and {@link GuestEvents#ACTIVITY_ENDED} for {@code guest} from now
	 * on, as they fire. The recorder registers once; it records only guests named here, so other tests are untouched.
	 */
	public static synchronized void recordActivities(String guest) {
		if (!listening) {
			listening = true;
			GuestEvents.ACTIVITY_BEGAN.register((server, held, activity) -> {
				List<String> kept = ACTIVITY_EVENTS.get(held);
				if (kept != null) {
					kept.add("began:" + activity.kind());
				}
			});
			GuestEvents.ACTIVITY_ENDED.register((server, held, activity, completed) -> {
				List<String> kept = ACTIVITY_EVENTS.get(held);
				if (kept != null) {
					kept.add("ended:" + activity.kind() + ":" + completed);
				}
			});
		}
		ACTIVITY_EVENTS.put(guest, Collections.synchronizedList(new ArrayList<>()));
	}

	/** The activity events recorded for {@code guest} since {@link #recordActivities}, in order. */
	public static List<String> activityEvents(String guest) {
		List<String> kept = ACTIVITY_EVENTS.get(guest);
		return kept == null ? List.of() : List.copyOf(kept);
	}

	public static void stopRecordingActivities(String guest) {
		ACTIVITY_EVENTS.remove(guest);
	}

	/** The serial and start day of the player's marriage record, as "serial@since[+ceremony]", or "none". */
	public static String marriage(MinecraftServer server, UUID player) {
		GuestMilestoneState.Marriage marriage = GuestMilestoneState.get(server).marriage(player);
		return marriage == null ? "none" : marriage.guest + "#" + marriage.serial + "@" + marriage.since + (marriage.ceremony ? "+ceremony" : "");
	}

	/** The reward milestone {@code key} would hand the player now (empty when none may be made). */
	public static ItemStack reward(ServerPlayer player, String guest, String key) {
		return GuestKeepsakes.reward(player.level().getServer(), player, guest, key);
	}

	/** What one of {@code stack} is worth to {@code guest} as a gift (0: not taken). */
	public static int giftValue(String guest, ItemStack stack) {
		return GuestFavors.giftValue(guest, stack);
	}

	/** The milestone state written with its codec, as the game writes it. */
	public static Tag save(MinecraftServer server) {
		return GuestMilestoneState.CODEC.encodeStart(NbtOps.INSTANCE, GuestMilestoneState.get(server)).getOrThrow();
	}

	/** Drops the player's record, as if it had never been kept. */
	public static void forget(MinecraftServer server, UUID player) {
		GuestMilestoneState.get(server).forget(player);
	}

	/** Reads {@code saved} back with the codec and puts the player's record from it in place of the live one. */
	public static void load(MinecraftServer server, Tag saved, UUID player) {
		GuestMilestoneState.get(server).restore(GuestMilestoneState.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow(), player);
	}

	// ---------------------------------------------------------------- the meal

	public static @Nullable Long mealDay(MinecraftServer server, UUID player, String guest) {
		return GuestMilestoneState.get(server).mealDay(player, guest);
	}

	// ---------------------------------------------------------------- the ceremony

	/** The test's own church for {@code guest}: a box in {@code level} (a priest must stand in it for a ceremony). */
	public static void church(String guest, ServerLevel level, AABB box) {
		GuestCeremonyVenue.TEST_PLOTS.put(guest, new GuestCeremonyVenue.Plot(level.dimension(), box));
	}

	/** Back to the spawn town's church for {@code guest}. */
	public static void forgetChurch(String guest) {
		GuestCeremonyVenue.TEST_PLOTS.remove(guest);
	}

	/** The venue's refusal for {@code guest} now ("NO_TOWN", "NOT_LOADED", "NO_PRIEST"), or "FOUND". */
	public static String venue(MinecraftServer server, String guest) {
		GuestCeremonyVenue.Found found = GuestCeremonyVenue.find(server, guest);
		return found.venue() != null ? "FOUND" : String.valueOf(found.refusal());
	}

	/** Whether the player has a running ceremony, and with whom ("" when none). */
	public static String running(UUID player) {
		GuestCeremonies.Running running = GuestCeremonies.running(player);
		return running == null ? "" : running.guest;
	}

	/** The line the player's running ceremony has reached, or -1 before its rite (or none). */
	public static int line(UUID player) {
		GuestCeremonies.Running running = GuestCeremonies.running(player);
		return running == null || running.riteAt < 0 ? -1 : running.line;
	}

	/** Makes the player's request and running ceremony {@code ticks} older. */
	public static void age(UUID player, long ticks) {
		GuestCeremonies.age(player, ticks);
	}

	/** One step of the player's running ceremony, now. */
	public static void step(MinecraftServer server, UUID player) {
		GuestCeremonies.step(server, player);
	}

	/** Whether a meal would be shared for no affection as a married couple at full affection (the meal's one exception). */
	public static boolean marriedAtFull(MinecraftServer server, UUID player, String guest) {
		return GuestMilestoneMeals.marriedAtFull(server, player, guest);
	}

	/** Why a ceremony may not be held now (its eligibility, the venue aside), or null when it may. */
	public static @Nullable GuestResult ceremonyRefusal(MinecraftServer server, UUID player, String guest) {
		return GuestCeremonies.eligible(server, player, guest);
	}

	/** The ceremonies' restart recovery, as the server runs it when started and once a second. */
	public static void recoverCeremonies(MinecraftServer server) {
		GuestCeremonies.recover(server, GuestActivity.get(server).all());
	}

	/** Ends the player's request and ceremony, if any (a test's cleanup). */
	public static void endCeremony(MinecraftServer server, UUID player) {
		GuestCeremonies.left(server, player);
	}

	/** The ceremony options' checks, as the menu reads them. */
	public static boolean option(ServerPlayer player, Townsfolk npc, String guest, String option) {
		return new GuestMilestoneFeature().available(player, npc, guest, option);
	}

	/** A menu option clicked (the feature's act, which checks everything itself). */
	public static GuestResult act(ServerPlayer player, Townsfolk npc, String guest, String option) {
		return new GuestMilestoneFeature().act(player, npc, guest, option);
	}

	/** Whether the feature is registered with its six options, as GuestService.register makes it. */
	public static boolean registered() {
		for (GuestFeature feature : GuestFeatures.all()) {
			if (feature.id().equals(GuestMilestoneFeature.ID)) {
				return feature.options().equals(GuestMilestoneFeature.OPTIONS);
			}
		}
		return false;
	}

	/** Ends the player's divorce cooldown and the guest's (operator repair, as {@code /guests repair}), so they may marry again. */
	public static void clearCooldowns(MinecraftServer server, UUID player, String guest) {
		GuestLedger ledger = GuestLedger.get(server);
		ledger.clearCooldown(player);
		ledger.clearCooldown(guest);
	}

	// ---------------------------------------------------------------- notices

	/** Keeps the notices sent to {@code receiver} from now on. */
	public static void record(UUID receiver) {
		GuestNotices.RECORDED.put(receiver, java.util.Collections.synchronizedList(new ArrayList<>()));
	}

	/** The notices sent to {@code receiver} since {@link #record}, as (named players, text). */
	public static List<GuestNotices.Notice> recorded(UUID receiver) {
		List<GuestNotices.Notice> kept = GuestNotices.RECORDED.get(receiver);
		return kept == null ? List.of() : List.copyOf(kept);
	}

	public static void stopRecording(UUID receiver) {
		GuestNotices.RECORDED.remove(receiver);
	}

	/** Sets the player's two notice settings directly (a test's own setup and cleanup). */
	public static boolean setNotices(MinecraftServer server, UUID player, boolean receive, boolean share) {
		return GuestMilestoneState.get(server).notices(player, receive, share);
	}

	public static boolean receives(MinecraftServer server, UUID player) {
		return GuestNotices.receives(server, player);
	}

	public static boolean shares(MinecraftServer server, UUID player) {
		return GuestNotices.shares(server, player);
	}

	/** Gossip line {@code pick} about {@code guest}. */
	public static Component gossip(String guest, int pick) {
		return GuestNotices.gossipAbout(GuestRoster.get(guest), pick);
	}

	/** The gossip keys {@code guest} may get. */
	public static List<String> gossipKeys(String guest) {
		return GuestNotices.gossipKeys(GuestRoster.get(guest));
	}

	/** A gossip line about a placed guest now, or null. */
	public static @Nullable Component gossip(MinecraftServer server) {
		return GuestNotices.gossip(server, server.overworld().getRandom());
	}

	/** An announcement about {@code subject}, as a milestone's; the players it reached. */
	public static List<UUID> announce(MinecraftServer server, UUID subject, String guest, String key) {
		List<UUID> out = new ArrayList<>();
		for (ServerPlayer player : GuestNotices.milestone(server, subject, guest, key)) {
			out.add(player.getUUID());
		}
		return out;
	}

	/** The recorded notices of {@code receivers} that name a player who does not share now (none, when the rule holds). */
	public static List<GuestNotices.Notice> unshared(MinecraftServer server, List<UUID> receivers) {
		List<GuestNotices.Notice> bad = new ArrayList<>();
		for (UUID receiver : receivers) {
			for (GuestNotices.Notice notice : recorded(receiver)) {
				if (notice.named().stream().anyMatch(named -> !GuestNotices.shares(server, named))) {
					bad.add(notice);
				}
			}
		}
		return bad;
	}
}
