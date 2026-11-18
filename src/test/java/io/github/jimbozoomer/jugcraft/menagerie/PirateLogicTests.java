package io.github.jimbozoomer.jugcraft.menagerie;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pure-logic checks of PirateMotion, PirateCourse and PirateFairness (docs/features/pirate-ships.md): the swell and
 * heel against the renderer's pose order, bracing, the wind, setting and furling; waypoints, lanes and basins against the
 * beat and the rig; the helm's stall memory; crew sizes, engagement and the spawn budget. The three classes use java.*
 * only, so this runs as a plain main (the pirateLogicTest task, as BoardRulesTests does); it throws on the first failure.
 */
public final class PirateLogicTests {
	private static int checks;

	private static void check(boolean ok, String what) {
		checks++;
		if (!ok) {
			throw new AssertionError(what);
		}
	}

	private static boolean near(double a, double b, double eps) {
		return Math.abs(a - b) <= eps;
	}

	public static void main(String[] args) {
		motion();
		phase();
		envelope();
		deckPlan();
		course();
		helm();
		fairness();
		budget();
		System.out.println("PirateLogicTests: " + checks + " checks passed");
	}

	// ------------------------------------------------------------------ motion

	/** Axis.XP then Axis.ZP as PoseStack applies them (pose.rotate(ZP) then pose.rotate(XP): the XP turn acts first). */
	private static double[] pose(double x, double y, double z, double roll, double pitch, double heave) {
		double p = Math.toRadians(pitch), r = Math.toRadians(roll);
		// Right-handed rotation about x by p.
		double[] a = {x, y * Math.cos(p) - z * Math.sin(p), y * Math.sin(p) + z * Math.cos(p)};
		// Right-handed rotation about z by r.
		return new double[] {a[0] * Math.cos(r) - a[1] * Math.sin(r), a[0] * Math.sin(r) + a[1] * Math.cos(r) + heave, a[2]};
	}

