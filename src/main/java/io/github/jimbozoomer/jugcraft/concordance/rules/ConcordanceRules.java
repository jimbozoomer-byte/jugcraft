package io.github.jimbozoomer.jugcraft.concordance.rules;

import com.google.gson.JsonElement;
import io.github.jimbozoomer.jugcraft.concordance.compose.Catalog;
import io.github.jimbozoomer.jugcraft.concordance.compose.Component;
import io.github.jimbozoomer.jugcraft.concordance.compose.Instrument;
import io.github.jimbozoomer.jugcraft.concordance.resource.Conversion;
import io.github.jimbozoomer.jugcraft.concordance.resource.ConversionTable;
import io.github.jimbozoomer.jugcraft.concordance.resource.ResourceType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.jspecify.annotations.Nullable;

/**
 * One validated set of Concordance definitions: what the server enforces until the next data reload. Built from
 * parsed files by {@link #build}, which drops anything whose references do not resolve (a prerequisite, unlock or
 * research that does not exist, or a prerequisite cycle) and records why in {@link #problems()}. Immutable, so a reload
 * swaps it whole and a definition never changes under a running operation.
 */
public final class ConcordanceRules {
	public static final ConcordanceRules EMPTY = new ConcordanceRules(Map.of(), Map.of(), Map.of(), ConversionTable.EMPTY, Catalog.EMPTY,
			List.of());

	private final Map<String, Definitions.Research> research;
	private final Map<String, Definitions.Invocation> invocations;
	private final Map<String, Definitions.Working> workings;
	private final ConversionTable conversions;
	private final Catalog catalog;
	private final List<String> problems;
	private final Map<String, Definitions.Invocation> bySpell = new HashMap<>();

	private ConcordanceRules(Map<String, Definitions.Research> research, Map<String, Definitions.Invocation> invocations,
			Map<String, Definitions.Working> workings, ConversionTable conversions, Catalog catalog, List<String> problems) {
		this.research = research;
		this.invocations = invocations;
		this.workings = workings;
		this.conversions = conversions;
		this.catalog = catalog;
		this.problems = problems;
		for (Definitions.Invocation invocation : invocations.values()) {
			bySpell.put(invocation.spell(), invocation);
		}
	}

	public Map<String, Definitions.Research> research() {
		return research;
	}

	public Map<String, Definitions.Invocation> invocations() {
		return invocations;
	}

	public Map<String, Definitions.Working> workings() {
		return workings;
	}

	/** The explicit conversions between resource types; nothing converts without one. */
	public ConversionTable conversions() {
		return conversions;
	}

	/** The composition grammar: components and instruments. */
	public Catalog catalog() {
		return catalog;
	}

	public List<String> problems() {
		return problems;
	}

	public Definitions.@Nullable Research research(String id) {
		return research.get(id);
	}

	public Definitions.@Nullable Invocation invocation(String id) {
		return invocations.get(id);
	}

	/** The invocation that casts this Spell Engine spell, or null if it is not a Concordance spell. */
	public Definitions.@Nullable Invocation invocationForSpell(String spell) {
		return bySpell.get(spell);
	}

	public Definitions.@Nullable Working working(String id) {
		return workings.get(id);
	}

	/** Entries a player can start with no other research: there must be at least one. */
	public List<String> entryPoints() {
		List<String> out = new ArrayList<>();
		for (Definitions.Research entry : research.values()) {
			if (entry.requires().isEmpty()) {
				out.add(entry.id());
			}
		}
		return out;
	}

	/**
	 * The files under {@code data/<ns>/concordance/}: kind (research, invocation, working, conversion, component or
	 * instrument), id, content.
	 */
	public record Source(String kind, String id, JsonElement json) {
	}

