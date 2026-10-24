package io.github.jimbozoomer.jugcraft.concordance.spirits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.ComposeText;
import io.github.jimbozoomer.jugcraft.concordance.courier.Couriers;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Code;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Diagnostic;
import io.github.jimbozoomer.jugcraft.concordance.logistics.Place;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWillLedger;
import io.github.jimbozoomer.jugcraft.concordance.worker.Agreement;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import io.github.jimbozoomer.jugcraft.concordance.worker.WorkerDefinition;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Concordance Chunk D, unit 1 (D-01): what a block that keeps an agreement shares, extracted from the Spirit Anchor
 * (critique 27) for the anchor now and D-04's pact perch. It keeps who placed the block, the agreement's terms as sealed
 * here and the presence serving it; the world's Bound Will says whether the agreement stands. A block counts its terms
 * only while its record is kept for the same holder: otherwise it is inert, says CONFLICTING_AGREEMENT and releases
 * nothing. Sealing asks the owner's side and Authority, never a stand-in (D-S8), is refused with one reason before
 * anything is recorded, spent or called (D-S11), spends its cost only once sealed (D-S13), and notes the presence on its
 * holder's roster at once (G5). A lost presence is called again under the same record at no cost (D-S18). Only its living
 * holder, in person and with the Concordance on, ends it, once; breaking the block with the Concordance on releases it
 * (D-S16). No item lives here.
 */
public final class AgreementHolder {
	/** The terms as sealed here (moved unchanged from SpiritAnchorBlockEntity, so an anchor's save reads as before). */
	public static final Codec<Agreement> AGREEMENT_CODEC = RecordCodecBuilder.create(i -> i.group(
			UUIDUtil.CODEC.fieldOf("record").forGetter(Agreement::record), UUIDUtil.CODEC.fieldOf("holder").forGetter(Agreement::holder),
			Codec.STRING.fieldOf("work").forGetter(Agreement::work), Codec.STRING.fieldOf("dimension").forGetter(Agreement::dimension),
			Codec.INT.fieldOf("x").forGetter(Agreement::x), Codec.INT.fieldOf("y").forGetter(Agreement::y), Codec.INT.fieldOf("z").forGetter(Agreement::z),
			Codec.INT.fieldOf("radius").forGetter(Agreement::radius), Codec.INT.fieldOf("from").forGetter(Agreement::from),
			Codec.INT.fieldOf("to").forGetter(Agreement::to), Codec.INT.fieldOf("quota").forGetter(Agreement::quota),
			Codec.LONG.fieldOf("window").forGetter(Agreement::window), Codec.INT.fieldOf("done_today").forGetter(Agreement::doneToday),
			Codec.BOOL.fieldOf("suspended").forGetter(Agreement::suspended)).apply(i, Agreement::new));

	/** How a block's terms stand against the world's Bound Wills (D-S9). */
	public enum Standing {
		/** It keeps no terms. */
		NONE,
		/** Its record is kept, for the same holder. */
		VALID,
		/** Its record is gone or kept for someone else: inert. */
		ELSEWHERE
	}

	public enum Recall {
		RECALLED, NOTHING, NOT_HOLDER, NOT_LOST, CONFLICTING, ROSTER_FULL, UNAVAILABLE, DISABLED
	}

	public enum Toggle {
		SUSPENDED, RESUMED, NOTHING, NOT_HOLDER, CONFLICTING, DISABLED
	}

	public enum Ended {
		ENDED, NOTHING, NOT_HOLDER, NOT_PERMITTED, CONFLICTING, DISABLED
	}

	/** A block entity that keeps an agreement through a holder. */
	public interface Holds {
		AgreementHolder holder();

		/**
		 * Calls this place's presence for {@code who}: the counterpart {@code counterpart} (namespaced), added to the level and
		 * on no roster yet; null if none came, or if this place does not call that counterpart.
		 */
		@Nullable WorkerEntity<?> call(ServerLevel level, UUID who, String counterpart);
	}

