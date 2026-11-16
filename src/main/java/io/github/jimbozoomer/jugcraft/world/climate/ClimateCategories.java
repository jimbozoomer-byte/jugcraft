package io.github.jimbozoomer.jugcraft.world.climate;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Which {@link ClimateCategory} a biome belongs to, from the explicit category tags
 * {@code #jugcraft:climate/category/<category>} (preserve, land, ocean, deep_ocean, river, beach, mountain). The shipped
 * tags list every Overworld biome of vanilla 26.3 and Jugcraft by ID, built from the repository's own biome metadata
 * (vanilla's role tags, the Landscape habitats and kinds, the region rules' replaced biomes, the cave lists), so custom
 * oceans, rivers and beaches are never taken for land. {@code preserve} also takes {@code #c:is_cave} and
 * {@code #c:is_underground} (optional), so another mod's tagged caves stay untouched.
 *
 * <p>There is no default: a biome in none of the tags has no category ({@code null}), and a remapper refuses it
 * rather than guess. The tags are constants; there is no cache or other mutable state.
 */
public final class ClimateCategories {
	private static final Map<ClimateCategory, TagKey<Biome>> TAGS = tags();

	private ClimateCategories() {
	}

	private static Map<ClimateCategory, TagKey<Biome>> tags() {
		Map<ClimateCategory, TagKey<Biome>> out = new EnumMap<>(ClimateCategory.class);
		for (ClimateCategory category : ClimateCategory.values()) {
			out.put(category, TagKey.create(Registries.BIOME, Jugcraft.id("climate/category/" + category.id())));
		}
		return java.util.Collections.unmodifiableMap(out);
	}

	/** The category tag {@code #jugcraft:climate/category/<category>}. */
	public static TagKey<Biome> tag(ClimateCategory category) {
		return TAGS.get(category);
	}

	/** The biome's category: the first category tag holding it, in {@link ClimateCategory#PRECEDENCE}; null if none. */
	public static ClimateCategory classify(Holder<Biome> biome) {
		for (ClimateCategory category : ClimateCategory.PRECEDENCE) {
			if (biome.is(TAGS.get(category))) {
				return category;
			}
		}
		return null;
	}

	/** The biomes among these that are in no category tag, in their given order. */
	public static List<Holder<Biome>> uncategorized(Collection<? extends Holder<Biome>> biomes) {
		List<Holder<Biome>> out = new ArrayList<>();
		for (Holder<Biome> biome : biomes) {
			if (classify(biome) == null) {
				out.add(biome);
			}
		}
		return out;
	}
}
