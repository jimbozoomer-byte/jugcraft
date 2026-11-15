package io.github.jimbozoomer.jugcraft.world.climate;

import java.util.List;

/**
 * The role a base biome plays in the landscape, which its replacement must keep: oceans stay oceans, rivers rivers,
 * beaches beaches. Pure: no Minecraft types. The data that assigns biomes to categories is the explicit tags
 * {@code #jugcraft:climate/category/<id>} ({@link ClimateCategories}).
 *
 * <p>{@link #PRESERVE} is the underground and cave biomes: they are never remapped, at any depth or height. A biome in
 * no category is not given one silently; {@link ClimateRemapper#requireCategorized} refuses it.
 */
public enum ClimateCategory {
	LAND("land"),
	OCEAN("ocean"),
	DEEP_OCEAN("deep_ocean"),
	RIVER("river"),
	BEACH("beach"),
	MOUNTAIN("mountain"),
	PRESERVE("preserve");

	/** The categories that have a pool in every banded zone, in their serialized order. */
	public static final List<ClimateCategory> POOLED = List.of(LAND, OCEAN, DEEP_OCEAN, RIVER, BEACH, MOUNTAIN);

	/**
	 * The order in which the category tags are tried; the first tag holding a biome decides its category. The explicit
	 * tags are disjoint, so this matters only when a data pack adds a biome to two of them.
	 */
	public static final List<ClimateCategory> PRECEDENCE = List.of(PRESERVE, DEEP_OCEAN, OCEAN, RIVER, BEACH, MOUNTAIN, LAND);

	private final String id;

	ClimateCategory(String id) {
		this.id = id;
	}

	/** The serialized name, as in the profile's {@code pools} object and the tag paths. */
	public String id() {
		return id;
	}

	/** Whether biomes of this category are replaced from a pool (every category but PRESERVE). */
	public boolean pooled() {
		return this != PRESERVE;
	}

	/** The pooled category with this serialized name, or null. */
	public static ClimateCategory pooled(String id) {
		for (ClimateCategory category : POOLED) {
			if (category.id.equals(id)) {
				return category;
			}
		}
		return null;
	}
}
