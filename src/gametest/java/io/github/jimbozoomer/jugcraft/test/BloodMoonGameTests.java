package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.BloodMoon;
import io.github.jimbozoomer.jugcraft.agriculture.CostumedMobs;
import io.github.jimbozoomer.jugcraft.agriculture.HalloweenSeason;
import io.github.jimbozoomer.jugcraft.agriculture.HarvestMoon;
import io.github.jimbozoomer.jugcraft.agriculture.TrickOrTreat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import org.joml.Vector3f;

/**
 * Game tests for the Blood Moon (docs/features/blood-moon.md): on which nights of the season it rises; that under it
 * more mobs put on costumes; and that the sky it turns is redder as night deepens and untouched by day. The season and
 * the moon are set and put back within the tick, so no other test sees them.
 */
public class BloodMoonGameTests {
	private static final long MIDNIGHT = 18000;
	private static final long NOON = 6000;

	/** The season on a fixed server clock reading 20:00 UTC on {@code date} (yyyy-MM-dd). */
	private static void onDate(String date) {
		HalloweenSeason.setMode(HalloweenSeason.Mode.AUTO);
		HalloweenSeason.setWindow(HalloweenSeason.DEFAULT_START, HalloweenSeason.DEFAULT_END, ZoneOffset.UTC,
				Clock.fixed(Instant.parse(date + "T20:00:00Z"), ZoneOffset.UTC));
	}

	/** Every fourth night of the season and Halloween's, at night only; never out of season. */
	@GameTest
	public void itRisesEveryFourthNightOfTheSeasonAndOnHalloween(GameTestHelper helper) {
		long day = TrickOrTreat.DAY;
		try {
			onDate("2026-10-27");
			helper.assertTrue(BloodMoon.rising(BloodMoon.EVERY * 3 * day + MIDNIGHT), "On every fourth night of the season it rises");
			helper.assertFalse(BloodMoon.rising((BloodMoon.EVERY * 3 + 1) * day + MIDNIGHT), "not on the night after");
			helper.assertFalse(BloodMoon.rising(BloodMoon.EVERY * 3 * day + NOON), "and never by day");
			helper.assertTrue(BloodMoon.rising(BloodMoon.EVERY * 3 * day + HarvestMoon.DUSK)
					&& !BloodMoon.rising(BloodMoon.EVERY * 3 * day + HarvestMoon.DAWN), "It rises at dusk and sets at dawn");
			onDate(HalloweenSeason.DEFAULT_HARVEST_MOON.atYear(2026).toString());
			helper.assertTrue(BloodMoon.rising((BloodMoon.EVERY * 3 + 1) * day + MIDNIGHT), "On Halloween it rises whatever the night");
			onDate("2026-12-01");
			helper.assertFalse(BloodMoon.rising(BloodMoon.EVERY * 3 * day + MIDNIGHT), "Out of season it never does");
		} finally {
			HalloweenSeason.reset();
		}
		helper.succeed();
	}

	/** Under it, {@value BloodMoon#COSTUME_CHANCE} of mobs dress up instead of {@value CostumedMobs#CHANCE}. */
	@GameTest
	public void underItMoreMobsPutOnCostumes(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		try {
			BloodMoon.force(true);
			BloodMoon.update(server);
			helper.assertTrue(BloodMoon.active() && CostumedMobs.chance() == BloodMoon.COSTUME_CHANCE, "Under it, more dress up");
			BloodMoon.force(false);
			BloodMoon.update(server);
			helper.assertTrue(!BloodMoon.active() && CostumedMobs.chance() == CostumedMobs.CHANCE, "Without it, as ever");
		} finally {
			BloodMoon.force(null);
			BloodMoon.update(server);
		}
		helper.succeed();
	}

	/** The sky it turns is red at night, redder than it is blue or green, and as it was by day. */
	@GameTest
	public void itTurnsTheNightSkyRed(GameTestHelper helper) {
		Vector3f night = BloodMoon.tint(0.02F, 0.03F, 0.08F, 1.0F);
		helper.assertTrue(night.x() > night.y() * 3.0F && night.x() > night.z() * 3.0F, "At night the sky is blood red: " + night);
		Vector3f day = BloodMoon.tint(0.5F, 0.7F, 1.0F, 0.0F);
		helper.assertTrue(day.x() == 0.5F && day.y() == 0.7F && day.z() == 1.0F, "By day it is untouched: " + day);
		helper.succeed();
	}
}
