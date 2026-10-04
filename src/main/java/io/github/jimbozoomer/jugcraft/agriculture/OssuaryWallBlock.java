package io.github.jimbozoomer.jugcraft.agriculture;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * An Ossuary Wall (the churchyard's ornaments): a full block of a catacomb's wall, rows of skulls stacked between courses
 * of long bones laid end-out, set facing whoever placed it so the skulls look out at them.
 */
public class OssuaryWallBlock extends HorizontalDirectionalBlock {
	public static final MapCodec<OssuaryWallBlock> CODEC = simpleCodec(OssuaryWallBlock::new);

	public OssuaryWallBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
