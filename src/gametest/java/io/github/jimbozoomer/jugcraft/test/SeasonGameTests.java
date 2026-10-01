package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.season.SeasonCalendar;
import io.github.jimbozoomer.jugcraft.season.SeasonPalette;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/** Seasonal colours: the server's calendar, the operator's settings, the palette and the biome tag. */
public class SeasonGameTests {
	@GameTest
	public void seasonDaysFollowTheCalendar(GameTestHelper helper) {
		// 20 October is peak autumn in the north and spring (half a year on) in the south.
		equal(helper, SeasonCalendar.seasonDay(LocalDate.of(2026, 10, 20), false), 293, "20 October, north");
		equal(helper, SeasonCalendar.seasonDay(LocalDate.of(2026, 10, 20), true), 110, "20 October, south");
		// The year turns over cleanly in both hemispheres, and leap years still end on day 365.
		equal(helper, SeasonCalendar.seasonDay(LocalDate.of(2026, 12, 31), false), 365, "31 December");
		equal(helper, SeasonCalendar.seasonDay(LocalDate.of(2027, 1, 1), false), 1, "1 January");
		equal(helper, SeasonCalendar.seasonDay(LocalDate.of(2027, 1, 1), true), 183, "1 January, south");
		equal(helper, SeasonCalendar.seasonDay(LocalDate.of(2028, 12, 31), false), 365, "31 December, leap year");
		equal(helper, SeasonCalendar.seasonDay(LocalDate.of(2028, 2, 29), false),
				SeasonCalendar.seasonDay(LocalDate.of(2028, 2, 28), false), "29 February shares the 28th");
		equal(helper, SeasonCalendar.seasonDay(LocalDate.of(2028, 3, 1), false),
				SeasonCalendar.seasonDay(LocalDate.of(2026, 3, 1), false), "1 March in a leap year");
		for (LocalDate date = LocalDate.of(2028, 1, 1); date.getYear() == 2028; date = date.plusDays(1)) {
			for (boolean southern : new boolean[] {false, true}) {
				int day = SeasonCalendar.seasonDay(date, southern);
				helper.assertTrue(day >= 1 && day <= SeasonCalendar.DAYS, "Season day " + day + " on " + date + " is outside the year");
			}
		}
		helper.succeed();
	}

	@GameTest
	public void modesAndTimeZonesSetTheDay(GameTestHelper helper) {
		LocalDate june = LocalDate.of(2026, 6, 1);
		SeasonCalendar.Settings settings = new SeasonCalendar.Settings(SeasonCalendar.Mode.AUTO, false, ZoneOffset.UTC);
		equal(helper, settings.dayOn(june), SeasonCalendar.seasonDay(june, false), "auto follows the date");
		equal(helper, settings.withMode(SeasonCalendar.Mode.AUTUMN).dayOn(june), 293, "autumn override");
		equal(helper, settings.withMode(SeasonCalendar.Mode.WINTER).dayOn(june), 15, "winter override");
		equal(helper, settings.withMode(SeasonCalendar.Mode.OFF).dayOn(june), 0, "off");
		equal(helper, SeasonCalendar.Mode.parse(" Autumn "), SeasonCalendar.Mode.AUTUMN, "mode text");
		// The server's zone decides when the day turns: 23:30 UTC on 31 December is already New Year in Auckland.
		Instant instant = Instant.parse("2026-12-31T23:30:00Z");
		equal(helper, settings.dayOn(LocalDate.ofInstant(instant, ZoneOffset.UTC)), 365, "UTC");
		equal(helper, settings.dayOn(LocalDate.ofInstant(instant, ZoneId.of("Pacific/Auckland"))), 1, "Auckland");
		helper.succeed();
	}

	@GameTest
	public void overridesLastUntilRestart(GameTestHelper helper) {
		// The test server's config file has the defaults, and a mode set at runtime is not written back, so a
		// restart reads the file again.
		SeasonCalendar.Settings fromFile = SeasonCalendar.Settings.fromConfig();
		equal(helper, fromFile, SeasonCalendar.Settings.DEFAULT, "settings in the test config");
		SeasonCalendar.Settings before = JugcraftSeasons.settings();
		JugcraftSeasons.setMode(helper.getLevel().getServer(), SeasonCalendar.Mode.WINTER);
		equal(helper, JugcraftSeasons.today(), 15, "winter override");
		equal(helper, SeasonCalendar.Settings.fromConfig(), fromFile, "settings after an override");
		JugcraftSeasons.setMode(helper.getLevel().getServer(), before.mode());
		equal(helper, JugcraftSeasons.settings(), before, "restored settings");
		helper.succeed();
	}

