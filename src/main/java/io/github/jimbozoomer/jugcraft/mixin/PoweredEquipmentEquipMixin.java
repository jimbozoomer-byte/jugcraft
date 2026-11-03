package io.github.jimbozoomer.jugcraft.mixin;

import io.github.jimbozoomer.jugcraft.tools.EquipmentUpdates;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Only the native equip sound/game-event decision ignores battery/shield/fuel bookkeeping. */
@Mixin(LivingEntity.class)
public abstract class PoweredEquipmentEquipMixin {
    @Redirect(method = "onEquipItem", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean jugcraft$resourceUpdateIsNotEquip(ItemStack before, ItemStack after) {
        return EquipmentUpdates.sameEquipment(before, after);
    }
}
