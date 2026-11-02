package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Harvest Bell (Barn & Harvest Tools, part 4; tools/barn_tools.py BELL): a farm bell hung in an iron yoke on a post,
 * on a stone footing, one block across and two up, the farm's call in from the fields. Use either block with an empty
 * hand to ring it: it swings and rings, the swing dying away over {@value #SWING_TICKS} ticks, and it can't be rung again
 * before (it says so). The bell is drawn swinging by client/HandWorkedRenderer from the blockstate's {@link #RUNNER}
 * model of its upper block, which a placed bell never takes; the ring is the server's, heard by everyone near.
 */
public class HarvestBellBlock extends MultiDecorationBlock implements EntityBlock {
	public static final int SWING_TICKS = 40;
	/** Its blocks: 1 across, 2 up, 1 deep (tools/barn_tools.py BELL["size"]). */
	private static final int[][] CELLS = box(1, 2, 1);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	public static final BooleanProperty RUNNER = BooleanProperty.create("runner");
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {4.0, 0.0, 4.0, 12.0, 2.0, 12.0},
			new double[] {7.0, 2.0, 7.0, 9.0, 23.5, 9.0}, new double[] {4.0, 23.5, 5.0, 12.0, 30.5, 11.0});

	public HarvestBellBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(RUNNER, false));
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

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new HarvestBellBlockEntity(pos, state) : null;
	}

	/** Only an empty hand rings it; anything else does what it would on any block. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
	}

	/** Rings it, unless it is still swinging from the last ring. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide() || !(level.getBlockEntity(masterPos(pos, state)) instanceof HarvestBellBlockEntity bell)) {
			return InteractionResult.SUCCESS;
		}
		long now = level.getGameTime();
		if (!bell.ready(now)) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.harvest_bell.ringing"));
			return InteractionResult.CONSUME;
		}
		bell.ring(now);
		BlockPos top = partPos(masterPos(pos, state), state.getValue(FACING), 1);
		level.playSound(null, top, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 1.2F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, top);
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNER);
	}
}
