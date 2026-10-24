package io.github.jimbozoomer.jugcraft.mixin;
import io.github.jimbozoomer.jugcraft.jimtafacts.spellbooks.JimtaWardSpells;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Applied after ordinary defense reducers; debt survives save/logout and is repaid once. */
@Mixin(value=LivingEntity.class,priority=900)
public abstract class JimtaHeartstopMixin {
    @Inject(method="getDamageAfterMagicAbsorb",at=@At("RETURN"),cancellable=true)
    private void jimta$defer(DamageSource source,float amount,CallbackInfoReturnable<Float> result){result.setReturnValue(JimtaWardSpells.defer((LivingEntity)(Object)this,source,result.getReturnValue()));}
    @Inject(method="tick",at=@At("RETURN"))
    private void jimta$refreshConcealment(CallbackInfo info){JimtaWardSpells.refreshConcealment((LivingEntity)(Object)this);}
}
