package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.spirits.PactAfford;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactMenu;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StarReckoning;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Concordance Chunk D (D-04, D-05; D-S14, D-S15): the pact perch's screen, native and narrated, drawn as the magic
 * interfaces' relic scroll card (docs/features/magic-interfaces.md, variant B: the owner's kit over the animated cosmos).
 * <p>
 * While a spirit visits, it gives its three beats on a ruled scroll, a page each (a Terms control skips to his terms on
 * the first two, and a spirit whose beats this player has heard before opens on his terms), then his terms: a chip for
 * each (a glyph or item and a few words), the six standing terms as one "Standing terms" chip whose More shows their six
 * lines, every blocker as a lock line at the top, and the price pinned at the foot as item stacks, each short one marked
 * before Accept (PactAfford: display only; the server decides). At an empty perch whose owner may invite two or more
 * spirits it first lists them as cards (GC-01), each not ready with its reason drawn. Nothing is spent or agreed until
 * Accept, and the keyboard lands on the terms' first line, never on Accept.
 * <p>
 * For a kept pact the header holds its service, its state, today's work as a gauge and the periods paid ahead as sockets;
 * the body its rows (what needs doing first), the matter its spirit raises (GC-05: the question and each answer as a
 * plate, the one kept marked), Star-reckoning's patterns and Recall, and at its foot the next offering with an Offer
 * plate for each carried; Suspend or Resume, End (which asks first) and the tabs between his service and his terms. A pact
 * without its perch can be seated here or ended.
 * <p>
 * Every server sentence stays whole: it is the message of the chip, line or row that draws its glance (PactGlance), so
 * narration, hover and keyboard focus read it all. The server's answer, a refusal with its reason included, is shown at
 * the body's top with its mark (a tick or a cross) and read aloud; Stolas's subtitles draw over the screen (D-06); ending
 * asks first, the keyboard on Keep it (KeepFirstConfirmScreen). Tab and Shift+Tab move, Enter or Space presses, Esc
 * leaves. The screen fits any window down to 320x180 scaled pixels (a compact layout under 240 tall, a narrow one under
 * 360 wide), every label whole, and scrolls its body between its pinned top and foot. Nothing in it decides: each button
 * sends one bounded request through {@link PactClient}. No text box, chat command, vanilla Dialog or GuiLib.
 */
public class PactScreen extends MagicScreen implements MenuAccess<PactMenu> {
	/** The page his terms are on, after his three beats. */
	public static final int TERMS_PAGE = PactStatePayload.MAX_BEATS;
	/** The sign by the speaker's name when a state names none (the chooser) or before the first state: the perch. */
	private static final String PERCH = "jugcraft:pact_perch";
	/** The standing terms (one chip, More's six lines), in TERMS' order. */
	private static final List<String> STANDING = List.of("suspension", "duration", "breach", "ending", "returns", "perch");
	/** The glyphs of More's six lines (spec 2.5), in STANDING's order. */
	/** Where the standing terms' words start: past the widest of their glyphs (INF, 11) and a gap. */
	private static final int STANDING_COLUMN = 13;
	private static final List<MagicArt.Glyph> STANDING_GLYPHS = List.of(MagicArt.Glyph.PAUSE, MagicArt.Glyph.INF, MagicArt.Glyph.HOURGLASS,
			MagicArt.Glyph.KEY, MagicArt.Glyph.SPIRAL, MagicArt.Glyph.PIN);
	/** Rows drawn by another element (tools/magic_ui.py FOLDED): narrated there, never a row of their own. */
	private static final Set<String> FOLDED = Set.of("pact.row.area", "pact.row.area_any", "pact.row.end", "pact.row.matter.awaiting",
			"pact.row.matter.answered", "pact.term.compact.items");
	/** The chip grid's row pitch and its columns' starts, in thousandths of its width (spec 2.12). */
	private static final int PITCH = 17;
	private static final int[] COLUMNS = {0, 355, 680, 1000};
	/** The scroll's stain colour, behind the chosen pattern; the light outline of a lit card (spec 2.8). */
	private static final int STAIN = 0xFFE2BA97;
	private static final int CARD_LIGHT = 0xFFFFFCAB;
	/** The wood's brown, the price row's rule. */
	private static final int WOOD = 0xFFB17245;
	private static final int FOOTER_WIDEST = 76;
	/** The price row's height at the body's foot: its rule, then a slot. */
	private static final int PRICE_FOOT = MagicArt.SLOT + 9;
	/** A row's ink chosen by its mark (rows of their own colour pass that colour). */
	private static final int BY_MARK = 0;
	private final PactMenu menu;
	private final Runnable listener = () -> dirty = true;
	private boolean termsTab;
	/** Whether More's six standing lines are shown (kept beside termsTab, so a state packet's rebuild keeps it). */
	private boolean more;
	private boolean asked;
	/** Whether the full layout's body overflowed on the last build: this state is drawn compact. */
	private boolean crowded;
	private int page;
	private int mode = -1;
	private int pattern = -1;
	/** The last answer this screen has shown (PactClient.lastAnswer()); one heard before it opened is not new. */
	private int shownAnswer = PactClient.lastAnswer();
	// What the chrome draws, set by build.
	private Surface surface = Surface.NONE;
	private Component name = Component.empty();
	private Component tag = Component.empty();
	private int tagColour = MagicTheme.HEADING;
	/** The room the header's tag takes at its right (its words, or para:0's). */
	private int tagRoom;
	/** Whether the header's second row is his service's name, dim (beats, closed pages), rather than the service chip. */
	private boolean namedService;
	private Component serviceName = Component.empty();
	private ItemStack serviceIcon = ItemStack.EMPTY;
	private String sign = PERCH;
	private boolean liveSpirit;
	private boolean ended;
	private int pips = -1;
	private int priceRule = -1;
	private Component heading = Component.empty();
	// The body's pinned parts, while building.
	private int top;
	private int bottom;

	/** What the screen draws behind its widgets. */
	private enum Surface {
		NONE, BEAT, SCROLL, CHOOSER
	}

	public PactScreen(PactMenu menu, Inventory inventory, Component title) {
		super(title);
		this.menu = menu;
	}

	@Override
	public PactMenu getMenu() {
		return menu;
	}

	static Component ui(String key, Object... args) {
		return Component.translatable("screen.jugcraft.pact." + key, args);
	}

	@Override
	protected void init() {
		PactClient.unlisten(listener);
		PactClient.listen(listener);
		crowded = false;
		super.init();
		// Spec 2.11: the full layout only when its body holds its words; a kept or closed page that would scroll there is
		// drawn compact instead (his terms scroll between their pinned top and foot by design).
		if (!layout.compact() && mode != PactStatePayload.NEGOTIATE && body != null && body.maxScrollAmount() > 0) {
			crowded = true;
			clearWidgets();
			clearFocus();
			super.init();
		}
	}

	@Override
	protected MagicLayout layout(int width, int height) {
		return MagicLayout.pact(width, height, crowded ? Integer.MAX_VALUE : 0);
	}

	@Override
	protected boolean holdsFocus() {
		return PactClient.waiting();
	}

	@Override
	protected void build() {
		surface = Surface.NONE;
		name = Component.empty();
		tag = Component.empty();
		tagColour = MagicTheme.HEADING;
		tagRoom = 0;
		namedService = false;
		serviceName = Component.empty();
		serviceIcon = ItemStack.EMPTY;
		sign = PERCH;
		liveSpirit = false;
		ended = false;
		pips = -1;
		priceRule = -1;
		heading = Component.empty();
		top = layout.text().top();
		bottom = layout.text().bottom();
		footOffers.clear();
		PactStatePayload state = PactClient.state(menu);
		if (state != null && state.mode() != mode) {
			// Sealed, ended or seated: the new state of things starts at its first page, its service before its terms.
			mode = state.mode();
			page = 0;
			termsTab = false;
			more = false;
			asked = false;
			pattern = -1;
			scrollTo(0);
			focus = switch (mode) {
				case PactStatePayload.NEGOTIATE -> "beat";
				case PactStatePayload.INVITE -> "invite:0";
				// Spec 2.12: a kept pact opens on how it stands (the answer itself is read aloud by PactClient).
				case PactStatePayload.KEEP -> "note:0";
				default -> state.result().isPresent() ? "result" : "note:0";
			};
			if (mode == PactStatePayload.NEGOTIATE && !state.beats().isEmpty() && ConcordanceClientOptions.heardBeats(spirit(state))) {
				// Spec 1.5: his beats were heard before, so his perch opens on his terms (asked for once, as turning to them does).
				page = state.beats().size();
				focus = "para:0";
				hear();
			}
		} else if (state != null && mode == PactStatePayload.NEGOTIATE && page >= state.beats().size() && state.result().isPresent()
				&& PactClient.lastAnswer() != shownAnswer) {
			// A new answer on his terms (a refusal: sealed or declined leaves the page) is the body's first line, pinned there;
			// the keyboard leaves Accept for it, so neither Accept's tooltip nor anything else hides it.
			scrollTo(0);
			focus = "result";
		}
		shownAnswer = PactClient.lastAnswer();
		if (state != null && PactClient.takeChanged()) {
			// His terms changed under the screen: they are shown again, from their first line, before anything is accepted.
			page = state.beats().size();
			termsTab = true;
			scrollTo(0);
			focus = "para:0";
		}
		if (state == null) {
			opening();
			return;
		}
		switch (state.mode()) {
			case PactStatePayload.NEGOTIATE -> negotiate(state);
			case PactStatePayload.KEEP -> keep(state);
			case PactStatePayload.SEAT -> seat(state);
			case PactStatePayload.INVITE -> invite(state);
			default -> closed(state);
		}
	}

