package io.github.jimbozoomer.jugcraft.raiders;

import io.github.jimbozoomer.jugcraft.artillery.CrewedGun;
import io.github.jimbozoomer.jugcraft.guns.GunItem;
import io.github.jimbozoomer.jugcraft.guns.MobGuns;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * What a raider of a raid remembers of its fight (docs/features/raiders.md, "City approaches and visible fights"), so
 * that a raid becomes a fight the defenders can see rather than a pile of raiders under a corner tower.
 *
 * <ul>
 * <li><b>Who it takes on.</b> On top of the guard-only rule ({@link RaiderTargets}), a raider on the ground leaves the
 * tower guns' crews to the blimps and takes on no defender standing more than {@value #ELEVATED} blocks above it (on a
 * wall walk or a tower): it fights the guards on the ground, at the gates and in the streets. A blimp may take on any
 * guard. Anything within striking distance ({@value #STRIKING} blocks) may be fought regardless.</li>
 * <li><b>Stuck-target recovery, bounded.</b> A raider that has neither got a block nearer its target nor been able to
 * strike or shoot at it (in reach and in sight) for {@value #PATIENCE} ticks ({@value #BLIMP_PATIENCE} for a blimp, which
 * climbs over what is in its way first) gives that target up and leaves it alone for {@value #IGNORE_TICKS} ticks, and
 * so marches on to its approach. A ranged raider that cannot walk up to its target tries up to {@value #STANDOFF_TRIES}
 * standoffs in sight of it ({@link RaidStandoff}) before giving it up.</li>
 * <li><b>Approaches given up</b> ({@link MarchGoal}): which of the town's approaches failed it, so a fresh plan tries
 * another, and whether it has given up for good and holds where it is.</li>
 * </ul>
 *
 * <p>Only raiders that belong to a raid follow these rules; a summoned raider or a camp's garrison fights as it always
 * did. None of it is saved: a raider loaded again starts afresh, which only restarts its bounded recovery. Nothing here
 * edits a block or loads a chunk.
 */
public final class RaidEngagement {
	/** How much higher than a ground raider a defender may stand and still be taken on from the ground, in blocks. */
	public static final double ELEVATED = 4.5;
	/** Within this many blocks a raider fights back whatever the rules, given up or not. */
	public static final double STRIKING = 3.0;
	/** Ticks a target may be kept without getting nearer it or engaging it before it is given up. */
	public static final int PATIENCE = 200;
	/** The same for a blimp, which first climbs over what is in its way (RaiderBlimp). */
	public static final int BLIMP_PATIENCE = 300;
	/** Ticks a given-up target is left alone. */
	public static final int IGNORE_TICKS = 600;
	/** Standoffs a raider tries against one target it cannot walk up to before giving it up. */
	public static final int STANDOFF_TRIES = 3;
	/** Ticks between looks at how the fight is going. */
	static final int WATCH_TICKS = 10;
	/** Ticks between plans of a ranged raider's way to its target. */
	static final int PLAN_TICKS = 20;
	/** Plans without getting nearer a standoff before trying another. */
	static final int STANDOFF_STALLS = 3;
	/** Given-up targets remembered before the expired ones are forgotten. */
	private static final int MEMORY = 16;

	private final Mob mob;
	private final Map<UUID, Integer> ignored = new HashMap<>();
	private final Set<Integer> failedApproaches = new HashSet<>();
	private @Nullable UUID watching;
	private double best;
	private int lastProgress;
	private @Nullable UUID plannedFor;
	private int planAt;
	private boolean reachable;
	private @Nullable Vec3 spot;
	private double spotNearest;
	private int spotStalls;
	private int tries;
	private int targetsGivenUp;
	private int approachesGivenUp;
	private int standoffs;
	private boolean holding;
	private @Nullable UUID meleePathTarget;
	private int meleePathCheckAt;
	private boolean meleePathReachable;

	public RaidEngagement(Mob mob) {
		this.mob = mob;
	}

	/** Whether {@code mob} is a raider of a raid, and so fights by these rules. */
	public static boolean inRaid(Entity mob) {
		return mob instanceof Raider raider && raider.raid() != null;
	}

	private boolean ground() {
		return !(mob instanceof RaiderBlimp);
	}

	/** Whether the raider may take {@code target} on (on top of {@link RaiderTargets#allowed}); see the class comment. */
	public boolean allows(LivingEntity target) {
		if (!inRaid(mob)) {
			return true;
		}
		if (mob.distanceToSqr(target) <= STRIKING * STRIKING) {
			return true;
		}
		Integer until = ignored.get(target.getUUID());
		if (until != null) {
			if (mob.tickCount < until) {
				return false;
			}
			ignored.remove(target.getUUID());
		}
		// The tower guns are the blimps' business: a ground raider takes on the defenders on the ground.
		return !ground() || !(target.getVehicle() instanceof CrewedGun) && target.getY() - mob.getY() <= ELEVATED;
	}

	/** Target acquisition also covers retaliation alerts, not only nearest-target searches. */
	public boolean canAcquire(LivingEntity target) {
		if (!inRaid(mob) || mob.distanceToSqr(target) <= STRIKING * STRIKING) return true;
		// Ally retaliation alerts bypass the nearest-target goal's mustReach check.
		// Apply the rule here too, without repeatedly searching for the same target.
		if (mob instanceof RaiderInfantry infantry && (infantry.role() == RaiderInfantry.Role.GRUNT || infantry.role() == RaiderInfantry.Role.OFFICER)) {
			if (!target.getUUID().equals(meleePathTarget) || mob.tickCount >= meleePathCheckAt) {
				meleePathTarget = target.getUUID();
				meleePathCheckAt = mob.tickCount + PLAN_TICKS;
				Path path = mob.getNavigation().createPath(target, 0);
				// A target-goal probe updates navigation's targetPos without replacing
				// its active march path. A subsequent probe can return that old path;
				// reaching the march objective does not prove this defender reachable.
				meleePathReachable = path != null && path.canReach()
						&& path.getTarget().equals(target.blockPosition());
			}
			return meleePathReachable;
		}
		return true;
	}

	/** Whether the raider is leaving {@code target} alone for now, having given it up. */
	public boolean ignoring(Entity target) {
		Integer until = ignored.get(target.getUUID());
		return until != null && mob.tickCount < until;
	}

	/** Called each server tick by the raider: gives up a target it has kept too long for nothing. */
	public void tick() {
		if (!inRaid(mob) || mob.tickCount % WATCH_TICKS != 0) {
			return;
		}
		LivingEntity target = mob.getTarget();
		if (target == null || !target.isAlive()) {
			watching = null;
			return;
		}
		double distance = progress(target);
		if (!target.getUUID().equals(watching)) {
			watching = target.getUUID();
			best = distance;
			lastProgress = mob.tickCount;
			return;
		}
		if (distance < best - 1.0 || engaging(target)) {
			best = Math.min(best, distance);
			lastProgress = mob.tickCount;
		} else if (mob.tickCount - lastProgress >= (ground() ? PATIENCE : BLIMP_PATIENCE)) {
			giveUp(target);
		}
	}

	/** How far it still has to go: a blimp measures how near overhead it is, the rest how near they are. */
	private double progress(LivingEntity target) {
		if (ground()) {
			return mob.distanceTo(target);
		}
		double dx = target.getX() - mob.getX();
		double dz = target.getZ() - mob.getZ();
		return Math.sqrt(dx * dx + dz * dz);
	}

	/** Whether it can strike, shoot or bomb its target from where it is now (in reach and, on the ground, in sight). */
	public boolean engaging(LivingEntity target) {
		double reach = reach();
		if (!ground()) {
			return progress(target) <= reach;
		}
		return mob.distanceToSqr(target) <= reach * reach && mob.getSensing().hasLineOfSight(target);
	}

	/** How far the raider's weapon reaches, in blocks. */
	public double reach() {
		if (mob instanceof RaiderWalker) {
			return JugcraftRaiders.WALKER_LAUNCH_MAX;
		}
		if (mob instanceof RaiderInfantry infantry) {
			return switch (infantry.role()) {
				case GRENADIER -> JugcraftRaiders.GRENADE_MAX_RANGE;
				case GUNNER -> infantry.getMainHandItem().getItem() instanceof GunItem gun ? MobGuns.reach(gun) : STRIKING;
				default -> STRIKING;
			};
		}
		return JugcraftRaiders.BLIMP_BOMB_REACH * 2.0;
	}

	/** Gives up {@code target}: it is left alone for {@value #IGNORE_TICKS} ticks (a raider of no raid never gives up). */
	public void giveUp(LivingEntity target) {
		if (!inRaid(mob)) {
			return;
		}
		if (ignored.size() >= MEMORY) {
			ignored.entrySet().removeIf(e -> e.getValue() <= mob.tickCount);
		}
		ignored.put(target.getUUID(), mob.tickCount + IGNORE_TICKS);
		targetsGivenUp++;
		watching = null;
		plannedFor = null;
		spot = null;
		tries = 0;
		if (mob.getTarget() == target) {
			mob.setTarget(null);
		}
		mob.getNavigation().stop();
	}

	/**
	 * Moves a ranged raider toward where it can shoot {@code target} from, called while it is out of reach or out of
	 * sight: straight at it when it can walk there, otherwise to a standoff in sight within {@code reach}
	 * ({@link RaidStandoff}), trying another when it gets no nearer one, and giving the target up after
	 * {@value #STANDOFF_TRIES}. A raider of no raid walks straight at its target, as it always did.
	 */
	public void close(LivingEntity target, double speed, double reach) {
		if (!inRaid(mob)) {
			mob.getNavigation().moveTo(target, speed);
			return;
		}
		boolean fresh = !target.getUUID().equals(plannedFor);
		if (!fresh && mob.tickCount < planAt) {
			// Between plans: keep on the way chosen.
			if (reachable) {
				mob.getNavigation().moveTo(target, speed);
			} else if (spot != null && mob.getNavigation().isDone()) {
				mob.getNavigation().moveTo(spot.x, spot.y, spot.z, speed);
			}
			return;
		}
		if (fresh) {
			plannedFor = target.getUUID();
			tries = 0;
			spot = null;
		}
		planAt = mob.tickCount + PLAN_TICKS;
		Path path = mob.getNavigation().createPath(target, 0);
		reachable = path != null && path.canReach();
		if (reachable) {
			spot = null;
			mob.getNavigation().moveTo(path, speed);
			return;
		}
		if (spot != null) {
			double gap = mob.position().distanceToSqr(spot);
			if (gap < spotNearest - 1.0) {
				spotNearest = gap;
				spotStalls = 0;
			} else if (++spotStalls >= STANDOFF_STALLS) {
				spot = null;
			}
		}
		if (spot == null) {
			if (tries >= STANDOFF_TRIES) {
				giveUp(target);
				return;
			}
			spot = RaidStandoff.spot(mob, target, Math.min(4.0, reach / 2.0), Math.max(2.0, reach - 1.0), tries++);
			if (spot == null) {
				giveUp(target);
				return;
			}
			standoffs++;
			spotNearest = Double.MAX_VALUE;
			spotStalls = 0;
		}
		mob.getNavigation().moveTo(spot.x, spot.y, spot.z, speed);
	}

	/**
	 * The next standoff to try against {@code target} ({@link RaidStandoff}, further round each try), or null when there
	 * is none: after {@value #STANDOFF_TRIES} tries a raider of a raid gives the target up, while one of no raid starts
	 * round again, as it always did.
	 */
	public @Nullable Vec3 nextStandoff(LivingEntity target, double near, double far) {
		if (!target.getUUID().equals(plannedFor)) {
			plannedFor = target.getUUID();
			tries = 0;
		}
		if (tries >= STANDOFF_TRIES) {
			if (inRaid(mob)) {
				giveUp(target);
				return null;
			}
			tries = 0;
		}
		Vec3 found = RaidStandoff.spot(mob, target, near, far, tries++);
		if (found != null) {
			standoffs++;
		} else if (tries >= STANDOFF_TRIES && inRaid(mob)) {
			giveUp(target);
		}
		return found;
	}

	/** The approaches (CityRaidApproach indices) that have failed this raider. */
	Set<Integer> failedApproaches() {
		return failedApproaches;
	}

	/** An approach has failed the raider: a fresh plan tries another. */
	void approachFailed(int index) {
		failedApproaches.add(index);
		approachesGivenUp++;
	}

	/** It has given up marching for good, and holds where it is. */
	void hold() {
		holding = true;
	}

	/** A new objective: what failed on the way to the old one may serve for this. */
	void newObjective() {
		failedApproaches.clear();
		holding = false;
	}

	/** Targets given up so far (stuck-target recoveries). */
	public int targetsGivenUp() {
		return targetsGivenUp;
	}

	/** Approaches given up so far. */
	public int approachesGivenUp() {
		return approachesGivenUp;
	}

	/** Standoff spots found so far. */
	public int standoffs() {
		return standoffs;
	}

	/** Whether it has given up marching for good and holds where it is. */
	public boolean holding() {
		return holding;
	}

	/** How many times it has been stuck: targets and approaches given up. */
	public int stuck() {
		return targetsGivenUp + approachesGivenUp;
	}
}
