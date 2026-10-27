package io.github.jimbozoomer.jugcraft.mixin;

import io.github.jimbozoomer.jugcraft.agriculture.Disguises;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Costume disguises (agriculture/Disguises): a monster never picks a player it takes for one of its own. */
@Mixin(Mob.class)
public abstract class DisguiseTargetMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void jugcraft$disguised(LivingEntity target, CallbackInfo ci) {
        if (target instanceof Player player && Disguises.fooled((Mob) (Object) this, player)) {
            ((Mob) (Object) this).setTarget(null);
            ci.cancel();
        }
    }
}
