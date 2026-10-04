package io.github.jimbozoomer.jugcraft.weapons;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.gear.JugcraftGear;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;

/**
 * Arms (batch 42, docs/features/arms.md): longswords, greatswords, rapiers, flanged maces, war hammers, glaives,
 * halberds, spears and lances in bronze and steel; and Arms II (batch 45, docs/features/arms-ii.md): daggers, sabres,
 * estocs, battle axes, flails, scythes, quarterstaves and pikes, each with a {@link Trait} of its own.
 *
 * <p>After studying how Epic Knights and Simply Swords make, show and animate their weapons (none of their code, models
 * or art is used): every trait here is one of 26.3's own item components, so these are plain items with no per-tick
 * code, and the game runs and checks their swings, reach, parries, thrusts and charges as it does its own weapons, on
 * the server. Their looks are data: one sprite and one model each over a shared in-hand pose per kind
 * (tools/arms.py). Keep {@link #KINDS}, {@link #CHARGES} and the numbers in sync with tools/arms.py;
 * tools/check_mod_data.py checks them.
 */
public final class JugcraftArms {
	/** The metals arms are made in (tools/arms.py: METALS; tools/gear.py: GEAR_TIERS). */
	public static final List<String> METALS = List.of("bronze", "steel");

	/**
	 * A kind of arm swung or thrust like a sword (tools/arms.py: KINDS). Damage is added to the metal's bonus as a
	 * sword's 3 is; speed is the attack-speed modifier; the swing is the attack animation and its length in ticks;
	 * reach is where a hit lands, in blocks, and margin widens the target's hitbox; disable is how long a hit stops a
	 * shield blocking (s) and wear the durability a hit costs; knockback is extra attack knockback; parry is the share
	 * of damage from in front that blocking with it stops (0: it does not block). Sword kinds sweep, mine cobwebs and
	 * take sword enchantments; pierce kinds thrust through every target in line, as the spear does.
	 */
	public record Kind(String name, float damage, float speed, SwingAnimationType swing, int swingTicks, float minReach,
			float maxReach, float margin, float disable, int wear, float knockback, float parry, boolean sword, boolean pierce) {
	}

	public static final List<Kind> KINDS = List.of(
			new Kind("longsword", 4.0F, -2.7F, SwingAnimationType.WHACK, 8, 0.0F, 3.25F, 0.0F, 0.0F, 1, 0.0F, 0.6F, true, false),
			new Kind("greatsword", 7.0F, -3.2F, SwingAnimationType.WHACK, 11, 0.0F, 3.75F, 0.0F, 2.0F, 1, 0.5F, 0.0F, true, false),
			new Kind("rapier", 1.5F, -2.0F, SwingAnimationType.STAB, 5, 0.0F, 3.5F, 0.125F, 0.0F, 1, 0.0F, 0.35F, true, false),
			new Kind("flanged_mace", 6.0F, -3.1F, SwingAnimationType.WHACK, 9, 0.0F, 3.0F, 0.0F, 3.0F, 1, 0.0F, 0.0F, false, false),
			new Kind("war_hammer", 8.0F, -3.3F, SwingAnimationType.WHACK, 12, 0.0F, 3.0F, 0.0F, 5.0F, 2, 1.0F, 0.0F, false, false),
			new Kind("glaive", 6.0F, -3.1F, SwingAnimationType.WHACK, 10, 0.0F, 4.25F, 0.0F, 0.0F, 1, 0.0F, 0.0F, true, false),
			new Kind("halberd", 7.0F, -3.2F, SwingAnimationType.STAB, 12, 1.0F, 4.5F, 0.125F, 3.0F, 1, 0.0F, 0.0F, false, true),
			// Arms II (batch 45).
			new Kind("dagger", 1.0F, -1.7F, SwingAnimationType.STAB, 4, 0.0F, 2.5F, 0.0F, 0.0F, 1, 0.0F, 0.0F, false, false),
			new Kind("sabre", 2.0F, -2.2F, SwingAnimationType.WHACK, 6, 0.0F, 3.0F, 0.0F, 0.0F, 1, 0.0F, 0.0F, true, false),
			new Kind("estoc", 3.0F, -2.6F, SwingAnimationType.STAB, 7, 0.0F, 3.5F, 0.125F, 0.0F, 1, 0.0F, 0.0F, false, false),
			new Kind("battle_axe", 8.0F, -3.3F, SwingAnimationType.WHACK, 12, 0.0F, 3.25F, 0.0F, 5.0F, 2, 0.5F, 0.0F, false, false),
			new Kind("flail", 5.0F, -3.0F, SwingAnimationType.WHACK, 10, 0.0F, 3.25F, 0.0F, 0.0F, 1, 0.0F, 0.0F, false, false),
			new Kind("scythe", 5.0F, -3.0F, SwingAnimationType.WHACK, 10, 0.0F, 4.0F, 0.0F, 0.0F, 1, 0.0F, 0.0F, true, false),
			new Kind("quarterstaff", 2.0F, -2.4F, SwingAnimationType.WHACK, 7, 0.0F, 3.5F, 0.0F, 0.0F, 1, 1.0F, 0.5F, false, false),
			new Kind("pike", 5.0F, -3.2F, SwingAnimationType.STAB, 10, 2.0F, 5.0F, 0.125F, 0.0F, 1, 0.0F, 0.0F, false, false));

