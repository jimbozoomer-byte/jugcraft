package io.github.jimbozoomer.jugcraft.menagerie;

import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.server.OperatorRegions;
import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.walker.WalkerAim;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
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
 * superset, so a ship turned 45 degrees asks for a little more open water than it covers.
 */
public final class PirateWaters {
	/** Half the ship's beam and half its length, in blocks (the hull is 6 x 16: PirateCrew's frame). */
	public static final double HALF_BEAM = 3.0;
	public static final double HALF_LENGTH = 8.0;
	/** A hull corner's distance from the centre: the most a point of the hull moves for each radian it turns. */
	public static final double CORNER = Math.sqrt(HALF_BEAM * HALF_BEAM + HALF_LENGTH * HALF_LENGTH);
	/** Blocks of water the keel needs under the waterline block, and the clear air the masts need above it. */
	public static final int DEPTH = 2;
	public static final int MAST = 13;
	/** How far up its top water block a ship's waterline sits (a source block's surface is at 8/9). */
	public static final double WATERLINE = 0.875;

	private PirateWaters() {
	}

	/**
	 * Whether a ship may be at {@code at} facing {@code yaw}: its footprint inside the world border and every chunk under it
	 * loaded; under it, the waterline block and {@value #DEPTH} more are water with nothing solid (kelp and seagrass pass,
	 * ice and blocks do not); above it, {@value #MAST} blocks clear of anything solid or liquid; no block entity anywhere
	 * in that volume; and nothing in that volume protected ({@link #unprotected}).
	 */
	public static boolean clear(ServerLevel level, Vec3 at, float yaw) {
		int surface = Mth.floor(at.y);
		return clearBox(level, footprint(at, yaw, surface - DEPTH, surface + MAST + 1), surface);
	}

	/**
	 * Whether the hull may pass from {@code from} at {@code fromYaw} to {@code to} at {@code toYaw} in one step: the box
	 * enclosing both footprints, widened by the farthest a hull corner can swing between the two yaws (its distance from
	 * the centre times the turn in radians), must be clear by {@link #clear}'s rules. So a turn whose stern or bow would
	 * sweep into a bank, a block or protected water is refused even where the water ahead is open.
	 */
	public static boolean sweep(ServerLevel level, Vec3 from, float fromYaw, Vec3 to, float toYaw) {
		int surface = Mth.floor(to.y);
		double swing = CORNER * Math.abs(Mth.wrapDegrees(toYaw - fromYaw)) * Mth.DEG_TO_RAD;
		AABB box = footprint(from, fromYaw, surface - DEPTH, surface + MAST + 1)
				.minmax(footprint(to, toYaw, surface - DEPTH, surface + MAST + 1)).inflate(swing, 0.0, swing);
		return clearBox(level, box, surface);
	}

	/** {@link #clear} for any box over the waterline block {@code surface}: every block of every column in it. */
	static boolean clearBox(ServerLevel level, AABB box, int surface) {
		if (!level.getWorldBorder().isWithinBounds(box) || !loaded(level, box) || surface - DEPTH < level.getMinY()
				|| surface + MAST > level.getMaxY()) {
			return false;
		}
		int x0 = Mth.floor(box.minX), x1 = Mth.floor(box.maxX - 1.0E-7);
		int z0 = Mth.floor(box.minZ), z1 = Mth.floor(box.maxZ - 1.0E-7);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				for (int y = surface - DEPTH; y <= surface + MAST; y++) {
					pos.set(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (state.hasBlockEntity() || !state.getCollisionShape(level, pos).isEmpty()) {
						return false;
					}
					boolean water = state.getFluidState().is(FluidTags.WATER);
					if (y <= surface ? !water : !state.getFluidState().isEmpty()) {
						return false;
					}
				}
			}
		}
		return unprotected(level, box);
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
		double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
		for (int sx = -1; sx <= 1; sx += 2) {
			for (int sz = -1; sz <= 1; sz += 2) {
				Vec3 corner = new Vec3(sx * HALF_BEAM, 0.0, sz * HALF_LENGTH).yRot(-yaw * Mth.DEG_TO_RAD);
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
		if (!level.hasChunkAt(at) || !level.getFluidState(at).is(FluidTags.WATER) || !level.getBlockState(at.above()).isAir()) {
			return null;
		}
		return new Vec3(x, top + WATERLINE, z);
	}
}
