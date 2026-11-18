package io.github.jimbozoomer.jugcraft.horses;

import java.util.ArrayList;
import java.util.List;

/**
 * Cargo conservation for saddlebags (docs/features/horse-breeds.md#saddlebags-and-cargo-conservation). Pure: no
 * Minecraft classes; HorseLineageTests runs it outside the game.
 *
 * <p>Every carrier stores {@value #SLOTS} slots whatever its capacity, so nothing is ever cut off when a capacity is
 * smaller than what a horse holds (a cross with fewer rows, an older save, a retuned breed). Only the first
 * {@code capacity} slots show in the menu. When the owner next opens the bags, {@link #plan} first moves every stack
 * stored past the capacity into an empty visible slot, in order; any stack still left over is handed back to the owner
 * (into their inventory, dropped at their feet if it is full). Until then it stays saved inside the horse, through recall,
 * knock-out and reload; on the horse's death every slot drops. Stacks move whole and only into empty slots, so a plan never
 * merges, splits, duplicates or drops anything: each stack ends in exactly one place.
 */
public final class HorseCargo {
	/** Slots every carrier keeps, whatever it can show: the largest menu, three rows of nine. */
	public static final int SLOTS = 27;

	private HorseCargo() {
	}

	/** The slots a carrier shows for {@code rows} rows of nine (0, 9 or 27). */
	public static int capacity(int rows) {
		return Math.max(0, Math.min(SLOTS, rows * 9));
	}

	/**
	 * What to do with stored stacks when the bags are opened with {@code capacity} visible slots. {@code moves} pairs are
	 * (from, to): from a slot at or past the capacity into an empty slot before it. {@code handBack} lists the slots
	 * whose stacks go back to the owner because nothing visible is free.
	 */
	public record Plan(List<int[]> moves, List<Integer> handBack) {
		public boolean empty() {
			return moves.isEmpty() && handBack.isEmpty();
		}
	}

	public static Plan plan(boolean[] occupied, int capacity) {
		int visible = Math.max(0, Math.min(capacity, occupied.length));
		boolean[] filled = occupied.clone();
		List<int[]> moves = new ArrayList<>();
		List<Integer> handBack = new ArrayList<>();
		int free = 0;
		for (int from = visible; from < filled.length; from++) {
			if (!filled[from]) {
				continue;
			}
			while (free < visible && filled[free]) {
				free++;
			}
			if (free < visible) {
				moves.add(new int[] {from, free});
				filled[free] = true;
				filled[from] = false;
			} else {
				handBack.add(from);
				filled[from] = false;
			}
		}
		return new Plan(moves, handBack);
	}
}
