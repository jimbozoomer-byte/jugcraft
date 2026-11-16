package io.github.jimbozoomer.jugcraft.world.climate;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.world.design.DesignBiomeSource;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

/**
 * {@code jugcraft:climate}: any base biome source with spawn-relative climate bands over its answers. The base is
 * normally the seeded Overworld ({@code minecraft:multi_noise}, preset {@code minecraft:overworld}), which still answers
 * through Jugcraft's {@code MultiNoiseBiomeSourceMixin} and regions; this source only remaps what the base's resolver
 * returns ({@link ClimateLayer}). It never subclasses the multi-noise source, so no mixin applies to it twice.
 *
 * <p>No shipped world preset uses this type yet: the procedural preset is deferred, because Alpine Spawn's start search
 * reads this very source's biomes and generates chunks before a start is known. A data pack that uses it must write a
 * {@code fixed} anchor at its world's authored spawn. A designed world takes its bands from the design source's own
 * {@code climate} field instead, where the painted cells win; this source refuses a design as its base, and refuses any
 * base that already applies bands.
 */
public final class ClimateBiomeSource extends BiomeSource implements ClimateLayer.Carrier {
	public static final MapCodec<ClimateBiomeSource> CODEC = RecordCodecBuilder.<ClimateBiomeSource>mapCodec(i -> i.group(
			BiomeSource.CODEC.fieldOf("base").forGetter(ClimateBiomeSource::base),
			ClimateProfile.CODEC.fieldOf("climate").forGetter(ClimateBiomeSource::climate)
	).apply(i, ClimateBiomeSource::new)).validate(ClimateBiomeSource::check);

	private final BiomeSource base;
	private final ClimateLayer layer;

	public ClimateBiomeSource(BiomeSource base, ClimateProfile climate) {
		this.base = base;
		this.layer = new ClimateLayer(climate, base);
	}

	private static DataResult<ClimateBiomeSource> check(ClimateBiomeSource source) {
		if (source.base instanceof DesignBiomeSource) {
			return DataResult.error(() -> "A jugcraft:climate source cannot wrap a designed world; give the design a climate field instead, so its painted cells win");
		}
		if (ClimateLayer.applies(source.base)) {
			return DataResult.error(() -> "A jugcraft:climate source cannot wrap a source that already applies climate bands");
		}
		return DataResult.success(source);
	}

	public BiomeSource base() {
		return base;
	}

	public ClimateProfile climate() {
		return layer.profile();
	}

	@Override
	public boolean appliesClimate() {
		return true;
	}

	@Override
	protected MapCodec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return Stream.concat(base.possibleBiomes().stream(), layer.possibleBiomes()).distinct();
	}

	@Override
	public BiomeResolver createResolver(Climate.Sampler sampler) {
		return layer.wrap(base.createResolver(sampler), sampler);
	}

	@Override
	public BiomeResolver createResolverForChunk(Climate.Sampler sampler, int x, int y, int z, int sx, int sy, int sz) {
		return layer.wrap(base.createResolverForChunk(sampler, x, y, z, sx, sy, sz), sampler);
	}
}
