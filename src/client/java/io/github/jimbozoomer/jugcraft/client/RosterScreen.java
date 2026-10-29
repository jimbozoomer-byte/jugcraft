package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.RosterRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.RosterStatePayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

/**
 * Concordance Chunk D, D-07 (D-S11): the roster, native and narrated. Every pact and agreement the player holds (one without
 * its place included), their courier requests and the rest of their familiars, spirits and constructs, as the server lists
 * them. Since the magic interfaces' unit 2 (docs/features/magic-interfaces.md, spec 2.14) it is drawn on the magic layer:
 * the gold title "Roster" and its page over the cosmos, and two columns of dark relic cards, each the portrait in its gilded
 * card frame (a render of the worker's own model; a request's item), the name, the state's mark and a few words (never
 * colour alone), one fact of at most four words with its item, the kind, and its actions as icons. Every sentence of the
 * row stays whole as the card's narration and its tip (hover or keyboard focus): its state and why it waits, its service,
 * place and whether it is loaded, its task and next obligation. The actions: suspend (two bars), resume, end (a cross,
 * which asks first, the keyboard on Keep it), cancel and recover; each sends one bounded request through
 * {@link RosterClient}, and the server's answer, a refusal included, is shown short with its mark (PactScreen.Verdict) in
 * the title row and read aloud whole (the switched-off note and the opening line sit there too, never pushing the cards).
 * Eight cards a page (Page Up and Page Down, or the footer's plates, turn it; how many more are not listed sits between
 * them and Refresh), six or fewer on a screen too short for four rows of cards (spec 2.14: six at 320x180); a page that
 * would draw over its 70 words drops its cards' kinds (each stays in its card's tip). Every card is one focusable, narrated
 * control and every action a button, which the mouse presses through its card: Tab and Shift+Tab move, Enter or Space
 * presses, Esc closes. It fits the smallest GUI (ScreenKit.Fitted; ScreenFitClientGameTests) and, an in-game screen, draws
 * subtitles over itself (MagicScreen). No text box, chat command, vanilla Dialog or GuiLib, and no second inventory: cargo
 * is the courier ledger's and only the server moves it.
 */
public class RosterScreen extends MagicScreen {
	/**
	 * Rows a page shows at most (the server sends at most RosterStatePayload.MAX_ROWS: eight pages at eight a page, eleven at
	 * six); see {@link #perPage}.
	 */
	public static final int ROWS_PER_PAGE = 8;
	/** The words a page draws at most (spec 1.1: a page of eight cards, its title, page and footer). */
	private static final int BUDGET = 70;
	/** A card's text starts this far in (after its portrait); its actions sit this far from its right edge. */
	private static final int TEXT_X = 37;
	private static final int ACTION_X = 19;
	/** A card under this wide draws the narrow card (spec 2.14): its name regular, its actions stacked, its kind in its tip. */
	private static final int NARROW_CARD = 180;
	/** A card's height: four rows a page where the screen is tall enough, never under the portrait frame's 41 and a rim. */
	private static final int CARD_MIN = 42;
	private static final int CARD_MAX = 48;
	/** The title row's and the footer's band: the cards' dark purple at 85% (C8). */
	private static final int BAND = 0xD8170F24;
	/** A row's actions, in the order their keys follow its card. */
	private static final int[] ACTIONS = {RosterRequestPayload.SUSPEND, RosterRequestPayload.RESUME, RosterRequestPayload.END,
			RosterRequestPayload.CANCEL, RosterRequestPayload.RECOVER};
	private final Runnable listener = () -> dirty = true;
	/** Each control's row (its place in the listing), so the keyboard stays with a row's place when the row goes. */
	private final Map<String, Integer> places = new HashMap<>();
	/** How each row's portrait was last drawn ("portrait" or "icon"), by row id, for tests. */
	private final Map<UUID, String> drawn = new HashMap<>();
	/** The cards whose words were cut to their room, for tests (spec 4.5: the roster cuts no label). */
	private final Set<String> cut = new LinkedHashSet<>();
	private final Component heading;
	private Component pageText = Component.empty();
	private boolean fromTop;
	private int page;
	private int focusRow = -1;
	/** How many cards a page held when last built, and the first row it showed: a resize keeps the rows in view (C12). */
	private int builtPer;
	private int firstShown;

	public RosterScreen() {
		super(Component.translatable("screen.jugcraft.roster.title"));
		heading = title.copy().withStyle(ChatFormatting.BOLD);
	}

	static Component ui(String key, Object... args) {
		return Component.translatable("screen.jugcraft.roster." + key, args);
	}

