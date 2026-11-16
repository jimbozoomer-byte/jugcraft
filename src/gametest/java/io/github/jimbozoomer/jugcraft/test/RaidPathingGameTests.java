package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.raiders.CityRaidApproach;
import io.github.jimbozoomer.jugcraft.raiders.CityRaidRoute;
import io.github.jimbozoomer.jugcraft.raiders.JugcraftRaiders;
import io.github.jimbozoomer.jugcraft.raiders.RaidEngagement;
import io.github.jimbozoomer.jugcraft.raiders.Raider;
import io.github.jimbozoomer.jugcraft.raiders.RaiderBlimp;
import io.github.jimbozoomer.jugcraft.raiders.RaiderInfantry;
import io.github.jimbozoomer.jugcraft.raiders.RaiderRaids;
import io.github.jimbozoomer.jugcraft.raiders.RaiderWalker;
import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.town.CityGuardVantage;
import io.github.jimbozoomer.jugcraft.town.JugcraftTown;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.TownDefense;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Raid pathing and visible fights (docs/features/raiders.md, "City approaches and visible fights"), case by case on a
 * stone floor: a player's wall in the way, a straight wall with no gate and a guard on top, a gate, a guard nobody can
 * walk up to, a wall corner, a guard who must move to see the raider, every kind of raider under the ground rules, a
 * blimp with a tower in its way, a march that can never arrive, and the town's own approaches.
 *
 * <p>Each case's raiders belong to a real raid (its own party sent away at once), since only raiders of a raid follow
 * the raid rules; a summoned raider fights as it always did. During the Thanksgiving truce no raid starts and the
 * cases that need one pass at once.
 */
public class RaidPathingGameTests {
	private static final String ARENA = "jugcraft-test:drone_tower";

	// ---------------------------------------------------------------- cases

	/** A player's wall across the way: the marcher walks round its end and arrives, without a ladder or a stop. */
	@GameTest(structure = ARENA, padding = 64, maxTicks = 600, skyAccess = true)
	public void aWallInTheWayIsWalkedRound(GameTestHelper h) {
		floor(h);
		wall(h, 10, 20, 33, 20, 3);
		BlockPos objective = new BlockPos(22, 1, 6);
		RaiderRaids.Raid raid = raid(h, objective);
		if (raid == null) {
			return;
		}
		RaiderInfantry officer = join(h, JugcraftRaiders.OFFICER, new Vec3(22.5, 1, 34.5), raid, objective);
		h.succeedWhen(() -> {
			double away = officer.position().distanceTo(Vec3.atBottomCenterOf(h.absolutePos(objective)));
			h.assertTrue(away < 4.0, "The officer should walk round the wall to its objective, but is " + away + " away at "
					+ rel(h, officer.position()));
			h.assertFalse(officer.engagement().holding(), "and should not have given its march up");
			cleanUp(h, raid, officer);
		});
	}

	/**
	 * A straight wall with no gate and a guard on top of it, in sight: no raider on the ground takes the guard on, so
	 * none goes to stand under it at the foot of the wall. The guard is genuinely in their sight (checked first).
	 */
	@GameTest(structure = ARENA, padding = 64, maxTicks = 340, skyAccess = true)
	public void aGuardOnAStraightWallDrawsNobodyToItsFoot(GameTestHelper h) {
		floor(h);
		wall(h, 0, 20, 43, 20, 6);
		Townsfolk guard = CityDefenseGameTests.guard(h, new Vec3(22.5, 7, 20.5));
		BlockPos objective = new BlockPos(22, 1, 37);
		RaiderRaids.Raid raid = raid(h, objective);
		if (raid == null) {
			guard.discard();
			return;
		}
		List<Mob> raiders = List.of(
				join(h, JugcraftRaiders.GRUNT, new Vec3(22.5, 1, 32.5), raid, objective),
				join(h, JugcraftRaiders.GUNNER, new Vec3(17.5, 1, 32.5), raid, objective),
				join(h, JugcraftRaiders.GRENADIER, new Vec3(27.5, 1, 32.5), raid, objective),
				join(h, JugcraftRaiders.OFFICER, new Vec3(20.5, 1, 34.5), raid, objective),
				join(h, JugcraftRaiders.WALKER, new Vec3(31.5, 1, 35.5), raid, objective));
		h.assertTrue(raiders.get(1).hasLineOfSight(guard), "The guard on the wall should be in the gunner's sight");
		for (Mob raider : raiders) {
			h.assertFalse(raider.canAttack(guard), raider.getType() + " should leave a guard high on a wall alone");
		}
		boolean[] targeted = {false};
		int[] atTheFoot = {0};
		h.onEachTick(() -> {
			for (Mob raider : raiders) {
				targeted[0] |= raider.getTarget() == guard;
				if (raider.isAlive() && raider.getZ() < h.absoluteVec(new Vec3(0, 0, 23.0)).z) {
					atTheFoot[0]++;
				}
			}
		});
		h.runAfterDelay(300, () -> {
			h.assertFalse(targeted[0], "No raider on the ground should take on the guard on the wall");
			h.assertTrue(atTheFoot[0] == 0, "No raider should stand at the foot of the wall under the guard (" + atTheFoot[0] + " raider-ticks)");
			guard.discard();
			done(h, raid, raiders.toArray(new Mob[0]));
		});
	}

