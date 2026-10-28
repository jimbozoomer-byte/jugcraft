package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

/**
 * The magic interfaces' art (docs/features/magic-interfaces.md): the sprites tools/magic_ui.py writes from the owner's
 * magic interface kit, and the helpers that draw them. Panels are nine-sliced by their .mcmeta and never scaled by a
 * fraction (spec 2.2); the cosmos backdrop and the gauge's fill are picked by time and hold still under reduced motion;
 * glyphs are white and take the ink of the surface they sit on; marks keep their own colours. Every sprite named here
 * has its file (check_magic_ui), and the glyph and mark widths are the files' own.
 */
final class MagicArt {
	static final Identifier SCROLL_PLAIN = sprite("scroll_plain");
	static final Identifier SCROLL_RULED = sprite("scroll_ruled");
	static final Identifier RELIC_HEADER = sprite("relic_header");
	static final Identifier SOCKET = sprite("socket");
	static final Identifier PORTRAIT_CARD = sprite("portrait_card");
	static final Identifier GOLD_FRAME = sprite("gold_frame");
	static final Identifier MANA = sprite("mana");
	static final Identifier SPELL_BOOK = sprite("spell_book");
	static final Identifier PAGE_TITLE_FRAME = sprite("page_title_frame");
	static final Identifier SCROLL_BALL = sprite("scroll_ball");
	static final Identifier PLATE_OUTLINE = sprite("plate_outline");
	/** The darkened wooden plate: actions, and in the compact layout the header's row. */
	static final Identifier PLATE = sprite("plate");
	static final Identifier STAR_BIG_ACTIVE = sprite("star/big_active");
	static final Identifier STAR_BIG_INACTIVE = sprite("star/big_inactive");
	static final Identifier STAR_SMALL_ACTIVE = sprite("star/small_active");
	static final Identifier STAR_SMALL_HOLE = sprite("star/small_hole");
	static final Identifier CARD_FRAME_ACTIVE = sprite("card/frame_active");
	static final Identifier CARD_FRAME_INACTIVE = sprite("card/frame_inactive");
	static final Identifier CARD_CHAINS = sprite("card/chains");
	static final Identifier CARD_LOCK = sprite("card/lock");
	static final Identifier CARD_COMPLETED = sprite("card/completed");
	static final Identifier DOC_FRAME_IMAGE = sprite("doc/frame_image");
	static final Identifier DOC_UNDERLINE = sprite("doc/underline");
	static final Identifier DOC_FRAME_ITEM = sprite("doc/frame_item");
	static final Identifier DOC_FRAME_PEDESTAL = sprite("doc/frame_pedestal");
	static final Identifier DOC_ARROW_DOWN = sprite("doc/arrow_down");
	static final Identifier MARKER_COLLAPSED = sprite("marker/collapsed");
	static final Identifier MARKER_EXPANDED = sprite("marker/expanded");
	/** The documentation's small page arrows (14x8), for the book's pages (variant A); the narrow footer's plates draw glyphs. */
	static final Identifier PAGE_LEFT = sprite("doc/page_left");
	static final Identifier PAGE_RIGHT = sprite("doc/page_right");
	/** The tooltip style jugcraft:magic: vanilla draws its sprites tooltip/magic_background and tooltip/magic_frame. */
	static final Identifier TOOLTIP = Jugcraft.id("magic");
	/** The animated backdrop, a plain texture (its frame is chosen here, so reduced motion can hold it). */
	static final Identifier COSMOS = Jugcraft.id("textures/gui/magic/cosmos.png");
	static final int COSMOS_WIDTH = 407;
	static final int COSMOS_HEIGHT = 234;
	static final int COSMOS_FRAMES = 8;
	/** Where a portrait window's fill is cut from: the opaque middle of each cosmos frame. */
	private static final int WINDOW_U = 146;
	private static final int WINDOW_V = 86;
	static final int HEADER_HEIGHT = 77;
	static final int PLATE_HEIGHT = 19;
	static final int SCROLL_WIDTH = 243;
	static final int BOOK_WIDTH = 290;
	static final int BOOK_HEIGHT = 188;
	/** A gold-frame slot (an item at +2,+2) and a documentation frame (an item at +3,+3). */
	static final int SLOT = 20;
	static final int PAPER_SLOT = 22;
	static final int SOCKET_SIZE = 8;
	static final int GAUGE_HEIGHT = 9;
	/** The mana fill: 96 wide, 198 rows used of its 256x256 file; the gauge shows three rows of it, scrolling. */
	private static final int MANA_SIZE = 256;
	private static final int MANA_WIDTH = 96;
	private static final int MANA_ROWS = 198;

