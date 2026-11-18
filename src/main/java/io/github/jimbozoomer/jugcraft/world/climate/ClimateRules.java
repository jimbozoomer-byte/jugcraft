package io.github.jimbozoomer.jugcraft.world.climate;

import java.util.ArrayList;
import java.util.List;

/**
 * The eleven validation rules of a climate profile, exactly as contract amendment 4 states them, so the Java codec
 * and the World Designer's JavaScript validator accept the same profiles. Pure: no Minecraft types. All arithmetic is
 * on integers (64-bit here, so out-of-range inputs cannot overflow), with no rounding; {@code T} is the transition.
 *
 * <ol>
 * <li>{@code schema == 1}.</li>
 * <li>{@code anchor.mode == "fixed"}.</li>
 * <li>{@code |anchor.x|, |anchor.z| <= 30,000,000}.</li>
 * <li>{@code north_frigid}, {@code south_hot_start}, {@code south_hot_end} and {@code south_frigid} are in
 * {@code [1, 1,000,000]}.</li>
 * <li>{@code T} is in {@code [0, 1024]}.</li>
 * <li>{@code salt} is a 32-bit int, optional, default 0 (a type rule: any int passes).</li>
 * <li>{@code north_frigid > T}.</li>
 * <li>{@code south_hot_start < south_hot_end < south_frigid}.</li>
 * <li>{@code 2*south_hot_start > T}.</li>
 * <li>{@code south_hot_end - south_hot_start >= T} (centred blends may touch, never overlap).</li>
 * <li>{@code 2*(south_frigid - south_hot_end) >= 3*T}.</li>
 * </ol>
 *
 * Non-integer numbers are refused by the codec's integer reader before these rules run ({@code ClimateProfile}).
 */
public final class ClimateRules {
	public static final int SCHEMA = 1;
	public static final int MAX_DISTANCE = 1_000_000;
	public static final int MAX_TRANSITION = 1024;

	public static final int DEFAULT_NORTH_FRIGID = 2500;
	public static final int DEFAULT_SOUTH_HOT_START = 2000;
	public static final int DEFAULT_SOUTH_HOT_END = 4000;
	public static final int DEFAULT_SOUTH_FRIGID = 5500;
	public static final int DEFAULT_TRANSITION = 256;
	public static final int DEFAULT_SALT = 0;

	private ClimateRules() {
	}

	/**
	 * Every rule the given raw values break, as messages that start with the rule's number; empty when the profile is
	 * valid. Rules 1 to 5 and 7 to 11 are all checked, so a profile with several faults reports each of them.
	 */
	public static List<String> problems(long schema, String mode, long anchorX, long anchorZ, long northFrigid,
			long southHotStart, long southHotEnd, long southFrigid, long transition) {
		List<String> out = new ArrayList<>();
		if (schema != SCHEMA) {
			out.add("rule 1: schema must be " + SCHEMA + ", not " + schema);
		}
		if (!ClimateAnchor.FIXED.equals(mode)) {
			out.add("rule 2: anchor.mode must be \"" + ClimateAnchor.FIXED + "\", not \"" + mode + "\""
					+ ("climate_spawn".equals(mode) ? " (climate_spawn was removed: a world needs an authored, frozen anchor)" : ""));
		}
		if (Math.abs(anchorX) > ClimateAnchor.LIMIT || Math.abs(anchorZ) > ClimateAnchor.LIMIT) {
			out.add("rule 3: anchor x and z must lie within +/-" + ClimateAnchor.LIMIT + ", not " + anchorX + ", " + anchorZ);
		}
		distance(out, "north_frigid", northFrigid);
		distance(out, "south_hot_start", southHotStart);
		distance(out, "south_hot_end", southHotEnd);
		distance(out, "south_frigid", southFrigid);
		if (transition < 0 || transition > MAX_TRANSITION) {
			out.add("rule 5: transition must be in [0, " + MAX_TRANSITION + "], not " + transition);
		}
		if (northFrigid <= transition) {
			out.add("rule 7: north_frigid (" + northFrigid + ") must exceed transition (" + transition + ")");
		}
		if (!(southHotStart < southHotEnd && southHotEnd < southFrigid)) {
			out.add("rule 8: need south_hot_start < south_hot_end < south_frigid, not " + southHotStart + ", " + southHotEnd
					+ ", " + southFrigid);
		}
		if (2 * southHotStart <= transition) {
			out.add("rule 9: 2*south_hot_start (" + 2 * southHotStart + ") must exceed transition (" + transition
					+ "), so the hot blend stays south of the anchor");
		}
		if (southHotEnd - southHotStart < transition) {
			out.add("rule 10: south_hot_end - south_hot_start (" + (southHotEnd - southHotStart)
					+ ") must be at least transition (" + transition + "), so the two centred blends cannot overlap");
		}
		if (2 * (southFrigid - southHotEnd) < 3 * transition) {
			out.add("rule 11: 2*(south_frigid - south_hot_end) (" + 2 * (southFrigid - southHotEnd)
					+ ") must be at least 3*transition (" + 3 * transition + "), so the hot-end blend cannot reach the frigid blend");
		}
		return List.copyOf(out);
	}

	private static void distance(List<String> out, String name, long value) {
		if (value < 1 || value > MAX_DISTANCE) {
			out.add("rule 4: " + name + " must be in [1, " + MAX_DISTANCE + "], not " + value);
		}
	}
}
