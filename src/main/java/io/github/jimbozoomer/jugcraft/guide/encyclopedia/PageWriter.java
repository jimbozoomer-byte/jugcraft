package io.github.jimbozoomer.jugcraft.guide.encyclopedia;

import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.journal.Journal;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.machine.MachineRecipe;
import io.github.jimbozoomer.jugcraft.machine.MultiMachineRecipe;
import io.github.jimbozoomer.jugcraft.town.JugcraftTown;
import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.town.TownDecor;
import io.github.jimbozoomer.jugcraft.town.TownShops;
import io.github.jimbozoomer.jugcraft.workshop.WorkshopRecipe;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;
import org.jspecify.annotations.Nullable;

/**
 * Writes one encyclopedia page's rows (Concordance Chunk A, A-04), and is the one place a page may show a recipe, a sale
 * or a codex link, so each is honest in one place:
 * <ul>
 * <li>a recipe is shown only if this server has it, as its crafting grid read from the server's own recipe (a data pack
 * that changes one changes the page); one a route needs but the server lacks is said to be unavailable, never shown. A
 * furnace, machine or starter-workshop recipe is drawn the same way, its inputs one slot per item used;</li>
 * <li>a sale is shown only for a town that stands in this world, for an offer on sale in today's theme;</li>
 * <li>a codex entry is linked only once the player's research has opened it (the entry's own unlock, which the audit
 * pins to the codex data).</li>
 * </ul>
 * Rows past {@value EncyclopediaPayload.Page#MAX_ROWS} are dropped (the last says there is more), grids past
 * {@value #MAX_GRIDS} keep their text only, and icons past {@value EncyclopediaPayload.Row#MAX_ICONS} are dropped. It only
 * reads.
 */
public final class PageWriter {
	/** The most crafting grids a page shows (the route maps draw one for each made step and its other ways). */
	public static final int MAX_GRIDS = 32;

	/** Finds a recipe on this server (game tests pass one that pretends some are missing). */
	@FunctionalInterface
	public interface Recipes {
		Optional<RecipeHolder<?>> find(Identifier id);
	}

	private final ServerPlayer player;
	private final Recipes recipes;
	private final List<EncyclopediaPayload.Row> rows = new ArrayList<>();
	private int grids;
	private boolean full;

	public PageWriter(ServerPlayer player, Recipes recipes) {
		this.player = player;
		this.recipes = recipes;
	}

