package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.raiders.RaiderWalker;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * Draws the Raider Walker (batch 57) from the Diesel Walker's parts in raider paint (assets/jugcraft/raider_quads.json,
 * tools/raiders.py): the body facing the way it walks, legs swinging as it walks, the fist arm swinging when it punches.
 * Its drill never turns (raiders break no blocks). Dying, it topples over sideways.
 */
public class RaiderWalkerRenderer extends EntityRenderer<RaiderWalker, RaiderWalkerRenderer.State> {
	/** Joints in pixels, the Diesel Walker's (DieselWalkerRenderer, tools/mech.py). */
	private static final float[][] HIPS = {{8, 26, 0}, {-8, 26, 0}};
	private static final float[] FIST_SHOULDER = {21, 46, 0};
	private static final float[] DRILL_SHOULDER = {-21, 46, 0};
	private static final float[] BIT = {0, -16, 12};
	private static final float STRIDE_RATE = 2.2F;
	private static final float LEG_SWING = 28.0F;
	private static final float PUNCH_TICKS = 10.0F;
	private static final float PUNCH_SWING = 70.0F;
	private static final int FULL_BRIGHT = 0xF000F0;

	public static final class State extends EntityRenderState {
		float yRot;
		float stride;
		float swing;
		float dying;
	}

	public RaiderWalkerRenderer(EntityRendererProvider.Context context) {
		super(context);
		shadowRadius = 1.4F;
	}

	@Override
	protected AABB getBoundingBoxForCulling(RaiderWalker walker, float partialTick) {
		return walker.getBoundingBox().inflate(1.5, 0.5, 1.5);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(RaiderWalker walker, State state, float partialTick) {
		super.extractRenderState(walker, state, partialTick);
		state.yRot = Mth.rotLerp(partialTick, walker.yBodyRotO, walker.yBodyRot);
		state.stride = walker.stride();
		float swingTime = walker.sinceSwing() + partialTick;
		state.swing = swingTime < PUNCH_TICKS ? Mth.sin(swingTime / PUNCH_TICKS * Mth.PI) : 0.0F;
		state.dying = walker.deathTime > 0 ? Math.min(1.0F, (walker.deathTime + partialTick) / 20.0F) : 0.0F;
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		pose.pushPose();
		pose.rotateDegrees(Axis.YP, -state.yRot);
		if (state.dying > 0) {
			pose.rotateDegrees(Axis.ZP, state.dying * state.dying * 80.0F);
		}
		draw("raider_walker_body", pose, collector, state.lightCoords);
		draw("raider_walker_core", pose, collector, FULL_BRIGHT);
		float legSwing = Mth.sin(state.stride * STRIDE_RATE) * LEG_SWING;
		for (int i = 0; i < HIPS.length; i++) {
			pose.pushPose();
			at(pose, HIPS[i]);
			pose.rotateDegrees(Axis.XP, i == 0 ? legSwing : -legSwing);
			draw("raider_walker_leg", pose, collector, state.lightCoords);
			pose.popPose();
		}
		pose.pushPose();
		at(pose, FIST_SHOULDER);
		pose.rotateDegrees(Axis.XP, -state.swing * PUNCH_SWING - legSwing * 0.3F);
		draw("raider_walker_fist_arm", pose, collector, state.lightCoords);
		pose.popPose();
		pose.pushPose();
		at(pose, DRILL_SHOULDER);
		pose.rotateDegrees(Axis.XP, legSwing * 0.3F);
		draw("raider_walker_drill_arm", pose, collector, state.lightCoords);
		at(pose, BIT);
		draw("raider_walker_drill_bit", pose, collector, state.lightCoords);
		pose.popPose();
		pose.popPose();
		super.submit(state, pose, collector, camera);
	}

	private static void at(PoseStack pose, float[] pixels) {
		pose.translate(pixels[0] / 16.0F, pixels[1] / 16.0F, pixels[2] / 16.0F);
	}

	private static void draw(String part, PoseStack pose, SubmitNodeCollector collector, int light) {
		QuadModel model = DecorQuads.get(part);
		if (model != null) {
			model.submit(pose, collector, light);
		}
	}
}
