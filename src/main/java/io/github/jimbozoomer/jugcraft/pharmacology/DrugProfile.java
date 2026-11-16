package io.github.jimbozoomer.jugcraft.pharmacology;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

/** A fixed game formulation. allowRedose is retained authoring metadata; replacement policy lives in Pharmacology. */
public record DrugProfile(String id, String family, int durationSeconds, int onsetSeconds, int strain,
		Holder<MobEffect> marker, Benefits benefits, int recoverySeconds, boolean allowRedose, Traits traits) {
	public DrugProfile(String id, String family, int durationSeconds, int onsetSeconds, int strain,
			Holder<MobEffect> marker, Benefits benefits, int recoverySeconds, boolean allowRedose) {
		this(id, family, durationSeconds, onsetSeconds, strain, marker, benefits, recoverySeconds, allowRedose, Traits.NONE);
	}
	public DrugProfile(String id, String family, int durationSeconds, int onsetSeconds, int strain,
			Holder<MobEffect> marker, Benefits benefits) {
		this(id, family, durationSeconds, onsetSeconds, strain, marker, benefits, Pharmacology.RECOVERY_SECONDS, true);
	}
	public DrugProfile {
		if (id.isBlank() || family.isBlank() || durationSeconds < 1 || durationSeconds > 3600
				|| onsetSeconds < 0 || onsetSeconds > 60 || strain < 0 || strain > Pharmacology.MAX_STRAIN
				|| marker == null || benefits == null || traits == null || recoverySeconds < 1 || recoverySeconds > 3600) {
			throw new IllegalArgumentException("Invalid drug formulation: " + id);
		}
	}

	/** Native amplifiers use -1 for absent. Passive bonuses take the best value rather than adding together. */
	public record Benefits(int haste, int speed, double walking, float healing, float exhaustion, float damage, Extra extra) {
		public Benefits(int haste, int speed, double walking, float healing, float exhaustion, float damage) {
			this(haste, speed, walking, healing, exhaustion, damage, Extra.NONE);
		}
		public Benefits {
			if (haste < -1 || haste > 4 || speed < -1 || speed > 4 || !Double.isFinite(walking)
					|| walking < 0 || walking > 1 || !Float.isFinite(healing) || healing < 1 || healing > 2
					|| !Float.isFinite(exhaustion) || exhaustion < .25F || exhaustion > 1
					|| !Float.isFinite(damage) || damage < .5F || damage > 1 || extra == null) {
				throw new IllegalArgumentException("Invalid drug benefits");
			}
		}
	}

	public record Extra(int strength, int absorptionHearts, double knockback, float sprint, float resistance) {
		public static final Extra NONE = new Extra(-1, 0, 0, 1, 1);
		public Extra {
			if (strength < -1 || strength > 4 || absorptionHearts < 0 || absorptionHearts > 10
					|| !Double.isFinite(knockback) || knockback < 0 || knockback > 1
					|| !Float.isFinite(sprint) || sprint < .25F || sprint > 1
					|| !Float.isFinite(resistance) || resistance < .5F || resistance > 1) {
				throw new IllegalArgumentException("Invalid additional drug benefits");
			}
		}
	}

	public record Traits(double melee, double movementPenalty, float activityCost, float spread, float aimSway, int fatigueSeconds) {
		public static final Traits NONE = new Traits(0, 0, 1, 1, 1, 0);
		public Traits {
			if (!Double.isFinite(melee) || melee < 0 || melee > .5 || !Double.isFinite(movementPenalty) || movementPenalty < 0 || movementPenalty > .5
					|| !Float.isFinite(activityCost) || activityCost < 1 || activityCost > 2 || !Float.isFinite(spread) || spread < 1 || spread > 2
					|| !Float.isFinite(aimSway) || aimSway < .5 || aimSway > 1 || fatigueSeconds < 0 || fatigueSeconds > 180) throw new IllegalArgumentException("Invalid drug traits");
		}
	}
}
