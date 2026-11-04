package io.github.jimbozoomer.jugcraft.pharmacology;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.materials.JugcraftRegistry;
import io.github.jimbozoomer.jugcraft.weapons.DescribedItem;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;

/** Named, paid longer-release profiles on the existing attachment and manufacturing system. */
public final class ControlledRelease {
	private static final Map<String, Item> ITEMS = new LinkedHashMap<>();
	public static DrugProfile CAFFEINE_XR, METHYLPHENIDATE_XR, AMPHETAMINE_XR, MODAFINIL_XR, FENTANYL_PATCH, MASTERWORK_RESERVE;
	private ControlledRelease() {}
	public static final class ReleaseDie extends PrecisionPharmacy.ProcessModule {
		public ReleaseDie(Properties properties) { super(properties); }
	}
	private static final class Effect extends MobEffect {
		Effect() { super(MobEffectCategory.BENEFICIAL, 0xA3BDC2); }
	}
	private static DrugProfile register(String id, String family, int duration, int onset, int strain,
			DrugProfile.Benefits benefits, int recovery, DrugProfile.Traits traits, boolean beverage, boolean patch) {
		var profile = Pharmacology.registerProfile(new DrugProfile(id, family, duration, onset, strain,
				Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Jugcraft.id(id), new Effect()), benefits, recovery, false, traits));
		ITEMS.put(id, JugcraftRegistry.item(id, properties -> new DrugDrinkItem(beverage ? Pharmacology.drinkProperties(properties)
				: patch ? properties.stacksTo(16).component(DataComponents.CONSUMABLE, Consumable.builder().consumeSeconds(1.6F)
						.animation(ItemUseAnimation.NONE).sound(SoundEvents.ARMOR_EQUIP_LEATHER).hasConsumeParticles(false).build())
				: Pharmacology.tabletProperties(properties), profile)));
		return profile;
	}
	public static void register() {
		CAFFEINE_XR = register("caffeine_xr", "stimulant", 1800, 30, 10,
				new DrugProfile.Benefits(0, -1, .10, 1, 1, 1), 120, DrugProfile.Traits.NONE, false, false);
		METHYLPHENIDATE_XR = register("methylphenidate_xr", "stimulant", 2400, 30, 30,
				new DrugProfile.Benefits(1, -1, 0, 1, 1, 1), 120, new DrugProfile.Traits(0, 0, 1.2F, 1, .75F, 0), false, false);
		AMPHETAMINE_XR = register("amphetamine_xr", "stimulant", 2100, 30, 40,
				new DrugProfile.Benefits(1, 0, 0, 1, 1, 1), 120, new DrugProfile.Traits(0, 0, 1.3F, 1, 1, 120), false, false);
		MODAFINIL_XR = register("modafinil_xr", "stimulant", 3600, 30, 25,
				new DrugProfile.Benefits(0, -1, 0, 1, .85F, 1), 120, new DrugProfile.Traits(0, 0, 1, 1, .85F, 0), false, false);
		FENTANYL_PATCH = register("fentanyl_patch", "opioid", 1200, 30, 40,
				new DrugProfile.Benefits(-1, -1, 0, 1, 1, 1, new DrugProfile.Extra(-1, 0, .50, 1, .80F)),
				180, new DrugProfile.Traits(0, .10, 1, 1, 1, 0), false, true);
		MASTERWORK_RESERVE = register("masterwork_reserve", "intoxicant", 1500, 20, 60,
				new DrugProfile.Benefits(-1, -1, 0, 1, 1, 1, new DrugProfile.Extra(-1, 0, .60, 1, 1)),
				180, new DrugProfile.Traits(.30, .15, 1, 1.35F, 1, 0), true, false);
		ITEMS.put("release_control_die", JugcraftRegistry.item("release_control_die", ReleaseDie::new));
		for (String id : new String[] {"caffeine_xr", "methylphenidate_xr", "amphetamine_xr", "modafinil_xr"}) {
			for (String suffix : new String[] {"core", "formula_plate"}) {
				String key = id + '_' + suffix;
				ITEMS.put(key, JugcraftRegistry.item(key, DescribedItem::new));
			}
		}
		for (String id : new String[] {"release_matrix_blend", "release_matrix", "advanced_release_matrix", "empty_pharmacy_capsule",
				"pharmacy_patch_backing", "fentanyl_patch_stock", "fentanyl_patch_formula_plate", "masterwork_blend",
				"masterwork_unconditioned_concentrate", "masterwork_reserve_concentrate", "masterwork_conditioning_insert", "masterwork_reserve_formula_plate"}) {
			ITEMS.put(id, JugcraftRegistry.item(id, DescribedItem::new));
		}
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> ITEMS.forEach((id, item) -> { if (Pharmacology.isProfile(id)) entries.accept(item); }));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(entries -> ITEMS.forEach((id, item) -> { if (!Pharmacology.isProfile(id)) entries.accept(item); }));
	}
	public static Item item(String id) {
		Item item = ITEMS.get(id);
		if (item == null) throw new IllegalArgumentException("Unknown controlled-release item: " + id);
		return item;
	}
}
