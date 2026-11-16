package io.github.jimbozoomer.jugcraft.test.mixin;

import io.github.jimbozoomer.jugcraft.test.EquipmentAudioProbe;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class EquipmentAudioProbeMixin {
    @Inject(method = "getEquipSound", at = @At("RETURN"))
    private void jugcraftTest$heard(EquipmentSlot slot, ItemStack stack, Equippable equipment,
            CallbackInfoReturnable<Holder<SoundEvent>> result) {
        EquipmentAudioProbe.record((LivingEntity)(Object)this);
    }
}
