package io.github.jimbozoomer.jugcraft.test;

import com.mojang.datafixers.util.Pair;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.SeasonalLeavesBlock;
import io.github.jimbozoomer.jugcraft.biome.JugcraftRegions;
import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.season.SeasonCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The biomes branch: Jugcraft regions, and the seasonal-forest trees (batch 1). */
public class BiomeGameTests {
	private static final Logger LOGGER = LoggerFactory.getLogger("jugcraft-test");

	private static Holder<MultiNoiseBiomeSourceParameterList> overworldPreset(ServerLevel level) {
		return level.registryAccess().lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
				.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
	}

	private static Block block(String id) {
		return JugcraftAgriculture.block(id);
	}

	// ---------------------------------------------------------------- regions

	/**
	 * The Jugcraft layout was recorded from the Overworld biome builder, every rule placed its biome, and vanilla's own
	 * table lists every Jugcraft biome once, at the unreachable climate.
	 */
	@GameTest
	public void regionLayoutFollowsItsRules(GameTestHelper helper) {
		List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> layout = JugcraftRegions.layout();
		helper.assertTrue(layout != null, "No Jugcraft layout was recorded");
		Map<String, Integer> counts = new TreeMap<>();
		for (Pair<Climate.ParameterPoint, ResourceKey<Biome>> entry : layout) {
			if (JugcraftRegions.biomes().contains(entry.getSecond())) {
				counts.merge(entry.getSecond().identifier().getPath(), 1, Integer::sum);
			}
		}
		LOGGER.info("Jugcraft layout: {} entries; Jugcraft biomes {}", layout.size(), counts);
		for (ResourceKey<Biome> biome : JugcraftRegions.biomes()) {
			helper.assertTrue(counts.getOrDefault(biome.identifier().getPath(), 0) > 0, "No climate entry became " + biome.identifier());
		}
		List<Pair<Climate.ParameterPoint, Holder<Biome>>> vanilla = overworldPreset(helper.getLevel()).value().parameters().values();
		for (ResourceKey<Biome> biome : JugcraftRegions.biomes()) {
			List<Pair<Climate.ParameterPoint, Holder<Biome>>> listed = vanilla.stream().filter(entry -> entry.getSecond().is(biome)).toList();
			helper.assertTrue(listed.size() == 1 && listed.get(0).getFirst().equals(JugcraftRegions.UNREACHABLE),
					biome.identifier() + " is listed in vanilla's table " + listed.size() + " times, not once at the unreachable climate");
		}
		helper.succeed();
	}

	/** About the configured share of the land is in Jugcraft regions, the same for a seed every time, and seeds differ. */
	@GameTest
	public void regionsSplitTheWorld(GameTestHelper helper) {
		long seed = JugcraftRegions.fingerprint(0x1234_5678_9ABCL);
		long next = JugcraftRegions.fingerprint(0x1234_5678_9ABDL);
		int inside = 0;
		int total = 0;
		int differ = 0;
		for (int x = -4096; x < 4096; x += 64) {
			for (int z = -4096; z < 4096; z += 64) {
				boolean jugcraft = JugcraftRegions.isJugcraft(seed, x, z);
				helper.assertTrue(jugcraft == JugcraftRegions.isJugcraft(seed, x, z), "Regions changed between lookups");
				inside += jugcraft ? 1 : 0;
				differ += jugcraft != JugcraftRegions.isJugcraft(next, x, z) ? 1 : 0;
				total++;
			}
		}
		double share = (double) inside / total;
		LOGGER.info("Jugcraft regions: {} of {} samples ({}%) in a 128 km square; {} differ for the next seed", inside, total,
				Math.round(share * 100), differ);
		helper.assertTrue(share > JugcraftRegions.SHARE - 0.2 && share < JugcraftRegions.SHARE + 0.2,
				"Jugcraft regions cover " + share + " of the land, far from " + JugcraftRegions.SHARE);
		helper.assertTrue(differ > total / 5, "Another seed gives almost the same regions");
		helper.succeed();
	}

