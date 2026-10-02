package io.github.jimbozoomer.jugcraft.gear;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;

/**
 * Bronze and steel tools and armor, and paxels for every tier (batch 25, docs/features/tools-and-armor.md), after
 * Mekanism: Tools (MIT); none of its code or art is used. Plain vanilla-style items: the 26.3 item properties do the
 * work. Keep the lists in sync with tools/gear.py; tools/check_mod_data.py checks them.
 */
public final class JugcraftGear {
	/** Metals with a full set of tools and armor (tools/gear.py: GEAR_TIERS). */
	public static final List<String> TIERS = List.of("bronze", "steel");
	public static final List<String> PIECES = List.of("sword", "pickaxe", "axe", "shovel", "hoe", "helmet", "chestplate",
			"leggings", "boots");
	/** Paxel tiers (tools/gear.py: PAXEL_TIERS). */
	public static final List<String> PAXEL_TIERS = List.of("wood", "stone", "iron", "gold", "diamond", "netherite", "bronze",
			"steel");
	/** A paxel lasts as long as the three tools it is made from. */
	public static final int PAXEL_DURABILITY = 3;
	/** What a paxel mines fast: everything a pickaxe, axe or shovel does (data/jugcraft/tags/block/mineable/paxel.json). */
	public static final TagKey<Block> MINEABLE_WITH_PAXEL = TagKey.create(Registries.BLOCK, Jugcraft.id("mineable/paxel"));

	/** Bronze: iron-tier drops, a little more durable and quicker than iron (iron: 250, 6.0, 2.0, 14). */
	public static final ToolMaterial BRONZE = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 320, 6.5F, 2.0F, 14,
			repairs("bronze"));
	/** Steel: diamond-tier drops, between iron and diamond in everything else (diamond: 1561, 8.0, 3.0, 10). */
	public static final ToolMaterial STEEL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 900, 7.0F, 2.5F, 12,
			repairs("steel"));
	/** Bronze armor: iron's defense with a little toughness. */
	public static final ArmorMaterial BRONZE_ARMOR = new ArmorMaterial(15, defense(2, 5, 6, 2), 12,
			SoundEvents.ARMOR_EQUIP_IRON, 0.5F, 0.0F, repairs("bronze"), asset("bronze"));
	/** Steel armor: between iron and diamond. */
	public static final ArmorMaterial STEEL_ARMOR = new ArmorMaterial(25, defense(3, 6, 7, 3), 10,
			SoundEvents.ARMOR_EQUIP_IRON, 1.5F, 0.05F, repairs("steel"), asset("steel"));

	/** Every item, by id, in registration order. */
	public static final Map<String, Item> ITEMS = new LinkedHashMap<>();

	private JugcraftGear() {
	}

	public static void register() {
		set("bronze", BRONZE, BRONZE_ARMOR, 6.0F, -3.1F, -2.0F, -1.0F);
		set("steel", STEEL, STEEL_ARMOR, 5.5F, -3.0F, -2.5F, 0.0F);
		for (String tier : PAXEL_TIERS) {
			ToolMaterial base = baseMaterial(tier);
			ToolMaterial paxel = new ToolMaterial(base.incorrectBlocksForDrops(), base.durability() * PAXEL_DURABILITY,
					base.speed(), base.attackDamageBonus(), base.enchantmentValue(), base.repairItems());
			item(tier + "_paxel", properties -> {
				Item.Properties tool = properties.tool(paxel, MINEABLE_WITH_PAXEL, 5.0F, -3.0F, 0.0F);
				return new Item(tier.equals("netherite") ? tool.fireResistant() : tool);
			});
		}
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> ITEMS.forEach(
				(id, item) -> {
					if (!id.endsWith("_sword") && !isArmor(id)) {
						output.accept(item);
					}
				}));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> ITEMS.forEach((id, item) -> {
			if (id.endsWith("_sword") || isArmor(id)) {
				output.accept(item);
			}
		}));
	}

	/** One metal's sword, pickaxe, axe, shovel and hoe, then its helmet, chestplate, leggings and boots. */
	private static void set(String tier, ToolMaterial material, ArmorMaterial armor, float axeDamage, float axeSpeed,
			float hoeDamage, float hoeSpeed) {
		item(tier + "_sword", properties -> new Item(properties.sword(material, 3.0F, -2.4F)));
		item(tier + "_pickaxe", properties -> new Item(properties.pickaxe(material, 1.0F, -2.8F)));
		item(tier + "_axe", properties -> new Item(properties.axe(material, axeDamage, axeSpeed)));
		item(tier + "_shovel", properties -> new Item(properties.shovel(material, 1.5F, -3.0F)));
		item(tier + "_hoe", properties -> new Item(properties.hoe(material, hoeDamage, hoeSpeed)));
		item(tier + "_helmet", properties -> new Item(properties.humanoidArmor(armor, ArmorType.HELMET)));
		item(tier + "_chestplate", properties -> new Item(properties.humanoidArmor(armor, ArmorType.CHESTPLATE)));
		item(tier + "_leggings", properties -> new Item(properties.humanoidArmor(armor, ArmorType.LEGGINGS)));
		item(tier + "_boots", properties -> new Item(properties.humanoidArmor(armor, ArmorType.BOOTS)));
	}

	private static ToolMaterial baseMaterial(String tier) {
		return switch (tier) {
			case "wood" -> ToolMaterial.WOOD;
			case "stone" -> ToolMaterial.STONE;
			case "iron" -> ToolMaterial.IRON;
			case "gold" -> ToolMaterial.GOLD;
			case "diamond" -> ToolMaterial.DIAMOND;
			case "netherite" -> ToolMaterial.NETHERITE;
			case "bronze" -> BRONZE;
			case "steel" -> STEEL;
			default -> throw new IllegalArgumentException(tier);
		};
	}

	private static boolean isArmor(String id) {
		return id.endsWith("_helmet") || id.endsWith("_chestplate") || id.endsWith("_leggings") || id.endsWith("_boots");
	}

	/** Defense by piece; the body value is only for animal armor and unused here. */
	private static Map<ArmorType, Integer> defense(int boots, int leggings, int chestplate, int helmet) {
		return Map.of(ArmorType.BOOTS, boots, ArmorType.LEGGINGS, leggings, ArmorType.CHESTPLATE, chestplate,
				ArmorType.HELMET, helmet, ArmorType.BODY, chestplate);
	}

	private static TagKey<Item> repairs(String metal) {
		return TagKey.create(Registries.ITEM, Jugcraft.id("repairs_" + metal + "_gear"));
	}

	private static ResourceKey<EquipmentAsset> asset(String metal) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, Jugcraft.id(metal));
	}

	private static void item(String name, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Jugcraft.id(name));
		ITEMS.put(name, Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key))));
	}
}
