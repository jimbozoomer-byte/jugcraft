package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.lair.CrownOfThunder;
import io.github.jimbozoomer.jugcraft.lair.CrownOfThunder.Rod;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairInstance;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import io.github.jimbozoomer.jugcraft.lair.roc.GaleFledglingEntity;
import io.github.jimbozoomer.jugcraft.lair.roc.JugcraftRoc;
import io.github.jimbozoomer.jugcraft.lair.roc.RocLoot;
import io.github.jimbozoomer.jugcraft.lair.roc.StormRocEntity;
import io.github.jimbozoomer.jugcraft.lair.roc.StormRocEntity.Attack;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Client game test for the Storm Roc (docs/features/storm-roc.md), on a real Crown of Thunder: unlike a game-test
 * server's world, it has the lair dimension, the summit, its rods, boulders and nest. His fight from end to end, with the
 * one player in survival (shielded by Resistance, so it can watch every attack): he waits in his nest; standing on the
 * summit wakes him and he rises; his Wing Buffet, Lightning Marks and Wind Strips; his fledglings called; a dive with the
 * north rod charged, his lane following the player, who stands beyond the rod: the rod grounds him; the Squall Rises; the
 * Eye of the Storm; and when he falls the player has their loot and Eye of the Storm, his fledglings are gone and the Grey
 * Mist stands at the foot of his crag, and a fresh instance in its slot has none of it and him in his nest again. Along
 * the way, pictures of each part of the fight, the world frozen for each so the pose holds. CI job {@code client}.
 */
public class StormRocClientGameTests implements FabricClientGameTest {
	private static final Lair CROWN = Lair.CROWN_OF_THUNDER;
	/** A standing player's eyes over their feet, where the camera looks from. */
	private static final double EYE = 1.62;
	/** Where he is posed for his attacks (the template's x, z): north of the summit's middle, facing south across it. */
	private static final double POSE_X = 32.5;
	private static final double POSE_Z = 29.5;
	/** Where the player stands for his dive (the template's x, z): beyond the north rod from where he dives. */
	private static final double BEYOND_X = 32.5;
	private static final double BEYOND_Z = 40.5;

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

			// An instance opens: the Crown is placed and he waits in his nest; the player arrives on the terrace.
			server.runOnServer(minecraft -> {
				LairInstance instance = Lairs.open(minecraft, CROWN);
				check(instance != null, "No Crown of Thunder opened");
				check(Lairs.enter(player(minecraft), instance), "The player did not go in");
			});
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			BlockPos o = CROWN.origin(0);
			String dim = CROWN.dimension.identifier().toString();
			int stand = CrownOfThunder.STAND;
			Vec3 centre = StormRocEntity.centre(o);
			Vec3 at = new Vec3(o.getX() + POSE_X, o.getY() + stand, o.getZ() + POSE_Z);
			server.runOnServer(minecraft -> {
				StormRocEntity roc = roc(minecraft);
				check(roc.phase() == StormRocEntity.Phase.WAITING, "He is not waiting but " + roc.phase());
				check(roc.position().distanceTo(roc.waiting()) < 0.1, "He is not in his nest but at " + roc.position());
				check(roc.action() == StormRocEntity.Action.PERCH, "Waiting, he is not perched");
			});
			shot(context, singleplayer, dim, o, 36.5, stand, 22.5, CrownOfThunder.WAIT_X, CrownOfThunder.WAIT_Y + 1.2, CrownOfThunder.WAIT_Z,
					false, "jugcraft_storm_roc_waiting");

