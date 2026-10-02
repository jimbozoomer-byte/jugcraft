package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.AtmMenu;
import io.github.jimbozoomer.jugcraft.town.ShopMenu;
import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.TownDecor;
import io.github.jimbozoomer.jugcraft.town.TownState;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The walled town in a real new world (seed {@link #SEED}): the server must have placed it near the start; the camera
 * flies over it while it builds, then looks round the square and a gate, in two themes, and opens a shop and an ATM.
 * The distances, the chunks built and the townsfolk out are logged.
 */
public class TownClientGameTests implements FabricClientGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger("jugcraft-town-client-tests");
	private static final String SEED = "jugcraft";

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().adjustSettings(creator -> {
			creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
			creator.setWorldType(new WorldCreationUiState.WorldTypeEntry(creator.getSettings().worldgenLoadContext()
					.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.NORMAL)));
			creator.setSeed(SEED);
		}).create()) {
			// The default render distance (12 chunks) loads, and so builds, the whole town (192 blocks) round the overview
			// spot; a larger one was too slow to render here with software rendering (run 37074095387).
			singleplayer.getConnection().waitForChunksRender();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			BlockPos origin = server.computeOnServer(minecraft -> TownState.get(minecraft).origin());
			if (origin == null) {
				throw new AssertionError("The new world has no town");
			}
			BlockPos start = server.computeOnServer(minecraft -> minecraft.overworld().getRespawnData().pos());
			BlockPos centre = Town.centre(origin);
			LOGGER.info("Town, seed {}: middle at {} {} {}, {} blocks from the start at {} {} {}", SEED, centre.getX(), centre.getY(),
					centre.getZ(), (int) Math.sqrt(centre.distSqr(start.atY(centre.getY()))), start.getX(), start.getY(), start.getZ());
			// Over the town from the south, while its chunks load and build.
			fly(context, server, origin, 96, 90, 176, 180, 36);
			int size = TownData.get().size;
			int chunks = (size / 16) * (size / 16);
			int built = 0;
			for (int wait = 0; wait < 90; wait++) {
				context.waitTicks(20);
				built = server.computeOnServer(minecraft -> builtChunks(minecraft.overworld(), origin));
				if (built >= chunks - 30 && wait > 20) {
					break;
				}
			}
			singleplayer.getConnection().waitForChunksRender();
			LOGGER.info("Town, seed {}: {} chunks built", SEED, built);
			context.takeScreenshot("jugcraft_town_overview");
			// The square, looking north to the church, in the theme of the day; then at Halloween and in December.
			fly(context, server, origin, 96, 3, 113, 180, -8);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_town_square");
			for (String theme : List.of("halloween", "december")) {
				server.runOnServer(minecraft -> {
					TownDecor.force(theme);
					redecorate(minecraft.overworld());
				});
				context.waitTicks(20);
				singleplayer.getConnection().waitForChunksRender();
				context.takeScreenshot("jugcraft_town_square_" + theme);
			}
			server.runOnServer(minecraft -> {
				TownDecor.force(null);
				redecorate(minecraft.overworld());
			});
			// The south gate from outside.
			fly(context, server, origin, 96, 6, 186, 180, 5);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_town_gate");
			// The townsfolk, a shop and an ATM.
			String people = server.computeOnServer(minecraft -> townsfolk(minecraft.overworld(), origin));
			LOGGER.info("Town, seed {}: townsfolk out: {}", SEED, people);
			fly(context, server, origin, 96, 2, 104, 270, 10);
			boolean shop = server.computeOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				ServerPlayer player = minecraft.getPlayerList().getPlayers().get(0);
				List<Townsfolk> keepers = level.getEntitiesOfClass(Townsfolk.class, townBox(origin, size),
						t -> t.shop().equals("seasonal"));
				if (keepers.isEmpty()) {
					return false;
				}
				player.teleportTo(keepers.get(0).getX() + 2, keepers.get(0).getY(), keepers.get(0).getZ());
				ShopMenu.open(player, keepers.get(0));
				return true;
			});
			context.waitTicks(10);
			context.takeScreenshot("jugcraft_town_shop");
			context.runOnClient(client -> client.player.closeContainer());
			boolean atm = server.computeOnServer(minecraft -> {
				ServerPlayer player = minecraft.getPlayerList().getPlayers().get(0);
				BlockPos at = origin.offset(TownData.get().atms.get(0));
				player.teleportTo(at.getX() + 0.5, at.getY(), at.getZ() - 1.5);
				AtmMenu.open(player, at);
				return minecraft.overworld().getBlockState(at).getBlock() instanceof io.github.jimbozoomer.jugcraft.town.AtmBlock;
			});
			context.waitTicks(10);
			context.takeScreenshot("jugcraft_town_atm");
			context.runOnClient(client -> client.player.closeContainer());
			LOGGER.info("Town, seed {}: shop screen opened {}, ATM in place {}", SEED, shop, atm);
			if (!shop || !atm) {
				throw new AssertionError("The town's seasonal stall keeper or its ATM is missing (shop " + shop + ", ATM " + atm + ")");
			}
		}
	}

	/** Puts the player at a town position (x, ground + up, z) looking along `yaw`, `pitch`, on a barrier if in the air. */
	private static void fly(ClientGameTestContext context, TestServerContext server, BlockPos origin, int x, int up, int z, int yaw, int pitch) {
		BlockPos at = origin.offset(x, up, z);
		if (up > 3) {
			server.runCommand("setblock %d %d %d minecraft:barrier".formatted(at.getX(), at.getY() - 1, at.getZ()));
		}
		server.runCommand(String.format(Locale.ROOT, "tp @p %d.5 %d %d.5 %d %d", at.getX(), at.getY(), at.getZ(), yaw, pitch));
		context.waitTicks(40);
	}

	private static int builtChunks(ServerLevel level, BlockPos origin) {
		TownState state = TownState.get(level);
		int size = TownData.get().size;
		int built = 0;
		for (int cx = origin.getX() >> 4; cx <= (origin.getX() + size - 1) >> 4; cx++) {
			for (int cz = origin.getZ() >> 4; cz <= (origin.getZ() + size - 1) >> 4; cz++) {
				if (state.built(new ChunkPos(cx, cz).pack())) {
					built++;
				}
			}
		}
		return built;
	}

	/** Changes every site in loaded chunks to the current theme at once (as the decorators would, given time). */
	private static void redecorate(ServerLevel level) {
		TownState state = TownState.get(level);
		state.setTheme(TownDecor.theme(), level.getGameTime());
		for (TownData.Site site : TownData.get().sites) {
			BlockPos at = state.origin().offset(site.anchor());
			if (level.getChunkSource().getChunkNow(at.getX() >> 4, at.getZ() >> 4) != null) {
				TownDecor.apply(level, site.index());
			}
		}
	}

	/** The town's whole volume, from its corner to {@code size} across and 60 up. */
	private static AABB townBox(BlockPos origin, int size) {
		return new AABB(origin.getX(), origin.getY(), origin.getZ(), origin.getX() + size, origin.getY() + 60, origin.getZ() + size);
	}

	private static String townsfolk(ServerLevel level, BlockPos origin) {
		int size = TownData.get().size;
		List<Townsfolk> people = level.getEntitiesOfClass(Townsfolk.class, townBox(origin, size));
		return people.size() + " " + people.stream().map(p -> p.role() + (p.shop().isEmpty() ? "" : "/" + p.shop())).sorted().toList();
	}
}
