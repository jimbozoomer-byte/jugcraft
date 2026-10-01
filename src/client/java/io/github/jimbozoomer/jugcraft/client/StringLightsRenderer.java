package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.StringLightHookBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Draws a String Light Hook's strand: a dark wire sagging from this hook to the one it is strung to, with a tiny
 * pumpkin bulb about every {@value #SPACING} blocks. Lit, the bulbs glow at full brightness; unlit they are dim
 * orange. Drawn from the hook that holds the strand, and off screen too, since a strand can be long.
 */
public class StringLightsRenderer implements BlockEntityRenderer<StringLightHookBlockEntity, StringLightsRenderer.State> {
	private static final RenderType STRAND = RenderTypes.entityCutout(Jugcraft.id("textures/entity/string_lights.png"));
	private static final float WIRE = 0.6F / 16;
	private static final float BULB = 2.5F / 16;
	private static final float SPACING = 0.75F;
	private static final int FULL_BRIGHT = 0xF000F0;

	public static final class State extends BlockEntityRenderState {
		@Nullable Vec3 offset;
		boolean lit;
	}

	public StringLightsRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(StringLightHookBlockEntity hook, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(hook, state, crumbling);
		BlockPos link = hook.link();
		BlockPos pos = hook.getBlockPos();
		state.offset = null;
		if (link != null && hook.getLevel() != null && hook.getLevel().getBlockEntity(link) instanceof StringLightHookBlockEntity) {
			state.offset = Vec3.atLowerCornerOf(link.subtract(pos));
			state.lit = StringLightHookBlockEntity.glows(hook.getLevel(), pos, link);
		}
	}

	/** The point a fraction {@code t} along the strand, sagging most in the middle. */
	private static Vector3f along(Vector3f start, Vec3 offset, float sag, float t) {
		return new Vector3f(start).add((float) offset.x * t, (float) offset.y * t - sag * 4.0F * t * (1.0F - t), (float) offset.z * t);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		Vec3 offset = state.offset;
		if (offset == null) {
			return;
		}
		float length = (float) offset.length();
		float sag = 0.15F + 0.05F * length;
		int segments = Math.max(4, (int) Math.ceil(length * 3.0F));
		int bulbs = Math.max(1, (int) Math.floor(length / SPACING));
		Vector3f start = new Vector3f(0.5F, 0.5F, 0.5F);
		int light = state.lightCoords;
		int bulbLight = state.lit ? FULL_BRIGHT : light;
		int bulbColor = state.lit ? 0xFFFFFFFF : 0xFF9A7A5A;
		collector.submitCustomGeometry(pose, STRAND, (matrix, buffer) -> {
			for (int i = 0; i < segments; i++) {
				Vector3f a = along(start, offset, sag, i / (float) segments);
				Vector3f b = along(start, offset, sag, (i + 1) / (float) segments);
				wire(buffer, matrix, light, a, b, new Vector3f(0, WIRE, 0));
				Vector3f side = new Vector3f(b).sub(a).cross(0, 1, 0);
				wire(buffer, matrix, light, a, b, side.lengthSquared() < 1.0E-8F ? new Vector3f(WIRE, 0, 0) : side.normalize().mul(WIRE));
			}
			for (int i = 1; i <= bulbs; i++) {
				Vector3f at = along(start, offset, sag, i / (float) (bulbs + 1)).sub(0, BULB * 0.7F, 0);
				bulb(buffer, matrix, bulbLight, bulbColor, at);
			}
		});
	}

	/** A flat strip of wire from {@code a} to {@code b}, {@code across} wide each side, seen from both sides. */
	private static void wire(VertexConsumer buffer, PoseStack.Pose matrix, int light, Vector3f a, Vector3f b, Vector3f across) {
		Vector3f normal = new Vector3f(0, 1, 0);
		quad(buffer, matrix, light, 0xFFFFFFFF, normal, new Vector3f(a).sub(across), new Vector3f(a).add(across), new Vector3f(b).add(across),
				new Vector3f(b).sub(across), 0.0F, 0.5F);
		quad(buffer, matrix, light, 0xFFFFFFFF, normal, new Vector3f(b).sub(across), new Vector3f(b).add(across), new Vector3f(a).add(across),
				new Vector3f(a).sub(across), 0.0F, 0.5F);
	}

	/** A little cube of pumpkin, {@link #BULB} across, centred on {@code at}. */
	private static void bulb(VertexConsumer buffer, PoseStack.Pose matrix, int light, int color, Vector3f at) {
		float h = BULB / 2;
		float x0 = at.x - h;
		float x1 = at.x + h;
		float y0 = at.y - h;
		float y1 = at.y + h;
		float z0 = at.z - h;
		float z1 = at.z + h;
		quad(buffer, matrix, light, color, new Vector3f(0, 0, -1), new Vector3f(x1, y0, z0), new Vector3f(x0, y0, z0), new Vector3f(x0, y1, z0),
				new Vector3f(x1, y1, z0), 0.5F, 1.0F);
		quad(buffer, matrix, light, color, new Vector3f(0, 0, 1), new Vector3f(x0, y0, z1), new Vector3f(x1, y0, z1), new Vector3f(x1, y1, z1),
				new Vector3f(x0, y1, z1), 0.5F, 1.0F);
		quad(buffer, matrix, light, color, new Vector3f(-1, 0, 0), new Vector3f(x0, y0, z0), new Vector3f(x0, y0, z1), new Vector3f(x0, y1, z1),
				new Vector3f(x0, y1, z0), 0.5F, 1.0F);
		quad(buffer, matrix, light, color, new Vector3f(1, 0, 0), new Vector3f(x1, y0, z1), new Vector3f(x1, y0, z0), new Vector3f(x1, y1, z0),
				new Vector3f(x1, y1, z1), 0.5F, 1.0F);
		quad(buffer, matrix, light, color, new Vector3f(0, 1, 0), new Vector3f(x0, y1, z1), new Vector3f(x1, y1, z1), new Vector3f(x1, y1, z0),
				new Vector3f(x0, y1, z0), 0.5F, 1.0F);
		quad(buffer, matrix, light, color, new Vector3f(0, -1, 0), new Vector3f(x0, y0, z0), new Vector3f(x1, y0, z0), new Vector3f(x1, y0, z1),
				new Vector3f(x0, y0, z1), 0.5F, 1.0F);
	}

	/** One quad, its texture from u0 to u1 across the whole height of the strand texture. */
	private static void quad(VertexConsumer buffer, PoseStack.Pose matrix, int light, int color, Vector3f normal, Vector3f a, Vector3f b,
			Vector3f c, Vector3f d, float u0, float u1) {
		vertex(buffer, matrix, light, color, normal, a, u0, 1.0F);
		vertex(buffer, matrix, light, color, normal, b, u1, 1.0F);
		vertex(buffer, matrix, light, color, normal, c, u1, 0.0F);
		vertex(buffer, matrix, light, color, normal, d, u0, 0.0F);
	}

	private static void vertex(VertexConsumer buffer, PoseStack.Pose matrix, int light, int color, Vector3f normal, Vector3f at, float u, float v) {
		buffer.addVertex(matrix, at.x(), at.y(), at.z()).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
				.setNormal(matrix, normal.x(), normal.y(), normal.z());
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 96;
	}
}