	/**
	 * A presence serving an agreement at a place. Only {@link WorkerEntity} subclasses implement it, so every presence has
	 * a roster entry its holder's roster can strike (critique 8).
	 */
	public interface Presence {
		@Nullable BlockPos seat();

		/** Its agreement is over or answered by another: it leaves what it carried at {@code home} (else at {@code at}), once, and departs. */
		void leave(ServerLevel level, @Nullable Holds home, BlockPos at);
	}

	/** The service terms copied into the block's Agreement at sealing. */
	public record Service(String work, int radius, int from, int to, int quota) {
		public static Service of(WorkerDefinition.Spirit terms) {
			return new Service(terms.work(), terms.radius(), terms.from(), terms.to(), terms.quota());
		}
	}

	/**
	 * What a block seals: its agreement and counterpart ids, its service; whether a holder keeps one with this counterpart
	 * at most; whether the owner's party may seal here as well as the owner (the anchor: yes; D-04's perch: no, D-S8); and
	 * its cost, if any: asked before anything is called, paid in the same call once sealed (D-S13).
	 */
	public record Sealing(String agreement, String counterpart, Service service, boolean onePerHolder, boolean partySeals,
			@Nullable Predicate<ServerPlayer> affords, @Nullable Consumer<ServerPlayer> pay) {
	}

	public record Sealed(Pacts.Outcome outcome, @Nullable WorkerEntity<?> presence) {
	}

	private final BlockEntity block;
	private final Holds holds;
	private @Nullable UUID owner;
	private @Nullable Agreement agreement;
	private @Nullable UUID spirit;

	public <B extends BlockEntity & Holds> AgreementHolder(B block) {
		this.block = block;
		this.holds = block;
	}

	public @Nullable UUID owner() {
		return owner;
	}

	/** Who placed the block (D-S8): they or their party seal here. Null for a block placed before owners were recorded. */
	public void setOwner(@Nullable UUID next) {
		owner = next;
		block.setChanged();
	}

	public @Nullable Agreement agreement() {
		return agreement;
	}

	public @Nullable UUID spirit() {
		return spirit;
	}

	public Place place(ServerLevel level) {
		return Couriers.place(level, block.getBlockPos());
	}

	/** The block forgets its terms (its record is untouched). */
	public void clear() {
		agreement = null;
		spirit = null;
		block.setChanged();
	}

	/** The presence's own record of a finished task (the block keeps the day's count). */
	public void taskDone(long gameTime) {
		if (agreement != null) {
			agreement = agreement.didTask(gameTime);
			block.setChanged();
		}
	}

	/**
	 * Whether {@code player} may seal here (D-S8): never a stand-in, whoever's identity it carries; only where they could
	 * change the block (Authority.mayChange: the world, spawn protection, towns, claims); and only its owner, or their party
	 * where {@code partySeals} (the Courier Post's rule, CourierPostBlockEntity.mayUse), unless it records no owner (placed
	 * before Chunk D).
	 */
	public boolean maySeal(ServerLevel level, ServerPlayer player, boolean partySeals) {
		if (Authority.isStandIn(player) || !Authority.mayChange(level, player, block.getBlockPos())) {
			return false;
		}
		return owner == null || owner.equals(player.getUUID()) || partySeals && JugcraftParties.sameParty(owner, player.getUUID());
	}

