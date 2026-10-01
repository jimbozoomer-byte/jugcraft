package io.github.jimbozoomer.jugcraft.world;

import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/**
 * The Retro Trader's Pixel Hollows map. Server-only and bounded: the search samples the biome noise (no chunks are
 * loaded or generated) in square rings of columns {@link #STEP} blocks apart, out to {@link #RINGS} rings, at
 * the heights in {@link #HEIGHTS}, and stops at the first ring that holds the biome. It runs once, when the
 * trade is created; the finished map is part of the saved offer, so it is never searched again.
 */
public final class PixelHollowsMaps {
	/** Keep in sync with MAP_SEARCH in tools/pixel_hollows.py. */
	public static final int STEP = 64;
	public static final int RINGS = 40;
	public static final int[] HEIGHTS = {-16, 0, -32, 16, -48};

	private PixelHollowsMaps() {
	}

	/** The nearest Pixel Hollows found within the search bounds of this level, if the biome can generate here at all. */
	public static Optional<BlockPos> find(ServerLevel level, BlockPos origin) {
		if (!JugcraftConfig.isFeatureEnabled(PixelHollows.FEATURE)) {
			return Optional.empty();
		}
		BiomeSource source = level.getChunkSource().getGenerator().getBiomeSource();
		if (source.possibleBiomes().stream().noneMatch(biome -> biome.is(PixelHollows.BIOME))) {
			return Optional.empty();
		}
		return find(source, level.getChunkSource().randomState().sampler(), origin);
	}

	/** The search itself, on any biome source (the tests run it on fresh Overworld seeds). */
	public static Optional<BlockPos> find(BiomeSource source, Climate.Sampler sampler, BlockPos origin) {
		for (int ring = 0; ring <= RINGS; ring++) {
			BlockPos best = null;
			for (int i = -ring; i <= ring; i++) {
				best = closer(origin, best, column(source, sampler, origin, i, -ring));
				if (ring > 0) {
					best = closer(origin, best, column(source, sampler, origin, i, ring));
				}
				if (i > -ring && i < ring) {
					best = closer(origin, best, column(source, sampler, origin, -ring, i));
					best = closer(origin, best, column(source, sampler, origin, ring, i));
				}
			}
			if (best != null) {
				return Optional.of(best);
			}
		}
		return Optional.empty();
	}

	private static BlockPos column(BiomeSource source, Climate.Sampler sampler, BlockPos origin, int dx, int dz) {
		int x = origin.getX() + dx * STEP;
		int z = origin.getZ() + dz * STEP;
		for (int y : HEIGHTS) {
			if (source.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z), sampler)
					.is(PixelHollows.BIOME)) {
				return new BlockPos(x, y, z);
			}
		}
		return null;
	}

	private static BlockPos closer(BlockPos origin, BlockPos best, BlockPos candidate) {
		if (candidate == null) {
			return best;
		}
		if (best == null || horizontal(origin, candidate) < horizontal(origin, best)) {
			return candidate;
		}
		return best;
	}

	private static long horizontal(BlockPos a, BlockPos b) {
		long dx = a.getX() - b.getX();
		long dz = a.getZ() - b.getZ();
		return dx * dx + dz * dz;
	}

	/** An explorer map to the nearest Pixel Hollows, named with its depth; empty when none is in reach. */
	public static Optional<ItemStack> create(ServerLevel level, BlockPos origin) {
		return find(level, origin).map(target -> map(level, target));
	}

	/** A filled explorer map centred on the target, marked with the Pixel Hollows marker and named with its depth. */
	public static ItemStack map(ServerLevel level, BlockPos target) {
		ItemStack map = MapItem.create(level, target.getX(), target.getZ(), (byte) 2, true, true);
		MapItem.renderBiomePreviewMap(level, map);
		MapItemSavedData.addTargetDecoration(map, target, "+", RetroTrader.MAP_MARKER);
		map.set(DataComponents.ITEM_NAME, Component.translatable("filled_map.jugcraft.pixel_hollows", target.getY()));
		return map;
	}

	/** Shown in a trade that can never be bought: no Pixel Hollows was in reach when the trade was made. */
	public static ItemStack unavailable() {
		ItemStack stack = new ItemStack(Items.MAP);
		stack.set(DataComponents.ITEM_NAME, Component.translatable("item.jugcraft.pixel_hollows_map"));
		stack.set(DataComponents.LORE, new ItemLore(List.of(
				Component.translatable("tooltip.jugcraft.pixel_hollows_map.none").withStyle(ChatFormatting.GRAY))));
		return stack;
	}
}
