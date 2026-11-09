package io.github.jimbozoomer.jugcraft.menagerie;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.town.Town;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The pirate ships at sea (docs/features/pirate-ships.md): which ships exist, where they were raised, and when each
 * stretch of sea may raise another. Saved with the overworld, so a ship whose chunks are unloaded still counts: it holds
 * its spawn region and its place under the cap until it is defeated, sunk or removed, and reloading never makes a second.
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
	/** A ship unseen this long (its chunks never loaded again: seven days) no longer holds its region or a place under the cap. */
	public static final long STALE = 168_000L;
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
			Rest.CODEC.listOf().optionalFieldOf("rests", List.of()).forGetter(PirateSeas::rests)).apply(i, PirateSeas::new));
	private static final SavedDataType<PirateSeas> TYPE = new SavedDataType<>(Jugcraft.id("pirate_seas"), PirateSeas::new, CODEC, null);

	private final Map<UUID, Ship> ships = new HashMap<>();
	private final Map<Long, Long> rests = new HashMap<>();

	public PirateSeas() {
	}

	private PirateSeas(List<Ship> ships, List<Rest> rests) {
		for (Ship ship : ships) {
			this.ships.put(ship.id(), ship);
		}
		for (Rest rest : rests) {
			this.rests.merge(rest.region(), rest.until(), Math::max);
		}
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

	/** Ships afloat and crewed that still hold a place under the cap. */
	public int active(long now) {
		int count = 0;
		for (Ship ship : ships.values()) {
			if (ship.state() == State.ACTIVE && now - ship.seen() < STALE) {
				count++;
			}
		}
		return count;
	}

	/** Whether a region may raise a ship now: no live ship of its own, beaten or not, and not resting. */
	public boolean regionFree(long region, long now) {
		if (now < rests.getOrDefault(region, Long.MIN_VALUE)) {
			return false;
		}
		for (Ship ship : ships.values()) {
			if (ship.region() == region && now - ship.seen() < STALE) {
				return false;
			}
		}
		return true;
	}

	/** Records a ship just raised. */
	public void raised(UUID id, long region, BlockPos anchor, long now) {
		ships.put(id, new Ship(id, region, anchor, State.ACTIVE, now));
		setDirty();
	}

	/**
	 * A ship's chunk holds it now: refreshes its record, or records it again if it had gone stale (its region and place
	 * come back with it). Called by natural ships only, every few seconds while loaded.
	 */
	public void seen(UUID id, long region, BlockPos anchor, boolean defeated, long now) {
		Ship old = ships.get(id);
		State state = defeated || old != null && old.state() == State.DEFEATED ? State.DEFEATED : State.ACTIVE;
		ships.put(id, new Ship(id, old == null ? region : old.region(), old == null ? anchor : old.anchor(), state, now));
		setDirty();
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

	/** The ship is sunk or removed for good: its record goes, and its region rests. */
	public void gone(UUID id, long now) {
		Ship old = ships.remove(id);
		if (old != null) {
			rest(old.region(), now);
			setDirty();
		}
	}

	private void rest(long region, long now) {
		rests.merge(region, now + COOLDOWN, Math::max);
	}

	/** Forgets ships unseen past {@link #STALE} (their regions rest) and rests that are over. */
	public void expire(long now) {
		List<UUID> stale = new ArrayList<>();
		for (Ship ship : ships.values()) {
			if (now - ship.seen() >= STALE) {
				stale.add(ship.id());
			}
		}
		for (UUID id : stale) {
			gone(id, now);
		}
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
	 * Whether a town, a land claim or a village lies within {@value #SETTLEMENT_CLEARANCE} blocks. Towns and claims are
	 * saved data and need no chunk; villages are read from their meeting points only where the chunk is loaded.
	 */
	static boolean nearSettlement(ServerLevel level, Vec3 at) {
		int r = SETTLEMENT_CLEARANCE;
		BlockPos centre = BlockPos.containing(at);
		for (int cx = (centre.getX() - r) >> 4; cx <= (centre.getX() + r) >> 4; cx++) {
			for (int cz = (centre.getZ() - r) >> 4; cz <= (centre.getZ() + r) >> 4; cz++) {
				BlockPos pos = new BlockPos((cx << 4) + 8, centre.getY(), (cz << 4) + 8);
				if (!LandRegistry.mayBuildFor(null, level, pos) || Town.isProtected(level, pos)
						|| level.hasChunkAt(pos) && level.isVillage(pos)) {
					return true;
				}
			}
		}
		return false;
	}
}
