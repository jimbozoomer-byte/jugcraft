package io.github.jimbozoomer.jugcraft.config;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Server-side feature switches, read once at startup from config/jugcraft.properties
 * as {@code <feature>.enabled}. A switch disables acquisition (worldgen and recipes);
 * it never unregisters content, so saved blocks and items survive.
 */
public final class JugcraftConfig {
	/** Every feature switch. Keep in sync with FEATURES in tools/materials.py. */
	public static final List<String> FEATURES = List.of(
			"tin", "zinc", "lead", "silver", "nickel", "tungsten", "uranium", "titanium", "aluminum",
			"salt", "phosphate", "lithium", "rare_earths", "sulfur", "silicon", "crude_oil", "machines",
			"deposits", "agriculture");

	/**
	 * Other server options, with their defaults. {@code carving.free_draw}: players may carve any face into a
	 * pumpkin; false allows only the starter faces (for servers that want no free drawing).
	 */
	public static final Map<String, Boolean> OPTIONS = Map.of("carving.free_draw", true);

	private static final String FILE_NAME = "jugcraft.properties";
	private static final Map<String, Boolean> ENABLED = new LinkedHashMap<>();
	private static final Map<String, Boolean> OPTION_VALUES = new LinkedHashMap<>(OPTIONS);

	private JugcraftConfig() {
	}

	public static boolean isFeatureEnabled(String feature) {
		return ENABLED.getOrDefault(feature, false);
	}

	/** A server option from {@link #OPTIONS}, as set in the file (its default before {@link #load}). */
	public static boolean option(String key) {
		Boolean value = OPTION_VALUES.get(key);
		if (value == null) {
			throw new IllegalArgumentException("No Jugcraft option " + key);
		}
		return value;
	}

	public static void load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		Properties properties = new Properties();

		if (Files.isRegularFile(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				properties.load(reader);
			} catch (IOException e) {
				Jugcraft.LOGGER.error("Could not read {}; using defaults", path, e);
			}
		}

		for (String feature : FEATURES) {
			String key = feature + ".enabled";
			boolean enabled = Boolean.parseBoolean(properties.getProperty(key, "true"));
			ENABLED.put(feature, enabled);
			properties.setProperty(key, Boolean.toString(enabled));
		}

		for (Map.Entry<String, Boolean> option : OPTIONS.entrySet()) {
			boolean value = Boolean.parseBoolean(properties.getProperty(option.getKey(), option.getValue().toString()));
			OPTION_VALUES.put(option.getKey(), value);
			properties.setProperty(option.getKey(), Boolean.toString(value));
		}

		try (Writer writer = Files.newBufferedWriter(path)) {
			properties.store(writer, "Jugcraft feature switches (false stops new worldgen and recipes; existing items and blocks stay)"
					+ " and server options (carving.free_draw=false allows only the starter pumpkin faces).");
		} catch (IOException e) {
			Jugcraft.LOGGER.warn("Could not write {}", path, e);
		}
	}
}
