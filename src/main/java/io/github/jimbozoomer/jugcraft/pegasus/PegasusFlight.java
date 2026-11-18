package io.github.jimbozoomer.jugcraft.pegasus;

/**
 * The Pegasus's flight rules as plain arithmetic (docs/features/pegasus.md): the server's flight state machine, the
 * pilot's bounded input, the speeds and their easing, the server's plausibility limit and the wing animation. Nothing
 * here touches Minecraft, so {@code src/test} checks it without a game ({@code PegasusFlightTests}), and the entity,
 * its client and its model all use the same numbers. Speeds are blocks a tick; angles radians; times ticks.
 *
 * <p>The takeoff trigger: on the ground, tapping or charging the jump key and releasing it within a second is the normal
 * vanilla horse jump. Holding Ascend (Space by default, the same key) for {@value #TAKEOFF_HOLD_TICKS} ticks takes off,
 * as does holding it for {@value #AIR_TAKEOFF_TICKS} ticks while already in the air (pressing it again during a jump or
 * a fall). In flight Ascend climbs, Descend (Left Ctrl by default) descends and lands, and neither holds level flight.
 */
public final class PegasusFlight {
	/** The server's flight state, synced to every observer as its ordinal. */
	public enum State {
		/** A normal horse: gravity, vanilla walking and jumping. */
		GROUNDED,
		/** Lifting off: a strong climb and the wings unfolding, for {@link #TAKEOFF_TICKS} ticks. */
		TAKEOFF,
		/** Powered flight under the pilot's control. */
		FLYING,
		/** Nobody in control (no rider, or the pilot's input went stale): a slow, safe glide down. */
		GLIDING;

		private static final State[] VALUES = values();

		/** The state for a synced or saved ordinal; anything out of range is {@link #GROUNDED}. */
		public static State of(int ordinal) {
			return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : GROUNDED;
		}

		/** Whether the pegasus flies itself in this state (no gravity, no fall damage). */
		public boolean airborne() {
			return this != GROUNDED;
		}
	}

	/** Input bits in {@link PegasusInputPayload#input()}; anything else in the value is malformed. */
	public static final int INPUT_ASCEND = 1;
	public static final int INPUT_DESCEND = 2;
	public static final int INPUT_MASK = INPUT_ASCEND | INPUT_DESCEND;

	/** The pilot's input counts for this long after its last accepted packet; then the pegasus glides. */
	public static final int INPUT_TIMEOUT_TICKS = 10;
	/** The client resends unchanged input this often, so the server knows the pilot is still there. */
	public static final int RESEND_TICKS = 5;
	/** Ascend held this long on the ground takes off (longer than a full vanilla jump charge, 10 ticks). */
	public static final int TAKEOFF_HOLD_TICKS = 20;
	/** Ascend held this long while already off the ground takes off. */
	public static final int AIR_TAKEOFF_TICKS = 3;
	/** How long the takeoff climb lasts before ordinary flight. */
	public static final int TAKEOFF_TICKS = 12;
	/** After flying, a landing within this many ticks takes no fall damage. */
	public static final int LANDING_GRACE_TICKS = 10;

	/** Top speeds: forward in flight and in a glide; sideways and backwards follow the horse's own input scaling. */
	public static final double FLY_SPEED = 0.6;
	public static final double GLIDE_SPEED = 0.45;
	/** Horizontal acceleration and deceleration a tick (at most), and how much of the remaining gap a tick closes. */
	public static final double H_ACCEL = 0.04;
	public static final double H_DECEL = 0.03;
	public static final double EASE = 0.25;
	/** Vertical speeds: climbing, the takeoff climb, descending, descending near the ground, and the glide's sink. */
	public static final double CLIMB = 0.30;
	public static final double TAKEOFF_CLIMB = 0.42;
	public static final double DESCEND = 0.35;
	public static final double LANDING_DESCEND = 0.15;
	public static final double GLIDE_SINK = 0.12;
	/** Vertical acceleration a tick (at most). */
	public static final double V_ACCEL = 0.05;
	/** Ground this close below slows a descent to {@link #LANDING_DESCEND}, in blocks. */
	public static final double GROUND_NEAR = 2.5;
	/** Horizontal speed kept a tick by a glide with nobody steering. */
	public static final double RIDERLESS_DRAG = 0.95;

	/** The server's own check: how far apart its looks are, and how much room it gives for lag. */
	public static final int CHECK_TICKS = 20;
	public static final double TOLERANCE = 3.0;
	/** A move this long in one server tick is a teleport (vanilla refuses client vehicle moves this long). */
	public static final double TELEPORT_STEP = 10.0;

