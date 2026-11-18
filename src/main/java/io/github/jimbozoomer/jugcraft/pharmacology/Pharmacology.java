package io.github.jimbozoomer.jugcraft.pharmacology;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.materials.JugcraftRegistry;
import io.github.jimbozoomer.jugcraft.weapons.DescribedItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;

/** Optional manufactured buffs. No health, injury, combat or environment event creates a new debuff. */
public final class Pharmacology {
	/** Kept as a stable API name for formulation validation/tooltips; this is now the safe threshold. */
	public static final int MAX_STRAIN = 100;
	public static final int RECOVERY_SECONDS = 120;
	public static final int TOLERANCE_SECONDS = 1800;
	private static final Identifier WALKING = Jugcraft.id("caffeine_walking");
	private static final Identifier KNOCKBACK = Jugcraft.id("pharmacy_knockback");
	private static final Identifier MOVE_PENALTY = Jugcraft.id("pharmacy_movement_penalty");
	private static final Identifier MINE_PENALTY = Jugcraft.id("pharmacy_mining_penalty");
	private static final Identifier ATTACK_PENALTY = Jugcraft.id("pharmacy_attack_penalty");
	private static final List<Identifier> LEGACY_MOVE = List.of(Jugcraft.id("adrenaline_fatigue_walk"),Jugcraft.id("opioid_drowsiness_walk"),Jugcraft.id("alcohol_movement"));
	private static final List<Identifier> LEGACY_MINE = List.of(Jugcraft.id("adrenaline_fatigue_mine"),Jugcraft.id("opioid_drowsiness_mine"),Jugcraft.id("thc_tool_speed"));
	private static final Identifier AFTER_ATTACHMENT_COPY = Jugcraft.id("pharmacology_after_attachment_copy");
	private static final Map<String, DrugProfile> PROFILES = new LinkedHashMap<>();
	public static AttachmentType<DrugState> STATE;
	public static Holder<MobEffect> CAFFEINATED;
	public static Holder<MobEffect> JITTERS;
	public static Holder<MobEffect> FORTITUDE;
	public static Holder<MobEffect> RECOVERY;
	public static Holder<MobEffect> ENDURANCE;
	public static Holder<MobEffect> STIMULATED;
	public static Holder<MobEffect> STIMULANT_APPETITE;
	public static DrugProfile CAFFEINE;
	public static DrugProfile ASPIRIN;
	public static DrugProfile PARACETAMOL;
	public static DrugProfile REHYDRATION_SALTS;
	public static DrugProfile STIMULANT;
	public static Item CAFFEINE_CONCENTRATE;
	public static Item CAFFEINE_DRINK;
	public static Item PHARMACEUTICAL_STARCH;
	public static Item PARACETAMOL_TABLET;
	public static Item REHYDRATION_DRINK;
	public static Item ACTIVATED_CHARCOAL;

	private Pharmacology() {
	}

	private static class DrugEffect extends MobEffect {
		DrugEffect(MobEffectCategory category, int color) {
			super(category, color);
		}
	}

	/** A separately identifiable drug drawback: charcoal must never erase hunger from an unrelated source. */
	private static final class StimulantAppetite extends DrugEffect {
		StimulantAppetite() {
			super(MobEffectCategory.HARMFUL, 0xA78C55);
		}

		@Override
		public boolean shouldApplyEffectTickThisTick(int ticks, int amplifier) {
			return ticks % 20 == 0;
		}

