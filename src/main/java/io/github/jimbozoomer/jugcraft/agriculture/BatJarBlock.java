package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * The Bat in a Jar (Halloween decorations batch 17): a bat hanging asleep in a dry jar. When a player comes within
 * {@value #WAKE_RANGE} blocks it wakes ({@link #AWAKE}), squeaks and flutters round the jar for {@value #FLUTTER_TICKS}
 * ticks (drawn by the client), then settles; it wakes again for the next player to come close ({@link #NEAR}). It looks
 * for players every {@value #CHECK_TICKS} ticks, nearby only.
 */
public class BatJarBlock extends OddityJarBlock {
	public static final int WAKE_RANGE = 3;
	public static final int FLUTTER_TICKS = 100;
	public static final int CHECK_TICKS = 10;
	public static final BooleanProperty AWAKE = BooleanProperty.create("awake");
	/** Whether a player was near at the last look, so it wakes as one comes rather than while one stays. */
	public static final BooleanProperty NEAR = BooleanProperty.create("near");

	public BatJarBlock(Properties properties) {
		super(properties, Kind.BAT);
		registerDefaultState(stateDefinition.any().setValue(AWAKE, false).setValue(NEAR, false));
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
		super.onPlace(state, level, pos, old, moved);
		if (!old.is(this)) {
			level.scheduleTick(pos, this, CHECK_TICKS);
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		boolean near = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, WAKE_RANGE, false) != null;
		boolean awake = state.getValue(AWAKE);
		long now = level.getGameTime();
		DecorationBlockEntity jar = level.getBlockEntity(pos) instanceof DecorationBlockEntity d ? d : null;
		if (near && !state.getValue(NEAR)) {
			awake = true;
			if (jar != null) {
				jar.mark(now);
			}
			level.playSound(null, pos, SoundEvents.BAT_TAKEOFF, SoundSource.BLOCKS, 0.5F, 1.4F);
		} else if (awake && (jar == null || now - jar.marked() >= FLUTTER_TICKS)) {
			awake = false;
		} else if (awake && random.nextInt(4) == 0) {
			level.playSound(null, pos, SoundEvents.BAT_AMBIENT, SoundSource.BLOCKS, 0.3F, 1.5F);
		}
		BlockState next = state.setValue(AWAKE, awake).setValue(NEAR, near);
		if (next != state) {
			level.setBlock(pos, next, Block.UPDATE_CLIENTS);
		}
		level.scheduleTick(pos, this, CHECK_TICKS);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AWAKE, NEAR);
	}
}