	/** Wing animation: how fast the wings open and close, change stroke size and change beat rate, a tick. */
	public static final double WING_OPEN_RATE = 0.125;
	public static final double WING_AMP_RATE = 0.08;
	public static final double WING_RATE_RATE = 0.06;
	/** Grounded with a rider, the folded wings are lifted this far open, off the rider's legs. */
	public static final double WING_OPEN_RIDDEN = 0.25;
	/** Beat rates (radians of phase a tick) and stroke sizes (0..1) for each kind of flight. */
	public static final double RATE_TAKEOFF = 1.05;
	public static final double RATE_CLIMB = 0.95;
	public static final double RATE_LEVEL = 0.6;
	public static final double RATE_DESCEND = 0.3;
	public static final double RATE_GLIDE = 0.2;
	public static final double AMP_TAKEOFF = 1.0;
	public static final double AMP_CLIMB = 1.0;
	public static final double AMP_LEVEL = 0.6;
	public static final double AMP_DESCEND = 0.2;
	public static final double AMP_GLIDE = 0.12;

	private PegasusFlight() {
	}

	// ------------------------------------------------------------------------------------------------ input

	/** The input value for the pilot's two keys. */
	public static int encode(boolean ascend, boolean descend) {
		return (ascend ? INPUT_ASCEND : 0) | (descend ? INPUT_DESCEND : 0);
	}

	/** Whether a received input value is well formed: only the two key bits. */
	public static boolean valid(int input) {
		return (input & ~INPUT_MASK) == 0;
	}

	public static boolean ascend(int input) {
		return (input & INPUT_ASCEND) != 0;
	}

	public static boolean descend(int input) {
		return (input & INPUT_DESCEND) != 0;
	}

	/** The vertical intent of an input: 1 climb, -1 descend, 0 level (both keys cancel). */
	public static int vertical(int input) {
		return (ascend(input) ? 1 : 0) - (descend(input) ? 1 : 0);
	}

	/** Whether {@code sequence} comes after {@code last}, allowing for the counter wrapping round. */
	public static boolean newer(int sequence, int last) {
		return sequence - last > 0;
	}

	// ------------------------------------------------------------------------------------------------ the state machine

	/**
	 * What the server knows each tick. {@code piloted}: a player controls the pegasus (saddled, first passenger);
	 * {@code fresh}: that player's input arrived within {@link #INPUT_TIMEOUT_TICKS}; {@code ascend}/{@code descend}: its
	 * keys (false unless fresh); {@code ascendHeldTicks}: for how many ticks Ascend has been held; {@code blockedAbove}: no
	 * room to climb (a ceiling over the pegasus or its rider, or the build limit).
	 */
	public record Conditions(boolean piloted, boolean fresh, boolean ascend, boolean descend, int ascendHeldTicks,
			boolean onGround, boolean inLiquid, boolean blockedAbove) {
	}

	/** The next flight state. {@code ticksInState} counts the ticks already spent in {@code state}. */
	public static State next(State state, int ticksInState, Conditions c) {
		boolean controlled = c.piloted() && c.fresh();
		boolean landed = c.onGround() || c.inLiquid();
		return switch (state) {
			case GROUNDED -> controlled && c.ascend() && !c.inLiquid() && !c.blockedAbove()
					&& c.ascendHeldTicks() >= (c.onGround() ? TAKEOFF_HOLD_TICKS : AIR_TAKEOFF_TICKS) ? State.TAKEOFF : State.GROUNDED;
			// The server's copy leaves the ground only when the pilot's client has heard of the takeoff and climbed (a round
			// trip), so a takeoff is not judged by the ground until it becomes flight.
			case TAKEOFF -> !controlled ? State.GLIDING : c.inLiquid() ? State.GROUNDED
					: ticksInState >= TAKEOFF_TICKS ? State.FLYING : State.TAKEOFF;
			case FLYING -> landed && !(c.ascend() && controlled && !c.inLiquid()) ? State.GROUNDED
					: !controlled ? State.GLIDING : State.FLYING;
			case GLIDING -> landed ? State.GROUNDED : controlled ? State.FLYING : State.GLIDING;
		};
	}

	// ------------------------------------------------------------------------------------------------ motion

	/** The vertical speed flight aims for: climbing, holding level, descending (slower near the ground) or gliding. */
	public static double verticalTarget(State state, boolean ascend, boolean descend, boolean groundNear) {
		return switch (state) {
			case GROUNDED -> 0.0;
			case TAKEOFF -> TAKEOFF_CLIMB;
			case FLYING -> ascend && !descend ? CLIMB : descend && !ascend ? -(groundNear ? LANDING_DESCEND : DESCEND) : 0.0;
			case GLIDING -> -GLIDE_SINK;
		};
	}

