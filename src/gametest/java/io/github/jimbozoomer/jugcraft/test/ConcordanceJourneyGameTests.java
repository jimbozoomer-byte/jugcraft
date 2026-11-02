package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.Alchemy;
import io.github.jimbozoomer.jugcraft.concordance.Brew;
import io.github.jimbozoomer.jugcraft.concordance.CircleAnchorBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleStandBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.Examination;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.KindledLanternItem;
import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Band;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Formula;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Operation;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Product;
import io.github.jimbozoomer.jugcraft.concordance.assay.Assaying;
import io.github.jimbozoomer.jugcraft.concordance.celestial.Attunement;
import io.github.jimbozoomer.jugcraft.concordance.celestial.Calendar;
import io.github.jimbozoomer.jugcraft.concordance.celestial.Pattern;
import io.github.jimbozoomer.jugcraft.concordance.courier.CourierLedger;
import io.github.jimbozoomer.jugcraft.concordance.courier.CourierPostBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.courier.Couriers;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Code;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Diagnostic;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Organism;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.garden.Canopies;
import io.github.jimbozoomer.jugcraft.concordance.garden.OrganismCropBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.GleanerBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantHeartBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.journal.Journal;
import io.github.jimbozoomer.jugcraft.concordance.progression.Route;
import io.github.jimbozoomer.jugcraft.concordance.progression.StageDefinition;
import io.github.jimbozoomer.jugcraft.concordance.progression.Stages;
import io.github.jimbozoomer.jugcraft.concordance.resource.AstralLedger;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.ritual.Readiness;
import io.github.jimbozoomer.jugcraft.concordance.ritual.RitualMachine;
import io.github.jimbozoomer.jugcraft.concordance.ritual.StructurePattern;
import io.github.jimbozoomer.jugcraft.concordance.rules.Evidence;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstralClaims;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstrolabeItem;
import io.github.jimbozoomer.jugcraft.concordance.sky.ObservatoryBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.sky.Sky;
import io.github.jimbozoomer.jugcraft.concordance.spire.ConcordSpire;
import io.github.jimbozoomer.jugcraft.concordance.spire.SpireRecord;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BoundWills;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BuerEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.AmdusiasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BathinEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BifronsEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.CanopyShelter;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ForasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenReach;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GraftKeeping;
import io.github.jimbozoomer.jugcraft.concordance.spirits.HabitatReading;
import io.github.jimbozoomer.jugcraft.concordance.spirits.MaraxEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ClockworkPorterEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenPacts;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactHistory;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactKeeping;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactMenu;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactObligations;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactPerchBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactService;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactServices;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSpiritEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Roster;
import io.github.jimbozoomer.jugcraft.concordance.spirits.RootRunning;
import io.github.jimbozoomer.jugcraft.concordance.spirits.RosterRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.RosterStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.SpiritAnchorBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StarReckoning;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StolasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StolasRegard;
import io.github.jimbozoomer.jugcraft.concordance.spirits.VerdanceCarrying;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerRoster;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkshopHandEntity;
import io.github.jimbozoomer.jugcraft.concordance.stages.StageProgress;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import io.github.jimbozoomer.jugcraft.fluid.JugcraftFluids;
import io.github.jimbozoomer.jugcraft.lair.MoltenSlagBlock;
import io.github.jimbozoomer.jugcraft.machine.JugcraftMachines;
import io.github.jimbozoomer.jugcraft.machine.MachineBlockEntity;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import io.github.jimbozoomer.jugcraft.styx.JugcraftStyx;
import io.github.jimbozoomer.jugcraft.styx.StyxState;
import io.github.jimbozoomer.jugcraft.styx.Styxhexenhammer;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Inventory;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

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
 * <p>
 * Since Concordance Chunk D (D-09), the pact hall (tools/concordance_journey.py PACT): one ordinary survival player walks it
 * whole, from Styx's hint to Stolas's departure, and every recipe its rows name loads.
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
		ServerPlayer player = TestPlayers.mock(helper);
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
		ServerPlayer player = TestPlayers.mock(helper);
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
			ServerPlayer friend = TestPlayers.mock(helper);
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

	// ---------------------------------------------------------------- Concordance Chunk D, D-09: the pact hall, whole

	/**
	 * Every recipe the pact hall's rows name (tools/acquisition.py PACT_HALL; check_acquisition holds this list to the rows).
	 */
	private static final List<String> PACT_HALL_RECIPES = List.of("verdant_bed", "sunpetal", "assayers_scale", "observatory", "astrolabe",
			"pact_perch", "spirit_anchor", "courier_post", "porter_key");
	/** The garden cohort, GC-03: the recipes of tools/acquisition.py GARDEN_PACTS (check_acquisition holds the two equal); GC-04 adds the perch and Mendvetch, GC-05 the Verdant Heart and the Gleaner. */
	private static final List<String> GARDEN_RECIPES = List.of("sunpetal", "verdant_bed", "bonding_charm", "pact_perch", "mendvetch", "verdant_heart",
			"gleaner", "dewmoss");

	// The pact hall in the yard (jugcraft-test:drone_depot, its floor laid first). WorkshopHands' corner (x 1 to 3, z 2 to 5)
	// keeps the workshop hand's post, chest, crucible and anchor, as the hand's own tests place them.
	/** Where Styxhexenhammer stands, the perch, the observatory, the Verdant Bed (its crop above it) and the Assayer's Scale. */
	private static final BlockPos STYX_AT = new BlockPos(13, 1, 2);
	private static final BlockPos PERCH_AT = new BlockPos(10, 1, 10);
	private static final BlockPos OBSERVATORY_AT = new BlockPos(13, 1, 10);
	private static final BlockPos BED_AT = new BlockPos(6, 1, 13);
	private static final BlockPos SCALE_AT = new BlockPos(13, 1, 13);
	/** Inventory slots past the hotbar for what the holder carries, so a use with the main hand never touches them. */
	private static final int COMPACT_SLOT = 9;
	private static final int ASTROLABE_SLOT = 20;
	private static final int OFFERING_SLOT = 21;
	/** Noon by the world's clock, for Stolas's thinking (ConcordanceWorkerGameTests' NOON is private). */
	private static final long PACT_NOON = 6000L;

	/** What Styx says to {@code listener} after his own line, as translation keys (his line first, as StyxGameTests reads it). */
	private static List<String> styxHint(GameTestHelper helper, Styxhexenhammer styx, ServerPlayer listener) {
		List<String> said = styx.says(listener).stream().map(ConcordanceWorkerGameTests::key).toList();
		helper.assertTrue(!said.isEmpty() && said.getFirst().startsWith("message.jugcraft.styx.line."), "Styx's own line comes first: " + said);
		return said.subList(1, said.size());
	}

	/** The keys of the journal's pacts section for {@code player}, as Journal.build sends it; empty when it is not shown. */
	private static List<String> pactsSection(ServerPlayer player) {
		return Journal.build(player).stream().filter(section -> section.id().equals("pacts")).findFirst()
				.map(section -> section.lines().stream().map(line -> ConcordanceWorkerGameTests.key(line.text())).toList()).orElse(List.of());
	}

	/**
	 * Styx's hint and the journal's pacts section name the same next step of Stolas's Regard for {@code player}, Styx with the
	 * sky step while {@code sky} (none recorded), the journal with the sky step apart: the route with Styx and without him.
	 */
	private static void sameStep(GameTestHelper helper, Styxhexenhammer styx, ServerPlayer player, String step, boolean sky, String when) {
		List<String> hint = sky ? List.of("message.jugcraft.styx.stolas." + step, "message.jugcraft.styx.stolas.sky")
				: List.of("message.jugcraft.styx.stolas." + step);
		helper.assertValueEqual(styxHint(helper, styx, player), hint, when + ": Styx tells the holder their own next step");
		List<String> lines = pactsSection(player);
		helper.assertTrue(lines.size() >= 2 && lines.get(0).equals("journal.jugcraft.pacts.regard." + step)
				&& lines.get(1).equals(sky ? "journal.jugcraft.pacts.sky_needed" : "journal.jugcraft.pacts.sky_recorded"),
				when + ": the journal's pacts section names the same step without him, and the sky apart: " + lines);
	}

	/** D-09: every recipe the pact hall's rows name loads (check_acquisition holds PACT_HALL_RECIPES to tools/acquisition.py PACT_HALL). */
	@GameTest(maxTicks = 20)
	public void thePactHallsRecipesLoad(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<String> missing = new ArrayList<>();
		for (String name : PACT_HALL_RECIPES) {
			if (level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(name))).isEmpty()) {
				missing.add(name);
			}
		}
		helper.assertTrue(missing.isEmpty(), "These pact-hall recipes did not load: " + missing);
		helper.succeed();
	}

	/** The garden cohort, GC-03: every recipe the garden route's rows name loads (check_acquisition holds GARDEN_RECIPES to tools/acquisition.py GARDEN_PACTS). */
	@GameTest(maxTicks = 20)
	public void theGardenPactsRecipesLoad(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<String> missing = new ArrayList<>();
		for (String name : GARDEN_RECIPES) {
			if (level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(name))).isEmpty()) {
				missing.add(name);
			}
		}
		helper.assertTrue(missing.isEmpty(), "These garden recipes did not load: " + missing);
		helper.succeed();
	}

	/**
	 * Concordance Chunk D, D-09 (E7, 09:174; tools/concordance_journey.py PACT, step by step): the whole pact path on one
	 * ordinary survival player, in order. At each stage Styx tells them their own next step and the journal's pacts section
	 * names the same one without him. They examine a feather, harvest a ripe Sunpetal by hand and dissolve iron nuggets at an
	 * Assayer's Scale (no notes, never one of Styx's flowers); record the full moon at an observatory, which asks no research;
	 * keep that observatory once they understand Celestial Attunement and let it gather eight full moons' resonance. They
	 * craft a Pact Perch and place it; opening it, Stolas arrives and names every term; accepting with an empty astrolabe is
	 * refused with its reason and spends nothing; filled at their observatory, they accept and the Open Hand Compact is spent
	 * once. Star-reckoning recalls the full moon, once a game day, under the claim a master's recall makes; an observation
	 * while the pact is active keeps its first obligation and masters Stolas's Regard once, which lets them seal a workshop
	 * hand at an anchor bound to their Courier Post: it carries the crucible's own request once, its glowstone conserved.
	 * Unpaid, the pact is warned, graced a day and suspended with nothing taken; an offering resumes it; away past a due date
	 * and a grace, they come back to a warning with the whole grace. Ended from the roster, what was paid ahead comes back
	 * once, Stolas makes his departing gesture and is gone, the departure is kept, and the Bound Wills and the courier ledger
	 * stay sound. The path takes one tick; the test then waits out Stolas's departing gesture.
	 */
	// The drone tests' 16 by 8 by 16 air box, as theLivingWorkshopFromAManualStart reuses it (no new structure file).
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 80)
	public void thePactHallFromStyxToDeparture(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
		MinecraftServer server = level.getServer();
		PactDefinition offer = ConcordanceWorkerGameTests.stolasOffer();
		Pattern full = Sky.catalog().pattern(ConcordanceWorkerGameTests.FULL_MOON);
		ServerPlayer holder = TestPlayers.survivor(helper);
		UUID id = holder.getUUID();
		RateGate.forget(id);
		ConcordanceWorkerGameTests.beside(helper, holder, PERCH_AT);
		Inventory inventory = holder.getInventory();
		for (int i = 0; i < offer.compact().size(); i++) {
			PactDefinition.Cost cost = offer.compact().get(i);
			inventory.setItem(COMPACT_SLOT + i, new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(cost.item())), cost.count()));
		}
		ItemStack astrolabe = new ItemStack(Sky.ASTROLABE);
		inventory.setItem(ASTROLABE_SLOT, astrolabe);
		Styxhexenhammer styx = JugcraftStyx.WIZARD.create(level, EntitySpawnReason.COMMAND);
		helper.assertTrue(styx != null, "Styxhexenhammer creates on a server");
		Vec3 styxAt = helper.absoluteVec(Vec3.atBottomCenterOf(STYX_AT));
		styx.snapTo(styxAt.x, styxAt.y, styxAt.z, 0.0F, 0.0F);
		level.addFreshEntity(styx);
		helper.assertTrue(styxHint(helper, styx, holder).isEmpty() && pactsSection(holder).isEmpty() && StolasRegard.step(holder) == null,
				"A stranger to binding: Styx says only his line, and the journal has no pacts section");

		// PACT step 1: the Binding Arts understood (granted, as the journey grants a route's research); a feather examined.
		ConcordanceProgress.grant(holder, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(holder, Workers.RESEARCH, ResearchState.UNDERSTOOD);
		sameStep(helper, styx, holder, "examine", true, "The Binding Arts understood");
		Examination.forget(id);
		ItemStack feather = new ItemStack(Items.FEATHER);
		helper.assertTrue(Examination.examine(holder, level, feather) != null && feather.getCount() == 1
				&& ConcordanceWorkerGameTests.regard(holder) == ResearchState.ENCOUNTERED, "A feather examined begins Stolas's Regard; the feather stays");
		sameStep(helper, styx, holder, "cultivate", true, "A feather examined");

		// Step 2: Verdant Husbandry understood; a ripe Sunpetal on a Verdant Bed harvested by hand, by the server's own use.
		ConcordanceProgress.grant(holder, Garden.RESEARCH, ResearchState.UNDERSTOOD);
		helper.setBlock(BED_AT, Garden.VERDANT_BED.defaultBlockState().setValue(VerdantBedBlock.MOISTURE, VerdantBedBlock.WET));
		OrganismCropBlock sunpetal = (OrganismCropBlock) Garden.SUNPETAL_CROP;
		helper.setBlock(BED_AT.above(), sunpetal.getStateForAge(OrganismCropBlock.MAX_AGE));
		helper.assertTrue(use(helper, holder, BED_AT.above(), ItemStack.EMPTY).consumesAction() && !sunpetal.isMaxAge(helper.getBlockState(BED_AT.above()))
				&& ConcordanceWorkerGameTests.regard(holder) == ResearchState.OBSERVED, "A ripe Sunpetal harvested by hand: Stolas's Regard is observed");
		sameStep(helper, styx, holder, "assay", true, "A crop harvested");

		// Step 3: the Assay understood; nine iron nuggets weighed, then dissolved, at an Assayer's Scale.
		ConcordanceProgress.grant(holder, Assaying.RESEARCH, ResearchState.UNDERSTOOD);
		helper.setBlock(SCALE_AT, Assaying.SCALE);
		ItemStack sample = new ItemStack(Items.IRON_NUGGET, 9);
		use(helper, holder, SCALE_AT, sample);
		helper.assertTrue(sample.getCount() == 9, "Weighed at the scale, nothing is taken yet");
		use(helper, holder, SCALE_AT, sample);
		helper.assertTrue(sample.isEmpty() && Assaying.balance(holder) > 0L && ConcordanceWorkerGameTests.regard(holder) == ResearchState.UNDERSTOOD,
				"Dissolved there: Stolas's Regard is understood");
		Set<String> evidence = ConcordanceProgress.knowledge(holder).progress(StolasRegard.RESEARCH).evidence().keySet();
		helper.assertTrue(evidence.contains("examine:minecraft:feather")
				&& evidence.stream().anyMatch(key -> key.startsWith("practice:" + Garden.ACTIVITY + ":"))
				&& evidence.stream().anyMatch(key -> key.startsWith("practice:" + Assaying.ACTIVITY + ":"))
				&& evidence.stream().noneMatch(key -> key.startsWith("notes:")), "Its evidence is the holder's own three deeds, no notes: " + evidence);
		sameStep(helper, styx, holder, "serve", true, "A sample dissolved");

		// Step 4, the sky step: an observatory its holder places (no Starwatcher yet, so it keeps no one) records the full moon.
		ObservatoryBlockEntity observatory = ConcordanceWorkerGameTests.observatory(helper, OBSERVATORY_AT);
		Sky.OBSERVATORY.setPlacedBy(level, helper.absolutePos(OBSERVATORY_AT), helper.getBlockState(OBSERVATORY_AT), holder, ItemStack.EMPTY);
		long g = level.getGameTime();
		helper.assertTrue(observatory.keeper() == null && !StolasRegard.skySeen(holder), "Placed by one who is no Starwatcher, it keeps no one; no sky yet");
		helper.assertTrue(observatory.observe(holder, level, ConcordanceWorkerGameTests.NIGHT_ZERO, g, ConcordanceWorkerGameTests.ONLY_MOON) == 1
				&& StolasRegard.skySeen(holder), "The full moon observed there: the sky step, which asks no research");
		sameStep(helper, styx, holder, "serve", false, "An observation recorded");

		// Step 5: Celestial Attunement understood, they align that observatory by using it; it gathers eight full moons.
		ConcordanceProgress.grant(holder, Sky.RESEARCH, ResearchState.UNDERSTOOD);
		helper.assertTrue(use(helper, holder, OBSERVATORY_AT, ItemStack.EMPTY).consumesAction() && id.equals(observatory.keeper()),
				"A Starwatcher now, its holder keeps the observatory");
		for (int k = 0; k < ObservatoryBlockEntity.CAPACITY / full.resonance(); k++) {
			helper.assertValueEqual(observatory.gather(level, ConcordanceWorkerGameTests.NIGHT_ZERO + k * 8L * Pacts.DAY, g + k * Calendar.minGap(full),
					ConcordanceWorkerGameTests.ONLY_MOON), "gathering", "Full moon " + k + " pays its keeper once");
		}
		helper.assertValueEqual(observatory.resonance(), (long) ObservatoryBlockEntity.CAPACITY, "Its store is full");

		// Step 6: the perch, crafted by its recipe and placed by hand; Stolas invited; a refusal; acceptance.
		ItemStack perchItem = craft(helper, Workers.PACT_PERCH_ITEM, 1, of(Items.STICK), of(Items.AMETHYST_SHARD), of(Items.STICK), null,
				of(Items.DARK_OAK_LOG), null, null, of(Items.POLISHED_DEEPSLATE), null);
		helper.assertTrue(place(helper, holder, PERCH_AT.below(), perchItem).consumesAction() && perchItem.isEmpty()
				&& helper.getBlockState(PERCH_AT).is(Workers.PACT_PERCH), "The perch, crafted by its recipe, is placed by hand");
		PactPerchBlockEntity perch = helper.getBlockEntity(PERCH_AT, PactPerchBlockEntity.class);
		helper.assertTrue(id.equals(perch.holder().owner()), "It records its owner");
		helper.assertTrue(use(helper, holder, PERCH_AT, ItemStack.EMPTY).consumesAction() && holder.containerMenu instanceof PactMenu,
				"Its owner opens it: the pact screen");
		PactMenu menu = (PactMenu) holder.containerMenu;
		PactStatePayload invited = menu.lastSent();
		helper.assertTrue(invited != null && invited.mode() == PactStatePayload.NEGOTIATE && invited.revision() == 1 && invited.notes().isEmpty()
				&& invited.offers(PactRequestPayload.ACCEPT) && invited.terms().stream().map(PactStatePayload.Term::key).toList().equals(PactStatePayload.TERMS)
				&& ConcordanceWorkerGameTests.keys(invited.beats())
						.equals(offer.beats().stream().map(beat -> ConcordanceWorkerGameTests.PACT_KEY + "stolas.beat." + beat).toList()),
				"Invited: his three beats and every term before anything is agreed, and nothing in the way: " + invited);
		UUID visitorId = perch.visitor();
		StolasEntity visitor = visitorId != null && level.getEntity(visitorId) instanceof StolasEntity found ? found : null;
		helper.assertTrue(visitor != null && visitor.owner() == null && helper.absolutePos(PERCH_AT).equals(visitor.perch()) && visitor.listening()
				&& visitor.cues().equals(List.of("arrive")), "Stolas alights on the perch, a visitor bound to no one, and listens");
		helper.assertTrue(BoundWills.of(server).ledger().heldBy(id).isEmpty() && perch.holder().agreement() == null, "Nothing is stored yet");
		PactStatePayload refused = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(ConcordanceWorkerGameTests.answered(refused, Pacts.Outcome.CANNOT_AFFORD.id) && !refused.result().get().done()
				&& ConcordanceWorkerGameTests.said(refused).equals(ConcordanceWorkerGameTests.PACT_KEY + "answer.cannot_afford")
				&& Integer.valueOf(offer.resonance()).equals(ConcordanceWorkerGameTests.arg(refused.result().get().message(), 0)),
				"Accepting with an empty astrolabe is refused, and says why: the compact's resonance");
		helper.assertTrue(ConcordanceWorkerGameTests.pocketed(holder, 1) && AstrolabeItem.charge(astrolabe) == 0 && perch.holder().agreement() == null
				&& BoundWills.of(server).ledger().heldBy(id).isEmpty() && visitorId.equals(perch.visitor()) && !visitor.gone(),
				"Nothing is spent or sealed, and Stolas stays");
		helper.assertValueEqual(observatory.fill(holder, level, astrolabe), (long) AstrolabeItem.CAPACITY, "Their observatory fills their astrolabe");
		long sealedAt = level.getGameTime();
		PactStatePayload sealed = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(ConcordanceWorkerGameTests.answered(sealed, PactMenu.SEALED) && sealed.result().get().done() && sealed.mode() == PactStatePayload.KEEP,
				"Accepted: sealed, and kept here");
		int afterCompact = AstrolabeItem.CAPACITY - offer.resonance();
		helper.assertTrue(ConcordanceWorkerGameTests.pocketed(holder, 0) && AstrolabeItem.charge(astrolabe) == afterCompact,
				"The Open Hand Compact is spent once: its materials, and its resonance from the astrolabe");
		BoundWill will = ConcordanceWorkerGameTests.recordOf(level, holder);
		helper.assertTrue(will != null && PactDefinition.STOLAS.equals(will.counterpart()) && visitorId.equals(will.pact().presence())
				&& id.equals(visitor.owner()) && will.pact().upkeep().due() == sealedAt + offer.period() && ConcordanceWorkerGameTests.escrowed(level, will.id()) == 0
				&& "pact".equals(WorkerRoster.of(server).workers(id).get(visitorId).kind()),
				"One Bound Will, its presence the visitor, on the roster as a pact; the compact paid the first period: " + will);
		UUID record = will.id();
		PactStatePayload again = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(ConcordanceWorkerGameTests.answered(again, Pacts.Outcome.TAKEN.id) && ConcordanceWorkerGameTests.pocketed(holder, 0)
				&& AstrolabeItem.charge(astrolabe) == afterCompact && BoundWills.of(server).ledger().heldBy(id).size() == 1,
				"Accepting again seals and spends nothing");
		helper.assertTrue(BoundWills.of(server).problems().isEmpty(), "Sealed, the Bound Wills are sound: " + BoundWills.of(server).problems());

		// Step 7: Star-reckoning through the screen, once a game day, on the master's claim; the first obligation.
		long clock = Sky.time(level);
		List<PactStatePayload.Choice> patterns = again.patterns();
		int moon = IntStream.range(0, patterns.size()).filter(i -> patterns.get(i).id().equals(ConcordanceWorkerGameTests.FULL_MOON)).findFirst().orElse(-1);
		helper.assertTrue(moon >= 0 && patterns.get(moon).ready() && again.offers(PactRequestPayload.RECALL), "Kept here, the screen offers the full moon to recall");
		PactStatePayload recalled = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.RECALL, moon);
		Attunement attuned = holder.getAttached(Sky.ATTUNEMENT);
		int afterRecall = afterCompact - full.recallCost();
		helper.assertTrue(ConcordanceWorkerGameTests.answered(recalled, Pacts.Reckoning.RECALLED.id) && AstrolabeItem.charge(astrolabe) == afterRecall
				&& attuned != null && attuned.recalled() && ConcordanceWorkerGameTests.FULL_MOON.equals(attuned.pattern())
				&& attuned.until() == sealedAt + Calendar.RECALL_TICKS && ConcordanceWorkerGameTests.done(level, perch, sealedAt) == 1,
				"Star-reckoning: the full moon recalled at its recall cost, for 2,400 ticks, the game day's one");
		helper.assertTrue(new AstralLedger.Claim(Calendar.occurrence(full, clock), sealedAt)
				.equals(AstralClaims.of(server).ledger(id).claims().get(AstrolabeItem.recallKey(full))), "claimed under the key a master's recall claims");
		PactStatePayload twice = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.RECALL, moon);
		helper.assertTrue(ConcordanceWorkerGameTests.answered(twice, Pacts.Reckoning.QUOTA_SPENT.id) && AstrolabeItem.charge(astrolabe) == afterRecall,
				"A second the same game day: the quota is spent, and nothing with it");
		helper.assertTrue(StarReckoning.reckon(level, holder, perch.holder(), ConcordanceWorkerGameTests.FULL_MOON, clock, sealedAt + Pacts.DAY)
				== Pacts.Reckoning.RECALLED_ALREADY && AstrolabeItem.charge(astrolabe) == afterRecall
				&& ConcordanceWorkerGameTests.done(level, perch, sealedAt + Pacts.DAY) == 0,
				"The next game day the same occurrence is recalled already, by the one shared claim: nothing spent, no quota used");
		holder.closeContainer();
		// The workshop hand's post, chest and crucible (its request filed); an anchor bound to the post before the Regard is mastered.
		WorkshopHands.post(helper, holder, WorkshopHands.POST);
		ChestBlockEntity chest = WorkshopHands.chest(helper, WorkshopHands.CHEST, 10);
		CrucibleBlockEntity crucible = WorkshopHands.crucible(helper, WorkshopHands.CRUCIBLE, holder);
		long request = crucible.request();
		List<Long> requests = List.of(request);
		SpiritAnchorBlockEntity anchor = WorkshopHands.anchor(helper, holder, WorkshopHands.ANCHOR);
		helper.assertTrue(WorkshopHands.use(helper, holder, WorkshopHands.keyFor(helper, holder, WorkshopHands.POST), WorkshopHands.ANCHOR) == InteractionResult.SUCCESS
				&& anchor.post() != null, "They bind a Spirit Anchor to their Courier Post with a Porter Key"
						+ WorkshopHands.why(helper, holder, WorkshopHands.ANCHOR, WorkshopHands.POST));
		helper.assertTrue(WorkshopHands.UNKNOWN.equals(WorkshopHands.key(WorkshopHands.seal(helper, holder, anchor))) && anchor.agreement() == null,
				"Stolas's Regard understood, not mastered: no workshop hand yet");
		WorkshopHands.conserved(helper, requests, WorkshopHands.PLAIN, 10, 0, "the crucible's request filed", chest, crucible);
		helper.assertTrue(observatory.observe(holder, level, ConcordanceWorkerGameTests.NIGHT_ZERO, sealedAt + 200, ConcordanceWorkerGameTests.ONLY_MOON) == 1
				&& ConcordanceWorkerGameTests.regard(holder) == ResearchState.MASTERED && ConcordanceWorkerGameTests.served(holder) == 1 && StarReckoning.kept(holder)
				&& ConcordanceWorkerGameTests.earned(holder, ConcordanceProgress.advancementFor(StolasRegard.RESEARCH, ResearchState.MASTERED)),
				"An observation while the pact is active keeps its first obligation: Stolas's Regard is mastered");
		helper.assertTrue(observatory.observe(holder, level, ConcordanceWorkerGameTests.NIGHT_ZERO, sealedAt + 400, ConcordanceWorkerGameTests.ONLY_MOON) == 1
				&& ConcordanceWorkerGameTests.served(holder) == 1, "Once: a second observation keeps nothing more");
		sameStep(helper, styx, holder, "kept", false, "Stolas's Regard mastered");
		styx.discard();

		// Step 8: the workshop hand, unlocked by the mastery, carries the crucible's own request once.
		RateGate.forget(id);
		helper.assertValueEqual(WorkshopHands.key(WorkshopHands.seal(helper, holder, anchor)), WorkshopHands.SEALED, "Mastered: they seal a workshop hand there");
		WorkshopHandEntity hand = level.getEntity(anchor.spirit()) instanceof WorkshopHandEntity found ? found : null;
		helper.assertTrue(hand != null && id.equals(hand.owner()) && BoundWills.of(server).ledger().heldBy(id).size() == 2
				&& BoundWills.of(server).problems().isEmpty(), "Its own Bound Will beside the pact's; the records sound");
		UUID handRecord = anchor.agreement().record();
		WorkshopHands.stand(helper, hand, 2.5, 1.0, 3.5);
		long morning = WorkshopHands.morning(level, 5_200_000L);
		long in = WorkshopHands.inHours();
		CourierLedger ledger = CourierLedger.of(server);
		helper.assertTrue(hand.think(level, in, morning) == Status.TRAVELLING && hand.task() == request, "The hand claims the crucible's own request");
		WorkshopHands.conserved(helper, requests, WorkshopHands.PLAIN, 10, 0, "claimed", chest, crucible);
		helper.assertTrue(hand.think(level, in, morning + 20) == Status.WORKING && ledger.ledger().get(request).progress().carried() == 2
				&& WorkshopHands.matching(chest, WorkshopHands.PLAIN) == 8, "It picks up the two the crucible asked for");
		WorkshopHands.conserved(helper, requests, WorkshopHands.PLAIN, 10, 0, "carried", chest, crucible);
		helper.assertTrue(hand.think(level, in, morning + 40) == Status.WORKING && ledger.ledger().get(request) == null
				&& WorkshopHands.matching(crucible, WorkshopHands.PLAIN) == 2 && anchor.agreement().done(morning + 40) == 1,
				"and delivers them once: the request closed, one load counted");
		WorkshopHands.conserved(helper, requests, WorkshopHands.PLAIN, 10, 0, "delivered", chest, crucible);
		helper.assertTrue(ConcordanceProgress.knowledge(holder).progress(Workers.RESEARCH).evidence().containsKey("practice:" + Workers.ACTIVITY + ":spirit")
				&& ConcordanceProgress.knowledge(holder).state(Workers.RESEARCH) == ResearchState.UNDERSTOOD,
				"A spirit's service, one of the Binding Arts' three kinds, not mastering them by itself");

		// Step 9, keeping: due a week after sealing with nothing paid ahead; warned, a day's grace, then suspended, nothing taken.
		ConcordanceWorkerGameTests.beside(helper, holder, PERCH_AT);
		inventory.setItem(OFFERING_SLOT, new ItemStack(Items.AMETHYST_SHARD, 1));
		long due = sealedAt + ConcordanceWorkerGameTests.PERIOD_7;
		helper.assertTrue(ConcordanceWorkerGameTests.stay(level, perch.holder(), sealedAt + 1_000L, due - 1_000L).isEmpty(), "Nothing falls due before its week");
		helper.assertValueEqual(perch.holder().look(level, due), Pacts.Event.WARNED, "Due with none paid ahead: a warning");
		helper.assertTrue(ConcordanceWorkerGameTests.pactOf(level, perch).pact().upkeep().graceUntil() == due + Pacts.DAY && perch.holder().held(level) == null
				&& ConcordanceWorkerGameTests.stolas(level, perch).think(level, PACT_NOON, due) == Status.UPKEEP_DUE,
				"A day of grace, the service still on; Stolas says an offering is due");
		helper.assertTrue(ConcordanceWorkerGameTests.stay(level, perch.holder(), due + 1_000L, due + Pacts.DAY - 1_000L).isEmpty(), "Graced: nothing more within the day");
		helper.assertValueEqual(perch.holder().look(level, due + Pacts.DAY), Pacts.Event.SUSPENDED, "Unpaid at its end: suspended for upkeep");
		helper.assertTrue(perch.holder().held(level) == Status.UPKEEP_HELD
				&& ConcordanceWorkerGameTests.stolas(level, perch).think(level, PACT_NOON, due + Pacts.DAY) == Status.UPKEEP_HELD
				&& StarReckoning.reckon(level, holder, perch.holder(), ConcordanceWorkerGameTests.FULL_MOON, clock, due + Pacts.DAY) == Pacts.Reckoning.UPKEEP_HELD
				&& AstrolabeItem.charge(astrolabe) == afterRecall, "The service stops: Stolas says so and reckons nothing");
		helper.assertTrue(ConcordanceWorkerGameTests.shards(holder) == 1 && ConcordanceWorkerGameTests.escrowed(level, record) == 0
				&& BoundWills.of(server).ledger().get(record) != null && BoundWills.of(server).problems().isEmpty(),
				"Nothing is taken: the shard they carry is still theirs, and the pact stays");
		long resumedAt = due + Pacts.DAY + 500L;
		helper.assertValueEqual(PactKeeping.deposit(level, holder, perch.holder(), Items.AMETHYST_SHARD, resumedAt), 1, "They offer the shard they carry");
		helper.assertTrue(perch.holder().held(level) == null
				&& ConcordanceWorkerGameTests.pactOf(level, perch).pact().upkeep().due() == resumedAt + ConcordanceWorkerGameTests.PERIOD_7
				&& ConcordanceWorkerGameTests.shards(holder) == 0 && ConcordanceWorkerGameTests.escrowed(level, record) == 0
				&& ConcordanceWorkerGameTests.stolas(level, perch).think(level, PACT_NOON, resumedAt) != Status.UPKEEP_HELD,
				"It pays the period held and resumes at once, the next a whole week on: one shard given, one taken, none ahead");
		// Away past the next due date and past a grace; back, the whole grace.
		TestPlayers.leave(holder);
		helper.assertTrue(ConcordanceWorkerGameTests.stay(level, perch.holder(), resumedAt + 1_000L, resumedAt + ConcordanceWorkerGameTests.PERIOD_7 + 3 * Pacts.DAY).isEmpty()
				&& perch.holder().held(level) == null && !ConcordanceWorkerGameTests.pactOf(level, perch).pact().upkeep().warned(),
				"Away past the next due date and a grace: nothing is looked at, warned or held");
		ServerPlayer back = TestPlayers.again(helper, id);
		RateGate.forget(id);
		ConcordanceWorkerGameTests.beside(helper, back, PERCH_AT);
		long returned = resumedAt + ConcordanceWorkerGameTests.PERIOD_7 + 3 * Pacts.DAY + 500L;
		helper.assertValueEqual(perch.holder().look(level, returned), Pacts.Event.WARNED, "Back, they are warned, never suspended at once");
		helper.assertTrue(ConcordanceWorkerGameTests.pactOf(level, perch).pact().upkeep().graceUntil() == returned + Pacts.DAY && perch.holder().held(level) == null
				&& ConcordanceWorkerGameTests.stolas(level, perch).think(level, PACT_NOON, returned) == Status.UPKEEP_DUE
				&& ConcordanceWorkerGameTests.regard(back) == ResearchState.MASTERED, "with the whole day of grace from their return; nothing of theirs lost");
		back.getInventory().setItem(OFFERING_SLOT, new ItemStack(Items.AMETHYST_SHARD, 3));
		helper.assertValueEqual(PactKeeping.deposit(level, back, perch.holder(), Items.AMETHYST_SHARD, returned + 500L), 3, "They pay three");
		helper.assertTrue(ConcordanceWorkerGameTests.escrowed(level, record) == 2 && ConcordanceWorkerGameTests.shards(back) == 0
				&& ConcordanceWorkerGameTests.pactOf(level, perch).pact().upkeep().due() == returned + ConcordanceWorkerGameTests.PERIOD_7
				&& !ConcordanceWorkerGameTests.pactOf(level, perch).pact().upkeep().warned() && BoundWills.of(server).problems().isEmpty(),
				"One for the period due, two kept ahead: four shards given in all, two taken by upkeep, two kept, none lost");

		// Ending, from the roster (D-07): the escrow back once, Stolas departs, the departure kept.
		RosterStatePayload listing = ConcordanceWorkerGameTests.roster(back);
		RosterStatePayload.Row row = ConcordanceWorkerGameTests.rowOf(listing, record);
		helper.assertTrue(row != null && row.offers(RosterRequestPayload.END) && ConcordanceWorkerGameTests.key(row.name()).equals("entity.jugcraft.stolas")
				&& ConcordanceWorkerGameTests.rowOf(listing, handRecord) != null, "Their roster lists the pact, end offered, and the workshop hand");
		StolasEntity presence = ConcordanceWorkerGameTests.stolas(level, perch);
		RosterStatePayload ended = ConcordanceWorkerGameTests.act(back, row, RosterRequestPayload.END);
		Component told = ConcordanceWorkerGameTests.result(ended);
		helper.assertTrue(ConcordanceWorkerGameTests.answered(ended, "ended") && ConcordanceWorkerGameTests.key(told).equals("message.jugcraft.concordance.pact.departed")
				&& Integer.valueOf(2).equals(ConcordanceWorkerGameTests.arg(told, 2)), "Ended from the roster: told who departs, why, and that two came back");
		helper.assertTrue(ConcordanceWorkerGameTests.shards(back) == 2 && BoundWills.of(server).escrow(record).isEmpty() && BoundWills.of(server).ledger().get(record) == null
				&& perch.holder().agreement() == null && helper.getBlockState(PERCH_AT).is(Workers.PACT_PERCH),
				"What was paid ahead comes back once (four given: two taken, two returned); the record goes; the perch stays");
		helper.assertTrue(presence != null && presence.departing() && presence.gone() && !presence.isRemoved()
				&& !WorkerRoster.of(server).workers(id).containsKey(presence.getUUID()) && perch.stolas(level) == null
				&& presence.cues().equals(List.of("arrive", "refuse", "assent", "inspect", "depart")),
				"Stolas makes his departing gesture, struck off at once; his cues in order: " + (presence == null ? "none" : presence.cues()));
		List<Pacts.Departure> history = PactHistory.of(server).history(id);
		helper.assertTrue(history.size() == 1 && record.equals(history.getFirst().record()) && Pacts.ENDED.equals(history.getFirst().reason())
				&& Couriers.place(level, helper.absolutePos(PERCH_AT)).equals(history.getFirst().place()), "The departure is kept once, with its reason and place: " + history);
		helper.assertValueEqual(pactsSection(back), List.of("journal.jugcraft.pacts.regard.kept", "journal.jugcraft.pacts.sky_recorded",
				"journal.jugcraft.pacts.kept", "journal.jugcraft.pacts.departed"), "The journal: the Regard kept, the sky recorded, the hand kept, Stolas's departure");
		helper.assertTrue(ConcordanceWorkerGameTests.rowOf(ended, record) == null && ConcordanceWorkerGameTests.rowOf(ended, handRecord) != null
				&& Roster.act(back, Roster.RECORD, record, RosterRequestPayload.END, level.getGameTime()).reason().equals("nothing")
				&& ConcordanceWorkerGameTests.shards(back) == 2 && PactHistory.of(server).history(id).size() == 1, "Ending again returns nothing more");
		helper.assertTrue(!StyxState.get(level).claimed(id) && ConcordanceWorkerGameTests.count(back, "jugcraft:stolas_starflower") == 0
				&& ConcordanceWorkerGameTests.count(back, "jugcraft:ravenquill_lupine") == 0
				&& ConcordanceWorkerGameTests.count(back, "jugcraft:amethyst_mourningbell") == 0,
				"The whole way without Styx's gift: a feather examined and amethyst offered, never one of his flowers");
		helper.assertTrue(BoundWills.of(server).problems().isEmpty() && CourierLedger.of(server).problems().isEmpty(),
				"The Bound Wills and the courier ledger are sound: " + BoundWills.of(server).problems() + ", " + CourierLedger.of(server).problems());
		helper.runAfterDelay(StolasEntity.Gesture.DEPART.ticks + 2, () -> {
			helper.assertTrue(presence.isRemoved() && ConcordanceWorkerGameTests.stolases(helper).isEmpty(), "His gesture over, Stolas is gone from the world");
			helper.succeed();
		});
	}

	// GC-04: Buer's journey in the drone tests' 16 x 8 x 16 box: the Regard's harvest bed far (10 and 10) from his perch, his bed
	// and the holder's own bed beside it.
	private static final BlockPos GARDEN_PERCH_AT = new BlockPos(3, 1, 3);
	private static final BlockPos HARVEST_AT = new BlockPos(13, 1, 13);
	private static final BlockPos BUER_BED_AT = new BlockPos(6, 1, 3);
	private static final BlockPos HAND_BED_AT = new BlockPos(3, 1, 6);
	/** Inventory slots past the hotbar for Buer's compact (three crafts of Mendvetch, then the bone meal), so a use with the main hand never touches them. */
	private static final int GARDEN_COMPACT_SLOT = 9;

	/** {@code player}'s state of The Garden's Regard. */
	private static ResearchState garden(ServerPlayer player) {
		return ConcordanceProgress.knowledge(player).state(GardenPacts.RESEARCH);
	}

	/** A wet Verdant Bed set at {@code at}, woken by {@code player}'s empty hand (the server's own use): awake and kept by them. */
	private static void wake(GameTestHelper helper, ServerPlayer player, BlockPos at) {
		helper.setBlock(at, Garden.VERDANT_BED.defaultBlockState().setValue(VerdantBedBlock.MOISTURE, VerdantBedBlock.WET));
		VerdantBedBlockEntity bed = helper.getBlockEntity(at, VerdantBedBlockEntity.class);
		helper.assertTrue(use(helper, player, at, ItemStack.EMPTY).consumesAction() && bed.awake() && player.getUUID().equals(bed.keeper()),
				"A Verdant Bed woken by its Greenwarden's hand is awake and kept by them");
	}

	/**
	 * Concordance garden cohort, GC-04 (GE7; the journey's GARDEN route, steps 1 to 6): Buer from The Garden's Regard to his
	 * departure, one ordinary survival player, in order, without Styx or Stolas. A Sunpetal examined, a ripe one harvested by
	 * hand on a bed they woke, a bound worker's service: the Regard understood. A Pact Perch crafted and placed by hand far
	 * from that bed: Buer does not come ("nothing here I could mend") and nothing is spent. A bed woken beside it: he comes,
	 * rooted, and names every term; accepting short of the compact is refused with its reason and spends nothing; with 4
	 * Mendvetch (crafted) and 4 bone meal it seals once and spends once. By daylight he eases one strain; a bed worked back to
	 * its worst is overworked, refused until bone meal by hand composts it, then mended; their own empty hand on a strained
	 * bed in his area keeps his first obligation once. Both Mendvetch left over paid ahead (he looks them over); suspended, he
	 * tends nothing; resumed; ended at the perch, the Mendvetch come back once, he closes and sinks away, the departure is kept
	 * once, and ending again returns nothing. The path takes one tick; the test then waits out his departing gesture. Since
	 * GC-05 the owner's open meets the chooser first (Buer and Bathin share The Garden's Regard), and Buer is chosen there by
	 * his sign (GC-P59); ConcordanceWorkerGameTests and the garden client class show the chooser itself (GC-P29).
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 80)
	public void buerFromTheCodexToDeparture(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
		MinecraftServer server = level.getServer();
		PactDefinition offer = ConcordanceWorkerGameTests.buerOffer();
		ServerPlayer holder = TestPlayers.survivor(helper);
		UUID id = holder.getUUID();
		RateGate.forget(id);
		ConcordanceWorkerGameTests.beside(helper, holder, GARDEN_PERCH_AT);

		// Steps 1 to 3 (GC-03's): The Garden's Regard, from the holder's own deeds.
		ConcordanceProgress.grant(holder, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(holder, Workers.RESEARCH, ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(holder, Garden.RESEARCH, ResearchState.UNDERSTOOD);
		Examination.forget(id);
		helper.assertTrue(Examination.examine(holder, level, new ItemStack(Garden.SUNPETAL)) != null && garden(holder) == ResearchState.ENCOUNTERED,
				"A Sunpetal examined begins The Garden's Regard");
		wake(helper, holder, HARVEST_AT);
		OrganismCropBlock sunpetal = (OrganismCropBlock) Garden.SUNPETAL_CROP;
		helper.setBlock(HARVEST_AT.above(), sunpetal.getStateForAge(OrganismCropBlock.MAX_AGE));
		helper.assertTrue(use(helper, holder, HARVEST_AT.above(), ItemStack.EMPTY).consumesAction() && garden(holder) == ResearchState.OBSERVED,
				"A ripe Sunpetal harvested by hand: observed");
		// A bound familiar's mending records the Binding Arts' practice (as HearthlingEntity records it; its own tests mend).
		ConcordanceProgress.record(holder, new Evidence.Practiced(Workers.ACTIVITY, "familiar"));
		helper.assertTrue(garden(holder) == ResearchState.UNDERSTOOD, "A bound worker's service: The Garden's Regard is understood");

		// Step 4: the perch, crafted by its recipe and placed by hand; no bed of theirs within 8: he does not come.
		ItemStack perchItem = craft(helper, Workers.PACT_PERCH_ITEM, 1, of(Items.STICK), of(Items.AMETHYST_SHARD), of(Items.STICK), null,
				of(Items.DARK_OAK_LOG), null, null, of(Items.POLISHED_DEEPSLATE), null);
		helper.assertTrue(place(helper, holder, GARDEN_PERCH_AT.below(), perchItem).consumesAction() && helper.getBlockState(GARDEN_PERCH_AT).is(Workers.PACT_PERCH),
				"The perch, crafted by its recipe, is placed by hand");
		PactPerchBlockEntity perch = helper.getBlockEntity(GARDEN_PERCH_AT, PactPerchBlockEntity.class);
		helper.assertTrue(use(helper, holder, GARDEN_PERCH_AT, ItemStack.EMPTY).consumesAction() && holder.containerMenu instanceof PactMenu, "Its owner opens it");
		PactStatePayload far = chooseBuer(helper, holder, (PactMenu) holder.containerMenu);
		helper.assertTrue(far.mode() == PactStatePayload.NEGOTIATE && ConcordanceWorkerGameTests.keys(far.notes()).contains(ConcordanceWorkerGameTests.TENDING_KEY + "missing")
				&& perch.visitor() == null, "Their only bed 10 blocks off: he does not come, and says why");
		holder.closeContainer();
		wake(helper, holder, BUER_BED_AT);
		helper.assertTrue(use(helper, holder, GARDEN_PERCH_AT, ItemStack.EMPTY).consumesAction() && holder.containerMenu instanceof PactMenu, "Opened again");
		PactMenu menu = (PactMenu) holder.containerMenu;
		PactStatePayload invited = chooseBuer(helper, holder, menu);
		helper.assertTrue(invited.mode() == PactStatePayload.NEGOTIATE && invited.notes().isEmpty() && invited.offers(PactRequestPayload.ACCEPT)
				&& invited.terms().stream().map(PactStatePayload.Term::key).toList().equals(PactStatePayload.TERMS)
				&& ConcordanceWorkerGameTests.keys(invited.beats()).equals(offer.beats().stream()
						.map(beat -> ConcordanceWorkerGameTests.PACT_KEY + "buer.beat." + beat).toList()),
				"With a bed of theirs beside it: his three beats and every term, nothing in the way: " + invited);
		BuerEntity buer = perch.visitor() != null && level.getEntity(perch.visitor()) instanceof BuerEntity found ? found : null;
		helper.assertTrue(buer != null && buer.owner() == null && helper.absolutePos(GARDEN_PERCH_AT).equals(buer.perch()) && buer.listening()
				&& buer.cues().equals(List.of("arrive")), "Buer unfurls on the perch, a visitor bound to no one, and listens");
		PactStatePayload refused = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(ConcordanceWorkerGameTests.answered(refused, Pacts.Outcome.CANNOT_AFFORD.id)
				&& ConcordanceWorkerGameTests.said(refused).equals(ConcordanceWorkerGameTests.PACT_KEY + "answer.cannot_afford.items")
				&& BoundWills.of(server).ledger().heldBy(id).isEmpty() && !buer.gone(), "Short of the compact: refused, nothing spent, he stays");
		// The compact past the hotbar (main-inventory slots, not the hand): a use with the main hand below never touches it.
		for (int i = 0; i < 3; i++) {
			holder.getInventory().setItem(GARDEN_COMPACT_SLOT + i, craft(helper, Garden.MENDVETCH, 2, of(Items.WHEAT_SEEDS), of(Items.BONE_MEAL),
					of(Items.AMETHYST_SHARD), null, null, null, null, null, null));
		}
		holder.getInventory().setItem(GARDEN_COMPACT_SLOT + 3, new ItemStack(Items.BONE_MEAL, 4));
		PactStatePayload sealed = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.ACCEPT, 0);
		BoundWill will = ConcordanceWorkerGameTests.recordOf(level, holder);
		helper.assertTrue(ConcordanceWorkerGameTests.answered(sealed, PactMenu.SEALED) && sealed.mode() == PactStatePayload.KEEP
				&& ConcordanceWorkerGameTests.count(holder, "jugcraft:mendvetch") == 2 && ConcordanceWorkerGameTests.count(holder, "minecraft:bone_meal") == 0
				&& will != null && BuerEntity.COUNTERPART.equals(will.counterpart()) && buer.getUUID().equals(will.pact().presence())
				&& id.equals(buer.owner()) && ConcordanceWorkerGameTests.escrowed(level, will.id()) == 0,
				"Sealed once: 4 Mendvetch and 4 bone meal spent once, one Bound Will, its presence Buer");
		UUID record = will.id();
		holder.closeContainer();

		// Step 5: by daylight he eases one strain; the overworked bed; the holder's own tending, his first obligation.
		VerdantBedBlockEntity bed = helper.getBlockEntity(BUER_BED_AT, VerdantBedBlockEntity.class);
		// Early in a game day (the unit review): his memory of today's eases is cleared at a day's turn, so the overworked
		// step must not cross one.
		long t = ConcordanceWorkerGameTests.dayStart(level, 1);
		bed.setStrain(2);
		helper.assertValueEqual(buer.think(level, PACT_NOON, t), Status.WORKING, "By day he eases one strain");
		helper.assertValueEqual(bed.strain(), 1, "2 to 1");
		bed.setStrain(3);
		helper.assertValueEqual(buer.think(level, PACT_NOON, t + Garden.TEND_TICKS), Status.IDLE, "Worked back to 3: overworked, refused");
		helper.assertTrue(use(helper, holder, BUER_BED_AT, new ItemStack(Items.BONE_MEAL)).consumesAction() && bed.strain() == 2, "Bone meal by hand composts it");
		helper.assertValueEqual(ConcordanceWorkerGameTests.count(holder, "jugcraft:mendvetch"), 2, "the hand's bone meal touched no Mendvetch");
		helper.assertValueEqual(buer.think(level, PACT_NOON, t + 2L * Garden.TEND_TICKS), Status.WORKING, "and he mends it again");
		wake(helper, holder, HAND_BED_AT);
		helper.getBlockEntity(HAND_BED_AT, VerdantBedBlockEntity.class).setStrain(2);
		use(helper, holder, HAND_BED_AT, ItemStack.EMPTY);
		helper.assertTrue(helper.getBlockEntity(HAND_BED_AT, VerdantBedBlockEntity.class).strain() == 1 && PactObligations.kept(holder, BuerEntity.COUNTERPART)
				&& garden(holder) == ResearchState.UNDERSTOOD, "Their own tending in his area keeps his first obligation (mastery needs three spirits)");

		// Step 6: an offering paid ahead; suspended and resumed; ended at the perch, the Mendvetch back once.
		helper.assertTrue(use(helper, holder, GARDEN_PERCH_AT, ItemStack.EMPTY).consumesAction() && holder.containerMenu instanceof PactMenu, "Kept here, opened");
		menu = (PactMenu) holder.containerMenu;
		int slot = ConcordanceWorkerGameTests.offering(menu.lastSent(), "jugcraft:mendvetch");
		PactStatePayload paid = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.DEPOSIT, slot);
		// A deposit pays every Mendvetch carried that the escrow has room for (4 ahead; PactKeeping.deposit), not one per request.
		helper.assertTrue(slot >= 0 && ConcordanceWorkerGameTests.answered(paid, PactMenu.DEPOSITED) && ConcordanceWorkerGameTests.escrowed(level, record) == 2
				&& ConcordanceWorkerGameTests.count(holder, "jugcraft:mendvetch") == 0 && "inspect".equals(buer.cues().getLast()),
				"Both Mendvetch they carry paid ahead; he looks them over: " + ConcordanceWorkerGameTests.escrowed(level, record) + " held, "
						+ ConcordanceWorkerGameTests.count(holder, "jugcraft:mendvetch") + " carried, " + buer.cues());
		helper.assertTrue(ConcordanceWorkerGameTests.answered(ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.SUSPEND, 0), "suspended"), "Suspended");
		bed.setStrain(2);
		helper.assertValueEqual(buer.think(level, PACT_NOON, t + 3L * Garden.TEND_TICKS), Status.SUSPENDED, "Suspended, he tends nothing");
		helper.assertValueEqual(bed.strain(), 2, "untouched");
		helper.assertTrue(ConcordanceWorkerGameTests.answered(ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.RESUME, 0), "resumed")
				&& buer.cues().stream().filter("assent"::equals).count() == 1,
				"Resumed (his assent falls within CUE_TICKS of the seal's, one tick: shown once)");
		PactStatePayload ended = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.END, 0);
		helper.assertTrue(ConcordanceWorkerGameTests.answered(ended, "ended") && ended.result().get().done()
				&& ConcordanceWorkerGameTests.count(holder, "jugcraft:mendvetch") == 2 && BoundWills.of(server).escrow(record).isEmpty()
				&& BoundWills.of(server).ledger().get(record) == null && buer.departing() && buer.gone()
				&& !WorkerRoster.of(server).workers(id).containsKey(buer.getUUID()) && "depart".equals(buer.cues().getLast()),
				"Ended: the Mendvetch back once, the record gone, he closes and sinks away, struck off at once");
		List<Pacts.Departure> history = PactHistory.of(server).history(id);
		helper.assertTrue(history.size() == 1 && record.equals(history.getFirst().record()) && Pacts.ENDED.equals(history.getFirst().reason()),
				"The departure is kept once: " + history);
		helper.assertTrue(!ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.END, 0).result().map(PactStatePayload.Result::done).orElse(false)
				&& ConcordanceWorkerGameTests.count(holder, "jugcraft:mendvetch") == 2, "Ending again returns nothing more");
		helper.assertTrue(BoundWills.of(server).problems().isEmpty(), "The Bound Wills are sound");
		helper.runAfterDelay(BuerEntity.Gesture.DEPART.ticks + 5, () -> {
			helper.assertTrue(buer.isRemoved(), "His departure over, he is gone from the world");
			helper.succeed();
		});
	}

	/**
	 * GC-05 (GC-P59): with Buer and Bathin both offered to a holder of The Garden's Regard, the owner's open shows the chooser
	 * first; Buer is chosen there by his sign, as his entry sends INVITE. The state then shown (Buer's offer).
	 */
	private static PactStatePayload chooseBuer(GameTestHelper helper, ServerPlayer holder, PactMenu menu) {
		if (menu.lastSent().mode() == PactStatePayload.INVITE) {
			List<PactStatePayload.Choice> listed = menu.lastSent().patterns();
			int index = -1;
			for (int i = 0; i < listed.size(); i++) {
				if (listed.get(i).id().equals(BuerEntity.SIGN)) {
					index = i;
				}
			}
			helper.assertTrue(index >= 0 && ConcordanceWorkerGameTests.answered(ConcordanceWorkerGameTests.invite(holder, menu, index), PactMenu.INVITED),
					"The chooser lists Buer, and he is invited from it: " + listed.stream().map(PactStatePayload.Choice::id).toList());
		}
		return menu.lastSent();
	}

	// GC-10 unit 7
	// ---------------------------------------------------------------- The garden cohort, GC-10 (GC-P143): the garden pacts, whole

	// The whole path in the drone tests' 16 x 8 x 16 box (Buer's journey's floor and perch). Every cell is yard-relative; the
	// perch's west cells (x 1 and 2 at z 3) stay clear for a standing spirit's station (Station.BESIDE), and the holder
	// stands two south of the perch (x 3, z 5) throughout.
	/** The perch; the Regard's harvest bed, 11 off it (beyond every garden spirit's area: 6 to 10), so no spirit ever counts it. */
	private static final BlockPos PATH_PERCH = new BlockPos(3, 1, 3);
	private static final BlockPos PATH_HARVEST = new BlockPos(14, 1, 14);
	/** Buer's bed (3 east of the perch, within his 8) and the holder's own strained bed for his first obligation (within it too). */
	private static final BlockPos PATH_BUER_BED = new BlockPos(6, 1, 3);
	private static final BlockPos PATH_HAND_BED = new BlockPos(3, 1, 6);
	/**
	 * Bathin's (within his 10): a Heart and a Gleaner 7 apart (beyond the Gleaner's own reach of 4), then a Heart with its own
	 * road (heartWithItsOwnRoad: its pylon east, its empty Gleaner 5 west, both within 10 and beyond 4 of every other Heart).
	 */
	private static final BlockPos PATH_HEART = new BlockPos(0, 1, 0);
	private static final BlockPos PATH_GLEANER = new BlockPos(7, 1, 7);
	private static final BlockPos PATH_ROADED_HEART = new BlockPos(10, 1, 1);
	/** Marax's planted bed (within his 8), later Foras's sampled host; Foras's store touching the perch (north) and the host he grafts by hand. */
	private static final BlockPos PATH_READ_BED = new BlockPos(5, 1, 5);
	private static final BlockPos PATH_STORE = new BlockPos(3, 1, 2);
	private static final BlockPos PATH_HAND_HOST = new BlockPos(6, 1, 1);
	/** Bifrons's full bed and hungry bed (within his 8). */
	private static final BlockPos PATH_FULL_BED = new BlockPos(6, 1, 6);
	private static final BlockPos PATH_HUNGRY_BED = new BlockPos(0, 1, 6);
	/** Amdusias's log (east of the perch: within 2), his Sunpetal and his Gloamcap (within his 6). */
	private static final BlockPos PATH_LOG = new BlockPos(4, 1, 3);
	private static final BlockPos PATH_SUNPETAL = new BlockPos(5, 1, 6);
	private static final BlockPos PATH_GLOAMCAP = new BlockPos(1, 1, 7);
	/** Night by the world's clock, for Bifrons (his hours are 13,000 to 23,000 ticks). */
	private static final long PACT_NIGHT = 18000L;
	/**
	 * The tick Marax and the later spirits come: Marax's bed is planted at tick 0 and sampled one request a tick from tick 1
	 * (the shared sampling rule of ConcordanceGardenGameTests.afterSampling, which succeeds the test and so is not called).
	 */
	private static final int PATH_LATER = 6;
	/** The test's length: the later spirits' tick, the longest departure (max of the six DEPART gestures) and a margin. */
	private static final int GARDEN_PATH_TICKS = 200;

	/** Each garden spirit's own missing key, in route order: what each says from the bare chooser. */
	private static final List<String> PATH_REFUSALS = List.of(ConcordanceWorkerGameTests.TENDING_KEY + "missing",
			ConcordanceWorkerGameTests.CARRYING_KEY + "missing", ConcordanceWorkerGameTests.READING_KEY + "missing",
			ConcordanceWorkerGameTests.GRAFTING_KEY + "missing.store", ConcordanceWorkerGameTests.RUNNING_KEY + "missing",
			ConcordanceWorkerGameTests.CANOPY_KEY + "missing.tree");

	/**
	 * The garden cohort, GC-10 (GC-P143; tools/concordance_journey.py GARDEN, all 22 steps): the garden pacts whole on one
	 * ordinary survival player, without Styx or Stolas, at one perch. The Garden's Regard understood by their own deeds; the
	 * perch crafted and placed by hand; its chooser lists the six garden spirits, none ready, and each, chosen from it in turn,
	 * says what is missing and does not come, nothing spent; Buer, his bed woken, refuses a holder short of his compact. Then
	 * in route order Buer, Bathin, Marax, Foras, Bifrons and Amdusias are each chosen, name every term, are sealed with their
	 * compacts (Mendvetch, Sunpetal and Dewmoss crafted by their recipes) spent once, serve once and meet their conflict (an
	 * overworked bed, a Heart with its own road, a proud familiar, a host a graft would not help, a growing full bed, a
	 * Gloamcap that wants the stir), and their first obligations are kept by hand: Buer's and Bathin's leave the Regard
	 * understood, Marax's masters it, and the later three record nothing more. Each pact is ended with one offering paid
	 * ahead, which comes back exactly once, and the next spirit is chosen only then. Six departures are kept in order, the
	 * Bound Wills stay sound, and every spirit is gone once its departing gesture ends. Marax's bed takes its first reading
	 * over the first ticks (a reading never samples), so the path from Marax on runs at tick PATH_LATER.
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = GARDEN_PATH_TICKS)
	public void theGardenPactsFromCodexToDeparture(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		int departing = Math.max(Math.max(Math.max(BuerEntity.Gesture.DEPART.ticks, BathinEntity.Gesture.DEPART.ticks),
				Math.max(MaraxEntity.Gesture.DEPART.ticks, ForasEntity.Gesture.DEPART.ticks)),
				Math.max(BifronsEntity.Gesture.DEPART.ticks, AmdusiasEntity.Gesture.DEPART.ticks));
		helper.assertTrue(PATH_LATER + departing + 5 < GARDEN_PATH_TICKS, "The test's length covers the later spirits and the longest departure");
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
		MinecraftServer server = level.getServer();
		ServerPlayer holder = TestPlayers.survivor(helper);
		UUID id = holder.getUUID();
		RateGate.forget(id);
		ConcordanceWorkerGameTests.beside(helper, holder, PATH_PERCH);

		// Steps 1 to 3: The Garden's Regard, from the holder's own deeds (as Buer's journey).
		ConcordanceProgress.grant(holder, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(holder, Workers.RESEARCH, ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(holder, Garden.RESEARCH, ResearchState.UNDERSTOOD);
		Examination.forget(id);
		helper.assertTrue(Examination.examine(holder, level, new ItemStack(Garden.SUNPETAL)) != null && garden(holder) == ResearchState.ENCOUNTERED,
				"Step 1: a Sunpetal examined begins The Garden's Regard");
		wake(helper, holder, PATH_HARVEST);
		helper.setBlock(PATH_HARVEST.above(), ((OrganismCropBlock) Garden.SUNPETAL_CROP).getStateForAge(OrganismCropBlock.MAX_AGE));
		helper.assertTrue(use(helper, holder, PATH_HARVEST.above(), ItemStack.EMPTY).consumesAction() && garden(holder) == ResearchState.OBSERVED,
				"Step 2: a ripe Sunpetal harvested by hand: observed");
		ConcordanceProgress.record(holder, new Evidence.Practiced(Workers.ACTIVITY, "familiar"));
		helper.assertTrue(garden(holder) == ResearchState.UNDERSTOOD, "Step 3: a bound worker's service: The Garden's Regard is understood");

		// Step 4's perch, crafted and placed by hand; its chooser lists the six garden spirits (no Stolas: no Stolas's Regard), none ready.
		ItemStack perchItem = craft(helper, Workers.PACT_PERCH_ITEM, 1, of(Items.STICK), of(Items.AMETHYST_SHARD), of(Items.STICK), null,
				of(Items.DARK_OAK_LOG), null, null, of(Items.POLISHED_DEEPSLATE), null);
		helper.assertTrue(place(helper, holder, PATH_PERCH.below(), perchItem).consumesAction() && helper.getBlockState(PATH_PERCH).is(Workers.PACT_PERCH),
				"Step 4: the perch, crafted by its recipe, is placed by hand");
		PactPerchBlockEntity perch = helper.getBlockEntity(PATH_PERCH, PactPerchBlockEntity.class);
		helper.assertTrue(use(helper, holder, PATH_PERCH, ItemStack.EMPTY).consumesAction() && holder.containerMenu instanceof PactMenu, "Its owner opens it");
		List<PactDefinition> garden = PactPerchBlockEntity.offered().stream().filter(d -> d.research().equals(GardenPacts.RESEARCH)).toList();
		helper.assertValueEqual(garden.stream().map(PactDefinition::id).toList(), List.of(BuerEntity.COUNTERPART, BathinEntity.COUNTERPART,
				MaraxEntity.COUNTERPART, ForasEntity.COUNTERPART, BifronsEntity.COUNTERPART, AmdusiasEntity.COUNTERPART),
				"The six garden spirits, in route order");
		PactStatePayload chooser = ((PactMenu) holder.containerMenu).lastSent();
		helper.assertTrue(chooser.mode() == PactStatePayload.INVITE
				&& chooser.patterns().stream().map(PactStatePayload.Choice::id).toList().equals(ConcordanceWorkerGameTests.signsOf(garden))
				&& chooser.patterns().stream().noneMatch(PactStatePayload.Choice::ready) && chooser.patterns().stream().allMatch(choice -> choice.amount() >= 1)
				&& perch.visitor() == null, "The chooser lists the six by their signs, none ready, each with something missing: " + chooser.patterns());
		holder.closeContainer();
		// Every refusal once: each chosen from the bare chooser says what it lacks (its service's own missing list) and does not come.
		for (int i = 0; i < garden.size(); i++) {
			PactDefinition d = garden.get(i);
			int carried = ConcordanceWorkerGameTests.carried(holder);
			RateGate.forget(id);
			PactStatePayload refused = ConcordanceWorkerGameTests.offerOf(helper, holder, perch, d).lastSent();
			List<String> missing = PactServices.get(d.work()).missing(level, holder, perch, d).stream()
					.map(m -> ConcordanceWorkerGameTests.key(m.note())).toList();
			List<String> notes = ConcordanceWorkerGameTests.keys(refused.notes());
			helper.assertTrue(missing.contains(PATH_REFUSALS.get(i)) && notes.containsAll(missing) && perch.visitor() == null
					&& ConcordanceWorkerGameTests.carried(holder) == carried && BoundWills.of(server).ledger().heldBy(id).isEmpty(),
					d.id() + " says what is missing (" + PATH_REFUSALS.get(i) + ") and does not come; nothing spent: " + notes);
			holder.closeContainer();
		}

		List<PactSpiritEntity<?>> gone = new ArrayList<>();
		pathBuer(helper, holder, perch, gone);
		pathBathin(helper, holder, perch, gone);
		// Marax's bed, planted now (his step's fixture), takes its first reading over the next ticks: a reading never samples.
		VerdantBedBlockEntity read = ConcordanceWorkerGameTests.plantedBed(helper, PATH_READ_BED, id, Garden.SUNPETAL_CROP, 0);
		for (int t = 1; t < PATH_LATER; t++) {
			helper.runAtTickTime(t, () -> {
				if (read.cachedHabitat(level) == null) {
					read.habitat(level);
				}
			});
		}
		helper.runAtTickTime(PATH_LATER, () -> {
			helper.assertTrue(read.cachedHabitat(level) != null, "Marax's bed holds its first reading");
			pathMarax(helper, holder, perch, read, gone);
			Map<String, Long> mastered = Map.copyOf(ConcordanceProgress.knowledge(holder).progress(GardenPacts.RESEARCH).evidence());
			pathForas(helper, holder, perch, read, mastered, gone);
			pathBifrons(helper, holder, perch, mastered, gone);
			pathAmdusias(helper, holder, perch, mastered, gone);
			// The end of the path: six departures kept in order, the Bound Wills sound.
			List<String> departed = PactHistory.of(server).history(id).stream()
					.map(departure -> Pacts.counterpart(departure.counterpart())).toList();
			helper.assertTrue(departed.equals(garden.stream().map(PactDefinition::id).toList())
					&& PactHistory.of(server).history(id).stream().allMatch(departure -> Pacts.ENDED.equals(departure.reason())),
					"Six departures kept, in route order, each ended: " + departed);
			helper.assertTrue(BoundWills.of(server).problems().isEmpty() && BoundWills.of(server).ledger().heldBy(id).isEmpty(),
					"The Bound Wills are sound, and no pact is held");
			helper.runAfterDelay(departing + 5, () -> {
				helper.assertTrue(gone.size() == 6 && gone.stream().allMatch(spirit -> spirit.isRemoved()),
						"Their departures over, all six are gone from the world: " + gone.size());
				helper.succeed();
			});
		});
	}

	/** GC-10: the state of The Garden's Regard is {@code state} and {@code spirits} first obligations are kept, {@code counterpart}'s among them. */
	private static void regardAt(GameTestHelper helper, ServerPlayer holder, String counterpart, int spirits, ResearchState state, String when) {
		helper.assertTrue(PactObligations.kept(holder, counterpart) && PactObligations.spiritsKept(holder) == spirits && garden(holder) == state,
				when + ": " + spirits + " kept, the Regard " + state + " (" + PactObligations.spiritsKept(holder) + ", " + garden(holder) + ")");
	}

	/** GC-10: after mastery, a later spirit's first obligation by hand records nothing more: the Regard mastered, its evidence {@code mastered}. */
	private static void stillMastered(GameTestHelper helper, ServerPlayer holder, Map<String, Long> mastered, String when) {
		helper.assertTrue(garden(holder) == ResearchState.MASTERED
				&& ConcordanceProgress.knowledge(holder).progress(GardenPacts.RESEARCH).evidence().equals(mastered),
				when + ": the Regard stays mastered, and nothing more is recorded");
	}

	/** {@code stack} in the first empty main-inventory slot past the hotbar, so a use with the main hand never touches it. */
	private static void stow(GameTestHelper helper, ServerPlayer holder, ItemStack stack) {
		int slot = 9;
		while (slot < Inventory.INVENTORY_SIZE && !holder.getInventory().getItem(slot).isEmpty()) {
			slot++;
		}
		helper.assertTrue(slot < Inventory.INVENTORY_SIZE, "Room past the hotbar for " + stack);
		holder.getInventory().setItem(slot, stack);
	}

	/**
	 * GC-10: the holder chooses {@code d} at the perch: it comes, a visitor bound to no one, listening, and names its beats and
	 * every term with nothing in the way. The menu, and the visitor.
	 */
	private static PactMenu pathInvite(GameTestHelper helper, ServerPlayer holder, PactPerchBlockEntity perch, PactDefinition d) {
		holder.closeContainer();
		RateGate.forget(holder.getUUID());
		PactMenu menu = ConcordanceWorkerGameTests.offerOf(helper, holder, perch, d);
		PactStatePayload offered = menu.lastSent();
		String name = d.id().substring(d.id().indexOf(':') + 1);
		PactSpiritEntity<?> visitor = visitor(helper, perch);
		helper.assertTrue(offered.notes().isEmpty() && offered.offers(PactRequestPayload.ACCEPT)
				&& offered.terms().stream().map(PactStatePayload.Term::key).toList().equals(PactStatePayload.TERMS)
				&& ConcordanceWorkerGameTests.keys(offered.beats()).equals(d.beats().stream()
						.map(beat -> ConcordanceWorkerGameTests.PACT_KEY + name + ".beat." + beat).toList())
				&& visitor != null && d.id().equals(visitor.counterpart()) && visitor.owner() == null && visitor.listening(),
				d.id() + " comes, bound to no one, and names its beats and every term, nothing in the way: " + offered);
		return menu;
	}

	/** The spirit visiting or kept at {@code perch} (not leaving), or null. */
	private static @Nullable PactSpiritEntity<?> visitor(GameTestHelper helper, PactPerchBlockEntity perch) {
		UUID visiting = perch.visitor();
		return visiting != null && helper.getLevel().getEntity(visiting) instanceof PactSpiritEntity<?> found && !found.gone() ? found : null;
	}

	/**
	 * GC-10: the holder accepts {@code d}'s offer with its compact stowed: sealed once, kept here, each material spent exactly
	 * once, one Bound Will whose counterpart is {@code d} and presence the visitor, now theirs, nothing in escrow. The record.
	 */
	private static BoundWill pathAccept(GameTestHelper helper, ServerPlayer holder, PactPerchBlockEntity perch, PactMenu menu, PactDefinition d) {
		ServerLevel level = helper.getLevel();
		PactSpiritEntity<?> visitor = visitor(helper, perch);
		Map<String, Integer> before = new LinkedHashMap<>();
		for (PactDefinition.Cost cost : d.compact()) {
			before.put(cost.item(), ConcordanceWorkerGameTests.count(holder, cost.item()));
		}
		PactStatePayload sealed = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.ACCEPT, 0);
		BoundWill will = ConcordanceWorkerGameTests.recordOf(level, holder);
		boolean spent = d.compact().stream().allMatch(cost -> ConcordanceWorkerGameTests.count(holder, cost.item()) == before.get(cost.item()) - cost.count());
		helper.assertTrue(ConcordanceWorkerGameTests.answered(sealed, PactMenu.SEALED) && sealed.mode() == PactStatePayload.KEEP && spent
				&& will != null && d.id().equals(Pacts.counterpart(will.counterpart())) && visitor != null
				&& visitor.getUUID().equals(will.pact().presence()) && holder.getUUID().equals(visitor.owner())
				&& ConcordanceWorkerGameTests.escrowed(level, will.id()) == 0,
				d.id() + ": sealed once, its compact spent once, one Bound Will, its presence the visitor: " + before);
		holder.closeContainer();
		return will;
	}

	/**
	 * GC-10: the pact with {@code spirit} ended at the perch: every {@code offering} the holder carries paid ahead first (they
	 * carry some), then End: those come back exactly once, the escrow is empty, the record gone, the spirit departing and
	 * struck off, the departure kept once more (its counterpart, ended); ending again returns nothing.
	 */
	private static void pathEnd(GameTestHelper helper, ServerPlayer holder, PactPerchBlockEntity perch, PactDefinition d, BoundWill will,
			PactSpiritEntity<?> spirit, Item offering, List<PactSpiritEntity<?>> gone) {
		ServerLevel level = helper.getLevel();
		MinecraftServer server = level.getServer();
		UUID id = holder.getUUID();
		UUID record = will.id();
		String kind = BuiltInRegistries.ITEM.getKey(offering).toString();
		int carried = ConcordanceWorkerGameTests.count(holder, kind);
		int departures = PactHistory.of(server).history(id).size();
		holder.closeContainer();
		PactMenu menu = ConcordanceWorkerGameTests.open(helper, holder, perch);
		int slot = ConcordanceWorkerGameTests.offering(menu.lastSent(), kind);
		PactStatePayload paid = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.DEPOSIT, slot);
		helper.assertTrue(carried > 0 && slot >= 0 && ConcordanceWorkerGameTests.answered(paid, PactMenu.DEPOSITED)
				&& ConcordanceWorkerGameTests.escrowed(level, record) == carried && ConcordanceWorkerGameTests.count(holder, kind) == 0,
				d.id() + ": the " + carried + " " + kind + " carried paid ahead: " + ConcordanceWorkerGameTests.escrowed(level, record) + " held");
		PactStatePayload ended = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.END, 0);
		List<Pacts.Departure> history = PactHistory.of(server).history(id);
		helper.assertTrue(ConcordanceWorkerGameTests.answered(ended, "ended") && ended.result().get().done()
				&& ConcordanceWorkerGameTests.count(holder, kind) == carried && BoundWills.of(server).escrow(record).isEmpty()
				&& BoundWills.of(server).ledger().get(record) == null && spirit.departing() && spirit.gone()
				&& !WorkerRoster.of(server).workers(id).containsKey(spirit.getUUID()),
				d.id() + ": ended, what was paid ahead back exactly once, the record gone, departing and struck off");
		helper.assertTrue(history.size() == departures + 1 && record.equals(history.getLast().record()) && Pacts.ENDED.equals(history.getLast().reason())
				&& d.id().equals(Pacts.counterpart(history.getLast().counterpart())), d.id() + ": the departure is kept once: " + history);
		PactStatePayload again = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.END, 0);
		helper.assertTrue(again != null && !again.result().map(PactStatePayload.Result::done).orElse(false)
				&& ConcordanceWorkerGameTests.count(holder, kind) == carried && PactHistory.of(server).history(id).size() == departures + 1,
				d.id() + ": ending again returns nothing more");
		holder.closeContainer();
		gone.add(spirit);
	}

	/** GC-10, steps 4 to 6: Buer. His bed woken; short of his compact, refused; sealed; one strain eased, the overworked bed composted by hand; his obligation; ended. */
	private static void pathBuer(GameTestHelper helper, ServerPlayer holder, PactPerchBlockEntity perch, List<PactSpiritEntity<?>> gone) {
		ServerLevel level = helper.getLevel();
		UUID id = holder.getUUID();
		PactDefinition d = ConcordanceWorkerGameTests.buerOffer();
		wake(helper, holder, PATH_BUER_BED);
		PactMenu menu = pathInvite(helper, holder, perch, d);
		PactStatePayload refused = ConcordanceWorkerGameTests.send(holder, menu, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(ConcordanceWorkerGameTests.answered(refused, Pacts.Outcome.CANNOT_AFFORD.id)
				&& ConcordanceWorkerGameTests.said(refused).equals(ConcordanceWorkerGameTests.PACT_KEY + "answer.cannot_afford.items")
				&& BoundWills.of(level.getServer()).ledger().heldBy(id).isEmpty() && visitor(helper, perch) != null,
				"Step 4: short of Buer's compact, accepting is refused once with its reason; nothing spent, he stays");
		for (int i = 0; i < 2; i++) {
			stow(helper, holder, craft(helper, Garden.MENDVETCH, 2, of(Items.WHEAT_SEEDS), of(Items.BONE_MEAL), of(Items.AMETHYST_SHARD),
					null, null, null, null, null, null));
		}
		stow(helper, holder, new ItemStack(Items.BONE_MEAL, 4));
		BoundWill will = pathAccept(helper, holder, perch, menu, d);
		BuerEntity buer = ConcordanceWorkerGameTests.buer(helper, perch);

		// Step 5: by daylight he eases one strain; worked back to its worst the bed is overworked, until bone meal by hand composts it.
		VerdantBedBlockEntity bed = helper.getBlockEntity(PATH_BUER_BED, VerdantBedBlockEntity.class);
		long t = ConcordanceWorkerGameTests.dayStart(level, 1);
		bed.setStrain(2);
		helper.assertTrue(buer.think(level, PACT_NOON, t) == Status.WORKING && bed.strain() == 1, "Step 5: by day Buer eases one strain, 2 to 1");
		bed.setStrain(3);
		helper.assertTrue(buer.think(level, PACT_NOON, t + Garden.TEND_TICKS) == Status.IDLE && bed.strain() == 3,
				"Worked back to 3, the bed is overworked: he refuses it (his conflict)");
		helper.assertTrue(use(helper, holder, PATH_BUER_BED, new ItemStack(Items.BONE_MEAL)).consumesAction() && bed.strain() == 2,
				"Bone meal by hand composts it");
		helper.assertTrue(buer.think(level, PACT_NOON, t + 2L * Garden.TEND_TICKS) == Status.WORKING && bed.strain() == 1, "and he mends it again");
		helper.assertTrue(!PactObligations.kept(holder, BuerEntity.COUNTERPART), "His own tending keeps nothing of the holder's");
		wake(helper, holder, PATH_HAND_BED);
		VerdantBedBlockEntity own = helper.getBlockEntity(PATH_HAND_BED, VerdantBedBlockEntity.class);
		own.setStrain(2);
		use(helper, holder, PATH_HAND_BED, ItemStack.EMPTY);
		helper.assertValueEqual(own.strain(), 1, "Their own tending by hand eases it");
		regardAt(helper, holder, BuerEntity.COUNTERPART, 1, ResearchState.UNDERSTOOD, "Buer's first obligation, a tending by hand in his area");

		// Step 6: a Mendvetch crafted and paid ahead; ended at the perch.
		stow(helper, holder, craft(helper, Garden.MENDVETCH, 2, of(Items.WHEAT_SEEDS), of(Items.BONE_MEAL), of(Items.AMETHYST_SHARD),
				null, null, null, null, null, null));
		pathEnd(helper, holder, perch, d, will, buer, Garden.MENDVETCH, gone);
		helper.setBlock(PATH_BUER_BED, Blocks.AIR);
		helper.setBlock(PATH_HAND_BED, Blocks.AIR);
	}

	/** GC-10, steps 7 to 9: Bathin. Verdance carried exactly; a Heart with its own road left until answered "take from it"; his obligation; ended. */
	private static void pathBathin(GameTestHelper helper, ServerPlayer holder, PactPerchBlockEntity perch, List<PactSpiritEntity<?>> gone) {
		ServerLevel level = helper.getLevel();
		UUID id = holder.getUUID();
		PactDefinition d = ConcordanceWorkerGameTests.bathinOffer();
		VerdantHeartBlockEntity heart = ConcordanceWorkerGameTests.keptHeart(helper, PATH_HEART, id, 16);
		GleanerBlockEntity gleaner = ConcordanceWorkerGameTests.keptGleaner(helper, PATH_GLEANER, id, 0);
		PactMenu menu = pathInvite(helper, holder, perch, d);
		stow(helper, holder, new ItemStack(Items.ENDER_PEARL, 2));
		// Verdant Chaff is the Gleaner's own product (its making is the garden's tests'); given here.
		stow(helper, holder, new ItemStack(Garden.VERDANT_CHAFF, 8));
		BoundWill will = pathAccept(helper, holder, perch, menu, d);
		BathinEntity bathin = ConcordanceWorkerGameTests.bathin(helper, perch);

		// Step 8: Heart to the far Gleaner, 8 a move, the total kept; then a Heart with its own road, left until answered.
		long t = ConcordanceWorkerGameTests.dayStart(level, 1);
		helper.assertTrue(bathin.think(level, PACT_NOON, t) == Status.WORKING && heart.verdance() == 8 && gleaner.verdance() == 8,
				"Step 8: Bathin moves 8 from the Heart into the far Gleaner at once: " + heart.verdance() + ", " + gleaner.verdance());
		helper.assertTrue(bathin.think(level, PACT_NOON, t + 20) == Status.WORKING && heart.verdance() == 0 && gleaner.verdance() == 16,
				"and the rest: 16 in all before and after");
		VerdantHeartBlockEntity roaded = ConcordanceWorkerGameTests.heartWithItsOwnRoad(helper, id, PATH_ROADED_HEART);
		GleanerBlockEntity west = helper.getBlockEntity(PATH_ROADED_HEART.west(5), GleanerBlockEntity.class);
		long refreshed = t + GardenReach.REFRESH_TICKS + 40;
		helper.assertTrue(bathin.think(level, PACT_NOON, refreshed) == Status.AWAITING_ANSWER && roaded.verdance() == 16 && west.verdance() == 0,
				"A Heart with its own pylon: he leaves it and waits for the answer (his matter)");
		PactService.Complication matter = VerdanceCarrying.SERVICE.complication(level, ConcordanceWorkerGameTests.pactOf(level, perch), perch, refreshed);
		helper.assertTrue(matter != null && VerdanceCarrying.TWO_ROADS.equals(matter.id()) && roaded.getBlockPos().equals(matter.pos()),
				"His matter names that Heart: " + matter);
		holder.closeContainer();
		ConcordanceWorkerGameTests.answer(helper, holder, perch, VerdanceCarrying.CLAIM);
		helper.assertTrue(bathin.think(level, PACT_NOON, refreshed + 20) == Status.WORKING && roaded.verdance() == 8 && west.verdance() == 8
				&& heart.verdance() + gleaner.verdance() + roaded.verdance() + west.verdance() == 32,
				"Answered take from it, he carries from it to the Gleaner with room, never its own pylon: 32 in all");
		ConcordanceGardenGameTests.handUse(helper, holder, PATH_HEART, ItemStack.EMPTY, false);
		regardAt(helper, holder, BathinEntity.COUNTERPART, 2, ResearchState.UNDERSTOOD, "Bathin's first obligation, an awake Heart read by hand");

		// Step 9: four Verdant Chaff paid ahead; ended.
		stow(helper, holder, new ItemStack(Garden.VERDANT_CHAFF, d.count()));
		pathEnd(helper, holder, perch, d, will, bathin, Garden.VERDANT_CHAFF, gone);
		for (BlockPos device : List.of(PATH_HEART, PATH_GLEANER, PATH_ROADED_HEART, PATH_ROADED_HEART.east(), PATH_ROADED_HEART.west(5))) {
			helper.setBlock(device, Blocks.AIR);
		}
	}

	/** GC-10, steps 10 to 13: Marax. A reading names one change and changes nothing; the same bed named three times is passed over; his obligation masters the Regard; ended. */
	private static void pathMarax(GameTestHelper helper, ServerPlayer holder, PactPerchBlockEntity perch, VerdantBedBlockEntity bed,
			List<PactSpiritEntity<?>> gone) {
		ServerLevel level = helper.getLevel();
		PactDefinition d = ConcordanceWorkerGameTests.maraxOffer();
		PactMenu menu = pathInvite(helper, holder, perch, d);
		stow(helper, holder, new ItemStack(Items.BOOK));
		stow(helper, holder, craft(helper, Garden.SUNPETAL, 2, of(Items.SUNFLOWER), of(Items.AMETHYST_SHARD), of(Items.BONE_MEAL),
				null, null, null, null, null, null));
		stow(helper, holder, craft(helper, Garden.DEWMOSS, 2, of(Items.MOSS_BLOCK), of(Items.KELP), of(Items.AMETHYST_SHARD),
				null, null, null, null, null, null));
		BoundWill will = pathAccept(helper, holder, perch, menu, d);
		MaraxEntity marax = ConcordanceWorkerGameTests.marax(helper, perch);

		// Step 11: a reading names its bed and one change and changes nothing; three running, the fourth passes it over.
		String named = bed.getBlockPos().toShortString();
		List<String> before = ConcordanceWorkerGameTests.unchanged(helper, List.of(bed));
		int used = Garden.budget(level).used(level.getGameTime());
		long day = ConcordanceWorkerGameTests.dayStart(level, 1);
		PactMenu.Answered first = ConcordanceWorkerGameTests.reading(helper, holder, perch, day);
		helper.assertTrue(first.done() && HabitatReading.READ.equals(first.reason()) && named.equals(ConcordanceWorkerGameTests.arg(first.said(), 1))
				&& ConcordanceWorkerGameTests.remedy(first) != null && Pacts.done(ConcordanceWorkerGameTests.pactOf(level, perch).pact(), day) == 1,
				"Step 11: Marax's reading names the bed and one change, counted once: " + first.said().getString());
		helper.assertTrue(ConcordanceWorkerGameTests.unchanged(helper, List.of(bed)).equals(before) && Garden.budget(level).used(level.getGameTime()) == used,
				"and changes nothing on it, no sample taken");
		long second = ConcordanceWorkerGameTests.dayStart(level, 2);
		for (long when : new long[] {day + 100, second}) {
			PactMenu.Answered again = ConcordanceWorkerGameTests.reading(helper, holder, perch, when);
			helper.assertTrue(again.done() && named.equals(ConcordanceWorkerGameTests.arg(again.said(), 1)), "The same bed named again at " + when);
		}
		helper.assertTrue(ConcordanceWorkerGameTests.familiar(marax).proudBeds() == 1, "Named three readings running, nothing changed: proud");
		PactMenu.Answered fourth = ConcordanceWorkerGameTests.reading(helper, holder, perch, second + 100);
		helper.assertTrue(fourth.done() && HabitatReading.NOTHING_TO_CORRECT.equals(fourth.reason())
				&& ConcordanceWorkerGameTests.key(fourth.said()).equals(ConcordanceWorkerGameTests.READING_KEY + "nothing_to_correct.proud")
				&& Pacts.done(ConcordanceWorkerGameTests.pactOf(level, perch).pact(), second + 100) == 1,
				"The fourth will not repeat itself (the proud familiar, his conflict) and counts nothing: " + fourth.said().getString());
		helper.assertTrue(!PactObligations.kept(holder, MaraxEntity.COUNTERPART), "His readings keep nothing of the holder's");
		ConcordanceGardenGameTests.handUse(helper, holder, PATH_READ_BED, ItemStack.EMPTY, false);
		// Step 12: three garden spirits' first obligations kept: The Garden's Regard is mastered (GB26).
		regardAt(helper, holder, MaraxEntity.COUNTERPART, 3, ResearchState.MASTERED, "Step 12: Marax's first obligation, the bed read by hand");

		// Step 13: two sheets of paper paid ahead; ended.
		stow(helper, holder, new ItemStack(Items.PAPER, d.count()));
		pathEnd(helper, holder, perch, d, will, marax, Items.PAPER, gone);
	}

	/**
	 * GC-10, steps 14 to 16: Foras. Marax's bed, its reading kept, is his host (a full-sky Sunpetal a graft would not help):
	 * named and left until answered; answered "where it helps", left; answered "graft every host", grafted with one Dewmoss
	 * from the store touching the perch; a graft by hand; ended. (A host the graft helps needs light 9 under a settled light
	 * engine, aPrudentForasGraftsWhereItHelps; the path grafts under the answer instead.)
	 */
	private static void pathForas(GameTestHelper helper, ServerPlayer holder, PactPerchBlockEntity perch, VerdantBedBlockEntity host,
			Map<String, Long> mastered, List<PactSpiritEntity<?>> gone) {
		ServerLevel level = helper.getLevel();
		UUID id = holder.getUUID();
		PactDefinition d = ConcordanceWorkerGameTests.forasOffer();
		// His fixture: a young Sunpetal on the read bed (its reading kept; 4 nutrients, as forasHost's) and a chest touching the perch.
		// A random tick since it was planted may have dried it: wet again (the block state; its block entity, and its reading, stay).
		helper.setBlock(PATH_READ_BED, helper.getBlockState(PATH_READ_BED).setValue(VerdantBedBlock.MOISTURE, VerdantBedBlock.WET));
		host.setNutrients(4);
		helper.setBlock(PATH_READ_BED.above(), Garden.SUNPETAL_CROP);
		ChestBlockEntity store = ConcordanceWorkerGameTests.forasStore(helper, PATH_STORE, 0);
		PactMenu menu = pathInvite(helper, holder, perch, d);
		for (int i = 0; i < 2; i++) {
			stow(helper, holder, craft(helper, Garden.DEWMOSS, 2, of(Items.MOSS_BLOCK), of(Items.KELP), of(Items.AMETHYST_SHARD),
					null, null, null, null, null, null));
		}
		stow(helper, holder, new ItemStack(Items.HONEYCOMB));
		BoundWill will = pathAccept(helper, holder, perch, menu, d);
		ForasEntity foras = ConcordanceWorkerGameTests.foras(helper, perch);
		// Two Dewmoss more: one into the store, one kept for the graft by hand.
		ItemStack donor = craft(helper, Garden.DEWMOSS, 2, of(Items.MOSS_BLOCK), of(Items.KELP), of(Items.AMETHYST_SHARD),
				null, null, null, null, null, null);
		store.setItem(0, donor.split(1));

		// Step 15: the host named and left until answered; "where it helps" leaves it; "graft every host" grafts it, one Dewmoss.
		long t = ConcordanceWorkerGameTests.dayStart(level, 1);
		helper.assertTrue(foras.think(level, PACT_NOON, t) == Status.AWAITING_ANSWER
				&& ConcordanceWorkerGameTests.keeping(foras).useless().equals(List.of(host.getBlockPos().above()))
				&& host.graft().isEmpty() && ConcordanceWorkerGameTests.dewmossIn(store) == 1,
				"Step 15: Foras names the host a graft would not help and waits for the answer (his matter), nothing spent");
		PactService.Complication matter = GraftKeeping.SERVICE.complication(level, ConcordanceWorkerGameTests.pactOf(level, perch), perch, t);
		helper.assertTrue(matter != null && GraftKeeping.NO_GAIN.equals(matter.id()), "His matter: " + matter);
		ConcordanceWorkerGameTests.forasAnswer(helper, holder, perch, GraftKeeping.PRUDENT, t);
		helper.assertTrue(foras.think(level, PACT_NOON, t + 1) == Status.IDLE && host.graft().isEmpty() && ConcordanceWorkerGameTests.dewmossIn(store) == 1,
				"Answered where it helps: nothing grafted against it");
		ConcordanceWorkerGameTests.forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, t + 1);
		helper.assertTrue(foras.think(level, PACT_NOON, t + 2) == Status.WORKING && ConcordanceWorkerGameTests.FORAS_GRAFT.equals(host.graft())
				&& ConcordanceWorkerGameTests.dewmossIn(store) == 0 && Pacts.done(ConcordanceWorkerGameTests.pactOf(level, perch).pact(), t + 2) == 1,
				"Answered graft every host: one graft, one Dewmoss spent, counted once");
		VerdantBedBlockEntity byHand = ConcordanceWorkerGameTests.forasHost(helper, PATH_HAND_HOST, id);
		ConcordanceGardenGameTests.handUse(helper, holder, PATH_HAND_HOST.above(), donor, false);
		helper.assertTrue(ConcordanceWorkerGameTests.FORAS_GRAFT.equals(byHand.graft()), "A Sunpetal grafted by hand in his area");
		stillMastered(helper, holder, mastered, "Foras's first obligation, a graft by hand");

		// Step 16: a honeycomb paid ahead; ended. His store and hosts are taken up after him (the chest emptied first).
		stow(helper, holder, new ItemStack(Items.HONEYCOMB, d.count()));
		pathEnd(helper, holder, perch, d, will, foras, Items.HONEYCOMB, gone);
		store.clearContent();
		for (BlockPos block : List.of(PATH_STORE, PATH_READ_BED.above(), PATH_READ_BED, PATH_HAND_HOST.above(), PATH_HAND_HOST)) {
			helper.setBlock(block, Blocks.AIR);
		}
	}

	/** GC-10, steps 17 to 19: Bifrons, by night. A root run from the full bed to the hungry one; a growing full bed his reserves until answered "any full bed"; his obligation; ended. */
	private static void pathBifrons(GameTestHelper helper, ServerPlayer holder, PactPerchBlockEntity perch, Map<String, Long> mastered,
			List<PactSpiritEntity<?>> gone) {
		ServerLevel level = helper.getLevel();
		UUID id = holder.getUUID();
		PactDefinition d = ConcordanceWorkerGameTests.bifronsOffer();
		VerdantBedBlockEntity full = ConcordanceWorkerGameTests.bifronsBed(helper, PATH_FULL_BED, id, 32);
		VerdantBedBlockEntity hungry = ConcordanceWorkerGameTests.bifronsBed(helper, PATH_HUNGRY_BED, id, 0);
		PactMenu menu = pathInvite(helper, holder, perch, d);
		stow(helper, holder, new ItemStack(Items.OAK_LOG, 4));
		stow(helper, holder, new ItemStack(Items.EMERALD));
		BoundWill will = pathAccept(helper, holder, perch, menu, d);
		BifronsEntity bifrons = ConcordanceWorkerGameTests.bifrons(helper, perch);

		// Step 18: he measures, then a beat later runs: 2 leave the full bed and 1 reaches the hungry one.
		long t = ConcordanceWorkerGameTests.dayStart(level, 1);
		helper.assertTrue(bifrons.think(level, PACT_NIGHT, t) == Status.WORKING && full.nutrients() == 32 && hungry.nutrients() == 0,
				"Step 18: by night Bifrons measures first, moving nothing");
		helper.assertTrue(bifrons.think(level, PACT_NIGHT, t + RootRunning.MEASURE_TICKS) == Status.WORKING && full.nutrients() == 30
				&& hungry.nutrients() == 1, "then runs the root: 2 leave, 1 arrives");
		ConcordanceWorkerGameTests.ripeOn(helper, PATH_FULL_BED);
		long asked = t + 100;
		helper.assertTrue(bifrons.think(level, PACT_NIGHT, asked) == Status.AWAITING_ANSWER && full.nutrients() == 30 && hungry.nutrients() == 1,
				"Its only full bed now growing: he asks which beds are reserves (his matter), drawing nothing");
		PactService.Complication matter = RootRunning.SERVICE.complication(level, ConcordanceWorkerGameTests.pactOf(level, perch), perch, asked);
		helper.assertTrue(matter != null && RootRunning.RESERVES.equals(matter.id()) && full.getBlockPos().equals(matter.pos()), "His matter: " + matter);
		holder.closeContainer();
		PactMenu page = ConcordanceWorkerGameTests.open(helper, holder, perch);
		helper.assertTrue(ConcordanceWorkerGameTests.answered(ConcordanceWorkerGameTests.send(holder, page, PactRequestPayload.RESOLVE,
				matter.answers().indexOf(RootRunning.FULL)), PactMenu.RESOLVED) && RootRunning.FULL.equals(perch.accord()), "Answered: any full bed");
		holder.closeContainer();
		helper.assertTrue(bifrons.think(level, PACT_NIGHT, asked + 20) == Status.WORKING
				&& bifrons.think(level, PACT_NIGHT, asked + 20 + RootRunning.MEASURE_TICKS) == Status.WORKING
				&& full.nutrients() == 28 && hungry.nutrients() == 2, "and he runs from the growing bed too: 30 to 28, 1 to 2");
		int fed = hungry.nutrients();
		ConcordanceGardenGameTests.handUse(helper, holder, PATH_HUNGRY_BED, new ItemStack(Items.BONE_MEAL), false);
		helper.assertValueEqual(hungry.nutrients(), fed + Garden.BONE_MEAL_NUTRIENTS, "A bed in his area fed with bone meal by hand");
		stillMastered(helper, holder, mastered, "Bifrons's first obligation, a feeding by hand");

		// Step 19: two logs paid ahead; ended.
		stow(helper, holder, new ItemStack(Items.OAK_LOG, d.count()));
		pathEnd(helper, holder, perch, d, will, bifrons, Items.OAK_LOG, gone);
		for (BlockPos block : List.of(PATH_FULL_BED.above(), PATH_FULL_BED, PATH_HUNGRY_BED)) {
			helper.setBlock(block, Blocks.AIR);
		}
	}

	/** GC-10, steps 20 to 22: Amdusias. His tree's shelter over a Sunpetal; a Gloamcap spared until answered "shelter all"; a harvest by hand under it; ended. */
	private static void pathAmdusias(GameTestHelper helper, ServerPlayer holder, PactPerchBlockEntity perch, Map<String, Long> mastered,
			List<PactSpiritEntity<?>> gone) {
		ServerLevel level = helper.getLevel();
		UUID id = holder.getUUID();
		PactDefinition d = ConcordanceWorkerGameTests.amdusiasOffer();
		helper.setBlock(PATH_LOG, Blocks.OAK_LOG);
		VerdantBedBlockEntity sunpetal = ConcordanceWorkerGameTests.shelteredCrop(helper, PATH_SUNPETAL, id, Garden.SUNPETAL_CROP);
		VerdantBedBlockEntity gloamcap = ConcordanceWorkerGameTests.shelteredCrop(helper, PATH_GLOAMCAP, id, Garden.GLOAMCAP_CROP);
		PactMenu menu = pathInvite(helper, holder, perch, d);
		stow(helper, holder, new ItemStack(Items.NOTE_BLOCK));
		stow(helper, holder, new ItemStack(Items.OAK_SAPLING, 2));
		BoundWill will = pathAccept(helper, holder, perch, menu, d);
		AmdusiasEntity amdusias = ConcordanceWorkerGameTests.amdusias(helper, perch);

		// Step 21: the canopy covers the Sunpetal (2 less stir); the Gloamcap, which wants the stir, is spared until answered.
		// His thinks read the canopy registry at the level's own game time.
		long now = level.getGameTime();
		helper.assertTrue(amdusias.think(level, PACT_NOON, now) == Status.AWAITING_ANSWER
				&& ConcordanceWorkerGameTests.canopy(amdusias).covered().equals(List.of(sunpetal.getBlockPos()))
				&& ConcordanceWorkerGameTests.canopy(amdusias).spared().equals(List.of(gloamcap.getBlockPos()))
				&& ConcordanceWorkerGameTests.dampingOver(level, sunpetal, Garden.SUNPETAL_CROP) == 2,
				"Step 21: Amdusias's tree shelters the Sunpetal (2 less stir), spares the Gloamcap and waits for the answer (his matter)");
		Canopies.Canopy shelter = Canopies.of(level, id, now);
		helper.assertTrue(shelter != null && shelter.beds().equals(Set.of(sunpetal.getBlockPos())) && !shelter.all(), "His canopy: " + shelter);
		ConcordanceWorkerGameTests.amdusiasAnswer(helper, holder, perch, CanopyShelter.ALL, now);
		helper.assertTrue(amdusias.think(level, PACT_NOON, now) == Status.WORKING && ConcordanceWorkerGameTests.canopy(amdusias).spared().isEmpty()
				&& Canopies.of(level, id, now) != null && Canopies.of(level, id, now).all()
				&& Canopies.of(level, id, now).beds().equals(Set.of(sunpetal.getBlockPos(), gloamcap.getBlockPos())),
				"Answered shelter all: the Gloamcap is covered too");
		BlockState ripe = ((OrganismCropBlock) Garden.SUNPETAL_CROP).getStateForAge(OrganismCropBlock.MAX_AGE);
		helper.setBlock(PATH_SUNPETAL.above(), ripe);
		ConcordanceGardenGameTests.handUse(helper, holder, PATH_SUNPETAL.above(), ItemStack.EMPTY, false);
		helper.assertTrue(!helper.getBlockState(PATH_SUNPETAL.above()).equals(ripe), "A ripe crop harvested by hand under his canopy");
		stillMastered(helper, holder, mastered, "Amdusias's first obligation, a harvest by hand");

		// Step 22: a sapling paid ahead; ended.
		stow(helper, holder, new ItemStack(Items.OAK_SAPLING, d.count()));
		pathEnd(helper, holder, perch, d, will, amdusias, Items.OAK_SAPLING, gone);
	}
}