	/** A gate in the wall: the raiders come through it to the guard behind, the grunt to striking distance, the gunner to fire. */
	@GameTest(structure = ARENA, padding = 64, maxTicks = 500, skyAccess = true)
	public void raidersComeThroughAGateToTheGuard(GameTestHelper h) {
		floor(h);
		wall(h, 0, 20, 19, 20, 6);
		wall(h, 24, 20, 43, 20, 6);
		Townsfolk guard = CityDefenseGameTests.guard(h, new Vec3(21.5, 1, 10.5));
		BlockPos objective = new BlockPos(21, 1, 6);
		RaiderRaids.Raid raid = raid(h, objective);
		if (raid == null) {
			guard.discard();
			return;
		}
		RaiderInfantry grunt = join(h, JugcraftRaiders.GRUNT, new Vec3(21.5, 1, 34.5), raid, objective);
		RaiderInfantry gunner = join(h, JugcraftRaiders.GUNNER, new Vec3(15.5, 1, 34.5), raid, objective);
		boolean[] closed = {false};
		boolean[] fired = {false};
		h.onEachTick(() -> {
			closed[0] |= grunt.getTarget() == guard && grunt.distanceTo(guard) < 2.5F;
			fired[0] |= gunner.getTarget() == guard && gunner.engagement().engaging(guard);
		});
		h.succeedWhen(() -> {
			h.assertTrue(closed[0], "The grunt should come through the gate to strike the guard; it is " + grunt.distanceTo(guard) + " away at "
					+ rel(h, grunt.position()));
			h.assertTrue(fired[0], "The gunner should come within reach and sight of the guard; it hunts " + gunner.getTarget());
			guard.discard();
			cleanUp(h, raid, grunt, gunner);
		});
	}

	/**
	 * A guard no raider can walk up to (behind a ring of fences it can be seen over): every raider either fights it from
	 * where it can (in reach and in sight) or gives it up; none keeps it for nothing much longer than the patience allows,
	 * and the melee officer never takes it on at all.
	 */
	@GameTest(structure = ARENA, padding = 64, maxTicks = 560, skyAccess = true)
	public void anUnreachableGuardIsFoughtFromSightOrGivenUp(GameTestHelper h) {
		floor(h);
		for (int x = 19; x <= 25; x++) {
			for (int z = 7; z <= 13; z++) {
				if (x == 19 || x == 25 || z == 7 || z == 13) {
					h.setBlock(new BlockPos(x, 1, z), Blocks.OAK_FENCE);
				}
			}
		}
		Townsfolk guard = CityDefenseGameTests.guard(h, new Vec3(22.5, 1, 10.5));
		BlockPos objective = new BlockPos(22, 1, 38);
		RaiderRaids.Raid raid = raid(h, objective);
		if (raid == null) {
			guard.discard();
			return;
		}
		RaiderInfantry gunner = join(h, JugcraftRaiders.GUNNER, new Vec3(16.5, 1, 30.5), raid, objective);
		RaiderInfantry grenadier = join(h, JugcraftRaiders.GRENADIER, new Vec3(28.5, 1, 30.5), raid, objective);
		RaiderInfantry officer = join(h, JugcraftRaiders.OFFICER, new Vec3(20.5, 1, 32.5), raid, objective);
		RaiderWalker walker = join(h, JugcraftRaiders.WALKER, new Vec3(23.5, 1, 35.5), raid, objective);
		List<Mob> raiders = List.of(gunner, grenadier, officer, walker);
		int[] idle = new int[raiders.size()];
		int[] worst = {0};
		boolean[] officerTook = {false};
		h.onEachTick(() -> {
			if (!officerTook[0] && officer.getTarget() == guard) {
				var path = officer.getNavigation().createPath(guard, 0);
				io.github.jimbozoomer.jugcraft.Jugcraft.LOGGER.info("[raid-fence-detail] officer={} guard={} pathReach={} pathEnd={} tick={}",
						rel(h, officer.position()), rel(h, guard.position()), path != null && path.canReach(), path == null ? "none" : path.getEndNode(), h.getTick());
			}
			officerTook[0] |= officer.getTarget() == guard;
			for (int i = 0; i < raiders.size(); i++) {
				Mob raider = raiders.get(i);
				LivingEntity target = raider.getTarget();
				idle[i] = target != null && !((Raider) raider).engagement().engaging(target) ? idle[i] + 1 : 0;
				worst[0] = Math.max(worst[0], idle[i]);
			}
		});
		h.runAfterDelay(520, () -> {
			h.assertFalse(officerTook[0], "The officer cannot reach the guard, so should never take it on");
			// The watch looks every WATCH_TICKS; a little more for a standoff walk that is itself bounded.
			int bound = RaidEngagement.PATIENCE + 3 * 10 + 40;
			h.assertTrue(worst[0] <= bound, "A raider kept a guard it could neither reach nor shoot for " + worst[0] + " ticks (bound " + bound + ")");
			guard.discard();
			done(h, raid, raiders.toArray(new Mob[0]));
		});
	}

