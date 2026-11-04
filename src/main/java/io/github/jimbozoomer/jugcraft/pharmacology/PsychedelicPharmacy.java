package io.github.jimbozoomer.jugcraft.pharmacology;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftCropBlock;
import io.github.jimbozoomer.jugcraft.materials.JugcraftRegistry;
import io.github.jimbozoomer.jugcraft.weapons.DescribedItem;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Fixed fictional formulations; all timing, strain and tolerance stay in Pharmacology.STATE. */
public final class PsychedelicPharmacy {
	private static final Map<String, Item> ITEMS = new LinkedHashMap<>();
	public static DrugProfile THC, PSILOCYBIN, MESCALINE, LSD, DMT;
	public static Holder<MobEffect> RELAXED, PATTERN_INTERFERENCE;
	public static final Map<String, Block> CROPS = new LinkedHashMap<>();
	private PsychedelicPharmacy() {}
	private static final class Effect extends MobEffect {
		Effect(MobEffectCategory category, int color) { super(category, color); }
	}
	private static Holder<MobEffect> effect(String id, MobEffect effect) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Jugcraft.id(id), effect);
	}
	private static DrugProfile profile(String id, String family, int seconds, int onset, int strain, int color) {
		var profile = Pharmacology.registerProfile(new DrugProfile(id, family, seconds, onset, strain,
				effect(id, new Effect(MobEffectCategory.BENEFICIAL, color)), new DrugProfile.Benefits(-1, -1, 0, 1, 1, 1), 120, false));
		ITEMS.put(id, JugcraftRegistry.item(id, p -> new DrugDrinkItem(Pharmacology.tabletProperties(p), profile)));
		return profile;
	}
	public static void register() {
		THC = profile("thc", "cannabinoid", 600, 10, 20, 0x8DA260);
		PSILOCYBIN = profile("psilocybin", "psychedelic", 480, 10, 20, 0xAD83CC);
		MESCALINE = profile("mescaline", "psychedelic", 900, 15, 25, 0x6EB5A1);
		LSD = profile("lsd", "psychedelic", 1500, 20, 35, 0xCA8BD6);
		DMT = profile("dmt", "psychedelic", 300, 20, 50, 0x8ECBDC);
		RELAXED = effect("botanical_relaxation", new Effect(MobEffectCategory.HARMFUL, 0x8DA260));
		PATTERN_INTERFERENCE = effect("pattern_interference", new Effect(MobEffectCategory.HARMFUL, 0xCA8BD6));
		for (String id : new String[] {"botanical_resin", "dreamcap", "prism_cactus_fruit", "dream_concentrate", "prismatic_concentrate",
				"perception_substrate", "oneiric_stock", "mycelium_catalyst", "botanical_capsule_shell", "study_canvas", "lucid_imprint", "pattern_imprint", "oneiric_imprint"})
			ITEMS.put(id, JugcraftRegistry.item(id, DescribedItem::new));
		for (String id : new String[] {"thc", "psilocybin", "mescaline", "lsd", "dmt"})
			for (String suffix : new String[] {"stock", "active", "formula_plate"}) {
				String key = id + '_' + suffix;
				ITEMS.put(key, JugcraftRegistry.item(key, DescribedItem::new));
			}
		for (String id : new String[] {"hemp", "dreamcap", "prism_cactus"}) {
			String seed = id + "_seeds", crop = id + "_crop";
			var key = ResourceKey.create(Registries.BLOCK, Jugcraft.id(crop));
			var block = Registry.register(BuiltInRegistries.BLOCK, key, new JugcraftCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).setId(key), seed, false) {
				@Override public Item seed() { return item(seed); }
			});
			CROPS.put(crop, block);
			ITEMS.put(seed, JugcraftRegistry.item(seed, p -> new BlockItem(block, p)));
		}
		DreamStudies.register();
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(e -> ITEMS.forEach((id, item) -> { if (Pharmacology.isProfile(id)) e.accept(item); }));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(e -> ITEMS.forEach((id, item) -> { if (!Pharmacology.isProfile(id)) e.accept(item); }));
	}
	public static Item item(String id) {
		Item item = ITEMS.get(id);
		if (item == null) throw new IllegalArgumentException("Unknown perception ingredient: " + id);
		return item;
	}
	static void benefits(ServerPlayer player, DrugProfile profile, int seconds) {
		int ticks = Math.min(40, seconds * 20);
		if (profile == THC) {
			player.addEffect(new MobEffectInstance(MobEffects.LUCK, ticks, 0, false, false, true));
			player.addEffect(new MobEffectInstance(RELAXED, ticks, 0, false, false, true));
		}
		if (profile == LSD) player.addEffect(new MobEffectInstance(PATTERN_INTERFERENCE, ticks, 0, false, false, true));
	}
	static void expired(ServerPlayer player, DrugProfile profile) {
		if (profile == THC) player.addEffect(new MobEffectInstance(Pharmacology.STIMULANT_APPETITE, 1200));
	}
	/** Native pre-bite waiting only: one extra countdown tick per three, 25% less waiting. */
	public static boolean fasterBite(ServerPlayer player, int hookTick) {
		return hookTick % 3 == 0 && Pharmacology.activeProfile(player, THC)
				&& !player.isPassenger() && !player.isSprinting() && player.getKnownMovement().lengthSqr() < .0004;
	}
	public static int sight(ServerPlayer player) {
		if (Pharmacology.activeProfile(player, DMT)) return 3;
		if (Pharmacology.activeProfile(player, LSD)) return 2;
		return Pharmacology.activeProfile(player, PSILOCYBIN) ? 1 : 0;
	}
}
