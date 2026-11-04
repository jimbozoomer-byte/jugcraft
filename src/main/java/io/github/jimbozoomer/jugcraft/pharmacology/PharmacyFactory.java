package io.github.jimbozoomer.jugcraft.pharmacology;

import io.github.jimbozoomer.jugcraft.materials.JugcraftRegistry;
import io.github.jimbozoomer.jugcraft.weapons.DescribedItem;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/** Stable factory inputs and reusable tools. All processing is owned by the shared formed-machine backend. */
public final class PharmacyFactory {
	private static final Map<String, Item> ITEMS = new LinkedHashMap<>();

	private PharmacyFactory() {
	}

	public static void register() {
		for (String id : List.of("blank_formula_plate", "pharmaceutical_base", "caffeine_formula_plate",
				"aspirin_formula_plate", "paracetamol_formula_plate", "rehydration_salts_formula_plate",
				"activated_charcoal_formula_plate", "stimulant_formula_plate")) {
			ITEMS.put(id, JugcraftRegistry.item(id, DescribedItem::new));
		}
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
				.register(entries -> ITEMS.values().forEach(entries::accept));
	}

	public static Item item(String id) {
		Item item = ITEMS.get(id);
		if (item == null) {
			throw new IllegalArgumentException("Unknown pharmacy factory item " + id);
		}
		return item;
	}
}
