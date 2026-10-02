package io.github.jimbozoomer.jugcraft.season;

/**
 * Season state that game code on both sides reads: whether winter snow is falling now. The server sets it from its
 * own settings and date; a client sets it from the server's {@link SeasonPayload} and clears it on leaving.
 */
public final class SeasonState {
	private static volatile boolean snowing;

	private SeasonState() {
	}

	/** Whether winter snow falls now in biomes tagged {@code #jugcraft:has_winter_snow}. */
	public static boolean snowing() {
		return snowing;
	}

	public static void setSnowing(boolean value) {
		snowing = value;
	}
}
