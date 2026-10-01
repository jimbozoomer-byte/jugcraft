package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Agriculture branch: crops, seeds, foods and farm tools (docs/branches/AGRICULTURE.md).
 * Keep the IDs and numbers here in sync with tools/agriculture.py; the checker compares them.
 * Everything is always registered; the {@value #FEATURE} switch only turns off recipes, wild
 * plants in new chunks and seeds from grass, so saved crops and items survive.
 */
public final class JugcraftAgriculture {
	public static final String FEATURE = "agriculture";
	/** Chance per short grass broken to drop one Jugcraft seed, chosen evenly: as often as vanilla wheat seeds. */
	public static final float GRASS_SEED_CHANCE = 0.125F;
	private static final List<String> GRASS_SEEDS = List.of("corn_kernels", "sunflower_seeds", "beans", "sweet_potato", "flax_seeds",
			"tomato_seeds", "pepper_seeds", "onion", "garlic", "cabbage_seeds", "oat_seeds", "barley_seeds",
			"butternut_squash_seeds", "acorn_squash_seeds", "warty_gourd_seeds", "turnip", "cranberries", "chestnut");
	/** The chestnut tree's feature (data/jugcraft/worldgen/feature/chestnut.json), grown by its sapling. */
	public static final ResourceKey<Feature> CHESTNUT_TREE = ResourceKey.create(Registries.FEATURE, Jugcraft.id("chestnut"));
	public static final TreeGrower CHESTNUT_GROWER = new TreeGrower(Jugcraft.MOD_ID + "_chestnut", WeightedList.of(CHESTNUT_TREE),
			WeightedList.of(), WeightedList.of(), CHESTNUT_TREE);

	private static final ResourceKey<ContextIntProvider> COMPOST_LOW = ContextIntProviders.COMPOSTABLE_LOW;
	private static final ResourceKey<ContextIntProvider> COMPOST_MEDIUM = ContextIntProviders.COMPOSTABLE_MEDIUM;
	private static final ResourceKey<ContextIntProvider> COMPOST_MEDIUM_HIGH = ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH;

	public static final Map<TallCrop, TallCropBlock> TALL_CROPS = new EnumMap<>(TallCrop.class);
	private static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
	private static final Map<String, Item> ITEMS = new LinkedHashMap<>();
	private static final List<Item> SEEDS_TAB = new ArrayList<>();
	private static final List<Item> FOOD_TAB = new ArrayList<>();
	private static final List<Item> INGREDIENT_TAB = new ArrayList<>();
	private static final List<Item> TOOL_TAB = new ArrayList<>();
	private static final List<Item> EQUIPMENT_TAB = new ArrayList<>();
	private static final List<Item> BUILDING_TAB = new ArrayList<>();

	public static RecipeType<CookingPotRecipe> POT_COOKING;
	public static RecipeSerializer<CookingPotRecipe> POT_SERIALIZER;
	public static BlockEntityType<CookingPotBlockEntity> COOKING_POT_ENTITY;
	public static BlockEntityType<CarvedPumpkinBlockEntity> CARVED_PUMPKIN_ENTITY;
	/** A hand-carved pumpkin's design, on its item (copied from and to the block entity). */
	public static DataComponentType<PumpkinCarving> CARVING;
	public static ExtendedMenuType<CookingPotMenu, BlockPos> COOKING_POT_MENU;

	private JugcraftAgriculture() {
	}

	/** A registered agriculture item by ID, such as {@code "corn"}. */
	public static Item item(String id) {
		Item item = ITEMS.get(id);
		if (item == null) {
			throw new IllegalArgumentException("No Jugcraft agriculture item " + id);
		}
		return item;
	}

	/** A registered agriculture block by ID, such as {@code "corn_crop"}. */
	public static Block block(String id) {
		Block block = BLOCKS.get(id);
		if (block == null) {
			throw new IllegalArgumentException("No Jugcraft agriculture block " + id);
		}
		return block;
	}

	public static void register() {
		// Crops. None has a block item: its seed places it.
		for (TallCrop crop : TallCrop.values()) {
			// Wheat's properties (plant colour, crop sounds, broken by pistons, random ticks). Tall and climbing
			// plants are not instant to break, so a sweep of the hand does not flatten a maze wall or a trellis row.
			BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT);
			if (crop.trellis || crop.height(TallCropBlock.MAX_AGE) > 1) {
				properties = properties.strength(0.2F);
			}
			TALL_CROPS.put(crop, (TallCropBlock) registerBlock(crop.blockId, props -> new TallCropBlock(props, crop), properties));
		}
		crop("bean_crop", "beans", true);
		crop("sweet_potato_crop", "sweet_potato", false);
		crop("flax_crop", "flax_seeds", false);
		crop("onion_crop", "onion", false);
		crop("garlic_crop", "garlic", false);
		crop("cabbage_crop", "cabbage_seeds", false);
		crop("oat_crop", "oat_seeds", false);
		crop("barley_crop", "barley_seeds", false);
		// Festival crops: turnips, gourds on stems, the cranberry bog bush and the chestnut tree.
		crop("turnip_crop", "turnip", false);
		gourd("butternut_squash", "butternut_squash_seeds", 1.0F, MapColor.TERRACOTTA_ORANGE);
		gourd("acorn_squash", "acorn_squash_seeds", 1.0F, MapColor.COLOR_GREEN);
		gourd("warty_gourd", "warty_gourd_seeds", 1.0F, MapColor.COLOR_YELLOW);
		registerBlock("cranberry_bush", CranberryBushBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH)
				.sound(SoundType.WET_GRASS));
		registerChestnutTree();

		// Seeds, produce and food.
		food("corn", 3, 0.6F, COMPOST_MEDIUM);
		seeds("corn_kernels", "corn_crop", COMPOST_LOW);
		food("roasted_corn", 5, 0.6F, COMPOST_MEDIUM_HIGH);
		food("popcorn", 2, 0.3F, COMPOST_MEDIUM_HIGH);
		seeds("sunflower_seeds", "sunflower_crop", COMPOST_LOW);
		food("roasted_sunflower_seeds", 2, 0.3F, COMPOST_MEDIUM_HIGH);
		seeds("beans", "bean_crop", COMPOST_MEDIUM);
		edibleSeeds("sweet_potato", "sweet_potato_crop", 2, 0.3F, COMPOST_MEDIUM);
		food("baked_sweet_potato", 5, 0.6F, COMPOST_MEDIUM_HIGH);
		plain("flax", COMPOST_MEDIUM);
		seeds("flax_seeds", "flax_crop", COMPOST_LOW);
		stew("three_sisters_stew", 10, 0.6F);
		// Kitchen Garden.
		food("tomato", 3, 0.3F, COMPOST_MEDIUM);
		trellisSeeds("tomato_seeds", TallCrop.TOMATO, COMPOST_LOW);
		food("pepper", 2, 0.3F, COMPOST_MEDIUM);
		seeds("pepper_seeds", "pepper_crop", COMPOST_LOW);
		seeds("onion", "onion_crop", COMPOST_MEDIUM);
		seeds("garlic", "garlic_crop", COMPOST_MEDIUM);
		food("cabbage", 3, 0.6F, COMPOST_MEDIUM);
		seeds("cabbage_seeds", "cabbage_crop", COMPOST_LOW);
		plain("oats", COMPOST_MEDIUM);
		seeds("oat_seeds", "oat_crop", COMPOST_LOW);
		plain("barley", COMPOST_MEDIUM);
		seeds("barley_seeds", "barley_crop", COMPOST_LOW);
		food("barley_bread", 5, 0.6F, COMPOST_MEDIUM_HIGH);
		food("sauerkraut", 4, 0.6F, COMPOST_MEDIUM_HIGH);
		stew("garden_salad", 7, 0.6F);
		stew("tomato_soup", 8, 0.6F);
		stew("onion_soup", 8, 0.6F);
		stew("vegetable_soup", 10, 0.6F);
		stew("mushroom_barley_soup", 8, 0.6F);
		stew("oat_porridge", 6, 0.6F);
		stew("chili", 10, 0.8F);
		meal("cabbage_rolls", 6, 0.8F);
		// Festival crops.
		seeds("butternut_squash_seeds", "butternut_squash_stem", COMPOST_LOW);
		seeds("acorn_squash_seeds", "acorn_squash_stem", COMPOST_LOW);
		seeds("warty_gourd_seeds", "warty_gourd_stem", COMPOST_LOW);
		edibleSeeds("turnip", "turnip_crop", 3, 0.6F, COMPOST_MEDIUM);
		edibleSeeds("cranberries", "cranberry_bush", 2, 0.1F, COMPOST_LOW);
		seeds("chestnut", "chestnut_sapling", COMPOST_LOW);
		food("roasted_chestnuts", 4, 0.6F, COMPOST_MEDIUM_HIGH);
		food("baked_acorn_squash", 6, 0.6F, COMPOST_MEDIUM_HIGH);
		food("squash_pie", 8, 0.3F, COMPOST_MEDIUM_HIGH);
		food("candy_corn", 2, 0.1F, COMPOST_MEDIUM_HIGH);
		stew("butternut_squash_soup", 8, 0.6F);
		stew("harvest_stew", 10, 0.6F);
		stew("cranberry_sauce", 5, 0.6F);
		food("roasted_pumpkin_seeds", 2, 0.3F, COMPOST_MEDIUM_HIGH);

		// Farm tools.
		sickle("flint_sickle", 1, 131);
		sickle("bronze_sickle", 2, 350);

		// Wild plants: natural seed sources.
		wild("wild_corn");
		wild("wild_sunflower");
		wild("wild_beans");
		wild("wild_sweet_potato");
		wild("wild_flax");
		wild("wild_tomato");
		wild("wild_pepper");
		wild("wild_onion");
		wild("wild_garlic");
		wild("wild_cabbage");
		wild("wild_oats");
		wild("wild_barley");
		wild("wild_turnip");

		registerEquipment();
		registerDecorations();
		registerCarving();

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(output -> SEEDS_TAB.forEach(output::accept));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(output -> FOOD_TAB.forEach(output::accept));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> INGREDIENT_TAB.forEach(output::accept));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> TOOL_TAB.forEach(output::accept));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> EQUIPMENT_TAB.forEach(output::accept));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> BUILDING_TAB.forEach(output::accept));

		registerGrassSeeds();
		registerWorldgen();
	}

	// ---------------------------------------------------------------- acquisition

	/**
	 * Short grass sometimes drops a Jugcraft seed, like wheat seeds: every crop is reachable in any world.
	 * One pool picks one seed evenly, so adding crops never makes grass drop more seeds overall.
	 */
	private static void registerGrassSeeds() {
		ResourceKey<LootTable> shortGrass = ResourceKey.create(Registries.LOOT_TABLE,
				Identifier.fromNamespaceAndPath("minecraft", "blocks/short_grass"));
		LootTableEvents.MODIFY.register((key, table, source, registries) -> {
			if (!source.isBuiltin() || !key.equals(shortGrass) || !JugcraftConfig.isFeatureEnabled(FEATURE)) {
				return;
			}
			LootPool.Builder pool = LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(GRASS_SEED_CHANCE));
			for (String seed : GRASS_SEEDS) {
				pool.add(LootItem.lootTableItem(item(seed)));
			}
			table.withPool(pool);
		});
	}

	/** The trellis for climbing crops and the Cooking Pot, with its block entity, screen and recipe type. */
	private static void registerEquipment() {
		Block trellis = registerBlock("trellis", TrellisBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
				.strength(1.0F).sound(SoundType.SCAFFOLDING).noOcclusion());
		registerItem("trellis", props -> new BlockItem(trellis, props), new Item.Properties().useBlockDescriptionPrefix(), EQUIPMENT_TAB);
		Block pot = registerBlock("cooking_pot", CookingPotBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
				.strength(2.0F).requiresCorrectToolForDrops().sound(SoundType.LANTERN).noOcclusion());
		registerItem("cooking_pot", props -> new BlockItem(pot, props), new Item.Properties().useBlockDescriptionPrefix(), EQUIPMENT_TAB);

		COOKING_POT_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Jugcraft.id("cooking_pot"),
				FabricBlockEntityTypeBuilder.create(CookingPotBlockEntity::new, pot).build());
		COOKING_POT_MENU = Registry.register(BuiltInRegistries.MENU, Jugcraft.id("cooking_pot"),
				new ExtendedMenuType<>((containerId, inventory, pos) -> new CookingPotMenu(containerId, inventory), BlockPos.STREAM_CODEC.cast()));
		POT_COOKING = Registry.register(BuiltInRegistries.RECIPE_TYPE, Jugcraft.id("pot_cooking"), new RecipeType<CookingPotRecipe>() {
			@Override
			public String toString() {
				return Jugcraft.MOD_ID + ":pot_cooking";
			}
		});
		POT_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Jugcraft.id("pot_cooking"),
				new RecipeSerializer<>(CookingPotRecipe.CODEC, CookingPotRecipe.STREAM_CODEC));
		CookingPotRecipe.registerReloadListener();
	}

	/** The Turnip Lantern: a carved turnip that gives light, the original jack-o'-lantern. */
	private static void registerDecorations() {
		Block lantern = registerBlock("turnip_lantern", TurnipLanternBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
				.strength(0.5F).sound(SoundType.WOOD).lightLevel(state -> 13).noOcclusion());
		registerItem("turnip_lantern", props -> new BlockItem(lantern, props), new Item.Properties().useBlockDescriptionPrefix(), EQUIPMENT_TAB);
	}

	/**
	 * Pumpkin carving: the Carving Knife, and the hand-carved pumpkin it makes (any face on any side,
	 * lit by a torch). See {@link PumpkinCarvings}.
	 */
	private static void registerCarving() {
		CARVING = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Jugcraft.id("carving"),
				DataComponentType.<PumpkinCarving>builder().persistent(PumpkinCarving.CODEC).networkSynchronized(PumpkinCarving.STREAM_CODEC).build());
		Block pumpkin = registerBlock("hand_carved_pumpkin", CarvedPumpkinBlock::new,
				BlockBehaviour.Properties.ofFullCopy(Blocks.CARVED_PUMPKIN).lightLevel(CarvedPumpkinBlock::light));
		registerItem("hand_carved_pumpkin", props -> new BlockItem(pumpkin, props), new Item.Properties().useBlockDescriptionPrefix(), EQUIPMENT_TAB);
		CARVED_PUMPKIN_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Jugcraft.id("hand_carved_pumpkin"),
				FabricBlockEntityTypeBuilder.create(CarvedPumpkinBlockEntity::new, pumpkin).build());
		registerItem("carving_knife", CarvingKnifeItem::new, new Item.Properties().durability(CARVING_KNIFE_DURABILITY), TOOL_TAB);
		PumpkinCarvings.register();
	}

	/** Uses of a Carving Knife: one per finished carving (shears' durability). */
	public static final int CARVING_KNIFE_DURABILITY = 238;

	/**
	 * The chestnut tree: its sapling (planted from a chestnut), fruiting leaves and a small wood set.
	 * Logs and wood strip with any axe; everything wooden burns like oak.
	 */
	private static void registerChestnutTree() {
		registerBlock("chestnut_sapling", props -> new SaplingBlock(CHESTNUT_GROWER, props) {
		}, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING));
		Block leaves = registerBlock("chestnut_leaves", ChestnutLeavesBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
				.mapColor(MapColor.PLANT));
		registerItem("chestnut_leaves", props -> new BlockItem(leaves, props), new Item.Properties().useBlockDescriptionPrefix(), SEEDS_TAB);

		BlockBehaviour.Properties log = BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).mapColor(MapColor.TERRACOTTA_BROWN);
		Block chestnutLog = registerBlock("chestnut_log", props -> new StrippableLogBlock(props, "stripped_chestnut_log"), log);
		Block wood = registerBlock("chestnut_wood", props -> new StrippableLogBlock(props, "stripped_chestnut_wood"),
				BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).mapColor(MapColor.TERRACOTTA_BROWN));
		Block strippedLog = registerBlock("stripped_chestnut_log", RotatedPillarBlock::new,
				BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG).mapColor(MapColor.COLOR_BROWN));
		Block strippedWood = registerBlock("stripped_chestnut_wood", RotatedPillarBlock::new,
				BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD).mapColor(MapColor.COLOR_BROWN));
		Block planks = registerBlock("chestnut_planks", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).mapColor(MapColor.COLOR_BROWN));
		Block stairs = registerBlock("chestnut_stairs", props -> new StairBlock(planks.defaultBlockState(), props) {
		}, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS).mapColor(MapColor.COLOR_BROWN));
		Block slab = registerBlock("chestnut_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB).mapColor(MapColor.COLOR_BROWN));
		Block fence = registerBlock("chestnut_fence", FenceBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE).mapColor(MapColor.COLOR_BROWN));
		Block gate = registerBlock("chestnut_fence_gate", props -> new FenceGateBlock(WoodType.OAK, props),
				BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE).mapColor(MapColor.COLOR_BROWN));
		// Furnace fuel like oak: a slab burns half as long as a block.
		for (Block block : List.of(chestnutLog, wood, strippedLog, strippedWood, planks, stairs, slab, fence, gate)) {
			String id = BuiltInRegistries.BLOCK.getKey(block).getPath();
			registerItem(id, props -> new BlockItem(block, props), new Item.Properties().useBlockDescriptionPrefix()
					.cookingFuel(block == slab ? ContextIntProviders.COOKING_TIME_WOOD_SLABS : ContextIntProviders.COOKING_TIME_WOOD_BLOCKS),
					BUILDING_TAB);
		}

		// Vanilla oak's fire behaviour: logs catch slowly, planks and their shapes faster, leaves fastest.
		FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
		for (Block block : List.of(chestnutLog, wood, strippedLog, strippedWood)) {
			fire.add(block, 5, 5);
		}
		for (Block block : List.of(planks, stairs, slab, fence, gate)) {
			fire.add(block, 5, 20);
		}
		fire.add(leaves, 30, 60);
	}

	/** Wild plant patches (data/jugcraft/worldgen) in the biomes each crop comes from. New chunks only. */
	private static void registerWorldgen() {
		if (!JugcraftConfig.isFeatureEnabled(FEATURE)) {
			Jugcraft.LOGGER.info("Wild crop worldgen disabled by config");
			return;
		}
		wildPatch("wild_corn", ConventionalBiomeTags.IS_PLAINS, ConventionalBiomeTags.IS_SAVANNA);
		wildPatch("wild_sunflower", ConventionalBiomeTags.IS_PLAINS);
		wildPatch("wild_beans", ConventionalBiomeTags.IS_FOREST, ConventionalBiomeTags.IS_JUNGLE);
		wildPatch("wild_sweet_potato", ConventionalBiomeTags.IS_SAVANNA, ConventionalBiomeTags.IS_JUNGLE);
		wildPatch("wild_flax", ConventionalBiomeTags.IS_PLAINS, ConventionalBiomeTags.IS_FLORAL);
		wildPatch("wild_tomato", ConventionalBiomeTags.IS_JUNGLE, ConventionalBiomeTags.IS_SAVANNA);
		wildPatch("wild_pepper", ConventionalBiomeTags.IS_SAVANNA, ConventionalBiomeTags.IS_BADLANDS);
		wildPatch("wild_onion", ConventionalBiomeTags.IS_PLAINS, ConventionalBiomeTags.IS_HILL);
		wildPatch("wild_garlic", ConventionalBiomeTags.IS_FOREST, ConventionalBiomeTags.IS_TAIGA);
		wildPatch("wild_cabbage", ConventionalBiomeTags.IS_WINDSWEPT, ConventionalBiomeTags.IS_HILL);
		wildPatch("wild_oats", ConventionalBiomeTags.IS_PLAINS, ConventionalBiomeTags.IS_TAIGA);
		wildPatch("wild_barley", ConventionalBiomeTags.IS_SAVANNA, ConventionalBiomeTags.IS_HILL);
		wildPatch("wild_turnip", ConventionalBiomeTags.IS_TAIGA, ConventionalBiomeTags.IS_BIRCH_FOREST);
		// Festival crops found as themselves: gourds on grass, ripe cranberries in swamp shallows, chestnut trees.
		wildPatch("butternut_squash", ConventionalBiomeTags.IS_PLAINS, ConventionalBiomeTags.IS_SAVANNA);
		wildPatch("acorn_squash", ConventionalBiomeTags.IS_FOREST, ConventionalBiomeTags.IS_TAIGA);
		wildPatch("warty_gourd", ConventionalBiomeTags.IS_SWAMP, ConventionalBiomeTags.IS_SPOOKY);
		wildPatch("cranberry_bush", ConventionalBiomeTags.IS_SWAMP);
		wildPatch("chestnut_tree", ConventionalBiomeTags.IS_FOREST);
	}

	@SafeVarargs
	private static void wildPatch(String name, TagKey<Biome>... biomes) {
		Predicate<BiomeSelectionContext> selector = BiomeSelectors.tag(biomes[0]);
		for (int i = 1; i < biomes.length; i++) {
			selector = selector.or(BiomeSelectors.tag(biomes[i]));
		}
		ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, Jugcraft.id("patch_" + name));
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld().and(selector), GenerationStep.Decoration.VEGETAL_DECORATION, key);
	}

	// ---------------------------------------------------------------- registration helpers

	private static Block registerBlock(String id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Jugcraft.id(id));
		Block block = Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
		BLOCKS.put(id, block);
		return block;
	}

	private static Item registerItem(String id, Function<Item.Properties, Item> factory, Item.Properties properties, List<Item> tab) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Jugcraft.id(id));
		Item item = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
		ITEMS.put(id, item);
		tab.add(item);
		return item;
	}

	private static void crop(String id, String seedId, boolean legume) {
		registerBlock(id, props -> new JugcraftCropBlock(props, seedId, legume), BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT));
	}

	/** Shapes of each gourd facing north or south, then east or west (models: tools/festival_data.py). */
	private static final Map<String, VoxelShape[]> GOURD_SHAPES = Map.of(
			"butternut_squash", new VoxelShape[] {Block.box(4.0, 0.0, 1.0, 12.0, 8.0, 15.0), Block.box(1.0, 0.0, 4.0, 15.0, 8.0, 12.0)},
			"acorn_squash", new VoxelShape[] {Block.box(3.0, 0.0, 3.0, 13.0, 11.0, 13.0), Block.box(3.0, 0.0, 3.0, 13.0, 11.0, 13.0)},
			"warty_gourd", new VoxelShape[] {Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0), Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0)});

	/** A squash or gourd block (with its item), its stem and its attached stem. The seeds are registered with the other items. */
	private static void gourd(String id, String seedId, float growthTime, MapColor color) {
		VoxelShape[] shapes = GOURD_SHAPES.get(id);
		Block gourd = registerBlock(id, props -> new GourdBlock(props, shapes[0], shapes[1]),
				BlockBehaviour.Properties.ofFullCopy(Blocks.PUMPKIN).mapColor(color).noOcclusion());
		registerItem(id, props -> new BlockItem(gourd, props), new Item.Properties().useBlockDescriptionPrefix().compostable(COMPOST_MEDIUM),
				SEEDS_TAB);
		registerBlock(id + "_stem", props -> new GourdStemBlock(props, id, seedId, growthTime), BlockBehaviour.Properties.ofFullCopy(Blocks.PUMPKIN_STEM));
		registerBlock("attached_" + id + "_stem", props -> new AttachedGourdStemBlock(props, id, seedId),
				BlockBehaviour.Properties.ofFullCopy(Blocks.ATTACHED_PUMPKIN_STEM));
	}

	private static FoodProperties nourishment(int nutrition, float saturation) {
		return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
	}

	private static void food(String id, int nutrition, float saturation, ResourceKey<ContextIntProvider> compost) {
		registerItem(id, Item::new, new Item.Properties().food(nourishment(nutrition, saturation)).compostable(compost), FOOD_TAB);
	}

	/** A food that composters refuse, like vanilla cooked meat. */
	private static void meal(String id, int nutrition, float saturation) {
		registerItem(id, Item::new, new Item.Properties().food(nourishment(nutrition, saturation)), FOOD_TAB);
	}

	private static void plain(String id, ResourceKey<ContextIntProvider> compost) {
		registerItem(id, Item::new, new Item.Properties().compostable(compost), INGREDIENT_TAB);
	}

	private static void seeds(String id, String crop, ResourceKey<ContextIntProvider> compost) {
		Block block = block(crop);
		registerItem(id, props -> new BlockItem(block, props), new Item.Properties().useItemDescriptionPrefix().compostable(compost), SEEDS_TAB);
	}

	/** Seeds of a climbing crop: planted on a trellis standing on farmland. */
	private static void trellisSeeds(String id, TallCrop crop, ResourceKey<ContextIntProvider> compost) {
		registerItem(id, props -> new TrellisSeedItem(props, crop), new Item.Properties().compostable(compost), SEEDS_TAB);
	}

	/** A crop planted from its own produce that can also be eaten raw, like a potato. */
	private static void edibleSeeds(String id, String crop, int nutrition, float saturation, ResourceKey<ContextIntProvider> compost) {
		Block block = block(crop);
		registerItem(id, props -> new BlockItem(block, props), new Item.Properties().useItemDescriptionPrefix()
				.food(nourishment(nutrition, saturation)).compostable(compost), FOOD_TAB);
	}

	private static void stew(String id, int nutrition, float saturation) {
		registerItem(id, Item::new, new Item.Properties().food(nourishment(nutrition, saturation)).usingConvertsTo(Items.BOWL).stacksTo(1),
				FOOD_TAB);
	}

	private static void sickle(String id, int radius, int durability) {
		registerItem(id, props -> new SickleItem(props, radius), new Item.Properties().durability(durability), TOOL_TAB);
	}

	private static void wild(String id) {
		// Sweet berry bush properties: plant colour, no collision, broken by pistons; wheat's colour depends on its age.
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).sound(SoundType.GRASS);
		Block block = registerBlock(id, WildCropBlock::new, properties);
		registerItem(id, props -> new BlockItem(block, props), new Item.Properties().useBlockDescriptionPrefix().compostable(COMPOST_MEDIUM),
				SEEDS_TAB);
	}
}
