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
 * The Pumpkin-Headed Drummer (Clockwork Animatronics, part 5; tools/clockwork.py PUMPKIN_DRUMMER): a bandsman 1.7 m tall
 * whose head is a carved jack-o'-lantern under a black shako, in a red coat frogged with gold, a rope-tensioned bass drum
 * slung in front of him, two blocks tall. His clockwork runs on kinetic power, {@value #NEED} KE a tick
 * ({@link ClockworkFigureBlockEntity}). While he runs ({@link #RUNNING}) he beats a march ({@link #PATTERN}), a stroke of
 * each mallet a backswing out from the drumhead and in again ending on its beat ({@link #stroke}), his head dipping in
 * time ({@link #nod}) and his carved face lit (drawn by client/PumpkinDrummerRenderer, the drum sounded by
 * {@link PumpkinDrummerBlockEntity}). His model is tools/clockwork_data.py pumpkin_drummer().
 */
public class PumpkinDrummerBlock extends TallDecorationBlock implements EntityBlock, ClockworkFigure {
	public static final int NEED = 4;
	public static final int SPIN_DOWN = 10;
	/** Ticks a step of the march takes. */
	public static final int STEP_TICKS = 5;
	/** The march, a step a character: R the right mallet, L the left, . a rest. */
	public static final String PATTERN = "R...R...R.L.R.L.";
	/** Ticks a stroke's backswing takes, out from the drumhead and in again. */
	public static final int BACK_TICKS = 8;
	/** Steps from one dip of his head to the next. */
	public static final int NOD_STEPS = 4;
	/** The drum's loudness, and its pitch under the right mallet and the left. */
	public static final float VOLUME = 0.9F;
	public static final float PITCH_RIGHT = 0.6F;
	public static final float PITCH_LEFT = 0.75F;

	public PumpkinDrummerBlock(Properties properties) {
		super(properties, Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), Block.box(3.0, 0.0, 3.0, 13.0, 15.0, 13.0));
		registerDefaultState(defaultBlockState().setValue(RUNNING, false));
	}

	/** The mallet that beats on {@code step} of the march ('R', 'L'), or '.' for a rest. */
	public static char beat(int step) {
		return PATTERN.charAt(Math.floorMod(step, PATTERN.length()));
	}

	/**
	 * How far the {@code hand} mallet ('R' or 'L') is swung back from the drumhead, {@code ticks} into his run: 0 at the
	 * head, 1 at the top of a backswing. Each of its beats is a backswing over the {@value #BACK_TICKS} ticks before it,
	 * ending at the head on the beat.
	 */
	public static float stroke(double ticks, char hand) {
		int length = PATTERN.length() * STEP_TICKS;
		double t = ticks % length;
		double back = 0.0;
		for (int i = 0; i < PATTERN.length(); i++) {
			if (PATTERN.charAt(i) == hand) {
				double until = ((i * STEP_TICKS - t) % length + length) % length;
				if (until < BACK_TICKS) {
					back = Math.max(back, 0.5 - 0.5 * Math.cos(2.0 * Math.PI * until / BACK_TICKS));
				}
			}
		}
		return (float) back;
	}

	/** How far his head dips, {@code ticks} into his run: all the way on every {@value #NOD_STEPS}th step, up between. */
	public static float nod(double ticks) {
		return (float) (0.5 + 0.5 * Math.cos(2.0 * Math.PI * ticks / (NOD_STEPS * STEP_TICKS)));
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
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new PumpkinDrummerBlockEntity(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide() || state.getValue(HALF) != DoubleBlockHalf.LOWER || type != JugcraftAgriculture.PUMPKIN_DRUMMER_ENTITY) {
			return null;
		}
		return (tickLevel, pos, tickState, entity) -> ((PumpkinDrummerBlockEntity) entity).serverTick((ServerLevel) tickLevel, pos, tickState);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNING);
	}
}
