package io.github.jimbozoomer.jugcraft.lair.roc;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import io.github.jimbozoomer.jugcraft.weapons.DescribedItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.Equippable;

/**
 * The Storm Roc's registrations (docs/features/storm-roc.md): him and his Gale Fledglings; his loot (the Roc Feather, the
 * Weather-Vane Lens and the Roc's Crest; the Stormcaller and the Galefeather are Arms VII trophies,
 * weapons/ArmVariants.java, with their Gale boon). He is set in the nest of every Crown of Thunder as it is placed.
 */
public final class JugcraftRoc {
	public static EntityType<StormRocEntity> STORM_ROC;
	public static EntityType<GaleFledglingEntity> GALE_FLEDGLING;
	public static Item ROC_FEATHER;
	public static Item WEATHER_VANE_LENS;
	public static Item ROCS_CREST;

	private JugcraftRoc() {
	}

	public static void register() {
		STORM_ROC = entity("storm_roc", EntityType.Builder.<StormRocEntity>of(StormRocEntity::new, MobCategory.MONSTER)
				.sized(2.0F, 2.2F).eyeHeight(1.9F).noLootTable().clientTrackingRange(12).updateInterval(1));
		FabricDefaultAttributeRegistry.register(STORM_ROC, StormRocEntity.createAttributes());
		GALE_FLEDGLING = entity("gale_fledgling", EntityType.Builder.<GaleFledglingEntity>of(GaleFledglingEntity::new, MobCategory.MONSTER)
				.sized(0.9F, 0.8F).eyeHeight(0.6F).noLootTable().noSave().clientTrackingRange(10).updateInterval(1));
		FabricDefaultAttributeRegistry.register(GALE_FLEDGLING, GaleFledglingEntity.createAttributes());

		// Each says what it is for in a grey line (tooltip.jugcraft.<id>).
		ROC_FEATHER = item("roc_feather", new Item.Properties().rarity(Rarity.UNCOMMON));
		WEATHER_VANE_LENS = item("weather_vane_lens", new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
		// Worn on the head, it is drawn there as its own model (its "head" display), as the costume hats are.
		ROCS_CREST = item("rocs_crest", new Item.Properties().stacksTo(1).rarity(Rarity.RARE).component(
				DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER).build()));

		Lairs.onPlaced(Lair.CROWN_OF_THUNDER, StormRocEntity::summon);
		WeatherVaneLens.register();
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> output.accept(ROC_FEATHER));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> {
			output.accept(WEATHER_VANE_LENS);
			output.accept(ROCS_CREST);
		});
	}

	/** The Stormcaller and the Galefeather, registered with the other Arms VII trophies (weapons/ArmVariants.java). */
	public static Item stormcaller() {
		return BuiltInRegistries.ITEM.getValue(Jugcraft.id("stormcaller"));
	}

	public static Item galefeather() {
		return BuiltInRegistries.ITEM.getValue(Jugcraft.id("galefeather"));
	}

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> entity(String id, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Jugcraft.id(id));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	private static Item item(String id, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Jugcraft.id(id));
		return Registry.register(BuiltInRegistries.ITEM, key, new DescribedItem(properties.setId(key)));
	}
}
