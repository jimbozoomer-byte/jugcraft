package io.github.jimbozoomer.jugcraft.world;

import com.google.common.collect.ImmutableSet;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.mixin.StructureTemplatePoolAccessor;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

/**
 * The Retro Trader: a villager profession whose job site is the arcade cabinet, and the Retro Game Shop that
 * brings one to some new plains villages. He always offers a Pixel Hollows map, sells a little of the cave's
 * palette and buys pixel shards back (trade numbers: TRADES in tools/pixel_hollows.py).
 *
 * <p>The {@code retro_trader} switch stops new shops, the map trade for new traders and the cabinet recipe. The
 * profession, the cabinet and existing traders and their offers stay.
 */
public final class RetroTrader {
	public static final String FEATURE = "retro_trader";
	public static final ResourceKey<VillagerProfession> PROFESSION =
			ResourceKey.create(Registries.VILLAGER_PROFESSION, Jugcraft.id("retro_trader"));
	public static final ResourceKey<PoiType> JOB_SITE = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, Jugcraft.id("arcade_cabinet"));
	public static final ResourceKey<StructureTemplatePool> PLAINS_HOUSES =
			ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.withDefaultNamespace("village/plains/houses"));
	/** The shop's template (data/jugcraft/structure/village/plains/retro_game_shop.nbt, from tools/retro_game_shop.py). */
	public static final Identifier SHOP = Jugcraft.id("village/plains/retro_game_shop");
	/** Weight among the plains houses (vanilla houses weigh 1 to 3 each); keep in sync with tools/pixel_hollows.py. */
	public static final int SHOP_WEIGHT = 1;
	public static final int MAP_EMERALDS = 12;

	public static Block ARCADE_CABINET;
	public static SoundEvent WORK_SOUND;
	public static Holder<MapDecorationType> MAP_MARKER;
	private static final Set<StructureTemplatePool> EXTENDED = Collections.newSetFromMap(new WeakHashMap<>());

	private RetroTrader() {
	}

	public static void register() {
		ResourceKey<Block> cabinetKey = ResourceKey.create(Registries.BLOCK, Jugcraft.id("arcade_cabinet"));
		ARCADE_CABINET = Registry.register(BuiltInRegistries.BLOCK, cabinetKey, new ArcadeCabinetBlock(
				BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.5F).noOcclusion()
						.lightLevel(state -> state.getValue(ArcadeCabinetBlock.HALF) == DoubleBlockHalf.UPPER ? 6 : 0)
						.setId(cabinetKey)));
		ResourceKey<Item> cabinetItem = ResourceKey.create(Registries.ITEM, Jugcraft.id("arcade_cabinet"));
		Registry.register(BuiltInRegistries.ITEM, cabinetItem,
				new BlockItem(ARCADE_CABINET, new Item.Properties().setId(cabinetItem).useBlockDescriptionPrefix()));

		Identifier workSound = Jugcraft.id("entity.villager.work_retro_trader");
		WORK_SOUND = Registry.register(BuiltInRegistries.SOUND_EVENT, workSound, SoundEvent.createVariableRangeEvent(workSound));
		MAP_MARKER = Registry.registerForHolder(BuiltInRegistries.MAP_DECORATION_TYPE, Jugcraft.id("pixel_hollows"),
				new MapDecorationType(Jugcraft.id("pixel_hollows"), true, 0x2A9D8F, true, false));

		// The lower half is the job site (unemployed villagers claim it through #minecraft:acquirable_job_site).
		List<BlockState> jobSite = ARCADE_CABINET.getStateDefinition().getPossibleStates().stream()
				.filter(state -> state.getValue(ArcadeCabinetBlock.HALF) == DoubleBlockHalf.LOWER).toList();
		PointOfInterestHelper.register(Jugcraft.id("arcade_cabinet"), 1, 1, jobSite);
		Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, PROFESSION, new VillagerProfession(
				Component.translatable("entity.jugcraft.villager.retro_trader"),
				poi -> poi.is(JOB_SITE), poi -> poi.is(JOB_SITE), ImmutableSet.of(), ImmutableSet.of(), WORK_SOUND));
		registerTrades();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> addShopToVillages(server.registryAccess()));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> output.accept(ARCADE_CABINET));
	}

	/**
	 * Two trades at novice and apprentice (a villager takes two per level, so these always appear), one at
	 * journeyman. Keep in sync with TRADES and MAP_TRADE in tools/pixel_hollows.py.
	 */
	private static void registerTrades() {
		TradeOfferHelper.registerVillagerOffers(PROFESSION, 1, factories -> {
			if (JugcraftConfig.isFeatureEnabled(FEATURE)) {
				factories.add(new PixelHollowsMapListing());
			}
			factories.add(trade(Items.EMERALD, 1, PixelHollows.CIRCUITSTONE, 8, 12, 1, 0.05F));
		});
		TradeOfferHelper.registerVillagerOffers(PROFESSION, 2, factories -> {
			// Buyback: price multiplier 0, so reputation discounts never make selling shards pay more than buying them.
			factories.add(trade(PixelHollows.PIXEL_SHARD, 6, Items.EMERALD, 1, 12, 5, 0.0F));
			factories.add(trade(Items.EMERALD, 3, PixelHollows.PIXEL_LAMP, 1, 4, 5, 0.05F));
		});
		TradeOfferHelper.registerVillagerOffers(PROFESSION, 3, factories ->
				factories.add(trade(Items.EMERALD, 4, PixelHollows.PIXEL_SHARD, 2, 3, 10, 0.05F)));
	}

	private static VillagerTrades.ItemListing trade(ItemLike wants, int wantsCount, ItemLike gives, int givesCount, int uses,
			int xp, float priceMultiplier) {
		return (level, trader, random) -> new MerchantOffer(new ItemCost(wants, wantsCount), new ItemStack(gives, givesCount),
				uses, xp, priceMultiplier);
	}

	/** Adds the shop to the plains village houses once per server (village pools are fixed while a world runs). */
	public static void addShopToVillages(RegistryAccess registries) {
		if (!JugcraftConfig.isFeatureEnabled(FEATURE)) {
			return;
		}
		StructureTemplatePool pool = registries.lookupOrThrow(Registries.TEMPLATE_POOL).getValue(PLAINS_HOUSES);
		if (pool == null || !EXTENDED.add(pool)) {
			return;
		}
		StructurePoolElement shop = StructurePoolElement.single(SHOP.toString()).apply(StructureTemplatePool.Projection.RIGID);
		for (int i = 0; i < SHOP_WEIGHT; i++) {
			((StructureTemplatePoolAccessor) pool).jugcraft$templates().add(shop);
		}
		Jugcraft.LOGGER.info("Retro Game Shop added to the plains village houses (weight {})", SHOP_WEIGHT);
	}

	/** The shop's pool element, if this server's plains villages have one (for tests). */
	public static Optional<StructurePoolElement> shopElement(RegistryAccess registries) {
		StructureTemplatePool pool = registries.lookupOrThrow(Registries.TEMPLATE_POOL).getValue(PLAINS_HOUSES);
		if (pool == null) {
			return Optional.empty();
		}
		return ((StructureTemplatePoolAccessor) pool).jugcraft$templates().stream()
				.filter(element -> element.toString().contains(SHOP.toString())).findFirst();
	}
}
