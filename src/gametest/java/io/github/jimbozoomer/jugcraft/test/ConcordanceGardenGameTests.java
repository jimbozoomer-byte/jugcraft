package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.GourdCanteenItem;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.chemistry.FertilizerItem;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.compose.Text;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Factor;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Habitat;
import io.github.jimbozoomer.jugcraft.concordance.ecology.HabitatInspection;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Organism;
import io.github.jimbozoomer.jugcraft.concordance.ecology.SampleBudget;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Sampler;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Verdict;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.garden.GleanerBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.HabitatGaugeBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.MulchMawBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.OrganismCropBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantHeartBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Roadmap step 14, ecological cultivation (docs/features/arcane-concordance-ecology.md), on a real server: a bed
 * placed by someone who does not know Verdant Husbandry stays dormant until a Greenwarden's touch wakes it and the
 * beds joined to it; a crop grows only within its niche and says which factor holds it back; every step costs its bed
 * nutrients and dries it, so a depleted bed stops growth until bone meal or fertilizer feeds it; a nitrogen fixer
 * feeds the poorest bed round it; the Verdant Heart beats nutrients into Verdance and pours it into a Ley Pylon, and
 * stops on a starved bed; the Mulch Maw turns plant matter into nutrients for the poorest bed and stops eating when
 * every bed is full; the Habitat Gauge gives the habitat as a signal; the Gleaner harvests a ripe crop for a Verdance
 * drawn from a Heart its keeper may use, and from no one else's; a level takes at most 16 area samples a tick; and
 * beds and devices keep what they hold through a save. Since Concordance Chunk C (C-07): the reading the Inspect page
 * shows judges each factor as growth does and names its reach, the light as sky and lamps; a bed forgets its verdict once
 * nothing that notes it stands on it; an item held on a bed is its own use, and a canteen's sip wets a bed, by permission
 * and rate.
 */
public class ConcordanceGardenGameTests {
	private static final UUID KEEPER = new UUID(14L, 1L);

	private static ServerLevel level(GameTestHelper helper) {
		return helper.getLevel();
	}

	/** An awake, wet bed holding {@code nutrients}. */
	private static VerdantBedBlockEntity bed(GameTestHelper helper, BlockPos at, int nutrients) {
		helper.setBlock(at, Garden.VERDANT_BED.defaultBlockState().setValue(VerdantBedBlock.MOISTURE, VerdantBedBlock.WET));
		VerdantBedBlockEntity bed = (VerdantBedBlockEntity) level(helper).getBlockEntity(helper.absolutePos(at));
		bed.awaken(KEEPER);
		bed.setNutrients(nutrients);
		return bed;
	}

	private static OrganismCropBlock.Growth grow(GameTestHelper helper, BlockPos at) {
		BlockState state = helper.getBlockState(at);
		return ((OrganismCropBlock) state.getBlock()).grow(level(helper), helper.absolutePos(at), state, level(helper).getRandom(), true);
	}

	private static int age(GameTestHelper helper, BlockPos at) {
		return helper.getBlockState(at).getValue(OrganismCropBlock.AGE);
	}

	/** Stone all round {@code at} and over it, so no light reaches it. */
	private static void shut(GameTestHelper helper, BlockPos at) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (dx != 0 || dz != 0) {
					helper.setBlock(at.offset(dx, 0, dz), Blocks.STONE);
				}
				helper.setBlock(at.offset(dx, 1, dz), Blocks.STONE);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T entity(GameTestHelper helper, BlockPos at) {
		return (T) level(helper).getBlockEntity(helper.absolutePos(at));
	}

	/** The top face of the block at {@code at}, as a use from above hits it. */
	private static BlockHitResult top(GameTestHelper helper, BlockPos at) {
		BlockPos absolute = helper.absolutePos(at);
		return new BlockHitResult(Vec3.atCenterOf(absolute).relative(Direction.UP, 0.5), Direction.UP, absolute, false);
	}

	/**
	 * {@code player} uses {@code held}, put in the main hand, on the top of the block at {@code at} by the server's own path
	 * (ServerPlayerGameMode.useItemOn), plainly or sneaking, its rate limit forgotten first.
	 */
	private static InteractionResult handUse(GameTestHelper helper, ServerPlayer player, BlockPos at, ItemStack held, boolean sneaking) {
		RateGate.forget(player.getUUID());
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		player.setShiftKeyDown(sneaking);
		try {
			return player.gameMode.useItemOn(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND, top(helper, at));
		} finally {
			player.setShiftKeyDown(false);
		}
	}

	/** One random tick of the block at {@code at}, as the level gives it. */
	private static void tickBed(GameTestHelper helper, BlockPos at) {
		helper.getBlockState(at).randomTick(level(helper), helper.absolutePos(at), level(helper).getRandom());
	}

