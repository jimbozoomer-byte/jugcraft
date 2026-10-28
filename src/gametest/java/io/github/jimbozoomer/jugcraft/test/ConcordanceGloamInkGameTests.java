package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonParser;
import io.github.jimbozoomer.jugcraft.compat.jade.ConcordanceDataProvider;
import io.github.jimbozoomer.jugcraft.concordance.Alchemy;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleStandBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.AlchemyCatalog;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Assay;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Axis;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Band;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.CrucibleInspection;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Extract;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Formula;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Heat;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Mixture;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Operation;
import io.github.jimbozoomer.jugcraft.concordance.compose.Text;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Code;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Diagnostic;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.rules.ConcordanceRules;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.rules.RulesParser;
import io.github.jimbozoomer.jugcraft.concordance.sign.Sign;
import io.github.jimbozoomer.jugcraft.energy.CreativeEnergyCellBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import local.peepo.CompanionStatus;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import org.jspecify.annotations.Nullable;

/**
 * Gloam Ink (Concordance C-D6; docs/features/gloam-ink.md): Gloamcap and its carrier, a spider eye, stirred under a lid (a
 * closed trapdoor or a slab on the crucible) bottle as Gloam Ink in a glass bottle; murky, as Gloam Sludge, which a
 * furnace boils back to its bottle; thin, uncovered, or in a bowl, as before. A formula writes its covered stirs and
 * waits for its lid before each (needs_cover; a Crucible Stand idles); its shortage
 * asks for the Gloamcap, then the carrier; a full output pauses; a covered batch and its formula survive a save; the lid
 * and what a bottle draws are shown, never the makeup; the extract's data parses strictly. Numbers are the generator's
 * model (tools/concordance_alchemy.py EXTRACTS), held to GLOAM by check_gloam_ink.
 */
public class ConcordanceGloamInkGameTests {
	private static final String GLOAMCAP = "jugcraft:gloamcap";
	private static final String EYE = "minecraft:spider_eye";
	private static final String RAW = "jugcraft:raw";
	private static final String INK = "jugcraft:gloam_ink";
	private static final String SLUDGE = "jugcraft:gloam_sludge";
	/** A stir may follow another only after this many ticks (CrucibleBlockEntity.STIR_TICKS, plus one). */
	private static final int NEXT_STIR = CrucibleBlockEntity.STIR_TICKS + 1;
	/** The worked formula (tools/concordance_alchemy.py EXTRACTS gloam_ink "process"), as a formula writes it. */
	static final String GLOAM_INK = "water 3; add jugcraft:gloamcap jugcraft:raw; add jugcraft:gloamcap jugcraft:raw; "
			+ "add jugcraft:gloamcap jugcraft:raw; add minecraft:spider_eye jugcraft:raw; stir hot covered; stir hot covered";
	/** On paper: the worked batch's Hollow and contaminant a part, the murky one's (a second eye), the thin one's Hollow. */
	static final long[] GLOAM = {1036, 200, 1355, 300, 715};

	private static List<Operation> batch(int water, int gloamcap, int eyes, Band band, int stirs, boolean covered) {
		List<Operation> process = new ArrayList<>(List.of(new Operation.Water(water)));
		for (int i = 0; i < gloamcap; i++) {
			process.add(new Operation.Add(GLOAMCAP, RAW));
		}
		for (int i = 0; i < eyes; i++) {
			process.add(new Operation.Add(EYE, RAW));
		}
		for (int i = 0; i < stirs; i++) {
			process.add(new Operation.Stir(band, covered));
		}
		return process;
	}

	private static Mixture replay(List<Operation> process) {
		return ((Mixture.Applied) new Formula(process).replay(Alchemy.catalog())).mixture();
	}

	private static Extract.@Nullable Kind kind(Mixture mixture) {
		Extract.Drawn drawn = Extract.drawn(mixture, Alchemy.catalog());
		return drawn == null ? null : drawn.kind();
	}

	private static CrucibleBlockEntity crucible(GameTestHelper helper, BlockPos at, Block beneath) {
		helper.setBlock(at.below(), beneath);
		helper.setBlock(at, JugcraftConcordance.CRUCIBLE);
		return helper.getBlockEntity(at, CrucibleBlockEntity.class);
	}

	private static ServerPlayer alembist(GameTestHelper helper) {
		ServerPlayer player = TestPlayers.mock(helper);
		player.setGameMode(GameType.SURVIVAL);
		player.getInventory().clearContent();
		ConcordanceProgress.grant(player, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(player, Alchemy.RESEARCH, ResearchState.UNDERSTOOD);
		return player;
	}

	private static InteractionResult use(CrucibleBlockEntity crucible, ServerPlayer player, ItemStack held) {
		RateGate.forget(player.getUUID());
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return crucible.use(player, (ServerLevel) player.level(), held, InteractionHand.MAIN_HAND);
	}

	private static ItemStack written(String text) {
		ItemStack formula = new ItemStack(JugcraftConcordance.FORMULA_ITEM);
		formula.set(JugcraftConcordance.FORMULA, text);
		return formula;
	}

	private static void pour(CrucibleBlockEntity crucible, long droplets) {
		try (Transaction transaction = Transaction.openOuter()) {
			crucible.water.insert(FluidVariant.of(Fluids.WATER), droplets, transaction);
			transaction.commit();
		}
	}

	private static Diagnostic pulse(CrucibleBlockEntity crucible, ServerLevel level, int times) {
		Diagnostic last = null;
		for (int i = 0; i < times; i++) {
			last = crucible.automate(level);
		}
		return last;
	}

	/** A lid on the crucible at {@code at}: an oak trapdoor in the lower half, closed or open. */
	private static void lid(GameTestHelper helper, BlockPos at, boolean closed) {
		helper.setBlock(at.above(), Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.OPEN, !closed));
	}

