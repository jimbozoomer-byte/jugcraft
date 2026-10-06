package io.github.jimbozoomer.jugcraft.concordance.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.jimbozoomer.jugcraft.concordance.compose.Component;
import io.github.jimbozoomer.jugcraft.concordance.compose.Grammar;
import io.github.jimbozoomer.jugcraft.concordance.compose.Instrument;
import io.github.jimbozoomer.jugcraft.concordance.compose.Slot;
import io.github.jimbozoomer.jugcraft.concordance.effect.EffectKind;
import io.github.jimbozoomer.jugcraft.concordance.effect.EffectSpec;
import io.github.jimbozoomer.jugcraft.concordance.effect.Intent;
import io.github.jimbozoomer.jugcraft.concordance.effect.Stacking;
import io.github.jimbozoomer.jugcraft.concordance.resource.Conversion;
import io.github.jimbozoomer.jugcraft.concordance.resource.ResourceType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Reads Concordance definitions from JSON strictly: an unknown field, a wrong type, a misspelt state or an invalid id
 * rejects that one definition with a message naming the file and the field, and leaves the others loaded. Cross
 * references (prerequisites, unlocks) are checked afterwards by {@link ConcordanceRules}.
 */
public final class RulesParser {
	/** The data format version these rules understand. A newer file is refused rather than misread. */
	public static final int SCHEMA = 1;
	private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
	private static final Pattern NAME = Pattern.compile("[a-z0-9_]+");
	/** The most modifiers an invocation offers as tunings. */
	public static final int MAX_TUNINGS = 3;

	private final List<String> problems = new ArrayList<>();

	public List<String> problems() {
		return Collections.unmodifiableList(problems);
	}

	/** Thrown inside the parser to abandon one definition; the message is the diagnostic. */
	private static final class Invalid extends RuntimeException {
		Invalid(String message) {
			super(message, null, false, false);
		}
	}

	public Definitions.@Nullable Research research(String id, JsonElement json) {
		try {
			JsonObject object = object(json, "research");
			only(object, "schema", "principle", "tradition", "stage", "icon", "requires", "states", "unlocks");
			int schema = schema(object);
			List<Definitions.Requirement> requires = new ArrayList<>();
			if (object.has("requires")) {
				for (JsonElement element : array(object, "requires")) {
					JsonObject requirement = object(element, "requires[]");
					only(requirement, "research", "state");
					requires.add(new Definitions.Requirement(id(requirement, "research"), state(requirement, "state")));
				}
			}
			Map<ResearchState, List<EvidenceRule>> states = new EnumMap<>(ResearchState.class);
			JsonObject stateObject = object(member(object, "states"), "states");
			ResearchState expected = ResearchState.ENCOUNTERED;
			for (Map.Entry<String, JsonElement> entry : stateObject.entrySet()) {
				ResearchState state = ResearchState.fromId(entry.getKey());
				if (state == null) {
					throw new Invalid("unknown state \"" + entry.getKey() + "\"");
				}
				if (state != expected) {
					throw new Invalid("states must run in order from encountered; found " + state.id() + " where "
							+ expected.id() + " belongs");
				}
				expected = state.next() == null ? expected : state.next();
				JsonObject block = object(entry.getValue(), "states." + state.id());
				only(block, "any");
				List<EvidenceRule> rules = new ArrayList<>();
				for (JsonElement rule : array(block, "any")) {
					rules.add(rule(object(rule, "states." + state.id() + ".any[]"), state));
				}
				if (rules.isEmpty()) {
					throw new Invalid("state " + state.id() + " has no way to reach it");
				}
				states.put(state, List.copyOf(rules));
			}
			if (states.isEmpty()) {
				throw new Invalid("no states");
			}
			Map<ResearchState, Definitions.Unlocks> unlocks = new EnumMap<>(ResearchState.class);
			if (object.has("unlocks")) {
				for (Map.Entry<String, JsonElement> entry : object(member(object, "unlocks"), "unlocks").entrySet()) {
					ResearchState state = ResearchState.fromId(entry.getKey());
					if (state == null || !states.containsKey(state)) {
						throw new Invalid("unlocks names state \"" + entry.getKey() + "\", which this entry does not have");
					}
					JsonObject block = object(entry.getValue(), "unlocks." + entry.getKey());
					only(block, "invocations", "workings");
					unlocks.put(state, new Definitions.Unlocks(ids(block, "invocations"), ids(block, "workings")));
				}
			}
			return new Definitions.Research(id, schema, name(object, "principle"), name(object, "tradition"),
					name(object, "stage"), id(object, "icon"), List.copyOf(requires), Collections.unmodifiableMap(states),
					Collections.unmodifiableMap(unlocks));
		} catch (Invalid | IllegalStateException | UnsupportedOperationException | NumberFormatException problem) {
			problems.add("research " + id + ": " + problem.getMessage());
			return null;
		}
	}

