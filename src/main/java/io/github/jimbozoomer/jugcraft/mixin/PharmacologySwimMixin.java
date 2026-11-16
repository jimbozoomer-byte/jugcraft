package io.github.jimbozoomer.jugcraft.mixin;

import io.github.jimbozoomer.jugcraft.pharmacology.Pharmacology;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
abstract class PharmacologySwimMixin {
	@ModifyArg(method = "travelInWater", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"), index = 0)
	private float jugcraft$waterInput(float amount) {
		return Pharmacology.waterAcceleration((LivingEntity) (Object) this, amount);
	}
}
