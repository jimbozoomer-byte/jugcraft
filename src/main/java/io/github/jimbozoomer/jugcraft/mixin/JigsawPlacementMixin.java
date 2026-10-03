package io.github.jimbozoomer.jugcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jimbozoomer.jugcraft.world.RetroShopPlacement;
import java.util.List;
import net.minecraft.util.SequencedPriorityIterator;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A village laid out with no room for its Retro Game Shop is laid out again (see RetroShopPlacement). The jigsaw
 * placer grows a structure from its start piece, then takes pieces from its queue while any are left; when none are,
 * this may put the start piece back for a new layout.
 */
@Mixin(JigsawPlacement.class)
public abstract class JigsawPlacementMixin {
	@WrapOperation(method = "addPieces", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/SequencedPriorityIterator;hasNext()Z"))
	private static boolean jugcraft$newLayoutForShop(SequencedPriorityIterator<?> placing, Operation<Boolean> original,
			@Local(argsOnly = true) PoolElementStructurePiece start, @Local(argsOnly = true) List<PoolElementStructurePiece> pieces,
			@Local(argsOnly = true) VoxelShape free) {
		return original.call(placing) || RetroShopPlacement.relayout(placing, start, pieces, free);
	}
}
