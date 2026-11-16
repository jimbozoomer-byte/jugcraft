package io.github.jimbozoomer.jugcraft.horses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleSupplier;

/**
 * Deterministic executable tests of the horse breeds' rules (HorseBreed, HorseLineage, HorseCargo, BreedDimensions): trait
 * tables and caps, ancestry blending and its saved form, the flight rule, base-stat rolls and inheritance, recovery, and
 * cargo capacity and overflow. No game, network or clock. Gradle task {@code horseLineageTest} (part of {@code check}).
 */
public final class HorseLineageTests {
	private static int checks;

	private static void check(boolean yes, String why) {
		checks++;
		if (!yes) {
			throw new AssertionError(why);
		}
	}

	private static int sum(int[] shares) {
		int total = 0;
		for (int share : shares) {
			total += share;
		}
		return total;
	}

	public static void main(String[] args) {
		breedTable();
		ancestry();
		codec();
		pairingAndFlight();
		traitsAndCaps();
		baseStats();
		recovery();
		cargo();
		dimensions();
		System.out.println("Horse lineage: " + checks + " assertions passed");
	}

	/** Every ground breed is best at something and worse at something; only the Pegasus flies. */
	static void breedTable() {
		check(HorseBreed.GROUND.size() == 5 && !HorseBreed.GROUND.contains(HorseBreed.PEGASUS), "five ground breeds");
		for (HorseBreed b : HorseBreed.values()) {
			check(b.flies() == (b == HorseBreed.PEGASUS), "only the Pegasus flies: " + b);
			check(HorseBreed.byId(b.id()) == b, "ids round-trip: " + b);
			check(b.cargoRows == 0 || b.carriesBags, "a breed with bags has tack drawn for them: " + b);
		}
		check(HorseBreed.byId("horse") == null && HorseBreed.byId("") == null, "unknown ids are null");
		check(HorseBreed.FRIESIAN.speed > HorseBreed.GIANT_FINNHORSE.speed && HorseBreed.FRIESIAN.jump >= 1.2, "Friesian fastest, best jumper");
		check(HorseBreed.SHIRE.cargoRows == 3 && HorseBreed.SHIRE.speed == 0.75, "Shire: most cargo, slowest");
		check(HorseBreed.GIANT_FINNHORSE.health == 1.5 && HorseBreed.GIANT_FINNHORSE.armor == 4.0, "Giant: most health, armour");
		check(HorseBreed.ICELANDIC.step == 0.25 && HorseBreed.ICELANDIC.safeFall == 2.0, "Icelandic: steps and falls");
		check(HorseBreed.MONGOLIAN.recovery > 0 && HorseBreed.MONGOLIAN.health > 1.0, "Mongolian: endurance");
		check(HorseBreed.PEGASUS.cargoRows < HorseBreed.SHIRE.cargoRows, "the Pegasus carries less than the Shire");
		for (HorseBreed b : HorseBreed.GROUND) {
			check(1.0 + b.step < 1.5, "no breed steps over a fence: " + b);
		}
	}

