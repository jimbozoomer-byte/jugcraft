package io.github.jimbozoomer.jugcraft.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * A focusable, narrated paragraph of a magic screen (docs/features/magic-interfaces.md), in place of BookArt.Paragraph:
 * its <b>message is the whole sentence</b> (narration, the tests), and it draws {@code shown}, which may be a short
 * glance of it; when they differ, or when the lines are clamped with "…", the whole sentence is its tip (hover or
 * keyboard focus, in the gold frame). Ink has no shadow on parchment; cream on dark has vanilla's. A leading glyph, mark
 * or item may stand before it, and on the ruled scroll its lines keep the rules' pitch of fourteen.
 */
final class MagicText extends AbstractWidget implements MagicScreen.Drawn {
	static final int PITCH = 10;
	static final int RULED_PITCH = 14;
	private static final int GAP = 3;
	private static final int TIP_WIDTH = 200;
	private final Font font;
	private final Component shown;
	private final int colour;
	private final boolean shadow;
	private int pitch = PITCH;
	private int maxLines = Integer.MAX_VALUE;
	private int focusColour;
	private MagicArt.@Nullable Glyph glyph;
	private MagicArt.@Nullable Mark mark;
	private ItemStack item = ItemStack.EMPTY;
	private Component usage = Component.empty();
	private List<FormattedCharSequence> lines = List.of();
	private List<FormattedCharSequence> tip = List.of();
	private String drawn = "";

	/** A paragraph whose message is {@code full} and which draws {@code shown} in {@code width}, in {@code inks}' {@code colour}. */
	MagicText(Component full, Component shown, Font font, int width, int colour, MagicTheme.Inks inks) {
		super(0, 0, Math.max(20, width), 1, full);
		this.font = font;
		this.shown = shown;
		this.colour = colour;
		this.shadow = inks.shadow();
		this.focusColour = inks.focus();
		layout();
	}

	/** A paragraph that draws its whole message. */
	MagicText(Component full, Font font, int width, int colour, MagicTheme.Inks inks) {
		this(full, full, font, width, colour, inks);
	}

	/** On the ruled scroll: lines fourteen apart, as its rules are (spec 2.2). */
	MagicText ruled() {
		pitch = RULED_PITCH;
		layout();
		return this;
	}

	/** At most {@code lines} lines, the last cut with "…" (the whole sentence then in its tip). */
	MagicText maxLines(int lines) {
		maxLines = Math.max(1, lines);
		layout();
		return this;
	}

	MagicText lead(MagicArt.Glyph glyph) {
		this.glyph = glyph;
		layout();
		return this;
	}

	MagicText lead(MagicArt.Mark mark) {
		this.mark = mark;
		layout();
		return this;
	}

	MagicText lead(ItemStack item) {
		this.item = item;
		layout();
		return this;
	}

	/** What narration adds after its words (an item grid's names, as the encyclopedia did with BookArt.Paragraph). */
	MagicText usage(Component usage) {
		this.usage = usage;
		return this;
	}

	private int leadWidth() {
		return !item.isEmpty() ? 16 + GAP : glyph != null ? glyph.width + GAP : mark != null ? mark.width + GAP : 0;
	}

	private void layout() {
		List<FormattedCharSequence> all = font.split(shown, Math.max(10, getWidth() - leadWidth() - 2));
		boolean clamped = all.size() > maxLines;
		List<FormattedCharSequence> kept = new ArrayList<>(all.subList(0, Math.min(all.size(), maxLines)));
		StringBuilder words = new StringBuilder();
		if (clamped) {
			// The last kept line, cut to leave room for "…", from the plain text of what is shown.
			List<FormattedText> plain = font.getSplitter().splitLines(shown, Math.max(10, getWidth() - leadWidth() - 2), Style.EMPTY);
			String last = MagicArt.fit(font, plain.get(maxLines - 1).getString() + " " + plain.get(maxLines).getString(),
					Math.max(10, getWidth() - leadWidth() - 2));
			kept.set(maxLines - 1, FormattedCharSequence.forward(last, Style.EMPTY));
			for (int i = 0; i < maxLines - 1; i++) {
				words.append(plain.get(i).getString()).append(' ');
			}
			words.append(last);
		} else {
			words.append(shown.getString());
		}
		lines = List.copyOf(kept);
		drawn = words.toString();
		boolean shortened = clamped || !shown.getString().equals(getMessage().getString());
		tip = shortened ? font.split(getMessage(), TIP_WIDTH) : List.of();
		int leadHeight = !item.isEmpty() ? 16 : mark != null ? MagicArt.Mark.HEIGHT : 0;
		setHeight(Math.max(leadHeight, Math.max(1, lines.size()) * pitch) + 2);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		int x = getX() + 1;
		int y = getY() + 1;
		if (!item.isEmpty()) {
			g.item(item, x, y + (pitch - 16) / 2);
		} else if (glyph != null) {
			MagicArt.glyph(g, glyph, x, y, colour);
		} else if (mark != null) {
			MagicArt.mark(g, mark, x, y - 1);
		}
		int tx = x + leadWidth();
		int ty = y + Math.max(0, (pitch - font.lineHeight) / 2);
		for (FormattedCharSequence line : lines) {
			g.text(font, line, tx, ty, colour, shadow);
			ty += pitch;
		}
		if (isFocused()) {
			// One pixel under its words while focused (spec 2.8).
			g.fill(tx, getY() + getHeight() - 1, getX() + getWidth() - 1, getY() + getHeight(), focusColour);
		}
	}

	@Override
	protected void extractTooltipForNextRenderPass(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		MagicArt.tip(g, this, tip, null, mouseX, mouseY);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, getMessage());
		if (!usage.getString().isEmpty()) {
			output.add(NarratedElementType.USAGE, usage);
		}
	}

	@Override
	public String drawnText() {
		return drawn;
	}
}
