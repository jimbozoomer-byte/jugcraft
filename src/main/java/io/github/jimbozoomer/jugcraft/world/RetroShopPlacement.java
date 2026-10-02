package io.github.jimbozoomer.jugcraft.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;

/**
 * One Retro Game Shop in every new village. A village is built by the game's jigsaw placer, which fills each house
 * slot from a shuffled list of the houses pool. While a village has no shop yet, the shop goes to the front of that
 * list, so the first house slot it fits becomes the shop; once it is placed it leaves the list, so there is never a
 * second. A village only lacks a shop if none of its house slots has room for it.
 *
 * <p>State is per thread: the placer builds a whole village on one thread, starting with a new placer
 * ({@code mixin/JigsawPlacerMixin} calls {@link #beginStructure}) and creating a piece for everything it places
 * ({@code mixin/PoolElementStructurePieceMixin} calls {@link #placed}). Outside a village, or with the
 * {@code retro_trader} switch off, nothing changes.
 */
public final class RetroShopPlacement {
	/** For the structure being built on this thread: whether its shop is placed. Null outside jigsaw placement. */
	private static final ThreadLocal<boolean[]> SHOP_PLACED = new ThreadLocal<>();

	private RetroShopPlacement() {
	}

	/** A jigsaw structure (a village, or anything else) starts being built on this thread. */
	public static void beginStructure() {
		SHOP_PLACED.set(new boolean[1]);
	}

	/** The placer created a piece for this element. */
	public static void placed(StructurePoolElement element) {
		boolean[] state = SHOP_PLACED.get();
		if (state != null && element != null && element == RetroTrader.shop()) {
			state[0] = true;
		}
	}

	/**
	 * The candidates for one jigsaw slot, reordered: the shop first while the structure has none, and not at all once
	 * it has one. Returns null (keep the game's list) when the list does not hold the shop or no structure is being built.
	 */
	public static List<StructurePoolElement> reorder(List<StructurePoolElement> shuffled) {
		StructurePoolElement shop = RetroTrader.shop();
		boolean[] state = SHOP_PLACED.get();
		if (shop == null || state == null || shuffled.stream().noneMatch(element -> element == shop)) {
			return null;
		}
		List<StructurePoolElement> out = new ArrayList<>(shuffled.size());
		if (!state[0]) {
			out.add(shop);
		}
		for (StructurePoolElement element : shuffled) {
			if (element != shop) {
				out.add(element);
			}
		}
		return out;
	}
}
