package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.lair.ChurchBridge;
import io.github.jimbozoomer.jugcraft.lair.ChurchBridge.Brazier;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairInstance;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import io.github.jimbozoomer.jugcraft.lair.horseman.HessianHorsemanEntity;
import io.github.jimbozoomer.jugcraft.lair.horseman.HessianHorsemanEntity.Action;
import io.github.jimbozoomer.jugcraft.lair.horseman.HessianHorsemanEntity.Attack;
import io.github.jimbozoomer.jugcraft.lair.horseman.HessianHorsemanEntity.Lantern;
import io.github.jimbozoomer.jugcraft.lair.horseman.HessianHorsemanEntity.Phase;
import io.github.jimbozoomer.jugcraft.lair.horseman.HorsemanLoot;
import io.github.jimbozoomer.jugcraft.lair.horseman.JugcraftHorseman;
import io.github.jimbozoomer.jugcraft.lair.horseman.PhantomRiderEntity;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Client game test for the Headless Horseman in his lair (docs/features/hessian-horseman.md), in a real Church Bridge:
 * unlike a game-test server's world, it has the lair dimension. His fight from end to end, with the one player in
 * survival (shielded by Resistance, so it can watch every attack): he waits mounted at the bridge's foot, his lantern
 * dark; stepping off the wood road wakes him, his horse rears and his lantern bursts into flame; the Dark Road snuffs the
 * two braziers nearest the player, who lights them both again and reveals him, his lantern flaring white; the Ride's
 * attacks; at half health the Hollow Wakes, his lantern held high at the bridge's foot; the Headless Charge's Head Hurl at
 * the player on the bridge, and his Phantom Riders; and when he falls the player has their loot and Laid to Rest, his
 * riders are gone, the braziers burn and Grey Mist stands on the bridge, and a fresh instance in his slot has none of it
 * and him waiting again. Along the way, pictures of each part of the fight, the world frozen for each so the pose holds.
 * CI job {@code client}.
 */
public class HessianHorsemanClientGameTests implements FabricClientGameTest {
	private static final Lair HOLLOW = Lair.CHURCH_BRIDGE;
	/** A standing player's eyes over their feet, where the camera looks from. */
	private static final double EYE = 1.62;
	/** Where he is posed for his attacks (the template's x, z): on the road in the hollow's middle, facing south down it. */
	private static final double POSE_X = 36.5;
	private static final double POSE_Z = 46.5;
	/** How far in front of him the player stands for a pose. */
	private static final double FRONT = 7.0;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			context.runOnClient(client -> {
				if (!client.gui.hud.isHidden()) {
					client.gui.hud.toggle();
				}
			});
			TestServerContext server = singleplayer.getServer();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("difficulty normal");  // he is a monster: in peaceful he would be gone

			// An instance opens: the hollow is placed and he waits at the bridge's foot; the player comes to on the road.
			server.runOnServer(minecraft -> {
				LairInstance instance = Lairs.open(minecraft, HOLLOW);
				check(instance != null, "No Church Bridge opened");
				check(Lairs.enter(player(minecraft), instance), "The player did not go in");
			});
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			BlockPos o = HOLLOW.origin(0);
			String dim = HOLLOW.dimension.identifier().toString();
			double stand = ChurchBridge.STAND;
			Vec3 at = new Vec3(o.getX() + POSE_X, o.getY() + stand, o.getZ() + POSE_Z);
			server.runOnServer(minecraft -> {
				HessianHorsemanEntity horseman = horseman(minecraft);
				check(horseman.phase() == Phase.WAITING, "He is not waiting but " + horseman.phase());
				check(horseman.position().distanceTo(horseman.waiting()) < 0.6, "He is not at the bridge's foot but at " + horseman.position());
				check(horseman.lantern() == Lantern.DARK, "Waiting, his lantern burns");
			});
			shot(context, singleplayer, dim, o, 39.5, stand + 0.5, 39.5, ChurchBridge.WAIT_X, stand + 1.8, ChurchBridge.WAIT_Z, false,
					"jugcraft_hessian_horseman_waiting");