	/**
	 * Seals {@code sealing} for {@code player}: DONE, or the one reason it was refused. A refusal records, spends and calls
	 * nothing; the roster and the cost are asked before anything is called (D-S11, D-S13), and the cost is paid only once
	 * the Bound Will is recorded.
	 */
	public Sealed seal(ServerLevel level, ServerPlayer player, Sealing sealing, long now) {
		if (!Workers.enabled()) {
			return new Sealed(Pacts.Outcome.DISABLED, null);
		}
		verify(level, now);
		MinecraftServer server = level.getServer();
		BoundWills wills = BoundWills.of(server);
		UUID holder = player.getUUID();
		Pacts.Outcome refused = Pacts.refusal(maySeal(level, player, sealing.partySeals()), agreement != null,
				sealing.onePerHolder() && Pacts.holds(wills.ledger(), holder, sealing.counterpart()), wills.ledger().heldBy(holder).size(),
				WorkerRoster.of(server).full(holder));
		if (refused != Pacts.Outcome.DONE) {
			return new Sealed(refused, null);
		}
		if (sealing.affords() != null && !sealing.affords().test(player)) {
			return new Sealed(Pacts.Outcome.CANNOT_AFFORD, null);
		}
		WorkerEntity<?> presence = holds.call(level, holder, Pacts.counterpart(sealing.counterpart()));
		if (presence == null) {
			return new Sealed(Pacts.Outcome.UNAVAILABLE, null);
		}
		BoundWill record = new BoundWill(UUID.randomUUID(), sealing.agreement(), sealing.counterpart(), holder, now, false);
		if (wills.seal(record).outcome() != BoundWillLedger.Outcome.DONE) {
			presence.discard();
			return new Sealed(Pacts.Outcome.UNAVAILABLE, null);
		}
		if (sealing.pay() != null) {
			sealing.pay().accept(player);
		}
		BlockPos pos = block.getBlockPos();
		Service service = sealing.service();
		agreement = new Agreement(record.id(), holder, service.work(), place(level).dimension(), pos.getX(), pos.getY(), pos.getZ(),
				service.radius(), service.from(), service.to(), service.quota(), Long.MIN_VALUE, 0, false);
		spirit = presence.getUUID();
		block.setChanged();
		note(level, presence);
		return new Sealed(Pacts.Outcome.DONE, presence);
	}

	/** A presence noted on its holder's roster at once (G5). */
	private static void note(ServerLevel level, WorkerEntity<?> presence) {
		UUID holder = presence.owner();
		if (holder != null) {
			WorkerRoster.of(level.getServer()).note(holder, presence.getUUID(), presence.kind(), presence.status(),
					level.dimension().identifier().toString(), presence.blockPosition());
		}
	}

	/**
	 * Whether this block's terms count (D-S9): its Bound Will kept, for the same holder. Until the record knows its place
	 * (D-02), a record gone or held by someone else leaves the block inert.
	 */
	public Standing verify(ServerLevel level, long now) {
		if (agreement == null) {
			return Standing.NONE;
		}
		BoundWill record = BoundWills.of(level.getServer()).ledger().records().get(agreement.record());
		return record != null && record.holder().equals(agreement.holder()) ? Standing.VALID : Standing.ELSEWHERE;
	}

	/** Why its service is held now (SUSPENDED), or null. */
	public @Nullable Status held(ServerLevel level) {
		return agreement != null && agreement.suspended() ? Status.SUSPENDED : null;
	}

	/** Its state for a reading: held, or null when simply active. */
	public @Nullable Status state(ServerLevel level) {
		return held(level);
	}

	/**
	 * Whether its presence is lost (D-S18): not loaded and not removed here, and struck off its holder's roster (a killed
	 * shade strikes itself off). One that is only unloaded keeps its roster entry and is not lost.
	 */
	public boolean spiritLost(ServerLevel level) {
		if (agreement == null) {
			return false;
		}
		if (spirit == null) {
			return true;
		}
		if (level.getEntity(spirit) instanceof WorkerEntity<?> worker && !worker.isRemoved()) {
			return false;
		}
		return !WorkerRoster.of(level.getServer()).workers(agreement.holder()).containsKey(spirit);
	}

	/** Its holder calls another presence under the same record, at no cost, when theirs is lost (D-S18); refused when their roster is full. */
	public Recall recall(ServerLevel level, ServerPlayer actor, long now) {
		if (!Workers.enabled()) {
			return Recall.DISABLED;
		}
		Standing standing = verify(level, now);
		if (standing == Standing.NONE) {
			return Recall.NOTHING;
		}
		if (standing != Standing.VALID) {
			return Recall.CONFLICTING;
		}
		if (Authority.isStandIn(actor) || !agreement.holder().equals(actor.getUUID())) {
			return Recall.NOT_HOLDER;
		}
		if (!spiritLost(level)) {
			return Recall.NOT_LOST;
		}
		if (WorkerRoster.of(level.getServer()).full(actor.getUUID())) {
			return Recall.ROSTER_FULL;
		}
		BoundWill record = BoundWills.of(level.getServer()).ledger().records().get(agreement.record());
		WorkerEntity<?> presence = holds.call(level, actor.getUUID(), Pacts.counterpart(record.counterpart()));
		if (presence == null) {
			return Recall.UNAVAILABLE;
		}
		spirit = presence.getUUID();
		block.setChanged();
		note(level, presence);
		return Recall.RECALLED;
	}

