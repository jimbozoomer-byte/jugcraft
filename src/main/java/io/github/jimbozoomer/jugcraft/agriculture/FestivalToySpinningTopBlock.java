package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Spinning Top (Thanksgiving festival toys, tools/thanksgiving_toys.py TOP; docs/features/thanksgiving-toys.md): a
 * turned and painted top that stands at rest on its tip until it is used. Used, it spins on its tip: {@link #SPIN}
 * counts down from {@value #STEPS} to 0, one scheduled tick a step ({@link #delay}: quick at first, slower as it winds
 * down), each step drawing the next sixteenth of a turn (the blockstate's four frames) and, for the last {@value #WOBBLE}
 * steps, leaning and precessing a quarter turn a step before it comes to rest. A spin always ends: it is at
 * most {@value #STEPS} scheduled ticks, started only by a use while it is still, and using it while it spins does
 * nothing. Everything is block state, changed only on the server and sent to everyone who can see it.
 *
 * <p>It has no block entity and holds nothing, so in a town spinning it is a public interaction (TownProtection's
 * publicBlock); claimed land keeps it to the claim's members, and breaking it is protected everywhere as any block.
 */
public class FestivalToySpinningTopBlock extends FacingDecorationBlock {
	public static final int STEPS = 32;
	public static final int WOBBLE = 8;
	public static final IntegerProperty SPIN = IntegerProperty.create("spin", 0, STEPS);

	/** {@code box}: its outline {x0, y0, z0, x1, y1, z1} in pixels, facing north (covering it lying, upright and leaning). */
	public FestivalToySpinningTopBlock(Properties properties, double[] box) {
		super(properties, box);
		registerDefaultState(defaultBlockState().setValue(SPIN, 0));
	}

	/** The ticks it waits at step {@code step} ({@value #STEPS} down to 1) before the next (tools/thanksgiving_toys.py top_delay). */
	public static int delay(int step) {
		if (step > 20) {
			return 1;
		}
		if (step > 12) {
			return 2;
		}
		if (step > WOBBLE) {
			return 3;
		}
		return new int[] {8, 8, 6, 6, 5, 5, 4, 4}[step - 1];
	}

	/** The ticks a whole spin takes, from the use to resting still again. */
	public static int spinTicks() {
		int ticks = 0;
		for (int step = 1; step <= STEPS; step++) {
			ticks += delay(step);
		}
		return ticks;
	}

	public static boolean spinning(BlockState state) {
		return state.getValue(SPIN) > 0;
	}

	/** Lying still, a use spins it; spinning, a use does nothing. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level instanceof ServerLevel server && !spinning(state)) {
			spin(server, pos, state, player);
		}
		return InteractionResult.SUCCESS;
	}

	/** Starts a spin: the first step now, the rest by scheduled ticks. Returns whether it started (not while it spins). */
	public static boolean spin(ServerLevel level, BlockPos pos, BlockState state, Player player) {
		if (!(state.getBlock() instanceof FestivalToySpinningTopBlock top) || spinning(state)) {
			return false;
		}
		level.setBlock(pos, state.setValue(SPIN, STEPS), Block.UPDATE_CLIENTS);
		level.scheduleTick(pos, top, delay(STEPS));
		level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.5F, 1.6F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return true;
	}

	/** One step of the spin: the next frame, and the next step scheduled until it rests still. */
	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int spin = state.getValue(SPIN);
		if (spin <= 0) {
			return;
		}
		int next = spin - 1;
		level.setBlock(pos, state.setValue(SPIN, next), Block.UPDATE_CLIENTS);
		if (next > 0) {
			level.scheduleTick(pos, this, delay(next));
		} else {
			level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.5F, 1.4F);
		}
	}

	/** A top set down mid-spin by other means (a structure, a command) still winds down rather than spinning forever. */
	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
		super.onPlace(state, level, pos, old, moved);
		if (!level.isClientSide() && !old.is(this) && spinning(state) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
			level.scheduleTick(pos, this, delay(state.getValue(SPIN)));
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(SPIN);
	}
}