	/** The glyph family of spec 2.5: white, seven rows, each at its own width (tools/magic_ui.py GLYPHS, in order). */
	enum Glyph {
		INF(11), SPIRAL(7), SPARKLE(7), MOON(8), KEY(10), SUN(7), CLOCK(7), RING(7), CALENDAR(7), HOURGLASS(7), PERSON(7),
		PARTY(12), SCROLL(7), PAUSE(5), CROSS(7), LOCK(7), TICK(7), PIN(7), ARROW(7), ASK(5), BED(7), BACK(7);

		static final int HEIGHT = 7;
		final int width;
		final Identifier sprite;

		Glyph(int width) {
			this.width = width;
			this.sprite = MagicArt.sprite("glyph/" + name().toLowerCase(Locale.ROOT));
		}
	}

	/** The state marks (spec 2.5): a glyph in colour with a dark edge, nine rows (tools/magic_ui.py MARKS, in order). */
	enum Mark {
		TICK(9), LOCK(9), CROSS(9), ASK(7), MOON(10), HOURGLASS(9);

		static final int HEIGHT = 9;
		final int width;
		final Identifier sprite;

		Mark(int width) {
			this.width = width;
			this.sprite = MagicArt.sprite("mark/" + name().toLowerCase(Locale.ROOT));
		}
	}

	/** The widget whose tip was set in the frame drawn last (tests: the keyboard's tip shows, spec 4.5). */
	private static @Nullable AbstractWidget tipped;
	private static @Nullable AbstractWidget tipping;

	private MagicArt() {
	}

	/** A magic screen's frame begins: the tip set in the frame before becomes the one {@link #tipped} reports. */
	static void frame() {
		tipped = tipping;
		tipping = null;
	}

	/** The widget whose tip was shown in the last whole frame, or null. */
	static @Nullable AbstractWidget tipped() {
		return tipped;
	}

	/** A heading's line: the documentation underline, {@code w} wide at its own height, tinted {@code colour}. */
	static void underline(GuiGraphicsExtractor g, int x, int y, int w, int colour) {
		g.blitSprite(RenderPipelines.GUI_TEXTURED, DOC_UNDERLINE, x, y, w, 6, colour);
	}

	static Identifier sprite(String path) {
		return Jugcraft.id("magic/" + path);
	}

