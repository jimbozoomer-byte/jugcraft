package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Outings and home visits with a town guest (worker C; plan item 3, docs/plans/jze-guest-outings-design.md). Friendly
 * companionship for every available guest: fishing, the fair, a board game, or a visit to the player's home spot.
 * Nothing romantic, nothing sexual, no money and no items; affection only through {@link GuestRewards#grant}.
 *
 * <p><b>How.</b> The player invites the guest from its menu's Activities panel ({@link #FEATURE}, options
 * {@link #OPTIONS}). Every entry point checks again for itself: the guest in the roster and available; the player alive,
 * not a spectator, not asleep, in the guest's level within {@link GuestBalance#INTERACT_RANGE} of it; the entity alive,
 * carrying this guest id and the ledger's canonical entity for it (a copy is refused); the player holding no activity
 * with any guest; the guest free ({@link GuestActivity}); the guest not keeping a fixed post (an operator's patrol or
 * wander 0, whose patrol goal ignores an activity's anchor); this kind not done today with this guest. A place is found
 * near the player in loaded chunks only ({@link GuestOutingPlaces}; a visit uses the saved spot, {@link GuestVisits}),
 * protected for neither party. Then {@link GuestActivity#begin} holds the guest there and its usual goals walk it over.
 *
 * <p><b>While it lasts</b> (checked every {@link #STEP_TICKS} ticks against the overworld's game time): together time
 * grows while the player is within {@link #PRESENT_RANGE} of the guest; the guest has arrived once within
 * {@link #ARRIVED} of the place. At the kind's duration the outing completes. Affection only if the guest arrived and
 * together time is at least {@link #MOST_NUMERATOR}/{@link #MOST_DENOMINATOR} of the duration (no idle farming: leave
 * and nothing is earned); then the kind is done for today and {@link GuestRewards#grant} adds at most the kind's reward
 * within the one shared daily budget. One outing of each kind per player and guest per day; one that fell short earns
 * nothing and may be tried again.
 *
 * <p><b>Endings</b>, every one of which ends the {@link GuestActivity}, so the guest walks back to its post: completion;
 * the player's {@code end_outing}/{@code end_visit}; logout; death; spectator mode; the player in another level; the
 * player beyond {@link #LEAVE_RANGE} of the guest; the guest, once arrived, beyond {@link #STRAY_RANGE} of the place; the
 * guest or the place unloaded; the guest gone; no progress towards the place for {@link #STUCK_TICKS}; and
 * {@link #recover}: an outing or visit activity nothing here tracks (after a restart, which logged every player out, all
 * of them) or older than its kind's bound. After an ending the walk home is watched: with no progress for
 * {@link #RETURN_STUCK_TICKS} the guest is put at its post, only if the post is loaded and the body fits there without
 * collision or liquid. Nothing here loads a chunk, clones an NPC, changes or moves a block or opens storage.
 *
 * <p><b>Numbers</b> (first choices for owner review): fishing 3600 ticks (3 min), fair 3600 (3 min), board game 3000
 * (2.5 min), home visit 4800 (4 min, the walk included); bound = duration + {@link #STALE_GRACE_TICKS}; rewards 12, 12,
 * 12 and 15 affection, within the shared 75 a day. All state but {@link GuestActivity} (per guest) and
 * {@link GuestOutingState} (per player) is memory, lost on a restart, which is why a restart ends every outing.
 *
 * <p><b>Messages</b> ({@code Component.translatableWithFallback}; the coordinator copies them into en_us.json):
 * {@code screen.jugcraft.guest.option.outings.*}, {@code message.jugcraft.guest.outing.*} and
 * {@code message.jugcraft.guest.visit.*}.
 */
public final class GuestOutings {
	/** The {@link GuestActivity} kinds this feature holds guests with (only this feature makes or ends them). */
	public static final String OUTING = "outing", VISIT = "visit";

	/** What a player and a guest do together: its id, its activity kind, its duration (ticks) and its affection. */
	enum Kind {
		FISHING("fishing", OUTING, 3600, 12),
		FAIR("fair", OUTING, 3600, 12),
		BOARD_GAME("board_game", OUTING, 3000, 12),
		VISIT_HOME("visit", VISIT, 4800, 15);

		final String id;
		final String activity;
		final int duration;
		final int reward;

		Kind(String id, String activity, int duration, int reward) {
			this.id = id;
			this.activity = activity;
			this.duration = duration;
			this.reward = reward;
		}

		/** After this long (ticks) an activity of this kind is stale, whatever else holds. */
		long bound() {
			return (long) duration + STALE_GRACE_TICKS;
		}

		static @Nullable Kind byId(String id) {
			for (Kind kind : values()) {
				if (kind.id.equals(id)) {
					return kind;
				}
			}
			return null;
		}
	}

	/** Why an outing ended. */
	enum Reason {
		COMPLETED, CANCELLED, LOGOUT, DEATH, AWAY, DIMENSION, TOO_FAR, STRAYED, UNLOADED, STUCK, GONE, STALE
	}

	/** Ticks between two steps. */
	static final int STEP_TICKS = 20;
	/** The player counts as together with the guest within this many blocks of it. */
	static final double PRESENT_RANGE = 16.0;
	/** The player this far (blocks) from the guest has left: the outing ends. */
	static final double LEAVE_RANGE = 32.0;
	/** The guest, once arrived, this far (blocks) from the place has left it: the outing ends. */
	static final double STRAY_RANGE = 16.0;
	/** Arrived within this many blocks of the place: the activity's range 2, the return goal's slack 4, and 0.5. */
	static final double ARRIVED = GuestActivity.RANGE + 4.5;
	/** Ticks without getting a block nearer the place before the way counts as stuck. */
	static final int STUCK_TICKS = 300;
	/** Together time needed for affection: this fraction of the duration. */
	static final int MOST_NUMERATOR = 3, MOST_DENOMINATOR = 4;
	/** An outing or visit activity older than its duration and this many ticks is stale. */
	static final int STALE_GRACE_TICKS = 1200;
	/** The bound for an outing whose kind is not known (after a restart): the longest outing's. */
	static final long OUTING_BOUND = Math.max(Kind.FISHING.bound(), Math.max(Kind.FAIR.bound(), Kind.BOARD_GAME.bound()));
	/** Ticks without getting a block nearer its post before a returning guest is put there (if it may be). */
	static final int RETURN_STUCK_TICKS = 300;
	/** Ticks a return is watched at most. */
	static final int RETURN_WATCH_TICKS = 6000;
	/** A returning guest is home within its wander range and this many blocks of its post (the return goal's slack 4, 0.5). */
	static final double HOME_SLACK = 4.5;

	static final String INVITE_FISHING = "invite_fishing", INVITE_FAIR = "invite_fair", INVITE_BOARD_GAME = "invite_board_game",
			END_OUTING = "end_outing", SET_VISIT_SPOT = "set_visit_spot", INVITE_HOME = "invite_home", END_VISIT = "end_visit";
	/** The options, in their fixed order. */
	static final List<String> OPTIONS = List.of(INVITE_FISHING, INVITE_FAIR, INVITE_BOARD_GAME, END_OUTING, SET_VISIT_SPOT, INVITE_HOME,
			END_VISIT);

	/** One outing under way (memory only). Server thread only. */
	static final class Outing {
		final String guest;
		final Kind kind;
		final UUID player;
		final long started;
		final ResourceKey<Level> dimension;
		final BlockPos anchor;
		long lastStep;
		long together;
		boolean arrived;
		double best = Double.MAX_VALUE;
		long progressAt;

		Outing(String guest, Kind kind, UUID player, long started, ResourceKey<Level> dimension, BlockPos anchor) {
			this.guest = guest;
			this.kind = kind;
			this.player = player;
			this.started = started;
			this.dimension = dimension;
			this.anchor = anchor.immutable();
			this.lastStep = started;
			this.progressAt = started;
		}
	}

	/** A guest walking back to its post after an outing (memory only). */
	static final class Return {
		final ResourceKey<Level> dimension;
		final long since;
		double best = Double.MAX_VALUE;
		long progressAt;

		Return(ResourceKey<Level> dimension, long since) {
			this.dimension = dimension;
			this.since = since;
			this.progressAt = since;
		}
	}

	/** Outings under way, by guest. */
	static final Map<String, Outing> OUTINGS = new HashMap<>();
	/** Returns watched, by guest. */
	static final Map<String, Return> RETURNS = new HashMap<>();
	/** Guests whose outing ended while they were not loaded: their return is watched when they next load. */
	static final Set<String> RETURN_ON_LOAD = new HashSet<>();
	private static int ticks;

	/** The guest menu's Activities for outings and visits. */
	static final GuestFeature FEATURE = new GuestFeature() {
		@Override
		public String id() {
			return "outings";
		}

		@Override
		public List<String> options() {
			return OPTIONS;
		}

		@Override
		public Component label(String option) {
			String english = switch (option) {
				case INVITE_FISHING -> "Go fishing together";
				case INVITE_FAIR -> "Go to the fair together";
				case INVITE_BOARD_GAME -> "Play a board game";
				case END_OUTING -> "End the outing";
				case SET_VISIT_SPOT -> "Make this my home visit spot";
				case INVITE_HOME -> "Invite them home for a visit";
				case END_VISIT -> "End the visit";
				default -> option;
			};
			return Component.translatableWithFallback("screen.jugcraft.guest.option.outings." + option, english);
		}

		@Override
		public boolean available(ServerPlayer player, Townsfolk npc, String guest, String option) {
			return switch (option) {
				case INVITE_FISHING -> whyNot(player, npc, guest, Kind.FISHING) == null;
				case INVITE_FAIR -> whyNot(player, npc, guest, Kind.FAIR) == null;
				case INVITE_BOARD_GAME -> whyNot(player, npc, guest, Kind.BOARD_GAME) == null;
				case INVITE_HOME -> GuestVisits.mayInvite(player, npc, guest);
				case END_OUTING -> check(player, npc, guest) == null && holding(player, guest, OUTING);
				case END_VISIT -> check(player, npc, guest) == null && holding(player, guest, VISIT);
				case SET_VISIT_SPOT -> check(player, npc, guest) == null;
				default -> false;
			};
		}

		@Override
		public GuestResult act(ServerPlayer player, Townsfolk npc, String guest, String option) {
			return switch (option) {
				case INVITE_FISHING -> invite(player, npc, guest, Kind.FISHING, GuestOutingPlaces.RADIUS);
				case INVITE_FAIR -> invite(player, npc, guest, Kind.FAIR, GuestOutingPlaces.RADIUS);
				case INVITE_BOARD_GAME -> invite(player, npc, guest, Kind.BOARD_GAME, GuestOutingPlaces.RADIUS);
				case INVITE_HOME -> GuestVisits.invite(player, npc, guest);
				case END_OUTING -> cancel(player, npc, guest, OUTING);
				case END_VISIT -> cancel(player, npc, guest, VISIT);
				case SET_VISIT_SPOT -> {
					GuestResult away = check(player, npc, guest);
					yield away != null ? away : GuestVisits.setSpot(player, npc);
				}
				default -> GuestResult.refused("message.jugcraft.guest.outing.unknown", "That is not something to do with %s.", npc.getDisplayName());
			};
		}
	};

	private GuestOutings() {
	}

	/** Called once from {@link GuestService#register}: the menu options, the server hooks and {@code /guests visit}. */
	public static void register() {
		GuestFeatures.register(FEATURE);
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (++ticks >= STEP_TICKS) {
				ticks = 0;
				step(server, now(server));
			}
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server -> recover(server, now(server)));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			ticks = 0;
			OUTINGS.clear();
			RETURNS.clear();
			RETURN_ON_LOAD.clear();
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> playerGone(server, handler.player.getUUID(), Reason.LOGOUT));
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayer player) {
				playerGone(player.level().getServer(), player.getUUID(), Reason.DEATH);
			}
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
			if (entity instanceof Townsfolk npc && npc.guest() != null) {
				guestUnloaded(level, npc);
			}
		});
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof Townsfolk npc && npc.guest() != null) {
				guestLoaded(level, npc);
			}
		});
		// Registered before the coordinator's /guests commands (GuestService registers its callback after calling this),
		// so the shared "guests" root carries no operator requirement; each operator child carries its own.
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> GuestVisits.register(dispatcher));
	}

	/** The clock outings run on: the overworld's game time (what {@link GuestActivity.Activity#started} records). */
	static long now(MinecraftServer server) {
		return server.overworld().getGameTime();
	}

	// ---------------------------------------------------------------- checks

	/**
	 * Why the player may not act with this guest now, or null when they may (as {@code GuestFavors} checks): the guest in
	 * the roster and available; the player alive and not a spectator; the entity alive, not removed, carrying
	 * {@code guest}, in the player's level within {@link GuestBalance#INTERACT_RANGE} of the player's eyes, and the
	 * ledger's canonical entity for the guest.
	 */
	static @Nullable GuestResult check(ServerPlayer player, @Nullable Townsfolk npc, @Nullable String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available() || guest == null) {
			Component name = npc != null ? npc.getDisplayName() : Component.literal(String.valueOf(guest));
			return GuestResult.refused("message.jugcraft.guest.unavailable", "%s is not receiving visitors yet.", name);
		}
		double reach = GuestBalance.INTERACT_RANGE;
		boolean here = npc != null && player.isAlive() && !player.isSpectator() && npc.isAlive() && !npc.isRemoved()
				&& guest.equals(npc.guest()) && npc.level() == player.level()
				&& npc.getBoundingBox().distanceToSqr(player.getEyePosition()) <= reach * reach
				&& npc.getUUID().equals(GuestLedger.get(player.level().getServer()).canonical(guest));
		return here ? null : GuestResult.refused("message.jugcraft.guest.not_here", "%s is not here to talk to.", Component.literal(entry.name()));
	}

	/**
	 * Why {@code kind} may not begin now, or null when it may: {@link #check}; the player not asleep and holding no
	 * activity with any guest; the guest free; the guest not keeping a fixed post; the kind not done today. No side
	 * effects, and cheap: the menu asks at every sync.
	 */
	static @Nullable GuestResult whyNot(ServerPlayer player, Townsfolk npc, String guest, Kind kind) {
		GuestResult away = check(player, npc, guest);
		if (away != null) {
			return away;
		}
		Component name = npc.getDisplayName();
		MinecraftServer server = player.level().getServer();
		if (player.isSleeping() || busy(server, player.getUUID())) {
			return GuestResult.refused("message.jugcraft.guest.outing.player_busy", "You are already spending time with a guest. End that first.");
		}
		if (GuestActivity.get(server).current(guest) != null) {
			return GuestResult.refused("message.jugcraft.guest.outing.guest_busy", "%s is busy right now. Try again a little later.", name);
		}
		if (keepsPost(npc)) {
			return GuestResult.refused("message.jugcraft.guest.outing.keeps_post", "%s is keeping to their post today.", name);
		}
		if (GuestOutingState.get(server).done(player.getUUID(), guest, kind.id, day(player))) {
			return GuestResult.refused("message.jugcraft.guest.outing.done_today." + kind.id, switch (kind) {
				case FISHING -> "You have already been fishing with %s today. Come back tomorrow.";
				case FAIR -> "You have already been to the fair with %s today. Come back tomorrow.";
				case BOARD_GAME -> "You have already played a board game with %s today. Come back tomorrow.";
				case VISIT_HOME -> "%s has already visited your home today. Come back tomorrow.";
			}, name);
		}
		return null;
	}

	/** Whether the player holds an activity with any guest (an outing, a visit, a ceremony, a rivalry). */
	static boolean busy(MinecraftServer server, UUID player) {
		for (GuestActivity.Activity held : GuestActivity.get(server).all().values()) {
			if (held.player().equals(player)) {
				return true;
			}
		}
		return false;
	}

	/** Whether the player holds an activity of the {@code activity} kind with this guest. */
	static boolean holding(ServerPlayer player, String guest, String activity) {
		GuestActivity.Activity held = GuestActivity.get(player.level().getServer()).current(guest);
		return held != null && held.kind().equals(activity) && held.player().equals(player.getUUID());
	}

	/**
	 * An operator's patrol or a wander of 0 keeps a custom NPC at fixed points: its patrol goal follows those, not an
	 * activity's anchor (coordinator patch requested), so such a guest is not taken on outings.
	 */
	static boolean keepsPost(Townsfolk npc) {
		return npc.custom() != null && (!npc.custom().patrol().isEmpty() || npc.custom().wander() == 0);
	}

	/** The day outings belong to: the favors' day (never before their high-water day), and never before this state's. */
	static long day(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();
		long favors = GuestFavors.State.get(server).effective(player.getUUID(), GuestFavors.day(player));
		return Math.max(favors, GuestOutingState.get(server).high(player.getUUID()));
	}

	// ---------------------------------------------------------------- beginning and cancelling

	/** Invites the guest on an outing of {@code kind}, its place searched within {@code radius} of the player. */
	static GuestResult invite(ServerPlayer player, Townsfolk npc, String guest, Kind kind, int radius) {
		GuestResult refused = whyNot(player, npc, guest, kind);
		if (refused != null) {
			return refused;
		}
		BlockPos place = GuestOutingPlaces.find(kind, player.level(), player, npc, radius);
		if (place == null) {
			return GuestResult.refused("message.jugcraft.guest.outing.no_place." + kind.id, switch (kind) {
				case FISHING -> "There is no open water near you to fish at with %s. It must be unclaimed or yours, outside town and protected areas.";
				case FAIR -> "There is no open ground near you for a fair with %s. It must be unclaimed or yours, outside town and protected areas.";
				case BOARD_GAME -> "There is nowhere near you to play with %s. Stand on clear ground that is unclaimed or yours, outside town and protected areas.";
				case VISIT_HOME -> "There is nowhere for %s to visit.";
			}, npc.getDisplayName());
		}
		return begin(player, npc, guest, kind, place);
	}

	/** Begins {@code kind} at {@code place}, checking everything again; the place is the caller's (searched, or the spot). */
	static GuestResult begin(ServerPlayer player, Townsfolk npc, String guest, Kind kind, BlockPos place) {
		GuestResult refused = whyNot(player, npc, guest, kind);
		if (refused != null) {
			return refused;
		}
		MinecraftServer server = player.level().getServer();
		long now = now(server);
		ResourceKey<Level> dimension = player.level().dimension();
		Component name = npc.getDisplayName();
		if (!GuestActivity.get(server).begin(guest, new GuestActivity.Activity(kind.activity, player.getUUID(), now, dimension, place.immutable()))) {
			return GuestResult.refused("message.jugcraft.guest.outing.guest_busy", "%s is busy right now. Try again a little later.", name);
		}
		OUTINGS.put(guest, new Outing(guest, kind, player.getUUID(), now, dimension, place));
		RETURNS.remove(guest);
		RETURN_ON_LOAD.remove(guest);
		return GuestResult.done("message.jugcraft.guest.outing.begin." + kind.id, switch (kind) {
			case FISHING -> "%s heads to the water with you to fish. Stay together for a while.";
			case FAIR -> "%s comes along to the fair with you. Stay together for a while.";
			case BOARD_GAME -> "%s sits down to a board game with you. Stay together for a while.";
			case VISIT_HOME -> "%s sets off to visit your home. Walk there together.";
		}, name);
	}

	/** The player ends their outing or visit ({@code activity}) with the guest, standing beside it. */
	static GuestResult cancel(ServerPlayer player, Townsfolk npc, String guest, String activity) {
		GuestResult away = check(player, npc, guest);
		if (away != null) {
			return away;
		}
		Component name = npc.getDisplayName();
		if (!holding(player, guest, activity)) {
			return activity.equals(VISIT)
					? GuestResult.refused("message.jugcraft.guest.visit.none", "%s is not visiting you.", name)
					: GuestResult.refused("message.jugcraft.guest.outing.none", "You are not on an outing with %s.", name);
		}
		MinecraftServer server = player.level().getServer();
		end(server, guest, Reason.CANCELLED, now(server));
		return activity.equals(VISIT)
				? GuestResult.done("message.jugcraft.guest.visit.ended", "You end the visit. %s heads back to their post.", name)
				: GuestResult.done("message.jugcraft.guest.outing.ended", "You end the outing. %s heads back to their post.", name);
	}

	// ---------------------------------------------------------------- the step

	/** Every outing, the recovery sweep and every return, at {@code now} (the overworld's game time). */
	static void step(MinecraftServer server, long now) {
		for (String guest : List.copyOf(OUTINGS.keySet())) {
			stepOuting(server, guest, now);
		}
		recover(server, now);
		for (String guest : List.copyOf(RETURNS.keySet())) {
			stepReturn(server, guest, now);
		}
	}

	/** One guest's outing and then its return, at {@code now} (game tests step one guest at a time). */
	static void stepGuest(MinecraftServer server, String guest, long now) {
		stepOuting(server, guest, now);
		stepReturn(server, guest, now);
	}

	private static void stepOuting(MinecraftServer server, String guest, long now) {
		Outing outing = OUTINGS.get(guest);
		if (outing == null) {
			return;
		}
		Reason reason = judge(server, outing, now);
		if (reason != null) {
			Component said = end(server, guest, reason, now);
			ServerPlayer player = server.getPlayerList().getPlayer(outing.player);
			if (said != null && player != null) {
				player.sendSystemMessage(said);
			}
		}
	}

	/** Why the outing ends now, or null while it goes on; counts together time and progress as it looks. */
	private static @Nullable Reason judge(MinecraftServer server, Outing outing, long now) {
		long delta = Math.max(0L, now - outing.lastStep);
		outing.lastStep = Math.max(outing.lastStep, now);
		GuestActivity.Activity held = GuestActivity.get(server).current(outing.guest);
		if (held == null || !held.kind().equals(outing.kind.activity) || !held.player().equals(outing.player)) {
			return Reason.STALE;
		}
		ServerPlayer player = server.getPlayerList().getPlayer(outing.player);
		if (player == null) {
			return Reason.LOGOUT;
		}
		if (!player.isAlive()) {
			return Reason.DEATH;
		}
		if (player.isSpectator()) {
			return Reason.AWAY;
		}
		ServerLevel level = server.getLevel(outing.dimension);
		if (level == null || player.level() != level) {
			return Reason.DIMENSION;
		}
		Townsfolk npc = loadedGuest(level, outing.guest);
		if (npc == null || !GuestOutingPlaces.loaded(level, outing.anchor)) {
			return Reason.UNLOADED;
		}
		double apart = npc.distanceToSqr(player);
		if (apart > LEAVE_RANGE * LEAVE_RANGE) {
			return Reason.TOO_FAR;
		}
		if (apart <= PRESENT_RANGE * PRESENT_RANGE) {
			outing.together += delta;
		}
		double toPlace = Math.sqrt(npc.distanceToSqr(Vec3.atBottomCenterOf(outing.anchor)));
		if (toPlace <= ARRIVED) {
			outing.arrived = true;
		}
		if (now - outing.started >= outing.kind.duration) {
			return Reason.COMPLETED;
		}
		if (outing.arrived) {
			return toPlace > STRAY_RANGE ? Reason.STRAYED : null;
		}
		if (toPlace < outing.best - 1.0) {
			outing.best = toPlace;
			outing.progressAt = now;
			return null;
		}
		return now - outing.progressAt >= STUCK_TICKS ? Reason.STUCK : null;
	}

	/**
	 * Ends the guest's outing or visit for {@code reason}: the {@link GuestActivity} (if an outing or visit holds the
	 * guest; only this feature makes those), the tracking, the reward when completed, and then the return watch. Returns
	 * what the player is told, or null (nothing was tracked, or nothing to say).
	 */
	static @Nullable Component end(MinecraftServer server, String guest, Reason reason, long now) {
		Outing outing = OUTINGS.remove(guest);
		GuestActivity activities = GuestActivity.get(server);
		GuestActivity.Activity held = activities.current(guest);
		ResourceKey<Level> where = outing != null ? outing.dimension : null;
		if (held != null && (held.kind().equals(OUTING) || held.kind().equals(VISIT))) {
			activities.end(guest, held.kind());
			where = held.dimension();
		}
		if (where != null) {
			watchReturn(server, guest, where, now);
		}
		if (outing == null) {
			return null;
		}
		Component name = guestName(guest);
		return switch (reason) {
			case COMPLETED -> completed(server, outing, name);
			case CANCELLED, LOGOUT -> null;
			case DEATH, AWAY, DIMENSION, TOO_FAR -> message("apart", "You and %s got separated, so your time together is over. They head back to their post.", name);
			case STRAYED -> message("strayed", "%s wandered away from the outing and heads back to their post.", name);
			case UNLOADED -> message("unloaded", "Your time with %s ended: that part of the world is no longer loaded.", name);
			case STUCK -> message("stuck", "%s could not find a way there and heads back to their post.", name);
			case GONE -> message("gone", "%s is no longer here, so your time together is over.", name);
			case STALE -> message("stale", "Your time with %s has ended.", name);
		};
	}

	/** The reward for a completed outing, if earned; what the player is told. */
	private static Component completed(MinecraftServer server, Outing outing, Component name) {
		ServerPlayer player = server.getPlayerList().getPlayer(outing.player);
		boolean earned = outing.arrived && outing.together * MOST_DENOMINATOR >= (long) outing.kind.duration * MOST_NUMERATOR;
		if (!earned || player == null) {
			return message("short." + outing.kind.id,
					"You and %s did not spend enough of the time together, so it earns no affection. You can try again today.", name);
		}
		GuestOutingState.get(server).markDone(player.getUUID(), outing.guest, outing.kind.id, day(player));
		int added = GuestRewards.grant(player, outing.guest, outing.kind.reward);
		String what = switch (outing.kind) {
			case FISHING -> "fishing";
			case FAIR -> "the fair";
			case BOARD_GAME -> "the board game";
			case VISIT_HOME -> "the visit";
		};
		return added > 0
				? message("done." + outing.kind.id, "You and %s enjoyed " + what + " together: +%s affection.", name, added)
				: message("done_capped." + outing.kind.id,
						"You and %s enjoyed " + what + " together. You have earned all the affection you can with them today.", name);
	}

	private static Component message(String key, String english, Object... args) {
		return Component.translatableWithFallback("message.jugcraft.guest.outing." + key, english, args);
	}

	private static Component guestName(String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		return Component.literal(entry != null ? entry.name() : guest);
	}

	/** The guest's canonical entity, if it is loaded in {@code level} (never loads anything); else null. */
	static @Nullable Townsfolk loadedGuest(ServerLevel level, String guest) {
		UUID id = GuestLedger.get(level.getServer()).canonical(guest);
		return id != null && level.getEntity(id) instanceof Townsfolk npc && guest.equals(npc.guest()) && npc.isAlive() && !npc.isRemoved()
				? npc : null;
	}

	// ---------------------------------------------------------------- endings from outside

	/** The player logged out or died: every outing or visit of theirs ends. */
	static void playerGone(MinecraftServer server, UUID player, Reason reason) {
		long now = now(server);
		for (Outing outing : List.copyOf(OUTINGS.values())) {
			if (outing.player.equals(player)) {
				Component said = end(server, outing.guest, reason, now);
				ServerPlayer online = reason == Reason.LOGOUT ? null : server.getPlayerList().getPlayer(player);
				if (said != null && online != null) {
					online.sendSystemMessage(said);
				}
			}
		}
	}

	/** The guest's entity left its level (unloaded with its chunk, changed level, killed or removed). */
	static void guestUnloaded(ServerLevel level, Townsfolk npc) {
		String guest = npc.guest();
		if (guest == null || !OUTINGS.containsKey(guest) && !RETURNS.containsKey(guest)) {
			return;   // memory first: nothing saved is read for a guest this feature does not hold
		}
		MinecraftServer server = level.getServer();
		if (!npc.getUUID().equals(GuestLedger.get(server).canonical(guest))) {
			return;
		}
		Outing outing = OUTINGS.get(guest);
		if (outing != null) {
			boolean gone = npc.getRemovalReason() != null && npc.getRemovalReason().shouldDestroy();
			Component said = end(server, guest, gone ? Reason.GONE : Reason.UNLOADED, now(server));
			ServerPlayer online = server.getPlayerList().getPlayer(outing.player);
			if (said != null && online != null) {
				online.sendSystemMessage(said);
			}
		}
		if (RETURNS.remove(guest) != null) {
			RETURN_ON_LOAD.add(guest);
		}
	}

	/** A guest loaded again: if its outing ended while it was away, its walk home is watched from now. */
	static void guestLoaded(ServerLevel level, Townsfolk npc) {
		String guest = npc.guest();
		if (guest == null || !RETURN_ON_LOAD.contains(guest)) {
			return;
		}
		MinecraftServer server = level.getServer();
		if (!npc.getUUID().equals(GuestLedger.get(server).canonical(guest))) {
			return;
		}
		RETURN_ON_LOAD.remove(guest);
		if (GuestActivity.get(server).current(guest) == null) {
			RETURNS.put(guest, new Return(level.dimension(), now(server)));
		}
	}

	// ---------------------------------------------------------------- recovery

	/**
	 * Ends every outing or visit activity nothing here tracks (after a restart, all of them: tracking is memory, and the
	 * shutdown logged every player out) or older than its kind's bound, and watches those guests' walk home. Called when
	 * the server has started and in every step. Returns how many it ended.
	 */
	static int recover(MinecraftServer server, long now) {
		int ended = 0;
		for (Map.Entry<String, GuestActivity.Activity> entry : GuestActivity.get(server).all().entrySet()) {
			GuestActivity.Activity held = entry.getValue();
			boolean visit = held.kind().equals(VISIT);
			if (!visit && !held.kind().equals(OUTING)) {
				continue;   // a ceremony or a rivalry: another feature's
			}
			Outing outing = OUTINGS.get(entry.getKey());
			boolean tracked = outing != null && outing.kind.activity.equals(held.kind()) && outing.player.equals(held.player());
			long bound = visit ? Kind.VISIT_HOME.bound() : OUTING_BOUND;
			if (!tracked || now - held.started() > bound) {
				Component said = end(server, entry.getKey(), Reason.STALE, now);
				ServerPlayer player = server.getPlayerList().getPlayer(held.player());
				if (said != null && player != null) {
					player.sendSystemMessage(said);
				}
				ended++;
			}
		}
		return ended;
	}

	// ---------------------------------------------------------------- the walk home

	/** Watches the guest's walk home from now: at once if it is loaded, else when it next loads. */
	private static void watchReturn(MinecraftServer server, String guest, ResourceKey<Level> dimension, long now) {
		ServerLevel level = server.getLevel(dimension);
		Townsfolk npc = level == null ? null : loadedGuest(level, guest);
		if (npc == null) {
			RETURNS.remove(guest);
			RETURN_ON_LOAD.add(guest);
			return;
		}
		// Its anchor is its post again; a path still running to the old place is dropped, so it turns for home now.
		npc.getNavigation().stop();
		RETURNS.put(guest, new Return(dimension, now));
	}

	private static void stepReturn(MinecraftServer server, String guest, long now) {
		Return back = RETURNS.get(guest);
		if (back == null) {
			return;
		}
		if (GuestActivity.get(server).current(guest) != null || now - back.since > RETURN_WATCH_TICKS) {
			RETURNS.remove(guest);   // held again (it walks there instead), or watched long enough
			return;
		}
		ServerLevel level = server.getLevel(back.dimension);
		Townsfolk npc = level == null ? null : loadedGuest(level, guest);
		if (npc == null) {
			RETURNS.remove(guest);
			RETURN_ON_LOAD.add(guest);
			return;
		}
		BlockPos post = npc.home();
		double distance = Math.sqrt(npc.distanceToSqr(Vec3.atBottomCenterOf(post)));
		if (distance <= npc.range() + HOME_SLACK) {
			RETURNS.remove(guest);
			return;
		}
		if (distance < back.best - 1.0) {
			back.best = distance;
			back.progressAt = now;
			return;
		}
		if (now - back.progressAt < RETURN_STUCK_TICKS) {
			return;
		}
		if (putHome(level, npc, post)) {
			RETURNS.remove(guest);
		} else {
			back.progressAt = now;   // the post is not loaded or not clear: wait another stretch
		}
	}

	/**
	 * Puts a stuck guest at its post: only when the post's blocks are loaded and its body fits there without touching a
	 * block, a colliding entity or a liquid. False, with nothing moved, otherwise.
	 */
	static boolean putHome(ServerLevel level, Townsfolk npc, BlockPos post) {
		if (!GuestOutingPlaces.loaded(level, post.below()) || !GuestOutingPlaces.loaded(level, post)
				|| !GuestOutingPlaces.loaded(level, post.above())) {
			return false;
		}
		Vec3 to = Vec3.atBottomCenterOf(post);
		AABB body = npc.getBoundingBox().move(to.subtract(npc.position()));
		if (!level.noCollision(npc, body) || level.containsAnyLiquid(body)) {
			return false;
		}
		npc.getNavigation().stop();
		npc.teleportTo(to.x, to.y, to.z);
		return true;
	}
}
