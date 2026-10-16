package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.chemistry.FertilizerItem;
import io.github.jimbozoomer.jugcraft.chemistry.FluidRecipe;
import io.github.jimbozoomer.jugcraft.chemistry.FluidRecipes;
import io.github.jimbozoomer.jugcraft.chemistry.PetroFluids;
import io.github.jimbozoomer.jugcraft.chemistry.PetroItems;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/**
 * Phosphoric acid (docs/features/phosphoric-acid.md): the Chemical Reactor dissolves two phosphate rock in 250 mB of water
 * (first tank) and 250 mB of sulfuric acid (second) into 250 mB of phosphoric acid and one calcium sulfate; phosphoric
 * acid on one more rock gives four triple superphosphate (a four-dose fertilizer); and four bottles and four sugar with
 * water, a little phosphoric acid and carbon dioxide give four cola, which gives Haste and leaves the bottle.
 */
public class PhosphoricAcidGameTests {
	@GameTest
	public void phosphateRockGivesPhosphoricAcid(GameTestHelper helper) {
		FluidRecipe recipe = reactor(helper, "phosphoric_acid");
		helper.assertTrue(recipe.fluids().get(0).fluid() == Fluids.WATER
				&& recipe.fluids().get(1).fluid() == PetroFluids.SULFURIC_ACID.source(), "Water first, then sulfuric acid");
		helper.assertTrue(recipe.fluidResults().getFirst().fluid() == PetroFluids.PHOSPHORIC_ACID.source()
				&& recipe.fluidResults().getFirst().amount() == 250, "It gives 250 mB of phosphoric acid");
		ItemStack residue = recipe.results().getFirst().create();
		helper.assertTrue(residue.is(BuiltInRegistries.ITEM.getValue(Jugcraft.id("calcium_sulfate"))) && residue.getCount() == 1,
				"It filters out one calcium sulfate: " + residue);
		helper.succeed();
	}

	@GameTest
	public void phosphoricAcidMakesTripleSuperphosphateAndCola(GameTestHelper helper) {
		ItemStack fertilizer = reactor(helper, "triple_superphosphate").results().getFirst().create();
		helper.assertTrue(fertilizer.is(PetroItems.TRIPLE_SUPERPHOSPHATE) && fertilizer.getCount() == 4, "Rock and acid give " + fertilizer);
		helper.assertTrue(PetroItems.TRIPLE_SUPERPHOSPHATE instanceof FertilizerItem
				&& PetroItems.TRIPLE_SUPERPHOSPHATE_DOSES > FertilizerItem.DOSES, "Triple superphosphate outdoes superphosphate");
		ItemStack cola = reactor(helper, "cola").results().getFirst().create();
		helper.assertTrue(cola.is(PetroItems.COLA) && cola.getCount() == 4, "Four bottles give " + cola);
		helper.succeed();
	}

	@GameTest(maxTicks = 20)
	public void colaGivesHasteAndTheBottle(GameTestHelper helper) {
		Mob pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(2, 1, 2));
		ItemStack left = new ItemStack(PetroItems.COLA).finishUsingItem(helper.getLevel(), pig);
		helper.assertTrue(pig.hasEffect(MobEffects.HASTE), "Cola gives no Haste");
		helper.assertTrue(left.is(Items.GLASS_BOTTLE), "Cola leaves " + left);
		helper.succeed();
	}

	private static FluidRecipe reactor(GameTestHelper helper, String name) {
		List<FluidRecipes.Entry> recipes = FluidRecipes.entries(helper.getLevel().getServer(), MachineKind.CHEMICAL_REACTOR);
		FluidRecipes.Entry entry = recipes.stream().filter(e -> e.id().equals(Jugcraft.id("chemical_reaction/" + name))).findFirst()
				.orElse(null);
		helper.assertTrue(entry != null, "No " + name + " recipe in the Chemical Reactor");
		return entry.recipe();
	}
}