	/**
	 * One tick of easing from {@code current} towards {@code target}: a share ({@link #EASE}) of the gap, never more
	 * than {@code maxStep}, so speeding up starts at a steady rate and arrival is gentle. Snaps on when within 1e-4.
	 */
	public static double ease(double current, double target, double maxStep) {
		double gap = target - current;
		if (Math.abs(gap) < 1.0E-4) {
			return target;
		}
		double step = Math.max(-maxStep, Math.min(maxStep, gap * EASE));
		if (Math.abs(step) < 1.0E-4) {
			step = Math.copySign(Math.min(Math.abs(gap), 1.0E-4), gap);
		}
		return current + step;
	}

	/**
	 * The world-space horizontal velocity aimed for, from the horse's ridden input (strafe, forward; vanilla scales them
	 * already) turned by the rider's yaw, as vanilla's {@code getInputVector} turns it. Returns {x, z}.
	 */
	public static double[] horizontalTarget(double strafe, double forward, float yawDegrees, double speed) {
		double length = Math.sqrt(strafe * strafe + forward * forward);
		if (length > 1.0) {
			strafe /= length;
			forward /= length;
		}
		double yaw = Math.toRadians(yawDegrees);
		double sin = Math.sin(yaw);
		double cos = Math.cos(yaw);
		return new double[] {(strafe * cos - forward * sin) * speed, (forward * cos + strafe * sin) * speed};
	}

	/**
	 * One tick of the horizontal velocity towards its target, as a vector: speeding up by at most {@code accel},
	 * slowing (the target shorter than the velocity) by at most {@code decel}, both eased. Returns {x, z}.
	 */
	public static double[] approach(double vx, double vz, double tx, double tz, double accel, double decel) {
		double gx = tx - vx;
		double gz = tz - vz;
		double gap = Math.sqrt(gx * gx + gz * gz);
		if (gap < 1.0E-4) {
			return new double[] {tx, tz};
		}
		double limit = tx * tx + tz * tz >= vx * vx + vz * vz ? accel : decel;
		double step = Math.min(limit, Math.max(gap * EASE, Math.min(gap, 1.0E-4)));
		return new double[] {vx + gx / gap * step, vz + gz / gap * step};
	}

	/** A climb no larger than the room left above (the build limit or a ceiling); descents pass unchanged. */
	public static double clampClimb(double vy, double headroom) {
		return vy > 0 ? Math.min(vy, Math.max(0.0, headroom)) : vy;
	}

	/**
	 * Whether a flying pegasus could have moved (dx, dy, dz) blocks in {@code ticks} ticks: no faster across than
	 * {@link #FLY_SPEED} and no faster up than {@link #TAKEOFF_CLIMB}, with {@link #TOLERANCE} times the room for lag.
	 * Descending is never refused (gravity and diving harm only the rider's own ride).
	 */
	public static boolean plausible(double dx, double dy, double dz, int ticks) {
		double across = Math.sqrt(dx * dx + dz * dz);
		return across <= FLY_SPEED * ticks * TOLERANCE + 1.0E-9 && dy <= TAKEOFF_CLIMB * ticks * TOLERANCE + 1.0E-9;
	}

	// ------------------------------------------------------------------------------------------------ the wings

	/** What the wings move towards: how open (0 folded, 1 spread), how big a stroke (0..1) and how fast a beat. */
	public record WingTargets(double open, double amp, double rate) {
	}

	/** The wing targets for a synced state, the synced vertical intent (-1, 0, 1) and whether it is ridden. */
	public static WingTargets wingTargets(State state, int vertical, boolean ridden) {
		return switch (state) {
			case GROUNDED -> new WingTargets(ridden ? WING_OPEN_RIDDEN : 0.0, 0.0, RATE_GLIDE);
			case TAKEOFF -> new WingTargets(1.0, AMP_TAKEOFF, RATE_TAKEOFF);
			case FLYING -> vertical > 0 ? new WingTargets(1.0, AMP_CLIMB, RATE_CLIMB)
					: vertical < 0 ? new WingTargets(1.0, AMP_DESCEND, RATE_DESCEND) : new WingTargets(1.0, AMP_LEVEL, RATE_LEVEL);
			case GLIDING -> new WingTargets(1.0, AMP_GLIDE, RATE_GLIDE);
		};
	}

