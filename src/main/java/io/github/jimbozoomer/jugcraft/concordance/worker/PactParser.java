package io.github.jimbozoomer.jugcraft.concordance.worker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.jimbozoomer.jugcraft.concordance.rules.Json;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Concordance Chunk D, D-04 (D-S23): reads a pact's offer strictly ({@link Json}). An unknown field, a wrong type or a
 * value out of bounds refuses the one file with a reason ({@link #problems()}); the rest load.
 * <pre>
 * {"schema": 1, "agreement": "jugcraft:stolas_pact", "research": "jugcraft:stolas_regard",
 *  "service": {"work": "star_reckoning", "radius": 8, "from": 0, "to": 24000, "quota": 1},
 *  "upkeep": {"offering": "jugcraft:stolas_offerings", "count": 1, "period_days": 7, "grace_days": 1, "cap": 4},
 *  "beneficiaries": "personal", "compact": {"items": [{"item": "minecraft:amethyst_shard", "count": 2}], "resonance": 8},
 *  "beats": ["greeting", "service", "price"]}
 * </pre>
 * Keep WORK and the numbers equal to tools/concordance_pacts.py (WORK, PARSER; check_pacts).
 */
public final class PactParser {
	public static final List<String> WORK = List.of("star_reckoning");
	public static final int MAX_COMPACT_ITEMS = 4;
	public static final int MAX_COMPACT_COUNT = 64;
	/** An astrolabe's capacity (sky/AstrolabeItem.CAPACITY; check_pacts compares). */
	public static final int MAX_RESONANCE = 32;
	public static final int MAX_BEATS = 3;
	/** The pacts the game offers in play: the pact perch offers Stolas. */
	public static final List<String> REQUIRED = List.of(PactDefinition.STOLAS);

	private final List<String> problems = new ArrayList<>();

	public List<String> problems() {
		return Collections.unmodifiableList(problems);
	}

	public @Nullable PactDefinition pact(String id, JsonElement json) {
		try {
			JsonObject object = Json.object(json, "pact");
			Json.only(object, "schema", "agreement", "research", "service", "upkeep", "beneficiaries", "compact", "beats");
			Json.schema(object);
			JsonObject service = Json.object(Json.member(object, "service"), "service");
			Json.only(service, "work", "radius", "from", "to", "quota");
			JsonObject upkeep = Json.object(Json.member(object, "upkeep"), "upkeep");
			Json.only(upkeep, "offering", "count", "period_days", "grace_days", "cap");
			JsonObject compact = Json.object(Json.member(object, "compact"), "compact");
			Json.only(compact, "items", "resonance");
			List<PactDefinition.Cost> costs = new ArrayList<>();
			Set<String> named = new HashSet<>();
			for (JsonElement element : Json.array(compact, "items")) {
				JsonObject cost = Json.object(element, "compact item");
				Json.only(cost, "item", "count");
				String item = Json.id(cost, "item");
				if (!named.add(item)) {
					throw new Json.Invalid("\"items\" names " + item + " twice");
				}
				costs.add(new PactDefinition.Cost(item, Json.range(cost, "count", 1, MAX_COMPACT_COUNT)));
			}
			if (costs.isEmpty() || costs.size() > MAX_COMPACT_ITEMS) {
				throw new Json.Invalid("\"items\" lists 1 to " + MAX_COMPACT_ITEMS + " materials");
			}
			List<String> beats = new ArrayList<>();
			for (JsonElement element : Json.array(object, "beats")) {
				if (!(element instanceof JsonPrimitive primitive) || !primitive.isString() || !Json.NAME.matcher(primitive.getAsString()).matches()) {
					throw new Json.Invalid("\"beats\" must list names");
				}
				beats.add(primitive.getAsString());
			}
			if (beats.isEmpty() || beats.size() > MAX_BEATS) {
				throw new Json.Invalid("\"beats\" lists 1 to " + MAX_BEATS + " lines");
			}
			return new PactDefinition(id, Json.id(object, "agreement"), Json.id(object, "research"),
					Json.choice(service, "work", WORK.toArray(String[]::new)), Json.range(service, "radius", 1, WorkerParser.MAX_RADIUS),
					Json.range(service, "from", 0, 23999), Json.range(service, "to", 0, 24000), Json.range(service, "quota", 1, WorkerParser.MAX_QUOTA),
					Json.id(upkeep, "offering"), Json.range(upkeep, "count", 1, Pacts.MAX_COUNT),
					Json.range(upkeep, "period_days", 1, (int) (Pacts.MAX_PERIOD / Pacts.DAY)) * Pacts.DAY,
					Json.range(upkeep, "grace_days", 0, (int) (Pacts.MAX_GRACE / Pacts.DAY)) * Pacts.DAY,
					Json.range(upkeep, "cap", 1, Pacts.MAX_CAP), Json.choice(object, "beneficiaries", Pacts.PERSONAL, Pacts.PARTY),
					costs, Json.range(compact, "resonance", 0, MAX_RESONANCE), beats);
		} catch (Json.Invalid | IllegalStateException | UnsupportedOperationException | IllegalArgumentException problem) {
			problems.add("pact " + id + ": " + problem.getMessage());
			return null;
		}
	}
}
