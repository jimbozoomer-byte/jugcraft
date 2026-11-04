package io.github.jimbozoomer.jugcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jimbozoomer.jugcraft.pharmacology.Pharmacology;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Boost only vanilla food-based healing, behind its food/gamerule checks; pay for the extra healing in food. */
@Mixin(FoodData.class)
public abstract class PharmacologyFoodMixin {
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V"))
	private void jugcraft$recovery(ServerPlayer player, float amount, Operation<Void> original) {
		original.call(player, amount * Pharmacology.naturalHealingMultiplier(player));
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V"))
	private void jugcraft$healingCost(FoodData food, float amount, Operation<Void> original,
			@Local(argsOnly = true) ServerPlayer player) {
		original.call(food, amount * Pharmacology.naturalHealingMultiplier(player));
	}
}
