package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A Hay-Loft Pulley's last haul: how far down its rope hung before it (the block state holds how far it hangs now), and,
 * from {@link HandWorkedBlockEntity}, when it began, so clients draw the rope running and the wheel turning between the
 * two. The server alone changes it.
 */
public class HayPulleyBlockEntity extends HandWorkedBlockEntity {
	private int from = 1;

	public HayPulleyBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.HAY_PULLEY_ENTITY, pos, state);
	}

	@Override
	protected int strokeTicks() {
		return HayPulleyBlock.HAUL_TICKS;
	}

	/** How many blocks down the rope hung before the last haul. */
	public int from() {
		return from;
	}

	/** Begins a haul at {@code now} from a rope {@code from} blocks down (the caller has checked {@link #ready}). */
	public void haul(long now, int from) {
		this.from = Math.clamp(from, 1, HayPulleyBlock.DROPS);
		stroke(now);
		changed();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		from = Math.clamp(input.getIntOr("from", 1), 1, HayPulleyBlock.DROPS);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("from", from);
	}
}
