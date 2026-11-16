package io.github.jimbozoomer.jugcraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.jimbozoomer.jugcraft.client.pegasus.PegasusClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Pegasus descent without sprinting (docs/features/pegasus.md): while the player pilots a Pegasus and Descend is bound
 * to the same key as Sprint (both Left Ctrl by default), the movement input reads Sprint as up. Only the reading inside
 * KeyboardInput.tick changes, and only then; the key mappings' own state, sprint anywhere else, and Descend are
 * untouched. Every other key read by tick passes through unchanged.
 */
@Mixin(KeyboardInput.class)
public abstract class PegasusSprintKeyMixin {
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
	private boolean jugcraft$pegasusDescendIsNotSprint(KeyMapping key, Operation<Boolean> original) {
		boolean down = original.call(key);
		return down && !(key == Minecraft.getInstance().options.keySprint && PegasusClient.suppressSprint());
	}
}
