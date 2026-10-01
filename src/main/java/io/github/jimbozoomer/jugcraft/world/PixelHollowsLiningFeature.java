package io.github.jimbozoomer.jugcraft.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Lines the Pixel Hollows: every stone face open to air or water inside the biome becomes circuitstone, and
 * scattered patches of the lining grow pixel crystal clusters. Runs once per chunk (data/jugcraft/worldgen/
 * placed_feature/pixel_hollows_lining.json has no placement modifiers), checks the biome per 4x4x4 cell, and
 * writes only inside its own chunk. Ores are left alone, so exposed ore still shows.
 */
public class PixelHollowsLiningFeature extends Feature<NoneFeatureConfiguration> {
	/** About one 4x4x4 cell of the lining in this many becomes a crystal patch. */
	static final int PATCH_RARITY = 7;
	/** Inside a patch, about one open face in this many grows a cluster. */
	static final int CLUSTER_RARITY = 4;
	private static final Direction[] DIRECTIONS = Direction.values();

	public PixelHollowsLiningFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		ChunkPos chunk = new ChunkPos(context.origin());
		int minX = chunk.getMinBlockX();
		int minZ = chunk.getMinBlockZ();
		int minY = level.getMinY();
		int maxY = level.getMaxY();
		BlockState lining = PixelHollows.CIRCUITSTONE.defaultBlockState();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos side = new BlockPos.MutableBlockPos();
		List<BlockPos> clusterSpots = new ArrayList<>();
		List<Direction> clusterFacings = new ArrayList<>();
		boolean changed = false;

		for (int qy = QuartPos.fromBlock(minY); qy <= QuartPos.fromBlock(maxY); qy++) {
			for (int qx = 0; qx < 4; qx++) {
				for (int qz = 0; qz < 4; qz++) {
					int cellX = minX + qx * 4;
					int cellY = QuartPos.toBlock(qy);
					int cellZ = minZ + qz * 4;
					if (!level.getNoiseBiome(QuartPos.fromBlock(cellX), qy, QuartPos.fromBlock(cellZ)).is(PixelHollows.BIOME)) {
						continue;
					}
					boolean patch = random.nextInt(PATCH_RARITY) == 0;
					for (int y = cellY; y < cellY + 4; y++) {
						if (y < minY || y > maxY) {
							continue;
						}
						for (int x = cellX; x < cellX + 4; x++) {
							for (int z = cellZ; z < cellZ + 4; z++) {
								pos.set(x, y, z);
								if (!level.getBlockState(pos).is(BlockTags.BASE_STONE_OVERWORLD)) {
									continue;
								}
								Direction open = openSide(level, pos, side, minY, maxY);
								if (open == null) {
									continue;
								}
								level.setBlock(pos, lining, Block.UPDATE_CLIENTS);
								changed = true;
								if (patch && random.nextInt(CLUSTER_RARITY) == 0) {
									clusterSpots.add(pos.relative(open));
									clusterFacings.add(open);
								}
							}
						}
					}
				}
			}
		}

		// Clusters point away from the circuitstone they grow on; only into air inside this chunk.
		for (int i = 0; i < clusterSpots.size(); i++) {
			BlockPos spot = clusterSpots.get(i);
			if (spot.getX() < minX || spot.getX() >= minX + 16 || spot.getZ() < minZ || spot.getZ() >= minZ + 16
					|| !level.getBlockState(spot).isAir()) {
				continue;
			}
			level.setBlock(spot, PixelHollows.PIXEL_CRYSTAL_CLUSTER.defaultBlockState()
					.setValue(AmethystClusterBlock.FACING, clusterFacings.get(i)), Block.UPDATE_CLIENTS);
		}
		return changed;
	}

	/** A side of the block that faces air or a fluid (preferring air), or null when it is buried. */
	private static Direction openSide(WorldGenLevel level, BlockPos pos, BlockPos.MutableBlockPos side, int minY, int maxY) {
		Direction wet = null;
		for (Direction direction : DIRECTIONS) {
			side.setWithOffset(pos, direction);
			if (side.getY() < minY || side.getY() > maxY) {
				continue;
			}
			BlockState state = level.getBlockState(side);
			if (state.isAir()) {
				return direction;
			}
			if (wet == null && !state.getFluidState().isEmpty()) {
				wet = direction;
			}
		}
		return wet;
	}
}
