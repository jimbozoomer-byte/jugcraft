package io.github.jimbozoomer.jugcraft.world;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.town.TownState;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateBiomeSource;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateCategories;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateCategory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Offline existing-save migration, followed by real generation and two server reopenings. */
public class ClimateMigrationClientGameTests implements FabricClientGameTest {
    private static final BlockPos CHEST = new BlockPos(20000, 300, 3400);
    private static final BlockPos VALLEY = new BlockPos(9008, 0, 400);
    private static final int[] OLD_Z = {-2600, 3400, 6400};

    @Override public void runTest(ClientGameTestContext context) {
        try { run(context); } catch (Exception e) { throw new AssertionError("Existing-world climate migration failed", e); }
    }

    private void run(ClientGameTestContext context) throws Exception {
        String fixture = System.getenv("JUGCRAFT_CLIMATE_MIGRATION_FIXTURE");
        CompoundTag prepared = fixture == null ? null : NbtIo.readCompressed(Path.of(fixture), NbtAccounter.unlimitedHeap());
        long seed = prepared == null ? 33445566L : prepared.getCompoundOrEmpty("data").getLongOr("seed", 0);
        int previousDistance = context.computeOnClient(client -> client.options.renderDistance().get());
        context.runOnClient(client -> client.options.renderDistance().set(4));
        TestWorldSave save;
        Map<String, String> oldBiomes = new LinkedHashMap<>();
        AtomicReference<BlockPos> town = new AtomicReference<>();
        try (TestSingleplayerContext world = context.worldBuilder().adjustSettings(creator -> {
            creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            creator.setSeed(Long.toString(seed));
            creator.setWorldType(new WorldCreationUiState.WorldTypeEntry(creator.getSettings().worldgenLoadContext()
                    .lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.NORMAL)));
        }).create()) {
            world.getServer().runCommand("setworldspawn 1400 92 395");
            world.getServer().runOnServer(server -> {
                ServerLevel level = server.overworld();
                town.set(TownState.get(level).origin());
                oldBiomes.putAll(stored(level, 20000, OLD_Z));
                level.setBlockAndUpdate(CHEST, Blocks.CHEST.defaultBlockState());
                ((Container) level.getBlockEntity(CHEST)).setItem(0, new ItemStack(Items.DIAMOND, 42));
                level.setBlockAndUpdate(CHEST.east(), Blocks.GOLD_BLOCK.defaultBlockState());
                // A pre-existing chosen/built valley must remain loaded with the inner procedural source.
                AdamOndiAhmanState state = AdamOndiAhmanState.get(level);
                state.choose(VALLEY);
                state.markBuilt(AltarKnolls.key(AdamOndiAhman.Site.at(VALLEY.getX(), VALLEY.getZ(), AdamOndiAhman.fingerprint(seed))));
            });
            save = world.getWorldSave();
        }

        Path directory = save.getSaveDirectory();
        Path settings = directory.resolve("data/minecraft/world_gen_settings.dat");
        Map<String, String> filesBefore = hashes(directory, settings);
        CompoundTag before = NbtIo.readCompressed(settings, NbtAccounter.unlimitedHeap());
        CompoundTag after = before.copy();
        CompoundTag generator = generator(after);
        CompoundTag originalSource = generator.getCompoundOrEmpty("biome_source");
        CompoundTag source;
        if (prepared != null) {
            source = generator(prepared).getCompoundOrEmpty("biome_source").copy();
            check(source.getCompoundOrEmpty("base").equals(originalSource), "Live metadata base differs from the test world's base");
        } else {
            source = new CompoundTag(); source.putString("type", "jugcraft:climate"); source.put("base", originalSource.copy());
            CompoundTag profile = new CompoundTag(); profile.putInt("schema", 1);
            CompoundTag anchor = new CompoundTag(); anchor.putString("mode", "fixed"); anchor.putInt("x", 1296); anchor.putInt("z", 400);
            profile.put("anchor", anchor); profile.putInt("north_frigid", 2500); profile.putInt("south_hot_start", 2000);
            profile.putInt("south_hot_end", 4000); profile.putInt("south_frigid", 5500); profile.putInt("transition", 256);
            source.put("climate", profile);
        }
        generator.put("biome_source", source);
        NbtIo.writeCompressed(after, settings);
        check(filesBefore.equals(hashes(directory, settings)), "Migration touched another saved file");
        CompoundTag unwrap = after.copy(); generator(unwrap).put("biome_source", source.getCompoundOrEmpty("base"));
        check(before.equals(unwrap), "Migration changed seed, terrain, dimensions or other metadata");

