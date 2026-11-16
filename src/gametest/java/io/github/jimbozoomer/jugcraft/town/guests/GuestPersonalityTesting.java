package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Worker B's test seams for the personality module (GuestPersonalityGameTests only): line tables and the romance gate,
 * the last line sent to a player, cooldown resets, errand records, and rivalries started with a named rival and kind,
 * expired and ticked on demand. Nothing here is reachable in play.
 */
public final class GuestPersonalityTesting {
	private GuestPersonalityTesting() {
	}

	// ---------------------------------------------------------------- lines and profiles

	/** The game-test guest {@code testGuest} speaks with {@code authored}'s lines and asks its errands until {@link #unborrow}. */
	public static void borrow(String testGuest, String authored) {
		if (!testGuest.startsWith("test_") || !GuestDialogueLines.AUTHORED.containsKey(authored)) {
			throw new IllegalArgumentException(testGuest + " cannot borrow " + authored);
		}
		GuestDialogueLines.BORROWED.put(testGuest, authored);
		GuestPersonalityErrands.BORROWED.put(testGuest, authored);
	}

	public static void unborrow(String testGuest) {
		GuestDialogueLines.BORROWED.remove(testGuest);
		GuestPersonalityErrands.BORROWED.remove(testGuest);
	}

	/** The guests with lines of their own. */
	public static Set<String> authored() {
		return Set.copyOf(GuestDialogueLines.AUTHORED.keySet());
	}

	/** Every line {@code guest} speaks: gift reactions, banter, spouse banter, the boast, and its errands' asks and thanks. */
	public static List<GuestDialogueLines.Line> allLines(String guest) {
		List<GuestDialogueLines.Line> all = new ArrayList<>(GuestDialogueLines.lines(guest).all());
		for (GuestPersonalityErrands.Errand errand : GuestPersonalityErrands.errands(guest)) {
			all.add(errand.ask());
			all.add(errand.thanks());
		}
		return all;
	}

	/** The generic set's lines, its errands' too. */
	public static List<GuestDialogueLines.Line> genericLines() {
		List<GuestDialogueLines.Line> all = new ArrayList<>(GuestDialogueLines.GENERIC.all());
		for (GuestPersonalityErrands.Errand errand : GuestPersonalityErrands.GENERIC) {
			all.add(errand.ask());
			all.add(errand.thanks());
		}
		return all;
	}

	/** Whether {@code guest} uses exactly the generic line set, errands and profile (the same objects). */
	public static boolean usesGeneric(String guest) {
		return GuestDialogueLines.lines(guest) == GuestDialogueLines.GENERIC && GuestPersonalityErrands.errands(guest) == GuestPersonalityErrands.GENERIC
				&& GuestPersonality.profile(guest) == GuestPersonality.GENERIC;
	}

	/** Whether the guest may take part in a rivalry. */
	public static boolean mayCompete(String guest) {
		return GuestPersonality.mayCompete(guest);
	}

