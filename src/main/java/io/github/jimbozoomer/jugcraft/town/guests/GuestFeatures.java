package io.github.jimbozoomer.jugcraft.town.guests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * The registered {@link GuestFeature}s and their options, flattened in registration order (coordinator's seam). Features
 * register from common initialization, in the fixed order of {@code GuestService.register}, so an option's index is the
 * same on client and server; the menu syncs which are available as bits, {@link #MAX_OPTIONS} at most.
 */
public final class GuestFeatures {
	public static final int MAX_OPTIONS = 64;
	private static final int PER_FEATURE = 16;

	/** One option: its feature, its id within the feature, and its flattened index. */
	public record Entry(GuestFeature feature, String option, int index) {
	}

	private static final List<GuestFeature> FEATURES = new ArrayList<>();
	private static final List<Entry> OPTIONS = new ArrayList<>();

	private GuestFeatures() {
	}

	/** Registers {@code feature} once, from a worker's register(); refuses a repeated id or too many options. */
	public static synchronized void register(GuestFeature feature) {
		List<String> options = List.copyOf(feature.options());
		Set<String> unique = new HashSet<>(options);
		if (FEATURES.stream().anyMatch(f -> f.id().equals(feature.id())) || unique.size() != options.size()
				|| options.size() > PER_FEATURE || OPTIONS.size() + options.size() > MAX_OPTIONS) {
			throw new IllegalStateException("Cannot register guest feature " + feature.id() + " with " + options);
		}
		FEATURES.add(feature);
		for (String option : options) {
			OPTIONS.add(new Entry(feature, option, OPTIONS.size()));
		}
	}

	public static synchronized List<GuestFeature> all() {
		return List.copyOf(FEATURES);
	}

	public static synchronized List<Entry> options() {
		return List.copyOf(OPTIONS);
	}

	public static synchronized @Nullable Entry option(int index) {
		return index >= 0 && index < OPTIONS.size() ? OPTIONS.get(index) : null;
	}
}
