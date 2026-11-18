package io.github.jimbozoomer.jugcraft.menagerie;

/**
 * The rules that keep pirate ships a fair fight and a finite reward (docs/features/pirate-ships.md), as pure arithmetic
 * with no game types, so they can be checked on their own:
 * <ul>
 * <li><b>Who meets them.</b> A ship is raised only near a player wearing at least {@value #MIN_ARMOR} points of armour
 * (most of a chain, gold or iron set); its crew is {@value #MIN_CREW} for such a player, one more from
 * {@value #MORE_ARMOR} points (full iron) and {@value #MAX_CREW} from {@value #MOST_ARMOR} (diamond or better).</li>
 * <li><b>Whom they fight.</b> The crew engage a player who is that well armed, or one who has provoked this ship (struck
 * it or its crew, or set foot on its deck) within {@value #PROVOKED_TICKS} ticks. Anyone else may sail past
 * unmolested.</li>
 * <li><b>How many.</b> At most {@value #MAX_SHIPS} ships afloat and crewed, one to a spawn region, none raised within
 * {@value #SPACING} blocks of another ship's anchor, and none near a player who had one raised near them in the last
 * {@value #PLAYER_REST} ticks (a day). With one finite hold a ship, rolled once, that bounds what any player can take.</li>
 * </ul>
 */
public final class PirateFairness {
	/** Armour points a player needs before ships are raised near them or the crew engage them unprovoked. */
	public static final int MIN_ARMOR = 10;
	/** Armour points for a crew of four, and of five. */
	public static final int MORE_ARMOR = 15;
	public static final int MOST_ARMOR = 20;
	public static final int MIN_CREW = 3;
	public static final int MAX_CREW = 5;
	/** How long a provocation lasts (five minutes). */
	public static final long PROVOKED_TICKS = 6_000L;
	/** Ships afloat at once, loaded or not. */
	public static final int MAX_SHIPS = 3;
	/** No ship is raised within this many blocks of another ship's anchor. */
	public static final double SPACING = 160.0;
	/** Ticks after a ship was raised near a player before another may be raised near them (one game day). */
	public static final long PLAYER_REST = 24_000L;
	/** A provocation time meaning "never". */
	public static final long NEVER = Long.MIN_VALUE;

	private PirateFairness() {
	}

	/** The crew a ship raised near a player wearing {@code armor} points carries: 3 to 5. */
	public static int crewFor(int armor) {
		return armor >= MOST_ARMOR ? MAX_CREW : armor >= MORE_ARMOR ? MIN_CREW + 1 : MIN_CREW;
	}

	/** Whether the crew may engage a player wearing {@code armor} points who last provoked this ship at {@code provokedAt}. */
	public static boolean mayEngage(int armor, long provokedAt, long now) {
		return armor >= MIN_ARMOR || provokedAt != NEVER && now >= provokedAt && now - provokedAt <= PROVOKED_TICKS;
	}

	/**
	 * Why no ship may be raised near a player now, or null when one may: the cap ({@code active} ships of
	 * {@value #MAX_SHIPS}), the player's armour, the player's rest ({@code restUntil}), the spawn region taken or resting,
	 * or another ship's anchor {@code nearestShip} blocks away.
	 */
	public static String refusal(int active, int armor, long restUntil, long now, boolean regionFree, double nearestShip) {
		if (active >= MAX_SHIPS) {
			return "the sea already holds " + active + " ships";
		}
		if (armor < MIN_ARMOR) {
			return "the player wears " + armor + " armour points, under " + MIN_ARMOR;
		}
		if (now < restUntil) {
			return "a ship was raised near this player " + (PLAYER_REST - (restUntil - now)) + " ticks ago";
		}
		if (!regionFree) {
			return "the spawn region holds a ship or is resting";
		}
		if (nearestShip < SPACING) {
			return "another ship's anchor is " + Math.round(nearestShip) + " blocks away";
		}
		return null;
	}
}
