package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * The Carving Knife: used on the side of a pumpkin, it opens the carving screen for that side. The face
 * is checked and carved on the server ({@link PumpkinCarvings}); each finished carving costs one durability.
 */
public class CarvingKnifeItem extends Item {
	public CarvingKnifeItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Direction side = context.getClickedFace();
		if (side.getAxis() == Direction.Axis.Y || !PumpkinCarvings.isCarvable(context.getLevel().getBlockState(context.getClickedPos()))) {
			return InteractionResult.PASS;
		}
		if (context.getPlayer() instanceof ServerPlayer player) {
			if (!player.mayUseItemAt(context.getClickedPos(), side, context.getItemInHand())) {
				return InteractionResult.FAIL;
			}
			PumpkinCarvings.open(player, context.getClickedPos(), side);
		}
		return InteractionResult.SUCCESS;
	}
}