	/** {@code current} moved towards {@code target} by at most {@code step}. */
	public static double toward(double current, double target, double step) {
		return current < target ? Math.min(target, current + step) : Math.max(target, current - step);
	}

	// Shoulder keyframes as (span, chord) directions in the body's frame (+x the horse's left, +y down, +z back);
	// tools/check_pegasus_model.py compares these with the preview script (wingpose.py).
	static final double[] FOLD_SPAN = {0.0, 0.14, 0.99};
	static final double[] FOLD_CHORD = {0.36, 0.93, 0.0};
	static final double[] RAISE_SPAN = {0.32, -0.80, 0.50};
	static final double[] RAISE_CHORD = {0.12, 0.30, 0.95};
	static final double[] SPREAD_EULER = {0.0, 0.0, -0.12};
	/** Elbow and hand keyframes (local Euler x, y, z): folded, raised, spread. */
	static final double[][] ELBOW = {{0.0, 0.08, 0.0}, {0.0, 0.10, 0.0}, {0.0, 0.0, 0.04}};
	static final double[][] HAND = {{0.0, 0.22, 0.0}, {0.0, 0.15, 0.0}, {0.0, 0.0, 0.06}};
	/** Stroke size about z at full amplitude, per bone; the elbow and hand lag behind; the shoulder sweeps fore and aft. */
	public static final double FLAP_SHOULDER = 0.95;
	public static final double FLAP_ELBOW = 0.40;
	public static final double FLAP_HAND = 0.55;
	static final double LAG_ELBOW = 0.7;
	static final double LAG_HAND = 1.3;
	static final double SWEEP = 0.18;

	private static final double[] Q_FOLD = quaternion(frame(FOLD_SPAN, FOLD_CHORD));
	private static final double[] Q_RAISE = quaternion(frame(RAISE_SPAN, RAISE_CHORD));
	private static final double[] Q_SPREAD = quaternion(euler(SPREAD_EULER[0], SPREAD_EULER[1], SPREAD_EULER[2]));

	/**
	 * The LEFT wing's pose: {shoulder x, y, z, elbow x, y, z, hand x, y, z}, radians, for ModelPart (which turns Z*Y*X).
	 * The right wing is its mirror image: (x, -y, -z). {@code open} 0 folds the wing along the flank, 0.5 raises it up
	 * and back, 1 spreads it; the shoulder moves between these by quaternion slerp, then beats about the body's z axis
	 * by {@code amp} (0..1) at {@code phase}, the elbow and hand following later in the beat. A wing beats only once
	 * more than half open.
	 */
	public static double[] wingPose(double open, double phase, double amp) {
		double o = clamp01(open);
		double[] q = o <= 0.5 ? slerp(Q_FOLD, Q_RAISE, o / 0.5) : slerp(Q_RAISE, Q_SPREAD, (o - 0.5) / 0.5);
		double flap = clamp01(amp) * clamp01((o - 0.5) / 0.5);
		double[] m = multiply(rotZ(flap * FLAP_SHOULDER * Math.sin(phase)), multiply(rotY(flap * SWEEP * Math.cos(phase)), matrix(q)));
		double[] shoulder = eulerOf(m);
		double[] elbow = keyframes(o, ELBOW);
		double[] hand = keyframes(o, HAND);
		return new double[] {shoulder[0], shoulder[1], shoulder[2],
				elbow[0], elbow[1], elbow[2] + flap * FLAP_ELBOW * Math.sin(phase - LAG_ELBOW),
				hand[0], hand[1], hand[2] + flap * FLAP_HAND * Math.sin(phase - LAG_HAND)};
	}

	static double clamp01(double v) {
		return Math.max(0.0, Math.min(1.0, v));
	}

	private static double[] keyframes(double open, double[][] frames) {
		double[] a = open <= 0.5 ? frames[0] : frames[1];
		double[] b = open <= 0.5 ? frames[1] : frames[2];
		double t = open <= 0.5 ? open / 0.5 : (open - 0.5) / 0.5;
		return new double[] {a[0] + t * (b[0] - a[0]), a[1] + t * (b[1] - a[1]), a[2] + t * (b[2] - a[2])};
	}

	// 3x3 matrices row-major in double[9]; quaternions (w, x, y, z).
	static double[] rotX(double a) {
		double c = Math.cos(a), s = Math.sin(a);
		return new double[] {1, 0, 0, 0, c, -s, 0, s, c};
	}

	static double[] rotY(double a) {
		double c = Math.cos(a), s = Math.sin(a);
		return new double[] {c, 0, s, 0, 1, 0, -s, 0, c};
	}

