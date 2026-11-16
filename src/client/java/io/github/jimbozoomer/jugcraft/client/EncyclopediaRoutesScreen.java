package io.github.jimbozoomer.jugcraft.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.klikli_dev.modonomicon.client.gui.BookGuiManager;
import com.klikli_dev.modonomicon.client.gui.book.BookAddress;
import com.klikli_dev.modonomicon.data.BookDataManager;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.guide.encyclopedia.EncyclopediaPayload;
import io.github.jimbozoomer.jugcraft.guide.encyclopedia.ProgressionRoutes;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Illustrated chapter directory over the existing generated handbook/codex routes; no duplicate progression data. Each
 * route has three tabs: its chapters, its milestones and its Map ({@link EncyclopediaRouteMap}): the route's items,
 * machines and milestones from {@link ProgressionRoutes}, with lines to what each needs. A map step shows its machine,
 * inputs, outputs and how each input is had, and this server's live recipe from the route page; it opens its existing
 * entry (a handbook page, a codex category or an Encyclopedia page) or that live page, each with a way back here.
 */
public class EncyclopediaRoutesScreen extends Screen {
    private static final Identifier CONTENT = Jugcraft.id("encyclopedia/en_us.json");
    private static final Identifier CODEX = Jugcraft.id("arcane_concordance");
    private record Link(String type, int chapter, int page, @Nullable Identifier category, String target) {
        Component describe() { return Component.translatable("screen.jugcraft.routes.opens." + type); }
    }
    private record Entry(String title, String text, String icon, Link link) { }
    private record Route(String id, String title, String icon, int accent, List<String> summary, List<Entry> milestones, List<Entry> chapters) { }
    private final @Nullable Screen parent;
    private final List<String> intro = new ArrayList<>();
    private final List<Route> routes = new ArrayList<>();
    private int route = -1, cursor = -1, left, top, panelWidth, panelHeight, pageWidth;
    private boolean codexLoaded, milestones, map;
    private @Nullable ComposerScroll directory;
    private @Nullable ComposerScroll details;
    private List<Entry> entries = List.of();
    private List<Button> entryButtons = new ArrayList<>();
    /** The Map tab's route, its steps in order and their tiles. */
    private ProgressionRoutes.@Nullable Route mapped;
    private List<ProgressionRoutes.Step> steps = List.of();
    private final List<EncyclopediaRouteMap.Node> nodes = new ArrayList<>();

