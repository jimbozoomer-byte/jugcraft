package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The fruit an Apple Bin or a Picking Bag holds ({@link FruitStore}): stacks of apples and pears in the order they went
 * in, at most the block's capacity in all. Saved with the world. Each change shows the block's new fill and top fruit on
 * it ({@link FruitStore#show}); breaking the block spills the fruit.
 */
public class FruitStoreBlockEntity extends BlockEntity {
	private final List<ItemStack> fruit = new ArrayList<>();

	public FruitStoreBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.FRUIT_STORE_ENTITY, pos, state);
	}

	/** How many fruit it holds when full. */
	public int capacity() {
		return getBlockState().getBlock() instanceof FruitStore store ? store.capacity() : 0;
	}

	/** How many fruit it holds. */
	public int count() {
		int count = 0;
		for (ItemStack stack : fruit) {
			count += stack.getCount();
		}
		return count;
	}

	public List<ItemStack> contents() {
		return fruit.stream().map(ItemStack::copy).toList();
	}

	/** Takes as much of {@code stack} as fits, if it is fruit, out of it; returns how many it took. */
	public int add(ItemStack stack) {
		int taken = Math.min(stack.getCount(), capacity() - count());
		if (!stack.is(FruitStore.FRUIT) || taken <= 0) {
			return 0;
		}
		ItemStack moving = stack.split(taken);
		if (!fruit.isEmpty() && ItemStack.isSameItemSameComponents(fruit.getLast(), moving)) {
			ItemStack last = fruit.getLast();
			int fits = Math.min(moving.getCount(), last.getMaxStackSize() - last.getCount());
			last.grow(fits);
			moving.shrink(fits);
		}
		while (!moving.isEmpty()) {
			fruit.add(moving.split(moving.getMaxStackSize()));
		}
		changed();
		return taken;
	}

	/** Takes back the stack that went in last (empty if there is none). */
	public ItemStack takeLast() {
		if (fruit.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack taken = fruit.removeLast();
		changed();
		return taken;
	}

	private void changed() {
		setChanged();
		if (level != null && !level.isClientSide() && getBlockState().getBlock() instanceof FruitStore store) {
			store.show(level, worldPosition, getBlockState(), FruitStore.fill(count(), store.capacity(), store.stages()),
					fruit.isEmpty() ? FruitStore.Kind.APPLE : FruitStore.Kind.of(fruit.getLast()));
		}
	}

	/** Breaking the block spills its fruit. */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level != null && !level.isClientSide()) {
			for (ItemStack stack : fruit) {
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
			}
		}
		fruit.clear();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		fruit.clear();
		int room = capacity();
		for (ItemStack stack : input.read("fruit", ItemStack.CODEC.listOf()).orElse(List.of())) {
			int count = Math.min(stack.getCount(), room);
			if (!stack.isEmpty() && count > 0) {
				fruit.add(stack.copyWithCount(count));
				room -= count;
			}
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("fruit", ItemStack.CODEC.listOf(), List.copyOf(fruit));
	}
}
