package io.github.jimbozoomer.jugcraft.concordance.alchemy;

import io.github.jimbozoomer.jugcraft.concordance.compose.Text;
import java.util.ArrayList;
import java.util.List;

/**
 * What sampling a mixture tells a player, at three levels of information (roadmap step 13):
 * <ol>
 * <li>a spoon: the temperature band, the volume, the strongest property and whether it is murky;</li>
 * <li>an assay glass: the temperature, every property dissolved and still pending, a part's share, and the
 * contaminant;</li>
 * <li>an assay glass in the hands of someone who has mastered the Alembic Arts: what a dose bottled now would do, and
 * why ({@link Outcome#reasons()}).</li>
 * </ol>
 */
public final class Assay {
	public static final int SPOON = 1;
	public static final int GLASS = 2;
	public static final int MASTERED = 3;

	private Assay() {
	}

	public static List<Text> read(Mixture mixture, int temperature, int level, AlchemyCatalog catalog) {
		List<Text> out = new ArrayList<>();
		Band band = Band.of(temperature);
		if (mixture.isEmpty()) {
			out.add(Text.of("alchemy.assay.empty", new Text.Ref("band", band.id)));
			return out;
		}
		out.add(Text.of("alchemy.assay.state", new Text.Ref("band", band.id), mixture.parts()));
		Vector concentration = mixture.concentration();
		Axis dominant = concentration.dominant();
		out.add(dominant == null ? Text.of("alchemy.assay.plain") : Text.of("alchemy.assay.taste", new Text.Ref("principle", dominant.id)));
		Property fouling = catalog.property(Property.CONTAMINANT);
		if (fouling != null && mixture.contaminantConcentration() * 2 >= fouling.threshold()) {
			out.add(Text.of("alchemy.assay.murky"));
		}
		if (level < GLASS) {
			return out;
		}
		out.add(Text.of("alchemy.assay.temperature", temperature));
		Vector pending = mixture.pending().divide(mixture.parts());
		for (Axis axis : Axis.values()) {
			if (concentration.get(axis) > 0 || pending.get(axis) > 0) {
				out.add(Text.of("alchemy.assay.axis", new Text.Ref("principle", axis.id), Vector.units(concentration.get(axis)),
						Vector.units(pending.get(axis))));
			}
		}
		out.add(Text.of("alchemy.assay.contaminant", Vector.units(mixture.contaminantConcentration())));
		if (level < MASTERED) {
			return out;
		}
		Outcome outcome = mixture.outcome(catalog);
		if (outcome.effects().isEmpty()) {
			out.add(Text.of("alchemy.assay.nothing"));
		}
		out.addAll(outcome.reasons());
		return out;
	}
}
