package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * Worker B's saved data, with the overworld as {@code jugcraft:town_guest_personality}: per player, the last day each
 * guest's chat granted affection, each guest's errand for its day, the last day a rivalry was rewarded, and the
 * player's high-water day (the latest day any of their records was written for). The day used never goes below the
 * high-water day, so a clock moved backwards neither repeats a day's reward nor offers an earlier day's errand again.
 * A record from an earlier day than the one in force counts as nothing done that day; when a record is written, the
 * player's records of earlier days are dropped (they can never be current again).
 *
 * <p>Nothing here is reset by closing or reopening a menu, reconnecting or restarting the server. In-memory message
 * cooldowns live in {@link GuestDialogue} and running rivalries in {@link GuestRivalries}; neither guards any reward.
 */
public final class GuestPersonalityState extends SavedData {
	/** A player's errand for a guest: OFFERED (nothing recorded yet), ACTIVE (taken up), DONE (turned in). */
	public enum ErrandState {
		OFFERED, ACTIVE, DONE
	}

	/** One player's errand with one guest: its day, state, and once taken up the errand's index, item id and count. */
	record Errand(long day, ErrandState state, int index, String item, int count) {
		static final Codec<ErrandState> STATE_CODEC = Codec.STRING.xmap(Errand::readState, state -> state.name().toLowerCase(Locale.ROOT));
		static final Codec<Errand> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.LONG.optionalFieldOf("day", 0L).forGetter(Errand::day),
				STATE_CODEC.optionalFieldOf("state", ErrandState.DONE).forGetter(Errand::state),
				Codec.INT.optionalFieldOf("index", 0).forGetter(Errand::index),
				Codec.STRING.optionalFieldOf("item", "").forGetter(Errand::item),
				Codec.INT.optionalFieldOf("count", 0).forGetter(Errand::count)
		).apply(i, Errand::new));

		Errand {
			state = state == null ? ErrandState.DONE : state;
			index = Math.max(0, index);
			item = item == null ? "" : item;
			count = Math.max(0, count);
		}

		/** A day with the errand offered and nothing taken up. */
		static Errand fresh(long day) {
			return new Errand(day, ErrandState.OFFERED, 0, "", 0);
		}

		/** An unknown saved state reads as done: never a second reward. */
		private static ErrandState readState(String saved) {
			return switch (saved) {
				case "offered" -> ErrandState.OFFERED;
				case "active" -> ErrandState.ACTIVE;
				default -> ErrandState.DONE;
			};
		}
	}

	static final Codec<GuestPersonalityState> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.unboundedMap(Codec.STRING, Codec.LONG))
					.optionalFieldOf("chats", Map.of()).forGetter(GuestPersonalityState::chats),
			Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.unboundedMap(Codec.STRING, Errand.CODEC))
					.optionalFieldOf("errands", Map.of()).forGetter(GuestPersonalityState::errands),
			Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.LONG).optionalFieldOf("rivalries", Map.of()).forGetter(GuestPersonalityState::rivalries),
			Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.LONG).optionalFieldOf("high_water", Map.of()).forGetter(GuestPersonalityState::highWater)
	).apply(i, GuestPersonalityState::new));
	static final SavedDataType<GuestPersonalityState> TYPE = new SavedDataType<>(Jugcraft.id("town_guest_personality"),
			GuestPersonalityState::new, CODEC, null);

	/** Per player and guest, the last day a chat granted affection. */
	final Map<UUID, Map<String, Long>> chats = new HashMap<>();
	/** Per player and guest, the errand of its day. */
	final Map<UUID, Map<String, Errand>> errands = new HashMap<>();
	/** Per player, the last day a finished rivalry was rewarded. */
	final Map<UUID, Long> rivalries = new HashMap<>();
	/** Per player, the latest day any of their records was written for: the day used never goes below it. */
	final Map<UUID, Long> highWater = new HashMap<>();

	GuestPersonalityState() {
	}

	private GuestPersonalityState(Map<UUID, Map<String, Long>> chats, Map<UUID, Map<String, Errand>> errands, Map<UUID, Long> rivalries,
			Map<UUID, Long> water) {
		chats.forEach((player, days) -> {
			this.chats.put(player, new LinkedHashMap<>(days));
			days.values().forEach(day -> highWater.merge(player, day, Long::max));
		});
		errands.forEach((player, guests) -> {
			this.errands.put(player, new LinkedHashMap<>(guests));
			guests.values().forEach(errand -> highWater.merge(player, errand.day(), Long::max));
		});
		rivalries.forEach((player, day) -> {
			this.rivalries.put(player, day);
			highWater.merge(player, day, Long::max);
		});
		water.forEach((player, day) -> highWater.merge(player, day, Long::max));
	}

	static GuestPersonalityState get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	Map<UUID, Map<String, Long>> chats() {
		return chats;
	}

	Map<UUID, Map<String, Errand>> errands() {
		return errands;
	}

	Map<UUID, Long> rivalries() {
		return rivalries;
	}

	Map<UUID, Long> highWater() {
		return highWater;
	}

	/** The day in force for the player on {@code day}: never earlier than their high-water day. */
	long effective(UUID player, long day) {
		Long seen = highWater.get(player);
		return seen == null ? day : Math.max(day, seen);
	}

	// ---------------------------------------------------------------- chats

	/** Whether a chat with {@code guest} has already granted affection on {@code day} (or later). */
	boolean chatGranted(UUID player, String guest, long day) {
		Map<String, Long> mine = chats.get(player);
		Long last = mine == null ? null : mine.get(guest);
		return last != null && last >= day;
	}

	/** Records that a chat with {@code guest} granted affection on {@code day}. */
	void chatted(UUID player, String guest, long day) {
		long current = raise(player, day);
		Map<String, Long> mine = chats.computeIfAbsent(player, id -> new LinkedHashMap<>());
		mine.values().removeIf(old -> old < current);
		mine.put(guest, current);
		setDirty();
	}

	// ---------------------------------------------------------------- errands

	/** The errand record for {@code day} (a day in force): the saved one if it is that day or later, else a fresh day. */
	Errand errand(UUID player, String guest, long day) {
		Map<String, Errand> mine = errands.get(player);
		Errand saved = mine == null ? null : mine.get(guest);
		return saved != null && saved.day() >= day ? saved : Errand.fresh(day);
	}

	/** Saves {@code errand} for its day, raising the high-water day and dropping the player's older errand records. */
	void putErrand(UUID player, String guest, Errand errand) {
		long current = raise(player, errand.day());
		Map<String, Errand> mine = errands.computeIfAbsent(player, id -> new LinkedHashMap<>());
		mine.values().removeIf(old -> old.day() < current);
		mine.put(guest, errand);
		setDirty();
	}

	// ---------------------------------------------------------------- rivalries

	/** Whether a rivalry has already been rewarded for the player on {@code day} (or later). */
	boolean rivalryRewarded(UUID player, long day) {
		Long last = rivalries.get(player);
		return last != null && last >= day;
	}

	/** Records that a rivalry was rewarded for the player on {@code day}. */
	void rewardedRivalry(UUID player, long day) {
		rivalries.put(player, raise(player, day));
		setDirty();
	}

	/** Raises the player's high-water day to {@code day}; returns the day in force. */
	private long raise(UUID player, long day) {
		long current = effective(player, day);
		highWater.put(player, current);
		return current;
	}

	/** The saved errand for the player and guest, or null (test seam: to check nothing moved on). */
	@Nullable Errand saved(UUID player, String guest) {
		Map<String, Errand> mine = errands.get(player);
		return mine == null ? null : mine.get(guest);
	}
}
