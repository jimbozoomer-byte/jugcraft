package io.github.jimbozoomer.jugcraft.mixin;

import io.github.jimbozoomer.jugcraft.lineage.Lineage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Star-bred lines (docs/features/star-bred-lines.md): a baby bred from two animals takes its line from them, and its
 * sign from the sky. At the end of finalizeSpawnChildFromBreeding vanilla has set the parents' rest and the baby is not
 * yet in the world, so its line, health and growing time are set before anyone sees it.
 */
@Mixin(Animal.class)
public abstract class LineageBreedingMixin {
	@Inject(method = "finalizeSpawnChildFromBreeding", at = @At("RETURN"))
	private void jugcraft$lineage(ServerLevel level, Animal mate, AgeableMob child, CallbackInfo ci) {
		Lineage.born(level, (Animal) (Object) this, mate, child);
	}
}
