package io.github.jimbozoomer.jugcraft.client.compat;

import java.util.ArrayList;
import java.util.List;
import io.github.jimbozoomer.jugcraft.agriculture.WoodlandHarvestCrafts;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * JEI information pages for the woodland harvest crafts (tools/thanksgiving_woodland.py INFO; lang keys
 * jei.jugcraft.woodland.&lt;id&gt;): what each piece is, the everyday form it follows, that it is inspired game decor and
 * not a historical reconstruction, and that it is decoration only. Called from JugcraftJeiPlugin.registerRecipes; JEI is
 * optional, so this class is only loaded through that plugin.
 */
public final class WoodlandJeiInfo {
	private WoodlandJeiInfo() {
	}

	public static void addInformation(IRecipeRegistration registration) {
		List<String> pieces = new ArrayList<>(WoodlandHarvestCrafts.FLOOR);
		pieces.add(WoodlandHarvestCrafts.RUSH_FLOOR_MAT);
		for (String id : pieces) {
			Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse("jugcraft:" + id));
			if (item != Items.AIR) {
				registration.addIngredientInfo(item, Component.translatable("jei.jugcraft.woodland." + id));
			}
		}
	}
}
