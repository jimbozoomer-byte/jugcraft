package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;

/**
 * Titanium gear (docs/features/titanium-gear.md): titanium ingots craft a pickaxe that mines obsidian; every titanium
 * tool and armor piece lasts longer than steel's and less than diamond's; the armor goes in the right slots.
 */
public class TitaniumGearGameTests {
	@GameTest
	public void titaniumIngotsCraftAnObsidianPickaxe(GameTestHelper helper) {
		ItemStack ingot = new ItemStack(item("titanium_ingot"));
		ItemStack stick = new ItemStack(Items.STICK);
		ItemStack none = ItemStack.EMPTY;
		ItemStack pickaxe = crafts(helper, CraftingInput.of(3, 3, List.of(ingot, ingot, ingot, none, stick, none, none, stick, none)));
		helper.assertTrue(pickaxe.is(item("titanium_pickaxe")), "Three titanium ingots and two sticks craft " + pickaxe);
		helper.assertTrue(pickaxe.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "A titanium pickaxe cannot get obsidian");
		helper.succeed();
	}

	@GameTest
	public void titaniumOutlastsSteelAndNotDiamond(GameTestHelper helper) {
		for (String piece : List.of("sword", "pickaxe", "axe", "shovel", "hoe")) {
			int titanium = new ItemStack(item("titanium_" + piece)).getMaxDamage();
			int steel = new ItemStack(item("steel_" + piece)).getMaxDamage();
			int diamond = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(
					"diamond_" + piece))).getMaxDamage();
			helper.assertTrue(steel < titanium && titanium < diamond,
					"A titanium " + piece + " lasts " + titanium + " (steel " + steel + ", diamond " + diamond + ")");
		}
		for (String[] piece : new String[][] {{"helmet", "HEAD"}, {"chestplate", "CHEST"}, {"leggings", "LEGS"}, {"boots", "FEET"}}) {
			ItemStack stack = new ItemStack(item("titanium_" + piece[0]));
			var equippable = stack.get(DataComponents.EQUIPPABLE);
			helper.assertTrue(equippable != null && equippable.slot() == EquipmentSlot.valueOf(piece[1]),
					"Titanium " + piece[0] + " does not go in " + piece[1]);
			int steel = new ItemStack(item("steel_" + piece[0])).getMaxDamage();
			helper.assertTrue(stack.getMaxDamage() > steel, "Titanium " + piece[0] + " lasts " + stack.getMaxDamage() + ", steel " + steel);
		}
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
