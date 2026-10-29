package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.ecology.Graft;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Habitat;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Organism;
import io.github.jimbozoomer.jugcraft.concordance.garden.Grafting;
import io.github.jimbozoomer.jugcraft.concordance.garden.OrganismCropBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenAsking.Asked;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-07 (Dossier 4 items 2, 3 and 5; docs/features/foras-pact.md): graft keeping, Foras's
 * service. Each think while his pact serves (PactSpiritEntity.think: a presence verified at its perch; an offering due
 * included, GC-P27) and while the day's quota lasts, he makes at most one graft, and a graft is one server step:
 * <ol>
 * <li>Hosts: the awake Verdant Beds of his beneficiaries in his area (GardenReach.withStores: loaded chunks only, at most
 * 64, nearest the perch first) whose crop is the host of a known graft with a donor item (Grafting.graftFor) and that
 * carry no graft the rules know (Grafting.holds). Under his default, {@link #PRUDENT} (also what an unanswered or unknown
 * accord means), only those the graft would improve by the bed's cached habitat (VerdantBedBlockEntity.cachedHabitat: he
 * never takes a sample himself; Grafting.gains: no factor's fit worse, one better); answered {@link #ALWAYS}, every host.
 * Hosts the graft would not improve are his matter (the logician's objection), raised once a presence with his inspect
 * gesture; while it waits unanswered his presence says AWAITING_ANSWER instead of his step's status (PactSpiritEntity's
 * serve, GC-P47), his step meanwhile keeping to the safe course.</li>
 * <li>For each host, nearest first: its crop and its bed asked at the change (GardenPacts.target: serves there, a
 * beneficiary's, the holder near, the holder's own hands), then the plain containers touching his perch (the index's
 * stores, at most 16), each asked with the holder as its keeper, for one holding the graft's donor (its views read, no
 * transaction). At most {@value #MAX_ASKED} places asked a think (GardenAsking, every garden service's one rule).</li>
 * <li>Every precondition held: one donor taken from that store in one outer Fabric transaction, committed only when exactly
 * one came out ({@link #spend}); nothing in the world changes while it is open. Then, the transaction closed, the graft
 * applied (Grafting's apply: setGraft, which cannot fail, and forgetVerdict). A refused or short extraction spends nothing
 * and grafts nothing.</li>
 * </ol>
 * The graft is counted on the record, faced, gestured (his cap tips: ForasEntity) and counted aloud to the holder (Dossier 4
 * detail b, {@link #countLine}). He never holds a donor, never touches an inventory but his store's, never replaces a graft
 * the rules know and never grafts a stranger's bed. What he remembers ({@link Memory}) is the presence's alone and never
 * saved.
 */
public final class GraftKeeping {
	/** Its work id (worker/PactParser.WORK; tools/concordance_pacts.py WORK). */
	public static final String WORK = "graft_keeping";
	/** His refusal's reasons (tools/concordance_pacts.py SERVICE_WORDS answers): no store touches the perch; no host is near. */
	public static final String NO_STORE = "no_store";
	public static final String NO_HOST = "no_host";
	/** His matter (GC-S11, GC-P86): hosts a graft would not improve. */
	public static final String NO_GAIN = "no_gain";
	/** Only where it helps: the safe course, also what an unanswered or unknown accord means. Saved as the perch's accord; never renamed. */
	public static final String PRUDENT = "prudent";
	/** Graft every host. Saved as the perch's accord; never renamed. */
	public static final String ALWAYS = "always";
	/** His answers, in the order the perch offers them (append only). Keep equal to tools/concordance_pacts.py SERVICE_WORDS accords. */
	public static final List<String> ACCORDS = List.of(PRUDENT, ALWAYS);
	/** The most places one think asks GardenPacts.target about. Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int MAX_ASKED = 8;
	/** His words: message.jugcraft.concordance.pact.graft_keeping.* (tools/concordance_pacts.py SERVICE_WORDS). */
	private static final String KEY = "message.jugcraft.concordance.pact.graft_keeping.";
	/** Graft keeping as the pact service of its work (Workers.register). */
	public static final PactService SERVICE = new Service();

	private GraftKeeping() {
	}

	/** A host he may graft: the crop at {@code crop} on {@code bed}, an {@code organism} that is {@code graft}'s host. */
	public record Host(BlockPos crop, VerdantBedBlockEntity bed, Organism organism, Graft graft) {
	}

	/** One graft he made: the host crop, the graft's id, the store its donor came from, when (game time). */
	public record Grafted(BlockPos crop, String graft, BlockPos store, long at) {
	}

	/** Why his last think grafted nothing, for Jade and the kept page. */
	public enum Idle {
		/** No host he may graft: every host in his area carries a graft the rules know, or none is his holder's to change by hand. */
		NO_HOST,
		/** Hosts wait, but none he would graft now: the graft would not improve them, or their beds hold no reading yet. */
		NO_GAIN,
		/** Hosts he would graft, but no store he may take from holds their donor. */
		NO_DONOR
	}

	/**
	 * Foras's one step for {@code record}'s pact at world time {@code time} and game time {@code now} ({@code holder}: its
	 * perch's). WORKING when he grafted; OUTSIDE_AGREEMENT outside his hours; FINISHED with the day's quota spent; what
	 * GardenPacts.target answers for the whole pact (suspended, held, the quota spent, the holder not near); else IDLE (no
	 * host, none he would graft, no donor, his matter raised, or no question left this think). While his matter waits
	 * unanswered his presence says AWAITING_ANSWER instead (PactSpiritEntity's serve, GC-P47). Bounded: one index read (at
	 * most 64 beds, 16 stores), at most MAX_ASKED places asked, one extraction, at most one graft.
	 */
	static Status keep(ServerLevel level, ForasEntity foras, BoundWill record, AgreementHolder holder, long time, long now) {
		Memory memory = foras.serviceMemory(Memory.class, Memory::new);
		GardenReach reach = memory.reach(record);
		String answer = answer(level, foras);
		boolean always = ALWAYS.equals(answer);
		BoundWill.Service service = record.pact().service();
		if (!Pacts.inHours(service, time)) {
			return Status.OUTSIDE_AGREEMENT;
		}
		if (Pacts.done(record.pact(), now) >= service.quota()) {
			return Status.FINISHED;
		}
		List<Host> hosts = new ArrayList<>();
		List<BlockPos> useless = new ArrayList<>();
		int unread = 0;
		for (VerdantBedBlockEntity bed : reach.beds(level, now)) {
			Host host = host(level, bed);
			if (host == null) {
				continue;
			}
			Boolean helps = helps(level, host);
			if (Boolean.FALSE.equals(helps)) {
				useless.add(host.crop());
			}
			if (helps == null) {
				unread++;
			}
			if (always || Boolean.TRUE.equals(helps)) {
				hosts.add(host);
			}
		}
		memory.noticed(useless, unread);
		if (answer == null && !useless.isEmpty() && memory.raise()) {
			// Raised once a presence: he turns to the nearest host he would not improve and weighs it; his words wait on the
			// kept page, in Jade and on the roster (the accord machinery). Nothing is grafted this think.
			foras.attendWork(useless.getFirst(), ForasEntity.Gesture.INSPECT.ticks, now);
			foras.gesture(ForasEntity.Gesture.INSPECT);
			return Status.IDLE;
		}
		if (hosts.isEmpty()) {
			memory.idle(useless.isEmpty() && unread == 0 ? Idle.NO_HOST : Idle.NO_GAIN);
			return Status.IDLE;
		}
		List<BlockPos> stores = reach.stores(level, now);
		GardenAsking asking = new GardenAsking(level, record, time, now, MAX_ASKED);
		boolean reached = false;
		for (Host host : hosts) {
			Asked asked = asking.both(host.crop(), host.bed().keeper(), host.bed().getBlockPos(), host.bed().keeper());
			if (asked == Asked.PACT) {
				return asking.stop();
			}
			if (asked == Asked.SPENT) {
				return Status.IDLE;
			}
			if (asked != Asked.MAY) {
				continue; // not this host (another's, or the holder may not change crop or bed)
			}
			reached = true;
			String item = Grafting.donorItem(host.graft());
			for (BlockPos at : stores) {
				Asked store = asking.ask(at, record.holder());
				if (store == Asked.PACT) {
					return asking.stop();
				}
				if (store == Asked.SPENT) {
					return Status.IDLE;
				}
				if (store != Asked.MAY || item == null) {
					continue;
				}
				Storage<ItemVariant> storage = CourierWork.store(level, at);
				ItemVariant donor = storage == null ? null
						: StorageUtil.findStoredResource(storage, variant -> item.equals(BuiltInRegistries.ITEM.getKey(variant.getItem()).toString()));
				if (donor != null && spend(storage, donor)) {
					Grafting.apply(level, host.crop(), host.bed(), host.graft());
					return grafted(level, foras, record, holder, memory, new Grafted(host.crop(), host.graft().id(), at.immutable(), now), now);
				}
			}
		}
		memory.idle(reached ? Idle.NO_DONOR : Idle.NO_HOST);
		return Status.IDLE;
	}

	/**
	 * GC-P85: one {@code donor} taken from {@code store} in one outer transaction, committed only when exactly one came out;
	 * otherwise the transaction closes uncommitted and the store is as it was. Nothing else happens while it is open. Public
	 * for the test of a store that refuses.
	 */
	public static boolean spend(Storage<ItemVariant> store, ItemVariant donor) {
		try (Transaction transaction = Transaction.openOuter()) {
			if (store.extract(donor, 1, transaction) != 1) {
				return false;
			}
			transaction.commit();
			return true;
		}
	}

	/** A graft made: remembered, counted on the record, faced, gestured, and counted aloud (Dossier 4 detail b). */
	private static Status grafted(ServerLevel level, ForasEntity foras, BoundWill record, AgreementHolder holder, Memory memory, Grafted graft, long now) {
		memory.grafted(graft);
		holder.taskDone(level, now);
		foras.attendWork(graft.crop(), ForasEntity.Gesture.WORK.ticks, now);
		foras.gesture(ForasEntity.Gesture.WORK);
		counted(level, record, holder, now);
		return Status.WORKING;
	}

	/** Detail (b): he says the day's count to the holder (near: GardenPacts.target asked), on their action bar. */
	private static void counted(ServerLevel level, BoundWill record, AgreementHolder holder, long now) {
		ServerPlayer player = level.getServer().getPlayerList().getPlayer(record.holder());
		if (player == null || player.level() != level) {
			return;
		}
		BoundWill counted = holder.record(level);
		player.sendOverlayMessage(countLine((counted != null && counted.pact() != null ? counted : record).pact(), now));
	}

	/** Detail (b): his count as he says it, today's grafts of {@code pact} at game time {@code now} against its quota. Reads only. */
	public static Component countLine(BoundWill.Pact pact, long now) {
		return Component.translatable(KEY + "counted", Pacts.done(pact, now), pact.service().quota());
	}

	/** The host on {@code bed}, or null: its crop is the host of a known graft with a donor item, and it carries no graft the rules know. Reads only. */
	static @Nullable Host host(ServerLevel level, VerdantBedBlockEntity bed) {
		BlockPos crop = bed.getBlockPos().above();
		if (Grafting.holds(bed) || !level.isLoaded(crop) || !(level.getBlockState(crop).getBlock() instanceof OrganismCropBlock block)) {
			return null;
		}
		Organism organism = block.organism();
		Graft graft = organism == null ? null : Grafting.graftFor(organism.id());
		return graft == null ? null : new Host(crop, bed, organism, graft);
	}

	/** Whether {@code host}'s graft would improve it by its bed's cached habitat (GC-P84); null while the bed holds no reading. Never samples. */
	static @Nullable Boolean helps(ServerLevel level, Host host) {
		Habitat habitat = host.bed().cachedHabitat(level);
		return habitat == null ? null : Grafting.gains(host.bed(), host.organism(), host.graft(), habitat);
	}

	/** Whether a crop of a known graft's host stands on {@code bed}, grafted or not (his invitation). Reads only. */
	static boolean knownHost(ServerLevel level, VerdantBedBlockEntity bed) {
		BlockPos crop = bed.getBlockPos().above();
		return level.isLoaded(crop) && level.getBlockState(crop).getBlock() instanceof OrganismCropBlock block && block.organism() != null
				&& Grafting.graftFor(block.organism().id()) != null;
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
	 * The rows of his kept page for {@code record} at game time {@code now}: today's grafts against his quota (detail b);
	 * his last graft; and, waiting on a donor, that his store lacks one. Reads only.
	 */
	public static List<Component> rows(ServerLevel level, BoundWill record, long now) {
		BoundWill.Service service = record.pact().service();
		List<Component> rows = new ArrayList<>();
		rows.add(Component.translatable(KEY + "row.service", Pacts.done(record.pact(), now), service.quota()));
		ForasEntity foras = GardenPacts.presence(level, record, ForasEntity.class);
		if (foras == null) {
			return rows;
		}
		Memory memory = foras.serviceMemory(Memory.class, Memory::new);
		Grafted last = memory.last();
		if (last != null) {
			rows.add(Component.translatable(KEY + "row.last", last.crop().toShortString(), last.store().toShortString()));
		}
		if (memory.idle() == Idle.NO_DONOR) {
			rows.add(Component.translatable(KEY + "row.no_donor"));
		}
		return rows;
	}

	/**
	 * What one Foras presence remembers between its thinks (PactSpiritEntity.serviceMemory: never saved, lost with the
	 * entity): his index, the hosts the graft would not improve, how many hosts' beds hold no reading yet, whether he has
	 * raised his matter, why he last idled, and his last graft. It never holds a donor: a graft is over when its call
	 * returns. Server thread only.
	 */
	public static final class Memory {
		private @Nullable GardenReach reach;
		private List<BlockPos> useless = List.of();
		private int unread;
		private boolean raised;
		private @Nullable Idle idle;
		private @Nullable Grafted last;

		public Memory() {
		}

		/** {@code record}'s store index, made again when its perch or radius changed (GardenReach.covers). */
		GardenReach reach(BoundWill record) {
			if (reach == null || !reach.covers(record)) {
				reach = GardenReach.withStores(record);
			}
			return reach;
		}

		/** Forgets his index, so his next think reads his area again (GardenReach.forget): an operator's or a test's. */
		public void forget() {
			if (reach != null) {
				reach.forget();
			}
		}

		/** The hosts the graft would not improve now, nearest the perch first (their crops), and how many hosts' beds hold no reading; why he idled is forgotten. */
		void noticed(List<BlockPos> found, int unreadNow) {
			useless = List.copyOf(found);
			unread = unreadNow;
			idle = null;
		}

		/** The crops of the hosts the graft would not improve at his last think, nearest first. */
		public List<BlockPos> useless() {
			return useless;
		}

		/** How many hosts at his last think stood on a bed that holds no reading yet (he waits for it: no sample of his). */
		public int unread() {
			return unread;
		}

		/** The nearest of them, his matter's crop; null when none. */
		public @Nullable BlockPos matter() {
			return useless.isEmpty() ? null : useless.getFirst();
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

		/** Why his last think grafted nothing (null: it grafted, it was stopped, or it raised his matter). */
		public @Nullable Idle idle() {
			return idle;
		}

		void grafted(Grafted graft) {
			last = graft;
		}

		/** His last graft since he came, or null. */
		public @Nullable Grafted last() {
			return last;
		}
	}

	/** Graft keeping behind PactService (GC-01): no on-request action; his rows, refusal, obligation, matter and Jade note. */
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
		 * Dossier 4 item 2 (GC-P88): he comes only where a plain container touches the perch (his store) and a crop of a known
		 * graft's host grows on an awake Verdant Bed the inviter keeps within his radius, grafted or not. Both read in one
		 * pass of the loaded chunks' block entities (GardenReach: at most 9 chunks), loading none; the store first.
		 */
		@Override
		public List<Missing> missing(ServerLevel level, ServerPlayer inviter, PactPerchBlockEntity perch, PactDefinition definition) {
			UUID own = inviter.getUUID();
			GardenReach reach = GardenReach.withStores(perch.getBlockPos(), definition.radius(), bed -> own.equals(bed.keeper()));
			long now = level.getGameTime();
			List<Missing> missing = new ArrayList<>();
			if (reach.stores(level, now).isEmpty()) {
				missing.add(new Missing(NO_STORE, Component.translatable(KEY + "missing.store")));
			}
			boolean host = false;
			for (VerdantBedBlockEntity bed : reach.beds(level, now)) {
				if (knownHost(level, bed)) {
					host = true;
					break;
				}
			}
			if (!host) {
				missing.add(new Missing(NO_HOST, Component.translatable(KEY + "missing.host", definition.radius())));
			}
			return List.copyOf(missing);
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
			return PactObligations.rows(player, record, PactObligations.Act.GRAFT);
		}

		@Override
		public Component suspendedRow(Component name) {
			return Component.translatable("message.jugcraft.concordance.pact.row.state.suspended.serves", name);
		}

		@Override
		public Status work(ServerLevel level, PactSpiritEntity<?> spirit, BoundWill record, AgreementHolder holder, long time, long now) {
			return spirit instanceof ForasEntity foras ? keep(level, foras, record, holder, time, now) : Status.IDLE;
		}

		@Override
		public PactObligations.@Nullable Act obligation() {
			return PactObligations.Act.GRAFT;
		}

		@Override
		public List<String> accords() {
			return ACCORDS;
		}

		/**
		 * His matter (hosts the graft would not improve) while one stands, answered or not, so the answer can be changed at the
		 * perch; null without a presence of his here (a copied perch's view has none), or when the nearest such host is gone,
		 * grafted, or would gain now. The host is read again (loaded, its bed awake, its cached verdict). Reads only.
		 */
		@Override
		public PactService.@Nullable Complication complication(ServerLevel level, BoundWill record, PactPerchBlockEntity perch, long now) {
			if (!(perch.presence(level) instanceof ForasEntity foras) || foras.owner() == null) {
				return null;
			}
			Memory memory = foras.serviceMemory(Memory.class, Memory::new);
			BlockPos crop = memory.matter();
			if (crop == null || !level.isLoaded(crop) || !(level.getBlockEntity(crop.below()) instanceof VerdantBedBlockEntity bed)
					|| bed.isRemoved() || !bed.awake()) {
				return null;
			}
			Host host = host(level, bed);
			if (host == null || !Boolean.FALSE.equals(helps(level, host))) {
				return null;
			}
			return new PactService.Complication(NO_GAIN, Component.translatable(KEY + "matter.no_gain", memory.useless().size()), crop, ACCORDS);
		}

		/**
		 * Jade's note: hosts the graft would not improve (how many) unless answered "graft every host"; else hosts whose beds
		 * hold no reading yet (how many), or why he idled; none while his pact is held. Reads only; names no player.
		 */
		@Override
		public PactService.@Nullable Note note(ServerLevel level, PactSpiritEntity<?> spirit, BoundWill record, long time, long now) {
			if (!(spirit instanceof ForasEntity foras) || !Pacts.serving(record)) {
				return null;
			}
			Memory memory = foras.serviceMemory(Memory.class, Memory::new);
			if (!ALWAYS.equals(answer(level, foras)) && !memory.useless().isEmpty()) {
				return new PactService.Note("no_gain", memory.useless().size());
			}
			if (memory.idle() == Idle.NO_GAIN) {
				return new PactService.Note("no_reading", memory.unread());
			}
			if (memory.idle() == Idle.NO_DONOR) {
				return new PactService.Note("no_donor", 0);
			}
			return memory.idle() == Idle.NO_HOST ? new PactService.Note("no_host", 0) : null;
		}
	}
}
