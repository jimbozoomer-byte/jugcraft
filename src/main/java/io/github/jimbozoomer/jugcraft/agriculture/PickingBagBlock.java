package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Picking Bag (Orchard & Apple Harvest, decor set 2, part 1; tools/orchard_harvest.py BAG): a canvas picking bag
 * with an iron hoop mouth, hung by its harness on an iron hook on a wall. It holds up to {@value #CAPACITY} apples and
 * pears ({@link FruitStore}), showing them heaped in its mouth ({@link #FILL}: empty, some, full). Use fruit on it to put
 * in what fits; use it with an empty hand to take back a stack. It needs a solid wall behind it and comes off (spilling
 * its fruit) when that goes. Its model is tools/orchard_harvest_data.py picking_bag().
 */
public class PickingBagBlock extends BaseEntityBlock implements FruitStore {
	public static final int CAPACITY = 16;
	public static final int STAGES = 2;
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, STAGES);
	/** The bag, its hook and harness, facing north on the wall to its south. */
	private static final double[][] BOX = {{4.25, 2.0, 8.75, 11.75, 15.25, 16.0}};

	public PickingBagBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FILL, 0).setValue(KIND, Kind.APPLE));
	}

	@Override
	public int capacity() {
		return CAPACITY;
	}

	@Override
	public int stages() {
		return STAGES;
	}

	@Override
	public void show(Level level, BlockPos pos, BlockState state, int fill, Kind fruit) {
		level.setBlock(pos, state.setValue(FILL, fill).setValue(KIND, fruit), Block.UPDATE_CLIENTS);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FruitStoreBlockEntity(pos, state);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return LongDecorationBlock.shape(BOX, state.getValue(FACING));
	}

	/** Hung on the wall the player faces (or the nearest one that holds it). */
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		for (Direction direction : context.getNearestLookingDirections()) {
			if (direction.getAxis().isHorizontal()) {
				BlockState state = defaultBlockState().setValue(FACING, direction.getOpposite());
				if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
					return state;
				}
			}
		}
		return null;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		BlockPos behind = pos.relative(facing.getOpposite());
		return level.getBlockState(behind).isFaceSturdy(level, behind, facing);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighbor, RandomSource random) {
		return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
				: super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		return level.getBlockEntity(pos) instanceof FruitStoreBlockEntity store ? FruitStore.putIn(store, stack, level, pos, player)
				: InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return level.getBlockEntity(pos) instanceof FruitStoreBlockEntity store
				? FruitStore.takeOut(store, level, pos, player, state.getValue(FILL) > 0) : InteractionResult.PASS;
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
		builder.add(FACING, FILL, KIND);
	}
}
