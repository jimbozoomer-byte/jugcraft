package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The candy corn in a Candy Corn Dispenser's globe ({@link CandyCornDispenserBlock}): how many pieces, up to
 * {@value CandyCornDispenserBlock#CAPACITY}, saved with the world and dropped if it is broken. Its block's {@code fill}
 * shows how full it is; the count itself is the server's alone. It also {@link #mark}s when its crank was last set
 * turning, on every side, for the renderer; that is not saved.
 */
public class CandyCornDispenserBlockEntity extends DecorationBlockEntity {
	private int count;

	public CandyCornDispenserBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.CANDY_CORN_DISPENSER_ENTITY, pos, state);
	}

	/** How many pieces it holds. */
	public int count() {
		return count;
	}

	/** Pours in as much of {@code candy} as fits; returns how many went in ({@code candy} is that many fewer). */
	public int pour(ItemStack candy) {
		int in = Math.min(candy.getCount(), CandyCornDispenserBlock.CAPACITY - count);
		if (in <= 0) {
			return 0;
		}
		candy.shrink(in);
		count += in;
		setChanged();
		return in;
	}

	/** One piece comes out, if it holds any; returns whether one did. */
	public boolean take() {
		if (count <= 0) {
			return false;
		}
		count--;
		setChanged();
		return true;
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (level != null && count > 0) {
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(JugcraftAgriculture.item("candy_corn"), count));
			count = 0;
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		count = Math.clamp(input.getIntOr("count", 0), 0, CandyCornDispenserBlock.CAPACITY);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("count", count);
	}
}
