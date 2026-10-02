package io.github.jimbozoomer.jugcraft.season;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Snow that winter laid (season/SeasonalSnow). It looks and behaves like vanilla snow layers, but once it is no
 * longer snowing (spring, or seasonal snow switched off) each random tick melts a layer, so the season leaves nothing
 * behind. Vanilla snow, including any a player placed, is a different block and never melts this way.
 */
public class SeasonalSnowBlock extends SnowLayerBlock {
	public SeasonalSnowBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!SeasonState.snowing()) {
			melt(state, level, pos);
			return;
		}
		super.randomTick(state, level, pos, random);
	}

	/** Takes one layer off (the last one leaves air); no snowballs drop, the snow just melts. */
	public static void melt(BlockState state, ServerLevel level, BlockPos pos) {
		int layers = state.getValue(LAYERS);
		if (layers > 1) {
			level.setBlockAndUpdate(pos, state.setValue(LAYERS, layers - 1));
		} else {
			level.removeBlock(pos, false);
		}
	}
}
