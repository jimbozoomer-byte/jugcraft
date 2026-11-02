package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A Chalkboard Sign's own words, if it has any: kept, carried and sent to clients as a Spooky Sign's are
 * ({@link SpookySignBlockEntity}); without them the boards show the market's {@link ChalkboardSignBlock#WORDS}.
 */
public class ChalkboardSignBlockEntity extends SpookySignBlockEntity {
	public ChalkboardSignBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.CHALKBOARD_SIGN_ENTITY, pos, state);
	}
}
