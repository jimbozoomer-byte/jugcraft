package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.walker.HowitzerWalker;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;

/**
 * Draws the Howitzer Walker from the parts tools/howitzer_walker.py exports (assets/jugcraft/howitzer_walker_quads.json),
 * facing the way it walks; see {@link HowitzerWalkerParts}.
 */
public class HowitzerWalkerRenderer extends EntityRenderer<HowitzerWalker, HowitzerWalkerRenderer.State> {
	public static final class State extends EntityRenderState {
		float yRot;
		float stride;
		float gait;
		float sinceShot;
	}

	public HowitzerWalkerRenderer(EntityRendererProvider.Context context) {
		super(context);
		shadowRadius = 1.4F;
	}

	/** The barrel reaches past the hitbox. */
	@Override
	protected AABB getBoundingBoxForCulling(HowitzerWalker walker, float partialTick) {
		return walker.getBoundingBox().inflate(2.0, 1.0, 2.0);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(HowitzerWalker walker, State state, float partialTick) {
		super.extractRenderState(walker, state, partialTick);
		state.yRot = walker.getYRot(partialTick);
		state.stride = walker.stride();
		state.gait = walker.gait(partialTick);
		state.sinceShot = walker.sinceShot() + partialTick;
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		pose.pushPose();
		pose.rotateDegrees(Axis.YP, -state.yRot);
		HowitzerWalkerParts.draw("howitzer_walker", pose, collector, state.lightCoords, state.stride, state.gait, state.sinceShot);
		pose.popPose();
		super.submit(state, pose, collector, camera);
	}
}
