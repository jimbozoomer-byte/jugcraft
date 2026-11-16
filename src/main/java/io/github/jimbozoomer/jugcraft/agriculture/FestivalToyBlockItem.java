package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/**
 * The item of a festival toy that does something when used (FestivalToys.HINTED): an ordinary block item whose tooltip
 * adds one grey line saying what using it does (item.jugcraft.<id>.tooltip), which JEI shows with the item too.
 */
public class FestivalToyBlockItem extends BlockItem {
	private final String hint;

	public FestivalToyBlockItem(Block block, Properties properties, String id) {
		super(block, properties);
		this.hint = "item.jugcraft." + id + ".tooltip";
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		tooltip.accept(Component.translatable(hint).withStyle(ChatFormatting.GRAY));
	}
}
