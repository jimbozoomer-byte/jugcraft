package io.github.jimbozoomer.jugcraft.mixin.client;

import io.github.jimbozoomer.jugcraft.agriculture.BloodMoon;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Blood Moon's night sky (agriculture/BloodMoon): while the server says it is up, the overworld's sky colour is
 * turned blood red, the more as night deepens. Only the colour the sky pass is given changes; nothing else is drawn.
 */
@Mixin(SkyRenderer.class)
public abstract class BloodMoonSkyMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void jugcraft$bloodMoon(ClientLevel level, float partial, Camera camera, SkyRenderState state, CallbackInfo ci) {
		if (BloodMoon.clientActive && level.dimension().equals(Level.OVERWORLD) && state.skyColor != null) {
			state.skyColor = BloodMoon.tint(state.skyColor.x(), state.skyColor.y(), state.skyColor.z(), state.starBrightness);
		}
	}
}