	static void ancestry() {
		for (HorseBreed b : HorseBreed.GROUND) {
			int[] pure = HorseLineage.pure(b);
			check(sum(pure) == HorseLineage.TOTAL && HorseLineage.share(pure, b) == HorseLineage.TOTAL, "pure " + b);
			check(Arrays.equals(HorseLineage.blend(pure, pure), pure), "pure x pure stays pure: " + b);
		}
		check(sum(HorseLineage.pure(HorseBreed.PEGASUS)) == 0, "the Pegasus has no ground share");
		int[] half = HorseLineage.blend(HorseLineage.pure(HorseBreed.SHIRE), HorseLineage.pure(HorseBreed.FRIESIAN));
		check(HorseLineage.share(half, HorseBreed.SHIRE) == 2048 && HorseLineage.share(half, HorseBreed.FRIESIAN) == 2048, "50/50");
		// Many generations of random crosses always keep an exact total.
		Random random = new Random(7);
		List<int[]> herd = new ArrayList<>();
		for (HorseBreed b : HorseBreed.GROUND) {
			herd.add(HorseLineage.pure(b));
		}
		for (int i = 0; i < 4000; i++) {
			int[] child = HorseLineage.blend(herd.get(random.nextInt(herd.size())), herd.get(random.nextInt(herd.size())));
			check(sum(child) == HorseLineage.TOTAL, "a cross keeps the total");
			for (int share : child) {
				check(share >= 0 && share <= HorseLineage.TOTAL, "shares stay in range");
			}
			herd.add(child);
		}
		check(HorseLineage.dominant(half, 0.0) == HorseBreed.SHIRE && HorseLineage.dominant(half, 0.99) == HorseBreed.FRIESIAN,
				"a tie is the roll's (breed order)");
		int[] quarter = HorseLineage.blend(half, HorseLineage.pure(HorseBreed.FRIESIAN));
		check(HorseLineage.dominant(quarter, 0.0) == HorseBreed.FRIESIAN && HorseLineage.dominant(quarter, 0.99) == HorseBreed.FRIESIAN,
				"the largest share wins whatever the roll");
		check(HorseLineage.describe(quarter, HorseBreed::id).equals("75% friesian_horse, 25% shire_horse"), "describe: " + HorseLineage.describe(quarter, HorseBreed::id));
		int[] eighth = HorseLineage.blend(quarter, HorseLineage.pure(HorseBreed.FRIESIAN));
		check(HorseLineage.describe(eighth, HorseBreed::id).equals("87.5% friesian_horse, 12.5% shire_horse"), "describe eighths");
		check(HorseLineage.generation(0, 0) == 1 && HorseLineage.generation(3, 7) == 8
				&& HorseLineage.generation(HorseLineage.MAX_GENERATION, 1) == HorseLineage.MAX_GENERATION, "generations count, capped");
	}

	/** The saved ancestry: round trip, and anything damaged reads as the breed's pure line. */
	static void codec() {
		int[] mix = HorseLineage.blend(HorseLineage.blend(HorseLineage.pure(HorseBreed.ICELANDIC), HorseLineage.pure(HorseBreed.SHIRE)),
				HorseLineage.pure(HorseBreed.MONGOLIAN));
		String saved = HorseLineage.encode(mix);
		check(saved.equals("icelandic_horse:1024,mongolian_horse:2048,shire_horse:1024"), "encoded: " + saved);
		check(Arrays.equals(HorseLineage.decode(saved, HorseBreed.ICELANDIC), mix), "decode(encode) is the same");
		int[] pure = HorseLineage.pure(HorseBreed.SHIRE);
		for (String bad : new String[] {null, "", " ", "shire_horse", "shire_horse:", "shire_horse:abc", "shire_horse:4095",
				"shire_horse:4097", "shire_horse:-1,friesian_horse:4097", "unicorn:4096", "pegasus:4096", "shire_horse:2048,shire_horse:2048",
				":4096", "shire_horse:0,friesian_horse:4096,x", "friesian_horse:4096,"}) {
			int[] read = HorseLineage.decode(bad, HorseBreed.SHIRE);
			check(Arrays.equals(read, pure), "damaged ancestry '" + bad + "' reads as pure: " + Arrays.toString(read));
		}
		check(Arrays.equals(HorseLineage.decode(" shire_horse : 4096 ", HorseBreed.FRIESIAN), pure), "spaces are tolerated");
		Random random = new Random(11);
		for (int i = 0; i < 2000; i++) {
			int[] a = HorseLineage.pure(HorseBreed.GROUND.get(random.nextInt(5)));
			int[] b = HorseLineage.pure(HorseBreed.GROUND.get(random.nextInt(5)));
			int[] c = HorseLineage.blend(HorseLineage.blend(a, b), HorseLineage.pure(HorseBreed.GROUND.get(random.nextInt(5))));
			check(Arrays.equals(HorseLineage.decode(HorseLineage.encode(c), HorseBreed.ICELANDIC), c), "round trip " + Arrays.toString(c));
		}
	}

