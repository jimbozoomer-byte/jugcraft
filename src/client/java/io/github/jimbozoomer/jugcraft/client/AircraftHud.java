package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.aircraft.Aircraft;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * The pilot's flight help, drawn by the client so that it names the keys the player has actually bound (the server
 * cannot know them) and fits whatever window and GUI scale the player uses.
 *
 * <p>The help is five short pieces (fuel or pedalling, steering, climbing, the weapon, the booster or telescope). They
 * are packed onto as few centred lines as the GUI's width allows, one piece never split between lines unless it is
 * wider than the whole GUI, in which case {@code font.split} wraps it. The lines stack upwards from just above
 * vanilla's action-bar line, so they clear the hotbar, the health rows and any action-bar message. When even that does
 * not fit (a very small window) the text is drawn smaller and packed again, down to half size.
 */
public final class AircraftHud {
	/** One line as drawn, in GUI pixels: its text and the box it fills. */
	public record Line(String text, float x, float y, float width, float height) {
	}

	/** What one frame drew: the GUI it was laid out for, the scale of its text, its pieces and its lines. */
	public record Frame(int guiWidth, int guiHeight, float scale, List<String> pieces, List<Line> lines) {
		/** Every line, joined with a space. */
		public String text() {
			return String.join(" ", lines.stream().map(Line::text).toList());
		}
	}

	/** Gap kept clear at the window's sides and top. */
	static final int MARGIN = 4;
	/** The lines end above vanilla's action-bar text, which is drawn from {@code guiHeight - 72}. */
	static final int ABOVE_BOTTOM = 74;
	/** The smallest the text is drawn when space is tight. */
	static final float SMALLEST = .5F;
	private static final String SEPARATOR = "  ·  ";
	private static final int COLOR = 0xFFFFFFFF;

	private static volatile Frame last;
	private static List<String> lastPieces = List.of();
	private static int lastWidth;
	private static int lastHeight;
	private static List<FormattedCharSequence> lastDrawn = List.of();

	private AircraftHud() {
	}

	/** The last frame drawn, for the client game test; null before the help has been drawn. */
	public static Frame lastFrame() {
		return last;
	}

	/** The help's pieces for this pilot and craft, with the key names from the player's current bindings. */
	public static List<Component> pieces(Minecraft client, Aircraft aircraft) {
		Options o = client.options;
		List<Component> out = new ArrayList<>();
		out.add(aircraft.musclePowered()
				? Component.translatableWithFallback("hud.jugcraft.aircraft.pedal", "Pedal %s to spin the rotor", key(o.keyUp))
				: Component.translatableWithFallback("hud.jugcraft.aircraft.fuel", "Fuel %ss", aircraft.fuelSeconds()));
		out.add(Component.translatableWithFallback("hud.jugcraft.aircraft.steer", "%s %s %s %s steer", key(o.keyUp), key(o.keyLeft),
				key(o.keyDown), key(o.keyRight)));
		out.add(Component.translatableWithFallback("hud.jugcraft.aircraft.climb", "%s climb / %s descend", key(o.keyJump),
				key(o.keySprint)));
		out.add(Component.translatableWithFallback("hud.jugcraft.aircraft.weapon", "%s weapon", key(o.keyAttack)));
		out.add(aircraft.hasTelescope()
				? Component.translatableWithFallback("hud.jugcraft.aircraft.zoom", "%s zoom", key(o.keyUse))
				: Component.translatableWithFallback("hud.jugcraft.aircraft.boost", "%s boost", key(o.keyUse)));
		return out;
	}

	/** A key's current binding as the controls screen names it. */
	static Component key(KeyMapping mapping) {
		return mapping.getTranslatedKeyMessage();
	}

	/**
	 * Lays the pieces out for a GUI of the given size: packed onto centred lines no wider than the GUI less its margins,
	 * stacked upwards from above the action bar, shrunk (and packed again) only if they would not otherwise fit.
	 */
	public static Frame layout(Font font, int guiWidth, int guiHeight, List<String> pieces) {
		List<FormattedCharSequence> drawn = new ArrayList<>();
		return layout(font, guiWidth, guiHeight, pieces, drawn);
	}

