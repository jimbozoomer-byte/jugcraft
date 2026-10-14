package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.concordance.Alchemy;
import io.github.jimbozoomer.jugcraft.concordance.Brew;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.Reagent;
import io.github.jimbozoomer.jugcraft.concordance.Saved;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.AlchemyCatalog;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Assay;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Axis;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Band;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Formula;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Heat;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Mixture;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Operation;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Outcome;
import io.github.jimbozoomer.jugcraft.concordance.compose.Text;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Code;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Diagnostic;
import io.github.jimbozoomer.jugcraft.concordance.rime.Frostward;
import io.github.jimbozoomer.jugcraft.concordance.rules.ConcordanceRules;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.sign.Sign;
import io.github.jimbozoomer.jugcraft.lair.yeti.JugcraftYeti;
import io.github.jimbozoomer.jugcraft.logistics.JugcraftLogistics;
import io.github.jimbozoomer.jugcraft.logistics.PneumaticExtractorBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import local.peepo.CompanionStatus;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;

/**
 * Roadmap step 13, experimental alchemy: a crucible takes its heat from the block beneath; the same process in two
 * crucibles makes the same mixture and the same draught, whose effect follows from what was put in; only someone who
 * understands the Alembic Arts may work one; the mortar prepares ingredients; a bowl makes a salve for a creature; a
 * recorded formula is followed by another crucible fed by pipe and hopper, making the same draught; real machinery (a
 * hopper, a campfire and a Pneumatic Extractor) runs a formula twice; a broken crucible drops its items; and a crucible
 * saves its mixture and formula. Since Concordance Chunk B (B-08), Winter Brine: Rime brews Frostward only stirred cold,
 * bottles made before keep their Resistance, a Rime formula written before brews Frostward, and nothing makes Frostward
 * cheaper than its snow. Since Concordance Chunk C (C-01), the crucible says why it waits: a formula the rules in force
 * refuse, or a saved one that no longer reads, is kept, names its step and drives nothing until the rules can follow it
 * again; a full output pauses and voids nothing; the Peepo port reads the crucible's own reason; and only someone who
 * could change the block by hand works it.
 */
public class ConcordanceAlchemyGameTests {
	private static final String GLOW = "minecraft:glowstone_dust";
	private static final String SNOW = "minecraft:snowball";
	/** A stir may follow another only after this many ticks (CrucibleBlockEntity.STIR_TICKS, plus one). */
	private static final int NEXT_STIR = CrucibleBlockEntity.STIR_TICKS + 1;

	private static CrucibleBlockEntity crucible(GameTestHelper helper, BlockPos at, boolean campfire) {
		helper.setBlock(at.below(), campfire ? Blocks.CAMPFIRE : Blocks.STONE);
		helper.setBlock(at, JugcraftConcordance.CRUCIBLE);
		return (CrucibleBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
	}

	private static ServerPlayer alembist(GameTestHelper helper, ResearchState state) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		ConcordanceProgress.grant(player, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		if (state != ResearchState.NONE) {
			ConcordanceProgress.grant(player, Alchemy.RESEARCH, state);
		}
		return player;
	}

	private static InteractionResult use(CrucibleBlockEntity crucible, ServerPlayer player, ItemStack held) {
		RateGate.forget(player.getUUID());
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return crucible.use(player, (ServerLevel) player.level(), held, InteractionHand.MAIN_HAND);
	}

	private static ItemStack waterBottle() {
		return PotionContents.createItemStack(Items.POTION, Potions.WATER);
	}

	private static CrucibleBlockEntity crucible(GameTestHelper helper, BlockPos at, Block beneath) {
		helper.setBlock(at.below(), beneath);
		helper.setBlock(at, JugcraftConcordance.CRUCIBLE);
		return (CrucibleBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
	}

	/**
	 * Winter Brine's process (tools/concordance_alchemy.py WINTER_BRINE): three snowballs as they come in {@code water}
	 * parts, stirred three times at {@code band}.
	 */
	private static Mixture threeSnowballs(int water, Band band) {
		List<Operation> process = new ArrayList<>(List.of(new Operation.Water(water)));
		for (int i = 0; i < 3; i++) {
			process.add(new Operation.Add(SNOW, "jugcraft:raw"));
		}
		for (int i = 0; i < 3; i++) {
			process.add(new Operation.Stir(band));
		}
		return ((Mixture.Applied) new Formula(process).replay(Alchemy.catalog())).mixture();
	}

	/** What the first bottle drawn from {@code mixture} would do, as the crucible bottles it. */
	private static Outcome bottled(Mixture mixture) {
		Mixture.Bottled bottled = mixture.bottle();
		return Outcome.of(bottled.dose(), bottled.contaminant(), Alchemy.catalog(), mixture.history());
	}

	/** Three snowballs in a bottle of water, then a stir: by hand, as an alembist does it. */
	private static void startWinterBrine(CrucibleBlockEntity crucible, ServerPlayer player) {
		use(crucible, player, waterBottle());
		for (int i = 0; i < 3; i++) {
			use(crucible, player, new ItemStack(Items.SNOWBALL));
		}
		use(crucible, player, new ItemStack(Items.STICK));
	}

	/** Water, one glowstone dust as it comes, and the crucible held hot: then two stirs, a little apart. */
	private static void startNightEye(GameTestHelper helper, CrucibleBlockEntity crucible, ServerPlayer player) {
		crucible.setTemperature(120);
		helper.assertTrue(use(crucible, player, waterBottle()).consumesAction() && crucible.mixture().parts() == 1, "A bottle of water is one part");
		helper.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE), "The empty bottle is handed back");
		use(crucible, player, new ItemStack(Items.GLOWSTONE_DUST));
		helper.assertTrue(crucible.mixture().pending().get(Axis.RADIANCE) == 1500, "Glowstone goes in undissolved");
		use(crucible, player, new ItemStack(Items.STICK));
	}

	/** The worked example as a formula: Night Vision I for 1435 ticks (1.196 Radiance a part). */
	private static final String NIGHT_EYE = "water 1; add minecraft:glowstone_dust jugcraft:raw; stir hot; stir hot";
	/** The same in two parts with two glowstone dust: two doses, each the worked example's. */
	private static final String TWO_NIGHT_EYES = "water 2; add minecraft:glowstone_dust jugcraft:raw; add minecraft:glowstone_dust jugcraft:raw; stir hot; stir hot";
	/** A spider eye stirred hot twice: Poison I (0.957 Hollow a part), 0.30 contaminant, under Nausea's 0.50. */
	private static final String SPIDER = "water 1; add minecraft:spider_eye jugcraft:raw; stir hot; stir hot";

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

	/** The draught a formula's first dose makes, worked out on paper. */
	private static ItemStack draught(String formula) {
		return Alchemy.brew(JugcraftConcordance.DRAUGHT, bottled(((Mixture.Applied) Formula.parse(formula).replay(Alchemy.catalog())).mixture()));
	}

	/** {@code times} pulses of its formula, at once; the last one's reason. */
	private static Diagnostic pulse(CrucibleBlockEntity crucible, ServerLevel level, int times) {
		Diagnostic last = null;
		for (int i = 0; i < times; i++) {
			last = crucible.automate(level);
		}
		return last;
	}

	/** A Fire Resistance draught: another brew than any formula here makes. */
	private static ItemStack otherBrew(int count) {
		ItemStack other = new ItemStack(JugcraftConcordance.DRAUGHT, count);
		other.set(JugcraftConcordance.BREW, new Brew(List.of(new Brew.Dose("minecraft:fire_resistance", 0, 1200, false)), "minecraft:fire_resistance@0"));
		return other;
	}

	/** The reason a status line gives (its second argument's key), or "" if none. */
	private static String reasonKey(Component line) {
		return line.getContents() instanceof TranslatableContents contents && contents.getArgs().length == 2
				&& contents.getArgs()[1] instanceof Component reason && reason.getContents() instanceof TranslatableContents why ? why.getKey() : "";
	}

	/** Step 2 of NIGHT_EYE refused under rules that know no glowstone (the empty rules stand for that reload). */
	private static Diagnostic glowstoneRefused() {
		return Diagnostic.incompatible(2, new Operation.Add(GLOW, "jugcraft:raw").describe(), Text.of("alchemy.not_ingredient", new Text.Ref("item", GLOW)));
	}

	/**
	 * {@code during} under {@code rules}, at once, the rules put back whatever happens (C-P28): ConcordanceData's rules are
	 * every test's, and the server runs other tests, so they are never left swapped past this call (check_diagnostics).
	 */
	private static <T> T under(ConcordanceRules rules, Supplier<T> during) {
		ConcordanceRules was = ConcordanceData.rules();
		ConcordanceData.replace(rules);
		try {
			return during.get();
		} finally {
			ConcordanceData.replace(was);
		}
	}

