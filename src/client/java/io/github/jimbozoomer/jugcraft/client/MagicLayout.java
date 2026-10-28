package io.github.jimbozoomer.jugcraft.client;

import net.minecraft.client.gui.navigation.ScreenRectangle;

/**
 * Where a magic screen puts its parts (docs/features/magic-interfaces.md, spec 2.11), from the GUI size and what the
 * body must hold. There is no height without a layout:
 * <ul>
 * <li><b>Full</b> when the screen is 240 or more tall and the body's words fit: the relic header (77), the body scroll
 * (130) and a footer of plates (19), centred.</li>
 * <li><b>Compact</b> below 240, or whenever the full body would not hold its words: the header is one plate row (19)
 * and the body takes the room it gave up.</li>
 * <li><b>Narrow</b> under 360 wide: the panel takes the width less a margin, and the paging arrows and bookmarks that
 * sit beside it when wide move into the footer.</li>
 * </ul>
 *
 * @param panel  the header, body and footer together
 * @param header the relic header, or in the compact layout its plate row
 * @param body   the body's scroll
 * @param footer the footer's row of controls
 */
record MagicLayout(int screenWidth, int screenHeight, boolean compact, boolean narrow, ScreenRectangle panel, ScreenRectangle header,
		ScreenRectangle body, ScreenRectangle footer) {
	static final int PANEL_WIDTH = 320;
	static final int HEADER = MagicArt.HEADER_HEIGHT;
	static final int COMPACT_HEADER = MagicArt.PLATE_HEIGHT;
	static final int BODY = 130;
	static final int FOOTER = MagicArt.PLATE_HEIGHT;
	static final int FULL_FROM = 240;
	static final int WIDE_FROM = 360;
	/** The room each paging arrow (13 wide) takes beside the panel when wide. */
	static final int ARROW_ROOM = 15;
	/** The scroll's painted margins, inside which its words sit (rollers and torn edges outside). */
	static final int TEXT_LEFT = 24;
	static final int TEXT_RIGHT = 26;
	static final int TEXT_TOP = 14;
	static final int TEXT_BOTTOM = 15;

	/**
	 * The pact screen's layout for a {@code width} x {@code height} GUI whose body must hold {@code content} pixels of
	 * words (0 when not known yet): full only when it is tall enough and they fit.
	 */
	static MagicLayout pact(int width, int height, int content) {
		boolean narrow = width < WIDE_FROM;
		int w = narrow ? Math.max(0, width - 8) : PANEL_WIDTH;
		int x = (width - w) / 2;
		boolean compact = height < FULL_FROM || content > BODY - TEXT_TOP - TEXT_BOTTOM;
		if (!compact) {
			int y = 4 + (height - FULL_FROM) / 2;
			ScreenRectangle header = new ScreenRectangle(x, y, w, HEADER);
			ScreenRectangle body = new ScreenRectangle(x, y + HEADER + 1, w, BODY);
			ScreenRectangle footer = new ScreenRectangle(x, body.bottom() + 3, w, FOOTER);
			return new MagicLayout(width, height, false, narrow, new ScreenRectangle(x, y, w, footer.bottom() - y), header, body, footer);
		}
		// Compact: a plate row, then the body as tall as the screen allows (up to what the header gave back), then the footer.
		int bodyHeight = Math.max(24, Math.min(BODY + HEADER - COMPACT_HEADER, height - 3 - COMPACT_HEADER - 2 - 3 - FOOTER - 3));
		int total = COMPACT_HEADER + 2 + bodyHeight + 3 + FOOTER;
		int y = Math.max(3, (height - total) / 2);
		ScreenRectangle header = new ScreenRectangle(x, y, w, COMPACT_HEADER);
		ScreenRectangle body = new ScreenRectangle(x, header.bottom() + 2, w, bodyHeight);
		ScreenRectangle footer = new ScreenRectangle(x, body.bottom() + 3, w, FOOTER);
		return new MagicLayout(width, height, true, narrow, new ScreenRectangle(x, y, w, footer.bottom() - y), header, body, footer);
	}

	/** Where the body's words go: inside the scroll's painted margins. */
	ScreenRectangle text() {
		return new ScreenRectangle(body.left() + TEXT_LEFT, body.top() + TEXT_TOP, Math.max(0, body.width() - TEXT_LEFT - TEXT_RIGHT),
				Math.max(0, body.height() - TEXT_TOP - TEXT_BOTTOM));
	}

	/** Whether the paging arrows sit beside the body (wide) rather than in the footer (narrow). */
	boolean arrowsBeside() {
		return !narrow;
	}

	/** The left arrow's place beside the body (wide layouts), 13x32 and centred on it. */
	ScreenRectangle leftArrow() {
		return new ScreenRectangle(body.left() - ARROW_ROOM, body.top() + (body.height() - 32) / 2, 13, 32);
	}

	/** The right arrow's place beside the body (wide layouts). */
	ScreenRectangle rightArrow() {
		return new ScreenRectangle(body.right() + 2, body.top() + (body.height() - 32) / 2, 13, 32);
	}

	/** Everything the screen draws as its panel, the arrows' room included when they sit beside it (ScreenKit.Fitted). */
	ScreenRectangle panelArea() {
		return narrow ? panel : new ScreenRectangle(panel.left() - ARROW_ROOM, panel.top(), panel.width() + 2 * ARROW_ROOM, panel.height());
	}

	/** Whether {@code area} lies wholly inside the screen. */
	boolean onScreen(ScreenRectangle area) {
		return area.left() >= 0 && area.top() >= 0 && area.right() <= screenWidth && area.bottom() <= screenHeight;
	}
}
