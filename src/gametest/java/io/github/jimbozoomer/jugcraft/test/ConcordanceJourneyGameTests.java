package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.Alchemy;
import io.github.jimbozoomer.jugcraft.concordance.Brew;
import io.github.jimbozoomer.jugcraft.concordance.CircleAnchorBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleStandBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.KindledLanternItem;
import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Band;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Formula;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Operation;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Product;
import io.github.jimbozoomer.jugcraft.concordance.courier.CourierLedger;
import io.github.jimbozoomer.jugcraft.concordance.courier.CourierPostBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Code;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Diagnostic;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Organism;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.progression.Route;
import io.github.jimbozoomer.jugcraft.concordance.progression.StageDefinition;
import io.github.jimbozoomer.jugcraft.concordance.progression.Stages;
import io.github.jimbozoomer.jugcraft.concordance.ritual.Readiness;
import io.github.jimbozoomer.jugcraft.concordance.ritual.RitualMachine;
import io.github.jimbozoomer.jugcraft.concordance.ritual.StructurePattern;
import io.github.jimbozoomer.jugcraft.concordance.rules.Evidence;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.spire.ConcordSpire;
import io.github.jimbozoomer.jugcraft.concordance.spire.SpireRecord;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ClockworkPorterEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import io.github.jimbozoomer.jugcraft.concordance.stages.StageProgress;
import io.github.jimbozoomer.jugcraft.fluid.JugcraftFluids;
import io.github.jimbozoomer.jugcraft.lair.MoltenSlagBlock;
import io.github.jimbozoomer.jugcraft.machine.JugcraftMachines;
import io.github.jimbozoomer.jugcraft.machine.MachineBlockEntity;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Roadmap step 31, the player journey (docs/features/arcane-concordance-journey.md), on a real server: each of the three
 * routes in tools/concordance_journey.py climbs the live stage rules its own way and founds its own Spire.
 * <p>
 * A fresh player understands First Light by ordinary evidence (three luminous specimens examined in the dark). The
 * route's other research is then granted in the states the route takes; each entry's own evidence and practice are
 * exercised by its system's tests. After each part the stage is the one the route expects, by the stage route it names
 * and not another. The Spire founded is the route's own, and a Spire whose research the route never took is refused.
 * Raising a Spire takes days and is played through by {@code ConcordanceSpireGameTests}; here its milestone is recorded
 * as raising records it, and the Architect stage follows.
 * <p>
 * Since Concordance Chunk C (C-10), the living workshop (tools/concordance_journey.py WORKSHOP): one survival player plays it
 * from a manual start with real blocks, and every recipe its rows name loads.
 */
public class ConcordanceJourneyGameTests {
	/** A route: what reaching Adept takes and by which stage route, what reaching Master takes, its Spire and the ones refused. */
	private record Journey(String name, List<String> adeptUnderstood, List<String> adeptMastered, String adept,
			List<String> masterUnderstood, List<String> masterMastered, String spire, List<String> refused) {
	}

	private static final Journey CULTIVATION = new Journey("cultivation", List.of("jugcraft:verdant_husbandry"),
			List.of("jugcraft:first_light", "jugcraft:verdant_husbandry"), "specialist",
			List.of("jugcraft:alembic_arts", "jugcraft:binding_arts", "jugcraft:circle_lore"),
			List.of("jugcraft:alembic_arts", "jugcraft:binding_arts", "jugcraft:circle_lore"), "jugcraft:verdant_spire",
			List.of("jugcraft:star_spire"));
	private static final Journey EXPLORATION = new Journey("exploration and combat",
			List.of("jugcraft:celestial_attunement", "jugcraft:crimson_rites", "jugcraft:relic_lore", "jugcraft:sympathy"), List.of(),
			"generalist", List.of("jugcraft:circle_lore"),
			List.of("jugcraft:first_light", "jugcraft:celestial_attunement", "jugcraft:crimson_rites", "jugcraft:relic_lore",
					"jugcraft:sympathy"), "jugcraft:star_spire", List.of("jugcraft:verdant_spire"));
	private static final Journey INFRASTRUCTURE = new Journey("crafting and infrastructure", List.of("jugcraft:circle_lore"),
			List.of("jugcraft:first_light"), "attuned", List.of("jugcraft:runesmithing", "jugcraft:assay", "jugcraft:binding_arts"),
			List.of("jugcraft:circle_lore", "jugcraft:runesmithing", "jugcraft:assay", "jugcraft:binding_arts"), "jugcraft:lantern_spire",
			List.of("jugcraft:verdant_spire", "jugcraft:star_spire"));

	private static String stage(ServerPlayer player) {
		StageDefinition stage = StageProgress.stage(player);
		return stage == null ? "none" : stage.id();
	}

	private static Route route(String stage, String id) {
		StageDefinition definition = StageProgress.catalog().stages().get(stage);
		if (definition == null) {
			throw new IllegalStateException("no stage " + stage);
		}
		return definition.routes().stream().filter(route -> route.id().equals(id)).findFirst()
				.orElseThrow(() -> new IllegalStateException("no route " + stage + "/" + id));
	}

