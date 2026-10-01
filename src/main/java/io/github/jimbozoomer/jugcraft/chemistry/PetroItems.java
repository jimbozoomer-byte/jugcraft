package io.github.jimbozoomer.jugcraft.chemistry;

import io.github.jimbozoomer.jugcraft.materials.JugcraftRegistry;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/** Plain items of the oil line (ITEMS in tools/petro.py). */
public final class PetroItems {
	/** Used up by the catalytic cracker, one per bucket of heavy fuel oil. */
	public static Item CRACKING_CATALYST;

	private PetroItems() {
	}

	public static void register() {
		CRACKING_CATALYST = JugcraftRegistry.item("cracking_catalyst");

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
			output.accept(CRACKING_CATALYST);
		});
	}
}
