package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.gear.JugcraftGear;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Blocks;

/**
 * Titanium tools and armor (docs/features/titanium-gear.md): the step above steel. Its pickaxe gets obsidian, its tools
 * outlast and outmine steel's, its armor protects more and is tougher, every piece goes in its slot, a titanium ingot
 * mends them, and they are crafted from titanium ingots.
 */
public class TitaniumGearGameTests {
	private static final String[] ARMOR = {"helmet", "chestplate", "leggings", "boots"};
	private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	/** Its tools reach obsidian, last longer and dig faster than steel's; the sword hits harder. */
	@GameTest
	public void titaniumToolsBeatSteel(GameTestHelper helper) {
		ItemStack pickaxe = new ItemStack(item("titanium_pickaxe"));
		ItemStack steel = new ItemStack(item("steel_pickaxe"));
		helper.assertTrue(pickaxe.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "A titanium pickaxe cannot get obsidian");
		helper.assertTrue(pickaxe.getMaxDamage() == 1300 && pickaxe.getMaxDamage() > steel.getMaxDamage(),
				"Titanium lasts " + pickaxe.getMaxDamage() + ", steel " + steel.getMaxDamage());
		float speed = pickaxe.getDestroySpeed(Blocks.STONE.defaultBlockState());
		helper.assertTrue(speed > steel.getDestroySpeed(Blocks.STONE.defaultBlockState()), "Titanium mines stone at " + speed);
		helper.assertTrue(JugcraftGear.TITANIUM.attackDamageBonus() > JugcraftGear.STEEL.attackDamageBonus(),
				"Titanium hits no harder than steel");
		helper.assertTrue(pickaxe.getMaxDamage() < new ItemStack(Items.NETHERITE_PICKAXE).getMaxDamage(), "Titanium outlasts netherite");
		helper.succeed();
	}

	/** Its armor goes in each slot, protects more than steel's and is tougher, and is mended with a titanium ingot. */
	@GameTest
	public void titaniumArmorBeatsSteel(GameTestHelper helper) {
		int titanium = 0;
		int steel = 0;
		for (ArmorType type : List.of(ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS)) {
			titanium += JugcraftGear.TITANIUM_ARMOR.defense().get(type);
			steel += JugcraftGear.STEEL_ARMOR.defense().get(type);
		}
		helper.assertTrue(titanium == 20 && titanium > steel, "A titanium set gives " + titanium + " armor, steel " + steel);
		helper.assertTrue(JugcraftGear.TITANIUM_ARMOR.toughness() > JugcraftGear.STEEL_ARMOR.toughness(), "Titanium is no tougher");
		TagKey<Item> repairs = TagKey.create(Registries.ITEM, Jugcraft.id("repairs_titanium_gear"));
		helper.assertTrue(new ItemStack(item("titanium_ingot")).is(repairs), "A titanium ingot does not mend titanium gear");
		for (int i = 0; i < ARMOR.length; i++) {
			ItemStack stack = new ItemStack(item("titanium_" + ARMOR[i]));
			var equippable = stack.get(DataComponents.EQUIPPABLE);
			helper.assertTrue(equippable != null && equippable.slot() == SLOTS[i], "titanium " + ARMOR[i] + " does not go in " + SLOTS[i]);
		}
		helper.succeed();
	}

	/** Three titanium ingots over two sticks make a titanium pickaxe; eight make a chestplate. */
	@GameTest
	public void titaniumGearIsCraftedFromIngots(GameTestHelper helper) {
		RecipeManager.CachedCheck<CraftingInput, CraftingRecipe> crafting = RecipeManager.createCheck(RecipeType.CRAFTING);
		ItemStack ingot = new ItemStack(item("titanium_ingot"));
		ItemStack stick = new ItemStack(Items.STICK);
		CraftingInput pickaxe = CraftingInput.of(3, 3, List.of(ingot, ingot, ingot, ItemStack.EMPTY, stick, ItemStack.EMPTY,
				ItemStack.EMPTY, stick, ItemStack.EMPTY));
		Optional<RecipeHolder<CraftingRecipe>> found = crafting.getRecipeFor(pickaxe, helper.getLevel());
		helper.assertTrue(found.isPresent() && found.get().value().assemble(pickaxe).is(item("titanium_pickaxe")),
				"Titanium ingots and sticks do not make a titanium pickaxe");
		List<ItemStack> chest = new ArrayList<>(List.of(ingot, ItemStack.EMPTY, ingot, ingot, ingot, ingot, ingot, ingot, ingot));
		CraftingInput chestplate = CraftingInput.of(3, 3, chest);
		found = crafting.getRecipeFor(chestplate, helper.getLevel());
		helper.assertTrue(found.isPresent() && found.get().value().assemble(chestplate).is(item("titanium_chestplate")),
				"Eight titanium ingots do not make a titanium chestplate");
		helper.succeed();
	}

	private static Item item(String path) {
		return BuiltInRegistries.ITEM.getValue(Jugcraft.id(path));
	}
}
