package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.sign.Presentation;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/**
 * This player's own Arcane Concordance display settings ({@code config/jugcraft-client.properties}): whether the
 * Focus line shows while an instrument is held, reduced motion (fewer, calmer particles and no sliding in the journal),
 * the visual intensity (how many particles signs and blocks draw: {@link Presentation.Intensity}), whether the journal
 * shows exact values beside its descriptions, and whether it always opens as the simple screen. Presentation only; the
 * server never sees them. Changed from {@link ConcordanceSettingsScreen} and the journal.
 */
public final class ConcordanceClientOptions {
	private static final String FILE_NAME = "jugcraft-client.properties";
	private static boolean hud = true;
	private static boolean reducedMotion;
	private static boolean exactValues;
	private static boolean simpleJournal;
	private static Presentation.Intensity intensity = Presentation.Intensity.FULL;

	private ConcordanceClientOptions() {
	}

	public static boolean hud() {
		return hud;
	}

	public static boolean reducedMotion() {
		return reducedMotion;
	}

	public static boolean exactValues() {
		return exactValues;
	}

	public static boolean simpleJournal() {
		return simpleJournal;
	}

	public static Presentation.Intensity intensity() {
		return intensity;
	}

	public static void set(boolean showHud, boolean calm, boolean exact, boolean simple, Presentation.Intensity chosen) {
		hud = showHud;
		reducedMotion = calm;
		exactValues = exact;
		simpleJournal = simple;
		intensity = chosen;
		Presentation.configure(intensity, reducedMotion);
		save();
	}

	/** The journal's own switch for exact values (kept with the other settings). */
	public static void setExactValues(boolean exact) {
		exactValues = exact;
		save();
	}

	static void load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		Properties properties = new Properties();
		if (Files.isRegularFile(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				properties.load(reader);
			} catch (IOException e) {
				Jugcraft.LOGGER.error("Could not read {}; using defaults", path, e);
			}
		}
		hud = Boolean.parseBoolean(properties.getProperty("concordance.hud", "true"));
		reducedMotion = Boolean.parseBoolean(properties.getProperty("concordance.reduced_motion", "false"));
		exactValues = Boolean.parseBoolean(properties.getProperty("concordance.exact_values", "false"));
		simpleJournal = Boolean.parseBoolean(properties.getProperty("concordance.simple_journal", "false"));
		intensity = Presentation.Intensity.fromId(properties.getProperty("concordance.visual_intensity", "full"));
		Presentation.configure(intensity, reducedMotion);
		save();
	}

	private static void save() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		Properties properties = new Properties();
		properties.setProperty("concordance.hud", Boolean.toString(hud));
		properties.setProperty("concordance.reduced_motion", Boolean.toString(reducedMotion));
		properties.setProperty("concordance.exact_values", Boolean.toString(exactValues));
		properties.setProperty("concordance.simple_journal", Boolean.toString(simpleJournal));
		properties.setProperty("concordance.visual_intensity", intensity.id());
		try (Writer writer = Files.newBufferedWriter(path)) {
			properties.store(writer, "Jugcraft client display settings (this computer only): concordance.hud shows Focus while"
					+ " a Concordance instrument is held; concordance.reduced_motion makes Kindled light, the bench and the journal"
					+ " calmer; concordance.exact_values shows exact figures in the journal; concordance.simple_journal always"
					+ " opens the plain journal screen; concordance.visual_intensity (full, reduced or minimal) is how many"
					+ " particles the Concordance draws (warnings always show).");
		} catch (IOException e) {
			Jugcraft.LOGGER.warn("Could not write {}", path, e);
		}
	}
}
