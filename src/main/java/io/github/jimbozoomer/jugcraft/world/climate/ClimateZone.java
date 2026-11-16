package io.github.jimbozoomer.jugcraft.world.climate;

import java.util.List;

/**
 * The spawn-relative climate belts. Pure: no Minecraft types.
 *
 * <p>North is -Z. With {@code s = blockZ - anchorZ} (south positive) and {@code n = -s}, the belts are, in order
 * from north to south: FRIGID ({@code n >= north_frigid}), CENTRAL, HOT ({@code south_hot_start <= s < south_hot_end}),
 * TEMPERATE ({@code south_hot_end <= s < south_frigid}) and FRIGID again ({@code s >= south_frigid}). CENTRAL keeps the
 * wrapped source's biome unchanged; the other three remap it into their pools ({@link ClimateRemapper}).
 */
public enum ClimateZone {
	CENTRAL("central"),
	FRIGID("frigid"),
	HOT("hot"),
	TEMPERATE("temperate");

	/** The zones that own biome pools, in their serialized order. */
	public static final List<ClimateZone> BANDED = List.of(FRIGID, HOT, TEMPERATE);

	private final String id;

	ClimateZone(String id) {
		this.id = id;
	}

	/** The serialized name, as in the profile's {@code pools} object and the default tag paths. */
	public String id() {
		return id;
	}

	/** Whether biomes in this zone are remapped (every zone but CENTRAL). */
	public boolean remapped() {
		return this != CENTRAL;
	}

	/** The banded zone with this serialized name, or null (CENTRAL has no pools and is not a valid key). */
	public static ClimateZone banded(String id) {
		for (ClimateZone zone : BANDED) {
			if (zone.id.equals(id)) {
				return zone;
			}
		}
		return null;
	}
}