	private EvidenceRule rule(JsonObject object, ResearchState state) {
		String type = string(object, "type");
		switch (type) {
			case "examine" -> {
				only(object, "type", "specimens", "max_light", "distinct");
				return new EvidenceRule(EvidenceRule.Kind.EXAMINE, specimens(object), optionalInt(object, "max_light", 0, 15),
						positive(object, "distinct", 1), null, null);
			}
			case "study" -> {
				only(object, "type", "specimens", "station", "distinct");
				return new EvidenceRule(EvidenceRule.Kind.STUDY, specimens(object), null, positive(object, "distinct", 1),
						id(object, "station"), null);
			}
			case "invoke" -> {
				only(object, "type", "invocation", "distinct_chunks");
				return new EvidenceRule(EvidenceRule.Kind.INVOKE, EvidenceRule.Specimens.NONE, null,
						positive(object, "distinct_chunks", 1), null, id(object, "invocation"));
			}
			case "notes" -> {
				only(object, "type", "distinct");
				if (state == ResearchState.MASTERED) {
					// Mastery is practical competency: it is earned by doing, never read from someone else's notes.
					throw new Invalid("state mastered: notes cannot stand for mastery");
				}
				return new EvidenceRule(EvidenceRule.Kind.NOTES, EvidenceRule.Specimens.NONE, null, positive(object, "distinct", 1),
						null, null);
			}
			default -> throw new Invalid("state " + state.id() + ": unknown evidence type \"" + type + "\"");
		}
	}

	public Definitions.@Nullable Invocation invocation(String id, JsonElement json) {
		try {
			JsonObject object = object(json, "invocation");
			only(object, "schema", "spell", "principle", "research", "stage", "focus", "mastered_focus", "role", "composition",
					"tunings", "work", "persists");
			int focus = range(object, "focus", 1, 1000);
			int mastered = range(object, "mastered_focus", 1, focus);
			String roleName = string(object, "role");
			Definitions.Role role = Definitions.Role.fromId(roleName);
			if (role == null) {
				throw new Invalid("unknown role \"" + roleName + "\" (damage, defense, movement, support, investigation, utility)");
			}
			String composition = string(object, "composition");
			if (composition.isBlank() || composition.length() > Grammar.MAX_TEXT) {
				throw new Invalid("\"composition\" must be 1 to " + Grammar.MAX_TEXT + " characters");
			}
			List<String> tunings = ids(object, "tunings");
			if (tunings.size() > MAX_TUNINGS) {
				throw new Invalid("at most " + MAX_TUNINGS + " tunings");
			}
			return new Definitions.Invocation(id, schema(object), id(object, "spell"), name(object, "principle"),
					id(object, "research"), state(object, "stage"), focus, mastered, role, composition, tunings,
					range(object, "work", 1, Grammar.MAX_WORK), range(object, "persists", 0, Grammar.MAX_DURATION));
		} catch (Invalid | IllegalStateException | UnsupportedOperationException | NumberFormatException problem) {
			problems.add("invocation " + id + ": " + problem.getMessage());
			return null;
		}
	}

