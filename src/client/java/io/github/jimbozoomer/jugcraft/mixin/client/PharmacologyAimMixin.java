package io.github.jimbozoomer.jugcraft.mixin.client;
import io.github.jimbozoomer.jugcraft.client.guns.GunView;
import io.github.jimbozoomer.jugcraft.guns.GunItem;
import io.github.jimbozoomer.jugcraft.pharmacology.PrecisionPharmacy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Scale the two existing native look-lag rotations, only while aiming a supported gun. */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
abstract class PharmacologyAimMixin {
	@ModifyArg(method = "submitHandsWithItems", at = @At(value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotateDegrees(Lcom/mojang/math/Axis;F)V"), index = 1, require = 2)
	private float jugcraft$steadyAimedHands(float angle) {
		var player = Minecraft.getInstance().player;
		return player != null && player.getMainHandItem().getItem() instanceof GunItem && GunView.aim(1) > 0
				? angle * PrecisionPharmacy.aimSwayMultiplier(player) : angle;
	}
}
