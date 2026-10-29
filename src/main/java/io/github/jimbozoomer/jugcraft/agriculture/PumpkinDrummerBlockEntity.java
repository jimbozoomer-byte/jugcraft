package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Pumpkin-Headed Drummer's clockwork, in his lower block ({@link ClockworkFigureBlockEntity}): while he runs he beats
 * his march from the top, a step every {@value PumpkinDrummerBlock#STEP_TICKS} ticks, the drum sounding on each beat,
 * lower under the right mallet than the left.
 */
public class PumpkinDrummerBlockEntity extends ClockworkFigureBlockEntity {
	public PumpkinDrummerBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.PUMPKIN_DRUMMER_ENTITY, pos, state);
	}

	@Override
	protected void play(ServerLevel level, BlockPos pos, int played) {
		if (played % PumpkinDrummerBlock.STEP_TICKS != 0) {
			return;
		}
		char hand = PumpkinDrummerBlock.beat(played / PumpkinDrummerBlock.STEP_TICKS);
		if (hand != '.') {
			level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASEDRUM.value(), SoundSource.RECORDS, PumpkinDrummerBlock.VOLUME,
					hand == 'R' ? PumpkinDrummerBlock.PITCH_RIGHT : PumpkinDrummerBlock.PITCH_LEFT);
		}
	}
}
