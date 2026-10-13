package io.github.jimbozoomer.jugcraft.materials;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;

/** A finite native ore pocket selected by its surface biome, with an optional, linked surface outcrop. */
public record SurfaceLinkedOreFeature(OreFeature ore, TagKey<Biome> surfaceBiomes, BlockState outcrop,
                                     int outcropChance) implements Feature {
	public static final MapCodec<SurfaceLinkedOreFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			OreFeature.CODEC.codec().fieldOf("ore").forGetter(SurfaceLinkedOreFeature::ore),
			TagKey.codec(Registries.BIOME).fieldOf("surface_biomes").forGetter(SurfaceLinkedOreFeature::surfaceBiomes),
			BlockState.CODEC.fieldOf("outcrop").forGetter(SurfaceLinkedOreFeature::outcrop),
			Codec.intRange(1, 1024).fieldOf("outcrop_chance").forGetter(SurfaceLinkedOreFeature::outcropChance)
	).apply(i, SurfaceLinkedOreFeature::new));

	@Override public MapCodec<SurfaceLinkedOreFeature> codec() { return CODEC; }

	@Override public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		// Biome modifications can also reach other dimensions reusing Overworld biomes. Stay in the Overworld.
		if (!level.getLevel().dimension().equals(Level.OVERWORLD)) return false;
		int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, origin.getX(), origin.getZ());
		if (!level.getBiome(new BlockPos(origin.getX(), surfaceY, origin.getZ())).is(surfaceBiomes)) return false;
		if (!ore.place(level, generator, random, origin)) return false;
		if (random.nextInt(outcropChance) == 0) placeOutcrop(level, random, origin);
		return true;
	}

	private void placeOutcrop(WorldGenLevel level, RandomSource random, BlockPos origin) {
		int count = 3 + random.nextInt(6);
		var structures = level instanceof WorldGenRegion region
				? level.getLevel().structureManager().forWorldGenRegion(region) : level.getLevel().structureManager();
		// A compact connected footprint, rotated each attempt. Eight bounded candidates; no chunk loading or scans.
		int[][] footprint = {{0, 0}, {1, 0}, {0, 1}, {1, 1}, {0, -1}, {1, -1}, {-1, 1}, {2, -1}};
		for (int attempt = 0; attempt < 8; attempt++) {
			int dx = random.nextInt(11) - 5, dz = random.nextInt(11) - 5;
			int rotation = random.nextInt(4);
			List<BlockPos> positions = new ArrayList<>(count);
			for (int n = 0; n < count; n++) {
				int x = footprint[n][0], z = footprint[n][1];
				for (int r = 0; r < rotation; r++) { int oldX = x; x = -z; z = oldX; }
				x += origin.getX() + dx; z += origin.getZ() + dz;
				int ox = x - origin.getX(), oz = z - origin.getZ();
				if (ox * ox + oz * oz > 64) break;
				BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z), z);
				// Heightmaps include snow blocks, but the thin snow layer may be above the ground.
				if (!level.ensureCanWrite(pos) || !level.getBiome(pos).is(surfaceBiomes)) break;
				BlockState ground = level.getBlockState(pos.below());
				BlockState cover = level.getBlockState(pos);
				if (!naturalGround(ground) || !ground.getFluidState().isEmpty()
						|| !(cover.isAir() || cover.is(Blocks.SNOW)) || !cover.getFluidState().isEmpty()
						|| !level.getBlockState(pos.above()).isAir() || level.getBlockEntity(pos.below()) != null) break;
				// Stay on a single terrace, so every ore has an exposed side even after seasonal snow settles.
				if (!positions.isEmpty() && pos.getY() != positions.getFirst().getY()) break;
				// Natural-looking stone roofs also occur in structures. Use the region's cached structure starts,
				// not the ServerLevel's chunk loader, while decoration is running on a worldgen worker.
				if (structures.getStructureWithPieceAt(pos.below(), holder -> true).isValid()) break;
				positions.add(pos);
			}
			if (positions.size() == count) {
				for (BlockPos pos : positions) level.setBlock(pos, outcrop, 2);
				return;
			}
		}
	}

	private static boolean naturalGround(BlockState state) {
		return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.DIRT) || state.is(BlockTags.GRASS_BLOCKS)
				|| state.is(Blocks.GRAVEL) || state.is(Blocks.SNOW_BLOCK);
	}
}
