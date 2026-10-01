package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.agriculture.CarvePayload;
import io.github.jimbozoomer.jugcraft.agriculture.CarvingTemplates;
import io.github.jimbozoomer.jugcraft.agriculture.OpenCarvingPayload;
import io.github.jimbozoomer.jugcraft.agriculture.PumpkinCarving;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/**
 * The Carving Knife's screen: one side of a pumpkin as a 16x16 grid, a pixel per block texel. Left-drag
 * carves with the chosen tool (Cut through, Shave the skin, or Erase this session's strokes), right-drag
 * erases. A carving only goes deeper: what was carved before this screen opened is fixed. Mirror copies
 * every stroke to the other half; starter faces can be pressed in; the candle shows it lit. Done sends the
 * face to the server, which checks it and carves ({@link io.github.jimbozoomer.jugcraft.agriculture.PumpkinCarvings}).
 */
public class CarvingScreen extends Screen {
	private static final int SIZE = PumpkinCarving.SIZE;
	private static final int CELL = 10;
	private static final int GRID = SIZE * CELL;
	private static final int WIDTH = 304;
	private static final int HEIGHT = 222;
	private static final int PREVIEW_SCALE = 3;
	private static final int UNDO_LIMIT = 64;
	/**
	 * Minecraft 26.3 numbers mouse buttons from 1: 1 is left, 2 middle, 3 right ({@code MouseHandler.onButton};
	 * vanilla widgets take only button 1). Older versions counted from 0, so these are spelled out here.
	 */
	public static final int MOUSE_LEFT = 1;
	public static final int MOUSE_RIGHT = 3;

	private static final int RIND = 0xFF6B3A10;
	private static final int PANEL = 0xFF2B1A0E;
	private static final int PANEL_LIGHT = 0xFF4A2E16;
	private static final int TEXT = 0xFFF5D9A8;
	private static final int TEXT_DIM = 0xFFB89468;
	private static final int SELECTED = 0xFFFFC040;
	private static final int SKIN = 0xFFE08A2A;
	private static final int SKIN_RIB = 0xFFC0661A;

	private enum Tool {
		CUT, SHAVE, ERASE
	}

	private final BlockPos pos;
	private final Direction side;
	private final int[] original;
	private final int[] face;
	private final boolean freeDraw;
	private final Deque<int[]> undo = new ArrayDeque<>();
	private Tool tool = Tool.CUT;
	private int brush = 1;
	private boolean mirror;
	private boolean litPreview;
	private int template;
	private boolean painting;
	private boolean erasing;
	private Component notice = Component.empty();
	private int left;
	private int top;
	private int gridX;
	private int gridY;
	private final Button[] toolButtons = new Button[Tool.values().length];

	public CarvingScreen(OpenCarvingPayload payload) {
		super(Component.translatable("screen.jugcraft.carving"));
		this.pos = payload.pos();
		this.side = payload.side();
		this.original = payload.face().clone();
		this.face = payload.face().clone();
		this.freeDraw = payload.freeDraw();
		if (!freeDraw) {
			notice = Component.translatable("screen.jugcraft.carving.templates_only");
		}
	}

