package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.chemistry.PetroItems;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

/**
 * Alumina ceramics (docs/features/alumina-ceramics.md): two alumina and a clay ball press into an unfired substrate;
 * only a blast furnace fires it into an alumina substrate (an ordinary furnace is not hot enough); and the circuit
 * assembler can bond a processor's chips on one in place of gold.
 */
public class AluminaCeramicsGameTests {
	@GameTest
	public void aluminaIsPressedAndFiredOnlyInABlastFurnace(GameTestHelper helper) {
		ItemStack alumina = new ItemStack(BuiltInRegistries.ITEM.getValue(Jugcraft.id("alumina")));
		CraftingInput press = CraftingInput.of(3, 1, List.of(alumina, alumina, new ItemStack(Items.CLAY_BALL)));
		Optional<RecipeHolder<CraftingRecipe>> pressed = RecipeManager.createCheck(RecipeType.CRAFTING).getRecipeFor(press, helper.getLevel());
		ItemStack blank = pressed.map(r -> r.value().assemble(press)).orElse(ItemStack.EMPTY);
		helper.assertTrue(blank.is(PetroItems.UNFIRED_ALUMINA_SUBSTRATE), "Alumina and clay press into " + blank);
		helper.assertTrue(fire(helper, RecipeType.BLASTING).is(PetroItems.ALUMINA_SUBSTRATE), "A blast furnace does not fire the substrate");
		helper.assertTrue(fire(helper, RecipeType.SMELTING).isEmpty(), "An ordinary furnace fires alumina");
		helper.succeed();
	}

	@GameTest
	public void processorsCanBeBondedOnAlumina(GameTestHelper helper) {
		var recipe = helper.getLevel().recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id("circuit_assembly/processor_on_alumina")));
		helper.assertTrue(recipe.isPresent(), "The circuit assembler has no alumina processor recipe");
		helper.succeed();
	}

	private static <T extends Recipe<SingleRecipeInput>> ItemStack fire(GameTestHelper helper, RecipeType<T> type) {
		SingleRecipeInput blank = new SingleRecipeInput(new ItemStack(PetroItems.UNFIRED_ALUMINA_SUBSTRATE));
		return helper.getLevel().getServer().getRecipeManager().getRecipeFor(type, blank, helper.getLevel())
				.map(holder -> holder.value().assemble(blank)).orElse(ItemStack.EMPTY);
	}
}
