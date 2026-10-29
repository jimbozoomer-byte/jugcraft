package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.kinetic.KineticConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * A Clockwork Animatronic's clockwork ({@link ClockworkFigure}): it takes kinetic energy from any shaft, gearbox or
 * source touching its block, up to the figure's {@link ClockworkFigure#need} KE a tick in all. On a tick it gets all of
 * it, the figure runs, and it keeps running {@link ClockworkFigure#spinDown} ticks after; the figure's
 * {@link ClockworkFigure#RUNNING} is set only when that changes. While it runs, each tick {@link #play} does what the
 * figure does on the server (a tune, a howl), counted from when it last started. The energy goes into its clockwork, so
 * nothing comes back out; it saves nothing.
 *
 * <p>A Show Controller ({@link ShowControllerBlockEntity}) may drive it instead, without shafts: it finds it in the
 * figures each level keeps as they tick ({@link #near}), holds it still while it cues a show ({@link #hold}), then gives
 * it all it needs each tick and its count, one count for every figure it drives ({@link #cue}). One controller drives a
 * figure at a time ({@link #free}).
 */
public abstract class ClockworkFigureBlockEntity extends BlockEntity implements KineticConsumer {
	/** The figures on each server level (their lower blocks), kept as they tick, so a controller never searches blocks. */
	private static final Map<Level, Set<BlockPos>> FIGURES = new WeakHashMap<>();
	private long tick = -1;
	private long taken;
	/** The last tick it got all it needs, or none yet. */
	private long fed = Long.MIN_VALUE / 2;
	/** Ticks it has run since it last started. */
	private int played;
	/** The tick a show controller holds it still until, while it cues a show; it doesn't run before. */
	private long heldUntil = Long.MIN_VALUE;
	/** The show controller that last held or drove it, and the tick it did. */
	private @Nullable BlockPos controller;
	private long cued = Long.MIN_VALUE / 2;

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
		return getBlockState().getBlock() instanceof ClockworkFigure figure && now >= heldUntil && now - fed <= figure.spinDown();
	}

	void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
		if (!(state.getBlock() instanceof ClockworkFigure figure)) {
			return;
		}
		FIGURES.computeIfAbsent(level, l -> new HashSet<>()).add(worldPosition);
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

	/** Whether the show controller at {@code by} may drive it at {@code now}: no other has held or driven it since the last tick. */
	public boolean free(BlockPos by, long now) {
		return controller == null || controller.equals(by) || now - cued > 1;
	}

	/** Holds it still until {@code until} for the show controller at {@code by}, cueing a show: its count from the top. */
	public void hold(BlockPos by, long now, long until) {
		controller = by;
		cued = now;
		heldUntil = until;
		played = 0;
	}

	/** Drives it from the show controller at {@code by}: all it needs at {@code now}, {@code show} ticks into the show. */
	public void cue(BlockPos by, long now, int show) {
		controller = by;
		cued = now;
		fed = now;
		played = show;
	}

	/** The figures on {@code level} within {@code range} blocks of {@code pos}, nearest first; forgets any that are gone. */
	public static List<ClockworkFigureBlockEntity> near(ServerLevel level, BlockPos pos, int range) {
		List<ClockworkFigureBlockEntity> near = new ArrayList<>();
		Set<BlockPos> figures = FIGURES.get(level);
		if (figures == null) {
			return near;
		}
		for (Iterator<BlockPos> it = figures.iterator(); it.hasNext(); ) {
			BlockPos at = it.next();
			if (at.distSqr(pos) > (double) range * range) {
				continue;
			}
			if (!level.isLoaded(at) || !(level.getBlockEntity(at) instanceof ClockworkFigureBlockEntity figure)) {
				it.remove();
				continue;
			}
			near.add(figure);
		}
		near.sort(Comparator.comparingDouble(figure -> figure.getBlockPos().distSqr(pos)));
		return near;
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		// Server levels only: the map is the server's, and client block entities are removed on the render thread.
		if (level != null && !level.isClientSide() && FIGURES.containsKey(level)) {
			FIGURES.get(level).remove(worldPosition);
		}
	}
}
