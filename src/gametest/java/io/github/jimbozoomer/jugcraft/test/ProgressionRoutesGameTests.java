package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.guide.encyclopedia.Encyclopedia;
import io.github.jimbozoomer.jugcraft.guide.encyclopedia.EncyclopediaPayload;
import io.github.jimbozoomer.jugcraft.guide.encyclopedia.EncyclopediaRequestPayload;
import io.github.jimbozoomer.jugcraft.guide.encyclopedia.PageWriter;
import io.github.jimbozoomer.jugcraft.guide.encyclopedia.ProgressionRoutes;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import io.github.jimbozoomer.jugcraft.machine.MachineRecipe;
import io.github.jimbozoomer.jugcraft.machine.MultiMachineRecipe;
import io.github.jimbozoomer.jugcraft.nouveau.JugNouveau;
import io.github.jimbozoomer.jugcraft.nouveau.NouveauRecipes;
import io.github.jimbozoomer.jugcraft.workshop.JugcraftWorkshop;
import io.github.jimbozoomer.jugcraft.workshop.WorkshopRecipe;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

/**
 * The Encyclopedia's route maps against a running server: every step's recipe is in the live recipe manager (or Jug
 * Nouveau's loaded workshop recipes) and makes the route's output, at the route's station, from ingredients that accept
 * the route's inputs in the route's counts; every item is registered; each route page has one step row per map step, in
 * order, with this server's grid for every made step; a server without the recipes shows each required step as
 * unavailable and draws no grid; and reading changes nothing.
 */
public final class ProgressionRoutesGameTests {
	private static List<ProgressionRoutes.Route> routes(GameTestHelper helper) {
		try {
			return ProgressionRoutes.load();
		} catch (Exception failure) {
			throw new AssertionError("The route maps cannot be read: " + failure, failure);
		}
	}

