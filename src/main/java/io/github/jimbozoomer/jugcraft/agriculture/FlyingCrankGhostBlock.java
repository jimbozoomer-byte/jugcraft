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
 * The Flying Crank Ghost (Clockwork Animatronics, part 3; tools/clockwork.py FLYING_GHOST): a hooded cheesecloth ghost
 * 1.3 m long hanging in the air on three wires from a crank on a black iron post 2 m tall, two blocks tall. Its
 * clockwork runs on kinetic power, {@value #NEED} KE a tick ({@link ClockworkFigureBlockEntity}). While it runs
 * ({@link #RUNNING}) the crank turns once round every {@value #CRANK_TICKS} ticks, so the ghost rides a small circle and
 * sways, its arms rising in turn as the wires over the crossbar's pulleys draw them up, and its eyes glow (drawn by
 * client/FlyingCrankGhostRenderer); every {@value #MOAN_EVERY} ticks it moans ({@link FlyingCrankGhostBlockEntity}).
 * Its model is tools/clockwork_data.py flying_crank_ghost().
 */
public class FlyingCrankGhostBlock extends TallDecorationBlock implements EntityBlock, ClockworkFigure {
	public static final int NEED = 4;
	public static final int SPIN_DOWN = 10;
	/** Ticks the crank takes to turn once round. */
	public static final int CRANK_TICKS = 40;
	/** Ticks from one moan to the next, the first as it starts. */
	public static final int MOAN_EVERY = 200;
	/** The moan's loudness and pitch. */
	public static final float VOLUME = 0.6F;
	public static final float PITCH = 0.9F;

	public FlyingCrankGhostBlock(Properties properties) {
		super(properties, Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
		registerDefaultState(defaultBlockState().setValue(RUNNING, false));
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
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new FlyingCrankGhostBlockEntity(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide() || state.getValue(HALF) != DoubleBlockHalf.LOWER || type != JugcraftAgriculture.FLYING_CRANK_GHOST_ENTITY) {
			return null;
		}
		return (tickLevel, pos, tickState, entity) -> ((FlyingCrankGhostBlockEntity) entity).serverTick((ServerLevel) tickLevel, pos, tickState);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNING);
	}
}