	/**
	 * A wall corner with a guard on the corner tower and the objective inside the angle: the raiders go round the end of
	 * the wall rather than piling up at the corner, and none takes on the tower's guard.
	 */
	@GameTest(structure = ARENA, padding = 64, maxTicks = 620, skyAccess = true)
	public void raidersDoNotPileUpAtAWallCorner(GameTestHelper h) {
		floor(h);
		wall(h, 22, 20, 30, 20, 6);
		wall(h, 30, 12, 30, 20, 6);
		h.setBlock(new BlockPos(30, 7, 20), Blocks.STONE_BRICKS);
		Townsfolk guard = CityDefenseGameTests.guard(h, new Vec3(30.5, 8, 20.5));
		BlockPos objective = new BlockPos(25, 1, 15);
		RaiderRaids.Raid raid = raid(h, objective);
		if (raid == null) {
			guard.discard();
			return;
		}
		List<Mob> raiders = List.of(
				join(h, JugcraftRaiders.OFFICER, new Vec3(36.5, 1, 26.5), raid, objective),
				join(h, JugcraftRaiders.GUNNER, new Vec3(38.5, 1, 26.5), raid, objective),
				join(h, JugcraftRaiders.GRENADIER, new Vec3(36.5, 1, 28.5), raid, objective),
				join(h, JugcraftRaiders.GUNNER, new Vec3(38.5, 1, 28.5), raid, objective));
		boolean[] targeted = {false};
		h.onEachTick(() -> raiders.forEach(r -> targeted[0] |= r.getTarget() == guard));
		Vec3 apex = h.absoluteVec(new Vec3(31.5, 1, 21.5));
		h.runAfterDelay(580, () -> {
			int piled = 0;
			int arrived = 0;
			for (Mob raider : raiders) {
				if (raider.position().subtract(apex).horizontalDistance() < 2.5) {
					piled++;
				}
				if (raider.position().distanceTo(Vec3.atBottomCenterOf(h.absolutePos(objective))) < 6.0) {
					arrived++;
				}
			}
			h.assertFalse(targeted[0], "No raider should take on the guard on the corner tower");
			h.assertTrue(piled <= 1, piled + " raiders are piled up at the wall's corner");
			h.assertTrue(arrived >= 2, "The raiders should go round the wall to their objective; " + arrived + " of 4 did");
			guard.discard();
			done(h, raid, raiders.toArray(new Mob[0]));
		});
	}

