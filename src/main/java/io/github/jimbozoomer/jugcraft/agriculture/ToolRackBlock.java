package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Pitchfork and Rake Rack (Barn & Harvest Tools, part 5; tools/barn_tools.py TOOL_RACK): a peg rail on a barn wall
 * with a pitchfork and a hay rake stood beneath it, one block across and two up ({@link MultiDecorationBlock}). It hangs
 * from the wall the player aims at: its rail, on its upper block, needs a sturdy wall behind it, and the rack comes off
 * (dropping itself) when the wall goes. It has no lanterns ({@link #LIT} stays on). Its model is tools/barn_tools_data.py
 * tool_rack().
 */
public class ToolRackBlock extends MultiDecorationBlock {
	/** Its blocks: 1 across, 2 up, 1 deep (tools/barn_tools.py TOOL_RACK["size"]); the rail is on part 1. */
	private static final int[][] CELLS = box(1, 2, 1);
	private static final int RAIL = 1;
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {1.0, 0.0, 10.5, 15.0, 28.0, 16.0});

	public ToolRackBlock(Properties properties) {
		super(properties);
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING))[part(state)];
	}

	/** Whether the rail's block at {@code railPos}, facing {@code facing}, has a sturdy wall behind it. */
	private static boolean hung(LevelReader level, BlockPos railPos, Direction facing) {
		BlockPos wall = railPos.relative(facing.getOpposite());
		return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
	}

	/** Stood against the wall the player aims at (or the nearest they look towards), the block above free. */
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState placed = super.getStateForPlacement(context);
		if (placed == null) {
			return null;
		}
		for (Direction direction : context.getNearestLookingDirections()) {
			if (direction.getAxis().isHorizontal()) {
				Direction facing = direction.getOpposite();
				if (hung(context.getLevel(), partPos(context.getClickedPos(), facing, RAIL), facing)) {
					return placed.setValue(FACING, facing);
				}
			}
		}
		return null;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return part(state) != RAIL || hung(level, pos, state.getValue(FACING));
	}

	/** The rail comes off when its wall goes, and the rest with it. */
	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighbor, RandomSource random) {
		return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
				: super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
	}

	/** Nothing to light. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return InteractionResult.PASS;
	}
}
