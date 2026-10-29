package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A block worked by hand a stroke at a time (Barn & Harvest Tools, part 2): the Quern's turn of its stone, the Butter
 * Churn's plunge of its dasher. Each stroke runs {@link #strokeTicks} ticks, and another waits for it to finish
 * ({@link #ready}), so the work goes no faster than the hand can do it, and every stroke is one whole cycle of the moving
 * part (a turn of the stone, a plunge down and back). When the last stroke began is told to clients, which draw the
 * moving part from it ({@link #progress}); nothing ticks.
 */
public abstract class HandWorkedBlockEntity extends BlockEntity {
	/** When the last stroke began. */
	private long strokedAt = Long.MIN_VALUE / 2;

	protected HandWorkedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/** How long one stroke runs, in ticks. */
	protected abstract int strokeTicks();

	/** Whether the last stroke has finished at {@code now}, so another may begin. */
	public boolean ready(long now) {
		return now - strokedAt >= strokeTicks();
	}

	/** Begins a stroke at {@code now}; the caller has checked {@link #ready}. */
	protected void stroke(long now) {
		strokedAt = now;
	}

	/** How far through its last stroke it is at {@code time}: 0 to 1, and 1 once it has finished. */
	public float progress(long time, float partialTick) {
		return Math.min(1.0F, Math.max(0.0F, (time - strokedAt + partialTick) / strokeTicks()));
	}

	/** Saves the change and tells clients (the stroke, and whatever the subclass shows). */
	protected void changed() {
		setChanged();
		if (level != null) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		strokedAt = input.getLongOr("stroked_at", Long.MIN_VALUE / 2);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putLong("stroked_at", strokedAt);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveCustomOnly(registries);
	}
}