	/** A copy of {@code bed} read back from {@code saved}: a save, or an update tag as a client reads it. */
	private static VerdantBedBlockEntity reload(GameTestHelper helper, VerdantBedBlockEntity bed, CompoundTag saved) {
		VerdantBedBlockEntity copy = new VerdantBedBlockEntity(bed.getBlockPos(), bed.getBlockState());
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level(helper).registryAccess(), saved));
		return copy;
	}

	private static ItemStack canteen(int water) {
		ItemStack stack = new ItemStack(JugcraftAgriculture.item("gourd_canteen"));
		stack.set(JugcraftAgriculture.CANTEEN_WATER, water);
		return stack;
	}

	/** Reads each bed's area early (ticks 10, 20, 30): a level takes only so many samples a tick, and any one serves 200. */
	private static void prefetch(GameTestHelper helper, VerdantBedBlockEntity... beds) {
		for (int tick : new int[] {10, 20, 30}) {
			helper.runAtTickTime(tick, () -> {
				for (VerdantBedBlockEntity bed : beds) {
					bed.habitat(level(helper));
				}
			});
		}
	}

	// ---------------------------------------------------------------- waking

	/**
	 * A bed placed by a player who has not understood Verdant Husbandry is dormant, and a crop on it does not grow; a
	 * Greenwarden's empty hand wakes it and the dormant beds joined to it, and no others.
	 */
	@GameTest(maxTicks = 40)
	public void aGreenwardenWakesTheBeds(GameTestHelper helper) {
		ServerPlayer novice = helper.makeMockServerPlayerInLevel();
		novice.setGameMode(GameType.SURVIVAL);
		List<BlockPos> row = List.of(new BlockPos(1, 1, 1), new BlockPos(2, 1, 1), new BlockPos(3, 1, 1));
		BlockPos apart = new BlockPos(6, 1, 1);
		for (BlockPos at : List.of(row.get(0), row.get(1), row.get(2), apart)) {
			helper.setBlock(at, Garden.VERDANT_BED);
			Garden.VERDANT_BED.setPlacedBy(level(helper), helper.absolutePos(at), helper.getBlockState(at), novice, ItemStack.EMPTY);
		}
		VerdantBedBlockEntity first = entity(helper, row.get(0));
		helper.assertTrue(!first.awake() && first.keeper() != null, "A novice's bed is dormant and remembers who placed it");
		helper.setBlock(row.get(0).above(), Garden.SUNPETAL_CROP);
		helper.assertTrue(grow(helper, row.get(0).above()) == OrganismCropBlock.Growth.DORMANT, "Nothing grows in a dormant bed");
		ServerPlayer greenwarden = helper.makeMockServerPlayerInLevel();
		greenwarden.setGameMode(GameType.SURVIVAL);
		ConcordanceProgress.grant(greenwarden, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(greenwarden, Garden.RESEARCH, ResearchState.UNDERSTOOD);
		helper.assertTrue(Garden.knows(greenwarden) && !Garden.knows(novice), "Only the Greenwarden knows Verdant Husbandry");
		RateGate.forget(greenwarden.getUUID());
		int woken = VerdantBedBlock.awaken(level(helper), helper.absolutePos(row.get(1)), greenwarden);
		helper.assertTrue(woken == 3, "One touch wakes the three joined beds: " + woken);
		for (BlockPos at : row) {
			helper.assertTrue(((VerdantBedBlockEntity) entity(helper, at)).awake(), "Bed " + at + " is awake");
		}
		helper.assertTrue(!((VerdantBedBlockEntity) entity(helper, apart)).awake(), "A bed not joined to them stays dormant");
		helper.succeed();
	}

	// ---------------------------------------------------------------- niches

	/**
	 * Two sunpetals on rich, wet, awake beds: the one lit by glowstone grows a step, paying a nutrient and drying its
	 * bed; the one shut in the dark does not, and its bed records why (too little light, with the reading and the
	 * least it needs), which its tooltip shows.
	 */
	@GameTest(maxTicks = 80)
	public void aCropGrowsOnlyWithinItsNiche(GameTestHelper helper) {
		BlockPos lit = new BlockPos(1, 1, 1);
		BlockPos dark = new BlockPos(5, 1, 5);
		VerdantBedBlockEntity litBed = bed(helper, lit, 10);
		VerdantBedBlockEntity darkBed = bed(helper, dark, 10);
		helper.setBlock(lit.above(), Garden.SUNPETAL_CROP);
		helper.setBlock(lit.above().north(), Blocks.GLOWSTONE);
		helper.setBlock(dark.above(), Garden.SUNPETAL_CROP);
		shut(helper, dark.above());
		// Give the light engine time to light (and darken) the new blocks. A vanilla random tick may have grown a step
		// or dried a bed meanwhile, so what follows is measured from the state just before.
		helper.runAtTickTime(40, () -> {
			ServerLevel level = level(helper);
			int light = level.getRawBrightness(helper.absolutePos(lit.above()), 0);
			int shade = level.getRawBrightness(helper.absolutePos(dark.above()), 0);
			helper.assertTrue(light >= 13 && shade <= 2, "Glowstone lights one (" + light + "), stone shuts out the other (" + shade + ")");
			int age = age(helper, lit.above());
			int nutrients = litBed.nutrients();
			int moisture = litBed.moisture();
			helper.assertTrue(age < OrganismCropBlock.MAX_AGE && grow(helper, lit.above()) == OrganismCropBlock.Growth.GREW
					&& age(helper, lit.above()) == age + 1, "The lit sunpetal grows a step");
			helper.assertTrue(litBed.nutrients() == nutrients - 1 && litBed.moisture() == Math.max(0, moisture - 1),
					"The step cost a nutrient and dried the bed: " + litBed.nutrients() + ", " + litBed.moisture());
			int darkNutrients = darkBed.nutrients();
			helper.assertTrue(grow(helper, dark.above()) == OrganismCropBlock.Growth.STALLED && age(helper, dark.above()) == 0,
					"The sunpetal in the dark does not grow");
			helper.assertTrue(darkBed.nutrients() == darkNutrients, "A stalled crop costs nothing");
			helper.assertTrue(darkBed.growth().equals("stalled") && !darkBed.reasons().isEmpty()
					&& darkBed.reasons().get(0).startsWith("ecology.short|light|") && darkBed.reasons().get(0).endsWith("|11"),
					"Its bed says why: too little light, needing at least 11: " + darkBed.reasons());
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------- depleted inputs constrain output

	/**
	 * A sunpetal on a bed with one nutrient grows one step and then stops: the empty bed is outside its niche. Bone meal
	 * on the bed feeds it two; fertilizer feeds each crop's bed two a dose; and it grows again.
	 */
	@GameTest(maxTicks = 80)
	public void anEmptyBedStopsGrowth(GameTestHelper helper) {
		BlockPos at = new BlockPos(2, 1, 2);
		VerdantBedBlockEntity bed = bed(helper, at, 1);
		helper.setBlock(at.above(), Garden.SUNPETAL_CROP);
		helper.setBlock(at.above().east(), Blocks.GLOWSTONE);
		helper.runAtTickTime(40, () -> {
			// Set the bed again: a random tick may have grown a step meanwhile.
			helper.setBlock(at.above(), Garden.SUNPETAL_CROP);
			bed.setNutrients(1);
			helper.assertTrue(grow(helper, at.above()) == OrganismCropBlock.Growth.GREW && bed.nutrients() == 0, "One nutrient, one step");
			helper.assertTrue(grow(helper, at.above()) == OrganismCropBlock.Growth.STALLED && age(helper, at.above()) == 1,
					"The empty bed stops it");
			helper.assertTrue(bed.reasons().stream().anyMatch(reason -> reason.startsWith("ecology.short|nutrients|0|")),
					"and says it is short of nutrients: " + bed.reasons());
			helper.assertTrue(bed.give(Garden.BONE_MEAL_NUTRIENTS) == 2 && bed.nutrients() == 2, "Bone meal's worth feeds the bed");
			helper.assertTrue(grow(helper, at.above()) == OrganismCropBlock.Growth.GREW && age(helper, at.above()) == 2, "and it grows again");
			int fed = FertilizerItem.fertilize(level(helper), helper.absolutePos(at.above()));
			helper.assertTrue(fed == 1 && bed.nutrients() == 1 + 2 * Garden.BONE_MEAL_NUTRIENTS,
					"Fertilizer feeds the crop's bed two doses' worth, never a free step: " + fed + ", " + bed.nutrients());
			helper.assertTrue(age(helper, at.above()) == 2, "Fertilizer did not force a step");
			helper.succeed();
		});
	}

	/** A Mendvetch's step costs nothing and gives a nutrient to the poorest bed of its own and those beside it. */
	@GameTest(maxTicks = 80)
	public void aFixerFeedsThePoorestBed(GameTestHelper helper) {
		BlockPos centre = new BlockPos(3, 1, 3);
		VerdantBedBlockEntity own = bed(helper, centre, 4);
		VerdantBedBlockEntity east = bed(helper, centre.east(), 1);
		VerdantBedBlockEntity west = bed(helper, centre.west(), 6);
		helper.setBlock(centre.above(), Garden.MENDVETCH_CROP);
		helper.setBlock(centre.above().north(), Blocks.GLOWSTONE);
		helper.runAtTickTime(40, () -> {
			helper.setBlock(centre.above(), Garden.MENDVETCH_CROP);
			own.setNutrients(4);
			east.setNutrients(1);
			west.setNutrients(6);
			helper.assertTrue(grow(helper, centre.above()) == OrganismCropBlock.Growth.GREW, "The vetch grows");
			helper.assertTrue(own.nutrients() == 4 && east.nutrients() == 2 && west.nutrients() == 6,
					"Its nutrient went to the poorest bed: " + own.nutrients() + "/" + east.nutrients() + "/" + west.nutrients());
			helper.succeed();
		});
	}

	/**
	 * Roadmap step 29: a step hastened by a Verdant Spire grows by the crop's own rules but costs its bed at least a
	 * nutrient and fixes none. A Mendvetch, free and fixing when time grows it, pays one and feeds no neighbour; on an
	 * empty bed nothing is hastened.
	 */
	@GameTest(maxTicks = 80)
	public void aHastenedStepPaysAndFixesNothing(GameTestHelper helper) {
		BlockPos centre = new BlockPos(3, 1, 3);
		VerdantBedBlockEntity own = bed(helper, centre, 4);
		VerdantBedBlockEntity east = bed(helper, centre.east(), 1);
		VerdantBedBlockEntity empty = bed(helper, new BlockPos(6, 1, 6), 0);
		helper.setBlock(centre.above(), Garden.MENDVETCH_CROP);
		helper.setBlock(centre.above().north(), Blocks.GLOWSTONE);
		helper.setBlock(new BlockPos(6, 2, 6), Garden.MENDVETCH_CROP);
		helper.setBlock(new BlockPos(6, 2, 5), Blocks.GLOWSTONE);
		helper.runAtTickTime(40, () -> {
			ServerLevel level = level(helper);
			helper.setBlock(centre.above(), Garden.MENDVETCH_CROP);
			helper.setBlock(new BlockPos(6, 2, 6), Garden.MENDVETCH_CROP);
			own.setNutrients(4);
			east.setNutrients(1);
			empty.setNutrients(0);
			BlockState vetch = helper.getBlockState(centre.above());
			helper.assertTrue(((OrganismCropBlock) vetch.getBlock()).hasten(level, helper.absolutePos(centre.above()), vetch, level.getRandom())
					== OrganismCropBlock.Growth.GREW && age(helper, centre.above()) == 1, "The vetch is hastened a step");
			helper.assertTrue(own.nutrients() == 4 - OrganismCropBlock.HASTENED_COST && east.nutrients() == 1,
					"It paid its bed a nutrient and fed no neighbour: " + own.nutrients() + "/" + east.nutrients());
			BlockState starved = helper.getBlockState(new BlockPos(6, 2, 6));
			helper.assertTrue(((OrganismCropBlock) starved.getBlock()).hasten(level, helper.absolutePos(new BlockPos(6, 2, 6)), starved,
					level.getRandom()) == OrganismCropBlock.Growth.STALLED && age(helper, new BlockPos(6, 2, 6)) == 0 && empty.nutrients() == 0,
					"On an empty bed nothing is hastened");
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------- the producer

	/**
	 * A Verdant Heart on a lit, fed bed among three other kinds of crop beats a nutrient into Verdance; beside a Ley Pylon
	 * it pours whole batches (3 Verdance for 2 Ley Charge); on a starved bed it stops and spends nothing.
	 */
	@GameTest(maxTicks = 80)
	public void theHeartBeatsVerdanceIntoAPylon(GameTestHelper helper) {
		BlockPos at = new BlockPos(3, 1, 3);
		VerdantBedBlockEntity bed = bed(helper, at, 10);
		helper.setBlock(at.above(), Garden.VERDANT_HEART);
		VerdantHeartBlockEntity heart = entity(helper, at.above());
		helper.setBlock(at.above().north(), Blocks.GLOWSTONE);
		// Company: three kinds of crop round it.
		Block[] kinds = {Garden.SUNPETAL_CROP, Garden.DEWMOSS_CROP, Garden.MENDVETCH_CROP};
		for (int i = 0; i < kinds.length; i++) {
			BlockPos spot = at.offset(i - 1, 0, 2);
			bed(helper, spot, 0);
			helper.setBlock(spot.above(), kinds[i]);
		}
		helper.setBlock(at.above().east(), JugcraftConcordance.LEY_PYLON);
		LeyPylonBlockEntity pylon = entity(helper, at.above().east());
		helper.runAtTickTime(40, () -> {
			ServerLevel level = level(helper);
			// Woken only now, so its own pulse has not beaten before these do.
			heart.awaken(KEEPER);
			bed.setNutrients(10);
			bed.forgetSample();
			Habitat habitat = bed.habitat(level);
			helper.assertTrue(habitat != null && habitat.diversity() == 3 && habitat.disturbance() == 4,
					"Three kinds round it; the Heart and the pylon disturb it by 4: " + habitat);
			helper.assertTrue(heart.beat(level).equals("working") && heart.verdance() == 2 && bed.nutrients() == 9,
					"A thriving beat: a nutrient for 2 Verdance: " + heart.verdance() + ", " + bed.nutrients());
			helper.assertTrue(pylon.ley() == 0, "Two Verdance is not a whole batch for the pylon");
			heart.beat(level);
			helper.assertTrue(pylon.ley() == 2 && heart.verdance() == 1 && bed.nutrients() == 8,
					"Four Verdance pours one batch: 3 for 2 Ley Charge: " + pylon.ley() + ", " + heart.verdance());
			bed.setNutrients(0);
			helper.assertTrue(heart.beat(level).equals("stalled") && heart.verdance() == 1, "On a starved bed it makes nothing");
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------- the consumer

	/**
	 * Fed Verdant Chaff (two quarters each), a Mulch Maw eats one a pulse and gives each whole nutrient to the poorest
	 * bed in reach; with every bed full it holds what it digested and eats no more.
	 */
	@GameTest(maxTicks = 40)
	public void theMawFeedsThePoorestBed(GameTestHelper helper) {
		BlockPos at = new BlockPos(3, 1, 3);
		helper.setBlock(at, Garden.MULCH_MAW);
		MulchMawBlockEntity maw = entity(helper, at);
		maw.awaken(KEEPER);
		VerdantBedBlockEntity rich = bed(helper, at.offset(2, 0, 0), 5);
		VerdantBedBlockEntity poor = bed(helper, at.offset(-2, 0, 0), 3);
		helper.assertTrue(maw.feed(new ItemStack(Garden.VERDANT_CHAFF, 4)) == 4, "It takes the chaff");
		helper.assertTrue(maw.feed(new ItemStack(Blocks.STONE)) == 0, "but not stone");
		ServerLevel level = level(helper);
		helper.assertTrue(maw.digest(level).equals("working") && maw.held() == 2 && poor.nutrients() == 3, "Half a nutrient digested");
		maw.digest(level);
		helper.assertTrue(maw.held() == 0 && poor.nutrients() == 4 && rich.nutrients() == 5,
				"A whole one goes to the poorest bed: " + poor.nutrients() + "/" + rich.nutrients());
		rich.setNutrients(VerdantBedBlockEntity.CAPACITY);
		poor.setNutrients(VerdantBedBlockEntity.CAPACITY);
		maw.digest(level);
		maw.digest(level);
		helper.assertTrue(maw.digest(level).equals("beds_full") && maw.held() == 4 && maw.food().isEmpty(),
				"With every bed full it keeps its four quarters and has eaten the rest: " + maw.held());
		helper.succeed();
	}

	// ---------------------------------------------------------------- the sensor

	/** The gauge gives the bed's moisture as a signal, and judges an attuned crop: 0 in the dark. */
	@GameTest(maxTicks = 80)
	public void theGaugeReadsTheHabitat(GameTestHelper helper) {
		BlockPos at = new BlockPos(3, 1, 3);
		bed(helper, at, 10);
		helper.setBlock(at.above(), Garden.HABITAT_GAUGE);
		HabitatGaugeBlockEntity gauge = entity(helper, at.above());
		gauge.awaken(KEEPER);
		shut(helper, at.above());
		helper.runAtTickTime(40, () -> {
			ServerLevel level = level(helper);
			gauge.cycle(level);
			int moisture = helper.getBlockState(at).getValue(VerdantBedBlock.MOISTURE);
			helper.assertTrue(gauge.mode() == HabitatGaugeBlockEntity.Mode.MOISTURE && gauge.signal() == moisture * 15 / 7
					&& gauge.signal() > 0, "Reading moisture, the bed's " + moisture + " gives " + gauge.signal());
			helper.assertTrue(helper.getBlockState(at.above()).getValue(io.github.jimbozoomer.jugcraft.concordance.garden.HabitatGaugeBlock.OPEN),
					"The bulb opens while it gives a signal");
			gauge.attune(level, "jugcraft:sunpetal");
			helper.assertTrue(gauge.mode() == HabitatGaugeBlockEntity.Mode.SUITABILITY && gauge.signal() == 0,
					"A sunpetal would not grow in the dark: 0");
			gauge.attune(level, "jugcraft:gloamcap");
			helper.assertTrue(gauge.signal() == 0, "Nor a gloamcap with no magic near it");
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------- the collector

	/**
	 * A Gleaner harvests a ripe sunpetal within reach for one Verdance, drawn from a Verdant Heart its keeper shares;
	 * the crop falls back to step 1 to grow again. A Gleaner kept by someone else cannot draw from that Heart. It
	 * harvests as its keeper, who is here (roadmap step 28).
	 */
	@GameTest(maxTicks = 40)
	public void theGleanerHarvestsForVerdance(GameTestHelper helper) {
		ServerLevel level = level(helper);
		ServerPlayer keeper = helper.makeMockServerPlayerInLevel();
		BlockPos crop = new BlockPos(2, 2, 2);
		bed(helper, crop.below(), 10);
		helper.setBlock(crop, ((OrganismCropBlock) Garden.SUNPETAL_CROP).getStateForAge(OrganismCropBlock.MAX_AGE));
		BlockPos heartBed = new BlockPos(5, 1, 2);
		bed(helper, heartBed, 10);
		helper.setBlock(heartBed.above(), Garden.VERDANT_HEART);
		VerdantHeartBlockEntity heart = entity(helper, heartBed.above());
		heart.awaken(keeper.getUUID());
		heart.setVerdance(5);
		BlockPos stranger = new BlockPos(7, 2, 5);
		helper.setBlock(stranger, Garden.GLEANER);
		GleanerBlockEntity theirs = entity(helper, stranger);
		theirs.awaken(helper.makeMockServerPlayerInLevel().getUUID());
		helper.assertTrue(theirs.glean(level).equals("no_verdance") && heart.verdance() == 5,
				"A stranger's Gleaner may not draw on the Heart");
		BlockPos at = new BlockPos(3, 2, 3);
		helper.setBlock(at, Garden.GLEANER);
		GleanerBlockEntity gleaner = entity(helper, at);
		gleaner.awaken(keeper.getUUID());
		helper.assertTrue(gleaner.glean(level).equals("working"), "It gleans: " + gleaner.status());
		helper.assertTrue(heart.verdance() == 0 && gleaner.verdance() == 4, "It drew the Heart's 5 and spent one: " + gleaner.verdance());
		List<ItemStack> held = new ArrayList<>();
		for (int i = 0; i < gleaner.getContainerSize(); i++) {
			if (!gleaner.getItem(i).isEmpty()) {
				held.add(gleaner.getItem(i));
			}
		}
		helper.assertTrue(held.size() == 2 && held.get(0).is(Garden.SUNPETAL) && held.get(0).getCount() == 2
				&& held.get(1).is(Garden.VERDANT_CHAFF), "Two sunpetals and a chaff: " + held);
		helper.assertTrue(age(helper, crop) == 1, "The crop falls back to step 1");
		helper.assertTrue(gleaner.glean(level).equals("idle"), "Nothing else is ripe");
		helper.succeed();
	}

	/**
	 * Roadmap step 28: a Gleaner whose keeper is away waits and draws nothing; with the server's option a stand-in
	 * answers for them, and harvests only a crop they could harvest by hand (a neighbour's claim refuses it).
	 */
	@GameTest(maxTicks = 40)
	public void theGleanerWaitsForItsKeeper(GameTestHelper helper) {
		ServerLevel level = level(helper);
		BlockPos crop = new BlockPos(2, 2, 2);
		bed(helper, crop.below(), 10);
		helper.setBlock(crop, ((OrganismCropBlock) Garden.SUNPETAL_CROP).getStateForAge(OrganismCropBlock.MAX_AGE));
		BlockPos heartBed = new BlockPos(5, 1, 2);
		bed(helper, heartBed, 10);
		helper.setBlock(heartBed.above(), Garden.VERDANT_HEART);
		VerdantHeartBlockEntity heart = entity(helper, heartBed.above());
		heart.awaken(KEEPER);
		heart.setVerdance(5);
		BlockPos at = new BlockPos(3, 2, 3);
		helper.setBlock(at, Garden.GLEANER);
		GleanerBlockEntity gleaner = entity(helper, at);
		gleaner.awaken(KEEPER);
		helper.assertValueEqual(gleaner.glean(level), "keeper_away", "its keeper away, it waits");
		helper.assertTrue(heart.verdance() == 5 && gleaner.verdance() == 0 && age(helper, crop) == OrganismCropBlock.MAX_AGE,
				"and draws and harvests nothing");
		JugcraftConfig.setOption(Authority.ABSENT_OPTION, true);
		try (TestClaims claims = TestClaims.open()) {
			claims.block(helper.absolutePos(crop), new UUID(14L, 3L));
			helper.assertValueEqual(gleaner.glean(level), "idle", "a stand-in answers for its keeper: a neighbour's claimed crop is refused");
			helper.assertTrue(age(helper, crop) == OrganismCropBlock.MAX_AGE, "and left ripe");
			claims.close();
			helper.assertValueEqual(gleaner.glean(level), "working", "unclaimed, it is harvested for its keeper");
		} finally {
			JugcraftConfig.setOption(Authority.ABSENT_OPTION, false);
		}
		helper.assertTrue(age(helper, crop) == 1, "the crop falls back to step 1");
		helper.succeed();
	}

	// ---------------------------------------------------------------- budget and saving

	/**
	 * Twenty beds asking for a fresh reading in one tick get at most 16 samples between them (the level's allowance);
	 * the rest keep waiting and are read in the next ticks.
	 */
	@GameTest(maxTicks = 40)
	public void samplingStaysWithinTheBudget(GameTestHelper helper) {
		List<VerdantBedBlockEntity> beds = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			beds.add(bed(helper, new BlockPos(i % 5, 1, i / 5), 5));
		}
		helper.runAtTickTime(5, () -> {
			ServerLevel level = level(helper);
			int read = 0;
			for (VerdantBedBlockEntity bed : beds) {
				bed.forgetSample();
			}
			for (VerdantBedBlockEntity bed : beds) {
				if (bed.habitat(level) != null) {
					read++;
				}
			}
			int used = Garden.budget(level).used(level.getGameTime());
			helper.assertTrue(read <= SampleBudget.PER_TICK && used <= SampleBudget.PER_TICK && read < beds.size(),
					"At most 16 samples this tick: " + read + " read, " + used + " used");
		});
		helper.succeedWhen(() -> {
			ServerLevel level = level(helper);
			for (VerdantBedBlockEntity bed : beds) {
				helper.assertTrue(bed.habitat(level) != null, "Every bed is read in a later tick");
			}
		});
	}

	/** A bed keeps its nutrients and whether it is awake through a save; a Heart its Verdance. */
	@GameTest
	public void bedsAndHeartsKeepWhatTheyHold(GameTestHelper helper) {
		ServerLevel level = level(helper);
		BlockPos at = new BlockPos(2, 1, 2);
		VerdantBedBlockEntity bed = bed(helper, at, 17);
		helper.setBlock(at.above(), Garden.VERDANT_HEART);
		VerdantHeartBlockEntity heart = entity(helper, at.above());
		heart.awaken(KEEPER);
		heart.setVerdance(9);
		CompoundTag savedBed = bed.saveWithoutMetadata(level.registryAccess());
		VerdantBedBlockEntity bedCopy = new VerdantBedBlockEntity(bed.getBlockPos(), bed.getBlockState());
		bedCopy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), savedBed));
		helper.assertTrue(bedCopy.nutrients() == 17 && bedCopy.awake() && KEEPER.equals(bedCopy.keeper()), "The bed is saved exactly");
		CompoundTag savedHeart = heart.saveWithoutMetadata(level.registryAccess());
		VerdantHeartBlockEntity heartCopy = new VerdantHeartBlockEntity(heart.getBlockPos(), heart.getBlockState());
		heartCopy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), savedHeart));
		helper.assertTrue(heartCopy.verdance() == 9 && heartCopy.awake(), "The Heart keeps its Verdance");
		helper.succeed();
	}

	// ---------------------------------------------------------------- habitat made visible (Concordance Chunk C, C-07)

	/**
	 * C-07 (C-P14, C-P54): the reading the Inspect page shows. A sunpetal walled and roofed in stone and lit by one glowstone
	 * reads its light from lamps alone, and its habitat's light is the greater of sky and lamps; a dewmoss 2 blocks away counts
	 * for diversity and a gloamcap 3 away does not; a Ley Pylon in the 5 by 5 disturbs it by 2 and one 3 away does not. Each
	 * factor is judged against the sunpetal's own range by the verdict growth reaches from the same reading and names how
	 * far it reaches; reading takes, dries and notes nothing.
	 */
	@GameTest(maxTicks = 80)
	public void aBedSaysWhatItsPlantNeedsAndHowFar(GameTestHelper helper) {
		BlockPos at = new BlockPos(3, 1, 3);
		VerdantBedBlockEntity bed = bed(helper, at, 10);
		helper.setBlock(at.above(), Garden.SUNPETAL_CROP);
		shut(helper, at.above());
		helper.setBlock(at.above().north(), Blocks.GLOWSTONE);
		bed(helper, new BlockPos(5, 1, 3), 0);
		helper.setBlock(new BlockPos(5, 2, 3), Garden.DEWMOSS_CROP);
		bed(helper, new BlockPos(6, 1, 3), 0);
		helper.setBlock(new BlockPos(6, 2, 3), Garden.GLOAMCAP_CROP);
		helper.setBlock(new BlockPos(1, 2, 3), JugcraftConcordance.LEY_PYLON);
		helper.setBlock(new BlockPos(0, 2, 3), JugcraftConcordance.LEY_PYLON);
		// The area is built now and never changes: its sample is taken early, away from the other tests' sampling ticks.
		prefetch(helper, bed);
		helper.runAtTickTime(44, () -> {
			ServerLevel level = level(helper);
			helper.setBlock(at.above(), Garden.SUNPETAL_CROP); // a random tick may have grown it meanwhile
			bed.setNutrients(10);
			bed.note("waiting", List.of()); // so a reading that noted would be caught
			BlockPos plant = helper.absolutePos(at.above());
			int moisture = bed.moisture();
			HabitatInspection.State state = bed.inspect(level);
			helper.assertTrue(state != null, "The bed is read");
			Habitat habitat = state.habitat();
			helper.assertTrue(state.sky() <= 2 && state.lamps() >= 13 && state.sky() == level.getBrightness(LightLayer.SKY, plant)
					&& state.lamps() == level.getBrightness(LightLayer.BLOCK, plant) && habitat.light() == Math.max(state.sky(), state.lamps()),
					"Walled and roofed, its light is the glowstone's: sky " + state.sky() + ", lamps " + state.lamps() + ", light " + habitat.light());
			helper.assertTrue(habitat.diversity() == 2 && habitat.disturbance() == 2,
					"The dewmoss 2 away counts, the gloamcap 3 away not; the pylon in the 5 by 5 disturbs it by 2, the one 3 away not: " + habitat);
			helper.assertTrue(bed.nutrients() == 10 && bed.moisture() == moisture && bed.growth().equals("waiting") && age(helper, at.above()) == 0,
					"Reading takes, dries and notes nothing");
			Organism sunpetal = Garden.catalog().organism("jugcraft:sunpetal");
			Verdict verdict = Verdict.of(sunpetal.niche(), habitat);
			List<Text> lines = HabitatInspection.judged(state, sunpetal.niche(), verdict);
			helper.assertTrue(lines.size() == Factor.values().length, "A line for each factor");
			for (Factor factor : Factor.values()) {
				Text row = lines.get(factor.ordinal());
				Text said = (Text) row.args().get(0);
				Text judged = (Text) row.args().get(1);
				helper.assertTrue(row.key().equals("ecology.inspect.judged") && said.key().equals("ecology.inspect." + factor.id)
						&& said.args().get(0).equals(habitat.get(factor)) && judged.key().equals("ecology.inspect." + verdict.fit(factor).id),
						factor.id + " is judged as growth judges it: " + row);
			}
			helper.assertValueEqual(((Text) lines.get(Factor.LIGHT.ordinal()).args().get(0)).args(),
					List.<Object>of(habitat.light(), state.sky(), state.lamps()), "light as sky and lamps");
			helper.assertValueEqual(((Text) lines.get(Factor.DIVERSITY.ordinal()).args().get(0)).args(), List.<Object>of(2, Sampler.RADIUS),
					"diversity names its reach");
			helper.assertValueEqual(((Text) lines.get(Factor.DISTURBANCE.ordinal()).args().get(0)).args(), List.<Object>of(2, 5, 5, Sampler.HEIGHT),
					"disturbance names its reach");
			helper.assertValueEqual(((Text) lines.get(Factor.MOISTURE.ordinal()).args().get(0)).args(), List.<Object>of(moisture, 7, Garden.WATER_REACH),
					"moisture is the bed's own and names the water's reach");
			Text diversity = (Text) lines.get(Factor.DIVERSITY.ordinal()).args().get(1);
			helper.assertTrue(diversity.key().equals("ecology.inspect.tolerable") && diversity.args().equals(List.of(3, 8, 1, 8)),
					"Two kinds are only tolerable, best 3 to 8: " + diversity);
			Text light = (Text) lines.get(Factor.LIGHT.ordinal()).args().get(1);
			helper.assertTrue(light.key().equals("ecology.inspect.ideal") && light.args().equals(List.of(13, 15, 11, 15)), "Lamps alone light it well: " + light);
			Text said = HabitatInspection.verdict(verdict);
			helper.assertTrue(verdict.growth() == Verdict.Growth.TOLERATING && said.key().equals("ecology.inspect.verdict")
					&& ((Text) said.args().get(0)).key().equals("ecology.growth.tolerating"), "Tolerating, for its diversity: " + said);
			helper.assertTrue(grow(helper, at.above()) != OrganismCropBlock.Growth.WAITING && bed.growth().equals(verdict.growth().id),
					"Growth reaches the same verdict from the same reading: " + bed.growth());
			helper.succeed();
		});
	}

	/**
	 * C-07 (C-P55): a bed's verdict, sent to clients for its crop's tooltip, stays until the bed's next random tick and then
	 * clears once nothing that notes it stands on it, synced and saved cleared; a Verdant Heart's note stays (the Heart notes
	 * its bed too); a crop planted again notes it again.
	 */
	@GameTest(maxTicks = 80)
	public void aVerdictClearsWhenThePlantGoes(GameTestHelper helper) {
		BlockPos at = new BlockPos(2, 1, 2);
		VerdantBedBlockEntity bed = bed(helper, at, 10);
		helper.setBlock(at.above(), Garden.SUNPETAL_CROP);
		shut(helper, at.above());
		BlockPos heartAt = new BlockPos(6, 1, 6);
		VerdantBedBlockEntity heartBed = bed(helper, heartAt, 10);
		helper.setBlock(heartAt.above(), Garden.VERDANT_HEART);
		VerdantHeartBlockEntity heart = entity(helper, heartAt.above());
		heart.awaken(KEEPER);
		prefetch(helper, bed, heartBed);
		helper.runAtTickTime(45, () -> {
			ServerLevel level = level(helper);
			helper.assertTrue(grow(helper, at.above()) == OrganismCropBlock.Growth.STALLED && bed.growth().equals("stalled") && !bed.reasons().isEmpty(),
					"The sunpetal in the dark stalls and its bed says why: " + bed.reasons());
			helper.setBlock(at.above(), Blocks.AIR);
			helper.assertTrue(bed.growth().equals("stalled"), "Until the bed's next random tick the verdict stays");
			tickBed(helper, at);
			helper.assertTrue(bed.growth().isEmpty() && bed.reasons().isEmpty(), "With nothing on it, its random tick forgets the verdict");
			CompoundTag sent = bed.getUpdateTag(level.registryAccess());
			helper.assertTrue(sent.getStringOr("growth", "?").isEmpty() && sent.getStringOr("reasons", "?").isEmpty(), "Clients are sent none: " + sent);
			VerdantBedBlockEntity copy = reload(helper, bed, bed.saveWithoutMetadata(level.registryAccess()));
			helper.assertTrue(copy.growth().isEmpty() && copy.reasons().isEmpty() && copy.nutrients() == bed.nutrients(), "and it is saved cleared");
			heart.beat(level);
			String noted = heartBed.growth();
			tickBed(helper, heartAt);
			helper.assertTrue(!noted.isEmpty() && heartBed.growth().equals(noted), "A Verdant Heart's note stays: " + noted);
			helper.setBlock(at.above(), Garden.SUNPETAL_CROP);
			helper.assertTrue(grow(helper, at.above()) == OrganismCropBlock.Growth.STALLED && bed.growth().equals("stalled"), "Planted again, it is noted again");
			helper.succeed();
		});
	}

	/**
	 * C-07 (C-P15, C-P56): an item held on a Verdant Bed is its own use. A Gourd Canteen's sip wets a dry bed to 7, by a plain
	 * use and a sneaking one alike; a wet bed takes no sip; two sips in one tick are rate-limited to one; a neighbour's claim
	 * refuses it and spends nothing; and a seed in hand plants on the bed without sneaking.
	 */
	@GameTest(maxTicks = 20)
	public void aCanteenWatersABed(GameTestHelper helper) {
		BlockPos at = new BlockPos(2, 1, 2);
		BlockPos second = new BlockPos(4, 1, 2);
		helper.setBlock(at, Garden.VERDANT_BED.defaultBlockState().setValue(VerdantBedBlock.MOISTURE, 0));
		ServerPlayer gardener = helper.makeMockServerPlayerInLevel();
		gardener.setGameMode(GameType.SURVIVAL);
		ItemStack sips = canteen(3);
		helper.assertTrue(handUse(helper, gardener, at, sips, false).consumesAction(), "A plain use pours: the bed passes a held item on");
		helper.assertTrue(helper.getBlockState(at).getValue(VerdantBedBlock.MOISTURE) == VerdantBedBlock.WET && GourdCanteenItem.water(sips) == 2,
				"A sip wets the dry bed to 7: " + helper.getBlockState(at));
		handUse(helper, gardener, at, sips, false);
		helper.assertTrue(GourdCanteenItem.water(sips) == 2, "A wet bed takes no sip");
		helper.setBlock(at, helper.getBlockState(at).setValue(VerdantBedBlock.MOISTURE, 1));
		handUse(helper, gardener, at, sips, true);
		helper.assertTrue(helper.getBlockState(at).getValue(VerdantBedBlock.MOISTURE) == VerdantBedBlock.WET && GourdCanteenItem.water(sips) == 1,
				"A sneaking use pours too");
		ItemStack full = canteen(3);
		helper.setBlock(at, helper.getBlockState(at).setValue(VerdantBedBlock.MOISTURE, 0));
		helper.setBlock(second, Garden.VERDANT_BED.defaultBlockState().setValue(VerdantBedBlock.MOISTURE, 0));
		handUse(helper, gardener, at, full, false);
		gardener.gameMode.useItemOn(gardener, helper.getLevel(), full, InteractionHand.MAIN_HAND, top(helper, second));
		helper.assertTrue(helper.getBlockState(second).getValue(VerdantBedBlock.MOISTURE) == 0 && GourdCanteenItem.water(full) == 2,
				"A second sip in the same tick is refused and spends nothing");
		try (TestClaims claims = TestClaims.open()) {
			claims.block(helper.absolutePos(second), new UUID(14L, 9L));
			helper.assertTrue(!handUse(helper, gardener, second, full, false).consumesAction()
					&& helper.getBlockState(second).getValue(VerdantBedBlock.MOISTURE) == 0 && GourdCanteenItem.water(full) == 2,
					"A claimed bed refuses the sip and spends nothing");
		}
		ItemStack seed = new ItemStack(Garden.SUNPETAL, 2);
		handUse(helper, gardener, at, seed, false);
		helper.assertTrue(helper.getBlockState(at.above()).is(Garden.SUNPETAL_CROP) && seed.getCount() == 1, "A seed in hand plants on the bed, not sneaking");
		helper.succeed();
	}
}