	/**
	 * Guard repositioning: a guard behind a wall cannot see the raider beyond it; with the watch (CityGuardVantage) it goes
	 * through the gap in the wall to where it can, and takes the raider on.
	 */
	@GameTest(structure = ARENA, padding = 64, maxTicks = 640, skyAccess = true)
	public void aGuardMovesToWhereItCanSeeTheRaider(GameTestHelper h) {
		floor(h);
		wall(h, 20, 0, 20, 29, 5);
		wall(h, 20, 33, 20, 43, 5);
		Townsfolk guard = h.spawn(JugcraftTown.TOWNSFOLK, new Vec3(14.5, 1, 12.5));
		guard.setUp(new TownData.Spot(-1, "guard", null, "Watch Guard", "guard_blue", BlockPos.ZERO), guard.blockPosition());
		// The guard answers the raiders a raid brought (its party), so keep one of the party: the officer, standing still.
		ServerLevel world = h.getLevel();
		BlockPos objective = h.absolutePos(new BlockPos(14, 1, 14));
		RaiderRaids.Raid raid = RaiderRaids.start(world, objective, h.absolutePos(new BlockPos(28, 1, 12)), 1, RandomSource.create(12));
		if (raid == null) {
			h.assertTrue(JugcraftSeasons.thanksgivingTruce(), "A raid starts outside the Thanksgiving truce");
			guard.discard();
			h.succeed();
			return;
		}
		RaiderInfantry raider = null;
		for (UUID id : raid.members()) {
			if (world.getEntity(id) instanceof RaiderInfantry member && member.role() == RaiderInfantry.Role.OFFICER && raider == null) {
				raider = member;
			} else if (world.getEntity(id) != null) {
				world.getEntity(id).discard();
			}
		}
		h.assertTrue(raider != null, "The raid brought its officer");
		raider.setNoAi(true);
		Vec3 post = h.absoluteVec(new Vec3(28.5, 1, 12.5));
		raider.teleportTo(post.x, post.y, post.z);
		RaiderInfantry officer = raider;
		CityGuardVantage.WatchGoal watch = CityGuardVantage.install(guard);
		h.assertTrue(watch != null && CityGuardVantage.install(guard) == watch, "A guard takes the watch once");
		ServerLevel level = h.getLevel();
		h.assertFalse(TownDefense.visible(level, guard, guard.getEyePosition(), officer.getBoundingBox().getCenter()),
				"At first the wall hides the raider from the guard");
		h.succeedWhen(() -> {
			boolean sees = TownDefense.visible(level, guard, guard.getEyePosition(), officer.getBoundingBox().getCenter());
			h.assertTrue(sees || guard.getTarget() == officer, "The guard should move to see the raider; it is at "
					+ rel(h, guard.position()) + ", making for " + watch.vantage() + ", having set aside " + watch.setAside());
			h.assertTrue(watch.moves() > 0, "It got there by its watch");
			h.assertTrue(guard.getHealth() == guard.getMaxHealth(), "and is still unhurt (the town's immunity)");
			guard.discard();
			cleanUp(h, raid, officer);
		});
	}

