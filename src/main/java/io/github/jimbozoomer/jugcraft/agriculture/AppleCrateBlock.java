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
 * A stack of apple crates (Orchard & Apple Harvest, decor set 2, part 2; tools/orchard_harvest.py CRATES): old wooden
 * field crates, 50 by 37 cm and 30 cm deep, each heaped with one variety of apple (red, green or golden: one block each)
 * and labelled on its end boards. Place another crate of the same kind on it to stack it, up to {@value #MAX} in a block
 * ({@link #CRATES}); breaking it drops each crate. Its models are tools/orchard_harvest_data.py crate().
 */
public class AppleCrateBlock extends Block {
	public static final int MAX = 3;
	/** One crate's height, in pixels. */
	public static final double CRATE_HEIGHT = 4.75;
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty CRATES = IntegerProperty.create("crates", 1, MAX);

	public AppleCrateBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CRATES, 1));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return LongDecorationBlock.shape(new double[][] {{4.0, 0.0, 5.0, 12.0, CRATE_HEIGHT * state.getValue(CRATES), 11.0}},
				state.getValue(FACING));
	}

	/** Another crate of the same kind, placed on top, stacks on it. */
	@Override
	protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		if (!context.getItemInHand().is(asItem()) || state.getValue(CRATES) >= MAX) {
			return false;
		}
		return !context.replacingClickedOnBlock() || context.getClickedFace() == Direction.UP;
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState there = context.getLevel().getBlockState(context.getClickedPos());
		if (there.is(this)) {
			return there.setValue(CRATES, Math.min(MAX, there.getValue(CRATES) + 1));
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
		builder.add(FACING, CRATES);
	}
}
