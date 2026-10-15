package io.github.jimbozoomer.jugcraft.concordance.ecology;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.jspecify.annotations.Nullable;

/** The loaded organisms (by id, and found by block and by item) and disturbance sources (roadmap step 14). */
public final class EcologyCatalog {
	public static final EcologyCatalog EMPTY = new EcologyCatalog(Map.of(), Map.of());

	private final Map<String, Organism> organisms;
	private final Map<String, Disturbance> disturbances;
	private final Map<String, Organism> byBlock = new HashMap<>();
	private final Map<String, Organism> byItem = new HashMap<>();

	public EcologyCatalog(Map<String, Organism> organisms, Map<String, Disturbance> disturbances) {
		this.organisms = Collections.unmodifiableMap(new TreeMap<>(organisms));
		this.disturbances = Collections.unmodifiableMap(new TreeMap<>(disturbances));
		for (Organism organism : this.organisms.values()) {
			byBlock.put(organism.block(), organism);
			if (organism.item() != null) {
				byItem.put(organism.item(), organism);
			}
		}
	}

	public Map<String, Organism> organisms() {
		return organisms;
	}

	public Map<String, Disturbance> disturbances() {
		return disturbances;
	}

	public @Nullable Organism organism(String id) {
		return organisms.get(id);
	}

	/** The organism a block is (a crop at any age, or a producer), or null. */
	public @Nullable Organism byBlock(String block) {
		return byBlock.get(block);
	}

	/** The crop an item plants, or null. */
	public @Nullable Organism byItem(String item) {
		return byItem.get(item);
	}

	/**
	 * Whether {@code kinds} (the plants round one, {@link Sampler.Area#kinds}, by block) holds another crop that speeds
	 * {@code organism}: a mixed bed (Concordance Chunk C, C-08). A neighbour that is not a crop costing nutrients, or is the
	 * organism itself, never counts (the rules report it), and a fixer is never sped.
	 */
	public boolean mixed(Organism organism, Set<String> kinds) {
		if (organism.fix() > 0) {
			return false;
		}
		for (String id : organism.neighbours().organisms()) {
			Organism neighbour = organisms.get(id);
			if (neighbour != null && neighbour.crop() && neighbour.fix() == 0 && !neighbour.id().equals(organism.id())
					&& kinds.contains(neighbour.block())) {
				return true;
			}
		}
		return false;
	}
}
