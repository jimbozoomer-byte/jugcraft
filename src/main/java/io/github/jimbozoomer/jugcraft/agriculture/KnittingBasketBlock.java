package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * The Knitting Basket (Cozy Fall Hearth, part 4; tools/hearth.py BASKET): a round wicker basket 37 cm across and 22 cm
 * high with a bentwood handle over it. Use a ball of yarn on it to put it in, up to {@value #BALLS}, each drawn in its own
 * colour by client/KnittingBasketRenderer; use knitting needles on it to stick them in a ball ({@link #NEEDLES}, drawn by
 * its model). An empty hand takes back the needles, then the ball put in last. Broken, it gives it all back
 * ({@link KnittingBasketBlockEntity}). It knits nothing: the needles do that in the hand.
 */
public class KnittingBasketBlock extends FacingDecorationBlock implements EntityBlock {
	public static final int BALLS = 4;
	public static final BooleanProperty NEEDLES = BooleanProperty.create("needles");

	public KnittingBasketBlock(Properties properties) {
		super(properties, new double[] {5.0, 0.0, 5.0, 11.0, 7.0, 11.0});
		registerDefaultState(defaultBlockState().setValue(NEEDLES, false));
	}

	/** Yarn or needles go in; anything else does what it would on any block. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!Knitting.isYarn(stack) && !stack.is(JugcraftAgriculture.item("knitting_needles"))) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof KnittingBasketBlockEntity basket) {
			if (!basket.put(stack)) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.knitting_basket.full"));
				return InteractionResult.SUCCESS;
			}
			stack.consume(1, player);
			level.setBlock(pos, state.setValue(NEEDLES, !basket.needles().isEmpty()), Block.UPDATE_ALL);
			level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/** An empty hand takes back the needles, then the ball put in last. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof KnittingBasketBlockEntity basket) || basket.yarn().isEmpty() && basket.needles().isEmpty()) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		ItemStack taken = basket.takeLast();
		if (!player.getInventory().add(taken)) {
			Block.popResource(level, pos.above(), taken);
		}
		level.setBlock(pos, state.setValue(NEEDLES, !basket.needles().isEmpty()), Block.UPDATE_ALL);
		level.playSound(null, pos, SoundEvents.WOOL_HIT, SoundSource.BLOCKS, 0.8F, 1.0F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KnittingBasketBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(NEEDLES);
	}
}
