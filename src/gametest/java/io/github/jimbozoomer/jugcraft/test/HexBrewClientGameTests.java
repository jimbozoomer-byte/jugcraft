package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.BubblingCauldronBlock;
import io.github.jimbozoomer.jugcraft.agriculture.BubblingCauldronBlock.Brew;
import io.github.jimbozoomer.jugcraft.agriculture.Hexes;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Client game test for the hex brews: the three hex brews bubbling in cauldrons over campfires, their draughts framed
 * on a wall behind; then the same seen by a player shrunk by the Shrinking Draught and by one grown by the Giant's
 * Draught. CI job {@code client}.
 */
public class HexBrewClientGameTests implements FabricClientGameTest {
	private static final List<Brew> HEXES = List.of(Brew.SHRINKING, Brew.GIANT, Brew.FLYING);

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
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 6, y - 3, z - 12, x + 12, y - 1, z + 8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 6, y, z - 12, x + 12, y + 10, z + 8));
			context.waitTicks(10);
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			for (int i = 0; i < HEXES.size(); i++) {
				server.runCommand("summon minecraft:item_frame %d %d %d {Facing:3b,Fixed:1b,Item:{id:\"jugcraft:%s\",count:1}}"
						.formatted(x + 1 + 2 * i, y + 2, z - 5, Hexes.draughtId(HEXES.get(i))));
			}
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();

			shoot(context, singleplayer, x + 3, y + 1, z + 1, 180, 25, "jugcraft_hex_brews");
			shoot(context, singleplayer, x + 1, y + 1, z - 2, 180, 45, "jugcraft_hex_shrinking_brew");
			// As a shrunk player sees it: the pot towers overhead.
			server.runCommand("effect give @p jugcraft:shrunk 120 0 true");
			shoot(context, singleplayer, x + 3, y, z - 1, 180, -10, "jugcraft_hex_shrunk");
			server.runCommand("effect clear @p");
			// And as a giant: the pots at their feet.
			server.runCommand("effect give @p jugcraft:giant 120 0 true");
			shoot(context, singleplayer, x + 3, y, z + 1, 180, 22, "jugcraft_hex_giant");
			server.runCommand("effect clear @p");
		}
	}

	/** Stands the camera at (x, y, z) looking along yaw and pitch, and waits for the world to draw. */
	private static void place(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z, int yaw, int pitch) {
		TestServerContext server = singleplayer.getServer();
		// Only into air, so standing on the ground leaves no hole in it for the later pictures.
		server.runCommand("fill %d %d %d %d %d %d minecraft:barrier replace minecraft:air".formatted(x, y - 1, z, x, y - 1, z));
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

	/** Three cauldrons over campfires, each holding a hex brew, before a cobbled wall for the framed draughts. */
	private static void build(ServerLevel level, BlockPos origin) {
		int x = origin.getX();
		int y = origin.getY();
		int z = origin.getZ();
		for (int dx = -1; dx <= 7; dx++) {
			for (int dy = 0; dy <= 4; dy++) {
				level.setBlock(new BlockPos(x + dx, y + dy, z - 6), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		for (int i = 0; i < HEXES.size(); i++) {
			BlockPos pot = new BlockPos(x + 1 + 2 * i, y + 1, z - 3);
			level.setBlock(pot.below(), Blocks.CAMPFIRE.defaultBlockState(), Block.UPDATE_ALL);
			level.setBlock(pot, JugcraftAgriculture.block("bubbling_cauldron").defaultBlockState().setValue(BubblingCauldronBlock.CONTENTS, HEXES.get(i))
					.setValue(BubblingCauldronBlock.DOSES_LEFT, BubblingCauldronBlock.DOSES - i), Block.UPDATE_ALL);
		}
	}
}
