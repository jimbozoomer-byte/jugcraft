package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.ComposeText;
import io.github.jimbozoomer.jugcraft.concordance.compose.Text;
import io.github.jimbozoomer.jugcraft.concordance.courier.Couriers;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Code;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Diagnostic;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Factor;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Graft;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Habitat;
import io.github.jimbozoomer.jugcraft.concordance.ecology.HabitatAdvice;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Niche;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Organism;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Verdict;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.garden.OrganismCropBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.sign.Sign;
import io.github.jimbozoomer.jugcraft.concordance.sign.Signs;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-06 (Dossier 3 items 3 and 5; docs/features/marax-pact.md): habitat reading, Marax's
 * service. On his holder's request at the perch (the kept page's Recall action; holder only; twice a game day, at any
 * hour) his familiar reads up to {@value #READ_BEDS} of the planted Verdant Beds in his index (GardenReach: the holder's,
 * loaded chunks only, nearest the perch first), each asked of GardenPacts.target, from the sample each bed already holds
 * (VerdantBedBlockEntity.cachedHabitat: it takes none), and names for the worst the one change HabitatAdvice gives. It
 * changes nothing: no sample, note, nutrient, strain, graft, item or practice; only the day's count on the record, and
 * the familiar's flight to the bed (PactSpiritEntity.showTarget: synced, never saved) while the answer is on screen.
 * <p>
 * The proud familiar (GC-P69): a bed named for the same factor on an unchanged verdict at {@value #PIQUE} readings
 * running is not read again while its verdict stays; the next-worst is read instead. What it remembers is the presence's
 * alone ({@link Memory}, PactSpiritEntity.serviceMemory; never saved): a restart forgets the pique, and nothing is owed.
 * <p>
 * Its reading is one sentence (Dossier 3 item 8) whose change, its remedy and its clause are arguments of their own, so
 * the magic interfaces' kept page draws the change short ("Light 9 → 11") and keeps the whole sentence as its tip.
 */
public final class HabitatReading {
	/** Its work id (worker/PactParser.WORK and HOLDER_ONLY; tools/concordance_pacts.py WORK). */
	public static final String WORK = "habitat_reading";
	/** The one choice its on-request action offers (PactStatePayload.Choice id). */
	public static final String CHOICE = "reading";
	/** A change named (tools/concordance_pacts.py SERVICE_WORDS answers, as the four below; none one of PactMenu's ANSWERS). */
	public static final String READ = "read";
	/** No planted bed of the holder's in his area that he may read; also his refusal of an invitation. */
	public static final String NOTHING_GROWS = "nothing_grows";
	/** Planted beds, none of them sampled yet. */
	public static final String NO_SAMPLE = "no_sample";
	/** Every crop read is thriving (or left proud). */
	public static final String NOTHING_TO_CORRECT = "nothing_to_correct";
	/** No Marax at the perch to send his familiar. */
	public static final String ABSENT = "absent";
	/** The most planted beds one reading asks about. Keep equal to tools/concordance_garden_pacts.py CONSTANTS. */
	public static final int READ_BEDS = 8;
	/** Readings running that name one bed and factor on one verdict before the familiar will not repeat itself. Keep equal to CONSTANTS. */
	public static final int PIQUE = 3;
	/** How long the familiar may hover over the bed it named while the answer is on screen (ticks). Keep equal to CONSTANTS. */
	public static final int SHOW_TICKS = 400;
	/** His words: message.jugcraft.concordance.pact.habitat_reading.* (tools/concordance_pacts.py SERVICE_WORDS). */
	private static final String KEY = "message.jugcraft.concordance.pact.habitat_reading.";
	/** Habitat reading as the pact service of its work (Workers.register). */
	public static final PactService SERVICE = new Service();

	private HabitatReading() {
	}

	/**
	 * {@code player} asks for a reading at {@code holder}'s perch at world time {@code time} and game time {@code now}: the
	 * answer, refused with one reason before anything is counted. Bounded: one index read (at most 64 beds, refreshed at
	 * most every 200 ticks), at most READ_BEDS planted beds asked, one HabitatAdvice call.
	 */
	public static PactMenu.Answered read(ServerLevel level, ServerPlayer player, AgreementHolder holder, long time, long now) {
		if (!Workers.enabled()) {
			return refused(Pacts.Reckoning.DISABLED, 0);
		}
		AgreementHolder.Standing standing = holder.verify(level, now);
		if (standing != AgreementHolder.Standing.VALID) {
			return refused(standing == AgreementHolder.Standing.NONE ? Pacts.Reckoning.NOTHING : Pacts.Reckoning.CONFLICTING, 0);
		}
		BoundWill record = holder.record(level);
		BoundWill.Service service = record.pact() == null ? null : record.pact().service();
		if (service == null || !WORK.equals(service.work())) {
			return refused(Pacts.Reckoning.NOTHING, 0);
		}
		if (Authority.isStandIn(player) || !record.holder().equals(player.getUUID())) {
			return refused(Pacts.Reckoning.NOT_HOLDER, 0);
		}
		Pacts.Reckoning serves = Pacts.serves(record, Couriers.place(level, player.blockPosition()), time, now);
		if (serves != null) {
			return refused(serves, service.quota());
		}
		MaraxEntity marax = GardenPacts.presence(level, record, MaraxEntity.class);
		if (marax == null || marax.gone()) {
			return new PactMenu.Answered(ABSENT, false, Component.translatable(KEY + "absent"));
		}
		Memory memory = marax.serviceMemory(Memory.class, Memory::new);
		memory.judge(level);
		int asked = 0;
		int planted = 0;
		int sampled = 0;
		VerdantBedBlockEntity worst = null;
		Organism worstOrganism = null;
		Habitat worstHabitat = null;
		Verdict worstVerdict = null;
		BlockPos passed = null;
		for (VerdantBedBlockEntity bed : memory.reach(record).beds(level, now)) {
			BlockPos pos = bed.getBlockPos();
			Organism organism = planted(level, pos);
			if (organism == null) {
				continue;
			}
			if (++asked > READ_BEDS) {
				break;
			}
			Status refused = GardenPacts.target(level, record, pos, bed.keeper(), time, now);
			if (refused == Status.IDLE || refused == Status.BLOCKED_BY_ACCESS || refused == Status.OUTSIDE_AGREEMENT) {
				// This bed's own answer: not a beneficiary's now, not the holder's to touch, or outside his area.
				continue;
			}
			if (refused != null) {
				// The pact's: suspended, held for upkeep, its quota spent, its holder not near (never a stand-in).
				return refused(reckoning(refused), service.quota());
			}
			planted++;
			Habitat habitat = bed.cachedHabitat(level);
			if (habitat == null) {
				continue;
			}
			sampled++;
			Verdict verdict = Verdict.of(bed.niche(organism), habitat);
			if (verdict.growth() == Verdict.Growth.THRIVING) {
				continue;
			}
			if (memory.proud(pos)) {
				passed = passed == null ? pos : passed;
				continue;
			}
			// The worst growth; the index's order (nearest the perch first) holds among equals.
			if (worstVerdict == null || verdict.growth().pace < worstVerdict.growth().pace) {
				worst = bed;
				worstOrganism = organism;
				worstHabitat = habitat;
				worstVerdict = verdict;
			}
		}
		if (planted == 0) {
			return new PactMenu.Answered(NOTHING_GROWS, false, Component.translatable(KEY + "nothing_grows"));
		}
		if (sampled == 0) {
			return new PactMenu.Answered(NO_SAMPLE, false, Component.translatable(KEY + "no_sample"));
		}
		if (worst == null) {
			return new PactMenu.Answered(NOTHING_TO_CORRECT, false, passed == null ? Component.translatable(KEY + "nothing_to_correct")
					: Component.translatable(KEY + "nothing_to_correct.proud", passed.toShortString()));
		}
		BlockPos pos = worst.getBlockPos();
		Niche niche = worst.niche(worstOrganism);
		Habitat habitat = worstHabitat;
		Map<String, Niche> grafts = grafts(worst, worstOrganism);
		HabitatAdvice.Advice advice = HabitatAdvice.one(niche, habitat, grafts);
		if (advice == null) {
			return new PactMenu.Answered(NOTHING_TO_CORRECT, false, Component.translatable(KEY + "nothing_to_correct"));
		}
		Component crop = level.getBlockState(pos.above()).getBlock().getName();
		memory.named(pos, advice.factor(), worstVerdict, crop);
		holder.taskDone(level, now);
		// GC-02 (the critique's item 9): while his work gesture plays he faces the bed his familiar names, then his speaker.
		marax.attendWork(pos, MaraxEntity.Gesture.WORK.ticks, now);
		// Dossier 3 item 7(b): the familiar flies to the bed it names while the answer is on screen.
		marax.showTarget(pos, now + SHOW_TICKS);
		// The shared sign of work done (roadmap step 27), at the bed: each client draws it as its settings allow.
		Signs.show(level, pos, Sign.WORK);
		return new PactMenu.Answered(READ, true, said(crop, pos, advice, passed));
	}

	/** The organism growing on the bed at {@code bed} (its crop above), or null when nothing is planted. Reads only (tests read it too). */
	public static @Nullable Organism planted(ServerLevel level, BlockPos bed) {
		return level.getBlockState(bed.above()).getBlock() instanceof OrganismCropBlock crop ? crop.organism() : null;
	}

	/** The niches the plant on {@code bed} would grow by under each graft it could take now (none while it carries one), by id. */
	private static Map<String, Niche> grafts(VerdantBedBlockEntity bed, Organism organism) {
		Map<String, Niche> out = new TreeMap<>();
		if (!bed.graft().isEmpty()) {
			return out;
		}
		for (Graft graft : Garden.catalog().grafts().values()) {
			if (graft.host().equals(organism.id())) {
				out.put(graft.id(), Garden.catalog().niche(organism, graft.id()));
			}
		}
		return out;
	}

	/**
	 * The reading in one sentence (Dossier 3 item 8: never a colour or a particle alone): the crop and its bed, its growth,
	 * the change with the factor's value, the value to bring it to, its ideal range and how (its remedy), whether that alone
	 * moves it on or is one of n (its clause), a graft that would serve, a proud bed passed over. Its arguments in that order:
	 * crop, bed, growth, remedy, clause, graft, proud (the kept page's glance draws the remedy and the clause).
	 */
	static Component said(Component crop, BlockPos pos, HabitatAdvice.Advice advice, @Nullable BlockPos passed) {
		Component growth = ComposeText.show(Text.of("ecology.growth." + advice.growth().id));
		Component clause = advice.alone() ? Component.translatable(KEY + "advice.alone")
				: Component.translatable(KEY + "advice.one_of", advice.of());
		Component graft = advice.graft() == null ? Component.empty()
				: Component.translatable(KEY + "advice.graft", ComposeText.name(new Text.Ref("graft", advice.graft())));
		Component proud = passed == null ? Component.empty() : Component.translatable(KEY + "read.proud", passed.toShortString());
		return Component.translatable(KEY + "read", crop, pos.toShortString(), growth, remedy(advice), clause, graft, proud);
	}

	/** The change in words, one key a remedy: the factor's name, its value, the value to bring it to, its ideal range, how. */
	static Component remedy(HabitatAdvice.Advice advice) {
		Object[] args = {ComposeText.name(new Text.Ref("factor", advice.factor().id)), advice.value(), advice.target(),
				advice.range().idealMin(), advice.range().idealMax()};
		return switch (advice.remedy()) {
			case "moisture.more" -> Component.translatable(KEY + "remedy.moisture.more", args);
			case "moisture.less" -> Component.translatable(KEY + "remedy.moisture.less", args);
			case "light.more" -> Component.translatable(KEY + "remedy.light.more", args);
			case "light.less" -> Component.translatable(KEY + "remedy.light.less", args);
			case "nutrients.more" -> Component.translatable(KEY + "remedy.nutrients.more", args);
			case "nutrients.less" -> Component.translatable(KEY + "remedy.nutrients.less", args);
			case "diversity.more" -> Component.translatable(KEY + "remedy.diversity.more", args);
			case "diversity.less" -> Component.translatable(KEY + "remedy.diversity.less", args);
			case "disturbance.more" -> Component.translatable(KEY + "remedy.disturbance.more", args);
			case "disturbance.less" -> Component.translatable(KEY + "remedy.disturbance.less", args);
			default -> Component.translatable(KEY + "remedy.strain", args);
		};
	}

	private static PactMenu.Answered refused(Pacts.Reckoning why, int quota) {
		Component said = switch (why) {
			case DISABLED -> Component.translatable("message.jugcraft.concordance.disabled");
			// A copy of a perch (another agreement covers it): the shared diagnostic, as Star-reckoning tells it.
			case CONFLICTING -> ComposeText.show(Diagnostic.of(Code.CONFLICTING_AGREEMENT).text());
			case NOT_HOLDER -> Component.translatable(KEY + "not_holder");
			case SUSPENDED -> Component.translatable(KEY + "suspended");
			case UPKEEP_HELD -> Component.translatable(KEY + "upkeep_held");
			case OUTSIDE_AREA -> Component.translatable(KEY + "outside_area");
			case OUTSIDE_HOURS -> Component.translatable(KEY + "outside_hours");
			case QUOTA_SPENT -> Component.translatable(KEY + "quota_spent", quota);
			default -> Component.translatable(KEY + "nothing");
		};
		return new PactMenu.Answered(why.id, false, said);
	}

	/** GardenPacts.target's answer for the whole pact, as a reckoning (its holder not near reads as not the holder here). */
	private static Pacts.Reckoning reckoning(Status refused) {
		return switch (refused) {
			case SUSPENDED -> Pacts.Reckoning.SUSPENDED;
			case UPKEEP_HELD -> Pacts.Reckoning.UPKEEP_HELD;
			case FINISHED -> Pacts.Reckoning.QUOTA_SPENT;
			default -> Pacts.Reckoning.NOT_HOLDER;
		};
	}

	/**
	 * The rows of his kept page for {@code record} at game time {@code now}: today's readings of the quota; the last
	 * reading (its crop, bed and factor); the proud bed (one, or how many and the nearest). Reads only; names no player.
	 */
	public static List<Component> rows(ServerLevel level, BoundWill record, long now) {
		BoundWill.Service service = record.pact().service();
		List<Component> rows = new ArrayList<>();
		rows.add(Component.translatable(KEY + "row.service", Pacts.done(record.pact(), now), service.quota()));
		MaraxEntity marax = GardenPacts.presence(level, record, MaraxEntity.class);
		Memory memory = marax == null ? null : marax.serviceMemory(Memory.class, Memory::new);
		if (memory == null || memory.lastBed() == null) {
			rows.add(Component.translatable(KEY + "row.last_none"));
		} else {
			rows.add(Component.translatable(KEY + "row.last", memory.lastCrop(), memory.lastBed().toShortString(),
					ComposeText.name(new Text.Ref("factor", memory.lastFactor().id))));
		}
		List<BlockPos> proud = memory == null ? List.of() : memory.shown(level, marax.perch());
		if (proud.size() == 1) {
			rows.add(Component.translatable(KEY + "row.proud", proud.getFirst().toShortString(), PIQUE));
		} else if (proud.size() > 1) {
			rows.add(Component.translatable(KEY + "row.proud_more", proud.size(), proud.getFirst().toShortString()));
		}
		return rows;
	}

	/**
	 * What one Marax presence remembers between readings (PactSpiritEntity.serviceMemory: never saved, lost with the
	 * entity): his bed index; the bed, factor and verdict his familiar last named and how many readings running named them;
	 * the proud beds with the verdict each was proud on, oldest first, at most READ_BEDS. Server thread only; each read
	 * touches at most READ_BEDS beds, in loaded chunks only.
	 */
	public static final class Memory {
		private @Nullable GardenReach reach;
		private @Nullable BlockPos lastBed;
		private @Nullable Factor lastFactor;
		private @Nullable Verdict lastVerdict;
		private Component lastCrop = Component.empty();
		private int runs;
		private final LinkedHashMap<BlockPos, Verdict> proud = new LinkedHashMap<>();

		public Memory() {
		}

		/** {@code record}'s bed index, made again when its perch or radius changed (GardenReach.covers). */
		GardenReach reach(BoundWill record) {
			if (reach == null || !reach.covers(record)) {
				reach = GardenReach.of(record);
			}
			return reach;
		}

		/** Each proud bed read again where its chunk is loaded: gone, unplanted, unsampled or its verdict changed, it is forgiven. */
		void judge(ServerLevel level) {
			proud.entrySet().removeIf(entry -> level.isLoaded(entry.getKey()) && !entry.getValue().equals(verdict(level, entry.getKey())));
		}

		/** Whether the familiar will not read the bed at {@code pos} again yet. */
		public boolean proud(BlockPos pos) {
			return proud.containsKey(pos);
		}

		/** A reading named {@code factor} for the bed at {@code pos} on {@code verdict}; at PIQUE such readings running, the bed is proud. */
		void named(BlockPos pos, Factor factor, Verdict verdict, Component crop) {
			BlockPos at = pos.immutable();
			runs = at.equals(lastBed) && factor == lastFactor && verdict.equals(lastVerdict) ? runs + 1 : 1;
			lastBed = at;
			lastFactor = factor;
			lastVerdict = verdict;
			lastCrop = crop;
			if (runs >= PIQUE) {
				proud.remove(at);
				proud.put(at, verdict);
				while (proud.size() > READ_BEDS) {
					Iterator<BlockPos> oldest = proud.keySet().iterator();
					oldest.next();
					oldest.remove();
				}
			}
		}

		public @Nullable BlockPos lastBed() {
			return lastBed;
		}

		public @Nullable Factor lastFactor() {
			return lastFactor;
		}

		public Component lastCrop() {
			return lastCrop;
		}

		/** How many readings running named its last bed and factor on one verdict. */
		public int runs() {
			return runs;
		}

		/** How many beds it holds proud (at most READ_BEDS). */
		public int proudBeds() {
			return proud.size();
		}

		/** Its proud beds that stand now (loaded, their verdict unchanged), nearest {@code perch} first. Reads only. */
		List<BlockPos> shown(ServerLevel level, @Nullable BlockPos perch) {
			List<BlockPos> out = new ArrayList<>();
			for (Map.Entry<BlockPos, Verdict> entry : proud.entrySet()) {
				if (level.isLoaded(entry.getKey()) && entry.getValue().equals(verdict(level, entry.getKey()))) {
					out.add(entry.getKey());
				}
			}
			if (perch != null) {
				out.sort(GardenReach.nearest(perch));
			}
			return out;
		}

		private static @Nullable Verdict verdict(ServerLevel level, BlockPos pos) {
			if (!(level.getBlockEntity(pos) instanceof VerdantBedBlockEntity bed) || bed.isRemoved() || !bed.awake()) {
				return null;
			}
			Organism organism = planted(level, pos);
			Habitat habitat = organism == null ? null : bed.cachedHabitat(level);
			return habitat == null ? null : Verdict.of(bed.niche(organism), habitat);
		}
	}

	/** Habitat reading behind PactService (GC-01): one on-request choice; its rows, refusal, obligation and Jade note. */
	private static final class Service implements PactService {
		@Override
		public String work() {
			return WORK;
		}

		@Override
		public boolean holderOnly() {
			return true;
		}

		/**
		 * Dossier 3 items 2 and 6: he comes only where an awake Verdant Bed the inviter keeps, with an organism crop on it,
		 * lies within his radius of the perch. Read from the loaded chunks' block entities (GardenReach: at most 9 chunks),
		 * loading none and changing nothing.
		 */
		@Override
		public List<Missing> missing(ServerLevel level, ServerPlayer inviter, PactPerchBlockEntity perch, PactDefinition definition) {
			UUID own = inviter.getUUID();
			for (VerdantBedBlockEntity bed : new GardenReach(perch.getBlockPos(), definition.radius(), found -> own.equals(found.keeper()))
					.beds(level, level.getGameTime())) {
				if (planted(level, bed.getBlockPos()) != null) {
					return List.of();
				}
			}
			return List.of(new Missing(NOTHING_GROWS, Component.translatable(KEY + "missing", definition.radius())));
		}

		/** One choice, "Ask Marax for a reading", with the readings left today (ready while any are). */
		@Override
		public List<PactStatePayload.Choice> choices(ServerLevel level, ServerPlayer player, BoundWill record, long now) {
			int left = Math.max(0, record.pact().service().quota() - Pacts.done(record.pact(), now));
			return List.of(new PactStatePayload.Choice(CHOICE, Component.translatable(KEY + "ask"), left, left > 0));
		}

		@Override
		public PactMenu.@Nullable Answered perform(ServerLevel level, ServerPlayer player, AgreementHolder holder, String choice, long time, long now) {
			return CHOICE.equals(choice) ? read(level, player, holder, time, now) : null;
		}

		@Override
		public List<Component> serviceRows(ServerLevel level, ServerPlayer player, BoundWill record, long now) {
			return rows(level, record, now);
		}

		@Override
		public List<Component> obligationRows(ServerLevel level, ServerPlayer player, BoundWill record, long now) {
			return PactObligations.rows(player, record, PactObligations.Act.READ);
		}

		@Override
		public Component suspendedRow(Component name) {
			return Component.translatable("message.jugcraft.concordance.pact.row.state.suspended.serves", name);
		}

		@Override
		public PactObligations.@Nullable Act obligation() {
			return PactObligations.Act.READ;
		}

		/** Jade's note: how many beds his familiar holds proud; none while his pact is held. Reads only; names no player. */
		@Override
		public PactService.@Nullable Note note(ServerLevel level, PactSpiritEntity<?> spirit, BoundWill record, long time, long now) {
			if (!(spirit instanceof MaraxEntity marax) || !Pacts.serving(record)) {
				return null;
			}
			int proud = marax.serviceMemory(Memory.class, Memory::new).shown(level, marax.perch()).size();
			return proud > 0 ? new PactService.Note("proud", proud) : null;
		}
	}
}
