package io.github.jimbozoomer.jugcraft.horses;

import java.util.function.DoubleSupplier;

/**
 * Breeding, ancestry and trait rules for the horse breeds (docs/features/horse-breeds.md#crossbreeding). Pure
 * arithmetic over {@link HorseBreed}: no Minecraft classes, deterministic for a given random source, tested by
 * HorseLineageTests outside the game.
 *
 * <ul>
 * <li><b>Ancestry</b> is each ground breed's share of a horse, out of {@value #TOTAL}. A wild or egg-spawned horse is
 * pure. A foal's share is the mean of its parents' (largest-remainder rounding keeps the sum exact).</li>
 * <li><b>The foal's breed</b> (its look, size and saddle) is the breed with the largest share; a tie is decided by the
 * random roll among the tied breeds.</li>
 * <li><b>Numeric traits</b> (health, speed, jump, step, safe fall, knockback, armour, road speed, recovery) are the
 * share-weighted mean of the breeds' values, then clamped to the range the pure ground breeds span: a cross is never
 * better at anything than the best pure breed, so breeding cannot run away.</li>
 * <li><b>Saddlebags</b> need a breed whose tack has them (Shire, Giant Finnhorse, Pegasus); a cross's rows are the
 * share-weighted rows rounded DOWN to a menu size (0, 1 or 3): only a pure Shire carries three rows.</li>
 * <li><b>Base stats</b> (the vanilla-style roll the multipliers apply to) are the parents' mean with modest variance,
 * clamped to vanilla's horse ranges.</li>
 * <li><b>Flight</b> follows one explicit rule: a Pegasus pairs only with a Pegasus, and only that pair has a flying foal
 * (a Pegasus). Every other pair is two ground breeds and has a ground foal; no ground horse ever flies.</li>
 * </ul>
 */
public final class HorseLineage {
	/** Ancestry shares are kept in 1/4096ths: twelve generations of exact halving. */
	public static final int TOTAL = 4096;
	public static final double MIN_HEALTH = 15.0;
	public static final double MAX_HEALTH = 30.0;
	public static final double MIN_SPEED = 0.1125;
	public static final double MAX_SPEED = 0.3375;
	public static final double MIN_JUMP = 0.4;
	public static final double MAX_JUMP = 1.0;
	/** A calm horse with the recovery trait regains health this often (ticks), after this long unhurt. */
	public static final int RECOVERY_TICKS = 200;
	public static final int CALM_TICKS = 300;
	/** The largest generation counted (older lines stay at it). */
	public static final int MAX_GENERATION = 999;

	private HorseLineage() {
	}

	// ------------------------------------------------------------------------------------------------ ancestry

	public static int[] pure(HorseBreed breed) {
		int[] shares = new int[HorseBreed.GROUND.size()];
		int at = HorseBreed.GROUND.indexOf(breed);
		if (at >= 0) {
			shares[at] = TOTAL;
		}
		return shares;
	}

	public static int share(int[] shares, HorseBreed breed) {
		int at = HorseBreed.GROUND.indexOf(breed);
		return at < 0 ? 0 : shares[at];
	}

	/** The mean of two ancestries, rounded so the shares still add up to {@value #TOTAL} (largest remainder first). */
	public static int[] blend(int[] a, int[] b) {
		int n = HorseBreed.GROUND.size();
		int[] out = new int[n];
		boolean[] half = new boolean[n];
		int sum = 0;
		for (int i = 0; i < n; i++) {
			int total = a[i] + b[i];
			out[i] = total / 2;
			half[i] = total % 2 != 0;
			sum += out[i];
		}
		for (int i = 0; i < n && sum < TOTAL; i++) {
			if (half[i]) {
				out[i]++;
				sum++;
			}
		}
		return out;
	}

