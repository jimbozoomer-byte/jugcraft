package io.github.jimbozoomer.jugcraft.pegasus;

import io.github.jimbozoomer.jugcraft.pegasus.PegasusFlight.Conditions;
import io.github.jimbozoomer.jugcraft.pegasus.PegasusFlight.State;

/**
 * Deterministic executable tests of the Pegasus's flight arithmetic (PegasusFlight): input bits, the server's state
 * machine, the speed curves, the plausibility limit and the wing pose. No game, network or clock. Gradle task
 * {@code pegasusFlightTest} (part of {@code check}).
 */
public final class PegasusFlightTests {
	private static int checks;

	private static void check(boolean yes, String why) {
		checks++;
		if (!yes) {
			throw new AssertionError(why);
		}
	}

	private static Conditions on(boolean ascend, boolean descend, int held) {
		return new Conditions(true, true, ascend, descend, held, true, false, false);
	}

	private static Conditions air(boolean ascend, boolean descend, int held) {
		return new Conditions(true, true, ascend, descend, held, false, false, false);
	}

	public static void main(String[] args) {
		input();
		takeoff();
		flightAndLanding();
		staleAndRiderless();
		ceilings();
		curves();
		plausibility();
		wings();
		System.out.println("Pegasus flight: " + checks + " assertions passed");
	}

