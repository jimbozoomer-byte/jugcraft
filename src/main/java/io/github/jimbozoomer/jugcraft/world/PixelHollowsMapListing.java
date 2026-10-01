package io.github.jimbozoomer.jugcraft.world;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * The Retro Trader's novice map trade: 12 emeralds and a compass for a map to the nearest Pixel Hollows. The search
 * runs once, here, when the trader first gets the trade. With no cave in reach the trade still appears, but with
 * no uses at all, so it can never sell a blank or wrong map, even after restocking; its tooltip says why.
 */
public final class PixelHollowsMapListing implements VillagerTrades.ItemListing {
	@Override
	public MerchantOffer getOffer(ServerLevel level, Entity trader, RandomSource random) {
		Optional<ItemStack> map = PixelHollowsMaps.create(level, trader.blockPosition());
		return new MerchantOffer(new ItemCost(Items.EMERALD, RetroTrader.MAP_EMERALDS), Optional.of(new ItemCost(Items.COMPASS, 1)),
				map.orElseGet(PixelHollowsMaps::unavailable), map.isPresent() ? 1 : 0, 5, 0.2F);
	}
}
