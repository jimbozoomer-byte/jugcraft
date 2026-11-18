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
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Fixed, fictional game stocks on the shared pharmacology clock; no injury or contamination system. */
public final class SterilePharmacy {
	private static final Map<String, Item> ITEMS = new LinkedHashMap<>();
	public static DrugProfile PENICILLIN, LIDOCAINE, ADRENALINE, SALBUTAMOL, MORPHINE;
	public static Holder<MobEffect> ABSORPTION, FATIGUE, DROWSINESS;
	private SterilePharmacy() {}

	private static class Effect extends MobEffect {
		Effect(MobEffectCategory category, int color) { super(category, color); }
	}

	/** Applied once on activation. Spending shield health never triggers another grant. */
	private static final class Shield extends Effect {
		Shield() {
			super(MobEffectCategory.BENEFICIAL, 0xA5C9D7);
			addAttributeModifier(Attributes.MAX_ABSORPTION, Jugcraft.id("lidocaine_absorption"), 8, AttributeModifier.Operation.ADD_VALUE);
		}
		@Override public void onEffectStarted(LivingEntity entity, int amplifier) {
			super.onEffectStarted(entity, amplifier);
			entity.setAbsorptionAmount(Math.max(entity.getAbsorptionAmount(), 8));
		}
		@Override public boolean shouldApplyEffectTickThisTick(int ticks, int amplifier) { return true; }
		@Override public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
			return entity.getAbsorptionAmount() > 0;
		}
	}

	private static Holder<MobEffect> effect(String id, MobEffect effect) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Jugcraft.id(id), effect);
	}
	private static Holder<MobEffect> marker(String id, int color) {
		return effect(id, new Effect(MobEffectCategory.BENEFICIAL, color));
	}
	private static DrugProfile profile(String id, String family, int seconds, int onset, int strain,
			DrugProfile.Benefits benefits) {
		return Pharmacology.registerProfile(new DrugProfile(id, family, seconds, onset, strain,
				marker(id.equals("penicillin") ? "sterile_recovery" : id, 0x90BCC1), benefits,
				id.equals("adrenaline") ? 180 : 120, !id.equals("adrenaline")));
	}

	public static void register() {
		ABSORPTION = effect("pharmacy_absorption", new Shield());
		FATIGUE = effect("adrenaline_fatigue", new Effect(MobEffectCategory.HARMFUL, 0xA78C55));
		DROWSINESS = effect("opioid_drowsiness", new Effect(MobEffectCategory.HARMFUL, 0x9B8FAE));
		PENICILLIN = profile("penicillin", "recovery", 1200, 5, 15,
				new DrugProfile.Benefits(-1, -1, 0, 1.5F, 1, 1));
		LIDOCAINE = profile("lidocaine", "shielding", 600, 5, 10,
				new DrugProfile.Benefits(-1, -1, 0, 1, 1, 1, new DrugProfile.Extra(-1, 4, .25, 1, 1)));
		ADRENALINE = profile("adrenaline", "adrenaline", 45, 0, 35,
				new DrugProfile.Benefits(-1, 1, 0, 1, 1, 1, new DrugProfile.Extra(1, 0, 0, 1, 1)));
		SALBUTAMOL = profile("salbutamol", "bronchodilator", 900, 0, 15,
				new DrugProfile.Benefits(-1, -1, 0, 1, 1, 1, new DrugProfile.Extra(-1, 0, 0, .5F, 1)));
		MORPHINE = profile("morphine", "opioid", 720, 5, 35,
				new DrugProfile.Benefits(-1, -1, 0, 1, 1, 1, new DrugProfile.Extra(-1, 0, .5, 1, .8F)));
		for (DrugProfile profile : new DrugProfile[] {PENICILLIN, LIDOCAINE, ADRENALINE, SALBUTAMOL, MORPHINE}) {
			ITEMS.put(profile.id(), JugcraftRegistry.item(profile.id(), properties ->
					new DrugDrinkItem(profile==ADRENALINE || profile==SALBUTAMOL?Pharmacology.deviceProperties(properties):Pharmacology.drinkProperties(properties), profile)));
		}
		ITEMS.put("naloxone", JugcraftRegistry.item("naloxone", properties -> new DescribedItem(
				Pharmacology.deviceProperties(properties).useCooldown(30)) {
			@Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
				if (stack.isEmpty() || !stack.is(this) || !(entity instanceof Player)) return stack;
				if (entity instanceof ServerPlayer player) Pharmacology.cancelFamily(player, "opioid");
				return super.finishUsingItem(stack, level, entity);
			}
		}));
		for (String id : new String[] {"sterile_vial", "sterile_carrier", "azure_culture_starter", "culture_feed",
				"antibiotic_extract", "lidocaine_stock", "adrenaline_stock", "salbutamol_stock", "redleaf_medical_stock", "naloxone_stock",
				"penicillin_formula_plate", "lidocaine_formula_plate", "adrenaline_formula_plate", "salbutamol_formula_plate", "morphine_formula_plate", "naloxone_formula_plate"}) {
			ITEMS.put(id, JugcraftRegistry.item(id, DescribedItem::new));
		}
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
			for (String id : new String[] {"penicillin", "lidocaine", "adrenaline", "salbutamol", "morphine", "naloxone"}) entries.accept(item(id));
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
			ITEMS.forEach((id, item) -> { if (!id.equals("naloxone") && !Pharmacology.isProfile(id)) entries.accept(item); });
		});
	}

	public static Item item(String id) {
		Item item = ITEMS.get(id);
		if (item == null) throw new IllegalArgumentException("Unknown sterile pharmacy item: " + id);
		return item;
	}
}
