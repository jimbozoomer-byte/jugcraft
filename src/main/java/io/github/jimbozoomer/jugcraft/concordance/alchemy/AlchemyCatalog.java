package io.github.jimbozoomer.jugcraft.concordance.alchemy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The loaded alchemy data: ingredients by item id, preparations, properties, (Concordance Chunk C, C-02) workshop
 * products by id and (Concordance C-D6) covered extractions by id. Immutable; part of the {@link io.github.jimbozoomer.jugcraft.concordance.rules.ConcordanceRules} a
 * reload swaps whole.
 */
public record AlchemyCatalog(Map<String, Ingredient> ingredients, Map<String, Preparation> preparations,
		Map<String, Property> properties, Map<String, Product> products, Map<String, Extract> extracts) {
	public static final AlchemyCatalog EMPTY = new AlchemyCatalog(Map.of(), Map.of(), Map.of(), Map.of(), Map.of());

	public AlchemyCatalog {
		ingredients = Collections.unmodifiableMap(new LinkedHashMap<>(ingredients));
		preparations = Collections.unmodifiableMap(new LinkedHashMap<>(preparations));
		properties = Collections.unmodifiableMap(new LinkedHashMap<>(properties));
		products = Collections.unmodifiableMap(new LinkedHashMap<>(products));
		extracts = Collections.unmodifiableMap(new LinkedHashMap<>(extracts));
	}

	/** The ingredient an item is, or null. */
	public @Nullable Ingredient ingredient(String item) {
		return ingredients.get(item);
	}

	public @Nullable Preparation preparation(String id) {
		return preparations.get(id);
	}

	/** The property for an axis id or {@link Property#CONTAMINANT}, or null if that does nothing. */
	public @Nullable Property property(String id) {
		return properties.get(id);
	}

	/** The preparation an item is used as when it goes in as it is: the one with no tool, if there is one. */
	public @Nullable Preparation asItComes() {
		for (Preparation preparation : preparations.values()) {
			if (preparation.tool() == null) {
				return preparation;
			}
		}
		return null;
	}

	/** The preparation a tool makes, or null. */
	public @Nullable Preparation madeWith(String tool) {
		for (Preparation preparation : preparations.values()) {
			if (tool.equals(preparation.tool())) {
				return preparation;
			}
		}
		return null;
	}

	/**
	 * The product a brew of {@code form} carrying {@code statuses} is (Concordance Chunk C, C-02, C-P2): the one product of
	 * that form whose status it carries, when it carries nothing {@code harmful}. Null if none matches, or if more than one
	 * does: an ambiguous brew stays unnamed rather than guess.
	 */
	public @Nullable Product product(Product.Form form, Collection<String> statuses, boolean harmful) {
		if (harmful) {
			return null;
		}
		Product found = null;
		for (Product product : products.values()) {
			if (product.form() == form && statuses.contains(product.status())) {
				if (found != null) {
					return null;
				}
				found = product;
			}
		}
		return found;
	}

	/**
	 * The extract a batch of {@code history}'s ingredients is (Concordance C-D6): the one whose base and carriers it is made
	 * of (Extract.madeOf). Null if none, or if more than one is (ConcordanceRules lets no ingredient be in two, so this
	 * guards only hand-made catalogues): an ambiguous batch draws the draught rather than guess.
	 */
	public @Nullable Extract extract(List<Operation> history) {
		Extract found = null;
		for (Extract extract : extracts.values()) {
			if (extract.madeOf(history)) {
				if (found != null) {
					return null;
				}
				found = extract;
			}
		}
		return found;
	}
}
