package io.github.jimbozoomer.jugcraft.client;

import java.util.Properties;

/** Local presentation only; these choices never change a dose or its server-owned penalties. */
public record DrugVisualOptions(boolean enabled, int intensity, int blur, int motion, int doubleVision) {
	public static final DrugVisualOptions DEFAULT = new DrugVisualOptions(true, 75, 70, 60, 70);
	public DrugVisualOptions {
		intensity = Math.clamp(intensity, 0, 100);
		blur = Math.clamp(blur, 0, 100);
		motion = Math.clamp(motion, 0, 100);
		doubleVision = Math.clamp(doubleVision, 0, 100);
	}
	public static DrugVisualOptions read(Properties properties) {
		return new DrugVisualOptions(!"false".equalsIgnoreCase(properties.getProperty("pharmacology.visuals", "true").trim()),
				readPercent(properties, "intensity", DEFAULT.intensity), readPercent(properties, "blur", DEFAULT.blur),
				readPercent(properties, "motion", DEFAULT.motion), readPercent(properties, "double_vision", DEFAULT.doubleVision));
	}
	private static int readPercent(Properties properties, String name, int fallback) {
		try { return Integer.parseInt(properties.getProperty("pharmacology." + name, Integer.toString(fallback)).trim()); }
		catch (NumberFormatException ignored) { return fallback; }
	}
	public void write(Properties properties) {
		properties.setProperty("pharmacology.visuals", Boolean.toString(enabled));
		properties.setProperty("pharmacology.intensity", Integer.toString(intensity));
		properties.setProperty("pharmacology.blur", Integer.toString(blur));
		properties.setProperty("pharmacology.motion", Integer.toString(motion));
		properties.setProperty("pharmacology.double_vision", Integer.toString(doubleVision));
	}
}