	/** A crucible over a campfire, held hot (120), following {@code formula} set by {@code player}. */
	private static CrucibleBlockEntity following(GameTestHelper helper, BlockPos at, String formula, ServerPlayer player) {
		CrucibleBlockEntity crucible = crucible(helper, at, true);
		crucible.setTemperature(120);
		helper.assertTrue(use(crucible, player, written(formula)).consumesAction() && crucible.program() != null, "the formula is set: " + formula);
		return crucible;
	}

	/** NIGHT_EYE followed with a bottle of water, its glowstone and a glass bottle at hand, its water already poured (step 1). */
	private static CrucibleBlockEntity pouredNightEye(GameTestHelper helper, BlockPos at) {
		CrucibleBlockEntity crucible = following(helper, at, NIGHT_EYE, alembist(helper, ResearchState.UNDERSTOOD));
		pour(crucible, FluidConstants.BOTTLE);
		crucible.setItem(0, new ItemStack(Items.GLOWSTONE_DUST));
		crucible.setItem(CrucibleBlockEntity.BOTTLE_SLOT, new ItemStack(Items.GLASS_BOTTLE));
		helper.assertTrue(crucible.automate(helper.getLevel()) == null && crucible.step() == 1, "The first pulse pours the water: step " + crucible.step());
		return crucible;
	}

