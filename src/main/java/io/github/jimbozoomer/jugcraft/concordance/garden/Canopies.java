package io.github.jimbozoomer.jugcraft.concordance.garden;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-09 (GC-S21, GC-P124; docs/features/amdusias-pact.md): the canopies standing in each level
 * now, in memory only. A canopy is one holder's: a pact service (Amdusias's canopy shelter, spirits/CanopyShelter) covers
 * the beds it may shelter from one perch, at most {@value #MAX_BEDS}, lowering the stir their crops read by at most
 * {@value #MAX_DAMPING}, and refreshes it every think while its pact serves; it lapses {@value #LAPSE_TICKS} ticks after
 * its last refresh, so it never outlasts its pact's serving by more than that (suspended, held or ended, the holder away,
 * the tree felled, the perch broken, the presence unloaded, the server stopped). A holder keeps one canopy: while one
 * stands, another perch's is refused until it lapses or is lifted. A bed reads the largest damping of the live canopies
 * covering it, never their sum (VerdantBedBlockEntity.cachedHabitat, Habitat.sheltered); a plant that wants the stir
 * (Niche.wantsStir) only from a canopy whose holder answered "shelter all". Nothing is saved or sent: a restart forgets
 * every canopy, and each returns at its presence's next think. Server thread only; each call reads the canopies kept in one
 * level (one a holder) or covering one chunk.
 */
public final class Canopies {
	/** The most a canopy lowers the stir a crop reads (GC-P122). Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int MAX_DAMPING = 2;
	/** The most beds one canopy covers at once (GC-P126: its quota is a cap). Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int MAX_BEDS = 16;
	/** How long a canopy stands after its last refresh (ticks): two of its presence's thinks. Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int LAPSE_TICKS = 40;
	/** Each level's canopies (weakly, as Garden's sampling budgets: a level let go takes its own with it). */
	private static final Map<ServerLevel, Canopies> LEVELS = new WeakHashMap<>();

	/** One holder's canopy: its perch, its damping, whether its holder answered "shelter all", the beds it covers, its last refresh. */
	public record Canopy(UUID holder, BlockPos perch, int damping, boolean all, Set<BlockPos> beds, long refreshedAt) {
		public Canopy {
			beds = Set.copyOf(beds);
		}

		/** Whether it stands at game time {@code now}: refreshed less than LAPSE_TICKS before (a clock run back: lapsed). */
		public boolean live(long now) {
			return now >= refreshedAt && now - refreshedAt < LAPSE_TICKS;
		}
	}

	private final Map<UUID, Canopy> byHolder = new HashMap<>();
	/** The holders whose canopy covers a bed in a chunk, by ChunkPos.pack. */
	private final Map<Long, Set<UUID>> byChunk = new HashMap<>();
	private long prunedAt = Long.MIN_VALUE;

	private Canopies() {
	}

	/**
	 * {@code holder}'s canopy at {@code perch} covers {@code beds} (the first MAX_BEDS of them in their order: the nearest, as
	 * its service lists them) with {@code damping} (clamped to 0..MAX_DAMPING) from game time {@code now}, replacing the one
	 * it kept; no beds: lifted. Refused (false, nothing changed) while the holder's canopy stands at another perch.
	 */
	public static boolean cover(ServerLevel level, UUID holder, BlockPos perch, int damping, boolean all, Collection<BlockPos> beds, long now) {
		Canopies canopies = LEVELS.computeIfAbsent(level, ignored -> new Canopies());
		canopies.prune(now);
		Canopy old = canopies.byHolder.get(holder);
		if (old != null && old.live(now) && !old.perch().equals(perch)) {
			return false;
		}
		if (old != null) {
			canopies.remove(old);
		}
		if (beds.isEmpty()) {
			return true;
		}
		Set<BlockPos> kept = new LinkedHashSet<>();
		for (BlockPos bed : beds) {
			if (kept.size() == MAX_BEDS) {
				break;
			}
			kept.add(bed.immutable());
		}
		Canopy canopy = new Canopy(holder, perch.immutable(), Math.clamp(damping, 0, MAX_DAMPING), all, kept, now);
		canopies.byHolder.put(holder, canopy);
		for (BlockPos bed : canopy.beds()) {
			canopies.byChunk.computeIfAbsent(ChunkPos.pack(bed), ignored -> new HashSet<>()).add(holder);
		}
		return true;
	}

	/** Lifts {@code holder}'s canopy if it stands at {@code perch} (another perch's is not this caller's to lift). */
	public static void lift(ServerLevel level, UUID holder, BlockPos perch) {
		Canopies canopies = LEVELS.get(level);
		Canopy old = canopies == null ? null : canopies.byHolder.get(holder);
		if (old != null && old.perch().equals(perch)) {
			canopies.remove(old);
		}
	}

	/** Whether any canopy is kept in {@code level} (standing, or lapsed and not yet pruned): a bed asks nothing more while none is. */
	public static boolean any(ServerLevel level) {
		Canopies canopies = LEVELS.get(level);
		return canopies != null && !canopies.byHolder.isEmpty();
	}

	/**
	 * The damping a plant on the bed at {@code bed} reads at game time {@code now}: the largest of the live canopies covering
	 * that bed that shelter such a plant ({@code wantsStir}: only one whose holder answered "shelter all"), never their sum;
	 * 0 for none.
	 */
	public static int damping(ServerLevel level, BlockPos bed, boolean wantsStir, long now) {
		Canopies canopies = LEVELS.get(level);
		if (canopies == null || canopies.byHolder.isEmpty()) {
			return 0;
		}
		canopies.prune(now);
		Set<UUID> here = canopies.byChunk.get(ChunkPos.pack(bed));
		int most = 0;
		if (here != null) {
			for (UUID holder : here) {
				Canopy canopy = canopies.byHolder.get(holder);
				if (canopy != null && canopy.live(now) && (canopy.all() || !wantsStir) && canopy.beds().contains(bed)) {
					most = Math.max(most, canopy.damping());
				}
			}
		}
		return most;
	}

	/** {@code holder}'s canopy standing in {@code level} at game time {@code now}, or null (his kept page, Jade, tests). */
	public static @Nullable Canopy of(ServerLevel level, UUID holder, long now) {
		Canopies canopies = LEVELS.get(level);
		Canopy canopy = canopies == null ? null : canopies.byHolder.get(holder);
		return canopy != null && canopy.live(now) ? canopy : null;
	}

	/** Drops every lapsed canopy, at most once a game tick (one a holder: bounded by the holders sheltering here). */
	private void prune(long now) {
		if (now == prunedAt || byHolder.isEmpty()) {
			return;
		}
		prunedAt = now;
		List<Canopy> lapsed = new ArrayList<>();
		for (Canopy canopy : byHolder.values()) {
			if (!canopy.live(now)) {
				lapsed.add(canopy);
			}
		}
		for (Canopy canopy : lapsed) {
			remove(canopy);
		}
	}

	private void remove(Canopy canopy) {
		byHolder.remove(canopy.holder(), canopy);
		for (BlockPos bed : canopy.beds()) {
			long chunk = ChunkPos.pack(bed);
			Set<UUID> holders = byChunk.get(chunk);
			if (holders != null) {
				holders.remove(canopy.holder());
				if (holders.isEmpty()) {
					byChunk.remove(chunk);
				}
			}
		}
	}
}
