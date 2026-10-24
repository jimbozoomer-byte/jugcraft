package io.github.jimbozoomer.jugcraft.test;

import com.mojang.authlib.GameProfile;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.Saved;
import io.github.jimbozoomer.jugcraft.concordance.courier.CourierLedger;
import io.github.jimbozoomer.jugcraft.concordance.courier.CourierPostBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.courier.Couriers;
import io.github.jimbozoomer.jugcraft.concordance.logistics.RequestState;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWillLedger;
import io.github.jimbozoomer.jugcraft.concordance.rules.Evidence;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.sign.Sign;
import io.github.jimbozoomer.jugcraft.concordance.spirits.AgreementHolder;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BondingCharmItem;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BoundWills;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ClockworkPorterEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GatheringShadeEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.HearthlingEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactHistory;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PorterKeyItem;
import io.github.jimbozoomer.jugcraft.concordance.spirits.SpiritAnchorBlock;
import io.github.jimbozoomer.jugcraft.concordance.spirits.SpiritAnchorBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerRoster;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkshopHandEntity;
import io.github.jimbozoomer.jugcraft.concordance.worker.Agreement;
import io.github.jimbozoomer.jugcraft.concordance.worker.Body;
import io.github.jimbozoomer.jugcraft.concordance.worker.Bond;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import io.github.jimbozoomer.jugcraft.concordance.worker.WorkerDefinition;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;
import io.github.jimbozoomer.jugcraft.party.PartyManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Roadmap step 17, spirits, familiars and constructs (docs/features/arcane-concordance-workers.md), on a real server.
 * Each test drives a worker's decisions with {@code think} at times it chooses, standing the worker within reach so no
 * walking is needed: a familiar binds one to a person, mends them by its bond and only so often, and waits when its
 * person is offline; a spirit's agreement is sealed as a Bound Will, it gathers and delivers within its terms, says it
 * is outside them at noon and suspended when suspended, and breaking its anchor releases it; a porter says it has no
 * route, is waiting for resources, carries and delivers, spends energy and wear, is full, needs repair and is fuelled
 * by a pylon; and each worker's model survives a save. Concordance Chunk D, unit 1: a killed shade leaves its load once
 * and its holder calls another under the same agreement; a sealed shade is on the roster at once; a refused seal says why
 * and spends nothing; only the holder ends an agreement, once; a familiar fades only for its person's own play time; and
 * one pact record's upkeep warns, gives grace, suspends and resumes, never owing time away or switched off, while no one
 * binds another and no place keeps two.
 */
public class ConcordanceWorkerGameTests {
	private static final long NIGHT = 18000L;
	private static final long NOON = 6000L;
	private static final long DAY = Agreement.DAY;
	private static final long PERIOD = 7 * DAY;
	/** Gentle's numbers on the vanilla #minecraft:wool tag (D-04 brings Stolas's own tag). */
	private static final BoundWill.Upkeep UPKEEP = Pacts.terms("minecraft:wool", 1, PERIOD, DAY, 4);

	private static ServerPlayer binder(GameTestHelper helper) {
		ServerPlayer player = TestPlayers.mock(helper);
		player.setGameMode(GameType.SURVIVAL);
		return taught(player);
	}

	/** {@code player} taught the Binding Arts, with no action of theirs rate-limited. */
	private static ServerPlayer taught(ServerPlayer player) {
		ConcordanceProgress.grant(player, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(player, Workers.RESEARCH, ResearchState.UNDERSTOOD);
		RateGate.forget(player.getUUID());
		return player;
	}

	private static void place(GameTestHelper helper, Entity entity, double x, double y, double z) {
		Vec3 at = helper.absoluteVec(new Vec3(x, y, z));
		entity.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
	}

	private static WorkerDefinition.Spirit terms() {
		return Workers.catalog().get(GatheringShadeEntity.DEFINITION, WorkerDefinition.Spirit.class);
	}

	private static String key(@Nullable Component line) {
		return line != null && line.getContents() instanceof TranslatableContents contents ? contents.getKey() : "";
	}

	private static @Nullable Object arg(Component line, int index) {
		return line.getContents() instanceof TranslatableContents contents && contents.getArgs().length > index ? contents.getArgs()[index] : null;
	}

	/** Drops {@code count} wheat on {@code shade} and has it gather at night; returns what it then carries. */
	private static int gather(ServerLevel level, GatheringShadeEntity shade, long game, int count) {
		ItemEntity dropped = new ItemEntity(level, shade.getX(), shade.getY(), shade.getZ(), new ItemStack(Items.WHEAT, count));
		dropped.setPickUpDelay(0);
		level.addFreshEntity(dropped);
		shade.think(level, NIGHT, game);
		return shade.carriedCount();
	}

	private static int wheat(SpiritAnchorBlockEntity anchor) {
		return anchor.contents().stream().filter(stack -> stack.is(Items.WHEAT)).mapToInt(ItemStack::getCount).sum();
	}

	private static int loose(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds(), entity -> entity.isAlive() && entity.getItem().is(item))
				.stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	private static int shades(GameTestHelper helper, UUID holder) {
		return helper.getLevel().getEntitiesOfClass(GatheringShadeEntity.class, helper.getBounds(), s -> s.isAlive() && holder.equals(s.owner())).size();
	}

	private static int held(ServerLevel level, UUID holder) {
		return BoundWills.of(level.getServer()).ledger().heldBy(holder).size();
	}

	private static SpiritAnchorBlockEntity anchor(GameTestHelper helper, BlockPos at) {
		helper.setBlock(at, Workers.SPIRIT_ANCHOR);
		return helper.getBlockEntity(at, SpiritAnchorBlockEntity.class);
	}

	private static void beside(GameTestHelper helper, ServerPlayer player, BlockPos at) {
		Vec3 to = helper.absoluteVec(new Vec3(at.getX() + 0.5, at.getY(), at.getZ() + 2.5));
		player.snapTo(to.x, to.y, to.z, 0.0F, 0.0F);
	}

	/** An anchor at {@code at} sealed for {@code holder} at {@code now}, with {@code upkeep}; its holder stands beside it. */
	private static SpiritAnchorBlockEntity sealed(GameTestHelper helper, ServerPlayer holder, BlockPos at, BoundWill.@Nullable Upkeep upkeep, long now) {
		SpiritAnchorBlockEntity anchor = anchor(helper, at);
		beside(helper, holder, at);
		helper.assertValueEqual(anchor.holder().seal(helper.getLevel(), holder, anchor.sealing(terms(), upkeep), now).outcome(), Pacts.Outcome.DONE,
				"the agreement is sealed");
		return anchor;
	}

	private static BoundWill will(ServerLevel level, SpiritAnchorBlockEntity anchor) {
		return BoundWills.of(level.getServer()).ledger().get(anchor.agreement().record());
	}

	private static BoundWill.Upkeep upkeep(ServerLevel level, SpiritAnchorBlockEntity anchor) {
		return will(level, anchor).pact().upkeep();
	}

	private static int escrowed(ServerLevel level, UUID record) {
		return BoundWills.of(level.getServer()).escrow(record).stream().mapToInt(ItemStack::getCount).sum();
	}

	private static int wool(ServerPlayer player) {
		return player.getInventory().countItem(Items.WOOL.white());
	}

	/** Looks at the pact every 1,000 ticks (less than ABSENT_TICKS) from {@code from} to {@code to}; returns what happened. */
	private static List<Pacts.Event> stay(ServerLevel level, SpiritAnchorBlockEntity anchor, long from, long to) {
		List<Pacts.Event> events = new ArrayList<>();
		for (long t = from; t <= to; t += 1_000L) {
			Pacts.Event event = anchor.holder().look(level, t);
			if (event != Pacts.Event.NONE) {
				events.add(event);
			}
		}
		return events;
	}

	/** The Hearthling {@code person}'s Bonding Charm binds where they stand. */
	private static HearthlingEntity bindFamiliar(GameTestHelper helper, ServerPlayer person) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(BondingCharmItem.use(person, level, false).equals(InteractionResult.SUCCESS), "The charm binds a Hearthling");
		UUID id = WorkerRoster.of(level.getServer()).familiar(person.getUUID());
		helper.assertTrue(id != null && level.getEntity(id) instanceof HearthlingEntity, "Its person's roster holds it");
		return (HearthlingEntity) level.getEntity(id);
	}

	private static HearthlingEntity loadFamiliar(ServerLevel level, CompoundTag saved) {
		HearthlingEntity copy = Workers.HEARTHLING.create(level, EntitySpawnReason.LOAD);
		copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		return copy;
	}

	/** Runs {@code during} with the Concordance switched off, then puts the switch back, in the same tick. */
	private static void off(Runnable during) {
		boolean on = JugcraftConfig.isFeatureEnabled(JugcraftConcordance.FEATURE);
		JugcraftConfig.setFeatureEnabled(JugcraftConcordance.FEATURE, false);
		try {
			during.run();
		} finally {
			JugcraftConfig.setFeatureEnabled(JugcraftConcordance.FEATURE, on);
		}
	}

	// ---------------------------------------------------------------- familiars

	/**
	 * A Bonding Charm binds one Hearthling; with a bond it mends its badly hurt person once, not again within its
	 * cooldown; a second charm use recalls rather than binds; sneaking releases it; and one whose person is offline
	 * waits and says so.
	 */
	@GameTest(maxTicks = 20)
	public void aFamiliarMendsByItsBond(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer person = binder(helper);
		helper.assertTrue(BondingCharmItem.use(person, level, false).equals(InteractionResult.SUCCESS), "The charm binds a Hearthling");
		UUID id = WorkerRoster.of(level.getServer()).familiar(person.getUUID());
		helper.assertTrue(id != null && level.getEntity(id) instanceof HearthlingEntity, "Its person's roster holds it");
		HearthlingEntity familiar = (HearthlingEntity) level.getEntity(id);
		helper.assertTrue(BondingCharmItem.use(person, level, false).equals(InteractionResult.SUCCESS)
				&& WorkerRoster.of(level.getServer()).familiar(person.getUUID()).equals(id), "Used again it recalls the same familiar, never a second");
		long game = level.getGameTime() + 100_000L;
		helper.assertTrue(familiar.think(level, NIGHT, game) != Status.SUPPORTING, "Unbonded, it does not mend");
		familiar.setBond(new Bond(Bond.FIRST, 0L, 0, Long.MIN_VALUE / 2, game));
		person.setHealth(5.0F);
		helper.assertTrue(familiar.think(level, NIGHT, game + 20) == Status.SUPPORTING && person.hasEffect(MobEffects.REGENERATION),
				"Bonded, it mends its badly hurt person");
		person.removeAllEffects();
		helper.assertTrue(familiar.think(level, NIGHT, game + 40) != Status.SUPPORTING && !person.hasEffect(MobEffects.REGENERATION),
				"Not again within its cooldown");
		helper.assertTrue(BondingCharmItem.use(person, level, true).equals(InteractionResult.SUCCESS) && familiar.isRemoved()
				&& WorkerRoster.of(level.getServer()).familiar(person.getUUID()) == null, "Sneaking releases it");
		HearthlingEntity orphan = Workers.HEARTHLING.create(level, EntitySpawnReason.MOB_SUMMONED);
		UUID absent = new UUID(17L, 99L);
		orphan.setOwner(absent);
		place(helper, orphan, 2.5, 1.0, 2.5);
		level.addFreshEntity(orphan);
		WorkerRoster.of(level.getServer()).note(absent, orphan.getUUID(), "familiar", Status.IDLE, "minecraft:overworld", orphan.blockPosition());
		helper.assertTrue(orphan.think(level, NIGHT, game) == Status.OWNER_OFFLINE, "Its person offline, it waits and says so");
		helper.succeed();
	}

