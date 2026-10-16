package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.chemistry.PetroItems;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
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
 * Porcelain insulators (docs/features/porcelain-insulators.md): two clay, a quartz and a bone meal prepare two porcelain
 * body; three body stack into an unfired insulator; a furnace or blast furnace fires it into a porcelain insulator; and
 * two of those are the Main Transformer's bushings, where white terracotta no longer serves.
 */
public class PorcelainInsulatorGameTests {
	@GameTest
	public void porcelainIsPreparedShapedAndFired(GameTestHelper helper) {
		ItemStack clay = new ItemStack(Items.CLAY_BALL);
		CraftingInput mix = CraftingInput.of(2, 2, List.of(clay, clay, new ItemStack(Items.QUARTZ), new ItemStack(Items.BONE_MEAL)));
		ItemStack body = crafts(helper, mix);
		helper.assertTrue(body.is(PetroItems.PORCELAIN_BODY) && body.getCount() == 2, "Clay, quartz and bone meal give " + body);
		ItemStack one = new ItemStack(PetroItems.PORCELAIN_BODY);
		ItemStack blank = crafts(helper, CraftingInput.of(1, 3, List.of(one, one, one)));
		helper.assertTrue(blank.is(PetroItems.UNFIRED_PORCELAIN_INSULATOR), "Three body do not shape a blank: " + blank);
		helper.assertTrue(fire(helper, RecipeType.SMELTING).is(PetroItems.PORCELAIN_INSULATOR), "A furnace does not fire the blank");
		helper.assertTrue(fire(helper, RecipeType.BLASTING).is(PetroItems.PORCELAIN_INSULATOR), "A blast furnace does not fire the blank");
		helper.succeed();
	}

	@GameTest
	public void porcelainInsulatorsGoIntoTheMainTransformer(GameTestHelper helper) {
		ItemStack cable = stack("aluminum_cable");
		ItemStack copper = vanilla("copper_block");
		ItemStack plate = stack("steel_plate");
		ItemStack insulator = new ItemStack(PetroItems.PORCELAIN_INSULATOR);
		ItemStack transformer = crafts(helper, CraftingInput.of(3, 3, List.of(insulator, cable, insulator, copper, stack("steel_block"), copper,
				plate, plate, plate)));
		helper.assertTrue(transformer.is(stack("main_transformer").getItem()), "Porcelain insulators do not complete the Main Transformer");
		ItemStack terracotta = vanilla("white_terracotta");
		ItemStack old = crafts(helper, CraftingInput.of(3, 3, List.of(terracotta, cable, terracotta, copper, stack("steel_block"), copper,
				plate, plate, plate)));
		helper.assertTrue(old.isEmpty(), "The Main Transformer is still made with white terracotta");
		helper.succeed();
	}

	private static <T extends Recipe<SingleRecipeInput>> ItemStack fire(GameTestHelper helper, RecipeType<T> type) {
		SingleRecipeInput blank = new SingleRecipeInput(new ItemStack(PetroItems.UNFIRED_PORCELAIN_INSULATOR));
		return helper.getLevel().getServer().getRecipeManager().getRecipeFor(type, blank, helper.getLevel())
				.map(holder -> holder.value().assemble(blank)).orElse(ItemStack.EMPTY);
	}

	private static ItemStack crafts(GameTestHelper helper, CraftingInput input) {
		Optional<RecipeHolder<CraftingRecipe>> recipe = RecipeManager.createCheck(RecipeType.CRAFTING).getRecipeFor(input, helper.getLevel());
		return recipe.map(holder -> holder.value().assemble(input)).orElse(ItemStack.EMPTY);
	}

	/** A vanilla item by ID (copper blocks and terracotta are not plain {@code Items} fields in this version). */
	private static ItemStack vanilla(String path) {
		return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(path)));
	}

	private static ItemStack stack(String path) {
		return new ItemStack(BuiltInRegistries.ITEM.getValue(Jugcraft.id(path)));
	}
}