	/** Forgets the portraits (RosterClient does, on leaving a world): the magic interfaces' visual-only cache. */
	static void forget() {
		MagicPortraits.clear();
	}

	@Override
	protected void init() {
		RosterClient.unlisten(listener);
		RosterClient.listen(listener);
		super.init();
	}

	@Override
	protected MagicLayout layout(int width, int height) {
		return MagicLayout.roster(width, height);
	}

	@Override
	protected void build() {
		places.clear();
		cut.clear();
		if (fromTop) {
			scrollTo(0);
			fromTop = false;
		}
		RosterStatePayload state = RosterClient.state();
		List<RosterStatePayload.Row> rows = state == null ? List.of() : state.rows();
		int pages = pages();
		int per = perPage();
		if (builtPer > 0 && per != builtPer) {
			// Another GUI size holds another number of cards a page: the page that holds the row the keyboard is on, else the
			// first row shown, so a resize keeps them in view.
			page = (focusRow >= 0 ? focusRow : firstShown) / per;
		}
		page = Math.max(0, Math.min(page, pages - 1));
		builtPer = per;
		firstShown = page * per;
		pageText = state == null ? Component.empty() : ui("short.page", page + 1, pages);
		drew(heading.getString());
		drew(pageText.getString());
		// The title row's room: the answer, else the switched-off note, else the opening line (none of them moves the cards).
		ScreenRectangle room = titleRoom();
		boolean answered = answer(room);
		boolean noted = state != null && !state.enabled();
		if (state == null) {
			boolean available = RosterClient.available();
			pin("opening", line(ui(available ? "opening" : "unavailable"), available ? ui("opening") : ui("short.unavailable"), MagicTheme.LAVENDER,
					room.width(), null), room, 2);
		} else if (noted && !answered) {
			pin("note", note(room.width()), room, 2);
		}
		ScreenRectangle area = layout.body();
		// Its gold ball (only while the body scrolls: the empty roster's lines in the smallest body) beside the cards, else on
		// their gap.
		int ball = area.right() + 13 <= width ? area.right() + 1 : area.left() + layout.rosterCard() + MagicLayout.ROSTER_GAP / 2 - 6;
		openBody(area).ball(MagicArt.SCROLL_BALL, ball, 12);
		if (state != null && rows.isEmpty()) {
			say(ui("empty"), ui("short.empty"), MagicTheme.CREAM, "empty");
			say(ui("empty.hint"), ui("short.empty.hint"), MagicTheme.LAVENDER, "");
		}
		boolean more = state != null && state.more() > 0 && page == pages - 1;
		footer(pages, more ? state.more() : 0, noted && answered);
		if (state != null) {
			// Last: a page weighs its cards against what the rest of the screen already draws.
			cards(rows, area);
		}
		keepPlace(rows);
	}

	/** The title row's room between the title (its underline included) and the page. */
	private ScreenRectangle titleRoom() {
		ScreenRectangle header = layout.header();
		int from = header.left() + font.width(heading) + 28;
		int to = header.right() - 4 - (pageText.getString().isEmpty() ? 0 : font.width(pageText) + 4);
		return new ScreenRectangle(from, header.top(), Math.max(30, to - from), header.height());
	}

	/** A line pinned to {@code room} (the title row, the footer's band), {@code down} below its top, held there (outside()). */
	private void pin(String key, AbstractWidget widget, ScreenRectangle room, int down) {
		widget.setX(room.left());
		widget.setY(room.top() + down);
		onScreen(key, widget, room);
	}

	/** One line on the cosmos, {@code width} wide, cut with "…" when it needs more (its whole sentence then its tip). */
	private MagicText line(Component full, Component shown, int colour, int width, MagicArt.@Nullable Mark mark) {
		MagicText line = new MagicText(full, shown, font, width, colour, MagicTheme.DARK).maxLines(1);
		return mark == null ? line : line.lead(mark);
	}

	/** The switched-off note: its lock and its few words, the server's whole sentence its tip and narration. */
	private MagicText note(int width) {
		return line(ui("disabled"), ui("short.disabled"), MagicTheme.WARN, width, MagicArt.Mark.LOCK);
	}

