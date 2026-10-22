package io.github.jimbozoomer.jugcraft.mixin;

import io.github.jimbozoomer.jugcraft.lair.Lairs;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Dying in a lair (a boss fight) costs nothing: as with the keepInventory rule, but only there (Lairs.keepsBelongings),
 * the player drops no items and no experience, and Lairs hands the respawned player the same inventory, held slot and
 * experience. Everywhere else death is vanilla.
 */
@Mixin(Player.class)
public abstract class LairKeepInventoryMixin {
	@Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
	private void jugcraft$keepItemsInLairs(CallbackInfo ci) {
		if (Lairs.keepsBelongings((Player) (Object) this)) {
			ci.cancel();
		}
	}

	@Inject(method = "getBaseExperienceReward", at = @At("HEAD"), cancellable = true)
	private void jugcraft$keepExperienceInLairs(CallbackInfoReturnable<Integer> cir) {
		if (Lairs.keepsBelongings((Player) (Object) this)) {
			cir.setReturnValue(0);
		}
	}
}
