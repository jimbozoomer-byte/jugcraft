package io.github.jimbozoomer.jugcraft.client.horses;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EquineRenderState;

/**
 * A breed's saddle and bridle (docs/features/horse-breeds.md), built as vanilla's EquineSaddleModel is: its parts on the
 * horse's own bones (cube-less copies, same pivots), the reins ({@code left_saddle_line}, {@code right_saddle_line})
 * shown only while ridden, and the breed's rig, so the tack moves with the horse.
 */
public class BreedSaddleModel<S extends EquineRenderState> extends BreedHorseModel<S> {
	private final ModelPart leftRein;
	private final ModelPart rightRein;

	public BreedSaddleModel(ModelPart root, BreedHorseMeshes.Rig rig) {
		super(root, rig);
		leftRein = headParts.getChild("left_saddle_line");
		rightRein = headParts.getChild("right_saddle_line");
	}

	@Override
	public void setupAnim(S state) {
		super.setupAnim(state);
		leftRein.visible = state.isRidden;
		rightRein.visible = state.isRidden;
	}
}
