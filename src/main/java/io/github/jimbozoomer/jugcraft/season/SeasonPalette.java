package io.github.jimbozoomer.jugcraft.season;

/**
 * The seasonal colour maths, kept free of client classes so server game tests can check it; the client applies it
 * to grass and foliage tints ({@code client/SeasonColors}). Keyframes through the year say how far each tint moves
 * from its vanilla colour towards a seasonal one; between keyframes the result blends smoothly. Summer (day 196) is
 * vanilla. Autumn foliage mixes gold, orange and red in patches about 24 blocks across, with smaller speckles, so
 * a forest turns in patches rather than one flat colour.
 */
public final class SeasonPalette {
	/**
	 * One point in the year: the foliage colour aimed for and how far towards it (0 vanilla, 1 fully), how much of
	 * the patchy autumn hue it mixes into that colour (0 none, 1 all), then grass the same way.
	 */
	public record Keyframe(int day, int foliage, float foliageStrength, float variety, int grass, float grassStrength) {
	}

	/** In day order; the year wraps from the last back to the first. */
	public static final Keyframe[] YEAR = {
			new Keyframe(15, 0x7d6f4a, 0.55F, 0.0F, 0x94946a, 0.50F), // midwinter: dormant, olive-brown
			new Keyframe(75, 0x6f8a3c, 0.25F, 0.0F, 0x8aa05a, 0.25F), // mid-March: first green
			new Keyframe(120, 0x6fd23a, 0.30F, 0.0F, 0x7cd44e, 0.30F), // spring: fresh, bright green
			new Keyframe(196, 0x000000, 0.0F, 0.0F, 0x000000, 0.0F), // midsummer: vanilla
			new Keyframe(244, 0xb8b23a, 0.35F, 0.3F, 0xa7b04a, 0.20F), // September: first yellowing
			new Keyframe(293, 0xd47a2a, 0.80F, 1.0F, 0xb3a24e, 0.40F), // 20 October: peak autumn
			new Keyframe(330, 0x8e5a2c, 0.70F, 0.4F, 0xa08e58, 0.45F), // late November: russet and bare
	};

	public static final int GOLD = 0xd8b032;
	public static final int ORANGE = 0xd9792a;
	public static final int RED = 0xb8402a;
	/** Blocks across an autumn colour patch, and across the smaller speckles inside it. */
	public static final double PATCH = 24.0;
	public static final double SPECKLE = 7.0;

	private SeasonPalette() {
	}

	/**
	 * The seasonal tint at block (x, z) on season day {@code day} (1 to 365; 0 or less leaves {@code vanilla} as it
	 * is), for a foliage or a grass tint whose vanilla colour is {@code vanilla}. The vanilla alpha byte is kept.
	 */
	public static int colour(int day, int vanilla, boolean foliage, double x, double z) {
		if (day <= 0 || day > SeasonCalendar.DAYS) {
			return vanilla;
		}
		Keyframe from = YEAR[YEAR.length - 1];
		int fromDay = from.day() - SeasonCalendar.DAYS;
		Keyframe to = YEAR[0];
		int toDay = to.day();
		for (int i = 0; i < YEAR.length && YEAR[i].day() <= day; i++) {
			from = YEAR[i];
			fromDay = from.day();
			to = YEAR[(i + 1) % YEAR.length];
			toDay = i + 1 < YEAR.length ? to.day() : to.day() + SeasonCalendar.DAYS;
		}
		float t = (float) (day - fromDay) / (toDay - fromDay);
		t = t * t * (3.0F - 2.0F * t);
		return mix(at(from, vanilla, foliage, x, z), at(to, vanilla, foliage, x, z), t);
	}

	/** The tint a single keyframe gives. */
	private static int at(Keyframe key, int vanilla, boolean foliage, double x, double z) {
		if (!foliage) {
			return mix(vanilla, key.grass(), key.grassStrength());
		}
		int target = key.variety() > 0.0F ? mix(key.foliage(), autumnHue(x, z), key.variety()) : key.foliage();
		return mix(vanilla, target, key.foliageStrength());
	}

	/** Gold, orange or red (or between), in patches with speckles, the same at a position every time. */
	public static int autumnHue(double x, double z) {
		double n = 0.7 * valueNoise(x / PATCH, z / PATCH, 0) + 0.3 * valueNoise(x / SPECKLE, z / SPECKLE, 1);
		// Averaged noise bunches around one half; stretch it so all three colours show.
		n = Math.clamp((n - 0.5) * 1.8 + 0.5, 0.0, 1.0);
		return n < 0.5 ? mix(GOLD, ORANGE, (float) (n * 2.0)) : mix(ORANGE, RED, (float) ((n - 0.5) * 2.0));
	}

	/** {@code a} moved {@code t} (0 to 1) of the way towards {@code b}, per colour channel; {@code a}'s alpha is kept. */
	public static int mix(int a, int b, float t) {
		if (t <= 0.0F) {
			return a;
		}
		int rgb = 0;
		for (int shift = 0; shift <= 16; shift += 8) {
			int from = a >> shift & 0xFF;
			int to = b >> shift & 0xFF;
			rgb |= Math.round(from + (to - from) * Math.min(t, 1.0F)) << shift;
		}
		return a & 0xFF000000 | rgb;
	}

	private static double valueNoise(double x, double z, int salt) {
		int x0 = (int) Math.floor(x);
		int z0 = (int) Math.floor(z);
		double fx = smooth(x - x0);
		double fz = smooth(z - z0);
		double top = lerp(lattice(x0, z0, salt), lattice(x0 + 1, z0, salt), fx);
		double bottom = lerp(lattice(x0, z0 + 1, salt), lattice(x0 + 1, z0 + 1, salt), fx);
		return lerp(top, bottom, fz);
	}

	private static double smooth(double t) {
		return t * t * (3.0 - 2.0 * t);
	}

	private static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}

	/** A fixed value in [0, 1) for each lattice point (a 64-bit mix of its coordinates). */
	private static double lattice(int x, int z, int salt) {
		long h = x * 0x9E3779B97F4A7C15L ^ z * 0xC2B2AE3D27D4EB4FL ^ salt * 0x165667B19E3779F9L;
		h ^= h >>> 33;
		h *= 0xFF51AFD7ED558CCDL;
		h ^= h >>> 33;
		h *= 0xC4CEB9FE1A85EC53L;
		h ^= h >>> 33;
		return (h >>> 11) * 0x1.0p-53;
	}
}