	private void opening() {
		surface = Surface.SCROLL;
		name = title;
		drew(name.getString());
		header(null, false);
		ScreenRectangle text = body(0);
		say(ui("opening"), ui("opening"), MagicTheme.DIM, "", text.width());
		footer(FOOTER_WIDEST, new String[] {"close"}, plate(ui("close"), ui("short.close"), this::onClose));
	}

	// ------------------------------------------------------------------------------------------------ his visit

	private void negotiate(PactStatePayload state) {
		int beats = state.beats().size();
		page = Math.min(page, beats);
		if (page < beats) {
			beat(state, beats);
			return;
		}
		surface = Surface.SCROLL;
		spiritHeader(state, true);
		tag(ui("terms"), true);
		header(state, false);
		pinTop(state);
		// The answer above his terms, at the top of the body, where no footer tooltip reaches (they open below the footer).
		result(state);
		List<AbstractWidget> blockers = notes(state, MagicTheme.NEED, PRICE_FOOT);
		ScreenRectangle text = body(PRICE_FOOT);
		unpinned(blockers);
		terms(state, text);
		priceRow(state, false);
		boolean free = !PactClient.waiting();
		MagicButton back = back(beats > 0, () -> turn(beats - 1));
		MagicButton accept = new MagicButton(MagicButton.Look.PRIMARY, ui("accept"), ui("short.accept"),
				unused -> PactClient.request(menu, PactRequestPayload.ACCEPT, 0)).hint(ui("short.accept.hint"), ui("short.accept.narration"));
		accept.active = free && state.offers(PactRequestPayload.ACCEPT);
		MagicButton decline = plate(ui("decline"), ui("short.decline"), () -> PactClient.request(menu, PactRequestPayload.DECLINE, 0))
				.hint(ui("decline.hint"), Component.empty());
		decline.active = free && state.offers(PactRequestPayload.DECLINE);
		if (layout.arrowsBeside()) {
			beside("back", back, layout.leftArrow());
			footer(FOOTER_WIDEST, new String[] {"accept", "decline"}, accept, decline);
		} else {
			footer(FOOTER_WIDEST, new String[] {"back", "accept", "decline"}, back, accept, decline);
		}
	}

	/** One of his beats on the ruled scroll: the stage direction dim on beat 1, then his line, the rules fourteen apart. */
	private void beat(PactStatePayload state, int beats) {
		surface = Surface.BEAT;
		spiritHeader(state, true);
		pips = page;
		boolean last = page + 1 >= beats;
		if (!last) {
			// Spec 1.5: beats 1 and 2 carry a Terms control at the header's right.
			MagicButton skip = new MagicButton(MagicButton.Look.LINK, ui("short.skip.hint"), ui("short.terms"), unused -> turn(beats))
					.glyph(MagicArt.Glyph.ARROW).glyphAfter().onDark(layout.compact());
			skip.setRectangle(skip.labelWidth(font), skip.getHeight(), headerRight() - skip.labelWidth(font), headerTextY() - 1);
			onScreen("skip", skip);
		}
		header(state, true);
		ScreenRectangle text = body(0);
		Component said = state.beats().get(page);
		Component stage = stage(said);
		int width = text.width() - 4;
		int at = 2;
		if (stage != null) {
			MagicText dim = new MagicText(stage, font, width, MagicTheme.DIM, theme.body()).ruled();
			dim.active = false;
			dim.setX(text.left() + 2);
			inBodyAt("", dim, at);
			at += 14 * Math.max(1, dim.getHeight() / 14);
		}
		// His line: the beat's own words after its stage direction (the whole beat stays its message).
		FormattedText line = stage == null ? said : FormattedText.of(said.getString().substring(stage.getString().length()).trim());
		MagicText words = new MagicText(said, line, font, width, MagicTheme.INK, theme.body()).ruled();
		words.setX(text.left() + 2);
		inBodyAt("beat", words, at);
		MagicButton back = back(page > 0, () -> turn(page - 1));
		MagicButton next;
		if (layout.arrowsBeside()) {
			next = new MagicButton(MagicButton.Look.ARROW_RIGHT, last ? ui("hear_terms") : ui("next"), Component.empty(), unused -> turn(page + 1));
			if (last) {
				next.caption(ui("short.terms"));
			}
			beside("back", back, layout.leftArrow());
			beside("next", next, layout.rightArrow());
			footer(FOOTER_WIDEST, new String[] {"leave"}, leave());
		} else {
			next = last ? plate(ui("hear_terms"), ui("short.terms"), () -> turn(page + 1)).glyph(MagicArt.Glyph.ARROW)
					: plate(ui("next"), Component.empty(), () -> turn(page + 1)).icon(MagicArt.PAGE_RIGHT, 14, 8);
			footer(FOOTER_WIDEST, new String[] {"back", "leave", "next"}, back, leave(), next);
		}
	}

	/** The greeting's stage direction (its own key, .stage, whose words begin the beat), or null when it has none. */
	private static @Nullable Component stage(Component beat) {
		if (beat.getContents() instanceof TranslatableContents contents) {
			Component stage = Component.translatableWithFallback(contents.getKey() + ".stage", "");
			String words = stage.getString();
			if (!words.isEmpty() && beat.getString().startsWith(words)) {
				return stage;
			}
		}
		return null;
	}

	private MagicButton leave() {
		return plate(ui("leave"), ui("short.leave"), this::onClose).hint(ui("leave.hint"), Component.empty());
	}

	/** Back to the beat before: a gold arrow beside the scroll when wide, a small plate in the footer when narrow. */
	private MagicButton back(boolean active, Runnable action) {
		MagicButton back = layout.arrowsBeside() ? new MagicButton(MagicButton.Look.ARROW_LEFT, ui("back"), Component.empty(), unused -> action.run())
				: plate(ui("back"), Component.empty(), action).icon(MagicArt.PAGE_LEFT, 14, 8);
		back.active = active;
		return back;
	}

	/** His terms (D-S15, amended: a chip each; the six standing terms one chip, or More's six lines; every one narrated whole). */
	private void terms(PactStatePayload state, ScreenRectangle text) {
		int at = this.at;
		if (more) {
			for (int i = 0; i < STANDING.size(); i++) {
				PactStatePayload.Term term = term(state, STANDING.get(i));
				if (term != null) {
					MagicText line = new MagicText(term.value(), PactGlance.of(term.value()).shown(), font, text.width(), MagicTheme.INK, theme.body())
							.lead(STANDING_GLYPHS.get(i)).column(STANDING_COLUMN).maxLines(2);
					line.setX(text.left());
					inBodyAt("term:" + term.key(), line, at);
					// Spec 2.12 (B-terms-buer-more.png): the six lines a line apart, their words on one column.
					at += line.getHeight() - 2;
				}
			}
			at += 8;
		}
		List<AbstractWidget> chips = new ArrayList<>();
		List<String> keys = new ArrayList<>();
		for (PactStatePayload.Term term : state.terms()) {
			if (term.key().equals("service") || term.key().equals("compact") || STANDING.contains(term.key())) {
				continue;
			}
			chips.add(chip(state, term, text.width()));
			keys.add("term:" + term.key());
			if (term.key().equals("beneficiaries") && !more) {
				AbstractWidget standing = standing(state);
				if (standing != null) {
					chips.add(standing);
					keys.add("term:standing");
				}
			}
		}
		grid(chips, keys, at, text);
	}

	/** The term under {@code key} in {@code state}, or null. */
	private static PactStatePayload.@Nullable Term term(PactStatePayload state, String key) {
		for (PactStatePayload.Term term : state.terms()) {
			if (term.key().equals(key)) {
				return term;
			}
		}
		return null;
	}

	/** A term's chip: its glyph or stack and its glance; its message the server's sentence (a line of it whole when it has no glance). */
	private AbstractWidget chip(PactStatePayload state, PactStatePayload.Term term, int width) {
		Component full = term.value();
		PactGlance.Glance glance = PactGlance.of(full);
		if (!glance.isShort()) {
			return new MagicText(full, font, width, MagicTheme.INK, theme.body()).maxLines(2);
		}
		String server = PactGlance.server(full);
		MagicChip chip = new MagicChip(full, glance.shown(), font, MagicTheme.INK, theme.body());
		switch (term.key()) {
			case "hours" -> chip.glyph(server.endsWith(".any") ? MagicArt.Glyph.CLOCK : MagicArt.Glyph.SUN);
			case "area" -> chip.glyph(MagicArt.Glyph.RING);
			case "quota" -> chip.glyph(MagicArt.Glyph.CALENDAR);
			case "grace" -> chip.glyph(MagicArt.Glyph.HOURGLASS);
			case "beneficiaries" -> chip.glyph(server.endsWith(".party") ? MagicArt.Glyph.PARTY : MagicArt.Glyph.PERSON);
			case "upkeep" -> {
				// Spec 2.6: the offering's stack, its tag's items in turn with "+N", and its period and cap as one chip.
				PactGlance.Glance cap = PactGlance.of(full, "cap");
				MagicChip upkeep = new MagicChip(full, Component.empty().append(glance.shown()).append(" · ").append(cap.shown()), font,
						MagicTheme.INK, theme.body());
				return offering(upkeep, full, 0, 1);
			}
			default -> {
			}
		}
		return chip;
	}

	/**
	 * A chip of an offering ({@code full}'s argument {@code tagArgument} names its tag, {@code countArgument} its count):
	 * its tag's items cycling with their count or "+N", and the whole tag listed under its tip.
	 */
	private MagicChip offering(MagicChip chip, Component full, int countArgument, int tagArgument) {
		List<ItemStack> items = List.of();
		int count = 1;
		if (full.getContents() instanceof TranslatableContents contents) {
			Object[] args = contents.getArgs();
			if (args.length > tagArgument && args[tagArgument] instanceof Component tag
					&& tag.getContents() instanceof TranslatableContents named && named.getFallback() != null) {
				items = MagicStacks.tag(named.getFallback());
			}
			if (args.length > countArgument && args[countArgument] instanceof Number number) {
				count = number.intValue();
			}
		}
		if (items.isEmpty()) {
			return chip;
		}
		chip.items(items).count(count);
		if (items.size() > 1) {
			chip.tipLine(ui("short.any_of", ComponentUtils.formatList(items, ItemStack::getHoverName)));
		}
		return chip;
	}

