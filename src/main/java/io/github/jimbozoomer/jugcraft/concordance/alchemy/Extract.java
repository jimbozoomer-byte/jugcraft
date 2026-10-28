package io.github.jimbozoomer.jugcraft.concordance.alchemy;

import io.github.jimbozoomer.jugcraft.concordance.compose.Text;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * A covered extraction's item (Concordance C-D6, Gloam Ink; data: {@code data/<ns>/concordance/extract/<name>.json}):
 * what a glass bottle draws, instead of a draught, from a covered batch ({@link Mixture#covered()}: stirred, every stir
 * under a lid) made of its {@code base} at least once and at least one of its {@code carriers}, and nothing else
 * ({@link #madeOf}). Murky ({@link Assay#murky}, what a spoon tells) it draws its {@code failure}, no use as the item;
 * clean, its {@code item} once a dose holds at least {@code least} milli-units of {@code axis} a part; thinner, the
 * draught, as uncovered. A bowl draws a salve as ever. Every dose of a batch is the same share (Mixture.bottle), so a
 * batch draws one thing. Never a brew: brews feed no recipe (tools/check_mod_data.py check_workshop_products). Pure.
 *
 * @param id       {@code <ns>:<name>}, stable once released
 * @param item     the item a clean batch draws: a glass bottle's, which gives the bottle back when crafted
 * @param failure  the item a murky batch draws
 * @param base     the ingredient (item id) every batch holds at least once
 * @param carriers the ingredients (item ids) of which every batch holds at least one; nothing else may go in
 * @param axis     the property a dose must hold
 * @param least    milli-units of it a part, at least (above 0)
 * @param codex    the codex entry that explains it ({@code alembic/<entry>})
 */
public record Extract(String id, String item, String failure, String base, List<String> carriers, Axis axis, long least, String codex) {
	public Extract {
		carriers = List.copyOf(carriers);
		if (carriers.isEmpty() || carriers.contains(base) || least < 1) {
			throw new IllegalArgumentException(id + ": at least one carrier, none of them the base, and least above 0");
		}
	}

	/** What a glass bottle draws: the extract's item, its failure (murky), or the draught (too thin). */
	public enum Kind {
		ITEM, FAILURE, THIN
	}

	/** What a glass bottle draws from a covered batch of an extract's ingredients, and the lines that say why. */
	public record Drawn(Extract extract, Kind kind, List<Text> reasons) {
		public Drawn {
			reasons = List.copyOf(reasons);
		}

		/** The item id drawn (its item, or, murky, its failure), or null when the bottle draws the draught. */
		public @Nullable String made() {
			return switch (kind) {
				case ITEM -> extract.item();
				case FAILURE -> extract.failure();
				case THIN -> null;
			};
		}
	}

	/** Its base, then its carriers: every ingredient a batch of it may hold. */
	public List<String> ingredients() {
		List<String> out = new ArrayList<>();
		out.add(base);
		out.addAll(carriers);
		return out;
	}

	/**
	 * Whether {@code history}'s ingredients are this extract's: its base at least once, at least one carrier, and
	 * nothing else (any preparation). At most Mixture.MAX_OPERATIONS read.
	 */
	public boolean madeOf(List<Operation> history) {
		boolean based = false;
		boolean carried = false;
		for (Operation operation : history) {
			if (operation instanceof Operation.Add add) {
				if (add.item().equals(base)) {
					based = true;
				} else if (carriers.contains(add.item())) {
					carried = true;
				} else {
					return false;
				}
			}
		}
		return based && carried;
	}

	/**
	 * What a glass bottle draws from {@code mixture} under these rules (C-D6): null when no extract applies (empty, not
	 * covered, or not made of exactly one extract's ingredients); else that extract, judged on one dose: murky, its
	 * failure; at least its least of its axis a part, its item; thinner, THIN (the draught). The crucible's bottling, the
	 * master's glass, the formula's preview and the risk line all ask this.
	 */
	public static @Nullable Drawn drawn(Mixture mixture, AlchemyCatalog catalog) {
		if (mixture.isEmpty() || !mixture.covered()) {
			return null;
		}
		Extract extract = catalog.extract(mixture.history());
		if (extract == null) {
			return null;
		}
		long contaminant = mixture.contaminantConcentration();
		long amount = mixture.concentration().get(extract.axis());
		Text.Ref item = new Text.Ref("item", extract.item());
		Text.Ref principle = new Text.Ref("principle", extract.axis().id);
		if (Assay.murky(contaminant, catalog)) {
			return new Drawn(extract, Kind.FAILURE, List.of(Text.of("alchemy.extract.murky", new Text.Ref("item", extract.failure()), item,
					Vector.units(contaminant), Vector.units(Assay.murkyFrom(catalog)))));
		}
		if (amount < extract.least()) {
			return new Drawn(extract, Kind.THIN, List.of(Text.of("alchemy.extract.thin", item, principle, Vector.units(amount),
					Vector.units(extract.least()))));
		}
		return new Drawn(extract, Kind.ITEM, List.of(Text.of("alchemy.extract.drawn", item, principle, Vector.units(amount),
				Vector.units(extract.least()))));
	}
}
