package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.garden.OrganismCropBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.spirits.AgreementHolder;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BoundWills;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BuerEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ForasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenPacts;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenReach;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GraftKeeping;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactAccords;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactHistory;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactKeeping;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactMenu;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactPerchBlock;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactPerchBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StrainTending;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerRoster;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import io.github.jimbozoomer.jugcraft.finance.FinanceBook;
import io.github.jimbozoomer.jugcraft.finance.FinanceService;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.finance.Ledger;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;
import io.github.jimbozoomer.jugcraft.party.PartyManager;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/**
 * The garden pacts' review (10 November 2026; docs/features/concordance-garden-cohort.md, "The garden pacts' review"): a
 * garden pact's whole lifecycle, each step in one test, on the shared helpers of ConcordanceWorkerGameTests. Buer stands for
 * the six: binding him at the perch's own screen and the benefit that follows; his upkeep left unpaid until his service
 * stops, and paid again; his pact ended; a stranger and a party member refused every holder's action; a real land claim
 * (LandRegistry, the repo's own claims, not a test listener) keeping him off a neighbour's ground; his pact, his perch and
 * his body through a save and a reload with nothing duplicated. Foras's ripe host after a reload is the review's gameplay
 * fix. Time is passed explicitly (each think's world time and game time), as the cohort's tests pass it; nothing waits on
 * the clock except Foras's reading, which waits for the level's sampling allowance.
 */
public class ConcordanceGardenLifecycleGameTests {
	/** World time at noon (Buer works by day, 0 to 12000) and Foras at any hour. */
	private static final long NOON = 6000L;
	/** A neighbour's land, in the real claim registry (no player behind it). */
	private static final UUID NEIGHBOUR = new UUID(0x6C10L, 0x1AL);

	/** How many of {@code offering} {@code player} carries. */
	private static int offerings(ServerPlayer player, Item offering) {
		return player.getInventory().countItem(offering);
	}

	/**
	 * Bind and benefit: an ordinary survival player with The Garden's Regard understood and Buer's compact invites him at a perch
	 * they placed, through its own screen; accepting binds him once (one Bound Will, its terms copied, the compact spent, his
	 * owner the holder, one roster entry), and by day he eases a strained bed of theirs, moving nothing else.
	 */
	@GameTest(maxTicks = 20)
	public void bindingBuerAtThePerchEasesTheHoldersBed(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PactDefinition d = ConcordanceWorkerGameTests.buerOffer();
		BlockPos at = new BlockPos(2, 1, 2);
		ServerPlayer keeper = ConcordanceWorkerGameTests.invitee(helper, d, at, 1);
		VerdantBedBlockEntity bed = ConcordanceWorkerGameTests.keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		helper.assertTrue(BoundWills.of(level.getServer()).ledger().heldBy(keeper.getUUID()).isEmpty() && ConcordanceWorkerGameTests.pocketed(keeper, 1, d),
				"Before: no pact, one compact carried");
		ConcordanceWorkerGameTests.sealThrough(helper, keeper, d, at);
		keeper.closeContainer();
		PactPerchBlockEntity perch = helper.getBlockEntity(at, PactPerchBlockEntity.class);
		BoundWill record = ConcordanceWorkerGameTests.pactOf(level, perch);
		BuerEntity buer = ConcordanceWorkerGameTests.buer(helper, perch);
		helper.assertTrue(ConcordanceWorkerGameTests.pocketed(keeper, 0, d) && keeper.getUUID().equals(record.holder())
				&& d.id().equals(Pacts.counterpart(record.counterpart())) && Pacts.serving(record) && buer.getUUID().equals(record.pact().presence())
				&& BoundWills.of(level.getServer()).ledger().heldBy(keeper.getUUID()).size() == 1,
				"Bound once: one serving record with Buer as its presence, the compact spent: " + record);
		helper.assertTrue(keeper.getUUID().equals(buer.owner()) && keeper.getUUID().equals(perch.holder().owner())
				&& WorkerRoster.of(level.getServer()).workers(keeper.getUUID()).keySet().equals(Set.of(buer.getUUID()))
				&& ConcordanceWorkerGameTests.spiritsOf(helper, d).size() == 1, "Buer is the holder's, on their roster, the only one here");
		long now = ConcordanceWorkerGameTests.dayStart(level, 1);
		int nutrients = bed.nutrients();
		helper.assertValueEqual(buer.think(level, NOON, now), Status.WORKING, "By day he eases the holder's strained bed");
		helper.assertTrue(bed.strain() == 2 && bed.nutrients() == nutrients && Pacts.done(ConcordanceWorkerGameTests.pactOf(level, perch).pact(), now) == 1,
				"The benefit: one strain eased, 3 to 2, nothing else moved, counted once on the record");
		helper.succeed();
	}

