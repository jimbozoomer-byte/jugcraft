package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Boxes and planes in a block's space, in pixels, drawn in one colour over a 16x16 texture that each face reads where a
 * block model's face would (from its position), for renderers that tint what they draw (the Aura Candle, the wax in the
 * Wax Melting Pot). Drawn with a render type that shows both sides, so the order of a face's corners doesn't matter.
 */
final class TintedBoxes {
	private TintedBoxes() {
	}

	static void box(VertexConsumer buffer, PoseStack.Pose matrix, float x0, float y0, float z0, float x1, float y1, float z1, int argb, int light) {
		face(buffer, matrix, argb, light, 0, 1, 0, new float[][] {{x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}},
				new float[][] {{x0, z0}, {x1, z0}, {x1, z1}, {x0, z1}});
		face(buffer, matrix, argb, light, 0, -1, 0, new float[][] {{x0, y0, z1}, {x1, y0, z1}, {x1, y0, z0}, {x0, y0, z0}},
				new float[][] {{x0, 16 - z1}, {x1, 16 - z1}, {x1, 16 - z0}, {x0, 16 - z0}});
		face(buffer, matrix, argb, light, 0, 0, -1, new float[][] {{x1, y1, z0}, {x0, y1, z0}, {x0, y0, z0}, {x1, y0, z0}},
				new float[][] {{16 - x1, 16 - y1}, {16 - x0, 16 - y1}, {16 - x0, 16 - y0}, {16 - x1, 16 - y0}});
		face(buffer, matrix, argb, light, 0, 0, 1, new float[][] {{x0, y1, z1}, {x1, y1, z1}, {x1, y0, z1}, {x0, y0, z1}},
				new float[][] {{x0, 16 - y1}, {x1, 16 - y1}, {x1, 16 - y0}, {x0, 16 - y0}});
		face(buffer, matrix, argb, light, -1, 0, 0, new float[][] {{x0, y1, z0}, {x0, y1, z1}, {x0, y0, z1}, {x0, y0, z0}},
				new float[][] {{z0, 16 - y1}, {z1, 16 - y1}, {z1, 16 - y0}, {z0, 16 - y0}});
		face(buffer, matrix, argb, light, 1, 0, 0, new float[][] {{x1, y1, z1}, {x1, y1, z0}, {x1, y0, z0}, {x1, y0, z1}},
				new float[][] {{16 - z1, 16 - y1}, {16 - z0, 16 - y1}, {16 - z0, 16 - y0}, {16 - z1, 16 - y0}});
	}

	/** A flat square facing up at height {@code y}, from ({@code x0}, {@code z0}) to ({@code x1}, {@code z1}). */
	static void top(VertexConsumer buffer, PoseStack.Pose matrix, float x0, float z0, float x1, float z1, float y, int argb, int light) {
		face(buffer, matrix, argb, light, 0, 1, 0, new float[][] {{x0, y, z0}, {x1, y, z0}, {x1, y, z1}, {x0, y, z1}},
				new float[][] {{x0, z0}, {x1, z0}, {x1, z1}, {x0, z1}});
	}

	/**
	 * A plane standing upright through the middle of the block, turned {@code turn} about the vertical, {@code width} wide and
	 * from {@code y0} to {@code y1}, showing the whole texture (for flames).
	 */
	static void plane(VertexConsumer buffer, PoseStack.Pose matrix, double turn, float width, float y0, float y1, int argb, int light) {
		float dx = (float) Math.cos(turn) * width / 2;
		float dz = (float) Math.sin(turn) * width / 2;
		float[][] corners = {{8 - dx, y1, 8 - dz}, {8 + dx, y1, 8 + dz}, {8 + dx, y0, 8 + dz}, {8 - dx, y0, 8 - dz}};
		float[][] uv = {{0, 0}, {16, 0}, {16, 16}, {0, 16}};
		face(buffer, matrix, argb, light, (float) -Math.sin(turn), 0, (float) Math.cos(turn), corners, uv);
	}

	private static void face(VertexConsumer buffer, PoseStack.Pose matrix, int argb, int light, float nx, float ny, float nz, float[][] corners,
			float[][] uv) {
		for (int i = 0; i < 4; i++) {
			buffer.addVertex(matrix, corners[i][0] / 16.0F, corners[i][1] / 16.0F, corners[i][2] / 16.0F).setColor(argb)
					.setUv(uv[i][0] / 16.0F, uv[i][1] / 16.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(matrix, nx, ny, nz);
		}
	}
}
