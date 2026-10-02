package io.github.jimbozoomer.jugcraft.test;

import com.mojang.datafixers.util.Pair;
import io.github.jimbozoomer.jugcraft.biome.JugcraftRegions;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The biomes branch in a real, normally generated world (seed "jugcraft"): how far from the start each seasonal-forest
 * biome is (logged), and screenshots of those found, seen from above the treetops.
 */
public class BiomeClientGameTests implements FabricClientGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger("jugcraft-test");
	private static final String SEED = "jugcraft";
	/** How far to look for each biome, in blocks. */
	private static final int SEARCH = 6400;
	/** At least this many of the nine biomes must be within SEARCH blocks of the start. */
	private static final int FOUND_AT_LEAST = 6;
	/** Biomes to photograph, if found. */
	private static final String[] SHOTS = {"maple_woods", "seasonal_forest", "aspen_glade", "coniferous_forest", "dead_forest", "tundra"};

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().adjustSettings(creator -> {
			creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
			creator.setWorldType(new WorldCreationUiState.WorldTypeEntry(creator.getSettings().worldgenLoadContext()
					.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.NORMAL)));
			creator.setSeed(SEED);
		}).create()) {
			singleplayer.getConnection().waitForChunksRender();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("gamerule sendCommandFeedback false");
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			BlockPos start = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			List<String> found = new ArrayList<>();
			List<BlockPos> places = new ArrayList<>();
			for (ResourceKey<Biome> biome : JugcraftRegions.biomes()) {
				BlockPos place = server.computeOnServer(minecraft -> {
					Pair<BlockPos, Holder<Biome>> nearest = minecraft.overworld().findClosestBiome3d(holder -> holder.is(biome), start,
							SEARCH, 64, 64);
					return nearest == null ? null : nearest.getFirst();
				});
				String name = biome.identifier().getPath();
				LOGGER.info("Biomes, seed {}: {} {}", SEED, name, place == null ? "not within " + SEARCH + " blocks"
						: String.format(Locale.ROOT, "at %d %d (%d blocks from the start)", place.getX(), place.getZ(),
								(int) Math.sqrt(place.distSqr(start.atY(place.getY())))));
				if (place != null) {
					found.add(name);
					places.add(place);
				}
			}
			// Vanilla regions keep the biomes the Jugcraft layout replaces.
			List<String> vanillaMissing = new ArrayList<>();
			for (ResourceKey<Biome> biome : List.of(Biomes.TAIGA, Biomes.FOREST, Biomes.BIRCH_FOREST)) {
				BlockPos place = server.computeOnServer(minecraft -> {
					Pair<BlockPos, Holder<Biome>> nearest = minecraft.overworld().findClosestBiome3d(holder -> holder.is(biome), start,
							SEARCH, 64, 64);
					return nearest == null ? null : nearest.getFirst();
				});
				LOGGER.info("Biomes, seed {}: vanilla {} {}", SEED, biome.identifier().getPath(), place == null ? "not within " + SEARCH
						+ " blocks" : "at " + place.getX() + " " + place.getZ());
				if (place == null) {
					vanillaMissing.add(biome.identifier().getPath());
				}
			}
			for (String shot : SHOTS) {
				int index = found.indexOf(shot);
				if (index < 0) {
					continue;
				}
				BlockPos place = places.get(index);
				int ground = server.computeOnServer(minecraft -> {
					ServerLevel level = minecraft.overworld();
					level.getChunk(place.getX() >> 4, place.getZ() >> 4);
					return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, place.getX(), place.getZ());
				});
				int y = ground + 14;
				server.runCommand("setblock %d %d %d minecraft:barrier".formatted(place.getX(), y - 1, place.getZ()));
				server.runCommand(String.format(Locale.ROOT, "tp @p %d.5 %d %d.5 135 30", place.getX(), y, place.getZ()));
				context.waitTicks(60);
				singleplayer.getConnection().waitForChunksRender();
				context.takeScreenshot("jugcraft_biome_" + shot);
			}
			if (!vanillaMissing.isEmpty()) {
				throw new AssertionError("Vanilla " + vanillaMissing + " not within " + SEARCH + " blocks of the start (seed " + SEED + ")");
			}
			if (found.size() < FOUND_AT_LEAST) {
				throw new AssertionError("Only " + found.size() + " of " + JugcraftRegions.biomes().size() + " batch 1 biomes within "
						+ SEARCH + " blocks of the start (seed " + SEED + "): " + found);
			}
		}
	}
}
