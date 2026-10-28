package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.GleanerBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantHeartBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenAsking.Asked;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-05 (Dossier 2 items 2, 3 and 5; docs/features/bathin-pact.md): Verdance carrying, Bathin's
 * service. Each think while his pact serves (PactSpiritEntity.think: a presence verified at its perch; an offering due
 * included, GC-P27) and while the day's quota of moves lasts, he makes at most one move, and a move is one server step:
 * the Heart's Verdance falls and the target's rises in the same call, so nothing is ever on the way and he holds none (S1).
 * <ul>
 * <li>Sources: the awake Verdant Hearts of his beneficiaries in his area (GardenReach.devices: loaded chunks only, at most
 * 16) holding a whole move ({@value #MOVE_MAX}), or holding any Verdance and making no more (their status not "working"),
 * the fullest first (GC-S25: a Heart still beating gives a whole move or nothing, not a beat's 1 or 2 each think; one that
 * has stopped is still emptied into a Gleaner with room (a pylon takes whole batches of 3, so 1 or 2 may stay, as before)).
 * A Heart that already pours into a Ley Pylon beside it (its own road) is his only once the holder has answered "take
 * from it" ({@link #CLAIM}) at the perch (PactAccords.answer); unanswered, or answered "leave it" ({@link #YIELD}, the
 * safe course), he leaves it alone and, once a presence, turns to it with his inspect gesture to raise the matter.</li>
 * <li>The lossless road first: a beneficiary Gleaner beyond its own reach of that Heart (where it would draw by itself),
 * with room for a whole move, the emptiest first: up to {@value #MOVE_MAX} through the Gleaner's own draw from that Heart
 * (GleanerBlockEntity.receiveFrom), both ownerships checked with the holder as the one acting.</li>
 * <li>Else a Ley Pylon not beside that Heart, a beneficiary's or no one's, with room for a batch: at most
 * {@value #MAX_BATCHES} batches through the Heart's own pour (VerdantHeartBlockEntity.pourInto: the data conversion
 * jugcraft:verdance_to_ley, 3 Verdance for 2 Ley Charge, its owner rule unchanged).</li>
 * </ul>
 * Both places of a move are asked at the change (GardenPacts.target: serves there, a beneficiary's, the holder near, the
 * holder's own hands), at most {@value #MAX_ASKED} places a think; a place refused for itself is not asked again until his
 * index is next read, so refused places never crowd out the rest. A move is shown only by Bathin's shimmer at its two
 * ends (BathinEntity.shimmer). No new conversion; no item, nutrient or practice moves; the move is counted on the record.
 * What he remembers ({@link Memory}: his index, the Hearts with their own road, the places skipped, why he idled, his
 * last move) is the presence's alone and never saved.
 */
public final class VerdanceCarrying {
	/** Its work id (worker/PactParser.WORK; tools/concordance_pacts.py WORK). */
	public static final String WORK = "verdance_carrying";
	/** His refusal's reason: no Heart of the inviter's, or nowhere of theirs to carry to (tools/concordance_pacts.py SERVICE_WORDS answers). */
	public static final String NOTHING_TO_CARRY = "nothing_to_carry";
	/** His matter (GC-S11; the accord machinery's first user): a Heart that already pours into a pylon beside it. */
	public static final String TWO_ROADS = "two_roads";
	/** Leave it: the safe course, also what an unanswered or unknown accord means. Saved as the perch's accord; never renamed. */
	public static final String YIELD = "yield";
	/** Take from it. Saved as the perch's accord; never renamed. */
	public static final String CLAIM = "claim";
	/** His answers, in the order the perch offers them (append only). Keep equal to tools/concordance_pacts.py SERVICE_WORDS accords. */
	public static final List<String> ACCORDS = List.of(YIELD, CLAIM);
	/**
	 * The most Verdance one move sets down in a Gleaner: its own draw (GleanerBlockEntity.DRAW); and what a Heart still beating
	 * must hold before he takes from it (GC-S25). Keep equal to tools/concordance_garden_pacts.py CONSTANTS.
	 */
	public static final int MOVE_MAX = 8;
	/** The most batches one move pours into a pylon (3 Verdance for 2 Ley Charge each). Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int MAX_BATCHES = 3;
	/** The most places one think asks GardenPacts.target about. Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int MAX_ASKED = 8;
	/** His words: message.jugcraft.concordance.pact.verdance_carrying.* (tools/concordance_pacts.py SERVICE_WORDS). */
	private static final String KEY = "message.jugcraft.concordance.pact.verdance_carrying.";
	/** Verdance carrying as the pact service of its work (Workers.register). */
	public static final PactService SERVICE = new Service();

	private VerdanceCarrying() {
	}

	/** One move he made: from the Heart at {@code from} to the Gleaner or pylon at {@code to}, the Verdance it took, the Ley Charge it made (pylons), when. */
	public record Move(BlockPos from, BlockPos to, boolean pylon, long verdance, long ley, long at) {
	}

	/** Why his last think moved nothing, for Jade. */
	public enum Idle {
		/** No Heart of his beneficiaries' holds Verdance he may take (a Heart still beating holds less than a whole move: GC-S25). */
		EMPTY,
		/** Verdance waits, but no Gleaner or pylon he may fill from it (or none found before the think's questions ran out). */
		NOWHERE
	}

	/**
	 * Bathin's one step for {@code record}'s pact at world time {@code time} and game time {@code now} ({@code holder}: its
	 * perch's). WORKING when he moved Verdance; OUTSIDE_AGREEMENT outside his hours; FINISHED with the day's quota spent; what
	 * GardenPacts.target answers for the whole pact (suspended, held, the quota spent, the holder not near); else IDLE (nothing
	 * to carry, nowhere to set it, his matter raised, or no question left this think). Bounded: one index read, at most
	 * 16 x 16 Heart-Gleaner and 16 x 16 Heart-pylon pairs looked at, at most MAX_ASKED places asked, at most one move.
	 */
	static Status carry(ServerLevel level, BathinEntity bathin, BoundWill record, AgreementHolder holder, long time, long now) {
		Memory memory = bathin.serviceMemory(Memory.class, Memory::new);
		GardenReach reach = memory.reach(record);
		List<VerdantHeartBlockEntity> hearts = reach.hearts(level, now);
		memory.notice(level, record, hearts);
		String answer = answer(level, bathin);
		boolean claim = CLAIM.equals(answer);
		BoundWill.Service service = record.pact().service();
		if (!Pacts.inHours(service, time)) {
			return Status.OUTSIDE_AGREEMENT;
		}
		if (Pacts.done(record.pact(), now) >= service.quota()) {
			return Status.FINISHED;
		}
		BlockPos matter = memory.road();
		if (answer == null && matter != null && memory.raise()) {
			// Raised once a presence: he turns to the Heart that already has a road and weighs it; his words wait on the kept
			// page, in Jade and on the roster (the accord machinery). Nothing moves this think.
			bathin.attendWork(matter, BathinEntity.Gesture.INSPECT.ticks, now);
			bathin.gesture(BathinEntity.Gesture.INSPECT);
			return Status.IDLE;
		}
		List<VerdantHeartBlockEntity> sources = new ArrayList<>();
		for (VerdantHeartBlockEntity heart : hearts) {
			// GC-S25 (decided by the session; the unit review's finding 4): a Heart beating on its bed makes 1 or 2 Verdance a
			// beat, so while it beats he takes only a whole move from it, and his day's moves are not spent a beat at a time;
			// once it makes no more (full, starved, stalled, no bed), whatever it holds is his to carry.
			if (heart.verdance() > 0 && (heart.verdance() >= MOVE_MAX || !"working".equals(heart.status()))
					&& (claim || !memory.roaded(heart.getBlockPos()))) {
				sources.add(heart);
			}
		}
		if (sources.isEmpty()) {
			memory.idle(Idle.EMPTY);
			return Status.IDLE;
		}
		// The fullest first; List.sort is stable, so the index's order (nearest the perch) holds among equals.
		sources.sort(Comparator.comparingLong(VerdantHeartBlockEntity::verdance).reversed());
		List<GleanerBlockEntity> gleaners = new ArrayList<>();
		for (GleanerBlockEntity gleaner : reach.gleaners(level, now)) {
			if (GleanerBlockEntity.CAPACITY - gleaner.verdance() >= MOVE_MAX) {
				gleaners.add(gleaner);
			}
		}
		// The emptiest first (where Verdance is wanted most), the nearest the perch among equals.
		gleaners.sort(Comparator.comparingLong(GleanerBlockEntity::verdance));
		GardenAsking asking = new GardenAsking(level, record, time, now, MAX_ASKED, memory::skipped, memory::skip);
		for (VerdantHeartBlockEntity heart : sources) {
			for (GleanerBlockEntity gleaner : gleaners) {
				if (gleaner.reaches(heart.getBlockPos())) {
					continue; // it draws on that Heart by itself
				}
				Asked asked = asking.both(heart.getBlockPos(), heart.keeper(), gleaner.getBlockPos(), gleaner.keeper());
				if (asked == Asked.PACT) {
					return asking.stop();
				}
				if (asked == Asked.SPENT) {
					memory.idle(Idle.NOWHERE);
					return Status.IDLE;
				}
				if (asked == Asked.MAY) {
					long moved = gleaner.receiveFrom(heart, record.holder(), MOVE_MAX, level);
					if (moved > 0) {
						return carried(level, bathin, holder, memory, new Move(heart.getBlockPos(), gleaner.getBlockPos(), false, moved, 0L, now), now);
					}
				}
			}
		}
		List<LeyPylonBlockEntity> pylons = reach.pylons(level, now);
		for (VerdantHeartBlockEntity heart : sources) {
			for (LeyPylonBlockEntity pylon : pylons) {
				if (heart.beside(pylon.getBlockPos()) || !heart.mayPourInto(pylon)) {
					continue; // the Heart's own road, another keeper's, too little Verdance, or no room for a batch
				}
				UUID owner = pylon.owner();
				Asked asked = asking.both(heart.getBlockPos(), heart.keeper(), pylon.getBlockPos(), owner == null ? record.holder() : owner);
				if (asked == Asked.PACT) {
					return asking.stop();
				}
				if (asked == Asked.SPENT) {
					memory.idle(Idle.NOWHERE);
					return Status.IDLE;
				}
				if (asked == Asked.MAY) {
					long before = heart.verdance();
					long made = heart.pourInto(pylon, level, MAX_BATCHES);
					if (made > 0) {
						return carried(level, bathin, holder, memory, new Move(heart.getBlockPos(), pylon.getBlockPos(), true, before - heart.verdance(), made, now), now);
					}
				}
			}
		}
		memory.idle(Idle.NOWHERE);
		return Status.IDLE;
	}

	/** A move made: remembered, counted on the record, faced and gestured, and shown at both ends on this tick (Dossier 2 detail b). */
	private static Status carried(ServerLevel level, BathinEntity bathin, AgreementHolder holder, Memory memory, Move move, long now) {
		memory.moved(move);
		holder.taskDone(level, now);
		bathin.attendWork(move.to(), BathinEntity.Gesture.WORK.ticks, now);
		bathin.gesture(BathinEntity.Gesture.WORK);
		bathin.shimmer(level, move.from(), move.to());
		return Status.WORKING;
	}

	/**
	 * The holder's answer to his matter as {@code spirit}'s own perch keeps it (PactAccords.answer: one of his ACCORDS, its
	 * pact counting there); null while unanswered, unknown, or not readable (his perch unloaded or not a perch).
	 */
	static @Nullable String answer(ServerLevel level, PactSpiritEntity<?> spirit) {
		BlockPos at = spirit.perch();
		return at != null && level.isLoaded(at) && level.getBlockEntity(at) instanceof PactPerchBlockEntity perch ? PactAccords.answer(level, perch) : null;
	}

	/**
	 * The rows of his kept page for {@code record} at game time {@code now}: today's moves; his last move (both ends and the
	 * amounts); and, answered "leave it", how many Hearts he leaves. Reads only.
	 */
	public static List<Component> rows(ServerLevel level, BoundWill record, long now) {
		BoundWill.Service service = record.pact().service();
		List<Component> rows = new ArrayList<>();
		rows.add(Component.translatable(KEY + "row.service", Pacts.done(record.pact(), now), service.quota()));
		BathinEntity bathin = GardenPacts.presence(level, record, BathinEntity.class);
		if (bathin == null) {
			return rows;
		}
		Memory memory = bathin.serviceMemory(Memory.class, Memory::new);
		Move last = memory.last();
		if (last != null && last.pylon()) {
			rows.add(Component.translatable(KEY + "row.poured", last.verdance(), last.from().toShortString(), last.to().toShortString(), last.ley()));
		} else if (last != null) {
			rows.add(Component.translatable(KEY + "row.moved", last.verdance(), last.from().toShortString(), last.to().toShortString()));
		}
		if (YIELD.equals(answer(level, bathin)) && !memory.roads().isEmpty()) {
			rows.add(Component.translatable(KEY + "row.leaving", memory.roads().size()));
		}
		return rows;
	}

	/**
	 * What one Bathin presence remembers between its thinks (PactSpiritEntity.serviceMemory: never saved, lost with the
	 * entity): his device index, the indexed Hearts that have their own road, the places refused for themselves since the
	 * index was last read, whether he has raised his matter, why he last idled, and his last move. It never holds Verdance:
	 * a move is over when its call returns. Server thread only.
	 */
	public static final class Memory {
		/** The most places skipped: every Heart, Gleaner and pylon an index holds. */
		private static final int MAX_SKIPPED = 3 * GardenReach.MAX_DEVICES;
		private @Nullable GardenReach reach;
		private List<BlockPos> roads = List.of();
		private final HashSet<BlockPos> skipped = new HashSet<>();
		private int skippedAt = -1;
		private boolean raised;
		private @Nullable Idle idle;
		private @Nullable Move last;

		public Memory() {
		}

		/** {@code record}'s device index, made again (forgetting the places skipped) when its perch or radius changed (GardenReach.covers). */
		GardenReach reach(BoundWill record) {
			if (reach == null || !reach.covers(record)) {
				reach = GardenReach.devices(record);
				skipped.clear();
				skippedAt = -1;
			}
			return reach;
		}

		/**
		 * Forgets his device index, so his next think reads his area again (GardenReach.forget): an operator's or a test's,
		 * after devices were placed within the index's REFRESH_TICKS. The places skipped are forgotten at that read; nothing
		 * else he remembers changes.
		 */
		public void forget() {
			if (reach != null) {
				reach.forget();
			}
		}

		/**
		 * The indexed Hearts whose keeper {@code record}'s pact benefits now (asked again each think: the index keeps its
		 * keepers only from its last refresh) that pour into a pylon beside them now, nearest the perch first (at most 16, six
		 * reads each); why he idled is forgotten. When the read that gave {@code hearts} refreshed the index, the places
		 * skipped since the last are forgotten, so a claim lifted or a keeper who joined the party is asked again.
		 */
		void notice(ServerLevel level, BoundWill record, List<VerdantHeartBlockEntity> hearts) {
			if (reach != null && reach.refreshes() != skippedAt) {
				skippedAt = reach.refreshes();
				skipped.clear();
			}
			List<BlockPos> found = new ArrayList<>();
			for (VerdantHeartBlockEntity heart : hearts) {
				if (GardenPacts.benefits(record, heart.keeper()) && heart.feedsAPylonBeside(level)) {
					found.add(heart.getBlockPos());
				}
			}
			roads = List.copyOf(found);
			idle = null;
		}

		boolean roaded(BlockPos heart) {
			return roads.contains(heart);
		}

		/** GardenPacts.target refused the place at {@code pos} for itself: not asked again until the index is next read. */
		void skip(BlockPos pos) {
			if (skipped.size() < MAX_SKIPPED) {
				skipped.add(pos.immutable());
			}
		}

		/** Whether the place at {@code pos} was refused for itself since the index was last read (tests and carry). */
		public boolean skipped(BlockPos pos) {
			return skipped.contains(pos);
		}

		/** The Hearts with their own road at his last think, nearest the perch first. */
		public List<BlockPos> roads() {
			return roads;
		}

		/** The nearest of them, his matter's Heart; null when none. */
		public @Nullable BlockPos road() {
			return roads.isEmpty() ? null : roads.getFirst();
		}

		/** Whether he raises his matter now: once a presence. */
		boolean raise() {
			if (raised) {
				return false;
			}
			raised = true;
			return true;
		}

		void idle(Idle why) {
			idle = why;
		}

		/** Why his last think moved nothing (null: it moved, it was stopped, or it raised his matter). */
		public @Nullable Idle idle() {
			return idle;
		}

		void moved(Move move) {
			last = move;
		}

		/** His last move since he came (what moved, from where, to where, when), or null. */
		public @Nullable Move last() {
			return last;
		}
	}

	/** Verdance carrying behind PactService (GC-01): no on-request action; his rows, refusal, obligation, matter and Jade note. */
	private static final class Service implements PactService {
		@Override
		public String work() {
			return WORK;
		}

		@Override
		public boolean holderOnly() {
			return false;
		}

		/**
		 * Dossier 2 item 2 (CT GC-05's missing): he comes only where an awake Verdant Heart the inviter keeps, and an awake
		 * Gleaner the inviter keeps or a Ley Pylon the inviter or no one owns, lie within his radius of the perch (a cube, his
		 * area). Whether the Heart holds Verdance or the Gleaner lies beyond its reach is his work's question (an IDLE note),
		 * not his invitation's. Read from the loaded chunks' block entities (GardenReach: at most 9 chunks), loading none.
		 */
		@Override
		public List<Missing> missing(ServerLevel level, ServerPlayer inviter, PactPerchBlockEntity perch, PactDefinition definition) {
			UUID own = inviter.getUUID();
			GardenReach reach = new GardenReach(perch.getBlockPos(), definition.radius(), bed -> false, own::equals);
			long now = level.getGameTime();
			boolean heart = !reach.hearts(level, now).isEmpty();
			boolean somewhere = !reach.gleaners(level, now).isEmpty() || !reach.pylons(level, now).isEmpty();
			return heart && somewhere ? List.of()
					: List.of(new Missing(NOTHING_TO_CARRY, Component.translatable(KEY + "missing", definition.radius(), definition.radius())));
		}

		@Override
		public List<PactStatePayload.Choice> choices(ServerLevel level, ServerPlayer player, BoundWill record, long now) {
			return List.of();
		}

		@Override
		public PactMenu.@Nullable Answered perform(ServerLevel level, ServerPlayer player, AgreementHolder holder, String choice, long time, long now) {
			return null;
		}

		@Override
		public List<Component> serviceRows(ServerLevel level, ServerPlayer player, BoundWill record, long now) {
			return rows(level, record, now);
		}

		@Override
		public List<Component> obligationRows(ServerLevel level, ServerPlayer player, BoundWill record, long now) {
			return PactObligations.rows(player, record, PactObligations.Act.DEVICE);
		}

		@Override
		public Component suspendedRow(Component name) {
			return Component.translatable("message.jugcraft.concordance.pact.row.state.suspended.serves", name);
		}

		@Override
		public Status work(ServerLevel level, PactSpiritEntity<?> spirit, BoundWill record, AgreementHolder holder, long time, long now) {
			return spirit instanceof BathinEntity bathin ? carry(level, bathin, record, holder, time, now) : Status.IDLE;
		}

		@Override
		public PactObligations.@Nullable Act obligation() {
			return PactObligations.Act.DEVICE;
		}

		@Override
		public List<String> accords() {
			return ACCORDS;
		}

		/**
		 * His matter (two roads from one Heart) while an indexed Heart of his has its own road, answered or not, so the answer
		 * can be changed at the perch; null without a presence of his here (a copied perch's view has none), or with no such
		 * Heart now. The Heart is read again (loaded, awake, its keeper still a beneficiary, its road still there). Reads only.
		 */
		@Override
		public PactService.@Nullable Complication complication(ServerLevel level, BoundWill record, PactPerchBlockEntity perch, long now) {
			if (!(perch.presence(level) instanceof BathinEntity bathin) || bathin.owner() == null) {
				return null;
			}
			BlockPos road = bathin.serviceMemory(Memory.class, Memory::new).road();
			if (road == null || !level.isLoaded(road) || !(level.getBlockEntity(road) instanceof VerdantHeartBlockEntity heart)
					|| heart.isRemoved() || !heart.awake() || !GardenPacts.benefits(record, heart.keeper()) || !heart.feedsAPylonBeside(level)) {
				return null;
			}
			return new PactService.Complication(TWO_ROADS, Component.translatable(KEY + "matter.two_roads", road.toShortString()), road, ACCORDS);
		}

		/** Jade's note: Hearts with their own road waiting for the answer (how many); else why he idled; none while his pact is held. Reads only; names no player. */
		@Override
		public PactService.@Nullable Note note(ServerLevel level, PactSpiritEntity<?> spirit, BoundWill record, long time, long now) {
			if (!(spirit instanceof BathinEntity bathin) || !Pacts.serving(record)) {
				return null;
			}
			Memory memory = bathin.serviceMemory(Memory.class, Memory::new);
			if (answer(level, bathin) == null && !memory.roads().isEmpty()) {
				return new PactService.Note("two_roads", memory.roads().size());
			}
			if (memory.idle() == Idle.EMPTY) {
				return new PactService.Note("empty", 0);
			}
			return memory.idle() == Idle.NOWHERE ? new PactService.Note("nowhere", 0) : null;
		}
	}
}
