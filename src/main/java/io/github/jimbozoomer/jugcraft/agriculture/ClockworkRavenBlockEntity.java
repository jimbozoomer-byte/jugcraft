package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Clockwork Raven's clockwork, in its lower block ({@link ClockworkFigureBlockEntity}): while it runs it caws
 * {@value ClockworkRavenBlock#CAWS} times in every {@value ClockworkRavenBlock#CYCLE} ticks
 * ({@link ClockworkRavenBlock#cawsAt}), from its upper block, where the raven perches.
 */
public class ClockworkRavenBlockEntity extends ClockworkFigureBlockEntity {
	public ClockworkRavenBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.CLOCKWORK_RAVEN_ENTITY, pos, state);
	}

	@Override
	protected void play(ServerLevel level, BlockPos pos, int played) {
		if (ClockworkRavenBlock.cawsAt(played % ClockworkRavenBlock.CYCLE)) {
			level.playSound(null, pos.above(), SoundEvents.PARROT_AMBIENT, SoundSource.RECORDS, ClockworkRavenBlock.VOLUME, ClockworkRavenBlock.PITCH);
		}
	}
}
