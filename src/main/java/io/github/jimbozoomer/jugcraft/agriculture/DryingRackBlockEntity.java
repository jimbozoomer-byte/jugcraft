package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * What hangs on a Drying Rack (on its first block): a bunch on each of its {@value DryingRackBlock#SLOTS} hooks, each up to
 * {@value DryingRackBlock#BUNCH} fresh herb sprigs or peppers and the game time it was hung. A bunch is dry once
 * {@value DryingRackBlock#DRY_TICKS} ticks have passed since, worked out from the time whenever it is looked at, so
 * nothing ticks; the block schedules one tick to show it dry. The server alone changes them; broken, the rack gives
 * back each bunch, dried if it has dried.
 */
public class DryingRackBlockEntity extends BlockEntity {
	private final ItemStack[] bunches = new ItemStack[DryingRackBlock.SLOTS];
	private final long[] hungAt = new long[DryingRackBlock.SLOTS];

	public DryingRackBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.DRYING_RACK_ENTITY, pos, state);
		Arrays.fill(bunches, ItemStack.EMPTY);
	}

	public ItemStack bunch(int slot) {
		return bunches[slot];
	}

	/** Whether the bunch on {@code slot} has dried by {@code now}. */
	public boolean dried(int slot, long now) {
		return !bunches[slot].isEmpty() && now - hungAt[slot] >= DryingRackBlock.DRY_TICKS;
	}

	/** What hangs on {@code slot} at {@code now}, as the rack shows it. */
	public DryingRackBlock.Bunch shown(int slot, long now) {
		return DryingRackBlock.Bunch.of(bunches[slot], dried(slot, now));
	}

	/** The ticks from {@code now} until the next bunch still drying is dry, or -1 if none is. */
	public long untilNextDry(long now) {
		long next = -1;
		for (int slot = 0; slot < bunches.length; slot++) {
			if (!bunches[slot].isEmpty() && !dried(slot, now)) {
				long left = DryingRackBlock.DRY_TICKS - (now - hungAt[slot]);
				next = next < 0 ? left : Math.min(next, left);
			}
		}
		return next;
	}

	/** Hangs a bunch of up to {@link DryingRackBlock#BUNCH} of {@code stack} on the first free hook, taking them from it; the hook, or -1. */
	public int hang(ItemStack stack, long now) {
		for (int slot = 0; slot < bunches.length; slot++) {
			if (bunches[slot].isEmpty()) {
				bunches[slot] = stack.split(Math.min(DryingRackBlock.BUNCH, stack.getCount()));
				hungAt[slot] = now;
				setChanged();
				return slot;
			}
		}
		return -1;
	}

	/** Takes down the first dried bunch, dried, or else the last hung, fresh; empty if nothing hangs. */
	public ItemStack takeDown(long now) {
		int chosen = -1;
		for (int slot = 0; slot < bunches.length && chosen < 0; slot++) {
			if (dried(slot, now)) {
				chosen = slot;
			}
		}
		for (int slot = bunches.length - 1; slot >= 0 && chosen < 0; slot--) {
			if (!bunches[slot].isEmpty()) {
				chosen = slot;
			}
		}
		if (chosen < 0) {
			return ItemStack.EMPTY;
		}
		ItemStack out = given(chosen, now);
		bunches[chosen] = ItemStack.EMPTY;
		setChanged();
		return out;
	}

	/** What the bunch on {@code slot} gives back at {@code now}: its dried form once dry, else itself. */
	private ItemStack given(int slot, long now) {
		ItemStack bunch = bunches[slot];
		return dried(slot, now) ? DryingRackBlock.driedForm(bunch) : bunch.copy();
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level != null && !level.isClientSide()) {
			long now = level.getGameTime();
			for (int slot = 0; slot < bunches.length; slot++) {
				if (!bunches[slot].isEmpty()) {
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), given(slot, now));
				}
			}
		}
		Arrays.fill(bunches, ItemStack.EMPTY);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		for (int slot = 0; slot < bunches.length; slot++) {
			ItemStack bunch = input.read("bunch_" + slot, ItemStack.CODEC).orElse(ItemStack.EMPTY);
			if (bunch.getCount() > DryingRackBlock.BUNCH) {
				bunch.setCount(DryingRackBlock.BUNCH);
			}
			bunches[slot] = DryingRackBlock.hangs(bunch) ? bunch : ItemStack.EMPTY;
			hungAt[slot] = input.getLongOr("hung_" + slot, 0L);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		for (int slot = 0; slot < bunches.length; slot++) {
			if (!bunches[slot].isEmpty()) {
				output.store("bunch_" + slot, ItemStack.CODEC, bunches[slot]);
				output.putLong("hung_" + slot, hungAt[slot]);
			}
		}
	}
}
