package io.github.jimbozoomer.jugcraft.horses;

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
 * A horse breed's spawn egg: a vanilla spawn egg whose tooltip says what the breed is good and bad at, where its wild
 * herds live, and (Shift) its traits, as the Pegasus egg does (gear/TraitTooltips, docs/features/trait-details.md).
 */
public final class BreedSpawnEggItem extends SpawnEggItem {
	private final HorseBreed breed;

	public BreedSpawnEggItem(Item.Properties properties, HorseBreed breed) {
		super(properties);
		this.breed = breed;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> lines,
			TooltipFlag flag) {
		super.appendHoverText(stack, context, display, lines, flag);
		describe(breed, lines);
	}

	/** The breed's role, home and traits, as tooltip lines. */
	public static void describe(HorseBreed breed, Consumer<Component> lines) {
		String key = "tooltip.jugcraft.horse_breeds." + breed.id();
		lines.accept(Component.translatable(key).withStyle(ChatFormatting.GRAY));
		TraitTooltips traits = TraitTooltips.of(lines);
		traits.trait("tooltip.jugcraft.horse_breeds.traits", ChatFormatting.GOLD, Component.translatable(key + ".good"),
				Component.translatable(key + ".bad"), Component.translatable(key + ".home"));
		if (breed.cargoRows > 0) {
			traits.trait("tooltip.jugcraft.horse_breeds.bags", ChatFormatting.AQUA, Component.translatable("tooltip.jugcraft.horse_breeds.bags.how"));
		}
		traits.end();
	}
}
