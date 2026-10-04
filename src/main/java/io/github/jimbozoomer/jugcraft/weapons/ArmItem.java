package io.github.jimbozoomer.jugcraft.weapons;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * An arm (batch 42, {@link JugcraftArms}): a plain item whose traits are all item components, with a grey line saying
 * what its kind does, from {@code tooltip.jugcraft.arms.<kind>}.
 */
public class ArmItem extends Item {
	private final String kind;

	public ArmItem(String kind, Properties properties) {
		super(properties);
		this.kind = kind;
	}

	/** The kind of arm: longsword, greatsword, rapier, flanged_mace, war_hammer, glaive, halberd, spear or lance. */
	public String kind() {
		return kind;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
			TooltipFlag flag) {
		tooltip.accept(Component.translatable("tooltip.jugcraft.arms." + kind).withStyle(ChatFormatting.GRAY));
	}
}
