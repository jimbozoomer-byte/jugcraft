package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.season.SeasonCalendar;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.guests.GuestDialogueLines.Cue;
import io.github.jimbozoomer.jugcraft.town.guests.GuestDialogueLines.Line;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * What guests say (worker B): a reaction to each gift, a situational greeting when talked to, and the "chat" option.
 * Lines come from {@link GuestDialogueLines}; every line goes to the one player concerned, as a system message, and
 * never to anyone else.
 *
 * <ul>
 * <li><b>Gift reactions</b> ({@link GuestEvents#GIFT_GIVEN}, after the gift is committed): one line, from the guest's
 * favourite set for a favourite, else (rarely, at most once per player and guest while the server runs) its comic
 * one-off, else its ordinary set. Messages only: a reaction never changes the gift, the inventory or affection. At most
 * one reaction per player every {@link #REACTION_GAP_TICKS}.</li>
 * <li><b>Greetings</b> ({@link GuestEvents#TALKED}): one banter line chosen for the moment, at most once per player and
 * guest every {@link #GREETING_COOLDOWN_TICKS}. No affection.</li>
 * <li><b>Chat</b> (the "chat" option): one banter line, at most once per player every {@link #CHAT_COOLDOWN_TICKS};
 * the first chat with a guest on a day grants {@link #CHAT_AFFECTION} through {@link GuestRewards#grant} (so within
 * the shared daily budget), at most once per player and guest per day (saved in {@link GuestPersonalityState}). A chat
 * when the budget is used grants nothing and does not use up the day's chat grant.</li>
 * </ul>
 *
 * <p><b>The moment</b> ({@link #context}): the time of day (the guest's own period), the weather (overworld only), the
 * season and seasonal events ({@link JugcraftSeasons}), whether the player gave this guest a gift today, and whether
 * the ledger's {@code spouseOf} shows the two married to each other. A romantic line (spouse banter) is a candidate only
 * when the guest's roster entry {@code mayMarry()} and they are married ({@link #candidates}).
 *
 * <p>Cooldowns are kept in memory by server tick ({@link MinecraftServer#getTickCount}), dropped when a player
 * disconnects or the server stops. They only limit messages; no reward depends on them.
 */
public final class GuestDialogue {
	/** Ticks between two gift reactions for one player: one second. */
	public static final int REACTION_GAP_TICKS = 20;
	/** Ticks between two greetings from one guest to one player: one minute. */
	public static final int GREETING_COOLDOWN_TICKS = 1200;
	/** Ticks between two chats by one player, with any guest: five seconds. */
	public static final int CHAT_COOLDOWN_TICKS = 100;
	/** Affection the first chat with a guest on a day grants (through the shared budget). */
	public static final int CHAT_AFFECTION = 2;
	/** A non-favourite gift's chance of the guest's comic one-off reaction: one in this many. */
	public static final int RARE_ONE_IN = 20;
	/** Chance that banter picks a situational line when one fits the moment. */
	public static final float SITUATIONAL_CHANCE = 0.6F;

	/** Per player, the server tick of their last gift reaction. */
	private static final Map<UUID, Long> REACTED = new HashMap<>();
	/** Per player and guest, the server tick of the last greeting. */
	private static final Map<UUID, Map<String, Long>> GREETED = new HashMap<>();
	/** Per player, the server tick of their last chat. */
	private static final Map<UUID, Long> CHATTED = new HashMap<>();
	/** Per player, the guests whose comic one-off they have seen while the server runs. */
	private static final Map<UUID, Set<String>> RARE_SEEN = new HashMap<>();
	/** Per player and guest, the key of the last line said (not repeated twice running). */
	private static final Map<UUID, Map<String, String>> LAST = new HashMap<>();
	/** Test seam: per player, the key of the last line sent to them by anything here. */
	static final Map<UUID, String> LAST_SAID = new HashMap<>();
	/** Test seam: per player, a reaction slot ("favourite", "ordinary", "rare") forced for their next gift reaction. */
	static final Map<UUID, String> FORCED_SLOT = new HashMap<>();

	private GuestDialogue() {
	}

	// ---------------------------------------------------------------- gift reactions

	/**
	 * {@link GuestEvents#GIFT_GIVEN}: one personality-flavoured line to the giver, rate-limited per player. Reads the
	 * event only; it never touches the gift, the inventory, the ledger or the favors.
	 */
	static void reactToGift(ServerPlayer player, Townsfolk npc, String guest, ItemStack gift, boolean favourite, int gained) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available()) {
			return;
		}
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		long now = server.getTickCount();
		Long last = REACTED.get(me);
		if (last != null && now - last < REACTION_GAP_TICKS && now >= last) {
			return;
		}
		REACTED.put(me, now);
		GuestDialogueLines.Lines lines = GuestDialogueLines.lines(guest);
		RandomSource random = player.getRandom();
		String forced = FORCED_SLOT.remove(me);
		List<Line> pool;
		if ("rare".equals(forced) || forced == null && !favourite && random.nextInt(RARE_ONE_IN) == 0
				&& RARE_SEEN.computeIfAbsent(me, id -> new HashSet<>()).add(guest)) {
			pool = lines.rare();
		} else if ("favourite".equals(forced) || forced == null && favourite) {
			pool = lines.favourite();
		} else {
			pool = lines.ordinary();
		}
		Line line = pick(me, guest, candidates(entry, pool, Set.of()), random);
		if (line != null) {
			tell(player, npc, guest, line);
		}
	}

	// ---------------------------------------------------------------- greetings and chat

	/** {@link GuestEvents#TALKED}: a line for the moment, at most once per player and guest per greeting cooldown. */
	static void greet(ServerPlayer player, Townsfolk npc, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available() || !GuestPersonality.here(player, npc, guest)) {
			return;
		}
		long now = player.level().getServer().getTickCount();
		Map<String, Long> mine = GREETED.computeIfAbsent(player.getUUID(), id -> new HashMap<>());
		Long last = mine.get(guest);
		if (last != null && now - last < GREETING_COOLDOWN_TICKS && now >= last) {
			return;
		}
		mine.put(guest, now);
		Line line = banter(player, npc, guest, entry);
		if (line != null) {
			tell(player, npc, guest, line);
		}
	}

	/**
	 * The "chat" option: a line for the moment and, the first time on a day with this guest, {@link #CHAT_AFFECTION}
	 * through {@link GuestRewards#grant}. Refused (nothing said or granted) unless the player may act with the guest
	 * ({@link GuestPersonality#check}) and their chat cooldown has run out.
	 */
	public static GuestResult chat(ServerPlayer player, Townsfolk npc, String guest) {
		GuestResult away = GuestPersonality.check(player, npc, guest);
		if (away != null) {
			return away;
		}
		GuestRoster.Guest entry = GuestRoster.get(guest);
		Component name = GuestPersonality.name(npc, guest);
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		long now = server.getTickCount();
		Long last = CHATTED.get(me);
		if (last != null && now - last < CHAT_COOLDOWN_TICKS && now >= last) {
			return GuestResult.refused("message.jugcraft.guest.personality.chat.cooldown", "Give %s a moment before chatting again.", name);
		}
		CHATTED.put(me, now);
		Line line = banter(player, npc, guest, entry);
		if (line != null) {
			tell(player, npc, guest, line);
		}
		int added = 0;
		long day = GuestPersonality.day(player);
		GuestPersonalityState state = GuestPersonalityState.get(server);
		if (!state.chatGranted(me, guest, day) && GuestRewards.left(player, guest) > 0) {
			added = GuestRewards.grant(player, guest, CHAT_AFFECTION);
			if (added > 0) {
				state.chatted(me, guest, day);
			}
		}
		return added > 0
				? GuestResult.done("message.jugcraft.guest.personality.chat.affection", "You chatted with %s: +%s affection.", name, added)
				: GuestResult.done("message.jugcraft.guest.personality.chat.done", "You chatted with %s.", name);
	}

	/** A banter line for the moment, or null if the guest has none that fits. */
	private static @Nullable Line banter(ServerPlayer player, Townsfolk npc, String guest, GuestRoster.Guest entry) {
		Set<Cue> cues = context(player, npc, guest);
		return pick(player.getUUID(), guest, candidates(entry, GuestDialogueLines.lines(guest).banter(), cues), player.getRandom());
	}

	// ---------------------------------------------------------------- choosing a line

	/**
	 * The moment, as cues: the guest's time of day, the weather (overworld only), the season and seasonal events, a gift
	 * from this player to this guest today, and a marriage between them as the ledger records it ({@code spouseOf} both
	 * ways). Read only.
	 */
	static Set<Cue> context(ServerPlayer player, Townsfolk npc, String guest) {
		EnumSet<Cue> cues = EnumSet.noneOf(Cue.class);
		cues.add(switch (npc.period()) {
			case MORNING -> Cue.MORNING;
			case WORK -> Cue.DAY;
			case EVENING -> Cue.EVENING;
			case NIGHT -> Cue.NIGHT;
		});
		Level level = npc.level();
		if (Level.OVERWORLD.equals(level.dimension())) {
			if (level.isThundering()) {
				cues.add(Cue.THUNDER);
				cues.add(Cue.RAIN);
			} else if (level.isRaining()) {
				cues.add(Cue.RAIN);
			} else {
				cues.add(Cue.CLEAR);
			}
		}
		switch (SeasonCalendar.seasonName(JugcraftSeasons.today())) {
			case "spring" -> cues.add(Cue.SPRING);
			case "summer" -> cues.add(Cue.SUMMER);
			case "autumn" -> cues.add(Cue.AUTUMN);
			case "winter" -> cues.add(Cue.WINTER);
			default -> {
			}
		}
		if (JugcraftSeasons.isActive(SeasonCalendar.Event.HARVEST_FEAST)) {
			cues.add(Cue.HARVEST);
		}
		if (JugcraftSeasons.isActive(SeasonCalendar.Event.DECEMBER)) {
			cues.add(Cue.DECEMBER);
		}
		if (GuestFavors.giftsLeft(player, guest) < GuestFavors.GIFTS_PER_DAY) {
			cues.add(Cue.GIFTED);
		}
		if (married(player, guest)) {
			cues.add(Cue.MARRIED);
		}
		return cues;
	}

	/** Whether the ledger records the player and the guest married to each other. */
	static boolean married(ServerPlayer player, String guest) {
		GuestLedger ledger = GuestLedger.get(player.level().getServer());
		return guest.equals(ledger.spouseOf(player.getUUID())) && player.getUUID().equals(ledger.spouseOf(guest));
	}

	/**
	 * The lines of {@code pool} that fit {@code cues}: every cue a line needs is present, and a romantic line only for a
	 * guest whose roster entry may marry ({@code mayMarry()}), when {@code cues} holds {@link Cue#MARRIED}, and never for
	 * a real person. Pure.
	 */
	static List<Line> candidates(GuestRoster.Guest entry, List<Line> pool, Set<Cue> cues) {
		List<Line> fit = new ArrayList<>();
		for (Line line : pool) {
			if (!cues.containsAll(line.when())) {
				continue;
			}
			if (line.romantic() && (!entry.mayMarry() || !cues.contains(Cue.MARRIED) || entry.origin() != GuestRoster.Origin.FICTIONAL)) {
				continue;
			}
			fit.add(line);
		}
		return fit;
	}

	/**
	 * One of {@code fit}: a situational one with chance {@link #SITUATIONAL_CHANCE} when any fits, and not the line this
	 * guest said to this player last time when there is another to say; null when nothing fits.
	 */
	private static @Nullable Line pick(UUID player, String guest, List<Line> fit, RandomSource random) {
		if (fit.isEmpty()) {
			return null;
		}
		String last = LAST.computeIfAbsent(player, id -> new HashMap<>()).get(guest);
		List<Line> fresh = new ArrayList<>(fit);
		if (fresh.size() > 1) {
			fresh.removeIf(line -> line.key().equals(last));
		}
		List<Line> situational = new ArrayList<>();
		List<Line> everyday = new ArrayList<>();
		for (Line line : fresh) {
			(line.situational() ? situational : everyday).add(line);
		}
		List<Line> from = !situational.isEmpty() && (everyday.isEmpty() || random.nextFloat() < SITUATIONAL_CHANCE) ? situational : everyday;
		return from.get(random.nextInt(from.size()));
	}

	/** Sends "guest: line" to the player alone, and remembers it. */
	private static void tell(ServerPlayer player, Townsfolk npc, String guest, Line line) {
		UUID me = player.getUUID();
		LAST.computeIfAbsent(me, id -> new HashMap<>()).put(guest, line.key());
		LAST_SAID.put(me, line.key());
		player.sendSystemMessage(GuestPersonality.says(GuestPersonality.name(npc, guest), line));
	}

	// ---------------------------------------------------------------- lifetime

	/** Drops a player's cooldowns and memory (they disconnected). */
	static void forget(UUID player) {
		REACTED.remove(player);
		GREETED.remove(player);
		CHATTED.remove(player);
		RARE_SEEN.remove(player);
		LAST.remove(player);
		LAST_SAID.remove(player);
		FORCED_SLOT.remove(player);
	}

	/** Drops everything (the server stopped). */
	static void clear() {
		REACTED.clear();
		GREETED.clear();
		CHATTED.clear();
		RARE_SEEN.clear();
		LAST.clear();
		LAST_SAID.clear();
		FORCED_SLOT.clear();
	}
}
