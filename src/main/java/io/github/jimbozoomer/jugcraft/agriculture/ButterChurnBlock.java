package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Butter Churn (Barn & Harvest Tools, part 2; tools/barn_tools.py CHURN): a stave dash churn with iron hoops and a
 * lid. Use a bucket of milk on it to pour it in (the bucket comes back; {@link #FULL} says it holds milk). Use it with an
 * empty hand to work the dasher down and up once; the {@value #STROKES}th stroke churns the milk into {@value #BUTTER}
 * Butter, which falls out at its front. A stroke takes {@value #STROKE_TICKS} ticks, and the next waits for it. Broken
 * with milk in it, the milk is spilt (the bucket already came back). Everything changes on the server; the dasher is
 * drawn moving by client/ButterChurnRenderer from the block's {@code runner} model.
 */
public class ButterChurnBlock extends HandWorkedBlock {
	public static final int STROKES = 8;
	public static final int BUTTER = 2;
	public static final int STROKE_TICKS = 10;
	public static final BooleanProperty FULL = BooleanProperty.create("full");
	private static final VoxelShape SHAPE = Block.box(4.75, 0.0, 4.75, 11.25, 13.5, 11.25);

	public ButterChurnBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FULL, false));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ButterChurnBlockEntity(pos, state);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	/** A bucket of milk goes in; anything else does what it would on any block, and an empty hand works the dasher. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!stack.is(Items.MILK_BUCKET)) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (state.getValue(FULL)) {
			if (!level.isClientSide()) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.butter_churn.full"));
			}
			return InteractionResult.CONSUME;
		}
		if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof ButterChurnBlockEntity churn)) {
			return InteractionResult.SUCCESS;
		}
		if (!churn.fill()) {
			return InteractionResult.CONSUME;
		}
		player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
		level.setBlock(pos, state.setValue(FULL, true), Block.UPDATE_CLIENTS);
		level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8F, 1.0F);
		level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
		return InteractionResult.SUCCESS;
	}

	/** Works the dasher down and up once. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof ButterChurnBlockEntity churn)) {
			return InteractionResult.SUCCESS;
		}
		long now = level.getGameTime();
		if (!churn.ready(now)) {
			return InteractionResult.CONSUME;
		}
		ItemStack out = churn.plunge(now);
		level.playSound(null, pos, churn.milk() || !out.isEmpty() ? SoundEvents.GENERIC_SPLASH : SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.4F,
				0.9F + level.getRandom().nextFloat() * 0.2F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		if (!out.isEmpty()) {
			Block.popResourceFromFace(level, pos, state.getValue(FACING), out);
			level.setBlock(pos, state.setValue(FULL, false), Block.UPDATE_CLIENTS);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FULL);
	}
}
