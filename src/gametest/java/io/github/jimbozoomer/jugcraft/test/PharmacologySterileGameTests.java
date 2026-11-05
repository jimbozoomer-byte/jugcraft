package io.github.jimbozoomer.jugcraft.test;

import static io.github.jimbozoomer.jugcraft.test.PharmacologyFactoryGameTests.*;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.chemistry.FluidRecipes;
import io.github.jimbozoomer.jugcraft.chemistry.PetroFluids;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import io.github.jimbozoomer.jugcraft.machine.form.*;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

public class PharmacologySterileGameTests {
	@GameTest(structure = "jugcraft-test:arms_arena")
	public void bothFermentersMakeBrothAndSeparatorRecoversExtract(GameTestHelper h) {
		for (var block : List.of(IndustrialForms.INDUSTRIAL_FERMENTER, IndustrialForms.BULK_FERMENTER)) {
			var machine = place(h, block, Direction.NORTH);
			var culture = recipe(machine, h, "sterile_azure_culture");
			feed(machine, culture);
			ticks(h, machine, culture.time());
			h.assertTrue(machine.lane(0) == null && machine.container().getItem(0).isEmpty()
					&& machine.tanks().input(0).millibuckets() == 0 && machine.tanks().output(0).millibuckets() == 500
					&& machine.tanks().output(0).has(PetroFluids.ANTIBIOTIC_BROTH.source(), 500)
					&& machine.tanks().output(1).millibuckets() == 0, "Both existing fermenters produce exact broth without exhaust");
			clear(h, machine);
		}
		var separator = place(h, IndustrialForms.PHARMACEUTICAL_SEPARATOR, Direction.NORTH);
		var extraction = recipe(separator, h, "sterile_antibiotic_extract");
		feed(separator, extraction);
		ticks(h, separator, extraction.time());
		h.assertTrue(separator.container().getItem(separator.form().firstOutputSlot()).is(SterilePharmacy.item("antibiotic_extract"))
				&& separator.container().getItem(separator.form().firstOutputSlot()).getCount() == 4, "Broth becomes four extract");
		h.succeed();
	}

