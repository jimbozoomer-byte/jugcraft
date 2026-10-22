package io.github.jimbozoomer.jugcraft.mixin.client;

import io.github.jimbozoomer.jugcraft.client.RetroTraderPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Poses a Retro Trader during his stomp-clap ({@link RetroTraderPose}): hides the folded arms while RetroTraderLayer
 * draws his free ones, lifts and slams his feet, dips his body on the beat and nods his head. Runs for every villager
 * model the renderer poses with his state (the base model and the profession layer's), so the layers stay together.
 * The model is shared between villagers, so the folded arms are shown again for everyone else.
 */
@Mixin(VillagerModel.class)
public abstract class RetroTraderModelMixin {
	@Shadow
	@Final
	private ModelPart head;
	@Shadow
	@Final
	private ModelPart rightLeg;
	@Shadow
	@Final
	private ModelPart leftLeg;
	@Shadow
	@Final
	private ModelPart arms;

	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/VillagerRenderState;)V", at = @At("TAIL"))
	private void jugcraft$retroTraderJam(VillagerRenderState state, CallbackInfo info) {
		RetroTraderPose pose = ((RetroTraderPose.Holder) state).jugcraft$retroTrader();
		arms.visible = pose == null || !pose.freeArms;
		if (pose == null || !pose.jamming) {
			return;
		}
		ModelPart body = ((VillagerModel) (Object) this).root().getChild("body");
		body.y += pose.bounce;
		head.y += pose.bounce;
		head.xRot += pose.nod;
		head.zRot += pose.sway;
		rightLeg.y -= pose.rightLift;
		rightLeg.xRot -= 0.03F * pose.rightLift;
		leftLeg.y -= pose.leftLift;
		leftLeg.xRot -= 0.03F * pose.leftLift;
	}
}
