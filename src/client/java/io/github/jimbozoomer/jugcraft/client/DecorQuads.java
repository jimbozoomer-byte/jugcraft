package io.github.jimbozoomer.jugcraft.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jspecify.annotations.Nullable;

/**
 * The moving decorations' shapes, from assets/jugcraft/decor_quads.json (written by tools/decor6_data.py from the
 * same boxes as their block and item models): the rocking chair and the giant fake spider.
 */
public final class DecorQuads {
	private static final Identifier FILE = Jugcraft.id("decor_quads.json");
	private static @Nullable Map<String, QuadModel> models;

	private DecorQuads() {
	}

	/** The named model, or null if the file is missing (the decoration then isn't drawn). */
	public static @Nullable QuadModel get(String name) {
		if (models == null) {
			models = load();
		}
		return models.get(name);
	}

	private static Map<String, QuadModel> load() {
		Map<String, QuadModel> out = new HashMap<>();
		Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(FILE);
		if (resource.isEmpty()) {
			Jugcraft.LOGGER.warn("Missing {}: rocking chairs and fake spiders will not be drawn", FILE);
			return out;
		}
		try (Reader reader = resource.get().openAsReader()) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
				out.put(entry.getKey(), QuadModel.parse(entry.getValue().getAsJsonArray()));
			}
		} catch (Exception e) {
			Jugcraft.LOGGER.warn("Could not read {}", FILE, e);
		}
		return out;
	}
}
