package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Organ-Grinder Skeleton's clockwork, in its lower block ({@link ClockworkFigureBlockEntity}): while it runs it plays
 * the organ's waltz from the top, a step every {@value OrganGrinderSkeletonBlock#STEP_TICKS} ticks, the melody on the
 * flute and each bar's bass on its first step.
 */
public class OrganGrinderSkeletonBlockEntity extends ClockworkFigureBlockEntity {
	public OrganGrinderSkeletonBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.ORGAN_GRINDER_SKELETON_ENTITY, pos, state);
	}

	@Override
	protected void play(ServerLevel level, BlockPos pos, int played) {
		if (played % OrganGrinderSkeletonBlock.STEP_TICKS != 0) {
			return;
		}
		int step = played / OrganGrinderSkeletonBlock.STEP_TICKS % OrganGrinderSkeletonBlock.MELODY.length;
		int note = OrganGrinderSkeletonBlock.MELODY[step];
		if (note >= 0) {
			level.playSound(null, pos.above(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.RECORDS, OrganGrinderSkeletonBlock.VOLUME,
					OrganGrinderSkeletonBlock.pitch(note));
		}
		if (step % OrganGrinderSkeletonBlock.STEPS_PER_BAR == 0) {
			int bass = OrganGrinderSkeletonBlock.BASS[step / OrganGrinderSkeletonBlock.STEPS_PER_BAR];
			level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.RECORDS, OrganGrinderSkeletonBlock.VOLUME,
					OrganGrinderSkeletonBlock.pitch(bass));
		}
	}
}
