package local.peepo;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.entity.EntityTypeTest;

/** Biome tags are optional integrations: Jugcraft need not be installed. */
public final class PeepoSpawns {
    public static final int LOCAL_LIMIT = 4;
    public static final int LOCAL_RADIUS = 128;
    public static TagKey<Biome> habitat(String name) {
        return TagKey.create(Registries.BIOME, PeepoMod.id(name));
    }
    public static void initialize() {
        register(PeepoMod.PEEPO, "peepo");
        register(PeepoMod.JUGHEAD, "jughead");
        CompanionSpawning.initialize();
    }
    private static void register(EntityType<PeepoEntity> type, String name) {
        var main = habitat(name + "_main_habitat");
        var secondary = habitat(name + "_secondary_habitat");
        BiomeModifications.addSpawn(c -> c.hasTag(main), MobCategory.CREATURE, type, 12, 1, 4);
        BiomeModifications.addSpawn(c -> c.hasTag(secondary) && !c.hasTag(main), MobCategory.CREATURE, type, 8, 1, 4);
        SpawnPlacements.register(type, SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PeepoSpawns::canSpawn);
    }
    public static boolean belowLocalLimit(EntityType<?> type, ServerLevelAccessor world, BlockPos pos) {
        return wildCount(type, world, pos) < LOCAL_LIMIT;
    }
    /** Loaded, naturally acquired, unowned population only. Stop once the limit is reached. */
    static int wildCount(EntityType<?> type, ServerLevelAccessor world, BlockPos pos) {
        var level = world.getLevel();
        var bounds = new AABB(pos.getX()-LOCAL_RADIUS, level.getMinY(), pos.getZ()-LOCAL_RADIUS,
            pos.getX()+LOCAL_RADIUS+1, level.getMaxY(), pos.getZ()+LOCAL_RADIUS+1);
        boolean jughead = type == PeepoMod.JUGHEAD;
        var found = new java.util.ArrayList<PeepoEntity>(LOCAL_LIMIT);
        level.getEntities(EntityTypeTest.forClass(PeepoEntity.class), bounds, m -> m.isAlive()
            && m.isNaturallySpawned() && !m.orders.tamed() && m.isJughead() == jughead
            && Math.pow(m.getX()-pos.getX()-.5, 2)+Math.pow(m.getZ()-pos.getZ()-.5, 2)
                <= LOCAL_RADIUS*LOCAL_RADIUS, found, LOCAL_LIMIT);
        return found.size();
    }
    public static boolean canSpawn(EntityType<PeepoEntity> type, ServerLevelAccessor world,
            EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        // Generate through the live spawn cycle, where each new group member is visible to the cap.
        // World generation runs before neighboring entities are loaded and cannot enforce that cap.
        if (reason == EntitySpawnReason.CHUNK_GENERATION) return false;
        if (reason != EntitySpawnReason.NATURAL) return true;
        String name = type == PeepoMod.JUGHEAD ? "jughead" : "peepo";
        var biome = world.getBiome(pos);
        var ground = world.getBlockState(pos.below());
        boolean soil = ground.is(Blocks.GRASS_BLOCK) || ground.is(BlockTags.DIRT) || ground.is(BlockTags.SAND)
            || ground.is(Blocks.MUD) || ground.is(Blocks.MOSS_BLOCK) || ground.is(Blocks.CALCITE);
        return world.getLevel().dimension().equals(Level.OVERWORLD)
            && (biome.is(habitat(name+"_main_habitat")) || biome.is(habitat(name+"_secondary_habitat")))
            && soil && world.getFluidState(pos).isEmpty()
            && pos.getY() >= world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ())
            && belowLocalLimit(type, world, pos);
    }
}
