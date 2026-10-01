package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.world.ArcadeCabinetBlock;
import io.github.jimbozoomer.jugcraft.world.PixelHollows;
import io.github.jimbozoomer.jugcraft.world.PixelHollowsMapListing;
import io.github.jimbozoomer.jugcraft.world.PixelHollowsMaps;
import io.github.jimbozoomer.jugcraft.world.RetroTrader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Game tests for the Pixel Hollows and the Retro Trader. The distribution test samples the real Overworld climate
 * (with the Pixel Hollows added) on ten fixed seeds without generating chunks, and logs what it measured with the
 * prefix "[pixel-hollows]" so the numbers can be read from the build log.
 */
public class PixelHollowsGameTests {
	private static final long[] SEEDS = {1L, 2L, 3L, 42L, 1234L, 8675309L, -1L, 20261001L, 777L, 31337L};

	private static int count(List<ItemStack> drops, net.minecraft.world.item.Item item) {
		return drops.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
	}

	/** A cluster drops 1-2 shards; Silk Touch takes the cluster; Fortune III adds at most three. */
	@GameTest
	public void pixelCrystalClusterDrops(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		BlockState cluster = PixelHollows.PIXEL_CRYSTAL_CLUSTER.defaultBlockState();
		ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
		boolean sawOne = false;
		boolean sawTwo = false;
		for (int i = 0; i < 64; i++) {
			List<ItemStack> drops = Block.getDrops(cluster, level, pos, null, null, pickaxe);
			int shards = count(drops, PixelHollows.PIXEL_SHARD);
			helper.assertTrue(shards >= 1 && shards <= 2 && count(drops, PixelHollows.PIXEL_CRYSTAL_CLUSTER.asItem()) == 0,
					"A cluster dropped " + drops);
			sawOne |= shards == 1;
			sawTwo |= shards == 2;
		}
		helper.assertTrue(sawOne && sawTwo, "Cluster drops should vary between 1 and 2 shards");

		var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		ItemStack silk = new ItemStack(Items.IRON_PICKAXE);
		silk.enchant(enchantments.getOrThrow(Enchantments.SILK_TOUCH), 1);
		List<ItemStack> silkDrops = Block.getDrops(cluster, level, pos, null, null, silk);
		helper.assertTrue(count(silkDrops, PixelHollows.PIXEL_CRYSTAL_CLUSTER.asItem()) == 1
				&& count(silkDrops, PixelHollows.PIXEL_SHARD) == 0, "Silk Touch should take the cluster, got " + silkDrops);

		ItemStack fortune = new ItemStack(Items.IRON_PICKAXE);
		fortune.enchant(enchantments.getOrThrow(Enchantments.FORTUNE), 3);
		for (int i = 0; i < 64; i++) {
			int shards = count(Block.getDrops(cluster, level, pos, null, null, fortune), PixelHollows.PIXEL_SHARD);
			helper.assertTrue(shards >= 1 && shards <= 5, "Fortune III gave " + shards + " shards");
		}
		helper.succeed();
	}

	/** Circuitstone needs a pickaxe and drops itself; the lamp drops itself to any tool. */
	@GameTest
	public void pixelHollowsBlocksDropThemselves(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		for (Block block : new Block[] {PixelHollows.CIRCUITSTONE, PixelHollows.POLISHED_CIRCUITSTONE, PixelHollows.CIRCUITSTONE_BRICKS}) {
			List<ItemStack> drops = Block.getDrops(block.defaultBlockState(), level, pos, null, null, new ItemStack(Items.WOODEN_PICKAXE));
			helper.assertTrue(count(drops, block.asItem()) == 1, block + " dropped " + drops);
			helper.assertTrue(block.defaultBlockState().requiresCorrectToolForDrops(), block + " should need a pickaxe");
		}
		List<ItemStack> lamp = Block.getDrops(PixelHollows.PIXEL_LAMP.defaultBlockState(), level, pos, null, null, ItemStack.EMPTY);
		helper.assertTrue(count(lamp, PixelHollows.PIXEL_LAMP.asItem()) == 1, "The lamp dropped " + lamp);
		helper.assertTrue(PixelHollows.PIXEL_LAMP.defaultBlockState().getLightEmission() == 15, "The lamp should give full light");
		helper.assertTrue(PixelHollows.PIXEL_CRYSTAL_CLUSTER.defaultBlockState().getLightEmission() == 3, "Clusters should glow faintly (3)");
		helper.assertTrue(!PixelHollows.PIXEL_CRYSTAL_CLUSTER.defaultBlockState().isRandomlyTicking(), "Clusters must not grow");
		helper.succeed();
	}