	/** The one flight rule: a foal flies only from two Pegasi; a Pegasus pairs with nothing else. */
	static void pairingAndFlight() {
		for (HorseBreed a : HorseBreed.values()) {
			for (HorseBreed b : HorseBreed.values()) {
				boolean pair = HorseLineage.mayPair(a, b);
				check(pair == (a.flies() == b.flies()), "pairing " + a + " x " + b);
				HorseLineage.Foal foal = HorseLineage.foal(a, HorseLineage.pure(a), b, HorseLineage.pure(b), 0.5);
				if (!pair) {
					check(foal == null, "no foal from " + a + " x " + b);
					continue;
				}
				check(foal != null && foal.breed().flies() == (a.flies() && b.flies()), "flight only from two Pegasi: " + a + " x " + b);
				check(HorseLineage.foalFlies(a, b) == (a == HorseBreed.PEGASUS && b == HorseBreed.PEGASUS), "foalFlies " + a + b);
				if (!foal.breed().flies()) {
					check(foal.breed() == a || foal.breed() == b, "a first cross is one of its parents' breeds");
				}
			}
			check(!HorseLineage.mayPair(a, null) && !HorseLineage.mayPair(null, a), "no pairing with a non-horse line");
		}
		// However mixed, a ground line never yields a flyer.
		Random random = new Random(3);
		for (int i = 0; i < 3000; i++) {
			HorseBreed a = HorseBreed.GROUND.get(random.nextInt(5));
			HorseBreed b = HorseBreed.GROUND.get(random.nextInt(5));
			HorseLineage.Foal foal = HorseLineage.foal(a, HorseLineage.pure(a), b, HorseLineage.pure(b), random.nextDouble());
			HorseLineage.Foal next = HorseLineage.foal(foal.breed(), foal.shares(), b, HorseLineage.pure(b), random.nextDouble());
			check(!foal.breed().flies() && !next.breed().flies(), "ground lines stay on the ground");
		}
	}

	/** Blended traits stay inside the pure breeds' range; bags round down; the Pegasus keeps its own. */
	static void traitsAndCaps() {
		for (HorseBreed b : HorseBreed.GROUND) {
			HorseLineage.Traits t = HorseLineage.traits(b, HorseLineage.pure(b));
			check(t.health() == b.health && t.speed() == b.speed && t.jump() == b.jump && t.armor() == b.armor
					&& t.cargoRows() == b.cargoRows && t.roadBonus() == b.roadBonus, "a pure breed has its own traits: " + b);
		}
		HorseLineage.Traits pegasus = HorseLineage.traits(HorseBreed.PEGASUS, HorseLineage.pure(HorseBreed.SHIRE));
		check(pegasus.health() == 1.0 && pegasus.speed() == 1.0 && pegasus.cargoRows() == 1, "the Pegasus ignores any ground share");
		int[] half = HorseLineage.blend(HorseLineage.pure(HorseBreed.SHIRE), HorseLineage.pure(HorseBreed.FRIESIAN));
		HorseLineage.Traits shire = HorseLineage.traits(HorseBreed.SHIRE, half);
		check(Math.abs(shire.speed() - 0.95) < 1e-9 && Math.abs(shire.health() - 1.025) < 1e-9 && Math.abs(shire.roadBonus() - 0.05) < 1e-9,
				"50/50 Shire x Friesian blends: " + shire);
		check(shire.cargoRows() == 1, "a half Shire carries one row, not three");
		check(HorseLineage.traits(HorseBreed.FRIESIAN, half).cargoRows() == 0, "a Friesian has no bags to carry");
		check(HorseLineage.traits(HorseBreed.SHIRE, HorseLineage.blend(HorseLineage.pure(HorseBreed.SHIRE),
				HorseLineage.pure(HorseBreed.GIANT_FINNHORSE))).cargoRows() == 1, "Shire x Giant: two rows round down to one");
		check(HorseLineage.menuRows(2.999) == 1 && HorseLineage.menuRows(3.0) == 3 && HorseLineage.menuRows(0.99) == 0, "menu sizes");
		// No cross is better than the best pure breed at anything (no runaway breeding).
		Random random = new Random(5);
		List<int[]> herd = new ArrayList<>();
		for (HorseBreed b : HorseBreed.GROUND) {
			herd.add(HorseLineage.pure(b));
		}
		for (int i = 0; i < 3000; i++) {
			int[] child = HorseLineage.blend(herd.get(random.nextInt(herd.size())), herd.get(random.nextInt(herd.size())));
			herd.add(child);
			HorseLineage.Traits t = HorseLineage.traits(HorseLineage.dominant(child, random.nextDouble()), child);
			check(t.health() >= 0.85 && t.health() <= 1.5 && t.speed() >= 0.75 && t.speed() <= 1.15 && t.jump() >= 0.7 && t.jump() <= 1.2,
					"multipliers capped: " + t);
			check(t.step() >= 0 && t.step() <= 0.25 && t.safeFall() >= 0 && t.safeFall() <= 2 && t.knockback() >= 0 && t.knockback() <= 0.8
					&& t.armor() >= 0 && t.armor() <= 4 && t.roadBonus() >= 0 && t.roadBonus() <= 0.1 && t.recovery() >= 0 && t.recovery() <= 1,
					"bonuses capped: " + t);
			check(t.cargoRows() >= 0 && t.cargoRows() <= 3, "rows capped");
		}
		check(HorseLineage.cap(9.0, b -> b.health) == 1.5 && HorseLineage.cap(-3.0, b -> b.speed) == 0.75, "cap clamps");
		HorseLineage.Traits empty = HorseLineage.traits(HorseBreed.SHIRE, new int[5]);
		check(empty.health() == HorseBreed.SHIRE.health, "an empty ancestry counts as pure");
	}

