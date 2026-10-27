package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.ComposeText;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Code;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Diagnostic;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.rules.Definitions;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstralClaims;
import io.github.jimbozoomer.jugcraft.concordance.sky.Sky;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactParser;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Concordance Chunk D, D-04 and D-05 (D-S6, D-S8, D-S13, D-S14, D-S15): the pact perch's screen on the server. It has no
 * slots and no button ids: every request is a {@link PactRequestPayload}, answered with a {@link PactStatePayload}. The
 * server drops a request from a stand-in, for a menu that is not its player's open one, from a menu no longer valid (the
 * perch gone, out of reach, the conversation timed out), out of bounds, or not newer than the last answered; asks the
 * rate gate; answers "changed", sending the terms again, a request naming a terms revision other than the one in force;
 * and only then acts, every action through the shared agreement holder's own checks. The revision counts the offer, what
 * accepting binds its reader to: a reload that changes it moves the revision; the perch being taken meanwhile does not, so
 * a second acceptance at one perch is refused as taken (D-S8). A choice indexes the list this menu last sent, which the
 * server keeps; after each action the player's inventory is synced, since a slotless menu syncs nothing of its own.
 * <p>
 * Concordance garden cohort, GC-01 (SG4, SG5, SG10): the perch offers every pact spirit a reader may invite, each through
 * PactSpirits and its work's PactService; this menu names no spirit and no service of its own. An empty perch whose owner
 * may invite two or more first sends the chooser (INVITE mode): INVITE then begins that spirit's visit. Every state names
 * the spirit it is about (its sign and work), and the shared words take that spirit's name.
 */
public class PactMenu extends AbstractContainerMenu {
	public static final int RATE_TICKS = 4;
	public static final int OPEN_TICKS = 10;
	static final String RATE_ACTION = "pact";
	static final String CHOOSE_ACTION = "pact_choose";
	// The reasons the menu itself answers with (tools/concordance_pacts.py ANSWERS); the others are the shared outcomes' ids.
	public static final String CHANGED = "changed";
	public static final String TOO_FAST = "too_fast";
	public static final String NOT_OFFERED = "not_offered";
	public static final String SEALED = "sealed";
	public static final String DECLINED = "declined";
	public static final String DISABLED = "disabled";
	public static final String NO_DEFINITION = "no_definition";
	public static final String MISSING_EVIDENCE = "missing_evidence";
	public static final String NO_OBSERVATION = "no_observation";
	public static final String DEPOSITED = "deposited";
	public static final String NOTHING_DEPOSITED = "nothing_deposited";
	/** GC-01 (SG4): an INVITE answered: the chosen spirit's visit begins (tools/concordance_pacts.py ANSWERS). */
	public static final String INVITED = "invited";

	/** An action's answer: its reason (a stable id), whether it was done, and the words for it. */
	public record Answered(String reason, boolean done, Component said) {
	}