	public Definitions.@Nullable Working working(String id, JsonElement json) {
		try {
			JsonObject object = object(json, "working");
			String typeName = string(object, "type");
			Definitions.WorkingType type = Definitions.WorkingType.fromId(typeName);
			if (type == null) {
				throw new Invalid("unknown type \"" + typeName + "\"");
			}
			int schema = schema(object);
			String station = id(object, "station");
			String research = id(object, "research");
			ResearchState state = state(object, "stage");
			String work = id(object, "work");
			return switch (type) {
				case CRAFT -> {
					only(object, "schema", "type", "station", "research", "stage", "work", "specimen", "result", "radiance");
					yield new Definitions.Working(id, schema, type, station, research, state, work, id(object, "specimen"),
							id(object, "result"), range(object, "radiance", 0, 1_000_000), Map.of(), 0, null);
				}
				case INFUSE -> {
					only(object, "schema", "type", "station", "research", "stage", "work", "specimens");
					Map<String, Integer> specimens = new LinkedHashMap<>();
					for (Map.Entry<String, JsonElement> entry : object(member(object, "specimens"), "specimens").entrySet()) {
						checkId(entry.getKey(), "specimens");
						specimens.put(entry.getKey(), intValue(entry.getValue(), "specimens." + entry.getKey(), 1, 1_000_000));
					}
					if (specimens.isEmpty()) {
						throw new Invalid("specimens is empty");
					}
					yield new Definitions.Working(id, schema, type, station, research, state, work, null, null, 0,
							Collections.unmodifiableMap(specimens), 0, null);
				}
				case CHANNEL -> {
					only(object, "schema", "type", "station", "research", "stage", "work", "conversion");
					// The amounts come from the conversion recipe (ConcordanceRules.build checks it exists and loses).
					yield new Definitions.Working(id, schema, type, station, research, state, work, null, null, 0, Map.of(), 0,
							id(object, "conversion"));
				}
			};
		} catch (Invalid | IllegalStateException | UnsupportedOperationException | NumberFormatException problem) {
			problems.add("working " + id + ": " + problem.getMessage());
			return null;
		}
	}

	/**
	 * A conversion recipe: {@code {"schema": 1, "from": {"resource": "focus", "amount": 6}, "to": {"resource":
	 * "essence/radiance", "amount": 2}}}. Resources are named by {@link ResourceType#id()}.
	 */
	public @Nullable Conversion conversion(String id, JsonElement json) {
		try {
			JsonObject object = object(json, "conversion");
			only(object, "schema", "from", "to");
			schema(object);
			JsonObject from = object(member(object, "from"), "from");
			JsonObject to = object(member(object, "to"), "to");
			only(from, "resource", "amount");
			only(to, "resource", "amount");
			return new Conversion(id, resource(from), range(from, "amount", 1, 1_000_000), resource(to),
					range(to, "amount", 1, 1_000_000));
		} catch (Invalid | IllegalStateException | UnsupportedOperationException | IllegalArgumentException problem) {
			problems.add("conversion " + id + ": " + problem.getMessage());
			return null;
		}
	}

	/**
	 * A composition component: {@code {"schema": 1, "slot": "operation", "requires": {"research": ..., "state": ...},
	 * "capacity": 2, "focus": 3, "operation": {...}}}, with one object named after its slot (see
	 * {@link Component} for each) and an optional {@code "authored": true} for a word only invocations use. Numbers
	 * outside the grammar's limits ({@link Grammar}) are refused.
	 */
	public @Nullable Component component(String id, JsonElement json) {
		try {
			JsonObject object = object(json, "component");
			String slotName = string(object, "slot");
			Slot slot = Slot.fromId(slotName);
			if (slot == null) {
				throw new Invalid("unknown slot \"" + slotName + "\" (delivery, selection, operation, modifier, termination)");
			}
			only(object, "schema", "slot", "requires", "capacity", "focus", "authored", slot.id());
			schema(object);
			boolean authored = bool(object, "authored", false);
			if (authored && slot == Slot.MODIFIER) {
				// Tunings are how players change invocations; a modifier only an author could use would change nothing.
				throw new Invalid("a modifier cannot be authored");
			}
			JsonObject requires = object(member(object, "requires"), "requires");
			only(requires, "research", "state");
			Definitions.Requirement requirement = new Definitions.Requirement(id(requires, "research"), state(requires, "state"));
			int capacity = range(object, "capacity", 0, Grammar.MAX_COMPONENT_COST);
			int focus = range(object, "focus", 0, Grammar.MAX_COMPONENT_COST);
			JsonObject body = object(member(object, slot.id()), slot.id());
			Component.Part part = switch (slot) {
				case DELIVERY -> delivery(body);
				case SELECTION -> selection(body);
				case OPERATION -> operation(body);
				case MODIFIER -> {
					only(body, "aspect", "amount");
					String aspectName = string(body, "aspect");
					Component.Aspect aspect = Component.Aspect.fromId(aspectName);
					if (aspect == null) {
						throw new Invalid("modifier: unknown aspect \"" + aspectName + "\" (magnitude, duration, radius, range)");
					}
					yield new Component.Modifier(aspect, range(body, "amount", 1, Grammar.MAX_MODIFIER_AMOUNT));
				}
				case TERMINATION -> {
					only(body, "pulses", "interval");
					int pulses = range(body, "pulses", 1, Grammar.MAX_PULSES);
					int interval = pulses == 1 ? range(body, "interval", 0, 0) : range(body, "interval", Grammar.MIN_INTERVAL, Grammar.MAX_INTERVAL);
					yield new Component.Termination(pulses, interval);
				}
			};
			return new Component(id, slot, requirement, capacity, focus, authored, part);
		} catch (Invalid | IllegalStateException | UnsupportedOperationException | IllegalArgumentException problem) {
			problems.add("component " + id + ": " + problem.getMessage());
			return null;
		}
	}

