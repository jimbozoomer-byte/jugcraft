package io.github.jimbozoomer.jugcraft.concordance.ecology;

/**
 * One reading of a habitat: the five {@link Factor}s at one plant, and the strain its bed's harvests left. Moisture and
 * nutrients are the bed's own, light is read at the plant, and diversity and disturbance come from the area round it
 * ({@link Sampler}). The strain (Concordance Chunk C, C-08, C-P17, C-P59) is not a sixth factor: {@link Verdict#of} counts it
 * against a plant's disturbance limit only ({@link #strained}), never as disturbance a plant needs.
 * <p>
 * Concordance garden cohort, GC-09 (GC-S21; docs/features/amdusias-pact.md): nor is a canopy's shelter. {@link #sheltered}
 * lowers the area's disturbance the plant reads by up to a damping, never below 0, and keeps what it took in
 * {@link #shelter}; {@link #strained} still counts the strain against the unsheltered area ({@link #unsheltered}), so a
 * canopy never eases harvest strain (Verdict.of judges the worse of the two). Every reading the earlier constructors make
 * has no shelter, so every habitat read without a canopy is what it was.
 */
public record Habitat(int moisture, int light, int nutrients, int diversity, int disturbance, int strain, int shelter) {
	/** The most nutrients a bed holds. Keep equal to tools/concordance_ecology.py. */
	public static final int MAX_NUTRIENTS = 32;
	/** The most strain harvests leave on a bed (C-P17). Keep equal to tools/concordance_ecology.py. */
	public static final int MAX_STRAIN = 3;

	public Habitat {
		check(Factor.MOISTURE, moisture);
		check(Factor.LIGHT, light);
		check(Factor.NUTRIENTS, nutrients);
		check(Factor.DIVERSITY, diversity);
		check(Factor.DISTURBANCE, disturbance);
		if (strain < 0 || strain > MAX_STRAIN) {
			throw new IllegalArgumentException("strain " + strain + " outside 0.." + MAX_STRAIN);
		}
		// GC-09: what a canopy took is never more than the area held, so the unsheltered disturbance is still a reading.
		if (shelter < 0 || disturbance + shelter > Factor.DISTURBANCE.max) {
			throw new IllegalArgumentException("shelter " + shelter + " outside 0.." + (Factor.DISTURBANCE.max - disturbance));
		}
	}

	/** A reading no canopy shelters (every reading before the garden cohort's GC-09). */
	public Habitat(int moisture, int light, int nutrients, int diversity, int disturbance, int strain) {
		this(moisture, light, nutrients, diversity, disturbance, strain, 0);
	}

	/** A reading of a bed no harvest has strained. */
	public Habitat(int moisture, int light, int nutrients, int diversity, int disturbance) {
		this(moisture, light, nutrients, diversity, disturbance, 0, 0);
	}

	private static void check(Factor factor, int value) {
		if (value < 0 || value > factor.max) {
			throw new IllegalArgumentException(factor.id + " " + value + " outside 0.." + factor.max);
		}
	}

	public int get(Factor factor) {
		return switch (factor) {
			case MOISTURE -> moisture;
			case LIGHT -> light;
			case NUTRIENTS -> nutrients;
			case DIVERSITY -> diversity;
			case DISTURBANCE -> disturbance;
		};
	}

	/**
	 * The disturbance with the strain counted against the plant, within the factor's range. GC-09: the unsheltered area's,
	 * so no canopy eases it.
	 */
	public int strained() {
		return Math.min(disturbance + shelter + strain, Factor.DISTURBANCE.max);
	}

	/** GC-09: the area's disturbance before any canopy took its share (the reading's own when none did). */
	public int unsheltered() {
		return disturbance + shelter;
	}

	/**
	 * This reading with one factor changed (clamped to its range); the strain stays, and (GC-09) the shelter, at most what
	 * the new disturbance leaves room for.
	 */
	public Habitat with(Factor factor, int value) {
		int v = Math.clamp(value, 0, factor.max);
		int kept = factor == Factor.DISTURBANCE ? Math.min(shelter, Factor.DISTURBANCE.max - v) : shelter;
		return new Habitat(factor == Factor.MOISTURE ? v : moisture, factor == Factor.LIGHT ? v : light,
				factor == Factor.NUTRIENTS ? v : nutrients, factor == Factor.DIVERSITY ? v : diversity,
				factor == Factor.DISTURBANCE ? v : disturbance, strain, kept);
	}

	/** GC-09: this reading with its strain changed (clamped to 0..MAX_STRAIN); every factor and the shelter stay. */
	public Habitat withStrain(int value) {
		return new Habitat(moisture, light, nutrients, diversity, disturbance, Math.clamp(value, 0, MAX_STRAIN), shelter);
	}

	/**
	 * GC-09 (GC-S21, GC-P122): this reading under a canopy that damps by {@code damping}: the area's disturbance lowered by
	 * up to that much, never below 0, what it took kept in shelter; every other factor and the strain unchanged, and
	 * strained() unchanged. A sheltered reading sheltered again takes the larger damping from its unsheltered area, never the
	 * sum, so two canopies never stack. A negative damping is refused; 0 changes nothing.
	 */
	public Habitat sheltered(int damping) {
		if (damping < 0) {
			throw new IllegalArgumentException("damping " + damping + " below 0");
		}
		int area = disturbance + shelter;
		int taken = Math.min(area, Math.max(shelter, damping));
		return new Habitat(moisture, light, nutrients, diversity, area - taken, strain, taken);
	}
}
