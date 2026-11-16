package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.guide.encyclopedia.ProgressionRoutes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * The Encyclopedia's route map (the routes screen's Map tab): each group of a route's steps under its heading, the steps
 * as item tiles in order, and lines from each step to the earlier steps it needs. A tile's frame says how much it matters
 * without relying on colour: a solid frame is required, a dotted frame recommended, corner brackets an alternative, and a
 * double frame with a star a milestone. The selected step's own lines are drawn solid (what it needs) or dashed (what it
 * may use instead); the others stay faint, so the map stays readable. Tiles are ordinary buttons: Tab, the arrow keys,
 * Enter and the narrator work as on every other page of the book.
 */
final class EncyclopediaRouteMap {
	static final int NODE = 22;
	private static final int GAP = 5;
	private static final int ROW_GAP = 9;
	private static final int HEADER = 15;
	private static final int GROUP_GAP = 7;
	private static final int FAINT = 0x50876D43;
	private static final int RECOMMENDED = 0xFF3F7FA8;
	private static final int ALTERNATIVE = 0xFFB86A1E;
	private static final int LIT = 0xFFE6D3AD;
	private static final int PAPER = 0x40C7A76C;

	private EncyclopediaRouteMap() {
	}

	/** Where a step's tile goes: across from the map's left, and down from the top of the content. */
	record Place(int dx, int dy) {
	}

	/** A group's heading, down from the top of the content. */
	record Heading(ProgressionRoutes.Group group, int dy) {
	}

	/** A laid-out route: one place per step (in the route's order), the headings and the content's height. */
	record Layout(List<Place> places, List<Heading> headings, int height) {
	}

	/** Lays {@code route} out in a column {@code width} pixels wide: as many tiles in a row as fit, then the next row. */
	static Layout layout(ProgressionRoutes.Route route, int width) {
		int columns = Math.max(1, (width + GAP) / (NODE + GAP));
		List<Place> places = new ArrayList<>();
		List<Heading> headings = new ArrayList<>();
		int y = 0;
		for (ProgressionRoutes.Group group : route.groups()) {
			headings.add(new Heading(group, y));
			y += HEADER;
			List<ProgressionRoutes.Step> steps = group.steps();
			for (int i = 0; i < steps.size(); i++) {
				places.add(new Place((i % columns) * (NODE + GAP), y + (i / columns) * (NODE + ROW_GAP)));
			}
			int rows = Math.max(1, (steps.size() + columns - 1) / columns);
			y += rows * NODE + (rows - 1) * ROW_GAP + GROUP_GAP;
		}
		return new Layout(List.copyOf(places), List.copyOf(headings), Math.max(1, y));
	}

	/** A step's name: its item's, or a milestone's title. */
	static Component name(ProgressionRoutes.Step step) {
		return step.milestone() ? Component.literal(step.title()) : ProgressionRoutes.item(step.item()).getDefaultInstance().getHoverName();
	}

	/**
	 * The headings and lines, drawn under the tiles. It takes no keyboard focus and says nothing itself: each tile says
	 * its own name, and the details page names what a step needs in words.
	 */
	static final class Canvas extends AbstractWidget {
		private final Font font;
		private final ProgressionRoutes.Route route;
		private final Layout layout;
		private final List<Node> nodes;
		private final Map<String, Integer> index = new HashMap<>();
		private final int selected;
		private final int accent;

		Canvas(int x, int width, Font font, ProgressionRoutes.Route route, Layout layout, List<Node> nodes, int selected) {
			super(x, 0, width, layout.height(), CommonComponents.EMPTY);
			this.font = font;
			this.route = route;
			this.layout = layout;
			this.nodes = nodes;
			this.selected = selected;
			this.accent = route.accent();
			List<ProgressionRoutes.Step> steps = route.steps();
			for (int i = 0; i < steps.size(); i++) {
				index.put(steps.get(i).id(), i);
			}
			active = false;
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
			for (Heading heading : layout.headings()) {
				int y = getY() + heading.dy();
				int x = getX();
				if (heading.group().milestone()) {
					BookArt.star(g, x + 3, y + 4, 3, BookArt.GOLD);
					x += 9;
				}
				g.text(font, BookArt.fit(font, heading.group().title(), getWidth() - (x - getX()) - 2), x, y, BookArt.INK, false);
				g.fill(x, y + 10, getX() + getWidth() - 4, y + 11, heading.group().milestone() ? accent : FAINT);
			}
			List<ProgressionRoutes.Step> steps = route.steps();
			// Faint lines first, then the selected step's own lines above them.
			for (int pass = 0; pass < 2; pass++) {
				for (int i = 0; i < steps.size() && i < nodes.size(); i++) {
					ProgressionRoutes.Step step = steps.get(i);
					for (String need : step.needs()) {
						connect(g, need, i, pass, false);
					}
					for (String need : step.altNeeds()) {
						connect(g, need, i, pass, true);
					}
				}
			}
		}