	/** The six standing terms as one chip (D-S15, amended): its message all six sentences, its tip "The same for every spirit". */
	private @Nullable AbstractWidget standing(PactStatePayload state) {
		MutableComponent all = Component.empty();
		boolean any = false;
		for (String key : STANDING) {
			PactStatePayload.Term term = term(state, key);
			if (term != null) {
				if (any) {
					all.append(". ");
				}
				all.append(term.value());
				any = true;
			}
		}
		return any ? new MagicChip(all, ui("short.standing"), font, MagicTheme.INK, theme.body()).glyph(MagicArt.Glyph.SCROLL)
				.tip(ui("short.standing.hint")) : null;
	}

	/** Lays chips in the scroll's three columns, each as many columns wide as its words need, rows {@link #PITCH} apart. */
	private void grid(List<AbstractWidget> chips, List<String> keys, int at, ScreenRectangle text) {
		int width = text.width();
		int column = 0;
		int row = at;
		for (int i = 0; i < chips.size(); i++) {
			AbstractWidget chip = chips.get(i);
			int end = span(column, chip.getWidth(), width);
			if (column > 0 && start(end, width) - start(column, width) < chip.getWidth()) {
				row += PITCH;
				column = 0;
				end = span(0, chip.getWidth(), width);
			}
			chip.setX(text.left() + start(column, width));
			inBodyAt(keys.get(i), chip, row + (chip.getHeight() >= 16 || chip instanceof MagicText ? 0 : 4));
			int tall = chip instanceof MagicText ? chip.getHeight() + 2 : PITCH;
			column = end;
			if (column >= 3) {
				column = 0;
				row += Math.max(PITCH, tall);
			}
		}
		this.at = row + (column > 0 ? PITCH : 0);
	}

	private static int start(int column, int width) {
		return width * COLUMNS[Math.min(3, column)] / 1000;
	}

	/** The column after the last a chip {@code need} wide takes from {@code column}. */
	private static int span(int column, int need, int width) {
		int end = column + 1;
		while (end < 3 && start(end, width) - start(column, width) - 6 < need) {
			end++;
		}
		return end;
	}

	/**
	 * Every note (D-04's blockers: each keeps its action; the whole sentence its message) as a line at the body's top, in
	 * {@code colour}: pinned there when they leave the scroll its room above a foot {@code foot} tall, else returned to be
	 * the scroll's first lines (unpinned), so none is ever hidden.
	 */
	private List<AbstractWidget> notes(PactStatePayload state, int colour, int foot) {
		List<AbstractWidget> lines = new ArrayList<>();
		int need = 0;
		for (int i = 0; i < state.notes().size(); i++) {
			MagicText line = line(state, state.notes().get(i), colour, layout.text().width());
			lines.add(line);
			need += line.getHeight() + 1;
		}
		if (top + need <= layout.text().bottom() - foot - 3 - 24) {
			for (int i = 0; i < lines.size(); i++) {
				lines.get(i).setPosition(layout.text().left(), top);
				onScreen("note:" + i, lines.get(i));
				top += lines.get(i).getHeight() + 1;
			}
			return List.of();
		}
		return lines;
	}

	/** The notes that did not fit pinned, as the scroll's first lines. */
	private void unpinned(List<AbstractWidget> lines) {
		for (int i = 0; i < lines.size(); i++) {
			lines.get(i).setX(layout.text().left());
			inBody("note:" + i, lines.get(i), 1);
		}
		if (!lines.isEmpty()) {
			at += 3;
		}
	}

	// ------------------------------------------------------------------------------------------------ his pact

	private void keep(PactStatePayload state) {
		surface = Surface.SCROLL;
		spiritHeader(state, true);
		tag(termsTab ? ui("terms") : ui("tab.service"), termsTab);
		Rows rows = rows(state);
		header(state, false, rows);
		pinTop(state, rows);
		result(state);
		if (termsTab) {
			ScreenRectangle text = body(PRICE_FOOT);
			terms(state, text);
			priceRow(state, true);
		} else {
			boolean foot = rows.next() >= 0 || !state.offerings().isEmpty();
			boolean offersBelow = offersFit(state);
			ScreenRectangle text = body(foot ? MagicArt.PLATE_HEIGHT + 3 : 0);
			for (int i : rows.needs()) {
				row(state, i);
			}
			// GC-05: the matter its spirit raises, asked before its service and offerings.
			matter(state, rows, text);
			for (int i : rows.others()) {
				row(state, i);
			}
			if (reckons(state)) {
				service(state, text);
			} else {
				workService(state, text);
			}
			if (!offersBelow) {
				offerings(state, text);
			}
			upkeepRow(state, rows, offersBelow);
		}
		boolean free = !PactClient.waiting();
		List<String> keys = new ArrayList<>();
		List<MagicButton> buttons = new ArrayList<>();
		MagicButton service = new MagicButton(MagicButton.Look.BOOKMARK_FAMILIAR, ui("tab.service"), Component.empty(), unused -> showTerms(false))
				.selected(!termsTab);
		MagicButton terms = new MagicButton(MagicButton.Look.BOOKMARK_DOCUMENTATION, ui("tab.terms"), Component.empty(), unused -> showTerms(true))
				.selected(termsTab);
		if (layout.arrowsBeside()) {
			ScreenRectangle scroll = layout.body();
			beside("tab:service", service, new ScreenRectangle(scroll.right() - 4, scroll.top() + 14, 23, 20));
			beside("tab:terms", terms, new ScreenRectangle(scroll.right() - 4, scroll.top() + 36, 23, 20));
		} else {
			keys.add("tab:service");
			buttons.add(service);
			keys.add("tab:terms");
			buttons.add(terms);
		}
		if (state.offers(PactRequestPayload.RESUME)) {
			MagicButton resume = new MagicButton(MagicButton.Look.PRIMARY, ui("resume"), ui("short.resume"),
					unused -> PactClient.request(menu, PactRequestPayload.RESUME, 0)).hint(ui("resume.hint"), Component.empty());
			resume.active = free;
			keys.add("resume");
			buttons.add(resume);
		} else if (state.offers(PactRequestPayload.SUSPEND)) {
			MagicButton suspend = plate(ui("suspend"), ui("short.suspend"), () -> PactClient.request(menu, PactRequestPayload.SUSPEND, 0))
					.glyph(MagicArt.Glyph.PAUSE).hint(ui("suspend.hint"), Component.empty());
			suspend.active = free;
			keys.add("suspend");
			buttons.add(suspend);
		}
		MagicButton end = plate(ui("end"), ui("short.end"), this::confirmEnd).glyph(MagicArt.Glyph.CROSS)
				.hint(ui("end.hint"), rows.end() >= 0 ? state.notes().get(rows.end()) : Component.empty());
		end.active = free && state.offers(PactRequestPayload.END);
		keys.add("end");
		buttons.add(end);
		keys.add("close");
		buttons.add(plate(ui("close"), ui("short.close"), this::onClose));
		footer(FOOTER_WIDEST - 10, keys.toArray(String[]::new), buttons.toArray(MagicButton[]::new));
	}

	/**
	 * A kept page's rows by where they are drawn (spec 1.7, 2.12): note:0 the state (the header), the work's service row
	 * (today's gauge), the escrow (the sockets), the next offering (the body's foot), the folded ones (narrated by the
	 * element that draws them), and the rest in the body: what needs doing first, then the others.
	 */
	private record Rows(int state, int gauge, int escrow, int next, int area, int end, int matter, List<Integer> needs, List<Integer> others) {
	}

	private Rows rows(PactStatePayload state) {
		int gauge = -1;
		int escrow = -1;
		int next = -1;
		int area = -1;
		int end = -1;
		int matter = -1;
		List<Integer> needs = new ArrayList<>();
		List<Integer> others = new ArrayList<>();
		List<Component> notes = state.notes();
		for (int i = 0; i < notes.size(); i++) {
			String server = PactGlance.server(notes.get(i));
			if (i == 0 && state.mode() == PactStatePayload.KEEP) {
				continue;
			}
			if (server.endsWith(".row.service") && gauge < 0) {
				gauge = i;
			} else if (server.equals("pact.row.escrow")) {
				escrow = i;
			} else if (server.equals("pact.row.next")) {
				next = i;
			} else if (server.equals("pact.row.area") || server.equals("pact.row.area_any")) {
				area = i;
			} else if (server.equals("pact.row.end")) {
				end = i;
			} else if (server.startsWith("pact.row.matter.")) {
				matter = i;
			} else if (FOLDED.contains(server)) {
				continue;
			} else {
				MagicArt.Mark mark = PactGlance.mark(server);
				(mark == MagicArt.Mark.HOURGLASS || mark == MagicArt.Mark.LOCK ? needs : others).add(i);
			}
		}
		return new Rows(state.mode() == PactStatePayload.KEEP && !notes.isEmpty() ? 0 : -1, gauge, escrow, next, area, end, matter, needs, others);
	}

