package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.Epitaph;
import io.github.jimbozoomer.jugcraft.agriculture.HeadstoneBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HeadstoneBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.OpenEpitaphPayload;
import io.github.jimbozoomer.jugcraft.client.EpitaphScreen;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client game test for the graveyard pack's headstones: a churchyard of all nine, each engraved and at its own stage of
 * weathering, between a gravel path and a wrought-iron fence; by day, up close to read them, from above onto the table
 * tomb and ledger stone, at night by lantern light, and the epitaph screen as the Stonemason's Chisel opens it. It also
 * prints what the client received of each epitaph. CI job {@code client}.
 */
public class HeadstoneClientGameTests implements FabricClientGameTest {
	/** Each headstone: id, where (right, back from the origin), stage, epitaph. */
	private record Grave(String id, int dx, int dz, int stage, List<String> lines) {
	}

	private static final List<Grave> GRAVES = List.of(
			new Grave("gothic_headstone", 0, 6, 0, List.of("IN LOVING MEMORY", "ELIZA THORNE", "1841 - 1899", "AT REST")),
			new Grave("willow_urn_headstone", 2, 6, 1, List.of("HERE LYES Ye BODY", "OF CAPt JOHN AMES", "WHO DIED OCT 31", "1786")),
			new Grave("winged_skull_headstone", 4, 6, 2, List.of("MEMENTO MORI", "SILAS WICKHAM", "1702", "AS I AM SO SHALL YE BE")),
			new Grave("lamb_headstone", 6, 6, 0, List.of("LITTLE ROSE", "BUDDED ON EARTH", "TO BLOOM IN HEAVEN")),
			new Grave("rustic_scroll_headstone", 8, 6, 3, List.of("THOMAS BRIGGS", "STONEMASON", "1832 - 1904")),
			new Grave("broken_column", 0, 10, 1, List.of("HENRY CALDER", "1820 - 1849", "CUT DOWN", "IN HIS PRIME")),
			new Grave("celtic_cross", 3, 10, 2, List.of("MARY O'NEILL", "1856 - 1931", "REST IN PEACE")),
			new Grave("table_tomb", 6, 10, 3, List.of("SIR EDMUND HALLOWAY", "1690 - 1761", "REQUIESCAT IN PACE")),
			new Grave("ledger_stone", 9, 10, 2, List.of("HERE LIETH", "ANNE PRYOR", "1744")));

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			// Hide the HUD, hand and chat whatever an earlier test in this client left: CI shares the client tests out
			// between parallel jobs, and only the first job's opening test hides them.
			context.runOnClient(client -> {
				if (!client.gui.hud.isHidden()) {
					client.gui.hud.toggle();
				}
			});
			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			int x = origin.getX();
			int y = origin.getY();
			int z = origin.getZ();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 6, y - 3, z - 16, x + 16, y - 1, z + 8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 6, y, z - 16, x + 16, y + 10, z + 8));
			context.waitTicks(10);
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			context.runOnClient(client -> {
				for (Grave grave : GRAVES) {
					BlockPos pos = new BlockPos(x + grave.dx(), y, z - grave.dz());
					if (client.level.getBlockEntity(pos) instanceof HeadstoneBlockEntity stone) {
						System.out.println("[graveyard test] epitaph on the client, " + grave.id() + ": " + stone.epitaph().lines());
					}
				}
			});

			shoot(context, singleplayer, x + 5, y + 1, z + 2, 180, 14, "jugcraft_graveyard_headstones");
			shoot(context, singleplayer, x + 2, y + 1, z - 3, 180, 8, "jugcraft_graveyard_headstones_close");
			shoot(context, singleplayer, x + 6, y + 1, z - 4, 160, 12, "jugcraft_graveyard_monuments");
			shoot(context, singleplayer, x + 7, y + 3, z - 8, 180, 62, "jugcraft_graveyard_tomb_top");
			server.runCommand("time set midnight");
			context.waitTicks(10);
			shoot(context, singleplayer, x + 5, y + 1, z + 2, 180, 14, "jugcraft_graveyard_headstones_night");
			server.runCommand("time set noon");
			context.setScreen(() -> new EpitaphScreen(new OpenEpitaphPayload(new BlockPos(x, y, z - 6), Epitaph.of(GRAVES.get(0).lines()).padded())));
			context.waitTicks(10);
			context.takeScreenshot("jugcraft_graveyard_epitaph_screen");
			context.setScreen(() -> null);
		}
	}

	/** Stands the camera at (x, y, z) looking along yaw and pitch, and waits for the world to draw. */
	private static void place(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z, int yaw, int pitch) {
		TestServerContext server = singleplayer.getServer();
		server.runCommand("setblock %d %d %d minecraft:barrier".formatted(x, y - 1, z));
		server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f %d %d", x + 0.5, y, z + 0.5, yaw, pitch));
		context.waitTicks(20);
		singleplayer.getConnection().waitForChunksRender();
	}

	private static void shoot(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z, int yaw, int pitch,
			String name) {
		place(context, singleplayer, x, y, z, yaw, pitch);
		context.waitTicks(20);
		context.takeScreenshot(name);
	}

	private static void build(ServerLevel level, BlockPos origin) {
		int x = origin.getX();
		int y = origin.getY();
		int z = origin.getZ();
		// A gravel path in front, and a wrought-iron fence behind with lanterns on stone posts.
		for (int dx = -2; dx <= 12; dx++) {
			level.setBlock(new BlockPos(x + dx, y - 1, z - 3), Blocks.GRAVEL.defaultBlockState(), Block.UPDATE_ALL);
			level.setBlock(new BlockPos(x + dx, y, z - 13), JugcraftAgriculture.block("cemetery_fence").defaultBlockState(), Block.UPDATE_ALL);
		}
		for (int dx : new int[] {-2, 4, 12}) {
			BlockPos post = new BlockPos(x + dx, y, z - 13);
			level.setBlock(post, Blocks.STONE_BRICK_WALL.defaultBlockState(), Block.UPDATE_ALL);
			level.setBlock(post.above(), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false), Block.UPDATE_ALL);
		}
		for (Grave grave : GRAVES) {
			BlockPos pos = new BlockPos(x + grave.dx(), y, z - grave.dz());
			HeadstoneBlock headstone = (HeadstoneBlock) JugcraftAgriculture.block(grave.id());
			BlockState state = headstone.defaultBlockState().setValue(HeadstoneBlock.FACING, Direction.SOUTH);
			level.setBlock(pos, state, Block.UPDATE_ALL);
			headstone.setPlacedBy(level, pos, state, null, ItemStack.EMPTY);
			headstone.setWeather(level, pos, grave.stage(), false);
			if (level.getBlockEntity(pos) instanceof HeadstoneBlockEntity stone) {
				stone.engrave(Epitaph.of(grave.lines()));
			}
		}
	}
}
