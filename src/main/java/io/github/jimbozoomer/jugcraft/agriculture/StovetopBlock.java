package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jspecify.annotations.Nullable;

/**
 * A pot or kettle set on the stove (Cozy Fall Hearth, part 2: the Tea Kettle and the Simmer Pot): it stands on any
 * sturdy top or on a heat source (block tag jugcraft:heat_sources: a campfire, the Kitchen Stove, the Woodstove), and is
 * {@link #HEATED} while what it stands on heats a pot, as a Cooking Pot counts it ({@link CookingPotBlockEntity#isHeated}):
 * in the tag, and lit if it can be put out. It is worked out again whenever the block below changes, so nothing ticks.
 */
public class StovetopBlock extends FacingDecorationBlock {
	public static final BooleanProperty HEATED = BooleanProperty.create("heated");

	public StovetopBlock(Properties properties, double[] box) {
		super(properties, box);
		registerDefaultState(defaultBlockState().setValue(HEATED, false));
	}

	/** Whether {@code below} heats what stands on it. */
	public static boolean heats(BlockState below) {
		return below.is(CookingPotBlockEntity.HEAT_SOURCES) && (!below.hasProperty(BlockStateProperties.LIT) || below.getValue(BlockStateProperties.LIT));
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockState below = level.getBlockState(pos.below());
		return below.is(CookingPotBlockEntity.HEAT_SOURCES) || Block.canSupportCenter(level, pos.below(), Direction.UP);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		return state == null ? null : state.setValue(HEATED, heats(context.getLevel().getBlockState(context.getClickedPos().below())));
	}

	/** Its stand gone, it comes off; its stand lit or put out, it heats or cools. */
	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction != Direction.DOWN) {
			return state;
		}
		return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state.setValue(HEATED, heats(neighborState));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HEATED);
	}
}
