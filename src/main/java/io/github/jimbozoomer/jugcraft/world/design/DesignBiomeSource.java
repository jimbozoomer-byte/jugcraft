package io.github.jimbozoomer.jugcraft.world.design;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateLayer;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateProfile;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.*;

/**
 * Per-world authored surface biomes, with the existing seeded source outside the design. An optional {@code climate}
 * profile puts spawn-relative climate bands over the seeded fallback only: painted cells always win. Without it the
 * source behaves, and serializes, exactly as before.
 */
public final class DesignBiomeSource extends BiomeSource implements ClimateLayer.Carrier {
	public static final MapCodec<DesignBiomeSource> CODEC = RecordCodecBuilder.<DesignBiomeSource>mapCodec(i -> i.group(
			Codec.intRange(1, 1).fieldOf("schema").forGetter(s -> 1),
			BiomeSource.CODEC.fieldOf("base").forGetter(s -> s.base),
			DesignGrid.CODEC.fieldOf("grid").forGetter(s -> s.grid),
			Biome.CODEC.listOf(1, 512).fieldOf("palette").forGetter(s -> s.palette),
			Codec.intRange(0, 512).listOf(25, 16641).fieldOf("cells").forGetter(s -> s.cells),
			BlockPos.CODEC.fieldOf("spawn").forGetter(s -> s.spawn),
			BlockPos.CODEC.optionalFieldOf("town").forGetter(s -> s.town),
			ClimateProfile.CODEC.optionalFieldOf("climate").forGetter(s -> s.climate)
	).apply(i, (schema, base, grid, palette, cells, spawn, town, climate) -> new DesignBiomeSource(base, grid, palette, cells, spawn, town, climate)))
			.validate(s -> s.cells.size() == s.grid.width() * s.grid.width() && s.cells.stream().allMatch(v -> v <= s.palette.size())
					&& s.grid.weight(s.spawn.getX(), s.spawn.getZ()) == 1
					&& s.town.map(p -> s.grid.weight(p.getX(), p.getZ()) == 1).orElse(true)
					? DataResult.success(s) : DataResult.error(() -> "Design cells/palette are invalid, or spawn/town is outside the solid design area"))
			.validate(DesignBiomeSource::checkClimate);
	private final BiomeSource base;
	private final DesignGrid grid;
	private final List<Holder<Biome>> palette;
	private final List<Integer> cells;
	private final BlockPos spawn;
	private final Optional<BlockPos> town;
	private final Optional<ClimateProfile> climate;
	/** This source's own climate bands over its seeded fallback (null without a climate profile); never shared. */
	private final ClimateLayer layer;
	public DesignBiomeSource(BiomeSource base, DesignGrid grid, List<Holder<Biome>> palette, List<Integer> cells, BlockPos spawn, Optional<BlockPos> town) {
		this(base, grid, palette, cells, spawn, town, Optional.empty());
	}
	public DesignBiomeSource(BiomeSource base, DesignGrid grid, List<Holder<Biome>> palette, List<Integer> cells, BlockPos spawn, Optional<BlockPos> town,
			Optional<ClimateProfile> climate) {
		this.base = base; this.grid = grid; this.palette = List.copyOf(palette); this.cells = List.copyOf(cells);
		this.spawn = spawn.immutable(); this.town = town.map(BlockPos::immutable);
		this.climate = climate; this.layer = climate.map(profile -> new ClimateLayer(profile, base)).orElse(null);
	}
	/** The bands' anchor is the design's authored spawn (the world spawn applySpawn sets), and bands are applied once. */
	private static DataResult<DesignBiomeSource> checkClimate(DesignBiomeSource s) {
		if (s.climate.isEmpty()) return DataResult.success(s);
		ClimateProfile c = s.climate.get();
		if (c.anchorX() != s.spawn.getX() || c.anchorZ() != s.spawn.getZ())
			return DataResult.error(() -> "Design climate anchor " + c.anchorX() + " " + c.anchorZ() + " must be the design spawn " + s.spawn.getX() + " " + s.spawn.getZ());
		if (ClimateLayer.applies(s.base))
			return DataResult.error(() -> "Design climate cannot wrap a base source that already applies climate bands");
		return DataResult.success(s);
	}
	public BlockPos spawn() { return spawn; }
	public Optional<BlockPos> town() { return town; }
	public DesignGrid grid() { return grid; }
	public BiomeSource base() { return base; }
	public Optional<ClimateProfile> climate() { return climate; }
	@Override public boolean appliesClimate() { return layer != null || ClimateLayer.applies(base); }
	@Override protected MapCodec<? extends BiomeSource> codec() { return CODEC; }
	@Override protected Stream<Holder<Biome>> collectPossibleBiomes() {
		Stream<Holder<Biome>> biomes = Stream.concat(base.possibleBiomes().stream(), palette.stream());
		return (layer == null ? biomes : Stream.concat(biomes, layer.possibleBiomes())).distinct();
	}
	@Override public BiomeResolver createResolver(Climate.Sampler sampler) { return overlay(banded(base.createResolver(sampler), sampler)); }
	@Override public BiomeResolver createResolverForChunk(Climate.Sampler sampler, int x, int y, int z, int sx, int sy, int sz) {
		return overlay(banded(base.createResolverForChunk(sampler, x, y, z, sx, sy, sz), sampler));
	}
	/** The seeded fallback, under the climate bands if this design has them; unchanged (the same resolver) if not. */
	private BiomeResolver banded(BiomeResolver fallback, Climate.Sampler sampler) {
		return layer == null ? fallback : layer.wrap(fallback, sampler);
	}
	private BiomeResolver overlay(BiomeResolver fallback) {
		return (x, y, z) -> {
			int bx = x * 4, bz = z * 4;
			if (y >= 0 && grid.weight(bx, bz) > 0) {
				int id = cells.get(grid.cell(bx, bz));
				if (id > 0) return palette.get(id - 1);
			}
			return fallback.getNoiseBiome(x, y, z);
		};
	}
}
