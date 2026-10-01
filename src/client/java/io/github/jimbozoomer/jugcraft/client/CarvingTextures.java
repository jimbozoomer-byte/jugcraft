package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.PumpkinCarving;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * One small texture per carved design: the four faces side by side (64x16), transparent where the skin
 * is left so the vanilla pumpkin texture shows, and coloured where it is shaved or cut through. Lit and
 * unlit pumpkins get their own colours (a dark hollow by day, candlelight at night). Every pumpkin with the
 * same design shares one texture; at most {@link #MAX} are kept, the least recently drawn going first.
 */
final class CarvingTextures {
	static final int MAX = 256;

	/** Unlit: pale flesh where shaved, a dark hollow where cut, a lighter inner wall under the top edge of a hole. */
	static final int SHAVED = 0xFFF2C27C;
	static final int HOLE = 0xFF2A1406;
	static final int HOLE_WALL = 0xFF4E2A0E;
	/** Lit: shaved skin glows amber, holes shine candle-yellow, the top inner wall catches orange light. */
	static final int SHAVED_LIT = 0xFFFFB347;
	static final int HOLE_LIT = 0xFFFFE07A;
	static final int HOLE_WALL_LIT = 0xFFF59A2C;

	private record Key(PumpkinCarving carving, boolean lit) {
	}

	private record Entry(Identifier id, RenderType type) {
	}

	private static final Map<Key, Entry> CACHE = new LinkedHashMap<>(64, 0.75F, true);
	private static int next;

	private CarvingTextures() {
	}

	/** The render type drawing this design, creating its texture the first time it is seen. */
	static RenderType get(PumpkinCarving carving, boolean lit) {
		Key key = new Key(carving, lit);
		Entry entry = CACHE.get(key);
		if (entry != null) {
			return entry.type();
		}
		if (CACHE.size() >= MAX) {
			Iterator<Entry> eldest = CACHE.values().iterator();
			Minecraft.getInstance().getTextureManager().release(eldest.next().id());
			eldest.remove();
		}
		NativeImage image = new NativeImage(PumpkinCarving.SIZE * PumpkinCarving.FACES, PumpkinCarving.SIZE, true);
		for (int face = 0; face < PumpkinCarving.FACES; face++) {
			for (int y = 0; y < PumpkinCarving.SIZE; y++) {
				for (int x = 0; x < PumpkinCarving.SIZE; x++) {
					image.setPixel(face * PumpkinCarving.SIZE + x, y, color(carving, face, x, y, lit));
				}
			}
		}
		Identifier id = Jugcraft.id("dynamic/carved_pumpkin_" + next++);
		DynamicTexture texture = new DynamicTexture(() -> "Jugcraft carved pumpkin " + id, image);
		texture.upload();
		Minecraft.getInstance().getTextureManager().register(id, texture);
		entry = new Entry(id, RenderTypes.entityCutout(id));
		CACHE.put(key, entry);
		return entry.type();
	}

	/** Frees every carving texture (on leaving a world). */
	static void clear() {
		for (Entry entry : CACHE.values()) {
			Minecraft.getInstance().getTextureManager().release(entry.id());
		}
		CACHE.clear();
	}

	/** A pixel's colour (ARGB): transparent skin, shaved flesh, or a hole with a lighter wall under its top edge. */
	static int color(PumpkinCarving carving, int face, int x, int y, boolean lit) {
		return switch (carving.depth(face, x, y)) {
			case PumpkinCarving.SHAVED -> lit ? SHAVED_LIT : SHAVED;
			case PumpkinCarving.CUT -> {
				boolean wall = y == 0 || carving.depth(face, x, y - 1) != PumpkinCarving.CUT;
				yield lit ? (wall ? HOLE_WALL_LIT : HOLE_LIT) : (wall ? HOLE_WALL : HOLE);
			}
			default -> 0;
		};
	}
}