	/** 4 shards + glass make one lamp; the cabinet takes 2 shards; the stonecutter cuts circuitstone. */
	@GameTest
	public void pixelHollowsRecipes(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var recipes = level.getServer().getRecipeManager();
		ItemStack s = new ItemStack(PixelHollows.PIXEL_SHARD);
		CraftingInput lampInput = CraftingInput.of(3, 3, List.of(ItemStack.EMPTY, s, ItemStack.EMPTY, s, new ItemStack(Items.GLASS), s,
				ItemStack.EMPTY, s, ItemStack.EMPTY));
		ItemStack lamp = recipes.getRecipeFor(RecipeType.CRAFTING, lampInput, level)
				.orElseThrow(() -> helper.assertionException("No pixel lamp recipe")).value().assemble(lampInput);
		helper.assertTrue(lamp.is(PixelHollows.PIXEL_LAMP.asItem()) && lamp.getCount() == 1, "4 shards + glass made " + lamp);

		ItemStack p = new ItemStack(Items.OAK_PLANKS);
		CraftingInput cabinetInput = CraftingInput.of(3, 3, List.of(p, new ItemStack(Items.GLASS_PANE), p, s, new ItemStack(Items.REDSTONE), s,
				p, p, p));
		ItemStack cabinet = recipes.getRecipeFor(RecipeType.CRAFTING, cabinetInput, level)
				.orElseThrow(() -> helper.assertionException("No arcade cabinet recipe")).value().assemble(cabinetInput);
		helper.assertTrue(cabinet.is(RetroTrader.ARCADE_CABINET.asItem()), "The cabinet recipe made " + cabinet);

		SingleRecipeInput stone = new SingleRecipeInput(new ItemStack(PixelHollows.CIRCUITSTONE));
		helper.assertTrue(recipes.getRecipeFor(RecipeType.STONECUTTING, stone, level).isPresent(), "No stonecutting for circuitstone");
		helper.succeed();
	}

	private static Holder<MultiNoiseBiomeSourceParameterList> overworldPreset(ServerLevel level) {
		return level.registryAccess().lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
				.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
	}

	/** The biome is registered, and it is in the Overworld's climate table (the mixin added it). */
	@GameTest
	public void pixelHollowsJoinsTheOverworld(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(level.registryAccess().lookupOrThrow(Registries.BIOME).get(PixelHollows.BIOME).isPresent(), "No pixel_hollows biome");
		boolean listed = overworldPreset(level).value().parameters().values().stream()
				.anyMatch(entry -> entry.getSecond().is(PixelHollows.BIOME));
		helper.assertTrue(listed, "The Overworld climate table has no Pixel Hollows");
		helper.succeed();
	}

	/**
	 * Every Overworld biome's features sort into one order. A vanilla feature listed out of order in the
	 * Pixel Hollows would be a "feature order cycle", which crashes world generation.
	 */
	@GameTest
	public void overworldFeatureOrderHasNoCycle(GameTestHelper helper) {
		MultiNoiseBiomeSource source = MultiNoiseBiomeSource.createFromPreset(overworldPreset(helper.getLevel()));
		List<Holder<Biome>> biomes = List.copyOf(source.possibleBiomes());
		helper.assertTrue(biomes.stream().anyMatch(biome -> biome.is(PixelHollows.BIOME)), "Pixel Hollows is not an Overworld biome");
		FeatureSorter.buildFeaturesPerStep(biomes, biome -> biome.value().getGenerationSettings().features(), true);
		helper.succeed();
	}

