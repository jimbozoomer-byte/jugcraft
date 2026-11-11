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
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * Outings' and home visits' saved data (worker C), with the overworld as {@code jugcraft:town_guest_outings}: per player
 * only, their home visit spot and, per guest, the day and the kinds of outing done that day (the once-a-day rule).
 * Nothing per guest lives here: the one saved state per guest is {@link GuestActivity}. A player's high-water day (the
 * latest day any of their records was written for) is kept too, so a game clock moved backwards never makes an outing
 * new again.
 *
 * <p>A player's entry that cannot be read is kept exactly as written ({@link Saved#keeping}) and never written over:
 * that player counts every kind as done (no outing, no reward) and cannot set a spot until an operator repairs the file.
 */
final class GuestOutingState extends SavedData {
	/** A home visit spot: the block the player stood on (their feet), in its level. */
	record Spot(ResourceKey<Level> dimension, BlockPos pos) {
		static final Codec<Spot> CODEC = RecordCodecBuilder.create(i -> i.group(
				ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Spot::dimension),
				BlockPos.CODEC.fieldOf("pos").forGetter(Spot::pos)
		).apply(i, Spot::new));

		Spot {
			pos = pos.immutable();
		}
	}

	/** The kinds of outing done with one guest on {@code day}. */
	record Done(long day, List<String> kinds) {
		static final Codec<Done> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.LONG.fieldOf("day").forGetter(Done::day),
				Codec.STRING.listOf().optionalFieldOf("kinds", List.of()).forGetter(Done::kinds)
		).apply(i, Done::new));

		Done {
			kinds = List.copyOf(kinds);
		}
	}

	/** One player's record. */
	static final class PlayerRecord {
		@Nullable Spot spot;
		/** The latest day any outing was recorded for: the day used never goes below it. */
		long high;
		/** Guest id to what was done with it on its day. */
		final Map<String, Done> done = new TreeMap<>();

		PlayerRecord() {
		}

		PlayerRecord(Optional<Spot> spot, long high, Map<String, Done> done) {
			this.spot = spot.orElse(null);
			this.high = high;
			this.done.putAll(done);
			for (Done kept : done.values()) {
				this.high = Math.max(this.high, kept.day());
			}
		}

		boolean empty() {
			return spot == null && done.isEmpty() && high == 0L;
		}
	}

	private static final Codec<PlayerRecord> PLAYER = RecordCodecBuilder.create(i -> i.group(
			Spot.CODEC.optionalFieldOf("visit_spot").forGetter(r -> Optional.ofNullable(r.spot)),
			Codec.LONG.optionalFieldOf("high_day", 0L).forGetter(r -> r.high),
			Codec.unboundedMap(Codec.STRING, Done.CODEC).optionalFieldOf("done", Map.of()).forGetter(r -> Map.copyOf(r.done))
	).apply(i, PlayerRecord::new));
	private static final Codec<GuestOutingState> BODY = RecordCodecBuilder.create(i -> i.group(
			Saved.keeping("town_guest_outings", UUIDUtil.STRING_CODEC, PLAYER)
					.optionalFieldOf("players", Saved.Kept.<UUID, PlayerRecord>of(Map.of())).forGetter(GuestOutingState::saved)
	).apply(i, GuestOutingState::new));
	/** The whole state, versioned ({@link Saved#versioned}). */
	static final Codec<GuestOutingState> CODEC = Saved.versioned("town_guest_outings", BODY);
	static final SavedDataType<GuestOutingState> TYPE = new SavedDataType<>(Jugcraft.id("town_guest_outings"), GuestOutingState::new, CODEC, null);

	private final Map<UUID, PlayerRecord> players = new HashMap<>();
	/** Entries kept as written because they could not be read (by the player's UUID as text). */
	private final Map<String, Dynamic<?>> unread = new LinkedHashMap<>();

	GuestOutingState() {
	}

	private GuestOutingState(Saved.Kept<UUID, PlayerRecord> saved) {
		players.putAll(saved.read());
		unread.putAll(saved.unread());
		unread.keySet().forEach(key -> Jugcraft.LOGGER.error("[guests] Outing record {} cannot be read; it is kept as written, and that "
				+ "player has no outings or visit spot until an operator repairs the town_guest_outings file", key));
	}

	static GuestOutingState get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	private Saved.Kept<UUID, PlayerRecord> saved() {
		Map<UUID, PlayerRecord> kept = new LinkedHashMap<>();
		players.forEach((player, record) -> {
			if (!record.empty()) {
				kept.put(player, record);
			}
		});
		return new Saved.Kept<>(kept, unread);
	}

	/** Whether the player's saved entry could not be read (kept as written; nothing is written over it). */
	boolean unreadable(UUID player) {
		return unread.containsKey(player.toString());
	}

	// ---------------------------------------------------------------- the visit spot

	/** The player's home visit spot, or null when none is set (or their entry is unreadable). */
	@Nullable Spot spot(UUID player) {
		PlayerRecord record = players.get(player);
		return record == null || unreadable(player) ? null : record.spot;
	}

	/** Sets (or with null forgets) the player's spot; false, changing nothing, when their entry is unreadable. */
	boolean spot(UUID player, @Nullable Spot spot) {
		if (unreadable(player)) {
			return false;
		}
		PlayerRecord record = players.computeIfAbsent(player, id -> new PlayerRecord());
		record.spot = spot;
		if (record.empty()) {
			players.remove(player);
		}
		setDirty();
		return true;
	}

	// ---------------------------------------------------------------- once a day

	/** The player's high-water day (0 when none). */
	long high(UUID player) {
		PlayerRecord record = players.get(player);
		return record == null ? 0L : record.high;
	}

	/** Whether {@code kind} was already done with {@code guest} on {@code day}; always true for an unreadable entry. */
	boolean done(UUID player, String guest, String kind, long day) {
		if (unreadable(player)) {
			return true;
		}
		PlayerRecord record = players.get(player);
		Done done = record == null ? null : record.done.get(guest);
		return done != null && done.day() >= day && done.kinds().contains(kind);
	}

	/**
	 * Records {@code kind} done with {@code guest} on {@code day} (never earlier than the player's high-water day), and
	 * drops their records of earlier days, which can never count again. Nothing for an unreadable entry.
	 */
	void markDone(UUID player, String guest, String kind, long day) {
		if (unreadable(player)) {
			return;
		}
		PlayerRecord record = players.computeIfAbsent(player, id -> new PlayerRecord());
		long current = Math.max(day, record.high);
		record.high = current;
		record.done.values().removeIf(old -> old.day() < current);
		Done today = record.done.get(guest);
		List<String> kinds = new ArrayList<>(today == null ? List.of() : today.kinds());
		if (!kinds.contains(kind)) {
			kinds.add(kind);
		}
		record.done.put(guest, new Done(current, kinds));
		setDirty();
	}

	/** Forgets the player's records (game tests: a codec round trip puts them back). */
	void forget(UUID player) {
		players.remove(player);
		setDirty();
	}

	/** Puts the player's record from {@code other} (a state read back with {@link #CODEC}) into this one. */
	void restore(GuestOutingState other, UUID player) {
		PlayerRecord record = other.players.get(player);
		if (record == null) {
			players.remove(player);
		} else {
			players.put(player, record);
		}
		setDirty();
	}
}