	private static void motion() {
		// lean matches the renderer's pose for points across the deck and aloft.
		for (double[] p : new double[][] {{2, 1, -2}, {-2, 1, 5.5}, {0, 1, -6}, {0, 9.5, 1}, {3, 2, 8}}) {
			for (float roll : new float[] {-4.7F, -1.2F, 0F, 2.3F}) {
				for (float pitch : new float[] {-0.6F, 0F, 0.6F}) {
					double[] got = PirateMotion.lean(p[0], p[1], p[2], roll, pitch, 0.05);
					double[] want = pose(p[0], p[1], p[2], roll, pitch, 0.05);
					check(near(got[0], want[0], 1e-9) && near(got[1], want[1], 1e-9) && near(got[2], want[2], 1e-9),
							"lean should match the pose's turns");
				}
			}
		}
		// A negative roll lowers the port side (+x).
		check(PirateMotion.lean(3, 0, 0, -5F, 0F, 0)[1] < 0, "a negative roll lowers the port side");
		// A positive pitch lowers the bow.
		check(PirateMotion.lean(0, 0, 8, 0F, 2F, 0)[1] < 0, "a positive pitch lowers the bow");
		// turnY is Axis.YP / Vec3.yRot: yaw 90 takes the bow (+z) to +x... yRot(90): x' = z sin = 1.
		double[] t = PirateMotion.turnY(0, 1, 90F);
		check(near(t[0], 1, 1e-9) && near(t[1], 0, 1e-9), "turnY(0,1,90) should be (1,0)");
		// Swell and heel stay small: the heave at most half a pixel; the heel bounded; the whole roll bounded.
		float maxRoll = 0;
		double maxHeave = 0;
		for (int tick = 0; tick < PirateMotion.PERIOD * 2; tick++) {
			float phase = PirateMotion.phase(tick, 0, 0F);
			maxHeave = Math.max(maxHeave, Math.abs(PirateMotion.heave(phase)));
			for (float rudder : new float[] {-1, 0, 1}) {
				for (float wind : new float[] {-90, -30, 0, 45, 90, 180}) {
					maxRoll = Math.max(maxRoll, Math.abs(PirateMotion.roll(phase, rudder, 1F, wind)));
				}
			}
		}
		check(maxHeave <= 1.0 / 32.0 + 1e-9, "the heave is at most half a model pixel, was " + maxHeave);
		check(maxRoll <= PirateMotion.ROLL + PirateMotion.HEEL_MAX + 1e-4, "the roll is bounded, was " + maxRoll);
		check(PirateMotion.heel(1F, 1F, 0F) < 0 && PirateMotion.heel(-1F, 1F, 0F) > 0, "a turn heels her outward");
		check(PirateMotion.heel(0F, 1F, 90F) < 0 && PirateMotion.heel(0F, 1F, -90F) > 0, "a beam wind heels her to leeward");
		check(PirateMotion.heel(1F, 0F, 90F) == 0F, "no headway, no heel");
		// The crew's seat on the drawn deck: with the most roll, a deck seat 2 blocks out moves at most about 0.16 blocks.
		double[] seat = PirateMotion.lean(2, 1, -2, -(PirateMotion.ROLL + PirateMotion.HEEL_MAX), PirateMotion.PITCH, PirateMotion.HEAVE);
		check(Math.abs(seat[1] - 1.0) < 0.25, "a heeled deck seat stays near the deck, was " + seat[1]);
		// Bracing: square dead astern, about 30 on the beam, sharp up at 60 and closer; the sign follows the wind's side.
		check(PirateMotion.brace(180F) == 0F && PirateMotion.brace(-180F) == 0F, "square before the wind");
		check(near(PirateMotion.brace(90F), 30.0, 1e-4) && near(PirateMotion.brace(-90F), -30.0, 1e-4), "30 degrees on the beam");
		check(PirateMotion.brace(60F) == PirateMotion.BRACE_MAX && PirateMotion.brace(10F) == PirateMotion.BRACE_MAX, "sharp up");
		check(PirateMotion.brace(-45F) == -PirateMotion.BRACE_MAX, "sharp up from port");
		float last = 0;
		for (int a = 180; a >= 0; a--) {
			float b = PirateMotion.brace(a);
			check(b >= last - 1e-5, "bracing grows as the wind draws forward");
			last = b;
		}
		// With the yards braced, the wind fills the after face (the face whose normal is aft, turned by the brace).
		for (int rel = -179; rel <= 179; rel++) {
			if (Math.abs(rel) < 70) {
				continue; // inside 70 degrees no square sail draws: the ship is pinching, the sails shiver (still braced sharp up)
			}
			float brace = PirateMotion.brace(rel);
			double[] normal = PirateMotion.turnY(0, -1, brace); // the after face's normal
			double[] blowsFrom = PirateMotion.turnY(0, 1, -rel); // a yaw rel to starboard is -x: turnY by -rel
			// The wind blows from blowsFrom toward -blowsFrom; it fills the after face when it travels against the normal.
			double dot = -blowsFrom[0] * normal[0] + -blowsFrom[1] * normal[1];
			check(dot < 0, "the wind should fill the after face at " + rel + " degrees off the bow (brace " + brace + ")");
		}
		// The wind: a compass point midday, the same for both sides, shifting smoothly at the day's end.
		Set<Float> points = new HashSet<>();
		for (long day = 0; day < 400; day++) {
			float w = PirateMotion.wind(day * PirateMotion.DAY + 12_000L);
			check(w >= 0 && w < 360 && near(w % 22.5F, 0, 1e-3), "a midday wind is one of the sixteen points: " + w);
			points.add(w);
		}
		check(points.size() >= 12, "the wind should visit most of the compass over 400 days, visited " + points.size());
		float prev = PirateMotion.wind(0);
		for (long time = 1; time < 5 * PirateMotion.DAY; time++) {
			float w = PirateMotion.wind(time);
			check(Math.abs(PirateMotion.wrap(w - prev)) <= 180.0F / PirateMotion.SHIFT * 1.6F, "the wind never jumps: " + time);
			prev = w;
		}
		check(PirateMotion.wind(-5) >= 0, "negative times still give a wind");
		// Sails: furl and set in 40 ticks, bounded.
		float s = 0;
		int ticks = 0;
		while (s < 1F) {
			s = PirateMotion.sailStep(s, true);
			ticks++;
		}
		check(ticks == 40, "40 ticks to set the sails, took " + ticks);
		check(PirateMotion.sailStep(0F, false) == 0F && PirateMotion.sailStep(1F, true) == 1F, "sails stay in 0..1");
		check(!PirateMotion.sailsSet(true, true, false, false) && !PirateMotion.sailsSet(false, false, false, false)
				&& !PirateMotion.sailsSet(true, false, true, false) && PirateMotion.sailsSet(true, false, false, false),
				"sails are set only under a helmsman, under way");
		check(PirateMotion.wrap(190F) == -170F && PirateMotion.wrap(-180F) == -180F + 360F - 360F, "wrap");
	}

