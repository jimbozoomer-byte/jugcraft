package io.github.jimbozoomer.jugcraft.world;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * The Pixel Hollows Map the Retro Trader sells: an unmarked map that, used, looks for the nearest Pixel Hollows
 * from where the player stands (server-side, bounded: {@link PixelHollowsMaps}) and becomes an explorer map marked
 * with it. With no cave in reach it says so and stays as it is, so it is never turned into a blank or wrong map.
 * A cooldown stops repeated searches.
 */
public class PixelHollowsMapItem extends Item {
	public static final int COOLDOWN_TICKS = 100;

	public PixelHollowsMapItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level instanceof ServerLevel server) {
			player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
			Optional<BlockPos> target = PixelHollowsMaps.find(server, player.blockPosition());
			if (target.isEmpty()) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.pixel_hollows_map.none",
						String.format("%,d", PixelHollowsMaps.RADIUS)));
				return InteractionResult.SUCCESS;
			}
			ItemStack map = PixelHollowsMaps.map(server, target.get());
			stack.consume(1, player);
			if (stack.isEmpty()) {
				player.setItemInHand(hand, map);
			} else if (!player.getInventory().add(map)) {
				Block.popResource(level, player.blockPosition(), map);
			}
			player.sendOverlayMessage(Component.translatable("message.jugcraft.pixel_hollows_map.found", target.get().getY()));
		}
		return InteractionResult.SUCCESS;
	}
}
