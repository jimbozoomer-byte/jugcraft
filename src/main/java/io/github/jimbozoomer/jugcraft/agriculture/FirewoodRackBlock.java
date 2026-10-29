package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * The Firewood Rack (Cozy Fall Hearth, part 3; tools/hearth.py RACK): a wrought-iron log rack 87 cm wide, 37 cm deep and
 * 94 cm high, two hoops on feet joined by rails. Use a log of any kind on it to stack it ({@link FirewoodRackBlockEntity}
 * keeps each as it went in), up to {@value #CAPACITY}; an empty hand takes back the one stacked last. It shows each as a
 * split log, four to a layer ({@link #LOGS}, its models tools/hearth_data.py firewood_rack()). A comparator reads how
 * full it is. Broken, it gives its logs back.
 */
public class FirewoodRackBlock extends FacingDecorationBlock implements EntityBlock {
	public static final int CAPACITY = 16;
	public static final IntegerProperty LOGS = IntegerProperty.create("logs", 0, CAPACITY);

	public FirewoodRackBlock(Properties properties) {
		super(properties, new double[] {1.0, 0.0, 5.0, 15.0, 15.0, 11.0});
		registerDefaultState(defaultBlockState().setValue(LOGS, 0));
	}

	/** A log is stacked on it; anything else does what it would on any block. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!stack.is(ItemTags.LOGS)) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FirewoodRackBlockEntity rack) {
			if (!rack.stack(stack)) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.firewood_rack.full"));
				return InteractionResult.SUCCESS;
			}
			stack.consume(1, player);
			level.setBlock(pos, state.setValue(LOGS, rack.count()), Block.UPDATE_ALL);
			level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 0.9F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/** An empty hand takes back the log stacked last. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (state.getValue(LOGS) == 0) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FirewoodRackBlockEntity rack) {
			ItemStack taken = rack.takeLast();
			if (taken.isEmpty()) {
				return InteractionResult.PASS;
			}
			if (!player.getInventory().add(taken)) {
				Block.popResource(level, pos.above(), taken);
			}
			level.setBlock(pos, state.setValue(LOGS, rack.count()), Block.UPDATE_ALL);
			level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	/** How full it is, 0 to 15 (15 full). */
	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		int logs = level.getBlockEntity(pos) instanceof FirewoodRackBlockEntity rack ? rack.count() : 0;
		return logs == 0 ? 0 : 1 + logs * 14 / CAPACITY;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FirewoodRackBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LOGS);
	}
}