	/**
	 * GC-05 (GC-S11): the matter its spirit raises (the state's spirit.matter, a narrated line with its "?" mark; its
	 * message the question and how it stands) and its answers (spirit.answers, the safe course first), each a plate sending
	 * RESOLVE with its index; the answer kept is the primary plate with its tick, and each says in its tip what choosing it
	 * means. Nothing is spent, an answer may be changed at any time, and the server decides.
	 */
	private void matter(PactStatePayload state, Rows rows, ScreenRectangle text) {
		List<PactStatePayload.Choice> answers = state.spirit().answers();
		if (answers.isEmpty()) {
			return;
		}
		Component asked = state.spirit().matter();
		Component whole = rows.matter() < 0 ? asked : Component.empty().append(asked).append(". ").append(state.notes().get(rows.matter()));
		inBody("matter", new MagicText(whole, PactGlance.of(asked).shown(), font, text.width(), MagicTheme.INK, theme.body())
				.lead(MagicArt.Mark.ASK).maxLines(2), 2).setX(text.left());
		boolean answered = answers.stream().anyMatch(answer -> answer.amount() > 0);
		boolean free = !PactClient.waiting();
		int x = text.left() + 11;
		int row = at;
		for (int i = 0; i < answers.size(); i++) {
			PactStatePayload.Choice answer = answers.get(i);
			boolean kept = answer.amount() > 0;
			int index = i;
			MagicButton plate = new MagicButton(MagicButton.Look.PLATE, answer.name(), answer.name(),
					unused -> PactClient.request(menu, PactRequestPayload.RESOLVE, index)).selected(kept)
					.hint(kept ? ui("matter.kept") : !answered && i == 0 ? ui("matter.safe") : ui("matter.choose"), Component.empty());
			plate.active = free && answer.ready() && state.offers(PactRequestPayload.RESOLVE);
			int width = Math.min(text.right() - x, Math.max(56, plate.labelWidth(font) + 4));
			if (x > text.left() + 11 && x + width > text.right()) {
				x = text.left() + 11;
				row += MagicArt.PLATE_HEIGHT + 3;
			}
			plate.setRectangle(width, MagicArt.PLATE_HEIGHT, x, 0);
			inBodyAt("answer:" + i, plate, row);
			x += width + 6;
		}
		at = row + MagicArt.PLATE_HEIGHT + 5;
	}

	/** Star-reckoning (D-S1): a pattern the holder observed, chosen here (its row), then Recall with its cost in its tip. */
	private void service(PactStatePayload state, ScreenRectangle text) {
		List<PactStatePayload.Choice> patterns = state.patterns();
		if (patterns.isEmpty()) {
			say(ui("no_patterns"), ui("no_patterns"), MagicTheme.DIM, "", text.width());
			return;
		}
		pattern = Math.min(pattern, patterns.size() - 1);
		int rowWidth = Math.min(text.width() - 70, 170);
		int first = at;
		for (int i = 0; i < patterns.size(); i++) {
			PactStatePayload.Choice choice = patterns.get(i);
			int index = i;
			Component hint = !choice.ready() ? ui("pattern.claimed") : i == pattern ? ui("pattern.chosen", choice.amount()) : ui("pattern.cost", choice.amount());
			PatternRow row = new PatternRow(choice, i == pattern, unused -> {
				pattern = index;
				dirty = true;
			});
			row.hint(hint, Component.empty());
			row.setRectangle(rowWidth, 11, text.left(), 0);
			inBodyAt("pattern:" + i, row, at);
			at += 12;
		}
		PactStatePayload.Choice chosen = pattern >= 0 ? patterns.get(pattern) : null;
		MagicButton recall = new MagicButton(MagicButton.Look.PRIMARY, chosen == null ? ui("recall.none") : ui("recall", chosen.name()), ui("short.recall"),
				unused -> PactClient.request(menu, PactRequestPayload.RECALL, pattern))
				.hint(chosen == null ? ui("recall.none") : ui("pattern.cost", chosen.amount()), Component.empty());
		recall.active = chosen != null && chosen.ready() && !PactClient.waiting() && state.offers(PactRequestPayload.RECALL);
		int width = Math.max(56, recall.labelWidth(font) + 6);
		recall.setRectangle(width, MagicArt.PLATE_HEIGHT, Math.min(text.right() - width, text.left() + rowWidth + 8), 0);
		inBodyAt("recall", recall, first + Math.max(0, (at - first - MagicArt.PLATE_HEIGHT) / 2));
		at = Math.max(at, first + MagicArt.PLATE_HEIGHT) + 4;
	}

	/** Whether a kept pact's service is Star-reckoning (its section as built; a state that names no work is one from before GC-01). */
	private static boolean reckons(PactStatePayload state) {
		String work = state.spirit().work();
		return work.isEmpty() || StarReckoning.WORK.equals(work);
	}

	/**
	 * GC-01 (SG5): another work's service is drawn by its rows (the header's gauge, the body's lines), its words the
	 * server's. GC-06 (GC-P74): then each choice of its on-request action (Marax's reading), a plate sending RECALL with its
	 * index.
	 */
	private void workService(PactStatePayload state, ScreenRectangle text) {
		boolean free = !PactClient.waiting() && state.offers(PactRequestPayload.RECALL);
		for (int i = 0; i < state.patterns().size(); i++) {
			PactStatePayload.Choice choice = state.patterns().get(i);
			int index = i;
			MagicButton work = plate(choice.name(), choice.name(), () -> PactClient.request(menu, PactRequestPayload.RECALL, index))
					.hint(choice.ready() ? ui("work.left", choice.amount()) : ui("work.spent"), Component.empty());
			work.active = free && choice.ready();
			work.setRectangle(Math.min(text.width(), Math.max(56, work.labelWidth(font) + 6)), MagicArt.PLATE_HEIGHT, text.left(), 0);
			inBody("work:" + i, work, 3);
		}
	}

	/**
	 * The kept page's foot (spec 2.12): the next offering (its stack and "Next in N days") and, when they fit beside it, an
	 * Offer plate with its count carried for each offering the holder carries (else they are rows of the body).
	 */
	private void upkeepRow(PactStatePayload state, Rows rows, boolean offersBelow) {
		ScreenRectangle text = layout.text();
		int y = text.bottom() - MagicArt.PLATE_HEIGHT;
		if (rows.next() >= 0) {
			Component full = state.notes().get(rows.next());
			MagicChip next = offering(new MagicChip(full, PactGlance.of(full).shown(), font, MagicTheme.INK, theme.body()), full, 0, 1);
			next.setPosition(text.left(), y + (MagicArt.PLATE_HEIGHT - next.getHeight()) / 2);
			onScreen("note:" + rows.next(), next);
		}
		if (!offersBelow) {
			return;
		}
		int x = text.right() - footWidth();
		for (int i = 0; i < footOffers.size(); i++) {
			MagicButton plate = footOffers.get(i);
			plate.setPosition(x, y);
			onScreen("offer:" + i, plate);
			x += plate.getWidth() + 4;
			MagicChip carried = carried(state, i);
			carried.setPosition(x, y + (MagicArt.PLATE_HEIGHT - carried.getHeight()) / 2);
			onScreen("", carried);
			x += carried.getWidth() + 8;
		}
	}

	/** The Offer plates measured for the foot (each followed by its count carried). */
	private final List<MagicButton> footOffers = new ArrayList<>();

	/** Whether every Offer plate fits in the foot beside the next offering's chip (measured here, placed by upkeepRow). */
	private boolean offersFit(PactStatePayload state) {
		footOffers.clear();
		for (int i = 0; i < state.offerings().size(); i++) {
			footOffers.add(offer(state, i));
		}
		return !footOffers.isEmpty() && footWidth() <= layout.text().width() - 110;
	}

	private int footWidth() {
		PactStatePayload state = PactClient.state(menu);
		int width = 0;
		for (int i = 0; i < footOffers.size(); i++) {
			width += footOffers.get(i).getWidth() + 4 + (state == null ? 0 : carried(state, i).getWidth()) + 8;
		}
		return Math.max(0, width - 8);
	}

	/** "×N carried" after an Offer plate: its count, drawn dim; the plate's message says it too. */
	private MagicChip carried(PactStatePayload state, int index) {
		Component count = ui("short.carried", state.offerings().get(index).amount());
		return new MagicChip(count, count, font, MagicTheme.DIM, theme.body()).quiet();
	}

	/** Upkeep (D-S3) in the body, when the foot has no room: each offering the holder carries, its plate and its count carried. */
	private void offerings(PactStatePayload state, ScreenRectangle text) {
		for (int i = 0; i < state.offerings().size(); i++) {
			MagicButton plate = offer(state, i);
			plate.setX(text.left());
			inBodyAt("offer:" + i, plate, at);
			MagicChip carried = carried(state, i);
			carried.setX(text.left() + plate.getWidth() + 4);
			inBodyAt("", carried, at + (MagicArt.PLATE_HEIGHT - carried.getHeight()) / 2);
			at += MagicArt.PLATE_HEIGHT + 3;
		}
	}

	/** An Offer plate (D-S6: paid ahead by the server, never a slot): the offering's icon and "Offer"; its message the whole label. */
	private MagicButton offer(PactStatePayload state, int index) {
		PactStatePayload.Choice choice = state.offerings().get(index);
		MagicButton plate = plate(ui("offer", choice.name(), choice.amount()), ui("short.offer"),
				() -> PactClient.request(menu, PactRequestPayload.DEPOSIT, index)).icon(PactGlance.item(choice.id())).hint(ui("offer.hint"), Component.empty());
		plate.active = choice.ready() && !PactClient.waiting() && state.offers(PactRequestPayload.DEPOSIT);
		plate.setWidth(Math.max(48, plate.labelWidth(font) + 4));
		return plate;
	}

	// ------------------------------------------------------------------------------------------------ the other pages

