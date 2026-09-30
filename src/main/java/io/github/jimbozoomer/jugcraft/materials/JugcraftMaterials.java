package io.github.jimbozoomer.jugcraft.materials;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;

/** Jugcraft's canonical metals. IDs here are permanent once released. */
public final class JugcraftMaterials {
	public static MetalFamily TIN;
	public static MetalFamily BRONZE;

	private JugcraftMaterials() {
	}

	public static void register() {
		TIN = MetalFamily.builder("tin").mined().build();
		BRONZE = MetalFamily.builder("bronze").extraItem("bronze_blend").build();

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
			entries.accept(TIN.ore);
			entries.accept(TIN.deepslateOre);
			entries.accept(TIN.rawBlock);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
			entries.accept(TIN.raw);
			entries.accept(TIN.ingot);
			entries.accept(TIN.nugget);
			BRONZE.extras.forEach(entries::accept);
			entries.accept(BRONZE.ingot);
			entries.accept(BRONZE.nugget);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
			entries.accept(TIN.storageBlock);
			entries.accept(BRONZE.storageBlock);
		});
	}
}
