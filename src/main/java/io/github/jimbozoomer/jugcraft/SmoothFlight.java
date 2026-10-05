package io.github.jimbozoomer.jugcraft;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LinearInterpolationHandler;

/**
 * Smooth motion on the client for things the server alone moves: the Observation Balloon, the hot-air balloons and the
 * pibals (5 October 2026: the owner saw the balloons "glitching upward" as they rose). Without a handler a client snaps
 * such an entity to each position the server sends, so it stood still and then jumped every update; with a
 * {@link LinearInterpolationHandler} it eases to each new position over a few ticks, and a rider climbs with it.
 *
 * <p>Minecraft 26.3's interpolation API is used nowhere else in this repo, so it is kept here and in the three
 * {@code createInterpolationHandler} overrides that call {@link #handler}. {@code LinearInterpolationHandler} and its
 * {@code DEFAULT_INTERPOLATION_STEPS} appear in Fabric API 0.161.0 for 26.3 (the version this builds against); the
 * {@code (Entity, int)} constructor, {@code Entity#createInterpolationHandler()} and {@code Entity#getInterpolation()} are
 * taken from 26.3 mods outside the repo. If the build stops here, this class and the three overrides are all there is to change.
 */
public final class SmoothFlight {
	private SmoothFlight() {
	}

	/** The handler an entity eases with: linear, over vanilla's default number of ticks. */
	public static LinearInterpolationHandler handler(Entity entity) {
		return new LinearInterpolationHandler(entity, LinearInterpolationHandler.DEFAULT_INTERPOLATION_STEPS);
	}

	/** Whether the entity eases with a linear handler (the client game test checks the override took). */
	public static boolean isEasing(Entity entity) {
		return entity.getInterpolation() instanceof LinearInterpolationHandler;
	}

	/**
	 * One client tick of easing towards the last position the server sent. Called from each balloon's client tick; if the
	 * game also drives the handler itself, the extra step only shortens the lag, and a steady climb still rises the same
	 * amount each tick.
	 */
	public static void step(Entity entity) {
		InterpolationHandler interpolation = entity.getInterpolation();
		if (interpolation != null) {
			interpolation.interpolate();
		}
	}
}
