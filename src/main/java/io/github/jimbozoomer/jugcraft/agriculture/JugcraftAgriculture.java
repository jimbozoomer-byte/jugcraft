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
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
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
	/** Chance per short grass broken to drop each Jugcraft seed item (vanilla wheat seeds: 0.125). */
	public static final float GRASS_SEED_CHANCE = 0.02F;
	private static final List<String> GRASS_SEEDS = List.of("corn_kernels", "sunflower_seeds", "beans", "sweet_potato", "flax_seeds");

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
			// Wheat's properties (plant colour, crop sounds, broken by pistons, random ticks), but not instant
			// to break, so a sweep of the hand does not flatten a maze wall.
			BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).strength(0.2F);
			TALL_CROPS.put(crop, (TallCropBlock) registerBlock(crop.blockId, props -> new TallCropBlock(props, crop), properties));
		}
		crop("bean_crop", "beans", true);
		crop("sweet_potato_crop", "sweet_potato", false);
		crop("flax_crop", "flax_seeds", false);

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

		// Farm tools.
		sickle("flint_sickle", 1, 131);
		sickle("bronze_sickle", 2, 350);

		// Wild plants: natural seed sources.
		wild("wild_corn");
		wild("wild_sunflower");
		wild("wild_beans");
		wild("wild_sweet_potato");
		wild("wild_flax");

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(output -> SEEDS_TAB.forEach(output::accept));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(output -> FOOD_TAB.forEach(output::accept));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> INGREDIENT_TAB.forEach(output::accept));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> TOOL_TAB.forEach(output::accept));

		registerGrassSeeds();
		registerWorldgen();
	}

	// ---------------------------------------------------------------- acquisition

	/** Short grass sometimes drops Jugcraft seeds, like wheat seeds: every crop is reachable in any world. */
	private static void registerGrassSeeds() {
		ResourceKey<LootTable> shortGrass = ResourceKey.create(Registries.LOOT_TABLE,
				Identifier.fromNamespaceAndPath("minecraft", "blocks/short_grass"));
		LootTableEvents.MODIFY.register((key, table, source, registries) -> {
			if (!source.isBuiltin() || !key.equals(shortGrass) || !JugcraftConfig.isFeatureEnabled(FEATURE)) {
				return;
			}
			for (String seed : GRASS_SEEDS) {
				table.withPool(LootPool.lootPool()
						.add(LootItem.lootTableItem(item(seed)))
						.when(LootItemRandomChanceCondition.randomChance(GRASS_SEED_CHANCE)));
			}
		});
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

	private static void plain(String id, ResourceKey<ContextIntProvider> compost) {
		registerItem(id, Item::new, new Item.Properties().compostable(compost), INGREDIENT_TAB);
	}

	private static void seeds(String id, String crop, ResourceKey<ContextIntProvider> compost) {
		Block block = block(crop);
		registerItem(id, props -> new BlockItem(block, props), new Item.Properties().useItemDescriptionPrefix().compostable(compost), SEEDS_TAB);
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
