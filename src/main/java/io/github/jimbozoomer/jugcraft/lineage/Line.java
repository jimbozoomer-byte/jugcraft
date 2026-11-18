package io.github.jimbozoomer.jugcraft.lineage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;

/**
 * An animal's line (docs/features/star-bred-lines.md): two copies of each {@link Trait}, each graded 0 to
 * {@value #MAX_GRADE}, in {@link Trait} order (yield, yield, vigour, vigour, ...); the copies of each {@link Gift} it
 * holds, 1 (a carrier) or 2 (it shows), by the gift's id; the sign it was born under (a celestial pattern's id, or ""
 * for none); and its generation (0 wild, then one more than its older parent). Immutable; anything read is clamped to
 * these bounds, so a hand-edited save cannot give a grade past 5 or a gift that is not one.
 */
public record Line(List<Integer> grades, Map<String, Integer> gifts, String sign, int generation) {
	public static final int MAX_GRADE = 5;
	public static final int MAX_GENERATION = 1_000_000;
	public static final int GRADES = Trait.values().length * 2;

	public static final Codec<Line> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.listOf().fieldOf("grades").forGetter(Line::grades),
			Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("gifts", Map.of()).forGetter(Line::gifts),
			Codec.STRING.optionalFieldOf("sign", "").forGetter(Line::sign),
			Codec.INT.optionalFieldOf("generation", 0).forGetter(Line::generation)).apply(i, Line::new));

	public Line {
		List<Integer> clamped = new ArrayList<>(GRADES);
		for (int i = 0; i < GRADES; i++) {
			clamped.add(grades != null && i < grades.size() && grades.get(i) != null ? Math.clamp(grades.get(i), 0, MAX_GRADE) : 0);
		}
		grades = List.copyOf(clamped);
		// Sorted, so a line written twice is written the same.
		Map<String, Integer> kept = new TreeMap<>();
		if (gifts != null) {
			gifts.forEach((id, copies) -> {
				if (Gift.byId(id) != null && copies != null && copies > 0) {
					kept.put(id, Math.min(copies, 2));
				}
			});
		}
		gifts = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(kept));
		sign = sign == null ? "" : sign;
		generation = Math.clamp(generation, 0, MAX_GENERATION);
	}

	public static Line of(int[] grades, Map<String, Integer> gifts, String sign, int generation) {
		List<Integer> list = new ArrayList<>(grades.length);
		for (int grade : grades) {
			list.add(grade);
		}
		return new Line(list, gifts, sign, generation);
	}

	/** Copy {@code copy} (0 or 1) of {@code trait}. */
	public int grade(Trait trait, int copy) {
		return grades.get(trait.ordinal() * 2 + copy);
	}

	/** The trait's number: its two copies added together, 0 to 10. */
	public int value(Trait trait) {
		return grade(trait, 0) + grade(trait, 1);
	}

	/** How far the trait is past vanilla: 0 at 4 or below, up to 6 at 10. */
	public int points(Trait trait) {
		return Math.max(0, value(trait) - Lineage.VANILLA_UP_TO);
	}

	/** Whether both copies of the trait match, so every baby of two such animals takes the same. */
	public boolean breedsTrue(Trait trait) {
		return grade(trait, 0) == grade(trait, 1);
	}

	public int copies(Gift gift) {
		return gifts.getOrDefault(gift.id, 0);
	}

	/** Whether the gift shows: two copies. */
	public boolean shows(Gift gift) {
		return copies(gift) >= 2;
	}

	/** Whether the line carries a gift that does not show (one copy of it). */
	public boolean carriesUnseen() {
		return gifts.values().stream().anyMatch(copies -> copies == 1);
	}

	/** The gifts that show, in declaration order. */
	public List<Gift> showing() {
		List<Gift> out = new ArrayList<>();
		for (Gift gift : Gift.values()) {
			if (shows(gift)) {
				out.add(gift);
			}
		}
		return out;
	}

	public Line withSign(String pattern) {
		return new Line(grades, gifts, pattern, generation);
	}

	/** This line with the weaker copy of {@code trait} raised by one grade, to at most {@value #MAX_GRADE}. */
	public Line raised(Trait trait) {
		int[] out = array();
		int weaker = trait.ordinal() * 2 + (grade(trait, 0) <= grade(trait, 1) ? 0 : 1);
		out[weaker] = Math.min(MAX_GRADE, out[weaker] + 1);
		return of(out, gifts, sign, generation);
	}

	/** This line with one more copy of one of its kind's gifts it does not already show, if any; at random. */
	public Line woken(EntityType<?> type, RandomSource random) {
		List<Gift> open = new ArrayList<>();
		for (Gift gift : Gift.of(type)) {
			if (!shows(gift)) {
				open.add(gift);
			}
		}
		if (open.isEmpty()) {
			return this;
		}
		Gift gift = open.get(random.nextInt(open.size()));
		Map<String, Integer> out = new LinkedHashMap<>(gifts);
		out.put(gift.id, copies(gift) + 1);
		return new Line(grades, out, sign, generation);
	}

	int[] array() {
		int[] out = new int[GRADES];
		for (int i = 0; i < GRADES; i++) {
			out[i] = grades.get(i);
		}
		return out;
	}
}
