package io.github.jimbozoomer.jugcraft.test;

import static io.github.jimbozoomer.jugcraft.test.PharmacologyFactoryGameTests.*;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import io.github.jimbozoomer.jugcraft.machine.form.IndustrialForms;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import io.github.jimbozoomer.jugcraft.chemistry.FluidRecipes;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;

public class PharmacologyPsychedelicGameTests {
	private static final String ARENA = "jugcraft-test:arms_arena";
	private static void advance(ServerPlayer p, int seconds) { for (int i = 0; i < seconds; i++) Pharmacology.tickSecond(p); }
	private static ServerPlayer player(GameTestHelper h) { var p = TestPlayers.survivor(h); p.setNoGravity(true); p.setOnGround(true); return p; }
	private static BlockPos site(GameTestHelper h, ServerPlayer p) {
		var pos = h.absolutePos(new BlockPos(3, 2, 3));
		h.getLevel().setBlockAndUpdate(pos, DreamStudies.PLINTH.defaultBlockState());
		p.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + 3);
		p.getInventory().setItem(0, new ItemStack(DreamStudies.FOLIO));
		p.getInventory().setItem(1, new ItemStack(PsychedelicPharmacy.item("study_canvas"), 4));
		return pos;
	}
	private static DreamStudyMenu menu(ServerPlayer p, BlockPos pos) {
		var menu = new DreamStudyMenu(17, p.getInventory(), pos); p.containerMenu = menu; return menu;
	}
	private static boolean click(ServerPlayer p, DreamStudyMenu menu, int button) { RateGate.forget(p.getUUID()); return menu.clickMenuButton(p, button); }
	private static void solve(ServerPlayer p, DreamStudyMenu menu) {
		int[] clues = {menu.clue(0), menu.clue(1), menu.clue(2)};
		for (int i = 0; i < 3; i++) click(p, menu, menu.lesson() == 1 ? clues[2 - i] : menu.lesson() == 2 ? (clues[i] + 1) % 4 : clues[i]);
	}
	private static int count(ServerPlayer p, String id) { return p.getInventory().countItem(PsychedelicPharmacy.item(id)); }

	@GameTest
	public void fiveProfilesPersistInTheSharedLedgerAndKeepFamiliesBounded(GameTestHelper h) {
		for (var profile : List.of(PsychedelicPharmacy.THC, PsychedelicPharmacy.PSILOCYBIN, PsychedelicPharmacy.MESCALINE, PsychedelicPharmacy.LSD, PsychedelicPharmacy.DMT)) {
			var p = player(h); h.assertTrue(Pharmacology.drink(p, profile), "First psychedelic formulation starts");
			h.assertTrue(Pharmacology.mayDrink(p, profile, false), "Redosing is available during onset for a new charge");
			var saved = DrugState.CODEC.encodeStart(JsonOps.INSTANCE, Pharmacology.state(p)).getOrThrow();
			h.assertTrue(DrugState.CODEC.parse(JsonOps.INSTANCE, saved).getOrThrow().equals(Pharmacology.state(p)), "New IDs round-trip the unchanged bounded save codec");
			if (profile != PsychedelicPharmacy.THC) h.assertTrue(Pharmacology.mayDrink(p, profile == PsychedelicPharmacy.DMT ? PsychedelicPharmacy.PSILOCYBIN : PsychedelicPharmacy.DMT, false), "A different psychedelic can replace the family");
			advance(p, profile.onsetSeconds());
			h.assertTrue(Pharmacology.activeProfile(p, profile) && Pharmacology.state(p).strainTenths() == profile.strain()*10-profile.onsetSeconds(), "Benefit starts after onset while strain decays");
			Pharmacology.cancelFamily(p, profile.family());
			h.assertTrue(Pharmacology.mayDrink(p, profile, false) && Pharmacology.state(p).strainTenths() == profile.strain()*10-profile.onsetSeconds(), "Cancellation preserves the current balance and permits another paid dose");
		}
		h.succeed();
	}
	@GameTest
	public void thcUsesNativeLuckToolSpeedAndItsOwnExpiryAppetite(GameTestHelper h) {
		var p = player(h); double mining = p.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
		Pharmacology.drink(p, PsychedelicPharmacy.THC);
		h.assertTrue(!p.hasEffect(MobEffects.LUCK) && !p.hasEffect(PsychedelicPharmacy.RELAXED), "No benefit or drawback before onset");
		advance(p, 10);
		h.assertTrue(p.hasEffect(MobEffects.LUCK) && Math.abs(p.getAttributeValue(Attributes.BLOCK_BREAK_SPEED) - mining * .9) < .0001, "Native Luck I and exact 10% slower mining");
		h.assertTrue(PsychedelicPharmacy.fasterBite(p, 3) && !PsychedelicPharmacy.fasterBite(p, 2), "Bounded pre-bite cadence");
		p.setSprinting(true); h.assertTrue(!PsychedelicPharmacy.fasterBite(p, 3), "Moving/sprinting cannot claim the stationary bonus"); p.setSprinting(false);
		advance(p, 600);
		h.assertTrue(p.getEffect(Pharmacology.STIMULANT_APPETITE).getDuration() == 1200 && !Pharmacology.activeProfile(p, PsychedelicPharmacy.THC), "Natural expiry adds exactly 60 s identifiable drug appetite");
		p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200));
		new ItemStack(Pharmacology.ACTIVATED_CHARCOAL).finishUsingItem(p.level(), p);
		h.assertTrue(!p.hasEffect(Pharmacology.STIMULANT_APPETITE) && p.hasEffect(MobEffects.HUNGER), "Charcoal removes this drug appetite, preserving unrelated Hunger");
		h.succeed();
	}
	@GameTest
	public void thcAdvancesOnlyTheNativePreBiteTimer(GameTestHelper h) throws Exception {
		var p = player(h); Pharmacology.drink(p, PsychedelicPharmacy.THC); advance(p, 10);
		var hook = new FishingHook(p, h.getLevel(), 0, 0);
		var waiting = FishingHook.class.getDeclaredField("timeUntilLured"); waiting.setAccessible(true);
		var hooked = FishingHook.class.getDeclaredField("timeUntilHooked"); hooked.setAccessible(true);
		var tick = FishingHook.class.getDeclaredMethod("catchingFish", BlockPos.class); tick.setAccessible(true);
		waiting.setInt(hook, 90); hooked.setInt(hook, 0); hook.getRandom().setSeed(91); hook.tickCount = 3;
		tick.invoke(hook, p.blockPosition()); int withDrug = waiting.getInt(hook);
		Pharmacology.cancelFamily(p, "cannabinoid");
		waiting.setInt(hook, 90); hooked.setInt(hook, 0); hook.getRandom().setSeed(91);
		tick.invoke(hook, p.blockPosition());
		h.assertTrue(waiting.getInt(hook) == withDrug + 1 && hooked.getInt(hook) == 0, "Native fishing method receives exactly one extra wait tick; no catch-window change");
		hook.discard(); h.succeed();
	}
	@GameTest(structure = ARENA)
	public void soberStudyHidesCluesAndCompletedMemoryPaysOnce(GameTestHelper h) {
		var p = player(h); var pos = site(h, p); h.assertTrue(DreamStudies.discover(p, pos), "Physical visit records a study site");
		var menu = menu(p, pos);
		h.assertTrue(menu.clue(0) == -1 && !click(p, menu, 0) && count(p, "study_canvas") == 4, "Sober forged input reveals/spends nothing");
		Pharmacology.drink(p, PsychedelicPharmacy.PSILOCYBIN); advance(p, 10); menu.broadcastChanges();
		h.assertTrue(menu.ready() && menu.clue(0) >= 0 && menu.sight() == 1, "Post-onset Dream Sight reveals only its first layer");
		click(p, menu, (menu.clue(0) + 1) % 4); h.assertTrue(menu.progress() == 0 && count(p, "study_canvas") == 4, "Wrong symbol is free to retry");
		solve(p, menu);
		h.assertTrue(menu.status() == 2 && count(p, "study_canvas") == 3 && count(p, "lucid_imprint") == 1, "One paid Memory lesson awards exactly one imprint");
		h.assertTrue(!click(p, menu, 0) && !click(p, menu, 5) && count(p, "lucid_imprint") == 1, "Completed or replayed native button cannot multiply rewards");
		var other = player(h); h.assertTrue(!click(other, menu, 0), "A different player cannot operate another player's study");
		h.succeed();
	}
	@GameTest(structure = ARENA)
	public void higherLayersRequireTheirOwnDoseAndDmtNeverTravels(GameTestHelper h) {
		for (var profile : List.of(PsychedelicPharmacy.PSILOCYBIN, PsychedelicPharmacy.LSD, PsychedelicPharmacy.DMT)) {
			var p = player(h); var pos = site(h, p); Pharmacology.drink(p, profile); advance(p, profile.onsetSeconds());
			var original = p.position(); var dimension = p.level().dimension(); var mode = p.gameMode.getGameModeForPlayer();
			var menu = menu(p, pos); int lesson = profile == PsychedelicPharmacy.PSILOCYBIN ? 1 : profile == PsychedelicPharmacy.LSD ? 1 : 2;
			click(p, menu, 4 + lesson);
			if (profile == PsychedelicPharmacy.PSILOCYBIN) h.assertTrue(menu.clue(0) == -1 && !click(p, menu, 0), "Psilocybin cannot reveal or solve Mirror");
			else { solve(p, menu); h.assertTrue(count(p, lesson == 1 ? "pattern_imprint" : "oneiric_imprint") == 1, "Correct transform awards the authored higher imprint"); }
			h.assertTrue(p.position().equals(original) && p.level().dimension().equals(dimension) && p.gameMode.getGameModeForPlayer() == mode && p.getInventory().getItem(0).is(DreamStudies.FOLIO), "Study never teleports, changes mode or escrows inventory");
		}
		h.succeed();
	}
	@GameTest(structure = ARENA)
	public void sightAndResonanceUseOnlyRecordedLoadedPlinths(GameTestHelper h) {
		var p = player(h); var pos = site(h, p); DreamStudies.discover(p, pos);
		h.assertTrue(DreamStudies.visible(p, pos, 16), "Nearby plinth has direct line of sight");
		int loaded = p.level().getChunkSource().getLoadedChunksCount();
		h.assertTrue(!DreamStudies.visible(p, pos.offset(16384, 0, 16384), 24)
				&& loaded == p.level().getChunkSource().getLoadedChunksCount(), "An unloaded clue query never loads a destination or ray path");
		var wall = pos.offset(0, 1, 1); h.getLevel().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
		h.assertTrue(!DreamStudies.visible(p, pos, 16), "Opaque block blocks clue reveal"); h.getLevel().removeBlock(wall, false);
		Pharmacology.drink(p, PsychedelicPharmacy.MESCALINE); advance(p, 15);
		h.assertTrue(DreamStudies.resonance(p).orElseThrow().equals(pos), "Mescaline points at a physically recorded site");
		p.setPos(pos.getX() + 100, pos.getY(), pos.getZ());
		h.assertTrue(DreamStudies.resonance(p).isEmpty() && !DreamStudies.visible(p, pos, 24), "Range bounds exclude distant sites");
		p.setPos(pos.getX() + 2, pos.getY(), pos.getZ()); h.getLevel().removeBlock(pos, false);
		h.assertTrue(DreamStudies.resonance(p).isEmpty(), "Stale recorded coordinates cannot reveal unrelated replacement blocks");
		h.succeed();
	}
	@GameTest(structure = ARENA)
	public void missingPaymentFullInventoryAndStaleMenusCannotProduceImprints(GameTestHelper h) {
		var p = player(h); var pos = site(h, p); Pharmacology.drink(p, PsychedelicPharmacy.PSILOCYBIN); advance(p, 10);
		p.getInventory().setItem(1, ItemStack.EMPTY); var menu = menu(p, pos); solve(p, menu);
		h.assertTrue(menu.status() == 3 && count(p, "lucid_imprint") == 0, "Missing payment produces nothing");
		for (int slot = 1; slot < 36; slot++) p.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
		p.getInventory().setItem(1, new ItemStack(PsychedelicPharmacy.item("study_canvas"), 2));
		click(p, menu, menu.clue(2));
		h.assertTrue(count(p, "study_canvas") == 2 && count(p, "lucid_imprint") == 0, "No output room refunds payment atomically");
		p.getInventory().setItem(2, ItemStack.EMPTY); Pharmacology.cancelFamily(p, "psychedelic"); menu.broadcastChanges();
		h.assertTrue(menu.clue(0) == -1 && !click(p, menu, 0), "Cleansed/expired dose cannot solve a previously opened menu");
		p.containerMenu = p.inventoryMenu; h.assertTrue(!click(p, menu, 0), "Closed container rejects replayed input"); h.succeed();
	}
	@GameTest
	public void lsdAimDrawbackUsesNativeSyncAndDoesNotChangeShotSpread(GameTestHelper h) {
		var p = player(h); Pharmacology.drink(p, PsychedelicPharmacy.LSD);
		h.assertTrue(PrecisionPharmacy.aimSwayMultiplier(p) == 1, "No early aim drawback during onset"); advance(p, 20);
		h.assertTrue(Math.abs(PrecisionPharmacy.aimSwayMultiplier(p) - 1.2F) < .0001 && Pharmacology.aimSpreadMultiplier(p) == 1, "Twenty percent hand sway uses its own synced effect without modifying shot spread");
		p.addEffect(new MobEffectInstance(PrecisionPharmacy.FOCUS, 40, 1));
		h.assertTrue(Math.abs(PrecisionPharmacy.aimSwayMultiplier(p) - .9F) < .0001, "Existing focus and drug interference compose predictably");
		Pharmacology.cancelFamily(p, "psychedelic"); h.assertTrue(!p.hasEffect(PsychedelicPharmacy.PATTERN_INTERFERENCE), "Drug-only aim drawback ends immediately with cancellation"); h.succeed();
	}
	@GameTest(structure = ARENA)
	public void folioAndNewDoseSurviveActualLogoutWithoutAnOfflineClock(GameTestHelper h) {
		var p = player(h); var pos = site(h, p); DreamStudies.discover(p, pos);
		Pharmacology.drink(p, PsychedelicPharmacy.DMT); advance(p, 7);
		var state = Pharmacology.state(p); var id = p.getUUID();
		TestPlayers.leave(p); var joined = TestPlayers.again(h, id);
		h.assertTrue(Pharmacology.state(joined).equals(state), "Actual player save/load preserves pending DMT duration, onset, strain and tolerance exactly");
		h.assertTrue(DreamStudies.sites(DreamStudies.folio(joined)).equals(List.of(pos)), "Discovered site survives native inventory persistence");
		h.assertTrue(PsychedelicPharmacy.sight(joined) == 0, "Rejoining cannot skip the remaining onset");
		advance(joined, 13); h.assertTrue(PsychedelicPharmacy.sight(joined) == 3, "Saved onset reaches Deep Reverie through the original dose clock");
		h.succeed();
	}
	@GameTest(structure = ARENA)
	public void advancedStocksRunOnExistingPrecisionReactorAndCompactRecipesStayReachable(GameTestHelper h) {
		for (String drug : List.of("lsd", "dmt")) {
			var machine = place(h, IndustrialForms.BULK_CHEMICAL_REACTOR, Direction.NORTH);
			var job = recipe(machine, h, "perception_prepare_" + drug); feed(machine, job);
			for (int i = 0; i < job.time(); i++) { ((SimpleEnergyStorage) machine.energy()).setAmount(machine.energy().getCapacity()); ticks(h, machine, 1); }
			h.assertTrue(machine.container().getItem(machine.form().firstOutputSlot()).is(PsychedelicPharmacy.item(drug + "_stock"))
					&& machine.container().getItem(machine.form().firstOutputSlot()).getCount() == 4, "Precision reactor yields exactly four paid stock"); clear(h, machine);
		}
		var compact = FluidRecipes.entries(h.getLevel().getServer(), MachineKind.CHEMICAL_REACTOR).stream().filter(e -> e.id().getPath().contains("perception_") && e.recipe().capability().isEmpty()).toList();
		h.assertTrue(compact.size() == 4 && compact.stream().allMatch(e -> e.recipe().items().size() == 2 && e.recipe().tool().isEmpty()), "Four early botanical jobs fit existing compact slots without precision or completed-drug gates");
		for (var entry : compact) {
			var block = io.github.jimbozoomer.jugcraft.machine.JugcraftMachines.MACHINES.get(MachineKind.CHEMICAL_REACTOR);
			var at = new BlockPos(4, 1, 4); var state = block.defaultBlockState().setValue(io.github.jimbozoomer.jugcraft.machine.MachineBlock.FACING, Direction.NORTH);
			h.setBlock(at, state); block.setPlacedBy(h.getLevel(), h.absolutePos(at), state, null, ItemStack.EMPTY);
			var machine = h.getBlockEntity(at, io.github.jimbozoomer.jugcraft.machine.MachineBlockEntity.class); var job = entry.recipe();
			for (int i=0;i<job.items().size();i++) machine.setItem(i, new ItemStack(job.items().get(i).ingredient().items().findFirst().orElseThrow(), job.items().get(i).count()));
			machine.tanks().input(0).fill(net.minecraft.world.level.material.Fluids.WATER, 500);
			((SimpleEnergyStorage)machine.energyFor(null)).setAmount(30000);
			for (int i=0;i<job.time();i++) machine.serverTick(h.getLevel(), machine.getBlockPos(), state);
			var result = machine.getItem(MachineKind.CHEMICAL_REACTOR.outputSlot());
			h.assertTrue(ItemStack.isSameItemSameComponents(result, job.results().getFirst().create()) && result.getCount()==4
					&& machine.getItem(0).isEmpty() && machine.getItem(1).isEmpty() && machine.tanks().input(0).millibuckets()==0, "Each early compact job runs, paying both solids and water exactly once");
			h.getLevel().removeBlock(machine.getBlockPos(), false);
		}
		h.succeed();
	}
	@GameTest
	public void starterSporesAndCatalystHaveDistinctReachableCraftingRecipes(GameTestHelper h) {
		for (var mushroom : List.of(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM)) {
			var input = CraftingInput.of(3, 1, List.of(new ItemStack(mushroom), new ItemStack(Items.BONE_MEAL), new ItemStack(Items.AMETHYST_SHARD)));
			var recipe = h.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel()).orElseThrow();
			var result = recipe.value().assemble(input); String id = mushroom == Items.BROWN_MUSHROOM ? "dreamcap_seeds" : "mycelium_catalyst";
			h.assertTrue(result.is(PsychedelicPharmacy.item(id)) && result.getCount()==(mushroom == Items.BROWN_MUSHROOM ? 2 : 4), "Brown mushroom starter spores and red mushroom catalyst are separate, reachable native crafts");
		}
		h.succeed();
	}
	@GameTest(structure = ARENA)
	public void nativeCropAgeControlsHarvestAndReturnsSeeds(GameTestHelper h) {
		for (var entry : PsychedelicPharmacy.CROPS.entrySet()) {
			var crop = (CropBlock) entry.getValue(); var pos = h.absolutePos(new BlockPos(3, 1, 3));
			var seed = PsychedelicPharmacy.item(entry.getKey().replace("_crop", "_seeds"));
			h.assertTrue(((BlockItem) seed).getBlock() == crop, "The registered seed plants its actual native crop");
			var young = Block.getDrops(crop.getStateForAge(0), h.getLevel(), pos, null);
			var mature = Block.getDrops(crop.getStateForAge(7), h.getLevel(), pos, null);
			h.assertTrue(young.size() == 1 && young.getFirst().is(seed), "Immature crop returns a seed, never active stock");
			h.assertTrue(mature.stream().filter(s -> s.is(seed)).mapToInt(ItemStack::getCount).sum() == 2
					&& mature.stream().filter(s -> !s.is(seed)).mapToInt(ItemStack::getCount).sum() == 2, "Mature crop yields exactly two produce and two seeds");
			h.getLevel().setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState());
			h.getLevel().setBlockAndUpdate(pos, crop.getStateForAge(7));
			h.getLevel().getBlockState(pos.below()).randomTick(h.getLevel(), pos.below(), net.minecraft.util.RandomSource.create(7));
			h.assertTrue(h.getLevel().getBlockState(pos.below()).is(Blocks.FARMLAND) && h.getLevel().getBlockState(pos).is(crop), "Native dry-farmland update preserves each cultivated crop instead of popping it off dirt");
		}
		h.succeed();
	}
}
