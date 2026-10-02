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
 * Server-side feature switches and options, read once at startup from config/jugcraft.properties
 * as {@code <feature>.enabled} and {@link #TEXT_OPTIONS}. A switch disables acquisition (worldgen and recipes);
 * it never unregisters content, so saved blocks and items survive.
 */
public final class JugcraftConfig {
	/** Every feature switch. Keep in sync with FEATURES in tools/materials.py. */
	public static final List<String> FEATURES = List.of(
			"tin", "zinc", "lead", "silver", "nickel", "tungsten", "uranium", "titanium", "aluminum",
			"salt", "phosphate", "lithium", "rare_earths", "sulfur", "silicon", "crude_oil", "machines",
			"deposits", "explosives", "agriculture", "parties", "drones",
			"pixel_hollows", "retro_trader");

	/**
	 * Other server options, with their defaults. {@code carving.free_draw}: players may carve any face into a
	 * pumpkin; false allows only the starter faces (for servers that want no free drawing).
	 */
	public static final Map<String, Boolean> OPTIONS = Map.of("carving.free_draw", true);

	/**
	 * Text options, with their defaults (see {@code season/SeasonCalendar.Settings}). The season follows the
	 * server's date in {@code seasons.timezone} for {@code seasons.hemisphere} ({@code north} or {@code south});
	 * {@code seasons.mode} is {@code auto} (follow the date), {@code spring}, {@code summer}, {@code autumn},
	 * {@code winter} (always that season) or {@code off} (vanilla colours). {@code seasons.snow} ({@code off} or
	 * {@code on}) lets winter lay snow, up to {@code seasons.snow_depth} layers, that melts in spring. Events on
	 * the same clock: the Harvest Feast ({@code harvest_feast}: {@code us}, {@code canada} or {@code off}, lasting
	 * {@code harvest_feast.days}) and December ({@code december}: {@code MM-DD..MM-DD} or {@code off}).
	 * The Halloween event (trick-or-treating) runs from {@code halloween.start} to {@code halloween.end} (month-day,
	 * both included) in {@code halloween.timezone} on the server's clock; {@code halloween.mode} is {@code auto}
	 * (follow the dates), {@code on} or {@code off} (for testing and off-season worlds); {@code halloween.harvest_moon}
	 * is the day (month-day) of the Harvest Moon, whose nights make crops grow faster. Treats already given are kept
	 * whatever the setting.
	 */
	public static final Map<String, String> TEXT_OPTIONS = Map.ofEntries(
			Map.entry("halloween.start", "10-20"), Map.entry("halloween.end", "11-03"), Map.entry("halloween.timezone", "UTC"),
			Map.entry("halloween.mode", "auto"), Map.entry("halloween.harvest_moon", "10-31"),
			Map.entry("seasons.mode", "auto"), Map.entry("seasons.hemisphere", "north"), Map.entry("seasons.timezone", "UTC"),
			Map.entry("seasons.snow", "off"), Map.entry("seasons.snow_depth", "2"),
			Map.entry("harvest_feast", "us"), Map.entry("harvest_feast.days", "4"),
			Map.entry("december", "12-01..01-06"));

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
					+ " the Halloween event runs: start and end as MM-DD, a timezone, mode auto, on or off, and the Harvest Moon's day;"
					+ " seasons.*: seasonal colours follow the server's date; mode auto, spring, summer, autumn,"
					+ " winter or off, hemisphere north or south, a timezone, and opt-in winter snow; harvest_feast us, canada or off;"
					+ " december MM-DD..MM-DD or off).");
		} catch (IOException e) {
			Jugcraft.LOGGER.warn("Could not write {}", path, e);
		}
	}
}