        Map<String, String> freshBiomes = new LinkedHashMap<>();
        for (int restart = 0; restart < 2; restart++) {
            final int pass = restart;
            try (TestSingleplayerContext reopened = save.open()) {
                reopened.getServer().runOnServer(server -> {
                    ServerLevel level = server.overworld();
                    check(level.getChunkSource().getGenerator().getBiomeSource() instanceof ClimateBiomeSource, "Saved migration was not loaded");
                    ClimateBiomeSource climate = (ClimateBiomeSource) level.getChunkSource().getGenerator().getBiomeSource();
                    check(climate.climate().anchorX() == 1296 && climate.climate().anchorZ() == 400, "Anchor moved");
                    check(level.getSeed() == seed, "Seed changed");
                    check(level.getRespawnData().pos().equals(new BlockPos(pass == 0 ? 1400 : 1500, 92, 395)), "Spawn reset");
                    check(Objects.equals(town.get(), TownState.get(level).origin()), "City moved");
                    check(oldBiomes.equals(stored(level, 20000, OLD_Z)), "Existing chunk biomes changed");
                    check(level.getBlockState(CHEST.east()).is(Blocks.GOLD_BLOCK), "Player construction lost");
                    ItemStack stack = ((Container) level.getBlockEntity(CHEST)).getItem(0);
                    check(stack.is(Items.DIAMOND) && stack.getCount() == 42, "Container contents lost");
                    check(AdamOndiAhman.startSite() != null && AdamOndiAhman.startSite().pos().equals(VALLEY), "Saved valley disabled by wrapper");
                    var baseBiome = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS);
                    var target = net.minecraft.world.level.biome.Climate.target(0, 0, 0, 0, 0, 0);
                    check(AdamOndiAhman.override(climate.base(), VALLEY.getX() >> 2, VALLEY.getZ() >> 2, target, baseBiome).is(AdamOndiAhman.BIOME), "Inner valley source identity lost");
                    if (pass == 0) freshBiomes.putAll(verifyFresh(level, 20128));
                    else {
                        check(freshBiomes.equals(verifyFresh(level, 20128)), "New climate chunks changed on second reopen");
                        verifyFresh(level, 20256);
                    }
                    Jugcraft.LOGGER.info("[climate-migration] Reopen {}: old biomes/build/container/city/spawn/valley preserved; future chunks obey belts", pass + 1);
                });
                if (pass == 0) reopened.getServer().runCommand("setworldspawn 1500 92 395");
            }
        }
        context.runOnClient(client -> client.options.renderDistance().set(previousDistance));
        Jugcraft.LOGGER.info("[climate-migration] PASS; actual prepared server metadata fixture = {}; {} old biome samples; {} other saved files unchanged by migration", prepared != null, oldBiomes.size(), filesBefore.size());
    }

    private static CompoundTag generator(CompoundTag root) {
        return root.getCompoundOrEmpty("data").getCompoundOrEmpty("dimensions").getCompoundOrEmpty("minecraft:overworld").getCompoundOrEmpty("generator");
    }

    private static Map<String, String> hashes(Path root, Path except) throws Exception {
        Map<String, String> result = new LinkedHashMap<>();
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(Files::isRegularFile).filter(p -> !p.equals(except)).toList())
                result.put(root.relativize(path).toString(), HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))));
        }
        return result;
    }

    private static Map<String, String> stored(ServerLevel level, int x, int[] zs) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int requestedZ : zs) {
            int z = requestedZ & ~15;
            var chunk = level.getChunk(x >> 4, z >> 4);
            for (int dx = 0; dx < 16; dx += 4) for (int dz = 0; dz < 16; dz += 4)
                for (int y = level.getMinY(); y <= level.getMaxY(); y += 4)
                    result.put((x+dx)+","+y+","+(z+dz), chunk.getNoiseBiome((x+dx)>>2, y>>2, (z+dz)>>2).getRegisteredName());
        }
        return result;
    }

    private static Map<String, String> verifyFresh(ServerLevel level, int x) {
        int[] zs = {-2700, 400, 3400, 5000, 6500};
        String[] zones = {"frigid", "central", "hot", "temperate", "frigid"};
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < zs.length; i++) {
            int z = zs[i] & ~15; var chunk = level.getChunk(x >> 4, z >> 4);
            for (int dx = 0; dx < 16; dx += 4) for (int dz = 0; dz < 16; dz += 4) {
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x+dx, z+dz);
                var biome = chunk.getNoiseBiome((x+dx)>>2, y>>2, (z+dz)>>2);
                ClimateCategory category = ClimateCategories.classify(biome);
                if (!zones[i].equals("central") && category != ClimateCategory.PRESERVE)
                    check(biome.is(TagKey.create(Registries.BIOME, Jugcraft.id("climate/"+zones[i]+"/"+category.id()))), "Wrong future biome " + biome.getRegisteredName()+" at "+(x+dx)+","+(z+dz));
                result.put((x+dx)+","+(z+dz), biome.getRegisteredName());
            }
        }
        return result;
    }

    private static void check(boolean valid, String message) { if (!valid) throw new AssertionError(message); }
}
