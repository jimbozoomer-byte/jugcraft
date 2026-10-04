package io.github.jimbozoomer.jugcraft.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jspecify.annotations.Nullable;

/**
 * The moving decorations' shapes, from assets/jugcraft/decor_quads.json (written by tools/decor6_data.py from the
 * same boxes as their block and item models: the rocking chair and the giant fake spider) and decor7_quads.json
 * (tools/decor7_data.py: the haunted chandelier, the suit of armor's helmet and the creepy doll's head) and
 * decor8_quads.json (tools/decor8_data.py: the lab table's patient, the specimens, the sarcophagus's lid and mummy, the
 * raven and the black cat's tail) and decor9_quads.json (tools/decor9_data.py: the yard inflatables, the porch witch's
 * arm and head, the wind chimes' bones and skull, and the weathervanes' vanes) and decor10_quads.json (tools/decor10_data.py:
 * the floating witch hat and its flame) and decor11_quads.json (tools/decor11_data.py: the jump-scare trap's lid, ghost
 * and spring, the bowling pumpkin, the ghost bell and its clapper, and the fortune teller's planchette) and
 * decor12_quads.json (tools/decor12_data.py: the haunted hayride's wagon and lantern) and ferris_wheel_quads.json
 * (tools/ferris_wheel_data.py: the Ferris wheel's frame, wheel, lights and cars) and pinata_quads.json
 * (tools/pinata_data.py: the piñatas, whole and torn, and their ropes) and balloon_quads.json
 * (tools/hot_air_balloon_data.py: the hot-air balloons' basket and rigging, flames and envelopes, and the pibal) and
 * decor16_quads.json (tools/decor16_data.py: the flying eyeball, its iris and its wings).
 */
public final class DecorQuads {
	private static final List<Identifier> FILES = List.of(Jugcraft.id("decor_quads.json"), Jugcraft.id("decor7_quads.json"),
			Jugcraft.id("decor8_quads.json"), Jugcraft.id("decor9_quads.json"), Jugcraft.id("decor10_quads.json"),
			Jugcraft.id("decor11_quads.json"), Jugcraft.id("decor12_quads.json"), Jugcraft.id("ferris_wheel_quads.json"), Jugcraft.id("pinata_quads.json"),
			Jugcraft.id("balloon_quads.json"), Jugcraft.id("decor16_quads.json"));
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
		for (Identifier file : FILES) {
			Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(file);
			if (resource.isEmpty()) {
				Jugcraft.LOGGER.warn("Missing {}: the decorations in it will not be drawn", file);
				continue;
			}
			try (Reader reader = resource.get().openAsReader()) {
				JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
					out.put(entry.getKey(), QuadModel.parse(entry.getValue().getAsJsonArray()));
				}
			} catch (Exception e) {
				Jugcraft.LOGGER.warn("Could not read {}", file, e);
			}
		}
		return out;
	}
}