	/** Saved form: {@code icelandic_horse:2048,shire_horse:2048} (breeds with no share left out). */
	public static String encode(int[] shares) {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < shares.length; i++) {
			if (shares[i] > 0) {
				if (!text.isEmpty()) {
					text.append(',');
				}
				text.append(HorseBreed.GROUND.get(i).id()).append(':').append(shares[i]);
			}
		}
		return text.toString();
	}

	/**
	 * The saved form read back. Anything unreadable (an unknown breed, a bad number, a negative share, a sum that is not
	 * {@value #TOTAL}) gives the pure ancestry of {@code fallback} instead, so a damaged record never invents a cross.
	 */
	public static int[] decode(String text, HorseBreed fallback) {
		int[] shares = new int[HorseBreed.GROUND.size()];
		if (text == null || text.isBlank()) {
			return pure(fallback);
		}
		int sum = 0;
		for (String part : text.split(",", -1)) {
			int colon = part.indexOf(':');
			if (colon <= 0) {
				return pure(fallback);
			}
			HorseBreed breed = HorseBreed.byId(part.substring(0, colon).trim());
			int at = breed == null ? -1 : HorseBreed.GROUND.indexOf(breed);
			int value;
			try {
				value = Integer.parseInt(part.substring(colon + 1).trim());
			} catch (NumberFormatException e) {
				return pure(fallback);
			}
			if (at < 0 || value <= 0 || value > TOTAL || shares[at] != 0) {
				return pure(fallback);
			}
			shares[at] = value;
			sum += value;
		}
		return sum == TOTAL ? shares : pure(fallback);
	}

	/** A readable ancestry, largest share first: "50% Shire, 50% Friesian" (names from {@code names}). */
	public static String describe(int[] shares, java.util.function.Function<HorseBreed, String> names) {
		StringBuilder text = new StringBuilder();
		boolean[] done = new boolean[shares.length];
		for (int k = 0; k < shares.length; k++) {
			int best = -1;
			for (int i = 0; i < shares.length; i++) {
				if (!done[i] && shares[i] > 0 && (best < 0 || shares[i] > shares[best])) {
					best = i;
				}
			}
			if (best < 0) {
				break;
			}
			done[best] = true;
			if (!text.isEmpty()) {
				text.append(", ");
			}
			text.append(percent(shares[best])).append("% ").append(names.apply(HorseBreed.GROUND.get(best)));
		}
		return text.toString();
	}

	static String percent(int share) {
		double value = share * 100.0 / TOTAL;
		return value == Math.rint(value) ? String.valueOf((int) value) : String.format(java.util.Locale.ROOT, "%.1f", value);
	}

	// ------------------------------------------------------------------------------------------------ pairing

	/** Whether two horses may pair: the flying line only with itself, the ground breeds among themselves. */
	public static boolean mayPair(HorseBreed a, HorseBreed b) {
		return a != null && b != null && a.flies() == b.flies();
	}

	/** The explicit flight rule: a foal flies only when both parents are of the flying line. */
	public static boolean foalFlies(HorseBreed a, HorseBreed b) {
		return mayPair(a, b) && a.flies() && b.flies();
	}

	/** A foal: its breed (look, size, tack) and ancestry. */
	public record Foal(HorseBreed breed, int[] shares) {
	}

	/**
	 * The foal of two horses, or null when they may not pair. {@code roll} in [0, 1) breaks a tie between equal largest
	 * shares, in breed order.
	 */
	public static Foal foal(HorseBreed typeA, int[] sharesA, HorseBreed typeB, int[] sharesB, double roll) {
		if (!mayPair(typeA, typeB)) {
			return null;
		}
		if (foalFlies(typeA, typeB)) {
			return new Foal(HorseBreed.PEGASUS, new int[HorseBreed.GROUND.size()]);
		}
		int[] shares = blend(sharesA, sharesB);
		return new Foal(dominant(shares, roll), shares);
	}

	/** The breed with the largest share; ties go by {@code roll} among the tied breeds. */
	public static HorseBreed dominant(int[] shares, double roll) {
		int best = 0;
		for (int value : shares) {
			best = Math.max(best, value);
		}
		int tied = 0;
		for (int value : shares) {
			if (value == best) {
				tied++;
			}
		}
		int pick = Math.min(tied - 1, (int) Math.floor(Math.max(0.0, Math.min(roll, 0.999999)) * tied));
		for (int i = 0; i < shares.length; i++) {
			if (shares[i] == best && pick-- == 0) {
				return HorseBreed.GROUND.get(i);
			}
		}
		return HorseBreed.GROUND.get(0);
	}

	// ------------------------------------------------------------------------------------------------ traits

	/** A horse's breed traits: multipliers on its base health, speed and jump, attribute bonuses, bags and extras. */
	public record Traits(double health, double speed, double jump, double step, double safeFall, double knockback, double armor,
			double roadBonus, double recovery, int cargoRows) {
	}

	/** The traits of a horse of breed {@code type} with ancestry {@code shares} (ignored for the Pegasus line). */
	public static Traits traits(HorseBreed type, int[] shares) {
		if (type.flies()) {
			HorseBreed p = HorseBreed.PEGASUS;
			return new Traits(p.health, p.speed, p.jump, p.step, p.safeFall, p.knockback, p.armor, p.roadBonus, p.recovery, p.cargoRows);
		}
		double health = 0;
		double speed = 0;
		double jump = 0;
		double step = 0;
		double fall = 0;
		double knock = 0;
		double armor = 0;
		double road = 0;
		double recovery = 0;
		double rows = 0;
		int sum = 0;
		for (int i = 0; i < shares.length; i++) {
			sum += shares[i];
		}
		if (sum <= 0) {
			shares = pure(type);
			sum = TOTAL;
		}
		for (int i = 0; i < shares.length; i++) {
			HorseBreed b = HorseBreed.GROUND.get(i);
			double w = shares[i] / (double) sum;
			health += w * b.health;
			speed += w * b.speed;
			jump += w * b.jump;
			step += w * b.step;
			fall += w * b.safeFall;
			knock += w * b.knockback;
			armor += w * b.armor;
			road += w * b.roadBonus;
			recovery += w * b.recovery;
			rows += w * b.cargoRows;
		}
		return new Traits(cap(health, b -> b.health), cap(speed, b -> b.speed), cap(jump, b -> b.jump), cap(step, b -> b.step),
				cap(fall, b -> b.safeFall), cap(knock, b -> b.knockback), cap(armor, b -> b.armor), cap(road, b -> b.roadBonus),
				cap(recovery, b -> b.recovery), type.carriesBags ? menuRows(rows) : 0);
	}

	/** Saddlebag rows rounded down to a menu the game has (one or three rows of nine). */
	public static int menuRows(double rows) {
		return rows >= 3.0 - 1e-9 ? 3 : rows >= 1.0 - 1e-9 ? 1 : 0;
	}

	/** {@code value} clamped to the range the pure ground breeds span for one trait. */
	static double cap(double value, java.util.function.ToDoubleFunction<HorseBreed> trait) {
		double lo = Double.MAX_VALUE;
		double hi = -Double.MAX_VALUE;
		for (HorseBreed b : HorseBreed.GROUND) {
			lo = Math.min(lo, trait.applyAsDouble(b));
			hi = Math.max(hi, trait.applyAsDouble(b));
		}
		return Math.max(lo, Math.min(hi, value));
	}

	// ------------------------------------------------------------------------------------------------ base stats

	/** A wild horse's base roll {health, speed, jump}: vanilla's horse spread, each the sum of three draws. */
	public static double[] roll(DoubleSupplier random) {
		double health = MIN_HEALTH + Math.floor(random.getAsDouble() * 8) + Math.floor(random.getAsDouble() * 9);
		double speed = (0.45 + random.getAsDouble() * 0.3 + random.getAsDouble() * 0.3 + random.getAsDouble() * 0.3) * 0.25;
		double jump = MIN_JUMP + random.getAsDouble() * 0.2 + random.getAsDouble() * 0.2 + random.getAsDouble() * 0.2;
		return new double[] {clamp(health, MIN_HEALTH, MAX_HEALTH), clamp(speed, MIN_SPEED, MAX_SPEED), clamp(jump, MIN_JUMP, MAX_JUMP)};
	}

	/**
	 * A foal's base value from its parents' base values: their mean, moved by at most half their difference plus a tenth
	 * of the range (the mean of three draws, so near the middle more often), clamped to [min, max].
	 */
	public static double inherit(double a, double b, double min, double max, DoubleSupplier random) {
		a = clamp(a, min, max);
		b = clamp(b, min, max);
		double noise = (random.getAsDouble() + random.getAsDouble() + random.getAsDouble()) / 3.0 - 0.5;
		return clamp((a + b) / 2.0 + noise * (Math.abs(a - b) + 0.2 * (max - min)), min, max);
	}

	/** Both parents' base {health, speed, jump} -> the foal's. */
	public static double[] inherit(double[] a, double[] b, DoubleSupplier random) {
		return new double[] {inherit(a[0], b[0], MIN_HEALTH, MAX_HEALTH, random), inherit(a[1], b[1], MIN_SPEED, MAX_SPEED, random),
				inherit(a[2], b[2], MIN_JUMP, MAX_JUMP, random)};
	}

	/** A saved base roll read back, each value clamped to its range; a missing or broken one is the range's middle. */
	public static double[] sane(double health, double speed, double jump) {
		return new double[] {saneValue(health, MIN_HEALTH, MAX_HEALTH), saneValue(speed, MIN_SPEED, MAX_SPEED), saneValue(jump, MIN_JUMP, MAX_JUMP)};
	}

	private static double saneValue(double value, double min, double max) {
		return Double.isFinite(value) && value > 0 ? clamp(value, min, max) : (min + max) / 2.0;
	}

	static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	/** The next generation number: one more than the older parent's, capped. */
	public static int generation(int a, int b) {
		return Math.min(MAX_GENERATION, Math.max(0, Math.max(a, b)) + 1);
	}

	// ------------------------------------------------------------------------------------------------ recovery

	/**
	 * The recovery trait, checked every {@value #RECOVERY_TICKS} ticks: health to regain now. {@code calmTicks} is how
	 * long the horse has gone without losing health; nothing when hurt recently, at full health, or without the trait.
	 */
	public static float recover(double recovery, int calmTicks, float health, float maxHealth) {
		if (recovery <= 0 || calmTicks < CALM_TICKS || health >= maxHealth || health <= 0) {
			return 0.0F;
		}
		return (float) Math.min(recovery, maxHealth - health);
	}
}
