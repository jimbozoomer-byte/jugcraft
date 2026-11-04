package io.github.jimbozoomer.jugcraft.test;

import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.pharmacology.DrugState;
import io.github.jimbozoomer.jugcraft.pharmacology.Pharmacology;
import io.github.jimbozoomer.jugcraft.weapons.FieldChemistry;
import io.github.jimbozoomer.jugcraft.chemistry.FluidRecipes;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueOutput;

/** Real recipes, consumption, native effect priority, player saves, death and anti-redose accounting. */
public class PharmacologyGameTests {
	@GameTest
	public void coffeeAndCocoaReachTheDrinkAndReturnTheBucket(GameTestHelper helper) {
		Item beans = BuiltInRegistries.ITEM.getValue(Jugcraft.id("coffee_beans"));
		Item cherries = BuiltInRegistries.ITEM.getValue(Jugcraft.id("coffee_cherries"));
		var roast = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.SMELTING,
				new SingleRecipeInput(new ItemStack(cherries)), helper.getLevel()).orElseThrow();
		helper.assertTrue(roast.value().assemble(new SingleRecipeInput(new ItemStack(cherries))).is(beans), "Existing coffee harvest roasts to beans");
		checkCraft(helper, CraftingInput.of(2, 1, List.of(new ItemStack(beans), new ItemStack(beans))),
				Pharmacology.CAFFEINE_CONCENTRATE, 2);
		checkCraft(helper, CraftingInput.of(2, 2, List.of(new ItemStack(Items.COCOA_BEANS), new ItemStack(Items.COCOA_BEANS),
				new ItemStack(Items.COCOA_BEANS), new ItemStack(Items.COCOA_BEANS))), Pharmacology.CAFFEINE_CONCENTRATE, 1);
		var input = CraftingInput.of(3, 2, List.of(new ItemStack(Pharmacology.CAFFEINE_CONCENTRATE), new ItemStack(Items.SUGAR),
				new ItemStack(Pharmacology.CAFFEINE_CONCENTRATE), new ItemStack(Items.GLASS_BOTTLE),
				new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.GLASS_BOTTLE)));
		checkCraft(helper, input, Pharmacology.CAFFEINE_DRINK, 2);
		var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow();
		helper.assertTrue(recipe.value().getRemainingItems(input).get(4).is(Items.BUCKET), "Water bucket returns one empty bucket");
		helper.succeed();
	}

	@GameTest
	public void drinkingHasAnOnsetAndUnavailableUseSpendsNothing(GameTestHelper helper) {
		ServerPlayer player = survivor(helper);
		double baseSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
		ItemStack stack = new ItemStack(Pharmacology.CAFFEINE_DRINK);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		ItemStack remainder = stack.finishUsingItem(player.level(), player);
		helper.assertTrue(remainder.is(Items.GLASS_BOTTLE), "A successful drink returns its bottle exactly once");
		helper.assertTrue(Pharmacology.strain(player) == 10 && !player.hasEffect(MobEffects.HASTE), "Onset charges strain before giving Haste");
		ItemStack denied = new ItemStack(Pharmacology.CAFFEINE_DRINK, 2);
		player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
		helper.assertTrue(denied.finishUsingItem(player.level(), player).getCount() == 2, "Unavailable spectator use spends nothing");
		player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
		advance(player, 10);
		helper.assertTrue(player.hasEffect(MobEffects.HASTE) && player.getEffect(MobEffects.HASTE).getAmplifier() == 0, "Onset gives Haste I");
		helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED) - baseSpeed * 1.10) < .0001, "Caffeine adds exactly 10% movement speed");
		helper.assertTrue(!player.hasEffect(Pharmacology.JITTERS), "First dose has no jitters or injury penalty");
		helper.assertTrue(Pharmacology.mayDrink(player, Pharmacology.CAFFEINE, false), "Early redosing is available for a full new strain charge");
		helper.succeed();
	}

	@GameTest
	public void nativePotionPriorityAndCleansingKeepRecovery(GameTestHelper helper) {
		ServerPlayer player = survivor(helper);
		double baseSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
		player.addEffect(new MobEffectInstance(MobEffects.HASTE, 12000, 1));
		player.addEffect(new MobEffectInstance(MobEffects.SPEED, 12000, 0));
		Pharmacology.drink(player, Pharmacology.CAFFEINE);
		advance(player, 10);
		helper.assertTrue(player.getEffect(MobEffects.HASTE).getAmplifier() == 1, "Caffeine does not downgrade stronger Haste");
		helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED) - baseSpeed * 1.20) < .0001, "Speed potion takes priority rather than multiplying caffeine");
		advance(player, 300);
		helper.assertTrue(player.hasEffect(MobEffects.HASTE) && player.getEffect(MobEffects.HASTE).getAmplifier() == 1, "Caffeine expiry leaves unrelated stronger Haste intact");
		helper.assertTrue(!player.hasEffect(Pharmacology.CAFFEINATED) && Pharmacology.strain(player) == 0, "Strain can clear before the long benefit ends");
		advance(player, 120);
		helper.assertTrue(Pharmacology.strain(player) == 0, "After-effect cleanup does not recharge a cleared pool");
		Pharmacology.drink(player, Pharmacology.CAFFEINE);
		player.removeAllEffects();
		Pharmacology.tickSecond(player);
		helper.assertTrue(!player.hasEffect(Pharmacology.CAFFEINATED) && Pharmacology.state(player).doses().getFirst().recovery() == 120,
				"Cleansing during onset cancels the pending benefit but keeps its recovery");
		helper.succeed();
	}

	@GameTest
	public void allowedRedosingHasBoundedDurationJittersAndTolerance(GameTestHelper helper) {
		ServerPlayer player = survivor(helper);
		Pharmacology.drink(player, Pharmacology.CAFFEINE);
		advance(player, 10 + 225);
		helper.assertTrue(Pharmacology.drink(player, Pharmacology.CAFFEINE), "Final-quarter redose is allowed");
		helper.assertTrue(Pharmacology.state(player).doses().getFirst().remaining() == 225, "Second dose is capped at 75% duration, never added to old duration");
		helper.assertTrue(player.hasEffect(Pharmacology.JITTERS) && Pharmacology.aimSpreadMultiplier(player) == 1.20F,
				"Early redosing causes exactly the declared firearm-spread drawback");
		helper.assertTrue(Pharmacology.strain(player) == 10, "The new full strain charge remains after the first charge already decayed");
		player.removeAllEffects();
		advance(player, 121);
		helper.assertTrue(Pharmacology.drink(player, Pharmacology.CAFFEINE), "A later dose is possible after recovery");
		helper.assertTrue(Pharmacology.state(player).doses().getFirst().total() == 150, "Third dose has 50% duration");
		player.removeAllEffects();
		advance(player, 1801);
		helper.assertTrue(Pharmacology.drink(player, Pharmacology.CAFFEINE), "Tolerance expires after an online break");
		helper.assertTrue(Pharmacology.state(player).doses().getFirst().total() == 300, "A rested first dose has full duration again");
		helper.succeed();
	}

	@GameTest
	public void savedDosesSurviveReconnectAndDeathWithoutResettingTolerance(GameTestHelper helper) {
		ServerPlayer player = survivor(helper);
		Pharmacology.drink(player, Pharmacology.CAFFEINE);
		Pharmacology.drink(player, Pharmacology.ASPIRIN);
		Pharmacology.drink(player, Pharmacology.PARACETAMOL);
		advance(player, 12);
		DrugState before = Pharmacology.state(player);
		DrugState decoded = DrugState.CODEC.parse(JsonOps.INSTANCE,
				DrugState.CODEC.encodeStart(JsonOps.INSTANCE, before).getOrThrow()).getOrThrow();
		helper.assertTrue(before.equals(decoded), "All dose and tolerance timers survive the persistent codec");
		var id = player.getUUID();
		TestPlayers.leave(player);
		ServerPlayer returned = TestPlayers.again(helper, id);
		helper.assertTrue(before.equals(Pharmacology.state(returned)), "Player-file save and reconnect preserve exact remaining online time");
		returned.setHealth(0);
		ServerPlayer respawned = returned.level().getServer().getPlayerList().respawn(returned, false, Entity.RemovalReason.KILLED);
		TestPlayers.track(helper, respawned);
		helper.assertTrue(!Pharmacology.state(respawned).doses().getFirst().active()
				&& Pharmacology.state(respawned).doses().getFirst().recovery() == 120,
				"Death ends benefits and retains recovery: " + Pharmacology.state(respawned));
		helper.assertTrue(Pharmacology.state(respawned).history().getFirst().doses() == 1, "Death does not clear tolerance");
		helper.assertTrue(Pharmacology.state(respawned).doses().size() == 3
				&& Pharmacology.state(respawned).doses().stream().noneMatch(DrugState.Dose::active)
				&& !respawned.hasEffect(Pharmacology.FORTITUDE) && !respawned.hasEffect(Pharmacology.RECOVERY),
				"Respawn normalizes every medicine family after copying the attachment");
		helper.succeed();
	}

	@GameTest
	public void pharmacyIngredientsRecipesAndContainersAreReachable(GameTestHelper helper) {
		checkCraft(helper, CraftingInput.of(3, 1, List.of(new ItemStack(Items.POTATO), new ItemStack(Items.POTATO),
				new ItemStack(Items.PAPER))), Pharmacology.PHARMACEUTICAL_STARCH, 2);
		checkCraft(helper, CraftingInput.of(2, 2, List.of(new ItemStack(Items.CHARCOAL), new ItemStack(Items.CHARCOAL),
				new ItemStack(Items.REDSTONE), new ItemStack(Items.PAPER))), Pharmacology.ACTIVATED_CHARCOAL, 2);
		Item salt = BuiltInRegistries.ITEM.getValue(Jugcraft.id("salt"));
		var input = CraftingInput.of(3, 2, List.of(new ItemStack(salt), new ItemStack(Items.SUGAR), new ItemStack(salt),
				new ItemStack(Items.PAPER), new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.PAPER)));
		checkCraft(helper, input, Pharmacology.REHYDRATION_DRINK, 2);
		var crafting = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow();
		helper.assertTrue(crafting.value().getRemainingItems(input).get(4).is(Items.BUCKET), "Salts crafting returns its bucket once");
		var reactor = FluidRecipes.entries(helper.getLevel().getServer(), MachineKind.CHEMICAL_REACTOR).stream()
				.filter(entry -> entry.id().equals(Jugcraft.id("chemical_reaction/paracetamol"))).findFirst().orElseThrow();
		ItemStack result = reactor.recipe().results().getFirst().create();
		helper.assertTrue(result.is(Pharmacology.PARACETAMOL_TABLET) && result.getCount() == 4,
				"The existing reactor loads the four-tablet formulation");
		helper.succeed();
	}

	@GameTest
	public void familiesCombineAndStimulantsReplaceWithFullStrainCost(GameTestHelper helper) {
		ServerPlayer player = survivor(helper);
		new ItemStack(FieldChemistry.ASPIRIN).finishUsingItem(player.level(), player);
		new ItemStack(Pharmacology.PARACETAMOL_TABLET).finishUsingItem(player.level(), player);
		ItemStack packet = new ItemStack(Pharmacology.REHYDRATION_DRINK).finishUsingItem(player.level(), player);
		helper.assertTrue(packet.isEmpty() && Pharmacology.strain(player) == 25,
				"Three different families combine, accounting for every dose without creating a bottle from a paper sachet");
		Pharmacology.drink(player, Pharmacology.CAFFEINE);
		ItemStack rejected = new ItemStack(FieldChemistry.STIMULANT, 2);
		helper.assertTrue(rejected.finishUsingItem(player.level(), player).getCount() == 1 && Pharmacology.strain(player) == 60,
				"Replacing pending caffeine consumes the new dose and adds its full cost");
		advance(player, 10);
		helper.assertTrue(player.hasEffect(Pharmacology.FORTITUDE) && player.hasEffect(Pharmacology.RECOVERY)
				&& player.hasEffect(Pharmacology.ENDURANCE), "All independent benefits start after their onset");
		ServerPlayer other = survivor(helper);
		ItemStack returned = new ItemStack(FieldChemistry.STIMULANT).finishUsingItem(other.level(), other);
		helper.assertTrue(returned.isEmpty() && Pharmacology.strain(other) == 25
				&& other.getEffect(MobEffects.HASTE).getAmplifier() == 1 && other.getEffect(MobEffects.SPEED).getAmplifier() == 1,
				"A different player independently receives immediate Haste II/Speed II from the same stable item ID");
		helper.assertTrue(Pharmacology.mayDrink(other, Pharmacology.CAFFEINE, false), "Caffeine can replace stimulant with shared tolerance and a new charge");
		helper.succeed();
	}

	@GameTest
	public void aspirinFortitudeChangesActualPhysicalDamageAndYieldsToResistance(GameTestHelper helper) {
		ServerPlayer player = survivor(helper);
		player.getAbilities().invulnerable = false;
		player.hasChangedDimension();
		player.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
		helper.assertTrue(player.connection.hasClientLoaded(), "Damage fixture completed the native loading handshake");
		var pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(2, 1, 2));
		var source = player.damageSources().mobAttack(pig);
		float baseline = hurt(player, source);
		Pharmacology.drink(player, Pharmacology.ASPIRIN);
		advance(player, 5);
		float fortified = hurt(player, source);
		helper.assertTrue(baseline > 0 && Math.abs(fortified - baseline * .9F) < .001F,
				"Fortitude reduces real physical damage by exactly 10%: " + baseline + " -> " + fortified);
		player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1200));
		float resisted = hurt(player, source);
		helper.assertTrue(Math.abs(resisted - baseline * .8F) < .001F, "Resistance wins without multiplying Fortitude");
		player.removeEffect(MobEffects.RESISTANCE);
		helper.assertTrue(Math.abs(hurt(player, player.damageSources().fall()) - 4) < .001F, "Fortitude does not reduce falling damage");
		player.removeAllEffects();
		helper.assertTrue(Math.abs(hurt(player, source) - baseline) < .001F, "Cleansing immediately removes the passive defense");
		helper.succeed();
	}

	@GameTest
	public void recoveryUsesVanillaFoodHealingAndPaysItsFoodCost(GameTestHelper helper) {
		ServerPlayer player = survivor(helper);
		Pharmacology.drink(player, Pharmacology.PARACETAMOL);
		Pharmacology.drink(player, Pharmacology.REHYDRATION_SALTS);
		advance(player, 5);
		player.setHealth(10);
		FoodData food = player.getFoodData();
		food.setFoodLevel(18);
		food.setSaturation(0);
		for (int tick = 0; tick < 80; tick++) {
			food.tick(player);
		}
		helper.assertTrue(Math.abs(player.getHealth() - 11.25F) < .001F, "Natural healing gains exactly 25% per native interval");
		helper.assertTrue(Math.abs(exhaustion(food) - 7.5F) < .001F,
				"The extra healing pays its proportional food cost, including while Endurance is active");
		player.setHealth(10);
		food.setFoodLevel(17);
		for (int tick = 0; tick < 100; tick++) {
			food.tick(player);
		}
		helper.assertTrue(player.getHealth() == 10, "Recovery cannot heal below vanilla's food requirement");
		player.heal(2);
		helper.assertTrue(player.getHealth() == 12, "Other healing sources are not multiplied");
		var rules = helper.getLevel().getGameRules();
		boolean previous = rules.get(GameRules.NATURAL_HEALTH_REGENERATION);
		try {
			rules.set(GameRules.NATURAL_HEALTH_REGENERATION, false, helper.getLevel().getServer());
			food.setFoodLevel(20);
			food.setSaturation(6);
			for (int tick = 0; tick < 100; tick++) {
				food.tick(player);
			}
			helper.assertTrue(player.getHealth() == 12, "Recovery respects the natural-regeneration gamerule");
		} finally {
			rules.set(GameRules.NATURAL_HEALTH_REGENERATION, previous, helper.getLevel().getServer());
		}
		helper.succeed();
	}

	@GameTest
	public void enduranceChangesActualActivityExhaustionAndStopsOnCleanse(GameTestHelper helper) {
		ServerPlayer player = survivor(helper);
		player.getAbilities().invulnerable = false;
		Pharmacology.drink(player, Pharmacology.REHYDRATION_SALTS);
		advance(player, 5);
		player.causeFoodExhaustion(10);
		helper.assertTrue(Math.abs(exhaustion(player.getFoodData()) - 8) < .001F, "Activity exhaustion is reduced by exactly 20%");
		player.removeAllEffects();
		player.causeFoodExhaustion(10);
		helper.assertTrue(Math.abs(exhaustion(player.getFoodData()) - 18) < .001F, "Milk-like cleansing immediately stops the discount");
		Pharmacology.tickSecond(player);
		helper.assertTrue(Pharmacology.strain(player) == 5 && Pharmacology.mayDrink(player, Pharmacology.REHYDRATION_SALTS, false),
				"Cleansing keeps the salt family's strain and recovery");
		helper.succeed();
	}

	@GameTest
	public void charcoalClearsOnlyDrugDrawbacksAndCannotResetAccounting(GameTestHelper helper) {
		ServerPlayer player = survivor(helper);
		Pharmacology.drink(player, Pharmacology.STIMULANT);
		advance(player, 45);
		Pharmacology.drink(player, Pharmacology.STIMULANT);
		player.addEffect(new MobEffectInstance(MobEffects.POISON, 1200));
		player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200));
		DrugState before = Pharmacology.state(player);
		helper.assertTrue(player.hasEffect(Pharmacology.JITTERS) && player.hasEffect(Pharmacology.STIMULANT_APPETITE),
				"A genuine stimulant redose produces both advertised drawbacks");
		ItemStack charcoal = new ItemStack(Pharmacology.ACTIVATED_CHARCOAL, 2);
		ItemStack remaining = charcoal.finishUsingItem(player.level(), player);
		helper.assertTrue(remaining.is(Pharmacology.ACTIVATED_CHARCOAL) && remaining.getCount() == 1
				&& remaining.has(DataComponents.USE_COOLDOWN), "Charcoal consumes one item and has a native cooldown");
		helper.assertTrue(!player.hasEffect(Pharmacology.JITTERS) && !player.hasEffect(Pharmacology.STIMULANT_APPETITE)
				&& Pharmacology.aimSpreadMultiplier(player) == 1, "Charcoal clears only the named drug drawbacks");
		helper.assertTrue(player.hasEffect(Pharmacology.STIMULATED) && player.hasEffect(MobEffects.HASTE)
				&& player.hasEffect(MobEffects.POISON) && player.hasEffect(MobEffects.HUNGER),
				"Positive effects and unrelated negative effects survive");
		helper.assertTrue(Pharmacology.state(player).equals(before) && Pharmacology.strain(player) == 46,
				"Charcoal never changes active doses, recovery or tolerance");
		helper.succeed();
	}

	@GameTest
	public void nonStimulantRedosingHasToleranceWithoutStimulantJitters(GameTestHelper helper) {
		ServerPlayer player = survivor(helper);
		Pharmacology.drink(player, Pharmacology.ASPIRIN);
		advance(player, 5 + 540);
		helper.assertTrue(Pharmacology.drink(player, Pharmacology.ASPIRIN)
				&& Pharmacology.state(player).doses().getFirst().total() == 540,
				"Final-quarter aspirin redosing refreshes at 75% duration");
		helper.assertTrue(!player.hasEffect(Pharmacology.JITTERS) && !player.hasEffect(Pharmacology.STIMULANT_APPETITE),
				"Other drug families do not inherit stimulant side effects");
		helper.succeed();
	}

	private static float exhaustion(FoodData food) {
		var output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
		food.addAdditionalSaveData(output);
		return output.buildResult().getFloatOr("foodExhaustionLevel", -1);
	}

	private static float hurt(ServerPlayer player, net.minecraft.world.damagesource.DamageSource source) {
		player.setHealth(20);
		player.setInvulnerableTime(0);
		player.damageCooldownTime = 0;
		player.hurtServer(player.level(), source, 4);
		return 20 - player.getHealth();
	}

	private static ServerPlayer survivor(GameTestHelper helper) {
		ServerPlayer player = TestPlayers.survivor(helper);
		player.getAbilities().invulnerable = true;
		player.setNoGravity(true);
		return player;
	}

	private static void advance(ServerPlayer player, int seconds) {
		for (int second = 0; second < seconds; second++) {
			Pharmacology.tickSecond(player);
		}
	}

	private static void checkCraft(GameTestHelper helper, CraftingInput input, Item expected, int count) {
		var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow();
		ItemStack output = recipe.value().assemble(input);
		helper.assertTrue(output.is(expected) && output.getCount() == count, "Loaded crafting recipe gives the specified product and yield");
	}
}
