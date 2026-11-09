package io.github.jimbozoomer.jugcraft.menagerie;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.phys.Vec3;

/**
 * Game-test access to a pirate ship's package-private seams (test code only, in the ship's own package): the test
 * answers the ship's questions to its crew, sets a waypoint, and rolls or predicts the hold.
 */
public final class PirateShipTesting {
	private PirateShipTesting() {
	}

	/** Stands in for "is the helm manned" (null: ask the crew). */
	public static void helm(PirateShip ship, Boolean manned) {
		ship.testHelm = manned;
	}

	/** Stands in for "are the crew beaten" (null: ask the crew). */
	public static void beaten(PirateShip ship, Boolean beaten) {
		ship.testBeaten = beaten;
	}

	public static void waypoint(PirateShip ship, Vec3 waypoint) {
		ship.setWaypoint(waypoint);
	}

	public static boolean mayRoll(PirateShip ship) {
		return ship.mayRollHold();
	}

	public static void rollHold(ServerLevel level, PirateShip ship) {
		ship.rollHold(level);
	}

	/** Links {@code crew} to {@code ship} at a station, as provisioning does (without seating it). */
	public static void link(OwnerPlunderer crew, PirateShip ship, int station) {
		crew.link(ship.getUUID(), station);
	}

	/** A cannonball fired by {@code owner} from {@code from} at {@code velocity} (blocks a tick), added to the level. */
	public static OwnerCannonball fire(ServerLevel level, net.minecraft.world.entity.Entity owner, Vec3 from, Vec3 velocity) {
		OwnerCannonball ball = new OwnerCannonball(OwnerCannon.BALL, level);
		ball.setOwner(owner);
		ball.setPos(from);
		ball.setDeltaMovement(velocity);
		level.addFreshEntity(ball);
		return ball;
	}

	/** What the ship's one roll puts in its hold: the same table, place and seed, into a fresh container. */
	public static SimpleContainer expectedHold(ServerLevel level, PirateShip ship, Vec3 rolledAt) {
		SimpleContainer expected = new SimpleContainer(PirateShip.HOLD_SLOTS);
		PirateShip.fillHold(level, rolledAt, ship.lootSeed(), expected);
		return expected;
	}
}
