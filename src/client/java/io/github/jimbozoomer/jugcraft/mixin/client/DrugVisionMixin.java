package io.github.jimbozoomer.jugcraft.mixin.client;

import io.github.jimbozoomer.jugcraft.client.DrugVisuals;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Compose with head/spectator effects through Minecraft's own post-effect request list. */
@Mixin(LocalPlayer.class)
abstract class DrugVisionMixin {
	@Inject(method = "getActivePostEffects", at = @At("RETURN"), cancellable = true)
	private void jugcraft$drugVision(CallbackInfoReturnable<List<Identifier>> info) {
		if (!DrugVisuals.request((LocalPlayer) (Object) this)) return;
		var effects = new ArrayList<>(info.getReturnValue());
		if (!effects.contains(DrugVisuals.ALCOHOL)) effects.add(DrugVisuals.ALCOHOL);
		info.setReturnValue(List.copyOf(effects));
	}
}
