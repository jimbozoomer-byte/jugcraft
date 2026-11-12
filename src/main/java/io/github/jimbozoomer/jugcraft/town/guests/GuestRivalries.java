package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Friendly rivalries (worker B): a player asks the guest they are talking to (the challenger) to take on another guest
 * nearby (the rival) in an absurd race, a contest of exaggerated credit-taking or a staring contest. Short, bounded,
 * cancellable, and nonsexual and non-romantic for everyone; real people never take part.
 *
 * <p><b>Holding the guests.</b> Both guests are held with a {@link GuestActivity} of kind {@link #KIND} for the player,
 * begun together or not at all: a rivalry never starts while either guest is held by anything (or the player already
 * has one running). While held, each guest walks to its activity's anchor by its usual goals: in a race each heads for
 * the other's starting spot (they swap places); in the other two both come over to the player. Ending the activities
 * sends both back to their posts. Nothing here loads a chunk, moves a block or moves an entity itself.
 *
 * <p><b>Ending.</b> A race ends when a guest reaches its finish (within {@link #ARRIVE} blocks) or after
 * {@link #RACE_TICKS}; the others after {@link #TALK_TICKS}. It is cancelled (both activities ended, nothing granted)
 * when the player cancels it, leaves the game (disconnect, or no longer in the player list), dies, becomes a spectator,
 * changes level or wanders more than {@link #LEASH} blocks from the challenger; when either guest unloads, is removed
 * or is no longer the canonical, available guest; when anything else ends either activity; and when the server stops.
 * Running rivalries are kept in memory only: after a restart (or a crash) every saved activity of kind {@link #KIND} is
 * ended when the server starts, so none is left holding a guest.
 *
 * <p><b>Outcome.</b> Comic text to the participating player only, and a small reward: {@link #REWARD} affection with
 * each of the two guests through {@link GuestRewards#grant} (so within the shared daily budget), for the first rivalry
 * a player finishes on a day (saved in {@link GuestPersonalityState}); later ones that day are for fun only. Players are
 * never named, mocked or announced to anyone else. Messages to nearby players would need opt-in notices (worker D's
 * part); this sends to the participating player only.
 */
public final class GuestRivalries {
	/** The {@link GuestActivity} kind a rivalry holds both guests with. */
	public static final String KIND = "rivalry";
	/** How far from the challenger (blocks) a rival may stand. */
	public static final double RIVAL_RANGE = 16.0;
	/** How far (blocks) the player may wander from the challenger before the rivalry fizzles out. */
	public static final double LEASH = 24.0;
	/** How near (blocks) a racer must come to its finish. */
	public static final double ARRIVE = 2.5;
	/** The longest a race runs: 30 seconds. */
	public static final int RACE_TICKS = 600;
	/** How long a credit or staring contest runs: 10 seconds. */
	public static final int TALK_TICKS = 200;
	/** Ticks between checks of each running rivalry. */
	public static final int CHECK_TICKS = 10;
	/** Affection with each of the two guests for the first rivalry a player finishes on a day (shared budget). */
	public static final int REWARD = 4;

	/** What the two guests do. */
	public enum Kind {
		/** Swap places: each heads for the other's starting spot; first there wins. */
		RACE,
		/** Both claim credit for something nobody asked about. */
		CREDIT,
		/** A staring contest. */
		STARE
	}

	/** Why a rivalry stopped early. */
	enum Stop {
		CANCELLED, FIZZLED, QUIET
	}

	/** One running rivalry, held in memory by the player who started it. */
	record Running(Kind kind, UUID player, String first, UUID firstEntity, String second, UUID secondEntity,
			ResourceKey<Level> dimension, BlockPos firstGoal, BlockPos secondGoal, long started, long ends) {
		boolean involves(String guest) {
			return first.equals(guest) || second.equals(guest);
		}

		boolean involves(UUID entity) {
			return firstEntity.equals(entity) || secondEntity.equals(entity);
		}
	}

	/** Running rivalries by player. Server thread only. */
	static final Map<UUID, Running> RUNNING = new HashMap<>();

	private GuestRivalries() {
	}

	/** Called once from {@link GuestPersonality#register}. */
	static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (!RUNNING.isEmpty() && server.getTickCount() % CHECK_TICKS == 0) {
				tick(server);
			}
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> loggedOut(server, handler.player.getUUID()));
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
			if (!RUNNING.isEmpty() && entity instanceof Townsfolk npc && npc.guest() != null) {
				for (Running running : List.copyOf(RUNNING.values())) {
					if (running.involves(entity.getUUID())) {
						stop(level.getServer(), running, Stop.FIZZLED);
					}
				}
			}
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(GuestRivalries::stopAll);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> RUNNING.clear());
		ServerLifecycleEvents.SERVER_STARTED.register(GuestRivalries::recover);
	}

	// ---------------------------------------------------------------- the options

	/**
	 * Whether the "rivalry" option is offered: a fictional guest, free, and the player has none running and holds no other
	 * guest's activity. Cheap, read only.
	 */
	static boolean mayOffer(ServerPlayer player, String guest) {
		MinecraftServer server = player.level().getServer();
		return GuestPersonality.mayCompete(guest) && !RUNNING.containsKey(player.getUUID()) && !GuestOutings.busy(server, player.getUUID())
				&& GuestActivity.get(server).current(guest) == null;
	}

	/** Whether the player's running rivalry involves this guest (the "rivalry_cancel" option). */
	static boolean involves(ServerPlayer player, String guest) {
		Running running = RUNNING.get(player.getUUID());
		return running != null && running.involves(guest);
	}

	/** Starts a rivalry between {@code guest} and the nearest willing guest, of a random kind. */
	public static GuestResult start(ServerPlayer player, Townsfolk npc, String guest) {
		return start(player, npc, guest, null, null);
	}

	/**
	 * Starts a rivalry; {@code kind} and {@code rival} chosen here when null (a test may name them; a named rival must
	 * pass the same checks as one found). Refused, holding nothing, unless the player may act with the guest, the guest
	 * may compete, the player has no rivalry running and holds no other guest's activity (an outing, a visit, a ceremony:
	 * one at a time, as outings require), neither guest is held by anything, and an eligible rival stands within
	 * {@link #RIVAL_RANGE}.
	 */
	static GuestResult start(ServerPlayer player, Townsfolk npc, String guest, @Nullable Kind kind, @Nullable Townsfolk rival) {
		GuestResult away = GuestPersonality.check(player, npc, guest);
		if (away != null) {
			return away;
		}
		Component name = GuestPersonality.name(npc, guest);
		if (!GuestPersonality.mayCompete(guest)) {
			return GuestResult.refused("message.jugcraft.guest.rivalry.not_for", "%s would rather cheer everyone on.", name);
		}
		UUID me = player.getUUID();
		if (RUNNING.containsKey(me)) {
			return GuestResult.refused("message.jugcraft.guest.rivalry.already", "You are already watching a rivalry.");
		}
		MinecraftServer server = player.level().getServer();
		if (GuestOutings.busy(server, me)) {
			return GuestResult.refused("message.jugcraft.guest.outing.player_busy", "You are already spending time with a guest. End that first.");
		}
		GuestActivity activity = GuestActivity.get(server);
		if (activity.current(guest) != null) {
			return GuestResult.refused("message.jugcraft.guest.rivalry.busy", "%s is busy right now.", name);
		}
		Townsfolk other = rival != null ? rival : find(npc, guest);
		if (other == null) {
			return GuestResult.refused("message.jugcraft.guest.rivalry.no_rival", "Nobody nearby is up for a challenge with %s.", name);
		}
		String otherGuest = other.guest();
		if (otherGuest == null || !eligible(npc, guest, other, false)) {
			return GuestResult.refused("message.jugcraft.guest.rivalry.no_rival", "Nobody nearby is up for a challenge with %s.", name);
		}
		Component otherName = GuestPersonality.name(other, otherGuest);
		if (activity.current(otherGuest) != null) {
			return GuestResult.refused("message.jugcraft.guest.rivalry.busy", "%s is busy right now.", otherName);
		}
		RandomSource random = player.getRandom();
		Kind chosen = kind != null ? kind : Kind.values()[random.nextInt(Kind.values().length)];
		BlockPos firstGoal = chosen == Kind.RACE ? other.blockPosition() : player.blockPosition();
		BlockPos secondGoal = chosen == Kind.RACE ? npc.blockPosition() : player.blockPosition();
		ResourceKey<Level> dimension = npc.level().dimension();
		long gameTime = npc.level().getGameTime();
		if (!activity.begin(guest, new GuestActivity.Activity(KIND, me, gameTime, dimension, firstGoal.immutable()))) {
			return GuestResult.refused("message.jugcraft.guest.rivalry.busy", "%s is busy right now.", name);
		}
		if (!activity.begin(otherGuest, new GuestActivity.Activity(KIND, me, gameTime, dimension, secondGoal.immutable()))) {
			activity.end(guest, KIND);   // together or not at all
			return GuestResult.refused("message.jugcraft.guest.rivalry.busy", "%s is busy right now.", otherName);
		}
		long now = server.getTickCount();
		Running running = new Running(chosen, me, guest, npc.getUUID(), otherGuest, other.getUUID(), dimension, firstGoal.immutable(),
				secondGoal.immutable(), now, now + (chosen == Kind.RACE ? RACE_TICKS : TALK_TICKS));
		RUNNING.put(me, running);
		player.sendSystemMessage(intro(chosen, name, otherName));
		player.sendSystemMessage(GuestPersonality.says(name, GuestDialogueLines.lines(guest).boast()));
		player.sendSystemMessage(GuestPersonality.says(otherName, GuestDialogueLines.lines(otherGuest).boast()));
		return GuestResult.done("message.jugcraft.guest.rivalry.started", "%s and %s are at it. Stay nearby to see how it ends.", name, otherName);
	}

	/** The "rivalry_cancel" option: ends the player's rivalry with this guest; nothing is granted. */
	public static GuestResult cancel(ServerPlayer player, Townsfolk npc, String guest) {
		GuestResult away = GuestPersonality.check(player, npc, guest);
		if (away != null) {
			return away;
		}
		Running running = RUNNING.get(player.getUUID());
		if (running == null || !running.involves(guest)) {
			return GuestResult.refused("message.jugcraft.guest.rivalry.none", "There is no rivalry with %s to call off.", GuestPersonality.name(npc, guest));
		}
		stop(player.level().getServer(), running, Stop.QUIET);
		return GuestResult.done("message.jugcraft.guest.rivalry.cancelled", "You call off the rivalry between %s and %s. They agree to settle it another day.",
				name(running.first()), name(running.second()));
	}

	// ---------------------------------------------------------------- running

	/** Checks every running rivalry: stops one whose conditions failed, finishes one whose time or race is up. */
	static void tick(MinecraftServer server) {
		long now = server.getTickCount();
		for (Running running : List.copyOf(RUNNING.values())) {
			if (RUNNING.get(running.player()) != running) {
				continue;
			}
			ServerPlayer player = server.getPlayerList().getPlayer(running.player());
			ServerLevel level = server.getLevel(running.dimension());
			Townsfolk first = level == null ? null : guest(level, running.firstEntity(), running.first());
			Townsfolk second = level == null ? null : guest(level, running.secondEntity(), running.second());
			if (player == null || first == null || second == null || !valid(server, running, player, first)) {
				stop(server, running, player == null ? Stop.QUIET : Stop.FIZZLED);
				continue;
			}
			if (running.kind() == Kind.RACE) {
				boolean firstIn = arrived(first, running.firstGoal());
				boolean secondIn = arrived(second, running.secondGoal());
				if (firstIn || secondIn || now >= running.ends()) {
					finish(server, running, player, firstIn, secondIn);
				}
			} else if (now >= running.ends()) {
				finish(server, running, player, false, false);
			}
		}
	}

	/** A player left the game: their rivalry ends quietly. */
	static void loggedOut(MinecraftServer server, UUID player) {
		Running running = RUNNING.get(player);
		if (running != null) {
			stop(server, running, Stop.QUIET);
		}
	}

	/** The server is stopping: every rivalry ends quietly, so no activity is saved holding a guest. */
	static void stopAll(MinecraftServer server) {
		for (Running running : List.copyOf(RUNNING.values())) {
			stop(server, running, Stop.QUIET);
		}
		RUNNING.clear();
	}

	/** After a start (or a crash): ends every saved rivalry activity, none of which is running any more. */
	static void recover(MinecraftServer server) {
		GuestActivity activity = GuestActivity.get(server);
		for (Map.Entry<String, GuestActivity.Activity> held : activity.all().entrySet()) {
			if (KIND.equals(held.getValue().kind()) && !runningFor(held.getKey())) {
				activity.end(held.getKey(), KIND);
			}
		}
	}

	private static boolean runningFor(String guest) {
		for (Running running : RUNNING.values()) {
			if (running.involves(guest)) {
				return true;
			}
		}
		return false;
	}

	/** Whether the rivalry may go on: player present and near, both activities still ours, both guests still themselves. */
	private static boolean valid(MinecraftServer server, Running running, ServerPlayer player, Townsfolk first) {
		if (!player.isAlive() || player.isSpectator() || !player.level().dimension().equals(running.dimension())
				|| player.distanceToSqr(first) > LEASH * LEASH) {
			return false;
		}
		GuestActivity activity = GuestActivity.get(server);
		return ours(activity.current(running.first()), running) && ours(activity.current(running.second()), running);
	}

	private static boolean ours(GuestActivity.@Nullable Activity held, Running running) {
		return held != null && KIND.equals(held.kind()) && running.player().equals(held.player()) && running.dimension().equals(held.dimension());
	}

	/** The loaded, living, canonical entity {@code entity} still carrying the available guest {@code guest}, or null. */
	private static @Nullable Townsfolk guest(ServerLevel level, UUID entity, String guest) {
		Entity found = level.getEntity(entity);
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (!(found instanceof Townsfolk npc) || !npc.isAlive() || npc.isRemoved() || !guest.equals(npc.guest()) || entry == null
				|| !entry.available() || !entity.equals(GuestLedger.get(level.getServer()).canonical(guest))) {
			return null;
		}
		return npc;
	}

	private static boolean arrived(Townsfolk npc, BlockPos goal) {
		return npc.distanceToSqr(Vec3.atBottomCenterOf(goal)) <= ARRIVE * ARRIVE;
	}

	/** The nearest eligible rival: a preferred rival of the challenger's if one stands near, else the nearest; null if none. */
	private static @Nullable Townsfolk find(Townsfolk npc, String guest) {
		GuestPersonality.Profile profile = GuestPersonality.profile(guest);
		List<String> preferred = profile == null ? List.of() : profile.rivals();
		Townsfolk best = null;
		int bestRank = Integer.MAX_VALUE;
		double bestDistance = Double.MAX_VALUE;
		for (Townsfolk other : npc.level().getEntitiesOfClass(Townsfolk.class, npc.getBoundingBox().inflate(RIVAL_RANGE),
				other -> other != npc && other.guest() != null)) {
			if (!eligible(npc, guest, other, true)) {
				continue;
			}
			int rank = preferred.indexOf(other.guest());
			rank = rank < 0 ? preferred.size() : rank;
			double distance = other.distanceToSqr(npc);
			if (rank < bestRank || rank == bestRank && distance < bestDistance) {
				best = other;
				bestRank = rank;
				bestDistance = distance;
			}
		}
		return best;
	}

	/**
	 * Whether {@code other} may be {@code guest}'s rival: a different living canonical guest, available and fictional, in
	 * the same level within {@link #RIVAL_RANGE}, in no running rivalry, and (when {@code free}) held by no activity.
	 */
	private static boolean eligible(Townsfolk npc, String guest, Townsfolk other, boolean free) {
		String id = other.guest();
		if (id == null || id.equals(guest) || other == npc || !other.isAlive() || other.isRemoved() || other.level() != npc.level()
				|| !GuestPersonality.mayCompete(id) || other.distanceToSqr(npc) > RIVAL_RANGE * RIVAL_RANGE
				|| runningFor(id)) {
			return false;
		}
		MinecraftServer server = npc.level().getServer();
		if (server == null || !other.getUUID().equals(GuestLedger.get(server).canonical(id))) {
			return false;
		}
		return !free || GuestActivity.get(server).current(id) == null;
	}

	/** Ends both activities (ours only, not completed) and forgets the rivalry; tells the player unless it ended quietly. */
	static void stop(MinecraftServer server, Running running, Stop why) {
		RUNNING.remove(running.player(), running);
		release(server, running, false);
		ServerPlayer player = why == Stop.QUIET ? null : server.getPlayerList().getPlayer(running.player());
		if (player != null) {
			player.sendSystemMessage(Component.translatableWithFallback("message.jugcraft.guest.rivalry.fizzled",
					"The rivalry between %s and %s fizzles out. They agree to settle it another day.", name(running.first()), name(running.second())));
		}
	}

	/** Ends each of the two activities if it is still this rivalry's; {@code completed} when it was played to its end. */
	private static void release(MinecraftServer server, Running running, boolean completed) {
		GuestActivity activity = GuestActivity.get(server);
		for (String guest : List.of(running.first(), running.second())) {
			if (ours(activity.current(guest), running)) {
				activity.end(guest, KIND, completed);
			}
		}
	}

	/** The rivalry's comic ending, then the day's reward (once per player per day), then both guests go home. */
	private static void finish(MinecraftServer server, Running running, ServerPlayer player, boolean firstIn, boolean secondIn) {
		RUNNING.remove(running.player(), running);
		release(server, running, true);
		Component a = name(running.first());
		Component b = name(running.second());
		RandomSource random = player.getRandom();
		player.sendSystemMessage(outcome(running.kind(), a, b, firstIn, secondIn, random));
		long day = GuestPersonality.day(player);
		GuestPersonalityState state = GuestPersonalityState.get(server);
		if (state.rivalryRewarded(player.getUUID(), day)) {
			return;
		}
		state.rewardedRivalry(player.getUUID(), day);
		int first = GuestRewards.grant(player, running.first(), REWARD);
		int second = GuestRewards.grant(player, running.second(), REWARD);
		if (first > 0 || second > 0) {
			player.sendSystemMessage(Component.translatableWithFallback("message.jugcraft.guest.rivalry.reward",
					"What a show: +%s affection with %s, +%s with %s.", first, a, second, b));
		}
	}

	// ---------------------------------------------------------------- text

	private static Component name(String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		return Component.literal(entry != null ? entry.name() : guest);
	}

	private static Component intro(Kind kind, Component a, Component b) {
		return switch (kind) {
			case RACE -> Component.translatableWithFallback("message.jugcraft.guest.rivalry.intro.race",
					"%s challenges %s to a race: swap places, first one there wins. Nobody asked why. It is now very important.", a, b);
			case CREDIT -> Component.translatableWithFallback("message.jugcraft.guest.rivalry.intro.credit",
					"%s and %s both claim they built the town fountain. They come over to argue it out in front of you.", a, b);
			case STARE -> Component.translatableWithFallback("message.jugcraft.guest.rivalry.intro.stare",
					"%s challenges %s to a staring contest. A passing pigeon agrees to referee.", a, b);
		};
	}

	/** The ending: a race by who arrived, the others by a coin the pigeon tosses. */
	static Component outcome(Kind kind, Component a, Component b, boolean firstIn, boolean secondIn, RandomSource random) {
		boolean heads = random.nextBoolean();
		Component winner = heads ? a : b;
		Component loser = heads ? b : a;
		return switch (kind) {
			case RACE -> firstIn && secondIn
					? Component.translatableWithFallback("message.jugcraft.guest.rivalry.race.tie",
							"%s and %s arrive at exactly the same moment and declare themselves joint champions of everything.", a, b)
					: firstIn || secondIn
							? Component.translatableWithFallback("message.jugcraft.guest.rivalry.race.win",
									"%s gets there first and accepts congratulations from absolutely nobody, gracefully. %s insists their half was uphill.",
									firstIn ? a : b, firstIn ? b : a)
							: Component.translatableWithFallback("message.jugcraft.guest.rivalry.race.draw",
									"Halfway there, %s and %s stop to argue about where the finish line was. The pigeon declares a draw.", a, b);
			case CREDIT -> heads
					? Component.translatableWithFallback("message.jugcraft.guest.rivalry.credit.0",
							"%s claims credit for the fountain. %s claims credit for the water. The town agrees to put both names on a very small plaque.", a, b)
					: Component.translatableWithFallback("message.jugcraft.guest.rivalry.credit.1",
							"%s says they invented Tuesdays. %s says they invented Wednesdays. Everyone quietly agrees to keep Thursday.", a, b);
			case STARE -> Component.translatableWithFallback("message.jugcraft.guest.rivalry.stare.win",
					"%s blinks first, blaming a very dramatic gust of wind. %s celebrates for slightly too long.", loser, winner);
		};
	}
}
