package io.github.jimbozoomer.jugcraft.raiders;

import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.town.TownDefense;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A standoff for a raider that cannot get at what it fights (docs/features/raiders.md, "City approaches and visible
 * fights"): somewhere it can stand, between {@code near} and {@code far} blocks from its target, with a clear line from
 * its eyes to the target, on its own side of the town wall, that it can walk to. Spots on its own side of the target are
 * tried first, nearest first; {@code attempt} turns the search a little further round each time it is asked again.
 *
 * <p>Bounded: ten bearings at two distances, only in loaded chunks (a chunk's height is never asked
 * for before it is known to be loaded), and at most {@value #PATH_CHECKS} paths worked out. It never edits a block.
 */
public final class RaidStandoff {
	/** Bearings tried, in degrees from the raider's own side of its target. */
	private static final int[] OFFSETS = {0, 30, -30, 60, -60, 90, -90, 135, -135, 180};
	/** Paths worked out per search, at most. */
	static final int PATH_CHECKS = 3;
	/** How far a spot's ground may lie above or below the raider's (no rooftops, no pits). */
	static final int CLIMB = 6;
	/** How much each later attempt turns the bearings, in degrees. */
	static final int TURN = 15;

	private record Candidate(BlockPos stand, double cost) {
	}

	private RaidStandoff() {
	}

	/** A standoff spot for {@code mob} against {@code target}, or null if there is none it can reach. */
	public static @Nullable Vec3 spot(Mob mob, LivingEntity target, double near, double far, int attempt) {
		if (!(mob.level() instanceof ServerLevel level)) {
			return null;
		}
		Vec3 at = target.position();
		Vec3 away = mob.position().subtract(at);
		double bearing = away.horizontalDistanceSqr() < 1.0E-6 ? 0.0 : Math.atan2(away.z, away.x);
		boolean inside = Town.isInside(level, mob.blockPosition());
		// Eye to eye, as the raider's own sight (Sensing) judges it: where it can see from, it stops and shoots.
		Vec3 aim = new Vec3(target.getX(), target.getEyeY(), target.getZ());
		double[] radii = {(near + far) / 2.0, far};
		List<Candidate> found = new ArrayList<>();
		for (int offset : OFFSETS) {
			double angle = bearing + Math.toRadians(offset + attempt * TURN);
			for (double r : radii) {
				int x = (int) Math.floor(at.x + Math.cos(angle) * r);
				int z = (int) Math.floor(at.z + Math.sin(angle) * r);
				// Check the column before asking for its height: a height lookup can load a missing chunk.
				if (!level.isLoaded(new BlockPos(x, mob.getBlockY(), z))) {
					continue;
				}
				BlockPos stand = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
				if (Math.abs(stand.getY() - mob.getBlockY()) > CLIMB || !level.getBlockState(stand.below()).isSolid()
						|| !level.getFluidState(stand.below()).isEmpty() || Town.isInside(level, stand) != inside
						|| !clear(level, stand, mob.getBbHeight())) {
					continue;
				}
				Vec3 eye = new Vec3(x + 0.5, stand.getY() + mob.getEyeHeight(), z + 0.5);
				if (!TownDefense.visible(level, mob, eye, aim)) {
					continue;
				}
				found.add(new Candidate(stand, mob.position().distanceToSqr(Vec3.atBottomCenterOf(stand))));
			}
		}
		found.sort(Comparator.comparingDouble(Candidate::cost));
		for (int i = 0; i < Math.min(PATH_CHECKS, found.size()); i++) {
			BlockPos stand = found.get(i).stand();
			Path path = mob.getNavigation().createPath(stand, 1);
			if (path != null && path.canReach()) {
				return Vec3.atBottomCenterOf(stand);
			}
		}
		return null;
	}

	/** Whether a spot has room to stand, as tall as the raider. */
	private static boolean clear(ServerLevel level, BlockPos stand, float height) {
		for (int dy = 0; dy < Math.ceil(height); dy++) {
			BlockPos p = stand.above(dy);
			if (!level.getBlockState(p).getCollisionShape(level, p).isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
