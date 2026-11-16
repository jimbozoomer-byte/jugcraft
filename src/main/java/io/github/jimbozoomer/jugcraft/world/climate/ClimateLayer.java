package io.github.jimbozoomer.jugcraft.world.climate;

import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

/**
 * One biome source's climate bands: its {@link ClimateProfile} and, built once the biome tags are bound, its
 * {@link ClimateRemapper}. It wraps a base source's resolver, never the base source itself, so the base keeps its own
 * resolvers and every hook on them (for a multi-noise base, {@code mixin/MultiNoiseBiomeSourceMixin}, Jugcraft's regions
 * and Adam-ondi-Ahman) runs exactly once, inside the base, before the bands remap its answer.
 *
 * <p>Instance state only: each biome source holds its own layer, so two worlds keep independent profiles. There is no
 * static or global climate state.
 */
public final class ClimateLayer {
	private final ClimateProfile profile;
	/** The source whose answers the bands remap (for a design, its seeded fallback; never the design's paint). */
	private final BiomeSource base;
	private volatile ClimateRemapper remapper;

	public ClimateLayer(ClimateProfile profile, BiomeSource base) {
		this.profile = Objects.requireNonNull(profile, "climate profile");
		this.base = Objects.requireNonNull(base, "base biome source");
		if (base instanceof StableBiomeLookup.Source source) source.jugcraft$stabilizeClimateLookup();
	}

	public ClimateProfile profile() {
		return profile;
	}

	/**
	 * The remapper, built on first use: the profile's pools and the category tags are bound only after the registries
	 * load. It fails here, loudly, when a pool is empty or unbound, or when any of the base source's possible biomes is
	 * in no category tag ({@link ClimateRemapper#ClimateRemapper(ClimateProfile, java.util.Collection)}): at resolver
	 * construction, before the bands answer anything, never part-way through generating a chunk. A failure is not
	 * cached, and the base biome is never silently kept instead.
	 */
	public ClimateRemapper remapper() {
		ClimateRemapper built = remapper;
		if (built == null) {
			synchronized (this) {
				built = remapper;
				if (built == null) {
					try {
						built = new ClimateRemapper(profile, base.possibleBiomes());
					} catch (RuntimeException e) {
						throw new IllegalStateException("Climate bands: this world's climate profile cannot be applied (" + e.getMessage() + ")", e);
					}
					remapper = built;
				}
			}
		}
		return built;
	}

	/** The base resolver's answer, remapped by the bands at the same quart position with the same sampler. */
	public BiomeResolver wrap(BiomeResolver base, Climate.Sampler sampler) {
		ClimateRemapper bands = remapper();
		return (x, y, z) -> bands.remap(base.getNoiseBiome(x, y, z), x, y, z, sampler);
	}

	/**
	 * Every pool member, without classifying the base. Tag contents can be available before the registry freezes
	 * the holder's reverse tag membership; possible-biome enumeration must not require that later step. Resolver
	 * construction still validates every pool and base category before generating any chunk.
	 */
	public Stream<Holder<Biome>> possibleBiomes() {
		return ClimateZone.BANDED.stream().flatMap(zone -> ClimateCategory.POOLED.stream()
				.flatMap(category -> profile.pools().get(zone, category).stream())).distinct();
	}

	/** Whether this source already applies climate bands somewhere in its chain, so they are never applied twice. */
	public static boolean applies(BiomeSource source) {
		return source instanceof Carrier carrier && carrier.appliesClimate();
	}

	/** A biome source that may apply climate bands itself or through the sources it wraps. */
	public interface Carrier {
		boolean appliesClimate();
	}
}
