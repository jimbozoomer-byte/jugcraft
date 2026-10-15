package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.concordance.Alchemy;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.Reagent;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Formula;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Operation;
import io.github.jimbozoomer.jugcraft.concordance.courier.CourierLedger;
import io.github.jimbozoomer.jugcraft.concordance.courier.CourierPostBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.courier.Couriers;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Code;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Diagnostic;
import io.github.jimbozoomer.jugcraft.concordance.logistics.Event;
import io.github.jimbozoomer.jugcraft.concordance.logistics.Logistics;
import io.github.jimbozoomer.jugcraft.concordance.logistics.Place;
import io.github.jimbozoomer.jugcraft.concordance.logistics.Request;
import io.github.jimbozoomer.jugcraft.concordance.logistics.RequestState;
import io.github.jimbozoomer.jugcraft.concordance.rules.ConcordanceRules;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ClockworkPorterEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import io.github.jimbozoomer.jugcraft.concordance.worker.Body;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import io.github.jimbozoomer.jugcraft.concordance.worker.WorkerDefinition;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;
import io.github.jimbozoomer.jugcraft.party.PartyManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import local.peepo.CompanionStatus;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
/**
 * Roadmap step 18, logistics with reservations and accountable transit (docs/features/arcane-concordance-logistics.md),
 * on a real server. Each test drives couriers' decisions with {@code think} at chosen times, standing them within reach
 * of the post and chests so no walking is needed, and audits after every step that each item is in a container or in
 * the ledger's cargo, exactly once ({@link CourierLedger#problems} and counted totals). Since Concordance Chunk C (C-06),
 * an Alembic Crucible files its own requests: one demand, its formula's shortage, served by a porter and a Peepo without
 * bringing it twice, through every failure case of plan 03:193.
 */
public class ConcordanceLogisticsGameTests {
	private static final long NIGHT = 18000L;

	private static ServerPlayer keeper(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		ConcordanceProgress.grant(player, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(player, Workers.RESEARCH, ResearchState.UNDERSTOOD);
		RateGate.forget(player.getUUID());
		return player;
	}

	private static void place(GameTestHelper helper, Entity entity, double x, double y, double z) {
		Vec3 at = helper.absoluteVec(new Vec3(x, y, z));
		entity.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
	}

	/** A post at (2,1,2) with chests at (1,1,2) and (3,1,2), and a fuelled courier between them, bound to the post. */
	private static ClockworkPorterEntity courier(GameTestHelper helper, ServerPlayer keeper, BlockPos post) {
		ServerLevel level = helper.getLevel();
		WorkerDefinition.Construct terms = ClockworkPorterEntity.terms();
		ClockworkPorterEntity porter = Workers.CLOCKWORK_PORTER.create(level, EntitySpawnReason.MOB_SUMMONED);
		porter.setOwner(keeper.getUUID());
		porter.setBody(new Body(terms.integrity(), terms.energy()));
		place(helper, porter, 2.5, 1.0, 3.5);
		level.addFreshEntity(porter);
		helper.assertTrue(porter.setPost(GlobalPos.of(level.dimension(), helper.absolutePos(post))), "The porter takes the post");
		return porter;
	}

	private static CourierPostBlockEntity post(GameTestHelper helper, ServerPlayer keeper, BlockPos at) {
		helper.setBlock(at, Couriers.COURIER_POST);
		CourierPostBlockEntity post = (CourierPostBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
		post.setOwner(keeper.getUUID());
		return post;
	}

	private static int count(net.minecraft.world.Container container, net.minecraft.world.item.Item item) {
		int count = 0;
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			if (container.getItem(slot).is(item)) {
				count += container.getItem(slot).getCount();
			}
		}
		return count;
	}

	private static void audit(GameTestHelper helper, CourierLedger ledger, String when) {
		helper.assertTrue(ledger.problems().isEmpty(), when + ": the ledger and its cargo agree: " + ledger.problems());
	}

	// ---------------------------------------------------------------- an Alembic Crucible files its own requests (Concordance Chunk C, C-06)

	private static final String GLOW = "minecraft:glowstone_dust";
	private static final Operation.Add RAW = new Operation.Add(GLOW, "jugcraft:raw");
	private static final Operation.Add GROUND_ADD = new Operation.Add(GLOW, "jugcraft:ground");
	private static final Reagent GROUND_GLOW = new Reagent(GLOW, "jugcraft:ground");
	private static final Predicate<ItemStack> PLAIN = stack -> stack.is(Items.GLOWSTONE_DUST);
	private static final Predicate<ItemStack> GROUND = stack -> stack.is(JugcraftConcordance.REAGENT_ITEM) && GROUND_GLOW.equals(stack.get(JugcraftConcordance.REAGENT));
	private static final String ONE_GLOW = "water 1; add minecraft:glowstone_dust jugcraft:raw; stir hot; stir hot";
	private static final String TWO_GLOW = "water 2; add minecraft:glowstone_dust jugcraft:raw; add minecraft:glowstone_dust jugcraft:raw; stir hot; stir hot";
	private static final String TWO_GROUND = "water 2; add minecraft:glowstone_dust jugcraft:ground; add minecraft:glowstone_dust jugcraft:ground; stir hot; stir hot";
	/** A formula's setter who is never online (anOfflineSetterPausesTheCourier). */
	private static final UUID AWAY = new UUID(31L, 7L);

	private static ServerPlayer alembist(GameTestHelper helper) {
		ServerPlayer player = keeper(helper);
		ConcordanceProgress.grant(player, Alchemy.RESEARCH, ResearchState.UNDERSTOOD);
		return player;
	}

	private static CrucibleBlockEntity crucible(GameTestHelper helper, BlockPos at) {
		helper.setBlock(at, JugcraftConcordance.CRUCIBLE);
		return helper.getBlockEntity(at, CrucibleBlockEntity.class);
	}

	private static ChestBlockEntity chest(GameTestHelper helper, BlockPos at) {
		helper.setBlock(at, Blocks.CHEST);
		return helper.getBlockEntity(at, ChestBlockEntity.class);
	}

	private static ItemStack ground(int count) {
		ItemStack reagent = new ItemStack(JugcraftConcordance.REAGENT_ITEM, count);
		reagent.set(JugcraftConcordance.REAGENT, GROUND_GLOW);
		return reagent;
	}

	private static void pour(CrucibleBlockEntity crucible, int parts) {
		try (Transaction transaction = Transaction.openOuter()) {
			crucible.water.insert(FluidVariant.of(Fluids.WATER), parts * FluidConstants.BOTTLE, transaction);
			transaction.commit();
		}
	}

	/** As a player: a written formula used on the crucible; its user is recorded as the setter. */
	private static void setFormula(GameTestHelper helper, CrucibleBlockEntity crucible, ServerPlayer player, String formula) {
		ItemStack written = new ItemStack(JugcraftConcordance.FORMULA_ITEM);
		written.set(JugcraftConcordance.FORMULA, formula);
		RateGate.forget(player.getUUID());
		player.setItemInHand(InteractionHand.MAIN_HAND, written);
		helper.assertTrue(crucible.use(player, helper.getLevel(), written, InteractionHand.MAIN_HAND).consumesAction() && crucible.program() != null
				&& player.getUUID().equals(crucible.formulaOwner()), "The formula is set and its setter recorded: " + formula);
	}

	/** Set by setter, water poured: two pulses reach its first ingredient, which it asks the couriers for; the request. */
	private static long waiting(GameTestHelper helper, CrucibleBlockEntity crucible, ServerPlayer setter, String formula) {
		setFormula(helper, crucible, setter, formula);
		pour(crucible, 2);
		Diagnostic poured = crucible.automate(helper.getLevel());
		Diagnostic waits = crucible.automate(helper.getLevel());
		helper.assertTrue(poured == null && waits != null && waits.code() == Code.MISSING_INGREDIENT && crucible.request() > 0L,
				"It waits for its glowstone and has asked the couriers: " + waits + ", request " + crucible.request());
		return crucible.request();
	}

	private static int matching(Container container, Predicate<ItemStack> which) {
		int count = 0;
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			if (which.test(container.getItem(slot))) {
				count += container.getItem(slot).getCount();
			}
		}
		return count;
	}

