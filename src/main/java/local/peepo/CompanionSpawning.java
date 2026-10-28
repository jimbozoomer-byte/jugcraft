package local.peepo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/** Bounded supplemental natural spawning, independent of the ordinary passive-animal budget. */
final class CompanionSpawning {
    static final int INTERVAL = 20, REGION_COOLDOWN = 1200, REGION_SIZE = 128;
    static final int PLAYER_CHECKS = 8, SURFACE_CHECKS = 8, GROUP_CHECKS = 6, MAX_GROUP = 3;
    private static final Map<MinecraftServer, Schedule> SERVERS = new WeakHashMap<>();

    static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(CompanionSpawning::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(SERVERS::remove);
    }

    /** No entity/world references, saved population cache, or catch-up loop. */
    static final class Schedule {
        private final LinkedHashMap<Long, Long> regions = new LinkedHashMap<>();
        private long next;
        int playerCursor;
        Schedule(long now) { next = now + REGION_COOLDOWN; }
        boolean pulse(long now) {
            if (now < next) return false;
            next = now + INTERVAL;
            return true;
        }
        boolean claim(long now, BlockPos pos) {
            // Insertion order is expiry order; bounded pruning also handles empty/quiet servers.
            for (int i = 0; i < 8 && !regions.isEmpty(); i++) {
                var first = regions.firstEntry();
                if (first.getValue() > now) break;
                regions.pollFirstEntry();
            }
            int x = Math.floorDiv(pos.getX(), REGION_SIZE), z = Math.floorDiv(pos.getZ(), REGION_SIZE);
            // Share the minute across adjacent cells too, so straddling a border cannot multiply attempts.
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
                if (regions.getOrDefault(ChunkPos.pack(x+dx, z+dz), 0L) > now) return false;
            if (regions.size() >= 1024) return false;
            regions.put(ChunkPos.pack(x, z), now + REGION_COOLDOWN);
            return true;
        }
    }

    static boolean enabled(ServerLevel level) {
        return level.dimension().equals(Level.OVERWORLD) && level.getGameRules().get(GameRules.SPAWN_MOBS);
    }

    static void tick(MinecraftServer server) {
        var level = server.overworld();
        if (level == null) return;
        long now = level.getGameTime();
        var schedule = SERVERS.computeIfAbsent(server, s -> new Schedule(now));
        if (!enabled(level) || !schedule.pulse(now)) return;
        var players = level.players();
        // At most eight cheap player checks and one terrain/group batch per second, for the whole server.
        for (int i = 0; i < Math.min(PLAYER_CHECKS, players.size()); i++) {
            schedule.playerCursor = Math.floorMod(schedule.playerCursor, players.size());
            var player = players.get(schedule.playerCursor++);
            if (!player.isAlive() || player.isSpectator() || !schedule.claim(now, player.blockPosition())) continue;
            attemptNear(level, player, level.getRandom());
            return;
        }
    }

    static int attemptNear(ServerLevel level, ServerPlayer player, RandomSource random) {
        if (!enabled(level) || player.level() != level || !player.isAlive() || player.isSpectator()) return 0;
        for (int i = 0; i < SURFACE_CHECKS; i++) {
            double angle = random.nextDouble() * Math.PI * 2, distance = 32 + random.nextInt(49);
            int x = (int)Math.floor(player.getX() + Math.cos(angle)*distance);
            int z = (int)Math.floor(player.getZ() + Math.sin(angle)*distance);
            var pos = surface(level, x, z);
            if (pos == null) continue;
            var type = habitatType(level, pos);
            if (type == null || !validPosition(level, type, pos)) continue;
            int population = PeepoSpawns.wildCount(type, level, pos);
            String name = type == PeepoMod.JUGHEAD ? "jughead" : "peepo";
            boolean main = level.getBiome(pos).is(PeepoSpawns.habitat(name + "_main_habitat"));
            // One probability roll per admitted region, never one per sampled column/player.
            float chance = (population == 0 ? .5F : .2F) * (main ? 1F : 8F/12F);
            if (population >= PeepoSpawns.LOCAL_LIMIT || random.nextFloat() >= chance) return 0;
            return spawnGroup(level, type, pos, 1 + random.nextInt(MAX_GROUP), random);
        }
        return 0;
    }

