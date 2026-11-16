package io.github.jimbozoomer.jugcraft.menagerie;

/**
 * How a pirate ship moves on the water beyond its course (docs/features/pirate-ships.md): the swell's roll, pitch and
 * heave, the heel in a turn and before the wind, the wind itself, how far the yards are braced round to it and how the
 * sails are set or furled. Pure arithmetic with no game types, shared by the server (synced brace and sails, the crew's
 * seats) and the client renderer, so the crew stand on the deck the renderer draws.
 *
 * <p><b>One phase for both sides.</b> The swell is a function of a phase, not of an entity's age: {@link #phase} is the
 * world's game time plus a fixed offset from the ship's UUID (saved with it and known to every client), wrapped to
 * {@value #PERIOD} ticks, plus the partial tick on the client. Every swell term completes a whole number of cycles in
 * {@value #PERIOD} ticks, so the wrap is seamless, and the phase stays small, so it never loses float precision over a
 * long uptime. The server's crew seats and the client's drawing therefore agree, however late a client began tracking
 * the ship.
 *
 * <p><b>Bounded.</b> There is no sinking list (Codex's local review removed it: it carried the hull outside the water
 * and protection it is checked in). The most the hull is turned is the swell's roll plus {@link #HEEL_MAX} and the swell's
 * pitch, the most it is lifted or lowered {@link #HEAVE}; with those the drawn keel stays within
 * {@code PirateWaters.KEEL} (1.75 blocks) of the waterline and the hull inside the water it is checked in (pirate logic
 * and art tests).
 *
 * <p>Frame as {@link CrewedVessel}: ship-local blocks, x to port, y up from the waterline, z to the bow. Angles are in
 * degrees. A roll is a turn about the ship's z axis as {@code PoseStack.rotateDegrees(Axis.ZP, roll)} turns it (a
 * negative roll lowers the port side, +x); a pitch is a turn about x as {@code Axis.XP} (a positive pitch lowers the
 * bow); the renderer's pose applies the heave, the yaw, the roll and the pitch in that order ({@link #lean}).
 */
public final class PirateMotion {
	/**
	 * The swell: the most the hull rolls and pitches (degrees) and heaves (blocks, a fifth of a model pixel). Small on
	 * purpose: the walkable deck squares stay level, so the drawn deck may part from them by no more than
	 * {@link #DECK_CAP} at any deck corner in any pose (Codex's review of checkpoint 2: boarders must not float over the
	 * drawn deck or sink into it). The visible motion is in the sails, which sway with the headway, the yards, braced to
	 * the wind, the flag and the helm.
	 */
	public static final float ROLL = 0.5F;
	public static final float PITCH = 0.2F;
	public static final double HEAVE = 0.0125;
	/** The most the drawn deck may part from the level walkable deck, anywhere over the deck squares (blocks, 1.5 pixels). */
	public static final double DECK_CAP = 1.5 / 16.0;
	/** The swell's period in ticks: every term completes a whole number of cycles in it (two minutes). */
	public static final int PERIOD = 2_400;
	/** Cycles per period of the roll, the pitch and the heave (about 126, 171 and 150 ticks each). */
	static final int ROLL_CYCLES = 19;
	static final int PITCH_CYCLES = 14;
	static final int HEAVE_CYCLES = 16;
	/** The heel (degrees) at full rudder and headway, outward from the turn; and before a beam wind at full headway. */
	public static final float HEEL_TURN = 0.3F;
	public static final float HEEL_WIND = 0.25F;
	/** The most the hull heels, all told (degrees): with the swell, within {@link #DECK_CAP} at the deck's corners. */
	public static final float HEEL_MAX = 0.4F;
	/** How far the yards may be braced round from square (degrees): sharp up, close-hauled. */
	public static final float BRACE_MAX = 40.0F;
	/** The relative wind (degrees off the bow) at and inside which the yards are braced sharp up. */
	public static final float SHARP_UP = 60.0F;
	/** Ticks in a day, and the ticks at the day's end over which the wind backs or veers to the next day's. */
	public static final long DAY = 24_000L;
	public static final long SHIFT = 600L;
	/** How fast the sails are set or furled: the share of the way each tick (so 40 ticks from furled to set). */
	public static final float SAIL_RATE = 0.025F;

