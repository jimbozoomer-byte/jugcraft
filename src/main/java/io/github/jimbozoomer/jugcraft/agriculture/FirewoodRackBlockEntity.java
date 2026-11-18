package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The logs on a Firewood Rack ({@link FirewoodRackBlock}): each one kept as it went in (its kind remembered), in order,
 * at most {@value FirewoodRackBlock#CAPACITY}. Saved with the world; breaking the rack gives them back. The client draws
 * the rack's layers from its block state, so nothing here is sent.
 */
public class FirewoodRackBlockEntity extends BlockEntity {
	private final List<ItemStack> logs = new ArrayList<>();
	public final FarmStoreStorage storage = new FarmStoreStorage(this, logs, () -> FirewoodRackBlock.CAPACITY, stack -> stack.is(ItemTags.LOGS), this::storageChanged, true);

	private void storageChanged() {
		setChanged();
		if (level != null && !level.isClientSide() && getBlockState().getBlock() instanceof FirewoodRackBlock)
			level.setBlock(worldPosition, getBlockState().setValue(FirewoodRackBlock.LOGS, count()), net.minecraft.world.level.block.Block.UPDATE_ALL);
	}

	public FirewoodRackBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.FIREWOOD_RACK_ENTITY, pos, state);
	}

	/** How many logs are on it. */
	public int count() {
		return logs.size();
	}

	/** Stacks one of {@code stack} (left as it is: the caller spends it) on the rack, if it is a log and there is room. */
	public boolean stack(ItemStack stack) {
		if (!stack.is(ItemTags.LOGS) || logs.size() >= FirewoodRackBlock.CAPACITY) {
			return false;
		}
		logs.add(stack.copyWithCount(1));
		setChanged();
		return true;
	}

	/** Takes back the log stacked last (empty if there is none). */
	public ItemStack takeLast() {
		if (logs.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack taken = logs.removeLast();
		setChanged();
		return taken;
	}

	/** Breaking the rack gives back its logs. */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level != null && !level.isClientSide()) {
			for (ItemStack stack : logs) {
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
			}
		}
		logs.clear();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		logs.clear();
		for (ItemStack stack : input.read("logs", ItemStack.CODEC.listOf()).orElse(List.of())) {
			if (stack.is(ItemTags.LOGS) && logs.size() < FirewoodRackBlock.CAPACITY) {
				logs.add(stack.copyWithCount(1));
			}
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("logs", ItemStack.CODEC.listOf(), List.copyOf(logs));
	}
}
