package io.github.jimbozoomer.jugcraft.test;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.aircraft.Aircraft;
import io.github.jimbozoomer.jugcraft.aircraft.JugcraftAircraft;
import io.github.jimbozoomer.jugcraft.client.AircraftHud;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The pilot's flight help (AircraftHud) fits the window and names the keys the player has bound: in Fabric's 854x480
 * test window at the smallest GUI scale (1) and at the largest that window allows (automatic, 2), and in a 640x480
 * window, the smallest GUI any scale gives (320x240). Every drawn line lies inside the window and above the action bar,
 * every piece of help is drawn whole on one line, and the help names each control by its current binding; rebinding
 * the left key changes what it says. Screenshots of each layout are kept for review.
 */
public final class AircraftHelpHudClientGameTests implements FabricClientGameTest {
	/** The window Fabric's client game tests run in, put back afterwards. */
	private static final int TEST_WIDTH = 854;
	private static final int TEST_HEIGHT = 480;

	@Override
	public void runTest(ClientGameTestContext c) {
		boolean hudHidden = c.computeOnClient(mc -> mc.gui.hud.isHidden());
		c.runOnClient(mc -> {
			if (mc.gui.hud.isHidden()) mc.gui.hud.toggle();
		});
		try (var world = c.worldBuilder().adjustSettings(s -> s.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			world.getConnection().waitForChunksRender();
			var server = world.getServer();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon");
			server.runOnServer(s -> {
				var p = s.getPlayerList().getPlayers().getFirst();
				var a = JugcraftAircraft.TYPES.get("biplane").create(p.level(), EntitySpawnReason.COMMAND);
				a.snapTo(p.getX(), p.getY(), p.getZ(), p.getYRot(), 0);
				a.setOwner(p.getUUID());
				p.level().addFreshEntity(a);
				for (int i = 0; i < a.spec().sockets().size(); i++) {
					if (a.spec().sockets().get(i).type().equals("boiler")) {
						a.inventory().setItem(i, new ItemStack(Items.COAL, 16));
					}
				}
				p.startRiding(a);
			});
			c.waitTicks(10);
			int scale = c.computeOnClient(mc -> mc.options.guiScale().get());
			try {
				c.runOnClient(mc -> {
					if (!(mc.player.getVehicle() instanceof Aircraft a) || a.getFirstPassenger() != mc.player) {
						throw new AssertionError("The player should pilot the biplane");
					}
				});
				c.getInput().resizeWindow(TEST_WIDTH, TEST_HEIGHT);
				layout(c, 1, "854x480 at GUI scale 1");
				layout(c, 0, "854x480 at the largest GUI scale it allows");
				int largest = c.computeOnClient(mc -> mc.getWindow().getGuiScale());
				if (largest < 2) {
					throw new AssertionError("854x480 should allow GUI scale 2, got " + largest);
				}
				c.getInput().resizeWindow(640, 480);
				layout(c, 0, "640x480 at the largest GUI scale it allows (320x240)");

				// The keys shown are the player's own: bind left to J and the steering help says J.
				c.getInput().resizeWindow(TEST_WIDTH, TEST_HEIGHT);
				c.runOnClient(mc -> {
					mc.options.keyLeft.setKey(InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_J));
					KeyMapping.resetMapping();
				});
				layout(c, 0, "854x480 with left bound to J");
				c.runOnClient(mc -> {
					String left = mc.options.keyLeft.getTranslatedKeyMessage().getString();
					AircraftHud.Frame frame = AircraftHud.lastFrame();
					if (!left.equals("J") || !frame.pieces().get(1).contains(left)) {
						throw new AssertionError("The steering help should name the rebound left key (" + left + "): " + frame.pieces());
					}
				});
			} finally {
				c.runOnClient(mc -> {
					mc.options.keyLeft.setKey(mc.options.keyLeft.getDefaultKey());
					KeyMapping.resetMapping();
					mc.options.guiScale().set(scale);
				});
				c.getInput().resizeWindow(TEST_WIDTH, TEST_HEIGHT);
				c.runOnClient(Minecraft::resizeGui);
				c.waitTicks(2);
				server.runOnServer(s -> s.getPlayerList().getPlayers().getFirst().stopRiding());
			}
		} finally {
			c.runOnClient(mc -> {
				if (mc.gui.hud.isHidden() != hudHidden) mc.gui.hud.toggle();
			});
		}
	}

