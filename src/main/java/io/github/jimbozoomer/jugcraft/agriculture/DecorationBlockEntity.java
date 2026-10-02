package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds nothing: it lets the client draw the moving or lit parts of a decoration that its block model can't show (the
 * Rocking Chair rocking, the Lurking Eyes blinking, a Silhouette Window glowing, the Giant Fake Spider swaying, the
 * Haunted Chandelier swinging, the Suit of Armor's helmet turning, the Spirit Mirror's face, Tattered Curtains swaying
 * and the Creepy Doll's head). Each of those blocks has its own block entity type of this class.
 */
public class DecorationBlockEntity extends BlockEntity {
	public DecorationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
}
