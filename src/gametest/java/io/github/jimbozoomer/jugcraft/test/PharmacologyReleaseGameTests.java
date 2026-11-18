package io.github.jimbozoomer.jugcraft.test;

import static io.github.jimbozoomer.jugcraft.test.PharmacologyFactoryGameTests.*;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.form.*;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
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
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;

public class PharmacologyReleaseGameTests {
	private static final String ARENA = "jugcraft-test:arms_arena";
	private static ServerPlayer player(GameTestHelper h) { var p = TestPlayers.survivor(h); p.setNoGravity(true); p.setOnGround(true); return p; }
	private static void advance(ServerPlayer p, int seconds) { for (int i = 0; i < seconds; i++) Pharmacology.tickSecond(p); }
	private static float exhaustion(FoodData food) { var out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING); food.addAdditionalSaveData(out); return out.buildResult().getFloatOr("foodExhaustionLevel", -1); }
	private static void powered(GameTestHelper h, FormMachineBlockEntity machine, int ticks) {
		for (int i = 0; i < ticks; i++) { ((SimpleEnergyStorage) machine.energy()).setAmount(machine.energy().getCapacity()); ticks(h, machine, 1); }
	}

	@GameTest
	public void farmAndPolymerInputsCraftTheFirstReleaseMaterialsWithoutFinishedDrugs(GameTestHelper h) {
		var starch = Pharmacology.PHARMACEUTICAL_STARCH;
		var pellets = io.github.jimbozoomer.jugcraft.chemistry.PetroItems.PLASTIC_PELLETS;
		var input = CraftingInput.of(3, 3, List.of(new ItemStack(starch), new ItemStack(starch), new ItemStack(starch), new ItemStack(starch),
				new ItemStack(Items.PAPER), new ItemStack(Items.PAPER), new ItemStack(Items.PAPER), new ItemStack(Items.PAPER), new ItemStack(pellets)));
		var recipe = h.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel()).orElseThrow().value();
		var result = recipe.assemble(input);
		h.assertTrue(result.is(ControlledRelease.item("release_matrix_blend")) && result.getCount() == 4, "Starch, paper and plastic provide four matrix blend before any XR product exists");
		for (var wool : List.of(Items.WOOL.white(), Items.WOOL.red())) {
			input = CraftingInput.of(2, 2, List.of(new ItemStack(wool), new ItemStack(wool), new ItemStack(Items.PAPER),
					new ItemStack(io.github.jimbozoomer.jugcraft.chemistry.PetroItems.RUBBER)));
			result = h.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel()).orElseThrow().value().assemble(input);
			h.assertTrue(result.is(ControlledRelease.item("pharmacy_patch_backing")) && result.getCount() == 4, "Ordinary wool colors produce exactly four backing with no vial or finished patch");
		}
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void releaseDieUsesExistingUpgradeSlotsAndCannotBeReplacedByThePrecisionModule(GameTestHelper h) {
		for (var block : List.of(IndustrialForms.LABORATORY_ANALYZER, IndustrialForms.FORMULATION_PRESS)) {
			var machine = place(h, block, Direction.NORTH);
			String job = block == IndustrialForms.LABORATORY_ANALYZER ? "release_analyze_caffeine_xr" : "release_matrix";
			var recipe = recipe(machine, h, job); feed(machine, recipe);
			int slot = machine.form().firstUpgradeSlot();
			h.assertTrue(machine.form().containerSize() == (block == IndustrialForms.LABORATORY_ANALYZER ? 5 : 7), "Existing saved machine layouts stay unchanged");
			machine.container().setItem(slot, new ItemStack(PrecisionPharmacy.item("precision_process_module")));
			long energy = machine.energy().getAmount(); int raw = machine.container().getItem(0).getCount(); ticks(h, machine, 1);
			h.assertTrue(machine.lane(0) == null && machine.energy().getAmount() == energy && machine.container().getItem(0).getCount() == raw, "Old precision module cannot authorize a release job");
			machine.container().setItem(slot, new ItemStack(ControlledRelease.item("release_control_die"))); ticks(h, machine, 20);
			var p = player(h); var menu = (FormMachineMenu) machine.createMenu(0, p.getInventory(), p);
			h.assertTrue(machine.lane(0) != null && !menu.getSlot(slot).mayPickup(p) && !menu.getSlot(slot).mayPlace(new ItemStack(PrecisionPharmacy.item("precision_process_module"))), "Virtual process-tool slot locks during release work");
			var saved = machine.saveWithoutMetadata(h.getLevel().registryAccess());
			machine.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved));
			powered(h, machine, recipe.time());
			h.assertTrue(machine.container().getItem(slot).is(ControlledRelease.item("release_control_die")) && !machine.container().getItem(machine.form().firstOutputSlot()).isEmpty(), "Saved release job finishes and keeps the die");
			clear(h, machine);
		}
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void everyCoatedProductPaysExtraActiveStockAndItsMatchingMatrix(GameTestHelper h) {
		String[] ids = {"caffeine_xr", "methylphenidate_xr", "amphetamine_xr", "modafinil_xr", "fentanyl_patch"};
		int[] active = {24, 4, 6, 4, 8};
		for (int i = 0; i < ids.length; i++) {
			var machine = place(h, IndustrialForms.RELEASE_COATER, Direction.SOUTH); var job = recipe(machine, h, "release_coat_" + ids[i]);
			h.assertTrue(job.items().getFirst().count() == active[i] && job.items().get(1).count() == 4 && job.results().getFirst().count() == 4, "Extra active and four matrices pay for four products");
			h.assertTrue(job.items().get(1).ingredient().test(new ItemStack(ControlledRelease.item(ids[i].equals("modafinil_xr") ? "advanced_release_matrix" : "release_matrix"))), "Named matrix is required");
			if (ids[i].equals("fentanyl_patch")) h.assertTrue(!job.items().getFirst().ingredient().test(new ItemStack(PrecisionPharmacy.item("fentanyl"))) && job.items().get(2).count() == 4, "One vial cannot multiply into patches; verified active and four backing are paid");
			feed(machine, job); powered(h, machine, job.time());
			h.assertTrue(machine.container().getItem(machine.form().firstOutputSlot()).getCount() == 4, "Four coated cores/patch stock complete"); clear(h, machine);
		}
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void coaterWaitsForPowerSavesWorkAndCancelsOnlyToOriginalInputs(GameTestHelper h) {
		var machine = place(h, IndustrialForms.RELEASE_COATER, Direction.EAST); var job = recipe(machine, h, "release_coat_fentanyl_patch"); feed(machine, job);
		ticks(h, machine, 20); var saved = machine.saveWithoutMetadata(h.getLevel().registryAccess());
		machine.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved));
		int progress = machine.lane(0).progress(); ((SimpleEnergyStorage) machine.energy()).setAmount(0); ticks(h, machine, 20);
		h.assertTrue(machine.lane(0).progress() == progress && machine.container().getItem(machine.form().firstOutputSlot()).isEmpty(), "Saved work stalls without power and cannot finish for free");
		machine.cancel();
		h.assertTrue(machine.lane(0) == null && machine.container().getItem(0).getCount() == 8 && machine.container().getItem(1).getCount() == 4
				&& machine.container().getItem(2).getCount() == 4 && machine.tanks().input(0).millibuckets() == 500, "Cancel returns eight original active, four matrices, four backing and original water");
		powered(h, machine, 20); var at = machine.getBlockPos();
		var far = machine.form().footprint().partPos(at, machine.getBlockState().getValue(MachineBlock.FACING), machine.form().footprint().size() - 1);
		h.getLevel().destroyBlock(far, true); var drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(5));
		h.assertTrue(count(drops, IndustrialForms.RELEASE_COATER.asItem()) == 1 && count(drops, PrecisionPharmacy.item("fentanyl_active")) == 8
				&& count(drops, ControlledRelease.item("release_matrix")) == 4 && count(drops, ControlledRelease.item("pharmacy_patch_backing")) == 4
				&& count(drops, ControlledRelease.item("release_control_die")) == 1 && count(drops, ControlledRelease.item("fentanyl_patch_stock")) == 0, "Breaking a far part drops one machine and only original uncompleted solids/tool");
		h.succeed();
	}
	private static int count(List<ItemEntity> drops, net.minecraft.world.item.Item item) { return drops.stream().filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum(); }

	@GameTest
	public void xrCapsulesHaveExactOnsetsEffectsCostsAndSharedStimulantExclusion(GameTestHelper h) {
		var profiles = List.of(ControlledRelease.CAFFEINE_XR, ControlledRelease.METHYLPHENIDATE_XR, ControlledRelease.AMPHETAMINE_XR, ControlledRelease.MODAFINIL_XR);
		int[] durations = {1800, 2400, 2100, 3600}, strains = {10, 30, 40, 25};
		for (int i = 0; i < profiles.size(); i++) {
			var p = player(h); var profile = profiles.get(i);
			var result = new ItemStack(ControlledRelease.item(profile.id())).finishUsingItem(p.level(), p);
			h.assertTrue(result.isEmpty() && !p.hasEffect(MobEffects.HASTE) && Pharmacology.strain(p) == strains[i], "Self-used capsule has no bottle and reserves exact strain before benefit");
			h.assertTrue(profile.durationSeconds() == durations[i] && profile.onsetSeconds() == 30, "Authored XR duration and onset");
			advance(p, 30);
			h.assertTrue(p.getEffect(MobEffects.HASTE).getAmplifier() == profile.benefits().haste(), "XR native Haste has the stated peak");
			if (profile == ControlledRelease.AMPHETAMINE_XR) h.assertTrue(p.getEffect(MobEffects.SPEED).getAmplifier() == 0, "Amphetamine XR is deliberately lower peak than immediate-release Amphetamine");
			if (profile == ControlledRelease.CAFFEINE_XR) h.assertTrue(Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED) - .11) < .001, "Caffeine XR retains ten percent walking");
			if (profile.traits().aimSway() < 1) h.assertTrue(PrecisionPharmacy.aimSwayMultiplier(p) == profile.traits().aimSway(), "Existing supported hand-sway integration receives XR focus");
			float before = exhaustion(p.getFoodData()); p.causeFoodExhaustion(1);
			h.assertTrue(Math.abs(exhaustion(p.getFoodData()) - before - profile.traits().activityCost()*profile.benefits().exhaustion()) < .001, "XR activity cost and work discount are exact");
			h.assertTrue(Pharmacology.mayDrink(p, Pharmacology.CAFFEINE, false) && Pharmacology.mayDrink(p, PrecisionPharmacy.METHYLPHENIDATE, false), "Immediate-release products can replace XR while keeping family tolerance");
			advance(p, durations[i] * 3 / 4 + 1);
			h.assertTrue(Pharmacology.mayDrink(p, profile, false), "XR may be redosed for a full new strain charge");
		}
		h.succeed();
	}

	@GameTest
	public void longReleaseStateRoundtripsAndDeathKeepsRecoveryAndTolerance(GameTestHelper h) {
		var p = player(h); Pharmacology.drink(p, ControlledRelease.MODAFINIL_XR); advance(p, 45);
		var state = Pharmacology.state(p); var encoded = DrugState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow();
		h.assertTrue(DrugState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow().equals(state) && state.doses().getFirst().total() == 3600, "The existing attachment codec accepts the full sixty-minute named formulation");
		Pharmacology.onDeath(p);
		h.assertTrue(Pharmacology.state(p).strainTenths() == state.strainTenths() && Pharmacology.state(p).doses().getFirst().recovery() == 120 && !p.hasEffect(ControlledRelease.MODAFINIL_XR.marker()), "Death ends benefit without resetting strain/recovery");
		advance(p, 120); Pharmacology.drink(p, ControlledRelease.CAFFEINE_XR);
		h.assertTrue(Pharmacology.state(p).doses().getFirst().total() == 1350, "Changing XR formulations keeps the common 75-percent stimulant repeat duration");
		h.succeed();
	}

	@GameTest
	public void amphetamineXrExpiryAndCharcoalUseExistingDrugOnlyFatigue(GameTestHelper h) {
		var p = player(h); Pharmacology.drink(p, ControlledRelease.AMPHETAMINE_XR); advance(p, 30 + 2100);
		h.assertTrue(p.hasEffect(SterilePharmacy.FATIGUE) && p.getEffect(SterilePharmacy.FATIGUE).getDuration() == 2400, "Natural XR expiry applies two minutes of existing Stimulant Fatigue");
		new ItemStack(Pharmacology.ACTIVATED_CHARCOAL).finishUsingItem(p.level(), p);
		h.assertTrue(!p.hasEffect(SterilePharmacy.FATIGUE) && Pharmacology.strain(p) == 0, "The long XR benefit outlasts its strain; Charcoal clears fatigue");
		h.succeed();
	}

	@GameTest
	public void patchIsLowerPeakAndExistingNaloxonePreservesItsOpioidAccounting(GameTestHelper h) {
		var p = player(h); p.hasChangedDimension(); p.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
		var result = new ItemStack(ControlledRelease.item("fentanyl_patch")).finishUsingItem(p.level(), p);
		h.assertTrue(result.isEmpty() && !p.hasEffect(SterilePharmacy.DROWSINESS), "Self-applied patch returns no bottle and waits for onset"); advance(p, 30);
		h.assertTrue(Math.abs(hurt(p) - 3.2F) < .001 && Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED) - .09) < .001
				&& Math.abs(p.getAttributeValue(Attributes.BLOCK_BREAK_SPEED) - .9) < .001 && Math.abs(p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) - .5) < .001, "Patch has lower 20-percent defense, 50-percent knockback and 10-percent movement/mining penalty");
		p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 12000, 1));
		h.assertTrue(Math.abs(hurt(p) - 2.4F) < .001, "Stronger native Resistance wins without multiplying patch protection");
		new ItemStack(SterilePharmacy.item("naloxone")).finishUsingItem(p.level(), p);
		h.assertTrue(!p.hasEffect(ControlledRelease.FENTANYL_PATCH.marker()) && !p.hasEffect(SterilePharmacy.DROWSINESS)
				&& p.hasEffect(MobEffects.RESISTANCE) && Pharmacology.strain(p) == 37 && Pharmacology.state(p).doses().getFirst().recovery() == 180, "Existing reversal keeps patch accounting and unrelated potion");
		h.assertTrue(Pharmacology.mayDrink(p, SterilePharmacy.MORPHINE, false), "Patch recovery permits a new paid opioid formulation");
		advance(p, 180); Pharmacology.drink(p, PrecisionPharmacy.FENTANYL);
		h.assertTrue(Pharmacology.state(p).doses().getFirst().total() == 360, "Patch and vial share one 75-percent repeat-duration history"); h.succeed();
	}
	private static float hurt(ServerPlayer p) { p.setHealth(20); p.setInvulnerableTime(0); p.damageCooldownTime = 0; p.hurtServer(p.level(), p.damageSources().fall(), 4); return 20 - p.getHealth(); }

	@GameTest
	public void masterworkIsStrongerSharesAlcoholRulesAndReturnsOneBottle(GameTestHelper h) {
		var p = player(h); double damage = p.getAttributeValue(Attributes.ATTACK_DAMAGE);
		var result = new ItemStack(ControlledRelease.item("masterwork_reserve")).finishUsingItem(p.level(), p);
		h.assertTrue(result.is(Items.GLASS_BOTTLE) && Pharmacology.strain(p) == 60, "Premium drink returns one bottle and reserves 60 strain"); advance(p, 20);
		h.assertTrue(Math.abs(p.getAttributeValue(Attributes.ATTACK_DAMAGE) - damage * 1.30) < .001 && Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED) - .085) < .001
				&& Math.abs(p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) - .60) < .001 && Math.abs(Pharmacology.aimSpreadMultiplier(p) - 1.35F) < .001, "Exact top-grade bonuses and drug-caused tradeoffs");
		advance(p, 1125); h.assertTrue(Pharmacology.mayDrink(p, ControlledRelease.MASTERWORK_RESERVE, false), "Masterwork permits another paid dose");
		Pharmacology.cancelFamily(p, "intoxicant"); advance(p, 180);
		var moonshine = ((DrugDrinkItem) PrecisionPharmacy.item("moonshine")).profile(); Pharmacology.drink(p, moonshine);
		h.assertTrue(Pharmacology.state(p).doses().getFirst().total() == 360, "Downgrading after 180-second recovery retains alcohol tolerance"); h.succeed();
	}

	@GameTest
	public void newTopTierProductsKeepSymmetricCompatibilityThroughRecovery(GameTestHelper h) {
		var profiles = List.of(ControlledRelease.MASTERWORK_RESERVE, ControlledRelease.FENTANYL_PATCH, PrecisionPharmacy.KETAMINE);
		for (var current : profiles) {
			var p = player(h); Pharmacology.drink(p, current);
			for (int phase = 0; phase < 3; phase++) {
				for (var other : profiles) if (other != current) {
					h.assertTrue(Pharmacology.mayDrink(p, other, false), "Restricted families are available across onset, benefit and recovery");
				}
				if (phase == 0) advance(p, current.onsetSeconds());
				if (phase == 1) { if (current == PrecisionPharmacy.KETAMINE) EchoView.cancel(p); else Pharmacology.cancelFamily(p, current.family()); }
			}
		}
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void masterworkConditioningRequiresItsOwnInsertAndSavesLoadedProgress(GameTestHelper h) {
		var machine = place(h, IndustrialForms.MASH_CONDITIONING_BARREL, Direction.WEST); var job = recipe(machine, h, "release_masterwork_conditioning"); feed(machine, job);
		int socket = machine.form().firstSocketSlot(); machine.container().setItem(socket, new ItemStack(PrecisionPharmacy.item("oak_conditioning_insert"))); ticks(h, machine, 1);
		h.assertTrue(machine.lane(0) == null && machine.container().getItem(0).getCount() == 4, "Basic oak insert cannot make the fifth grade");
		machine.container().setItem(socket, new ItemStack(ControlledRelease.item("masterwork_conditioning_insert"))); ticks(h, machine, 2400);
		var saved = machine.saveWithoutMetadata(h.getLevel().registryAccess());
		machine.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved));
		h.assertTrue(machine.lane(0).progress() == 2400 && machine.energy().getCapacity() == 0, "Saved loaded-only conditioning has no hidden electrical or offline progress");
		ticks(h, machine, 2400); var result = machine.container().getItem(machine.form().firstOutputSlot());
		h.assertTrue(result.is(ControlledRelease.item("masterwork_reserve_concentrate")) && result.getCount() == 4 && machine.container().getItem(socket).getCount() == 1, "Four premium concentrate and reusable advanced insert"); h.succeed();
	}
}
