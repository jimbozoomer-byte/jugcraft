package io.github.jimbozoomer.jugcraft.mixin;

import io.github.jimbozoomer.jugcraft.diagonal.DiagonalConnections;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Fences and bars start with no diagonals. A block's constructor names its default state by setting each of its own
 * properties on the state definition's first state, whose booleans are all true, so the four diagonal properties that
 * DiagonalStateMixin adds would otherwise start true. This clears them in every default state that has them, whatever
 * subclass registers it.
 */
@Mixin(Block.class)
public abstract class DiagonalDefaultStateMixin {
	@ModifyVariable(method = "registerDefaultState", at = @At("HEAD"), argsOnly = true)
	private BlockState jugcraft$noDiagonals(BlockState state) {
		return DiagonalConnections.withoutDiagonals(state);
	}
}