	// ------------------------------------------------------------------ phase, envelope, deck

	/**
	 * One phase on both sides: the server's (game time, partial 0) and a client's (its game time and frame's partial tick)
	 * agree for any start time, the client's entity age plays no part, the phase stays small and wraps without a seam,
	 * and the offset is fixed by the UUID (a save and reload keeps it).
	 */
	private static void phase() {
		long[] starts = {0L, 1L, 2_399L, 2_400L, 987_654_321L, 1L << 40, Long.MAX_VALUE / 4};
		long uuidHigh = 0x1234_5678_9abc_def0L, uuidLow = 0x0fed_cba9_8765_4321L;
		long offset = PirateMotion.offset(uuidHigh, uuidLow);
		check(offset >= 0 && offset < PirateMotion.PERIOD && offset == PirateMotion.offset(uuidHigh, uuidLow),
				"the offset is fixed by the UUID and within the period");
		check(PirateMotion.offset(uuidHigh, uuidLow + 1) != offset || PirateMotion.offset(uuidHigh + 7, uuidLow) != offset,
				"different ships get different offsets");
		for (long start : starts) {
			for (int tick = 0; tick < 300; tick++) {
				long time = start + tick;
				float server = PirateMotion.phase(time, offset, 0F);
				check(server >= 0 && server < PirateMotion.PERIOD, "the server's phase stays within the period at " + time);
				// A client a tick behind, a whole frame into it, draws the server's pose.
				float client = PirateMotion.phase(time - 1, offset, 1F);
				check(near(PirateMotion.swellRoll(server), PirateMotion.swellRoll(client), 1e-4)
						&& near(PirateMotion.pitch(server), PirateMotion.pitch(client), 1e-4)
						&& near(PirateMotion.heave(server), PirateMotion.heave(client), 1e-6),
						"client and server agree across the tick and the wrap at " + time);
				// Smooth within a tick: a half frame lies between the two ticks' values (no jump, no float noise).
				float half = PirateMotion.phase(time, offset, 0.5F);
				double a = PirateMotion.swellRoll(server), b = PirateMotion.swellRoll(PirateMotion.phase(time + 1, offset, 0F));
				check(PirateMotion.swellRoll(half) >= Math.min(a, b) - 0.01 && PirateMotion.swellRoll(half) <= Math.max(a, b) + 0.01,
						"the partial tick eases between ticks at " + time);
			}
		}
		// Periodic: a whole period later the swell is the same.
		for (int p = 0; p < PirateMotion.PERIOD; p += 7) {
			check(near(PirateMotion.swellRoll(p), PirateMotion.swellRoll(p + PirateMotion.PERIOD), 1e-4)
					&& near(PirateMotion.pitch(p), PirateMotion.pitch(p + PirateMotion.PERIOD), 1e-4), "the swell repeats each period");
		}
	}

