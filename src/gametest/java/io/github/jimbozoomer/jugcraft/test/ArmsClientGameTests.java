package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.weapons.JugcraftArms;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;

/**
 * Client game test for the arms (batch 42): every arm held by an armor stand (each kind's in-hand pose; a rack of
 * bronze, then one of steel), every arm in an item frame (the inventory sprite), and the player holding a greatsword, a halberd and a lance
 * in first person, a greatsword from the front, and parrying with a longsword and charging with a lance while holding
 * use (CI job {@code client}).
 */
public class ArmsClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			int x = origin.getX();
			int y = origin.getY();
			int z = origin.getZ();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(x - 4, y - 1, z - 16, x + 20, y - 1, z + 8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 4, y, z - 16, x + 20, y + 8, z + 8));
			context.waitTicks(10);

			// Bronze arms on a rack of armor stands facing south, steel arms on a second rack nearer the camera, and all of
			// them in item frames high on a wall behind.
			List<String> bronze = JugcraftArms.ITEMS.keySet().stream().filter(id -> id.startsWith("bronze_")).toList();
			List<String> steel = JugcraftArms.ITEMS.keySet().stream().filter(id -> id.startsWith("steel_")).toList();
			server.runCommand("fill %d %d %d %d %d %d minecraft:spruce_planks".formatted(x, y, z - 13, x + 15, y + 5, z - 13));
			for (int i = 0; i < bronze.size(); i++) {
				stand(server, x + 1.5 + i * 1.5, y, z - 10.5, bronze.get(i));
				stand(server, x + 1.5 + i * 1.5, y, z - 3.5, steel.get(i));
				frame(server, x + 3 + i, y + 3, z - 12, bronze.get(i));
				frame(server, x + 3 + i, y + 4, z - 12, steel.get(i));
			}
			context.waitTicks(20);
			// Hide the HUD, hand and chat for the scenery shots.
			context.getInput().pressKey(options -> options.keyToggleGui);
			shoot(context, singleplayer, x + 7, y + 1, z - 5, 180, 12, "jugcraft_arms_bronze_rack");
			shoot(context, singleplayer, x + 7, y + 1, z + 2, 180, 12, "jugcraft_arms_steel_rack");
			shoot(context, singleplayer, x + 7, y + 3, z - 7, 180, -5, "jugcraft_arms_frames");
			context.getInput().pressKey(options -> options.keyToggleGui);

			// In the hand, first person (the hotbar shows), then the greatsword from the front.
			server.runCommand("tp @p %d %d %d 180 0".formatted(x + 7, y, z + 5));
			for (String arm : List.of("steel_greatsword", "steel_halberd", "steel_lance")) {
				server.runCommand("item replace entity @p weapon.mainhand with jugcraft:" + arm);
				context.waitTicks(30);
				context.takeScreenshot("jugcraft_arms_held_" + arm);
			}
			server.runCommand("item replace entity @p weapon.mainhand with jugcraft:steel_greatsword");
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			context.waitTicks(30);
			context.takeScreenshot("jugcraft_arms_greatsword_front");

			// Holding use: a longsword parries (from the front and in first person), a lance charges.
			server.runCommand("item replace entity @p weapon.mainhand with jugcraft:steel_longsword");
			context.waitTicks(10);
			context.getInput().holdKey(options -> options.keyUse);
			context.waitTicks(15);
			Jugcraft.LOGGER.info("[arms client] holding use with a longsword: blocking {}", context.computeOnClient(client -> client.player.isBlocking()));
			context.takeScreenshot("jugcraft_arms_parry_front");
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));
			context.waitTicks(5);
			context.takeScreenshot("jugcraft_arms_parry");
			context.getInput().releaseKey(options -> options.keyUse);
			server.runCommand("item replace entity @p weapon.mainhand with jugcraft:steel_lance");
			context.waitTicks(10);
			context.getInput().holdKey(options -> options.keyUse);
			context.waitTicks(15);
			Jugcraft.LOGGER.info("[arms client] holding use with a lance: using {}", context.computeOnClient(client -> client.player.isUsingItem()));
			context.takeScreenshot("jugcraft_arms_lance_charge");
			context.getInput().releaseKey(options -> options.keyUse);
		}
	}

	private static void stand(TestServerContext server, double x, int y, double z, String arm) {
		server.runCommand(String.format(Locale.ROOT, "summon minecraft:armor_stand %.1f %d %.1f {ShowArms:1b,NoBasePlate:1b,Rotation:[0f,0f],"
				+ "equipment:{mainhand:{id:\"jugcraft:%s\",count:1}}}", x, y, z, arm));
	}

	private static void frame(TestServerContext server, int x, int y, int z, String arm) {
		server.runCommand("summon minecraft:item_frame %d %d %d {Facing:3b,Fixed:1b,Item:{id:\"jugcraft:%s\",count:1}}".formatted(x, y, z, arm));
	}

	private static void shoot(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z,
			int yaw, int pitch, String name) {
		TestServerContext server = singleplayer.getServer();
		server.runCommand("setblock %d %d %d minecraft:barrier".formatted(x, y - 1, z));
		server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f %d %d", x + 0.5, y, z + 0.5, yaw, pitch));
		context.waitTicks(40);
		singleplayer.getConnection().waitForChunksRender();
		context.takeScreenshot(name);
	}
}
