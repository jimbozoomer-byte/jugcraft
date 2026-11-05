package io.github.jimbozoomer.jugcraft.pharmacology;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** Validates twice: starting a drink never reserves a dose, and a rejected finish spends no item. */
public final class DrugDrinkItem extends Item {
	private final DrugProfile profile;

	public DrugDrinkItem(Properties properties, DrugProfile profile) {
		super(properties);
		this.profile = profile;
	}

	public DrugProfile profile() {
		return profile;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer serverPlayer && !Pharmacology.mayDrink(serverPlayer, profile, true)) {
			return InteractionResult.FAIL;
		}
		return super.use(level, player, hand);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (stack.isEmpty() || !stack.is(this) || !(entity instanceof Player)) {
			return stack;
		}
		if (entity instanceof ServerPlayer player && !Pharmacology.drink(player, profile)) {
			return stack;
		}
		return super.finishUsingItem(stack, level, entity);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.translatable("tooltip.jugcraft." + BuiltInRegistries.ITEM.getKey(this).getPath())
				.withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("tooltip.jugcraft.drug.timing", profile.durationSeconds(),
				profile.onsetSeconds(), profile.strain(), Pharmacology.MAX_STRAIN).withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("tooltip.jugcraft.drug.redosing").withStyle(ChatFormatting.DARK_GRAY));
		if (profile.family().equals("stimulant")) {
			tooltip.accept(Component.translatable("tooltip.jugcraft.drug.stimulant_redosing").withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}
