package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.artillery.ArtilleryShell;
import io.github.jimbozoomer.jugcraft.artillery.JugcraftArtillery;
import io.github.jimbozoomer.jugcraft.walker.ArmouredWalker;
import io.github.jimbozoomer.jugcraft.walker.DieselWalker;
import io.github.jimbozoomer.jugcraft.walker.HowitzerWalker;
import io.github.jimbozoomer.jugcraft.walker.JugcraftWalkers;
import io.github.jimbozoomer.jugcraft.walker.WalkerAim;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The walker cockpit (docs/features/walker-cockpit.md): the pilots see out over their hulls, the cannon's shell lands
 * where the pilot's telegraph says it will, the reach the telegraph draws is what the attack strikes, and the gauges are
 * the walker's own fuel and wear.
 */
public class WalkerCockpitGameTests {
	/** The tops of the Armoured and Howitzer Walkers' hulls, in blocks (tools/armoured_walker.py ROOF; the Howitzer's hatch ring). */
	private static final double ARMOURED_ROOF = 72 / 16.0;
	private static final double HOWITZER_ROOF = 81.5 / 16.0;

	private static void floor(GameTestHelper helper, int size) {
		for (int x = 0; x < size; x++) {
			for (int z = 0; z < size; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
	}

	private static <T extends DieselWalker> T piloted(GameTestHelper helper, EntityType<T> type, Vec3 at, ServerPlayer pilot) {
		T walker = helper.spawn(type, at);
		walker.setFuel(1000);
		pilot.setPos(walker.getX(), walker.getY(), walker.getZ());
		helper.assertTrue(pilot.startRiding(walker, true, true), "The pilot could not climb in");
		return walker;
	}

	/** The Armoured and Howitzer Walkers' pilots look out from over their roofs, not from inside the hull. */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 40, skyAccess = true)
	public void walkerPilotsLookOutOverTheirHulls(GameTestHelper helper) {
		floor(helper, 44);
		ServerPlayer armouredPilot = helper.makeMockServerPlayerInLevel();
		ServerPlayer howitzerPilot = helper.makeMockServerPlayerInLevel();
		var armoured = piloted(helper, JugcraftWalkers.ARMOURED_WALKER, new Vec3(8.5, 1, 8.5), armouredPilot);
		var howitzer = piloted(helper, JugcraftWalkers.HOWITZER_WALKER, new Vec3(28.5, 1, 8.5), howitzerPilot);
		helper.runAfterDelay(5, () -> {
			double armouredEye = armouredPilot.getEyeY() - armoured.getY();
			double howitzerEye = howitzerPilot.getEyeY() - howitzer.getY();
			helper.assertTrue(armouredEye > ARMOURED_ROOF + 0.2,
					"The Armoured Walker's pilot should see over its roof (" + ARMOURED_ROOF + "), but their eye is at " + armouredEye);
			helper.assertTrue(howitzerEye > HOWITZER_ROOF + 0.2,
					"The Howitzer Walker's pilot should see over its hatch ring (" + HOWITZER_ROOF + "), but their eye is at " + howitzerEye);
			helper.assertTrue(armoured.cameraDistance() > 4 && howitzer.cameraDistance() > armoured.cameraDistance(),
					"The third-person camera should sit back past each hull");
			helper.succeed();
		});
	}

	/**
	 * The Armoured Walker's cannon lands on what the pilot looks at, low on the ground 28 blocks ahead: the arc the pilot's
	 * telegraph draws ({@link WalkerAim#flight} from {@link ArmouredWalker#cannonVelocity}) comes down there, the shell
	 * fired comes down where the arc does, and its burst hurts the pig standing by.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 120, skyAccess = true)
	public void armouredCannonLandsWhereItsArcShows(GameTestHelper helper) {
		floor(helper, 44);
		ServerPlayer pilot = helper.makeMockServerPlayerInLevel();
		pilot.setGameMode(GameType.SURVIVAL);
		var walker = piloted(helper, JugcraftWalkers.ARMOURED_WALKER, new Vec3(8.5, 1, 6.5), pilot);
		pilot.getInventory().add(new ItemStack(JugcraftArtillery.HEAVY_SHELL_ITEM, 1));
		// Two blocks off the line of flight, so the shell comes down on the ground and not on it; the burst still reaches it.
		Mob pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(10, 1, 34));
		Vec3 aim = helper.absoluteVec(new Vec3(8.5, 1.0, 34.5));
		Vec3[] predicted = {null};
		Map<UUID, Vec3> flying = new HashMap<>();
		Vec3[] landed = {null};
		AABB area = new AABB(helper.absolutePos(BlockPos.ZERO)).expandTowards(44, 26, 44);
		int[] tick = {0};
		helper.onEachTick(() -> {
			pilot.lookAt(EntityAnchorArgument.Anchor.EYES, aim);
			if (tick[0]++ == 5) {
				Vec3 muzzle = walker.muzzle(1.0F);
				WalkerAim.Flight flight = WalkerAim.flight(helper.getLevel(), muzzle,
						ArmouredWalker.cannonVelocity(helper.getLevel(), pilot, muzzle), JugcraftArtillery.HEAVY_GRAVITY, pilot);
				helper.assertTrue(flight.impact() != null, "The telegraph should show where the shell comes down");
				helper.assertTrue(flight.impact().distanceTo(aim) < 1.5,
						"The telegraph should land on the crosshair " + aim + ", not " + flight.impact());
				predicted[0] = flight.impact();
			}
			walker.steer(pilot, 0, 0, 0, predicted[0] != null ? 1 : 0, 0);
			Map<UUID, Vec3> seen = new HashMap<>();
			for (ArtilleryShell shell : helper.getLevel().getEntitiesOfClass(ArtilleryShell.class, area)) {
				seen.put(shell.getUUID(), shell.position());
			}
			for (Map.Entry<UUID, Vec3> last : flying.entrySet()) {
				if (!seen.containsKey(last.getKey())) {
					landed[0] = last.getValue();
				}
			}
			flying.clear();
			flying.putAll(seen);
		});
		helper.succeedWhen(() -> {
			helper.assertTrue(landed[0] != null, "The shell should have come down");
			// Its last place before it burst is less than a tick's flight short of where it came down.
			helper.assertTrue(landed[0].distanceTo(predicted[0]) < ArmouredWalker.CANNON_SPEED + 1.0,
					"The shell should come down where the telegraph showed (" + predicted[0] + "), but was last at " + landed[0]);
			helper.assertTrue(!pig.isAlive() || pig.getHealth() < pig.getMaxHealth(), "The burst should hurt the pig by the crosshair");
		});
	}

	/** The Howitzer Walker's telegraph, drawn from its barrel, comes down on the crosshair once the gun is laid. */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 100, skyAccess = true)
	public void howitzerArcComesDownOnTheCrosshairOnceLaid(GameTestHelper helper) {
		floor(helper, 44);
		ServerPlayer pilot = helper.makeMockServerPlayerInLevel();
		var walker = piloted(helper, JugcraftWalkers.HOWITZER_WALKER, new Vec3(8.5, 1, 6.5), pilot);
		Vec3 aim = helper.absoluteVec(new Vec3(8.5, 1.0, 38.5));
		helper.onEachTick(() -> {
			pilot.lookAt(EntityAnchorArgument.Anchor.EYES, aim);
			walker.steer(pilot, 0, 0, 0, 0, 0);
		});
		helper.runAfterDelay(60, () -> {
			// The muzzle and barrel as the pilot's client works them out (HowitzerWalker.muzzle and barrel), from the synced
			// elevation and yaw; on the server they are read straight, as there is no easing between frames to do.
			Vec3 dir = Vec3.directionFromRotation(-walker.elevation(), walker.getYRot());
			Vec3 trunnion = walker.position().add(HowitzerWalker.TRUNNION.yRot(-walker.getYRot() * net.minecraft.util.Mth.DEG_TO_RAD));
			WalkerAim.Flight flight = WalkerAim.flight(helper.getLevel(), trunnion.add(dir.scale(HowitzerWalker.BARREL)),
					dir.scale(JugcraftArtillery.HEAVY_SPEED), JugcraftArtillery.HEAVY_GRAVITY, pilot);
			helper.assertTrue(flight.impact() != null && flight.impact().distanceTo(aim) < 2.5,
					"Once laid, the gun's arc should come down on the crosshair " + aim + ", not " + flight.impact());
			helper.succeed();
		});
	}