	public static ConcordanceRules build(List<Source> sources) {
		RulesParser parser = new RulesParser();
		Map<String, Definitions.Research> research = new TreeMap<>();
		Map<String, Definitions.Invocation> invocations = new TreeMap<>();
		Map<String, Definitions.Working> workings = new TreeMap<>();
		List<Conversion> conversionList = new ArrayList<>();
		Map<String, Component> components = new TreeMap<>();
		Map<String, Instrument> instruments = new TreeMap<>();
		List<String> problems = new ArrayList<>();
		for (Source source : sources) {
			switch (source.kind()) {
				case "research" -> {
					Definitions.Research entry = parser.research(source.id(), source.json());
					if (entry != null) {
						research.put(entry.id(), entry);
					}
				}
				case "invocation" -> {
					Definitions.Invocation entry = parser.invocation(source.id(), source.json());
					if (entry != null) {
						invocations.put(entry.id(), entry);
					}
				}
				case "working" -> {
					Definitions.Working entry = parser.working(source.id(), source.json());
					if (entry != null) {
						workings.put(entry.id(), entry);
					}
				}
				case "conversion" -> {
					Conversion conversion = parser.conversion(source.id(), source.json());
					if (conversion != null) {
						conversionList.add(conversion);
					}
				}
				case "component" -> {
					Component component = parser.component(source.id(), source.json());
					if (component != null) {
						components.put(component.id(), component);
					}
				}
				case "instrument" -> {
					Instrument instrument = parser.instrument(source.id(), source.json());
					if (instrument != null) {
						instruments.put(instrument.id(), instrument);
					}
				}
				default -> problems.add(source.id() + ": unknown kind of Concordance file \"" + source.kind()
						+ "\" (expected research, invocation, working, conversion, component or instrument)");
			}
		}
		problems.addAll(0, parser.problems());
		// Prerequisites must exist and must not loop; entries in a loop could never be started.
		boolean changed = true;
		while (changed) {
			changed = false;
			for (Definitions.Research entry : List.copyOf(research.values())) {
				for (Definitions.Requirement requirement : entry.requires()) {
					Definitions.Research needed = research.get(requirement.research());
					if (needed == null || !needed.states().containsKey(requirement.state())) {
						problems.add("research " + entry.id() + ": requires " + requirement.research() + " "
								+ requirement.state().id() + ", which does not exist");
						research.remove(entry.id());
						changed = true;
						break;
					}
				}
			}
		}
		for (String cycle : cycles(research)) {
			problems.add("research " + cycle + ": its prerequisites lead back to itself");
			research.remove(cycle);
		}
		// Invocations need a research state to learn them from.
		for (Definitions.Invocation invocation : List.copyOf(invocations.values())) {
			Definitions.Research entry = research.get(invocation.research());
			if (entry == null || !entry.states().containsKey(invocation.state())) {
				problems.add("invocation " + invocation.id() + ": learnt from " + invocation.research() + " "
						+ invocation.state().id() + ", which does not exist");
				invocations.remove(invocation.id());
			}
		}
		ConversionTable conversions = ConversionTable.build(conversionList);
		problems.addAll(conversions.problems());
		// A channel runs its conversion recipe: Focus into the Radiance a lantern holds, always losing.
		for (Definitions.Working working : List.copyOf(workings.values())) {
			if (working.type() != Definitions.WorkingType.CHANNEL) {
				continue;
			}
			Conversion conversion = working.conversion() == null ? null : conversions.get(working.conversion());
			if (conversion == null || !conversion.from().equals(ResourceType.FOCUS) || !conversion.to().equals(ResourceType.RADIANCE)) {
				problems.add("working " + working.id() + ": channels by conversion " + working.conversion()
						+ ", which does not exist or does not turn focus into essence/radiance");
				workings.remove(working.id());
			} else if (conversion.toAmount() >= conversion.fromAmount() || conversion.fromAmount() > 1000) {
				// Focus returns with time; a channel that gave back as much as it took would be a free conversion.
				problems.add("working " + working.id() + ": conversion " + conversion.id() + " must give less than it takes");
				workings.remove(working.id());
			} else {
				workings.put(working.id(), working.withChannel((int) conversion.fromAmount(), (int) conversion.toAmount()));
			}
		}
		for (Definitions.Working working : List.copyOf(workings.values())) {
			Definitions.Research entry = research.get(working.research());
			if (entry == null || !entry.states().containsKey(working.state())) {
				problems.add("working " + working.id() + ": needs " + working.research() + " " + working.state().id()
						+ ", which does not exist");
				workings.remove(working.id());
			}
		}
		// Unlock lists and invoke evidence must name things that exist (a typo would silently teach nothing).
		for (Definitions.Research entry : research.values()) {
			for (Map.Entry<ResearchState, Definitions.Unlocks> unlock : entry.unlocks().entrySet()) {
				for (String id : unlock.getValue().invocations()) {
					if (!invocations.containsKey(id)) {
						problems.add("research " + entry.id() + ": unlocks unknown invocation " + id);
					}
				}
				for (String id : unlock.getValue().workings()) {
					if (!workings.containsKey(id)) {
						problems.add("research " + entry.id() + ": unlocks unknown working " + id);
					}
				}
			}
			for (List<EvidenceRule> rules : entry.states().values()) {
				for (EvidenceRule rule : rules) {
					if (rule.kind() == EvidenceRule.Kind.INVOKE && !invocations.containsKey(rule.invocation())) {
						problems.add("research " + entry.id() + ": its evidence names unknown invocation " + rule.invocation());
					}
				}
			}
		}
		// A component is learnt from a research state like an invocation; one naming none could never be used.
		for (Component component : List.copyOf(components.values())) {
			Definitions.Research entry = research.get(component.requires().research());
			if (entry == null || !entry.states().containsKey(component.requires().state())) {
				problems.add("component " + component.id() + ": needs " + component.requires().research() + " "
						+ component.requires().state().id() + ", which does not exist");
				components.remove(component.id());
			}
		}
		if (!research.isEmpty() && research.values().stream().noneMatch(entry -> entry.requires().isEmpty())) {
			problems.add("no research entry can be started without another: the Concordance has no way in");
		}
		return new ConcordanceRules(Collections.unmodifiableMap(new LinkedHashMap<>(research)),
				Collections.unmodifiableMap(new LinkedHashMap<>(invocations)),
				Collections.unmodifiableMap(new LinkedHashMap<>(workings)), conversions, new Catalog(components, instruments),
				List.copyOf(problems));
	}

	/** Entries that are part of, or depend on, a prerequisite cycle. */
	private static Set<String> cycles(Map<String, Definitions.Research> research) {
		Set<String> done = new HashSet<>();
		Set<String> bad = new HashSet<>();
		for (String start : research.keySet()) {
			visit(start, research, new HashSet<>(), done, bad);
		}
		return bad;
	}

	private static boolean visit(String id, Map<String, Definitions.Research> research, Set<String> path, Set<String> done,
			Set<String> bad) {
		if (bad.contains(id)) {
			return true;
		}
		if (done.contains(id)) {
			return false;
		}
		if (!path.add(id)) {
			bad.add(id);
			return true;
		}
		boolean looped = false;
		Definitions.Research entry = research.get(id);
		if (entry != null) {
			for (Definitions.Requirement requirement : entry.requires()) {
				looped |= visit(requirement.research(), research, path, done, bad);
			}
		}
		path.remove(id);
		if (looped) {
			bad.add(id);
		} else {
			done.add(id);
		}
		return looped;
	}
}
