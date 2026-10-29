package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * A prop of several blocks (Orchard & Apple Harvest, decor set 2; {@link MultiDecorationBlock}) that holds picked fruit
 * ({@link FruitStore}): the Apple Bin and the Orchard Handcart. Use fruit on any of its blocks to put in all you hold
 * that fits; use it with an empty hand to take back a stack. Its first block keeps the fruit; breaking it spills them.
 * Its load rises through {@value #STAGES} stages ({@link #FILL}), shown with the fruit on top on every block it has. It
 * has no lanterns ({@link #LIT} stays on).
 */
public abstract class FruitBinBlock extends MultiDecorationBlock implements EntityBlock, FruitStore {
	public static final int STAGES = 4;
	public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, STAGES);

	protected FruitBinBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FILL, 0).setValue(KIND, Kind.APPLE));
	}

	/** Each part's shape for each facing. */
	protected abstract Map<Direction, VoxelShape[]> shapes();

	@Override
	public int stages() {
		return STAGES;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shapes().get(state.getValue(FACING))[part(state)];
	}

	/** The prop's fruit, on its first block, from any of its blocks. */
	public @Nullable FruitStoreBlockEntity store(Level level, BlockPos pos, BlockState state) {
		return level.getBlockEntity(masterPos(pos, state)) instanceof FruitStoreBlockEntity store ? store : null;
	}

	@Override
	public void show(Level level, BlockPos pos, BlockState state, int fill, Kind fruit) {
		Direction facing = state.getValue(FACING);
		for (int part = 0; part < cells().length; part++) {
			BlockPos at = partPos(pos, facing, part);
			BlockState there = level.getBlockState(at);
			if (there.is(this) && part(there) == part && there.getValue(FACING) == facing) {
				level.setBlock(at, there.setValue(FILL, fill).setValue(KIND, fruit), Block.UPDATE_CLIENTS);
			}
		}
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		FruitStoreBlockEntity store = store(level, pos, state);
		return store == null ? InteractionResult.TRY_WITH_EMPTY_HAND : FruitStore.putIn(store, stack, level, pos, player);
	}

	/** Takes back the stack that went in last (it has no lanterns to light). */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		FruitStoreBlockEntity store = store(level, pos, state);
		return store == null ? InteractionResult.PASS : FruitStore.takeOut(store, level, pos, player, state.getValue(FILL) > 0);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new FruitStoreBlockEntity(pos, state) : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FILL, KIND);
	}
}