			// Stepping off the road wakes him: the player, in survival, and his horse rears, his lantern bursting into flame.
			server.runCommand("effect give @a minecraft:resistance infinite 4 true");
			server.runCommand("effect give @a minecraft:saturation infinite 0 true");
			server.runCommand(String.format(Locale.ROOT, "execute in %s run tp @a %.1f %.3f %.1f 180 0", dim, o.getX() + 30.5, o.getY() + stand,
					o.getZ() + 44.5));
			server.runCommand("gamemode survival @a");
			context.waitTicks(30);
			server.runCommand("tick freeze");
			server.runOnServer(minecraft -> {
				HessianHorsemanEntity horseman = horseman(minecraft);
				check(horseman.phase() == Phase.WAKING && horseman.lantern() == Lantern.LIT, "Stepping off the road did not wake him: "
						+ horseman.phase());
			});
			shot(context, singleplayer, dim, o, 41.5, stand + 0.5, 38.5, ChurchBridge.WAIT_X, stand + 2.0, ChurchBridge.WAIT_Z, true,
					"jugcraft_hessian_horseman_wakes");
			server.runCommand("tick unfreeze");
			context.waitTicks(HessianHorsemanEntity.WAKE_TICKS);
			server.runOnServer(minecraft -> check(horseman(minecraft).phase() == Phase.RIDE, "He does not ride: " + horseman(minecraft).phase()));

			// The Dark Road: by the west braziers he wheels and lifts his lantern, and the two nearest the player go dark.
			List<Brazier> expected = ChurchBridge.run(26.5, 47.5, HessianHorsemanEntity.SNUFFED);
			server.runOnServer(minecraft -> {
				player(minecraft).teleportTo(o.getX() + 26.5, o.getY() + stand, o.getZ() + 47.5);
				horseman(minecraft).snuffIn(1);
			});
			// He finishes an attack already begun before he snuffs.
			int snuffWait = Arrays.stream(Attack.values()).mapToInt(Attack::length).max().orElseThrow() + HessianHorsemanEntity.SNUFF_TICKS + 40;
			boolean snuffing = false;
			for (int wait = 0; wait < snuffWait && !snuffing; wait++) {
				context.waitTick();
				snuffing = server.computeOnServer(minecraft -> !horseman(minecraft).snuffing().isEmpty());
			}
			check(snuffing, "He did not ride the Dark Road after a full attack's time");
			context.waitTicks(HessianHorsemanEntity.SNUFF_TICKS / 2);
			server.runCommand("tick freeze");
			Vec3 snuffer = server.computeOnServer(minecraft -> horseman(minecraft).position());
			shotFrom(context, singleplayer, dim, snuffer, 5.0, 0.5, 5.0, 0.0, 2.5, 0.0, "jugcraft_hessian_horseman_dark_road");
			server.runCommand("tick unfreeze");
			context.waitTicks(HessianHorsemanEntity.SNUFF_TICKS);
			server.runOnServer(minecraft -> {
				HessianHorsemanEntity horseman = horseman(minecraft);
				ServerLevel level = lair(minecraft);
				check(horseman.snuffed().equals(expected), "He snuffed " + horseman.snuffed() + ", not " + expected);
				for (Brazier brazier : expected) {
					check(!ChurchBridge.lit(level, o, brazier), "The " + brazier.title() + " brazier is not dark");
				}
			});
			context.waitTicks(4);
			server.runCommand("tick freeze");
			Brazier first = expected.getFirst();
			shot(context, singleplayer, dim, o, ChurchBridge.BRAZIERS[first.ordinal()].getX() + 4.5, stand + 1.5,
					ChurchBridge.BRAZIERS[first.ordinal()].getZ() + 3.5, ChurchBridge.BRAZIERS[first.ordinal()].getX() + 0.5, stand + 0.5,
					ChurchBridge.BRAZIERS[first.ordinal()].getZ() + 0.5, true, "jugcraft_hessian_horseman_snuffed");

			// The player lights both again in time: he is revealed, his lantern flaring white, his horse rearing.
			server.runCommand("tick unfreeze");
			server.runOnServer(minecraft -> {
				ServerLevel level = lair(minecraft);
				ServerPlayer player = player(minecraft);
				for (Brazier brazier : expected) {
					BlockPos pos = brazier.pos(o);
					player.teleportTo(pos.getX() + 0.5, o.getY() + stand, pos.getZ() + 2.5);
					player.gameMode.useItemOn(player, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND,
							new BlockHitResult(Vec3.atCenterOf(pos), Direction.SOUTH, pos, false));
				}
				HessianHorsemanEntity horseman = horseman(minecraft);
				check(horseman.revealed() && horseman.lantern() == Lantern.WHITE, "Lighting both braziers again did not reveal him");
				player.teleportTo(at.x, at.y, at.z + FRONT);
				horseman.teleportTo(at.x, at.y, at.z);
				face(horseman);
			});
			context.waitTicks(12);
			server.runCommand("tick freeze");
			shotFrom(context, singleplayer, dim, at, 5.0, 0.5, 6.0, 0.0, 2.0, 0.0, "jugcraft_hessian_horseman_revealed");

