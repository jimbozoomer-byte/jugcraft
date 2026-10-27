package io.github.jimbozoomer.jugcraft.lair;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.level.Level;

/**
 * The Glass Accord: a disc of leaded glass round a lens. Held up by day in clear weather, in sunlight that falls through
 * glass, it opens the Glass Chapel ({@link GlassAccordRite}) and is used up. Every use, answered or not, rests the Accord
 * for {@value GlassAccordRite#COOLDOWN} ticks.
 */
public class GlassAccordItem extends Item {
	public GlassAccordItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) {
			ItemStack accord = player.getItemInHand(hand);
			player.getCooldowns().addCooldown(accord, GlassAccordRite.COOLDOWN);
			GlassAccordRite.hold(server, serverPlayer, accord);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
			TooltipFlag flag) {
		tooltip.accept(Component.translatable("tooltip.jugcraft.glass_accord").withStyle(ChatFormatting.GRAY));
	}
}
