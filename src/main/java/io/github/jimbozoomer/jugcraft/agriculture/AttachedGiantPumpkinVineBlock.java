package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A full-grown {@link GiantPumpkinVineBlock} holding its fruit ({@link #FACING}). While it holds it, the
 * giant pumpkin grows; when the pumpkin is harvested it turns back into a full-grown vine, which sets another.
 */
public class AttachedGiantPumpkinVineBlock extends VegetationBlock {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	private static final Map<Direction, VoxelShape> SHAPES = Map.of(
			Direction.NORTH, Block.box(5.0, 0.0, 0.0, 11.0, 12.0, 11.0),
			Direction.SOUTH, Block.box(5.0, 0.0, 5.0, 11.0, 12.0, 16.0),
			Direction.WEST, Block.box(0.0, 0.0, 5.0, 11.0, 12.0, 11.0),
			Direction.EAST, Block.box(5.0, 0.0, 5.0, 16.0, 12.0, 11.0));

	public AttachedGiantPumpkinVineBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(BlockTags.SUPPORTS_STEM_CROPS);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction == state.getValue(FACING) && !(neighborState.getBlock() instanceof GiantPumpkinBlock)) {
			return JugcraftAgriculture.block("giant_pumpkin_vine").defaultBlockState().setValue(GiantPumpkinVineBlock.AGE, GiantPumpkinVineBlock.MAX_AGE);
		}
		return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(JugcraftAgriculture.item("giant_pumpkin_seeds"));
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
		builder.add(FACING);
	}
}
