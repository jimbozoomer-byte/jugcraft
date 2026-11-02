package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
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
 * A piece of the Halloween Party Table that holds a food (tools/party_table.py; part 2: the Spider Cupcake Tower and the
 * Ghost Meringue Platter). It stands facing whoever places it ({@link FacingDecorationBlock}) and holds up to
 * {@link #capacity} of its food ({@link #servings}, in its block state, so every client sees how many). The food used on
 * it goes on, one at a time; an empty hand takes one off. Broken, it drops itself and what it holds (its loot table).
 * On a Feast Table, whose top is lower than a block, it is set down {@value #TABLE_DROP} pixels onto it
 * ({@link #ON_TABLE}: its models' {@code _table} copies, and its shape).
 */
public abstract class PartyServingBlock extends FacingDecorationBlock {
	public static final BooleanProperty ON_TABLE = BooleanProperty.create("on_table");
	/** How far below its block a Feast Table's top is (pixels): tools/party_table.py TABLE. */
	public static final float TABLE_DROP = 3.0F;
	private final String serves;
	private final Map<Direction, VoxelShape> lowered = new EnumMap<>(Direction.class);

	/** {@code box}: its shape facing north (pixels); {@code serves}: the ID of the food it holds. */
	protected PartyServingBlock(Properties properties, double[] box, String serves) {
		super(properties, box);
		this.serves = serves;
		double[] down = {box[0], box[1] - TABLE_DROP, box[2], box[3], box[4] - TABLE_DROP, box[5]};
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			lowered.put(facing, LongDecorationBlock.shape(new double[][] {down}, facing));
		}
		registerDefaultState(defaultBlockState().setValue(ON_TABLE, false).setValue(servings(), 0));
	}

	/** How many of its food it holds, 0 to {@link #capacity}. */
	public abstract IntegerProperty servings();

	/** The most it holds. */
	public abstract int capacity();

	/** The food it holds. */
	public Item serves() {
		return JugcraftAgriculture.item(serves);
	}

	/** Whether {@code below} is a Feast Table, to be set down onto. */
	public static boolean table(BlockState below) {
		return below.getBlock() instanceof FeastTableBlock;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(ON_TABLE) ? lowered.get(state.getValue(FACING)) : super.getShape(state, level, pos, context);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		return state == null ? null : state.setValue(ON_TABLE, table(context.getLevel().getBlockState(context.getClickedPos().below())));
	}

	/** Set down onto a Feast Table put under it, or back up when it goes. */
	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighbor, RandomSource random) {
		return direction == Direction.DOWN ? state.setValue(ON_TABLE, table(neighbor)) : state;
	}

	/** Its food goes on, one at a time; anything else is an empty hand. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!stack.is(serves())) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		int held = state.getValue(servings());
		if (held >= capacity()) {
			return InteractionResult.FAIL;
		}
		if (level instanceof ServerLevel server) {
			server.setBlock(pos, state.setValue(servings(), held + 1), Block.UPDATE_ALL);
			stack.consume(1, player);
			server.playSound(null, pos, SoundEvents.BUNDLE_INSERT, SoundSource.BLOCKS, 0.6F, 1.3F);
			server.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/** An empty hand takes one off, into the player's inventory (or at their feet). */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		int held = state.getValue(servings());
		if (held <= 0) {
			return InteractionResult.PASS;
		}
		if (level instanceof ServerLevel server) {
			server.setBlock(pos, state.setValue(servings(), held - 1), Block.UPDATE_ALL);
			ItemStack one = new ItemStack(serves());
			if (!player.getInventory().add(one)) {
				Block.popResource(server, pos, one);
			}
			server.playSound(null, pos, SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.BLOCKS, 0.6F, 1.3F);
			server.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(ON_TABLE, servings());
	}
}
