package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.chemistry.FluidRecipes;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import io.github.jimbozoomer.jugcraft.weapons.FieldChemistry;
import io.github.jimbozoomer.jugcraft.pharmacology.Pharmacology;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

/**
 * Aspirin (docs/features/aspirin.md): the Chemical Reactor steeps willow (two) or birch (one) into salicylic acid and
 * processes two with cider vinegar and 50 mB sulfuric acid into four tablets. The stable item now grants
 * Fortitude through the shared pharmacy ledger; it does not clear unrelated conditions.
 */
public class AspirinGameTests {
	@GameTest
	public void theReactorMakesSalicylicAcidAndAspirin(GameTestHelper helper) {
		List<FluidRecipes.Entry> reactor = FluidRecipes.entries(helper.getLevel().getServer(), MachineKind.CHEMICAL_REACTOR);
		helper.assertTrue(makes(reactor, "salicylic_acid_willow", FieldChemistry.SALICYLIC_ACID, 2), "Willow gives two salicylic acid");
		helper.assertTrue(makes(reactor, "salicylic_acid_birch", FieldChemistry.SALICYLIC_ACID, 1), "Birch gives one salicylic acid");
		helper.assertTrue(makes(reactor, "aspirin", FieldChemistry.ASPIRIN, 4), "Two salicylic acid make four aspirin");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void aspirinUsesFortitudeAndPreservesUnrelatedEffects(GameTestHelper helper) {
		var player = TestPlayers.survivor(helper);
		player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 400, 0));
		player.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 0));
		ItemStack tablet = new ItemStack(FieldChemistry.ASPIRIN);
		tablet.finishUsingItem(helper.getLevel(), player);
		for (int second = 0; second < 5; second++) {
			Pharmacology.tickSecond(player);
		}
		helper.assertTrue(player.hasEffect(Pharmacology.FORTITUDE) && Pharmacology.strain(player) == 10,
				"Aspirin now grants Fortitude after onset with 10 strain");
		helper.assertTrue(!player.hasEffect(MobEffects.REGENERATION), "The old regeneration effect is no longer applied");
		helper.assertTrue(player.hasEffect(MobEffects.POISON) && player.hasEffect(MobEffects.NAUSEA), "Unrelated conditions are preserved");
		helper.assertTrue(!Pharmacology.mayDrink(player, Pharmacology.ASPIRIN, false), "The shared ledger blocks immediate reuse");
		helper.succeed();
	}

	private static boolean makes(List<FluidRecipes.Entry> recipes, String name, net.minecraft.world.item.Item item, int count) {
		return recipes.stream().filter(entry -> entry.id().equals(Jugcraft.id("chemical_reaction/" + name))).findFirst()
				.map(entry -> {
					ItemStack result = entry.recipe().results().getFirst().create();
					return result.is(item) && result.getCount() == count;
				}).orElse(false);
	}
}