	@GameTest(structure = "jugcraft-test:arms_arena")
	public void sterileFillerLocksFormulaAndResumesSavedBatchExactlyOnce(GameTestHelper h) {
		var machine = place(h, IndustrialForms.STERILE_FILLER, Direction.EAST);
		var recipe = recipe(machine, h, "sterile_fill_penicillin");
		feed(machine, recipe);
		int socket = machine.form().firstSocketSlot();
		machine.container().setItem(socket, new ItemStack(SterilePharmacy.item("morphine_formula_plate")));
		ticks(h, machine, 1);
		h.assertTrue(machine.lane(0) == null && machine.status().state() == MachineLifecycle.WAITING_TOOL
				&& machine.container().getItem(0).getCount() == 2, "Wrong sterile formula cannot spend stock");
		machine.container().setItem(socket, new ItemStack(SterilePharmacy.item("penicillin_formula_plate")));
		ticks(h, machine, 30);
		h.assertTrue(machine.lane(0) != null && machine.socketLocked(0), "Matching sterile formula is locked during work");
		var saved = machine.saveWithoutMetadata(h.getLevel().registryAccess());
		machine.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved));
		ticks(h, machine, 180);
		var result = machine.container().getItem(machine.form().firstOutputSlot());
		h.assertTrue(result.is(SterilePharmacy.item("penicillin")) && result.getCount() == 4
				&& machine.container().getItem(socket).getCount() == 1, "Saved batch completes once and keeps formula");
		h.succeed();
	}

	@GameTest
	public void stocksBootstrapWithoutFinishedMedicineAndHaveDistinctReactorInputs(GameTestHelper h) {
		var input = CraftingInput.of(3, 1, List.of(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("minecraft:blue_dye"))), new ItemStack(Items.BROWN_MUSHROOM), new ItemStack(Items.SUGAR)));
		var output = h.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel()).orElseThrow().value().assemble(input);
		h.assertTrue(output.is(SterilePharmacy.item("azure_culture_starter")) && output.getCount() == 2, "Culture starts from reachable fantasy ingredients");
		for (var block : List.of(IndustrialForms.AUTOCLAVE, IndustrialForms.STERILE_FILLER)) {
			String[] ids = {"jugcraft:stainless_steel_plate", "jugcraft:borosilicate_glass", "jugcraft:stainless_steel_plate",
					"jugcraft:electric_motor", "jugcraft:basic_circuit", "jugcraft:machine_casing", "jugcraft:stainless_steel_plate",
					"jugcraft:copper_cable", block == IndustrialForms.AUTOCLAVE ? "jugcraft:stainless_steel_plate" : "minecraft:piston"};
			var construction = CraftingInput.of(3, 3, java.util.Arrays.stream(ids)
					.map(id -> new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)))).toList());
			var crafted = h.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, construction, h.getLevel()).orElseThrow().value().assemble(construction);
			h.assertTrue(crafted.is(block.asItem()), "Distinct native construction recipe reaches " + block.form().id());
		}
		var all = FluidRecipes.entries(h.getLevel().getServer(), MachineKind.CHEMICAL_REACTOR);
		for (String stock : List.of("culture_feed", "lidocaine_stock", "adrenaline_stock", "salbutamol_stock", "redleaf_medical_stock", "naloxone_stock")) {
			var recipe = all.stream().filter(e -> e.id().equals(Jugcraft.id("chemical_reaction/sterile_" + stock))).findFirst().orElseThrow().recipe();
			h.assertTrue(recipe.legacy() && recipe.items().size() == 2 && recipe.results().getFirst().create().getCount() == 4, "Compact reactor can prepare " + stock);
			var first = new ItemStack(recipe.items().get(0).ingredient().items().findFirst().orElseThrow(), recipe.items().get(0).count());
			var second = new ItemStack(recipe.items().get(1).ingredient().items().findFirst().orElseThrow(), recipe.items().get(1).count());
			long matches = all.stream().map(e -> e.recipe()).filter(r -> r.legacy() && r.items().size() == 2
					&& r.items().get(0).ingredient().test(first) && r.items().get(0).count() <= first.getCount()
					&& r.items().get(1).ingredient().test(second) && r.items().get(1).count() <= second.getCount()
					&& r.fluids().size() == 1 && r.fluids().getFirst().fluid() == net.minecraft.world.level.material.Fluids.WATER
					&& r.fluids().getFirst().amount() <= 250).count();
			h.assertTrue(matches == 1, "No competing compact reactor route intercepts " + stock);
		}
		h.succeed();
	}

	@GameTest
	public void penicillinImprovesNativeFoodHealingAndSharesParacetamolFamily(GameTestHelper h) {
		var player = player(h);
		Pharmacology.drink(player, SterilePharmacy.PENICILLIN);
		h.assertTrue(Pharmacology.mayDrink(player, Pharmacology.PARACETAMOL, false), "The same family can be replaced rather than stacked");
		advance(player, 5);
		player.setHealth(10);
		player.getFoodData().setFoodLevel(18);
		player.getFoodData().setSaturation(0);
		for (int tick = 0; tick < 80; tick++) player.getFoodData().tick(player);
		h.assertTrue(Math.abs(player.getHealth() - 11.5F) < .001F && Math.abs(exhaustion(player.getFoodData()) - 9) < .001F,
				"Recovery II supplies 50% extra native healing with proportional food cost");
		h.succeed();
	}

	@GameTest
	public void lidocaineShieldIsGrantedOnceAndPotionShieldTakesPriority(GameTestHelper h) {
		var player = player(h);
		Pharmacology.drink(player, SterilePharmacy.LIDOCAINE);
		h.assertTrue(player.getAbsorptionAmount() == 0, "No shield before onset");
		advance(player, 5);
		h.assertTrue(player.getAbsorptionAmount() == 8 && Math.abs(player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) - .25) < .001,
				"One dose grants four hearts and quarter knockback resistance");
		player.setAbsorptionAmount(0);
		advance(player, 50);
		h.assertTrue(player.getAbsorptionAmount() == 0, "Spent absorption is never replenished by pulses");
		player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 12000, 2));
		Pharmacology.tickSecond(player);
		h.assertTrue(!player.hasEffect(SterilePharmacy.ABSORPTION) && player.getAbsorptionAmount() == 12, "Stronger potion shield takes priority without stacking capacity");
		player.removeAllEffects();
		Pharmacology.tickSecond(player);
		h.assertTrue(player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) == 0 && Pharmacology.strain(player) == 5, "Cleanse removes bonuses and preserves the decaying balance");
		h.succeed();
	}

	@GameTest
	public void adrenalineCanBeReplacedAndNaturalExpiryKeepsFatigueAndAccounting(GameTestHelper h) {
		var player = player(h);
		Pharmacology.drink(player, SterilePharmacy.ADRENALINE);
		h.assertTrue(player.getEffect(MobEffects.STRENGTH).getAmplifier() == 1 && player.getEffect(MobEffects.SPEED).getAmplifier() == 1, "Immediate Strength II and Speed II");
		advance(player, 35);
		h.assertTrue(Pharmacology.mayDrink(player, SterilePharmacy.ADRENALINE, false), "Adrenaline may be replaced for another full charge");
		advance(player, 10);
		h.assertTrue(player.hasEffect(SterilePharmacy.FATIGUE) && Pharmacology.state(player).doses().getFirst().recovery() == 180, "Expiry starts fatigue and 180 second recovery");
		var before = Pharmacology.state(player);
		var json = DrugState.CODEC.encodeStart(JsonOps.INSTANCE, before).getOrThrow();
		h.assertTrue(DrugState.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow().equals(before), "Longer tail roundtrips in saved codec");
		new ItemStack(Pharmacology.ACTIVATED_CHARCOAL).finishUsingItem(player.level(), player);
		h.assertTrue(!player.hasEffect(SterilePharmacy.FATIGUE) && Pharmacology.state(player).equals(before), "Charcoal removes only drawback, leaving accounting");
		Pharmacology.onDeath(player);
		h.assertTrue(Pharmacology.state(player).strainTenths() == before.strainTenths(), "Death preserves the remaining decayed balance");
		h.succeed();
	}

	@GameTest
	public void salbutamolDiscountsOnlyNativeSprintAndUsesBestSaltDiscount(GameTestHelper h) {
		var player = player(h);
		player.getAbilities().invulnerable = false;
		Pharmacology.drink(player, SterilePharmacy.SALBUTAMOL);
		Pharmacology.drink(player, Pharmacology.REHYDRATION_SALTS);
		advance(player, 5);
		player.setOnGround(true);
		player.setSprinting(true);
		float before = exhaustion(player.getFoodData());
		player.checkMovementStatistics(1, 0, 0);
		h.assertTrue(Math.abs(exhaustion(player.getFoodData()) - before - .05F) < .001F, "Actual native one-metre sprint costs half, not multiplicative salt savings");
		before = exhaustion(player.getFoodData());
		player.causeFoodExhaustion(.2F);
		h.assertTrue(Math.abs(exhaustion(player.getFoodData()) - before - .16F) < .001F, "Other activities receive only the salts discount even while sprinting");
		h.assertTrue(Math.abs(Pharmacology.waterAcceleration(player, .02F) - .023F) < .00001F, "Water input receives 15% acceleration");
		player.removeEffect(SterilePharmacy.SALBUTAMOL.marker());
		h.assertTrue(Pharmacology.waterAcceleration(player, .02F) == .02F, "Cleanse stops swimming bonus immediately");
		h.succeed();
	}

	@GameTest
	public void naloxoneEndsOnlyOpioidBenefitsAndDrowsinessWithoutFreeingStrain(GameTestHelper h) {
		var player = player(h);
		Pharmacology.drink(player, SterilePharmacy.MORPHINE);
		Pharmacology.drink(player, SterilePharmacy.LIDOCAINE);
		Pharmacology.drink(player, Pharmacology.ASPIRIN);
		advance(player, 5);
		h.assertTrue(player.hasEffect(SterilePharmacy.DROWSINESS) && player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) == .5,
				"Opioid grants best knockback bonus rather than stacking with lidocaine");
		var source = player.damageSources().fall();
		h.assertTrue(Math.abs(Pharmacology.physicalDamage(player, source, 10) - 8) < .001F, "Morphine supplies Resistance-equivalent reduction on falling");
		new ItemStack(Pharmacology.ACTIVATED_CHARCOAL).finishUsingItem(player.level(), player);
		h.assertTrue(player.hasEffect(SterilePharmacy.DROWSINESS), "Charcoal cannot bypass the opioid tradeoff");
		var result = new ItemStack(SterilePharmacy.item("naloxone")).finishUsingItem(player.level(), player);
		h.assertTrue(result.is(Items.GLASS_BOTTLE) && !player.hasEffect(SterilePharmacy.MORPHINE.marker())
				&& !player.hasEffect(SterilePharmacy.DROWSINESS) && Pharmacology.physicalDamage(player, source, 10) == 10
				&& player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) == .25, "Naloxone ends opioid shield and penalty immediately, leaving lidocaine");
		h.assertTrue(Pharmacology.strain(player) == 55 && Pharmacology.state(player).history().size() == 3
				&& player.hasEffect(Pharmacology.FORTITUDE), "Strain, tolerance and unrelated families remain");
		h.succeed();
	}

	private static ServerPlayer player(GameTestHelper h) {
		var player = TestPlayers.survivor(h);
		player.getAbilities().invulnerable = true;
		player.setNoGravity(true);
		return player;
	}
	private static void advance(ServerPlayer player, int seconds) { for (int i = 0; i < seconds; i++) Pharmacology.tickSecond(player); }
	private static float exhaustion(FoodData food) {
		var output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
		food.addAdditionalSaveData(output);
		return output.buildResult().getFloatOr("foodExhaustionLevel", -1);
	}
}
