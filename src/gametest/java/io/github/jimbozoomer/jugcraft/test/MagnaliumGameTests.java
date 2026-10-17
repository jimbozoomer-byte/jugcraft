package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import io.github.jimbozoomer.jugcraft.machine.MachineRecipes;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Magnalium (docs/features/magnalium.md): the alloy smelter makes four magnalium ingots from three aluminum and one
 * magnesium; the Metal Press presses an ingot into a plate; four plates craft six aluminum cladding, and four plates and a
 * stick two propeller sets.
 */
public class MagnaliumGameTests {
	@GameTest
	public void alloySmelterMakesMagnalium(GameTestHelper helper) {
		List<ItemStack> inputs = List.of(new ItemStack(item("aluminum_ingot"), 3), new ItemStack(item("magnesium_ingot")));
		MachineRecipes.MultiMatch match = MachineRecipes.findMulti(helper.getLevel(), MachineKind.ALLOY_SMELTER, inputs)
				.orElseThrow(() -> helper.assertionException("The alloy smelter does not alloy aluminum with magnesium"));
		ItemStack ingots = match.recipe().output().create();
		helper.assertTrue(ingots.is(item("magnalium_ingot")) && ingots.getCount() == 4, "The alloy smelter makes " + ingots);
		helper.succeed();
	}

	@GameTest
	public void metalPressMakesMagnaliumPlate(GameTestHelper helper) {
		ItemStack plate = MachineRecipes.find(helper.getLevel(), MachineKind.METAL_PRESS, new ItemStack(item("magnalium_ingot")))
				.orElseThrow(() -> helper.assertionException("The Metal Press does not press magnalium")).output().create();
		helper.assertTrue(plate.is(item("magnalium_plate")) && plate.getCount() == 1, "The Metal Press makes " + plate);
		helper.succeed();
	}

	@GameTest
	public void magnaliumPlatesMakeCladdingAndPropellers(GameTestHelper helper) {
		ItemStack plate = new ItemStack(item("magnalium_plate"));
		ItemStack cladding = crafts(helper, CraftingInput.of(2, 2, List.of(plate, plate, plate, plate)));
		helper.assertTrue(cladding.is(item("aluminum_cladding")) && cladding.getCount() == 6, "Four plates craft " + cladding);
		ItemStack stick = new ItemStack(Items.STICK);
		ItemStack none = ItemStack.EMPTY;
		ItemStack propellers = crafts(helper, CraftingInput.of(3, 3, List.of(plate, none, plate, none, stick, none, plate, none, plate)));
		helper.assertTrue(propellers.is(item("aluminum_propeller_set")) && propellers.getCount() == 2,
				"Four plates and a stick craft " + propellers);
		helper.succeed();
	}

	private static Item item(String path) {
		return BuiltInRegistries.ITEM.getValue(Jugcraft.id(path));
	}

	private static ItemStack crafts(GameTestHelper helper, CraftingInput input) {
		RecipeManager.CachedCheck<CraftingInput, CraftingRecipe> crafting = RecipeManager.createCheck(RecipeType.CRAFTING);
		Optional<RecipeHolder<CraftingRecipe>> recipe = crafting.getRecipeFor(input, helper.getLevel());
		return recipe.map(holder -> holder.value().assemble(input)).orElse(ItemStack.EMPTY);
	}
}