	/**
	 * The reach the telegraph draws is what the attack strikes: the Diesel Walker's fist reaches the pig in front and not
	 * the one behind, and strikes only the one in front; the Howitzer Walker's stomp reaches the pig beside it and not the
	 * one six blocks off.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", maxTicks = 100, skyAccess = true)
	public void theDrawnReachIsWhatIsStruck(GameTestHelper helper) {
		floor(helper, 44);
		ServerPlayer pilot = helper.makeMockServerPlayerInLevel();
		ServerPlayer gunner = helper.makeMockServerPlayerInLevel();
		DieselWalker walker = piloted(helper, JugcraftWalkers.DIESEL_WALKER, new Vec3(7.5, 1, 7.5), pilot);
		HowitzerWalker howitzer = piloted(helper, JugcraftWalkers.HOWITZER_WALKER, new Vec3(30.5, 1, 20.5), gunner);
		Mob front = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(7, 1, 9));
		Mob behind = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(7, 1, 4));
		Mob beside = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(32, 1, 21));
		Mob far = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(36, 1, 20));
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(walker.attackReaches(front), "The fist's drawn reach should take in the pig in front");
			helper.assertTrue(!walker.attackReaches(behind), "The fist's drawn reach should not take in the pig behind");
			helper.assertTrue(howitzer.attackReaches(beside), "The stomp's drawn ring should take in the pig beside it");
			helper.assertTrue(!howitzer.attackReaches(far), "The stomp's drawn ring should not take in the pig six blocks off");
			helper.assertTrue(walker.attackReady(), "A walker at rest should be ready to strike");
		});
		helper.runAfterDelay(5, () -> walker.steer(pilot, 0, 0, 0, 0, 1));
		helper.succeedWhen(() -> {
			helper.assertTrue(front.getHealth() < front.getMaxHealth() || !front.isAlive(), "The fist should strike the pig in front");
			helper.assertTrue(behind.getHealth() == behind.getMaxHealth(), "The fist should not strike the pig behind");
		});
	}

	/** The pilot's gauges are the walker's own: its fuel, and the wear a hit puts on its hull. */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 40)
	public void theGaugesReadTheWalker(GameTestHelper helper) {
		floor(helper, 16);
		ServerPlayer pilot = helper.makeMockServerPlayerInLevel();
		ServerPlayer attacker = helper.makeMockServerPlayerInLevel();
		attacker.setGameMode(GameType.SURVIVAL);
		DieselWalker walker = piloted(helper, JugcraftWalkers.DIESEL_WALKER, new Vec3(7.5, 1, 7.5), pilot);
		walker.setFuel(DieselWalker.FUEL_TANK / 4);
		helper.runAfterDelay(2, () -> walker.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(attacker), 10.0F));
		helper.runAfterDelay(4, () -> {
			helper.assertTrue(Math.abs(walker.fuelGauge() - 0.25F) < 0.01F, "The fuel gauge should read a quarter, not " + walker.fuelGauge());
			helper.assertTrue(walker.wear() > 0.0F && walker.wear() < 1.0F, "A hit should show as wear on the hull, not " + walker.wear());
			helper.succeed();
		});
	}

	/**
	 * Aiming never loads a chunk: a path whose two ends are in loaded chunks (0, 0) and (1, 1) but which cuts across the
	 * corner of chunk (1, 0) on the way counts that chunk, so the look or a shell's flight stops there instead of
	 * {@code Level.clip} loading it. A chunk the path does not cross, (0, 1), doesn't stop it, and with every chunk loaded
	 * the path runs on: then the look and the flight clip exactly as before.
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 20)
	public void aimingStopsAtAnUnloadedChunkOnItsPath(GameTestHelper helper) {
		Vec3 from = new Vec3(14.0, 70.0, 14.0);
		Vec3 to = new Vec3(18.0, 70.0, 17.0); // crosses x = 16 at z = 15.5, in chunk (1, 0), before z = 16
		helper.assertTrue(!WalkerAim.chunksLoadedAlong(from, to, (x, z) -> !(x == 1 && z == 0)),
				"A path crossing the unloaded chunk (1, 0) between loaded ends must count it");
		helper.assertTrue(!WalkerAim.chunksLoadedAlong(to, from, (x, z) -> !(x == 1 && z == 0)), "and the same the other way");
		helper.assertTrue(WalkerAim.chunksLoadedAlong(from, to, (x, z) -> !(x == 0 && z == 1)),
				"A chunk the path does not cross, (0, 1), must not stop it");
		helper.assertTrue(WalkerAim.chunksLoadedAlong(from, to, (x, z) -> true), "With every chunk loaded the path runs on");
		helper.assertTrue(!WalkerAim.chunksLoadedAlong(new Vec3(31.5, 70.0, 8.0), new Vec3(48.5, 70.0, 8.0), (x, z) -> x != 2),
				"A straight path through a whole unloaded chunk, (2, 0), must count it");
		// The clip's block walk starts a little behind its start (by a ten-millionth of its length), so a path starting
		// that close to a chunk's edge may read the block over it.
		helper.assertTrue(!WalkerAim.chunksLoadedAlong(new Vec3(15.9999999, 70.0, 8.0), new Vec3(10.0, 70.0, 8.0), (x, z) -> x != 1),
				"A path starting a hair short of a chunk's edge counts the chunk over the edge");
		helper.succeed();
	}

	/**
	 * Getting out, a pilot steps off on the ground beside the walker, clear of its hull, and takes no fall: the Howitzer's
	 * pilot (the tallest walker, 5.4 blocks) to its left, and the Armoured Walker's to its right when its left is walled
	 * off. Vanilla's default would set them on top of the hull and drop them through it.
	 */
	@GameTest(structure = "jugcraft-test:drone_depot", maxTicks = 60)
	public void pilotsStepOffBesideTheirWalkers(GameTestHelper helper) {
		floor(helper, 16);
		ServerPlayer open = helper.makeMockServerPlayerInLevel();
		ServerPlayer walled = helper.makeMockServerPlayerInLevel();
		open.setGameMode(GameType.SURVIVAL);
		walled.setGameMode(GameType.SURVIVAL);
		HowitzerWalker tall = piloted(helper, JugcraftWalkers.HOWITZER_WALKER, new Vec3(4.5, 1, 4.5), open);
		ArmouredWalker boxed = piloted(helper, JugcraftWalkers.ARMOURED_WALKER, new Vec3(11.5, 1, 10.5), walled);
		helper.runAfterDelay(3, () -> {
			// A wall four blocks high along the Armoured Walker's left, a block out from its hull.
			Vec3 heading = boxed.heading();
			Vec3 wall = boxed.position().add(left(boxed).scale(boxed.getBbWidth() / 2.0 + 1.0));
			for (int along = -2; along <= 2; along++) {
				for (int up = 0; up <= 3; up++) {
					helper.getLevel().setBlockAndUpdate(BlockPos.containing(wall.add(heading.scale(along))).above(up), Blocks.STONE.defaultBlockState());
				}
			}
			open.stopRiding();
			walled.stopRiding();
		});
		helper.runAfterDelay(25, () -> {
			steppedOff(helper, open, tall, 1.0);
			steppedOff(helper, walled, boxed, -1.0);
			helper.succeed();
		});
	}

	/** The walker's left, flat. */
	private static Vec3 left(DieselWalker walker) {
		Vec3 heading = walker.heading();
		return new Vec3(heading.z, 0, -heading.x);
	}

	/** The pilot is out, on the ground beside the walker clear of its hull, on its left ({@code side} 1) or right (-1), unhurt. */
	private static void steppedOff(GameTestHelper helper, ServerPlayer pilot, DieselWalker walker, double side) {
		String name = walker.getClass().getSimpleName();
		Vec3 away = pilot.position().subtract(walker.position()).multiply(1, 0, 1);
		helper.assertTrue(pilot.getVehicle() == null, "The " + name + "'s pilot is still aboard");
		helper.assertTrue(Math.abs(pilot.getY() - walker.getY()) < 0.6,
				"The " + name + "'s pilot should stand on the ground beside it, but is " + (pilot.getY() - walker.getY()) + " blocks up");
		helper.assertTrue(away.length() > walker.getBbWidth() / 2.0, "The " + name + "'s pilot stepped off inside its hull, " + away.length() + " out");
		helper.assertTrue(away.dot(left(walker)) * side > 0, "The " + name + "'s pilot stepped off on the wrong side: " + away);
		helper.assertTrue(pilot.getHealth() >= pilot.getMaxHealth(), "The " + name + "'s pilot was hurt getting out: " + pilot.getHealth());
	}
}