	/** This server's own recipes. */
	public static Recipes recipes(ServerLevel level) {
		return id -> level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, id));
	}

	/** The writer the encyclopedia uses: this server's recipes. */
	public static PageWriter of(ServerPlayer player) {
		return new PageWriter(player, recipes(player.level()));
	}

	public ServerPlayer player() {
		return player;
	}

	public void heading(Component text) {
		add(EncyclopediaPayload.Kind.HEADING, EncyclopediaPayload.StepState.NONE, EncyclopediaPayload.EdgeKind.NONE, text, null, List.of(), null, null);
	}

	public void fact(Component text) {
		fact(text, null, List.of());
	}

	public void fact(Component text, @Nullable Component exact, List<Item> icons) {
		add(EncyclopediaPayload.Kind.FACT, EncyclopediaPayload.StepState.NONE, EncyclopediaPayload.EdgeKind.NONE, text, exact, icons, null, null);
	}

	/** A step of a route, where it stands for this player and how much it matters. */
	public void step(EncyclopediaPayload.StepState state, EncyclopediaPayload.EdgeKind importance, Component text, @Nullable Component exact,
			List<Item> icons) {
		add(EncyclopediaPayload.Kind.STEP, state, importance, text, exact, icons, null, null);
	}

	/** One way to the step above it. */
	public void edge(EncyclopediaPayload.EdgeKind kind, Component text, @Nullable Component exact, List<Item> icons) {
		add(EncyclopediaPayload.Kind.EDGE, EncyclopediaPayload.StepState.NONE, kind, text, exact, icons, null, null);
	}

	/** Whether this server has the recipe {@code id}. */
	public boolean present(Identifier id) {
		return recipes.find(id).isPresent();
	}

	/**
	 * A way to the step above that is the recipe {@code id}, shown with its crafting grid if this server has it. If it
	 * does not, a required way is said to be unavailable here (and nothing is advertised); any other is left out. Returns
	 * whether the recipe was shown.
	 */
	public boolean recipe(EncyclopediaPayload.EdgeKind kind, Identifier id, Component text) {
		Optional<RecipeHolder<?>> found = recipes.find(id);
		if (found.isEmpty()) {
			if (kind == EncyclopediaPayload.EdgeKind.REQUIRED) {
				absent(kind, text);
			}
			return false;
		}
		EncyclopediaPayload.Grid grid = grids < MAX_GRIDS ? grid(id, found.get().value()) : null;
		if (grid != null) {
			grids++;
		}
		add(EncyclopediaPayload.Kind.EDGE, EncyclopediaPayload.StepState.NONE, kind, text, null,
				grid == null ? List.of() : List.of(grid.result()), grid, null);
		return true;
	}

	/** A needed way that this server has no recipe for: said to be unavailable here, and nothing is advertised. */
	public void absent(EncyclopediaPayload.EdgeKind kind, Component text) {
		add(EncyclopediaPayload.Kind.EDGE, EncyclopediaPayload.StepState.BLOCKED, kind,
				Component.translatable("encyclopedia.jugcraft.absent", text), null, List.of(), null, null);
	}

	/** A recipe's grid, read from the server's recipe as the recipe book shows it; null for a kind it does not draw. */
	private EncyclopediaPayload.@Nullable Grid grid(Identifier id, Recipe<?> recipe) {
		List<RecipeDisplay> displays = recipe.display();
		if (displays.isEmpty()) {
			return process(id, recipe);
		}
		RecipeDisplay display = displays.getFirst();
		ContextMap context = SlotDisplayContext.fromLevel(player.level());
		int station;
		int width;
		List<SlotDisplay> slots;
		switch (display) {
			case ShapedCraftingRecipeDisplay shaped -> {
				station = EncyclopediaPayload.Grid.SHAPED;
				width = shaped.width();
				slots = shaped.ingredients();
			}
			case ShapelessCraftingRecipeDisplay shapeless -> {
				station = EncyclopediaPayload.Grid.SHAPELESS;
				width = 3;
				slots = shapeless.ingredients();
			}
			case StonecutterRecipeDisplay cut -> {
				station = EncyclopediaPayload.Grid.STONECUTTER;
				width = 1;
				slots = List.of(cut.input());
			}
			default -> {
				return process(id, recipe);
			}
		}
		List<List<Item>> items = new ArrayList<>();
		for (SlotDisplay slot : slots) {
			if (items.size() >= EncyclopediaPayload.Grid.MAX_SLOTS) {
				break;
			}
			Set<Item> choices = new LinkedHashSet<>();
			for (ItemStack stack : slot.resolveForStacks(context)) {
				if (!stack.isEmpty() && choices.size() < EncyclopediaPayload.Grid.MAX_ALTERNATIVES) {
					choices.add(stack.getItem());
				}
			}
			items.add(List.copyOf(choices));
		}
		ItemStack result = display.result().resolveForFirstStack(context);
		if (result.isEmpty()) {
			return null;
		}
		return new EncyclopediaPayload.Grid(id, station, width, items, result.getItem(), result.getCount());
	}

	/**
	 * A furnace, Jugcraft machine or starter-workshop recipe as a grid: each input once per item one operation uses (at
	 * most {@value EncyclopediaPayload.Grid#MAX_SLOTS} slots), and what one operation makes; null for any other kind.
	 */
	private static EncyclopediaPayload.@Nullable Grid process(Identifier id, Recipe<?> recipe) {
		List<List<Item>> slots = new ArrayList<>();
		ItemStack result;
		int station;
		if (recipe instanceof MachineRecipe machine) {
			station = EncyclopediaPayload.Grid.MACHINE;
			slots(machine.input(), machine.inputCount(), slots);
			result = machine.output().create();
		} else if (recipe instanceof AbstractCookingRecipe cooking) {
			station = EncyclopediaPayload.Grid.SMELTING;
			slots(cooking.input(), 1, slots);
			result = cooking.assemble(new SingleRecipeInput(ItemStack.EMPTY));
		} else if (recipe instanceof MultiMachineRecipe multi) {
			station = EncyclopediaPayload.Grid.MACHINE;
			for (MultiMachineRecipe.Part part : multi.parts()) {
				slots(part.ingredient(), part.count(), slots);
			}
			result = multi.output().create();
		} else if (recipe instanceof WorkshopRecipe workshop) {
			station = EncyclopediaPayload.Grid.WORKSHOP;
			for (MultiMachineRecipe.Part part : workshop.parts()) {
				slots(part.ingredient(), part.count(), slots);
			}
			result = workshop.result().create();
		} else {
			return null;
		}
		if (slots.isEmpty() || result.isEmpty()) {
			return null;
		}
		return new EncyclopediaPayload.Grid(id, station, Math.min(3, slots.size()), slots, result.getItem(), result.getCount());
	}

	/** {@code count} slots of {@code ingredient}'s items (at most a grid's alternatives each), while the grid has room. */
	private static void slots(Ingredient ingredient, int count, List<List<Item>> out) {
		List<Item> choices = ingredient.items().limit(EncyclopediaPayload.Grid.MAX_ALTERNATIVES).map(Holder::value).toList();
		for (int i = 0; i < count && out.size() < EncyclopediaPayload.Grid.MAX_SLOTS; i++) {
			out.add(choices);
		}
	}

	/**
	 * A way by trade: each offer of {@code item} on sale today in a town shop, when the town feature is on and the town
	 * stands in this world. Returns whether any was written.
	 */
	public boolean sales(Item item) {
		if (!JugcraftConfig.isFeatureEnabled(JugcraftTown.FEATURE) || Town.origin(player.level().getServer().overworld()) == null) {
			return false;
		}
		boolean any = false;
		for (TownShops.Sale sale : TownShops.get().sales(item, TownDecor.theme())) {
			edge(EncyclopediaPayload.EdgeKind.TRADE, Component.translatable("encyclopedia.jugcraft.trade.town", sale.offer().count(),
					item.getDefaultInstance().getHoverName(), sale.shop().name(), sale.offer().price()), null, List.of(item));
			any = true;
		}
		return any;
	}

	/**
	 * A link to the codex entry {@code entry} (its id within the Arcane Concordance codex, such as
	 * {@code composition/composing}), written only when {@code research} is null or the player's research has reached
	 * {@code state} (the entry's own unlock, pinned to the codex data by the audit). Returns whether it was written.
	 */
	public boolean codex(String entry, @Nullable String research, @Nullable ResearchState state) {
		if (research != null && state != null && ConcordanceProgress.knowledge(player).state(research).ordinal() < state.ordinal()) {
			return false;
		}
		link(Component.translatable("encyclopedia.jugcraft.link.codex",
				Component.translatable("book.jugcraft.arcane_concordance." + entry.replace('/', '.') + ".name")), EncyclopediaPayload.Link.codex(entry));
		return true;
	}

	/** A link to the Spell Composer, the journal or another page. */
	public void link(Component text, EncyclopediaPayload.Link link) {
		add(EncyclopediaPayload.Kind.LINK, EncyclopediaPayload.StepState.NONE, EncyclopediaPayload.EdgeKind.NONE, text, null, List.of(), null, link);
	}

	/** A journal-style report, each of its lines a fact (the journal's research and stage sections write here). */
	public Journal.Report report() {
		return (text, exact) -> fact(text, exact, List.of());
	}

	/** Whether anything but headings was written (a page with nothing else is left out). */
	public boolean hasContent() {
		return rows.stream().anyMatch(row -> row.kindOf() != EncyclopediaPayload.Kind.HEADING);
	}

	public List<EncyclopediaPayload.Row> rows() {
		return List.copyOf(rows);
	}

	/** Up to {@code max} items of an item tag, in the registry's order (for a step's icons). */
	public static List<Item> items(TagKey<Item> tag, int max) {
		List<Item> out = new ArrayList<>();
		for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
			if (out.size() >= max) {
				break;
			}
			out.add(holder.value());
		}
		return out;
	}

	private void add(EncyclopediaPayload.Kind kind, EncyclopediaPayload.StepState state, EncyclopediaPayload.EdgeKind edge, Component text,
			@Nullable Component exact, List<Item> icons, EncyclopediaPayload.@Nullable Grid grid, EncyclopediaPayload.@Nullable Link link) {
		if (full) {
			return;
		}
		if (rows.size() == EncyclopediaPayload.Page.MAX_ROWS - 1) {
			// The last row says there was more than a page holds.
			full = true;
			rows.add(new EncyclopediaPayload.Row(EncyclopediaPayload.Kind.FACT.ordinal(), 0, 0, Component.translatable("encyclopedia.jugcraft.more"),
					Optional.empty(), List.of(), Optional.empty(), Optional.empty()));
			return;
		}
		List<Item> shown = icons.size() > EncyclopediaPayload.Row.MAX_ICONS ? icons.subList(0, EncyclopediaPayload.Row.MAX_ICONS) : icons;
		rows.add(new EncyclopediaPayload.Row(kind.ordinal(), state.ordinal(), edge.ordinal(), text, Optional.ofNullable(exact), shown,
				Optional.ofNullable(grid), Optional.ofNullable(link)));
	}
}
