package io.github.jimbozoomer.jugcraft.weapons;

/**
 * The woodland hunting weapons' numbers (Thanksgiving weapons, docs/features/thanksgiving-weapons.md;
 * tools/thanksgiving_weapons.py HATCHET, SPEAR and BOW carry the same numbers). These are everyday hunting tools of the
 * stone and wood tiers: a stone-headed hatchet and a hunting spear, both thrown as the arms' francisca and javelin are,
 * and a short woodland bow drawn as the arms' longbow is.
 *
 * <p>The numbers are free of game types, so they can be checked outside Gradle. A blow's damage is the hand's 1, plus
 * the material's bonus (stone 1), plus the kind's damage, as a sword's is. Its rate is 4 plus the speed modifier.
 */
public final class HuntingWeaponStats {
	/** Vanilla ToolMaterial.STONE's attack damage bonus. */
	public static final float STONE_BONUS = 1.0F;

	// The Stone-headed Hatchet: a light chopping blow, chops wood as a stone axe does, and is thrown end over end.
	public static final float HATCHET_DAMAGE = 3.0F;
	public static final float HATCHET_SPEED = -2.8F;
	public static final int HATCHET_SWING_TICKS = 8;
	public static final float HATCHET_REACH = 2.75F;
	/** Seconds a blow stops a raised shield blocking (the francisca's 2, an axe's 5). */
	public static final float HATCHET_DISABLE = 1.5F;
	public static final int HATCHET_WIND = 6;
	public static final float HATCHET_THROW_SPEED = 1.5F;
	public static final float HATCHET_GRAVITY = 0.06F;
	public static final float HATCHET_THROW_DAMAGE = 4.0F;

	// The Hunting Spear: quick jabs with a long reach, and thrown far and straight.
	public static final float SPEAR_DAMAGE = 2.0F;
	public static final float SPEAR_SPEED = -2.6F;
	public static final int SPEAR_SWING_TICKS = 7;
	public static final float SPEAR_REACH = 3.5F;
	public static final float SPEAR_MARGIN = 0.125F;
	public static final int SPEAR_WIND = 10;
	public static final float SPEAR_THROW_SPEED = 2.4F;
	public static final float SPEAR_GRAVITY = 0.035F;
	public static final float SPEAR_THROW_DAMAGE = 5.0F;

	// The Woodland Hunting Bow: quicker to draw than vanilla's bow, its arrows a little slower.
	public static final int BOW_DRAW = 17;
	public static final float BOW_SPEED = 2.4F;
	public static final float BOW_ARROW_DAMAGE = 2.0F;
	public static final int BOW_DURABILITY = 256;

	/** Vanilla's bow: full draw in 20 ticks, arrows at 3.0 blocks a tick with base damage 2.0. */
	public static final int VANILLA_BOW_DRAW = 20;
	public static final float VANILLA_BOW_SPEED = 3.0F;
	public static final float VANILLA_ARROW_DAMAGE = 2.0F;

	private HuntingWeaponStats() {
	}

	/** A blow's damage: the hand's 1, the material's bonus and the kind's damage. */
	public static float blow(float materialBonus, float damage) {
		return 1.0F + materialBonus + damage;
	}

	/** Blows a second for an attack-speed modifier. */
	public static float rate(float speed) {
		return 4.0F + speed;
	}

	/** Damage a second in the hand. */
	public static float perSecond(float materialBonus, float damage, float speed) {
		return blow(materialBonus, damage) * rate(speed);
	}

	/** A fully drawn arrow's damage before the game's rounding and crits: its base damage times its speed. */
	public static float arrowHit(float speed, float damage) {
		return speed * damage;
	}

	/** A bow's damage a second at full draw, shot after shot. */
	public static float bowPerSecond(int draw, float speed, float damage) {
		return arrowHit(speed, damage) * 20.0F / draw;
	}

	/** By how many percent (rounded) a bow's arrows fly slower than vanilla's: positive when slower. */
	public static int slowerPercent(float speed) {
		return Math.round((1.0F - speed / VANILLA_BOW_SPEED) * 100.0F);
	}
}
