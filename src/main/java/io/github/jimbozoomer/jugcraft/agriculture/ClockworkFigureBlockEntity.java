package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.kinetic.KineticConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A Clockwork Animatronic's clockwork ({@link ClockworkFigure}): it takes kinetic energy from any shaft, gearbox or
 * source touching its block, up to the figure's {@link ClockworkFigure#need} KE a tick in all. On a tick it gets all of
 * it, the figure runs, and it keeps running {@link ClockworkFigure#spinDown} ticks after; the figure's
 * {@link ClockworkFigure#RUNNING} is set only when that changes. While it runs, each tick {@link #play} does what the
 * figure does on the server (a tune, a howl), counted from when it last started. The energy goes into its clockwork, so
 * nothing comes back out; it saves nothing.
 */
public abstract class ClockworkFigureBlockEntity extends BlockEntity implements KineticConsumer {
	private long tick = -1;
	private long taken;
	/** The last tick it got all it needs, or none yet. */
	private long fed = Long.MIN_VALUE / 2;
	/** Ticks it has run since it last started. */
	private int played;

	protected ClockworkFigureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public long acceptKinetic(Direction side, long maxAmount) {
		if (!(level instanceof ServerLevel server) || !(getBlockState().getBlock() instanceof ClockworkFigure figure)) {
			return 0;
		}
		long now = server.getGameTime();
		if (now != tick) {
			tick = now;
			taken = 0;
		}
		long take = Math.min(maxAmount, figure.need() - taken);
		if (take <= 0) {
			return 0;
		}
		taken += take;
		if (taken >= figure.need()) {
			fed = now;
		}
		return take;
	}

	/** Whether it has had all it needs lately enough to be running at {@code now}. */
	public boolean powered(long now) {
		return getBlockState().getBlock() instanceof ClockworkFigure figure && now - fed <= figure.spinDown();
	}

	void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
		if (!(state.getBlock() instanceof ClockworkFigure figure)) {
			return;
		}
		boolean running = powered(level.getGameTime());
		if (running != state.getValue(ClockworkFigure.RUNNING)) {
			figure.setRunning(level, pos, state, running);
		}
		if (!running) {
			played = 0;
			return;
		}
		play(level, pos, played);
		played++;
	}

	/** What the figure does on the server on the {@code played}th tick since it last started (from 0). */
	protected abstract void play(ServerLevel level, BlockPos pos, int played);

	/** Ticks it has run since it last started (for tests). */
	public int played() {
		return played;
	}
}
