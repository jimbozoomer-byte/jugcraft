package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * When a Harvest Bell was last rung (on its first block), from {@link HandWorkedBlockEntity}: clients draw it swinging
 * from then until its swing dies away; it can't be rung again before.
 */
public class HarvestBellBlockEntity extends HandWorkedBlockEntity {
	public HarvestBellBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.HARVEST_BELL_ENTITY, pos, state);
	}

	@Override
	protected int strokeTicks() {
		return HarvestBellBlock.SWING_TICKS;
	}

	/** Rings it at {@code now} (the caller has checked {@link #ready}). */
	public void ring(long now) {
		stroke(now);
		changed();
	}
}