	@GameTest
	public void paletteKeepsSummerAndTurnsAutumn(GameTestHelper helper) {
		int foliage = 0xFF77AB2F;
		int grass = 0xFF91BD59;
		equal(helper, SeasonPalette.colour(0, foliage, true, 10, 20), foliage, "off");
		equal(helper, SeasonPalette.colour(SeasonCalendar.Mode.SUMMER.day, foliage, true, 10, 20), foliage, "summer foliage");
		equal(helper, SeasonPalette.colour(SeasonCalendar.Mode.SUMMER.day, grass, false, 10, 20), grass, "summer grass");
		int autumn = SeasonPalette.colour(SeasonCalendar.Mode.AUTUMN.day, foliage, true, 10, 20);
		helper.assertTrue((autumn >>> 24) == 0xFF, "Autumn lost the tint's alpha");
		helper.assertTrue((autumn >> 16 & 0xFF) > (foliage >> 16 & 0xFF) + 40, "Autumn leaves are not redder: " + Integer.toHexString(autumn));
		helper.assertTrue(SeasonPalette.colour(SeasonCalendar.Mode.WINTER.day, grass, false, 10, 20) != grass, "Winter grass is vanilla");
		// Colours change gradually, day by day and across New Year, and the same place gets the same colour.
		for (int day = 1; day <= SeasonCalendar.DAYS; day++) {
			int next = day % SeasonCalendar.DAYS + 1;
			for (boolean isFoliage : new boolean[] {true, false}) {
				int vanilla = isFoliage ? foliage : grass;
				int a = SeasonPalette.colour(day, vanilla, isFoliage, 37, -81);
				int b = SeasonPalette.colour(next, vanilla, isFoliage, 37, -81);
				for (int shift = 0; shift <= 16; shift += 8) {
					int step = Math.abs((a >> shift & 0xFF) - (b >> shift & 0xFF));
					helper.assertTrue(step <= 4, "Colour jumps by " + step + " from day " + day + " to " + next);
				}
			}
		}
		equal(helper, SeasonPalette.colour(293, foliage, true, 5, 9), SeasonPalette.colour(293, foliage, true, 5, 9), "repeatable");
		// Autumn patches show more than one colour across a forest.
		boolean redder = false;
		boolean golder = false;
		for (int x = 0; x < 256; x += 4) {
			for (int z = 0; z < 256; z += 4) {
				int hue = SeasonPalette.autumnHue(x, z);
				redder |= (hue >> 8 & 0xFF) < 0x60;
				golder |= (hue >> 8 & 0xFF) > 0xA0;
			}
		}
		helper.assertTrue(redder && golder, "Autumn foliage has no red or no gold patches");
		helper.succeed();
	}

	@GameTest
	public void temperateBiomesHaveSeasons(GameTestHelper helper) {
		Registry<Biome> biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
		for (ResourceKey<Biome> biome : List.of(Biomes.PLAINS, Biomes.FOREST, Biomes.DARK_FOREST, Biomes.TAIGA, Biomes.MEADOW)) {
			helper.assertTrue(biomes.getOrThrow(biome).is(JugcraftSeasons.HAS_SEASONS), biome.identifier() + " has no seasons");
		}
		for (ResourceKey<Biome> biome : List.of(Biomes.DESERT, Biomes.JUNGLE, Biomes.SWAMP, Biomes.CHERRY_GROVE, Biomes.SNOWY_PLAINS)) {
			helper.assertTrue(!biomes.getOrThrow(biome).is(JugcraftSeasons.HAS_SEASONS), biome.identifier() + " has seasons");
		}
		helper.succeed();
	}

	private static void equal(GameTestHelper helper, Object actual, Object expected, String what) {
		helper.assertTrue(Objects.equals(actual, expected), what + ": expected " + expected + ", got " + actual);
	}
}
