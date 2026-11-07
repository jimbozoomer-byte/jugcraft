package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.garden.OrganismCropBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenAsking.Asked;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-08 (Dossier 5 items 2, 3 and 5; docs/features/bifrons-pact.md): root-running, Bifrons's
 * service. By night (his hours), while his pact serves and its day's quota lasts, he runs one root at a time from a full
 * Verdant Bed to a hungry one and loses half along it: {@value #TAKE} nutrients leave the source and {@value #GIVE}
 * reaches the destination in one server step, so nothing is ever on the way and he holds none (S1). He measures first,
 * a beat apart:
 * <ol>
 * <li>a think chooses the pair and measures the source: he attends it, his inspect gesture plays and the shared
 * preparation sign rises there (BifronsEntity.mark); nothing moves;</li>
 * <li>a later think, at least MEASURE_TICKS and at most MEASURE_LIMIT ticks after, reads and asks both beds again, marks
 * the destination, checks the destination has room for what reaches it, and only then runs the root: the source's exact
 * take, then the destination's give, which never makes.</li>
 * </ol>
 * The destination is the poorest awake bed of his beneficiaries with room (Garden.poorest over his bed index); the source
 * the fullest holding at least SOURCE_MIN and GAP more than it, fallow (no organism crop and no Verdant Heart on it)
 * unless the holder has answered "any full bed" at the perch, and never the bed under a Heart. When no fallow bed can
 * give (none rich enough, or every one refused him) but a planted one of a beneficiary's could, the planted ones are his
 * reserves: his matter (PactAccords), raised once a presence with his inspect gesture toward the first, once he may change
 * it. Every bed is asked at the change (GardenAsking: GardenPacts.target, serves there, a beneficiary's, the holder near,
 * the holder's own hands), at most MAX_ASKED a think, a bed refused for itself only once each time the index is read. He
 * feeds a bed by no other rule, eases no strain, moves no item and makes no nutrient: the economy's root_running, two for
 * one. What he remembers ({@link Memory}: his bed index, the beds refused for themselves, a measure under way, his last
 * run, his reserves) is the presence's alone, never saved.
 */
public final class RootRunning {
	/** Its work id (worker/PactParser.WORK; tools/concordance_pacts.py WORK). */
	public static final String WORK = "root_running";
	/** His refusal's reason: fewer than two awake beds of the inviter's in his area (tools/concordance_pacts.py SERVICE_WORDS answers). */
	public static final String NO_TWO_BEDS = "no_two_beds";
	/** His matter (GC-S11): his only full beds are growing. */
	public static final String RESERVES = "reserves";
	/** Fallow beds only: the safe course, also what an unanswered or unknown accord means. Saved as the perch's accord; never renamed. */
	public static final String FALLOW = "fallow";
	/** Any full bed, never the one under a Verdant Heart. Saved as the perch's accord; never renamed. */
	public static final String FULL = "full";
	/** His answers, in the order the perch offers them (append only). Keep equal to tools/concordance_pacts.py SERVICE_WORDS accords. */
	public static final List<String> ACCORDS = List.of(FALLOW, FULL);
	/** What a run takes from the source (exactly). Keep equal to tools/concordance_economy.py and concordance_garden_pacts.py CONSTANTS. */
	public static final int TAKE = 2;
	/** What a run gives the destination (the rest is lost along the root). Keep equal to the same two tables. */
	public static final int GIVE = 1;
	/** The least a source holds. Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int SOURCE_MIN = 24;
	/** How much richer than the destination a source is, at least. Keep equal to CONSTANTS. */
	public static final int GAP = 3;
	/** How many awake beds of the inviter's he needs in his area before he comes. Keep equal to CONSTANTS. */
	public static final int BEDS = 2;
	/** The most beds one think asks GardenPacts.target about. Keep equal to CONSTANTS. */
	public static final int MAX_ASKED = 8;
	/** His second beat comes no sooner than this after his first (ticks: one think). Keep equal to CONSTANTS. */
	public static final int MEASURE_TICKS = 20;
	/** A measure older than this (ticks) is stale: dropped at no cost and made afresh. Keep equal to CONSTANTS. */
	public static final int MEASURE_LIMIT = 60;
	/** The most reserves he remembers (his matter names the first; Jade counts them). Keep equal to CONSTANTS. */
	public static final int MAX_RESERVES = 16;
	/** His words: message.jugcraft.concordance.pact.root_running.* (tools/concordance_pacts.py SERVICE_WORDS). */
	private static final String KEY = "message.jugcraft.concordance.pact.root_running.";
	/** Root-running as the pact service of its work (Workers.register). */
	public static final PactService SERVICE = new Service();

	private RootRunning() {
	}

	/** A measure under way: the source and destination he chose, and the game time of his first beat. */
	public record Measure(BlockPos from, BlockPos to, long at) {
	}

	/** One run he made: from the bed at {@code from} to the bed at {@code to}, what left one and reached the other, when. */
	public record Run(BlockPos from, BlockPos to, int taken, int given, long at) {
		/** What was lost along the root. */
		public int lost() {
			return taken - given;
		}
	}

	/** Why his last think ran and measured nothing, for Jade. */
	public enum Idle {
		/** No bed of his beneficiaries' with room that he may give to. */
		NO_HUNGRY,
		/** A hungry bed, but no bed he may draw from holds SOURCE_MIN and GAP more than it. */
		NO_FULL
	}

	/**
	 * Bifrons's one step for {@code record}'s pact at world time {@code time} and game time {@code now} ({@code holder}: its
	 * perch's). WORKING when he measured or ran (or is between his two beats); OUTSIDE_AGREEMENT outside his hours;
	 * FINISHED with the day's quota spent; what GardenPacts.target answers for the whole pact (suspended, held, the quota
	 * spent, the holder not near); else IDLE (no hungry bed, no full one, his matter raised, a measure abandoned, or no
	 * question left this think). Bounded: one index read (at most 64 beds, refreshed at most every 200 ticks), at most
	 * MAX_ASKED beds asked (a bed refused for itself only once each time the index is read), at most two block reads above
	 * each indexed bed (fallow, then under a Heart), at most one run and two signs.
	 */
	static Status run(ServerLevel level, BifronsEntity bifrons, BoundWill record, AgreementHolder holder, long time, long now) {
		Memory memory = bifrons.serviceMemory(Memory.class, Memory::new);
		memory.idle(null);
		BoundWill.Service service = record.pact().service();
		if (!Pacts.inHours(service, time)) {
			memory.drop();
			return Status.OUTSIDE_AGREEMENT;
		}
		if (Pacts.done(record.pact(), now) >= service.quota()) {
			memory.drop();
			return Status.FINISHED;
		}
		List<VerdantBedBlockEntity> beds = memory.beds(level, record, now);
		String kept = answer(level, bifrons);
		boolean full = FULL.equals(kept);
		GardenAsking asking = new GardenAsking(level, record, time, now, MAX_ASKED, memory::skipped, memory::skip);
		Measure measure = memory.measure();
		if (measure != null && now >= measure.at() && now - measure.at() <= MEASURE_LIMIT) {
			// Between his beats he is still measuring; a beat later he marks the destination and runs the root.
			return now - measure.at() < MEASURE_TICKS ? Status.WORKING : ran(level, bifrons, holder, memory, beds, measure, full, asking, now);
		}
		memory.drop();
		List<VerdantBedBlockEntity> wanting = new ArrayList<>(beds);
		VerdantBedBlockEntity to = null;
		while (to == null) {
			VerdantBedBlockEntity poorest = Garden.poorest(wanting);
			if (poorest == null) {
				memory.reserves(List.of());
				memory.idle(Idle.NO_HUNGRY);
				return Status.IDLE;
			}
			Asked asked = asking.ask(poorest.getBlockPos(), poorest.keeper());
			if (asked == Asked.PACT) {
				return asking.stop();
			}
			if (asked == Asked.SPENT) {
				return Status.IDLE;
			}
			if (asked == Asked.MAY) {
				to = poorest;
			} else {
				wanting.remove(poorest);
			}
		}
		List<VerdantBedBlockEntity> fallow = new ArrayList<>();
		List<VerdantBedBlockEntity> planted = new ArrayList<>();
		for (VerdantBedBlockEntity bed : beds) {
			// A beneficiary's now (the index keeps its keepers only from its last refresh; GC-P106), rich enough.
			if (bed == to || !GardenPacts.benefits(record, bed.keeper()) || !gives(bed, to)) {
				continue;
			}
			if (fallow(level, bed)) {
				fallow.add(bed);
			} else if (!underAHeart(level, bed)) {
				planted.add(bed);
			}
		}
		// His reserves: the planted beds that could give while no fallow bed can (his matter), nearest the perch first.
		Status matter = reserves(bifrons, memory, asking, kept, fallow.isEmpty() ? planted : List.of(), now);
		if (matter != null) {
			return matter;
		}
		List<VerdantBedBlockEntity> sources = new ArrayList<>(fallow);
		if (full) {
			sources.addAll(planted);
		}
		// The fullest first; List.sort is stable, so the index's order (nearest the perch) holds among equals.
		sources.sort(Comparator.comparingInt(VerdantBedBlockEntity::nutrients).reversed());
		for (VerdantBedBlockEntity from : sources) {
			Asked asked = asking.ask(from.getBlockPos(), from.keeper());
			if (asked == Asked.PACT) {
				return asking.stop();
			}
			if (asked == Asked.SPENT) {
				return Status.IDLE;
			}
			if (asked == Asked.MAY) {
				// His first beat: he measures the source; the destination's mark and the run wait for his next think.
				memory.begin(new Measure(from.getBlockPos(), to.getBlockPos(), now));
				bifrons.attendWork(from.getBlockPos(), BifronsEntity.Gesture.INSPECT.ticks, now);
				bifrons.gesture(BifronsEntity.Gesture.INSPECT);
				bifrons.mark(level, from.getBlockPos(), true, now);
				return Status.WORKING;
			}
		}
		if (!full && !fallow.isEmpty() && !planted.isEmpty()) {
			// Every fallow bed that could give was refused for itself (no question left returned above): none can give after all,
			// so the planted ones are his reserves, and his matter stands (Dossier 5 item 5; GC-P106).
			matter = reserves(bifrons, memory, asking, kept, planted, now);
			if (matter != null) {
				return matter;
			}
		}
		memory.idle(Idle.NO_FULL);
		return Status.IDLE;
	}

	/**
	 * His reserves among {@code planted} (the beds that could give while no fallow bed can), nearest the perch first, kept in
	 * his memory for his page, Jade and the perch. Unanswered ({@code kept} null), his matter names only a reserve he may
	 * change now: one he may not (a claim the holder may not build in) is no reserve of his, and none is named past the
	 * think's questions. Raised once a presence, and only for a bed he may change: he turns to the first reserve and weighs
	 * it; his words wait on the kept page, in Jade and on the roster (the accord machinery), and nothing runs this think.
	 * Returns how the think ends (the pact's own reason, or IDLE when raised), or null when it goes on.
	 */
	private static @Nullable Status reserves(BifronsEntity bifrons, Memory memory, GardenAsking asking, @Nullable String kept,
			List<VerdantBedBlockEntity> planted, long now) {
		List<VerdantBedBlockEntity> reserves = new ArrayList<>(planted);
		Asked first = Asked.NOT_HERE;
		while (kept == null && !reserves.isEmpty()) {
			first = asking.ask(reserves.getFirst().getBlockPos(), reserves.getFirst().keeper());
			if (first == Asked.PACT) {
				return asking.stop();
			}
			if (first != Asked.NOT_HERE) {
				break;
			}
			reserves.removeFirst();
		}
		memory.reserves(first == Asked.SPENT ? List.of() : reserves);
		BlockPos reserve = memory.reserve();
		if (kept == null && reserve != null && first == Asked.MAY && memory.raise()) {
			bifrons.attendWork(reserve, BifronsEntity.Gesture.INSPECT.ticks, now);
			bifrons.gesture(BifronsEntity.Gesture.INSPECT);
			return Status.IDLE;
		}
		return null;
	}

	/**
	 * His second beat for {@code measure}: both beds read again (still indexed and awake, the source still his to draw from,
	 * SOURCE_MIN and GAP above the destination) and asked again; the destination marked; then, only if it has room for what
	 * reaches it (defensive: the gap already leaves room), the source's exact take and the destination's give, in this one
	 * step. Anything changed since his first beat: nothing is taken, and his next think measures afresh.
	 */
	private static Status ran(ServerLevel level, BifronsEntity bifrons, AgreementHolder holder, Memory memory, List<VerdantBedBlockEntity> beds,
			Measure measure, boolean full, GardenAsking asking, long now) {
		memory.drop();
		VerdantBedBlockEntity source = indexed(beds, measure.from());
		VerdantBedBlockEntity destination = indexed(beds, measure.to());
		if (source == null || destination == null || !gives(source, destination)
				|| !(fallow(level, source) || full && !underAHeart(level, source))) {
			return Status.IDLE;
		}
		Asked asked = asking.both(source.getBlockPos(), source.keeper(), destination.getBlockPos(), destination.keeper());
		if (asked == Asked.PACT) {
			return asking.stop();
		}
		if (asked != Asked.MAY) {
			return Status.IDLE;
		}
		bifrons.mark(level, destination.getBlockPos(), false, now);
		// Room first: what reaches the destination must fit there, or nothing leaves the source.
		if (VerdantBedBlockEntity.CAPACITY - destination.nutrients() < GIVE || !source.take(TAKE)) {
			return Status.IDLE;
		}
		int given = destination.give(GIVE);
		memory.ran(new Run(source.getBlockPos(), destination.getBlockPos(), TAKE, given, now));
		holder.taskDone(level, now);
		bifrons.attendWork(destination.getBlockPos(), BifronsEntity.Gesture.WORK.ticks, now);
		bifrons.gesture(BifronsEntity.Gesture.WORK);
		return Status.WORKING;
	}

	/** Whether {@code source} may give to {@code destination} now: at least SOURCE_MIN, GAP richer, and room there for GIVE. */
	static boolean gives(VerdantBedBlockEntity source, VerdantBedBlockEntity destination) {
		return source.nutrients() >= SOURCE_MIN && source.nutrients() - destination.nutrients() >= GAP
				&& VerdantBedBlockEntity.CAPACITY - destination.nutrients() >= GIVE;
	}

	/** Whether nothing grows on {@code bed} and no Heart sits on it (the garden's own planted test, VerdantBedBlock). Reads one block. */
	public static boolean fallow(ServerLevel level, VerdantBedBlockEntity bed) {
		BlockState above = level.getBlockState(bed.getBlockPos().above());
		return !(above.getBlock() instanceof OrganismCropBlock) && !above.is(Garden.VERDANT_HEART);
	}

	/** Whether a Verdant Heart sits on {@code bed} (its beat feeds on it): never a source, whatever the answer. Reads one block. */
	public static boolean underAHeart(ServerLevel level, VerdantBedBlockEntity bed) {
		return level.getBlockState(bed.getBlockPos().above()).is(Garden.VERDANT_HEART);
	}

	/** The bed at {@code pos} among {@code beds} (his index's, read this think), or null. */
	private static @Nullable VerdantBedBlockEntity indexed(List<VerdantBedBlockEntity> beds, BlockPos pos) {
		for (VerdantBedBlockEntity bed : beds) {
			if (bed.getBlockPos().equals(pos)) {
				return bed;
			}
		}
		return null;
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
	 * The rows of his kept page for {@code record} at world time {@code time} and game time {@code now}: today's runs; by
	 * day, that he rests until dusk; his last run (both beds, what was taken, given and lost); and, answered "fallow beds
	 * only", how many planted beds he keeps as reserves. Reads only; names no player.
	 */
	public static List<Component> rows(ServerLevel level, BoundWill record, long time, long now) {
		BoundWill.Service service = record.pact().service();
		List<Component> rows = new ArrayList<>();
		rows.add(Component.translatable(KEY + "row.service", Pacts.done(record.pact(), now), service.quota()));
		if (!Pacts.inHours(service, time)) {
			rows.add(Component.translatable(KEY + "row.asleep", Workers.clock(service.from()), Workers.clock(service.to())));
		}
		BifronsEntity bifrons = GardenPacts.presence(level, record, BifronsEntity.class);
		if (bifrons == null) {
			return rows;
		}
		Memory memory = bifrons.serviceMemory(Memory.class, Memory::new);
		Run last = memory.last();
		if (last != null) {
			rows.add(Component.translatable(KEY + "row.last", last.taken(), last.from().toShortString(), last.given(),
					last.to().toShortString(), last.lost()));
		}
		if (FALLOW.equals(answer(level, bifrons)) && !memory.reserves().isEmpty()) {
			rows.add(Component.translatable(KEY + "row.reserved", memory.reserves().size()));
		}
		return rows;
	}

	/**
	 * What one Bifrons presence remembers between its thinks (PactSpiritEntity.serviceMemory: never saved, lost with the
	 * entity): his bed index; the beds refused for themselves since the index was last read, at most GardenReach.MAX_BEDS,
	 * forgotten at its next read (as Buer's); a measure under way, his last run, his reserves, whether he has raised his
	 * matter and why he last idled. It never holds nutrients: a run is over when its call returns. Server thread only.
	 */
	public static final class Memory {
		private @Nullable GardenReach reach;
		private final HashSet<BlockPos> skipped = new HashSet<>();
		private int skippedAt = -1;
		private @Nullable Measure measure;
		private @Nullable Run last;
		private List<BlockPos> reserves = List.of();
		private boolean raised;
		private @Nullable Idle idle;

		public Memory() {
		}

		/** {@code record}'s bed index, made again (forgetting the beds skipped) when its perch or radius changed (GardenReach.covers). */
		GardenReach reach(BoundWill record) {
			if (reach == null || !reach.covers(record)) {
				reach = GardenReach.of(record);
				skipped.clear();
				skippedAt = -1;
			}
			return reach;
		}

		/**
		 * {@code record}'s indexed beds at game time {@code now} (GardenReach.beds). When that read refreshed the index, the
		 * beds skipped since the last are forgotten, so a claim lifted or a keeper who joined the party is asked again.
		 */
		List<VerdantBedBlockEntity> beds(ServerLevel level, BoundWill record, long now) {
			GardenReach index = reach(record);
			List<VerdantBedBlockEntity> beds = index.beds(level, now);
			if (index.refreshes() != skippedAt) {
				skippedAt = index.refreshes();
				skipped.clear();
			}
			return beds;
		}

		/** GardenPacts.target refused the bed at {@code pos} for itself: not asked again until the index is next read. */
		void skip(BlockPos pos) {
			if (skipped.size() < GardenReach.MAX_BEDS) {
				skipped.add(pos.immutable());
			}
		}

		/** Whether the bed at {@code pos} was refused for itself since the index was last read (tests and run). */
		public boolean skipped(BlockPos pos) {
			return skipped.contains(pos);
		}

		/**
		 * Forgets his bed index, so his next think reads his area again (GardenReach.forget): an operator's or a test's,
		 * after beds were placed within the index's REFRESH_TICKS. The beds skipped are forgotten at that read; nothing else
		 * he remembers changes.
		 */
		public void forget() {
			if (reach != null) {
				reach.forget();
			}
		}

		void begin(Measure started) {
			measure = started;
		}

		/** The measure under way (his first beat made, his second not yet), or null. */
		public @Nullable Measure measure() {
			return measure;
		}

		void drop() {
			measure = null;
		}

		void ran(Run run) {
			last = run;
		}

		/** His last run since he came, or null. */
		public @Nullable Run last() {
			return last;
		}

		void reserves(List<VerdantBedBlockEntity> planted) {
			List<BlockPos> found = new ArrayList<>();
			for (VerdantBedBlockEntity bed : planted) {
				if (found.size() == MAX_RESERVES) {
					break;
				}
				found.add(bed.getBlockPos());
			}
			reserves = List.copyOf(found);
		}

		/** His reserves at his last think that chose a destination, nearest the perch first. */
		public List<BlockPos> reserves() {
			return reserves;
		}

		/** The first of them, his matter's bed; null when none. */
		public @Nullable BlockPos reserve() {
			return reserves.isEmpty() ? null : reserves.getFirst();
		}

		/** Whether he raises his matter now: once a presence. */
		boolean raise() {
			if (raised) {
				return false;
			}
			raised = true;
			return true;
		}

		void idle(@Nullable Idle why) {
			idle = why;
		}

		/** Why his last think ran and measured nothing (null: it did, it was stopped, or it raised his matter). */
		public @Nullable Idle idle() {
			return idle;
		}
	}

	/** Root-running behind PactService (GC-01): no on-request action; his rows, refusal, obligation, matter and Jade note. */
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
		 * Dossier 5 item 2: he comes only where two awake Verdant Beds the inviter keeps lie within his radius of the perch
		 * (a cube, his area). Whether one is full and another hungry is his work's question (an IDLE note), not his
		 * invitation's. Read from the loaded chunks' block entities (GardenReach: at most 9 chunks), loading none.
		 */
		@Override
		public List<Missing> missing(ServerLevel level, ServerPlayer inviter, PactPerchBlockEntity perch, PactDefinition definition) {
			UUID own = inviter.getUUID();
			GardenReach reach = new GardenReach(perch.getBlockPos(), definition.radius(), bed -> own.equals(bed.keeper()));
			return reach.beds(level, level.getGameTime()).size() >= BEDS ? List.of()
					: List.of(new Missing(NO_TWO_BEDS, Component.translatable(KEY + "missing", definition.radius())));
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
			return rows(level, record, level.getOverworldClockTime(), now);
		}

		@Override
		public List<Component> obligationRows(ServerLevel level, ServerPlayer player, BoundWill record, long now) {
			return PactObligations.rows(player, record, PactObligations.Act.FEED);
		}

		@Override
		public Component suspendedRow(Component name) {
			return Component.translatable("message.jugcraft.concordance.pact.row.state.suspended.serves", name);
		}

		@Override
		public Status work(ServerLevel level, PactSpiritEntity<?> spirit, BoundWill record, AgreementHolder holder, long time, long now) {
			return spirit instanceof BifronsEntity bifrons ? run(level, bifrons, record, holder, time, now) : Status.IDLE;
		}

		@Override
		public PactObligations.@Nullable Act obligation() {
			return PactObligations.Act.FEED;
		}

		@Override
		public List<String> accords() {
			return ACCORDS;
		}

		/**
		 * His matter (which beds are reserves) while a reserve of his stands, answered or not, so the answer can be changed at
		 * the perch; null without a presence of his here (a copied perch's view has none), or with no reserve now. The bed is
		 * read again (loaded, awake, its keeper still a beneficiary, planted, not under a Heart, still full). Reads only.
		 */
		@Override
		public PactService.@Nullable Complication complication(ServerLevel level, BoundWill record, PactPerchBlockEntity perch, long now) {
			if (!(perch.presence(level) instanceof BifronsEntity bifrons) || bifrons.owner() == null) {
				return null;
			}
			BlockPos reserve = bifrons.serviceMemory(Memory.class, Memory::new).reserve();
			if (reserve == null || !level.isLoaded(reserve) || !(level.getBlockEntity(reserve) instanceof VerdantBedBlockEntity bed)
					|| bed.isRemoved() || !bed.awake() || !GardenPacts.benefits(record, bed.keeper())
					|| bed.nutrients() < SOURCE_MIN || fallow(level, bed) || underAHeart(level, bed)) {
				return null;
			}
			return new PactService.Complication(RESERVES, Component.translatable(KEY + "matter.reserves", reserve.toShortString(), bed.nutrients()),
					reserve, ACCORDS);
		}

		/** Jade's note: reserves waiting for the answer (how many); else asleep by day; else why he idled; none while his pact is held. Reads only; names no player. */
		@Override
		public PactService.@Nullable Note note(ServerLevel level, PactSpiritEntity<?> spirit, BoundWill record, long time, long now) {
			if (!(spirit instanceof BifronsEntity bifrons) || !Pacts.serving(record)) {
				return null;
			}
			Memory memory = bifrons.serviceMemory(Memory.class, Memory::new);
			if (answer(level, bifrons) == null && !memory.reserves().isEmpty()) {
				return new PactService.Note("reserves", memory.reserves().size());
			}
			if (!Pacts.inHours(record.pact().service(), time)) {
				return new PactService.Note("asleep", 0);
			}
			if (memory.idle() == Idle.NO_FULL) {
				return new PactService.Note("no_full", 0);
			}
			return memory.idle() == Idle.NO_HUNGRY ? new PactService.Note("no_hungry", 0) : null;
		}
	}
}
