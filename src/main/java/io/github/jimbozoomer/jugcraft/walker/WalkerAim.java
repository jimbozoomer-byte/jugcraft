package io.github.jimbozoomer.jugcraft.walker;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
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

	/**
	 * The point on the block the pilot looks at, up to {@code reach} blocks, or null if there is none. The look is traced a
	 * chunk at a time and stops at the first chunk that is not loaded, so aiming never loads one. The look is the
	 * rotation the mounted pilot's camera sends, not the animated head yaw.
	 */
	public static @Nullable Vec3 lookedAt(Level level, Entity pilot, int reach) {
		Vec3 from = pilot.getEyePosition();
		Vec3 look = Vec3.directionFromRotation(pilot.getXRot(), pilot.getYRot());
		for (int travelled = 0; travelled < reach; travelled += 16) {
			Vec3 to = from.add(look.scale(Math.min(16, reach - travelled)));
			if (!level.isLoaded(BlockPos.containing(to))) {
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
	 * models it; it comes down on the first block it meets, and stops at a chunk that is not loaded.
	 */
	public static Flight flight(Level level, Vec3 start, Vec3 velocity, double gravity, @Nullable Entity shooter) {
		List<Vec3> points = new ArrayList<>();
		Vec3 at = start;
		Vec3 v = velocity;
		points.add(at);
		for (int tick = 0; tick < FLIGHT_TICKS; tick++) {
			Vec3 next = at.add(v);
			if (!level.isLoaded(BlockPos.containing(next)) || next.y < level.getMinY()) {
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
