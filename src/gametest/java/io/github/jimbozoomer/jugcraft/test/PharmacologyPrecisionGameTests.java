package io.github.jimbozoomer.jugcraft.test;

import static io.github.jimbozoomer.jugcraft.test.PharmacologyFactoryGameTests.*;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.chemistry.PetroFluids;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.form.*;
import io.github.jimbozoomer.jugcraft.nouveau.NouveauScrying;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

public class PharmacologyPrecisionGameTests {
	private static final String ARENA = "jugcraft-test:arms_arena";
	private static DrugProfile profile(String id) { return ((DrugDrinkItem) PrecisionPharmacy.item(id)).profile(); }
	private static ServerPlayer player(GameTestHelper h) { var p = TestPlayers.survivor(h); p.getAbilities().invulnerable = true; p.setNoGravity(true); p.setOnGround(true); return p; }
	private static void advance(ServerPlayer p, int seconds) { for (int i = 0; i < seconds; i++) Pharmacology.tickSecond(p); }
	private static float exhaustion(FoodData food) { var out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING); food.addAdditionalSaveData(out); return out.buildResult().getFloatOr("foodExhaustionLevel", -1); }

	@GameTest(structure = ARENA)
	public void lowTechMashAndStillMakeFourBottlesAndReturnContainersExactlyOnce(GameTestHelper h) {
		for (var crop : List.of(Items.WHEAT, io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture.item("corn"))) {
			var input = CraftingInput.of(3, 3, List.of(new ItemStack(crop), new ItemStack(crop), new ItemStack(crop), new ItemStack(crop),
					new ItemStack(Items.SUGAR), new ItemStack(Items.SUGAR), new ItemStack(Items.WATER_BUCKET), new ItemStack(PrecisionPharmacy.item("empty_mash_jug")), ItemStack.EMPTY));
			var recipe = h.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel()).orElseThrow().value();
			h.assertTrue(recipe.assemble(input).is(PrecisionPharmacy.item("mash_jug")) && recipe.getRemainingItems(input).get(6).is(Items.BUCKET), "Both crops craft mash and return exactly the water bucket");
		}
		var barrel = place(h, IndustrialForms.MASH_CONDITIONING_BARREL, Direction.NORTH);
		var fermentation = recipe(barrel, h, "beverage_ferment_mash"); feed(barrel, fermentation); ticks(h, barrel, fermentation.time());
		h.assertTrue(barrel.container().getItem(barrel.form().firstOutputSlot()).is(PrecisionPharmacy.item("fermented_mash_jug"))
				&& barrel.energy().getCapacity() == 0, "Loaded passive barrel ferments without electricity"); clear(h, barrel);
		var still = place(h, IndustrialForms.MOONSHINE_STILL, Direction.NORTH);
		var distill = recipe(still, h, "beverage_moonshine"); feed(still, distill);
		int fuel = FormMachineBlockEntity.furnaceTicks(h.getLevel(), new ItemStack(Items.COAL));
		ticks(h, still, distill.time());
		h.assertTrue(still.energy().getCapacity() == 0 && still.furnaceTicksRemaining() == fuel - 200, "Still spends exactly 200 native furnace ticks and has no electrical store");
		var result = still.container().getItem(still.form().firstOutputSlot());
		h.assertTrue(result.is(PrecisionPharmacy.item("moonshine")) && result.getCount() == 4
				&& still.container().getItem(still.form().firstOutputSlot() + 1).is(PrecisionPharmacy.item("empty_mash_jug")), "Four bottles plus one returned empty jug");
		ticks(h, still, 20); h.assertTrue(result.getCount() == 4 && still.furnaceTicksRemaining() == fuel - 200, "Idle does not burn fuel or duplicate output");
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void fueledSavedWorkAndCancellationNeverRefundSpentFuel(GameTestHelper h) {
		var machine = place(h, IndustrialForms.MOONSHINE_STILL, Direction.EAST);
		var recipe = recipe(machine, h, "beverage_moonshine"); feed(machine, recipe);
		machine.container().setItem(machine.form().fuelSlot(), ItemStack.EMPTY);
		ticks(h, machine, 5); h.assertTrue(machine.lane(0) != null && machine.lane(0).progress() == 0, "No fuel holds escrowed raw ingredients without progress");
		machine.container().setItem(machine.form().fuelSlot(), new ItemStack(Items.LAVA_BUCKET));
		ticks(h, machine, 20);
		h.assertTrue(machine.container().getItem(machine.form().fuelSlot()).is(Items.BUCKET), "Native container fuel returns its bucket once in the fuel slot");
		int fuel = machine.furnaceTicksRemaining();
		var saved = machine.saveWithoutMetadata(h.getLevel().registryAccess());
		machine.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved));
		ticks(h, machine, 10); h.assertTrue(machine.lane(0).progress() == 30 && machine.furnaceTicksRemaining() == fuel - 10, "Saved heat and work resume without re-consuming lava");
		machine.togglePause(); ticks(h, machine, 40);
		h.assertTrue(machine.lane(0).progress() == 30 && machine.furnaceTicksRemaining() == fuel - 10, "Paused work spends no heat");
		machine.togglePause();
		machine.cancel();
		h.assertTrue(machine.lane(0) == null && machine.container().getItem(0).is(PrecisionPharmacy.item("fermented_mash_jug"))
				&& machine.container().getItem(1).getCount() == 4 && machine.container().getItem(machine.form().firstOutputSlot()).isEmpty(), "Cancel refunds only original mash and empty bottles");
		h.assertTrue(machine.container().getItem(machine.form().fuelSlot()).is(Items.BUCKET) && machine.furnaceTicksRemaining() == fuel - 10, "No refunded lava or restored spent ticks");
		ticks(h, machine, 20);
		h.assertTrue(machine.lane(0) != null && machine.lane(0).progress() == 20, "Refunded inputs restart exactly one uncompleted job");
		var controller = machine.getBlockPos();
		var far = machine.form().footprint().partPos(controller, machine.getBlockState().getValue(MachineBlock.FACING), machine.form().footprint().size() - 1);
		h.assertTrue(h.getLevel().destroyBlock(far, true), "The actual far part of the east-facing still is broken");
		var drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(controller).inflate(5));
		h.assertTrue(dropped(drops, IndustrialForms.MOONSHINE_STILL.asItem()) == 1
				&& dropped(drops, PrecisionPharmacy.item("fermented_mash_jug")) == 1 && dropped(drops, Items.GLASS_BOTTLE) == 4
				&& dropped(drops, Items.BUCKET) == 1 && dropped(drops, PrecisionPharmacy.item("moonshine")) == 0,
				"Breaking running fueled work returns one machine and only its original ingredients/container; actual drops: "
						+ drops.stream().map(e -> e.getItem().toString()).toList());
		h.succeed();
	}
	private static int dropped(List<ItemEntity> drops, net.minecraft.world.item.Item item) { return drops.stream().filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum(); }

	@GameTest(structure = ARENA)
	public void beverageIndustryUsesSeparateWashAndStockAndKeepsStillage(GameTestHelper h) {
		for (var block : List.of(IndustrialForms.INDUSTRIAL_FERMENTER, IndustrialForms.BULK_FERMENTER)) {
			var machine = place(h, block, Direction.NORTH); var job = recipe(machine, h, "beverage_wash"); feed(machine, job); ticks(h, machine, job.time());
			h.assertTrue(machine.tanks().output(0).has(PetroFluids.BEVERAGE_WASH.source(), 1000) && machine.tanks().output(1).millibuckets() == 0, "Both fermenters produce beverage wash without exhaust"); clear(h, machine);
		}
		var distiller = place(h, IndustrialForms.CHEMICAL_DISTILLATION_STATION, Direction.NORTH);
		var job = recipe(distiller, h, "beverage_refine_stock"); feed(distiller, job); ticks(h, distiller, job.time());
		h.assertTrue(distiller.tanks().output(0).has(PetroFluids.REFINED_BEVERAGE_STOCK.source(), 500)
				&& distiller.tanks().output(1).has(PetroFluids.STILLAGE.source(), 500), "Stock and paid stillage conserve the wash volume");
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void everyPrecisionReactorJobRequiresAndRetainsLockedModule(GameTestHelper h) {
		for (String drug : List.of("methylphenidate", "amphetamine", "modafinil", "ketamine", "fentanyl")) {
			var machine = place(h, IndustrialForms.BULK_CHEMICAL_REACTOR, Direction.NORTH);
			var job = recipe(machine, h, "precision_prepare_" + drug); feed(machine, job);
			int slot = machine.form().firstUpgradeSlot(); machine.container().setItem(slot, ItemStack.EMPTY);
			ticks(h, machine, 1); h.assertTrue(machine.lane(0) == null && machine.container().getItem(0).getCount() == 1, "Module is required before reserving blend");
			machine.container().setItem(slot, new ItemStack(PrecisionPharmacy.item("precision_process_module")));
			ticks(h, machine, 20);
			var p = player(h); var menu = (FormMachineMenu) machine.createMenu(0, p.getInventory(), p);
			h.assertTrue(machine.lane(0) != null && !menu.getSlot(slot).mayPickup(p), "Existing upgrade slot locks its precision process tool");
			var saved = machine.saveWithoutMetadata(h.getLevel().registryAccess());
			machine.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved));
			ticks(h, machine, job.time());
			var result = machine.container().getItem(machine.form().firstOutputSlot());
			h.assertTrue(result.is(PrecisionPharmacy.item(drug + "_stock")) && result.getCount() == 4
					&& machine.container().getItem(slot).getCount() == 1, "Native precision job completes once and keeps the module"); clear(h, machine);
		}
		h.succeed();
	}

	@GameTest
	public void allFourAlcoholGradesHaveTheirExactBonusesAndSharedFamily(GameTestHelper h) {
		String[] grades = {"moonshine", "refined_spirits", "reserve_whiskey", "high_proof_spirits"};
		for (int i = 0; i < grades.length; i++) {
			var p = player(h); var profile = profile(grades[i]); double damage = p.getAttributeValue(Attributes.ATTACK_DAMAGE), speed = p.getAttributeValue(Attributes.MOVEMENT_SPEED);
			var bottle = new ItemStack(PrecisionPharmacy.item(grades[i])).finishUsingItem(p.level(), p);
			h.assertTrue(bottle.is(Items.GLASS_BOTTLE) && profile.family().equals("intoxicant"), "Alcohol is self-administered with one returned bottle and a shared family");
			advance(p, profile.onsetSeconds());
			h.assertTrue(Math.abs(p.getAttributeValue(Attributes.ATTACK_DAMAGE) - damage * (1.10 + i * .05)) < .0001, "Exact native melee bonus for grade " + i);
			h.assertTrue(Math.abs(p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) - (.20 + i * .10)) < .0001
					&& Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED) - speed * (.95 - i * .025)) < .0001, "Exact knockback and intoxication movement penalty");
			h.assertTrue(Math.abs(Pharmacology.aimSpreadMultiplier(p) - (1.15F + i * .05F)) < .0001F, "Supported shot spread drawback");
			p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 1200));
			h.assertTrue(Math.abs(p.getAttributeValue(Attributes.ATTACK_DAMAGE) - damage - 3) < .0001, "Stronger native Strength wins without an extra alcohol multiplier");
			h.assertTrue(Pharmacology.mayDrink(p, profile(grades[(i + 1) % 4]), false), "Changing grade replaces the family for a new charge");
		}
		h.succeed();
	}

	@GameTest
	public void alcoholOpioidAndDissociativeOverlapsChargeInBothDirections(GameTestHelper h) {
		var profiles = List.of(profile("moonshine"), PrecisionPharmacy.FENTANYL, PrecisionPharmacy.KETAMINE);
		for (var first : profiles) for (var second : profiles) {
			if (first == second) continue;
			var p = player(h); h.assertTrue(Pharmacology.drink(p, first), "First compatible dose starts");
			h.assertTrue(Pharmacology.mayDrink(p, second, false) && Pharmacology.doseCost(p,second)==second.strain()+25, "Overlapping another restricted family is allowed with one 25-point surcharge");
			var stack = new ItemStack(PrecisionPharmacy.item(second.id()), 2); var result = stack.finishUsingItem(p.level(), p);
			h.assertTrue(result.getCount()==1 && Pharmacology.state(p).strainTenths()==(first.strain()+second.strain()+25)*10, "Real consumption spends one item and charges both doses plus the surcharge");
			EchoView.cancel(p); Pharmacology.cancelFamily(p,first.family()); Pharmacology.cancelFamily(p,second.family());
			h.assertTrue(Pharmacology.mayDrink(p, second, false) && Pharmacology.doseCost(p,second)==second.strain(), "Ended doses do not create an overlap charge or recovery lock");
		}
		h.succeed();
	}

	@GameTest
	public void laterAlcoholReplacementRetainsToleranceWhenChangingGrade(GameTestHelper h) {
		var p = player(h); var high = profile("high_proof_spirits"); Pharmacology.drink(p, high); advance(p, high.onsetSeconds() + 900);
		h.assertTrue(Pharmacology.mayDrink(p, high, false), "High-proof may be replaced for another full charge");
		Pharmacology.cancelFamily(p, "intoxicant"); h.assertTrue(Pharmacology.state(p).doses().getFirst().recovery() == 180, "Higher grade preserves 180-second recovery");
		advance(p, 180); h.assertTrue(Pharmacology.drink(p, profile("moonshine")), "Earlier grade is reachable after recovery");
		h.assertTrue(Pharmacology.state(p).doses().getFirst().total() == 360, "Changing grade retains the shared 75% duration penalty");
		h.succeed();
	}

	@GameTest
	public void precisionStimulantsStartAfterOnsetAndPayTheirActivityCost(GameTestHelper h) {
		for (var profile : List.of(PrecisionPharmacy.METHYLPHENIDATE, PrecisionPharmacy.AMPHETAMINE, PrecisionPharmacy.MODAFINIL)) {
			var p = player(h); p.getAbilities().invulnerable = false; Pharmacology.drink(p, profile);
			h.assertTrue(!p.hasEffect(MobEffects.HASTE), "No benefit before onset"); advance(p, 20);
			h.assertTrue(p.getEffect(MobEffects.HASTE).getAmplifier() == profile.benefits().haste(), "Correct native Haste strength");
			float before = exhaustion(p.getFoodData()); p.causeFoodExhaustion(1);
			h.assertTrue(Math.abs(exhaustion(p.getFoodData()) - before - profile.traits().activityCost()*profile.benefits().exhaustion()) < .001F, "Actual activity exhaustion pays the stated cost and work discount");
			h.assertTrue(Pharmacology.mayDrink(p, Pharmacology.CAFFEINE, false), "Advanced stimulants share replacement tolerance with caffeine");
			if (profile != PrecisionPharmacy.AMPHETAMINE) h.assertTrue(PrecisionPharmacy.aimSwayMultiplier(p) == profile.traits().aimSway(), "Synced focus strength matches its hand-sway benefit");
			else { advance(p, 900); h.assertTrue(p.hasEffect(SterilePharmacy.FATIGUE) && p.getEffect(SterilePharmacy.FATIGUE).getDuration() == 2400, "Amphetamine expiry gives two minutes of drug-only fatigue"); }
		}
		h.succeed();
	}

	@GameTest
	public void fentanylUsesStrongestRealDamageProtectionAndExistingNaloxone(GameTestHelper h) {
		var p = player(h); p.getAbilities().invulnerable = false; p.hasChangedDimension();
		p.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
		Pharmacology.drink(p, PrecisionPharmacy.FENTANYL); advance(p, 5);
		h.assertTrue(Math.abs(hurt(p) - 2.4F) < .001F, "Fentanyl reduces actual fall damage by 40%");
		for (int amp = 0; amp <= 2; amp++) {
			p.removeEffect(MobEffects.RESISTANCE); p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 12000, amp));
			float expected = amp < 2 ? 2.4F : 1.6F;
			h.assertTrue(Math.abs(hurt(p) - expected) < .001F, "Weaker/equal/stronger native Resistance chooses best without multiplication");
		}
		h.assertTrue(Math.abs(p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) - .75) < .001
				&& Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED) - .08) < .001, "Exact Fentanyl knockback bonus and 20% Drowsiness");
		new ItemStack(SterilePharmacy.item("naloxone")).finishUsingItem(p.level(), p);
		h.assertTrue(!p.hasEffect(PrecisionPharmacy.FENTANYL.marker()) && !p.hasEffect(SterilePharmacy.DROWSINESS)
				&& p.hasEffect(MobEffects.RESISTANCE) && Pharmacology.strain(p) == 55
				&& Pharmacology.state(p).doses().getFirst().recovery() == 180, "Existing Naloxone reverses only opioid state and keeps accounting/other potion");
		h.assertTrue(Pharmacology.mayDrink(p, SterilePharmacy.MORPHINE, false), "Recovery no longer prevents replacing an opioid");
		advance(p, 180); Pharmacology.drink(p, SterilePharmacy.MORPHINE);
		h.assertTrue(Pharmacology.state(p).doses().getFirst().total() == SterilePharmacy.MORPHINE.durationSeconds() * 75 / 100, "Morphine and Fentanyl retain one opioid tolerance history");
		advance(p, SterilePharmacy.MORPHINE.onsetSeconds());
		p.removeEffect(SterilePharmacy.DROWSINESS); p.addEffect(new MobEffectInstance(SterilePharmacy.DROWSINESS, 1200, 0));
		Pharmacology.tickSecond(p);
		h.assertTrue(p.getEffect(SterilePharmacy.DROWSINESS).getAmplifier() == 2
				&& Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED) - .085) < .001, "Saved Batch 3 Drowsiness keeps Morphine's original 15% penalty");
		h.succeed();
	}
	private static float hurt(ServerPlayer p) { p.setHealth(20); p.setInvulnerableTime(0); p.damageCooldownTime = 0; p.hurtServer(p.level(), p.damageSources().fall(), 4); return 20 - p.getHealth(); }

	@GameTest
	public void expandedFamilyHistoryKeepsOldAndNewSavedData(GameTestHelper h) {
		var history = java.util.stream.IntStream.range(0, 10).mapToObj(i -> new DrugState.History("family_" + i, 2, 1000)).toList();
		var state = new DrugState(List.of(new DrugState.Dose("fentanyl", 480, 0, 0, 180)), history);
		var json = DrugState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow();
		h.assertTrue(DrugState.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow().equals(state), "Ten family histories and longer recovery roundtrip under the same attachment codec"); h.succeed();
	}

	@GameTest
	public void damageImmediatelyEndsEchoViewWithoutCreatingAnInjuryCondition(GameTestHelper h) {
		var p = player(h); p.getAbilities().invulnerable = false; p.hasChangedDimension();
		p.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
		Pharmacology.drink(p, PrecisionPharmacy.KETAMINE);
		h.assertTrue(EchoView.active(p), "Echo View started");
		var gun = new ItemStack(io.github.jimbozoomer.jugcraft.guns.JugcraftGuns.GUNS.get("garrison_rifle"));
		io.github.jimbozoomer.jugcraft.guns.GunItem.setLoaded(gun, 3);
		p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, gun);
		p.getInventory().setItem(1, new ItemStack(io.github.jimbozoomer.jugcraft.guns.JugcraftGuns.ROUNDS.get("rifle_round"), 32));
		h.assertTrue(!io.github.jimbozoomer.jugcraft.guns.GunShots.fire(p)
				&& !io.github.jimbozoomer.jugcraft.guns.GunShots.reload(p, net.minecraft.world.InteractionHand.MAIN_HAND)
				&& io.github.jimbozoomer.jugcraft.guns.GunItem.loaded(gun) == 3, "Custom firearm trigger/reload actions are read-only during Echo View, without spending ammunition");
		p.getInventory().setItem(0, new ItemStack(Items.STONE, 4));
		p.connection.handlePlayerAction(new net.minecraft.network.protocol.game.ServerboundPlayerActionPacket(
				net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.DROP_ITEM, BlockPos.ZERO, Direction.DOWN));
		h.assertTrue(p.getInventory().getItem(0).getCount() == 4, "A real native drop packet cannot move inventory during Echo View");
		h.assertTrue(Math.abs(hurt(p) - 4) < .001 && !EchoView.active(p) && Pharmacology.strain(p) == 40
				&& Pharmacology.state(p).doses().getFirst().recovery() == 120, "Body stays vulnerable; damage ends only the view and preserves recovery");
		h.succeed();
	}

	@GameTest(structure = ARENA, maxTicks = 260)
	public void echoViewIsBoundedRateLimitedAndCannotMoveTheRealBody(GameTestHelper h) {
		for (int x = 3; x <= 7; x++) for (int z = 4; z <= 7; z++) for (int y = 2; y <= 30; y++) h.setBlock(new BlockPos(x, y, z), Blocks.AIR);
		var p = player(h); p.setPos(h.absoluteVec(new Vec3(5, 2, 5))); Vec3 body = p.position();
		p.setOnGround(false);
		var rejected = new ItemStack(PrecisionPharmacy.item("ketamine"), 2);
		rejected.finishUsingItem(p.level(), p);
		h.assertTrue(rejected.getCount() == 2 && Pharmacology.strain(p) == 0 && !EchoView.active(p), "Airborne activation is rejected without spending an item");
		p.setOnGround(true);
		Pharmacology.drink(p, PrecisionPharmacy.KETAMINE);
		h.assertTrue(EchoView.active(p) && NouveauScrying.active(p), "Echo uses the shared read-only remote-view interaction guards");
		Vec3 first = EchoView.position(p);
		EchoView.control(p, new EchoView.Input((byte) 1, (byte) 0, (byte) 0, Float.NaN, 0, false));
		h.assertTrue(EchoView.position(p).equals(first), "Non-finite input rejected");
		EchoView.control(p, new EchoView.Input((byte) 0, (byte) 1, (byte) 0, 0, 0, false));
		h.assertTrue(Math.abs(EchoView.position(p).x - first.x + .30) < .001, "Right strafe faces west when looking south, matching native movement");
		for (int i = 1; i <= 100; i++) h.runAfterDelay(i * 2, () -> {
			EchoView.control(p, new EchoView.Input((byte) 0, (byte) 0, (byte) 1, 0, 0, false));
			Vec3 once = EchoView.position(p); EchoView.control(p, new EchoView.Input((byte) 0, (byte) 0, (byte) 1, 0, 0, false));
			h.assertTrue(once.equals(EchoView.position(p)), "Second packet in same tick cannot add movement");
		});
		h.runAfterDelay(205, () -> {
			h.assertTrue(EchoView.position(p).distanceToSqr(first) > 23.5*23.5 && EchoView.position(p).distanceToSqr(first) <= 576.001 && p.position().distanceToSqr(body) < .001, "View moves beyond the old range to its 24-block bound while body remains at origin");
			int strainBeforeReturn=Pharmacology.state(p).strainTenths();
			EchoView.cancel(p); h.assertTrue(!EchoView.active(p) && !p.hasEffect(PrecisionPharmacy.KETAMINE.marker()) && Pharmacology.state(p).strainTenths()==strainBeforeReturn, "Return ends the benefit without erasing the remaining decayed strain"); h.succeed();
		});
	}

	@GameTest(structure = ARENA, maxTicks = 30)
	public void echoViewStopsAtOrdinarySolidWallsAndCleansingEndsItsSession(GameTestHelper h) {
		var p = player(h); p.setPos(h.absoluteVec(new Vec3(5.5, 2, 5.5)));
		for (int y = 2; y <= 5; y++) h.setBlock(new BlockPos(5, y, 6), Blocks.STONE);
		Pharmacology.drink(p, PrecisionPharmacy.KETAMINE); Vec3 initial = EchoView.position(p);
		for (int i = 1; i <= 6; i++) h.runAfterDelay(i * 2, () -> EchoView.control(p, new EchoView.Input((byte) 1, (byte) 0, (byte) 0, 0, 0, false)));
		h.runAfterDelay(15, () -> {
			h.assertTrue(EchoView.position(p).z - initial.z < .5, "Solid wall cannot be passed or viewed from inside");
			p.removeAllEffects();
		});
		h.runAfterDelay(18, () -> { h.assertTrue(!EchoView.active(p) && Pharmacology.strain(p) == 40, "Milk-style cleanse closes view and keeps reserved recovery"); h.succeed(); });
	}
}
