package io.github.jimbozoomer.jugcraft.raiders;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * The grenadier's fight, as vanilla's ranged attack always ran it for the grenadier: it closes until it has seen its
 * target for {@value #SIGHTED} ticks from within {@code radius} blocks, then throws every {@code interval} ticks while
 * it sees it. Only how it closes differs: through {@link RaidEngagement#close}, so a grenadier of a raid that cannot walk
 * up to its target makes for a standoff in sight of it, or gives it up, rather than pressing against the foot of a wall.
 */
final class RaiderThrowGoal extends Goal {
	/** Ticks it must have seen its target before it stops closing in. */
	private static final int SIGHTED = 5;
	private final RaiderInfantry mob;
	private final double speed;
	private final int interval;
	private final float radius;
	private int seeTime;
	private int attackTime = -1;

	RaiderThrowGoal(RaiderInfantry mob, double speed, int interval, float radius) {
		this.mob = mob;
		this.speed = speed;
		this.interval = interval;
		this.radius = radius;
		setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity target = mob.getTarget();
		return target != null && target.isAlive();
	}

	@Override
	public boolean canContinueToUse() {
		return canUse();
	}

	@Override
	public void stop() {
		seeTime = 0;
		attackTime = -1;
		mob.getNavigation().stop();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		LivingEntity target = mob.getTarget();
		if (target == null) {
			return;
		}
		double distance = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
		boolean sees = mob.getSensing().hasLineOfSight(target);
		seeTime = sees ? seeTime + 1 : 0;
		if (distance <= (double) radius * radius && seeTime >= SIGHTED) {
			mob.getNavigation().stop();
		} else {
			mob.engagement().close(target, speed, radius);
		}
		mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
		if (--attackTime == 0) {
			if (!sees) {
				return;
			}
			mob.performRangedAttack(target, Mth.clamp((float) Math.sqrt(distance) / radius, 0.1F, 1.0F));
			attackTime = interval;
		} else if (attackTime < 0) {
			attackTime = interval;
		}
	}
}