	/** The banter lines that may be chosen for {@code guest} in a moment with {@code cues} (the romance gate included). */
	public static List<GuestDialogueLines.Line> banterCandidates(String guest, Set<GuestDialogueLines.Cue> cues) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		return entry == null ? List.of() : GuestDialogue.candidates(entry, GuestDialogueLines.lines(guest).banter(), cues);
	}

	/** Every line of {@code guest}'s tables that may be chosen with {@code cues}, gift reactions and boast included. */
	public static List<GuestDialogueLines.Line> allCandidates(String guest, Set<GuestDialogueLines.Cue> cues) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		return entry == null ? List.of() : GuestDialogue.candidates(entry, GuestDialogueLines.lines(guest).all(), cues);
	}

	/** The moment as {@link GuestDialogue} reads it for the player and guest. */
	public static Set<GuestDialogueLines.Cue> context(ServerPlayer player, Townsfolk npc, String guest) {
		return GuestDialogue.context(player, npc, guest);
	}

	// ---------------------------------------------------------------- what was said, cooldowns

	/** The key of the last line sent to the player by the personality module, or null; {@link #clearSaid} forgets it. */
	public static @Nullable String lastSaid(ServerPlayer player) {
		return GuestDialogue.LAST_SAID.get(player.getUUID());
	}

	public static void clearSaid(ServerPlayer player) {
		GuestDialogue.LAST_SAID.remove(player.getUUID());
	}

	/** Drops the player's message cooldowns and memory, as a reconnect does (no reward depends on them). */
	public static void resetCooldowns(ServerPlayer player) {
		GuestDialogue.forget(player.getUUID());
	}

	/** The player's next gift reaction comes from {@code slot} ("favourite", "ordinary" or "rare"). */
	public static void forceReaction(ServerPlayer player, String slot) {
		GuestDialogue.FORCED_SLOT.put(player.getUUID(), slot);
	}

	/** Calls the gift-reaction listener directly, as {@link GuestEvents#GIFT_GIVEN} would. */
	public static void react(ServerPlayer player, Townsfolk npc, String guest, ItemStack gift, boolean favourite, int gained) {
		GuestDialogue.reactToGift(player, npc, guest, gift, favourite, gained);
	}

	/** Calls the talked listener directly, as {@link GuestEvents#TALKED} would. */
	public static void greet(ServerPlayer player, Townsfolk npc, String guest) {
		GuestDialogue.greet(player, npc, guest);
	}

	/** Whether a chat with {@code guest} has granted affection on the player's current day. */
	public static boolean chatGranted(ServerPlayer player, String guest) {
		return GuestPersonalityState.get(player.level().getServer()).chatGranted(player.getUUID(), guest, GuestPersonality.day(player));
	}

	/** The day the module's limits belong to for the player. */
	public static long day(ServerPlayer player) {
		return GuestPersonality.day(player);
	}

	// ---------------------------------------------------------------- errands

	/** The player's saved errand record for the guest and their high-water day, as text: to check nothing moved on. */
	public static String errandRecord(ServerPlayer player, String guest) {
		GuestPersonalityState state = GuestPersonalityState.get(player.level().getServer());
		return state.saved(player.getUUID(), guest) + " @ " + state.highWater.get(player.getUUID());
	}

	/** Today's errand state name for the player and guest: "OFFERED", "ACTIVE" or "DONE". */
	public static String errandState(ServerPlayer player, String guest) {
		return GuestPersonalityErrands.state(player, guest).name();
	}

	// ---------------------------------------------------------------- rivalries

	/** {@link GuestRivalries#start} with a named kind ("RACE", "CREDIT", "STARE") and rival, checked as a found one is. */
	public static GuestResult startRivalry(ServerPlayer player, Townsfolk npc, String guest, String kind, Townsfolk rival) {
		return GuestRivalries.start(player, npc, guest, GuestRivalries.Kind.valueOf(kind), rival);
	}

	/** Whether the player has a rivalry running. */
	public static boolean running(ServerPlayer player) {
		return GuestRivalries.RUNNING.containsKey(player.getUUID());
	}

	/** The player's running rivalry's time runs out now (the next {@link #tick} finishes it). */
	public static void expire(ServerPlayer player) {
		GuestRivalries.Running running = GuestRivalries.RUNNING.get(player.getUUID());
		if (running == null) {
			throw new IllegalStateException("No rivalry running for " + player.getUUID());
		}
		long now = player.level().getServer().getTickCount();
		GuestRivalries.RUNNING.put(player.getUUID(), new GuestRivalries.Running(running.kind(), running.player(), running.first(),
				running.firstEntity(), running.second(), running.secondEntity(), running.dimension(), running.firstGoal(), running.secondGoal(),
				running.started(), now));
	}

	/** One check of every running rivalry now, as the server tick makes every {@link GuestRivalries#CHECK_TICKS}. */
	public static void tick(MinecraftServer server) {
		GuestRivalries.tick(server);
	}

	/** The disconnect listener's work for the player. */
	public static void loggedOut(MinecraftServer server, UUID player) {
		GuestRivalries.loggedOut(server, player);
	}

	/** The server-start recovery: ends every saved rivalry activity not running. */
	public static void recover(MinecraftServer server) {
		GuestRivalries.recover(server);
	}

	/** Ends the player's rivalry quietly, if any (test cleanup). */
	public static void stop(MinecraftServer server, UUID player) {
		GuestRivalries.Running running = GuestRivalries.RUNNING.get(player);
		if (running != null) {
			GuestRivalries.stop(server, running, GuestRivalries.Stop.QUIET);
		}
	}

	/** Whether the player's rivalry has been rewarded on their current day. */
	public static boolean rivalryRewarded(ServerPlayer player) {
		return GuestPersonalityState.get(player.level().getServer()).rivalryRewarded(player.getUUID(), GuestPersonality.day(player));
	}

	/** The menu index of one of the personality feature's options. */
	public static int optionIndex(String option) {
		for (GuestFeatures.Entry entry : GuestFeatures.options()) {
			if (entry.feature().id().equals(GuestPersonalityFeature.ID) && entry.option().equals(option)) {
				return entry.index();
			}
		}
		throw new IllegalStateException("No personality option " + option);
	}

	/** The personality feature, for its availability checks. */
	public static GuestFeature feature() {
		return GuestPersonalityFeature.INSTANCE;
	}

	/** Holds of every guest (a copy), for checks that nothing is left held. */
	public static Map<String, GuestActivity.Activity> activities(MinecraftServer server) {
		return GuestActivity.get(server).all();
	}
}