	// ---------------------------------------------------------------- trees

	/** Grows a tree on dirt from a sapling-like start; returns logs and leaves counted around it. */
	private static int[] grow(GameTestHelper helper, TreeGrower grower, BlockPos at, Block sapling, String log, String leaves) {
		ServerLevel level = helper.getLevel();
		helper.setBlock(at.below(), Blocks.DIRT);
		helper.setBlock(at, sapling);
		BlockPos absolute = helper.absolutePos(at);
		boolean grown = grower.growTree(level, level.getChunkSource().getGenerator(), absolute, level.getBlockState(absolute), level.getRandom());
		helper.assertTrue(grown, "The " + log + " tree did not grow");
		int logs = 0;
		int leaf = 0;
		for (BlockPos pos : BlockPos.betweenClosed(absolute.offset(-6, 0, -6), absolute.offset(6, 24, 6))) {
			BlockState state = level.getBlockState(pos);
			logs += state.is(block(log)) ? 1 : 0;
			leaf += leaves != null && state.is(block(leaves)) ? 1 : 0;
		}
		return new int[] {logs, leaf};
	}

	/** On top of whatever closes the test area above, so a tall tree has room. */
	private static BlockPos open(GameTestHelper helper, BlockPos start) {
		for (int dy = 0; dy <= 24; dy++) {
			if (!helper.getBlockState(start.above(dy)).isAir()) {
				return start.above(dy + 1);
			}
		}
		return start;
	}

	/** Maple, aspen and fir saplings grow their trees; in autumn the maple and aspen come out in autumn colours. */
	@GameTest(maxTicks = 100)
	public void seasonalForestSaplingsGrow(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		SeasonCalendar.Settings before = JugcraftSeasons.settings();
		BlockPos at = open(helper, new BlockPos(3, 2, 3));
		String[][] trees = {{"maple", "maple_leaves"}, {"aspen", "aspen_leaves"}, {"fir", "fir_needles"}};
		TreeGrower[] growers = {JugcraftAgriculture.MAPLE_GROWER, JugcraftAgriculture.ASPEN_GROWER, JugcraftAgriculture.FIR_GROWER};
		try {
			JugcraftSeasons.setMode(server, SeasonCalendar.Mode.AUTUMN);
			for (int i = 0; i < trees.length; i++) {
				String tree = trees[i][0];
				int[] counts = grow(helper, growers[i], at.offset(0, 0, 0), block(tree + "_sapling"), tree + "_log", trees[i][1]);
				int gold = 0;
				BlockPos absolute = helper.absolutePos(at);
				for (BlockPos pos : BlockPos.betweenClosed(absolute.offset(-6, 0, -6), absolute.offset(6, 24, 6))) {
					BlockState state = helper.getLevel().getBlockState(pos);
					if (state.is(block(trees[i][1])) && state.hasProperty(SeasonalLeavesBlock.SEASON)
							&& state.getValue(SeasonalLeavesBlock.SEASON) == SeasonalLeavesBlock.Foliage.GOLD) {
						gold++;
					}
				}
				LOGGER.info("A {} grown in autumn: {} logs, {} leaves ({} in autumn colours)", tree, counts[0], counts[1], gold);
				helper.assertTrue(counts[0] >= 4 && counts[1] >= 10, "A small " + tree + ": " + counts[0] + " logs, " + counts[1] + " leaves");
				helper.assertTrue(tree.equals("fir") ? gold == 0 : gold == counts[1], tree + ": " + gold + " of " + counts[1] + " leaves in autumn colours");
				for (BlockPos pos : BlockPos.betweenClosed(absolute.offset(-6, 0, -6), absolute.offset(6, 24, 6))) {
					helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
				}
			}
		} finally {
			JugcraftSeasons.setMode(server, before.mode());
		}
		helper.succeed();
	}

