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
			"salt", "phosphate", "lithium", "rare_earths", "sulfur", "silicon", "crude_oil", "machines", "agriculture");

	/**
	 * Other server options, with their defaults. {@code carving.free_draw}: players may carve any face into a
	 * pumpkin; false allows only the starter faces (for servers that want no free drawing).
	 */
	public static final Map<String, Boolean> OPTIONS = Map.of("carving.free_draw", true);

	/**
	 * Text options, with their defaults. The Halloween event (trick-or-treating) runs from
	 * {@code halloween.start} to {@code halloween.end} (month-day, both included) in {@code halloween.timezone}
	 * on the server's clock; {@code halloween.mode} is {@code auto} (follow the dates), {@code on} or {@code off}
	 * (for testing and off-season worlds); {@code halloween.harvest_moon} is the day (month-day) of the Harvest
	 * Moon, whose nights make crops grow faster. Treats already given are kept whatever the setting.
	 */
	public static final Map<String, String> TEXT_OPTIONS = Map.of(
			"halloween.start", "10-20", "halloween.end", "11-03", "halloween.timezone", "UTC", "halloween.mode", "auto",
			"halloween.harvest_moon", "10-31");

	private static final String FILE_NAME = "jugcraft.properties";
	private static final Map<String, Boolean> ENABLED = new LinkedHashMap<>();
	private static final Map<String, Boolean> OPTION_VALUES = new LinkedHashMap<>(OPTIONS);
	private static final Map<String, String> TEXT_VALUES = new LinkedHashMap<>(TEXT_OPTIONS);

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

	/** A text option from {@link #TEXT_OPTIONS}, as set in the file (its default before {@link #load}). */
	public static String textOption(String key) {
		String value = TEXT_VALUES.get(key);
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

		for (Map.Entry<String, String> option : TEXT_OPTIONS.entrySet()) {
			String value = properties.getProperty(option.getKey(), option.getValue()).trim();
			TEXT_VALUES.put(option.getKey(), value);
			properties.setProperty(option.getKey(), value);
		}

		try (Writer writer = Files.newBufferedWriter(path)) {
			properties.store(writer, "Jugcraft feature switches (false stops new worldgen and recipes; existing items and blocks stay)"
					+ " and server options (carving.free_draw=false allows only the starter pumpkin faces; halloween.* sets when"
					+ " the Halloween event runs: start and end as MM-DD, a timezone, mode auto, on or off, and the Harvest Moon's day).");
		} catch (IOException e) {
			Jugcraft.LOGGER.warn("Could not write {}", path, e);
		}
	}
}
