package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.kinetic.KineticConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Organ-Grinder Skeleton's clockwork, in its lower block: it takes kinetic energy from any shaft, gearbox or source
 * touching that block, up to {@value OrganGrinderSkeletonBlock#NEED} KE a tick in all. On a tick it gets all of it, it
 * runs, and it keeps running {@value OrganGrinderSkeletonBlock#SPIN_DOWN} ticks after; it sets its block's
 * {@link OrganGrinderSkeletonBlock#RUNNING} only when that changes. While it runs it plays the organ's waltz from the
 * top, a step every {@value OrganGrinderSkeletonBlock#STEP_TICKS} ticks. The energy goes into its clockwork, so nothing
 * comes back out; it saves nothing.
 */
public class OrganGrinderSkeletonBlockEntity extends BlockEntity implements KineticConsumer {
	private long tick = -1;
	private long taken;
	/** The last tick it got all it needs, or none yet. */
	private long fed = Long.MIN_VALUE / 2;
	/** Ticks it has run since it last started, for its tune. */
	private int played;

	public OrganGrinderSkeletonBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.ORGAN_GRINDER_SKELETON_ENTITY, pos, state);
	}

	@Override
	public long acceptKinetic(Direction side, long maxAmount) {
		if (!(level instanceof ServerLevel server)) {
			return 0;
		}
		long now = server.getGameTime();
		if (now != tick) {
			tick = now;
			taken = 0;
		}
		long take = Math.min(maxAmount, OrganGrinderSkeletonBlock.NEED - taken);
		if (take <= 0) {
			return 0;
		}
		taken += take;
		if (taken >= OrganGrinderSkeletonBlock.NEED) {
			fed = now;
		}
		return take;
	}

	/** Whether it has had all it needs lately enough to be running at {@code now}. */
	public boolean powered(long now) {
		return now - fed <= OrganGrinderSkeletonBlock.SPIN_DOWN;
	}

	void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
		boolean running = powered(level.getGameTime());
		if (running != state.getValue(OrganGrinderSkeletonBlock.RUNNING)) {
			OrganGrinderSkeletonBlock.run(level, pos, state, running);
		}
		if (!running) {
			played = 0;
			return;
		}
		if (played % OrganGrinderSkeletonBlock.STEP_TICKS == 0) {
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
		played++;
	}

	/** Ticks it has run since it last started (for tests). */
	public int played() {
		return played;
	}
}