	/** TWO_NIGHT_EYES followed to its end with all it needs: two parts made, nothing bottled yet (step 5). */
	private static CrucibleBlockEntity twoNightEyesMade(GameTestHelper helper, BlockPos at) {
		CrucibleBlockEntity crucible = following(helper, at, TWO_NIGHT_EYES, alembist(helper, ResearchState.UNDERSTOOD));
		pour(crucible, FluidConstants.BOTTLE * 2);
		crucible.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, 2));
		crucible.setItem(CrucibleBlockEntity.BOTTLE_SLOT, new ItemStack(Items.GLASS_BOTTLE, 2));
		helper.assertTrue(pulse(crucible, helper.getLevel(), 5) == null && crucible.step() == 5 && crucible.mixture().parts() == 2,
				"Five pulses make both parts: step " + crucible.step() + ", " + crucible.mixture());
		return crucible;
	}

	// ---------------------------------------------------------------- heat

	/** A lit campfire heats a crucible one degree a tick to 120 (hot); with nothing beneath it cools to the air's 20. */
	@GameTest(maxTicks = 160)
	public void heatComesFromBeneath(GameTestHelper helper) {
		CrucibleBlockEntity heated = crucible(helper, new BlockPos(1, 2, 1), true);
		CrucibleBlockEntity cold = crucible(helper, new BlockPos(4, 2, 1), false);
		cold.setTemperature(100);
		helper.runAfterDelay(40, () -> helper.assertTrue(heated.temperature() >= 55 && heated.temperature() <= 65,
				"Forty ticks over a campfire: about 60 degrees, not jumping: " + heated.temperature()));
		helper.succeedWhen(() -> {
			helper.assertTrue(heated.temperature() == 120 && heated.band() == Band.HOT, "Over a campfire it settles hot: " + heated.temperature());
			helper.assertTrue(cold.temperature() == Heat.AMBIENT, "With nothing beneath it cools to 20: " + cold.temperature());
		});
	}

	// ---------------------------------------------------------------- the same process, the same result

	/**
	 * Two crucibles worked the same way hold exactly the same mixture; the draughts they make are the same; drunk, one
	 * gives Night Vision I for 71.75 seconds, as a Radiance of 1.20 a part (two hot stirs of one glowstone dust) says it
	 * should; and the outcome is recorded as practice.
	 */
	@GameTest(maxTicks = 100)
	public void theSameProcessMakesTheSameDraught(GameTestHelper helper) {
		CrucibleBlockEntity first = crucible(helper, new BlockPos(1, 2, 1), true);
		CrucibleBlockEntity second = crucible(helper, new BlockPos(4, 2, 1), true);
		ServerPlayer player = alembist(helper, ResearchState.UNDERSTOOD);
		startNightEye(helper, first, player);
		startNightEye(helper, second, player);
		helper.runAfterDelay(NEXT_STIR, () -> {
			use(first, player, new ItemStack(Items.STICK));
			use(second, player, new ItemStack(Items.STICK));
			helper.assertTrue(first.mixture().equals(second.mixture()), "Both crucibles hold the same mixture: " + first.mixture());
			helper.assertTrue(first.mixture().dissolved().get(Axis.RADIANCE) == 1196, "Two hot stirs dissolve 1.196: " + first.mixture());
			use(first, player, new ItemStack(Items.GLASS_BOTTLE));
			ItemStack one = take(player, JugcraftConcordance.DRAUGHT);
			use(second, player, new ItemStack(Items.GLASS_BOTTLE));
			ItemStack two = take(player, JugcraftConcordance.DRAUGHT);
			Brew brew = one.get(JugcraftConcordance.BREW);
			helper.assertTrue(brew != null && brew.equals(two.get(JugcraftConcordance.BREW)), "The two draughts are the same: " + brew);
			helper.assertTrue(brew.effects().size() == 1 && brew.effects().get(0).status().equals("minecraft:night_vision")
					&& brew.effects().get(0).amplifier() == 0 && brew.effects().get(0).ticks() == 1196 * 1200 / 1000,
					"Night Vision I for 1435 ticks: " + brew);
			helper.assertTrue(first.mixture().isEmpty(), "One part, one bottle: the crucible is empty");
			helper.assertTrue(ConcordanceProgress.knowledge(player).progress(Alchemy.RESEARCH).evidence()
					.containsKey("practice:jugcraft:alchemy:minecraft:night_vision@0"), "The outcome is recorded as practice");
			one.getItem().finishUsingItem(one, helper.getLevel(), player);
			helper.assertTrue(player.hasEffect(MobEffects.NIGHT_VISION), "Drunk, it gives night vision");
			helper.succeed();
		});
	}

	private static ItemStack take(ServerPlayer player, net.minecraft.world.item.Item item) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack.is(item)) {
				player.getInventory().setItem(i, ItemStack.EMPTY);
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	// ---------------------------------------------------------------- who may, and with what

	/**
	 * Without the Alembic Arts a player cannot put anything in (nothing changes) but can still taste it with a spoon;
	 * the mortar grinds an ingredient into a reagent that dissolves at once in part; a bowl bottles a salve, which used
	 * on a pig gives it the effect.
	 */
	@GameTest(maxTicks = 60)
	public void onlyAnAlembistWorksTheCrucible(GameTestHelper helper) {
		CrucibleBlockEntity crucible = crucible(helper, new BlockPos(2, 2, 2), true);
		crucible.setTemperature(120);
		ServerPlayer novice = alembist(helper, ResearchState.NONE);
		helper.assertTrue(use(crucible, novice, waterBottle()) == InteractionResult.FAIL && crucible.mixture().isEmpty(),
				"Without the Alembic Arts nothing goes in");
		helper.assertTrue(use(crucible, novice, new ItemStack(JugcraftConcordance.SAMPLING_SPOON)).consumesAction(), "Anyone may taste it");
		ServerPlayer player = alembist(helper, ResearchState.UNDERSTOOD);
		use(crucible, player, waterBottle());
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftConcordance.MORTAR));
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GLOWSTONE_DUST, 2));
		RateGate.forget(player.getUUID());
		JugcraftConcordance.MORTAR.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(player.getOffhandItem().getCount() == 1, "The mortar grinds one");
		ItemStack reagent = take(player, JugcraftConcordance.REAGENT_ITEM);
		Reagent ground = reagent.get(JugcraftConcordance.REAGENT);
		helper.assertTrue(ground != null && ground.item().equals(GLOW) && ground.preparation().equals("jugcraft:ground"), "A ground glowstone reagent: " + ground);
		use(crucible, player, reagent);
		helper.assertTrue(crucible.mixture().dissolved().get(Axis.RADIANCE) == 810 && crucible.mixture().pending().get(Axis.RADIANCE) == 540,
				"Ground, 0.81 of its 1.35 dissolves at once: " + crucible.mixture());
		use(crucible, player, new ItemStack(Items.BOWL));
		ItemStack salve = take(player, JugcraftConcordance.SALVE);
		helper.assertTrue(salve.has(JugcraftConcordance.BREW), "A bowl bottles a salve");
		Mob pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 2, 4));
		RateGate.forget(player.getUUID());
		player.setItemInHand(InteractionHand.MAIN_HAND, salve);
		salve.getItem().interactLivingEntity(salve, player, pig, InteractionHand.MAIN_HAND);
		helper.assertTrue(pig.hasEffect(MobEffects.NIGHT_VISION) && player.getInventory().countItem(Items.BOWL) == 1,
				"The salve takes on the pig, and the bowl comes back");
		helper.succeed();
	}

	// ---------------------------------------------------------------- recorded, and repeated

	/**
	 * A formula recorded from a fresh mixture is followed by another crucible: water by pipe, the glowstone from its
	 * buffer, two stirs once it is hot, and a draught into its output from a supplied bottle, the same draught as the
	 * one made by hand. Hoppers reach only the buffer and bottle slot from above or a side; the output comes out from a side
	 * or below, never from above.
	 */
	@GameTest(maxTicks = 400)
	public void aFormulaIsRepeatedByAnotherCrucible(GameTestHelper helper) {
		CrucibleBlockEntity byHand = crucible(helper, new BlockPos(1, 2, 1), true);
		CrucibleBlockEntity automated = crucible(helper, new BlockPos(4, 2, 4), true);
		ServerPlayer player = alembist(helper, ResearchState.UNDERSTOOD);
		startNightEye(helper, byHand, player);
		helper.runAfterDelay(NEXT_STIR, () -> {
			use(byHand, player, new ItemStack(Items.STICK));
			use(byHand, player, new ItemStack(JugcraftConcordance.FORMULA_ITEM));
			ItemStack written = take(player, JugcraftConcordance.FORMULA_ITEM);
			String text = written.get(JugcraftConcordance.FORMULA);
			helper.assertTrue("water 1; add minecraft:glowstone_dust jugcraft:raw; stir hot; stir hot".equals(text), "The formula records the process: " + text);
			use(byHand, player, new ItemStack(Items.GLASS_BOTTLE));
			Brew byHandBrew = take(player, JugcraftConcordance.DRAUGHT).get(JugcraftConcordance.BREW);
			helper.assertTrue(use(automated, player, written).consumesAction() && automated.program() != null, "The written formula is set");
			automated.setTemperature(120);
			try (Transaction transaction = Transaction.openOuter()) {
				automated.water.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BOTTLE, transaction);
				transaction.commit();
			}
			helper.assertTrue(automated.canPlaceItemThroughFace(0, new ItemStack(Items.GLOWSTONE_DUST), Direction.UP)
					&& !automated.canPlaceItemThroughFace(0, new ItemStack(Items.DIRT), Direction.UP)
					&& !automated.canPlaceItemThroughFace(0, new ItemStack(Items.GLOWSTONE_DUST), Direction.DOWN)
					&& automated.canPlaceItemThroughFace(CrucibleBlockEntity.BOTTLE_SLOT, new ItemStack(Items.GLASS_BOTTLE), Direction.NORTH)
					&& !automated.canTakeItemThroughFace(CrucibleBlockEntity.OUTPUT_SLOT, ItemStack.EMPTY, Direction.UP)
					&& automated.canTakeItemThroughFace(CrucibleBlockEntity.OUTPUT_SLOT, ItemStack.EMPTY, Direction.EAST)
					&& automated.canTakeItemThroughFace(CrucibleBlockEntity.OUTPUT_SLOT, ItemStack.EMPTY, Direction.DOWN)
					&& !automated.canTakeItemThroughFace(0, ItemStack.EMPTY, Direction.EAST),
					"Hoppers reach the buffer and bottles from above and the sides; the output comes out of a side or below");
			automated.setItem(0, new ItemStack(Items.GLOWSTONE_DUST));
			automated.setItem(CrucibleBlockEntity.BOTTLE_SLOT, new ItemStack(Items.GLASS_BOTTLE));
			helper.succeedWhen(() -> {
				ItemStack made = automated.getItem(CrucibleBlockEntity.OUTPUT_SLOT);
				helper.assertTrue(made.is(JugcraftConcordance.DRAUGHT), "Waiting for the repeat: step " + automated.step() + ", " + automated.mixture());
				helper.assertTrue(byHandBrew.equals(made.get(JugcraftConcordance.BREW)), "The repeat makes the same draught: " + made.get(JugcraftConcordance.BREW));
				helper.assertTrue(automated.getItem(0).isEmpty() && automated.water.amount == 0 && automated.mixture().isEmpty(),
						"It used exactly the glowstone and the water");
			});
		});
	}

	/**
	 * A crucible run entirely by machinery: a campfire beneath, a hopper above dropping in glowstone and bottles, water
	 * in its tank through the Transfer API, and a Pneumatic Extractor at its side moving each draught into a chest. The
	 * formula runs twice over and makes two draughts, both the one the worked example predicts.
	 */
	@GameTest(maxTicks = 600)
	public void machineryRunsAFormulaTwice(GameTestHelper helper) {
		BlockPos at = new BlockPos(2, 2, 2);
		CrucibleBlockEntity crucible = crucible(helper, at, true);
		crucible.setTemperature(120);
		ServerPlayer player = alembist(helper, ResearchState.UNDERSTOOD);
		ItemStack written = new ItemStack(JugcraftConcordance.FORMULA_ITEM);
		written.set(JugcraftConcordance.FORMULA, "water 1; add minecraft:glowstone_dust jugcraft:raw; stir hot; stir hot");
		helper.assertTrue(use(crucible, player, written).consumesAction() && crucible.program() != null, "The formula is set");
		try (Transaction transaction = Transaction.openOuter()) {
			crucible.water.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BOTTLE * 2, transaction);
			transaction.commit();
		}
		helper.setBlock(at.above(), Blocks.HOPPER);
		HopperBlockEntity hopper = helper.getBlockEntity(at.above(), HopperBlockEntity.class);
		hopper.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, 2));
		hopper.setItem(1, new ItemStack(Items.GLASS_BOTTLE, 2));
		BlockPos side = at.east();
		helper.setBlock(side, JugcraftLogistics.PNEUMATIC_EXTRACTOR.defaultBlockState().setValue(PneumaticExtractorBlock.FACING, Direction.WEST));
		helper.setBlock(side.east(), Blocks.CHEST);
		ChestBlockEntity chest = helper.getBlockEntity(side.east(), ChestBlockEntity.class);
		helper.succeedWhen(() -> {
			int made = 0;
			Brew first = null;
			boolean same = true;
			for (int i = 0; i < chest.getContainerSize(); i++) {
				ItemStack stack = chest.getItem(i);
				if (stack.is(JugcraftConcordance.DRAUGHT)) {
					made += stack.getCount();
					Brew brew = stack.get(JugcraftConcordance.BREW);
					same &= first == null || first.equals(brew);
					first = first == null ? brew : first;
				}
			}
			helper.assertTrue(made == 2, "Waiting for two draughts in the chest: " + made + " (step " + crucible.step() + ", "
					+ crucible.mixture() + ", output " + crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT) + ")");
			helper.assertTrue(same && first != null && first.effects().size() == 1
					&& first.effects().get(0).status().equals("minecraft:night_vision") && first.effects().get(0).ticks() == 1435,
					"Both are Night Vision I for 1435 ticks: " + first);
			helper.assertTrue(hopper.isEmpty() && crucible.water.amount == 0 && crucible.mixture().isEmpty()
					&& crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT).isEmpty(), "It used exactly the glowstone, bottles and water");
		});
	}

	/** Broken, a crucible drops what its buffer, bottle slot and output hold; its mixture spills. */
	@GameTest(maxTicks = 20)
	public void aBrokenCrucibleDropsItsItems(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = new BlockPos(2, 2, 2);
		CrucibleBlockEntity crucible = crucible(helper, at, false);
		crucible.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, 3));
		crucible.setItem(CrucibleBlockEntity.BOTTLE_SLOT, new ItemStack(Items.GLASS_BOTTLE, 2));
		helper.destroyBlock(at);
		int glowstone = 0;
		int bottles = 0;
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(at)).inflate(3.0))) {
			glowstone += item.getItem().is(Items.GLOWSTONE_DUST) ? item.getItem().getCount() : 0;
			bottles += item.getItem().is(Items.GLASS_BOTTLE) ? item.getItem().getCount() : 0;
		}
		helper.assertTrue(glowstone == 3 && bottles == 2, "The crucible's items drop once: " + glowstone + " glowstone, " + bottles + " bottles");
		helper.succeed();
	}

	/** A crucible saves its mixture (to the milli-unit), its history and its formula. */
	@GameTest(maxTicks = 40)
	public void aCrucibleKeepsItsMixtureWhenSaved(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		CrucibleBlockEntity crucible = crucible(helper, new BlockPos(2, 2, 2), true);
		ServerPlayer player = alembist(helper, ResearchState.UNDERSTOOD);
		startNightEye(helper, crucible, player);
		use(crucible, player, new ItemStack(Items.SWEET_BERRIES));
		Mixture before = crucible.mixture();
		CompoundTag saved = crucible.saveWithoutMetadata(level.registryAccess());
		CrucibleBlockEntity copy = new CrucibleBlockEntity(crucible.getBlockPos(), crucible.getBlockState());
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		helper.assertTrue(copy.mixture().equals(before), "The mixture is saved exactly: " + copy.mixture() + " vs " + before);
		helper.assertTrue(Formula.of(copy.mixture()) != null && Formula.of(copy.mixture()).text().equals(Formula.of(before).text()),
				"Its history still records as the same formula");
		helper.succeed();
	}

	// ---------------------------------------------------------------- what it shows (roadmap step 27)

	/**
	 * The vessel shows what is in it and no more: what a client is sent is the volume, the heat and the spoon's reading
	 * (the strongest property, murky or not), never the mixture's makeup. Each step shows as work, a searing stir (which
	 * damages the mixture) as danger, and a bottle filled as success; nothing shows a shortage while nothing is lacking.
	 */
	@GameTest(maxTicks = 100)
	public void theVesselShowsWhatASpoonFinds(GameTestHelper helper) {
		SignWatch signs = new SignWatch(helper);
		BlockPos at = new BlockPos(1, 2, 1);
		CrucibleBlockEntity crucible = crucible(helper, at, false);
		ServerPlayer player = alembist(helper, ResearchState.UNDERSTOOD);
		CompoundTag empty = crucible.getUpdateTag(helper.getLevel().registryAccess());
		helper.assertTrue(empty.getIntOr("parts", -1) == 0 && empty.getStringOr("taste", "?").isEmpty() && !empty.getBooleanOr("murky", true),
				"An empty crucible shows nothing in it: " + empty);
		startNightEye(helper, crucible, player);
		CompoundTag shown = crucible.getUpdateTag(helper.getLevel().registryAccess());
		helper.assertTrue(shown.keySet().equals(Set.of("temperature", "parts", "taste", "murky")),
				"A client is sent the heat, the volume and the spoon's reading, nothing of the makeup: " + shown.keySet());
		helper.assertTrue(shown.getIntOr("parts", 0) == 1 && shown.getStringOr("taste", "").equals(Axis.RADIANCE.id),
				"One part, tasting of Radiance, as a spoon would find: " + shown);
		helper.assertValueEqual(signs.at(Sign.WORK, at), 3L, "water, glowstone and a stir: three steps of work");
		helper.runAfterDelay(NEXT_STIR, () -> {
			crucible.setTemperature(200);
			use(crucible, player, new ItemStack(Items.STICK));
			helper.assertTrue(crucible.band() == Band.SEARING, "Searing: " + crucible.temperature());
			helper.assertValueEqual(signs.at(Sign.PERIL, at), 1L, "a searing stir shows danger");
			helper.assertValueEqual(signs.at(Sign.WORK, at), 3L, "and not work");
			use(crucible, player, new ItemStack(Items.GLASS_BOTTLE));
			helper.assertValueEqual(signs.at(Sign.DONE, at), 1L, "a bottle filled shows success");
			helper.assertTrue(signs.of(Sign.WANT).isEmpty(), "No shortage while nothing is lacking: " + signs.all());
			signs.close();
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------- Winter Brine (Concordance Chunk B, B-08)

	/**
	 * Rime brews Frostward, and Rime is cold's own: each stir above the cold band keeps only part of the Rime already
	 * dissolved. On paper, three snowballs as they come in one part of water, stirred three times, keep what
	 * tools/concordance_alchemy.py winter_brine says at each band (check_mod_data holds the two to each other): cold, a
	 * draught of Frostward I for 48.75 s; warm, too weak, naming the stirs that thawed it; searing, only the fouling's
	 * Nausea. A warm stir before any snowball went in thaws nothing and is not named. By hand, one crucible over stone and
	 * one over a magma block: the cold one's draught drunk gives Frostward and no Resistance; the warm one's does nothing.
	 */
	@GameTest(maxTicks = 60)
	public void winterBrineNeedsTheColdBand(GameTestHelper helper) {
		AlchemyCatalog catalog = Alchemy.catalog();
		helper.assertTrue(catalog.property(Axis.RIME.id) != null && Frostward.STATUS.equals(catalog.property(Axis.RIME.id).status()),
				"Rime brews Frostward: " + catalog.property(Axis.RIME.id));
		long[] kept = {813, 685, 211, 0};
		for (int i = 0; i < kept.length; i++) {
			Band band = Band.values()[i];
			long rime = threeSnowballs(1, band).dissolved().get(Axis.RIME);
			helper.assertTrue(rime == kept[i], "Three " + band.id + " stirs keep " + kept[i] + " Rime, not " + rime);
		}
		Outcome cold = bottled(threeSnowballs(1, Band.COLD));
		helper.assertTrue(cold.key().equals("jugcraft:frostward@0") && cold.effects().size() == 1 && cold.effects().get(0).ticks() == 975,
				"Stirred cold: Frostward I for 975 ticks: " + cold);
		Outcome warm = bottled(threeSnowballs(1, Band.WARM));
		helper.assertTrue(warm.effects().isEmpty() && warm.reasons().stream().anyMatch(reason -> reason.key().equals("alchemy.too_weak")
				&& reason.args().stream().anyMatch(arg -> arg.toString().contains("thawed"))),
				"Stirred warm: too weak, and the warm stirs named as what thawed it: " + warm.reasons());
		List<Operation> stirredFirst = new ArrayList<>(List.of(new Operation.Water(1), new Operation.Stir(Band.WARM)));
		for (int i = 0; i < 3; i++) {
			stirredFirst.add(new Operation.Add(SNOW, "jugcraft:raw"));
		}
		for (int i = 0; i < 3; i++) {
			stirredFirst.add(new Operation.Stir(Band.COLD));
		}
		Outcome early = bottled(((Mixture.Applied) new Formula(stirredFirst).replay(catalog)).mixture());
		helper.assertTrue(early.key().equals("jugcraft:frostward@0") && early.reasons().stream()
						.noneMatch(reason -> reason.args().stream().anyMatch(arg -> arg.toString().contains("thawed"))),
				"A warm stir before any Rime went in thaws nothing and is not named: " + early.reasons());
		Outcome searing = bottled(threeSnowballs(1, Band.SEARING));
		helper.assertTrue(searing.key().equals("minecraft:nausea@0"), "Stirred searing: no Rime left, only the fouling: " + searing.key());

		ServerPlayer player = alembist(helper, ResearchState.UNDERSTOOD);
		CrucibleBlockEntity cool = crucible(helper, new BlockPos(1, 2, 1), Blocks.STONE);
		CrucibleBlockEntity warmed = crucible(helper, new BlockPos(4, 2, 1), Blocks.MAGMA_BLOCK);
		warmed.setTemperature(60);
		startWinterBrine(cool, player);
		startWinterBrine(warmed, player);
		helper.runAfterDelay(NEXT_STIR, () -> {
			use(cool, player, new ItemStack(Items.STICK));
			use(warmed, player, new ItemStack(Items.STICK));
			helper.runAfterDelay(NEXT_STIR, () -> {
				use(cool, player, new ItemStack(Items.STICK));
				use(warmed, player, new ItemStack(Items.STICK));
				helper.assertTrue(cool.band() == Band.COLD && warmed.band() == Band.WARM, "Over stone it stays cold, over magma warm: "
						+ cool.temperature() + ", " + warmed.temperature());
				helper.assertTrue(cool.mixture().dissolved().get(Axis.RIME) == 813 && warmed.mixture().dissolved().get(Axis.RIME) == 685,
						"By hand, the same Rime as on paper: " + cool.mixture() + " / " + warmed.mixture());
				use(cool, player, new ItemStack(Items.GLASS_BOTTLE));
				ItemStack brine = take(player, JugcraftConcordance.DRAUGHT);
				use(warmed, player, new ItemStack(Items.GLASS_BOTTLE));
				Brew thawed = take(player, JugcraftConcordance.DRAUGHT).get(JugcraftConcordance.BREW);
				helper.assertTrue(thawed != null && thawed.effects().isEmpty(), "Stirred warm, the draught does nothing: " + thawed);
				brine.getItem().finishUsingItem(brine, helper.getLevel(), player);
				MobEffectInstance ward = player.getEffect(Frostward.FROSTWARD);
				helper.assertTrue(ward != null && ward.getDuration() == 975 && ward.getAmplifier() == 0 && !player.hasEffect(MobEffects.RESISTANCE),
						"Drunk, Winter Brine gives Frostward I for 975 ticks and no Resistance: " + ward);
				helper.succeed();
			});
		});
	}

	/**
	 * A save test: a Rime draught bottled before Winter Brine (Resistance I for 487 ticks: 0.813 a part at 600 ticks a
	 * unit) reads, writes and reads again as it was, and drunk still gives Resistance, never Frostward: a bottle keeps
	 * what it was bottled with.
	 */
	@GameTest(maxTicks = 20)
	public void oldRimeBottlesStillGiveResistance(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		DynamicOps<JsonElement> ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
		JsonElement saved = JsonParser.parseString("{\"id\":\"jugcraft:draught\",\"count\":1,\"components\":{\"jugcraft:brew\":"
				+ "{\"effects\":[{\"status\":\"minecraft:resistance\",\"amplifier\":0,\"ticks\":487}],\"outcome\":\"minecraft:resistance@0\"}}}");
		ItemStack old = ItemStack.CODEC.parse(ops, saved).getOrThrow();
		Brew brew = old.get(JugcraftConcordance.BREW);
		helper.assertTrue(old.is(JugcraftConcordance.DRAUGHT) && brew != null && brew.effects().size() == 1
				&& brew.effects().get(0).status().equals("minecraft:resistance"), "The old draught reads as it was: " + brew);
		ItemStack again = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, old).getOrThrow()).getOrThrow();
		helper.assertTrue(brew.equals(again.get(JugcraftConcordance.BREW)), "Written and read again it is the same: " + again.get(JugcraftConcordance.BREW));
		ServerPlayer player = alembist(helper, ResearchState.NONE);
		old.getItem().finishUsingItem(old, level, player);
		MobEffectInstance resistance = player.getEffect(MobEffects.RESISTANCE);
		helper.assertTrue(resistance != null && resistance.getDuration() == 487 && resistance.getAmplifier() == 0,
				"Drunk, it still gives Resistance I for 487 ticks: " + resistance);
		helper.assertTrue(!Frostward.warded(player), "and no Frostward");
		helper.succeed();
	}

	/**
	 * A Rime formula written before Winter Brine (three snowballs, three cold stirs) still reads, is set on a crucible,
	 * and run from its tank and buffer now bottles Frostward I for 975 ticks, using exactly its snow and water.
	 */
	@GameTest(maxTicks = 300)
	public void aRimeFormulaWrittenBeforeBrewsFrostward(GameTestHelper helper) {
		String text = "water 1; add minecraft:snowball jugcraft:raw; add minecraft:snowball jugcraft:raw; "
				+ "add minecraft:snowball jugcraft:raw; stir cold; stir cold; stir cold";
		helper.assertTrue(Formula.parse(text) != null, "The formula still reads");
		CrucibleBlockEntity crucible = crucible(helper, new BlockPos(2, 2, 2), Blocks.STONE);
		ServerPlayer player = alembist(helper, ResearchState.UNDERSTOOD);
		ItemStack written = new ItemStack(JugcraftConcordance.FORMULA_ITEM);
		written.set(JugcraftConcordance.FORMULA, text);
		helper.assertTrue(use(crucible, player, written).consumesAction() && crucible.program() != null, "The formula is set");
		try (Transaction transaction = Transaction.openOuter()) {
			crucible.water.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BOTTLE, transaction);
			transaction.commit();
		}
		crucible.setItem(0, new ItemStack(Items.SNOWBALL, 3));
		crucible.setItem(CrucibleBlockEntity.BOTTLE_SLOT, new ItemStack(Items.GLASS_BOTTLE));
		Brew expected = new Brew(List.of(new Brew.Dose(Frostward.STATUS, 0, 975, false)), "jugcraft:frostward@0");
		helper.succeedWhen(() -> {
			ItemStack made = crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT);
			helper.assertTrue(expected.equals(made.get(JugcraftConcordance.BREW)), "Waiting for Frostward: step " + crucible.step() + ", "
					+ crucible.mixture() + ", output " + made.get(JugcraftConcordance.BREW));
			helper.assertTrue(crucible.getItem(0).isEmpty() && crucible.water.amount == 0, "It used exactly its three snowballs and its water");
		});
	}

	/**
	 * Economy (06:146-158): Frostward is never cheaper than its snow. Yeti Fur, the Frost Horn, wool and brews are no
	 * ingredients: the crucible will not take fur by hand or by hopper, and the mortar will not grind it. Two parts of
	 * water make more bottles but no more Frostward (each too weak); and two draughts drunk one after the other last as
	 * long as one.
	 */
	@GameTest(maxTicks = 20)
	public void frostwardIsNeverCheaperThanItsSnow(GameTestHelper helper) {
		AlchemyCatalog catalog = Alchemy.catalog();
		for (String thing : List.of("jugcraft:yeti_fur", "jugcraft:frost_horn", "minecraft:white_wool", "jugcraft:draught", "jugcraft:salve")) {
			helper.assertTrue(catalog.ingredient(thing) == null, thing + " is no alchemical ingredient");
		}
		CrucibleBlockEntity crucible = crucible(helper, new BlockPos(2, 2, 2), Blocks.STONE);
		ServerPlayer player = alembist(helper, ResearchState.UNDERSTOOD);
		use(crucible, player, waterBottle());
		Mixture before = crucible.mixture();
		ItemStack fur = new ItemStack(JugcraftYeti.YETI_FUR, 2);
		helper.assertTrue(use(crucible, player, fur) == InteractionResult.TRY_WITH_EMPTY_HAND && fur.getCount() == 2
				&& crucible.mixture().equals(before), "The crucible takes no Yeti Fur: " + crucible.mixture());
		helper.assertTrue(!crucible.canPlaceItem(0, fur), "nor from a hopper");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftConcordance.MORTAR));
		player.setItemInHand(InteractionHand.OFF_HAND, fur);
		RateGate.forget(player.getUUID());
		helper.assertTrue(JugcraftConcordance.MORTAR.use(helper.getLevel(), player, InteractionHand.MAIN_HAND) == InteractionResult.FAIL
				&& player.getOffhandItem().getCount() == 2 && take(player, JugcraftConcordance.REAGENT_ITEM).isEmpty(),
				"The mortar grinds no Yeti Fur");
		Mixture diluted = threeSnowballs(2, Band.COLD);
		Mixture.Bottled first = diluted.bottle();
		Outcome weak = Outcome.of(first.dose(), first.contaminant(), catalog, diluted.history());
		helper.assertTrue(first.dose().get(Axis.RIME) == 406 && weak.effects().isEmpty(),
				"Three snowballs in two parts: 0.406 a part, too weak for Frostward: " + first.dose());
		Outcome cold = bottled(threeSnowballs(1, Band.COLD));
		ItemStack one = Alchemy.brew(JugcraftConcordance.DRAUGHT, cold);
		ItemStack two = Alchemy.brew(JugcraftConcordance.DRAUGHT, cold);
		ServerPlayer drinker = alembist(helper, ResearchState.NONE);
		one.getItem().finishUsingItem(one, helper.getLevel(), drinker);
		two.getItem().finishUsingItem(two, helper.getLevel(), drinker);
		MobEffectInstance ward = drinker.getEffect(Frostward.FROSTWARD);
		helper.assertTrue(ward != null && ward.getDuration() == 975, "Two draughts last as long as one, not their sum: " + ward);
		helper.succeed();
	}

	/** Roadmap step 28: only an alembist may stop a crucible's formula, as only one may set it. */
	@GameTest(maxTicks = 20)
	public void onlyAnAlembistStopsAFormula(GameTestHelper helper) {
		CrucibleBlockEntity crucible = crucible(helper, new BlockPos(2, 2, 2), true);
		ServerPlayer alembist = alembist(helper, ResearchState.UNDERSTOOD);
		ItemStack written = new ItemStack(JugcraftConcordance.FORMULA_ITEM);
		written.set(JugcraftConcordance.FORMULA, "water 1; add minecraft:glowstone_dust jugcraft:raw; stir hot; stir hot");
		helper.assertTrue(use(crucible, alembist, written).consumesAction() && crucible.program() != null, "The formula is set");
		ServerPlayer stranger = alembist(helper, ResearchState.NONE);
		crucible.useEmpty(stranger, true);
		helper.assertTrue(crucible.program() != null, "Someone who knows no alchemy cannot stop it");
		crucible.useEmpty(alembist, true);
		helper.assertTrue(crucible.program() == null, "An alembist can");
		helper.succeed();
	}

	// ---------------------------------------------------------------- why it waits (Concordance Chunk C, C-01)

	/**
	 * C-01 (C-P1): rules that can no longer make a formula's next step (a reload that drops glowstone; the empty rules
	 * stand for it) keep the formula, its step and all it holds. Each pulse names the step and why, the danger shows
	 * once, the Peepo port asks for nothing and calls the recipe unavailable, and the status line gives the reason.
	 */
	@GameTest(maxTicks = 20)
	public void aChangedRuleKeepsTheFormulaAndSaysWhy(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = new BlockPos(2, 2, 2);
		SignWatch signs = new SignWatch(helper);
		CrucibleBlockEntity crucible = pouredNightEye(helper, at);
		Mixture before = crucible.mixture();
		record Seen(Diagnostic first, Diagnostic second, Diagnostic shown, int dust, int bottles, CompanionStatus status, Component line) {
		}
		Seen seen = under(ConcordanceRules.EMPTY, () -> new Seen(crucible.automate(level), crucible.automate(level), crucible.waiting(),
				crucible.companion.needed(new ItemStack(Items.GLOWSTONE_DUST)), crucible.companion.needed(new ItemStack(Items.GLASS_BOTTLE)),
				crucible.companion.status(), crucible.statusLine(ConcordanceProgress.now(level))));
		signs.close();
		helper.assertTrue(glowstoneRefused().equals(seen.first()) && seen.first().equals(seen.second()) && seen.first().equals(seen.shown()),
				"Each pulse names step 2 and why: " + seen.first() + " / " + seen.second() + " / " + seen.shown());
		helper.assertTrue(crucible.program() != null && NIGHT_EYE.equals(crucible.program().text()) && crucible.step() == 1,
				"The formula is kept at its step: " + crucible.program() + ", step " + crucible.step());
		helper.assertTrue(crucible.mixture().equals(before) && crucible.getItem(0).getCount() == 1
				&& crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT).getCount() == 1, "Nothing is taken or bottled: " + crucible.mixture());
		helper.assertTrue(seen.dust() == 0 && seen.bottles() == 0 && seen.status() == CompanionStatus.RECIPE_MISSING,
				"The Peepo port asks for nothing: " + seen.dust() + " dust, " + seen.bottles() + " bottles, " + seen.status());
		helper.assertValueEqual(signs.at(Sign.PERIL, at), 1L, "the danger shown once, when it began");
		helper.assertTrue(reasonKey(seen.line()).equals("compose.jugcraft.diagnostic.incompatible"), "The status line says why: " + seen.line());
		helper.succeed();
	}

	/**
	 * C-01 (C-P1): rules that can make the step again are noticed before any pulse (the reason clears at once), and the
	 * formula carries on from its step to the same draught it would have made.
	 */
	@GameTest(maxTicks = 20)
	public void aRestoredRuleResumesTheFormula(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = new BlockPos(2, 2, 2);
		SignWatch signs = new SignWatch(helper);
		CrucibleBlockEntity crucible = pouredNightEye(helper, at);
		Diagnostic during = under(ConcordanceRules.EMPTY, () -> crucible.automate(level));
		helper.assertTrue(glowstoneRefused().equals(during), "Under the changed rules it names step 2: " + during);
		helper.assertTrue(crucible.waiting() == null && crucible.incompatibility() == null,
				"The rules restored, it no longer waits, before any pulse: " + crucible.waiting());
		helper.assertTrue(crucible.automate(level) == null && crucible.step() == 2 && crucible.getItem(0).isEmpty(),
				"It carries on from its step: step " + crucible.step());
		helper.assertTrue(pulse(crucible, level, 2) == null && crucible.step() == 4, "Two stirs: step " + crucible.step());
		helper.assertTrue(crucible.automate(level) == null
				&& ItemStack.isSameItemSameComponents(crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT), draught(NIGHT_EYE))
				&& crucible.mixture().isEmpty() && crucible.step() == 0, "The same draught as ever: " + crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT));
		helper.assertValueEqual(signs.at(Sign.PERIL, at), 1L, "the danger shown once");
		signs.close();
		helper.succeed();
	}

	/**
	 * C-01 (plan 03:54): a written formula the rules in force cannot replay is not set, and says which step and why; one
	 * that does not read is not set either. Setting one names what is never hidden: the harmful statuses a dose carries
	 * and the first searing stir.
	 */
	@GameTest(maxTicks = 20)
	public void aWrittenFormulaThatNoLongerReplaysSaysWhichStep(GameTestHelper helper) {
		BlockPos at = new BlockPos(2, 2, 2);
		CrucibleBlockEntity crucible = crucible(helper, at, true);
		ServerPlayer player = alembist(helper, ResearchState.UNDERSTOOD);
		record Tried(InteractionResult result, Diagnostic why) {
		}
		Tried tried = under(ConcordanceRules.EMPTY, () -> new Tried(use(crucible, player, written(NIGHT_EYE)),
				Formula.parse(NIGHT_EYE).incompatibility(Alchemy.catalog())));
		helper.assertTrue(tried.result() == InteractionResult.FAIL && crucible.program() == null && glowstoneRefused().equals(tried.why()),
				"Rules without glowstone refuse it at step 2: " + tried);
		AlchemyCatalog catalog = Alchemy.catalog();
		Diagnostic overflow = Formula.parse("water 4; water 3").incompatibility(catalog);
		helper.assertTrue(Diagnostic.incompatible(2, new Operation.Water(3).describe(), Text.of("alchemy.overflowing", Mixture.MAX_PARTS)).equals(overflow),
				"Seven parts overflow at step 2: " + overflow);
		helper.assertTrue(use(crucible, player, written("water 4; water 3")) == InteractionResult.FAIL && crucible.program() == null,
				"and it is not set");
		helper.assertTrue(Formula.parse("water 1; stir boiling") == null && use(crucible, player, written("water 1; stir boiling")) == InteractionResult.FAIL,
				"A formula that does not read is not set");
		List<Text> spider = Assay.risks(Formula.parse(SPIDER), Mixture.EMPTY, catalog);
		helper.assertTrue(List.of(Text.of("alchemy.risk.harm", List.of(new Text.Ref("status", "minecraft:poison")))).equals(spider),
				"A spider eye's dose brings Poison, and its fouling is under Nausea's threshold: " + spider);
		List<Text> seared = Assay.risks(Formula.parse("water 2; stir searing"), Mixture.EMPTY, catalog);
		helper.assertTrue(List.of(Text.of("alchemy.risk.searing", 2)).equals(seared), "Step 2 stirs it searing: " + seared);
		helper.assertTrue(Assay.risks(Formula.parse(NIGHT_EYE), Mixture.EMPTY, catalog).isEmpty(), "The worked example hides nothing");
		helper.assertTrue(use(crucible, player, written(SPIDER)).consumesAction() && SPIDER.equals(crucible.program().text()),
				"A formula with a risk is still set, by a plain use");
		helper.succeed();
	}

	/**
	 * C-01 (C-P1): a saved formula this build cannot read is kept as written and saved back, never followed: it says so,
	 * shows danger once, asks the Peepo port for nothing and draws no water; sneaking with an empty hand clears it.
	 */
	@GameTest(maxTicks = 20)
	public void aSavedFormulaThatNoLongerReadsIsKeptAndReported(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = new BlockPos(2, 2, 2);
		SignWatch signs = new SignWatch(helper);
		CrucibleBlockEntity crucible = crucible(helper, at, false);
		ServerPlayer player = alembist(helper, ResearchState.UNDERSTOOD);
		helper.assertTrue(use(crucible, player, written(NIGHT_EYE)).consumesAction(), "The formula is set");
		pour(crucible, FluidConstants.BOTTLE);
		CompoundTag saved = crucible.saveWithoutMetadata(level.registryAccess());
		String unknown = "water 1; stir boiling";
		saved.putString("program", unknown);
		saved.putInt("step", 1);
		crucible.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		helper.assertTrue(crucible.program() == null && unknown.equals(crucible.unreadableProgram()) && crucible.step() == 1,
				"Read back, the formula is kept as written at its step: '" + crucible.unreadableProgram() + "', step " + crucible.step());
		Diagnostic unreadable = Diagnostic.of(Code.UNREADABLE);
		helper.assertTrue(unreadable.equals(crucible.waiting()) && unreadable.equals(crucible.incompatibility()),
				"It says it cannot be read: " + crucible.waiting());
		helper.assertTrue(crucible.companion.needed(new ItemStack(Items.GLASS_BOTTLE)) == 0 && crucible.companion.status() == CompanionStatus.RECIPE_MISSING,
				"The Peepo port asks for nothing: " + crucible.companion.status());
		helper.assertTrue(unreadable.equals(crucible.automate(level)) && crucible.water.amount == FluidConstants.BOTTLE && crucible.mixture().isEmpty(),
				"A pulse does nothing: " + crucible.water.amount + " droplets, " + crucible.mixture());
		helper.assertValueEqual(signs.at(Sign.PERIL, at), 1L, "the danger shown once");
		Component line = crucible.statusLine(ConcordanceProgress.now(level));
		helper.assertTrue(reasonKey(line).equals("compose.jugcraft.diagnostic.unreadable"), "The status line says why: " + line);
		CompoundTag again = crucible.saveWithoutMetadata(level.registryAccess());
		helper.assertTrue(unknown.equals(again.getStringOr("program", "")) && again.getIntOr("step", -1) == 1 && again.getIntOr(Saved.VERSION, 0) == 1,
				"Saved again, it is written back unchanged: " + again);
		crucible.useEmpty(player, true);
		helper.assertTrue(crucible.unreadableProgram().isEmpty() && crucible.waiting() == null
				&& !crucible.saveWithoutMetadata(level.registryAccess()).keySet().contains("program"), "Sneaking with an empty hand clears it");
		signs.close();
		helper.succeed();
	}

	/**
	 * C-01 (C-P1, C-P23): while the rules cannot follow its formula, the crucible asks the Peepo port for nothing (seen at
	 * once, before any pulse), draws nothing, and does not bottle even a finished batch; the rules restored, it asks again
	 * and bottles. C-05's stand and C-06's filing extend this test.
	 */
	@GameTest(maxTicks = 20)
	public void anIncompatibleFormulaAsksForNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = new BlockPos(2, 2, 2);
		CrucibleBlockEntity crucible = following(helper, at, NIGHT_EYE, alembist(helper, ResearchState.UNDERSTOOD));
		ItemStack dust = new ItemStack(Items.GLOWSTONE_DUST);
		ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
		helper.assertTrue(crucible.companion.needed(dust) == 1 && crucible.companion.needed(bottle) == 1 && crucible.incompatibility() == null,
				"Followable, it asks for its glowstone and a bottle");
		pour(crucible, FluidConstants.BOTTLE);
		record Asked(int dust, int bottles, CompanionStatus status, Diagnostic hook, Diagnostic first, Diagnostic second, Diagnostic third) {
		}
		Asked asked = under(ConcordanceRules.EMPTY, () -> new Asked(crucible.companion.needed(dust), crucible.companion.needed(bottle),
				crucible.companion.status(), crucible.incompatibility(), crucible.automate(level), crucible.automate(level), crucible.automate(level)));
		helper.assertTrue(asked.dust() == 0 && asked.bottles() == 0 && asked.status() == CompanionStatus.RECIPE_MISSING,
				"It asks for nothing at once: " + asked);
		helper.assertTrue(asked.hook() != null && asked.hook().code() == Code.INCOMPATIBLE && asked.hook().equals(asked.first())
				&& asked.hook().equals(asked.second()) && asked.hook().equals(asked.third()), "Every pulse says why: " + asked);
		helper.assertTrue(crucible.water.amount == FluidConstants.BOTTLE && crucible.mixture().isEmpty() && crucible.step() == 0,
				"Nothing is drawn: " + crucible.water.amount + " droplets, " + crucible.mixture());
		helper.assertTrue(crucible.companion.needed(dust) == 1 && crucible.incompatibility() == null, "The rules restored, it asks again");
		// C-P23: a batch made to its end is not bottled while the rules refuse its formula.
		crucible.setItem(0, new ItemStack(Items.GLOWSTONE_DUST));
		crucible.setItem(CrucibleBlockEntity.BOTTLE_SLOT, new ItemStack(Items.GLASS_BOTTLE));
		helper.assertTrue(pulse(crucible, level, 4) == null && crucible.step() == 4 && crucible.mixture().parts() == 1,
				"Made to its end, one part to bottle: step " + crucible.step());
		Mixture done = crucible.mixture();
		record Finished(Diagnostic pulse, Diagnostic hook, boolean outputEmpty, int bottles, Mixture mixture) {
		}
		Finished finished = under(ConcordanceRules.EMPTY, () -> new Finished(crucible.automate(level), crucible.incompatibility(),
				crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT).isEmpty(), crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT).getCount(), crucible.mixture()));
		helper.assertTrue(glowstoneRefused().equals(finished.pulse()) && finished.pulse().equals(finished.hook()) && finished.outputEmpty()
				&& finished.bottles() == 1 && done.equals(finished.mixture()), "Even a finished batch waits: " + finished);
		helper.assertTrue(crucible.automate(level) == null && ItemStack.isSameItemSameComponents(crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT), draught(NIGHT_EYE))
				&& crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT).getCount() == 1 && crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT).isEmpty()
				&& crucible.mixture().isEmpty(), "The rules restored, one pulse bottles it: " + crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT));
		helper.succeed();
	}

	/**
	 * C-01 (plan 03:64): a finished batch waits for a bottle, then for room: a full output and an output holding another
	 * brew pause it, nothing voided or taken, each shown once; emptied, it bottles into it.
	 */
	@GameTest(maxTicks = 20)
	public void aFullOutputPausesAndVoidsNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = new BlockPos(2, 2, 2);
		SignWatch signs = new SignWatch(helper);
		CrucibleBlockEntity crucible = following(helper, at, NIGHT_EYE, alembist(helper, ResearchState.UNDERSTOOD));
		pour(crucible, FluidConstants.BOTTLE);
		crucible.setItem(0, new ItemStack(Items.GLOWSTONE_DUST));
		helper.assertTrue(pulse(crucible, level, 4) == null && crucible.step() == 4, "Made to its end: step " + crucible.step());
		Mixture made = crucible.mixture();
		Diagnostic noBottle = crucible.automate(level);
		helper.assertTrue(Diagnostic.of(Code.NEEDS_CONTAINER).equals(noBottle), "It waits for a bottle: " + noBottle);
		crucible.setItem(CrucibleBlockEntity.BOTTLE_SLOT, new ItemStack(Items.GLASS_BOTTLE));
		crucible.setItem(CrucibleBlockEntity.OUTPUT_SLOT, draught(NIGHT_EYE).copyWithCount(16));
		Diagnostic full = crucible.automate(level);
		Diagnostic still = crucible.automate(level);
		Diagnostic noRoom = Diagnostic.of(Code.INSUFFICIENT_CAPACITY);
		helper.assertTrue(noRoom.equals(full) && noRoom.equals(still) && noRoom.equals(crucible.waiting()), "A full output: " + full + " / " + still);
		helper.assertTrue(crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT).getCount() == 16 && crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT).getCount() == 1
				&& crucible.mixture().equals(made) && crucible.step() == 4, "Nothing voided or taken: " + crucible.mixture());
		crucible.setItem(CrucibleBlockEntity.OUTPUT_SLOT, otherBrew(3));
		Diagnostic differs = crucible.automate(level);
		ItemStack other = crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT);
		helper.assertTrue(Diagnostic.of(Code.OUTPUT_DIFFERS).equals(differs), "Another brew in its output: " + differs);
		helper.assertTrue(other.getCount() == 3 && otherBrew(1).get(JugcraftConcordance.BREW).equals(other.get(JugcraftConcordance.BREW))
				&& crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT).getCount() == 1 && crucible.mixture().equals(made), "The other brew is kept as it was: " + other);
		helper.assertValueEqual(signs.at(Sign.WANT, at), 3L, "one want for no bottle, one for no room, one for another brew");
		crucible.setItem(CrucibleBlockEntity.OUTPUT_SLOT, ItemStack.EMPTY);
		helper.assertTrue(crucible.automate(level) == null, "Emptied, it bottles");
		ItemStack out = crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT);
		helper.assertTrue(out.getCount() == 1 && ItemStack.isSameItemSameComponents(out, draught(NIGHT_EYE)) && crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT).isEmpty()
				&& crucible.mixture().isEmpty() && crucible.step() == 0 && crucible.waiting() == null, "One draught, and nothing waits: " + out);
		helper.assertValueEqual(signs.at(Sign.DONE, at), 1L, "a bottle filled");
		signs.close();
		helper.succeed();
	}

	/**
	 * C-01: two hoppers, one above and one at a side, race to fill the buffer and bottle slot. The formula runs twice on
	 * its two parts of water and takes exactly what it needs; the glowstone it does not need stays, and it then says it
	 * needs water.
	 */
	@GameTest(maxTicks = 600)
	public void twoHoppersRaceForTheBuffer(GameTestHelper helper) {
		BlockPos at = new BlockPos(2, 2, 2);
		CrucibleBlockEntity crucible = following(helper, at, NIGHT_EYE, alembist(helper, ResearchState.UNDERSTOOD));
		pour(crucible, FluidConstants.BOTTLE * 2);
		helper.setBlock(at.above(), Blocks.HOPPER);
		helper.setBlock(at.north(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.SOUTH));
		List<HopperBlockEntity> hoppers = List.of(helper.getBlockEntity(at.above(), HopperBlockEntity.class),
				helper.getBlockEntity(at.north(), HopperBlockEntity.class));
		for (HopperBlockEntity hopper : hoppers) {
			hopper.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, 2));
			hopper.setItem(1, new ItemStack(Items.GLASS_BOTTLE));
		}
		ItemStack expected = draught(NIGHT_EYE);
		helper.succeedWhen(() -> {
			ItemStack made = crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT);
			helper.assertTrue(made.getCount() == 2 && ItemStack.isSameItemSameComponents(made, expected),
					"Waiting for two draughts: " + made + " (step " + crucible.step() + ", " + crucible.waiting() + ")");
			helper.assertTrue(hoppers.stream().allMatch(HopperBlockEntity::isEmpty), "Both hoppers are emptied");
			int dust = 0;
			for (int i = 0; i < CrucibleBlockEntity.BUFFER_SLOTS; i++) {
				dust += crucible.getItem(i).is(Items.GLOWSTONE_DUST) ? crucible.getItem(i).getCount() : 0;
			}
			helper.assertTrue(dust == 2, "Four in, two used: " + dust + " left in the buffer");
			helper.assertTrue(crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT).isEmpty(), "Both bottles are used");
			helper.assertTrue(crucible.water.amount == 0 && crucible.mixture().isEmpty(), "Both parts of water are used");
			helper.assertTrue(Diagnostic.needsWater(1, 0).equals(crucible.waiting()), "It says it needs water: " + crucible.waiting());
		});
	}

	/** C-01: a tank short of a step's water waits (shown once) and draws nothing; topped up, it draws exactly the step's parts. */
	@GameTest(maxTicks = 20)
	public void partialWaterWaitsThenDrawsExactly(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = new BlockPos(2, 2, 2);
		SignWatch signs = new SignWatch(helper);
		CrucibleBlockEntity crucible = following(helper, at, TWO_NIGHT_EYES, alembist(helper, ResearchState.UNDERSTOOD));
		pour(crucible, FluidConstants.BOTTLE + FluidConstants.BOTTLE / 2);
		Diagnostic lacking = Diagnostic.needsWater(2, 1);
		Diagnostic first = crucible.automate(level);
		Diagnostic second = crucible.automate(level);
		helper.assertTrue(lacking.equals(first) && lacking.equals(second), "Two parts needed, one whole part held: " + first + " / " + second);
		helper.assertTrue(crucible.water.amount == FluidConstants.BOTTLE * 3 / 2 && crucible.mixture().isEmpty() && crucible.step() == 0,
				"Nothing is drawn: " + crucible.water.amount);
		helper.assertValueEqual(signs.at(Sign.WANT, at), 1L, "the want shown once");
		pour(crucible, FluidConstants.BOTTLE);
		helper.assertTrue(crucible.automate(level) == null && crucible.step() == 1 && crucible.mixture().parts() == 2, "Topped up, it pours two parts");
		helper.assertTrue(crucible.water.amount == FluidConstants.BOTTLE / 2 && crucible.water.variant.isOf(Fluids.WATER),
				"Exactly two parts drawn: " + crucible.water.amount + " left");
		signs.close();
		helper.succeed();
	}

	/**
	 * C-01: saved and read back between its two bottles, a two-part batch bottles its second dose once (the same as the
	 * first) and then waits for water for the next repeat.
	 */
	@GameTest(maxTicks = 20)
	public void aRestartJustBeforeOutputBottlesOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		CrucibleBlockEntity crucible = twoNightEyesMade(helper, new BlockPos(2, 2, 2));
		helper.assertTrue(crucible.automate(level) == null && crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT).getCount() == 1
				&& crucible.mixture().drawn() == 1, "One bottled: " + crucible.mixture());
		CompoundTag saved = crucible.saveWithoutMetadata(level.registryAccess());
		helper.assertTrue(saved.getIntOr("step", -1) == 5 && saved.getIntOr("drawn", -1) == 1 && saved.getIntOr("parts", -1) == 1,
				"Saved at its end with one part left: " + saved);
		crucible.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		helper.assertTrue(crucible.waiting() == null && crucible.step() == 5 && crucible.mixture().drawn() == 1
				&& crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT).getCount() == 1, "Read back as it was: step " + crucible.step());
		helper.assertTrue(crucible.automate(level) == null, "It bottles the second dose");
		Diagnostic next = crucible.automate(level);
		helper.assertTrue(Diagnostic.needsWater(2, 0).equals(next), "Then waits for water to repeat: " + next);
		ItemStack made = crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT);
		helper.assertTrue(made.getCount() == 2 && ItemStack.isSameItemSameComponents(made, draught(TWO_NIGHT_EYES)), "Two draughts, the same: " + made);
		helper.assertTrue(crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT).isEmpty() && crucible.getItem(0).isEmpty() && crucible.mixture().isEmpty(),
				"Exactly its bottles and glowstone were used");
		helper.succeed();
	}

	/**
	 * C-01: the Peepo port says FULL only when the crucible itself is stopped by its output (full, or another brew), not
	 * merely because the output holds its own draughts.
	 */
	@GameTest(maxTicks = 20)
	public void theCompanionAndTheCrucibleAgreeOnFull(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		CrucibleBlockEntity crucible = twoNightEyesMade(helper, new BlockPos(2, 2, 2));
		crucible.setItem(CrucibleBlockEntity.OUTPUT_SLOT, draught(TWO_NIGHT_EYES).copyWithCount(1));
		helper.assertTrue(crucible.automate(level) == null && crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT).getCount() == 2
				&& crucible.companion.status() != CompanionStatus.FULL && crucible.waiting() == null,
				"Its own draughts in the output are no reason to say FULL: " + crucible.companion.status());
		crucible.setItem(CrucibleBlockEntity.OUTPUT_SLOT, draught(TWO_NIGHT_EYES).copyWithCount(16));
		helper.assertTrue(Diagnostic.of(Code.INSUFFICIENT_CAPACITY).equals(crucible.automate(level)) && crucible.companion.status() == CompanionStatus.FULL,
				"A full output is FULL: " + crucible.companion.status());
		crucible.setItem(CrucibleBlockEntity.OUTPUT_SLOT, otherBrew(1));
		helper.assertTrue(Diagnostic.of(Code.OUTPUT_DIFFERS).equals(crucible.automate(level)) && crucible.companion.status() == CompanionStatus.FULL,
				"Another brew in the output is FULL: " + crucible.companion.status());
		helper.assertTrue(crucible.mixture().parts() == 1 && crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT).getCount() == 1, "Nothing more was bottled");
		helper.succeed();
	}

	/**
	 * C-01 (C-P5, C-P24): only someone who could change the block by hand works a crucible. In someone else's claim a
	 * player who knows more alchemy than its owner can pour, add, stir, bottle, set or clear nothing, but may still read
	 * it with a spoon or glass and record its process on a blank formula. Outside any claim, adventure mode refuses the
	 * same way (a protected town cannot be built in a game test).
	 */
	@GameTest(maxTicks = 20)
	public void onlyThoseWhoMayChangeACrucibleWorkIt(GameTestHelper helper) {
		BlockPos at = new BlockPos(2, 2, 2);
		CrucibleBlockEntity crucible = crucible(helper, at, true);
		crucible.setTemperature(120);
		ServerPlayer owner = alembist(helper, ResearchState.UNDERSTOOD);
		ServerPlayer stranger = alembist(helper, ResearchState.MASTERED);
		try (TestClaims claims = TestClaims.open()) {
			claims.block(helper.absolutePos(at), owner.getUUID());
			helper.assertTrue(use(crucible, owner, waterBottle()).consumesAction() && use(crucible, owner, new ItemStack(Items.GLOWSTONE_DUST)).consumesAction(),
					"The claim's owner pours water and adds glowstone");
			Mixture before = crucible.mixture();
			// The stranger's stick comes before any stir, so only the claim can refuse it (not the settling time).
			helper.assertTrue(use(crucible, stranger, waterBottle()) == InteractionResult.FAIL && stranger.getMainHandItem().is(Items.POTION),
					"A stranger pours no water");
			ItemStack dust = new ItemStack(Items.GLOWSTONE_DUST, 2);
			helper.assertTrue(use(crucible, stranger, dust) == InteractionResult.FAIL && dust.getCount() == 2, "adds nothing");
			helper.assertTrue(use(crucible, stranger, new ItemStack(Items.STICK)) == InteractionResult.FAIL, "stirs nothing");
			ItemStack glass = new ItemStack(Items.GLASS_BOTTLE);
			helper.assertTrue(use(crucible, stranger, glass) == InteractionResult.FAIL && glass.getCount() == 1
					&& take(stranger, JugcraftConcordance.DRAUGHT).isEmpty(), "bottles nothing");
			helper.assertTrue(use(crucible, stranger, written(NIGHT_EYE)) == InteractionResult.FAIL && crucible.program() == null, "and sets no formula");
			helper.assertTrue(crucible.mixture().equals(before), "Nothing in the crucible changed: " + crucible.mixture());
			helper.assertTrue(use(crucible, owner, new ItemStack(Items.STICK)).consumesAction() && crucible.mixture().history().size() == 3, "Its owner stirs");
			Mixture stirred = crucible.mixture();
			helper.assertTrue(use(crucible, stranger, new ItemStack(JugcraftConcordance.SAMPLING_SPOON)).consumesAction()
					&& use(crucible, stranger, new ItemStack(JugcraftConcordance.ASSAY_GLASS)).consumesAction(), "Anyone may read it");
			helper.assertTrue(use(crucible, stranger, new ItemStack(JugcraftConcordance.FORMULA_ITEM)).consumesAction()
					&& take(stranger, JugcraftConcordance.FORMULA_ITEM).get(JugcraftConcordance.FORMULA) != null && crucible.mixture().equals(stirred),
					"and record its process on a blank formula, changing nothing");
			helper.assertTrue(use(crucible, owner, written(NIGHT_EYE)).consumesAction() && crucible.program() != null, "Its owner sets a formula");
			crucible.useEmpty(stranger, true);
			helper.assertTrue(crucible.program() != null, "A stranger cannot clear it");
			crucible.useEmpty(owner, true);
			helper.assertTrue(crucible.program() == null, "Its owner can");
		}
		Mixture kept = crucible.mixture();
		stranger.setGameMode(GameType.ADVENTURE);
		helper.assertTrue(use(crucible, stranger, waterBottle()) == InteractionResult.FAIL && stranger.getMainHandItem().is(Items.POTION)
				&& crucible.mixture().equals(kept), "In adventure mode, outside any claim, nothing goes in");
		stranger.setGameMode(GameType.SURVIVAL);
		helper.assertTrue(use(crucible, stranger, waterBottle()).consumesAction() && crucible.mixture().parts() == 2,
				"Back in survival, outside the claim, the same player may pour");
		helper.succeed();
	}
}
