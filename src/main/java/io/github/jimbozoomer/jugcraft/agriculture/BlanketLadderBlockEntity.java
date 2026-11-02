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
 * The throws on a Blanket Ladder ({@link BlanketLadderBlock}, on its first block): up to {@value BlanketLadderBlock#RUNGS}
 * wool carpets, one over each rung from the top down, each kept as it went in. Saved with the world and sent to clients,
 * which draw each as a throw in its carpet's colour; breaking the ladder gives them back.
 */
public class BlanketLadderBlockEntity extends BlockEntity {
	private final List<ItemStack> throwsOn = new ArrayList<>();

	public BlanketLadderBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.BLANKET_LADDER_ENTITY, pos, state);
	}

	/** The carpets over its rungs, from the top rung down. */
	public List<ItemStack> throwsOn() {
		return throwsOn.stream().map(ItemStack::copy).toList();
	}

	/** Hangs one of {@code stack} (left as it is: the caller spends it) over the next rung, if it is a carpet and one is free. */
	public boolean hang(ItemStack stack) {
		if (!stack.is(BlanketLadderBlock.CARPETS) || throwsOn.size() >= BlanketLadderBlock.RUNGS) {
			return false;
		}
		throwsOn.add(stack.copyWithCount(1));
		changed();
		return true;
	}

	/** Takes back the throw hung last (empty if there is none). */
	public ItemStack takeLast() {
		if (throwsOn.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack taken = throwsOn.removeLast();
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

	/** Breaking the ladder gives back its throws. */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level != null && !level.isClientSide()) {
			for (ItemStack stack : throwsOn) {
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
			}
		}
		throwsOn.clear();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		throwsOn.clear();
		for (ItemStack stack : input.read("throws", ItemStack.CODEC.listOf()).orElse(List.of())) {
			if (stack.is(BlanketLadderBlock.CARPETS) && throwsOn.size() < BlanketLadderBlock.RUNGS) {
				throwsOn.add(stack.copyWithCount(1));
			}
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("throws", ItemStack.CODEC.listOf(), List.copyOf(throwsOn));
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
