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
 * What is in a Knitting Basket ({@link KnittingBasketBlock}): up to {@value KnittingBasketBlock#BALLS} balls of yarn, each
 * kept as it went in (its colour with it), and a pair of knitting needles (kept as they are, worn or not). Saved with the
 * world and sent to clients, which draw the balls in their colours; breaking the basket gives it all back.
 */
public class KnittingBasketBlockEntity extends BlockEntity {
	private final List<ItemStack> yarn = new ArrayList<>();
	private ItemStack needles = ItemStack.EMPTY;

	public KnittingBasketBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.KNITTING_BASKET_ENTITY, pos, state);
	}

	/** The balls of yarn in it, in the order they went in. */
	public List<ItemStack> yarn() {
		return yarn.stream().map(ItemStack::copy).toList();
	}

	/** The needles in it (empty if there are none). */
	public ItemStack needles() {
		return needles.copy();
	}

	/** Puts one of {@code stack} in (left as it is: the caller spends it): a ball of yarn while there is room, or needles. */
	public boolean put(ItemStack stack) {
		if (Knitting.isYarn(stack) && yarn.size() < KnittingBasketBlock.BALLS) {
			yarn.add(stack.copyWithCount(1));
		} else if (stack.is(JugcraftAgriculture.item("knitting_needles")) && needles.isEmpty()) {
			needles = stack.copyWithCount(1);
		} else {
			return false;
		}
		changed();
		return true;
	}

	/** Takes back the needles, or if there are none the ball put in last (empty if it is empty). */
	public ItemStack takeLast() {
		ItemStack taken;
		if (!needles.isEmpty()) {
			taken = needles;
			needles = ItemStack.EMPTY;
		} else if (!yarn.isEmpty()) {
			taken = yarn.removeLast();
		} else {
			return ItemStack.EMPTY;
		}
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

	/** Breaking the basket gives back what is in it. */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level != null && !level.isClientSide()) {
			for (ItemStack stack : yarn) {
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
			}
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), needles);
		}
		yarn.clear();
		needles = ItemStack.EMPTY;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		yarn.clear();
		for (ItemStack stack : input.read("yarn", ItemStack.CODEC.listOf()).orElse(List.of())) {
			if (Knitting.isYarn(stack) && yarn.size() < KnittingBasketBlock.BALLS) {
				yarn.add(stack.copyWithCount(1));
			}
		}
		needles = input.read("needles", ItemStack.CODEC).orElse(ItemStack.EMPTY);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("yarn", ItemStack.CODEC.listOf(), List.copyOf(yarn));
		if (!needles.isEmpty()) {
			output.store("needles", ItemStack.CODEC, needles);
		}
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
