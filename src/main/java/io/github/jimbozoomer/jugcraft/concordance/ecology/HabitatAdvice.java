package io.github.jimbozoomer.jugcraft.concordance.ecology;

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-06 (Dossier 3 item 3; docs/features/marax-pact.md): the one correction Marax's familiar
 * names for a plant on one reading of its habitat. Pure: the same niche, reading and grafts always give the same advice,
 * and it changes nothing.
 * <ul>
 * <li>A thriving plant has nothing to correct (null).</li>
 * <li>The factors that hold it at its step are those outside its range (stalled) or only tolerable (tolerating), in
 * {@link Factor}'s order. When exactly one holds it and one change of it alone raises the plant a step (stalled to
 * tolerating, tolerating to thriving), that change is the advice ({@link Advice#alone}): the value nearest the one it has
 * that does so ({@link Advice#target}). When two or more hold it, no one change can: the advice names the first of them,
 * "one of n" ({@link Advice#of}), with the nearest value of its ideal range.</li>
 * <li>Harvest strain is not a factor (Verdict.of counts it against the disturbance limit only): where it decided the
 * disturbance fit, easing it is that factor's change ({@link Change#EASE}), tried before moving the area's magic;
 * otherwise the area's disturbance is tried first. A change only ever moves a factor toward its ideal range, and the
 * strain only ever down, so a plant whose niche needs no disturbance is never told to add any.</li>
 * <li>{@code grafts}: the niches the plant would grow by under each graft its bed could take now (by id, in the caller's
 * order; empty when its bed carries one already). The first that alone raises it a step on this reading is named beside
 * the change (the Dewlit graft where it alone would raise a Sunpetal a step: at light 9 to 12).</li>
 * </ul>
 * Bounded: at most {@value #MOST_VERDICTS} verdicts a call besides one for each graft offered.
 */
public final class HabitatAdvice {
	/** The most verdicts one call reads besides the grafts': its own, then at most 32 values for each of two ways to change one factor. */
	public static final int MOST_VERDICTS = 1 + 2 * (2 * Habitat.MAX_NUTRIENTS);

	/** Which way the named factor should go: raised, lowered, or (disturbance only) its bed's harvest strain eased. */
	public enum Change {
		MORE("more"),
		LESS("less"),
		EASE("ease");

		public final String id;

		Change(String id) {
			this.id = id;
		}
	}

	/**
	 * One correction: the plant's growth now; the factor named and which way; its value now and the value the change sets
	 * (for EASE, the strain now and the strain to ease it to); the range its niche gives that factor; how many factors hold
	 * it at its step ({@code of}: 1 when this one change raises it a step); and a graft that alone would raise it a step on
	 * this reading, or null.
	 */
	public record Advice(Verdict.Growth growth, Factor factor, Change change, int value, int target, Niche.Range range, int of,
			@Nullable String graft) {
		/** Whether this one change raises its growth a step. */
		public boolean alone() {
			return of == 1;
		}

		/** Its remedy's word id: "strain" for an eased strain, else "{factor}.{more|less}". */
		public String remedy() {
			return change == Change.EASE ? "strain" : factor.id + "." + change.id;
		}
	}

	private HabitatAdvice() {
	}

	/** {@link #one(Niche, Habitat, Map)} with no graft to offer. */
	public static @Nullable Advice one(Niche niche, Habitat habitat) {
		return one(niche, habitat, Map.of());
	}

	/** The one correction for a plant of {@code niche} on {@code habitat}, {@code grafts} its bed could take; null while it thrives. */
	public static @Nullable Advice one(Niche niche, Habitat habitat, Map<String, Niche> grafts) {
		Verdict verdict = Verdict.of(niche, habitat);
		if (verdict.growth() == Verdict.Growth.THRIVING) {
			return null;
		}
		Verdict.Growth goal = verdict.growth() == Verdict.Growth.STALLED ? Verdict.Growth.TOLERATING : Verdict.Growth.THRIVING;
		List<Factor> held = verdict.growth() == Verdict.Growth.STALLED ? verdict.outside() : verdict.tolerable();
		Factor first = held.getFirst();
		String graft = graft(grafts, habitat, goal);
		if (held.size() == 1) {
			boolean strainFirst = first == Factor.DISTURBANCE && verdict.strained();
			for (boolean easing : strainFirst ? new boolean[] {true, false} : new boolean[] {false, true}) {
				if (easing && first != Factor.DISTURBANCE) {
					continue;
				}
				int target = nearest(niche, habitat, first, easing, goal);
				if (target >= 0) {
					int value = easing ? habitat.strain() : habitat.get(first);
					Change change = easing ? Change.EASE : target > value ? Change.MORE : Change.LESS;
					return new Advice(verdict.growth(), first, change, value, target, niche.range(first), 1, graft);
				}
			}
		}
		Niche.Range range = niche.range(first);
		if (first == Factor.DISTURBANCE && verdict.strained()) {
			return new Advice(verdict.growth(), first, Change.EASE, habitat.strain(), 0, range, Math.max(2, held.size()), graft);
		}
		int value = habitat.get(first);
		int target = Math.clamp(value, range.idealMin(), range.idealMax());
		return new Advice(verdict.growth(), first, target > value ? Change.MORE : Change.LESS, value, target, range, Math.max(2, held.size()), graft);
	}

	/**
	 * The value of {@code factor} (or, {@code easing}, the strain, only ever lowered) nearest the one it has that alone raises
	 * the plant to {@code goal}, or -1 when none does.
	 */
	private static int nearest(Niche niche, Habitat habitat, Factor factor, boolean easing, Verdict.Growth goal) {
		int now = easing ? habitat.strain() : habitat.get(factor);
		int most = easing ? now : factor.max;
		for (int step = 1; step <= Math.max(now, most - now); step++) {
			for (int value : new int[] {now - step, now + step}) {
				if (value < 0 || value > most) {
					continue;
				}
				Habitat changed = easing ? new Habitat(habitat.moisture(), habitat.light(), habitat.nutrients(), habitat.diversity(),
						habitat.disturbance(), value) : habitat.with(factor, value);
				if (reaches(niche, changed, goal)) {
					return value;
				}
			}
		}
		return -1;
	}

	private static boolean reaches(Niche niche, Habitat habitat, Verdict.Growth goal) {
		return Verdict.of(niche, habitat).growth().pace >= goal.pace;
	}

	private static @Nullable String graft(Map<String, Niche> grafts, Habitat habitat, Verdict.Growth goal) {
		for (Map.Entry<String, Niche> entry : grafts.entrySet()) {
			if (reaches(entry.getValue(), habitat, goal)) {
				return entry.getKey();
			}
		}
		return null;
	}
}