	private PirateMotion() {
	}

	/**
	 * The swell's phase (ticks, 0 to {@value #PERIOD}, and the partial tick): the game time plus the ship's
	 * {@link #offset}, wrapped to the period. The server passes a partial tick of 0; the client its frame's.
	 */
	public static float phase(long gameTime, long offset, float partialTick) {
		return Math.floorMod(gameTime + offset, (long) PERIOD) + partialTick;
	}

	/** A ship's fixed phase offset (0 to {@value #PERIOD}), from its UUID's two halves: ships do not roll in step. */
	public static long offset(long mostSignificant, long leastSignificant) {
		long h = (mostSignificant ^ leastSignificant) * 0x9E3779B97F4A7C15L;
		return Math.floorMod(h ^ (h >>> 31), (long) PERIOD);
	}

	private static double wave(float phase, int cycles, double shift) {
		return Math.sin(2.0 * Math.PI * cycles * phase / PERIOD + shift);
	}

	/** The swell's roll at {@code phase}, before heeling. */
	public static float swellRoll(float phase) {
		return (float) (wave(phase, ROLL_CYCLES, 0.0) * ROLL);
	}

	/** The swell's pitch at {@code phase}. */
	public static float pitch(float phase) {
		return (float) (wave(phase, PITCH_CYCLES, 1.0) * PITCH);
	}

	/** The swell's heave at {@code phase} (blocks, up positive). */
	public static double heave(float phase) {
		return wave(phase, HEAVE_CYCLES, 2.0) * HEAVE;
	}

	/**
	 * The heel: outward from a turn (a starboard turn, positive {@code rudder}, heels her to port, a negative roll) and
	 * to leeward of the wind ({@code relativeWind} positive is a wind from starboard, which heels her to port), both in
	 * proportion to {@code headway} (0 to 1), bounded by {@link #HEEL_MAX}.
	 */
	public static float heel(float rudder, float headway, float relativeWind) {
		double turn = -HEEL_TURN * clamp(rudder, -1.0F, 1.0F) * clamp(headway, 0.0F, 1.0F);
		double wind = -HEEL_WIND * clamp(headway, 0.0F, 1.0F) * Math.sin(Math.toRadians(relativeWind));
		return clamp((float) (turn + wind), -HEEL_MAX, HEEL_MAX);
	}

	/** The whole roll: the swell's and the heel. Never more than {@link #ROLL} + {@link #HEEL_MAX}; no sinking list. */
	public static float roll(float phase, float rudder, float headway, float relativeWind) {
		return swellRoll(phase) + heel(rudder, headway, relativeWind);
	}

	/**
	 * The wind on a game day, as the yaw it blows from (0 to 360; a ship facing that yaw heads straight into it): one of
	 * sixteen points fixed by the day's number, the same for every ship and both sides, shifting to the next
	 * day's over the last {@value #SHIFT} ticks of each day, round the shorter way.
	 */
	public static float wind(long gameTime) {
		long day = Math.floorDiv(gameTime, DAY);
		long into = Math.floorMod(gameTime, DAY);
		float today = point(day);
		if (into < DAY - SHIFT) {
			return today;
		}
		float t = (into - (DAY - SHIFT)) / (float) SHIFT;
		t = t * t * (3.0F - 2.0F * t);
		return wrap360(today + wrap(point(day + 1) - today) * t);
	}

	/** One of sixteen compass points for a day (a fixed scramble of the day's number). */
	static float point(long day) {
		long h = day * 0x9E3779B97F4A7C15L;
		h ^= h >>> 29;
		h *= 0xBF58476D1CE4E5B9L;
		h ^= h >>> 32;
		return (int) Math.floorMod(h, 16L) * 22.5F;
	}

