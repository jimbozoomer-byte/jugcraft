package io.github.jimbozoomer.jugcraft.concordance.alchemy;

import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Where a crucible's temperature heads: towards the heat of the block beneath it ({@link #SOURCES}, by block id; a
 * campfire only while lit), or {@link #AMBIENT} with none, by {@link #RATE} degrees a tick. Worked out on the server
 * each tick from the block below, so a fire put out or lit changes the process at once. Keep equal to
 * tools/concordance_alchemy.py.
 */
public final class Heat {
	public static final int AMBIENT = 20;
	public static final int RATE = 1;
	public static final Map<String, Integer> SOURCES = Map.of(
			"minecraft:magma_block", 60,
			"minecraft:soul_campfire", 100,
			"minecraft:campfire", 120,
			"minecraft:soul_fire", 150,
			"minecraft:fire", 175,
			"minecraft:lava", 220);

	private Heat() {
	}

	/** The block beneath a crucible (Concordance Chunk C, C-01): its id and whether it is a source that is out (an unlit campfire). */
	public record Below(String block, boolean unlit) {
		/** Its heat as a source ({@link Heat#SOURCES}), lit or not; null for a block that does not heat a crucible. */
		public @Nullable Integer heat() {
			return SOURCES.get(block);
		}

		/** Where it heats a crucible towards: its heat while lit, the air's otherwise. */
		public int target() {
			Integer heat = heat();
			return heat == null || unlit ? AMBIENT : heat;
		}
	}

	/** The temperature a tick later, moving towards {@code target}. */
	public static int toward(int temperature, int target) {
		if (temperature < target) {
			return Math.min(target, temperature + RATE);
		}
		return Math.max(target, temperature - RATE);
	}
}
