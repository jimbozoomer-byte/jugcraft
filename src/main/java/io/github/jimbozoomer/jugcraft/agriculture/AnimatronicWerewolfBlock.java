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
 * The Animatronic Werewolf (Clockwork Animatronics, part 2; tools/clockwork.py WEREWOLF): a werewolf 1.75 m tall
 * hunched, in torn trousers, on a brass turntable on a black iron plinth, two blocks tall. Its clockwork runs on kinetic
 * power, {@value #NEED} KE a tick ({@link ClockworkFigureBlockEntity}). While it runs ({@link #RUNNING}) the turntable
 * turns it round once every {@value #TURN_TICKS} ticks, and every {@value #HOWL_EVERY} ticks it raises its claws, throws
 * back its head and howls ({@link #howl}; drawn by client/AnimatronicWerewolfRenderer, its howl played by
 * {@link AnimatronicWerewolfBlockEntity}). Its model is tools/clockwork_data.py animatronic_werewolf().
 */
public class AnimatronicWerewolfBlock extends TallDecorationBlock implements EntityBlock, ClockworkFigure {
	public static final int NEED = 8;
	public static final int SPIN_DOWN = 10;
	/** Ticks the turntable takes to turn it once round. */
	public static final int TURN_TICKS = 320;
	/** Ticks from one howl to the next, its arms taking {@value #RAISE_TICKS} to rise and to fall, held {@value #HOLD_TICKS}. */
	public static final int HOWL_EVERY = 160;
	public static final int RAISE_TICKS = 12;
	public static final int HOLD_TICKS = 24;
	/** The howl's loudness and pitch. */
	public static final float VOLUME = 1.2F;
	public static final float PITCH = 0.7F;

	public AnimatronicWerewolfBlock(Properties properties) {
		super(properties, Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), Block.box(3.5, 0.0, 3.0, 12.5, 13.0, 12.0));
		registerDefaultState(defaultBlockState().setValue(RUNNING, false));
	}

	/** The tick of each howl's cycle at which its arms start to rise: the howl ends with the cycle. */
	public static int howlStart() {
		return HOWL_EVERY - 2 * RAISE_TICKS - HOLD_TICKS;
	}

	/**
	 * How far into its howl the werewolf is, {@code ticks} into its run: 0 at rest, rising to 1 over {@value #RAISE_TICKS}
	 * ticks (eased), 1 while it holds its howl, falling back to 0 over {@value #RAISE_TICKS} again.
	 */
	public static float howl(double ticks) {
		double h = ticks % HOWL_EVERY - howlStart();
		double up = h < 0 ? 0.0 : h < RAISE_TICKS ? h / RAISE_TICKS : h < RAISE_TICKS + HOLD_TICKS ? 1.0
				: Math.max(0.0, 1.0 - (h - RAISE_TICKS - HOLD_TICKS) / RAISE_TICKS);
		return (float) (up * up * (3.0 - 2.0 * up));
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
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new AnimatronicWerewolfBlockEntity(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide() || state.getValue(HALF) != DoubleBlockHalf.LOWER || type != JugcraftAgriculture.ANIMATRONIC_WEREWOLF_ENTITY) {
			return null;
		}
		return (tickLevel, pos, tickState, entity) -> ((AnimatronicWerewolfBlockEntity) entity).serverTick((ServerLevel) tickLevel, pos, tickState);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNING);
	}
}
