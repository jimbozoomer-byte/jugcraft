package io.github.jimbozoomer.jugcraft.mixin.client;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import io.github.jimbozoomer.jugcraft.client.DrugVisuals;
import java.util.Map;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only Jugcraft's named uniform is dynamic. Native targets, shader reloads and all other passes keep their ownership. */
@Mixin(PostPass.class)
abstract class DrugPostPassMixin {
	@Shadow @Final private Map<String, GpuBuffer> customUniforms;
	@Unique private MappableRingBuffer jugcraft$drugUniform;
	@Inject(method = "<init>", at = @At("TAIL"))
	private void jugcraft$allocate(CallbackInfo info) {
		var original = customUniforms.get(DrugVisuals.UNIFORM);
		if (original == null) return;
		original.close();
		jugcraft$drugUniform = new MappableRingBuffer(() -> "Jugcraft drug vision", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 128);
		customUniforms.put(DrugVisuals.UNIFORM, jugcraft$drugUniform.currentBuffer());
	}
	@Inject(method = "addToFrame", at = @At("HEAD"))
	private void jugcraft$update(CallbackInfo info) {
		if (jugcraft$drugUniform == null) return;
		var buffer = jugcraft$drugUniform.currentBuffer();
		var frame = DrugVisuals.frame();
		try (var mapped = buffer.map(false, true)) {
			Std140Builder.intoBuffer(mapped.data()).putVec4(frame.strength(), frame.blur(), frame.motion(), frame.doubling())
					.putVec4(frame.time(), frame.warmth(), frame.vignette(), 0)
					.putVec4(frame.focus(), frame.energy(), frame.tone(), frame.fatigue())
					.putVec4(frame.warning(), frame.jitter(), 0, 0)
					.putVec4(frame.thc(), frame.opioid(), frame.echo(), 0)
					.putVec4(frame.psilocybin(),frame.mescaline(),frame.lsd(),frame.dmt())
					.putVec4(frame.trailAX(),frame.trailAY(),frame.trailBX(),frame.trailBY())
					.putVec4(frame.trailCX(),frame.trailCY(),frame.glimpse(),0);
		}
		customUniforms.put(DrugVisuals.UNIFORM, buffer);
		jugcraft$drugUniform.rotate();
	}
	@Inject(method = "close", at = @At("HEAD"))
	private void jugcraft$close(CallbackInfo info) {
		if (jugcraft$drugUniform == null) return;
		customUniforms.remove(DrugVisuals.UNIFORM);
		jugcraft$drugUniform.close(); jugcraft$drugUniform = null;
	}
}
