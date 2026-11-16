package io.github.jimbozoomer.jugcraft.test;

import static io.github.jimbozoomer.jugcraft.test.ClimateRuntimeGameTests.*;

import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.town.TownPlanner;
import io.github.jimbozoomer.jugcraft.town.TownState;
import io.github.jimbozoomer.jugcraft.world.AdamOndiAhman;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateLayer;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateProfile;
import io.github.jimbozoomer.jugcraft.world.design.DesignBiomeSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/**
 * Climate bands in a real designed world: a new world whose design source carries the small test profile of
 * {@link ClimateRuntimeGameTests} (anchor at the design spawn, -128 256). Chunks are generated in every band, inside and
 * outside the design, and the biomes they store are read back: painted cells unchanged; central unchanged from the
 * seeded base; elsewhere the band's pool for the base biome's own category (oceans stay oceans); caves unchanged. The
 * spawn, the city and Adam-ondi-Ahman behave as in a designed world without climate. Saved and reopened, the profile,
 * the stored biomes and newly generated chunks still agree.
 */
public class ClimateDesignClientGameTests implements FabricClientGameTest {
    /** Columns sampled: painted (west of x 0, inside the design), unpainted, and outside the design. */
    private static final int[] XS = {-320, -200, 64, 200, 1200, -1200};
    /** Rows sampled: inside each pure band of the test profile, near and far. */
    private static final int[] ZS = {-1500, -400, FRIGID_NORTH_Z, CENTRAL_Z, HOT_Z, TEMPERATE_Z, FRIGID_SOUTH_Z, 1500};
    /** Fresh columns generated only after reopening. */
    private static final int[] AFTER_REOPEN_XS = {2000, -2000};
    /** Block heights sampled besides the surface (Y8 is above the paint's Y0 floor; -40 is cave depth). */
    private static final int[] DEPTHS = {8, -40};

