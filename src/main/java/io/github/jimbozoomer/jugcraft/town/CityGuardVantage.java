package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.raiders.CityRaidApproach;
import io.github.jimbozoomer.jugcraft.raiders.RaiderBlimp;
import io.github.jimbozoomer.jugcraft.raiders.RaiderRaids;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Guards meet a raid where they can see it (docs/features/raiders.md, "City approaches and visible fights"). A town
 * guard on foot or a guard walker, while a raid is under way near it and it has nobody to fight, finds the raider on the
 * ground nearest it and moves to a vantage with a clear line to that raider, then holds there while it can see it:
 * its own targeting (sight and reach) and, for a walker, its cannon take it from there.
 *
 * <p>Vantages, in order, for the approach the raider is at ({@link CityRaidApproach#nearest}): the gate's inner road, the
 * inner end of its passage, the passage, the gate guards' posts outside it and its forecourt; for a stretch of wall,
 * the nearest gate's in the same order and then points round the outside of the wall toward the raider. Then, always
 * (a raid away from the town too), points along the way from the guard to the raider, and last points round the raider
 * ({@value #RING_NEAR} and {@value #RING_FAR} blocks off), nearest the guard first, which find the way round a wall
 * through a gap. The first loaded spot with room to stand and a clear line to the raider within sight ({@value #SIGHT}
 * blocks; {@value #WALKER_SIGHT} for a guard walker's cannon) is chosen; with none in sight, the usable one nearest the
 * raider.
 *
 * <p>Bounded: a vantage it gets no nearer for {@value #STALLS} checks ({@value #CHECK_TICKS} ticks each), or reaches and
 * still cannot see from, is set aside for that raid; with every vantage set aside it holds where it stands. Flak crews
 * keep their guns, operators' custom guards keep their own posts, and a guard keeps the town's combat immunity
 * throughout. Nothing here edits a block or loads a chunk.
 *
 * <p>This is a goal for guards, not part of {@link Townsfolk}: it is installed with {@link #install} (game tests, or the
 * one-line hook in Townsfolk.registerGoals described in the delivery notes). At priority 0 it takes precedence over
 * Townsfolk's GateDefenseGoal, which sends guards to the threatened gate's inner road, and finishes the job: from there
 * on to a spot that can see the raiders.
 */
public final class CityGuardVantage {
	/** Ticks between looks at the raid. */
	public static final int CHECK_TICKS = 40;
	/** How near a raider on the ground must be for a guard to answer it. */
	public static final int THREAT_RANGE = 128;
	/** How far a guard on foot wants to see a raider from (its follow range). */
	public static final int SIGHT = 48;
	/** How far a guard walker wants to see a raider from (well within its cannon's reach). */
	public static final int WALKER_SIGHT = 96;
	/** Checks without getting nearer a vantage before it is set aside. */
	public static final int STALLS = 3;
	/** The spacing of points toward the raider, in blocks, and how many. */
	static final int STEP = 4;
	static final int STEPS = 12;
	/** The spacing of points round the outside of the wall toward a wall section, in blocks. */
	static final int RING_STEP = 8;
	/** Points round the raider: at these distances, on this many bearings. */
	static final int RING_NEAR = 8;
	static final int RING_FAR = 14;
	static final int RING_BEARINGS = 12;

	private CityGuardVantage() {
	}

	/** Whether a townsperson keeps watch this way: a guard on foot or a guard walker, not a gun's crew or a custom guard. */
	public static boolean eligible(Townsfolk guard) {
		return guard.isGuard() && guard.civicPost==0 && guard.custom() == null && !guard.role().equals("flak_guard") && !guard.isPassenger();
	}

	/** How far this guard wants to see a raider from. */
	public static int sight(Townsfolk guard) {
		return guard instanceof TownGuardMecha ? WALKER_SIGHT : SIGHT;
	}

	/**
	 * The raider on the ground nearest {@code guard}, within {@value #THREAT_RANGE} blocks, of a raid whose objective is
	 * near it; null if none. Only raiders in loaded chunks; blimps (the guns' business) and crews aboard are left out.
	 */
	public static @Nullable LivingEntity threat(ServerLevel level, Townsfolk guard) {
		LivingEntity nearest = null;
		double best = (double) THREAT_RANGE * THREAT_RANGE;
		double reach = (double) RaiderRaids.ABANDON_RANGE * RaiderRaids.ABANDON_RANGE;
		for (RaiderRaids.Raid raid : RaiderRaids.raids(level)) {
			if (raid.objective().distSqr(guard.blockPosition()) > reach) {
				continue;
			}
			for (UUID id : raid.members()) {
				if (!(level.getEntity(id) instanceof LivingEntity raider) || !raider.isAlive() || raider instanceof RaiderBlimp
						|| raider.isPassenger()) {
					continue;
				}
				double d = guard.distanceToSqr(raider);
				if (d < best) {
					best = d;
					nearest = raider;
				}
			}
		}
		return nearest;
	}

	/** The vantages to try against {@code threat}, in order (see the class comment). */
	public static List<BlockPos> candidates(ServerLevel level, Townsfolk guard, LivingEntity threat) {
		List<BlockPos> spots = new ArrayList<>();
		BlockPos origin = Town.origin(level);
		if (origin != null && threat.blockPosition().distSqr(Town.centre(origin)) <= (double) RaiderRaids.ABANDON_RANGE * RaiderRaids.ABANDON_RANGE) {
			CityRaidApproach.Approach attacked = CityRaidApproach.nearest(origin, threat.position());
			CityRaidApproach.Approach gate = attacked.isGate() ? attacked : CityRaidApproach.nearestGate(origin, threat.position());
			var entrance = gate.entrance();
			if (entrance != null) {
				spots.add(entrance.inside());
				spots.add(entrance.passage().offset(-entrance.dx() * 6, 0, -entrance.dz() * 6));
				spots.add(entrance.passage());
				spots.add(entrance.passage().offset(entrance.dx() * 4, 0, entrance.dz() * 4));
				spots.add(gate.front());
			}
			if (!attacked.isGate()) {
				// Round the outside of the wall from the gate's forecourt toward the section, stopping short of it.
				Vec3 centre = Vec3.atBottomCenterOf(Town.centre(origin));
				Vec3 front = Vec3.atBottomCenterOf(attacked.front());
				double radius = Math.sqrt(front.subtract(centre).horizontalDistanceSqr());
				double arc = Math.atan2(Math.sin(attacked.bearing() - gate.bearing()), Math.cos(attacked.bearing() - gate.bearing()));
				int steps = (int) Math.floor(Math.abs(arc) * radius / RING_STEP);
				for (int i = 1; i < steps; i++) {
					double a = gate.bearing() + arc * i / steps;
					spots.add(BlockPos.containing(centre.x + Math.cos(a) * radius, attacked.front().getY(), centre.z + Math.sin(a) * radius));
				}
			}
		}
		Vec3 from = guard.position();
		Vec3 way = threat.position().subtract(from);
		double length = Math.sqrt(way.horizontalDistanceSqr());
		for (int i = 1; i <= STEPS && i * STEP < length - 2; i++) {
			double k = i * STEP / length;
			spots.add(BlockPos.containing(from.x + way.x * k, from.y, from.z + way.z * k));
		}
		// Round the raider, nearest the guard first: the way round a wall the line above runs into (a gap, a gate).
		List<BlockPos> ring = new ArrayList<>();
		for (int radius : new int[] {RING_NEAR, RING_FAR}) {
			for (int k = 0; k < RING_BEARINGS; k++) {
				double a = 2 * Math.PI * k / RING_BEARINGS;
				ring.add(BlockPos.containing(threat.getX() + Math.cos(a) * radius, threat.getY(), threat.getZ() + Math.sin(a) * radius));
			}
		}
		BlockPos at = guard.blockPosition();
		ring.sort(Comparator.comparingDouble(p -> p.distSqr(at)));
		spots.addAll(ring);
		return spots;
	}

	/**
	 * The vantage to make for: the first candidate not set aside that is loaded, has room to stand and has a clear line
	 * to the raider within sight; with none in sight, the usable one nearest the raider; null when none is left.
	 */
	public static @Nullable BlockPos vantage(ServerLevel level, Townsfolk guard, LivingEntity threat, Set<BlockPos> setAside) {
		Vec3 aim = threat.getBoundingBox().getCenter();
		int sight = sight(guard);
		BlockPos fallback = null;
		double fallbackDistance = Double.MAX_VALUE;
		for (BlockPos candidate : candidates(level, guard, threat)) {
			if (setAside.contains(candidate) || !level.isLoaded(candidate)) {
				continue;
			}
			BlockPos stand = stand(level, candidate, guard.getBbHeight());
			if (stand == null || setAside.contains(stand)) {
				continue;
			}
			double toThreat = threat.distanceToSqr(Vec3.atBottomCenterOf(stand));
			if (toThreat < fallbackDistance) {
				fallbackDistance = toThreat;
				fallback = stand;
			}
			Vec3 eye = new Vec3(stand.getX() + 0.5, stand.getY() + guard.getEyeHeight(), stand.getZ() + 0.5);
			if (eye.distanceTo(aim) <= sight && TownDefense.visible(level, guard, eye, aim)) {
				return stand;
			}
		}
		return fallback;
	}

	/**
	 * Where to stand at a candidate (in a loaded chunk): the candidate itself when it has room and ground (a gate's road
	 * under its roof), else the ground the column's height gives, if that is near; null if neither.
	 */
	static @Nullable BlockPos stand(ServerLevel level, BlockPos candidate, float height) {
		if (room(level, candidate, height) && level.getBlockState(candidate.below()).isSolid()) {
			return candidate.immutable();
		}
		BlockPos ground = new BlockPos(candidate.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate.getX(), candidate.getZ()),
				candidate.getZ());
		if (Math.abs(ground.getY() - candidate.getY()) <= 4 && room(level, ground, height) && level.getBlockState(ground.below()).isSolid()
				&& level.getFluidState(ground.below()).isEmpty()) {
			return ground;
		}
		return null;
	}

	private static boolean room(ServerLevel level, BlockPos at, float height) {
		for (int dy = 0; dy < Math.ceil(height); dy++) {
			BlockPos p = at.above(dy);
			if (!level.getBlockState(p).getCollisionShape(level, p).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/** Gives an eligible guard this watch (once), at priority 0. Returns its goal, or null for one not eligible by role. */
	public static @Nullable WatchGoal install(Townsfolk guard) {
		WatchGoal existing = of(guard);
		if (existing != null) {
			return existing;
		}
		if (!guard.isGuard() || guard.role().equals("flak_guard")) {
			return null;
		}
		WatchGoal goal = new WatchGoal(guard);
		guard.getGoalSelector().addGoal(0, goal);
		return goal;
	}

	/** The guard's watch, if it has one. */
	public static @Nullable WatchGoal of(Townsfolk guard) {
		for (var wrapped : guard.getGoalSelector().getAvailableGoals()) {
			if (wrapped.getGoal() instanceof WatchGoal goal) {
				return goal;
			}
		}
		return null;
	}

	/** The guard's goal: see the class comment. */
	public static final class WatchGoal extends Goal {
		private final Townsfolk guard;
		private final Set<BlockPos> setAside = new HashSet<>();
		private @Nullable LivingEntity threat;
		private @Nullable BlockPos vantage;
		private int checkAt;
		private double nearest;
		private int stalls;
		private int moves;
		private int sighted;

		public WatchGoal(Townsfolk guard) {
			this.guard = guard;
			setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		private boolean visibleOpponent() {
			LivingEntity target = guard.getTarget();
			return target != null && target.isAlive() && guard.getSensing().hasLineOfSight(target);
		}

		@Override
		public boolean canUse() {
			if (guard.tickCount < checkAt || !eligible(guard) || visibleOpponent() || guard.isSleeping()
					|| !(guard.level() instanceof ServerLevel level)) {
				return false;
			}
			checkAt = guard.tickCount + CHECK_TICKS;
			threat = CityGuardVantage.threat(level, guard);
			if (threat == null) {
				setAside.clear();   // the raid is over (or out of reach): a fresh start next time
				return false;
			}
			return true;
		}

		@Override
		public boolean canContinueToUse() {
			return threat != null && threat.isAlive() && !visibleOpponent() && eligible(guard);
		}

		@Override
		public void start() {
			vantage = null;
			nearest = Double.MAX_VALUE;
			stalls = 0;
			checkAt = guard.tickCount;
		}

		@Override
		public void stop() {
			guard.getNavigation().stop();
			threat = null;
			vantage = null;
		}

		@Override
		public void tick() {
			if (threat == null || !(guard.level() instanceof ServerLevel level)) {
				return;
			}
			guard.getLookControl().setLookAt(threat, 30.0F, 30.0F);
			if (guard.tickCount < checkAt) {
				return;
			}
			checkAt = guard.tickCount + CHECK_TICKS;
			LivingEntity now = CityGuardVantage.threat(level, guard);
			if (now == null) {
				threat = null;
				return;
			}
			threat = now;
			Vec3 aim = now.getBoundingBox().getCenter();
			Vec3 eye = guard.getEyePosition();
			if (eye.distanceTo(aim) <= sight(guard) && TownDefense.visible(level, guard, eye, aim)) {
				// In sight: hold here, and let its own targeting and weapons take it on.
				guard.getNavigation().stop();
				vantage = null;
				sighted++;
				return;
			}
			if (vantage != null) {
				double gap = guard.distanceToSqr(Vec3.atBottomCenterOf(vantage));
				if (gap < 2.25) {
					setAside.add(vantage);   // there, and still cannot see it: the next
					vantage = null;
				} else if (gap < nearest - 1.0) {
					nearest = gap;
					stalls = 0;
				} else if (++stalls >= STALLS) {
					setAside.add(vantage);   // no nearer (a crowd, a player's wall): the next
					vantage = null;
				}
			}
			if (vantage == null) {
				vantage = CityGuardVantage.vantage(level, guard, now, setAside);
				nearest = Double.MAX_VALUE;
				stalls = 0;
				if (vantage == null) {
					guard.getNavigation().stop();   // every vantage set aside: hold where it stands
					return;
				}
				moves++;
			}
			guard.getNavigation().moveTo(vantage.getX() + 0.5, vantage.getY(), vantage.getZ() + 0.5, 1.1);
		}

		/** The raider it is answering, or null. */
		public @Nullable LivingEntity threat() {
			return threat;
		}

		/** The vantage it is making for, or null (holding, or idle). */
		public @Nullable BlockPos vantage() {
			return vantage;
		}

		/** Vantages chosen so far. */
		public int moves() {
			return moves;
		}

		/** Checks at which it had the raider in sight. */
		public int sighted() {
			return sighted;
		}

		/** Vantages set aside for this raid. */
		public int setAside() {
			return setAside.size();
		}
	}
}