	/**
	 * The hull's swell envelope against the clearance the spawn, route and claim checks use (PirateWaters, whose values are
	 * mirrored here and tied to the Java by scripts/tests/test_pirate_ship_art.py): every hull corner at or under the
	 * waterline, at every extreme of roll (swell and the most heel), pitch and heave, lies inside the checked water. The
	 * model's origin is 0.875 above the bottom of the waterline block (WATERLINE); that row is checked open water or air
	 * across the rig's breadth and length; the DEPTH rows under it open water across the hull's 6 x 16; KEEL is the most
	 * the drawn hull may reach under the waterline (a sinking ship's checks).
	 */
	private static void envelope() {
		double waterline = 0.875, halfBeam = 3.0, halfLength = 8.0, rigHalfBeam = 4.0, rigStern = -9.5, rigBow = 11.75;
		int depth = 2;
		double keel = 1.75;
		// The hull's corners at and under the waterline, pixels (tools/pirate_ship.py): the waterline's widest slices, the
		// two underwater layers, the keel, deadwood and sternpost, the stem's foot and the rudder (turned either way).
		java.util.List<double[]> corners = new java.util.ArrayList<>();
		for (double x : new double[] {-48, 48}) {
			for (double z : new double[] {-88, 48}) {
				for (double y : new double[] {0, 4, 8, 16}) {
					corners.add(new double[] {x, y, z});
				}
			}
		}
		for (double x : new double[] {-36, 36}) {
			for (double y : new double[] {0, 4}) {
				corners.add(new double[] {x, y, -128});
			}
		}
		for (double x : new double[] {-38, 38}) {
			for (double z : new double[] {-114, 120}) {
				corners.add(new double[] {x, -12, z});
			}
		}
		for (double x : new double[] {-22, 22}) {
			for (double z : new double[] {-108, 108}) {
				corners.add(new double[] {x, -24, z});
			}
		}
		for (double x : new double[] {-2.5, 2.5}) {
			corners.add(new double[] {x, -26, -117});
			corners.add(new double[] {x, -26, 114});
			corners.add(new double[] {x, -24, -117});
			corners.add(new double[] {x, -12, 126});
		}
		for (float turn : new float[] {-25F, 0F, 25F}) {
			for (double[] blade : new double[][] {{-2, -22, -10.5}, {2, -22, -10.5}, {-2, -22, 0}, {2, -22, 0}}) {
				double[] xz = PirateMotion.turnY(blade[0], blade[2], turn);
				corners.add(new double[] {xz[0], blade[1], xz[1] - 117});
			}
		}
		float maxRoll = PirateMotion.ROLL + PirateMotion.HEEL_MAX;
		double deepest = 0, widestBelowRow = 0, widestInRow = 0, longestBelowRow = 0;
		for (double[] c : corners) {
			for (float roll : new float[] {-maxRoll, -maxRoll / 2, 0F, maxRoll / 2, maxRoll}) {
				for (float pitch : new float[] {-PirateMotion.PITCH, 0F, PirateMotion.PITCH}) {
					for (double heave : new double[] {-PirateMotion.HEAVE, PirateMotion.HEAVE}) {
						double[] p = PirateMotion.lean(c[0] / 16, c[1] / 16, c[2] / 16, roll, pitch, heave);
						if (p[1] > 0.125) {
							continue; // above the waterline row: the rig's box, checked by the art test
						}
						deepest = Math.max(deepest, -p[1]);
						if (p[1] >= -waterline) {
							widestInRow = Math.max(widestInRow, Math.abs(p[0]));
							check(Math.abs(p[0]) <= rigHalfBeam && p[2] >= rigStern && p[2] <= rigBow,
									"in the waterline row the hull stays inside the rig's checked breadth: " + java.util.Arrays.toString(p));
						} else {
							widestBelowRow = Math.max(widestBelowRow, Math.abs(p[0]));
							longestBelowRow = Math.max(longestBelowRow, Math.abs(p[2]));
							check(Math.abs(p[0]) <= halfBeam && Math.abs(p[2]) <= halfLength && p[1] >= -(waterline + depth),
									"under the waterline row the hull stays inside the checked 6 x 16 water: " + java.util.Arrays.toString(p));
						}
					}
				}
			}
		}
		check(deepest <= keel, "the drawn hull reaches " + deepest + " under the waterline, more than KEEL " + keel);
		// The drawn deck against the level walkable deck: over every deck square (PirateDeckPlan, corners and edges) and
		// every extreme of roll, pitch and heave, the drawn deck's height parts from the walkable deck's (y 1.0) by no more
		// than PirateMotion.DECK_CAP.
		double worstDeck = 0;
		for (int sq = 0; sq < PirateDeckPlan.SQUARE_CENTRES.length; sq++) {
			double keelZ = PirateDeckPlan.SQUARE_CENTRES[sq], h = PirateDeckPlan.SQUARE_WIDTHS[sq] / 2;
			for (double dx : new double[] {-h, -h / 2, 0, h / 2, h}) {
				for (double dz : new double[] {-h, 0, h}) {
					for (float roll : new float[] {-maxRoll, 0F, maxRoll}) {
						for (float pitch : new float[] {-PirateMotion.PITCH, 0F, PirateMotion.PITCH}) {
							for (double heave : new double[] {-PirateMotion.HEAVE, PirateMotion.HEAVE}) {
								double[] p = PirateMotion.lean(dx, 1.0, keelZ + dz, roll, pitch, heave);
								worstDeck = Math.max(worstDeck, Math.abs(p[1] - 1.0));
							}
						}
					}
				}
			}
		}
		check(worstDeck <= PirateMotion.DECK_CAP, "the drawn deck parts from the walkable deck by " + worstDeck + ", over the cap");
		check(deepest <= waterline + depth, "and inside the checked depth");
		System.out.printf(java.util.Locale.ROOT, "Swell envelope: deepest %.4f blocks under the waterline (KEEL %.2f); widest %.4f in the "
				+ "waterline row (rig %.1f), %.4f under it (hull %.1f), longest %.4f under it (hull %.1f); drawn deck off the walkable "
				+ "deck by at most %.4f blocks (%.2f px; cap %.2f px)%n", deepest, keel, widestInRow, rigHalfBeam, widestBelowRow, halfBeam,
				longestBelowRow, halfLength, worstDeck, worstDeck * 16, PirateMotion.DECK_CAP * 16);
	}

