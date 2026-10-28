package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * What a Caramel Apple Stand holds (on its first block; {@link CaramelAppleStandBlock}): servings of caramel in its pot,
 * sticks in its jar, and the caramel apples on its tray, each with the game time it was dipped, so it has set
 * {@link CaramelAppleStandBlock#COOL_TICKS} later. Nothing ticks: whether an apple has set is worked out when it is
 * taken. Saved with the world; each change shows the stand's new stock on its blocks; breaking it gives everything back.
 */
public class CaramelAppleStandBlockEntity extends BlockEntity {
	private int caramel;
	private int sticks;
	/** The game time each tray spot's apple was dipped, or -1 for an empty spot. */
	private final long[] dipped = new long[CaramelAppleStandBlock.RACK];

	public CaramelAppleStandBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.CARAMEL_APPLE_STAND_ENTITY, pos, state);
		Arrays.fill(dipped, -1L);
	}

	public int caramel() {
		return caramel;
	}

	public int sticks() {
		return sticks;
	}

	/** How many caramel apples stand on the tray. */
	public int apples() {
		int count = 0;
		for (long time : dipped) {
			count += time >= 0 ? 1 : 0;
		}
		return count;
	}

	/** Adds a serving of caramel, if the pot has room. */
	public boolean addCaramel() {
		if (caramel >= CaramelAppleStandBlock.CARAMEL) {
			return false;
		}
		caramel++;
		changed();
		return true;
	}

	/** Adds a stick to the jar, if it has room. */
	public boolean addStick() {
		if (sticks >= CaramelAppleStandBlock.STICKS) {
			return false;
		}
		sticks++;
		changed();
		return true;
	}

	/** Dips an apple at {@code now}: spends a serving and a stick and stands it on the first free spot. */
	public boolean dip(long now) {
		for (int spot = 0; spot < dipped.length; spot++) {
			if (dipped[spot] < 0 && caramel > 0 && sticks > 0) {
				caramel--;
				sticks--;
				dipped[spot] = now;
				changed();
				return true;
			}
		}
		return false;
	}

	/** Whether an apple on the tray has set by {@code now}. */
	public boolean anySet(long now) {
		for (long time : dipped) {
			if (time >= 0 && now - time >= CaramelAppleStandBlock.COOL_TICKS) {
				return true;
			}
		}
		return false;
	}

	/** Takes the last set apple off the tray (empty if none has set). */
	public ItemStack takeSet(long now) {
		for (int spot = dipped.length - 1; spot >= 0; spot--) {
			if (dipped[spot] >= 0 && now - dipped[spot] >= CaramelAppleStandBlock.COOL_TICKS) {
				dipped[spot] = -1L;
				pack();
				changed();
				return new ItemStack(JugcraftAgriculture.item("caramel_apple"));
			}
		}
		return ItemStack.EMPTY;
	}

	/** Moves the apples left on the tray to its first spots, in the order they were dipped. */
	private void pack() {
		long[] kept = Arrays.stream(dipped).filter(time -> time >= 0).toArray();
		Arrays.fill(dipped, -1L);
		System.arraycopy(kept, 0, dipped, 0, kept.length);
	}

	private void changed() {
		setChanged();
		if (level != null && !level.isClientSide() && getBlockState().getBlock() instanceof CaramelAppleStandBlock stand) {
			stand.show(level, worldPosition, getBlockState(), caramel, sticks, apples());
		}
	}

	/** Breaking the stand gives back its caramel, sticks and caramel apples. */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level != null && !level.isClientSide()) {
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(JugcraftAgriculture.item("caramel"), caramel));
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.STICK, sticks));
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(JugcraftAgriculture.item("caramel_apple"), apples()));
		}
		caramel = 0;
		sticks = 0;
		Arrays.fill(dipped, -1L);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		caramel = Math.clamp(input.getIntOr("caramel", 0), 0, CaramelAppleStandBlock.CARAMEL);
		sticks = Math.clamp(input.getIntOr("sticks", 0), 0, CaramelAppleStandBlock.STICKS);
		for (int spot = 0; spot < dipped.length; spot++) {
			dipped[spot] = input.getLongOr("dipped_" + spot, -1L);
		}
		pack();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("caramel", caramel);
		output.putInt("sticks", sticks);
		for (int spot = 0; spot < dipped.length; spot++) {
			output.putLong("dipped_" + spot, dipped[spot]);
		}
	}
}