	private static void input() {
		check(PegasusFlight.encode(false, false) == 0 && PegasusFlight.encode(true, false) == 1
				&& PegasusFlight.encode(false, true) == 2 && PegasusFlight.encode(true, true) == 3, "Two key bits");
		for (int i = 0; i <= 3; i++) {
			check(PegasusFlight.valid(i), "Inputs 0..3 are valid: " + i);
		}
		for (int bad : new int[] {4, 7, 8, 255, -1, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
			check(!PegasusFlight.valid(bad), "Out-of-range input is malformed: " + bad);
		}
		check(PegasusFlight.vertical(1) == 1 && PegasusFlight.vertical(2) == -1 && PegasusFlight.vertical(3) == 0
				&& PegasusFlight.vertical(0) == 0, "Ascend climbs, Descend sinks, both cancel");
		check(PegasusFlight.newer(5, 4) && !PegasusFlight.newer(4, 4) && !PegasusFlight.newer(3, 4), "Sequences only move on");
		check(PegasusFlight.newer(Integer.MIN_VALUE, Integer.MAX_VALUE), "The sequence wraps round");
		check(State.of(-1) == State.GROUNDED && State.of(99) == State.GROUNDED && State.of(2) == State.FLYING, "Bad ordinals ground");
	}

	private static void takeoff() {
		State s = State.GROUNDED;
		for (int held = 1; held < PegasusFlight.TAKEOFF_HOLD_TICKS; held++) {
			check(PegasusFlight.next(s, held, on(true, false, held)) == State.GROUNDED, "A short hold on the ground is a jump, not a takeoff: " + held);
		}
		check(PegasusFlight.next(s, 0, on(true, false, PegasusFlight.TAKEOFF_HOLD_TICKS)) == State.TAKEOFF, "A long hold takes off");
		check(PegasusFlight.next(s, 0, air(true, false, PegasusFlight.AIR_TAKEOFF_TICKS - 1)) == State.GROUNDED, "Not quite in the air");
		check(PegasusFlight.next(s, 0, air(true, false, PegasusFlight.AIR_TAKEOFF_TICKS)) == State.TAKEOFF, "Holding Ascend mid-jump takes off");
		check(PegasusFlight.next(s, 0, new Conditions(false, true, true, false, 99, true, false, false)) == State.GROUNDED, "Nobody piloting: no takeoff");
		check(PegasusFlight.next(s, 0, new Conditions(true, false, true, false, 99, true, false, false)) == State.GROUNDED, "Stale input: no takeoff");
		check(PegasusFlight.next(s, 0, new Conditions(true, true, true, false, 99, false, true, false)) == State.GROUNDED, "No takeoff from water");
		check(PegasusFlight.next(s, 0, new Conditions(true, true, true, false, 99, true, false, true)) == State.GROUNDED, "No takeoff under a ceiling");
		State t = State.TAKEOFF;
		check(PegasusFlight.next(t, PegasusFlight.TAKEOFF_TICKS - 1, air(false, false, 0)) == State.TAKEOFF, "The takeoff runs its course");
		check(PegasusFlight.next(t, PegasusFlight.TAKEOFF_TICKS, air(false, false, 0)) == State.FLYING, "then flies");
		check(PegasusFlight.next(t, 1, on(true, false, 30)) == State.TAKEOFF, "A lagging client's takeoff is not cut short on the ground");
		check(PegasusFlight.next(State.FLYING, 0, on(false, false, 0)) == State.GROUNDED, "A takeoff that never left the ground lands as flight begins");
		check(PegasusFlight.next(t, 1, new Conditions(true, true, true, false, 30, false, true, false)) == State.GROUNDED, "Water stops a takeoff");
		check(PegasusFlight.verticalTarget(State.TAKEOFF, false, false, false) == PegasusFlight.TAKEOFF_CLIMB, "Takeoff climbs hard");
	}

	private static void flightAndLanding() {
		State f = State.FLYING;
		check(PegasusFlight.next(f, 5, air(true, false, 9)) == State.FLYING && PegasusFlight.next(f, 5, air(false, false, 0)) == State.FLYING
				&& PegasusFlight.next(f, 5, air(false, true, 0)) == State.FLYING, "Flight holds in the air under control");
		check(PegasusFlight.next(f, 5, on(false, true, 0)) == State.GROUNDED, "A descent onto the ground lands");
		check(PegasusFlight.next(f, 5, on(false, false, 0)) == State.GROUNDED, "Touching down level lands");
		check(PegasusFlight.next(f, 5, on(true, false, 4)) == State.FLYING, "Climbing off the ground keeps flying");
		check(PegasusFlight.next(f, 5, new Conditions(true, true, true, false, 4, false, true, false)) == State.GROUNDED, "Water ends flight");
		check(PegasusFlight.verticalTarget(f, true, false, false) == PegasusFlight.CLIMB, "Ascend climbs");
		check(PegasusFlight.verticalTarget(f, false, false, false) == 0.0, "Releasing both holds level");
		check(PegasusFlight.verticalTarget(f, true, true, false) == 0.0, "Both keys hold level");
		check(PegasusFlight.verticalTarget(f, false, true, false) == -PegasusFlight.DESCEND, "Descend sinks");
		check(PegasusFlight.verticalTarget(f, false, true, true) == -PegasusFlight.LANDING_DESCEND, "Near the ground it slows to land");
		check(PegasusFlight.LANDING_DESCEND < 0.5 && PegasusFlight.GLIDE_SINK < 0.5, "A landing or a glide falls less than half a block a tick");
	}

	private static void staleAndRiderless() {
		Conditions stale = new Conditions(true, false, false, false, 0, false, false, false);
		Conditions none = new Conditions(false, false, false, false, 0, false, false, false);
		for (State s : new State[] {State.TAKEOFF, State.FLYING}) {
			check(PegasusFlight.next(s, 3, stale) == State.GLIDING, s + ": stale input glides");
			check(PegasusFlight.next(s, 3, none) == State.GLIDING, s + ": no rider glides");
		}
		check(PegasusFlight.next(State.GLIDING, 3, none) == State.GLIDING, "A glide keeps gliding");
		check(PegasusFlight.next(State.GLIDING, 3, air(false, false, 0)) == State.FLYING, "Fresh input takes the glide back");
		check(PegasusFlight.next(State.GLIDING, 3, new Conditions(false, false, false, false, 0, true, false, false)) == State.GROUNDED, "A glide lands");
		check(PegasusFlight.verticalTarget(State.GLIDING, true, false, false) == -PegasusFlight.GLIDE_SINK, "A glide ignores Ascend");
	}

	private static void ceilings() {
		check(PegasusFlight.clampClimb(0.3, 0.1) == 0.1, "A climb stops at the ceiling");
		check(PegasusFlight.clampClimb(0.3, -2.0) == 0.0, "Above the limit it may not climb at all");
		check(PegasusFlight.clampClimb(-0.3, 0.0) == -0.3, "Descending is never held back");
		check(PegasusFlight.clampClimb(0.2, 5.0) == 0.2, "Open sky lets it climb");
	}

	private static void curves() {
		double v = 0;
		double last = 0;
		int ticks = 0;
		while (v < PegasusFlight.CLIMB && ticks < 200) {
			double next = PegasusFlight.ease(v, PegasusFlight.CLIMB, PegasusFlight.V_ACCEL);
			check(next > v && next - v <= PegasusFlight.V_ACCEL + 1.0E-12, "Speeding up is gradual: " + ticks);
			check(next <= PegasusFlight.CLIMB + 1.0E-12, "It never overshoots");
			last = next - v;
			v = next;
			ticks++;
		}
		check(v == PegasusFlight.CLIMB && ticks < 60, "It reaches its climb in under three seconds: " + ticks);
		check(last < PegasusFlight.V_ACCEL, "and arrives gently");
		double[] h = {0, 0};
		double[] target = PegasusFlight.horizontalTarget(0, 1, 0, PegasusFlight.FLY_SPEED);
		check(Math.abs(target[0]) < 1.0E-12 && Math.abs(target[1] - PegasusFlight.FLY_SPEED) < 1.0E-12, "Forward, facing south, flies south");
		double[] west = PegasusFlight.horizontalTarget(0, 1, 90, 1);
		check(west[0] < -0.999 && Math.abs(west[1]) < 1.0E-9, "Facing west, forward flies west");
		double[] diagonal = PegasusFlight.horizontalTarget(1, 1, 0, 1);
		check(Math.abs(Math.hypot(diagonal[0], diagonal[1]) - 1) < 1.0E-12, "Diagonals are no faster");
		for (int i = 0; i < 200; i++) {
			double[] n = PegasusFlight.approach(h[0], h[1], target[0], target[1], PegasusFlight.H_ACCEL, PegasusFlight.H_DECEL);
			check(Math.hypot(n[0] - h[0], n[1] - h[1]) <= PegasusFlight.H_ACCEL + 1.0E-12, "Acceleration is bounded");
			h = n;
		}
		check(Math.abs(h[1] - PegasusFlight.FLY_SPEED) < 1.0E-9, "Held forward it reaches its top speed");
		double[] slowing = PegasusFlight.approach(h[0], h[1], 0, 0, PegasusFlight.H_ACCEL, PegasusFlight.H_DECEL);
		check(h[1] - slowing[1] <= PegasusFlight.H_DECEL + 1.0E-12 && slowing[1] < h[1], "Letting go slows it gently");
	}

	private static void plausibility() {
		int t = PegasusFlight.CHECK_TICKS;
		double across = PegasusFlight.FLY_SPEED * t * PegasusFlight.TOLERANCE;
		double up = PegasusFlight.TAKEOFF_CLIMB * t * PegasusFlight.TOLERANCE;
		check(PegasusFlight.plausible(across - 0.5, 0, 0, t), "Top speed is plausible");
		check(!PegasusFlight.plausible(across + 0.5, 0, 0, t), "Faster is not");
		check(PegasusFlight.plausible(0, up - 0.5, 0, t) && !PegasusFlight.plausible(0, up + 0.5, 0, t), "Nor climbing faster");
		check(PegasusFlight.plausible(0, -200, 0, t), "Descending is not refused");
		check(PegasusFlight.TELEPORT_STEP > PegasusFlight.FLY_SPEED * 2, "A teleport step is far beyond a tick of flight");
	}

	// open, phase, amp, then the left wing's nine angles (shoulder, elbow, hand), from tools wingpose.py.
	private static final double[][] REFERENCE = {
		{0.0, 0.0, 0.0, 2.768913836286, -1.430313698405, 1.570796326795, 0.0, 0.08, 0.0, 0.0, 0.22, 0.0},
		{0.25, 0.0, 0.0, -0.391845496858, -1.114234617418, -1.309900289853, 0.0, 0.09, 0.0, 0.0, 0.185, 0.0},
		{0.5, 0.0, 0.0, -0.232974292464, -0.525806700825, -1.190289949683, 0.0, 0.10, 0.0, 0.0, 0.15, 0.0},
		{0.75, 1.0, 0.6, -0.205452501845, -0.208607846187, -0.408962513677, 0.0, 0.05, 0.055462424799, 0.0, 0.075, -0.018760834099},
		{1.0, 0.0, 0.0, 0.0, 0.0, -0.12, 0.0, 0.0, 0.04, 0.0, 0.0, 0.06},
		{1.0, -Math.PI / 2, 1.0, 0.0, 0.0, -1.07, 0.0, 0.0, -0.265936874914, 0.0, 0.0, -0.087124355744},
		{1.0, Math.PI / 2, 1.0, 0.0, 0.0, 0.83, 0.0, 0.0, 0.345936874914, 0.0, 0.0, 0.207124355744},
		{1.0, 0.6, 0.15, -0.002668109529, 0.022123782555, -0.039567963100, 0.0, 0.0, 0.034009995001, 0.0, 0.0, 0.006852040803},
	};

	private static double matrixGap(double[] a, double[] b) {
		double gap = 0;
		double[] ma = PegasusFlight.euler(a[0], a[1], a[2]);
		double[] mb = PegasusFlight.euler(b[0], b[1], b[2]);
		for (int i = 0; i < 9; i++) {
			gap = Math.max(gap, Math.abs(ma[i] - mb[i]));
		}
		return gap;
	}

	private static void wings() {
		for (double[] ref : REFERENCE) {
			double[] pose = PegasusFlight.wingPose(ref[0], ref[1], ref[2]);
			for (int bone = 0; bone < 3; bone++) {
				double[] got = {pose[bone * 3], pose[bone * 3 + 1], pose[bone * 3 + 2]};
				double[] want = {ref[3 + bone * 3], ref[4 + bone * 3], ref[5 + bone * 3]};
				check(matrixGap(got, want) < 1.0E-7, "Wing pose matches the preview script at open " + ref[0] + ", bone " + bone);
			}
		}
		// Folded: the shoulder sends the span back along the flank and the feathers down it.
		double[] fold = PegasusFlight.wingPose(0, 0, 1);
		double[] m = PegasusFlight.euler(fold[0], fold[1], fold[2]);
		check(m[6] > 0.95 && m[5] > 0.9, "Folded, the span runs back and the feathers hang down: " + m[6] + ", " + m[5]);
		check(m[2] > 0.3, "and flare out from the flank, clear of the saddle: " + m[2]);
		check(PegasusFlight.wingPose(0, 0, 1)[2] == PegasusFlight.wingPose(0, 2, 1)[2], "A folded wing does not beat");
		// Spread: amplitude bounds and the beat's direction (positive phase sine = the left tip down).
		double[] down = PegasusFlight.wingPose(1, Math.PI / 2, 1);
		double[] up = PegasusFlight.wingPose(1, -Math.PI / 2, 1);
		check(down[2] > 0 && up[2] < 0, "Downstroke lowers the left tip, upstroke raises it");
		check(Math.abs(down[2] - up[2] - 2 * PegasusFlight.FLAP_SHOULDER) < 1.0E-9, "The full stroke is twice the shoulder amplitude");
		// Continuity: small changes of open never jump the shoulder's rotation.
		double[] previous = null;
		for (int i = 0; i <= 400; i++) {
			double[] p = PegasusFlight.wingPose(i / 400.0, 0.3, 1);
			if (previous != null) {
				check(matrixGap(p, previous) < 0.05, "The fold moves smoothly at open " + i / 400.0);
			}
			previous = p;
		}
		// Targets: folded on the ground, lifted when ridden, beating harder to climb than to descend.
		check(PegasusFlight.wingTargets(State.GROUNDED, 0, false).open() == 0, "Grounded, the wings fold");
		check(PegasusFlight.wingTargets(State.GROUNDED, 0, true).open() == PegasusFlight.WING_OPEN_RIDDEN, "Ridden, they lift off the legs");
		var climb = PegasusFlight.wingTargets(State.FLYING, 1, true);
		var level = PegasusFlight.wingTargets(State.FLYING, 0, true);
		var descend = PegasusFlight.wingTargets(State.FLYING, -1, true);
		var glide = PegasusFlight.wingTargets(State.GLIDING, 0, false);
		check(climb.amp() > level.amp() && level.amp() > descend.amp() && climb.rate() > level.rate() && level.rate() > descend.rate(),
				"Climbing beats harder and faster; descending is calmer");
		check(glide.amp() < level.amp() && glide.open() == 1, "A glide holds the wings out with barely a beat");
		check(PegasusFlight.toward(0, 1, 0.125) == 0.125 && PegasusFlight.toward(1, 0, 0.3) == 0.7 && PegasusFlight.toward(0.9, 1, 0.5) == 1,
				"Wing smoothing steps and stops at its target");
	}
}