	/** The deck plan behind PirateDecks.standingOnDeck. */
	private static void deckPlan() {
		check(PirateDeckPlan.over(0, 0, 0F) && PirateDeckPlan.over(2.4, 0, 0F) && PirateDeckPlan.over(2.4, -7.4, 0F)
				&& !PirateDeckPlan.over(2.6, 0, 0F) && !PirateDeckPlan.over(0, -7.6, 0F) && !PirateDeckPlan.over(0, 7.7, 0F),
				"the deck is 5 wide amidships, from the transom to the stem");
		// The bow is fine: the squares follow it (no standing on air beside the stem).
		check(!PirateDeckPlan.over(2.4, 6.0, 0F) && PirateDeckPlan.over(1.3, 6.0, 0F) && !PirateDeckPlan.over(1.0, 7.4, 0F)
				&& PirateDeckPlan.over(0.7, 7.4, 0F), "the deck narrows with the bow");
		check(PirateDeckPlan.SQUARE_CENTRES.length == PirateDeckPlan.SQUARE_WIDTHS.length, "a width for every square");
		for (int i = 0; i < PirateDeckPlan.SQUARE_CENTRES.length - 1; i++) {
			double end = PirateDeckPlan.SQUARE_CENTRES[i] + PirateDeckPlan.SQUARE_WIDTHS[i] / 2;
			double next = PirateDeckPlan.SQUARE_CENTRES[i + 1] - PirateDeckPlan.SQUARE_WIDTHS[i + 1] / 2;
			check(next <= end, "the squares meet or overlap along the keel: no gap between " + i + " and " + (i + 1));
		}
		// At yaw 90 the bow (+z local) points to -x world (yRot(-90)): the deck runs along x.
		check(PirateDeckPlan.over(-7.4, 0, 90F) && PirateDeckPlan.over(7.4, 0, 90F) && !PirateDeckPlan.over(0, 3.0, 90F),
				"turned 90 degrees it runs along x");
		// Between right angles the axis-aligned squares overreach; the plan does not: a corner of the squares' box at 45
		// degrees is off the deck.
		double[] corner = PirateMotion.turnY(2.5, -7.5, -45F); // the deck's corner at the transom, in world axes
		check(PirateDeckPlan.over(corner[0] * 0.99, corner[1] * 0.99, 45F), "just inside the turned corner is on the deck");
		check(!PirateDeckPlan.over(2.5 * Math.sqrt(2) + 0.1, 0, 45F), "off the turned deck's side is not");
		for (int yaw = -180; yaw < 180; yaw += 15) {
			double[] w = PirateMotion.turnY(1.0, 3.0, -yaw);
			check(PirateDeckPlan.over(w[0], w[1], yaw), "a deck point stays on the deck at yaw " + yaw);
			double[] off = PirateMotion.turnY(3.5, 0.0, -yaw);
			check(!PirateDeckPlan.over(off[0], off[1], yaw), "water beside the hull is off it at yaw " + yaw);
		}
		check(PirateDeckPlan.atDeckHeight(1.0) && PirateDeckPlan.atDeckHeight(1.05) && !PirateDeckPlan.atDeckHeight(1.125)
				&& !PirateDeckPlan.atDeckHeight(0.5) && !PirateDeckPlan.atDeckHeight(2.0), "feet at the deck's top, within a pixel");
		check(PirateDeckPlan.standing(0, 1.0, 2, 0F, true), "standing on the deck");
		check(!PirateDeckPlan.standing(0, 1.0, 2, 0F, false), "mid-jump over the deck is not standing on it");
		check(!PirateDeckPlan.standing(0, 2.0, 2, 0F, true), "on a block on the deck is not on the deck");
		check(!PirateDeckPlan.standing(0, 9.5, 1, 0F, true), "in the main top is not on the deck");
		check(!PirateDeckPlan.standing(0, 0.4, 2, 0F, true), "inside the hull below the deck is not on it");
		check(!PirateDeckPlan.standing(4.0, 1.0, 0, 0F, true), "on a dock beside the ship at deck height is not on it");
		check(!PirateDeckPlan.standing(4.0, -0.1, 0, 0F, true), "swimming beside the hull is not on it");
	}