	/**
	 * Measures the biome on ten fixed seeds: its share of the sampled underground (and the dripstone and lush caves'
	 * for comparison), and the bounded map search from the origin with its time. Logs the numbers.
	 */
	@GameTest(maxTicks = 400)
	public void pixelHollowsDistribution(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		MultiNoiseBiomeSource source = MultiNoiseBiomeSource.createFromPreset(overworldPreset(level));
		Map<String, Integer> totals = new HashMap<>();
		int samples = 0;
		int found = 0;
		List<Long> micros = new ArrayList<>();
		for (long seed : SEEDS) {
			Climate.Sampler sampler = RandomState.create(level.registryAccess(), NoiseGeneratorSettings.OVERWORLD, seed).sampler();
			for (int x = -4096; x <= 4096; x += 128) {
				for (int z = -4096; z <= 4096; z += 128) {
					for (int y : PixelHollowsMaps.HEIGHTS) {
						Holder<Biome> biome = source.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z), sampler);
						totals.merge(biome.getRegisteredName(), 1, Integer::sum);
						samples++;
					}
				}
			}
			long start = System.nanoTime();
			Optional<BlockPos> target = PixelHollowsMaps.find(source, sampler, BlockPos.ZERO);
			long took = (System.nanoTime() - start) / 1000;
			micros.add(took);
			if (target.isPresent()) {
				found++;
				BlockPos pos = target.get();
				helper.assertTrue(source.getNoiseBiome(QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getY()),
						QuartPos.fromBlock(pos.getZ()), sampler).is(PixelHollows.BIOME), "The map target is not in the biome");
			}
			Jugcraft.LOGGER.info("[pixel-hollows] seed {}: nearest {} ({} blocks), search {} us", seed, target.orElse(null),
					target.map(pos -> (int) Math.sqrt(pos.getX() * (double) pos.getX() + pos.getZ() * (double) pos.getZ())).orElse(-1), took);
		}
		int total = samples;
		List<Map.Entry<String, Integer>> top = totals.entrySet().stream()
				.sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())).limit(12).toList();
		for (Map.Entry<String, Integer> entry : top) {
			Jugcraft.LOGGER.info("[pixel-hollows] {}: {}% of samples", entry.getKey(), String.format("%.2f", 100.0 * entry.getValue() / total));
		}
		micros.sort(null);
		Jugcraft.LOGGER.info("[pixel-hollows] map found on {}/{} seeds; search time median {} us, worst {} us", found, SEEDS.length,
				micros.get(micros.size() / 2), micros.get(micros.size() - 1));
		int pixel = totals.entrySet().stream().filter(e -> e.getKey().contains("jugcraft:pixel_hollows")).mapToInt(Map.Entry::getValue).sum();
		int dripstone = totals.entrySet().stream().filter(e -> e.getKey().contains("minecraft:dripstone_caves")).mapToInt(Map.Entry::getValue).sum();
		helper.assertTrue(pixel > 0, "Pixel Hollows never generated in " + total + " samples");
		helper.assertTrue(pixel < dripstone, "Pixel Hollows (" + pixel + ") should be rarer than dripstone caves (" + dripstone + ")");
		helper.assertTrue(found >= SEEDS.length / 2, "The map search found the biome on only " + found + " of " + SEEDS.length + " seeds");
		helper.succeed();
	}

	/** A map built for a found target carries the Pixel Hollows marker and names the depth. */
	@GameTest
	public void pixelHollowsMapIsMarked(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos target = new BlockPos(640, -16, -320);
		ItemStack map = PixelHollowsMaps.map(level, target);
		helper.assertTrue(map.is(Items.FILLED_MAP), "Not a filled map: " + map);
		var decorations = map.get(DataComponents.MAP_DECORATIONS);
		helper.assertTrue(decorations != null && decorations.decorations().values().stream()
				.anyMatch(entry -> entry.type().equals(RetroTrader.MAP_MARKER)), "The map has no Pixel Hollows marker");
		helper.assertTrue(map.getHoverName().getString().contains("-16"), "The map's name should give the depth: " + map.getHoverName().getString());
		helper.succeed();
	}

	private static Holder<VillagerProfession> profession() {
		return BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(RetroTrader.PROFESSION);
	}

	private static List<MerchantOffer> offers(GameTestHelper helper, Villager villager, int level) {
		VillagerTrades.ItemListing[] listings = VillagerTrades.TRADES.get(RetroTrader.PROFESSION).get(level);
		List<MerchantOffer> out = new ArrayList<>();
		for (VillagerTrades.ItemListing listing : listings) {
			out.add(listing.getOffer(helper.getLevel(), villager, helper.getLevel().getRandom()));
		}
		return out;
	}

	/**
	 * A new Retro Trader has exactly his two novice trades. In the superflat test world no Pixel Hollows exists, so the
	 * map trade is permanently sold out (restocking does not change that) and shows an unfilled map, never a wrong one.
	 */
	@GameTest
	public void retroTraderNoviceTrades(GameTestHelper helper) {
		Villager villager = helper.spawn(EntityType.VILLAGER, new BlockPos(2, 1, 2));
		villager.setVillagerData(villager.getVillagerData().withProfession(profession()));
		List<MerchantOffer> novice = new ArrayList<>(villager.getOffers());
		helper.assertTrue(novice.size() == 2, "A novice Retro Trader should have 2 trades, has " + novice.size());
		MerchantOffer map = novice.stream().filter(offer -> offer.getResult().is(Items.MAP) || offer.getResult().is(Items.FILLED_MAP))
				.findFirst().orElseThrow(() -> helper.assertionException("No map trade"));
		helper.assertTrue(map.getCostA().is(Items.EMERALD) && map.getCostA().getCount() == RetroTrader.MAP_EMERALDS
				&& map.getCostB().is(Items.COMPASS), "The map should cost 12 emeralds and a compass");
		helper.assertTrue(map.getResult().is(Items.MAP) && map.isOutOfStock(), "With no cave in reach the map trade must be sold out");
		map.resetUses();
		helper.assertTrue(map.isOutOfStock(), "Restocking must not make the empty map trade buyable");
		helper.assertTrue(novice.stream().anyMatch(offer -> offer.getResult().is(PixelHollows.CIRCUITSTONE.asItem())
				&& offer.getResult().getCount() == 8), "No circuitstone trade");

		MerchantOffer direct = new PixelHollowsMapListing().getOffer(helper.getLevel(), villager, helper.getLevel().getRandom());
		helper.assertTrue(direct.getMaxUses() == 0, "The unavailable map trade must have no uses");
		helper.succeed();
	}

	/**
	 * Buying shards and selling them back can never gain emeralds: the cheapest the sale can get (1 emerald for 2
	 * shards) still costs more per shard than the buyback can pay under Hero of the Village V (1 emerald for 3), and the
	 * buyback ignores reputation (price multiplier 0).
	 */
	@GameTest
	public void retroTraderHasNoProfitLoop(GameTestHelper helper) {
		Villager villager = helper.spawn(EntityType.VILLAGER, new BlockPos(2, 1, 2));
		villager.setVillagerData(villager.getVillagerData().withProfession(profession()));
		List<MerchantOffer> apprentice = offers(helper, villager, 2);
		List<MerchantOffer> journeyman = offers(helper, villager, 3);
		MerchantOffer buyback = apprentice.stream().filter(offer -> offer.getCostA().is(PixelHollows.PIXEL_SHARD)).findFirst()
				.orElseThrow(() -> helper.assertionException("No shard buyback"));
		MerchantOffer sale = journeyman.stream().filter(offer -> offer.getResult().is(PixelHollows.PIXEL_SHARD)).findFirst()
				.orElseThrow(() -> helper.assertionException("No shard sale"));
		helper.assertTrue(buyback.getPriceMultiplier() == 0.0F, "Reputation must not discount the buyback");
		helper.assertTrue(apprentice.stream().anyMatch(offer -> offer.getResult().is(PixelHollows.PIXEL_LAMP.asItem())), "No lamp trade");

		sale.setSpecialPriceDiff(-1000);
		double cheapestPerShard = sale.getCostA().getCount() / (double) sale.getResult().getCount();
		int baseBuyback = buyback.getBaseCostA().getCount();
		buyback.setSpecialPriceDiff(-Math.max((int) Math.floor(0.55 * baseBuyback), 1));
		double bestPayPerShard = buyback.getResult().getCount() / (double) buyback.getCostA().getCount();
		helper.assertTrue(bestPayPerShard < cheapestPerShard,
				"Selling shards pays " + bestPayPerShard + " each but they can be bought for " + cheapestPerShard);
		helper.succeed();
	}

	/** The cabinet's lower half is a job site villagers can claim; its upper half is not a second one. */
	@GameTest
	public void arcadeCabinetIsAJobSite(GameTestHelper helper) {
		BlockState lower = RetroTrader.ARCADE_CABINET.defaultBlockState();
		Optional<Holder<PoiType>> poi = PoiTypes.forState(lower);
		helper.assertTrue(poi.isPresent() && poi.get().is(RetroTrader.JOB_SITE), "The cabinet is not the arcade_cabinet job site");
		helper.assertTrue(poi.get().is(PoiTypeTags.ACQUIRABLE_JOB_SITE), "Unemployed villagers cannot claim the cabinet");
		helper.assertTrue(PoiTypes.forState(lower.setValue(ArcadeCabinetBlock.HALF, DoubleBlockHalf.UPPER)).isEmpty(),
				"The upper half must not be a second job site");
		helper.assertTrue(profession().value().acquirableJobSite().test(poi.get()), "The profession does not take the cabinet");
		helper.succeed();
	}

	/** An unemployed villager next to a cabinet walks to it and becomes a Retro Trader. */
	@GameTest(maxTicks = 1800)
	public void villagerClaimsTheArcadeCabinet(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 1, 1), RetroTrader.ARCADE_CABINET.defaultBlockState().setValue(ArcadeCabinetBlock.FACING,
				net.minecraft.core.Direction.SOUTH));
		helper.setBlock(new BlockPos(1, 2, 1), RetroTrader.ARCADE_CABINET.defaultBlockState()
				.setValue(ArcadeCabinetBlock.FACING, net.minecraft.core.Direction.SOUTH).setValue(ArcadeCabinetBlock.HALF, DoubleBlockHalf.UPPER));
		Villager villager = helper.spawn(EntityType.VILLAGER, new BlockPos(4, 1, 4));
		helper.succeedWhen(() -> helper.assertTrue(villager.getVillagerData().profession().is(RetroTrader.PROFESSION),
				"The villager is still " + villager.getVillagerData().profession()));
	}

	/**
	 * The Retro Game Shop template, placed as this test's structure: the cabinet, a lit sign, the street jigsaw and a
	 * villager inside. The shop is also in this server's plains village houses; the per-house chance is logged.
	 */
	@GameTest(structure = "jugcraft:village/plains/retro_game_shop", maxTicks = 40)
	public void retroGameShopTemplate(GameTestHelper helper) {
		helper.assertBlockPresent(RetroTrader.ARCADE_CABINET, new BlockPos(7, 1, 4));
		helper.assertBlockPresent(RetroTrader.ARCADE_CABINET, new BlockPos(7, 2, 4));
		helper.assertBlockPresent(Blocks.JIGSAW, new BlockPos(4, 1, 7));
		helper.assertBlockPresent(Blocks.OAK_DOOR, new BlockPos(4, 1, 6));
		SignBlockEntity sign = helper.getBlockEntity(new BlockPos(4, 6, 7), SignBlockEntity.class);
		helper.assertTrue(sign.getFrontText().getMessage(1, false).getString().equals("Retro")
				&& sign.getFrontText().getMessage(2, false).getString().equals("Games"), "The shop sign does not read Retro Games");
		helper.assertEntityPresent(EntityType.VILLAGER);

		var pool = helper.getLevel().registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL).getValue(RetroTrader.PLAINS_HOUSES);
		helper.assertTrue(pool != null && RetroTrader.shopElement(helper.getLevel().registryAccess()).isPresent(),
				"The shop is not in the plains village houses");
		int slots = ((io.github.jimbozoomer.jugcraft.mixin.StructureTemplatePoolAccessor) pool).jugcraft$templates().size();
		Jugcraft.LOGGER.info("[pixel-hollows] Retro Game Shop: weight {} of {} in plains houses ({}% per house)", RetroTrader.SHOP_WEIGHT,
				slots, String.format("%.2f", 100.0 * RetroTrader.SHOP_WEIGHT / slots));
		helper.succeed();
	}
}