	/**
	 * D-S17: logged out for days, the familiar unloaded and loaded again meanwhile, its bond does not fade, though its person
	 * comes back 20 blocks away; nor does it fade for days they play while the Concordance is switched off (D-S16).
	 */
	@GameTest(maxTicks = 20)
	public void aFamiliarDoesNotFadeWhileItsPersonIsAway(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer person = taught(TestPlayers.survivor(helper));
		place(helper, person, 2.5, 1.0, 2.5);
		HearthlingEntity familiar = bindFamiliar(helper, person);
		TestPlayers.play(person, 5_000);
		long game = level.getGameTime() + 400_000L;
		familiar.setBond(new Bond(50, Long.MIN_VALUE, 0, Long.MIN_VALUE / 2, 0L, Bond.UNSEEN));
		familiar.think(level, NIGHT, game);
		helper.assertTrue(familiar.bond().strength() == 51 && familiar.bond().seenPlayed() == 5_000L,
				"Together, the bond grows and counts its person's play time from here: " + familiar.bond());
		UUID id = person.getUUID();
		TestPlayers.leave(person);
		helper.assertTrue(familiar.think(level, NIGHT, game + Bond.DAY) == Status.OWNER_OFFLINE && familiar.bond().strength() == 51,
				"Its person logged out, it waits and says so, its bond unchanged");
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		familiar.saveWithoutId(saved);
		familiar.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		HearthlingEntity reloaded = Workers.HEARTHLING.create(level, EntitySpawnReason.LOAD);
		reloaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
		level.addFreshEntity(reloaded);
		ServerPlayer back = TestPlayers.again(helper, id);
		place(helper, back, 22.5, 1.0, 2.5);
		helper.assertTrue(TestPlayers.played(back) == 5_000L && back.distanceTo(reloaded) >= 19.0F,
				"The same person comes back 20 blocks away, with their own play time: " + TestPlayers.played(back));
		reloaded.think(level, NIGHT, game + 3 * Bond.DAY);
		helper.assertValueEqual(reloaded.bond().strength(), 51, "the bond after three days logged out, the familiar unloaded meanwhile");
		off(() -> {
			TestPlayers.play(back, 2 * (int) Bond.DAY);
			helper.assertTrue(reloaded.think(level, NIGHT, game + 4 * Bond.DAY) == Status.DISABLED && reloaded.bond().strength() == 51,
					"Switched off, it waits and says so, its bond unchanged");
		});
		reloaded.think(level, NIGHT, game + 5 * Bond.DAY);
		helper.assertValueEqual(reloaded.bond().strength(), 51, "the bond after two days played while the Concordance was switched off");
		helper.succeed();
	}

	/**
	 * D-S17: days its person plays away fade it by 4 each, once, in this level or another; back beside it, that day fades it
	 * before the visit counts.
	 */
	@GameTest(maxTicks = 20)
	public void aFamiliarFadesWhileItsPersonPlaysElsewhere(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer person = taught(TestPlayers.survivor(helper));
		place(helper, person, 2.5, 1.0, 2.5);
		HearthlingEntity familiar = bindFamiliar(helper, person);
		long game = level.getGameTime() + 500_000L;
		familiar.setBond(new Bond(50, Long.MIN_VALUE, 0, Long.MIN_VALUE / 2, 0L, Bond.UNSEEN));
		familiar.think(level, NIGHT, game);
		helper.assertValueEqual(familiar.bond().strength(), 51, "Together, the bond grows");
		place(helper, person, 22.5, 1.0, 2.5);
		TestPlayers.play(person, 3 * (int) Bond.DAY);
		familiar.think(level, NIGHT, game + 40);
		helper.assertValueEqual(familiar.bond().strength(), 51 - 3 * Bond.NEGLECT, "three days played 20 blocks away fade it by 12");
		familiar.think(level, NIGHT, game + 60);
		helper.assertValueEqual(familiar.bond().strength(), 51 - 3 * Bond.NEGLECT, "once");
		ServerLevel nether = level.getServer().getLevel(Level.NETHER);
		helper.assertTrue(nether != null, "The test server has a Nether");
		person.teleport(new TeleportTransition(nether, new Vec3(0.5, 64.0, 0.5), Vec3.ZERO, 0.0F, 0.0F, TeleportTransition.DO_NOTHING));
		helper.assertTrue(person.level() == nether && familiar.think(level, NIGHT, game + 80) == Status.OTHER_DIMENSION
				&& familiar.bond().strength() == 51 - 3 * Bond.NEGLECT, "Its person in another dimension, it waits and says so");
		TestPlayers.play(person, (int) Bond.DAY);
		person.teleport(new TeleportTransition(level, helper.absoluteVec(new Vec3(3.5, 1.0, 2.5)), Vec3.ZERO, 0.0F, 0.0F, TeleportTransition.DO_NOTHING));
		familiar.think(level, NIGHT, game + 400);
		helper.assertValueEqual(familiar.bond().strength(), 51 - 4 * Bond.NEGLECT + Bond.GAIN,
				"a day played in another dimension, then back beside it: that day fades it before the visit counts");
		helper.succeed();
	}

	// ---------------------------------------------------------------- spirits

	/**
	 * A Spirit Anchor seals an agreement as a Bound Will and calls a Gathering Shade; at night it gathers a dropped item
	 * and delivers it to the anchor; at noon it is outside its agreement; suspended, it says so; the anchor broken, the
	 * Bound Will is released and the shade departs.
	 */
	@GameTest(maxTicks = 20)
	public void aSpiritKeepsToItsAgreement(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		BlockPos at = new BlockPos(3, 1, 3);
		helper.setBlock(at, Workers.SPIRIT_ANCHOR);
		SpiritAnchorBlockEntity anchor = (SpiritAnchorBlockEntity) level.getBlockEntity(helper.absolutePos(at));
		SpiritAnchorBlock.use(holder, level, anchor, false);
		helper.assertTrue(anchor.agreement() != null && anchor.spirit() != null, "Sealed");
		helper.assertTrue(BoundWills.of(level.getServer()).ledger().heldBy(holder.getUUID()).size() == 1, "as a Bound Will its holder holds");
		GatheringShadeEntity shade = (GatheringShadeEntity) level.getEntity(anchor.spirit());
		long game = level.getGameTime() + 200_000L;
		ItemEntity dropped = new ItemEntity(level, shade.getX(), shade.getY(), shade.getZ(), new ItemStack(Items.WHEAT, 3));
		dropped.setPickUpDelay(0);
		level.addFreshEntity(dropped);
		helper.assertTrue(shade.think(level, NIGHT, game) == Status.WORKING && shade.carriedCount() == 3, "At night it gathers: " + shade.status());
		Status delivered = shade.think(level, NIGHT, game + 20);
		helper.assertTrue(shade.carriedCount() == 0 && anchor.contents().stream().anyMatch(stack -> stack.is(Items.WHEAT) && stack.getCount() == 3),
				"and delivers to its anchor: " + delivered);
		helper.assertTrue(anchor.agreement().done(game + 20) == 1, "one task of its quota");
		helper.assertTrue(shade.think(level, NOON, game + 40) == Status.OUTSIDE_AGREEMENT, "At noon it is outside its agreement");
		SpiritAnchorBlock.use(holder, level, anchor, true);
		helper.assertTrue(anchor.holder().held(level) == Status.SUSPENDED && !anchor.agreement().suspended()
				&& shade.think(level, NIGHT, game + 60) == Status.SUSPENDED, "Suspended on its Bound Will, the one authority, it says so");
		helper.setBlock(at, Blocks.AIR);
		helper.assertTrue(shade.isRemoved() && BoundWills.of(level.getServer()).ledger().heldBy(holder.getUUID()).isEmpty(),
				"The anchor broken, the agreement is released and the shade departs");
		helper.succeed();
	}