	// ------------------------------------------------------------------ course

	private static void course() {
		double rigCorner = Math.sqrt(4.0 * 4.0 + 11.75 * 11.75);
		check(PirateCourse.BASIN >= rigCorner, "the basin should hold the rig's farthest corner as it turns");
		// The slowest turning circle: a third of the top speed (0.12) at one degree a tick.
		double turningRadius = (0.12 / 3.0) / Math.toRadians(1.0);
		check(PirateCourse.WAYPOINT_MAX + PirateCourse.ARRIVAL + 2 * turningRadius <= PirateCourse.PATROL_RADIUS,
				"a ship turning at a waypoint stays on the beat");
		check(PirateCourse.LANE_HALF_WIDTH >= 4 + 2, "a lane holds the rig with sea room");
		check(PirateCourse.LANE_DEPTH > 2, "a lane needs more water than the hull draws");
		for (int i = 0; i < 1000; i++) {
			double angle = i * 0.37, share = (i % 101) / 100.0;
			double[] c = PirateCourse.candidate(100.5, -40.25, angle, share);
			double d = Math.hypot(c[0] - 100.5, c[1] + 40.25);
			check(d >= PirateCourse.MIN_LEG - 1e-9 && d <= PirateCourse.WAYPOINT_MAX + 1e-9, "candidate distance " + d);
			check(PirateCourse.onBeat(100.5, -40.25, c[0], c[1]), "a candidate is on the beat");
		}
		check(!PirateCourse.onBeat(0, 0, 64.1, 0) && PirateCourse.onBeat(0, 0, 64, 0), "the beat's edge");
		// A lane: both ends' centre columns, full width, no repeats, spacing within a sample.
		List<int[]> lane = PirateCourse.lane(0.5, 0.5, 30.5, 40.5);
		Set<Long> keys = new HashSet<>();
		for (int[] c : lane) {
			check(keys.add(((long) c[0] << 32) ^ (c[1] & 0xFFFFFFFFL)), "no column twice");
		}
		check(contains(lane, 0, 0) && contains(lane, 30, 40), "the lane holds both ends");
		double length = 50;
		for (int[] c : lane) {
			double t = ((c[0] + 0.5 - 0.5) * 30 + (c[1] + 0.5 - 0.5) * 40) / length;
			double across = Math.abs(((c[0] + 0.5 - 0.5) * 40 - (c[1] + 0.5 - 0.5) * 30) / length);
			check(t >= -1.5 && t <= length + 1.5 && across <= PirateCourse.LANE_HALF_WIDTH + 1.5, "a lane column stays in the lane");
		}
		// Every point of the lane's centre line and edges is within a sample (and a block's rounding) of a column.
		for (double t = 0; t <= length; t += 0.5) {
			for (double k = -PirateCourse.LANE_HALF_WIDTH; k <= PirateCourse.LANE_HALF_WIDTH; k += 0.5) {
				double x = 0.5 + 30 * t / length - 40 * k / length, z = 0.5 + 40 * t / length + 30 * k / length;
				double best = Double.MAX_VALUE;
				for (int[] c : lane) {
					best = Math.min(best, Math.hypot(c[0] + 0.5 - x, c[1] + 0.5 - z));
				}
				check(best <= PirateCourse.SAMPLE * Math.sqrt(2) / 2 + 0.75, "the lane leaves no gap wider than a sample");
			}
		}
		List<int[]> zero = PirateCourse.lane(5, 5, 5, 5);
		check(!zero.isEmpty(), "a lane of no length still checks where the ship is");
		List<int[]> basin = PirateCourse.basin(10.5, -3.5);
		check(contains(basin, 10 - PirateCourse.BASIN, -4 - PirateCourse.BASIN) && contains(basin, 10 + PirateCourse.BASIN, -4 + PirateCourse.BASIN)
				&& basin.size() == (PirateCourse.BASIN + 1) * (PirateCourse.BASIN + 1), "the basin spans its square, corners included");
		double[] reach = PirateCourse.protectedReach(0, 0, 30, 40);
		check(reach[0] <= -PirateCourse.CLAIM_MARGIN - PirateCourse.BASIN && reach[3] >= 40 + PirateCourse.CLAIM_MARGIN + PirateCourse.BASIN,
				"the protected reach keeps the claim margin round lane and basin");
	}

