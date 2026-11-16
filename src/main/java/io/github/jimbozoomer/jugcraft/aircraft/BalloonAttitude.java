package io.github.jimbozoomer.jugcraft.aircraft;

import net.minecraft.util.Mth;
import org.joml.Quaternionf;

/**
 * How {@code HotAirBalloonRenderer} turns a hot-air balloon (fall addition 29). Common code, so that the server game
 * test {@code AircraftFrameGameTests} checks the renderer's own arithmetic.
 *
 * <p>The balloon drifts in the world's frame (the wind aloft, {@code FiestaWinds}), so its envelope's lean is worked
 * out and applied in the world's frame: the top leads, the way the balloon drifts, whichever way its basket was placed.
 * Until November 13 the lean was applied after the basket's facing, so a balloon placed facing south (yaw 0) leaned
 * against its drift and one placed facing west or east leaned across it.
 */
public final class BalloonAttitude {
	/** Degrees of lean per block a tick of drift, and the most it leans. */
	public static final float LEAN = 20, MOST = 3;

	private BalloonAttitude() {
	}

	/**
	 * The envelope's lean from the balloon's drift, in degrees about the world's X and Z axes (in that order): Rx(+)
	 * tips the top toward +Z and Rz(-) toward +X, so the top leans the way the balloon moves.
	 */
	public static float[] lean(double motionX, double motionZ) {
		return new float[] {(float) Mth.clamp(motionZ * LEAN, -MOST, MOST), (float) -Mth.clamp(motionX * LEAN, -MOST, MOST)};
	}

	/** The basket and rigging's turn: the supplied basket is drawn facing -Z, so 180 - yaw about Y. */
	public static Quaternionf basket(float yaw) {
		return new Quaternionf().rotationY((180 - yaw) * Mth.DEG_TO_RAD);
	}

	/**
	 * The envelope's turn about its throat: its sway (degrees about the world's X and Z) outermost, in the world's
	 * frame, then the basket's facing, so the envelope's design still faces the way the basket does.
	 */
	public static Quaternionf envelope(float yaw, float swayX, float swayZ) {
		return new Quaternionf().rotationX(swayX * Mth.DEG_TO_RAD).rotateZ(swayZ * Mth.DEG_TO_RAD).rotateY((180 - yaw) * Mth.DEG_TO_RAD);
	}
}
