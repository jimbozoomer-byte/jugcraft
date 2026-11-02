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
 * What grows in a planter ({@link PlanterBlock}): one small flower in each spot taken, in the order they went in, at most
 * as many as the planter has spots. Saved with the world and sent to clients, which draw them; breaking the planter gives
 * them back.
 */
public class PlanterBlockEntity extends BlockEntity {
	private final List<ItemStack> plants = new ArrayList<>();

	public PlanterBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.PLANTER_ENTITY, pos, state);
	}

	/** How many spots its planter has. */
	public int spots() {
		return getBlockState().getBlock() instanceof PlanterBlock planter ? planter.planter().spots().length : 0;
	}

	/** What grows in it, spot by spot. */
	public List<ItemStack> plants() {
		return plants.stream().map(ItemStack::copy).toList();
	}

	/** Plants one of {@code stack} (left as it is: the caller spends it) in the next free spot, if it is plantable. */
	public boolean plant(ItemStack stack) {
		if (!PlanterBlock.plantable(stack) || plants.size() >= spots()) {
			return false;
		}
		plants.add(stack.copyWithCount(1));
		changed();
		return true;
	}

	/** Takes back the flower planted last (empty if there is none). */
	public ItemStack takeLast() {
		if (plants.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack taken = plants.removeLast();
		changed();
		return taken;
	}

	private void changed() {
		setChanged();
		if (level != null && !level.isClientSide()) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
		}
	}

	/** Breaking the planter gives back what grows in it. */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level != null && !level.isClientSide()) {
			for (ItemStack stack : plants) {
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
			}
		}
		plants.clear();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		plants.clear();
		for (ItemStack stack : input.read("plants", ItemStack.CODEC.listOf()).orElse(List.of())) {
			if (PlanterBlock.plantable(stack) && plants.size() < spots()) {
				plants.add(stack.copyWithCount(1));
			}
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("plants", ItemStack.CODEC.listOf(), List.copyOf(plants));
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
