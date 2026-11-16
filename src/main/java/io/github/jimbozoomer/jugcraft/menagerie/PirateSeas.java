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
import net.minecraft.world.entity.player.Player;
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
 * adventure wearing at least {@value PirateFairness#MIN_ARMOR} armour points, never near a town, claim, operator region
 * or village, nor where any chunk that near is not loaded (what cannot be seen counts as settled), at most
 * {@value #MAX_SHIPS} afloat, at most one per {@value #REGION_SIZE}-block region, which then rests {@value #COOLDOWN}
 * ticks after its ship is beaten or gone, none within {@value PirateFairness#SPACING} blocks of another ship's anchor,
 * and at most one a day near any one player ({@link PirateFairness}). A natural ship carries a crew of three to five,
 * by that player's armour.
 *
 * <p>Saved as format {@value #FORMAT}: format 1 (no {@code version} field) had no player rests; reading it starts with
 * none, and every ship, rest and tombstone in it is kept as it was.
 */
public final class PirateSeas extends SavedData {
	/** Ships afloat at once, loaded or not ({@link PirateFairness#MAX_SHIPS}). */
	public static final int MAX_SHIPS = PirateFairness.MAX_SHIPS;
	/** The saved format: 2 added the players' rests. */
	public static final int FORMAT = 2;
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
	/** No ship is raised with a town, claim or village this close, or with a chunk this close not loaded. */
	public static final int SETTLEMENT_CLEARANCE = 48;
	/**
	 * Heights above the waterline at which villages are looked for in each chunk round the place. Each look reaches one
	 * section (16 blocks) up and down, so these cover from 16 blocks under the waterline to 80 above it: a village on the
	 * shore or on the cliffs over it.
	 */
	static final int[] VILLAGE_HEIGHTS = {0, 32, 64};

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

	/** A player near whom no ship may be raised until a game time (one had been, a day before). */
	public record PlayerRest(UUID player, long until) {
		static final Codec<PlayerRest> CODEC = RecordCodecBuilder.create(i -> i.group(
				UUIDUtil.CODEC.fieldOf("player").forGetter(PlayerRest::player),
				Codec.LONG.fieldOf("until").forGetter(PlayerRest::until)).apply(i, PlayerRest::new));
	}

	public static final Codec<PirateSeas> CODEC = RecordCodecBuilder.create(i -> i.group(
			Ship.CODEC.listOf().optionalFieldOf("ships", List.of()).forGetter(s -> List.copyOf(s.ships.values())),
			Rest.CODEC.listOf().optionalFieldOf("rests", List.of()).forGetter(PirateSeas::rests),
			UUIDUtil.CODEC.listOf().optionalFieldOf("gone", List.of()).forGetter(s -> List.copyOf(s.tombstones)),
			PlayerRest.CODEC.listOf().optionalFieldOf("players", List.of()).forGetter(PirateSeas::playerRests),
			Codec.INT.optionalFieldOf("version", 1).forGetter(s -> FORMAT)).apply(i, PirateSeas::new));
	private static final SavedDataType<PirateSeas> TYPE = new SavedDataType<>(Jugcraft.id("pirate_seas"), PirateSeas::new, CODEC, null);

	private final Map<UUID, Ship> ships = new HashMap<>();
	private final Map<Long, Long> rests = new HashMap<>();
	private final java.util.LinkedHashSet<UUID> tombstones = new java.util.LinkedHashSet<>();
	private final Map<UUID, Long> players = new HashMap<>();

	public PirateSeas() {
	}

	private PirateSeas(List<Ship> ships, List<Rest> rests, List<UUID> gone, List<PlayerRest> players, int version) {
		for (Ship ship : ships) {
			this.ships.put(ship.id(), ship);
		}
		for (Rest rest : rests) {
			this.rests.merge(rest.region(), rest.until(), Math::max);
		}
		tombstones.addAll(gone);
		for (PlayerRest rest : players) {
			this.players.merge(rest.player(), rest.until(), Math::max);
		}
	}

	private List<PlayerRest> playerRests() {
		List<PlayerRest> out = new ArrayList<>();
		players.forEach((player, until) -> out.add(new PlayerRest(player, until)));
		return out;
	}

	/** Until when no ship may be raised near {@code player} (a time already past when none is resting). */
	public long playerRest(UUID player) {
		return players.getOrDefault(player, Long.MIN_VALUE);
	}

	/** A ship was raised near {@code player}: none other may be for {@value PirateFairness#PLAYER_REST} ticks. */
	public void restPlayer(UUID player, long now) {
		players.merge(player, now + PirateFairness.PLAYER_REST, Math::max);
		setDirty();
	}

	/** How far (horizontally, blocks) the nearest recorded ship's anchor is from (x, z), beaten or not; infinite if none. */
	public double nearestShip(double x, double z) {
		double best = Double.POSITIVE_INFINITY;
		for (Ship ship : ships.values()) {
			double dx = ship.anchor().getX() + 0.5 - x, dz = ship.anchor().getZ() + 0.5 - z;
			best = Math.min(best, Math.sqrt(dx * dx + dz * dz));
		}
		return best;
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
		boolean changed = rests.values().removeIf(until -> until <= now);
		if (players.values().removeIf(until -> until <= now) || changed) {
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
			// Only a player who could take a ship on is tried: survival or adventure, armoured, not met by one today.
			if (!player.isAlive() || player.isSpectator() || player.isCreative() || player.getArmorValue() < PirateFairness.MIN_ARMOR
					|| now < seas.playerRest(player.getUUID())) {
				continue;
			}
			attempts++;
			if (random.nextFloat() < CHANCE) {
				raiseNear(level, player, random, now);
			}
		}
	}

	/**
	 * One try at a ship somewhere {@value #MIN_DISTANCE}-{@value #MAX_DISTANCE} blocks from {@code player}, over ocean, if
	 * the fairness rules allow ({@link PirateFairness#refusal}), with a crew for the player's armour
	 * ({@link PirateFairness#crewFor}). A ship raised rests the player for a day.
	 */
	static @Nullable PirateShip raiseNear(ServerLevel level, Player player, RandomSource random, long now) {
		PirateSeas seas = get(level);
		double angle = random.nextDouble() * Math.PI * 2;
		double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
		Vec3 at = PirateWaters.waterline(level, player.getX() + Math.cos(angle) * distance, player.getZ() + Math.sin(angle) * distance);
		if (at == null || !level.getBiome(BlockPos.containing(at)).is(BiomeTags.IS_OCEAN)) {
			return null;
		}
		int armor = player.getArmorValue();
		if (PirateFairness.refusal(seas.active(now), armor, seas.playerRest(player.getUUID()), now,
				seas.regionFree(region(at.x, at.z), now), seas.nearestShip(at.x, at.z)) != null) {
			return null;
		}
		PirateShip ship = raise(level, at, random.nextFloat() * 360.0F, now, PirateFairness.crewFor(armor));
		if (ship != null) {
			seas.restPlayer(player.getUUID(), now);
		}
		return ship;
	}

	/** {@link #raise(ServerLevel, Vec3, float, long, int)} with the smallest crew, {@value PirateFairness#MIN_CREW}. */
	public static @Nullable PirateShip raise(ServerLevel level, Vec3 at, float yaw, long now) {
		return raise(level, at, yaw, now, PirateFairness.MIN_CREW);
	}

	/**
	 * Raises a ship with a small crew of {@code crew} (3 to 5: {@link PirateCrew#crew}) at {@code at} (a waterline place)
	 * facing {@code yaw}, if the sea allows: under the cap, its region free, no other ship's anchor within
	 * {@value PirateFairness#SPACING} blocks, the water clear ({@link PirateWaters#clear}) with room to turn
	 * ({@link PirateWaters#laneClear} of no length) and no settlement near. The crew are seated before the ship is added,
	 * so ship and crew enter the world together, once. Null, with nothing changed, when it may not.
	 */
	public static @Nullable PirateShip raise(ServerLevel level, Vec3 at, float yaw, long now, int crew) {
		PirateSeas seas = get(level);
		long region = region(at.x, at.z);
		if (seas.active(now) >= MAX_SHIPS || !seas.regionFree(region, now) || seas.nearestShip(at.x, at.z) < PirateFairness.SPACING
				|| !PirateWaters.clear(level, at, yaw) || settlementNear(level, at) != null || !PirateWaters.laneClear(level, at, at)) {
			return null;
		}
		PirateShip ship = PirateShips.SHIP.create(level, EntitySpawnReason.NATURAL);
		if (ship == null) {
			return null;
		}
		ship.snapTo(at.x, at.y, at.z, yaw, 0.0F);
		ship.launch(at, true);
		ship.setCrewed(PirateCrew.provision(level, ship, PirateCrew.crew(crew)) > 0);
		ship.setCrewFormat(PirateShip.SMALL_CREW);
		level.addFreshEntityWithPassengers(ship);
		if (level.getEntity(ship.getUUID()) != ship) {
			// Not added (a clash of ids, say): nothing of it stays.
			for (var member : ship.getPassengers()) {
				member.discard();
			}
			ship.discard();
			return null;
		}
		seas.raised(ship.getUUID(), region, BlockPos.containing(at), now);
		return ship;
	}

	/**
	 * Why no ship may be raised at {@code at}, or null when one may: a chunk within {@value #SETTLEMENT_CLEARANCE} blocks not
	 * loaded (what cannot be seen counts as settled, so a village there is never missed), a town, land claim or operator
	 * region within it (its whole height, keel to mast: saved data, tested exactly by {@link PirateWaters#unprotected}), or
	 * a village within it, from its meeting points, beds and workstations ({@link ServerLevel#isVillage}) looked for in every
	 * chunk at the waterline and above it ({@link #VILLAGE_HEIGHTS}). Loads nothing.
	 */
	public static @Nullable String settlementNear(ServerLevel level, Vec3 at) {
		int r = SETTLEMENT_CLEARANCE;
		int surface = Mth.floor(at.y);
		BlockPos centre = BlockPos.containing(at);
		int cx0 = (centre.getX() - r) >> 4, cx1 = (centre.getX() + r) >> 4;
		int cz0 = (centre.getZ() - r) >> 4, cz1 = (centre.getZ() + r) >> 4;
		for (int cx = cx0; cx <= cx1; cx++) {
			for (int cz = cz0; cz <= cz1; cz++) {
				if (level.getChunkSource().getChunkNow(cx, cz) == null) {
					return "unloaded chunk " + cx + ", " + cz;
				}
			}
		}
		AABB around = new AABB(at.x - r, surface - PirateWaters.DEPTH, at.z - r, at.x + r, surface + PirateWaters.MAST + 1, at.z + r);
		if (!PirateWaters.unprotected(level, around)) {
			return "town, claim or operator region";
		}
		for (int cx = cx0; cx <= cx1; cx++) {
			for (int cz = cz0; cz <= cz1; cz++) {
				for (int height : VILLAGE_HEIGHTS) {
					BlockPos pos = new BlockPos((cx << 4) + 8, surface + height, (cz << 4) + 8);
					if (!level.isOutsideBuildHeight(pos) && level.isVillage(pos)) {
						return "village by chunk " + cx + ", " + cz;
					}
				}
			}
		}
		return null;
	}
}