	private void seat(PactStatePayload state) {
		surface = Surface.SCROLL;
		spiritHeader(state, false);
		tag(ui("without_perch"), false);
		header(state, false);
		pinTop(state);
		result(state);
		List<AbstractWidget> lines = notes(state, MagicTheme.NEED, 0);
		ScreenRectangle text = body(0);
		unpinned(lines);
		// GC-01 (GB4): with two or more pacts without their perch, which one these terms are; choosing another reads its terms (CHOOSE).
		if (state.patterns().size() > 1) {
			pattern = Math.max(0, Math.min(pattern, state.patterns().size() - 1));
			int x = text.left();
			int row = at;
			for (int i = 0; i < state.patterns().size(); i++) {
				PactStatePayload.Choice choice = state.patterns().get(i);
				int index = i;
				Stranded card = new Stranded(choice, i == pattern, unused -> {
					pattern = index;
					PactClient.request(menu, PactRequestPayload.CHOOSE, index);
				});
				if (x > text.left() && x + card.getWidth() > text.right()) {
					x = text.left();
					row += card.getHeight() + 4;
				}
				card.setX(x);
				inBodyAt("stranded:" + i, card, row);
				x += card.getWidth() + 8;
			}
			at = row + 47;
		}
		terms(state, text);
		boolean free = !PactClient.waiting();
		MagicButton seat = new MagicButton(MagicButton.Look.PRIMARY, ui("seat"), ui("short.seat"),
				unused -> PactClient.request(menu, PactRequestPayload.SEAT, endChoice())).hint(ui("seat.hint"), Component.empty());
		seat.active = free && state.offers(PactRequestPayload.SEAT);
		MagicButton end = plate(ui("end"), ui("short.end"), this::confirmEnd).glyph(MagicArt.Glyph.CROSS).hint(ui("end.hint"), Component.empty());
		end.active = free && state.offers(PactRequestPayload.END);
		footer(FOOTER_WIDEST, new String[] {"seat", "end", "close"}, seat, end, plate(ui("close"), ui("short.close"), this::onClose));
	}

	/**
	 * GC-01 (SG4): the chooser: each spirit its owner may invite, a card each (its portrait, its name on a plate, and ready
	 * or not yet, a reason of at most four words under each not ready); choosing one sends INVITE with its index, and its
	 * visit begins: its beats, then its terms.
	 */
	private void invite(PactStatePayload state) {
		surface = Surface.CHOOSER;
		heading = ui("short.invite").withStyle(ChatFormatting.BOLD);
		drew(heading.getString());
		ScreenRectangle footer = layout.footer();
		int titleY = titleY();
		// The cards start 18 under the title; the scroll starts 3 above them, room for the focused card's outline.
		ScreenRectangle area = new ScreenRectangle(8, titleY + 15, Math.max(40, width - 16), Math.max(24, footer.top() - 4 - titleY - 15));
		openBody(area);
		at = 0;
		if (PactClient.waiting() || state.result().isPresent()) {
			result(state);
			if (controls.get("result") != null) {
				controls.get("result").setX(area.left() + Math.max(0, (area.width() - controls.get("result").getWidth()) / 2));
			}
		}
		boolean free = !PactClient.waiting();
		int step = width < MagicLayout.WIDE_FROM ? 96 : 110;
		int perRow = Math.max(1, Math.min(state.patterns().size(), area.width() / step));
		int notes = 0;
		int rowTop = at + 3;
		for (int i = 0; i < state.patterns().size(); i++) {
			PactStatePayload.Choice choice = state.patterns().get(i);
			Component reason = Component.empty();
			if (!choice.ready() && notes < state.notes().size()) {
				reason = PactGlance.card(state.notes().get(notes++));
			}
			int index = i;
			Card card = new Card(choice, reason, step - 4, unused -> PactClient.request(menu, PactRequestPayload.INVITE, index));
			card.active = free && state.offers(PactRequestPayload.INVITE);
			int column = i % perRow;
			int inRow = Math.min(perRow, state.patterns().size() - i + column);
			if (column == 0 && i > 0) {
				rowTop += card.getHeight() + 6;
			}
			card.setX(area.left() + (area.width() - inRow * step) / 2 + column * step + 2);
			inBodyAt("invite:" + i, card, rowTop);
		}
		body.ball(MagicArt.SCROLL_BALL, area.right() - 12, 12);
		footer(FOOTER_WIDEST, new String[] {"close"}, plate(ui("close"), ui("short.close"), this::onClose));
	}

	private int titleY() {
		return layout.compact() ? 4 : 10 + (height - MagicLayout.FULL_FROM) / 2;
	}

	/** Nothing the reader can do here (the notes say why): the pact ended here, another's pact, the Concordance off. */
	private void closed(PactStatePayload state) {
		surface = Surface.SCROLL;
		spiritHeader(state, false);
		for (Component note : state.notes()) {
			if (PactGlance.server(note).equals("pact.ended_here")) {
				ended = true;
			}
		}
		if (ended) {
			tag(ui("short.ended"), false);
			tagColour = MagicTheme.DIM;
		}
		header(state, true);
		ScreenRectangle text = body(0);
		at = layout.compact() ? 0 : 18;
		for (int i = 0; i < state.notes().size(); i++) {
			inRow(state, i, BY_MARK);
		}
		at += 4;
		PactStatePayload.Result departed = state.result().filter(result -> result.done() && PactGlance.server(result.message()).equals("pact.departed"))
				.orElse(null);
		if (departed != null && !PactClient.waiting()) {
			// Spec 2.12 (B-ended-buer.png): who departed and what came back is a line in ink with what returns, not a verdict.
			MagicText line = line(state, departed.message(), MagicTheme.INK, layout.text().width());
			line.setX(layout.text().left());
			inBody("result", line, 4);
		} else {
			result(state);
		}
		footer(FOOTER_WIDEST, new String[] {"close"}, plate(ui("close"), ui("short.close"), this::onClose));
	}

	// ------------------------------------------------------------------------------------------------ the header

	/** The spirit this state is about: his name, sign and service, and whether the real one stands at this perch. */
	private void spiritHeader(PactStatePayload state, boolean live) {
		name = state.speaker();
		sign = sign(state);
		liveSpirit = live;
		String work = state.spirit().work();
		serviceIcon = PactGlance.workIcon(work, state.spirit().sign());
		serviceName = Component.translatableWithFallback(PactGlance.key("pact.term.service." + work, "name"), work);
		drew(name.getString());
	}

	/** GC-01 (GB8, SG5): the spirit's own sign, as the server sent it (PactSpirits); the perch's when it sent none. */
	private static String sign(PactStatePayload state) {
		return state.spirit().sign().isEmpty() ? PERCH : state.spirit().sign();
	}

	/** The tag at the header's right: "His terms" is para:0, the page's first line; any other is the chrome's. */
	private void tag(Component words, boolean first) {
		tag = words;
		tagRoom = font.width(words) + 8;
		if (first) {
			boolean dark = layout.compact();
			MagicText his = new MagicText(words, font, font.width(words) + 4, dark ? MagicTheme.CREAM : MagicTheme.HEADING, dark ? theme.chrome() : theme.body());
			his.setPosition(headerRight() - font.width(words) - 1, headerTextY() - 1);
			onScreen("para:0", his);
			tag = Component.empty();
		} else {
			drew(words.getString());
		}
	}

	/** The header's right edge for its tag, and its first row's text top. */
	private int headerRight() {
		ScreenRectangle header = layout.header();
		return layout.compact() ? header.right() - 12 : header.right() - 26;
	}

	private int headerTextY() {
		return layout.header().top() + (layout.compact() ? 6 : 19);
	}

	/**
	 * The header's second and third rows in the full layout: his service (the service chip, term:service; or on beats and
	 * closed pages his service's name, dim) and, on a kept page, his state, today's work and the sockets.
	 */
	private void header(@Nullable PactStatePayload state, boolean named) {
		header(state, named, null);
	}

	private void header(@Nullable PactStatePayload state, boolean named, @Nullable Rows rows) {
		if (state == null) {
			return;
		}
		if (named) {
			namedService = true;
			if (!layout.compact()) {
				drew(serviceName.getString());
			}
			return;
		}
		if (layout.compact()) {
			return;
		}
		ScreenRectangle header = layout.header();
		int x = header.left() + 56;
		AbstractWidget service = serviceChip(state, rows, header.right() - 26 - x);
		if (service != null) {
			service.setPosition(x, header.top() + 29);
			onScreen("term:service", service);
		}
		if (rows != null && rows.state() >= 0) {
			stateRow(state, rows, x, header.top() + 48, header.right() - 26);
			if (rows.escrow() >= 0) {
				Sockets sockets = sockets(state, rows.escrow());
				if (sockets != null) {
					sockets.setPosition(header.left() + 24 - sockets.getWidth() / 2, header.top() + 63);
					onScreen("note:" + rows.escrow(), sockets);
				}
			}
		}
	}

	/** The service chip: his work's icon and its service line; its message the whole service sentence (and on a kept page where it works). */
	private @Nullable AbstractWidget serviceChip(PactStatePayload state, @Nullable Rows rows, int width) {
		PactStatePayload.Term term = term(state, "service");
		if (term == null) {
			return null;
		}
		Component full = term.value();
		if (rows != null && rows.area() >= 0) {
			full = Component.empty().append(full).append(". ").append(state.notes().get(rows.area()));
		}
		PactGlance.Glance glance = PactGlance.of(term.value());
		if (!glance.isShort()) {
			return new MagicText(full, font, width, MagicTheme.INK, theme.body()).maxLines(1);
		}
		MagicChip chip = new MagicChip(full, glance.shown(), font, MagicTheme.INK, theme.body()).tipWidth(MagicChip.WIDE_TIP);
		return serviceIcon.isEmpty() ? chip : chip.items(List.of(serviceIcon));
	}

	/** A kept page's state (note:0: its mark and word) and today's work (the gauge, or Stolas's recalls left); returns its right edge. */
	private int stateRow(PactStatePayload state, Rows rows, int x, int y, int right) {
		Component full = state.notes().get(rows.state());
		PactGlance.Glance glance = PactGlance.of(full);
		// A state without a glance (one this version does not know) is its whole sentence on one line, cut, its tip whole.
		AbstractWidget mark = glance.isShort() ? MagicChip.of(glance, font, colour(glance.mark()), theme.body())
				: new MagicText(full, font, Math.max(20, right - x), colour(glance.mark()), theme.body()).maxLines(1);
		mark.setPosition(x, y);
		onScreen("note:" + rows.state(), mark);
		int next = x + mark.getWidth() + 12;
		if (rows.gauge() >= 0 && glance.isShort()) {
			AbstractWidget work = work(state, rows.gauge(), layout.compact() ? 70 : 92);
			work.setPosition(next, y);
			onScreen("note:" + rows.gauge(), work);
			next += work.getWidth();
		}
		return next;
	}

