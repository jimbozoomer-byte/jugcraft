package io.github.jimbozoomer.jugcraft.client;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * The magic interfaces' themes (docs/features/magic-interfaces.md, spec 2.3): one palette for the whole family, in
 * place of the navy constants the pact, roster and composer screens each copied. Text on scroll and paper is ink with no
 * shadow; text on the cosmos, wood and the gold frame is cream or gold with vanilla's shadow. Every ink is held to
 * tools/magic_ui.py PALETTE by check_magic_ui, and each sprite's main fill against the inks drawn on it is 4.5:1 or more.
 *
 * @param name   the theme's name (for the record)
 * @param body   the inks of the body's surface
 * @param chrome the inks of what sits on the backdrop and on wood (titles, plates, tooltips)
 * @param panel  the body's nine-sliced surface, or null when the screen draws its own (the alchemy screens' engraved art)
 * @param ruled  the ruled surface (the beats' scroll), or null
 */
record MagicTheme(String name, Inks body, Inks chrome, @Nullable Identifier panel, @Nullable Identifier ruled) {
	// On scroll and paper (no shadow).
	static final int INK = 0xFF403527;
	static final int DIM = 0xFF64523C;
	static final int HEADING = 0xFF7C281C;
	static final int NAME = 0xFF5B2216;
	static final int GREEN = 0xFF2F5A1A;
	static final int NEED = 0xFF704808;
	static final int RED = 0xFF8F2A17;
	// On the cosmos, wood and the gold frame (with vanilla's shadow).
	static final int CREAM = 0xFFFFFAEE;
	static final int LAVENDER = 0xFFB6A8D8;
	static final int GOLD = 0xFFFFC43B;
	static final int GOOD = 0xFFB9E05A;
	static final int WARN = 0xFFFFA53B;
	static final int BAD = 0xFFFF7466;
	/** A disabled plate's label. */
	static final int GREY = 0xFFA89C8C;

	/** One surface's inks: body text, secondary, headings, names, good, needs action, refused; with or without shadow. */
	record Inks(int text, int secondary, int heading, int name, int good, int needs, int refused, boolean shadow) {
		/**
		 * The colour of a focused element's one-pixel underline: on dark, the gold of spec 2.8; on parchment the heading
		 * ink, since gold on the scroll's #eed1ad is too faint to see.
		 */
		int focus() {
			return shadow ? GOLD : heading;
		}
	}

	static final Inks PAPER = new Inks(INK, DIM, HEADING, NAME, GREEN, NEED, RED, false);
	static final Inks DARK = new Inks(CREAM, LAVENDER, GOLD, CREAM, GOOD, WARN, BAD, true);

	/** Variant B (the relic scroll card): the pact screens, the roster and the composer. */
	static final MagicTheme SCROLL = new MagicTheme("scroll", PAPER, DARK, MagicArt.SCROLL_PLAIN, MagicArt.SCROLL_RULED);
	/** Variant A (the open spellbook), kept for the Field Guide and the codex: ink on the book's pages. */
	static final MagicTheme BOOK = new MagicTheme("book", PAPER, DARK, MagicArt.SPELL_BOOK, null);
	/** The alchemy and Jug Nouveau screens: their own engraved art as the surface, the family's palette and plates. */
	static final MagicTheme ALCHEMY = new MagicTheme("alchemy", DARK, DARK, null, null);
}
