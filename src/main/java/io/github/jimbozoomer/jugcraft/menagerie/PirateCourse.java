package io.github.jimbozoomer.jugcraft.menagerie;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The geometry of a pirate ship's patrol (docs/features/pirate-ships.md), as pure arithmetic with no game types, so it
 * can be checked on its own: where a waypoint may lie within the beat, which water columns a straight lane to it and a
 * turning basin round it must show to be open sea, and what the helm does when the way is refused (backing off, then
 * resting), so a ship never sits stalled against a shore pushing at the same step.
 *
 * <p>{@link PirateWaters} reads the columns this names (loaded chunks only); {@link PirateWaters#sweep} still checks
 * every step exactly. The lanes and basins are a screen at a sample spacing of {@value #SAMPLE} blocks: they keep the
 * ship off shores, shallows and anything built on the water, and away from protected land by {@value #CLAIM_MARGIN}
 * blocks; a lone post between two samples is caught by the step's sweep, which stops the ship and makes it choose again.
 */
public final class PirateCourse {
	/** Blocks from the anchor the ship may sail at most (the beat), and the nearest and farthest a waypoint may lie. */
	public static final double PATROL_RADIUS = 64.0;
	public static final double MIN_LEG = 16.0;
	/**
	 * The farthest a waypoint lies from the anchor: inside the beat by twice the slowest turning circle and the arrival
	 * distance, so a ship that reaches it can turn there without its centre crossing the beat's edge.
	 */
	public static final double WAYPOINT_MAX = 54.0;
	/** A ship has reached its waypoint within this many blocks. */
	public static final double ARRIVAL = 3.0;
	/** Spacing of the sampled columns, along and across a lane and over a basin. */
	public static final int SAMPLE = 2;
	/** Half the width of the open water a lane needs: the rig's half-breadth (4) and two blocks of sea room. */
	public static final int LANE_HALF_WIDTH = 6;
	/**
	 * Half the side of the open square a waypoint (and a place a ship is raised) needs to turn in: the rig's farthest
	 * corner from the centre (about 12.4 blocks: bowsprit and stern lantern) rounded up.
	 */
	public static final int BASIN = 13;
	/** Water under the waterline block a lane's columns need: one more than the hull's draught ({@link PirateWaters#DEPTH}). */
	public static final int LANE_DEPTH = 3;
	/** How far lanes and basins keep from any claim, town or operator region. */
	public static final int CLAIM_MARGIN = 16;
	/** Refused steps in a row before the helm backs off. */
	public static final int STALL_TICKS = 60;
	/** The most ticks a stalled ship goes astern, and how fast (blocks a tick). */
	public static final int BACK_TICKS = 80;
	public static final double BACK_SPEED = 0.03;
	/** Ticks a ship that can neither go ahead nor astern lies still before it tries again. */
	public static final int REST_TICKS = 100;

	private PirateCourse() {
	}

	/**
	 * A waypoint candidate {@value #MIN_LEG}-{@value #WAYPOINT_MAX} blocks from the anchor: at {@code angle} (radians)
	 * and {@code share} (0 to 1) of the way from the nearest to the farthest. Returns {x, z}.
	 */
	public static double[] candidate(double anchorX, double anchorZ, double angle, double share) {
		double s = Math.max(0.0, Math.min(1.0, share));
		double distance = MIN_LEG + s * (WAYPOINT_MAX - MIN_LEG);
		return new double[] {anchorX + Math.cos(angle) * distance, anchorZ + Math.sin(angle) * distance};
	}

	/** Whether a point is on the beat: within {@value #PATROL_RADIUS} blocks of the anchor, horizontally. */
	public static boolean onBeat(double anchorX, double anchorZ, double x, double z) {
		double dx = x - anchorX, dz = z - anchorZ;
		return dx * dx + dz * dz <= PATROL_RADIUS * PATROL_RADIUS;
	}

	/**
	 * The block columns a straight lane from (fromX, fromZ) to (toX, toZ) must show as open sea: every {@value #SAMPLE}
	 * blocks along it, ends included, and across it from -{@value #LANE_HALF_WIDTH} to +{@value #LANE_HALF_WIDTH}
	 * blocks every {@value #SAMPLE}, each sample's block column once. Returns {x, z} pairs, in order along the lane.
	 */
	public static List<int[]> lane(double fromX, double fromZ, double toX, double toZ) {
		double dx = toX - fromX, dz = toZ - fromZ;
		double length = Math.sqrt(dx * dx + dz * dz);
		double ux = length < 1.0E-9 ? 0.0 : dx / length, uz = length < 1.0E-9 ? 1.0 : dz / length;
		// Across the lane: the direction turned a right angle.
		double px = -uz, pz = ux;
		Set<Long> seen = new LinkedHashSet<>();
		List<int[]> out = new ArrayList<>();
		int steps = (int) Math.ceil(length / SAMPLE);
		for (int i = 0; i <= steps; i++) {
			double t = Math.min(length, i * (double) SAMPLE);
			double cx = fromX + ux * t, cz = fromZ + uz * t;
			for (int k = -LANE_HALF_WIDTH; k <= LANE_HALF_WIDTH; k += SAMPLE) {
				int x = (int) Math.floor(cx + px * k), z = (int) Math.floor(cz + pz * k);
				if (seen.add(key(x, z))) {
					out.add(new int[] {x, z});
				}
			}
		}
		return out;
	}

	/** The block columns of the open square a ship needs to turn in at (x, z): every {@value #SAMPLE} blocks over ±{@value #BASIN}. */
	public static List<int[]> basin(double x, double z) {
		List<int[]> out = new ArrayList<>();
		int cx = (int) Math.floor(x), cz = (int) Math.floor(z);
		for (int i = -BASIN; i <= BASIN; i += SAMPLE) {
			for (int k = -BASIN; k <= BASIN; k += SAMPLE) {
				out.add(new int[] {cx + i, cz + k});
			}
		}
		return out;
	}

	/**
	 * The box (minX, minZ, maxX, maxZ, in blocks) enclosing a lane and the basin at its end, widened by
	 * {@value #CLAIM_MARGIN}: the area no claim, town or operator region may touch.
	 */
	public static double[] protectedReach(double fromX, double fromZ, double toX, double toZ) {
		double reach = Math.max(LANE_HALF_WIDTH, BASIN) + CLAIM_MARGIN;
		return new double[] {Math.min(fromX, toX) - reach, Math.min(fromZ, toZ) - reach, Math.max(fromX, toX) + reach,
				Math.max(fromZ, toZ) + reach};
	}

	private static long key(int x, int z) {
		return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
	}

	/** What the helm does this tick. */
	public enum Order {
		/** Steer for the waypoint (or choose one). */
		SAIL,
		/** Go astern, straight, at {@link #BACK_SPEED}. */
		BACK,
		/** Lie still. */
		REST
	}

	/**
	 * The helm's memory of refused steps (saved by the ship as three numbers): after {@value #STALL_TICKS} refused steps in
	 * a row it backs off astern for up to {@value #BACK_TICKS} ticks; if it can go neither ahead nor astern it rests
	 * {@value #REST_TICKS} ticks before it tries again. It never moves the ship by any other means.
	 */
	public static final class Helm {
		private int refused;
		private int backing;
		private int resting;

		public Helm() {
		}

		public Helm(int refused, int backing, int resting) {
			this.refused = Math.max(0, Math.min(STALL_TICKS, refused));
			this.backing = Math.max(0, Math.min(BACK_TICKS, backing));
			this.resting = Math.max(0, Math.min(REST_TICKS, resting));
		}

		/** This tick's order. */
		public Order order() {
			return resting > 0 ? Order.REST : backing > 0 ? Order.BACK : Order.SAIL;
		}

		/** After an order: whether its step was taken. Returns the next tick's order. */
		public Order after(Order order, boolean moved) {
			switch (order) {
				case REST -> resting--;
				case BACK -> {
					if (moved) {
						backing--;
					} else {
						// Astern is blocked too: lie still a while, then try ahead again.
						backing = 0;
						resting = REST_TICKS;
					}
				}
				case SAIL -> {
					if (moved) {
						refused = 0;
					} else if (++refused >= STALL_TICKS) {
						refused = 0;
						backing = BACK_TICKS;
					}
				}
			}
			return order();
		}

		/** Forgets any stall (the crew hove to, or let go again). */
		public void clear() {
			refused = backing = resting = 0;
		}

		public int refused() {
			return refused;
		}

		public int backing() {
			return backing;
		}

		public int resting() {
			return resting;
		}
	}
}