	/** Today's work: a gauge of done of max ("5/16 today") when the row's first two arguments are numbers, else its glance with a calendar. */
	private AbstractWidget work(PactStatePayload state, int index, int gauge) {
		Component full = state.notes().get(index);
		PactGlance.Glance glance = PactGlance.of(full);
		String server = PactGlance.server(full);
		if (!server.equals("pact.row.service") && full.getContents() instanceof TranslatableContents contents && contents.getArgs().length >= 2
				&& contents.getArgs()[0] instanceof Number done && contents.getArgs()[1] instanceof Number max) {
			return new Gauge(full, glance.shown(), done.intValue(), max.intValue(), gauge, font);
		}
		return new MagicChip(full, glance.shown(), font, MagicTheme.INK, theme.body()).glyph(MagicArt.Glyph.CALENDAR);
	}

	/** The periods paid ahead as sockets (row.escrow's held of cap), or null when its arguments are not numbers. */
	private @Nullable Sockets sockets(PactStatePayload state, int index) {
		Component full = state.notes().get(index);
		if (full.getContents() instanceof TranslatableContents contents && contents.getArgs().length >= 2
				&& contents.getArgs()[0] instanceof Number held && contents.getArgs()[1] instanceof Number cap) {
			return new Sockets(full, held.intValue(), cap.intValue());
		}
		return null;
	}

	/** The colour of a row with {@code mark}: green for a tick, the needs-action ink for an hourglass or lock, dim for the moon. */
	private static int colour(MagicArt.@Nullable Mark mark) {
		if (mark == null) {
			return MagicTheme.INK;
		}
		return switch (mark) {
			case TICK -> MagicTheme.GREEN;
			case HOURGLASS, LOCK -> MagicTheme.NEED;
			case CROSS -> MagicTheme.RED;
			case MOON -> MagicTheme.DIM;
			case ASK -> MagicTheme.INK;
		};
	}

	/**
	 * In the compact layout the header is a plate row (spec 2.11): his service line and, on a kept page, his state, today's
	 * work and what is paid ahead move to the body's first lines, pinned there.
	 */
	private void pinTop(PactStatePayload state) {
		pinTop(state, null);
	}

	private void pinTop(PactStatePayload state, @Nullable Rows rows) {
		ScreenRectangle text = layout.text();
		top = text.top();
		if (!layout.compact()) {
			return;
		}
		AbstractWidget service = serviceChip(state, rows, text.width());
		if (service != null) {
			service.setPosition(text.left(), top);
			onScreen("term:service", service);
			top += service.getHeight() + 2;
		}
		if (rows != null && rows.state() >= 0) {
			stateRow(state, rows, text.left(), top, text.right());
			if (rows.escrow() >= 0) {
				Component full = state.notes().get(rows.escrow());
				MagicChip ahead = new MagicChip(full, PactGlance.of(full).shown(), font, MagicTheme.DIM, theme.body());
				ahead.setPosition(text.right() - ahead.getWidth(), top);
				onScreen("note:" + rows.escrow(), ahead);
			}
			top += 14;
		}
	}

	// ------------------------------------------------------------------------------------------------ the body

	/**
	 * Opens the body's scroll between what is pinned at its top (from {@code top}) and a foot {@code foot} tall at its
	 * bottom; the scroll's gold ball on its right roller shows when it scrolls. Returns its text area.
	 */
	private ScreenRectangle body(int foot) {
		ScreenRectangle text = layout.text();
		int from = Math.max(text.top(), top);
		bottom = text.bottom() - foot;
		ScreenRectangle area = new ScreenRectangle(text.left(), from, text.width(), Math.max(24, bottom - from - (foot > 0 ? 3 : 0)));
		openBody(area).ball(MagicArt.SCROLL_BALL, layout.body().right() - 18, 12);
		at = 0;
		return new ScreenRectangle(text.left(), area.top(), text.width(), area.height());
	}

	/**
	 * The price row pinned at the body's foot (spec 2.12): "Price" (term:compact, its message the whole compact), the
	 * compact's stacks, each one the player is short of marked (PactAfford's count: display only), the resonance (boxed when
	 * short), and More at the right. A kept pact's says it was paid at sealing. Returns its height.
	 */
	private int priceRow(PactStatePayload state, boolean paid) {
		ScreenRectangle text = layout.text();
		PactStatePayload.Term compact = term(state, "compact");
		int y = text.bottom() - MagicArt.SLOT;
		priceRule = y - 7;
		int x = text.left();
		if (compact != null) {
			MagicChip price = new MagicChip(compact.value(), ui("short.price").copy().withStyle(ChatFormatting.BOLD), font, MagicTheme.HEADING, theme.body());
			price.setPosition(x, y + 6);
			onScreen("term:compact", price);
			x += 36;
			if (paid || state.compact().isEmpty()) {
				PactGlance.Glance glance = PactGlance.of(compact.value());
				if (paid) {
					MagicChip when = new MagicChip(compact.value(), glance.shown(), font, MagicTheme.DIM, theme.body()).quiet();
					when.setPosition(x, y + 6);
					onScreen("", when);
				}
			} else {
				PactAfford.Count count = minecraft.player == null ? new PactAfford.Count(List.of(), 0, 0, true)
						: PactAfford.count(minecraft.player, PactAfford.needs(state.compact()), state.resonance());
				MagicStacks stacks = new MagicStacks(font, MagicStacks.compact(state.compact(), count), false).inks(theme.body());
				stacks.setPosition(x, y);
				onScreen("", stacks);
				x += stacks.getWidth() + 6;
				if (state.resonance() > 0) {
					boolean short_ = !count.resonanceMet();
					Component shown = short_ ? ui("short.resonance", count.charge(), state.resonance()) : PactGlance.of(compact.value()).shown();
					MagicChip resonance = new MagicChip(compact.value(), shown, font, short_ ? MagicTheme.RED : MagicTheme.INK, theme.body())
							.glyph(MagicArt.Glyph.SPARKLE).quiet();
					if (short_) {
						resonance.box(0xFFC0392B);
					}
					resonance.setPosition(x + 2, y + 6);
					onScreen("", resonance);
				}
			}
		}
		Component label = more ? ui("short.less") : ui("short.more");
		MagicButton toggle = new MagicButton(MagicButton.Look.LINK, label, label, unused -> {
			more = !more;
			dirty = true;
		}).icon(more ? MagicArt.MARKER_EXPANDED : MagicArt.MARKER_COLLAPSED, more ? 6 : 3, more ? 3 : 6);
		int width = toggle.labelWidth(font);
		toggle.setRectangle(width, toggle.getHeight(), text.right() - width, y + 5);
		onScreen("more", toggle);
		return MagicArt.SLOT + 9;
	}

	/** A kept page's row in the scroll: its icon or mark and its glance; the whole row its message. */
	private void row(PactStatePayload state, int index) {
		MagicText line = line(state, state.notes().get(index), BY_MARK, layout.text().width());
		line.setX(layout.text().left());
		inBody("note:" + index, line, 3);
	}

	/** A closed page's row, in the scroll. */
	private void inRow(PactStatePayload state, int index, int colour) {
		MagicText line = line(state, state.notes().get(index), colour, layout.text().width());
		line.setX(layout.text().left());
		inBody("note:" + index, line, 4);
	}

	/** A sentence's line: its icon or mark, its glance in the ink its mark gives (or {@code colour}), at most two lines; the whole sentence its message. */
	private MagicText line(PactStatePayload state, Component full, int colour, int width) {
		PactGlance.Glance glance = PactGlance.of(full);
		String server = PactGlance.server(full);
		int ink = colour != BY_MARK ? colour : colour(glance.mark());
		MagicText line = new MagicText(full, glance.shown(), font, width, ink, theme.body()).maxLines(2);
		PactGlance.Icon icon = PactGlance.icon(server, state.spirit().work(), state.spirit().sign());
		if (!icon.item().isEmpty()) {
			line.lead(icon.item());
		} else if (icon.glyph() != null) {
			line.lead(icon.glyph());
		} else if (glance.mark() != null) {
			line.lead(glance.mark());
		}
		return line;
	}

	/** A paragraph in the scroll (a note of the screen's own). */
	private void say(Component full, Component shown, int colour, String key, int width) {
		MagicText line = new MagicText(full, shown, font, width, colour, theme.body());
		line.setX(layout.text().left());
		inBody(key, line, 3);
	}

	/** The answer (D-06), pinned at the body's top with its mark; while one is awaited, that it is. */
	private void result(PactStatePayload state) {
		if (PactClient.waiting()) {
			MagicText waiting = new MagicText(ui("waiting"), font, layout.text().width(), MagicTheme.DIM, theme.body());
			pin("result", waiting);
		} else {
			state.result().ifPresent(result -> verdict(result.message(), result.done()));
		}
	}

	/** D-06: the answer, marked done or refused (Verdict), its glance in bold, under the key "result". */
	private void verdict(Component text, boolean done) {
		PactGlance.Glance glance = PactGlance.of(text);
		int colour = done ? MagicTheme.GREEN : MagicTheme.RED;
		Verdict line = new Verdict(text, glance.shown().copy().withStyle(ChatFormatting.BOLD), done, font, layout.text().width(), colour, false);
		pin("result", line);
	}

