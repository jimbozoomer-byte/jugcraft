package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Prize Ribbon Board (the Harvest Festival Grounds, decor set 10, part 2; tools/harvest_festival.py RIBBON_BOARD): a
 * show's results board, green-painted planks on two posts 1.6 m tall, its three brass cup hooks numbered 1, 2 and 3 from
 * the left as you face it. Two blocks tall ({@link TallDecorationBlock}). Use a First, Second or Third Prize Ribbon on
 * it (the Harvest Scale's, {@link HarvestScaleBlockEntity#RIBBONS}) to hang it on its own hook ({@link #FIRST},
 * {@link #SECOND}, {@link #THIRD}, kept on both halves); an empty hand takes back the hung ribbon nearest where you aim.
 * Broken, the board drops itself and every ribbon hanging on it (its loot table). Hanging and taking change the world,
 * so they need build permission. Its model is tools/harvest_festival_data.py ribbon_board(), each ribbon drawn by its
 * own model when it hangs.
 */
public class PrizeRibbonBoardBlock extends TallDecorationBlock {
	public static final BooleanProperty FIRST = BooleanProperty.create("first");
	public static final BooleanProperty SECOND = BooleanProperty.create("second");
	public static final BooleanProperty THIRD = BooleanProperty.create("third");
	/** The hooks, 1 to 3 from the left as you face the board, and the ribbon each takes. */
	public static final List<BooleanProperty> HOOKS = List.of(FIRST, SECOND, THIRD);
	/** The posts and the foot of the board (lower half), the board and its posts' tops (upper half), facing north. */
	private static final double[][] LOWER = {{1.5, 0.0, 8.5, 3.0, 16.0, 10.0}, {13.0, 0.0, 8.5, 14.5, 16.0, 10.0}, {0.5, 14.0, 6.5, 15.5, 16.0, 8.5}};
	private static final double[][] UPPER = {{0.5, 0.0, 6.5, 15.5, 9.0, 8.5}, {1.25, 0.0, 8.25, 3.25, 10.5, 10.25},
			{12.75, 0.0, 8.25, 14.75, 10.5, 10.25}};
	private static final Map<Direction, VoxelShape> LOWER_SHAPES = new EnumMap<>(Direction.class);
	private static final Map<Direction, VoxelShape> UPPER_SHAPES = new EnumMap<>(Direction.class);

	static {
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			LOWER_SHAPES.put(facing, LongDecorationBlock.shape(LOWER, facing));
			UPPER_SHAPES.put(facing, LongDecorationBlock.shape(UPPER, facing));
		}
	}

	public PrizeRibbonBoardBlock(Properties properties) {
		super(properties, LOWER_SHAPES.get(Direction.NORTH), UPPER_SHAPES.get(Direction.NORTH));
		registerDefaultState(defaultBlockState().setValue(FIRST, false).setValue(SECOND, false).setValue(THIRD, false));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return (state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPES : UPPER_SHAPES).get(state.getValue(FACING));
	}

	/** The hook a ribbon item hangs on, or -1 if {@code stack} is not a prize ribbon. */
	public static int hookFor(ItemStack stack) {
		for (int hook = 0; hook < HOOKS.size(); hook++) {
			if (stack.is(JugcraftAgriculture.item(HarvestScaleBlockEntity.RIBBONS.get(hook)))) {
				return hook;
			}
		}
		return -1;
	}

	/** The hook (0 to 2, from the left as you face the board) nearest where {@code hit} is across it. */
	public static int hookAt(BlockState state, BlockPos pos, Vec3 hit) {
		Direction right = state.getValue(FACING).getCounterClockWise();
		double across = (hit.x - (pos.getX() + 0.5)) * right.getStepX() + (hit.z - (pos.getZ() + 0.5)) * right.getStepZ();
		return Mth.clamp(Mth.floor((across + 0.5) * HOOKS.size()), 0, HOOKS.size() - 1);
	}

	/** A prize ribbon hangs on its own hook, if that is free, using it up. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		int hook = hookFor(stack);
		if (hook < 0) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!player.mayBuild() || !level.mayInteract(player, pos)) {
			return InteractionResult.FAIL;
		}
		if (state.getValue(HOOKS.get(hook))) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.prize_ribbon_board.taken"));
			return InteractionResult.SUCCESS;
		}
		setBoth(level, pos, state.setValue(HOOKS.get(hook), true));
		stack.consume(1, player);
		level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** An empty hand takes back the hung ribbon nearest the hook it aims at. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		int aimed = hookAt(state, pos, hit.getLocation());
		int hook = -1;
		for (int off = 0; off < HOOKS.size() && hook < 0; off++) {
			for (int at : new int[] {aimed - off, aimed + off}) {
				if (hook < 0 && at >= 0 && at < HOOKS.size() && state.getValue(HOOKS.get(at))) {
					hook = at;
				}
			}
		}
		if (hook < 0) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!player.mayBuild() || !level.mayInteract(player, pos)) {
			return InteractionResult.FAIL;
		}
		setBoth(level, pos, state.setValue(HOOKS.get(hook), false));
		ItemStack ribbon = new ItemStack(JugcraftAgriculture.item(HarvestScaleBlockEntity.RIBBONS.get(hook)));
		if (!player.getInventory().add(ribbon)) {
			Block.popResource(level, pos, ribbon);
		}
		level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FIRST, SECOND, THIRD);
	}
}
