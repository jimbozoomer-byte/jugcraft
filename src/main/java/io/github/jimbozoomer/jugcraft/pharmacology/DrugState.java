package io.github.jimbozoomer.jugcraft.pharmacology;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

/** Remaining online seconds, never wall-clock deadlines: leaving the server pauses all drug accounting. */
public record DrugState(List<Dose> doses, List<History> history, int strainTenths) {
	public static final DrugState EMPTY = new DrugState(List.of(), List.of(), 0);
	/** Missing strain is a legacy save, migrated once by Pharmacology.state after profiles are registered. */
	public DrugState(List<Dose> doses, List<History> history) { this(doses, history, -1); }
	public static final Codec<DrugState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Dose.CODEC.listOf(0, 32).optionalFieldOf("doses", List.of()).forGetter(DrugState::doses),
			History.CODEC.listOf(0, 16).optionalFieldOf("history", List.of()).forGetter(DrugState::history),
			Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("strain_tenths", -1).forGetter(DrugState::strainTenths)
	).apply(instance, DrugState::new));

	public DrugState {
		if (doses.size() > 32 || history.size() > 16 || strainTenths < -1) throw new IllegalArgumentException("Invalid pharmacy state");
		doses = List.copyOf(doses);
		history = List.copyOf(history);
	}

	public record Dose(String drug, int total, int remaining, int onset, int recovery) {
		public static final Codec<Dose> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("drug").forGetter(Dose::drug),
				Codec.intRange(1, 3600).fieldOf("total").forGetter(Dose::total),
				Codec.intRange(0, 3600).fieldOf("remaining").forGetter(Dose::remaining),
				Codec.intRange(0, 60).fieldOf("onset").forGetter(Dose::onset),
				Codec.intRange(0, 3600).fieldOf("recovery").forGetter(Dose::recovery)
		).apply(instance, Dose::new));

		public boolean active() {
			return remaining > 0;
		}

		public Dose recovering() {
			return recovering(Pharmacology.RECOVERY_SECONDS);
		}

		public Dose recovering(int seconds) {
			return new Dose(drug, total, 0, 0, seconds);
		}
	}

	public record History(String family, int doses, int recovery) {
		public static final Codec<History> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("family").forGetter(History::family),
				Codec.intRange(1, 3).fieldOf("doses").forGetter(History::doses),
				Codec.intRange(0, 1800).fieldOf("recovery").forGetter(History::recovery)
		).apply(instance, History::new));
	}
}
