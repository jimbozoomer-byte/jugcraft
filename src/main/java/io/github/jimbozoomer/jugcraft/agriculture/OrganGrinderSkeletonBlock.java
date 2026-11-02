package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jspecify.annotations.Nullable;

/**
 * The Organ-Grinder Skeleton (Clockwork Animatronics, part 1; tools/clockwork.py ORGAN_GRINDER): a skeleton 1.75 m tall
 * in a battered top hat and a moth-eaten red waistcoat, standing behind a street barrel organ (a hurdy-gurdy) on a
 * two-wheeled cart, two blocks tall. Its clockwork runs on kinetic power: a shaft, gearbox or source touching its lower
 * block gives it up to {@value #NEED} KE a tick ({@link ClockworkFigureBlockEntity}). While it runs ({@link #RUNNING})
 * its right hand turns the organ's crank, its head nods and its jaw chatters (drawn by the client,
 * client/OrganGrinderSkeletonRenderer), and the organ plays its waltz ({@link #MELODY}, {@link #BASS}). Its model is
 * tools/clockwork_data.py organ_grinder_skeleton().
 */
public class OrganGrinderSkeletonBlock extends TallDecorationBlock implements EntityBlock, ClockworkFigure {
	/** The KE a tick its clockwork takes; it runs only on all of it. */
	public static final int NEED = 4;
	/** Ticks it keeps running after its last full tick of power, as a shaft keeps turning. */
	public static final int SPIN_DOWN = 10;
	/** Ticks the organ's crank takes to go once round. */
	public static final int CRANK_TICKS = 20;
	/** Ticks between the waltz's steps (quavers), and its steps to a bar. */
	public static final int STEP_TICKS = 5;
	public static final int STEPS_PER_BAR = 6;
	/** The waltz's melody, a note block pitch a step (0 is F#3, 24 F#5), -1 where a note is held. */
	public static final int[] MELODY = {
			15, -1, 18, -1, 22, -1, 20, 18, 17, -1, 15, -1, 11, -1, 15, -1, 20, -1, 18, 17, 14, -1, 10, -1,
			15, -1, 18, -1, 22, -1, 23, 22, 20, -1, 18, -1, 17, -1, 14, -1, 10, -1, 15, -1, -1, -1, -1, -1};
	/** Each bar's bass note, played on its first step. */
	public static final int[] BASS = {15, 15, 8, 10, 15, 11, 10, 15};
	/** The waltz's loudness. */
	public static final float VOLUME = 0.6F;

	public OrganGrinderSkeletonBlock(Properties properties) {
		super(properties, Block.box(2.0, 0.0, 2.25, 14.0, 16.0, 13.0), Block.box(4.25, 0.0, 9.0, 11.75, 15.0, 13.0));
		registerDefaultState(defaultBlockState().setValue(RUNNING, false));
	}

	/** A note block's pitch for {@code note} (0 is F#3, 12 F#4, 24 F#5), as a sound's pitch. */
	public static float pitch(int note) {
		return (float) Math.pow(2.0, (note - 12) / 12.0);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new OrganGrinderSkeletonBlockEntity(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide() || state.getValue(HALF) != DoubleBlockHalf.LOWER || type != JugcraftAgriculture.ORGAN_GRINDER_SKELETON_ENTITY) {
			return null;
		}
		return (tickLevel, pos, tickState, entity) -> ((OrganGrinderSkeletonBlockEntity) entity).serverTick((ServerLevel) tickLevel, pos, tickState);
	}

	@Override
	public int need() {
		return NEED;
	}

	@Override
	public int spinDown() {
		return SPIN_DOWN;
	}

	/** Sets whether the figure at {@code lower} runs, on both its halves. */
	@Override
	public void setRunning(ServerLevel level, BlockPos lower, BlockState state, boolean running) {
		setBoth(level, lower, state.setValue(RUNNING, running));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNING);
	}
}
