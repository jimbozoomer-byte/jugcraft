package io.github.jimbozoomer.jugcraft.world.climate;

/**
 * The fixed, authored point the climate belts are measured from. Pure: no Minecraft types.
 *
 * <p>{@code fixed} is the only mode (contract amendments 1 and 3): a designed world's editor writes the design spawn
 * x/z at export, and the design's biome source refuses an anchor that differs from it. The anchor is part of the
 * serialized generator settings, so it never moves with {@code /setworldspawn}. The earlier {@code climate_spawn}
 * mode was removed; a profile naming it is rejected ({@link ClimateRules}).
 */
public record ClimateAnchor(int x, int z) {
	/** The serialized {@code anchor.mode}. */
	public static final String FIXED = "fixed";
	/** The largest accepted {@code |x|} and {@code |z|} (rule 3): the world border's reach. */
	public static final int LIMIT = 30_000_000;

	public ClimateAnchor {
		if (Math.abs((long) x) > LIMIT || Math.abs((long) z) > LIMIT) {
			throw new IllegalArgumentException("Climate anchor " + x + ", " + z + " lies beyond +/-" + LIMIT);
		}
	}

	/** The serialized mode: always {@link #FIXED}. */
	public String mode() {
		return FIXED;
	}
}