	/**
	 * This page's cards, two to a row: each card, then its actions (their keys follow it), as many rows as the body holds. A
	 * page that would draw more than its {@value #BUDGET} words (every card at eight, an answer at the top) draws its cards
	 * without their kinds, each kind kept in its card's tip and narration (spec 1.1).
	 */
	private void cards(List<RosterStatePayload.Row> rows, ScreenRectangle area) {
		int card = layout.rosterCard();
		int gap = gap();
		int lines = perPage() / 2;
		int tall = Math.max(CARD_MIN, Math.min(CARD_MAX, (area.height() - (lines - 1) * gap) / lines));
		int first = page * perPage();
		int last = Math.min(rows.size(), first + perPage());
		List<Card> made = make(rows, first, last, area, card, tall, false);
		int words = words();
		for (Card each : made) {
			words += MagicScreen.words(each.drawnText());
		}
		if (words > BUDGET) {
			made = make(rows, first, last, area, card, tall, true);
		}
		int top = at;
		boolean free = !RosterClient.waiting() && RosterClient.available();
		for (int i = first; i < last; i++) {
			int slot = i - first;
			card(made.get(slot), rows.get(i), i, top + slot / 2 * (tall + gap), free);
		}
		if (last > first) {
			at = top + (last - first + 1) / 2 * (tall + gap) + 1;
		}
	}

	/** The cards of rows {@code first} to {@code last} (not yet added), with or without their kinds. */
	private List<Card> make(List<RosterStatePayload.Row> rows, int first, int last, ScreenRectangle area, int card, int tall, boolean kindless) {
		List<Card> made = new ArrayList<>();
		for (int i = first; i < last; i++) {
			int x = area.left() + (i - first) % 2 * (card + MagicLayout.ROSTER_GAP);
			made.add(new Card(rows.get(i), rows, x, card, tall, offered(rows.get(i)).size(), kindless));
		}
		return made;
	}

	/** The actions a row offers, in {@link #ACTIONS}' order. */
	private static List<Integer> offered(RosterStatePayload.Row row) {
		List<Integer> offered = new ArrayList<>();
		for (int action : ACTIONS) {
			if (row.offers(action)) {
				offered.add(action);
			}
		}
		return offered;
	}

	private void card(Card card, RosterStatePayload.Row row, int index, int offset, boolean free) {
		List<Integer> offered = offered(row);
		int x = card.getX();
		int width = card.getWidth();
		inBodyAt("row:" + row.id(), card, offset);
		places.put("row:" + row.id(), index);
		if (card.cut()) {
			cut.add("row:" + row.id());
		}
		int right = x + width - ACTION_X;
		for (int i = 0; i < offered.size(); i++) {
			int action = offered.get(i);
			String name = RosterRequestPayload.ACTIONS.get(action);
			// The last action offered sits at the card's right (End's cross); the others before it, or under it on a narrow card.
			int from = offered.size() - 1 - i;
			MagicButton button = new MagicButton(look(action), ui(name), Component.empty(), unused -> act(row, action));
			// Cancel's narration says, in the server's words, what it does (nothing is lost, so it does not ask first).
			Component hint = action == RosterRequestPayload.CANCEL && !row.confirm().getString().isEmpty() ? row.confirm()
					: ui(name + ".hint", row.name());
			// Its tip, one line naming the card ("End Bathin..."); its narration adds the whole hint.
			button.hint(Component.translatable("screen.jugcraft.roster.short." + name + ".tip", RosterGlance.name(row)), hint);
			button.active = free;
			button.setX(card.narrow() ? right : right - 16 * from);
			button.at(new CardTip(card, button));
			inBodyAt(name + ":" + row.id(), button, offset + 4 + (card.narrow() ? 17 * from : 0));
			places.put(name + ":" + row.id(), index);
			card.actions.add(button);
		}
	}

	/** An action's icon (spec 2.14): End and Cancel the cross, Suspend the two bars, Resume the play triangle, Recover the up arrow. */
	private static MagicButton.Look look(int action) {
		return switch (action) {
			case RosterRequestPayload.SUSPEND -> MagicButton.Look.ACTION_SUSPEND;
			case RosterRequestPayload.RESUME -> MagicButton.Look.ACTION_RESUME;
			case RosterRequestPayload.RECOVER -> MagicButton.Look.ACTION_OFFER;
			default -> MagicButton.Look.ACTION_END;
		};
	}

	/**
	 * The server's answer to the last action, short with its mark (D-06's Verdict) on one line of the title row, kept until
	 * the next is sent; while one waits, that it waits. Whether either was pinned.
	 */
	private boolean answer(ScreenRectangle room) {
		if (RosterClient.waiting()) {
			pin("result", line(ui("waiting"), ui("waiting"), MagicTheme.LAVENDER, room.width(), null), room, 2);
			return true;
		}
		PactStatePayload.Result answer = RosterClient.result();
		if (answer == null) {
			return false;
		}
		int colour = answer.done() ? MagicTheme.GOOD : MagicTheme.WARN;
		pin("result", new PactScreen.Verdict(answer.message(), PactGlance.of(answer.message()).shown(), answer.done(), font, room.width(), colour,
				true, true), room, 2);
		return true;
	}