	/** What the client is told when the screen opens: which perch. */
	public record Opening(BlockPos perch) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Opening> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC,
				Opening::perch, Opening::new);
	}

	private record View(int mode, List<Component> beats, List<PactStatePayload.Term> terms, List<Component> notes,
			List<PactStatePayload.Choice> patterns, List<String> patternIds, List<PactStatePayload.Choice> offerings, List<Item> offeringItems,
			List<ItemStack> compact, int resonance, int actions, @Nullable String counterpart, String work) {
		static View closed(List<Component> notes, @Nullable String counterpart, String work) {
			return new View(PactStatePayload.CLOSED, List.of(), List.of(), notes, List.of(), List.of(), List.of(), List.of(), List.of(), 0,
					PactStatePayload.mask(PactRequestPayload.CHOOSE), counterpart, work);
		}
	}

	/** GC-01 (SG4): what an empty perch offers a reader now: one offer, or for its owner with two or more to invite, the chooser's list. */
	private record Offer(@Nullable PactDefinition definition, List<PactDefinition> listed, boolean choosing) {
	}

	public final Opening opening;
	private final @Nullable PactPerchBlockEntity perch;
	private int sequence;
	private int revision;
	private List<PactStatePayload.Term> offer = List.of();
	private List<String> patternIds = List.of();
	private List<Item> offeringItems = List.of();
	private boolean concluded;
	private @Nullable PactStatePayload lastSent;
	/** GC-01 (SG4): the spirit this conversation's reader chose at the chooser (its counterpart); memory only. */
	private @Nullable String chosen;
	/** GC-01: the mode and the work of the state last built: INVITE, RECALL, SEAT and END index the lists it sent. */
	private int lastMode = -1;
	private String lastWork = "";
	/** GC-01: which pact without its perch the seat page shows (CHOOSE's index on that page). */
	private int seatFocus;
	/** GC-01: whose pact this conversation ended (ended_here names that spirit). */
	private @Nullable String concludedWith;

	/** Client side: everything else arrives as PactStatePayload. */
	public PactMenu(int containerId, Inventory inventory, Opening opening) {
		this(containerId, opening, null);
	}

	private PactMenu(int containerId, Opening opening, @Nullable PactPerchBlockEntity perch) {
		super(Workers.PERCH_MENU, containerId);
		this.opening = opening;
		this.perch = perch;
	}

	public static void register() {
		PayloadTypeRegistry.serverboundPlay().register(PactRequestPayload.TYPE, PactRequestPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(PactStatePayload.TYPE, PactStatePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PactRequestPayload.TYPE, (payload, context) -> receive(context.player(), payload));
		// A reload may change a spirit's offer: every open perch screen is sent its state again (a revision change).
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> refreshAll(server));
		PactSessions.register();
	}

	/** The state last sent (server side; tests read it). */
	public @Nullable PactStatePayload lastSent() {
		return lastSent;
	}

	/** The last request sequence this menu took (server side; tests send the next). */
	public int lastSequence() {
		return sequence;
	}

	/** Opens the perch's screen for {@code player} (the block's use, after its rate gate; tests call it directly). */
	public static void open(ServerPlayer player, ServerLevel level, PactPerchBlockEntity perch) {
		if (Authority.isStandIn(player)) {
			return;
		}
		long now = level.getGameTime();
		AgreementHolder holder = perch.holder();
		// Its holder back at their perch: a lost presence is called again, at no cost (D-S7).
		if (holder.verify(level, now) == AgreementHolder.Standing.VALID && holder.agreement().holder().equals(player.getUUID())
				&& holder.spiritLost(level)) {
			holder.recall(level, player, now);
		}
		Opening opening = new Opening(perch.getBlockPos().immutable());
		player.openMenu(new ExtendedMenuProvider<Opening>() {
			@Override
			public Opening getScreenOpeningData(ServerPlayer p) {
				return opening;
			}

			@Override
			public Component getDisplayName() {
				return Component.translatable("container.jugcraft.pact_perch");
			}

			@Override
			public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
				return new PactMenu(containerId, opening, perch);
			}
		});
		if (player.containerMenu instanceof PactMenu menu && menu.perch == perch) {
			menu.begin(level, player, now);
		}
	}

	private void begin(ServerLevel level, ServerPlayer player, long now) {
		PactSessions.open(player, GlobalPos.of(level.dimension(), perch.getBlockPos()), containerId, now);
		Offer offer = offerFor(level, player);
		// GC-01: a visitor comes for the one offer shown (none for the chooser, nor while a pact of theirs waits to be seated).
		if (!offer.choosing() && PactKeeping.strandedPacts(level, player).isEmpty() && invitable(level, player, offer.definition(), now)) {
			perch.welcome(level, player, offer.definition().id());
		}
		// Concordance Chunk D, D-06: it attends while the conversation is open (its listening loop; its sound).
		perch.heard(level);
		answer(level, player, null, 0, now);
	}

	/**
	 * Switched on, the offer loaded and its spirit registered, the perch keeping nothing, its owner (D-S8), its research
	 * understood, nothing its service still asks (Stolas: a star observed; GB2), no pact with it held.
	 */
	private boolean invitable(ServerLevel level, ServerPlayer player, @Nullable PactDefinition definition, long now) {
		PactService service = definition == null ? null : PactServices.get(definition.work());
		return Workers.enabled() && definition != null && service != null && PactSpirits.knows(definition.id())
				&& perch.holder().verify(level, now) == AgreementHolder.Standing.NONE && perch.holder().maySeal(level, player, false)
				&& knows(player, definition) && service.missing(level, player, perch, definition).isEmpty()
				&& !Pacts.holds(BoundWills.of(level.getServer()).ledger(), player.getUUID(), definition.id());
	}

	/** GC-01 (SG4, GC-P10): the offers {@code player} may invite: offered here, research understood, none held; at most MAX_CHOICES, in the perch's order. */
	private static List<PactDefinition> listed(ServerLevel level, ServerPlayer player) {
		List<PactDefinition> out = new ArrayList<>();
		for (PactDefinition definition : PactPerchBlockEntity.offered()) {
			if (knows(player, definition) && !Pacts.holds(BoundWills.of(level.getServer()).ledger(), player.getUUID(), definition.id())) {
				out.add(definition);
				if (out.size() >= PactStatePayload.MAX_CHOICES) {
					break;
				}
			}
		}
		return out;
	}

	/**
	 * GC-01 (SG4, GC-P10): the offer an empty perch shows {@code player}. The one they chose, while it is still offered.
	 * Not its owner: the visitor's offer while one waits (twoNegotiationsAtOnePerchSealOnce), else as below. Its owner with
	 * two or more they may invite: the chooser. Otherwise the one they may invite; with none, the first whose research they
	 * have begun; else the first offered (Chunk D's perch always showed Stolas: with him alone each rule finds him).
	 */
	private Offer offerFor(ServerLevel level, ServerPlayer player) {
		List<PactDefinition> offered = PactPerchBlockEntity.offered();
		if (chosen != null) {
			PactDefinition kept = PactPerchBlockEntity.definition(chosen);
			if (kept != null && offered.contains(kept)) {
				return new Offer(kept, List.of(), false);
			}
		}
		List<PactDefinition> listed = listed(level, player);
		if (!perch.holder().maySeal(level, player, false)) {
			PactSpiritEntity<?> guest = perch.visitor(level);
			PactDefinition visiting = guest == null ? null : PactPerchBlockEntity.definition(guest.counterpart());
			if (visiting != null && offered.contains(visiting)) {
				return new Offer(visiting, List.of(), false);
			}
		} else if (listed.size() >= 2) {
			return new Offer(null, listed, true);
		}
		if (!listed.isEmpty()) {
			return new Offer(listed.getFirst(), List.of(), false);
		}
		for (PactDefinition definition : offered) {
			if (ConcordanceProgress.knowledge(player).state(definition.research()) != ResearchState.NONE) {
				return new Offer(definition, List.of(), false);
			}
		}
		return new Offer(offered.isEmpty() ? null : offered.getFirst(), List.of(), false);
	}

	/** The spirit named when no offer is loaded at all: the first the game ships (Chunk D: Stolas). */
	private static String unavailable() {
		return PactParser.REQUIRED.getFirst();
	}

	static boolean knows(Player player, PactDefinition definition) {
		return ConcordanceProgress.knowledge(player).state(definition.research()).atLeast(ResearchState.UNDERSTOOD);
	}

	static boolean observed(ServerLevel level, Player player) {
		return !AstralClaims.of(level.getServer()).observed(player.getUUID()).isEmpty();
	}

	@Override
	public boolean stillValid(Player player) {
		if (perch == null) {
			return true;
		}
		return !perch.isRemoved() && Container.stillValidBlockEntity(perch, player)
				&& !PactSessions.expired(player.getUUID(), containerId, player.level().getGameTime());
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		return false;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (perch != null) {
			PactSessions.close(player.getUUID(), containerId);
		}
	}

	/** A screen's request (the receiver calls this, and so do tests); null when it is dropped. */
	public static @Nullable PactStatePayload receive(ServerPlayer player, PactRequestPayload request) {
		if (Authority.isStandIn(player) || !(player.containerMenu instanceof PactMenu menu) || menu.containerId != request.containerId()
				|| menu.perch == null || !menu.stillValid(player)) {
			return null;
		}
		if (request.action() < 0 || request.action() >= PactRequestPayload.ACTIONS.size() || request.choice() < 0
				|| request.choice() >= PactStatePayload.MAX_CHOICES || request.revision() < 0 || request.sequence() <= menu.sequence) {
			return null;
		}
		menu.sequence = request.sequence();
		ServerLevel level = player.level();
		long now = level.getGameTime();
		PactSessions.touch(player.getUUID(), menu.containerId, now);
		String gate = request.action() == PactRequestPayload.CHOOSE ? CHOOSE_ACTION : RATE_ACTION;
		if (!RateGate.allow(player, gate, RATE_TICKS)) {
			return menu.answer(level, player, new Answered(TOO_FAST, false, line("answer.too_fast")), request.sequence(), now);
		}
		menu.revise(level, player, now);
		if (request.revision() != menu.revision) {
			return menu.answer(level, player, new Answered(CHANGED, false, line("answer.changed")), request.sequence(), now);
		}
		Answered done = menu.act(level, player, request.action(), request.choice(), now);
		if (done != null) {
			// Concordance Chunk D, D-06: its spirit answers the action with its gesture and its sound, fired here, never replayed.
			menu.perch.react(level, request.action(), done.reason(), done.done());
		}
		player.inventoryMenu.broadcastChanges();
		if (player.containerMenu != menu) {
			// Declined: the menu closed in this call and its client drops a state for a closed menu, so the answer is shown
			// on the action bar instead and the state only returned (the unit review).
			if (done != null) {
				player.sendOverlayMessage(done.said());
			}
			return menu.state(level, player, done, request.sequence(), now);
		}
		return menu.answer(level, player, done, request.sequence(), now);
	}

	/** The action's answer; null for CHOOSE (hearing the terms, or on the seat page reading another pact's: nothing is stored or spent). */
	private @Nullable Answered act(ServerLevel level, ServerPlayer player, int action, int choice, long now) {
		return switch (action) {
			case PactRequestPayload.CHOOSE -> choose(choice);
			case PactRequestPayload.ACCEPT -> accept(level, player, now);
			case PactRequestPayload.DECLINE -> decline(level, player);
			case PactRequestPayload.RECALL -> recall(level, player, choice, now);
			case PactRequestPayload.DEPOSIT -> deposit(level, player, choice, now);
			case PactRequestPayload.SUSPEND -> toggled(PactKeeping.suspend(level, player, perch.holder(), now));
			case PactRequestPayload.RESUME -> toggled(PactKeeping.resume(level, player, perch.holder(), now));
			case PactRequestPayload.END -> end(level, player, choice, now);
			case PactRequestPayload.SEAT -> seat(level, player, choice, now);
			case PactRequestPayload.INVITE -> invite(level, player, choice, now);
			default -> notOffered();
		};
	}

	/** CHOOSE: on the seat page, which pact without its perch to read (GC-01); otherwise only the terms heard. Never an answer. */
	private @Nullable Answered choose(int choice) {
		if (lastMode == PactStatePayload.SEAT && choice < patternIds.size()) {
			seatFocus = choice;
		}
		return null;
	}

	/** Accepting: refused with its first reason before anything is called or spent; the compact paid by the seal itself (D-S13). */
	private Answered accept(ServerLevel level, ServerPlayer player, long now) {
		Offer offer = offerFor(level, player);
		PactDefinition definition = offer.definition();
		if (!Workers.enabled()) {
			return new Answered(DISABLED, false, Component.translatable("message.jugcraft.concordance.disabled"));
		}
		if (offer.choosing()) {
			return notOffered();
		}
		if (definition == null) {
			return new Answered(NO_DEFINITION, false, line("unavailable", PactKeeping.name(unavailable())));
		}
		Component name = PactKeeping.name(definition.id());
		if (!perch.holder().maySeal(level, player, false)) {
			return refused(Pacts.Outcome.NOT_PERMITTED);
		}
		if (!knows(player, definition)) {
			return new Answered(MISSING_EVIDENCE, false, line("answer.missing_evidence", name));
		}
		PactService service = PactServices.get(definition.work());
		if (service == null) {
			return new Answered(NO_DEFINITION, false, line("unavailable", name));
		}
		List<PactService.Missing> missing = service.missing(level, player, perch, definition);
		if (!missing.isEmpty()) {
			return new Answered(missing.getFirst().reason(), false, missing.getFirst().note());
		}
		List<PactCompact.Need> needs = PactCompact.needs(definition);
		if (needs == null) {
			return new Answered(NO_DEFINITION, false, line("unavailable", name));
		}
		AgreementHolder.Sealed sealed = perch.holder().seal(level, player, PactPerchBlockEntity.sealing(definition,
				payer -> PactCompact.affords(payer, needs, definition.resonance()), payer -> PactCompact.pay(payer, needs, definition.resonance())), now);
		return switch (sealed.outcome()) {
			case DONE -> new Answered(SEALED, true, line("answer.sealed", name));
			// GB23: a compact without resonance names no astrolabe (GC-P4).
			case CANNOT_AFFORD -> new Answered(sealed.outcome().id, false, definition.resonance() == 0 ? line("answer.cannot_afford.items")
					: line("answer.cannot_afford", definition.resonance()));
			default -> refused(sealed.outcome());
		};
	}

	private static Answered refused(Pacts.Outcome outcome) {
		return new Answered(outcome.id, false, AgreementHolder.told(outcome));
	}

	private Answered decline(ServerLevel level, ServerPlayer player) {
		Offer offer = offerFor(level, player);
		if (offer.choosing()) {
			return notOffered();
		}
		Component name = PactKeeping.name(offer.definition() == null ? unavailable() : offer.definition().id());
		PactSessions.close(player.getUUID(), containerId);
		player.closeContainer();
		return new Answered(DECLINED, true, line("answer.declined", name));
	}

	/** Its holder pays offerings into the pact's keeping: a server action, never a slot (D-S6, critique 21). */
	private Answered deposit(ServerLevel level, ServerPlayer player, int choice, long now) {
		if (choice >= offeringItems.size()) {
			return notOffered();
		}
		Item kind = offeringItems.get(choice);
		int taken = PactKeeping.deposit(level, player, perch.holder(), kind, now);
		return new Answered(taken > 0 ? DEPOSITED : NOTHING_DEPOSITED, taken > 0, PactKeeping.deposited(level, perch.holder(), kind, taken));
	}

	/** The pact's on-request service (D-05's Star-reckoning; any work's, GC-01): the choice this menu last offered, by its kept pact's own service. */
	private Answered recall(ServerLevel level, ServerPlayer player, int choice, long now) {
		if (lastMode != PactStatePayload.KEEP || choice >= patternIds.size()) {
			return notOffered();
		}
		PactService service = PactServices.get(lastWork);
		PactMenu.Answered done = service == null ? null
				: service.perform(level, player, perch.holder(), patternIds.get(choice), Sky.time(level), now);
		return done == null ? notOffered() : done;
	}

	private static Answered toggled(AgreementHolder.Toggle toggle) {
		return new Answered(toggle.name().toLowerCase(Locale.ROOT),
				toggle == AgreementHolder.Toggle.SUSPENDED || toggle == AgreementHolder.Toggle.RESUMED, AgreementHolder.told(toggle));
	}

	/** Its holder ends the pact kept here, or (at a perch keeping nothing) one of their pacts without its perch (D-S6; GC-01: the one the seat page names). */
	private Answered end(ServerLevel level, ServerPlayer player, int choice, long now) {
		String counterpart = strandedChoice(level, player, choice);
		if (counterpart == null) {
			return notOffered();
		}
		BoundWill kept = perch.holder().agreement() == null ? null : perch.holder().record(level);
		PactKeeping.Ending ending = PactKeeping.end(level, player, perch.holder(), counterpart, now);
		boolean done = ending.ended() == AgreementHolder.Ended.ENDED;
		if (done) {
			concluded = true;
			concludedWith = kept != null && kept.holder().equals(player.getUUID()) ? Pacts.counterpart(kept.counterpart()) : counterpart;
		}
		return new Answered(ending.ended().name().toLowerCase(Locale.ROOT), done, ending.said());
	}

	private Answered seat(ServerLevel level, ServerPlayer player, int choice, long now) {
		String counterpart = strandedChoice(level, player, choice);
		if (counterpart == null) {
			return notOffered();
		}
		AgreementHolder.Seat seat = perch.holder().seat(level, player, counterpart, now);
		return new Answered(seat.id, seat == AgreementHolder.Seat.SEATED, AgreementHolder.told(seat));
	}

	/**
	 * GC-01 (GB4): the counterpart SEAT or END names. On the seat page, the pact at {@code choice} in the list it last sent
	 * (null past it); elsewhere the reader's first pact without its perch, else the pact kept here, else the offer's spirit
	 * (Chunk D passed Stolas, its only spirit, whatever the page).
	 */
	private @Nullable String strandedChoice(ServerLevel level, ServerPlayer player, int choice) {
		if (lastMode == PactStatePayload.SEAT) {
			return choice < patternIds.size() ? patternIds.get(choice) : null;
		}
		List<BoundWill> away = PactKeeping.strandedPacts(level, player);
		if (!away.isEmpty()) {
			return Pacts.counterpart(away.getFirst().counterpart());
		}
		BoundWill kept = perch.holder().agreement() == null ? null : perch.holder().record(level);
		if (kept != null) {
			return Pacts.counterpart(kept.counterpart());
		}
		Offer offer = offerFor(level, player);
		return offer.definition() == null ? unavailable() : offer.definition().id();
	}

	/**
	 * GC-01 (SG4): INVITE, only on the chooser, by its owner: the spirit at {@code choice} in the list it last sent, if they
	 * may still invite it. Its visit begins (its beats, then its terms; it comes now if nothing stands in the way, and
	 * otherwise a visitor of another spirit still waiting here leaves, so no spirit waits on the perch while another's terms
	 * are shown: the unit review). Nothing is stored but this conversation's choice; to choose again, decline and reopen.
	 */
	private Answered invite(ServerLevel level, ServerPlayer player, int choice, long now) {
		if (lastMode != PactStatePayload.INVITE || choice >= patternIds.size() || !perch.holder().maySeal(level, player, false)) {
			return notOffered();
		}
		PactDefinition definition = PactPerchBlockEntity.definition(patternIds.get(choice));
		if (definition == null || !listed(level, player).contains(definition)) {
			return notOffered();
		}
		chosen = definition.id();
		if (invitable(level, player, definition, now)) {
			perch.welcome(level, player, definition.id());
		} else {
			perch.dismiss(level, definition.id());
		}
		perch.heard(level);
		return new Answered(INVITED, true, line("answer.invited", PactKeeping.name(definition.id())));
	}

	private static Answered notOffered() {
		return new Answered(NOT_OFFERED, false, line("answer.not_offered"));
	}

	/** A changed offer moves the revision: the offer the screen is about (GC-01: the kept pact's spirit's, the chooser's list, or the offer shown). */
	private void revise(ServerLevel level, ServerPlayer player, long now) {
		List<PactStatePayload.Term> current = topic(level, player);
		if (revision == 0 || !current.equals(offer)) {
			offer = current;
			revision++;
		}
	}

	/**
	 * What the revision counts (read only: the holder is peeked, never verified). Kept here: its spirit's offer in force
	 * (Chunk D's: Stolas's, whatever the page). Empty here: the chooser's list, or the offer shown, or nothing.
	 */
	private List<PactStatePayload.Term> topic(ServerLevel level, ServerPlayer player) {
		if (perch.holder().peek(level) == AgreementHolder.Standing.VALID) {
			BoundWill record = perch.holder().record(level);
			PactDefinition kept = record == null ? null : PactPerchBlockEntity.definition(record.counterpart());
			return kept == null ? List.of() : offer(kept);
		}
		Offer shown = offerFor(level, player);
		if (shown.choosing()) {
			return shown.listed().stream().map(d -> new PactStatePayload.Term("invite", PactKeeping.name(d.id()))).toList();
		}
		return shown.definition() == null ? List.of() : offer(shown.definition());
	}

	/** What the screen shows now. */
	private View view(ServerLevel level, ServerPlayer player, long now) {
		revise(level, player, now);
		if (!Workers.enabled()) {
			return closed(level, player, List.of(Component.translatable("message.jugcraft.concordance.disabled")));
		}
		AgreementHolder holder = perch.holder();
		AgreementHolder.Standing standing = holder.verify(level, now);
		if (standing == AgreementHolder.Standing.ELSEWHERE) {
			return closed(level, player, List.of(ComposeText.show(Diagnostic.of(Code.CONFLICTING_AGREEMENT).text())));
		}
		if (standing == AgreementHolder.Standing.UNREADABLE) {
			return closed(level, player, List.of(line("reading.unreadable")));
		}
		if (standing == AgreementHolder.Standing.VALID) {
			BoundWill record = holder.record(level);
			return record != null && record.pact() != null && record.holder().equals(player.getUUID()) ? kept(level, player, record, now)
					: closed(level, player, List.of(line("reading.taken")));
		}
		if (concluded) {
			return closed(level, player, List.of(line("ended_here", PactKeeping.name(concludedWith == null ? unavailable() : concludedWith))));
		}
		List<BoundWill> away = PactKeeping.strandedPacts(level, player);
		if (!away.isEmpty()) {
			return seating(level, player, away);
		}
		Offer offer = offerFor(level, player);
		if (offer.choosing()) {
			return choosing(level, player, offer.listed(), now);
		}
		if (offer.definition() == null) {
			return closed(level, player, List.of(line("unavailable", PactKeeping.name(unavailable()))));
		}
		return negotiate(level, player, offer.definition());
	}

	/** A closed page, about the pact kept here, else the one this conversation ended, else the offer an empty perch shows (Chunk D: Stolas). */
	private View closed(ServerLevel level, ServerPlayer player, List<Component> notes) {
		BoundWill kept = perch.holder().agreement() == null ? null : perch.holder().record(level);
		String counterpart = kept != null ? Pacts.counterpart(kept.counterpart()) : concludedWith;
		if (counterpart == null) {
			Offer offer = offerFor(level, player);
			counterpart = offer.definition() == null ? unavailable() : offer.definition().id();
		}
		PactDefinition definition = PactPerchBlockEntity.definition(counterpart);
		BoundWill.Service service = kept == null || kept.pact() == null ? null : kept.pact().service();
		return View.closed(notes, counterpart, service != null ? service.work() : definition == null ? "" : definition.work());
	}

	private View negotiate(ServerLevel level, ServerPlayer player, PactDefinition definition) {
		List<Component> notes = missing(level, player, definition);
		List<PactCompact.Need> needs = PactCompact.needs(definition);
		List<ItemStack> compact = needs == null ? List.of() : needs.stream().map(need -> new ItemStack(need.item(), need.count())).toList();
		int actions = PactStatePayload.mask(PactRequestPayload.CHOOSE, PactRequestPayload.DECLINE)
				| (notes.isEmpty() ? PactStatePayload.mask(PactRequestPayload.ACCEPT) : 0);
		return new View(PactStatePayload.NEGOTIATE, beats(definition), offer, notes, List.of(), List.of(), List.of(), List.of(), compact,
				definition.resonance(), actions, definition.id(), definition.work());
	}

	private View kept(ServerLevel level, ServerPlayer player, BoundWill record, long now) {
		PactKeeping.Page page = PactKeeping.page(level, player, perch.holder(), now);
		BoundWill.Pact pact = record.pact();
		List<PactStatePayload.Choice> patterns = page.choices();
		List<String> ids = patterns.stream().map(PactStatePayload.Choice::id).toList();
		List<PactStatePayload.Choice> offerings = new ArrayList<>();
		boolean room = PactKeeping.room(level, record);
		for (Item item : page.offerings()) {
			offerings.add(new PactStatePayload.Choice(BuiltInRegistries.ITEM.getKey(item).toString(), new ItemStack(item).getHoverName(),
					player.getInventory().countItem(item), room));
		}
		boolean unheld = pact.hold() == BoundWill.Hold.NONE;
		int actions = PactStatePayload.mask(PactRequestPayload.CHOOSE, PactRequestPayload.END)
				| (unheld ? PactStatePayload.mask(PactRequestPayload.SUSPEND) : 0)
				| (pact.hold() == BoundWill.Hold.HOLDER ? PactStatePayload.mask(PactRequestPayload.RESUME) : 0)
				| (!offerings.isEmpty() ? PactStatePayload.mask(PactRequestPayload.DEPOSIT) : 0)
				| (unheld && !patterns.isEmpty() ? PactStatePayload.mask(PactRequestPayload.RECALL) : 0);
		BoundWill.Service service = pact.service();
		return new View(PactStatePayload.KEEP, List.of(), terms(record), page.rows(), patterns, ids, offerings, page.offerings(), List.of(), 0,
				actions, Pacts.counterpart(record.counterpart()), service == null ? "" : service.work());
	}

	/** GB4: the reader's pacts without their perch (at most MAX_CHOICES): the one in focus shown, each listed when there are two or more. */
	private View seating(ServerLevel level, ServerPlayer player, List<BoundWill> away) {
		BoundWill shown = away.get(Math.min(seatFocus, away.size() - 1));
		Component name = PactKeeping.name(shown.counterpart());
		List<Component> notes = new ArrayList<>(List.of(line("seat.offer", name)));
		boolean may = perch.holder().maySeal(level, player, false);
		if (!may) {
			notes.add(line("missing.owner", name));
		}
		List<PactStatePayload.Choice> listed = away.size() < 2 ? List.of() : away.stream()
				.map(record -> new PactStatePayload.Choice(PactSpirits.sign(record.counterpart()), PactKeeping.name(record.counterpart()), 0, may)).toList();
		List<String> ids = away.stream().map(record -> Pacts.counterpart(record.counterpart())).toList();
		int actions = PactStatePayload.mask(PactRequestPayload.CHOOSE, PactRequestPayload.END) | (may ? PactStatePayload.mask(PactRequestPayload.SEAT) : 0);
		BoundWill.Service service = shown.pact() == null ? null : shown.pact().service();
		return new View(PactStatePayload.SEAT, List.of(), terms(shown), notes, listed, ids, List.of(), List.of(), List.of(), 0, actions,
				Pacts.counterpart(shown.counterpart()), service == null ? "" : service.work());
	}

	/**
	 * GC-01 (SG4): the chooser: each spirit its owner may invite (choice id: its sign; amount: what is still missing; ready:
	 * nothing is), and for each not ready its first missing thing as a note, named.
	 */
	private View choosing(ServerLevel level, ServerPlayer player, List<PactDefinition> listed, long now) {
		List<PactStatePayload.Choice> choices = new ArrayList<>();
		List<String> ids = new ArrayList<>();
		List<Component> notes = new ArrayList<>();
		for (PactDefinition definition : listed) {
			PactService service = PactServices.get(definition.work());
			List<PactService.Missing> missing = service == null ? List.of() : service.missing(level, player, perch, definition);
			Component name = PactKeeping.name(definition.id());
			boolean ready = service != null && missing.isEmpty();
			choices.add(new PactStatePayload.Choice(PactSpirits.sign(definition.id()), name, service == null ? 1 : missing.size(), ready));
			ids.add(definition.id());
			if (!ready) {
				notes.add(line("invite.missing", name, service == null ? line("unavailable", name) : missing.getFirst().note()));
			}
		}
		return new View(PactStatePayload.INVITE, List.of(), List.of(), notes, choices, ids, List.of(), List.of(), List.of(), 0,
				PactStatePayload.mask(PactRequestPayload.CHOOSE, PactRequestPayload.INVITE), null, "");
	}

	/** What is missing before the offered spirit will agree, each named (D-04): the owner, prerequisites, each step of its research, its service's own (GB2), a pact already held. */
	private List<Component> missing(ServerLevel level, ServerPlayer player, PactDefinition definition) {
		Component name = PactKeeping.name(definition.id());
		List<Component> notes = new ArrayList<>();
		if (!perch.holder().maySeal(level, player, false)) {
			notes.add(line("missing.owner", name));
		}
		Definitions.Research entry = ConcordanceData.rules().research(definition.research());
		if (entry != null) {
			for (Definitions.Requirement requirement : entry.requires()) {
				if (!ConcordanceProgress.knowledge(player).state(requirement.research()).atLeast(requirement.state())) {
					notes.add(line("missing.prerequisite", ConcordanceProgress.researchName(requirement.research())));
				}
			}
		}
		ResearchState has = ConcordanceProgress.knowledge(player).state(definition.research());
		for (ResearchState step : List.of(ResearchState.ENCOUNTERED, ResearchState.OBSERVED, ResearchState.UNDERSTOOD)) {
			if (!has.atLeast(step)) {
				Identifier advancement = ConcordanceProgress.advancementFor(definition.research(), step);
				notes.add(line("missing.step", ConcordanceProgress.researchName(definition.research()),
						Component.translatable("research_state.jugcraft." + step.id()),
						Component.translatable("advancements." + advancement.getNamespace() + "." + advancement.getPath() + ".description")));
			}
		}
		PactService service = PactServices.get(definition.work());
		if (service == null) {
			notes.add(line("unavailable", name));
		} else {
			for (PactService.Missing missing : service.missing(level, player, perch, definition)) {
				notes.add(missing.note());
			}
		}
		if (Pacts.holds(BoundWills.of(level.getServer()).ledger(), player.getUUID(), definition.id())) {
			notes.add(line("missing.held", name));
		}
		return notes;
	}

	/** What accepting {@code d} binds its reader to (D-S15): every term, the compact's cost in words. */
	static List<PactStatePayload.Term> offer(PactDefinition d) {
		List<PactStatePayload.Term> terms = new ArrayList<>(service(d.work(), d.radius(), d.from(), d.to(), d.quota()));
		terms.addAll(upkeep(d.offering(), d.count(), d.period(), d.grace(), d.cap()));
		terms.addAll(standing(d.beneficiaries()));
		Component items = ComponentUtils.formatList(d.compact(), cost -> line("term.compact_item", cost.count(), itemName(cost.item())));
		// GB23: a compact without resonance names no astrolabe (GC-P4).
		terms.add(new PactStatePayload.Term("compact", d.resonance() == 0 ? line("term.compact.items", items) : line("term.compact", d.resonance(), items)));
		return terms;
	}

	/** A sealed pact's terms, as copied onto its record at sealing (D-S23): a reload never changes them. */
	static List<PactStatePayload.Term> terms(BoundWill record) {
		BoundWill.Pact pact = record.pact();
		List<PactStatePayload.Term> terms = new ArrayList<>();
		BoundWill.Service service = pact.service();
		if (service != null) {
			terms.addAll(service(service.work(), service.radius(), service.from(), service.to(), service.quota()));
		}
		BoundWill.Upkeep upkeep = pact.upkeep();
		if (upkeep != null) {
			terms.addAll(upkeep(upkeep.offering(), upkeep.count(), upkeep.period(), upkeep.grace(), upkeep.cap()));
		}
		terms.addAll(standing(pact.beneficiaries()));
		terms.add(new PactStatePayload.Term("compact", line("term.compact_paid")));
		return terms;
	}

	private static List<PactStatePayload.Term> service(String work, int radius, int from, int to, int quota) {
		return List.of(new PactStatePayload.Term("service", line("term.service." + work)),
				new PactStatePayload.Term("hours", from == 0 && to == 24000 ? line("term.hours.any") : line("term.hours", Workers.clock(from), Workers.clock(to))),
				new PactStatePayload.Term("area", line("term.area", radius)), new PactStatePayload.Term("quota", line("term.quota", quota)));
	}

	private static List<PactStatePayload.Term> upkeep(String offering, int count, long period, long grace, int cap) {
		return List.of(new PactStatePayload.Term("upkeep", line("term.upkeep", count, AgreementHolder.offeringName(offering), period / Pacts.DAY, cap)),
				new PactStatePayload.Term("grace", line("term.grace", grace / Pacts.DAY)));
	}

	private static List<PactStatePayload.Term> standing(String beneficiaries) {
		List<PactStatePayload.Term> terms = new ArrayList<>();
		for (String key : List.of("suspension", "duration", "breach", "ending", "returns", "perch")) {
			terms.add(new PactStatePayload.Term(key, line("term." + key)));
		}
		terms.add(new PactStatePayload.Term("beneficiaries", line("term.beneficiaries." + beneficiaries)));
		return terms;
	}

	private static List<Component> beats(PactDefinition d) {
		String name = d.id().substring(d.id().indexOf(':') + 1);
		return d.beats().stream().map(beat -> line(name + ".beat." + beat)).toList();
	}

	private static Component itemName(String id) {
		Identifier key = Identifier.tryParse(id);
		Item item = key == null ? null : BuiltInRegistries.ITEM.getOptional(key).orElse(null);
		return item == null ? Component.translatableWithFallback("item." + id.replace(':', '.'), id) : new ItemStack(item).getHoverName();
	}

	private PactStatePayload answer(ServerLevel level, ServerPlayer player, @Nullable Answered answered, int answering, long now) {
		PactStatePayload state = state(level, player, answered, answering, now);
		lastSent = state;
		if (ServerPlayNetworking.canSend(player, PactStatePayload.TYPE)) {
			ServerPlayNetworking.send(player, state);
		}
		return state;
	}

	/** The state this menu shows {@code player} now, answering request {@code answering}: built, not sent. */
	private PactStatePayload state(ServerLevel level, ServerPlayer player, @Nullable Answered answered, int answering, long now) {
		View view = view(level, player, now);
		patternIds = view.patternIds();
		offeringItems = view.offeringItems();
		lastMode = view.mode();
		lastWork = view.work();
		// GC-01 (SG5): the speaker is the spirit the page is about (Chunk D: Stolas always), the perch itself for the chooser.
		Component speaker = view.counterpart() == null ? Component.translatable("container.jugcraft.pact_perch") : PactKeeping.name(view.counterpart());
		PactStatePayload.Spirit spirit = view.counterpart() == null ? PactStatePayload.Spirit.NONE
				: new PactStatePayload.Spirit(PactSpirits.sign(view.counterpart()), view.work());
		return new PactStatePayload(containerId, revision, view.mode(), speaker, view.beats(), view.terms(), view.notes(), view.patterns(),
				view.offerings(), view.compact(), view.resonance(), view.actions(),
				Optional.ofNullable(answered).map(done -> new PactStatePayload.Result(answering, done.reason(), done.done(), done.said())), spirit);
	}

	private static void refreshAll(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.containerMenu instanceof PactMenu menu && menu.perch != null && menu.stillValid(player)) {
				menu.answer(player.level(), player, null, menu.sequence, player.level().getGameTime());
			}
		}
	}

	private static Component line(String key, Object... args) {
		return Component.translatable("message.jugcraft.concordance.pact." + key, args);
	}
}
