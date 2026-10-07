package io.github.jimbozoomer.jugcraft.concordance.progression;

import io.github.jimbozoomer.jugcraft.concordance.rules.Definitions;
import java.util.List;

/**
 * What makes a practice possible at all (roadmap step 24, data: concordance/practice): the research states a player
 * needs before the deed a practice records can happen (a ritual needs Circle Lore understood, an observation the
 * Celestial Attunement), and the tradition the practice belongs to. A mastery that asks for a practice therefore needs
 * what the practice needs.
 */
public record PracticeGate(String activity, String tradition, List<Definitions.Requirement> requires) {
	public PracticeGate {
		requires = List.copyOf(requires);
	}
}
