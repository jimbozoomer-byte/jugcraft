package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel.Role;
import io.github.jimbozoomer.jugcraft.menagerie.OwnerPlunderer;
import io.github.jimbozoomer.jugcraft.menagerie.OwnerSupplementaries;
import io.github.jimbozoomer.jugcraft.menagerie.PirateCrew;
import io.github.jimbozoomer.jugcraft.test.TestVessels.StubVessel;
import io.github.jimbozoomer.jugcraft.town.JugcraftTown;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The crew of a moving pirate ship (docs/features/pirate-ship-crews.md), on the test-only stub vessel
 * ({@link TestVessels}): provisioning, seats turned once with the ship, save and reload without duplicates, waiting for
 * an unloaded ship without loading it, boarding, defeat, sinking, targets, claims, damage-only finite gunnery, and
 * unlinked (galleon) plunderers unchanged.
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
				h.assertTrue(Math.abs(crew.getX() - expected.x) < 1.0E-3 && Math.abs(crew.getZ() - expected.z) < 1.0E-3
						&& Math.abs(crew.getY() - expected.y) < 1.0, "Station " + crew.shipStation()
						+ " is its seat turned once by the ship's 90° yaw: " + crew.position() + " vs " + expected);
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
		cleanup(level, ship);
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
	public void offShipCrewWaitForAnUnloadedShipAndGiveUpAGoneOne(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		BlockPos far = h.absolutePos(new BlockPos(8, 1, 8)).offset(4096, 0, 4096);
		h.assertTrue(!level.hasChunkAt(far), "Setup: the far ship position is in a chunk that is not loaded");
		floor(h, 2, 6, 2, 13);
		OwnerPlunderer waiting = plunderer(h, new Vec3(3.5, 1.0, 3.5));
		waiting.link(UUID.randomUUID(), 6);
		waiting.sawShip(far);
		OwnerPlunderer stranded = plunderer(h, new Vec3(5.5, 1.0, 12.5));
		stranded.link(UUID.randomUUID(), 7);
		stranded.sawShip(h.absolutePos(new BlockPos(5, 1, 8)));
		h.runAfterDelay(200, () -> {
			h.assertTrue(waiting.shipLink() != null && waiting.shipStation() == 6, "A crew member whose ship is out of loaded range waits for it");
			h.assertTrue(!level.hasChunkAt(far), "Waiting never loads the ship's chunk");
			h.assertTrue(stranded.shipLink() == null && stranded.shipStation() == -1,
					"A ship missing from its own loaded place is given up: the crew member fights on unlinked");
			waiting.discard();
			stranded.discard();
			h.succeed();
		});
	}

	@GameTest(structure = ARENA, maxTicks = 200)
	public void boardingHeavesToAndSendsDeckCrewToFightUntilDefeated(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		floor(h, 6, 10, 1, 15);
		StubVessel ship = ship(h, 0.0F, 8.5);
		PirateCrew.provision(level, ship);
		level.addFreshEntityWithPassengers(ship);
		var player = h.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		Vec3 onDeck = h.absoluteVec(new Vec3(8.5, 2.0, 12.5));
		player.snapTo(onDeck.x, onDeck.y, onDeck.z, 180.0F, 0.0F);
		h.assertTrue(ship.deckBounds().contains(player.position()), "Setup: the player stands on the ship's deck");
		crew(ship).forEach(crew -> crew.setTarget(player));
		h.runAfterDelay(10, () -> {
			h.assertTrue(ship.heaveTo(), "The helm heaves to for a boarder on the deck");
			var all = linked(level, ship);
			var deck = all.stream().filter(c -> PirateCrew.STATIONS.get(c.shipStation()).role() == Role.DECK).toList();
			h.assertTrue(deck.size() == 4 && deck.stream().noneMatch(Entity::isPassenger), "All four deck crew go over to fight, still linked");
			h.assertTrue(all.stream().filter(c -> !deck.contains(c)).allMatch(Entity::isPassenger), "Helm, lookout and gunners keep their stations");
			h.assertTrue(!PirateCrew.defeated(ship), "A ship with crew alive is not defeated");
			all.forEach(Entity::discard);
			loadAround(level, ship);
			h.assertTrue(PirateCrew.defeated(ship), "With every crew member gone the ship is defeated: its loot trigger");
			cleanup(level, ship);
			player.discard();
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
		var villager = EntityType.VILLAGER.create(level, EntitySpawnReason.TRIGGERED);
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
		var player = h.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		Vec3 at = h.absoluteVec(new Vec3(13.5, 1.0, 8.5));
		player.snapTo(at.x, at.y, at.z, 90.0F, 0.0F);
		OwnerPlunderer helm = crew(ship).stream().filter(c -> c.shipStation() == 0).findFirst().orElseThrow();
		h.assertTrue(helm.canAttack(player), "Outside any claim or settlement the crew may fight a survival player");
		try (var claim = TestClaims.open().block(player.blockPosition(), UUID.randomUUID())) {
			h.assertTrue(!helm.canAttack(player), "A claim that refuses an unprivileged identity keeps the crew from fighting there");
		}
		h.assertTrue(helm.canAttack(player), "Lifting the claim lifts the restriction");
		player.setGameMode(GameType.CREATIVE);
		h.assertTrue(!helm.canAttack(player), "Creative players are never targets");
		cleanup(level, ship);
		player.discard();
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
		var player = h.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		Vec3 at = h.absoluteVec(new Vec3(13.5, 1.0, 11.5));
		player.snapTo(at.x, at.y, at.z, 90.0F, 0.0F);
		OwnerPlunderer gunner = crew(ship).stream().filter(c -> c.shipStation() == 2).findFirst().orElseThrow();
		gunner.setTarget(player);
		h.runAfterDelay(40, () -> {
			h.assertTrue(count(gunner, Items.GUNPOWDER) == 7 && count(gunner, cannonball()) == 7,
					"The port fore gunner fired exactly once (cooldown), spending one powder and one ball: "
					+ count(gunner, Items.GUNPOWDER) + "/" + count(gunner, cannonball()));
			for (int y = 1; y <= 3; y++) {
				for (int z = 9; z <= 13; z++) {
					h.assertBlockPresent(Blocks.STONE, new BlockPos(15, y, z));
				}
			}
			h.assertBlockPresent(Blocks.STONE, new BlockPos(13, 0, 11));
			cleanup(level, ship);
			player.discard();
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
		var villager = EntityType.VILLAGER.create(level, EntitySpawnReason.TRIGGERED);
		h.assertTrue(!plunderer.canAttack(villager), "It still never targets villagers");
		var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		plunderer.saveWithoutId(out);
		CompoundTag tag = out.buildResult();
		h.assertTrue(!tag.contains("Ship") && !tag.contains("ShipStation") && !tag.contains("ShipPos"),
				"An unlinked plunderer saves exactly as before, with no ship fields");
		h.succeed();
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

	/** The test, not the crew, loads every chunk in defeated()'s reach, so its answer is known rather than "not yet". */
	private static void loadAround(ServerLevel level, Entity ship) {
		AABB reach = ship.getBoundingBox().inflate(64.0);
		for (int x = Mth.floor(reach.minX) >> 4; x <= Mth.floor(reach.maxX) >> 4; x++) {
			for (int z = Mth.floor(reach.minZ) >> 4; z <= Mth.floor(reach.maxZ) >> 4; z++) {
				level.getChunk(x, z);
			}
		}
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