			// The Ride's attacks, each caught as it lands, he facing south, the player in front of him.
			pose(context, server, at, (horseman, level, player) -> horseman.begin(level, Attack.SABRE_PASS, player), Attack.SABRE_PASS.windup - 2);
			shotFrom(context, singleplayer, dim, at, 6.0, 0.5, 3.0, 0.0, 2.0, 3.0, "jugcraft_hessian_horseman_sabre_pass");
			pose(context, server, at, (horseman, level, player) -> horseman.begin(level, Attack.PUMPKIN_TOSS, player), Attack.PUMPKIN_TOSS.windup - 3);
			server.runOnServer(minecraft -> check(horseman(minecraft).holding(), "He does not hold a pumpkin up"));
			shotFrom(context, singleplayer, dim, at, 6.0, 0.5, 3.0, 0.0, 2.5, 4.0, "jugcraft_hessian_horseman_pumpkin_toss");
			pose(context, server, at, (horseman, level, player) -> horseman.begin(level, Attack.REARING_STOMP, player), Attack.REARING_STOMP.windup - 2);
			shotFrom(context, singleplayer, dim, at, 6.0, 0.5, 2.0, 0.0, 2.0, 0.0, "jugcraft_hessian_horseman_rearing_stomp");

			// At half his health, the Hollow Wakes: back at the bridge's foot, his lantern held high.
			server.runCommand("tick unfreeze");
			server.runOnServer(minecraft -> {
				HessianHorsemanEntity horseman = horseman(minecraft);
				horseman.snuffIn(2000);
				horseman.setHealth(horseman.getMaxHealth() * HessianHorsemanEntity.WAKES_AT - 1.0F);
			});
			boolean lifted = false;
			for (int wait = 0; wait < snuffWait + HessianHorsemanEntity.RIDE_BACK_TICKS && !lifted; wait++) {
				context.waitTick();
				lifted = server.computeOnServer(minecraft -> horseman(minecraft).phase() == Phase.HOLLOW_WAKES
						&& horseman(minecraft).action() == Action.LIFT);
			}
			check(lifted, "The Hollow did not wake, or he did not lift his lantern at the bridge's foot");
			context.waitTicks(16);
			server.runCommand("tick freeze");
			shot(context, singleplayer, dim, o, 41.5, stand + 0.5, 39.5, ChurchBridge.WAIT_X, stand + 2.5, ChurchBridge.WAIT_Z, true,
					"jugcraft_hessian_horseman_hollow_wakes");
			server.runCommand("tick unfreeze");
			context.waitTicks(HessianHorsemanEntity.WAKES_TICKS);
			server.runOnServer(minecraft -> check(horseman(minecraft).phase() == Phase.CHARGE, "The Headless Charge did not follow: "
					+ horseman(minecraft).phase()));

			// The Headless Charge: his Head Hurl at the player on the bridge, and his Phantom Riders.
			server.runOnServer(minecraft -> {
				ServerLevel level = lair(minecraft);
				ServerPlayer player = player(minecraft);
				HessianHorsemanEntity horseman = horseman(minecraft);
				player.teleportTo(o.getX() + 36.5, o.getY() + stand, o.getZ() + 25.5);
				horseman.teleportTo(o.getX() + 36.5, o.getY() + stand, o.getZ() + 36.5);
				horseman.snuffIn(2000);
				horseman.begin(level, Attack.HEAD_HURL, player);
			});
			context.waitTicks(Attack.HEAD_HURL.windup - 4);
			server.runCommand("tick freeze");
			server.runOnServer(minecraft -> check(horseman(minecraft).held() == HessianHorsemanEntity.Held.HEAD, "He does not hold his lantern head up"));
			shot(context, singleplayer, dim, o, 42.5, stand + 1.0, 33.5, 36.5, stand + 2.0, 31.0, true, "jugcraft_hessian_horseman_head_hurl");
			pose(context, server, at, (horseman, level, player) -> horseman.begin(level, Attack.PHANTOM_RIDERS, player),
					Attack.PHANTOM_RIDERS.windup + 8);
			server.runOnServer(minecraft -> check(!riders(minecraft).isEmpty(), "No phantom riders gallop"));
			shotFrom(context, singleplayer, dim, at, 9.0, 3.0, 9.0, 0.0, 1.0, FRONT, "jugcraft_hessian_horseman_phantom_riders");

