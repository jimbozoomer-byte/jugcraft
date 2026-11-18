package io.github.jimbozoomer.jugcraft.weapons;

/**
 * The Naval Cutlass's numbers (Thanksgiving weapons, tools/thanksgiving_weapons.py CUTLASS; docs/features/thanksgiving-weapons.md):
 * plain numbers and the arithmetic on them, with no game types, so they can be checked on their own.
 *
 * <p>Its damage is added to iron's attack bonus and the hand's 1, as a sword's 3 is: a blow of 5 at 1.8 a second, 9.0
 * a second against the iron sword's 6 at 1.6, 9.6. It reaches 2.75 blocks (the hand reaches 3). Held up, its shell
 * guard parries {@link #PARRY} of a blow from in front (the longsword parries 0.6). Afloat, the fight it is made for, a
 * blow deals {@link #BOARDING} more: while its wielder or its target is in water or aboard a boat (12.6 a second).
 */
public final class NavalCutlassStats {
	/** Added to the material's bonus and the hand's 1. */
	public static final float DAMAGE = 2.0F;
	/** The attack speed modifier: 4 - 2.2 = 1.8 blows a second. */
	public static final float SPEED = -2.2F;
	/** The swing animation's length, in ticks (a plain swing is 6). */
	public static final int SWING_TICKS = 6;
	/** The farthest a blow lands, in blocks. */
	public static final float REACH = 2.75F;
	/** The share of a blow from in front that parrying stops. */
	public static final float PARRY = 0.4F;
	/** What a blow adds while its wielder or its target is afloat. */
	public static final float BOARDING = 2.0F;
	/** Vanilla's iron tool material's attack damage bonus, which the cutlass is made with. */
	public static final float IRON_BONUS = 2.0F;

	private NavalCutlassStats() {
	}

	/** A plain blow with a material whose attack bonus is {@code materialBonus}. */
	public static float blow(float materialBonus) {
		return 1.0F + materialBonus + DAMAGE;
	}

	/** Blows a second. */
	public static float rate() {
		return 4.0F + SPEED;
	}

	/** Damage a second to one foe, plain blows only. */
	public static float perSecond(float materialBonus, boolean afloat) {
		return (blow(materialBonus) + (afloat ? BOARDING : 0.0F)) * rate();
	}

	/** What a blow adds: {@link #BOARDING} while its wielder or its target is afloat, nothing ashore. */
	public static float boardingBonus(boolean wielderAfloat, boolean targetAfloat) {
		return wielderAfloat || targetAfloat ? BOARDING : 0.0F;
	}

	/** What reaches the parrying wielder of a blow of {@code damage} from in front. */
	public static float parried(float damage) {
		return damage * (1.0F - PARRY);
	}
}
