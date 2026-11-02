package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Simmer Pot (Cozy Fall Hearth, part 2; tools/hearth.py SIMMER_POT): a little enamel saucepan of spiced water with
 * apple slices, cinnamon sticks and cloves in it, set on the stove ({@link StovetopBlock}) to scent the room. Heated, it
 * simmers: its surface moves (the blockstate's simmering surface, an animated texture in smooth bands) and wisps of
 * steam rise from it. It is a decoration: it never boils dry and makes nothing. Its model is tools/hearth_data.py
 * simmer_pot().
 */
public class SimmerPotBlock extends StovetopBlock {
	public SimmerPotBlock(Properties properties) {
		super(properties, new double[] {5.75, 0.0, 5.75, 10.25, 2.75, 13.25});
	}

	/** Heated, wisps of steam rise from it now and then (each client's own, near it). */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getValue(HEATED) && random.nextInt(4) == 0) {
			level.addParticle(ParticleTypes.WHITE_SMOKE, pos.getX() + 0.4 + random.nextDouble() * 0.2, pos.getY() + 0.2,
					pos.getZ() + 0.55 + random.nextDouble() * 0.2, 0.0, 0.02, 0.0);
		}
	}
}
