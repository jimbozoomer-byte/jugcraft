package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.courier.Couriers;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Habitat;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.sign.Sign;
import io.github.jimbozoomer.jugcraft.concordance.sign.Signs;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-04 (Dossier 1 items 3 and 5; docs/features/buer-pact.md): strain tending, Buer's service.
 * Each think while his pact serves (PactSpiritEntity.think: a presence verified at its perch; an offering due included,
 * GC-P27), by daylight and while the day's quota lasts, he takes from his bed index (GardenReach: loaded chunks only, at
 * most 64 beds of his beneficiaries' keeping, nearest the perch first) the beds the bed's own rule lets be tended now
 * (mayTendAt) that are not overworked, the most strained first (then the nearest), asks GardenPacts.target about at most
 * {@value #MAX_ASKED} of them in that order (whose bed it is now, the holder near, the holder's own hands), and eases one
 * strain from the first it may change through the bed's own tending, the call a Greenwarden's hand makes, so his tending
 * and theirs share each bed's 600-tick window. One bed a think at most; no nutrient, item or practice moves; the task is
 * counted on the record (its quota).
 * <p>
 * The overworked bed (GC-P22, GC-P23): a bed he eased today that stands at Habitat.MAX_STRAIN again is overworked; he
 * does not tend it while it stays there, and says so on the kept page and in Jade. When it eases by its own rules (compost,
 * a Mulch Maw's nutrient, rest while fallow) to 2 or less, the complaint ends, and the bed is his to mend again. What he
 * remembers is the presence's alone ({@link Memory}, PactSpiritEntity.serviceMemory; never saved), at most
 * {@value #MEMORY} beds: a restart or an unload forgets the complaint, and nothing is ever owed.
 */
public final class StrainTending {
	/** Its work id (worker/PactParser.WORK; tools/concordance_pacts.py WORK). */
	public static final String WORK = "strain_tending";
	/** His refusal's reason: no awake bed the inviter keeps within his radius of the perch (tools/concordance_pacts.py SERVICE_WORDS answers). */
	public static final String NOTHING_TO_MEND = "nothing_to_mend";
	/** The most beds he remembers at once (eased by him today, or overworked). Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int MEMORY = 16;
	/** The most beds one think asks GardenPacts.target about. Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int MAX_ASKED = 8;
	/** His words: message.jugcraft.concordance.pact.strain_tending.* (tools/concordance_pacts.py SERVICE_WORDS). */
	private static final String KEY = "message.jugcraft.concordance.pact.strain_tending.";
	/** Strain tending as the pact service of its work (Workers.register). */
	public static final PactService SERVICE = new Service();

	private StrainTending() {
	}

	/**
	 * Buer's one step for {@code record}'s pact at world time {@code time} and game time {@code now} ({@code holder}: its
	 * perch's). WORKING when he eased a bed; OUTSIDE_AGREEMENT outside his hours (he sleeps); FINISHED with the day's quota
	 * spent; what GardenPacts.target answers for the whole pact (suspended, held, the quota spent, the holder not near);
	 * else IDLE. Bounded: one index read (at most 64 beds, refreshed at most every 200 ticks), at most MAX_ASKED asked, at
	 * most one bed changed.
	 */
	static Status tend(ServerLevel level, BuerEntity buer, BoundWill record, AgreementHolder holder, long time, long now) {
		Memory memory = buer.serviceMemory(Memory.class, Memory::new);
		memory.today(now);
		memory.judge(level);
		BoundWill.Service service = record.pact().service();
		if (!Pacts.inHours(service, time)) {
			return Status.OUTSIDE_AGREEMENT;
		}
		if (Pacts.done(record.pact(), now) >= service.quota()) {
			return Status.FINISHED;
		}
		List<VerdantBedBlockEntity> ready = new ArrayList<>();
		for (VerdantBedBlockEntity bed : memory.reach(record).beds(level, now)) {
			if (bed.mayTendAt(now) && !memory.overworked(bed.getBlockPos())) {
				ready.add(bed);
			}
		}
		// The most strained first; List.sort is stable, so the index's order (nearest the perch) holds among equals.
		ready.sort(Comparator.comparingInt(VerdantBedBlockEntity::strainNow).reversed());
		for (int i = 0; i < Math.min(MAX_ASKED, ready.size()); i++) {
			VerdantBedBlockEntity bed = ready.get(i);
			BlockPos pos = bed.getBlockPos();
			Status refused = GardenPacts.target(level, record, pos, bed.keeper(), time, now);
			if (refused == Status.IDLE || refused == Status.BLOCKED_BY_ACCESS || refused == Status.OUTSIDE_AGREEMENT) {
				// This bed's own answer: not a beneficiary's now, not the holder's to change, or outside the area.
				continue;
			}
			if (refused != null) {
				// The pact's: suspended, held for upkeep, its quota spent, its holder not near (GC-S7: never a stand-in).
				return refused;
			}
			if (!bed.tend(now)) {
				return Status.IDLE;
			}
			memory.eased(pos);
			holder.taskDone(level, now);
			// Dossier 1 detail (b): his pistil points at the bed he chose (GC-02), then he unfurls over it.
			buer.attendWork(pos, BuerEntity.Gesture.WORK.ticks, now);
			buer.gesture(BuerEntity.Gesture.WORK);
			// The shared sign of work done (roadmap step 27), at the bed: each client draws it as its settings allow.
			Signs.show(level, pos, Sign.WORK);
			return Status.WORKING;
		}
		return Status.IDLE;
	}

	/**
	 * The rows of his kept page for {@code record} at world time {@code time} and game time {@code now} (serviceRows with the
	 * world's clock; tests give the time): today's eases, or that he sleeps until dawn with today's eases; then the
	 * overworked bed (one, or how many and the nearest), with every remedy the bed's own rules have. Reads only.
	 */
	public static List<Component> rows(ServerLevel level, BoundWill record, long time, long now) {
		BoundWill.Service service = record.pact().service();
		int done = Pacts.done(record.pact(), now);
		List<Component> rows = new ArrayList<>();
		rows.add(Pacts.inHours(service, time)
				? Component.translatable(KEY + "row.service", done, service.quota())
				: Component.translatable(KEY + "row.asleep", Workers.clock(service.from()), Workers.clock(service.to()), done, service.quota()));
		BuerEntity buer = presence(level, record);
		List<BlockPos> overworked = buer == null ? List.of() : buer.serviceMemory(Memory.class, Memory::new).shown(level, buer.perch());
		if (overworked.size() == 1) {
			rows.add(Component.translatable(KEY + "row.overworked", overworked.getFirst().toShortString()));
		} else if (overworked.size() > 1) {
			rows.add(Component.translatable(KEY + "row.overworked_more", overworked.size(), overworked.getFirst().toShortString()));
		}
		return rows;
	}

	/** The Buer kept at {@code record}'s perch, in {@code level} and loaded, a presence (an owner), not leaving; or null. Reads only. */
	static @Nullable BuerEntity presence(ServerLevel level, BoundWill record) {
		BoundWill.Pact pact = record.pact();
		if (pact == null || pact.place() == null || !pact.place().dimension().equals(level.dimension().identifier().toString())) {
			return null;
		}
		BlockPos at = Couriers.pos(pact.place());
		return level.isLoaded(at) && level.getBlockEntity(at) instanceof PactPerchBlockEntity perch
				&& perch.presence(level) instanceof BuerEntity buer && buer.owner() != null ? buer : null;
	}

	/**
	 * What one Buer presence remembers between its thinks (PactSpiritEntity.serviceMemory: never saved, lost with the
	 * entity): his bed index, the game day he speaks of, the beds he eased that day, and his complaints (overworked beds),
	 * each oldest first, together at most MEMORY (the oldest eased forgotten first). Server thread only; each read and
	 * change touches at most MEMORY beds, in loaded chunks only.
	 */
	public static final class Memory {
		private @Nullable GardenReach reach;
		private long day = Long.MIN_VALUE;
		private final LinkedHashSet<BlockPos> eased = new LinkedHashSet<>();
		private final LinkedHashSet<BlockPos> overworked = new LinkedHashSet<>();

		public Memory() {
		}

		/** {@code record}'s bed index, made again when its perch or radius changed (GardenReach.covers). */
		GardenReach reach(BoundWill record) {
			if (reach == null || !reach.covers(record)) {
				reach = GardenReach.of(record);
			}
			return reach;
		}

		/** A new game day (of game time {@code now}) forgets yesterday's eases; a complaint stands until its bed eases. */
		void today(long now) {
			long today = Math.floorDiv(now, Pacts.DAY);
			if (today != day) {
				day = today;
				eased.clear();
			}
		}

		/** He eased the bed at {@code pos} today. */
		void eased(BlockPos pos) {
			BlockPos at = pos.immutable();
			eased.remove(at);
			eased.add(at);
			while (eased.size() + overworked.size() > MEMORY) {
				Iterator<BlockPos> oldest = (eased.isEmpty() ? overworked : eased).iterator();
				oldest.next();
				oldest.remove();
			}
		}

		/**
		 * Each remembered bed read again where its chunk is loaded (one not loaded is left as it was): broken or asleep,
		 * forgotten; one he eased today standing at Habitat.MAX_STRAIN (as growth counts it), overworked; an overworked one
		 * eased to 2 or less since, no longer (GC-P22, GC-P23). Sizes never grow here.
		 */
		void judge(ServerLevel level) {
			for (Iterator<BlockPos> it = eased.iterator(); it.hasNext(); ) {
				BlockPos pos = it.next();
				if (!level.isLoaded(pos)) {
					continue;
				}
				VerdantBedBlockEntity bed = bed(level, pos);
				if (bed == null) {
					it.remove();
				} else if (bed.strainNow() >= Habitat.MAX_STRAIN) {
					it.remove();
					overworked.add(pos);
				}
			}
			overworked.removeIf(pos -> {
				if (!level.isLoaded(pos)) {
					return false;
				}
				VerdantBedBlockEntity bed = bed(level, pos);
				return bed == null || bed.strainNow() < Habitat.MAX_STRAIN;
			});
		}

		/** Whether he holds the bed at {@code pos} overworked. */
		public boolean overworked(BlockPos pos) {
			return overworked.contains(pos);
		}

		/** The beds he eased today, oldest first (tests and his rows). */
		public List<BlockPos> easedToday() {
			return List.copyOf(eased);
		}

		/** How many beds he remembers (at most MEMORY). */
		public int remembered() {
			return eased.size() + overworked.size();
		}

		/** His complaints that stand now (loaded, awake and still at Habitat.MAX_STRAIN), nearest {@code perch} first. Reads only. */
		List<BlockPos> shown(ServerLevel level, @Nullable BlockPos perch) {
			List<BlockPos> out = new ArrayList<>();
			for (BlockPos pos : overworked) {
				VerdantBedBlockEntity bed = level.isLoaded(pos) ? bed(level, pos) : null;
				if (bed != null && bed.strainNow() >= Habitat.MAX_STRAIN) {
					out.add(pos);
				}
			}
			if (perch != null) {
				out.sort(GardenReach.nearest(perch));
			}
			return out;
		}

		private static @Nullable VerdantBedBlockEntity bed(ServerLevel level, BlockPos pos) {
			return level.getBlockEntity(pos) instanceof VerdantBedBlockEntity bed && !bed.isRemoved() && bed.awake() ? bed : null;
		}
	}

	/** Strain tending behind PactService (GC-01): no on-request action; its rows, refusal, obligation and Jade note. */
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
		 * Dossier 1 item 6: he comes only where an awake bed the inviter keeps lies within his radius of the perch. Read from
		 * the loaded chunks' block entities (GardenReach: at most 9 chunks), loading none and changing nothing.
		 */
		@Override
		public List<Missing> missing(ServerLevel level, ServerPlayer inviter, PactPerchBlockEntity perch, PactDefinition definition) {
			UUID own = inviter.getUUID();
			boolean bed = !new GardenReach(perch.getBlockPos(), definition.radius(), found -> own.equals(found.keeper()))
					.beds(level, level.getGameTime()).isEmpty();
			return bed ? List.of() : List.of(new Missing(NOTHING_TO_MEND, Component.translatable(KEY + "missing", definition.radius())));
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
			return PactObligations.rows(player, record, PactObligations.Act.TEND);
		}

		@Override
		public Component suspendedRow(Component name) {
			return Component.translatable("message.jugcraft.concordance.pact.row.state.suspended.serves", name);
		}

		@Override
		public Status work(ServerLevel level, PactSpiritEntity<?> spirit, BoundWill record, AgreementHolder holder, long time, long now) {
			return spirit instanceof BuerEntity buer ? tend(level, buer, record, holder, time, now) : Status.IDLE;
		}

		@Override
		public PactObligations.@Nullable Act obligation() {
			return PactObligations.Act.TEND;
		}

		/** Jade's note: his standing complaints (how many), else that he sleeps till dawn; none while his pact is held. Reads only. */
		@Override
		public PactService.@Nullable Note note(ServerLevel level, PactSpiritEntity<?> spirit, BoundWill record, long time, long now) {
			if (!(spirit instanceof BuerEntity buer) || !Pacts.serving(record)) {
				return null;
			}
			int overworked = buer.serviceMemory(Memory.class, Memory::new).shown(level, buer.perch()).size();
			if (overworked > 0) {
				return new PactService.Note("overworked", overworked);
			}
			return Pacts.inHours(record.pact().service(), time) ? null : new PactService.Note("asleep", 0);
		}
	}
}
