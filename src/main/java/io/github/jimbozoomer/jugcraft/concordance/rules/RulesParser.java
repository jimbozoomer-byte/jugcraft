package io.github.jimbozoomer.jugcraft.concordance.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
			default -> throw new Invalid("state " + state.id() + ": unknown evidence type \"" + type + "\"");
		}
	}

	public Definitions.@Nullable Invocation invocation(String id, JsonElement json) {
		try {
			JsonObject object = object(json, "invocation");
			only(object, "schema", "spell", "principle", "research", "stage", "focus", "mastered_focus");
			int focus = range(object, "focus", 1, 1000);
			int mastered = range(object, "mastered_focus", 1, focus);
			return new Definitions.Invocation(id, schema(object), id(object, "spell"), name(object, "principle"),
					id(object, "research"), state(object, "stage"), focus, mastered);
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
							id(object, "result"), range(object, "radiance", 0, 1_000_000), Map.of(), 0);
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
							Collections.unmodifiableMap(specimens), 0);
				}
				case CHANNEL -> {
					only(object, "schema", "type", "station", "research", "stage", "work", "focus", "radiance");
					int focus = range(object, "focus", 1, 1000);
					int radiance = range(object, "radiance", 1, 1_000_000);
					if (radiance >= focus) {
						// Focus returns with time; a channel that gave back as much as it took would be a free conversion.
						throw new Invalid("radiance (" + radiance + ") must be less than focus (" + focus + ")");
					}
					yield new Definitions.Working(id, schema, type, station, research, state, work, null, null, radiance,
							Map.of(), focus);
				}
			};
		} catch (Invalid | IllegalStateException | UnsupportedOperationException | NumberFormatException problem) {
			problems.add("working " + id + ": " + problem.getMessage());
			return null;
		}
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
