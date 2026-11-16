package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel.Role;
import io.github.jimbozoomer.jugcraft.menagerie.OwnerPlunderer;
import io.github.jimbozoomer.jugcraft.menagerie.PirateCrew;
import io.github.jimbozoomer.jugcraft.menagerie.PirateDecks;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShip;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShipDeck;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShips;
import io.github.jimbozoomer.jugcraft.menagerie.PirateWaters;
import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.weapons.NavalCutlassItem;
import io.github.jimbozoomer.jugcraft.weapons.NavalCutlassStats;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * The Naval Cutlass's Boarding bonus on a pirate ship's deck. This test class joins the cutlass
 * (tools/thanksgiving_weapons.py, docs/features/thanksgiving-weapons.md) to the pirate ship
 * (docs/features/pirate-ships.md).
 *
 * <p>A pirate ship is not ridden: its boarders stand on its deck squares, so a boarder is neither in water nor in a boat.
 * The cutlass therefore asks {@link PirateDecks#standingOnDeck}. The bonus counts:
 * <ul>
 * <li>on the deck, for the wielder or for the foe;</li>
 * <li>for the ship's seated crew, who ride it, the lookout in the main top among them;</li>
 * <li>not on the shore beside the ship;</li>
 * <li>not on a block set on the deck;</li>
 * <li>not in the air above the deck;</li>
 * <li>and still, as before, in water and aboard a vanilla boat.</li>
 * </ul>
 *
 * <p>Every test floats a ship in PirateShipGameTests' pool, built in the empty drone_tower structure:
 * <ul>
 * <li>a stone floor at y 0 and water from y 1 to 4;</li>
 * <li>the ship at the pool's centre, its bow to +z, so its deck squares span x 19.5 to 24.5 and z 14 to 30, and their
 * tops are the deck, the ship's y plus 1.0 (5.875 here);</li>
 * <li>a stone "shore" beside the hull, at x 25 to 27 and z 20 to 25 with its top at y 6, an eighth of a block above the
 * deck and half a block off its edge.</li>
 * </ul>
 * Written for the server game tests; not run in Minecraft.
 */
public class CutlassDeckGameTests {
	private static final String POOL_STRUCTURE = "jugcraft-test:drone_tower";
	private static final int POOL_MIN = 2, POOL_MAX = 41, FLOOR = 0, SURFACE = 4;
	private static final Vec3 CENTRE = new Vec3(22.0, SURFACE + PirateWaters.WATERLINE, 22.0);
	/** The shore's blocks: x 25 and 26 (the deck's edge is x 24.5), z 20 to 24, from the floor up to y 5 (top y 6). */
	private static final int SHORE_X0 = 25, SHORE_X1 = 26, SHORE_Z0 = 20, SHORE_Z1 = 24, SHORE_TOP = 5;
	/** Where a fighter stands on the shore (relative), beside the deck's edge at x 24.5: its box ends 0.7 off it. */
	private static final Vec3 SHORE = new Vec3(25.5, SHORE_TOP + 1.0, 21.0);
	private static final Vec3 SHORE_2 = new Vec3(25.5, SHORE_TOP + 1.0, 23.5);
	/** A fighter has settled on a floor once it is on the ground within this of the floor's height. */
	private static final double SETTLED = 0.01;
	/** A blow's damage before the bonus (the bonus does not depend on it). */
	private static final float BLOW = 5.0F;

	/**
	 * On the deck: a wielder on the deck gets the bonus against a foe on the shore. A wielder on the shore gets it
	 * against a foe on the deck. Two on the deck get it once (the bonus is "wielder or foe", never doubled).
	 */
	@GameTest(structure = POOL_STRUCTURE, maxTicks = 120, skyAccess = true)
	public void boardingCountsOnThePirateDeck(GameTestHelper helper) {
		pool(helper);
		shore(helper);
		PirateShip ship = ship(helper);
		helper.getLevel().addFreshEntity(ship);
		Mob[] fighters = new Mob[3];
		Runnable board = () -> {
			helper.assertTrue(ship.decks().size() == PirateShipDeck.KEEL.length, "The ship has not laid its deck squares");
			fighters[0] = fighter(helper, aboard(-2.0, 0.25), false);
			fighters[1] = fighter(helper, aboard(2.0, 0.25), false);
			fighters[2] = fighter(helper, SHORE.add(0.0, 0.25, 0.0), false);
		};
		helper.runAfterDelay(2, board);
		helper.succeedWhen(() -> {
			helper.assertTrue(fighters[2] != null, "The fighters are not out yet");
			Mob boarder = fighters[0], mate = fighters[1], ashore = fighters[2];
			double deck = ship.getY() + 1.0;
			helper.assertTrue(settled(boarder, deck) && settled(mate, deck), "The boarders have not settled on the deck at "
					+ deck + " (at " + boarder.getY() + " and " + mate.getY() + ")");
			helper.assertTrue(settled(ashore, helper.absoluteVec(SHORE).y), "The shore fighter has not settled on the shore");
			helper.assertTrue(PirateDecks.standingOnDeck(boarder) && NavalCutlassItem.afloat(boarder),
					"A fighter standing on the deck is not afloat");
			helper.assertTrue(!PirateDecks.standingOnDeck(ashore) && !NavalCutlassItem.afloat(ashore),
					"A fighter on the shore beside the ship is afloat");
			bonus(helper, boarder, ashore, NavalCutlassStats.BOARDING, "a wielder on the deck striking a foe on the shore");
			bonus(helper, ashore, mate, NavalCutlassStats.BOARDING, "a wielder on the shore striking a foe on the deck");
			bonus(helper, boarder, mate, NavalCutlassStats.BOARDING, "a wielder and a foe both on the deck");
			clear(ship, fighters);
		});
	}

	/**
	 * Beside or above the deck nothing is added. The foe stands on the shore throughout, and the wielder is in turn:
	 * <ul>
	 * <li>on the shore beside the hull;</li>
	 * <li>on a stone block set on the deck (an eighth of a block above it);</li>
	 * <li>hanging a block and a half above the deck;</li>
	 * <li>high in the air ten blocks above it.</li>
	 * </ul>
	 * A foe on that block, struck from the shore, adds nothing either.
	 */
	@GameTest(structure = POOL_STRUCTURE, maxTicks = 120, skyAccess = true)
	public void boardingDoesNotCountBesideOrAboveTheDeck(GameTestHelper helper) {
		pool(helper);
		shore(helper);
		PirateShip ship = ship(helper);
		helper.getLevel().addFreshEntity(ship);
		// A stone block set on the deck, over the forward squares: y 5 to 6, its top an eighth of a block above the deck.
		BlockPos onDeck = BlockPos.containing(CENTRE.x, SHORE_TOP, CENTRE.z + 4.0);
		helper.setBlock(onDeck, Blocks.STONE);
		Mob[] fighters = new Mob[5];
		Runnable stage = () -> {
			helper.assertTrue(ship.decks().size() == PirateShipDeck.KEEL.length, "The ship has not laid its deck squares");
			fighters[0] = fighter(helper, SHORE.add(0.0, 0.25, 0.0), false);
			fighters[1] = fighter(helper, SHORE_2.add(0.0, 0.25, 0.0), false);
			fighters[2] = fighter(helper, Vec3.atBottomCenterOf(onDeck.above()).add(0.0, 0.25, 0.0), false);
			fighters[3] = fighter(helper, aboard(-5.5, 1.5), true);
			fighters[4] = fighter(helper, aboard(-2.0, 10.0), true);
		};
		helper.runAfterDelay(2, stage);
		helper.succeedWhen(() -> {
			helper.assertTrue(fighters[4] != null, "The fighters are not out yet");
			Mob foe = fighters[0], ashore = fighters[1], onBlock = fighters[2], hanging = fighters[3], high = fighters[4];
			double deck = ship.getY() + 1.0;
			double shoreTop = helper.absoluteVec(SHORE).y;
			helper.assertTrue(settled(foe, shoreTop) && settled(ashore, shoreTop), "The shore fighters have not settled");
			helper.assertTrue(settled(onBlock, helper.absolutePos(onDeck).getY() + 1.0), "The fighter on the block has not settled on it");
			helper.assertTrue(Math.abs(onBlock.getY() - deck - 0.125) < SETTLED, "The block's top should be an eighth above the deck at "
					+ deck + ", not at " + onBlock.getY());
			helper.assertTrue(!hanging.onGround() && Math.abs(hanging.getY() - deck - 1.5) < SETTLED, "The hanging fighter moved to " + hanging.getY());
			helper.assertTrue(!high.onGround() && Math.abs(high.getY() - deck - 10.0) < SETTLED, "The high fighter moved to " + high.getY());
			for (Mob nowhere : new Mob[] {ashore, onBlock, hanging, high}) {
				helper.assertTrue(!PirateDecks.standingOnDeck(nowhere) && !NavalCutlassItem.afloat(nowhere),
						"A fighter at " + nowhere.position() + ", off the deck, is afloat");
			}
			bonus(helper, ashore, foe, 0.0F, "a wielder on the shore beside the ship");
			bonus(helper, onBlock, foe, 0.0F, "a wielder on a block set on the deck");
			bonus(helper, hanging, foe, 0.0F, "a wielder in the air a block and a half above the deck");
			bonus(helper, high, foe, 0.0F, "a wielder high above the deck");
			bonus(helper, foe, onBlock, 0.0F, "a foe on a block set on the deck");
			helper.setBlock(onDeck, Blocks.AIR);
			clear(ship, fighters);
		});
	}

	/**
	 * Beside the ship, the cutlass's earlier support is unchanged. A wielder standing in the pool's water gets the
	 * bonus, and so does a wielder seated in a vanilla oak boat on it; the foe is on the shore. These go through the
	 * water and boat checks, which come before the deck is asked.
	 */
	@GameTest(structure = POOL_STRUCTURE, maxTicks = 120, skyAccess = true)
	public void boardingStillCountsInWaterAndInABoat(GameTestHelper helper) {
		pool(helper);
		shore(helper);
		PirateShip ship = ship(helper);
		ServerLevel level = helper.getLevel();
		level.addFreshEntity(ship);
		@SuppressWarnings("unchecked")
		EntityType<Entity> boatType = (EntityType<Entity>) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("oak_boat"));
		Entity boat = boatType.create(level, EntitySpawnReason.TRIGGERED);
		helper.assertTrue(boat != null, "No boat");
		boat.snapTo(helper.absoluteVec(new Vec3(31.5, SURFACE + 1.0, 22.5)));
		level.addFreshEntity(boat);
		Mob[] fighters = new Mob[3];
		Runnable stage = () -> {
			fighters[0] = fighter(helper, SHORE.add(0.0, 0.25, 0.0), false);
			fighters[1] = fighter(helper, new Vec3(34.5, FLOOR + 1.0, 30.5), false);
			fighters[2] = fighter(helper, new Vec3(31.5, SURFACE + 1.5, 26.5), false);
			fighters[2].startRiding(boat);
		};
		helper.runAfterDelay(2, stage);
		helper.succeedWhen(() -> {
			helper.assertTrue(fighters[2] != null, "The fighters are not out yet");
			Mob foe = fighters[0], swimmer = fighters[1], rower = fighters[2];
			helper.assertTrue(settled(foe, helper.absoluteVec(SHORE).y), "The shore fighter has not settled");
			helper.assertTrue(swimmer.isInWater(), "The wielder in the pool is not in the water");
			helper.assertTrue(rower.getVehicle() == boat, "The wielder did not board the oak boat");
			bonus(helper, swimmer, foe, NavalCutlassStats.BOARDING, "a wielder in water beside the ship");
			bonus(helper, rower, foe, NavalCutlassStats.BOARDING, "a wielder aboard a vanilla boat beside the ship");
			bonus(helper, foe, swimmer, NavalCutlassStats.BOARDING, "a foe in water beside the ship");
			rower.stopRiding();
			boat.discard();
			clear(ship, fighters);
		});
	}

	/**
	 * Seated crew are aboard. A ship crewed by PirateCrew.provision seats one plunderer at each station: the helm, the
	 * lookout in the main top, the guns and the deck. Each counts as afloat, and as a foe struck from the shore each
	 * gives the bonus. The lookout is the case only riding the ship covers: in the main top it is not standing on the
	 * deck.
	 */
	@GameTest(structure = POOL_STRUCTURE, maxTicks = 120, skyAccess = true)
	public void boardingCountsForTheSeatedCrew(GameTestHelper helper) {
		pool(helper);
		shore(helper);
		ServerLevel level = helper.getLevel();
		PirateShip ship = ship(helper);
		int seated = PirateCrew.provision(level, ship);
		ship.setCrewed(seated > 0);
		level.addFreshEntityWithPassengers(ship);
		helper.assertTrue(seated == PirateCrew.STATIONS.size() && ship.getPassengers().size() == seated,
				"Provisioning should seat a crew member at each of the " + PirateCrew.STATIONS.size() + " stations, not " + seated);
		List<OwnerPlunderer> crew = new ArrayList<>();
		for (Entity passenger : ship.getPassengers()) {
			crew.add((OwnerPlunderer) passenger);
		}
		Mob[] ashore = {null};
		Runnable stage = () -> ashore[0] = fighter(helper, SHORE.add(0.0, 0.25, 0.0), false);
		helper.runAfterDelay(2, stage);
		helper.succeedWhen(() -> {
			helper.assertTrue(ashore[0] != null && settled(ashore[0], helper.absoluteVec(SHORE).y), "The shore fighter has not settled");
			OwnerPlunderer lookout = null;
			for (OwnerPlunderer member : crew) {
				helper.assertTrue(member.getVehicle() == ship, "The crew member at station " + member.shipStation() + " is not seated");
				helper.assertTrue(NavalCutlassItem.afloat(member), "The crew member seated at station " + member.shipStation() + " is not afloat");
				bonus(helper, ashore[0], member, NavalCutlassStats.BOARDING, "a foe seated at station " + member.shipStation());
				if (PirateCrew.STATIONS.get(member.shipStation()).role() == Role.LOOKOUT) {
					lookout = member;
				}
			}
			helper.assertTrue(lookout != null && !PirateDecks.standingOnDeck(lookout),
					"The lookout should be seated in the main top, not standing on the deck");
			for (OwnerPlunderer member : crew) {
				member.discard();
			}
			ashore[0].discard();
			ship.discard();
		});
	}

	/** PirateShipGameTests' pool: stone floor at y 0, water y 1 to 4, x and z 2 to 41. */
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

	/** The stone shore beside the hull, from the floor up to y 5 (its top y 6). */
	private static void shore(GameTestHelper helper) {
		for (int x = SHORE_X0; x <= SHORE_X1; x++) {
			for (int z = SHORE_Z0; z <= SHORE_Z1; z++) {
				for (int y = FLOOR + 1; y <= SHORE_TOP; y++) {
					helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
				}
			}
		}
	}

	/** A ship at the pool's centre (absolute), facing the +z bow, anchored there and not natural, not yet added. */
	private static PirateShip ship(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Vec3 at = helper.absoluteVec(CENTRE);
		if (Town.isProtected(level, BlockPos.containing(at))) {
			helper.fail("The test pool lies inside the town's protection; move the test structure");
		}
		PirateShip ship = PirateShips.SHIP.create(level, EntitySpawnReason.EVENT);
		helper.assertTrue(ship != null, "The pirate ship type should create a ship");
		ship.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		ship.launch(at, false);
		return ship;
	}

	/** A place over the deck (relative), {@code keel} blocks along the keel from the ship's middle, {@code up} above the deck. */
	private static Vec3 aboard(double keel, double up) {
		return new Vec3(CENTRE.x, CENTRE.y + 1.0 + up, CENTRE.z + keel);
	}

	/** A still husk with a cutlass (it does not burn by day; it neither walks nor swims); hung in place if asked. */
	private static Mob fighter(GameTestHelper helper, Vec3 at, boolean hung) {
		@SuppressWarnings("unchecked")
		EntityType<Mob> type = (EntityType<Mob>) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("husk"));
		Mob husk = helper.spawnWithNoFreeWill(type, at);
		husk.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(NavalCutlassItem.CUTLASS));
		if (hung) {
			husk.setNoGravity(true);
			husk.setDeltaMovement(Vec3.ZERO);
		}
		return husk;
	}

	/** On the ground, at {@code floor} (absolute). */
	private static boolean settled(Mob mob, double floor) {
		return mob.onGround() && Math.abs(mob.getY() - floor) < SETTLED;
	}

	/** The cutlass's bonus for a blow by {@code wielder} on {@code foe} is {@code expected}. */
	private static void bonus(GameTestHelper helper, Mob wielder, Entity foe, float expected, String what) {
		float bonus = NavalCutlassItem.CUTLASS.getAttackDamageBonus(foe, BLOW, helper.getLevel().damageSources().mobAttack(wielder));
		helper.assertTrue(bonus == expected, "For " + what + " the cutlass adds " + bonus + ", not " + expected);
	}

	private static void clear(PirateShip ship, Mob[] fighters) {
		for (Mob fighter : fighters) {
			fighter.discard();
		}
		ship.discard();
	}
}
