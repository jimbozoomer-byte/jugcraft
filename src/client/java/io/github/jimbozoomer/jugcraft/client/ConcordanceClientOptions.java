package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.LumenMoteBlock;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/**
 * This player's own Arcane Concordance display settings ({@code config/jugcraft-client.properties}): whether the
 * Focus line shows while an instrument is held, and reduced motion (fewer, calmer particles). Presentation only; the
 * server never sees them. Changed from {@link ConcordanceSettingsScreen}.
 */
public final class ConcordanceClientOptions {
	private static final String FILE_NAME = "jugcraft-client.properties";
	private static boolean hud = true;
	private static boolean reducedMotion;

	private ConcordanceClientOptions() {
	}

	public static boolean hud() {
		return hud;
	}

	public static boolean reducedMotion() {
		return reducedMotion;
	}

	public static void set(boolean showHud, boolean calm) {
		hud = showHud;
		reducedMotion = calm;
		LumenMoteBlock.reducedMotion = calm;
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
		LumenMoteBlock.reducedMotion = reducedMotion;
		save();
	}

	private static void save() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		Properties properties = new Properties();
		properties.setProperty("concordance.hud", Boolean.toString(hud));
		properties.setProperty("concordance.reduced_motion", Boolean.toString(reducedMotion));
		try (Writer writer = Files.newBufferedWriter(path)) {
			properties.store(writer, "Jugcraft client display settings (this computer only): concordance.hud shows Focus while"
					+ " a Concordance instrument is held; concordance.reduced_motion makes Kindled light and the bench calmer.");
		} catch (IOException e) {
			Jugcraft.LOGGER.warn("Could not write {}", path, e);
		}
	}
}
