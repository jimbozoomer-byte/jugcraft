package io.github.jimbozoomer.jugcraft.lair;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * The Hessian's Bridle: used at night, out of the Halloween season, on a jack-o'-lantern standing on a fence or wall post
 * under the open sky, it opens the Church Bridge ({@link HessiansBridleRite}) and is used up. Used on anything else, or in
 * the air, it says what it wants. Every use on a lantern on a post, answered or not, rests the bridle for
 * {@value HessiansBridleRite#COOLDOWN} ticks.
 */
public class HessiansBridleItem extends Item {
	public HessiansBridleItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (!(context.getLevel() instanceof ServerLevel level) || !(context.getPlayer() instanceof ServerPlayer player)) {
			return InteractionResult.SUCCESS;
		}
		BlockPos pos = context.getClickedPos();
		if (!HessiansBridleRite.lanternOnPost(level, pos)) {
			player.sendOverlayMessage(HessiansBridleRite.message(HessiansBridleRite.Missing.NOT_LANTERN));
			return InteractionResult.SUCCESS;
		}
		ItemStack bridle = context.getItemInHand();
		player.getCooldowns().addCooldown(bridle, HessiansBridleRite.COOLDOWN);
		HessiansBridleRite.bridle(level, player, pos, bridle);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendOverlayMessage(HessiansBridleRite.message(HessiansBridleRite.Missing.NOT_LANTERN));
		}
		return InteractionResult.PASS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
			TooltipFlag flag) {
		tooltip.accept(Component.translatable("tooltip.jugcraft.hessians_bridle").withStyle(ChatFormatting.GRAY));
	}
}
