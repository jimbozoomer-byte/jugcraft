package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.journal.Journal;
import io.github.jimbozoomer.jugcraft.concordance.rules.ConcordanceRules;
import io.github.jimbozoomer.jugcraft.concordance.rules.Knowledge;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchEngine;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Concordance garden cohort, GC-01 (SG7, GB17): the journal's pacts section. Stolas's Regard's lines as Chunk D wrote them
 * (StolasRegard.regard); then, once its reader has begun it (GC-P16), each other research a perch's offers need, once,
 * in the perch's order: its next step by its own rules (ResearchEngine.explain, as StolasRegard.step reads his), and once
 * it is understood which spirits it lets them invite; then their pacts and departures (Roster.report). Reads only:
 * talking and reading teach nothing.
 */
public final class PactRegards {
	private PactRegards() {
	}

	public static void report(ServerPlayer player, Journal.Report out) {
		StolasRegard.regard(player, out);
		ConcordanceRules rules = ConcordanceData.rules();
		Knowledge knowledge = ConcordanceProgress.knowledge(player);
		for (String research : researches()) {
			ResearchState has = knowledge.state(research);
			if (research.equals(StolasRegard.RESEARCH) || rules.research(research) == null || has == ResearchState.NONE) {
				continue;
			}
			Component name = ConcordanceProgress.researchName(research);
			Component state = Component.translatable("journal.jugcraft.research.has", Component.translatable("journal.jugcraft.state." + has.id()));
			switch (ResearchEngine.explain(knowledge, rules, research, ConcordanceProgress.TAG_LOOKUP).getFirst()) {
				case ResearchEngine.Explanation.Unknown unknown -> {
				}
				case ResearchEngine.Explanation.NeedsPrerequisite needs -> out.line(Component.translatable("journal.jugcraft.pacts.research.needs", name,
						ConcordanceProgress.researchName(needs.requirement().research())), state);
				case ResearchEngine.Explanation.Complete complete -> out.line(Component.translatable("journal.jugcraft.pacts.research.complete", name), state);
				case ResearchEngine.Explanation.Next next -> {
					Identifier advancement = ConcordanceProgress.advancementFor(research, next.state());
					out.line(Component.translatable("journal.jugcraft.pacts.research.next", name,
							Component.translatable("advancements." + advancement.getNamespace() + "." + advancement.getPath() + ".description")), state);
				}
			}
			if (has.atLeast(ResearchState.UNDERSTOOD)) {
				out.line(Component.translatable("journal.jugcraft.pacts.research.invite", name, ComponentUtils.formatList(spirits(research), PactKeeping::name)));
			}
		}
		Roster.report(player, out);
	}

	/** The researches the perch's offers need, each once, in the perch's order. */
	private static Set<String> researches() {
		Set<String> out = new LinkedHashSet<>();
		for (PactDefinition definition : PactPerchBlockEntity.offered()) {
			out.add(definition.research());
		}
		return out;
	}

	/** The counterparts whose offers need {@code research}, in the perch's order. */
	private static List<String> spirits(String research) {
		return PactPerchBlockEntity.offered().stream().filter(d -> d.research().equals(research)).map(PactDefinition::id).toList();
	}
}
