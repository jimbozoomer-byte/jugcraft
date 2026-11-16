package io.github.jimbozoomer.jugcraft.guide.encyclopedia;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.nouveau.JugNouveau;
import io.github.jimbozoomer.jugcraft.nouveau.NouveauRecipes;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * The Encyclopedia's route maps: a Technology and a Magic route from a starting material to the next tier, each a list of
 * groups of steps (an item, a machine or a milestone). The route's selection and order are hand-written in
 * tools/encyclopedia_progression.py; each step's machine, inputs and outputs are derived from the recipe data and written
 * to {@value #RESOURCE}, which both sides read: the client draws the map from it, and the server writes the
 * {@value #TECHNOLOGY} and {@value #MAGIC} pages here, reading every recipe again from this server (the live recipe
 * manager through {@link PageWriter}, or Jug Nouveau's live recipes), so a recipe a data pack removed is said to be
 * unavailable and never shown as a stale grid.
 * <p>
 * Each page has exactly one step row per step, in the file's order (the client's map finds a step's live rows by that
 * order). Nothing here gates, rewards, grants or spends: a page only reads the player's level and this server's recipes
 * and sales.
 */
public final class ProgressionRoutes {
	public static final String TECHNOLOGY = "route_technology";
	public static final String MAGIC = "route_magic";
	public static final String RESOURCE = "/jugcraft/progression_routes.json";

	/** An input or output: the item a route names, how many one operation uses or makes, and where the input comes from. */
	public record Part(Identifier item, int count, String fromStep, String howMethod, @Nullable Identifier howMachine,
			@Nullable Identifier howRecipe) {
		/** Made by an earlier step of the same route. */
		public boolean fromRoute() {
			return !fromStep.isEmpty();
		}
	}

	/** A step's existing Encyclopedia entry: a handbook page (chapter, page), a codex category or an Encyclopedia page. */
	public record Entry(String type, int chapter, int page, String target) {
	}

	/** Another recipe for the same item. */
	public record Alternative(Identifier recipe, String method, @Nullable Identifier machine) {
	}

	/**
	 * One node: its id, kind (step or milestone), role (required, recommended or alternative), item (a milestone's icon),
	 * title (milestones only), method, machine, source (recipe, nouveau, gathering or none), recipe, the block a gathered
	 * material comes from, a workshop tool, inputs, outputs, other recipes, the earlier steps it needs and the earlier
	 * steps it may use instead, its advice and its existing entry.
	 */
	public record Step(String id, String kind, String role, Identifier item, String title, String method, @Nullable Identifier machine,
			String source, @Nullable Identifier recipe, @Nullable Identifier from, @Nullable Identifier tool, List<Part> inputs,
			List<Part> outputs, List<Alternative> alternatives, List<String> needs, List<String> altNeeds, String advice, Entry entry) {
		public boolean milestone() {
			return kind.equals("milestone");
		}
	}

	public record Group(String id, String title, String text, boolean milestone, List<Step> steps) {
	}

	public record Route(String id, String page, String title, Identifier icon, int accent, List<String> summary, List<Group> groups) {
		/** Every step, in order. */
		public List<Step> steps() {
			List<Step> out = new ArrayList<>();
			groups.forEach(group -> out.addAll(group.steps()));
			return out;
		}

		public @Nullable Step step(String id) {
			for (Step step : steps()) {
				if (step.id().equals(id)) {
					return step;
				}
			}
			return null;
		}
	}

	private static volatile @Nullable List<Route> routes;

	private ProgressionRoutes() {
	}

	/** Both routes (technology, then magic); empty, with a warning in the log, if the file cannot be read. */
	public static List<Route> routes() {
		List<Route> loaded = routes;
		if (loaded == null) {
			try {
				loaded = load();
			} catch (RuntimeException | IOException failure) {
				Jugcraft.LOGGER.warn("Encyclopedia: the route maps could not be read", failure);
				loaded = List.of();
			}
			routes = loaded;
		}
		return loaded;
	}

	public static @Nullable Route byId(String id) {
		return routes().stream().filter(route -> route.id().equals(id)).findFirst().orElse(null);
	}

	public static @Nullable Route byPage(String page) {
		return routes().stream().filter(route -> route.page().equals(page)).findFirst().orElse(null);
	}

	/** Reads {@value #RESOURCE} (tests call this to see a failure rather than an empty list). */
	public static List<Route> load() throws IOException {
		try (InputStream in = ProgressionRoutes.class.getResourceAsStream(RESOURCE)) {
			if (in == null) {
				throw new IOException("Missing " + RESOURCE);
			}
			JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			List<Route> out = new ArrayList<>();
			for (JsonElement element : root.getAsJsonArray("routes")) {
				out.add(route(element.getAsJsonObject()));
			}
			return List.copyOf(out);
		}
	}

	private static Route route(JsonObject data) {
		List<String> summary = new ArrayList<>();
		data.getAsJsonArray("summary").forEach(line -> summary.add(line.getAsString()));
		List<Group> groups = new ArrayList<>();
		for (JsonElement element : data.getAsJsonArray("groups")) {
			JsonObject group = element.getAsJsonObject();
			List<Step> steps = new ArrayList<>();
			group.getAsJsonArray("steps").forEach(step -> steps.add(step(step.getAsJsonObject())));
			groups.add(new Group(text(group, "id"), text(group, "title"), text(group, "text"), group.get("milestone").getAsBoolean(),
					List.copyOf(steps)));
		}
		return new Route(text(data, "id"), text(data, "page"), text(data, "title"), Identifier.parse(text(data, "icon")),
				0xFF000000 | Integer.parseInt(text(data, "accent"), 16), List.copyOf(summary), List.copyOf(groups));
	}

	private static Step step(JsonObject data) {
		List<Alternative> alternatives = new ArrayList<>();
		for (JsonElement element : array(data, "alternatives")) {
			JsonObject alternative = element.getAsJsonObject();
			alternatives.add(new Alternative(Identifier.parse(text(alternative, "recipe")), text(alternative, "method"), id(alternative, "machine")));
		}
		JsonObject link = data.getAsJsonObject("link");
		Entry entry = new Entry(text(link, "type"), link.has("chapter") ? link.get("chapter").getAsInt() : 0,
				link.has("page") ? link.get("page").getAsInt() : 0,
				link.has("target") ? text(link, "target") : link.has("category") ? text(link, "category") : "");
		return new Step(text(data, "id"), text(data, "kind"), text(data, "role"), Identifier.parse(text(data, "item")),
				data.has("title") ? text(data, "title") : "", text(data, "method"), id(data, "machine"), text(data, "source"),
				id(data, "recipe"), id(data, "from"), id(data, "tool"), parts(array(data, "inputs")), parts(array(data, "outputs")),
				List.copyOf(alternatives), strings(array(data, "needs")), strings(array(data, "alt_needs")), text(data, "note"), entry);
	}

	private static List<Part> parts(JsonArray array) {
		List<Part> out = new ArrayList<>();
		for (JsonElement element : array) {
			JsonObject part = element.getAsJsonObject();
			JsonObject how = part.has("how") ? part.getAsJsonObject("how") : new JsonObject();
			out.add(new Part(Identifier.parse(text(part, "item")), part.get("count").getAsInt(),
					part.has("from_step") ? text(part, "from_step") : "", how.has("method") ? text(how, "method") : "",
					id(how, "machine"), id(how, "recipe")));
		}
		return List.copyOf(out);
	}

	private static List<String> strings(JsonArray array) {
		List<String> out = new ArrayList<>();
		array.forEach(element -> out.add(element.getAsString()));
		return List.copyOf(out);
	}

	private static String text(JsonObject data, String field) {
		JsonElement value = data.get(field);
		return value == null || value.isJsonNull() ? "" : value.getAsString();
	}

	private static @Nullable Identifier id(JsonObject data, String field) {
		String value = text(data, field);
		return value.isEmpty() ? null : Identifier.parse(value);
	}

	private static JsonArray array(JsonObject data, String field) {
		return data.has(field) && data.get(field).isJsonArray() ? data.getAsJsonArray(field) : new JsonArray();
	}

	// -------------------------------------------------------------------------------------------------------- the pages

	/** Writes the route whose page is {@code page}: its summary, then each group's heading and text, then its steps. */
	public static void write(String page, ServerPlayer player, PageWriter out) {
		Route route = byPage(page);
		if (route == null) {
			return;
		}
		for (String line : route.summary()) {
			out.fact(Component.literal(line));
		}
		for (Group group : route.groups()) {
			out.heading(Component.literal(group.title()));
			if (!group.text().isBlank()) {
				out.fact(Component.literal(group.text()));
			}
			for (Step step : group.steps()) {
				step(step, out);
			}
		}
	}

	/** One step: its row (where it stands on this server and how much it matters), then its live ways and its inputs. */
	private static void step(Step step, PageWriter out) {
		Item item = item(step.item());
		EncyclopediaPayload.EdgeKind weight = weight(step.role());
		if (step.milestone()) {
			out.step(EncyclopediaPayload.StepState.NONE, weight,
					Component.translatable("encyclopedia.jugcraft.progression.milestone", Component.literal(step.title())), null, List.of(item));
			advice(step, out);
			return;
		}
		NouveauRecipes.@Nullable Working working = step.source().equals("nouveau") ? nouveau(step.recipe()) : null;
		boolean available = switch (step.source()) {
			case "recipe" -> step.recipe() != null && out.present(step.recipe());
			case "nouveau" -> working != null;
			default -> true;
		};
		List<Item> icons = new ArrayList<>(List.of(item));
		Identifier station = step.machine() != null ? step.machine() : step.from();
		if (station != null) {
			icons.add(item(station));
		}
		out.step(available ? EncyclopediaPayload.StepState.NONE : EncyclopediaPayload.StepState.BLOCKED, weight,
				Component.translatable("encyclopedia.jugcraft.progression.step", name(item), method(step)), null, icons);
		if (step.recipe() != null && step.source().equals("recipe")) {
			out.recipe(weight, step.recipe(), serverRecipe(step.recipe()));
		} else if (step.recipe() != null && step.source().equals("nouveau")) {
			if (working == null) {
				if (weight == EncyclopediaPayload.EdgeKind.REQUIRED) {
					out.absent(weight, serverRecipe(step.recipe()));
				}
			} else {
				nouveauWay(step, working, weight, out);
			}
		}
		if (!step.inputs().isEmpty()) {
			out.fact(Component.translatable("encyclopedia.jugcraft.progression.needs", counted(step.inputs())), null, items(step.inputs()));
			List<Part> elsewhere = step.inputs().stream().filter(part -> part.howMachine() != null).toList();
			if (!elsewhere.isEmpty()) {
				MutableComponent made = Component.empty();
				for (int i = 0; i < elsewhere.size(); i++) {
					Part part = elsewhere.get(i);
					made.append(i == 0 ? Component.empty() : Component.literal(", ")).append(Component.translatable(
							"encyclopedia.jugcraft.progression.made_by", name(item(part.item())), name(item(part.howMachine()))));
				}
				out.fact(Component.translatable("encyclopedia.jugcraft.progression.made_elsewhere", made), null, items(elsewhere));
			}
		}
		if (step.tool() != null) {
			Item tool = item(step.tool());
			out.fact(Component.translatable("encyclopedia.jugcraft.progression.tool", name(tool)), null, List.of(tool));
		}
		for (Alternative alternative : step.alternatives()) {
			out.recipe(EncyclopediaPayload.EdgeKind.ALTERNATIVE, alternative.recipe(), serverRecipe(alternative.recipe()));
		}
		// Town sales are live: only a town that stands, and only today's offers.
		out.sales(item);
		for (Part part : step.inputs()) {
			out.sales(item(part.item()));
		}
		advice(step, out);
	}

	/** A Jug Nouveau recipe as one way, read from this server's loaded workshop recipes: the station, what goes in, what comes out. */
	private static void nouveauWay(Step step, NouveauRecipes.Working working, EncyclopediaPayload.EdgeKind weight, PageWriter out) {
		List<Item> icons = new ArrayList<>();
		MutableComponent inputs = Component.empty();
		for (JsonElement ingredient : working.ingredients()) {
			List<Item> choices = choices(ingredient);
			if (choices.isEmpty()) {
				continue;
			}
			inputs.append(icons.isEmpty() ? Component.empty() : Component.literal(", ")).append(name(choices.getFirst()));
			icons.add(choices.getFirst());
		}
		ItemStack made = NouveauRecipes.preview(working, out.player().registryAccess());
		if (!made.isEmpty()) {
			icons.add(made.getItem());
		}
		Item station = step.machine() == null ? Items.AIR : item(step.machine());
		out.edge(weight, Component.translatable("encyclopedia.jugcraft.progression.nouveau", name(station), inputs,
				Component.translatable("encyclopedia.jugcraft.progression.count", made.getCount(), made.getHoverName())), null, icons);
	}

	/** The loaded Jug Nouveau recipe {@code id}, if Jug Nouveau is on and this server has it. */
	public static NouveauRecipes.@Nullable Working nouveau(@Nullable Identifier id) {
		if (id == null || !JugNouveau.enabled()) {
			return null;
		}
		String wanted = id.toString();
		for (NouveauRecipes.Working working : NouveauRecipes.all()) {
			if (working.id().equals(wanted)) {
				return working;
			}
		}
		return null;
	}

	/** The items a Jug Nouveau ingredient names (an item, a tag, or a list of either), at most a grid's alternatives. */
	public static List<Item> choices(JsonElement ingredient) {
		if (ingredient == null || ingredient.isJsonNull()) {
			return List.of();
		}
		if (ingredient.isJsonArray()) {
			List<Item> out = new ArrayList<>();
			for (JsonElement option : ingredient.getAsJsonArray()) {
				for (Item item : choices(option)) {
					if (out.size() < EncyclopediaPayload.Grid.MAX_ALTERNATIVES && !out.contains(item)) {
						out.add(item);
					}
				}
			}
			return out;
		}
		String ref;
		if (ingredient.isJsonObject()) {
			JsonObject object = ingredient.getAsJsonObject();
			ref = object.has("item") ? object.get("item").getAsString() : object.has("tag") ? "#" + object.get("tag").getAsString() : "";
		} else {
			ref = ingredient.getAsString();
		}
		if (ref.startsWith("#")) {
			Identifier tag = Identifier.tryParse(ref.substring(1));
			return tag == null ? List.of() : PageWriter.items(TagKey.create(Registries.ITEM, tag), EncyclopediaPayload.Grid.MAX_ALTERNATIVES);
		}
		Identifier id = Identifier.tryParse(ref);
		Item item = id == null ? Items.AIR : item(id);
		return item == Items.AIR ? List.of() : List.of(item);
	}

	private static void advice(Step step, PageWriter out) {
		if (!step.advice().isBlank()) {
			out.fact(Component.literal(step.advice()));
		}
	}

	/** How a step is made, in words: "craft at a Crafting Table", "gather from Tin Ore". */
	private static Component method(Step step) {
		Identifier station = step.machine() != null ? step.machine() : step.from();
		return Component.translatable("encyclopedia.jugcraft.progression.method." + step.method(),
				station == null ? Component.empty() : name(item(station)));
	}

	private static Component serverRecipe(Identifier recipe) {
		return Component.translatable("encyclopedia.jugcraft.server_recipe", recipe.getPath().replace('_', ' '));
	}

	/** "3 × Copper Ingot, 1 × Tin Ingot". */
	private static Component counted(List<Part> parts) {
		MutableComponent out = Component.empty();
		for (int i = 0; i < parts.size(); i++) {
			Part part = parts.get(i);
			out.append(i == 0 ? Component.empty() : Component.literal(", "))
					.append(Component.translatable("encyclopedia.jugcraft.progression.count", part.count(), name(item(part.item()))));
		}
		return out;
	}

	private static List<Item> items(List<Part> parts) {
		return parts.stream().map(part -> item(part.item())).toList();
	}

	/** A route role's weight: required, recommended, or another way. */
	public static EncyclopediaPayload.EdgeKind weight(String role) {
		return switch (role) {
			case "recommended" -> EncyclopediaPayload.EdgeKind.RECOMMENDED;
			case "alternative" -> EncyclopediaPayload.EdgeKind.ALTERNATIVE;
			default -> EncyclopediaPayload.EdgeKind.REQUIRED;
		};
	}

	public static Item item(Identifier id) {
		return BuiltInRegistries.ITEM.getValue(id);
	}

	private static Component name(Item item) {
		return item.getDefaultInstance().getHoverName();
	}
}