	/**
	 * What an Arms II kind does besides its numbers (tools/arms.py: TRAITS), worked by {@link ArmItem} on the server:
	 * BACKSTAB, a blow from behind deals more; SADDLE, more damage while riding; ARMOR_PIERCE, more the more armor the
	 * target wears; CHOP, mines wood as its metal's axe; DAZE, a hit slows; REAP, use on ripe crops to harvest and
	 * replant them; RIDERS, more damage against anything riding or ridden.
	 */
	public enum Trait {
		BACKSTAB, SADDLE, ARMOR_PIERCE, CHOP, DAZE, REAP, RIDERS
	}

	public static final Map<String, Trait> TRAITS = Map.of("dagger", Trait.BACKSTAB, "sabre", Trait.SADDLE, "estoc",
			Trait.ARMOR_PIERCE, "battle_axe", Trait.CHOP, "flail", Trait.DAZE, "scythe", Trait.REAP, "pike", Trait.RIDERS);
	/** A backstab, within BACKSTAB_ANGLE degrees of straight behind the target's body, deals BACKSTAB of the blow more. */
	public static final float BACKSTAB = 0.5F;
	public static final float BACKSTAB_ANGLE = 70.0F;
	/** A sabre deals SADDLE more while its wielder rides. */
	public static final float SADDLE = 3.0F;
	/** An estoc deals ARMOR_PIERCE more per point of the target's armor, at most ARMOR_PIERCE_MAX. */
	public static final float ARMOR_PIERCE = 0.3F;
	public static final float ARMOR_PIERCE_MAX = 6.0F;
	/** A flail's hit slows the target for DAZE_TICKS at Slowness amplifier DAZE_AMPLIFIER. */
	public static final int DAZE_TICKS = 40;
	public static final int DAZE_AMPLIFIER = 1;
	/** A scythe reaps every ripe crop within REAP_RADIUS of the one used on, at REAP_WEAR durability each. */
	public static final int REAP_RADIUS = 1;
	public static final int REAP_WEAR = 1;
	/** A pike deals RIDERS of the blow more against anything riding or ridden. */
	public static final float RIDERS = 0.5F;

	/**
	 * A charging kind in one metal (tools/arms.py: CHARGE), as {@link Item.Properties#spear} takes it: jab duration (s),
	 * charge damage multiplier, charge delay (s), and for unhorsing, knockback and damage the longest a charge counts
	 * (s) and the speed it needs.
	 */
	public record Charge(String name, String metal, float jab, float multiplier, float delay, float dismountTime,
			float dismountSpeed, float knockbackTime, float knockbackSpeed, float damageTime, float damageSpeed) {
	}

	public static final List<Charge> CHARGES = List.of(
			new Charge("spear", "bronze", 0.95F, 1.0F, 0.6F, 2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F),
			new Charge("lance", "bronze", 1.25F, 1.3F, 0.9F, 3.5F, 9.0F, 8.0F, 4.5F, 14.0F, 4.0F),
			new Charge("spear", "steel", 1.0F, 1.04F, 0.55F, 2.75F, 10.5F, 6.6F, 5.1F, 10.6F, 4.6F),
			new Charge("lance", "steel", 1.3F, 1.4F, 0.8F, 4.0F, 8.5F, 8.5F, 4.5F, 15.0F, 4.0F));

	/** The lance's jab adds this to the spear's, and reaches farther: from LANCE_MIN_REACH to LANCE_MAX_REACH blocks. */
	public static final float LANCE_DAMAGE = 1.0F;
	public static final float LANCE_MIN_REACH = 2.5F;
	public static final float LANCE_MAX_REACH = 5.5F;
	/** Creative players reach this much farther, as with vanilla's spears. */
	public static final float CREATIVE_REACH = 2.0F;
	/** A parry covers this many degrees either side of straight ahead (a shield: 90) and blocks after this long (s). */
	public static final float PARRY_ANGLE = 60.0F;
	public static final float PARRY_DELAY = 0.1F;
	/** A parried hit of PARRY_WEAR_THRESHOLD or more wears the arm by PARRY_WEAR_BASE plus PARRY_WEAR_FACTOR of it. */
	public static final float PARRY_WEAR_THRESHOLD = 3.0F;
	public static final float PARRY_WEAR_BASE = 1.0F;
	public static final float PARRY_WEAR_FACTOR = 0.5F;

