package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.artillery.JugcraftArtillery;
import io.github.jimbozoomer.jugcraft.client.WalkerCockpitClient;
import io.github.jimbozoomer.jugcraft.walker.DieselWalker;
import io.github.jimbozoomer.jugcraft.walker.HowitzerWalker;
import io.github.jimbozoomer.jugcraft.walker.JugcraftWalkers;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

/**
 * The walker cockpit (docs/features/walker-cockpit.md) through the pilot's own eyes, for each walker: the view from
 * the seat with the telegraphs (the cannon's and howitzer's arcs and landing, the fist's, ram's and stomp's reach with the
 * pigs it would hit outlined, the drill's block) and the gauges; the view turning with the hull on left/right; and the
 * third-person camera sitting back past the hull.
 */
public class WalkerCockpitClientGameTests implements FabricClientGameTest {
	private static final List<EntityType<? extends DieselWalker>> WALKERS = List.of(JugcraftWalkers.DIESEL_WALKER,
			JugcraftWalkers.ARMOURED_WALKER, JugcraftWalkers.HOWITZER_WALKER);

	@Override
	public void runTest(ClientGameTestContext context) {
		try (var world = context.worldBuilder().adjustSettings(
				creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			var server = world.getServer();
			world.getConnection().waitForChunksRender();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			BlockPos origin = context.computeOnClient(client -> client.player.blockPosition().offset(0, 4, 0));
			int x = origin.getX(), y = origin.getY(), z = origin.getZ();
			server.runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(x - 12, y - 1, z - 10, x + 12, y - 1, z + 60));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 12, y, z - 10, x + 12, y + 14, z + 60));
			// Something to shell: a low wall 30 blocks ahead, and a block for the Diesel Walker's drill.
			server.runCommand("fill %d %d %d %d %d %d minecraft:bricks".formatted(x - 4, y, z + 30, x + 4, y + 1, z + 30));
			server.runCommand("setblock %d %d %d minecraft:cobblestone".formatted(x, y + 1, z + 3));
			for (EntityType<? extends DieselWalker> type : WALKERS) {
				String kind = EntityType.getKey(type).getPath();
				server.runOnServer(minecraft -> {
					var player = minecraft.getPlayerList().getPlayers().getFirst();
					player.stopRiding();
					for (var old : player.level().getEntitiesOfClass(DieselWalker.class, player.getBoundingBox().inflate(80))) {
						old.discard();
					}
					for (var old : player.level().getEntitiesOfClass(net.minecraft.world.entity.animal.pig.Pig.class,
							player.getBoundingBox().inflate(80))) {
						old.discard();
					}
					player.teleportTo(x + 0.5, y, z + 0.5);
					DieselWalker walker = type.create(player.level(), EntitySpawnReason.COMMAND);
					walker.snapTo(x + 0.5, y, z + 0.5, 0, 0);
					walker.setFuel(DieselWalker.FUEL_TANK * 3 / 4);
					player.level().addFreshEntity(walker);
					// A pig in reach of the fist, ram or stomp, and one out of it.
					for (int[] at : new int[][] {{1, 2}, {-6, 8}}) {
						var pig = EntityTypes.PIG.create(player.level(), EntitySpawnReason.COMMAND);
						pig.snapTo(x + 0.5 + at[0], y, z + 0.5 + at[1], 180, 0);
						pig.setNoAi(true);
						player.level().addFreshEntity(pig);
					}
					player.setGameMode(GameType.SURVIVAL);
					player.getInventory().clearContent();
					player.getInventory().add(new ItemStack(JugcraftArtillery.HEAVY_SHELL_ITEM, 12));
					player.startRiding(walker, true, true);
				});
				context.waitTicks(10);
				context.runOnClient(client -> {
					client.options.setCameraType(CameraType.FIRST_PERSON);
					client.player.setYRot(0);
					client.player.setXRot(type == JugcraftWalkers.DIESEL_WALKER ? 35 : 6);
				});
				context.waitTicks(40);
				context.runOnClient(client -> {
					if (!(client.player.getVehicle() instanceof DieselWalker)) {
						throw new AssertionError("The pilot should be riding the " + kind);
					}
					if (WalkerCockpitClient.describe() == null) {
						throw new AssertionError("The cockpit should show for the " + kind + "'s pilot");
					}
					if (type != JugcraftWalkers.DIESEL_WALKER && !WalkerCockpitClient.hasImpact()) {
						throw new AssertionError("The " + kind + "'s telegraph should show where its shell lands: " + WalkerCockpitClient.describe());
					}
				});
				context.takeScreenshot("walker_cockpit_" + kind + "_seat");

				// Turning the hull with left turns the view with it.
				float[] before = context.computeOnClient(client -> new float[] {client.player.getYRot(), client.player.getVehicle().getYRot()});
				context.getInput().holdKey(options -> options.keyLeft);
				context.waitTicks(20);
				context.getInput().releaseKey(options -> options.keyLeft);
				context.waitTicks(10);
				context.runOnClient(client -> {
					float hullTurned = Mth.wrapDegrees(client.player.getVehicle().getYRot() - before[1]);
					float viewTurned = Mth.wrapDegrees(client.player.getYRot() - before[0]);
					if (Math.abs(hullTurned) < 10) {
						throw new AssertionError("Holding left should turn the " + kind + ", but it turned " + hullTurned);
					}
					if (Math.abs(viewTurned - hullTurned) > 4) {
						throw new AssertionError("The " + kind + "'s pilot's view should turn with the hull (" + hullTurned
								+ "), but it turned " + viewTurned);
					}
				});
				context.takeScreenshot("walker_cockpit_" + kind + "_turned");

				context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
				context.waitTicks(10);
				context.runOnClient(client -> {
					DieselWalker walker = (DieselWalker) client.player.getVehicle();
					double distance = client.gameRenderer.mainCamera().position().distanceTo(client.player.getEyePosition());
					if (distance < walker.cameraDistance() * 0.7) {
						throw new AssertionError("The third-person camera should sit back past the " + kind + ", but it is " + distance);
					}
				});
				context.takeScreenshot("walker_cockpit_" + kind + "_behind");
				context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));
				if (type == JugcraftWalkers.HOWITZER_WALKER) {
					// The view does not follow the gun laying itself (which would chase the look round): left alone, it stays put.
					float look = context.computeOnClient(client -> client.player.getYRot());
					context.waitTicks(30);
					context.runOnClient(client -> {
						if (Math.abs(Mth.wrapDegrees(client.player.getYRot() - look)) > 1) {
							throw new AssertionError("The Howitzer Walker's pilot's view should stay where they left it, but it moved from "
									+ look + " to " + client.player.getYRot());
						}
						if (!(client.player.getVehicle() instanceof HowitzerWalker)) {
							throw new AssertionError("The pilot should still be in the Howitzer Walker");
						}
					});
				}
			}
			server.runOnServer(minecraft -> {
				var player = minecraft.getPlayerList().getPlayers().getFirst();
				player.stopRiding();
				player.setGameMode(GameType.CREATIVE);
			});
		} finally {
			context.getInput().releaseKey(options -> options.keyLeft);
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));
		}
	}
}
