package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * What a Pumpkin Patch Market display holds ({@link ProduceDisplayBlock}): one piece of produce on each of its spots, in
 * the order they went on, at most as many as it has spots. Saved with the world and sent to clients, which draw them.
 */
public class ProduceDisplayBlockEntity extends BlockEntity {
	private final List<ItemStack> produce = new ArrayList<>();

	public ProduceDisplayBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.PRODUCE_DISPLAY_ENTITY, pos, state);
	}

	/** How many pieces it holds when full: one a spot. */
	public int capacity() {
		return getBlockState().getBlock() instanceof ProduceDisplayBlock display ? display.spots().length : 0;
	}

	public List<ItemStack> produce() {
		return List.copyOf(produce);
	}

	public boolean isFull() {
		return produce.size() >= capacity();
	}

	public boolean isEmpty() {
		return produce.isEmpty();
	}

	/** Sets one piece of produce on the next spot, if it is produce and there is room. */
	public boolean add(ItemStack one) {
		if (one.isEmpty() || !one.is(PumpkinCrateBlock.PRODUCE) || isFull()) {
			return false;
		}
		produce.add(one.copyWithCount(1));
		sync();
		return true;
	}

	/** Takes back the piece put on last (empty if there is none). */
	public ItemStack takeLast() {
		if (produce.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack taken = produce.removeLast();
		sync();
		return taken;
	}

	private void sync() {
		setChanged();
		if (level != null) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
		}
	}

	/** Breaking the display spills what is on it. */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level != null && !level.isClientSide()) {
			for (ItemStack stack : produce) {
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
			}
		}
		produce.clear();
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		produce.clear();
		int capacity = capacity();
		input.read("produce", ItemStack.CODEC.listOf()).ifPresent(list -> list.stream().filter(stack -> !stack.isEmpty())
				.limit(capacity > 0 ? capacity : list.size()).forEach(stack -> produce.add(stack.copyWithCount(1))));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("produce", ItemStack.CODEC.listOf(), List.copyOf(produce));
	}
}
