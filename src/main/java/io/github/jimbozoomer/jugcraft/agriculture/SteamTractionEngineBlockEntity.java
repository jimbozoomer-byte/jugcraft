package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The Steam Traction Engine's firebox, on its first block ({@link SteamTractionEngineBlock}): the ticks of coal left in
 * it, up to {@value SteamTractionEngineBlock#MAX_FUEL}. While there is any it burns a tick's worth each tick; when the
 * last is gone the engine is let out. It saves what is left.
 */
public class SteamTractionEngineBlockEntity extends BlockEntity {
	private int fuel;

	public SteamTractionEngineBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.STEAM_TRACTION_ENGINE_ENTITY, pos, state);
	}

	/** Ticks of coal left in the firebox. */
	public int fuel() {
		return fuel;
	}

	/** Puts {@code ticks} of coal in the firebox, if it has room for them all. */
	public boolean addFuel(int ticks) {
		if (fuel + ticks > SteamTractionEngineBlock.MAX_FUEL) {
			return false;
		}
		fuel += ticks;
		setChanged();
		return true;
	}

	/** Sets the ticks of coal left (tests), within the firebox's room. */
	public void setFuel(int ticks) {
		fuel = Math.clamp(ticks, 0, SteamTractionEngineBlock.MAX_FUEL);
		setChanged();
	}

	void serverTick(ServerLevel level, BlockPos pos) {
		if (fuel <= 0) {
			return;
		}
		fuel--;
		if (fuel == 0 && getBlockState().getBlock() instanceof SteamTractionEngineBlock engine) {
			engine.setLit(level, pos, false);
		}
		if (fuel % 20 == 0) {
			setChanged();
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		fuel = Math.clamp(input.getIntOr("fuel", 0), 0, SteamTractionEngineBlock.MAX_FUEL);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("fuel", fuel);
	}
}