	private static void grant(ServerPlayer player, List<String> research, ResearchState state) {
		for (String id : research) {
			ConcordanceProgress.grant(player, id, state);
		}
		StageProgress.refresh(player);
	}

	/** Which of the stage's routes hold for {@code player} now. */
	private static List<String> holding(ServerPlayer player, String stage) {
		Stages.Situation situation = StageProgress.situation(player);
		return StageProgress.catalog().stages().get(stage).routes().stream().filter(route -> Stages.missing(route, situation).isEmpty())
				.map(Route::id).toList();
	}

	private static void climb(GameTestHelper helper, Journey journey, BlockPos at) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		RateGate.forget(player.getUUID());
		for (String specimen : List.of("minecraft:amethyst_shard", "minecraft:glow_berries", "minecraft:glow_ink_sac")) {
			ConcordanceProgress.record(player, new Evidence.Examined(specimen, 0));
		}
		helper.assertValueEqual(ConcordanceProgress.knowledge(player).state("jugcraft:first_light"), ResearchState.UNDERSTOOD,
				journey.name() + ": three luminous specimens examined in the dark understand First Light");
		helper.assertValueEqual(stage(player), "practitioner", journey.name() + ": understanding First Light makes a Practitioner");

		grant(player, journey.adeptUnderstood(), ResearchState.UNDERSTOOD);
		grant(player, journey.adeptMastered(), ResearchState.MASTERED);
		helper.assertValueEqual(stage(player), "adept", journey.name() + ": its first part makes an Adept");
		helper.assertValueEqual(holding(player, "adept"), List.of(journey.adept()), journey.name() + ": by its own route alone");
		helper.assertValueEqual(holding(player, "master"), List.of(), journey.name() + ": and no Master yet");

		grant(player, journey.masterUnderstood(), ResearchState.UNDERSTOOD);
		grant(player, journey.masterMastered(), ResearchState.MASTERED);
		helper.assertValueEqual(stage(player), "master", journey.name() + ": its second part makes a Master");
		helper.assertTrue(holding(player, "master").contains("specialist"), journey.name() + ": five entries mastered in three traditions");

