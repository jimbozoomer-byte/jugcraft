package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.menagerie.OwnerCannonball;
import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel.Role;
import io.github.jimbozoomer.jugcraft.menagerie.OwnerPlunderer;
import io.github.jimbozoomer.jugcraft.menagerie.PirateCourse;
import io.github.jimbozoomer.jugcraft.menagerie.PirateCrew;
import io.github.jimbozoomer.jugcraft.menagerie.PirateDecks;
import io.github.jimbozoomer.jugcraft.menagerie.PirateFairness;
import io.github.jimbozoomer.jugcraft.menagerie.PirateMotion;
import io.github.jimbozoomer.jugcraft.menagerie.PirateSeas;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShip;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShipDeck;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShipTesting;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShips;
import io.github.jimbozoomer.jugcraft.menagerie.PirateWaters;
import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.town.TownData;
import java.util.Locale;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The moving pirate ship (docs/features/pirate-ships.md): where it may float, how it patrols and stops, the deck it is
 * walked on, its hold rolled once, its saved state and the spawn ledger. The crew themselves are PirateCrewGameTests';
 * here the ship's questions to its crew ("is the helm manned", "are they beaten") are answered by the test.
 *
 * <p>Every test floats the ship in a pool built in the empty drone_tower structure: stone floor at y 0, water y 1 to 4,
 * so the waterline is in y 4 and the ship needs {@value PirateWaters#DEPTH} more water blocks under it (y 2 and 3).
 */
public class PirateShipGameTests {
	private static final int POOL_MIN = 2, POOL_MAX = 41, FLOOR = 0, SURFACE = 4;
	private static final Vec3 CENTRE = new Vec3(22.0, SURFACE + PirateWaters.WATERLINE, 22.0);

	private static void pool(GameTestHelper helper) {
		for (int x = POOL_MIN; x <= POOL_MAX; x++) {
			for (int z = POOL_MIN; z <= POOL_MAX; z++) {
				helper.setBlock(new BlockPos(x, FLOOR, z), Blocks.STONE);
				for (int y = FLOOR + 1; y <= SURFACE; y++) {
					helper.setBlock(new BlockPos(x, y, z), Blocks.WATER);
				}
			}
		}
	}

	/** A ship at the pool's centre (absolute), facing the +z bow, anchored there, not yet added. */
	private static PirateShip ship(GameTestHelper helper, boolean natural) {
		ServerLevel level = helper.getLevel();
		Vec3 at = helper.absoluteVec(CENTRE);
		if (Town.isProtected(level, BlockPos.containing(at))) {
			helper.fail("The test pool lies inside the town's protection; move the test structure");
		}
		PirateShip ship = PirateShips.SHIP.create(level, EntitySpawnReason.EVENT);
		helper.assertTrue(ship != null, "The pirate ship type should create a ship");
		ship.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		ship.launch(at, natural);
		return ship;
	}

	private static PirateShip copy(GameTestHelper helper, PirateShip ship) {
		ServerLevel level = helper.getLevel();
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		ship.saveWithoutId(out);
		PirateShip copy = PirateShips.SHIP.create(level, EntitySpawnReason.LOAD);
		copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult()));
		return copy;
	}

	/**
	 * PirateWaters.clear: the pool's centre is clear; a block in the mast envelope, a shallow spot under the keel and an
	 * unloaded place far away are not; and asking about the unloaded place loads nothing.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 40, skyAccess = true)
	public void watersMustBeDeepOpenAndLoaded(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		Vec3 at = helper.absoluteVec(CENTRE);
		helper.assertTrue(PirateWaters.clear(level, at, 0.0F), "Open, deep, loaded water should be clear at " + at);
		helper.assertTrue(PirateWaters.clear(level, at, 45.0F), "Turned 45 degrees, its larger footprint still fits the pool");
		BlockPos mast = new BlockPos(22, SURFACE + 9, 27);
		helper.setBlock(mast, Blocks.OAK_PLANKS);
		helper.assertTrue(!PirateWaters.clear(level, at, 0.0F), "A block in the mast envelope should block it");
		helper.setBlock(mast, Blocks.AIR);
		BlockPos keel = new BlockPos(24, SURFACE - 2, 16);
		helper.setBlock(keel, Blocks.STONE);
		helper.assertTrue(!PirateWaters.clear(level, at, 0.0F), "Stone two blocks under the waterline should ground the keel");
		helper.setBlock(keel, Blocks.WATER);
		helper.assertTrue(PirateWaters.clear(level, at, 0.0F), "With the water back the place is clear again");
		Vec3 far = at.add(4_000_000.0, 0.0, 4_000_000.0);
		BlockPos farPos = BlockPos.containing(far);
		helper.assertTrue(!level.hasChunkAt(farPos), "The far place should start unloaded");
		helper.assertTrue(!PirateWaters.clear(level, far, 0.0F), "Unloaded water is never clear");
		helper.assertTrue(!level.hasChunkAt(farPos), "Asking about unloaded water must not load its chunk");
		AABB footprint = PirateWaters.footprint(Vec3.ZERO, 90.0F, 0.0, 1.0);
		helper.assertTrue(Math.abs(footprint.getXsize() - 16.0) < 1.0E-6 && Math.abs(footprint.getZsize() - 6.0) < 1.0E-6,
				"Turned 90 degrees, the footprint is 16 across x and 6 along z, not " + footprint);
		helper.succeed();
	}

	/**
	 * The deck: four squares under the hull whose tops are exactly the deck (the waterline plus 1.0), and a pig dropped on
	 * the deck lands on it, not in the sea; the squares follow the ship when it turns.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 100, skyAccess = true)
	public void theDeckIsSolidAtDeckHeight(GameTestHelper helper) {
		pool(helper);
		PirateShip ship = ship(helper, false);
		helper.getLevel().addFreshEntity(ship);
		Mob[] pig = {null};
		helper.runAfterDelay(2, () -> {
			List<PirateShipDeck> decks = ship.decks();
			helper.assertTrue(decks.size() == PirateShipDeck.KEEL.length, "The ship should lay " + PirateShipDeck.KEEL.length
					+ " deck squares, not " + decks.size());
			for (PirateShipDeck deck : decks) {
				helper.assertTrue(Math.abs(deck.getBoundingBox().maxY - (ship.getY() + 1.0)) < 1.0E-6,
						"A deck square's top should be the deck, " + (ship.getY() + 1.0) + ", not " + deck.getBoundingBox().maxY);
				helper.assertTrue(deck.ship() == ship, "Each square should know its ship");
			}
			pig[0] = helper.spawnWithNoFreeWill(EntityTypes.PIG, helper.relativeVec(ship.position().add(0.0, 3.0, 4.0)));
		});
		helper.runAfterDelay(60, () -> {
			helper.assertTrue(pig[0].onGround() && Math.abs(pig[0].getY() - (ship.getY() + 1.0)) < 0.01,
					"The pig should stand on the deck at " + (ship.getY() + 1.0) + ", not at " + pig[0].getY());
			ship.setYRot(90.0F);
		});
		helper.runAfterDelay(63, () -> {
			for (int i = 0; i < PirateShipDeck.KEEL.length; i++) {
				Vec3 expected = ship.position().add(new Vec3(0.0, 0.0, PirateShipDeck.KEEL[i]).yRot(-90.0F * net.minecraft.util.Mth.DEG_TO_RAD));
				helper.assertTrue(ship.decks().get(i).position().distanceTo(expected) < 1.0E-6,
						"Deck square " + i + " should follow the turned hull to " + expected + ", not " + ship.decks().get(i).position());
			}
			pig[0].discard();
			ship.discard();
			helper.succeed();
		});
	}

	/**
	 * Sailing: with the helm manned it makes way toward its waypoint; it stops dead without a helmsman, when hove to, and
	 * while a player stands on its deck; it never strays past its patrol radius.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 200, skyAccess = true)
	public void itSailsOnlyWhenHelmedAndFree(GameTestHelper helper) {
		pool(helper);
		PirateShip ship = ship(helper, false);
		PirateShipTesting.helm(ship, true);
		PirateShipTesting.waypoint(ship, ship.position().add(0.0, 0.0, 6.0));
		helper.getLevel().addFreshEntity(ship);
		Vec3 start = ship.position();
		Vec3[] mark = {null};
		helper.runAfterDelay(60, () -> {
			helper.assertTrue(ship.getZ() - start.z > 1.0, "With the helm manned it should make way toward the bow, but moved "
					+ (ship.getZ() - start.z));
			helper.assertTrue(ship.position().subtract(ship.anchor()).horizontalDistance() <= PirateShip.PATROL_RADIUS,
					"It should keep within its patrol radius");
			// Under way again toward a farther waypoint, still inside the pool.
			PirateShipTesting.waypoint(ship, start.add(0.0, 0.0, 9.0));
		});
		helper.runAfterDelay(72, () -> {
			helper.assertTrue(ship.speed() > 0.0, "It should be under way again");
			PirateShipTesting.helm(ship, false);
		});
		helper.runAfterDelay(73, () -> mark[0] = ship.position());
		helper.runAfterDelay(88, () -> {
			helper.assertTrue(ship.position().equals(mark[0]) && ship.speed() == 0.0, "Without a helmsman it should stop dead");
			PirateShipTesting.helm(ship, true);
			ship.setHeaveTo(true);
		});
		helper.runAfterDelay(89, () -> mark[0] = ship.position());
		helper.runAfterDelay(104, () -> {
			helper.assertTrue(ship.position().equals(mark[0]) && !ship.mayMove(), "Hove to, it should lie still");
			ship.setHeaveTo(false);
			helper.assertTrue(ship.mayMove(), "Let go, with the helm manned, it may make way");
			ServerPlayer visitor = helper.makeMockServerPlayerInLevel();
			Vec3 deck = ship.position().add(0.0, 1.0, 0.0);
			visitor.snapTo(deck.x, deck.y, deck.z, 0.0F, 0.0F);
			helper.assertTrue(!ship.mayMove(), "With a player on its deck it should not make way");
			visitor.snapTo(deck.x + 40.0, deck.y + 30.0, deck.z, 0.0F, 0.0F);
			ship.discard();
			helper.succeed();
		});
	}

	/**
	 * The hold: empty while the crew hold out; rolled once from the galleon chest's table when they are beaten (exactly that
	 * table's roll for the ship's own seed); emptied by
	 * a player it stays empty, through more ticks and a save and reload; a ship nobody crewed never rolls at all.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 200, skyAccess = true)
	public void theHoldIsRolledOnceAfterTheCrewAreBeaten(GameTestHelper helper) {
		pool(helper);
		PirateShip ship = ship(helper, false);
		ship.setCrewed(true);
		PirateShipTesting.beaten(ship, false);
		helper.getLevel().addFreshEntity(ship);
		PirateShip uncrewed = ship(helper, false); // never added: only asked whether it may roll
		PirateShipTesting.beaten(uncrewed, true);
		helper.runAfterDelay(41, () -> {
			helper.assertTrue(!ship.lootRolled() && ship.hold().isEmpty(), "The hold stays empty while the crew hold out");
			PirateShipTesting.beaten(ship, true);
		});
		helper.runAfterDelay(65, () -> {
			helper.assertTrue(ship.lootRolled() && ship.beaten() && ship.heaveTo(), "Beaten, the ship should roll its hold and lie still");
			var expected = PirateShipTesting.expectedHold(helper.getLevel(), ship, ship.position());
			for (int slot = 0; slot < PirateShip.HOLD_SLOTS; slot++) {
				helper.assertTrue(ItemStack.matches(ship.hold().getItem(slot), expected.getItem(slot)),
						"Hold slot " + slot + " should be the galleon chest table's roll for this ship, " + expected.getItem(slot)
								+ ", not " + ship.hold().getItem(slot));
			}
			ship.hold().clearContent();
		});
		helper.runAfterDelay(110, () -> {
			helper.assertTrue(ship.hold().isEmpty(), "An emptied hold is never refilled");
			PirateShip reloaded = copy(helper, ship);
			helper.assertTrue(reloaded.lootRolled() && reloaded.beaten() && reloaded.hold().isEmpty(),
					"After a save and reload it is still rolled and still empty");
			PirateShipTesting.rollHold(helper.getLevel(), reloaded);
			helper.assertTrue(reloaded.hold().isEmpty(), "Asking it to roll again does nothing");
			helper.assertTrue(!PirateShipTesting.mayRoll(uncrewed), "A ship nobody crewed never rolls its hold");
			ship.discard();
			helper.succeed();
		});
	}

	/** Saving keeps the anchor, waypoint, flags, damage and hold, and nothing about it changes on reload. */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 20, skyAccess = true)
	public void aShipSavesItsPatrolHoldAndState(GameTestHelper helper) {
		pool(helper);
		PirateShip ship = ship(helper, true);
		ship.setCrewed(true);
		ship.setHeaveTo(true);
		PirateShipTesting.waypoint(ship, ship.position().add(10.0, 0.0, -12.0));
		ship.hold().setItem(4, new ItemStack(Items.GOLD_INGOT, 7));
		PirateShip copy = copy(helper, ship);
		helper.assertTrue(copy.anchor().equals(ship.anchor()), "The anchor should survive a save, not " + copy.anchor());
		helper.assertTrue(copy.waypoint() != null && copy.waypoint().distanceTo(ship.waypoint()) < 1.0E-9, "The waypoint too");
		helper.assertTrue(copy.natural() && copy.crewed() && copy.heaveTo() && !copy.lootRolled() && !copy.sunk(),
				"Natural, crewed, hove to, not rolled, not sunk");
		helper.assertTrue(ItemStack.matches(copy.hold().getItem(4), ship.hold().getItem(4)), "The hold's gold should survive");
		helper.assertTrue(copy.getUUID().equals(ship.getUUID()), "Its identity (the entity UUID the crew roster keys on) survives");
		helper.succeed();
	}

	/**
	 * Only players harm it; when their blows sink it, the hold spills and the ship settles and goes. A blow that is not a
	 * player's does nothing.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 300, skyAccess = true)
	public void playersSinkItAndTheHoldSpills(GameTestHelper helper) {
		pool(helper);
		PirateShip ship = ship(helper, false);
		ship.hold().setItem(0, new ItemStack(Items.EMERALD, 3));
		helper.getLevel().addFreshEntity(ship);
		ServerLevel level = helper.getLevel();
		helper.assertTrue(!ship.hurtServer(level, level.damageSources().generic(), 1000.0F) && ship.damage() == 0.0F,
				"A blow that is not a player's should not harm the hull");
		// A real survival player (vanilla's mock stays creative, which would remove the ship outright instead).
		ServerPlayer player = TestPlayers.survivor(helper);
		helper.assertTrue(ship.hurtServer(level, level.damageSources().playerAttack(player), PirateShip.HEALTH), "A player's blow lands");
		helper.assertTrue(ship.sunk() && ship.hold().isEmpty(), "Enough of them sink it and empty the hold");
		helper.assertTrue(!level.getEntitiesOfClass(ItemEntity.class, ship.getBoundingBox().inflate(4.0),
				item -> item.getItem().is(Items.EMERALD)).isEmpty(), "The hold's emeralds should spill where it lies");
		helper.succeedWhen(() -> helper.assertTrue(ship.isRemoved(), "After settling it is gone"));
	}

	/**
	 * The spawn ledger: one ship per region, a cap on ships afloat (loaded or not, however long unloaded), a rest after a
	 * ship is beaten or gone, tombstones for removed ships, and all of it saved. Pure ledger logic: no ship is raised.
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 20)
	public void theSeasKeepOneShipPerRegionUnderTheCap(GameTestHelper helper) {
		PirateSeas seas = new PirateSeas();
		long now = 1_000L;
		List<UUID> ids = new ArrayList<>();
		for (int i = 0; i < PirateSeas.MAX_SHIPS; i++) {
			UUID id = UUID.randomUUID();
			ids.add(id);
			long region = PirateSeas.region(i * PirateSeas.REGION_SIZE, 0);
			helper.assertTrue(seas.regionFree(region, now), "Region " + i + " should start free");
			seas.raised(id, region, new BlockPos(i * PirateSeas.REGION_SIZE, 63, 0), now);
			helper.assertTrue(!seas.regionFree(region, now), "A region with a ship afloat is not free");
		}
		helper.assertTrue(seas.active(now) == PirateSeas.MAX_SHIPS, "Every ship counts toward the cap, loaded or not");
		long first = PirateSeas.region(0, 0);
		seas.defeated(ids.get(0), now);
		helper.assertTrue(seas.active(now) == PirateSeas.MAX_SHIPS - 1, "A beaten ship leaves the cap");
		helper.assertTrue(!seas.regionFree(first, now + PirateSeas.COOLDOWN - 1), "Its region rests");
		seas.gone(ids.get(0), now);
		helper.assertTrue(!seas.regionFree(first, now + PirateSeas.COOLDOWN - 1) && seas.regionFree(first, now + PirateSeas.COOLDOWN),
				"Gone, its region rests out the cooldown and is free after it");
		Tag saved = PirateSeas.CODEC.encodeStart(NbtOps.INSTANCE, seas).getOrThrow();
		PirateSeas reloaded = PirateSeas.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
		helper.assertTrue(reloaded.active(now) == PirateSeas.MAX_SHIPS - 1 && !reloaded.regionFree(first, now + 1),
				"The ledger saves its ships and rests");
		// Unloaded far longer than a week (no report at all): still counted, still holding its region.
		long later = now + 10L * 168_000L;
		reloaded.expire(later);
		long second = PirateSeas.region(PirateSeas.REGION_SIZE, 0);
		helper.assertTrue(reloaded.active(later) == PirateSeas.MAX_SHIPS - 1 && !reloaded.regionFree(second, later),
				"A ship unloaded for weeks still holds its place under the cap and its region");
		helper.assertTrue(reloaded.seen(ids.get(1), second, new BlockPos(PirateSeas.REGION_SIZE, 63, 0), false, later)
				&& reloaded.active(later) == PirateSeas.MAX_SHIPS - 1, "When it loads again it is the same record, not a second ship");
		reloaded.gone(ids.get(1), later);
		helper.assertTrue(reloaded.tombstoned(ids.get(1)) && !reloaded.seen(ids.get(1), second, BlockPos.ZERO, false, later + 1)
				&& reloaded.active(later + 1) == PirateSeas.MAX_SHIPS - 2, "A removed ship is tombstoned: a stale copy is refused, not recorded again");
		PirateSeas again = PirateSeas.CODEC.parse(NbtOps.INSTANCE, PirateSeas.CODEC.encodeStart(NbtOps.INSTANCE, reloaded).getOrThrow()).getOrThrow();
		helper.assertTrue(again.tombstoned(ids.get(1)), "Tombstones are saved");
		helper.succeed();
	}

	/**
	 * A stale copy of a removed natural ship (its id tombstoned: an older chunk save, a restored backup) goes on its first
	 * tick, with its crew, rolling no loot and spilling nothing.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 40, skyAccess = true)
	public void aRemovedShipsStaleCopyNeverSailsAgain(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper, true);
		ship.setCrewed(true);
		PirateShipTesting.beaten(ship, true);
		PirateSeas.get(level).gone(ship.getUUID(), level.getGameTime());
		level.addFreshEntity(ship);
		helper.runAfterDelay(5, () -> {
			helper.assertTrue(ship.isRemoved(), "The tombstoned copy should be gone");
			helper.assertTrue(!ship.lootRolled() && ship.hold().isEmpty(), "It rolls no loot");
			helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, ship.getBoundingBox().inflate(8.0)).isEmpty(), "It spills nothing");
			helper.assertTrue(PirateSeas.get(level).ship(ship.getUUID()) == null, "And it is not recorded again");
			helper.succeed();
		});
	}

	/** Runs an operator command as a mock operator standing at {@code at} (absolute); /protect takes its corners from there. */
	private static void protect(GameTestHelper helper, ServerPlayer op, String command, BlockPos at) {
		op.snapTo(at.getX(), at.getY(), at.getZ(), 0.0F, 0.0F);
		var server = helper.getLevel().getServer();
		try {
			server.getCommands().getDispatcher().execute(command, server.createCommandSourceStack().withEntity(op).withSuppressedOutput());
		} catch (Exception e) {
			helper.fail("/" + command + " failed: " + e.getMessage());
		}
	}

	/**
	 * Protection is tested exactly, every block of the volume: a one-block operator region in the water between where a
	 * coarser sampling would look, and one at bridge height over the water across the masts' way, each make the place
	 * unclear; a region wholly above the masts' reach does not.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 40, skyAccess = true)
	public void operatorRegionsAnywhereInTheHullsVolumeBlockIt(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		Vec3 at = helper.absoluteVec(CENTRE);
		ServerPlayer op = helper.makeMockServerPlayerInLevel();
		String name = "pirate_" + op.getUUID().toString().replace("-", "");
		helper.assertTrue(PirateWaters.clear(level, at, 0.0F), "The pool's centre starts clear");
		BlockPos one = helper.absolutePos(new BlockPos(21, SURFACE, 20));
		protect(helper, op, "protect pos1", one);
		protect(helper, op, "protect pos2", one);
		protect(helper, op, "protect create " + name, one);
		helper.assertTrue(!PirateWaters.clear(level, at, 0.0F), "A one-block region in the hull's water should block it");
		protect(helper, op, "protect remove " + name, one);
		helper.assertTrue(PirateWaters.clear(level, at, 0.0F), "Removed, the place is clear again");
		protect(helper, op, "protect pos1", helper.absolutePos(new BlockPos(POOL_MIN, SURFACE + 5, 24)));
		protect(helper, op, "protect pos2", helper.absolutePos(new BlockPos(POOL_MAX, SURFACE + 6, 25)));
		protect(helper, op, "protect create " + name, one);
		helper.assertTrue(!PirateWaters.clear(level, at, 0.0F), "A region at bridge height across the masts' way should block it");
		protect(helper, op, "protect remove " + name, one);
		protect(helper, op, "protect pos1", helper.absolutePos(new BlockPos(POOL_MIN, SURFACE + PirateWaters.MAST + 1, 24)));
		protect(helper, op, "protect pos2", helper.absolutePos(new BlockPos(POOL_MAX, SURFACE + PirateWaters.MAST + 2, 25)));
		protect(helper, op, "protect create " + name, one);
		helper.assertTrue(PirateWaters.clear(level, at, 0.0F), "A region wholly above the masts' reach does not");
		protect(helper, op, "protect remove " + name, one);
		helper.succeed();
	}

	/**
	 * The swept hull and rig: with a block (and then a one-block operator region) just off the port quarter, outside the
	 * ship and outside the water 3 blocks ahead, the ship may hold its place and sail straight on, but may not turn, since
	 * its stern would swing into it. A helmed ship asked to turn there never lets its rig reach that cell.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 120, skyAccess = true)
	public void theSternMayNotSwingIntoAnObstacle(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		Vec3 at = helper.absoluteVec(CENTRE);
		// Just outside the rig at rest (its port side reaches x 26.0 here), off the port quarter.
		BlockPos quarter = new BlockPos(26, SURFACE + 1, 13);
		helper.setBlock(quarter, Blocks.OAK_PLANKS);
		helper.assertTrue(PirateWaters.clear(level, at, 0.0F), "The hull itself is clear where it lies");
		helper.assertTrue(PirateWaters.clear(level, at.add(0.0, 0.0, 3.0), 0.0F), "The water 3 blocks ahead is clear too");
		helper.assertTrue(PirateWaters.sweep(level, at, 0.0F, at.add(0.0, 0.0, PirateShip.SPEED), 0.0F), "Straight on is allowed");
		helper.assertTrue(!PirateWaters.sweep(level, at, 0.0F, at, PirateShip.TURN), "Turning would swing the stern into the block");
		helper.setBlock(quarter, Blocks.AIR);
		helper.assertTrue(PirateWaters.sweep(level, at, 0.0F, at, PirateShip.TURN), "With the block gone it may turn");
		ServerPlayer op = helper.makeMockServerPlayerInLevel();
		String name = "pirate_" + op.getUUID().toString().replace("-", "");
		BlockPos cell = helper.absolutePos(quarter);
		protect(helper, op, "protect pos1", cell);
		protect(helper, op, "protect pos2", cell);
		protect(helper, op, "protect create " + name, cell);
		helper.assertTrue(!PirateWaters.sweep(level, at, 0.0F, at, PirateShip.TURN), "Nor may it swing into protected air");
		protect(helper, op, "protect remove " + name, cell);
		helper.setBlock(quarter, Blocks.OAK_PLANKS);
		PirateShip ship = ship(helper, false);
		PirateShipTesting.helm(ship, true);
		PirateShipTesting.waypoint(ship, at.add(-10.0, 0.0, 4.0));
		level.addFreshEntity(ship);
		AABB obstacle = new AABB(helper.absolutePos(quarter));
		helper.onEachTick(() -> {
			AABB rig = PirateWaters.rigFootprint(ship.position(), ship.getYRot(), ship.getY(), ship.getY() + PirateWaters.MAST);
			helper.assertTrue(!rig.intersects(obstacle), "The rig must never reach the obstacle, but at yaw " + ship.getYRot()
					+ " it covers " + rig);
		});
		helper.runAfterDelay(100, () -> {
			ship.discard();
			helper.succeed();
		});
	}

	/**
	 * Two envelopes: under the water only the 6 x 16 hull needs water (a block beside it, below the waterline, is no
	 * matter), but above the water the rig is wider and longer (rails, bowsprit): a block beside the hull at rail height,
	 * or where the bowsprit reaches, blocks the place.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 40, skyAccess = true)
	public void theRigAboveTheWaterNeedsMoreRoomThanTheHull(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		Vec3 at = helper.absoluteVec(CENTRE);
		BlockPos besideBelow = new BlockPos(25, SURFACE - 1, 22);
		helper.setBlock(besideBelow, Blocks.STONE);
		helper.assertTrue(PirateWaters.clear(level, at, 0.0F), "Stone beside the hull, under the water, should not matter");
		helper.setBlock(besideBelow, Blocks.WATER);
		BlockPos besideAbove = new BlockPos(25, SURFACE + 1, 22);
		helper.setBlock(besideAbove, Blocks.OAK_PLANKS);
		helper.assertTrue(!PirateWaters.clear(level, at, 0.0F), "A block beside the hull at rail height is in the rig's way");
		helper.setBlock(besideAbove, Blocks.AIR);
		BlockPos bowsprit = new BlockPos(22, SURFACE + 3, 33);
		helper.setBlock(bowsprit, Blocks.OAK_PLANKS);
		helper.assertTrue(!PirateWaters.clear(level, at, 0.0F), "A block where the bowsprit reaches is in the rig's way");
		helper.setBlock(bowsprit, Blocks.AIR);
		helper.assertTrue(PirateWaters.clear(level, at, 0.0F), "Cleared, the place is clear again");
		AABB rig = PirateWaters.rigFootprint(Vec3.ZERO, 0.0F, 0.0, 1.0);
		helper.assertTrue(rig.minX == -PirateWaters.RIG_HALF_BEAM && rig.maxX == PirateWaters.RIG_HALF_BEAM
				&& rig.minZ == PirateWaters.RIG_STERN && rig.maxZ == PirateWaters.RIG_BOW, "The rig box at yaw 0 is exactly its envelope: " + rig);
		helper.succeed();
	}

	/**
	 * Cannon fire and the real hull: a crew member's own cannonball, fired straight across its own deck at hull height from
	 * the port muzzle, passes over the deck squares and out the starboard side, harming nothing; a player's ball on the
	 * same line is stopped by the hull and dents it; so is a ball from another ship's crew (a sea fight).
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 60, skyAccess = true)
	public void cannonFireSparesItsOwnHullAndDentsAnEnemys(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper, false);
		level.addFreshEntity(ship);
		OwnerPlunderer[] crew = {null, null};
		OwnerCannonball[] balls = {null, null, null};
		double[] farthest = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
		float[] dent = {0.0F};
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(ship.decks().size() == PirateShipDeck.KEEL.length, "The real deck should be laid");
			crew[0] = helper.spawnWithNoFreeWill(OwnerPlunderer.TYPE, helper.relativeVec(ship.position().add(10.0, 6.0, 0.0)));
			PirateShipTesting.link(crew[0], ship, 2);
			// Across the beam at hull height, inside the deck squares' boxes, from the port muzzle (+x) toward starboard.
			balls[0] = PirateShipTesting.fire(level, crew[0], ship.position().add(3.4, 0.6, 2.5), new Vec3(-1.8, 0.0, 0.0));
		});
		helper.onEachTick(() -> {
			for (int i = 0; i < balls.length; i++) {
				if (balls[i] != null && !balls[i].isRemoved()) {
					farthest[i] = Math.min(farthest[i], balls[i].getX());
				}
			}
		});
		helper.runAfterDelay(12, () -> {
			helper.assertTrue(farthest[0] < ship.getX() - PirateWaters.HALF_BEAM, "The crew's own ball should cross the deck and leave"
					+ " the starboard side, but got no farther than x " + farthest[0] + " (the starboard side is " + (ship.getX() - PirateWaters.HALF_BEAM) + ")");
			helper.assertTrue(ship.damage() == 0.0F && !ship.sunk(), "Its own ball harms nothing");
			ServerPlayer player = TestPlayers.survivor(helper);
			balls[1] = PirateShipTesting.fire(level, player, ship.position().add(3.4, 0.6, -2.0), new Vec3(-1.8, 0.0, 0.0));
		});
		helper.runAfterDelay(18, () -> {
			helper.assertTrue(balls[1].isRemoved() && farthest[1] > ship.getX(), "A player's ball should be stopped by the hull, but reached x "
					+ farthest[1]);
			helper.assertTrue(ship.damage() > 0.0F, "A player's cannonball should dent the hull");
			dent[0] = ship.damage();
			crew[1] = helper.spawnWithNoFreeWill(OwnerPlunderer.TYPE, helper.relativeVec(ship.position().add(-10.0, 6.0, 0.0)));
			PirateShipTesting.link(crew[1], java.util.UUID.randomUUID(), 2);
			balls[2] = PirateShipTesting.fire(level, crew[1], ship.position().add(3.4, 0.6, 5.0), new Vec3(-1.8, 0.0, 0.0));
		});
		helper.runAfterDelay(24, () -> {
			helper.assertTrue(balls[2].isRemoved() && farthest[2] > ship.getX(), "Another ship's crew's ball should be stopped by the hull");
			helper.assertTrue(ship.damage() > dent[0], "Another ship's crew's cannonball should dent the hull (a sea fight)");
			crew[0].discard();
			crew[1].discard();
			ship.discard();
			helper.succeed();
		});
	}

	/**
	 * The real thing, with no stand-ins: a ship crewed by PirateCrew.provision (ten linked plunderers, seated and on its
	 * roster) holds out while any of them lives, rolls its hold only when the roster has recorded every death, rolls it
	 * from the galleon table's own roll for its seed, lies still, and forgets its roster when it is removed.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 160, skyAccess = true)
	public void realCrewDeathsBeatTheShipAndOpenItsHold(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper, false);
		int seated = PirateCrew.provision(level, ship);
		ship.setCrewed(seated > 0);
		level.addFreshEntityWithPassengers(ship);
		helper.assertTrue(seated == PirateCrew.STATIONS.size() && ship.getPassengers().size() == seated,
				"Provisioning should seat a crew member at each of the " + PirateCrew.STATIONS.size() + " stations, not " + seated);
		helper.assertTrue(PirateCrew.Roster.get(level).knows(ship.getUUID()), "The crew should be on the roster");
		List<OwnerPlunderer> crew = new ArrayList<>();
		for (var passenger : ship.getPassengers()) {
			crew.add((OwnerPlunderer) passenger);
		}
		helper.runAfterDelay(5, () -> {
			for (int i = 0; i < crew.size() - 1; i++) {
				crew.get(i).kill(level);
			}
		});
		helper.runAfterDelay(50, () -> {
			helper.assertTrue(!PirateCrew.defeated(ship) && !ship.lootRolled() && ship.hold().isEmpty(),
					"With one crew member alive the ship holds out and its hold stays shut");
			crew.get(crew.size() - 1).kill(level);
		});
		helper.runAfterDelay(80, () -> {
			helper.assertTrue(PirateCrew.defeated(ship), "With every death on the roster the crew are beaten");
			helper.assertTrue(ship.lootRolled() && ship.beaten() && ship.heaveTo(), "Beaten, the ship rolls its hold and lies still");
			var expected = PirateShipTesting.expectedHold(level, ship, ship.position());
			for (int slot = 0; slot < PirateShip.HOLD_SLOTS; slot++) {
				helper.assertTrue(ItemStack.matches(ship.hold().getItem(slot), expected.getItem(slot)),
						"Hold slot " + slot + " should be the table's roll for this ship, " + expected.getItem(slot) + ", not " + ship.hold().getItem(slot));
			}
			ship.discard();
		});
		helper.runAfterDelay(82, () -> {
			helper.assertTrue(!PirateCrew.Roster.get(level).knows(ship.getUUID()), "Removed for good, the ship's roster is forgotten");
			helper.succeed();
		});
	}

	/**
	 * Protection holds at impact: with an operator region laid over the ship where it already floats, or only over the
	 * column of cells where real balls fired from outside it strike (not where they were the tick before), neither another
	 * ship's crew (judged as the unprivileged pirate identity) nor a player dents the hull; with the region gone, the same
	 * shot does.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 80, skyAccess = true)
	public void protectionAtTheShipOrTheImpactShieldsItFromCannonFire(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper, false);
		level.addFreshEntity(ship);
		ServerPlayer op = helper.makeMockServerPlayerInLevel();
		String name = "pirate_" + op.getUUID().toString().replace("-", "");
		OwnerPlunderer[] enemy = {null};
		ServerPlayer[] gunner = {null};
		helper.runAfterDelay(3, () -> {
			enemy[0] = helper.spawnWithNoFreeWill(OwnerPlunderer.TYPE, helper.relativeVec(ship.position().add(10.0, 6.0, 0.0)));
			PirateShipTesting.link(enemy[0], java.util.UUID.randomUUID(), 2);
			gunner[0] = TestPlayers.survivor(helper);
			// A one-block region over the ship's own place, laid while it floats there.
			BlockPos at = ship.blockPosition();
			protect(helper, op, "protect pos1", at);
			protect(helper, op, "protect pos2", at);
			protect(helper, op, "protect create " + name, at);
			PirateShipTesting.fire(level, enemy[0], ship.position().add(3.4, 0.6, 2.5), new Vec3(-1.8, 0.0, 0.0));
			PirateShipTesting.fire(level, gunner[0], ship.position().add(3.4, 0.6, -2.0), new Vec3(-1.8, 0.0, 0.0));
		});
		helper.runAfterDelay(12, () -> {
			helper.assertTrue(ship.damage() == 0.0F, "A region over the ship shields it from enemy and player fire, but it took " + ship.damage());
			protect(helper, op, "protect remove " + name, ship.blockPosition());
			// Now a region over only the one column of cells where the shots strike the port side (x + 2.8, inside the deck
			// squares' reach), not over the ship's own place nor where the balls are the tick before they strike (x + 3.2):
			// real balls fired from outside it (x + 5.0) fly across its boundary into the hull. ThrowableProjectile.tick sets
			// a ball at its hit's location before the hit is applied (26.3 bytecode), so the ball's own place is the impact.
			BlockPos impact = BlockPos.containing(ship.position().add(2.8, 0.0, 0.0));
			helper.assertTrue(impact.getX() != BlockPos.containing(ship.position().add(3.2, 0.0, 0.0)).getX(),
					"The impact cell and the tick-before cell should differ for this test to tell them apart");
			protect(helper, op, "protect pos1", new BlockPos(impact.getX(), impact.getY() - 1, impact.getZ() - 6));
			protect(helper, op, "protect pos2", new BlockPos(impact.getX(), impact.getY() + 2, impact.getZ() + 7));
			protect(helper, op, "protect create " + name, ship.blockPosition());
			PirateShipTesting.fire(level, enemy[0], ship.position().add(5.0, 0.6, 5.0), new Vec3(-1.8, 0.0, 0.0));
			PirateShipTesting.fire(level, gunner[0], ship.position().add(5.0, 0.6, -4.0), new Vec3(-1.8, 0.0, 0.0));
		});
		helper.runAfterDelay(22, () -> {
			helper.assertTrue(ship.damage() == 0.0F, "A region over only the impact shields it too, but it took " + ship.damage());
			protect(helper, op, "protect remove " + name, ship.blockPosition());
			PirateShipTesting.fire(level, enemy[0], ship.position().add(5.0, 0.6, 5.0), new Vec3(-1.8, 0.0, 0.0));
		});
		helper.runAfterDelay(32, () -> {
			helper.assertTrue(ship.damage() > 0.0F, "With the region gone the same enemy shot dents the hull");
			enemy[0].discard();
			ship.discard();
			helper.succeed();
		});
	}
	/** A block state by its id, checked to exist (a misspelt id parses to air). */
	private static BlockState state(GameTestHelper helper, String id) {
		BlockState state = TownData.parse(id);
		helper.assertTrue(!state.isAir(), "The block state " + id + " should exist");
		return state;
	}

	/**
	 * Pistons never move the ship or its deck: a piston whose head extends into the ship's box and two deck squares
	 * leaves the ship exactly where it was (only its own checked sailing moves it).
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 60, skyAccess = true)
	public void pistonsNeverMoveTheShipOrItsDeck(GameTestHelper helper) {
		pool(helper);
		PirateShip ship = ship(helper, false);
		helper.getLevel().addFreshEntity(ship);
		Vec3 start = ship.position();
		helper.assertTrue(ship.getPistonPushReaction() == PushReaction.IGNORE_ENTITY, "Pistons should ignore the ship");
		// Above the water at the starboard rail, facing west: the head reaches x 24, inside the ship's box (x 19 to 25)
		// and the deck squares (x 19.5 to 24.5) at z 21, where the second and third squares meet.
		BlockPos piston = new BlockPos(25, SURFACE + 1, 21);
		helper.runAfterDelay(2, () -> {
			for (PirateShipDeck deck : ship.decks()) {
				helper.assertTrue(deck.getPistonPushReaction() == PushReaction.IGNORE_ENTITY, "Pistons should ignore a deck square");
			}
			helper.setBlock(piston.east(), Blocks.REDSTONE_BLOCK);
			helper.setBlock(piston, state(helper, "minecraft:piston[facing=west]"));
		});
		helper.runAfterDelay(14, () -> {
			helper.assertTrue(helper.getBlockState(piston).getValue(PistonBaseBlock.EXTENDED), "The piston should have extended");
			helper.assertTrue(ship.position().equals(start), "The piston must not move the ship from " + start + " to " + ship.position());
			for (int i = 0; i < PirateShipDeck.KEEL.length; i++) {
				Vec3 keel = start.add(0.0, 0.0, PirateShipDeck.KEEL[i]);
				helper.assertTrue(ship.decks().get(i).position().distanceTo(keel) < 1.0E-6,
						"Deck square " + i + " should be at " + keel + ", not " + ship.decks().get(i).position());
			}
			ship.discard();
			helper.succeed();
		});
	}

	/**
	 * What cannot be seen counts as settled: a place beside a chunk that is not loaded, within the clearance, raises no
	 * ship, and asking loads nothing. (Villages are looked for at the waterline and above it; see PirateSeas.)
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 20, skyAccess = true)
	public void anUnloadedChunkInTheClearanceRaisesNoShip(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos centre = helper.absolutePos(new BlockPos(22, SURFACE, 22));
		int cx = centre.getX() >> 4, cz = centre.getZ() >> 4;
		Integer unloaded = null;
		for (int i = 1; i <= 4_096 && unloaded == null; i++) {
			if (!level.hasChunkAt(new BlockPos((cx + i) << 4, centre.getY(), cz << 4))) {
				unloaded = cx + i;
			}
		}
		helper.assertTrue(unloaded != null, "Some chunk east of the test should be unloaded");
		BlockPos edge = new BlockPos(unloaded << 4, centre.getY(), cz << 4);
		// 12 blocks short of it, in the loaded chunk before it: well within the 48-block clearance.
		Vec3 beside = new Vec3(edge.getX() - 12 + 0.5, centre.getY() + PirateWaters.WATERLINE, centre.getZ() + 0.5);
		helper.assertTrue(level.hasChunkAt(BlockPos.containing(beside)), "The place itself is loaded");
		String why = PirateSeas.settlementNear(level, beside);
		helper.assertTrue(why != null && why.startsWith("unloaded"), "An unloaded chunk in the clearance should refuse a ship: " + why);
		helper.assertTrue(PirateSeas.raise(level, beside, 0.0F, level.getGameTime()) == null, "No ship is raised there");
		helper.assertTrue(!level.hasChunkAt(edge), "Asking must not load the unloaded chunk");
		helper.succeed();
	}

	/**
	 * The bow's stem rises through the top of the waterline row a quarter block ahead of the hull's 16 blocks, so the
	 * rig's box is checked from the waterline row: a block in that row just ahead of the hull box (but within the rig's
	 * reach) blocks the place, while one below the waterline beside the hull still does not.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 40, skyAccess = true)
	public void theBowsStemNeedsTheWaterlineRowClear(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		Vec3 at = helper.absoluteVec(CENTRE);
		helper.assertTrue(PirateWaters.clear(level, at, 0.0F), "The pool's centre starts clear");
		// The hull box ends at z 30.0; the stem reaches 30.25 in the waterline row.
		BlockPos stem = new BlockPos(22, SURFACE, 30);
		helper.setBlock(stem, Blocks.STONE);
		helper.assertTrue(!PirateWaters.clear(level, at, 0.0F), "A block in the waterline row just ahead of the bow should block it");
		helper.assertTrue(!PirateWaters.sweep(level, at, 0.0F, at.add(0.0, 0.0, PirateShip.SPEED), 0.0F), "Nor may it sail into it");
		helper.setBlock(stem, Blocks.WATER);
		helper.assertTrue(PirateWaters.clear(level, at, 0.0F), "With the water back it is clear again");
		helper.succeed();
	}

	/**
	 * Clutter: above the water the rig needs air, so a torch at rail height blocks the place; under it the hull needs open
	 * water, so a waterlogged slab, fence or rail blocks it, while kelp and seagrass (the sea's own plants) do not.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 40, skyAccess = true)
	public void onlyAirAboveAndOpenWaterBelow(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		Vec3 at = helper.absoluteVec(CENTRE);
		BlockPos torch = new BlockPos(25, SURFACE + 1, 22);
		helper.setBlock(torch, Blocks.TORCH);
		helper.assertTrue(!PirateWaters.clear(level, at, 0.0F), "A torch in the rig's way should block it");
		helper.setBlock(torch, Blocks.AIR);
		// The sea's own plants, each on a stone footing below the hull's water rows.
		for (BlockPos footing : List.of(new BlockPos(20, 1, 20), new BlockPos(23, 1, 25), new BlockPos(24, 1, 15), new BlockPos(21, 1, 18))) {
			helper.setBlock(footing, Blocks.STONE);
		}
		helper.setBlock(new BlockPos(20, 2, 20), Blocks.KELP_PLANT);
		helper.setBlock(new BlockPos(20, 3, 20), Blocks.KELP);
		helper.setBlock(new BlockPos(23, 2, 25), Blocks.SEAGRASS);
		helper.setBlock(new BlockPos(24, 2, 15), state(helper, "minecraft:tall_seagrass[half=lower]"));
		helper.setBlock(new BlockPos(24, 3, 15), state(helper, "minecraft:tall_seagrass[half=upper]"));
		helper.assertTrue(PirateWaters.clear(level, at, 0.0F), "Kelp and seagrass under the hull should not block it");
		List<BlockPos> spots = List.of(new BlockPos(21, 3, 24), new BlockPos(23, 2, 17), new BlockPos(21, 2, 18));
		List<String> waterlogged = List.of("minecraft:oak_slab[type=bottom,waterlogged=true]", "minecraft:oak_fence[waterlogged=true]",
				"minecraft:rail[waterlogged=true]");
		for (int i = 0; i < spots.size(); i++) {
			helper.setBlock(spots.get(i), state(helper, waterlogged.get(i)));
			helper.assertTrue(!PirateWaters.clear(level, at, 0.0F), "A " + waterlogged.get(i) + " in the hull's water should block it");
			helper.setBlock(spots.get(i), Blocks.WATER);
			helper.assertTrue(PirateWaters.clear(level, at, 0.0F), "With the water back it is clear again");
		}
		helper.succeed();
	}

	/**
	 * A sinking ship settles no lower than the hull's checked depth (its waterline row less {@value PirateWaters#DEPTH}),
	 * here in water deep enough that nothing else would stop it.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 160, skyAccess = true)
	public void aSinkingShipSettlesNoLowerThanItsCheckedDepth(GameTestHelper helper) {
		pool(helper);
		int deep = SURFACE + 4;
		// A walled basin over the pool, deep enough under the whole rig that the keel never meets the floor.
		for (int x = 17; x <= 27; x++) {
			for (int z = 11; z <= 35; z++) {
				boolean wall = x == 17 || x == 27 || z == 11 || z == 35;
				for (int y = FLOOR + 1; y <= deep; y++) {
					helper.setBlock(new BlockPos(x, y, z), wall ? Blocks.STONE : Blocks.WATER);
				}
			}
		}
		ServerLevel level = helper.getLevel();
		Vec3 at = helper.absoluteVec(new Vec3(22.0, deep + PirateWaters.WATERLINE, 22.0));
		PirateShip ship = PirateShips.SHIP.create(level, EntitySpawnReason.EVENT);
		ship.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		ship.launch(at, false);
		level.addFreshEntity(ship);
		double floor = helper.absolutePos(new BlockPos(0, deep - PirateWaters.DEPTH, 0)).getY();
		helper.runAfterDelay(2, () -> PirateShipTesting.sink(level, ship));
		helper.onEachTick(() -> helper.assertTrue(ship.isRemoved() || ship.getY() >= floor - 1.0E-9,
				"A sinking ship must stay at or above " + floor + ", not " + ship.getY()));
		helper.runAfterDelay(140, () -> {
			helper.assertTrue(ship.sunk() && ship.getY() >= floor - 1.0E-9 && ship.getY() < floor + PirateShip.SINK_STEP + 1.0E-6,
					"It should have settled to its checked depth, " + floor + ", and no lower: " + ship.getY());
			ship.discard();
			helper.succeed();
		});
	}

	/** A sinking ship never settles into an operator region laid under it: its keel stops above the region. */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 80, skyAccess = true)
	public void aSinkingShipStopsAboveAnOperatorRegion(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper, false);
		level.addFreshEntity(ship);
		ServerPlayer op = helper.makeMockServerPlayerInLevel();
		String name = "pirate_" + op.getUUID().toString().replace("-", "");
		// One row of water under the hull, two rows below the waterline, made an operator region after the ship is there.
		protect(helper, op, "protect pos1", helper.absolutePos(new BlockPos(19, SURFACE - 2, 14)));
		protect(helper, op, "protect pos2", helper.absolutePos(new BlockPos(24, SURFACE - 2, 29)));
		protect(helper, op, "protect create " + name, helper.absolutePos(new BlockPos(19, SURFACE - 2, 14)));
		double regionTop = helper.absolutePos(new BlockPos(0, SURFACE - 1, 0)).getY();
		double start = ship.getY();
		helper.runAfterDelay(2, () -> PirateShipTesting.sink(level, ship));
		helper.onEachTick(() -> helper.assertTrue(ship.isRemoved() || ship.getY() - PirateWaters.KEEL >= regionTop - 1.0E-9,
				"The keel must stay above the region's top, " + regionTop + ", not " + (ship.getY() - PirateWaters.KEEL)));
		helper.runAfterDelay(60, () -> {
			double keel = ship.getY() - PirateWaters.KEEL;
			protect(helper, op, "protect remove " + name, helper.absolutePos(new BlockPos(19, SURFACE - 2, 14)));
			helper.assertTrue(ship.sunk() && ship.getY() < start && keel >= regionTop - 1.0E-9 && keel < regionTop + PirateShip.SINK_STEP + 1.0E-6,
					"It should settle until its keel is just above the region (" + regionTop + "), not at " + keel);
			ship.discard();
			helper.succeed();
		});
	}

	/** A ship added without a launch (a /summon, no anchor saved) anchors its patrol where it first floats, and saves it. */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 40, skyAccess = true)
	public void aSummonedShipAnchorsWhereItFloats(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		Vec3 at = helper.absoluteVec(CENTRE);
		var server = level.getServer();
		String command = String.format(Locale.ROOT, "summon jugcraft:pirate_ship %.4f %.4f %.4f", at.x, at.y, at.z);
		try {
			server.getCommands().getDispatcher().execute(command, server.createCommandSourceStack().withLevel(level).withSuppressedOutput());
		} catch (Exception e) {
			helper.fail("/" + command + " failed: " + e.getMessage());
		}
		List<PirateShip> found = level.getEntitiesOfClass(PirateShip.class, new AABB(at, at).inflate(1.0));
		helper.assertTrue(found.size() == 1, "One summoned ship, not " + found.size());
		PirateShip ship = found.getFirst();
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(ship.anchor().distanceTo(ship.position()) < 1.0E-6 && ship.anchor().distanceTo(at) < 1.0E-3,
					"A summoned ship should anchor where it floats, " + ship.position() + ", not at " + ship.anchor());
			helper.assertTrue(copy(helper, ship).anchor().equals(ship.anchor()), "And its anchor is saved");
			ship.discard();
			helper.succeed();
		});
	}

	/**
	 * A ship saved before its first tick, never launched (a /summon whose chunk is saved before it ticks), saves no
	 * anchor, and reloaded it anchors where it is placed on its first tick, not at the world's origin; that anchor is
	 * then saved. An anchored save is kept as it was: reloaded, the ship keeps its saved anchor, not where it floats.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 40, skyAccess = true)
	public void anUnanchoredSaveAnchorsWhereItIsReloaded(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		Vec3 at = helper.absoluteVec(CENTRE);
		PirateShip unticked = PirateShips.SHIP.create(level, EntitySpawnReason.COMMAND);
		unticked.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		unticked.saveWithoutId(out);
		var unanchored = out.buildResult();
		helper.assertTrue(!unanchored.contains("AnchorX") && !unanchored.contains("AnchorY") && !unanchored.contains("AnchorZ"),
				"A ship saved before its first tick should save no anchor: " + unanchored);
		PirateShip reloaded = PirateShips.SHIP.create(level, EntitySpawnReason.LOAD);
		reloaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), unanchored));
		helper.assertTrue(reloaded.position().distanceTo(at) < 1.0E-6, "It reloads where it was saved, " + at);
		level.addFreshEntity(reloaded);
		// An anchored save: launched with its anchor 8 blocks off to port of where it floats.
		Vec3 anchor = at.add(8.0, 0.0, 0.0);
		PirateShip launched = ship(helper, false);
		launched.launch(anchor, false);
		out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		launched.saveWithoutId(out);
		var anchored = out.buildResult();
		helper.assertTrue(anchored.contains("AnchorX") && anchored.contains("AnchorY") && anchored.contains("AnchorZ"),
				"A launched ship should save its anchor: " + anchored);
		PirateShip[] kept = new PirateShip[1];
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(reloaded.anchor().distanceTo(at) < 1.0E-6,
					"On its first tick the reloaded ship should anchor where it was placed, " + at + ", not at " + reloaded.anchor());
			helper.assertTrue(copy(helper, reloaded).anchor().distanceTo(at) < 1.0E-6, "That anchor is then saved");
			reloaded.discard();
			kept[0] = PirateShips.SHIP.create(level, EntitySpawnReason.LOAD);
			kept[0].load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), anchored));
			level.addFreshEntity(kept[0]);
		});
		helper.runAfterDelay(6, () -> {
			helper.assertTrue(kept[0].position().distanceTo(at) < 1.0E-6 && kept[0].anchor().distanceTo(anchor) < 1.0E-6,
					"A reloaded anchored ship should keep its saved anchor, " + anchor + ", not " + kept[0].anchor());
			kept[0].discard();
			helper.succeed();
		});
	}

	// ------------------------------------------------------------------------------------------------- 14 November: crews

	/** Saves {@code ship} (its crew inside it, as vanilla does) and each crew member alone, for a restore. */
	private record Saved(CompoundTag ship, List<CompoundTag> crew) {
	}

	private static Saved save(GameTestHelper helper, PirateShip ship) {
		ServerLevel level = helper.getLevel();
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		ship.saveWithoutId(out);
		List<CompoundTag> crew = new ArrayList<>();
		for (Entity passenger : ship.getPassengers()) {
			TagValueOutput one = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
			passenger.saveWithoutId(one);
			crew.add(one.buildResult());
		}
		return new Saved(out.buildResult(), crew);
	}

	/**
	 * As a chunk unloading and loading again does: the ship and its crew leave the level (saved, not destroyed), then come
	 * back from their saves, the crew forced back onto their stations as the game's recursive load does, all added again.
	 */
	private static PirateShip reload(GameTestHelper helper, PirateShip ship, Saved saved) {
		ServerLevel level = helper.getLevel();
		List<Entity> riders = List.copyOf(ship.getPassengers());
		ship.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		riders.forEach(rider -> rider.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK));
		PirateShip restored = PirateShips.SHIP.create(level, EntitySpawnReason.LOAD);
		restored.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.ship()));
		for (CompoundTag tag : saved.crew()) {
			OwnerPlunderer crew = OwnerPlunderer.TYPE.create(level, EntitySpawnReason.LOAD);
			crew.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
			helper.assertTrue(crew.startRiding(restored, true, true), "A reloaded crew member takes its station again");
		}
		level.addFreshEntityWithPassengers(restored);
		return restored;
	}

	private static List<OwnerPlunderer> crewOf(Entity ship) {
		return ship.getPassengers().stream().filter(OwnerPlunderer.class::isInstance).map(OwnerPlunderer.class::cast).toList();
	}

	/** Kills a crew member for real (its roster records the death) and takes it off at once: a dying mob stays seated. */
	private static void kill(ServerLevel level, OwnerPlunderer crew) {
		crew.kill(level);
		crew.stopRiding();
	}

	/**
	 * An old ten-crew ship (provisioned before small crews: its save has no CrewFormat) keeps working: saved with three of
	 * its crew dead, reloaded, it is read as the legacy format with its seven living crew at their old stations and its
	 * roster's deaths kept; no new crew is ever made for it (provisioning it again, with or without passengers, seats
	 * nobody); the next hand takes the wheel when its captain falls; it is beatable by killing the rest, the defeat is
	 * recorded once in the roster and the sea's ledger (its region's rest set once), its hold is rolled exactly once (the
	 * table's roll for its seed), and a further save and reload neither refills, rerolls nor resets the hold or its flags.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 400, skyAccess = true)
	public void anOldTenCrewShipStaysBeatableThroughSaveAndLoad(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper, true);
		int seated = PirateCrew.provision(level, ship);
		ship.setCrewed(seated > 0);
		helper.assertTrue(seated == 10 && ship.crewFormat() == PirateShip.LEGACY_CREW, "Setup: a ten-crew ship of the old format");
		level.addFreshEntityWithPassengers(ship);
		PirateSeas seas = PirateSeas.get(level);
		seas.raised(ship.getUUID(), PirateSeas.region(ship.getX(), ship.getZ()), ship.blockPosition(), level.getGameTime());
		UUID id = ship.getUUID();
		PirateShip[] live = {ship};
		helper.runAfterDelay(3, () -> {
			for (OwnerPlunderer crew : crewOf(ship)) {
				if (crew.shipStation() == 0 || crew.shipStation() == 2 || crew.shipStation() == 9) {
					kill(level, crew);
				}
			}
		});
		helper.runAfterDelay(8, () -> {
			helper.assertTrue(crewOf(ship).size() == 7, "Seven of the ten should still ride it");
			Saved saved = save(helper, ship);
			saved.ship().remove("CrewFormat"); // as a save written before the field existed
			live[0] = reload(helper, ship, saved);
			PirateShip restored = live[0];
			helper.assertTrue(restored.getUUID().equals(id) && restored.crewFormat() == PirateShip.LEGACY_CREW && restored.crewed()
					&& !restored.lootRolled() && !restored.beaten() && restored.hold().isEmpty(),
					"Reloaded, it is the same legacy ship, crewed, unbeaten, its hold shut and empty");
			helper.assertTrue(crewOf(restored).size() == 7 && crewOf(restored).stream().allMatch(c -> id.equals(c.shipLink())),
					"The same seven crew ride it, still linked");
			helper.assertTrue(PirateCrew.Roster.get(level).knows(id) && !PirateCrew.defeated(restored), "Its roster is kept and it is not beaten");
			helper.assertTrue(PirateCrew.provision(level, restored) == 0 && crewOf(restored).size() == 7,
					"Provisioning a ship that has crew seats nobody");
			PirateShip bare = PirateShips.SHIP.create(level, EntitySpawnReason.LOAD);
			bare.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.ship()));
			helper.assertTrue(PirateCrew.provision(level, bare) == 0 && bare.getPassengers().isEmpty(),
					"A copy of it with no passengers gets no new crew either: the roster already holds it");
		});
		// The captain was among the dead: within the succession time a deck hand holds the wheel.
		helper.runAfterDelay(8 + PirateShip.SUCCESSION_TICKS + 30, () -> {
			PirateShip restored = live[0];
			helper.assertTrue(PirateCrew.helmManned(restored), "The next hand should have taken the wheel");
			OwnerPlunderer helm = crewOf(restored).stream().filter(c -> c.shipStation() == PirateCrew.HELM).findFirst().orElseThrow();
			helper.assertTrue(crewOf(restored).size() == 7 && crewOf(restored).stream().map(OwnerPlunderer::shipStation).distinct().count() == 7,
					"Nobody was made or lost by the change of helmsman; every station is held once");
			crewOf(restored).forEach(c -> kill(level, c));
			helper.assertTrue(helm.isDeadOrDying(), "Setup: the new helmsman falls too");
		});
		helper.runAfterDelay(8 + PirateShip.SUCCESSION_TICKS + 70, () -> {
			PirateShip restored = live[0];
			helper.assertTrue(PirateCrew.defeated(restored) && restored.lootRolled() && restored.beaten(), "With every death recorded it is beaten");
			var expected = PirateShipTesting.expectedHold(level, restored, restored.position());
			for (int slot = 0; slot < PirateShip.HOLD_SLOTS; slot++) {
				helper.assertTrue(ItemStack.matches(restored.hold().getItem(slot), expected.getItem(slot)),
						"Hold slot " + slot + " should be the table's one roll for this ship");
			}
			PirateSeas.Ship record = PirateSeas.get(level).ship(id);
			helper.assertTrue(record != null && record.state() == PirateSeas.State.DEFEATED, "The sea's ledger records the defeat");
			PirateSeas.get(level).defeated(id, level.getGameTime() + 500L);
			helper.assertTrue(record.equals(PirateSeas.get(level).ship(id)),
					"A second report of the same defeat changes nothing (nor its region's rest): it was recorded once");
			Saved again = save(helper, restored);
			PirateShip copy = PirateShips.SHIP.create(level, EntitySpawnReason.LOAD);
			copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), again.ship()));
			helper.assertTrue(copy.lootRolled() && copy.beaten() && copy.heaveTo() && copy.crewFormat() == PirateShip.LEGACY_CREW,
					"Saved and loaded again it is still rolled, beaten, hove to and of the old format");
			for (int slot = 0; slot < PirateShip.HOLD_SLOTS; slot++) {
				helper.assertTrue(ItemStack.matches(copy.hold().getItem(slot), restored.hold().getItem(slot)), "The hold is as it was");
			}
			PirateShipTesting.rollHold(level, copy);
			for (int slot = 0; slot < PirateShip.HOLD_SLOTS; slot++) {
				helper.assertTrue(ItemStack.matches(copy.hold().getItem(slot), restored.hold().getItem(slot)), "Never rolled again");
			}
			helper.assertTrue(PirateCrew.provision(level, copy) == 0 && copy.getPassengers().isEmpty(), "And no crew is ever made for it again");
			restored.discard();
			helper.succeed();
		});
	}

	/**
	 * A small crew: the stations a crew of three, four and five man (the captain, one gunner a side, then the fore deck
	 * hands), each seated once, clamped to 3..5; the crew for a player's armour; and never a second crew, even once the
	 * first is gone without dying.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 20, skyAccess = true)
	public void aSmallCrewIsMadeOnceAndNeverAgain(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		helper.assertTrue(java.util.Arrays.equals(PirateCrew.crew(3), new int[] {0, 3, 5})
				&& java.util.Arrays.equals(PirateCrew.crew(4), new int[] {0, 3, 5, 6})
				&& java.util.Arrays.equals(PirateCrew.crew(5), new int[] {0, 3, 5, 6, 7})
				&& PirateCrew.crew(1).length == 3 && PirateCrew.crew(9).length == 5, "Crews of 3 to 5 at the agreed stations");
		helper.assertTrue(PirateFairness.crewFor(10) == 3 && PirateFairness.crewFor(15) == 4 && PirateFairness.crewFor(20) == 5,
				"Three for the least-armoured player a ship is raised for, five for diamond");
		for (int size = 3; size <= 5; size++) {
			PirateShip ship = ship(helper, false);
			int seated = PirateCrew.provision(level, ship, PirateCrew.crew(size));
			ship.setCrewed(seated > 0);
			ship.setCrewFormat(PirateShip.SMALL_CREW);
			level.addFreshEntityWithPassengers(ship);
			List<OwnerPlunderer> crew = crewOf(ship);
			helper.assertTrue(seated == size && crew.size() == size, "A crew of " + size + " seats " + size + ", not " + seated);
			helper.assertTrue(crew.stream().filter(c -> PirateCrew.STATIONS.get(c.shipStation()).role() == Role.HELM).count() == 1
					&& crew.stream().filter(c -> PirateCrew.STATIONS.get(c.shipStation()).role() == Role.GUNNER).count() == 2
					&& crew.stream().filter(c -> PirateCrew.STATIONS.get(c.shipStation()).role() == Role.GUNNER)
							.map(c -> Math.signum(PirateCrew.STATIONS.get(c.shipStation()).seat().x)).distinct().count() == 2,
					"A captain at the helm and a gunner on each side");
			helper.assertTrue(PirateCrew.helmManned(ship) && PirateCrew.Roster.get(level).knows(ship.getUUID()), "Helmed and on the roster");
			helper.assertTrue(PirateCrew.provision(level, ship, PirateCrew.crew(5)) == 0, "A crewed ship gets no second crew");
			crew.forEach(Entity::discard); // gone without dying, as on Peaceful
			helper.assertTrue(ship.getPassengers().isEmpty() && PirateCrew.provision(level, ship, PirateCrew.crew(5)) == 0
					&& ship.getPassengers().isEmpty(), "Its crew gone, it still gets none: no crew is ever made twice for a ship");
			helper.assertTrue(!PirateCrew.defeated(ship), "Discarded crew never beat a ship");
			Saved saved = save(helper, ship);
			PirateShip copy = PirateShips.SHIP.create(level, EntitySpawnReason.LOAD);
			copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.ship()));
			helper.assertTrue(copy.crewFormat() == PirateShip.SMALL_CREW, "The small-crew format is saved");
			ship.discard();
		}
		helper.succeed();
	}

	/**
	 * Captain lost on a small crew: nobody steers until, after the succession time, a deck hand takes the wheel (moved to
	 * the helm's station, not made); with only gunners left a gunner does; the roster and its deaths are unchanged.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 300, skyAccess = true)
	public void theNextHandTakesTheWheelWhenTheCaptainFalls(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper, false);
		ship.setCrewed(PirateCrew.provision(level, ship, PirateCrew.crew(4)) > 0);
		level.addFreshEntityWithPassengers(ship);
		UUID[] deckHand = {null}, gunner = {null};
		helper.runAfterDelay(2, () -> {
			deckHand[0] = crewOf(ship).stream().filter(c -> c.shipStation() == 6).findFirst().orElseThrow().getUUID();
			gunner[0] = crewOf(ship).stream().filter(c -> c.shipStation() == 3).findFirst().orElseThrow().getUUID();
			kill(level, crewOf(ship).stream().filter(c -> c.shipStation() == PirateCrew.HELM).findFirst().orElseThrow());
		});
		helper.runAfterDelay(10, () -> helper.assertTrue(!PirateCrew.helmManned(ship) && !ship.mayMove(),
				"With its captain dead nobody steers at first, and it makes no way"));
		helper.runAfterDelay(2 + PirateShip.SUCCESSION_TICKS + 30, () -> {
			OwnerPlunderer helm = crewOf(ship).stream().filter(c -> c.shipStation() == PirateCrew.HELM).findFirst().orElse(null);
			helper.assertTrue(helm != null && helm.getUUID().equals(deckHand[0]), "The deck hand should have taken the wheel");
			helper.assertTrue(crewOf(ship).size() == 3, "Nobody was made: three living crew");
			kill(level, helm);
		});
		helper.runAfterDelay(2 + 2 * PirateShip.SUCCESSION_TICKS + 70, () -> {
			OwnerPlunderer helm = crewOf(ship).stream().filter(c -> c.shipStation() == PirateCrew.HELM).findFirst().orElse(null);
			helper.assertTrue(helm != null && helm.getUUID().equals(gunner[0]) && crewOf(ship).size() == 2,
					"With only gunners left, the lowest-stationed gunner takes the wheel");
			helper.assertTrue(!PirateCrew.defeated(ship) && PirateCrew.Roster.get(level).knows(ship.getUUID()), "Two living crew: not beaten");
			for (Entity passenger : List.copyOf(ship.getPassengers())) {
				passenger.discard();
			}
			ship.discard();
			helper.succeed();
		});
	}

	/**
	 * A crewed ship whose crew vanished without dying (Peaceful's despawn, an admin's discard), unbeaten, with nobody near,
	 * is scuttled at the end of the abandonment time: sunk with an empty hold (it was never rolled: no loot), then gone,
	 * freeing its place in the sea's ledger (its id tombstoned).
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", padding = 64, maxTicks = 400, skyAccess = true)
	public void aCrewlessUnbeatenShipIsScuttledEmpty(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper, true);
		ship.setCrewed(PirateCrew.provision(level, ship, PirateCrew.crew(3)) > 0);
		level.addFreshEntityWithPassengers(ship);
		PirateSeas.get(level).raised(ship.getUUID(), PirateSeas.region(ship.getX(), ship.getZ()), ship.blockPosition(), level.getGameTime());
		helper.runAfterDelay(2, () -> {
			for (Entity passenger : List.copyOf(ship.getPassengers())) {
				passenger.discard();
			}
			helper.assertTrue(!PirateCrew.defeated(ship), "Discarded crew do not beat it");
			PirateShipTesting.abandoned(ship, PirateShip.ABANDON_TICKS - 60);
		});
		helper.runAfterDelay(100, () -> {
			helper.assertTrue(ship.sunk() && !ship.lootRolled() && ship.hold().isEmpty(), "Abandoned, it is scuttled with nothing in it");
			helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, ship.getBoundingBox().inflate(8.0)).isEmpty(), "It spills nothing");
		});
		helper.succeedWhen(() -> {
			helper.assertTrue(ship.isRemoved(), "After settling it is gone");
			helper.assertTrue(PirateSeas.get(level).ship(ship.getUUID()) == null && PirateSeas.get(level).tombstoned(ship.getUUID()),
					"Its place in the ledger is freed and its id tombstoned");
		});
	}

	/**
	 * Fairness on the real ship: its crew leave an unarmoured player be; striking the ship (even where the blow is refused)
	 * or setting foot on its deck provokes it, and its crew then fight that player; a well-armoured player is fair game
	 * unprovoked.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 60, skyAccess = true)
	public void theCrewSpareTheUnarmouredUntilProvoked(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper, false);
		ship.setCrewed(PirateCrew.provision(level, ship, PirateCrew.crew(3)) > 0);
		level.addFreshEntityWithPassengers(ship);
		ServerPlayer player = TestPlayers.survivor(helper);
		player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
		Vec3 near = ship.position().add(8.0, 1.0, 0.0);
		player.snapTo(near.x, near.y, near.z, 90.0F, 0.0F);
		ServerPlayer other = TestPlayers.survivor(helper);
		other.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
		other.snapTo(near.x, near.y, near.z + 2.0, 90.0F, 0.0F);
		helper.runAfterDelay(2, () -> {
			OwnerPlunderer captain = crewOf(ship).stream().filter(c -> c.shipStation() == PirateCrew.HELM).findFirst().orElseThrow();
			helper.assertTrue(!captain.canAttack(player) && ship.provokedAt(player) == PirateFairness.NEVER,
					"An unarmoured stranger is left be");
			ship.hurtServer(level, level.damageSources().playerAttack(player), 1.0F);
			helper.assertTrue(ship.provokedAt(player) != PirateFairness.NEVER && captain.canAttack(player), "Striking the ship provokes its crew");
			helper.assertTrue(!captain.canAttack(other), "Only the one who struck: the other stranger is still left be");
			Vec3 deck = ship.position().add(0.0, 1.0, 2.0);
			other.snapTo(deck.x, deck.y, deck.z, 0.0F, 0.0F);
		});
		helper.runAfterDelay(5, () -> {
			OwnerPlunderer captain = crewOf(ship).stream().filter(c -> c.shipStation() == PirateCrew.HELM).findFirst().orElseThrow();
			helper.assertTrue(ship.provokedAt(other) != PirateFairness.NEVER && captain.canAttack(other), "Boarding provokes it too");
			ServerPlayer armoured = TestPlayers.survivor(helper);
			armoured.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
			armoured.snapTo(near.x, near.y, near.z - 2.0, 90.0F, 0.0F);
			armoured.getAttribute(Attributes.ARMOR).setBaseValue(PirateFairness.MIN_ARMOR);
			helper.assertTrue(captain.canAttack(armoured), "A well-armoured player is fair game unprovoked");
			for (Entity passenger : List.copyOf(ship.getPassengers())) {
				passenger.discard();
			}
			ship.discard();
			TestPlayers.leave(player);
			TestPlayers.leave(other);
			TestPlayers.leave(armoured);
			helper.succeed();
		});
	}

	// ------------------------------------------------------------------------------------------------- 14 November: routes

	/**
	 * A lane (PirateWaters.laneWater, laneClear): open, deep water along a straight lane with a turning basin at its end is
	 * clear; one shallow column (stone where a lane needs its third block of water), a block just over the waterline, or an
	 * end in unloaded water is not, and asking loads nothing; an operator region within the claim margin of it, though
	 * outside the water it uses, refuses the lane as a whole.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 40, skyAccess = true)
	public void lanesNeedDeepOpenSeaAndClearanceFromProtection(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		Vec3 at = helper.absoluteVec(CENTRE);
		Vec3 to = at.add(0.0, 0.0, 4.0);
		helper.assertTrue(PirateWaters.laneWater(level, at, to), "Open, deep water with room to turn makes a lane");
		BlockPos shallow = new BlockPos(22, SURFACE - PirateCourse.LANE_DEPTH, 24);
		helper.setBlock(shallow, Blocks.STONE);
		helper.assertTrue(!PirateWaters.laneWater(level, at, to), "A shoal three blocks down is too shallow for a lane");
		helper.setBlock(shallow, Blocks.WATER);
		BlockPos above = new BlockPos(16, SURFACE + 1, 26);
		helper.setBlock(above, Blocks.OAK_PLANKS);
		helper.assertTrue(!PirateWaters.laneWater(level, at, to), "Anything on the water across the lane refuses it");
		helper.setBlock(above, Blocks.AIR);
		helper.assertTrue(PirateWaters.laneWater(level, at, to), "Cleared again");
		Vec3 far = at.add(4_000_000.0, 0.0, 0.0);
		helper.assertTrue(!level.hasChunkAt(BlockPos.containing(far)), "Setup: the far end is not loaded");
		helper.assertTrue(!PirateWaters.laneWater(level, at, far) && !level.hasChunkAt(BlockPos.containing(far)),
				"A lane into unloaded water is refused, and loads nothing");
		ServerPlayer op = helper.makeMockServerPlayerInLevel();
		String name = "pirate_" + op.getUUID().toString().replace("-", "");
		BlockPos cell = helper.absolutePos(new BlockPos(22, SURFACE + 2, 22)).offset(PirateCourse.BASIN + 6, 0, 0);
		protect(helper, op, "protect pos1", cell);
		protect(helper, op, "protect pos2", cell);
		protect(helper, op, "protect create " + name, cell);
		helper.assertTrue(PirateWaters.laneWater(level, at, to) && !PirateWaters.laneClear(level, at, to),
				"A region within the claim margin refuses the lane, though its water is open");
		protect(helper, op, "protect remove " + name, cell);
		helper.succeed();
	}

	/**
	 * A stalled ship backs off: helmed into a wall across its bow that it keeps steering for, its steps are refused; after
	 * the stall time it goes astern, straight; and its rig never enters the wall.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 260, skyAccess = true)
	public void aStalledShipBacksOffAndNeverPushesThroughAWall(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		for (int x = POOL_MIN; x <= POOL_MAX; x++) {
			for (int y = SURFACE; y <= SURFACE + 2; y++) {
				helper.setBlock(new BlockPos(x, y, 37), Blocks.STONE);
			}
		}
		PirateShip ship = ship(helper, false);
		PirateShipTesting.helm(ship, true);
		Vec3 target = ship.position().add(0.0, 0.0, 12.0);
		PirateShipTesting.waypoint(ship, target);
		level.addFreshEntity(ship);
		AABB wall = new AABB(helper.absolutePos(new BlockPos(POOL_MIN, SURFACE, 37))).minmax(new AABB(helper.absolutePos(new BlockPos(POOL_MAX, SURFACE + 2, 37))));
		double[] mostAhead = {Double.NEGATIVE_INFINITY};
		boolean[] backed = {false};
		helper.onEachTick(() -> {
			if (ship.waypoint() == null && ship.helm().order() == PirateCourse.Order.SAIL) {
				PirateShipTesting.waypoint(ship, target); // as a helmsman who keeps choosing the same way would
			}
			AABB rig = PirateWaters.rigFootprint(ship.position(), ship.getYRot(), ship.getY(), ship.getY() + PirateWaters.MAST);
			helper.assertTrue(!rig.intersects(wall), "The rig must never enter the wall, but covers " + rig);
			mostAhead[0] = Math.max(mostAhead[0], ship.getZ());
			if (ship.getZ() < mostAhead[0] - 0.5) {
				backed[0] = true;
			}
		});
		helper.runAfterDelay(250, () -> {
			helper.assertTrue(backed[0], "Stalled against the wall it should have gone astern at least half a block");
			ship.discard();
			helper.succeed();
		});
	}

	/**
	 * Collision (PirateWaters.unobstructed): a step whose swept hull would meet a player in the water, or another pirate
	 * ship's deck, is refused; a pig, its own deck squares and its own crew are not in its way. A helmed ship sailing at a
	 * swimmer stops short of them.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 120, skyAccess = true)
	public void aShipStopsShortOfASwimmerAndAnotherShip(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper, false);
		ship.setCrewed(PirateCrew.provision(level, ship, PirateCrew.crew(3)) > 0);
		level.addFreshEntityWithPassengers(ship);
		Vec3 ahead = ship.position().add(0.0, 0.0, 0.5);
		ServerPlayer[] swimmer = new ServerPlayer[1];
		helper.runAfterDelay(2, () -> {
			AABB step = PirateWaters.stepBox(ship.position(), 0.0F, ahead, 0.0F);
			helper.assertTrue(PirateWaters.unobstructed(level, ship, step), "Its own crew and deck squares are not in its way");
			Mob pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, helper.relativeVec(ship.position().add(1.0, 0.0, 10.0)));
			helper.assertTrue(PirateWaters.unobstructed(level, ship, step), "Nor is a pig");
			pig.discard();
			swimmer[0] = TestPlayers.survivor(helper);
			Vec3 bow = ship.position().add(0.0, 0.0, 11.0);
			swimmer[0].snapTo(bow.x, bow.y, bow.z, 0.0F, 0.0F);
			helper.assertTrue(!PirateWaters.unobstructed(level, ship, step), "A swimmer under the bowsprit is in its way");
			swimmer[0].snapTo(bow.x, bow.y, bow.z + 2.0, 0.0F, 0.0F);
			PirateShipTesting.helm(ship, true);
			PirateShipTesting.waypoint(ship, ship.position().add(0.0, 0.0, 10.0));
		});
		// All callbacks are installed up front; 26.3 traverses its live scheduled-callback map.
		helper.onEachTick(() -> {
			if (swimmer[0] == null || ship.isRemoved()) return;
			AABB rig = PirateWaters.rigFootprint(ship.position(), ship.getYRot(), ship.getY(), ship.getY() + 4.0);
			helper.assertTrue(!rig.intersects(swimmer[0].getBoundingBox()), "It must stop short of the swimmer");
		});
		helper.runAfterDelay(82, () -> {
			TestPlayers.leave(swimmer[0]);
			for (Entity passenger : List.copyOf(ship.getPassengers())) {
				passenger.discard();
			}
			ship.discard();
			helper.succeed();
		});
	}

	/** Another ship's deck in the way refuses a step, as a ship never sails through another. */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 20, skyAccess = true)
	public void shipsNeverSailThroughEachOther(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper, false);
		level.addFreshEntity(ship);
		PirateShip other = ship(helper, false);
		other.snapTo(ship.getX() + 6.0, ship.getY(), ship.getZ(), 0.0F, 0.0F);
		level.addFreshEntity(other);
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(!PirateWaters.unobstructed(level, ship, PirateWaters.stepBox(ship.position(), 0.0F, ship.position(), 0.0F)),
					"A ship lying alongside, its deck within the rig's reach, blocks the way");
			helper.assertTrue(!PirateShipTesting.step(level, ship, ship.position().add(0.0, 0.0, 0.1), 0.0F),
					"And the step is refused");
			other.discard();
			ship.discard();
			helper.succeed();
		});
	}

	// ------------------------------------------------------------------------------------------------- 14 November: budgets

	/**
	 * The sea's ledger, format 2: a player near whom a ship was raised rests a day; another ship's anchor is measured for
	 * spacing; the rests are saved; and a format-1 save (no players, no version) reads with every ship, rest and tombstone
	 * kept and no player resting.
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 20)
	public void theSeasRestEachPlayerAndKeepShipsApart(GameTestHelper helper) {
		PirateSeas seas = new PirateSeas();
		long now = 5_000L;
		UUID player = UUID.randomUUID(), ship = UUID.randomUUID(), gone = UUID.randomUUID();
		helper.assertTrue(seas.playerRest(player) < now, "A player starts unrested");
		seas.restPlayer(player, now);
		helper.assertTrue(seas.playerRest(player) == now + PirateFairness.PLAYER_REST, "A raise rests the player a day");
		helper.assertTrue(PirateFairness.refusal(0, 20, seas.playerRest(player), now + 1, true, 1.0E9) != null
				&& PirateFairness.refusal(0, 20, seas.playerRest(player), now + PirateFairness.PLAYER_REST, true, 1.0E9) == null,
				"No second ship near them that day; one the next");
		seas.raised(ship, PirateSeas.region(0, 0), new BlockPos(100, 62, 100), now);
		helper.assertTrue(Math.abs(seas.nearestShip(100.5 + 30.0, 100.5 + 40.0) - 50.0) < 1.0E-6, "Spacing is measured from anchors");
		seas.gone(gone, now);
		Tag saved = PirateSeas.CODEC.encodeStart(NbtOps.INSTANCE, seas).getOrThrow();
		helper.assertTrue(saved instanceof CompoundTag tag && tag.getIntOr("version", 0) == PirateSeas.FORMAT, "Saved as format 2");
		PirateSeas reloaded = PirateSeas.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
		helper.assertTrue(reloaded.playerRest(player) == now + PirateFairness.PLAYER_REST && reloaded.ship(ship) != null
				&& reloaded.tombstoned(gone), "Player rests, ships and tombstones are saved");
		CompoundTag old = ((CompoundTag) saved).copy();
		old.remove("players");
		old.remove("version");
		PirateSeas legacy = PirateSeas.CODEC.parse(NbtOps.INSTANCE, old).getOrThrow();
		helper.assertTrue(legacy.ship(ship) != null && legacy.tombstoned(gone) && !legacy.regionFree(PirateSeas.region(0, 0), now)
				&& legacy.playerRest(player) < now, "A format-1 ledger keeps everything it had and rests no player");
		legacy.expire(now + 2L * PirateFairness.PLAYER_REST);
		reloaded.expire(now + 2L * PirateFairness.PLAYER_REST);
		helper.assertTrue(reloaded.playerRest(player) < now, "Spent rests are forgotten");
		helper.succeed();
	}

	/** Saving keeps the crew format and the helm's stall memory; a save without them reads as legacy, no stall. */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 20, skyAccess = true)
	public void aShipSavesItsCrewFormatAndHelm(GameTestHelper helper) {
		pool(helper);
		PirateShip ship = ship(helper, false);
		ship.setCrewFormat(PirateShip.SMALL_CREW);
		for (int i = 0; i < 10; i++) {
			ship.helm().after(PirateCourse.Order.SAIL, false);
		}
		PirateShip copy = copy(helper, ship);
		helper.assertTrue(copy.crewFormat() == PirateShip.SMALL_CREW && copy.helm().refused() == 10, "Format and stall memory are saved");
		Saved saved = save(helper, ship);
		saved.ship().remove("CrewFormat");
		saved.ship().remove("StallRefused");
		PirateShip old = PirateShips.SHIP.create(helper.getLevel(), EntitySpawnReason.LOAD);
		old.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved.ship()));
		helper.assertTrue(old.crewFormat() == PirateShip.LEGACY_CREW && old.helm().refused() == 0, "Missing fields read as legacy, no stall");
		helper.succeed();
	}

	// ------------------------------------------------------------------------------------------------- 14 November: decks, phase

	/**
	 * PirateDecks.standingOnDeck, the API for the Naval Cutlass's boarding bonus: a pig resting on the deck stands on it;
	 * one swimming beside the hull, one on a stone landing beside the ship, one on a block set on the deck, and one held in
	 * the air over the deck do not; of the crew, the captain at the wheel and a gunner do, the lookout in the main top does
	 * not.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 100, skyAccess = true)
	public void standingOnDeckMeansFeetOnTheDeck(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper, false);
		ship.setCrewed(PirateCrew.provision(level, ship) > 0);
		level.addFreshEntityWithPassengers(ship);
		// A stone landing beside the ship (a dock), its top above the water; and a block set on the deck.
		for (int y = FLOOR; y <= SURFACE + 1; y++) {
			helper.setBlock(new BlockPos(28, y, 22), Blocks.STONE);
		}
		helper.setBlock(new BlockPos(22, SURFACE + 1, 18), Blocks.STONE);
		Mob[] pigs = new Mob[5];
		helper.runAfterDelay(2, () -> {
			pigs[0] = helper.spawnWithNoFreeWill(EntityTypes.PIG, helper.relativeVec(ship.position().add(0.0, 2.5, 3.0)));
			pigs[1] = helper.spawnWithNoFreeWill(EntityTypes.PIG, helper.relativeVec(ship.position().add(-4.6, -0.3, 0.0)));
			pigs[2] = helper.spawnWithNoFreeWill(EntityTypes.PIG, new Vec3(28.5, SURFACE + 2.5, 22.5));
			pigs[3] = helper.spawnWithNoFreeWill(EntityTypes.PIG, new Vec3(22.5, SURFACE + 2.5, 18.5));
			pigs[4] = helper.spawnWithNoFreeWill(EntityTypes.PIG, helper.relativeVec(ship.position().add(0.0, 3.0, 5.0)));
			pigs[4].setNoGravity(true);
		});
		helper.runAfterDelay(60, () -> {
			helper.assertTrue(pigs[0].onGround() && PirateDecks.standingOnDeck(pigs[0]), "A pig resting on the deck stands on it, at "
					+ pigs[0].getY() + " over the deck at " + (ship.getY() + 1.0));
			helper.assertTrue(!PirateDecks.standingOnDeck(pigs[1]), "A pig in the water beside the hull does not");
			helper.assertTrue(pigs[2].onGround() && !PirateDecks.standingOnDeck(pigs[2]), "A pig on a landing beside the ship does not");
			helper.assertTrue(pigs[3].onGround() && !PirateDecks.standingOnDeck(pigs[3]), "A pig on a block set on the deck does not");
			helper.assertTrue(!PirateDecks.standingOnDeck(pigs[4]), "A pig in the air over the deck does not");
			for (OwnerPlunderer crew : crewOf(ship)) {
				boolean deckLevel = crew.shipStation() != 1;
				helper.assertTrue(PirateDecks.standingOnDeck(crew) == deckLevel,
						"Crew at station " + crew.shipStation() + (deckLevel ? " stand on the deck" : " (the main top) do not"));
			}
			for (Mob pig : pigs) {
				pig.discard();
			}
			for (Entity passenger : List.copyOf(ship.getPassengers())) {
				passenger.discard();
			}
			ship.discard();
			helper.succeed();
		});
	}

	/**
	 * The swell's phase is the world's game time plus an offset fixed by the ship's UUID (PirateMotion.phase): a saved and
	 * reloaded copy, whose own age starts again at nought, has the same phase and leans its crew's seats the same way at the
	 * same moment; another ship has its own offset.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 60, skyAccess = true)
	public void aReloadedShipKeepsItsSwellPhase(GameTestHelper helper) {
		pool(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper, false);
		level.addFreshEntity(ship);
		helper.runAfterDelay(40, () -> {
			PirateShip copy = copy(helper, ship);
			helper.assertTrue(ship.tickCount >= 40 && copy.tickCount == 0, "Setup: the copy is young, the ship is not");
			helper.assertTrue(copy.swellPhase(0.0F) == ship.swellPhase(0.0F), "The copy's phase is the ship's: " + copy.swellPhase(0.0F)
					+ " vs " + ship.swellPhase(0.0F));
			long expectedOffset = PirateMotion.offset(ship.getUUID().getMostSignificantBits(), ship.getUUID().getLeastSignificantBits());
			helper.assertTrue(ship.swellPhase(0.0F) == PirateMotion.phase(level.getGameTime(), expectedOffset, 0.0F),
					"The phase is the game time plus the UUID's offset");
			Vec3 seat = PirateCrew.STATIONS.get(0).seat();
			helper.assertTrue(copy.lean(seat).distanceTo(ship.lean(seat)) < 1.0E-9, "And so are the crew's seats");
			ship.discard();
			helper.succeed();
		});
	}
}