	/** A sprite at (x, y) drawn w x h by its own scaling (a nine-slice tiles its middle; nothing is stretched). */
	static void panel(GuiGraphicsExtractor g, Identifier sprite, int x, int y, int w, int h) {
		g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, w, h);
	}

	/** A scroll: plain for terms and kept pages, ruled for the beats (draw it {@link #ruledHeight} tall). */
	static void scroll(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean ruled) {
		panel(g, ruled ? SCROLL_RULED : SCROLL_PLAIN, x, y, w, h);
	}

	/**
	 * The tallest ruled scroll at most {@code room} tall whose rules stay fourteen apart: 88 + 14k (its slice tiles one
	 * rule period, spec 2.2).
	 */
	static int ruledHeight(int room) {
		return room < 88 ? room : 88 + (room - 88) / 14 * 14;
	}

	/** The relic header at its own height, its scroll stretched sideways only. */
	static void header(GuiGraphicsExtractor g, int x, int y, int w) {
		panel(g, RELIC_HEADER, x, y, w, HEADER_HEIGHT);
	}

	/** The spellbook at its own width; a short window tiles out rows of its pages (never stretched). */
	static void book(GuiGraphicsExtractor g, int x, int y, int h) {
		panel(g, SPELL_BOOK, x, y, BOOK_WIDTH, Math.min(BOOK_HEIGHT, h));
	}

	/** The cosmos frame to draw now: about eight a second, frame 0 under reduced motion. */
	static int cosmosFrame() {
		return ConcordanceClientOptions.reducedMotion() ? 0 : (int) (Util.getMillis() / 125L % COSMOS_FRAMES);
	}

	/** The cosmos blob centred on (cx, cy), its ragged edge over the dimmed world. */
	static void cosmos(GuiGraphicsExtractor g, int cx, int cy) {
		g.blit(RenderPipelines.GUI_TEXTURED, COSMOS, cx - COSMOS_WIDTH / 2, cy - COSMOS_HEIGHT / 2, 0, cosmosFrame() * COSMOS_HEIGHT,
				COSMOS_WIDTH, COSMOS_HEIGHT, COSMOS_WIDTH, COSMOS_HEIGHT * COSMOS_FRAMES);
	}

	/** A portrait window filled with a piece of the cosmos (at most its opaque middle's size). */
	static void cosmosWindow(GuiGraphicsExtractor g, int x, int y, int w, int h) {
		int cw = Math.min(w, COSMOS_WIDTH - WINDOW_U);
		int ch = Math.min(h, COSMOS_HEIGHT - WINDOW_V);
		g.blit(RenderPipelines.GUI_TEXTURED, COSMOS, x, y, WINDOW_U, cosmosFrame() * COSMOS_HEIGHT + WINDOW_V, cw, ch, COSMOS_WIDTH,
				COSMOS_HEIGHT * COSMOS_FRAMES);
	}

	/**
	 * Periods paid ahead (spec 2.5): exactly {@code cap} sockets centred on {@code cx}, the first {@code lit} with a gold
	 * star set in them. Sockets mean nothing else.
	 */
	static void sockets(GuiGraphicsExtractor g, int cx, int y, int cap, int lit) {
		int x = cx - cap * SOCKET_SIZE / 2;
		for (int i = 0; i < cap; i++) {
			panel(g, SOCKET, x + i * SOCKET_SIZE, y, SOCKET_SIZE, SOCKET_SIZE);
			if (i < lit) {
				panel(g, STAR_BIG_ACTIVE, x + i * SOCKET_SIZE, y, 8, 7);
			}
		}
	}

	/** Today's work as a gauge (spec 2.1): the gold frame, its purple well filled to value/max with the slow mana. */
	static void gauge(GuiGraphicsExtractor g, int x, int y, int w, int value, int max) {
		panel(g, GOLD_FRAME, x, y, w, GAUGE_HEIGHT);
		int fill = (int) ((w - 6) * (long) Math.max(0, Math.min(value, max)) / Math.max(1, max));
		int v = ConcordanceClientOptions.reducedMotion() ? 0 : (int) (Util.getMillis() / 150L % (MANA_ROWS - 3));
		for (int done = 0; done < fill; done += MANA_WIDTH) {
			g.blitSprite(RenderPipelines.GUI_TEXTURED, MANA, MANA_SIZE, MANA_SIZE, 0, v, x + 3 + done, y + 3, Math.min(MANA_WIDTH, fill - done), 3);
		}
	}

	/** An item slot: the gold frame on dark and on the scroll, or the documentation frame on the book's pages. */
	static void slot(GuiGraphicsExtractor g, int x, int y, boolean paper) {
		if (paper) {
			panel(g, DOC_FRAME_ITEM, x, y, PAPER_SLOT, PAPER_SLOT);
		} else {
			panel(g, GOLD_FRAME, x, y, SLOT, SLOT);
		}
	}

	/** A glyph at its own size, in {@code colour} (the ink of the surface it sits on). */
	static void glyph(GuiGraphicsExtractor g, Glyph glyph, int x, int y, int colour) {
		g.blitSprite(RenderPipelines.GUI_TEXTURED, glyph.sprite, x, y, glyph.width, Glyph.HEIGHT, colour);
	}

	/** A state mark at its own size, in its own colours. */
	static void mark(GuiGraphicsExtractor g, Mark mark, int x, int y) {
		panel(g, mark.sprite, x, y, mark.width, Mark.HEIGHT);
	}

	/** {@code text} cut with "…" to {@code width} (the one copy for the magic screens; BookArt.fit and ScreenKit.fit are the same rule). */
	static String fit(Font font, String text, int width) {
		if (font.width(text) <= width) {
			return text;
		}
		return font.plainSubstrByWidth(text, Math.max(0, width - font.width("…"))) + "…";
	}

	/**
	 * A widget's tip in the gold-frame style (jugcraft:magic, verified in 26.3: Tooltip.create's style and
	 * TooltipRenderUtil), shown while it is hovered or keyboard-focused, as vanilla's own WidgetTooltipHolder shows one;
	 * {@code at} places it (null: vanilla's own placement for the mouse or the keyboard).
	 */
	static void tip(GuiGraphicsExtractor g, AbstractWidget widget, List<FormattedCharSequence> lines, @Nullable ClientTooltipPositioner at,
			int mouseX, int mouseY) {
		if (lines.isEmpty()) {
			return;
		}
		boolean keyboard = widget.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
		if (!widget.isHovered() && !keyboard) {
			return;
		}
		ClientTooltipPositioner place = at != null ? at
				: !widget.isHovered() ? new BelowOrAboveWidgetTooltipPositioner(widget.getRectangle()) : new MenuTooltipPositioner(widget.getRectangle());
		g.setTooltipForNextFrame(Minecraft.getInstance().font, lines, Optional.empty(), place, mouseX, mouseY, keyboard, TOOLTIP);
		// As vanilla keeps it: the keyboard's tip replaces one already set this frame, a hovered one only fills an empty place.
		if (keyboard || tipping == null) {
			tipping = widget;
		}
	}
}
