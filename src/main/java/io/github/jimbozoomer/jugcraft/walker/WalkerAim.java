package io.github.jimbozoomer.jugcraft.walker;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Where a walker's pilot is aiming, and where a shell fired from a walker will fly (docs/features/walker-cockpit.md).
 * The server uses these to aim and fire; the pilot's client uses the very same calls to draw the shell's arc and its
 * landing, so what the pilot is shown is what the gun does. Shared, with no client classes.
 */
public final class WalkerAim {
	/** How far a shell's flight is followed, in ticks. */
	public static final int FLIGHT_TICKS = 200;
	/** Drag a tick, as a thrown projectile's (and {@link io.github.jimbozoomer.jugcraft.artillery.Ballistics}'). */
	public static final double DRAG = 0.99;

	private WalkerAim() {
	}

	/** A shell's flight: its position each tick, and where it comes down (null if it does not within the ticks followed). */
	public record Flight(List<Vec3> points, @Nullable Vec3 impact) {
	}

	/** Whether the chunk column at chunk coordinates (x, z) is loaded. */
	@FunctionalInterface
	public interface ChunkLoaded {
		boolean test(int chunkX, int chunkZ);
	}

	/** A hair past each end of a path, more than the block walk of {@link Level#clip} reaches beyond its ends. */
	private static final double PATH_EDGE = 1.0E-4;

	/** {@link #chunksLoadedAlong} for {@code level}'s loaded chunks. */
	public static boolean loadedAlong(Level level, Vec3 a, Vec3 b) {
		return chunksLoadedAlong(a, b, (x, z) -> level.hasChunkAt(new BlockPos(x << 4, 0, z << 4)));
	}

	/**
	 * Whether every chunk the straight path from {@code a} to {@code b} may cross is loaded. On a server,
	 * {@link Level#clip} reads each block along its path, and reading a block in a chunk that is not loaded loads (or
	 * generates) it. So a path's ends being loaded is not enough: a path across a chunk's corner passes through a
	 * neighbour. The path is walked in steps of at most a block, each counting the chunks its own little box spans (a hair
	 * past the path's ends). That covers every chunk the path touches, and a chunk it misses is counted only when the path
	 * passes within a block of that chunk's corner.
	 */
	public static boolean chunksLoadedAlong(Vec3 a, Vec3 b, ChunkLoaded loaded) {
		Vec3 path = b.subtract(a);
		double length = path.length();
		Vec3 edge = length > 0 ? path.scale(PATH_EDGE / length) : Vec3.ZERO;
		Vec3 start = a.subtract(edge);
		Vec3 span = b.add(edge).subtract(start);
		int steps = Math.max(1, (int) Math.ceil(length));
		Vec3 previous = start;
		for (int step = 1; step <= steps; step++) {
			Vec3 next = start.add(span.scale(step / (double) steps));
			int x0 = Math.min(Mth.floor(previous.x), Mth.floor(next.x)) >> 4, x1 = Math.max(Mth.floor(previous.x), Mth.floor(next.x)) >> 4;
			int z0 = Math.min(Mth.floor(previous.z), Mth.floor(next.z)) >> 4, z1 = Math.max(Mth.floor(previous.z), Mth.floor(next.z)) >> 4;
			for (int x = x0; x <= x1; x++) {
				for (int z = z0; z <= z1; z++) {
					if (!loaded.test(x, z)) {
						return false;
					}
				}
			}
			previous = next;
		}
		return true;
	}

	/** Whether every chunk {@code box} spans is loaded, so testing it for blocks (as noCollision does) loads none. */
	public static boolean loadedIn(Level level, AABB box) {
		for (int x = Mth.floor(box.minX) >> 4; x <= Mth.floor(box.maxX) >> 4; x++) {
			for (int z = Mth.floor(box.minZ) >> 4; z <= Mth.floor(box.maxZ) >> 4; z++) {
				if (!level.hasChunkAt(new BlockPos(x << 4, 0, z << 4))) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * The point on the block the pilot looks at, up to {@code reach} blocks, or null if there is none. The look is traced
	 * at most 16 blocks at a time and stops where it would read a chunk that is not loaded (its end's, or one it crosses
	 * on the way: {@link #loadedAlong}), so aiming never loads one. The look is the rotation the mounted pilot's camera
	 * sends, not the animated head yaw.
	 */
	public static @Nullable Vec3 lookedAt(Level level, Entity pilot, int reach) {
		Vec3 from = pilot.getEyePosition();
		Vec3 look = Vec3.directionFromRotation(pilot.getXRot(), pilot.getYRot());
		for (int travelled = 0; travelled < reach; travelled += 16) {
			Vec3 to = from.add(look.scale(Math.min(16, reach - travelled)));
			if (!level.isLoaded(BlockPos.containing(to)) || !loadedAlong(level, from, to)) {
				return null;
			}
			BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pilot));
			if (hit.getType() == HitResult.Type.BLOCK) {
				return hit.getLocation();
			}
			from = to;
		}
		return null;
	}

	/**
	 * A shell's flight from {@code start} at {@code velocity} (blocks a tick), each tick moving by its speed, losing
	 * {@value #DRAG} to drag and falling by {@code gravity}, as {@link io.github.jimbozoomer.jugcraft.artillery.Ballistics}
	 * models it; it comes down on the first block it meets, and stops where its next tick would read a chunk that is not
	 * loaded (where it ends, or one it crosses on the way: {@link #loadedAlong}).
	 */
	public static Flight flight(Level level, Vec3 start, Vec3 velocity, double gravity, @Nullable Entity shooter) {
		List<Vec3> points = new ArrayList<>();
		Vec3 at = start;
		Vec3 v = velocity;
		points.add(at);
		for (int tick = 0; tick < FLIGHT_TICKS; tick++) {
			Vec3 next = at.add(v);
			if (!level.isLoaded(BlockPos.containing(next)) || next.y < level.getMinY() || !loadedAlong(level, at, next)) {
				return new Flight(points, null);
			}
			BlockHitResult hit = level.clip(new ClipContext(at, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
					shooter == null ? net.minecraft.world.phys.shapes.CollisionContext.empty() : net.minecraft.world.phys.shapes.CollisionContext.of(shooter)));
			if (hit.getType() == HitResult.Type.BLOCK) {
				points.add(hit.getLocation());
				return new Flight(points, hit.getLocation());
			}
			at = next;
			points.add(at);
			v = new Vec3(v.x * DRAG, v.y * DRAG - gravity, v.z * DRAG);
		}
		return new Flight(points, null);
	}
}
