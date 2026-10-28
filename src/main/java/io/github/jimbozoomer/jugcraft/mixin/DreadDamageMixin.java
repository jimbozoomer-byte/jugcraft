package io.github.jimbozoomer.jugcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.jimbozoomer.jugcraft.weapons.DreadBoon;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A creature in dread (the Dread boon, weapons/DreadBoon.java) takes a fifth more from every blow, counted after its armor
 * and protection. Vanilla has no attribute for the damage a creature takes, so the share is added here; it is a map lookup
 * of the creature's own effects, nothing more.
 */
@Mixin(LivingEntity.class)
public abstract class DreadDamageMixin {
	@ModifyReturnValue(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"))
	private float jugcraft$dread(float amount) {
		return DreadBoon.taken((LivingEntity) (Object) this, amount);
	}
}
