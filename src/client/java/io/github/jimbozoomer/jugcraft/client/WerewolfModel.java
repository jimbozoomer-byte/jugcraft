package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * A werewolf, about two and a half blocks tall: a barrel chest hunched forward under a shaggy hump of mane, a wolf's
 * head on it (a long snout, a jaw that drops open when it hunts, pricked ears), long arms ending in clawed hands that
 * hang near its knees, legs bent back like a wolf's hind legs onto long feet, and a bushy tail. It lopes with its arms
 * swinging; hunting, it hunches lower, arms raised and jaws open. Texture: tools/werewolf_textures.py, whose box layout
 * matches this one (128 by 64).
 */
public class WerewolfModel extends EntityModel<WerewolfRenderer.State> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(Jugcraft.id("werewolf"), "main");
	/** How far forward the body leans standing, and hunting. */
	private static final float LEAN = 0.35F;
	private static final float HUNT_LEAN = 0.6F;

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart jaw;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;
	private final ModelPart tail;

	public WerewolfModel(ModelPart root) {
		super(root);
		body = root.getChild("body");
		head = body.getChild("head");
		jaw = head.getChild("jaw");
		rightArm = body.getChild("right_arm");
		leftArm = body.getChild("left_arm");
		rightLeg = root.getChild("right_leg");
		leftLeg = root.getChild("left_leg");
		tail = body.getChild("tail");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		// The chest, hips at the pivot, and the hump of mane over the shoulders.
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-6.5F, -17.0F, -4.5F, 13.0F, 17.0F, 9.0F)
				.texOffs(0, 28).addBox(-7.0F, -19.0F, -2.0F, 14.0F, 6.0F, 8.0F),
				PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, LEAN, 0.0F, 0.0F));
		// The head on the front of the shoulders: skull, snout, ears; the jaw hinged under the snout.
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(46, 0).addBox(-4.5F, -7.0F, -6.0F, 9.0F, 8.0F, 8.0F)
				.texOffs(82, 0).addBox(-2.5F, -4.0F, -12.0F, 5.0F, 4.0F, 6.0F)
				.texOffs(106, 8).addBox(-4.0F, -11.0F, -2.0F, 3.0F, 4.0F, 2.0F)
				.texOffs(106, 8).addBox(1.0F, -11.0F, -2.0F, 3.0F, 4.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, -17.0F, -4.0F, -LEAN, 0.0F, 0.0F));
		head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(106, 0).addBox(-2.5F, 0.0F, -5.0F, 5.0F, 2.0F, 5.0F),
				PartPose.offset(0.0F, 0.0F, -6.0F));
		// Long arms: upper arm, forearm, and a clawed hand.
		for (int side = -1; side <= 1; side += 2) {
			body.addOrReplaceChild(side < 0 ? "right_arm" : "left_arm", CubeListBuilder.create()
					.texOffs(46, 18).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 12.0F, 5.0F)
					.texOffs(68, 18).addBox(-2.0F, 10.0F, -2.0F, 4.0F, 10.0F, 4.0F)
					.texOffs(86, 12).addBox(-2.5F, 19.0F, -2.5F, 5.0F, 3.0F, 5.0F),
					PartPose.offset(side * 8.5F, -15.0F, 0.0F));
		}
		// Legs bent back: thigh, shin set back, and a long foot.
		for (int side = -1; side <= 1; side += 2) {
			root.addOrReplaceChild(side < 0 ? "right_leg" : "left_leg", CubeListBuilder.create()
					.texOffs(46, 38).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 8.0F, 6.0F)
					.texOffs(72, 36).addBox(-2.5F, 7.0F, -1.0F, 5.0F, 6.0F, 5.0F)
					.texOffs(94, 36).addBox(-3.0F, 12.0F, -5.0F, 6.0F, 2.0F, 7.0F),
					PartPose.offset(side * 3.5F, 10.0F, 0.0F));
		}
		// The tail, from the small of the back, hanging down and out.
		body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 44).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F),
				PartPose.offsetAndRotation(0.0F, -2.0F, 4.0F, -0.9F, 0.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 64);
	}

	@Override
	public void setupAnim(WerewolfRenderer.State state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float walk = state.walkAnimationPos;
		float speed = Math.min(1.0F, state.walkAnimationSpeed);
		float lean = state.hunting ? HUNT_LEAN : LEAN;
		body.xRot = lean + 0.05F * Mth.sin(t * 0.08F);
		head.yRot = state.yRot * Mth.DEG_TO_RAD;
		head.xRot = state.xRot * Mth.DEG_TO_RAD - lean;
		jaw.xRot = state.hunting ? 0.45F + 0.15F * Mth.sin(t * 0.6F) : 0.05F;
		rightLeg.xRot = Mth.cos(walk * 0.6662F) * 1.2F * speed;
		leftLeg.xRot = Mth.cos(walk * 0.6662F + Mth.PI) * 1.2F * speed;
		float swing = Mth.cos(walk * 0.6662F) * 0.9F * speed;
		if (state.hunting) {
			// Arms up and forward, claws out, raking.
			rightArm.xRot = -1.3F - lean + 0.3F * Mth.sin(t * 0.5F);
			leftArm.xRot = -1.3F - lean - 0.3F * Mth.sin(t * 0.5F);
		} else {
			rightArm.xRot = -lean + swing;
			leftArm.xRot = -lean - swing;
		}
		rightArm.zRot = 0.12F;
		leftArm.zRot = -0.12F;
		tail.xRot = -0.9F + 0.1F * Mth.sin(t * 0.1F);
		tail.yRot = 0.25F * Mth.sin(walk * 0.6662F) * speed + 0.1F * Mth.sin(t * 0.07F);
	}
}