			// Standing on the summit wakes him: he rises screeching off the nest; caught as he rises.
			server.runCommand("effect give @a minecraft:resistance infinite 4 true");
			server.runCommand("effect give @a minecraft:saturation infinite 0 true");
			server.runCommand(String.format(Locale.ROOT, "execute in %s run tp @a %.1f %.3f %.1f 180 0", dim, centre.x + 3.0, centre.y, centre.z - 6.0));
			server.runCommand("gamemode survival @a");
			context.waitTicks(24);
			server.runCommand("tick freeze");
			server.runOnServer(minecraft -> check(roc(minecraft).phase() == StormRocEntity.Phase.WAKING,
					"Standing on the summit did not wake him: " + roc(minecraft).phase()));
			shot(context, singleplayer, dim, o, 36.5, stand, 22.5, CrownOfThunder.WAIT_X, CrownOfThunder.WAIT_Y + 2.0, CrownOfThunder.WAIT_Z + 4.0,
					true, "jugcraft_storm_roc_wakes");
			// The player stands out of the fight (in creative) while he lands, so he has no foe to begin an attack on; then
			// back in.
			server.runCommand("gamemode creative @a");
			server.runCommand("tick unfreeze");
			context.waitTicks(StormRocEntity.WAKE_TICKS + 10);
			server.runOnServer(minecraft -> check(roc(minecraft).phase() == StormRocEntity.Phase.GATHERING,
					"He did not come down to fight: " + roc(minecraft).phase()));
			server.runCommand("gamemode survival @a");

			// His attacks, each caught as it is read: he north of the summit's middle facing south, the player five blocks in
			// front of him; the camera then stands where it shows the attack best.
			pose(context, server, at, (roc, level, player) -> roc.begin(level, Attack.WING_BUFFET, player), Attack.WING_BUFFET.windup - 4);
			server.runOnServer(minecraft -> check(roc(minecraft).action() == StormRocEntity.Action.BUFFET_WINDUP,
					"He did not rear for his Wing Buffet: " + roc(minecraft).action()));
			shotFrom(context, singleplayer, dim, at, 6.5, 0.5, 4.5, 0.0, 1.8, 0.5, "jugcraft_storm_roc_buffet");
			pose(context, server, at, (roc, level, player) -> roc.begin(level, Attack.LIGHTNING_MARKS, player), Attack.LIGHTNING_MARKS.windup - 6);
			server.runOnServer(minecraft -> check(roc(minecraft).marks().size() >= 1, "No Lightning Marks were laid"));
			shotFrom(context, singleplayer, dim, at, -5.0, 3.0, 9.0, 0.0, 0.5, 5.0, "jugcraft_storm_roc_marks");
			pose(context, server, at, (roc, level, player) -> roc.begin(level, Attack.WIND_STRIPS, player), Attack.WIND_STRIPS.windup + 10);
			server.runOnServer(minecraft -> check(roc(minecraft).strips().size() == StormRocEntity.STRIPS, "No Wind Strips were laid"));
			shotFrom(context, singleplayer, dim, at, 0.0, 6.0, -6.0, 0.0, 0.0, 8.0, "jugcraft_storm_roc_wind");
			pose(context, server, at, (roc, level, player) -> roc.callFledglings(level), 40);
			server.runOnServer(minecraft -> check(lair(minecraft).getEntitiesOfClass(GaleFledglingEntity.class, slot()).size()
					== StormRocEntity.FLEDGLING_CAP, "His fledglings did not leave the nest"));
			shotFrom(context, singleplayer, dim, at, 4.0, 1.0, 9.0, 0.0, 3.0, 0.0, "jugcraft_storm_roc_fledglings");

			// His rule: the north rod charges and he climbs behind it, his lane following the player, who stands beyond the
			// rod; caught hovering, the rod burning.
			Vec3 beyond = new Vec3(o.getX() + BEYOND_X, o.getY() + stand, o.getZ() + BEYOND_Z);
			pose(context, server, at, (roc, level, player) -> {
				player.teleportTo(beyond.x, beyond.y, beyond.z);
				roc.dive(level, player, Rod.NORTH, 0.0);
			}, StormRocEntity.CHARGE_TICKS + 15);
			server.runOnServer(minecraft -> {
				StormRocEntity roc = roc(minecraft);
				check(roc.dive() == StormRocEntity.Dive.LANE && roc.charged() == Rod.NORTH, "He is not holding over the edge, the north rod charged: "
						+ roc.dive());
				check(CrownOfThunder.charged(lair(minecraft), o) == Rod.NORTH, "The north rod does not burn");
			});
			shot(context, singleplayer, dim, o, 38.5, stand, 34.5, 32.5, stand + StormRocEntity.PERCH_UP, 13.5, true, "jugcraft_storm_roc_lane");
			// Back where his lane follows the player, the dive: the rod grounds him.
			server.runOnServer(minecraft -> player(minecraft).teleportTo(beyond.x, beyond.y, beyond.z));
			server.runCommand("tick unfreeze");
			context.waitTicks(StormRocEntity.LANE_TICKS - 15 + 20);
			server.runCommand("tick freeze");
			server.runOnServer(minecraft -> {
				StormRocEntity roc = roc(minecraft);
				check(roc.dive() == StormRocEntity.Dive.GROUNDED, "The charged rod did not ground him: " + roc.dive());
				check(CrownOfThunder.charged(lair(minecraft), o) == null, "The rod still burns after grounding him");
			});
			shot(context, singleplayer, dim, o, 37.5, stand, 27.5, 32.5, stand + 0.6, 22.5, true, "jugcraft_storm_roc_grounded");
			server.runOnServer(minecraft -> player(minecraft).teleportTo(beyond.x, beyond.y, beyond.z));
			server.runCommand("tick unfreeze");
			context.waitTicks(StormRocEntity.GROUNDED_TICKS + 5);
			server.runOnServer(minecraft -> check(roc(minecraft).dive() == StormRocEntity.Dive.NONE, "He did not rise from his stun"));

