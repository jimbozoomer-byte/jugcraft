package io.github.jimbozoomer.jugcraft.client.horses;

import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.util.Mth;

/**
 * A breed horse, or one of its tack layers (docs/features/horse-breeds.md): vanilla's AbstractEquineModel over the
 * breed's own mesh (BreedHorseMeshes, the same 12 bones, names and parents as vanilla's horse), so vanilla's setupAnim
 * walks, gallops, grazes, rears and swishes it, with the breed's rearing rig in the protected hooks vanilla 26.3 calls
 * from setupAnim (the supplied javap). Every breed but the Giant Finnhorse uses vanilla's values; the giant's keep its
 * hind legs out of its rump when it rears (docs/features/horse-breeds.md#giant-finnhorse-rearing). The tack layers use
 * the same rig, so tack moves exactly with the horse.
 */
public class BreedHorseModel<S extends EquineRenderState> extends AbstractEquineModel<S> {
	private final BreedHorseMeshes.Rig rig;

	public BreedHorseModel(ModelPart root, BreedHorseMeshes.Rig rig) {
		super(root);
		this.rig = rig;
	}

	/** How far the front legs rise while rearing (vanilla 12). */
	@Override
	protected float getLegStandingYOffset() {
		return rig.legStandingY();
	}

	/** How far the front legs come back while rearing (vanilla 4). */
	@Override
	protected float getLegStandingZOffset() {
		return rig.legStandingZ();
	}

	/** Vanilla's head placement (lift -8 rearing, drop 7 grazing, back to z -4 rearing) with the breed's values. */
	@Override
	protected void animateHeadPartsPlacement(float eat, float stand) {
		headParts.y += Mth.lerp(eat, Mth.lerp(stand, 0.0F, rig.headStandLift()), rig.headEatDrop());
		headParts.z = Mth.lerp(stand, headParts.z, rig.headStandZ());
	}

	/** Vanilla does nothing here; a breed may raise its body and step its hind legs back while it stands (bounded, linear). */
	@Override
	protected void offsetLegPositionWhenStanding(float stand) {
		body.y -= rig.bodyStandLift() * stand;
		leftHindLeg.z += rig.hindLegStandZ() * stand;
		rightHindLeg.z += rig.hindLegStandZ() * stand;
	}
}
