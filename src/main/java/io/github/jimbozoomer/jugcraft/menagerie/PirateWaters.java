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
	/** Blocks of water the keel needs under the waterline block, and the clear air the masts need above it. */
	public static final int DEPTH = 2;
	public static final int MAST = 13;
	/** How far up its top water block a ship's waterline sits (a source block's surface is at 8/9). */
	public static final double WATERLINE = 0.875;
	/** Blocks between protection samples across the footprint (towns and operator regions extend past their walls). */
	private static final int SAMPLE = 4;

	private PirateWaters() {
	}

	/**
	 * Whether a ship may be at {@code at} facing {@code yaw}: its footprint inside the world border and every chunk under it
	 * loaded; under it, the
	 * waterline block and {@value #DEPTH} more are water with nothing solid (kelp and seagrass pass, ice and blocks do
	 * not); above it, {@value #MAST} blocks clear of anything solid or liquid; no block entity anywhere in that volume;
	 * and no town, operator region or land claim under any part of it.
	 */
	public static boolean clear(ServerLevel level, Vec3 at, float yaw) {
		int surface = Mth.floor(at.y);
		AABB box = footprint(at, yaw, surface - DEPTH, surface + MAST + 1);
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
		return unprotected(level, box, surface);
	}

	/** Whether no town, operator region or land claim lies under {@code box} (loaded chunks only; the caller checked). */
	static boolean unprotected(ServerLevel level, AABB box, int y) {
		int x0 = Mth.floor(box.minX), x1 = Mth.floor(box.maxX - 1.0E-7);
		int z0 = Mth.floor(box.minZ), z1 = Mth.floor(box.maxZ - 1.0E-7);
		for (int cx = x0 >> 4; cx <= x1 >> 4; cx++) {
			for (int cz = z0 >> 4; cz <= z1 >> 4; cz++) {
				// Claims are whole chunks: an owner of null may build only where nobody has claimed (LandRegistry.mayBuildFor).
				BlockPos inChunk = new BlockPos(Mth.clamp(cx << 4, x0, x1), y, Mth.clamp(cz << 4, z0, z1));
				if (!LandRegistry.mayBuildFor(null, level, inChunk)) {
					return false;
				}
			}
		}
		for (int x = x0; ; x = Math.min(x + SAMPLE, x1)) {
			for (int z = z0; ; z = Math.min(z + SAMPLE, z1)) {
				BlockPos pos = new BlockPos(x, y, z);
				if (Town.isProtected(level, pos) || OperatorRegions.protects(level, pos)) {
					return false;
				}
				if (z == z1) {
					break;
				}
			}
			if (x == x1) {
				break;
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
