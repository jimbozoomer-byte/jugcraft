package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Worker C's test seam for {@link GuestOutings} and {@link GuestVisits} (game tests only): the feature, invitations with
 * a narrow search, a begin at a given place, one guest stepped at a given game time, recovery, the events' handlers, the
 * numbers, and {@link GuestOutingState}'s codec round trip. Nothing here is reachable in play. Kinds are named by id:
 * "fishing", "fair", "board_game", "visit".
 */
public final class GuestOutingTesting {
	private GuestOutingTesting() {
	}

	/** The outings feature, as the guest menu calls it. */
	public static GuestFeature feature() {
		return GuestOutings.FEATURE;
	}

	/** An invitation to {@code kind} whose place is searched only within {@code radius} of the player (keeps a test to its own area). */
	public static GuestResult invite(ServerPlayer player, Townsfolk npc, String guest, String kind, int radius) {
		return GuestOutings.invite(player, npc, guest, kind(kind), radius);
	}

	/** Begins {@code kind} at {@code place} with every entry check, but without searching for the place. */
	public static GuestResult begin(ServerPlayer player, Townsfolk npc, String guest, String kind, BlockPos place) {
		return GuestOutings.begin(player, npc, guest, kind(kind), place);
	}

	/** Steps one guest's outing and return at game time {@code now} (no other guest is touched). */
	public static void step(MinecraftServer server, String guest, long now) {
		GuestOutings.stepGuest(server, guest, now);
	}

	/** The game time outings run on. */
	public static long now(MinecraftServer server) {
		return GuestOutings.now(server);
	}

	/** The recovery a started server runs (and every step), at the real game time; how many activities it ended. */
	public static int recover(MinecraftServer server) {
		return GuestOutings.recover(server, GuestOutings.now(server));
	}

	/** What the server does when the guest's entity unloads (its chunk unloads, it changes level or is removed). */
	public static void unloaded(ServerLevel level, Townsfolk npc) {
		GuestOutings.guestUnloaded(level, npc);
	}

	/** What the server does when the guest's entity loads again. */
	public static void loaded(ServerLevel level, Townsfolk npc) {
		GuestOutings.guestLoaded(level, npc);
	}

	/** Whether an outing or visit with the guest is tracked (under way). */
	public static boolean tracked(String guest) {
		return GuestOutings.OUTINGS.containsKey(guest);
	}

	/** Whether the guest's walk home is being watched now. */
	public static boolean returning(String guest) {
		return GuestOutings.RETURNS.containsKey(guest);
	}

	/** Whether the guest's walk home will be watched when it next loads. */
	public static boolean returnsOnLoad(String guest) {
		return GuestOutings.RETURN_ON_LOAD.contains(guest);
	}

	/** Ticks of together time counted so far for the guest's outing (-1 when none). */
	public static long together(String guest) {
		GuestOutings.Outing outing = GuestOutings.OUTINGS.get(guest);
		return outing == null ? -1L : outing.together;
	}

	public static int duration(String kind) {
		return kind(kind).duration;
	}

	public static int reward(String kind) {
		return kind(kind).reward;
	}

	/** The stale bound of an outing whose kind is not known (after a restart). */
	public static long outingBound() {
		return GuestOutings.OUTING_BOUND;
	}

	public static long visitBound() {
		return GuestOutings.Kind.VISIT_HOME.bound();
	}

	public static int stuckTicks() {
		return GuestOutings.STUCK_TICKS;
	}

	public static int returnStuckTicks() {
		return GuestOutings.RETURN_STUCK_TICKS;
	}

	/** Whether {@code kind} is done today with {@code guest} for {@code player}. */
	public static boolean done(ServerPlayer player, String guest, String kind) {
		return GuestOutingState.get(player.level().getServer()).done(player.getUUID(), guest, kind(kind).id, GuestOutings.day(player));
	}

	/** Records {@code kind} done today, as a completed outing would (no affection). */
	public static void markDone(ServerPlayer player, String guest, String kind) {
		GuestOutingState.get(player.level().getServer()).markDone(player.getUUID(), guest, kind(kind).id, GuestOutings.day(player));
	}

	/** Sets the player's home visit spot where they stand, as {@code /guests visit spot} does. */
	public static GuestResult setSpot(ServerPlayer player) {
		return GuestVisits.setSpot(player, null);
	}

	/** The player's saved home visit spot, or null. */
	public static @Nullable BlockPos spot(MinecraftServer server, UUID player) {
		GuestOutingState.Spot spot = GuestOutingState.get(server).spot(player);
		return spot == null ? null : spot.pos();
	}

	/** The outing state's saved data, written with its codec as the game writes it. */
	public static Tag save(MinecraftServer server) {
		return GuestOutingState.CODEC.encodeStart(NbtOps.INSTANCE, GuestOutingState.get(server)).getOrThrow();
	}

	/** Drops the player's live record, as if it had never been kept. */
	public static void forget(MinecraftServer server, UUID player) {
		GuestOutingState.get(server).forget(player);
	}

	/** Reads {@code saved} back with the codec and puts the player's record from it into the live state. */
	public static void load(MinecraftServer server, Tag saved, UUID player) {
		GuestOutingState.get(server).restore(GuestOutingState.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow(), player);
	}

	private static GuestOutings.Kind kind(String id) {
		GuestOutings.Kind kind = GuestOutings.Kind.byId(id);
		if (kind == null) {
			throw new IllegalArgumentException("No outing kind " + id);
		}
		return kind;
	}
}
