package io.github.jimbozoomer.jugcraft.client.pegasus;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.pegasus.PegasusFlight;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * The Pegasus (docs/features/pegasus.md): vanilla 26.3's horse rig, unchanged (AbstractEquineModel.createBodyMesh with
 * CubeDeformation.NONE, its 12 bones, names, parents, pivots and cubes, so vanilla's setupAnim still walks, grazes,
 * rears and swishes the tail), plus a feathered wing on each side of {@code body}: shoulder ({@code left_wing}),
 * elbow and hand bones, each with a leading edge, a covert layer and stepped flight feathers.
 *
 * <p>Editable source: art/pegasus/pegasus.bbmodel (raw mesh px, wings spread in its rest pose). The part definitions
 * between the GENERATED markers are written from the same mesh as that file; tools/check_pegasus_model.py compares
 * them. Like ModelLayers.HORSE (Codex's 26.3 evidence), the adult layer is scaled once here with
 * MeshTransformer.scaling({@value #ADULT_SCALE}); the .bbmodel is never pre-scaled. The texture is 128x64: the horse's
 * nets keep vanilla's texOffs in u 0..64 and the wings' (shared, the left wing mirrored) are in u 64..128.
 *
 * <p>setupAnim runs vanilla's horse animation, then poses the wings from the smoothed, synced flight state
 * (PegasusFlight.wingPose): folded when grounded, lifted off the rider's legs when ridden, unfolding on takeoff,
 * beating while powered, faster and harder to climb, calmer to descend, gliding, and folding again on landing.
 */
public class PegasusModel extends AbstractEquineModel<PegasusRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(Jugcraft.id("pegasus"), "main");
	public static final ModelLayerLocation BABY_LAYER = new ModelLayerLocation(Jugcraft.id("pegasus"), "baby");
	/** ModelLayers.HORSE's adult scale (applied once, here). */
	public static final float ADULT_SCALE = 1.1F;
	/** A baby (only from commands: breeding is off) is the same rig at about half size. */
	public static final float BABY_SCALE = 0.55F;
	public static final int TEXTURE_WIDTH = 128;
	public static final int TEXTURE_HEIGHT = 64;

	private final ModelPart leftWing;
	private final ModelPart leftElbow;
	private final ModelPart leftHand;
	private final ModelPart rightWing;
	private final ModelPart rightElbow;
	private final ModelPart rightHand;

	public PegasusModel(ModelPart root) {
		super(root);
		leftWing = body.getChild("left_wing");
		leftElbow = leftWing.getChild("left_wing_elbow");
		leftHand = leftElbow.getChild("left_wing_hand");
		rightWing = body.getChild("right_wing");
		rightElbow = rightWing.getChild("right_wing_elbow");
		rightHand = rightElbow.getChild("right_wing_hand");
	}

	/** The raw (unscaled) mesh: the vanilla horse plus both wings. */
	public static MeshDefinition createMesh() {
		MeshDefinition mesh = AbstractEquineModel.createBodyMesh(CubeDeformation.NONE);
		PartDefinition body = mesh.getRoot().getChild("body");
		// BEGIN GENERATED WINGS (tools: java_gen.py from pegasus.mesh.json; checked by tools/check_pegasus_model.py)
		PartDefinition leftWing = body.addOrReplaceChild("left_wing", CubeListBuilder.create().mirror()
				.texOffs(105, 0).addBox(-2.5F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F)
				.texOffs(64, 13).addBox(1.5F, -1.0F, -2.0F, 6.0F, 2.0F, 2.0F)
				.texOffs(107, 17).addBox(1.5F, -1.0F, 0.0F, 6.0F, 1.0F, 2.0F)
				.texOffs(64, 8).addBox(1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 4.0F)
				.texOffs(110, 12).addBox(4.5F, 0.0F, 0.0F, 3.0F, 1.0F, 4.0F),
				PartPose.offset(5.5F, -8.5F, -12.0F));
		PartDefinition leftWingElbow = leftWing.addOrReplaceChild("left_wing_elbow", CubeListBuilder.create().mirror()
				.texOffs(90, 7).addBox(0.0F, -1.0F, -2.0F, 8.0F, 2.0F, 2.0F)
				.texOffs(92, 12).addBox(0.0F, -1.0F, 0.0F, 8.0F, 1.0F, 3.0F)
				.texOffs(106, 6).addBox(0.0F, 0.0F, 0.0F, 3.0F, 1.0F, 5.0F)
				.texOffs(79, 7).addBox(3.0F, 0.0F, 0.0F, 3.0F, 1.0F, 5.0F)
				.texOffs(95, 0).addBox(6.0F, 0.0F, 0.0F, 2.0F, 1.0F, 6.0F),
				PartPose.offset(7.5F, 0.0F, 0.0F));
		PartDefinition leftWingHand = leftWingElbow.addOrReplaceChild("left_wing_hand", CubeListBuilder.create().mirror()
				.texOffs(78, 15).addBox(0.0F, -1.0F, -2.0F, 6.0F, 2.0F, 2.0F)
				.texOffs(76, 19).addBox(0.0F, -1.0F, 0.0F, 6.0F, 1.0F, 2.0F)
				.texOffs(77, 0).addBox(0.0F, 0.0F, 0.0F, 3.0F, 1.0F, 6.0F)
				.texOffs(64, 0).addBox(3.0F, 0.0F, 0.0F, 3.0F, 1.0F, 7.0F)
				.texOffs(90, 20).addBox(6.0F, 0.0F, -2.0F, 6.0F, 1.0F, 2.0F)
				.texOffs(91, 16).addBox(6.0F, 0.0F, 0.0F, 5.0F, 1.0F, 3.0F)
				.texOffs(64, 17).addBox(6.0F, 0.0F, 3.0F, 4.0F, 1.0F, 3.0F)
				.texOffs(89, 0).addBox(6.0F, 0.0F, 6.0F, 2.0F, 1.0F, 2.0F),
				PartPose.offset(8.0F, 0.0F, 0.0F));
		PartDefinition rightWing = body.addOrReplaceChild("right_wing", CubeListBuilder.create()
				.texOffs(105, 0).addBox(-1.5F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F)
				.texOffs(64, 13).addBox(-7.5F, -1.0F, -2.0F, 6.0F, 2.0F, 2.0F)
				.texOffs(107, 17).addBox(-7.5F, -1.0F, 0.0F, 6.0F, 1.0F, 2.0F)
				.texOffs(64, 8).addBox(-4.5F, 0.0F, 0.0F, 3.0F, 1.0F, 4.0F)
				.texOffs(110, 12).addBox(-7.5F, 0.0F, 0.0F, 3.0F, 1.0F, 4.0F),
				PartPose.offset(-5.5F, -8.5F, -12.0F));
		PartDefinition rightWingElbow = rightWing.addOrReplaceChild("right_wing_elbow", CubeListBuilder.create()
				.texOffs(90, 7).addBox(-8.0F, -1.0F, -2.0F, 8.0F, 2.0F, 2.0F)
				.texOffs(92, 12).addBox(-8.0F, -1.0F, 0.0F, 8.0F, 1.0F, 3.0F)
				.texOffs(106, 6).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 1.0F, 5.0F)
				.texOffs(79, 7).addBox(-6.0F, 0.0F, 0.0F, 3.0F, 1.0F, 5.0F)
				.texOffs(95, 0).addBox(-8.0F, 0.0F, 0.0F, 2.0F, 1.0F, 6.0F),
				PartPose.offset(-7.5F, 0.0F, 0.0F));
		PartDefinition rightWingHand = rightWingElbow.addOrReplaceChild("right_wing_hand", CubeListBuilder.create()
				.texOffs(78, 15).addBox(-6.0F, -1.0F, -2.0F, 6.0F, 2.0F, 2.0F)
				.texOffs(76, 19).addBox(-6.0F, -1.0F, 0.0F, 6.0F, 1.0F, 2.0F)
				.texOffs(77, 0).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 1.0F, 6.0F)
				.texOffs(64, 0).addBox(-6.0F, 0.0F, 0.0F, 3.0F, 1.0F, 7.0F)
				.texOffs(90, 20).addBox(-12.0F, 0.0F, -2.0F, 6.0F, 1.0F, 2.0F)
				.texOffs(91, 16).addBox(-11.0F, 0.0F, 0.0F, 5.0F, 1.0F, 3.0F)
				.texOffs(64, 17).addBox(-10.0F, 0.0F, 3.0F, 4.0F, 1.0F, 3.0F)
				.texOffs(89, 0).addBox(-8.0F, 0.0F, 6.0F, 2.0F, 1.0F, 2.0F),
				PartPose.offset(-8.0F, 0.0F, 0.0F));
		// END GENERATED WINGS
		return mesh;
	}

	public static LayerDefinition createBodyLayer() {
		return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT).apply(MeshTransformer.scaling(ADULT_SCALE));
	}

	public static LayerDefinition createBabyLayer() {
		return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT).apply(MeshTransformer.scaling(BABY_SCALE));
	}

	@Override
	public void setupAnim(PegasusRenderState state) {
		super.setupAnim(state);
		double[] pose = PegasusFlight.wingPose(state.wingOpen, state.wingPhase, state.wingAmp);
		pose(leftWing, pose, 0, false);
		pose(leftElbow, pose, 3, false);
		pose(leftHand, pose, 6, false);
		pose(rightWing, pose, 0, true);
		pose(rightElbow, pose, 3, true);
		pose(rightHand, pose, 6, true);
	}

	/** The left wing's angles as they are; the right wing's mirrored across the body: (x, -y, -z). */
	private static void pose(ModelPart part, double[] pose, int at, boolean mirror) {
		part.xRot = (float) pose[at];
		part.yRot = (float) (mirror ? -pose[at + 1] : pose[at + 1]);
		part.zRot = (float) (mirror ? -pose[at + 2] : pose[at + 2]);
	}
}