	/**
	 * D-01 (D-S18, G1): killed, a shade's load goes into its loaded anchor once; it is struck off; its agreement stays and its
	 * anchor knows it is lost; a second removal leaves nothing more.
	 */
	@GameTest(maxTicks = 40)
	public void aKilledShadeLeavesItsLoadOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		SpiritAnchorBlockEntity anchor = anchor(helper, new BlockPos(3, 1, 3));
		SpiritAnchorBlock.use(holder, level, anchor, false);
		UUID first = anchor.spirit();
		GatheringShadeEntity shade = (GatheringShadeEntity) level.getEntity(first);
		helper.assertValueEqual(gather(level, shade, level.getGameTime() + 200_000L, 3), 3, "At night it gathers three");
		shade.kill(level);
		helper.runAfterDelay(25, () -> {
			helper.assertTrue(shade.isRemoved(), "Killed, it is gone once its death is over");
			helper.assertTrue(wheat(anchor) == 3 && loose(helper, Items.WHEAT) == 0 && shade.carriedCount() == 0,
					"What it carried is in its anchor, none loose: " + wheat(anchor) + ", " + loose(helper, Items.WHEAT));
			helper.assertTrue(!WorkerRoster.of(level.getServer()).workers(holder.getUUID()).containsKey(first), "It is struck off its holder's roster");
			helper.assertTrue(anchor.agreement() != null && first.equals(anchor.spirit()) && anchor.holder().spiritLost(level)
					&& held(level, holder.getUUID()) == 1, "Its agreement and Bound Will stay, and its anchor knows it is lost");
			shade.remove(Entity.RemovalReason.KILLED);
			helper.assertTrue(wheat(anchor) == 3 && loose(helper, Items.WHEAT) == 0, "A second removal leaves nothing more");
			helper.succeed();
		});
	}

	/**
	 * D-01 (D-S18, D-S16): its anchor not loaded, a killed shade's load drops where it fell, once, even switched off and with
	 * the block-drop rule off (the unit review's fix 3: its holder's things are not something mined).
	 */
	@GameTest(maxTicks = 20)
	public void aKilledShadeAwayFromItsAnchorDropsWhereItFell(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		BlockPos at = new BlockPos(1, 1, 1);
		SpiritAnchorBlockEntity anchor = anchor(helper, at);
		SpiritAnchorBlock.use(holder, level, anchor, false);
		GatheringShadeEntity shade = (GatheringShadeEntity) level.getEntity(anchor.spirit());
		helper.assertValueEqual(gather(level, shade, level.getGameTime() + 200_000L, 2), 2, "It gathers two");
		place(helper, shade, 6.5, 1.0, 6.5);
		shade.setAnchor(helper.absolutePos(at).offset(100_000, 0, 0)); // as if its anchor's chunk were not loaded
		BlockPos fell = shade.blockPosition();
		GameRules rules = level.getGameRules();
		boolean drops = rules.get(GameRules.BLOCK_DROPS);
		rules.set(GameRules.BLOCK_DROPS, false, level.getServer());
		try {
			off(() -> shade.remove(Entity.RemovalReason.KILLED));
		} finally {
			rules.set(GameRules.BLOCK_DROPS, drops, level.getServer());
		}
		int near = level.getEntitiesOfClass(ItemEntity.class, new AABB(fell).inflate(1.5), item -> item.isAlive() && item.getItem().is(Items.WHEAT))
				.stream().mapToInt(item -> item.getItem().getCount()).sum();
		helper.assertTrue(near == 2 && wheat(anchor) == 0 && shade.carriedCount() == 0,
				"Its anchor not loaded, its load drops where it fell, once, even switched off and with blocks dropping nothing: " + near);
		helper.assertTrue(!WorkerRoster.of(level.getServer()).workers(holder.getUUID()).containsKey(shade.getUUID()) && anchor.holder().spiritLost(level),
				"It is struck off its holder's roster, and its anchor knows it is lost");
		helper.succeed();
	}

	/**
	 * D-01 (D-S18): a lost shade is reported; switched off nothing is called; its holder's next use calls another, at no
	 * cost, and its Bound Will names it; a full roster refuses; an agreement whose Bound Will is gone is forgotten (D-02, D-P7).
	 */
	@GameTest(maxTicks = 20)
	public void aLostShadeIsCalledAgainUnderItsAgreement(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		ServerPlayer passer = binder(helper);
		UUID id = holder.getUUID();
		BlockPos at = new BlockPos(3, 1, 3);
		SpiritAnchorBlockEntity anchor = anchor(helper, at);
		SpiritAnchorBlock.use(holder, level, anchor, false);
		UUID record = anchor.agreement().record();
		UUID first = anchor.spirit();
		WorkerRoster roster = WorkerRoster.of(level.getServer());
		BoundWills wills = BoundWills.of(level.getServer());
		level.getEntity(first).remove(Entity.RemovalReason.KILLED);
		helper.assertTrue(anchor.holder().spiritLost(level), "Killed, its anchor knows its shade is lost");
		helper.assertValueEqual(key(SpiritAnchorBlock.use(passer, level, anchor, false)), "message.jugcraft.concordance.workers.spirit_lost",
				"and says so to anyone who reads it");
		helper.assertTrue(first.equals(anchor.spirit()) && shades(helper, id) == 0, "who calls nothing");
		off(() -> helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, anchor, false)), "message.jugcraft.concordance.disabled",
				"Switched off, nothing is called"));
		helper.assertTrue(first.equals(anchor.spirit()) && shades(helper, id) == 0, "and nothing changes");
		helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, anchor, false)), "message.jugcraft.concordance.workers.spirit_recalled",
				"Its holder's next use calls another");
		UUID second = anchor.spirit();
		helper.assertTrue(second != null && !second.equals(first) && level.getEntity(second) instanceof GatheringShadeEntity shade
				&& id.equals(shade.owner()) && helper.absolutePos(at).equals(shade.anchor()), "a new shade for the same holder at the same anchor");
		helper.assertTrue(record.equals(anchor.agreement().record()) && held(level, id) == 1, "under the same agreement and Bound Will, at no cost");
		helper.assertTrue(second.equals(wills.ledger().get(record).pact().presence()), "the Bound Will names its new presence");
		helper.assertTrue(roster.workers(id).containsKey(second) && !roster.workers(id).containsKey(first), "on the roster at once, in the lost one's place");
		helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, anchor, false)), "message.jugcraft.concordance.workers.spirit",
				"Used again, it reads its shade: no second call");
		helper.assertTrue(second.equals(anchor.spirit()) && shades(helper, id) == 1, "one shade");
		level.getEntity(second).remove(Entity.RemovalReason.KILLED);
		String dimension = level.dimension().identifier().toString();
		for (int i = 0; i < WorkerRoster.MAX_PER_PLAYER; i++) {
			roster.note(id, UUID.randomUUID(), "construct", Status.IDLE, dimension, helper.absolutePos(at));
		}
		Component crowded = SpiritAnchorBlock.use(holder, level, anchor, false);
		helper.assertTrue(key(crowded).equals("message.jugcraft.concordance.workers.recall_roster_full")
				&& Integer.valueOf(WorkerRoster.MAX_PER_PLAYER).equals(arg(crowded, 0)), "A full roster refuses the call and says so: " + crowded);
		helper.assertTrue(second.equals(anchor.spirit()) && shades(helper, id) == 0 && anchor.holder().spiritLost(level), "still lost; nothing called");
		List.copyOf(roster.workers(id).keySet()).forEach(worker -> roster.remove(id, worker));
		wills.release(record, id);
		helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, anchor, false)), "message.jugcraft.concordance.workers.sealed",
				"An agreement whose Bound Will is gone is forgotten, and the anchor may be sealed again");
		helper.assertTrue(!record.equals(anchor.agreement().record()) && held(level, id) == 1, "under a new Bound Will");
		helper.succeed();
	}

	/**
	 * D-01 (D-S18, critique 22): unloaded is not lost; one missing from the roster is, and is replaced; the old one, loading
	 * again, takes no roster room while switched off, and leaves its load in the anchor once and departs.
	 */
	@GameTest(maxTicks = 20)
	public void anUnloadedShadeReplacedLeavesOnceAndDeliversOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		UUID id = holder.getUUID();
		SpiritAnchorBlockEntity anchor = anchor(helper, new BlockPos(3, 1, 3));
		SpiritAnchorBlock.use(holder, level, anchor, false);
		GatheringShadeEntity old = (GatheringShadeEntity) level.getEntity(anchor.spirit());
		long game = level.getGameTime() + 200_000L;
		helper.assertValueEqual(gather(level, old, game, 3), 3, "It gathers three");
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		old.saveWithoutId(saved);
		old.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		WorkerRoster roster = WorkerRoster.of(level.getServer());
		helper.assertTrue(roster.workers(id).containsKey(old.getUUID()) && !anchor.holder().spiritLost(level),
				"Unloaded is not lost: it keeps its place on the roster");
		helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, anchor, false)), "message.jugcraft.concordance.workers.spirit_away",
				"and its anchor says it is away, calling nothing");
		helper.assertTrue(old.getUUID().equals(anchor.spirit()) && shades(helper, id) == 0, "nothing called");
		roster.remove(id, old.getUUID()); // as one sealed before its roster note, or while the roster was full
		helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, anchor, false)), "message.jugcraft.concordance.workers.spirit_recalled",
				"Missing from the roster it is lost, and its holder calls another");
		UUID second = anchor.spirit();
		GatheringShadeEntity back = Workers.GATHERING_SHADE.create(level, EntitySpawnReason.LOAD);
		back.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
		helper.assertTrue(back.getUUID().equals(old.getUUID()) && back.carriedCount() == 3, "The old shade loads again with its load");
		off(() -> helper.assertTrue(back.think(level, NIGHT, game + 20) == Status.DISABLED && !roster.workers(id).containsKey(old.getUUID()),
				"Switched off, it waits and takes no roster room (the unit review's fix 2)"));
		helper.assertTrue(back.think(level, NIGHT, game + 20) == Status.FINISHED && back.isRemoved() && back.carriedCount() == 0,
				"finds another in its place and leaves");
		helper.assertValueEqual(wheat(anchor), 3, "leaving its load in its anchor");
		back.remove(Entity.RemovalReason.KILLED);
		helper.assertTrue(wheat(anchor) == 3 && loose(helper, Items.WHEAT) == 0, "once: nothing more on a second removal");
		helper.assertTrue(second.equals(anchor.spirit()) && roster.workers(id).containsKey(second) && !roster.workers(id).containsKey(old.getUUID())
				&& held(level, id) == 1 && shades(helper, id) == 1, "the new shade keeps the agreement, alone");
		helper.succeed();
	}

	/** D-01 (G5): a sealed shade is on its holder's roster before it has thought; one missing from it notes itself when it next thinks. */
	@GameTest(maxTicks = 20)
	public void aSealedShadeIsOnTheRosterAtOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		SpiritAnchorBlockEntity anchor = anchor(helper, new BlockPos(3, 1, 3));
		SpiritAnchorBlock.use(holder, level, anchor, false);
		GatheringShadeEntity shade = (GatheringShadeEntity) level.getEntity(anchor.spirit());
		WorkerRoster roster = WorkerRoster.of(level.getServer());
		WorkerRoster.Entry entry = roster.workers(holder.getUUID()).get(shade.getUUID());
		helper.assertTrue(entry != null && entry.kind().equals("spirit") && entry.status().equals(Status.IDLE.id)
				&& entry.dimension().equals(level.dimension().identifier().toString()) && entry.pos() == shade.blockPosition().asLong(),
				"Sealed, the shade is on its holder's roster before it has thought: " + entry);
		roster.remove(holder.getUUID(), shade.getUUID());
		helper.assertTrue(shade.think(level, NIGHT, level.getGameTime() + 200_000L) == Status.IDLE
				&& roster.workers(holder.getUUID()).containsKey(shade.getUUID()), "One missing from it notes itself when it next thinks");
		helper.succeed();
	}

	/**
	 * D-01 (D-S11, G5): a full roster refuses the seal before anything is recorded and says why; with room it seals and the
	 * shade takes the last place at once.
	 */
	@GameTest(maxTicks = 20)
	public void aFullRosterRefusesTheSeal(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		UUID id = holder.getUUID();
		BlockPos at = new BlockPos(3, 1, 3);
		SpiritAnchorBlockEntity anchor = anchor(helper, at);
		WorkerRoster roster = WorkerRoster.of(level.getServer());
		String dimension = level.dimension().identifier().toString();
		for (int i = 0; i < WorkerRoster.MAX_PER_PLAYER; i++) {
			roster.note(id, UUID.randomUUID(), "construct", Status.IDLE, dimension, helper.absolutePos(at));
		}
		Component crowded = SpiritAnchorBlock.use(holder, level, anchor, false);
		helper.assertTrue(key(crowded).equals("message.jugcraft.concordance.workers.seal_roster_full")
				&& Integer.valueOf(WorkerRoster.MAX_PER_PLAYER).equals(arg(crowded, 0)), "A full roster refuses the seal and says why: " + crowded);
		helper.assertTrue(anchor.agreement() == null && anchor.spirit() == null && held(level, id) == 0
				&& roster.workers(id).size() == WorkerRoster.MAX_PER_PLAYER && shades(helper, id) == 0, "before anything is recorded or called");
		roster.remove(id, roster.workers(id).keySet().iterator().next());
		helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, anchor, false)), "message.jugcraft.concordance.workers.sealed", "With room, it seals");
		helper.assertTrue(anchor.spirit() != null && roster.workers(id).containsKey(anchor.spirit())
				&& roster.workers(id).size() == WorkerRoster.MAX_PER_PLAYER && held(level, id) == 1, "and the shade takes the last place at once");
		helper.succeed();
	}

	/**
	 * D-01 (G4, D-S13): a holder with the most Bound Wills is told so and nothing is sealed; a seal its sealer cannot pay for
	 * is refused before anything is called, and one they can pay for is paid once, once sealed; a second seal at a kept
	 * anchor is TAKEN and changes nothing.
	 */
	@GameTest(maxTicks = 20)
	public void aFullHolderIsToldWhy(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		UUID id = holder.getUUID();
		SpiritAnchorBlockEntity anchor = anchor(helper, new BlockPos(3, 1, 3));
		BoundWills wills = BoundWills.of(level.getServer());
		List<UUID> kept = new ArrayList<>();
		for (int i = 0; i < BoundWillLedger.MAX_PER_HOLDER; i++) {
			BoundWill will = new BoundWill(UUID.randomUUID(), "jugcraft:test_pact", "jugcraft:test_spirit", id, 0L, false);
			wills.seal(will);
			kept.add(will.id());
		}
		Component full = SpiritAnchorBlock.use(holder, level, anchor, false);
		helper.assertTrue(key(full).equals("message.jugcraft.concordance.workers.seal_holder_full")
				&& Integer.valueOf(BoundWillLedger.MAX_PER_HOLDER).equals(arg(full, 0)), "Holding the most Bound Wills, they are told so: " + full);
		helper.assertTrue(anchor.agreement() == null && anchor.spirit() == null && held(level, id) == BoundWillLedger.MAX_PER_HOLDER
				&& WorkerRoster.of(level.getServer()).workers(id).isEmpty() && shades(helper, id) == 0, "and nothing is sealed, recorded or called");
		kept.forEach(will -> wills.release(will, id));
		long now = level.getGameTime();
		int[] paid = {0};
		AgreementHolder.Sealing plain = anchor.sealing(terms(), null);
		AgreementHolder.Sealing dear = new AgreementHolder.Sealing(plain.agreement(), plain.counterpart(), plain.service(), plain.upkeep(),
				plain.beneficiaries(), plain.onePerHolder(), plain.partySeals(), player -> false, player -> paid[0]++);
		helper.assertValueEqual(anchor.holder().seal(level, holder, dear, now).outcome(), Pacts.Outcome.CANNOT_AFFORD, "A seal they cannot pay for is refused");
		helper.assertTrue(paid[0] == 0 && anchor.agreement() == null && held(level, id) == 0 && shades(helper, id) == 0
				&& AgreementHolder.told(Pacts.Outcome.CANNOT_AFFORD).getContents() instanceof TranslatableContents why
				&& why.getKey().equals("message.jugcraft.concordance.workers.seal_cannot_afford"), "and says so, before anything is spent, recorded or called");
		SpiritAnchorBlockEntity other = anchor(helper, new BlockPos(5, 1, 5));
		AgreementHolder.Sealing paying = new AgreementHolder.Sealing(plain.agreement(), plain.counterpart(), plain.service(), plain.upkeep(),
				plain.beneficiaries(), plain.onePerHolder(), plain.partySeals(), player -> true, player -> paid[0]++);
		helper.assertTrue(other.holder().seal(level, holder, paying, now).outcome() == Pacts.Outcome.DONE && paid[0] == 1 && held(level, id) == 1,
				"One they can pay for is sealed and paid once");
		helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, anchor, false)), "message.jugcraft.concordance.workers.sealed", "With fewer, it seals");
		ServerPlayer stranger = binder(helper);
		UUID spirit = anchor.spirit();
		helper.assertValueEqual(anchor.seal(level, stranger, terms(), level.getGameTime()).outcome(), Pacts.Outcome.TAKEN,
				"A second seal at a kept anchor is refused as taken");
		helper.assertTrue(spirit.equals(anchor.spirit()) && id.equals(anchor.agreement().holder()) && held(level, stranger.getUUID()) == 0
				&& paid[0] == 1, "and changes nothing");
		helper.succeed();
	}

	/**
	 * D-01 (G6, E1, E4): neither another player, nor a stand-in, nor its holder while switched off ends it; its holder ends it
	 * without breaking the anchor, once; the anchor may be sealed again.
	 */
	@GameTest(maxTicks = 20)
	public void theHolderEndsWithoutBreaking(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		ServerPlayer other = binder(helper);
		BlockPos at = new BlockPos(3, 1, 3);
		SpiritAnchorBlockEntity anchor = anchor(helper, at);
		SpiritAnchorBlock.use(holder, level, anchor, false);
		GatheringShadeEntity shade = (GatheringShadeEntity) level.getEntity(anchor.spirit());
		UUID spirit = shade.getUUID();
		UUID record = anchor.agreement().record();
		long now = level.getGameTime();
		helper.assertValueEqual(gather(level, shade, now + 200_000L, 3), 3, "It gathers three");
		helper.assertValueEqual(anchor.holder().end(level, other, now), AgreementHolder.Ended.NOT_HOLDER, "Another player cannot end it");
		FakePlayer standIn = FakePlayer.get(level, new GameProfile(holder.getUUID(), "[Concordance]"));
		helper.assertValueEqual(anchor.holder().end(level, standIn, now), AgreementHolder.Ended.NOT_PERMITTED, "nor a stand-in with its holder's identity");
		off(() -> helper.assertValueEqual(anchor.holder().end(level, holder, now), AgreementHolder.Ended.DISABLED, "Switched off, not even its holder"));
		helper.assertTrue(anchor.agreement() != null && !shade.isRemoved() && shade.carriedCount() == 3 && held(level, holder.getUUID()) == 1,
				"So far nothing has changed");
		helper.assertValueEqual(anchor.holder().end(level, holder, now), AgreementHolder.Ended.ENDED, "Its holder ends it without breaking the anchor");
		helper.assertTrue(anchor.agreement() == null && anchor.spirit() == null && held(level, holder.getUUID()) == 0 && shade.isRemoved()
				&& !WorkerRoster.of(level.getServer()).workers(holder.getUUID()).containsKey(spirit), "its Bound Will released, its shade gone and off the roster");
		helper.assertTrue(wheat(anchor) == 3 && loose(helper, Items.WHEAT) == 0 && helper.getBlockState(at).is(Workers.SPIRIT_ANCHOR),
				"what it carried left in the anchor, which stays");
		helper.assertValueEqual(anchor.holder().end(level, holder, now), AgreementHolder.Ended.NOTHING, "A second end finds nothing");
		helper.assertTrue(AgreementHolder.end(level, null, holder, record, Pacts.ENDED, now) == AgreementHolder.Ended.NOTHING
				&& wheat(anchor) == 3 && loose(helper, Items.WHEAT) == 0, "and ending the record again returns nothing more");
		helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, anchor, false)), "message.jugcraft.concordance.workers.sealed", "The anchor may be sealed again");
		helper.succeed();
	}

	/**
	 * D-02 (E2, D-S3): due with none held, a warning and a day of grace; its end, suspended for upkeep (nothing taken, nothing
	 * accrues, its holder's word cannot lift it); an offering resumes it at once, even in the tick of a ticker's look, a whole
	 * period from then (critique 3: the time held is never charged); paid ahead, one period at a time.
	 */
	@GameTest(maxTicks = 20)
	public void upkeepWarnsThenSuspendsAfterGraceAndResumes(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		BoundWills wills = BoundWills.of(level.getServer());
		long t0 = level.getGameTime() + 1_000_000L;
		SpiritAnchorBlockEntity anchor = sealed(helper, holder, new BlockPos(3, 1, 3), UPKEEP, t0);
		GatheringShadeEntity shade = (GatheringShadeEntity) level.getEntity(anchor.spirit());
		UUID id = anchor.agreement().record();
		helper.assertTrue(upkeep(level, anchor).due() == t0 + PERIOD && !upkeep(level, anchor).warned(), "Sealed, its first offering falls due a period on");
		helper.assertTrue(stay(level, anchor, t0 + 1_000L, t0 + PERIOD - 1_000L).isEmpty(), "Nothing falls due before then");
		long t1 = t0 + PERIOD;
		helper.assertValueEqual(anchor.holder().look(level, t1), Pacts.Event.WARNED, "Due with none held: a warning");
		helper.assertTrue(upkeep(level, anchor).graceUntil() == t1 + DAY && Pacts.status(will(level, anchor)) == Status.UPKEEP_DUE
				&& anchor.holder().held(level) == null && shade.think(level, NIGHT, t1) != Status.UPKEEP_HELD, "and a day of grace, the service still on");
		helper.assertTrue(wills.problems().isEmpty(), "warned, the records are sound: " + wills.problems());
		helper.assertTrue(stay(level, anchor, t1 + 1_000L, t1 + DAY - 1_000L).isEmpty() && anchor.holder().held(level) == null, "Within its grace, nothing more");
		helper.assertValueEqual(anchor.holder().look(level, t1 + DAY), Pacts.Event.SUSPENDED, "The grace over, it is suspended for upkeep");
		helper.assertTrue(anchor.holder().held(level) == Status.UPKEEP_HELD && shade.think(level, NIGHT, t1 + DAY) == Status.UPKEEP_HELD, "and its shade says so");
		helper.assertTrue(anchor.isEmpty() && escrowed(level, id) == 0 && wills.problems().isEmpty(), "Nothing is taken, from the anchor or anyone");
		helper.assertTrue(stay(level, anchor, t1 + DAY + 1_000L, t1 + 6 * DAY).isEmpty() && upkeep(level, anchor).due() == t0 + PERIOD
				&& will(level, anchor).pact().hold() == BoundWill.Hold.UPKEEP, "Held, nothing accrues and nothing more falls due");
		helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, anchor, true)), "message.jugcraft.concordance.workers.held_for_upkeep",
				"Its holder's word does not lift an upkeep hold");
		helper.assertTrue(will(level, anchor).pact().hold() == BoundWill.Hold.UPKEEP, "it stays held");
		long t2 = t1 + 6 * DAY + 500L;
		helper.assertTrue(anchor.holder().look(level, t2) == Pacts.Event.NONE && upkeep(level, anchor).lookedAt() == t2,
				"A ticker's look in the offering's tick (the unit review's fix 7)");
		ItemStack offered = new ItemStack(Items.WOOL.white(), 6);
		helper.assertValueEqual(anchor.holder().deposit(level, holder, offered, t2), 4, "An offering fills its escrow to its cap");
		helper.assertTrue(offered.getCount() == 2 && anchor.holder().held(level) == null && escrowed(level, id) == 3
				&& upkeep(level, anchor).due() == t2 + PERIOD && !upkeep(level, anchor).warned(),
				"takes one period and resumes it at once, the next a whole period on: " + upkeep(level, anchor));
		helper.assertTrue(shade.think(level, NIGHT, t2) != Status.UPKEEP_HELD, "its shade works again");
		helper.assertTrue(stay(level, anchor, t2 + 1_000L, t2 + PERIOD - 1_000L).isEmpty(), "Paid ahead, nothing falls due early");
		helper.assertValueEqual(anchor.holder().look(level, t2 + PERIOD), Pacts.Event.PAID, "At the next period one offering is taken");
		helper.assertTrue(escrowed(level, id) == 2 && upkeep(level, anchor).due() == t2 + 2 * PERIOD && wills.problems().isEmpty(),
				"one only, the next a period on");
		helper.succeed();
	}

	/**
	 * D-02 (E2, D-S3, critique 2a): a holder away while warned, or away past both due date and grace, is warned again at their
	 * first look back with the whole grace; never suspended at once, even when they suspend and resume it before that look.
	 */
	@GameTest(maxTicks = 20)
	public void aReturnAfterAbsenceGetsTheFullGrace(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = taught(TestPlayers.survivor(helper));
		UUID id = holder.getUUID();
		long t0 = level.getGameTime() + 1_000_000L;
		BlockPos first = new BlockPos(2, 1, 2);
		SpiritAnchorBlockEntity anchor = sealed(helper, holder, first, UPKEEP, t0);
		helper.assertTrue(stay(level, anchor, t0 + 1_000L, t0 + PERIOD - 1_000L).isEmpty(), "Nothing due before its period");
		long t1 = t0 + PERIOD;
		helper.assertTrue(anchor.holder().look(level, t1) == Pacts.Event.WARNED && upkeep(level, anchor).graceUntil() == t1 + DAY, "Warned, with a day of grace");
		TestPlayers.leave(holder);
		helper.assertTrue(stay(level, anchor, t1 + 1_000L, t1 + 10 * DAY).isEmpty() && anchor.holder().held(level) == null
				&& upkeep(level, anchor).graceUntil() == t1 + DAY, "Its holder away ten days, nothing is looked at: no suspension");
		ServerPlayer back = taught(TestPlayers.again(helper, id));
		beside(helper, back, first);
		long t2 = t1 + 10 * DAY + 500L;
		helper.assertValueEqual(anchor.holder().look(level, t2), Pacts.Event.WARNED, "Back, they are warned again");
		helper.assertTrue(upkeep(level, anchor).graceUntil() == t2 + DAY && anchor.holder().held(level) == null, "with the whole day of grace from their return");
		helper.assertTrue(stay(level, anchor, t2 + 1_000L, t2 + DAY - 1_000L).isEmpty(), "and nothing within it");
		helper.assertValueEqual(anchor.holder().look(level, t2 + DAY), Pacts.Event.SUSPENDED, "Still unpaid at its end, it is suspended; nothing is lost");
		BlockPos second = new BlockPos(5, 1, 2);
		long t3 = t2 + 2 * DAY;
		SpiritAnchorBlockEntity other = sealed(helper, back, second, UPKEEP, t3);
		helper.assertTrue(stay(level, other, t3 + 1_000L, t3 + DAY).isEmpty(), "A second agreement, a day attended");
		TestPlayers.leave(back);
		ServerPlayer again = taught(TestPlayers.again(helper, id));
		beside(helper, again, second);
		long tr = t3 + 21 * DAY;
		helper.assertValueEqual(other.holder().look(level, tr), Pacts.Event.WARNED, "Away past its due date and past a grace, the first look back warns");
		helper.assertTrue(upkeep(level, other).graceUntil() == tr + DAY && other.holder().held(level) == null, "with the whole grace: never suspended at once");
		UUID otherRecord = other.agreement().record();
		TestPlayers.leave(again);
		ServerPlayer later = taught(TestPlayers.again(helper, id));
		beside(helper, later, second);
		long ts = tr + 3 * DAY; // away past that grace too
		helper.assertTrue(AgreementHolder.toggle(level, later, otherRecord, ts) == AgreementHolder.Toggle.SUSPENDED
				&& AgreementHolder.toggle(level, later, otherRecord, ts + 100L) == AgreementHolder.Toggle.RESUMED,
				"Back, before any look, they suspend it and resume it");
		helper.assertTrue(other.holder().look(level, ts + 120L) == Pacts.Event.WARNED && upkeep(level, other).graceUntil() == ts + 120L + DAY
				&& other.holder().held(level) == null,
				"The absence before their hold still counts: warned again with the whole grace, never suspended at once (the unit review's fix 1)");
		helper.succeed();
	}

	/**
	 * D-02 (E2, critique 2b): logged out, online beyond 64 blocks, in another dimension, or away while the server lets
	 * stand-ins answer for absent owners, nothing is looked at; back, at most one period falls due, and a due date that
	 * passed while nobody looked becomes the moment of return (critique 3): no debt. With no ticker in unit 1, "unloaded" is
	 * only a gap between looks here; D-04's perch tests unload the place itself (critique 16).
	 */
	@GameTest(maxTicks = 20)
	public void absentAndUnloadedTimeIsNeverOwed(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = taught(TestPlayers.survivor(helper));
		UUID id = holder.getUUID();
		BlockPos at = new BlockPos(3, 1, 3);
		long t0 = level.getGameTime() + 1_000_000L;
		SpiritAnchorBlockEntity anchor = sealed(helper, holder, at, UPKEEP, t0);
		UUID record = anchor.agreement().record();
		helper.assertValueEqual(anchor.holder().deposit(level, holder, new ItemStack(Items.WOOL.white(), 4), t0), 4, "Four periods paid ahead");
		TestPlayers.leave(holder);
		for (long t = t0 + DAY; t <= t0 + 30 * DAY; t += DAY) {
			helper.assertValueEqual(anchor.holder().look(level, t), Pacts.Event.NONE, "Logged out, nothing is looked at");
		}
		helper.assertValueEqual(escrowed(level, record), 4, "and nothing is taken");
		ServerPlayer back = taught(TestPlayers.again(helper, id));
		beside(helper, back, at);
		long tr = t0 + 30 * DAY + 500L;
		helper.assertValueEqual(anchor.holder().look(level, tr), Pacts.Event.PAID, "Back after thirty days, one period falls due");
		helper.assertTrue(escrowed(level, record) == 3 && upkeep(level, anchor).due() == tr + PERIOD, "one offering, never four; the next a period from now");
		long tu = tr + 21 * DAY; // nobody looks for three periods, as if the anchor were unloaded
		helper.assertValueEqual(anchor.holder().look(level, tu), Pacts.Event.PAID, "Unlooked at for three periods, one falls due");
		helper.assertTrue(escrowed(level, record) == 2 && upkeep(level, anchor).due() == tu + PERIOD, "one offering");
		Vec3 far = helper.absoluteVec(new Vec3(at.getX() + 70.5, at.getY(), at.getZ() + 0.5));
		back.snapTo(far.x, far.y, far.z, 0.0F, 0.0F);
		helper.assertTrue(stay(level, anchor, tu + 1_000L, tu + 3 * PERIOD).isEmpty() && escrowed(level, record) == 2,
				"Online but beyond 64 blocks, nothing is looked at");
		beside(helper, back, at);
		long tw = tu + 3 * PERIOD + 500L;
		helper.assertValueEqual(anchor.holder().look(level, tw), Pacts.Event.PAID, "Back near it, one period falls due");
		helper.assertTrue(escrowed(level, record) == 1 && upkeep(level, anchor).due() == tw + PERIOD && BoundWills.of(level.getServer()).problems().isEmpty(),
				"one offering for three periods away: no debt");
		long tx = tw + 2 * PERIOD - 1_000L; // no look since tw: an absence across tw + PERIOD
		helper.assertValueEqual(anchor.holder().look(level, tx), Pacts.Event.PAID, "Back just before the next due date, one offering");
		helper.assertTrue(upkeep(level, anchor).due() == tx + PERIOD && anchor.holder().look(level, tw + 2 * PERIOD) == Pacts.Event.NONE,
				"the next a period from the return, not 1,000 ticks later");
		BoundWill kept = will(level, anchor);
		ServerLevel nether = level.getServer().getLevel(Level.NETHER);
		helper.assertTrue(nether != null, "The test server has a Nether");
		back.teleport(new TeleportTransition(nether, new Vec3(0.5, 64.0, 0.5), Vec3.ZERO, 0.0F, 0.0F, TeleportTransition.DO_NOTHING));
		long tn = tx + PERIOD + 500L;
		helper.assertTrue(back.level() == nether && anchor.holder().look(level, tn) == Pacts.Event.NONE && will(level, anchor).equals(kept),
				"Its holder in another dimension, nothing is looked at");
		boolean answering = JugcraftConfig.option(Authority.ABSENT_OPTION);
		JugcraftConfig.setOption(Authority.ABSENT_OPTION, true);
		try {
			TestPlayers.leave(back);
			helper.assertTrue(anchor.holder().look(level, tn + PERIOD) == Pacts.Event.NONE && will(level, anchor).equals(kept),
					"Logged out while stand-ins may answer for absent owners, nothing is looked at: a stand-in never pays or is warned");
		} finally {
			JugcraftConfig.setOption(Authority.ABSENT_OPTION, answering);
		}
		helper.succeed();
	}

	/**
	 * D-02 (E1, E4, D-S6): its holder's party pays nothing into it; its holder's party, a stand-in and a holder who is not
	 * alive cannot end it; the living holder ends it and the escrow comes back once.
	 */
	@GameTest(maxTicks = 20)
	public void onlyTheHolderEnds(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		ServerPlayer friend = binder(helper);
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(holder.getUUID(), friend.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(friend.getUUID(), ms) == PartyManager.Result.OK, "The holder and a friend form a party");
			long now = level.getGameTime() + 1_000_000L;
			SpiritAnchorBlockEntity anchor = sealed(helper, holder, new BlockPos(3, 1, 3), UPKEEP, now);
			UUID record = anchor.agreement().record();
			BoundWills wills = BoundWills.of(level.getServer());
			helper.assertValueEqual(anchor.holder().deposit(level, holder, new ItemStack(Items.WOOL.white(), 2), now), 2, "Two periods paid ahead");
			helper.assertTrue(anchor.holder().deposit(level, friend, new ItemStack(Items.WOOL.white(), 1), now) == 0 && escrowed(level, record) == 2,
					"Their party pays nothing into it (the unit review's fix 5)");
			helper.assertValueEqual(anchor.holder().end(level, friend, now), AgreementHolder.Ended.NOT_HOLDER, "Their party may not end it");
			FakePlayer standIn = FakePlayer.get(level, new GameProfile(holder.getUUID(), "[Concordance]"));
			helper.assertValueEqual(anchor.holder().end(level, standIn, now), AgreementHolder.Ended.NOT_PERMITTED, "nor a stand-in with the holder's identity");
			holder.setHealth(0.0F);
			helper.assertValueEqual(anchor.holder().end(level, holder, now), AgreementHolder.Ended.NOT_PERMITTED, "nor a holder who is not alive");
			helper.assertTrue(wool(holder) == 0 && escrowed(level, record) == 2 && wills.ledger().get(record) != null,
					"the escrow is never handed to a dead holder; it stays with the record");
			holder.setHealth(holder.getMaxHealth());
			helper.assertValueEqual(anchor.holder().end(level, holder, now), AgreementHolder.Ended.ENDED, "Its living holder ends it");
			helper.assertTrue(wool(holder) == 2 && wills.ledger().get(record) == null && wills.escrow(record).isEmpty() && anchor.agreement() == null,
					"and the escrow comes back into their inventory, once");
		} finally {
			parties.adminDisband(holder.getUUID());
		}
		helper.succeed();
	}

	/**
	 * D-02 (E5, D-S10): two seals in one tick: one DONE, one TAKEN, one shade; a record kept here by a block that forgot it
	 * is left without its place, still its holder's; no place keeps two.
	 */
	@GameTest(maxTicks = 20)
	public void twoPlayersCannotBothSealOnePlace(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer first = binder(helper);
		ServerPlayer second = binder(helper);
		SpiritAnchorBlockEntity anchor = anchor(helper, new BlockPos(3, 1, 3));
		long now = level.getGameTime();
		int before = level.getEntitiesOfClass(GatheringShadeEntity.class, helper.getBounds(), Entity::isAlive).size();
		helper.assertValueEqual(anchor.seal(level, first, terms(), now).outcome(), Pacts.Outcome.DONE, "The first seals");
		AgreementHolder.Sealed late = anchor.seal(level, second, terms(), now);
		helper.assertTrue(late.outcome() == Pacts.Outcome.TAKEN && late.presence() == null, "the second, in the same tick, is refused as taken and calls nothing");
		helper.assertTrue(held(level, first.getUUID()) == 1 && held(level, second.getUUID()) == 0
				&& level.getEntitiesOfClass(GatheringShadeEntity.class, helper.getBounds(), Entity::isAlive).size() == before + 1, "One Bound Will, one shade");
		UUID kept = anchor.agreement().record();
		anchor.holder().clear(); // as a block replaced without its side effects: the record is still kept at this place
		helper.assertValueEqual(anchor.seal(level, second, terms(), now + 1).outcome(), Pacts.Outcome.DONE, "A block that knows no terms may be sealed");
		BoundWill orphan = BoundWills.of(level.getServer()).ledger().get(kept);
		helper.assertTrue(orphan != null && first.getUUID().equals(orphan.holder()) && !Pacts.seated(orphan) && !Pacts.unclaimed(orphan)
				&& orphan.pact().presence() == null, "the record it replaces stays its holder's, without its place");
		helper.assertTrue(BoundWills.of(level.getServer()).problems().isEmpty(), "and no place keeps two");
		helper.succeed();
	}

	/** D-02 (E1): a stand-in binds nobody; the actor is always the holder; no transfer or update gives a record away; another's sneaking use only reads it. */
	@GameTest(maxTicks = 20)
	public void noOneBindsAnotherPlayer(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		ServerPlayer actor = binder(helper);
		SpiritAnchorBlockEntity anchor = anchor(helper, new BlockPos(3, 1, 3));
		FakePlayer standIn = FakePlayer.get(level, new GameProfile(holder.getUUID(), "[Concordance]"));
		long now = level.getGameTime();
		helper.assertValueEqual(anchor.seal(level, standIn, terms(), now).outcome(), Pacts.Outcome.NOT_PERMITTED, "A stand-in never seals for anyone");
		helper.assertTrue(held(level, holder.getUUID()) == 0 && anchor.agreement() == null, "and binds nobody");
		helper.assertValueEqual(anchor.seal(level, actor, terms(), now).outcome(), Pacts.Outcome.DONE, "A player seals");
		BoundWills wills = BoundWills.of(level.getServer());
		BoundWill record = wills.ledger().get(anchor.agreement().record());
		helper.assertTrue(actor.getUUID().equals(record.holder()) && held(level, holder.getUUID()) == 0 && !record.transferable(),
				"for themself alone, and the record cannot change hands");
		helper.assertValueEqual(wills.ledger().transfer(record.id(), actor.getUUID(), holder.getUUID()).outcome(), BoundWillLedger.Outcome.NOT_TRANSFERABLE,
				"no transfer moves it");
		helper.assertValueEqual(wills.update(record.withHolder(holder.getUUID())).outcome(), BoundWillLedger.Outcome.NOT_HOLDER, "and no update gives it away");
		helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, anchor, true)), "message.jugcraft.concordance.workers.spirit",
				"Another player's sneaking use only reads it");
		helper.assertTrue(anchor.holder().toggle(level, holder, now) == AgreementHolder.Toggle.NOT_HOLDER
				&& wills.ledger().get(record.id()).pact().hold() == BoundWill.Hold.NONE, "and suspends nothing");
		helper.succeed();
	}

	/**
	 * D-02 (E2, D-S16): switched off, nothing falls due or is taken, its shade waits, nothing is suspended, ended or paid in,
	 * and breaking an anchor releases nothing (critique 5: its record, escrow and shade stay, and no departure is kept); on
	 * again, at most one period at the first look, and a record whose anchor was broken meanwhile is still its holder's to end.
	 */
	@GameTest(maxTicks = 20)
	public void theConcordanceOffHoldsEveryClock(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		BoundWills wills = BoundWills.of(level.getServer());
		long t0 = level.getGameTime() + 1_000_000L;
		BlockPos brokenAt = new BlockPos(6, 1, 3);
		SpiritAnchorBlockEntity other = sealed(helper, holder, brokenAt, UPKEEP, t0);
		GatheringShadeEntity otherShade = (GatheringShadeEntity) level.getEntity(other.spirit());
		UUID otherRecord = other.agreement().record();
		helper.assertValueEqual(other.holder().deposit(level, holder, new ItemStack(Items.WOOL.white(), 1), t0), 1, "One period paid ahead at a second anchor");
		BlockPos at = new BlockPos(3, 1, 3);
		SpiritAnchorBlockEntity anchor = sealed(helper, holder, at, UPKEEP, t0);
		GatheringShadeEntity shade = (GatheringShadeEntity) level.getEntity(anchor.spirit());
		UUID record = anchor.agreement().record();
		helper.assertValueEqual(anchor.holder().deposit(level, holder, new ItemStack(Items.WOOL.white(), 1), t0), 1, "One period paid ahead");
		helper.assertTrue(stay(level, anchor, t0 + 1_000L, t0 + PERIOD - 1_000L).isEmpty(), "Nothing due yet");
		BoundWill before = will(level, anchor);
		BoundWill otherBefore = wills.ledger().get(otherRecord);
		int departures = PactHistory.of(level.getServer()).history(holder.getUUID()).size();
		off(() -> {
			helper.assertTrue(stay(level, anchor, t0 + PERIOD, t0 + 3 * PERIOD).isEmpty() && will(level, anchor).equals(before)
					&& escrowed(level, record) == 1, "Switched off, nothing falls due and nothing is taken");
			helper.assertTrue(shade.think(level, NIGHT, t0 + PERIOD) == Status.DISABLED, "its shade waits, switched off");
			helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, anchor, true)), "message.jugcraft.concordance.disabled", "its holder's word waits");
			helper.assertValueEqual(anchor.holder().end(level, holder, t0 + PERIOD), AgreementHolder.Ended.DISABLED, "nothing ends");
			helper.assertValueEqual(anchor.holder().deposit(level, holder, new ItemStack(Items.WOOL.white(), 1), t0 + PERIOD), 0, "and nothing is paid in");
			helper.assertTrue(will(level, anchor).equals(before) && escrowed(level, record) == 1, "the record and its escrow untouched");
			helper.setBlock(brokenAt, Blocks.AIR);
			helper.assertTrue(otherBefore.equals(wills.ledger().get(otherRecord)) && escrowed(level, otherRecord) == 1 && !otherShade.isRemoved()
					&& PactHistory.of(level.getServer()).history(holder.getUUID()).size() == departures,
					"Broken while switched off, an anchor releases nothing: its record, escrow and shade stay, and no departure is kept");
		});
		helper.assertTrue(wills.problems().isEmpty(), "and the records stay sound: " + wills.problems());
		long back = t0 + 3 * PERIOD + 500L;
		helper.assertValueEqual(anchor.holder().look(level, back), Pacts.Event.PAID, "On again, one period falls due at the first look");
		helper.assertTrue(escrowed(level, record) == 0 && upkeep(level, anchor).due() == back + PERIOD, "one offering only, the next a period from now");
		helper.assertTrue(AgreementHolder.end(level, null, holder, otherRecord, Pacts.ENDED, back) == AgreementHolder.Ended.ENDED && wool(holder) == 1
				&& wills.ledger().get(otherRecord) == null, "The record whose anchor was broken while switched off is still its holder's to end, escrow and all");
		helper.succeed();
	}

	// ---------------------------------------------------------------- constructs

	/**
	 * A porter between two chests: no route, idle; an empty source, waiting for resources; it carries and delivers,
	 * spending energy and wear; a full target, full; worn out, it needs repair and copper mends it; out of energy, a
	 * pylon by its source fuels it.
	 */
	@GameTest(maxTicks = 20)
	public void aPorterSaysWhyItStops(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = binder(helper);
		BlockPos source = new BlockPos(1, 1, 2);
		BlockPos target = new BlockPos(3, 1, 2);
		helper.setBlock(source, Blocks.CHEST);
		helper.setBlock(target, Blocks.CHEST);
		WorkerDefinition.Construct terms = ClockworkPorterEntity.terms();
		ClockworkPorterEntity porter = Workers.CLOCKWORK_PORTER.create(level, EntitySpawnReason.MOB_SUMMONED);
		porter.setOwner(keeper.getUUID());
		porter.setBody(new Body(terms.integrity(), terms.energy()));
		place(helper, porter, 2.5, 1.0, 2.5);
		level.addFreshEntity(porter);
		long game = level.getGameTime() + 300_000L;
		helper.assertTrue(porter.think(level, NIGHT, game) == Status.IDLE, "No route: idle");
		porter.setRoute(new ClockworkPorterEntity.Route(helper.absolutePos(source), helper.absolutePos(target), level.dimension().identifier().toString()));
		helper.assertTrue(porter.think(level, NIGHT, game + 20) == Status.WAITING_FOR_RESOURCES, "An empty source: waiting for resources");
		ChestBlockEntity from = (ChestBlockEntity) level.getBlockEntity(helper.absolutePos(source));
		ChestBlockEntity to = (ChestBlockEntity) level.getBlockEntity(helper.absolutePos(target));
		from.setItem(0, new ItemStack(Items.COBBLESTONE, 5));
		helper.assertTrue(porter.think(level, NIGHT, game + 40) == Status.WORKING && porter.carriedCount() == 5, "It takes the load");
		helper.assertTrue(porter.think(level, NIGHT, game + 60) == Status.WORKING && to.getItem(0).getCount() == 5
				&& porter.body().energy() == terms.energy() - terms.tripEnergy() && porter.body().integrity() == terms.integrity() - terms.wear(),
				"and delivers it, spending energy and wear: " + porter.body());
		for (int slot = 0; slot < to.getContainerSize(); slot++) {
			to.setItem(slot, new ItemStack(Items.DIRT, 64));
		}
		from.setItem(0, new ItemStack(Items.COBBLESTONE, 3));
		porter.think(level, NIGHT, game + 80);
		helper.assertTrue(porter.think(level, NIGHT, game + 100) == Status.FULL && porter.carriedCount() == 3, "A full target: full, and it keeps its load");
		porter.setBody(new Body(0, terms.energy()));
		helper.assertTrue(porter.think(level, NIGHT, game + 120) == Status.NEEDS_REPAIR, "Worn out: needs repair");
		keeper.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.COPPER_INGOT, 2));
		keeper.interactOn(porter, net.minecraft.world.InteractionHand.MAIN_HAND, porter.position());
		helper.assertTrue(porter.body().integrity() == terms.repairAmount() && keeper.getMainHandItem().getCount() == 1, "Copper mends it");
		ClockworkPorterEntity empty = Workers.CLOCKWORK_PORTER.create(level, EntitySpawnReason.MOB_SUMMONED);
		empty.setOwner(keeper.getUUID());
		empty.setBody(new Body(terms.integrity(), 0L));
		place(helper, empty, 2.5, 1.0, 2.5);
		level.addFreshEntity(empty);
		empty.setRoute(new ClockworkPorterEntity.Route(helper.absolutePos(source), helper.absolutePos(target), level.dimension().identifier().toString()));
		helper.assertTrue(empty.think(level, NIGHT, game) == Status.NO_ENERGY, "Unfuelled, with no pylon: no energy");
		helper.setBlock(new BlockPos(1, 1, 4), JugcraftConcordance.LEY_PYLON);
		LeyPylonBlockEntity pylon = (LeyPylonBlockEntity) level.getBlockEntity(helper.absolutePos(new BlockPos(1, 1, 4)));
		pylon.setLey(level, 40);
		empty.think(level, NIGHT, game + 20);
		helper.assertTrue(empty.body().energy() == 40 && pylon.ley() == 0, "A pylon by its source fuels it: " + empty.body());
		helper.succeed();
	}

	// ---------------------------------------------------------------- saving

	/**
	 * A familiar's bond (with the play time it counted to, D-S17; one saved before reads it as unknown, and an unreadable one
	 * keeps the rest), a porter's body and route, and a shade's anchor, holder and load (G10) survive a save.
	 */
	@GameTest(maxTicks = 20)
	public void workersKeepTheirModelsThroughASave(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		HearthlingEntity familiar = Workers.HEARTHLING.create(level, EntitySpawnReason.LOAD);
		familiar.setOwner(new UUID(17L, 5L));
		familiar.setBond(new Bond(42, 3L, 7, 1000L, 2000L, 96_000L));
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		familiar.saveWithoutId(out);
		HearthlingEntity copy = Workers.HEARTHLING.create(level, EntitySpawnReason.LOAD);
		copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult()));
		helper.assertTrue(copy.bond().equals(familiar.bond()) && familiar.owner().equals(copy.owner()), "The bond and its person are saved");
		CompoundTag bondSaved = out.buildResult();
		helper.assertTrue(bondSaved.getCompoundOrEmpty("bond").getLongOr("seen_played", Bond.UNSEEN) == 96_000L && bondSaved.getIntOr(Saved.VERSION, 0) == 1,
				"The play time it counted to is saved, still at version 1");
		Bond rest = new Bond(42, 3L, 7, 1000L, 2000L);
		CompoundTag older = bondSaved.copy();
		older.getCompoundOrEmpty("bond").remove("seen_played");
		helper.assertTrue(loadFamiliar(level, older).bond().equals(rest), "A bond saved before reads the rest unchanged, its play time not yet counted");
		CompoundTag unreadable = bondSaved.copy();
		unreadable.getCompoundOrEmpty("bond").putString("seen_played", "soon");
		helper.assertTrue(loadFamiliar(level, unreadable).bond().equals(rest), "One whose play time cannot be read keeps the rest of its bond");
		CompoundTag forged = bondSaved.copy();
		forged.getCompoundOrEmpty("bond").putLong("seen_played", -50L);
		helper.assertTrue(loadFamiliar(level, forged).bond().equals(rest), "and one below none reads as none");
		helper.assertTrue(rest.neglected(5 * Bond.DAY).equals(new Bond(42, 3L, 7, 1000L, 2000L, 5 * Bond.DAY))
				&& new Bond(42, 3L, 7, 1000L, 2000L, 96_000L).neglected(10_000L).equals(new Bond(42, 3L, 7, 1000L, 2000L, 10_000L)),
				"A first count, and a play time that went back, only start the count: nothing is owed for time it never counted");
		ClockworkPorterEntity porter = Workers.CLOCKWORK_PORTER.create(level, EntitySpawnReason.LOAD);
		porter.setBody(new Body(30, 12L));
		porter.setRoute(new ClockworkPorterEntity.Route(new BlockPos(1, 2, 3), new BlockPos(4, 5, 6), "minecraft:overworld"));
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		porter.saveWithoutId(saved);
		ClockworkPorterEntity porterCopy = Workers.CLOCKWORK_PORTER.create(level, EntitySpawnReason.LOAD);
		porterCopy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
		helper.assertTrue(porterCopy.body().equals(porter.body()) && porterCopy.route().equals(porter.route()), "The body and route are saved");
		GatheringShadeEntity shade = Workers.GATHERING_SHADE.create(level, EntitySpawnReason.LOAD);
		shade.setOwner(new UUID(17L, 6L));
		shade.setAnchor(new BlockPos(7, 8, 9));
		TagValueOutput shadeOut = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		shade.saveWithoutId(shadeOut);
		CompoundTag laden = shadeOut.buildResult();
		laden.put("carried", ItemStack.CODEC.listOf().encodeStart(level.registryAccess().createSerializationContext(NbtOps.INSTANCE),
				List.of(new ItemStack(Items.WHEAT, 5))).getOrThrow());
		GatheringShadeEntity shadeCopy = Workers.GATHERING_SHADE.create(level, EntitySpawnReason.LOAD);
		shadeCopy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), laden));
		TagValueOutput again = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		shadeCopy.saveWithoutId(again);
		GatheringShadeEntity shadeBack = Workers.GATHERING_SHADE.create(level, EntitySpawnReason.LOAD);
		shadeBack.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), again.buildResult()));
		helper.assertTrue(new BlockPos(7, 8, 9).equals(shadeBack.anchor()) && new UUID(17L, 6L).equals(shadeBack.owner())
				&& shadeBack.carriedCount() == 5, "A shade's anchor, holder and load are saved: " + shadeBack.carriedCount());
		helper.succeed();
	}

	// ---------------------------------------------------------------- what it shows (roadmap step 27)

	/**
	 * A porter shows a shortage over itself when it comes to lack something (an empty source), once: wavering between
	 * working and wanting within {@code WorkerEntity.SIGN_TICKS} shows no second sign, and a working porter shows none.
	 * Its body never claims progress it is not making: on its way but not moving, it is shown standing still.
	 */
	@GameTest(maxTicks = 20)
	public void aPorterShowsOnceWhatItLacks(GameTestHelper helper) {
		SignWatch signs = new SignWatch(helper);
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = binder(helper);
		BlockPos source = new BlockPos(1, 1, 2);
		BlockPos target = new BlockPos(3, 1, 2);
		helper.setBlock(source, Blocks.CHEST);
		helper.setBlock(target, Blocks.CHEST);
		WorkerDefinition.Construct terms = ClockworkPorterEntity.terms();
		ClockworkPorterEntity porter = Workers.CLOCKWORK_PORTER.create(level, EntitySpawnReason.MOB_SUMMONED);
		porter.setOwner(keeper.getUUID());
		porter.setBody(new Body(terms.integrity(), terms.energy()));
		place(helper, porter, 2.5, 1.0, 2.5);
		level.addFreshEntity(porter);
		long game = level.getGameTime() + 300_000L;
		porter.setRoute(new ClockworkPorterEntity.Route(helper.absolutePos(source), helper.absolutePos(target), level.dimension().identifier().toString()));
		helper.assertTrue(porter.think(level, NIGHT, game) == Status.WAITING_FOR_RESOURCES, "An empty source: waiting for resources");
		helper.assertValueEqual((long) signs.of(Sign.WANT).size(), 1L, "the lack shows over the porter");
		porter.think(level, NIGHT, game + 20);
		helper.assertValueEqual((long) signs.of(Sign.WANT).size(), 1L, "still waiting: no second sign");
		ChestBlockEntity from = (ChestBlockEntity) level.getBlockEntity(helper.absolutePos(source));
		from.setItem(0, new ItemStack(Items.COBBLESTONE, 5));
		helper.assertTrue(porter.think(level, NIGHT, game + 40) == Status.WORKING, "It takes the load");
		from.setItem(0, ItemStack.EMPTY);
		porter.think(level, NIGHT, game + 60);
		porter.think(level, NIGHT, game + 80);
		helper.assertTrue(signs.of(Sign.WANT).size() == 1 && signs.of(Sign.DONE).isEmpty(),
				"Wavering between working and wanting within the sign's interval shows no more: " + signs.all());
		helper.assertTrue(WorkerEntity.shown(Status.TRAVELLING, false) == Status.IDLE && WorkerEntity.shown(Status.TRAVELLING, true) == Status.TRAVELLING
				&& WorkerEntity.shown(Status.FOLLOWING, false) == Status.IDLE && WorkerEntity.shown(Status.WORKING, false) == Status.WORKING,
				"On its way but not moving, a worker is shown still; working, it is shown working");
		signs.close();
		helper.succeed();
	}

	// ---------------------------------------------------------------- the workshop hand (Concordance Chunk D, D-08)

	/**
	 * D-08 (D-S2, D-S23): a workshop hand keeps to the terms sealed at its anchor (narrow test terms: 3 blocks, its hours, 2
	 * loads a day). Outside its hours it takes nothing and says OUTSIDE_AGREEMENT, though a claim it holds is carried to its
	 * end; a request for a crucible outside its area is refused with the same reason and left open while one inside is taken
	 * past it; its day's loads done it says FINISHED; the next day it serves again. Suspended by its holder (critique 4), it
	 * finishes the load it has claimed but claims nothing new, saying SUSPENDED (held is asked before its day's loads);
	 * resumed, it serves again. Nothing moves twice.
	 */
	@GameTest(maxTicks = 20)
	public void aRequestOutsideTheHoursOrAreaIsRefusedWithItsReason(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = WorkshopHands.holder(helper);
		CourierPostBlockEntity post = WorkshopHands.post(helper, holder, WorkshopHands.POST);
		ChestBlockEntity chest = WorkshopHands.chest(helper, WorkshopHands.CHEST, 10);
		long now = WorkshopHands.morning(level, 3_200_000L);
		WorkerDefinition.Spirit terms = WorkshopHands.terms();
		WorkshopHandEntity hand = WorkshopHands.handUnder(helper, holder,
				new WorkerDefinition.Spirit(WorkshopHandEntity.DEFINITION, terms.work(), 3, terms.from(), terms.to(), 2, terms.carry()), now);
		SpiritAnchorBlockEntity anchor = helper.getBlockEntity(WorkshopHands.ANCHOR, SpiritAnchorBlockEntity.class);
		CourierLedger ledger = CourierLedger.of(level.getServer());
		CrucibleBlockEntity inside = WorkshopHands.crucible(helper, WorkshopHands.CRUCIBLE, holder); // 3 blocks from the anchor
		long first = inside.request();
		List<Long> ids = new ArrayList<>(List.of(first));
		long in = WorkshopHands.inHours();
		long out = WorkshopHands.outOfHours();
		helper.assertTrue(hand.think(level, out, now) == Status.OUTSIDE_AGREEMENT && hand.status() == Status.OUTSIDE_AGREEMENT && hand.task() == 0L
				&& ledger.ledger().get(first).state() == RequestState.OPEN, "Outside its hours it takes nothing and says why");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "outside its hours", chest, inside, post);
		helper.assertTrue(hand.think(level, in, now + 20) == Status.TRAVELLING && hand.task() == first, "In its hours it takes the crucible's request");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "claimed", chest, inside, post);
		helper.assertTrue(hand.think(level, out, now + 40) == Status.WORKING && ledger.ledger().get(first).progress().carried() == 2,
				"A claim it holds is carried on past its hours: it picks up");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "picked up past its hours", chest, inside, post);
		helper.assertTrue(hand.think(level, out, now + 60) == Status.WORKING && ledger.ledger().get(first) == null
				&& WorkshopHands.matching(inside, WorkshopHands.PLAIN) == 2 && anchor.agreement().done(now + 60) == 1, "and delivers: one of its two loads today");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "the first delivered", chest, inside, post);
		CrucibleBlockEntity outside = WorkshopHands.crucible(helper, new BlockPos(1, 1, 1), holder); // 4 blocks from the anchor
		long far = outside.request();
		ids.add(far);
		helper.assertTrue(hand.think(level, in, now + 80) == Status.OUTSIDE_AGREEMENT && hand.task() == 0L && ledger.ledger().get(far).state() == RequestState.OPEN,
				"A request for a crucible outside its area is refused with the same reason and left open for another courier");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "outside its area", chest, inside, outside, post);
		Couriers.request(holder, level, post, new ItemStack(Items.GLOWSTONE_DUST), 2);
		long near = WorkshopHands.lastFiled(helper, WorkshopHands.POST);
		ids.add(near);
		helper.assertTrue(hand.think(level, in, now + 100) == Status.TRAVELLING && hand.task() == near && ledger.ledger().get(far).progress().worker() == null,
				"One inside its area is taken past it");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "the second claimed", chest, inside, outside, post);
		helper.assertValueEqual(hand.think(level, in, now + 120), Status.WORKING, "it picks up");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "the second picked up", chest, inside, outside, post);
		helper.assertTrue(hand.think(level, in, now + 140) == Status.WORKING && ledger.ledger().get(near) == null
				&& WorkshopHands.matching(post, WorkshopHands.PLAIN) == 2 && anchor.agreement().done(now + 140) == 2, "and delivered: its two loads for the day");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "the second delivered", chest, inside, outside, post);
		Couriers.request(holder, level, post, new ItemStack(Items.GLOWSTONE_DUST), 2);
		long late = WorkshopHands.lastFiled(helper, WorkshopHands.POST);
		ids.add(late);
		helper.assertTrue(hand.think(level, in, now + 160) == Status.FINISHED && hand.task() == 0L && ledger.ledger().get(late).state() == RequestState.OPEN,
				"Its loads done, it says FINISHED and takes nothing more today");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "finished", chest, inside, outside, post);
		long tomorrow = now + Agreement.DAY;
		helper.assertTrue(hand.think(level, in, tomorrow) == Status.TRAVELLING && hand.task() == late, "The next day it serves again");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "the next day claimed", chest, inside, outside, post);
		helper.assertValueEqual(hand.think(level, in, tomorrow + 20), Status.WORKING, "it picks up");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "the next day picked up", chest, inside, outside, post);
		helper.assertTrue(hand.think(level, in, tomorrow + 40) == Status.WORKING && ledger.ledger().get(late) == null
				&& WorkshopHands.matching(post, WorkshopHands.PLAIN) == 4, "and delivers");
		helper.assertTrue(ledger.ledger().get(far).state() == RequestState.OPEN && ledger.ledger().get(far).progress().worker() == null,
				"It never took the request outside its area");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "the next day", chest, inside, outside, post);
		// Critique 4 (D-P75): a hold stops new claims only; the load it has claimed it finishes.
		Couriers.request(holder, level, post, new ItemStack(Items.GLOWSTONE_DUST), 2);
		long kept = WorkshopHands.lastFiled(helper, WorkshopHands.POST);
		ids.add(kept);
		helper.assertTrue(hand.think(level, in, tomorrow + 60) == Status.TRAVELLING && hand.task() == kept, "Its second load today is claimed");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "claimed before the hold", chest, inside, outside, post);
		helper.assertValueEqual(anchor.holder().toggle(level, holder, tomorrow + 70), AgreementHolder.Toggle.SUSPENDED, "Its holder suspends it");
		helper.assertTrue(hand.think(level, in, tomorrow + 80) == Status.WORKING && ledger.ledger().get(kept).progress().carried() == 2,
				"Suspended, it still picks up the load it had claimed");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "picked up while suspended", chest, inside, outside, post);
		helper.assertTrue(hand.think(level, in, tomorrow + 100) == Status.WORKING && ledger.ledger().get(kept) == null
				&& WorkshopHands.matching(post, WorkshopHands.PLAIN) == 6, "and delivers it");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "delivered while suspended", chest, inside, outside, post);
		Couriers.request(holder, level, post, new ItemStack(Items.GLOWSTONE_DUST), 2);
		long waiting = WorkshopHands.lastFiled(helper, WorkshopHands.POST);
		ids.add(waiting);
		helper.assertTrue(hand.think(level, in, tomorrow + 120) == Status.SUSPENDED && hand.task() == 0L
				&& ledger.ledger().get(waiting).state() == RequestState.OPEN && ledger.ledger().get(waiting).progress().worker() == null,
				"but claims nothing new, and says it is suspended (before its day's loads, now done)");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "suspended", chest, inside, outside, post);
		helper.assertValueEqual(anchor.holder().toggle(level, holder, tomorrow + 130), AgreementHolder.Toggle.RESUMED, "Its holder resumes it");
		helper.assertTrue(hand.think(level, in, tomorrow + Agreement.DAY) == Status.TRAVELLING && hand.task() == waiting
				&& ledger.ledger().get(waiting).progress().reserved() == 2, "Resumed, it claims the waiting request on its next day");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "resumed", chest, inside, outside, post);
		helper.succeed();
	}

	/**
	 * D-08 (D-S12, D-S2): binding asks no mastery, but a workshop hand is sealed only once Stolas's Regard is mastered: never
	 * met or understood, the anchor names the reason and records, calls and notes nothing. Mastered, its own agreement is on
	 * its Bound Will (its terms, for the holder and their party) and it is on the roster at once. An anchor bound to no post
	 * still seals a shade. Mastery is asked at sealing only: the sealed hand serves on.
	 */
	@GameTest(maxTicks = 20)
	public void theHandNeedsStolasRegardMastered(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = WorkshopHands.taught(TestPlayers.survivor(helper), null);
		CourierPostBlockEntity post = WorkshopHands.post(helper, holder, WorkshopHands.POST);
		ChestBlockEntity chest = WorkshopHands.chest(helper, WorkshopHands.CHEST, 10);
		Couriers.request(holder, level, post, new ItemStack(Items.GLOWSTONE_DUST), 2);
		long id = WorkshopHands.lastFiled(helper, WorkshopHands.POST);
		List<Long> ids = List.of(id);
		SpiritAnchorBlockEntity anchor = WorkshopHands.anchor(helper, holder, WorkshopHands.ANCHOR);
		helper.assertTrue(WorkshopHands.use(helper, holder, WorkshopHands.keyFor(helper, holder, WorkshopHands.POST), WorkshopHands.ANCHOR) == InteractionResult.SUCCESS
				&& anchor.post() != null, "Binding an anchor to one's own post asks no mastery" + WorkshopHands.why(helper, holder, WorkshopHands.ANCHOR, WorkshopHands.POST));
		for (ResearchState regard : new ResearchState[] {ResearchState.NONE, ResearchState.UNDERSTOOD}) {
			if (regard != ResearchState.NONE) {
				ConcordanceProgress.grant(holder, WorkshopHandEntity.REGARD, regard);
			}
			helper.assertValueEqual(WorkshopHands.key(WorkshopHands.seal(helper, holder, anchor)), WorkshopHands.UNKNOWN,
					"Stolas's Regard " + regard.id() + ": no hand, and the reason is named");
			helper.assertTrue(anchor.agreement() == null && anchor.spirit() == null && held(level, holder.getUUID()) == 0
					&& level.getEntitiesOfClass(WorkshopHandEntity.class, helper.getBounds(), Entity::isAlive).isEmpty()
					&& WorkerRoster.of(level.getServer()).workers(holder.getUUID()).isEmpty(), "Nothing is recorded, called or noted");
			WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "refused at " + regard.id(), chest, post);
		}
		ConcordanceProgress.grant(holder, WorkshopHandEntity.REGARD, ResearchState.MASTERED);
		helper.assertValueEqual(WorkshopHands.key(WorkshopHands.seal(helper, holder, anchor)), WorkshopHands.SEALED, "Mastered, its holder seals a workshop hand");
		WorkshopHandEntity hand = level.getEntity(anchor.spirit()) instanceof WorkshopHandEntity found ? found : null;
		BoundWill will = BoundWills.of(level.getServer()).ledger().get(anchor.agreement().record());
		WorkerDefinition.Spirit terms = WorkshopHands.terms();
		helper.assertTrue(hand != null && holder.getUUID().equals(hand.owner()) && helper.absolutePos(WorkshopHands.ANCHOR).equals(hand.seat())
				&& WorkshopHandEntity.DEFINITION.equals(will.agreement()) && WorkshopHandEntity.DEFINITION.equals(will.counterpart()) && !will.transferable()
				&& Couriers.place(level, anchor.getBlockPos()).equals(will.pact().place()) && Pacts.PARTY.equals(will.pact().beneficiaries())
				&& will.pact().upkeep() == null && new BoundWill.Service("courier", terms.radius(), terms.from(), terms.to(), terms.quota()).equals(will.pact().service()),
				"Its own agreement on its Bound Will: the hand's terms, for its holder and their party: " + will);
		WorkerRoster.Entry noted = WorkerRoster.of(level.getServer()).workers(holder.getUUID()).get(hand.getUUID());
		helper.assertTrue(noted != null && "spirit".equals(noted.kind()), "On its holder's roster at once, as a spirit");
		SpiritAnchorBlockEntity plain = WorkshopHands.anchor(helper, holder, new BlockPos(4, 1, 5));
		helper.assertValueEqual(WorkshopHands.key(WorkshopHands.seal(helper, holder, plain)), "message.jugcraft.concordance.workers.sealed",
				"An anchor bound to no post still seals a Gathering Shade");
		helper.assertTrue(level.getEntity(plain.spirit()) instanceof GatheringShadeEntity, "a shade, not a hand");
		ConcordanceProgress.grant(holder, WorkshopHandEntity.REGARD, ResearchState.UNDERSTOOD);
		WorkshopHands.stand(helper, hand, 2.5, 1.0, 3.5);
		long now = WorkshopHands.morning(level, 3_300_000L);
		long in = WorkshopHands.inHours();
		helper.assertTrue(hand.think(level, in, now) == Status.TRAVELLING && hand.task() == id, "Mastery is asked at sealing only: the sealed hand serves on");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "claimed", chest, post);
		helper.assertValueEqual(hand.think(level, in, now + 20), Status.WORKING, "it picks up");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "picked up", chest, post);
		helper.assertTrue(hand.think(level, in, now + 40) == Status.WORKING && CourierLedger.of(level.getServer()).ledger().get(id) == null
				&& WorkshopHands.matching(post, WorkshopHands.PLAIN) == 2, "and delivers");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "delivered", chest, post);
		BlockPos thirdAt = new BlockPos(6, 1, 5);
		SpiritAnchorBlockEntity third = WorkshopHands.anchor(helper, holder, thirdAt);
		helper.assertTrue(WorkshopHands.use(helper, holder, WorkshopHands.keyFor(helper, holder, WorkshopHands.POST), thirdAt) == InteractionResult.SUCCESS
				&& WorkshopHands.UNKNOWN.equals(WorkshopHands.key(WorkshopHands.seal(helper, holder, third))) && third.agreement() == null,
				"but no new hand is sealed without mastery");
		helper.succeed();
	}

	/**
	 * D-08 (D-S16): switched off, a workshop hand carrying for a crucible waits, DISABLED, its claim, cargo and lease
	 * untouched; no anchor is bound, nothing sealed, no agreement ended; on again within its lease, it delivers once.
	 */
	@GameTest(maxTicks = 20)
	public void theConcordanceOffHoldsTheHand(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		WorkshopHands.Workshop shop = WorkshopHands.workshop(helper);
		WorkshopHandEntity hand = shop.hand();
		CrucibleBlockEntity crucible = shop.crucible();
		ServerPlayer holder = shop.holder();
		long id = shop.request();
		List<Long> ids = List.of(id);
		CourierLedger ledger = CourierLedger.of(level.getServer());
		BlockPos spareAt = new BlockPos(4, 1, 5);
		SpiritAnchorBlockEntity spare = WorkshopHands.anchor(helper, holder, spareAt);
		ItemStack portersKey = WorkshopHands.keyFor(helper, holder, WorkshopHands.POST);
		long now = WorkshopHands.morning(level, 3_400_000L);
		long in = WorkshopHands.inHours();
		helper.assertValueEqual(hand.think(level, in, now), Status.TRAVELLING, "The hand claims the crucible's request");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "claimed", shop.chest(), crucible);
		hand.think(level, in, now + 20);
		var carrying = ledger.ledger().get(id);
		helper.assertTrue(carrying.state() == RequestState.IN_TRANSIT && carrying.progress().carried() == 2, "The hand carries two for the crucible");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "carried", shop.chest(), crucible);
		BoundWill will = BoundWills.of(level.getServer()).ledger().get(shop.anchor().agreement().record());
		off(() -> {
			helper.assertTrue(hand.think(level, in, now + 40) == Status.DISABLED && hand.task() == id
					&& ledger.ledger().get(id).progress().equals(carrying.progress()), "Switched off, it waits: its claim, cargo and lease untouched");
			WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "switched off", shop.chest(), crucible);
			helper.assertTrue(WorkshopHands.use(helper, holder, portersKey, spareAt) == InteractionResult.FAIL && spare.post() == null, "No anchor is bound to a post");
			helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, spare, false)), "message.jugcraft.concordance.disabled", "nothing is sealed");
			helper.assertValueEqual(shop.anchor().holder().end(level, holder, now + 40), AgreementHolder.Ended.DISABLED, "and no agreement ends");
			helper.assertTrue(will.equals(BoundWills.of(level.getServer()).ledger().get(will.id())) && !hand.isRemoved() && spare.agreement() == null,
					"Its Bound Will and its hand untouched");
		});
		helper.assertTrue(hand.think(level, in, now + 60) == Status.WORKING && ledger.ledger().get(id) == null
				&& WorkshopHands.matching(crucible, WorkshopHands.PLAIN) == 2, "On again within its lease, it delivers once");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "delivered", shop.chest(), crucible);
		helper.succeed();
	}

	/**
	 * D-08 (D-P71): a workshop hand's delivery is a spirit's service, so the Binding Arts' "each kind once" is unchanged: a
	 * familiar's and the hand's are two of the three, and exactly those two are recorded; a spirit's again adds nothing; a
	 * construct's masters them.
	 */
	@GameTest(maxTicks = 20)
	public void aHandsServiceIsASpiritsService(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		WorkshopHands.Workshop shop = WorkshopHands.workshop(helper);
		ServerPlayer holder = shop.holder();
		WorkshopHandEntity hand = shop.hand();
		ConcordanceProgress.record(holder, new Evidence.Practiced(Workers.ACTIVITY, "familiar"));
		List<Long> ids = List.of(shop.request());
		long now = WorkshopHands.morning(level, 3_500_000L);
		long in = WorkshopHands.inHours();
		helper.assertValueEqual(hand.think(level, in, now), Status.TRAVELLING, "The hand claims the crucible's request");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "claimed", shop.chest(), shop.crucible());
		helper.assertValueEqual(hand.think(level, in, now + 20), Status.WORKING, "it picks up");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "picked up", shop.chest(), shop.crucible());
		helper.assertTrue(hand.think(level, in, now + 40) == Status.WORKING && CourierLedger.of(level.getServer()).ledger().get(shop.request()) == null
				&& shop.anchor().agreement().done(now + 40) == 1, "The hand delivers the crucible's two: one load");
		WorkshopHands.conserved(helper, ids, WorkshopHands.PLAIN, 10, 0, "delivered", shop.chest(), shop.crucible());
		helper.assertValueEqual(ConcordanceProgress.knowledge(holder).progress(Workers.RESEARCH).evidence().keySet(),
				Set.of("practice:" + Workers.ACTIVITY + ":familiar", "practice:" + Workers.ACTIVITY + ":spirit"),
				"the hand's delivery recorded exactly a spirit's service");
		helper.assertValueEqual(ConcordanceProgress.knowledge(holder).state(Workers.RESEARCH), ResearchState.UNDERSTOOD,
				"A familiar's and the hand's service: two of the three kinds");
		helper.assertTrue(!ConcordanceProgress.record(holder, new Evidence.Practiced(Workers.ACTIVITY, "spirit")).recorded(),
				"A spirit's again adds nothing: the hand's was a spirit's");
		helper.assertValueEqual(ConcordanceProgress.knowledge(holder).state(Workers.RESEARCH), ResearchState.UNDERSTOOD,
				"and the Binding Arts stay understood");
		ConcordanceProgress.record(holder, new Evidence.Practiced(Workers.ACTIVITY, "construct"));
		helper.assertValueEqual(ConcordanceProgress.knowledge(holder).state(Workers.RESEARCH), ResearchState.MASTERED,
				"A construct's makes the three: the Binding Arts are mastered, as before the hand");
		helper.succeed();
	}

	/**
	 * D-08 (D-P73): an anchor is bound only while it keeps no agreement, only with the Concordance on, only by a key that names
	 * a post in its level inside a hand's area; sneaking unbinds; sealed, it is neither unbound nor bound anew; a copied anchor
	 * binds nothing (CONFLICTING_AGREEMENT).
	 */
	@GameTest(maxTicks = 20)
	public void anAnchorIsBoundOnlyWhileItKeepsNoAgreement(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = WorkshopHands.holder(helper);
		WorkshopHands.post(helper, owner, WorkshopHands.POST);
		SpiritAnchorBlockEntity anchor = WorkshopHands.anchor(helper, owner, WorkshopHands.ANCHOR);
		ItemStack mine = WorkshopHands.keyFor(helper, owner, WorkshopHands.POST);
		GlobalPos bound = GlobalPos.of(level.dimension(), helper.absolutePos(WorkshopHands.POST));
		PorterKeyItem.Binding none = PorterKeyItem.bind(owner, level, anchor, new ItemStack(Workers.PORTER_KEY), false);
		helper.assertTrue(!none.changed() && key(none.told()).equals("message.jugcraft.concordance.workers.key_no_post") && anchor.post() == null,
				"A key that names no post binds nothing, and says how");
		ItemStack nether = new ItemStack(Workers.PORTER_KEY);
		nether.set(Couriers.KEY_POST, GlobalPos.of(Level.NETHER, helper.absolutePos(WorkshopHands.POST)));
		PorterKeyItem.Binding elsewhere = PorterKeyItem.bind(owner, level, anchor, nether, false);
		helper.assertTrue(!elsewhere.changed() && key(elsewhere.told()).equals("message.jugcraft.concordance.workers.post_unusable") && anchor.post() == null
				&& WorkshopHandEntity.within(anchor.getBlockPos(), anchor.getBlockPos().offset(12, -12, 12), 12)
				&& !WorkshopHandEntity.within(anchor.getBlockPos(), anchor.getBlockPos().offset(13, 0, 0), 12),
				"A post in another dimension, or past a hand's 12 blocks, binds nothing");
		// Critique 6: a hand's 12 blocks through bind itself, with posts of the owner's own straight above the anchor (the space
		// over this test's structure is no other test's), removed again.
		BlockPos tooFar = new BlockPos(2, 14, 5);
		BlockPos atTheEdge = new BlockPos(2, 13, 5);
		WorkshopHands.post(helper, owner, tooFar);
		WorkshopHands.post(helper, owner, atTheEdge);
		PorterKeyItem.Binding past = PorterKeyItem.bind(owner, level, anchor, WorkshopHands.keyFor(helper, owner, tooFar), false);
		helper.assertTrue(!past.changed() && key(past.told()).equals("message.jugcraft.concordance.workers.post_unusable") && anchor.post() == null,
				"Their own post 13 blocks above the anchor, past a hand's 12, binds nothing");
		PorterKeyItem.Binding edge = PorterKeyItem.bind(owner, level, anchor, WorkshopHands.keyFor(helper, owner, atTheEdge), false);
		helper.assertTrue(edge.changed() && key(edge.told()).equals("message.jugcraft.concordance.workers.post_bound")
				&& GlobalPos.of(level.dimension(), helper.absolutePos(atTheEdge)).equals(anchor.post()), "One 12 blocks above, at its edge, binds it");
		helper.assertTrue(PorterKeyItem.bind(owner, level, anchor, mine, true).changed() && anchor.post() == null, "and sneaking unbinds it again");
		helper.setBlock(tooFar, Blocks.AIR);
		helper.setBlock(atTheEdge, Blocks.AIR);
		off(() -> helper.assertTrue(key(PorterKeyItem.bind(owner, level, anchor, mine, false).told()).equals("message.jugcraft.concordance.disabled")
				&& anchor.post() == null, "Switched off, nothing is bound"));
		PorterKeyItem.Binding first = PorterKeyItem.bind(owner, level, anchor, mine, false);
		helper.assertTrue(first.changed() && key(first.told()).equals("message.jugcraft.concordance.workers.post_bound") && bound.equals(anchor.post()),
				"Its owner binds it to their post");
		PorterKeyItem.Binding unbound = PorterKeyItem.bind(owner, level, anchor, mine, true);
		helper.assertTrue(unbound.changed() && key(unbound.told()).equals("message.jugcraft.concordance.workers.post_unbound") && anchor.post() == null,
				"sneaking unbinds it");
		helper.assertTrue(PorterKeyItem.bind(owner, level, anchor, mine, false).changed() && bound.equals(anchor.post()), "and it binds again");
		helper.assertValueEqual(WorkshopHands.key(WorkshopHands.seal(helper, owner, anchor)), WorkshopHands.SEALED, "A hand is sealed there");
		PorterKeyItem.Binding sealedUnbind = PorterKeyItem.bind(owner, level, anchor, mine, true);
		PorterKeyItem.Binding sealedRebind = PorterKeyItem.bind(owner, level, anchor, mine, false);
		helper.assertTrue(!sealedUnbind.changed() && !sealedRebind.changed() && key(sealedUnbind.told()).equals("message.jugcraft.concordance.workers.post_sealed")
				&& key(sealedRebind.told()).equals("message.jugcraft.concordance.workers.post_sealed") && bound.equals(anchor.post()),
				"Sealed, it is neither unbound nor bound anew");
		helper.setBlock(new BlockPos(5, 1, 5), Workers.SPIRIT_ANCHOR);
		SpiritAnchorBlockEntity copy = helper.getBlockEntity(new BlockPos(5, 1, 5), SpiritAnchorBlockEntity.class);
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), anchor.saveWithoutMetadata(level.registryAccess())));
		PorterKeyItem.Binding copied = PorterKeyItem.bind(owner, level, copy, mine, true);
		helper.assertTrue(!copied.changed() && key(copied.told()).equals("compose.jugcraft.diagnostic.conflicting_agreement") && bound.equals(copy.post()),
				"A copied anchor binds and unbinds nothing: another agreement covers it");
		helper.succeed();
	}
}