	@Override
	protected void init() {
		left = (width - WIDTH) / 2;
		top = (height - HEIGHT) / 2;
		gridX = left + 12;
		gridY = top + 24;
		int x = gridX + GRID + 12;
		int y = gridY;
		int column = WIDTH - (x - left) - 12;
		Tool[] tools = Tool.values();
		int toolWidth = (column - 4) / tools.length;
		for (int i = 0; i < tools.length; i++) {
			Tool each = tools[i];
			toolButtons[i] = addRenderableWidget(Button.builder(Component.translatable("screen.jugcraft.carving." + each.name().toLowerCase()),
					button -> tool = each).bounds(x + i * (toolWidth + 2), y, toolWidth, 16).build());
			toolButtons[i].active = freeDraw;
		}
		int half = (column - 2) / 2;
		Button brushButton = addRenderableWidget(Button.builder(brushLabel(), button -> {
			brush = brush % 3 + 1;
			button.setMessage(brushLabel());
		}).bounds(x, y + 20, half, 16).build());
		Button mirrorButton = addRenderableWidget(Button.builder(mirrorLabel(), button -> {
			mirror = !mirror;
			button.setMessage(mirrorLabel());
		}).bounds(x + half + 2, y + 20, half, 16).build());
		brushButton.active = freeDraw;
		mirrorButton.active = freeDraw;
		addRenderableWidget(Button.builder(templateLabel(), button -> {
			template = (template + 1) % CarvingTemplates.ALL.size();
			button.setMessage(templateLabel());
		}).bounds(x, y + 40, column - 36, 16).build());
		addRenderableWidget(Button.builder(Component.translatable("screen.jugcraft.carving.apply"), button -> applyTemplate())
				.bounds(x + column - 34, y + 40, 34, 16).build());
		addRenderableWidget(Button.builder(candleLabel(), button -> {
			litPreview = !litPreview;
			button.setMessage(candleLabel());
		}).bounds(x, y + 60, column, 16).build());
		addRenderableWidget(Button.builder(Component.translatable("screen.jugcraft.carving.undo"), button -> undo())
				.bounds(x, y + 80, half, 16).build());
		addRenderableWidget(Button.builder(Component.translatable("screen.jugcraft.carving.reset"), button -> {
			pushUndo();
			System.arraycopy(original, 0, face, 0, SIZE);
		}).bounds(x + half + 2, y + 80, half, 16).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> finish())
				.bounds(x, top + HEIGHT - 22, half, 16).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
				.bounds(x + half + 2, top + HEIGHT - 22, half, 16).build());
	}

	private Component brushLabel() {
		return Component.translatable("screen.jugcraft.carving.brush", brush);
	}

	private Component mirrorLabel() {
		return Component.translatable(mirror ? "screen.jugcraft.carving.mirror_on" : "screen.jugcraft.carving.mirror_off");
	}

	private Component templateLabel() {
		return Component.translatable("carving.jugcraft.template." + CarvingTemplates.ALL.get(template).id());
	}

	private Component candleLabel() {
		return Component.translatable(litPreview ? "screen.jugcraft.carving.candle_on" : "screen.jugcraft.carving.candle_off");
	}

	/** Sends the face to the server (only if anything changed) and closes. */
	private void finish() {
		if (!Arrays.equals(face, original)) {
			ClientPlayNetworking.send(new CarvePayload(pos, side, face.clone()));
		}
		onClose();
	}

	/** Presses the chosen starter face in, deepening what is there (with free drawing) or replacing it exactly. */
	private void applyTemplate() {
		int[] shape = CarvingTemplates.ALL.get(template).face();
		int[] result = new int[SIZE];
		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				int depth = freeDraw ? Math.max(PumpkinCarving.pixel(face[y], x), PumpkinCarving.pixel(shape[y], x)) : PumpkinCarving.pixel(shape[y], x);
				result[y] = PumpkinCarving.withPixel(result[y], x, depth);
			}
		}
		if (!PumpkinCarving.deepensOnly(original, result)) {
			notice = Component.translatable("screen.jugcraft.carving.does_not_fit");
			return;
		}
		pushUndo();
		System.arraycopy(result, 0, face, 0, SIZE);
	}

	private void pushUndo() {
		undo.push(face.clone());
		while (undo.size() > UNDO_LIMIT) {
			undo.removeLast();
		}
	}

	private void undo() {
		if (!undo.isEmpty()) {
			System.arraycopy(undo.pop(), 0, face, 0, SIZE);
		}
	}

	/** The middle of a grid cell in GUI coordinates (the client game test clicks the grid through it). */
	public double cellCentreX(int x) {
		return gridX + x * CELL + CELL / 2.0;
	}

	public double cellCentreY(int y) {
		return gridY + y * CELL + CELL / 2.0;
	}

	private boolean inGrid(double mouseX, double mouseY) {
		return mouseX >= gridX && mouseX < gridX + GRID && mouseY >= gridY && mouseY < gridY + GRID;
	}

	/** Applies the brush (and its mirror image) at the mouse. */
	private void paint(double mouseX, double mouseY) {
		if (!inGrid(mouseX, mouseY)) {
			return;
		}
		int cx = (int) Math.floor((mouseX - gridX) / CELL);
		int cy = (int) Math.floor((mouseY - gridY) / CELL);
		int low = -(brush - 1) / 2;
		int high = brush / 2;
		for (int dy = low; dy <= high; dy++) {
			for (int dx = low; dx <= high; dx++) {
				set(cx + dx, cy + dy);
				if (mirror) {
					set(SIZE - 1 - (cx + dx), cy + dy);
				}
			}
		}
	}

	private void set(int x, int y) {
		if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
			return;
		}
		int fixed = PumpkinCarving.pixel(original[y], x);
		int now = PumpkinCarving.pixel(face[y], x);
		int depth;
		if (erasing || tool == Tool.ERASE) {
			depth = fixed;
		} else if (tool == Tool.CUT) {
			depth = PumpkinCarving.CUT;
		} else {
			depth = Math.max(now, PumpkinCarving.SHAVED); // shaving never fills a hole back in
		}
		face[y] = PumpkinCarving.withPixel(face[y], x, Math.max(depth, fixed));
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (freeDraw && inGrid(event.x(), event.y()) && (event.button() == 0 || event.button() == 1)) {
			pushUndo();
			painting = true;
			erasing = event.button() == 1;
			paint(event.x(), event.y());
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (painting) {
			paint(event.x(), event.y());
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (painting) {
			painting = false;
			// A click that changed nothing leaves nothing to undo.
			if (!undo.isEmpty() && Arrays.equals(undo.peek(), face)) {
				undo.pop();
			}
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.hasControlDown() && event.key() == 90) {
			undo();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractBackground(graphics, mouseX, mouseY, delta);
		graphics.fill(left - 2, top - 2, left + WIDTH + 2, top + HEIGHT + 2, RIND);
		graphics.fill(left, top, left + WIDTH, top + HEIGHT, PANEL);
		graphics.fill(left, top, left + WIDTH, top + 1, PANEL_LIGHT);
		graphics.fill(gridX - 2, gridY - 2, gridX + GRID + 2, gridY + GRID + 2, RIND);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		graphics.text(font, title, left + 12, top + 9, TEXT, false);

		// The face, a cell per texel, with faint lines between cells.
		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				int cx = gridX + x * CELL;
				int cy = gridY + y * CELL;
				graphics.fill(cx, cy, cx + CELL, cy + CELL, color(x, y));
				graphics.fill(cx, cy, cx + CELL, cy + 1, 0x22000000);
				graphics.fill(cx, cy, cx + 1, cy + CELL, 0x22000000);
			}
		}
		// The brush under the mouse, and its mirror image.
		if (freeDraw && inGrid(mouseX, mouseY)) {
			int cx = (mouseX - gridX) / CELL;
			int cy = (mouseY - gridY) / CELL;
			int low = -(brush - 1) / 2;
			int span = brush * CELL;
			graphics.outline(gridX + (cx + low) * CELL, gridY + (cy + low) * CELL, span, span, 0xCCFFFFFF);
			if (mirror) {
				int mx = SIZE - 1 - (cx + low + brush - 1);
				graphics.outline(gridX + mx * CELL, gridY + (cy + low) * CELL, span, span, 0x88FFFFFF);
			}
		}
		// The selected tool.
		Button selected = toolButtons[tool.ordinal()];
		if (freeDraw) {
			graphics.outline(selected.getX() - 1, selected.getY() - 1, selected.getWidth() + 2, selected.getHeight() + 2, SELECTED);
		}

		// Actual size, as it will look on the pumpkin.
		int px = gridX + GRID + 12;
		int py = gridY + 102;
		graphics.text(font, Component.translatable("screen.jugcraft.carving.preview"), px, py, TEXT_DIM, false);
		int size = SIZE * PREVIEW_SCALE;
		graphics.fill(px - 1, py + 11, px + size + 1, py + 12 + size, RIND);
		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				graphics.fill(px + x * PREVIEW_SCALE, py + 12 + y * PREVIEW_SCALE, px + (x + 1) * PREVIEW_SCALE, py + 12 + (y + 1) * PREVIEW_SCALE,
						color(x, y));
			}
		}

		Component hint = notice.getString().isEmpty() ? Component.translatable("screen.jugcraft.carving.hint") : notice;
		graphics.text(font, hint, gridX, gridY + GRID + 8, notice.getString().isEmpty() ? TEXT_DIM : SELECTED, false);
	}

	/** A cell's colour: the pumpkin's skin with darker ribs, shaved flesh, or a hole (lit or not). */
	private int color(int x, int y) {
		int depth = PumpkinCarving.pixel(face[y], x);
		if (depth == PumpkinCarving.SKIN) {
			return x % 5 == 2 ? SKIN_RIB : SKIN;
		}
		if (depth == PumpkinCarving.SHAVED) {
			return litPreview ? CarvingTextures.SHAVED_LIT : CarvingTextures.SHAVED;
		}
		boolean wall = y == 0 || PumpkinCarving.pixel(face[y - 1], x) != PumpkinCarving.CUT;
		return litPreview ? (wall ? CarvingTextures.HOLE_WALL_LIT : CarvingTextures.HOLE_LIT) : (wall ? CarvingTextures.HOLE_WALL : CarvingTextures.HOLE);
	}
}
