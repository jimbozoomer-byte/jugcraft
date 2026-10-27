package io.github.jimbozoomer.jugcraft.chemistry;

import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.Set;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;

/**
 * Found plastic (docs/features/found-plastic.md): old plastic bottles, ducks and crates in the chests of ruins, wrecks
 * and camps, each broken down by hand into plastic pellets (tools/found_plastic.py). Only chests that generate once with
 * a structure get them, so they cannot be farmed.
 */
public final class FoundPlastic {
	/** The chests that get the pool: wrecks, ruins, mines, dungeons and raiders' camps. */
	public static final Set<String> CHESTS = Set.of(
			"minecraft:chests/shipwreck_supply", "minecraft:chests/buried_treasure",
			"minecraft:chests/underwater_ruin_small", "minecraft:chests/underwater_ruin_big",
			"minecraft:chests/abandoned_mineshaft", "minecraft:chests/simple_dungeon", "minecraft:chests/ruined_portal",
			"minecraft:chests/pillager_outpost",
			"jugcraft:chests/raider_camp", "jugcraft:chests/landscape_desert_outpost",
			"jugcraft:chests/landscape_camp_loot_table", "jugcraft:chests/menagerie_campsite",
			"jugcraft:chests/menagerie_ruins", "jugcraft:chests/cavern_abyssal_ruins",
			"jugcraft:chests/cavern_forlorn_ruins", "jugcraft:chests/cavern_magnetic_ruins",
			"jugcraft:chests/cavern_toxic_ruins");
	/** The chance a chest has found plastic at all, and how many things it then holds. */
	public static final float CHANCE = 0.3F;
	public static final int ROLLS = 2;

	private FoundPlastic() {
	}

	static void register() {
		LootTableEvents.MODIFY.register((key, table, source, registries) -> {
			if (!source.isBuiltin() || !CHESTS.contains(key.identifier().toString())
					|| !JugcraftConfig.isFeatureEnabled("machines")) {
				return;
			}
			// A bottle is the commonest find and a crate the rarest: 1, 2 and 4 pellets.
			table.withPool(LootPool.lootPool().setRolls(Holder.direct(new ConstantValue(ROLLS)))
					.when(LootItemRandomChanceCondition.randomChance(CHANCE))
					.add(LootItem.lootTableItem(PetroItems.OLD_PLASTIC_BOTTLE).setWeight(6))
					.add(LootItem.lootTableItem(PetroItems.PLASTIC_DUCK).setWeight(3))
					.add(LootItem.lootTableItem(PetroItems.OLD_PLASTIC_CRATE).setWeight(2)));
		});
	}
}
