package io.github.jimbozoomer.jugcraft.mixin;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.jimbozoomer.jugcraft.pharmacology.Pharmacology;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(LivingEntity.class)
abstract class PharmacologyAttackMixin {
	@ModifyReturnValue(method = "getAttributeValue", at = @At("RETURN"))
	private double jugcraft$meleeBenefit(double value, Holder<Attribute> attribute) {
		return attribute.equals(Attributes.ATTACK_DAMAGE) ? Pharmacology.meleeDamage((LivingEntity) (Object) this, value) : value;
	}
}
