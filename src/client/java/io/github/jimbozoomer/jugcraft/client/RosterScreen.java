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
 * {@link RosterClient}, and the server's answer, a refusal included, is shown short with its mark (PactScreen.Verdict) and
 * read aloud whole. Eight cards a page (Page Up and Page Down, or the footer's plates, turn it), six or fewer on a screen too
 * short for four rows of cards (spec 2.14: six at 320x180). Every card is one focusable, narrated control and every action a button: Tab and
 * Shift+Tab move, Enter or Space presses, Esc closes. It fits the smallest GUI (ScreenKit.Fitted; ScreenFitClientGameTests)
 * and, an in-game screen, draws subtitles over itself (MagicScreen). No text box, chat command, vanilla Dialog or GuiLib,
 * and no second inventory: cargo is the courier ledger's and only the server moves it.
 */
public class RosterScreen extends MagicScreen {
	/** Rows a page shows at most (the server sends at most RosterStatePayload.MAX_ROWS: at most eight pages); see {@link #perPage}. */
	public static final int ROWS_PER_PAGE = 8;
	/** A card's text starts this far in (after its portrait); its actions sit this far from its right edge. */
	private static final int TEXT_X = 37;
	private static final int ACTION_X = 19;
	/** A card under this wide draws the narrow card (spec 2.14): its name regular, its actions stacked, its kind in its tip. */
	private static final int NARROW_CARD = 180;
	/** A card's height: four rows a page where the screen is tall enough, never under the portrait frame's 41 and a rim. */
	private static final int CARD_MIN = 42;
	private static final int CARD_MAX = 48;
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
		page = Math.max(0, Math.min(page, pages - 1));
		ScreenRectangle area = layout.body();
		// Its gold ball (only while an answer or a note pushes the cards past the body) beside the cards, else on their gap.
		int ball = area.right() + 13 <= width ? area.right() + 1 : area.left() + layout.rosterCard() + MagicLayout.ROSTER_GAP / 2 - 6;
		openBody(area).ball(MagicArt.SCROLL_BALL, ball, 12);
		answer(area);
		if (state == null) {
			boolean available = RosterClient.available();
			say(ui(available ? "opening" : "unavailable"), available ? ui("opening") : ui("short.unavailable"), MagicTheme.LAVENDER, "opening");
		} else {
			if (!state.enabled()) {
				say(ui("disabled"), ui("short.disabled"), MagicTheme.WARN, "note");
			}
			if (rows.isEmpty()) {
				say(ui("empty"), ui("short.empty"), MagicTheme.CREAM, "empty");
				say(ui("empty.hint"), ui("short.empty.hint"), MagicTheme.LAVENDER, "");
			}
			cards(rows, area);
			if (state.more() > 0 && page == pages - 1) {
				say(ui("more", state.more(), RosterStatePayload.MAX_ROWS), ui("short.more", state.more()), MagicTheme.LAVENDER, "more");
			}
		}
		pageText = state == null ? Component.empty() : ui("short.page", page + 1, pages);
		drew(heading.getString());
		drew(pageText.getString());
		footer(pages);
		keepPlace(rows);
	}

	/** This page's cards, two to a row: each card, then its actions (their keys follow it), as many rows as the body holds. */
	private void cards(List<RosterStatePayload.Row> rows, ScreenRectangle area) {
		int card = layout.rosterCard();
		int gap = gap();
		int lines = perPage() / 2;
		int tall = Math.max(CARD_MIN, Math.min(CARD_MAX, (area.height() - (lines - 1) * gap) / lines));
		int first = page * perPage();
		int last = Math.min(rows.size(), first + perPage());
		int top = at;
		boolean free = !RosterClient.waiting() && RosterClient.available();
		for (int i = first; i < last; i++) {
			int slot = i - first;
			int x = area.left() + slot % 2 * (card + MagicLayout.ROSTER_GAP);
			int offset = top + slot / 2 * (tall + gap);
			card(rows.get(i), i, rows, x, offset, card, tall, free);
		}
		if (last > first) {
			at = top + (last - first + 1) / 2 * (tall + gap) + 1;
		}
	}

	private void card(RosterStatePayload.Row row, int index, List<RosterStatePayload.Row> rows, int x, int offset, int width, int height,
			boolean free) {
		List<Integer> offered = new ArrayList<>();
		for (int action : ACTIONS) {
			if (row.offers(action)) {
				offered.add(action);
			}
		}
		Card card = new Card(row, rows, x, width, height, offered.size());
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

	/** An action's icon (spec 2.14): End and Cancel the cross, Suspend the two bars, Resume the turning arrows, Recover the up arrow. */
	private static MagicButton.Look look(int action) {
		return switch (action) {
			case RosterRequestPayload.SUSPEND -> MagicButton.Look.ACTION_SUSPEND;
			case RosterRequestPayload.RESUME -> MagicButton.Look.ACTION_REFRESH;
			case RosterRequestPayload.RECOVER -> MagicButton.Look.ACTION_OFFER;
			default -> MagicButton.Look.ACTION_END;
		};
	}

	/** The server's answer to the last action, short with its mark (D-06's Verdict), kept until the next is sent; while one waits, that it waits. */
	private void answer(ScreenRectangle area) {
		if (RosterClient.waiting()) {
			say(ui("waiting"), ui("waiting"), MagicTheme.LAVENDER, "result");
			return;
		}
		PactStatePayload.Result answer = RosterClient.result();
		if (answer != null) {
			int colour = answer.done() ? MagicTheme.GOOD : MagicTheme.WARN;
			PactScreen.Verdict line = new PactScreen.Verdict(answer.message(), PactGlance.of(answer.message()).shown(), answer.done(), font,
					area.width() - 4, colour, true);
			line.setX(area.left() + 2);
			inBody("result", line, 4);
		}
	}

	private void say(Component full, Component shown, int colour, String key) {
		ScreenRectangle area = layout.body();
		MagicText line = new MagicText(full, shown, font, area.width() - 4, colour, MagicTheme.DARK);
		line.setX(area.left() + 2);
		inBody(key, line, 4);
	}

	/** The footer (spec 2.14): the page's arrows at its left, Refresh and Close at its right, their tips below or beside it. */
	private void footer(int pages) {
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
		place("next", next, row.left() + previous.labelWidth(font) + 4, next.labelWidth(font), row);
		place("refresh", refresh, row.right() - closeWidth - 5 - refresh.labelWidth(font), refresh.labelWidth(font), row);
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
		// The title in gold over its underline at the panel's left, the page at its right (spec 2.14); no panel art: the cards are it.
		ScreenRectangle header = layout.header();
		MagicArt.title(g, font, heading, header.left() + 12, header.top() + 3, font.width(heading) + 24);
		if (!pageText.getString().isEmpty()) {
			g.text(font, pageText, header.right() - 4 - font.width(pageText), header.top() + 3, MagicTheme.LAVENDER, true);
		}
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
	 * One row as a relic card (spec 2.14): the gold frame and its purple well; the portrait in the gilded card frame; the
	 * name (bold, regular on a narrow card); the state's mark and a few words; one fact with its item; the kind at its foot
	 * (on a narrow card, in its tip). It takes the keyboard so the narrator reads the row as one, every word of it; its tip
	 * is every sentence of the row; its actions are the keys after it. A word too long for its room loses, in turn, the
	 * kind beside it, its item, and only then its end ("…", and {@link #cut()} says so).
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

		Card(RosterStatePayload.Row row, List<RosterStatePayload.Row> rows, int x, int width, int height, int offered) {
			super(x, 0, width, height, words(row));
			this.row = row;
			this.narrow = width < NARROW_CARD;
			// The name's room ends at the actions on its line; the lines under it end at a narrow card's column of actions.
			int nameRoom = (offered == 0 ? width - 6 : narrow ? width - ACTION_X - 3 : width - ACTION_X - 16 * (offered - 1) - 3) - TEXT_X;
			int lineRoom = (narrow && offered > 0 ? width - ACTION_X - 3 : width - 6) - TEXT_X;
			Component shownName = RosterGlance.name(row);
			Component styled = narrow ? shownName : shownName.copy().withStyle(ChatFormatting.BOLD);
			MagicArt.Mark shownMark = mark(row.icon());
			if (row.icon() == RosterStatePayload.WAITING && MagicGlance.markOf(row.waiting()) == MagicArt.Mark.ASK) {
				// Waiting on its holder's answer (a matter at its perch): the "?" of a question asked.
				shownMark = MagicArt.Mark.ASK;
			}
			this.mark = shownMark;
			Component shownState = RosterGlance.state(row, stateWord(row.icon()));
			RosterGlance.Fact shownFact = RosterGlance.fact(row, rows);
			Component shownKind = RosterGlance.kind(row);
			int factWidth = font.width(shownFact.shown());
			int kindRoom = narrow || shownKind.getString().equals(shownName.getString()) ? 0 : font.width(shownKind) + 6;
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

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
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
			for (MagicButton action : actions) {
				if (action.isMouseOver(mouseX, mouseY)) {
					// The action under the mouse shows its own tip, not the card's.
					return;
				}
			}
			MagicArt.tip(g, this, tip, null, mouseX, mouseY);
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