	/** Pins {@code widget} at the body's top, or, on a page without a pinned top (the chooser), at the scroll's start. */
	private void pin(String key, AbstractWidget widget) {
		if (surface == Surface.CHOOSER || body != null) {
			widget.setX(layout.text().left());
			if (body != null) {
				inBody(key, widget, 4);
				return;
			}
		}
		widget.setPosition(layout.text().left(), top);
		onScreen(key, widget);
		top += widget.getHeight() + 2;
	}

	/** Adds {@code widget} beside the panel (an arrow, a bookmark) at {@code place}. */
	private void beside(String key, MagicButton widget, ScreenRectangle place) {
		widget.setRectangle(place.width(), place.height(), place.left(), place.top());
		onScreen(key, widget);
	}

	private MagicButton plate(Component message, Component label, Runnable action) {
		return new MagicButton(MagicButton.Look.PLATE, message, label, unused -> action.run());
	}

	// ------------------------------------------------------------------------------------------------ actions

	private void turn(int to) {
		PactStatePayload state = PactClient.state(menu);
		if (state == null) {
			return;
		}
		page = Math.max(0, Math.min(to, state.beats().size()));
		if (page == state.beats().size()) {
			hear();
			// Spec 1.5: his beats are heard; his perch opens on his terms from now on.
			ConcordanceClientOptions.hearBeats(spirit(state));
			focus = "para:0";
		} else {
			minecraft.getNarrator().saySystemNow(state.beats().get(page));
		}
		dirty = true;
	}

	/** The holder asks to hear his terms, once (D-S9): nothing is stored or spent. */
	private void hear() {
		if (!asked) {
			asked = true;
			PactClient.request(menu, PactRequestPayload.CHOOSE, 0);
		}
	}

	/** The spirit whose beats these are, as the client options remember it: its counterpart, else its name. */
	private static String spirit(PactStatePayload state) {
		String counterpart = MagicPortrait.counterpart(state.spirit().sign());
		return counterpart.isEmpty() ? state.speaker().getString() : counterpart;
	}

	private void showTerms(boolean terms) {
		termsTab = terms;
		dirty = true;
	}

	/**
	 * Ending asks first, through vanilla's confirmation with the keyboard on Keep it (KeepFirstConfirmScreen, D-06), drawn
	 * on the scroll; only its yes sends END, and Esc keeps the pact.
	 */
	private void confirmEnd() {
		PactStatePayload state = PactClient.state(menu);
		Component speaker = state == null ? title : state.speaker();
		minecraft.gui.setScreen(KeepFirstConfirmScreen.magic(yes -> {
			minecraft.gui.setScreen(this);
			if (yes) {
				PactClient.request(menu, PactRequestPayload.END, endChoice());
			}
		}, ui("end.title", speaker), ui("end.confirm", speaker), List.of(ui("short.end.leaves"), ui("short.end.undone")), ui("end.yes"), ui("end.no")));
	}

	/** The pact END and SEAT name: on the seat page the one chosen there (0 with one); 0 elsewhere. */
	private int endChoice() {
		return mode == PactStatePayload.SEAT ? Math.max(0, pattern) : 0;
	}

	// ------------------------------------------------------------------------------------------------ drawing

	@Override
	protected void extractChrome(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		if (surface == Surface.CHOOSER) {
			int y = titleY();
			g.text(font, heading, (width - font.width(heading)) / 2, y, MagicTheme.GOLD, true);
			MagicArt.underline(g, (width - 132) / 2, y + 10, 132, MagicTheme.GOLD);
			return;
		}
		ScreenRectangle header = layout.header();
		ScreenRectangle scroll = layout.body();
		PactStatePayload state = PactClient.state(menu);
		if (layout.compact()) {
			MagicArt.panel(g, MagicArt.PLATE, header.left(), header.top(), header.width(), MagicArt.PLATE_HEIGHT);
			ItemStack shownSign = PactGlance.item(sign);
			if (!shownSign.isEmpty()) {
				g.item(shownSign, header.left() + 5, header.top() + 1);
			}
			g.text(font, bold(name, headerRight() - tagRoom - header.left() - 26), header.left() + 25, header.top() + 6, MagicTheme.CREAM, true);
			if (!tag.getString().isEmpty()) {
				g.text(font, tag, headerRight() - font.width(tag), header.top() + 6, tagColour == MagicTheme.DIM ? MagicTheme.LAVENDER : MagicTheme.CREAM, true);
			}
		} else {
			MagicArt.header(g, header.left(), header.top(), header.width());
			LivingEntity creature = state == null ? null : liveSpirit ? MagicPortrait.atPerch(menu.opening.perch(), sign) : null;
			if (creature == null && state != null && !liveSpirit) {
				creature = MagicPortrait.ofSign(sign);
			}
			MagicPortrait.draw(g, header.left() + 7, header.top() + 10, 34, 49, creature, sign, ended);
			int x = header.left() + 56;
			int right = headerRight();
			g.text(font, bold(name, right - tagRoom - x), x, header.top() + 19, MagicTheme.NAME, false);
			if (!tag.getString().isEmpty()) {
				g.text(font, tag, right - font.width(tag), header.top() + 19, tagColour, false);
			}
			if (namedService) {
				if (!serviceIcon.isEmpty()) {
					g.item(serviceIcon, x, header.top() + 29);
				}
				g.text(font, serviceName, x + 19, header.top() + 33, MagicTheme.DIM, false);
			}
		}
		if (pips >= 0 && state != null) {
			// Three page pips at the header's right, left of the skip (or at the right edge on the last beat).
			AbstractWidget skip = controls.get("skip");
			int right = skip != null ? skip.getX() - 6 : headerRight();
			int count = state.beats().size();
			int y = headerTextY() + 2;
			for (int i = 0; i < count; i++) {
				MagicArt.panel(g, i <= pips ? MagicArt.STAR_SMALL_ACTIVE : MagicArt.STAR_SMALL_HOLE, right - (count - i) * 6, y, 4, 4);
			}
		}
		if (surface == Surface.BEAT) {
			MagicArt.scroll(g, scroll.left(), scroll.top(), scroll.width(), MagicArt.ruledHeight(scroll.height()), true);
		} else if (surface == Surface.SCROLL) {
			MagicArt.scroll(g, scroll.left(), scroll.top(), scroll.width(), scroll.height(), false);
		}
		if (priceRule >= 0) {
			ScreenRectangle text = layout.text();
			MagicArt.underline(g, text.left() - 4, priceRule, text.width() + 8, WOOD);
		}
	}

	/** {@code name} in bold, cut with "…" to {@code width} (bold is a pixel wider a letter). */
	private FormattedCharSequence bold(Component name, int width) {
		Component strong = name.copy().withStyle(ChatFormatting.BOLD);
		if (font.width(strong) <= width) {
			return strong.getVisualOrderText();
		}
		String plain = name.getString();
		return FormattedCharSequence.forward(MagicArt.fit(font, plain, Math.max(10, width - plain.length())), Style.EMPTY.withBold(true));
	}

	private Component pageName(PactStatePayload state) {
		return switch (state.mode()) {
			case PactStatePayload.NEGOTIATE -> page < state.beats().size() ? ui("page", page + 1, state.beats().size()) : ui("terms");
			case PactStatePayload.KEEP -> termsTab ? ui("terms") : ui("kept");
			case PactStatePayload.SEAT -> ui("without_perch");
			case PactStatePayload.INVITE -> ui("invite.heading");
			default -> Component.empty();
		};
	}

	@Override
	public Component getNarrationMessage() {
		PactStatePayload state = PactClient.state(menu);
		return state == null ? title : CommonComponents.joinForNarration(state.speaker(), pageName(state));
	}

	@Override
	protected Component getUsageNarration() {
		return ui("help");
	}

	@Override
	public void onClose() {
		// Closing is leaving: a visit he holds no pact for ends there (D-S7); a kept pact is untouched.
		minecraft.player.closeContainer();
		super.onClose();
	}

	@Override
	public void removed() {
		PactClient.unlisten(listener);
		super.removed();
	}

	/** The page shown (0 to 2 his words, {@link #TERMS_PAGE} his terms), for tests. */
	public int page() {
		return page;
	}

	/** Whether More's six standing lines are shown, for tests. */
	public boolean more() {
		return more;
	}

	// ------------------------------------------------------------------------------------------------ its widgets

	/**
	 * Concordance Chunk D, D-06 (03:159): the answer to the last request with its mark, a tick when it was done, a cross when
	 * it was refused, drawn before its words, so the outcome never rests on colour alone nor on his gesture or its sound being
	 * seen or heard. It draws the answer's glance ("Short of the price"); its message, narration ("Done: ..." or "Refused:
	 * ...") and tip are the whole sentence. Focusable, as MagicText is. The roster uses it too, with its own words and colour.
	 */
	public static final class Verdict extends AbstractWidget implements MagicScreen.Drawn {
		private final Font font;
		private final List<FormattedCharSequence> lines;
		private final List<FormattedCharSequence> tip;
		private final String drawn;
		private final int colour;
		private final boolean refused;
		private final boolean shadow;

		Verdict(Component text, boolean done, Font font, int width, int colour) {
			this(text, text, done, font, width, colour, false);
		}

		Verdict(Component full, Component shown, boolean done, Font font, int width, int colour, boolean shadow) {
			super(0, 0, Math.max(30, width), 1, full);
			this.font = font;
			this.colour = colour;
			this.refused = !done;
			this.shadow = shadow;
			lines = font.split(shown, getWidth() - MagicArt.Mark.CROSS.width - 6);
			drawn = shown.getString();
			tip = drawn.equals(full.getString()) ? List.of() : font.split(full, 200);
			setHeight(Math.max(MagicArt.Mark.HEIGHT + 2, Math.max(1, lines.size()) * (font.lineHeight + 2) + 1));
		}