	/** A crucible over a campfire held hot (120) following {@code formula}, set by {@code player}, with three parts of water,
	 * three Gloamcap, a spider eye and three of {@code container}. */
	private static CrucibleBlockEntity stocked(GameTestHelper helper, BlockPos at, String formula, Item container, ServerPlayer player) {
		CrucibleBlockEntity crucible = crucible(helper, at, Blocks.CAMPFIRE);
		crucible.setTemperature(120);
		helper.assertTrue(use(crucible, player, written(formula)).consumesAction() && crucible.program() != null, "the formula is set: " + formula);
		pour(crucible, FluidConstants.BOTTLE * 3);
		crucible.setItem(0, new ItemStack(Garden.GLOAMCAP, 3));
		crucible.setItem(1, new ItemStack(Items.SPIDER_EYE));
		crucible.setItem(CrucibleBlockEntity.BOTTLE_SLOT, new ItemStack(container, 3));
		return crucible;
	}

	private static int carried(ServerPlayer player, Item item) {
		int count = 0;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			count += stack.is(item) ? stack.getCount() : 0;
		}
		return count;
	}

	private static ItemStack take(ServerPlayer player, Item item) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack.is(item)) {
				player.getInventory().setItem(i, ItemStack.EMPTY);
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	/** The reason a status line gives (its second argument's key), or "" if none. */
	private static String reasonKey(Component line) {
		return line.getContents() instanceof TranslatableContents contents && contents.getArgs().length == 2
				&& contents.getArgs()[1] instanceof Component reason && reason.getContents() instanceof TranslatableContents why ? why.getKey() : "";
	}

	/**
	 * On paper: three Gloamcap and a spider eye stirred hot twice under a lid hold 1.036 Hollow and 0.200 contaminant a
	 * part and draw Gloam Ink; a second spider eye (0.300) or a searing stir makes them murky, Gloam Sludge; one hot stir
	 * leaves 0.715, thin. Uncovered, half covered, without its carrier or its Gloamcap, with sugar or unstirred, no
	 * extract: the same mixture, the Poison draught as ever. Every dose draws the same; rules with no extract draw the
	 * draught; the formula writes and reads its covered stirs.
	 */
	@GameTest(maxTicks = 20)
	public void gloamInkIsDrawnOnlyFromACoveredBatchOfItsOwnIngredients(GameTestHelper helper) {
		AlchemyCatalog catalog = Alchemy.catalog();
		Extract extract = catalog.extracts().get(INK);
		helper.assertTrue(extract != null && extract.item().equals(INK) && extract.failure().equals(SLUDGE) && extract.base().equals(GLOAMCAP)
				&& extract.carriers().equals(List.of(EYE)) && extract.axis() == Axis.HOLLOW && extract.least() == 1000,
				"Gloam Ink: Gloamcap and a spider eye, at least 1.00 Hollow a part: " + extract);
		Mixture ink = replay(batch(3, 3, 1, Band.HOT, 2, true));
		Mixture murky = replay(batch(3, 3, 2, Band.HOT, 2, true));
		Mixture thin = replay(batch(3, 3, 1, Band.HOT, 1, true));
		Mixture seared = replay(batch(3, 3, 1, Band.SEARING, 1, true));
		helper.assertTrue(ink.covered() && ink.concentration().get(Axis.HOLLOW) == GLOAM[0] && ink.contaminantConcentration() == GLOAM[1]
				&& kind(ink) == Extract.Kind.ITEM, "The worked batch draws Gloam Ink: " + ink);
		helper.assertTrue(murky.concentration().get(Axis.HOLLOW) == GLOAM[2] && murky.contaminantConcentration() == GLOAM[3]
				&& kind(murky) == Extract.Kind.FAILURE && kind(seared) == Extract.Kind.FAILURE, "Murky, by an eye or a searing stir: Gloam Sludge");
		helper.assertTrue(thin.concentration().get(Axis.HOLLOW) == GLOAM[4] && kind(thin) == Extract.Kind.THIN, "One hot stir: too thin: " + thin);
		Mixture open = replay(batch(3, 3, 1, Band.HOT, 2, false));
		List<Operation> half = batch(3, 3, 1, Band.HOT, 1, true);
		half.add(new Operation.Stir(Band.HOT));
		List<Operation> sweetened = batch(3, 3, 1, Band.HOT, 2, true);
		sweetened.add(1, new Operation.Add("minecraft:sugar", RAW));
		for (List<Operation> none : List.of(batch(3, 3, 1, Band.HOT, 2, false), half, batch(3, 3, 0, Band.HOT, 2, true),
				batch(3, 0, 2, Band.HOT, 2, true), sweetened, batch(3, 3, 1, Band.HOT, 0, true))) {
			helper.assertTrue(Extract.drawn(replay(none), catalog) == null, "No extract: " + new Formula(none).text());
		}
		helper.assertTrue(!open.covered() && open.outcome(catalog).key().equals("minecraft:poison@0") && open.concentration().equals(ink.concentration())
				&& open.contaminant() == ink.contaminant(), "Uncovered, the same mixture is the Poison draught: " + open.outcome(catalog).key());
		Mixture rest = ink;
		for (int dose = 0; dose < 3; dose++) {
			helper.assertTrue(kind(rest) == Extract.Kind.ITEM, "Dose " + dose + " draws Gloam Ink");
			rest = rest.bottle().rest();
		}
		helper.assertTrue(rest.isEmpty(), "Three parts, three bottles");
		AlchemyCatalog none = new AlchemyCatalog(catalog.ingredients(), catalog.preparations(), catalog.properties(), catalog.products(), Map.of());
		helper.assertTrue(Extract.drawn(ink, none) == null, "Rules with no extract draw the draught from the same batch");
		Formula formula = Formula.parse(GLOAM_INK);
		helper.assertTrue(formula != null && formula.text().equals(GLOAM_INK) && formula.operations().get(5).equals(new Operation.Stir(Band.HOT, true))
				&& formula.replay(catalog) instanceof Mixture.Applied applied && applied.mixture().equals(ink),
				"The formula writes its covered stirs and replays the worked batch: " + formula);
		for (String bad : List.of("water 1; stir hot open", "water 1; stir hot covered covered", "water 1; stir covered hot")) {
			helper.assertTrue(Formula.parse(bad) == null, "Not canonical, not read: " + bad);
		}
		helper.succeed();
	}

	/** A lid is a #jugcraft:crucible_lids block on the crucible with its underside whole; nothing else is (ten cases). */
	@GameTest(maxTicks = 20)
	public void aLidIsAClosedTrapdoorOrASlabOnTheCrucible(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = new BlockPos(2, 2, 2);
		CrucibleBlockEntity crucible = crucible(helper, at, Blocks.STONE);
		BlockState trapdoor = Blocks.OAK_TRAPDOOR.defaultBlockState();
		BlockState slab = Blocks.STONE_SLAB.defaultBlockState();
		record Case(String what, BlockState above, boolean lid) {
		}
		for (Case each : List.of(new Case("nothing", Blocks.AIR.defaultBlockState(), false),
				new Case("a closed trapdoor", trapdoor, true),
				new Case("an open trapdoor", trapdoor.setValue(TrapDoorBlock.OPEN, true), false),
				new Case("a closed trapdoor in the upper half", trapdoor.setValue(TrapDoorBlock.HALF, Half.TOP), false),
				new Case("a closed iron trapdoor", Blocks.IRON_TRAPDOOR.defaultBlockState(), true),
				new Case("a bottom slab", slab, true),
				new Case("a top slab", slab.setValue(SlabBlock.TYPE, SlabType.TOP), false),
				new Case("a double slab", slab.setValue(SlabBlock.TYPE, SlabType.DOUBLE), true),
				new Case("a stone block", Blocks.STONE.defaultBlockState(), false),
				new Case("a hopper", Blocks.HOPPER.defaultBlockState(), false))) {
			helper.setBlock(at.above(), each.above());
			helper.assertTrue(crucible.covered(level) == each.lid(), each.what() + " on the crucible is " + (each.lid() ? "a" : "no") + " lid");
		}
		helper.succeed();
	}

	/**
	 * By hand over a campfire, under a closed trapdoor: a water bucket, three Gloamcap, a spider eye and two stirs make the
	 * worked batch, whose formula writes its covered stirs; three glass bottles draw three Gloam Ink, practised.
	 */
	@GameTest(maxTicks = 100)
	public void gloamInkByHandUnderALid(GameTestHelper helper) {
		BlockPos at = new BlockPos(2, 2, 2);
		CrucibleBlockEntity crucible = crucible(helper, at, Blocks.CAMPFIRE);
		crucible.setTemperature(120);
		lid(helper, at, true);
		ServerPlayer player = alembist(helper);
		use(crucible, player, new ItemStack(Items.WATER_BUCKET));
		for (int i = 0; i < 3; i++) {
			use(crucible, player, new ItemStack(Garden.GLOAMCAP));
		}
		use(crucible, player, new ItemStack(Items.SPIDER_EYE));
		helper.assertTrue(use(crucible, player, new ItemStack(Items.STICK)).consumesAction()
				&& crucible.mixture().history().getLast().equals(new Operation.Stir(Band.HOT, true)), "Stirred under the lid: " + crucible.mixture().history());
		helper.runAfterDelay(NEXT_STIR, () -> {
			use(crucible, player, new ItemStack(Items.STICK));
			Mixture made = crucible.mixture();
			helper.assertTrue(made.equals(replay(Formula.parse(GLOAM_INK).operations())), "By hand, the worked batch: " + made);
			use(crucible, player, new ItemStack(JugcraftConcordance.FORMULA_ITEM));
			helper.assertValueEqual(take(player, JugcraftConcordance.FORMULA_ITEM).get(JugcraftConcordance.FORMULA), GLOAM_INK,
					"the recorded formula, its covered stirs written");
			// One stack of three bottles, held through each use: a product placed back lands beside it and the next merge in
			// (26.3's Inventory skips an empty selected slot, so a fresh stack each time would overwrite the last product).
			ItemStack bottles = new ItemStack(Items.GLASS_BOTTLE, 3);
			for (int i = 0; i < 3; i++) {
				helper.assertTrue(use(crucible, player, bottles).consumesAction(), "A glass bottle draws dose " + i);
			}
			helper.assertTrue(carried(player, JugcraftConcordance.GLOAM_INK) == 3 && carried(player, JugcraftConcordance.DRAUGHT) == 0
					&& crucible.mixture().isEmpty(), "Three Gloam Ink, no draught, the crucible empty");
			helper.assertTrue(ConcordanceProgress.knowledge(player).progress(Alchemy.RESEARCH).evidence().containsKey("practice:jugcraft:alchemy:" + INK),
					"Gloam Ink is an outcome of its own, practised");
			helper.succeed();
		});
	}

	/**
	 * A second spider eye: anyone's spoon finds it murky and anyone's risk line names Gloam Sludge, a master's glass says
	 * why; three glass bottles draw three Gloam Sludge, practised as nothing; a furnace boils one back to its glass bottle;
	 * no crucible takes it.
	 */
	@GameTest(maxTicks = 100)
	public void aMurkyBatchBottlesSludgeAndSaysSoBeforehand(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		AlchemyCatalog catalog = Alchemy.catalog();
		BlockPos at = new BlockPos(2, 2, 2);
		CrucibleBlockEntity crucible = crucible(helper, at, Blocks.CAMPFIRE);
		crucible.setTemperature(120);
		lid(helper, at, true);
		ServerPlayer player = alembist(helper);
		use(crucible, player, new ItemStack(Items.WATER_BUCKET));
		for (int i = 0; i < 3; i++) {
			use(crucible, player, new ItemStack(Garden.GLOAMCAP));
		}
		use(crucible, player, new ItemStack(Items.SPIDER_EYE));
		use(crucible, player, new ItemStack(Items.SPIDER_EYE));
		use(crucible, player, new ItemStack(Items.STICK));
		helper.runAfterDelay(NEXT_STIR, () -> {
			use(crucible, player, new ItemStack(Items.STICK));
			Mixture murky = crucible.mixture();
			helper.assertTrue(murky.equals(replay(batch(3, 3, 2, Band.HOT, 2, true))), "The murky batch: " + murky);
			helper.assertTrue(Assay.read(murky, crucible.temperature(), Assay.SPOON, catalog).contains(Text.of("alchemy.assay.murky")), "A spoon finds it murky");
			List<Text> risks = Assay.risks(null, murky, false, Alchemy::harmfulStatus, catalog);
			helper.assertTrue(risks.contains(Text.of("alchemy.risk.failure", new Text.Ref("item", SLUDGE))), "Anyone is told it draws Gloam Sludge: " + risks);
			List<Text> master = Assay.read(murky, crucible.temperature(), Assay.MASTERED, catalog);
			helper.assertTrue(master.contains(Text.of("alchemy.extract.murky", new Text.Ref("item", SLUDGE), new Text.Ref("item", INK), "0.30", "0.25")),
					"A master's glass says why: " + master);
			ItemStack bottles = new ItemStack(Items.GLASS_BOTTLE, 3);
			for (int i = 0; i < 3; i++) {
				use(crucible, player, bottles);
			}
			helper.assertTrue(carried(player, JugcraftConcordance.GLOAM_SLUDGE) == 3 && carried(player, JugcraftConcordance.GLOAM_INK) == 0
					&& crucible.mixture().isEmpty(), "Three Gloam Sludge and no ink");
			var evidence = ConcordanceProgress.knowledge(player).progress(Alchemy.RESEARCH).evidence();
			helper.assertTrue(!evidence.containsKey("practice:jugcraft:alchemy:" + INK) && !evidence.containsKey("practice:jugcraft:alchemy:" + SLUDGE),
					"Sludge is practised as nothing: " + evidence.keySet());
			SingleRecipeInput sludge = new SingleRecipeInput(new ItemStack(JugcraftConcordance.GLOAM_SLUDGE));
			ItemStack boiled = level.getServer().getRecipeManager().getRecipeFor(RecipeType.SMELTING, sludge, level)
					.orElseThrow(() -> helper.assertionException("A furnace does not boil Gloam Sludge")).value().assemble(sludge);
			helper.assertTrue(boiled.is(Items.GLASS_BOTTLE) && boiled.getCount() == 1, "Boiled dry, it gives back its bottle and no more: " + boiled);
			helper.assertTrue(!crucible.canPlaceItem(0, new ItemStack(JugcraftConcordance.GLOAM_SLUDGE))
					&& use(crucible, player, new ItemStack(JugcraftConcordance.GLOAM_SLUDGE)) == InteractionResult.TRY_WITH_EMPTY_HAND, "It feeds no crucible");
			helper.succeed();
		});
	}

	/**
	 * Under a lid, an uncovered formula's stirs are taken as written and bottle the Poison draught; a covered formula
	 * stirred once is thin and bottles the draught; a bowl under the lid draws a salve, never ink.
	 */
	@GameTest(maxTicks = 20)
	public void anUncoveredOrThinBatchIsTheDraughtAsBefore(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = alembist(helper);
		String uncovered = GLOAM_INK.replace(" covered", "");
		String once = GLOAM_INK.substring(0, GLOAM_INK.lastIndexOf("; stir hot covered"));
		BlockPos[] at = {new BlockPos(1, 2, 1), new BlockPos(4, 2, 1), new BlockPos(1, 2, 4)};
		CrucibleBlockEntity open = stocked(helper, at[0], uncovered, Items.GLASS_BOTTLE, player);
		CrucibleBlockEntity thin = stocked(helper, at[1], once, Items.GLASS_BOTTLE, player);
		CrucibleBlockEntity bowl = stocked(helper, at[2], GLOAM_INK, Items.BOWL, player);
		for (BlockPos each : at) {
			lid(helper, each, true);
		}
		helper.assertTrue(pulse(open, level, 10) == null && pulse(thin, level, 9) == null && pulse(bowl, level, 10) == null,
				"Each made and bottled its three doses: " + open.waiting() + " / " + thin.waiting() + " / " + bowl.waiting());
		ItemStack poison = Alchemy.brew(JugcraftConcordance.DRAUGHT, replay(Formula.parse(uncovered).operations()).outcome(Alchemy.catalog()));
		ItemStack weak = Alchemy.brew(JugcraftConcordance.DRAUGHT, replay(Formula.parse(once).operations()).outcome(Alchemy.catalog()));
		ItemStack fromOpen = open.getItem(CrucibleBlockEntity.OUTPUT_SLOT);
		ItemStack fromThin = thin.getItem(CrucibleBlockEntity.OUTPUT_SLOT);
		helper.assertTrue(fromOpen.getCount() == 3 && ItemStack.isSameItemSameComponents(fromOpen, poison)
				&& "minecraft:poison@0".equals(poison.get(JugcraftConcordance.BREW).outcome()), "Three Poison draughts: " + fromOpen);
		helper.assertTrue(fromThin.getCount() == 3 && ItemStack.isSameItemSameComponents(fromThin, weak), "Three thin draughts: " + fromThin);
		helper.assertTrue(bowl.getItem(CrucibleBlockEntity.OUTPUT_SLOT).getCount() == 3 && bowl.getItem(CrucibleBlockEntity.OUTPUT_SLOT).is(JugcraftConcordance.SALVE),
				"A bowl draws a salve, never ink");
		helper.succeed();
	}

	/**
	 * Following the worked formula over a campfire: water and four ingredients, then its covered stir waits for its lid
	 * (needs_cover, one WANT when the wait begins; the status line says why), nothing taken;
	 * lidded, the stir is taken; opened, the next waits again; closed, it finishes three Gloam Ink from exactly its
	 * Gloamcap, spider eye, water and bottles.
	 */
	@GameTest(maxTicks = 20)
	public void aCoveredFormulaWaitsForItsLidAndSaysSo(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = new BlockPos(2, 2, 2);
		SignWatch signs = new SignWatch(helper);
		CrucibleBlockEntity crucible = stocked(helper, at, GLOAM_INK, Items.GLASS_BOTTLE, alembist(helper));
		helper.assertTrue(pulse(crucible, level, 5) == null && crucible.step() == 5, "Water and the four ingredients: step " + crucible.step());
		Mixture before = crucible.mixture();
		Diagnostic noLid = Diagnostic.of(Code.NEEDS_COVER);
		Diagnostic first = crucible.automate(level);
		Diagnostic second = crucible.automate(level);
		helper.assertTrue(noLid.equals(first) && noLid.equals(second) && noLid.equals(crucible.waiting()), "It waits for its lid: " + first + " / " + second);
		helper.assertTrue(crucible.step() == 5 && crucible.mixture().equals(before), "No stir is taken without it");
		// The Peepo port (local/peepo/CrucibleCompanion.status) has no arm for needs_cover in this change: it answers its default,
		// IDLE. Mapping it to MACHINE_PAUSED is a change in the Peepo package, left for that package's owner.
		helper.assertValueEqual(reasonKey(crucible.statusLine(ConcordanceProgress.now(level))), "compose.jugcraft.diagnostic.needs_cover",
				"the status line says why");
		helper.assertValueEqual(signs.at(Sign.WANT, at), 1L, "a want, once, when the wait began");
		lid(helper, at, true);
		helper.assertTrue(crucible.automate(level) == null && crucible.step() == 6
				&& crucible.mixture().history().getLast().equals(new Operation.Stir(Band.HOT, true)), "Lidded, the covered stir is taken");
		lid(helper, at, false);
		helper.assertTrue(noLid.equals(crucible.automate(level)) && crucible.step() == 6, "Opened, the next covered stir waits again");
		lid(helper, at, true);
		helper.assertTrue(pulse(crucible, level, 4) == null && crucible.step() == 0 && crucible.mixture().isEmpty(), "Closed: its stir, then three bottles");
		ItemStack output = crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT);
		helper.assertTrue(ItemStack.isSameItemSameComponents(output, new ItemStack(JugcraftConcordance.GLOAM_INK)) && output.getCount() == 3
				&& crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT).isEmpty() && crucible.getItem(0).isEmpty() && crucible.getItem(1).isEmpty()
				&& crucible.water.amount == 0, "Three Gloam Ink from exactly its Gloamcap, spider eye, water and bottles: " + output);
		helper.assertValueEqual(signs.at(Sign.WANT, at), 2L, "a want each time the wait began");
		helper.assertValueEqual(signs.at(Sign.DONE, at), 3L, "three bottles filled");
		signs.close();
		helper.succeed();
	}

	/**
	 * On a Crucible Stand: without its lid the crucible waits (needs_cover) and the stand idles, saying why, paying nothing;
	 * lidded, it waits for hot and the stand aims hot; fed by a creative cell it heats into hot, never past its target, and
	 * the formula finishes three Gloam Ink unattended, then waits for water while the stand gives nothing.
	 */
	@GameTest(maxTicks = 900)
	public void aStandHeatsOnlyForACoveredStirItCanTake(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = new BlockPos(2, 2, 2);
		helper.setBlock(at.below(), JugcraftConcordance.CRUCIBLE_STAND);
		helper.setBlock(at, JugcraftConcordance.CRUCIBLE);
		CrucibleStandBlockEntity stand = helper.getBlockEntity(at.below(), CrucibleStandBlockEntity.class);
		CrucibleBlockEntity crucible = helper.getBlockEntity(at, CrucibleBlockEntity.class);
		helper.assertTrue(use(crucible, alembist(helper), written(GLOAM_INK)).consumesAction(), "The formula is set");
		pour(crucible, FluidConstants.BOTTLE * 3);
		crucible.setItem(0, new ItemStack(Garden.GLOAMCAP, 3));
		crucible.setItem(1, new ItemStack(Items.SPIDER_EYE));
		crucible.setItem(CrucibleBlockEntity.BOTTLE_SLOT, new ItemStack(Items.GLASS_BOTTLE, 3));
		helper.assertTrue(pulse(crucible, level, 5) == null && crucible.step() == 5 && crucible.temperature() == Heat.AMBIENT, "In, still cold");
		Diagnostic noLid = crucible.automate(level);
		CrucibleStandBlockEntity.Plan plan = stand.plan(level);
		long held = stand.energy.getAmount();
		helper.assertTrue(Diagnostic.of(Code.NEEDS_COVER).equals(noLid) && !plan.heats() && plan.waits() && noLid.equals(plan.reason()) && plan.band() == null,
				"Without its lid the stand idles, saying why: " + plan);
		helper.assertTrue(!stand.heat(level) && stand.energy.getAmount() == held && stand.given() == Heat.AMBIENT, "and pays nothing");
		helper.setBlock(at.below().west(), CreativeEnergyCellBlock.BLOCK);
		lid(helper, at, true);
		Diagnostic cold = crucible.automate(level);
		helper.assertTrue(Diagnostic.wrongHeat(Band.HOT.id, Band.COLD.id).equals(cold) && stand.plan(level).band() == Band.HOT,
				"Lidded, it waits for hot and the stand aims hot: " + cold + ", " + stand.plan(level));
		int[] hottest = {Heat.AMBIENT};
		helper.onEachTick(() -> hottest[0] = Math.max(hottest[0], crucible.temperature()));
		helper.succeedWhen(() -> {
			ItemStack made = crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT);
			helper.assertTrue(made.is(JugcraftConcordance.GLOAM_INK) && made.getCount() == 3, "Waiting for three Gloam Ink: " + made
					+ " (step " + crucible.step() + ", " + crucible.temperature() + " degrees, " + crucible.waiting() + ")");
			helper.assertTrue(Diagnostic.needsWater(3, 0).equals(crucible.waiting()) && stand.given() == Heat.AMBIENT, "Then it waits for water, unheated");
			helper.assertTrue(hottest[0] >= Band.HOT.from && hottest[0] <= Heat.STAND_HOT, "Heated into hot, never past its target: " + hottest[0]);
		});
	}

	/**
	 * The supply job as built: the formula's shortage is three Gloamcap first (the Peepo port asks for them, not the eye),
	 * then its one spider eye, then nothing; the four go in as four steps.
	 */
	@GameTest(maxTicks = 20)
	public void aGloamInkFormulaAsksForGloamcapThenItsCarrier(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		CrucibleBlockEntity crucible = crucible(helper, new BlockPos(2, 2, 2), Blocks.CAMPFIRE);
		crucible.setTemperature(120);
		helper.assertTrue(use(crucible, alembist(helper), written(GLOAM_INK)).consumesAction(), "The formula is set");
		pour(crucible, FluidConstants.BOTTLE * 3);
		Operation.Add gloamcap = new Operation.Add(GLOAMCAP, RAW);
		Operation.Add eye = new Operation.Add(EYE, RAW);
		helper.assertTrue(crucible.automate(level) == null && crucible.step() == 1, "It pours its water");
		Diagnostic lacking = crucible.automate(level);
		helper.assertTrue(Diagnostic.missingIngredient(1, gloamcap.describe()).equals(lacking) && new CrucibleBlockEntity.Shortage(gloamcap, 3).equals(crucible.shortage())
				&& crucible.companion.needed(new ItemStack(Garden.GLOAMCAP, 3)) == 3 && crucible.companion.needed(new ItemStack(Items.SPIDER_EYE)) == 0
				&& crucible.companion.status() == CompanionStatus.NO_INPUT, "Three Gloamcap first: " + lacking + ", " + crucible.shortage());
		crucible.setItem(0, new ItemStack(Garden.GLOAMCAP, 3));
		helper.assertTrue(new CrucibleBlockEntity.Shortage(eye, 1).equals(crucible.shortage()) && crucible.companion.needed(new ItemStack(Items.SPIDER_EYE)) == 1
				&& crucible.companion.needed(new ItemStack(Garden.GLOAMCAP)) == 0, "then its carrier: " + crucible.shortage());
		crucible.setItem(1, new ItemStack(Items.SPIDER_EYE));
		helper.assertTrue(crucible.shortage() == null && pulse(crucible, level, 4) == null && crucible.step() == 5, "then nothing, and all four go in");
		helper.succeed();
	}

	/** A full stack of ink pauses it and spends nothing; sludge in its output is another thing; one more fills the stack. */
	@GameTest(maxTicks = 20)
	public void aFullOutputOfInkPausesAndSludgeIsAnotherThing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = new BlockPos(2, 2, 2);
		CrucibleBlockEntity crucible = stocked(helper, at, GLOAM_INK, Items.GLASS_BOTTLE, alembist(helper));
		lid(helper, at, true);
		helper.assertTrue(pulse(crucible, level, 7) == null && crucible.step() == 7, "Made to its end under the lid");
		Mixture made = crucible.mixture();
		crucible.setItem(CrucibleBlockEntity.OUTPUT_SLOT, new ItemStack(JugcraftConcordance.GLOAM_INK, 16));
		helper.assertTrue(Diagnostic.of(Code.INSUFFICIENT_CAPACITY).equals(crucible.automate(level)) && crucible.mixture().equals(made)
				&& crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT).getCount() == 3, "A full stack of ink pauses it, nothing spent");
		crucible.setItem(CrucibleBlockEntity.OUTPUT_SLOT, new ItemStack(JugcraftConcordance.GLOAM_SLUDGE));
		helper.assertTrue(Diagnostic.of(Code.OUTPUT_DIFFERS).equals(crucible.automate(level)) && crucible.mixture().equals(made)
				&& crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT).is(JugcraftConcordance.GLOAM_SLUDGE), "Sludge in its output is another thing: kept, paused");
		crucible.setItem(CrucibleBlockEntity.OUTPUT_SLOT, new ItemStack(JugcraftConcordance.GLOAM_INK, 15));
		helper.assertTrue(crucible.automate(level) == null && crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT).getCount() == 16, "One more fills the stack");
		helper.assertTrue(Diagnostic.of(Code.INSUFFICIENT_CAPACITY).equals(crucible.automate(level)), "then it pauses, full");
		helper.succeed();
	}

	/** Saved after one covered stir: its history and formula say covered, read back so, and it finishes three Gloam Ink. */
	@GameTest(maxTicks = 20)
	public void aCoveredBatchAndItsFormulaSurviveASave(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = new BlockPos(2, 2, 2);
		CrucibleBlockEntity crucible = stocked(helper, at, GLOAM_INK, Items.GLASS_BOTTLE, alembist(helper));
		lid(helper, at, true);
		helper.assertTrue(pulse(crucible, level, 6) == null && crucible.step() == 6, "One covered stir taken");
		Mixture before = crucible.mixture();
		CompoundTag saved = crucible.saveWithoutMetadata(level.registryAccess());
		helper.assertTrue(saved.toString().contains("stir hot covered") && GLOAM_INK.equals(saved.getStringOr("program", "")) && saved.getIntOr("step", -1) == 6,
				"Saved with its covered stir and formula: " + saved);
		CrucibleBlockEntity copy = new CrucibleBlockEntity(crucible.getBlockPos(), crucible.getBlockState());
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		helper.assertTrue(copy.mixture().equals(before) && copy.mixture().covered() && copy.program() != null && GLOAM_INK.equals(copy.program().text())
				&& copy.step() == 6, "Read back covered, at its step: " + copy.mixture());
		crucible.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		helper.assertTrue(pulse(crucible, level, 4) == null && crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT).getCount() == 3
				&& crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT).is(JugcraftConcordance.GLOAM_INK), "Restarted, it finishes three Gloam Ink");
		helper.succeed();
	}

	/**
	 * Jade is told the lid (only when there is one) and the crucible's own reason; the Inspect page shows anyone the lid and
	 * the covered step, and a master's preview what a glass bottle draws; a murky formula's risk names Gloam Sludge; a
	 * client's update tag is the heat, volume and look as ever.
	 */
	@GameTest(maxTicks = 20)
	public void theLidAndItsInkAreShownButNeverTheMakeup(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		AlchemyCatalog catalog = Alchemy.catalog();
		BlockPos at = new BlockPos(2, 2, 2);
		CrucibleBlockEntity crucible = stocked(helper, at, GLOAM_INK, Items.GLASS_BOTTLE, alembist(helper));
		helper.assertTrue(Diagnostic.of(Code.NEEDS_COVER).equals(pulse(crucible, level, 6)) && crucible.step() == 5, "It waits for its lid at step 6");
		Set<String> allowed = Set.of("temperature", "band", "held", "parts", "tank", "step", "steps", "next", "reason", "covered");
		if (FabricLoader.getInstance().isModLoaded("jade")) {
			CompoundTag open = ConcordanceDataProvider.snapshot(crucible, level.getGameTime());
			helper.assertTrue(!open.contains("covered") && Diagnostic.of(Code.NEEDS_COVER).encode().equals(open.getStringOr("reason", ""))
					&& allowed.containsAll(open.keySet()), "Jade: no lid, and why it waits: " + open);
		}
		lid(helper, at, true);
		if (FabricLoader.getInstance().isModLoaded("jade")) {
			CompoundTag lidded = ConcordanceDataProvider.snapshot(crucible, level.getGameTime());
			helper.assertTrue(lidded.getBooleanOr("covered", false) && allowed.containsAll(lidded.keySet()), "Jade: under a lid, and no makeup: " + lidded);
		}
		CrucibleInspection.State state = new CrucibleInspection.State(crucible.temperature(), crucible.below(level), 0, crucible.mixture(),
				crucible.tankParts(), CrucibleBlockEntity.WATER_PER_BUCKET, crucible.program(), crucible.step(), null, false, crucible.covered(level));
		List<Text> seen = CrucibleInspection.read(state, Assay.SPOON, Alchemy::harmfulStatus, catalog).getFirst().lines();
		helper.assertTrue(seen.contains(Text.of("alchemy.inspect.covered"))
				&& seen.contains(Text.of("alchemy.inspect.step", 6, 7, new Operation.Stir(Band.HOT, true).describe())), "Anyone sees the lid and the covered stir: " + seen);
		List<Text> preview = CrucibleInspection.read(state, Assay.MASTERED, Alchemy::harmfulStatus, catalog).getLast().lines();
		helper.assertTrue(preview.contains(Text.of("alchemy.extract.drawn", new Text.Ref("item", INK), new Text.Ref("principle", "hollow"), "1.04", "1.00")),
				"A master's preview: a glass bottle draws Gloam Ink: " + preview);
		Formula murky = new Formula(batch(3, 3, 2, Band.HOT, 2, true));
		helper.assertTrue(Assay.risks(murky, Mixture.EMPTY, false, Alchemy::harmfulStatus, catalog).contains(Text.of("alchemy.risk.failure", new Text.Ref("item", SLUDGE))),
				"A murky formula's risk names Gloam Sludge, to anyone");
		helper.assertValueEqual(crucible.getUpdateTag(level.registryAccess()).keySet(), Set.of("temperature", "parts", "taste", "murky"), "the update tag is unchanged");
		helper.succeed();
	}

	/** The extract parses strictly; an extract of ingredients the rules do not know is left out and said. */
	@GameTest(maxTicks = 20)
	public void theExtractParsesStrictly(GameTestHelper helper) {
		String good = "{\"schema\": 1, \"item\": \"jugcraft:gloam_ink\", \"failure\": \"jugcraft:gloam_sludge\", \"base\": \"jugcraft:gloamcap\", "
				+ "\"carriers\": [\"minecraft:spider_eye\"], \"axis\": \"hollow\", \"least\": 1.0, \"codex\": \"alembic/gloam_ink\"}";
		helper.assertValueEqual(new RulesParser().extract(INK, JsonParser.parseString(good)), Alchemy.catalog().extracts().get(INK), "the data's extract");
		for (String bad : List.of(good.replace("\"least\": 1.0", "\"least\": 0.0"),
				good.replace("[\"minecraft:spider_eye\"]", "[]"),
				good.replace("[\"minecraft:spider_eye\"]", "[\"jugcraft:gloamcap\"]"),
				good.replace("[\"minecraft:spider_eye\"]", "[\"minecraft:spider_eye\", \"minecraft:spider_eye\"]"),
				good.replace("\"hollow\"", "\"gloom\""),
				good.replace("\"failure\": \"jugcraft:gloam_sludge\", ", ""),
				good.replace("\"jugcraft:gloam_sludge\"", "\"jugcraft:gloam_ink\""),
				good.replace("\"alembic/gloam_ink\"", "\"gloam ink\""),
				good.replace("}", ", \"colour\": \"black\"}"))) {
			RulesParser strict = new RulesParser();
			helper.assertTrue(strict.extract("test:bad", JsonParser.parseString(bad)) == null && strict.problems().size() == 1
					&& strict.problems().getFirst().startsWith("extract test:bad: "), "Refused with its reason: " + bad + " -> " + strict.problems());
		}
		ConcordanceRules alone = ConcordanceRules.build(List.of(new ConcordanceRules.Source("extract", "test:ink", JsonParser.parseString(good))));
		helper.assertTrue(alone.alchemy().extracts().isEmpty()
				&& alone.problems().stream().anyMatch(p -> p.equals("extract test:ink: jugcraft:gloamcap is not an alchemical ingredient")),
				"Left out and said: " + alone.problems());
		helper.succeed();
	}
}