		BlockPos heart = helper.absolutePos(at);
		for (String other : journey.refused()) {
			helper.assertValueEqual(ConcordSpire.found(player, level, heart, other), "research",
					journey.name() + ": a Spire whose research the route never took is refused: " + other);
		}
		helper.assertValueEqual(ConcordSpire.found(player, level, heart, journey.spire()), "",
				journey.name() + ": the route's own Spire is founded: " + journey.spire());
		SpireRecord.of(level.getServer()).remove(ConcordSpire.id(level, heart));
		helper.assertValueEqual(holding(player, "architect"), List.of(), journey.name() + ": founding alone is not raising");
		StageProgress.milestone(player, ConcordSpire.MILESTONE);
		helper.assertValueEqual(stage(player), "architect", journey.name() + ": a raised Spire makes an Architect");
	}

	/** The three routes, each its own way to its own Spire. */
	@GameTest(maxTicks = 20)
	public void eachRouteClimbsItsOwnWay(GameTestHelper helper) {
		climb(helper, CULTIVATION, new BlockPos(1, 2, 1));
		climb(helper, EXPLORATION, new BlockPos(3, 2, 1));
		climb(helper, INFRASTRUCTURE, new BlockPos(5, 2, 1));
		helper.succeed();
	}

	// ---------------------------------------------------------------- the living workshop (Concordance Chunk C, C-10)

	/**
	 * Every recipe the workshop's rows name (tools/acquisition.py WORKSHOP and FACTORY; check_acquisition holds this list to
	 * the rows): the Thallite plate's is thallite_plate_by_hand; the Kindled Lantern is a bench working and the foundation a
	 * rite, so neither has one.
	 */
	private static final List<String> WORKSHOP_RECIPES = List.of("mendvetch", "crucible", "formula", "lampwright_bench", "ley_pylon",
			"circle_anchor", "warding_stone", "thallite_plate_by_hand", "crucible_stand", "courier_post", "clockwork_porter", "porter_key",
			"verdant_bed", "sunpetal", "dewmoss", "gloamcap", "coal_generator", "copper_cable", "machine_casing", "metal_press",
			"electric_pump", "bronze_fluid_pipe");

	// The yard (jugcraft-test:drone_depot, 16 by 8 by 16, all air; a stone floor at y 0 is laid first).
	/** The hand crucibles: the Kiln Draught's over a campfire, the poultice's over a magma block. */
	private static final BlockPos KILN = new BlockPos(2, 1, 2);
	private static final BlockPos POULTICE = new BlockPos(5, 1, 2);
	/** The Lesser Circle's anchor, and where its one participant stands inside it (no part there). */
	private static final BlockPos ANCHOR = new BlockPos(4, 1, 10);
	private static final BlockPos IN_CIRCLE = new BlockPos(5, 1, 11);
	/** The powered crucible's stand (the crucible above it), its coal generator and cable, the pump's spring and its generator. */
	private static final BlockPos STAND = new BlockPos(11, 1, 3);
	private static final BlockPos STAND_GENERATOR = new BlockPos(11, 1, 1);
	private static final BlockPos CABLE = new BlockPos(11, 1, 2);
	private static final BlockPos SPRING = new BlockPos(9, 1, 3);
	private static final BlockPos PUMP_GENERATOR = new BlockPos(9, 3, 3);
	/** A hopper over the powered crucible, holding its one bowl. */
	private static final BlockPos HOPPER = new BlockPos(11, 3, 3);
	/** The Courier Post, its chest, the pylon that fuels its porter, and where the porter is set down (all within its reach). */
	private static final BlockPos POST = new BlockPos(13, 1, 3);
	private static final BlockPos CHEST = new BlockPos(13, 1, 2);
	private static final BlockPos POST_PYLON = new BlockPos(14, 1, 3);
	private static final BlockPos PORTER_AT = new BlockPos(12, 1, 2);
	/** The garden: a Dewmoss and a Gloamcap two blocks apart (a mixed bed), and a Sunpetal to graft. */
	private static final BlockPos MOSS_BED = new BlockPos(10, 1, 11);
	private static final BlockPos CAP_BED = new BlockPos(12, 1, 11);
	private static final BlockPos SUN_BED = new BlockPos(10, 1, 14);
	private static final String DEWLIT = "jugcraft:dewlit";
	/** Root and Rivet's four steps and a margin (ConcordanceRitualGameTests.rootAndRivetAttunesOnePlateOnce waits the same). */
	private static final long RITE_DONE = RitualMachine.STEP_TICKS * 4L + 20;

	/** The top face of the block at {@code at}, as a use from above hits it. */
	private static BlockHitResult top(GameTestHelper helper, BlockPos at) {
		BlockPos absolute = helper.absolutePos(at);
		return new BlockHitResult(Vec3.atCenterOf(absolute).relative(Direction.UP, 0.5), Direction.UP, absolute, false);
	}

	/**
	 * {@code player} uses {@code held}, put in the main hand, on the top of the block at {@code at} by the server's own path
	 * (ServerPlayerGameMode.useItemOn: the block's use, then an empty hand's, then the item's), its rate limits forgotten.
	 */
	private static InteractionResult use(GameTestHelper helper, ServerPlayer player, BlockPos at, ItemStack held) {
		RateGate.forget(player.getUUID());
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return player.gameMode.useItemOn(player, helper.getLevel(), held, InteractionHand.MAIN_HAND, top(helper, at));
	}

	/** {@code player} places {@code held} on the top of the block at {@code at}, as its item's own use does (a block above it, a porter). */
	private static InteractionResult place(GameTestHelper helper, ServerPlayer player, BlockPos at, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return held.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, top(helper, at)));
	}

	/**
	 * What a crafting table makes of {@code grid} (three rows of three, null for an empty cell) by the server's own recipe
	 * matcher, asserted to be {@code count} of {@code item}; taking it uses up one of each ingredient.
	 */
	private static ItemStack craft(GameTestHelper helper, Item item, int count, ItemStack... grid) {
		List<ItemStack> cells = new ArrayList<>();
		for (ItemStack cell : grid) {
			cells.add(cell == null ? ItemStack.EMPTY : cell);
		}
		CraftingInput input = CraftingInput.of(3, 3, cells);
		Optional<RecipeHolder<CraftingRecipe>> recipe = RecipeManager.createCheck(RecipeType.CRAFTING).getRecipeFor(input, helper.getLevel());
		ItemStack made = recipe.map(holder -> holder.value().assemble(input)).orElse(ItemStack.EMPTY);
		helper.assertTrue(made.is(item) && made.getCount() == count, "A crafting table makes " + count + " " + item + ": " + made);
		for (ItemStack cell : cells) {
			if (!cell.isEmpty()) {
				cell.shrink(1);
			}
		}
		return made;
	}

	private static ItemStack of(Item item) {
		return new ItemStack(item);
	}

	private static Item item(String id) {
		return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
	}

	/** Takes the first stack of {@code item} out of {@code player}'s inventory (a product, a record or a return), or EMPTY. */
	private static ItemStack take(ServerPlayer player, Item item) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.is(item)) {
				player.getInventory().setItem(slot, ItemStack.EMPTY);
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	private static ItemStack fullLantern(ServerLevel level) {
		ItemStack lantern = new ItemStack(JugcraftConcordance.KINDLED_LANTERN);
		KindledLanternItem.set(lantern, KindledLanternItem.CAPACITY, ConcordanceProgress.now(level), false);
		return lantern;
	}

	private static Organism organism(String id) {
		Organism organism = Garden.catalog().organism(id);
		if (organism == null) {
			throw new IllegalStateException("no organism " + id);
		}
		return organism;
	}

	/**
	 * A fresh survival player standing in the circle: First Light understood by three luminous specimens examined in the
	 * dark, then, granted as the routes grant their research, the Alembic Arts, Circle Lore and the Binding Arts (still a
	 * Practitioner: four entries) and Verdant Husbandry (five entries in five traditions: the generalist's Adept), as
	 * tools/concordance_journey.py WORKSHOP's steps say.
	 */
	private static ServerPlayer workshopper(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos feet = helper.absolutePos(IN_CIRCLE);
		player.snapTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 0.0F, 0.0F);
		RateGate.forget(player.getUUID());
		for (String specimen : List.of("minecraft:amethyst_shard", "minecraft:glow_berries", "minecraft:glow_ink_sac")) {
			ConcordanceProgress.record(player, new Evidence.Examined(specimen, 0));
		}
		helper.assertValueEqual(stage(player), "practitioner", "the workshop: understanding First Light makes a Practitioner");
		grant(player, List.of(Alchemy.RESEARCH, "jugcraft:circle_lore", Workers.RESEARCH), ResearchState.UNDERSTOOD);
		helper.assertValueEqual(stage(player), "practitioner", "the workshop: four entries understood are still a Practitioner's");
		grant(player, List.of(Garden.RESEARCH), ResearchState.UNDERSTOOD);
		helper.assertValueEqual(stage(player), "adept", "the workshop: Verdant Husbandry makes the fifth entry in a fifth tradition");
		helper.assertValueEqual(holding(player, "adept"), List.of("generalist"), "the workshop: an Adept by the generalist's route alone");
		return player;
	}

	/** C-10: every recipe the workshop's rows name loads (check_acquisition holds WORKSHOP_RECIPES to tools/acquisition.py). */
	@GameTest(maxTicks = 20)
	public void theWorkshopsRecipesLoad(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<String> missing = new ArrayList<>();
		for (String name : WORKSHOP_RECIPES) {
			if (level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(name))).isEmpty()) {
				missing.add(name);
			}
		}
		helper.assertTrue(missing.isEmpty(), "These workshop recipes did not load: " + missing);
		helper.succeed();
	}

	/**
	 * Concordance Chunk C, C-10 (09:140): the living workshop from a manual start, played by one survival player with real
	 * blocks (tools/concordance_journey.py WORKSHOP, step by step). Given only vanilla materials, two Thallite ingots, two
	 * Kindled Lanterns kindled and infused full at a Lampwright's Bench (amethyst shards, ConcordanceGameTests) and a blank
	 * formula, they craft Mendvetch by the server's own recipe matcher; brew the Kiln Draught over a campfire and the
	 * Stillroot Poultice over a magma block by hand, recording the poultice's process; fill a Lesser Circle's pylons from a
	 * lantern and perform Root and Rivet; craft a Crucible Stand from its foundation and set a crafted crucible on it, fed
	 * JE by a coal generator through a cable and water by an electric pump; set the recorded formula, whose Mendvetch a
	 * Clockwork Porter (set down, keyed to a crafted Courier Post and fuelled by a lantern-filled pylon) brings by itself
	 * from a chest at the post; and lay a garden of crafted beds and seeds with a mixed bed and a Dewlit graft. Nothing is
	 * lost or made twice: every crafted Mendvetch is spent once, the post's chest gives exactly two, the courier ledger
	 * agrees with its cargo, and the powered poultice is the hand-made one; the stand pays nothing while the crucible waits
	 * for its Mendvetch.
	 */
	// The drone tests' 16 by 8 by 16 air box, reused: a structure file of its own would make tools/select_client_tests.py
	// count a shared file and run every client class.
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 900)
	public void theLivingWorkshopFromAManualStart(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
		ServerPlayer player = workshopper(helper);

		// Step 1, the manual start: Mendvetch crafted (four crafts, two each: two for the poultice, four for the rite, two for
		// the porter to bring); two crucibles over the heat each product stirs at.
		int crafted = 0;
		for (int i = 0; i < 4; i++) {
			crafted += craft(helper, Garden.MENDVETCH, 2, of(Items.WHEAT_SEEDS), of(Items.BONE_MEAL), of(Items.AMETHYST_SHARD), null, null, null,
					null, null, null).getCount();
		}
		ItemStack mendvetch = new ItemStack(Garden.MENDVETCH, crafted);
		helper.setBlock(KILN.below(), Blocks.CAMPFIRE);
		helper.setBlock(KILN, JugcraftConcordance.CRUCIBLE);
		helper.setBlock(POULTICE.below(), Blocks.MAGMA_BLOCK);
		helper.setBlock(POULTICE, JugcraftConcordance.CRUCIBLE);
		CrucibleBlockEntity kiln = helper.getBlockEntity(KILN, CrucibleBlockEntity.class);
		CrucibleBlockEntity poultice = helper.getBlockEntity(POULTICE, CrucibleBlockEntity.class);
		for (BlockPos at : List.of(KILN, POULTICE)) {
			helper.assertTrue(use(helper, player, at, PotionContents.createItemStack(Items.POTION, Potions.WATER)).consumesAction(),
					"A bottle of water goes into the crucible at " + at);
		}
		use(helper, player, KILN, of(Items.SUGAR));
		use(helper, player, KILN, of(Items.SUGAR));
		use(helper, player, POULTICE, mendvetch);
		use(helper, player, POULTICE, mendvetch);
		helper.assertTrue(mendvetch.getCount() == 6 && poultice.mixture().parts() == 1, "Two crafted Mendvetch went in: " + mendvetch.getCount());

		// Step 2: a Lesser Circle, its four pylons filled from one lantern, and Root and Rivet on a crafted plate.
		helper.setBlock(ANCHOR, JugcraftConcordance.CIRCLE_ANCHOR);
		List<BlockPos> pylons = new ArrayList<>();
		for (StructurePattern.Part part : ConcordanceData.rules().structure("jugcraft:lesser_circle").parts()) {
			BlockPos at = ANCHOR.offset(part.offset().x(), part.offset().y(), part.offset().z());
			if (part.role() == StructurePattern.Role.CHANNEL) {
				helper.setBlock(at, JugcraftConcordance.LEY_PYLON);
				pylons.add(at);
			} else if (part.role() == StructurePattern.Role.BOUNDARY) {
				helper.setBlock(at, JugcraftConcordance.WARDING_STONE);
			}
		}
		ItemStack lantern = fullLantern(level);
		for (BlockPos at : pylons) {
			use(helper, player, at, lantern);
			helper.assertValueEqual(helper.getBlockEntity(at, LeyPylonBlockEntity.class).ley(), 10L, "15 Radiance poured make 10 Ley Charge at " + at);
		}
		helper.assertValueEqual(KindledLanternItem.remaining(lantern, ConcordanceProgress.now(level)), 4, "four pours from one lantern");
		Item ingot = item("jugcraft:thallite_ingot");
		ItemStack plate = craft(helper, item("jugcraft:thallite_plate"), 1, of(ingot), null, null, of(ingot), null, null, null, null, null);
		use(helper, player, ANCHOR, plate);
		use(helper, player, ANCHOR, new ItemStack(Items.COPPER_INGOT, 4));
		use(helper, player, ANCHOR, mendvetch.split(4));
		use(helper, player, ANCHOR, ItemStack.EMPTY);
		CircleAnchorBlockEntity anchor = helper.getBlockEntity(ANCHOR, CircleAnchorBlockEntity.class);
		helper.assertTrue(anchor.phase() == RitualMachine.Phase.CHANNELING && "jugcraft:root_and_rivet".equals(anchor.run().ritual())
				&& ConcordanceProgress.currentFocus(player) == 20 - 6 && plate.isEmpty(), "Root and Rivet begins for six Focus: " + anchor.run());

		// Steps 3 and 4, laid out now: the factory's power and water, the hopper's bowl, the post and its porter.
		helper.setBlock(STAND_GENERATOR, JugcraftMachines.MACHINES.get(MachineKind.COAL_GENERATOR).defaultBlockState());
		helper.setBlock(CABLE, JugcraftMachines.COPPER_CABLE);
		helper.setBlock(PUMP_GENERATOR, JugcraftMachines.MACHINES.get(MachineKind.COAL_GENERATOR).defaultBlockState());
		for (BlockPos generator : List.of(STAND_GENERATOR, PUMP_GENERATOR)) {
			helper.getBlockEntity(generator, MachineBlockEntity.class).setItem(0, of(Items.COAL));
		}
		// A spring walled in stone (loose water would run into the next test), the pump on it and a pipe to the crucible's west.
		for (BlockPos wall : List.of(SPRING.west(), SPRING.east(), SPRING.north(), SPRING.south())) {
			helper.setBlock(wall, Blocks.STONE);
		}
		helper.setBlock(SPRING, Blocks.WATER);
		helper.setBlock(SPRING.above(), JugcraftFluids.ELECTRIC_PUMP);
		helper.setBlock(STAND.above().west(), JugcraftFluids.BRONZE_FLUID_PIPE);
		helper.setBlock(HOPPER, Blocks.HOPPER);
		helper.getBlockEntity(HOPPER, HopperBlockEntity.class).setItem(0, of(Items.BOWL));
		ItemStack post = craft(helper, item("jugcraft:courier_post"), 1, of(Items.OAK_PLANKS), of(Items.BARREL), of(Items.OAK_PLANKS),
				of(Items.OAK_PLANKS), of(Items.WRITABLE_BOOK), of(Items.OAK_PLANKS), of(Items.OAK_PLANKS), null, of(Items.OAK_PLANKS));
		helper.assertTrue(place(helper, player, POST.below(), post).consumesAction() && post.isEmpty()
				&& helper.getBlockEntity(POST, CourierPostBlockEntity.class).mayUse(player.getUUID()), "The crafted post is set down, its placer's");
		helper.setBlock(CHEST, Blocks.CHEST);
		ChestBlockEntity chest = helper.getBlockEntity(CHEST, ChestBlockEntity.class);
		chest.setItem(0, mendvetch.split(2));
		helper.setBlock(POST_PYLON, JugcraftConcordance.LEY_PYLON);
		use(helper, player, POST_PYLON, fullLantern(level));
		ItemStack porterItem = craft(helper, Workers.PORTER, 1, of(Items.COPPER_INGOT), of(Items.BARREL), of(Items.COPPER_INGOT),
				of(Items.COPPER_INGOT), of(Items.REDSTONE), of(Items.COPPER_INGOT), of(Items.IRON_INGOT), null, of(Items.IRON_INGOT));
		helper.assertTrue(place(helper, player, PORTER_AT.below(), porterItem).consumesAction() && porterItem.isEmpty(),
				"With the Binding Arts understood, a Clockwork Porter is set down");
		List<ClockworkPorterEntity> porters = level.getEntitiesOfClass(ClockworkPorterEntity.class, new AABB(helper.absolutePos(PORTER_AT)).inflate(1.0));
		helper.assertTrue(porters.size() == 1, "One porter stands by the post: " + porters);
		ClockworkPorterEntity porter = porters.get(0);
		ItemStack key = craft(helper, Workers.PORTER_KEY, 1, of(Items.COPPER_INGOT), of(Items.COPPER_INGOT), of(Items.IRON_NUGGET),
				null, null, null, null, null, null);
		place(helper, player, POST, key);
		key.getItem().interactLivingEntity(key, player, porter, InteractionHand.MAIN_HAND);
		helper.assertTrue(porter.post() != null && porter.post().pos().equals(helper.absolutePos(POST)), "The key binds the porter to the post");

		// Step 5: three crafted beds laid by a Greenwarden (awake), Dewmoss and Gloamcap two blocks apart, a Sunpetal grafted Dewlit.
		ItemStack beds = craft(helper, item("jugcraft:verdant_bed"), 3, of(Items.MOSS_BLOCK), of(Items.BONE_MEAL), of(Items.MOSS_BLOCK),
				of(Items.DIRT), of(Items.DIRT), of(Items.DIRT), null, null, null);
		for (BlockPos at : List.of(MOSS_BED, CAP_BED, SUN_BED)) {
			place(helper, player, at.below(), beds);
			helper.assertTrue(helper.getBlockEntity(at, VerdantBedBlockEntity.class).awake(), "A Greenwarden's bed wakes as it is laid at " + at);
		}
		ItemStack dewmoss = craft(helper, Garden.DEWMOSS, 2, of(Items.MOSS_BLOCK), of(Items.KELP), of(Items.AMETHYST_SHARD), null, null, null, null, null, null);
		ItemStack gloamcap = craft(helper, Garden.GLOAMCAP, 2, of(Items.BROWN_MUSHROOM), of(Items.GLOW_LICHEN), of(Items.AMETHYST_SHARD), null, null, null,
				null, null, null);
		ItemStack sunpetal = craft(helper, Garden.SUNPETAL, 2, of(Items.SUNFLOWER), of(Items.AMETHYST_SHARD), of(Items.BONE_MEAL), null, null, null, null,
				null, null);
		place(helper, player, MOSS_BED, dewmoss);
		place(helper, player, CAP_BED, gloamcap);
		place(helper, player, SUN_BED, sunpetal);
		helper.assertTrue(helper.getBlockState(MOSS_BED.above()).is(Garden.DEWMOSS_CROP) && helper.getBlockState(CAP_BED.above()).is(Garden.GLOAMCAP_CROP)
				&& helper.getBlockState(SUN_BED.above()).is(Garden.SUNPETAL_CROP), "Each crafted seed planted on its bed");
		VerdantBedBlockEntity sunBed = helper.getBlockEntity(SUN_BED, VerdantBedBlockEntity.class);
		helper.assertTrue(use(helper, player, SUN_BED.above(), dewmoss).consumesAction() && DEWLIT.equals(sunBed.graft()) && dewmoss.isEmpty(),
				"A Dewmoss used on the Sunpetal grafts Dewlit onto its bed and is spent: " + sunBed.graft());
		VerdantBedBlockEntity mossBed = helper.getBlockEntity(MOSS_BED, VerdantBedBlockEntity.class);
		VerdantBedBlockEntity capBed = helper.getBlockEntity(CAP_BED, VerdantBedBlockEntity.class);
		mossBed.forgetSample();
		capBed.forgetSample();

		// Step 1's stirs: the poultice warm (its magma block holds 60), the Kiln Draught hot (the campfire, 120), each a stick.
		for (int tick : new int[] {30, 41, 52, 63}) {
			helper.runAtTickTime(tick, () -> {
				helper.assertTrue(poultice.band() == Band.WARM, "The poultice's crucible stirs warm: " + poultice.temperature());
				helper.assertTrue(use(helper, player, POULTICE, of(Items.STICK)).consumesAction(), "A warm stir");
			});
		}
		for (int tick : new int[] {80, 91}) {
			helper.runAtTickTime(tick, () -> {
				helper.assertTrue(kiln.band() == Band.HOT, "The Kiln Draught's crucible stirs hot: " + kiln.temperature());
				helper.assertTrue(use(helper, player, KILN, of(Items.STICK)).consumesAction(), "A hot stir");
			});
		}
		List<Operation> process = new ArrayList<>(List.of(new Operation.Water(1), new Operation.Add("jugcraft:mendvetch", "jugcraft:raw"),
				new Operation.Add("jugcraft:mendvetch", "jugcraft:raw")));
		for (int i = 0; i < 4; i++) {
			process.add(new Operation.Stir(Band.WARM));
		}
		String written = new Formula(process).text();
		AtomicReference<ItemStack> recorded = new AtomicReference<>(ItemStack.EMPTY);
		AtomicReference<ItemStack> handMade = new AtomicReference<>(ItemStack.EMPTY);
		helper.runAtTickTime(70, () -> {
			use(helper, player, POULTICE, of(JugcraftConcordance.FORMULA_ITEM));
			recorded.set(take(player, JugcraftConcordance.FORMULA_ITEM));
			helper.assertTrue(written.equals(recorded.get().get(JugcraftConcordance.FORMULA)), "A blank formula records the poultice's process: "
					+ recorded.get().get(JugcraftConcordance.FORMULA));
			use(helper, player, POULTICE, of(Items.BOWL));
			ItemStack salve = take(player, JugcraftConcordance.SALVE);
			Product product = Alchemy.product(salve);
			helper.assertTrue(product != null && Alchemy.STILLROOT_POULTICE.equals(product.id()) && new Brew(List.of(new Brew.Dose("minecraft:regeneration", 1, 450,
					false)), "minecraft:regeneration@1").equals(salve.get(JugcraftConcordance.BREW)), "By hand, a Stillroot Poultice of Regeneration II: " + salve);
			handMade.set(salve.copy());
			ServerPlayer friend = helper.makeMockServerPlayerInLevel();
			friend.setGameMode(GameType.SURVIVAL);
			friend.setHealth(10.0F);
			player.setItemInHand(InteractionHand.MAIN_HAND, salve);
			RateGate.forget(player.getUUID());
			salve.getItem().interactLivingEntity(salve, player, friend, InteractionHand.MAIN_HAND);
			MobEffectInstance mending = friend.getEffect(MobEffects.REGENERATION);
			helper.assertTrue(mending != null && mending.getAmplifier() == 1 && mending.getDuration() == 450 && salve.isEmpty()
					&& player.getInventory().countItem(Items.BOWL) == 1, "On a friend it mends (Regeneration II, 450 ticks) and the bowl comes back: " + mending);
		});
		helper.runAtTickTime(102, () -> {
			use(helper, player, KILN, of(Items.GLASS_BOTTLE));
			ItemStack draught = take(player, JugcraftConcordance.DRAUGHT);
			Product product = Alchemy.product(draught);
			helper.assertTrue(product != null && "jugcraft:kiln_draught".equals(product.id()) && new Brew(List.of(new Brew.Dose("minecraft:fire_resistance", 0,
					1722, false)), "minecraft:fire_resistance@0").equals(draught.get(JugcraftConcordance.BREW)), "From sugar, a Kiln Draught of 1722 ticks: " + draught);
			draught.finishUsingItem(level, player);
			MobEffectInstance resistance = player.getEffect(MobEffects.FIRE_RESISTANCE);
			helper.assertTrue(resistance != null && resistance.getDuration() == 1722 && !MoltenSlagBlock.harms(player),
					"Drunk, it keeps the Cinder Kiln's slag off its drinker: " + resistance);
		});

		// Step 3: the rite done, the foundation crafted into a stand; a crafted crucible on it; the recorded formula set.
		AtomicReference<CrucibleBlockEntity> powered = new AtomicReference<>();
		helper.runAtTickTime(RITE_DONE, () -> {
			helper.assertTrue(anchor.phase() == RitualMachine.Phase.COMPLETE && anchor.output().is(JugcraftConcordance.ROOTED_THALLITE_FOUNDATION),
					"Root and Rivet made a foundation: " + anchor.run());
			helper.assertValueEqual(anchor.spent(), new Readiness.Spent(6, 32, 4, 0, 0), "spent in all: 6 Focus, 32 Ley Charge");
			for (BlockPos at : pylons) {
				helper.assertValueEqual(helper.getBlockEntity(at, LeyPylonBlockEntity.class).ley(), 2L, "8 of the lantern's 10 Ley Charge drawn at " + at);
			}
			use(helper, player, ANCHOR, ItemStack.EMPTY);
			ItemStack foundation = take(player, JugcraftConcordance.ROOTED_THALLITE_FOUNDATION);
			helper.assertTrue(foundation.getCount() == 1 && anchor.offerings().stream().allMatch(ItemStack::isEmpty), "The participant took it; nothing is left");
			ItemStack stand = craft(helper, JugcraftConcordance.CRUCIBLE_STAND.asItem(), 1, of(Items.IRON_INGOT), of(Items.COPPER_INGOT), of(Items.IRON_INGOT),
					of(Items.IRON_INGOT), of(Items.COPPER_INGOT), of(Items.IRON_INGOT), of(Items.IRON_INGOT), foundation, of(Items.IRON_INGOT));
			helper.assertTrue(foundation.isEmpty(), "The stand takes the foundation");
			ItemStack crucible = craft(helper, JugcraftConcordance.CRUCIBLE.asItem(), 1, of(Items.COPPER_INGOT), null, of(Items.COPPER_INGOT),
					of(Items.COPPER_INGOT), of(Items.CAULDRON), of(Items.COPPER_INGOT), of(Items.BRICKS), of(Items.BRICKS), of(Items.BRICKS));
			helper.assertTrue(place(helper, player, STAND.below(), stand).consumesAction() && place(helper, player, STAND, crucible).consumesAction()
					&& helper.getBlockState(STAND).is(JugcraftConcordance.CRUCIBLE_STAND) && helper.getBlockState(STAND.above()).is(JugcraftConcordance.CRUCIBLE),
					"The stand is set down and the crucible on it");
			CrucibleBlockEntity on = helper.getBlockEntity(STAND.above(), CrucibleBlockEntity.class);
			helper.assertTrue(use(helper, player, STAND.above(), recorded.get()).consumesAction() && on.program() != null
					&& player.getUUID().equals(on.formulaOwner()), "The recorded formula is set; its setter is the workshopper");
			powered.set(on);
		});

		// What the powered crucible, its stand and its porter do, tick by tick (Step 4): the request it files, and whether the
		// stand paid while the crucible waited for its Mendvetch.
		AtomicLong request = new AtomicLong();
		AtomicBoolean waited = new AtomicBoolean();
		AtomicBoolean paidWhileWaiting = new AtomicBoolean();
		AtomicBoolean heated = new AtomicBoolean();
		helper.onEachTick(() -> {
			CrucibleBlockEntity on = powered.get();
			if (on == null) {
				return;
			}
			boolean heats = helper.getBlockEntity(STAND, CrucibleStandBlockEntity.class).plan(level).heats();
			Diagnostic waiting = on.waiting();
			if (waiting != null && waiting.code() == Code.MISSING_INGREDIENT) {
				waited.set(true);
				paidWhileWaiting.compareAndSet(false, heats);
			}
			heated.compareAndSet(false, heats);
			if (request.get() == 0L && on.request() > 0L) {
				request.set(on.request());
			}
		});

		Organism moss = organism("jugcraft:dewmoss");
		Organism cap = organism("jugcraft:gloamcap");
		helper.succeedWhen(() -> {
			CrucibleBlockEntity on = powered.get();
			helper.assertTrue(on != null, "The stand is not set yet");
			ItemStack made = on.getItem(CrucibleBlockEntity.OUTPUT_SLOT);
			helper.assertTrue(made.getCount() == 1 && ItemStack.isSameItemSameComponents(made, handMade.get()),
					"Waiting for the powered poultice, the hand-made one's twin: " + made + " (step " + on.step() + ", " + on.temperature() + ", " + on.waiting() + ")");
			CourierLedger ledger = CourierLedger.of(level.getServer());
			helper.assertTrue(request.get() > 0L && ledger.ledger().get(request.get()) == null && ledger.problems().isEmpty(),
					"The crucible's request was filed, delivered and closed, and the ledger agrees with its cargo: " + request.get() + ", " + ledger.problems());
			helper.assertTrue(chest.countItem(Garden.MENDVETCH) == 0 && mendvetch.isEmpty(),
					"The porter brought exactly the chest's two; every crafted Mendvetch is spent once");
			helper.assertTrue(waited.get() && !paidWhileWaiting.get() && heated.get(),
					"The stand heated for the stirs and paid nothing while the crucible waited for its Mendvetch");
			helper.assertTrue(helper.getBlockEntity(HOPPER, HopperBlockEntity.class).isEmpty() && on.getItem(CrucibleBlockEntity.BOTTLE_SLOT).isEmpty(),
					"The hopper's one bowl was bottled");
			helper.assertTrue(mossBed.habitat(level) != null && mossBed.mixed(moss) && capBed.habitat(level) != null && capBed.mixed(cap),
					"Dewmoss and Gloamcap two blocks apart are a mixed bed, each way");
		});
	}
}
