package io.github.jimbozoomer.jugcraft.season;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.Map;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * The newer biomes' kinds, as the seasonal and fall systems read them (docs/features/seasonal-biome-audit.md). The
 * landscape collection, the Alpine Spawn and the caverns came after the seasons and the fall content and are in no
 * conventional biome tag, so every conventional tag a seasonal or fall system reads has a counterpart,
 * {@code #jugcraft:seasonal_kinds/<the same path>}, which holds them by what they are (tools/seasonal_kinds.py KINDS): a
 * landscape maple wood is a forest, a landscape bog a swamp. Only the seasonal and fall systems read these, so ores,
 * raider camps and the other wild crops are as they were.
 */
public final class SeasonalKinds {
	/** Each conventional tag the seasonal and fall systems read, and its counterpart (tools/seasonal_kinds.py KINDS). */
	private static final Map<TagKey<Biome>, TagKey<Biome>> KINDS = Map.ofEntries(
			Map.entry(ConventionalBiomeTags.IS_PLAINS, of("is_plains")),
			Map.entry(ConventionalBiomeTags.IS_FOREST, of("is_forest")),
			Map.entry(ConventionalBiomeTags.IS_TAIGA, of("is_taiga")),
			Map.entry(ConventionalBiomeTags.IS_BIRCH_FOREST, of("is_birch_forest")),
			Map.entry(ConventionalBiomeTags.IS_SWAMP, of("is_swamp")),
			Map.entry(ConventionalBiomeTags.IS_SPOOKY, of("is_spooky")),
			Map.entry(ConventionalBiomeTags.IS_SNOWY, of("is_snowy")),
			Map.entry(ConventionalBiomeTags.IS_FLORAL, of("is_floral")),
			Map.entry(ConventionalBiomeTags.IS_SAVANNA, of("is_savanna")),
			Map.entry(ConventionalBiomeTags.IS_JUNGLE, of("is_jungle")),
			Map.entry(ConventionalBiomeTags.IS_HILL, of("is_hill")),
			Map.entry(ConventionalBiomeTags.IS_WINDSWEPT, of("is_windswept")),
			Map.entry(ConventionalBiomeTags.IS_RIVER, of("is_river")),
			Map.entry(ConventionalBiomeTags.IS_BADLANDS, of("is_badlands")));

	private SeasonalKinds() {
	}

	/** The counterpart of the conventional tag {@code c:<path>} ({@code is_forest} -> {@code jugcraft:seasonal_kinds/is_forest}). */
	public static TagKey<Biome> of(String path) {
		return TagKey.create(Registries.BIOME, Jugcraft.id("seasonal_kinds/" + path));
	}

	/** The counterpart of {@code conventional}, one of the conventional tags the seasonal and fall systems read. */
	public static TagKey<Biome> of(TagKey<Biome> conventional) {
		TagKey<Biome> kind = KINDS.get(conventional);
		if (kind == null) {
			throw new IllegalArgumentException("No seasonal kind for " + conventional + " (tools/seasonal_kinds.py KINDS)");
		}
		return kind;
	}

	/** Biomes in {@code conventional}, or of its kind among the newer biomes: for a seasonal or fall system's placement. */
	public static Predicate<BiomeSelectionContext> selector(TagKey<Biome> conventional) {
		return BiomeSelectors.tag(conventional).or(BiomeSelectors.tag(of(conventional)));
	}

	/** Whether {@code biome} is in {@code conventional}, or of its kind among the newer biomes. */
	public static boolean is(Holder<Biome> biome, TagKey<Biome> conventional) {
		return biome.is(conventional) || biome.is(of(conventional));
	}
}
