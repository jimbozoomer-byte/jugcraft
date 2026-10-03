package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.gear.GrappleHook;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * The pneumatic grapple's hook (batch 30): a steel claw on a brass collar, and its line back to the owner's hand, slack and
 * sagging in flight and pulled straight once the hook bites. Drawn tinted over a plain light texture.
 */
public class GrappleHookRenderer extends EntityRenderer<GrappleHook, GrappleHookRenderer.State> {
	private static final RenderType STEEL = RenderTypes.entityCutout(Jugcraft.id("textures/entity/grapple_hook.png"));
	private static final int CLAW = 0xFFA4AEB8;
	private static final int COLLAR = 0xFFC8A048;
	private static final int LINE = 0xFF3C3A36;
	/** Half the line's width, in blocks. */
	private static final float LINE_WIDTH = 0.6F / 16;

	public static final class State extends EntityRenderState {
		/** From the hook to the owner's hand, or null without an owner. */
		@Nullable Vec3 line;
		boolean taut;
	}

	public GrappleHookRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(GrappleHook hook, State state, float partialTick) {
		super.extractRenderState(hook, state, partialTick);
		Entity owner = hook.getOwner();
		state.line = owner == null ? null : owner.getRopeHoldPosition(partialTick).subtract(hook.getPosition(partialTick));
		state.taut = hook.isAnchored() || hook.hookedId() >= 0;
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		int light = state.lightCoords;
		Vec3 line = state.line;
		if (line != null) {
			float length = (float) line.length();
			float sag = state.taut ? 0.0F : 0.05F * length;
			int segments = Math.max(2, (int) Math.ceil(length * 2.0F));
			collector.submitCustomGeometry(pose, STEEL, (matrix, buffer) -> {
				for (int i = 0; i < segments; i++) {
					Vector3f a = along(line, sag, i / (float) segments);
					Vector3f b = along(line, sag, (i + 1) / (float) segments);
					strip(buffer, matrix, light, a, b, new Vector3f(0, LINE_WIDTH, 0));
					Vector3f side = new Vector3f(b).sub(a).cross(0, 1, 0);
					strip(buffer, matrix, light, a, b, side.lengthSquared() < 1.0E-8F ? new Vector3f(LINE_WIDTH, 0, 0)
							: side.normalize().mul(LINE_WIDTH));
				}
			});
		}
		pose.pushPose();
		pose.translate(-0.5F, -0.5F, -0.5F);
		collector.submitCustomGeometry(pose, STEEL, (matrix, buffer) -> {
			// The collar and hub, then four prongs splayed out and hooked back.
			TintedBoxes.box(buffer, matrix, 6.5F, 6.5F, 6.5F, 9.5F, 9.5F, 9.5F, COLLAR, light);
			TintedBoxes.box(buffer, matrix, 7.0F, 9.5F, 7.0F, 9.0F, 11.0F, 9.0F, CLAW, light);
			TintedBoxes.box(buffer, matrix, 4.0F, 5.0F, 7.5F, 6.5F, 6.0F, 8.5F, CLAW, light);
			TintedBoxes.box(buffer, matrix, 9.5F, 5.0F, 7.5F, 12.0F, 6.0F, 8.5F, CLAW, light);
			TintedBoxes.box(buffer, matrix, 7.5F, 5.0F, 4.0F, 8.5F, 6.0F, 6.5F, CLAW, light);
			TintedBoxes.box(buffer, matrix, 7.5F, 5.0F, 9.5F, 8.5F, 6.0F, 12.0F, CLAW, light);
			TintedBoxes.box(buffer, matrix, 3.5F, 6.0F, 7.5F, 4.5F, 8.0F, 8.5F, CLAW, light);
			TintedBoxes.box(buffer, matrix, 11.5F, 6.0F, 7.5F, 12.5F, 8.0F, 8.5F, CLAW, light);
			TintedBoxes.box(buffer, matrix, 7.5F, 6.0F, 3.5F, 8.5F, 8.0F, 4.5F, CLAW, light);
			TintedBoxes.box(buffer, matrix, 7.5F, 6.0F, 11.5F, 8.5F, 8.0F, 12.5F, CLAW, light);
		});
		pose.popPose();
		super.submit(state, pose, collector, camera);
	}

	/** The point a fraction {@code t} along the line, sagging most in the middle. */
	private static Vector3f along(Vec3 line, float sag, float t) {
		return new Vector3f((float) line.x * t, (float) line.y * t - sag * 4.0F * t * (1.0F - t), (float) line.z * t);
	}

	/** A flat strip of line from {@code a} to {@code b}, {@code across} wide each side, seen from both sides. */
	private static void strip(VertexConsumer buffer, PoseStack.Pose matrix, int light, Vector3f a, Vector3f b, Vector3f across) {
		Vector3f[] front = {new Vector3f(a).sub(across), new Vector3f(a).add(across), new Vector3f(b).add(across), new Vector3f(b).sub(across)};
		Vector3f[] back = {front[3], front[2], front[1], front[0]};
		for (Vector3f[] quad : new Vector3f[][] {front, back}) {
			float[][] uv = {{0, 1}, {1, 1}, {1, 0}, {0, 0}};
			for (int i = 0; i < 4; i++) {
				buffer.addVertex(matrix, quad[i].x(), quad[i].y(), quad[i].z()).setColor(LINE).setUv(uv[i][0], uv[i][1])
						.setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(matrix, 0, 1, 0);
			}
		}
	}
}
