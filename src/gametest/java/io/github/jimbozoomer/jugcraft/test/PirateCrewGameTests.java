package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel;
import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel.Role;
import io.github.jimbozoomer.jugcraft.menagerie.OwnerPlunderer;
import io.github.jimbozoomer.jugcraft.menagerie.OwnerSupplementaries;
import io.github.jimbozoomer.jugcraft.menagerie.PirateCrew;
import io.github.jimbozoomer.jugcraft.test.TestVessels.StubVessel;
import io.github.jimbozoomer.jugcraft.town.JugcraftTown;
import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The crew of a moving pirate ship (docs/features/pirate-ship-crews.md), on the test-only stub vessel
 * ({@link TestVessels}): provisioning, seats turned once with the ship (feet on the deck), save and reload without
 * duplicates, waiting for an unloaded ship without loading it and giving up a destroyed one, boarding and the deck
 * fight, victory by the roster (deaths only, never absence or discards), the tether, sinking, targets, claims,
 * damage-only finite gunnery, and unlinked (galleon) plunderers unchanged.
 */
public class PirateCrewGameTests {
	private static final String ARENA = "jugcraft-test:arms_arena";

	@GameTest(structure = ARENA)
	public void crewProvisionsOncePerStationAndNeverTwice(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		h.assertTrue(PirateCrew.STATIONS.size() == 10, "Ten standard stations: helm, lookout, four gunners, four deck crew");
		StubVessel ship = ship(h, 0.0F, 8.5);
		h.assertTrue(PirateCrew.provision(level, ship) == 10 && ship.getPassengers().size() == 10, "One plunderer seated per station");
		level.addFreshEntityWithPassengers(ship);
		h.assertTrue(PirateCrew.provision(level, ship) == 0 && ship.getPassengers().size() == 10, "A crewed ship gets no second crew");
		var stations = new HashSet<Integer>();
		for (OwnerPlunderer crew : crew(ship)) {
			h.assertTrue(ship.getUUID().equals(crew.shipLink()) && crew.isPersistenceRequired(), "Each crew member is linked and persistent");
			h.assertTrue(stations.add(crew.shipStation()), "One crew member to a station");
			boolean gunner = PirateCrew.STATIONS.get(crew.shipStation()).role() == Role.GUNNER;
			h.assertTrue(count(crew, Items.GUNPOWDER) == (gunner ? 8 : 0) && count(crew, cannonball()) == (gunner ? 8 : 0),
					"Only gunners carry finite powder and shot");
		}
		h.assertTrue(PirateCrew.STATIONS.get(0).role() == Role.HELM && PirateCrew.STATIONS.get(1).role() == Role.LOOKOUT
				&& PirateCrew.STATIONS.subList(2, 6).stream().allMatch(s -> s.role() == Role.GUNNER)
				&& PirateCrew.STATIONS.subList(6, 10).stream().allMatch(s -> s.role() == Role.DECK), "Station order: helm, lookout, gunners, deck");
		h.assertTrue(PirateCrew.helmManned(ship) && !PirateCrew.defeated(ship), "A crewed ship has its helm and is not defeated");
		OwnerPlunderer stranger = OwnerPlunderer.TYPE.create(level, EntitySpawnReason.TRIGGERED);
		h.assertTrue(stranger != null && !PirateCrew.mayBoard(ship, stranger), "A plunderer of no ship cannot take a station");
		cleanup(level, ship);
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void seatsAndFacingTurnOnceWithTheShip(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		StubVessel ship = ship(h, 90.0F, 8.5);
		PirateCrew.provision(level, ship);
		level.addFreshEntityWithPassengers(ship);
		h.runAfterDelay(4, () -> {
			for (OwnerPlunderer crew : crew(ship)) {
				var station = PirateCrew.STATIONS.get(crew.shipStation());
				Vec3 expected = ship.position().add(station.seat().yRot(-90.0F * Mth.DEG_TO_RAD));
				h.assertTrue(Math.abs(crew.getX() - expected.x) < 1.0E-3 && Math.abs(crew.getZ() - expected.z) < 1.0E-3,
						"Station " + crew.shipStation() + " is its seat turned once by the ship's 90° yaw: " + crew.position() + " vs " + expected);
				h.assertTrue(Math.abs(crew.getY() - expected.y) < 1.0E-3, "Station " + crew.shipStation()
						+ " stands with its feet at the seat's height (deck 1.0, crow's nest 9.5), not its hips: " + crew.getY() + " vs " + expected.y);
				h.assertTrue(Math.abs(Mth.wrapDegrees(crew.yBodyRot - (90.0F + station.facing()))) < 0.01F,
						"Station " + crew.shipStation() + " faces the ship's yaw plus its facing");
			}
			AABB deck = ship.deckBounds();
			h.assertTrue(deck.contains(ship.position().add(-7.0, 1.5, 0.0)) && !deck.contains(ship.position().add(0.0, 1.5, -7.0)),
					"At yaw 90 the deck runs along x (the bow points to -x)");
			cleanup(level, ship);
			h.succeed();
		});
	}

	@GameTest(structure = ARENA)
	public void crewAndShipComeBackFromSaveWithoutDuplicates(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		StubVessel ship = ship(h, 0.0F, 8.5);
		PirateCrew.provision(level, ship);
		level.addFreshEntityWithPassengers(ship);
		ship.setHeaveTo(true);
		var shipSave = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		ship.saveWithoutId(shipSave);
		CompoundTag shipTag = shipSave.buildResult();
		h.assertTrue(shipTag.contains("Passengers"), "The game saves the crew inside the ship");
		List<CompoundTag> crewTags = new ArrayList<>();
		var identities = new HashSet<UUID>();
		for (OwnerPlunderer crew : crew(ship)) {
			var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
			crew.saveWithoutId(out);
			crewTags.add(out.buildResult());
			identities.add(crew.getUUID());
		}
		UUID shipId = ship.getUUID();
		unload(ship);
		var roster = PirateCrew.Roster.get(level);
		h.assertTrue(roster.knows(shipId) && !roster.defeated(shipId), "Unloading loses no crew member and forgets no ship");
		var reread = PirateCrew.Roster.CODEC.parse(NbtOps.INSTANCE, PirateCrew.Roster.CODEC.encodeStart(NbtOps.INSTANCE, roster).getOrThrow()).getOrThrow();
		h.assertTrue(reread.knows(shipId) && !reread.defeated(shipId), "The roster comes back from its save with the ship's crew");
		// Restore as the game's recursive load does: the ship, then each saved passenger forced back onto it.
		StubVessel restored = TestVessels.STUB.create(level, EntitySpawnReason.LOAD);
		restored.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), shipTag));
		h.assertTrue(shipId.equals(restored.getUUID()) && restored.heaveTo(), "The ship keeps its identity and heave-to");
		for (CompoundTag tag : crewTags) {
			OwnerPlunderer crew = OwnerPlunderer.TYPE.create(level, EntitySpawnReason.LOAD);
			crew.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
			h.assertTrue(shipId.equals(crew.shipLink()) && crew.shipStation() >= 0 && PirateCrew.mayBoard(restored, crew),
					"A reloaded crew member keeps its ship and its free station");
			h.assertTrue(crew.startRiding(restored, true, true), "It takes its station again");
		}
		level.addFreshEntityWithPassengers(restored);
		h.runAfterDelay(2, () -> {
			var linked = linked(level, restored);
			h.assertTrue(linked.size() == 10 && restored.getPassengers().size() == 10, "Exactly ten crew after reload, none duplicated");
			h.assertTrue(linked.stream().map(Entity::getUUID).collect(Collectors.toSet()).equals(identities), "The same crew identities");
			h.assertTrue(linked.stream().map(OwnerPlunderer::shipStation).collect(Collectors.toSet()).size() == 10, "Every station held once");
			h.assertTrue(PirateCrew.helmManned(restored), "The helm is manned again");
			cleanup(level, restored);
			h.succeed();
		});
	}

	@GameTest(structure = ARENA, maxTicks = 300)
	public void offShipCrewWaitForAnUnloadedShipAndGiveUpADestroyedOne(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		floor(h, 0, 15, 0, 15);
		BlockPos far = h.absolutePos(new BlockPos(8, 1, 8)).offset(4096, 0, 4096);
		h.assertTrue(!level.hasChunkAt(far), "Setup: the far ship position is in a chunk that is not loaded");
		// A ship that sailed out of loaded range, leaving a crew member ashore.
		StubVessel away = ship(h, 0.0F, 4.5);
		PirateCrew.provision(level, away);
		level.addFreshEntityWithPassengers(away);
		OwnerPlunderer waiting = station(away, 6);
		waiting.stopRiding();
		Vec3 ashore = h.absoluteVec(new Vec3(0.5, 2.0, 0.5));
		waiting.snapTo(ashore.x, ashore.y, ashore.z, 0.0F, 0.0F);
		waiting.sawShip(far);
		unload(away);
		// A ship destroyed under its crew.
		StubVessel wrecked = ship(h, 0.0F, 12.5);
		PirateCrew.provision(level, wrecked);
		level.addFreshEntityWithPassengers(wrecked);
		List<OwnerPlunderer> survivors = crew(wrecked);
		wrecked.discard();
		h.runAfterDelay(200, () -> {
			h.assertTrue(waiting.isAlive() && waiting.shipLink() != null && waiting.shipStation() == 6,
					"A crew member whose ship is out of loaded range waits for it, linked, however long");
			h.assertTrue(!level.hasChunkAt(far), "Waiting never loads the ship's chunk");
			h.assertTrue(PirateCrew.Roster.get(level).knows(away.getUUID()) && !PirateCrew.defeated(away),
					"An unloaded ship stays on the roster, undefeated");
			h.assertTrue(!PirateCrew.Roster.get(level).knows(wrecked.getUUID()), "A destroyed ship is forgotten");
			h.assertTrue(survivors.size() == 10 && survivors.stream().allMatch(c -> c.isAlive() && !c.isPassenger()
					&& c.shipLink() == null && c.shipStation() == -1), "A destroyed ship's crew give it up and fight on unlinked");
			waiting.discard();
			PirateCrew.forget(level, away.getUUID());
			survivors.forEach(Entity::discard);
			h.succeed();
		});
	}

	@GameTest(structure = ARENA, maxTicks = 200)
	public void boardingHeavesToAndSendsDeckCrewToFightUntilDefeated(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		floor(h, 5, 11, 0, 15);
		StubVessel ship = ship(h, 0.0F, 8.5);
		PirateCrew.provision(level, ship);
		level.addFreshEntityWithPassengers(ship);
		ServerPlayer player = boarder(h);
		player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0); // Struck, not killed, by four deck crew.
		player.setHealth(200.0F);
		Vec3 onDeck = h.absoluteVec(new Vec3(8.5, 2.0, 12.5));
		player.snapTo(onDeck.x, onDeck.y, onDeck.z, 180.0F, 0.0F);
		h.assertTrue(ship.deckBounds().contains(player.position()), "Setup: the player stands on the ship's deck");
		crew(ship).forEach(crew -> crew.setTarget(player));
		h.runAfterDelay(10, () -> {
			OwnerPlunderer helm = linked(level, ship).stream().filter(c -> c.shipStation() == 0).findFirst().orElse(null);
			h.assertTrue(ship.heaveTo(), "The helm heaves to for a boarder on the deck" + (helm == null ? " [no helm]" : why(level, helm, player)));
			var all = linked(level, ship);
			var deck = all.stream().filter(c -> PirateCrew.STATIONS.get(c.shipStation()).role() == Role.DECK).toList();
			h.assertTrue(deck.size() == 4 && deck.stream().noneMatch(Entity::isPassenger), "All four deck crew go over to fight, still linked");
			h.assertTrue(deck.stream().allMatch(c -> ship.deckBounds().inflate(0.5).contains(c.position())),
					"They step off their stations onto the deck, not wherever the ship would set a rider down");
			h.assertTrue(all.stream().filter(c -> !deck.contains(c)).allMatch(Entity::isPassenger), "Helm, lookout and gunners keep their stations");
			h.assertTrue(!PirateCrew.defeated(ship), "A ship with crew alive is not defeated");
		});
		h.runAfterDelay(50, () -> {
			h.assertTrue(player.getHealth() < player.getMaxHealth(),
					"The deck crew close and strike a boarder on the deck, where no path runs, by direct movement"
					+ linked(level, ship).stream().filter(c -> !c.isPassenger()).map(c -> " #" + c.shipStation() + " at "
							+ c.position() + why(level, c, player)).collect(Collectors.joining()));
			var all = linked(level, ship);
			OwnerPlunderer last = all.stream().filter(c -> c.shipStation() == 6).findFirst().orElseThrow();
			all.stream().filter(c -> c != last).forEach(c -> c.kill(level));
			h.assertTrue(!PirateCrew.defeated(ship), "One rostered crew member still alive keeps the ship undefeated");
			last.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
			h.assertTrue(PirateCrew.defeated(ship), "With every rostered crew member killed, the ship is defeated: its loot trigger");
			cleanup(level, ship);
			TestPlayers.leave(player);
			h.succeed();
		});
	}

	@GameTest(structure = ARENA, maxTicks = 100)
	public void victoryCountsTheRosterAndNeverAbsence(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		StubVessel ship = ship(h, 0.0F, 8.5);
		PirateCrew.provision(level, ship);
		level.addFreshEntityWithPassengers(ship);
		UUID shipId = ship.getUUID();
		List<OwnerPlunderer> all = crew(ship);
		// The bait: lured 80 blocks off, beyond any search around the ship, and alive.
		OwnerPlunderer bait = station(ship, 6);
		bait.stopRiding();
		Vec3 lured = ship.position().add(80.0, 1.0, 0.0);
		bait.snapTo(lured.x, lured.y, lured.z, 0.0F, 0.0F);
		// Another unloaded with its chunk, as a far chunk unloads: saved, not lost.
		OwnerPlunderer unloaded = station(ship, 7);
		unloaded.stopRiding();
		var save = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		unloaded.saveWithoutId(save);
		CompoundTag unloadedTag = save.buildResult();
		unloaded.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		all.stream().filter(c -> c != bait && c != unloaded).forEach(c -> c.kill(level));
		h.assertTrue(ship.getPassengers().isEmpty(), "Setup: nobody is aboard");
		h.assertTrue(!PirateCrew.defeated(ship), "A crew member alive 80 blocks off, and one unloaded, keep the ship undefeated");
		var roster = PirateCrew.Roster.get(level);
		var reread = PirateCrew.Roster.CODEC.parse(NbtOps.INSTANCE, PirateCrew.Roster.CODEC.encodeStart(NbtOps.INSTANCE, roster).getOrThrow()).getOrThrow();
		h.assertTrue(reread.knows(shipId) && !reread.defeated(shipId), "Saved and read back, the roster still counts them alive");
		bait.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
		h.assertTrue(!bait.isAlive() && !PirateCrew.defeated(ship), "The bait dies far off and is lost; the unloaded one still counts");
		// The unloaded crew member comes back with its chunk, and is killed.
		OwnerPlunderer back = OwnerPlunderer.TYPE.create(level, EntitySpawnReason.LOAD);
		back.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), unloadedTag));
		h.assertTrue(back.getUUID().equals(unloaded.getUUID()) && shipId.equals(back.shipLink()), "It comes back as itself, still linked");
		level.addFreshEntity(back);
		h.assertTrue(!PirateCrew.defeated(ship), "Back, and alive: still undefeated");
		back.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
		h.assertTrue(PirateCrew.defeated(ship), "Every rostered crew member lost, wherever each died: defeated");
		reread = PirateCrew.Roster.CODEC.parse(NbtOps.INSTANCE, PirateCrew.Roster.CODEC.encodeStart(NbtOps.INSTANCE, roster).getOrThrow()).getOrThrow();
		h.assertTrue(reread.defeated(shipId), "A defeat survives the roster's save");
		bait.discard();
		back.discard();
		ship.discard();
		h.assertTrue(!PirateCrew.Roster.get(level).knows(shipId) && !PirateCrew.defeated(ship), "A destroyed ship is forgotten");
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void onlyDeathsDefeatAShipNeverDiscards(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		// Every crew member discarded, as Peaceful's despawn (or an admin) does: unaccounted for, never defeated.
		StubVessel discarded = ship(h, 0.0F, 4.5);
		PirateCrew.provision(level, discarded);
		level.addFreshEntityWithPassengers(discarded);
		crew(discarded).forEach(Entity::discard);
		h.assertTrue(discarded.getPassengers().isEmpty() && PirateCrew.Roster.get(level).knows(discarded.getUUID()),
				"Setup: the discarded crew are gone, the ship still on the roster");
		h.assertTrue(!PirateCrew.defeated(discarded), "A ship whose crew were all discarded, not killed, is not defeated: no treasure in Peaceful");
		// Nine killed and one discarded: still not defeated.
		StubVessel mixed = ship(h, 0.0F, 12.5);
		PirateCrew.provision(level, mixed);
		level.addFreshEntityWithPassengers(mixed);
		List<OwnerPlunderer> mixedCrew = crew(mixed);
		mixedCrew.getFirst().discard();
		mixedCrew.subList(1, mixedCrew.size()).forEach(c -> c.kill(level));
		h.assertTrue(mixedCrew.size() == 10 && !PirateCrew.defeated(mixed), "Nine killed and one discarded is not defeated");
		// Every crew member killed: defeated.
		StubVessel killed = ship(h, 0.0F, 8.5);
		PirateCrew.provision(level, killed);
		level.addFreshEntityWithPassengers(killed);
		List<OwnerPlunderer> killedCrew = crew(killed);
		killedCrew.forEach(c -> c.kill(level));
		h.assertTrue(killedCrew.size() == 10 && killedCrew.stream().noneMatch(Entity::isAlive) && PirateCrew.defeated(killed),
				"A ship whose crew were all killed is defeated");
		for (StubVessel ship : List.of(discarded, mixed, killed)) {
			cleanup(level, ship);
		}
		mixedCrew.forEach(Entity::discard);
		killedCrew.forEach(Entity::discard);
		h.succeed();
	}

	@GameTest(structure = ARENA, maxTicks = 100)
	public void crewOffTheShipFightOnlyWithinTheTether(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		floor(h, 0, 15, 0, 15);
		StubVessel ship = ship(h, 0.0F, 8.5);
		PirateCrew.provision(level, ship);
		level.addFreshEntityWithPassengers(ship);
		OwnerPlunderer gunner = station(ship, 2);
		OwnerPlunderer deckhand = station(ship, 6);
		deckhand.stopRiding();
		Vec3 onDeck = h.absoluteVec(new Vec3(9.5, 2.0, 13.5));
		deckhand.snapTo(onDeck.x, onDeck.y, onDeck.z, 0.0F, 0.0F);
		ServerPlayer player = boarder(h);
		Vec3 near = h.absoluteVec(new Vec3(13.5, 2.0, 8.5));
		player.snapTo(near.x, near.y, near.z, 90.0F, 0.0F);
		h.assertTrue(deckhand.canAttack(player), "Within the tether a crew member off the ship may fight" + why(level, deckhand, player));
		// 4 blocks beyond the tether, off the stern; the test (never the crew) loads that chunk so the answer is the tether's.
		Vec3 beyond = new Vec3(ship.getX(), ship.getY() + 1.0, ship.deckBounds().minZ - 28.0);
		level.getChunk(BlockPos.containing(beyond));
		player.snapTo(beyond.x, beyond.y, beyond.z, 0.0F, 0.0F);
		h.assertTrue(gunner.canAttack(player), "Setup: the far spot is open to a fight; a gunner aboard may still lay on it" + why(level, gunner, player));
		h.assertTrue(!deckhand.canAttack(player), "A target beyond 24 blocks of the deck is given up by crew off the ship" + why(level, deckhand, player));
		player.snapTo(near.x, near.y, near.z, 90.0F, 0.0F);
		deckhand.snapTo(beyond.x, beyond.y, beyond.z, 0.0F, 0.0F);
		h.assertTrue(!deckhand.canAttack(player), "A crew member drawn beyond the tether fights nobody until it is back" + why(level, deckhand, player));
		deckhand.snapTo(onDeck.x, onDeck.y, onDeck.z, 0.0F, 0.0F);
		player.snapTo(beyond.x, beyond.y, beyond.z, 0.0F, 0.0F);
		deckhand.setTarget(player);
		h.runAfterDelay(40, () -> {
			h.assertTrue(deckhand.getVehicle() == ship && deckhand.shipLink() != null,
					"With its target beyond the tether, the crew member goes back aboard to its station");
			cleanup(level, ship);
			TestPlayers.leave(player);
			h.succeed();
		});
	}

	@GameTest(structure = ARENA, maxTicks = 100)
	public void sinkingShipsCrewAbandonItAndUnlink(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		StubVessel ship = ship(h, 0.0F, 8.5);
		PirateCrew.provision(level, ship);
		level.addFreshEntityWithPassengers(ship);
		var ids = crew(ship).stream().map(Entity::getUUID).collect(Collectors.toSet());
		ship.sunk = true;
		h.runAfterDelay(10, () -> {
			var former = level.getEntitiesOfClass(OwnerPlunderer.class, ship.getBoundingBox().inflate(32.0), c -> ids.contains(c.getUUID()));
			h.assertTrue(former.size() == 10 && former.stream().noneMatch(Entity::isPassenger)
					&& former.stream().allMatch(c -> c.shipLink() == null && c.shipStation() == -1), "A sinking ship's crew leave it and unlink");
			h.assertTrue(!PirateCrew.helmManned(ship), "Nobody holds a sunk ship's helm");
			former.forEach(Entity::discard);
			ship.discard();
			h.succeed();
		});
	}

	@GameTest(structure = ARENA)
	public void linkedCrewNeverTargetVillagersOrTownsfolk(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		StubVessel ship = ship(h, 0.0F, 8.5);
		PirateCrew.provision(level, ship);
		level.addFreshEntityWithPassengers(ship);
		var villager = net.minecraft.world.entity.EntityTypes.VILLAGER.create(level, EntitySpawnReason.TRIGGERED);
		var townsfolk = JugcraftTown.TOWNSFOLK.create(level, EntitySpawnReason.TRIGGERED);
		Vec3 near = h.absoluteVec(new Vec3(12.5, 1.0, 8.5));
		villager.snapTo(near.x, near.y, near.z, 0.0F, 0.0F);
		townsfolk.snapTo(near.x, near.y, near.z + 2.0, 0.0F, 0.0F);
		for (OwnerPlunderer crew : crew(ship)) {
			h.assertTrue(!crew.canAttack(villager) && !crew.canAttack(townsfolk), "Ship crew never fight villagers or townsfolk");
		}
		OwnerPlunderer galleonCrew = OwnerPlunderer.TYPE.create(level, EntitySpawnReason.TRIGGERED);
		h.assertTrue(!galleonCrew.canAttack(villager), "The galleon crew's existing no-villager rule is unchanged");
		cleanup(level, ship);
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void crewWillNotFightAPlayerInsideAClaim(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		floor(h, 12, 14, 7, 9);
		StubVessel ship = ship(h, 0.0F, 6.5);
		PirateCrew.provision(level, ship);
		level.addFreshEntityWithPassengers(ship);
		ServerPlayer player = boarder(h);
		Vec3 at = h.absoluteVec(new Vec3(13.5, 2.0, 8.5));
		player.snapTo(at.x, at.y, at.z, 90.0F, 0.0F);
		OwnerPlunderer helm = crew(ship).stream().filter(c -> c.shipStation() == 0).findFirst().orElseThrow();
		h.assertTrue(helm.canAttack(player), "Outside any claim or settlement the crew may fight a survival player" + why(level, helm, player));
		try (var claim = TestClaims.open().block(player.blockPosition(), UUID.randomUUID())) {
			h.assertTrue(!helm.canAttack(player), "A claim that refuses an unprivileged identity keeps the crew from fighting there" + why(level, helm, player));
		}
		h.assertTrue(helm.canAttack(player), "Lifting the claim lifts the restriction" + why(level, helm, player));
		player.setGameMode(GameType.CREATIVE);
		h.assertTrue(!helm.canAttack(player), "Creative players are never targets" + why(level, helm, player));
		cleanup(level, ship);
		TestPlayers.leave(player);
		h.succeed();
	}

	@GameTest(structure = ARENA, maxTicks = 200)
	public void gunnerFiresOnceFromFiniteSuppliesAndShotsAreDamageOnly(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		floor(h, 12, 14, 9, 13);
		for (int y = 1; y <= 3; y++) {
			for (int z = 9; z <= 13; z++) {
				h.setBlock(new BlockPos(15, y, z), Blocks.STONE);
			}
		}
		StubVessel ship = ship(h, 0.0F, 4.5);
		PirateCrew.provision(level, ship);
		level.addFreshEntityWithPassengers(ship);
		ServerPlayer player = boarder(h);
		player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0); // Struck, not killed, by the shot.
		player.setHealth(200.0F);
		Vec3 at = h.absoluteVec(new Vec3(13.5, 2.0, 11.5));
		player.snapTo(at.x, at.y, at.z, 90.0F, 0.0F);
		OwnerPlunderer gunner = crew(ship).stream().filter(c -> c.shipStation() == 2).findFirst().orElseThrow();
		gunner.setTarget(player);
		h.runAfterDelay(40, () -> {
			h.assertTrue(count(gunner, Items.GUNPOWDER) == 7 && count(gunner, cannonball()) == 7,
					"The port fore gunner fired exactly once (cooldown), spending one powder and one ball: "
					+ count(gunner, Items.GUNPOWDER) + "/" + count(gunner, cannonball()) + ", target " + gunner.getTarget() + why(level, gunner, player));
			for (int y = 1; y <= 3; y++) {
				for (int z = 9; z <= 13; z++) {
					h.assertBlockPresent(Blocks.STONE, new BlockPos(15, y, z));
				}
			}
			h.assertBlockPresent(Blocks.STONE, new BlockPos(13, 0, 11));
			cleanup(level, ship);
			TestPlayers.leave(player);
			h.succeed();
		});
	}

	@GameTest(structure = ARENA)
	public void unlinkedPlunderersKeepTheirGalleonRules(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		OwnerPlunderer plunderer = OwnerPlunderer.TYPE.create(level, EntitySpawnReason.TRIGGERED);
		Vec3 at = h.absoluteVec(new Vec3(8.5, 1.0, 8.5));
		plunderer.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		h.assertTrue(plunderer.shipLink() == null && plunderer.shipStation() == -1 && !plunderer.canJoinRaid(),
				"A galleon plunderer has no ship and never joins raids");
		var villager = net.minecraft.world.entity.EntityTypes.VILLAGER.create(level, EntitySpawnReason.TRIGGERED);
		h.assertTrue(!plunderer.canAttack(villager), "It still never targets villagers");
		var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		plunderer.saveWithoutId(out);
		CompoundTag tag = out.buildResult();
		h.assertTrue(!tag.contains("Ship") && !tag.contains("ShipStation") && !tag.contains("ShipPos"),
				"An unlinked plunderer saves exactly as before, with no ship fields");
		h.succeed();
	}

	/**
	 * An ordinary survival player who has finished loading, to be fought. Vanilla's mock player is creative for good
	 * (its isCreative() always answers true, whatever setGameMode says), and the crew never fight a creative player; a
	 * player whose client has not reported loaded cannot be hurt.
	 */
	private static ServerPlayer boarder(GameTestHelper h) {
		ServerPlayer player = TestPlayers.survivor(h);
		player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
		h.assertTrue(!player.isCreative() && !player.isSpectator() && player.connection.hasClientLoaded(),
				"Fixture: a loaded survival player, not the creative mock");
		return player;
	}

	/**
	 * Every condition the crew member's canAttack weighs against {@code target} (PirateCrew.fair, then the game's own),
	 * for a failed assertion's message: the target's mode, aboard, the tether, and each protection question the pirate
	 * identity is asked at both positions.
	 */
	private static String why(ServerLevel level, OwnerPlunderer crew, LivingEntity target) {
		Entity ship = crew.shipLink() == null ? null : level.getEntity(crew.shipLink());
		boolean aboard = crew.getVehicle() != null && crew.getVehicle().getUUID().equals(crew.shipLink());
		String tether = ship instanceof CrewedVessel vessel ? "crew " + vessel.deckBounds().inflate(24.0).contains(crew.position())
				+ ", target " + vessel.deckBounds().inflate(24.0).contains(target.position()) : "ship not loaded";
		return " [canAttack " + crew.canAttack(target) + ", creative " + (target instanceof Player player && player.isCreative())
				+ ", spectator " + target.isSpectator() + ", alive " + target.isAlive() + ", difficulty "
				+ level.getDifficulty() + ", aboard " + aboard + ", tethered (" + tether + "), crew at " + where(level, crew.blockPosition())
				+ ", target at " + where(level, target.blockPosition()) + "]";
	}

	/** Each protection question PirateCrew.mayFight asks at {@code pos}, as the pirate identity. */
	private static String where(ServerLevel level, BlockPos pos) {
		ServerPlayer actor = Authority.pirateCrewActor(level);
		return pos.toShortString() + " {loaded " + level.hasChunkAt(pos) + ", town " + Town.isProtected(level, pos)
				+ ", mayChange " + Authority.mayChange(level, actor, pos) + ", inBounds " + level.isInWorldBounds(pos)
				+ ", border " + level.getWorldBorder().isWithinBounds(pos) + ", mayInteract " + level.mayInteract(actor, pos)
				+ ", mayUseItemAt " + actor.mayUseItemAt(pos, Direction.UP, ItemStack.EMPTY) + ", mayBuild " + actor.getAbilities().mayBuild
				+ ", townDenies " + TownProtection.denies(actor, level, pos) + ", breakAllowed "
				+ PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, actor, pos, level.getBlockState(pos), level.getBlockEntity(pos))
				+ "}";
	}

	private static StubVessel ship(GameTestHelper h, float yaw, double x) {
		StubVessel ship = TestVessels.STUB.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
		Vec3 at = h.absoluteVec(new Vec3(x, 1.0, 8.5));
		ship.snapTo(at.x, at.y, at.z, yaw, 0.0F);
		return ship;
	}

	private static OwnerPlunderer plunderer(GameTestHelper h, Vec3 relative) {
		OwnerPlunderer plunderer = OwnerPlunderer.TYPE.create(h.getLevel(), EntitySpawnReason.TRIGGERED);
		Vec3 at = h.absoluteVec(relative);
		plunderer.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		plunderer.setPersistenceRequired();
		h.getLevel().addFreshEntity(plunderer);
		return plunderer;
	}

	private static void floor(GameTestHelper h, int x0, int x1, int z0, int z1) {
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				h.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
				h.setBlock(new BlockPos(x, 1, z), Blocks.OAK_PLANKS);
			}
		}
	}

	private static List<OwnerPlunderer> crew(Entity ship) {
		return ship.getPassengers().stream().filter(OwnerPlunderer.class::isInstance).map(OwnerPlunderer.class::cast).toList();
	}

	private static List<OwnerPlunderer> linked(ServerLevel level, Entity ship) {
		return level.getEntitiesOfClass(OwnerPlunderer.class, ship.getBoundingBox().inflate(32.0), c -> ship.getUUID().equals(c.shipLink()));
	}

	private static Item cannonball() {
		return OwnerSupplementaries.ITEMS.get("cannonball");
	}

	private static int count(OwnerPlunderer crew, Item item) {
		int total = 0;
		for (int slot = 0; slot < crew.getInventory().getContainerSize(); slot++) {
			if (crew.getInventory().getItem(slot).is(item)) {
				total += crew.getInventory().getItem(slot).getCount();
			}
		}
		return total;
	}

	private static OwnerPlunderer station(Entity ship, int index) {
		return crew(ship).stream().filter(c -> c.shipStation() == index).findFirst().orElseThrow();
	}

	/** As a chunk unloading does: the ship and its crew leave the level, saved, neither lost nor destroyed. */
	private static void unload(Entity ship) {
		List<Entity> riders = List.copyOf(ship.getPassengers());
		ship.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		riders.forEach(rider -> rider.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK));
	}

	private static void cleanup(ServerLevel level, Entity ship) {
		for (OwnerPlunderer crew : linked(level, ship)) {
			crew.discard();
		}
		for (Entity passenger : List.copyOf(ship.getPassengers())) {
			passenger.discard();
		}
		ship.discard();
	}
}
