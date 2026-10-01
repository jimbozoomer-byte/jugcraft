package io.github.jimbozoomer.jugcraft.season;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

/**
 * The server's season, from its own date (never a client's clock). The season is a "season day" from 1 to 365 on the
 * northern calendar: 1 is the first of January, 293 the 20th of October. The southern hemisphere is half a year
 * ahead (its first of January is northern day 183). Day 0 means seasons are off and colours stay vanilla.
 */
public final class SeasonCalendar {
	public static final int DAYS = 365;
	/** Half a year, for the southern hemisphere. */
	public static final int SOUTH_OFFSET = 182;

	/** How the season is chosen: from the date, fixed to one season (for testing and off-season worlds) or off. */
	public enum Mode {
		AUTO(-1), SPRING(105), SUMMER(196), AUTUMN(293), WINTER(15), OFF(0);

		/** The season day a fixed mode shows (mid-season), or -1 for {@link #AUTO}. */
		public final int day;

		Mode(int day) {
			this.day = day;
		}

		public static Mode parse(String text) {
			return valueOf(text.trim().toUpperCase(Locale.ROOT));
		}
	}

	/** The operator's settings: {@code seasons.mode}, {@code seasons.hemisphere} and {@code seasons.timezone}. */
	public record Settings(Mode mode, boolean southern, ZoneId zone) {
		public static final Settings DEFAULT = new Settings(Mode.AUTO, false, ZoneId.of("UTC"));

		/** The settings in config/jugcraft.properties; an unreadable value keeps its default and logs a warning. */
		public static Settings fromConfig() {
			Mode mode = DEFAULT.mode;
			String modeText = JugcraftConfig.textOption("seasons.mode");
			try {
				mode = Mode.parse(modeText);
			} catch (IllegalArgumentException e) {
				Jugcraft.LOGGER.warn("seasons.mode \"{}\" is not auto, spring, summer, autumn, winter or off; using auto", modeText);
			}
			boolean southern = DEFAULT.southern;
			String hemisphere = JugcraftConfig.textOption("seasons.hemisphere").trim().toLowerCase(Locale.ROOT);
			if (hemisphere.equals("south") || hemisphere.equals("north")) {
				southern = hemisphere.equals("south");
			} else {
				Jugcraft.LOGGER.warn("seasons.hemisphere \"{}\" is not north or south; using north", hemisphere);
			}
			ZoneId zone = DEFAULT.zone;
			String zoneText = JugcraftConfig.textOption("seasons.timezone");
			try {
				zone = ZoneId.of(zoneText.trim());
			} catch (DateTimeException e) {
				Jugcraft.LOGGER.warn("seasons.timezone \"{}\" is not a time zone (for example UTC or Europe/London); using UTC", zoneText);
			}
			return new Settings(mode, southern, zone);
		}

		public Settings withMode(Mode newMode) {
			return new Settings(newMode, southern, zone);
		}

		/** The season day on {@code date} (in this zone): the mode's fixed day, or the date's day in this hemisphere. */
		public int dayOn(LocalDate date) {
			return mode == Mode.AUTO ? seasonDay(date, southern) : mode.day;
		}
	}

	private SeasonCalendar() {
	}

	/**
	 * The season day of a date, 1 to 365. The 29th of February shares the 28th's day, so every year has 365; in the
	 * southern hemisphere the day is half a year on.
	 */
	public static int seasonDay(LocalDate date, boolean southern) {
		int day = date.getDayOfYear();
		if (date.isLeapYear() && day > 59) {
			day--;
		}
		return southern ? (day - 1 + SOUTH_OFFSET) % DAYS + 1 : day;
	}
}