	/** Sets the GUI scale (0 is automatic: the largest the window allows), draws a frame and checks the help in it. */
	private static void layout(ClientGameTestContext c, int guiScale, String what) {
		c.runOnClient(mc -> {
			mc.options.guiScale().set(guiScale);
			mc.resizeGui();
		});
		c.waitTicks(3);
		// New-world welcome messages otherwise cover the controls in captures.
		c.runOnClient(mc -> mc.gui.hud.getChat().clearMessages(true));
		c.waitTicks(2);
		String shot = what.replaceAll("[^A-Za-z0-9]+", "_").toLowerCase(java.util.Locale.ROOT);
		c.takeScreenshot("aircraft_help_" + shot);
		c.runOnClient(mc -> check(mc, what));
	}

	private static void check(Minecraft mc, String what) {
		AircraftHud.Frame frame = AircraftHud.lastFrame();
		if (frame == null) {
			throw new AssertionError("The flight help was not drawn: " + what);
		}
		int width = mc.getWindow().getGuiScaledWidth(), height = mc.getWindow().getGuiScaledHeight();
		if (frame.guiWidth() != width || frame.guiHeight() != height) {
			throw new AssertionError("The last help was laid out for " + frame.guiWidth() + "x" + frame.guiHeight() + ", not the "
					+ width + "x" + height + " GUI: " + what);
		}
		var aircraft = (Aircraft) mc.player.getVehicle();
		List<String> pieces = AircraftHud.pieces(mc, aircraft).stream().map(net.minecraft.network.chat.Component::getString).toList();
		if (!frame.pieces().equals(pieces)) {
			throw new AssertionError("The drawn help is stale: " + frame.pieces() + " instead of " + pieces + ": " + what);
		}
		if (frame.lines().isEmpty()) {
			throw new AssertionError("No help lines: " + what);
		}
		for (AircraftHud.Line line : frame.lines()) {
			if (line.x() < 0 || line.y() < 0 || line.x() + line.width() > width || line.y() + line.height() > height) {
				throw new AssertionError("Help line \"" + line.text() + "\" at " + line.x() + "," + line.y() + " (" + line.width() + "x"
						+ line.height() + ") leaves the " + width + "x" + height + " GUI: " + what);
			}
			// Vanilla draws the action bar from guiHeight - 72; the hotbar and health rows are below that.
			if (line.y() + line.height() > height - 72) {
				throw new AssertionError("Help line \"" + line.text() + "\" overlaps the action bar or hotbar: " + what);
			}
		}
		for (String piece : pieces) {
			if (frame.lines().stream().noneMatch(line -> line.text().contains(piece))) {
				throw new AssertionError("The help \"" + piece + "\" is not drawn whole on one line: " + frame.lines() + ": " + what);
			}
		}
		var o = mc.options;
		for (KeyMapping key : List.of(o.keyUp, o.keyLeft, o.keyDown, o.keyRight, o.keyJump, o.keySprint, o.keyAttack, o.keyUse)) {
			String name = key.getTranslatedKeyMessage().getString();
			if (!frame.text().contains(name)) {
				throw new AssertionError("The help does not name " + key.getName() + " by its binding \"" + name + "\": " + frame.text()
						+ ": " + what);
			}
		}
		if (frame.scale() != 1F) {
			throw new AssertionError("The help should fit without shrinking at a " + width + "x" + height + " GUI: scale "
					+ frame.scale() + ": " + what);
		}
	}
}
