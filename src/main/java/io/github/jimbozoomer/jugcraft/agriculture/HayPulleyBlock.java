package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Hay-Loft Pulley (Barn & Harvest Tools, part 4; tools/barn_tools.py PULLEY): a braced beam out from a wall with an
 * iron pulley block at its end, a rope over its wheel down to a hay hook, as a barn's hay hood has. It hangs on a sturdy
 * wall, facing out from it, and comes down when the wall goes. Its rope hangs {@link #DROP} blocks down, one to
 * {@value #DROPS}: use it with an empty hand to let it down a block, or sneaking to haul it up a block. A haul takes
 * {@value #HAUL_TICKS} ticks, the wheel turning and the rope running, and the next waits for it. The rope and hook are
 * drawn only (client/HayPulleyRenderer, from the blockstate's {@link #PIECE} models, which a placed pulley never takes):
 * nothing collides with them.
 */
public class HayPulleyBlock extends BaseEntityBlock {
	public static final int DROPS = 4;
	public static final int HAUL_TICKS = 20;
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty DROP = IntegerProperty.create("drop", 1, DROPS);
	public static final EnumProperty<Piece> PIECE = EnumProperty.create("piece", Piece.class);
	/** The beam and the plate, facing north, east, south and west. */
	private static final VoxelShape[] SHAPES = {Block.box(5.5, 9.0, 1.0, 10.5, 15.5, 16.0), Block.box(0.0, 9.0, 5.5, 15.0, 15.5, 10.5),
			Block.box(5.5, 9.0, 0.0, 10.5, 15.5, 15.0), Block.box(1.0, 9.0, 5.5, 16.0, 15.5, 10.5)};

	/** Which model the blockstate names: the pulley itself, or one of the pieces its renderer moves. */
	public enum Piece implements StringRepresentable {
		BLOCK, WHEEL, ROPE, HOOK;

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	public HayPulleyBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(DROP, 1).setValue(PIECE, Piece.BLOCK));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new HayPulleyBlockEntity(pos, state);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES[switch (state.getValue(FACING)) {
			case EAST -> 1;
			case SOUTH -> 2;
			case WEST -> 3;
			default -> 0;
		}];
	}

	/** Hung on the wall the player faces (or the nearest one that holds it), facing out from it. */
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

	/** Only an empty hand works the rope; anything else does what it would on any block. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
	}

	/** Lets the rope down a block, or (sneaking) hauls it up a block. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		int drop = state.getValue(DROP);
		int next = Math.clamp(drop + (player.isSecondaryUseActive() ? -1 : 1), 1, DROPS);
		if (next == drop) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof HayPulleyBlockEntity pulley)) {
			return InteractionResult.SUCCESS;
		}
		long now = level.getGameTime();
		if (!pulley.ready(now)) {
			return InteractionResult.CONSUME;
		}
		level.setBlock(pos, state.setValue(DROP, next), Block.UPDATE_CLIENTS);
		pulley.haul(now, drop);
		level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.6F, next > drop ? 0.8F : 1.0F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
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
		builder.add(FACING, DROP, PIECE);
	}
}