		@Override
		public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
			if (entity instanceof ServerPlayer player) {
				player.causeFoodExhaustion(.1F);
			}
			return true;
		}
	}

	private static Holder<MobEffect> marker(String id, int color) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Jugcraft.id(id),
				new DrugEffect(MobEffectCategory.BENEFICIAL, color));
	}

	private static DrugProfile profile(String id, String family, int duration, int onset, int strain,
			Holder<MobEffect> marker, DrugProfile.Benefits benefits) {
		DrugProfile profile = new DrugProfile(id, family, duration, onset, strain, marker, benefits);
		PROFILES.put(id, profile);
		return profile;
	}

	static DrugProfile registerProfile(DrugProfile profile) {
		if (PROFILES.putIfAbsent(profile.id(), profile) != null) throw new IllegalArgumentException("Duplicate drug " + profile.id());
		return profile;
	}

	static boolean isProfile(String id) { return PROFILES.containsKey(id); }
	static DrugProfile profile(String id) { return PROFILES.get(id); }

	public static Item.Properties drinkProperties(Item.Properties properties) {
		return properties.stacksTo(16).usingConvertsTo(Items.GLASS_BOTTLE).component(DataComponents.CONSUMABLE,
				Consumables.defaultDrink().consumeSeconds(1.6F).hasConsumeParticles(false).build());
	}

	public static Item.Properties tabletProperties(Item.Properties properties) {
		return properties.stacksTo(16).component(DataComponents.CONSUMABLE,
				Consumable.builder().consumeSeconds(.8F).animation(ItemUseAnimation.EAT).hasConsumeParticles(false).build());
	}
	/** Same paid-use time as the former drink; the packet/tablet has no glass container. */
	public static Item.Properties packagedDoseProperties(Item.Properties properties) {
		return properties.stacksTo(16).component(DataComponents.CONSUMABLE,
				Consumable.builder().consumeSeconds(1.6F).animation(ItemUseAnimation.EAT).hasConsumeParticles(false).build());
	}
	/** Existing sterile vial is the device's internal cartridge; its empty glass is returned once. */
	public static Item.Properties deviceProperties(Item.Properties properties) {
		return properties.stacksTo(16).usingConvertsTo(Items.GLASS_BOTTLE).component(DataComponents.CONSUMABLE,
				Consumable.builder().consumeSeconds(1.6F).animation(ItemUseAnimation.NONE)
						.sound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER).hasConsumeParticles(false).build());
	}

	public static void register() {
		PharmacyFactory.register();
		STATE = AttachmentRegistry.<DrugState>builder().persistent(DrugState.CODEC).copyOnDeath()
				.buildAndRegister(Jugcraft.id("pharmacology_v1"));
		CAFFEINATED = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Jugcraft.id("caffeinated"),
				new DrugEffect(MobEffectCategory.BENEFICIAL, 0xAD703C));
		JITTERS = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Jugcraft.id("caffeine_jitters"),
				new DrugEffect(MobEffectCategory.HARMFUL, 0xD3A14B));
		FORTITUDE = marker("fortitude", 0xB7A78C);
		RECOVERY = marker("pharmacy_recovery", 0xCC8F96);
		ENDURANCE = marker("endurance", 0x6FBCC0);
		STIMULATED = marker("stimulated", 0xE8BE64);
		STIMULANT_APPETITE = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Jugcraft.id("stimulant_appetite"),
				new StimulantAppetite());
		CAFFEINE = profile("caffeine", "stimulant", 300, 10, 10, CAFFEINATED,
				new DrugProfile.Benefits(0, -1, .10, 1, 1, 1));
		ASPIRIN = profile("aspirin", "fortitude", 720, 5, 10, FORTITUDE,
				new DrugProfile.Benefits(-1, -1, 0, 1, 1, .90F));
		PARACETAMOL = profile("paracetamol", "recovery", 720, 5, 10, RECOVERY,
				new DrugProfile.Benefits(-1, -1, 0, 1.25F, 1, 1));
		REHYDRATION_SALTS = profile("rehydration_salts", "endurance", 900, 5, 5, ENDURANCE,
				new DrugProfile.Benefits(-1, -1, 0, 1, .80F, 1));
		STIMULANT = profile("stimulant", "stimulant", 60, 0, 25, STIMULATED,
				new DrugProfile.Benefits(1, 1, 0, 1, 1, 1));
		SterilePharmacy.register();
		PrecisionPharmacy.register();
		ControlledRelease.register();
		PsychedelicPharmacy.register();
		StrainStatus.register();
		CAFFEINE_CONCENTRATE = JugcraftRegistry.item("caffeine_concentrate", Item::new);
		CAFFEINE_DRINK = JugcraftRegistry.item("caffeine_drink", properties -> new DrugDrinkItem(
				drinkProperties(properties), CAFFEINE));
		PHARMACEUTICAL_STARCH = JugcraftRegistry.item("pharmaceutical_starch", DescribedItem::new);
		PARACETAMOL_TABLET = JugcraftRegistry.item("paracetamol", properties -> new DrugDrinkItem(
				tabletProperties(properties), PARACETAMOL));
		REHYDRATION_DRINK = JugcraftRegistry.item("rehydration_salts", properties -> new DrugDrinkItem(
				packagedDoseProperties(properties), REHYDRATION_SALTS));
		ACTIVATED_CHARCOAL = JugcraftRegistry.item("activated_charcoal", properties -> new DescribedItem(
				tabletProperties(properties).useCooldown(60).component(DataComponents.CONSUMABLE,
						Consumable.builder().consumeSeconds(.8F).animation(ItemUseAnimation.EAT).hasConsumeParticles(false)
								.onConsume(new RemoveStatusEffectsConsumeEffect(HolderSet.direct(JITTERS, STIMULANT_APPETITE, SterilePharmacy.FATIGUE)))
								.build())));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
			entries.accept(CAFFEINE_CONCENTRATE);
			entries.accept(CAFFEINE_DRINK);
			entries.accept(PHARMACEUTICAL_STARCH);
			entries.accept(PARACETAMOL_TABLET);
			entries.accept(REHYDRATION_DRINK);
			entries.accept(ACTIVATED_CHARCOAL);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.hasAttached(STATE) && server.getTickCount() % 20 == 0) {
					tickSecond(player);
				}
				if (player.isAlive()) walking(player);
				StrainStatus.sync(player);
			}
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity,source) -> { if(entity instanceof ServerPlayer player && player.hasAttached(STATE))onDeath(player); });
		// Fabric transfers copy-on-death attachments in the default phase. Normalize only after that transfer,
		// independent of whether this mod's initializer ran before the attachment module's initializer.
		ServerPlayerEvents.AFTER_RESPAWN.addPhaseOrdering(Event.DEFAULT_PHASE, AFTER_ATTACHMENT_COPY);
		ServerPlayerEvents.AFTER_RESPAWN.register(AFTER_ATTACHMENT_COPY, (oldPlayer, newPlayer, alive) -> {
			if (!alive) {
				onDeath(newPlayer);
			}
		});
	}

	public static DrugState state(ServerPlayer player) {
		DrugState state = player.getAttached(STATE);
		if (state == null) return DrugState.EMPTY;
		if (state.strainTenths() < 0) {
			int initial=state.doses().stream().mapToInt(d -> { var p=PROFILES.get(d.drug()); return p==null?0:p.strain()*10; }).sum();
			state=new DrugState(state.doses(),state.history(),initial);
			player.setAttached(STATE,state);
		}
		return state;
	}

	public static int strain(ServerPlayer player) {
		return (int)((state(player).strainTenths()+9L)/10);
	}
	public static boolean dosing(ServerPlayer player) {
		return player.isAlive() && state(player).doses().stream().anyMatch(d -> d.active() && PROFILES.containsKey(d.drug()) && player.hasEffect(PROFILES.get(d.drug()).marker()));
	}
	public static int combinations(ServerPlayer player) {
		int mask=0;
		for(var d:state(player).doses()) { var p=PROFILES.get(d.drug()); if(p!=null && d.active() && player.hasEffect(p.marker()))mask|=DrugStrain.familyBit(p.family()); }
		return mask;
	}
	public static int doseCost(ServerPlayer player, DrugProfile profile) { return profile.strain()+DrugStrain.surcharge(profile.family(),combinations(player)); }

	public static boolean mayDrink(ServerPlayer player, DrugProfile profile, boolean explain) {
		String reason = null;
		if (!player.isAlive() || player.isSpectator() || PROFILES.get(profile.id()) != profile) {
			reason = "message.jugcraft.drug.unavailable";
		}
		if (profile == PrecisionPharmacy.KETAMINE && !EchoView.canStart(player)) reason = "message.jugcraft.echo_unavailable";
		if (reason != null && explain) {
			player.sendOverlayMessage(Component.translatable(reason));
		}
		return reason == null;
	}

	public static boolean drink(ServerPlayer player, DrugProfile profile) {
		if (!mayDrink(player, profile, true)) {
			return false;
		}
		DrugState state = state(player);
		DrugState.History previous = state.history().stream().filter(h -> h.family().equals(profile.family()))
				.findFirst().orElse(null);
		int count = previous == null ? 1 : Math.min(3, previous.doses() + 1);
		int duration = profile.durationSeconds() * (count == 1 ? 100 : count == 2 ? 75 : 50) / 100;
		int cost=doseCost(player,profile);
		boolean redosing = state.doses().stream().anyMatch(d -> { var p=PROFILES.get(d.drug()); return p!=null && p.family().equals(profile.family()) && active(player,d,p); });
		List<DrugState.Dose> doses = new ArrayList<>(state.doses());
		for(var dose:doses) { var old=PROFILES.get(dose.drug()); if(old!=null && old.family().equals(profile.family())) { player.removeEffect(old.marker()); clearPulses(player,old); } }
		doses.removeIf(d -> { var old=PROFILES.get(d.drug()); return old==null || old.family().equals(profile.family()); });
		doses.add(new DrugState.Dose(profile.id(), duration, duration, profile.onsetSeconds(), 0));
		List<DrugState.History> history = new ArrayList<>(state.history());
		history.removeIf(h -> h.family().equals(profile.family()));
		history.add(new DrugState.History(profile.family(), count, TOLERANCE_SECONDS));
		player.setAttached(STATE, new DrugState(doses, history, DrugStrain.add(state.strainTenths(),cost)));
		player.addEffect(new MobEffectInstance(profile.marker(), (duration + profile.onsetSeconds()) * 20));
		if (redosing && profile.family().equals("stimulant")) {
			player.addEffect(new MobEffectInstance(JITTERS, 60 * 20));
		}
		if (profile == STIMULANT) {
			player.addEffect(new MobEffectInstance(STIMULANT_APPETITE, duration * 20));
		}
		if (profile.onsetSeconds() == 0) {
			activate(player, profile, duration);
			benefits(player, profile, duration);
			walking(player);
		}
		// Restore other families' short native pulses if they shared an effect with the replaced dose.
		for(var dose:doses) { var p=PROFILES.get(dose.drug()); if(active(player,dose,p))benefits(player,p,dose.remaining()); }
		walking(player);
		StrainStatus.dosed(player,profile);
		StrainStatus.sync(player);
		showStatus(player);
		return true;
	}

	/** One bounded online second. Public so lifecycle and serialization tests exercise the production clock. */
	public static void tickSecond(ServerPlayer player) {
		DrugState state = state(player);
		if(!player.isAlive() && state.doses().stream().anyMatch(DrugState.Dose::active)) { onDeath(player);state=state(player); }
		List<DrugState.Dose> doses = new ArrayList<>();
		for (DrugState.Dose dose : state.doses()) {
			DrugProfile profile = PROFILES.get(dose.drug());
			if (profile == null) {
				continue;
			}
			if (!dose.active()) {
				if (dose.recovery() > 1) {
					doses.add(new DrugState.Dose(dose.drug(), dose.total(), 0, 0, dose.recovery() - 1));
				}
				continue;
			}
			if (dose.onset() == 0 && dose.remaining() <= 1) {
				player.removeEffect(profile.marker());
				doses.add(dose.recovering(profile.recoverySeconds()));
				int fatigue = profile == SterilePharmacy.ADRENALINE ? 60 : profile.traits().fatigueSeconds();
				if (fatigue > 0) player.addEffect(new MobEffectInstance(SterilePharmacy.FATIGUE, fatigue * 20));
				PsychedelicPharmacy.expired(player, profile);
			} else if (!player.hasEffect(profile.marker())) {
				doses.add(dose.recovering(profile.recoverySeconds()));
			} else if (dose.onset() > 0) {
				int onset = dose.onset() - 1;
				doses.add(new DrugState.Dose(dose.drug(), dose.total(), dose.remaining(), onset, 0));
				if (onset == 0) {
					player.addEffect(new MobEffectInstance(profile.marker(), dose.remaining() * 20));
					activate(player, profile, dose.remaining());
					benefits(player, profile, dose.remaining());
				}
			} else {
				int remaining = dose.remaining() - 1;
				doses.add(new DrugState.Dose(dose.drug(), dose.total(), remaining, 0, 0));
				benefits(player, profile, remaining);
			}
		}
		List<DrugState.History> history = new ArrayList<>();
		for (DrugState.History entry : state.history()) {
			boolean active = doses.stream().anyMatch(d -> d.active() && PROFILES.get(d.drug()).family().equals(entry.family()));
			int remaining = active ? TOLERANCE_SECONDS : entry.recovery() - 1;
			if (remaining > 0) {
				history.add(new DrugState.History(entry.family(), entry.doses(), remaining));
			}
		}
		boolean stillDosing=doses.stream().anyMatch(d -> d.active() && player.hasEffect(PROFILES.get(d.drug()).marker()));
		int remainingStrain=Math.max(0,state.strainTenths()-(stillDosing?1:2));
		if (doses.isEmpty() && history.isEmpty() && remainingStrain==0) {
			player.removeAttached(STATE);
		} else {
			player.setAttached(STATE, new DrugState(doses, history, remainingStrain));
		}
		walking(player);
		if (heldProfile(player) != null) {
			showStatus(player);
		}
	}

	private static void benefits(ServerPlayer player, DrugProfile profile, int seconds) {
		PsychedelicPharmacy.benefits(player, profile, seconds);
		if (DrugPresentation.flag(profile)==DrugPresentation.MODAFINIL) {
			// Native phantom spawning reads this same rest statistic. No new sleep/withdrawal condition.
			player.resetStat(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.TIME_SINCE_REST));
		}
		// Short native pulses preserve stronger/longer potion effects and expire promptly after a cleanse.
		if (profile.benefits().haste() >= 0) {
			player.addEffect(new MobEffectInstance(MobEffects.HASTE, Math.min(40, seconds * 20),
					profile.benefits().haste(), false, false, true));
		}
		if (profile.benefits().speed() >= 0) {
			player.addEffect(new MobEffectInstance(MobEffects.SPEED, Math.min(40, seconds * 20),
					profile.benefits().speed(), false, false, true));
		}
		if (profile.benefits().extra().strength() >= 0) {
			player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, Math.min(40, seconds * 20),
					profile.benefits().extra().strength(), false, false, true));
		}
		if (profile.traits().aimSway() < 1) player.addEffect(new MobEffectInstance(PrecisionPharmacy.FOCUS, Math.min(40, seconds * 20), profile.traits().aimSway() < .8F ? 1 : 0, false, false, true));
	}
	/** Remove only short, hidden-particle native pulses matching this formulation; preserve normal potions. */
	private static void clearPulse(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
		var current=player.getEffect(effect);
		if(amplifier>=0 && current!=null && current.getAmplifier()==amplifier && current.getDuration()<=40 && !current.isVisible() && !current.isAmbient()) player.removeEffect(effect);
	}
	private static void clearPulses(ServerPlayer player, DrugProfile profile) {
		clearPulse(player,MobEffects.HASTE,profile.benefits().haste());
		clearPulse(player,MobEffects.SPEED,profile.benefits().speed());
		clearPulse(player,MobEffects.STRENGTH,profile.benefits().extra().strength());
		if(profile==PsychedelicPharmacy.THC) clearPulse(player,MobEffects.LUCK,0);
	}

	private static void activate(ServerPlayer player, DrugProfile profile, int seconds) {
		if (profile.benefits().extra().absorptionHearts() > 0 && !player.hasEffect(MobEffects.ABSORPTION)) {
			player.removeEffect(SterilePharmacy.ABSORPTION);
			player.addEffect(new MobEffectInstance(SterilePharmacy.ABSORPTION, seconds * 20));
		}
		if (profile.family().equals("opioid")) {
			double penalty = profile.traits().movementPenalty() > 0 ? profile.traits().movementPenalty() : .15;
			player.addEffect(new MobEffectInstance(SterilePharmacy.DROWSINESS, seconds * 20, (int) Math.round(penalty / .05) - 1));
		}
		if (profile.family().equals("intoxicant")) player.addEffect(new MobEffectInstance(PrecisionPharmacy.INTOXICATION, seconds * 20, (int) Math.round(profile.traits().movementPenalty() / .025) - 1));
		if (profile == PrecisionPharmacy.KETAMINE) EchoView.open(player, seconds);
	}

	static boolean active(ServerPlayer player, DrugState.Dose dose, DrugProfile profile) {
		return profile != null && dose.active() && dose.onset() == 0 && player.hasEffect(profile.marker());
	}

	/** These hooks only run inside vanilla's existing damage, food-healing and activity-exhaustion paths. */
	public static float physicalDamage(LivingEntity entity, DamageSource source, float amount) {
		if (!(entity instanceof ServerPlayer player)) {
			return amount;
		}
		float multiplier = 1;
		for (DrugState.Dose dose : state(player).doses()) {
			DrugProfile profile = PROFILES.get(dose.drug());
			if (active(player, dose, profile)) {
				if (!source.is(DamageTypeTags.BYPASSES_ARMOR) && !source.is(DamageTypeTags.IS_FIRE)) {
					multiplier = Math.min(multiplier, profile.benefits().damage());
				}
				if (!source.is(DamageTypeTags.BYPASSES_EFFECTS) && !source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
					multiplier = Math.min(multiplier, profile.benefits().extra().resistance());
				}
			}
		}
		var resistance = player.getEffect(MobEffects.RESISTANCE);
		float nativeMultiplier = resistance == null || source.is(DamageTypeTags.BYPASSES_EFFECTS) || source.is(DamageTypeTags.BYPASSES_RESISTANCE)
				? 1 : Math.max(0, 1 - .2F * (resistance.getAmplifier() + 1));
		return nativeMultiplier == 0 ? amount : amount * Math.min(multiplier, nativeMultiplier) / nativeMultiplier;
	}

	public static float naturalHealingMultiplier(ServerPlayer player) {
		float multiplier = 1;
		for (DrugState.Dose dose : state(player).doses()) {
			DrugProfile profile = PROFILES.get(dose.drug());
			if (active(player, dose, profile)) {
				multiplier = Math.max(multiplier, profile.benefits().healing());
			}
		}
		return multiplier;
	}

	public static float activityExhaustion(LivingEntity entity, float amount) {
		if (!(entity instanceof ServerPlayer player)) {
			return amount;
		}
		float multiplier = 1, cost = DrugStrain.band(state(player).strainTenths()).food;
		for (DrugState.Dose dose : state(player).doses()) {
			DrugProfile profile = PROFILES.get(dose.drug());
			if (active(player, dose, profile)) {
				multiplier = Math.min(multiplier, profile.benefits().exhaustion());
				cost = Math.max(cost, profile.traits().activityCost());
			}
		}
		return amount * multiplier * cost;
	}

	/** Applied only to the native ground-sprint branch, before the general activity discount. */
	public static float sprintExhaustion(ServerPlayer player, float amount) {
		float general = 1;
		for (DrugState.Dose dose : state(player).doses()) {
			DrugProfile profile = PROFILES.get(dose.drug());
			if (active(player, dose, profile)) general = Math.min(general, profile.benefits().exhaustion());
		}
		float sprint = 1;
		for (DrugState.Dose dose : state(player).doses()) {
			DrugProfile profile = PROFILES.get(dose.drug());
			if (active(player, dose, profile)) sprint = Math.min(sprint, profile.benefits().extra().sprint());
		}
		return amount * Math.min(sprint, general) / general;
	}

	/** The native water input hook runs on both sides for responsive local movement. */
	public static float waterAcceleration(LivingEntity entity, float amount) {
		return entity instanceof net.minecraft.world.entity.player.Player && entity.hasEffect(SterilePharmacy.SALBUTAMOL.marker())
				? amount * 1.15F : amount;
	}

	private static void walking(ServerPlayer player) {
		var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}
		double bonus = 0;
		for (DrugState.Dose dose : state(player).doses()) {
			DrugProfile profile = PROFILES.get(dose.drug());
			if (active(player, dose, profile)) {
				bonus = Math.max(bonus, profile.benefits().walking());
			}
		}
		if (bonus == 0 || player.hasEffect(MobEffects.SPEED)) {
			speed.removeModifier(WALKING);
		} else if (!speed.hasModifier(WALKING)) {
			speed.addTransientModifier(new AttributeModifier(WALKING, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
		double knockback = 0;
		boolean shielding = false, opioid = false, alcohol = false, focus = false;
		for (DrugState.Dose dose : state(player).doses()) {
			DrugProfile profile = PROFILES.get(dose.drug());
			if (active(player, dose, profile)) {
				knockback = Math.max(knockback, profile.benefits().extra().knockback());
				shielding |= profile == SterilePharmacy.LIDOCAINE;
				opioid |= profile.family().equals("opioid");
				alcohol |= profile.family().equals("intoxicant");
				focus |= profile.traits().aimSway() < 1;
			}
		}
		var resistance = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
		if (resistance != null) {
			var current = resistance.getModifier(KNOCKBACK);
			if (current != null && current.amount() != knockback) resistance.removeModifier(KNOCKBACK);
			if (knockback > 0 && !resistance.hasModifier(KNOCKBACK)) {
				resistance.addTransientModifier(new AttributeModifier(KNOCKBACK, knockback, AttributeModifier.Operation.ADD_VALUE));
			}
		}
		if (!shielding || player.hasEffect(MobEffects.ABSORPTION)) player.removeEffect(SterilePharmacy.ABSORPTION);
		if (!opioid) player.removeEffect(SterilePharmacy.DROWSINESS);
		else {
			var drowsy = player.getEffect(SterilePharmacy.DROWSINESS);
			// Retain the 15% penalty of saved Batch 3 Morphine effects after moving to 5%-per-level scaling.
			if (drowsy != null && drowsy.getAmplifier() == 0) player.addEffect(new MobEffectInstance(SterilePharmacy.DROWSINESS, drowsy.getDuration(), 2));
		}
		if (!alcohol) player.removeEffect(PrecisionPharmacy.INTOXICATION);
		if (!focus) player.removeEffect(PrecisionPharmacy.FOCUS);
		if (!activeProfile(player, PsychedelicPharmacy.THC)) player.removeEffect(PsychedelicPharmacy.RELAXED);
		if (!activeProfile(player, PsychedelicPharmacy.LSD)) player.removeEffect(PsychedelicPharmacy.PATTERN_INTERFERENCE);
		// Old effect modifier IDs are retired without changing their saved effect registrations.
		for(var id:LEGACY_MOVE) speed.removeModifier(id);
		var mining=player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
		if(mining!=null) for(var id:LEGACY_MINE) mining.removeModifier(id);
		var band=DrugStrain.band(state(player).strainTenths());
		double movement=band.movement, mine=band.mining;
		var drowsy=player.getEffect(SterilePharmacy.DROWSINESS);
		if(drowsy!=null) { double penalty=drowsy.getAmplifier()==0?.15:Math.min(.50,(drowsy.getAmplifier()+1)*.05); movement=Math.max(movement,penalty);mine=Math.max(mine,penalty); }
		var intoxicated=player.getEffect(PrecisionPharmacy.INTOXICATION);
		if(intoxicated!=null) movement=Math.max(movement,Math.min(.50,(intoxicated.getAmplifier()+1)*.025));
		if(player.hasEffect(SterilePharmacy.FATIGUE)) { movement=Math.max(movement,.10);mine=Math.max(mine,.15); }
		if(player.hasEffect(PsychedelicPharmacy.RELAXED)) mine=Math.max(mine,.10);
		penalty(player,Attributes.MOVEMENT_SPEED,MOVE_PENALTY,movement);
		penalty(player,Attributes.BLOCK_BREAK_SPEED,MINE_PENALTY,mine);
		penalty(player,Attributes.ATTACK_SPEED,ATTACK_PENALTY,band.attackSpeed);
	}
	private static void penalty(ServerPlayer player, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, Identifier id, double amount) {
		var value=player.getAttribute(attribute); if(value==null)return;
		var existing=value.getModifier(id);
		if(existing!=null && existing.amount()!=-amount) value.removeModifier(id);
		if(amount>0 && !value.hasModifier(id)) value.addTransientModifier(new AttributeModifier(id,-amount,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	}

	/** Self-administered reversal: accounting survives and other families are untouched. */
	public static void cancelFamily(ServerPlayer player, String family) {
		DrugState state = state(player);
		List<DrugState.Dose> doses = new ArrayList<>();
		for (DrugState.Dose dose : state.doses()) {
			DrugProfile profile = PROFILES.get(dose.drug());
			if (profile != null && dose.active() && profile.family().equals(family)) {
				player.removeEffect(profile.marker());
				clearPulses(player,profile);
				doses.add(dose.recovering(profile.recoverySeconds()));
			} else doses.add(dose);
		}
		player.setAttached(STATE, new DrugState(doses, state.history(),state.strainTenths()));
		walking(player);
		StrainStatus.sync(player);
	}

	public static void onDeath(ServerPlayer player) {
		DrugState state = state(player);
		player.setAttached(STATE, new DrugState(state.doses().stream()
				.map(dose -> dose.active() ? dose.recovering(PROFILES.containsKey(dose.drug())
						? PROFILES.get(dose.drug()).recoverySeconds() : RECOVERY_SECONDS) : dose).toList(), state.history(),state.strainTenths()));
		for (DrugProfile profile : PROFILES.values()) {
			player.removeEffect(profile.marker());
			clearPulses(player,profile);
		}
		player.removeEffect(SterilePharmacy.FATIGUE);
		walking(player);
		StrainStatus.sync(player);
	}

	/** Strongest drug/strain accuracy drawback, shared with the server-side firearm spread calculation. */
	public static float aimSpreadMultiplier(LivingEntity player) {
		float spread = player.hasEffect(JITTERS) ? 1.20F : 1.0F;
		if (player instanceof ServerPlayer serverPlayer) {
			spread=Math.max(spread,DrugStrain.band(state(serverPlayer).strainTenths()).spread);
			for (var dose : state(serverPlayer).doses()) {
			DrugProfile profile = PROFILES.get(dose.drug());
			if (active(serverPlayer, dose, profile)) spread = Math.max(spread, profile.traits().spread());
		}
		}
		return spread;
	}

	/** Aimed precision benefit applies after the strongest drawback; hip fire never gains focus. */
	public static float aimSpreadMultiplier(LivingEntity player, boolean aiming) {
		float drawback=aimSpreadMultiplier(player);
		if (!aiming || !(player instanceof ServerPlayer p)) return drawback;
		boolean focused=activeProfile(p,PrecisionPharmacy.METHYLPHENIDATE) || activeProfile(p,ControlledRelease.METHYLPHENIDATE_XR);
		return drawback * (focused ? .75F : 1F);
	}

	public static boolean activeProfile(ServerPlayer player, DrugProfile profile) {
		return state(player).doses().stream().anyMatch(d -> d.drug().equals(profile.id()) && active(player, d, profile));
	}
	/** Called only for the native melee attack attribute; stronger flat Strength bonuses retain priority. */
	public static double meleeDamage(LivingEntity entity, double amount) {
		if (!(entity instanceof ServerPlayer player)) return amount;
		double bonus = 0;
		for (var dose : state(player).doses()) {
			DrugProfile profile = PROFILES.get(dose.drug());
			if (active(player, dose, profile)) bonus = Math.max(bonus, profile.traits().melee());
		}
		var strength = player.getEffect(MobEffects.STRENGTH);
		double nativeBonus = strength == null ? 0 : 3 * (strength.getAmplifier() + 1);
		return amount + Math.max(0, Math.max(0, amount - nativeBonus) * bonus - nativeBonus);
	}

	static DrugProfile heldProfile(ServerPlayer player) {
		if (player.getMainHandItem().getItem() instanceof DrugDrinkItem item) {
			return item.profile();
		}
		return player.getOffhandItem().getItem() instanceof DrugDrinkItem item ? item.profile() : null;
	}

	private static void showStatus(ServerPlayer player) {
		DrugProfile profile = heldProfile(player);
		if (profile == null) {
			return;
		}
		DrugState.Dose dose = state(player).doses().stream().filter(d -> {
			DrugProfile current = PROFILES.get(d.drug());
			return current != null && current.family().equals(profile.family());
		}).findFirst().orElse(null);
		if (dose != null && dose.active()) {
			int time = dose.onset() > 0 ? dose.onset() : dose.active() ? dose.remaining() : dose.recovery();
			String phase = dose.onset() > 0 ? "onset" : dose.active() ? "active" : "recovery";
			player.sendOverlayMessage(Component.translatable("message.jugcraft.drug.status",
					Component.translatable("drug.jugcraft." + dose.drug()), Component.translatable("drug.jugcraft.phase." + phase),
					time / 60, String.format(java.util.Locale.ROOT, "%02d", time % 60), DrugStrain.format(state(player).strainTenths()), MAX_STRAIN));
		} else {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.drug.ready",
					Component.translatable("drug.jugcraft." + profile.id()), DrugStrain.format(state(player).strainTenths()), MAX_STRAIN));
		}
	}
}
