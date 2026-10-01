package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.world.ArcadeCabinetBlock;
import io.github.jimbozoomer.jugcraft.world.PixelHollows;
import io.github.jimbozoomer.jugcraft.world.PixelHollowsMaps;
import io.github.jimbozoomer.jugcraft.world.RetroTrader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Game tests for the Pixel Hollows and the Retro Trader. Measurements are logged with the prefix "[pixel-hollows]"
 * so they can be read from the build log.
 */
public class PixelHollowsGameTests {
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

	/**
	 * In the superflat test world there is no Pixel Hollows, so using the map tells the player and leaves the map as it
	 * is: it never becomes a blank or wrong map.
	 */
	@GameTest
	public void pixelHollowsMapNeedsACaveInReach(GameTestHelper helper) {
		var player = helper.makeMockServerPlayerInLevel();
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RetroTrader.PIXEL_HOLLOWS_MAP));
		RetroTrader.PIXEL_HOLLOWS_MAP.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
		helper.assertTrue(held.is(RetroTrader.PIXEL_HOLLOWS_MAP) && held.getCount() == 1, "With no cave in reach the map changed into " + held);
		helper.assertTrue(PixelHollowsMaps.find(helper.getLevel(), BlockPos.ZERO).isEmpty(), "Found a Pixel Hollows in a superflat world");
		helper.succeed();
	}

	/** A villager given the profession at a level, with the trades that level brings (the trades are data). */
	private static List<MerchantOffer> traderOffers(GameTestHelper helper, int level) {
		var villager = helper.spawn(EntityType.VILLAGER, new BlockPos(1 + level, 1, 2));
		var profession = BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(RetroTrader.PROFESSION);
		villager.setVillagerData(villager.getVillagerData().withProfession(profession).withLevel(level));
		return new ArrayList<>(villager.getOffers());
	}

	private static MerchantOffer offer(GameTestHelper helper, List<MerchantOffer> offers, net.minecraft.world.item.Item gives) {
		return offers.stream().filter(offer -> offer.getResult().is(gives)).findFirst()
				.orElseThrow(() -> helper.assertionException("No trade giving " + gives + " in " + offers.size() + " offers"));
	}

	/** Each level brings exactly the trades in tools/pixel_hollows.py; the map costs 12 emeralds and a compass. */
	@GameTest
	public void retroTraderTrades(GameTestHelper helper) {
		List<MerchantOffer> novice = traderOffers(helper, 1);
		helper.assertTrue(novice.size() == 2, "A novice Retro Trader should have 2 trades, has " + novice.size());
		MerchantOffer map = offer(helper, novice, RetroTrader.PIXEL_HOLLOWS_MAP);
		helper.assertTrue(map.getCostA().is(Items.EMERALD) && map.getCostA().getCount() == 12 && map.getCostB().is(Items.COMPASS)
				&& map.getMaxUses() == 1, "The map should cost 12 emeralds and a compass, once per restock");
		MerchantOffer stone = offer(helper, novice, PixelHollows.CIRCUITSTONE.asItem());
		helper.assertTrue(stone.getResult().getCount() == 8 && stone.getCostA().getCount() == 1, "1 emerald should buy 8 circuitstone");

		List<MerchantOffer> apprentice = traderOffers(helper, 2);
		helper.assertTrue(apprentice.size() == 2, "An apprentice should get 2 trades, got " + apprentice.size());
		offer(helper, apprentice, PixelHollows.PIXEL_LAMP.asItem());
		offer(helper, apprentice, Items.EMERALD);
		List<MerchantOffer> journeyman = traderOffers(helper, 3);
		helper.assertTrue(journeyman.size() == 1 && offer(helper, journeyman, PixelHollows.PIXEL_SHARD).getResult().getCount() == 2,
				"A journeyman should sell 2 shards");
		helper.succeed();
	}

	/**
	 * Buying shards and selling them back can never gain emeralds: the cheapest the sale can get (1 emerald for 2
	 * shards) still costs more per shard than the buyback can pay under Hero of the Village V (1 emerald for 3), and the
	 * buyback ignores reputation (its price multiplier, the trade's reputation_discount, is 0).
	 */
	@GameTest
	public void retroTraderHasNoProfitLoop(GameTestHelper helper) {
		MerchantOffer buyback = offer(helper, traderOffers(helper, 2), Items.EMERALD);
		MerchantOffer sale = offer(helper, traderOffers(helper, 3), PixelHollows.PIXEL_SHARD);
		helper.assertTrue(buyback.getCostA().is(PixelHollows.PIXEL_SHARD) && buyback.getCostA().getCount() == 6, "6 shards should buy 1 emerald");
		helper.assertTrue(buyback.getPriceMultiplier() == 0.0F, "Reputation must not discount the buyback, multiplier " + buyback.getPriceMultiplier());

		sale.setSpecialPriceDiff(-1000);
		double cheapestPerShard = sale.getCostA().getCount() / (double) sale.getResult().getCount();
		int baseBuyback = buyback.getBaseCostA().getCount();
		buyback.setSpecialPriceDiff(-Math.max((int) Math.floor(0.55 * baseBuyback), 1));
		double bestPayPerShard = buyback.getResult().getCount() / (double) buyback.getCostA().getCount();
		Jugcraft.LOGGER.info("[pixel-hollows] cheapest shard {} emeralds, best buyback {} emeralds per shard", cheapestPerShard, bestPayPerShard);
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
		var profession = BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(RetroTrader.PROFESSION);
		helper.assertTrue(profession.value().acquirableJobSite().test(poi.get()), "The profession does not take the cabinet");
		helper.succeed();
	}

	/** An unemployed villager next to a cabinet walks to it and becomes a Retro Trader. */
	@GameTest(maxTicks = 1800)
	public void villagerClaimsTheArcadeCabinet(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 1, 1), RetroTrader.ARCADE_CABINET.defaultBlockState().setValue(ArcadeCabinetBlock.FACING,
				net.minecraft.core.Direction.SOUTH));
		helper.setBlock(new BlockPos(1, 2, 1), RetroTrader.ARCADE_CABINET.defaultBlockState()
				.setValue(ArcadeCabinetBlock.FACING, net.minecraft.core.Direction.SOUTH).setValue(ArcadeCabinetBlock.HALF, DoubleBlockHalf.UPPER));
		var villager = helper.spawn(EntityType.VILLAGER, new BlockPos(4, 1, 4));
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