	private static long bound(CourierLedger ledger, Place destination) {
		return ledger.ledger().requests().stream().filter(r -> r.ticket().destination().equals(destination)).count();
	}

	/** After every step (C-06): the ledger agrees with its cargo, and what {@code which} matches is in a container or carried by {@code ids}, {@code n} in all. */
	private static void conserved(GameTestHelper helper, CourierLedger ledger, List<Long> ids, Predicate<ItemStack> which, int n, String when, Container... containers) {
		int held = 0;
		for (Container container : containers) {
			held += matching(container, which);
		}
		int carried = 0;
		for (long id : ids) {
			Request request = ledger.ledger().get(id);
			carried += request == null ? 0 : request.progress().carried();
		}
		helper.assertTrue(ledger.problems().isEmpty(), when + ": the ledger and its cargo agree: " + ledger.problems());
		helper.assertTrue(held + carried == n, when + ": " + held + " held and " + carried + " carried, not " + n);
	}

	/** The kinds of {@code id}'s events in a post's history, oldest first. */
	private static List<String> story(CourierLedger ledger, Place post, long id) {
		return ledger.ledger().history(post).stream().filter(e -> e.request() == id).map(e -> e.kind()).toList();
	}

	/**
	 * {@code during} under {@code rules}, at once, the rules put back whatever happens (C-P28): ConcordanceData's rules are
	 * every test's, and the server runs other tests, so they are never left swapped past this call (check_diagnostics).
	 */
	private static <T> T under(ConcordanceRules rules, Supplier<T> during) {
		ConcordanceRules was = ConcordanceData.rules();
		ConcordanceData.replace(rules);
		try {
			return during.get();
		} finally {
			ConcordanceData.replace(was);
		}
	}

	/** Runs {@code check} with the server's absent-owner option on, then puts it back. */
	private static void withAbsentOwners(Runnable check) {
		JugcraftConfig.setOption(Authority.ABSENT_OPTION, true);
		try {
			check.run();
		} finally {
			JugcraftConfig.setOption(Authority.ABSENT_OPTION, false);
		}
	}

	/**
	 * A request is filed for exactly an item, claimed, reserved at the chest that has it, picked up into the ledger,
	 * delivered to the post and closed; the history tells each step; the named ingots in the same chest are untouched.
	 */
	@GameTest(maxTicks = 20)
	public void aRequestIsFetchedAndDelivered(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = keeper(helper);
		CourierPostBlockEntity post = post(helper, keeper, new BlockPos(2, 1, 2));
		helper.setBlock(new BlockPos(1, 1, 2), Blocks.CHEST);
		ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(helper.absolutePos(new BlockPos(1, 1, 2)));
		chest.setItem(0, new ItemStack(Items.IRON_INGOT, 40));
		ItemStack named = new ItemStack(Items.IRON_INGOT, 5);
		named.set(DataComponents.CUSTOM_NAME, Component.literal("Heirloom"));
		chest.setItem(1, named);
		ClockworkPorterEntity porter = courier(helper, keeper, new BlockPos(2, 1, 2));
		CourierLedger ledger = CourierLedger.of(level.getServer());
		Couriers.request(keeper, level, post, new ItemStack(Items.IRON_INGOT), 32);
		Place at = Couriers.place(level, post.getBlockPos());
		Request request = ledger.ledger().requests().stream().filter(r -> r.ticket().post().equals(at)).findFirst().orElse(null);
		helper.assertTrue(request != null && request.ticket().wanted() == 32 && request.state() == RequestState.OPEN, "Filed: open");
		long id = request.id();
		long now = level.getGameTime() + 400_000L;
		porter.think(level, NIGHT, now);
		helper.assertTrue(porter.task() == id && ledger.ledger().get(id).progress().reserved() == 32, "Claimed and reserved: "
				+ ledger.ledger().get(id).progress());
		porter.think(level, NIGHT, now + 20);
		helper.assertTrue(ledger.ledger().get(id).state() == RequestState.IN_TRANSIT && count(chest, Items.IRON_INGOT) == 13,
				"Picked up 32 plain ingots; the 5 named ones stay: " + count(chest, Items.IRON_INGOT));
		audit(helper, ledger, "in transit");
		porter.think(level, NIGHT, now + 40);
		helper.assertTrue(ledger.ledger().get(id) == null && count(post, Items.IRON_INGOT) == 32 && porter.task() == 0L,
				"Delivered to the post and closed");
		helper.assertTrue(chest.getItem(1).has(DataComponents.CUSTOM_NAME), "The named ingots were never taken");
		java.util.List<String> kinds = ledger.ledger().history(at).stream().map(e -> e.kind()).toList();
		helper.assertTrue(kinds.containsAll(java.util.List.of("filed", "claimed", "reserved", "picked_up", "done")), "The history tells it: " + kinds);
		audit(helper, ledger, "done");
		helper.succeed();
	}

	/** Two couriers and two requests for more than the chest holds: one claim each, never the same items twice. */
	@GameTest(maxTicks = 20)
	public void simultaneousRequestsNeverClaimTheSameItems(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = keeper(helper);
		CourierPostBlockEntity post = post(helper, keeper, new BlockPos(2, 1, 2));
		helper.setBlock(new BlockPos(1, 1, 2), Blocks.CHEST);
		ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(helper.absolutePos(new BlockPos(1, 1, 2)));
		chest.setItem(0, new ItemStack(Items.COBBLESTONE, 40));
		ClockworkPorterEntity one = courier(helper, keeper, new BlockPos(2, 1, 2));
		ClockworkPorterEntity two = courier(helper, keeper, new BlockPos(2, 1, 2));
		CourierLedger ledger = CourierLedger.of(level.getServer());
		Couriers.request(keeper, level, post, new ItemStack(Items.COBBLESTONE), 30);
		Couriers.request(keeper, level, post, new ItemStack(Items.COBBLESTONE), 30);
		long now = level.getGameTime() + 500_000L;
		one.think(level, NIGHT, now);
		two.think(level, NIGHT, now);
		helper.assertTrue(one.task() != 0L && two.task() != 0L && one.task() != two.task(), "Each courier holds a different request");
		int reserved = ledger.ledger().get(one.task()).progress().reserved() + ledger.ledger().get(two.task()).progress().reserved();
		helper.assertTrue(reserved == 40, "Together they reserve the chest's 40, not 60: " + reserved);
		// The server-wide ledger also holds cargo from other tests. These synchronous decisions must add exactly
		// our chest's forty items while leaving that existing cargo accounted for.
		int carriedBefore = ledger.ledger().carried().values().stream().mapToInt(Integer::intValue).sum();
		one.think(level, NIGHT, now + 20);
		two.think(level, NIGHT, now + 20);
		int carried = ledger.ledger().get(one.task()).progress().carried() + ledger.ledger().get(two.task()).progress().carried();
		int carriedAfter = ledger.ledger().carried().values().stream().mapToInt(Integer::intValue).sum();
		helper.assertTrue(count(chest, Items.COBBLESTONE) == 0 && carried == 40 && carriedAfter - carriedBefore == 40,
				"40 picked up by these requests, no more: requests=" + carried + ", ledger increase=" + (carriedAfter - carriedBefore));
		audit(helper, ledger, "both carrying");
		helper.succeed();
	}

