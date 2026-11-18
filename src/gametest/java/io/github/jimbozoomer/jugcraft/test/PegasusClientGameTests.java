package io.github.jimbozoomer.jugcraft.test;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.client.pegasus.PegasusClient;
import io.github.jimbozoomer.jugcraft.pegasus.JugcraftPegasus;
import io.github.jimbozoomer.jugcraft.pegasus.Pegasus;
import io.github.jimbozoomer.jugcraft.pegasus.PegasusFlight;
import io.github.jimbozoomer.jugcraft.pegasus.PegasusFlight.State;
import java.util.Arrays;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Client game test for the Pegasus (docs/features/pegasus.md), flown by the real client with real keys: the two keys
 * in their own category with Space and Left Ctrl defaults; Space still pressing vanilla Jump when not mounted; holding
 * Ascend taking off and climbing; level flight; no input while chat is open; Descend not sprinting; a landing without
 * damage; and this client's own copy of the entity (what any observer receives) following the server's synced flight
 * state, its wings opening in flight, flapping while it climbs and folding after landing, with its light pack on the
 * saddle (docs/features/horse-breeds.md). Screenshots {@code jugcraft_pegasus_*}. Written for
 * the repository's client suite; not run in this change.
 */
public class PegasusClientGameTests implements FabricClientGameTest {
	private static void check(boolean yes, String why) {
		if (!yes) {
			throw new AssertionError(why);
		}
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		context.runOnClient(client -> {
			KeyMapping up = PegasusClient.ascendKey();
			KeyMapping down = PegasusClient.descendKey();
			check(Arrays.asList(client.options.keyMappings).contains(up) && Arrays.asList(client.options.keyMappings).contains(down),
					"The Pegasus keys are registered");
			check(up.getDefaultKey().getValue() == InputConstants.KEY_SPACE && down.getDefaultKey().getValue() == InputConstants.KEY_LCONTROL,
					"Ascend defaults to Space and Descend to Left Ctrl");
			check(up.getCategory() == PegasusClient.CATEGORY && down.getCategory() == PegasusClient.CATEGORY, "in their own category");
			check(up.getName().equals(JugcraftPegasus.ASCEND_KEY) && down.getName().equals(JugcraftPegasus.DESCEND_KEY), "under their lang keys");
		});
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			context.runOnClient(client -> {
				if (!client.gui.hud.isHidden()) {
					client.gui.hud.toggle();
				}
			});
			TestServerContext server = singleplayer.getServer();
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("gamerule minecraft:send_command_feedback false");

			// Not on a pegasus, Space is still vanilla's Jump (and Ascend reads it too).
			context.getInput().holdKey(options -> options.keyJump);
			context.waitTicks(2);
			boolean[] space = context.computeOnClient(client -> new boolean[] {client.options.keyJump.isDown(),
					PegasusClient.held(client, PegasusClient.ascendKey()), PegasusClient.input(client) == 0});
			context.getInput().releaseKey(options -> options.keyJump);
			check(space[0], "Space must still press vanilla Jump while Ascend is bound to it");
			check(space[1], "Ascend reads Space");
			check(space[2], "Off a pegasus, no flight input is read");

			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			int x = origin.getX();
			int y = origin.getY();
			int z = origin.getZ();
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 12, y - 1, z - 12, x + 12, y - 1, z + 12));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 12, y, z - 12, x + 12, y + 30, z + 12));
			context.waitTicks(10);
			int id = server.computeOnServer(minecraft -> {
				ServerPlayer player = minecraft.getPlayerList().getPlayers().get(0);
				Pegasus pegasus = JugcraftPegasus.PEGASUS.create(player.level(), EntitySpawnReason.COMMAND);
				pegasus.snapTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
				pegasus.tameWithName(player);
				pegasus.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
				pegasus.pack().setFitted(true);                 // its light pack, drawn on the saddle in every shot
				player.level().addFreshEntity(pegasus);
				player.startRiding(pegasus, true, false);
				return pegasus.getId();
			});
			context.waitTicks(20);
			check(context.computeOnClient(client -> PegasusClient.piloted(client) != null), "The player pilots the pegasus");
			float health = context.computeOnClient(client -> client.player.getHealth());
			double ground = server.computeOnServer(minecraft -> minecraft.overworld().getEntity(id).getY());
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
			context.takeScreenshot("jugcraft_pegasus_saddled");

			// Holding Ascend takes off and climbs.
			context.getInput().holdKey(options -> PegasusClient.ascendKey());
			context.waitTicks(PegasusFlight.TAKEOFF_HOLD_TICKS + 40);
			String climbing = server.computeOnServer(minecraft -> {
				Pegasus pegasus = (Pegasus) minecraft.overworld().getEntity(id);
				return pegasus.flightState() + " " + (pegasus.getY() - ground);
			});
			check(climbing.startsWith("FLYING") && Double.parseDouble(climbing.split(" ")[1]) > 3.0, "Held, Ascend takes off and climbs: " + climbing);
			String seen = context.computeOnClient(client -> {
				Pegasus mine = (Pegasus) client.level.getEntity(id);
				return mine.flightState() + " " + mine.flightVertical() + " " + mine.wingOpen(1.0F);
			});
			check(seen.startsWith("FLYING 1 ") && Float.parseFloat(seen.split(" ")[2]) > 0.9F,
					"The client's copy follows the synced state, wings spread and climbing: " + seen);
			float flap = context.computeOnClient(client -> ((Pegasus) client.level.getEntity(id)).wingAmp(1.0F));
			check(flap > 0.5F, "Climbing, the wings flap hard: " + flap);
			check(context.computeOnClient(client -> ((Pegasus) client.level.getEntity(id)).packFitted()), "The client draws the pack");
			context.takeScreenshot("jugcraft_pegasus_climbing");

			// Released: level flight.
			context.getInput().releaseKey(options -> PegasusClient.ascendKey());
			context.waitTicks(15);
			double before = server.computeOnServer(minecraft -> minecraft.overworld().getEntity(id).getY());
			context.waitTicks(20);
			double after = server.computeOnServer(minecraft -> minecraft.overworld().getEntity(id).getY());
			check(Math.abs(after - before) < 0.5, "Releasing both keys holds level flight: " + (after - before));
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			context.takeScreenshot("jugcraft_pegasus_level");

			// Chat open: the keys are not read, even held.
			context.getInput().pressKey(options -> options.keyChat);
			context.waitForScreen(ChatScreen.class);
			context.getInput().holdKey(options -> PegasusClient.ascendKey());
			context.waitTicks(10);
			int typing = context.computeOnClient(client -> PegasusClient.input(client) | PegasusClient.lastSentInput());
			context.getInput().releaseKey(options -> PegasusClient.ascendKey());
			check(typing == 0, "No flight input while chat is open: " + typing);
			context.getInput().pressKey(InputConstants.KEY_ESCAPE);
			context.waitForScreen(null);

			// Descend: down and land, without sprinting and without damage.
			context.getInput().holdKey(options -> PegasusClient.descendKey());
			context.waitTicks(10);
			boolean sprinting = context.computeOnClient(client -> client.player.isSprinting());
			check(!sprinting, "Left Ctrl descends without sprinting while piloting");
			String descending = context.computeOnClient(client -> String.valueOf(((Pegasus) client.level.getEntity(id)).flightVertical()));
			check(descending.equals("-1"), "Observers see a descent: " + descending);
			State landed = State.FLYING;
			for (int wait = 0; wait < 30 && landed != State.GROUNDED; wait++) {
				context.waitTicks(10);
				landed = server.computeOnServer(minecraft -> ((Pegasus) minecraft.overworld().getEntity(id)).flightState());
			}
			context.getInput().releaseKey(options -> PegasusClient.descendKey());
			check(landed == State.GROUNDED, "Descend lands: " + landed);
			context.waitTicks(20);
			String afterLanding = server.computeOnServer(minecraft -> {
				Pegasus pegasus = (Pegasus) minecraft.overworld().getEntity(id);
				return pegasus.getHealth() + " " + pegasus.getMaxHealth() + " " + pegasus.isNoGravity();
			});
			check(afterLanding.matches("(\\S+) \\1 false"), "Landing hurt the pegasus or left gravity off: " + afterLanding);
			check(context.computeOnClient(client -> client.player.getHealth()) == health, "Landing did not hurt the rider");
			float folded = context.computeOnClient(client -> ((Pegasus) client.level.getEntity(id)).wingOpen(1.0F));
			check(folded <= (float) PegasusFlight.WING_OPEN_RIDDEN + 0.01F, "After landing the wings fold (lifted for the rider): " + folded);
			context.takeScreenshot("jugcraft_pegasus_landed");
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			context.waitTicks(5);
			context.takeScreenshot("jugcraft_pegasus_folded_with_pack");
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));
		}
	}
}
