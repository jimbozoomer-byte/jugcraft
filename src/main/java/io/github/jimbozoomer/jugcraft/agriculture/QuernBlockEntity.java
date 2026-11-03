package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The grain in a Quern's eye (up to {@link QuernBlock#CAPACITY} of one kind) and how many turns it has had towards the
 * next ground piece; and, from {@link HandWorkedBlockEntity}, how far round its stone has turned. The server alone
 * changes them; the grain is spilt when the quern is broken.
 */
public class QuernBlockEntity extends HandWorkedBlockEntity {
	private ItemStack grain = ItemStack.EMPTY;
	private int turns;
	public final HandFarmCompanion companion = new HandFarmCompanion(this);
	void restoreInput(ItemStack stack, int progress) { grain = stack.copy(); turns = progress; }

	public QuernBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.QUERN_ENTITY, pos, state);
	}

	@Override
	protected int strokeTicks() {
		return QuernBlock.SPIN_TICKS;
	}

	public ItemStack grain() {
		return grain;
	}

	public int turns() {
		return turns;
	}

	/** Puts as much of {@code stack} in as fits (one kind of grain at a time), taking it from the stack; how many went in. */
	public int add(ItemStack stack) {
		if (!grain.isEmpty() && !ItemStack.isSameItemSameComponents(grain, stack)) {
			return 0;
		}
		int count = Math.min(stack.getCount(), QuernBlock.CAPACITY - grain.getCount());
		if (count <= 0) {
			return 0;
		}
		if (grain.isEmpty()) {
			grain = stack.copyWithCount(count);
		} else {
			grain.grow(count);
		}
		stack.shrink(count);
		changed();
		return count;
	}

	/** Gives back the grain not yet ground. */
	public ItemStack takeGrain() {
		ItemStack out = grain;
		grain = ItemStack.EMPTY;
		turns = 0;
		changed();
		return out;
	}

	/**
	 * One turn of the stone at {@code now} (the caller has checked {@link #ready}): every {@link QuernBlock#TURNS} turns
	 * with grain in grind one piece, returning what it gives; otherwise nothing.
	 */
	public ItemStack turn(long now) {
		ItemStack out = advance(now);
		changed();
		return out;
	}

	/** Native stroke without notification; the companion publishes it after its transaction commits. */
	ItemStack advance(long now) {
		stroke(now);
		ItemStack out = ItemStack.EMPTY;
		if (!grain.isEmpty() && ++turns >= QuernBlock.TURNS) {
			out = QuernBlock.ground(grain);
			grain.shrink(1);
			if (grain.isEmpty()) {
				grain = ItemStack.EMPTY;
			}
			turns = 0;
		}
		return out;
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		companion.drop();
		super.preRemoveSideEffects(pos, state);
		if (level != null && !level.isClientSide() && !grain.isEmpty()) {
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), grain);
		}
		grain = ItemStack.EMPTY;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		grain = input.read("grain", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		if (grain.getCount() > QuernBlock.CAPACITY) {
			grain.setCount(QuernBlock.CAPACITY);
		}
		turns = Math.clamp(input.getIntOr("turns", 0), 0, QuernBlock.TURNS - 1);
		companion.load(input);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!grain.isEmpty()) {
			output.store("grain", ItemStack.CODEC, grain);
		}
		output.putInt("turns", turns);
		companion.save(output);
	}
	@Override public void setRemoved() { companion.job.removed(); super.setRemoved(); }
}
