package io.github.jimbozoomer.jugcraft.pharmacology;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.materials.JugcraftRegistry;
import io.github.jimbozoomer.jugcraft.weapons.DescribedItem;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/** Precision and beverage formulations on the existing player-owned drug ledger. */
public final class PrecisionPharmacy {
	private static final Map<String, Item> ITEMS = new LinkedHashMap<>();
	public static DrugProfile METHYLPHENIDATE, AMPHETAMINE, MODAFINIL, KETAMINE, FENTANYL;
	public static Holder<MobEffect> INTOXICATION, FOCUS;
	private PrecisionPharmacy() {}
	public static class ProcessModule extends DescribedItem {
		public ProcessModule(Properties properties) { super(properties.stacksTo(1)); }
	}
	private static class Effect extends MobEffect {
		Effect(MobEffectCategory category, int color) { super(category, color); }
	}
	private static Holder<MobEffect> effect(String id, MobEffect effect) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Jugcraft.id(id), effect);
	}
	private static DrugProfile profile(String id, String family, int duration, int onset, int strain,
			int haste, int speed, double knockback, float resistance, int recovery, boolean redose, DrugProfile.Traits traits, boolean tablet) {
		var profile = Pharmacology.registerProfile(new DrugProfile(id, family, duration, onset, strain,
				effect(id, new Effect(MobEffectCategory.BENEFICIAL, 0xB8AE83)),
				new DrugProfile.Benefits(haste, speed, 0, 1, id.equals("modafinil") ? .85F : 1, 1, new DrugProfile.Extra(-1, 0, knockback, 1, resistance)), recovery, redose, traits));
		ITEMS.put(id, JugcraftRegistry.item(id, properties -> new DrugDrinkItem(tablet ? Pharmacology.tabletProperties(properties) : Pharmacology.drinkProperties(properties), profile)));
		return profile;
	}
	public static void register() {
		INTOXICATION = effect("intoxication", new Effect(MobEffectCategory.HARMFUL, 0xB78842));
		FOCUS = effect("precision_focus", new Effect(MobEffectCategory.BENEFICIAL, 0x83BBC0));
		METHYLPHENIDATE = profile("methylphenidate", "stimulant", 1200, 20, 35, 1, -1, 0, 1, 120, true,
				new DrugProfile.Traits(0, 0, 1.2F, 1, .75F, 0), true);
		AMPHETAMINE = profile("amphetamine", "stimulant", 900, 20, 55, 2, 1, 0, 1, 120, true,
				new DrugProfile.Traits(0, 0, 1.5F, 1, 1, 120), true);
		MODAFINIL = profile("modafinil", "stimulant", 2400, 20, 25, 0, -1, 0, 1, 120, true,
				new DrugProfile.Traits(0, 0, 1, 1, .85F, 0), true);
		KETAMINE = profile("ketamine", "dissociative", 30, 0, 40, -1, -1, 0, 1, 120, false, DrugProfile.Traits.NONE, false);
		FENTANYL = profile("fentanyl", "opioid", 480, 5, 55, -1, -1, .75, .6F, 180, false,
				new DrugProfile.Traits(0, .20, 1, 1, 1, 0), false);
		String[] alcohol = {"moonshine", "refined_spirits", "reserve_whiskey", "high_proof_spirits"};
		for (int grade = 0; grade < alcohol.length; grade++) profile(alcohol[grade], "intoxicant", 480 + grade * 240,
				grade == 3 ? 15 : 10, 20 + grade * 10, -1, -1, .20 + grade * .10, 1, grade >= 2 ? 180 : 120, grade < 2,
				new DrugProfile.Traits(.10 + grade * .05, .05 + grade * .025, 1, 1.15F + grade * .05F, 1, 0), false);
		ITEMS.put("precision_process_module", JugcraftRegistry.item("precision_process_module", ProcessModule::new));
		for (String id : new String[] {"methylphenidate", "amphetamine", "modafinil", "ketamine", "fentanyl"}) {
			for (String suffix : new String[] {"reagent_blend", "stock", "active", "formula_plate"}) {
				String key = id + '_' + suffix;
				ITEMS.put(key, JugcraftRegistry.item(key, DescribedItem::new));
			}
		}
		for (String id : new String[] {"empty_mash_jug", "mash_jug", "fermented_mash_jug", "beverage_culture_pack", "oak_conditioning_insert",
				"refined_spirits_concentrate", "reserve_whiskey_concentrate", "high_proof_spirits_concentrate",
				"refined_spirits_formula_plate", "reserve_whiskey_formula_plate", "high_proof_spirits_formula_plate"}) {
			ITEMS.put(id, JugcraftRegistry.item(id, DescribedItem::new));
		}
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> ITEMS.forEach((id, item) -> { if (Pharmacology.isProfile(id)) entries.accept(item); }));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(entries -> ITEMS.forEach((id, item) -> { if (!Pharmacology.isProfile(id)) entries.accept(item); }));
		EchoView.register();
	}
	public static Item item(String id) {
		Item item = ITEMS.get(id);
		if (item == null) throw new IllegalArgumentException("Unknown precision pharmacy item: " + id);
		return item;
	}
	/** Legacy risk-pair predicate. These pairings now add strain rather than rejecting use. */
	public static boolean conflicts(String first, String second) {
		return !first.equals(second) && restricted(first) && restricted(second);
	}
	private static boolean restricted(String family) { return family.equals("intoxicant") || family.equals("opioid") || family.equals("dissociative"); }
	/** Native effect sync is used for the client's existing aimed-hand lag; no shot-cone modification. */
	public static float aimSwayMultiplier(LivingEntity player) {
		var focus = player.getEffect(FOCUS);
		float multiplier = focus == null ? 1 : focus.getAmplifier() >= 1 ? .75F : .85F;
		return multiplier * (player.hasEffect(PsychedelicPharmacy.PATTERN_INTERFERENCE) ? 1.20F : 1);
	}
}
