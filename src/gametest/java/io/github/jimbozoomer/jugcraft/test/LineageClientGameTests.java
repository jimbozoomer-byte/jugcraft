package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.lineage.Gift;
import io.github.jimbozoomer.jugcraft.lineage.Line;
import io.github.jimbozoomer.jugcraft.lineage.Lineage;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;

/**
 * Client game test for star-bred lines (docs/features/star-bred-lines.md): the Astrawand held in first person (its 3D
 * baton, not a flat sprite) over a pen of star-bred animals; the player from the front holding it; its 16x16 icon in an
 * item frame, close up; and a reading, the flourish, sparkles and notes, with the reading's lines in the chat. CI job
 * {@code client}.
 */
public class LineageClientGameTests implements FabricClientGameTest {
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
			server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 8, y - 3, z - 12, x + 8, y - 1, z + 6));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 8, y, z - 12, x + 8, y + 8, z + 6));
			context.waitTicks(10);
			server.runOnServer(minecraft -> pen(minecraft.overworld(), origin));
			server.runCommand("summon minecraft:item_frame %d %d %d {Facing:3b,Fixed:1b,Item:{id:\"jugcraft:astrawand\",count:1}}"
					.formatted(x + 5, y + 2, z - 10));
			server.runCommand("setblock %d %d %d minecraft:spruce_planks".formatted(x + 5, y + 2, z - 11));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			context.runOnClient(client -> {
				if (client.gui.hud.isHidden()) {
					client.gui.hud.toggle();
				}
				client.options.setCameraType(CameraType.FIRST_PERSON);
			});

			// In the hand, first person (the hotbar shows), over the pen.
			server.runCommand("item replace entity @p weapon.mainhand with jugcraft:astrawand");
			place(context, singleplayer, x, y, z + 2, 180, 20);
			context.takeScreenshot("jugcraft_astrawand_held");
			// A reading: the flourish, the sparkles and notes, and its words in the chat. The ewe stands two blocks ahead.
			context.getInput().holdKey(options -> options.keyUse);
			context.waitTicks(3);
			context.getInput().releaseKey(options -> options.keyUse);
			context.waitTicks(4);
			context.takeScreenshot("jugcraft_astrawand_reading");
			// From the front, holding it.
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			context.waitTicks(30);
			context.takeScreenshot("jugcraft_astrawand_front");
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));
			// Its icon in a frame, close up, with the hand and HUD hidden.
			context.runOnClient(client -> {
				if (!client.gui.hud.isHidden()) {
					client.gui.hud.toggle();
				}
			});
			place(context, singleplayer, x + 5, y + 1, z - 8, 180, -5);
			context.takeScreenshot("jugcraft_astrawand_frame");
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

	/** A star-bred animal standing still at (x, y, z), facing the camera (south), with {@code line}. */
	private static void animal(ServerLevel level, EntityType<? extends Animal> type, double x, int y, double z, Line line) {
		Animal animal = type.create(level, EntitySpawnReason.COMMAND);
		if (animal == null) {
			return;
		}
		animal.setNoAi(true);
		animal.setPersistenceRequired();
		animal.snapTo(x, y, z, 0.0F, 0.0F);
		animal.setYHeadRot(0.0F);
		animal.setYBodyRot(0.0F);
		animal.setAttached(Lineage.LINE, line);
		level.addFreshEntity(animal);
	}

	private static void pen(ServerLevel level, BlockPos origin) {
		int x = origin.getX();
		int y = origin.getY();
		int z = origin.getZ();
		Line moonborn = Line.of(new int[] {5, 4, 3, 3, 2, 2, 3, 1}, Map.of(Gift.MOONFLEECE.id, 2), "jugcraft:full_moon", 3);
		// The ewe two blocks ahead of the camera, the rest of the pen behind her.
		animal(level, EntityType.SHEEP, x + 0.5, y, z - 0.5, moonborn);
		animal(level, EntityType.COW, x - 2.5, y, z - 4.5, Line.of(new int[] {3, 3, 5, 5, 4, 3, 2, 2}, Map.of(Gift.HEAVY_HIDE.id, 1), "", 2));
		animal(level, EntityType.GOAT, x + 3.0, y, z - 4.0, Line.of(new int[] {2, 2, 2, 2, 2, 2, 5, 5}, Map.of(Gift.ANGORA.id, 2), "jugcraft:echo_comet", 1));
		animal(level, EntityType.CHICKEN, x - 1.0, y, z - 2.5, Line.of(new int[] {4, 4, 1, 1, 3, 3, 2, 2}, Map.of(Gift.DOUBLE_YOLK.id, 2), "", 1));
		animal(level, JugcraftAgriculture.TURKEY, x + 1.8, y, z - 2.8, Line.of(new int[] {2, 1, 2, 1, 2, 1, 2, 1}, Map.of(), "", 0));
	}
}