			// The Squall Rises: he climbs and circles over the summit, his fledglings out.
			pose(context, server, at, (roc, level, player) -> {
				roc.setHealth(roc.getMaxHealth() * StormRocEntity.SQUALL_AT - 1.0F);
				roc.squall(level);
			}, 25);
			server.runOnServer(minecraft -> check(roc(minecraft).phase() == StormRocEntity.Phase.RISING, "The Squall did not rise: " + roc(minecraft).phase()));
			shot(context, singleplayer, dim, o, 32.5, stand, 46.5, 32.5, stand + StormRocEntity.SQUALL_HEIGHT, 32.5, true, "jugcraft_storm_roc_squall");
			server.runCommand("tick unfreeze");
			context.waitTicks(StormRocEntity.SQUALL_TICKS);
			server.runOnServer(minecraft -> {
				StormRocEntity roc = roc(minecraft);
				check(roc.phase() == StormRocEntity.Phase.SQUALL, "The Squall did not begin: " + roc.phase());
				roc.setHealth(roc.getMaxHealth() * StormRocEntity.EYE_AT - 1.0F);
			});
			context.waitTicks(5);
			server.runOnServer(minecraft -> check(roc(minecraft).phase() == StormRocEntity.Phase.EYE, "The Eye of the Storm did not come: "
					+ roc(minecraft).phase()));

			// He falls: the player's loot and advancement, his fledglings gone, and the Grey Mist at the foot of his crag.
			// Beside it, a stand wears the Roc's Crest and holds the Stormcaller.
			server.runOnServer(minecraft -> {
				ServerLevel level = lair(minecraft);
				StormRocEntity roc = roc(minecraft);
				check(RocLoot.firstKill(player(minecraft)), "The player had already won Eye of the Storm");
				roc.took(player(minecraft), 10.0F);
				roc.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
			});
			server.runCommand(String.format(Locale.ROOT, "execute in %s run summon minecraft:armor_stand %.1f %.1f %.1f {ShowArms:1b,NoBasePlate:1b,"
					+ "Rotation:[170f,0f],equipment:{head:{id:\"jugcraft:rocs_crest\",count:1},mainhand:{id:\"jugcraft:stormcaller\",count:1}}}",
					dim, o.getX() + 35.5, o.getY() + (double) stand, o.getZ() + 17.5));
			context.waitTicks(6);
			server.runCommand("tick freeze");
			server.runOnServer(minecraft -> {
				ServerPlayer player = player(minecraft);
				ServerLevel level = lair(minecraft);
				check(player.getInventory().countItem(JugcraftRoc.ROC_FEATHER) >= 2, "No Roc Feathers");
				int trophies = player.getInventory().countItem(JugcraftRoc.stormcaller()) + player.getInventory().countItem(JugcraftRoc.galefeather());
				check(trophies == 1, "A first kill must bring one of his trophies, not " + trophies);
				check(!RocLoot.firstKill(player), "Eye of the Storm was not awarded");
				for (BlockPos cell : CrownOfThunder.fallExitCells()) {
					check(level.getBlockState(o.offset(cell)).is(JugcraftLairs.LAIR_EXIT), "No Grey Mist at the foot of his crag at " + cell);
				}
				check(level.getEntitiesOfClass(GaleFledglingEntity.class, slot()).isEmpty(), "His fledglings outlived him");
			});
			shot(context, singleplayer, dim, o, 37.5, stand, 23.5, 32.5, stand + 1.0, 15.5, true, "jugcraft_storm_roc_defeated");
			server.runCommand("tick unfreeze");
			server.runCommand(String.format(Locale.ROOT, "execute in %s run kill @e[type=minecraft:armor_stand]", dim));
			context.waitTicks(30);
			server.runOnServer(minecraft -> check(lair(minecraft).getEntitiesOfClass(StormRocEntity.class, slot()).stream()
					.noneMatch(StormRocEntity::isAlive), "He is still there"));

