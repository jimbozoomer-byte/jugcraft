package io.github.jimbozoomer.jugcraft.raiders;

import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.town.TownData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Where a raid comes at the walled town (docs/features/raiders.md, "City approaches and visible fights"): on purpose, at a
 * gate or at a stretch of wall the guards can see, never at whatever corner of the wall happens to lie nearest.
 *
 * <ul>
 * <li><b>Gates first.</b> The three authored gates ({@link CityRaidRoute#gates}) are what a raid makes for. Each has a
 * forecourt {@value #FORECOURT} blocks out from its passage, on the road, where siege walkers (too tall for the raised
 * portcullis) hold and fight in sight of the gate's guards; everything else marches on through the passage.</li>
 * <li><b>Wall sections in sight of a guard post.</b> The middle of each of the wall's {@value #SECTIONS} straight runs,
 * halfway between two towers and so never at one, {@value #WALL_STANDOFF} blocks out from the wall's face. A section is
 * kept only when some guard's post (a gate or square guard, a guard walker, a flak crew, or a gate's passage or inner
 * road) has a clear line to it through the town's blueprint, within {@value #POST_SIGHT} blocks; the blind stretches
 * (the north wall, which has no gate) are never approaches.</li>
 * <li><b>Choosing.</b> A raider's first approach is always the nearest gate. Once one has failed it (a player's wall
 * across it, say), it takes the nearest approach it has not given up, a wall section counting {@value #WALL_PENALTY}
 * blocks further away than it is: so it tries the section beside the failed gate, in sight of the same gate's guards,
 * before the long march round to another gate.</li>
 * </ul>
 *
 * <p>Routes go round the outside on a ring {@value #RING} blocks from the middle rather than diagonally across the
 * curtain wall, and keep west of the Styx estate, exactly as {@link CityRaidRoute#approach} does for its nearest gate.
 * Everything here is worked out from the town's own data: it never edits a block and never loads a chunk.
 */
public final class CityRaidApproach {
	/** How far out from a gate's passage its forecourt lies, in blocks: on the road, past the town's protected margin. */
	public static final int FORECOURT = 10;
	/** How far out from the wall's outer face a wall section's front lies, in blocks. */
	public static final int WALL_STANDOFF = 14;
	/** The ring round the outside that routes follow, in blocks from the town's middle (a gate's outside point is on it). */
	public static final double RING = 92;
	/** How much further than it is a wall section counts when choosing, so gates come first. */
	public static final int WALL_PENALTY = 48;
	/** How far a guard's post can see a wall section from, in blocks. */
	public static final int POST_SIGHT = 64;
	/** The wall's straight runs (tools/town.py VERTICES): a section is the middle of each. */
	public static final int SECTIONS = 16;
	/** The step of a line of sight through the blueprint, in blocks. */
	private static final double SIGHT_STEP = 0.1;

	/** A gate, or a stretch of wall in sight of a guard post. */
	public enum Kind {
		GATE, WALL
	}

	/**
	 * One approach: its index (the gates 0 to 2 in {@link CityRaidRoute#gates} order, then the wall sections kept), its
	 * kind, its bearing from the town's middle (radians: 0 east, a quarter turn south), where raiders hold and fight there
	 * ({@code front}), where they leave the ring for it ({@code staging}) and, for a gate, the gate.
	 */
	public record Approach(int index, Kind kind, double bearing, BlockPos front, BlockPos staging, CityRaidRoute.@Nullable Gate entrance) {
		public boolean isGate() {
			return kind == Kind.GATE;
		}
	}

	private record Plan(BlockPos origin, List<Approach> approaches) {
	}

	private static volatile @Nullable Plan plan;

	private CityRaidApproach() {
	}

	/** The town's approaches, gates first (worked out once per town origin, from the blueprint). */
	public static List<Approach> approaches(BlockPos origin) {
		Plan cached = plan;
		if (cached != null && cached.origin().equals(origin)) {
			return cached.approaches();
		}
		List<Approach> list = build(origin, TownData.get());
		plan = new Plan(origin.immutable(), list);
		return list;
	}

	static List<Approach> build(BlockPos origin, TownData data) {
		List<Approach> list = new ArrayList<>();
		for (CityRaidRoute.Gate gate : CityRaidRoute.gates(origin)) {
			BlockPos front = gate.passage().offset(gate.dx() * FORECOURT, 0, gate.dz() * FORECOURT);
			list.add(new Approach(list.size(), Kind.GATE, Math.atan2(gate.dz(), gate.dx()), front, gate.outside(), gate));
		}
		List<Vec3> posts = posts(data);
		int half = data.size / 2;
		for (int k = 0; k < SECTIONS; k++) {
			double bearing = 2 * Math.PI * (k + 0.5) / SECTIONS;
			double face = face(data, bearing);
			if (face < 0) {
				continue;
			}
			double cos = Math.cos(bearing);
			double sin = Math.sin(bearing);
			int fx = (int) Math.floor(half + cos * (face + WALL_STANDOFF));
			int fz = (int) Math.floor(half + sin * (face + WALL_STANDOFF));
			// A raider's middle, a block over the town's ground (town coordinates).
			Vec3 aim = new Vec3(fx + 0.5, 2.0, fz + 0.5);
			if (!openGround(data, fx, fz) || posts.stream().noneMatch(p -> p.distanceTo(aim) <= POST_SIGHT && sight(data, p, aim))) {
				continue;
			}
			double ring = Math.max(RING, face + WALL_STANDOFF + 4);
			BlockPos front = origin.offset(fx, 1, fz);
			BlockPos staging = origin.offset((int) Math.floor(half + cos * ring), 1, (int) Math.floor(half + sin * ring));
			list.add(new Approach(list.size(), Kind.WALL, bearing, front, staging, null));
		}
		return List.copyOf(list);
	}

	/**
	 * Where the guards keep watch, in town coordinates at eye height: every guard's, guard walker's and flak crew's post
	 * from the blueprint, and each gate's passage and inner road.
	 */
	public static List<Vec3> posts(TownData data) {
		List<Vec3> posts = new ArrayList<>();
		for (TownData.Spot spot : data.spots) {
			double eye = switch (spot.role()) {
				case "guard" -> 1.62;
				case "mecha_guard" -> 2.3;
				case "flak_guard" -> 2.0;
				default -> -1;
			};
			if (eye > 0) {
				posts.add(new Vec3(spot.pos().getX() + 0.5, spot.pos().getY() + eye, spot.pos().getZ() + 0.5));
			}
		}
		for (CityRaidRoute.Gate gate : CityRaidRoute.gates(BlockPos.ZERO)) {
			posts.add(Vec3.atBottomCenterOf(gate.passage()).add(0, 1.62, 0));
			posts.add(Vec3.atBottomCenterOf(gate.inside()).add(0, 1.62, 0));
		}
		return posts;
	}

	/** How far out from the town's middle the wall's outer face is on a bearing (the first column not inside), or -1. */
	static double face(TownData data, double bearing) {
		int half = data.size / 2;
		for (double r = 30; r < data.size; r += 0.25) {
			int x = (int) Math.floor(half + Math.cos(bearing) * r);
			int z = (int) Math.floor(half + Math.sin(bearing) * r);
			if (data.mask(x, z) != TownData.INSIDE) {
				return r;
			}
		}
		return -1;
	}

	/** Whether a column outside the wall is open at standing height in the blueprint (the land's own blocks count as open). */
	public static boolean openGround(TownData data, int x, int z) {
		if (data.mask(x, z) == TownData.INSIDE) {
			return false;
		}
		for (int y = 1; y <= 3; y++) {
			if (blocks(data, x, y, z)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Whether the blueprint has a clear line between two points (town coordinates). Strict: every authored block that is
	 * not air blocks it, even a slab or a fence, so a line clear here is clear in the world too (bar what players build).
	 */
	public static boolean sight(TownData data, Vec3 from, Vec3 to) {
		Vec3 delta = to.subtract(from);
		int steps = Math.max(1, (int) Math.ceil(delta.length() / SIGHT_STEP));
		for (int i = 1; i < steps; i++) {
			double t = i / (double) steps;
			if (blocks(data, (int) Math.floor(from.x + delta.x * t), (int) Math.floor(from.y + delta.y * t), (int) Math.floor(from.z + delta.z * t))) {
				return false;
			}
		}
		return true;
	}

	/** Whether the blueprint puts a block here: an authored block that is not air, or the ground (the land's own, at or under 0). */
	private static boolean blocks(TownData data, int x, int y, int z) {
		int index = data.index(x, y, z);
		return index == TownData.KEEP ? y <= 0 : !data.state(index).isAir();
	}

	/**
	 * The approach to make for from {@code from}: with nothing in {@code failed}, the nearest gate; otherwise the nearest
	 * not in {@code failed}, a wall section counting {@value #WALL_PENALTY} blocks further than it is. Null once every
	 * approach has failed.
	 */
	public static @Nullable Approach choose(BlockPos origin, Vec3 from, Collection<Integer> failed) {
		Approach best = null;
		double cost = Double.MAX_VALUE;
		for (Approach approach : approaches(origin)) {
			if (failed.contains(approach.index()) || failed.isEmpty() && !approach.isGate()) {
				continue;
			}
			double c = Math.sqrt(Vec3.atBottomCenterOf(approach.staging()).subtract(from).horizontalDistanceSqr())
					+ (approach.isGate() ? 0 : WALL_PENALTY);
			if (c < cost) {
				cost = c;
				best = approach;
			}
		}
		return best;
	}

	/** The approach whose front is nearest {@code at}: the one a raider there is attacking. */
	public static Approach nearest(BlockPos origin, Vec3 at) {
		Approach best = null;
		double distance = Double.MAX_VALUE;
		for (Approach approach : approaches(origin)) {
			double d = Vec3.atBottomCenterOf(approach.front()).subtract(at).horizontalDistanceSqr();
			if (d < distance) {
				distance = d;
				best = approach;
			}
		}
		return best;
	}

	/** The nearest gate to {@code at} as an approach. */
	public static Approach nearestGate(BlockPos origin, Vec3 at) {
		Approach best = null;
		double distance = Double.MAX_VALUE;
		for (Approach approach : approaches(origin)) {
			double d = Vec3.atBottomCenterOf(approach.front()).subtract(at).horizontalDistanceSqr();
			if (approach.isGate() && d < distance) {
				distance = d;
				best = approach;
			}
		}
		return best;
	}

	/**
	 * The way from {@code from} to an approach: round the ring outside the wall to its bearing, then for a gate to its
	 * outside point and through its passage to its inner road ({@code tall} raiders, which cannot pass under the
	 * portcullis, stop at the forecourt instead), and for a wall section to its staging point and its front. A raider
	 * already inside the wall goes straight to the gate's inner road (or nowhere, for a wall section).
	 */
	public static List<BlockPos> route(BlockPos origin, Vec3 from, Approach approach, boolean tall) {
		CityRaidRoute.Gate gate = approach.entrance();
		int lx = (int) Math.floor(from.x) - origin.getX();
		int lz = (int) Math.floor(from.z) - origin.getZ();
		if (TownData.get().mask(lx, lz) == TownData.INSIDE) {
			return gate != null && !tall ? List.of(gate.inside()) : List.of();
		}
		Vec3 c = Vec3.atBottomCenterOf(Town.centre(origin));
		int y = origin.getY() + 1;
		List<BlockPos> route = new ArrayList<>();
		// The Styx estate starts at local X=192 and occupies Z=52..100 (as CityRaidRoute.approach): keep west of it, and
		// go round its perimeter from beyond it.
		double approachX = from.x;
		double approachZ = from.z;
		if (from.x > origin.getX() + 188) {
			double low = origin.getZ() + 44;
			double high = origin.getZ() + 108;
			boolean outsideEstate = from.x >= origin.getX() + 259 || from.z < origin.getZ() + 52 || from.z > origin.getZ() + 100;
			if (outsideEstate) {
				approachZ = from.z < (low + high) / 2 ? Math.min(from.z, low) : Math.max(from.z, high);
				route.add(BlockPos.containing(from.x, y, approachZ));
				approachX = origin.getX() + 188;
				route.add(BlockPos.containing(approachX, y, approachZ));
			}
		}
		double start = Math.atan2(approachZ - c.z, approachX - c.x);
		double arc = Math.atan2(Math.sin(approach.bearing() - start), Math.cos(approach.bearing() - start));
		int steps = Math.max(1, (int) Math.ceil(Math.abs(arc) / .18));
		for (int i = 0; i <= steps; i++) {
			double a = start + arc * i / steps;
			route.add(BlockPos.containing(c.x + Math.cos(a) * RING, y, c.z + Math.sin(a) * RING));
		}
		if (gate != null) {
			route.add(gate.outside());
			if (tall) {
				route.add(approach.front());
			} else {
				route.add(gate.passage());
				route.add(gate.inside());
			}
		} else {
			route.add(approach.staging());
			route.add(approach.front());
		}
		return List.copyOf(route);
	}
}