	private void say(Component full, Component shown, int colour, String key) {
		ScreenRectangle area = layout.body();
		MagicText line = new MagicText(full, shown, font, area.width() - 4, colour, MagicTheme.DARK);
		line.setX(area.left() + 2);
		inBody(key, line, 4);
	}

	/**
	 * The footer (spec 2.14): the page's arrows at its left, Refresh and Close at its right, their tips below or beside it;
	 * between them, on the last page, how many {@code more} are not listed, and the switched-off note when an answer holds
	 * the title row ({@code noted}).
	 */
	private void footer(int pages, int more, boolean noted) {
		ScreenRectangle row = layout.footer();
		boolean free = !RosterClient.waiting() && RosterClient.available();
		MagicButton previous = plate(ui("previous"), Component.empty(), () -> turn(page - 1)).glyph(MagicArt.Glyph.BACK)
				.hint(ui("previous.hint"), Component.empty());
		previous.active = page > 0;
		MagicButton next = plate(ui("next"), Component.empty(), () -> turn(page + 1)).glyph(MagicArt.Glyph.ARROW)
				.hint(ui("next.hint"), Component.empty());
		next.active = page + 1 < pages;
		MagicButton refresh = plate(ui("refresh"), Component.empty(), RosterClient::refresh).icon(MagicArt.sprite("action/refresh"), 14, 13)
				.hint(ui("refresh.hint"), Component.empty());
		refresh.active = free;
		MagicButton close = plate(ui("close"), ui("close"), this::onClose);
		int closeWidth = Math.max(56, close.labelWidth(font));
		place("previous", previous, row.left(), previous.labelWidth(font), row);
		int nextX = row.left() + previous.labelWidth(font) + 4;
		place("next", next, nextX, next.labelWidth(font), row);
		int refreshX = row.right() - closeWidth - 5 - refresh.labelWidth(font);
		// The band between the arrows and Refresh (C7: on screen whatever the page's cards), held there.
		int from = nextX + next.labelWidth(font) + 6;
		int room = refreshX - 6 - from;
		int shared = more > 0 && noted ? (room - 6) / 2 : room;
		if (more > 0) {
			ScreenRectangle band = new ScreenRectangle(from, row.top(), Math.max(20, shared), row.height());
			pin("more", line(ui("more", more, RosterStatePayload.MAX_ROWS), ui("short.more", more), MagicTheme.LAVENDER, band.width(), null), band, 4);
			from += shared + 6;
		}
		if (noted) {
			ScreenRectangle band = new ScreenRectangle(from, row.top(), Math.max(20, shared), row.height());
			pin("note", note(band.width()), band, 4);
		}
		place("refresh", refresh, refreshX, refresh.labelWidth(font), row);
		place("close", close, row.right() - closeWidth, closeWidth, row);
	}

	private MagicButton plate(Component message, Component label, Runnable action) {
		return new MagicButton(MagicButton.Look.PLATE, message, label, unused -> action.run());
	}

	private void place(String key, MagicButton button, int x, int width, ScreenRectangle row) {
		button.setRectangle(width, MagicArt.PLATE_HEIGHT, x, row.top());
		button.footer(row);
		onScreen(key, button);
		footer.add(key);
	}

	private void act(RosterStatePayload.Row row, int action) {
		if (action == RosterRequestPayload.END) {
			confirmEnd(row);
		} else {
			RosterClient.request(action, row.id(), row.revision());
		}
	}

	/**
	 * Ending asks first (D-07): only its yes sends END, for the row and revision it was asked about, with the server's words
	 * (what comes back, once) as its narration; drawn on the scroll over the cosmos with two short lines (spec 2.12); Esc
	 * and Keep it keep it, and the keyboard starts on Keep it (KeepFirstConfirmScreen).
	 */
	private void confirmEnd(RosterStatePayload.Row row) {
		minecraft.gui.setScreen(KeepFirstConfirmScreen.magic(yes -> {
			minecraft.gui.setScreen(this);
			if (yes) {
				RosterClient.request(RosterRequestPayload.END, row.id(), row.revision());
			}
		}, ui("end.title", row.name()), row.confirm().getString().isEmpty() ? ui("end.confirm", row.name(), row.kind()) : row.confirm(),
				List.of(ui("short.end.leaves"), Component.translatable("screen.jugcraft.pact.short.end.undone")), ui("end.yes"), ui("end.no")));
	}

