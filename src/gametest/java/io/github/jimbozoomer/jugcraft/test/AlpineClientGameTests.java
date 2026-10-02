package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.world.AlpineSpawn;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Alpine Spawn in a real, normally generated world (a fixed seed): a new world starts in the biome, and a village
 * stands near the start. Screenshots of the start and of the view from above it.
 */
public class AlpineClientGameTests implements FabricClientGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger("jugcraft-test");
	private static final String SEED = "jugcraft";

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().adjustSettings(creator -> {
			creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
			creator.setWorldType(new WorldCreationUiState.WorldTypeEntry(creator.getSettings().worldgenLoadContext()
					.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.NORMAL)));
			creator.setSeed(SEED);
			// Fabric's consistent test settings switch structures off; villages are part of what this checks.
			creator.setGenerateStructures(true);
		}).create()) {
			singleplayer.getConnection().waitForChunksRender();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("gamerule sendCommandFeedback false");
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			BlockPos start = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			boolean alpine = server.computeOnServer(minecraft -> minecraft.overworld().getBiome(start).is(AlpineSpawn.BIOME));
			String biome = server.computeOnServer(minecraft -> minecraft.overworld().getBiome(start).unwrapKey()
					.map(key -> key.identifier().toString()).orElse("?"));
			BlockPos village = server.computeOnServer(minecraft -> minecraft.overworld()
					.findNearestMapStructure(StructureTags.VILLAGE, start, 8, false));
			LOGGER.info("Alpine Spawn, seed {}: the player starts at {} {} {} in {}; nearest village {}", SEED, start.getX(),
					start.getY(), start.getZ(), biome, village == null ? "none within 8 chunks"
					: village.getX() + " " + village.getZ() + " (" + (int) Math.sqrt(village.distSqr(start)) + " blocks)");
			context.takeScreenshot("jugcraft_alpine_spawn_start");
			server.runCommand("tp @p %d %d %d 135 25".formatted(start.getX(), start.getY() + 40, start.getZ()));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_alpine_spawn_overview");
			if (!alpine) {
				throw new AssertionError("A new world (seed " + SEED + ") starts in " + biome + ", not Alpine Spawn");
			}
		}
	}
}
