package io.github.jimbozoomer.jugcraft.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

/**
 * A magic interface button (docs/features/magic-interfaces.md, spec 2.8): a plate, paper strip, gold arrow, action icon
 * or bookmark from the owner's kit, with an optional glyph, mark or item. Its <b>message is always the whole label</b>
 * (narration, the tests' keys, Fabric's clickScreenButton), whatever it draws: a short label, or none at all for an icon.
 * Hover and keyboard focus light it (the outline sprite); a disabled one greys; a selected plate becomes the primary plate
 * with its tick (the answer kept). Its tip is drawn in the gold-frame style, and a footer button's tip opens below the
 * footer or beside the button, never over the body and its price (critique 2a).
 */
class MagicButton extends Button implements MagicScreen.Drawn {
	/** The kit's button faces: their sprites by state, their size (0: as wide as asked), their label's caps and ink. */
	enum Look {
		PLATE(new WidgetSprites(MagicArt.sprite("plate"), MagicArt.sprite("plate_disabled"), MagicArt.sprite("plate_highlighted"),
				MagicArt.sprite("plate_disabled")), null, 0, MagicArt.PLATE_HEIGHT, 3, 7, true),
		PRIMARY(new WidgetSprites(MagicArt.sprite("plate_primary"), MagicArt.sprite("plate_disabled"),
				MagicArt.sprite("plate_primary_highlighted"), MagicArt.sprite("plate_disabled")), null, 0, MagicArt.PLATE_HEIGHT, 3, 7, true),
		PAPER_ACCEPT(paper("accept"), null, 0, 15, 16, 5, false),
		PAPER_DECLINE(paper("decline"), null, 0, 15, 16, 5, false),
		ARROW_LEFT(arrow("left"), null, 13, 32, 0, 0, true),
		ARROW_RIGHT(arrow("right"), null, 13, 32, 0, 0, true),
		ACTION_END(action("end"), null, 14, 13, 0, 0, true),
		ACTION_SUSPEND(action("suspend"), null, 14, 13, 0, 0, true),
		ACTION_REFRESH(action("refresh"), null, 14, 13, 0, 0, true),
		ACTION_OFFER(action("offer"), null, 14, 13, 0, 0, true),
		BOOKMARK_FAMILIAR(bookmark("familiar"), bookmark("familiar_selected"), 23, 20, 0, 0, true),
		BOOKMARK_DOCUMENTATION(bookmark("documentation"), bookmark("documentation_selected"), 23, 20, 0, 0, true),
		BOOKMARK_SUMMON_CIRCLE(bookmark("summon_circle"), bookmark("summon_circle_selected"), 23, 20, 0, 0, true),
		BOOKMARK_SPELL_STYLE(bookmark("spell_style"), bookmark("spell_style_selected"), 23, 20, 0, 0, true),
		BOOKMARK_SETTINGS(bookmark("settings"), bookmark("settings_selected"), 23, 20, 0, 0, true);

		final WidgetSprites sprites;
		final @Nullable WidgetSprites selected;
		final int width;
		final int height;
		final int left;
		final int right;
		/** Whether its label is cream with a shadow (wood) rather than ink (paper). */
		final boolean dark;

		Look(WidgetSprites sprites, @Nullable WidgetSprites selected, int width, int height, int left, int right, boolean dark) {
			this.sprites = sprites;
			this.selected = selected;
			this.width = width;
			this.height = height;
			this.left = left;
			this.right = right;
			this.dark = dark;
		}

		boolean bookmark() {
			return selected != null;
		}

		private static WidgetSprites paper(String name) {
			Identifier face = MagicArt.sprite("paper/" + name);
			return new WidgetSprites(face, face, MagicArt.sprite("paper/" + name + "_highlighted"), face);
		}

		private static WidgetSprites arrow(String side) {
			return new WidgetSprites(MagicArt.sprite("arrow/" + side), MagicArt.sprite("arrow/" + side + "_disabled"),
					MagicArt.sprite("arrow/" + side + "_highlighted"), MagicArt.sprite("arrow/" + side + "_disabled"));
		}

		private static WidgetSprites action(String name) {
			return new WidgetSprites(MagicArt.sprite("action/" + name), MagicArt.sprite("action/" + name + "_inactive"),
					MagicArt.sprite("action/" + name + "_highlighted"), MagicArt.sprite("action/" + name + "_inactive"));
		}

		private static WidgetSprites bookmark(String name) {
			return new WidgetSprites(MagicArt.sprite("bookmark/" + name));
		}
	}

	/** Room round a label beyond the face's caps: two each side. */
	static final int LABEL_MARGIN = 4;
	private static final int GAP = 3;
	private final Look look;
	private final Component label;
	private MagicArt.@Nullable Glyph glyph;
	private MagicArt.@Nullable Mark mark;
	private ItemStack icon = ItemStack.EMPTY;
	private boolean selected;
	private List<FormattedCharSequence> tip = List.of();
	private Component hint = Component.empty();
	private Component narration = Component.empty();
	private @Nullable ClientTooltipPositioner positioner;

	/**
	 * A button whose message is {@code message} (the whole label) and which draws {@code label} (a short one, or empty
	 * for an icon alone).
	 */
	MagicButton(Look look, Component message, Component label, OnPress onPress) {
		super(0, 0, look.width > 0 ? look.width : 40, look.height, message, onPress, DEFAULT_NARRATION);
		this.look = look;
		this.label = label;
	}