	static DoubleSupplier fixed(double value) {
		return () -> value;
	}

	static void baseStats() {
		Random random = new Random(9);
		for (int i = 0; i < 20000; i++) {
			double[] roll = HorseLineage.roll(random::nextDouble);
			check(roll[0] >= 15 && roll[0] <= 30 && roll[1] >= 0.1125 && roll[1] <= 0.3375 && roll[2] >= 0.4 && roll[2] <= 1.0,
					"rolls stay in vanilla's ranges: " + Arrays.toString(roll));
		}
		double[] low = HorseLineage.roll(fixed(0.0));
		double[] high = HorseLineage.roll(fixed(0.999999));
		check(low[0] == 15 && Math.abs(low[1] - 0.1125) < 1e-9 && Math.abs(low[2] - 0.4) < 1e-9, "lowest roll");
		check(high[0] == 30 && Math.abs(high[1] - 0.3375) < 1e-5 && Math.abs(high[2] - 1.0) < 1e-5, "highest roll");
		// Inheritance: the mean, moved by at most half the gap plus a tenth of the range, clamped.
		for (int i = 0; i < 20000; i++) {
			double a = 15 + random.nextDouble() * 15;
			double b = 15 + random.nextDouble() * 15;
			double child = HorseLineage.inherit(a, b, 15, 30, random::nextDouble);
			check(child >= 15 && child <= 30, "inherited health in range");
			check(Math.abs(child - (a + b) / 2) <= Math.abs(a - b) / 2 + 1.5 + 1e-9, "modest variance: " + a + " " + b + " " + child);
		}
		check(HorseLineage.inherit(30, 30, 15, 30, fixed(0.5)) == 30 && HorseLineage.inherit(20, 24, 15, 30, fixed(0.5)) == 22, "the mean at the middle roll");
		check(HorseLineage.inherit(30, 30, 15, 30, fixed(0.999999)) == 30, "the top never goes past the range");
		check(HorseLineage.inherit(99, 99, 15, 30, fixed(0.5)) == 30 && HorseLineage.inherit(-5, -5, 15, 30, fixed(0.5)) == 15, "bad parents clamp");
		// Breeding the best with the best for many generations never leaves the range (no runaway).
		double[] line = {30, 0.3375, 1.0};
		for (int g = 0; g < 200; g++) {
			line = HorseLineage.inherit(line, line, random::nextDouble);
			check(line[0] <= 30 && line[1] <= 0.3375 && line[2] <= 1.0, "a bred line stays in range");
		}
		double[] sane = HorseLineage.sane(Double.NaN, -1, 9);
		check(sane[0] == 22.5 && Math.abs(sane[1] - 0.225) < 1e-9 && sane[2] == 1.0, "broken saved bases read sanely: " + Arrays.toString(sane));
	}

	static void recovery() {
		check(HorseLineage.recover(1.0, HorseLineage.CALM_TICKS, 10, 20) == 1.0F, "a calm hurt horse recovers");
		check(HorseLineage.recover(1.0, HorseLineage.CALM_TICKS - 1, 10, 20) == 0.0F, "not while recently hurt");
		check(HorseLineage.recover(1.0, HorseLineage.CALM_TICKS, 20, 20) == 0.0F, "not at full health");
		check(HorseLineage.recover(1.0, HorseLineage.CALM_TICKS, 19.5F, 20) == 0.5F, "never past full");
		check(HorseLineage.recover(0.0, 99999, 10, 20) == 0.0F, "not without the trait");
		check(HorseLineage.recover(1.0, 99999, 0, 20) == 0.0F, "not when dead");
	}

