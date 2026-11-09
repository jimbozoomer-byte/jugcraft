package io.github.jimbozoomer.jugcraft.menagerie;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The pirate ships at sea (docs/features/pirate-ships.md): which ships exist, where they were raised, and when each
 * stretch of sea may raise another. Saved with the overworld, so a ship whose chunks are unloaded still counts: it holds
 * its spawn region (and, while crewed, its place under the cap) for as long as it exists, however long it stays
 * unloaded. Only a positive end releases it: the ship sunk, beaten and gone, or removed. A removed ship's id is kept
 * as a tombstone, so a stale copy of it (an older chunk save, a restored backup) never comes back to life: it goes again,
 * quietly, without its hold.
 *
 * <p>Ships appear naturally on their own: now and then, out of sight in loaded ocean near a player in survival or
 * adventure, never near a town, claim, operator region or village, at most {@value #MAX_SHIPS} afloat and at most one per
 * {@value #REGION_SIZE}-block region, which then rests {@value #COOLDOWN} ticks after its ship is beaten or gone.
 */
public final class PirateSeas extends SavedData {
	/** Ships afloat at once, loaded or not. */
	public static final int MAX_SHIPS = 3;
	/** Spawn regions are squares of this many blocks; one ship each. */
	public static final int REGION_SIZE = 512;
	private static final int REGION_SHIFT = 9;
	/** Ticks a region rests after its ship is defeated, sunk or removed (three days). */
	public static final long COOLDOWN = 72_000L;
	/** Tombstones kept for removed ships (the oldest is forgotten first). */
	public static final int TOMBSTONES = 1_024;
	/** Ticks between spawn passes, and the most players tried in one pass. */
	public static final int PASS_TICKS = 1_200;
	public static final int MAX_ATTEMPTS = 4;
	/** The chance that a tried player gets a ship this pass, if the sea allows one. */
	public static final float CHANCE = 0.25F;
	/** How far from the player a ship is raised: past their sight of the hull, inside loaded chunks. */
	public static final int MIN_DISTANCE = 48;
	public static final int MAX_DISTANCE = 96;
	/** No ship is raised with a town, claim or village this close. */
	public static final int SETTLEMENT_CLEARANCE = 48;

	/** Where a ship stands in the ledger: still crewed, or beaten (it keeps its hold until it sinks or is removed). */
	public enum State {
		ACTIVE, DEFEATED;

		static final Codec<State> CODEC = Codec.STRING.xmap(name -> "defeated".equals(name) ? DEFEATED : ACTIVE,
				state -> state == DEFEATED ? "defeated" : "active");
	}

	/** One ship: its entity UUID, its spawn region, where it was raised, its state and when its chunk last held it. */
	public record Ship(UUID id, long region, BlockPos anchor, State state, long seen) {
		static final Codec<Ship> CODEC = RecordCodecBuilder.create(i -> i.group(
				UUIDUtil.CODEC.fieldOf("id").forGetter(Ship::id),
				Codec.LONG.fieldOf("region").forGetter(Ship::region),
				BlockPos.CODEC.fieldOf("anchor").forGetter(Ship::anchor),
				State.CODEC.fieldOf("state").forGetter(Ship::state),
				Codec.LONG.fieldOf("seen").forGetter(Ship::seen)).apply(i, Ship::new));
	}

	/** A region resting until a game time. */
	public record Rest(long region, long until) {
		static final Codec<Rest> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.LONG.fieldOf("region").forGetter(Rest::region),
				Codec.LONG.fieldOf("until").forGetter(Rest::until)).apply(i, Rest::new));
	}

	public static final Codec<PirateSeas> CODEC = RecordCodecBuilder.create(i -> i.group(
			Ship.CODEC.listOf().optionalFieldOf("ships", List.of()).forGetter(s -> List.copyOf(s.ships.values())),
			Rest.CODEC.listOf().optionalFieldOf("rests", List.of()).forGetter(PirateSeas::rests),
			UUIDUtil.CODEC.listOf().optionalFieldOf("gone", List.of()).forGetter(s -> List.copyOf(s.tombstones))).apply(i, PirateSeas::new));
	private static final SavedDataType<PirateSeas> TYPE = new SavedDataType<>(Jugcraft.id("pirate_seas"), PirateSeas::new, CODEC, null);

	private final Map<UUID, Ship> ships = new HashMap<>();
	private final Map<Long, Long> rests = new HashMap<>();
	private final java.util.LinkedHashSet<UUID> tombstones = new java.util.LinkedHashSet<>();

	public PirateSeas() {
	}

	private PirateSeas(List<Ship> ships, List<Rest> rests, List<UUID> gone) {
		for (Ship ship : ships) {
			this.ships.put(ship.id(), ship);
		}
		for (Rest rest : rests) {
			this.rests.merge(rest.region(), rest.until(), Math::max);
		}
		tombstones.addAll(gone);
	}

	private List<Rest> rests() {
		List<Rest> out = new ArrayList<>();
		rests.forEach((region, until) -> out.add(new Rest(region, until)));
		return out;
	}

	/** The overworld's ledger (pirate ships sail only there). */
	public static PirateSeas get(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	/** The spawn region of a block column. */
	public static long region(double x, double z) {
		return ((long) (Mth.floor(x) >> REGION_SHIFT) << 32) ^ ((Mth.floor(z) >> REGION_SHIFT) & 0xFFFFFFFFL);
	}

	/** The recorded ship with this id, or null. */
	public @Nullable Ship ship(UUID id) {
		return ships.get(id);
	}

	/** Ships afloat and crewed, loaded or not, however long unseen: each holds a place under the cap until it is beaten or gone. */
	public int active(long now) {
		int count = 0;
		for (Ship ship : ships.values()) {
			if (ship.state() == State.ACTIVE) {
				count++;
			}
		}
		return count;
	}

	/** Whether a region may raise a ship now: no ship of its own still exists (beaten or not), and it is not resting. */
	public boolean regionFree(long region, long now) {
		if (now < rests.getOrDefault(region, Long.MIN_VALUE)) {
			return false;
		}
		for (Ship ship : ships.values()) {
			if (ship.region() == region) {
				return false;
			}
		}
		return true;
	}

	/** Whether a ship with this id was removed for good (a copy of it must not sail again). */
	public boolean tombstoned(UUID id) {
		return tombstones.contains(id);
	}

	/** Records a ship just raised. */
	public void raised(UUID id, long region, BlockPos anchor, long now) {
		ships.put(id, new Ship(id, region, anchor, State.ACTIVE, now));
		setDirty();
	}

	/**
	 * A natural ship's chunk holds it now. False for a tombstoned ship, which must go (it is a stale copy of one already
	 * removed); otherwise its record is refreshed, or, if the ledger lost it (an edited or replaced save), recorded again,
	 * since the ship plainly exists. Called when the ship loads and every few seconds while it is loaded.
	 */
	public boolean seen(UUID id, long region, BlockPos anchor, boolean defeated, long now) {
		if (tombstones.contains(id)) {
			return false;
		}
		Ship old = ships.get(id);
		State state = defeated || old != null && old.state() == State.DEFEATED ? State.DEFEATED : State.ACTIVE;
		ships.put(id, new Ship(id, old == null ? region : old.region(), old == null ? anchor : old.anchor(), state, now));
		setDirty();
		return true;
	}

	/** The ship's crew are beaten: it leaves the cap, and its region starts to rest. */
	public void defeated(UUID id, long now) {
		Ship old = ships.get(id);
		if (old == null || old.state() == State.DEFEATED) {
			return;
		}
		ships.put(id, new Ship(id, old.region(), old.anchor(), State.DEFEATED, now));
		rest(old.region(), now);
		setDirty();
	}

	/** The ship is sunk or removed for good: its record goes, its region rests, and its id is tombstoned. */
	public void gone(UUID id, long now) {
		Ship old = ships.remove(id);
		if (old != null) {
			rest(old.region(), now);
		}
		if (tombstones.add(id) && tombstones.size() > TOMBSTONES) {
			tombstones.remove(tombstones.iterator().next());
		}
		setDirty();
	}

	private void rest(long region, long now) {
		rests.merge(region, now + COOLDOWN, Math::max);
	}

	/** Forgets rests that are over. Ships are never forgotten for being unseen: only a positive end releases one. */
	public void expire(long now) {
		if (rests.values().removeIf(until -> until <= now)) {
			setDirty();
		}
	}

	// ------------------------------------------------------------------------------------------------- natural ships

	/** The server tick: every {@value #PASS_TICKS} ticks, maybe raises a ship near a player at sea. Overworld only. */
	public static void tick(ServerLevel level) {
		if (level.dimension() != Level.OVERWORLD || level.getGameTime() % PASS_TICKS != 0
				|| level.getDifficulty() == Difficulty.PEACEFUL || !level.getGameRules().get(GameRules.SPAWN_MOBS)
				|| !level.getGameRules().get(GameRules.SPAWN_MONSTERS)) {
			return;
		}
		long now = level.getGameTime();
		PirateSeas seas = get(level);
		seas.expire(now);
		RandomSource random = level.getRandom();
		int attempts = 0;
		for (ServerPlayer player : List.copyOf(level.players())) {
			if (attempts >= MAX_ATTEMPTS || seas.active(now) >= MAX_SHIPS) {
				return;
			}
			if (!player.isAlive() || player.isSpectator() || player.isCreative()) {
				continue;
			}
			attempts++;
			if (random.nextFloat() < CHANCE) {
				raiseNear(level, player.position(), random, now);
			}
		}
	}

	/** One try at a ship somewhere {@value #MIN_DISTANCE}-{@value #MAX_DISTANCE} blocks from {@code near}, over ocean. */
	static @Nullable PirateShip raiseNear(ServerLevel level, Vec3 near, RandomSource random, long now) {
		double angle = random.nextDouble() * Math.PI * 2;
		double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
		Vec3 at = PirateWaters.waterline(level, near.x + Math.cos(angle) * distance, near.z + Math.sin(angle) * distance);
		if (at == null || !level.getBiome(BlockPos.containing(at)).is(BiomeTags.IS_OCEAN)) {
			return null;
		}
		return raise(level, at, random.nextFloat() * 360.0F, now);
	}

	/**
	 * Raises a crewed ship at {@code at} (a waterline place) facing {@code yaw}, if the sea allows: under the cap, its
	 * region free, the water clear ({@link PirateWaters#clear}) and no settlement near. The crew are seated before the ship
	 * is added, so ship and crew enter the world together, once. Null, with nothing changed, when it may not.
	 */
	public static @Nullable PirateShip raise(ServerLevel level, Vec3 at, float yaw, long now) {
		PirateSeas seas = get(level);
		long region = region(at.x, at.z);
		if (seas.active(now) >= MAX_SHIPS || !seas.regionFree(region, now) || !PirateWaters.clear(level, at, yaw)
				|| nearSettlement(level, at)) {
			return null;
		}
		PirateShip ship = PirateShips.SHIP.create(level, EntitySpawnReason.NATURAL);
		if (ship == null) {
			return null;
		}
		ship.snapTo(at.x, at.y, at.z, yaw, 0.0F);
		ship.launch(at, true);
		ship.setCrewed(PirateCrew.provision(level, ship) > 0);
		level.addFreshEntityWithPassengers(ship);
		if (level.getEntity(ship.getUUID()) != ship) {
			// Not added (a clash of ids, say): nothing of it stays.
			for (var crew : ship.getPassengers()) {
				crew.discard();
			}
			ship.discard();
			return null;
		}
		seas.raised(ship.getUUID(), region, BlockPos.containing(at), now);
		return ship;
	}

	/**
	 * Whether a town, a land claim, an operator region or a village lies within {@value #SETTLEMENT_CLEARANCE} blocks of the
	 * ship's place (its whole height, keel to mast). Towns, claims and operator regions are saved data, tested exactly
	 * ({@link PirateWaters#unprotected}); villages are read from their meeting points only where the chunk is loaded.
	 */
	static boolean nearSettlement(ServerLevel level, Vec3 at) {
		int r = SETTLEMENT_CLEARANCE;
		int surface = Mth.floor(at.y);
		AABB around = new AABB(at.x - r, surface - PirateWaters.DEPTH, at.z - r, at.x + r, surface + PirateWaters.MAST + 1, at.z + r);
		if (!PirateWaters.unprotected(level, around)) {
			return true;
		}
		BlockPos centre = BlockPos.containing(at);
		for (int cx = (centre.getX() - r) >> 4; cx <= (centre.getX() + r) >> 4; cx++) {
			for (int cz = (centre.getZ() - r) >> 4; cz <= (centre.getZ() + r) >> 4; cz++) {
				BlockPos pos = new BlockPos((cx << 4) + 8, centre.getY(), (cz << 4) + 8);
				if (level.hasChunkAt(pos) && level.isVillage(pos)) {
					return true;
				}
			}
		}
		return false;
	}
}
