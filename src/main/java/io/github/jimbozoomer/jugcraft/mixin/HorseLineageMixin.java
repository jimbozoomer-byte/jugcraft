package io.github.jimbozoomer.jugcraft.mixin;

import io.github.jimbozoomer.jugcraft.horses.HorseBreeds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the horse lines apart (docs/features/horse-breeds.md#crossbreeding): the breeds and the Pegasus decide their
 * own pairings (BreedHorse.canMate, Pegasus.canMate), but a vanilla horse or donkey accepts any horse as a partner, so
 * a pairing it starts with a breed or a Pegasus is refused here, before a foal is made. Both lose their love; nothing
 * else changes.
 */
@Mixin(Animal.class)
public abstract class HorseLineageMixin {
	@Inject(method = "spawnChildFromBreeding", at = @At("HEAD"), cancellable = true)
	private void jugcraft$keepHorseLinesApart(ServerLevel level, Animal mate, CallbackInfo ci) {
		Animal self = (Animal) (Object) this;
		if (HorseBreeds.crossesLines(self, mate)) {
			self.resetLove();
			mate.resetLove();
			ci.cancel();
		}
	}
}