	private static Component.Delivery delivery(JsonObject body) {
		only(body, "form", "range");
		String formName = string(body, "form");
		Component.Form form = Component.Form.fromId(formName);
		if (form == null) {
			throw new Invalid("delivery: unknown form \"" + formName + "\" (here, touch, ray)");
		}
		return new Component.Delivery(form, form == Component.Form.HERE ? range(body, "range", 0, 0) : range(body, "range", 1, Grammar.MAX_RANGE));
	}

	private static Component.Selection selection(JsonObject body) {
		only(body, "pick", "radius", "targets");
		String pickName = string(body, "pick");
		Component.Pick pick = Component.Pick.fromId(pickName);
		if (pick == null) {
			throw new Invalid("selection: unknown pick \"" + pickName + "\" (struck, creatures, blocks, allies)");
		}
		if (pick == Component.Pick.STRUCK) {
			return new Component.Selection(pick, range(body, "radius", 0, 0), range(body, "targets", 1, 1));
		}
		return new Component.Selection(pick, range(body, "radius", 1, Grammar.MAX_RADIUS), range(body, "targets", 1, Grammar.MAX_TARGETS));
	}

	private static Component.Operation operation(JsonObject body) {
		only(body, "effect", "intent", "principle", "magnitude", "duration", "status", "stacking", "school", "scaling");
		String kindName = string(body, "effect");
		EffectKind kind = EffectKind.fromId(kindName);
		if (kind == null) {
			throw new Invalid("operation: unknown effect \"" + kindName + "\"");
		}
		String intentName = string(body, "intent");
		Intent intent = Intent.fromId(intentName);
		if (intent == null) {
			throw new Invalid("operation: intent must be helpful or harmful, not \"" + intentName + "\"");
		}
		String principle = name(body, "principle");
		if (!ResourceType.PRINCIPLES.contains(principle)) {
			throw new Invalid("operation: \"" + principle + "\" is not a Principle");
		}
		int strongest = switch (kind) {
			case STATUS -> EffectSpec.MAX_AMPLIFIER;
			case MOVEMENT -> EffectSpec.MAX_PUSH;
			case DAMAGE, RESTORATION, PROTECTION -> EffectSpec.MAX_MAGNITUDE;
			default -> 0;
		};
		int weakest = kind == EffectKind.DAMAGE || kind == EffectKind.RESTORATION || kind == EffectKind.PROTECTION
				|| kind == EffectKind.MOVEMENT ? 1 : 0;
		int magnitude = body.has("magnitude") || weakest > 0 ? range(body, "magnitude", weakest, strongest) : 0;
		boolean lasting = kind == EffectKind.STATUS || kind == EffectKind.PROTECTION || kind == EffectKind.DETECTION
				|| kind == EffectKind.ILLUMINATION;
		int duration = lasting ? range(body, "duration", 1, EffectSpec.MAX_DURATION) : body.has("duration") ? range(body, "duration", 0, 0) : 0;
		String status = kind == EffectKind.STATUS ? id(body, "status") : null;
		if (kind != EffectKind.STATUS && body.has("status")) {
			throw new Invalid("operation: only a status effect names a status");
		}
		Stacking stacking = Stacking.STRONGEST;
		if (body.has("stacking")) {
			String stackingName = string(body, "stacking");
			stacking = Stacking.fromId(stackingName);
			if (stacking == null) {
				throw new Invalid("operation: unknown stacking \"" + stackingName + "\" (strongest, accumulate, exclusive)");
			}
		}
		String school = body.has("school") ? id(body, "school") : null;
		double scaling = 0.0;
		if (body.has("scaling")) {
			// Spell Power is damage power in a school: it scales only damage that names its school.
			if (kind != EffectKind.DAMAGE || school == null) {
				throw new Invalid("operation: only damage that names a school scales with Spell Power");
			}
			scaling = decimal(body, "scaling", 0.0, MAX_SCALING);
		}
		return new Component.Operation(new EffectSpec(kind, intent, magnitude, duration, status, stacking, school), principle, scaling);
	}

