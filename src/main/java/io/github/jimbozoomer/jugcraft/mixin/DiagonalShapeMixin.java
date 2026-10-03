package io.github.jimbozoomer.jugcraft.mixin;

import io.github.jimbozoomer.jugcraft.diagonal.DiagonalConnections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A fence's or bars block's diagonals in its outline and collision (each worked out once per state and kept), in
 * rotation and mirroring (structures and the structure block), and for its diagonal neighbours when it is placed,
 * broken or changed (see DiagonalConnections). Blocks without the diagonal properties are left as they are.
 */
@Mixin(CrossCollisionBlock.class)
public abstract class DiagonalShapeMixin {
	@Shadow
	@Final
	private Function<BlockState, VoxelShape> shapes;
	@Shadow
	@Final
	private Function<BlockState, VoxelShape> collisionShapes;
	@Unique
	private final Map<BlockState, VoxelShape> jugcraft$diagonalShapes = new ConcurrentHashMap<>();
	@Unique
	private final Map<BlockState, VoxelShape> jugcraft$diagonalCollisionShapes = new ConcurrentHashMap<>();

	@Inject(method = "getShape", at = @At("RETURN"), cancellable = true)
	private void jugcraft$diagonalShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context,
			CallbackInfoReturnable<VoxelShape> callback) {
		if (DiagonalConnections.mask(state) != 0) {
			VoxelShape shape = callback.getReturnValue();
			callback.setReturnValue(jugcraft$diagonalShapes.computeIfAbsent(state, s -> DiagonalConnections.withArms(shape, s, shapes)));
		}
	}

	@Inject(method = "getCollisionShape", at = @At("RETURN"), cancellable = true)
	private void jugcraft$diagonalCollision(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context,
			CallbackInfoReturnable<VoxelShape> callback) {
		if (DiagonalConnections.mask(state) != 0) {
			VoxelShape shape = callback.getReturnValue();
			callback.setReturnValue(jugcraft$diagonalCollisionShapes.computeIfAbsent(state,
					s -> DiagonalConnections.withArms(shape, s, collisionShapes)));
		}
	}

	@Inject(method = "rotate", at = @At("RETURN"), cancellable = true)
	private void jugcraft$rotateDiagonals(BlockState state, Rotation rotation, CallbackInfoReturnable<BlockState> callback) {
		callback.setReturnValue(DiagonalConnections.rotate(callback.getReturnValue(), state, rotation));
	}

	@Inject(method = "mirror", at = @At("RETURN"), cancellable = true)
	private void jugcraft$mirrorDiagonals(BlockState state, Mirror mirror, CallbackInfoReturnable<BlockState> callback) {
		callback.setReturnValue(DiagonalConnections.mirror(callback.getReturnValue(), state, mirror));
	}

	/**
	 * Added to CrossCollisionBlock, where it overrides BlockBehaviour's empty method: the game calls it for a block's
	 * old and new state whenever it is set without UPDATE_KNOWN_SHAPE.
	 */
	protected void updateIndirectNeighbourShapes(BlockState state, LevelAccessor level, BlockPos pos, int flags, int recursionLeft) {
		if (DiagonalConnections.hasDiagonals(state)) {
			DiagonalConnections.updateDiagonalNeighbours(level, pos, flags, recursionLeft);
		}
	}
}
