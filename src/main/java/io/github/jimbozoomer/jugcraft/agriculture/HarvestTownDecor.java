package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.world.level.block.state.BlockState;

/**
 * The Thanksgiving harvest town details' numbers (tools/thanksgiving_town.py; docs/features/thanksgiving-town-details.md),
 * registered by JugcraftAgriculture.registerThanksgivingTown(): the Copper Chafing Dish's light while its burner is lit.
 * Everything else is the existing decoration blocks' ({@link FacingDecorationBlock}, {@link CandleCenterpieceBlock}) or
 * this set's {@link HarvestTownArchBlock} and {@link HarvestTownHangingBlock}.
 */
public final class HarvestTownDecor {
	/** The Copper Chafing Dish's light while its spirit burner is lit (tools/thanksgiving_town.py PROPS). */
	public static final int CHAFING_DISH_LIGHT = 7;

	private HarvestTownDecor() {
	}

	/** The light the Copper Chafing Dish gives: {@value #CHAFING_DISH_LIGHT} while its burner is lit. */
	public static int chafingDishLight(BlockState state) {
		return state.getValue(CandleCenterpieceBlock.LIT) ? CHAFING_DISH_LIGHT : 0;
	}
}