	/**
	 * Every kind of raider under the ground rules: the ground kinds leave a tower gun's crew and a guard high on a pillar
	 * alone but take on a guard on the ground; a blimp may take on any of them; a summoned raider of no raid fights as it
	 * always did; a guard given up is left alone, unless it comes within striking distance.
	 */
	@GameTest(structure = ARENA, padding = 64, maxTicks = 40, skyAccess = true)
	public void everyRaiderKindFightsByTheGroundRules(GameTestHelper h) {
		floor(h);
		for (int y = 1; y <= 6; y++) {
			h.setBlock(new BlockPos(34, y, 8), Blocks.STONE);
		}
		ServerLevel level = h.getLevel();
		Townsfolk ground = CityDefenseGameTests.guard(h, new Vec3(8.5, 1, 8.5));
		Townsfolk high = CityDefenseGameTests.guard(h, new Vec3(34.5, 7, 8.5));
		Townsfolk crew = CityDefenseGameTests.guard(h, new Vec3(20.5, 1, 36.5));
		crew.setUp(new TownData.Spot(9998, "flak_guard", null, "Crew", "guard_blue", BlockPos.ZERO), crew.blockPosition());
		TownDefense.crew(level, crew);
		h.assertTrue(crew.getVehicle() != null, "The crew mans its gun");
		BlockPos objective = new BlockPos(20, 1, 20);
		RaiderRaids.Raid raid = raid(h, objective);
		if (raid == null) {
			discard(ground, high, crew, crew.getVehicle());
			return;
		}
		List<EntityType<? extends Mob>> kinds = List.of(JugcraftRaiders.GRUNT, JugcraftRaiders.GRENADIER, JugcraftRaiders.OFFICER,
				JugcraftRaiders.GUNNER, JugcraftRaiders.WALKER, JugcraftRaiders.BLIMP);
		List<Mob> made = new ArrayList<>();
		for (EntityType<? extends Mob> kind : kinds) {
			Mob member = join(h, kind, new Vec3(20.5, kind == JugcraftRaiders.BLIMP ? 12 : 1, 20.5), raid, objective);
			Mob loner = h.spawn(kind, new Vec3(24.5, kind == JugcraftRaiders.BLIMP ? 12 : 1, 24.5));
			made.add(member);
			made.add(loner);
			RaidEngagement engagement = ((Raider) member).engagement();
			String name = kind.toString();
			h.assertTrue(engagement.reach() > 0, name + " has a reach");
			h.assertTrue(member.canAttack(ground), name + " of a raid takes on a guard on the ground");
			if (kind == JugcraftRaiders.BLIMP) {
				h.assertTrue(member.canAttack(crew) && member.canAttack(high), "A blimp of a raid may take on the gun crews and the guards up high");
			} else {
				h.assertFalse(member.canAttack(crew), name + " of a raid leaves a tower gun's crew to the blimps");
				h.assertFalse(member.canAttack(high), name + " of a raid leaves a guard high on a pillar alone");
			}
			h.assertTrue(loner.canAttack(crew), name + " of no raid fights as it always did");
			// A guard given up is left alone, but fought within striking distance.
			engagement.giveUp(ground);
			h.assertTrue(engagement.ignoring(ground) && !member.canAttack(ground), name + " leaves a guard it gave up alone");
			h.assertTrue(engagement.targetsGivenUp() == 1, name + " counts what it gave up");
			Vec3 home = ground.position();
			ground.teleportTo(member.getX() + 1.0, member.getY(), member.getZ());
			h.assertTrue(member.canAttack(ground), name + " fights a guard it gave up once it is within striking distance");
			ground.teleportTo(home.x, home.y, home.z);
		}
		h.assertTrue(((Raider) made.get(8)).engagement().reach() == JugcraftRaiders.WALKER_LAUNCH_MAX, "The walker reaches as far as its launcher");
		h.assertTrue(((Raider) made.get(2)).engagement().reach() == JugcraftRaiders.GRENADE_MAX_RANGE, "The grenadier as far as it throws");
		discard(ground, high, crew.getVehicle(), crew);
		done(h, raid, made.toArray(new Mob[0]));
	}

	/** A blimp with a tower between it and the guard it hunts rises over the tower and comes overhead. */
	@GameTest(structure = ARENA, padding = 64, maxTicks = 700, skyAccess = true)
	public void aBlimpRisesOverATowerInItsWay(GameTestHelper h) {
		floor(h);
		for (int x = 20; x <= 23; x++) {
			for (int z = 19; z <= 22; z++) {
				for (int y = 1; y <= 20; y++) {
					h.setBlock(new BlockPos(x, y, z), Blocks.STONE);
				}
			}
		}
		Townsfolk guard = CityDefenseGameTests.guard(h, new Vec3(21.5, 1, 6.5));
		BlockPos objective = new BlockPos(21, 1, 6);
		RaiderRaids.Raid raid = raid(h, objective);
		if (raid == null) {
			guard.discard();
			return;
		}
		RaiderBlimp blimp = join(h, JugcraftRaiders.BLIMP, new Vec3(21.5, 8, 36.5), raid, objective);
		double[] overTower = {0};
		h.onEachTick(() -> {
			// Keep it on this case's guard (another case's guard may be nearer), unless it has given the guard up.
			if (blimp.getTarget() != guard && !blimp.engagement().ignoring(guard)) {
				blimp.setTarget(guard);
			}
			Vec3 at = rel(h, blimp.position());
			if (at.x > 19 && at.x < 25 && at.z > 18 && at.z < 24) {
				overTower[0] = Math.max(overTower[0], at.y);
			}
		});
		h.succeedWhen(() -> {
			double dx = blimp.getX() - guard.getX();
			double dz = blimp.getZ() - guard.getZ();
			h.assertTrue(dx * dx + dz * dz <= 16.0, "The blimp should come over the guard; it is at " + rel(h, blimp.position()));
			h.assertTrue(overTower[0] == 0 || overTower[0] > 21, "Over the tower it should fly above it, not through it: " + overTower[0]);
			h.assertFalse(blimp.engagement().ignoring(guard), "It got there without giving the guard up");
			guard.discard();
			cleanUp(h, raid, blimp);
		});
	}

