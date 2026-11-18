package io.github.jimbozoomer.jugcraft.world.climate;

/**
 * Which climate belt a place is in: pure, deterministic band and transition logic (contract section 1 as amended by
 * amendment 4). No Minecraft types except the profile overload, which only reads {@link ClimateProfile#geometry()}.
 *
 * <p>With {@code s = blockZ - anchor.z} (south positive, north is -Z) and {@code n = -s}, and T the transition:
 * <ul>
 * <li>{@code n >= north_frigid} or {@code s >= south_frigid}: FRIGID, always (strict; no blend, no exception).</li>
 * <li>{@code north_frigid - T <= n < north_frigid}: blend of CENTRAL into FRIGID, entirely on the inner side.</li>
 * <li>{@code south_frigid - T <= s < south_frigid}: blend of TEMPERATE into FRIGID, entirely on the inner side.</li>
 * <li>{@code 2h - T <= 2s < 2h + T} for {@code h = south_hot_start}: blend of CENTRAL into HOT, centred on h; and the
 * same for {@code h = south_hot_end}: HOT into TEMPERATE. Doubled integers, so odd T needs no rounding.</li>
 * <li>Otherwise CENTRAL for {@code s < south_hot_start}, HOT below {@code south_hot_end}, TEMPERATE below
 * {@code south_frigid}.</li>
 * </ul>
 * The rules ({@link ClimateRules}) keep every blend apart from the others and from the strict limits. With T = 0 there
 * are no blends.
 *
 * <p>Inside a blend the outer zone's probability rises across the band by a smoothstep of the band fraction. Each
 * place compares that probability with coherent two-octave value noise of its own x/z ({@link ClimateNoise}), so the
 * front between the two zones is a natural, deterministic fringe of patches about 20 to 60 blocks across, not per-quart
 * salt and pepper. X only adds this variety: the belts themselves run infinitely east and west.
 */
public final class ClimateBands {
	/** Blend edge numbers, which also select the noise stream of each edge. */
	static final int NORTH_FRIGID_EDGE = 0, HOT_START_EDGE = 1, HOT_END_EDGE = 2, SOUTH_FRIGID_EDGE = 3;

	private ClimateBands() {
	}

	/** The zone of a block column of a world with this profile (contract API). */
	public static ClimateZone zone(ClimateProfile profile, int blockX, int blockZ) {
		return zone(profile.geometry(), blockX, blockZ);
	}

	/** The zone of a block column, block-precise. */
	public static ClimateZone zone(ClimateGeometry g, int blockX, int blockZ) {
		long s = (long) blockZ - g.anchor().z();
		long n = -s;
		int t = g.transition();
		if (n >= g.northFrigid() || s >= g.southFrigid()) {
			return ClimateZone.FRIGID;
		}
		if (n >= g.northFrigid() - t) {
			double fraction = (n - (g.northFrigid() - t) + 0.5) / t;
			return choose(g, NORTH_FRIGID_EDGE, blockX, blockZ, fraction, ClimateZone.CENTRAL, ClimateZone.FRIGID);
		}
		if (s >= g.southFrigid() - t) {
			double fraction = (s - (g.southFrigid() - t) + 0.5) / t;
			return choose(g, SOUTH_FRIGID_EDGE, blockX, blockZ, fraction, ClimateZone.TEMPERATE, ClimateZone.FRIGID);
		}
		long s2 = 2 * s;
		if (centred(s2, g.southHotStart(), t)) {
			double fraction = (s2 - (2L * g.southHotStart() - t) + 1) / (2.0 * t);
			return choose(g, HOT_START_EDGE, blockX, blockZ, fraction, ClimateZone.CENTRAL, ClimateZone.HOT);
		}
		if (centred(s2, g.southHotEnd(), t)) {
			double fraction = (s2 - (2L * g.southHotEnd() - t) + 1) / (2.0 * t);
			return choose(g, HOT_END_EDGE, blockX, blockZ, fraction, ClimateZone.HOT, ClimateZone.TEMPERATE);
		}
		if (s < g.southHotStart()) {
			return ClimateZone.CENTRAL;
		}
		return s < g.southHotEnd() ? ClimateZone.HOT : ClimateZone.TEMPERATE;
	}

	/**
	 * The zone a stored biome quart takes. It is {@link #zone} at the quart's corner block {@code (4qx, 4qz)}, except that
	 * a quart any block at or beyond a strict frigid limit can read is FRIGID. Minecraft's biome lookup for a block
	 * ({@code BiomeManager}) fuzzes between the quarts round it: block z reads quart {@code (z-2)>>2} or the next, so
	 * quart q is read by blocks {@code 4q-2} to {@code 4q+5}. Making such quarts frigid keeps the limits strict at block
	 * level too: no block at or beyond a limit can show a non-frigid biome. This moves the edge by at most five blocks
	 * inward, inside the inner-side blend.
	 */
	public static ClimateZone quartZone(ClimateGeometry g, int quartX, int quartZ) {
		long lowestReader = ((long) quartZ << 2) - 2, highestReader = ((long) quartZ << 2) + 5;
		long az = g.anchor().z();
		if (az - lowestReader >= g.northFrigid() || highestReader - az >= g.southFrigid()) {
			return ClimateZone.FRIGID;
		}
		return zone(g, quartX << 2, quartZ << 2);
	}

	/** The outer zone's chance at a band fraction in [0, 1]: a smoothstep, so 0 at the inner edge, 1 at the outer. */
	public static double outerProbability(double fraction) {
		double t = Math.clamp(fraction, 0.0, 1.0);
		return t * t * (3 - 2 * t);
	}

	/** Whether doubled offset {@code s2 = 2s} lies in the centred blend of threshold h: {@code 2h - T <= 2s < 2h + T}. */
	static boolean centred(long s2, int h, int t) {
		return s2 >= 2L * h - t && s2 < 2L * h + t;
	}

	private static ClimateZone choose(ClimateGeometry g, int edge, int blockX, int blockZ, double fraction, ClimateZone inner,
			ClimateZone outer) {
		return ClimateNoise.blend(g.salt(), edge, blockX, blockZ) < outerProbability(fraction) ? outer : inner;
	}
}
