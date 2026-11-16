package io.github.jimbozoomer.jugcraft.menagerie;

/**
 * The plan of a pirate ship's walkable deck (docs/features/pirate-ships.md), as pure arithmetic with no game types: the
 * squares along the keel that the deck entities ({@link PirateShipDeck}) are, their tops at y 1.0 over the waterline,
 * turned with the ship, and whether feet are on them. {@link PirateDecks#standingOnDeck} asks this of the loaded deck
 * squares under an entity's feet.
 *
 * <p>An entity's box is a square, so the deck is laid as squares that follow the hull's taper (14 November; before, four
 * 5-block squares overreached the fine bow by up to two blocks, so a boarder could stand on air beside the stem): none
 * reaches more than a pixel past the drawn hull's side (tools/pirate_ship.py, the art test), and none leaves a gap of
 * 0.6 blocks or more (a player's width) inside the bulwarks.
 */
public final class PirateDeckPlan {
	/** The deck squares, ship-local blocks: each one's centre along the keel (z) and its side, stern to bow. */
	public static final double[] SQUARE_CENTRES = {-5.0, -1.0, 2.0, 3.375, 5.625, 6.875};
	public static final double[] SQUARE_WIDTHS = {5.0, 5.0, 5.0, 4.5, 2.75, 1.5};
	/** The deck's top over the waterline. */
	public static final double DECK_Y = 1.0;
	/** How near the deck's top feet must be to stand on it (a pixel): resting feet are exactly on it. */
	public static final double TOLERANCE = 0.0625;

	private PirateDeckPlan() {
	}

	/**
	 * Whether a point (dx, dz: its offset from the ship's position, world axes) lies over the deck of a ship at
	 * {@code yaw}: turned into the ship's frame (the inverse of the seats' {@code yRot(-yaw)}), within one of the deck
	 * squares as laid along the keel. Exact at any yaw, unlike the axis-aligned deck entities, whose corners reach past
	 * the hull between right angles.
	 */
	public static boolean over(double dx, double dz, float yaw) {
		double[] local = PirateMotion.turnY(dx, dz, yaw);
		for (int i = 0; i < SQUARE_CENTRES.length; i++) {
			double half = SQUARE_WIDTHS[i] / 2.0;
			if (Math.abs(local[0]) <= half && Math.abs(local[1] - SQUARE_CENTRES[i]) <= half) {
				return true;
			}
		}
		return false;
	}

	/** Whether feet {@code feetDy} above the ship's position (its waterline) are at the deck's top, within a pixel. */
	public static boolean atDeckHeight(double feetDy) {
		return Math.abs(feetDy - DECK_Y) <= TOLERANCE;
	}

	/**
	 * Whether feet at (dx, feetDy, dz) from a ship at {@code yaw} stand on its deck: supported (on the ground), at the
	 * deck's height and over it. Feet in the water beside the hull, on a shore or a block beside it, on a block or the
	 * rigging above the deck, in the hull below it, or in the air over it, do not.
	 */
	public static boolean standing(double dx, double feetDy, double dz, float yaw, boolean supported) {
		return supported && atDeckHeight(feetDy) && over(dx, dz, yaw);
	}
}