	/** Its holder suspends or resumes it. */
	public Toggle toggle(ServerLevel level, ServerPlayer player, long now) {
		if (!Workers.enabled()) {
			return Toggle.DISABLED;
		}
		Standing standing = verify(level, now);
		if (standing == Standing.NONE) {
			return Toggle.NOTHING;
		}
		if (standing != Standing.VALID) {
			return Toggle.CONFLICTING;
		}
		if (Authority.isStandIn(player) || !agreement.holder().equals(player.getUUID())) {
			return Toggle.NOT_HOLDER;
		}
		agreement = agreement.suspended(!agreement.suspended());
		block.setChanged();
		return agreement.suspended() ? Toggle.SUSPENDED : Toggle.RESUMED;
	}

	/** Its holder ends the agreement kept here, without breaking the block (G6). */
	public Ended end(ServerLevel level, ServerPlayer player, long now) {
		if (!Workers.enabled()) {
			return Ended.DISABLED;
		}
		Standing standing = verify(level, now);
		if (standing == Standing.NONE) {
			return Ended.NOTHING;
		}
		if (standing != Standing.VALID) {
			return Ended.CONFLICTING;
		}
		return end(level, holds, player, agreement.record(), Pacts.ENDED, now);
	}

	/**
	 * Its holder ends record {@code id} (here, or with {@code home} null where no block keeps it): only with the Concordance
	 * on, never a stand-in, only a living holder; the record goes once and its presence leaves, its load left at its place.
	 * A second end finds nothing.
	 */
	public static Ended end(ServerLevel level, @Nullable Holds home, ServerPlayer player, UUID id, String reason, long now) {
		if (!Workers.enabled()) {
			return Ended.DISABLED;
		}
		if (Authority.isStandIn(player) || !player.isAlive()) {
			return Ended.NOT_PERMITTED;
		}
		BoundWillLedger.Change released = BoundWills.of(level.getServer()).release(id, player.getUUID());
		if (released.outcome() != BoundWillLedger.Outcome.DONE) {
			return released.outcome() == BoundWillLedger.Outcome.NOT_HOLDER ? Ended.NOT_HOLDER : Ended.NOTHING;
		}
		departed(level, home, id, player.getUUID());
		return Ended.ENDED;
	}

	/**
	 * Its block is broken (preRemoveSideEffects): with the Concordance on (D-S16), an agreement whose terms count here is
	 * released once and its presence leaves its load here; one that does not count releases nothing.
	 */
	public void broken(ServerLevel level, long now) {
		if (agreement != null && Workers.enabled() && verify(level, now) == Standing.VALID) {
			UUID id = agreement.record();
			UUID holder = agreement.holder();
			if (BoundWills.of(level.getServer()).release(id, holder).outcome() == BoundWillLedger.Outcome.DONE) {
				departed(level, holds, id, holder);
			}
		}
		agreement = null;
		spirit = null;
	}

	/** Record {@code id} was released: the block keeping it lets its presence go (off the roster, leaving its load there) and forgets it. */
	private static void departed(ServerLevel level, @Nullable Holds home, UUID id, UUID holder) {
		if (home == null || home.holder().agreement == null || !id.equals(home.holder().agreement.record())) {
			return;
		}
		AgreementHolder kept = home.holder();
		if (kept.spirit != null) {
			WorkerRoster.of(level.getServer()).remove(holder, kept.spirit);
			if (level.getEntity(kept.spirit) instanceof Presence leaving) {
				leaving.leave(level, home, kept.block.getBlockPos());
			}
		}
		kept.clear();
	}

