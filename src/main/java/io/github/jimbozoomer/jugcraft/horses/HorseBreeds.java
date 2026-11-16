package io.github.jimbozoomer.jugcraft.horses;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.pegasus.Pegasus;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * The five horse breeds' registration (docs/features/horse-breeds.md): one {@link BreedHorse} entity type per breed,
 * sized from its model (BreedDimensions), each with a spawn egg (creative Spawn Eggs tab) and wild herds in its own
 * biomes, so every breed is found in survival:
 *
 * <ul>
 * <li>Icelandic: snowy and windswept biomes ({@code c:is_snowy}, {@code c:is_windswept});</li>
 * <li>Mongolian: plains and savannas;</li>
 * <li>Shire: plains, rarer;</li>
 * <li>Giant Finnhorse: taiga, rare and alone or in pairs;</li>
 * <li>Friesian: flowery biomes ({@code c:is_floral}) and plains.</li>
 * </ul>
 * Wild herds stand on grass (or snow, for the Icelandic) in daylight, as vanilla's horses do; eggs and commands go
 * anywhere. Taming, saddling, feeding and breeding are vanilla's horse rules, with no research or season needed.
 */
public final class HorseBreeds {
	private static final Map<EntityType<?>, HorseBreed> BY_TYPE = new IdentityHashMap<>();
	private static final Map<HorseBreed, EntityType<BreedHorse>> TYPES = new EnumMap<>(HorseBreed.class);
	private static final Map<HorseBreed, Item> EGGS = new EnumMap<>(HorseBreed.class);

	private HorseBreeds() {
	}

	public static void register() {
		for (HorseBreed breed : HorseBreed.GROUND) {
			ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Jugcraft.id(breed.id()));
			BreedDimensions.Size size = BreedDimensions.of(breed.id());
			EntityType<BreedHorse> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
					EntityType.Builder.<BreedHorse>of(BreedHorse::new, MobCategory.CREATURE).sized(size.width(), size.height())
							.eyeHeight(size.eyes()).passengerAttachments(size.seat()).clientTrackingRange(10).build(key));
			TYPES.put(breed, type);
			BY_TYPE.put(type, breed);
			FabricDefaultAttributeRegistry.register(type, BreedHorse.createAttributes());
			ResourceKey<Item> eggKey = ResourceKey.create(Registries.ITEM, Jugcraft.id(breed.id() + "_spawn_egg"));
			Item egg = Registry.register(BuiltInRegistries.ITEM, eggKey,
					new BreedSpawnEggItem(new Item.Properties().setId(eggKey).spawnEgg(type), breed));
			EGGS.put(breed, egg);
			CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> entries.accept(egg));
			SpawnPlacements.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, HorseBreeds::grassland);
		}
		BiomeModifications.addSpawn(c -> c.hasTag(ConventionalBiomeTags.IS_SNOWY) || c.hasTag(ConventionalBiomeTags.IS_WINDSWEPT),
				MobCategory.CREATURE, type(HorseBreed.ICELANDIC), 3, 2, 4);
		BiomeModifications.addSpawn(c -> c.hasTag(ConventionalBiomeTags.IS_PLAINS) && !c.hasTag(ConventionalBiomeTags.IS_SNOWY),
				MobCategory.CREATURE, type(HorseBreed.MONGOLIAN), 2, 2, 4);
		BiomeModifications.addSpawn(c -> c.hasTag(ConventionalBiomeTags.IS_SAVANNA), MobCategory.CREATURE, type(HorseBreed.MONGOLIAN), 1, 2, 3);
		BiomeModifications.addSpawn(c -> c.hasTag(ConventionalBiomeTags.IS_PLAINS) && !c.hasTag(ConventionalBiomeTags.IS_SNOWY),
				MobCategory.CREATURE, type(HorseBreed.SHIRE), 1, 1, 3);
		BiomeModifications.addSpawn(c -> c.hasTag(ConventionalBiomeTags.IS_TAIGA), MobCategory.CREATURE, type(HorseBreed.GIANT_FINNHORSE), 1, 1, 2);
		BiomeModifications.addSpawn(c -> c.hasTag(ConventionalBiomeTags.IS_FLORAL), MobCategory.CREATURE, type(HorseBreed.FRIESIAN), 2, 2, 3);
		BiomeModifications.addSpawn(c -> c.hasTag(ConventionalBiomeTags.IS_PLAINS) && !c.hasTag(ConventionalBiomeTags.IS_SNOWY),
				MobCategory.CREATURE, type(HorseBreed.FRIESIAN), 1, 1, 2);
	}

	public static EntityType<BreedHorse> type(HorseBreed breed) {
		EntityType<BreedHorse> type = TYPES.get(breed);
		if (type == null) {
			throw new IllegalArgumentException("No ground breed entity for " + breed);
		}
		return type;
	}

	public static Item egg(HorseBreed breed) {
		return EGGS.get(breed);
	}

	/** The breed an entity type is (the first ground breed for anything else; only BreedHorse asks). */
	public static HorseBreed breedOf(EntityType<?> type) {
		HorseBreed breed = BY_TYPE.get(type);
		return breed != null ? breed : HorseBreed.ICELANDIC;
	}

	/** The breeding line an animal belongs to: a breed, the Pegasus, or null for any other animal. */
	public static HorseBreed line(Entity animal) {
		if (animal instanceof BreedHorse horse) {
			return horse.breed();
		}
		return animal instanceof Pegasus ? HorseBreed.PEGASUS : null;
	}

	/**
	 * Whether a pairing must be refused because it crosses lines (HorseLineageMixin, for pairings a vanilla horse or donkey
	 * starts): one of them a breed or a Pegasus and the other not, or a Pegasus with a ground breed.
	 */
	public static boolean crossesLines(Animal a, Animal b) {
		HorseBreed one = line(a);
		HorseBreed two = line(b);
		if (one == null && two == null) {
			return false;
		}
		return !HorseLineage.mayPair(one, two);
	}

	/** Natural herds stand on grass (or snow) in daylight, as horses do; eggs and commands go anywhere. */
	public static boolean grassland(EntityType<BreedHorse> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
			return true;
		}
		BlockState below = level.getBlockState(pos.below());
		boolean ground = below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
				|| breedOf(type) == HorseBreed.ICELANDIC && (below.is(Blocks.SNOW_BLOCK) || below.is(Blocks.SNOW));
		return ground && level.getRawBrightness(pos, 0) > 8;
	}
}
