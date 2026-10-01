package io.github.jimbozoomer.jugcraft.agriculture;

import com.mojang.serialization.Codec;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
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
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LanternBlock;
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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

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
			"butternut_squash_seeds", "acorn_squash_seeds", "warty_gourd_seeds", "turnip", "cranberries", "chestnut",
			"giant_pumpkin_seeds", "white_pumpkin_seeds", "jarrahdale_pumpkin_seeds", "cinderella_pumpkin_seeds", "bottle_gourd_seeds",
			"ornamental_corn_kernels");
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
	/** A Pumpkin Stencil's traced design (face 0). */
	public static DataComponentType<PumpkinCarving> STENCIL;
	/** Sips of water in a Gourd Canteen. */
	public static DataComponentType<Integer> CANTEEN_WATER;
	public static BlockEntityType<GiantPumpkinBlockEntity> GIANT_PUMPKIN_ENTITY;
	public static BlockEntityType<ScarecrowBlockEntity> SCARECROW_ENTITY;
	/** What a scarecrow wears for a head. */
	public static final TagKey<Item> SCARECROW_HEADS = TagKey.create(Registries.ITEM, Jugcraft.id("scarecrow_heads"));
	/** A giant pumpkin carried as an item: see {@link GiantPumpkinData}. */
	public static DataComponentType<GiantPumpkinData> GIANT_PUMPKIN;
	public static BlockEntityType<HarvestScaleBlockEntity> HARVEST_SCALE_ENTITY;
	/** A pumpkin boat's weight, torch and carving, on its item and its entity. */
	public static DataComponentType<PumpkinBoatData> PUMPKIN_BOAT;
	public static EntityType<PumpkinBoat> PUMPKIN_BARGE;
	public static EntityType<PumpkinBoat> PUMPKIN_RACER;
	public static BlockEntityType<RegattaFlagBlockEntity> REGATTA_FLAG_ENTITY;
	public static BlockEntityType<RegattaBuoyBlockEntity> REGATTA_BUOY_ENTITY;
	public static BlockEntityType<JudgingStandBlockEntity> JUDGING_STAND_ENTITY;
	public static BlockEntityType<GravestoneBlockEntity> GRAVESTONE_ENTITY;
	public static DataComponentType<CandyBagItem.Night> CANDY_BAG_NIGHT;
	public static EntityType<WillOWisp> WILL_O_WISP;
	public static EntityType<FlyingPumpkin> FLYING_PUMPKIN;
	public static EntityType<ThrowMarker> THROW_MARKER;
	public static EntityType<HeadlessHorseman> HEADLESS_HORSEMAN;
	public static EntityType<FlamingPumpkin> FLAMING_PUMPKIN;
	public static BlockEntityType<TrebuchetBlockEntity> TREBUCHET_ENTITY;
	/** What each kind of pumpkin becomes when first carved by hand, and the loot table its seeds come from. */
	private static final Map<Block, Block> CARVED_FROM = new HashMap<>();
	private static final Map<Block, ResourceKey<LootTable>> CARVE_LOOT = new HashMap<>();
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
		// Halloween harvest: heirloom pumpkins (all carvable), the bottle gourd, and the giant pumpkin.
		gourd("white_pumpkin", "white_pumpkin_seeds", 1.0F, MapColor.SNOW);
		gourd("jarrahdale_pumpkin", "jarrahdale_pumpkin_seeds", 1.0F, MapColor.TERRACOTTA_CYAN);
		gourd("cinderella_pumpkin", "cinderella_pumpkin_seeds", 1.0F, MapColor.COLOR_RED);
		gourd("bottle_gourd", "bottle_gourd_seeds", 1.0F, MapColor.COLOR_LIGHT_GREEN);
		registerGiantPumpkin();
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
		// Halloween harvest.
		seeds("giant_pumpkin_seeds", "giant_pumpkin_vine", COMPOST_LOW);
		plain("pumpkin_guts", COMPOST_MEDIUM);
		stew("pumpkin_soup", 8, 0.6F);
		seeds("white_pumpkin_seeds", "white_pumpkin_stem", COMPOST_LOW);
		seeds("jarrahdale_pumpkin_seeds", "jarrahdale_pumpkin_stem", COMPOST_LOW);
		seeds("cinderella_pumpkin_seeds", "cinderella_pumpkin_stem", COMPOST_LOW);
		seeds("bottle_gourd_seeds", "bottle_gourd_stem", COMPOST_LOW);
		plain("dried_bottle_gourd", COMPOST_MEDIUM);
		plain("ornamental_corn", COMPOST_MEDIUM);
		seeds("ornamental_corn_kernels", "ornamental_corn_crop", COMPOST_LOW);
		plain("corn_stalks", COMPOST_MEDIUM);
		food("caramel", 2, 0.1F, COMPOST_MEDIUM_HIGH);
		treat("caramel_apple", 6, 0.6F);
		food("popcorn_ball", 5, 0.6F, COMPOST_MEDIUM_HIGH);
		// Trick-or-treating's rare prize (only villagers hand it out).
		food("king_size_candy_bar", 8, 0.4F, COMPOST_MEDIUM_HIGH);
		// Spooky sweets from the Cooking Pot: a moment of magic each.
		sweet("glow_gum", 1, 0.1F, MobEffects.GLOWING, 30);
		sweet("ghost_taffy", 1, 0.1F, MobEffects.INVISIBILITY, 3);
		sweet("fizz_rocks", 1, 0.1F, MobEffects.JUMP_BOOST, 20);
		sweet("witchs_licorice", 1, 0.1F, MobEffects.NIGHT_VISION, 45);

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
		registerHalloween();
		registerRegatta();
		registerTrickOrTreat();
		registerFestivities();
		registerNight();

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
		List<Block> carved = new ArrayList<>();
		carved.add(carvedPumpkin("hand_carved_pumpkin", Blocks.PUMPKIN, BuiltInLootTables.CARVE_PUMPKIN, MapColor.COLOR_ORANGE));
		for (String variety : List.of("white_pumpkin", "jarrahdale_pumpkin", "cinderella_pumpkin")) {
			carved.add(carvedPumpkin("hand_carved_" + variety, block(variety),
					ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("carve/" + variety)), block(variety).defaultMapColor()));
		}
		CARVED_PUMPKIN_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Jugcraft.id("hand_carved_pumpkin"),
				FabricBlockEntityTypeBuilder.create(CarvedPumpkinBlockEntity::new, carved.toArray(Block[]::new)).build());
		registerItem("carving_knife", CarvingKnifeItem::new, new Item.Properties().durability(CARVING_KNIFE_DURABILITY), TOOL_TAB);
		PumpkinCarvings.register();
	}

	/** A hand-carved pumpkin of one kind (with its item), carved from {@code pumpkin}, whose seeds come from {@code seeds}. */
	private static Block carvedPumpkin(String id, Block pumpkin, ResourceKey<LootTable> seeds, MapColor color) {
		Block carved = registerBlock(id, CarvedPumpkinBlock::new,
				BlockBehaviour.Properties.ofFullCopy(Blocks.CARVED_PUMPKIN).mapColor(color).lightLevel(CarvedPumpkinBlock::light));
		// Worn like a vanilla carved pumpkin: on the head, seen through, never swapped on by a right click (that places it).
		Equippable worn = Equippable.builder(EquipmentSlot.HEAD).setSwappable(false)
				.setCameraOverlay(Identifier.withDefaultNamespace("misc/pumpkinblur")).build();
		registerItem(id, props -> new BlockItem(carved, props), new Item.Properties().useBlockDescriptionPrefix()
				.component(DataComponents.EQUIPPABLE, worn), EQUIPMENT_TAB);
		CARVED_FROM.put(pumpkin, carved);
		CARVE_LOOT.put(pumpkin, seeds);
		return carved;
	}

	/** The plain pumpkin a hand-carved pumpkin of this kind was carved from, or null if it is not a hand-carved pumpkin. */
	public static @Nullable Block plainPumpkin(Block carved) {
		for (Map.Entry<Block, Block> entry : CARVED_FROM.entrySet()) {
			if (entry.getValue() == carved) {
				return entry.getKey();
			}
		}
		return null;
	}

	/** The hand-carved pumpkin a plain pumpkin of this kind becomes when first carved, or null if it is not a carvable pumpkin. */
	public static @Nullable Block carvedFrom(Block pumpkin) {
		return CARVED_FROM.get(pumpkin);
	}

	/** The loot table of the seeds that come out of a pumpkin of this kind when it is first carved. */
	public static ResourceKey<LootTable> carveLoot(Block pumpkin) {
		return CARVE_LOOT.getOrDefault(pumpkin, BuiltInLootTables.CARVE_PUMPKIN);
	}

	/** Uses of a Carving Knife: one per finished carving (shears' durability). */
	public static final int CARVING_KNIFE_DURABILITY = 238;

	/**
	 * The giant pumpkin: its vine (planted from Giant Pumpkin Seeds), the vine holding a fruit, and the fruit,
	 * a block of up to 3x3x3 whose master block keeps its growth, weight and carvings. Pistons cannot move it;
	 * breaking it picks it up whole as a Giant Pumpkin item, which places it again.
	 */
	private static void registerGiantPumpkin() {
		GIANT_PUMPKIN = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Jugcraft.id("giant_pumpkin"),
				DataComponentType.<GiantPumpkinData>builder().persistent(GiantPumpkinData.CODEC)
						.networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(GiantPumpkinData.CODEC)).build());
		Block giant = registerBlock("giant_pumpkin", GiantPumpkinBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.PUMPKIN)
				.strength(3.0F).lightLevel(GiantPumpkinBlock::light).pushReaction(PushReaction.IMMOVEABLE));
		registerItem("giant_pumpkin", props -> new GiantPumpkinItem(giant, props), new Item.Properties().stacksTo(1).useBlockDescriptionPrefix()
				.component(GIANT_PUMPKIN, GiantPumpkinData.FULL_GROWN), EQUIPMENT_TAB);
		registerBlock("giant_pumpkin_vine", GiantPumpkinVineBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.PUMPKIN_STEM));
		registerBlock("attached_giant_pumpkin_vine", AttachedGiantPumpkinVineBlock::new,
				BlockBehaviour.Properties.ofFullCopy(Blocks.ATTACHED_PUMPKIN_STEM));
		GIANT_PUMPKIN_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Jugcraft.id("giant_pumpkin"),
				FabricBlockEntityTypeBuilder.create(GiantPumpkinBlockEntity::new, giant).build());
	}

	/**
	 * The rest of the Halloween harvest: the Harvest Scale and its ribbons, pumpkin stencils, the Scarecrow,
	 * the Corn Shock and Ornamental Corn Bundle, the Gourd Birdhouse and Gourd Canteen, and mums with their pots.
	 */
	private static void registerHalloween() {
		STENCIL = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Jugcraft.id("stencil"),
				DataComponentType.<PumpkinCarving>builder().persistent(PumpkinCarving.CODEC).networkSynchronized(PumpkinCarving.STREAM_CODEC).build());
		CANTEEN_WATER = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Jugcraft.id("canteen_water"),
				DataComponentType.<Integer>builder().persistent(Codec.intRange(0, GourdCanteenItem.CAPACITY))
						.networkSynchronized(ByteBufCodecs.VAR_INT).build());

		Block scale = registerBlock("harvest_scale", HarvestScaleBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
				.strength(2.0F).sound(SoundType.WOOD).noOcclusion());
		registerItem("harvest_scale", props -> new BlockItem(scale, props), new Item.Properties().useBlockDescriptionPrefix(), EQUIPMENT_TAB);
		HARVEST_SCALE_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Jugcraft.id("harvest_scale"),
				FabricBlockEntityTypeBuilder.create(HarvestScaleBlockEntity::new, scale).build());
		for (String ribbon : HarvestScaleBlockEntity.RIBBONS) {
			registerItem(ribbon, Item::new, new Item.Properties().rarity(Rarity.UNCOMMON), TOOL_TAB);
		}

		registerItem("blank_stencil", BlankStencilItem::new, new Item.Properties(), TOOL_TAB);
		registerItem("pumpkin_stencil", PumpkinStencilItem::new, new Item.Properties().stacksTo(1), TOOL_TAB);
		registerItem("gourd_canteen", GourdCanteenItem::new, new Item.Properties().stacksTo(1).component(CANTEEN_WATER, 0), TOOL_TAB);

		Block scarecrow = registerBlock("scarecrow", ScarecrowBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
				.strength(0.8F).sound(SoundType.GRASS).noOcclusion().ignitedByLava().pushReaction(PushReaction.POPPED)
				.lightLevel(ScarecrowBlock::light));
		SCARECROW_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Jugcraft.id("scarecrow"),
				FabricBlockEntityTypeBuilder.create(ScarecrowBlockEntity::new, scarecrow).build());
		Block shock = registerBlock("corn_shock", props -> new TallDecorationBlock(props, Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0),
				Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0)), BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
				.strength(0.5F).sound(SoundType.GRASS).noOcclusion().ignitedByLava().pushReaction(PushReaction.POPPED));
		Block bundle = registerBlock("ornamental_corn_bundle", props -> new WallDecorationBlock(props, 3.0, 2.0, 14.0),
				BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.3F).sound(SoundType.GRASS).noCollision()
						.noOcclusion().ignitedByLava().pushReaction(PushReaction.POPPED));
		Block birdhouse = registerBlock("gourd_birdhouse", LanternBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW)
				.strength(0.5F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.POPPED));
		for (Block block : List.of(scarecrow, shock, bundle, birdhouse)) {
			String id = BuiltInRegistries.BLOCK.getKey(block).getPath();
			registerItem(id, props -> new BlockItem(block, props), new Item.Properties().useBlockDescriptionPrefix(), EQUIPMENT_TAB);
		}
		// Straw and dry stalks burn like a hay bale.
		FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
		for (Block block : List.of(scarecrow, shock, bundle)) {
			fire.add(block, 60, 20);
		}

		mum("yellow_mum", MobEffects.SATURATION, 0.35F);
		mum("orange_mum", MobEffects.FIRE_RESISTANCE, 4.0F);
		mum("red_mum", MobEffects.REGENERATION, 8.0F);
		mum("purple_mum", MobEffects.NIGHT_VISION, 5.0F);
	}

	/**
	 * The pumpkin regatta: Pumpkin Barge and Pumpkin Racer (entities and their items, hollowed from giant
	 * pumpkins), the Regatta Flag that starts and times a run, and the numbered Regatta Buoys of its course.
	 */
	private static void registerRegatta() {
		PUMPKIN_BOAT = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Jugcraft.id("pumpkin_boat"),
				DataComponentType.<PumpkinBoatData>builder().persistent(PumpkinBoatData.CODEC).networkSynchronized(PumpkinBoatData.STREAM_CODEC)
						.build());
		PUMPKIN_BARGE = boat(PumpkinBoat.Kind.BARGE, 2.75F, 1.125F);
		PUMPKIN_RACER = boat(PumpkinBoat.Kind.RACER, 1.75F, 0.9F);
		for (PumpkinBoat.Kind kind : PumpkinBoat.Kind.values()) {
			registerItem(kind.id, props -> new PumpkinBoatItem(kind, props), new Item.Properties().stacksTo(1)
					.component(PUMPKIN_BOAT, kind.defaultData()), TOOL_TAB);
		}
		Block flag = registerBlock("regatta_flag", RegattaFlagBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
				.strength(1.0F).sound(SoundType.WOOD).noOcclusion().ignitedByLava());
		registerItem("regatta_flag", props -> new BlockItem(flag, props), new Item.Properties().useBlockDescriptionPrefix(), EQUIPMENT_TAB);
		Block buoy = registerBlock("regatta_buoy", RegattaBuoyBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
				.strength(0.5F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.POPPED));
		registerItem("regatta_buoy", props -> new PlaceOnWaterBlockItem(buoy, props), new Item.Properties().useBlockDescriptionPrefix(),
				EQUIPMENT_TAB);
		REGATTA_FLAG_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Jugcraft.id("regatta_flag"),
				FabricBlockEntityTypeBuilder.create(RegattaFlagBlockEntity::new, flag).build());
		REGATTA_BUOY_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Jugcraft.id("regatta_buoy"),
				FabricBlockEntityTypeBuilder.create(RegattaBuoyBlockEntity::new, buoy).build());
	}

	private static EntityType<PumpkinBoat> boat(PumpkinBoat.Kind kind, float width, float height) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Jugcraft.id(kind.id));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, EntityType.Builder.<PumpkinBoat>of(
				(type, level) -> new PumpkinBoat(type, level, kind), MobCategory.MISC)
				.noLootTable().sized(width, height).eyeHeight(height).clientTrackingRange(10).build(key));
	}

	/** The entity type of a kind of pumpkin boat. */
	public static EntityType<PumpkinBoat> boatType(PumpkinBoat.Kind kind) {
		return kind == PumpkinBoat.Kind.BARGE ? PUMPKIN_BARGE : PUMPKIN_RACER;
	}

	/** Costumes (worn on the head, nothing else) and the Candy Bag that knocks on villagers' doors. */
	private static void registerTrickOrTreat() {
		for (String costume : COSTUMES) {
			Equippable.Builder worn = Equippable.builder(EquipmentSlot.HEAD).setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER);
			if (costume.equals("ghost_sheet")) {
				worn.setCameraOverlay(Jugcraft.id("misc/ghost_sheet"));
			}
			registerItem(costume, Item::new, new Item.Properties().stacksTo(1).component(DataComponents.EQUIPPABLE, worn.build()), TOOL_TAB);
		}
		CANDY_BAG_NIGHT = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Jugcraft.id("candy_bag_night"),
				DataComponentType.<CandyBagItem.Night>builder().persistent(CandyBagItem.Night.CODEC)
						.networkSynchronized(CandyBagItem.Night.STREAM_CODEC).build());
		registerItem("candy_bag", CandyBagItem::new, new Item.Properties().stacksTo(1)
				.component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY), TOOL_TAB);
		TrickOrTreat.register();
	}

	/**
	 * Halloween festivities: the Judging Stand of the carving contest, costumed mobs and the Halloween Peddler
	 * (both only while the event runs), and decorations for any time of year: three gravestones to engrave,
	 * the Spun Cobweb (no sticking), the Hanging Ghost and the Candle Skull.
	 */
	private static void registerFestivities() {
		Block stand = registerBlock("judging_stand", JudgingStandBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
				.strength(1.5F).sound(SoundType.WOOD).noOcclusion().ignitedByLava());
		registerItem("judging_stand", props -> new BlockItem(stand, props), new Item.Properties().useBlockDescriptionPrefix(), EQUIPMENT_TAB);
		JUDGING_STAND_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Jugcraft.id("judging_stand"),
				FabricBlockEntityTypeBuilder.create(JudgingStandBlockEntity::new, stand).build());
		CarvingContest.register();
		CostumedMobs.register();
		HalloweenPeddler.register();

		List<Block> gravestones = new ArrayList<>();
		for (GravestoneBlock.Style style : GravestoneBlock.Style.values()) {
			Block stone = registerBlock(style.id, props -> new GravestoneBlock(props, style), BlockBehaviour.Properties.of()
					.mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE).noOcclusion());
			registerItem(style.id, props -> new BlockItem(stone, props), new Item.Properties().useBlockDescriptionPrefix(), EQUIPMENT_TAB);
			gravestones.add(stone);
		}
		GRAVESTONE_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Jugcraft.id("gravestone"),
				FabricBlockEntityTypeBuilder.create(GravestoneBlockEntity::new, gravestones.toArray(Block[]::new)).build());

		Block cobweb = registerBlock("spun_cobweb", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).noCollision()
				.strength(0.2F).sound(SoundType.COBWEB).noOcclusion().ignitedByLava().pushReaction(PushReaction.POPPED));
		Block ghost = registerBlock("hanging_ghost", HangingGhostBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
				.noCollision().strength(0.3F).sound(SoundType.WOOL).noOcclusion().ignitedByLava().pushReaction(PushReaction.POPPED));
		Block skull = registerBlock("candle_skull", CandleSkullBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
				.strength(1.0F).sound(SoundType.BONE_BLOCK).noOcclusion().lightLevel(CandleSkullBlock::light).pushReaction(PushReaction.POPPED));
		for (Block block : List.of(cobweb, ghost, skull)) {
			String id = BuiltInRegistries.BLOCK.getKey(block).getPath();
			registerItem(id, props -> new BlockItem(block, props), new Item.Properties().useBlockDescriptionPrefix(), EQUIPMENT_TAB);
		}
		// String and cloth burn like wool.
		FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
		fire.add(cobweb, 30, 60);
		fire.add(ghost, 30, 60);
	}

	/**
	 * Halloween nights: will-o'-wisps (and the Wisp in a Jar they are caught into), the Pumpkin Chunkin' Trebuchet
	 * with its flying pumpkins and landing markers, the Headless Horseman with his flaming pumpkins, lantern and
	 * cloak, and the Harvest Moon.
	 */
	private static void registerNight() {
		WILL_O_WISP = entity("will_o_wisp", EntityType.Builder.<WillOWisp>of(WillOWisp::new, MobCategory.AMBIENT).noLootTable()
				.sized(0.4F, 0.4F).eyeHeight(0.2F).clientTrackingRange(8));
		FabricDefaultAttributeRegistry.register(WILL_O_WISP, WillOWisp.createAttributes());
		Block jar = registerBlock("wisp_in_a_jar", LanternBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
				.strength(0.3F).sound(SoundType.GLASS).lightLevel(state -> WISP_JAR_LIGHT).noOcclusion().pushReaction(PushReaction.POPPED));
		registerItem("wisp_in_a_jar", props -> new BlockItem(jar, props), new Item.Properties().useBlockDescriptionPrefix(), EQUIPMENT_TAB);
		Wisps.register();

		Block trebuchet = registerBlock("trebuchet", TrebuchetBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
				.strength(2.0F).sound(SoundType.WOOD).noOcclusion().ignitedByLava());
		registerItem("trebuchet", props -> new BlockItem(trebuchet, props), new Item.Properties().useBlockDescriptionPrefix(), EQUIPMENT_TAB);
		TREBUCHET_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Jugcraft.id("trebuchet"),
				FabricBlockEntityTypeBuilder.create(TrebuchetBlockEntity::new, trebuchet).build());
		FLYING_PUMPKIN = entity("flying_pumpkin", EntityType.Builder.<FlyingPumpkin>of(FlyingPumpkin::new, MobCategory.MISC).noLootTable()
				.sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(5));
		THROW_MARKER = entity("throw_marker", EntityType.Builder.<ThrowMarker>of(ThrowMarker::new, MobCategory.MISC).noLootTable().noSave()
				.sized(0.4F, 1.2F).clientTrackingRange(8).updateInterval(40));

		HEADLESS_HORSEMAN = entity("headless_horseman", EntityType.Builder.<HeadlessHorseman>of(HeadlessHorseman::new, MobCategory.MONSTER)
				.sized(1.4F, 2.9F).eyeHeight(2.5F).fireImmune().notInPeaceful().clientTrackingRange(10));
		FabricDefaultAttributeRegistry.register(HEADLESS_HORSEMAN, HeadlessHorseman.createAttributes());
		FLAMING_PUMPKIN = entity("flaming_pumpkin", EntityType.Builder.<FlamingPumpkin>of(FlamingPumpkin::new, MobCategory.MISC).noLootTable()
				.sized(0.5F, 0.5F).fireImmune().clientTrackingRange(8).updateInterval(5));
		Block lantern = registerBlock("horseman_lantern", LanternBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE)
				.strength(1.0F).sound(SoundType.LANTERN).lightLevel(state -> 15).noOcclusion().pushReaction(PushReaction.POPPED));
		registerItem("horseman_lantern", props -> new BlockItem(lantern, props), new Item.Properties().useBlockDescriptionPrefix()
				.rarity(Rarity.EPIC), EQUIPMENT_TAB);
		Equippable cloak = Equippable.builder(EquipmentSlot.CHEST).setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER)
				.setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID, Jugcraft.id("horseman_cloak"))).build();
		registerItem("horseman_cloak", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).component(DataComponents.EQUIPPABLE, cloak),
				TOOL_TAB);
		HarvestMoon.register();
	}

	/** How brightly a Wisp in a Jar glows. */
	public static final int WISP_JAR_LIGHT = 13;

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> entity(String id, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Jugcraft.id(id));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	/** The costume hats, in the order of their tag. */
	public static final List<String> COSTUMES = List.of("witch_hat", "ghost_sheet", "scarecrow_hat");

	/** A mum (garden chrysanthemum): a small flower with its item, and its potted form (no item, like vanilla's). */
	private static void mum(String id, Holder<MobEffect> stewEffect, float seconds) {
		Block flower = registerBlock(id, props -> new FlowerBlock(stewEffect, seconds, props), BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION));
		registerItem(id, props -> new BlockItem(flower, props), new Item.Properties().useBlockDescriptionPrefix().compostable(COMPOST_MEDIUM),
				SEEDS_TAB);
		registerBlock("potted_" + id, props -> new FlowerPotBlock(flower, props), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_DANDELION));
	}

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
		// Halloween harvest: heirloom pumpkins and bottle gourds on grass, and mums in flower-rich places.
		wildPatch("white_pumpkin", ConventionalBiomeTags.IS_BIRCH_FOREST, ConventionalBiomeTags.IS_SNOWY);
		wildPatch("jarrahdale_pumpkin", ConventionalBiomeTags.IS_SAVANNA, ConventionalBiomeTags.IS_WINDSWEPT);
		wildPatch("cinderella_pumpkin", ConventionalBiomeTags.IS_PLAINS, ConventionalBiomeTags.IS_FLORAL);
		wildPatch("bottle_gourd", ConventionalBiomeTags.IS_JUNGLE, ConventionalBiomeTags.IS_SAVANNA);
		wildPatch("mums", ConventionalBiomeTags.IS_FLORAL, ConventionalBiomeTags.IS_FOREST);
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
			"warty_gourd", new VoxelShape[] {Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0), Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0)},
			"white_pumpkin", new VoxelShape[] {Shapes.block(), Shapes.block()},
			"jarrahdale_pumpkin", new VoxelShape[] {Shapes.block(), Shapes.block()},
			"cinderella_pumpkin", new VoxelShape[] {Shapes.block(), Shapes.block()},
			"bottle_gourd", new VoxelShape[] {Block.box(4.0, 0.0, 4.0, 12.0, 15.0, 12.0), Block.box(4.0, 0.0, 4.0, 12.0, 15.0, 12.0)});

	/** A squash or gourd block (with its item), its stem and its attached stem. The seeds are registered with the other items. */
	private static void gourd(String id, String seedId, float growthTime, MapColor color) {
		VoxelShape[] shapes = GOURD_SHAPES.get(id);
		// Pumpkins are whole blocks, like vanilla's; the smaller gourds let light and faces past them.
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(Blocks.PUMPKIN).mapColor(color);
		Block gourd = registerBlock(id, props -> new GourdBlock(props, shapes[0], shapes[1]),
				shapes[0] == Shapes.block() ? properties : properties.noOcclusion());
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

	/** A treat on a stick, like a caramel apple: eating it leaves the stick. */
	private static void treat(String id, int nutrition, float saturation) {
		registerItem(id, Item::new, new Item.Properties().food(nourishment(nutrition, saturation)).usingConvertsTo(Items.STICK), FOOD_TAB);
	}

	/** A spooky sweet: a bite of sugar with a short effect, eaten even on a full stomach. */
	private static void sweet(String id, int nutrition, float saturation, Holder<MobEffect> effect, int seconds) {
		FoodProperties food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).alwaysEdible().build();
		Consumable eaten = Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(effect, seconds * 20)))
				.build();
		registerItem(id, Item::new, new Item.Properties().food(food, eaten), FOOD_TAB);
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