			// A fresh instance in its slot is placed anew: the Grey Mist he left is gone, and, once the player has gone in again
			// (onto the terrace, far from him), he waits in his nest again.
			server.runOnServer(minecraft -> {
				LairInstance used = Lairs.instance(CROWN, 0);
				check(used != null, "His instance is not open");
				Lairs.close(minecraft, used);
				LairInstance fresh = Lairs.open(minecraft, CROWN);
				check(fresh != null && fresh.slot == 0, "No fresh instance opened in its slot");
				ServerLevel level = lair(minecraft);
				for (BlockPos cell : CrownOfThunder.fallExitCells()) {
					check(!level.getBlockState(o.offset(cell)).is(JugcraftLairs.LAIR_EXIT), "His Grey Mist outlived its instance at " + cell);
				}
				check(Lairs.enter(player(minecraft), fresh), "The player did not go into the fresh Crown");
			});
			context.waitTicks(40);
			server.runOnServer(minecraft -> {
				StormRocEntity again = roc(minecraft);
				check(again.phase() == StormRocEntity.Phase.WAITING && again.position().distanceTo(again.waiting()) < 0.1,
						"He does not wait in the fresh Crown's nest: " + again.phase() + " at " + again.position());
			});
			System.out.println("[roc] client game test: his fight passed end to end");
			server.runOnServer(minecraft -> Lairs.reset());
		}
	}

	@FunctionalInterface
	private interface Posing {
		void pose(StormRocEntity roc, ServerLevel level, ServerPlayer player);
	}

	/**
	 * Unfreezes the world, puts him at {@code spot} facing south and the player five blocks in front of him (so his attacks
	 * land in view), does what {@code posing} asks, waits {@code ticks} for the pose and freezes the world on it. Every
	 * pose waits at least 10 ticks: the client learns where he was put only on the server's next updates, and a frozen
	 * world sends none.
	 */
	private static void pose(ClientGameTestContext context, TestServerContext server, Vec3 spot, Posing posing, int ticks) {
		server.runCommand("tick unfreeze");
		server.runOnServer(minecraft -> {
			ServerLevel level = lair(minecraft);
			StormRocEntity roc = roc(minecraft);
			ServerPlayer player = player(minecraft);
			player.teleportTo(spot.x, spot.y, spot.z + 5.0);
			roc.teleportTo(spot.x, spot.y, spot.z);
			roc.setYRot(0.0F);
			roc.setYBodyRot(0.0F);
			roc.setYHeadRot(0.0F);
			posing.pose(roc, level, player);
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

	/** A picture from (x, y, z) in the Crown's own coordinates (the camera's feet), turned to look at (ax, ay, az) in them. */
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
		return minecraft.getLevel(CROWN.dimension);
	}

	/** The first slot's Crown, with room round it (and over it, for his flights). */
	private static AABB slot() {
		BlockPos o = CROWN.origin(0);
		return new AABB(o.getX() - 8, o.getY() - 16, o.getZ() - 8, o.getX() + CROWN.width + 8, o.getY() + CROWN.height + 24,
				o.getZ() + CROWN.length + 8);
	}

	/** Him, alive, in the first slot's Crown. */
	private static StormRocEntity roc(MinecraftServer minecraft) {
		List<StormRocEntity> found = lair(minecraft).getEntitiesOfClass(StormRocEntity.class, slot(), StormRocEntity::isAlive);
		check(!found.isEmpty(), "The Storm Roc is not on his Crown");
		return found.getFirst();
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
