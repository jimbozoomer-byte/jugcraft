package io.github.jimbozoomer.jugcraft.lair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Where the Glass Chapel's parts lie (docs/features/glass-chapel.md), relative to its template's corner, and the Abbot's
 * beam worked out from its mirrors: the numbers and the geometry of tools/glass_chapel.py, checked against it by
 * tools/check_mod_data.py. An instance's part is at {@code Lair.GLASS_CHAPEL.origin(slot)} offset by these.
 *
 * <p>The beam (part 2) runs in the plane of the floor. It leaves the Abbot's place aimed at the middle of a mirror, and
 * turns at the middle of each marked mirror it strikes, once, about the way that mirror's glass faces; a mirror that is
 * not marked is dark and stops it, as a mirror it has struck before does. A pillar stops it, and it fades at the walls and
 * at the rood screen. It comes home when it passes within {@value #HIT} of the Abbot's place after striking a mirror.
 */
public final class GlassChapel {
	/** The floor's top layer, and where anyone in the chapel stands. */
	public static final int PAVE = 2;
	public static final int STAND = 3;
	/** The chapel's middle (x and z, block corners). */
	public static final double CENTRE_X = 17.5;
	public static final double CENTRE_Z = 22.5;
	/** The nave between its walls (inclusive), the rood screen's row south of it, and the narthex beyond. */
	public static final int NAVE_WEST = 2;
	public static final int NAVE_EAST = 32;
	public static final int NAVE_NORTH = 9;
	public static final int NAVE_SOUTH = 37;
	public static final int ROOD_Z = 38;
	public static final int NARTHEX_NORTH = 39;
	public static final int NARTHEX_SOUTH = 43;
	/** The apse north of the nave (inclusive), its back corners cut off {@value #APSE_CHAMFER} cells. */
	public static final int APSE_WEST = 9;
	public static final int APSE_EAST = 25;
	public static final int APSE_NORTH = 2;
	public static final int APSE_SOUTH = 8;
	public static final int APSE_CHAMFER = 3;
	/** The Abbot's place (x and z, block corners): where he kneels, and where his beam leaves and comes home to. */
	public static final double ABBOT_X = 17.5;
	public static final double ABBOT_Z = 8.5;
	/** The lectern before him, north of his place. */
	public static final BlockPos LECTERN = new BlockPos(17, STAND, 7);
	/** The Grey Mist in the south door: three columns, three high from the floor. */
	public static final int EXIT_WEST = 16;
	public static final int EXIT_EAST = 18;
	public static final int EXIT_Z = 44;
	/** The four pillars: each {@value #PILLAR} cells square from its north-west cell. */
	public static final int[][] PILLARS = {{8, 10}, {25, 10}, {8, 29}, {25, 29}};
	public static final int PILLAR = 2;
	/** A mirror's angle steps: each is this many degrees. */
	public static final double ANGLE_STEP = 22.5;
	/** The beam's reach (x0, z0, x1, z1), how near it must pass a middle to strike it, and its most turns. */
	public static final double[] REACH = {2.0, 2.0, 33.0, 38.0};
	public static final double HIT = 0.75;
	public static final int BOUNCES = 4;

	/** The three Chapel Mirrors (tools/glass_chapel.py MIRRORS and STOPS). */
	public enum Mirror implements StringRepresentable {
		EAST(29, 20, 2, 4, 5, 7), SOUTH(17, 32, 3, 5, 7, 1), WEST(5, 20, 0, 3, 4, 6);

		/** A mirror stands at its first stop, a decoy, when an instance is placed and after the Abbot resets. */
		public static final int REST = 0;
		public static final int STOPS = 4;

		private final int x;
		private final int z;
		private final int[] steps;

		Mirror(int x, int z, int... steps) {
			this.x = x;
			this.z = z;
			this.steps = steps;
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}

		/** The mirror's name in words ("the east mirror"). */
		public Component display() {
			return Component.translatable("lair.jugcraft.glass_chapel.mirror." + getSerializedName());
		}

		/** The mirror's lower block, in the template. */
		public BlockPos cell() {
			return new BlockPos(x, STAND, z);
		}

		/** The mirror's lower block in the instance at {@code origin}. */
		public BlockPos base(BlockPos origin) {
			return origin.offset(cell());
		}

		public double middleX() {
			return x + 0.5;
		}

		public double middleZ() {
			return z + 0.5;
		}

		/** The angle step (0 to 7) the mirror faces at {@code stop}. */
		public int step(int stop) {
			return steps[Math.floorMod(stop, STOPS)];
		}

		/** The stop at which the mirror sends the beam from the Abbot's place on to {@code other}, or back. */
		public int stopFor(Mirror other) {
			int want = pairStep(this, other);
			for (int stop = 0; stop < STOPS; stop++) {
				if (steps[stop] == want) {
					return stop;
				}
			}
			throw new IllegalStateException(this + " has no stop for " + other);
		}
	}

	/** Where the beam went: its points (x and z, in the template), the mirrors it struck in order, and whether it came home. */
	public record Beam(List<double[]> points, List<Mirror> struck, boolean home) {
	}

	private GlassChapel() {
	}

	/** The chapel's middle, on its floor. */
	public static Vec3 centre() {
		return new Vec3(CENTRE_X, STAND, CENTRE_Z);
	}

	/** Where the Abbot kneels. */
	public static Vec3 abbot() {
		return new Vec3(ABBOT_X, STAND, ABBOT_Z);
	}

	/** Whether (x, z) (in the template) is in the nave, north of the rood screen. */
	public static boolean inNave(double x, double z) {
		return x >= NAVE_WEST && x < NAVE_EAST + 1 && z >= NAVE_NORTH && z < NAVE_SOUTH + 1;
	}

	/** Whether (x, z) (in the template) is in the apse, north of the nave, where the Abbot kneels. */
	public static boolean inApse(double x, double z) {
		int cx = (int) Math.floor(x);
		int cz = (int) Math.floor(z);
		if (cx < APSE_WEST || cx > APSE_EAST || cz < APSE_NORTH || cz > APSE_SOUTH) {
			return false;
		}
		int back = APSE_CHAMFER - (cz - APSE_NORTH);
		return !(back > 0 && (cx - APSE_WEST < back || APSE_EAST - cx < back));
	}

	/** Whether (x, z) (in the template) is in the arena: the nave or the apse. */
	public static boolean inArena(double x, double z) {
		return inNave(x, z) || inApse(x, z);
	}

	/** Whether (x, z) (in the template) is anywhere inside the chapel: the arena, the rood screen's row or the narthex. */
	public static boolean inside(double x, double z) {
		return inArena(x, z) || inNarthex(x, z) || x >= NAVE_WEST && x < NAVE_EAST + 1 && z >= ROOD_Z && z < ROOD_Z + 1;
	}

	/** Whether (x, z) (in the template) is in the narthex, the arrival, south of the rood screen. */
	public static boolean inNarthex(double x, double z) {
		return x >= NAVE_WEST && x < NAVE_EAST + 1 && z >= NARTHEX_NORTH && z < NARTHEX_SOUTH + 1;
	}

	/** The way a mirror at angle {@code step} faces (x and z): its glass's normal, by bearing clockwise from north. */
	public static double[] facing(int step) {
		double a = Math.toRadians(step * ANGLE_STEP);
		return new double[] {Math.sin(a), -Math.cos(a)};
	}

	private static double bearing(double dx, double dz) {
		return Math.floorMod(Math.round(Math.toDegrees(Math.atan2(dx, -dz)) * 1000.0), 360000L) / 1000.0;
	}

	/** The angle step at which {@code mirror} sends the beam from the Abbot's place on to {@code other}: half way between. */
	public static int pairStep(Mirror mirror, Mirror other) {
		double toAbbot = bearing(ABBOT_X - mirror.middleX(), ABBOT_Z - mirror.middleZ());
		double toOther = bearing(other.middleX() - mirror.middleX(), other.middleZ() - mirror.middleZ());
		double half = (toAbbot + toOther) / 2.0;
		if (Math.abs(toAbbot - toOther) > 180.0) {
			half += 180.0;
		}
		return (int) Math.floorMod(Math.round((half % 180.0) / ANGLE_STEP), 8L);
	}

	/**
	 * The beam with the mirrors at {@code stops} and the mirrors in {@code marked} lit, aimed from the Abbot's place at the
	 * middle of {@code first} (as tools/glass_chapel.py trace).
	 */
	public static Beam trace(Map<Mirror, Integer> stops, Collection<Mirror> marked, Mirror first) {
		double px = ABBOT_X;
		double pz = ABBOT_Z;
		double length = Math.hypot(first.middleX() - px, first.middleZ() - pz);
		double dx = (first.middleX() - px) / length;
		double dz = (first.middleZ() - pz) / length;
		List<double[]> points = new ArrayList<>();
		List<Mirror> struck = new ArrayList<>();
		points.add(new double[] {px, pz});
		Mirror leaving = null;
		for (int turn = 0; turn <= BOUNCES; turn++) {
			double best = exit(px, pz, dx, dz);
			Mirror mirror = null;
			boolean abbot = false;
			boolean pillar = false;
			for (Mirror each : Mirror.values()) {
				if (each == leaving) {
					continue;
				}
				double t = passes(px, pz, dx, dz, each.middleX(), each.middleZ());
				if (t >= 0.0 && t < best) {
					best = t;
					mirror = each;
				}
			}
			if (!struck.isEmpty()) {
				double t = passes(px, pz, dx, dz, ABBOT_X, ABBOT_Z);
				if (t >= 0.0 && t < best) {
					best = t;
					mirror = null;
					abbot = true;
				}
			}
			for (int[] corner : PILLARS) {
				double t = boxHit(px, pz, dx, dz, corner[0], corner[1], corner[0] + PILLAR, corner[1] + PILLAR);
				if (t >= 0.0 && t < best) {
					best = t;
					mirror = null;
					abbot = false;
					pillar = true;
				}
			}
			if (mirror != null && marked.contains(mirror) && !struck.contains(mirror)) {
				px = mirror.middleX();
				pz = mirror.middleZ();
				points.add(new double[] {px, pz});
				struck.add(mirror);
				double[] n = facing(mirror.step(stops.getOrDefault(mirror, Mirror.REST)));
				double dot = dx * n[0] + dz * n[1];
				dx -= 2.0 * dot * n[0];
				dz -= 2.0 * dot * n[1];
				leaving = mirror;
				continue;
			}
			if (mirror != null) {
				struck.add(mirror);
				points.add(new double[] {mirror.middleX(), mirror.middleZ()});
				return new Beam(points, struck, false);
			}
			points.add(abbot ? new double[] {ABBOT_X, ABBOT_Z} : new double[] {px + best * dx, pz + best * dz});
			return new Beam(points, struck, abbot && !pillar);
		}
		return new Beam(points, struck, false);
	}

	/** Whether the beam, with the two mirrors of a pair marked and aimed at either, strikes both and comes home. */
	public static boolean comesHome(Map<Mirror, Integer> stops, Mirror a, Mirror b) {
		List<Mirror> marked = List.of(a, b);
		for (Mirror first : marked) {
			Beam beam = trace(stops, marked, first);
			if (beam.home() && beam.struck().contains(a) && beam.struck().contains(b)) {
				return true;
			}
		}
		return false;
	}

	private static double exit(double px, double pz, double dx, double dz) {
		double out = Double.MAX_VALUE;
		if (dx > 1e-12) {
			out = Math.min(out, (REACH[2] - px) / dx);
		} else if (dx < -1e-12) {
			out = Math.min(out, (REACH[0] - px) / dx);
		}
		if (dz > 1e-12) {
			out = Math.min(out, (REACH[3] - pz) / dz);
		} else if (dz < -1e-12) {
			out = Math.min(out, (REACH[1] - pz) / dz);
		}
		return out;
	}

	/** How far along the ray it passes within {@value #HIT} of (cx, cz), ahead of its start, or -1. */
	private static double passes(double px, double pz, double dx, double dz, double cx, double cz) {
		double t = (cx - px) * dx + (cz - pz) * dz;
		if (t <= 1e-9 || Math.hypot(px + t * dx - cx, pz + t * dz - cz) > HIT) {
			return -1.0;
		}
		return t;
	}

	/** How far along the ray it enters the box, or -1. */
	private static double boxHit(double px, double pz, double dx, double dz, double x0, double z0, double x1, double z1) {
		double lo = 0.0;
		double hi = Double.MAX_VALUE;
		double[][] axes = {{px, dx, x0, x1}, {pz, dz, z0, z1}};
		for (double[] axis : axes) {
			if (Math.abs(axis[1]) < 1e-12) {
				if (axis[0] < axis[2] || axis[0] > axis[3]) {
					return -1.0;
				}
			} else {
				double t0 = (axis[2] - axis[0]) / axis[1];
				double t1 = (axis[3] - axis[0]) / axis[1];
				lo = Math.max(lo, Math.min(t0, t1));
				hi = Math.min(hi, Math.max(t0, t1));
			}
		}
		return lo <= hi && lo > 1e-9 ? lo : -1.0;
	}

	/** Each mirror's stop in the instance at {@code origin}, read from its lower block (rest where it is missing). */
	public static Map<Mirror, Integer> stops(Level level, BlockPos origin) {
		Map<Mirror, Integer> out = new EnumMap<>(Mirror.class);
		for (Mirror mirror : Mirror.values()) {
			BlockState state = level.getBlockState(mirror.base(origin));
			out.put(mirror, state.getBlock() instanceof ChapelMirrorBlock ? state.getValue(ChapelMirrorBlock.STOP) : Mirror.REST);
		}
		return out;
	}

	/** The cells of the Grey Mist in the south door. */
	public static List<BlockPos> exitCells() {
		List<BlockPos> out = new ArrayList<>();
		for (int y = STAND; y < STAND + 3; y++) {
			for (int x = EXIT_WEST; x <= EXIT_EAST; x++) {
				out.add(new BlockPos(x, y, EXIT_Z));
			}
		}
		return out;
	}
}
