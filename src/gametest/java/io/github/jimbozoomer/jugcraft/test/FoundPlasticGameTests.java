package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.chemistry.FoundPlastic;
import io.github.jimbozoomer.jugcraft.chemistry.PetroItems;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/**
 * Found plastic (docs/features/found-plastic.md): old plastic bottles, ducks and crates turn up in the listed structure
 * chests and nowhere else, and each breaks down in the crafting grid into plastic pellets.
 */
public class FoundPlasticGameTests {
	@GameTest
	public void foundPlasticBreaksDownIntoPellets(GameTestHelper helper) {
		RecipeManager.CachedCheck<CraftingInput, CraftingRecipe> crafting = RecipeManager.createCheck(RecipeType.CRAFTING);
		for (Map.Entry<Item, Integer> found : Map.of(PetroItems.OLD_PLASTIC_BOTTLE, 1, PetroItems.PLASTIC_DUCK, 2,
				PetroItems.OLD_PLASTIC_CRATE, 4).entrySet()) {
			CraftingInput input = CraftingInput.of(1, 1, List.of(new ItemStack(found.getKey())));
			Optional<RecipeHolder<CraftingRecipe>> recipe = crafting.getRecipeFor(input, helper.getLevel());
			helper.assertTrue(recipe.isPresent(), found.getKey() + " does not break down");
			ItemStack pellets = recipe.get().value().assemble(input);
			helper.assertTrue(pellets.is(PetroItems.PLASTIC_PELLETS) && pellets.getCount() == found.getValue(),
					found.getKey() + " gives " + pellets + ", not " + found.getValue() + " plastic pellets");
		}
		helper.succeed();
	}

	@GameTest
	public void foundPlasticIsInTheListedChestsOnly(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (String chest : FoundPlastic.CHESTS) {
			helper.assertTrue(table(level, chest) != LootTable.EMPTY, chest + " did not load");
		}
		// At a 30% chance, 200 shipwreck chests without one would happen about once in 10^31 tries.
		helper.assertTrue(rollsFoundPlastic(helper, "minecraft:chests/shipwreck_supply", 200), "200 shipwreck supply chests held no found plastic");
		helper.assertTrue(rollsFoundPlastic(helper, "jugcraft:chests/raider_camp", 200), "200 raider camp chests held no found plastic");
		helper.assertTrue(!rollsFoundPlastic(helper, "minecraft:chests/end_city_treasure", 200), "An end city chest held found plastic");
		helper.succeed();
	}

	private static LootTable table(ServerLevel level, String id) {
		return level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(id)));
	}

	private static boolean rollsFoundPlastic(GameTestHelper helper, String chest, int rolls) {
		ServerLevel level = helper.getLevel();
		LootTable table = table(level, chest);
		for (int roll = 0; roll < rolls; roll++) {
			LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,
					helper.absoluteVec(new Vec3(1, 2, 1))).create(LootContextParamSets.CHEST);
			for (ItemStack stack : table.getRandomItems(params)) {
				if (stack.is(PetroItems.OLD_PLASTIC_BOTTLE) || stack.is(PetroItems.PLASTIC_DUCK) || stack.is(PetroItems.OLD_PLASTIC_CRATE)) {
					return true;
				}
			}
		}
		return false;
	}
}