	private static Frame layout(Font font, int guiWidth, int guiHeight, List<String> pieces, List<FormattedCharSequence> drawn) {
		int top = Math.min(MARGIN, guiHeight);
		int bottom = Math.max(top + font.lineHeight, guiHeight - ABOVE_BOTTOM);
		if (bottom > guiHeight) {
			bottom = guiHeight;
		}
		int room = Math.max(1, guiWidth - 2 * MARGIN);
		float scale = 1;
		List<FormattedCharSequence> lines = pack(font, pieces, room);
		while (scale > SMALLEST && height(font, lines.size()) * scale > bottom - top) {
			scale = Math.max(SMALLEST, scale - .05F);
			lines = pack(font, pieces, (int) Math.floor(room / scale));
		}
		drawn.clear();
		drawn.addAll(lines);
		List<Line> boxes = new ArrayList<>();
		float step = (font.lineHeight + 1) * scale;
		float y = bottom - height(font, lines.size()) * scale;
		for (FormattedCharSequence line : lines) {
			float width = font.width(line) * scale;
			boxes.add(new Line(plain(line), (guiWidth - width) / 2, y, width, font.lineHeight * scale));
			y += step;
		}
		return new Frame(guiWidth, guiHeight, scale, List.copyOf(pieces), List.copyOf(boxes));
	}

	/** The height in unscaled pixels of {@code count} lines one pixel apart. */
	private static int height(Font font, int count) {
		return count == 0 ? 0 : count * (font.lineHeight + 1) - 1;
	}

	/** Packs whole pieces onto lines no wider than {@code room}; a piece wider than that is wrapped by {@code font.split}. */
	private static List<FormattedCharSequence> pack(Font font, List<String> pieces, int room) {
		List<FormattedCharSequence> out = new ArrayList<>();
		String line = "";
		for (String piece : pieces) {
			String joined = line.isEmpty() ? piece : line + SEPARATOR + piece;
			if (font.width(joined) <= room) {
				line = joined;
				continue;
			}
			if (!line.isEmpty()) {
				out.add(Component.literal(line).getVisualOrderText());
			}
			if (font.width(piece) <= room) {
				line = piece;
			} else {
				out.addAll(font.split(Component.literal(piece), room));
				line = "";
			}
		}
		if (!line.isEmpty()) {
			out.add(Component.literal(line).getVisualOrderText());
		}
		return out;
	}

	private static String plain(FormattedCharSequence sequence) {
		StringBuilder text = new StringBuilder();
		sequence.accept((index, style, codePoint) -> {
			text.appendCodePoint(codePoint);
			return true;
		});
		return text.toString();
	}

	/** The HUD element: only for the pilot, only with no screen open. */
	static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.gui.screen() != null || !(client.player.getVehicle() instanceof Aircraft aircraft)
				|| aircraft.getFirstPassenger() != client.player) {
			return;
		}
		List<String> pieces = pieces(client, aircraft).stream().map(Component::getString).toList();
		int width = g.guiWidth(), height = g.guiHeight();
		Frame frame = last;
		if (frame == null || width != lastWidth || height != lastHeight || !pieces.equals(lastPieces)) {
			List<FormattedCharSequence> drawn = new ArrayList<>();
			frame = layout(client.font, width, height, pieces, drawn);
			lastDrawn = drawn;
			lastPieces = pieces;
			lastWidth = width;
			lastHeight = height;
			last = frame;
		}
		for (int i = 0; i < frame.lines().size(); i++) {
			Line line = frame.lines().get(i);
			g.pose().pushMatrix();
			g.pose().translate(line.x(), line.y());
			g.pose().scale(frame.scale(), frame.scale());
			g.text(client.font, lastDrawn.get(i), 0, 0, COLOR, true);
			g.pose().popMatrix();
		}
	}
}
