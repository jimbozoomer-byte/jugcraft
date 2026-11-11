package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.brigadier.CommandDispatcher;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The optional church ceremony (worker D): purely cosmetic, for a couple the ledger already has married, once per
 * marriage. Adults only: the guest's {@code mayMarry()} must be true (owner rule, 11 November); held guests never reach
 * it, because the ledger cannot marry them. It never makes, changes or ends a marriage: {@link GuestLedger} is only read,
 * and its spouses, cooldowns, confirmations and exclusivity stay exactly as they are.
 *
 * <ol>
 * <li><b>Request</b> ({@link #request}) and <b>confirm</b> ({@link #confirm}), as {@link GuestMarriage} asks for a proposal:
 * the request lives {@link #CONFIRM_TICKS} in memory, and every check is made again on confirmation.</li>
 * <li>Confirmed, it holds the guest with {@link GuestActivity} kind {@value #KIND}, anchored by the Priest of Notch
 * ({@link GuestCeremonyVenue}), and the guest walks there with its usual goals.</li>
 * <li>When the guest is within {@link #ARRIVE} blocks of the anchor and the player within {@link #ATTEND}, the rite runs:
 * {@link #LINES} lines of the priest, {@link #LINE_TICKS} apart, with bells, chimes and particles. Then the ceremony is
 * recorded for this marriage and its bouquet ({@link GuestKeepsakes}) is given.</li>
 * <li>It ends cleanly, the activity ended and nothing recorded, on cancel, a lapsed request, the walk taking longer than
 * {@link #WALK_TICKS}, the whole lasting longer than {@link #MAX_TICKS}, the player leaving (beyond {@link #LEAVE},
 * another level, logout, death, spectating), the guest, the priest or the church unloading, a divorce, the activity
 * taken, or the server stopping. After a restart a ceremony activity nobody runs is ended ({@link #recover}).</li>
 * </ol>
 *
 * <p>It changes no block, moves or removes no church object or slot machine, opens no storage and loads no chunk.
 * Invitations are voluntary: the players the couple names ({@code /guests ceremony invite}), and, only when the couple's
 * player shares their notices ({@link GuestNotices}), opted-in players nearby. Nobody else is told.
 */
public final class GuestCeremonies {
	/** The {@link GuestActivity} kind a ceremony holds its guest with. */
	public static final String KIND = "ceremony";
	/** A request must be confirmed within this many ticks (as a proposal must). */
	public static final int CONFIRM_TICKS = GuestBalance.CONFIRM_TICKS;
	/** The guest and the player must reach the church within this many ticks of confirming (2 minutes). */
	public static final int WALK_TICKS = 2400;
	/** The priest's lines, and the ticks between them. */
	public static final int LINES = 5, LINE_TICKS = 60;
	/** No ceremony lasts longer than this (3 minutes). */
	public static final int MAX_TICKS = 3600;
	/** The guest is at the church within this many blocks of the anchor; the player within {@link #ATTEND}. */
	public static final double ARRIVE = 6.5, ATTEND = 16.0;
	/** A player farther than this from the anchor has left the ceremony. */
	public static final double LEAVE = 48.0;
	/** Invited players within this many blocks of the anchor hear the priest; opted-in players within it may be invited. */
	public static final double NEARBY = 48.0;
	/** At most this many players named by the couple are invited. */
	public static final int MAX_INVITES = 8;

	/** Why a running ceremony ended. */
	enum End {
		DONE, CANCELLED, TIMEOUT, LEFT, GUEST_AWAY, PRIEST_AWAY, UNLOADED, NOT_MARRIED, TAKEN, STOPPED
	}

	private record Pending(String guest, long asked) {
	}

	/** A confirmed ceremony, in memory. Times are the server's tick count. */
	static final class Running {
		final String guest;
		final UUID player;
		final UUID npc;
		final long serial;
		final ResourceKey<Level> dimension;
		final BlockPos anchor;
		final UUID priest;
		final AABB church;
		long started;
		long riteAt = -1L;
		int line;
		final Set<UUID> invited = new LinkedHashSet<>();

		Running(String guest, UUID player, UUID npc, long serial, ResourceKey<Level> dimension, BlockPos anchor, UUID priest, AABB church,
				long started) {
			this.guest = guest;
			this.player = player;
			this.npc = npc;
			this.serial = serial;
			this.dimension = dimension;
			this.anchor = anchor;
			this.priest = priest;
			this.church = church;
			this.started = started;
		}
	}

	private static final Map<UUID, Pending> PENDING = new HashMap<>();
	/** Running ceremonies by player (one each; one per guest too). */
	private static final Map<UUID, Running> RUNNING = new HashMap<>();
	/** Players named while a request waits for confirmation. */
	private static final Map<UUID, Set<UUID>> NAMED = new HashMap<>();

	private GuestCeremonies() {
	}

	/**
	 * {@code /guests ceremony invite <players>} and {@code /guests ceremony cancel}: player commands, the requirement on
	 * this subcommand (never on the shared {@code /guests} root), checked again when they run.
	 */
	static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("guests").then(Commands.literal("ceremony").requires(GuestNotices::player)
				.then(Commands.literal("invite").then(Commands.argument("players", EntityArgument.players())
						.executes(c -> invite(c.getSource(), EntityArgument.getPlayers(c, "players")))))
				.then(Commands.literal("cancel").executes(c -> cancelCommand(c.getSource())))));
	}

	// ---------------------------------------------------------------- request, confirm, cancel

	/** Asks for a ceremony with {@code guest} (the player beside its canonical entity); nothing held yet. */
	public static GuestResult request(ServerPlayer player, Townsfolk npc, String guest) {
		GuestResult away = GuestMilestones.beside(player, npc, guest);
		if (away != null) {
			return away;
		}
		MinecraftServer server = player.level().getServer();
		GuestResult why = eligible(server, player.getUUID(), guest);
		if (why != null) {
			return why;
		}
		GuestCeremonyVenue.Found found = GuestCeremonyVenue.find(server, guest);
		GuestResult place = venueRefusal(found, npc);
		if (place != null) {
			return place;
		}
		PENDING.put(player.getUUID(), new Pending(guest, now(server)));
		return GuestResult.done("message.jugcraft.guest.ceremony.asked",
				"Hold a ceremony with %s at the Church of Notch? Confirm within %s seconds, or cancel. It is a celebration only: "
						+ "your marriage stays exactly as it is.", name(guest), CONFIRM_TICKS / 20);
	}

	/** Confirms the player's request: every check again, then the guest sets off for the church. */
	public static GuestResult confirm(ServerPlayer player, Townsfolk npc, String guest) {
		GuestResult away = GuestMilestones.beside(player, npc, guest);
		if (away != null) {
			return away;
		}
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		Pending pending = live(server, me);
		if (pending == null || !pending.guest().equals(guest)) {
			return GuestResult.refused("message.jugcraft.guest.ceremony.none",
					"There is no ceremony with %s waiting to be confirmed (it may have lapsed).", name(guest));
		}
		PENDING.remove(me);   // used up from here, held or not
		GuestResult why = eligible(server, me, guest);
		if (why != null) {
			return why;
		}
		GuestCeremonyVenue.Found found = GuestCeremonyVenue.find(server, guest);
		GuestResult place = venueRefusal(found, npc);
		if (place != null) {
			return place;
		}
		GuestCeremonyVenue.Venue venue = found.venue();
		GuestMilestoneState.Marriage marriage = GuestMilestones.marriage(server, me);
		if (marriage == null || !marriage.guest.equals(guest) || marriage.ceremony) {
			return notMarried(guest);
		}
		long now = now(server);
		GuestActivity.Activity held = new GuestActivity.Activity(KIND, me, server.overworld().getGameTime(), venue.level().dimension(),
				venue.anchor());
		if (!GuestActivity.get(server).begin(guest, held)) {
			return busy(guest);
		}
		Running running = new Running(guest, me, npc.getUUID(), marriage.serial, venue.level().dimension(), venue.anchor(),
				venue.priest().getUUID(), venue.church(), now);
		RUNNING.put(me, running);
		Set<UUID> named = NAMED.remove(me);
		if (named != null) {
			for (UUID invitee : named) {
				ServerPlayer other = server.getPlayerList().getPlayer(invitee);
				if (other != null) {
					running.invited.add(invitee);
				}
			}
		}
		running.invited.addAll(GuestNotices.inviteNearby(server, player, guest, venue.level(), venue.anchor(), NEARBY));
		venue.level().playSound(null, venue.anchor(), SoundEvents.BELL_BLOCK, SoundSource.NEUTRAL, 1.0F, 1.0F);
		return GuestResult.done("message.jugcraft.guest.ceremony.confirmed",
				"%s sets off for the Church of Notch. Meet them beside the Priest of Notch.", name(guest));
	}

	/** Withdraws the player's request, or ends their running ceremony, with {@code guest}; nothing is recorded. */
	public static GuestResult cancel(ServerPlayer player, String guest) {
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		Pending pending = live(server, me);
		if (pending != null && pending.guest().equals(guest)) {
			PENDING.remove(me);
			NAMED.remove(me);
			return GuestResult.done("message.jugcraft.guest.ceremony.withdrawn", "You withdrew the ceremony with %s.", name(guest));
		}
		Running running = RUNNING.get(me);
		if (running != null && running.guest.equals(guest)) {
			end(server, running, End.CANCELLED);
			return GuestResult.done("message.jugcraft.guest.ceremony.cancelled",
					"The ceremony with %s is called off. Nothing has changed.", name(guest));
		}
		return GuestResult.refused("message.jugcraft.guest.ceremony.cancel_none", "There is no ceremony with %s to cancel.", name(guest));
	}

	/** Whether a request now could pass its own checks (all but the venue): for the menu, cheap and without side effects. */
	static boolean mayRequest(ServerPlayer player, String guest) {
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		Pending pending = PENDING.get(me);
		return eligible(server, me, guest) == null && (pending == null || !pending.guest().equals(guest)
				|| now(server) - pending.asked() > CONFIRM_TICKS);
	}

	/** Whether the player has a live request for {@code guest} (read only). */
	static boolean pending(ServerPlayer player, String guest) {
		Pending pending = PENDING.get(player.getUUID());
		return pending != null && pending.guest().equals(guest) && now(player.level().getServer()) - pending.asked() <= CONFIRM_TICKS;
	}

	/** Whether the player has a request or a running ceremony with {@code guest} to cancel. */
	static boolean cancellable(ServerPlayer player, String guest) {
		Running running = RUNNING.get(player.getUUID());
		return pending(player, guest) || running != null && running.guest.equals(guest);
	}

	/**
	 * Why the player may not hold a ceremony with {@code guest} now, or null: the guest marriage-eligible
	 * ({@code mayMarry}), the two married to each other in the ledger, the ceremony of this marriage not yet held, no
	 * ceremony of the player or the guest under way, and nothing else holding the guest. Reads only.
	 */
	static @Nullable GuestResult eligible(MinecraftServer server, UUID player, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available() || !entry.mayMarry()) {
			return GuestResult.refused("message.jugcraft.guest.ceremony.not_offered",
					"A church ceremony is not offered with %s.", name(guest));
		}
		GuestMilestoneState state = GuestMilestoneState.get(server);
		if (state.frozen(player)) {
			return locked();
		}
		GuestLedger ledger = GuestLedger.get(server);
		if (!guest.equals(ledger.spouseOf(player)) || !player.equals(ledger.spouseOf(guest))) {
			return notMarried(guest);
		}
		GuestMilestoneState.Marriage marriage = state.marriage(player);
		if (marriage != null && marriage.guest.equals(guest) && marriage.ceremony) {
			return GuestResult.refused("message.jugcraft.guest.ceremony.held",
					"You have already held your ceremony with %s. It is once per marriage.", name(guest));
		}
		if (RUNNING.containsKey(player) || runningFor(guest) != null) {
			return GuestResult.refused("message.jugcraft.guest.ceremony.under_way", "A ceremony with %s is already under way.", name(guest));
		}
		if (GuestActivity.get(server).current(guest) != null) {
			return busy(guest);
		}
		return null;
	}

	// ---------------------------------------------------------------- invitations

	private static int invite(CommandSourceStack source, Collection<ServerPlayer> targets) {
		ServerPlayer me = source.getPlayer();
		if (me == null) {
			source.sendFailure(Component.translatableWithFallback("message.jugcraft.guest.ceremony.invite.player",
					"Only a player can send ceremony invitations."));
			return 0;
		}
		MinecraftServer server = source.getServer();
		Pending pending = live(server, me.getUUID());
		Running running = RUNNING.get(me.getUUID());
		String guest = running != null ? running.guest : pending != null ? pending.guest() : null;
		if (guest == null) {
			source.sendFailure(Component.translatableWithFallback("message.jugcraft.guest.ceremony.invite.none",
					"Ask for a ceremony first; then you may invite up to %s players.", MAX_INVITES));
			return 0;
		}
		Set<UUID> invited = running != null ? running.invited : NAMED.computeIfAbsent(me.getUUID(), id -> new LinkedHashSet<>());
		int sent = 0;
		for (ServerPlayer target : targets) {
			if (target == me || invited.contains(target.getUUID())) {
				continue;
			}
			if (invited.size() >= MAX_INVITES) {
				break;
			}
			invited.add(target.getUUID());
			target.sendSystemMessage(Component.translatableWithFallback("message.jugcraft.guest.ceremony.invitation",
					"%s invites you to their ceremony with %s at the Church of Notch. Coming is entirely up to you.",
					me.getName(), name(guest)));
			sent++;
		}
		int count = sent;
		source.sendSuccess(() -> Component.translatableWithFallback("message.jugcraft.guest.ceremony.invite.sent",
				"Invitations sent: %s (at most %s in all).", count, MAX_INVITES), false);
		return sent;
	}

	private static int cancelCommand(CommandSourceStack source) {
		ServerPlayer me = source.getPlayer();
		if (me == null) {
			return 0;
		}
		MinecraftServer server = source.getServer();
		Pending pending = live(server, me.getUUID());
		Running running = RUNNING.get(me.getUUID());
		String guest = running != null ? running.guest : pending != null ? pending.guest() : null;
		if (guest == null) {
			source.sendFailure(Component.translatableWithFallback("message.jugcraft.guest.ceremony.cancel_nothing", "You have no ceremony to cancel."));
			return 0;
		}
		GuestResult result = cancel(me, guest);
		source.sendSuccess(result::message, false);
		return result.done() ? 1 : 0;
	}

	// ---------------------------------------------------------------- the rite

	/** Every server tick: lapsed requests go, and each running ceremony moves on ({@link #step}). */
	static void tick(MinecraftServer server) {
		long now = now(server);
		PENDING.entrySet().removeIf(entry -> now - entry.getValue().asked() > CONFIRM_TICKS);
		NAMED.keySet().removeIf(player -> !PENDING.containsKey(player));
		for (Running running : new ArrayList<>(RUNNING.values())) {
			step(server, running);
		}
	}

	/** One step of a running ceremony: its checks, then the walk or the next line, then its end. */
	static void step(MinecraftServer server, Running running) {
		if (RUNNING.get(running.player) != running) {
			return;
		}
		ServerPlayer player = server.getPlayerList().getPlayer(running.player);
		GuestActivity.Activity held = GuestActivity.get(server).current(running.guest);
		if (held == null || !KIND.equals(held.kind()) || !running.player.equals(held.player())) {
			RUNNING.remove(running.player);   // something else ended or took the activity: it is not ours to end
			tell(player, End.TAKEN, running.guest);
			return;
		}
		ServerLevel level = server.getLevel(running.dimension);
		long age = now(server) - running.started;
		if (player == null || !player.isAlive() || player.isSpectator() || level == null || player.level() != level
				|| player.position().distanceTo(Vec3.atBottomCenterOf(running.anchor)) > LEAVE) {
			end(server, running, End.LEFT);
			return;
		}
		if (!GuestKeepsakes.romance(server, running.player, running.guest)) {
			end(server, running, End.NOT_MARRIED);
			return;
		}
		if (!GuestCeremonyVenue.loaded(level, running.church)) {
			end(server, running, End.UNLOADED);
			return;
		}
		Entity guestEntity = level.getEntity(running.npc);
		if (!(guestEntity instanceof Townsfolk npc) || !npc.isAlive() || npc.isRemoved() || !running.guest.equals(npc.guest())
				|| !running.npc.equals(GuestLedger.get(server).canonical(running.guest))) {
			end(server, running, End.GUEST_AWAY);
			return;
		}
		if (!(level.getEntity(running.priest) instanceof Townsfolk priest) || !GuestCeremonyVenue.isPriest(priest)) {
			end(server, running, End.PRIEST_AWAY);
			return;
		}
		if (age > MAX_TICKS) {
			end(server, running, End.TIMEOUT);
			return;
		}
		Vec3 anchor = Vec3.atBottomCenterOf(running.anchor);
		if (running.riteAt < 0) {
			if (age > WALK_TICKS) {
				end(server, running, End.TIMEOUT);
				return;
			}
			if (npc.position().distanceTo(anchor) <= ARRIVE && player.position().distanceTo(anchor) <= ATTEND) {
				running.riteAt = now(server);
				running.line = 0;
			} else {
				return;
			}
		}
		long rite = now(server) - running.riteAt;
		while (running.line < LINES && rite >= (long) running.line * LINE_TICKS) {
			speak(level, running, priest, player, running.line);
			running.line++;
		}
		if (running.line >= LINES && rite >= (long) LINES * LINE_TICKS) {
			complete(server, running, level, player);
		}
	}

	/** The priest's line {@code line}, with its sound and particles, to the couple and the invited players nearby. */
	private static void speak(ServerLevel level, Running running, Townsfolk priest, ServerPlayer player, int line) {
		Component said = switch (line) {
			case 0 -> Component.translatableWithFallback("message.jugcraft.guest.ceremony.line.0",
					"Friends, welcome to the Church of Notch, where every block was placed with care.");
			case 1 -> Component.translatableWithFallback("message.jugcraft.guest.ceremony.line.1",
					"We gather to celebrate %s and %s, who keep each other company in this block-built world.", player.getName(),
					name(running.guest));
			case 2 -> Component.translatableWithFallback("message.jugcraft.guest.ceremony.line.2",
					"May your torches never run short, your paths stay clear of creepers, and your chests always have room.");
			case 3 -> Component.translatableWithFallback("message.jugcraft.guest.ceremony.line.3",
					"By the light of Notch, I bless this companionship. You may now bow to your guests.");
			default -> Component.translatableWithFallback("message.jugcraft.guest.ceremony.line.4",
					"Go in peace, and do come back for the bake sale.");
		};
		Component message = Component.translatable("message.jugcraft.townsfolk.says", priest.getDisplayName(), said);
		for (ServerPlayer listener : listeners(level, running, player)) {
			listener.sendSystemMessage(message);
		}
		Vec3 at = Vec3.atBottomCenterOf(running.anchor);
		level.sendParticles(ParticleTypes.NOTE, priest.getX(), priest.getY() + 2.2, priest.getZ(), 3, 0.4, 0.2, 0.4, 0.0);
		level.sendParticles(ParticleTypes.HAPPY_VILLAGER, at.x, at.y + 1.0, at.z, 8, 0.8, 0.5, 0.8, 0.0);
		if (line == 0 || line == LINES - 1) {
			level.playSound(null, running.anchor, SoundEvents.BELL_BLOCK, SoundSource.NEUTRAL, 1.0F, line == 0 ? 0.9F : 1.2F);
		} else {
			level.playSound(null, running.anchor, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.NEUTRAL, 0.8F, 0.8F + 0.1F * line);
		}
	}

	/** The couple's player and the invited players in the level within {@link #NEARBY} of the anchor. */
	private static List<ServerPlayer> listeners(ServerLevel level, Running running, ServerPlayer player) {
		List<ServerPlayer> out = new ArrayList<>();
		out.add(player);
		Vec3 at = Vec3.atBottomCenterOf(running.anchor);
		for (UUID id : running.invited) {
			ServerPlayer other = level.getServer().getPlayerList().getPlayer(id);
			if (other != null && other != player && other.level() == level && other.position().distanceTo(at) <= NEARBY) {
				out.add(other);
			}
		}
		return out;
	}

	/** The rite is over: recorded once for this marriage, the bouquet given, the activity ended. */
	private static void complete(MinecraftServer server, Running running, ServerLevel level, ServerPlayer player) {
		RUNNING.remove(running.player);
		GuestActivity.get(server).end(running.guest, KIND);
		Vec3 at = Vec3.atBottomCenterOf(running.anchor);
		level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 1.2, at.z, 24, 1.0, 0.8, 1.0, 0.02);
		level.playSound(null, running.anchor, SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.8F, 1.0F);
		GuestMilestoneState state = GuestMilestoneState.get(server);
		if (!state.ceremonyHeld(running.player, running.serial)) {
			Jugcraft.LOGGER.warn("[guests] Ceremony of {} and {} finished, but marriage {} is no longer theirs; nothing recorded",
					running.player, running.guest, running.serial);
			return;
		}
		player.sendSystemMessage(Component.translatableWithFallback("message.jugcraft.guest.ceremony.done",
				"Your ceremony with %s is complete. The town wishes you both well.", name(running.guest)));
		GuestMilestones.reach(server, running.player, running.guest, "ceremony_" + running.serial);
		GuestNotices.ceremony(server, running.player, running.guest);
	}

	/** Ends a running ceremony early: the activity ended, nothing recorded, the player told why. */
	static void end(MinecraftServer server, Running running, End why) {
		if (RUNNING.get(running.player) == running) {
			RUNNING.remove(running.player);
		}
		GuestActivity.Activity held = GuestActivity.get(server).current(running.guest);
		if (held != null && KIND.equals(held.kind()) && running.player.equals(held.player())) {
			GuestActivity.get(server).end(running.guest, KIND);
		}
		tell(server.getPlayerList().getPlayer(running.player), why, running.guest);
	}

	private static void tell(@Nullable ServerPlayer player, End why, String guest) {
		if (player == null || why == End.DONE || why == End.CANCELLED || why == End.STOPPED) {
			return;
		}
		Component message = switch (why) {
			case TIMEOUT -> Component.translatableWithFallback("message.jugcraft.guest.ceremony.end.timeout",
					"The ceremony with %s ran out of time and is called off. Nothing has changed; you may ask again.", name(guest));
			case LEFT -> Component.translatableWithFallback("message.jugcraft.guest.ceremony.end.left",
					"You left the ceremony with %s, so it is called off. Nothing has changed.", name(guest));
			case GUEST_AWAY -> Component.translatableWithFallback("message.jugcraft.guest.ceremony.end.guest",
					"%s could not be found at the church, so the ceremony is called off.", name(guest));
			case PRIEST_AWAY -> Component.translatableWithFallback("message.jugcraft.guest.ceremony.end.priest",
					"The Priest of Notch is not at the church, so the ceremony with %s is called off.", name(guest));
			case UNLOADED -> Component.translatableWithFallback("message.jugcraft.guest.ceremony.end.unloaded",
					"The Church of Notch is out of reach, so the ceremony with %s is called off.", name(guest));
			case NOT_MARRIED -> Component.translatableWithFallback("message.jugcraft.guest.ceremony.end.marriage",
					"You are no longer married to %s, so the ceremony is called off.", name(guest));
			default -> Component.translatableWithFallback("message.jugcraft.guest.ceremony.end.taken",
					"The ceremony with %s was interrupted.", name(guest));
		};
		player.sendSystemMessage(message);
	}

	// ---------------------------------------------------------------- lifecycle

	/** A divorce: the player's request goes and their ceremony ends. */
	static void divorced(MinecraftServer server, UUID player) {
		PENDING.remove(player);
		NAMED.remove(player);
		Running running = RUNNING.get(player);
		if (running != null) {
			end(server, running, End.NOT_MARRIED);
		}
	}

	/** The player left the game: their request goes and their ceremony ends. */
	static void left(MinecraftServer server, UUID player) {
		PENDING.remove(player);
		NAMED.remove(player);
		Running running = RUNNING.get(player);
		if (running != null) {
			end(server, running, End.LEFT);
		}
	}

	/** Once a second: a ceremony activity nobody here runs (after a restart or a crash) is ended. */
	static void recover(MinecraftServer server, Map<String, GuestActivity.Activity> held) {
		held.forEach((guest, activity) -> {
			if (KIND.equals(activity.kind()) && runningFor(guest) == null) {
				GuestActivity.get(server).end(guest, KIND);
				Jugcraft.LOGGER.info("[guests] Ended a leftover ceremony activity of {} (none is running)", guest);
			}
		});
	}

	/** The server is stopping: every ceremony's activity is ended, so none is saved half done. */
	static void stopping(MinecraftServer server) {
		for (Running running : new ArrayList<>(RUNNING.values())) {
			end(server, running, End.STOPPED);
		}
		clear();
	}

	static void clear() {
		PENDING.clear();
		RUNNING.clear();
		NAMED.clear();
	}

	// ---------------------------------------------------------------- helpers

	private static @Nullable Running runningFor(String guest) {
		for (Running running : RUNNING.values()) {
			if (running.guest.equals(guest)) {
				return running;
			}
		}
		return null;
	}

	/** The player's request if still live; a lapsed one is dropped. */
	private static @Nullable Pending live(MinecraftServer server, UUID player) {
		Pending pending = PENDING.get(player);
		if (pending != null && now(server) - pending.asked() > CONFIRM_TICKS) {
			PENDING.remove(player);
			NAMED.remove(player);
			return null;
		}
		return pending;
	}

	private static long now(MinecraftServer server) {
		return server.getTickCount();
	}

	/** Why the venue will not do, or null: no town, the church or priest not loaded, or the guest in another level. */
	private static @Nullable GuestResult venueRefusal(GuestCeremonyVenue.Found found, Townsfolk npc) {
		if (found.venue() == null) {
			GuestCeremonyVenue.Refusal why = found.refusal() == null ? GuestCeremonyVenue.Refusal.NO_TOWN : found.refusal();
			return switch (why) {
				case NO_TOWN -> GuestResult.refused("message.jugcraft.guest.ceremony.no_town",
						"There is no town church in this world for a ceremony.");
				case NOT_LOADED -> GuestResult.refused("message.jugcraft.guest.ceremony.not_loaded",
						"The Church of Notch is not loaded just now. Go nearer to it first: a ceremony never loads the town.");
				case NO_PRIEST -> GuestResult.refused("message.jugcraft.guest.ceremony.no_priest",
						"The Priest of Notch is not at the church just now. Try again when the priest is in.");
			};
		}
		if (npc.level() != found.venue().level()) {
			return GuestResult.refused("message.jugcraft.guest.ceremony.other_world",
					"The Church of Notch is in another world from here.");
		}
		return null;
	}

	private static GuestResult notMarried(String guest) {
		return GuestResult.refused("message.jugcraft.guest.ceremony.not_married",
				"Only a couple already married in town may hold a ceremony; you are not married to %s.", name(guest));
	}

	private static GuestResult busy(String guest) {
		return GuestResult.refused("message.jugcraft.guest.ceremony.busy", "%s is busy with something else just now.", name(guest));
	}

	private static GuestResult locked() {
		return GuestResult.refused("message.jugcraft.guest.milestone.locked",
				"Your keepsake record cannot be read just now; an operator can check it.");
	}

	private static Component name(String guest) {
		return GuestMilestones.name(guest);
	}

	// ---------------------------------------------------------------- tests

	/** The player's running ceremony, or null (tests). */
	static @Nullable Running running(UUID player) {
		return RUNNING.get(player);
	}

	/** Makes the player's running ceremony and pending request {@code ticks} older (tests: no waiting for real time). */
	static void age(UUID player, long ticks) {
		Running running = RUNNING.get(player);
		if (running != null) {
			running.started -= ticks;
			if (running.riteAt >= 0) {
				running.riteAt -= ticks;
			}
		}
		Pending pending = PENDING.get(player);
		if (pending != null) {
			PENDING.put(player, new Pending(pending.guest(), pending.asked() - ticks));
		}
	}

	/** Runs one step of the player's running ceremony now (tests). */
	static void step(MinecraftServer server, UUID player) {
		Running running = RUNNING.get(player);
		if (running != null) {
			step(server, running);
		}
	}
}
