package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.Saved;
import io.github.jimbozoomer.jugcraft.concordance.celestial.Pattern;
import io.github.jimbozoomer.jugcraft.concordance.courier.Couriers;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWillLedger;
import io.github.jimbozoomer.jugcraft.concordance.rules.ConcordanceRules;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.sign.Sign;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstralClaims;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstrolabeItem;
import io.github.jimbozoomer.jugcraft.concordance.sky.Sky;
import io.github.jimbozoomer.jugcraft.concordance.spirits.AgreementHolder;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BondingCharmItem;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BoundWills;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ClockworkPorterEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GatheringShadeEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.HearthlingEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactHistory;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactMenu;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactPerchBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSessions;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.SpiritAnchorBlock;
import io.github.jimbozoomer.jugcraft.concordance.spirits.SpiritAnchorBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StolasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StolasRegard;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerRoster;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import io.github.jimbozoomer.jugcraft.concordance.worker.Agreement;
import io.github.jimbozoomer.jugcraft.concordance.worker.Body;
import io.github.jimbozoomer.jugcraft.concordance.worker.Bond;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactCatalog;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import io.github.jimbozoomer.jugcraft.concordance.worker.WorkerDefinition;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;
import io.github.jimbozoomer.jugcraft.party.PartyManager;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.IntStream;
import local.peepo.MobTransportCrate;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
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
 * binds another and no place keeps two. Concordance Chunk D, D-04: Stolas explains before anyone agrees; a stale terms
 * revision, a refusal and a second negotiation spend nothing; accepting seals once and spends once; one pact a holder;
 * nobody accepts for another; a visitor without a pact or a live conversation leaves; crates and ordinary damage cannot
 * take him; a broken definition is reported and sealed pacts keep their terms; the screen's two messages are bounded.
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

	static String key(@Nullable Component line) {
		return line != null && line.getContents() instanceof TranslatableContents contents ? contents.getKey() : "";
	}

	static @Nullable Object arg(@Nullable Component line, int index) {
		return line != null && line.getContents() instanceof TranslatableContents contents && contents.getArgs().length > index ? contents.getArgs()[index] : null;
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

	static void beside(GameTestHelper helper, ServerPlayer player, BlockPos at) {
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

	static int escrowed(ServerLevel level, UUID record) {
		return BoundWills.of(level.getServer()).escrow(record).stream().mapToInt(ItemStack::getCount).sum();
	}

	private static int wool(ServerPlayer player) {
		return player.getInventory().countItem(Items.WHITE_WOOL);
	}

	/** Looks at the pact every 1,000 ticks (less than ABSENT_TICKS) from {@code from} to {@code to}; returns what happened. */
	private static List<Pacts.Event> stay(ServerLevel level, SpiritAnchorBlockEntity anchor, long from, long to) {
		return stay(level, anchor.holder(), from, to);
	}

	/** The same for any holder block (Concordance Chunk D, unit 2: the pact perch too). */
	static List<Pacts.Event> stay(ServerLevel level, AgreementHolder holder, long from, long to) {
		List<Pacts.Event> events = new ArrayList<>();
		for (long t = from; t <= to; t += 1_000L) {
			Pacts.Event event = holder.look(level, t);
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
	static void off(Runnable during) {
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

	/** D-01 (D-S18, D-S16): its anchor not loaded, a killed shade's load drops where it fell, once, even switched off. */
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
		off(() -> shade.remove(Entity.RemovalReason.KILLED));
		int near = level.getEntitiesOfClass(ItemEntity.class, new AABB(fell).inflate(1.5), item -> item.isAlive() && item.getItem().is(Items.WHEAT))
				.stream().mapToInt(item -> item.getItem().getCount()).sum();
		helper.assertTrue(near == 2 && wheat(anchor) == 0 && shade.carriedCount() == 0,
				"Its anchor not loaded, its load drops where it fell, once, even switched off: " + near);
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
	 * again, leaves its load in the anchor once and departs.
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
	 * accrues, its holder's word cannot lift it); an offering resumes it at once, a whole period from then (critique 3: the
	 * time held is never charged); paid ahead, one period at a time.
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
		ItemStack offered = new ItemStack(Items.WHITE_WOOL, 6);
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
	 * first look back with the whole grace; never suspended at once.
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
		helper.assertValueEqual(anchor.holder().deposit(level, holder, new ItemStack(Items.WHITE_WOOL, 4), t0), 4, "Four periods paid ahead");
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

	/** D-02 (E1, E4, D-S6): its holder's party, a stand-in and a holder who is not alive cannot end it; the living holder ends it and the escrow comes back once. */
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
			helper.assertValueEqual(anchor.holder().deposit(level, holder, new ItemStack(Items.WHITE_WOOL, 2), now), 2, "Two periods paid ahead");
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
		helper.assertValueEqual(other.holder().deposit(level, holder, new ItemStack(Items.WHITE_WOOL, 1), t0), 1, "One period paid ahead at a second anchor");
		BlockPos at = new BlockPos(3, 1, 3);
		SpiritAnchorBlockEntity anchor = sealed(helper, holder, at, UPKEEP, t0);
		GatheringShadeEntity shade = (GatheringShadeEntity) level.getEntity(anchor.spirit());
		UUID record = anchor.agreement().record();
		helper.assertValueEqual(anchor.holder().deposit(level, holder, new ItemStack(Items.WHITE_WOOL, 1), t0), 1, "One period paid ahead");
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
			helper.assertValueEqual(anchor.holder().deposit(level, holder, new ItemStack(Items.WHITE_WOOL, 1), t0 + PERIOD), 0, "and nothing is paid in");
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

	// ---------------------------------------------------------------- Concordance Chunk D, unit 2: the pact hall
	// Package-private: the Sky, Sharing, Persistence and Authority suites use them; no new helper file, since a
	// shared helper file would select every client test class (tools/select_client_tests.py).

	static final String PACT_KEY = "message.jugcraft.concordance.pact.";
	static final String FULL_MOON = "jugcraft:full_moon";
	static final String HARVEST_MOON = "jugcraft:harvest_moon";
	static final String LANTERN_STAR = "jugcraft:lantern_star";
	static final String WINTER_CROWN = "jugcraft:winter_crown";
	static final String ECHO_COMET = "jugcraft:echo_comet";
	/** Midnight of world day 0 (the full moon is up), and a sky in which only the full moon is seen (as the Sky tests'). */
	static final long NIGHT_ZERO = 18000L;
	static final Function<Pattern, @Nullable String> ONLY_MOON = pattern -> pattern.id().equals(FULL_MOON) ? null : "no_sky";

	static PactDefinition stolasOffer() {
		PactDefinition stolas = PactPerchBlockEntity.definition();
		if (stolas == null) {
			throw new IllegalStateException("Stolas's offer did not load");
		}
		return stolas;
	}

	/** The Binding Arts and Stolas's Regard understood, the Lantern Star observed, no action rate-limited. */
	static ServerPlayer ready(ServerLevel level, ServerPlayer player) {
		ConcordanceProgress.grant(player, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(player, Workers.RESEARCH, ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(player, StolasRegard.RESEARCH, ResearchState.UNDERSTOOD);
		AstralClaims.of(level.getServer()).observe(player.getUUID(), LANTERN_STAR, level.getGameTime());
		RateGate.forget(player.getUUID());
		return player;
	}

	/** An ordinary survival player who understands Stolas's Regard (and the Binding Arts it needs), nothing rate-limited. */
	static ServerPlayer regarded(GameTestHelper helper) {
		ServerPlayer player = TestPlayers.survivor(helper);
		ConcordanceProgress.grant(player, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(player, Workers.RESEARCH, ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(player, StolasRegard.RESEARCH, ResearchState.UNDERSTOOD);
		RateGate.forget(player.getUUID());
		return player;
	}

	/** {@code times} compacts' materials, and an astrolabe holding {@code resonance}. */
	static void pocket(ServerPlayer player, int times, int resonance) {
		for (PactDefinition.Cost cost : stolasOffer().compact()) {
			player.getInventory().add(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(cost.item())), cost.count() * times));
		}
		ItemStack astrolabe = new ItemStack(Sky.ASTROLABE);
		AstrolabeItem.setCharge(astrolabe, resonance);
		player.getInventory().add(astrolabe);
	}

	/** Whether {@code player} carries exactly {@code times} compacts' materials. */
	static boolean pocketed(ServerPlayer player, int times) {
		for (PactDefinition.Cost cost : stolasOffer().compact()) {
			if (count(player, cost.item()) != cost.count() * times) {
				return false;
			}
		}
		return true;
	}

	static int count(ServerPlayer player, String item) {
		return player.getInventory().countItem(BuiltInRegistries.ITEM.getValue(Identifier.parse(item)));
	}

	static int resonance(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		int held = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (inventory.getItem(slot).is(Sky.ASTROLABE)) {
				held += AstrolabeItem.charge(inventory.getItem(slot));
			}
		}
		return held;
	}

	/** A perch at {@code at} placed by {@code owner} (null: set without a placer, as a command or structure sets one). */
	static PactPerchBlockEntity perch(GameTestHelper helper, BlockPos at, @Nullable ServerPlayer owner) {
		helper.setBlock(at, Workers.PACT_PERCH);
		BlockPos absolute = helper.absolutePos(at);
		if (owner != null) {
			Workers.PACT_PERCH.setPlacedBy(helper.getLevel(), absolute, helper.getLevel().getBlockState(absolute), owner, new ItemStack(Workers.PACT_PERCH_ITEM));
		}
		return helper.getBlockEntity(at, PactPerchBlockEntity.class);
	}

	static PactMenu open(GameTestHelper helper, ServerPlayer player, PactPerchBlockEntity perch) {
		PactMenu.open(player, helper.getLevel(), perch);
		helper.assertTrue(player.containerMenu instanceof PactMenu, "The perch's screen opens");
		return (PactMenu) player.containerMenu;
	}

	static @Nullable PactStatePayload request(ServerPlayer player, int containerId, int sequence, int action, int choice, int revision) {
		RateGate.forget(player.getUUID());
		return PactMenu.receive(player, new PactRequestPayload(containerId, sequence, action, choice, revision));
	}

	/** As a screen sends: the next sequence and the revision of the state it last read. */
	static @Nullable PactStatePayload send(ServerPlayer player, PactMenu menu, int action, int choice) {
		return request(player, menu.containerId, menu.lastSequence() + 1, action, choice, menu.lastSent().revision());
	}

	/** A perch at {@code at} placed by {@code player}, who stands beside it, opens it and accepts through the menu. */
	static PactMenu acceptedAt(GameTestHelper helper, ServerPlayer player, BlockPos at) {
		PactPerchBlockEntity perch = perch(helper, at, player);
		beside(helper, player, at);
		PactMenu menu = open(helper, player, perch);
		helper.assertTrue(answered(send(player, menu, PactRequestPayload.ACCEPT, 0), PactMenu.SEALED), "The pact is sealed");
		return menu;
	}

	/** Stolas's terms as the perch seals them, the compact left out (these tests are about what follows acceptance). */
	static AgreementHolder.Sealing stolasTerms() {
		return PactPerchBlockEntity.sealing(stolasOffer(), null, null);
	}

	static AgreementHolder.Sealing withService(AgreementHolder.Sealing sealing, BoundWill.Service service) {
		return new AgreementHolder.Sealing(sealing.agreement(), sealing.counterpart(), service, sealing.upkeep(), sealing.beneficiaries(),
				sealing.onePerHolder(), sealing.partySeals(), sealing.affords(), sealing.pay());
	}

	/** Stolas's pact sealed for {@code holder} at a perch they placed at {@code at}, at game time {@code now}; they stand beside it. */
	static PactPerchBlockEntity stolasPact(GameTestHelper helper, ServerPlayer holder, BlockPos at, long now) {
		PactPerchBlockEntity perch = perch(helper, at, holder);
		beside(helper, holder, at);
		helper.assertValueEqual(perch.holder().seal(helper.getLevel(), holder, stolasTerms(), now).outcome(), Pacts.Outcome.DONE,
				"Stolas's pact is sealed at the perch");
		return perch;
	}

	static BoundWill pactOf(ServerLevel level, PactPerchBlockEntity perch) {
		return BoundWills.of(level.getServer()).ledger().get(perch.holder().agreement().record());
	}

	static @Nullable StolasEntity stolas(ServerLevel level, PactPerchBlockEntity perch) {
		UUID id = perch.holder().spirit();
		return id != null && level.getEntity(id) instanceof StolasEntity found && !found.isRemoved() ? found : null;
	}

	static @Nullable BoundWill recordOf(ServerLevel level, ServerPlayer holder) {
		List<BoundWill> held = List.copyOf(BoundWills.of(level.getServer()).ledger().heldBy(holder.getUUID()));
		return held.size() == 1 ? held.getFirst() : null;
	}

	static List<StolasEntity> stolases(GameTestHelper helper) {
		return helper.getLevel().getEntitiesOfClass(StolasEntity.class, helper.getBounds(), Entity::isAlive);
	}

	static boolean answered(@Nullable PactStatePayload state, String reason) {
		return state != null && state.result().isPresent() && state.result().get().reason().equals(reason);
	}

	static String said(@Nullable PactStatePayload state) {
		return state == null ? "" : key(state.result().map(PactStatePayload.Result::message).orElse(null));
	}

	static List<String> keys(List<Component> lines) {
		return lines.stream().map(ConcordanceWorkerGameTests::key).toList();
	}

	static @Nullable Component term(PactStatePayload state, String key) {
		return state.terms().stream().filter(term -> term.key().equals(key)).map(PactStatePayload.Term::value).findFirst().orElse(null);
	}

	/** Stolas's offer as shipped (data/jugcraft/concordance/pact/stolas.json), read from the server's data. */
	static JsonObject shipped(ServerLevel level) {
		try (InputStream in = level.getServer().getResourceManager().getResourceOrThrow(Jugcraft.id("concordance/pact/stolas.json")).open();
				Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	// ---------------------------------------------------------------- Concordance Chunk D, D-04: the pact perch, Stolas and his offer

	/** D-04 (D-S7, D-S14, D-S15): ready, at their own perch, its owner is sent Stolas's three beats and every term at revision 1; Stolas arrives as a visitor bound to no one; nothing is stored, noted or spent; asking to hear the terms answers with the state alone. */
	@GameTest(maxTicks = 20)
	public void stolasExplainsBeforeAgreeing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PactDefinition stolas = stolasOffer();
		ServerPlayer owner = ready(level, TestPlayers.survivor(helper));
		pocket(owner, 1, AstrolabeItem.CAPACITY);
		BlockPos at = new BlockPos(3, 1, 3);
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu menu = open(helper, owner, perch);
		PactStatePayload state = menu.lastSent();
		helper.assertTrue(state != null && state.containerId() == menu.containerId && state.revision() == 1
				&& state.mode() == PactStatePayload.NEGOTIATE && state.result().isEmpty(), "Opened, the screen is sent its first state at revision 1: " + state);
		helper.assertValueEqual(keys(state.beats()), stolas.beats().stream().map(beat -> PACT_KEY + "stolas.beat." + beat).toList(), "his three beats, in order");
		helper.assertValueEqual(state.terms().stream().map(PactStatePayload.Term::key).toList(), PactStatePayload.TERMS,
				"every term, in order, before anything is agreed");
		helper.assertTrue(Integer.valueOf(stolas.radius()).equals(arg(term(state, "area"), 0))
				&& Integer.valueOf(stolas.quota()).equals(arg(term(state, "quota"), 0))
				&& Long.valueOf(stolas.period() / Agreement.DAY).equals(arg(term(state, "upkeep"), 2))
				&& Integer.valueOf(stolas.cap()).equals(arg(term(state, "upkeep"), 3))
				&& Long.valueOf(stolas.grace() / Agreement.DAY).equals(arg(term(state, "grace"), 0))
				&& Integer.valueOf(stolas.resonance()).equals(arg(term(state, "compact"), 0))
				&& state.resonance() == stolas.resonance() && state.compact().size() == stolas.compact().size(),
				"with the definition's numbers: the area, the quota, the period, the cap, the grace, the compact");
		helper.assertTrue(state.offers(PactRequestPayload.ACCEPT) && state.offers(PactRequestPayload.DECLINE) && state.notes().isEmpty()
				&& state.patterns().isEmpty() && state.offerings().isEmpty(), "Ready, they may accept or decline; nothing is missing");
		List<StolasEntity> visitors = stolases(helper);
		helper.assertTrue(visitors.size() == 1 && visitors.getFirst().owner() == null && visitors.getFirst().getUUID().equals(perch.visitor())
				&& helper.absolutePos(at).equals(visitors.getFirst().perch()), "Stolas arrives at the perch: a visitor, bound to no one");
		helper.assertValueEqual(visitors.getFirst().think(level, 0L, level.getGameTime()), Status.IDLE, "and stays while the conversation is open");
		helper.assertTrue(BoundWills.of(level.getServer()).ledger().heldBy(owner.getUUID()).isEmpty() && perch.holder().agreement() == null
				&& WorkerRoster.of(level.getServer()).workers(owner.getUUID()).isEmpty(), "Nothing is stored: no Bound Will, no agreement, no roster entry");
		helper.assertTrue(pocketed(owner, 1) && resonance(owner) == AstrolabeItem.CAPACITY && !owner.hasInfiniteMaterials(),
				"and a survival player has spent nothing");
		PactStatePayload heard = send(owner, menu, PactRequestPayload.CHOOSE, 0);
		helper.assertTrue(heard != null && heard.result().isEmpty() && heard.revision() == 1 && menu.lastSequence() == 1,
				"Asking to hear the terms answers with the state alone");
		helper.succeed();
	}

	/** D-04 (D-S14; the revision counts the offer): a revision the server never showed is refused as changed, the terms sent again; a reload that changes the offer under an open screen refuses acceptance of the old terms the same way; accepting the terms now shown seals at their cost, once. Nothing is agreed or spent before. */
	@GameTest(maxTicks = 20)
	public void aStaleTermsRevisionIsRefused(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PactDefinition stolas = stolasOffer();
		ServerPlayer owner = ready(level, TestPlayers.survivor(helper));
		pocket(owner, 1, AstrolabeItem.CAPACITY);
		BlockPos at = new BlockPos(3, 1, 3);
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu menu = open(helper, owner, perch);
		List<PactStatePayload.Term> read = menu.lastSent().terms();
		PactStatePayload never = request(owner, menu.containerId, 1, PactRequestPayload.ACCEPT, 0, 0);
		helper.assertTrue(answered(never, PactMenu.CHANGED) && never.revision() == 1 && never.terms().equals(read),
				"A revision the server never showed: changed, the terms sent again");
		helper.assertTrue(perch.holder().agreement() == null && pocketed(owner, 1) && resonance(owner) == AstrolabeItem.CAPACITY, "nothing agreed or spent");
		ConcordanceRules was = ConcordanceData.rules();
		JsonObject dearer = shipped(level);
		dearer.getAsJsonObject("compact").addProperty("resonance", stolas.resonance() + 4);
		try {
			ConcordanceData.replace(was.withPacts(PactCatalog.parse(Map.of(PactDefinition.STOLAS, dearer), was.research().keySet())));
			PactStatePayload refused = send(owner, menu, PactRequestPayload.ACCEPT, 0);
			helper.assertTrue(answered(refused, PactMenu.CHANGED) && refused.revision() == 2
					&& Integer.valueOf(stolas.resonance() + 4).equals(arg(term(refused, "compact"), 0)),
					"The offer changed under the screen: accepting what was read is refused as changed, the new terms sent");
			helper.assertTrue(perch.holder().agreement() == null && pocketed(owner, 1) && resonance(owner) == AstrolabeItem.CAPACITY, "nothing agreed or spent");
			PactStatePayload accepted = send(owner, menu, PactRequestPayload.ACCEPT, 0);
			helper.assertTrue(answered(accepted, PactMenu.SEALED) && accepted.result().get().done() && pocketed(owner, 0)
					&& resonance(owner) == AstrolabeItem.CAPACITY - stolas.resonance() - 4, "Accepting the terms now shown seals at their cost, once");
		} finally {
			ConcordanceData.replace(was);
		}
		helper.succeed();
	}

	/** D-04 (D-S13, E1): no star observed, one resonance short, declining, or Stolas's Regard not begun: each is refused with its reason (every step named), nothing is taken, called or sealed. Survival players. */
	@GameTest(maxTicks = 20)
	public void aRefusalSpendsNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PactDefinition stolas = stolasOffer();
		ServerPlayer owner = TestPlayers.survivor(helper);
		ConcordanceProgress.grant(owner, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(owner, Workers.RESEARCH, ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(owner, stolas.research(), ResearchState.UNDERSTOOD);
		RateGate.forget(owner.getUUID());
		pocket(owner, 1, stolas.resonance() - 1);
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu menu = open(helper, owner, perch);
		PactStatePayload opened = menu.lastSent();
		helper.assertTrue(keys(opened.notes()).contains(PACT_KEY + "missing.observation") && !opened.offers(PactRequestPayload.ACCEPT)
				&& perch.visitor() == null && stolases(helper).isEmpty(), "No star observed: the screen names the sky step; Stolas does not come");
		helper.assertTrue(answered(send(owner, menu, PactRequestPayload.ACCEPT, 0), PactMenu.NO_OBSERVATION), "Accepting anyway: no observation");
		AstralClaims.of(level.getServer()).observe(owner.getUUID(), LANTERN_STAR, level.getGameTime());
		helper.assertTrue(answered(send(owner, menu, PactRequestPayload.ACCEPT, 0), Pacts.Outcome.CANNOT_AFFORD.id),
				"A star observed, one resonance short: the compact cannot be paid");
		helper.assertTrue(pocketed(owner, 1) && resonance(owner) == stolas.resonance() - 1 && stolases(helper).isEmpty()
				&& BoundWills.of(level.getServer()).ledger().heldBy(owner.getUUID()).isEmpty(), "Nothing is taken, called or sealed");
		PactStatePayload declined = send(owner, menu, PactRequestPayload.DECLINE, 0);
		helper.assertTrue(answered(declined, PactMenu.DECLINED) && owner.containerMenu == owner.inventoryMenu
				&& pocketed(owner, 1) && resonance(owner) == stolas.resonance() - 1, "Declining closes the conversation and spends nothing");
		ServerPlayer novice = TestPlayers.survivor(helper);
		ConcordanceProgress.grant(novice, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(novice, Workers.RESEARCH, ResearchState.UNDERSTOOD);
		AstralClaims.of(level.getServer()).observe(novice.getUUID(), LANTERN_STAR, level.getGameTime());
		RateGate.forget(novice.getUUID());
		pocket(novice, 1, AstrolabeItem.CAPACITY);
		BlockPos theirs = new BlockPos(5, 1, 2);
		PactPerchBlockEntity own = perch(helper, theirs, novice);
		beside(helper, novice, theirs);
		PactMenu learning = open(helper, novice, own);
		helper.assertTrue(Collections.frequency(keys(learning.lastSent().notes()), PACT_KEY + "missing.step") == 3,
				"Stolas's Regard not begun: each of its three steps is named");
		helper.assertTrue(answered(send(novice, learning, PactRequestPayload.ACCEPT, 0), PactMenu.MISSING_EVIDENCE)
				&& pocketed(novice, 1) && resonance(novice) == AstrolabeItem.CAPACITY, "accepting is refused for the evidence; nothing spent");
		helper.assertTrue(!owner.hasInfiniteMaterials() && !novice.hasInfiniteMaterials(), "both ordinary survival players");
		helper.succeed();
	}

	/** D-04 (D-S13, E5): accepting spends the compact once in the call that seals; one Bound Will, its terms copied, the visitor its presence on the roster as a pact, the first offering due a period on; accepting again seals and spends nothing more. */
	@GameTest(maxTicks = 20)
	public void acceptingSealsOnceAndSpendsOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PactDefinition stolas = stolasOffer();
		ServerPlayer owner = ready(level, TestPlayers.survivor(helper));
		pocket(owner, 2, AstrolabeItem.CAPACITY);
		BlockPos at = new BlockPos(3, 1, 3);
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu menu = open(helper, owner, perch);
		UUID visitor = perch.visitor();
		long now = level.getGameTime();
		PactStatePayload sealed = send(owner, menu, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(answered(sealed, PactMenu.SEALED) && sealed.mode() == PactStatePayload.KEEP
				&& sealed.terms().stream().map(PactStatePayload.Term::key).toList().equals(PactStatePayload.TERMS), "Accepted: sealed, now kept here, its terms shown");
		helper.assertTrue(pocketed(owner, 1) && resonance(owner) == AstrolabeItem.CAPACITY - stolas.resonance(), "The compact is spent once");
		BoundWill record = recordOf(level, owner);
		helper.assertTrue(record != null && PactDefinition.STOLAS.equals(record.counterpart()) && stolas.agreement().equals(record.agreement()) && !record.transferable()
				&& Couriers.place(level, helper.absolutePos(at)).equals(record.pact().place()) && visitor.equals(record.pact().presence())
				&& new BoundWill.Service(stolas.work(), stolas.radius(), stolas.from(), stolas.to(), stolas.quota()).equals(record.pact().service())
				&& record.pact().upkeep().due() == now + stolas.period() && stolas.offering().equals(record.pact().upkeep().offering())
				&& Pacts.PERSONAL.equals(record.pact().beneficiaries()), "One Bound Will: kept at this perch, its terms copied, the first offering due a period on");
		StolasEntity presence = (StolasEntity) level.getEntity(visitor);
		helper.assertTrue(presence != null && owner.getUUID().equals(presence.owner()) && perch.visitor() == null && stolases(helper).size() == 1
				&& "pact".equals(WorkerRoster.of(level.getServer()).workers(owner.getUUID()).get(visitor).kind()),
				"The visitor stays as the pact's one presence, on its holder's roster as a pact");
		helper.assertTrue(presence.think(level, 0L, now) == Status.IDLE && record.id().equals(presence.record()), "and knows its record");
		PactStatePayload again = send(owner, menu, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(answered(again, Pacts.Outcome.TAKEN.id) && said(again).equals("message.jugcraft.concordance.workers.seal_taken")
				&& pocketed(owner, 1) && resonance(owner) == AstrolabeItem.CAPACITY - stolas.resonance()
				&& BoundWills.of(level.getServer()).ledger().heldBy(owner.getUUID()).size() == 1, "Accepting again seals nothing and spends nothing");
		helper.assertTrue(BoundWills.of(level.getServer()).problems().isEmpty(), "The records are sound: " + BoundWills.of(level.getServer()).problems());
		helper.succeed();
	}

	/** D-04 (D-S10, D-S15, E5): one Stolas pact a holder: at a second perch of theirs Stolas does not come and accepting is refused, nothing spent; another holder keeps their own. */
	@GameTest(maxTicks = 20)
	public void oneStolasPactPerHolder(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = ready(level, TestPlayers.survivor(helper));
		pocket(owner, 2, AstrolabeItem.CAPACITY);
		acceptedAt(helper, owner, new BlockPos(2, 1, 2));
		int left = resonance(owner);
		BlockPos second = new BlockPos(6, 1, 2);
		PactPerchBlockEntity other = perch(helper, second, owner);
		beside(helper, owner, second);
		PactMenu menu = open(helper, owner, other);
		helper.assertTrue(keys(menu.lastSent().notes()).contains(PACT_KEY + "missing.held") && !menu.lastSent().offers(PactRequestPayload.ACCEPT)
				&& other.visitor() == null && stolases(helper).size() == 1, "At a second perch: already held; Stolas does not come");
		PactStatePayload refused = send(owner, menu, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(answered(refused, Pacts.Outcome.ALREADY_HELD.id) && said(refused).equals("message.jugcraft.concordance.workers.seal_already_held")
				&& pocketed(owner, 1) && resonance(owner) == left && BoundWills.of(level.getServer()).ledger().heldBy(owner.getUUID()).size() == 1,
				"Accepting there is refused, nothing spent; one pact");
		ServerPlayer neighbour = ready(level, TestPlayers.survivor(helper));
		pocket(neighbour, 1, AstrolabeItem.CAPACITY);
		acceptedAt(helper, neighbour, new BlockPos(2, 1, 6));
		helper.assertTrue(recordOf(level, neighbour) != null && stolases(helper).size() == 2, "Any number of holders keep their own");
		helper.succeed();
	}

	/** D-04 (E1, D-S8, D-S14): a party member at the owner's perch is told only its owner may invite; Stolas does not come; accepting is not permitted, nothing spent; requests for another menu, an answered sequence, out of bounds, after closing, or from a stand-in are dropped. */
	@GameTest(maxTicks = 20)
	public void anotherPlayerCannotAcceptForTheHolder(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = ready(level, TestPlayers.survivor(helper));
		ServerPlayer member = ready(level, TestPlayers.survivor(helper));
		pocket(member, 1, AstrolabeItem.CAPACITY);
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(owner.getUUID(), member.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(member.getUUID(), ms) == PartyManager.Result.OK, "The owner and a member form a party");
			BlockPos at = new BlockPos(3, 1, 3);
			PactPerchBlockEntity perch = perch(helper, at, owner);
			beside(helper, member, at);
			PactMenu menu = open(helper, member, perch);
			helper.assertTrue(keys(menu.lastSent().notes()).contains(PACT_KEY + "missing.owner") && perch.visitor() == null,
					"Only the perch's owner may invite: Stolas does not come for a party member");
			PactStatePayload refused = send(member, menu, PactRequestPayload.ACCEPT, 0);
			helper.assertTrue(answered(refused, Pacts.Outcome.NOT_PERMITTED.id) && said(refused).equals("message.jugcraft.concordance.workers.seal_not_permitted")
					&& pocketed(member, 1) && resonance(member) == AstrolabeItem.CAPACITY, "Accepting is not permitted; nothing spent");
			int revision = refused.revision();
			int answeredAt = refused.result().get().sequence();
			helper.assertTrue(request(member, menu.containerId + 1, 50, PactRequestPayload.ACCEPT, 0, revision) == null, "A request naming another menu's container is dropped");
			helper.assertTrue(request(member, menu.containerId, answeredAt, PactRequestPayload.ACCEPT, 0, revision) == null, "one repeating an answered sequence is dropped");
			helper.assertTrue(request(member, menu.containerId, 60, PactRequestPayload.ACTIONS.size(), 0, revision) == null
					&& request(member, menu.containerId, 61, PactRequestPayload.ACCEPT, PactStatePayload.MAX_CHOICES, revision) == null, "out of bounds is dropped");
			int closed = menu.containerId;
			member.closeContainer();
			helper.assertTrue(request(member, closed, 70, PactRequestPayload.ACCEPT, 0, revision) == null, "after closing, it names nothing");
			FakePlayer standIn = FakePlayer.get(level, new GameProfile(owner.getUUID(), "[Concordance]"));
			helper.assertTrue(PactMenu.receive(standIn, new PactRequestPayload(closed, 1, PactRequestPayload.ACCEPT, 0, 1)) == null, "and a stand-in is never answered");
			helper.assertTrue(BoundWills.of(level.getServer()).ledger().heldBy(member.getUUID()).isEmpty()
					&& BoundWills.of(level.getServer()).ledger().heldBy(owner.getUUID()).isEmpty() && perch.holder().agreement() == null, "No one was bound");
		} finally {
			parties.adminDisband(owner.getUUID());
		}
		helper.succeed();
	}

	/** D-04 (D-S8, E5; the revision counts the offer): two conversations at one ownerless perch share one visitor; the first acceptance seals; the second, on the same unchanged terms, is taken and spends nothing. */
	@GameTest(maxTicks = 20)
	public void twoNegotiationsAtOnePerchSealOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer first = ready(level, TestPlayers.survivor(helper));
		ServerPlayer second = ready(level, TestPlayers.survivor(helper));
		pocket(first, 1, AstrolabeItem.CAPACITY);
		pocket(second, 1, AstrolabeItem.CAPACITY);
		BlockPos at = new BlockPos(3, 1, 3);
		PactPerchBlockEntity perch = perch(helper, at, null);
		beside(helper, first, at);
		beside(helper, second, at);
		PactMenu one = open(helper, first, perch);
		PactMenu two = open(helper, second, perch);
		helper.assertTrue(stolases(helper).size() == 1 && perch.visitor() != null, "Two conversations at one perch: one visitor");
		helper.assertTrue(answered(send(first, one, PactRequestPayload.ACCEPT, 0), PactMenu.SEALED), "The first accepts: sealed");
		PactStatePayload late = send(second, two, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(answered(late, Pacts.Outcome.TAKEN.id) && said(late).equals("message.jugcraft.concordance.workers.seal_taken")
				&& late.mode() == PactStatePayload.CLOSED && keys(late.notes()).contains(PACT_KEY + "reading.taken"), "The second is refused as taken, and shown so");
		helper.assertTrue(pocketed(second, 1) && resonance(second) == AstrolabeItem.CAPACITY
				&& BoundWills.of(level.getServer()).ledger().heldBy(second.getUUID()).isEmpty() && pocketed(first, 0)
				&& recordOf(level, first) != null && stolases(helper).size() == 1, "Only the first spent and holds; one Stolas");
		helper.assertTrue(BoundWills.of(level.getServer()).problems().isEmpty(), "no place keeps two");
		helper.succeed();
	}

	/** D-04 (D-S7, D-S9): a visitor with no pact leaves when its conversation ends: declined, closed, timed out, its player gone, or after a restart (no session or visitor is saved). Nothing is recorded or noted. */
	@GameTest(maxTicks = 20)
	public void aVisitingStolasLeavesWithoutARecord(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		long now = level.getGameTime();
		record Visit(ServerPlayer player, PactPerchBlockEntity perch, PactMenu menu, StolasEntity visitor) {
		}
		Function<BlockPos, Visit> visit = at -> {
			ServerPlayer player = ready(level, TestPlayers.mock(helper));
			PactPerchBlockEntity perch = perch(helper, at, player);
			beside(helper, player, at);
			PactMenu menu = open(helper, player, perch);
			StolasEntity visitor = (StolasEntity) level.getEntity(perch.visitor());
			helper.assertTrue(visitor != null && visitor.think(level, 0L, now) == Status.IDLE && !visitor.isRemoved(), "Stolas waits while it is open");
			return new Visit(player, perch, menu, visitor);
		};
		Visit declined = visit.apply(new BlockPos(1, 1, 1));
		send(declined.player(), declined.menu(), PactRequestPayload.DECLINE, 0);
		helper.assertTrue(declined.visitor().think(level, 0L, now) == Status.FINISHED && declined.visitor().isRemoved(), "Declined: he leaves");
		Visit closed = visit.apply(new BlockPos(4, 1, 1));
		closed.player().closeContainer();
		helper.assertTrue(closed.visitor().think(level, 0L, now) == Status.FINISHED && closed.visitor().isRemoved(), "Closed: he leaves");
		Visit idle = visit.apply(new BlockPos(7, 1, 1));
		helper.assertTrue(idle.visitor().think(level, 0L, now + PactSessions.TIMEOUT_TICKS) == Status.IDLE, "Within its time he waits");
		helper.assertTrue(idle.visitor().think(level, 0L, now + PactSessions.TIMEOUT_TICKS + 1) == Status.FINISHED && idle.visitor().isRemoved(), "Timed out: he leaves");
		Visit gone = visit.apply(new BlockPos(1, 1, 5));
		TestPlayers.leave(gone.player());
		helper.assertTrue(gone.visitor().think(level, 0L, now) == Status.FINISHED && gone.visitor().isRemoved(), "Its player gone: he leaves");
		Visit saved = visit.apply(new BlockPos(5, 1, 5));
		CompoundTag perchSave = saved.perch().saveWithoutMetadata(level.registryAccess());
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		saved.visitor().saveWithoutId(out);
		CompoundTag body = out.buildResult();
		helper.assertTrue(!perchSave.contains("visitor") && body.contains("perch") && !body.contains("record") && !body.contains("owner"),
				"Neither the perch nor Stolas saves a visit: " + body);
		saved.perch().loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), perchSave));
		StolasEntity reloaded = Workers.STOLAS.create(level, EntitySpawnReason.LOAD);
		reloaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), body));
		helper.assertTrue(saved.perch().visitor() == null && reloaded.think(level, 0L, now) == Status.FINISHED && reloaded.isRemoved()
				&& saved.visitor().think(level, 0L, now) == Status.FINISHED, "After a restart the perch knows no visitor, and he leaves");
		for (Visit each : List.of(declined, closed, idle, gone, saved)) {
			helper.assertTrue(BoundWills.of(level.getServer()).ledger().heldBy(each.player().getUUID()).isEmpty()
					&& WorkerRoster.of(level.getServer()).workers(each.player().getUUID()).isEmpty() && each.perch().holder().agreement() == null, "Nothing recorded or noted");
		}
		helper.succeed();
	}

	/** D-04 (D-S7): a transport crate refuses Stolas, presence or visitor (MISC), where it takes a cow; he stays. */
	@GameTest(maxTicks = 20)
	public void aCrateRefusesStolas(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = ready(level, TestPlayers.mock(helper));
		acceptedAt(helper, owner, new BlockPos(2, 1, 2));
		StolasEntity presence = (StolasEntity) level.getEntity(recordOf(level, owner).pact().presence());
		ServerPlayer guest = ready(level, TestPlayers.mock(helper));
		BlockPos other = new BlockPos(5, 1, 2);
		PactPerchBlockEntity perch = perch(helper, other, guest);
		beside(helper, guest, other);
		open(helper, guest, perch);
		StolasEntity visitor = (StolasEntity) level.getEntity(perch.visitor());
		ServerPlayer carrier = TestPlayers.mock(helper);
		beside(helper, carrier, new BlockPos(3, 1, 2));
		ItemStack crate = new ItemStack(MobTransportCrate.WOOD);
		carrier.setItemInHand(InteractionHand.MAIN_HAND, crate);
		helper.assertTrue(Workers.STOLAS.getCategory() == MobCategory.MISC && !MobTransportCrate.friendly(presence) && !MobTransportCrate.friendly(visitor),
				"Stolas is no calm farm creature to a crate");
		helper.assertTrue(!MobTransportCrate.capture(carrier, crate, presence) && !MobTransportCrate.capture(carrier, crate, visitor)
				&& !presence.isRemoved() && !visitor.isRemoved() && MobTransportCrate.count(crate) == 0, "It takes neither the presence nor the visitor");
		Cow cow = EntityTypes.COW.create(level, EntitySpawnReason.MOB_SUMMONED);
		Vec3 by = helper.absoluteVec(new Vec3(3.5, 1.0, 4.5));
		cow.snapTo(by.x, by.y, by.z, 0.0F, 0.0F);
		level.addFreshEntity(cow);
		helper.assertTrue(MobTransportCrate.capture(carrier, crate, cow) && MobTransportCrate.count(crate) == 1, "where it takes a cow");
		helper.succeed();
	}

	/** D-04 (D-S7, 03:146): no ordinary damage harms Stolas, and harming him ends nothing; only damage past invulnerability hurts him. */
	@GameTest(maxTicks = 20)
	public void stolasTakesNoOrdinaryDamage(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = ready(level, TestPlayers.mock(helper));
		acceptedAt(helper, owner, new BlockPos(3, 1, 3));
		BoundWill record = recordOf(level, owner);
		StolasEntity presence = (StolasEntity) level.getEntity(record.pact().presence());
		float health = presence.getHealth();
		for (DamageSource source : List.of(level.damageSources().generic(), level.damageSources().playerAttack(owner), level.damageSources().fall(),
				level.damageSources().inFire(), level.damageSources().magic())) {
			helper.assertTrue(!presence.hurtServer(level, source, 1000.0F) && presence.getHealth() == health && presence.isAlive(), "unharmed by " + source.getMsgId());
		}
		helper.assertTrue(presence.fireImmune() && presence.hurtServer(level, level.damageSources().fellOutOfWorld(), 1.0F) && presence.getHealth() < health,
				"only damage past invulnerability hurts him");
		helper.assertTrue(record.equals(BoundWills.of(level.getServer()).ledger().get(record.id())), "and his pact is untouched");
		helper.succeed();
	}

	/** D-04 (D-S23, 11:59): with stolas.json invalid, then missing, the reload reports it; new invitations are refused with the reason (Stolas does not come; nothing spent); a sealed pact keeps the terms copied at sealing. */
	@GameTest(maxTicks = 20)
	public void aBrokenPactDefinitionIsReported(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = ready(level, TestPlayers.mock(helper));
		BlockPos kept = new BlockPos(2, 1, 2);
		acceptedAt(helper, holder, kept);
		holder.closeContainer();
		PactPerchBlockEntity keptPerch = helper.getBlockEntity(kept, PactPerchBlockEntity.class);
		BoundWill record = recordOf(level, holder);
		ServerPlayer newcomer = ready(level, TestPlayers.survivor(helper));
		pocket(newcomer, 1, AstrolabeItem.CAPACITY);
		BlockPos fresh = new BlockPos(6, 1, 2);
		PactPerchBlockEntity freshPerch = perch(helper, fresh, newcomer);
		ConcordanceRules was = ConcordanceData.rules();
		JsonObject invalid = shipped(level);
		invalid.addProperty("surprise", true);
		PactCatalog broken = PactCatalog.parse(Map.of(PactDefinition.STOLAS, invalid), was.research().keySet());
		helper.assertTrue(broken.get(PactDefinition.STOLAS) == null && broken.problems().stream().anyMatch(p -> p.startsWith("pact jugcraft:stolas:") && p.contains("surprise")),
				"An invalid definition is refused, naming its fault: " + broken.problems());
		for (PactCatalog catalog : List.of(broken, PactCatalog.parse(Map.of(), was.research().keySet()))) {
			try {
				ConcordanceData.replace(was.withPacts(catalog));
				helper.assertTrue(ConcordanceData.rules().problems().stream().anyMatch(p -> p.startsWith("pact jugcraft:stolas")), "the reload reports it with the rules' problems");
				beside(helper, newcomer, fresh);
				PactMenu menu = open(helper, newcomer, freshPerch);
				helper.assertTrue(menu.lastSent().mode() == PactStatePayload.CLOSED && keys(menu.lastSent().notes()).contains(PACT_KEY + "unavailable")
						&& freshPerch.visitor() == null, "New invitations are refused with the reason; Stolas does not come");
				helper.assertTrue(answered(send(newcomer, menu, PactRequestPayload.ACCEPT, 0), PactMenu.NO_DEFINITION)
						&& pocketed(newcomer, 1) && BoundWills.of(level.getServer()).ledger().heldBy(newcomer.getUUID()).isEmpty(), "accepting spends nothing");
				newcomer.closeContainer();
				beside(helper, holder, kept);
				PactMenu theirs = open(helper, holder, keptPerch);
				helper.assertTrue(theirs.lastSent().mode() == PactStatePayload.KEEP
						&& theirs.lastSent().terms().stream().map(PactStatePayload.Term::key).toList().equals(PactStatePayload.TERMS)
						&& record.equals(BoundWills.of(level.getServer()).ledger().get(record.id()))
						&& keptPerch.holder().verify(level, level.getGameTime()) == AgreementHolder.Standing.VALID, "A sealed pact keeps the terms it copied and goes on");
				holder.closeContainer();
			} finally {
				ConcordanceData.replace(was);
			}
		}
		helper.succeed();
	}

	/**
	 * D-04 (D-S14): the pact screen's two messages are bounded both ways. A state's lists are cut to their limits (an empty
	 * stack dropped) before anything is sent, and it reads back as written; a request is its five numbers and nothing more;
	 * a term's key past its bound cannot be encoded, so it is never sent.
	 */
	@GameTest(maxTicks = 20)
	public void thePactScreensMessagesAreBounded(GameTestHelper helper) {
		RegistryAccess registries = helper.getLevel().registryAccess();
		List<Component> many = IntStream.range(0, 14).<Component>mapToObj(i -> Component.literal("line " + i)).toList();
		List<PactStatePayload.Term> terms = IntStream.range(0, 20)
				.mapToObj(i -> new PactStatePayload.Term("term_" + i, Component.literal("value " + i))).toList();
		List<PactStatePayload.Choice> choices = IntStream.range(0, 12)
				.mapToObj(i -> new PactStatePayload.Choice("jugcraft:choice_" + i, Component.literal("choice " + i), i, i % 2 == 0)).toList();
		PactStatePayload state = new PactStatePayload(7, 3, PactStatePayload.NEGOTIATE, Component.literal("Stolas"), many, terms, many,
				choices, choices, List.of(new ItemStack(Items.FEATHER, 2), ItemStack.EMPTY), 6,
				PactStatePayload.mask(PactRequestPayload.ACCEPT, PactRequestPayload.DECLINE),
				Optional.of(new PactStatePayload.Result(4, PactMenu.SEALED, true, Component.literal("sealed"))));
		helper.assertTrue(state.beats().size() == PactStatePayload.MAX_BEATS && state.terms().size() == PactStatePayload.MAX_TERMS
				&& state.notes().size() == PactStatePayload.MAX_NOTES && state.patterns().size() == PactStatePayload.MAX_CHOICES
				&& state.offerings().size() == PactStatePayload.MAX_CHOICES && state.compact().size() == 1,
				"A state's lists are cut to their limits and an empty stack is dropped");
		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
		PactStatePayload.CODEC.encode(buffer, state);
		PactStatePayload back = PactStatePayload.CODEC.decode(buffer);
		helper.assertTrue(back.containerId() == 7 && back.revision() == 3 && back.mode() == PactStatePayload.NEGOTIATE
				&& back.beats().equals(state.beats()) && back.terms().equals(state.terms()) && back.patterns().equals(state.patterns())
				&& back.resonance() == 6 && back.offers(PactRequestPayload.ACCEPT) && !back.offers(PactRequestPayload.END)
				&& ItemStack.matches(back.compact().getFirst(), state.compact().getFirst())
				&& back.result().orElseThrow().equals(state.result().orElseThrow()) && buffer.readableBytes() == 0,
				"A state reads back as written");
		RegistryFriendlyByteBuf request = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
		PactRequestPayload asked = new PactRequestPayload(7, 9, PactRequestPayload.RECALL, 1, 3);
		PactRequestPayload.CODEC.encode(request, asked);
		helper.assertTrue(PactRequestPayload.CODEC.decode(request).equals(asked) && request.readableBytes() == 0,
				"A request is its five numbers and nothing more");
		helper.assertTrue(PactRequestPayload.ACTIONS.size() == 9 && "seat".equals(PactRequestPayload.ACTIONS.get(PactRequestPayload.SEAT))
				&& PactStatePayload.TERMS.size() == 14, "The actions keep their indexes; the terms page has its fourteen terms");
		boolean refused = false;
		try {
			PactStatePayload.CODEC.encode(new RegistryFriendlyByteBuf(Unpooled.buffer(), registries), new PactStatePayload(7, 3,
					PactStatePayload.KEEP, Component.literal("Stolas"), List.of(),
					List.of(new PactStatePayload.Term("k".repeat(PactStatePayload.MAX_KEY + 1), Component.empty())), List.of(), List.of(),
					List.of(), List.of(), 0, 0, Optional.empty()));
		} catch (RuntimeException expected) {
			refused = true;
		}
		helper.assertTrue(refused, "A key past its bound cannot be encoded, so it is never sent");
		helper.succeed();
	}
}
