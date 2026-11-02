package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jspecify.annotations.Nullable;

/**
 * The Tea Kettle (Cozy Fall Hearth, part 2; tools/hearth.py KETTLE): a hammered copper kettle with a brass bail handle,
 * set on the stove ({@link StovetopBlock}). Heated, it boils: steam rises from its spout, it simmers aloud now and then,
 * and its lid rattles, drawn by client/TeaKettleRenderer from the blockstate's {@link #RUNNER} model (the lid alone,
 * which a placed kettle never takes; the block's own model is the kettle without its lid). It brews nothing: tea is the
 * Cooking Pot's. Its model is tools/hearth_data.py kettle().
 */
public class TeaKettleBlock extends StovetopBlock implements EntityBlock {
	public static final BooleanProperty RUNNER = BooleanProperty.create("runner");
	/** How high its lid jumps as it rattles, in pixels, and how far it tips, in degrees. */
	public static final float LID_LIFT = 0.35F;
	public static final float LID_TIP = 5.0F;

	public TeaKettleBlock(Properties properties) {
		super(properties, new double[] {3.25, 0.0, 5.75, 10.25, 7.0, 10.25});
		registerDefaultState(defaultBlockState().setValue(RUNNER, false));
	}

	/**
	 * How much its lid is rattling at {@code time} (ticks, with the partial tick), from 0 to 1: two quick beats out of
	 * step, so it jumps unevenly, as a lid on a boil does; nothing while it is cold.
	 */
	public static float rattle(BlockPos pos, boolean heated, float time) {
		if (!heated) {
			return 0.0F;
		}
		float phase = (pos.hashCode() & 0xFF) / 40.0F;
		float beat = (float) (Math.sin(time * 1.7 + phase) * Math.sin(time * 0.43 + phase * 2.0));
		return Math.max(0.0F, beat);
	}

	/** Heated, steam rises from its spout (facing its right, as the model has it) and now and then it simmers aloud. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(HEATED)) {
			return;
		}
		Direction spout = state.getValue(FACING).getCounterClockWise();
		double x = pos.getX() + 0.5 + spout.getStepX() * 0.36;
		double z = pos.getZ() + 0.5 + spout.getStepZ() * 0.36;
		if (random.nextInt(3) == 0) {
			level.addParticle(ParticleTypes.WHITE_SMOKE, x, pos.getY() + 0.42, z, spout.getStepX() * 0.01, 0.04, spout.getStepZ() * 0.01);
		}
		if (random.nextInt(40) == 0) {
			level.playLocalSound(x, pos.getY() + 0.3, z, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.4F,
					0.8F + random.nextFloat() * 0.3F, false);
		}
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DecorationBlockEntity(JugcraftAgriculture.TEA_KETTLE_ENTITY, pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNER);
	}
}