	/** Every arm, by id, in registration order: each metal's kinds, then its spear and lance. */
	public static final Map<String, Item> ITEMS = new LinkedHashMap<>();

	private JugcraftArms() {
	}

	public static void register() {
		for (String metal : METALS) {
			ToolMaterial material = metal.equals("bronze") ? JugcraftGear.BRONZE : JugcraftGear.STEEL;
			for (Kind kind : KINDS) {
				item(metal + "_" + kind.name(), kind.name(), properties -> arm(properties, material, kind));
			}
			for (Charge charge : CHARGES) {
				if (charge.metal().equals(metal)) {
					item(metal + "_" + charge.name(), charge.name(), properties -> charge(properties, material, charge));
				}
			}
		}
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> ITEMS.values().forEach(item -> output.accept(item)));
	}

	/** A swung or thrust arm: the metal's durability, repair and enchantability, and the kind's traits. */
	static Item.Properties arm(Item.Properties properties, ToolMaterial material, Kind kind) {
		// Swords' kinds start as a sword (cobwebs, no breaking blocks in creative), a chopping kind as an axe (wood); the
		// others mine nothing, like the mace.
		Item.Properties base = kind.sword() ? properties.sword(material, kind.damage(), kind.speed())
				: TRAITS.get(kind.name()) == Trait.CHOP ? properties.axe(material, kind.damage(), kind.speed())
				: properties.durability(material.durability()).repairable(material.repairItems())
						.enchantable(material.enchantmentValue()).component(DataComponents.TOOL, new Tool(List.of(), 1.0F, 2, false));
		base.attributes(attributes(material.attackDamageBonus() + kind.damage(), kind.speed(), kind.knockback()))
				.component(DataComponents.WEAPON, new Weapon(kind.wear(), kind.disable()))
				.component(DataComponents.ATTACK_ANIMATION, new SwingAnimation(kind.swing(), kind.swingTicks()))
				.component(DataComponents.ATTACK_RANGE, new AttackRange(kind.minReach(), kind.maxReach(), kind.minReach(),
						kind.maxReach() + CREATIVE_REACH, kind.margin(), 1.0F));
		if (kind.parry() > 0.0F) {
			// Blocking as a shield does, against what a shield blocks, but only part of the blow and over a narrower arc.
			// The damage types are a dynamic registry, so this is filled in once they load, as the shield's is.
			base.delayedComponent(DataComponents.BLOCKS_ATTACKS, registries -> new BlocksAttacks(PARRY_DELAY, 1.0F,
					List.of(new BlocksAttacks.DamageReduction(PARRY_ANGLE, Optional.empty(), 0.0F, kind.parry())),
					new BlocksAttacks.ItemDamageFunction(PARRY_WEAR_THRESHOLD, PARRY_WEAR_BASE, PARRY_WEAR_FACTOR),
					Optional.of(registries.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)), Optional.of(SoundEvents.SHIELD_BLOCK),
					Optional.of(SoundEvents.SHIELD_BREAK)));
		}
		if (kind.pierce()) {
			base.component(DataComponents.PIERCING_WEAPON, new PiercingWeapon(true, false, Optional.of(SoundEvents.SPEAR_ATTACK),
					Optional.of(SoundEvents.SPEAR_HIT)));
		}
		return base;
	}

	/** A charging arm: vanilla's spear with the charge's numbers; the lance jabs harder and reaches farther. */
	static Item.Properties charge(Item.Properties properties, ToolMaterial material, Charge charge) {
		Item.Properties spear = properties.spear(material, charge.jab(), charge.multiplier(), charge.delay(), charge.dismountTime(),
				charge.dismountSpeed(), charge.knockbackTime(), charge.knockbackSpeed(), charge.damageTime(), charge.damageSpeed());
		if (charge.name().equals("lance")) {
			spear.attributes(attributes(material.attackDamageBonus() + LANCE_DAMAGE, 1.0F / charge.jab() - 4.0F, 0.0F))
					.component(DataComponents.ATTACK_RANGE, new AttackRange(LANCE_MIN_REACH, LANCE_MAX_REACH, LANCE_MIN_REACH,
							LANCE_MAX_REACH + CREATIVE_REACH, 0.125F, 0.5F));
		}
		return spear;
	}

	private static ItemAttributeModifiers attributes(float damage, float speed, float knockback) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage,
						AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, speed,
						AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		if (knockback > 0.0F) {
			builder.add(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(Jugcraft.id("arms_knockback"), knockback,
					AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		}
		return builder.build();
	}

	private static void item(String name, String kind, Function<Item.Properties, Item.Properties> traits) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Jugcraft.id(name));
		ITEMS.put(name, Registry.register(BuiltInRegistries.ITEM, key, new ArmItem(kind, traits.apply(new Item.Properties().setId(key)))));
	}
}
