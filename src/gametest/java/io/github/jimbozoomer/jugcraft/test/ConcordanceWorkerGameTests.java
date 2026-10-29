package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.Saved;
import io.github.jimbozoomer.jugcraft.concordance.celestial.Pattern;
import io.github.jimbozoomer.jugcraft.concordance.courier.CourierLedger;
import io.github.jimbozoomer.jugcraft.concordance.courier.CourierPostBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.courier.Couriers;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Factor;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Habitat;
import io.github.jimbozoomer.jugcraft.concordance.ecology.HabitatAdvice;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Niche;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Organism;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Verdict;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.garden.GleanerBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.OrganismCropBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantHeartBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.journal.Journal;
import io.github.jimbozoomer.jugcraft.concordance.journal.JournalSection;
import io.github.jimbozoomer.jugcraft.concordance.logistics.Logistics;
import io.github.jimbozoomer.jugcraft.concordance.logistics.Place;
import io.github.jimbozoomer.jugcraft.concordance.logistics.RequestState;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWillLedger;
import io.github.jimbozoomer.jugcraft.concordance.rules.ConcordanceRules;
import io.github.jimbozoomer.jugcraft.concordance.rules.Definitions;
import io.github.jimbozoomer.jugcraft.concordance.rules.Evidence;
import io.github.jimbozoomer.jugcraft.concordance.rules.Knowledge;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.sign.Sign;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstralClaims;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstrolabeItem;
import io.github.jimbozoomer.jugcraft.concordance.sky.ObservatoryBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.sky.Sky;
import io.github.jimbozoomer.jugcraft.concordance.spirits.AgreementHolder;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BathinEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BondingCharmItem;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BoundWills;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BuerEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ClockworkPorterEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ForasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenPacts;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenReach;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GatheringShadeEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GraftKeeping;
import io.github.jimbozoomer.jugcraft.concordance.spirits.HabitatReading;
import io.github.jimbozoomer.jugcraft.concordance.spirits.HearthlingEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.MaraxEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactAccords;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactHistory;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactKeeping;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactMenu;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactObligations;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactPerchBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactReading;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactService;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactServices;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSessions;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSpiritEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSpirits;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PorterKeyItem;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Roster;
import io.github.jimbozoomer.jugcraft.concordance.spirits.RosterRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.RosterStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.SpiritAnchorBlock;
import io.github.jimbozoomer.jugcraft.concordance.spirits.SpiritAnchorBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StarReckoning;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StolasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StolasRegard;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StrainTending;
import io.github.jimbozoomer.jugcraft.concordance.spirits.VerdanceCarrying;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerRoster;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkshopHandEntity;
import io.github.jimbozoomer.jugcraft.concordance.worker.Agreement;
import io.github.jimbozoomer.jugcraft.concordance.worker.Body;
import io.github.jimbozoomer.jugcraft.concordance.worker.Bond;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactCatalog;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactParser;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import io.github.jimbozoomer.jugcraft.concordance.worker.WorkerDefinition;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;
import io.github.jimbozoomer.jugcraft.party.PartyManager;
import io.github.jimbozoomer.jugcraft.styx.JugcraftStyx;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.IntStream;
import local.peepo.MobTransportCrate;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
 * binds another and no place keeps two. Concordance Chunk D, D-04: Stolas explains before anyone agrees; a stale terms
 * revision, a refusal and a second negotiation spend nothing; accepting seals once and spends once; one pact a holder;
 * nobody accepts for another; a visitor without a pact or a live conversation leaves; crates and ordinary damage cannot
 * take him; a broken definition is reported and sealed pacts keep their terms; the screen's two messages are bounded.
 * Concordance Chunk D, D-05: Star-reckoning refuses with its reasons and recalls through the one recall; the first
 * obligation masters Stolas's Regard once; his upkeep warns, gives grace, suspends and resumes on his own offerings, never
 * owing time away, switched off or answered by a stand-in; ending returns what was paid ahead once to a living holder;
 * breaking his perch ends nothing and the pact is seated again; a lost Stolas is called back at no cost.
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
		return player.getInventory().countItem(Items.WOOL.white());
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
		// D-05 (E2 on Stolas): his own terms; the compact paid the first period; warned, he serves through the grace; held for
		// upkeep he says so and reckons nothing; his own offerings resume both.
		ServerPlayer keeper = regarded(helper);
		long s0 = dayStart(level, 200);
		PactPerchBlockEntity perch = stolasPact(helper, keeper, new BlockPos(6, 1, 6), s0);
		UUID pact = perch.holder().agreement().record();
		observed(level, keeper, HARVEST_MOON);
		ItemStack charged = holding(keeper, AstrolabeItem.CAPACITY);
		BoundWill.Upkeep terms = pactOf(level, perch).pact().upkeep();
		helper.assertTrue(terms.offering().equals("jugcraft:stolas_offerings") && terms.count() == 1 && terms.period() == PERIOD_7 && terms.grace() == Pacts.DAY
				&& terms.cap() == 4, "Stolas's upkeep: one of his offerings a week, a day's grace, four ahead at most: " + terms);
		helper.assertTrue(terms.due() == s0 + PERIOD_7 && escrowed(level, pact) == 0, "The compact paid the first: nothing paid ahead, the first offering a week on");
		helper.assertTrue(stay(level, perch.holder(), s0 + 1_000L, s0 + PERIOD_7 - 1_000L).isEmpty(), "Nothing falls due before then");
		long s1 = s0 + PERIOD_7;
		helper.assertValueEqual(perch.holder().look(level, s1), Pacts.Event.WARNED, "Due with none held, a warning");
		helper.assertTrue(pactOf(level, perch).pact().upkeep().graceUntil() == s1 + Pacts.DAY && stolas(level, perch).think(level, NOON, s1) == Status.UPKEEP_DUE
				&& WorkerEntity.sign(Status.UPKEEP_DUE) == Sign.WANT, "a day of grace; Stolas says an offering is due, with a sign of want");
		helper.assertValueEqual(reckon(level, keeper, perch, HARVEST_MOON, s1, s1), Pacts.Reckoning.RECALLED, "Within the grace he still reckons");
		helper.assertTrue(stay(level, perch.holder(), s1 + 1_000L, s1 + Pacts.DAY - 1_000L).isEmpty(), "Nothing more within the grace");
		helper.assertValueEqual(perch.holder().look(level, s1 + Pacts.DAY), Pacts.Event.SUSPENDED, "The grace over, it is suspended for upkeep");
		int spent = AstrolabeItem.CAPACITY - Sky.catalog().pattern(HARVEST_MOON).recallCost();
		helper.assertTrue(stolas(level, perch).think(level, NOON, s1 + Pacts.DAY) == Status.UPKEEP_HELD
				&& reckon(level, keeper, perch, HARVEST_MOON, s1 + Pacts.DAY, s1 + Pacts.DAY) == Pacts.Reckoning.UPKEEP_HELD
				&& AstrolabeItem.charge(charged) == spent && escrowed(level, pact) == 0, "Stolas says so, reckons nothing, and nothing is taken");
		helper.assertValueEqual(PactKeeping.resume(level, keeper, perch.holder(), s1 + Pacts.DAY), AgreementHolder.Toggle.HELD_FOR_UPKEEP, "The holder's word does not lift it");
		give(keeper, new ItemStack(Items.AMETHYST_SHARD, 6));
		long s2 = s1 + Pacts.DAY + 500L;
		helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), Items.AMETHYST_SHARD, s2), 4, "Six offered: four go in, the most it holds");
		helper.assertTrue(shards(keeper) == 2 && escrowed(level, pact) == 3 && pactOf(level, perch).pact().hold() == BoundWill.Hold.NONE
				&& pactOf(level, perch).pact().upkeep().due() == s2 + PERIOD_7 && !pactOf(level, perch).pact().upkeep().warned(),
				"one taken for the period; resumed at once, the next a week on");
		helper.assertTrue(stolas(level, perch).think(level, NOON, s2) != Status.UPKEEP_HELD
				&& reckon(level, keeper, perch, HARVEST_MOON, s2 + Pacts.DAY, s2 + Pacts.DAY) == Pacts.Reckoning.RECALLED, "Stolas serves again");
		helper.assertTrue(stay(level, perch.holder(), s2 + 1_000L, s2 + PERIOD_7 - 1_000L).isEmpty() && perch.holder().look(level, s2 + PERIOD_7) == Pacts.Event.PAID
				&& escrowed(level, pact) == 2 && pactOf(level, perch).pact().upkeep().due() == s2 + 2 * PERIOD_7 && BoundWills.of(level.getServer()).problems().isEmpty(),
				"Paid ahead, one offering a week, no sooner");
		helper.succeed();
	}

	/**
	 * D-02 (E2, D-S3, critique 2a): a holder away while warned (from the moment of the warning, so the grace left is all of
	 * it, D-P50), or away past both due date and grace, is warned again at their first look back with the whole grace; never
	 * suspended at once, even when they suspend and resume it before that look. D-05: the same on Stolas, an offering
	 * resuming it.
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
		// D-05 (E2 on Stolas): away ten game days with nothing paid ahead, the holder returns to a warning and a whole day's
		// grace; still unpaid at its end, the pact is suspended; a Stolas Starflower resumes it.
		ServerPlayer keeper = regarded(helper);
		UUID keeperId = keeper.getUUID();
		BlockPos perchAt = new BlockPos(6, 1, 6);
		long s0 = level.getGameTime() + 6_000_000L;
		PactPerchBlockEntity perch = stolasPact(helper, keeper, perchAt, s0);
		helper.assertTrue(stay(level, perch.holder(), s0 + 1_000L, s0 + PERIOD_7 - 1_000L).isEmpty() && perch.holder().look(level, s0 + PERIOD_7) == Pacts.Event.WARNED,
				"Warned when the first offering falls due");
		TestPlayers.leave(keeper);
		helper.assertTrue(stay(level, perch.holder(), s0 + PERIOD_7 + 1_000L, s0 + PERIOD_7 + 10 * Pacts.DAY).isEmpty() && perch.holder().held(level) == null,
				"Away ten game days, nothing is looked at: no suspension");
		ServerPlayer home = TestPlayers.again(helper, keeperId);
		RateGate.forget(keeperId);
		beside(helper, home, perchAt);
		long sr = s0 + PERIOD_7 + 10 * Pacts.DAY + 500L;
		helper.assertValueEqual(perch.holder().look(level, sr), Pacts.Event.WARNED, "Back, they are warned again");
		helper.assertTrue(pactOf(level, perch).pact().upkeep().graceUntil() == sr + Pacts.DAY && stolas(level, perch).think(level, NOON, sr) == Status.UPKEEP_DUE,
				"with the whole day of grace from their return");
		helper.assertTrue(stay(level, perch.holder(), sr + 1_000L, sr + Pacts.DAY - 1_000L).isEmpty() && perch.holder().look(level, sr + Pacts.DAY) == Pacts.Event.SUSPENDED,
				"Still unpaid at its end: suspended; nothing is lost");
		give(home, new ItemStack(starflower(), 1));
		helper.assertTrue(PactKeeping.deposit(level, home, perch.holder(), starflower(), sr + Pacts.DAY + 500L) == 1 && perch.holder().held(level) == null,
				"A Stolas Starflower resumes it");
		helper.succeed();
	}

	/**
	 * D-05 (D-S3, as the unit review proposes): leaving for a minute never renews a warning. Warned, then away beyond 64
	 * blocks for 61 seconds after every 1,000 ticks present, the holder is warned again at each return with the grace they
	 * had left, its end moved on by the time away (never charged); after one game day present the pact is suspended.
	 */
	@GameTest(maxTicks = 20)
	public void shortAbsencesNeverRenewTheGrace(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = regarded(helper);
		BlockPos at = new BlockPos(3, 1, 3);
		long s0 = level.getGameTime() + 1_000_000L;
		PactPerchBlockEntity perch = stolasPact(helper, keeper, at, s0);
		helper.assertTrue(stay(level, perch.holder(), s0 + 1_000L, s0 + PERIOD_7 - 1_000L).isEmpty(), "Nothing falls due before its week");
		long now = s0 + PERIOD_7;
		helper.assertValueEqual(perch.holder().look(level, now), Pacts.Event.WARNED, "Due with none paid ahead: a warning and a day of grace");
		Vec3 far = helper.absoluteVec(new Vec3(at.getX() + 70.5, at.getY(), at.getZ() + 0.5));
		long present = 0L;
		int returns = 0;
		Pacts.Event seen = Pacts.Event.NONE;
		while (seen == Pacts.Event.NONE && present < 2 * Pacts.DAY) {
			for (int look = 0; look < 5 && seen == Pacts.Event.NONE; look++) {
				now += 200L;
				present += 200L;
				seen = perch.holder().look(level, now);
			}
			if (seen != Pacts.Event.NONE) {
				break;
			}
			keeper.snapTo(far.x, far.y, far.z, 0.0F, 0.0F);
			helper.assertValueEqual(perch.holder().look(level, now + 600L), Pacts.Event.NONE, "Away beyond 64 blocks, nothing is looked at");
			now += 1_220L;
			beside(helper, keeper, at);
			BoundWill.Upkeep before = pactOf(level, perch).pact().upkeep();
			helper.assertValueEqual(perch.holder().look(level, now), Pacts.Event.WARNED, "Back after 61 seconds, warned again");
			helper.assertValueEqual(pactOf(level, perch).pact().upkeep().graceUntil(), before.graceUntil() + (now - before.lookedAt()),
					"with the grace they had left, its end moved on by the time away");
			returns++;
		}
		helper.assertTrue(seen == Pacts.Event.SUSPENDED && present == Pacts.DAY && returns == 23
				&& pactOf(level, perch).pact().hold() == BoundWill.Hold.UPKEEP,
				"After one game day present the pact is suspended for upkeep: " + seen + " after " + present + " ticks present and " + returns + " returns");
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
		// D-05 (E2 on Stolas): logged out, the perch unloaded (no look: its ticker runs only while loaded; modelled as a gap
		// between looks, not a chunk-load listener), online 100 blocks away, or in another level: nothing is looked at; back,
		// at most one offering each time.
		ServerPlayer keeper = regarded(helper);
		UUID keeperId = keeper.getUUID();
		BlockPos perchAt = new BlockPos(6, 1, 6);
		long s0 = level.getGameTime() + 9_000_000L;
		PactPerchBlockEntity perch = stolasPact(helper, keeper, perchAt, s0);
		UUID pact = perch.holder().agreement().record();
		give(keeper, new ItemStack(Items.AMETHYST_SHARD, 4));
		helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), Items.AMETHYST_SHARD, s0), 4, "Four paid ahead");
		TestPlayers.leave(keeper);
		for (long t = s0 + Pacts.DAY; t <= s0 + 30 * Pacts.DAY; t += Pacts.DAY) {
			helper.assertValueEqual(perch.holder().look(level, t), Pacts.Event.NONE, "Logged out, nothing is looked at");
			PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), t);
		}
		helper.assertValueEqual(escrowed(level, pact), 4, "and nothing is taken");
		ServerPlayer stolasBack = TestPlayers.again(helper, keeperId);
		beside(helper, stolasBack, perchAt);
		long stolasTr = s0 + 30 * Pacts.DAY + 500L;
		helper.assertTrue(perch.holder().look(level, stolasTr) == Pacts.Event.PAID && escrowed(level, pact) == 3
				&& pactOf(level, perch).pact().upkeep().due() == stolasTr + PERIOD_7, "Back after thirty days: one offering, the next a week from now");
		long stolasTu = stolasTr + 21 * Pacts.DAY;
		helper.assertTrue(perch.holder().look(level, stolasTu) == Pacts.Event.PAID && escrowed(level, pact) == 2, "The perch unloaded three weeks: one offering");
		Vec3 stolasFar = helper.absoluteVec(new Vec3(perchAt.getX() + 100.5, perchAt.getY(), perchAt.getZ() + 0.5));
		stolasBack.snapTo(stolasFar.x, stolasFar.y, stolasFar.z, 0.0F, 0.0F);
		helper.assertTrue(stay(level, perch.holder(), stolasTu + 1_000L, stolasTu + 3 * PERIOD_7).isEmpty() && escrowed(level, pact) == 2,
				"Online 100 blocks away, nothing is looked at");
		beside(helper, stolasBack, perchAt);
		long stolasTw = stolasTu + 3 * PERIOD_7 + 500L;
		helper.assertTrue(perch.holder().look(level, stolasTw) == Pacts.Event.PAID && escrowed(level, pact) == 1, "Back near it: one offering for three weeks away");
		ServerLevel stolasNether = level.getServer().getLevel(Level.NETHER);
		stolasBack.teleport(new TeleportTransition(stolasNether, new Vec3(0.5, 64.0, 0.5), Vec3.ZERO, 0.0F, 0.0F, TeleportTransition.DO_NOTHING));
		helper.assertTrue(stolasBack.level() == stolasNether && stay(level, perch.holder(), stolasTw + 1_000L, stolasTw + 3 * PERIOD_7).isEmpty()
				&& escrowed(level, pact) == 1, "In another level, nothing is looked at");
		stolasBack.teleport(new TeleportTransition(level, helper.absoluteVec(new Vec3(perchAt.getX() + 0.5, perchAt.getY(), perchAt.getZ() + 2.5)), Vec3.ZERO,
				0.0F, 0.0F, TeleportTransition.DO_NOTHING));
		long stolasTn = stolasTw + 3 * PERIOD_7 + 500L;
		helper.assertTrue(perch.holder().look(level, stolasTn) == Pacts.Event.PAID && escrowed(level, pact) == 0 && BoundWills.of(level.getServer()).problems().isEmpty(),
				"Back: one offering; no debt for any of it");
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
		// D-05 (D-S16, E2 on Stolas): switched off, nothing falls due or is taken; Stolas waits; nothing is reckoned, paid,
		// suspended, ended, seated or kept; a lost Stolas is not called; on again, one offering, and he returns at no cost.
		ServerPlayer keeper = regarded(helper);
		long s0 = dayStart(level, 400);
		PactPerchBlockEntity perch = stolasPact(helper, keeper, new BlockPos(6, 1, 6), s0);
		UUID pact = perch.holder().agreement().record();
		give(keeper, new ItemStack(Items.AMETHYST_SHARD, 2));
		helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), Items.AMETHYST_SHARD, s0), 2, "Two paid ahead");
		observed(level, keeper, FULL_MOON);
		ItemStack charged = holding(keeper, AstrolabeItem.CAPACITY);
		ObservatoryBlockEntity observatory = observatory(helper, new BlockPos(0, 1, 6));
		StolasEntity stolas = stolas(level, perch);
		BoundWill kept = pactOf(level, perch);
		off(() -> {
			helper.assertTrue(stay(level, perch.holder(), s0 + PERIOD_7, s0 + 3 * PERIOD_7).isEmpty() && pactOf(level, perch).equals(kept) && escrowed(level, pact) == 2,
					"Switched off, nothing falls due and nothing is taken");
			helper.assertValueEqual(stolas.think(level, NOON, s0 + PERIOD_7), Status.DISABLED, "Stolas waits, switched off");
			give(keeper, new ItemStack(Items.AMETHYST_SHARD, 1));
			helper.assertTrue(reckon(level, keeper, perch, FULL_MOON, Pacts.DAY + 6000L, s0 + PERIOD_7) == Pacts.Reckoning.DISABLED
					&& AstrolabeItem.charge(charged) == AstrolabeItem.CAPACITY && !recalledEver(level, keeper, FULL_MOON)
					&& PactKeeping.deposit(level, keeper, perch.holder(), Items.AMETHYST_SHARD, s0 + PERIOD_7) == 0 && shards(keeper) == 1
					&& PactKeeping.suspend(level, keeper, perch.holder(), s0 + PERIOD_7) == AgreementHolder.Toggle.DISABLED
					&& key(PactKeeping.end(level, keeper, perch.holder(), StolasEntity.COUNTERPART, s0 + PERIOD_7).said()).equals("message.jugcraft.concordance.disabled")
					&& perch.holder().seat(level, keeper, StolasEntity.COUNTERPART, s0 + PERIOD_7) == AgreementHolder.Seat.DISABLED,
					"nothing is reckoned, paid, suspended, ended or seated");
			helper.assertTrue(observatory.observe(keeper, level, NIGHT_ZERO, s0 + PERIOD_7, ONLY_MOON) == 0 && regard(keeper) == ResearchState.UNDERSTOOD,
					"no observation keeps the first obligation");
			stolas.remove(Entity.RemovalReason.KILLED);
			PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), s0 + PERIOD_7);
			helper.assertTrue(stolas(level, perch) == null && pactOf(level, perch).equals(kept) && escrowed(level, pact) == 2,
					"lost while switched off, he is not called back; the record and escrow are untouched");
		});
		long stolasBack = s0 + 3 * PERIOD_7 + 500L;
		helper.assertTrue(perch.holder().look(level, stolasBack) == Pacts.Event.PAID && escrowed(level, pact) == 1, "On again, one offering at the first look");
		PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), stolasBack + 20);
		helper.assertTrue(stolas(level, perch) != null && reckon(level, keeper, perch, FULL_MOON, Pacts.DAY + 6000L, stolasBack + 40) == Pacts.Reckoning.RECALLED,
				"and Stolas, called back at no cost, reckons again");
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
		return pocketed(player, times, stolasOffer());
	}

	/** Whether {@code player} carries exactly {@code times} of {@code offer}'s compacts' materials (an offer read before the rules change). */
	static boolean pocketed(ServerPlayer player, int times, PactDefinition offer) {
		for (PactDefinition.Cost cost : offer.compact()) {
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
		return id != null && level.getEntity(id) instanceof StolasEntity found && !found.gone() ? found : null;
	}

	static @Nullable BoundWill recordOf(ServerLevel level, ServerPlayer holder) {
		List<BoundWill> held = List.copyOf(BoundWills.of(level.getServer()).ledger().heldBy(holder.getUUID()));
		return held.size() == 1 ? held.getFirst() : null;
	}

	static List<StolasEntity> stolases(GameTestHelper helper) {
		return helper.getLevel().getEntitiesOfClass(StolasEntity.class, helper.getBounds(), found -> found.isAlive() && !found.gone());
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

	// D-05's helpers: Star-reckoning, the first obligation and upkeep on Stolas's own pact.

	static final long PERIOD_7 = 7 * Pacts.DAY;
	/** How often each player's pact recorded its service, heard as the Conclave hears it (every record, repeats included). */
	private static final Map<UUID, Integer> SERVED = new ConcurrentHashMap<>();

	static {
		ConcordanceProgress.listen((player, evidence, result) -> {
			if (evidence instanceof Evidence.Practiced practiced && StolasRegard.ACTIVITY.equals(practiced.activity())) {
				SERVED.merge(player.getUUID(), 1, Integer::sum);
			}
		});
	}

	static int served(ServerPlayer player) {
		return SERVED.getOrDefault(player.getUUID(), 0);
	}

	/** GC-03: how often each player's garden pacts recorded a first obligation, every record heard (as SERVED for Stolas's). */
	private static final Map<UUID, Integer> GARDEN_SERVED = new ConcurrentHashMap<>();
	/** GC-03: every piece of evidence recorded for each player, repeats included (talkingTeachesNothing). */
	private static final Map<UUID, Integer> HEARD = new ConcurrentHashMap<>();

	static {
		ConcordanceProgress.listen((player, evidence, result) -> {
			HEARD.merge(player.getUUID(), 1, Integer::sum);
			if (evidence instanceof Evidence.Practiced practiced && GardenPacts.ACTIVITY.equals(practiced.activity())) {
				GARDEN_SERVED.merge(player.getUUID(), 1, Integer::sum);
			}
		});
	}

	static ResearchState regard(ServerPlayer player) {
		return ConcordanceProgress.knowledge(player).state(StolasRegard.RESEARCH);
	}

	/** Early in a game day, {@code days} game days after now (the quota counts game days). */
	static long dayStart(ServerLevel level, int days) {
		return (Math.floorDiv(level.getGameTime(), Pacts.DAY) + days) * Pacts.DAY + 1_000L;
	}

	static ItemStack holding(ServerPlayer player, int charge) {
		ItemStack held = astrolabe(charge);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return held;
	}

	static ItemStack astrolabe(int charge) {
		ItemStack stack = new ItemStack(Sky.ASTROLABE);
		AstrolabeItem.setCharge(stack, charge);
		return stack;
	}

	static void observed(ServerLevel level, ServerPlayer player, String... patterns) {
		for (String pattern : patterns) {
			AstralClaims.of(level.getServer()).observe(player.getUUID(), pattern, 0L);
		}
	}

	static Pacts.Reckoning reckon(ServerLevel level, ServerPlayer player, PactPerchBlockEntity perch, String pattern, long time, long gameTime) {
		return StarReckoning.reckon(level, player, perch.holder(), pattern, time, gameTime);
	}

	static boolean recalledEver(ServerLevel level, ServerPlayer player, String pattern) {
		return AstralClaims.of(level.getServer()).ledger(player.getUUID()).claimed("recall:" + pattern);
	}

	static int done(ServerLevel level, PactPerchBlockEntity perch, long gameTime) {
		return Pacts.done(pactOf(level, perch).pact(), gameTime);
	}

	static int shards(ServerPlayer player) {
		return player.getInventory().countItem(Items.AMETHYST_SHARD);
	}

	static void give(ServerPlayer player, ItemStack stack) {
		player.getInventory().add(stack);
	}

	static Item starflower() {
		return JugcraftStyx.FLOWERS.get("stolas_starflower").asItem();
	}

	static ObservatoryBlockEntity observatory(GameTestHelper helper, BlockPos at) {
		helper.setBlock(at, Sky.OBSERVATORY);
		return helper.getBlockEntity(at, ObservatoryBlockEntity.class);
	}

	static int looseNear(ServerLevel level, AABB box, Item item) {
		return level.getEntitiesOfClass(ItemEntity.class, box, entity -> entity.isAlive() && entity.getItem().is(item)).stream()
				.mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	static boolean earned(ServerPlayer player, Identifier id) {
		AdvancementHolder advancement = player.level().getServer().getAdvancements().get(id);
		return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
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

	/**
	 * D-04 (D-S8, E5): two conversations at one perch share one visitor; its owner's acceptance seals, and the other player,
	 * not its owner, is not permitted and spends nothing (a second acceptance by a permitted player is TAKEN:
	 * acceptingSealsOnceAndSpendsOnce). A perch no player placed (set by a machine or a command) seals for nobody: Stolas
	 * does not come and accepting is not permitted (the unit review).
	 */
	@GameTest(maxTicks = 20)
	public void twoNegotiationsAtOnePerchSealOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer first = ready(level, TestPlayers.survivor(helper));
		ServerPlayer second = ready(level, TestPlayers.survivor(helper));
		pocket(first, 1, AstrolabeItem.CAPACITY);
		pocket(second, 1, AstrolabeItem.CAPACITY);
		BlockPos at = new BlockPos(3, 1, 3);
		PactPerchBlockEntity perch = perch(helper, at, first);
		beside(helper, first, at);
		beside(helper, second, at);
		PactMenu one = open(helper, first, perch);
		PactMenu two = open(helper, second, perch);
		helper.assertTrue(stolases(helper).size() == 1 && perch.visitor() != null && keys(two.lastSent().notes()).contains(PACT_KEY + "missing.owner"),
				"Two conversations at one perch: one visitor, and the player who is not its owner told so");
		helper.assertTrue(answered(send(first, one, PactRequestPayload.ACCEPT, 0), PactMenu.SEALED), "Its owner accepts: sealed");
		PactStatePayload late = send(second, two, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(answered(late, Pacts.Outcome.NOT_PERMITTED.id) && said(late).equals("message.jugcraft.concordance.workers.seal_not_permitted"),
				"The other player is not permitted");
		helper.assertTrue(pocketed(second, 1) && resonance(second) == AstrolabeItem.CAPACITY
				&& BoundWills.of(level.getServer()).ledger().heldBy(second.getUUID()).isEmpty() && pocketed(first, 0)
				&& recordOf(level, first) != null && stolases(helper).size() == 1, "Only the owner spent and holds; one Stolas");
		BlockPos built = new BlockPos(6, 1, 6);
		PactPerchBlockEntity unowned = perch(helper, built, null);
		beside(helper, second, built);
		PactMenu three = open(helper, second, unowned);
		helper.assertTrue(unowned.holder().owner() == null && unowned.visitor() == null && stolases(helper).size() == 1
				&& keys(three.lastSent().notes()).contains(PACT_KEY + "missing.owner") && !three.lastSent().offers(PactRequestPayload.ACCEPT),
				"At a perch no player placed, Stolas does not come");
		PactStatePayload refused = send(second, three, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(answered(refused, Pacts.Outcome.NOT_PERMITTED.id) && pocketed(second, 1) && resonance(second) == AstrolabeItem.CAPACITY
				&& unowned.holder().agreement() == null && BoundWills.of(level.getServer()).ledger().heldBy(second.getUUID()).isEmpty(),
				"and it seals for nobody, spending nothing");
		second.closeContainer();
		helper.assertTrue(BoundWills.of(level.getServer()).problems().isEmpty(), "no place keeps two");
		helper.succeed();
	}

	/** D-04 (D-S7, D-S9): a visitor with no pact leaves when its conversation ends: declined, closed, timed out, its player gone, or after a restart (no session is saved; a visitor saved with his chunk is no perch's visitor when he loads, and leaves, with the Concordance on or off). Nothing is recorded or noted. */
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
			helper.assertTrue(visitor != null && visitor.think(level, 0L, now) == Status.IDLE && !visitor.gone(), "Stolas waits while it is open");
			return new Visit(player, perch, menu, visitor);
		};
		Visit declined = visit.apply(new BlockPos(1, 1, 1));
		send(declined.player(), declined.menu(), PactRequestPayload.DECLINE, 0);
		helper.assertTrue(declined.visitor().think(level, 0L, now) == Status.FINISHED && declined.visitor().gone(), "Declined: he leaves");
		Visit closed = visit.apply(new BlockPos(4, 1, 1));
		closed.player().closeContainer();
		helper.assertTrue(closed.visitor().think(level, 0L, now) == Status.FINISHED && closed.visitor().gone(), "Closed: he leaves");
		Visit idle = visit.apply(new BlockPos(7, 1, 1));
		helper.assertTrue(idle.visitor().think(level, 0L, now + PactSessions.TIMEOUT_TICKS) == Status.IDLE, "Within its time he waits");
		helper.assertTrue(idle.visitor().think(level, 0L, now + PactSessions.TIMEOUT_TICKS + 1) == Status.FINISHED && idle.visitor().gone(), "Timed out: he leaves");
		Visit gone = visit.apply(new BlockPos(1, 1, 5));
		TestPlayers.leave(gone.player());
		helper.assertTrue(gone.visitor().think(level, 0L, now) == Status.FINISHED && gone.visitor().gone(), "Its player gone: he leaves");
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
		helper.assertTrue(saved.perch().visitor() == null && reloaded.think(level, 0L, now) == Status.FINISHED && reloaded.gone()
				&& saved.visitor().think(level, 0L, now) == Status.FINISHED, "After a restart the perch knows no visitor, and he leaves");
		StolasEntity loadedOff = Workers.STOLAS.create(level, EntitySpawnReason.LOAD);
		loadedOff.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), body));
		off(() -> helper.assertTrue(loadedOff.think(level, 0L, now) == Status.FINISHED && loadedOff.gone(),
				"Loaded while the Concordance is off, a visitor leaves all the same"));
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
		// Read while the shipped offer is loaded: the loop below runs with it refused or missing.
		PactDefinition offer = stolasOffer();
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
						&& pocketed(newcomer, 1, offer) && BoundWills.of(level.getServer()).ledger().heldBy(newcomer.getUUID()).isEmpty(), "accepting spends nothing");
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
	 * a term's key past its bound cannot be encoded, so it is never sent; a choice's id, which data names, encodes at any
	 * length data may give it (the unit review: a long offering id never drops its player).
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
		helper.assertTrue(PactRequestPayload.ACTIONS.subList(0, 9).equals(List.of("choose", "accept", "decline", "recall", "deposit", "suspend",
				"resume", "end", "seat")) && "seat".equals(PactRequestPayload.ACTIONS.get(PactRequestPayload.SEAT))
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
		PactStatePayload.Choice longId = new PactStatePayload.Choice("jugcraft:" + "o".repeat(200), Component.literal("a long offering"), 3, true);
		RegistryFriendlyByteBuf wide = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
		PactStatePayload.CODEC.encode(wide, new PactStatePayload(7, 3, PactStatePayload.KEEP, Component.literal("Stolas"), List.of(), List.of(),
				List.of(), List.of(longId), List.of(longId), List.of(), 0, 0, Optional.empty()));
		PactStatePayload read = PactStatePayload.CODEC.decode(wide);
		helper.assertTrue(read.patterns().equals(List.of(longId)) && read.offerings().equals(List.of(longId)) && wide.readableBytes() == 0,
				"A choice's id longer than a key, as data may name one, is sent and read back");
		helper.succeed();
	}

	// ---------------------------------------------------------------- Concordance Chunk D, D-05: Star-reckoning, the first obligation and keeping

	/** D-05 (D-S12): an observation at an observatory while the pact is active keeps its first obligation: pact_service, once, which masters Stolas's Regard; nothing before a pact, while it is suspended, while its perch, broken with the Concordance off, keeps nothing of it, again, or for a second pact. */
	@GameTest(maxTicks = 20)
	public void theObligationMastersTheEntryOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = regarded(helper);
		ObservatoryBlockEntity observatory = observatory(helper, new BlockPos(0, 1, 6));
		long g0 = level.getGameTime() + 1_000_000L;
		helper.assertTrue(observatory.observe(keeper, level, NIGHT_ZERO, g0, ONLY_MOON) == 1 && regard(keeper) == ResearchState.UNDERSTOOD && served(keeper) == 0,
				"Before any pact, an observation keeps nothing");
		PactPerchBlockEntity perch = stolasPact(helper, keeper, new BlockPos(3, 1, 3), g0);
		helper.assertValueEqual(PactKeeping.suspend(level, keeper, perch.holder(), g0), AgreementHolder.Toggle.SUSPENDED, "Its holder suspends the pact");
		helper.assertTrue(observatory.observe(keeper, level, NIGHT_ZERO, g0 + 20, ONLY_MOON) == 1 && regard(keeper) == ResearchState.UNDERSTOOD && served(keeper) == 0,
				"Suspended, an observation keeps nothing");
		helper.assertValueEqual(PactKeeping.resume(level, keeper, perch.holder(), g0 + 40), AgreementHolder.Toggle.RESUMED, "and resumes it");
		UUID pact = perch.holder().agreement().record();
		off(() -> helper.setBlock(new BlockPos(3, 1, 3), Blocks.AIR));
		BoundWill left = BoundWills.of(level.getServer()).ledger().get(pact);
		helper.assertTrue(Pacts.serving(left) && AgreementHolder.stranded(level.getServer(), left),
				"Broken while switched off, its record still names the vanished place, which keeps nothing of it");
		helper.assertTrue(observatory.observe(keeper, level, NIGHT_ZERO, g0 + 50, ONLY_MOON) == 1 && regard(keeper) == ResearchState.UNDERSTOOD && served(keeper) == 0,
				"Its perch broken while the Concordance was off, the pact keeps nothing there: an observation keeps nothing");
		PactPerchBlockEntity seated = perch(helper, new BlockPos(6, 1, 3), keeper);
		beside(helper, keeper, new BlockPos(6, 1, 3));
		helper.assertValueEqual(seated.holder().seat(level, keeper, StolasEntity.COUNTERPART, g0 + 55), AgreementHolder.Seat.SEATED, "Seated at a new perch");
		helper.assertTrue(observatory.observe(keeper, level, NIGHT_ZERO, g0 + 60, ONLY_MOON) == 1 && regard(keeper) == ResearchState.MASTERED && served(keeper) == 1,
				"Active, the observation keeps the first obligation: Stolas's Regard is mastered");
		helper.assertTrue(ConcordanceProgress.knowledge(keeper).progress(StolasRegard.RESEARCH).evidence().containsKey(StarReckoning.OBLIGATION_KEY)
				&& StarReckoning.kept(keeper) && earned(keeper, ConcordanceProgress.advancementFor(StolasRegard.RESEARCH, ResearchState.MASTERED)),
				"recorded as the practice jugcraft:pact_service of jugcraft:stolas, with its advancement");
		helper.assertTrue(observatory.observe(keeper, level, NIGHT_ZERO + 8 * Pacts.DAY, g0 + 80, ONLY_MOON) == 1 && served(keeper) == 1,
				"A second observation records nothing more: the Conclave hears it once");
		helper.assertValueEqual(seated.holder().end(level, keeper, g0 + 100), AgreementHolder.Ended.ENDED, "Ended");
		stolasPact(helper, keeper, new BlockPos(5, 1, 5), g0 + 120);
		helper.assertTrue(observatory.observe(keeper, level, NIGHT_ZERO, g0 + 140, ONLY_MOON) == 1 && served(keeper) == 1, "nor for a second pact");
		helper.succeed();
	}

	/**
	 * D-05 (D-S1, D-S9): suspended by its holder or for upkeep, the pact recalls nothing and spends nothing; suspend and resume
	 * are idempotent; the time suspended is not owed; only an offering lifts an upkeep hold.
	 */
	@GameTest(maxTicks = 20)
	public void aSuspendedPactRecallsNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = regarded(helper);
		long g0 = dayStart(level, 100);
		PactPerchBlockEntity perch = stolasPact(helper, keeper, new BlockPos(3, 1, 3), g0);
		observed(level, keeper, FULL_MOON, HARVEST_MOON);
		ItemStack held = holding(keeper, AstrolabeItem.CAPACITY);
		helper.assertValueEqual(PactKeeping.suspend(level, keeper, perch.holder(), g0), AgreementHolder.Toggle.SUSPENDED, "Suspended");
		long since = pactOf(level, perch).pact().holdSince();
		helper.assertTrue(PactKeeping.suspend(level, keeper, perch.holder(), g0 + 10) == AgreementHolder.Toggle.SUSPENDED && pactOf(level, perch).pact().holdSince() == since,
				"Suspending again changes nothing");
		helper.assertTrue(reckon(level, keeper, perch, FULL_MOON, Pacts.DAY + 6000, g0) == Pacts.Reckoning.SUSPENDED && AstrolabeItem.charge(held) == AstrolabeItem.CAPACITY
				&& !recalledEver(level, keeper, FULL_MOON) && done(level, perch, g0) == 0, "Suspended, Stolas reckons nothing and nothing is spent");
		helper.assertValueEqual(stolas(level, perch).think(level, NOON, g0), Status.SUSPENDED, "and he says so");
		helper.assertTrue(PactKeeping.resume(level, keeper, perch.holder(), g0 + 20) == AgreementHolder.Toggle.RESUMED
				&& PactKeeping.resume(level, keeper, perch.holder(), g0 + 30) == AgreementHolder.Toggle.RESUMED, "Resumed, and resuming again is no toggle");
		long due = pactOf(level, perch).pact().upkeep().due();
		helper.assertValueEqual(due, g0 + PERIOD_7 + 20, "the time suspended is not owed: the first offering falls due that much later");
		helper.assertValueEqual(reckon(level, keeper, perch, FULL_MOON, Pacts.DAY + 6000, g0 + 40), Pacts.Reckoning.RECALLED, "Active, he reckons");
		helper.assertTrue(stay(level, perch.holder(), g0 + 1_000L, due - 1_000L).isEmpty() && perch.holder().look(level, due) == Pacts.Event.WARNED
				&& stay(level, perch.holder(), due + 1_000L, due + Pacts.DAY - 1_000L).isEmpty()
				&& perch.holder().look(level, due + Pacts.DAY) == Pacts.Event.SUSPENDED, "Unpaid past its grace, it is held for its upkeep");
		long h = due + Pacts.DAY;
		int charge = AstrolabeItem.charge(held);
		helper.assertTrue(reckon(level, keeper, perch, HARVEST_MOON, h, h) == Pacts.Reckoning.UPKEEP_HELD && AstrolabeItem.charge(held) == charge,
				"Held for upkeep, he reckons nothing");
		helper.assertTrue(PactKeeping.suspend(level, keeper, perch.holder(), h) == AgreementHolder.Toggle.HELD_FOR_UPKEEP
				&& PactKeeping.resume(level, keeper, perch.holder(), h) == AgreementHolder.Toggle.HELD_FOR_UPKEEP, "and the holder's word neither adds to nor lifts it");
		give(keeper, new ItemStack(Items.AMETHYST_SHARD, 1));
		helper.assertTrue(PactKeeping.deposit(level, keeper, perch.holder(), Items.AMETHYST_SHARD, h + 500L) == 1 && perch.holder().held(level) == null
				&& reckon(level, keeper, perch, HARVEST_MOON, h + 600L, h + 600L) == Pacts.Reckoning.RECALLED, "An offering resumes it, and he reckons again");
		helper.succeed();
	}

	/** D-05 (D-S1): outside its hours or area (or in another level), the day's quota spent, no astrolabe or too little resonance: each refused with its reason, nothing spent; a second astrolabe holding enough pays. */
	@GameTest(maxTicks = 20)
	public void theQuotaAndTheAreaRefuseWithReasons(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = regarded(helper);
		BlockPos at = new BlockPos(3, 1, 3);
		long g0 = dayStart(level, 50);
		PactPerchBlockEntity perch = perch(helper, at, keeper);
		beside(helper, keeper, at);
		BoundWill.Service night = new BoundWill.Service(StarReckoning.WORK, 8, 13000, 23000, 1);
		helper.assertValueEqual(perch.holder().seal(level, keeper, withService(stolasTerms(), night), g0).outcome(), Pacts.Outcome.DONE,
				"A pact whose hours are the night (test terms; Stolas's own are any hour)");
		observed(level, keeper, FULL_MOON, LANTERN_STAR);
		ItemStack held = holding(keeper, AstrolabeItem.CAPACITY);
		long noon = Pacts.DAY + 6000;
		long midnight = Pacts.DAY + 18000;
		helper.assertTrue(reckon(level, keeper, perch, FULL_MOON, noon, g0) == Pacts.Reckoning.OUTSIDE_HOURS && AstrolabeItem.charge(held) == AstrolabeItem.CAPACITY,
				"Outside its hours: refused, nothing spent");
		helper.assertValueEqual(key(StarReckoning.told(Pacts.Reckoning.OUTSIDE_HOURS, null, night)), "message.jugcraft.concordance.pact.reckoning.outside_hours", "and told so");
		place(helper, keeper, at.getX() + 12.5, at.getY(), at.getZ() + 0.5);
		helper.assertValueEqual(reckon(level, keeper, perch, FULL_MOON, midnight, g0), Pacts.Reckoning.OUTSIDE_AREA, "Twelve blocks off: outside its area");
		helper.assertValueEqual(Pacts.serves(pactOf(level, perch), new Place("minecraft:the_nether", 0, 0, 0), midnight, g0), Pacts.Reckoning.OUTSIDE_AREA,
				"In another level: outside its area");
		beside(helper, keeper, at);
		helper.assertTrue(reckon(level, keeper, perch, FULL_MOON, midnight, g0) == Pacts.Reckoning.RECALLED && done(level, perch, g0) == 1, "At the perch at night: recalled");
		int after = AstrolabeItem.charge(held);
		helper.assertTrue(reckon(level, keeper, perch, LANTERN_STAR, midnight + 1000, g0 + 1000) == Pacts.Reckoning.QUOTA_SPENT
				&& reckon(level, keeper, perch, LANTERN_STAR, midnight + 8 * Pacts.DAY, g0 + 2000) == Pacts.Reckoning.QUOTA_SPENT
				&& AstrolabeItem.charge(held) == after, "The day's quota spent, whatever the clock says");
		long g1 = g0 + Pacts.DAY;
		keeper.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		helper.assertTrue(reckon(level, keeper, perch, LANTERN_STAR, midnight + Pacts.DAY, g1) == Pacts.Reckoning.NO_ASTROLABE && done(level, perch, g1) == 0,
				"The next game day, no astrolabe: refused");
		ItemStack poor = holding(keeper, 5);
		helper.assertTrue(reckon(level, keeper, perch, LANTERN_STAR, midnight + Pacts.DAY, g1) == Pacts.Reckoning.TOO_LITTLE && AstrolabeItem.charge(poor) == 5,
				"Too little resonance for its recall: refused");
		ItemStack rich = astrolabe(AstrolabeItem.CAPACITY);
		keeper.getInventory().setItem(9, rich);
		helper.assertTrue(reckon(level, keeper, perch, LANTERN_STAR, midnight + Pacts.DAY, g1) == Pacts.Reckoning.RECALLED && AstrolabeItem.charge(poor) == 5
				&& AstrolabeItem.charge(rich) == AstrolabeItem.CAPACITY - Sky.catalog().pattern(LANTERN_STAR).recallCost(), "The astrolabe that holds enough pays");
		helper.succeed();
	}

	/** D-05 (E2, D-S3): with the absent-owner option on, a stand-in answers protection questions for the holder but never for upkeep; it pays, reckons, ends and suspends nothing. */
	@GameTest(maxTicks = 20)
	public void aStandInNeverAnswersForUpkeep(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = regarded(helper);
		UUID id = keeper.getUUID();
		BlockPos at = new BlockPos(3, 1, 3);
		long s0 = level.getGameTime() + 1_000_000L;
		PactPerchBlockEntity perch = stolasPact(helper, keeper, at, s0);
		UUID record = perch.holder().agreement().record();
		give(keeper, new ItemStack(Items.AMETHYST_SHARD, 2));
		helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), Items.AMETHYST_SHARD, s0), 2, "Two periods paid ahead");
		BoundWill.Upkeep before = pactOf(level, perch).pact().upkeep();
		TestPlayers.leave(keeper);
		boolean was = JugcraftConfig.option(Authority.ABSENT_OPTION);
		JugcraftConfig.setOption(Authority.ABSENT_OPTION, true);
		try {
			ServerPlayer answering = Authority.answering(level, id);
			helper.assertTrue(answering != null && Authority.isStandIn(answering), "A stand-in answers protection questions for the absent holder");
			for (long t = s0 + Pacts.DAY; t <= s0 + 3 * PERIOD_7; t += Pacts.DAY) {
				helper.assertValueEqual(perch.holder().look(level, t), Pacts.Event.NONE, "but never for upkeep");
				PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), t);
			}
			helper.assertTrue(escrowed(level, record) == 2 && pactOf(level, perch).pact().upkeep().equals(before), "Nothing is taken, warned or moved");
			ItemStack offered = new ItemStack(Items.AMETHYST_SHARD, 1);
			helper.assertTrue(perch.holder().deposit(level, answering, offered, s0) == 0 && offered.getCount() == 1 && escrowed(level, record) == 2
					&& reckon(level, answering, perch, FULL_MOON, Pacts.DAY + 6000, s0 + 100) == Pacts.Reckoning.NOT_HOLDER
					&& perch.holder().end(level, answering, s0 + 100) == AgreementHolder.Ended.NOT_PERMITTED
					&& PactKeeping.suspend(level, answering, perch.holder(), s0 + 100) == AgreementHolder.Toggle.NOT_HOLDER,
					"and it pays (a real offering in hand), reckons, ends and suspends nothing for them");
		} finally {
			JugcraftConfig.setOption(Authority.ABSENT_OPTION, was);
		}
		ServerPlayer back = TestPlayers.again(helper, id);
		beside(helper, back, at);
		long tr = s0 + 3 * PERIOD_7 + 500L;
		helper.assertTrue(perch.holder().look(level, tr) == Pacts.Event.PAID && escrowed(level, record) == 1, "Back in person, one period falls due: one offering");
		helper.succeed();
	}

	/** D-05 (E4, D-S6): its living holder ends the pact: what was paid ahead comes back once (into the inventory, the rest at their feet); Stolas departs and is struck off; who, why and how many told; the reason and place kept once; ending again returns nothing; the perch stays. */
	@GameTest(maxTicks = 20)
	public void endingReturnsTheEscrowOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = regarded(helper);
		BlockPos at = new BlockPos(3, 1, 3);
		long g0 = level.getGameTime() + 1_000_000L;
		PactPerchBlockEntity perch = stolasPact(helper, keeper, at, g0);
		UUID record = perch.holder().agreement().record();
		StolasEntity stolas = stolas(level, perch);
		give(keeper, new ItemStack(Items.AMETHYST_SHARD, 3));
		give(keeper, new ItemStack(starflower(), 1));
		helper.assertTrue(PactKeeping.deposit(level, keeper, perch.holder(), Items.AMETHYST_SHARD, g0) == 3
				&& PactKeeping.deposit(level, keeper, perch.holder(), starflower(), g0) == 1 && escrowed(level, record) == 4, "Four offerings paid ahead, the most it holds");
		Inventory inventory = keeper.getInventory();
		for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
			inventory.setItem(slot, slot == 0 ? new ItemStack(Items.AMETHYST_SHARD, 63) : new ItemStack(Items.STONE, 64));
		}
		Component said = PactKeeping.end(level, keeper, perch.holder(), StolasEntity.COUNTERPART, g0 + 100).said();
		helper.assertTrue(key(said).equals("message.jugcraft.concordance.pact.departed") && Integer.valueOf(4).equals(arg(said, 2)),
				"Its holder ends it, told who departs, why and what came back: " + said);
		AABB feet = keeper.getBoundingBox().inflate(3.0);
		helper.assertTrue(inventory.countItem(Items.AMETHYST_SHARD) == 64 && looseNear(level, feet, Items.AMETHYST_SHARD) == 2 && looseNear(level, feet, starflower()) == 1,
				"What was paid ahead comes back once: what fits into the inventory, the rest at their feet");
		BoundWills wills = BoundWills.of(level.getServer());
		helper.assertTrue(wills.ledger().get(record) == null && wills.escrow(record).isEmpty() && stolas.gone()
				&& !WorkerRoster.of(level.getServer()).workers(keeper.getUUID()).containsKey(stolas.getUUID()) && perch.holder().agreement() == null,
				"The record and its escrow go; Stolas departs and is struck off");
		List<Pacts.Departure> history = PactHistory.of(level.getServer()).history(keeper.getUUID());
		helper.assertTrue(history.size() == 1 && history.getFirst().record().equals(record) && Pacts.ENDED.equals(history.getFirst().reason())
				&& Couriers.place(level, helper.absolutePos(at)).equals(history.getFirst().place()), "The departure is kept once, with its reason and place: " + history);
		helper.assertTrue(perch.holder().end(level, keeper, g0 + 200) == AgreementHolder.Ended.NOTHING
				&& AgreementHolder.end(level, null, keeper, record, Pacts.ENDED, g0 + 200) == AgreementHolder.Ended.NOTHING
				&& inventory.countItem(Items.AMETHYST_SHARD) == 64 && looseNear(level, feet, Items.AMETHYST_SHARD) == 2
				&& PactHistory.of(level.getServer()).history(keeper.getUUID()).size() == 1, "Ending again returns nothing more");
		helper.assertTrue(helper.getBlockState(at).is(Workers.PACT_PERCH), "and the perch stays");
		helper.succeed();
	}

	/** D-05 (E4, D-S6): a holder who is not alive is never handed what was paid ahead, at the perch or by record; it stays with the record until they live. */
	@GameTest(maxTicks = 20)
	public void aDeadHolderIsNeverHandedTheEscrow(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = regarded(helper);
		long g0 = level.getGameTime() + 1_000_000L;
		PactPerchBlockEntity perch = stolasPact(helper, keeper, new BlockPos(3, 1, 3), g0);
		UUID record = perch.holder().agreement().record();
		give(keeper, new ItemStack(Items.AMETHYST_SHARD, 2));
		helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), Items.AMETHYST_SHARD, g0), 2, "Two paid ahead");
		keeper.setHealth(0.0F);
		helper.assertTrue(key(PactKeeping.end(level, keeper, perch.holder(), StolasEntity.COUNTERPART, g0).said()).equals("message.jugcraft.concordance.workers.end_not_permitted")
				&& AgreementHolder.end(level, null, keeper, record, Pacts.ENDED, g0) == AgreementHolder.Ended.NOT_PERMITTED, "Not alive, they cannot end it, here or by record");
		helper.assertTrue(shards(keeper) == 0 && escrowed(level, record) == 2 && BoundWills.of(level.getServer()).ledger().get(record) != null
				&& PactHistory.of(level.getServer()).history(keeper.getUUID()).isEmpty(), "what was paid ahead stays with the record");
		keeper.setHealth(keeper.getMaxHealth());
		helper.assertTrue(perch.holder().end(level, keeper, g0) == AgreementHolder.Ended.ENDED && shards(keeper) == 2, "Alive again, they end it and it comes back");
		helper.succeed();
	}

	/** D-05 (D-S6, E4): breaking the perch ends nothing: the pact is without its perch, its holder, terms and escrow kept, no departure; Stolas leaves and is struck off; nothing is reckoned or kept as an obligation; ended from the roster, the escrow comes back once. */
	@GameTest(maxTicks = 20)
	public void aBrokenPerchLeavesThePactWithoutItsPerch(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = regarded(helper);
		BlockPos at = new BlockPos(2, 1, 2);
		long g0 = dayStart(level, 100);
		PactPerchBlockEntity perch = stolasPact(helper, keeper, at, g0);
		UUID record = perch.holder().agreement().record();
		give(keeper, new ItemStack(Items.AMETHYST_SHARD, 2));
		helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), Items.AMETHYST_SHARD, g0), 2, "Two paid ahead");
		observed(level, keeper, FULL_MOON);
		holding(keeper, AstrolabeItem.CAPACITY);
		StolasEntity stolas = stolas(level, perch);
		BoundWill before = pactOf(level, perch);
		helper.setBlock(at, Blocks.AIR);
		BoundWill kept = BoundWills.of(level.getServer()).ledger().get(record);
		helper.assertTrue(kept != null && keeper.getUUID().equals(kept.holder()) && !Pacts.seated(kept) && kept.pact().presence() == null
				&& kept.pact().upkeep().equals(before.pact().upkeep()) && kept.pact().service().equals(before.pact().service()) && escrowed(level, record) == 2,
				"Broken, the perch ends nothing: the pact is without its perch, its terms and escrow kept");
		helper.assertTrue(stolas.gone() && !WorkerRoster.of(level.getServer()).workers(keeper.getUUID()).containsKey(stolas.getUUID())
				&& looseNear(level, new AABB(helper.absolutePos(at)).inflate(2.0), Items.AMETHYST_SHARD) == 0
				&& PactHistory.of(level.getServer()).history(keeper.getUUID()).isEmpty(), "Stolas leaves the world; nothing drops; nothing departed");
		PactPerchBlockEntity other = perch(helper, new BlockPos(5, 1, 5), keeper);
		beside(helper, keeper, new BlockPos(5, 1, 5));
		helper.assertValueEqual(StarReckoning.reckon(level, keeper, other.holder(), FULL_MOON, Pacts.DAY + 6000, g0), Pacts.Reckoning.NOTHING,
				"Without its perch, Stolas reckons nowhere");
		ObservatoryBlockEntity observatory = observatory(helper, new BlockPos(0, 1, 6));
		helper.assertTrue(observatory.observe(keeper, level, NIGHT_ZERO, g0 + 20, ONLY_MOON) == 1 && regard(keeper) == ResearchState.UNDERSTOOD,
				"and an observation keeps no obligation");
		helper.assertTrue(AgreementHolder.end(level, null, keeper, record, Pacts.ENDED, g0 + 40) == AgreementHolder.Ended.ENDED && shards(keeper) == 2
				&& PactHistory.of(level.getServer()).history(keeper.getUUID()).getFirst().place() == null, "Ended from the roster, the escrow comes back once");
		helper.succeed();
	}

	/** D-05 (D-S6, D-S8): a pact without its perch is seated again only by its holder at a perch they own: the same record, terms, today's count, upkeep clock and escrow, Stolas called back at no cost; the time it waited is not owed; one broken while switched off is seated too, through the perch's screen. */
	@GameTest(maxTicks = 20)
	public void aPactIsSeatedAgainAtANewPerch(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = regarded(helper);
		ServerPlayer stranger = regarded(helper);
		long g0 = dayStart(level, 100);
		PactPerchBlockEntity first = stolasPact(helper, keeper, new BlockPos(2, 1, 2), g0);
		UUID record = first.holder().agreement().record();
		observed(level, keeper, FULL_MOON, LANTERN_STAR);
		holding(keeper, AstrolabeItem.CAPACITY);
		helper.assertValueEqual(reckon(level, keeper, first, FULL_MOON, Pacts.DAY + 6000, g0), Pacts.Reckoning.RECALLED, "Today's reckoning made");
		give(keeper, new ItemStack(Items.AMETHYST_SHARD, 2));
		helper.assertValueEqual(PactKeeping.deposit(level, keeper, first.holder(), Items.AMETHYST_SHARD, g0), 2, "Two paid ahead");
		BoundWill before = pactOf(level, first);
		helper.setBlock(new BlockPos(2, 1, 2), Blocks.AIR);
		PactPerchBlockEntity theirs = perch(helper, new BlockPos(5, 1, 2), stranger);
		beside(helper, keeper, new BlockPos(5, 1, 2));
		helper.assertTrue(theirs.holder().seat(level, keeper, StolasEntity.COUNTERPART, g0 + 100) == AgreementHolder.Seat.NOT_PERMITTED
				&& theirs.holder().seat(level, stranger, StolasEntity.COUNTERPART, g0 + 100) == AgreementHolder.Seat.NOTHING && theirs.holder().agreement() == null,
				"Not at another's perch; and its owner has no pact to seat");
		PactPerchBlockEntity mine = perch(helper, new BlockPos(5, 1, 5), keeper);
		beside(helper, keeper, new BlockPos(5, 1, 5));
		helper.assertValueEqual(mine.holder().seat(level, FakePlayer.get(level, new GameProfile(keeper.getUUID(), "[Concordance]")), StolasEntity.COUNTERPART, g0 + 150),
				AgreementHolder.Seat.NOT_HOLDER, "Never by a stand-in");
		helper.assertValueEqual(mine.holder().seat(level, keeper, StolasEntity.COUNTERPART, g0 + 200), AgreementHolder.Seat.SEATED, "Its holder seats it at a perch they own");
		BoundWill after = BoundWills.of(level.getServer()).ledger().get(record);
		helper.assertTrue(after != null && Couriers.place(level, helper.absolutePos(new BlockPos(5, 1, 5))).equals(after.pact().place())
				&& after.pact().presence().equals(mine.holder().spirit()) && stolas(level, mine) != null
				&& WorkerRoster.of(level.getServer()).workers(keeper.getUUID()).containsKey(mine.holder().spirit()), "the same record, Stolas called back and on the roster");
		helper.assertTrue(after.pact().service().equals(before.pact().service()) && after.pact().upkeep().equals(before.pact().upkeep())
				&& after.pact().window() == before.pact().window() && after.pact().doneToday() == before.pact().doneToday() && escrowed(level, record) == 2
				&& mine.holder().agreement().record().equals(record), "with its terms, today's count, upkeep clock and escrow");
		helper.assertValueEqual(reckon(level, keeper, mine, LANTERN_STAR, Pacts.DAY + 7000, g0 + 300), Pacts.Reckoning.QUOTA_SPENT, "Today's quota came with it");
		helper.assertValueEqual(perch(helper, new BlockPos(2, 1, 5), keeper).holder().seat(level, keeper, StolasEntity.COUNTERPART, g0 + 400), AgreementHolder.Seat.NOTHING,
				"Seated, it is seated nowhere else");
		long back = g0 + 10 * Pacts.DAY;
		helper.assertTrue(mine.holder().look(level, back) == Pacts.Event.PAID && escrowed(level, record) == 1
				&& pactOf(level, mine).pact().upkeep().due() == back + PERIOD_7, "The time it waited is not owed: one offering, the next a week on");
		BlockPos mineAt = new BlockPos(5, 1, 5);
		off(() -> helper.setBlock(mineAt, Blocks.AIR));
		helper.assertTrue(Pacts.seated(BoundWills.of(level.getServer()).ledger().get(record)), "Broken while switched off, its record still names the vanished place");
		PactPerchBlockEntity again = perch(helper, new BlockPos(6, 1, 2), keeper);
		beside(helper, keeper, new BlockPos(6, 1, 2));
		PactMenu menu = open(helper, keeper, again);
		helper.assertTrue(menu.lastSent().mode() == PactStatePayload.SEAT && menu.lastSent().offers(PactRequestPayload.SEAT)
				&& menu.lastSent().offers(PactRequestPayload.END) && !menu.lastSent().offers(PactRequestPayload.ACCEPT),
				"At a perch of theirs keeping nothing, the screen offers to seat the pact here, or to end it");
		PactStatePayload seated = send(keeper, menu, PactRequestPayload.SEAT, 0);
		helper.assertTrue(answered(seated, AgreementHolder.Seat.SEATED.id) && seated.result().get().done() && seated.mode() == PactStatePayload.KEEP
				&& again.holder().agreement() != null && again.holder().agreement().record().equals(record) && BoundWills.of(level.getServer()).problems().isEmpty(),
				"yet, its old place keeping nothing of it, it is seated again through the perch's screen");
		keeper.closeContainer();
		helper.succeed();
	}

	/** D-05 (D-S7): a lost Stolas never ends the pact: the perch calls him back at no cost while his holder is near and the Concordance on; not when they are far or it is off. */
	@GameTest(maxTicks = 20)
	public void aLostPresenceIsCalledBackAndEndsNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = regarded(helper);
		BlockPos at = new BlockPos(3, 1, 3);
		long g0 = level.getGameTime() + 1_000_000L;
		PactPerchBlockEntity perch = stolasPact(helper, keeper, at, g0);
		UUID record = perch.holder().agreement().record();
		give(keeper, new ItemStack(Items.AMETHYST_SHARD, 1));
		PactKeeping.deposit(level, keeper, perch.holder(), Items.AMETHYST_SHARD, g0);
		StolasEntity first = stolas(level, perch);
		first.remove(Entity.RemovalReason.KILLED);
		helper.assertTrue(first.isRemoved() && perch.holder().spiritLost(level) && BoundWills.of(level.getServer()).ledger().get(record) != null
				&& escrowed(level, record) == 1 && PactHistory.of(level.getServer()).history(keeper.getUUID()).isEmpty(), "Lost, he ends nothing");
		PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 20);
		StolasEntity second = stolas(level, perch);
		helper.assertTrue(second != null && !second.getUUID().equals(first.getUUID()) && second.getUUID().equals(pactOf(level, perch).pact().presence())
				&& WorkerRoster.of(level.getServer()).workers(keeper.getUUID()).containsKey(second.getUUID()) && escrowed(level, record) == 1 && shards(keeper) == 0,
				"His holder near, the perch calls him back under the same record, at no cost");
		second.remove(Entity.RemovalReason.KILLED);
		place(helper, keeper, at.getX() + 100.5, at.getY(), at.getZ() + 0.5);
		PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 40);
		helper.assertTrue(stolas(level, perch) == null && perch.holder().spiritLost(level), "His holder far away, nobody is called");
		beside(helper, keeper, at);
		off(() -> PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 60));
		helper.assertTrue(stolas(level, perch) == null, "Switched off, nobody is called");
		PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 80);
		helper.assertTrue(stolas(level, perch) != null && BoundWills.of(level.getServer()).ledger().get(record) != null
				&& PactHistory.of(level.getServer()).history(keeper.getUUID()).isEmpty(), "Back near it, he returns; the pact never ended");
		helper.succeed();
	}

	// ---------------------------------------------------------------- Concordance Chunk D, D-06: Stolas's body and his leaving

	/**
	 * D-06 (D-S7): leaving, Stolas makes his departing gesture and is gone when it ends: struck off at once, never saved
	 * meanwhile, never taken for the perch's visitor or presence again; a new invitation summons a new Stolas. A declined
	 * visitor and an ended pact's presence alike. He listens while the screen is open; nothing of this is saved.
	 */
	@GameTest(maxTicks = 80)
	public void aDepartingStolasIsNeverAVisitorAgain(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		long now = level.getGameTime();
		ServerPlayer guest = ready(level, TestPlayers.mock(helper));
		BlockPos at = new BlockPos(1, 1, 1);
		PactPerchBlockEntity perch = perch(helper, at, guest);
		beside(helper, guest, at);
		PactMenu menu = open(helper, guest, perch);
		StolasEntity visitor = (StolasEntity) level.getEntity(perch.visitor());
		helper.assertTrue(visitor != null && visitor.think(level, 0L, now) == Status.IDLE && visitor.listening() && !visitor.gone()
				&& visitor.equals(perch.stolas(level)), "Invited, he waits at the perch, listening");
		send(guest, menu, PactRequestPayload.DECLINE, 0);
		helper.assertTrue(visitor.think(level, 0L, now) == Status.FINISHED && visitor.departing() && !visitor.isRemoved() && visitor.gone()
				&& !visitor.shouldBeSaved() && perch.stolas(level) == null && "depart".equals(visitor.cues().getLast()),
				"Declined, he departs: gone for the perch, not saved, still in the world for his gesture, its sound with it: " + visitor.cues());
		helper.assertTrue(!visitor.listening() && visitor.leaving(),
				"Leaving, he listens no more, and his departure is synced: one who starts watching him now sees him gone (the unit review)");
		helper.assertTrue(visitor.think(level, 0L, now) == Status.FINISHED && visitor.departing(), "Thinking again changes nothing");
		open(helper, guest, perch);
		StolasEntity second = (StolasEntity) level.getEntity(perch.visitor());
		helper.assertTrue(second != null && !second.equals(visitor) && !second.gone() && second.equals(perch.stolas(level))
				&& "arrive".equals(second.cues().getLast()), "A new invitation summons a new Stolas, never the departing one; he alights");
		ServerPlayer keeper = ready(level, TestPlayers.survivor(helper));
		PactPerchBlockEntity home = stolasPact(helper, keeper, new BlockPos(5, 1, 5), now);
		StolasEntity presence = stolas(level, home);
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		presence.saveWithoutId(out);
		CompoundTag body = out.buildResult();
		helper.assertTrue(!body.contains("listening") && !body.contains("departing") && !body.contains("cues"), "Nothing of his presentation is saved: " + body);
		helper.assertTrue(PactKeeping.end(level, keeper, home.holder(), StolasEntity.COUNTERPART, now).ended() == AgreementHolder.Ended.ENDED
				&& presence.departing() && !presence.isRemoved() && !presence.shouldBeSaved() && home.stolas(level) == null
				&& !WorkerRoster.of(level.getServer()).workers(keeper.getUUID()).containsKey(presence.getUUID())
				&& "depart".equals(presence.cues().getLast()), "Ended, his presence departs at once: struck off, never saved; heard to go");
		helper.runAfterDelay(StolasEntity.Gesture.DEPART.ticks + 2, () -> {
			helper.assertTrue(visitor.isRemoved() && presence.isRemoved() && !second.isRemoved(),
					"Their departures end with them gone; the new visitor, its screen still open, stays");
			helper.succeed();
		});
	}

	/**
	 * D-06 (the unit review): a departure ends on time where his entity does not tick (his chunk tracked but not ticking, or
	 * unloaded around him, since a departing Stolas is never saved): the server's sweep discards him when it ends, never
	 * before. The sweep runs here at explicit game times, with no tick between.
	 */
	@GameTest(maxTicks = 20)
	public void aDepartureEndsWithoutHisTicks(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer guest = ready(level, TestPlayers.mock(helper));
		BlockPos at = new BlockPos(1, 1, 1);
		PactPerchBlockEntity perch = perch(helper, at, guest);
		beside(helper, guest, at);
		open(helper, guest, perch);
		StolasEntity visitor = (StolasEntity) level.getEntity(perch.visitor());
		helper.assertTrue(visitor != null && visitor.listening() && !visitor.leaving() && !visitor.departing(), "Invited, he listens at the perch");
		long now = level.getGameTime();
		visitor.leave(level, null, perch.getBlockPos());
		helper.assertTrue(visitor.departing() && visitor.leaving() && !visitor.listening() && !visitor.isRemoved() && !visitor.shouldBeSaved(),
				"Leaving, he listens no more, his departure synced and never saved");
		StolasEntity.sweep(now + StolasEntity.Gesture.DEPART.ticks - 1);
		helper.assertTrue(!visitor.isRemoved(), "The sweep never ends a departure before its gesture's length");
		StolasEntity.sweep(now + StolasEntity.Gesture.DEPART.ticks);
		helper.assertTrue(visitor.isRemoved() && visitor.getRemovalReason() == Entity.RemovalReason.DISCARDED,
				"Its length over, the sweep discards him, whether or not he ticked: " + visitor.getRemovalReason());
		helper.succeed();
	}

	// D-06: his cues (the server fires each with its sound), his one presence a pact, and Jade's readings.

	static int offering(PactStatePayload state, String item) {
		for (int i = 0; i < state.offerings().size(); i++) {
			if (state.offerings().get(i).id().equals(item)) {
				return i;
			}
		}
		return -1;
	}

	/** D-06 (03:159): which gesture answers which action (none for the menu's own answers), which loop each state shows, every cue's sound registered. */
	@GameTest(maxTicks = 20)
	public void stolasGesturesAndSoundsFollowTheirTable(GameTestHelper helper) {
		record Reaction(int action, String reason, boolean done, StolasEntity.@Nullable Gesture gesture) {
		}
		for (Reaction row : List.of(new Reaction(PactRequestPayload.ACCEPT, PactMenu.SEALED, true, StolasEntity.Gesture.ASSENT),
				new Reaction(PactRequestPayload.ACCEPT, Pacts.Outcome.CANNOT_AFFORD.id, false, StolasEntity.Gesture.REFUSE),
				new Reaction(PactRequestPayload.ACCEPT, PactMenu.MISSING_EVIDENCE, false, StolasEntity.Gesture.REFUSE),
				new Reaction(PactRequestPayload.ACCEPT, Pacts.Outcome.TAKEN.id, false, StolasEntity.Gesture.REFUSE),
				new Reaction(PactRequestPayload.ACCEPT, PactMenu.NOT_OFFERED, false, null),
				new Reaction(PactRequestPayload.RECALL, Pacts.Reckoning.RECALLED.id, true, StolasEntity.Gesture.INSPECT),
				new Reaction(PactRequestPayload.RECALL, Pacts.Reckoning.QUOTA_SPENT.id, false, StolasEntity.Gesture.REFUSE),
				new Reaction(PactRequestPayload.RECALL, PactMenu.NOT_OFFERED, false, null),
				new Reaction(PactRequestPayload.DEPOSIT, PactMenu.DEPOSITED, true, StolasEntity.Gesture.INSPECT),
				new Reaction(PactRequestPayload.DEPOSIT, PactMenu.NOTHING_DEPOSITED, false, null),
				new Reaction(PactRequestPayload.RESUME, "resumed", true, StolasEntity.Gesture.ASSENT),
				new Reaction(PactRequestPayload.RESUME, "held_for_upkeep", false, null),
				new Reaction(PactRequestPayload.SUSPEND, "suspended", true, null),
				new Reaction(PactRequestPayload.DECLINE, PactMenu.DECLINED, true, null),
				new Reaction(PactRequestPayload.END, "ended", true, null),
				new Reaction(PactRequestPayload.SEAT, AgreementHolder.Seat.SEATED.id, true, null),
				new Reaction(PactRequestPayload.CHOOSE, "", true, null))) {
			helper.assertTrue(StolasEntity.reaction(row.action(), row.reason(), row.done()) == row.gesture(),
					PactRequestPayload.ACTIONS.get(row.action()) + " answered " + row.reason() + " is answered by " + row.gesture());
		}
		record Shown(Status status, boolean listening, StolasEntity.Loop loop) {
		}
		for (Shown row : List.of(new Shown(Status.IDLE, false, StolasEntity.Loop.IDLE), new Shown(Status.IDLE, true, StolasEntity.Loop.LISTEN),
				new Shown(Status.SUSPENDED, false, StolasEntity.Loop.SUSPENDED), new Shown(Status.SUSPENDED, true, StolasEntity.Loop.LISTEN),
				new Shown(Status.UPKEEP_HELD, false, StolasEntity.Loop.SUSPENDED), new Shown(Status.UPKEEP_DUE, false, StolasEntity.Loop.ASTIR),
				new Shown(Status.OWNER_OFFLINE, false, StolasEntity.Loop.WAITING), new Shown(Status.DESTINATION_UNLOADED, false, StolasEntity.Loop.WAITING),
				new Shown(Status.OUTSIDE_AGREEMENT, false, StolasEntity.Loop.WAITING), new Shown(Status.DISABLED, true, StolasEntity.Loop.WAITING),
				new Shown(Status.FINISHED, false, StolasEntity.Loop.WAITING))) {
			helper.assertTrue(StolasEntity.loop(row.status(), row.listening()) == row.loop(),
					"while " + row.status().id + (row.listening() ? ", listening" : "") + ": " + row.loop());
		}
		for (String id : StolasEntity.SOUND_IDS) {
			helper.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(Jugcraft.id("stolas." + id)), "jugcraft:stolas." + id + " is registered");
		}
		for (StolasEntity.Gesture gesture : StolasEntity.Gesture.values()) {
			helper.assertTrue(StolasEntity.SOUND_IDS.contains(gesture.id), gesture.id + " has its sound");
		}
		helper.succeed();
	}

	/** D-06 (03:159, 08:145): each gesture is fired by the server's own action, once, with its sound: he alights as the invitation opens (his listening not cued over it), refuses a refused acceptance, assents to a sealed one, inspects an offering paid ahead (a second inspection in the same moment is not repeated), shows suspension and listening by his loop, and departs when the pact ends. */
	@GameTest(maxTicks = 20)
	public void stolasAnswersEachActionWithItsGesture(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = ready(level, TestPlayers.survivor(helper));
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu menu = open(helper, owner, perch);
		StolasEntity stolas = (StolasEntity) level.getEntity(perch.visitor());
		helper.assertTrue(stolas != null && stolas.cues().equals(List.of("arrive")) && stolas.listening(),
				"The invitation opens: he alights and listens (his loop; no second sound over his arrival): " + (stolas == null ? "none" : stolas.cues()));
		helper.assertTrue(answered(send(owner, menu, PactRequestPayload.ACCEPT, 0), Pacts.Outcome.CANNOT_AFFORD.id) && "refuse".equals(stolas.cues().getLast()),
				"Accepting without the compact: refused, and he refuses: " + stolas.cues());
		pocket(owner, 1, AstrolabeItem.CAPACITY);
		helper.assertTrue(answered(send(owner, menu, PactRequestPayload.ACCEPT, 0), PactMenu.SEALED) && stolas.equals(stolas(level, perch))
				&& "assent".equals(stolas.cues().getLast()), "Sealed: the visitor is the presence, and he assents: " + stolas.cues());
		give(owner, new ItemStack(Items.AMETHYST_SHARD, 3));
		send(owner, menu, PactRequestPayload.CHOOSE, 0);
		helper.assertTrue(answered(send(owner, menu, PactRequestPayload.DEPOSIT, offering(menu.lastSent(), "minecraft:amethyst_shard")), PactMenu.DEPOSITED)
				&& "inspect".equals(stolas.cues().getLast()), "An offering paid ahead: he inspects it: " + stolas.cues());
		int shown = stolas.cues().size();
		give(owner, new ItemStack(Items.AMETHYST_SHARD, 1));
		send(owner, menu, PactRequestPayload.CHOOSE, 0);
		helper.assertTrue(answered(send(owner, menu, PactRequestPayload.DEPOSIT, offering(menu.lastSent(), "minecraft:amethyst_shard")), PactMenu.DEPOSITED)
				&& stolas.cues().size() == shown, "A second offering the same moment: no second inspection (one of a kind every CUE_TICKS)");
		helper.assertTrue(answered(send(owner, menu, PactRequestPayload.SUSPEND, 0), "suspended") && stolas.cues().size() == shown
				&& stolas.think(level, 0L, level.getGameTime()) == Status.SUSPENDED && stolas.listening()
				&& StolasEntity.loop(stolas.status(), stolas.listening()) == StolasEntity.Loop.LISTEN, "Suspended: no gesture; the screen open, he listens");
		owner.closeContainer();
		stolas.think(level, 0L, level.getGameTime());
		helper.assertTrue(!stolas.listening() && StolasEntity.loop(stolas.status(), false) == StolasEntity.Loop.SUSPENDED, "Closed, his loop is the suspended pact's");
		Component said = PactKeeping.end(level, owner, perch.holder(), StolasEntity.COUNTERPART, level.getGameTime()).said();
		helper.assertTrue(key(said).equals(PACT_KEY + "departed") && stolas.departing() && "depart".equals(stolas.cues().getLast()),
				"Ended: told who departs, and he departs: " + stolas.cues());
		helper.succeed();
	}

	/** D-06 (D-S7, E4): Stolas discarded (as a server tool removes an entity) ends nothing: record, terms, escrow and clock stay, nothing departs; his holder away, nobody is called; back near, he returns under the same record, alighting, at no cost. */
	@GameTest(maxTicks = 20)
	public void aRemovedStolasNeverEndsThePact(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = ready(level, TestPlayers.survivor(helper));
		BlockPos at = new BlockPos(3, 1, 3);
		long g0 = level.getGameTime();
		PactPerchBlockEntity perch = stolasPact(helper, keeper, at, g0);
		UUID record = perch.holder().agreement().record();
		give(keeper, new ItemStack(Items.AMETHYST_SHARD, 2));
		PactKeeping.deposit(level, keeper, perch.holder(), Items.AMETHYST_SHARD, g0);
		BoundWill before = pactOf(level, perch);
		StolasEntity first = stolas(level, perch);
		first.discard();
		BoundWill kept = BoundWills.of(level.getServer()).ledger().get(record);
		helper.assertTrue(kept != null && kept.pact().upkeep().due() == before.pact().upkeep().due() && kept.pact().service().equals(before.pact().service())
				&& kept.pact().hold() == before.pact().hold() && escrowed(level, record) == 2 && PactHistory.of(level.getServer()).history(keeper.getUUID()).isEmpty()
				&& perch.holder().spiritLost(level), "Discarded, he ends nothing; he is lost");
		place(helper, keeper, at.getX() + 100.5, at.getY(), at.getZ() + 0.5);
		PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 20);
		helper.assertTrue(stolas(level, perch) == null, "His holder away, nobody is called");
		beside(helper, keeper, at);
		PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 40);
		StolasEntity second = stolas(level, perch);
		helper.assertTrue(second != null && second.getUUID().equals(pactOf(level, perch).pact().presence()) && second.cues().equals(List.of("arrive"))
				&& escrowed(level, record) == 2 && shards(keeper) == 0 && pactOf(level, perch).pact().upkeep().due() == before.pact().upkeep().due(),
				"Back near, he returns under the same record, alighting, at no cost; the clock untouched");
		helper.succeed();
	}

	/** D-06 (D-S7, E5): one presence a pact: looks, openings and a recall call no second while he serves or is merely unloaded; struck off he is called once; the old one loaded again finds another in his place and departs. */
	@GameTest(maxTicks = 20)
	public void onePresencePerPact(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = ready(level, TestPlayers.survivor(helper));
		BlockPos at = new BlockPos(3, 1, 3);
		long g0 = level.getGameTime();
		PactPerchBlockEntity perch = stolasPact(helper, keeper, at, g0);
		StolasEntity first = stolas(level, perch);
		for (int i = 1; i <= 3; i++) {
			PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 20L * i);
			RateGate.forget(keeper.getUUID());
			PactMenu.open(keeper, level, perch);
			keeper.closeContainer();
		}
		helper.assertTrue(level.getEntitiesOfClass(StolasEntity.class, helper.getBounds(), s -> !s.gone()).size() == 1 && first.equals(stolas(level, perch))
				&& perch.holder().recall(level, keeper, g0 + 100) == AgreementHolder.Recall.NOT_LOST, "Serving, he is the only one");
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		first.saveWithoutId(saved);
		first.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 120);
		helper.assertTrue(!perch.holder().spiritLost(level) && level.getEntitiesOfClass(StolasEntity.class, helper.getBounds(), s -> !s.gone()).isEmpty()
				&& perch.holder().recall(level, keeper, g0 + 120) == AgreementHolder.Recall.NOT_LOST, "Unloaded is not lost: no second is called");
		WorkerRoster.of(level.getServer()).remove(keeper.getUUID(), first.getUUID());
		PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 140);
		StolasEntity second = stolas(level, perch);
		helper.assertTrue(second != null && !second.getUUID().equals(first.getUUID()), "Struck off, he is lost and one is called back");
		StolasEntity back = Workers.STOLAS.create(level, EntitySpawnReason.LOAD);
		back.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
		helper.assertTrue(back.getUUID().equals(first.getUUID()) && back.think(level, 0L, g0 + 160) == Status.FINISHED && back.gone()
				&& "depart".equals(back.cues().getLast()), "The old one, loaded again, finds another in his place and departs");
		helper.assertTrue(second.equals(stolas(level, perch)) && WorkerRoster.of(level.getServer()).workers(keeper.getUUID()).size() == 1,
				"One presence, one roster entry, one pact");
		helper.succeed();
	}

	/** D-06 (D-S7, 02:151, 03:146, E4): killing Stolas is never the fastest route: only damage past invulnerability can; his death ends, returns and drops nothing; he is called back at no cost; only ending returns what was paid ahead, once. */
	@GameTest(maxTicks = 60)
	public void killingStolasIsNeverTheFastestRoute(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = ready(level, TestPlayers.survivor(helper));
		BlockPos at = new BlockPos(3, 1, 3);
		long g0 = level.getGameTime();
		PactPerchBlockEntity perch = stolasPact(helper, keeper, at, g0);
		UUID record = perch.holder().agreement().record();
		give(keeper, new ItemStack(Items.AMETHYST_SHARD, 2));
		PactKeeping.deposit(level, keeper, perch.holder(), Items.AMETHYST_SHARD, g0);
		holding(keeper, AstrolabeItem.CAPACITY);
		int resonance = resonance(keeper);
		StolasEntity first = stolas(level, perch);
		helper.assertTrue(!first.hurtServer(level, level.damageSources().playerAttack(keeper), Float.MAX_VALUE) && first.isAlive(), "A blow does nothing");
		helper.assertTrue(first.hurtServer(level, level.damageSources().fellOutOfWorld(), Float.MAX_VALUE) && first.isDeadOrDying(), "Only damage past invulnerability kills him");
		helper.runAfterDelay(25, () -> {
			AABB around = new AABB(helper.absolutePos(at)).inflate(4.0);
			helper.assertTrue(first.isRemoved() && BoundWills.of(level.getServer()).ledger().get(record) != null && escrowed(level, record) == 2
					&& shards(keeper) == 0 && resonance(keeper) == resonance && PactHistory.of(level.getServer()).history(keeper.getUUID()).isEmpty()
					&& looseNear(level, around, Items.AMETHYST_SHARD) == 0 && looseNear(level, around, Items.FEATHER) == 0
					&& level.getEntitiesOfClass(ExperienceOrb.class, around).isEmpty(), "Killed, he ends, returns and drops nothing");
			long now = level.getGameTime();
			PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), now);
			StolasEntity second = stolas(level, perch);
			helper.assertTrue(second != null && !second.getUUID().equals(first.getUUID()) && shards(keeper) == 0 && resonance(keeper) == resonance
					&& escrowed(level, record) == 2, "His holder near, he is called back at no cost");
			helper.assertTrue(PactKeeping.end(level, keeper, perch.holder(), StolasEntity.COUNTERPART, now).ended() == AgreementHolder.Ended.ENDED
					&& shards(keeper) == 2 && escrowed(level, record) == 0, "Only ending returns what was paid ahead");
			helper.assertTrue(PactKeeping.end(level, keeper, perch.holder(), StolasEntity.COUNTERPART, now).ended() != AgreementHolder.Ended.ENDED
					&& shards(keeper) == 2 && PactHistory.of(level.getServer()).history(keeper.getUUID()).size() == 1, "and only once");
			helper.succeed();
		});
	}

	/** D-06 (G11): Jade's readings say whether an agreement is kept, with whom, its service and state, the next offering or the grace, why its presence waits, a copy or a visit, for a perch, a Spirit Anchor and Stolas; no player named, no escrow, nothing changed. */
	@GameTest(maxTicks = 20)
	public void theJadeReadingsNameNoPlayerAndChangeNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = ready(level, TestPlayers.survivor(helper));
		ServerPlayer neighbour = ready(level, TestPlayers.survivor(helper));
		ServerPlayer guest = ready(level, TestPlayers.survivor(helper));
		long g0 = level.getGameTime();
		List<CompoundTag> readings = new ArrayList<>();
		PactPerchBlockEntity perch = stolasPact(helper, keeper, new BlockPos(2, 1, 2), g0);
		BoundWill sealed = pactOf(level, perch);
		CompoundTag kept = PactReading.holder(level, perch, g0);
		CompoundTag his = PactReading.stolas(level, stolas(level, perch), g0);
		readings.addAll(List.of(kept, his));
		helper.assertTrue("kept".equals(kept.getStringOr("standing", "")) && StolasEntity.COUNTERPART.equals(kept.getStringOr("counterpart", ""))
				&& StarReckoning.WORK.equals(kept.getStringOr("service", "")) && "active".equals(kept.getStringOr("state", ""))
				&& kept.getIntOr("due_days", -1) == 7 && !kept.contains("grace_days") && !kept.contains("waiting") && sealed.equals(pactOf(level, perch)),
				"Kept: Stolas, Star-reckoning, active, next offering in 7 game days; reading changed nothing: " + kept);
		helper.assertTrue("kept".equals(his.getStringOr("standing", "")) && his.getIntOr("due_days", -1) == 7 && !his.contains("counterpart"), "Stolas: " + his);
		long due = sealed.pact().upkeep().due();
		stay(level, perch.holder(), g0 + 1_000L, due - 1_000L);
		helper.assertTrue(perch.holder().look(level, due) == Pacts.Event.WARNED, "Nothing paid ahead: warned when due");
		CompoundTag warned = PactReading.holder(level, perch, due);
		readings.add(warned);
		helper.assertTrue("upkeep_due".equals(warned.getStringOr("state", "")) && warned.getIntOr("grace_days", -1) == 1 && !warned.contains("due_days"), "Warned: " + warned);
		stay(level, perch.holder(), due + 1_000L, due + Pacts.DAY - 1_000L);
		helper.assertTrue(perch.holder().look(level, due + Pacts.DAY) == Pacts.Event.SUSPENDED, "The grace over, held");
		CompoundTag held = PactReading.holder(level, perch, due + Pacts.DAY);
		readings.add(held);
		helper.assertTrue("upkeep_held".equals(held.getStringOr("state", "")) && !held.contains("due_days") && !held.contains("grace_days"), "Held: " + held);
		SpiritAnchorBlockEntity anchor = sealed(helper, keeper, new BlockPos(6, 1, 2), null, g0);
		CompoundTag gathering = PactReading.holder(level, anchor, g0);
		readings.add(gathering);
		helper.assertTrue("kept".equals(gathering.getStringOr("standing", "")) && Pacts.counterpart(SpiritAnchorBlockEntity.COUNTERPART).equals(gathering.getStringOr("counterpart", ""))
				&& "gather".equals(gathering.getStringOr("service", "")) && "active".equals(gathering.getStringOr("state", "")) && !gathering.contains("due_days"),
				"The anchor keeps a Gathering Shade's agreement, gathering, no upkeep: " + gathering);
		((WorkerEntity<?>) level.getEntity(anchor.spirit())).remove(Entity.RemovalReason.KILLED);
		CompoundTag lost = PactReading.holder(level, anchor, g0);
		readings.add(lost);
		helper.assertTrue(PactReading.LOST.equals(lost.getStringOr("waiting", "")), "Its shade killed, lost: " + lost);
		PactPerchBlockEntity theirs = stolasPact(helper, neighbour, new BlockPos(2, 1, 6), g0);
		place(helper, neighbour, 100.5, 1.0, 100.5);
		helper.assertTrue(stolas(level, theirs).think(level, 0L, g0 + 20) == Status.OWNER_OFFLINE, "Its holder away, he waits");
		CompoundTag away = PactReading.holder(level, theirs, g0 + 20);
		readings.add(away);
		helper.assertTrue(Status.OWNER_OFFLINE.id.equals(away.getStringOr("waiting", "")), "The perch says why: " + away);
		PactPerchBlockEntity copy = perch(helper, new BlockPos(4, 1, 4), keeper);
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), perch.saveWithoutMetadata(level.registryAccess())));
		CompoundTag copied = PactReading.holder(level, copy, g0);
		readings.add(copied);
		helper.assertTrue("copy".equals(copied.getStringOr("standing", "")) && copy.stolas(level) == null,
				"A copied perch reads as a copy, and the original's Stolas is never the copy's (critique 6): " + copied);
		BlockPos visited = new BlockPos(6, 1, 6);
		PactPerchBlockEntity fresh = perch(helper, visited, guest);
		beside(helper, guest, visited);
		open(helper, guest, fresh);
		CompoundTag visiting = PactReading.holder(level, fresh, g0);
		readings.add(visiting);
		helper.assertTrue("none".equals(visiting.getStringOr("standing", "")) && visiting.getBooleanOr("visiting", false), "Visiting: " + visiting);
		CompoundTag switchedOff = new CompoundTag();
		off(() -> switchedOff.merge(PactReading.holder(level, perch, g0)));
		helper.assertTrue("disabled".equals(switchedOff.getStringOr("standing", "")), "Switched off, it says so");
		for (CompoundTag reading : readings) {
			String text = reading.toString();
			helper.assertTrue(PactReading.KEYS.containsAll(reading.keySet()), "Only PactReading.KEYS: " + text);
			for (ServerPlayer player : List.of(keeper, neighbour, guest)) {
				helper.assertTrue(!text.contains(player.getUUID().toString()) && !text.contains(player.getName().getString()), "No player named: " + text);
			}
		}
		BoundWills.of(level.getServer()).release(perch.holder().agreement().record(), keeper.getUUID());
		CompoundTag released = PactReading.holder(level, perch, g0);
		helper.assertTrue("none".equals(released.getStringOr("standing", "")) && !released.getBooleanOr("visiting", false)
				&& perch.holder().agreement() != null,
				"Its record gone, the reading says none, no visit (its presence is not its visitor, critique 7), and forgets nothing: " + released);
		helper.assertTrue(perch.holder().verify(level, g0) == AgreementHolder.Standing.NONE && perch.holder().agreement() == null, "only verifying forgets");
		helper.succeed();
	}

	// ---------------------------------------------------------------- Concordance Chunk D, D-07: the roster

	/** {@code player}'s roster as the key opens it, its gate forgotten first: the state the server answered. */
	static RosterStatePayload roster(ServerPlayer player) {
		RateGate.forget(player.getUUID());
		RosterStatePayload state = Roster.receive(player, new RosterRequestPayload(Roster.lastSequence(player) + 1, RosterRequestPayload.OPEN,
				RosterRequestPayload.NO_ROW, 0));
		if (state == null) {
			throw new IllegalStateException("The roster did not open for " + player.getName().getString());
		}
		return state;
	}

	/** As the screen sends {@code action} on {@code row}: the next sequence and the revision it showed. */
	static @Nullable RosterStatePayload act(ServerPlayer player, RosterStatePayload.@Nullable Row row, int action) {
		RateGate.forget(player.getUUID());
		if (row == null) {
			throw new IllegalStateException("No such row on the roster");
		}
		return Roster.receive(player, new RosterRequestPayload(Roster.lastSequence(player) + 1, action, row.id(), row.revision()));
	}

	static RosterStatePayload.@Nullable Row rowOf(@Nullable RosterStatePayload state, UUID id) {
		return state == null ? null : state.rows().stream().filter(row -> row.id().equals(id)).findFirst().orElse(null);
	}

	static boolean answered(@Nullable RosterStatePayload state, String reason) {
		return state != null && state.result().isPresent() && state.result().get().reason().equals(reason);
	}

	static @Nullable Component result(@Nullable RosterStatePayload state) {
		return state == null ? null : state.result().map(PactStatePayload.Result::message).orElse(null);
	}

	/**
	 * D-07 (E1, E4, D-S11): the roster lists only its reader's own: their agreement, its shade in the same row, nothing of
	 * another's; suspended and resumed from it (a second suspend not offered, so nothing toggles back), ended from it: the
	 * escrow back once, the shade gone and off the roster, the anchor forgets it; the other's agreement, escrow and shade are
	 * untouched and still on their own roster.
	 */
	@GameTest(maxTicks = 20)
	public void theRosterEndsOnlyWhatTheReaderHolds(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer reader = binder(helper);
		ServerPlayer other = binder(helper);
		long now = level.getGameTime() + 4_400_000L;
		SpiritAnchorBlockEntity mine = sealed(helper, reader, new BlockPos(1, 1, 1), UPKEEP, now);
		SpiritAnchorBlockEntity theirs = sealed(helper, other, new BlockPos(5, 1, 1), UPKEEP, now);
		UUID record = mine.agreement().record();
		UUID otherRecord = theirs.agreement().record();
		UUID shade = mine.spirit();
		UUID otherShade = theirs.spirit();
		beside(helper, reader, new BlockPos(1, 1, 1));
		helper.assertValueEqual(mine.holder().deposit(level, reader, new ItemStack(Items.WOOL.white(), 2), now), 2, "The reader pays two ahead");
		helper.assertValueEqual(theirs.holder().deposit(level, other, new ItemStack(Items.WOOL.white(), 1), now), 1, "The other pays one ahead");
		RosterStatePayload state = roster(reader);
		List<UUID> ids = state.rows().stream().map(RosterStatePayload.Row::id).toList();
		helper.assertValueEqual(ids, List.of(record), "The reader's roster: their own agreement, its shade in the same row, nothing of the other's");
		RosterStatePayload.Row row = state.rows().getFirst();
		helper.assertTrue(!row.request() && "jugcraft:gathering_shade".equals(row.portrait())
				&& key(row.kind()).equals("message.jugcraft.concordance.workers.kind.spirit")
				&& key(row.name()).equals("entity.jugcraft.gathering_shade") && key(row.service()).equals("message.jugcraft.concordance.roster.service.gather")
				&& row.loaded() && row.icon() == RosterStatePayload.ACTIVE && row.offers(RosterRequestPayload.SUSPEND) && row.offers(RosterRequestPayload.END)
				&& !row.offers(RosterRequestPayload.RESUME) && !row.offers(RosterRequestPayload.CANCEL) && !row.offers(RosterRequestPayload.RECOVER),
				"Its row: a spirit gathering, loaded, active; suspend and end offered: " + row);
		BoundWills wills = BoundWills.of(level.getServer());
		RosterStatePayload suspended = act(reader, row, RosterRequestPayload.SUSPEND);
		helper.assertTrue(answered(suspended, "suspended") && wills.ledger().get(record).pact().hold() == BoundWill.Hold.HOLDER
				&& wills.ledger().get(otherRecord).pact().hold() == BoundWill.Hold.NONE, "Suspended from the roster; the other's is not");
		RosterStatePayload.Row held = rowOf(suspended, record);
		helper.assertTrue(held != null && held.offers(RosterRequestPayload.RESUME) && !held.offers(RosterRequestPayload.SUSPEND)
				&& held.icon() == RosterStatePayload.SUSPENDED, "Now it offers resume, not suspend");
		RosterStatePayload again = act(reader, held, RosterRequestPayload.SUSPEND);
		helper.assertTrue(answered(again, PactMenu.NOT_OFFERED) && wills.ledger().get(record).pact().hold() == BoundWill.Hold.HOLDER,
				"A second suspend is not offered: nothing toggles back");
		RosterStatePayload resumed = act(reader, rowOf(again, record), RosterRequestPayload.RESUME);
		helper.assertTrue(answered(resumed, "resumed") && wills.ledger().get(record).pact().hold() == BoundWill.Hold.NONE, "and resumed");
		RosterStatePayload ended = act(reader, rowOf(resumed, record), RosterRequestPayload.END);
		helper.assertTrue(answered(ended, "ended") && wool(reader) == 2 && wills.ledger().get(record) == null && wills.escrow(record).isEmpty(),
				"Ended from the roster: the escrow comes back once: " + key(result(ended)));
		Entity gone = level.getEntity(shade);
		helper.assertTrue((gone == null || gone.isRemoved()) && !WorkerRoster.of(level.getServer()).workers(reader.getUUID()).containsKey(shade)
				&& mine.agreement() == null && PactHistory.of(level.getServer()).history(reader.getUUID()).size() == 1 && ended.rows().isEmpty(),
				"Its shade departs, off the roster; the anchor forgets it; one departure kept; the roster is empty");
		Entity still = level.getEntity(otherShade);
		helper.assertTrue(wills.ledger().get(otherRecord) != null && escrowed(level, otherRecord) == 1 && wool(other) == 0 && still != null
				&& !still.isRemoved() && theirs.agreement() != null && PactHistory.of(level.getServer()).history(other.getUUID()).isEmpty()
				&& rowOf(roster(other), otherRecord) != null, "The other's agreement, escrow and shade are untouched, and on their own roster");
		helper.succeed();
	}

	/**
	 * D-07 (E1, 08:99): another player naming the holder's record or request, with its true revision, is answered not_listed;
	 * below the listing the authorities refuse as well (not_holder, not_yours); a party member's roster lists none of it and
	 * may not end it; a stand-in with the holder's identity is dropped and ends nothing; nothing of the holder's changes; the
	 * holder cancels their own request.
	 */
	@GameTest(maxTicks = 20)
	public void aRequestNamingAnotherPlayersRecordIsRefused(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		ServerPlayer other = binder(helper);
		long now = level.getGameTime() + 4_500_000L;
		SpiritAnchorBlockEntity anchor = sealed(helper, holder, new BlockPos(3, 1, 3), UPKEEP, now);
		UUID record = anchor.agreement().record();
		helper.assertValueEqual(anchor.holder().deposit(level, holder, new ItemStack(Items.WOOL.white(), 2), now), 2, "Two periods paid ahead");
		CourierLedger ledger = CourierLedger.of(level.getServer());
		Place post = Couriers.place(level, helper.absolutePos(new BlockPos(1, 1, 1)));
		Logistics.Filed filed = ledger.file(holder.getUUID(), post, post, new ItemStack(Items.STONE), 4, level.registryAccess(), now);
		helper.assertTrue(filed.outcome() == Logistics.Outcome.DONE, "The holder files a request");
		RosterStatePayload mine = roster(holder);
		RosterStatePayload.Row theirs = rowOf(mine, record);
		RosterStatePayload.Row theirRequest = rowOf(mine, Roster.rowOf(filed.id()));
		helper.assertTrue(theirs != null && theirRequest != null, "Both are on the holder's roster");
		helper.assertTrue(theirRequest.request() && "minecraft:stone".equals(theirRequest.portrait()) && !theirs.request(),
				"A request's row is drawn as its item; the record's is not a request");
		helper.assertTrue(roster(other).rows().isEmpty(), "Another player's roster lists nothing of the holder's");
		for (int action : new int[] {RosterRequestPayload.SUSPEND, RosterRequestPayload.END}) {
			helper.assertTrue(answered(act(other, theirs, action), Roster.NOT_LISTED), "Named by another, not listed: " + RosterRequestPayload.ACTIONS.get(action));
		}
		for (int action : new int[] {RosterRequestPayload.CANCEL, RosterRequestPayload.RECOVER}) {
			helper.assertTrue(answered(act(other, theirRequest, action), Roster.NOT_LISTED), "Named by another, not listed: " + RosterRequestPayload.ACTIONS.get(action));
		}
		helper.assertValueEqual(Roster.act(other, Roster.RECORD, record, RosterRequestPayload.END, now).reason(), "not_holder", "Ending another's record");
		helper.assertValueEqual(Roster.act(other, Roster.RECORD, record, RosterRequestPayload.SUSPEND, now).reason(), "not_holder", "suspending it");
		helper.assertValueEqual(Roster.act(other, Roster.REQUEST, Roster.rowOf(filed.id()), RosterRequestPayload.CANCEL, now).reason(), "not_yours",
				"cancelling another's request");
		helper.assertValueEqual(Roster.act(other, Roster.REQUEST, Roster.rowOf(filed.id()), RosterRequestPayload.RECOVER, now).reason(), "not_yours",
				"recovering it");
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(holder.getUUID(), other.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(other.getUUID(), ms) == PartyManager.Result.OK, "They form a party");
			helper.assertTrue(roster(other).rows().isEmpty()
					&& Roster.act(other, Roster.RECORD, record, RosterRequestPayload.END, now).reason().equals("not_holder"),
					"A party member's roster lists none of it, and they may not end it");
		} finally {
			parties.adminDisband(holder.getUUID());
		}
		FakePlayer standIn = FakePlayer.get(level, new GameProfile(holder.getUUID(), "[Concordance]"));
		helper.assertTrue(Roster.receive(standIn, new RosterRequestPayload(1, RosterRequestPayload.OPEN, RosterRequestPayload.NO_ROW, 0)) == null
				&& Roster.receive(standIn, new RosterRequestPayload(Roster.lastSequence(holder) + 1, RosterRequestPayload.END, record, theirs.revision())) == null
				&& Roster.act(standIn, Roster.RECORD, record, RosterRequestPayload.END, now).reason().equals(Roster.NOT_PERMITTED),
				"A stand-in with the holder's identity is dropped and ends nothing");
		BoundWills wills = BoundWills.of(level.getServer());
		helper.assertTrue(wills.ledger().get(record) != null && wills.ledger().get(record).pact().hold() == BoundWill.Hold.NONE && escrowed(level, record) == 2
				&& wool(other) == 0 && ledger.ledger().get(filed.id()) != null && ledger.ledger().get(filed.id()).state() == RequestState.OPEN,
				"Nothing of the holder's was ended, suspended, cancelled or handed over");
		helper.assertTrue(answered(act(holder, theirRequest, RosterRequestPayload.CANCEL), "cancelled") && ledger.ledger().get(filed.id()) == null,
				"Its own requester cancels it from the roster");
		helper.succeed();
	}

	/**
	 * D-07 (D-S11, critique 17): with the roster's 32 full, a pact whose perch was broken and a record from before Chunk D not yet
	 * claimed are still listed, first: without its place, or unclaimed; each offers only its end. Kept at the perch, the pact's
	 * row carries Stolas and offers suspend. A construct not loaded is drawn as its kind's creature.
	 */
	@GameTest(maxTicks = 20)
	public void theRosterListsAPactWithoutItsPerch(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = regarded(helper);
		long now = level.getGameTime() + 4_100_000L;
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = stolasPact(helper, holder, at, now);
		UUID record = perch.holder().agreement().record();
		StolasEntity stolas = stolas(level, perch);
		RosterStatePayload kept = roster(holder);
		RosterStatePayload.Row seated = rowOf(kept, record);
		helper.assertTrue(stolas != null && seated != null && rowOf(kept, stolas.getUUID()) == null
				&& key(seated.kind()).equals("message.jugcraft.concordance.workers.kind.pact") && key(seated.name()).equals("entity.jugcraft.stolas")
				&& "jugcraft:stolas".equals(seated.portrait())
				&& seated.loaded() && seated.offers(RosterRequestPayload.SUSPEND) && seated.offers(RosterRequestPayload.END),
				"Kept at the perch: one row, Stolas in it, suspend and end offered: " + seated);
		helper.setBlock(at, Blocks.AIR);
		helper.assertTrue(stolas.gone() && BoundWills.of(level.getServer()).ledger().get(record).pact().place() == null,
				"The perch broken, the pact is without it; Stolas leaves");
		WorkerRoster roster = WorkerRoster.of(level.getServer());
		String dimension = level.dimension().identifier().toString();
		while (roster.workers(holder.getUUID()).size() < WorkerRoster.MAX_PER_PLAYER) {
			roster.note(holder.getUUID(), UUID.randomUUID(), "construct", Status.IDLE, dimension, helper.absolutePos(at));
		}
		UUID legacy = UUID.randomUUID();
		helper.assertValueEqual(BoundWills.of(level.getServer()).seal(new BoundWill(legacy, GatheringShadeEntity.DEFINITION, "gathering_shade",
				holder.getUUID(), now + 1, false)).outcome(), BoundWillLedger.Outcome.DONE, "A record from before Chunk D, not yet claimed");
		RosterStatePayload state = roster(holder);
		helper.assertTrue(state.rows().size() == WorkerRoster.MAX_PER_PLAYER + 2 && state.more() == 0
				&& state.rows().get(0).id().equals(record) && state.rows().get(1).id().equals(legacy),
				"With the roster's 32 full, both records are listed, first: " + state.rows().size());
		RosterStatePayload.Row orphan = state.rows().get(0);
		RosterStatePayload.Row unclaimed = state.rows().get(1);
		helper.assertTrue(orphan.icon() == RosterStatePayload.WITHOUT_PLACE && key(orphan.place()).equals("message.jugcraft.concordance.roster.place.none")
				&& key(orphan.waiting()).equals("message.jugcraft.concordance.roster.waiting.without_place") && !orphan.loaded()
				&& orphan.offers(RosterRequestPayload.END) && !orphan.offers(RosterRequestPayload.SUSPEND) && !orphan.offers(RosterRequestPayload.RESUME),
				"The pact without its perch: without its place, only its end offered: " + orphan);
		helper.assertTrue(unclaimed.icon() == RosterStatePayload.UNCLAIMED && "jugcraft:gathering_shade".equals(unclaimed.portrait())
				&& key(unclaimed.name()).equals("entity.jugcraft.gathering_shade")
				&& unclaimed.offers(RosterRequestPayload.END) && !unclaimed.offers(RosterRequestPayload.SUSPEND),
				"The unclaimed record: only its end offered: " + unclaimed);
		RosterStatePayload.Row construct = state.rows().get(2);
		helper.assertTrue(!construct.loaded() && "jugcraft:clockwork_porter".equals(construct.portrait()) && construct.actions() == 0,
				"A construct not loaded is drawn as its kind's creature and offers nothing: " + construct);
		helper.succeed();
	}

	/**
	 * D-07 (E4, D-S6, D-S16): a pact without its perch, ended from the roster, gives its escrow back once to its living
	 * reader: switched off it is listed but nothing is offered or ended, and the screen's end is answered disabled; a reader
	 * who is not alive ends nothing and is handed nothing; alive, it ends, the two offerings come back once and the departure
	 * is kept with no place; ended again (an old row, or by its id) it finds nothing; the journal's pacts section names the
	 * departure.
	 */
	@GameTest(maxTicks = 20)
	public void endingAnOrphanReturnsItsEscrowOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = regarded(helper);
		long now = level.getGameTime() + 4_200_000L;
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = stolasPact(helper, holder, at, now);
		UUID record = perch.holder().agreement().record();
		helper.assertValueEqual(perch.holder().deposit(level, holder, new ItemStack(Items.AMETHYST_SHARD, 2), now), 2, "Two offerings paid ahead");
		helper.setBlock(at, Blocks.AIR);
		BoundWills wills = BoundWills.of(level.getServer());
		helper.assertTrue(wills.ledger().get(record).pact().place() == null && escrowed(level, record) == 2, "Without its perch, the escrow kept");
		RosterStatePayload.Row orphan = rowOf(roster(holder), record);
		helper.assertTrue(orphan != null && key(orphan.confirm()).equals("message.jugcraft.concordance.roster.confirm.end")
				&& Integer.valueOf(2).equals(arg(orphan.confirm(), 1)), "Its confirmation says two offerings come back");
		off(() -> {
			RosterStatePayload closed = roster(holder);
			RosterStatePayload.Row offRow = rowOf(closed, record);
			helper.assertTrue(!closed.enabled() && offRow != null && offRow.actions() == 0 && offRow.icon() == RosterStatePayload.DISABLED
					&& answered(act(holder, offRow, RosterRequestPayload.END), PactMenu.DISABLED)
					&& Roster.act(holder, Roster.RECORD, record, RosterRequestPayload.END, level.getGameTime()).reason().equals(PactMenu.DISABLED)
					&& BoundWills.of(level.getServer()).ledger().get(record) != null && escrowed(level, record) == 2,
					"Switched off, it is listed, nothing is offered, the screen's end is answered disabled and nothing ends (D-S16, D-P110)");
		});
		holder.setHealth(0.0F);
		RosterStatePayload refused = act(holder, orphan, RosterRequestPayload.END);
		helper.assertTrue(answered(refused, Roster.NOT_PERMITTED) && wills.ledger().get(record) != null && escrowed(level, record) == 2
				&& holder.getInventory().countItem(Items.AMETHYST_SHARD) == 0, "A reader who is not alive ends nothing and is handed nothing");
		holder.setHealth(holder.getMaxHealth());
		RosterStatePayload ended = act(holder, rowOf(refused, record), RosterRequestPayload.END);
		helper.assertTrue(answered(ended, "ended") && key(result(ended)).equals("message.jugcraft.concordance.pact.departed")
				&& Integer.valueOf(2).equals(arg(result(ended), 2)), "Ended: Stolas's departure, two offerings back: " + result(ended));
		helper.assertTrue(wills.ledger().get(record) == null && wills.escrow(record).isEmpty() && holder.getInventory().countItem(Items.AMETHYST_SHARD) == 2
				&& rowOf(ended, record) == null, "The record and its escrow go; the two come back once; it is off the roster");
		List<Pacts.Departure> history = PactHistory.of(level.getServer()).history(holder.getUUID());
		helper.assertTrue(history.size() == 1 && history.getFirst().record().equals(record) && Pacts.ENDED.equals(history.getFirst().reason())
				&& history.getFirst().place() == null, "One departure kept, ended, with no place: " + history);
		helper.assertTrue(answered(act(holder, orphan, RosterRequestPayload.END), Roster.NOT_LISTED)
				&& Roster.act(holder, Roster.RECORD, record, RosterRequestPayload.END, now).reason().equals("nothing")
				&& holder.getInventory().countItem(Items.AMETHYST_SHARD) == 2 && PactHistory.of(level.getServer()).history(holder.getUUID()).size() == 1
				&& wills.problems().isEmpty(), "Ended again it finds nothing, and nothing more comes back");
		JournalSection pacts = Journal.build(holder).stream().filter(section -> section.id().equals("pacts")).findFirst().orElse(null);
		helper.assertTrue(pacts != null && !pacts.lines().isEmpty() && key(pacts.lines().getLast().text()).equals("journal.jugcraft.pacts.departed")
				&& arg(pacts.lines().getLast().text(), 0) instanceof Component who && key(who).equals("entity.jugcraft.stolas")
				&& arg(pacts.lines().getLast().text(), 2) instanceof Component why && key(why).equals("message.jugcraft.concordance.workers.departed.ended")
				&& pacts.lines().stream().noneMatch(line -> key(line.text()).equals("journal.jugcraft.pacts.kept")),
				"The journal's pacts section names the departure and keeps no pact");
		helper.succeed();
	}

	/**
	 * D-07 (08:99): an end on a row read before its escrow changed is answered changed, nothing ended or handed back, with the
	 * row again at its new revision; a request not newer than the last is dropped; read afresh, it ends and both come back once.
	 */
	@GameTest(maxTicks = 20)
	public void aStaleRosterRevisionIsRefused(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = binder(helper);
		long now = level.getGameTime() + 4_300_000L;
		SpiritAnchorBlockEntity anchor = sealed(helper, holder, new BlockPos(3, 1, 3), UPKEEP, now);
		UUID record = anchor.agreement().record();
		BoundWills wills = BoundWills.of(level.getServer());
		helper.assertValueEqual(anchor.holder().deposit(level, holder, new ItemStack(Items.WOOL.white(), 1), now), 1, "One period paid ahead");
		RosterStatePayload.Row seen = rowOf(roster(holder), record);
		helper.assertTrue(seen != null && Integer.valueOf(1).equals(arg(seen.confirm(), 1)), "Read: one offering would come back");
		helper.assertValueEqual(anchor.holder().deposit(level, holder, new ItemStack(Items.WOOL.white(), 1), now), 1, "Then a second is paid ahead");
		RosterStatePayload stale = act(holder, seen, RosterRequestPayload.END);
		helper.assertTrue(answered(stale, PactMenu.CHANGED) && wills.ledger().get(record) != null && escrowed(level, record) == 2 && wool(holder) == 0,
				"An end on what was read before is refused: nothing ended, nothing handed back");
		RosterStatePayload.Row fresh = rowOf(stale, record);
		helper.assertTrue(fresh != null && fresh.revision() != seen.revision() && Integer.valueOf(2).equals(arg(fresh.confirm(), 1)),
				"The answer reads it again: two would come back");
		RateGate.forget(holder.getUUID());
		helper.assertTrue(Roster.receive(holder, new RosterRequestPayload(Roster.lastSequence(holder), RosterRequestPayload.END, record, fresh.revision())) == null
				&& wills.ledger().get(record) != null, "A request not newer than the last is dropped");
		RosterStatePayload ended = act(holder, fresh, RosterRequestPayload.END);
		helper.assertTrue(answered(ended, "ended") && wool(holder) == 2 && wills.ledger().get(record) == null, "Read afresh, it ends; both come back, once");
		helper.succeed();
	}

	/**
	 * D-07 (critique 17): 32 Bound Wills, 16 courier requests and 32 workers are 80: the roster sends 64, records first in the
	 * order sealed, then requests oldest first, then workers, and counts the 16 left out; a request is drawn as its item and a
	 * construct not loaded as its kind's creature; the 64 cross whole; no payload holds a 65th.
	 */
	@GameTest(maxTicks = 20)
	public void theRosterStopsAt64Rows(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer reader = binder(helper);
		UUID me = reader.getUUID();
		long now = level.getGameTime() + 4_600_000L;
		BoundWills wills = BoundWills.of(level.getServer());
		List<UUID> records = new ArrayList<>();
		for (int i = 0; i < BoundWillLedger.MAX_PER_HOLDER; i++) {
			BoundWill.Pact pact = new BoundWill.Pact(null, BoundWill.Hold.NONE, now, Pacts.PERSONAL, null, null);
			BoundWill record = new BoundWill(UUID.randomUUID(), GatheringShadeEntity.DEFINITION, GatheringShadeEntity.DEFINITION, me, now + i, false, pact);
			helper.assertTrue(wills.seal(record).outcome() == BoundWillLedger.Outcome.DONE, "record " + i);
			records.add(record.id());
		}
		CourierLedger ledger = CourierLedger.of(level.getServer());
		Place post = Couriers.place(level, helper.absolutePos(new BlockPos(1, 1, 1)));
		List<Long> requests = new ArrayList<>();
		for (int i = 0; i < Logistics.MAX_OPEN_PER_PLAYER; i++) {
			Logistics.Filed filed = ledger.file(me, post, post, new ItemStack(Items.STONE), 1, level.registryAccess(), now);
			helper.assertTrue(filed.outcome() == Logistics.Outcome.DONE, "request " + i + ": " + filed);
			requests.add(filed.id());
		}
		WorkerRoster roster = WorkerRoster.of(level.getServer());
		String dimension = level.dimension().identifier().toString();
		for (int i = 0; i < WorkerRoster.MAX_PER_PLAYER; i++) {
			roster.note(me, UUID.randomUUID(), "construct", Status.IDLE, dimension, helper.absolutePos(new BlockPos(1, 1, 1)));
		}
		try {
			RosterStatePayload state = roster(reader);
			int all = BoundWillLedger.MAX_PER_HOLDER + Logistics.MAX_OPEN_PER_PLAYER + WorkerRoster.MAX_PER_PLAYER;
			helper.assertTrue(state.rows().size() == RosterStatePayload.MAX_ROWS && state.more() == all - RosterStatePayload.MAX_ROWS,
					"At most 64 rows, the rest counted: " + state.rows().size() + ", " + state.more());
			List<UUID> ids = state.rows().stream().map(RosterStatePayload.Row::id).toList();
			int r = records.size();
			int q = requests.size();
			helper.assertValueEqual(ids.subList(0, r), records, "Every record first, in the order sealed");
			helper.assertValueEqual(ids.subList(r, r + q), requests.stream().map(Roster::rowOf).toList(), "then every request, oldest first");
			List<UUID> workers = new ArrayList<>(roster.workers(me).keySet());
			helper.assertValueEqual(ids.subList(r + q, RosterStatePayload.MAX_ROWS), workers.subList(0, RosterStatePayload.MAX_ROWS - r - q),
					"then the roster's workers as far as room allows");
			helper.assertTrue(state.rows().get(r).request() && "minecraft:stone".equals(state.rows().get(r).portrait())
					&& !state.rows().get(r + q).request() && "jugcraft:clockwork_porter".equals(state.rows().get(r + q).portrait()),
					"A request is drawn as its item, a construct not loaded as its kind's creature");
			RosterStatePayload.Row first = state.rows().getFirst();
			helper.assertTrue(first.offers(RosterRequestPayload.END) && !first.offers(RosterRequestPayload.SUSPEND) && first.icon() == RosterStatePayload.WITHOUT_PLACE,
					"A record without its place offers only its end");
			RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
			RosterStatePayload.CODEC.encode(buffer, state);
			RosterStatePayload received = RosterStatePayload.CODEC.decode(buffer);
			helper.assertTrue(received.rows().size() == RosterStatePayload.MAX_ROWS && received.more() == state.more()
					&& received.rows().getFirst().id().equals(first.id()) && received.rows().getFirst().revision() == first.revision()
					&& received.rows().getFirst().actions() == first.actions() && received.rows().getFirst().portrait().equals(first.portrait())
					&& received.rows().getFirst().icon() == first.icon(), "The 64 rows cross whole");
			List<RosterStatePayload.Row> many = new ArrayList<>(state.rows());
			many.add(first);
			helper.assertValueEqual(new RosterStatePayload(0, true, many, 0, Optional.empty()).rows().size(), RosterStatePayload.MAX_ROWS,
					"No payload holds a 65th row");
		} finally {
			requests.forEach(id -> ledger.cancel(id, me, now));
		}
		helper.succeed();
	}

	/**
	 * D-07, closing D-08's As built (its last item; unit 1's review fix 2, for the hand): a workshop hand unloaded and
	 * replaced, loading again while the Concordance is off, takes no roster place by a change of status; on again, it finds
	 * another hand in its place and leaves, and the new hand keeps the agreement and its roster place.
	 */
	@GameTest(maxTicks = 20)
	public void aReplacedHandLoadingWhileSwitchedOffStaysOffTheRoster(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		WorkshopHands.Workshop shop = WorkshopHands.workshop(helper);
		ServerPlayer holder = shop.holder();
		UUID id = holder.getUUID();
		WorkshopHandEntity old = shop.hand();
		UUID first = old.getUUID();
		WorkerRoster roster = WorkerRoster.of(level.getServer());
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		old.saveWithoutId(saved);
		old.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		helper.assertTrue(roster.workers(id).containsKey(first) && !shop.anchor().holder().spiritLost(level),
				"Unloaded is not lost: the hand keeps its roster place");
		roster.remove(id, first); // as one sealed before its roster note, or while the roster was full
		long now = WorkshopHands.morning(level, 3_500_000L);
		helper.assertValueEqual(shop.anchor().holder().recall(level, holder, now), AgreementHolder.Recall.RECALLED,
				"Missing from the roster it is lost, and its holder calls another hand");
		UUID second = shop.anchor().spirit();
		WorkshopHandEntity back = Workers.WORKSHOP_HAND.create(level, EntitySpawnReason.LOAD);
		back.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
		helper.assertTrue(back.getUUID().equals(first) && second != null && !first.equals(second), "The old hand loads again, a new one in its place");
		long in = WorkshopHands.inHours();
		off(() -> helper.assertTrue(back.think(level, in, now + 20) == Status.DISABLED && !roster.workers(id).containsKey(first),
				"Switched off, it waits and takes no roster place (joinsRosterOnStatus false, as the shade's and Stolas's)"));
		helper.assertTrue(back.think(level, in, now + 40) == Status.FINISHED && back.isRemoved() && !roster.workers(id).containsKey(first),
				"On again, it finds another hand in its place and leaves, off the roster");
		helper.assertTrue(second.equals(shop.anchor().spirit()) && roster.workers(id).containsKey(second)
				&& BoundWills.of(level.getServer()).ledger().heldBy(id).size() == 1, "The new hand keeps the agreement and its roster place");
		helper.succeed();
	}

	// ---------------------------------------------------------------- Concordance garden cohort, GC-01: one perch, many spirits
	// Package-private, as Chunk D's: the cohort's later tests (GC-04's Buer, its chooser) use them; no new helper file, since a
	// shared helper file would select every client test class (tools/select_client_tests.py).

	/** The pact spirits of {@code d}'s kind at the test (alive, not leaving). */
	static List<PactSpiritEntity<?>> spiritsOf(GameTestHelper helper, PactDefinition d) {
		List<PactSpiritEntity<?>> out = new ArrayList<>();
		for (Entity found : helper.getLevel().getEntitiesOfClass(Entity.class, helper.getBounds(),
				entity -> entity instanceof PactSpiritEntity<?> spirit && spirit.isAlive() && !spirit.gone())) {
			if (found instanceof PactSpiritEntity<?> spirit && spirit.counterpart().equals(d.id())) {
				out.add(spirit);
			}
		}
		return out;
	}

	/** The pact spirit kept at {@code perch} now (any spirit; not leaving), or null. */
	static @Nullable PactSpiritEntity<?> presenceAt(ServerLevel level, PactPerchBlockEntity perch) {
		UUID id = perch.holder().spirit();
		return id != null && level.getEntity(id) instanceof PactSpiritEntity<?> found && !found.gone() ? found : null;
	}

	/** INVITE at {@code choice}, as the chooser's entry sends it. */
	static @Nullable PactStatePayload invite(ServerPlayer player, PactMenu menu, int choice) {
		return send(player, menu, PactRequestPayload.INVITE, choice);
	}

	/** The note keyed {@code key} (PACT_KEY + key) among {@code state}'s, or null. */
	static @Nullable Component note(PactStatePayload state, String key) {
		return state.notes().stream().filter(line -> key(line).equals(PACT_KEY + key)).findFirst().orElse(null);
	}

	/** {@code line} is shown, and the server renders it as {@code expected} (the lang file's words, with the spirit's name). */
	private static void rendersAs(GameTestHelper helper, @Nullable Component line, String expected, String what) {
		helper.assertTrue(line != null, what + " is shown");
		helper.assertValueEqual(line.getString(), expected, what);
	}

	/** Whether {@code line}, as the server renders it, names neither an astrolabe nor resonance (GB23). */
	private static boolean namesNoAstrolabe(@Nullable Component line) {
		String words = line == null ? "" : line.getString().toLowerCase(Locale.ROOT);
		return line != null && !words.contains("astrolabe") && !words.contains("resonance");
	}

	/** INVITE on a page that is not the chooser: not offered, nothing done; the mode, the reader's pacts and the spirits unchanged; past the screen's bound it is dropped. */
	private static void notInvited(GameTestHelper helper, ServerPlayer player, PactMenu menu, int mode) {
		ServerLevel level = helper.getLevel();
		helper.assertValueEqual(menu.lastSent().mode(), mode, "the page before INVITE");
		int held = BoundWills.of(level.getServer()).ledger().heldBy(player.getUUID()).size();
		int spirits = stolases(helper).size();
		PactStatePayload sent = invite(player, menu, 0);
		helper.assertTrue(answered(sent, PactMenu.NOT_OFFERED) && !sent.result().get().done() && sent.mode() == mode
				&& BoundWills.of(level.getServer()).ledger().heldBy(player.getUUID()).size() == held && stolases(helper).size() == spirits,
				"INVITE outside the chooser (mode " + mode + ") is not offered, and nothing changes: " + sent);
		helper.assertTrue(request(player, menu.containerId, menu.lastSequence() + 1, PactRequestPayload.INVITE, PactStatePayload.MAX_CHOICES,
				menu.lastSent().revision()) == null, "past the screen's bound it is dropped before anything");
	}

	/** GC-01 (SG4, GC-P10): one spirit its owner may invite: no chooser, Stolas's offer at once, the state naming him; an INVITE off the chooser is not offered and summons nothing. */
	@GameTest(maxTicks = 20)
	public void theChooserIsSkippedWithOneSpirit(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = ready(level, TestPlayers.survivor(helper));
		pocket(owner, 1, AstrolabeItem.CAPACITY);
		BlockPos at = new BlockPos(3, 1, 3);
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu menu = open(helper, owner, perch);
		PactStatePayload state = menu.lastSent();
		helper.assertTrue(state.mode() == PactStatePayload.NEGOTIATE && state.revision() == 1 && state.patterns().isEmpty() && state.notes().isEmpty(),
				"One spirit they may invite: no chooser, his offer at once: " + state);
		helper.assertTrue(state.spirit().equals(new PactStatePayload.Spirit(StolasEntity.SIGN, StarReckoning.WORK))
				&& key(state.speaker()).equals("entity.jugcraft.stolas"), "The state names its spirit, his sign and his work: " + state.spirit());
		helper.assertTrue(stolases(helper).size() == 1 && perch.visitor() != null, "Stolas comes");
		PactStatePayload sent = invite(owner, menu, 0);
		helper.assertTrue(answered(sent, PactMenu.NOT_OFFERED) && sent.mode() == PactStatePayload.NEGOTIATE && stolases(helper).size() == 1
				&& sent.revision() == 1, "An INVITE off the chooser is not offered; nothing is summoned and the offer stands");
		helper.succeed();
	}

	/** GC-01 (SG4): on a negotiation, a kept pact, a closed page and the seat page, INVITE is not offered and changes nothing; past the screen's bound it is dropped. */
	@GameTest(maxTicks = 20)
	public void anInviteOutsideTheInviteModeIsDropped(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = ready(level, TestPlayers.survivor(helper));
		pocket(owner, 1, AstrolabeItem.CAPACITY);
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu menu = open(helper, owner, perch);
		notInvited(helper, owner, menu, PactStatePayload.NEGOTIATE);
		helper.assertTrue(answered(send(owner, menu, PactRequestPayload.ACCEPT, 0), PactMenu.SEALED), "Its owner seals Stolas's pact");
		notInvited(helper, owner, menu, PactStatePayload.KEEP);
		ServerPlayer other = ready(level, TestPlayers.survivor(helper));
		beside(helper, other, at);
		PactMenu taken = open(helper, other, perch);
		helper.assertTrue(keys(taken.lastSent().notes()).contains(PACT_KEY + "reading.taken"), "Another reader at the kept perch: taken");
		notInvited(helper, other, taken, PactStatePayload.CLOSED);
		ServerPlayer third = regarded(helper);
		BlockPos broken = new BlockPos(6, 1, 2);
		stolasPact(helper, third, broken, level.getGameTime());
		helper.setBlock(broken, Blocks.AIR);
		BlockPos fresh = new BlockPos(6, 1, 6);
		PactPerchBlockEntity empty = perch(helper, fresh, third);
		beside(helper, third, fresh);
		notInvited(helper, third, open(helper, third, empty), PactStatePayload.SEAT);
		helper.succeed();
	}

	/** GC-01 (SG1, SG6): a pact whose work this version does not know is kept, listed, suspended and ended as before: its page says so, it offers no service action and its presence takes no step. */
	@GameTest(maxTicks = 20)
	public void aRecordWithAnUnknownWorkStillEnds(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = regarded(helper);
		BlockPos at = new BlockPos(3, 1, 3);
		long g0 = level.getGameTime();
		PactPerchBlockEntity perch = perch(helper, at, keeper);
		beside(helper, keeper, at);
		helper.assertValueEqual(perch.holder().seal(level, keeper, withService(stolasTerms(), new BoundWill.Service("unknown_work", 8, 0, 24000, 1)), g0)
				.outcome(), Pacts.Outcome.DONE, "A pact whose work this version does not know is sealed (as a newer save would hold one)");
		give(keeper, new ItemStack(Items.AMETHYST_SHARD, 2));
		helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), Items.AMETHYST_SHARD, g0), 2, "Two paid ahead");
		PactMenu menu = open(helper, keeper, perch);
		PactStatePayload state = menu.lastSent();
		List<String> notes = keys(state.notes());
		helper.assertTrue(state.mode() == PactStatePayload.KEEP && notes.contains(PACT_KEY + "row.service_unknown") && !notes.contains(PACT_KEY + "row.service")
				&& !notes.contains(PACT_KEY + "row.obligation") && !notes.contains(PACT_KEY + "row.obligation_kept"),
				"Kept, its page says its service is not known here, and names no service or obligation of its own: " + notes);
		helper.assertTrue(state.patterns().isEmpty() && !state.offers(PactRequestPayload.RECALL) && state.spirit().work().equals("unknown_work"),
				"It offers no service action, and the state names its work: " + state.spirit());
		helper.assertTrue(answered(send(keeper, menu, PactRequestPayload.RECALL, 0), PactMenu.NOT_OFFERED), "RECALL is not offered");
		helper.assertValueEqual(stolas(level, perch).think(level, 0L, g0 + 20), Status.IDLE, "Its presence has no service to step: it idles");
		PactStatePayload suspended = send(keeper, menu, PactRequestPayload.SUSPEND, 0);
		helper.assertTrue(answered(suspended, "suspended") && keys(suspended.notes()).getFirst().equals(PACT_KEY + "row.state.suspended.serves"),
				"Suspended by its holder, its page says so with its spirit's name: " + keys(suspended.notes()));
		helper.assertTrue(answered(send(keeper, menu, PactRequestPayload.RESUME, 0), "resumed"), "and resumed");
		PactStatePayload ended = send(keeper, menu, PactRequestPayload.END, 0);
		helper.assertTrue(answered(ended, "ended") && ended.result().get().done() && shards(keeper) == 2
				&& PactHistory.of(level.getServer()).history(keeper.getUUID()).size() == 1, "Ended: what was paid ahead comes back once, the departure kept");
		PactStatePayload again = send(keeper, menu, PactRequestPayload.END, 0);
		helper.assertTrue(again != null && again.result().isPresent() && !again.result().get().done() && shards(keeper) == 2
				&& PactHistory.of(level.getServer()).history(keeper.getUUID()).size() == 1, "Ending again returns nothing more");
		helper.succeed();
	}

	/**
	 * GC-01 (GB4): one pact without its perch: the seat page shows it, lists nothing, names its spirit; an END past the one-entry
	 * list is not offered; SEAT seats it at the new perch, its spirit called there. GC-04's half, with a second spirit: two
	 * pacts without their perch are both listed by sign and name, the first's terms shown; CHOOSE (the state alone) shows the
	 * other's; SEAT seats the one chosen; another empty perch then shows the one left alone, and END ends it with its escrow once.
	 */
	@GameTest(maxTicks = 20)
	public void aStrandedPactOfAnySpiritIsSeatedOrEnded(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = regarded(helper);
		BlockPos first = new BlockPos(2, 1, 2);
		long g0 = level.getGameTime();
		PactPerchBlockEntity was = stolasPact(helper, keeper, first, g0);
		UUID record = was.holder().agreement().record();
		helper.setBlock(first, Blocks.AIR);
		BlockPos second = new BlockPos(6, 1, 2);
		PactPerchBlockEntity perch = perch(helper, second, keeper);
		beside(helper, keeper, second);
		PactMenu menu = open(helper, keeper, perch);
		PactStatePayload state = menu.lastSent();
		helper.assertTrue(state.mode() == PactStatePayload.SEAT && state.patterns().isEmpty() && keys(state.notes()).getFirst().equals(PACT_KEY + "seat.offer")
				&& state.spirit().sign().equals(StolasEntity.SIGN), "One pact without its perch: the seat page, no list, its spirit named: " + state);
		helper.assertTrue(answered(send(keeper, menu, PactRequestPayload.END, 1), PactMenu.NOT_OFFERED)
				&& BoundWills.of(level.getServer()).ledger().get(record) != null, "An END past the one-entry list is not offered; the pact is still held");
		PactStatePayload seated = send(keeper, menu, PactRequestPayload.SEAT, 0);
		helper.assertTrue(answered(seated, AgreementHolder.Seat.SEATED.id) && seated.mode() == PactStatePayload.KEEP
				&& Couriers.place(level, helper.absolutePos(second)).equals(BoundWills.of(level.getServer()).ledger().get(record).pact().place())
				&& stolas(level, perch) != null, "SEAT seats it here, and Stolas is called to the new perch");
		keeper.closeContainer();
		// GC-04's half: two pacts without their perch, Stolas's (an offering paid ahead) and Buer's.
		ServerPlayer both = regarded(helper);
		long g1 = level.getGameTime();
		BlockPos stolasAt = new BlockPos(1, 1, 5);
		PactPerchBlockEntity stolasPerch = stolasPact(helper, both, stolasAt, g1);
		UUID stolasRecord = stolasPerch.holder().agreement().record();
		give(both, new ItemStack(Items.AMETHYST_SHARD, 1));
		helper.assertValueEqual(PactKeeping.deposit(level, both, stolasPerch.holder(), Items.AMETHYST_SHARD, g1), 1, "One offering paid ahead into Stolas's pact");
		BlockPos buerAt = new BlockPos(3, 1, 5);
		buerPact(helper, both, buerAt, g1);
		helper.setBlock(stolasAt, Blocks.AIR);
		helper.setBlock(buerAt, Blocks.AIR);
		BlockPos third = new BlockPos(5, 1, 5);
		PactPerchBlockEntity seat = perch(helper, third, both);
		beside(helper, both, third);
		PactMenu page = open(helper, both, seat);
		PactStatePayload listed = page.lastSent();
		helper.assertTrue(listed.mode() == PactStatePayload.SEAT
				&& listed.patterns().stream().map(PactStatePayload.Choice::id).toList().equals(List.of(StolasEntity.SIGN, BuerEntity.SIGN))
				&& keys(listed.patterns().stream().map(PactStatePayload.Choice::name).toList()).equals(List.of("entity.jugcraft.stolas", "entity.jugcraft.buer"))
				&& key(term(listed, "service")).equals(PACT_KEY + "term.service.star_reckoning") && listed.spirit().sign().equals(StolasEntity.SIGN),
				"Two pacts without their perch: both listed by sign and name, the first's terms shown: " + listed);
		PactStatePayload focused = send(both, page, PactRequestPayload.CHOOSE, 1);
		helper.assertTrue(focused != null && focused.result().isEmpty() && focused.mode() == PactStatePayload.SEAT
				&& key(term(focused, "service")).equals(PACT_KEY + "term.service.strain_tending") && focused.spirit().sign().equals(BuerEntity.SIGN),
				"CHOOSE(1) answers with the state alone, Buer's terms shown: " + focused);
		PactStatePayload seatedBuer = send(both, page, PactRequestPayload.SEAT, 1);
		helper.assertTrue(answered(seatedBuer, AgreementHolder.Seat.SEATED.id) && seatedBuer.mode() == PactStatePayload.KEEP
				&& presenceAt(level, seat) instanceof BuerEntity && BuerEntity.COUNTERPART.equals(Pacts.counterpart(pactOf(level, seat).counterpart())),
				"SEAT(1) seats Buer's pact here, Buer called to it");
		both.closeContainer();
		BlockPos fourth = new BlockPos(7, 1, 5);
		PactPerchBlockEntity last = perch(helper, fourth, both);
		beside(helper, both, fourth);
		PactMenu alone = open(helper, both, last);
		helper.assertTrue(alone.lastSent().mode() == PactStatePayload.SEAT && alone.lastSent().patterns().isEmpty()
				&& alone.lastSent().spirit().sign().equals(StolasEntity.SIGN), "Another empty perch shows Stolas's pact alone, listing nothing");
		PactStatePayload ended = send(both, alone, PactRequestPayload.END, 0);
		helper.assertTrue(answered(ended, "ended") && ended.result().get().done() && shards(both) == 1
				&& BoundWills.of(level.getServer()).ledger().get(stolasRecord) == null && PactHistory.of(level.getServer()).history(both.getUUID()).size() == 1,
				"END(0) ends Stolas's pact there, its offering back once");
		PactStatePayload again = send(both, alone, PactRequestPayload.END, 0);
		helper.assertTrue(again != null && !again.result().map(PactStatePayload.Result::done).orElse(false) && shards(both) == 1,
				"Ending again returns nothing more");
		both.closeContainer();
		helper.succeed();
	}

	/** GC-01 (SG10, GC-P12): the shared lines that named Stolas now take a spirit's name, and the server renders each, with his, word for word as Chunk D did; his own lines are unchanged. */
	@GameTest(maxTicks = 20)
	public void stolasLinesRenderAsBefore(GameTestHelper helper) {
		helper.assertTrue(Language.getInstance().has("message.jugcraft.concordance.pact.answer.sealed"), "the server reads the mod's words (Fabric's server translations)");
		ServerLevel level = helper.getLevel();
		// missing.owner, answer.sealed, row.end, ended_here: an owner's conversation, and a second reader at the same perch.
		ServerPlayer owner = ready(level, TestPlayers.survivor(helper));
		pocket(owner, 1, AstrolabeItem.CAPACITY);
		BlockPos at = new BlockPos(1, 1, 1);
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu mine = open(helper, owner, perch);
		ServerPlayer two = ready(level, TestPlayers.survivor(helper));
		beside(helper, two, at);
		PactMenu theirs = open(helper, two, perch);
		rendersAs(helper, note(theirs.lastSent(), "missing.owner"), "Only this perch's owner may invite Stolas here", "missing.owner");
		two.closeContainer();
		PactStatePayload sealed = send(owner, mine, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(answered(sealed, PactMenu.SEALED), "Its owner seals the pact");
		rendersAs(helper, sealed.result().get().message(), "The pact is sealed: Stolas keeps this perch", "answer.sealed");
		rendersAs(helper, note(sealed, "row.end"), "Ending returns what is paid ahead to you, once, and Stolas departs; breaking the perch ends nothing", "row.end");
		PactStatePayload ended = send(owner, mine, PactRequestPayload.END, 0);
		helper.assertTrue(answered(ended, "ended"), "Its owner ends it");
		rendersAs(helper, note(ended, "ended_here"), "The pact is ended. Use the perch again to invite Stolas anew", "ended_here");
		owner.closeContainer();
		// missing.held, then seat.offer: a holder at a second perch of theirs, then with their first perch broken.
		ServerPlayer holder = regarded(helper);
		BlockPos kept = new BlockPos(4, 1, 1);
		stolasPact(helper, holder, kept, level.getGameTime());
		BlockPos second = new BlockPos(4, 1, 4);
		PactPerchBlockEntity other = perch(helper, second, holder);
		beside(helper, holder, second);
		rendersAs(helper, note(open(helper, holder, other).lastSent(), "missing.held"), "You already keep a pact with Stolas", "missing.held");
		holder.closeContainer();
		helper.setBlock(kept, Blocks.AIR);
		rendersAs(helper, note(open(helper, holder, other).lastSent(), "seat.offer"), "Your pact with Stolas is without its perch: seat it here, or end it",
				"seat.offer");
		holder.closeContainer();
		// answer.declined: a ready player declines at their own perch.
		ServerPlayer decliner = ready(level, TestPlayers.survivor(helper));
		BlockPos theirsAt = new BlockPos(7, 1, 1);
		PactPerchBlockEntity declined = perch(helper, theirsAt, decliner);
		beside(helper, decliner, theirsAt);
		PactStatePayload left = send(decliner, open(helper, decliner, declined), PactRequestPayload.DECLINE, 0);
		helper.assertTrue(answered(left, PactMenu.DECLINED), "They decline");
		rendersAs(helper, left.result().get().message(), "You decline; Stolas leaves. Nothing was spent", "answer.declined");
		// answer.missing_evidence: a novice (the Binding Arts only) accepts.
		ServerPlayer novice = TestPlayers.survivor(helper);
		ConcordanceProgress.grant(novice, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(novice, Workers.RESEARCH, ResearchState.UNDERSTOOD);
		RateGate.forget(novice.getUUID());
		BlockPos noviceAt = new BlockPos(1, 1, 7);
		PactPerchBlockEntity learning = perch(helper, noviceAt, novice);
		beside(helper, novice, noviceAt);
		PactStatePayload refused = send(novice, open(helper, novice, learning), PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(answered(refused, PactMenu.MISSING_EVIDENCE), "A novice's acceptance waits for the evidence");
		rendersAs(helper, refused.result().get().message(), "Stolas will not agree yet: see what is missing. Nothing was spent", "answer.missing_evidence");
		novice.closeContainer();
		// unavailable: no definition loaded, a ready player opens their own perch.
		ConcordanceRules was = ConcordanceData.rules();
		try {
			ConcordanceData.replace(was.withPacts(PactCatalog.parse(Map.of(), was.research().keySet())));
			ServerPlayer reader = ready(level, TestPlayers.survivor(helper));
			BlockPos readerAt = new BlockPos(4, 1, 7);
			PactPerchBlockEntity unread = perch(helper, readerAt, reader);
			beside(helper, reader, readerAt);
			rendersAs(helper, note(open(helper, reader, unread).lastSent(), "unavailable"),
					"Stolas's terms could not be read on this server (the pact's definition did not load); nothing can be agreed until it does", "unavailable");
			reader.closeContainer();
		} finally {
			ConcordanceData.replace(was);
		}
		// Jade's visiting line, as the client builds it (JugcraftJadeClient.pactLines, the reading's visitor).
		rendersAs(helper, Component.translatable("tooltip.jugcraft.concordance.jade.pact.visiting", Component.translatable("entity.jugcraft.stolas")),
				"Stolas is visiting: he waits for an answer", "jade visiting");
		// Three of the 26 lines kept as Chunk D wrote them (the audit holds all 26: check_pacts STOLAS_OWN_LINES).
		rendersAs(helper, Component.translatable(PACT_KEY + "reckoning.nothing"), "No pact with Stolas is kept at this perch", "reckoning.nothing");
		rendersAs(helper, Component.translatable("screen.jugcraft.pact.hear_terms"), "Hear his terms", "hear_terms");
		rendersAs(helper, Component.translatable(PACT_KEY + "row.state.suspended"),
				"Suspended by you: Stolas reckons nothing and nothing falls due until you resume it", "row.state.suspended");
		helper.succeed();
	}

	/** GC-01 (GB23, GC-P4): a compact without resonance names no astrolabe, in its term or its refusal; it is sealed with its materials alone. */
	@GameTest(maxTicks = 20)
	public void aCompactWithoutResonanceNamesNoAstrolabe(GameTestHelper helper) {
		// The words are read as the server renders them; without the mod's words a key would name neither, so the check would
		// say nothing (GC-P29): the server must have them.
		helper.assertTrue(Language.getInstance().has(PACT_KEY + "term.compact.items"), "the server reads the mod's words (Fabric's server translations)");
		ServerLevel level = helper.getLevel();
		ConcordanceRules was = ConcordanceData.rules();
		JsonObject plain = shipped(level);
		plain.getAsJsonObject("compact").addProperty("resonance", 0);
		try {
			ConcordanceData.replace(was.withPacts(PactCatalog.parse(Map.of(PactDefinition.STOLAS, plain), was.research().keySet())));
			PactDefinition offer = stolasOffer();
			ServerPlayer owner = ready(level, TestPlayers.survivor(helper)); // no materials, no astrolabe
			BlockPos at = new BlockPos(3, 1, 3);
			PactPerchBlockEntity perch = perch(helper, at, owner);
			beside(helper, owner, at);
			PactMenu menu = open(helper, owner, perch);
			Component compact = term(menu.lastSent(), "compact");
			helper.assertTrue(menu.lastSent().resonance() == 0 && key(compact).equals(PACT_KEY + "term.compact.items") && namesNoAstrolabe(compact),
					"Without resonance its compact term names no astrolabe: " + (compact == null ? null : compact.getString()));
			PactStatePayload short_ = send(owner, menu, PactRequestPayload.ACCEPT, 0);
			helper.assertTrue(answered(short_, Pacts.Outcome.CANNOT_AFFORD.id) && said(short_).equals(PACT_KEY + "answer.cannot_afford.items")
					&& namesNoAstrolabe(short_.result().get().message()) && BoundWills.of(level.getServer()).ledger().heldBy(owner.getUUID()).isEmpty(),
					"Short of the materials, the refusal names no astrolabe and nothing is held: " + short_.result().get().message().getString());
			for (PactDefinition.Cost cost : offer.compact()) {
				give(owner, new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(cost.item())), cost.count()));
			}
			helper.assertTrue(answered(send(owner, menu, PactRequestPayload.ACCEPT, 0), PactMenu.SEALED) && pocketed(owner, 0, offer) && resonance(owner) == 0,
					"With its materials alone it is sealed; they are spent, and no astrolabe was needed");
		} finally {
			ConcordanceData.replace(was);
		}
		helper.succeed();
	}

	/** GC-01 (SG5): every state names the spirit it is about, its sign and work; the nested spirit reads back as written, and a state built as Chunk D's reads back about no spirit. */
	@GameTest(maxTicks = 20)
	public void theStateNamesItsSpirit(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PactStatePayload.Spirit stolasSpirit = new PactStatePayload.Spirit(StolasEntity.SIGN, StarReckoning.WORK);
		ServerPlayer owner = ready(level, TestPlayers.survivor(helper));
		pocket(owner, 1, AstrolabeItem.CAPACITY);
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu menu = open(helper, owner, perch);
		List<PactStatePayload> states = new ArrayList<>(List.of(menu.lastSent()));
		states.add(send(owner, menu, PactRequestPayload.ACCEPT, 0));
		owner.closeContainer();
		helper.setBlock(at, Blocks.AIR);
		BlockPos second = new BlockPos(6, 1, 2);
		PactPerchBlockEntity fresh = perch(helper, second, owner);
		beside(helper, owner, second);
		PactMenu seat = open(helper, owner, fresh);
		states.add(seat.lastSent());
		states.add(send(owner, seat, PactRequestPayload.END, 0));
		List<Integer> modes = states.stream().map(PactStatePayload::mode).toList();
		helper.assertValueEqual(modes, List.of(PactStatePayload.NEGOTIATE, PactStatePayload.KEEP, PactStatePayload.SEAT, PactStatePayload.CLOSED),
				"A negotiation, the kept pact, its seat page, and the page after it is ended");
		helper.assertTrue(keys(states.getLast().notes()).contains(PACT_KEY + "ended_here"), "ended here");
		for (PactStatePayload state : states) {
			helper.assertTrue(state.spirit().equals(stolasSpirit) && key(state.speaker()).equals("entity.jugcraft.stolas"),
					"Mode " + state.mode() + " names its spirit, his sign and work, and he speaks: " + state.spirit());
		}
		RegistryAccess registries = level.registryAccess();
		PactStatePayload.Spirit wide = new PactStatePayload.Spirit("jugcraft:" + "o".repeat(200), "w".repeat(80));
		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
		PactStatePayload.CODEC.encode(buffer, new PactStatePayload(7, 3, PactStatePayload.KEEP, Component.literal("Stolas"), List.of(), List.of(), List.of(),
				List.of(), List.of(), List.of(), 0, 0, Optional.empty(), wide));
		PactStatePayload back = PactStatePayload.CODEC.decode(buffer);
		helper.assertTrue(back.spirit().equals(wide) && buffer.readableBytes() == 0, "The nested spirit reads back as written: " + back.spirit());
		RegistryFriendlyByteBuf old = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
		PactStatePayload.CODEC.encode(old, new PactStatePayload(7, 3, PactStatePayload.KEEP, Component.literal("Stolas"), List.of(), List.of(), List.of(),
				List.of(), List.of(), List.of(), 0, 0, Optional.empty()));
		PactStatePayload none = PactStatePayload.CODEC.decode(old);
		helper.assertTrue(none.spirit().equals(PactStatePayload.Spirit.NONE) && old.readableBytes() == 0, "A state built as Chunk D's is about no spirit");
		helper.succeed();
	}

	/** GC-01 (SG1, SG3): every work the parser accepts has its service, holder-only as the parser says; every offered pact is a registered spirit's, in REQUIRED's order; a definition naming no spirit is offered nowhere. */
	@GameTest(maxTicks = 20)
	public void everyWorkHasItsServiceAndEverySpiritItsType(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(PactParser.WORK.stream().allMatch(w -> PactServices.get(w) != null && PactServices.get(w).work().equals(w)
				&& PactServices.get(w).holderOnly() == PactParser.HOLDER_ONLY.contains(w)) && PactServices.get(StarReckoning.WORK) == StarReckoning.SERVICE,
				"Each work has its one service, holder-only as the parser says; Star-reckoning's is StarReckoning.SERVICE");
		helper.assertTrue(PactSpirits.get(StolasEntity.COUNTERPART).type() == Workers.STOLAS && PactSpirits.sign(StolasEntity.COUNTERPART).equals(StolasEntity.SIGN)
				&& PactSpirits.get("jugcraft:no_such_spirit") == null && PactSpirits.sign("jugcraft:no_such_spirit").isEmpty(),
				"Stolas is registered with his type and sign; a counterpart that is no spirit has neither");
		List<String> offered = PactPerchBlockEntity.offered().stream().map(PactDefinition::id).toList();
		List<String> shipped = PactParser.REQUIRED.stream().filter(id -> ConcordanceData.rules().pacts().get(id) != null).toList();
		helper.assertTrue(offered.stream().allMatch(PactSpirits::knows) && offered.equals(shipped) && offered.getFirst().equals(PactDefinition.STOLAS),
				"Every offer is a registered spirit's, in REQUIRED's order, Stolas's first: " + offered);
		ConcordanceRules was = ConcordanceData.rules();
		try {
			ConcordanceData.replace(was.withPacts(PactCatalog.parse(Map.of(PactDefinition.STOLAS, shipped(level), "jugcraft:no_such_spirit", shipped(level)),
					was.research().keySet())));
			helper.assertTrue(ConcordanceData.rules().pacts().get("jugcraft:no_such_spirit") != null
					&& PactPerchBlockEntity.offered().stream().map(PactDefinition::id).toList().equals(List.of(PactDefinition.STOLAS)),
					"A definition whose counterpart is no spirit loads, and is offered nowhere");
		} finally {
			ConcordanceData.replace(was);
		}
		helper.succeed();
	}

	// SG12: the shared pact cases, run for every spirit a perch offers. A fixture table says what each spirit needs, besides its
	// research, before it will come (its service's missing met); every offered definition must have one, so each new spirit
	// inherits the cases by adding its row. Every think passes world time 0 (06:00, inside every unit-1 spirit's hours: A-05).

	/** GC-01 (SG12): what a test player needs before a spirit will come to a perch at {@code perch}, besides its research; the blocks it set (cleared by the case). */
	@FunctionalInterface
	interface Fixture {
		List<BlockPos> meet(GameTestHelper helper, ServerPlayer player, BlockPos perch);
	}

	/** Each pact spirit's fixture, by counterpart, filled below and by each spirit's section (GC-04 adds Buer's). */
	static final Map<String, Fixture> FIXTURES = new LinkedHashMap<>();

	static {
		// Stolas: a star observed (his service's missing step).
		FIXTURES.put(StolasEntity.COUNTERPART, (helper, player, perch) -> {
			AstralClaims.of(helper.getLevel().getServer()).observe(player.getUUID(), LANTERN_STAR, helper.getLevel().getGameTime());
			return List.of();
		});
	}

	/** Every offer a perch makes (PactPerchBlockEntity.offered), each with its fixture: a spirit without one fails the case. */
	static List<PactDefinition> everySpirit(GameTestHelper helper) {
		List<PactDefinition> all = PactPerchBlockEntity.offered();
		helper.assertTrue(!all.isEmpty(), "Some pact is offered");
		for (PactDefinition d : all) {
			helper.assertTrue(FIXTURES.containsKey(d.id()), d.id() + " has its fixture (SG12): add it to FIXTURES");
		}
		return all;
	}

	/** Spirit {@code i}'s perch: one cell of a 3 x 3 grid, so seven spirits fit one test. */
	static BlockPos perchFor(int i) {
		return new BlockPos(1 + 3 * (i % 3), 1, 1 + 3 * (i / 3));
	}

	/** {@code research} understood, and first each research it requires (and First Light). */
	static void understand(ServerPlayer player, String research) {
		Definitions.Research entry = ConcordanceData.rules().research(research);
		if (entry != null) {
			for (Definitions.Requirement requirement : entry.requires()) {
				understand(player, requirement.research());
			}
		}
		ConcordanceProgress.grant(player, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(player, research, ResearchState.UNDERSTOOD);
	}

	/** {@code times} of {@code d}'s compacts' materials, and an astrolabe at capacity when it asks resonance. */
	static void pocketFor(ServerPlayer player, PactDefinition d, int times) {
		for (PactDefinition.Cost cost : d.compact()) {
			player.getInventory().add(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(cost.item())), cost.count() * times));
		}
		if (d.resonance() > 0) {
			player.getInventory().add(astrolabe(AstrolabeItem.CAPACITY));
		}
	}

	/** The resonance {@code player}, pocketed by pocketFor, holds before paying {@code d}'s compact. */
	static int resonanceFor(PactDefinition d) {
		return d.resonance() > 0 ? AstrolabeItem.CAPACITY : 0;
	}

	/** A survival player ready to invite {@code d} at {@code perch}: its research understood, {@code times} compacts, its fixture met. */
	static ServerPlayer invitee(GameTestHelper helper, PactDefinition d, BlockPos perch, int times) {
		ServerPlayer player = TestPlayers.survivor(helper);
		understand(player, d.research());
		pocketFor(player, d, times);
		FIXTURES.get(d.id()).meet(helper, player, perch);
		RateGate.forget(player.getUUID());
		return player;
	}

	/** {@code d}'s pact sealed through the perch's menu by its owner {@code player} at {@code at}; its menu. */
	static PactMenu sealThrough(GameTestHelper helper, ServerPlayer player, PactDefinition d, BlockPos at) {
		PactPerchBlockEntity perch = perch(helper, at, player);
		beside(helper, player, at);
		// GC-05 (GC-P59): a garden invitee may invite Buer and Bathin both, so the chooser comes first.
		PactMenu menu = offerOf(helper, player, perch, d);
		helper.assertTrue(answered(send(player, menu, PactRequestPayload.ACCEPT, 0), PactMenu.SEALED), d.id() + "'s pact is sealed");
		return menu;
	}

	/** {@code d}'s pact sealed through the holder (its compact left out) for {@code holder} at a perch they placed at {@code at}, at game time {@code now}; they stand beside it. */
	static PactPerchBlockEntity pactWith(GameTestHelper helper, ServerPlayer holder, PactDefinition d, BlockPos at, long now) {
		PactPerchBlockEntity perch = perch(helper, at, holder);
		beside(helper, holder, at);
		helper.assertValueEqual(perch.holder().seal(helper.getLevel(), holder, PactPerchBlockEntity.sealing(d, null, null), now).outcome(),
				Pacts.Outcome.DONE, d.id() + "'s pact is sealed at the perch");
		return perch;
	}

	/** The first offering {@code d}'s upkeep takes (its tag's first item). */
	static Item offeringOf(PactDefinition d) {
		for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, Identifier.parse(d.offering())))) {
			return holder.value();
		}
		throw new IllegalStateException(d.offering() + " holds no item");
	}

	/** SG12: invited, every spirit explains before agreeing; accepting seals once and spends once, its terms copied; accepting again seals and spends nothing more. */
	@GameTest(maxTicks = 20)
	public void everySpiritSealsOnceAndSpendsOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer player = invitee(helper, d, at, 2);
			PactPerchBlockEntity perch = perch(helper, at, player);
			beside(helper, player, at);
			// GC-05 (GC-P59): a garden invitee meets the chooser first (Buer and Bathin share The Garden's Regard), which counts
			// one revision before the offer.
			PactMenu menu = offerOf(helper, player, perch, d);
			PactStatePayload opened = menu.lastSent();
			List<PactSpiritEntity<?>> visitors = spiritsOf(helper, d);
			helper.assertTrue(opened.mode() == PactStatePayload.NEGOTIATE && opened.revision() >= 1 && opened.notes().isEmpty() && opened.offers(PactRequestPayload.ACCEPT)
					&& opened.spirit().equals(new PactStatePayload.Spirit(PactSpirits.sign(d.id()), d.work())) && visitors.size() == 1 && visitors.getFirst().owner() == null,
					d.id() + ": opened, its offer at revision 1 or later, nothing missing, its spirit named, a visitor bound to no one: " + opened);
			UUID visitor = visitors.getFirst().getUUID();
			long now = level.getGameTime();
			PactStatePayload sealed = send(player, menu, PactRequestPayload.ACCEPT, 0);
			helper.assertTrue(answered(sealed, PactMenu.SEALED) && sealed.mode() == PactStatePayload.KEEP && pocketed(player, 1, d)
					&& resonance(player) == resonanceFor(d) - d.resonance(), d.id() + ": sealed, kept here, the compact spent once");
			BoundWill record = recordOf(level, player);
			helper.assertTrue(record != null && d.id().equals(Pacts.counterpart(record.counterpart())) && d.agreement().equals(record.agreement())
					&& new BoundWill.Service(d.work(), d.radius(), d.from(), d.to(), d.quota()).equals(record.pact().service())
					&& record.pact().upkeep().due() == now + d.period() && d.beneficiaries().equals(record.pact().beneficiaries())
					&& visitor.equals(record.pact().presence()) && "pact".equals(WorkerRoster.of(level.getServer()).workers(player.getUUID()).get(visitor).kind()),
					d.id() + ": one Bound Will, its terms copied, the first offering a period on, the visitor its presence on the roster as a pact");
			PactStatePayload again = send(player, menu, PactRequestPayload.ACCEPT, 0);
			helper.assertTrue(answered(again, Pacts.Outcome.TAKEN.id) && pocketed(player, 1, d) && resonance(player) == resonanceFor(d) - d.resonance()
					&& BoundWills.of(level.getServer()).ledger().heldBy(player.getUUID()).size() == 1, d.id() + ": accepting again seals nothing and spends nothing");
			player.closeContainer();
		}
		helper.succeed();
	}

	/** SG12: one pact with each spirit a holder: at a second perch of theirs it does not come and accepting is refused, nothing spent. */
	@GameTest(maxTicks = 20)
	public void everySpiritIsOnePactAHolder(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer player = invitee(helper, d, at, 2);
			sealThrough(helper, player, d, at);
			int left = resonance(player);
			// GC-06b: north of its cell (z 0, 3 or 6), inside the structure for each of seven spirits' cells (south of the
			// seventh, z 8, is outside it).
			BlockPos second = at.north();
			PactPerchBlockEntity other = perch(helper, second, player);
			beside(helper, player, second);
			PactMenu menu = open(helper, player, other);
			// GC-05 (GC-P59): a garden holder is offered the other garden spirit there (the one other they may invite); the
			// second perch never offers d itself, or offers it already held, and d never comes twice.
			PactStatePayload shown = menu.lastSent();
			helper.assertTrue((!PactSpirits.sign(d.id()).equals(shown.spirit().sign()) || note(shown, "missing.held") != null)
					&& spiritsOf(helper, d).size() == 1, d.id() + ": at a second perch it is not offered again, or offered already held; it does not come: " + shown);
			PactStatePayload refused = send(player, menu, PactRequestPayload.ACCEPT, 0);
			helper.assertTrue(refused != null && !refused.result().get().done() && pocketed(player, 1, d) && resonance(player) == left
					&& BoundWills.of(level.getServer()).ledger().heldBy(player.getUUID()).size() == 1 && spiritsOf(helper, d).size() == 1,
					d.id() + ": accepting there seals nothing of d's (one record) and spends none of its compact: " + said(refused));
			player.closeContainer();
		}
		helper.succeed();
	}

	/** SG12: without the compact's materials, accepting is refused with its reason (worded without an astrolabe when it asks no resonance), nothing spent; declining sends the visitor away. */
	@GameTest(maxTicks = 20)
	public void everySpiritsRefusalSpendsNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer player = invitee(helper, d, at, 0);
			PactPerchBlockEntity perch = perch(helper, at, player);
			beside(helper, player, at);
			PactMenu menu = offerOf(helper, player, perch, d);
			List<PactSpiritEntity<?>> visitors = spiritsOf(helper, d);
			helper.assertValueEqual(visitors.size(), 1, d.id() + ": it comes to hear them");
			PactStatePayload refused = send(player, menu, PactRequestPayload.ACCEPT, 0);
			helper.assertTrue(answered(refused, Pacts.Outcome.CANNOT_AFFORD.id)
					&& said(refused).equals(PACT_KEY + (d.resonance() > 0 ? "answer.cannot_afford" : "answer.cannot_afford.items"))
					&& BoundWills.of(level.getServer()).ledger().heldBy(player.getUUID()).isEmpty() && resonance(player) == resonanceFor(d),
					d.id() + ": short of the compact, refused with its reason; nothing held or spent: " + said(refused));
			PactStatePayload declined = send(player, menu, PactRequestPayload.DECLINE, 0);
			helper.assertTrue(answered(declined, PactMenu.DECLINED) && player.containerMenu == player.inventoryMenu
					&& visitors.getFirst().think(level, 0L, level.getGameTime()) == Status.FINISHED && visitors.getFirst().gone(),
					d.id() + ": declined, the conversation closes and the visitor leaves");
		}
		helper.succeed();
	}

	/** SG12: every spirit's upkeep warns when due, gives its grace, suspends after it, and its own offering resumes it; an absence while warned keeps only the grace left (D-P50). */
	@GameTest(maxTicks = 20)
	public void everySpiritsUpkeepWarnsGivesGraceSuspendsAndResumes(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer player = invitee(helper, d, at, 0);
			long g0 = level.getGameTime() + 1_000_000L * (i + 1);
			PactPerchBlockEntity perch = pactWith(helper, player, d, at, g0);
			long due = pactOf(level, perch).pact().upkeep().due();
			helper.assertTrue(due == g0 + d.period() && stay(level, perch.holder(), g0 + 1_000L, due - 1_000L).isEmpty(), d.id() + ": nothing falls due before its period");
			helper.assertTrue(perch.holder().look(level, due) == Pacts.Event.WARNED && Pacts.status(pactOf(level, perch)) == Status.UPKEEP_DUE,
					d.id() + ": due with none paid ahead, a warning; an offering is due");
			helper.assertTrue(stay(level, perch.holder(), due + 1_000L, due + d.grace() - 1_000L).isEmpty()
					&& perch.holder().look(level, due + d.grace()) == Pacts.Event.SUSPENDED && pactOf(level, perch).pact().hold() == BoundWill.Hold.UPKEEP,
					d.id() + ": nothing more within the grace; at its end, suspended for upkeep");
			Item offering = offeringOf(d);
			give(player, new ItemStack(offering, d.count()));
			long t2 = due + d.grace() + 500L;
			helper.assertTrue(PactKeeping.deposit(level, player, perch.holder(), offering, t2) == d.count() && Pacts.status(pactOf(level, perch)) == null
					&& pactOf(level, perch).pact().hold() == BoundWill.Hold.NONE, d.id() + ": its own offering resumes it");
			long t3 = pactOf(level, perch).pact().upkeep().due();
			helper.assertTrue(perch.holder().look(level, t3) == Pacts.Event.WARNED && perch.holder().look(level, t3 + 1_000L) == Pacts.Event.NONE,
					d.id() + ": warned at the next period, an hour of its grace spent present");
			place(helper, player, at.getX() + 100.5, at.getY(), at.getZ() + 0.5);
			helper.assertValueEqual(perch.holder().look(level, t3 + 2_000L), Pacts.Event.NONE, d.id() + ": away, nothing is looked at");
			beside(helper, player, at);
			BoundWill.Upkeep before = pactOf(level, perch).pact().upkeep();
			long back = t3 + 3 * Pacts.DAY;
			helper.assertValueEqual(perch.holder().look(level, back), Pacts.Event.WARNED, d.id() + ": back, warned again");
			long graceUntil = pactOf(level, perch).pact().upkeep().graceUntil();
			helper.assertTrue(graceUntil == before.graceUntil() + (back - before.lookedAt()) && graceUntil < back + d.grace(),
					d.id() + ": with only the grace left, its end moved on by the time away (D-P50): " + pactOf(level, perch).pact().upkeep());
		}
		helper.succeed();
	}

	/** SG12: with the absent-owner option on, a stand-in answers for no spirit's upkeep: it pays, serves, ends and suspends nothing; back in person, one period falls due. */
	@GameTest(maxTicks = 20)
	public void aStandInNeverAnswersAnySpiritsUpkeep(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer keeper = invitee(helper, d, at, 0);
			UUID id = keeper.getUUID();
			long s0 = level.getGameTime() + 1_000_000L * (i + 1);
			PactPerchBlockEntity perch = pactWith(helper, keeper, d, at, s0);
			UUID record = perch.holder().agreement().record();
			Item offering = offeringOf(d);
			give(keeper, new ItemStack(offering, 2 * d.count()));
			helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), offering, s0), 2 * d.count(), d.id() + ": two periods paid ahead");
			BoundWill.Upkeep before = pactOf(level, perch).pact().upkeep();
			TestPlayers.leave(keeper);
			boolean was = JugcraftConfig.option(Authority.ABSENT_OPTION);
			JugcraftConfig.setOption(Authority.ABSENT_OPTION, true);
			try {
				ServerPlayer answering = Authority.answering(level, id);
				helper.assertTrue(answering != null && Authority.isStandIn(answering), d.id() + ": a stand-in answers protection questions for the absent holder");
				for (long t = s0 + Pacts.DAY; t <= s0 + 3 * d.period(); t += Pacts.DAY) {
					helper.assertValueEqual(perch.holder().look(level, t), Pacts.Event.NONE, d.id() + ": but never for upkeep");
					PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), t);
				}
				helper.assertTrue(escrowed(level, record) == 2 * d.count() && pactOf(level, perch).pact().upkeep().equals(before), d.id() + ": nothing is taken, warned or moved");
				ItemStack offered = new ItemStack(offering, 1);
				PactService service = PactServices.get(d.work());
				PactMenu.Answered served = service.perform(level, answering, perch.holder(), "", 0L, s0 + 100);
				helper.assertTrue(perch.holder().deposit(level, answering, offered, s0) == 0 && offered.getCount() == 1 && escrowed(level, record) == 2 * d.count()
						&& (served == null || !served.done()) && perch.holder().end(level, answering, s0 + 100) == AgreementHolder.Ended.NOT_PERMITTED
						&& PactKeeping.suspend(level, answering, perch.holder(), s0 + 100) == AgreementHolder.Toggle.NOT_HOLDER,
						d.id() + ": it pays (a real offering in hand), serves, ends and suspends nothing for them");
			} finally {
				JugcraftConfig.setOption(Authority.ABSENT_OPTION, was);
			}
			ServerPlayer back = TestPlayers.again(helper, id);
			beside(helper, back, at);
			long tr = s0 + 3 * d.period() + 500L;
			helper.assertTrue(perch.holder().look(level, tr) == Pacts.Event.PAID && escrowed(level, record) == d.count(),
					d.id() + ": back in person, one period falls due: one period's offering");
		}
		helper.succeed();
	}

	/** SG12: breaking any spirit's perch ends nothing: the pact is without its perch, its escrow kept, its presence gone, nothing departed. */
	@GameTest(maxTicks = 20)
	public void everySpiritsBrokenPerchLeavesThePactWithoutIt(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer keeper = invitee(helper, d, at, 0);
			long g0 = level.getGameTime();
			PactPerchBlockEntity perch = pactWith(helper, keeper, d, at, g0);
			UUID record = perch.holder().agreement().record();
			Item offering = offeringOf(d);
			give(keeper, new ItemStack(offering, 2));
			helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), offering, g0), 2, d.id() + ": two paid ahead");
			PactSpiritEntity<?> presence = presenceAt(level, perch);
			helper.assertTrue(presence != null, d.id() + ": its presence perches there");
			helper.setBlock(at, Blocks.AIR);
			BoundWill kept = BoundWills.of(level.getServer()).ledger().get(record);
			helper.assertTrue(kept != null && kept.pact().place() == null && escrowed(level, record) == 2 && presence.gone()
					&& PactHistory.of(level.getServer()).history(keeper.getUUID()).isEmpty(),
					d.id() + ": broken, the perch ends nothing: the pact is without it, its escrow kept, its presence gone, nothing departed");
		}
		helper.succeed();
	}

	/** SG12: any spirit's pact without its perch is seated again at a new perch of its holder's, through its screen: the same record and escrow, its spirit called there once. */
	@GameTest(maxTicks = 20)
	public void everySpiritsPactIsSeatedAgain(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer keeper = invitee(helper, d, at, 0);
			long g0 = level.getGameTime();
			PactPerchBlockEntity perch = pactWith(helper, keeper, d, at, g0);
			UUID record = perch.holder().agreement().record();
			Item offering = offeringOf(d);
			give(keeper, new ItemStack(offering, 2));
			helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), offering, g0), 2, d.id() + ": two paid ahead");
			helper.setBlock(at, Blocks.AIR);
			// GC-06b: north of its cell, inside the structure for each of seven spirits' cells.
			BlockPos second = at.north();
			PactPerchBlockEntity fresh = perch(helper, second, keeper);
			beside(helper, keeper, second);
			PactMenu menu = open(helper, keeper, fresh);
			helper.assertTrue(menu.lastSent().mode() == PactStatePayload.SEAT && menu.lastSent().patterns().isEmpty(),
					d.id() + ": at a new perch of theirs, the seat page, nothing listed");
			PactStatePayload seated = send(keeper, menu, PactRequestPayload.SEAT, 0);
			PactSpiritEntity<?> presence = presenceAt(level, fresh);
			helper.assertTrue(answered(seated, AgreementHolder.Seat.SEATED.id) && seated.mode() == PactStatePayload.KEEP
					&& Couriers.place(level, helper.absolutePos(second)).equals(BoundWills.of(level.getServer()).ledger().get(record).pact().place())
					&& presence != null && presence.getType() == PactSpirits.get(d.id()).type() && spiritsOf(helper, d).size() == 1
					&& fresh.holder().agreement().record().equals(record) && escrowed(level, record) == 2,
					d.id() + ": seated, the same record and escrow, its one presence there");
			keeper.closeContainer();
		}
		helper.succeed();
	}

	/** SG12: any spirit's presence discarded ends nothing; its holder away, nobody is called; back near, it returns under the same record, arriving, at no cost. */
	@GameTest(maxTicks = 20)
	public void aRemovedPresenceNeverEndsAnySpiritsPact(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer keeper = invitee(helper, d, at, 0);
			long g0 = level.getGameTime();
			PactPerchBlockEntity perch = pactWith(helper, keeper, d, at, g0);
			UUID record = perch.holder().agreement().record();
			Item offering = offeringOf(d);
			give(keeper, new ItemStack(offering, 2));
			PactKeeping.deposit(level, keeper, perch.holder(), offering, g0);
			BoundWill before = pactOf(level, perch);
			presenceAt(level, perch).discard();
			BoundWill kept = BoundWills.of(level.getServer()).ledger().get(record);
			helper.assertTrue(kept != null && kept.pact().upkeep().due() == before.pact().upkeep().due() && kept.pact().service().equals(before.pact().service())
					&& kept.pact().hold() == before.pact().hold() && escrowed(level, record) == 2 && PactHistory.of(level.getServer()).history(keeper.getUUID()).isEmpty()
					&& perch.holder().spiritLost(level), d.id() + ": discarded, its presence ends nothing; it is lost");
			place(helper, keeper, at.getX() + 100.5, at.getY(), at.getZ() + 0.5);
			PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 20);
			helper.assertTrue(presenceAt(level, perch) == null, d.id() + ": its holder away, nobody is called");
			beside(helper, keeper, at);
			PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 40);
			PactSpiritEntity<?> second = presenceAt(level, perch);
			helper.assertTrue(second != null && second.getUUID().equals(pactOf(level, perch).pact().presence()) && second.cues().equals(List.of("arrive"))
					&& escrowed(level, record) == 2 && count(keeper, BuiltInRegistries.ITEM.getKey(offering).toString()) == 0
					&& pactOf(level, perch).pact().upkeep().due() == before.pact().upkeep().due(),
					d.id() + ": back near, it returns under the same record, arriving, at no cost; the clock untouched");
		}
		helper.succeed();
	}

	/** SG12: its living holder ends any spirit's pact: what was paid ahead comes back once and the departure is kept once; ending again returns nothing. */
	@GameTest(maxTicks = 20)
	public void endingAnySpiritsPactReturnsTheEscrowOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer keeper = invitee(helper, d, at, 0);
			long g0 = level.getGameTime();
			PactPerchBlockEntity perch = pactWith(helper, keeper, d, at, g0);
			UUID record = perch.holder().agreement().record();
			Item offering = offeringOf(d);
			String item = BuiltInRegistries.ITEM.getKey(offering).toString();
			give(keeper, new ItemStack(offering, 2));
			helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), offering, g0), 2, d.id() + ": two paid ahead");
			PactKeeping.Ending ending = PactKeeping.end(level, keeper, perch.holder(), d.id(), g0 + 100);
			List<Pacts.Departure> history = PactHistory.of(level.getServer()).history(keeper.getUUID());
			helper.assertTrue(ending.ended() == AgreementHolder.Ended.ENDED && count(keeper, item) == 2 && history.size() == 1
					&& history.getFirst().record().equals(record) && d.id().equals(Pacts.counterpart(history.getFirst().counterpart())),
					d.id() + ": ended, the two come back once, the departure kept once: " + history);
			helper.assertTrue(PactKeeping.end(level, keeper, perch.holder(), d.id(), g0 + 200).ended() != AgreementHolder.Ended.ENDED && count(keeper, item) == 2
					&& PactHistory.of(level.getServer()).history(keeper.getUUID()).size() == 1, d.id() + ": ending again returns nothing more");
		}
		helper.succeed();
	}

	/** SG12: any spirit's perch copied elsewhere is inert: it names another agreement, no presence or visitor, and no action of its screen changes the pact. */
	@GameTest(maxTicks = 20)
	public void aCopiedPerchIsInertForEverySpirit(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer keeper = invitee(helper, d, at, 0);
			long now = level.getGameTime();
			PactPerchBlockEntity original = pactWith(helper, keeper, d, at, now);
			Item offering = offeringOf(d);
			give(keeper, new ItemStack(offering, 2));
			helper.assertValueEqual(PactKeeping.deposit(level, keeper, original.holder(), offering, now), 2, d.id() + ": two paid ahead");
			BoundWill record = pactOf(level, original);
			BlockPos copyAt = at.north();
			helper.setBlock(copyAt, Workers.PACT_PERCH);
			PactPerchBlockEntity copy = helper.getBlockEntity(copyAt, PactPerchBlockEntity.class);
			copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), original.saveWithoutMetadata(level.registryAccess())));
			beside(helper, keeper, copyAt);
			PactMenu menu = open(helper, keeper, copy);
			helper.assertTrue(copy.holder().verify(level, now) == AgreementHolder.Standing.ELSEWHERE && menu.lastSent().mode() == PactStatePayload.CLOSED
					&& keys(menu.lastSent().notes()).contains("compose.jugcraft.diagnostic.conflicting_agreement") && copy.presence(level) == null
					&& copy.visitor() == null, d.id() + ": the copy names another agreement, and no presence or visitor");
			for (int action : List.of(PactRequestPayload.ACCEPT, PactRequestPayload.DEPOSIT, PactRequestPayload.SUSPEND, PactRequestPayload.RESUME,
					PactRequestPayload.END, PactRequestPayload.SEAT, PactRequestPayload.RECALL, PactRequestPayload.INVITE)) {
				PactStatePayload answer = send(keeper, menu, action, 0);
				helper.assertTrue(answer != null && answer.result().isPresent() && !answer.result().get().done()
						&& record.equals(BoundWills.of(level.getServer()).ledger().get(record.id())) && escrowed(level, record.id()) == 2,
						d.id() + ": at the copy, action " + action + " changes nothing: " + answer);
			}
			keeper.closeContainer();
		}
		helper.succeed();
	}

	/** SG12: one presence a pact, for every spirit: looks and openings call no second while it serves or is merely unloaded; struck off, one is called; the old one loaded again finds another in its place and departs. */
	@GameTest(maxTicks = 20)
	public void onePresencePerPactForEverySpirit(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer keeper = invitee(helper, d, at, 0);
			long g0 = level.getGameTime();
			PactPerchBlockEntity perch = pactWith(helper, keeper, d, at, g0);
			PactSpiritEntity<?> first = presenceAt(level, perch);
			for (int n = 1; n <= 3; n++) {
				PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 20L * n);
				RateGate.forget(keeper.getUUID());
				PactMenu.open(keeper, level, perch);
				keeper.closeContainer();
			}
			helper.assertTrue(spiritsOf(helper, d).size() == 1 && first != null && first.equals(presenceAt(level, perch))
					&& perch.holder().recall(level, keeper, g0 + 100) == AgreementHolder.Recall.NOT_LOST, d.id() + ": serving, it is the only one");
			TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
			first.saveWithoutId(saved);
			first.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
			PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 120);
			helper.assertTrue(!perch.holder().spiritLost(level) && spiritsOf(helper, d).isEmpty()
					&& perch.holder().recall(level, keeper, g0 + 120) == AgreementHolder.Recall.NOT_LOST, d.id() + ": unloaded is not lost: no second is called");
			WorkerRoster.of(level.getServer()).remove(keeper.getUUID(), first.getUUID());
			PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 140);
			PactSpiritEntity<?> second = presenceAt(level, perch);
			helper.assertTrue(second != null && !second.getUUID().equals(first.getUUID()), d.id() + ": struck off, it is lost and one is called back");
			PactSpiritEntity<?> back = PactSpirits.get(d.id()).type().create(level, EntitySpawnReason.LOAD);
			back.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
			helper.assertTrue(back.getUUID().equals(first.getUUID()) && back.think(level, 0L, g0 + 160) == Status.FINISHED && back.gone()
					&& "depart".equals(back.cues().getLast()), d.id() + ": the old one, loaded again, finds another in its place and departs");
			helper.assertTrue(second.equals(presenceAt(level, perch)) && WorkerRoster.of(level.getServer()).workers(keeper.getUUID()).size() == 1,
					d.id() + ": one presence, one roster entry, one pact");
		}
		helper.succeed();
	}

	/** SG12: every spirit's presence waits while its holder is far: no service, no upkeep, nothing changed; back near, it idles. */
	@GameTest(maxTicks = 20)
	public void everySpiritWaitsForANearHolder(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer keeper = invitee(helper, d, at, 0);
			long g0 = level.getGameTime();
			PactPerchBlockEntity perch = pactWith(helper, keeper, d, at, g0);
			UUID record = perch.holder().agreement().record();
			Item offering = offeringOf(d);
			give(keeper, new ItemStack(offering, d.count()));
			helper.assertValueEqual(PactKeeping.deposit(level, keeper, perch.holder(), offering, g0), d.count(), d.id() + ": one period paid ahead, the next due later");
			PactSpiritEntity<?> presence = presenceAt(level, perch);
			BoundWill before = pactOf(level, perch);
			place(helper, keeper, at.getX() + 100.5, at.getY(), at.getZ() + 0.5);
			helper.assertValueEqual(presence.think(level, 0L, g0 + 20), Status.OWNER_OFFLINE, d.id() + ": its holder far, it waits");
			helper.assertTrue(perch.holder().look(level, before.pact().upkeep().due()) == Pacts.Event.NONE && escrowed(level, record) == d.count()
					&& Pacts.done(pactOf(level, perch).pact(), g0 + 20) == 0 && before.equals(pactOf(level, perch)),
					d.id() + ": nothing is looked at, taken, served or changed");
			beside(helper, keeper, at);
			helper.assertValueEqual(presence.think(level, 0L, g0 + 40), Status.IDLE, d.id() + ": back near, it idles");
		}
		helper.succeed();
	}

	/** SG12: two conversations at one perch share one visitor of every spirit; its owner's acceptance seals, the other's is not permitted and spends nothing. */
	@GameTest(maxTicks = 20)
	public void twoNegotiationsAtOnePerchSealOnceForEverySpirit(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer first = invitee(helper, d, at, 1);
			ServerPlayer second = invitee(helper, d, at, 1);
			PactPerchBlockEntity perch = perch(helper, at, first);
			beside(helper, first, at);
			beside(helper, second, at);
			PactMenu one = offerOf(helper, first, perch, d);
			PactMenu two = open(helper, second, perch);
			helper.assertTrue(spiritsOf(helper, d).size() == 1 && perch.visitor() != null && note(two.lastSent(), "missing.owner") != null,
					d.id() + ": two conversations, one visitor; the one who is not its owner told so");
			helper.assertTrue(answered(send(first, one, PactRequestPayload.ACCEPT, 0), PactMenu.SEALED), d.id() + ": its owner accepts: sealed");
			helper.assertTrue(answered(send(second, two, PactRequestPayload.ACCEPT, 0), Pacts.Outcome.NOT_PERMITTED.id) && pocketed(second, 1, d)
					&& resonance(second) == resonanceFor(d) && BoundWills.of(level.getServer()).ledger().heldBy(second.getUUID()).isEmpty()
					&& spiritsOf(helper, d).size() == 1, d.id() + ": the other player is not permitted and spends nothing; one spirit");
			first.closeContainer();
			second.closeContainer();
		}
		helper.succeed();
	}

	/** SG12: a party member at the owner's perch is told only its owner may invite any spirit; none comes for them, and their acceptance is not permitted, nothing spent. */
	@GameTest(maxTicks = 20)
	public void anotherPlayerCannotAcceptForAnySpiritsHolder(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		PartyManager parties = JugcraftParties.manager();
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer owner = invitee(helper, d, at, 1);
			ServerPlayer member = invitee(helper, d, at, 1);
			long ms = System.currentTimeMillis();
			try {
				helper.assertTrue(parties.invite(owner.getUUID(), member.getUUID(), ms) == PartyManager.Result.OK
						&& parties.accept(member.getUUID(), ms) == PartyManager.Result.OK, d.id() + ": the owner and a member form a party");
				PactPerchBlockEntity perch = perch(helper, at, owner);
				beside(helper, member, at);
				PactMenu menu = open(helper, member, perch);
				helper.assertTrue(note(menu.lastSent(), "missing.owner") != null && perch.visitor() == null && spiritsOf(helper, d).isEmpty(),
						d.id() + ": only the perch's owner may invite; none comes for a party member");
				helper.assertTrue(answered(send(member, menu, PactRequestPayload.ACCEPT, 0), Pacts.Outcome.NOT_PERMITTED.id) && pocketed(member, 1, d)
						&& resonance(member) == resonanceFor(d) && BoundWills.of(level.getServer()).ledger().heldBy(member.getUUID()).isEmpty(),
						d.id() + ": accepting is not permitted; nothing spent");
				member.closeContainer();
			} finally {
				parties.adminDisband(owner.getUUID());
			}
		}
		helper.succeed();
	}

	/** SG12: every spirit's presence saves its perch, record, owner and status (Chunk D's keys, the base's); loaded, it serves on while it is the presence, and departs when another took its place. */
	@GameTest(maxTicks = 20)
	public void everySpiritsKeysSurviveASave(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer keeper = invitee(helper, d, at, 0);
			long g0 = level.getGameTime();
			PactPerchBlockEntity perch = pactWith(helper, keeper, d, at, g0);
			PactSpiritEntity<?> presence = presenceAt(level, perch);
			helper.assertValueEqual(presence.think(level, 0L, g0), Status.IDLE, d.id() + ": its presence serves, and knows its record");
			TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
			presence.saveWithoutId(out);
			CompoundTag saved = out.buildResult();
			helper.assertTrue(saved.contains("perch") && saved.contains("record") && saved.contains("owner") && saved.contains("status"),
					d.id() + ": its save holds perch, record, owner and status: " + saved.keySet());
			presence.discard();
			PactSpiritEntity<?> loaded = PactSpirits.get(d.id()).type().create(level, EntitySpawnReason.LOAD);
			loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
			helper.assertTrue(helper.absolutePos(at).equals(loaded.perch()) && presence.record().equals(loaded.record()) && keeper.getUUID().equals(loaded.owner())
					&& loaded.think(level, 0L, g0 + 20) == Status.IDLE, d.id() + ": loaded with its original discarded, it is the presence and serves on");
			PactKeeping.tick(level, perch.holder(), perch.getBlockPos(), g0 + 40);
			PactSpiritEntity<?> another = presenceAt(level, perch);
			helper.assertTrue(another != null && !another.getUUID().equals(loaded.getUUID()) && loaded.think(level, 0L, g0 + 60) == Status.FINISHED
					&& loaded.gone(), d.id() + ": another called in its place (the original struck off), the loaded one departs");
		}
		helper.succeed();
	}

	// ---------------------------------------------------------------- Concordance garden cohort, GC-02: a spirit faces whoever speaks to it

	/** GC-P8: the head within this of its bearing once turned (vanilla's 10° a tick; tools/concordance_pacts.py FACING_HEAD_TOLERANCE). */
	static final float HEAD_TOLERANCE = 10.0F;
	/** GC-P8: the body within this of its bearing once vanilla's body control has followed (FACING_BODY_TOLERANCE). */
	static final float BODY_TOLERANCE = 15.0F;
	/** A spirit's yaw facing a speaker on each side of its perch (Minecraft's: 0 south, 90 west, 180 north, -90 east). */
	static final float SOUTH = 0.0F;
	static final float WEST = 90.0F;
	static final float NORTH = 180.0F;
	static final float EAST = -90.0F;

	/** {@code player} stands two blocks {@code side} of the perch at {@code at} (relative), level with it. */
	static void standBy(GameTestHelper helper, ServerPlayer player, BlockPos at, Direction side) {
		Vec3 to = helper.absoluteVec(new Vec3(at.getX() + 0.5 + 2 * side.getStepX(), at.getY(), at.getZ() + 0.5 + 2 * side.getStepZ()));
		player.snapTo(to.x, to.y, to.z, 0.0F, 0.0F);
	}

	static boolean within(float yaw, float expected, float tolerance) {
		return Mth.degreesDifferenceAbs(yaw, expected) <= tolerance;
	}

	static String yaws(LivingEntity spirit) {
		return "yaw " + spirit.getYRot() + ", head " + spirit.getYHeadRot() + ", body " + spirit.yBodyRot;
	}

	/** {@code spirit} attends {@code who} (its brain's look target is that entity). */
	static boolean attends(WorkerEntity<?> spirit, Entity who) {
		return spirit.attending() instanceof EntityTracker tracked && tracked.getEntity() == who;
	}

	/**
	 * GC-02 (GC-S6): summoned, a spirit arrives facing whoever it comes for, yaw, head and body alike, before any tick: an
	 * invitation's visitor faces its inviter (west here), and a presence called back faces its holder (north here).
	 */
	@GameTest(maxTicks = 20)
	public void aSpiritArrivesFacingItsInviter(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer guest = ready(level, TestPlayers.survivor(helper));
		BlockPos west = new BlockPos(2, 1, 4);
		PactPerchBlockEntity perch = perch(helper, west, guest);
		standBy(helper, guest, west, Direction.WEST);
		open(helper, guest, perch);
		StolasEntity visitor = (StolasEntity) level.getEntity(perch.visitor());
		helper.assertTrue(visitor != null && within(visitor.getYRot(), WEST, 1.0F) && within(visitor.getYHeadRot(), WEST, 1.0F)
				&& within(visitor.yBodyRot, WEST, 1.0F), "Invited from the west, he arrives facing west: " + (visitor == null ? "none" : yaws(visitor)));
		guest.closeContainer();
		ServerPlayer keeper = ready(level, TestPlayers.survivor(helper));
		BlockPos north = new BlockPos(5, 1, 2);
		long now = level.getGameTime();
		PactPerchBlockEntity home = stolasPact(helper, keeper, north, now);
		StolasEntity first = stolas(level, home);
		helper.assertTrue(first != null && within(first.yBodyRot, SOUTH, 1.0F), "Sealed with his holder south of the perch, he faces south: "
				+ (first == null ? "none" : yaws(first)));
		first.discard();
		standBy(helper, keeper, north, Direction.NORTH);
		PactKeeping.tick(level, home.holder(), home.getBlockPos(), now + 20);
		StolasEntity back = stolas(level, home);
		helper.assertTrue(back != null && !back.getUUID().equals(first.getUUID()) && within(back.getYRot(), NORTH, 1.0F)
				&& within(back.getYHeadRot(), NORTH, 1.0F) && within(back.yBodyRot, NORTH, 1.0F),
				"Called back with his holder north of the perch, he arrives facing north: " + (back == null ? "none" : yaws(back)));
		helper.succeed();
	}

	/**
	 * GC-02 (GC-S6, GC-P8, GB21): while a conversation is open at his perch he attends whoever opened it. SmartBrainLib's
	 * LookAtTarget starts under WorkerEntity.unseen (its own test refused every entity target), so within five ticks his
	 * head has turned toward his speaker; within 40 it faces her, and within 60 his body does too. His holder, south when
	 * the pact was sealed, speaks from the east.
	 */
	@GameTest(maxTicks = 80)
	public void aSpiritFacesWhoeverSpeaksToIt(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = ready(level, TestPlayers.survivor(helper));
		BlockPos at = new BlockPos(3, 1, 3);
		PactPerchBlockEntity perch = stolasPact(helper, keeper, at, level.getGameTime());
		StolasEntity stolas = stolas(level, perch);
		helper.assertTrue(stolas != null && within(stolas.yBodyRot, SOUTH, 1.0F) && stolas.attending() == null,
				"Sealed with his holder south of the perch, he faces south and attends no one");
		standBy(helper, keeper, at, Direction.EAST);
		open(helper, keeper, perch);
		helper.assertTrue(attends(stolas, keeper) && within(stolas.getYRot(), EAST, 1.0F),
				"The screen opened, he attends its speaker at once, his yaw on her bearing: " + yaws(stolas));
		helper.runAfterDelay(5, () -> helper.assertTrue(attends(stolas, keeper) && within(stolas.getYHeadRot(), EAST, 60.0F),
				"Five ticks on the look target is kept and his head has turned at least 30° toward her (GB21): " + yaws(stolas)));
		helper.runAfterDelay(40, () -> helper.assertTrue(within(stolas.getYHeadRot(), EAST, HEAD_TOLERANCE),
				"Within 40 ticks his head faces her: " + yaws(stolas)));
		helper.runAfterDelay(60, () -> {
			helper.assertTrue(within(stolas.yBodyRot, EAST, BODY_TOLERANCE) && stolas.listening() && attends(stolas, keeper),
					"Within 60 ticks his body faces her too, listening: " + yaws(stolas));
			helper.succeed();
		});
	}

	/**
	 * GC-02 (GC-S6): two players at one perch; he attends whoever spoke last. The owner invites him from the south; a second
	 * player opens the screen from the east and he turns to her; the owner sends a request and at his next think he turns
	 * back, head then body.
	 */
	@GameTest(maxTicks = 120)
	public void theLatestSpeakerIsFaced(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer first = ready(level, TestPlayers.survivor(helper));
		ServerPlayer second = ready(level, TestPlayers.survivor(helper));
		BlockPos at = new BlockPos(3, 1, 3);
		PactPerchBlockEntity perch = perch(helper, at, first);
		standBy(helper, first, at, Direction.SOUTH);
		standBy(helper, second, at, Direction.EAST);
		PactMenu one = open(helper, first, perch);
		StolasEntity visitor = (StolasEntity) level.getEntity(perch.visitor());
		helper.assertTrue(visitor != null && within(visitor.yBodyRot, SOUTH, 1.0F) && attends(visitor, first), "Invited from the south, he faces south");
		open(helper, second, perch);
		GlobalPos place = GlobalPos.of(level.dimension(), perch.getBlockPos());
		helper.assertTrue(PactSessions.speaker(level.getServer(), place, level.getGameTime()) == second && attends(visitor, second),
				"A second player opens the screen: she spoke last, and he attends her");
		helper.runAfterDelay(40, () -> {
			helper.assertTrue(within(visitor.getYHeadRot(), EAST, HEAD_TOLERANCE), "He has turned his head to her: " + yaws(visitor));
			send(first, one, PactRequestPayload.CHOOSE, 0);
			helper.assertTrue(PactSessions.speaker(level.getServer(), place, level.getGameTime()) == first, "The owner's request makes him the latest speaker");
			visitor.think(level);
			helper.assertTrue(attends(visitor, first), "At his next think he attends the owner");
		});
		helper.runAfterDelay(80, () -> helper.assertTrue(within(visitor.getYHeadRot(), SOUTH, HEAD_TOLERANCE), "His head is back on the owner: " + yaws(visitor)));
		helper.runAfterDelay(100, () -> {
			helper.assertTrue(within(visitor.yBodyRot, SOUTH, BODY_TOLERANCE), "and his body: " + yaws(visitor));
			first.closeContainer();
			second.closeContainer();
			helper.succeed();
		});
	}

	/**
	 * GC-02: moved off his perch he hops back on it (stay(); teleportTo keeps the yaw) still facing as before: at each think
	 * his yaw is set to the bearing of whoever he attends, or with no one to his body's own facing, so a client drawing the
	 * hop (vanilla's body control turns a moving body to the yaw) never snaps him back to the south.
	 */
	@GameTest(maxTicks = 80)
	public void aTeleportBackKeepsTheFacing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = ready(level, TestPlayers.survivor(helper));
		BlockPos at = new BlockPos(3, 1, 3);
		PactPerchBlockEntity perch = stolasPact(helper, keeper, at, level.getGameTime());
		StolasEntity stolas = stolas(level, perch);
		standBy(helper, keeper, at, Direction.EAST);
		open(helper, keeper, perch);
		helper.runAfterDelay(60, () -> {
			helper.assertTrue(within(stolas.yBodyRot, EAST, BODY_TOLERANCE), "Spoken to from the east, he faces east: " + yaws(stolas));
			Vec3 home = stolas.position();
			stolas.setPos(home.x - 3.0, home.y, home.z);
			stolas.think(level);
			helper.assertTrue(stolas.position().distanceToSqr(home) < 1.0E-6 && within(stolas.getYRot(), EAST, 1.0F)
					&& within(stolas.yBodyRot, EAST, BODY_TOLERANCE), "Moved off, he hops back with his yaw on his speaker: " + yaws(stolas));
			keeper.closeContainer();
			stolas.think(level);
			float facing = stolas.yBodyRot;
			stolas.setPos(home.x, home.y, home.z + 3.0);
			stolas.think(level);
			helper.assertTrue(stolas.position().distanceToSqr(home) < 1.0E-6 && stolas.attending() == null && within(stolas.getYRot(), facing, 1.0F)
					&& within(stolas.yBodyRot, EAST, BODY_TOLERANCE), "Spoken to by no one, he hops back with his yaw on his body's facing: " + yaws(stolas));
			helper.succeed();
		});
	}

	/**
	 * GC-02: when the screen closes he attends no one: his head eases back to his body, which keeps its facing, and he does
	 * not follow his holder, who walks round to the north afterwards.
	 */
	@GameTest(maxTicks = 100)
	public void aClosedConversationLeavesTheFacing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = ready(level, TestPlayers.survivor(helper));
		BlockPos at = new BlockPos(3, 1, 3);
		PactPerchBlockEntity perch = stolasPact(helper, keeper, at, level.getGameTime());
		StolasEntity stolas = stolas(level, perch);
		standBy(helper, keeper, at, Direction.EAST);
		open(helper, keeper, perch);
		helper.runAfterDelay(60, () -> {
			helper.assertTrue(within(stolas.yBodyRot, EAST, BODY_TOLERANCE), "Spoken to from the east, he faces east: " + yaws(stolas));
			keeper.closeContainer();
			stolas.think(level);
			helper.assertTrue(stolas.attending() == null && !stolas.listening(), "The screen closed, he attends no one and listens no more");
			standBy(helper, keeper, at, Direction.NORTH);
		});
		helper.runAfterDelay(90, () -> {
			helper.assertTrue(within(stolas.yBodyRot, EAST, BODY_TOLERANCE) && within(stolas.getYHeadRot(), stolas.yBodyRot, HEAD_TOLERANCE),
					"His head has eased back to his body, which keeps its facing; he did not follow his holder north: " + yaws(stolas));
			helper.succeed();
		});
	}

	/**
	 * GC-02 (GC-P9, GB21): WorkerEntity.unseen keeps a living target in his level within 32 blocks and drops one beyond, gone
	 * or in another level; a block is never dropped. SmartBrainLib's own test would have refused even his near speaker:
	 * nothing of his fills the visible-entities memory.
	 */
	@GameTest(maxTicks = 20)
	public void theLookTargetIsDroppedOnlyWhenGoneElsewhereOrFar(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = ready(level, TestPlayers.survivor(helper));
		PactPerchBlockEntity perch = stolasPact(helper, keeper, new BlockPos(3, 1, 3), level.getGameTime());
		StolasEntity stolas = stolas(level, perch);
		Vec3 home = stolas.position();
		Cow near = EntityTypes.COW.create(level, EntitySpawnReason.MOB_SUMMONED);
		Cow far = EntityTypes.COW.create(level, EntitySpawnReason.MOB_SUMMONED);
		Cow gone = EntityTypes.COW.create(level, EntitySpawnReason.MOB_SUMMONED);
		Cow elsewhere = EntityTypes.COW.create(level.getServer().getLevel(Level.NETHER), EntitySpawnReason.MOB_SUMMONED);
		near.snapTo(home.x + WorkerEntity.LOOK_REACH - 1.0, home.y, home.z, 0.0F, 0.0F);
		far.snapTo(home.x + WorkerEntity.LOOK_REACH + 1.0, home.y, home.z, 0.0F, 0.0F);
		gone.snapTo(home.x + 2.0, home.y, home.z, 0.0F, 0.0F);
		gone.discard();
		elsewhere.snapTo(home.x + 2.0, home.y, home.z, 0.0F, 0.0F);
		helper.assertTrue(!WorkerEntity.unseen(stolas, new EntityTracker(keeper, true)) && !WorkerEntity.unseen(stolas, new EntityTracker(near, true)),
				"A living target in his level within 32 blocks is kept");
		helper.assertTrue(WorkerEntity.unseen(stolas, new EntityTracker(far, true)) && WorkerEntity.unseen(stolas, new EntityTracker(gone, true))
				&& WorkerEntity.unseen(stolas, new EntityTracker(elsewhere, true)), "Beyond 32 blocks, gone, or in another level, it is dropped");
		helper.assertTrue(!WorkerEntity.unseen(stolas, new BlockPosTracker(stolas.blockPosition().offset(100, 0, 0))), "A block is never dropped");
		helper.assertTrue(!new EntityTracker(keeper, true).isVisibleBy(stolas),
				"SmartBrainLib's own test would refuse his near speaker: no sensor of his fills the visible-entities memory (GB21)");
		helper.succeed();
	}

	/**
	 * GC-02 (GC-P8): a work target outranks the speaker while its work gesture plays; then the speaker again; with nobody
	 * speaking, the work while it plays and then no one. Stolas has no work of his own (Star-reckoning takes no step), so the
	 * hook a garden service calls (attendWork, GC-04 onward) is called here, for a bed three blocks east of his perch and a
	 * work gesture of 16 ticks. The window is counted in the think's game time.
	 */
	@GameTest(maxTicks = 20)
	public void aWorkTargetOutranksTheSpeakerWhileItsGesturePlays(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = ready(level, TestPlayers.survivor(helper));
		BlockPos at = new BlockPos(3, 1, 3);
		PactPerchBlockEntity perch = stolasPact(helper, keeper, at, level.getGameTime());
		StolasEntity stolas = stolas(level, perch);
		standBy(helper, keeper, at, Direction.SOUTH);
		open(helper, keeper, perch);
		helper.assertTrue(attends(stolas, keeper) && within(stolas.getYRot(), SOUTH, 1.0F), "Spoken to from the south, he attends his speaker: " + yaws(stolas));
		BlockPos bed = helper.absolutePos(at.east(3));
		int ticks = 16;
		long now = level.getGameTime();
		long time = level.getOverworldClockTime();
		stolas.attendWork(bed, ticks, now);
		stolas.think(level, time, now);
		helper.assertTrue(stolas.attending() instanceof BlockPosTracker tracked && tracked.currentBlockPosition().equals(bed)
				&& within(stolas.getYRot(), EAST, 1.0F) && stolas.listening(),
				"While his work gesture plays he looks at the bed he works, east, over his speaker, still listening: " + yaws(stolas));
		stolas.think(level, time, now + ticks - 1);
		helper.assertTrue(stolas.attending() instanceof BlockPosTracker, "A tick before his gesture ends he still looks at his work: " + yaws(stolas));
		stolas.think(level, time, now + ticks);
		helper.assertTrue(attends(stolas, keeper) && within(stolas.getYRot(), SOUTH, 1.0F),
				"His work gesture over, he turns back to his speaker: " + yaws(stolas));
		keeper.closeContainer();
		stolas.attendWork(bed, ticks, now + ticks);
		stolas.think(level, time, now + ticks + 1);
		helper.assertTrue(stolas.attending() instanceof BlockPosTracker tracked && tracked.currentBlockPosition().equals(bed)
				&& within(stolas.getYRot(), EAST, 1.0F) && !stolas.listening(), "Nobody speaking, he still looks at his work while it plays: " + yaws(stolas));
		stolas.think(level, time, now + 2L * ticks);
		helper.assertTrue(stolas.attending() == null && within(stolas.getYRot(), stolas.yBodyRot, 1.0F),
				"His work over and nobody speaking, he attends no one, his yaw on his body's facing: " + yaws(stolas));
		helper.succeed();
	}

	// ---------------------------------------------------------------- Concordance garden cohort, GC-03: the groundwork garden pacts share

	/**
	 * GC-03 (GC-S7, GC-P34): whose land a garden service may touch, asked of a record alone. Only where its pact serves now (its
	 * area, its hours, its day's quota; neither suspended nor held for upkeep; seated, in this level); only for a beneficiary at
	 * that moment (its holder, and their party only under party and only while they are one); only while its holder is online
	 * and within Pacts.REACH of the perch, never a stand-in whatever the absent-owner option; and only where the holder could
	 * change the block by hand. Each refusal is a status.
	 */
	@GameTest(maxTicks = 20)
	public void aGardenPactActsOnlyForABeneficiaryWhileItsHolderIsNear(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = TestPlayers.survivor(helper);
		ServerPlayer member = TestPlayers.survivor(helper);
		UUID stranger = new UUID(0x6C03L, 9L);
		BlockPos perchAt = new BlockPos(3, 1, 3);
		BlockPos perch = helper.absolutePos(perchAt);
		BlockPos own = helper.absolutePos(new BlockPos(1, 1, 1));
		BlockPos far = perch.east(9);
		long now = dayStart(level, 1);
		long noon = 6000L;
		long night = 13000L;
		BoundWill record = new BoundWill(UUID.randomUUID(), "jugcraft:test_pact", "jugcraft:test_spirit", holder.getUUID(), now, false,
				Pacts.sealed(Couriers.place(level, perch), Pacts.PARTY, null, new BoundWill.Service("strain_tending", 8, 0, 12000, 16), null, now));
		beside(helper, holder, perchAt);
		helper.assertTrue(GardenPacts.target(level, record, own, holder.getUUID(), noon, now) == null,
				"Its holder's own bed, by day, in its area, the holder near: it may");
		helper.assertTrue(GardenPacts.target(level, record, own, stranger, noon, now) == Status.IDLE
				&& GardenPacts.target(level, record, own, null, noon, now) == Status.IDLE
				&& GardenPacts.target(level, record, own, member.getUUID(), noon, now) == Status.IDLE,
				"A stranger's bed, nobody's and someone's outside the holder's party: skipped");
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(holder.getUUID(), member.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(member.getUUID(), ms) == PartyManager.Result.OK, "The holder and a member form a party");
			helper.assertTrue(GardenPacts.target(level, record, own, member.getUUID(), noon, now) == null, "Under party, a member's bed: it may");
			BoundWill personal = record.with(Pacts.sealed(record.pact().place(), Pacts.PERSONAL, null, record.pact().service(), null, now));
			helper.assertTrue(GardenPacts.target(level, personal, own, member.getUUID(), noon, now) == Status.IDLE,
					"A personal pact skips even a member's bed");
		} finally {
			parties.adminDisband(holder.getUUID());
		}
		helper.assertTrue(GardenPacts.target(level, record, own, member.getUUID(), noon, now) == Status.IDLE,
				"The party gone, the member's bed is skipped again");
		helper.assertTrue(GardenPacts.target(level, record, far, holder.getUUID(), noon, now) == Status.OUTSIDE_AGREEMENT, "Outside its area");
		helper.assertTrue(GardenPacts.target(level, record, own, holder.getUUID(), night, now) == Status.OUTSIDE_AGREEMENT, "Outside its hours");
		helper.assertTrue(GardenPacts.target(level, record.with(record.pact().counted(Math.floorDiv(now, Pacts.DAY), 16)), own, holder.getUUID(), noon, now)
				== Status.FINISHED, "Its day's quota spent");
		helper.assertTrue(GardenPacts.target(level, Pacts.suspend(record, now), own, holder.getUUID(), noon, now) == Status.SUSPENDED, "Suspended by its holder");
		helper.assertTrue(GardenPacts.target(level, record.with(record.pact().held(BoundWill.Hold.UPKEEP, now)), own, holder.getUUID(), noon, now)
				== Status.UPKEEP_HELD, "Held for its upkeep");
		helper.assertTrue(GardenPacts.target(level, Pacts.unseat(record), own, holder.getUUID(), noon, now) == Status.OUTSIDE_AGREEMENT, "Without its perch");
		helper.assertTrue(GardenPacts.target(level, record.with(record.pact().at(new Place("minecraft:the_nether", perch.getX(), perch.getY(), perch.getZ()))),
				own, holder.getUUID(), noon, now) == Status.OUTSIDE_AGREEMENT, "Seated in another level");
		holder.snapTo(perch.getX() + 100.5, perch.getY(), perch.getZ() + 0.5, 0.0F, 0.0F);
		helper.assertTrue(GardenPacts.target(level, record, own, holder.getUUID(), noon, now) == Status.OWNER_OFFLINE,
				"Its holder far from the perch: it waits");
		boolean answering = JugcraftConfig.option(Authority.ABSENT_OPTION);
		JugcraftConfig.setOption(Authority.ABSENT_OPTION, true);
		try {
			helper.assertTrue(GardenPacts.target(level, record, own, holder.getUUID(), noon, now) == Status.OWNER_OFFLINE,
					"whatever the absent-owner option: no stand-in answers for a garden pact");
		} finally {
			JugcraftConfig.setOption(Authority.ABSENT_OPTION, answering);
		}
		beside(helper, holder, perchAt);
		try (TestClaims claims = TestClaims.open()) {
			claims.block(own, stranger);
			helper.assertTrue(GardenPacts.target(level, record, own, holder.getUUID(), noon, now) == Status.BLOCKED_BY_ACCESS,
					"A block its holder could not change by hand: blocked by access");
		}
		helper.assertTrue(GardenPacts.target(level, record, own, holder.getUUID(), noon, now) == null, "Its claim lifted, it may again");
		TestPlayers.leave(holder);
		helper.assertTrue(GardenPacts.target(level, record, own, holder.getUUID(), noon, now) == Status.OWNER_OFFLINE, "Its holder gone: it waits");
		helper.succeed();
	}

	/**
	 * GC-03 (R10): talking teaches nothing, with every spirit a perch offers. Invited, its owner turns to the terms (CHOOSE: the
	 * state alone, no result) and declines; their knowledge is exactly as it was, and no evidence at all was recorded for them.
	 */
	@GameTest(maxTicks = 20)
	public void talkingTeachesNothing(GameTestHelper helper) {
		List<PactDefinition> all = everySpirit(helper);
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			BlockPos at = perchFor(i);
			ServerPlayer player = invitee(helper, d, at, 1);
			PactPerchBlockEntity perch = perch(helper, at, player);
			beside(helper, player, at);
			Knowledge before = ConcordanceProgress.knowledge(player);
			int heard = HEARD.getOrDefault(player.getUUID(), 0);
			PactMenu menu = offerOf(helper, player, perch, d);
			PactStatePayload terms = send(player, menu, PactRequestPayload.CHOOSE, 0);
			helper.assertTrue(terms != null && terms.result().isEmpty(), d.id() + ": turning to the terms answers with the state alone");
			helper.assertTrue(answered(send(player, menu, PactRequestPayload.DECLINE, 0), PactMenu.DECLINED), d.id() + ": declined");
			helper.assertTrue(ConcordanceProgress.knowledge(player).equals(before) && HEARD.getOrDefault(player.getUUID(), 0) == heard,
					d.id() + ": talking taught nothing and recorded no evidence (" + (HEARD.getOrDefault(player.getUUID(), 0) - heard) + " heard)");
		}
		helper.succeed();
	}

	// ---------------------------------------------------------------- Concordance garden cohort, GC-04: Buer, strain tending

	/** GC-04: his service's words (tools/concordance_pacts.py SERVICE_WORDS). */
	static final String TENDING_KEY = "message.jugcraft.concordance.pact.strain_tending.";
	/** Outside his hours by the world's clock (0 to 12,000 ticks: 06:00 to 18:00); inside them, NOON (above). */
	static final long MIDNIGHT = 18_000L;
	/** A stranger to every pact here. */
	static final UUID STRANGER = new UUID(0x6C04L, 9L);

	static {
		// GC-04 (SG12): Buer comes where an awake bed the inviter keeps lies within 8 of the perch: one in the floor beside the
		// perch's grid cell (perchFor leaves x - 1 free), so every shared case runs for him with no edit.
		FIXTURES.put(BuerEntity.COUNTERPART, (helper, player, perch) -> {
			BlockPos at = perch.offset(-1, -1, 0);
			keptBed(helper, at, player.getUUID(), 0);
			return List.of(at);
		});
	}

	/** Buer's offer as the server loaded it (data/jugcraft/concordance/pact/buer.json). */
	static PactDefinition buerOffer() {
		PactDefinition offer = PactPerchBlockEntity.definition(BuerEntity.COUNTERPART);
		if (offer == null) {
			throw new AssertionError("Buer's offer is loaded");
		}
		return offer;
	}

	/**
	 * A survival player who understands The Garden's Regard (so the Binding Arts and Verdant Husbandry: a Greenwarden), carrying
	 * nothing. A test survivor joins in the test server's default creative mode, so join listeners hand it welcome items (the
	 * Creative Quick Start, in its main hand, where an empty-hand use would destroy it); each test sets what its player carries.
	 */
	static ServerPlayer gardenHolder(GameTestHelper helper) {
		ServerPlayer player = TestPlayers.survivor(helper);
		understand(player, GardenPacts.RESEARCH);
		RateGate.forget(player.getUUID());
		player.getInventory().clearContent();
		return player;
	}

	/** An awake, wet Verdant Bed at {@code at} kept by {@code keeper}, at {@code strain}, holding 4 nutrients (room for compost). */
	static VerdantBedBlockEntity keptBed(GameTestHelper helper, BlockPos at, UUID keeper, int strain) {
		helper.setBlock(at, Garden.VERDANT_BED.defaultBlockState().setValue(VerdantBedBlock.MOISTURE, VerdantBedBlock.WET));
		VerdantBedBlockEntity bed = helper.getBlockEntity(at, VerdantBedBlockEntity.class);
		bed.awaken(keeper);
		bed.setNutrients(4);
		bed.setStrain(strain);
		return bed;
	}

	/** Buer's pact sealed for {@code holder} at a perch they placed at {@code at}, at game time {@code now}; they stand beside it. */
	static PactPerchBlockEntity buerPact(GameTestHelper helper, ServerPlayer holder, BlockPos at, long now) {
		PactPerchBlockEntity perch = perch(helper, at, holder);
		beside(helper, holder, at);
		helper.assertValueEqual(perch.holder().seal(helper.getLevel(), holder, PactPerchBlockEntity.sealing(buerOffer(), null, null), now).outcome(),
				Pacts.Outcome.DONE, "Buer's pact is sealed at the perch");
		return perch;
	}

	/** The Buer kept at {@code perch} (its presence), asserted there. */
	static BuerEntity buer(GameTestHelper helper, PactPerchBlockEntity perch) {
		PactSpiritEntity<?> found = perch.presence(helper.getLevel());
		helper.assertTrue(found instanceof BuerEntity, "Buer is on his perch: " + found);
		return (BuerEntity) found;
	}

	static StrainTending.Memory memory(BuerEntity buer) {
		return buer.serviceMemory(StrainTending.Memory.class, StrainTending.Memory::new);
	}

	/** Every loose item in the test's bounds (none should appear: he moves no item). */
	static int loose(GameTestHelper helper) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds(), Entity::isAlive).size();
	}

	/** How many items {@code player} carries, all kinds. */
	static int carried(ServerPlayer player) {
		int count = 0;
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			count += player.getInventory().getItem(slot).getCount();
		}
		return count;
	}

	/** GC-04: one strain eased, at the bed's own rate (the window a hand shares), nothing else moved; counted on the record. */
	@GameTest(maxTicks = 20)
	public void buerEasesOneStrainAtTheBedsOwnRate(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long g0 = level.getGameTime();
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), g0);
		BuerEntity buer = buer(helper, perch);
		BlockPos bedAt = new BlockPos(4, 1, 4);
		VerdantBedBlockEntity bed = keptBed(helper, bedAt, keeper.getUUID(), 3);
		int nutrients = bed.nutrients(), items = carried(keeper);
		helper.assertValueEqual(buer.think(level, NOON, g0), Status.WORKING, "By day he eases the strained bed");
		helper.assertTrue(bed.strain() == 2 && bed.nutrients() == nutrients && carried(keeper) == items && loose(helper) == 0,
				"One strain eased, 3 to 2; no nutrient and no item moved");
		helper.assertTrue(Pacts.done(pactOf(level, perch).pact(), g0) == 1 && "work".equals(buer.cues().getLast())
				&& memory(buer).easedToday().equals(List.of(bed.getBlockPos())), "Counted once on the record; his work cue; he remembers it");
		helper.assertValueEqual(buer.think(level, NOON, g0 + Garden.TEND_TICKS - 1), Status.IDLE, "Within the bed's 600 ticks he waits");
		helper.assertTrue(bed.strain() == 2 && !bed.mayTendAt(g0), "The bed's own rule holds it for anyone");
		ConcordanceGardenGameTests.handUse(helper, keeper, bedAt, ItemStack.EMPTY, false);
		helper.assertTrue(bed.strain() == 2, "A Greenwarden's empty hand in his window is told it was tended lately: nothing eased");
		helper.assertValueEqual(buer.think(level, NOON, g0 + Garden.TEND_TICKS), Status.WORKING, "600 ticks on, he eases it again");
		// The quota counts game days: a day boundary between g0 and g0 + 600 (real game time) leaves one in the new day.
		int counted = Math.floorDiv(g0, Pacts.DAY) == Math.floorDiv(g0 + Garden.TEND_TICKS, Pacts.DAY) ? 2 : 1;
		helper.assertTrue(bed.strain() == 1 && bed.nutrients() == nutrients && carried(keeper) == items && loose(helper) == 0
				&& Pacts.done(pactOf(level, perch).pact(), g0 + Garden.TEND_TICKS) == counted, "2 to 1, still nothing else moved, counted again");
		helper.succeed();
	}

	/** GC-04 (GC-S7): a stranger's bed never; a party member's under party while in it (from the index's next refresh), never after. */
	@GameTest(maxTicks = 20)
	public void buerTendsOnlyBedsOfHisBeneficiaries(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		ServerPlayer member = TestPlayers.survivor(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), now);
		BuerEntity buer = buer(helper, perch);
		VerdantBedBlockEntity strangers = keptBed(helper, new BlockPos(3, 1, 5), STRANGER, 3);
		VerdantBedBlockEntity members = keptBed(helper, new BlockPos(5, 1, 3), member.getUUID(), 2);
		helper.assertValueEqual(buer.think(level, NOON, now), Status.IDLE, "No bed here is a beneficiary's: he waits");
		helper.assertTrue(strangers.strain() == 3 && members.strain() == 2, "A stranger's and a non-member's beds are untouched");
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(keeper.getUUID(), member.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(member.getUUID(), ms) == PartyManager.Result.OK, "The holder and a member form a party");
			long refreshed = now + GardenReach.REFRESH_TICKS;
			helper.assertValueEqual(buer.think(level, NOON, refreshed), Status.WORKING, "Under party, the member's bed is his to mend");
			helper.assertTrue(members.strain() == 1 && strangers.strain() == 3, "The member's bed eased once; the stranger's untouched");
			helper.assertTrue(parties.leave(member.getUUID()) == PartyManager.Result.OK, "The member leaves the party");
			members.setStrain(2);
			helper.assertValueEqual(buer.think(level, NOON, refreshed + Garden.TEND_TICKS), Status.IDLE,
					"Their bed is refused at once (asked at each change), indexed or not");
			helper.assertTrue(members.strain() == 2 && strangers.strain() == 3, "Nothing more eased");
		} finally {
			parties.adminDisband(keeper.getUUID());
		}
		helper.succeed();
	}

	/**
	 * GC-04 (GC-S7): a bed of the holder's that the holder could not change by hand (a claim), and a dormant bed, untouched.
	 * The claim lifted, the bed is his to mend from his index's next read (a bed refused for itself is not asked before).
	 */
	@GameTest(maxTicks = 20)
	public void buerNeverTouchesAClaimedOrDormantBed(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), now);
		BuerEntity buer = buer(helper, perch);
		BlockPos claimedAt = new BlockPos(4, 1, 2);
		VerdantBedBlockEntity claimed = keptBed(helper, claimedAt, keeper.getUUID(), 3);
		helper.setBlock(new BlockPos(2, 1, 4), Garden.VERDANT_BED.defaultBlockState());
		VerdantBedBlockEntity dormant = helper.getBlockEntity(new BlockPos(2, 1, 4), VerdantBedBlockEntity.class);
		dormant.setStrain(3);
		try (TestClaims claims = TestClaims.open().block(helper.absolutePos(claimedAt), STRANGER)) {
			helper.assertValueEqual(buer.think(level, NOON, now), Status.IDLE, "A claimed bed and a dormant one: he waits");
			helper.assertTrue(claimed.strain() == 3 && dormant.strain() == 3 && !dormant.awake(), "Neither is touched");
		}
		helper.assertTrue(memory(buer).skipped(claimed.getBlockPos()), "Refused for itself, the claimed bed is skipped until his index is read again");
		helper.assertValueEqual(buer.think(level, NOON, now + GardenReach.REFRESH_TICKS), Status.WORKING,
				"Unclaimed, the holder's bed is his to mend once his index is read again");
		helper.assertTrue(claimed.strain() == 2 && dormant.strain() == 3, "The dormant bed stays as it was");
		helper.succeed();
	}

	/**
	 * GC-04 (GC-S7; the unit review): beds he may not change never crowd out those he may. Nine of the holder's beds, nearest
	 * the perch and as strained as any, stand in a claim. His first think asks the eight nearest, is refused each and waits;
	 * his next, inside the same read of his index, skips those eight, is refused the ninth and eases the holder's own bed
	 * beyond them. Each refused bed is asked once a read of the index, not each think; at the next read he asks again, and a
	 * claim lifted by then is his to mend.
	 */
	@GameTest(maxTicks = 20)
	public void buerSkipsBedsHeMayNotChange(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), now);
		BuerEntity buer = buer(helper, perch);
		List<VerdantBedBlockEntity> claimed = new ArrayList<>();
		VerdantBedBlockEntity own = keptBed(helper, new BlockPos(8, 1, 8), keeper.getUUID(), 3);
		try (TestClaims claims = TestClaims.open()) {
			for (int x = 3; x <= 5; x++) {
				for (int z = 3; z <= 5; z++) {
					BlockPos at = new BlockPos(x, 1, z);
					claimed.add(keptBed(helper, at, keeper.getUUID(), 3));
					claims.block(helper.absolutePos(at), STRANGER);
				}
			}
			helper.assertValueEqual(buer.think(level, NOON, now), Status.IDLE, "The eight nearest asked, each in a claim: refused, he waits");
			helper.assertTrue(claimed.stream().filter(bed -> memory(buer).skipped(bed.getBlockPos())).count() == StrainTending.MAX_ASKED
					&& !memory(buer).skipped(own.getBlockPos()), "Those eight are skipped until his index is read again; his own bed is not");
			helper.assertValueEqual(buer.think(level, NOON, now + WorkerEntity.THINK_TICKS), Status.WORKING,
					"His next think skips them and mends the holder's own bed beyond them");
			helper.assertTrue(own.strain() == 2 && claimed.stream().allMatch(bed -> bed.strain() == 3 && memory(buer).skipped(bed.getBlockPos())),
					"The holder's own bed eased once; every claimed bed untouched and skipped");
			helper.assertValueEqual(buer.think(level, NOON, now + GardenReach.REFRESH_TICKS), Status.IDLE,
					"His index read again, he asks the claimed beds again (his own bed waits out its window): refused");
			helper.assertTrue(claimed.stream().filter(bed -> memory(buer).skipped(bed.getBlockPos())).count() == StrainTending.MAX_ASKED,
					"asked once that read, eight skipped again");
		}
		helper.assertValueEqual(buer.think(level, NOON, now + 2L * GardenReach.REFRESH_TICKS), Status.WORKING,
				"The claim lifted, at his index's next read the nearest is his to mend");
		helper.assertTrue(claimed.getFirst().strain() == 2 && own.strain() == 2, "The nearest claimed bed eased once");
		helper.succeed();
	}

	/** GC-04: far, dead, in another level or offline, the holder is not near: he waits (owner_offline), absent-owner option or not. */
	@GameTest(maxTicks = 20)
	public void buerWaitsForAPresentHolder(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = buerPact(helper, keeper, at, now);
		BuerEntity buer = buer(helper, perch);
		VerdantBedBlockEntity bed = keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		BlockPos home = helper.absolutePos(at);
		keeper.snapTo(home.getX() + 100.5, home.getY(), home.getZ() + 0.5, 0.0F, 0.0F);
		helper.assertValueEqual(buer.think(level, NOON, now), Status.OWNER_OFFLINE, "100 blocks off in this level: not near");
		boolean answering = JugcraftConfig.option(Authority.ABSENT_OPTION);
		JugcraftConfig.setOption(Authority.ABSENT_OPTION, true);
		try {
			helper.assertValueEqual(buer.think(level, NOON, now + 20), Status.OWNER_OFFLINE, "With absent owners' devices allowed, still no stand-in");
		} finally {
			JugcraftConfig.setOption(Authority.ABSENT_OPTION, answering);
		}
		beside(helper, keeper, at);
		keeper.setHealth(0.0F);
		helper.assertValueEqual(buer.think(level, NOON, now + 40), Status.OWNER_OFFLINE, "Dead beside the perch: not present");
		keeper.setHealth(keeper.getMaxHealth());
		ServerLevel nether = level.getServer().getLevel(Level.NETHER);
		helper.assertTrue(nether != null, "The test server has a Nether");
		keeper.teleport(new TeleportTransition(nether, new Vec3(0.5, 64.0, 0.5), Vec3.ZERO, 0.0F, 0.0F, TeleportTransition.DO_NOTHING));
		helper.assertValueEqual(buer.think(level, NOON, now + 60), Status.OWNER_OFFLINE, "In another level: not present");
		helper.assertTrue(bed.strain() == 3 && Pacts.done(pactOf(level, perch).pact(), now) == 0, "Nothing was tended or counted meanwhile");
		keeper.teleport(new TeleportTransition(level, helper.absoluteVec(new Vec3(2.5, 1.0, 4.5)), Vec3.ZERO, 0.0F, 0.0F, TeleportTransition.DO_NOTHING));
		helper.assertValueEqual(buer.think(level, NOON, now + 80), Status.WORKING, "Back beside the perch, he mends");
		helper.assertValueEqual(bed.strain(), 2, "one strain");
		TestPlayers.leave(keeper);
		bed.setStrain(3);
		helper.assertValueEqual(buer.think(level, NOON, now + 80 + Garden.TEND_TICKS), Status.OWNER_OFFLINE, "Logged out: he waits");
		helper.assertValueEqual(bed.strain(), 3, "and tends nothing for them");
		helper.succeed();
	}

	/** GC-04: two holders' Buers (a party) over one party bed ease it once a window, as does a hand; each counts only its own. */
	@GameTest(maxTicks = 20)
	public void twoBuersShareOneBedsTendWindow(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer first = gardenHolder(helper);
		ServerPlayer second = gardenHolder(helper);
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(first.getUUID(), second.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(second.getUUID(), ms) == PartyManager.Result.OK, "Two holders in one party");
			long g0 = level.getGameTime();
			PactPerchBlockEntity a = buerPact(helper, first, new BlockPos(2, 1, 2), g0);
			PactPerchBlockEntity b = buerPact(helper, second, new BlockPos(6, 1, 2), g0);
			BuerEntity one = buer(helper, a), other = buer(helper, b);
			BlockPos bedAt = new BlockPos(4, 1, 4);
			VerdantBedBlockEntity bed = keptBed(helper, bedAt, first.getUUID(), 3);
			helper.assertValueEqual(one.think(level, NOON, g0), Status.WORKING, "The first Buer eases the party's bed");
			helper.assertValueEqual(other.think(level, NOON, g0), Status.IDLE, "The second finds it tended lately: the bed's one window");
			helper.assertValueEqual(other.think(level, NOON, g0 + Garden.TEND_TICKS - 1), Status.IDLE, "still inside it");
			ConcordanceGardenGameTests.handUse(helper, second, bedAt, ItemStack.EMPTY, false);
			helper.assertValueEqual(bed.strain(), 2, "Eased once, whoever asked: two spirits and a hand");
			helper.assertValueEqual(other.think(level, NOON, g0 + Garden.TEND_TICKS), Status.WORKING, "The window over, the second eases it");
			helper.assertValueEqual(one.think(level, NOON, g0 + Garden.TEND_TICKS), Status.IDLE, "and the first may not in the same window");
			helper.assertTrue(bed.strain() == 1 && Pacts.done(pactOf(level, a).pact(), g0) == 1
					&& Pacts.done(pactOf(level, b).pact(), g0 + Garden.TEND_TICKS) == 1, "Each pact counted its own one");
		} finally {
			parties.adminDisband(first.getUUID());
		}
		helper.succeed();
	}

	/** GC-04: by night his petals shut and he sleeps; the kept page and Jade say asleep until dawn; at 06:00 he mends. */
	@GameTest(maxTicks = 20)
	public void buerSleepsOutsideHisHours(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), now);
		BuerEntity buer = buer(helper, perch);
		VerdantBedBlockEntity bed = keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		helper.assertValueEqual(buer.think(level, MIDNIGHT, now), Status.OUTSIDE_AGREEMENT, "At midnight he sleeps");
		helper.assertTrue(bed.strain() == 3 && buer.status() == Status.OUTSIDE_AGREEMENT
				&& BuerEntity.loop(buer.status(), false) == BuerEntity.Loop.WAITING, "His petals shut; nothing tended");
		List<Component> rows = StrainTending.rows(level, pactOf(level, perch), MIDNIGHT, now);
		helper.assertTrue(keys(rows).equals(List.of(TENDING_KEY + "row.asleep")) && "06:00".equals(arg(rows.getFirst(), 0))
				&& "18:00".equals(arg(rows.getFirst(), 1)), "The kept page: asleep until dawn, his hours named: " + keys(rows));
		helper.assertTrue(new PactService.Note("asleep", 0).equals(StrainTending.SERVICE.note(level, buer, pactOf(level, perch), MIDNIGHT, now)),
				"Jade's note: asleep");
		helper.assertValueEqual(buer.think(level, 12_000L, now + 20), Status.OUTSIDE_AGREEMENT, "18:00 is past his hours");
		helper.assertValueEqual(buer.think(level, 0L, now + 40), Status.WORKING, "At 06:00 he wakes and mends");
		helper.assertTrue(bed.strain() == 2 && keys(StrainTending.rows(level, pactOf(level, perch), NOON, now + 40))
				.equals(List.of(TENDING_KEY + "row.service")), "One strain; by day his row counts today's eases");
		helper.succeed();
	}

	/**
	 * GC-04: 16 beds eased a game day and no more; his quota spent, he still turns to and listens to whoever speaks once his
	 * last work gesture is over (amendment A-07; the work window is counted in the think's game time); the next day he goes on.
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 20)
	public void buerStopsAtHisQuota(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(8, 1, 8), now);
		BuerEntity buer = buer(helper, perch);
		List<VerdantBedBlockEntity> beds = new ArrayList<>();
		for (int i = 0; i < 17; i++) {
			beds.add(keptBed(helper, new BlockPos(4 + i % 9, 1, 4 + i / 9), keeper.getUUID(), 3));
		}
		for (int k = 0; k < 16; k++) {
			helper.assertValueEqual(buer.think(level, NOON, now + 20L * k), Status.WORKING, "Bed " + k + " of his 16 a day");
		}
		helper.assertValueEqual(buer.think(level, NOON, now + 20L * 16), Status.FINISHED, "His day's quota spent, he stops");
		helper.assertTrue(beds.stream().mapToInt(VerdantBedBlockEntity::strain).sum() == 17 * 3 - 16
				&& beds.stream().filter(bed -> bed.strain() == 3).count() == 1 && Pacts.done(pactOf(level, perch).pact(), now) == 16,
				"Sixteen eased once each, the seventeenth untouched");
		helper.assertValueEqual(buer.think(level, NOON, now + 20L * 16 + BuerEntity.Gesture.WORK.ticks), Status.FINISHED,
				"Past his last work gesture, his quota still spent");
		open(helper, keeper, perch);
		helper.assertTrue(attends(buer, keeper) && BuerEntity.loop(buer.status(), buer.listening()) == BuerEntity.Loop.LISTEN,
				"His quota spent, he still turns to and listens to whoever speaks (GC-P8, GC-P25): " + yaws(buer));
		keeper.closeContainer();
		helper.assertValueEqual(buer.think(level, NOON, dayStart(level, 2)), Status.WORKING, "The next game day he mends again");
		helper.succeed();
	}

	/** GC-04: one bed a think: the most strained first, the nearest among equals; one task a think. */
	@GameTest(maxTicks = 20)
	public void buerChangesAtMostOneBedAThink(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), now);
		BuerEntity buer = buer(helper, perch);
		VerdantBedBlockEntity near = keptBed(helper, new BlockPos(3, 1, 2), keeper.getUUID(), 2);
		VerdantBedBlockEntity far = keptBed(helper, new BlockPos(6, 1, 2), keeper.getUUID(), 3);
		VerdantBedBlockEntity middle = keptBed(helper, new BlockPos(2, 1, 5), keeper.getUUID(), 3);
		helper.assertValueEqual(buer.think(level, NOON, now), Status.WORKING, "A think");
		helper.assertTrue(middle.strain() == 2 && far.strain() == 3 && near.strain() == 2, "The most strained, and of those the nearest");
		helper.assertValueEqual(buer.think(level, NOON, now + 20), Status.WORKING, "The next think");
		helper.assertTrue(far.strain() == 2 && middle.strain() == 2 && near.strain() == 2, "the other at 3");
		helper.assertValueEqual(buer.think(level, NOON, now + 40), Status.WORKING, "The next");
		helper.assertTrue(near.strain() == 1 && far.strain() == 2 && middle.strain() == 2, "the nearest at 2 not tended lately");
		helper.assertTrue(buer.cues().stream().filter("work"::equals).count() == 1 && Pacts.done(pactOf(level, perch).pact(), now) == 3,
				"One task a think; his work cue at most once every CUE_TICKS (the three thinks share one tick here): " + buer.cues());
		helper.succeed();
	}

	/** GC-04: eased today and worked back to 3, a bed is overworked: refused, named, until compost by hand eases it; others go on. */
	@GameTest(maxTicks = 20)
	public void anOverworkedBedWaitsForCompost(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), now);
		BuerEntity buer = buer(helper, perch);
		BlockPos bedAt = new BlockPos(4, 1, 4);
		VerdantBedBlockEntity bed = keptBed(helper, bedAt, keeper.getUUID(), 3);
		VerdantBedBlockEntity other = keptBed(helper, new BlockPos(5, 1, 2), keeper.getUUID(), 3);
		helper.assertValueEqual(buer.think(level, NOON, now), Status.WORKING, "He eases the nearer of two at 3");
		helper.assertTrue(bed.strain() == 2 && other.strain() == 3, "the nearer");
		// Worked back to its worst (a harvest's own counting is Chunk C's to test; the tests' setter stands in for it).
		bed.setStrain(3);
		helper.assertValueEqual(buer.think(level, NOON, now + Garden.TEND_TICKS), Status.WORKING, "He goes on with the other bed");
		helper.assertTrue(bed.strain() == 3 && other.strain() == 2 && memory(buer).overworked(bed.getBlockPos()),
				"and refuses the overworked one, though its window is over");
		BoundWill record = pactOf(level, perch);
		List<Component> rows = StrainTending.rows(level, record, NOON, now + Garden.TEND_TICKS);
		helper.assertTrue(keys(rows).equals(List.of(TENDING_KEY + "row.service", TENDING_KEY + "row.overworked"))
				&& bed.getBlockPos().toShortString().equals(arg(rows.get(1), 0)), "The kept page names it and its remedies: " + keys(rows));
		helper.assertTrue(new PactService.Note("overworked", 1).equals(StrainTending.SERVICE.note(level, buer, record, NOON, now)),
				"Jade's note: one overworked bed");
		ConcordanceGardenGameTests.handUse(helper, keeper, bedAt, new ItemStack(Items.BONE_MEAL), false);
		helper.assertTrue(bed.strain() == 2 && bed.nutrients() == 4 + Garden.BONE_MEAL_NUTRIENTS, "Bone meal by hand: compost eases one strain by the bed's own rule");
		helper.assertValueEqual(buer.think(level, NOON, now + 2L * Garden.TEND_TICKS), Status.WORKING, "At 2 he takes it back");
		helper.assertTrue(bed.strain() == 1 && !memory(buer).overworked(bed.getBlockPos())
				&& keys(StrainTending.rows(level, pactOf(level, perch), NOON, now)).equals(List.of(TENDING_KEY + "row.service")),
				"Mended again; no complaint left");
		helper.succeed();
	}

	/** GC-04: his memory is never saved: a reloaded Buer has no complaint, and nothing is owed. */
	@GameTest(maxTicks = 20)
	public void anOverworkedComplaintIsForgottenOnRestart(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), now);
		BuerEntity buer = buer(helper, perch);
		VerdantBedBlockEntity bed = keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		buer.think(level, NOON, now);
		bed.setStrain(3);
		helper.assertValueEqual(buer.think(level, NOON, now + Garden.TEND_TICKS), Status.IDLE, "Overworked: refused");
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		buer.saveWithoutId(saved);
		CompoundTag tag = saved.buildResult();
		helper.assertTrue(tag.keySet().stream().noneMatch(key -> key.contains("eased") || key.contains("overworked") || key.contains("memory"))
				&& tag.contains("perch") && tag.contains("record"), "His save holds his perch and record, never his memory: " + tag.keySet());
		buer.discard();
		BuerEntity back = Workers.BUER.create(level, EntitySpawnReason.LOAD);
		back.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
		helper.assertTrue(memory(back).remembered() == 0, "Reloaded, he remembers nothing");
		helper.assertValueEqual(back.think(level, NOON, now + Garden.TEND_TICKS + 20), Status.WORKING, "and mends the bed again: no debt");
		helper.assertValueEqual(bed.strain(), 2, "one strain");
		helper.succeed();
	}

	/** GC-04: no awake bed of the inviter's within 8 (a stranger's, a dormant one and one 9 off do not count): he does not come; nothing spent. */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 20)
	public void buerRefusesWithoutABedToMend(GameTestHelper helper) {
		// His words are read as the server renders them; without the mod's words the check would say nothing (GC-P29).
		helper.assertTrue(Language.getInstance().has(TENDING_KEY + "missing"), "the server reads the mod's words (Fabric's server translations)");
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		pocketFor(keeper, buerOffer(), 1);
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = perch(helper, at, keeper);
		beside(helper, keeper, at);
		keptBed(helper, new BlockPos(4, 1, 4), STRANGER, 0);
		keptBed(helper, new BlockPos(11, 1, 2), keeper.getUUID(), 0);
		helper.setBlock(new BlockPos(3, 1, 4), Garden.VERDANT_BED.defaultBlockState());
		// GC-05 (GC-P59): Buer is chosen from the chooser (he and Bathin share The Garden's Regard); not ready, he does not come.
		PactMenu menu = offerOf(helper, keeper, perch, buerOffer());
		PactStatePayload state = menu.lastSent();
		Component note = state.notes().stream().filter(line -> key(line).equals(TENDING_KEY + "missing")).findFirst().orElse(null);
		helper.assertTrue(state.mode() == PactStatePayload.NEGOTIATE && note != null && Integer.valueOf(8).equals(arg(note, 0))
				&& perch.visitor() == null, "His offer says why he does not come, and he does not: " + keys(state.notes()));
		PactStatePayload refused = send(keeper, menu, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(answered(refused, StrainTending.NOTHING_TO_MEND) && !refused.result().get().done()
				&& said(refused).equals(TENDING_KEY + "missing") && pocketed(keeper, 1, buerOffer()) && recordOf(level, keeper) == null,
				"Accepting is refused with his reason; nothing spent, nothing held");
		helper.assertTrue(refused.result().get().message().getString().startsWith("\"There is nothing here I could mend.\""),
				"His words: " + refused.result().get().message().getString());
		keeper.closeContainer();
		keptBed(helper, new BlockPos(3, 1, 4), keeper.getUUID(), 0);
		RateGate.forget(keeper.getUUID());
		PactStatePayload invited = offerOf(helper, keeper, perch, buerOffer()).lastSent();
		helper.assertTrue(invited.notes().isEmpty() && invited.offers(PactRequestPayload.ACCEPT)
				&& perch.visitor() != null && level.getEntity(perch.visitor()) instanceof BuerEntity, "With a bed of theirs awake within 8, he comes");
		helper.succeed();
	}

	/** GC-04: his first obligation is the holder's own tending by hand (PactObligations), never his; his kept page says which. */
	@GameTest(maxTicks = 20)
	public void buersFirstObligationIsATendingByHand(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long g0 = level.getGameTime();
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), g0);
		BuerEntity buer = buer(helper, perch);
		keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		helper.assertTrue(StrainTending.SERVICE.obligation() == PactObligations.Act.TEND
				&& keys(StrainTending.SERVICE.obligationRows(level, keeper, pactOf(level, perch), g0))
						.equals(List.of("message.jugcraft.concordance.garden_pact.obligation")), "His page names the act: a tending by hand");
		int heard = GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0);
		helper.assertValueEqual(buer.think(level, NOON, g0), Status.WORKING, "He tends");
		helper.assertTrue(!PactObligations.kept(keeper, BuerEntity.COUNTERPART) && GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == heard,
				"His own tending keeps nothing of the holder's");
		keptBed(helper, new BlockPos(5, 1, 2), keeper.getUUID(), 2);
		ConcordanceGardenGameTests.handUse(helper, keeper, new BlockPos(5, 1, 2), ItemStack.EMPTY, false);
		helper.assertTrue(PactObligations.kept(keeper, BuerEntity.COUNTERPART) && GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == heard + 1
				&& keys(StrainTending.SERVICE.obligationRows(level, keeper, pactOf(level, perch), g0))
						.equals(List.of("message.jugcraft.concordance.garden_pact.obligation_kept")), "Their own tending in his area keeps it, once");
		helper.succeed();
	}

	/**
	 * GC-04 (GC-S6, GC-P8): spoken to from the south, he attends his speaker; his work outranks the speaker while it plays
	 * (his yaw on the bed he chose at once, his head within 40 ticks, his body by 55); his work over, he turns back to his
	 * speaker; the screen closed, he attends no one. His own thinks run between (by night they say he sleeps, and he still
	 * attends whoever speaks: GC-P8).
	 */
	@GameTest(maxTicks = 120)
	public void buerFacesTheBedHeTends(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		BlockPos at = new BlockPos(3, 1, 3);
		long g0 = level.getGameTime();
		PactPerchBlockEntity perch = buerPact(helper, keeper, at, g0);
		BuerEntity buer = buer(helper, perch);
		VerdantBedBlockEntity bed = keptBed(helper, at.east(3), keeper.getUUID(), 3);
		standBy(helper, keeper, at, Direction.SOUTH);
		open(helper, keeper, perch);
		helper.assertTrue(attends(buer, keeper), "Spoken to from the south, he attends his speaker: " + yaws(buer));
		helper.assertValueEqual(buer.think(level, NOON, level.getGameTime()), Status.WORKING, "He tends the bed east of him");
		helper.assertTrue(buer.attending() instanceof BlockPosTracker tracked && tracked.currentBlockPosition().equals(bed.getBlockPos())
				&& within(buer.getYRot(), EAST, 1.0F), "His work outranks his speaker: he attends the bed he chose, his yaw on it: " + yaws(buer));
		helper.runAfterDelay(40, () -> helper.assertTrue(within(buer.getYHeadRot(), EAST, HEAD_TOLERANCE), "His pistil has turned to it: " + yaws(buer)));
		helper.runAfterDelay(55, () -> helper.assertTrue(within(buer.yBodyRot, EAST, BODY_TOLERANCE), "and his body: " + yaws(buer)));
		helper.runAfterDelay(BuerEntity.Gesture.WORK.ticks + WorkerEntity.THINK_TICKS + 5, () -> {
			helper.assertTrue(attends(buer, keeper) && within(buer.getYRot(), SOUTH, 1.0F), "His work over, he turns back to his speaker: " + yaws(buer));
			keeper.closeContainer();
			buer.think(level);
			helper.assertTrue(buer.attending() == null, "The screen closed, he attends no one");
			helper.succeed();
		});
	}

	/** GC-04: suspended by its holder he tends nothing and his page says so by name; resumed, he mends. */
	@GameTest(maxTicks = 20)
	public void aSuspendedBuerTendsNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), now);
		BuerEntity buer = buer(helper, perch);
		VerdantBedBlockEntity bed = keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		helper.assertTrue(PactKeeping.suspend(level, keeper, perch.holder(), now) == AgreementHolder.Toggle.SUSPENDED, "Its holder suspends it");
		helper.assertValueEqual(buer.think(level, NOON, now + 20), Status.SUSPENDED, "Suspended, he waits");
		Component row = StrainTending.SERVICE.suspendedRow(PactKeeping.name(BuerEntity.COUNTERPART));
		helper.assertTrue(bed.strain() == 3 && Pacts.done(pactOf(level, perch).pact(), now) == 0
				&& key(row).equals("message.jugcraft.concordance.pact.row.state.suspended.serves")
				&& BuerEntity.loop(buer.status(), false) == BuerEntity.Loop.SUSPENDED, "Nothing tended or counted; his page and his droop say so");
		helper.assertTrue(PactKeeping.resume(level, keeper, perch.holder(), now + 40) == AgreementHolder.Toggle.RESUMED, "Resumed");
		helper.assertValueEqual(buer.think(level, NOON, now + 60), Status.WORKING, "He mends again");
		helper.assertValueEqual(bed.strain(), 2, "one strain");
		helper.succeed();
	}

	/**
	 * GC-04 (GC-P27): an offering due, he works on through his day's grace (astir), never for an absent holder; past it, held,
	 * nothing. Its holder looks at the pact every 1,000 ticks through the grace (stay), as one present does: a longer gap is an
	 * absence, which would warn again with the grace moved on (Pacts.ABSENT_TICKS).
	 */
	@GameTest(maxTicks = 20)
	public void buerTendsThroughHisGrace(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long g0 = dayStart(level, 1);
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = buerPact(helper, keeper, at, g0);
		BuerEntity buer = buer(helper, perch);
		VerdantBedBlockEntity bed = keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		long due = pactOf(level, perch).pact().upkeep().due();
		helper.assertTrue(stay(level, perch.holder(), g0 + 1_000L, due - 1_000L).isEmpty() && perch.holder().look(level, due) == Pacts.Event.WARNED
				&& Pacts.status(pactOf(level, perch)) == Status.UPKEEP_DUE, "Due with nothing paid ahead: warned, a day's grace");
		long inGrace = due + 1_000L;
		helper.assertValueEqual(buer.think(level, NOON, inGrace), Status.UPKEEP_DUE, "Through his grace he works on, and says an offering is due");
		helper.assertTrue(bed.strain() == 2 && Pacts.done(pactOf(level, perch).pact(), inGrace) == 1
				&& BuerEntity.loop(buer.status(), false) == BuerEntity.Loop.ASTIR, "One strain eased and counted; astir");
		BlockPos home = helper.absolutePos(at);
		keeper.snapTo(home.getX() + 100.5, home.getY(), home.getZ() + 0.5, 0.0F, 0.0F);
		helper.assertValueEqual(buer.think(level, NOON, inGrace + Garden.TEND_TICKS), Status.UPKEEP_DUE, "Its holder away, still due");
		helper.assertValueEqual(bed.strain(), 2, "and nothing tended for one not near (GC-S7: asked at the change)");
		beside(helper, keeper, at);
		helper.assertTrue(stay(level, perch.holder(), due + 1_000L, due + Pacts.DAY - 1_000L).isEmpty()
				&& perch.holder().look(level, due + Pacts.DAY) == Pacts.Event.SUSPENDED, "Unpaid past the grace: held for upkeep");
		helper.assertValueEqual(buer.think(level, NOON, due + Pacts.DAY + 20), Status.UPKEEP_HELD, "Held, he tends nothing");
		helper.assertValueEqual(bed.strain(), 2, "untouched");
		helper.succeed();
	}

	/** GC-04: a second Buer at the perch, or a copy of his save, is no presence: it leaves at once and tends nothing; his own tends once. */
	@GameTest(maxTicks = 20)
	public void aDuplicateBuerTendsNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), now);
		BuerEntity buer = buer(helper, perch);
		VerdantBedBlockEntity bed = keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		BuerEntity stranger = Workers.BUER.create(level, EntitySpawnReason.MOB_SUMMONED);
		stranger.setOwner(keeper.getUUID());
		stranger.setPerch(perch.getBlockPos());
		helper.assertValueEqual(stranger.think(level, NOON, now), Status.FINISHED, "A Buer not the perch's presence leaves");
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		buer.saveWithoutId(saved);
		BuerEntity twin = Workers.BUER.create(level, EntitySpawnReason.LOAD);
		twin.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
		twin.setUUID(UUID.randomUUID());
		helper.assertValueEqual(twin.think(level, NOON, now), Status.FINISHED, "A copy of his save, under another id, leaves");
		helper.assertTrue(bed.strain() == 3 && Pacts.done(pactOf(level, perch).pact(), now) == 0 && stranger.gone() && twin.gone(),
				"Neither tended or counted anything");
		helper.assertValueEqual(buer.think(level, NOON, now), Status.WORKING, "The presence tends, once");
		helper.assertValueEqual(bed.strain(), 2, "one strain");
		helper.succeed();
	}

	/** GC-04: Jade's readings of his perch and of him carry his overworked note and its count, his work, and no player. */
	@GameTest(maxTicks = 20)
	public void buersReadingSaysItsNoteAndNamesNoPlayer(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), now);
		BuerEntity buer = buer(helper, perch);
		VerdantBedBlockEntity bed = keptBed(helper, new BlockPos(4, 1, 4), keeper.getUUID(), 3);
		buer.think(level, NOON, now);
		bed.setStrain(3);
		buer.think(level, NOON, now + Garden.TEND_TICKS);
		CompoundTag ofPerch = PactReading.holder(level, perch, now + Garden.TEND_TICKS);
		CompoundTag ofHim = PactReading.presence(level, buer, now + Garden.TEND_TICKS);
		String both = ofPerch + " " + ofHim;
		for (CompoundTag reading : List.of(ofPerch, ofHim)) {
			helper.assertTrue("kept".equals(reading.getStringOr("standing", "")) && StrainTending.WORK.equals(reading.getStringOr("service", ""))
					&& "overworked".equals(reading.getStringOr("note", "")) && reading.getIntOr("note_count", 0) == 1
					&& PactReading.KEYS.containsAll(reading.keySet()), "The reading: his work and one overworked bed: " + reading);
		}
		helper.assertTrue(BuerEntity.COUNTERPART.equals(ofPerch.getStringOr("counterpart", "")) && !ofHim.contains("counterpart")
				&& !both.contains(keeper.getUUID().toString()) && !both.contains(keeper.getName().getString()), "and names no player: " + both);
		helper.succeed();
	}

	/** GC-04: which gesture answers which action, which loop each state shows, every cue's sound registered. */
	@GameTest(maxTicks = 20)
	public void buerAnswersEachActionWithHisGesture(GameTestHelper helper) {
		helper.assertTrue(BuerEntity.reaction(PactRequestPayload.ACCEPT, PactMenu.SEALED, true) == BuerEntity.Gesture.ASSENT
				&& BuerEntity.reaction(PactRequestPayload.ACCEPT, StrainTending.NOTHING_TO_MEND, false) == BuerEntity.Gesture.REFUSE
				&& BuerEntity.reaction(PactRequestPayload.DEPOSIT, PactMenu.DEPOSITED, true) == BuerEntity.Gesture.INSPECT
				&& BuerEntity.reaction(PactRequestPayload.DEPOSIT, PactMenu.NOTHING_DEPOSITED, false) == null
				&& BuerEntity.reaction(PactRequestPayload.RESUME, "resumed", true) == BuerEntity.Gesture.ASSENT
				&& BuerEntity.reaction(PactRequestPayload.RECALL, "recalled", true) == null
				&& BuerEntity.reaction(PactRequestPayload.ACCEPT, PactMenu.NOT_OFFERED, false) == null, "His answers");
		helper.assertTrue(BuerEntity.loop(Status.WORKING, false) == BuerEntity.Loop.IDLE && BuerEntity.loop(Status.IDLE, true) == BuerEntity.Loop.LISTEN
				&& BuerEntity.loop(Status.OUTSIDE_AGREEMENT, false) == BuerEntity.Loop.WAITING
				&& BuerEntity.loop(Status.UPKEEP_DUE, false) == BuerEntity.Loop.ASTIR && BuerEntity.loop(Status.UPKEEP_HELD, true) == BuerEntity.Loop.LISTEN
				&& BuerEntity.loop(Status.SUSPENDED, false) == BuerEntity.Loop.SUSPENDED && BuerEntity.loop(Status.DISABLED, true) == BuerEntity.Loop.WAITING
				&& BuerEntity.loop(Status.FINISHED, true) == BuerEntity.Loop.LISTEN && BuerEntity.loop(Status.FINISHED, false) == BuerEntity.Loop.WAITING,
				"His loops (A-06: listening over all but switched off; his quota spent, shut only while nobody speaks)");
		for (String cue : BuerEntity.SOUND_IDS) {
			helper.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(Jugcraft.id("buer." + cue)), "jugcraft:buer." + cue + " is registered");
		}
		helper.succeed();
	}

	/** GC-04 (GB23): every offered pact asking no resonance names no astrolabe or resonance in its terms, notes or cannot-afford refusal. */
	@GameTest(maxTicks = 20)
	public void noGardenTermOrRefusalNamesAnAstrolabe(GameTestHelper helper) {
		// The words are read as the server renders them; without the mod's words a key would name neither, so the check would
		// say nothing (GC-P29): the server must have them, and the compact's term must be the one without resonance.
		helper.assertTrue(Language.getInstance().has(PACT_KEY + "term.compact.items"), "the server reads the mod's words (Fabric's server translations)");
		List<PactDefinition> all = everySpirit(helper);
		boolean any = false;
		for (int i = 0; i < all.size(); i++) {
			PactDefinition d = all.get(i);
			if (d.resonance() > 0) {
				continue;
			}
			any = true;
			BlockPos at = perchFor(i);
			ServerPlayer player = invitee(helper, d, at, 0);
			PactPerchBlockEntity perch = perch(helper, at, player);
			beside(helper, player, at);
			PactMenu menu = offerOf(helper, player, perch, d);
			helper.assertTrue(key(term(menu.lastSent(), "compact")).equals(PACT_KEY + "term.compact.items"),
					d.id() + ": its compact's term is the one without resonance");
			List<String> words = new ArrayList<>();
			menu.lastSent().terms().forEach(term -> words.add(term.value().getString()));
			menu.lastSent().notes().forEach(note -> words.add(note.getString()));
			PactStatePayload refused = send(player, menu, PactRequestPayload.ACCEPT, 0);
			helper.assertTrue(answered(refused, Pacts.Outcome.CANNOT_AFFORD.id) && said(refused).equals(PACT_KEY + "answer.cannot_afford.items"),
					d.id() + ": without its compact, refused as cannot_afford (items)");
			words.add(refused.result().get().message().getString());
			for (String line : words) {
				String lower = line.toLowerCase(Locale.ROOT);
				helper.assertTrue(!lower.contains("astrolabe") && !lower.contains("resonance"), d.id() + " names no astrolabe or resonance: " + line);
			}
			player.closeContainer();
		}
		helper.assertTrue(any, "Some offered pact asks no resonance (Buer's)");
		helper.succeed();
	}

	// GC-03's first obligation, shown with Buer's pact (GC-P31, GC-P38): recorded once a player a spirit, never as
	// Stolas's, and only while the pact is active and in its area.

	/**
	 * GC-03 (GC-P31, GC-P38): a tending by hand in Buer's area while his pact is active records his first obligation once
	 * (the Conclave hears it once); a second tending, or one told "tended lately", records nothing more; another holder's own
	 * pact records theirs.
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 40)
	public void anObligationIsRecordedOncePerPlayerAndSpirit(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long g0 = level.getGameTime();
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), g0);
		helper.assertTrue(!PactObligations.kept(keeper, BuerEntity.COUNTERPART) && GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == 0,
				"Before: nothing kept, nothing heard");
		BlockPos first = new BlockPos(4, 1, 4);
		VerdantBedBlockEntity bed = keptBed(helper, first, keeper.getUUID(), 2);
		ConcordanceGardenGameTests.handUse(helper, keeper, first, ItemStack.EMPTY, false);
		Knowledge.Progress progress = ConcordanceProgress.knowledge(keeper).progress(GardenPacts.RESEARCH);
		helper.assertTrue(bed.strain() == 1 && GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == 1
				&& progress.evidence().containsKey(PactObligations.key(BuerEntity.COUNTERPART)) && PactObligations.kept(keeper, BuerEntity.COUNTERPART)
				&& PactObligations.spiritsKept(keeper) == 1 && progress.state() == ResearchState.UNDERSTOOD,
				"A tending by hand in his area keeps his first obligation, once; the Regard stays understood: " + progress);
		helper.assertTrue(keys(PactObligations.rows(keeper, pactOf(level, perch), PactObligations.Act.TEND))
				.equals(List.of("message.jugcraft.concordance.garden_pact.obligation_kept")), "The kept page says it is kept");
		BlockPos second = new BlockPos(5, 1, 4);
		VerdantBedBlockEntity other = keptBed(helper, second, keeper.getUUID(), 2);
		ConcordanceGardenGameTests.handUse(helper, keeper, second, ItemStack.EMPTY, false);
		helper.assertTrue(other.strain() == 1 && GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == 1,
				"A second tending eases its bed and records nothing again");
		ConcordanceGardenGameTests.handUse(helper, keeper, first, ItemStack.EMPTY, false);
		helper.assertTrue(bed.strain() == 1 && GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == 1, "One told it was tended lately records nothing");
		ServerPlayer another = gardenHolder(helper);
		buerPact(helper, another, new BlockPos(10, 1, 10), g0);
		BlockPos theirs = new BlockPos(12, 1, 12);
		keptBed(helper, theirs, another.getUUID(), 2);
		ConcordanceGardenGameTests.handUse(helper, another, theirs, ItemStack.EMPTY, false);
		helper.assertTrue(GARDEN_SERVED.getOrDefault(another.getUUID(), 0) == 1 && GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == 1,
				"Another holder's own tending in their own pact's area records theirs, once");
		helper.succeed();
	}

	/** GC-03 (GB5): a garden obligation kept beside Stolas's pact records only the garden practice: Stolas's Regard is not mastered by it. */
	@GameTest(maxTicks = 40)
	public void anObligationNeverMastersStolassRegard(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		understand(keeper, StolasRegard.RESEARCH);
		long g0 = level.getGameTime();
		buerPact(helper, keeper, new BlockPos(2, 1, 2), g0);
		stolasPact(helper, keeper, new BlockPos(6, 1, 2), g0);
		int served = served(keeper);
		BlockPos at = new BlockPos(4, 1, 4);
		keptBed(helper, at, keeper.getUUID(), 2);
		ConcordanceGardenGameTests.handUse(helper, keeper, at, ItemStack.EMPTY, false);
		Set<String> stolas = ConcordanceProgress.knowledge(keeper).progress(StolasRegard.RESEARCH).evidence().keySet();
		helper.assertTrue(PactObligations.kept(keeper, BuerEntity.COUNTERPART) && regard(keeper) == ResearchState.UNDERSTOOD && served(keeper) == served
				&& stolas.stream().noneMatch(key -> key.startsWith("practice:" + GardenPacts.ACTIVITY + ":")) && !StarReckoning.kept(keeper),
				"The garden key is recorded; Stolas's Regard is untouched and his obligation unkept: " + stolas);
		helper.succeed();
	}

	/**
	 * GC-03 (GC-P31): nothing is recorded for a tending outside the area, while the pact is suspended (by its holder) or held
	 * for its upkeep, or without its perch, nor with the Concordance off, nor for a player without such a pact; resumed, a
	 * tending records it whatever the hour (the obligation reads Pacts.serving, never the hours).
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 40)
	public void anObligationOutsideTheAreaOrWhileSuspendedCountsNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long g0 = level.getGameTime();
		PactPerchBlockEntity perch = buerPact(helper, keeper, new BlockPos(2, 1, 2), g0);
		BlockPos outside = new BlockPos(13, 1, 13);
		VerdantBedBlockEntity far = keptBed(helper, outside, keeper.getUUID(), 2);
		ConcordanceGardenGameTests.handUse(helper, keeper, outside, ItemStack.EMPTY, false);
		helper.assertTrue(far.strain() == 1 && GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == 0 && !PactObligations.kept(keeper, BuerEntity.COUNTERPART),
				"Tended 11 blocks from the perch, outside his area: nothing recorded");
		helper.assertTrue(PactKeeping.suspend(level, keeper, perch.holder(), g0) == AgreementHolder.Toggle.SUSPENDED, "Its holder suspends the pact");
		BlockPos inside = new BlockPos(4, 1, 4);
		VerdantBedBlockEntity near = keptBed(helper, inside, keeper.getUUID(), 2);
		ConcordanceGardenGameTests.handUse(helper, keeper, inside, ItemStack.EMPTY, false);
		helper.assertTrue(near.strain() == 1 && GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == 0, "Suspended: a tending in his area records nothing");
		helper.assertTrue(PactKeeping.resume(level, keeper, perch.holder(), g0) == AgreementHolder.Toggle.RESUMED, "Resumed");
		BlockPos again = new BlockPos(5, 1, 4);
		keptBed(helper, again, keeper.getUUID(), 2);
		ConcordanceGardenGameTests.handUse(helper, keeper, again, ItemStack.EMPTY, false);
		helper.assertTrue(GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == 1 && PactObligations.kept(keeper, BuerEntity.COUNTERPART),
				"Resumed, whatever the hour, a tending records it");
		// A second keeper's pact held for its upkeep.
		ServerPlayer held = gardenHolder(helper);
		BlockPos heldAt = new BlockPos(10, 1, 2);
		long h0 = dayStart(level, 1);
		PactPerchBlockEntity heldPerch = buerPact(helper, held, heldAt, h0);
		long due = pactOf(level, heldPerch).pact().upkeep().due();
		helper.assertTrue(stay(level, heldPerch.holder(), h0 + 1_000L, due - 1_000L).isEmpty() && heldPerch.holder().look(level, due) == Pacts.Event.WARNED
				&& stay(level, heldPerch.holder(), due + 1_000L, due + Pacts.DAY - 1_000L).isEmpty()
				&& heldPerch.holder().look(level, due + Pacts.DAY) == Pacts.Event.SUSPENDED, "Unpaid past its grace: held for upkeep");
		BlockPos heldBed = new BlockPos(12, 1, 2);
		keptBed(helper, heldBed, held.getUUID(), 2);
		ConcordanceGardenGameTests.handUse(helper, held, heldBed, ItemStack.EMPTY, false);
		helper.assertTrue(GARDEN_SERVED.getOrDefault(held.getUUID(), 0) == 0, "Held for upkeep: nothing recorded");
		// A third whose perch was broken while switched off: the pact stays seated there and cannot be kept.
		ServerPlayer stranded = gardenHolder(helper);
		BlockPos strandedAt = new BlockPos(2, 1, 10);
		buerPact(helper, stranded, strandedAt, g0);
		off(() -> helper.setBlock(strandedAt, Blocks.AIR));
		BlockPos strandedBed = new BlockPos(4, 1, 10);
		keptBed(helper, strandedBed, stranded.getUUID(), 2);
		ConcordanceGardenGameTests.handUse(helper, stranded, strandedBed, ItemStack.EMPTY, false);
		helper.assertTrue(GARDEN_SERVED.getOrDefault(stranded.getUUID(), 0) == 0, "Its perch gone: nothing recorded");
		// The Concordance switched off.
		ServerPlayer switched = gardenHolder(helper);
		BlockPos switchedAt = new BlockPos(10, 1, 10);
		buerPact(helper, switched, switchedAt, g0);
		BlockPos switchedBed = new BlockPos(12, 1, 10);
		keptBed(helper, switchedBed, switched.getUUID(), 2);
		off(() -> ConcordanceGardenGameTests.handUse(helper, switched, switchedBed, ItemStack.EMPTY, false));
		helper.assertTrue(GARDEN_SERVED.getOrDefault(switched.getUUID(), 0) == 0, "Switched off: nothing recorded");
		// A player with no pact tending inside Buer's area.
		ServerPlayer passer = gardenHolder(helper);
		BlockPos passerBed = new BlockPos(3, 1, 5);
		keptBed(helper, passerBed, passer.getUUID(), 2);
		ConcordanceGardenGameTests.handUse(helper, passer, passerBed, ItemStack.EMPTY, false);
		helper.assertTrue(GARDEN_SERVED.getOrDefault(passer.getUUID(), 0) == 0 && GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == 1,
				"A player with no pact records nothing, for anyone");
		helper.succeed();
	}

	// GC-01's chooser, journal and roster cases (GC-P10, GC-P16), with a second spirit to choose.

	/** A survival owner ready to invite both Stolas and Buer at a perch at {@code at}: both Regards, a star, a bed within 8, one compact of each. */
	static ServerPlayer chooserOwner(GameTestHelper helper, BlockPos at) {
		ServerPlayer owner = TestPlayers.survivor(helper);
		for (PactDefinition d : List.of(stolasOffer(), buerOffer())) {
			understand(owner, d.research());
			pocketFor(owner, d, 1);
			FIXTURES.get(d.id()).meet(helper, owner, at);
		}
		RateGate.forget(owner.getUUID());
		return owner;
	}

	/**
	 * GC-06b (GC-P77): the offers the chooser lists for {@code player}, worked out here as PactMenu does: each offer the perch
	 * makes (REQUIRED's order) whose research they understand and whose pact they do not hold, at most MAX_CHOICES. So the
	 * chooser cases hold for every garden spirit a unit adds.
	 */
	static List<PactDefinition> invitable(ServerLevel level, ServerPlayer player) {
		return PactPerchBlockEntity.offered().stream()
				.filter(d -> ConcordanceProgress.knowledge(player).state(d.research()).atLeast(ResearchState.UNDERSTOOD)
						&& !Pacts.holds(BoundWills.of(level.getServer()).ledger(), player.getUUID(), d.id()))
				.limit(PactStatePayload.MAX_CHOICES).toList();
	}

	/** GC-06b (GC-P77): {@code offers}' signs, in order (each chooser entry's id). */
	static List<String> signsOf(List<PactDefinition> offers) {
		return offers.stream().map(d -> PactSpirits.sign(d.id())).toList();
	}

	/** GC-06b (GC-P77): whether each of {@code offers} asks nothing more of {@code player} at {@code perch}: its service's own missing list empty. */
	static List<Boolean> readyFor(ServerLevel level, ServerPlayer player, PactPerchBlockEntity perch, List<PactDefinition> offers) {
		return offers.stream().map(d -> PactServices.get(d.work()).missing(level, player, perch, d).isEmpty()).toList();
	}

	/**
	 * GC-01 (SG4, GC-P10): an owner who may invite two spirits is shown the chooser (both, in the perch's order, by sign,
	 * ready); without Buer's bed his entry says what is missing; with one spirit to invite there is no chooser. INVITE(1)
	 * begins Buer's visit; accepting seals his pact and spends nothing of Stolas's compact. GC-05 and GC-06b (GC-P77): every
	 * garden spirit shares The Garden's Regard, so each is listed too, ready exactly as its own service says.
	 */
	@GameTest(maxTicks = 20)
	public void aPerchOffersOnlyWhatThePlayerMayInvite(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PactDefinition stolasD = stolasOffer();
		PactDefinition buerD = buerOffer();
		BlockPos at = perchFor(0);
		ServerPlayer owner = chooserOwner(helper, at);
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu menu = open(helper, owner, perch);
		PactStatePayload chooser = menu.lastSent();
		// GC-05 (GC-P59): Bathin shares The Garden's Regard, so he is listed too (third, REQUIRED's order), not ready: no Heart.
		// GC-06b (GC-P77): so does every later garden spirit, after him, ready exactly as its own service says.
		List<PactDefinition> listed = invitable(level, owner);
		List<Boolean> ready = readyFor(level, owner, perch, listed);
		helper.assertTrue(chooser.mode() == PactStatePayload.INVITE
				&& chooser.patterns().stream().map(PactStatePayload.Choice::id).toList().equals(signsOf(listed))
				&& signsOf(listed).subList(0, 3).equals(List.of(StolasEntity.SIGN, BuerEntity.SIGN, BathinEntity.SIGN))
				&& chooser.patterns().stream().map(PactStatePayload.Choice::ready).toList().equals(ready)
				&& ready.subList(0, 3).equals(List.of(true, true, false))
				&& chooser.patterns().subList(0, 2).stream().allMatch(choice -> choice.amount() == 0)
				&& chooser.notes().size() == ready.stream().filter(r -> !r).count() && note(chooser, "invite.missing") != null
				&& chooser.spirit().equals(PactStatePayload.Spirit.NONE) && key(chooser.speaker()).equals("container.jugcraft.pact_perch")
				&& chooser.actions() == PactStatePayload.mask(PactRequestPayload.CHOOSE, PactRequestPayload.INVITE) && perch.visitor() == null,
				"Both Regards understood, a star and a bed: the chooser, Stolas, Buer, Bathin and every later garden spirit by their "
						+ "signs, Stolas and Buer ready, each one not ready named (Bathin's missing Heart), no visitor: " + chooser);
		// Without Buer's bed his entry says what is still missing.
		BlockPos bare = perchFor(2);
		ServerPlayer bedless = TestPlayers.survivor(helper);
		understand(bedless, stolasD.research());
		understand(bedless, buerD.research());
		FIXTURES.get(stolasD.id()).meet(helper, bedless, bare);
		RateGate.forget(bedless.getUUID());
		PactPerchBlockEntity barePerch = perch(helper, bare, bedless);
		beside(helper, bedless, bare);
		PactStatePayload missing = open(helper, bedless, barePerch).lastSent();
		helper.assertTrue(missing.mode() == PactStatePayload.INVITE && missing.patterns().size() == invitable(level, bedless).size()
				&& missing.patterns().get(0).ready()
				&& !missing.patterns().get(1).ready() && missing.patterns().get(1).amount() == 1 && note(missing, "invite.missing") != null,
				"Without a bed of theirs within 8, Buer is listed not ready, what is missing named: " + missing);
		bedless.closeContainer();
		// One who understands only the Garden's Regard: the chooser with Buer and Bathin, not Stolas (GC-05, GC-P59), and with
		// every later garden spirit after them (GC-06b, GC-P77).
		List<String> garden = signsOf(PactPerchBlockEntity.offered().stream().filter(d -> d.research().equals(GardenPacts.RESEARCH)).toList());
		BlockPos gardenAt = perchFor(3);
		ServerPlayer gardener = invitee(helper, buerD, gardenAt, 0);
		PactPerchBlockEntity gardenPerch = perch(helper, gardenAt, gardener);
		beside(helper, gardener, gardenAt);
		PactStatePayload one = open(helper, gardener, gardenPerch).lastSent();
		helper.assertTrue(one.mode() == PactStatePayload.INVITE && one.patterns().stream().map(PactStatePayload.Choice::id).toList().equals(garden)
				&& garden.subList(0, 2).equals(List.of(BuerEntity.SIGN, BathinEntity.SIGN)) && !garden.contains(StolasEntity.SIGN),
				"Only the Garden's Regard understood: the chooser with every garden spirit, Buer and Bathin first, not Stolas: " + one);
		gardener.closeContainer();
		// One already holding Stolas's pact: one to invite, no chooser.
		BlockPos heldAt = perchFor(4);
		ServerPlayer holding = chooserOwner(helper, heldAt);
		stolasPact(helper, holding, perchFor(5), level.getGameTime());
		PactPerchBlockEntity heldPerch = perch(helper, heldAt, holding);
		beside(helper, holding, heldAt);
		PactStatePayload rest = open(helper, holding, heldPerch).lastSent();
		helper.assertTrue(rest.mode() == PactStatePayload.INVITE && rest.patterns().stream().map(PactStatePayload.Choice::id).toList().equals(garden),
				"Holding Stolas's pact, he is not listed: the chooser with every garden spirit (GC-05, GC-06b): " + rest);
		holding.closeContainer();
		// Back on the chooser: INVITE(1) begins Buer's visit; accepting seals his pact alone.
		PactStatePayload invited = invite(owner, menu, 1);
		helper.assertTrue(answered(invited, PactMenu.INVITED) && invited.result().get().done() && invited.mode() == PactStatePayload.NEGOTIATE
				&& keys(invited.beats()).equals(buerD.beats().stream().map(beat -> PACT_KEY + "buer.beat." + beat).toList())
				&& invited.spirit().sign().equals(BuerEntity.SIGN) && perch.visitor(level) instanceof BuerEntity, "INVITE(1): Buer comes, his beats and sign: " + invited);
		PactStatePayload sealed = send(owner, menu, PactRequestPayload.ACCEPT, 0);
		BoundWill record = recordOf(level, owner);
		helper.assertTrue(answered(sealed, PactMenu.SEALED) && record != null && BuerEntity.COUNTERPART.equals(Pacts.counterpart(record.counterpart()))
				&& pocketed(owner, 0, buerD) && pocketed(owner, 1, stolasD) && resonance(owner) == resonanceFor(stolasD),
				"Accepting seals Buer's pact, his compact spent once, nothing of Stolas's");
		helper.succeed();
	}

	/** GC-01 (SG4): on the chooser, an INVITE past its list is not offered and summons nothing; past the screen's bound it is dropped. */
	@GameTest(maxTicks = 20)
	public void anInviteIndexOutOfRangeIsDropped(GameTestHelper helper) {
		BlockPos at = perchFor(0);
		ServerPlayer owner = chooserOwner(helper, at);
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu menu = open(helper, owner, perch);
		helper.assertValueEqual(menu.lastSent().mode(), PactStatePayload.INVITE, "The chooser");
		// GC-05 (GC-P59): INVITE(2) is Bathin now; one past the list is its size.
		int listed = menu.lastSent().patterns().size();
		PactStatePayload past = invite(owner, menu, listed);
		helper.assertTrue(answered(past, PactMenu.NOT_OFFERED) && past.mode() == PactStatePayload.INVITE && perch.visitor() == null
				&& spiritsOf(helper, buerOffer()).isEmpty() && spiritsOf(helper, stolasOffer()).isEmpty() && spiritsOf(helper, bathinOffer()).isEmpty(),
				"INVITE(" + listed + ") past the " + listed + " listed: not offered, still the chooser, nobody comes");
		helper.assertTrue(request(owner, menu.containerId, menu.lastSequence() + 1, PactRequestPayload.INVITE, PactStatePayload.MAX_CHOICES,
				menu.lastSent().revision()) == null, "INVITE(8), past the screen's bound: dropped before anything");
		helper.succeed();
	}

	/** GC-01 (SG4, GC-P10): a reader who is not the perch's owner never sees the chooser: the visitor's offer, else the first they may invite, each with missing.owner. */
	@GameTest(maxTicks = 20)
	public void aNonOwnerNeverSeesTheChooser(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = perchFor(0);
		ServerPlayer owner = chooserOwner(helper, at);
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu mine = open(helper, owner, perch);
		helper.assertTrue(answered(invite(owner, mine, 1), PactMenu.INVITED) && perch.visitor(level) instanceof BuerEntity, "Its owner invites Buer");
		ServerPlayer other = chooserOwner(helper, perchFor(1));
		beside(helper, other, at);
		PactMenu theirs = open(helper, other, perch);
		PactStatePayload visiting = theirs.lastSent();
		helper.assertTrue(visiting.mode() == PactStatePayload.NEGOTIATE && visiting.spirit().sign().equals(BuerEntity.SIGN)
				&& note(visiting, "missing.owner") != null, "With the owner's Buer waiting: his offer, and only the owner may invite him: " + visiting);
		helper.assertTrue(answered(invite(other, theirs, 0), PactMenu.NOT_OFFERED), "INVITE is not offered to them");
		other.closeContainer();
		helper.assertTrue(answered(send(owner, mine, PactRequestPayload.DECLINE, 0), PactMenu.DECLINED), "Its owner declines");
		PactSpiritEntity<?> guest = perch.visitor(level);
		helper.assertTrue(guest == null || guest.think(level, 0L, level.getGameTime()) == Status.FINISHED, "No conversation waits: the visitor leaves");
		PactStatePayload none = open(helper, other, perch).lastSent();
		helper.assertTrue(none.mode() == PactStatePayload.NEGOTIATE && none.spirit().sign().equals(StolasEntity.SIGN)
				&& note(none, "missing.owner") != null && perch.visitor(level) == null,
				"With none waiting: the first they may invite, Stolas, and only the owner may: " + none);
		helper.assertTrue(answered(invite(other, open(helper, other, perch), 0), PactMenu.NOT_OFFERED), "never the chooser");
		helper.succeed();
	}

	/** GC-01 (SG4): a reload that removes a listed spirit's offer: INVITE at the chooser's revision answers changed, the one left offered. */
	@GameTest(maxTicks = 20)
	public void aStaleChooserRevisionIsRefused(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = perchFor(0);
		ServerPlayer owner = chooserOwner(helper, at);
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu menu = open(helper, owner, perch);
		int revision = menu.lastSent().revision();
		helper.assertValueEqual(menu.lastSent().mode(), PactStatePayload.INVITE, "The chooser at revision " + revision);
		ConcordanceRules was = ConcordanceData.rules();
		try {
			ConcordanceData.replace(was.withPacts(PactCatalog.parse(Map.of(PactDefinition.STOLAS, shipped(level)), was.research().keySet())));
			PactStatePayload stale = request(owner, menu.containerId, menu.lastSequence() + 1, PactRequestPayload.INVITE, 1, revision);
			helper.assertTrue(answered(stale, PactMenu.CHANGED) && stale.revision() != revision && stale.mode() == PactStatePayload.NEGOTIATE
					&& stale.spirit().sign().equals(StolasEntity.SIGN) && perch.visitor() == null,
					"Buer's offer gone, INVITE at the old revision: changed, Stolas's offer alone, no chooser: " + stale);
		} finally {
			ConcordanceData.replace(was);
		}
		helper.succeed();
	}

	/**
	 * GC-01 (SG4; the unit review): a visitor still awaited from a visit its owner declined, the conversation reopened before
	 * its next think, leaves once its owner, back on the chooser, invites a spirit that cannot come yet, so no spirit waits on
	 * the perch while another's terms are shown.
	 */
	@GameTest(maxTicks = 20)
	public void aVisitorOfAnotherSpiritLeavesWhenTheChosenCannotCome(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = perchFor(0);
		ServerPlayer owner = TestPlayers.survivor(helper);
		understand(owner, stolasOffer().research());
		understand(owner, buerOffer().research());
		FIXTURES.get(BuerEntity.COUNTERPART).meet(helper, owner, at);
		RateGate.forget(owner.getUUID());
		PactPerchBlockEntity perch = perch(helper, at, owner);
		beside(helper, owner, at);
		PactMenu menu = open(helper, owner, perch);
		PactStatePayload chooser = menu.lastSent();
		helper.assertTrue(chooser.mode() == PactStatePayload.INVITE && chooser.patterns().size() >= 3 && !chooser.patterns().get(0).ready()
				&& chooser.patterns().get(1).ready() && chooser.patterns().stream().filter(c -> c.id().equals(BathinEntity.SIGN))
						.map(PactStatePayload.Choice::ready).toList().equals(List.of(false)),
				"The chooser: Stolas not ready (no star of theirs), Buer ready, Bathin listed once and not ready (no Heart; GC-05): " + chooser);
		helper.assertTrue(answered(invite(owner, menu, 1), PactMenu.INVITED) && perch.visitor(level) instanceof BuerEntity, "INVITE(1): Buer comes");
		helper.assertTrue(answered(send(owner, menu, PactRequestPayload.DECLINE, 0), PactMenu.DECLINED), "Its owner declines");
		RateGate.forget(owner.getUUID());
		PactMenu again = open(helper, owner, perch);
		PactSpiritEntity<?> waiting = perch.visitor(level);
		helper.assertTrue(again.lastSent().mode() == PactStatePayload.INVITE && waiting instanceof BuerEntity,
				"Reopened before his next think: the chooser, and Buer still awaited on the perch");
		PactStatePayload invited = invite(owner, again, 0);
		helper.assertTrue(answered(invited, PactMenu.INVITED) && perch.visitor() == null && perch.visitor(level) == null && waiting.gone(),
				"INVITE(0), Stolas, who cannot come yet: Buer leaves, and nobody waits on the perch while Stolas's terms are shown: " + invited);
		helper.succeed();
	}

	/** GC-01 (GB6, SG9): each pact's roster row is portrayed by its spirit; a pact worker with no record has no portrait; a record whose counterpart is no spirit is a spirit's row. */
	@GameTest(maxTicks = 20)
	public void theRosterPortraitsEachPactBySpirit(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		long now = level.getGameTime();
		UUID stolasRecord = stolasPact(helper, holder, new BlockPos(2, 1, 2), now).holder().agreement().record();
		UUID buerRecord = buerPact(helper, holder, new BlockPos(6, 1, 2), now).holder().agreement().record();
		RosterStatePayload both = roster(holder);
		RosterStatePayload.Row stolasRow = rowOf(both, stolasRecord);
		RosterStatePayload.Row buerRow = rowOf(both, buerRecord);
		helper.assertTrue(stolasRow != null && buerRow != null && key(stolasRow.kind()).equals("message.jugcraft.concordance.workers.kind.pact")
				&& key(buerRow.kind()).equals("message.jugcraft.concordance.workers.kind.pact") && "jugcraft:stolas".equals(stolasRow.portrait())
				&& "jugcraft:buer".equals(buerRow.portrait()), "Two pacts, each a pact row portrayed by its own spirit: " + stolasRow + " " + buerRow);
		UUID noted = UUID.randomUUID();
		WorkerRoster.of(level.getServer()).note(holder.getUUID(), noted, "pact", Status.IDLE, level.dimension().identifier().toString(),
				helper.absolutePos(new BlockPos(4, 1, 4)));
		UUID odd = UUID.randomUUID();
		helper.assertValueEqual(BoundWills.of(level.getServer()).seal(new BoundWill(odd, "jugcraft:no_such_pact", "jugcraft:no_such_spirit",
				holder.getUUID(), now + 1, false)).outcome(), BoundWillLedger.Outcome.DONE, "A record whose counterpart is no spirit");
		RosterStatePayload after = roster(holder);
		RosterStatePayload.Row worker = rowOf(after, noted);
		RosterStatePayload.Row spirit = rowOf(after, odd);
		helper.assertTrue(worker != null && "".equals(worker.portrait()) && key(worker.kind()).equals("message.jugcraft.concordance.workers.kind.pact"),
				"A pact worker noted with no record: no portrait of its own (the Spirit Anchor): " + worker);
		helper.assertTrue(spirit != null && key(spirit.kind()).equals("message.jugcraft.concordance.workers.kind.spirit"),
				"A record whose counterpart is no spirit: a spirit's row: " + spirit);
		helper.succeed();
	}

	// ---------------------------------------------------------------- Concordance garden cohort, GC-05: Bathin, his service and his matter

	/** GC-05: his service's words (tools/concordance_pacts.py SERVICE_WORDS["verdance_carrying"]). */
	static final String CARRYING_KEY = "message.jugcraft.concordance.pact.verdance_carrying.";

	static {
		// GC-05 (SG12): Bathin comes where an awake Verdant Heart and an awake Gleaner the inviter keeps lie within 10 of the
		// perch: the Heart beside the perch's grid cell (perchFor leaves x - 1 free) and the Gleaner at x - 1, z - 1 (clear of
		// every case's second and copied perches, which stand north of the perch at x, z - 1). The Heart holds no Verdance and
		// stands on no bed, so it makes none and nothing is ever carried: every shared case reads him idle.
		FIXTURES.put(BathinEntity.COUNTERPART, (helper, player, perch) -> {
			BlockPos heart = perch.offset(-1, 0, 0);
			BlockPos gleaner = perch.offset(-1, 0, -1);
			keptHeart(helper, heart, player.getUUID(), 0);
			keptGleaner(helper, gleaner, player.getUUID(), 0);
			return List.of(heart, gleaner);
		});
	}

	/** Bathin's offer as the server loaded it (data/jugcraft/concordance/pact/bathin.json). */
	static PactDefinition bathinOffer() {
		PactDefinition offer = PactPerchBlockEntity.definition(BathinEntity.COUNTERPART);
		if (offer == null) {
			throw new AssertionError("Bathin's offer is loaded");
		}
		return offer;
	}

	/** An awake Verdant Heart at {@code at} kept by {@code keeper}, holding {@code verdance}, with no bed under it (it makes none). */
	static VerdantHeartBlockEntity keptHeart(GameTestHelper helper, BlockPos at, UUID keeper, long verdance) {
		helper.setBlock(at, Garden.VERDANT_HEART);
		VerdantHeartBlockEntity heart = helper.getBlockEntity(at, VerdantHeartBlockEntity.class);
		heart.awaken(keeper);
		heart.setVerdance(verdance);
		return heart;
	}

	/** An awake Gleaner at {@code at} kept by {@code keeper}, holding {@code verdance}. */
	static GleanerBlockEntity keptGleaner(GameTestHelper helper, BlockPos at, UUID keeper, long verdance) {
		helper.setBlock(at, Garden.GLEANER);
		GleanerBlockEntity gleaner = helper.getBlockEntity(at, GleanerBlockEntity.class);
		gleaner.awaken(keeper);
		gleaner.setVerdance(verdance);
		return gleaner;
	}

	/** A Ley Pylon at {@code at} owned by {@code owner} (null: no one), holding {@code ley}. */
	static LeyPylonBlockEntity ownedPylon(GameTestHelper helper, BlockPos at, @Nullable UUID owner, long ley) {
		helper.setBlock(at, JugcraftConcordance.LEY_PYLON);
		LeyPylonBlockEntity pylon = helper.getBlockEntity(at, LeyPylonBlockEntity.class);
		pylon.setOwner(owner);
		pylon.setLey(helper.getLevel(), ley);
		return pylon;
	}

	/** Bathin's pact sealed for {@code holder} at a perch they placed at {@code at}, at game time {@code now}; they stand beside it. */
	static PactPerchBlockEntity bathinPact(GameTestHelper helper, ServerPlayer holder, BlockPos at, long now) {
		PactPerchBlockEntity perch = perch(helper, at, holder);
		beside(helper, holder, at);
		helper.assertValueEqual(perch.holder().seal(helper.getLevel(), holder, PactPerchBlockEntity.sealing(bathinOffer(), null, null), now).outcome(),
				Pacts.Outcome.DONE, "Bathin's pact is sealed at the perch");
		return perch;
	}

	/** The Bathin kept at {@code perch} (its presence), asserted there. */
	static BathinEntity bathin(GameTestHelper helper, PactPerchBlockEntity perch) {
		PactSpiritEntity<?> found = perch.presence(helper.getLevel());
		helper.assertTrue(found instanceof BathinEntity, "Bathin is on his perch: " + found);
		return (BathinEntity) found;
	}

	static VerdanceCarrying.Memory carrying(BathinEntity bathin) {
		return bathin.serviceMemory(VerdanceCarrying.Memory.class, VerdanceCarrying.Memory::new);
	}

	/** The Verdance in {@code hearts} and {@code gleaners} together. */
	static long verdance(List<VerdantHeartBlockEntity> hearts, List<GleanerBlockEntity> gleaners) {
		return hearts.stream().mapToLong(VerdantHeartBlockEntity::verdance).sum() + gleaners.stream().mapToLong(GleanerBlockEntity::verdance).sum();
	}

	/** How many of {@code spirit}'s recent cues are {@code cue}. */
	static long cued(PactSpiritEntity<?> spirit, String cue) {
		return spirit.cues().stream().filter(cue::equals).count();
	}

	/** The holder answers his matter at the perch as the screen does (RESOLVE with the answer's index among those sent); the perch keeps it. */
	static void answer(GameTestHelper helper, ServerPlayer holder, PactPerchBlockEntity perch, String accord) {
		RateGate.forget(holder.getUUID());
		PactMenu menu = open(helper, holder, perch);
		send(holder, menu, PactRequestPayload.RESOLVE, VerdanceCarrying.ACCORDS.indexOf(accord));
		holder.closeContainer();
		helper.assertValueEqual(perch.accord(), accord, "The perch keeps the answer " + accord);
	}

	/**
	 * GC-05: a Heart of {@code keeper}'s that already has its own road: an awake Verdant Heart at {@code heartAt} holding 16
	 * Verdance (on no bed: it makes none and never pours by itself), a Ley Pylon {@code keeper} owns beside it (east), and,
	 * so Bathin has somewhere else to carry, an awake empty Gleaner of theirs five blocks west (beyond its own reach of 4).
	 */
	static VerdantHeartBlockEntity heartWithItsOwnRoad(GameTestHelper helper, UUID keeper, BlockPos heartAt) {
		VerdantHeartBlockEntity heart = keptHeart(helper, heartAt, keeper, 16);
		ownedPylon(helper, heartAt.east(), keeper, 0);
		keptGleaner(helper, heartAt.west(5), keeper, 0);
		return heart;
	}

	/**
	 * GC-05 (GC-P59): {@code player} opens their empty perch for {@code d}. With two or more spirits they may invite (Buer and
	 * Bathin share The Garden's Regard) the chooser comes first, and {@code d} is chosen there by its sign, as its entry sends
	 * INVITE; with one, its offer comes at once (GC-01's rule). The menu, its last state {@code d}'s NEGOTIATE.
	 */
	static PactMenu offerOf(GameTestHelper helper, ServerPlayer player, PactPerchBlockEntity perch, PactDefinition d) {
		PactMenu menu = open(helper, player, perch);
		if (menu.lastSent().mode() == PactStatePayload.INVITE) {
			List<PactStatePayload.Choice> listed = menu.lastSent().patterns();
			int index = -1;
			for (int i = 0; i < listed.size(); i++) {
				if (listed.get(i).id().equals(PactSpirits.sign(d.id()))) {
					index = i;
				}
			}
			helper.assertTrue(index >= 0, d.id() + " is on the chooser: " + listed.stream().map(PactStatePayload.Choice::id).toList());
			PactStatePayload invited = invite(player, menu, index);
			helper.assertTrue(answered(invited, PactMenu.INVITED), d.id() + " is invited from the chooser: "
					+ String.valueOf(invited == null ? null : invited.result()));
		}
		helper.assertTrue(menu.lastSent().mode() == PactStatePayload.NEGOTIATE && PactSpirits.sign(d.id()).equals(menu.lastSent().spirit().sign()),
				d.id() + "'s offer is shown");
		return menu;
	}

	/** GC-05: one move a think: the Heart down by exactly what a far Gleaner gains, at most 8, both in one step; counted once; no item. */
	@GameTest(maxTicks = 20)
	public void bathinMovesVerdanceExactly(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity heart = keptHeart(helper, new BlockPos(0, 1, 0), keeper.getUUID(), 10);
		GleanerBlockEntity near = keptGleaner(helper, new BlockPos(2, 1, 2), keeper.getUUID(), 0);
		GleanerBlockEntity far = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		int items = carried(keeper);
		helper.assertValueEqual(bathin.think(level, NOON, now), Status.WORKING, "He carries");
		helper.assertTrue(heart.verdance() == 2 && far.verdance() == 8 && near.verdance() == 0,
				"8 from the Heart into the far Gleaner in the same step; the Gleaner within its own reach is left to its own draw: "
						+ heart.verdance() + ", " + far.verdance() + ", " + near.verdance());
		VerdanceCarrying.Move last = carrying(bathin).last();
		helper.assertTrue(last != null && !last.pylon() && last.verdance() == 8 && last.from().equals(heart.getBlockPos())
				&& last.to().equals(far.getBlockPos()) && last.at() == now, "He remembers the move, never an amount held: " + last);
		helper.assertTrue(Pacts.done(pactOf(level, perch).pact(), now) == 1 && cued(bathin, "work") == 1, "Counted once; one work cue");
		List<Component> rows = VerdanceCarrying.rows(level, pactOf(level, perch), now);
		helper.assertTrue(keys(rows).equals(List.of(CARRYING_KEY + "row.service", CARRYING_KEY + "row.moved"))
				&& Integer.valueOf(1).equals(arg(rows.get(0), 0)) && Long.valueOf(8L).equals(arg(rows.get(1), 0))
				&& far.getBlockPos().toShortString().equals(arg(rows.get(1), 2)), "The kept page: 1 move today, and where 8 went: " + keys(rows));
		helper.assertValueEqual(bathin.think(level, MIDNIGHT, now + 20), Status.WORKING,
				"The next think, at midnight (his hours are every hour, GC-P41): the rest");
		helper.assertTrue(heart.verdance() == 0 && far.verdance() == 10 && heart.verdance() + far.verdance() + near.verdance() == 10,
				"The last 2: what the Heart held; 10 in all before and after");
		helper.assertValueEqual(bathin.think(level, NOON, now + 40), Status.IDLE, "The Heart empty, he waits");
		helper.assertTrue(carrying(bathin).idle() == VerdanceCarrying.Idle.EMPTY
				&& new PactService.Note("empty", 0).equals(VerdanceCarrying.SERVICE.note(level, bathin, pactOf(level, perch), NOON, now + 40)),
				"Jade says why: nothing to carry");
		helper.assertTrue(carried(keeper) == items && loose(helper) == 0 && Pacts.done(pactOf(level, perch).pact(), now) == 2,
				"No item moved; two moves counted");
		helper.succeed();
	}

	/** GC-05: into a pylon not beside the Heart, at most 3 whole batches, 3 Verdance for 2 Ley Charge each; never a part batch; a loss. */
	@GameTest(maxTicks = 20)
	public void bathinPoursIntoAFarPylonAtThreeForTwo(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity heart = keptHeart(helper, new BlockPos(0, 1, 0), keeper.getUUID(), 10);
		LeyPylonBlockEntity far = ownedPylon(helper, new BlockPos(7, 1, 0), keeper.getUUID(), 0);
		helper.assertValueEqual(bathin.think(level, NOON, now), Status.WORKING, "He pours into the far pylon");
		helper.assertTrue(heart.verdance() == 1 && far.ley() == 6, "Three batches at most: 9 Verdance for 6 Ley Charge, 1 left: "
				+ heart.verdance() + ", " + far.ley());
		helper.assertTrue(3 * far.ley() == 2 * (10 - heart.verdance()) && far.ley() < 10 - heart.verdance(), "3 for 2: a third lost, nothing made");
		VerdanceCarrying.Move last = carrying(bathin).last();
		helper.assertTrue(last != null && last.pylon() && last.verdance() == 9 && last.ley() == 6 && last.to().equals(far.getBlockPos()),
				"He remembers the pour: " + last);
		List<Component> rows = VerdanceCarrying.rows(level, pactOf(level, perch), now);
		helper.assertTrue(keys(rows).equals(List.of(CARRYING_KEY + "row.service", CARRYING_KEY + "row.poured"))
				&& Long.valueOf(9L).equals(arg(rows.get(1), 0)) && Long.valueOf(6L).equals(arg(rows.get(1), 3)), "The kept page names the pour");
		helper.assertValueEqual(bathin.think(level, NOON, now + 20), Status.IDLE, "1 Verdance is not a batch");
		helper.assertTrue(heart.verdance() == 1 && far.ley() == 6, "nothing moved");
		heart.setVerdance(64);
		far.setLey(level, LeyPylonBlockEntity.CAPACITY - 1);
		helper.assertValueEqual(bathin.think(level, NOON, now + 40), Status.IDLE, "Room for less than a batch: full to him");
		helper.assertTrue(heart.verdance() == 64 && far.ley() == LeyPylonBlockEntity.CAPACITY - 1
				&& carrying(bathin).idle() == VerdanceCarrying.Idle.NOWHERE, "nothing moved; nowhere to set it");
		far.setLey(level, LeyPylonBlockEntity.CAPACITY - 4);
		helper.assertValueEqual(bathin.think(level, NOON, now + 60), Status.WORKING, "Room for two batches");
		helper.assertTrue(heart.verdance() == 58 && far.ley() == LeyPylonBlockEntity.CAPACITY && Pacts.done(pactOf(level, perch).pact(), now) == 2,
				"Two whole batches, 6 for 4; two moves counted");
		helper.succeed();
	}

	/** GC-05 (GC-P50): the lossless road first: a far Gleaner with room for a whole move before a pylon; without that room, the pylon. */
	@GameTest(maxTicks = 20)
	public void bathinPrefersAFarGleanerToAPylon(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity heart = keptHeart(helper, new BlockPos(0, 1, 0), keeper.getUUID(), 10);
		GleanerBlockEntity gleaner = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		LeyPylonBlockEntity pylon = ownedPylon(helper, new BlockPos(7, 1, 0), keeper.getUUID(), 0);
		helper.assertValueEqual(bathin.think(level, NOON, now), Status.WORKING, "He carries");
		helper.assertTrue(gleaner.verdance() == 8 && heart.verdance() == 2 && pylon.ley() == 0,
				"The lossless road first: 8 into the far Gleaner, nothing into the pylon: " + heart.verdance() + ", " + gleaner.verdance() + ", " + pylon.ley());
		helper.assertValueEqual(bathin.think(level, NOON, now + 20), Status.WORKING, "The next think");
		helper.assertTrue(gleaner.verdance() == 10 && heart.verdance() == 0 && pylon.ley() == 0, "The rest into the Gleaner too");
		heart.setVerdance(10);
		gleaner.setVerdance(9);
		helper.assertValueEqual(bathin.think(level, NOON, now + 40), Status.WORKING, "A Gleaner with room for 7 is no whole move: the pylon");
		helper.assertTrue(pylon.ley() == 6 && heart.verdance() == 1 && gleaner.verdance() == 9,
				"A Gleaner without room for a whole move is passed over; the pylon takes 3 batches: " + heart.verdance() + ", " + pylon.ley());
		helper.assertTrue(Pacts.done(pactOf(level, perch).pact(), now) == 3, "Three moves counted");
		helper.succeed();
	}

	/** GC-05 (GE3; GC-P50's order): one change a think: the fullest Heart into the emptiest Gleaner with room, the total kept. */
	@GameTest(maxTicks = 20)
	public void bathinChangesAtMostOneMoveAThink(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity a = keptHeart(helper, new BlockPos(0, 1, 0), keeper.getUUID(), 10);
		VerdantHeartBlockEntity b = keptHeart(helper, new BlockPos(0, 1, 7), keeper.getUUID(), 20);
		GleanerBlockEntity l = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		GleanerBlockEntity m = keptGleaner(helper, new BlockPos(7, 1, 0), keeper.getUUID(), 4);
		long total = 34;
		helper.assertValueEqual(bathin.think(level, NOON, now), Status.WORKING, "Think 1");
		helper.assertTrue(b.verdance() == 12 && l.verdance() == 8 && a.verdance() == 10 && m.verdance() == 4
				&& verdance(List.of(a, b), List.of(l, m)) == total && cued(bathin, "work") == 1,
				"The fullest Heart into the emptiest Gleaner; nothing else changed; one work cue");
		helper.assertValueEqual(bathin.think(level, NOON, now + 20), Status.WORKING, "Think 2");
		helper.assertTrue(b.verdance() == 4 && m.verdance() == 12 && a.verdance() == 10 && l.verdance() == 8
				&& verdance(List.of(a, b), List.of(l, m)) == total, "B still the fullest; M now the emptiest with room");
		helper.assertValueEqual(bathin.think(level, NOON, now + 40), Status.WORKING, "Think 3");
		helper.assertTrue(a.verdance() == 2 && l.verdance() == 16 && b.verdance() == 4 && m.verdance() == 12
				&& verdance(List.of(a, b), List.of(l, m)) == total, "A the fullest; L has room for a whole move, M not");
		helper.assertValueEqual(bathin.think(level, NOON, now + 60), Status.IDLE, "Think 4: no Gleaner has room for a whole move, and no pylon");
		helper.assertTrue(a.verdance() == 2 && l.verdance() == 16 && b.verdance() == 4 && m.verdance() == 12
				&& verdance(List.of(a, b), List.of(l, m)) == total && carrying(bathin).idle() == VerdanceCarrying.Idle.NOWHERE,
				"Nothing changed; nowhere to set it");
		helper.succeed();
	}

	/**
	 * GC-05 (GC-S25, decided by the session; the unit review's finding 4): a Heart beating on its bed makes 1 or 2 Verdance a
	 * beat (tolerating or thriving). He leaves it while it holds less than a whole move, so no move of his day is spent on a
	 * beat's worth, and carries MOVE_MAX in one move once it has gathered; when it makes no more (its bed spent), what is left
	 * is his to carry, so the nearly empty Heart is still emptied. The bed lit and among three kinds of crop, as the garden's
	 * own test of the beat; the Heart placed asleep and woken only at tick 40, so its own pulse beats nothing before these do.
	 */
	@GameTest(maxTicks = 60)
	public void bathinCarriesWholeMovesFromABeatingHeart(GameTestHelper helper) {
		ServerPlayer keeper = gardenHolder(helper);
		BlockPos bedAt = new BlockPos(1, 1, 1);
		VerdantBedBlockEntity bed = keptBed(helper, bedAt, keeper.getUUID(), 0);
		bed.setNutrients(16);
		// Three kinds of crop in the Heart's square at its height (Sampler.RADIUS 2), each on a bed of its own; a lamp beside it.
		keptBed(helper, new BlockPos(0, 1, 3), keeper.getUUID(), 0);
		keptBed(helper, new BlockPos(1, 1, 3), keeper.getUUID(), 0);
		keptBed(helper, new BlockPos(2, 1, 3), keeper.getUUID(), 0);
		helper.setBlock(new BlockPos(0, 2, 3), Garden.SUNPETAL_CROP);
		helper.setBlock(new BlockPos(1, 2, 3), Garden.DEWMOSS_CROP);
		helper.setBlock(new BlockPos(2, 2, 3), Garden.MENDVETCH_CROP);
		helper.setBlock(bedAt.above().north(), Blocks.GLOWSTONE);
		helper.setBlock(bedAt.above(), Garden.VERDANT_HEART);
		VerdantHeartBlockEntity heart = helper.getBlockEntity(bedAt.above(), VerdantHeartBlockEntity.class);
		// Its bed's reading is taken in the ticks before (the level's samples a tick are shared with every running test).
		for (int tick : new int[] {10, 20, 30}) {
			helper.runAtTickTime(tick, () -> bed.habitat(helper.getLevel()));
		}
		helper.runAtTickTime(40, () -> {
			ServerLevel level = helper.getLevel();
			helper.assertTrue(bed.habitat(level) != null, "The Heart's bed has its reading");
			heart.awaken(keeper.getUUID());
			long now = dayStart(level, 1);
			PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(5, 1, 3), now);
			BathinEntity bathin = bathin(helper, perch);
			GleanerBlockEntity far = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
			long at = now;
			int beats = 0;
			while (heart.verdance() < VerdanceCarrying.MOVE_MAX) {
				long before = heart.verdance();
				beats++;
				String beat = heart.beat(level);
				helper.assertTrue(beats <= VerdanceCarrying.MOVE_MAX && beat.equals("working") && heart.verdance() > before,
						"The Heart beats on its bed, making Verdance: " + beat + ", " + heart.verdance());
				if (heart.verdance() < VerdanceCarrying.MOVE_MAX) {
					helper.assertValueEqual(bathin.think(level, NOON, at), Status.IDLE, "Less than a whole move in a beating Heart: he leaves it");
					helper.assertTrue(far.verdance() == 0 && carrying(bathin).last() == null && carrying(bathin).idle() == VerdanceCarrying.Idle.EMPTY,
							"Nothing moved, and Jade says there is nothing to carry yet: " + heart.verdance());
					at += 20;
				}
			}
			long gathered = heart.verdance();
			helper.assertTrue(gathered - VerdanceCarrying.MOVE_MAX < 2 && beats >= VerdanceCarrying.MOVE_MAX / 2
					&& Pacts.done(pactOf(level, perch).pact(), now) == 0,
					"1 or 2 a beat: " + beats + " beats gathered " + gathered + ", and none of his moves was spent on them");
			helper.assertValueEqual(bathin.think(level, NOON, at), Status.WORKING, "A whole move gathered, he carries it");
			VerdanceCarrying.Move move = carrying(bathin).last();
			helper.assertTrue(heart.verdance() == gathered - VerdanceCarrying.MOVE_MAX && far.verdance() == VerdanceCarrying.MOVE_MAX
					&& move != null && move.verdance() == VerdanceCarrying.MOVE_MAX && Pacts.done(pactOf(level, perch).pact(), now) == 1,
					"MOVE_MAX in one move, counted once: " + heart.verdance() + ", " + far.verdance() + ", " + move);
			helper.assertValueEqual(heart.beat(level), "working", "It beats again");
			long left = heart.verdance();
			helper.assertTrue(left > 0 && left < VerdanceCarrying.MOVE_MAX, "A beat's worth and what the move left: " + left);
			helper.assertValueEqual(bathin.think(level, NOON, at + 20), Status.IDLE, "Still beating with less than a whole move: he leaves it");
			bed.setNutrients(0);
			String stopped = heart.beat(level);
			helper.assertTrue(!stopped.equals("working") && heart.verdance() == left, "Its bed spent, it makes no more: " + stopped);
			helper.assertValueEqual(bathin.think(level, NOON, at + 40), Status.WORKING, "Making no more, what it holds is his to carry");
			helper.assertTrue(heart.verdance() == 0 && far.verdance() == VerdanceCarrying.MOVE_MAX + left
					&& Pacts.done(pactOf(level, perch).pact(), now) == 2,
					"The nearly empty Heart emptied in one move; every Verdance it made is in the Gleaner: " + heart.verdance() + ", " + far.verdance());
			helper.succeed();
		});
	}

	/** GC-05 (S1): his save holds no Verdance; killed or unloaded between thinks every reservoir holds what it held; called back, he carries on. */
	@GameTest(maxTicks = 20)
	public void bathinNeverHoldsVerdance(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(3, 1, 3);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, at, now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity heart = keptHeart(helper, new BlockPos(0, 1, 0), keeper.getUUID(), 20);
		GleanerBlockEntity gleaner = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		helper.assertValueEqual(bathin.think(level, NOON, now), Status.WORKING, "A move");
		helper.assertTrue(heart.verdance() == 12 && gleaner.verdance() == 8, "8 moved, 20 in all");
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		bathin.saveWithoutId(saved);
		CompoundTag tag = saved.buildResult();
		helper.assertTrue(!tag.toString().toLowerCase(Locale.ROOT).contains("verdance") && !tag.contains("memory") && !tag.contains("last"),
				"His save holds no Verdance and no memory: " + tag.keySet());
		bathin.remove(Entity.RemovalReason.KILLED);
		helper.assertTrue(heart.verdance() == 12 && gleaner.verdance() == 8 && loose(helper) == 0, "Killed between thinks: nothing dropped, lost or owed");
		PactKeeping.tick(level, perch.holder(), helper.absolutePos(at), now + 20);
		BathinEntity back = bathin(helper, perch);
		helper.assertTrue(back != bathin && carrying(back).last() == null, "Called back for his holder near, he remembers no move");
		helper.assertValueEqual(back.think(level, NOON, now + 40), Status.WORKING, "and carries on from the reservoirs as they are");
		helper.assertTrue(heart.verdance() == 4 && gleaner.verdance() == 16, "8 more; still 20 in all");
		back.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		helper.assertTrue(heart.verdance() == 4 && gleaner.verdance() == 16, "Unloaded between thinks: unchanged");
		VerdantHeartBlockEntity heartCopy = new VerdantHeartBlockEntity(heart.getBlockPos(), heart.getBlockState());
		heartCopy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), heart.saveWithoutMetadata(level.registryAccess())));
		GleanerBlockEntity gleanerCopy = new GleanerBlockEntity(gleaner.getBlockPos(), gleaner.getBlockState());
		gleanerCopy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), gleaner.saveWithoutMetadata(level.registryAccess())));
		helper.assertTrue(heartCopy.verdance() == 4 && gleanerCopy.verdance() == 16, "A restart reads every Verdance back from its reservoir");
		helper.succeed();
	}

	/** GC-05: a stranger's Heart, Gleaner and pylon never; a party member's while in the party, not after; no one's pylon as the Heart's own pour would. */
	@GameTest(maxTicks = 20)
	public void bathinHonoursTheHeartsParty(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		ServerPlayer member = TestPlayers.survivor(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity theirs = keptHeart(helper, new BlockPos(0, 1, 0), STRANGER, 20);
		GleanerBlockEntity mine = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		helper.assertValueEqual(bathin.think(level, NOON, now), Status.IDLE, "A stranger's Heart is no source of his");
		helper.assertTrue(theirs.verdance() == 20 && mine.verdance() == 0, "untouched");
		VerdantHeartBlockEntity own = keptHeart(helper, new BlockPos(0, 1, 7), keeper.getUUID(), 20);
		GleanerBlockEntity strangers = keptGleaner(helper, new BlockPos(7, 1, 0), STRANGER, 0);
		LeyPylonBlockEntity strangersPylon = ownedPylon(helper, new BlockPos(7, 3, 4), STRANGER, 0);
		mine.setVerdance(GleanerBlockEntity.CAPACITY);
		long refreshed = now + GardenReach.REFRESH_TICKS;
		helper.assertValueEqual(bathin.think(level, NOON, refreshed), Status.IDLE, "His Gleaner full, a stranger's Gleaner and pylon are nowhere he sets it down");
		helper.assertTrue(own.verdance() == 20 && strangers.verdance() == 0 && strangersPylon.ley() == 0 && theirs.verdance() == 20, "all untouched");
		LeyPylonBlockEntity nobodys = ownedPylon(helper, new BlockPos(4, 3, 7), null, 0);
		long again = refreshed + GardenReach.REFRESH_TICKS;
		helper.assertValueEqual(bathin.think(level, NOON, again), Status.WORKING, "No one's pylon is fed, as the Heart's own pour would feed it");
		helper.assertTrue(own.verdance() == 11 && nobodys.ley() == 6 && strangersPylon.ley() == 0 && strangers.verdance() == 0,
				"9 for 6 into no one's; a stranger's still untouched");
		VerdantHeartBlockEntity members = keptHeart(helper, new BlockPos(1, 5, 1), member.getUUID(), 30);
		GleanerBlockEntity membersGleaner = keptGleaner(helper, new BlockPos(7, 5, 7), member.getUUID(), 0);
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(keeper.getUUID(), member.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(member.getUUID(), ms) == PartyManager.Result.OK, "The holder and a member form a party");
			long joined = again + GardenReach.REFRESH_TICKS;
			helper.assertValueEqual(bathin.think(level, NOON, joined), Status.WORKING, "Under party, the member's Heart and Gleaner are his to serve");
			helper.assertTrue(members.verdance() == 22 && membersGleaner.verdance() == 8, "8 from the member's Heart (the fullest) into the member's Gleaner");
			helper.assertTrue(parties.leave(member.getUUID()) == PartyManager.Result.OK, "The member leaves the party");
			helper.assertValueEqual(bathin.think(level, NOON, joined + 20), Status.WORKING, "He goes on with the holder's own");
			helper.assertTrue(members.verdance() == 22 && membersGleaner.verdance() == 8 && own.verdance() == 2 && nobodys.ley() == 12,
					"Their Heart and Gleaner refused at once (asked at each change), indexed or not; the holder's poured instead");
		} finally {
			parties.adminDisband(keeper.getUUID());
		}
		helper.succeed();
	}

	/**
	 * GC-05 (GC-S7): never a Gleaner or a Heart its holder could not change by hand (a claim); each refused for itself is
	 * skipped until his index is read again (the unit review, as Buer's beds); unclaimed and read again, he carries.
	 */
	@GameTest(maxTicks = 20)
	public void bathinNeverTouchesAClaimedReservoir(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity heart = keptHeart(helper, new BlockPos(0, 1, 0), keeper.getUUID(), 10);
		GleanerBlockEntity gleaner = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		try (TestClaims claims = TestClaims.open().block(helper.absolutePos(new BlockPos(7, 1, 7)), STRANGER)) {
			helper.assertValueEqual(bathin.think(level, NOON, now), Status.IDLE, "The holder could not change the claimed Gleaner by hand");
			helper.assertTrue(heart.verdance() == 10 && gleaner.verdance() == 0 && carrying(bathin).skipped(gleaner.getBlockPos())
					&& !carrying(bathin).skipped(heart.getBlockPos()), "Neither is touched; the Gleaner, refused for itself, is skipped");
		}
		try (TestClaims claims = TestClaims.open().block(helper.absolutePos(new BlockPos(0, 1, 0)), STRANGER)) {
			helper.assertValueEqual(bathin.think(level, NOON, now + 20), Status.IDLE, "nor take from a claimed Heart");
			helper.assertTrue(heart.verdance() == 10 && gleaner.verdance() == 0 && carrying(bathin).skipped(heart.getBlockPos()),
					"unchanged; the Heart, refused for itself, is skipped too");
		}
		helper.assertValueEqual(bathin.think(level, NOON, now + 40), Status.IDLE, "Unclaimed, both are skipped until his index is read again");
		helper.assertTrue(heart.verdance() == 10 && gleaner.verdance() == 0, "still unchanged");
		helper.assertValueEqual(bathin.think(level, NOON, now + GardenReach.REFRESH_TICKS), Status.WORKING,
				"His index read again, both are asked again: unclaimed, he carries");
		helper.assertTrue(heart.verdance() == 2 && gleaner.verdance() == 8 && !carrying(bathin).skipped(gleaner.getBlockPos()),
				"8 from the Heart into the Gleaner");
		helper.succeed();
	}

	/**
	 * GC-05 (the unit review): Gleaners refused for themselves never starve the pylon road. His questions spent on the Heart
	 * and seven claimed Gleaners, he idles and Jade says why; those Gleaners skipped until his index is read again, his next
	 * think spends its questions on the pylon and pours.
	 */
	@GameTest(maxTicks = 20)
	public void refusedGleanersNeverStarveBathinsPylonRoad(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity heart = keptHeart(helper, new BlockPos(0, 1, 0), keeper.getUUID(), 10);
		LeyPylonBlockEntity pylon = ownedPylon(helper, new BlockPos(0, 3, 7), keeper.getUUID(), 0);
		List<BlockPos> claimed = List.of(new BlockPos(7, 1, 7), new BlockPos(7, 1, 5), new BlockPos(5, 1, 7), new BlockPos(7, 3, 7),
				new BlockPos(5, 3, 7), new BlockPos(7, 3, 5), new BlockPos(7, 5, 7));
		helper.assertValueEqual(claimed.size(), VerdanceCarrying.MAX_ASKED - 1, "Seven Gleaners: with the Heart, every question of a think");
		try (TestClaims claims = TestClaims.open()) {
			List<GleanerBlockEntity> gleaners = new ArrayList<>();
			for (BlockPos at : claimed) {
				gleaners.add(keptGleaner(helper, at, keeper.getUUID(), 0));
				claims.block(helper.absolutePos(at), STRANGER);
			}
			helper.assertValueEqual(bathin.think(level, NOON, now), Status.IDLE,
					"His questions spent on the Heart and the claimed Gleaners, none is left for the pylon");
			helper.assertTrue(heart.verdance() == 10 && pylon.ley() == 0
					&& gleaners.stream().allMatch(gleaner -> gleaner.verdance() == 0 && carrying(bathin).skipped(gleaner.getBlockPos()))
					&& carrying(bathin).idle() == VerdanceCarrying.Idle.NOWHERE
					&& new PactService.Note("nowhere", 0).equals(VerdanceCarrying.SERVICE.note(level, bathin, pactOf(level, perch), NOON, now)),
					"Nothing moved; each refused Gleaner skipped until his index is read again; Jade says why he idles");
			helper.assertValueEqual(bathin.think(level, NOON, now + 20), Status.WORKING, "His next think asks the pylon instead");
			helper.assertTrue(heart.verdance() == 1 && pylon.ley() == 6 && gleaners.stream().allMatch(gleaner -> gleaner.verdance() == 0),
					"9 for 6 into the pylon; no claimed Gleaner touched: " + heart.verdance() + ", " + pylon.ley());
		}
		helper.succeed();
	}

	/**
	 * GC-05 (PA 2.14; the unit review): his matter is raised only for a beneficiary's Heart. A party member's Heart with its
	 * own road raises it; the member gone from the party, at once it is no longer asked at the perch, and his next think,
	 * before his index is read again, neither weighs it nor counts it for Jade as waiting for an answer.
	 */
	@GameTest(maxTicks = 20)
	public void aFormerMembersHeartRaisesNoMatter(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		ServerPlayer member = TestPlayers.survivor(helper);
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(keeper.getUUID(), member.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(member.getUUID(), ms) == PartyManager.Result.OK, "The holder and a member form a party");
			long now = dayStart(level, 1);
			PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), now);
			BathinEntity bathin = bathin(helper, perch);
			VerdantHeartBlockEntity members = heartWithItsOwnRoad(helper, member.getUUID(), new BlockPos(5, 2, 5));
			helper.assertValueEqual(bathin.think(level, NOON, now), Status.AWAITING_ANSWER, "The member's Heart has its own road: he raises his matter");
			helper.assertTrue(VerdanceCarrying.SERVICE.complication(level, pactOf(level, perch), perch, now) != null, "His matter is asked at the perch");
			helper.assertTrue(parties.leave(member.getUUID()) == PartyManager.Result.OK, "The member leaves the party");
			helper.assertTrue(VerdanceCarrying.SERVICE.complication(level, pactOf(level, perch), perch, now + 10) == null,
					"At once their Heart is no matter of his: whose it is is asked again");
			helper.assertValueEqual(bathin.think(level, NOON, now + 20), Status.IDLE,
					"His next think, before his index is read again: nothing of theirs to weigh or carry");
			helper.assertTrue(members.verdance() == 16 && carrying(bathin).roads().isEmpty()
					&& !new PactService.Note("two_roads", 1).equals(VerdanceCarrying.SERVICE.note(level, bathin, pactOf(level, perch), NOON, now + 20)),
					"Their Heart untouched; Jade counts no Heart as waiting for an answer");
		} finally {
			parties.adminDisband(keeper.getUUID());
		}
		helper.succeed();
	}

	/** GC-05: a Heart beside its own pylon is left until answered; he raises it once and works on under the safe answer; answered leave it, he leaves it. */
	@GameTest(maxTicks = 20)
	public void bathinLeavesAHeartWithItsOwnPylonUntilAnswered(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity roaded = keptHeart(helper, new BlockPos(0, 1, 0), keeper.getUUID(), 30);
		LeyPylonBlockEntity road = ownedPylon(helper, new BlockPos(1, 1, 0), keeper.getUUID(), 0);
		GleanerBlockEntity far = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		helper.assertTrue(roaded.feedsAPylonBeside(level) && perch.accord() == null, "The Heart pours into the pylon beside it; nothing answered");
		helper.assertValueEqual(bathin.think(level, NOON, now), Status.AWAITING_ANSWER, "He raises his matter and waits for the answer");
		helper.assertTrue(roaded.verdance() == 30 && far.verdance() == 0 && road.ley() == 0, "and leaves that Heart alone");
		helper.assertTrue(cued(bathin, "inspect") == 1 && bathin.attending() instanceof BlockPosTracker tracked
				&& tracked.currentBlockPosition().equals(roaded.getBlockPos()), "He turns to that Heart and considers it");
		PactService.Complication matter = VerdanceCarrying.SERVICE.complication(level, pactOf(level, perch), perch, now);
		helper.assertTrue(matter != null && VerdanceCarrying.TWO_ROADS.equals(matter.id()) && roaded.getBlockPos().equals(matter.pos())
				&& matter.answers().equals(List.of("yield", "claim")) && key(matter.line()).equals(CARRYING_KEY + "matter.two_roads"),
				"His matter: the Heart with a road, and the two answers: " + matter);
		helper.assertTrue(new PactService.Note("two_roads", 1).equals(VerdanceCarrying.SERVICE.note(level, bathin, pactOf(level, perch), NOON, now)),
				"Jade: one Heart waits for the answer");
		helper.assertValueEqual(bathin.think(level, NOON, now + 20), Status.AWAITING_ANSWER, "Still waiting");
		helper.assertTrue(cued(bathin, "inspect") == 1, "He raised it once");
		VerdantHeartBlockEntity plain = keptHeart(helper, new BlockPos(0, 1, 6), keeper.getUUID(), 10);
		long refreshed = now + GardenReach.REFRESH_TICKS;
		helper.assertValueEqual(bathin.think(level, NOON, refreshed), Status.AWAITING_ANSWER,
				"Meanwhile he works under the safe answer, still awaiting it (GC-P47)");
		helper.assertTrue(plain.verdance() == 2 && far.verdance() == 8 && roaded.verdance() == 30, "from the Heart without a road; the other left");
		answer(helper, keeper, perch, VerdanceCarrying.YIELD);
		helper.assertValueEqual(bathin.think(level, NOON, refreshed + 20), Status.WORKING, "Answered leave it, he goes on with the other");
		helper.assertTrue(plain.verdance() == 0 && far.verdance() == 10 && roaded.verdance() == 30, "Its last 2; the Heart with a road still left");
		helper.assertValueEqual(bathin.think(level, NOON, refreshed + 40), Status.IDLE, "Answered, he no longer waits; nothing else to carry");
		helper.assertTrue(roaded.verdance() == 30 && road.ley() == 0
				&& VerdanceCarrying.SERVICE.complication(level, pactOf(level, perch), perch, refreshed + 40) != null
				&& keys(VerdanceCarrying.rows(level, pactOf(level, perch), refreshed + 40)).contains(CARRYING_KEY + "row.leaving"),
				"That Heart is left as answered; the matter stands so the answer can be changed; the page says which he leaves");
		helper.succeed();
	}

	/** GC-05: answered "take from it", he takes from a Heart with its own road too, into a far Gleaner or a far pylon, never the one beside it. */
	@GameTest(maxTicks = 20)
	public void aClaimAnswerLetsBathinTakeFromIt(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity roaded = keptHeart(helper, new BlockPos(0, 1, 0), keeper.getUUID(), 30);
		LeyPylonBlockEntity road = ownedPylon(helper, new BlockPos(1, 1, 0), keeper.getUUID(), 0);
		GleanerBlockEntity far = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		LeyPylonBlockEntity farPylon = ownedPylon(helper, new BlockPos(7, 3, 0), keeper.getUUID(), 0);
		helper.assertValueEqual(bathin.think(level, NOON, now), Status.AWAITING_ANSWER, "He raises his matter");
		answer(helper, keeper, perch, VerdanceCarrying.CLAIM);
		helper.assertValueEqual(bathin.think(level, NOON, now + 20), Status.WORKING, "Answered take from it, he takes from it too");
		helper.assertTrue(roaded.verdance() == 22 && far.verdance() == 8, "8 into the far Gleaner: " + roaded.verdance() + ", " + far.verdance());
		helper.assertValueEqual(bathin.think(level, NOON, now + 40), Status.WORKING, "Again");
		helper.assertTrue(roaded.verdance() == 14 && far.verdance() == 16, "The Gleaner full");
		helper.assertValueEqual(bathin.think(level, NOON, now + 60), Status.WORKING, "Then a pylon");
		helper.assertTrue(roaded.verdance() == 5 && farPylon.ley() == 6 && road.ley() == 0,
				"A far pylon, never the one beside it: that is the Heart's own road");
		answer(helper, keeper, perch, VerdanceCarrying.YIELD);
		helper.assertValueEqual(bathin.think(level, NOON, now + 80), Status.IDLE, "Answered leave it again, he leaves it");
		helper.assertTrue(roaded.verdance() == 5 && new PactService.Note("empty", 0).equals(VerdanceCarrying.SERVICE.note(level, bathin,
				pactOf(level, perch), NOON, now + 80)), "Answered, Jade says no answer waits: nothing to carry");
		helper.succeed();
	}

	/** GC-05: his terms' moves a game day (12) and no more; the next day he goes on. */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 20)
	public void bathinStopsAtHisQuota(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(8, 1, 8), now);
		BathinEntity bathin = bathin(helper, perch);
		int quota = bathinOffer().quota();
		helper.assertValueEqual(quota, 12, "His terms: 12 moves a game day (GC-P41)");
		List<VerdantHeartBlockEntity> hearts = List.of(keptHeart(helper, new BlockPos(1, 1, 1), keeper.getUUID(), 64),
				keptHeart(helper, new BlockPos(1, 1, 3), keeper.getUUID(), 64));
		List<GleanerBlockEntity> gleaners = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			gleaners.add(keptGleaner(helper, new BlockPos(7 + i, 1, 13), keeper.getUUID(), 0));
		}
		for (int k = 0; k < quota; k++) {
			helper.assertValueEqual(bathin.think(level, NOON, now + 20L * k), Status.WORKING, "Move " + k + " of his " + quota);
		}
		helper.assertValueEqual(bathin.think(level, NOON, now + 20L * quota), Status.FINISHED, "His day's moves spent, he stops");
		long held = hearts.stream().mapToLong(VerdantHeartBlockEntity::verdance).sum();
		long set = gleaners.stream().mapToLong(GleanerBlockEntity::verdance).sum();
		helper.assertTrue(held == 128 - 8L * quota && set == 8L * quota && Pacts.done(pactOf(level, perch).pact(), now) == quota,
				"12 moves of 8: " + held + " left, " + set + " set down");
		helper.assertValueEqual(bathin.think(level, NOON, dayStart(level, 2)), Status.WORKING, "The next game day he carries again");
		helper.succeed();
	}

	/** GC-05 (GE1, GE2): no stand-in; nothing carried for a holder away, dead, in another level or logged out; back beside the perch, he carries. */
	@GameTest(maxTicks = 20)
	public void bathinWaitsForAPresentHolder(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(3, 1, 3);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, at, now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity heart = keptHeart(helper, new BlockPos(0, 1, 0), keeper.getUUID(), 10);
		GleanerBlockEntity gleaner = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		BlockPos home = helper.absolutePos(at);
		keeper.snapTo(home.getX() + 100.5, home.getY(), home.getZ() + 0.5, 0.0F, 0.0F);
		helper.assertValueEqual(bathin.think(level, NOON, now), Status.OWNER_OFFLINE, "100 blocks off in this level: not near");
		boolean answering = JugcraftConfig.option(Authority.ABSENT_OPTION);
		JugcraftConfig.setOption(Authority.ABSENT_OPTION, true);
		try {
			helper.assertValueEqual(bathin.think(level, NOON, now + 20), Status.OWNER_OFFLINE, "With absent owners' devices allowed, still no stand-in");
		} finally {
			JugcraftConfig.setOption(Authority.ABSENT_OPTION, answering);
		}
		beside(helper, keeper, at);
		keeper.setHealth(0.0F);
		helper.assertValueEqual(bathin.think(level, NOON, now + 40), Status.OWNER_OFFLINE, "Dead beside the perch: not present");
		keeper.setHealth(keeper.getMaxHealth());
		ServerLevel nether = level.getServer().getLevel(Level.NETHER);
		helper.assertTrue(nether != null, "The test server has a Nether");
		keeper.teleport(new TeleportTransition(nether, new Vec3(0.5, 64.0, 0.5), Vec3.ZERO, 0.0F, 0.0F, TeleportTransition.DO_NOTHING));
		helper.assertValueEqual(bathin.think(level, NOON, now + 60), Status.OWNER_OFFLINE, "In another level: not present");
		helper.assertTrue(heart.verdance() == 10 && gleaner.verdance() == 0 && Pacts.done(pactOf(level, perch).pact(), now) == 0,
				"Nothing was carried or counted meanwhile");
		keeper.teleport(new TeleportTransition(level, helper.absoluteVec(new Vec3(3.5, 1.0, 5.5)), Vec3.ZERO, 0.0F, 0.0F, TeleportTransition.DO_NOTHING));
		helper.assertValueEqual(bathin.think(level, NOON, now + 80), Status.WORKING, "Back beside the perch, he carries");
		helper.assertTrue(heart.verdance() == 2 && gleaner.verdance() == 8, "8 moved");
		TestPlayers.leave(keeper);
		helper.assertValueEqual(bathin.think(level, NOON, now + 100), Status.OWNER_OFFLINE, "Logged out: he waits");
		helper.assertTrue(heart.verdance() == 2 && gleaner.verdance() == 8, "and carries nothing for them");
		helper.succeed();
	}

	/** GC-05 (Dossier 2 item 4): suspended by its holder he carries nothing and his page says so by name; resumed, he carries. */
	@GameTest(maxTicks = 20)
	public void aSuspendedBathinCarriesNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity heart = keptHeart(helper, new BlockPos(0, 1, 0), keeper.getUUID(), 10);
		GleanerBlockEntity gleaner = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		helper.assertTrue(PactKeeping.suspend(level, keeper, perch.holder(), now) == AgreementHolder.Toggle.SUSPENDED, "Its holder suspends it");
		helper.assertValueEqual(bathin.think(level, NOON, now + 20), Status.SUSPENDED, "Suspended, he waits");
		Component row = VerdanceCarrying.SERVICE.suspendedRow(PactKeeping.name(BathinEntity.COUNTERPART));
		helper.assertTrue(heart.verdance() == 10 && gleaner.verdance() == 0 && Pacts.done(pactOf(level, perch).pact(), now) == 0
				&& key(row).equals("message.jugcraft.concordance.pact.row.state.suspended.serves")
				&& VerdanceCarrying.SERVICE.note(level, bathin, pactOf(level, perch), NOON, now + 20) == null,
				"Nothing carried or counted; his page says so by name; Jade has no note while the pact does not serve");
		helper.assertTrue(PactKeeping.resume(level, keeper, perch.holder(), now + 40) == AgreementHolder.Toggle.RESUMED, "Resumed");
		helper.assertValueEqual(bathin.think(level, NOON, now + 60), Status.WORKING, "He carries again");
		helper.assertTrue(heart.verdance() == 2 && gleaner.verdance() == 8, "8 moved");
		helper.succeed();
	}

	/** GC-05 (GE5; S1 under contention): two holders' Bathins (a party) over one Heart in one tick move what it held, never more; each counts its own. */
	@GameTest(maxTicks = 20)
	public void twoBathinsShareOneHeartExactly(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer first = gardenHolder(helper);
		ServerPlayer second = gardenHolder(helper);
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(first.getUUID(), second.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(second.getUUID(), ms) == PartyManager.Result.OK, "Two holders in one party");
			long now = dayStart(level, 1);
			PactPerchBlockEntity a = bathinPact(helper, first, new BlockPos(2, 1, 2), now);
			PactPerchBlockEntity b = bathinPact(helper, second, new BlockPos(5, 1, 5), now);
			BathinEntity one = bathin(helper, a), other = bathin(helper, b);
			VerdantHeartBlockEntity heart = keptHeart(helper, new BlockPos(0, 1, 0), first.getUUID(), 10);
			GleanerBlockEntity gleanerA = keptGleaner(helper, new BlockPos(7, 1, 7), first.getUUID(), 0);
			GleanerBlockEntity gleanerB = keptGleaner(helper, new BlockPos(7, 1, 0), second.getUUID(), 0);
			helper.assertValueEqual(one.think(level, NOON, now), Status.WORKING, "The first Bathin carries from the party's Heart");
			helper.assertValueEqual(other.think(level, NOON, now), Status.WORKING, "The second, in the same tick, carries what is left");
			helper.assertValueEqual(one.think(level, NOON, now + 20), Status.IDLE, "Then the Heart is empty to the first");
			helper.assertValueEqual(other.think(level, NOON, now + 20), Status.IDLE, "and to the second");
			helper.assertTrue(heart.verdance() == 0 && gleanerA.verdance() + gleanerB.verdance() == 10
					&& Pacts.done(pactOf(level, a).pact(), now) == 1 && Pacts.done(pactOf(level, b).pact(), now) == 1,
					"Two carriers over one Heart moved what it held, never more; each pact counted its own one");
		} finally {
			parties.adminDisband(first.getUUID());
		}
		helper.succeed();
	}

	/** GC-05 (GC-P57): he faces where he set the Verdance down while his work gesture plays. */
	@GameTest(maxTicks = 20)
	public void bathinFacesWhereHeSetsItDown(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), now);
		BathinEntity bathin = bathin(helper, perch);
		keptHeart(helper, new BlockPos(0, 1, 0), keeper.getUUID(), 10);
		GleanerBlockEntity far = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		helper.assertValueEqual(bathin.think(level, NOON, now), Status.WORKING, "He carries");
		helper.assertTrue(bathin.attending() instanceof BlockPosTracker tracked && tracked.currentBlockPosition().equals(far.getBlockPos())
				&& within(bathin.getYRot(), PactSpiritEntity.bearing(bathin.position(), Vec3.atCenterOf(far.getBlockPos()), 0.0F), 1.0F)
				&& cued(bathin, "work") == 1, "He faces where he set it down for his work gesture: " + yaws(bathin));
		helper.succeed();
	}

	/** GC-05: his first obligation is the holder's own use of an awake device by hand in his area; a waking is not; his own carrying is not. */
	@GameTest(maxTicks = 20)
	public void bathinsFirstObligationIsADeviceReadByHand(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long g0 = level.getGameTime();
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), g0);
		BathinEntity bathin = bathin(helper, perch);
		BlockPos heartAt = new BlockPos(0, 1, 0);
		helper.setBlock(heartAt, Garden.VERDANT_HEART);
		BlockPos abs = helper.absolutePos(heartAt);
		Garden.VERDANT_HEART.setPlacedBy(level, abs, level.getBlockState(abs), keeper, new ItemStack(Garden.VERDANT_HEART));
		VerdantHeartBlockEntity heart = helper.getBlockEntity(heartAt, VerdantHeartBlockEntity.class);
		helper.assertTrue(heart.awake() && keeper.getUUID().equals(heart.keeper()), "Placed by a Greenwarden, a Heart is awake at once");
		heart.setVerdance(10);
		keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		helper.assertTrue(VerdanceCarrying.SERVICE.obligation() == PactObligations.Act.DEVICE
				&& keys(VerdanceCarrying.SERVICE.obligationRows(level, keeper, pactOf(level, perch), g0))
						.equals(List.of("message.jugcraft.concordance.garden_pact.obligation")), "His page names the act");
		int heard = GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0);
		helper.assertValueEqual(bathin.think(level, NOON, g0), Status.WORKING, "He carries");
		helper.assertTrue(!PactObligations.kept(keeper, BathinEntity.COUNTERPART) && GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == heard,
				"His own carrying keeps nothing of the holder's");
		BlockPos dormantAt = new BlockPos(6, 1, 0);
		helper.setBlock(dormantAt, Garden.GLEANER);
		ConcordanceGardenGameTests.handUse(helper, keeper, dormantAt, ItemStack.EMPTY, false);
		helper.assertTrue(helper.getBlockEntity(dormantAt, GleanerBlockEntity.class).awake() && !PactObligations.kept(keeper, BathinEntity.COUNTERPART)
				&& GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == heard, "Waking a dormant Gleaner by hand is not his obligation");
		ConcordanceGardenGameTests.handUse(helper, keeper, heartAt, ItemStack.EMPTY, false);
		helper.assertTrue(PactObligations.kept(keeper, BathinEntity.COUNTERPART) && GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == heard + 1
				&& keys(VerdanceCarrying.SERVICE.obligationRows(level, keeper, pactOf(level, perch), g0))
						.equals(List.of("message.jugcraft.concordance.garden_pact.obligation_kept")),
				"Reading the awake Heart with an empty hand in his area keeps it, once");
		ConcordanceGardenGameTests.handUse(helper, keeper, dormantAt, ItemStack.EMPTY, false);
		helper.assertTrue(GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == heard + 1, "and it is not recorded again");
		helper.succeed();
	}

	/**
	 * GC-05 (Jade; GC-P48): the readings of his perch and of him: kept, his work, active; the perch's names him. With a Heart
	 * that has its own road unanswered, both carry his note (two_roads, one Heart) and his matter, no answer yet; none of
	 * the four readings names a player.
	 */
	@GameTest(maxTicks = 20)
	public void bathinsReadingSaysItsNoteAndNamesNoPlayer(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(3, 1, 3), now);
		BathinEntity bathin = bathin(helper, perch);
		CompoundTag ofPerch = PactReading.holder(level, perch, now);
		CompoundTag ofHim = PactReading.presence(level, bathin, now);
		for (CompoundTag reading : List.of(ofPerch, ofHim)) {
			helper.assertTrue("kept".equals(reading.getStringOr("standing", "")) && VerdanceCarrying.WORK.equals(reading.getStringOr("service", ""))
					&& "active".equals(reading.getStringOr("state", "")) && PactReading.KEYS.containsAll(reading.keySet()),
					"The reading: kept, his work, active: " + reading);
		}
		helper.assertTrue(BathinEntity.COUNTERPART.equals(ofPerch.getStringOr("counterpart", "")) && !ofHim.contains("counterpart"),
				"The perch's reading names him; his own does not repeat it: " + ofPerch + " " + ofHim);
		keptHeart(helper, new BlockPos(0, 1, 0), keeper.getUUID(), 30);
		ownedPylon(helper, new BlockPos(1, 1, 0), keeper.getUUID(), 0);
		keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		helper.assertValueEqual(bathin.think(level, NOON, now), Status.AWAITING_ANSWER, "A Heart with its own road waits for the answer");
		CompoundTag perchAsked = PactReading.holder(level, perch, now);
		CompoundTag himAsked = PactReading.presence(level, bathin, now);
		for (CompoundTag reading : List.of(perchAsked, himAsked)) {
			helper.assertTrue("two_roads".equals(reading.getStringOr("note", "")) && reading.getIntOr("note_count", 0) == 1
					&& VerdanceCarrying.TWO_ROADS.equals(reading.getStringOr("matter", "")) && !reading.contains("accord")
					&& PactReading.KEYS.containsAll(reading.keySet()), "His note and his matter, no answer yet: " + reading);
		}
		String all = ofPerch + " " + ofHim + " " + perchAsked + " " + himAsked;
		helper.assertTrue(!all.contains(keeper.getUUID().toString()) && !all.contains(keeper.getName().getString()), "and no player is named: " + all);
		helper.succeed();
	}

	/**
	 * GC-05: no Heart of the inviter's within 10, or nowhere to carry it (a stranger's pylon is nowhere): he does not come and
	 * nothing is spent; with a Heart and a pylon no one owns, he comes.
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 20)
	public void bathinRefusesWithNothingToCarry(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(Language.getInstance().has(CARRYING_KEY + "missing"), "the server reads the mod's words (Fabric's language loader)");
		ServerPlayer keeper = gardenHolder(helper);
		pocketFor(keeper, bathinOffer(), 1);
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = perch(helper, at, keeper);
		beside(helper, keeper, at);
		keptHeart(helper, new BlockPos(4, 2, 4), STRANGER, 0);
		keptHeart(helper, new BlockPos(13, 2, 2), keeper.getUUID(), 0);
		helper.setBlock(new BlockPos(3, 2, 6), Garden.VERDANT_HEART);
		keptGleaner(helper, new BlockPos(2, 1, 9), keeper.getUUID(), 0);
		PactMenu menu = offerOf(helper, keeper, perch, bathinOffer());
		PactStatePayload state = menu.lastSent();
		Component note = state.notes().stream().filter(line -> key(line).equals(CARRYING_KEY + "missing")).findFirst().orElse(null);
		helper.assertTrue(note != null && Integer.valueOf(10).equals(arg(note, 0)) && Integer.valueOf(10).equals(arg(note, 1)) && perch.visitor() == null,
				"No Heart of theirs within 10 (a stranger's, a dormant one and one 11 off do not count): he says why and does not come: "
						+ keys(state.notes()));
		PactStatePayload refused = send(keeper, menu, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(answered(refused, VerdanceCarrying.NOTHING_TO_CARRY) && !refused.result().get().done()
				&& said(refused).equals(CARRYING_KEY + "missing") && pocketed(keeper, 1, bathinOffer()) && recordOf(level, keeper) == null,
				"Accepting is refused with his reason; nothing spent, nothing held");
		helper.assertTrue(refused.result().get().message().getString().startsWith("\"Nothing here to carry, or nowhere to carry it.\""),
				"His words: " + refused.result().get().message().getString());
		keeper.closeContainer();
		helper.setBlock(new BlockPos(2, 1, 9), Blocks.AIR);
		keptHeart(helper, new BlockPos(3, 2, 6), keeper.getUUID(), 0);
		RateGate.forget(keeper.getUUID());
		PactStatePayload nowhere = offerOf(helper, keeper, perch, bathinOffer()).lastSent();
		helper.assertTrue(keys(nowhere.notes()).contains(CARRYING_KEY + "missing") && perch.visitor() == null,
				"A Heart of theirs and no Gleaner or pylon: nowhere to carry it, so he does not come: " + keys(nowhere.notes()));
		keeper.closeContainer();
		ownedPylon(helper, new BlockPos(6, 1, 2), STRANGER, 0);
		RateGate.forget(keeper.getUUID());
		helper.assertTrue(keys(offerOf(helper, keeper, perch, bathinOffer()).lastSent().notes()).contains(CARRYING_KEY + "missing")
				&& perch.visitor() == null, "A stranger's pylon is nowhere of theirs to carry to: he does not come");
		keeper.closeContainer();
		ownedPylon(helper, new BlockPos(6, 1, 2), null, 0);
		RateGate.forget(keeper.getUUID());
		PactStatePayload invited = offerOf(helper, keeper, perch, bathinOffer()).lastSent();
		helper.assertTrue(invited.notes().isEmpty() && invited.offers(PactRequestPayload.ACCEPT)
				&& perch.visitor() != null && level.getEntity(perch.visitor()) instanceof BathinEntity,
				"With a Heart of theirs and a Ley Pylon no one owns within 10, and no Gleaner, he comes (Dossier 2 item 2)");
		helper.succeed();
	}

	/**
	 * GC-05: he never travels. Invited, he is at his station in the tick he is summoned, in a ring of dust; sealed and ended,
	 * he departs where he is, in a second ring, and is gone the moment his departure ends, never having moved. Each ring is
	 * the shared sign his row names, shown at his foot.
	 */
	@GameTest(maxTicks = 20)
	public void bathinArrivesAndDepartsAtOnceInARingOfDust(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		pocketFor(keeper, bathinOffer(), 1);
		BlockPos at = new BlockPos(2, 1, 2);
		FIXTURES.get(BathinEntity.COUNTERPART).meet(helper, keeper, at);
		PactPerchBlockEntity perch = perch(helper, at, keeper);
		beside(helper, keeper, at);
		long now = level.getGameTime();
		try (SignWatch signs = new SignWatch(helper)) {
			PactMenu menu = offerOf(helper, keeper, perch, bathinOffer());
			BathinEntity visitor = perch.visitor() != null && level.getEntity(perch.visitor()) instanceof BathinEntity found ? found : null;
			Vec3 station = helper.absoluteVec(new Vec3(at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5));
			helper.assertTrue(visitor != null && visitor.position().equals(station) && visitor.rings() == 1 && visitor.cues().equals(List.of("arrive"))
					&& signs.of(BathinEntity.RING).size() == 1,
					"Invited, he is on his perch at once, in a ring of dust: " + (visitor == null ? "none" : visitor.position() + ", rings " + visitor.rings()));
			helper.assertTrue(answered(send(keeper, menu, PactRequestPayload.ACCEPT, 0), PactMenu.SEALED), "His pact is sealed");
			BathinEntity presence = bathin(helper, perch);
			helper.assertTrue(presence == visitor && presence.position().equals(station) && presence.rings() == 1,
					"The visitor is his pact's presence, where he arrived");
			helper.assertTrue(answered(send(keeper, menu, PactRequestPayload.END, 0), "ended") && presence.departing() && presence.rings() == 2
					&& "depart".equals(presence.cues().getLast()) && presence.position().equals(station) && signs.of(BathinEntity.RING).size() == 2,
					"Ended, he departs where he is, in a ring of dust: " + presence.cues());
			PactSpiritEntity.sweep(now + BathinEntity.Gesture.DEPART.ticks - 1);
			helper.assertTrue(!presence.isRemoved() && presence.position().equals(station), "Still there, unmoved, until his departure ends");
			PactSpiritEntity.sweep(now + BathinEntity.Gesture.DEPART.ticks);
			helper.assertTrue(presence.isRemoved(), "Then gone at once, with no path");
		}
		helper.succeed();
	}

	/**
	 * GC-05: one carry, by his own think: the Heart falls and the far Gleaner rises by the same amount in one server step, his
	 * shimmer names exactly those two reservoirs at that tick (the shared sign of work at each, nothing travelling between),
	 * his work is heard, and no ring shows.
	 */
	@GameTest(maxTicks = 20)
	public void bathinsCarryShimmersBothEndsAtOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(2, 1, 2), now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity heart = keptHeart(helper, new BlockPos(1, 2, 1), keeper.getUUID(), 8);
		GleanerBlockEntity gleaner = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		int rings = bathin.rings();
		try (SignWatch signs = new SignWatch(helper)) {
			helper.assertValueEqual(bathin.think(level, NOON, now), Status.WORKING, "He carries");
			BathinEntity.Shimmer shimmer = bathin.lastShimmer();
			helper.assertTrue(heart.verdance() == 0 && gleaner.verdance() == 8,
					"8 left the Heart and 8 reached the Gleaner 6 blocks beyond its reach, at once: " + heart.verdance() + " " + gleaner.verdance());
			helper.assertTrue(shimmer != null && shimmer.at() == level.getGameTime() && shimmer.from().equals(heart.getBlockPos())
					&& shimmer.to().equals(gleaner.getBlockPos()), "Both ends shimmered in the tick of the move, and nothing else: " + shimmer);
			helper.assertTrue(signs.at(BathinEntity.SHIMMER, new BlockPos(1, 2, 1)) == 1 && signs.at(BathinEntity.SHIMMER, new BlockPos(7, 1, 7)) == 1
					&& signs.of(Sign.FLOW).isEmpty() && signs.of(BathinEntity.RING).isEmpty(),
					"The shared sign of work at each end, nothing travelling between them: " + signs.all());
			helper.assertTrue("work".equals(bathin.cues().getLast()) && bathin.rings() == rings, "His work is heard; no ring of dust for a carry");
		}
		helper.succeed();
	}

	/** GC-05: which gesture answers which action, which loop each state shows, every cue's sound registered. */
	@GameTest(maxTicks = 20)
	public void bathinAnswersEachActionWithHisGesture(GameTestHelper helper) {
		helper.assertTrue(BathinEntity.reaction(PactRequestPayload.ACCEPT, PactMenu.SEALED, true) == BathinEntity.Gesture.ASSENT
				&& BathinEntity.reaction(PactRequestPayload.ACCEPT, VerdanceCarrying.NOTHING_TO_CARRY, false) == BathinEntity.Gesture.REFUSE
				&& BathinEntity.reaction(PactRequestPayload.DEPOSIT, PactMenu.DEPOSITED, true) == BathinEntity.Gesture.INSPECT
				&& BathinEntity.reaction(PactRequestPayload.DEPOSIT, PactMenu.NOTHING_DEPOSITED, false) == null
				&& BathinEntity.reaction(PactRequestPayload.RESUME, "resumed", true) == BathinEntity.Gesture.ASSENT
				&& BathinEntity.reaction(PactRequestPayload.RESOLVE, "resolved", true) == BathinEntity.Gesture.ASSENT
				&& BathinEntity.reaction(PactRequestPayload.RESOLVE, "resolved", false) == null
				&& BathinEntity.reaction(PactRequestPayload.RECALL, "recalled", true) == null
				&& BathinEntity.reaction(PactRequestPayload.ACCEPT, PactMenu.NOT_OFFERED, false) == null, "His answers");
		helper.assertTrue(BathinEntity.loop(Status.WORKING, false) == BathinEntity.Loop.IDLE && BathinEntity.loop(Status.IDLE, true) == BathinEntity.Loop.LISTEN
				&& BathinEntity.loop(Status.OWNER_OFFLINE, false) == BathinEntity.Loop.WAITING
				&& BathinEntity.loop(Status.UPKEEP_DUE, false) == BathinEntity.Loop.ASTIR && BathinEntity.loop(Status.UPKEEP_HELD, true) == BathinEntity.Loop.LISTEN
				&& BathinEntity.loop(Status.SUSPENDED, false) == BathinEntity.Loop.SUSPENDED && BathinEntity.loop(Status.DISABLED, true) == BathinEntity.Loop.WAITING
				&& BathinEntity.loop(Status.FINISHED, true) == BathinEntity.Loop.LISTEN && BathinEntity.loop(Status.FINISHED, false) == BathinEntity.Loop.WAITING
				&& BathinEntity.loop(Status.AWAITING_ANSWER, false) == BathinEntity.Loop.IDLE, "His loops");
		for (String cue : BathinEntity.SOUND_IDS) {
			helper.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(Jugcraft.id("bathin." + cue)), "jugcraft:bathin." + cue + " is registered");
		}
		helper.succeed();
	}

	/** GC-05: a second Bathin at the perch, or a copy of his save, is no presence: it leaves at once and carries nothing; his own carries once. */
	@GameTest(maxTicks = 20)
	public void aDuplicateBathinCarriesNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long now = dayStart(level, 1);
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(2, 1, 2), now);
		BathinEntity bathin = bathin(helper, perch);
		VerdantHeartBlockEntity heart = keptHeart(helper, new BlockPos(1, 2, 1), keeper.getUUID(), 8);
		GleanerBlockEntity gleaner = keptGleaner(helper, new BlockPos(7, 1, 7), keeper.getUUID(), 0);
		BathinEntity stranger = Workers.BATHIN.create(level, EntitySpawnReason.MOB_SUMMONED);
		stranger.setOwner(keeper.getUUID());
		stranger.setPerch(perch.getBlockPos());
		helper.assertValueEqual(stranger.think(level, NOON, now), Status.FINISHED, "A Bathin not the perch's presence leaves");
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		bathin.saveWithoutId(saved);
		BathinEntity twin = Workers.BATHIN.create(level, EntitySpawnReason.LOAD);
		twin.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
		twin.setUUID(UUID.randomUUID());
		helper.assertValueEqual(twin.think(level, NOON, now), Status.FINISHED, "A copy of his save, under another id, leaves");
		helper.assertTrue(heart.verdance() == 8 && gleaner.verdance() == 0 && stranger.lastShimmer() == null && twin.lastShimmer() == null
				&& Pacts.done(pactOf(level, perch).pact(), now) == 0 && stranger.gone() && twin.gone(),
				"Neither carried, shimmered or counted anything");
		helper.assertValueEqual(bathin.think(level, NOON, now), Status.WORKING, "The presence carries, once");
		helper.assertTrue(heart.verdance() == 0 && gleaner.verdance() == 8, "8 moved, by him alone");
		helper.succeed();
	}

	/**
	 * GC-05 (GC-S11, GC-P46): Bathin raises his matter on the kept page: its line, his two answers (the safe course first,
	 * none kept), the row saying a choice waits and naming the safe course, second; RESOLVE keeps an answer as the perch's
	 * accord, marked on the page and said in the row; it can be changed back and given again; answering spends, moves and
	 * changes nothing else, and the terms' revision does not move.
	 */
	@GameTest(maxTicks = 20)
	public void theKeptPageAsksTheMatterAndKeepsTheAnswer(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long g0 = level.getGameTime();
		VerdantHeartBlockEntity heart = heartWithItsOwnRoad(helper, keeper.getUUID(), new BlockPos(5, 2, 5));
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(1, 1, 1), g0);
		helper.assertValueEqual(bathin(helper, perch).think(level, NOON, g0), Status.AWAITING_ANSWER, "A choice waits: he says so");
		helper.assertTrue(heart.verdance() == 16, "Until it is answered he leaves the Heart with its own road alone: nothing is lost while it waits");
		PactMenu menu = open(helper, keeper, perch);
		PactStatePayload asked = menu.lastSent();
		PactStatePayload.Spirit spirit = asked.spirit();
		helper.assertTrue(asked.mode() == PactStatePayload.KEEP && asked.offers(PactRequestPayload.RESOLVE)
				&& spirit.work().equals(VerdanceCarrying.WORK) && !spirit.matter().getString().isBlank()
				&& spirit.answers().stream().map(PactStatePayload.Choice::id).toList().equals(List.of(VerdanceCarrying.YIELD, VerdanceCarrying.CLAIM))
				&& spirit.answers().stream().allMatch(answer -> answer.amount() == 0 && answer.ready()),
				"The kept page asks his matter with his two answers, the safe course first, none kept: " + spirit);
		List<String> notes = keys(asked.notes());
		helper.assertTrue(notes.get(1).equals(PACT_KEY + "row.matter.awaiting") && notes.getLast().equals(PACT_KEY + "row.end")
				&& key((Component) arg(asked.notes().get(1), 1)).equals(PACT_KEY + VerdanceCarrying.WORK + ".accord." + VerdanceCarrying.YIELD),
				"Second, the row says a choice waits and names the safe course; the page is whole (row.end last): " + notes);
		int revision = asked.revision();
		int items = carried(keeper);
		int escrow = escrowed(level, pactOf(level, perch).id());
		PactStatePayload claimed = send(keeper, menu, PactRequestPayload.RESOLVE, 1);
		helper.assertTrue(answered(claimed, PactMenu.RESOLVED) && claimed.result().get().done() && VerdanceCarrying.CLAIM.equals(perch.accord())
				&& said(claimed).equals(PACT_KEY + "answer.resolved"), "RESOLVE keeps take from it as the perch's accord");
		helper.assertTrue(claimed.spirit().answers().get(0).amount() == 0 && claimed.spirit().answers().get(1).amount() == 1
				&& keys(claimed.notes()).get(1).equals(PACT_KEY + "row.matter.answered")
				&& key((Component) arg(claimed.notes().get(1), 1)).equals(PACT_KEY + VerdanceCarrying.WORK + ".accord." + VerdanceCarrying.CLAIM),
				"The answer kept is marked, and the row says it stands");
		PactStatePayload yielded = send(keeper, menu, PactRequestPayload.RESOLVE, 0);
		helper.assertTrue(answered(yielded, PactMenu.RESOLVED) && VerdanceCarrying.YIELD.equals(perch.accord())
				&& yielded.spirit().answers().get(0).amount() == 1, "It can be changed back at any time");
		PactStatePayload again = send(keeper, menu, PactRequestPayload.RESOLVE, 0);
		helper.assertTrue(answered(again, PactMenu.RESOLVED) && again.result().get().done() && VerdanceCarrying.YIELD.equals(perch.accord()),
				"The same answer again is kept as it was");
		helper.assertTrue(again.revision() == revision && carried(keeper) == items && escrowed(level, pactOf(level, perch).id()) == escrow
				&& heart.verdance() == 16, "Answering moves no term, item, offering or Verdance");
		keeper.closeContainer();
		helper.succeed();
	}

	/**
	 * GC-05 (GB27, GC-P44): while a choice waits and its holder is near, the presence says AWAITING_ANSWER: its roster entry
	 * keeps that saved status and its row says "awaiting your answer" with the waiting icon; Jade says it with the matter;
	 * it is a reason shown with a sign of want. With its holder away it says that instead, and back near it asks again (nothing
	 * was settled for them); once answered it no longer awaits and Jade names the answer. Stolas's pact never awaits one.
	 */
	@GameTest(maxTicks = 20)
	public void theRosterSaysAwaitingYourAnswer(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long g0 = level.getGameTime();
		BlockPos at = new BlockPos(1, 1, 1);
		heartWithItsOwnRoad(helper, keeper.getUUID(), new BlockPos(5, 2, 5));
		PactPerchBlockEntity perch = bathinPact(helper, keeper, at, g0);
		BathinEntity bathin = bathin(helper, perch);
		BoundWill record = pactOf(level, perch);
		helper.assertValueEqual(bathin.think(level, NOON, g0), Status.AWAITING_ANSWER, "A choice waits");
		WorkerRoster.Entry entry = WorkerRoster.of(level.getServer()).workers(keeper.getUUID()).get(bathin.getUUID());
		helper.assertTrue(entry != null && "awaiting_answer".equals(entry.status()) && Status.fromId(entry.status()) == Status.AWAITING_ANSWER,
				"The roster keeps the saved status awaiting_answer: " + entry);
		RosterStatePayload.Row row = rowOf(roster(keeper), record.id());
		helper.assertTrue(row != null && key(row.waiting()).equals("compose.jugcraft.worker.status.awaiting_answer")
				&& row.icon() == RosterStatePayload.WAITING, "Its row says awaiting your answer, with the waiting icon: " + row);
		helper.assertTrue(WorkerEntity.sign(Status.AWAITING_ANSWER) == Sign.WANT && !Status.AWAITING_ANSWER.active,
				"A reason, not an intent, shown with a sign of want");
		CompoundTag reading = PactReading.holder(level, perch, g0);
		CompoundTag ofHim = PactReading.presence(level, bathin, g0);
		helper.assertTrue("awaiting_answer".equals(reading.getStringOr("waiting", "")) && VerdanceCarrying.TWO_ROADS.equals(reading.getStringOr("matter", ""))
				&& !reading.contains("accord") && VerdanceCarrying.TWO_ROADS.equals(ofHim.getStringOr("matter", ""))
				&& PactReading.KEYS.containsAll(reading.keySet()) && PactReading.KEYS.containsAll(ofHim.keySet())
				&& !(reading + " " + ofHim).contains(keeper.getUUID().toString()),
				"Jade: its presence awaits your answer, the matter, no answer yet, no player named: " + reading + " " + ofHim);
		place(helper, keeper, 100.5, 1.0, 100.5);
		helper.assertValueEqual(bathin.think(level, NOON, g0 + 20), Status.OWNER_OFFLINE, "Its holder away, it says that instead (GC-S7)");
		beside(helper, keeper, at);
		helper.assertValueEqual(bathin.think(level, NOON, g0 + 40), Status.AWAITING_ANSWER, "Back near, the choice still waits: nothing was settled for them");
		helper.assertTrue(PactAccords.resolve(level, keeper, perch, VerdanceCarrying.WORK, VerdanceCarrying.CLAIM, g0 + 40), "The holder answers at the perch");
		Status after = bathin.think(level, NOON, g0 + 60);
		RosterStatePayload.Row answered = rowOf(roster(keeper), record.id());
		CompoundTag kept = PactReading.holder(level, perch, g0 + 60);
		helper.assertTrue(after != Status.AWAITING_ANSWER && answered != null && !key(answered.waiting()).endsWith(".awaiting_answer")
				&& VerdanceCarrying.CLAIM.equals(kept.getStringOr("accord", "")) && !"awaiting_answer".equals(kept.getStringOr("waiting", "")),
				"Answered, it no longer awaits; the roster and Jade say so, Jade with the answer: " + after + " " + kept);
		ServerPlayer reckoner = regarded(helper);
		PactPerchBlockEntity his = stolasPact(helper, reckoner, new BlockPos(6, 1, 1), g0);
		helper.assertTrue(stolas(level, his).think(level, NOON, g0) != Status.AWAITING_ANSWER
				&& PactAccords.matter(level, his, pactOf(level, his), g0) == null, "Stolas raises no matter and never awaits an answer");
		helper.succeed();
	}

	/**
	 * GC-05 (GC-P42; GE3): an answer is the perch's optional key "accord", the stamp still 1, and reads back after a
	 * restart: the perch reloaded keeps it, its presence keeps to that course and does not ask again. A copy of the perch
	 * carries the key but reads nothing: no answer, no matter, no RESOLVE (its page is closed); the original keeps its own.
	 */
	@GameTest(maxTicks = 20)
	public void anAnsweredAccordSurvivesARestart(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long g0 = level.getGameTime();
		heartWithItsOwnRoad(helper, keeper.getUUID(), new BlockPos(5, 2, 5));
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(1, 1, 1), g0);
		PactService carrying = PactServices.get(VerdanceCarrying.WORK);
		CompoundTag unanswered = perch.saveWithoutMetadata(level.registryAccess());
		helper.assertTrue(!unanswered.contains("accord") && unanswered.getIntOr(Saved.VERSION, 0) == 1, "Unanswered: no key, version 1: " + unanswered);
		for (String answer : List.of(VerdanceCarrying.YIELD, VerdanceCarrying.CLAIM)) {
			helper.assertTrue(PactAccords.resolve(level, keeper, perch, VerdanceCarrying.WORK, answer, g0), "Answered " + answer);
			CompoundTag saved = perch.saveWithoutMetadata(level.registryAccess());
			helper.assertTrue(answer.equals(saved.getStringOr("accord", "")) && saved.getIntOr(Saved.VERSION, 0) == 1,
					"Saved as the perch's optional accord, the stamp still 1: " + saved);
			PactPerchBlockEntity read = new PactPerchBlockEntity(perch.getBlockPos(), perch.getBlockState());
			read.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
			helper.assertTrue(answer.equals(read.accord()), "and read back: " + read.accord());
			perch.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
			helper.assertTrue(answer.equals(PactAccords.answer(level, perch)) && answer.equals(PactAccords.course(level, perch, carrying))
					&& bathin(helper, perch).think(level, NOON, g0) != Status.AWAITING_ANSWER, "After the restart it keeps to " + answer + " and does not ask again");
		}
		BlockPos copyAt = new BlockPos(3, 1, 1);
		PactPerchBlockEntity copy = perch(helper, copyAt, keeper);
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), perch.saveWithoutMetadata(level.registryAccess())));
		CompoundTag copied = PactReading.holder(level, copy, g0);
		helper.assertTrue(VerdanceCarrying.CLAIM.equals(copy.accord()) && PactAccords.answer(level, copy) == null && VerdanceCarrying.YIELD.equals(PactAccords.course(level, copy, carrying))
				&& "copy".equals(copied.getStringOr("standing", "")) && !copied.contains("accord") && !copied.contains("matter"),
				"A copy carries the key but reads nothing (GE3): " + copied);
		beside(helper, keeper, copyAt);
		PactMenu atCopy = open(helper, keeper, copy);
		helper.assertTrue(atCopy.lastSent().mode() == PactStatePayload.CLOSED && atCopy.lastSent().spirit().answers().isEmpty()
				&& answered(send(keeper, atCopy, PactRequestPayload.RESOLVE, 0), PactMenu.NOT_OFFERED)
				&& VerdanceCarrying.CLAIM.equals(PactAccords.answer(level, perch)), "No answer is given at a copy; the original keeps its own");
		keeper.closeContainer();
		helper.succeed();
	}

	/**
	 * GC-05 (GC-P42): an accord this version does not know (a word no service has, or another service's) reads as
	 * unanswered: the matter is asked again and the safe course kept, the unknown value saved back as written until the
	 * holder answers; one longer than 16 characters, or at a perch keeping no pact, is not read at all.
	 */
	@GameTest(maxTicks = 20)
	public void anUnknownAccordReadsAsUnanswered(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long g0 = level.getGameTime();
		VerdantHeartBlockEntity heart = heartWithItsOwnRoad(helper, keeper.getUUID(), new BlockPos(5, 2, 5));
		PactPerchBlockEntity perch = bathinPact(helper, keeper, new BlockPos(1, 1, 1), g0);
		PactService carrying = PactServices.get(VerdanceCarrying.WORK);
		for (String unknown : List.of("sideways", "prudent")) {
			CompoundTag saved = perch.saveWithoutMetadata(level.registryAccess());
			saved.putString("accord", unknown);
			perch.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
			helper.assertTrue(unknown.equals(perch.accord()) && PactAccords.answer(level, perch) == null
					&& VerdanceCarrying.YIELD.equals(PactAccords.course(level, perch, carrying)), "Kept as written, read as unanswered: " + unknown);
			helper.assertValueEqual(bathin(helper, perch).think(level, NOON, g0), Status.AWAITING_ANSWER, "so the matter is asked again (" + unknown + ")");
			helper.assertTrue(heart.verdance() == 16 && unknown.equals(perch.saveWithoutMetadata(level.registryAccess()).getStringOr("accord", "")),
					"the safe course kept, and the value saved back as it was, for the build that wrote it");
		}
		PactMenu menu = open(helper, keeper, perch);
		PactStatePayload asked = menu.lastSent();
		helper.assertTrue(asked.spirit().answers().stream().allMatch(answer -> answer.amount() == 0)
				&& keys(asked.notes()).get(1).equals(PACT_KEY + "row.matter.awaiting"), "Its page asks, none marked");
		helper.assertTrue(answered(send(keeper, menu, PactRequestPayload.RESOLVE, 0), PactMenu.RESOLVED) && VerdanceCarrying.YIELD.equals(perch.accord())
				&& VerdanceCarrying.YIELD.equals(perch.saveWithoutMetadata(level.registryAccess()).getStringOr("accord", "")), "An answer replaces it");
		keeper.closeContainer();
		CompoundTag tooLong = perch.saveWithoutMetadata(level.registryAccess());
		tooLong.putString("accord", "x".repeat(PactAccords.MAX_ACCORD + 1));
		perch.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tooLong));
		helper.assertTrue(perch.accord() == null && !perch.saveWithoutMetadata(level.registryAccess()).contains("accord"),
				"Longer than 16 characters, it is not read and not written back");
		PactPerchBlockEntity empty = perch(helper, new BlockPos(3, 1, 1), keeper);
		CompoundTag stray = empty.saveWithoutMetadata(level.registryAccess());
		stray.putString("accord", VerdanceCarrying.CLAIM);
		empty.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), stray));
		helper.assertTrue(empty.accord() == null && !empty.saveWithoutMetadata(level.registryAccess()).contains("accord"),
				"At a perch keeping no pact, an accord is not read");
		helper.succeed();
	}

	/**
	 * GC-05 (GE1): only the holder answers, in person. Another player at the perch reads it closed, sees no answers, and
	 * their RESOLVE is not offered; a party member who shares the pact's service is refused alike; a stand-in carrying the
	 * holder's identity is never heard; an answer for another work's page, or one Bathin does not know, is refused; nothing
	 * is written until the holder answers.
	 */
	@GameTest(maxTicks = 20)
	public void aResolveFromAnotherPlayerIsRefused(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		ServerPlayer stranger = gardenHolder(helper);
		ServerPlayer friend = gardenHolder(helper);
		long g0 = level.getGameTime();
		BlockPos at = new BlockPos(1, 1, 1);
		heartWithItsOwnRoad(helper, keeper.getUUID(), new BlockPos(5, 2, 5));
		PactPerchBlockEntity perch = bathinPact(helper, keeper, at, g0);
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(keeper.getUUID(), friend.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(friend.getUUID(), ms) == PartyManager.Result.OK, "The holder and a friend form a party");
			for (ServerPlayer other : List.of(stranger, friend)) {
				beside(helper, other, at);
				PactMenu menu = open(helper, other, perch);
				PactStatePayload shown = menu.lastSent();
				helper.assertTrue(shown.mode() == PactStatePayload.CLOSED && shown.spirit().answers().isEmpty() && !shown.offers(PactRequestPayload.RESOLVE),
						"Another player reads the perch closed, with no answers");
				PactStatePayload sent = send(other, menu, PactRequestPayload.RESOLVE, 1);
				helper.assertTrue(answered(sent, PactMenu.NOT_OFFERED) && !sent.result().get().done() && perch.accord() == null,
						"Their RESOLVE is not offered and writes nothing");
				helper.assertTrue(!PactAccords.resolve(level, other, perch, VerdanceCarrying.WORK, VerdanceCarrying.CLAIM, g0) && perch.accord() == null,
						"nor does the accord take their answer directly");
				other.closeContainer();
			}
			FakePlayer standIn = FakePlayer.get(level, new GameProfile(keeper.getUUID(), "[Concordance]"));
			helper.assertTrue(!PactAccords.resolve(level, standIn, perch, VerdanceCarrying.WORK, VerdanceCarrying.CLAIM, g0)
					&& PactMenu.receive(standIn, new PactRequestPayload(0, 1, PactRequestPayload.RESOLVE, 1, 1)) == null && perch.accord() == null,
					"A stand-in with the holder's identity never answers, nor is it heard");
			helper.assertTrue(!PactAccords.resolve(level, keeper, perch, StrainTending.WORK, VerdanceCarrying.CLAIM, g0)
					&& !PactAccords.resolve(level, keeper, perch, VerdanceCarrying.WORK, "sideways", g0) && perch.accord() == null,
					"An answer for another work's page, or one Bathin does not know, is refused");
			helper.assertValueEqual(bathin(helper, perch).think(level, NOON, g0), Status.AWAITING_ANSWER, "So the choice still waits on its holder");
			helper.assertTrue(PactAccords.resolve(level, keeper, perch, VerdanceCarrying.WORK, VerdanceCarrying.CLAIM, g0) && VerdanceCarrying.CLAIM.equals(perch.accord()),
					"The holder, in person, answers");
		} finally {
			parties.adminDisband(keeper.getUUID());
		}
		helper.succeed();
	}

	/**
	 * GC-05 (GC-P43): RESOLVE is taken only on the kept page that sent answers. On a negotiation, the chooser, the kept page
	 * of a pact whose service raises no choice (Stolas's) and a seat page no answers are sent, RESOLVE is not offered, and
	 * it is answered not offered and writes nothing (the closed page is aResolveFromAnotherPlayerIsRefused's). On Bathin's
	 * kept page an index past the two answers sent is not offered; one past the screen's bound is dropped before anything.
	 */
	@GameTest(maxTicks = 20)
	public void aResolveOutsideTheKeepModeIsDropped(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		long g0 = level.getGameTime();
		ServerPlayer novice = ready(level, TestPlayers.survivor(helper));
		PactPerchBlockEntity first = perch(helper, new BlockPos(1, 1, 1), novice);
		beside(helper, novice, new BlockPos(1, 1, 1));
		PactMenu negotiating = open(helper, novice, first);
		ServerPlayer chooser = ready(level, gardenHolder(helper));
		PactPerchBlockEntity second = perch(helper, new BlockPos(3, 1, 1), chooser);
		beside(helper, chooser, new BlockPos(3, 1, 1));
		PactMenu choosing = open(helper, chooser, second);
		ServerPlayer reckoner = ready(level, TestPlayers.survivor(helper));
		PactPerchBlockEntity his = stolasPact(helper, reckoner, new BlockPos(5, 1, 1), g0);
		PactMenu keepingStolas = open(helper, reckoner, his);
		ServerPlayer keeper = gardenHolder(helper);
		heartWithItsOwnRoad(helper, keeper.getUUID(), new BlockPos(5, 2, 5));
		bathinPact(helper, keeper, new BlockPos(1, 1, 6), g0);
		helper.setBlock(new BlockPos(1, 1, 6), Blocks.AIR);
		PactPerchBlockEntity seat = perch(helper, new BlockPos(3, 1, 6), keeper);
		beside(helper, keeper, new BlockPos(3, 1, 6));
		PactMenu seating = open(helper, keeper, seat);
		record Shown(String name, ServerPlayer player, PactMenu menu, int mode) {
		}
		for (Shown page : List.of(new Shown("negotiate", novice, negotiating, PactStatePayload.NEGOTIATE),
				new Shown("invite", chooser, choosing, PactStatePayload.INVITE),
				new Shown("keep without a choice", reckoner, keepingStolas, PactStatePayload.KEEP),
				new Shown("seat", keeper, seating, PactStatePayload.SEAT))) {
			PactStatePayload before = page.menu().lastSent();
			helper.assertTrue(before.mode() == page.mode() && before.spirit().answers().isEmpty() && !before.offers(PactRequestPayload.RESOLVE),
					page.name() + ": no answers are sent and RESOLVE is not offered");
			PactStatePayload sent = send(page.player(), page.menu(), PactRequestPayload.RESOLVE, 0);
			helper.assertTrue(answered(sent, PactMenu.NOT_OFFERED) && !sent.result().get().done() && sent.mode() == page.mode(),
					page.name() + ": RESOLVE is answered not offered and the page stays");
		}
		helper.assertTrue(first.accord() == null && second.accord() == null && his.accord() == null && seat.accord() == null,
				"Nothing was written anywhere");
		helper.assertTrue(answered(send(keeper, seating, PactRequestPayload.SEAT, 0), AgreementHolder.Seat.SEATED.id), "Seated at the new perch");
		bathin(helper, seat).think(level, NOON, g0);
		PactStatePayload kept = send(keeper, seating, PactRequestPayload.CHOOSE, 0);
		helper.assertTrue(kept.mode() == PactStatePayload.KEEP && kept.spirit().answers().size() == 2 && kept.offers(PactRequestPayload.RESOLVE),
				"His kept page now sends two answers");
		helper.assertTrue(answered(send(keeper, seating, PactRequestPayload.RESOLVE, 2), PactMenu.NOT_OFFERED) && seat.accord() == null,
				"Past the two answers sent: not offered");
		helper.assertTrue(request(keeper, seating.containerId, seating.lastSequence() + 1, PactRequestPayload.RESOLVE, PactStatePayload.MAX_CHOICES,
				seating.lastSent().revision()) == null && seat.accord() == null, "Past the screen's bound: dropped before anything");
		for (ServerPlayer player : List.of(novice, chooser, reckoner, keeper)) {
			player.closeContainer();
		}
		helper.succeed();
	}

	/**
	 * GC-05 (GC-P42, GC-P46): an answer belongs to the pact kept at its perch. Ended there, ended from the roster, or left
	 * without its perch, it is gone (neither read nor saved); a pact sealed again at that perch, or seated at another,
	 * starts unanswered and asks again.
	 */
	@GameTest(maxTicks = 20)
	public void anAccordIsClearedWhenItsPactEnds(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		long g0 = level.getGameTime();
		BlockPos at = new BlockPos(1, 1, 1);
		heartWithItsOwnRoad(helper, keeper.getUUID(), new BlockPos(5, 2, 5));
		PactPerchBlockEntity perch = bathinPact(helper, keeper, at, g0);
		bathin(helper, perch).think(level, NOON, g0);
		PactMenu menu = open(helper, keeper, perch);
		helper.assertTrue(answered(send(keeper, menu, PactRequestPayload.RESOLVE, 1), PactMenu.RESOLVED) && VerdanceCarrying.CLAIM.equals(perch.accord()), "Answered: take from it");
		helper.assertTrue(answered(send(keeper, menu, PactRequestPayload.END, 0), AgreementHolder.Ended.ENDED.name().toLowerCase(Locale.ROOT))
				&& perch.accord() == null && !perch.saveWithoutMetadata(level.registryAccess()).contains("accord"), "Ended at the perch: the answer is gone");
		keeper.closeContainer();
		helper.assertValueEqual(perch.holder().seal(level, keeper, PactPerchBlockEntity.sealing(bathinOffer(), null, null), g0).outcome(),
				Pacts.Outcome.DONE, "Sealed again at the same perch");
		helper.assertTrue(perch.accord() == null && PactAccords.answer(level, perch) == null
				&& bathin(helper, perch).think(level, NOON, g0) == Status.AWAITING_ANSWER, "A new pact starts unanswered and asks again");
		helper.assertTrue(PactAccords.resolve(level, keeper, perch, VerdanceCarrying.WORK, VerdanceCarrying.CLAIM, g0), "Answered again");
		UUID id = pactOf(level, perch).id();
		helper.assertTrue(answered(act(keeper, rowOf(roster(keeper), id), RosterRequestPayload.END), "ended") && perch.accord() == null
				&& !perch.saveWithoutMetadata(level.registryAccess()).contains("accord"), "Ended from the roster, its perch loaded: the answer is gone");
		helper.assertValueEqual(perch.holder().seal(level, keeper, PactPerchBlockEntity.sealing(bathinOffer(), null, null), g0).outcome(),
				Pacts.Outcome.DONE, "Sealed a third time");
		helper.assertTrue(PactAccords.resolve(level, keeper, perch, VerdanceCarrying.WORK, VerdanceCarrying.CLAIM, g0), "Answered a third time");
		helper.setBlock(at, Blocks.AIR);
		BlockPos movedAt = new BlockPos(1, 1, 6);
		PactPerchBlockEntity moved = perch(helper, movedAt, keeper);
		beside(helper, keeper, movedAt);
		PactMenu seating = open(helper, keeper, moved);
		helper.assertTrue(answered(send(keeper, seating, PactRequestPayload.SEAT, 0), AgreementHolder.Seat.SEATED.id) && moved.accord() == null
				&& bathin(helper, moved).think(level, NOON, g0) == Status.AWAITING_ANSWER, "Left without its perch and seated at another, it starts unanswered");
		keeper.closeContainer();
		helper.succeed();
	}

	/**
	 * GC-05 (GC-P43, GC-P44): the wire's additions. RESOLVE is action 10, appended; a state's spirit carries its matter and
	 * at most three answers, cut before sending and read back whole; the two-part spirit has neither; every service's
	 * answers are at most three short saved words, Bathin's the safe course first; AWAITING_ANSWER is appended, a reason.
	 */
	@GameTest(maxTicks = 20)
	public void theAccordWireIsBounded(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(PactRequestPayload.RESOLVE == 10 && PactRequestPayload.ACTIONS.get(10).equals("resolve")
				&& PactRequestPayload.ACTIONS.size() == 11 && PactStatePayload.MAX_ANSWERS == 3, "RESOLVE appended as 10; three answers at most");
		List<PactStatePayload.Choice> four = List.of(VerdanceCarrying.YIELD, VerdanceCarrying.CLAIM, "third", "fourth").stream()
				.map(id -> new PactStatePayload.Choice(id, Component.literal(id), 0, true)).toList();
		PactStatePayload.Spirit spirit = new PactStatePayload.Spirit(BathinEntity.SIGN, VerdanceCarrying.WORK,
				Component.translatable(PACT_KEY + VerdanceCarrying.WORK + ".question"), four);
		PactStatePayload.Spirit plain = new PactStatePayload.Spirit("jugcraft:a", "b");
		helper.assertTrue(spirit.answers().size() == 3 && plain.matter().getString().isEmpty() && plain.answers().isEmpty(),
				"Cut to three; the two-part spirit has no matter and no answers");
		PactStatePayload state = new PactStatePayload(1, 1, PactStatePayload.KEEP, Component.empty(), List.of(), List.of(), List.of(), List.of(),
				List.of(), List.of(), 0, PactStatePayload.mask(PactRequestPayload.RESOLVE), Optional.empty(), spirit);
		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
		PactStatePayload.CODEC.encode(buffer, state);
		PactStatePayload back = PactStatePayload.CODEC.decode(buffer);
		helper.assertTrue(back.spirit().equals(spirit) && buffer.readableBytes() == 0 && back.offers(PactRequestPayload.RESOLVE),
				"Read back whole: " + back.spirit());
		for (PactService service : PactServices.all()) {
			List<String> accords = service.accords();
			helper.assertTrue(accords.size() <= PactStatePayload.MAX_ANSWERS && accords.stream().distinct().count() == accords.size()
					&& accords.stream().allMatch(value -> value.matches("[a-z_]{1," + PactAccords.MAX_ACCORD + "}")),
					service.work() + "'s answers are at most three short saved words: " + accords);
		}
		helper.assertTrue(PactServices.get(VerdanceCarrying.WORK).accords().equals(List.of(VerdanceCarrying.YIELD, VerdanceCarrying.CLAIM))
				&& PactServices.get(StarReckoning.WORK).accords().isEmpty() && PactServices.get(StrainTending.WORK).accords().isEmpty(),
				"Bathin's: leave it, then take from it; Star-reckoning and strain tending raise no choice");
		Status[] statuses = Status.values();
		helper.assertTrue(statuses[statuses.length - 1] == Status.AWAITING_ANSWER && Status.fromId("awaiting_answer") == Status.AWAITING_ANSWER
				&& !Status.AWAITING_ANSWER.active, "awaiting_answer is appended, a reason, read back from its id");
		helper.succeed();
	}

	// ---------------------------------------------------------------- Concordance garden cohort, GC-06b: the standing station and the work target, shared

	/** GC-06b: stone under the whole structure (y 0): ground for a standing spirit beside its perch (Station.BESIDE's sturdy floor). */
	static void floor(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
	}

	/**
	 * GC-06b (GC-P62, GC-P66): the reaching bone's pose (PactSpiritEntity.posed, the renderer's arithmetic): a bed three blocks
	 * south of a body is drawn where GeckoLib's turn of the model by 180 - bodyYaw puts it, at every bearing and scale; a
	 * target past TARGET_REACH is reached that far toward it and no further.
	 */
	@GameTest(maxTicks = 20)
	public void theFamiliarHookReachesItsTargetAtEveryBearing(GameTestHelper helper) {
		Vec3 from = new Vec3(100.5, 64.0, 100.5);
		Vec3 to = PactSpiritEntity.hover(new BlockPos(100, 64, 103));
		float[] yaws = {0.0F, 90.0F, 180.0F, -90.0F};
		Vec3[] expected = {new Vec3(0.0, 2.5, -3.0), new Vec3(-3.0, 2.5, 0.0), new Vec3(0.0, 2.5, 3.0), new Vec3(3.0, 2.5, 0.0)};
		for (int i = 0; i < yaws.length; i++) {
			Vec3 posed = PactSpiritEntity.posed(from, yaws[i], to, 1.0F);
			helper.assertTrue(posed.distanceTo(expected[i]) < 1.0E-6, "Facing yaw " + yaws[i] + ", a bed 3 south is drawn at " + expected[i] + ": " + posed);
		}
		for (int yaw = -180; yaw < 180; yaw += 15) {
			for (float scale : new float[] {0.5F, 1.0F, 1.4F}) {
				Vec3 posed = PactSpiritEntity.posed(from, yaw, to, scale);
				double turn = Math.toRadians(180.0 - yaw);
				Vec3 drawn = new Vec3(posed.x * Math.cos(turn) + posed.z * Math.sin(turn), posed.y,
						-posed.x * Math.sin(turn) + posed.z * Math.cos(turn)).scale(scale);
				helper.assertTrue(from.add(drawn).distanceTo(to) < 1.0E-6,
						"Turned back as GeckoLib draws it, the pose lands on the bed (yaw " + yaw + ", scale " + scale + ")");
			}
		}
		Vec3 far = new Vec3(100.5, 64.0, 130.5);
		Vec3 reached = PactSpiritEntity.reach(from, far);
		helper.assertTrue(Math.abs(reached.distanceTo(from) - PactSpiritEntity.TARGET_REACH) < 1.0E-9 && reached.x == from.x && reached.z > from.z
				&& Math.abs(PactSpiritEntity.posed(from, 0.0F, far, 1.0F).length() - PactSpiritEntity.TARGET_REACH) < 1.0E-9,
				"Thirty blocks off, it reaches TARGET_REACH toward it and no further: " + reached);
		helper.succeed();
	}

	// GC-06 unit 3
	// ---------------------------------------------------------------- Concordance garden cohort, GC-06: Marax (Dossier 3)

	static final String READING_KEY = "message.jugcraft.concordance.pact.habitat_reading.";
	/** Marax's perch in his tests: on a stone floor (floor), his station two blocks west of it (his inviter stands two south). */
	static final BlockPos MARAX_PERCH = new BlockPos(3, 1, 2);

	static {
		// GC-06 (SG12): Marax comes where a planted bed the inviter keeps lies within 8 of the perch: one in the floor beside the
		// perch's grid cell (perchFor leaves x - 1 free), its Sunpetal at the perch's height, so every shared case runs for him
		// with no edit (none reads where he stands).
		FIXTURES.put(MaraxEntity.COUNTERPART, (helper, player, perch) -> {
			BlockPos bed = perch.offset(-1, -1, 0);
			keptBed(helper, bed, player.getUUID(), 0);
			helper.setBlock(bed.above(), ((OrganismCropBlock) Garden.SUNPETAL_CROP).getStateForAge(OrganismCropBlock.MAX_AGE));
			return List.of(bed, bed.above());
		});
	}

	/** Marax's offer as the server loaded it (data/jugcraft/concordance/pact/marax.json). */
	static PactDefinition maraxOffer() {
		PactDefinition offer = PactPerchBlockEntity.definition(MaraxEntity.COUNTERPART);
		if (offer == null) {
			throw new AssertionError("Marax's offer is loaded");
		}
		return offer;
	}

	/** Marax's pact sealed for {@code holder} at a perch they placed at {@code at}, at game time {@code now}; they stand beside it. */
	static PactPerchBlockEntity maraxPact(GameTestHelper helper, ServerPlayer holder, BlockPos at, long now) {
		PactPerchBlockEntity perch = perch(helper, at, holder);
		beside(helper, holder, at);
		helper.assertValueEqual(perch.holder().seal(helper.getLevel(), holder, PactPerchBlockEntity.sealing(maraxOffer(), null, null), now).outcome(),
				Pacts.Outcome.DONE, "Marax's pact is sealed at the perch");
		return perch;
	}

	/** The Marax kept at {@code perch} (its presence), asserted there. */
	static MaraxEntity marax(GameTestHelper helper, PactPerchBlockEntity perch) {
		PactSpiritEntity<?> found = perch.presence(helper.getLevel());
		helper.assertTrue(found instanceof MaraxEntity, "Marax is at his perch: " + found);
		return (MaraxEntity) found;
	}

	static HabitatReading.Memory familiar(MaraxEntity marax) {
		return marax.serviceMemory(HabitatReading.Memory.class, HabitatReading.Memory::new);
	}

	/** An awake, wet Verdant Bed at {@code at} kept by {@code keeper}, holding {@code nutrients}, with {@code crop} ripe on it. */
	static VerdantBedBlockEntity plantedBed(GameTestHelper helper, BlockPos at, UUID keeper, Block crop, int nutrients) {
		VerdantBedBlockEntity bed = keptBed(helper, at, keeper, 0);
		bed.setNutrients(nutrients);
		helper.setBlock(at.above(), ((OrganismCropBlock) crop).getStateForAge(OrganismCropBlock.MAX_AGE));
		return bed;
	}

	/** Stone round {@code plant} and over it, so no sky reaches it (ConcordanceGardenGameTests.shut's rule; that one is private). */
	static void shutIn(GameTestHelper helper, BlockPos plant) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (dx != 0 || dz != 0) {
					helper.setBlock(plant.offset(dx, 0, dz), Blocks.STONE);
				}
				helper.setBlock(plant.offset(dx, 1, dz), Blocks.STONE);
			}
		}
	}

	/** {@code player} asks Marax at {@code perch} for a reading at noon and game time {@code now}, as his page's Recall does. */
	static PactMenu.Answered reading(GameTestHelper helper, ServerPlayer player, PactPerchBlockEntity perch, long now) {
		return HabitatReading.read(helper.getLevel(), player, perch.holder(), NOON, now);
	}

	/** What a reading must leave alone on {@code beds}: each bed's nutrients, moisture, strain, graft, verdict and crop. */
	static List<String> unchanged(GameTestHelper helper, List<VerdantBedBlockEntity> beds) {
		ServerLevel level = helper.getLevel();
		List<String> out = new ArrayList<>();
		for (VerdantBedBlockEntity bed : beds) {
			BlockPos pos = bed.getBlockPos();
			Organism organism = HabitatReading.planted(level, pos);
			Habitat habitat = bed.cachedHabitat(level);
			out.add(pos.toShortString() + " " + bed.nutrients() + " " + bed.moisture() + " " + bed.strain() + " " + bed.graft() + " "
					+ (organism == null || habitat == null ? "-" : Verdict.of(bed.niche(organism), habitat)) + " " + level.getBlockState(pos.above()));
		}
		return out;
	}

	/**
	 * A reading's remedy (its sentence's fourth argument: the factor, its value, the value to bring it to, its ideal range) and
	 * its clause (its fifth: that alone moves it on, or one of n). The reading's arguments are crop, bed, growth, remedy,
	 * clause, graft, proud (HabitatReading.said).
	 */
	static @Nullable Component remedy(PactMenu.Answered answered) {
		return arg(answered.said(), 3) instanceof Component found ? found : null;
	}

	static @Nullable Component clause(PactMenu.Answered answered) {
		return arg(answered.said(), 4) instanceof Component found ? found : null;
	}

	/**
	 * GC-06 (CT GC-06; Dossier 3 items 3 and 8): a stalled Sunpetal at light 9 is the worse of two beds; the reading names its
	 * light, 9 to 11, alone, and the Dewlit graft, and his familiar's target is that bed, which he faces while his work gesture
	 * plays; nothing on either bed changes and no sample is taken; the quota counts each reading; a third the same day is
	 * refused and counts nothing.
	 */
	@GameTest(maxTicks = 80)
	public void aReadingNamesOneCorrectionAndChangesNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		BlockPos dimAt = new BlockPos(5, 1, 5);
		VerdantBedBlockEntity dim = plantedBed(helper, dimAt, keeper.getUUID(), Garden.SUNPETAL_CROP, 4);
		shutIn(helper, dimAt.above());
		helper.setBlock(dimAt.north(), Blocks.STONE);
		helper.setBlock(dimAt.above().north(), Blocks.SOUL_LANTERN);
		// The lantern's open side: no sky through it (the critique's item 3).
		helper.setBlock(dimAt.above().north(2), Blocks.STONE);
		VerdantBedBlockEntity bright = plantedBed(helper, new BlockPos(2, 1, 5), keeper.getUUID(), Garden.SUNPETAL_CROP, 4);
		ConcordanceGardenGameTests.afterSampling(helper, List.of(dim, bright), () -> {
			helper.assertValueEqual(level.getRawBrightness(helper.absolutePos(dimAt.above()), 0), 9,
					"The dim Sunpetal's light is the lantern's alone: 9, no sky");
			long now = level.getGameTime();
			MaraxEntity marax = marax(helper, perch);
			open(helper, keeper, perch);
			List<String> before = unchanged(helper, List.of(dim, bright));
			int used = Garden.budget(level).used(now);
			PactMenu.Answered first = reading(helper, keeper, perch, now);
			helper.assertTrue(first.done() && HabitatReading.READ.equals(first.reason()) && key(first.said()).equals(READING_KEY + "read"),
					"A reading is given: " + first.said().getString());
			helper.assertValueEqual(arg(first.said(), 1), helper.absolutePos(dimAt).toShortString(), "It names the dim bed, the worse");
			helper.assertValueEqual(key(clause(first)), READING_KEY + "advice.alone", "One change alone moves it on");
			Component remedy = remedy(first);
			helper.assertTrue(key(remedy).equals(READING_KEY + "remedy.light.more") && Integer.valueOf(9).equals(arg(remedy, 1))
					&& Integer.valueOf(11).equals(arg(remedy, 2)) && Integer.valueOf(13).equals(arg(remedy, 3))
					&& Integer.valueOf(15).equals(arg(remedy, 4)), "Its light is 9; give it 11 (best 13 to 15): " + remedy);
			helper.assertValueEqual(key((Component) arg(first.said(), 5)), READING_KEY + "advice.graft", "The Dewlit graft is named");
			helper.assertTrue(marax.target().equals(Optional.of(helper.absolutePos(dimAt))), "His familiar's target is the bed it names");
			marax.think(level, NOON, now);
			helper.assertTrue(Math.abs(Mth.wrapDegrees(marax.getYRot()
					- PactSpiritEntity.bearing(marax.position(), Vec3.atCenterOf(helper.absolutePos(dimAt)), 0.0F))) <= 1.0F
					&& marax.target().equals(Optional.of(helper.absolutePos(dimAt))),
					"While his work gesture plays he faces the bed his familiar names (GC-02), and it is still shown: " + marax.getYRot());
			helper.assertValueEqual(Garden.budget(level).used(now), used, "No sample was taken");
			helper.assertValueEqual(Pacts.done(pactOf(level, perch).pact(), now), 1, "The reading counted once");
			PactMenu.Answered second = reading(helper, keeper, perch, now);
			helper.assertTrue(second.done() && Pacts.done(pactOf(level, perch).pact(), now) == 2, "A second the same day counts again");
			PactMenu.Answered third = reading(helper, keeper, perch, now);
			helper.assertTrue(!third.done() && Pacts.Reckoning.QUOTA_SPENT.id.equals(third.reason())
					&& key(third.said()).equals(READING_KEY + "quota_spent") && Pacts.done(pactOf(level, perch).pact(), now) == 2,
					"A third the same day is refused, its quota spent, and counts nothing");
			helper.assertValueEqual(unchanged(helper, List.of(dim, bright)), before, "Nothing on either bed changed");
			helper.assertValueEqual(Garden.budget(level).used(now), used, "Still no sample taken");
			keeper.closeContainer();
		});
	}

	/** GC-06 (Dossier 3 item 3): a tolerating Sunpetal is told of its diversity; no habitat a Sunpetal reads is ever told to add magic. */
	@GameTest(maxTicks = 80)
	public void aReadingNeverAdvisesDisturbanceToASunpetal(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		VerdantBedBlockEntity bed = plantedBed(helper, new BlockPos(5, 1, 5), keeper.getUUID(), Garden.SUNPETAL_CROP, 4);
		ConcordanceGardenGameTests.afterSampling(helper, List.of(bed), () -> {
			PactMenu.Answered read = reading(helper, keeper, perch, level.getGameTime());
			Component remedy = remedy(read);
			helper.assertTrue(read.done() && key(remedy).equals(READING_KEY + "remedy.diversity.more") && Integer.valueOf(1).equals(arg(remedy, 1))
					&& Integer.valueOf(3).equals(arg(remedy, 2)), "Alone under open sky, its diversity, 1 to 3: " + read.said().getString());
			Organism sunpetal = Garden.catalog().organism("jugcraft:sunpetal");
			List<Niche> niches = List.of(sunpetal.niche(), Garden.catalog().niche(sunpetal, "jugcraft:dewlit"));
			int checked = 0;
			for (Niche niche : niches) {
				for (int moisture : new int[] {0, 2, 4, 7}) {
					for (int light : new int[] {0, 9, 11, 13, 15}) {
						for (int nutrients : new int[] {0, 1, 3, 32}) {
							for (int diversity : new int[] {1, 3}) {
								for (int disturbance = 0; disturbance <= 15; disturbance++) {
									for (int strain = 0; strain <= 3; strain++) {
										HabitatAdvice.Advice advice = HabitatAdvice.one(niche,
												new Habitat(moisture, light, nutrients, diversity, disturbance, strain));
										checked++;
										helper.assertTrue(advice == null || advice.factor() != Factor.DISTURBANCE || advice.change() != HabitatAdvice.Change.MORE,
												"A Sunpetal is never told to add magic: " + advice);
									}
								}
							}
						}
					}
				}
			}
			helper.assertValueEqual(checked, 2 * 4 * 5 * 4 * 2 * 16 * 4, "Every reading checked");
		});
	}

	/** GC-06 (Dossier 3 item 3): a Gloamcap with no magic near is told of a quiet source, 0 to 1, that alone moves it on. */
	@GameTest(maxTicks = 80)
	public void aReadingAdvisesAQuietSourceForAGloamcap(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		BlockPos at = new BlockPos(5, 1, 5);
		VerdantBedBlockEntity bed = plantedBed(helper, at, keeper.getUUID(), Garden.GLOAMCAP_CROP, 4);
		shutIn(helper, at.above());
		ConcordanceGardenGameTests.afterSampling(helper, List.of(bed), () -> {
			PactMenu.Answered read = reading(helper, keeper, perch, level.getGameTime());
			Component remedy = remedy(read);
			helper.assertTrue(read.done() && key(remedy).equals(READING_KEY + "remedy.disturbance.more") && Integer.valueOf(0).equals(arg(remedy, 1))
					&& Integer.valueOf(1).equals(arg(remedy, 2)) && key(clause(read)).equals(READING_KEY + "advice.alone"),
					"In the dark with no magic, its disturbance, 0 to 1, alone: " + read.said().getString());
			helper.assertTrue(Language.getInstance().getOrDefault(READING_KEY + "remedy.disturbance.more").contains("a quiet source"),
					"Its words name a quiet source of magic");
		});
	}

	/** GC-06 (GB11): a bed with no sample is skipped (never sampled for the reading); with none sampled, he says so and counts nothing. */
	@GameTest(maxTicks = 80)
	public void aReadingSkipsBedsWithoutACachedSample(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		VerdantBedBlockEntity unread = plantedBed(helper, new BlockPos(2, 1, 5), keeper.getUUID(), Garden.SUNPETAL_CROP, 0);
		BlockPos sampledAt = new BlockPos(5, 1, 5);
		VerdantBedBlockEntity read = plantedBed(helper, sampledAt, keeper.getUUID(), Garden.SUNPETAL_CROP, 4);
		ConcordanceGardenGameTests.afterSampling(helper, List.of(read), () -> {
			long now = level.getGameTime();
			// A random tick of its own may have sampled the stalled bed: forgotten, it holds no sample again.
			unread.forgetSample();
			int used = Garden.budget(level).used(now);
			// His page open, so a reading's bed is shown (PactSpiritEntity.showTarget shows nothing to no one listening).
			open(helper, keeper, perch);
			PactMenu.Answered named = reading(helper, keeper, perch, now);
			helper.assertTrue(named.done() && helper.absolutePos(sampledAt).toShortString().equals(arg(named.said(), 1))
					&& unread.cachedHabitat(level) == null && Garden.budget(level).used(now) == used,
					"The unsampled stalled bed is skipped, never sampled; the sampled one is read: " + named.said().getString());
			read.forgetSample();
			PactMenu.Answered none = reading(helper, keeper, perch, now);
			helper.assertTrue(!none.done() && HabitatReading.NO_SAMPLE.equals(none.reason()) && Pacts.done(pactOf(level, perch).pact(), now) == 1
					&& marax(helper, perch).target().equals(Optional.of(helper.absolutePos(sampledAt))),
					"None sampled: he says so, nothing is counted, and no bed is shown in place of the one last named");
			keeper.closeContainer();
		});
	}

	/** GC-06 (CT GC-06): two faults (moisture 1, nutrients 0): the first in Factor order, moisture to 4, "one of 2". */
	@GameTest(maxTicks = 80)
	public void aReadingWithTwoFaultsNamesTheFirst(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		BlockPos at = new BlockPos(5, 1, 5);
		VerdantBedBlockEntity bed = plantedBed(helper, at, keeper.getUUID(), Garden.SUNPETAL_CROP, 0);
		helper.setBlock(at, helper.getBlockState(at).setValue(VerdantBedBlock.MOISTURE, 1));
		ConcordanceGardenGameTests.afterSampling(helper, List.of(bed), () -> {
			// A random tick may have dried or wetted it: set again (the block state; its block entity stays).
			helper.setBlock(at, helper.getBlockState(at).setValue(VerdantBedBlock.MOISTURE, 1));
			PactMenu.Answered read = reading(helper, keeper, perch, level.getGameTime());
			Component remedy = remedy(read);
			Component clause = clause(read);
			helper.assertTrue(read.done() && key(clause).equals(READING_KEY + "advice.one_of") && Integer.valueOf(2).equals(arg(clause, 0)),
					"Two things hold it: one of 2: " + read.said().getString());
			helper.assertTrue(key(remedy).equals(READING_KEY + "remedy.moisture.more") && Integer.valueOf(1).equals(arg(remedy, 1))
					&& Integer.valueOf(4).equals(arg(remedy, 2)), "The first, moisture before nutrients, 1 to 4: " + remedy);
		});
	}

	/** GC-06 (holder only): a stranger, the holder's party and a stand-in with the holder's id are each refused, nothing counted or shown. */
	@GameTest(maxTicks = 20)
	public void onlyTheHolderMayAskForAReading(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		ServerPlayer stranger = gardenHolder(helper);
		ServerPlayer friend = gardenHolder(helper);
		floor(helper);
		long now = level.getGameTime();
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, now);
		plantedBed(helper, new BlockPos(5, 1, 5), keeper.getUUID(), Garden.SUNPETAL_CROP, 0);
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(keeper.getUUID(), friend.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(friend.getUUID(), ms) == PartyManager.Result.OK, "The holder and a friend form a party");
			beside(helper, stranger, MARAX_PERCH);
			beside(helper, friend, MARAX_PERCH);
			MaraxEntity marax = marax(helper, perch);
			FakePlayer standIn = FakePlayer.get(level, new GameProfile(keeper.getUUID(), "[Concordance]"));
			// The holder's page open, so a reading's bed would be shown (showTarget shows nothing to no one listening).
			open(helper, keeper, perch);
			for (ServerPlayer asker : List.of(stranger, friend, standIn)) {
				PactMenu.Answered refused = reading(helper, asker, perch, now);
				helper.assertTrue(!refused.done() && Pacts.Reckoning.NOT_HOLDER.id.equals(refused.reason())
						&& key(refused.said()).equals(READING_KEY + "not_holder") && Pacts.done(pactOf(level, perch).pact(), now) == 0
						&& marax.target().isEmpty(), "Only the holder may ask: " + asker.getName().getString());
			}
			keeper.closeContainer();
			PactMenu menu = open(helper, stranger, perch);
			helper.assertTrue(menu.lastSent().mode() == PactStatePayload.CLOSED && !menu.lastSent().offers(PactRequestPayload.RECALL),
					"The stranger's page is closed, with no reading offered");
			helper.assertTrue(answered(send(stranger, menu, PactRequestPayload.RECALL, 0), PactMenu.NOT_OFFERED)
					&& Pacts.done(pactOf(level, perch).pact(), now) == 0, "and a reading asked anyway is not offered");
			stranger.closeContainer();
		} finally {
			parties.adminDisband(keeper.getUUID());
		}
		helper.succeed();
	}

	/**
	 * GC-06 (ownership): a stranger's planted bed is never his to read. At a perch with only the stranger's bed near, he does
	 * not come; at his pact's perch the reading names the holder's own bed though the stranger's is worse and nearer, and with
	 * the holder's crop gone nothing grows for him.
	 */
	@GameTest(maxTicks = 80)
	public void maraxNeverReadsAStrangersBed(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		VerdantBedBlockEntity strangers = plantedBed(helper, new BlockPos(2, 1, 5), STRANGER, Garden.SUNPETAL_CROP, 0);
		// Before his bed: in an 8 x 8 x 8 structure every bed is within 8 of every perch (the critique's item 13).
		BlockPos otherAt = new BlockPos(6, 1, 1);
		PactPerchBlockEntity other = perch(helper, otherAt, keeper);
		beside(helper, keeper, otherAt);
		PactStatePayload offered = offerOf(helper, keeper, other, maraxOffer()).lastSent();
		helper.assertTrue(keys(offered.notes()).contains(READING_KEY + "missing") && other.visitor() == null,
				"With only a stranger's planted bed near, he does not come: " + keys(offered.notes()));
		keeper.closeContainer();
		RateGate.forget(keeper.getUUID());
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		BlockPos ownAt = new BlockPos(5, 1, 5);
		VerdantBedBlockEntity own = plantedBed(helper, ownAt, keeper.getUUID(), Garden.SUNPETAL_CROP, 4);
		ConcordanceGardenGameTests.afterSampling(helper, List.of(strangers, own), () -> {
			long now = level.getGameTime();
			PactMenu.Answered read = reading(helper, keeper, perch, now);
			helper.assertTrue(read.done() && helper.absolutePos(ownAt).toShortString().equals(arg(read.said(), 1)),
					"His own tolerating bed is read, never the stranger's stalled one: " + read.said().getString());
			helper.setBlock(ownAt.above(), Blocks.AIR);
			PactMenu.Answered none = reading(helper, keeper, perch, now);
			helper.assertTrue(!none.done() && HabitatReading.NOTHING_GROWS.equals(none.reason()) && Pacts.done(pactOf(level, perch).pact(), now) == 1,
					"His crop gone, nothing grows for him (the stranger's is never his), and nothing more is counted");
		});
	}

	/**
	 * GC-06 (GC-S7; Marax's review): never a bed the holder could not change by hand. Their stalled bed, nearer the perch,
	 * stands in a claim; the reading names their own tolerating bed beyond it and leaves the claimed one as it was. With only
	 * the claimed bed planted, nothing grows for him, and nothing more is counted.
	 */
	@GameTest(maxTicks = 80)
	public void maraxNeverReadsABedHeMayNotChange(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		BlockPos claimedAt = new BlockPos(2, 1, 5);
		BlockPos ownAt = new BlockPos(5, 1, 5);
		VerdantBedBlockEntity claimed = plantedBed(helper, claimedAt, keeper.getUUID(), Garden.SUNPETAL_CROP, 0);
		VerdantBedBlockEntity own = plantedBed(helper, ownAt, keeper.getUUID(), Garden.SUNPETAL_CROP, 4);
		ConcordanceGardenGameTests.afterSampling(helper, List.of(claimed, own), () -> {
			long now = level.getGameTime();
			MaraxEntity marax = marax(helper, perch);
			try (TestClaims claims = TestClaims.open()) {
				claims.block(helper.absolutePos(claimedAt), STRANGER);
				open(helper, keeper, perch);
				List<String> before = unchanged(helper, List.of(claimed));
				PactMenu.Answered read = reading(helper, keeper, perch, now);
				helper.assertTrue(read.done() && helper.absolutePos(ownAt).toShortString().equals(arg(read.said(), 1))
						&& marax.target().equals(Optional.of(helper.absolutePos(ownAt))),
						"The claimed stalled bed, though nearer and worse, is passed by; his own tolerating bed is read: " + read.said().getString());
				helper.assertValueEqual(unchanged(helper, List.of(claimed)), before, "The claimed bed is as it was");
				helper.setBlock(ownAt.above(), Blocks.AIR);
				PactMenu.Answered none = reading(helper, keeper, perch, now);
				helper.assertTrue(!none.done() && HabitatReading.NOTHING_GROWS.equals(none.reason()) && Pacts.done(pactOf(level, perch).pact(), now) == 1,
						"With only the claimed bed planted, nothing grows for him, and nothing more is counted: " + none.said().getString());
				keeper.closeContainer();
			}
		});
	}

	/**
	 * GC-06 (GC-S7; Marax's review): beds he may not change never use up his READ_BEDS. The holder's eight planted beds
	 * nearest the perch, each only tolerating, stand in a claim; a ninth of theirs, farthest and stalled, is read and named.
	 * The claim lifted, the eight are read and the ninth, though the worst, is past his READ_BEDS and never read.
	 */
	@GameTest(maxTicks = 80)
	public void maraxReadsPastBedsHeMayNotChange(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		// Round the perch at (3, 1, 2), clear of his station (two west) and of his holder (two south).
		List<BlockPos> nearAt = List.of(new BlockPos(3, 1, 1), new BlockPos(3, 1, 3), new BlockPos(4, 1, 2), new BlockPos(4, 1, 1),
				new BlockPos(4, 1, 3), new BlockPos(2, 1, 1), new BlockPos(2, 1, 3), new BlockPos(5, 1, 2));
		List<VerdantBedBlockEntity> beds = new ArrayList<>();
		for (BlockPos at : nearAt) {
			beds.add(plantedBed(helper, at, keeper.getUUID(), Garden.SUNPETAL_CROP, 4));
		}
		BlockPos farAt = new BlockPos(7, 1, 7);
		beds.add(plantedBed(helper, farAt, keeper.getUUID(), Garden.SUNPETAL_CROP, 0));
		helper.assertValueEqual(nearAt.size(), HabitatReading.READ_BEDS, "As many near beds as one reading reads");
		ConcordanceGardenGameTests.afterSampling(helper, beds, () -> {
			long now = level.getGameTime();
			String far = helper.absolutePos(farAt).toShortString();
			try (TestClaims claims = TestClaims.open()) {
				for (BlockPos at : nearAt) {
					claims.block(helper.absolutePos(at), STRANGER);
				}
				PactMenu.Answered read = reading(helper, keeper, perch, now);
				helper.assertTrue(read.done() && far.equals(arg(read.said(), 1)),
						"The eight nearest, each in a claim, are passed by and use none of his READ_BEDS; the ninth is read: " + read.said().getString());
			}
			PactMenu.Answered lifted = reading(helper, keeper, perch, now);
			List<String> near = nearAt.stream().map(at -> helper.absolutePos(at).toShortString()).toList();
			helper.assertTrue(lifted.done() && near.contains(arg(lifted.said(), 1)) && !far.equals(arg(lifted.said(), 1)),
					"The claim lifted, he reads his READ_BEDS nearest and never the ninth, though it is the worst: " + lifted.said().getString());
		});
	}

	/**
	 * GC-06 (Dossier 3 item 5; Marax's review): a bed is proud only after three readings running with no change to its
	 * verdict. Named twice, its crop taken (nothing grows; nothing counted) and planted again, it is named once more as if
	 * for the first time: one running, not proud.
	 */
	@GameTest(maxTicks = 80)
	public void theProudCountStartsAgainWhenTheBedChanges(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		BlockPos at = new BlockPos(5, 1, 5);
		VerdantBedBlockEntity bed = plantedBed(helper, at, keeper.getUUID(), Garden.SUNPETAL_CROP, 0);
		BlockState ripe = ((OrganismCropBlock) Garden.SUNPETAL_CROP).getStateForAge(OrganismCropBlock.MAX_AGE);
		ConcordanceGardenGameTests.afterSampling(helper, List.of(bed), () -> {
			MaraxEntity marax = marax(helper, perch);
			String named = helper.absolutePos(at).toShortString();
			for (long when : new long[] {dayStart(level, 1), dayStart(level, 1) + 100}) {
				helper.assertTrue(reading(helper, keeper, perch, when).done(), "Its bed named at " + when);
			}
			HabitatReading.Memory memory = familiar(marax);
			helper.assertTrue(memory.runs() == 2 && !memory.proud(helper.absolutePos(at)), "Twice running: not yet proud");
			long second = dayStart(level, 2);
			helper.setBlock(at.above(), Blocks.AIR);
			PactMenu.Answered none = reading(helper, keeper, perch, second);
			helper.assertTrue(!none.done() && HabitatReading.NOTHING_GROWS.equals(none.reason()) && Pacts.done(pactOf(level, perch).pact(), second) == 0,
					"Its crop taken: nothing grows, nothing counted");
			helper.setBlock(at.above(), ripe);
			PactMenu.Answered again = reading(helper, keeper, perch, second + 100);
			helper.assertTrue(again.done() && named.equals(arg(again.said(), 1)) && memory.runs() == 1 && !memory.proud(helper.absolutePos(at)),
					"Planted again, it is named as if for the first time: " + memory.runs() + " running, not proud");
		});
	}

	/**
	 * GC-06 (save and load): a reading's target and the familiar's memory are never saved; read back, he shows and remembers
	 * nothing. Its only bed named three readings running is proud first, so the save is asked of a pique too, and a fourth
	 * reading passes it over: nothing to correct, an answer given (his assent) that counts nothing.
	 */
	@GameTest(maxTicks = 80)
	public void theTargetIsNeverSaved(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		VerdantBedBlockEntity bed = plantedBed(helper, new BlockPos(5, 1, 5), keeper.getUUID(), Garden.SUNPETAL_CROP, 0);
		ConcordanceGardenGameTests.afterSampling(helper, List.of(bed), () -> {
			MaraxEntity marax = marax(helper, perch);
			// He takes his pact's record at his first think (every THINK_TICKS, so maybe not yet at tick 5): think once, so his
			// save is asked to hold it (the review's item 2).
			marax.think(level, NOON, level.getGameTime());
			open(helper, keeper, perch);
			helper.assertTrue(reading(helper, keeper, perch, level.getGameTime()).done() && marax.target().isPresent()
					&& familiar(marax).lastBed() != null, "A reading shows its bed and is remembered");
			for (long when : new long[] {dayStart(level, 1), dayStart(level, 1) + 100}) {
				helper.assertTrue(reading(helper, keeper, perch, when).done(), "Its only bed named again at " + when);
			}
			helper.assertTrue(familiar(marax).proudBeds() == 1, "Named three readings running, nothing changed: proud");
			long fourth = dayStart(level, 2);
			PactMenu.Answered passed = reading(helper, keeper, perch, fourth);
			helper.assertTrue(passed.done() && HabitatReading.NOTHING_TO_CORRECT.equals(passed.reason())
					&& key(passed.said()).equals(READING_KEY + "nothing_to_correct.proud") && Pacts.done(pactOf(level, perch).pact(), fourth) == 0
					&& MaraxEntity.reaction(PactRequestPayload.RECALL, passed.reason(), passed.done()) == MaraxEntity.Gesture.ASSENT,
					"Its only bed proud, nothing to correct: an answer given, with his assent, that counts nothing: " + passed.said().getString());
			helper.assertTrue(marax.target().isPresent(), "The bed last named is still shown as he is saved");
			TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
			marax.saveWithoutId(saved);
			CompoundTag tag = saved.buildResult();
			helper.assertTrue(tag.contains("perch") && tag.contains("record") && tag.keySet().stream().map(k -> k.toLowerCase(Locale.ROOT))
					.noneMatch(k -> k.contains("target") || k.contains("proud") || k.contains("familiar") || k.equals("side")),
					"His save holds his perch and record, no target, side or pique: " + tag.keySet());
			MaraxEntity copy = Workers.MARAX.create(level, EntitySpawnReason.LOAD);
			copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
			helper.assertTrue(copy.target().isEmpty() && familiar(copy).proudBeds() == 0 && familiar(copy).lastBed() == null,
					"Read back, he shows nothing and remembers no reading");
			keeper.closeContainer();
		});
	}

	/**
	 * GC-06 (GC-P65; the critique's item 8): a reading shows its bed only while he is listened to, for at most SHOW_TICKS:
	 * its time passed with the screen still open clears it; so does the conversation's end at his next think, and leaving.
	 */
	@GameTest(maxTicks = 80)
	public void theTargetClearsWhenTheConversationEnds(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		BlockPos bedAt = new BlockPos(5, 1, 5);
		VerdantBedBlockEntity bed = plantedBed(helper, bedAt, keeper.getUUID(), Garden.SUNPETAL_CROP, 0);
		ConcordanceGardenGameTests.afterSampling(helper, List.of(bed), () -> {
			long now = level.getGameTime();
			MaraxEntity marax = marax(helper, perch);
			Optional<BlockPos> named = Optional.of(helper.absolutePos(bedAt));
			open(helper, keeper, perch);
			helper.assertTrue(reading(helper, keeper, perch, now).done() && marax.target().equals(named),
					"Asked with the screen open, his familiar's target is the bed");
			marax.think(level, NOON, now + HabitatReading.SHOW_TICKS - 1);
			helper.assertTrue(marax.listening() && marax.target().equals(named), "Still listened to and inside its time: still shown");
			marax.think(level, NOON, now + HabitatReading.SHOW_TICKS);
			helper.assertTrue(marax.listening() && marax.target().isEmpty(), "Its time passed with the screen still open: cleared");
			helper.assertTrue(reading(helper, keeper, perch, now).done() && marax.target().equals(named), "The day's second reading shows it again");
			keeper.closeContainer();
			marax.think(level, NOON, now);
			helper.assertTrue(!marax.listening() && marax.target().isEmpty(), "The conversation over: cleared at the next think");
			// Fed a little overnight, its verdict changes and the count starts again: a fourth reading of an unchanged bed
			// would find it proud (PIQUE) and name nothing. Still the only bed and not thriving, it is named again.
			bed.setNutrients(1);
			long day = dayStart(level, 1);
			helper.assertTrue(reading(helper, keeper, perch, day).done() && marax.target().isEmpty(), "With no screen open a reading shows nothing");
			open(helper, keeper, perch);
			helper.assertTrue(reading(helper, keeper, perch, day).done() && marax.target().equals(named), "Listened to again, it shows");
			marax.leave(level, perch, helper.absolutePos(MARAX_PERCH));
			helper.assertTrue(marax.target().isEmpty() && marax.leaving(), "Leaving, it shows nothing at once");
			keeper.closeContainer();
		});
	}

	/**
	 * GC-06 (GC-P69, Dossier 3 item 5): the poor bed (nutrients 0, stalled) named for its nutrients at three readings running,
	 * nothing changed, is proud: the fourth reads the plain bed (tolerating) and says it will not repeat itself; the kept page
	 * and Jade say so; fed, the poor bed's verdict changes and it is forgiven.
	 */
	@GameTest(maxTicks = 80)
	public void theProudFamiliarSkipsABedNamedThreeTimes(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		BlockPos poorAt = new BlockPos(5, 1, 5);
		BlockPos plainAt = new BlockPos(2, 1, 5);
		VerdantBedBlockEntity poor = plantedBed(helper, poorAt, keeper.getUUID(), Garden.SUNPETAL_CROP, 0);
		VerdantBedBlockEntity plain = plantedBed(helper, plainAt, keeper.getUUID(), Garden.SUNPETAL_CROP, 4);
		ConcordanceGardenGameTests.afterSampling(helper, List.of(poor, plain), () -> {
			MaraxEntity marax = marax(helper, perch);
			String poorPos = helper.absolutePos(poorAt).toShortString();
			for (long when : new long[] {dayStart(level, 1), dayStart(level, 1) + 100, dayStart(level, 2)}) {
				PactMenu.Answered named = reading(helper, keeper, perch, when);
				helper.assertTrue(named.done() && poorPos.equals(arg(named.said(), 1)) && key(remedy(named)).equals(READING_KEY + "remedy.nutrients.more"),
						"The poor bed is named for its nutrients: " + named.said().getString());
			}
			HabitatReading.Memory memory = familiar(marax);
			helper.assertTrue(memory.proud(helper.absolutePos(poorAt)) && memory.runs() == HabitatReading.PIQUE,
					"Three readings running, nothing changed: proud");
			long fourth = dayStart(level, 2) + 100;
			PactMenu.Answered next = reading(helper, keeper, perch, fourth);
			Component passed = (Component) arg(next.said(), 6);
			helper.assertTrue(next.done() && helper.absolutePos(plainAt).toShortString().equals(arg(next.said(), 1))
					&& key(passed).equals(READING_KEY + "read.proud") && poorPos.equals(arg(passed, 0)),
					"The fourth reads the next-worst bed and will not repeat itself about the poor one: " + next.said().getString());
			helper.assertTrue(keys(HabitatReading.rows(level, pactOf(level, perch), fourth)).contains(READING_KEY + "row.proud"),
					"Its kept page says so");
			PactService.Note note = HabitatReading.SERVICE.note(level, marax, pactOf(level, perch), NOON, fourth);
			helper.assertTrue(note != null && note.id().equals("proud") && note.count() == 1, "and Jade's note: " + note);
			poor.setNutrients(1);
			PactMenu.Answered fed = reading(helper, keeper, perch, dayStart(level, 3));
			helper.assertTrue(fed.done() && !memory.proud(helper.absolutePos(poorAt)) && key((Component) arg(fed.said(), 6)).isEmpty(),
					"Fed, its verdict changed: forgiven, and nothing is passed over");
		});
	}

	/** GC-06 (Dossier 3 item 6): with no planted bed of the inviter's near he does not come, in his words; a crop planted, he comes. */
	@GameTest(maxTicks = 20)
	public void maraxRefusesWhereNothingGrows(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(Language.getInstance().has(READING_KEY + "missing"), "the server reads the mod's words (Fabric's language loader)");
		ServerPlayer keeper = gardenHolder(helper);
		pocketFor(keeper, maraxOffer(), 1);
		floor(helper);
		PactPerchBlockEntity perch = perch(helper, MARAX_PERCH, keeper);
		beside(helper, keeper, MARAX_PERCH);
		BlockPos bedAt = new BlockPos(5, 1, 5);
		keptBed(helper, bedAt, keeper.getUUID(), 0);
		PactStatePayload bare = offerOf(helper, keeper, perch, maraxOffer()).lastSent();
		Component note = bare.notes().stream().filter(line -> key(line).equals(READING_KEY + "missing")).findFirst().orElse(null);
		helper.assertTrue(note != null && Integer.valueOf(8).equals(arg(note, 0)) && perch.visitor() == null
				&& note.getString().startsWith("\"My familiar reads what grows. Nothing grows here.\""),
				"An awake bed with nothing planted: he says why and does not come: " + keys(bare.notes()));
		keeper.closeContainer();
		helper.setBlock(bedAt.above(), ((OrganismCropBlock) Garden.SUNPETAL_CROP).getStateForAge(OrganismCropBlock.MAX_AGE));
		RateGate.forget(keeper.getUUID());
		PactMenu menu = offerOf(helper, keeper, perch, maraxOffer());
		helper.assertTrue(menu.lastSent().notes().isEmpty() && perch.visitor() != null && level.getEntity(perch.visitor()) instanceof MaraxEntity,
				"A Sunpetal planted on it, he comes (Dossier 3 item 2)");
		helper.assertTrue(answered(send(keeper, menu, PactRequestPayload.ACCEPT, 0), PactMenu.SEALED), "and his pact is sealed");
		keeper.closeContainer();
		helper.setBlock(bedAt.above(), Blocks.AIR);
		long now = level.getGameTime();
		PactMenu.Answered none = reading(helper, keeper, perch, now);
		helper.assertTrue(!none.done() && HabitatReading.NOTHING_GROWS.equals(none.reason()) && Pacts.done(pactOf(level, perch).pact(), now) == 0,
				"Its crop gone, nothing grows: refused, nothing counted: " + none.said().getString());
		helper.succeed();
	}

	/**
	 * GC-06 (GC-P70; the critique's item 10): his first obligation is the holder's own use of a Verdant Bed itself in his area
	 * with an empty hand while the pact is active: nothing before the pact or while it is suspended, nothing for the crop's
	 * own report (a young crop, which an empty hand would not harvest), once a player.
	 */
	@GameTest(maxTicks = 20)
	public void maraxsFirstObligationIsABedReadByHand(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		BlockPos bedAt = new BlockPos(5, 1, 5);
		keptBed(helper, bedAt, keeper.getUUID(), 0);
		BlockState young = ((OrganismCropBlock) Garden.SUNPETAL_CROP).getStateForAge(1);
		helper.setBlock(bedAt.above(), young);
		int heard = GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0);
		ConcordanceGardenGameTests.handUse(helper, keeper, bedAt, ItemStack.EMPTY, false);
		helper.assertTrue(!PactObligations.kept(keeper, MaraxEntity.COUNTERPART), "Before any pact, nothing is kept");
		long now = level.getGameTime();
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, now);
		helper.assertTrue(HabitatReading.SERVICE.obligation() == PactObligations.Act.READ
				&& keys(HabitatReading.SERVICE.obligationRows(level, keeper, pactOf(level, perch), now))
						.equals(List.of("message.jugcraft.concordance.garden_pact.obligation")), "His page names the act");
		helper.assertValueEqual(PactKeeping.suspend(level, keeper, perch.holder(), now), AgreementHolder.Toggle.SUSPENDED, "Suspended");
		ConcordanceGardenGameTests.handUse(helper, keeper, bedAt, ItemStack.EMPTY, false);
		helper.assertTrue(!PactObligations.kept(keeper, MaraxEntity.COUNTERPART), "Suspended, nothing is kept");
		helper.assertValueEqual(PactKeeping.resume(level, keeper, perch.holder(), now), AgreementHolder.Toggle.RESUMED, "Resumed");
		ConcordanceGardenGameTests.handUse(helper, keeper, bedAt.above(), ItemStack.EMPTY, false);
		helper.assertTrue(!PactObligations.kept(keeper, MaraxEntity.COUNTERPART) && helper.getBlockState(bedAt.above()).equals(young),
				"The crop's own report is no bed read: nothing kept, the crop not harvested");
		ConcordanceGardenGameTests.handUse(helper, keeper, bedAt, ItemStack.EMPTY, false);
		helper.assertTrue(PactObligations.kept(keeper, MaraxEntity.COUNTERPART) && PactObligations.spiritsKept(keeper) == 1
				&& GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == heard + 1
				&& keys(HabitatReading.SERVICE.obligationRows(level, keeper, pactOf(level, perch), now))
						.equals(List.of("message.jugcraft.concordance.garden_pact.obligation_kept")),
				"The bed read by hand in his area, the pact active: kept, once");
		ConcordanceGardenGameTests.handUse(helper, keeper, bedAt, ItemStack.EMPTY, false);
		helper.assertTrue(PactObligations.spiritsKept(keeper) == 1 && GARDEN_SERVED.getOrDefault(keeper.getUUID(), 0) == heard + 1,
				"and it is not recorded again");
		helper.succeed();
	}

	/** GC-06 (GC-P67): he arrives two blocks west of the perch (a quarter turn clockwise from his inviter in the south), facing them; led off he comes back; his side walled, the next clockwise. */
	@GameTest(maxTicks = 20)
	public void maraxStandsBesideThePerchFacingHisInviter(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		MaraxEntity marax = marax(helper, perch);
		Vec3 west = Vec3.atBottomCenterOf(helper.absolutePos(MARAX_PERCH.west(2)));
		helper.assertTrue(marax.position().distanceTo(west) < 1.0E-3, "He stands two blocks west, on the ground: " + marax.position());
		float facing = PactSpiritEntity.bearing(marax.position(), keeper.position(), 0.0F);
		helper.assertTrue(Math.abs(Mth.wrapDegrees(marax.getYRot() - facing)) <= 1.0F, "facing his inviter: " + marax.getYRot() + " against " + facing);
		marax.teleportTo(west.x, west.y, west.z + 3.0);
		marax.think(level, NOON, level.getGameTime());
		helper.assertTrue(marax.position().distanceTo(west) < 1.0E-3, "Led off, he is back on his own side at his next think");
		helper.setBlock(MARAX_PERCH.west(1), Blocks.STONE);
		marax.think(level, NOON, level.getGameTime());
		helper.assertTrue(marax.position().distanceTo(Vec3.atBottomCenterOf(helper.absolutePos(MARAX_PERCH.north(2)))) < 1.0E-3,
				"His side walled, he takes the next clear one clockwise (north): " + marax.position());
		helper.assertTrue(perch.presence(level) == marax, "still the pact's one presence");
		helper.succeed();
	}

	/** GC-06 (GC-P67): walled in on every side, a standing spirit stands on its perch; a side cleared, it steps down to it at its next think. */
	@GameTest(maxTicks = 20)
	public void aStandingSpiritFallsBackToThePerchWithNoFreeSide(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		for (Direction side : Direction.Plane.HORIZONTAL) {
			helper.setBlock(MARAX_PERCH.relative(side), Blocks.STONE);
		}
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		MaraxEntity marax = marax(helper, perch);
		helper.assertTrue(marax.position().distanceTo(Vec3.atBottomCenterOf(helper.absolutePos(MARAX_PERCH.above()))) < 1.0E-3,
				"Walled in on every side, he stands on the perch: " + marax.position());
		helper.setBlock(MARAX_PERCH.west(), Blocks.AIR);
		marax.think(level, NOON, level.getGameTime());
		helper.assertTrue(marax.position().distanceTo(Vec3.atBottomCenterOf(helper.absolutePos(MARAX_PERCH.west(2)))) < 1.0E-3,
				"A side cleared, he steps down to it at his next think: " + marax.position());
		helper.succeed();
	}

	/**
	 * GC-06 (Jade; GC-P69): the readings of his perch and of him: kept, his work, active; before any reading his page says so
	 * and holds no proud row; with a bed proud, both readings carry his note (proud, one bed); none of his rows or readings
	 * names a player.
	 */
	@GameTest(maxTicks = 80)
	public void maraxsReadingSaysItsNoteAndNamesNoPlayer(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		VerdantBedBlockEntity poor = plantedBed(helper, new BlockPos(5, 1, 5), keeper.getUUID(), Garden.SUNPETAL_CROP, 0);
		VerdantBedBlockEntity plain = plantedBed(helper, new BlockPos(2, 1, 5), keeper.getUUID(), Garden.SUNPETAL_CROP, 4);
		ConcordanceGardenGameTests.afterSampling(helper, List.of(poor, plain), () -> {
			MaraxEntity marax = marax(helper, perch);
			long first = dayStart(level, 1);
			List<String> fresh = keys(HabitatReading.rows(level, pactOf(level, perch), first));
			helper.assertTrue(fresh.contains(READING_KEY + "row.last_none") && !fresh.contains(READING_KEY + "row.proud"),
					"Before any reading: none given, nothing proud: " + fresh);
			for (long when : new long[] {first, first + 100, dayStart(level, 2), dayStart(level, 2) + 100}) {
				helper.assertTrue(reading(helper, keeper, perch, when).done(), "A reading at " + when);
			}
			long now = dayStart(level, 2) + 100;
			CompoundTag ofPerch = PactReading.holder(level, perch, now);
			CompoundTag ofHim = PactReading.presence(level, marax, now);
			for (CompoundTag reading : List.of(ofPerch, ofHim)) {
				helper.assertTrue("kept".equals(reading.getStringOr("standing", "")) && HabitatReading.WORK.equals(reading.getStringOr("service", ""))
						&& "active".equals(reading.getStringOr("state", "")) && "proud".equals(reading.getStringOr("note", ""))
						&& reading.getIntOr("note_count", 0) == 1 && PactReading.KEYS.containsAll(reading.keySet()),
						"The reading: kept, his work, active, one bed proud: " + reading);
			}
			BoundWill record = pactOf(level, perch);
			String all = ofPerch + " " + ofHim + " " + HabitatReading.SERVICE.serviceRows(level, keeper, record, now)
					+ " " + HabitatReading.SERVICE.obligationRows(level, keeper, record, now);
			helper.assertTrue(all.contains("row.proud") && all.contains("garden_pact.obligation")
					&& !all.contains(keeper.getUUID().toString()) && !all.contains(keeper.getName().getString()),
					"His rows say what is proud and his first task, and no player is named: " + all);
		});
	}

	/** GC-06: which gesture answers which action, which loop each state shows, every cue's sound registered; a reading through his page is his work. */
	@GameTest(maxTicks = 80)
	public void maraxAnswersEachActionWithHisGesture(GameTestHelper helper) {
		helper.assertTrue(MaraxEntity.reaction(PactRequestPayload.ACCEPT, PactMenu.SEALED, true) == MaraxEntity.Gesture.ASSENT
				&& MaraxEntity.reaction(PactRequestPayload.ACCEPT, HabitatReading.NOTHING_GROWS, false) == MaraxEntity.Gesture.REFUSE
				&& MaraxEntity.reaction(PactRequestPayload.RECALL, HabitatReading.READ, true) == MaraxEntity.Gesture.WORK
				&& MaraxEntity.reaction(PactRequestPayload.RECALL, HabitatReading.NOTHING_TO_CORRECT, true) == MaraxEntity.Gesture.ASSENT
				&& MaraxEntity.reaction(PactRequestPayload.RECALL, Pacts.Reckoning.QUOTA_SPENT.id, false) == MaraxEntity.Gesture.REFUSE
				&& MaraxEntity.reaction(PactRequestPayload.DEPOSIT, PactMenu.DEPOSITED, true) == MaraxEntity.Gesture.INSPECT
				&& MaraxEntity.reaction(PactRequestPayload.DEPOSIT, PactMenu.NOTHING_DEPOSITED, false) == null
				&& MaraxEntity.reaction(PactRequestPayload.RESUME, "resumed", true) == MaraxEntity.Gesture.ASSENT
				&& MaraxEntity.reaction(PactRequestPayload.RESOLVE, PactMenu.RESOLVED, true) == null, "His answers");
		for (int action : new int[] {PactRequestPayload.DECLINE, PactRequestPayload.END, PactRequestPayload.SUSPEND, PactRequestPayload.SEAT,
				PactRequestPayload.INVITE, PactRequestPayload.CHOOSE}) {
			helper.assertTrue(MaraxEntity.reaction(action, "done", true) == null, "No gesture of his for action " + action);
		}
		for (int action = 0; action < PactRequestPayload.ACTIONS.size(); action++) {
			helper.assertTrue(MaraxEntity.reaction(action, PactMenu.NOT_OFFERED, false) == null, "Not offered: no gesture (action " + action + ")");
		}
		helper.assertTrue(MaraxEntity.loop(Status.IDLE, false, false) == MaraxEntity.Loop.IDLE && MaraxEntity.loop(Status.IDLE, false, true) == MaraxEntity.Loop.STARGAZE
				&& MaraxEntity.loop(Status.IDLE, true, true) == MaraxEntity.Loop.LISTEN && MaraxEntity.loop(Status.OWNER_OFFLINE, false, true) == MaraxEntity.Loop.WAITING
				&& MaraxEntity.loop(Status.UPKEEP_DUE, false, false) == MaraxEntity.Loop.ASTIR
				&& MaraxEntity.loop(Status.SUSPENDED, false, true) == MaraxEntity.Loop.SUSPENDED
				&& MaraxEntity.loop(Status.DISABLED, true, false) == MaraxEntity.Loop.WAITING
				&& MaraxEntity.loop(Status.FINISHED, false, false) == MaraxEntity.Loop.WAITING, "His loops; by night, idle, he looks at the sky");
		for (String cue : MaraxEntity.SOUND_IDS) {
			helper.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(Jugcraft.id("marax." + cue)), "jugcraft:marax." + cue + " is registered");
		}
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		floor(helper);
		PactPerchBlockEntity perch = maraxPact(helper, keeper, MARAX_PERCH, level.getGameTime());
		VerdantBedBlockEntity bed = plantedBed(helper, new BlockPos(5, 1, 5), keeper.getUUID(), Garden.SUNPETAL_CROP, 0);
		ConcordanceGardenGameTests.afterSampling(helper, List.of(bed), () -> {
			MaraxEntity marax = marax(helper, perch);
			PactMenu menu = open(helper, keeper, perch);
			PactStatePayload state = send(keeper, menu, PactRequestPayload.RECALL, 0);
			helper.assertTrue(answered(state, HabitatReading.READ) && "work".equals(marax.cues().getLast()),
					"A reading asked on his page is given, and he makes his work gesture: " + marax.cues());
			keeper.closeContainer();
		});
	}

	// GC-07 unit 4
	// ---------------------------------------------------------------- Concordance garden cohort, GC-07: Foras, his service and his matter

	/** GC-07: his service's words (tools/concordance_pacts.py SERVICE_WORDS["graft_keeping"]). */
	static final String GRAFTING_KEY = "message.jugcraft.concordance.pact.graft_keeping.";
	/** GC-07: the graft he keeps (data/jugcraft/concordance/graft/dewlit.json). */
	static final String FORAS_GRAFT = "jugcraft:dewlit";

	static {
		// GC-07 (SG12): Foras comes where a plain container touches the perch and a crop of a known graft's host grows on an
		// awake Verdant Bed the inviter keeps within 8: a chest east of the perch's grid cell and a Sunpetal on a bed in the
		// floor at x + 1, z - 1 (clear of every other fixture: the next grid cell's x - 1 is x + 2). The bed already carries
		// Dewlit: a host kept, so a random tick's sample never raises his matter and every shared case reads him idle; the
		// chest is empty, so nothing is ever grafted. SG12's structure has no floor, so he keeps his perch's top there.
		FIXTURES.put(ForasEntity.COUNTERPART, (helper, player, perch) -> {
			BlockPos chest = perch.east();
			BlockPos bed = perch.offset(1, -1, -1);
			helper.setBlock(chest, Blocks.CHEST);
			keptBed(helper, bed, player.getUUID(), 0).setGraft(FORAS_GRAFT);
			helper.setBlock(bed.above(), Garden.SUNPETAL_CROP);
			return List.of(chest, bed, bed.above());
		});
	}

	/** Foras's offer as the server loaded it (data/jugcraft/concordance/pact/foras.json). */
	static PactDefinition forasOffer() {
		PactDefinition offer = PactPerchBlockEntity.definition(ForasEntity.COUNTERPART);
		if (offer == null) {
			throw new AssertionError("Foras's offer is loaded");
		}
		return offer;
	}

	/** Foras's pact sealed for {@code holder} at a perch they placed at {@code at}, at game time {@code now}; they stand beside it (2.5 south). */
	static PactPerchBlockEntity forasPact(GameTestHelper helper, ServerPlayer holder, BlockPos at, long now) {
		PactPerchBlockEntity perch = perch(helper, at, holder);
		beside(helper, holder, at);
		helper.assertValueEqual(perch.holder().seal(helper.getLevel(), holder, PactPerchBlockEntity.sealing(forasOffer(), null, null), now).outcome(),
				Pacts.Outcome.DONE, "Foras's pact is sealed at the perch");
		return perch;
	}

	/** The Foras kept at {@code perch} (its presence), asserted there. */
	static ForasEntity foras(GameTestHelper helper, PactPerchBlockEntity perch) {
		PactSpiritEntity<?> found = perch.presence(helper.getLevel());
		helper.assertTrue(found instanceof ForasEntity, "Foras is at his perch: " + found);
		return (ForasEntity) found;
	}

	static GraftKeeping.Memory keeping(ForasEntity foras) {
		return foras.serviceMemory(GraftKeeping.Memory.class, GraftKeeping.Memory::new);
	}

	/** A host of Foras's: an awake, wet Verdant Bed at {@code at} kept by {@code keeper} (keptBed) with a Sunpetal on it; no sample taken. */
	static VerdantBedBlockEntity forasHost(GameTestHelper helper, BlockPos at, UUID keeper) {
		VerdantBedBlockEntity bed = keptBed(helper, at, keeper, 0);
		helper.setBlock(at.above(), Garden.SUNPETAL_CROP);
		return bed;
	}

	/** A store at {@code at}: a single chest holding {@code dewmoss} Dewmoss (0: empty). */
	static ChestBlockEntity forasStore(GameTestHelper helper, BlockPos at, int dewmoss) {
		helper.setBlock(at, Blocks.CHEST);
		ChestBlockEntity chest = helper.getBlockEntity(at, ChestBlockEntity.class);
		if (dewmoss > 0) {
			chest.setItem(0, new ItemStack(Garden.DEWMOSS, dewmoss));
		}
		return chest;
	}

	static int dewmossIn(Container store) {
		return store.countItem(Garden.DEWMOSS);
	}

	/** The holder answers Foras's matter at {@code perch} with {@code accord} (PactAccords.resolve, the kept page's own call); the perch keeps it. */
	static void forasAnswer(GameTestHelper helper, ServerPlayer holder, PactPerchBlockEntity perch, String accord, long now) {
		helper.assertTrue(PactAccords.resolve(helper.getLevel(), holder, perch, GraftKeeping.WORK, accord, now), "The holder answers " + accord);
		helper.assertValueEqual(perch.accord(), accord, "The perch keeps the answer " + accord);
	}

	/** A one-variant store holding one Dewmoss whose extraction is refused ({@code refuses}) or allowed. */
	static SingleVariantStorage<ItemVariant> oneDewmoss(boolean refuses) {
		SingleVariantStorage<ItemVariant> store = new SingleVariantStorage<>() {
			@Override
			protected ItemVariant getBlankVariant() {
				return ItemVariant.blank();
			}

			@Override
			protected long getCapacity(ItemVariant variant) {
				return 64;
			}

			@Override
			protected boolean canExtract(ItemVariant variant) {
				return !refuses;
			}
		};
		store.variant = ItemVariant.of(Garden.DEWMOSS);
		store.amount = 1;
		return store;
	}

	/**
	 * GC-07 (the garden cohort's shared sampling, GC-06b, from tick {@code from}): {@code beds} sampled one request a tick,
	 * for the first bed still unsampled, from tick {@code from} to {@code from} + 2 * beds + 2 (a request the level's
	 * allowance refuses is made again the next tick), then {@code body} at {@code from} + 2 * beds + 3, and the test succeeds
	 * after it. ConcordanceGardenGameTests.afterSampling begun later, for a test that must first let the light engine settle.
	 */
	static void sampledFrom(GameTestHelper helper, int from, List<VerdantBedBlockEntity> beds, Runnable body) {
		ServerLevel level = helper.getLevel();
		for (int t = from; t <= from + 2 * beds.size() + 2; t++) {
			helper.runAtTickTime(t, () -> {
				for (VerdantBedBlockEntity bed : beds) {
					if (bed.cachedHabitat(level) == null) {
						bed.habitat(level);
						break;
					}
				}
			});
		}
		helper.runAtTickTime(from + 2L * beds.size() + 3, () -> {
			for (VerdantBedBlockEntity bed : beds) {
				helper.assertTrue(bed.cachedHabitat(level) != null, "The bed's area is sampled: " + bed.getBlockPos());
			}
			body.run();
			helper.succeed();
		});
	}

	/**
	 * GC-07 (GE4; Dossier 4 item 3): answered "graft every host", three hosts and a store of five Dewmoss: three thinks graft
	 * the three, one Dewmoss each, and the fourth grafts nothing; nothing is dropped and the holder's inventory is untouched.
	 * A host whose crop its holder may not change is refused with nothing spent; an extraction a store refuses, or one it
	 * cannot fill, spends nothing and leaves no transaction open.
	 */
	@GameTest(maxTicks = 20)
	public void forasSpendsOneDonorAGraftAndNeverTwo(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(3, 1, 3);
		ChestBlockEntity chest = forasStore(helper, at.east(), 5);
		List<VerdantBedBlockEntity> beds = new ArrayList<>();
		for (int x : new int[] {1, 3, 5}) {
			beds.add(forasHost(helper, new BlockPos(x, 1, 1), holder.getUUID()));
		}
		PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
		ForasEntity foras = foras(helper, perch);
		forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, now);
		int items = carried(holder);
		for (int i = 1; i <= 3; i++) {
			// The second think at midnight: his hours are every hour (GC-P81).
			helper.assertValueEqual(foras.think(level, i == 2 ? MIDNIGHT : 6000L, now + i), Status.WORKING, "Think " + i + " grafts a host");
			helper.assertValueEqual(dewmossIn(chest), 5 - i, "and spends exactly one Dewmoss");
			helper.assertValueEqual(beds.stream().filter(bed -> FORAS_GRAFT.equals(bed.graft())).count(), (long) i, "One more bed carries Dewlit");
		}
		helper.assertValueEqual(foras.think(level, 6000L, now + 4), Status.IDLE, "Every host grafted, he grafts nothing more");
		helper.assertTrue(dewmossIn(chest) == 2 && keeping(foras).idle() == GraftKeeping.Idle.NO_HOST && loose(helper) == 0 && carried(holder) == items,
				"Two Dewmoss left, nothing dropped, the holder's inventory untouched: " + dewmossIn(chest) + ", " + keeping(foras).idle());
		BlockPos refused = new BlockPos(6, 1, 5);
		VerdantBedBlockEntity claimed = forasHost(helper, refused, holder.getUUID());
		keeping(foras).forget();
		try (TestClaims claims = TestClaims.open()) {
			claims.block(helper.absolutePos(refused.above()), STRANGER);
			helper.assertValueEqual(foras.think(level, 6000L, now + 5), Status.IDLE, "A host whose crop is another's is not grafted");
			helper.assertTrue(claimed.graft().isEmpty() && dewmossIn(chest) == 2, "and nothing is spent for it");
		}
		SingleVariantStorage<ItemVariant> refusing = oneDewmoss(true);
		helper.assertTrue(!GraftKeeping.spend(refusing, ItemVariant.of(Garden.DEWMOSS)) && refusing.amount == 1 && !Transaction.isOpen(),
				"A store that refuses the extraction gives nothing, and no transaction is left open");
		SingleVariantStorage<ItemVariant> willing = oneDewmoss(false);
		helper.assertTrue(GraftKeeping.spend(willing, ItemVariant.of(Garden.DEWMOSS)) && willing.amount == 0 && !Transaction.isOpen(),
				"A willing store gives exactly one");
		helper.assertTrue(!GraftKeeping.spend(willing, ItemVariant.of(Garden.DEWMOSS)) && willing.amount == 0 && !Transaction.isOpen(),
				"An empty one gives nothing");
		helper.succeed();
	}

	/** GC-07: a graft the rules know is never replaced; one they no longer know is replaced by the next. */
	@GameTest(maxTicks = 20)
	public void forasNeverReplacesAKnownGraft(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(3, 1, 3);
		ChestBlockEntity chest = forasStore(helper, at.east(), 3);
		VerdantBedBlockEntity known = forasHost(helper, new BlockPos(1, 1, 1), holder.getUUID());
		known.setGraft(FORAS_GRAFT);
		VerdantBedBlockEntity gone = forasHost(helper, new BlockPos(5, 1, 1), holder.getUUID());
		gone.setGraft("jugcraft:gone");
		PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
		ForasEntity foras = foras(helper, perch);
		forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, now);
		helper.assertValueEqual(foras.think(level, 6000L, now + 1), Status.WORKING, "The host whose graft the rules no longer know is grafted");
		GraftKeeping.Grafted last = keeping(foras).last();
		helper.assertTrue(FORAS_GRAFT.equals(gone.graft()) && last != null && last.crop().equals(gone.getBlockPos().above()) && dewmossIn(chest) == 2,
				"Its bed now carries Dewlit, one Dewmoss spent: " + gone.graft() + ", " + last);
		helper.assertValueEqual(foras.think(level, 6000L, now + 2), Status.IDLE, "Every host carries a graft the rules know");
		helper.assertTrue(keeping(foras).idle() == GraftKeeping.Idle.NO_HOST && dewmossIn(chest) == 2 && FORAS_GRAFT.equals(known.graft())
				&& keeping(foras).last().crop().equals(gone.getBlockPos().above()), "Nothing more spent; the known graft never touched");
		helper.succeed();
	}

	/** GC-07 ("recover things lost"): a graft stays through a replanting; lost to another crop, its host is grafted again when it returns. */
	@GameTest(maxTicks = 20)
	public void forasGraftsAgainAfterAReplanting(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(3, 1, 3);
		ChestBlockEntity chest = forasStore(helper, at.east(), 3);
		BlockPos host = new BlockPos(1, 1, 1);
		VerdantBedBlockEntity bed = forasHost(helper, host, holder.getUUID());
		PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
		ForasEntity foras = foras(helper, perch);
		forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, now);
		helper.assertValueEqual(foras.think(level, 6000L, now + 1), Status.WORKING, "He grafts the host");
		helper.assertTrue(FORAS_GRAFT.equals(bed.graft()) && dewmossIn(chest) == 2, "Dewlit, one Dewmoss spent");
		helper.setBlock(host.above(), Blocks.AIR);
		helper.setBlock(host.above(), Garden.SUNPETAL_CROP);
		helper.assertValueEqual(foras.think(level, 6000L, now + 2), Status.IDLE, "Harvested and replanted, the host keeps its graft");
		helper.assertTrue(FORAS_GRAFT.equals(bed.graft()) && keeping(foras).idle() == GraftKeeping.Idle.NO_HOST && dewmossIn(chest) == 2,
				"and nothing is spent: " + bed.graft());
		// Growth's own call when another organism grew on the bed: the graft is lost.
		bed.clearGraftUnlessFor("jugcraft:gloamcap");
		helper.setBlock(host.above(), Blocks.AIR);
		helper.setBlock(host.above(), Garden.SUNPETAL_CROP);
		helper.assertTrue(bed.graft().isEmpty(), "Another crop grew there: the graft was lost");
		keeping(foras).forget();
		helper.assertValueEqual(foras.think(level, 6000L, now + 3), Status.WORKING, "The host returned, he grafts it again");
		helper.assertTrue(FORAS_GRAFT.equals(bed.graft()) && dewmossIn(chest) == 1, "Dewlit again, one more Dewmoss spent");
		helper.succeed();
	}

	/**
	 * GC-P83: he takes only from a plain container touching his perch that his holder could open and change by hand: a
	 * stranger's chest beside the perch is never his store, a chest two blocks off is none; one of the holder's beside it is.
	 * The stranger's chest, refused for itself, and the holder's, read empty, are not asked or read again until his index
	 * is: a Dewmoss put in it is found at that read.
	 */
	@GameTest(maxTicks = 20)
	public void forasTakesOnlyFromAStoreTheHolderMayChange(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(3, 1, 3);
		ChestBlockEntity claimedChest = forasStore(helper, at.east(), 2);
		ChestBlockEntity own = forasStore(helper, at.north(), 0);
		ChestBlockEntity far = forasStore(helper, at.offset(2, 0, 0).south(), 4);
		VerdantBedBlockEntity bed = forasHost(helper, new BlockPos(1, 1, 1), holder.getUUID());
		helper.assertTrue(!GardenReach.withStores(helper.absolutePos(at), 8, found -> true).stores(level, now).contains(far.getBlockPos()),
				"A chest two blocks off is no store of his");
		try (TestClaims claims = TestClaims.open()) {
			claims.block(claimedChest.getBlockPos(), STRANGER);
			PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
			ForasEntity foras = foras(helper, perch);
			forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, now);
			helper.assertValueEqual(foras.think(level, 6000L, now + 1), Status.IDLE, "A stranger's chest and an empty one: no donor he may take");
			helper.assertTrue(keeping(foras).idle() == GraftKeeping.Idle.NO_DONOR && dewmossIn(claimedChest) == 2 && dewmossIn(far) == 4
					&& bed.graft().isEmpty(), "Nothing taken from the stranger's chest or the far one: " + keeping(foras).idle());
			helper.assertTrue(keys(GraftKeeping.rows(level, pactOf(level, perch), now + 1)).contains(GRAFTING_KEY + "row.no_donor"),
					"His page says he waits for a donor");
			helper.assertTrue(keeping(foras).skipped(claimedChest.getBlockPos()) && keeping(foras).lacks(own.getBlockPos(), "jugcraft:dewmoss"),
					"The stranger's chest is skipped and the holder's remembered without a donor until his index is read again");
			own.setItem(0, new ItemStack(Garden.DEWMOSS, 1));
			helper.assertValueEqual(foras.think(level, 6000L, now + 2), Status.IDLE, "Before his index is read again, he does not read the chest");
			helper.assertTrue(keeping(foras).idle() == GraftKeeping.Idle.NO_DONOR && dewmossIn(own) == 1, "and still waits for a donor");
			keeping(foras).forget();
			helper.assertValueEqual(foras.think(level, 6000L, now + 3), Status.WORKING, "His index read again, the Dewmoss in the holder's own chest: he grafts");
			GraftKeeping.Grafted last = keeping(foras).last();
			helper.assertTrue(dewmossIn(claimedChest) == 2 && dewmossIn(own) == 0 && dewmossIn(far) == 4 && last != null
					&& last.store().equals(own.getBlockPos()) && FORAS_GRAFT.equals(bed.graft()), "From the holder's chest alone: " + last);
		}
		helper.succeed();
	}

	/** GC-S7: a host whose bed, or whose crop, its holder may not change by hand is refused, nothing spent; once they may, he grafts. */
	@GameTest(maxTicks = 20)
	public void forasRefusedWhereTheHolderMayNotBuild(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(3, 1, 3);
		ChestBlockEntity chest = forasStore(helper, at.east(), 2);
		BlockPos first = new BlockPos(1, 1, 1);
		BlockPos second = new BlockPos(5, 1, 1);
		VerdantBedBlockEntity bedClaimed = forasHost(helper, first, holder.getUUID());
		VerdantBedBlockEntity cropClaimed = forasHost(helper, second, holder.getUUID());
		PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
		ForasEntity foras = foras(helper, perch);
		forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, now);
		try (TestClaims claims = TestClaims.open()) {
			claims.block(helper.absolutePos(first), STRANGER).block(helper.absolutePos(second.above()), STRANGER);
			helper.assertValueEqual(foras.think(level, 6000L, now + 1), Status.IDLE, "Neither host is his holder's to change by hand");
			helper.assertTrue(keeping(foras).idle() == GraftKeeping.Idle.NO_HOST && bedClaimed.graft().isEmpty() && cropClaimed.graft().isEmpty()
					&& dewmossIn(chest) == 2, "Nothing grafted, nothing spent");
		}
		keeping(foras).forget();
		helper.assertValueEqual(foras.think(level, 6000L, now + 2), Status.WORKING, "The claims lifted, he grafts");
		helper.assertTrue((FORAS_GRAFT.equals(bedClaimed.graft()) ? 1 : 0) + (FORAS_GRAFT.equals(cropClaimed.graft()) ? 1 : 0) == 1 && dewmossIn(chest) == 1,
				"One host, one Dewmoss");
		helper.succeed();
	}

	/**
	 * GC-07 (GC-P85, the unit review): a place refused for itself is skipped until his index is read again, so refused
	 * places never crowd the rest out of a think's questions. Seven hosts of the holder's party, nearer the perch than the
	 * holder's own, stand in a claim the holder is not trusted in: his first think spends seven of its eight questions on
	 * their crops and the eighth on the holder's crop, and grafts nothing; his second skips the seven and grafts the
	 * holder's host.
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 20)
	public void forasSkipsPlacesRefusedForThemselves(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		ServerPlayer member = gardenHolder(helper);
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(holder.getUUID(), member.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(member.getUUID(), ms) == PartyManager.Result.OK, "The holder and a party member");
			long now = dayStart(level, 1);
			BlockPos at = new BlockPos(8, 1, 8);
			ChestBlockEntity chest = forasStore(helper, at.east(), 2);
			// The party member's seven hosts two blocks north of the perch (at most 13 off, squared); the holder's four north (16).
			List<VerdantBedBlockEntity> theirs = new ArrayList<>();
			List<BlockPos> claimed = new ArrayList<>();
			for (int x = 5; x <= 11; x++) {
				BlockPos bed = new BlockPos(x, 1, 6);
				theirs.add(forasHost(helper, bed, member.getUUID()));
				claimed.add(helper.absolutePos(bed.above()));
			}
			VerdantBedBlockEntity own = forasHost(helper, new BlockPos(8, 1, 4), holder.getUUID());
			try (TestClaims claims = TestClaims.open()) {
				for (BlockPos crop : claimed) {
					claims.block(crop, STRANGER);
				}
				PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
				ForasEntity foras = foras(helper, perch);
				forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, now);
				helper.assertValueEqual(foras.think(level, 6000L, now + 1), Status.IDLE,
						"Seven refused crops and the holder's crop spend his eight questions: nothing grafted");
				GraftKeeping.Memory memory = keeping(foras);
				helper.assertTrue(own.graft().isEmpty() && dewmossIn(chest) == 2 && claimed.stream().allMatch(memory::skipped)
						&& memory.idle() == GraftKeeping.Idle.NO_HOST, "The seven are skipped from now on, and Jade has a reason: " + memory.idle());
				helper.assertValueEqual(foras.think(level, 6000L, now + 2), Status.WORKING, "His next think skips them and grafts the holder's host");
				helper.assertTrue(FORAS_GRAFT.equals(own.graft()) && dewmossIn(chest) == 1 && theirs.stream().allMatch(bed -> bed.graft().isEmpty()),
						"The holder's host carries Dewlit, one Dewmoss spent; no claimed host is touched");
			}
		} finally {
			parties.adminDisband(holder.getUUID());
		}
		helper.succeed();
	}

	/** GC-07: a stranger's bed, or a bed nobody woke, is never his host. */
	@GameTest(maxTicks = 20)
	public void forasNeverGraftsAStrangersBed(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(3, 1, 3);
		ChestBlockEntity chest = forasStore(helper, at.east(), 2);
		VerdantBedBlockEntity strangers = forasHost(helper, new BlockPos(1, 1, 1), STRANGER);
		BlockPos dormantAt = new BlockPos(5, 1, 1);
		helper.setBlock(dormantAt, Garden.VERDANT_BED.defaultBlockState().setValue(VerdantBedBlock.MOISTURE, VerdantBedBlock.WET));
		helper.setBlock(dormantAt.above(), Garden.SUNPETAL_CROP);
		VerdantBedBlockEntity dormant = helper.getBlockEntity(dormantAt, VerdantBedBlockEntity.class);
		PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
		ForasEntity foras = foras(helper, perch);
		forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, now);
		helper.assertValueEqual(foras.think(level, 6000L, now + 1), Status.IDLE, "A stranger's bed and a sleeping one hold no host of his");
		helper.assertValueEqual(foras.think(level, 6000L, now + 2), Status.IDLE, "and never do");
		helper.assertTrue(keeping(foras).idle() == GraftKeeping.Idle.NO_HOST && strangers.graft().isEmpty() && dormant.graft().isEmpty()
				&& !dormant.awake() && dewmossIn(chest) == 2, "Nothing grafted, nothing spent");
		helper.succeed();
	}

	/** GC-07 (GE5): two holders' Foras (a party) over one host graft it once, one Dewmoss in all; the second finds it kept. */
	@GameTest(maxTicks = 20)
	public void twoForasNeverGraftOneBedTwice(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer first = gardenHolder(helper);
		ServerPlayer second = gardenHolder(helper);
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(first.getUUID(), second.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(second.getUUID(), ms) == PartyManager.Result.OK, "Two holders in one party");
			long now = dayStart(level, 1);
			BlockPos atA = new BlockPos(1, 1, 5);
			BlockPos atB = new BlockPos(6, 1, 5);
			ChestBlockEntity storeA = forasStore(helper, atA.east(), 2);
			ChestBlockEntity storeB = forasStore(helper, atB.east(), 2);
			VerdantBedBlockEntity bed = forasHost(helper, new BlockPos(3, 1, 1), first.getUUID());
			PactPerchBlockEntity a = forasPact(helper, first, atA, now);
			PactPerchBlockEntity b = forasPact(helper, second, atB, now);
			ForasEntity one = foras(helper, a), other = foras(helper, b);
			forasAnswer(helper, first, a, GraftKeeping.ALWAYS, now);
			forasAnswer(helper, second, b, GraftKeeping.ALWAYS, now);
			helper.assertValueEqual(one.think(level, 6000L, now + 1), Status.WORKING, "The first Foras grafts the party's host");
			helper.assertValueEqual(other.think(level, 6000L, now + 1), Status.IDLE, "The second, in the same tick, finds it grafted");
			helper.assertTrue(FORAS_GRAFT.equals(bed.graft()) && keeping(other).idle() == GraftKeeping.Idle.NO_HOST
					&& dewmossIn(storeA) + dewmossIn(storeB) == 3, "One graft, one Dewmoss in all: " + dewmossIn(storeA) + " + " + dewmossIn(storeB));
		} finally {
			parties.adminDisband(first.getUUID());
		}
		helper.succeed();
	}

	/**
	 * GC-P84, GC-P86 (Dossier 4 items 3 and 5): unanswered, Foras leaves a host the graft would not improve (full sky: ideal
	 * either way), names it once (his matter, raised with his inspect gesture) and awaits the answer; a host whose bed holds
	 * no reading he waits on too. Answered "where it helps", he still leaves both, Jade naming the first; that host gone,
	 * Jade says hosts wait for their bed's first reading (GC-P90).
	 */
	@GameTest(maxTicks = 20)
	public void aPrudentForasSkipsAUselessGraft(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		BlockPos at = new BlockPos(3, 1, 3);
		ChestBlockEntity chest = forasStore(helper, at.east(), 2);
		BlockPos open = new BlockPos(1, 1, 1);
		VerdantBedBlockEntity sampled = forasHost(helper, open, holder.getUUID());
		VerdantBedBlockEntity unread = forasHost(helper, new BlockPos(5, 1, 1), holder.getUUID());
		ConcordanceGardenGameTests.afterSampling(helper, List.of(sampled), () -> {
			long now = dayStart(level, 1);
			// Forgotten here, not at the start: its crop's random tick before now may have read it.
			unread.forgetSample();
			helper.assertTrue(unread.cachedHabitat(level) == null, "The second bed holds no reading");
			PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
			ForasEntity foras = foras(helper, perch);
			helper.assertValueEqual(foras.think(level, 6000L, now + 1), Status.AWAITING_ANSWER, "Unanswered, he names the host and awaits the answer (GC-P47)");
			PactService.Complication matter = GraftKeeping.SERVICE.complication(level, pactOf(level, perch), perch, now + 1);
			helper.assertTrue(cued(foras, "inspect") == 1 && keeping(foras).useless().equals(List.of(helper.absolutePos(open.above())))
					&& matter != null && GraftKeeping.NO_GAIN.equals(matter.id()) && Integer.valueOf(1).equals(arg(matter.line(), 0))
					&& matter.answers().equals(GraftKeeping.ACCORDS) && dewmossIn(chest) == 2,
					"His matter, raised with his inspect gesture, one host: " + keeping(foras).useless() + " " + matter);
			helper.assertValueEqual(foras.think(level, 6000L, now + 2), Status.AWAITING_ANSWER, "Still waiting");
			helper.assertTrue(cued(foras, "inspect") == 1, "and his matter is raised once a presence");
			forasAnswer(helper, holder, perch, GraftKeeping.PRUDENT, now + 2);
			helper.assertValueEqual(foras.think(level, 6000L, now + 3), Status.IDLE, "Answered where it helps, he leaves both");
			helper.assertTrue(sampled.graft().isEmpty() && unread.graft().isEmpty() && dewmossIn(chest) == 2
					&& new PactService.Note("no_gain", 1).equals(GraftKeeping.SERVICE.note(level, foras, pactOf(level, perch), 6000L, now + 3)),
					"Nothing grafted; Jade names the host a graft would not help");
			helper.setBlock(open.above(), Blocks.AIR);
			helper.assertValueEqual(foras.think(level, 6000L, now + 4), Status.IDLE, "That host gone, one waits for its reading");
			helper.assertTrue(new PactService.Note("no_reading", 1).equals(GraftKeeping.SERVICE.note(level, foras, pactOf(level, perch), 6000L, now + 4))
					&& GraftKeeping.SERVICE.complication(level, pactOf(level, perch), perch, now + 4) == null,
					"Jade says one host waits for its bed's first reading; no matter stands");
		});
	}

	/**
	 * GC-P84, GC-P86 (Dossier 4 items 3 and 5): unanswered, Foras grafts only where the graft helps. A Sunpetal shut in stone
	 * beside crying obsidian (light 9: short for it, tolerable grafted) gains; one in full sky (light 15: ideal either way)
	 * does not. His first think names the second (his matter, raised once with his inspect gesture), spends nothing and
	 * awaits the answer; his next grafts the first under the safe course, still awaiting it (GC-P47); the second is left, and
	 * his matter is not raised again. Sealed once the light engine has lit the new blocks (from the 30th tick, the beds
	 * sampled one a tick: the shared sampling rule).
	 */
	@GameTest(maxTicks = 80)
	public void aPrudentForasGraftsWhereItHelps(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		BlockPos at = new BlockPos(3, 1, 3);
		ChestBlockEntity chest = forasStore(helper, at.east(), 1);
		BlockPos shut = new BlockPos(6, 1, 1);
		BlockPos open = new BlockPos(1, 1, 1);
		VerdantBedBlockEntity shutBed = keptBed(helper, shut, holder.getUUID(), 0);
		VerdantBedBlockEntity openBed = keptBed(helper, open, holder.getUUID(), 0);
		BlockPos cell = shut.above();
		for (BlockPos wall : List.of(cell.above(), cell.north(), cell.east(), cell.west())) {
			helper.setBlock(wall, Blocks.STONE);
		}
		helper.setBlock(cell.south(), Blocks.CRYING_OBSIDIAN);
		helper.setBlock(cell, Garden.SUNPETAL_CROP);
		helper.setBlock(open.above(), Garden.SUNPETAL_CROP);
		sampledFrom(helper, 30, List.of(shutBed, openBed), () -> {
			long now = dayStart(level, 1);
			int dim = level.getRawBrightness(helper.absolutePos(cell), 0);
			int lit = level.getRawBrightness(helper.absolutePos(open.above()), 0);
			helper.assertTrue(dim == 9 && lit == 15, "The shut host reads light 9 and the open one 15: " + dim + ", " + lit);
			PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
			ForasEntity foras = foras(helper, perch);
			helper.assertValueEqual(foras.think(level, 6000L, now + 1), Status.AWAITING_ANSWER,
					"Unanswered, he first names the host a graft would not help, and awaits the answer (GC-P47)");
			helper.assertTrue(keeping(foras).useless().equals(List.of(helper.absolutePos(open.above()))) && dewmossIn(chest) == 1
					&& cued(foras, "inspect") == 1 && foras.attending() != null,
					"The open host is his matter, raised with his inspect gesture toward it; nothing spent: " + keeping(foras).useless());
			helper.assertValueEqual(foras.think(level, 6000L, now + 2), Status.AWAITING_ANSWER,
					"Then he grafts the host it helps under the safe answer, still awaiting it (GC-P47; unit 2's B-06)");
			helper.assertTrue(FORAS_GRAFT.equals(shutBed.graft()) && openBed.graft().isEmpty() && dewmossIn(chest) == 0
					&& Pacts.done(pactOf(level, perch).pact(), now + 2) == 1,
					"The shut host carries Dewlit, one graft counted; the open one does not: " + shutBed.graft() + ", " + openBed.graft());
			foras.think(level, 6000L, now + 3);
			helper.assertTrue(openBed.graft().isEmpty() && cued(foras, "inspect") == 1, "The open host is left, and his matter is raised once a presence");
		});
	}

	/** GC-P86: answered "graft every host", he grafts a host the graft would not help and one whose bed holds no reading; his matter stands no more. */
	@GameTest(maxTicks = 20)
	public void anAlwaysAnswerGraftsEveryHost(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		BlockPos at = new BlockPos(3, 1, 3);
		ChestBlockEntity chest = forasStore(helper, at.east(), 3);
		VerdantBedBlockEntity useless = forasHost(helper, new BlockPos(1, 1, 1), holder.getUUID());
		VerdantBedBlockEntity unread = forasHost(helper, new BlockPos(5, 1, 1), holder.getUUID());
		ConcordanceGardenGameTests.afterSampling(helper, List.of(useless), () -> {
			long now = dayStart(level, 1);
			PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
			ForasEntity foras = foras(helper, perch);
			forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, now);
			helper.assertValueEqual(foras.think(level, 6000L, now + 1), Status.WORKING, "Answered every host, he grafts");
			helper.assertValueEqual(foras.think(level, 6000L, now + 2), Status.WORKING, "and grafts the other");
			PactService.Note note = GraftKeeping.SERVICE.note(level, foras, pactOf(level, perch), 6000L, now + 2);
			helper.assertTrue(FORAS_GRAFT.equals(useless.graft()) && FORAS_GRAFT.equals(unread.graft()) && dewmossIn(chest) == 1
					&& (note == null || !"no_gain".equals(note.id()))
					&& GraftKeeping.SERVICE.complication(level, pactOf(level, perch), perch, now + 2) == null,
					"Both carry Dewlit, two Dewmoss spent; no matter stands: " + note);
		});
	}

	/** GC-P81: his terms' grafts a game day (8) and no more; the next day he goes on. */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 20)
	public void forasStopsAtHisQuota(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(8, 1, 8);
		ChestBlockEntity chest = forasStore(helper, at.east(), 16);
		List<VerdantBedBlockEntity> beds = new ArrayList<>();
		for (int x = 4; x <= 12; x++) {
			beds.add(forasHost(helper, new BlockPos(x, 1, 4), holder.getUUID()));
		}
		PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
		ForasEntity foras = foras(helper, perch);
		forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, now);
		int quota = forasOffer().quota();
		helper.assertValueEqual(quota, 8, "His terms: 8 grafts a game day (GC-P81)");
		for (int k = 0; k < quota; k++) {
			helper.assertValueEqual(foras.think(level, 6000L, now + 20L * k), Status.WORKING, "Graft " + k + " of his " + quota);
		}
		helper.assertValueEqual(foras.think(level, 6000L, now + 20L * quota), Status.FINISHED, "His day's grafts spent, he stops");
		helper.assertTrue(dewmossIn(chest) == 8 && beds.stream().filter(bed -> bed.graft().isEmpty()).count() == 1
				&& Pacts.done(pactOf(level, perch).pact(), now) == quota, "Eight grafts, eight Dewmoss, one host left: " + dewmossIn(chest));
		helper.assertValueEqual(foras.think(level, 6000L, dayStart(level, 2)), Status.WORKING, "The next game day he grafts again");
		helper.succeed();
	}

	/** GC-07 (GE1, GE2): no stand-in; nothing grafted for a holder away; back beside the perch, he grafts. */
	@GameTest(maxTicks = 20)
	public void forasWaitsForAPresentHolder(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(3, 1, 3);
		ChestBlockEntity chest = forasStore(helper, at.east(), 2);
		VerdantBedBlockEntity bed = forasHost(helper, new BlockPos(1, 1, 1), holder.getUUID());
		PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
		ForasEntity foras = foras(helper, perch);
		forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, now);
		BlockPos home = helper.absolutePos(at);
		holder.snapTo(home.getX() + 70.5, home.getY(), home.getZ() + 0.5, 0.0F, 0.0F);
		helper.assertValueEqual(foras.think(level, 6000L, now + 1), Status.OWNER_OFFLINE, "70 blocks off: not near, he waits");
		helper.assertTrue(bed.graft().isEmpty() && dewmossIn(chest) == 2 && Pacts.done(pactOf(level, perch).pact(), now) == 0,
				"Nothing grafted, spent or counted meanwhile");
		beside(helper, holder, at);
		helper.assertValueEqual(foras.think(level, 6000L, now + 2), Status.WORKING, "Back beside the perch, he grafts");
		helper.assertTrue(FORAS_GRAFT.equals(bed.graft()) && dewmossIn(chest) == 1, "Dewlit, one Dewmoss");
		helper.succeed();
	}

	/**
	 * GC-07 (GC-P90's first obligation): the holder's own graft by hand in his area while the pact is active keeps it, once;
	 * outside his area, or while the pact is suspended, it keeps nothing; a second graft by hand records nothing more.
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 20)
	public void forassFirstObligationIsAGraftByHand(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		long g0 = level.getGameTime();
		BlockPos at = new BlockPos(2, 1, 2);
		PactPerchBlockEntity perch = forasPact(helper, holder, at, g0);
		BlockPos outside = new BlockPos(12, 1, 2);
		List<BlockPos> inside = List.of(new BlockPos(4, 1, 2), new BlockPos(2, 1, 5), new BlockPos(5, 1, 5));
		forasHost(helper, outside, holder.getUUID());
		for (BlockPos host : inside) {
			forasHost(helper, host, holder.getUUID());
		}
		helper.assertTrue(GraftKeeping.SERVICE.obligation() == PactObligations.Act.GRAFT
				&& keys(GraftKeeping.SERVICE.obligationRows(level, holder, pactOf(level, perch), g0))
						.equals(List.of("message.jugcraft.concordance.garden_pact.obligation")), "His page names the act");
		int heard = GARDEN_SERVED.getOrDefault(holder.getUUID(), 0);
		ConcordanceGardenGameTests.handUse(helper, holder, outside.above(), new ItemStack(Garden.DEWMOSS), false);
		helper.assertTrue(FORAS_GRAFT.equals(helper.getBlockEntity(outside, VerdantBedBlockEntity.class).graft())
				&& !PactObligations.kept(holder, ForasEntity.COUNTERPART) && GARDEN_SERVED.getOrDefault(holder.getUUID(), 0) == heard,
				"A graft by hand 10 blocks off, outside his area, keeps nothing");
		helper.assertTrue(PactKeeping.suspend(level, holder, perch.holder(), g0) == AgreementHolder.Toggle.SUSPENDED, "Its holder suspends it");
		ConcordanceGardenGameTests.handUse(helper, holder, inside.get(0).above(), new ItemStack(Garden.DEWMOSS), false);
		helper.assertTrue(FORAS_GRAFT.equals(helper.getBlockEntity(inside.get(0), VerdantBedBlockEntity.class).graft())
				&& !PactObligations.kept(holder, ForasEntity.COUNTERPART) && GARDEN_SERVED.getOrDefault(holder.getUUID(), 0) == heard,
				"Suspended, a graft by hand in his area keeps nothing");
		helper.assertTrue(PactKeeping.resume(level, holder, perch.holder(), g0) == AgreementHolder.Toggle.RESUMED, "Resumed");
		ConcordanceGardenGameTests.handUse(helper, holder, inside.get(1).above(), new ItemStack(Garden.DEWMOSS), false);
		helper.assertTrue(PactObligations.kept(holder, ForasEntity.COUNTERPART) && GARDEN_SERVED.getOrDefault(holder.getUUID(), 0) == heard + 1
				&& keys(GraftKeeping.SERVICE.obligationRows(level, holder, pactOf(level, perch), g0))
						.equals(List.of("message.jugcraft.concordance.garden_pact.obligation_kept"))
				&& PactObligations.key(ForasEntity.COUNTERPART).equals("practice:" + GardenPacts.ACTIVITY + ":" + ForasEntity.COUNTERPART),
				"A graft by hand in his area while the pact is active keeps it, once");
		ConcordanceGardenGameTests.handUse(helper, holder, inside.get(2).above(), new ItemStack(Garden.DEWMOSS), false);
		helper.assertTrue(GARDEN_SERVED.getOrDefault(holder.getUUID(), 0) == heard + 1, "and it is not recorded again");
		helper.succeed();
	}

	/**
	 * GC-07 (detail b): two grafts, and his kept page's rows say the day's count against his quota first, then his last graft
	 * (its host and its store), with his first obligation after; his count aloud is the same; no row names a player.
	 */
	@GameTest(maxTicks = 20)
	public void theKeptPageCountsForasGrafts(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(3, 1, 3);
		ChestBlockEntity chest = forasStore(helper, at.east(), 3);
		BlockPos nearer = new BlockPos(2, 1, 1);
		BlockPos farther = new BlockPos(6, 1, 1);
		forasHost(helper, nearer, holder.getUUID());
		forasHost(helper, farther, holder.getUUID());
		PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
		ForasEntity foras = foras(helper, perch);
		forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, now);
		helper.assertValueEqual(foras.think(level, 6000L, now + 1), Status.WORKING, "The nearer host");
		helper.assertValueEqual(foras.think(level, 6000L, now + 2), Status.WORKING, "and the farther");
		List<Component> rows = GraftKeeping.rows(level, pactOf(level, perch), now + 2);
		helper.assertTrue(keys(rows).equals(List.of(GRAFTING_KEY + "row.service", GRAFTING_KEY + "row.last"))
				&& Integer.valueOf(2).equals(arg(rows.get(0), 0)) && Integer.valueOf(8).equals(arg(rows.get(0), 1))
				&& helper.absolutePos(farther.above()).toShortString().equals(arg(rows.get(1), 0))
				&& chest.getBlockPos().toShortString().equals(arg(rows.get(1), 1)) && dewmossIn(chest) == 1,
				"2 of 8 grafts today, then his last, the farther host from the chest: " + rows);
		Component count = GraftKeeping.countLine(pactOf(level, perch).pact(), now + 2);
		helper.assertTrue(key(count).equals(GRAFTING_KEY + "counted") && Integer.valueOf(2).equals(arg(count, 0)) && Integer.valueOf(8).equals(arg(count, 1)),
				"He counts aloud the same: 2 of 8 grafts today");
		PactMenu menu = open(helper, holder, perch);
		List<String> page = keys(menu.lastSent().notes());
		int service = page.indexOf(GRAFTING_KEY + "row.service");
		int last = page.indexOf(GRAFTING_KEY + "row.last");
		int obligation = page.indexOf("message.jugcraft.concordance.garden_pact.obligation");
		String shown = menu.lastSent().notes().stream().map(Component::getString).toList().toString();
		helper.assertTrue(service >= 0 && service < last && last < obligation && !shown.contains(holder.getName().getString())
				&& !shown.contains(holder.getUUID().toString()), "His kept page: the count, his last graft, then his first obligation; no player named: " + page);
		holder.closeContainer();
		helper.succeed();
	}

	/**
	 * GC-07 (Jade; GC-P90): the readings of his perch and of him: kept, his work, active; with a host the graft would not
	 * help, both carry his note (no_gain, one host) and his matter, naming no player; answered "graft every host" with his
	 * store empty, his note is that no donor waits.
	 */
	@GameTest(maxTicks = 20)
	public void forassReadingSaysItsNoteAndNamesNoPlayer(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		BlockPos at = new BlockPos(3, 1, 3);
		forasStore(helper, at.east(), 0);
		VerdantBedBlockEntity sampled = forasHost(helper, new BlockPos(1, 1, 1), holder.getUUID());
		VerdantBedBlockEntity unread = forasHost(helper, new BlockPos(5, 1, 1), holder.getUUID());
		ConcordanceGardenGameTests.afterSampling(helper, List.of(sampled), () -> {
			long now = dayStart(level, 1);
			// Forgotten here, not at the start: its crop's random tick before now may have read it.
			unread.forgetSample();
			PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
			ForasEntity foras = foras(helper, perch);
			helper.assertValueEqual(foras.think(level, 6000L, now + 1), Status.AWAITING_ANSWER, "A host the graft would not help: his matter waits");
			CompoundTag ofPerch = PactReading.holder(level, perch, now + 1);
			CompoundTag ofHim = PactReading.presence(level, foras, now + 1);
			for (CompoundTag reading : List.of(ofPerch, ofHim)) {
				helper.assertTrue("kept".equals(reading.getStringOr("standing", "")) && GraftKeeping.WORK.equals(reading.getStringOr("service", ""))
						&& "no_gain".equals(reading.getStringOr("note", "")) && reading.getIntOr("note_count", 0) == 1
						&& GraftKeeping.NO_GAIN.equals(reading.getStringOr("matter", "")) && !reading.contains("accord")
						&& PactReading.KEYS.containsAll(reading.keySet()), "His note and his matter, no answer yet: " + reading);
			}
			helper.assertTrue(ForasEntity.COUNTERPART.equals(ofPerch.getStringOr("counterpart", "")), "The perch's reading names him: " + ofPerch);
			forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, now + 1);
			helper.assertValueEqual(foras.think(level, 6000L, now + 2), Status.IDLE, "Every host his, but no donor in his store");
			CompoundTag waiting = PactReading.presence(level, foras, now + 2);
			helper.assertTrue("no_donor".equals(waiting.getStringOr("note", "")) && keeping(foras).idle() == GraftKeeping.Idle.NO_DONOR,
					"Jade says no donor waits: " + waiting);
			String all = ofPerch + " " + ofHim + " " + waiting;
			helper.assertTrue(!all.contains(holder.getUUID().toString()) && !all.contains(holder.getName().getString()), "and no player is named: " + all);
		});
	}

	/**
	 * GC-P89 (unit 3's Station.BESIDE, GC-P67): summoned for a holder south of his perch, Foras stands on the ground two
	 * blocks west of it (a quarter turn clockwise from them), facing them; led off, he is back at his next think; his side
	 * walled, he takes the next clear side clockwise from it (north); with no side clear, the perch's top.
	 */
	@GameTest(maxTicks = 20)
	public void forasStandsBesideThePerchFacingHisInviter(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(3, 1, 3);
		floor(helper);
		forasStore(helper, at.east(), 0);
		forasHost(helper, new BlockPos(1, 1, 1), holder.getUUID());
		ForasEntity foras = foras(helper, forasPact(helper, holder, at, now));
		Vec3 west = Vec3.atBottomCenterOf(helper.absolutePos(at.west(2)));
		helper.assertTrue(foras.position().distanceTo(west) < 1.0E-3, "He stands on the ground two blocks west of his perch: " + foras.position());
		float facing = PactSpiritEntity.bearing(foras.position(), holder.position(), 0.0F);
		helper.assertTrue(Math.abs(Mth.wrapDegrees(foras.getYRot() - facing)) < 1.0F, "and faces his holder: " + foras.getYRot() + " for " + facing);
		foras.teleportTo(west.x, west.y, west.z + 3.0);
		foras.think(level, 6000L, now + 1);
		helper.assertTrue(foras.position().distanceTo(west) < 1.0E-3, "Led off, he is back on his side at his next think: " + foras.position());
		helper.setBlock(at.west(), Blocks.STONE);
		foras.think(level, 6000L, now + 2);
		helper.assertTrue(foras.position().distanceTo(Vec3.atBottomCenterOf(helper.absolutePos(at.north(2)))) < 1.0E-3,
				"His side walled, he takes the next clear side clockwise from it, the north: " + foras.position());
		helper.setBlock(at.north(), Blocks.STONE);
		helper.setBlock(at.south(), Blocks.STONE);
		foras.think(level, 6000L, now + 3);
		helper.assertTrue(foras.position().distanceTo(Vec3.atBottomCenterOf(helper.absolutePos(at.above()))) < 1.0E-3,
				"With no side clear (his store fills the east) he stands on the perch's top: " + foras.position());
		helper.succeed();
	}

	/**
	 * GC-P88 (Dossier 4 item 2): with a host near but no plain container touching the perch (a chest two blocks off is none),
	 * he says why in his own words, is listed not ready and does not come; accepting is refused with nothing spent; a chest
	 * touching the perch, nothing is missing.
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 20)
	public void forasRefusesWithoutAStore(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(Language.getInstance().has(GRAFTING_KEY + "missing.store"), "the server reads the mod's words (Fabric's language loader)");
		ServerPlayer keeper = gardenHolder(helper);
		pocketFor(keeper, forasOffer(), 1);
		BlockPos at = new BlockPos(4, 1, 4);
		PactPerchBlockEntity perch = perch(helper, at, keeper);
		beside(helper, keeper, at);
		forasHost(helper, new BlockPos(8, 1, 4), keeper.getUUID());
		forasStore(helper, at.east(2), 0);
		List<PactService.Missing> missing = GraftKeeping.SERVICE.missing(level, keeper, perch, forasOffer());
		helper.assertTrue(missing.stream().map(PactService.Missing::reason).toList().equals(List.of(GraftKeeping.NO_STORE))
				&& missing.getFirst().note().getString().startsWith("\"Where shall I keep the cuttings? Set a store beside me.\""),
				"No store touches the perch: that alone is missing, in his words: " + missing);
		PactMenu chooser = open(helper, keeper, perch);
		PactStatePayload.Choice listed = chooser.lastSent().patterns().stream().filter(choice -> choice.id().equals(ForasEntity.SIGN)).findFirst().orElse(null);
		helper.assertTrue(chooser.lastSent().mode() == PactStatePayload.INVITE && listed != null && !listed.ready() && listed.amount() == 1,
				"The chooser lists him not ready, one thing missing: " + listed);
		keeper.closeContainer();
		RateGate.forget(keeper.getUUID());
		PactMenu menu = offerOf(helper, keeper, perch, forasOffer());
		PactStatePayload state = menu.lastSent();
		helper.assertTrue(keys(state.notes()).contains(GRAFTING_KEY + "missing.store") && perch.visitor() == null,
				"He says why and does not come: " + keys(state.notes()));
		PactStatePayload refused = send(keeper, menu, PactRequestPayload.ACCEPT, 0);
		helper.assertTrue(answered(refused, GraftKeeping.NO_STORE) && !refused.result().get().done()
				&& said(refused).equals(GRAFTING_KEY + "missing.store") && pocketed(keeper, 1, forasOffer()) && recordOf(level, keeper) == null,
				"Accepting is refused with his reason; nothing spent, nothing held");
		keeper.closeContainer();
		forasStore(helper, at.east(), 0);
		helper.assertTrue(GraftKeeping.SERVICE.missing(level, keeper, perch, forasOffer()).isEmpty(), "A chest touching the perch: nothing is missing");
		helper.succeed();
	}

	/**
	 * GC-P88: with a store touching the perch but no crop of a known graft's host on a bed the inviter keeps within 8 (a
	 * Gloamcap takes no graft; a Sunpetal 9 off is outside), he says so; a grafted Sunpetal within 8 counts, grafted or not.
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 20)
	public void forasRefusesWithoutAHost(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = gardenHolder(helper);
		BlockPos at = new BlockPos(4, 1, 4);
		PactPerchBlockEntity perch = perch(helper, at, keeper);
		beside(helper, keeper, at);
		forasStore(helper, at.east(), 0);
		keptBed(helper, new BlockPos(2, 1, 4), keeper.getUUID(), 0);
		helper.setBlock(new BlockPos(2, 2, 4), Garden.GLOAMCAP_CROP);
		forasHost(helper, new BlockPos(13, 1, 4), keeper.getUUID());
		List<PactService.Missing> missing = GraftKeeping.SERVICE.missing(level, keeper, perch, forasOffer());
		helper.assertTrue(missing.stream().map(PactService.Missing::reason).toList().equals(List.of(GraftKeeping.NO_HOST))
				&& key(missing.getFirst().note()).equals(GRAFTING_KEY + "missing.host") && Integer.valueOf(8).equals(arg(missing.getFirst().note(), 0)),
				"No host within 8 (a Gloamcap takes no graft, a Sunpetal 9 off is outside): " + missing);
		forasHost(helper, new BlockPos(6, 1, 4), keeper.getUUID()).setGraft(FORAS_GRAFT);
		helper.assertTrue(GraftKeeping.SERVICE.missing(level, keeper, perch, forasOffer()).isEmpty(), "A Sunpetal within 8, grafted or not: nothing missing");
		helper.succeed();
	}

	/** GC-07: which gesture answers which action, which loop each state shows, every cue's sound registered. */
	@GameTest(maxTicks = 20)
	public void forasAnswersEachActionWithHisGesture(GameTestHelper helper) {
		helper.assertTrue(ForasEntity.reaction(PactRequestPayload.ACCEPT, PactMenu.SEALED, true) == ForasEntity.Gesture.ASSENT
				&& ForasEntity.reaction(PactRequestPayload.ACCEPT, GraftKeeping.NO_STORE, false) == ForasEntity.Gesture.REFUSE
				&& ForasEntity.reaction(PactRequestPayload.ACCEPT, GraftKeeping.NO_HOST, false) == ForasEntity.Gesture.REFUSE
				&& ForasEntity.reaction(PactRequestPayload.DEPOSIT, PactMenu.DEPOSITED, true) == ForasEntity.Gesture.INSPECT
				&& ForasEntity.reaction(PactRequestPayload.DEPOSIT, PactMenu.NOTHING_DEPOSITED, false) == null
				&& ForasEntity.reaction(PactRequestPayload.RESUME, "resumed", true) == ForasEntity.Gesture.ASSENT
				&& ForasEntity.reaction(PactRequestPayload.RESOLVE, "resolved", true) == ForasEntity.Gesture.ASSENT
				&& ForasEntity.reaction(PactRequestPayload.RESOLVE, "resolved", false) == null
				&& ForasEntity.reaction(PactRequestPayload.RECALL, "recalled", true) == null
				&& ForasEntity.reaction(PactRequestPayload.ACCEPT, PactMenu.NOT_OFFERED, false) == null, "His answers");
		helper.assertTrue(ForasEntity.loop(Status.WORKING, false) == ForasEntity.Loop.IDLE && ForasEntity.loop(Status.IDLE, true) == ForasEntity.Loop.LISTEN
				&& ForasEntity.loop(Status.OWNER_OFFLINE, false) == ForasEntity.Loop.WAITING
				&& ForasEntity.loop(Status.UPKEEP_DUE, false) == ForasEntity.Loop.ASTIR && ForasEntity.loop(Status.UPKEEP_HELD, true) == ForasEntity.Loop.LISTEN
				&& ForasEntity.loop(Status.SUSPENDED, false) == ForasEntity.Loop.SUSPENDED && ForasEntity.loop(Status.DISABLED, true) == ForasEntity.Loop.WAITING
				&& ForasEntity.loop(Status.FINISHED, true) == ForasEntity.Loop.LISTEN && ForasEntity.loop(Status.FINISHED, false) == ForasEntity.Loop.WAITING
				&& ForasEntity.loop(Status.AWAITING_ANSWER, false) == ForasEntity.Loop.IDLE, "His loops");
		for (String cue : ForasEntity.SOUND_IDS) {
			helper.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(Jugcraft.id("foras." + cue)), "jugcraft:foras." + cue + " is registered");
		}
		helper.succeed();
	}

	/** GC-07: a second Foras at the perch, or a copy of his save, is no presence: it leaves at once and grafts nothing; his own grafts once. */
	@GameTest(maxTicks = 20)
	public void aDuplicateForasGraftsNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = gardenHolder(helper);
		long now = dayStart(level, 1);
		BlockPos at = new BlockPos(3, 1, 3);
		ChestBlockEntity chest = forasStore(helper, at.east(), 1);
		VerdantBedBlockEntity bed = forasHost(helper, new BlockPos(1, 1, 1), holder.getUUID());
		PactPerchBlockEntity perch = forasPact(helper, holder, at, now);
		ForasEntity foras = foras(helper, perch);
		forasAnswer(helper, holder, perch, GraftKeeping.ALWAYS, now);
		ForasEntity stranger = Workers.FORAS.create(level, EntitySpawnReason.MOB_SUMMONED);
		stranger.setOwner(holder.getUUID());
		stranger.setPerch(perch.getBlockPos());
		helper.assertValueEqual(stranger.think(level, 6000L, now + 1), Status.FINISHED, "A Foras not the perch's presence leaves");
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		foras.saveWithoutId(saved);
		ForasEntity twin = Workers.FORAS.create(level, EntitySpawnReason.LOAD);
		twin.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
		twin.setUUID(UUID.randomUUID());
		helper.assertValueEqual(twin.think(level, 6000L, now + 1), Status.FINISHED, "A copy of his save, under another id, leaves");
		helper.assertTrue(bed.graft().isEmpty() && dewmossIn(chest) == 1 && keeping(stranger).last() == null && keeping(twin).last() == null
				&& Pacts.done(pactOf(level, perch).pact(), now) == 0 && stranger.gone() && twin.gone(), "Neither grafted, spent or counted anything");
		helper.assertValueEqual(foras.think(level, 6000L, now + 2), Status.WORKING, "The presence grafts, once");
		helper.assertTrue(FORAS_GRAFT.equals(bed.graft()) && dewmossIn(chest) == 0, "By him alone");
		helper.succeed();
	}

	// GC-08 unit 5
	// GC-09 unit 6
}
