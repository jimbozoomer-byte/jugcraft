package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.io.InputStream;
import java.util.Base64;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.resources.Identifier;

/** PROBE (removed before review): logs vanilla 26.3's bow, crossbow and shield item models and sprites, to match them. */
public class ArmsProbeClientGameTests implements FabricClientGameTest {
	private static final String[] PATHS = {"items/bow.json", "items/crossbow.json", "items/shield.json", "models/item/bow.json",
			"models/item/bow_pulling_0.json", "models/item/crossbow.json", "models/item/crossbow_pulling_0.json",
			"models/item/crossbow_arrow.json", "models/item/shield.json", "models/item/shield_blocking.json",
			"models/item/generated.json", "models/item/handheld.json", "textures/item/bow.png", "textures/item/bow_pulling_0.png",
			"textures/item/bow_pulling_1.png", "textures/item/bow_pulling_2.png", "textures/item/crossbow_standby.png",
			"textures/item/crossbow_pulling_0.png", "textures/item/crossbow_pulling_1.png", "textures/item/crossbow_pulling_2.png",
			"textures/item/crossbow_arrow.png", "textures/item/crossbow_firework.png"};

	@Override
	public void runTest(ClientGameTestContext context) {
		context.runOnClient(client -> {
			for (String path : PATHS) {
				try {
					var resource = client.getResourceManager().getResource(Identifier.withDefaultNamespace(path));
					if (resource.isEmpty()) {
						Jugcraft.LOGGER.info("[probe] ASSET {} missing", path);
						continue;
					}
					try (InputStream in = resource.get().open()) {
						Jugcraft.LOGGER.info("[probe] ASSET {} {}", path, Base64.getEncoder().encodeToString(in.readAllBytes()));
					}
				} catch (Exception exception) {
					Jugcraft.LOGGER.info("[probe] ASSET {} unreadable: {}", path, exception.toString());
				}
			}
		});
	}
}