    @Override public void runTest(ClientGameTestContext context) {
        final TestWorldSave save;
        Map<String, String> stored = new ConcurrentHashMap<>();
        AtomicReference<String> profileJson = new AtomicReference<>();
        try (TestSingleplayerContext world = context.worldBuilder().adjustSettings(creator -> {
            creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            creator.setWorldType(new WorldCreationUiState.WorldTypeEntry(creator.getSettings().worldgenLoadContext()
                    .lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.NORMAL)));
            creator.setSeed("jugcraft-climate-bands");
            creator.updateDimensions((registries, dimensions) -> {
                var grid = grid();
                var ops = RegistryOps.create(JsonOps.INSTANCE, registries);
                var original = registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD).value();
                var settings = NoiseGeneratorSettings.DIRECT_CODEC.encodeStart(ops, original).getOrThrow().getAsJsonObject();
                var router = settings.getAsJsonObject("noise_router");
                var land = new com.google.gson.JsonObject(); land.addProperty("type", "jugcraft:design_density");
                land.add("grid", io.github.jimbozoomer.jugcraft.world.design.DesignGrid.CODEC.encodeStart(ops, grid).getOrThrow());
                land.add("input", router.get("final_density"));
                var surface = new com.google.gson.JsonObject(); surface.addProperty("type", "jugcraft:design_density"); surface.addProperty("surface", true);
                surface.add("grid", land.get("grid")); surface.add("input", router.get("chunk_surface_level"));
                router.add("final_density", land); router.add("chunk_surface_level", surface);
                if (settings.has("aquifers")) settings.getAsJsonObject("aquifers").add("surface_level", surface);
                var noise = NoiseGeneratorSettings.DIRECT_CODEC.parse(ops, settings).getOrThrow();
                var biomes = registries.lookupOrThrow(Registries.BIOME);
                ClimateProfile climate = ClimateProfile.CODEC.parse(ops, testProfileJson()).getOrThrow();
                var source = new DesignBiomeSource(dimensions.overworld().getBiomeSource(), grid, List.of(biomes.getOrThrow(Biomes.DESERT)),
                        westPainted(), new BlockPos(ANCHOR_X, 0, ANCHOR_Z), Optional.of(new BlockPos(256, 0, 160)), Optional.of(climate));
                var generator = new NoiseBasedChunkGenerator(source, Holder.direct(noise));
                // Round-trip the exact generator codec that level.dat persists, before any chunk is created.
                var decoded = ChunkGenerator.CODEC.parse(ops, ChunkGenerator.CODEC.encodeStart(ops, generator).getOrThrow()).getOrThrow();
                return dimensions.replaceOverworldGenerator(registries, decoded);
            });
        }).create()) {
            world.getServer().runOnServer(server -> {
                var level = server.overworld();
                var design = verifyWorld(level);
                profileJson.set(encodeProfile(level, design));
                for (int x : XS) for (int z : ZS) sample(level, design, x, z, stored);
                strictSweep(level, design);
                Jugcraft.LOGGER.info("[climate-check] {} generated biome samples obey the bands; spawn {}, city {}", stored.size(),
                        level.getRespawnData().pos(), TownState.get(level).origin());
            });
            save = world.getWorldSave();
        }
        try (TestSingleplayerContext reopened = save.open()) {
            reopened.getServer().runOnServer(server -> {
                var level = server.overworld();
                var design = verifyWorld(level);
                check(encodeProfile(level, design).equals(profileJson.get()), "The climate profile changed on reopening: " + encodeProfile(level, design));
                Map<String, String> again = new ConcurrentHashMap<>();
                for (int x : XS) for (int z : ZS) sample(level, design, x, z, again);
                check(again.equals(stored), "Stored biomes changed on reopening");
                Map<String, String> fresh = new ConcurrentHashMap<>();
                for (int x : AFTER_REOPEN_XS) for (int z : ZS) sample(level, design, x, z, fresh);
                strictSweep(level, design);
                Jugcraft.LOGGER.info("[climate-check] Profile, {} stored samples and {} newly generated samples agree after reopening",
                        again.size(), fresh.size());
            });
        }
    }

    /** The designed world as created: climate present, spawn and city from the design, no Adam-ondi-Ahman (as without climate). */
    private static DesignBiomeSource verifyWorld(ServerLevel level) {
        var source = level.getChunkSource().getGenerator().getBiomeSource();
        check(source instanceof DesignBiomeSource, "The designed biome source is missing");
        var design = (DesignBiomeSource) source;
        check(design.climate().isPresent() && ClimateLayer.applies(design), "The design lost its climate");
        check(design.climate().get().anchorX() == ANCHOR_X && design.climate().get().anchorZ() == ANCHOR_Z, "The climate anchor moved");
        var spawn = level.getRespawnData().pos();
        check(spawn.getX() == ANCHOR_X && spawn.getZ() == ANCHOR_Z, "Spawn did not use the design: " + spawn);
        check(TownPlanner.originAround(256, 79, 160).equals(TownState.get(level).origin()), "City did not use the design: " + TownState.get(level).origin());
        // A designed source is not a multi-noise source, with or without climate: Adam-ondi-Ahman takes up no world.
        check(AdamOndiAhman.startSite() == null, "Adam-ondi-Ahman must not run in a designed world");
        return design;
    }

    private static String encodeProfile(ServerLevel level, DesignBiomeSource design) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, level.registryAccess());
        return ClimateProfile.CODEC.encodeStart(ops, design.climate().orElseThrow()).getOrThrow().toString();
    }

    /** Whether the design's overlay answers this quart from its paint (as DesignBiomeSource reads it: the quart's corner). */
    private static boolean painted(DesignBiomeSource design, int quartX, int quartY, int quartZ) {
        int x = quartX * 4, z = quartZ * 4;
        return quartY >= 0 && design.grid().weight(x, z) > 0 && westPainted().get(design.grid().cell(x, z)) > 0;
    }

    /**
     * Generates the chunk at (x, z) and checks the biomes it stored at the surface and below against the bands, the base
     * source's own answer and the paint; records them under "x z quartY".
     */
    private static void sample(ServerLevel level, DesignBiomeSource design, int x, int z, Map<String, String> into) {
        var chunk = level.getChunk(x >> 4, z >> 4);
        BiomeResolver base = design.base().createUncachedResolver(level.getChunkSource().randomState());
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        int[] heights = {top, DEPTHS[0], DEPTHS[1]};
        String zone = zone(z);
        check(zone != null, "Test row " + z + " lies in a transition");
        for (int y : heights) {
            int qx = x >> 2, qy = y >> 2, qz = z >> 2;
            Holder<Biome> got = chunk.getNoiseBiome(qx, qy, qz);
            Holder<Biome> was = base.getNoiseBiome(qx, qy, qz);
            String where = x + " " + y + " " + z + " (" + zone + ")";
            if (painted(design, qx, qy, qz)) {
                check(got.is(Biomes.DESERT), "A painted cell changed at " + where + ": " + id(got));
            } else if (zone.equals("central")) {
                check(got.equals(was), "Central changed the base biome at " + where + ": " + id(was) + " -> " + id(got));
            } else {
                String category = categoryOf(was);
                check(category != null, "Uncategorized base biome " + id(was) + " at " + where);
                if (category.equals("preserve")) {
                    check(got.equals(was), "A cave biome changed at " + where + ": " + id(was) + " -> " + id(got));
                } else {
                    check(got.is(pool(zone, category)), id(was) + " (" + category + ") became " + id(got) + " at " + where);
                }
            }
            into.put(x + " " + z + " " + qy, id(got));
        }
    }

    /**
     * Strict frigid, read through the world's own biome source as generation does: at and beyond each frigid limit, for
     * 16,000 blocks east to west, every answer is a frigid pool member or an unchanged cave biome.
     */
    private static void strictSweep(ServerLevel level, DesignBiomeSource design) {
        BiomeResolver banded = design.createUncachedResolver(level.getChunkSource().randomState());
        BiomeResolver base = design.base().createUncachedResolver(level.getChunkSource().randomState());
        for (int z : new int[]{0, -1000, -6000, 736, 2000, 9000}) {
            for (int x = -8000; x <= 8000; x += 256) {
                for (int qy : new int[]{4, 16, 20, 32}) {
                    Holder<Biome> got = banded.getNoiseBiome(x >> 2, qy, z >> 2);
                    if (painted(design, x >> 2, qy, z >> 2)) continue;
                    Holder<Biome> was = base.getNoiseBiome(x >> 2, qy, z >> 2);
                    boolean cave = "preserve".equals(categoryOf(was));
                    check(cave ? got.equals(was) : inZone(got, "frigid"), "Not strictly frigid at " + x + " " + (qy * 4) + " " + z + ": " + id(got));
                }
            }
        }
    }

    private static void check(boolean pass, String message) { if (!pass) throw new AssertionError(message); }
}
