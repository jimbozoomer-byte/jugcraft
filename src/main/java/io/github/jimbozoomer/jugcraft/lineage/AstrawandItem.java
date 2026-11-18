package io.github.jimbozoomer.jugcraft.lineage;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * The Astrawand (docs/features/star-bred-lines.md): a slender conductor's baton, a fan homage to the Wind Waker of The
 * Legend of Zelda with its own name, its icon and model drawn for Jugcraft. It only reads: pointed at an animal and used,
 * it tells its line ({@link Lineage#read}) and changes nothing. Anyone may use it; the reading deepens with the reader's
 * Celestial Attunement.
 */
public class AstrawandItem extends Item {
	public AstrawandItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		if (!(target instanceof Animal animal)) {
			return InteractionResult.PASS;
		}
		if (player instanceof ServerPlayer reader) {
			Lineage.read(reader, animal);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.translatable("tooltip.jugcraft.astrawand").withStyle(ChatFormatting.GRAY));
	}
}
