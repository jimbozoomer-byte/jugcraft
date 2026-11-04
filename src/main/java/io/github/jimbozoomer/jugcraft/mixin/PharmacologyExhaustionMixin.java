package io.github.jimbozoomer.jugcraft.mixin;

import io.github.jimbozoomer.jugcraft.pharmacology.Pharmacology;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Endurance reduces activity exhaustion; vanilla healing's direct food cost remains intact. */
@Mixin(Player.class)
public abstract class PharmacologyExhaustionMixin {
	@ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
	private float jugcraft$endurance(float amount) {
		return Pharmacology.activityExhaustion((Player) (Object) this, amount);
	}
}
