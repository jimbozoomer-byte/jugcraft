package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.concordance.Alchemy;
import io.github.jimbozoomer.jugcraft.concordance.Brew;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.Reagent;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Axis;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Band;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Formula;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Heat;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Mixture;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;

/**
 * Roadmap step 13, experimental alchemy: a crucible takes its heat from the block beneath; the same process in two
 * crucibles makes the same mixture and the same draught, whose effect follows from what was put in; only someone who
 * understands the Alembic Arts may work one; the mortar prepares ingredients; a bowl makes a salve for a creature; a
 * recorded formula is followed by another crucible fed by pipe and hopper, making the same draught; and a crucible
 * saves its mixture and formula.
 */
public class ConcordanceAlchemyGameTests {
	private static final String GLOW = "minecraft:glowstone_dust";
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

	/** Water, one glowstone dust as it comes, and the crucible held hot: then two stirs, a little apart. */
	private static void startNightEye(GameTestHelper helper, CrucibleBlockEntity crucible, ServerPlayer player) {
		crucible.setTemperature(120);
		helper.assertTrue(use(crucible, player, waterBottle()).consumesAction() && crucible.mixture().parts() == 1, "A bottle of water is one part");
		helper.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE), "The empty bottle is handed back");
		use(crucible, player, new ItemStack(Items.GLOWSTONE_DUST));
		helper.assertTrue(crucible.mixture().pending().get(Axis.RADIANCE) == 1500, "Glowstone goes in undissolved");
		use(crucible, player, new ItemStack(Items.STICK));
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
	 * one made by hand. Hoppers reach only the buffer and bottle slot from above or the side, and the output from below.
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
					&& automated.canTakeItemThroughFace(CrucibleBlockEntity.OUTPUT_SLOT, ItemStack.EMPTY, Direction.DOWN),
					"Hoppers reach the buffer and bottles from above and the sides, and the output only from below");
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
}
