package io.github.jimbozoomer.jugcraft.world.climate;

/**
 * Stateless hashing and coherent value noise for the climate blends and in-pool choices. Pure: no Minecraft types, no
 * {@code Random}, no mutable or thread-local state, so every answer depends only on its arguments, never on chunk
 * generation order or thread.
 *
 * <p>All hashing is 32-bit integer arithmetic (murmur3's finaliser) and the interpolation is plain IEEE doubles, so a
 * port using {@code Math.imul} and {@code >>>} reproduces it exactly, should a preview ever need the real pattern.
 */
final class ClimateNoise {
	/** Lattice spacing of the coarse and fine octaves of the blend noise, in blocks. */
	static final int COARSE = 64;
	static final int FINE = 23;
	static final double COARSE_WEIGHT = 0.65;

	private ClimateNoise() {
	}

	static int mix(int h) {
		h ^= h >>> 16;
		h *= 0x85EBCA6B;
		h ^= h >>> 13;
		h *= 0xC2B2AE35;
		h ^= h >>> 16;
		return h;
	}

	/** A well-mixed hash of the salt, a stream number (which pattern) and two integers. */
	static int hash(int salt, int stream, int a, int b) {
		int h = mix(salt ^ 0x9E3779B9 * (stream + 1));
		h = mix(h ^ a * 0x27D4EB2F);
		return mix(h ^ b * 0x165667B1);
	}

	/** A hash as a fraction in [0, 1), from its top 24 bits. */
	static double unit(int h) {
		return (h >>> 8) * 0x1.0p-24;
	}

	/** Bilinear value noise on a square lattice of {@code cell} blocks, with a smoothstep fade; in [0, 1). */
	static double value(int salt, int stream, int x, int z, int cell) {
		int cx = Math.floorDiv(x, cell), cz = Math.floorDiv(z, cell);
		double fx = fade((x - (double) cx * cell + 0.5) / cell), fz = fade((z - (double) cz * cell + 0.5) / cell);
		double a = unit(hash(salt, stream, cx, cz)), b = unit(hash(salt, stream, cx + 1, cz));
		double c = unit(hash(salt, stream, cx, cz + 1)), d = unit(hash(salt, stream, cx + 1, cz + 1));
		double top = a + (b - a) * fx, bottom = c + (d - c) * fx;
		return top + (bottom - top) * fz;
	}

	/**
	 * The quantiles of {@link #rawBlend} at k/64, k = 0 to 64, measured from 24 million samples (six salts, four
	 * edges, a million places each); the ends are set to 0 and 1. Value noise bunches round 0.5, so the raw value is
	 * mapped through this fixed table ({@link #uniform}) to be uniform on [0, 1): then "noise &lt; p" holds on a share p
	 * of the land, and the blend's outer share follows the smoothstep across the whole band. A constant, never written.
	 */
	private static final double[] QUANTILES = {
			0.0, 0.176835, 0.211589, 0.235626, 0.254720, 0.270972, 0.285224, 0.298145,
			0.310010, 0.321014, 0.331347, 0.341181, 0.350571, 0.359530, 0.368196, 0.376580,
			0.384740, 0.392662, 0.400435, 0.408031, 0.415504, 0.422876, 0.430119, 0.437284,
			0.444369, 0.451389, 0.458336, 0.465243, 0.472111, 0.478967, 0.485774, 0.492542,
			0.499316, 0.506088, 0.512892, 0.519718, 0.526550, 0.533442, 0.540353, 0.547320,
			0.554354, 0.561456, 0.568633, 0.575906, 0.583296, 0.590815, 0.598483, 0.606294,
			0.614294, 0.622487, 0.630935, 0.639649, 0.648706, 0.658093, 0.667938, 0.678375,
			0.689444, 0.701366, 0.714321, 0.728686, 0.744974, 0.764225, 0.788450, 0.823586,
			1.0};

	/** Two octaves of value noise for one blend edge ({@code edge} 0 to 3); in [0, 1), bunched round 0.5. */
	static double rawBlend(int salt, int edge, int x, int z) {
		return COARSE_WEIGHT * value(salt, 2 * edge, x, z, COARSE) + (1 - COARSE_WEIGHT) * value(salt, 2 * edge + 1, x, z, FINE);
	}

	/** The coherent blend noise of one edge, made uniform on [0, 1) by the quantile table. */
	static double blend(int salt, int edge, int x, int z) {
		return uniform(rawBlend(salt, edge, x, z));
	}

	/** A raw blend value's place in the measured distribution: piecewise linear between the quantiles; in [0, 1). */
	static double uniform(double raw) {
		int lo = 0, hi = QUANTILES.length - 1;
		while (hi - lo > 1) {
			int mid = (lo + hi) >>> 1;
			if (raw < QUANTILES[mid]) {
				hi = mid;
			} else {
				lo = mid;
			}
		}
		double within = Math.clamp((raw - QUANTILES[lo]) / (QUANTILES[hi] - QUANTILES[lo]), 0.0, 1.0);
		return Math.min((lo + within) / (QUANTILES.length - 1), Math.nextDown(1.0));
	}

	static double fade(double t) {
		return t * t * (3 - 2 * t);
	}
}