			// He falls: the player's loot and advancement, his riders gone with him, the braziers burning, and Grey Mist on
			// the bridge.
			server.runCommand("tick unfreeze");
			server.runOnServer(minecraft -> {
				ServerLevel level = lair(minecraft);
				HessianHorsemanEntity horseman = horseman(minecraft);
				check(HorsemanLoot.firstKill(player(minecraft)), "The player had already won Laid to Rest");
				horseman.took(player(minecraft), 10.0F);
				horseman.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
			});
			context.waitTicks(10);
			server.runCommand("tick freeze");
			server.runOnServer(minecraft -> {
				ServerPlayer player = player(minecraft);
				ServerLevel level = lair(minecraft);
				check(player.getInventory().countItem(BuiltInRegistries.ITEM.getValue(io.github.jimbozoomer.jugcraft.Jugcraft.id("horseman_lantern"))) >= 1,
						"No Horseman's Lantern");
				int trophies = player.getInventory().countItem(JugcraftHorseman.hollowflail()) + player.getInventory().countItem(JugcraftHorseman.hessianSabre());
				check(trophies == 1, "A first win must bring one of his trophies, not " + trophies);
				check(!HorsemanLoot.firstKill(player), "Laid to Rest was not awarded");
				for (BlockPos cell : ChurchBridge.bridgeExitCells()) {
					check(level.getBlockState(cell.offset(o)).is(JugcraftLairs.LAIR_EXIT), "No Grey Mist on the bridge at " + cell);
				}
				check(riders(minecraft).isEmpty(), "His riders outlived him");
				check(ChurchBridge.lit(level, o).size() == Brazier.values().length, "The braziers do not all burn: " + ChurchBridge.lit(level, o));
			});
			shot(context, singleplayer, dim, o, 40.5, stand + 1.5, 33.5, 36.5, stand + 1.0, ChurchBridge.BRIDGE_EXIT_Z + 0.5, true,
					"jugcraft_hessian_horseman_defeated");
			server.runCommand("tick unfreeze");
			context.waitTicks(30);
			server.runOnServer(minecraft -> check(lair(minecraft).getEntitiesOfClass(HessianHorsemanEntity.class, slot()).stream()
					.noneMatch(HessianHorsemanEntity::isAlive), "He is still there"));

