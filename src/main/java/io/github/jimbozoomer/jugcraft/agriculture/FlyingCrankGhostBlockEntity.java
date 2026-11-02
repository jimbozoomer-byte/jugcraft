package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Flying Crank Ghost's clockwork, in its lower block ({@link ClockworkFigureBlockEntity}): while it runs it moans
 * once in every {@value FlyingCrankGhostBlock#MOAN_EVERY} ticks, the first as it starts, from its upper block, where
 * the ghost hangs.
 */
public class FlyingCrankGhostBlockEntity extends ClockworkFigureBlockEntity {
	public FlyingCrankGhostBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.FLYING_CRANK_GHOST_ENTITY, pos, state);
	}

	@Override
	protected void play(ServerLevel level, BlockPos pos, int played) {
		if (played % FlyingCrankGhostBlock.MOAN_EVERY == 0) {
			level.playSound(null, pos.above(), SoundEvents.GHAST_AMBIENT, SoundSource.RECORDS, FlyingCrankGhostBlock.VOLUME, FlyingCrankGhostBlock.PITCH);
		}
	}
}
