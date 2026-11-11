package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.brigadier.CommandDispatcher;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Opt-in town notices (worker D). Two settings per player, kept in {@link GuestMilestoneState}, both <b>off</b> until the
 * player turns them on:
 * <ul>
 * <li><b>notices</b> (option {@code milestones/notices_toggle}, {@code /guests notices on|off}): receive fictional town
 * gossip and rivalry notices about guests, and announcements of other players' milestones and ceremonies;</li>
 * <li><b>share</b> (option {@code milestones/notices_share}, {@code /guests notices share on|off}): let my own milestones
 * and ceremonies be announced to players who receive notices.</li>
 * </ul>
 *
 * <p>Every notice goes through {@link #deliver}, which sends it only to a player with notices on, and only when every
 * player it names has share on: no relationship is ever announced without its subject's opt-in, and nothing is ever
 * broadcast to everyone. Gossip names guests only, never players. Gossip about a real-person guest is generic and about
 * town life only (errands, visitors, gifts); fictional guests may also get light, original town-flavoured lines. Nothing
 * here is sexual, mocks anyone or embarrasses a player.
 */
public final class GuestNotices {
	/** Ticks between two gossip lines for one receiving player (10 minutes). */
	public static final int GOSSIP_TICKS = 12_000;
	/** Ticks between two rivalry notices about one guest (5 minutes). */
	public static final int RIVALRY_TICKS = 6_000;

	/** A notice as sent: to whom, the players it names, and its text (tests read these). */
	public record Notice(UUID receiver, Set<UUID> named, Component text) {
	}

	/** Gossip about any guest: town life only, safe for real-person guests. */
	private static final String[][] TOWN = {
			{"town.0", "Town notice: %s is receiving visitors in town today."},
			{"town.1", "The town board says %s has an errand for anyone who would like to help."},
			{"town.2", "The Priest of Notch reminds everyone to make town guests such as %s feel welcome."},
			{"town.3", "The florist reports that flowers are a popular gift for town guests such as %s."},
			{"town.4", "Word in the square: the town is glad to have %s as a guest."}};
	/** Light, original town gossip for fictional guests only. */
	private static final String[][] FICTION = {
			{"fiction.0", "Town gossip: a baker swears %s counted every cobblestone in the square. Twice."},
			{"fiction.1", "Town gossip: the guards say %s challenged a scarecrow to a staring contest. It was declared a draw."},
			{"fiction.2", "Town gossip: %s was seen giving very serious directions to a lost chicken."},
			{"fiction.3", "Town gossip: the Priest of Notch says %s asked whether Notch accepts compliments on the windows."},
			{"fiction.4", "Town gossip: %s and the mayor debated the best kind of bread for an hour. Nobody won."}};

	private static final Map<UUID, Long> NEXT_GOSSIP = new HashMap<>();
	private static final Map<String, Long> LAST_RIVALRY = new HashMap<>();
	private static final Map<String, GuestActivity.Activity> RIVALRIES = new HashMap<>();
	/** Game tests only: the notices sent to these receivers are kept here. Empty in play. */
	static final Map<UUID, List<Notice>> RECORDED = new ConcurrentHashMap<>();

	private GuestNotices() {
	}

	/**
	 * {@code /guests notices on|off} and {@code /guests notices share on|off}: a player command. The {@code /guests} root
	 * carries no requirement (Brigadier keeps the first root registration's); this subcommand requires a player, checked
	 * again when it runs.
	 */
	static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("guests").then(Commands.literal("notices").requires(GuestNotices::player)
				.then(Commands.literal("on").executes(c -> command(c.getSource(), true, null)))
				.then(Commands.literal("off").executes(c -> command(c.getSource(), false, null)))
				.then(Commands.literal("share")
						.then(Commands.literal("on").executes(c -> command(c.getSource(), null, true)))
						.then(Commands.literal("off").executes(c -> command(c.getSource(), null, false))))));
	}

	/** A player's command source (the notices and ceremony subcommands' requirement). */
	static boolean player(CommandSourceStack source) {
		return source.getPlayer() != null;
	}

	private static int command(CommandSourceStack source, @Nullable Boolean receive, @Nullable Boolean share) {
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			source.sendFailure(Component.translatableWithFallback("message.jugcraft.guest.notices.player", "Only a player has notice settings."));
			return 0;
		}
		MinecraftServer server = source.getServer();
		UUID me = player.getUUID();
		GuestResult result = set(server, me, receive != null ? receive : receives(server, me), share != null ? share : shares(server, me));
		if (result.done()) {
			source.sendSuccess(result::message, false);
		} else {
			source.sendFailure(result.message());
		}
		return result.done() ? 1 : 0;
	}

	// ---------------------------------------------------------------- settings

	/** Whether the player receives notices (off unless they turned it on). */
	public static boolean receives(MinecraftServer server, UUID player) {
		return GuestMilestoneState.get(server).notices(player);
	}

	/** Whether the player lets their own milestones and ceremonies be announced (off unless they turned it on). */
	public static boolean shares(MinecraftServer server, UUID player) {
		return GuestMilestoneState.get(server).share(player);
	}

	/** Menu option {@code notices_toggle}: the player beside the guest, then notices on or off. */
	static GuestResult toggle(ServerPlayer player, Townsfolk npc, String guest) {
		GuestResult away = GuestMilestones.beside(player, npc, guest);
		if (away != null) {
			return away;
		}
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		return set(server, me, !receives(server, me), shares(server, me));
	}

	/** Menu option {@code notices_share}: the player beside the guest, then sharing on or off. */
	static GuestResult toggleShare(ServerPlayer player, Townsfolk npc, String guest) {
		GuestResult away = GuestMilestones.beside(player, npc, guest);
		if (away != null) {
			return away;
		}
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		return set(server, me, receives(server, me), !shares(server, me));
	}

	private static GuestResult set(MinecraftServer server, UUID player, boolean receive, boolean share) {
		if (!GuestMilestoneState.get(server).notices(player, receive, share)) {
			return GuestResult.refused("message.jugcraft.guest.milestone.locked",
					"Your keepsake record cannot be read just now; an operator can check it.");
		}
		if (!receive) {
			NEXT_GOSSIP.remove(player);
		}
		return GuestResult.done("message.jugcraft.guest.notices.settings",
				"Town notices: %s. Mentioning you in notices: %s.", onOff(receive), onOff(share));
	}

	private static Component onOff(boolean on) {
		return on ? Component.translatableWithFallback("message.jugcraft.guest.notices.on", "on")
				: Component.translatableWithFallback("message.jugcraft.guest.notices.off", "off");
	}

	// ---------------------------------------------------------------- sending

	/**
	 * Sends {@code text} to {@code receiver} as a notice: only when the receiver has notices on and every player in
	 * {@code named} has share on. The one way a notice is sent. Returns whether it was sent.
	 */
	static boolean deliver(MinecraftServer server, ServerPlayer receiver, Set<UUID> named, Component text) {
		if (!receives(server, receiver.getUUID())) {
			return false;
		}
		for (UUID subject : named) {
			if (!shares(server, subject)) {
				return false;
			}
		}
		receiver.sendSystemMessage(text);
		List<Notice> tap = RECORDED.get(receiver.getUUID());
		if (tap != null) {
			tap.add(new Notice(receiver.getUUID(), Set.copyOf(named), text));
		}
		return true;
	}

	/**
	 * Announces {@code text}, which names {@code subject}, to every other online player who receives notices, only if the
	 * subject shares; the players it reached.
	 */
	static List<ServerPlayer> announce(MinecraftServer server, UUID subject, Component text) {
		List<ServerPlayer> reached = new ArrayList<>();
		if (!shares(server, subject)) {
			return reached;
		}
		for (ServerPlayer other : List.copyOf(server.getPlayerList().getPlayers())) {
			if (!other.getUUID().equals(subject) && deliver(server, other, Set.of(subject), text)) {
				reached.add(other);
			}
		}
		return reached;
	}

	/** A milestone of {@code subject} with {@code guest}, announced only if the subject shares. */
	static List<ServerPlayer> milestone(MinecraftServer server, UUID subject, String guest, String key) {
		ServerPlayer player = server.getPlayerList().getPlayer(subject);
		if (player == null || !shares(server, subject)) {
			return List.of();
		}
		Component name = GuestMilestones.name(guest);
		Component text;
		if (key.startsWith("affection_")) {
			text = Component.translatableWithFallback("message.jugcraft.guest.notice.affection",
					"Town notice: %s and %s reached a friendship milestone (%s).", player.getName(), name, key.substring("affection_".length()));
		} else if (key.startsWith("anniversary_")) {
			String[] parts = key.split("_");
			text = Component.translatableWithFallback("message.jugcraft.guest.notice.anniversary",
					"Town notice: %s and %s celebrate %s days of marriage.", player.getName(), name, parts.length == 3 ? parts[2] : "?");
		} else if (key.startsWith("ceremony_")) {
			return List.of();   // announced by ceremony() when it completes
		} else {
			text = Component.translatableWithFallback("message.jugcraft.guest.notice.first",
					"Town notice: %s and %s are getting to know each other.", player.getName(), name);
		}
		return announce(server, subject, text);
	}

	/** A completed ceremony, announced only if the couple's player shares. */
	static List<ServerPlayer> ceremony(MinecraftServer server, UUID subject, String guest) {
		ServerPlayer player = server.getPlayerList().getPlayer(subject);
		if (player == null) {
			return List.of();
		}
		return announce(server, subject, Component.translatableWithFallback("message.jugcraft.guest.notice.ceremony",
				"Town notice: %s and %s held a ceremony at the Church of Notch.", player.getName(), GuestMilestones.name(guest)));
	}

	/**
	 * When the couple's player shares: invites players with notices on within {@code range} of the church to the ceremony.
	 * The players invited.
	 */
	static List<UUID> inviteNearby(MinecraftServer server, ServerPlayer couple, String guest, ServerLevel level, BlockPos anchor, double range) {
		List<UUID> invited = new ArrayList<>();
		if (!shares(server, couple.getUUID())) {
			return invited;
		}
		Component text = Component.translatableWithFallback("message.jugcraft.guest.notice.ceremony_invitation",
				"Town notice: %s and %s are holding a ceremony at the Church of Notch now. All are welcome; coming is up to you.",
				couple.getName(), GuestMilestones.name(guest));
		Vec3 at = Vec3.atBottomCenterOf(anchor);
		for (ServerPlayer other : List.copyOf(level.players())) {
			if (other != couple && other.position().distanceTo(at) <= range && deliver(server, other, Set.of(couple.getUUID()), text)) {
				invited.add(other.getUUID());
			}
		}
		return invited;
	}

	// ---------------------------------------------------------------- gossip and rivalries

	/** Once a second: rivalry notices for rivalries just begun, and gossip for receivers whose turn it is. */
	static void tick(MinecraftServer server, Map<String, GuestActivity.Activity> held) {
		long now = server.getTickCount();
		rivalries(server, held, now);
		for (ServerPlayer player : List.copyOf(server.getPlayerList().getPlayers())) {
			UUID me = player.getUUID();
			if (!receives(server, me)) {
				NEXT_GOSSIP.remove(me);
				continue;
			}
			Long next = NEXT_GOSSIP.get(me);
			if (next == null) {
				NEXT_GOSSIP.put(me, now + GOSSIP_TICKS);   // the first line comes a while after turning notices on
			} else if (now >= next) {
				NEXT_GOSSIP.put(me, now + GOSSIP_TICKS);
				Component line = gossip(server, server.overworld().getRandom());
				if (line != null) {
					deliver(server, player, Set.of(), line);
				}
			}
		}
	}

	/** A gossip line about a placed, available guest, or null when no guest is placed. Names no player. */
	static @Nullable Component gossip(MinecraftServer server, RandomSource random) {
		GuestLedger ledger = GuestLedger.get(server);
		List<GuestRoster.Guest> placed = new ArrayList<>();
		for (GuestRoster.Guest guest : GuestRoster.ALL) {
			if (guest.available() && ledger.canonical(guest.id()) != null && !ledger.missing(guest.id())) {
				placed.add(guest);
			}
		}
		if (placed.isEmpty()) {
			return null;
		}
		return gossipAbout(placed.get(random.nextInt(placed.size())), random.nextInt(TOWN.length + FICTION.length));
	}

	/** Gossip line {@code pick} about {@code guest}: a real-person guest only ever gets the town-life lines. */
	static Component gossipAbout(GuestRoster.Guest guest, int pick) {
		boolean real = guest.origin() == GuestRoster.Origin.REAL_PERSON;
		int index = Math.floorMod(pick, real ? TOWN.length : TOWN.length + FICTION.length);
		String[] line = index < TOWN.length ? TOWN[index] : FICTION[index - TOWN.length];
		return Component.translatableWithFallback("message.jugcraft.guest.gossip." + line[0], line[1], Component.literal(guest.name()));
	}

	/** Gossip keys a guest may get (tests: real-person guests only the town-life set). */
	static List<String> gossipKeys(GuestRoster.Guest guest) {
		List<String> keys = new ArrayList<>();
		for (String[] line : TOWN) {
			keys.add("message.jugcraft.guest.gossip." + line[0]);
		}
		if (guest.origin() != GuestRoster.Origin.REAL_PERSON) {
			for (String[] line : FICTION) {
				keys.add("message.jugcraft.guest.gossip." + line[0]);
			}
		}
		return keys;
	}

	/** A rivalry just begun (not seen a second ago) is noticed, naming only its guest, at most once per guest per {@link #RIVALRY_TICKS}. */
	private static void rivalries(MinecraftServer server, Map<String, GuestActivity.Activity> held, long now) {
		List<String> begun = new ArrayList<>();
		held.forEach((guest, activity) -> {
			if ("rivalry".equals(activity.kind()) && !activity.equals(RIVALRIES.get(guest))) {
				begun.add(guest);
			}
		});
		RIVALRIES.clear();
		held.forEach((guest, activity) -> {
			if ("rivalry".equals(activity.kind())) {
				RIVALRIES.put(guest, activity);
			}
		});
		for (String guest : begun) {
			Long last = LAST_RIVALRY.get(guest);
			if (last != null && now - last < RIVALRY_TICKS || GuestRoster.get(guest) == null) {
				continue;
			}
			LAST_RIVALRY.put(guest, now);
			Component text = Component.translatableWithFallback("message.jugcraft.guest.notice.rivalry",
					"Rivalry notice: %s has a friendly rivalry under way in town. Spectators welcome.", GuestMilestones.name(guest));
			for (ServerPlayer player : List.copyOf(server.getPlayerList().getPlayers())) {
				deliver(server, player, Set.of(), text);
			}
		}
	}

	static void clear() {
		NEXT_GOSSIP.clear();
		LAST_RIVALRY.clear();
		RIVALRIES.clear();
	}
}
