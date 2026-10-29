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
 * The Clockwork Raven (Clockwork Animatronics, part 4; tools/clockwork.py CLOCKWORK_RAVEN): a life-size raven of black
 * enamelled steel plates with a gunmetal beak and red glass eyes, perched on a brass bird stand 1.3 m tall, its
 * clockwork in a riveted brass drum at the stand's foot, two blocks tall. Its clockwork runs on kinetic power,
 * {@value #NEED} KE a tick ({@link ClockworkFigureBlockEntity}). While it runs ({@link #RUNNING}) the drum's flywheel
 * turns, its head turns slowly from side to side, and every {@value #CYCLE} ticks it caws {@value #CAWS} times, its beak
 * opening ({@link #beak}) and its wings beating once with each caw ({@link #wings}); its eyes glow (drawn by
 * client/ClockworkRavenRenderer, its caws played by {@link ClockworkRavenBlockEntity}). Its model is
 * tools/clockwork_data.py clockwork_raven().
 */
public class ClockworkRavenBlock extends TallDecorationBlock implements EntityBlock, ClockworkFigure {
	public static final int NEED = 2;
	public static final int SPIN_DOWN = 10;
	/** Ticks the drum's flywheel takes to turn once round. */
	public static final int FLYWHEEL_TICKS = 20;
	/** Ticks its head takes to look to one side, to the other and back. */
	public static final int LOOK_TICKS = 120;
	/** Ticks from one burst of caws to the next, and the tick of the burst's first caw. */
	public static final int CYCLE = 120;
	public static final int BURST = 60;
	/** Caws in a burst, and the ticks between them (a wing beat each). */
	public static final int CAWS = 3;
	public static final int CAW_EVERY = 12;
	/** Ticks its beak takes to open for a caw (and to shut after it), and stays open. */
	public static final int BEAK_TICKS = 2;
	public static final int BEAK_HOLD = 4;
	/** The caw's loudness and pitch. */
	public static final float VOLUME = 0.8F;
	public static final float PITCH = 0.5F;

	public ClockworkRavenBlock(Properties properties) {
		super(properties, Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0), Block.box(3.0, 0.0, 3.0, 13.0, 13.0, 13.0));
		registerDefaultState(defaultBlockState().setValue(RUNNING, false));
	}

	/** Whether it caws on {@code tick} of its cycle (from 0). */
	public static boolean cawsAt(int tick) {
		int after = tick - BURST;
		return after >= 0 && after % CAW_EVERY == 0 && after / CAW_EVERY < CAWS;
	}

	/**
	 * How far its beak is open, {@code ticks} into its run: 0 shut, 1 wide. For each caw it opens over
	 * {@value #BEAK_TICKS} ticks before it, stays open {@value #BEAK_HOLD} and shuts over {@value #BEAK_TICKS}.
	 */
	public static float beak(double ticks) {
		double t = ticks % CYCLE;
		double open = 0.0;
		for (int i = 0; i < CAWS; i++) {
			double d = t - (BURST + i * CAW_EVERY);
			double o = d < -BEAK_TICKS ? 0.0 : d < 0 ? 1.0 + d / BEAK_TICKS : d < BEAK_HOLD ? 1.0 : Math.max(0.0, 1.0 - (d - BEAK_HOLD) / BEAK_TICKS);
			open = Math.max(open, o);
		}
		return (float) open;
	}

	/**
	 * How far its wings are raised, {@code ticks} into its run, 0 folded to 1 at the top of a beat: one beat with each caw,
	 * from folded half a beat ({@value #CAW_EVERY} ticks) before it to the top at the caw and folded again half a beat
	 * after.
	 */
	public static float wings(double ticks) {
		double t = ticks % CYCLE - (BURST - CAW_EVERY / 2.0);
		if (t < 0.0 || t > CAWS * CAW_EVERY) {
			return 0.0F;
		}
		return (float) (0.5 - 0.5 * Math.cos(2.0 * Math.PI * t / CAW_EVERY));
	}

	@Override
	public int need() {
		return NEED;
	}

	@Override
	public int spinDown() {
		return SPIN_DOWN;
	}

	@Override
	public void setRunning(ServerLevel level, BlockPos lower, BlockState state, boolean running) {
		setBoth(level, lower, state.setValue(RUNNING, running));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new ClockworkRavenBlockEntity(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide() || state.getValue(HALF) != DoubleBlockHalf.LOWER || type != JugcraftAgriculture.CLOCKWORK_RAVEN_ENTITY) {
			return null;
		}
		return (tickLevel, pos, tickState, entity) -> ((ClockworkRavenBlockEntity) entity).serverTick((ServerLevel) tickLevel, pos, tickState);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNING);
	}
}
