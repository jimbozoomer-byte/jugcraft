package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.ecology.Organism;
import io.github.jimbozoomer.jugcraft.concordance.garden.Canopies;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-09 (Dossier 6 items 2, 3 and 5; docs/features/amdusias-pact.md): the bending canopy,
 * Amdusias's service. Each think while his pact serves (PactSpiritEntity.think: a presence verified at its perch, its
 * holder near; an offering due included, GC-P27) he finds his tree (a log within {@value #TREE_REACH} blocks of the perch)
 * and bends its shelter over the crops on his beneficiaries' awake beds in his area, the nearest first, at most his quota
 * at once: a cap, never a daily count (nothing is counted on the record). Each bed is asked at the change
 * (GardenPacts.target: his pact serves there, a beneficiary's, the holder near, the holder's own hands). Under the canopy
 * a crop reads the area's disturbance up to {@value #DAMPING} lower, never below 0 (Canopies; Habitat.sheltered): never a
 * Verdant Heart, never more stir, never harvest strain eased, never two canopies added. A crop that wants the stir
 * (Niche.wantsStir: a Gloamcap) he spares and asks about until his holder answers "shelter all" at the perch
 * ({@link #ALL}; {@link #SPARE}, the safe course, is what an unanswered or unknown accord means). The canopy is the
 * registry's, in memory only: this step refreshes it each think and lifts it whenever it cannot keep it, and it lapses
 * Canopies.LAPSE_TICKS after its last refresh whatever stopped him. He makes, moves and spends nothing and changes no
 * block; what he remembers ({@link Memory}) is the presence's alone and never saved.
 */
public final class CanopyShelter {
	/** Its work id (worker/PactParser.WORK; tools/concordance_pacts.py WORK). */
	public static final String WORK = "canopy_shelter";
	/** His refusal's reasons (tools/concordance_pacts.py SERVICE_WORDS answers): no tree beside the perch; no awake bed of the inviter's in his area. */
	public static final String NO_TREE = "no_tree";
	public static final String NO_BED = "no_bed";
	/** His matter (GC-S11, GC-P130): crops that want the stir his canopy takes away. */
	public static final String WANTS_STIR = "wants_stir";
	/** Spare them: the safe course, also what an unanswered or unknown accord means. Saved as the perch's accord; never renamed. */
	public static final String SPARE = "spare";
	/** Shelter all. Saved as the perch's accord; never renamed. */
	public static final String ALL = "all";
	/** His answers, in the order the perch offers them (append only). Keep equal to tools/concordance_pacts.py SERVICE_WORDS accords. */
	public static final List<String> ACCORDS = List.of(SPARE, ALL);
	/** How much his canopy lowers the stir a crop reads (at most Canopies.MAX_DAMPING). Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int DAMPING = 2;
	/** How far from his perch his tree may stand, on every axis (a 5 x 5 x 5 cube: 125 reads). Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int TREE_REACH = 2;
	/** How long he waits before looking for a tree again when none stands (ticks). Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int TREE_TICKS = 200;
	/** How often his tree sways while he shelters (ticks; Dossier 6 detail b: every 10 s). Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int SWAY_TICKS = 200;
	/** The most beds one think asks GardenPacts.target about (his cap). Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int MAX_ASKED = 16;
	/** His words: message.jugcraft.concordance.pact.canopy_shelter.* (tools/concordance_pacts.py SERVICE_WORDS). */
	private static final String KEY = "message.jugcraft.concordance.pact.canopy_shelter.";
	/** The canopy shelter as the pact service of its work (Workers.register). */
	public static final PactService SERVICE = new Service();

	private CanopyShelter() {
	}

	/** Why his last think sheltered nothing, for Jade. */
	public enum Idle {
		/** No crop of his beneficiaries' in his area he may shelter now (or every one spared). */
		NOTHING,
		/** His holder's canopy stands at another perch (one a holder). */
		ELSEWHERE
	}

	/**
	 * Amdusias's one step for {@code record}'s pact at world time {@code time} and game time {@code now}: WORKING while his
	 * canopy stands over at least one crop; WAITING_FOR_RESOURCES with no tree beside the perch; OUTSIDE_AGREEMENT outside his
	 * hours; what GardenPacts.target answers for the whole pact (suspended, held, the holder not near); else IDLE (nothing to
	 * shelter, every crop spared, or his holder's canopy elsewhere). Each stop lifts his canopy and forgets the crops he
	 * spared, so a stopped Amdusias says why he stopped, never an old question; a stop that never reaches this step (the
	 * holder away, suspended, held) leaves his memory stale, and what reads it asks {@link Memory#fresh} first. Each bed is
	 * asked before it is spared, so he spares only crops he may shelter; a bed refused for itself is skipped until his index
	 * is read again (Buer's rule, StrainTending), so refused beds never crowd out the rest. Bounded: his remembered log read
	 * (or 125 reads at most every TREE_TICKS), one index read (at most 64 beds, one block state and one block entity above
	 * each), at most MAX_ASKED beds asked, one registry write.
	 */
	static Status shelter(ServerLevel level, AmdusiasEntity amdusias, BoundWill record, AgreementHolder holder, long time, long now) {
		Memory memory = amdusias.serviceMemory(Memory.class, Memory::new);
		memory.served(now);
		BlockPos perch = amdusias.perch();
		BoundWill.Service service = record.pact().service();
		UUID owner = record.holder();
		if (perch == null) {
			return Status.IDLE;
		}
		if (!Pacts.inHours(service, time)) {
			Canopies.lift(level, owner, perch);
			memory.stopped();
			memory.spared(List.of());
			return Status.OUTSIDE_AGREEMENT;
		}
		BlockPos tree = memory.tree(level, perch, now);
		if (tree == null) {
			Canopies.lift(level, owner, perch);
			memory.stopped();
			memory.spared(List.of());
			return Status.WAITING_FOR_RESOURCES;
		}
		boolean all = ALL.equals(answer(level, amdusias));
		int cap = Math.min(service.quota(), Canopies.MAX_BEDS);
		List<BlockPos> covered = new ArrayList<>();
		List<BlockPos> spared = new ArrayList<>();
		int asked = 0;
		for (VerdantBedBlockEntity bed : memory.beds(level, record, now)) {
			if (covered.size() >= cap || asked >= MAX_ASKED) {
				break;
			}
			BlockPos pos = bed.getBlockPos();
			if (memory.skipped(pos)) {
				continue; // refused for itself since his index was last read
			}
			Organism plant = bed.canopyPlant(level);
			if (plant == null) {
				continue; // a Verdant Heart, a device, bare soil: never sheltered
			}
			asked++;
			Status refused = GardenPacts.target(level, record, pos, bed.keeper(), time, now);
			if (refused == Status.IDLE || refused == Status.BLOCKED_BY_ACCESS) {
				// This bed's own answer (not a beneficiary's now, not the holder's to change): skipped until the index is read
				// again, and never spared, so his matter names only crops he may shelter (the unit review).
				memory.skip(pos);
				continue;
			}
			if (refused != null) {
				// His pact cannot serve now (suspended, held, the holder away): no canopy anywhere.
				Canopies.lift(level, owner, perch);
				memory.stopped();
				memory.spared(List.of());
				return refused;
			}
			if (bed.niche(plant).wantsStir() && !all) {
				spared.add(pos);
				continue;
			}
			covered.add(pos);
		}
		memory.spared(spared);
		if (covered.isEmpty()) {
			Canopies.lift(level, owner, perch);
			memory.stopped();
			memory.idle(Idle.NOTHING);
			raise(level, amdusias, memory, now);
			return Status.IDLE;
		}
		if (!Canopies.cover(level, owner, perch, DAMPING, all, covered, now)) {
			memory.stopped();
			memory.idle(Idle.ELSEWHERE);
			return Status.IDLE;
		}
		memory.idle(null);
		if (memory.formed(covered, now)) {
			// Bent anew: he turns to his tree and dips his branch toward it, bowing its shelter over the beds.
			amdusias.attendWork(tree, AmdusiasEntity.Gesture.WORK.ticks, now);
			amdusias.gesture(AmdusiasEntity.Gesture.WORK);
		} else {
			raise(level, amdusias, memory, now);
		}
		if (memory.swayDue(now)) {
			amdusias.sway(level, tree);
		}
		return Status.WORKING;
	}

	/**
	 * His matter raised once a presence, while unanswered: he turns to the nearest crop he spares and weighs it; his words wait
	 * on the kept page, in Jade and on the roster (the accord machinery). His canopy over the rest stands meanwhile.
	 */
	private static void raise(ServerLevel level, AmdusiasEntity amdusias, Memory memory, long now) {
		if (!memory.spared().isEmpty() && answer(level, amdusias) == null && memory.raise()) {
			amdusias.attendWork(memory.spared().getFirst(), AmdusiasEntity.Gesture.INSPECT.ticks, now);
			amdusias.gesture(AmdusiasEntity.Gesture.INSPECT);
		}
	}

	/**
	 * The holder's answer to his matter as {@code spirit}'s own perch keeps it (PactAccords.answer: one of his ACCORDS, its
	 * pact counting there); null while unanswered, unknown, or not readable.
	 */
	static @Nullable String answer(ServerLevel level, PactSpiritEntity<?> spirit) {
		BlockPos at = spirit.perch();
		return at != null && level.isLoaded(at) && level.getBlockEntity(at) instanceof PactPerchBlockEntity perch ? PactAccords.answer(level, perch) : null;
	}

	/**
	 * The log {@code amdusias} bends: his remembered tree, or (a visitor, or none remembered) the nearest log within
	 * TREE_REACH of his perch; null for none. Reads at most 125 blocks, loaded only (his departure's sway).
	 */
	public static @Nullable BlockPos tree(ServerLevel level, AmdusiasEntity amdusias) {
		BlockPos remembered = amdusias.serviceMemory(Memory.class, Memory::new).tree();
		BlockPos at = amdusias.perch();
		return remembered != null ? remembered : at == null ? null : Memory.find(level, at);
	}

	/**
	 * The rows of his kept page for {@code record} at game time {@code now}: the beds sheltered now of his cap and the damping
	 * (its gauge); then, as it stands, no tree, or the crops he spares while unanswered (as of a think within
	 * Canopies.LAPSE_TICKS: a stopped Amdusias spares nothing). Reads only.
	 */
	public static List<Component> rows(ServerLevel level, BoundWill record, long now) {
		BoundWill.Service service = record.pact().service();
		List<Component> rows = new ArrayList<>();
		AmdusiasEntity amdusias = GardenPacts.presence(level, record, AmdusiasEntity.class);
		Canopies.Canopy canopy = Canopies.of(level, record.holder(), now);
		int beds = canopy != null && amdusias != null && canopy.perch().equals(amdusias.perch()) ? canopy.beds().size() : 0;
		rows.add(Component.translatable(KEY + "row.service", beds, Math.min(service.quota(), Canopies.MAX_BEDS), DAMPING));
		if (amdusias == null) {
			return rows;
		}
		Memory memory = amdusias.serviceMemory(Memory.class, Memory::new);
		if (memory.looked() && memory.tree() == null) {
			rows.add(Component.translatable(KEY + "row.no_tree", TREE_REACH));
		} else if (memory.fresh(now) && !memory.spared().isEmpty() && answer(level, amdusias) == null) {
			rows.add(Component.translatable(KEY + "row.spared", memory.spared().size()));
		}
		return rows;
	}

	/** What one presence remembers between its thinks; never saved, lost with the entity (S6). Server thread only. */
	public static final class Memory {
		private @Nullable GardenReach reach;
		private final HashSet<BlockPos> skipped = new HashSet<>();
		private int skippedAt = -1;
		private @Nullable BlockPos tree;
		private long lookedAt = Long.MIN_VALUE;
		private long servedAt = Long.MIN_VALUE;
		private List<BlockPos> covered = List.of();
		private List<BlockPos> spared = List.of();
		private @Nullable Idle idle;
		private long shelteredAt = Long.MIN_VALUE;
		private long swayedAt = Long.MIN_VALUE;
		private boolean raised;

		public Memory() {
		}

		/** His bed index for {@code record}'s area (GardenReach.of, made again, forgetting the beds skipped, when the area changed). */
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

		/** Whether the bed at {@code pos} was refused for itself since the index was last read (tests and shelter). */
		public boolean skipped(BlockPos pos) {
			return skipped.contains(pos);
		}

		/** His service took a step at game time {@code now} (the top of shelter, whatever it answered). */
		void served(long now) {
			servedAt = now;
		}

		/**
		 * Whether what he remembers of his last step (the crops spared, why he idled) is current at game time {@code now}: his
		 * service stepped less than Canopies.LAPSE_TICKS before (a clock run back: stale). A stop outside his step (the holder
		 * away or offline, suspended, held) never clears it, so Jade, the kept page and his matter ask this first.
		 */
		public boolean fresh(long now) {
			return servedAt != Long.MIN_VALUE && now >= servedAt && now - servedAt < Canopies.LAPSE_TICKS;
		}

		/** Forgets his bed index, so his next think reads his area again (GardenReach.forget): an operator's or a test's. */
		public void forget() {
			if (reach != null) {
				reach.forget();
			}
		}

		/**
		 * His tree at game time {@code now}: the remembered log while it stands (one read); felled, looked for again at once;
		 * none found, looked for again only TREE_TICKS later (a clock run back looks at once).
		 */
		@Nullable BlockPos tree(ServerLevel level, BlockPos perch, long now) {
			if (tree != null && level.isLoaded(tree) && level.getBlockState(tree).is(BlockTags.LOGS)) {
				return tree;
			}
			if (tree == null && lookedAt != Long.MIN_VALUE && now >= lookedAt && now - lookedAt < TREE_TICKS) {
				return null;
			}
			tree = find(level, perch);
			lookedAt = now;
			return tree;
		}

		/** The nearest log within TREE_REACH of {@code perch} on every axis, in loaded chunks (125 reads); null for none. */
		static @Nullable BlockPos find(ServerLevel level, BlockPos perch) {
			BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
			BlockPos best = null;
			for (int dy = -TREE_REACH; dy <= TREE_REACH; dy++) {
				for (int dx = -TREE_REACH; dx <= TREE_REACH; dx++) {
					for (int dz = -TREE_REACH; dz <= TREE_REACH; dz++) {
						at.setWithOffset(perch, dx, dy, dz);
						if (level.isLoaded(at) && level.getBlockState(at).is(BlockTags.LOGS) && (best == null || at.distSqr(perch) < best.distSqr(perch))) {
							best = at.immutable();
						}
					}
				}
			}
			return best;
		}

		/** Whether his canopy is newly formed this think (none stood at his last), remembering what it covers. */
		boolean formed(List<BlockPos> beds, long now) {
			boolean fresh = shelteredAt == Long.MIN_VALUE || now < shelteredAt || now - shelteredAt >= Canopies.LAPSE_TICKS;
			shelteredAt = now;
			covered = List.copyOf(beds);
			return fresh;
		}

		/** His canopy is down (lifted or refused): nothing covered, and his tree sways at once when it next stands. */
		void stopped() {
			shelteredAt = Long.MIN_VALUE;
			swayedAt = Long.MIN_VALUE;
			covered = List.of();
		}

		/** Whether his tree sways at game time {@code now}: at once when his canopy stands again, then every SWAY_TICKS while it does. */
		boolean swayDue(long now) {
			if (swayedAt == Long.MIN_VALUE || now < swayedAt || now - swayedAt >= SWAY_TICKS) {
				swayedAt = now;
				return true;
			}
			return false;
		}

		/** Whether his matter is raised now: once a presence. */
		boolean raise() {
			if (raised) {
				return false;
			}
			raised = true;
			return true;
		}

		void spared(List<BlockPos> beds) {
			spared = List.copyOf(beds);
		}

		void idle(@Nullable Idle why) {
			idle = why;
		}

		/** His tree as of his last look (null: none found, or none looked for yet). */
		public @Nullable BlockPos tree() {
			return tree;
		}

		/** Whether he has looked for his tree since he came (before his first think he has not, and says nothing of it). */
		public boolean looked() {
			return lookedAt != Long.MIN_VALUE;
		}

		/** The beds his canopy covers as of his last think (nearest first). */
		public List<BlockPos> covered() {
			return covered;
		}

		/** The beds whose crops want the stir that he spared at his last think (nearest first); none once he stopped. */
		public List<BlockPos> spared() {
			return spared;
		}

		/** Why his last think sheltered nothing (null: it sheltered, or it was stopped). */
		public @Nullable Idle idle() {
			return idle;
		}
	}

	/** The canopy shelter behind PactService (GC-01): no on-request action; his rows, refusal, obligation, matter and Jade note. */
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
		 * Dossier 6 item 2: he comes only where a log stands within TREE_REACH of the perch and an awake Verdant Bed the inviter
		 * keeps lies within his radius (a cube, his area), read from the loaded chunks (GardenReach: at most 9 chunks), loading
		 * none: each missing one named, the tree first.
		 */
		@Override
		public List<Missing> missing(ServerLevel level, ServerPlayer inviter, PactPerchBlockEntity perch, PactDefinition definition) {
			List<Missing> out = new ArrayList<>();
			if (Memory.find(level, perch.getBlockPos()) == null) {
				out.add(new Missing(NO_TREE, Component.translatable(KEY + "missing.tree", TREE_REACH)));
			}
			UUID own = inviter.getUUID();
			if (new GardenReach(perch.getBlockPos(), definition.radius(), found -> own.equals(found.keeper())).beds(level, level.getGameTime()).isEmpty()) {
				out.add(new Missing(NO_BED, Component.translatable(KEY + "missing.bed", definition.radius())));
			}
			return List.copyOf(out);
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
			return PactObligations.rows(player, record, PactObligations.Act.HARVEST);
		}

		@Override
		public Component suspendedRow(Component name) {
			return Component.translatable("message.jugcraft.concordance.pact.row.state.suspended.serves", name);
		}

		@Override
		public Status work(ServerLevel level, PactSpiritEntity<?> spirit, BoundWill record, AgreementHolder holder, long time, long now) {
			return spirit instanceof AmdusiasEntity amdusias ? shelter(level, amdusias, record, holder, time, now) : Status.IDLE;
		}

		@Override
		public PactObligations.@Nullable Act obligation() {
			return PactObligations.Act.HARVEST;
		}

		@Override
		public String quotaTerm() {
			return "term.quota." + WORK;
		}

		@Override
		public List<String> accords() {
			return ACCORDS;
		}

		/**
		 * His matter while a crop that wants the stir is spared in his area, answered or not (so the answer can be changed at
		 * the perch): the nearest, re-read (one bed: still awake, still bearing such a plant), with how many he spares; null
		 * otherwise, and null while his last step is stale (Memory.fresh: a stopped Amdusias asks nothing). Reads only (his
		 * presence's memory, one bed).
		 */
		@Override
		public PactService.@Nullable Complication complication(ServerLevel level, BoundWill record, PactPerchBlockEntity perch, long now) {
			AmdusiasEntity amdusias = GardenPacts.presence(level, record, AmdusiasEntity.class);
			Memory memory = amdusias == null ? null : amdusias.serviceMemory(Memory.class, Memory::new);
			List<BlockPos> spared = memory == null || !memory.fresh(now) ? List.of() : memory.spared();
			if (spared.isEmpty()) {
				return null;
			}
			BlockPos first = spared.getFirst();
			if (!level.isLoaded(first) || !(level.getBlockEntity(first) instanceof VerdantBedBlockEntity bed) || bed.isRemoved() || !bed.awake()) {
				return null;
			}
			Organism plant = bed.canopyPlant(level);
			if (plant == null || !bed.niche(plant).wantsStir()) {
				return null;
			}
			return new PactService.Complication(WANTS_STIR, Component.translatable(KEY + "matter.wants_stir", spared.size(), first.toShortString()),
					first, ACCORDS);
		}

		/**
		 * Jade's note: no tree (looked for and none found); crops spared while unanswered (how many, as of a fresh step); the
		 * beds his canopy covers now (how many, the registry's own reading, as his kept page's gauge reads it); else why he
		 * idled (as of a fresh step); none while his pact is held. Reads only; names no player.
		 */
		@Override
		public PactService.@Nullable Note note(ServerLevel level, PactSpiritEntity<?> spirit, BoundWill record, long time, long now) {
			if (!(spirit instanceof AmdusiasEntity amdusias) || !Pacts.serving(record)) {
				return null;
			}
			Memory memory = amdusias.serviceMemory(Memory.class, Memory::new);
			if (memory.looked() && memory.tree() == null) {
				return new PactService.Note("no_tree", 0);
			}
			boolean fresh = memory.fresh(now);
			if (fresh && answer(level, amdusias) == null && !memory.spared().isEmpty()) {
				return new PactService.Note("spared", memory.spared().size());
			}
			Canopies.Canopy canopy = Canopies.of(level, record.holder(), now);
			if (canopy != null && canopy.perch().equals(amdusias.perch()) && !canopy.beds().isEmpty()) {
				return new PactService.Note("covered", canopy.beds().size());
			}
			if (!fresh) {
				return null;
			}
			return memory.idle() == Idle.ELSEWHERE ? new PactService.Note("elsewhere", 0)
					: memory.idle() == Idle.NOTHING ? new PactService.Note("nothing", 0) : null;
		}
	}
}
