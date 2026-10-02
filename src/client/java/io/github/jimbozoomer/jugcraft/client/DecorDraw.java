package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Small drawing helpers for the decorations' block entity renderers: textured boxes and single quads, with corners in
 * the order the generated quads use (tools/decor6_data.py FACE_CORNERS), so they face the way their normals point.
 * Positions are in blocks.
 */
final class DecorDraw {
	/** Each face's corners as 0/1 picks of the box's low and high x, y, z, and its normal. */
	private static final int[][][] CORNERS = {
			{{0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}}, // north
			{{1, 0, 1}, {1, 1, 1}, {0, 1, 1}, {0, 0, 1}}, // south
			{{0, 0, 1}, {0, 1, 1}, {0, 1, 0}, {0, 0, 0}}, // west
			{{1, 0, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}}, // east
			{{0, 1, 0}, {0, 1, 1}, {1, 1, 1}, {1, 1, 0}}, // up
			{{0, 0, 1}, {0, 0, 0}, {1, 0, 0}, {1, 0, 1}}}; // down
	private static final float[][] NORMALS = {{0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, -1, 0}};
	static final int NORTH = 1;
	static final int SOUTH = 2;
	static final int WEST = 4;
	static final int EAST = 8;
	static final int UP = 16;
	static final int DOWN = 32;
	static final int ALL = 63;

	private DecorDraw() {
	}

	/** A box from (x0, y0, z0) to (x1, y1, z1), each face showing the texture's (u0, v0)-(u1, v1). */
	static void box(VertexConsumer buffer, PoseStack.Pose matrix, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0,
			float u1, float v1, int color, int light, int faces) {
		float[] lo = {x0, y0, z0};
		float[] hi = {x1, y1, z1};
		for (int face = 0; face < 6; face++) {
			if ((faces & (1 << face)) == 0) {
				continue;
			}
			boolean flat = face >= 4;
			float[][] uv = flat ? new float[][] {{u0, v0}, {u0, v1}, {u1, v1}, {u1, v0}} : new float[][] {{u1, v1}, {u1, v0}, {u0, v0}, {u0, v1}};
			for (int i = 0; i < 4; i++) {
				int[] pick = CORNERS[face][i];
				buffer.addVertex(matrix, pick[0] == 1 ? hi[0] : lo[0], pick[1] == 1 ? hi[1] : lo[1], pick[2] == 1 ? hi[2] : lo[2]).setColor(color)
						.setUv(uv[i][0], uv[i][1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
						.setNormal(matrix, NORMALS[face][0], NORMALS[face][1], NORMALS[face][2]);
			}
		}
	}

	/** One quad through four corners {x, y, z, u, v}, in the order that faces it toward its normal. */
	static void quad(VertexConsumer buffer, PoseStack.Pose matrix, float[][] corners, float nx, float ny, float nz, int color, int light) {
		for (float[] c : corners) {
			buffer.addVertex(matrix, c[0], c[1], c[2]).setColor(color).setUv(c[3], c[4]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
					.setNormal(matrix, nx, ny, nz);
		}
	}
}