		private void connect(GuiGraphicsExtractor g, String need, int to, int pass, boolean alternative) {
			Integer from = index.get(need);
			if (from == null || from >= nodes.size()) {
				return;
			}
			boolean own = to == selected || from == selected;
			if (own != (pass == 1)) {
				return;
			}
			int colour = !own ? FAINT : alternative ? ALTERNATIVE : BookArt.INK;
			Node a = nodes.get(from);
			Node b = nodes.get(to);
			int ax = a.getX() + NODE / 2;
			int ay = a.getY() + NODE / 2;
			int bx = b.getX() + NODE / 2;
			int by = b.getY() + NODE / 2;
			boolean dashed = alternative;
			if (ay == by) {
				segment(g, Math.min(ax, bx), ay, Math.max(ax, bx), ay, colour, dashed);
				return;
			}
			// Down from the earlier tile, across in the gap above the later tile's row, then down into it.
			int turn = by - NODE / 2 - ROW_GAP / 2 - 1;
			segment(g, ax, Math.min(ay, turn), ax, Math.max(ay, turn), colour, dashed);
			segment(g, Math.min(ax, bx), turn, Math.max(ax, bx), turn, colour, dashed);
			segment(g, bx, Math.min(turn, by), bx, Math.max(turn, by), colour, dashed);
			if (own && !alternative) {
				// An arrowhead into the later tile.
				g.fill(bx - 2, b.getY() - 3, bx + 3, b.getY() - 2, colour);
				g.fill(bx - 1, b.getY() - 2, bx + 2, b.getY() - 1, colour);
			}
		}

		/** A one-pixel horizontal or vertical segment, solid or dashed (two on, two off). */
		private static void segment(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int colour, boolean dashed) {
			if (!dashed) {
				g.fill(x0, y0, x1 + 1, y1 + 1, colour);
				return;
			}
			if (y0 == y1) {
				for (int x = x0; x <= x1; x += 4) {
					g.fill(x, y0, Math.min(x + 2, x1 + 1), y0 + 1, colour);
				}
			} else {
				for (int y = y0; y <= y1; y += 4) {
					g.fill(x0, y, x0 + 1, Math.min(y + 2, y1 + 1), colour);
				}
			}
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
		}
	}

	/** One step's tile: its item, framed by its weight; pressing it selects the step. */
	static final class Node extends Button {
		private final ProgressionRoutes.Step step;
		private final ItemStack icon;
		private final boolean selected;
		private final int accent;

		Node(int x, ProgressionRoutes.Step step, boolean selected, int accent, Component role, OnPress action) {
			super(x, 0, NODE, NODE, name(step), action, DEFAULT_NARRATION);
			this.step = step;
			this.selected = selected;
			this.accent = accent;
			this.icon = ProgressionRoutes.item(step.item()).getDefaultInstance();
			setTooltip(Tooltip.create(name(step).copy().append("\n").append(role)));
		}

		ProgressionRoutes.Step step() {
			return step;
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
			int x = getX();
			int y = getY();
			boolean lit = selected || isHovered() || isFocused();
			g.fill(x, y, x + NODE, y + NODE, lit ? LIT : PAPER);
			if (step.milestone()) {
				g.outline(x, y, NODE, NODE, BookArt.GOLD);
				g.outline(x + 2, y + 2, NODE - 4, NODE - 4, BookArt.GOLD);
			} else {
				switch (step.role()) {
					case "recommended" -> dotted(g, x, y, RECOMMENDED);
					case "alternative" -> corners(g, x, y, ALTERNATIVE);
					default -> g.outline(x, y, NODE, NODE, BookArt.INK);
				}
			}
			if (!icon.isEmpty()) {
				g.item(icon, x + 3, y + 3);
			}
			if (step.milestone()) {
				BookArt.star(g, x + NODE - 3, y + 3, 3, BookArt.GOLD);
			}
			if (selected) {
				g.outline(x - 2, y - 2, NODE + 4, NODE + 4, accent);
			}
			if (isFocused()) {
				g.outline(x - 1, y - 1, NODE + 2, NODE + 2, BookArt.INK);
			}
		}

		private static void dotted(GuiGraphicsExtractor g, int x, int y, int colour) {
			for (int i = 0; i < NODE; i += 2) {
				g.fill(x + i, y, x + i + 1, y + 1, colour);
				g.fill(x + i, y + NODE - 1, x + i + 1, y + NODE, colour);
				g.fill(x, y + i, x + 1, y + i + 1, colour);
				g.fill(x + NODE - 1, y + i, x + NODE, y + i + 1, colour);
			}
		}

		private static void corners(GuiGraphicsExtractor g, int x, int y, int colour) {
			int arm = 6;
			int r = x + NODE;
			int b = y + NODE;
			g.fill(x, y, x + arm, y + 1, colour);
			g.fill(x, y, x + 1, y + arm, colour);
			g.fill(r - arm, y, r, y + 1, colour);
			g.fill(r - 1, y, r, y + arm, colour);
			g.fill(x, b - 1, x + arm, b, colour);
			g.fill(x, b - arm, x + 1, b, colour);
			g.fill(r - arm, b - 1, r, b, colour);
			g.fill(r - 1, b - arm, r, b, colour);
		}
	}
}
