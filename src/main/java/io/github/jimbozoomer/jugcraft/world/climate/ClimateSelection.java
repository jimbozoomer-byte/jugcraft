package io.github.jimbozoomer.jugcraft.world.climate;

/**
 * Deterministic, bounded choice of one pool member. Pure: no Minecraft types and no state.
 *
 * <p>A pool's members are in its tag's order, which the default tags keep from driest to wettest. The vanilla
 * humidity at the place, which the wrapped source used to pick its own biome, picks a humidity group of contiguous
 * members ({@link #GROUPS} at most); a hash of the base biome's ID, the salt, the zone and the category then picks one
 * member of that group. So a dry base climate becomes a dry member and a wet one a wet member, and each replacement
 * patch follows the base biome's own patch and vanilla's humidity contours: coherent at vanilla's scale, with
 * different base biomes giving different members side by side.
 */
public final class ClimateSelection {
	/** The most humidity groups a pool is split into. */
	public static final int GROUPS = 4;
	/** Vanilla's Overworld humidity band edges (OverworldBiomeBuilder; tools/biomes.py HUMIDITY_BANDS). */
	private static final double[] EDGES = {-1.0, -0.35, -0.1, 0.1, 0.3, 1.0};

	private ClimateSelection() {
	}

	/**
	 * Unquantized humidity (about -1 to 1) as a rank in [0, 1): each of vanilla's five humidity bands gets a fifth,
	 * linearly inside it, so the groups are about equally common. Out-of-range and non-finite input is clamped.
	 */
	public static double humidityRank(double humidity) {
		double h = Double.isNaN(humidity) ? 0 : Math.clamp(humidity, EDGES[0], EDGES[EDGES.length - 1]);
		int band = 0;
		while (band < EDGES.length - 2 && h >= EDGES[band + 1]) {
			band++;
		}
		double rank = (band + (h - EDGES[band]) / (EDGES[band + 1] - EDGES[band])) / (EDGES.length - 1);
		return Math.min(rank, Math.nextDown(1.0));
	}

	/** How many humidity groups a pool of {@code size} members has. */
	public static int groups(int size) {
		return Math.max(1, Math.min(size, GROUPS));
	}

	/** The humidity group in [0, groups(size)) for a humidity. */
	public static int group(int size, double humidity) {
		int groups = groups(size);
		return Math.min(groups - 1, (int) (humidityRank(humidity) * groups));
	}

	/** The first member index of group {@code g}; group g holds {@code [start(g), start(g + 1))}. */
	public static int start(int size, int g) {
		return (int) ((long) g * size / groups(size));
	}

	/**
	 * The member index in [0, size) for a place with this humidity whose base biome's ID hashes to {@code baseHash}.
	 * {@code zone} and {@code category} are the ordinals, so pools of different zones decorrelate.
	 */
	public static int index(int size, double humidity, int baseHash, int salt, int zone, int category) {
		if (size <= 0) {
			throw new IllegalArgumentException("Cannot choose from an empty pool");
		}
		int g = group(size, humidity);
		int lo = start(size, g), hi = start(size, g + 1);
		int pick = ClimateNoise.hash(salt, 0x100 + zone * 16 + category, baseHash, g);
		return lo + Math.floorMod(pick, hi - lo);
	}
}