	Look look() {
		return look;
	}

	MagicButton glyph(MagicArt.Glyph glyph) {
		this.glyph = glyph;
		return this;
	}

	MagicButton mark(MagicArt.Mark mark) {
		this.mark = mark;
		return this;
	}

	MagicButton icon(ItemStack icon) {
		this.icon = icon;
		return this;
	}

	/** A plate that is the choice kept (the matter's kept answer): drawn as the primary plate with its tick. */
	MagicButton selected(boolean selected) {
		this.selected = selected;
		return this;
	}

	/** Its tip (one short line, in the gold frame) and what narration adds after it (empty: the tip alone). */
	MagicButton hint(Component hint, Component narration) {
		this.hint = hint;
		this.narration = narration;
		this.tip = hint.getString().isEmpty() ? List.of() : Minecraft.getInstance().font.split(hint, 200);
		return this;
	}

	/** Places its tip for the footer row {@code band}: below it when there is room, else beside the button on the band. */
	void footer(ScreenRectangle band) {
		positioner = new FooterTip(band, this);
	}

	boolean isSelected() {
		return selected;
	}

	/** The width this button needs to draw its label whole: its caps, its icon and its words. */
	int labelWidth(Font font) {
		if (look.width > 0) {
			return look.width;
		}
		return look.left + look.right + LABEL_MARGIN + contentWidth(font);
	}

	private int contentWidth(Font font) {
		int iconWidth = !icon.isEmpty() ? 16 : glyph != null ? glyph.width : shownMark() != null ? shownMark().width : 0;
		String words = label.getString();
		return iconWidth + (iconWidth > 0 && !words.isEmpty() ? GAP : 0) + font.width(words);
	}

	private MagicArt.@Nullable Mark shownMark() {
		return mark != null ? mark : look == Look.PRIMARY || selected && look == Look.PLATE ? MagicArt.Mark.TICK : null;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		boolean lit = isHoveredOrFocused();
		WidgetSprites faces = selected && look.selected != null ? look.selected
				: selected && look == Look.PLATE ? Look.PRIMARY.sprites : look.sprites;
		// A bookmark lifts when lit (spec 2.8); paper fades when disabled.
		int y = getY() - (look.bookmark() && lit ? 1 : 0);
		float alpha = !active && !look.dark ? 0.5F : 1.0F;
		g.blitSprite(RenderPipelines.GUI_TEXTURED, faces.get(active, lit), getX(), y, getWidth(), getHeight(), alpha);
		Font font = Minecraft.getInstance().font;
		int content = contentWidth(font);
		if (content == 0) {
			return;
		}
		int x = getX() + look.left + (getWidth() - look.left - look.right - content) / 2;
		int mid = y + getHeight() / 2;
		if (!icon.isEmpty()) {
			g.item(icon, x, mid - 8);
			x += 16 + GAP;
		} else if (glyph != null) {
			MagicArt.glyph(g, glyph, x, mid - MagicArt.Glyph.HEIGHT / 2 - 1, active ? ink() : MagicTheme.GREY);
			x += glyph.width + GAP;
		} else if (shownMark() != null) {
			MagicArt.mark(g, shownMark(), x, mid - MagicArt.Mark.HEIGHT / 2 - 1);
			x += shownMark().width + GAP;
		}
		String words = label.getString();
		if (!words.isEmpty()) {
			g.text(font, words, x, mid - 4, active ? ink() : MagicTheme.GREY, look.dark && active);
		}
	}

	private int ink() {
		return look.dark ? MagicTheme.CREAM : MagicTheme.INK;
	}

	@Override
	protected void extractTooltipForNextRenderPass(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		MagicArt.tip(g, this, tip, positioner, mouseX, mouseY);
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
		if (!hint.getString().isEmpty()) {
			output.add(NarratedElementType.HINT, narration.getString().isEmpty() ? hint : Component.empty().append(hint).append(". ").append(narration));
		}
	}

	@Override
	public String drawnText() {
		return label.getString();
	}

	/**
	 * The footer's tooltip placement (spec 2.8): below the footer when the tip fits between it and the screen's edge;
	 * otherwise on the footer's band beside the button (right, else left); never above the footer's top edge, so the body
	 * and its price stay in view while Accept is hovered.
	 */
	static final class FooterTip implements ClientTooltipPositioner {
		/** Vanilla draws a tip's frame 3 beyond its words, and the gold rim a little further: keep this clear. */
		private static final int PAD = 5;
		private final ScreenRectangle band;
		private final MagicButton button;

		FooterTip(ScreenRectangle band, MagicButton button) {
			this.band = band;
			this.button = button;
		}

		@Override
		public Vector2ic positionTooltip(int screenWidth, int screenHeight, int x, int y, int tooltipWidth, int tooltipHeight) {
			int left = Math.max(PAD, Math.min(button.getX(), screenWidth - PAD - tooltipWidth));
			int below = band.bottom() + PAD;
			if (below + tooltipHeight + PAD <= screenHeight) {
				return new Vector2i(left, below);
			}
			int top = band.top() + PAD;
			int right = button.getX() + button.getWidth() + PAD;
			if (right + tooltipWidth + PAD <= screenWidth) {
				return new Vector2i(right, top);
			}
			int before = button.getX() - PAD - tooltipWidth;
			return new Vector2i(before >= PAD ? before : Math.max(PAD, screenWidth - PAD - tooltipWidth), top);
		}
	}
}
