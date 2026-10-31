package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Whether a Butter Churn holds a bucket of milk and how many strokes it has had; and, from {@link HandWorkedBlockEntity},
 * where its dasher is. The server alone changes them.
 */
public class ButterChurnBlockEntity extends HandWorkedBlockEntity {
	private boolean milk;
	private int strokes;
	public final HandFarmCompanion companion = new HandFarmCompanion(this);
	void restoreInput(boolean filled, int progress) { milk = filled; strokes = progress; }

	public ButterChurnBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.BUTTER_CHURN_ENTITY, pos, state);
	}

	@Override
	protected int strokeTicks() {
		return ButterChurnBlock.STROKE_TICKS;
	}

	public boolean milk() {
		return milk;
	}

	public int strokes() {
		return strokes;
	}

	/** Pours in a bucket of milk, if it holds none; whether it went in. */
	public boolean fill() {
		if (milk) {
			return false;
		}
		milk = true;
		strokes = 0;
		changed();
		return true;
	}

	/**
	 * One stroke of the dasher at {@code now} (the caller has checked {@link #ready}): the {@link ButterChurnBlock#STROKES}th
	 * with milk in churns it into butter, returning it; otherwise nothing.
	 */
	public ItemStack plunge(long now) {
		ItemStack out = advance(now);
		changed();
		return out;
	}

	/** Native stroke without notification; the companion publishes it after its transaction commits. */
	ItemStack advance(long now) {
		stroke(now);
		ItemStack out = ItemStack.EMPTY;
		if (milk && ++strokes >= ButterChurnBlock.STROKES) {
			out = new ItemStack(JugcraftAgriculture.item("butter"), ButterChurnBlock.BUTTER);
			milk = false;
			strokes = 0;
		}
		return out;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		milk = input.getBooleanOr("milk", false);
		strokes = milk ? Math.clamp(input.getIntOr("strokes", 0), 0, ButterChurnBlock.STROKES - 1) : 0;
		companion.load(input);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putBoolean("milk", milk);
		output.putInt("strokes", strokes);
		companion.save(output);
	}
	@Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { companion.drop(); super.preRemoveSideEffects(pos, state); }
	@Override public void setRemoved() { companion.job.removed(); super.setRemoved(); }
}
