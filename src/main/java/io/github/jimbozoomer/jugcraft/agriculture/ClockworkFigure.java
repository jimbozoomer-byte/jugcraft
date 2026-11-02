package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * A Clockwork Animatronic's block (decor set 9; tools/clockwork.py): a figure moved by clockwork on kinetic power. Its
 * block entity ({@link ClockworkFigureBlockEntity}) takes up to {@link #need} KE a tick and runs it only on all of it,
 * keeping it running {@link #spinDown} ticks after; the block shows whether it runs in {@link #RUNNING}, which the client
 * draws it moving from.
 */
public interface ClockworkFigure {
	BooleanProperty RUNNING = BooleanProperty.create("running");

	/** The KE a tick its clockwork takes; it runs only on all of it. */
	int need();

	/** Ticks it keeps running after its last full tick of power, as a shaft keeps turning. */
	int spinDown();

	/** Sets whether the figure whose clockwork is at {@code pos} runs (on every block of it). */
	void setRunning(ServerLevel level, BlockPos pos, BlockState state, boolean running);
}
