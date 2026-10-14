package io.github.jimbozoomer.jugcraft.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jspecify.annotations.Nullable;

/**
 * The look of each machine's screen (docs/features/machine-screens.md): which of the five looks by tech level it uses
 * (workshop, industrial, electric, lab, advanced) and that look's colours, from assets/jugcraft/gui/machine_themes.json
 * (written by tools/gui_textures.py, which also draws the backgrounds). Industrial forms all use one look, the file's
 * "forms".
 */
public final class MachineScreenThemes {
	private static final Identifier FILE = Jugcraft.id("gui/machine_themes.json");
	/** Used when the file is missing or a machine is not in it: the industrial look's colours. */
	public static final Theme FALLBACK = new Theme("industrial", Jugcraft.id("textures/gui/machine_industrial.png"), 0xFF60E66E,
			0xFF308C3E, 0xFFFF6046, 0xFF60E66E, 0xFF60E66E, 0xFF96FFA0, 0xFF18191A, 0xFF60E66E, 0xFF0A0B0C, 0xFF1A1C1D,
			0xFF707477, 0xFF24A84C, 0xFF125C28, 0xFF78F096, 0xFFE6FFEC, false);
	private static @Nullable Map<String, Theme> machines;
	private static Theme forms = FALLBACK;

	private MachineScreenThemes() {
	}

	/**
	 * A look: its name and background, then its colours (ARGB): screen text bright and dim, warnings, all's-well, the
	 * energy and progress fills, the title on the body, the help box's text, the slots (dark edge, face, light edge),
	 * the buttons (face, edge, highlight, icon), and whether slots and buttons are thin outlines (the screen-like
	 * looks) rather than sunken bevels.
	 */
	public record Theme(String look, Identifier texture, int text, int dim, int warn, int ok, int energy, int progress, int label,
			int help, int slotDark, int slotFace, int slotLight, int buttonFace, int buttonEdge, int buttonLight, int buttonIcon,
			boolean outlined) {
	}

	public static Theme of(MachineKind kind) {
		return load().getOrDefault(kind.id, FALLBACK);
	}

	/** The look every industrial form's screen uses. */
	public static Theme forms() {
		load();
		return forms;
	}

	private static Map<String, Theme> load() {
		if (machines != null) {
			return machines;
		}
		Map<String, Theme> out = new HashMap<>();
		machines = out;
		Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(FILE);
		if (resource.isEmpty()) {
			Jugcraft.LOGGER.warn("Missing {}: machine screens use the default look", FILE);
			return out;
		}
		try (Reader reader = resource.get().openAsReader()) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			Map<String, Theme> looks = new HashMap<>();
			for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("themes").entrySet()) {
				JsonObject t = entry.getValue().getAsJsonObject();
				looks.put(entry.getKey(), new Theme(entry.getKey(), Jugcraft.id("textures/gui/machine_" + entry.getKey() + ".png"),
						color(t, "text"), color(t, "dim"), color(t, "warn"), color(t, "ok"), color(t, "energy"),
						color(t, "progress"), color(t, "label"), color(t, "help"), color(t, "slot_dark"), color(t, "slot_face"),
						color(t, "slot_light"), color(t, "button_face"), color(t, "button_edge"), color(t, "button_light"),
						color(t, "button_icon"), t.has("outlined") && t.get("outlined").getAsBoolean()));
			}
			for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("machines").entrySet()) {
				Theme theme = looks.get(entry.getValue().getAsString());
				if (theme != null) {
					out.put(entry.getKey(), theme);
				}
			}
			if (root.has("forms") && looks.containsKey(root.get("forms").getAsString())) {
				forms = looks.get(root.get("forms").getAsString());
			}
		} catch (Exception e) {
			Jugcraft.LOGGER.warn("Could not read {}", FILE, e);
		}
		return out;
	}

	private static int color(JsonObject theme, String key) {
		return (int) Long.parseLong(theme.get(key).getAsString(), 16);
	}
}