	private static boolean contains(List<int[]> columns, int x, int z) {
		for (int[] c : columns) {
			if (c[0] == x && c[1] == z) {
				return true;
			}
		}
		return false;
	}

	private static void helm() {
		PirateCourse.Helm helm = new PirateCourse.Helm();
		check(helm.order() == PirateCourse.Order.SAIL, "a fresh helm sails");
		for (int i = 0; i < PirateCourse.STALL_TICKS - 1; i++) {
			check(helm.after(PirateCourse.Order.SAIL, false) == PirateCourse.Order.SAIL, "it keeps trying until it stalls");
		}
		check(helm.after(PirateCourse.Order.SAIL, true) == PirateCourse.Order.SAIL && helm.refused() == 0, "a step taken clears the stall");
		for (int i = 0; i < PirateCourse.STALL_TICKS - 1; i++) {
			helm.after(PirateCourse.Order.SAIL, false);
		}
		check(helm.after(PirateCourse.Order.SAIL, false) == PirateCourse.Order.BACK, "stalled, it backs off");
		int backs = 0;
		while (helm.order() == PirateCourse.Order.BACK) {
			helm.after(PirateCourse.Order.BACK, true);
			backs++;
		}
		check(backs == PirateCourse.BACK_TICKS && helm.order() == PirateCourse.Order.SAIL, "it backs at most BACK_TICKS, then sails");
		for (int i = 0; i < PirateCourse.STALL_TICKS; i++) {
			helm.after(PirateCourse.Order.SAIL, false);
		}
		check(helm.after(PirateCourse.Order.BACK, false) == PirateCourse.Order.REST, "boxed in astern too, it rests");
		int rests = 0;
		while (helm.order() == PirateCourse.Order.REST) {
			helm.after(PirateCourse.Order.REST, false);
			rests++;
		}
		check(rests == PirateCourse.REST_TICKS, "it rests REST_TICKS");
		PirateCourse.Helm saved = new PirateCourse.Helm(999, -4, 7);
		check(saved.refused() == PirateCourse.STALL_TICKS && saved.backing() == 0 && saved.resting() == 7, "saved values are clamped");
		saved.clear();
		check(saved.order() == PirateCourse.Order.SAIL && saved.resting() == 0, "clear forgets the stall");
	}