	private void turn(int to) {
		int next = Math.max(0, Math.min(to, pages() - 1));
		if (next == page) {
			return;
		}
		page = next;
		fromTop = true;
		dirty = true;
		minecraft.getNarrator().saySystemNow(heading());
	}

	/**
	 * The keyboard stays where it was: on the same control after a refresh or an answer; when that control is gone (its row
	 * ended) or cannot be pressed (an answer is awaited), on the row now in its place on this page, else the page's first
	 * line; before anything was focused, setInitialFocus decides (MagicScreen gives the kept key back after this).
	 */
	private void keepPlace(List<RosterStatePayload.Row> rows) {
		if (focus.isEmpty()) {
			return;
		}
		AbstractWidget widget = controls.get(focus);
		if (widget != null && widget.active) {
			return;
		}
		String key = "";
		int first = page * perPage();
		int last = Math.min(rows.size(), first + perPage()) - 1;
		if (last >= first) {
			int index = focusRow < 0 ? first : Math.max(first, Math.min(focusRow, last));
			key = "row:" + rows.get(index).id();
		}
		if (!controls.containsKey(key)) {
			key = controls.containsKey("empty") ? "empty" : "close";
		}
		focus = key;
	}

	@Override
	protected void focused(@Nullable GuiEventListener target) {
		super.focused(target);
		controls.forEach((key, widget) -> {
			if (widget == target) {
				focusRow = places.getOrDefault(key, -1);
			}
		});
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.input() == InputConstants.KEY_PAGEDOWN || event.input() == InputConstants.KEY_PAGEUP) {
			turn(page + (event.input() == InputConstants.KEY_PAGEDOWN ? 1 : -1));
			return true;
		}
		return super.keyPressed(event);
	}

	private Component heading() {
		RosterStatePayload state = RosterClient.state();
		return state == null ? Component.empty() : ui("page", page + 1, pages(), state.rows().size());
	}

	@Override
	protected void extractChrome(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		// The title row and the footer on a dark band (C8: the title and the page at 4.5:1 or more over a bright world), then
		// the title in gold over its underline at the panel's left, the page at its right (spec 2.14); no panel art: the cards
		// are it.
		ScreenRectangle header = layout.header();
		ScreenRectangle foot = layout.footer();
		band(g, header);
		band(g, foot);
		MagicArt.title(g, font, heading, header.left() + 12, header.top() + 3, font.width(heading) + 24);
		if (!pageText.getString().isEmpty()) {
			g.text(font, pageText, header.right() - 4 - font.width(pageText), header.top() + 3, MagicTheme.LAVENDER, true);
		}
	}

	/** A dark band behind a row of the chrome: the card's well, nearly opaque, its corners cut. */
	private static void band(GuiGraphicsExtractor g, ScreenRectangle row) {
		g.fill(row.left() + 1, row.top(), row.right() - 1, row.bottom(), BAND);
		g.fill(row.left(), row.top() + 1, row.left() + 1, row.bottom() - 1, BAND);
		g.fill(row.right() - 1, row.top() + 1, row.right(), row.bottom() - 1, BAND);
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(title, heading());
	}

	@Override
	protected Component getUsageNarration() {
		return ui("help");
	}

	@Override
	public void removed() {
		RosterClient.unlisten(listener);
		super.removed();
	}

	/** The page shown, from 0 (tests). */
	public int page() {
		return page;
	}

	public int pages() {
		RosterStatePayload state = RosterClient.state();
		int rows = state == null ? 0 : state.rows().size();
		return Math.max(1, (rows + perPage() - 1) / perPage());
	}

	/**
	 * How many cards a page shows in this layout (tests): two to a row, as many rows as its body holds whole, at most four
	 * ({@link #ROWS_PER_PAGE}): eight from 240 tall, six at 320x180 (spec 2.14).
	 */
	public int perPage() {
		int gap = gap();
		return 2 * Math.max(1, Math.min(ROWS_PER_PAGE / 2, (layout.body().height() + gap) / (CARD_MIN + gap)));
	}

	private int gap() {
		return height < MagicLayout.FULL_FROM ? 3 : 4;
	}

	/** The page row {@code id} is on, or -1 (tests). */
	public int pageOf(UUID id) {
		RosterStatePayload state = RosterClient.state();
		if (state != null) {
			for (int i = 0; i < state.rows().size(); i++) {
				if (state.rows().get(i).id().equals(id)) {
					return i / perPage();
				}
			}
		}
		return -1;
	}

	/** How row {@code id}'s portrait was last drawn: "portrait", "icon", or "" if not yet (tests). */
	public String drawn(UUID id) {
		return drawn.getOrDefault(id, "");
	}

	/** The cards on this page whose words were cut to fit (tests: none, spec 4.5). */
	public List<String> cut() {
		return List.copyOf(cut);
	}

	/** A request's item, or the item a worker's type names; the Spirit Anchor's when neither is an item. */
	private static String icon(RosterStatePayload.Row row) {
		Identifier id = row.portrait().isEmpty() ? null : Identifier.tryParse(row.portrait());
		return id != null && BuiltInRegistries.ITEM.containsKey(id) ? row.portrait() : "jugcraft:spirit_anchor";
	}

	private static Component stateWord(int icon) {
		return icon >= 0 && icon < RosterStatePayload.ICONS.size()
				? Component.translatable("screen.jugcraft.roster.state." + RosterStatePayload.ICONS.get(icon)) : Component.empty();
	}

	private static Component said(RosterStatePayload.Row row) {
		Component word = stateWord(row.icon());
		return row.waiting().getString().isEmpty() ? word : ui("row.state", word, row.waiting());
	}

	/** The row in words, as the narrator reads it and tests read it (every word the server sent: the card draws a few). */
	private static Component words(RosterStatePayload.Row row) {
		List<Component> parts = new ArrayList<>();
		parts.add(ui("row.title", row.name(), row.kind()));
		parts.addAll(details(row));
		return CommonComponents.joinForNarration(parts.toArray(Component[]::new));
	}

	/** The row's whole sentences after its title: its state and why, its service, its place, its task and its next obligation. */
	private static List<Component> details(RosterStatePayload.Row row) {
		List<Component> parts = new ArrayList<>();
		parts.add(said(row));
		if (!row.service().getString().isEmpty()) {
			parts.add(row.service());
		}
		if (!row.place().getString().isEmpty()) {
			parts.add(ui(row.loaded() ? "row.loaded" : "row.unloaded", row.place()));
		}
		if (!row.task().getString().isEmpty()) {
			parts.add(ui("row.task", row.task()));
		}
		if (!row.next().getString().isEmpty()) {
			parts.add(ui("row.obligation", row.next()));
		}
		return parts;
	}

	/**
	 * A state's mark (spec 2.5: the family's marks), always beside its words, so no meaning rests on colour: at work, in
	 * transit or going back a tick; waiting, an offering due or stranded cargo the hourglass or the cross; suspended, not
	 * loaded, lost or not yet claimed the moon; without its place or switched off the lock.
	 */
	static MagicArt.Mark mark(int icon) {
		return switch (icon) {
			case RosterStatePayload.ACTIVE, RosterStatePayload.IN_TRANSIT -> MagicArt.Mark.TICK;
			case RosterStatePayload.WAITING, RosterStatePayload.UPKEEP, RosterStatePayload.RETURNING -> MagicArt.Mark.HOURGLASS;
			case RosterStatePayload.SUSPENDED, RosterStatePayload.UNLOADED, RosterStatePayload.LOST, RosterStatePayload.UNCLAIMED -> MagicArt.Mark.MOON;
			case RosterStatePayload.WITHOUT_PLACE, RosterStatePayload.DISABLED -> MagicArt.Mark.LOCK;
			case RosterStatePayload.STRANDED -> MagicArt.Mark.CROSS;
			default -> MagicArt.Mark.ASK;
		};
	}

	/** A state's ink on the card's purple well (spec 2.3): good, needs action, refused, or the secondary lavender. */
	private static int ink(MagicArt.Mark mark) {
		return switch (mark) {
			case TICK -> MagicTheme.GOOD;
			case HOURGLASS, ASK -> MagicTheme.WARN;
			case CROSS -> MagicTheme.BAD;
			default -> MagicTheme.LAVENDER;
		};
	}

	/**
	 * An action's tip (spec 2.14: End's tip stays inside its own card's band): to the left of the button, else to its right,
	 * its top on the card's band, so it never covers the next row of cards.
	 */
	private record CardTip(AbstractWidget card, AbstractWidget button) implements ClientTooltipPositioner {
		/** Vanilla draws a tip's frame 3 beyond its words, and the gold rim a little further: keep this clear. */
		private static final int PAD = 5;

		@Override
		public Vector2ic positionTooltip(int screenWidth, int screenHeight, int x, int y, int tooltipWidth, int tooltipHeight) {
			int left = button.getX() - PAD - tooltipWidth - 2;
			if (left < PAD) {
				left = Math.min(screenWidth - PAD - tooltipWidth, button.getX() + button.getWidth() + PAD + 2);
			}
			int top = Math.max(card.getY() + PAD, Math.min(button.getY() + button.getHeight() + 2, card.getY() + card.getHeight() - PAD - tooltipHeight));
			return new Vector2i(Math.max(PAD, left), Math.max(PAD, Math.min(top, screenHeight - PAD - tooltipHeight)));
		}
	}

	/**
	 * A card's own tip (every sentence of its row; the unit's review, C4): beside the card toward the other column, as far
	 * as the screen allows (where it has no room it overlaps the card, as little as it can), its top at the card's, and
	 * always whole on the screen (a tall tip never starts above it).
	 */
	private record CardSide(AbstractWidget card) implements ClientTooltipPositioner {
		private static final int PAD = 5;

		@Override
		public Vector2ic positionTooltip(int screenWidth, int screenHeight, int x, int y, int tooltipWidth, int tooltipHeight) {
			boolean leftColumn = card.getX() + card.getWidth() / 2 < screenWidth / 2;
			int left = leftColumn ? card.getRight() + PAD + 2 : card.getX() - PAD - 2 - tooltipWidth;
			left = Math.max(PAD, Math.min(left, screenWidth - PAD - tooltipWidth));
			int top = Math.max(PAD, Math.min(card.getY() + PAD, screenHeight - PAD - tooltipHeight));
			return new Vector2i(left, top);
		}
	}

	/**
	 * One row as a relic card (spec 2.14): the gold frame and its purple well; the portrait in the gilded card frame; the
	 * name (bold, regular on a narrow card); the state's mark and a few words; one fact with its item; the kind at its foot
	 * (on a narrow card, or on a page over its budget, in its tip). It takes the keyboard so the narrator reads the row as
	 * one, every word of it; its tip is every sentence of the row; its actions are the keys after it, and the mouse over one
	 * reaches the action, not the card. A word too long for its room loses, in turn, the kind beside it, its item, and only
	 * then its end ("…", and {@link #cut()} says so).
	 */
	private final class Card extends AbstractWidget implements MagicScreen.Drawn {
		private final RosterStatePayload.Row row;
		private final boolean narrow;
		private final List<MagicButton> actions = new ArrayList<>();
		private final FormattedCharSequence name;
		private final MagicArt.Mark mark;
		private final FormattedCharSequence state;
		private final FormattedCharSequence fact;
		private final List<ItemStack> items;
		private final Component kind;
		private final List<FormattedCharSequence> tip = new ArrayList<>();
		private final String text;
		private final boolean cut;
		private final ClientTooltipPositioner side = new CardSide(this);

		Card(RosterStatePayload.Row row, List<RosterStatePayload.Row> rows, int x, int width, int height, int offered, boolean kindless) {
			super(x, 0, width, height, words(row));
			this.row = row;
			this.narrow = width < NARROW_CARD;
			// The name's room ends at the actions on its line; the lines under it end at a narrow card's column of actions.
			int nameRoom = (offered == 0 ? width - 6 : narrow ? width - ACTION_X - 3 : width - ACTION_X - 16 * (offered - 1) - 3) - TEXT_X;
			int lineRoom = (narrow && offered > 0 ? width - ACTION_X - 3 : width - 6) - TEXT_X;
			Component shownName = RosterGlance.name(row);
			Component styled = narrow ? shownName : shownName.copy().withStyle(ChatFormatting.BOLD);
			// A familiar or construct not loaded says so (the status it sends is only its last report; the unit's review, C9);
			// any other row draws the mark its waiting reason has in Jade too (MagicGlance: "?" for an answer asked, the moon
			// for idle), else its icon's.
			boolean away = !row.request() && row.next().getString().isEmpty() && !row.loaded() && row.icon() != RosterStatePayload.DISABLED;
			MagicArt.Mark said = MagicGlance.markOf(row.waiting());
			this.mark = away ? mark(RosterStatePayload.UNLOADED) : said != null ? said : mark(row.icon());
			Component shownState = away ? stateWord(RosterStatePayload.UNLOADED) : RosterGlance.state(row, stateWord(row.icon()));
			RosterGlance.Fact shownFact = RosterGlance.fact(row, rows);
			Component shownKind = RosterGlance.kind(row);
			int factWidth = font.width(shownFact.shown());
			int kindRoom = narrow || kindless || shownKind.getString().equals(shownName.getString()) ? 0 : font.width(shownKind) + 6;
			int iconRoom = shownFact.items().isEmpty() ? 0 : 17;
			if (kindRoom > 0 && factWidth + iconRoom > lineRoom - kindRoom) {
				kindRoom = 0;
			}
			if (iconRoom > 0 && factWidth + iconRoom > lineRoom - kindRoom) {
				iconRoom = 0;
			}
			int stateRoom = lineRoom - mark.width - 3;
			int factRoom = lineRoom - kindRoom - iconRoom;
			this.name = MagicArt.fit(font, styled, nameRoom);
			this.state = MagicArt.fit(font, shownState, stateRoom);
			this.fact = MagicArt.fit(font, shownFact.shown(), factRoom);
			this.items = iconRoom > 0 ? shownFact.items() : List.of();
			this.kind = kindRoom > 0 ? shownKind : Component.empty();
			this.cut = font.width(styled) > nameRoom || font.width(shownState) > stateRoom || factWidth > factRoom;
			String drawnName = MagicArt.fit(font, shownName.getString(), nameRoom);
			String drawnState = MagicArt.fit(font, shownState.getString(), stateRoom);
			String drawnFact = MagicArt.fit(font, shownFact.shown().getString(), factRoom);
			this.text = String.join(" ", List.of(drawnName, drawnState, drawnFact, kind.getString())).trim().replaceAll(" +", " ");
			// Its tip: every sentence of the row, its title first (the narrow card's kind is there).
			tip.addAll(font.split(ui("row.title", row.name(), row.kind()), 200));
			for (Component line : details(row)) {
				tip.addAll(font.split(line, 200));
			}
		}

		boolean narrow() {
			return narrow;
		}

		boolean cut() {
			return cut;
		}

		/** Whether ({@code x}, {@code y}) is on one of its actions, pressable or not (they sit on it). */
		private boolean overAction(double x, double y) {
			for (MagicButton action : actions) {
				if (x >= action.getX() && x < action.getX() + action.getWidth() && y >= action.getY() && y < action.getY() + action.getHeight()) {
					return true;
				}
			}
			return false;
		}

		/** Not under the mouse where an action is: a click there is the action's (its card is before it in the body). */
		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return super.isMouseOver(mouseX, mouseY) && !overAction(mouseX, mouseY);
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
			if (isHovered && overAction(mouseX, mouseY)) {
				// The action under the mouse is lit, tipped and narrated, not its card.
				isHovered = false;
			}
			int x0 = getX();
			int y0 = getY();
			MagicArt.panel(g, MagicArt.GOLD_FRAME, x0, y0, width, height);
			// The portrait: the worker's own model (visual-only, MagicPortraits) or its item, in the cosmos window, then the
			// gilded card frame over it (the HUD's card frame, greyed when the Concordance is switched off).
			int frameY = y0 + (height - 41) / 2;
			LivingEntity creature = row.request() ? null : MagicPortraits.of(row.portrait());
			MagicPortrait.draw(g, x0 + 8, frameY + 6, 20, 29, creature, icon(row), false);
			drawn.put(row.id(), creature != null ? "portrait" : "icon");
			MagicArt.panel(g, row.icon() == RosterStatePayload.DISABLED ? MagicArt.CARD_FRAME_INACTIVE : MagicArt.CARD_FRAME_ACTIVE, x0 + 3,
					frameY, 30, 41);
			int tx = x0 + TEXT_X;
			int spread = (height - CARD_MIN) / 3;
			g.text(font, name, tx, y0 + 5, MagicTheme.CREAM, true);
			MagicArt.state(g, font, mark, state, tx, y0 + 17 + spread, ink(mark));
			int fy = y0 + 29 + 2 * spread;
			int fx = tx;
			if (!items.isEmpty()) {
				int shown = ConcordanceClientOptions.reducedMotion() ? 0 : (int) (Util.getMillis() / 1000L % items.size());
				g.item(items.get(shown), tx - 1, fy - 4);
				fx += 17;
			}
			g.text(font, fact, fx, fy, MagicTheme.LAVENDER, true);
			if (!kind.getString().isEmpty()) {
				g.text(font, kind, x0 + width - 6 - font.width(kind), fy, MagicTheme.LAVENDER, true);
			}
			if (isHoveredOrFocused()) {
				// Lit (spec 2.8): a light outline on its rim, a fainter one inside it.
				g.outline(x0, y0, width, height, MagicArt.CARD_LIGHT);
				g.outline(x0 + 1, y0 + 1, width - 2, height - 2, MagicArt.CARD_LIGHT & 0x78FFFFFF);
			}
		}

		@Override
		protected void extractTooltipForNextRenderPass(GuiGraphicsExtractor g, int mouseX, int mouseY) {
			MagicArt.tip(g, this, tip, side, mouseX, mouseY);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			output.add(NarratedElementType.TITLE, getMessage());
			output.add(NarratedElementType.USAGE, ui(row.actions() == 0 ? "row.usage.none" : "row.usage"));
		}

		@Override
		public String drawnText() {
			return text;
		}
	}
}