	/**
	 * A march that can never arrive (the objective walled in on every side): the marcher gives its route up, plans afresh
	 * a bounded number of times, then holds where it is for good rather than trying for ever.
	 */
	@GameTest(structure = ARENA, padding = 64, maxTicks = 3600, skyAccess = true)
	public void aMarchThatCannotArriveEndsHolding(GameTestHelper h) {
		floor(h);
		// Recovery deliberately exceeds the no-observer withdrawal timeout. Keep
		// a real nearby observer so the case tests marching, not raid abandonment.
		var observer = TestPlayers.survivor(h);
		observer.setPos(h.absoluteVec(new Vec3(4.5, 1, 36.5)));
		h.getLevel().getChunkSource().move(observer);
		wall(h, 14, 8, 30, 8, 5);
		wall(h, 14, 24, 30, 24, 5);
		wall(h, 14, 8, 14, 24, 5);
		wall(h, 30, 8, 30, 24, 5);
		for (int x = 14; x <= 30; x++) {
			for (int z = 8; z <= 24; z++) {
				h.setBlock(new BlockPos(x, 6, z), Blocks.STONE);
			}
		}
		BlockPos objective = new BlockPos(22, 1, 16);
		RaiderRaids.Raid raid = raid(h, objective);
		if (raid == null) {
			return;
		}
		RaiderInfantry officer = join(h, JugcraftRaiders.OFFICER, new Vec3(22.5, 1, 36.5), raid, objective);
		long[] since = {-1};
		Vec3[] heldAt = {null};
		h.onEachTick(() -> {
			if (since[0] < 0 && officer.engagement().holding()) {
				since[0] = h.getTick();
				heldAt[0] = officer.position();
			}
		});
		h.succeedWhen(() -> {
			h.assertTrue(since[0] >= 0 && h.getTick() - since[0] >= 200, "The officer should give its march up for good in the end, and "
					+ "keep to that; it is at " + rel(h, officer.position()) + ", holding " + officer.engagement().holding() + ", age " + officer.tickCount + ", target " + officer.getTarget());
			h.assertTrue(officer.engagement().holding(), "It never plans again once it holds");
			// Idle strolling must not carry it away from its final holding position.
			h.assertTrue(officer.position().distanceTo(heldAt[0]) < 12.0, "It holds near where it gave up");
			cleanUp(h, raid, officer);
		});
	}

	/**
	 * The town's own approaches, from its blueprint: the three gates first, then only stretches of wall a guard post can
	 * see, outside the wall on open ground; routes round the ring stay outside the wall; siege walkers stop at a gate's
	 * forecourt and the rest go through to its inner road; a failed gate falls back to another approach, and with every
	 * approach failed there is none (the raider holds).
	 */
	@GameTest
	public void theTownsApproachesAreGatesOrWatchedWall(GameTestHelper h) {
		TownData data = TownData.get();
		BlockPos origin = BlockPos.ZERO;
		List<CityRaidApproach.Approach> approaches = CityRaidApproach.approaches(origin);
		List<CityRaidRoute.Gate> gates = CityRaidRoute.gates(origin);
		for (int i = 0; i < gates.size(); i++) {
			h.assertTrue(approaches.get(i).isGate() && gates.get(i).equals(approaches.get(i).entrance()), "Gates come first, in order");
		}
		List<Vec3> posts = CityRaidApproach.posts(data);
		int walls = 0;
		for (CityRaidApproach.Approach approach : approaches) {
			BlockPos f = approach.front();
			h.assertTrue(CityRaidApproach.openGround(data, f.getX(), f.getZ()), "An approach's front is open ground outside the wall: " + f);
			if (approach.isGate()) {
				continue;
			}
			walls++;
			Vec3 aim = new Vec3(f.getX() + 0.5, 2.0, f.getZ() + 0.5);
			h.assertTrue(posts.stream().anyMatch(p -> p.distanceTo(aim) <= CityRaidApproach.POST_SIGHT && CityRaidApproach.sight(data, p, aim)),
					"A guard post sees the wall section at " + f);
		}
		h.assertTrue(walls >= 6 && walls < CityRaidApproach.SECTIONS, "Some, not all, stretches of wall are approaches: " + walls);
		Vec3 centre = Vec3.atBottomCenterOf(io.github.jimbozoomer.jugcraft.town.Town.centre(origin));
		for (int degrees = 0; degrees < 360; degrees += 30) {
			double a = Math.toRadians(degrees);
			Vec3 from = centre.add(Math.cos(a) * 130, 1, Math.sin(a) * 130);
			CityRaidApproach.Approach first = CityRaidApproach.choose(origin, from, Set.of());
			h.assertTrue(first != null && first.isGate(), "A raid makes for a gate first");
			List<BlockPos> foot = CityRaidApproach.route(origin, from, first, false);
			List<BlockPos> tall = CityRaidApproach.route(origin, from, first, true);
			h.assertTrue(foot.getLast().equals(first.entrance().inside()), "Infantry go through to the gate's inner road");
			h.assertTrue(tall.getLast().equals(first.front()), "Siege walkers stop at the gate's forecourt");
			for (int i = 0; i < tall.size(); i++) {
				BlockPos p = tall.get(i);
				h.assertTrue(data.mask(p.getX(), p.getZ()) != TownData.INSIDE, "The way to the forecourt stays outside the wall: " + p);
			}
			CityRaidApproach.Approach second = CityRaidApproach.choose(origin, from, Set.of(first.index()));
			h.assertTrue(second != null && second.index() != first.index(), "A failed gate falls back to another approach");
		}
		List<Integer> all = new ArrayList<>();
		approaches.forEach(a -> all.add(a.index()));
		h.assertTrue(CityRaidApproach.choose(origin, centre, all) == null, "With every approach failed there is none");
		h.succeed();
	}