	/** Where the wind comes from, relative to the bow: positive from starboard, negative from port, ±180 dead astern. */
	public static float relativeWind(float windFrom, float shipYaw) {
		return wrap(windFrom - shipYaw);
	}

	/**
	 * How far the yards are braced round, about each mast (a turn as {@code Axis.YP}): square before the wind, braced
	 * round further as the wind draws forward, sharp up ({@link #BRACE_MAX}) at {@link #SHARP_UP} degrees off the bow and
	 * closer. A wind from starboard braces them positive (the starboard yardarms forward, the lee yardarms aft), so the
	 * wind always fills the after face of the sails.
	 */
	public static float brace(float relativeWind) {
		float off = Math.abs(wrap(relativeWind));
		float share = clamp((180.0F - off) / (180.0F - SHARP_UP), 0.0F, 1.0F);
		// Dead ahead (0) counts as from starboard: braced sharp up one way or the other, never left square.
		return (wrap(relativeWind) < 0.0F ? -1.0F : 1.0F) * BRACE_MAX * share;
	}

	/**
	 * Whether the sails should be set: only while she makes way under a helmsman (the crew furl them when they heave to,
	 * when she is beaten or sinking, or when nobody holds the helm).
	 */
	public static boolean sailsSet(boolean helmed, boolean heaveTo, boolean beaten, boolean sunk) {
		return helmed && !heaveTo && !beaten && !sunk;
	}

	/** One tick of setting or furling: {@code current} (0 furled to 1 set) moved {@link #SAIL_RATE} toward the target. */
	public static float sailStep(float current, boolean set) {
		// Snapped at the ends, so float steps land exactly on furled and set.
		float next = set ? current + SAIL_RATE : current - SAIL_RATE;
		return next >= 1.0F - SAIL_RATE / 4.0F ? 1.0F : next <= SAIL_RATE / 4.0F ? 0.0F : next;
	}

	/**
	 * A ship-local point after the swell and heel, still in the ship's frame: pitched about x, then rolled about z, then
	 * heaved, exactly as the renderer's pose turns the hull ({@code translate(0, heave, 0)}, the yaw, {@code Axis.ZP} by
	 * {@code roll}, {@code Axis.XP} by {@code pitch}; the yaw is the caller's). So a crew member's seat is where the drawn
	 * deck is. Returns {x, y, z}.
	 */
	public static double[] lean(double x, double y, double z, float roll, float pitch, double heave) {
		double p = Math.toRadians(pitch), r = Math.toRadians(roll);
		double y1 = y * Math.cos(p) - z * Math.sin(p);
		double z1 = y * Math.sin(p) + z * Math.cos(p);
		double x2 = x * Math.cos(r) - y1 * Math.sin(r);
		double y2 = x * Math.sin(r) + y1 * Math.cos(r);
		return new double[] {x2, y2 + heave, z1};
	}

	/**
	 * A point turned about the vertical by {@code degrees}, as {@code Axis.YP} turns it (and {@code Vec3.yRot} in
	 * radians): x' = x cos + z sin, z' = z cos - x sin. Returns {x, z}.
	 */
	public static double[] turnY(double x, double z, float degrees) {
		double a = Math.toRadians(degrees);
		return new double[] {x * Math.cos(a) + z * Math.sin(a), z * Math.cos(a) - x * Math.sin(a)};
	}

	/** Degrees wrapped to -180 (exclusive) .. 180. */
	public static float wrap(float degrees) {
		float d = degrees % 360.0F;
		if (d >= 180.0F) {
			d -= 360.0F;
		}
		if (d < -180.0F) {
			d += 360.0F;
		}
		return d;
	}

	static float wrap360(float degrees) {
		float d = degrees % 360.0F;
		return d < 0.0F ? d + 360.0F : d;
	}

	static float clamp(float v, float lo, float hi) {
		return v < lo ? lo : v > hi ? hi : v;
	}
}