    public EncyclopediaRoutesScreen(@Nullable Screen parent) {
        super(Component.translatable("screen.jugcraft.routes.title")); this.parent = parent; load();
    }
	private void load() {
		Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(CONTENT);
		if (resource.isEmpty()) {
			return;
		}
		try (Reader reader = resource.get().openAsReader()) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			for (JsonElement paragraph : root.getAsJsonArray("intro")) {
				intro.add(paragraph.getAsString());
			}
			for (JsonElement element : root.getAsJsonArray("routes")) {
				routes.add(parseRoute(element.getAsJsonObject()));
			}
		} catch (Exception e) {
			Jugcraft.LOGGER.warn("Could not read the Jugcraft Encyclopedia", e);
			intro.clear();
			routes.clear();
		}
	}

	private static Route parseRoute(JsonObject data) {
		List<String> summary = new ArrayList<>();
		for (JsonElement paragraph : data.getAsJsonArray("summary")) {
			summary.add(paragraph.getAsString());
		}
		int accent = 0xFF000000 | Integer.parseInt(data.get("accent").getAsString(), 16);
		return new Route(data.has("id") ? data.get("id").getAsString() : "", data.get("title").getAsString(), data.get("icon").getAsString(), accent, summary,
				parseEntries(data.getAsJsonArray("milestones")), parseEntries(data.getAsJsonArray("chapters")));
	}

	private static List<Entry> parseEntries(JsonArray array) {
		List<Entry> entries = new ArrayList<>();
		for (JsonElement element : array) {
			JsonObject data = element.getAsJsonObject();
			JsonObject link = data.getAsJsonObject("link");
			String type = link.get("type").getAsString();
			Link target = new Link(type, link.has("chapter") ? link.get("chapter").getAsInt() : 0,
					link.has("page") ? link.get("page").getAsInt() : 0,
					link.has("category") ? Identifier.parse(link.get("category").getAsString()) : null,
                        link.has("target") ? link.get("target").getAsString() : "");
			entries.add(new Entry(data.get("title").getAsString(), data.has("text") ? data.get("text").getAsString() : "",
					data.get("icon").getAsString(), target));
		}
		return entries;
	}

    private static Component ui(String key, Object... args) { return ComposerScreen.ui(key, args); }
    public int route() { return route; }
    public void showRoute(int index) { route = index; cursor = 0; milestones = true; map = false; directory = null; rebuildWidgets(); }
    /** Shows the route's Map tab (tests and the keyboard use it too). */
    public void showMap(int index) { route = index; cursor = -1; map = true; directory = null; rebuildWidgets(); }
    public boolean mapShown() { return route >= 0 && map; }
    /** The Map tab's tiles, in the route's order (tests read their bounds). */
    public List<? extends Button> mapNodes() { return List.copyOf(nodes); }
    /** The selected step's id on the Map tab, or "". */
    public String selectedStep() { return map && cursor >= 0 && cursor < steps.size() ? steps.get(cursor).id() : ""; }
    /** Selects the Map tab's {@code index}-th step. */
    public void selectStep(int index) { cursor = index; rebuildWidgets(); }
    private void showHome() { route = -1; directory = null; rebuildWidgets(); }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
    @Override protected void init() {
        panelWidth = Math.min(700, width - 20); panelHeight = Math.min(410, height - 18);
        left = (width - panelWidth) / 2; top = (height - panelHeight) / 2; pageWidth = panelWidth / 2 - 34;
        codexLoaded = BookDataManager.get().getBook(CODEX) != null;
        tab(left + 18, top + panelHeight - 29, Math.min(100, pageWidth), 18, ui("contents"), "", false, this::onClose);
        if (route < 0 || route >= routes.size()) {
            route = -1;
            for (int i = 0; i < routes.size(); i++) {
                int at = i;
                Route r = routes.get(i);
                tab(left + (i % 2) * panelWidth / 2 + 20, top + 58, pageWidth, 56,
                    Component.literal(r.title()), r.icon(), false, () -> showRoute(at));
            }
            return;
        }
        Route shown = routes.get(route);
        // Three tabs sharing the page's width, each as wide as its label where they fit.
        Component[] labels = {ui("book.chapters"), ui("book.progression"), ui("book.route_map")};
        int[] widths = new int[labels.length];
        int wanted = 0;
        for (int i = 0; i < labels.length; i++) { widths[i] = font.width(labels[i]) + 14; wanted += widths[i]; }
        int room = pageWidth - 2 * (labels.length - 1);
        int tx = left + 18;
        for (int i = 0; i < labels.length; i++) {
            int w = wanted <= room ? widths[i] + (room - wanted) / labels.length : Math.max(16, widths[i] * room / wanted);
            int mode = i;
            boolean selected = i == 2 ? map : !map && milestones == (i == 1);
            Component hint = i == 2 ? ui("book.route_map.hint") : Component.empty();
            addRenderableWidget(new BookArt.Tab(tx, top + 42, w, 20, labels[i], hint, "", selected, false, unused -> {
                map = mode == 2; milestones = mode == 1; cursor = -1; directory = null; rebuildWidgets();
            }));
            tx += w + 2;
        }
        if (map) { initMap(shown); return; }
        mapped = null; steps = List.of(); nodes.clear();
        double scroll = directory == null ? 0 : directory.scrollAmount();
        directory = addRenderableWidget(new ComposerScroll(minecraft, left + 18, top + 68, pageWidth + 4, panelHeight - 109, unused -> {}));
        entries = milestones ? shown.milestones() : shown.chapters();
        entryButtons.clear();
        int cy = 0;
        for (int i = 0; i < entries.size(); i++) {
            int at = i; Entry entry = entries.get(i);
            BookArt.Tab button = new BookArt.Tab(left + 18, 0, pageWidth - 6, 27, Component.literal(entry.title()), Component.literal(entry.text()),
                entry.icon(), cursor == i, false, unused -> { cursor = at; rebuildWidgets(); });
            directory.add(button, cy); entryButtons.add(button); cy += 29;
        }
        directory.arrange(scroll);
        int rx = left + panelWidth / 2 + 15;
        details = addRenderableWidget(new ComposerScroll(minecraft, rx, top + 82, pageWidth + 3, panelHeight - 140, unused -> {}));
        int dy = 0;
        if (cursor < 0 || cursor >= entries.size()) {
            dy = paragraph(dy, ui("book.choose_topic"));
            for (String text : shown.summary()) dy = paragraph(dy + 5, Component.literal(text));
        } else {
            Entry entry = entries.get(cursor);
            dy = paragraph(dy, Component.literal(entry.title()).withColor(BookArt.INK & 0xFFFFFF));
            if (!entry.text().isBlank()) dy = paragraph(dy + 7, Component.literal(entry.text()));
            dy = paragraph(dy + 10, entry.link().describe());
            Button read = tab(rx, top + panelHeight - 51, pageWidth, 20, ui("book.read"), "", true, () -> follow(entry.link()));
            read.active = available(entry.link());
            if (!read.active) paragraph(dy + 6, Component.translatable("screen.jugcraft.routes.unavailable"));
        }
        details.arrange(0);
    }
    /** The Map tab: the route's tiles and lines on the left page; the selected step's details and links on the right. */
    private void initMap(Route shown) {
        mapped = ProgressionRoutes.byId(shown.id());
        steps = mapped == null ? List.of() : mapped.steps();
        entries = List.of(); entryButtons.clear(); nodes.clear();
        if (cursor >= steps.size()) cursor = -1;
        double scroll = directory == null ? 0 : directory.scrollAmount();
        int mapX = left + 22, mapWidth = pageWidth - 14;
        directory = addRenderableWidget(new ComposerScroll(minecraft, left + 18, top + 68, pageWidth + 4, panelHeight - 109, unused -> {}));
        int rx = left + panelWidth / 2 + 15;
        details = addRenderableWidget(new ComposerScroll(minecraft, rx, top + 82, pageWidth + 3, panelHeight - 140, unused -> {}));
        if (mapped == null) {
            paragraph(0, Component.translatable("screen.jugcraft.routes.missing"));
            directory.arrange(0); details.arrange(0);
            return;
        }
        EncyclopediaRouteMap.Layout layout = EncyclopediaRouteMap.layout(mapped, mapWidth);
        for (int i = 0; i < steps.size(); i++) {
            int at = i;
            ProgressionRoutes.Step step = steps.get(i);
            EncyclopediaRouteMap.Place place = layout.places().get(i);
            nodes.add(new EncyclopediaRouteMap.Node(mapX + place.dx(), step, cursor == i, mapped.accent(), role(step),
                unused -> { cursor = at; rebuildWidgets(); }));
        }
        // The headings and lines first, so the tiles are drawn over them.
        directory.add(new EncyclopediaRouteMap.Canvas(mapX, mapWidth, font, mapped, layout, List.copyOf(nodes), cursor), 0);
        for (int i = 0; i < nodes.size(); i++) directory.add(nodes.get(i), layout.places().get(i).dy());
        directory.arrange(scroll);
        int dy = 0;
        if (cursor < 0) {
            dy = paragraph(dy, ui("book.map.choose"));
            dy = paragraph(dy + 5, ui("book.map.legend"));
            for (String text : mapped.summary()) dy = paragraph(dy + 5, Component.literal(text));
            details.arrange(0);
            return;
        }
        ProgressionRoutes.Step step = steps.get(cursor);
        dy = paragraph(dy, EncyclopediaRouteMap.name(step).copy().withColor(BookArt.INK & 0xFFFFFF));
        dy = paragraph(dy + 2, role(step));
        if (!step.milestone()) {
            Station station = station(step);
            if (station != null) dy = paragraph(dy + 4, ui("book.map.method." + step.method()).copy().append(" ").append(station.name()));
        }
        if (!step.inputs().isEmpty()) {
            dy = paragraph(dy + 4, ui("book.map.needs"));
            dy = stacks(dy, step.inputs());
            for (ProgressionRoutes.Part part : step.inputs()) dy = paragraph(dy, source(part));
        }
        if (!step.outputs().isEmpty()) {
            dy = paragraph(dy + 4, ui("book.map.makes"));
            dy = stacks(dy, step.outputs());
        }
        List<String> after = new ArrayList<>(step.needs());
        if (!after.isEmpty()) {
            net.minecraft.network.chat.MutableComponent names = Component.empty();
            for (int i = 0; i < after.size(); i++) {
                ProgressionRoutes.Step need = mapped.step(after.get(i));
                if (need != null) names.append(i == 0 ? Component.empty() : Component.literal(", ")).append(EncyclopediaRouteMap.name(need));
            }
            dy = paragraph(dy + 4, ui("book.map.after", names));
        }
        if (!step.alternatives().isEmpty()) {
            net.minecraft.network.chat.MutableComponent ways = Component.empty();
            for (int i = 0; i < step.alternatives().size(); i++) {
                ProgressionRoutes.Alternative way = step.alternatives().get(i);
                Component name = way.machine() == null ? Component.literal(way.recipe().getPath()) : itemName(way.machine());
                ways.append(i == 0 ? Component.empty() : Component.literal(", ")).append(name);
            }
            dy = paragraph(dy + 4, ui("book.map.alternatives", ways));
        }
        if (!step.advice().isBlank()) dy = paragraph(dy + 4, Component.literal(step.advice()));
        dy = live(dy + 6, cursor);
        details.arrange(0);
        int half = (pageWidth - 3) / 2;
        Link link = link(step.entry());
        Button open = tab(rx, top + panelHeight - 51, half, 20, ui("book.open_entry"), "", true, () -> follow(link));
        open.active = available(link);
        int at = cursor;
        Button recipe = tab(rx + half + 3, top + panelHeight - 51, pageWidth - half - 3, 20, ui("book.recipe_page"), "", false,
            () -> openLiveRecipe(at));
        recipe.active = !step.milestone() && pageRows(mapped.page()) != null;
    }

    /** Where a step is made or found, with its name: a machine, a station, or the block it is gathered from. */
    private record Station(net.minecraft.resources.Identifier id, Component name) { }

    private static @Nullable Station station(ProgressionRoutes.Step step) {
        var id = step.machine() != null ? step.machine() : step.from();
        return id == null ? null : new Station(id, itemName(id));
    }

    private static Component itemName(net.minecraft.resources.Identifier id) {
        return ProgressionRoutes.item(id).getDefaultInstance().getHoverName();
    }

    private static Component role(ProgressionRoutes.Step step) {
        return ui("book.map.role." + (step.milestone() ? "milestone" : step.role()));
    }

    /** Where an input comes from: an earlier step, a vanilla item, or its own recipe's machine. */
    private Component source(ProgressionRoutes.Part part) {
        Component name = itemName(part.item());
        if (part.fromRoute()) return ui("book.map.from_step", part.count(), name);
        if (part.howMachine() == null) return ui("book.map.vanilla", part.count(), name);
        return ui("book.map.made", part.count(), name, itemName(part.howMachine()));
    }

    /** A row of item slots, each with its count. */
    private int stacks(int y, List<ProgressionRoutes.Part> parts) {
        List<ItemStack> shown = new ArrayList<>();
        for (ProgressionRoutes.Part part : parts) shown.add(new ItemStack(ProgressionRoutes.item(part.item()), Math.max(1, part.count())));
        var widget = new EncyclopediaWidgets.Items(left + panelWidth / 2 + 18, font, pageWidth - 12, shown);
        details.add(widget, y);
        return y + widget.getHeight() + 3;
    }

    /** The server's live rows for the {@code step}-th step of the map's page: its first grid, or that it is not available. */
    private int live(int y, int step) {
        List<EncyclopediaPayload.Row> rows = mapped == null ? null : pageRows(mapped.page());
        if (rows == null) return paragraph(y, ui("book.map.waiting"));
        List<EncyclopediaPayload.Row> own = stepRows(rows, step);
        if (own.isEmpty()) return y;
        if (own.getFirst().stateOf() == EncyclopediaPayload.StepState.BLOCKED) return paragraph(y, ui("book.map.not_here"));
        for (EncyclopediaPayload.Row row : own) {
            if (row.grid().isPresent()) {
                y = paragraph(y, ui("book.map.live"));
                var grid = new EncyclopediaWidgets.Grid(left + panelWidth / 2 + 18, font, row.grid().get());
                details.add(grid, y);
                return y + grid.getHeight() + 3;
            }
            if (row.kindOf() == EncyclopediaPayload.Kind.EDGE && !row.icons().isEmpty()) {
                y = paragraph(y, ui("book.map.live"));
                y = paragraph(y, row.text());
                var icons = new EncyclopediaWidgets.Items(left + panelWidth / 2 + 18, font, row.icons(), pageWidth - 12);
                details.add(icons, y);
                return y + icons.getHeight() + 3;
            }
        }
        return y;
    }

    /** The server's last rows for {@code page}, or null if it has not sent that page. */
    private static @Nullable List<EncyclopediaPayload.Row> pageRows(String page) {
        for (EncyclopediaPayload.Page found : EncyclopediaClient.navigator().pages()) {
            if (found.id().equals(page)) return found.rows();
        }
        return null;
    }

    /** The rows of the {@code step}-th step: its step row and every row up to the next step or heading. */
    static List<EncyclopediaPayload.Row> stepRows(List<EncyclopediaPayload.Row> rows, int step) {
        int seen = -1;
        List<EncyclopediaPayload.Row> out = new ArrayList<>();
        for (EncyclopediaPayload.Row row : rows) {
            EncyclopediaPayload.Kind kind = row.kindOf();
            if (kind == EncyclopediaPayload.Kind.STEP) seen++;
            if (seen == step) {
                if (!out.isEmpty() && (kind == EncyclopediaPayload.Kind.STEP || kind == EncyclopediaPayload.Kind.HEADING)) break;
                out.add(row);
            }
        }
        return out;
    }

    /** A step's existing entry as this screen's link. */
    private static Link link(ProgressionRoutes.Entry entry) {
        Identifier category = entry.type().equals("codex") ? Identifier.tryParse(entry.target()) : null;
        return new Link(entry.type(), entry.chapter(), entry.page(), category, entry.target());
    }

    /** Opens the map's live route page at the {@code step}-th step, with a button back to this map. */
    private void openLiveRecipe(int step) {
        if (mapped == null) return;
        EncyclopediaScreen book = parent instanceof EncyclopediaScreen existing ? existing : new EncyclopediaScreen(EncyclopediaScreen.Back.GAME);
        book.returnTo(this);
        minecraft.gui.setScreen(book);
        book.showStep(mapped.page(), step);
    }

    private int paragraph(int y, Component text) {
        var widget = new BookArt.Paragraph(text.copy().withColor(BookArt.INK & 0xFFFFFF), font, pageWidth - 9, BookArt.INK);
        widget.setX(left + panelWidth / 2 + 17); details.add(widget, y);
        return y + widget.getHeight() + 3;
    }
    private Button tab(int x, int y, int w, int h, Component label, String icon, boolean selected, Runnable action) {
        return addRenderableWidget(new BookArt.Tab(x, y, w, h, label, Component.empty(), icon, selected, false, unused -> action.run()));
    }
	private boolean available(Link link) {
		return !link.type().equals("codex") || codexLoaded;
	}

	/** Opens where a link leads. The handbook closes back here; the codex and the journal close to the game. */
	private void follow(Link link) {
		if (!available(link)) {
			return;
		}
		switch (link.type()) {
			case "handbook" -> minecraft.gui.setScreen(new HandbookScreen(link.chapter(), link.page(), this));
			case "codex" -> BookGuiManager.get().openBook(BookAddress.of(CODEX, link.category(), null, -1));
			case "journal" -> JournalClient.open(minecraft);
            case "page" -> {
                EncyclopediaScreen book = parent instanceof EncyclopediaScreen existing ? existing : new EncyclopediaScreen(EncyclopediaScreen.Back.GAME);
                book.returnTo(this);
                minecraft.gui.setScreen(book); book.showPage(link.target());
            }
			default -> Jugcraft.LOGGER.warn("Encyclopedia link of unknown type {}", link.type());
		}
	}

    @Override public boolean keyPressed(KeyEvent event) {
        // The encyclopedia's key closes the guides as it closes the encyclopedia (to the game, or the inventory it came from).
        if (EncyclopediaClient.key().matches(event)) {
            if (parent instanceof EncyclopediaScreen book) book.onClose(); else onClose();
            return true;
        }
        if (route < 0) return super.keyPressed(event);
        if (event.input() == InputConstants.KEY_BACKSPACE) { showHome(); return true; }
        if (map) {
            if (event.input() == InputConstants.KEY_DOWN || event.input() == InputConstants.KEY_UP) {
                if (!steps.isEmpty()) {
                    int step = event.input() == InputConstants.KEY_DOWN ? 1 : -1;
                    cursor = cursor < 0 ? (step > 0 ? 0 : steps.size() - 1) : Math.floorMod(cursor + step, steps.size());
                    rebuildWidgets();
                    if (cursor < nodes.size()) { directory.setFocused(nodes.get(cursor)); setFocused(directory); }
                }
                return true;
            }
            if (event.input() == InputConstants.KEY_RETURN && getFocused() == directory && cursor >= 0 && cursor < steps.size()) {
                follow(link(steps.get(cursor).entry())); return true;
            }
            return super.keyPressed(event);
        }
        if (event.input() == InputConstants.KEY_DOWN || event.input() == InputConstants.KEY_UP) {
            if (!entries.isEmpty()) {
                int step = event.input() == InputConstants.KEY_DOWN ? 1 : -1;
                cursor = cursor < 0 ? (step > 0 ? 0 : entries.size() - 1) : Math.floorMod(cursor + step, entries.size());
                rebuildWidgets(); directory.setFocused(entryButtons.get(cursor)); setFocused(directory);
            }
            return true;
        }
        if (event.input() == InputConstants.KEY_RETURN && getFocused() == directory && cursor >= 0 && cursor < entries.size()) { follow(entries.get(cursor).link()); return true; }
        return super.keyPressed(event);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0xB20C1118); BookArt.book(g, left, top, panelWidth, panelHeight);
        String title = route < 0 ? ui("book.guides").getString() : routes.get(route).title();
        g.text(font, BookArt.fit(font, title, pageWidth), left + 20, top + 21, BookArt.INK, false);
        BookArt.rule(g, left + 20, top + 36, pageWidth - 4);
        int rx = left + panelWidth / 2 + 15;
        g.text(font, ui("book.field_notes"), rx, top + 21, BookArt.INK, false);
        BookArt.rule(g, rx, top + 36, pageWidth - 4);
        if (route >= 0) {
            String icon = map ? (cursor >= 0 && cursor < steps.size() ? steps.get(cursor).item().toString() : routes.get(route).icon())
                : cursor >= 0 && cursor < entries.size() ? entries.get(cursor).icon() : routes.get(route).icon();
            int cx = rx + pageWidth / 2;
            BookArt.circle(g, cx, top + 59, 17, 0x80876D43);
            BookArt.icon(g, icon, cx - 8, top + 51);
        }
    }
}