			// A fresh instance in his slot is placed anew: the Grey Mist he left is gone with the old one and, once the player
			// has gone in again (onto the road), he waits at the bridge's foot again.
			server.runOnServer(minecraft -> {
				LairInstance used = Lairs.instance(HOLLOW, 0);
				check(used != null, "His instance is not open");
				Lairs.close(minecraft, used);
				LairInstance fresh = Lairs.open(minecraft, HOLLOW);
				check(fresh != null && fresh.slot == 0, "No fresh instance opened in his slot");
				ServerLevel level = lair(minecraft);
				for (BlockPos cell : ChurchBridge.bridgeExitCells()) {
					check(!level.getBlockState(cell.offset(o)).is(JugcraftLairs.LAIR_EXIT), "His Grey Mist outlived his instance at " + cell);
				}
				check(Lairs.enter(player(minecraft), fresh), "The player did not go into the fresh hollow");
			});
			context.waitTicks(40);
			server.runOnServer(minecraft -> {
				HessianHorsemanEntity again = horseman(minecraft);
				check(again.phase() == Phase.WAITING && again.position().distanceTo(again.waiting()) < 0.6,
						"He does not wait at the fresh hollow's bridge foot: " + again.phase() + " at " + again.position());
			});
			System.out.println("[hessian_horseman] client game test: his fight passed end to end");
			server.runOnServer(minecraft -> Lairs.reset());
		}
	}

	@FunctionalInterface
	private interface Posing {
		void pose(HessianHorsemanEntity horseman, ServerLevel level, ServerPlayer player);
	}

	private static void face(HessianHorsemanEntity horseman) {
		horseman.setYRot(0.0F);
		horseman.setYBodyRot(0.0F);
		horseman.setYHeadRot(0.0F);
	}

	/**
	 * Unfreezes the world, puts him at {@code spot} facing south and the player {@value #FRONT} blocks in front of him (so
	 * his attacks land in view), puts his next snuffing off, does what {@code posing} asks, waits {@code ticks} for the pose
	 * and freezes the world on it. Every pose waits at least 10 ticks: the client learns where he was put only on the
	 * server's next updates, and a frozen world sends none.
	 */
	private static void pose(ClientGameTestContext context, TestServerContext server, Vec3 spot, Posing posing, int ticks) {
		server.runCommand("tick unfreeze");
		server.runOnServer(minecraft -> {
			ServerLevel level = lair(minecraft);
			HessianHorsemanEntity horseman = horseman(minecraft);
			ServerPlayer player = player(minecraft);
			player.teleportTo(spot.x, spot.y, spot.z + FRONT);
			horseman.teleportTo(spot.x, spot.y, spot.z);
			face(horseman);
			horseman.snuffIn(2000);
			posing.pose(horseman, level, player);
		});
		context.waitTicks(Math.max(10, ticks));
		server.runCommand("tick freeze");
	}

	/**
	 * A picture from ({@code dx}, {@code up}, {@code dz}) off {@code base} (the camera's feet), turned to look at
	 * ({@code ax}, {@code ay}, {@code az}) off it, the world frozen.
	 */
	private static void shotFrom(ClientGameTestContext context, TestSingleplayerContext singleplayer, String dimension, Vec3 base, double dx,
			double up, double dz, double ax, double ay, double az, String name) {
		Vec3 camera = base.add(dx, up, dz);
		Vec3 look = base.add(ax, ay, az);
		shoot(context, singleplayer, dimension, camera.x, camera.y, camera.z, aimYaw(camera, look), aimPitch(camera, look), true, name);
	}

	/** A picture from (x, y, z) in the hollow's own coordinates (the camera's feet), turned to look at (ax, ay, az) in them. */
	private static void shot(ClientGameTestContext context, TestSingleplayerContext singleplayer, String dimension, BlockPos o, double x,
			double y, double z, double ax, double ay, double az, boolean frozen, String name) {
		Vec3 camera = new Vec3(o.getX() + x, o.getY() + y, o.getZ() + z);
		Vec3 look = new Vec3(o.getX() + ax, o.getY() + ay, o.getZ() + az);
		shoot(context, singleplayer, dimension, camera.x, camera.y, camera.z, aimYaw(camera, look), aimPitch(camera, look), frozen, name);
	}

	/** The yaw that turns a player standing at {@code feet} towards {@code at}. */
	private static int aimYaw(Vec3 feet, Vec3 at) {
		return (int) Math.round(Math.toDegrees(Math.atan2(-(at.x - feet.x), at.z - feet.z)));
	}

	/** The pitch that turns the eyes of a player standing at {@code feet} towards {@code at} (down is positive). */
	private static int aimPitch(Vec3 feet, Vec3 at) {
		double flat = Math.hypot(at.x - feet.x, at.z - feet.z);
		return (int) Math.round(Math.toDegrees(-Math.atan2(at.y - (feet.y + EYE), flat)));
	}

	private static ServerPlayer player(MinecraftServer minecraft) {
		return minecraft.getPlayerList().getPlayers().get(0);
	}

	private static ServerLevel lair(MinecraftServer minecraft) {
		return minecraft.getLevel(HOLLOW.dimension);
	}

	/** The first slot's hollow, with room round it. */
	private static AABB slot() {
		BlockPos o = HOLLOW.origin(0);
		return new AABB(o.getX() - 8, o.getY() - 16, o.getZ() - 8, o.getX() + HOLLOW.width + 8, o.getY() + HOLLOW.height + 8, o.getZ() + HOLLOW.length + 8);
	}

	/** He, alive, in the first slot's hollow. */
	private static HessianHorsemanEntity horseman(MinecraftServer minecraft) {
		List<HessianHorsemanEntity> found = lair(minecraft).getEntitiesOfClass(HessianHorsemanEntity.class, slot(), HessianHorsemanEntity::isAlive);
		check(!found.isEmpty(), "The Headless Horseman is not in his hollow");
		return found.getFirst();
	}

	/** His phantom riders, galloping. */
	private static List<PhantomRiderEntity> riders(MinecraftServer minecraft) {
		return lair(minecraft).getEntitiesOfClass(PhantomRiderEntity.class, slot(), PhantomRiderEntity::isAlive);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	/**
	 * Stands the camera at (x, y, z) in {@code dimension}, looking along yaw and pitch, waits for the world to draw, and
	 * shoots.
	 */
	private static void shoot(ClientGameTestContext context, TestSingleplayerContext singleplayer, String dimension, double x, double y,
			double z, int yaw, int pitch, boolean frozen, String name) {
		TestServerContext server = singleplayer.getServer();
		server.runOnServer(minecraft -> {
			// Flying, so the camera stays where it is put (set on the server, which tells the client).
			ServerPlayer player = player(minecraft);
			player.getAbilities().mayfly = true;
			player.getAbilities().flying = true;
			player.onUpdateAbilities();
		});
		server.runCommand(String.format(Locale.ROOT, "execute in %s run tp @a %.2f %.2f %.2f %d %d", dimension, x, y, z, yaw, pitch));
		context.waitTicks(frozen ? 20 : 30);
		singleplayer.getConnection().waitForChunksRender();
		context.waitTicks(10);
		context.takeScreenshot(name);
	}
}