	private static Component conflicting() {
		return ComposeText.show(Diagnostic.of(Code.CONFLICTING_AGREEMENT).text());
	}

	/** What a seal tells its player, a refusal its one reason (G4). */
	public static Component told(Pacts.Outcome outcome) {
		return switch (outcome) {
			case DONE -> Component.translatable("message.jugcraft.concordance.workers.seal_done");
			case DISABLED -> Component.translatable("message.jugcraft.concordance.disabled");
			case NOT_PERMITTED -> Component.translatable("message.jugcraft.concordance.workers.seal_not_permitted");
			case TAKEN -> Component.translatable("message.jugcraft.concordance.workers.seal_taken");
			case ALREADY_HELD -> Component.translatable("message.jugcraft.concordance.workers.seal_already_held");
			case HOLDER_FULL -> Component.translatable("message.jugcraft.concordance.workers.seal_holder_full", BoundWillLedger.MAX_PER_HOLDER);
			case ROSTER_FULL -> Component.translatable("message.jugcraft.concordance.workers.seal_roster_full", WorkerRoster.MAX_PER_PLAYER);
			case UNAVAILABLE -> Component.translatable("message.jugcraft.concordance.workers.seal_unavailable");
			case CANNOT_AFFORD -> Component.translatable("message.jugcraft.concordance.workers.seal_cannot_afford");
		};
	}

	public static Component told(Recall recall) {
		return switch (recall) {
			case RECALLED -> Component.translatable("message.jugcraft.concordance.workers.spirit_recalled");
			case NOTHING -> Component.translatable("message.jugcraft.concordance.workers.no_agreement");
			case NOT_HOLDER -> Component.translatable("message.jugcraft.concordance.workers.not_holder");
			case NOT_LOST -> Component.translatable("message.jugcraft.concordance.workers.spirit_away");
			case CONFLICTING -> conflicting();
			case ROSTER_FULL -> Component.translatable("message.jugcraft.concordance.workers.recall_roster_full", WorkerRoster.MAX_PER_PLAYER);
			case UNAVAILABLE -> Component.translatable("message.jugcraft.concordance.workers.seal_unavailable");
			case DISABLED -> Component.translatable("message.jugcraft.concordance.disabled");
		};
	}

	public static Component told(Toggle toggle) {
		return switch (toggle) {
			case SUSPENDED -> Component.translatable("message.jugcraft.concordance.workers.suspended");
			case RESUMED -> Component.translatable("message.jugcraft.concordance.workers.resumed");
			case NOTHING -> Component.translatable("message.jugcraft.concordance.workers.no_agreement");
			case NOT_HOLDER -> Component.translatable("message.jugcraft.concordance.workers.not_holder");
			case CONFLICTING -> conflicting();
			case DISABLED -> Component.translatable("message.jugcraft.concordance.disabled");
		};
	}

	public static Component told(Ended ended) {
		return switch (ended) {
			case ENDED -> Component.translatable("message.jugcraft.concordance.workers.ended");
			case NOTHING -> Component.translatable("message.jugcraft.concordance.workers.no_agreement");
			case NOT_HOLDER -> Component.translatable("message.jugcraft.concordance.workers.not_holder");
			case NOT_PERMITTED -> Component.translatable("message.jugcraft.concordance.workers.end_not_permitted");
			case CONFLICTING -> conflicting();
			case DISABLED -> Component.translatable("message.jugcraft.concordance.disabled");
		};
	}

	public void load(ValueInput input) {
		owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
		agreement = input.read("agreement", AGREEMENT_CODEC).orElse(null);
		spirit = input.read("spirit", UUIDUtil.CODEC).orElse(null);
	}

	public void save(ValueOutput output) {
		if (owner != null) {
			output.store("owner", UUIDUtil.CODEC, owner);
		}
		if (agreement != null) {
			output.store("agreement", AGREEMENT_CODEC, agreement);
		}
		if (spirit != null) {
			output.store("spirit", UUIDUtil.CODEC, spirit);
		}
	}
}
