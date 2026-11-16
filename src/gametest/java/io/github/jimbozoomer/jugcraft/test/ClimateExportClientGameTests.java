package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateCategories;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateCategory;
import io.github.jimbozoomer.jugcraft.world.design.DesignBiomeSource;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

/** Loads actual model.js export files during registry loading, not a programmatically constructed generator. */
public class ClimateExportClientGameTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        int previousDistance = context.computeOnClient(client -> client.options.renderDistance().get());
        context.runOnClient(client -> client.options.renderDistance().set(4));
        final TestWorldSave save;
        Map<String, String> before = new LinkedHashMap<>();
        try (TestSingleplayerContext world = context.worldBuilder().adjustSettings(creator -> {
            creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            creator.setSeed("jugcraft-export-fixture");
            creator.setWorldType(new WorldCreationUiState.WorldTypeEntry(creator.getSettings().worldgenLoadContext()
                    .lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(ResourceKey.create(Registries.WORLD_PRESET, Jugcraft.id("designed")))));
        }).create()) {
            world.getServer().runOnServer(server -> before.putAll(verify(server.overworld())));
            world.getServer().runCommand("gamemode spectator @a");
            world.getServer().runCommand("time set noon");
            world.getServer().runCommand("weather clear");
            var previousClouds = context.computeOnClient(client -> client.options.cloudStatus().get());
            context.runOnClient(client -> client.options.cloudStatus().set(net.minecraft.client.CloudStatus.OFF));
            world.getServer().runCommand("tp @a 320 90 -3000 0 30");
            context.waitTicks(120);
            world.getConnection().waitForChunksRender();
            context.getInput().lookAt(new BlockPos(320, 65, -2960));
            context.runOnClient(client -> { if (!client.gui.hud.isHidden()) client.gui.hud.toggle(); });
            context.takeScreenshot("jugcraft_climate_export_north");
            context.runOnClient(client -> { client.options.cloudStatus().set(previousClouds); });
            context.runOnClient(client -> { if (client.gui.hud.isHidden()) client.gui.hud.toggle(); });
            // Moving world spawn later must not change the serialized climate origin.
            world.getServer().runCommand("setworldspawn 400 90 -100");
            save = world.getWorldSave();
        }
        try (TestSingleplayerContext reopened = save.open()) {
            reopened.getServer().runOnServer(server -> {
                check(before.equals(verify(server.overworld())), "Exported climate changed after reopening");
                check(server.overworld().getRespawnData().pos().getX() == 400, "Later world spawn was overwritten");
                Jugcraft.LOGGER.info("[climate-export] Registered exported preset generated all five belts and survived save/reopen and spawn relocation");
            });
        }
        context.runOnClient(client -> client.options.renderDistance().set(previousDistance));
    }

    private static Map<String, String> verify(ServerLevel level) {
        check(level.getChunkSource().getGenerator().getBiomeSource() instanceof DesignBiomeSource, "Exported preset was not loaded");
        var design = (DesignBiomeSource) level.getChunkSource().getGenerator().getBiomeSource();
        var profile = design.climate().orElseThrow();
        check(profile.anchorX() == 320 && profile.anchorZ() == -192, "Exported origin changed");
        Map<String, String> result = new LinkedHashMap<>();
        int[] offsets = {-3000, 0, 3000, 4700, 6000};
        String[] zones = {"frigid", "central", "hot", "temperate", "frigid"};
        for (int i = 0; i < offsets.length; i++) for (int x : new int[]{320, -2400}) {
            int z = -192 + offsets[i];
            var chunk = level.getChunk(x >> 4, z >> 4);
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            var biome = chunk.getNoiseBiome(x >> 2, y >> 2, z >> 2);
            if (!zones[i].equals("central") && ClimateCategories.classify(biome) != ClimateCategory.PRESERVE)
                check(ClimateRuntimeGameTests.inZone(biome, zones[i]), "Wrong exported belt at " + x + "," + z + ": " + biome.getRegisteredName());
            result.put(x + "," + z, biome.getRegisteredName());
        }
        return result;
    }

    private static void check(boolean valid, String message) { if (!valid) throw new AssertionError(message); }
}
