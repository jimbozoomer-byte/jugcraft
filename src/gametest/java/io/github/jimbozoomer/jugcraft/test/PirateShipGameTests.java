package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.menagerie.OwnerCannonball;
import io.github.jimbozoomer.jugcraft.menagerie.OwnerPlunderer;
import io.github.jimbozoomer.jugcraft.menagerie.PirateCrew;
import io.github.jimbozoomer.jugcraft.menagerie.PirateSeas;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShip;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShipDeck;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShipTesting;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShips;
import io.github.jimbozoomer.jugcraft.menagerie.PirateWaters;
import io.github.jimbozoomer.jugcraft.town.Town;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
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
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
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
			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			player.setGameMode(GameType.SURVIVAL);
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
}
