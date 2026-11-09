package io.github.jimbozoomer.jugcraft.menagerie;

import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.server.OperatorRegions;
import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.walker.WalkerAim;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Where a pirate ship may be (docs/features/pirate-ships.md): open, deep, loaded water away from anyone's land. A ship's
 * place is its entity position, at the waterline (y in the top water block), and its yaw. Every check reads loaded chunks
 * only and loads none, and none of them changes the world.
 *
 * <p>The ship's footprint is its 6 x 16 hull turned to its yaw, enclosed by an axis-aligned box: a conservative
 * superset, so a ship turned 45 degrees asks for a little more open water than it covers. Above the water the wider rig's
 * box is used, from the waterline row up (the bow's stem rises through the top of the waterline row a little ahead of
 * the hull's 16 blocks).
 */
public final class PirateWaters {
	/** Half the ship's beam and half its length, in blocks (the hull is 6 x 16: PirateCrew's frame). */
	public static final double HALF_BEAM = 3.0;
	public static final double HALF_LENGTH = 8.0;
	/** A hull corner's distance from the centre: the most a point of the hull moves for each radian it turns. */
	public static final double CORNER = Math.sqrt(HALF_BEAM * HALF_BEAM + HALF_LENGTH * HALF_LENGTH);
	/**
	 * The rig above the water, wider and longer than the hull (rails, bowsprit, rudder, stern lantern, sails at their
	 * sway; tools/pirate_ship.py's model, rounded outward): half its breadth, its stern and bow ends (ship-local z), and
	 * its corner's greatest distance from the centre. Its height is {@link #MAST} blocks above the waterline block.
	 */
	public static final double RIG_HALF_BEAM = 4.0;
	public static final double RIG_STERN = -9.5;
	public static final double RIG_BOW = 11.75;
	public static final double RIG_CORNER = Math.sqrt(RIG_HALF_BEAM * RIG_HALF_BEAM + Math.max(RIG_STERN * RIG_STERN, RIG_BOW * RIG_BOW));
	/** Blocks of water the keel needs under the waterline block, and the clear air the masts need above it. */
	public static final int DEPTH = 2;
	/** How far the keel reaches under the waterline (tools/pirate_ship.py's keel_y), for a sinking ship's checks. */
	public static final double KEEL = 1.625;
	public static final int MAST = 13;
	/** How far up its top water block a ship's waterline sits (a source block's surface is at 8/9). */
	public static final double WATERLINE = 0.875;

	private PirateWaters() {
	}

	/**
	 * Whether a ship may be at {@code at} facing {@code yaw}. Two volumes, each turned to the yaw: the hull under the
	 * water (6 x 16: the waterline block and {@value #DEPTH} more must be {@link #openWater}: plain water or a natural
	 * water plant, never ice, a block or any other waterlogged block) and the rig ({@link #rigFootprint}): in the waterline
	 * row open water or air, and the {@value #MAST} blocks above it air and nothing else. Both inside the world border,
	 * every chunk under them loaded, and nothing in either protected ({@link #unprotected}).
	 */
	public static boolean clear(ServerLevel level, Vec3 at, float yaw) {
		int surface = Mth.floor(at.y);
		return clearVolumes(level, footprint(at, yaw, surface - DEPTH, surface + 1),
				rigFootprint(at, yaw, surface, surface + MAST + 1), surface);
	}

	/**
	 * Whether the ship may pass from {@code from} at {@code fromYaw} to {@code to} at {@code toYaw} in one step: for the
	 * hull and for the rig, the box enclosing both positions' footprints, widened by the farthest a corner of it swings
	 * between the two yaws (its distance from the centre times the turn in radians), must be clear by {@link #clear}'s
	 * rules. So a turn whose stern, bowsprit or rail would sweep into a bank, a bridge, a block or protected water or air
	 * is refused even where the water ahead is open.
	 */
	public static boolean sweep(ServerLevel level, Vec3 from, float fromYaw, Vec3 to, float toYaw) {
		int surface = Mth.floor(to.y);
		double turn = Math.abs(Mth.wrapDegrees(toYaw - fromYaw)) * Mth.DEG_TO_RAD;
		double hullSwing = CORNER * turn, rigSwing = RIG_CORNER * turn;
		AABB hull = footprint(from, fromYaw, surface - DEPTH, surface + 1)
				.minmax(footprint(to, toYaw, surface - DEPTH, surface + 1)).inflate(hullSwing, 0.0, hullSwing);
		AABB rig = rigFootprint(from, fromYaw, surface, surface + MAST + 1)
				.minmax(rigFootprint(to, toYaw, surface, surface + MAST + 1)).inflate(rigSwing, 0.0, rigSwing);
		return clearVolumes(level, hull, rig, surface);
	}

	/**
	 * {@link #clear}'s rules for a hull box (its blocks from {@code surface - DEPTH} to {@code surface} open water) and a
	 * rig box (from {@code surface}: the waterline row open water or air, then air up to {@code surface + MAST}), every
	 * block of every column in each.
	 */
	static boolean clearVolumes(ServerLevel level, AABB hull, AABB rig, int surface) {
		AABB both = hull.minmax(rig);
		if (!level.getWorldBorder().isWithinBounds(both) || !loaded(level, both) || surface - DEPTH < level.getMinY()
				|| surface + MAST > level.getMaxY()) {
			return false;
		}
		return blocks(level, hull, surface - DEPTH, surface, Room.WATER) && blocks(level, rig, surface, surface, Room.WATER_OR_AIR)
				&& blocks(level, rig, surface + 1, surface + MAST, Room.AIR) && unprotected(level, hull) && unprotected(level, rig);
	}

	/** What a block of a volume may be. */
	enum Room {
		/** {@link #openWater} only (the hull under the waterline). */
		WATER,
		/** Open water or air (the rig's waterline row; a sinking ship's way down). */
		WATER_OR_AIR,
		/** Air only (the rig above the water): no torch, rail, flower, vine or fire either. */
		AIR;

		boolean admits(BlockState state) {
			return switch (this) {
				case WATER -> openWater(state);
				case WATER_OR_AIR -> state.isAir() || openWater(state);
				case AIR -> state.isAir();
			};
		}
	}

	/**
	 * Water a hull may float in: plain water, still or flowing, a bubble column (natural over magma and soul sand), or a
	 * natural water plant (kelp, seagrass). Never any other waterlogged block (a slab, a fence, a rail, a sign).
	 */
	public static boolean openWater(BlockState state) {
		return state.is(Blocks.WATER) || state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT) || state.is(Blocks.SEAGRASS)
				|| state.getBlock() instanceof TallSeagrassBlock || state.getBlock() instanceof BubbleColumnBlock;
	}

	/** Every block of {@code box}'s columns from {@code low} to {@code high} is one {@code room} admits. */
	private static boolean blocks(ServerLevel level, AABB box, int low, int high, Room room) {
		int x0 = Mth.floor(box.minX), x1 = Mth.floor(box.maxX - 1.0E-7);
		int z0 = Mth.floor(box.minZ), z1 = Mth.floor(box.maxZ - 1.0E-7);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				for (int y = low; y <= high; y++) {
					if (!room.admits(level.getBlockState(pos.set(x, y, z)))) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Whether a sinking ship whose waterline was in row {@code surface}, at {@code at} facing {@code yaw}, may settle to
	 * height {@code nextY}: no lower than the hull's checked depth ({@code surface - DEPTH}), and the rig's box from the
	 * keel's new row ({@value #KEEL} under {@code nextY}) up to the waterline row only open water or air (never terrain or
	 * a block), loaded, inside the border, and, keel to masthead, unprotected. Loads nothing.
	 */
	public static boolean maySink(ServerLevel level, Vec3 at, float yaw, double nextY, int surface) {
		if (nextY < surface - DEPTH) {
			return false;
		}
		int keel = Mth.floor(nextY - KEEL);
		AABB box = rigFootprint(at, yaw, keel, surface + MAST + 1);
		if (keel < level.getMinY() || !level.getWorldBorder().isWithinBounds(box) || !loaded(level, box)) {
			return false;
		}
		return blocks(level, box, keel, surface, Room.WATER_OR_AIR) && unprotected(level, box);
	}

	/**
	 * Whether no part of {@code box} (its blocks, every height included) is protected: no land claim on any chunk it
	 * touches (claims are whole chunk columns), no operator region sharing a block with it (an exact cuboid test, so a
	 * one-block region or one at bridge height over the water counts), and no town-protected column within the town's
	 * protected heights (every column of the box is tested at a height inside both). Saved data only: no chunk is read.
	 */
	public static boolean unprotected(ServerLevel level, AABB box) {
		int x0 = Mth.floor(box.minX), x1 = Mth.floor(box.maxX - 1.0E-7);
		int y0 = Mth.floor(box.minY), y1 = Mth.floor(box.maxY - 1.0E-7);
		int z0 = Mth.floor(box.minZ), z1 = Mth.floor(box.maxZ - 1.0E-7);
		for (int cx = x0 >> 4; cx <= x1 >> 4; cx++) {
			for (int cz = z0 >> 4; cz <= z1 >> 4; cz++) {
				// An owner of null may build only where nobody has claimed (LandRegistry.mayBuildFor).
				if (!LandRegistry.mayBuildFor(null, level, new BlockPos(cx << 4, y0, cz << 4))) {
					return false;
				}
			}
		}
		if (OperatorRegions.intersects(level, x0, y0, z0, x1, y1, z1)) {
			return false;
		}
		BlockPos origin = Town.origin(level);
		if (origin != null) {
			int low = Math.max(y0, origin.getY() - Town.PROTECT_BELOW);
			int high = Math.min(y1, origin.getY() + Town.PROTECT_ABOVE);
			if (low <= high) {
				BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
				for (int x = x0; x <= x1; x++) {
					for (int z = z0; z <= z1; z++) {
						if (Town.isProtected(level, pos.set(x, low, z))) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * The axis-aligned box enclosing the 6 x 16 hull at {@code at} turned to {@code yaw}, from {@code bottom} to {@code top}
	 * (world heights). Exact at multiples of 90 degrees, a superset otherwise.
	 */
	public static AABB footprint(Vec3 at, float yaw, double bottom, double top) {
		return turnedBox(at, yaw, HALF_BEAM, -HALF_LENGTH, HALF_LENGTH, bottom, top);
	}

	/**
	 * The axis-aligned box enclosing the above-water rig ({@link #RIG_HALF_BEAM}, {@link #RIG_STERN} to {@link #RIG_BOW})
	 * at {@code at} turned to {@code yaw}, from {@code bottom} to {@code top}. Exact at multiples of 90 degrees.
	 */
	public static AABB rigFootprint(Vec3 at, float yaw, double bottom, double top) {
		return turnedBox(at, yaw, RIG_HALF_BEAM, RIG_STERN, RIG_BOW, bottom, top);
	}

	private static AABB turnedBox(Vec3 at, float yaw, double halfBeam, double stern, double bow, double bottom, double top) {
		double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
		for (int sx = -1; sx <= 1; sx += 2) {
			for (double z : new double[] {stern, bow}) {
				Vec3 corner = new Vec3(sx * halfBeam, 0.0, z).yRot(-yaw * Mth.DEG_TO_RAD);
				minX = Math.min(minX, corner.x);
				maxX = Math.max(maxX, corner.x);
				minZ = Math.min(minZ, corner.z);
				maxZ = Math.max(maxZ, corner.z);
			}
		}
		return new AABB(at.x + minX, bottom, at.z + minZ, at.x + maxX, top, at.z + maxZ);
	}

	/** Whether every chunk {@code box} spans is loaded, so reading its blocks loads none ({@link WalkerAim#loadedIn}). */
	public static boolean loaded(ServerLevel level, AABB box) {
		return WalkerAim.loadedIn(level, box);
	}

	/**
	 * The waterline place for a ship over column (x, z): the top water block at the level's sea level, or null where the
	 * sea-level block is not water with air above it (land, ice, a river bank above the sea). Loaded chunks only.
	 */
	public static @Nullable Vec3 waterline(ServerLevel level, double x, double z) {
		int top = level.getSeaLevel() - 1;
		BlockPos at = BlockPos.containing(x, top, z);
		if (!level.hasChunkAt(at) || !openWater(level.getBlockState(at)) || !level.getBlockState(at.above()).isAir()) {
			return null;
		}
		return new Vec3(x, top + WATERLINE, z);
	}
}
