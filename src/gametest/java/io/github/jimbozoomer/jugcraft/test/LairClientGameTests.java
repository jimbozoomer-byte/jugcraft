package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.HeadstoneBlock;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairInstance;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import io.github.jimbozoomer.jugcraft.lair.MistGateEntity;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;

/**
 * Client game test for the lairs and the Hollow Acre (docs/features/hollow-acre.md): the Last Rites' grave at midnight
 * with its candles, wreath and the mist gate open over it; then, through the gate, the Hollow Acre from the arrival point,
 * above the Mown Circle, before the bone chapel's throne, at the lych gate's Grey Mist, and from off the island. CI job
 * {@code client}.
 */
public class LairClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getConnection().waitForChunksRender();
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
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set midnight");
			server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 8, y - 3, z - 10, x + 12, y - 1, z + 6));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 8, y, z - 10, x + 12, y + 8, z + 6));
			context.waitTicks(10);

			// The Last Rites' grave, and the gate they open over it.
			BlockPos grave = new BlockPos(x + 2, y, z - 5);
			server.runOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				Block headstone = BuiltInRegistries.BLOCK.getValue(Jugcraft.id("gothic_headstone"));
				level.setBlock(grave, headstone.defaultBlockState().setValue(HeadstoneBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
				level.setBlock(grave.south(), JugcraftLairs.MOURNING_WREATH.defaultBlockState(), Block.UPDATE_ALL);
				for (BlockPos candle : new BlockPos[] {grave.west(2), grave.east(2), grave.west(2).south(2), grave.east(2).south(2)}) {
					level.setBlock(candle, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true).setValue(CandleBlock.CANDLES, 3),
							Block.UPDATE_ALL);
				}
				LairInstance instance = Lairs.open(minecraft, Lair.HOLLOW_ACRE);
				if (instance == null) {
					throw new IllegalStateException("No Hollow Acre opened");
				}
				MistGateEntity.open(level, grave, instance, 6000);
			});
			context.waitTicks(20);
			shoot(context, singleplayer, "minecraft:overworld", x + 2.5, y + 1, z + 1.5, 180, 15, "jugcraft_last_rites");

			// Through the gate.
			server.runOnServer(minecraft -> {
				ServerPlayer player = minecraft.getPlayerList().getPlayers().get(0);
				LairInstance instance = Lairs.open(Lair.HOLLOW_ACRE).getFirst();
				Lairs.enter(player, instance);
			});
			context.waitTicks(40);
			context.runOnClient(client -> {
				client.player.getAbilities().flying = true;
				client.player.onUpdateAbilities();
			});
			singleplayer.getConnection().waitForChunksRender();
			BlockPos o = Lair.HOLLOW_ACRE.origin(0);
			String acre = Lair.HOLLOW_ACRE.dimension.identifier().toString();
			shoot(context, singleplayer, acre, o.getX() + 32.0, o.getY() + 18, o.getZ() + 67.5, 180, 8, "jugcraft_hollow_acre_arrival");
			shoot(context, singleplayer, acre, o.getX() + 32.0, o.getY() + 30, o.getZ() + 64.5, 180, 24, "jugcraft_hollow_acre_arena");
			shoot(context, singleplayer, acre, o.getX() + 31.5, o.getY() + 19, o.getZ() + 27.5, 180, 6, "jugcraft_hollow_acre_chapel");
			shoot(context, singleplayer, acre, o.getX() + 32.0, o.getY() + 18, o.getZ() + 63.5, 0, 4, "jugcraft_hollow_acre_gate");
			shoot(context, singleplayer, acre, o.getX() + 84.0, o.getY() + 26, o.getZ() + 104.0, 145, 14, "jugcraft_hollow_acre_island");
		}
	}

	/** Stands the camera at (x, y, z) in {@code dimension}, looking along yaw and pitch, waits for the world to draw, and shoots. */
	private static void shoot(ClientGameTestContext context, TestSingleplayerContext singleplayer, String dimension, double x, double y,
			double z, int yaw, int pitch, String name) {
		TestServerContext server = singleplayer.getServer();
		server.runCommand(String.format(Locale.ROOT, "execute in %s run tp @a %.1f %.1f %.1f %d %d", dimension, x, y, z, yaw, pitch));
		context.waitTicks(30);
		singleplayer.getConnection().waitForChunksRender();
		context.waitTicks(10);
		context.takeScreenshot(name);
	}
}