	// ---------------------------------------------------------------- helpers

	private static void floor(GameTestHelper h) {
		for (int x = 0; x < 44; x++) {
			for (int z = 0; z < 44; z++) {
				h.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
	}

	/** A wall of stone bricks from (x0, z0) to (x1, z1), {@code height} blocks high. */
	static void wall(GameTestHelper h, int x0, int z0, int x1, int z1, int height) {
		for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
			for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
				for (int y = 1; y <= height; y++) {
					h.setBlock(new BlockPos(x, y, z), Blocks.STONE_BRICKS);
				}
			}
		}
	}

	/**
	 * A raid on {@code objective} (relative) whose own party is sent away at once, for the case's raiders to join. Null,
	 * and the case passed, during the Thanksgiving truce (no raid starts then).
	 */
	static RaiderRaids.@Nullable Raid raid(GameTestHelper h, BlockPos objective) {
		ServerLevel level = h.getLevel();
		BlockPos at = h.absolutePos(objective);
		RaiderRaids.Raid raid = RaiderRaids.start(level, at, at, 1, RandomSource.create(11));
		if (raid == null) {
			h.assertTrue(JugcraftSeasons.thanksgivingTruce(), "A raid starts outside the Thanksgiving truce");
			h.succeed();
			return null;
		}
		for (UUID id : raid.members()) {
			Entity member = level.getEntity(id);
			if (member != null) {
				member.discard();
			}
		}
		return raid;
	}

	/** A raider of {@code type} at {@code at} (relative) that belongs to {@code raid}, headed for {@code objective}. */
	static <T extends Mob> T join(GameTestHelper h, EntityType<T> type, Vec3 at, RaiderRaids.Raid raid, BlockPos objective) {
		T mob = h.spawn(type, at);
		((Raider) mob).joinRaid(raid.id(), h.absolutePos(objective));
		return mob;
	}

	private static void discard(@Nullable Entity... entities) {
		for (Entity entity : entities) {
			if (entity != null) {
				entity.discard();
			}
		}
	}

	/** Ends the case and passes it: the raid withdraws and its raiders go. */
	private static void done(GameTestHelper h, RaiderRaids.Raid raid, Mob... raiders) {
		cleanUp(h, raid, raiders);
		h.succeed();
	}

	/** The raid withdraws and its raiders go (inside a succeedWhen, which passes the case itself). */
	static void cleanUp(GameTestHelper h, RaiderRaids.Raid raid, Mob... raiders) {
		RaiderRaids.withdraw(h.getLevel(), raid);
		for (Mob raider : raiders) {
			raider.ejectPassengers();
			raider.discard();
		}
	}

	/** Where {@code at} is in the case's own coordinates, for messages. */
	static Vec3 rel(GameTestHelper h, Vec3 at) {
		return at.subtract(h.absoluteVec(Vec3.ZERO));
	}
}
