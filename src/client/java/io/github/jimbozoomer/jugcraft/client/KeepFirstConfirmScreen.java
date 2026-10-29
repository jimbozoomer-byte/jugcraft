package io.github.jimbozoomer.jugcraft.client;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;

/**
 * Concordance Chunk D (D-06, D-07): vanilla's confirmation whose keyboard starts on its "no", so Enter on arrival keeps what
 * it asks about (Esc keeps it too, ConfirmScreen's own rule). The pact screen's End and the roster's End ask through it.
 * <p>
 * The pact's (docs/features/magic-interfaces.md, spec 2.12) is drawn on the magic interfaces' scroll over the cosmos: its
 * question and two short lines in ink, End the pact (with its cross) and Keep it on wooden plates. It stays vanilla's
 * confirmation: its narration is its question and its whole message, and only the words drawn are shorter. Since the
 * design's unit 2 the roster's End asks on the same scroll ({@link #magic}).
 */
public final class KeepFirstConfirmScreen extends ConfirmScreen {
	private static final int SCROLL_WIDTH = 300;
	private static final int SCROLL_HEIGHT = 92;
	/** The short lines drawn on the scroll (empty: vanilla's look). */
	private final List<Component> lines;
	private int scrollX;
	private int scrollY;

	KeepFirstConfirmScreen(BooleanConsumer callback, Component title, Component message, Component yes, Component no) {
		this(callback, title, message, List.of(), yes, no);
	}

	private KeepFirstConfirmScreen(BooleanConsumer callback, Component title, Component message, List<Component> lines, Component yes, Component no) {
		super(callback, title, message, yes, no);
		this.lines = List.copyOf(lines);
	}

	/** An ending on the scroll (the pact's, the roster's): {@code title} and {@code lines} drawn, {@code message} (the whole sentence) narrated. */
	static KeepFirstConfirmScreen magic(BooleanConsumer callback, Component title, Component message, List<Component> lines, Component yes, Component no) {
		return new KeepFirstConfirmScreen(callback, title, message, lines, yes, no);
	}

	private boolean magic() {
		return !lines.isEmpty();
	}

	@Override
	protected void init() {
		super.init();
		if (magic()) {
			// The question is drawn on the scroll in its heading ink (extractBackground), not as vanilla's white line.
			layout.visitWidgets(widget -> {
				if (widget instanceof StringWidget words && words.getMessage() == title) {
					words.visible = false;
				}
			});
		}
	}

	@Override
	protected LayoutElement addMessage() {
		// The scroll's lines are drawn by extractBackground; this keeps their room.
		return magic() ? new SpacerElement(SCROLL_WIDTH - 40, lines.size() * 14) : super.addMessage();
	}

	@Override
	protected void addButtons(LinearLayout buttonLayout) {
		if (!magic()) {
			super.addButtons(buttonLayout);
			return;
		}
		MagicButton yes = new MagicButton(MagicButton.Look.PLATE, yesButtonComponent, yesButtonComponent, unused -> callback.accept(true))
				.glyph(MagicArt.Glyph.CROSS);
		MagicButton no = new MagicButton(MagicButton.Look.PLATE, noButtonComponent, noButtonComponent, unused -> callback.accept(false));
		yes.setWidth(yes.labelWidth(font) + 8);
		no.setWidth(Math.max(60, no.labelWidth(font) + 8));
		yesButton = buttonLayout.addChild(yes);
		noButton = buttonLayout.addChild(no);
	}

	@Override
	protected void repositionElements() {
		if (!magic()) {
			super.repositionElements();
			return;
		}
		int width = Math.min(SCROLL_WIDTH, this.width - 20);
		scrollX = (this.width - width) / 2;
		scrollY = Math.max(4, (height - SCROLL_HEIGHT) / 2 - 14);
		int y = scrollY + SCROLL_HEIGHT + 6;
		int total = yesButton.getWidth() + 8 + noButton.getWidth();
		yesButton.setPosition((this.width - total) / 2, y);
		noButton.setPosition((this.width - total) / 2 + yesButton.getWidth() + 8, y);
	}

	@Override
	protected void setInitialFocus() {
		// Vanilla's own initial focus acts only for keyboard input too (Screen.setInitialFocus); a mouse user starts unfocused.
		if (noButton != null && minecraft.getLastInputType().isKeyboard()) {
			setInitialFocus(noButton);
		} else {
			super.setInitialFocus();
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		if (!magic()) {
			super.extractBackground(g, mouseX, mouseY, delta);
			return;
		}
		MagicArt.frame();
		g.fill(0, 0, width, height, 0xA0080610);
		MagicArt.cosmos(g, width / 2, height / 2);
		int w = Math.min(SCROLL_WIDTH, width - 20);
		MagicArt.scroll(g, scrollX, scrollY, w, SCROLL_HEIGHT, false);
		Component question = title.copy().withStyle(ChatFormatting.BOLD);
		g.text(font, question, (width - font.width(question)) / 2, scrollY + 18, MagicTheme.HEADING, false);
		int y = scrollY + 36;
		for (int i = 0; i < lines.size(); i++) {
			Component line = lines.get(i);
			g.text(font, line, (width - font.width(line)) / 2, y, i == 0 ? MagicTheme.INK : MagicTheme.DIM, false);
			y += 14;
		}
		// Spirits' subtitles over this screen too (deferred to here, as vanilla's Screen.extractBackground does).
		minecraft.gui.hud.extractDeferredSubtitles();
	}

	/** Every string it draws (its question, its lines and its buttons' labels: spec 1.1's budget of 25), for tests. */
	public List<String> drawn() {
		List<String> out = new ArrayList<>();
		out.add(title.getString());
		lines.forEach(line -> out.add(line.getString()));
		if (yesButton != null) {
			out.add(yesButton.getMessage().getString());
		}
		if (noButton != null) {
			out.add(noButton.getMessage().getString());
		}
		return out;
	}
}