	/** The dead tree (no sapling grows it) is a bare trunk of dead wood with no leaves at all. */
	@GameTest
	public void deadTreesAreBare(GameTestHelper helper) {
		BlockPos at = open(helper, new BlockPos(3, 2, 3));
		int[] counts = grow(helper, JugcraftAgriculture.DEAD_TREE_GROWER, at, Blocks.OAK_SAPLING, "dead_log", null);
		int leaves = 0;
		BlockPos absolute = helper.absolutePos(at);
		for (BlockPos pos : BlockPos.betweenClosed(absolute.offset(-6, 0, -6), absolute.offset(6, 24, 6))) {
			leaves += helper.getLevel().getBlockState(pos).is(BlockTags.LEAVES) ? 1 : 0;
		}
		LOGGER.info("A dead tree: {} dead logs, {} leaves", counts[0], leaves);
		helper.assertTrue(counts[0] >= 4 && leaves == 0, "A dead tree of " + counts[0] + " logs and " + leaves + " leaves");
		helper.succeed();
	}

	/** Every seasonal tree's leaves are green in spring and summer, in autumn colours in autumn and bare in winter. */
	@GameTest
	public void seasonalLeavesFollowTheModes(GameTestHelper helper) {
		Map<String, SeasonalLeavesBlock.Schedule> schedules = Map.of("larch", JugcraftAgriculture.LARCH_LEAVES,
				"maple", JugcraftAgriculture.MAPLE_LEAVES, "aspen", JugcraftAgriculture.ASPEN_LEAVES);
		Map<SeasonCalendar.Mode, SeasonalLeavesBlock.Foliage> wanted = Map.of(SeasonCalendar.Mode.SPRING, SeasonalLeavesBlock.Foliage.GREEN,
				SeasonCalendar.Mode.SUMMER, SeasonalLeavesBlock.Foliage.GREEN, SeasonCalendar.Mode.AUTUMN, SeasonalLeavesBlock.Foliage.GOLD,
				SeasonCalendar.Mode.WINTER, SeasonalLeavesBlock.Foliage.BARE, SeasonCalendar.Mode.OFF, SeasonalLeavesBlock.Foliage.GREEN);
		for (Map.Entry<String, SeasonalLeavesBlock.Schedule> tree : schedules.entrySet()) {
			for (Map.Entry<SeasonCalendar.Mode, SeasonalLeavesBlock.Foliage> mode : wanted.entrySet()) {
				for (BlockPos pos : BlockPos.betweenClosed(0, 60, 0, 15, 61, 15)) {
					SeasonalLeavesBlock.Foliage look = tree.getValue().on(mode.getKey().day, pos);
					helper.assertTrue(look == mode.getValue(), tree.getKey() + " leaves at " + pos + " in " + mode.getKey() + ": " + look);
				}
			}
		}
		helper.succeed();
	}

	/** Any axe strips the new woods' logs; their planks, saplings and leaves are in vanilla's tags. */
	@GameTest
	public void seasonalForestWoodsWorkLikeWood(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 2, 1);
		for (String wood : new String[] {"maple", "aspen", "fir", "dead"}) {
			helper.setBlock(pos, block(wood + "_log").defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
			Player player = helper.makeMockPlayer(GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
			helper.useBlock(pos, player);
			BlockState stripped = helper.getBlockState(pos);
			helper.assertTrue(stripped.is(block("stripped_" + wood + "_log")) && stripped.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Z,
					"An axe should strip the " + wood + " log along its axis, found " + stripped);
			helper.assertTrue(new ItemStack(JugcraftAgriculture.item(wood + "_planks")).is(ItemTags.PLANKS), wood + " planks are not planks");
		}
		for (String tree : new String[] {"maple", "aspen", "fir"}) {
			helper.assertTrue(block(tree + "_sapling").defaultBlockState().is(BlockTags.SAPLINGS), tree + " sapling is not a sapling");
		}
		for (String leaves : new String[] {"maple_leaves", "aspen_leaves", "fir_needles"}) {
			helper.assertTrue(block(leaves).defaultBlockState().is(BlockTags.LEAVES), leaves + " are not leaves");
		}
		helper.succeed();
	}
}