		/** Whether its mark is the refusal's cross (tests). */
		public boolean refused() {
			return refused;
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
			MagicArt.Mark mark = refused ? MagicArt.Mark.CROSS : MagicArt.Mark.TICK;
			MagicArt.mark(g, mark, getX(), getY());
			int tx = getX() + mark.width + 3;
			int ty = getY() + 1;
			for (FormattedCharSequence line : lines) {
				g.text(font, line, tx, ty, colour, shadow);
				ty += font.lineHeight + 2;
			}
			if (isFocused()) {
				g.fill(tx, getY() + getHeight() - 1, getX() + getWidth() - 1, getY() + getHeight(), colour);
			}
		}

		@Override
		protected void extractTooltipForNextRenderPass(GuiGraphicsExtractor g, int mouseX, int mouseY) {
			MagicArt.tip(g, this, tip, null, mouseX, mouseY);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			output.add(NarratedElementType.TITLE, ui(refused ? "result.refused" : "result.done", getMessage()));
		}

		@Override
		public String drawnText() {
			return drawn;
		}
	}

	/** Today's work (spec 2.1): the gold-framed gauge filled to done of max, and its glance ("5/16 today"); its message the whole row. */
	private static final class Gauge extends AbstractWidget implements MagicScreen.Drawn {
		private final Font font;
		private final Component shown;
		private final int done;
		private final int max;
		private final int bar;
		private final List<FormattedCharSequence> tip;

		Gauge(Component full, Component shown, int done, int max, int bar, Font font) {
			super(0, 0, bar + 5 + font.width(shown), MagicArt.GAUGE_HEIGHT, full);
			this.font = font;
			this.shown = shown;
			this.done = done;
			this.max = max;
			this.bar = bar;
			this.tip = font.split(full, MagicChip.TIP_WIDTH);
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
			MagicArt.gauge(g, getX(), getY(), bar, done, max);
			int tx = getX() + bar + 5;
			g.text(font, shown, tx, getY() + 1, MagicTheme.INK, false);
			if (isFocused()) {
				g.fill(tx, getY() + getHeight(), tx + font.width(shown), getY() + getHeight() + 1, MagicTheme.HEADING);
			}
		}

		@Override
		protected void extractTooltipForNextRenderPass(GuiGraphicsExtractor g, int mouseX, int mouseY) {
			MagicArt.tip(g, this, tip, null, mouseX, mouseY);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			output.add(NarratedElementType.TITLE, getMessage());
		}

		@Override
		public String drawnText() {
			return shown.getString();
		}
	}

	/** The periods paid ahead (spec 2.5): exactly cap sockets, the first held lit; its message the escrow's sentence. */
	private static final class Sockets extends AbstractWidget {
		private final int held;
		private final int cap;
		private final List<FormattedCharSequence> tip;

		Sockets(Component full, int held, int cap) {
			super(0, 0, Math.max(1, cap) * MagicArt.SOCKET_SIZE, MagicArt.SOCKET_SIZE, full);
			this.held = held;
			this.cap = cap;
			this.tip = Minecraft.getInstance().font.split(full, MagicChip.TIP_WIDTH);
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
			MagicArt.sockets(g, getX() + getWidth() / 2, getY(), cap, held);
			if (isFocused()) {
				g.outline(getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, MagicTheme.GOLD);
			}
		}

		@Override
		protected void extractTooltipForNextRenderPass(GuiGraphicsExtractor g, int mouseX, int mouseY) {
			MagicArt.tip(g, this, tip, null, mouseX, mouseY);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			output.add(NarratedElementType.TITLE, getMessage());
		}
	}

	/**
	 * A pattern the holder observed (D-S1), as a row: its glyph and name, then its cost in resonance and "used" when this
	 * occurrence was recalled; the one chosen sits on the scroll's stain. Its message is the pattern's name.
	 */
	private final class PatternRow extends MagicButton {
		private final PactStatePayload.Choice choice;
		private final boolean chosen;

		PatternRow(PactStatePayload.Choice choice, boolean chosen, OnPress press) {
			super(Look.LINK, choice.name(), choice.name(), press);
			this.choice = choice;
			this.chosen = chosen;
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
			if (chosen) {
				g.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), STAIN);
			}
			int ink = choice.ready() ? MagicTheme.INK : MagicTheme.DIM;
			int x = getX() + 3;
			MagicArt.glyph(g, choice.id().contains("moon") ? MagicArt.Glyph.MOON : MagicArt.Glyph.SUN, x, getY() + 2, ink);
			g.text(font, choice.name(), x + 13, getY() + 2, ink, false);
			int cost = x + Math.max(92, font.width(choice.name()) + 20);
			MagicArt.glyph(g, MagicArt.Glyph.SPARKLE, cost, getY() + 2, ink);
			g.text(font, String.valueOf(choice.amount()), cost + 11, getY() + 2, ink, false);
			if (!choice.ready()) {
				g.text(font, ui("short.used"), cost + 24, getY() + 2, MagicTheme.DIM, false);
			}
			if (isHoveredOrFocused()) {
				g.fill(x + 13, getY() + getHeight() - 1, x + 13 + font.width(choice.name()), getY() + getHeight(), MagicTheme.HEADING);
			}
		}

		@Override
		public String drawnText() {
			return choice.name().getString() + " " + choice.amount() + (choice.ready() ? "" : " " + ui("short.used").getString());
		}
	}

	/** A pact without its perch (GB4), as a small portrait card: its frame, its spirit, its name; the one chosen lit. */
	private final class Stranded extends MagicButton {
		private final PactStatePayload.Choice choice;
		private final boolean chosen;

		Stranded(PactStatePayload.Choice choice, boolean chosen, OnPress press) {
			super(Look.LINK, choice.name(), choice.name(), press);
			this.choice = choice;
			this.chosen = chosen;
			setRectangle(30 + 4 + font.width(choice.name().copy().withStyle(ChatFormatting.BOLD)) + 2, 43, 0, 0);
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
			int x = getX();
			int y = getY() + 1;
			MagicPortrait.draw(g, x + 5, y + 6, 20, 29, MagicPortrait.ofSign(choice.id()), choice.id(), false);
			MagicArt.panel(g, MagicArt.CARD_FRAME_ACTIVE, x, y, 30, 41);
			Component shown = chosen ? choice.name().copy().withStyle(ChatFormatting.BOLD) : choice.name();
			g.text(font, shown, x + 34, y + 16, chosen ? MagicTheme.NAME : MagicTheme.DIM, false);
			if (chosen || isHoveredOrFocused()) {
				g.outline(x - 2, y - 2, 34, 45, chosen ? CARD_LIGHT : MagicTheme.HEADING);
			}
		}

		@Override
		public String drawnText() {
			return choice.name().getString();
		}
	}

	/**
	 * A spirit on the chooser (spec 2.14): its card (the gilded frame and the spirit in it, chained and locked when it is
	 * not ready), its name on a plate, and under it "Ready" or "Not yet" with its reason of at most four words, drawn on
	 * every card that is not ready. Its message is the spirit's name.
	 */
	private final class Card extends MagicButton {
		private final PactStatePayload.Choice choice;
		private final List<FormattedCharSequence> reason;
		private final String reasonText;

		Card(PactStatePayload.Choice choice, Component reason, int width, OnPress press) {
			super(Look.LINK, choice.name(), choice.name(), press);
			this.choice = choice;
			this.reason = font.split(reason, Math.max(40, width));
			this.reasonText = reason.getString();
			setRectangle(Math.max(64, width), 77 + 2 + MagicArt.PLATE_HEIGHT + 3 + 10 + this.reason.size() * 10, 0, 0);
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
			int cx = getX() + getWidth() / 2;
			int y = getY();
			int card = cx - 24;
			MagicPortrait.draw(g, card + 7, y + 10, 34, 49, MagicPortrait.ofSign(choice.id()), choice.id(), !choice.ready());
			MagicArt.panel(g, MagicArt.PORTRAIT_CARD, card, y, 48, 77);
			if (!choice.ready()) {
				MagicArt.panel(g, MagicArt.CARD_CHAINS, card + 8, y + 14, 32, 41);
				MagicArt.panel(g, MagicArt.CARD_LOCK, card + 17, y + 54, 14, 17);
			}
			boolean lit = isHoveredOrFocused();
			if (lit) {
				g.outline(card - 2, y - 2, 52, 81, CARD_LIGHT);
			}
			int py = y + 79;
			MagicArt.panel(g, lit ? MagicArt.sprite("plate_highlighted") : MagicArt.PLATE, cx - 32, py, 64, MagicArt.PLATE_HEIGHT);
			Component name = choice.name().copy().withStyle(ChatFormatting.BOLD);
			g.text(font, name, cx - font.width(name) / 2, py + 6, MagicTheme.CREAM, true);
			int sy = py + MagicArt.PLATE_HEIGHT + 4;
			MagicArt.Mark mark = choice.ready() ? MagicArt.Mark.TICK : MagicArt.Mark.LOCK;
			Component state = choice.ready() ? ui("short.ready") : ui("short.not_yet");
			int w = mark.width + 3 + font.width(state);
			MagicArt.mark(g, mark, cx - w / 2, sy - 1);
			g.text(font, state, cx - w / 2 + mark.width + 3, sy, choice.ready() ? MagicTheme.GOOD : MagicTheme.LAVENDER, true);
			int ry = sy + 11;
			for (FormattedCharSequence line : reason) {
				g.text(font, line, cx - font.width(line) / 2, ry, MagicTheme.LAVENDER, true);
				ry += 10;
			}
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput output) {
			super.updateWidgetNarration(output);
			output.add(NarratedElementType.HINT, choice.ready() ? ui("invite.ready") : reasonText.isEmpty() ? ui("invite.not_ready")
					: Component.empty().append(ui("short.not_yet")).append(": ").append(reasonText));
		}

		@Override
		public String drawnText() {
			String state = (choice.ready() ? ui("short.ready") : ui("short.not_yet")).getString();
			return choice.name().getString() + " " + state + (reasonText.isEmpty() ? "" : " " + reasonText);
		}
	}
}