	static double[] rotZ(double a) {
		double c = Math.cos(a), s = Math.sin(a);
		return new double[] {c, -s, 0, s, c, 0, 0, 0, 1};
	}

	static double[] multiply(double[] a, double[] b) {
		double[] out = new double[9];
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				out[r * 3 + c] = a[r * 3] * b[c] + a[r * 3 + 1] * b[3 + c] + a[r * 3 + 2] * b[6 + c];
			}
		}
		return out;
	}

	/** The rotation ModelPart applies for (xRot, yRot, zRot): Z * Y * X. */
	static double[] euler(double x, double y, double z) {
		return multiply(rotZ(z), multiply(rotY(y), rotX(x)));
	}

	/** The (xRot, yRot, zRot) that {@link #euler} turns back into {@code m}. */
	static double[] eulerOf(double[] m) {
		double sy = Math.max(-1.0, Math.min(1.0, -m[6]));
		double y = Math.asin(sy);
		if (Math.abs(sy) < 0.999999) {
			return new double[] {Math.atan2(m[7], m[8]), y, Math.atan2(m[3], m[0])};
		}
		return new double[] {Math.atan2(-m[5], m[4]), y, 0.0};
	}

	/** The rotation taking +x to {@code span} and +z to {@code chord} (made square to the span). */
	static double[] frame(double[] span, double[] chord) {
		double[] s = normalized(span);
		double d = chord[0] * s[0] + chord[1] * s[1] + chord[2] * s[2];
		double[] c = normalized(new double[] {chord[0] - d * s[0], chord[1] - d * s[1], chord[2] - d * s[2]});
		double[] y = {c[1] * s[2] - c[2] * s[1], c[2] * s[0] - c[0] * s[2], c[0] * s[1] - c[1] * s[0]};
		return new double[] {s[0], y[0], c[0], s[1], y[1], c[1], s[2], y[2], c[2]};
	}

	private static double[] normalized(double[] v) {
		double l = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
		return new double[] {v[0] / l, v[1] / l, v[2] / l};
	}

	static double[] quaternion(double[] m) {
		double t = m[0] + m[4] + m[8];
		double r;
		if (t > 0) {
			r = Math.sqrt(1 + t) * 2;
			return new double[] {0.25 * r, (m[7] - m[5]) / r, (m[2] - m[6]) / r, (m[3] - m[1]) / r};
		}
		if (m[0] > m[4] && m[0] > m[8]) {
			r = Math.sqrt(1 + m[0] - m[4] - m[8]) * 2;
			return new double[] {(m[7] - m[5]) / r, 0.25 * r, (m[1] + m[3]) / r, (m[2] + m[6]) / r};
		}
		if (m[4] > m[8]) {
			r = Math.sqrt(1 + m[4] - m[0] - m[8]) * 2;
			return new double[] {(m[2] - m[6]) / r, (m[1] + m[3]) / r, 0.25 * r, (m[5] + m[7]) / r};
		}
		r = Math.sqrt(1 + m[8] - m[0] - m[4]) * 2;
		return new double[] {(m[3] - m[1]) / r, (m[2] + m[6]) / r, (m[5] + m[7]) / r, 0.25 * r};
	}

	static double[] matrix(double[] q) {
		double n = Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
		double w = q[0] / n, x = q[1] / n, y = q[2] / n, z = q[3] / n;
		return new double[] {1 - 2 * (y * y + z * z), 2 * (x * y - z * w), 2 * (x * z + y * w),
				2 * (x * y + z * w), 1 - 2 * (x * x + z * z), 2 * (y * z - x * w),
				2 * (x * z - y * w), 2 * (y * z + x * w), 1 - 2 * (x * x + y * y)};
	}

	static double[] slerp(double[] a, double[] b, double t) {
		double d = a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3];
		double[] e = b;
		if (d < 0) {
			e = new double[] {-b[0], -b[1], -b[2], -b[3]};
			d = -d;
		}
		double[] out = new double[4];
		if (d > 0.9995) {
			double n = 0;
			for (int i = 0; i < 4; i++) {
				out[i] = a[i] + t * (e[i] - a[i]);
				n += out[i] * out[i];
			}
			n = Math.sqrt(n);
			for (int i = 0; i < 4; i++) {
				out[i] /= n;
			}
			return out;
		}
		double theta = Math.acos(Math.min(1.0, d));
		double sa = Math.sin((1 - t) * theta) / Math.sin(theta);
		double sb = Math.sin(t * theta) / Math.sin(theta);
		for (int i = 0; i < 4; i++) {
			out[i] = sa * a[i] + sb * e[i];
		}
		return out;
	}
}