	private static Optional<RecipeHolder<?>> recipe(GameTestHelper helper, Identifier id) {
		return helper.getLevel().recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, id));
	}

	/** The station a live recipe is made at: a crafting table, a furnace, a machine's block or a starter workshop station. */
	private static Identifier station(Recipe<?> recipe) {
		if (recipe instanceof CraftingRecipe) {
			return Identifier.parse("minecraft:crafting_table");
		}
		if (recipe instanceof BlastingRecipe) {
			return Identifier.parse("minecraft:blast_furnace");
		}
		if (recipe instanceof SmeltingRecipe) {
			return Identifier.parse("minecraft:furnace");
		}
		if (recipe instanceof WorkshopRecipe workshop) {
			return Jugcraft.id(JugcraftWorkshop.STATIONS[workshop.station()]);
		}
		Identifier type = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
		for (MachineKind kind : MachineKind.values()) {
			if (type != null && type.getNamespace().equals(Jugcraft.MOD_ID) && type.getPath().equals(kind.recipeType())) {
				return Jugcraft.id(kind.id);
			}
		}
		return Identifier.parse("minecraft:air");
	}

	/** The live recipe's ingredients, each once per item one operation uses. */
	private static List<Ingredient> ingredients(Recipe<?> recipe) {
		List<Ingredient> out = new ArrayList<>();
		if (recipe instanceof MachineRecipe machine) {
			for (int i = 0; i < machine.inputCount(); i++) {
				out.add(machine.input());
			}
		} else if (recipe instanceof SingleItemRecipe single) {
			out.add(single.input());
		} else if (recipe instanceof MultiMachineRecipe multi) {
			for (MultiMachineRecipe.Part part : multi.parts()) {
				for (int i = 0; i < part.count(); i++) {
					out.add(part.ingredient());
				}
			}
		} else if (recipe instanceof WorkshopRecipe workshop) {
			for (MultiMachineRecipe.Part part : workshop.parts()) {
				for (int i = 0; i < part.count(); i++) {
					out.add(part.ingredient());
				}
			}
		} else {
			out.addAll(recipe.placementInfo().ingredients());
		}
		return out;
	}

	private static ItemStack made(GameTestHelper helper, Recipe<?> recipe) {
		if (recipe instanceof MachineRecipe machine) {
			return machine.output().create();
		}
		if (recipe instanceof AbstractCookingRecipe cooking) {
			return cooking.assemble(new SingleRecipeInput(ItemStack.EMPTY));
		}
		if (recipe instanceof MultiMachineRecipe multi) {
			return multi.output().create();
		}
		if (recipe instanceof WorkshopRecipe workshop) {
			return workshop.result().create();
		}
		// A crafting recipe's result, as its display shows it.
		return recipe.display().isEmpty() ? ItemStack.EMPTY : recipe.display().getFirst().result().resolveForFirstStack(
				SlotDisplayContext.fromLevel(helper.getLevel()));
	}

	@GameTest(maxTicks = 20)
	public void everyRouteItemAndMachineIsRegistered(GameTestHelper helper) {
		for (ProgressionRoutes.Route route : routes(helper)) {
			for (ProgressionRoutes.Step step : route.steps()) {
				List<Identifier> refs = new ArrayList<>(List.of(step.item()));
				for (Identifier ref : new Identifier[] {step.machine(), step.from(), step.tool()}) {
					if (ref != null) {
						refs.add(ref);
					}
				}
				step.inputs().forEach(part -> refs.add(part.item()));
				step.outputs().forEach(part -> refs.add(part.item()));
				for (Identifier ref : refs) {
					helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ref), route.id() + "/" + step.id() + ": " + ref + " is registered");
				}
			}
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 20)
	public void everyRouteRecipeResolvesInTheLiveRecipeManager(GameTestHelper helper) {
		int checked = 0;
		for (ProgressionRoutes.Route route : routes(helper)) {
			for (ProgressionRoutes.Step step : route.steps()) {
				if (!step.source().equals("recipe") || step.recipe() == null) {
					continue;
				}
				String name = route.id() + "/" + step.id() + " (" + step.recipe() + ")";
				Optional<RecipeHolder<?>> holder = recipe(helper, step.recipe());
				helper.assertTrue(holder.isPresent(), name + " is on this server");
				Recipe<?> live = holder.get().value();
				ItemStack result = made(helper, live);
				ProgressionRoutes.Part output = step.outputs().getFirst();
				helper.assertTrue(BuiltInRegistries.ITEM.getKey(result.getItem()).equals(output.item()) && result.getCount() == output.count(),
						name + " makes " + output.count() + " " + output.item() + ", not " + result);
				helper.assertValueEqual(station(live), step.machine(), name + "'s station");
				List<Ingredient> ingredients = ingredients(live);
				for (ProgressionRoutes.Part part : step.inputs()) {
					ItemStack stack = new ItemStack(ProgressionRoutes.item(part.item()));
					long accepting = ingredients.stream().filter(ingredient -> ingredient.test(stack)).count();
					helper.assertValueEqual((int) accepting, part.count(), name + ": slots that take " + part.item());
				}
				int total = step.inputs().stream().mapToInt(ProgressionRoutes.Part::count).sum();
				helper.assertValueEqual(ingredients.size(), total, name + ": every ingredient is one of the route's inputs");
				for (ProgressionRoutes.Alternative alternative : step.alternatives()) {
					Optional<RecipeHolder<?>> other = recipe(helper, alternative.recipe());
					helper.assertTrue(other.isPresent() && made(helper, other.get().value()).is(ProgressionRoutes.item(step.item()))
							&& station(other.get().value()).equals(alternative.machine()), name + ": other way " + alternative.recipe());
				}
				for (ProgressionRoutes.Part part : step.inputs()) {
					if (part.howRecipe() != null) {
						Optional<RecipeHolder<?>> how = recipe(helper, part.howRecipe());
						helper.assertTrue(how.isPresent() && made(helper, how.get().value()).is(ProgressionRoutes.item(part.item()))
								&& station(how.get().value()).equals(part.howMachine()), name + ": " + part.item() + " is made by " + part.howRecipe());
					}
				}
				checked++;
			}
		}
		helper.assertTrue(checked >= 30, "both routes' made steps were checked: " + checked);
		helper.succeed();
	}

	@GameTest(maxTicks = 20)
	public void jugNouveauStepsResolveAgainstItsLoadedRecipes(GameTestHelper helper) {
		if (!JugNouveau.enabled()) {
			helper.succeed();
			return;
		}
		for (ProgressionRoutes.Route route : routes(helper)) {
			for (ProgressionRoutes.Step step : route.steps()) {
				if (!step.source().equals("nouveau")) {
					continue;
				}
				NouveauRecipes.Working working = ProgressionRoutes.nouveau(step.recipe());
				helper.assertTrue(working != null && working.accepts("imbuement_chamber"), step.id() + ": " + step.recipe() + " is loaded for the chamber");
				ItemStack made = NouveauRecipes.preview(working, helper.getLevel().registryAccess());
				helper.assertTrue(made.is(ProgressionRoutes.item(step.item())) && made.getCount() == step.outputs().getFirst().count(),
						step.id() + " makes " + step.item() + ": " + made);
				for (ProgressionRoutes.Part part : step.inputs()) {
					helper.assertTrue(working.ingredients().stream().anyMatch(ingredient -> ProgressionRoutes.choices(ingredient)
							.contains(ProgressionRoutes.item(part.item()))), step.id() + " takes " + part.item());
				}
			}
		}
		helper.succeed();
	}

	/** One step row per map step, in order, and the server's own grid for each made step whose recipe it draws. */
	@GameTest(maxTicks = 20)
	public void eachRoutePageFollowsItsMapWithLiveGrids(GameTestHelper helper) {
		ServerPlayer player = TestPlayers.mock(helper);
		ItemStack carried = new ItemStack(Items.COPPER_INGOT, 5);
		player.getInventory().add(carried.copy());
		EncyclopediaPayload payload = Encyclopedia.build(player, new EncyclopediaRequestPayload(1, Optional.empty()), PageWriter::of);
		for (ProgressionRoutes.Route route : routes(helper)) {
			EncyclopediaPayload.Page page = payload.pages().stream().filter(p -> p.id().equals(route.page())).findFirst().orElse(null);
			helper.assertTrue(page != null, route.page() + " is sent to a newcomer");
			List<ProgressionRoutes.Step> steps = route.steps();
			List<List<EncyclopediaPayload.Row>> own = stepRows(page.rows());
			helper.assertValueEqual(own.size(), steps.size(), route.page() + ": one step row per map step");
			helper.assertTrue(page.rows().size() < EncyclopediaPayload.Page.MAX_ROWS, route.page() + " is not cut short");
			for (int i = 0; i < steps.size(); i++) {
				ProgressionRoutes.Step step = steps.get(i);
				List<EncyclopediaPayload.Row> rows = own.get(i);
				helper.assertTrue(rows.getFirst().stateOf() != EncyclopediaPayload.StepState.BLOCKED, step.id() + " is available here");
				helper.assertValueEqual(rows.getFirst().edgeOf(), ProgressionRoutes.weight(step.role()), step.id() + "'s weight");
				if (step.source().equals("recipe")) {
					helper.assertTrue(rows.stream().anyMatch(row -> row.grid().map(grid -> grid.recipe().equals(step.recipe())).orElse(false)),
							step.id() + " shows this server's grid for " + step.recipe());
				}
				for (ProgressionRoutes.Alternative alternative : step.alternatives()) {
					helper.assertTrue(rows.stream().anyMatch(row -> row.edgeOf() == EncyclopediaPayload.EdgeKind.ALTERNATIVE
							&& row.grid().map(grid -> grid.recipe().equals(alternative.recipe())).orElse(false)), step.id() + " shows " + alternative.recipe());
				}
			}
		}
		helper.assertValueEqual(player.getInventory().countItem(Items.COPPER_INGOT), 5, "reading the routes takes nothing");
		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		try {
			EncyclopediaPayload.CODEC.encode(buffer, payload);
			EncyclopediaPayload received = EncyclopediaPayload.CODEC.decode(buffer);
			helper.assertValueEqual(received.pages().size(), payload.pages().size(), "the route pages survive the trip to the client");
		} finally {
			buffer.release();
		}
		helper.succeed();
	}

	/** A server without the routes' recipes: every required made step is unavailable, said so, and no grid is drawn. */
	@GameTest(maxTicks = 20)
	public void aServerWithoutTheRecipesShowsTheStepsAsUnavailable(GameTestHelper helper) {
		ServerPlayer player = TestPlayers.mock(helper);
		for (ProgressionRoutes.Route route : routes(helper)) {
			PageWriter out = new PageWriter(player, id -> Optional.empty());
			ProgressionRoutes.write(route.page(), player, out);
			List<List<EncyclopediaPayload.Row>> own = stepRows(out.rows());
			List<ProgressionRoutes.Step> steps = route.steps();
			helper.assertValueEqual(own.size(), steps.size(), route.page() + ": still one step row per map step");
			helper.assertTrue(out.rows().stream().noneMatch(row -> row.grid().isPresent()), route.page() + ": no stale grid");
			for (int i = 0; i < steps.size(); i++) {
				ProgressionRoutes.Step step = steps.get(i);
				if (!step.source().equals("recipe")) {
					continue;
				}
				List<EncyclopediaPayload.Row> rows = own.get(i);
				helper.assertValueEqual(rows.getFirst().stateOf(), EncyclopediaPayload.StepState.BLOCKED, step.id() + " cannot be made here");
				if (step.role().equals("required")) {
					helper.assertTrue(rows.stream().anyMatch(row -> row.kindOf() == EncyclopediaPayload.Kind.EDGE
							&& row.stateOf() == EncyclopediaPayload.StepState.BLOCKED), step.id() + " says it is unavailable");
				}
			}
		}
		helper.succeed();
	}

	/** Each step row with the rows after it, up to the next step or heading. */
	private static List<List<EncyclopediaPayload.Row>> stepRows(List<EncyclopediaPayload.Row> rows) {
		List<List<EncyclopediaPayload.Row>> out = new ArrayList<>();
		List<EncyclopediaPayload.Row> current = null;
		for (EncyclopediaPayload.Row row : rows) {
			if (row.kindOf() == EncyclopediaPayload.Kind.STEP) {
				current = new ArrayList<>();
				out.add(current);
			} else if (row.kindOf() == EncyclopediaPayload.Kind.HEADING) {
				current = null;
			}
			if (current != null) {
				current.add(row);
			}
		}
		return out;
	}
}
