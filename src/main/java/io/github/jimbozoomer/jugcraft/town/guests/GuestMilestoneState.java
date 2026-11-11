package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.Saved;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * Worker D's saved data, kept with the overworld as {@code jugcraft:town_guest_milestones}: per player, the milestones
 * reached with each guest (each recorded once, for good), the rewards not yet handed over, the day of the last shared
 * meal with each guest, the marriage as this feature knows it (a serial number per player, its start day, whether its
 * ceremony was held), and the two notice settings (both off unless the player turns them on).
 *
 * <p>It never decides who is married: {@link GuestLedger} does. The marriage record here only numbers each marriage the
 * ledger makes ({@link GuestEvents#MARRIED}) so that a ceremony and anniversaries belong to one marriage; it is cleared
 * on {@link GuestEvents#DIVORCED} and reconciled with the ledger before it is used ({@link GuestMilestones#marriage}).
 *
 * <p>Versioned ({@link Saved#versioned}); each player's entry is read on its own ({@link Saved#keeping}). An entry that
 * cannot be read is kept exactly as written and written back unchanged, and that player is {@link #frozen}: no
 * milestone fires, no meal is shared and no ceremony is held for them until an operator repairs the file, so a damaged
 * save never hands a reward out twice.
 */
public final class GuestMilestoneState extends SavedData {
	/** The marriage this feature numbers: the guest, the player's marriage serial, its start game day, its ceremony held. */
	static final class Marriage {
		final String guest;
		final long serial;
		final long since;
		boolean ceremony;

		Marriage(String guest, long serial, long since, boolean ceremony) {
			this.guest = guest;
			this.serial = serial;
			this.since = since;
			this.ceremony = ceremony;
		}
	}

	/** A reward reached and not yet handed over (the player was away, or not yet ticked). */
	record Waiting(String guest, String key) {
	}

	/** One player's record with one guest. */
	static final class GuestRecord {
		final Set<String> reached = new TreeSet<>();
		/** The game day of the last meal shared; none yet when absent. */
		@Nullable Long mealDay;

		GuestRecord() {
		}

		GuestRecord(List<String> reached, Optional<Long> mealDay) {
			this.reached.addAll(reached);
			this.mealDay = mealDay.orElse(null);
		}
	}

	/** One player's record. */
	static final class PlayerRecord {
		boolean notices;
		boolean share;
		/** How many marriages this feature has numbered for the player (the latest one's serial). */
		long serial;
		@Nullable Marriage marriage;
		final Map<String, GuestRecord> guests = new TreeMap<>();
		final List<Waiting> waiting = new ArrayList<>();

		PlayerRecord() {
		}

		PlayerRecord(boolean notices, boolean share, long serial, Optional<Marriage> marriage, Map<String, GuestRecord> guests,
				List<Waiting> waiting) {
			this.notices = notices;
			this.share = share;
			this.serial = Math.max(0L, serial);
			this.marriage = marriage.orElse(null);
			if (this.marriage != null && this.marriage.serial > this.serial) {
				this.serial = this.marriage.serial;
			}
			this.guests.putAll(guests);
			this.waiting.addAll(waiting);
		}
	}

	private static final Codec<Marriage> MARRIAGE = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.fieldOf("guest").forGetter(m -> m.guest),
			Codec.LONG.fieldOf("serial").forGetter(m -> m.serial),
			Codec.LONG.fieldOf("since").forGetter(m -> m.since),
			Codec.BOOL.optionalFieldOf("ceremony", false).forGetter(m -> m.ceremony)
	).apply(i, Marriage::new));
	private static final Codec<Waiting> WAITING = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.fieldOf("guest").forGetter(Waiting::guest),
			Codec.STRING.fieldOf("key").forGetter(Waiting::key)
	).apply(i, Waiting::new));
	private static final Codec<GuestRecord> GUEST = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.listOf().optionalFieldOf("reached", List.of()).forGetter(r -> List.copyOf(r.reached)),
			Codec.LONG.optionalFieldOf("meal_day").forGetter(r -> Optional.ofNullable(r.mealDay))
	).apply(i, GuestRecord::new));
	private static final Codec<PlayerRecord> PLAYER = RecordCodecBuilder.create(i -> i.group(
			Codec.BOOL.optionalFieldOf("notices", false).forGetter(r -> r.notices),
			Codec.BOOL.optionalFieldOf("share", false).forGetter(r -> r.share),
			Codec.LONG.optionalFieldOf("serial", 0L).forGetter(r -> r.serial),
			MARRIAGE.optionalFieldOf("marriage").forGetter(r -> Optional.ofNullable(r.marriage)),
			Codec.unboundedMap(Codec.STRING, GUEST).optionalFieldOf("guests", Map.of()).forGetter(r -> Map.copyOf(r.guests)),
			WAITING.listOf().optionalFieldOf("waiting", List.of()).forGetter(r -> List.copyOf(r.waiting))
	).apply(i, PlayerRecord::new));
	private static final Codec<GuestMilestoneState> BODY = RecordCodecBuilder.create(i -> i.group(
			Saved.keeping("town_guest_milestones", UUIDUtil.STRING_CODEC, PLAYER)
					.optionalFieldOf("players", Saved.Kept.<UUID, PlayerRecord>of(Map.of())).forGetter(GuestMilestoneState::saved)
	).apply(i, GuestMilestoneState::new));
	/** The whole state, versioned. */
	static final Codec<GuestMilestoneState> CODEC = Saved.versioned("town_guest_milestones", BODY);
	static final SavedDataType<GuestMilestoneState> TYPE = new SavedDataType<>(Jugcraft.id("town_guest_milestones"),
			GuestMilestoneState::new, CODEC, null);

	private final Map<UUID, PlayerRecord> players = new HashMap<>();
	/** Entries kept as written because they could not be read. */
	private final Map<String, Dynamic<?>> unread = new LinkedHashMap<>();
	/** Players whose own entry is unreadable (or every player, when an unreadable key names nobody). */
	private final Set<UUID> frozen = new TreeSet<>();
	private boolean frozenAll;

	GuestMilestoneState() {
	}

	private GuestMilestoneState(Saved.Kept<UUID, PlayerRecord> saved) {
		players.putAll(saved.read());
		unread.putAll(saved.unread());
		unread.keySet().forEach(key -> {
			try {
				frozen.add(UUID.fromString(key));
				Jugcraft.LOGGER.error("[guests] Player {}'s milestone record cannot be read; it is kept as written, and their "
						+ "milestones, meals and ceremonies wait until an operator repairs the town_guest_milestones file", key);
			} catch (IllegalArgumentException notUuid) {
				frozenAll = true;
				Jugcraft.LOGGER.error("[guests] A milestone record ({}) cannot be read and names no player: every milestone waits", key);
			}
		});
	}

	public static GuestMilestoneState get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	private Saved.Kept<UUID, PlayerRecord> saved() {
		return new Saved.Kept<>(new LinkedHashMap<>(players), unread);
	}

	/** Whether the player's saved entry could not be read: nothing is recorded for them, nothing handed out. */
	boolean frozen(UUID player) {
		return frozenAll || frozen.contains(player);
	}

	/** The player's record, or null (never made by a read). */
	@Nullable PlayerRecord peek(UUID player) {
		return players.get(player);
	}

	/** The player's record, made on first use; null while {@link #frozen}. */
	private @Nullable PlayerRecord record(UUID player) {
		return frozen(player) ? null : players.computeIfAbsent(player, id -> new PlayerRecord());
	}

	// ---------------------------------------------------------------- milestones

	boolean reached(UUID player, String guest, String key) {
		PlayerRecord record = players.get(player);
		GuestRecord with = record == null ? null : record.guests.get(guest);
		return with != null && with.reached.contains(key);
	}

	/**
	 * Records the milestone and queues its reward; false (nothing changed) when it was reached before or the player is
	 * frozen. The only way a milestone is recorded, so each fires once.
	 */
	boolean reach(UUID player, String guest, String key) {
		PlayerRecord record = record(player);
		if (record == null) {
			return false;
		}
		GuestRecord with = record.guests.computeIfAbsent(guest, id -> new GuestRecord());
		if (!with.reached.add(key)) {
			return false;
		}
		record.waiting.add(new Waiting(guest, key));
		setDirty();
		return true;
	}

	/** The rewards waiting for the player, oldest first (a copy). */
	List<Waiting> waiting(UUID player) {
		PlayerRecord record = frozen(player) ? null : players.get(player);
		return record == null ? List.of() : List.copyOf(record.waiting);
	}

	/** Takes {@code waiting} off the player's list (before it is handed over); false if it was not there. */
	boolean handOver(UUID player, Waiting waiting) {
		PlayerRecord record = frozen(player) ? null : players.get(player);
		if (record == null || !record.waiting.remove(waiting)) {
			return false;
		}
		setDirty();
		return true;
	}

	// ---------------------------------------------------------------- meals

	/** The game day of the last meal the player shared with the guest, or null. */
	@Nullable Long mealDay(UUID player, String guest) {
		PlayerRecord record = players.get(player);
		GuestRecord with = record == null ? null : record.guests.get(guest);
		return with == null ? null : with.mealDay;
	}

	void meal(UUID player, String guest, long day) {
		PlayerRecord record = record(player);
		if (record != null) {
			record.guests.computeIfAbsent(guest, id -> new GuestRecord()).mealDay = day;
			setDirty();
		}
	}

	// ---------------------------------------------------------------- marriage

	@Nullable Marriage marriage(UUID player) {
		PlayerRecord record = players.get(player);
		return record == null ? null : record.marriage;
	}

	/** Numbers a new marriage of the player to {@code guest} starting on {@code day}; null while frozen. */
	@Nullable Marriage startMarriage(UUID player, String guest, long day) {
		PlayerRecord record = record(player);
		if (record == null) {
			return null;
		}
		record.serial++;
		record.marriage = new Marriage(guest, record.serial, day, false);
		setDirty();
		return record.marriage;
	}

	/** Forgets the player's marriage record (a divorce, or no marriage in the ledger any more). */
	void endMarriage(UUID player) {
		PlayerRecord record = players.get(player);
		if (record != null && record.marriage != null && !frozen(player)) {
			record.marriage = null;
			setDirty();
		}
	}

	/** Records the ceremony of marriage {@code serial} as held; false unless that marriage is the player's current one. */
	boolean ceremonyHeld(UUID player, long serial) {
		PlayerRecord record = frozen(player) ? null : players.get(player);
		if (record == null || record.marriage == null || record.marriage.serial != serial || record.marriage.ceremony) {
			return false;
		}
		record.marriage.ceremony = true;
		setDirty();
		return true;
	}

	// ---------------------------------------------------------------- notices

	boolean notices(UUID player) {
		PlayerRecord record = frozen(player) ? null : players.get(player);
		return record != null && record.notices;
	}

	boolean share(UUID player) {
		PlayerRecord record = frozen(player) ? null : players.get(player);
		return record != null && record.share;
	}

	/** Sets the player's notice settings; false while frozen. */
	boolean notices(UUID player, boolean receive, boolean share) {
		PlayerRecord record = record(player);
		if (record == null) {
			return false;
		}
		record.notices = receive;
		record.share = share;
		setDirty();
		return true;
	}

	// ---------------------------------------------------------------- tests

	/** Drops the player's record (tests: as if never kept). */
	void forget(UUID player) {
		if (players.remove(player) != null) {
			setDirty();
		}
	}

	/** Puts the player's record from {@code other} (a loaded copy) in place of theirs (tests). */
	void restore(GuestMilestoneState other, UUID player) {
		PlayerRecord record = other.players.get(player);
		if (record == null) {
			players.remove(player);
		} else {
			players.put(player, record);
		}
		setDirty();
	}
}
