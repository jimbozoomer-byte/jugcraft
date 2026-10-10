package io.github.jimbozoomer.jugcraft.lair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A soul brazier round a lair's arena: a squat black-stone pedestal and an iron bowl of soul fire. A lair fixture (the
 * fight lights and puts them out); it cannot be broken.
 */
public class LairBrazierBlock extends Block {
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14);

	public LairBrazierBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(LIT, true));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getValue(LIT) && random.nextInt(3) == 0) {
			level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.85,
					pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, 0.03, 0.0);
		}
	}
}
