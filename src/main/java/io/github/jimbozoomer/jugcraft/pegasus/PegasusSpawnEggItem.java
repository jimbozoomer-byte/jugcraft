package io.github.jimbozoomer.jugcraft.pegasus;

import io.github.jimbozoomer.jugcraft.gear.TraitTooltips;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * The Pegasus's spawn egg: a vanilla spawn egg whose tooltip teaches the flight controls and its light pack. The key names are
 * keybind components, so the tooltip shows whatever each control is bound to now; the steps show while Shift is held
 * (gear/TraitTooltips, docs/features/trait-details.md).
 */
public final class PegasusSpawnEggItem extends SpawnEggItem {
	public PegasusSpawnEggItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> lines,
			TooltipFlag flag) {
		super.appendHoverText(stack, context, display, lines, flag);
		controls(lines);
	}

	/** The controls, as tooltip lines (shared with anything else that explains the Pegasus). */
	public static void controls(Consumer<Component> lines) {
		lines.accept(Component.translatable("tooltip.jugcraft.pegasus.egg").withStyle(ChatFormatting.GRAY));
		TraitTooltips traits = TraitTooltips.of(lines);
		traits.trait("tooltip.jugcraft.pegasus.controls.trait", ChatFormatting.AQUA,
				Component.translatable("tooltip.jugcraft.pegasus.controls.saddle"),
				Component.translatable("tooltip.jugcraft.pegasus.controls.takeoff", Component.keybind("key.jump"),
						Component.keybind(JugcraftPegasus.ASCEND_KEY)),
				Component.translatable("tooltip.jugcraft.pegasus.controls.fly", Component.keybind(JugcraftPegasus.ASCEND_KEY),
						Component.keybind(JugcraftPegasus.DESCEND_KEY)),
				Component.translatable("tooltip.jugcraft.pegasus.controls.steer", Component.keybind("key.forward"),
						Component.keybind("key.left"), Component.keybind("key.back"), Component.keybind("key.right"),
						Component.keybind("key.sneak")));
		traits.trait("tooltip.jugcraft.pegasus.pack.trait", ChatFormatting.GOLD, Component.translatable("tooltip.jugcraft.pegasus.pack.fit"),
				Component.translatable("tooltip.jugcraft.pegasus.pack.open"));
		traits.end();
	}
}
