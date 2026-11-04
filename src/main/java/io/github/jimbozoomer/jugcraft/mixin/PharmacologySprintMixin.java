package io.github.jimbozoomer.jugcraft.mixin;

import io.github.jimbozoomer.jugcraft.pharmacology.Pharmacology;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayer.class)
abstract class PharmacologySprintMixin {
	// 26.3: swim, underwater, wading, then ground sprint. Combat/mining never enter this branch.
	@ModifyArg(method = "checkMovementStatistics", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V", ordinal = 3), index = 0)
	private float jugcraft$sprintCost(float amount) {
		return Pharmacology.sprintExhaustion((ServerPlayer) (Object) this, amount);
	}
}