	/**
	 * Upkeep exhausted: one offering paid ahead is taken when it falls due; the next, unpaid, is warned and he works on through his
	 * grace; past it the pact is held for its upkeep and he tends nothing, nothing more accrues and nothing is taken or ended; an
	 * offering paid then resumes it with a whole period from that moment, and he works again.
	 */
	@GameTest(maxTicks = 20)
	public void anUnpaidUpkeepStopsTheServiceUntilItIsPaid(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PactDefinition d = ConcordanceWorkerGameTests.buerOffer();
		ServerPlayer keeper = ConcordanceWorkerGameTests.gardenHolder(helper);
		long g0 = ConcordanceWorkerGameTests.dayStart(level, 1);
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = ConcordanceWorkerGameTests.buerPact(helper, keeper, at, g0);
		BuerEntity buer = ConcordanceWorkerGameTests.buer(helper, perch);
		VerdantBedBlockEntity bed = ConcordanceWorkerGameTests.keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		UUID id = perch.holder().agreement().record();
		Item offering = ConcordanceWorkerGameTests.offeringOf(d);
		helper.assertValueEqual(buer.think(level, NOON, g0), Status.WORKING, "Sealed, he works");
		ConcordanceWorkerGameTests.give(keeper, new ItemStack(offering, d.count()));
		helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), offering, g0), d.count(), "One period paid ahead");
		long due1 = ConcordanceWorkerGameTests.pactOf(level, perch).pact().upkeep().due();
		helper.assertTrue(due1 == g0 + d.period() && ConcordanceWorkerGameTests.stay(level, perch.holder(), g0 + 1_000L, due1 - 1_000L).isEmpty()
				&& perch.holder().look(level, due1) == Pacts.Event.PAID && ConcordanceWorkerGameTests.escrowed(level, id) == 0
				&& ConcordanceWorkerGameTests.pactOf(level, perch).pact().upkeep().due() == due1 + d.period(),
				"Due a period on, the offering paid ahead is taken; the next falls a period later");
		long due2 = due1 + d.period();
		helper.assertTrue(ConcordanceWorkerGameTests.stay(level, perch.holder(), due1 + 1_000L, due2 - 1_000L).isEmpty()
				&& perch.holder().look(level, due2) == Pacts.Event.WARNED && Pacts.status(ConcordanceWorkerGameTests.pactOf(level, perch)) == Status.UPKEEP_DUE,
				"Due again with nothing paid ahead: warned, his grace begins");
		helper.assertValueEqual(buer.think(level, NOON, due2 + 20), Status.UPKEEP_DUE, "Through his grace he works on, saying an offering is due");
		helper.assertValueEqual(bed.strain(), 1, "one strain eased in the grace");
		helper.assertTrue(ConcordanceWorkerGameTests.stay(level, perch.holder(), due2 + 1_000L, due2 + d.grace() - 1_000L).isEmpty()
				&& perch.holder().look(level, due2 + d.grace()) == Pacts.Event.SUSPENDED
				&& ConcordanceWorkerGameTests.pactOf(level, perch).pact().hold() == BoundWill.Hold.UPKEEP,
				"Unpaid past his grace: held for its upkeep");
		long held = due2 + d.grace() + 20;
		helper.assertValueEqual(buer.think(level, NOON, held), Status.UPKEEP_HELD, "Held, he waits");
		BoundWill heldRecord = ConcordanceWorkerGameTests.pactOf(level, perch);
		helper.assertTrue(bed.strain() == 1 && GardenPacts.target(level, heldRecord, bed.getBlockPos(), keeper.getUUID(), NOON, held) == Status.UPKEEP_HELD,
				"and tends nothing: the garden service refuses every bed while the pact is held");
		helper.assertTrue(keeper.getUUID().equals(heldRecord.holder()) && BoundWills.of(level.getServer()).ledger().get(id) != null
				&& offerings(keeper, offering) == 0 && ConcordanceWorkerGameTests.escrowed(level, id) == 0 && keeper.getUUID().equals(buer.owner()),
				"Nothing lost: the pact is still the holder's, Buer still theirs, nothing taken from them");
		long later = due2 + d.grace() + 5 * d.period();
		helper.assertTrue(perch.holder().look(level, later) == Pacts.Event.NONE
				&& ConcordanceWorkerGameTests.pactOf(level, perch).pact().upkeep().due() == heldRecord.pact().upkeep().due(),
				"While held, nothing accrues as time passes");
		ConcordanceWorkerGameTests.give(keeper, new ItemStack(offering, d.count()));
		long paid = later + 500L;
		helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), offering, paid), d.count(), "An offering paid while held");
		BoundWill resumed = ConcordanceWorkerGameTests.pactOf(level, perch);
		helper.assertTrue(resumed.pact().hold() == BoundWill.Hold.NONE && Pacts.status(resumed) == null && resumed.pact().upkeep().due() == paid + d.period(),
				"resumes it at once, the next offering a whole period from then: " + resumed.pact().upkeep());
		helper.assertValueEqual(buer.think(level, NOON, paid + 20), Status.WORKING, "Resumed, he works again");
		helper.assertValueEqual(bed.strain(), 0, "one more strain eased");
		helper.succeed();
	}

	/**
	 * Unbind: its holder ends the pact; the record goes once, what was paid ahead comes back once into their inventory, the
	 * departure is kept once, the perch keeps nothing and Buer is struck off the roster and departs, tending nothing after.
	 * Ending again finds nothing; the perch is free for another pact.
	 */
	@GameTest(maxTicks = 20)
	public void endingTheGardenPactReturnsTheEscrowOnceAndBuerLeaves(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PactDefinition d = ConcordanceWorkerGameTests.buerOffer();
		ServerPlayer keeper = ConcordanceWorkerGameTests.gardenHolder(helper);
		long now = ConcordanceWorkerGameTests.dayStart(level, 1);
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = ConcordanceWorkerGameTests.buerPact(helper, keeper, at, now);
		BuerEntity buer = ConcordanceWorkerGameTests.buer(helper, perch);
		VerdantBedBlockEntity bed = ConcordanceWorkerGameTests.keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		UUID id = perch.holder().agreement().record();
		Item offering = ConcordanceWorkerGameTests.offeringOf(d);
		ConcordanceWorkerGameTests.give(keeper, new ItemStack(offering, 2));
		helper.assertTrue(PactKeeping.deposit(level, keeper, perch.holder(), offering, now) == 2 && offerings(keeper, offering) == 0
				&& ConcordanceWorkerGameTests.escrowed(level, id) == 2, "Two offerings paid ahead into the escrow");
		PactKeeping.Ending ending = PactKeeping.end(level, keeper, perch.holder(), d.id(), now + 100);
		BoundWills wills = BoundWills.of(level.getServer());
		List<Pacts.Departure> history = PactHistory.of(level.getServer()).history(keeper.getUUID());
		helper.assertTrue(ending.ended() == AgreementHolder.Ended.ENDED && offerings(keeper, offering) == 2 && wills.ledger().get(id) == null
				&& wills.ledger().heldBy(keeper.getUUID()).isEmpty() && wills.escrow(id).isEmpty(),
				"Ended: the record is gone and the two paid ahead come back once");
		helper.assertTrue(history.size() == 1 && history.getFirst().record().equals(id) && Pacts.ENDED.equals(history.getFirst().reason()),
				"The departure is kept once: " + history);
		helper.assertTrue(perch.holder().agreement() == null && perch.holder().spirit() == null && perch.presence(level) == null
				&& !WorkerRoster.of(level.getServer()).workers(keeper.getUUID()).containsKey(buer.getUUID()),
				"The perch keeps nothing and Buer is struck off the holder's roster");
		helper.assertTrue(buer.think(level, NOON, now + Garden.TEND_TICKS) == Status.FINISHED && buer.gone() && bed.strain() == 3,
				"Buer departs and tends nothing after");
		helper.assertTrue(PactKeeping.end(level, keeper, perch.holder(), d.id(), now + 200).ended() != AgreementHolder.Ended.ENDED
				&& offerings(keeper, offering) == 2 && PactHistory.of(level.getServer()).history(keeper.getUUID()).size() == 1,
				"Ending again returns nothing more and keeps no second departure");
		helper.assertValueEqual(perch.holder().verify(level, now + 200), AgreementHolder.Standing.NONE, "The perch is free for another pact");
		helper.succeed();
	}

	/**
	 * Ownership: at the holder's perch a stranger can bind nothing, and neither ends, suspends, pays, calls back, answers nor reads
	 * the holder's pact, whether by the perch's own screen, the perch or the roster's record actions; a party member benefits
	 * (Buer's pact is for the party) but cannot end or suspend it either. The record is unchanged, and Buer serves its holder.
	 */
	@GameTest(maxTicks = 20)
	public void onlyTheHolderMayBindEndPayOrCommandTheirSpirit(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PactDefinition d = ConcordanceWorkerGameTests.buerOffer();
		ServerPlayer keeper = ConcordanceWorkerGameTests.gardenHolder(helper);
		ServerPlayer stranger = ConcordanceWorkerGameTests.gardenHolder(helper);
		long now = ConcordanceWorkerGameTests.dayStart(level, 1);
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = ConcordanceWorkerGameTests.buerPact(helper, keeper, at, now);
		BuerEntity buer = ConcordanceWorkerGameTests.buer(helper, perch);
		VerdantBedBlockEntity bed = ConcordanceWorkerGameTests.keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		UUID id = perch.holder().agreement().record();
		BoundWill before = ConcordanceWorkerGameTests.pactOf(level, perch);
		Item offering = ConcordanceWorkerGameTests.offeringOf(d);
		ConcordanceWorkerGameTests.beside(helper, stranger, at);
		ConcordanceWorkerGameTests.give(stranger, new ItemStack(offering, 1));
		helper.assertValueEqual(perch.holder().seal(level, stranger, PactPerchBlockEntity.sealing(d, null, null), now).outcome(), Pacts.Outcome.NOT_PERMITTED,
				"A stranger binds nothing at another's perch");
		helper.assertTrue(PactKeeping.end(level, stranger, perch.holder(), d.id(), now).ended() == AgreementHolder.Ended.NOT_HOLDER
				&& AgreementHolder.end(level, null, stranger, id, Pacts.ENDED, now) == AgreementHolder.Ended.NOT_HOLDER,
				"nor ends it, at the perch or by its record (the roster's way)");
		helper.assertTrue(PactKeeping.suspend(level, stranger, perch.holder(), now) == AgreementHolder.Toggle.NOT_HOLDER
				&& AgreementHolder.toggle(level, stranger, id, now) == AgreementHolder.Toggle.NOT_HOLDER, "nor suspends it");
		helper.assertTrue(PactKeeping.deposit(level, stranger, perch.holder(), offering, now) == 0 && offerings(stranger, offering) == 1,
				"nor pays into it (nothing taken from them)");
		helper.assertTrue(perch.holder().recall(level, stranger, now) == AgreementHolder.Recall.NOT_HOLDER
				&& !PactAccords.resolve(level, stranger, perch, StrainTending.WORK, "any", now)
				&& PactKeeping.page(level, stranger, perch.holder(), now) == PactKeeping.Page.NONE,
				"nor calls Buer back, answers for the holder or reads the holder's page");
		PactMenu menu = ConcordanceWorkerGameTests.open(helper, stranger, perch);
		for (int action : List.of(PactRequestPayload.END, PactRequestPayload.SUSPEND, PactRequestPayload.DEPOSIT, PactRequestPayload.RECALL)) {
			PactStatePayload answer = ConcordanceWorkerGameTests.send(stranger, menu, action, 0);
			helper.assertTrue((answer == null || answer.result().isEmpty() || !answer.result().get().done())
					&& before.equals(ConcordanceWorkerGameTests.pactOf(level, perch)), "The stranger's screen action " + action + " changes nothing: " + answer);
		}
		stranger.closeContainer();
		ServerPlayer member = ConcordanceWorkerGameTests.gardenHolder(helper);
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(keeper.getUUID(), member.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(member.getUUID(), ms) == PartyManager.Result.OK, "The holder and a member form a party");
			ConcordanceWorkerGameTests.beside(helper, member, at);
			VerdantBedBlockEntity members = ConcordanceWorkerGameTests.keptBed(helper, new BlockPos(5, 1, 2), member.getUUID(), 3);
			helper.assertTrue(PactKeeping.end(level, member, perch.holder(), d.id(), now).ended() == AgreementHolder.Ended.NOT_HOLDER
					&& PactKeeping.suspend(level, member, perch.holder(), now) == AgreementHolder.Toggle.NOT_HOLDER,
					"A party member cannot end or suspend the holder's pact");
			helper.assertTrue(GardenPacts.target(level, before, members.getBlockPos(), member.getUUID(), NOON, now) == null,
					"but their bed is the pact's to serve: Buer's pact is for the holder's party");
		} finally {
			parties.adminDisband(keeper.getUUID());
		}
		helper.assertTrue(before.equals(ConcordanceWorkerGameTests.pactOf(level, perch)) && ConcordanceWorkerGameTests.escrowed(level, id) == 0
				&& keeper.getUUID().equals(buer.owner()), "The record is as it was and Buer is still the holder's");
		helper.assertTrue(GardenPacts.target(level, before, bed.getBlockPos(), STRANGER_BED, NOON, now) == Status.IDLE,
				"A bed kept by someone outside the party is never the pact's to serve");
		helper.assertValueEqual(buer.think(level, NOON, now), Status.WORKING, "Buer serves his holder");
		helper.assertValueEqual(bed.strain(), 2, "on the holder's bed");
		helper.succeed();
	}

	/** A keeper outside every party in these tests. */
	private static final UUID STRANGER_BED = new UUID(0x6C10L, 0x1BL);

	/**
	 * Claims: a neighbour's claim in the real registry (LandRegistry: Jugcraft's own land, asked through Authority.mayChange and
	 * Fabric's break event, LandProtection) over the chunk of the holder's own bed: the holder could not change it by hand, so the
	 * garden service may not, and Buer leaves it as it was. The chunk passed to the holder, his index read again, he eases it.
	 * The claim is lifted before the test ends.
	 */
	@GameTest(maxTicks = 20)
	public void aRealLandClaimKeepsBuerOffANeighboursGround(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = ConcordanceWorkerGameTests.gardenHolder(helper);
		helper.assertTrue(!FinanceService.isAdmin(keeper), "The holder is an ordinary player (an operator may build in any claim)");
		long now = ConcordanceWorkerGameTests.dayStart(level, 1);
		PactPerchBlockEntity perch = ConcordanceWorkerGameTests.buerPact(helper, keeper, new BlockPos(2, 1, 2), now);
		BuerEntity buer = ConcordanceWorkerGameTests.buer(helper, perch);
		VerdantBedBlockEntity bed = ConcordanceWorkerGameTests.keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		BlockPos pos = bed.getBlockPos();
		LandRegistry land = LandRegistry.get(level.getServer());
		FinanceBook book = Ledger.get(level.getServer()).book();
		long key = LandRegistry.keyOf(pos);
		helper.assertTrue(!LandRegistry.isClaimed(level, pos), "The bed's chunk starts unclaimed");
		try {
			helper.assertTrue(land.assign(book, key, book.account(NEIGHBOUR, "GardenNeighbour"), 0).ok() && LandRegistry.isClaimed(level, pos),
					"A neighbour claims the chunk the holder's bed stands in");
			helper.assertTrue(!LandRegistry.mayBuild(keeper, level, pos) && !Authority.mayChange(level, keeper, pos),
					"The holder could not change the bed by hand there");
			helper.assertValueEqual(GardenPacts.target(level, ConcordanceWorkerGameTests.pactOf(level, perch), pos, keeper.getUUID(), NOON, now),
					Status.BLOCKED_BY_ACCESS, "so the garden service may not either");
			helper.assertValueEqual(buer.think(level, NOON, now), Status.IDLE, "Buer waits");
			helper.assertTrue(bed.strain() == 3 && Pacts.done(ConcordanceWorkerGameTests.pactOf(level, perch).pact(), now) == 0,
					"and leaves the claimed bed as it was, counting nothing");
			helper.assertTrue(land.assign(book, key, book.account(keeper.getUUID(), "GardenHolder"), 0).ok(), "The chunk passes to the holder");
			long later = now + GardenReach.REFRESH_TICKS;
			helper.assertTrue(GardenPacts.target(level, ConcordanceWorkerGameTests.pactOf(level, perch), pos, keeper.getUUID(), NOON, later) == null,
					"In the holder's own claim the service may act");
			helper.assertValueEqual(buer.think(level, NOON, later), Status.WORKING, "Once his index is read again, he eases the bed");
			helper.assertValueEqual(bed.strain(), 2, "one strain");
		} finally {
			land.abandon(keeper, book, key, true, 0);
		}
		helper.assertTrue(!LandRegistry.isClaimed(level, pos), "The claim is lifted");
		helper.succeed();
	}

	/**
	 * Save and reload: the world's records (Bound Wills with the pact and its escrow, the roster) written and read by their own
	 * codecs read back exactly; the perch read back from its save keeps its owner, terms and presence; Buer unloaded and read back
	 * from his save is the same Buer, the holder's, his perch's presence. The perch's looks and the holder's screen call no second
	 * Buer; a copy of his save under another id is no presence and leaves; and the reloaded Buer serves on, once.
	 */
	@GameTest(maxTicks = 20)
	public void aGardenPactSurvivesASaveAndReloadWithNothingDuplicated(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		DynamicOps<JsonElement> ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
		PactDefinition d = ConcordanceWorkerGameTests.buerOffer();
		ServerPlayer keeper = ConcordanceWorkerGameTests.gardenHolder(helper);
		long now = ConcordanceWorkerGameTests.dayStart(level, 1);
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = ConcordanceWorkerGameTests.buerPact(helper, keeper, at, now);
		BuerEntity buer = ConcordanceWorkerGameTests.buer(helper, perch);
		VerdantBedBlockEntity bed = ConcordanceWorkerGameTests.keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		Item offering = ConcordanceWorkerGameTests.offeringOf(d);
		ConcordanceWorkerGameTests.give(keeper, new ItemStack(offering, 2));
		helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), offering, now), 2, "Two offerings paid ahead");
		helper.assertValueEqual(buer.think(level, NOON, now), Status.WORKING, "Before the save he eases the bed once (and learns his record)");
		UUID id = perch.holder().agreement().record();
		UUID body = buer.getUUID();
		BoundWill before = ConcordanceWorkerGameTests.pactOf(level, perch);

		// The world's records, as a restart writes and reads them.
		BoundWills wills = BoundWills.of(level.getServer());
		JsonElement written = BoundWills.CODEC.encodeStart(ops, wills).result().orElse(null);
		BoundWills read = written == null ? null : BoundWills.CODEC.parse(ops, written).result().orElse(null);
		helper.assertTrue(read != null && before.equals(read.ledger().get(id)) && read.ledger().heldBy(keeper.getUUID()).size() == 1
				&& keeper.getUUID().equals(read.ledger().get(id).holder()) && body.equals(read.ledger().get(id).pact().presence())
				&& Pacts.done(read.ledger().get(id).pact(), now) == 1
				&& read.escrow(id).stream().mapToInt(ItemStack::getCount).sum() == 2,
				"The pact reads back exactly (holder, presence, terms, today's count, upkeep) with its two offerings: " + before);
		WorkerRoster roster = WorkerRoster.of(level.getServer());
		JsonElement listed = WorkerRoster.CODEC.encodeStart(ops, roster).result().orElse(null);
		WorkerRoster rosterRead = listed == null ? null : WorkerRoster.CODEC.parse(ops, listed).result().orElse(null);
		helper.assertTrue(rosterRead != null && rosterRead.workers(keeper.getUUID()).keySet().equals(Set.of(body)),
				"The holder's roster reads back with Buer alone");

		// The perch and Buer, as a chunk's unload and load write and read them.
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		buer.saveWithoutId(out);
		CompoundTag saved = out.buildResult();
		buer.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		perch.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), perch.saveWithoutMetadata(level.registryAccess())));
		helper.assertTrue(keeper.getUUID().equals(perch.holder().owner()) && id.equals(perch.holder().agreement().record())
				&& body.equals(perch.holder().spirit()) && perch.visitor() == null, "The perch reads back its owner, terms and presence, and no visitor");
		BuerEntity back = Workers.BUER.create(level, EntitySpawnReason.LOAD);
		back.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		helper.assertTrue(body.equals(back.getUUID()) && keeper.getUUID().equals(back.owner()) && id.equals(back.record())
				&& helper.absolutePos(at).equals(back.perch()) && level.addFreshEntity(back) && perch.presence(level) == back,
				"Read back: the same Buer, the holder's, his record and perch kept, his perch's presence");
		for (int n = 1; n <= 3; n++) {
			PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), now + 20L * n);
		}
		PactMenu.open(keeper, level, perch);
		keeper.closeContainer();
		helper.assertTrue(ConcordanceWorkerGameTests.spiritsOf(helper, d).size() == 1 && !perch.holder().spiritLost(level)
				&& WorkerRoster.of(level.getServer()).workers(keeper.getUUID()).keySet().equals(Set.of(body))
				&& BoundWills.of(level.getServer()).ledger().heldBy(keeper.getUUID()).size() == 1 && ConcordanceWorkerGameTests.escrowed(level, id) == 2,
				"After the reload: one Buer, one roster entry, one pact, its escrow as it was; looks and the screen called no second");

		// A duplicated save (another id: a copied chunk, a restored backup) is no presence.
		BuerEntity twin = Workers.BUER.create(level, EntitySpawnReason.LOAD);
		twin.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		twin.setUUID(UUID.randomUUID());
		long next = now + Garden.TEND_TICKS;
		helper.assertTrue(twin.think(level, NOON, next) == Status.FINISHED && twin.gone() && bed.strain() == 2
				&& Pacts.done(ConcordanceWorkerGameTests.pactOf(level, perch).pact(), next) == 1, "A copy of his save under another id leaves, tending nothing");
		helper.assertValueEqual(back.think(level, NOON, next), Status.WORKING, "The reloaded Buer serves on");
		helper.assertTrue(bed.strain() == 1 && Pacts.done(ConcordanceWorkerGameTests.pactOf(level, perch).pact(), next) == 2,
				"once: one more strain eased, counted on the same record");
		helper.succeed();
	}

	/**
	 * The review's gameplay fix (foras-pact.md, "A ripe host after a restart"): a bed's reading is never saved and a ripe crop no
	 * longer grows, so after a restart or a chunk reload nothing read a ripe host's bed and Foras, on his safe course, waited on
	 * it for good. Now he reads one such bed a think, within the level's sampling allowance: the ripe Sunpetal in full sky is
	 * read and judged (a graft would not help it, so it is his matter, awaiting the holder's answer); a growing host's bed is
	 * never sampled by him (its own growth reads it). Nothing is grafted or spent. The bed's lost reading is
	 * VerdantBedBlockEntity.forgetSample, the state a loaded bed starts in. The allowance is shared with every test running
	 * beside this one, so a think it refuses is tried again the next tick.
	 */
	@GameTest(maxTicks = 40)
	public void forasReadsARipeHostThatLostItsReading(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = ConcordanceWorkerGameTests.gardenHolder(helper);
		long now = ConcordanceWorkerGameTests.dayStart(level, 1);
		BlockPos at = new BlockPos(3, 1, 3);
		ConcordanceWorkerGameTests.floor(helper);
		ChestBlockEntity store = ConcordanceWorkerGameTests.forasStore(helper, at.east(), 2);
		BlockPos ripeAt = new BlockPos(1, 1, 1);
		VerdantBedBlockEntity ripe = ConcordanceWorkerGameTests.forasHost(helper, ripeAt, holder.getUUID());
		helper.setBlock(ripeAt.above(), ((OrganismCropBlock) Garden.SUNPETAL_CROP).getStateForAge(OrganismCropBlock.MAX_AGE));
		// Farther from the perch than the ripe host, so the nearest-first order never puts it first.
		VerdantBedBlockEntity young = ConcordanceWorkerGameTests.forasHost(helper, new BlockPos(6, 1, 6), holder.getUUID());
		PactPerchBlockEntity perch = ConcordanceWorkerGameTests.forasPact(helper, holder, at, now);
		ForasEntity foras = ConcordanceWorkerGameTests.foras(helper, perch);
		AtomicBoolean read = new AtomicBoolean();
		for (int t = 1; t <= 30; t++) {
			long think = now + t;
			helper.runAtTickTime(t, () -> {
				if (read.get()) {
					return;
				}
				// As a restart or a chunk reload leaves them: neither bed holds a reading.
				ripe.forgetSample();
				young.forgetSample();
				Status status = foras.think(level, NOON, think);
				if (ripe.cachedHabitat(level) == null) {
					// The level's allowance was spent this tick (by tests running beside this one): nothing read, nothing done.
					helper.assertTrue(ripe.graft().isEmpty() && store.countItem(Garden.DEWMOSS) == 2, "Unread, nothing is grafted or spent");
					return;
				}
				read.set(true);
				GraftKeeping.Memory memory = ConcordanceWorkerGameTests.keeping(foras);
				helper.assertTrue(status == Status.AWAITING_ANSWER && memory.useless().equals(List.of(helper.absolutePos(ripeAt.above()))),
						"Read, the ripe host is judged: a graft would not help it in full sky, so it is his matter: " + status + " " + memory.useless());
				helper.assertTrue(young.cachedHabitat(level) == null && memory.unread() == 1,
						"The growing host's bed is not sampled by him: it waits for its own growth's reading");
				helper.assertTrue(ripe.graft().isEmpty() && young.graft().isEmpty() && store.countItem(Garden.DEWMOSS) == 2,
						"Nothing grafted or spent while his matter waits");
			});
		}
		helper.runAtTickTime(32, () -> {
			helper.assertTrue(read.get(), "Within 30 ticks Foras read the ripe host's bed");
			helper.succeed();
		});
	}

	/**
	 * Research gating: the perch itself is ordinary equipment, never behind research. A survival player who has learned nothing
	 * may place a perch where they may build and open its screen; what a spirit asks is its own research (The Garden's Regard),
	 * named on that screen as the steps still missing, and nothing else.
	 */
	@GameTest(maxTicks = 20)
	public void thePerchNeedsNoResearchOnlyTheSpiritsOwn(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer novice = TestPlayers.survivor(helper);
		novice.getInventory().clearContent();
		BlockPos at = new BlockPos(2, 1, 2);
		helper.assertTrue(PactPerchBlock.mayPlace(level, novice, helper.absolutePos(at)), "A player with no research may place a perch where they may build");
		PactPerchBlockEntity perch = ConcordanceWorkerGameTests.perch(helper, at, novice);
		ConcordanceWorkerGameTests.beside(helper, novice, at);
		PactMenu menu = ConcordanceWorkerGameTests.open(helper, novice, perch);
		PactStatePayload shown = menu.lastSent();
		List<String> notes = ConcordanceWorkerGameTests.keys(shown.notes());
		helper.assertTrue(novice.getUUID().equals(perch.holder().owner()) && !notes.isEmpty()
				&& notes.stream().allMatch(note -> note.startsWith("message.jugcraft.concordance.pact.")),
				"Its screen opens, and names only what the spirit itself asks: " + notes);
		novice.closeContainer();
		helper.succeed();
	}
}
