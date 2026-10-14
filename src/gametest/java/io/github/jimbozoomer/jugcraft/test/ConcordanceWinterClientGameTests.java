package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.rime.Frostward;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/**
 * Concordance Chunk B, unit 4 in a real client (docs/features/arcane-concordance-winter.md): Frostward's icon, the
 * owner's, is in the client's resources; then a shot for people to look at:
 * <ul>
 * <li>{@code jugcraft_winter_frostward_hud}: the player under Frostward (Winter Brine, B-08), given by command.</li>
 * </ul>
 * Nothing here judges a picture. The HUD is shown for the shot and put back as it was. CI job {@code client}.
 */
public class ConcordanceWinterClientGameTests implements FabricClientGameTest {
	private static final List<String> STATUSES = List.of("frostward");

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getConnection().waitForChunksRender();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("gamerule minecraft:advance_time false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("gamemode survival @a");
			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			server.runCommand(String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:snow_block", origin.getX() - 6, origin.getY() - 1,
					origin.getZ() - 6, origin.getX() + 6, origin.getY() - 1, origin.getZ() + 6));
			server.runCommand(String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:air", origin.getX() - 6, origin.getY(), origin.getZ() - 6,
					origin.getX() + 6, origin.getY() + 6, origin.getZ() + 6));
			server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f 180 10", origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5));
			context.waitTicks(20);
			boolean hudWasHidden = context.computeOnClient(client -> client.gui.hud.isHidden());
			setHudHidden(context, false);

			String missing = context.computeOnClient(ConcordanceWinterClientGameTests::missing);
			Jugcraft.LOGGER.info("[winter client] the owner's icons: {}", missing.isEmpty() ? "present" : missing);
			check(missing.isEmpty(), "The client lacks the winter icons: " + missing);

			// Frostward on the HUD, as a draught of Winter Brine leaves it (given by command: presentation, not evidence).
			server.runCommand("effect give @p jugcraft:frostward 60");
			context.waitTicks(20);
			boolean shown = context.computeOnClient(client -> client.player.hasEffect(Frostward.FROSTWARD));
			check(shown, "The client was not told of Frostward");
			context.takeScreenshot("jugcraft_winter_frostward_hud");
			server.runCommand("effect clear @p");
			setHudHidden(context, hudWasHidden);
		}
	}

	/** The status icons the client cannot find, or an empty string. */
	private static String missing(Minecraft client) {
		List<String> missing = new ArrayList<>();
		for (String status : STATUSES) {
			if (client.getResourceManager().getResource(Jugcraft.id("textures/mob_effect/" + status + ".png")).isEmpty()) {
				missing.add("textures/mob_effect/" + status + ".png");
			}
		}
		return String.join(", ", missing);
	}

	private static void setHudHidden(ClientGameTestContext context, boolean hidden) {
		context.runOnClient(client -> {
			if (client.gui.hud.isHidden() != hidden) {
				client.gui.hud.toggle();
			}
		});
	}

	private static void check(boolean ok, String message) {
		if (!ok) {
			throw new AssertionError(message);
		}
	}
}