	/**
	 * A full post takes what fits and the rest stays in transit; the courier removed, its cargo is stranded at the post,
	 * not lost; another courier takes it up; a restart of the ledger (saved and loaded) keeps every count.
	 */
	@GameTest(maxTicks = 20)
	public void fullStorageRemovalAndRestartLoseNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = keeper(helper);
		CourierPostBlockEntity post = post(helper, keeper, new BlockPos(2, 1, 2));
		for (int slot = 0; slot < CourierPostBlockEntity.SLOTS - 1; slot++) {
			post.setItem(slot, new ItemStack(Items.DIRT, 64));
		}
		post.setItem(CourierPostBlockEntity.SLOTS - 1, new ItemStack(Items.SAND, 54));
		helper.setBlock(new BlockPos(1, 1, 2), Blocks.CHEST);
		ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(helper.absolutePos(new BlockPos(1, 1, 2)));
		chest.setItem(0, new ItemStack(Items.SAND, 30));
		ClockworkPorterEntity first = courier(helper, keeper, new BlockPos(2, 1, 2));
		CourierLedger ledger = CourierLedger.of(level.getServer());
		Couriers.request(keeper, level, post, new ItemStack(Items.SAND), 30);
		long now = level.getGameTime() + 600_000L;
		first.think(level, NIGHT, now);
		long id = first.task();
		first.think(level, NIGHT, now + 20);
		first.think(level, NIGHT, now + 40);
		helper.assertTrue(count(post, Items.SAND) == 64 && ledger.ledger().get(id).progress().carried() == 20
				&& first.status() == Status.WORKING, "The post takes 10; 20 stay in transit: " + ledger.ledger().get(id).progress());
		helper.assertTrue(first.think(level, NIGHT, now + 60) == Status.FULL, "It says the post is full");
		audit(helper, ledger, "full");
		first.discard();
		helper.assertTrue(ledger.ledger().get(id).state() == RequestState.STRANDED && ledger.ledger().get(id).progress().carried() == 20,
				"Its courier removed: 20 stranded at the post, not dropped and not lost");
		// The ledger saved and loaded again (a restart): the same request, the same cargo.
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		saved.store("ledger", CourierLedger.CODEC, ledger);
		CourierLedger restarted = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult())
				.read("ledger", CourierLedger.CODEC).orElseThrow();
		helper.assertTrue(restarted.ledger().get(id) != null && restarted.ledger().get(id).state() == RequestState.STRANDED
				&& restarted.ledger().get(id).progress().carried() == 20 && restarted.item(id) != null && restarted.problems().isEmpty(),
				"After a restart the stranded cargo is there, counted once");
		for (int slot = 0; slot < CourierPostBlockEntity.SLOTS - 1; slot++) {
			post.setItem(slot, ItemStack.EMPTY);
		}
		ClockworkPorterEntity second = courier(helper, keeper, new BlockPos(2, 1, 2));
		second.think(level, NIGHT, now + 80);
		helper.assertTrue(second.task() == id && ledger.ledger().get(id).progress().aboard(), "Another courier takes it up at the post");
		second.think(level, NIGHT, now + 100);
		helper.assertTrue(ledger.ledger().get(id) == null && count(post, Items.SAND) == 84, "and delivers the rest: all 30 arrived, once");
		audit(helper, ledger, "taken up and delivered");
		helper.succeed();
	}

	/** Cancelling with cargo in transit takes it back to its chest; recovering stranded cargo hands it to its requester. */
	@GameTest(maxTicks = 20)
	public void cancellingAndRecoveringGiveItemsBack(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = keeper(helper);
		CourierPostBlockEntity post = post(helper, keeper, new BlockPos(2, 1, 2));
		helper.setBlock(new BlockPos(1, 1, 2), Blocks.CHEST);
		ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(helper.absolutePos(new BlockPos(1, 1, 2)));
		chest.setItem(0, new ItemStack(Items.OAK_LOG, 16));
		ClockworkPorterEntity porter = courier(helper, keeper, new BlockPos(2, 1, 2));
		CourierLedger ledger = CourierLedger.of(level.getServer());
		Couriers.request(keeper, level, post, new ItemStack(Items.OAK_LOG), 16);
		long now = level.getGameTime() + 700_000L;
		porter.think(level, NIGHT, now);
		long id = porter.task();
		porter.think(level, NIGHT, now + 20);
		helper.assertTrue(count(chest, Items.OAK_LOG) == 0 && ledger.cancel(id, keeper.getUUID(), now + 30) == Logistics.Outcome.DONE
				&& ledger.ledger().get(id).state() == RequestState.RETURNING, "Cancelled in transit: it is taken back");
		porter.think(level, NIGHT, now + 40);
		helper.assertTrue(ledger.ledger().get(id) == null && count(chest, Items.OAK_LOG) == 16 && count(post, Items.OAK_LOG) == 0,
				"All 16 back in the chest, none at the post");
		// Stranded cargo with no courier: its requester recovers it, once.
		chest.setItem(0, new ItemStack(Items.OAK_LOG, 8));
		Couriers.request(keeper, level, post, new ItemStack(Items.OAK_LOG), 8);
		porter.think(level, NIGHT, now + 60);
		long lost = porter.task();
		porter.think(level, NIGHT, now + 80);
		porter.discard();
		helper.assertTrue(ledger.recover(lost, keeper, now + 90) == Logistics.Outcome.DONE
				&& keeper.getInventory().countItem(Items.OAK_LOG) == 8, "The requester recovers the 8 stranded logs");
		helper.assertTrue(ledger.recover(lost, keeper, now + 91) == Logistics.Outcome.NOT_FOUND && keeper.getInventory().countItem(Items.OAK_LOG) == 8,
				"and never twice");
		audit(helper, ledger, "recovered");
		helper.succeed();
	}

	/** Breaking the post while a courier carries for it sends the cargo back to its chest. */
	@GameTest(maxTicks = 20)
	public void aBrokenPostSendsItsCargoBack(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = keeper(helper);
		CourierPostBlockEntity post = post(helper, keeper, new BlockPos(2, 1, 2));
		helper.setBlock(new BlockPos(1, 1, 2), Blocks.CHEST);
		ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(helper.absolutePos(new BlockPos(1, 1, 2)));
		chest.setItem(0, new ItemStack(Items.BRICK, 12));
		ClockworkPorterEntity porter = courier(helper, keeper, new BlockPos(2, 1, 2));
		CourierLedger ledger = CourierLedger.of(level.getServer());
		Couriers.request(keeper, level, post, new ItemStack(Items.BRICK), 12);
		long now = level.getGameTime() + 800_000L;
		porter.think(level, NIGHT, now);
		long id = porter.task();
		porter.think(level, NIGHT, now + 20);
		helper.setBlock(new BlockPos(2, 1, 2), Blocks.AIR);
		helper.assertTrue(ledger.ledger().get(id) != null && ledger.ledger().get(id).state() == RequestState.RETURNING,
				"The post broken: its request turns back");
		helper.assertTrue(ledger.ledger().get(id).progress().carried() == 12 && ledger.problems().isEmpty(), "with all 12 still counted");
		helper.succeed();
	}

	/**
	 * Concordance Chunk C, C-06 (C-D4, C-P13): a crucible waiting for its formula's first ingredient files one request, as
	 * its formula's setter, at the post, for exactly what it lacks (two ground glowstone; the plain glowstone in its buffer
	 * is the wrong preparation and never stands in), and no other while that one is open. A porter brings them; while a
	 * courier holds the request the Peepo port asks for none of it (C-P50); a restart keeps the request, its exact reagent
	 * and its cargo; cargo already carried is delivered even after the shortage falls below it (C-P51: one put in by hand,
	 * which the crucible takes, so one ground glowstone is left over); the formula goes on.
	 */
	@GameTest(maxTicks = 20)
	public void aCrucibleAsksOnceForItsExactShortage(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer setter = alembist(helper);
		CourierPostBlockEntity post = post(helper, setter, new BlockPos(2, 1, 2));
		ChestBlockEntity chest = chest(helper, new BlockPos(1, 1, 2));
		chest.setItem(0, ground(10));
		chest.setItem(1, new ItemStack(Items.GLOWSTONE_DUST, 7));
		CrucibleBlockEntity crucible = crucible(helper, new BlockPos(3, 1, 2));
		setFormula(helper, crucible, setter, TWO_GROUND);
		pour(crucible, 2);
		crucible.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, 3));
		CourierLedger ledger = CourierLedger.of(level.getServer());
		Place here = Couriers.place(level, crucible.getBlockPos());
		Place at = Couriers.place(level, post.getBlockPos());
		helper.assertTrue(crucible.automate(level) == null && crucible.step() == 1 && crucible.request() == -1L, "The water poured; nothing asked yet");
		Diagnostic wrong = crucible.automate(level);
		long id = crucible.request();
		Request filed = ledger.ledger().get(id);
		helper.assertTrue(wrong != null && wrong.code() == Code.WRONG_PREPARATION && filed != null && filed.ticket().wanted() == 2
				&& filed.ticket().requester().equals(setter.getUUID()) && filed.ticket().post().equals(at) && filed.ticket().destination().equals(here)
				&& ItemVariant.of(ground(1)).equals(ledger.item(id)), "Its setter's post is asked for exactly two ground glowstone: " + wrong + ", " + filed);
		helper.assertTrue(crucible.companion.needed(ground(1)) == 2 && crucible.courierRemainder(GROUND_ADD) == 0,
				"No courier holds it yet, so a Peepo would still bring both");
		long next = ledger.ledger().nextId();
		for (int i = 0; i < 3; i++) {
			crucible.automate(level);
		}
		helper.assertTrue(crucible.request() == id && bound(ledger, here) == 1 && ledger.ledger().nextId() == next,
				"Three more pulses ask nothing more while it is open");
		conserved(helper, ledger, List.of(id), GROUND, 10, "filed", chest, crucible);
		conserved(helper, ledger, List.of(id), PLAIN, 10, "filed (plain)", chest, crucible);
		ClockworkPorterEntity porter = courier(helper, setter, new BlockPos(2, 1, 2));
		long now = level.getGameTime() + 900_000L;
		porter.think(level, NIGHT, now);
		helper.assertTrue(porter.task() == id && ledger.ledger().get(id).progress().reserved() == 2 && crucible.companion.needed(ground(1)) == 0,
				"Claimed and reserved: a Peepo now brings none of it");
		conserved(helper, ledger, List.of(id), GROUND, 10, "claimed", chest, crucible);
		porter.think(level, NIGHT, now + 20);
		helper.assertTrue(ledger.ledger().get(id).state() == RequestState.IN_TRANSIT && matching(chest, GROUND) == 8 && matching(chest, PLAIN) == 7,
				"Two ground glowstone picked up; the plain ones stay");
		conserved(helper, ledger, List.of(id), GROUND, 10, "in transit", chest, crucible);
		// The contract's "server restarts" row (critique correction 6): the ledger saved and loaded keeps the crucible's request.
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		saved.store("ledger", CourierLedger.CODEC, ledger);
		CourierLedger restarted = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult())
				.read("ledger", CourierLedger.CODEC).orElseThrow();
		helper.assertTrue(restarted.ledger().get(id) != null && restarted.ledger().get(id).progress().carried() == 2
				&& ItemVariant.of(ground(1)).equals(restarted.item(id)) && restarted.ledger().get(id).ticket().destination().equals(here)
				&& restarted.problems().isEmpty(), "A restart keeps its request, its exact reagent and its cargo");
		// C-P51 (the unit review): cargo already carried is always delivered, though the request now asks more than is lacked.
		crucible.setItem(1, ground(1));
		CrucibleBlockEntity.Shortage fell = crucible.shortage();
		helper.assertTrue(fell != null && fell.count() == 1, "One put in by hand: it lacks one, not two: " + fell);
		helper.assertTrue(crucible.automate(level) == null && crucible.step() == 2 && crucible.request() == id
				&& ledger.ledger().get(id).state() == RequestState.IN_TRANSIT && ledger.ledger().get(id).progress().carried() == 2,
				"It takes the one put in and keeps the request its courier carries for");
		conserved(helper, ledger, List.of(id), GROUND, 10, "carried, the shortage fallen (the one put in, used)", chest, crucible);
		porter.think(level, NIGHT, now + 40);
		helper.assertTrue(ledger.ledger().get(id) == null && matching(crucible, GROUND) == 2 && matching(crucible, PLAIN) == 3 && porter.task() == 0L,
				"Delivered into its buffer and closed");
		conserved(helper, ledger, List.of(id), GROUND, 10, "delivered", chest, crucible);
		helper.assertTrue(crucible.automate(level) == null && crucible.step() == 3 && crucible.request() == -1L && bound(ledger, here) == 0
				&& matching(crucible, GROUND) == 1 && matching(crucible, PLAIN) == 3,
				"It goes on, forgetting the closed request, and takes the second; one is left over and the plain glowstone is untouched");
		helper.assertValueEqual(story(ledger, at, id), List.of("filed", "claimed", "reserved", "picked_up", "done"), "the post's history of it");
		helper.succeed();
	}

	/**
	 * C-06 (C-P13): every way its formula ends turns the crucible's request back: cleared by its setter; replaced by another
	 * formula, its carried cargo going back to the chest (the same formula set again by the same player keeps it); set by
	 * another player; or refused by the rules in force, and asked again once they allow it. Broken, the crucible takes its
	 * last request with it. Nothing is lost or doubled at any step.
	 */
	@GameTest(maxTicks = 20)
	public void aClearedOrReplacedFormulaCancelsItsRequest(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer setter = alembist(helper);
		ServerPlayer other = alembist(helper);
		post(helper, setter, new BlockPos(2, 1, 2));
		ChestBlockEntity chest = chest(helper, new BlockPos(1, 1, 2));
		chest.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, 10));
		CrucibleBlockEntity cleared = crucible(helper, new BlockPos(3, 1, 2));
		CrucibleBlockEntity replaced = crucible(helper, new BlockPos(3, 1, 3));
		CrucibleBlockEntity taken = crucible(helper, new BlockPos(3, 1, 1));
		CrucibleBlockEntity refused = crucible(helper, new BlockPos(1, 1, 3));
		Container[] all = {chest, cleared, replaced, taken, refused};
		CourierLedger ledger = CourierLedger.of(level.getServer());
		List<Long> ids = new ArrayList<>();
		long first = waiting(helper, cleared, setter, TWO_GLOW);
		ids.add(first);
		cleared.useEmpty(setter, true);
		helper.assertTrue(ledger.ledger().get(first) == null && cleared.request() == -1L && cleared.program() == null && cleared.formulaOwner() == null,
				"Cleared by its setter: its request is turned back and its setter forgotten");
		conserved(helper, ledger, ids, PLAIN, 10, "cleared", all);
		long second = waiting(helper, replaced, setter, TWO_GLOW);
		ids.add(second);
		ClockworkPorterEntity porter = courier(helper, setter, new BlockPos(2, 1, 2));
		long now = level.getGameTime() + 1_000_000L;
		porter.think(level, NIGHT, now);
		porter.think(level, NIGHT, now + 20);
		helper.assertTrue(porter.task() == second && ledger.ledger().get(second).progress().carried() == 2 && matching(chest, PLAIN) == 8,
				"A courier carries two for it");
		conserved(helper, ledger, ids, PLAIN, 10, "carried", all);
		setFormula(helper, replaced, setter, TWO_GLOW);
		helper.assertTrue(replaced.request() == second && ledger.ledger().get(second).state() == RequestState.IN_TRANSIT,
				"The same formula set again by its setter keeps the request");
		conserved(helper, ledger, ids, PLAIN, 10, "set again", all);
		setFormula(helper, replaced, setter, ONE_GLOW);
		helper.assertTrue(replaced.request() == -1L && ledger.ledger().get(second) != null && ledger.ledger().get(second).state() == RequestState.RETURNING
				&& ledger.ledger().get(second).progress().carried() == 2, "Replaced: the two carried are taken back");
		conserved(helper, ledger, ids, PLAIN, 10, "returning", all);
		porter.think(level, NIGHT, now + 40);
		helper.assertTrue(ledger.ledger().get(second) == null && matching(chest, PLAIN) == 10 && matching(replaced, PLAIN) == 0,
				"Back in the chest, none in the crucible");
		conserved(helper, ledger, ids, PLAIN, 10, "returned", all);
		long third = waiting(helper, taken, setter, TWO_GLOW);
		ids.add(third);
		setFormula(helper, taken, other, TWO_GLOW);
		helper.assertTrue(ledger.ledger().get(third) == null && taken.request() == -1L && other.getUUID().equals(taken.formulaOwner()),
				"Set by another player: the first setter's request is turned back");
		conserved(helper, ledger, ids, PLAIN, 10, "taken", all);
		long fourth = waiting(helper, refused, setter, TWO_GLOW);
		ids.add(fourth);
		Diagnostic incompatible = under(ConcordanceRules.EMPTY, () -> refused.automate(level));
		helper.assertTrue(incompatible != null && incompatible.code() == Code.INCOMPATIBLE && ledger.ledger().get(fourth) == null && refused.request() == -1L,
				"A formula the rules refuse turns its request back: " + incompatible);
		Diagnostic again = refused.automate(level);
		long fifth = refused.request();
		ids.add(fifth);
		helper.assertTrue(again != null && again.code() == Code.MISSING_INGREDIENT && fifth > 0L && fifth != fourth
				&& bound(ledger, Couriers.place(level, refused.getBlockPos())) == 1, "The rules restored, it asks again, once: " + again + ", request " + fifth);
		conserved(helper, ledger, ids, PLAIN, 10, "asked again", all);
		helper.setBlock(new BlockPos(1, 1, 3), Blocks.AIR);
		helper.assertTrue(ledger.ledger().get(fifth) == null, "Broken, the crucible takes its request with it");
		helper.succeed();
	}

	/**
	 * C-06 (C-P13, C-P52): with its setter's sixteen requests open, the crucible files none, says why (the count, what, and
	 * when it asks again) and backs off 200 ticks, doubling to 1,200; a Peepo reads that as missing input and may still
	 * bring it. Room again, it waits out the backoff and its own pulse files the request, once.
	 */
	@GameTest(maxTicks = 300)
	public void aFullCourierQuotaWaitsAndBacksOff(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer setter = alembist(helper);
		CourierPostBlockEntity post = post(helper, setter, new BlockPos(2, 1, 2));
		CrucibleBlockEntity crucible = crucible(helper, new BlockPos(3, 1, 2));
		setFormula(helper, crucible, setter, TWO_GLOW);
		pour(crucible, 2);
		CourierLedger ledger = CourierLedger.of(level.getServer());
		Place at = Couriers.place(level, post.getBlockPos());
		Place here = Couriers.place(level, crucible.getBlockPos());
		List<Long> fillers = new ArrayList<>();
		for (int i = 0; i < Logistics.MAX_OPEN_PER_PLAYER; i++) {
			Logistics.Filed filler = ledger.file(setter.getUUID(), at, at, new ItemStack(Items.COBBLESTONE), 1, level.registryAccess(), level.getGameTime());
			helper.assertTrue(filler.outcome() == Logistics.Outcome.DONE, "Filler " + i + " filed: " + filler);
			fillers.add(filler.id());
		}
		helper.assertTrue(crucible.automate(level) == null && crucible.step() == 1, "The water poured");
		long next = ledger.ledger().nextId();
		Diagnostic quota = Diagnostic.courierQuota(2, RAW.describe(), CrucibleBlockEntity.FILING_BACKOFF / 20);
		long start = level.getGameTime();
		Diagnostic full = crucible.automate(level);
		helper.assertTrue(quota.equals(full) && quota.equals(crucible.waiting()) && crucible.request() == -1L && ledger.ledger().nextId() == next
				&& ledger.ledger().openFor(setter.getUUID()) == Logistics.MAX_OPEN_PER_PLAYER,
				"Its setter's sixteen are open: it says so and files nothing: " + full);
		helper.assertTrue(crucible.companion.status() == CompanionStatus.NO_INPUT && crucible.companion.needed(new ItemStack(Items.GLOWSTONE_DUST)) == 2,
				"A Peepo reads the quota wait as missing input and may still bring both");
		helper.assertTrue(ledger.cancel(fillers.getFirst(), setter.getUUID(), level.getGameTime()) == Logistics.Outcome.DONE
				&& ledger.ledger().openFor(setter.getUUID()) == Logistics.MAX_OPEN_PER_PLAYER - 1, "One of the sixteen cancelled");
		Diagnostic still = crucible.automate(level);
		helper.assertTrue(quota.equals(still) && crucible.request() == -1L && ledger.ledger().nextId() == next,
				"Room again, it waits out its backoff: " + still);
		helper.assertTrue(CrucibleBlockEntity.backoffAfter(0) == 200 && CrucibleBlockEntity.backoffAfter(200) == 400
				&& CrucibleBlockEntity.backoffAfter(400) == 800 && CrucibleBlockEntity.backoffAfter(800) == 1200
				&& CrucibleBlockEntity.backoffAfter(1200) == 1200, "The backoff doubles from 200 ticks to 1,200");
		helper.succeedWhen(() -> {
			long asked = crucible.request();
			Request open = ledger.ledger().get(asked);
			helper.assertTrue(asked > 0L && open != null && open.ticket().wanted() == 2 && open.ticket().destination().equals(here)
					&& open.ticket().filed() >= start + CrucibleBlockEntity.FILING_BACKOFF
					&& open.ticket().filed() < start + CrucibleBlockEntity.FILING_BACKOFF + CrucibleBlockEntity.AUTOMATION_TICKS
					&& ledger.ledger().openFor(setter.getUUID()) == Logistics.MAX_OPEN_PER_PLAYER
					&& crucible.waiting() != null && crucible.waiting().code() == Code.MISSING_INGREDIENT,
					"Its own pulse files once the backoff is over: request " + asked + ", " + open);
			// Broken, the crucible turns its request back and forgets its formula, so it files nothing more (the unit review).
			ledger.destinationGone(at, level.getGameTime());
			helper.setBlock(new BlockPos(3, 1, 2), Blocks.AIR);
		});
	}

	/**
	 * C-06: breaking a crucible a courier carries for turns its request back (destination_gone): the carried cargo goes
	 * back to its chest, never dropped; the crucible's own slots drop once, as before.
	 */
	@GameTest(maxTicks = 20)
	public void aBrokenCrucibleTurnsItsRequestBack(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer setter = alembist(helper);
		post(helper, setter, new BlockPos(2, 1, 2));
		ChestBlockEntity chest = chest(helper, new BlockPos(1, 1, 2));
		chest.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, 10));
		BlockPos at = new BlockPos(3, 1, 2);
		CrucibleBlockEntity crucible = crucible(helper, at);
		crucible.setItem(1, new ItemStack(Items.SPIDER_EYE, 3));
		long id = waiting(helper, crucible, setter, TWO_GLOW);
		CourierLedger ledger = CourierLedger.of(level.getServer());
		ClockworkPorterEntity porter = courier(helper, setter, new BlockPos(2, 1, 2));
		long now = level.getGameTime() + 1_200_000L;
		porter.think(level, NIGHT, now);
		porter.think(level, NIGHT, now + 20);
		helper.assertTrue(ledger.ledger().get(id).progress().carried() == 2 && matching(chest, PLAIN) == 8, "A courier carries two for it");
		conserved(helper, ledger, List.of(id), PLAIN, 10, "carried", chest, crucible);
		helper.destroyBlock(at);
		Request back = ledger.ledger().get(id);
		helper.assertTrue(back != null && back.state() == RequestState.RETURNING && back.progress().carried() == 2
				&& "destination_gone".equals(back.progress().note()), "Broken: its request turns back with the two carried: " + back);
		int eyes = 0;
		int glowstone = 0;
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(at)).inflate(3.0))) {
			eyes += item.getItem().is(Items.SPIDER_EYE) ? item.getItem().getCount() : 0;
			glowstone += item.getItem().is(Items.GLOWSTONE_DUST) ? item.getItem().getCount() : 0;
		}
		helper.assertTrue(eyes == 3 && glowstone == 0, "Its slots drop once; the carried glowstone is not dropped: " + eyes + " eyes, " + glowstone + " glowstone");
		conserved(helper, ledger, List.of(id), PLAIN, 10, "broken", chest);
		porter.think(level, NIGHT, now + 40);
		helper.assertTrue(ledger.ledger().get(id) == null && matching(chest, PLAIN) == 10, "The two are back in the chest");
		conserved(helper, ledger, List.of(id), PLAIN, 10, "returned", chest);
		helper.succeed();
	}

	/**
	 * C-06, 03:193's "destination fills": a full crucible takes what fits; the rest stays aboard, noted "full", and the
	 * courier says so; with room again it is delivered, once. Never into its bottle or output slots.
	 */
	@GameTest(maxTicks = 20)
	public void aCourierFillsACrucibleUntilItIsFull(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer setter = alembist(helper);
		CourierPostBlockEntity post = post(helper, setter, new BlockPos(2, 1, 2));
		ChestBlockEntity chest = chest(helper, new BlockPos(1, 1, 2));
		chest.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, 10));
		CrucibleBlockEntity crucible = crucible(helper, new BlockPos(3, 1, 2));
		long id = waiting(helper, crucible, setter, TWO_GLOW);
		CourierLedger ledger = CourierLedger.of(level.getServer());
		Place at = Couriers.place(level, post.getBlockPos());
		ClockworkPorterEntity porter = courier(helper, setter, new BlockPos(2, 1, 2));
		long now = level.getGameTime() + 1_400_000L;
		porter.think(level, NIGHT, now);
		porter.think(level, NIGHT, now + 20);
		helper.assertTrue(ledger.ledger().get(id).progress().carried() == 2, "A courier carries two for it");
		for (int slot = 0; slot < 4; slot++) {
			crucible.setItem(slot, new ItemStack(Items.SPIDER_EYE, 64));
		}
		crucible.setItem(4, new ItemStack(Items.GLOWSTONE_DUST, 63));
		conserved(helper, ledger, List.of(id), PLAIN, 73, "filled", chest, crucible);
		helper.assertTrue(porter.think(level, NIGHT, now + 40) == Status.WORKING, "It delivers what fits");
		Request part = ledger.ledger().get(id);
		helper.assertTrue(part != null && part.progress().delivered() == 1 && part.progress().carried() == 1 && part.progress().aboard()
				&& "full".equals(part.progress().note()) && matching(crucible, PLAIN) == 64, "One fitted; one stays aboard, noted full: " + part);
		conserved(helper, ledger, List.of(id), PLAIN, 73, "one fitted", chest, crucible);
		helper.assertTrue(porter.think(level, NIGHT, now + 60) == Status.FULL && ledger.ledger().get(id).progress().carried() == 1,
				"Full: it says so and keeps the rest");
		helper.assertTrue(crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT).isEmpty() && crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT).isEmpty(),
				"Nothing went into its bottle or output slots");
		conserved(helper, ledger, List.of(id), PLAIN, 73, "full", chest, crucible);
		crucible.setItem(0, ItemStack.EMPTY);
		helper.assertTrue(porter.think(level, NIGHT, now + 80) == Status.WORKING && ledger.ledger().get(id) == null && matching(crucible, PLAIN) == 65,
				"Room again: the last one delivered and the request closed");
		conserved(helper, ledger, List.of(id), PLAIN, 73, "delivered", chest, crucible);
		List<Event> events = ledger.ledger().history(at).stream().filter(e -> e.request() == id).toList();
		helper.assertValueEqual(events.stream().map(Event::kind).toList(), List.of("filed", "claimed", "reserved", "picked_up", "delivered", "done"),
				"the post's history of it");
		helper.assertTrue("full".equals(events.get(4).note()), "the part delivery is noted full: " + events.get(4));
		helper.succeed();
	}

	/**
	 * C-06, the unit review: a crucible whose buffer is full of other things files nothing, since a porter could only wait
	 * at it, FULL, holding its post's queue; one slot emptied, its next pulse files at once (no backoff), for what fits.
	 */
	@GameTest(maxTicks = 20)
	public void aCrucibleWithNoRoomAsksForNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer setter = alembist(helper);
		post(helper, setter, new BlockPos(2, 1, 2));
		CrucibleBlockEntity crucible = crucible(helper, new BlockPos(3, 1, 2));
		setFormula(helper, crucible, setter, TWO_GLOW);
		pour(crucible, 2);
		for (int slot = 0; slot < CrucibleBlockEntity.BUFFER_SLOTS; slot++) {
			crucible.setItem(slot, new ItemStack(Items.SPIDER_EYE));
		}
		CourierLedger ledger = CourierLedger.of(level.getServer());
		Place here = Couriers.place(level, crucible.getBlockPos());
		helper.assertTrue(crucible.automate(level) == null && crucible.step() == 1, "The water poured");
		long next = ledger.ledger().nextId();
		Diagnostic full = crucible.automate(level);
		helper.assertTrue(full != null && full.code() == Code.MISSING_INGREDIENT && crucible.request() == -1L && ledger.ledger().nextId() == next
				&& bound(ledger, here) == 0, "Five slots of spider eyes: it waits and files nothing: " + full);
		crucible.setItem(0, ItemStack.EMPTY);
		Diagnostic room = crucible.automate(level);
		long id = crucible.request();
		Request filed = ledger.ledger().get(id);
		helper.assertTrue(room != null && room.code() == Code.MISSING_INGREDIENT && id > 0L && filed != null && filed.ticket().wanted() == 2
				&& filed.ticket().requester().equals(setter.getUUID()) && filed.ticket().destination().equals(here) && bound(ledger, here) == 1,
				"One slot emptied, its next pulse files at once: " + room + ", " + filed);
		crucible.useEmpty(setter, true);
		helper.assertTrue(ledger.ledger().get(id) == null && crucible.request() == -1L, "Cleared, it leaves no request in the shared ledger");
		helper.succeed();
	}

	/**
	 * C-06, 03:193's "access removed": the crucible files only at a post its setter may use, never a nearer one of someone
	 * else's; when its post is barred to its setter, it turns the request back and asks no post it may not use.
	 */
	@GameTest(maxTicks = 20)
	public void aPostTheSetterMayNotUseIsNeverAsked(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer setter = alembist(helper);
		ServerPlayer stranger = keeper(helper);
		CourierPostBlockEntity theirs = post(helper, stranger, new BlockPos(2, 1, 2));
		CourierPostBlockEntity mine = post(helper, setter, new BlockPos(2, 1, 6));
		CrucibleBlockEntity crucible = crucible(helper, new BlockPos(3, 1, 2));
		BlockPos at = crucible.getBlockPos();
		Place here = Couriers.place(level, at);
		helper.assertTrue(Couriers.nearestPost(level, at, setter.getUUID(), CrucibleBlockEntity.POST_REACH) == mine
				&& Couriers.nearestPost(level, at, stranger.getUUID(), CrucibleBlockEntity.POST_REACH) == theirs,
				"The nearest post each may use: the setter's own, though the stranger's is nearer");
		CourierLedger ledger = CourierLedger.of(level.getServer());
		long k = ledger.ledger().nextId();
		long id = waiting(helper, crucible, setter, TWO_GLOW);
		Place minePlace = Couriers.place(level, mine.getBlockPos());
		helper.assertTrue(ledger.ledger().get(id).ticket().post().equals(minePlace) && ledger.ledger().nextId() == k + 1,
				"It asked at its setter's post, once");
		mine.setOwner(stranger.getUUID());
		Diagnostic barred = crucible.automate(level);
		helper.assertTrue(ledger.ledger().get(id) == null && crucible.request() == -1L && bound(ledger, here) == 0
				&& barred != null && barred.code() == Code.MISSING_INGREDIENT, "Its post barred to its setter: the request is turned back: " + barred);
		crucible.automate(level);
		helper.assertTrue(crucible.request() == -1L && ledger.ledger().nextId() == k + 1, "and no post it may not use is asked");
		helper.assertValueEqual(story(ledger, minePlace, id), List.of("filed", "cancelled"), "the post's history of it");
		helper.succeed();
	}

	/**
	 * C-06, 03:193's "target chunk unloads": a courier carrying for a crucible whose chunk is not loaded waits there,
	 * DESTINATION_UNLOADED, with no ledger step but its lease renewed; a restart keeps the cargo; cancelled, it goes back.
	 */
	@GameTest(maxTicks = 20)
	public void anUnloadedCrucibleWaitsAndLosesNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer setter = alembist(helper);
		CourierPostBlockEntity post = post(helper, setter, new BlockPos(2, 1, 2));
		ChestBlockEntity chest = chest(helper, new BlockPos(1, 1, 2));
		chest.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, 10));
		BlockPos away = helper.absolutePos(new BlockPos(3, 1, 2)).offset(100_000, 0, 0);
		helper.assertTrue(!level.isLoaded(away), "The crucible's chunk is not loaded");
		CourierLedger ledger = CourierLedger.of(level.getServer());
		Place at = Couriers.place(level, post.getBlockPos());
		// Stands for a request the crucible filed while it was loaded.
		Logistics.Filed filed = ledger.file(setter.getUUID(), at, Couriers.place(level, away), CrucibleBlockEntity.sample(RAW), 2, level.registryAccess(),
				level.getGameTime());
		helper.assertTrue(filed.outcome() == Logistics.Outcome.DONE, "Filed: " + filed);
		long id = filed.id();
		ClockworkPorterEntity porter = courier(helper, setter, new BlockPos(2, 1, 2));
		long now = level.getGameTime() + 1_300_000L;
		porter.think(level, NIGHT, now);
		porter.think(level, NIGHT, now + 20);
		helper.assertTrue(ledger.ledger().get(id).state() == RequestState.IN_TRANSIT && ledger.ledger().get(id).progress().carried() == 2
				&& matching(chest, PLAIN) == 8, "Two picked up for it");
		conserved(helper, ledger, List.of(id), PLAIN, 10, "in transit", chest);
		for (int i = 1; i <= 2; i++) {
			long then = now + 20 + 20L * i;
			Status waits = porter.think(level, NIGHT, then);
			Request held = ledger.ledger().get(id);
			helper.assertTrue(waits == Status.DESTINATION_UNLOADED && held.progress().carried() == 2 && held.progress().delivered() == 0
					&& held.progress().aboard() && held.progress().lease() == then + Logistics.LEASE_TICKS,
					"It waits with the cargo aboard, its claim renewed: " + waits + ", " + held.progress());
			conserved(helper, ledger, List.of(id), PLAIN, 10, "unloaded " + i, chest);
		}
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		saved.store("ledger", CourierLedger.CODEC, ledger);
		CourierLedger restarted = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult())
				.read("ledger", CourierLedger.CODEC).orElseThrow();
		helper.assertTrue(restarted.ledger().get(id) != null && restarted.ledger().get(id).state() == RequestState.IN_TRANSIT
				&& restarted.ledger().get(id).progress().carried() == 2 && ItemVariant.of(new ItemStack(Items.GLOWSTONE_DUST)).equals(restarted.item(id))
				&& restarted.problems().isEmpty(), "A restart keeps the cargo, counted once");
		helper.assertTrue(ledger.cancel(id, setter.getUUID(), now + 70) == Logistics.Outcome.DONE && ledger.ledger().get(id).state() == RequestState.RETURNING,
				"Cancelled: it is taken back");
		porter.think(level, NIGHT, now + 80);
		helper.assertTrue(ledger.ledger().get(id) == null && matching(chest, PLAIN) == 10, "All ten back in the chest");
		conserved(helper, ledger, List.of(id), PLAIN, 10, "returned", chest);
		helper.succeed();
	}

	/**
	 * C-06, 03:193's "entity dies": a porter discarded with cargo aboard strands it, counted, never dropped; the Peepo port
	 * no longer counts it (no courier holds it); its requester recovers it exactly once; the crucible then asks again.
	 */
	@GameTest(maxTicks = 20)
	public void aDiscardedPorterStrandsItsCargoOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer setter = alembist(helper);
		post(helper, setter, new BlockPos(2, 1, 2));
		ChestBlockEntity chest = chest(helper, new BlockPos(1, 1, 2));
		chest.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, 10));
		BlockPos at = new BlockPos(3, 1, 2);
		CrucibleBlockEntity crucible = crucible(helper, at);
		long id = waiting(helper, crucible, setter, TWO_GLOW);
		CourierLedger ledger = CourierLedger.of(level.getServer());
		ClockworkPorterEntity porter = courier(helper, setter, new BlockPos(2, 1, 2));
		long now = level.getGameTime() + 1_500_000L;
		porter.think(level, NIGHT, now);
		porter.think(level, NIGHT, now + 20);
		helper.assertTrue(ledger.ledger().get(id).progress().carried() == 2, "A courier carries two for it");
		BlockPos where = porter.blockPosition();
		porter.discard();
		Request stranded = ledger.ledger().get(id);
		helper.assertTrue(stranded != null && stranded.state() == RequestState.STRANDED && stranded.progress().carried() == 2
				&& "worker_removed".equals(stranded.progress().note()), "Its courier gone: the two are stranded, counted: " + stranded);
		int dropped = 0;
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(where).inflate(3.0))) {
			dropped += item.getItem().is(Items.GLOWSTONE_DUST) ? item.getItem().getCount() : 0;
		}
		helper.assertTrue(dropped == 0, "Nothing dropped where it stood: " + dropped);
		helper.assertTrue(crucible.courierRemainder(RAW) == 0 && crucible.companion.needed(new ItemStack(Items.GLOWSTONE_DUST)) == 2,
				"No courier holds it: a Peepo may bring both");
		conserved(helper, ledger, List.of(id), PLAIN, 10, "stranded", chest, crucible);
		helper.assertTrue(ledger.recover(id, setter, now + 30) == Logistics.Outcome.DONE && setter.getInventory().countItem(Items.GLOWSTONE_DUST) == 2
				&& ledger.ledger().get(id) == null, "Its requester recovers the two");
		helper.assertTrue(ledger.recover(id, setter, now + 31) == Logistics.Outcome.NOT_FOUND && setter.getInventory().countItem(Items.GLOWSTONE_DUST) == 2,
				"and never twice");
		conserved(helper, ledger, List.of(id), PLAIN, 10, "recovered", chest, crucible, setter.getInventory());
		long k = ledger.ledger().nextId();
		Diagnostic waits = crucible.automate(level);
		long again = crucible.request();
		helper.assertTrue(waits != null && waits.code() == Code.MISSING_INGREDIENT && again > 0L && again != id && ledger.ledger().nextId() == k + 1
				&& ledger.ledger().get(again).ticket().wanted() == 2, "The request closed, it asks again for both: " + waits + ", request " + again);
		helper.setBlock(at, Blocks.AIR);
		helper.assertTrue(ledger.ledger().get(again) == null, "Broken, its request goes with it");
		helper.succeed();
	}

	/**
	 * C-06, 03:193's "owner logs out" (C-P53): a crucible whose setter is away files nothing, unless the server lets absent
	 * owners' devices act; a request filed for a setter who is away is served only while someone answers for them: the
	 * porter waits, OWNER_OFFLINE, and the Peepo port may still bring it all; with the stand-in, it is fetched.
	 */
	@GameTest(maxTicks = 20)
	public void anOfflineSetterPausesTheCourier(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = keeper(helper);
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(keeper.getUUID(), AWAY, ms) == PartyManager.Result.OK && parties.accept(AWAY, ms) == PartyManager.Result.OK,
					"The keeper and the absent setter form a party");
			post(helper, keeper, new BlockPos(2, 1, 2));
			ChestBlockEntity chest = chest(helper, new BlockPos(1, 1, 2));
			chest.setItem(0, new ItemStack(Items.GLOWSTONE_DUST, 10));
			CrucibleBlockEntity crucible = crucible(helper, new BlockPos(3, 1, 2));
			crucible.follow(Formula.parse(TWO_GLOW), AWAY);
			pour(crucible, 2);
			CourierLedger ledger = CourierLedger.of(level.getServer());
			long next = ledger.ledger().nextId();
			Diagnostic poured = crucible.automate(level);
			Diagnostic away = crucible.automate(level);
			helper.assertTrue(poured == null && away != null && away.code() == Code.MISSING_INGREDIENT && crucible.request() == -1L
					&& ledger.ledger().nextId() == next, "Away, with the option off, it files nothing: " + away);
			withAbsentOwners(() -> crucible.automate(level));
			long id = crucible.request();
			Request open = ledger.ledger().get(id);
			helper.assertTrue(id > 0L && open != null && open.ticket().requester().equals(AWAY) && ledger.ledger().nextId() == next + 1,
					"With the server's option, it files as its absent setter: " + open);
			ClockworkPorterEntity porter = courier(helper, keeper, new BlockPos(2, 1, 2));
			long now = level.getGameTime() + 1_600_000L;
			for (long then : new long[] {now, now + 20}) {
				helper.assertTrue(porter.think(level, NIGHT, then) == Status.OWNER_OFFLINE && porter.task() == 0L
						&& ledger.ledger().get(id).state() == RequestState.OPEN && matching(chest, PLAIN) == 10
						&& crucible.companion.needed(new ItemStack(Items.GLOWSTONE_DUST)) == 2,
						"Its setter away, the porter waits and a Peepo may bring both");
				conserved(helper, ledger, List.of(id), PLAIN, 10, "offline", chest, crucible);
			}
			withAbsentOwners(() -> {
				helper.assertTrue(porter.think(level, NIGHT, now + 40) == Status.TRAVELLING && crucible.companion.needed(new ItemStack(Items.GLOWSTONE_DUST)) == 0,
						"A stand-in answers: the porter claims it, and a Peepo brings none");
				conserved(helper, ledger, List.of(id), PLAIN, 10, "claimed", chest, crucible);
				porter.think(level, NIGHT, now + 60);
				helper.assertTrue(ledger.ledger().get(id).progress().carried() == 2 && matching(chest, PLAIN) == 8, "picks up two");
				conserved(helper, ledger, List.of(id), PLAIN, 10, "carried", chest, crucible);
				porter.think(level, NIGHT, now + 80);
				helper.assertTrue(ledger.ledger().get(id) == null && matching(crucible, PLAIN) == 2 && matching(chest, PLAIN) == 8, "and delivers them");
				conserved(helper, ledger, List.of(id), PLAIN, 10, "delivered", chest, crucible);
			});
		} finally {
			parties.adminDisband(keeper.getUUID());
		}
		helper.succeed();
	}
}
