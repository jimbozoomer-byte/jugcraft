package io.github.jimbozoomer.jugcraft.client;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * A term chip (docs/features/magic-interfaces.md, spec 1.2 and 2.5): a glyph, a state mark or an item stack, and a
 * short value (a glance). Its <b>message is the server's whole sentence</b>: its narration and, in the gold frame, its
 * tip on hover or keyboard focus; it keeps its key on the screen (term:&lt;key&gt;, note:N). A stack cycles through its
 * items once a second (an offering tag's), still under reduced motion; a chip may be boxed in red when it is short (the
 * resonance chip's "6/8 resonance").
 */
final class MagicChip extends AbstractWidget implements MagicScreen.Drawn {
	private static final int GAP = 3;
	/** A tip's width: most sentences; the service chip's whole sentence is wrapped wider (spec 1.1). */
	static final int TIP_WIDTH = 200;
	static final int WIDE_TIP = 280;
	private final Font font;
	private final Component shown;
	private final int colour;
	private final boolean shadow;
	private final int focusColour;
	private MagicArt.@Nullable Glyph glyph;
	private MagicArt.@Nullable Mark mark;
	private List<ItemStack> items = List.of();
	private int box;
	private List<FormattedCharSequence> tip;
	private @Nullable ClientTooltipPositioner positioner;

	/** A chip whose message is {@code full} and which draws {@code shown}, in {@code inks}' {@code colour}. */
	MagicChip(Component full, Component shown, Font font, int colour, MagicTheme.Inks inks) {
		super(0, 0, 1, 1, full);
		this.font = font;
		this.shown = shown;
		this.colour = colour;
		this.shadow = inks.shadow();
		this.focusColour = inks.focus();
		this.tip = font.split(full, TIP_WIDTH);
		size();
	}

	/** A chip from a glance: its mark (if any), its short words, its whole sentence. */
	static MagicChip of(PactGlance.Glance glance, Font font, int colour, MagicTheme.Inks inks) {
		MagicChip chip = new MagicChip(glance.full(), glance.shown(), font, colour, inks);
		return glance.mark() == null ? chip : chip.mark(glance.mark());
	}

	MagicChip glyph(MagicArt.Glyph glyph) {
		this.glyph = glyph;
		size();
		return this;
	}

	MagicChip mark(MagicArt.Mark mark) {
		this.mark = mark;
		size();
		return this;
	}

	/** The stack it shows, cycled once a second when there are several (an offering tag's items). */
	MagicChip items(List<ItemStack> items) {
		this.items = List.copyOf(items);
		size();
		return this;
	}

	/** Boxed in {@code colour} (a short resonance chip; 0: not boxed). */
	MagicChip box(int colour) {
		this.box = colour;
		return this;
	}

	/** Wraps its tip at {@code width} (the service chip carries the whole service sentence at 280). */
	MagicChip tipWidth(int width) {
		this.tip = font.split(getMessage(), width);
		return this;
	}

	/** Places its tip (clear of the other chips, where the screen has room); null: vanilla's placement. */
	MagicChip at(@Nullable ClientTooltipPositioner positioner) {
		this.positioner = positioner;
		return this;
	}

	private int iconWidth() {
		return !items.isEmpty() ? 16 : glyph != null ? glyph.width : mark != null ? mark.width : 0;
	}

	private void size() {
		int icon = iconWidth();
		setWidth(icon + (icon > 0 ? GAP : 0) + font.width(shown));
		setHeight(!items.isEmpty() ? 16 : Math.max(font.lineHeight, mark != null ? MagicArt.Mark.HEIGHT : MagicArt.Glyph.HEIGHT));
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		int x = getX();
		int mid = getY() + getHeight() / 2;
		if (!items.isEmpty()) {
			int shownItem = ConcordanceClientOptions.reducedMotion() ? 0 : (int) (Util.getMillis() / 1000L % items.size());
			g.item(items.get(shownItem), x, getY());
		} else if (glyph != null) {
			MagicArt.glyph(g, glyph, x, mid - MagicArt.Glyph.HEIGHT / 2 - 1, colour);
		} else if (mark != null) {
			MagicArt.mark(g, mark, x, mid - MagicArt.Mark.HEIGHT / 2 - 1);
		}
		int tx = x + iconWidth() + (iconWidth() > 0 ? GAP : 0);
		g.text(font, shown, tx, mid - 4, colour, shadow);
		if (box != 0) {
			g.outline(tx - 2, mid - 6, font.width(shown) + 4, 12, box);
		}
		if (isFocused()) {
			g.fill(tx, getY() + getHeight(), tx + font.width(shown), getY() + getHeight() + 1, focusColour);
		}
	}

	@Override
	protected void extractTooltipForNextRenderPass(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		MagicArt.tip(g, this, tip, positioner, mouseX, mouseY);
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