	/**
	 * What an instrument can hold: {@code {"schema": 1, "item": ..., "capacity": 8, "targets": 6, "work": 48,
	 * "branches": 1, "duration": 1200}}, each at most the grammar's limit.
	 */
	public @Nullable Instrument instrument(String id, JsonElement json) {
		try {
			JsonObject object = object(json, "instrument");
			only(object, "schema", "item", "capacity", "targets", "work", "branches", "duration");
			schema(object);
			return new Instrument(id, id(object, "item"), range(object, "capacity", 1, Grammar.MAX_CAPACITY),
					range(object, "targets", 1, Grammar.MAX_TARGETS), range(object, "work", 1, Grammar.MAX_WORK),
					range(object, "branches", 0, Grammar.MAX_BRANCHES), range(object, "duration", 0, Grammar.MAX_DURATION));
		} catch (Invalid | IllegalStateException | UnsupportedOperationException | IllegalArgumentException problem) {
			problems.add("instrument " + id + ": " + problem.getMessage());
			return null;
		}
	}

	private static ResourceType resource(JsonObject object) {
		String value = string(object, "resource");
		ResourceType type = ResourceType.parse(value);
		if (type == null) {
			throw new Invalid("\"" + value + "\" is not a resource (focus, ley_charge, essence/<principle>, vitae, "
					+ "astral_resonance, prima_materia)");
		}
		return type;
	}

	// ------------------------------------------------------------------------------------------------- field helpers

	private static int schema(JsonObject object) {
		int schema = range(object, "schema", 1, Integer.MAX_VALUE);
		if (schema > SCHEMA) {
			throw new Invalid("schema " + schema + " is newer than this version of Jugcraft understands (" + SCHEMA + ")");
		}
		return schema;
	}

	private static JsonElement member(JsonObject object, String field) {
		JsonElement element = object.get(field);
		if (element == null || element.isJsonNull()) {
			throw new Invalid("missing \"" + field + "\"");
		}
		return element;
	}

	private static JsonObject object(JsonElement element, String where) {
		if (element == null || !element.isJsonObject()) {
			throw new Invalid(where + " must be an object");
		}
		return element.getAsJsonObject();
	}

	private static JsonArray array(JsonObject object, String field) {
		JsonElement element = member(object, field);
		if (!element.isJsonArray()) {
			throw new Invalid("\"" + field + "\" must be a list");
		}
		return element.getAsJsonArray();
	}

	private static void only(JsonObject object, String... fields) {
		List<String> allowed = List.of(fields);
		for (String key : object.keySet()) {
			// Fabric's own keys, such as fabric:load_conditions, are for the loader, not the rules.
			if (!allowed.contains(key) && !key.startsWith("fabric:")) {
				throw new Invalid("unknown field \"" + key + "\"");
			}
		}
	}

	private static String string(JsonObject object, String field) {
		JsonElement element = member(object, field);
		if (!(element instanceof JsonPrimitive primitive) || !primitive.isString()) {
			throw new Invalid("\"" + field + "\" must be a string");
		}
		return primitive.getAsString();
	}

	private static String id(JsonObject object, String field) {
		String value = string(object, field);
		checkId(value, field);
		return value;
	}

