package io.github.jimbozoomer.jugcraft.test;

import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;

/** Actual held/GUI/action views for review, including the existing colonial weapons reused by this expansion. */
public class ThanksgivingWeaponsClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		boolean hidden = context.computeOnClient(c -> c.gui.hud.isHidden());
		try (TestSingleplayerContext world = context.worldBuilder()
				.adjustSettings(c -> c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			world.getConnection().waitForChunksRender();
			var server = world.getServer();
			BlockPos at = context.computeOnClient(c -> c.player.blockPosition());
			int x = at.getX(), y = at.getY(), z = at.getZ();
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("gamerule minecraft:advance_time false");
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("fill %d %d %d %d %d %d minecraft:stone_bricks".formatted(x-12,y-1,z-12,x+12,y-1,z+12));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x-12,y,z-12,x+12,y+8,z+12));
			server.runCommand("tp @p %d %d %d 180 0".formatted(x,y,z));
			context.runOnClient(c -> { if (c.gui.hud.isHidden()) c.gui.hud.toggle(); });
			for (String id : List.of("naval_cutlass", "stone_headed_hatchet", "hunting_spear", "woodland_hunting_bow",
					"duelling_pistol", "line_musket", "bellmouth")) {
				server.runCommand("item replace entity @p weapon.mainhand with jugcraft:" + id);
				context.runOnClient(c -> c.options.setCameraType(CameraType.FIRST_PERSON));
				context.waitTicks(20);
				capture(context, "jugcraft_tg_weapon_" + id + "_first");
				context.runOnClient(c -> c.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
				context.waitTicks(10);
				capture(context, "jugcraft_tg_weapon_" + id + "_held");
				if (id.equals("naval_cutlass") || id.equals("stone_headed_hatchet") || id.equals("hunting_spear") || id.equals("woodland_hunting_bow")) {
					context.getInput().holdKey(o -> o.keyUse);
					context.waitTicks(20);
					if (!context.computeOnClient(c -> c.player.isUsingItem())) throw new AssertionError(id + " did not enter use pose");
					capture(context, "jugcraft_tg_weapon_" + id + "_use");
					context.getInput().releaseKey(o -> o.keyUse);
					context.waitTicks(2);
					capture(context, "jugcraft_tg_weapon_" + id + "_release");
				}
				context.runOnClient(c -> c.options.setCameraType(CameraType.FIRST_PERSON));
				context.getInput().pressKey(o -> o.keyInventory);
				context.waitTicks(8);
				capture(context, "jugcraft_tg_weapon_" + id + "_inventory");
				context.runOnClient(c -> c.gui.setScreen(null));
				server.runCommand("kill @e[type=jugcraft:thrown_arm]");
			}
		} finally {
			context.runOnClient(c -> {
				c.options.setCameraType(CameraType.FIRST_PERSON);
				if (c.gui.hud.isHidden() != hidden) c.gui.hud.toggle();
			});
		}
	}

	private static void capture(ClientGameTestContext context, String name) {
		// Welcome/command chat obscures the held model; keep the HUD on so first-person hands still render.
		context.runOnClient(c -> { c.gui.hud.getChat().clearMessages(true); c.gui.toastManager().clear(); });
		context.waitTicks(2);
		context.takeScreenshot(name);
	}
}
