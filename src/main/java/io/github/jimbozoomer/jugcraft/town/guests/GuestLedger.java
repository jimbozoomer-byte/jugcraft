package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.Saved;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.LongSupplier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * The one persistent, canonical record of the town guests, saved with the overworld as {@code jugcraft:town_guests}:
 * which entity is each guest, affection per player and guest, spouses, and remarriage cooldowns.
 *
 * <p>Rules every implementation keeps:
 * <ul>
 * <li>One canonical entity per guest id, server-wide, whether loaded or not. The first entity to {@link #verify} an
 * unbound id binds it; any other entity (a copy from an old save, a second placement) is {@link Claim#DUPLICATE} and
 * leaves. Its removal never frees the identity: {@link #lost} keeps the reservation, spouse and affection, and only
 * {@link #bind} (an operator's placement or repair) puts a new entity in its place.</li>
 * <li>One spouse per player, one player per guest; {@link #marry} checks and commits both sides at once.</li>
 * <li>{@link #divorce} frees both and gives each a cooldown of {@link GuestBalance#REMARRIAGE_COOLDOWN_MILLIS} of real
 * time, saved: it keeps running while the server is down and never shortens when the wall clock moves backwards.</li>
 * <li>Affection stays within 0 to {@link GuestBalance#MAX_AFFECTION}. Everything is saved on change.</li>
 * </ul>
 *
 * <p><b>Replaced entities.</b> An entity {@link #bind} replaces is kept as a tombstone and never accepted for any guest
 * again. At most {@link #MAX_REPLACED} are kept per guest. Only a tombstone whose entity has since been seen destroyed
 * (killed or discarded, reported through {@link #lost}) may be dropped to make room; one that might still be lying in an
 * unloaded chunk is never dropped, so a bind that would need to drop one is refused instead. (Once a guest is bound it
 * stays bound, so every other entity verifying as it is a duplicate anyway; the tombstones stop a replaced entity from
 * being bound again.)
 *
 * <p><b>The cooldown clock.</b> Cooldowns are kept as real milliseconds remaining, per player and per guest, with the
 * wall clock's high-water mark: the latest reading ever seen, saved. About once a second (the server tick) and on every
 * read the ledger advances: only time beyond the high-water mark is taken off the running cooldowns, and the mark moves
 * up to the reading. A clock moved backwards credits nothing, and nothing again while it climbs back to the mark, so no
 * stretch of time is ever credited twice (a clock set back by an hour holds every cooldown still for that hour). After a
 * load the first advance credits the time from the saved mark, so downtime counts. A wall clock moved forwards cannot
 * be told apart from downtime and is credited too: that is the price of counting real time while the server is down.
 *
 * <p><b>Damaged saves.</b> A guest's or player's saved entry that cannot be read is kept exactly as written and written
 * back unchanged ({@link Saved#keeping}); nothing ever writes a fresh entry over it. Its parties are refused marriage
 * until an operator repairs the file, and this is logged when the ledger loads and when a marriage is refused for it:
 * <ul>
 * <li>a guest whose own entry is unreadable is frozen: nothing binds it, verifies as it or marries it
 * ({@link Refusal#NO_MARRIAGE}), so damage never quietly frees an identity or a spouse;</li>
 * <li>the player named as that guest's spouse, when the damaged entry still names one, counts as married
 * ({@link Refusal#PLAYER_MARRIED}); when it cannot be told whom it names, every player does;</li>
 * <li>a player whose own entry is unreadable (affection, cooldown) is refused as cooling down
 * ({@link Refusal#PLAYER_COOLDOWN}) and gains no affection; when it cannot be told whose entry it is, every player
 * is.</li>
 * </ul>
 */
public final class GuestLedger extends SavedData {
	/** What a loaded guest entity is to its identity. */
	public enum Claim {
		CANONICAL, DUPLICATE, UNKNOWN_GUEST
	}

	/** Why a marriage cannot happen now. */
	public enum Refusal {
		NO_MARRIAGE, NOT_FULL_AFFECTION, PLAYER_MARRIED, GUEST_MARRIED, PLAYER_COOLDOWN, GUEST_COOLDOWN, GUEST_MISSING
	}

	/** Where a guest was last seen. */
	public record Place(ResourceKey<Level> dimension, BlockPos pos) {
	}

	/** Replaced entities kept per guest (see the class notes). */
	static final int MAX_REPLACED = 64;
	/** Server ticks between two advances of the cooldown clock from the tick hook (reads advance it too). */
	static final int CLOCK_TICKS = 20;
	/** The wall clock, in milliseconds. Package-private test seam: tests swap it, then put {@link System#currentTimeMillis} back. */
	static LongSupplier clock = System::currentTimeMillis;

	/** An entity a bind replaced: never accepted again. {@code gone}: it has been seen destroyed, so it cannot come back. */
	record Replaced(UUID entity, boolean gone) {
	}

	/** One guest's record. */
	static final class GuestRecord {
		@Nullable UUID canonical;
		/** The canonical entity was killed or removed: the identity, spouse and affection stay until a bind replaces it. */
		boolean missing;
		@Nullable Place seen;
		@Nullable UUID spouse;
		/** Real milliseconds before the guest may marry again. */
		long cooldown;
		final List<Replaced> replaced = new ArrayList<>();

		GuestRecord() {
		}

		GuestRecord(Optional<UUID> canonical, boolean missing, Optional<Place> seen, Optional<UUID> spouse, long cooldown,
				List<Replaced> replaced) {
			this.canonical = canonical.orElse(null);
			this.missing = missing && this.canonical != null;
			this.seen = seen.orElse(null);
			this.spouse = spouse.orElse(null);
			this.cooldown = Math.max(0L, cooldown);
			this.replaced.addAll(replaced);
		}

		boolean empty() {
			return canonical == null && seen == null && spouse == null && cooldown <= 0 && replaced.isEmpty();
		}
	}

	/** One player's record: affection per guest and the remarriage cooldown. Their spouse is kept on the guest's side. */
	static final class PlayerRecord {
		final Map<String, Integer> affection = new TreeMap<>();
		/** Real milliseconds before the player may marry again. */
		long cooldown;

		PlayerRecord() {
		}

		PlayerRecord(Map<String, Integer> affection, long cooldown) {
			affection.forEach((guest, value) -> {
				int kept = Math.clamp(value, 0, GuestBalance.MAX_AFFECTION);
				if (kept > 0) {
					this.affection.put(guest, kept);
				}
			});
			this.cooldown = Math.max(0L, cooldown);
		}

		boolean empty() {
			return affection.isEmpty() && cooldown <= 0;
		}
	}

	// Codecs in dependency order (each refers only to those above it).
	private static final Codec<Place> PLACE = RecordCodecBuilder.create(i -> i.group(
			ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Place::dimension),
			BlockPos.CODEC.fieldOf("pos").forGetter(Place::pos)
	).apply(i, Place::new));
	private static final Codec<Replaced> REPLACED = RecordCodecBuilder.create(i -> i.group(
			UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(Replaced::entity),
			Codec.BOOL.optionalFieldOf("gone", false).forGetter(Replaced::gone)
	).apply(i, Replaced::new));
	private static final Codec<GuestRecord> GUEST = RecordCodecBuilder.create(i -> i.group(
			UUIDUtil.STRING_CODEC.optionalFieldOf("canonical").forGetter(r -> Optional.ofNullable(r.canonical)),
			Codec.BOOL.optionalFieldOf("missing", false).forGetter(r -> r.missing),
			PLACE.optionalFieldOf("last_seen").forGetter(r -> Optional.ofNullable(r.seen)),
			UUIDUtil.STRING_CODEC.optionalFieldOf("spouse").forGetter(r -> Optional.ofNullable(r.spouse)),
			Codec.LONG.optionalFieldOf("cooldown_millis", 0L).forGetter(r -> r.cooldown),
			REPLACED.listOf().optionalFieldOf("replaced", List.of()).forGetter(r -> List.copyOf(r.replaced))
	).apply(i, GuestRecord::new));
	private static final Codec<PlayerRecord> PLAYER = RecordCodecBuilder.create(i -> i.group(
			Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("affection", Map.of()).forGetter(r -> Map.copyOf(r.affection)),
			Codec.LONG.optionalFieldOf("cooldown_millis", 0L).forGetter(r -> r.cooldown)
	).apply(i, PlayerRecord::new));
	private static final Codec<GuestLedger> BODY = RecordCodecBuilder.create(i -> i.group(
			Saved.keeping("town_guests", Codec.STRING, GUEST)
					.optionalFieldOf("guests", Saved.Kept.<String, GuestRecord>of(Map.of())).forGetter(GuestLedger::savedGuests),
			Saved.keeping("town_guests", UUIDUtil.STRING_CODEC, PLAYER)
					.optionalFieldOf("players", Saved.Kept.<UUID, PlayerRecord>of(Map.of())).forGetter(GuestLedger::savedPlayers),
			Codec.LONG.optionalFieldOf("clock_high_millis", 0L).forGetter(ledger -> ledger.high)
	).apply(i, GuestLedger::new));
	/** The whole ledger, versioned ({@link Saved#versioned}). */
	public static final Codec<GuestLedger> CODEC = Saved.versioned("town_guests", BODY);
	static final SavedDataType<GuestLedger> TYPE = new SavedDataType<>(Jugcraft.id("town_guests"), GuestLedger::new, CODEC, null);

	private static int ticks;

	/** Guest id to record (sorted, so the repair of a damaged save is deterministic). */
	private final Map<String, GuestRecord> guests = new TreeMap<>();
	private final Map<UUID, PlayerRecord> players = new HashMap<>();
	/** Entries kept as written because they could not be read. */
	private final Map<String, Dynamic<?>> unreadGuests = new LinkedHashMap<>();
	private final Map<String, Dynamic<?>> unreadPlayers = new LinkedHashMap<>();
	/** Player to spouse, built from the guests' records (the one place a marriage is saved). */
	private final Map<UUID, String> spouses = new HashMap<>();
	/** Players named as spouses in unreadable guest entries (to the guest id): they count as married. */
	private final Map<UUID, String> unreadSpouses = new HashMap<>();
	/** Players whose own entry is unreadable: no marriage, no new affection, nothing written over it. */
	private final Set<UUID> unreadablePlayers = new HashSet<>();
	/** An unreadable guest entry whose spouse cannot be told: anyone may be married. */
	private boolean unknownSpouse;
	/** An unreadable player entry whose player cannot be told: anyone's cooldown may be running. */
	private boolean unknownPlayer;
	/** The wall clock's high-water mark (milliseconds): the latest reading seen; 0 until anchored. */
	private long high;
	/** How many times this ledger has changed (tests read it: {@link #seen} counts a change only when the place changes). */
	int changes;

	/** A new, empty ledger, its clock anchored now. */
	GuestLedger() {
		this.high = clock.getAsLong();
	}

	private GuestLedger(Saved.Kept<String, GuestRecord> guests, Saved.Kept<UUID, PlayerRecord> players, long high) {
		this.guests.putAll(guests.read());
		this.unreadGuests.putAll(guests.unread());
		this.players.putAll(players.read());
		this.unreadPlayers.putAll(players.unread());
		this.high = high;
		inspectDamage();
		for (Map.Entry<String, GuestRecord> entry : this.guests.entrySet()) {
			UUID spouse = entry.getValue().spouse;
			if (spouse == null) {
				continue;
			}
			String earlier = spouses.putIfAbsent(spouse, entry.getKey());
			if (earlier != null) {
				// A damaged save gave one player two spouses: the first (by guest id) is kept, as the rule allows one.
				Jugcraft.LOGGER.warn("[guests] Player {} was saved married to both {} and {}; keeping {}", spouse, earlier, entry.getKey(), earlier);
				entry.getValue().spouse = null;
				setDirty();
			}
		}
	}

	/** Works out (again) whom the unreadable entries concern, and says so loudly. */
	private void inspectDamage() {
		unreadSpouses.clear();
		unreadablePlayers.clear();
		unknownSpouse = false;
		unknownPlayer = false;
		unreadGuests.forEach((guest, raw) -> {
			Jugcraft.LOGGER.error("[guests] {}'s saved record cannot be read; it is kept as written, and {} is frozen (no binding, "
					+ "no marriage) until an operator repairs the town_guests file", guest, guest);
			if (raw.getMapValues().result().isEmpty()) {
				unknownSpouse = true;
				Jugcraft.LOGGER.error("[guests] {}'s damaged record may name a spouse that cannot be told: every marriage is refused", guest);
				return;
			}
			Optional<String> named = raw.get("spouse").get().result().map(spouse -> spouse.asString().result().orElse(""));
			if (named.isEmpty()) {
				return;   // no spouse saved
			}
			UUID spouse = uuid(named.get());
			if (spouse == null) {
				unknownSpouse = true;
				Jugcraft.LOGGER.error("[guests] {}'s damaged record names a spouse that cannot be read: every marriage is refused", guest);
			} else {
				unreadSpouses.put(spouse, guest);
				Jugcraft.LOGGER.error("[guests] Player {} counts as married to {} (from its damaged record) and is refused marriage", spouse, guest);
			}
		});
		unreadPlayers.forEach((key, raw) -> {
			UUID player = uuid(key);
			if (player == null) {
				unknownPlayer = true;
				Jugcraft.LOGGER.error("[guests] A saved player record ({}) cannot be read and names no player: every marriage is refused", key);
			} else {
				unreadablePlayers.add(player);
				Jugcraft.LOGGER.error("[guests] Player {}'s saved record cannot be read; it is kept as written, and they are refused marriage "
						+ "and new affection until an operator repairs the town_guests file", player);
			}
		});
	}

	private static @Nullable UUID uuid(String text) {
		try {
			return UUID.fromString(text);
		} catch (IllegalArgumentException notUuid) {
			return null;
		}
	}

	/**
	 * Whether damaged saved data concerns this player: their own entry, or a spouse entry naming them (or naming someone
	 * who cannot be told). They are refused marriage until an operator repairs the file.
	 */
	boolean damaged(UUID player) {
		return unknownSpouse || unknownPlayer || unreadSpouses.containsKey(player) || unreadablePlayers.contains(player);
	}

	/** Whether this player's own saved entry is unreadable (kept as written; nothing may be written over it). */
	boolean unreadable(UUID player) {
		return unreadablePlayers.contains(player);
	}

	/**
	 * Whether this player's affection may not be changed: their own saved entry is unreadable, or an unreadable entry
	 * names a player who cannot be told (it might be theirs, so it blocks everyone until an operator repairs the file).
	 */
	boolean affectionLocked(UUID player) {
		return unknownPlayer || unreadablePlayers.contains(player);
	}

	private Saved.Kept<String, GuestRecord> savedGuests() {
		Map<String, GuestRecord> kept = new LinkedHashMap<>();
		guests.forEach((guest, record) -> {
			if (!record.empty()) {
				kept.put(guest, record);
			}
		});
		return new Saved.Kept<>(kept, unreadGuests);
	}

	private Saved.Kept<UUID, PlayerRecord> savedPlayers() {
		Map<UUID, PlayerRecord> kept = new LinkedHashMap<>();
		players.forEach((player, record) -> {
			if (!record.empty()) {
				kept.put(player, record);
			}
		});
		return new Saved.Kept<>(kept, unreadPlayers);
	}

	/**
	 * Server hooks: the cooldown clock advances about once a second; {@link GuestMarriage} notes the game time every tick
	 * (its expiry high-water mark), and its pending proposals and divorces, kept in memory, are dropped when the server
	 * stops. Called once from {@link GuestService}.
	 */
	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			GuestMarriage.observe(server);
			if (++ticks >= CLOCK_TICKS) {
				ticks = 0;
				get(server).advance();
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			ticks = 0;
			GuestMarriage.clear();
		});
	}

	/** The server's ledger (the overworld's saved data). */
	public static GuestLedger get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	// ---------------------------------------------------------------- identity

	/** Called for each loaded guest entity once a second: binds an unbound id to it, else says whether it is the one. */
	public Claim verify(String guest, UUID entity) {
		if (!placeable(guest)) {
			return Claim.UNKNOWN_GUEST;   // not a guest that may stand anywhere (unknown, pending or unreadable): left alone
		}
		GuestRecord record = guests.get(guest);
		if (record == null || record.canonical == null) {
			if (retired(entity) || canonicalElsewhere(entity, guest)) {
				return Claim.DUPLICATE;
			}
			record = guests.computeIfAbsent(guest, id -> new GuestRecord());
			record.canonical = entity;
			record.missing = false;
			changed();
			Jugcraft.LOGGER.info("[guests] {} is bound to entity {} (the first to claim it)", guest, entity);
			return Claim.CANONICAL;
		}
		if (!entity.equals(record.canonical)) {
			return Claim.DUPLICATE;
		}
		if (record.missing) {
			// The canonical entity itself is back (its removal was not final after all): it is no longer missing.
			record.missing = false;
			changed();
			Jugcraft.LOGGER.info("[guests] {}'s canonical entity {} is present again", guest, entity);
		}
		return Claim.CANONICAL;
	}

	/** The guest's canonical entity, loaded or not; null while unbound. */
	public @Nullable UUID canonical(String guest) {
		GuestRecord record = guests.get(guest);
		return record == null ? null : record.canonical;
	}

	/**
	 * Makes {@code entity} the guest's canonical entity (placement, operator repair). Refused (false) while another
	 * canonical entity exists and has not been {@link #lost}; the entity it replaces is never accepted again.
	 */
	public boolean bind(String guest, UUID entity) {
		return bind(guest, entity, false);
	}

	/**
	 * An operator's forced rebind ({@code /guests repair rebind <guest> force}): as {@link #bind}, but a canonical entity
	 * that is not lost is replaced too, kept as a tombstone that may still come back (and leaves when it does).
	 */
	boolean forceBind(String guest, UUID entity) {
		return bind(guest, entity, true);
	}

	private boolean bind(String guest, UUID entity, boolean force) {
		if (!placeable(guest) || retired(entity) || canonicalElsewhere(entity, guest)) {
			return false;
		}
		GuestRecord record = guests.computeIfAbsent(guest, id -> new GuestRecord());
		UUID old = record.canonical;
		if (entity.equals(old)) {
			if (record.missing) {
				record.missing = false;
				changed();
			}
			return true;
		}
		if (old != null) {
			if (!record.missing && !force) {
				return false;
			}
			if (!retire(guest, record, new Replaced(old, record.missing))) {
				return false;
			}
		}
		boolean wasMissing = record.missing;
		record.canonical = entity;
		record.missing = false;
		changed();
		Jugcraft.LOGGER.info("[guests] {} is bound to entity {}{}", guest, entity,
				old == null ? "" : " in place of " + old + (wasMissing ? " (lost)" : " (forced)"));
		return true;
	}

	/** Keeps {@code old} as a tombstone, making room if it must; false (nothing changed) when no room can be made. */
	private boolean retire(String guest, GuestRecord record, Replaced old) {
		if (record.replaced.size() >= MAX_REPLACED) {
			int gone = -1;
			for (int i = 0; i < record.replaced.size(); i++) {
				if (record.replaced.get(i).gone()) {
					gone = i;
					break;
				}
			}
			if (gone < 0) {
				Jugcraft.LOGGER.warn("[guests] {} already keeps {} replaced entities that may still exist; refusing another rebind",
						guest, MAX_REPLACED);
				return false;
			}
			record.replaced.remove(gone);   // the oldest one seen destroyed: it cannot come back
		}
		record.replaced.add(old);
		return true;
	}

	/** Records where the canonical entity is (for menus and repair); marks the ledger changed only when the block changes. Ignored for any other entity. */
	public void seen(String guest, UUID entity, ResourceKey<Level> dimension, BlockPos pos) {
		GuestRecord record = guests.get(guest);
		if (record == null || !entity.equals(record.canonical)) {
			return;
		}
		if (record.seen == null || !record.seen.dimension().equals(dimension) || !record.seen.pos().equals(pos)) {
			record.seen = new Place(dimension, pos.immutable());
			changed();
		}
	}

	/** The canonical entity was killed or removed for good: the identity, spouse and affection are kept. Ignored for any other entity. */
	public void lost(String guest, UUID entity) {
		GuestRecord record = guests.get(guest);
		if (record == null) {
			return;
		}
		if (entity.equals(record.canonical)) {
			if (!record.missing) {
				record.missing = true;
				changed();
				Jugcraft.LOGGER.info("[guests] {}'s canonical entity {} is gone; the identity, spouse and affection are kept", guest, entity);
			}
			return;
		}
		// A replaced entity seen destroyed can no longer come back: its tombstone may now make room for another.
		for (int i = 0; i < record.replaced.size(); i++) {
			Replaced old = record.replaced.get(i);
			if (old.entity().equals(entity) && !old.gone()) {
				record.replaced.set(i, new Replaced(entity, true));
				changed();
				return;
			}
		}
	}

	/** Whether the guest's canonical entity is gone (lost and not yet replaced). */
	public boolean missing(String guest) {
		GuestRecord record = guests.get(guest);
		return record != null && record.canonical != null && record.missing;
	}

	public @Nullable Place lastSeen(String guest) {
		GuestRecord record = guests.get(guest);
		return record == null ? null : record.seen;
	}

	/** The entities a bind replaced for this guest, oldest first (never accepted again). */
	List<UUID> replaced(String guest) {
		GuestRecord record = guests.get(guest);
		return record == null ? List.of() : record.replaced.stream().map(Replaced::entity).toList();
	}

	/** Whether the guest's saved entry could not be read (frozen until an operator repairs the file). */
	boolean unreadable(String guest) {
		return unreadGuests.containsKey(guest);
	}

	/** A roster guest that may be placed (not pending), whose record is readable. */
	private boolean placeable(String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		return entry != null && entry.available() && !unreadable(guest);
	}

	/** Whether {@code entity} was replaced by a bind, for any guest: it is never accepted again. */
	private boolean retired(UUID entity) {
		for (GuestRecord record : guests.values()) {
			for (Replaced old : record.replaced) {
				if (old.entity().equals(entity)) {
					return true;
				}
			}
		}
		return false;
	}

	/** Whether {@code entity} is another guest's canonical entity: one entity is never two guests. */
	private boolean canonicalElsewhere(UUID entity, String guest) {
		for (Map.Entry<String, GuestRecord> entry : guests.entrySet()) {
			if (!entry.getKey().equals(guest) && entity.equals(entry.getValue().canonical)) {
				return true;
			}
		}
		return false;
	}

	// ---------------------------------------------------------------- affection

	public int affection(UUID player, String guest) {
		PlayerRecord record = players.get(player);
		return record == null ? 0 : record.affection.getOrDefault(guest, 0);
	}

	/**
	 * Whether a positive {@link #addAffection} would really change the stored value now: the guest is in the roster and
	 * available (not pending), the guest's own saved entry and the player's are readable (so neither identity nor
	 * affection is damaged), and the player's affection is below {@link GuestBalance#MAX_AFFECTION}. Anything that takes
	 * an item for affection (gifts, quests) asks this first, so a damaged ledger or full affection never consumes an item
	 * for nothing.
	 */
	public boolean canGainAffection(UUID player, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		return entry != null && entry.available() && !unreadable(guest) && !affectionLocked(player)
				&& affection(player, guest) < GuestBalance.MAX_AFFECTION;
	}

	/**
	 * Adds {@code delta} (may be negative), kept within 0 to the maximum; returns the new value. Nothing changes for an
	 * unknown or pending guest, or when the guest's or the player's saved entry is unreadable (see
	 * {@link #canGainAffection}).
	 */
	public int addAffection(UUID player, String guest, int delta) {
		int now = affection(player, guest);
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available()) {
			return now;   // a pending or unknown guest cannot be befriended
		}
		if (affectionLocked(player) || unreadable(guest)) {
			Jugcraft.LOGGER.warn("[guests] Affection of player {} for {} not changed: saved data about them cannot be read", player, guest);
			return now;
		}
		return setAffection(player, guest, (int) Math.clamp((long) now + delta, 0L, (long) GuestBalance.MAX_AFFECTION));
	}

	/** Sets the player's affection for the guest (operator repair), kept within 0 to the maximum; returns the new value. */
	int setAffection(UUID player, String guest, int value) {
		int next = Math.clamp(value, 0, GuestBalance.MAX_AFFECTION);
		if (affectionLocked(player)) {
			return affection(player, guest);   // never write a fresh entry over one kept as written, or one that might be theirs
		}
		if (next == affection(player, guest)) {
			return next;
		}
		PlayerRecord record = players.computeIfAbsent(player, id -> new PlayerRecord());
		if (next == 0) {
			record.affection.remove(guest);
		} else {
			record.affection.put(guest, next);
		}
		if (record.empty()) {
			players.remove(player);
		}
		changed();
		return next;
	}

	/** The player's affection for every guest they have any for. */
	Map<String, Integer> affections(UUID player) {
		PlayerRecord record = players.get(player);
		return record == null ? Map.of() : Map.copyOf(record.affection);
	}

	/** How many players hold any affection for the guest. */
	int admirers(String guest) {
		int count = 0;
		for (PlayerRecord record : players.values()) {
			if (record.affection.containsKey(guest)) {
				count++;
			}
		}
		return count;
	}

	// ---------------------------------------------------------------- marriage

	public @Nullable String spouseOf(UUID player) {
		return spouses.get(player);
	}

	public @Nullable UUID spouseOf(String guest) {
		GuestRecord record = guests.get(guest);
		return record == null ? null : record.spouse;
	}

	/** Real milliseconds left before the player may marry again; 0 when free. */
	public long cooldownMillis(UUID player) {
		advance();
		PlayerRecord record = players.get(player);
		return record == null ? 0L : record.cooldown;
	}

	/** Real milliseconds left before the guest may marry again; 0 when free. */
	public long cooldownMillis(String guest) {
		advance();
		GuestRecord record = guests.get(guest);
		return record == null ? 0L : record.cooldown;
	}

	/**
	 * Why the player may not marry the guest now, or null when they may (items and distance are not the ledger's). The
	 * first that applies, in the order of {@link Refusal}: a guest with no marriage (pending, not {@code mayMarry}, or its
	 * record unreadable), affection short of full, either side married, either side's cooldown, then no canonical entity
	 * in the world. Damaged saved data about the player (see the class notes) refuses them straight after the first.
	 */
	public @Nullable Refusal canMarry(UUID player, String guest) {
		advance();
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.mayMarry() || unreadable(guest)) {
			return Refusal.NO_MARRIAGE;
		}
		if (unknownSpouse || unreadSpouses.containsKey(player)) {
			return Refusal.PLAYER_MARRIED;   // a damaged guest entry names them (or anyone) as its spouse
		}
		if (unknownPlayer || unreadablePlayers.contains(player)) {
			return Refusal.PLAYER_COOLDOWN;   // their own record (affection, cooldown) cannot be read
		}
		if (affection(player, guest) < GuestBalance.MAX_AFFECTION) {
			return Refusal.NOT_FULL_AFFECTION;
		}
		if (spouses.containsKey(player)) {
			return Refusal.PLAYER_MARRIED;
		}
		GuestRecord record = guests.get(guest);
		if (record != null && record.spouse != null) {
			return Refusal.GUEST_MARRIED;
		}
		PlayerRecord mine = players.get(player);
		if (mine != null && mine.cooldown > 0) {
			return Refusal.PLAYER_COOLDOWN;
		}
		if (record != null && record.cooldown > 0) {
			return Refusal.GUEST_COOLDOWN;
		}
		if (record == null || record.canonical == null || record.missing) {
			return Refusal.GUEST_MISSING;
		}
		return null;
	}

	/** Marries them if {@link #canMarry} allows it, both sides at once; false (nothing changed) otherwise. */
	public boolean marry(UUID player, String guest) {
		Refusal why = canMarry(player, guest);
		if (why != null) {
			if (damaged(player) || unreadable(guest)) {
				Jugcraft.LOGGER.warn("[guests] Marriage of player {} and {} refused ({}): saved data about them cannot be read", player, guest, why);
			}
			return false;
		}
		guests.get(guest).spouse = player;   // canMarry found the record: a canonical entity is required
		spouses.put(player, guest);
		changed();
		Jugcraft.LOGGER.info("[guests] Player {} married {}", player, guest);
		return true;
	}

	/**
	 * Undoes a marriage just made, with no cooldown for either side (the proposal item could not be taken). Only for
	 * {@link GuestMarriage}; nothing happens unless the player is married to exactly this guest.
	 */
	void unmarry(UUID player, String guest) {
		GuestRecord record = guests.get(guest);
		if (!guest.equals(spouses.get(player)) || record == null || !player.equals(record.spouse)) {
			return;
		}
		record.spouse = null;
		spouses.remove(player);
		changed();
		Jugcraft.LOGGER.warn("[guests] Marriage of player {} and {} undone (no cooldown): the proposal item could not be taken", player, guest);
	}

	/** Divorces the player from their spouse, starting both cooldowns; the guest freed, or null if unmarried. */
	public @Nullable String divorce(UUID player) {
		String guest = spouses.get(player);
		if (guest == null) {
			return null;
		}
		separate(guest, true);
		Jugcraft.LOGGER.info("[guests] Player {} divorced {}; neither may marry for {} ms", player, guest, GuestBalance.REMARRIAGE_COOLDOWN_MILLIS);
		return guest;
	}

	/** An operator's forced divorce of the guest, with or without the two cooldowns; the player freed, or null if unmarried. */
	@Nullable UUID forceDivorce(String guest, boolean cooldowns) {
		return separate(guest, cooldowns);
	}

	private @Nullable UUID separate(String guest, boolean cooldowns) {
		advance();   // time before now is not taken off the cooldowns started now
		GuestRecord record = guests.get(guest);
		if (record == null || record.spouse == null) {
			return null;
		}
		UUID player = record.spouse;
		record.spouse = null;
		spouses.remove(player);
		if (cooldowns) {
			record.cooldown = GuestBalance.REMARRIAGE_COOLDOWN_MILLIS;
			if (unreadable(player)) {
				// Their own entry is kept as written; they stay refused marriage until it is repaired.
				Jugcraft.LOGGER.warn("[guests] Player {}'s cooldown not recorded: their saved record cannot be read", player);
			} else {
				players.computeIfAbsent(player, id -> new PlayerRecord()).cooldown = GuestBalance.REMARRIAGE_COOLDOWN_MILLIS;
			}
		}
		changed();
		return player;
	}

	/** Ends the player's cooldown (operator repair); false when there was none. */
	boolean clearCooldown(UUID player) {
		advance();
		PlayerRecord record = players.get(player);
		if (record == null || record.cooldown <= 0) {
			return false;
		}
		record.cooldown = 0;
		if (record.empty()) {
			players.remove(player);
		}
		changed();
		return true;
	}

	/** Ends the guest's cooldown (operator repair); false when there was none. */
	boolean clearCooldown(String guest) {
		advance();
		GuestRecord record = guests.get(guest);
		if (record == null || record.cooldown <= 0) {
			return false;
		}
		record.cooldown = 0;
		changed();
		return true;
	}

	// ---------------------------------------------------------------- the clock

	/**
	 * Takes the real time beyond the clock's high-water mark off every running cooldown and raises the mark. A reading at
	 * or below the mark (the clock moved back, or still climbing back) credits nothing. Server thread only.
	 */
	void advance() {
		long now = clock.getAsLong();
		if (high <= 0) {
			high = now;   // never anchored (a save from before the mark): nothing to measure from
			if (running()) {
				changed();
			}
			return;
		}
		if (now <= high) {
			return;
		}
		long elapsed = now - high;
		high = now;
		boolean credited = false;
		for (GuestRecord record : guests.values()) {
			if (record.cooldown > 0) {
				record.cooldown = Math.max(0L, record.cooldown - elapsed);
				credited = true;
			}
		}
		for (PlayerRecord record : players.values()) {
			if (record.cooldown > 0) {
				record.cooldown = Math.max(0L, record.cooldown - elapsed);
				credited = true;
			}
		}
		if (credited) {
			changed();
		}
	}

	private boolean running() {
		for (GuestRecord record : guests.values()) {
			if (record.cooldown > 0) {
				return true;
			}
		}
		for (PlayerRecord record : players.values()) {
			if (record.cooldown > 0) {
				return true;
			}
		}
		return false;
	}

	// ---------------------------------------------------------------- tests

	/**
	 * Forgets everything about one guest: its identity, place, spouse (both sides), cooldown, tombstones and every
	 * player's affection for it. Game tests only (GuestLedgerTesting), so each test starts from a clean guest; the mod
	 * never calls it.
	 */
	void forget(String guest) {
		GuestRecord record = guests.remove(guest);
		if (record != null && record.spouse != null) {
			spouses.remove(record.spouse);
		}
		if (unreadGuests.remove(guest) != null) {
			inspectDamage();
		}
		players.values().forEach(each -> each.affection.remove(guest));
		players.values().removeIf(PlayerRecord::empty);
		changed();
	}

	/**
	 * Makes the guest's entry unreadable, exactly as a failed read from disk leaves it: its record (or an empty one)
	 * becomes a raw entry kept as written, with one field that cannot be read, so it stays unreadable across a save and
	 * load too, and the parties it names are worked out as on loading. Game tests only (GuestLedgerTesting.damageGuest).
	 */
	void damage(String guest) {
		GuestRecord record = guests.remove(guest);
		if (record != null && record.spouse != null) {
			spouses.remove(record.spouse);
		}
		unreadGuests.put(guest, unreadableCopy(GUEST, record == null ? new GuestRecord() : record));
		inspectDamage();
		changed();
	}

	/** As {@link #damage(String)}, for the player's own entry (GuestLedgerTesting.damagePlayer). */
	void damage(UUID player) {
		PlayerRecord record = players.remove(player);
		unreadPlayers.put(player.toString(), unreadableCopy(PLAYER, record == null ? new PlayerRecord() : record));
		inspectDamage();
		changed();
	}

	/** {@code value} as written, with its cooldown field replaced by text, which no reading accepts. */
	private static <T> Dynamic<?> unreadableCopy(Codec<T> codec, T value) {
		CompoundTag written = codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow() instanceof CompoundTag tag ? tag : new CompoundTag();
		written.putString("cooldown_millis", "unreadable");
		return new Dynamic<>(NbtOps.INSTANCE, written);
	}

	private void changed() {
		changes++;
		setDirty();
	}
}
