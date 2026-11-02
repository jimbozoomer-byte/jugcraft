package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Bat Wreath (Front-Yard Haunt Scenes, part 3; tools/front_yard.py BAT_WREATH): black twigs wound into a ring 75 cm
 * across, three black paper bats flying off it and a purple satin bow at its foot. It hangs on the side of a block or on
 * either face of a door, as the Autumn and Holly Wreaths do, and is dressed for good: it takes no mums. Its model is
 * tools/front_yard_data.py bat_wreath().
 */
public final class BatWreathBlock extends AutumnWreathBlock {
	public BatWreathBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean takesMums() {
		return false;
	}

	/** Its bats fly a little out past the twigs. */
	@Override
	public double[] doorBox() {
		return new double[] {7.5, 0.5, 14.75};
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		return InteractionResult.PASS;
	}
}
