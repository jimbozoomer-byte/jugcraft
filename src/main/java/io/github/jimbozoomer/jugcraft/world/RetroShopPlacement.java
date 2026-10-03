package io.github.jimbozoomer.jugcraft.world;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.SequencedPriorityIterator;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

/**
 * One Retro Game Shop in every new village. A village is built by the game's jigsaw placer, which fills each house
 * slot from a shuffled list of the houses pool. While a village has no shop yet, the shop goes to the front of that
 * list, so the first house slot it fits becomes the shop; once it is placed it leaves the list, so there is never a
 * second.
 *
 * <p>Village houses are built inside their street's own plot (the game's "expansion hack" for villages), and many
 * plots are too small for the 9 by 8 shop: in a desert village only 12 of the 38 house slots can hold it, and none of
 * them is at a meeting point. So now and then a village is laid out with no slot that has room. When the placer runs
 * out of pieces with the shop offered but not placed, the village is laid out again from its start piece
 * ({@link #relayout}), up to {@link #LAYOUTS} layouts in all. The new layout draws on the same seeded random source, so
 * a world seed still makes the same village.
 *
 * <p>State is per thread: the placer builds a whole village on one thread, starting with a new placer
 * ({@code mixin/JigsawPlacerMixin} calls {@link #beginStructure}), creating a piece for everything it places
 * ({@code mixin/PoolElementStructurePieceMixin} calls {@link #placed}) and asking whether pieces are left to grow from
 * ({@code mixin/JigsawPlacementMixin} calls {@link #relayout} when none are). Outside a village, or with the
 * {@code retro_trader} switch off, nothing changes.
 */
public final class RetroShopPlacement {
	/** How many layouts a village may try for its shop; the last one stays, with or without a shop. */
	public static final int LAYOUTS = 8;

	/** For the structure being built on this thread. Null outside jigsaw placement. */
	private static final ThreadLocal<State> STATE = new ThreadLocal<>();
	/** New layouts made since the game started (for tests). */
	private static final AtomicInteger RELAYOUTS = new AtomicInteger();
	/** For tests: how many first layouts of each structure started on this thread are built without the shop. */
	private static final ThreadLocal<Integer> WITHHELD = ThreadLocal.withInitial(() -> 0);
	private static volatile @Nullable Constructor<?> pieceState;
	private static volatile boolean pieceStateMissing;

	private RetroShopPlacement() {
	}

	private static final class State {
		/** The shop is placed. */
		boolean placed;
		/** A houses pool holding the shop was asked for candidates (never in zombie villages or other structures). */
		boolean offered;
		/** Layouts tried so far, this one included. */
		int layouts = 1;
		/** Layouts built without the shop (tests only; see {@link #withholdShopForTests}). */
		int withheld;
	}

	/** A jigsaw structure (a village, or anything else) starts being built on this thread. */
	public static void beginStructure() {
		State state = new State();
		state.withheld = WITHHELD.get();
		STATE.set(state);
	}

	/**
	 * For tests: structures started on this thread build their first {@code layouts} layouts without the shop, so a
	 * village has to be laid out again to get one. 0 (the default) turns this off.
	 */
	public static void withholdShopForTests(int layouts) {
		WITHHELD.set(layouts);
	}

	/** The placer created a piece for this element. */
	public static void placed(StructurePoolElement element) {
		State state = STATE.get();
		if (state != null && element != null && element == RetroTrader.shop()) {
			state.placed = true;
		}
	}

	/**
	 * The candidates for one jigsaw slot, reordered: the shop first while the structure has none, and not at all once
	 * it has one. Returns null (keep the game's list) when the list does not hold the shop or no structure is being built.
	 */
	public static List<StructurePoolElement> reorder(List<StructurePoolElement> shuffled) {
		StructurePoolElement shop = RetroTrader.shop();
		State state = STATE.get();
		if (shop == null || state == null || shuffled.stream().noneMatch(element -> element == shop)) {
			return null;
		}
		state.offered = true;
		List<StructurePoolElement> out = new ArrayList<>(shuffled.size());
		if (!state.placed && state.layouts > state.withheld) {
			out.add(shop);
		}
		for (StructurePoolElement element : shuffled) {
			if (element != shop) {
				out.add(element);
			}
		}
		return out;
	}

	/**
	 * The placer has no pieces left to grow from. If this structure was offered the shop but has none, and has layouts
	 * left, start it again: every piece but the start piece is dropped, the start piece forgets its junctions, and it
	 * goes back in the placer's queue with the structure's original free space, as the placer first grew it. Returns
	 * whether the placer has work again.
	 */
	@SuppressWarnings("unchecked")
	public static boolean relayout(SequencedPriorityIterator<?> placing, PoolElementStructurePiece start,
			List<PoolElementStructurePiece> pieces, VoxelShape free) {
		State state = STATE.get();
		if (state == null || !state.offered || state.placed || state.layouts >= LAYOUTS || pieces.isEmpty() || pieces.get(0) != start) {
			return false;
		}
		Object again = pieceState(start, free);
		if (again == null) {
			return false;
		}
		state.layouts++;
		RELAYOUTS.incrementAndGet();
		pieces.removeIf(piece -> piece != start);
		start.getJunctions().clear();
		((SequencedPriorityIterator<Object>) placing).add(again, 0);
		return true;
	}

	/** New village layouts made for a shop since the game started. */
	public static int relayouts() {
		return RELAYOUTS.get();
	}

	/** Whether this game version lets a village be laid out again (the placer's queue entry can be made). */
	public static boolean canRelayout() {
		return constructor() != null;
	}

	/**
	 * The placer's queue entry for a piece at depth 0 with this free space: the game's private record
	 * {@code JigsawPlacement.PieceState(piece, free, depth)}. Null if it cannot be made; then villages keep their
	 * first layout, as before.
	 */
	private static @Nullable Object pieceState(PoolElementStructurePiece start, VoxelShape free) {
		Constructor<?> constructor = constructor();
		if (constructor == null) {
			return null;
		}
		Class<?>[] parameters = constructor.getParameterTypes();
		Object[] arguments = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			arguments[i] = parameters[i] == PoolElementStructurePiece.class ? start
					: parameters[i] == MutableObject.class ? new MutableObject<>(free) : (Object) 0;
		}
		try {
			return constructor.newInstance(arguments);
		} catch (ReflectiveOperationException | RuntimeException exception) {
			missing(exception);
			return null;
		}
	}

	/** The record's constructor (piece, free space, depth, in whatever order it takes them), found once. */
	private static @Nullable Constructor<?> constructor() {
		Constructor<?> constructor = pieceState;
		if (constructor != null || pieceStateMissing) {
			return constructor;
		}
		try {
			Class<?> type = Class.forName("net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$PieceState");
			for (Constructor<?> candidate : type.getDeclaredConstructors()) {
				List<Class<?>> parameters = List.of(candidate.getParameterTypes());
				if (parameters.size() == 3 && parameters.contains(PoolElementStructurePiece.class)
						&& parameters.contains(MutableObject.class) && parameters.contains(int.class)) {
					candidate.setAccessible(true);
					pieceState = candidate;
					return candidate;
				}
			}
			throw new NoSuchMethodException("JigsawPlacement$PieceState(PoolElementStructurePiece, MutableObject, int)");
		} catch (ReflectiveOperationException | RuntimeException exception) {
			missing(exception);
			return null;
		}
	}

	private static void missing(Exception exception) {
		pieceStateMissing = true;
		Jugcraft.LOGGER.warn("Retro Game Shop: cannot lay a village out again ({}); villages keep their first layout", exception.toString());
	}
}
