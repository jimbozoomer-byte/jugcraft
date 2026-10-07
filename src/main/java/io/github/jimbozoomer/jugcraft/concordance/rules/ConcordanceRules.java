package io.github.jimbozoomer.jugcraft.concordance.rules;

import com.google.gson.JsonElement;
import io.github.jimbozoomer.jugcraft.concordance.compose.Authored;
import io.github.jimbozoomer.jugcraft.concordance.compose.Catalog;
import io.github.jimbozoomer.jugcraft.concordance.compose.Compiler;
import io.github.jimbozoomer.jugcraft.concordance.compose.Component;
import io.github.jimbozoomer.jugcraft.concordance.compose.Composition;
import io.github.jimbozoomer.jugcraft.concordance.compose.CompositionParser;
import io.github.jimbozoomer.jugcraft.concordance.compose.Instrument;
import io.github.jimbozoomer.jugcraft.concordance.compose.Plan;
import io.github.jimbozoomer.jugcraft.concordance.compose.Slot;
import io.github.jimbozoomer.jugcraft.concordance.compose.Text;
import io.github.jimbozoomer.jugcraft.concordance.resource.Conversion;
import io.github.jimbozoomer.jugcraft.concordance.resource.ConversionTable;
import io.github.jimbozoomer.jugcraft.concordance.resource.ResourceType;
import io.github.jimbozoomer.jugcraft.concordance.ritual.RitualDefinition;
import io.github.jimbozoomer.jugcraft.concordance.ritual.StructurePattern;
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
			Map.of(), Map.of(), Map.of(), List.of());

	private final Map<String, Definitions.Research> research;
	private final Map<String, Definitions.Invocation> invocations;
	private final Map<String, Definitions.Working> workings;
	private final ConversionTable conversions;
	private final Catalog catalog;
	private final Map<String, Map<String, Authored>> authored;
	private final Map<String, StructurePattern> structures;
	private final Map<String, RitualDefinition> rituals;
	private final List<String> problems;
	private final Map<String, Definitions.Invocation> bySpell = new HashMap<>();

	private ConcordanceRules(Map<String, Definitions.Research> research, Map<String, Definitions.Invocation> invocations,
			Map<String, Definitions.Working> workings, ConversionTable conversions, Catalog catalog,
			Map<String, Map<String, Authored>> authored, Map<String, StructurePattern> structures, Map<String, RitualDefinition> rituals,
			List<String> problems) {
		this.research = research;
		this.invocations = invocations;
		this.workings = workings;
		this.conversions = conversions;
		this.catalog = catalog;
		this.authored = authored;
		this.structures = structures;
		this.rituals = rituals;
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

	/** Ritual structures, by id. */
	public Map<String, StructurePattern> structures() {
		return structures;
	}

	/** Rituals, by id; each names a loaded structure and research state. */
	public Map<String, RitualDefinition> rituals() {
		return rituals;
	}

	public @Nullable StructurePattern structure(String id) {
		return structures.get(id);
	}

	public @Nullable RitualDefinition ritual(String id) {
		return rituals.get(id);
	}

	/** The rituals performed at an anchor block (by block id), in id order. */
	public List<RitualDefinition> ritualsAt(String anchor) {
		List<RitualDefinition> out = new ArrayList<>();
		for (RitualDefinition ritual : rituals.values()) {
			StructurePattern pattern = structures.get(ritual.structure());
			if (pattern != null && pattern.anchor().equals(anchor)) {
				out.add(ritual);
			}
		}
		return out;
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

	/**
	 * An invocation as compiled for an instrument (by instrument id), or null if it does not fit that instrument. Every
	 * loaded invocation fits at least one.
	 */
	public @Nullable Authored authored(String invocation, String instrument) {
		Map<String, Authored> forms = authored.get(invocation);
		return forms == null ? null : forms.get(instrument);
	}

	/** An invocation's compiled forms, by instrument id (empty if it is not loaded). */
	public Map<String, Authored> authored(String invocation) {
		return authored.getOrDefault(invocation, Map.of());
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
	 * The files under {@code data/<ns>/concordance/}: kind (research, invocation, working, conversion, component,
	 * instrument, structure or ritual), id, content.
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
		Map<String, StructurePattern> structures = new TreeMap<>();
		Map<String, RitualDefinition> rituals = new TreeMap<>();
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
				case "structure" -> {
					StructurePattern structure = parser.structure(source.id(), source.json());
					if (structure != null) {
						structures.put(structure.id(), structure);
					}
				}
				case "ritual" -> {
					RitualDefinition ritual = parser.ritual(source.id(), source.json());
					if (ritual != null) {
						rituals.put(ritual.id(), ritual);
					}
				}
				default -> problems.add(source.id() + ": unknown kind of Concordance file \"" + source.kind()
						+ "\" (expected research, invocation, working, conversion, component, instrument, structure or ritual)");
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
		// A component is learnt from a research state like an invocation; one naming none could never be used.
		for (Component component : List.copyOf(components.values())) {
			Definitions.Research entry = research.get(component.requires().research());
			if (entry == null || !entry.states().containsKey(component.requires().state())) {
				problems.add("component " + component.id() + ": needs " + component.requires().research() + " "
						+ component.requires().state().id() + ", which does not exist");
				components.remove(component.id());
			}
		}
		Catalog catalog = new Catalog(components, instruments);
		// Invocations need a research state to learn them from, and a composition that compiles.
		Map<String, Map<String, Authored>> authored = new TreeMap<>();
		Map<String, String> signatures = new HashMap<>();
		for (Definitions.Invocation invocation : List.copyOf(invocations.values())) {
			Definitions.Research entry = research.get(invocation.research());
			if (entry == null || !entry.states().containsKey(invocation.state())) {
				problems.add("invocation " + invocation.id() + ": learnt from " + invocation.research() + " "
						+ invocation.state().id() + ", which does not exist");
				invocations.remove(invocation.id());
				continue;
			}
			Map<String, Authored> forms = compile(invocation, catalog, problems);
			if (forms == null) {
				invocations.remove(invocation.id());
				continue;
			}
			// Two invocations that select and do the same would be one spell with two prices.
			String signature = forms.values().iterator().next().plan().signature();
			String same = signatures.putIfAbsent(signature, invocation.id());
			if (same != null) {
				problems.add("invocation " + invocation.id() + ": does what " + same + " does (" + signature + ")");
				invocations.remove(invocation.id());
				continue;
			}
			authored.put(invocation.id(), Collections.unmodifiableMap(forms));
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
		// A ritual is built around a loaded structure and learnt from a research state like an invocation. One that
		// draws Ley Charge needs channels to draw it from.
		for (RitualDefinition ritual : List.copyOf(rituals.values())) {
			StructurePattern structure = structures.get(ritual.structure());
			Definitions.Research entry = research.get(ritual.research());
			String problem = null;
			if (structure == null) {
				problem = "is built around structure " + ritual.structure() + ", which does not exist";
			} else if (entry == null || !entry.states().containsKey(ritual.state())) {
				problem = "is learnt from " + ritual.research() + " " + ritual.state().id() + ", which does not exist";
			} else if (ritual.ley() > 0 && structure.channels().isEmpty()) {
				problem = "draws Ley Charge, but structure " + structure.id() + " has no channels";
			}
			if (problem != null) {
				problems.add("ritual " + ritual.id() + ": " + problem);
				rituals.remove(ritual.id());
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
				for (String id : unlock.getValue().rituals()) {
					if (!rituals.containsKey(id)) {
						problems.add("research " + entry.id() + ": unlocks unknown ritual " + id);
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
		if (!research.isEmpty() && research.values().stream().noneMatch(entry -> entry.requires().isEmpty())) {
			problems.add("no research entry can be started without another: the Concordance has no way in");
		}
		return new ConcordanceRules(Collections.unmodifiableMap(new LinkedHashMap<>(research)),
				Collections.unmodifiableMap(new LinkedHashMap<>(invocations)),
				Collections.unmodifiableMap(new LinkedHashMap<>(workings)), conversions, catalog,
				Collections.unmodifiableMap(authored), Collections.unmodifiableMap(new LinkedHashMap<>(structures)),
				Collections.unmodifiableMap(new LinkedHashMap<>(rituals)), List.copyOf(problems));
	}

	/**
	 * Compiles an invocation's composition, untuned and with each tuning, for every instrument it fits, and checks what
	 * its author declared: that it costs at least the Focus its composition would ({@link #mayCost}), and that no form
	 * spends more work or makes anything last longer than {@link Definitions.Invocation#work} and
	 * {@link Definitions.Invocation#persists}. Returns null (with the reasons recorded) if it cannot be loaded; a tuning
	 * that is not a modifier, changes nothing or fits no instrument is dropped on its own.
	 */
	private static @Nullable Map<String, Authored> compile(Definitions.Invocation invocation, Catalog catalog, List<String> problems) {
		String name = "invocation " + invocation.id() + ": ";
		CompositionParser.Parsed parsed = CompositionParser.parse(invocation.composition());
		Composition composition = parsed.composition();
		if (composition == null) {
			problems.add(name + "composition \"" + invocation.composition() + "\" does not parse: " + describe(parsed.problems()));
			return null;
		}
		Map<String, Composition> tunings = new LinkedHashMap<>();
		for (String id : invocation.tunings()) {
			Component modifier = catalog.component(id);
			Composition tuned = modifier == null || modifier.slot() != Slot.MODIFIER ? null : Compiler.tune(composition, modifier, catalog);
			if (tuned == null) {
				problems.add(name + "tuning " + id + " is not a modifier that changes it");
			} else {
				tunings.put(id, tuned);
			}
		}
		Map<String, Authored> forms = new TreeMap<>();
		List<Text> refused = List.of();
		for (Instrument instrument : catalog.instruments().values()) {
			Compiler.Compilation base = Compiler.compileAuthored(composition, catalog, instrument);
			if (base.plan() == null) {
				refused = base.problems();
				continue;
			}
			Map<String, Plan> tuned = new LinkedHashMap<>();
			for (Map.Entry<String, Composition> tuning : tunings.entrySet()) {
				Plan plan = Compiler.compileAuthored(tuning.getValue(), catalog, instrument).plan();
				if (plan != null && plan.signature().equals(base.plan().signature())) {
					tuned.put(tuning.getKey(), plan);
				}
			}
			forms.put(instrument.id(), new Authored(instrument.id(), base.plan(), tuned));
		}
		if (forms.isEmpty()) {
			problems.add(name + "fits no instrument: " + describe(refused));
			return null;
		}
		for (String tuning : tunings.keySet()) {
			if (forms.values().stream().noneMatch(form -> form.tunings().containsKey(tuning))) {
				problems.add(name + "tuning " + tuning + " fits no instrument");
			}
		}
		for (Authored form : forms.values()) {
			int least = form.plan().focus();
			if (!mayCost(invocation.focus(), invocation.masteredFocus(), least)) {
				problems.add(name + "costs less than its composition: at least " + least + " Focus, and "
						+ (least - (least + 3) / 4) + " once mastered, on " + form.instrument());
				return null;
			}
			List<Plan> plans = new ArrayList<>(form.tunings().values());
			plans.add(form.plan());
			for (Plan plan : plans) {
				if (plan.limits().work() > invocation.work() || plan.persists() > invocation.persists()) {
					problems.add(name + "\"" + plan.text() + "\" spends up to " + plan.limits().work() + " work and lasts up to "
							+ plan.persists() + " ticks on " + form.instrument() + "; it declares " + invocation.work() + " and "
							+ invocation.persists());
					return null;
				}
			}
		}
		return forms;
	}

	/**
	 * Whether an invocation may cost {@code focus} ({@code mastered} once its research is mastered) when its composition
	 * costs {@code composed}: never less than a player would pay to compose it, and mastery takes off at most a quarter
	 * (rounded up). So an invocation is never a cheaper way to the same effect than the grammar.
	 */
	public static boolean mayCost(int focus, int mastered, int composed) {
		return focus >= composed && mastered >= composed - (composed + 3) / 4;
	}

	private static String describe(List<Text> texts) {
		List<String> out = new ArrayList<>();
		for (Text text : texts) {
			out.add(text.key() + text.args());
		}
		return out.isEmpty() ? "no instrument is loaded" : String.join("; ", out);
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
