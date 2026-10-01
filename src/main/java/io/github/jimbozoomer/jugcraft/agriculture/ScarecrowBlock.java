package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * The Scarecrow: a straw figure on a post, two blocks tall, in a flannel shirt. Use any dye on it to
 * change the shirt's colour ({@link #SHIRT}). Its head is whatever is put on top: a pumpkin, a hand-carved
 * pumpkin or a jack o'lantern sits on the post at head height. With a lit one, sneak-used at midnight during the
 * Halloween event, it summons the Headless Horseman ({@link HorsemanSummoning}). Once crop-eating birds exist, it
 * will keep them off nearby fields (docs/branches/AGRICULTURE.md).
 */
public class ScarecrowBlock extends TallDecorationBlock {
	public static final EnumProperty<DyeColor> SHIRT = EnumProperty.create("shirt", DyeColor.class);
	public static final DyeColor DEFAULT_SHIRT = DyeColor.RED;

	public ScarecrowBlock(Properties properties) {
		super(properties, Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0), Block.box(2.0, 0.0, 5.0, 14.0, 16.0, 11.0));
		registerDefaultState(defaultBlockState().setValue(SHIRT, DEFAULT_SHIRT));
	}

	/** The colour of a vanilla dye, or null if the stack is not one. */
	public static @Nullable DyeColor dyeColor(ItemStack stack) {
		for (DyeColor color : DyeColor.values()) {
			if (stack.is(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", color.getSerializedName() + "_dye")))) {
				return color;
			}
		}
		return null;
	}

	/** A dye recolours the shirt (both halves), using up one dye. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		DyeColor color = dyeColor(stack);
		if (color == null || state.getValue(SHIRT) == color) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			if (!player.mayBuild() || !level.mayInteract(player, pos)) {
				return InteractionResult.FAIL;
			}
			setBoth(level, pos, state.setValue(SHIRT, color));
			stack.consume(1, player);
			level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/**
	 * Sneak-use with an empty hand at midnight during the Halloween event, wearing a lit pumpkin for a head, and the
	 * Headless Horseman comes for it ({@link HorsemanSummoning}).
	 */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		}
		if (player instanceof ServerPlayer caller) {
			BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
			HorsemanSummoning.Result result = HorsemanSummoning.summon(caller, lower);
			if (result != HorsemanSummoning.Result.SUMMONED) {
				caller.sendOverlayMessage(Component.translatable("message.jugcraft.horseman." + result.name().toLowerCase(Locale.ROOT)));
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(SHIRT);
	}
}