	private static void checkId(String value, String field) {
		if (!ID.matcher(value).matches()) {
			throw new Invalid("\"" + field + "\" is not a valid id: \"" + value + "\"");
		}
	}

	private static String name(JsonObject object, String field) {
		String value = string(object, field);
		if (!NAME.matcher(value).matches()) {
			throw new Invalid("\"" + field + "\" is not a valid name: \"" + value + "\"");
		}
		return value;
	}

	private static List<String> ids(JsonObject object, String field) {
		if (!object.has(field)) {
			return List.of();
		}
		List<String> out = new ArrayList<>();
		for (JsonElement element : array(object, field)) {
			if (!(element instanceof JsonPrimitive primitive) || !primitive.isString()) {
				throw new Invalid("\"" + field + "\" must list ids");
			}
			checkId(primitive.getAsString(), field);
			out.add(primitive.getAsString());
		}
		return List.copyOf(out);
	}

	private static ResearchState state(JsonObject object, String field) {
		String value = string(object, field);
		ResearchState state = ResearchState.fromId(value);
		if (state == null) {
			throw new Invalid("\"" + field + "\" is not a research state: \"" + value + "\"");
		}
		return state;
	}

	private static EvidenceRule.Specimens specimens(JsonObject object) {
		JsonElement element = member(object, "specimens");
		List<String> values = new ArrayList<>();
		if (element.isJsonArray()) {
			for (JsonElement each : element.getAsJsonArray()) {
				if (!(each instanceof JsonPrimitive primitive) || !primitive.isString()) {
					throw new Invalid("\"specimens\" must list item ids or tags");
				}
				values.add(primitive.getAsString());
			}
		} else if (element instanceof JsonPrimitive primitive && primitive.isString()) {
			values.add(primitive.getAsString());
		} else {
			throw new Invalid("\"specimens\" must be an item id, a #tag or a list of them");
		}
		List<String> items = new ArrayList<>();
		List<String> tags = new ArrayList<>();
		for (String value : values) {
			boolean tag = value.startsWith("#");
			String bare = tag ? value.substring(1) : value;
			checkId(bare, "specimens");
			(tag ? tags : items).add(bare);
		}
		if (items.isEmpty() && tags.isEmpty()) {
			throw new Invalid("\"specimens\" is empty");
		}
		return new EvidenceRule.Specimens(List.copyOf(items), List.copyOf(tags));
	}

	private static int intValue(JsonElement element, String where, int min, int max) {
		if (!(element instanceof JsonPrimitive primitive) || !primitive.isNumber()) {
			throw new Invalid("\"" + where + "\" must be a whole number");
		}
		double value = primitive.getAsDouble();
		if (value != Math.rint(value) || value < min || value > max) {
			throw new Invalid("\"" + where + "\" must be a whole number from " + min + " to " + max);
		}
		return (int) value;
	}

	/** The most damage an operation may add per point of Spell Power. */
	public static final double MAX_SCALING = 2.0;

	private static boolean bool(JsonObject object, String field, boolean fallback) {
		if (!object.has(field)) {
			return fallback;
		}
		JsonElement element = object.get(field);
		if (!(element instanceof JsonPrimitive primitive) || !primitive.isBoolean()) {
			throw new Invalid("\"" + field + "\" must be true or false");
		}
		return primitive.getAsBoolean();
	}

	private static double decimal(JsonObject object, String field, double min, double max) {
		JsonElement element = member(object, field);
		if (!(element instanceof JsonPrimitive primitive) || !primitive.isNumber()) {
			throw new Invalid("\"" + field + "\" must be a number");
		}
		double value = primitive.getAsDouble();
		if (!(value >= min && value <= max)) {
			throw new Invalid("\"" + field + "\" must be from " + min + " to " + max);
		}
		return value;
	}

	private static int range(JsonObject object, String field, int min, int max) {
		return intValue(member(object, field), field, min, max);
	}

	private static int positive(JsonObject object, String field, int fallback) {
		return object.has(field) ? range(object, field, 1, 1024) : fallback;
	}

	private static @Nullable Integer optionalInt(JsonObject object, String field, int min, int max) {
		return object.has(field) ? range(object, field, min, max) : null;
	}
}
