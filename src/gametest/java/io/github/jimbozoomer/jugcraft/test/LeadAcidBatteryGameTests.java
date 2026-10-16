package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.JugcraftMachines;
import io.github.jimbozoomer.jugcraft.machine.MachineBlockEntity;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * The lead-acid battery (docs/features/lead-acid-battery.md): a battery between the battery box and the capacitor bank,
 * 1,600,000 JE at 1,024 JE/t, crafted from four lead plates, a bucket of sulfuric acid (given back), two steel plates, a
 * battery box and a copper cable.
 */
public class LeadAcidBatteryGameTests {
	@GameTest
	public void leadAcidBatteryStoresBetweenTheBoxAndTheBank(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, JugcraftMachines.MACHINES.get(MachineKind.LEAD_ACID_BATTERY).defaultBlockState());
		MachineBlockEntity machine = helper.getBlockEntity(pos, MachineBlockEntity.class);
		SimpleEnergyStorage energy = (SimpleEnergyStorage) machine.energyFor(null);
		helper.assertTrue(energy.getCapacity() == 1_600_000, "It holds " + energy.getCapacity());
		helper.assertTrue(MachineKind.LEAD_ACID_BATTERY.isBattery(), "It is not a battery");
		helper.assertTrue(MachineKind.BATTERY_BOX.capacity < MachineKind.LEAD_ACID_BATTERY.capacity
				&& MachineKind.LEAD_ACID_BATTERY.capacity < MachineKind.CAPACITOR_BANK.capacity, "It is not between the box and the bank");
		helper.succeed();
	}

	@GameTest
	public void leadAcidBatteryCraftsAndGivesTheBucketBack(GameTestHelper helper) {
		ItemStack lead = stack("lead_plate");
		ItemStack steel = stack("steel_plate");
		CraftingInput input = CraftingInput.of(3, 3, List.of(lead, stack("sulfuric_acid_bucket"), lead, steel, stack("battery_box"), steel,
				lead, stack("copper_cable"), lead));
		Optional<RecipeHolder<CraftingRecipe>> recipe = RecipeManager.createCheck(RecipeType.CRAFTING).getRecipeFor(input, helper.getLevel());
		helper.assertTrue(recipe.isPresent() && recipe.get().value().assemble(input).is(stack("lead_acid_battery").getItem()),
				"Lead, acid, steel and a battery box do not make a lead-acid battery");
		NonNullList<ItemStack> left = recipe.get().value().getRemainingItems(input);
		helper.assertTrue(left.get(1).is(Items.BUCKET), "The acid's bucket is not given back: " + left);
		helper.succeed();
	}

	private static ItemStack stack(String path) {
		return new ItemStack(BuiltInRegistries.ITEM.getValue(Jugcraft.id(path)));
	}
}
