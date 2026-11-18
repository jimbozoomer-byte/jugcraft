package io.github.jimbozoomer.jugcraft.world.climate;

import java.util.List;

/**
 * The band geometry of a climate profile: its anchor, thresholds (blocks from the anchor), transition width and salt.
 * Pure: no Minecraft types, so the belts can be tested and reasoned about without the game. Always valid: the
 * constructor applies {@link ClimateRules} and refuses a geometry that breaks any rule.
 */
public record ClimateGeometry(ClimateAnchor anchor, int northFrigid, int southHotStart, int southHotEnd, int southFrigid,
		int transition, int salt) {
	public ClimateGeometry {
		if (anchor == null) {
			throw new IllegalArgumentException("A climate geometry needs an anchor");
		}
		List<String> problems = ClimateRules.problems(ClimateRules.SCHEMA, anchor.mode(), anchor.x(), anchor.z(), northFrigid,
				southHotStart, southHotEnd, southFrigid, transition);
		if (!problems.isEmpty()) {
			throw new IllegalArgumentException("Invalid climate geometry: " + String.join("; ", problems));
		}
	}

	/** The owner's default belts (2500 north; 2000, 4000 and 5500 south; 256-block transitions), salt 0. */
	public static ClimateGeometry defaults(ClimateAnchor anchor) {
		return new ClimateGeometry(anchor, ClimateRules.DEFAULT_NORTH_FRIGID, ClimateRules.DEFAULT_SOUTH_HOT_START,
				ClimateRules.DEFAULT_SOUTH_HOT_END, ClimateRules.DEFAULT_SOUTH_FRIGID, ClimateRules.DEFAULT_TRANSITION,
				ClimateRules.DEFAULT_SALT);
	}

	/** This geometry with another salt (which only changes the blend and in-pool patterns). */
	public ClimateGeometry withSalt(int newSalt) {
		return new ClimateGeometry(anchor, northFrigid, southHotStart, southHotEnd, southFrigid, transition, newSalt);
	}
}
