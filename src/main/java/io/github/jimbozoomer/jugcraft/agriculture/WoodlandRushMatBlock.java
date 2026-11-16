package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Rush Floor Mat (tools/thanksgiving_woodland.py; docs/features/thanksgiving-woodland-crafts.md): a mat of rushes
 * twined with cord, a pixel thick, lying on any block as a carpet does and walked on like one. Its rushes run the way the
 * player faced when laying it ({@link #AXIS}). Mats side by side join ({@link #NORTH}, {@link #EAST}, {@link #SOUTH},
 * {@link #WEST}: whether the mat on that side is a rush mat too), so a floor of them reads as one mat, its bound edge drawn
 * only round its outside. A decoration only: using it does nothing.
 */
public class WoodlandRushMatBlock extends Block {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	private static final Map<Direction, BooleanProperty> SIDES = Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH,
			Direction.WEST, WEST);
	private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

	public WoodlandRushMatBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.Z).setValue(NORTH, false).setValue(EAST, false)
				.setValue(SOUTH, false).setValue(WEST, false));
	}

	/** Whether a mat joins the block beside it: only another rush mat. */
	public boolean joins(BlockState neighbor) {
		return neighbor.is(this);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
		for (Map.Entry<Direction, BooleanProperty> side : SIDES.entrySet()) {
			state = state.setValue(side.getValue(), joins(context.getLevel().getBlockState(context.getClickedPos().relative(side.getKey()))));
		}
		return state;
	}

	/** On anything but air, as a carpet. */
	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return !level.isEmptyBlock(pos.below());
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		BooleanProperty side = SIDES.get(direction);
		return side != null ? state.setValue(side, joins(neighborState))
				: super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		BlockState out = state;
		for (Map.Entry<Direction, BooleanProperty> side : SIDES.entrySet()) {
			out = out.setValue(SIDES.get(rotation.rotate(side.getKey())), state.getValue(side.getValue()));
		}
		boolean quarter = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90;
		return quarter ? out.setValue(AXIS, state.getValue(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X) : out;
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		BlockState out = state;
		for (Map.Entry<Direction, BooleanProperty> side : SIDES.entrySet()) {
			out = out.setValue(SIDES.get(mirror.mirror(side.getKey())), state.getValue(side.getValue()));
		}
		return out;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS, NORTH, EAST, SOUTH, WEST);
	}
}