    private static EntityType<PeepoEntity> habitatType(ServerLevel level, BlockPos pos) {
        var biome = level.getBiome(pos);
        for (String name : new String[]{"peepo", "jughead"})
            if (biome.is(PeepoSpawns.habitat(name + "_main_habitat"))
                    || biome.is(PeepoSpawns.habitat(name + "_secondary_habitat")))
                return name.equals("peepo") ? PeepoMod.PEEPO : PeepoMod.JUGHEAD;
        return null;
    }

    static BlockPos surface(ServerLevel level, int x, int z) {
        var chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
        if (chunk == null) return null;
        return new BlockPos(x, chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x & 15, z & 15) + 1, z);
    }

    static boolean validPosition(ServerLevel level, EntityType<PeepoEntity> type, BlockPos pos) {
        if (!enabled(level) || !level.getWorldBorder().isWithinBounds(pos)) return false;
        // Native placement/collision checks may read the adjacent block; never let them load a chunk.
        for (int x = (pos.getX()-1)>>4; x <= (pos.getX()+1)>>4; x++)
            for (int z = (pos.getZ()-1)>>4; z <= (pos.getZ()+1)>>4; z++)
                if (level.getChunkSource().getChunkNow(x,z) == null) return false;
        if (!level.isPositionEntityTicking(pos) || !level.canSpawnEntitiesInChunk(ChunkPos.containing(pos))) return false;
        var nearest = level.getNearestPlayer(pos.getX()+.5, pos.getY(), pos.getZ()+.5, -1, false);
        if (nearest == null) return false;
        double distance = nearest.distanceToSqr(pos.getX()+.5, pos.getY(), pos.getZ()+.5);
        if (distance <= 24*24 || distance > 96*96) return false;
        var spawn = level.getRespawnData();
        if (spawn.dimension().equals(level.dimension()) && spawn.pos().closerToCenterThan(Vec3.atBottomCenterOf(pos),24)) return false;
        // Honor datapack/environment spawn-table removal as well as the existing habitat/ground rules.
        if (level.environmentAttributes().getValue(EnvironmentAttributes.NATURAL_MOB_SPAWNS, pos)
                .getMobsToSpawn(MobCategory.CREATURE).unwrap().stream().noneMatch(e -> e.value().type() == type)) return false;
        return SpawnPlacements.isSpawnPositionOk(type, level, pos)
            && SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, level.getRandom());
    }

    static int spawnGroup(ServerLevel level, EntityType<PeepoEntity> type, BlockPos center, int requested, RandomSource random) {
        if (!enabled(level)) return 0;
        int spawned = 0, wanted = Math.clamp(requested, 0, MAX_GROUP);
        for (int i = 0; i < GROUP_CHECKS && spawned < wanted; i++) {
            var pos = i == 0 ? center : surface(level, center.getX()+random.nextInt(9)-4, center.getZ()+random.nextInt(9)-4);
            // Each successful insertion is immediately visible to the next member's population check.
            if (pos == null || !validPosition(level, type, pos)) continue;
            var npc = type.create(level, EntitySpawnReason.NATURAL);
            if (npc == null) continue;
            npc.snapTo(pos.getX()+.5, pos.getY(), pos.getZ()+.5, random.nextFloat()*360, 0);
            if (!npc.checkSpawnRules(level, EntitySpawnReason.NATURAL) || !npc.checkSpawnObstruction(level)
                    || !level.noCollision(npc)) { npc.discard(); continue; }
            npc.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.NATURAL, null);
            // Goes through Fabric's ordinary ALLOW_LOAD cancellation; never insert through a raw entity manager.
            if (level.addFreshEntity(npc)) spawned++;
            else npc.discard();
        }
        return spawned;
    }
}
