package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.CarvingFace;
import io.github.jimbozoomer.jugcraft.agriculture.PumpkinCarving;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * One small texture per carved design: the four faces side by side (64x16 for a pumpkin, 192x48 for a giant
 * pumpkin), transparent where the skin is left so the pumpkin's own texture shows, and coloured where it is
 * shaved or cut through. Lit and unlit pumpkins get their own colours (a dark hollow by day, candlelight at
 * night, or a cold blue from a soul torch). Every pumpkin with the same design shares one texture; at most {@link #MAX} are kept, the least
 * recently drawn going first.
 */
final class CarvingTextures {
	static final int MAX = 256;
	/** A giant pumpkin's face size. */
	private static final int GIANT = CarvingFace.SIZES[1];

	/** Unlit: pale flesh where shaved, a dark hollow where cut, a lighter inner wall under the top edge of a hole. */
	static final int SHAVED = 0xFFF2C27C;
	static final int HOLE = 0xFF2A1406;
	static final int HOLE_WALL = 0xFF4E2A0E;
	/** Lit: shaved skin glows amber, holes shine candle-yellow, the top inner wall catches orange light. */
	static final int SHAVED_LIT = 0xFFFFB347;
	static final int HOLE_LIT = 0xFFFFE07A;
	static final int HOLE_WALL_LIT = 0xFFF59A2C;
	/** Soul-lit: shaved skin glows pale cyan, holes shine ice-blue, the top inner wall catches a deeper blue. */
	static final int SHAVED_SOUL = 0xFF8FE3E8;
	static final int HOLE_SOUL = 0xFFC8FAFF;
	static final int HOLE_WALL_SOUL = 0xFF37A9C9;

	private record Key(PumpkinCarving carving, boolean lit, boolean soul) {
	}

	/** A giant pumpkin's four 48x48 faces in one array (compared by content), whether it is lit, and by a soul torch. */
	private record GiantKey(int[] faces, boolean lit, boolean soul) {
		@Override
		public boolean equals(Object other) {
			return other instanceof GiantKey key && key.lit == lit && key.soul == soul && Arrays.equals(key.faces, faces);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(faces) * 31 + (lit ? 1 : 0) + (soul ? 2 : 0);
		}
	}

	private record Entry(Identifier id, RenderType type) {
	}

	private static final Map<Object, Entry> CACHE = new LinkedHashMap<>(64, 0.75F, true);
	private static int next;

	private CarvingTextures() {
	}

	/** The render type drawing this design, creating its texture the first time it is seen. */
	static RenderType get(PumpkinCarving carving, boolean lit, boolean soul) {
		Key key = new Key(carving, lit, lit && soul);
		Entry entry = CACHE.get(key);
		if (entry != null) {
			return entry.type();
		}
		int[][] faces = new int[PumpkinCarving.FACES][];
		for (int face = 0; face < PumpkinCarving.FACES; face++) {
			faces[face] = carving.face(face);
		}
		return create(key, faces, PumpkinCarving.SIZE, lit, key.soul());
	}

	/**
	 * The render type drawing a giant pumpkin's four 48x48 faces, side by side in {@code faces} (index 0 to 3,
	 * as {@link net.minecraft.core.Direction#get2DDataValue()}), creating its texture the first time.
	 */
	static RenderType getGiant(int[][] faces, boolean lit, boolean soul) {
		int ints = CarvingFace.ints(GIANT);
		int[] all = new int[ints * faces.length];
		for (int face = 0; face < faces.length; face++) {
			System.arraycopy(faces[face], 0, all, face * ints, ints);
		}
		GiantKey key = new GiantKey(all, lit, lit && soul);
		Entry entry = CACHE.get(key);
		return entry != null ? entry.type() : create(key, faces, GIANT, lit, key.soul());
	}

	private static RenderType create(Object key, int[][] faces, int size, boolean lit, boolean soul) {
		if (CACHE.size() >= MAX) {
			Iterator<Entry> eldest = CACHE.values().iterator();
			Minecraft.getInstance().getTextureManager().release(eldest.next().id());
			eldest.remove();
		}
		NativeImage image = new NativeImage(size * faces.length, size, true);
		for (int face = 0; face < faces.length; face++) {
			for (int y = 0; y < size; y++) {
				for (int x = 0; x < size; x++) {
					int above = y == 0 ? PumpkinCarving.SKIN : CarvingFace.pixel(faces[face], size, x, y - 1);
					image.setPixel(face * size + x, y, color(CarvingFace.pixel(faces[face], size, x, y), above, lit, soul));
				}
			}
		}
		Identifier id = Jugcraft.id("dynamic/carved_pumpkin_" + next++);
		DynamicTexture texture = new DynamicTexture(() -> "Jugcraft carved pumpkin " + id, image);
		texture.upload();
		Minecraft.getInstance().getTextureManager().register(id, texture);
		Entry entry = new Entry(id, RenderTypes.entityCutout(id));
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

	/**
	 * A pixel's colour (ARGB) from its depth and the depth of the pixel above it: transparent skin, shaved
	 * flesh, or a hole with a lighter wall under its top edge; candlelit, soul-lit or dark.
	 */
	static int color(int depth, int above, boolean lit, boolean soul) {
		boolean blue = lit && soul;
		return switch (depth) {
			case PumpkinCarving.SHAVED -> blue ? SHAVED_SOUL : lit ? SHAVED_LIT : SHAVED;
			case PumpkinCarving.CUT -> {
				boolean wall = above != PumpkinCarving.CUT;
				if (blue) {
					yield wall ? HOLE_WALL_SOUL : HOLE_SOUL;
				}
				yield lit ? (wall ? HOLE_WALL_LIT : HOLE_LIT) : (wall ? HOLE_WALL : HOLE);
			}
			default -> 0;
		};
	}
}
