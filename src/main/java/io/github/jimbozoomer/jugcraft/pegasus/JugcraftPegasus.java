package io.github.jimbozoomer.jugcraft.pegasus;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * The Pegasus (docs/features/pegasus.md): its entity {@code jugcraft:pegasus}, its spawn egg and the pilot's input
 * packet. In survival a wild Pegasus is found alone, rarely, on high mountain grass (biome tag
 * {@code minecraft:is_mountain}: meadows and cherry groves, at {@value #HIGH_GROUND} or above, one natural spawn try in
 * {@value #RARITY} kept); two tamed Pegasi breed more (docs/features/horse-breeds.md#crossbreeding). Taming, saddling and
 * riding are vanilla's horse rules, with no research needed to fly it.
 */
public final class JugcraftPegasus {
	public static final String ID = "pegasus";
	/** The pilot's key mappings (client: PegasusClient), named here so shared tooltips can show their bindings. */
	public static final String ASCEND_KEY = "key.jugcraft.pegasus_ascend";
	public static final String DESCEND_KEY = "key.jugcraft.pegasus_descend";
	public static EntityType<Pegasus> PEGASUS;
	public static Item SPAWN_EGG;
	/** Natural spawning: weight among a mountain biome's animals, the lowest height, and one kept try in this many. */
	public static final int SPAWN_WEIGHT = 1;
	public static final int HIGH_GROUND = 100;
	public static final int RARITY = 3;

	private JugcraftPegasus() {
	}

	public static void register() {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Jugcraft.id(ID));
		// A vanilla horse's size, eye height and seat (EntityType.HORSE's numbers), so its saddle, rider and hitbox match.
		PEGASUS = Registry.register(BuiltInRegistries.ENTITY_TYPE, key, EntityType.Builder.<Pegasus>of(Pegasus::new, MobCategory.CREATURE)
				.sized(1.3964844F, 1.6F).eyeHeight(1.52F).passengerAttachments(1.31875F).clientTrackingRange(10).build(key));
		FabricDefaultAttributeRegistry.register(PEGASUS, Pegasus.createAttributes());
		ResourceKey<Item> eggKey = ResourceKey.create(Registries.ITEM, Jugcraft.id(ID + "_spawn_egg"));
		SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM, eggKey, new PegasusSpawnEggItem(new Item.Properties().setId(eggKey).spawnEgg(PEGASUS)));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> entries.accept(SPAWN_EGG));
		PayloadTypeRegistry.serverboundPlay().register(PegasusInputPayload.TYPE, PegasusInputPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PegasusInputPayload.TYPE, (payload, context) -> receive(context.player(), payload));
		SpawnPlacements.register(PEGASUS, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, JugcraftPegasus::highMeadow);
		BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_MOUNTAIN), MobCategory.CREATURE, PEGASUS, SPAWN_WEIGHT, 1, 1);
	}

	/** A wild Pegasus stands on grass, high up, in daylight, and rarely; eggs, breeding and commands go anywhere. */
	public static boolean highMeadow(EntityType<Pegasus> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
			return true;
		}
		return pos.getY() >= HIGH_GROUND && random.nextInt(RARITY) == 0 && level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)
				&& level.getRawBrightness(pos, 0) > 8;
	}

	/**
	 * A pilot packet, on the server thread: dropped unless its sender rides the very pegasus it names and is that
	 * pegasus's controlling passenger, the input holds only the two key bits, and the sequence is newer than the last.
	 */
	public static Pegasus.InputResult receive(ServerPlayer player, PegasusInputPayload payload) {
		if (!(player.getVehicle() instanceof Pegasus pegasus)) {
			return Pegasus.InputResult.NOT_RIDING;
		}
		if (pegasus.getId() != payload.vehicleId()) {
			return Pegasus.InputResult.WRONG_VEHICLE;
		}
		return pegasus.acceptInput(player, payload.input(), payload.sequence());
	}
}
