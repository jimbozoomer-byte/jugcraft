package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Grain Sacks (Barn & Harvest Tools, part 5; tools/barn_tools.py SACK): burlap sacks of grain lying on their sides, tied
 * at the neck and stamped with a mill's mark. Place another on one to stack it, up to {@value #MAX} in a block
 * ({@link #SACKS}), each {@value #SACK_HEIGHT} pixels high; breaking them drops each sack. Their models are
 * tools/barn_tools_data.py sack().
 */
public class GrainSackBlock extends Block {
	public static final int MAX = 3;
	/** One sack's height, in pixels. */
	public static final double SACK_HEIGHT = 4.5;
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty SACKS = IntegerProperty.create("sacks", 1, MAX);
	/** Each facing's shape (by its 2D value) for each count of sacks (less one). */
	private static final VoxelShape[][] SHAPES = new VoxelShape[4][MAX];

	static {
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			for (int n = 1; n <= MAX; n++) {
				SHAPES[facing.get2DDataValue()][n - 1] = LongDecorationBlock.shape(new double[][] {{2.0, 0.0, 4.5, 15.5, SACK_HEIGHT * n, 11.5}}, facing);
			}
		}
	}

	public GrainSackBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SACKS, 1));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(FACING).get2DDataValue()][state.getValue(SACKS) - 1];
	}

	/** Another sack, placed on top, stacks on it. */
	@Override
	protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		if (!context.getItemInHand().is(asItem()) || state.getValue(SACKS) >= MAX) {
			return false;
		}
		return !context.replacingClickedOnBlock() || context.getClickedFace() == Direction.UP;
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState there = context.getLevel().getBlockState(context.getClickedPos());
		if (there.is(this)) {
			return there.setValue(SACKS, Math.min(MAX, there.getValue(SACKS) + 1));
		}
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, SACKS);
	}
}