	/** Capacity and overflow: every stored stack ends in exactly one place, none lost, none doubled. */
	static void cargo() {
		check(HorseCargo.capacity(3) == 27 && HorseCargo.capacity(1) == 9 && HorseCargo.capacity(0) == 0 && HorseCargo.capacity(9) == 27
				&& HorseCargo.capacity(-1) == 0, "capacities");
		boolean[] full = new boolean[27];
		Arrays.fill(full, true);
		check(HorseCargo.plan(full, 27).empty(), "a full bag at full capacity needs nothing");
		HorseCargo.Plan nine = HorseCargo.plan(full, 9);
		check(nine.moves().isEmpty() && nine.handBack().size() == 18, "full 27 into 9: 18 stacks back to the owner");
		HorseCargo.Plan none = HorseCargo.plan(full, 0);
		check(none.handBack().size() == 27, "capacity 0: everything back");
		boolean[] sparse = new boolean[27];
		sparse[2] = sparse[12] = sparse[26] = true;
		HorseCargo.Plan moved = HorseCargo.plan(sparse, 9);
		check(moved.moves().size() == 2 && moved.handBack().isEmpty(), "two overflow stacks move into free visible slots");
		check(moved.moves().get(0)[0] == 12 && moved.moves().get(0)[1] == 0 && moved.moves().get(1)[0] == 26 && moved.moves().get(1)[1] == 1,
				"in order, into the first empty slots");
		Random random = new Random(13);
		for (int i = 0; i < 20000; i++) {
			boolean[] occupied = new boolean[27];
			int stored = 0;
			for (int s = 0; s < 27; s++) {
				occupied[s] = random.nextInt(3) == 0;
				stored += occupied[s] ? 1 : 0;
			}
			int capacity = HorseCargo.capacity(random.nextInt(4));
			HorseCargo.Plan plan = HorseCargo.plan(occupied, capacity);
			boolean[] after = occupied.clone();
			int[] landed = new int[27];
			for (int[] move : plan.moves()) {
				check(move[0] >= capacity && move[1] < capacity, "moves go from overflow into view");
				check(after[move[0]] && !after[move[1]], "a move takes a real stack into an empty slot");
				after[move[1]] = true;
				after[move[0]] = false;
				landed[move[1]]++;
			}
			for (int slot : plan.handBack()) {
				check(after[slot] && slot >= capacity, "hand back only real overflow stacks");
				after[slot] = false;
			}
			int kept = 0;
			for (int s = 0; s < 27; s++) {
				check(landed[s] <= 1, "no slot receives two stacks");
				if (after[s]) {
					kept++;
					check(s < capacity, "nothing is left out of view");
				}
			}
			check(kept + plan.handBack().size() == stored, "every stack ends in exactly one place");
			if (capacity == 27) {
				check(plan.empty(), "full capacity changes nothing");
			}
		}
	}

	static void dimensions() {
		BreedDimensions.Size vanilla = BreedDimensions.of("horse");
		for (HorseBreed b : HorseBreed.GROUND) {
			BreedDimensions.Size size = BreedDimensions.of(b.id());
			check(size.width() > 1.0 && size.width() < 3.0 && size.height() > 1.2 && size.height() < 3.0, "sane hitbox: " + b);
			check(size.eyes() < size.height() && size.seat() > 0.8 && size.seat() < size.height(), "eyes and seat inside it: " + b);
		}
		check(BreedDimensions.of("icelandic_horse").height() < vanilla.height() && BreedDimensions.of("icelandic_horse").seat() < vanilla.seat(),
				"the Icelandic is lower than vanilla");
		check(BreedDimensions.of("giant_finnhorse").width() > BreedDimensions.of("shire_horse").width()
				&& BreedDimensions.of("shire_horse").width() > vanilla.width(), "Giant > Shire > vanilla");
		check(BreedDimensions.of("giant_finnhorse").width() > 2.0, "the giant does not fit a two-wide gate");
		check(BreedDimensions.of("shire_horse").width() < 2.0, "the Shire does");
	}
}
