package io.github.jimbozoomer.jugcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jimbozoomer.jugcraft.pharmacology.Pharmacology;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fortitude reduces armor-sensitive physical damage without stacking with stronger vanilla Resistance. */
@Mixin(LivingEntity.class)
public abstract class PharmacologyDamageMixin {
	@ModifyReturnValue(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"))
	private float jugcraft$fortitude(float amount, @Local(argsOnly = true) DamageSource source) {
		return Pharmacology.physicalDamage((LivingEntity) (Object) this, source, amount);
	}
}
