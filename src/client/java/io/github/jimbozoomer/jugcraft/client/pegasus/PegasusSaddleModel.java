package io.github.jimbozoomer.jugcraft.client.pegasus;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * The Pegasus's saddle and bridle (docs/features/pegasus.md), drawn while the vanilla saddle slot holds a saddle, so
 * saddling, unsaddling (shears or the horse screen) and the saddle dropping on death are vanilla's. Built as vanilla's
 * EquineSaddleModel is: on the horse's own bones (here cube-less copies with the same names, parents and PartPoses), so
 * vanilla's setupAnim moves it with the horse; the reins show only while ridden. The seat sits behind the wing roots
 * and the breast collar runs below them, so neither clips a wing. Its own 64x64 texture
 * (textures/entity/pegasus/pegasus_saddle.png); editable source art/pegasus/pegasus_saddle.bbmodel. Scaled as the
 * body layer is ({@link PegasusModel#ADULT_SCALE}), once.
 */
public class PegasusSaddleModel extends AbstractEquineModel<PegasusRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(Jugcraft.id("pegasus"), "saddle");
	private final ModelPart leftRein;
	private final ModelPart rightRein;

	public PegasusSaddleModel(ModelPart root) {
		super(root);
		leftRein = headParts.getChild("left_rein");
		rightRein = headParts.getChild("right_rein");
	}

	public static LayerDefinition createSaddleLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		// The vanilla horse's bones (AbstractEquineModel.createBodyMesh's PartPoses, from the 26.3 javap), without cubes.
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 11.0F, 5.0F));
		body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -5.0F, 2.0F, 0.5235988F, 0.0F, 0.0F));
		PartDefinition headParts = root.addOrReplaceChild("head_parts", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0.0F, 4.0F, -12.0F, 0.5235988F, 0.0F, 0.0F));
		root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create(), PartPose.offset(4.0F, 14.0F, 7.0F));
		root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create(), PartPose.offset(-4.0F, 14.0F, 7.0F));
		root.addOrReplaceChild("left_front_leg", CubeListBuilder.create(), PartPose.offset(4.0F, 14.0F, -10.0F));
		root.addOrReplaceChild("right_front_leg", CubeListBuilder.create(), PartPose.offset(-4.0F, 14.0F, -10.0F));
		// BEGIN GENERATED SADDLE (tools: java_gen.py from pegasus_saddle.mesh.json; checked by tools/check_pegasus_model.py)
		body.addOrReplaceChild("saddle", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.5F, -8.5F, -7.0F, 7.0F, 1.0F, 8.0F)
				.texOffs(0, 9).addBox(-3.0F, -9.5F, -6.0F, 6.0F, 1.0F, 6.0F)
				.texOffs(54, 5).addBox(-2.0F, -10.5F, -6.0F, 4.0F, 1.0F, 1.0F)
				.texOffs(52, 9).addBox(-2.5F, -10.5F, -1.0F, 5.0F, 1.0F, 1.0F)
				.texOffs(22, 0).addBox(5.0F, -8.5F, -4.0F, 1.0F, 4.0F, 1.0F)
				.texOffs(22, 0).addBox(-6.0F, -8.5F, -4.0F, 1.0F, 4.0F, 1.0F)
				.texOffs(30, 0).addBox(-5.5F, -5.0F, -4.5F, 11.0F, 8.0F, 1.0F)
				.texOffs(0, 16).addBox(-5.5F, -5.0F, -17.5F, 11.0F, 2.0F, 1.0F)
				.texOffs(54, 0).addBox(5.0F, -4.5F, -16.0F, 1.0F, 1.0F, 4.0F)
				.texOffs(54, 0).addBox(-6.0F, -4.5F, -16.0F, 1.0F, 1.0F, 4.0F),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		headParts.addOrReplaceChild("bridle", CubeListBuilder.create()
				.texOffs(24, 9).addBox(-3.5F, -11.5F, 2.5F, 7.0F, 6.0F, 1.0F)
				.texOffs(40, 9).addBox(-2.5F, -11.5F, -5.5F, 5.0F, 6.0F, 1.0F)
				.texOffs(0, 0).addBox(2.5F, -8.0F, -6.0F, 1.0F, 2.0F, 2.0F)
				.texOffs(0, 0).addBox(-3.5F, -8.0F, -6.0F, 1.0F, 2.0F, 2.0F),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		headParts.addOrReplaceChild("left_rein", CubeListBuilder.create()
				.texOffs(24, 0).addBox(3.6F, -7.0F, -7.0F, 0.0F, 2.0F, 16.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5235988F, 0.0F, 0.0F));
		headParts.addOrReplaceChild("right_rein", CubeListBuilder.create()
				.texOffs(24, 0).addBox(-3.6F, -7.0F, -7.0F, 0.0F, 2.0F, 16.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5235988F, 0.0F, 0.0F));
		// END GENERATED SADDLE
		return LayerDefinition.create(mesh, 64, 64).apply(MeshTransformer.scaling(PegasusModel.ADULT_SCALE));
	}

	@Override
	public void setupAnim(PegasusRenderState state) {
		super.setupAnim(state);
		leftRein.visible = state.isRidden;
		rightRein.visible = state.isRidden;
	}
}
