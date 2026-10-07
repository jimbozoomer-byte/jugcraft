package io.github.jimbozoomer.jugcraft.client.guns;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jimbozoomer.jugcraft.guns.GunItem;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * The player's arms in their own first-person view of a gun, drawn at the gun model's right_arm and left_arm bones
 * (tools/guns.py places them; the owner's animations move them). Each bone's pivot is the hand; the arm runs back from
 * it along the bone's -y, twelve pixels with the fist two past the pivot, as the idle turns point it at the camera.
 * The arms are the player model's own (the skin's arm and sleeve, wide or slim).
 */
final class GunArmsLayer extends GeoRenderLayer<GunItem, GeoItemRenderer.RenderData, GeoRenderState> {
	/** The player model's arm runs from y -2 (shoulder) to 10 (fist); this puts the fist 2 px past the bone's pivot. */
	private static final float FIST = 8.0F;
	private static ModelPart[] arms;
	private static ModelPart[] slimArms;

	GunArmsLayer(GunRenderer renderer) {
		super(renderer);
	}

	@Override
	public void addPerBoneRender(RenderPassInfo<GeoRenderState> info, BiConsumer<GeoBone, PerBoneRender<GeoRenderState>> consumer) {
		GunRenderer.View view = info.getGeckolibData(GunRenderer.VIEW);
		if (view == null || !info.willRender()) {
			return;
		}
		for (int side = 0; side < 2; side++) {
			boolean right = side == 0;
			info.model().getBone(right ? "right_arm" : "left_arm")
					.ifPresent(bone -> consumer.accept(bone, (pass, posed, tasks) -> arm(pass, tasks, view, right)));
		}
	}

	private static void arm(RenderPassInfo<GeoRenderState> pass, net.minecraft.client.renderer.SubmitNodeCollector tasks,
			GunRenderer.View view, boolean right) {
		ModelPart part = parts(view.slim())[right ? 0 : 1];
		// Centre the arm on the bone: a wide arm's box spans x -3..1 (right) or -1..3 (left), a slim one's -2..1 or -1..2.
		float centre = view.slim() ? 0.5F : 1.0F;
		PoseStack poseStack = pass.poseStack();
		poseStack.pushPose();
		poseStack.translate((right ? centre : -centre) / 16.0F, -FIST / 16.0F, 0.0F);
		int light = pass.packedLight();
		tasks.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(view.skin()), (pose, buffer) -> {
			PoseStack local = new PoseStack();
			local.last().set(pose);
			part.render(local, buffer, light, OverlayTexture.NO_OVERLAY);
		});
		poseStack.popPose();
	}

	/** The player model's right and left arm (with their sleeves), posed at the origin. */
	private static ModelPart[] parts(boolean slim) {
		if (slim ? slimArms == null : arms == null) {
			ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER);
			ModelPart[] baked = {root.getChild("right_arm"), root.getChild("left_arm")};
			for (ModelPart part : baked) {
				part.x = 0.0F;
				part.y = 0.0F;
				part.z = 0.0F;
				part.xRot = 0.0F;
				part.yRot = 0.0F;
				part.zRot = 0.0F;
			}
			if (slim) {
				slimArms = baked;
			} else {
				arms = baked;
			}
		}
		return slim ? slimArms : arms;
	}
}
