package io.github.jimbozoomer.jugcraft.mixin;

import io.github.jimbozoomer.jugcraft.world.RetroShopPlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A new jigsaw placer means a new structure: it has no Retro Game Shop yet (see RetroShopPlacement). */
@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
public abstract class JigsawPlacerMixin {
	@Inject(method = "<init>*", at = @At("RETURN"))
	private void jugcraft$beginStructure(CallbackInfo callback) {
		RetroShopPlacement.beginStructure();
	}
}
