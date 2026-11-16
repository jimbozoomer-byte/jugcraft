package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort (GC-05's rule, lifted from VerdanceCarrying in GC-06b so every garden service shares it): one
 * think's questions to GardenPacts.target for a garden service, each place asked once, at most {@code most} places a think.
 * A service that remembers the places refused for themselves (Bathin's) passes what its memory skips and how it adds to
 * it: a skipped place is answered NOT_HERE at no question, and a place refused for itself is added. Server thread only;
 * never kept past its think, and nothing in it is saved.
 */
final class GardenAsking {
	/** What a place's question came to this think. */
	enum Asked {
		/** The holder may change it now, for a beneficiary. */
		MAY,
		/** Not this place (not a beneficiary's now, not the holder's to change by hand, outside the area): skipped. */
		NOT_HERE,
		/** The pact's own reason (suspended, held, its quota spent, its holder not near): the think stops with it. */
		PACT,
		/** No question left this think. */
		SPENT
	}

	private final ServerLevel level;
	private final BoundWill record;
	private final long time;
	private final long now;
	private final int most;
	private final Predicate<BlockPos> skipped;
	private final Consumer<BlockPos> skip;
	private final Map<BlockPos, Asked> asked = new HashMap<>();
	private Status stop = Status.IDLE;

	/** One think's questions for a service that remembers no refused place. */
	GardenAsking(ServerLevel level, BoundWill record, long time, long now, int most) {
		this(level, record, time, now, most, pos -> false, pos -> {
		});
	}

	/** One think's questions; {@code skipped} answers whether its memory skips a place, {@code skip} adds one refused for itself. */
	GardenAsking(ServerLevel level, BoundWill record, long time, long now, int most, Predicate<BlockPos> skipped, Consumer<BlockPos> skip) {
		this.level = level;
		this.record = record;
		this.time = time;
		this.now = now;
		this.most = most;
		this.skipped = skipped;
		this.skip = skip;
	}

	/** The first place's answer unless it is MAY, else the second's. */
	Asked both(BlockPos first, @Nullable UUID firstKeeper, BlockPos second, @Nullable UUID secondKeeper) {
		Asked answer = ask(first, firstKeeper);
		return answer == Asked.MAY ? ask(second, secondKeeper) : answer;
	}

	/** {@code pos}'s answer this think: asked of GardenPacts.target once, unless its service's memory skips it or no question is left. */
	Asked ask(BlockPos pos, @Nullable UUID keeper) {
		Asked known = asked.get(pos);
		if (known != null) {
			return known;
		}
		if (skipped.test(pos)) {
			return Asked.NOT_HERE;
		}
		if (asked.size() >= most) {
			return Asked.SPENT;
		}
		Status refused = GardenPacts.target(level, record, pos, keeper, time, now);
		Asked answer;
		if (refused == null) {
			answer = Asked.MAY;
		} else if (refused == Status.IDLE || refused == Status.BLOCKED_BY_ACCESS || refused == Status.OUTSIDE_AGREEMENT) {
			// This place's own answer: its service's memory skips it until its index is read again (the unit review, as Buer's
			// beds), so refused places never crowd the rest out of a think's questions, and each protection a refusal asks is
			// asked once a read.
			answer = Asked.NOT_HERE;
			skip.accept(pos);
		} else {
			stop = refused;
			answer = Asked.PACT;
		}
		asked.put(pos.immutable(), answer);
		return answer;
	}

	/** The pact's reason that stopped the think. */
	Status stop() {
		return stop;
	}
}
