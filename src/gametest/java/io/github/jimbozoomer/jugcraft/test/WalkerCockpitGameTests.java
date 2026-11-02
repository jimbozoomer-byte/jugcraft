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
import net.minecraft.gametest.framework.GameTest;
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
}
