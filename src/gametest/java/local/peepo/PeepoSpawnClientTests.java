package local.peepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.slf4j.LoggerFactory;

/** Full-pack habitat registration, placement, and the native natural-spawn group path. */
public final class PeepoSpawnClientTests implements FabricClientGameTest {
    private record Habitat(String biome, boolean jughead, boolean primary) {}
    private static final List<Habitat> HABITATS = List.of(
        new Habitat("minecraft:swamp", false, true),
        new Habitat("jugcraft:lush_swamp", false, true),
        new Habitat("jugcraft:bayou", false, true),
        new Habitat("jugcraft:marsh", false, false),
        new Habitat("jugcraft:wetland", false, false),
        new Habitat("jugcraft:lush_river", false, false),
        new Habitat("minecraft:beach", true, true),
        new Habitat("jugcraft:dune_beach", true, true),
        new Habitat("jugcraft:overgrown_beach", true, true),
        new Habitat("jugcraft:oasis", true, false),
        new Habitat("jugcraft:hot_springs", true, false));
    private int checks;
    private static boolean denyNaturalCompanions;

    private void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }

    @Override public void runTest(ClientGameTestContext context) {
        verifySchedule();
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ALLOW_LOAD.register((entity, level, reason, fromDisk) ->
            !denyNaturalCompanions || fromDisk || reason != EntitySpawnReason.NATURAL || !(entity instanceof PeepoEntity));
        try (var world = context.worldBuilder().adjustSettings(c -> {
            c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext()
                .lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));
            c.setGenerateStructures(false);
        }).create()) {
            var server = world.getServer();
            world.getConnection().waitForChunksRender();
            var pos = context.computeOnClient(c -> c.player.blockPosition().above(4).offset(80, 0, 0));
            server.runCommand("gamerule minecraft:do_mob_spawning false");
            server.runCommand("weather clear");
            server.runCommand("time set noon");
            server.runCommand(String.format(Locale.ROOT, "tp @a %d %d %d", pos.getX()-64, pos.getY(), pos.getZ()));
            world.getConnection().waitForChunksRender();
            server.runCommand(boxCommand("fill", pos.offset(-24, 0, -24), pos.offset(24, 6, 24), "minecraft:air"));
            server.runCommand(boxCommand("fill", pos.offset(-24, -1, -24), pos.offset(24, -1, 24), "minecraft:grass_block"));
            server.waitFor(s -> s.overworld().isPositionEntityTicking(pos)
                && s.overworld().canSpawnEntitiesInChunk(ChunkPos.containing(pos)));
            for (var habitat : HABITATS) {
                server.runCommand(boxCommand("fillbiome", pos.offset(-24, -4, -24), pos.offset(24, 8, 24), habitat.biome()));
                server.runOnServer(s -> {
                    verifyHabitat(s.overworld(), pos, habitat);
                    verifySupplemental(s.overworld(), pos, habitat);
                });
            }
            server.runCommand(boxCommand("fillbiome", pos.offset(-24, -4, -24), pos.offset(24, 8, 24), "minecraft:plains"));
            server.runOnServer(s -> {
                for (var type : List.of(PeepoMod.PEEPO, PeepoMod.JUGHEAD)) {
                    check(!allowed(type, s.overworld(), pos), "plains must reject " + type);
                    var entries = s.overworld().environmentAttributes().getValue(
                        net.minecraft.world.attribute.EnvironmentAttributes.NATURAL_MOB_SPAWNS, pos)
                        .getMobsToSpawn(MobCategory.CREATURE).unwrap();
                    check(entries.stream().noneMatch(e -> e.value().type() == type), "plains spawn table contains " + type);
                }
            });
        }
        LoggerFactory.getLogger("peepo-spawn-test").info("PASS: {} habitats, {} spawn assertions", HABITATS.size(), checks);
    }

    private static String boxCommand(String command, BlockPos min, BlockPos max, String value) {
        return String.format(Locale.ROOT, "%s %d %d %d %d %d %d %s", command,
            min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ(), value);
    }

    private boolean allowed(EntityType<PeepoEntity> type, ServerLevel level, BlockPos pos) {
        return SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, level.getRandom());
    }

    private void verifyHabitat(ServerLevel level, BlockPos pos, Habitat habitat) {
        var type = habitat.jughead() ? PeepoMod.JUGHEAD : PeepoMod.PEEPO;
        var other = habitat.jughead() ? PeepoMod.PEEPO : PeepoMod.JUGHEAD;
        String name = habitat.jughead() ? "jughead" : "peepo";
        check(level.getBiome(pos).is(PeepoSpawns.habitat(name + (habitat.primary() ? "_main_habitat" : "_secondary_habitat"))),
            "missing habitat tag: " + habitat.biome());
        var entries = level.environmentAttributes().getValue(net.minecraft.world.attribute.EnvironmentAttributes.NATURAL_MOB_SPAWNS, pos)
            .getMobsToSpawn(MobCategory.CREATURE).unwrap();
        var matches = entries.stream().filter(e -> e.value().type() == type).toList();
        check(matches.size() == 1, "expected exactly one spawn entry: " + habitat.biome());
        var entry = matches.getFirst();
        check(entry.weight() == (habitat.primary() ? 12 : 8), "spawn weight: " + habitat.biome());
        check(entry.value().count().minInclusive() == 1 && entry.value().count().maxInclusive() == 4,
            "group size: " + habitat.biome());
        check(entries.stream().noneMatch(e -> e.value().type() == other), "wrong companion in table: " + habitat.biome());
        check(!allowed(other, level, pos), "wrong companion allowed: " + habitat.biome());
        check(!SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.CHUNK_GENERATION, pos, level.getRandom()),
            "chunk-generation spawning bypasses local cap: " + habitat.biome());
        for (var soil : List.of(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.SAND, Blocks.MUD, Blocks.MOSS_BLOCK, Blocks.CALCITE)) {
            level.setBlockAndUpdate(pos.below(), soil.defaultBlockState());
            check(allowed(type, level, pos), "ground rejected: " + habitat.biome() + "/" + soil);
            check(SpawnPlacements.isSpawnPositionOk(type, level, pos), "native ground placement rejected: " + habitat.biome() + "/" + soil);
        }
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        check(!allowed(type, level, pos), "stone allowed: " + habitat.biome());
        level.setBlockAndUpdate(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
        check(!allowed(type, level, pos), "underwater allowed: " + habitat.biome());
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos.above(3), Blocks.STONE.defaultBlockState());
        check(!allowed(type, level, pos), "roof allowed: " + habitat.biome());
        level.setBlockAndUpdate(pos.above(3), Blocks.AIR.defaultBlockState());
        check(allowed(type, level, pos), "clear surface rejected: " + habitat.biome());

        // Invoke vanilla's real selection/placement/finalization path. Omit the global
        // creature-budget gate and filter out other species to isolate habitat failures.
        var spawned = new ArrayList<PeepoEntity>();
        try {
            level.getRandom().setSeed(0x504545504FL);
            for (int attempt = 0; attempt < 600 && spawned.size() < PeepoSpawns.LOCAL_LIMIT; attempt++) {
                spawnGroup(level, pos, type, spawned);
            }
            check(spawned.size() == 4, "native natural spawning did not reach four: " + habitat.biome() + " count=" + spawned.size());
            check(spawned.stream().allMatch(PeepoEntity::isNaturallySpawned), "natural origin missing: " + habitat.biome());
            check(!allowed(type, level, pos), "fifth wild companion allowed: " + habitat.biome());
            check(PeepoSpawns.belowLocalLimit(other, level, pos), "species caps not independent: " + habitat.biome());
            for (int attempt = 0; attempt < 80; attempt++) spawnGroup(level, pos, type, spawned);
            check(spawned.size() == 4, "native group spawning exceeded four: " + habitat.biome());
            var adopted = spawned.getFirst();
            adopted.orders.tame(level.players().getFirst());
            check(adopted.isNaturallySpawned() && adopted.orders.tamed(), "taming must retain natural origin");
            check(allowed(type, level, pos) && PeepoSpawns.wildCount(type, level, pos) == 3, "taming did not free a wild place");
            for (int attempt = 0; attempt < 600 && spawned.size() < 5; attempt++) spawnGroup(level, pos, type, spawned);
            check(spawned.size() == 5 && !allowed(type, level, pos), "native replacement did not respect the wild-only cap");
            spawned.removeLast().discard();
            check(allowed(type, level, pos), "fourth companion blocked with three wild present: " + habitat.biome());
            LoggerFactory.getLogger("peepo-spawn-test").info("PASS {}: native spawning reached four wild and replaced adopted companions", habitat.biome());
        } finally {
            spawned.forEach(PeepoEntity::discard);
        }
        // Command-spawned companions must not use the natural population budget.
        var summoned = new ArrayList<PeepoEntity>();
        try {
            for (int i = 0; i < 7; i++) {
                var npc = type.create(level, EntitySpawnReason.COMMAND);
                npc.snapTo(pos.getX()+i+0.5, pos.getY(), pos.getZ()+0.5, 0, 0);
                npc.setNoAi(true);
                level.addFreshEntity(npc);
                summoned.add(npc);
            }
            check(allowed(type, level, pos), "summoned companions consumed natural cap: " + habitat.biome());
        } finally {
            summoned.forEach(PeepoEntity::discard);
        }
    }

    private void spawnGroup(ServerLevel level, BlockPos pos, EntityType<PeepoEntity> type, List<PeepoEntity> spawned) {
        NaturalSpawner.spawnCategoryForPosition(MobCategory.CREATURE, level, level.getChunk(pos), pos,
            (candidate, world, candidatePos, chunk) -> candidate == type,
            (mob, chunk) -> {
                check(mob instanceof PeepoEntity && mob.getType() == type, "unexpected naturally spawned entity");
                var npc = (PeepoEntity) mob;
                npc.setNoAi(true);
                spawned.add(npc);
            });
    }

    private void verifySchedule() {
        var schedule = new CompanionSpawning.Schedule(100);
        check(!schedule.pulse(1299) && schedule.pulse(1300), "startup minute");
        check(!schedule.pulse(1300) && !schedule.pulse(1319) && schedule.pulse(1320), "server-wide one-second budget");
        var edge = new BlockPos(127, 64, 127);
        check(schedule.claim(1320, edge), "first occupied region");
        for (int player = 0; player < 200; player++)
            check(!schedule.claim(1320+player, edge.offset(player%3, player, player%3)), "neighboring players multiplied attempts");
        check(schedule.claim(1320, edge.offset(512,0,0)), "distant region blocked");
        check(!schedule.claim(2519, edge) && schedule.claim(2520, edge), "shared regional cooldown expiry");
        check(schedule.claim(2520, new BlockPos(-512,64,-512)), "negative coordinate region");
        check(!schedule.claim(2521, new BlockPos(-513,64,-513)), "negative border bypass");
        check(schedule.pulse(99999) && !schedule.pulse(99999), "lag cannot create catch-up batches");
    }

    private List<PeepoEntity> nearby(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(PeepoEntity.class, new AABB(pos).inflate(32));
    }

    private void verifySupplemental(ServerLevel level, BlockPos pos, Habitat habitat) {
        var type = habitat.jughead() ? PeepoMod.JUGHEAD : PeepoMod.PEEPO;
        var animals = new ArrayList<Mob>();
        var random = net.minecraft.util.RandomSource.create(1234);
        try {
            check(CompanionSpawning.spawnGroup(level,type,pos,3,random) == 0, "disabled mob spawning bypassed");
            level.getGameRules().set(GameRules.SPAWN_MOBS, true, level.getServer());
            for (int i = 0; i < 12; i++) {
                var cow = EntityTypes.COW.create(level, EntitySpawnReason.COMMAND);
                cow.snapTo(pos.getX()-12+i, pos.getY(), pos.getZ()+12, 0, 0);
                cow.setNoAi(true); level.addFreshEntity(cow); animals.add(cow);
            }
            var budget = NaturalSpawner.createState(289, level, (key, consumer) -> {
                var chunk = level.getChunkSource().getChunkNow(ChunkPos.getX(key), ChunkPos.getZ(key));
                if (chunk != null) consumer.accept(chunk);
            }, new LocalMobCapCalculator(level.getChunkSource().chunkMap));
            check(!NaturalSpawner.getFilteredSpawningCategories(budget,true,true).contains(MobCategory.CREATURE), "fixture must saturate vanilla creature budget");
            check(CompanionSpawning.validPosition(level,type,pos), "supplemental placement rejected: " + habitat.biome()
                + " ticking=" + level.isPositionEntityTicking(pos) + " chunk=" + level.canSpawnEntitiesInChunk(ChunkPos.containing(pos))
                + " native=" + allowed(type,level,pos) + " population=" + PeepoSpawns.wildCount(type,level,pos)
                + " player=" + level.players().getFirst().position());
            check(CompanionSpawning.surface(level,pos.getX(),pos.getZ()).equals(pos), "loaded surface height");
            check(CompanionSpawning.spawnGroup(level,type,pos,99,random) == 3, "extra group must contain at most three despite requested size");
            check(CompanionSpawning.spawnGroup(level,type,pos,3,random) == 1, "extra group must stop at four wild");
            check(nearby(level,pos).size() == 4 && nearby(level,pos).stream().allMatch(PeepoEntity::isNaturallySpawned), "extra companions have natural provenance");
            check(CompanionSpawning.spawnGroup(level,type,pos,3,random) == 0, "supplemental local limit bypassed");
            nearby(level,pos).getFirst().orders.tame(level.players().getFirst());
            check(CompanionSpawning.spawnGroup(level,type,pos,3,random) == 1, "adoption must allow one supplemental replacement");
            check(PeepoSpawns.wildCount(type,level,pos) == 4 && nearby(level,pos).size() == 5, "owned companion consumed wild budget");
            nearby(level,pos).forEach(PeepoEntity::discard);

            denyNaturalCompanions = true;
            check(CompanionSpawning.spawnGroup(level,type,pos,3,random) == 0 && nearby(level,pos).isEmpty(), "Fabric spawn veto ignored");
            denyNaturalCompanions = false;
            var player = level.players().getFirst(); var original = player.position();
            try {
                player.snapTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
                check(!CompanionSpawning.validPosition(level,type,pos), "spawn too close to player");
                player.snapTo(pos.getX()-120,pos.getY(),pos.getZ(),0,0);
                check(!CompanionSpawning.validPosition(level,type,pos), "spawn too far from player");
            } finally { player.snapTo(original.x,original.y,original.z,0,0); }
            level.setBlockAndUpdate(pos,Blocks.WATER.defaultBlockState());
            check(!CompanionSpawning.validPosition(level,type,pos), "extra spawn in water");
            level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos.above(),Blocks.STONE.defaultBlockState());
            check(CompanionSpawning.spawnGroup(level,type,pos,0,random) == 0 && !CompanionSpawning.validPosition(level,type,pos), "extra spawn under solid obstruction");
            level.setBlockAndUpdate(pos.above(),Blocks.AIR.defaultBlockState());
            var remote = new BlockPos(20_000_000,pos.getY(),20_000_000);
            int loaded = level.getChunkSource().getLoadedChunksCount();
            check(CompanionSpawning.surface(level,remote.getX(),remote.getZ()) == null, "unloaded surface accepted");
            check(!CompanionSpawning.validPosition(level,type,remote), "unloaded spawn position accepted");
            check(loaded == level.getChunkSource().getLoadedChunksCount(), "spawn probe loaded a chunk");
            check(!CompanionSpawning.enabled(level.getServer().getLevel(net.minecraft.world.level.Level.NETHER)), "supplemental Nether spawning");
            var border = level.getWorldBorder();
            double borderSize = border.getSize(), borderX = border.getCenterX(), borderZ = border.getCenterZ();
            try {
                border.setCenter(pos.getX()-64,pos.getZ()); border.setSize(32);
                check(!CompanionSpawning.validPosition(level,type,pos), "world border bypassed");
            } finally { border.setCenter(borderX,borderZ); border.setSize(borderSize); }
            int sampled = 0;
            for (int attempt = 0; attempt < 200 && sampled == 0; attempt++)
                sampled = CompanionSpawning.attemptNear(level,player,random);
            check(sampled > 0 && sampled <= 3, "bounded surface sampling failed to find habitat: " + habitat.biome());
            check(nearby(level,pos).stream().allMatch(p -> p.getType() == type && p.isNaturallySpawned()), "sampled wrong species/origin");
            LoggerFactory.getLogger("peepo-spawn-test").info("PASS {}: supplemental natural groups work with a full passive cap and stop at four wild",habitat.biome());
        } finally {
            denyNaturalCompanions = false;
            animals.forEach(Mob::discard);
            nearby(level,pos).forEach(PeepoEntity::discard);
            level.getGameRules().set(GameRules.SPAWN_MOBS,false,level.getServer());
        }
    }
}
