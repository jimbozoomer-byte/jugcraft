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
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

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
			"tomato_seeds", "pepper_seeds", "onion", "garlic", "cabbage_seeds", "oat_seeds", "barley_seeds");

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

	public static RecipeType<CookingPotRecipe> POT_COOKING;
	public static RecipeSerializer<CookingPotRecipe> POT_SERIALIZER;
	public static BlockEntityType<CookingPotBlockEntity> COOKING_POT_ENTITY;
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

		registerEquipment();

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(output -> SEEDS_TAB.forEach(output::accept));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(output -> FOOD_TAB.forEach(output::accept));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> INGREDIENT_TAB.forEach(output::accept));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> TOOL_TAB.forEach(output::accept));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> EQUIPMENT_TAB.forEach(output::accept));

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