	// ------------------------------------------------------------------ fairness and budgets

	private static void fairness() {
		check(PirateFairness.crewFor(0) == 3 && PirateFairness.crewFor(14) == 3 && PirateFairness.crewFor(15) == 4
				&& PirateFairness.crewFor(19) == 4 && PirateFairness.crewFor(20) == 5 && PirateFairness.crewFor(30) == 5, "crew 3 to 5 by armour");
		for (int armor = 0; armor <= 30; armor++) {
			int crew = PirateFairness.crewFor(armor);
			check(crew >= PirateFairness.MIN_CREW && crew <= PirateFairness.MAX_CREW, "crew within 3..5");
			check(armor == 0 || crew >= PirateFairness.crewFor(armor - 1), "crew never shrinks with more armour");
		}
		long now = 100_000L;
		check(!PirateFairness.mayEngage(7, PirateFairness.NEVER, now), "a leather-clad stranger sails past");
		check(PirateFairness.mayEngage(10, PirateFairness.NEVER, now), "a well-armed player is fair game");
		check(PirateFairness.mayEngage(0, now - 10, now), "a provoker is fair game");
		check(!PirateFairness.mayEngage(0, now - PirateFairness.PROVOKED_TICKS - 1, now), "a provocation wears off");
		check(!PirateFairness.mayEngage(0, now + 50, now), "a provocation in the future (a clock change) does not count");
		check(PirateFairness.refusal(3, 20, 0, now, true, 1e9) != null, "the cap refuses");
		check(PirateFairness.refusal(0, 9, 0, now, true, 1e9) != null, "low armour refuses");
		check(PirateFairness.refusal(0, 20, now + 1, now, true, 1e9) != null, "a resting player refuses");
		check(PirateFairness.refusal(0, 20, 0, now, false, 1e9) != null, "a taken region refuses");
		check(PirateFairness.refusal(0, 20, 0, now, true, PirateFairness.SPACING - 1) != null, "a near ship refuses");
		check(PirateFairness.refusal(2, 10, now, now, true, PirateFairness.SPACING) == null, "otherwise a ship may be raised");
	}

	/**
	 * A player at sea for 30 days, a spawn pass every 1,200 ticks that always succeeds when the rules allow, and every ship
	 * beaten at once (so the cap never binds): at most one ship a day is raised near them, so at most one hold a day.
	 */
	private static void budget() {
		long rest = 0;
		int raised = 0;
		long days = 30;
		for (long now = 0; now < days * PirateFairness.PLAYER_REST; now += 1_200) {
			if (PirateFairness.refusal(0, 20, rest, now, true, 1e9) == null) {
				raised++;
				rest = now + PirateFairness.PLAYER_REST;
			}
		}
		check(raised <= days, "at most one ship a day near one player, raised " + raised);
		// Several players together: each raise rests only its own player, the cap still bounds what is afloat.
		check(PirateFairness.MAX_SHIPS * 10 <= 30, "the entity budget: three ships of at most 1 + 4 deck squares + 5 crew");
	}
}
