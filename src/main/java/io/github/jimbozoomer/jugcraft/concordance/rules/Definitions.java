package io.github.jimbozoomer.jugcraft.concordance.rules;

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The shared definitions of the Arcane Concordance, as loaded from {@code data/<ns>/concordance/}. These are the same
 * for every player; what one player has learned is a {@link Knowledge}. tools/concordance.py writes Jugcraft's own,
 * and a data pack may add or replace them: {@link RulesParser} rejects malformed ones with a diagnostic.
 */
public final class Definitions {
	private Definitions() {
	}

	/** One prerequisite: another entry that must have reached a state before this one can progress. */
	public record Requirement(String research, ResearchState state) {
	}

	/** What reaching a state makes available. */
	public record Unlocks(List<String> invocations, List<String> workings) {
		public static final Unlocks NONE = new Unlocks(List.of(), List.of());
	}

	/**
	 * A research entry. {@code states} maps each state, in order from {@link ResearchState#ENCOUNTERED}, to its
	 * alternatives; an entry need not define every state (one without a mastery has no {@code mastered}).
	 */
	public record Research(String id, int schema, String principle, String tradition, String stage, String icon,
			List<Requirement> requires, Map<ResearchState, List<EvidenceRule>> states, Map<ResearchState, Unlocks> unlocks) {
		public ResearchState highest() {
			ResearchState highest = ResearchState.NONE;
			for (ResearchState state : states.keySet()) {
				if (state.ordinal() > highest.ordinal()) {
					highest = state;
				}
			}
			return highest;
		}
	}

	/** An invocation: the Spell Engine spell it casts, the research that teaches it and its Focus cost. */
	public record Invocation(String id, int schema, String spell, String principle, String research, ResearchState state,
			int focus, int masteredFocus) {
		/** The Focus it costs a player whose research has reached {@code reached}. */
		public int cost(ResearchState reached) {
			return reached == ResearchState.MASTERED ? masteredFocus : focus;
		}
	}

	public enum WorkingType {
		CRAFT("craft"),
		INFUSE("infuse"),
		CHANNEL("channel");

		public final String id;

		WorkingType(String id) {
			this.id = id;
		}

		public static @Nullable WorkingType fromId(String id) {
			for (WorkingType type : values()) {
				if (type.id.equals(id)) {
					return type;
				}
			}
			return null;
		}
	}

	/**
	 * A process at a station. {@code CRAFT} turns {@code work} plus one {@code specimen} into {@code result} holding
	 * {@code radiance}; {@code INFUSE} adds one specimen's value from {@code specimens} to the work; {@code CHANNEL}
	 * turns {@code focus} of the player's Focus into {@code radiance} in the work.
	 */
	public record Working(String id, int schema, WorkingType type, String station, String research, ResearchState state,
			String work, @Nullable String specimen, @Nullable String result, int radiance, Map<String, Integer> specimens,
			int focus) {
	}
}
