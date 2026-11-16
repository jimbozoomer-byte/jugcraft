package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.lair.ChapelMirrorBlock;
import io.github.jimbozoomer.jugcraft.lair.GlassChapel;
import io.github.jimbozoomer.jugcraft.lair.GlassChapel.Mirror;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairInstance;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import io.github.jimbozoomer.jugcraft.lair.abbot.AbbotLoot;
import io.github.jimbozoomer.jugcraft.lair.abbot.GlassAbbotEntity;
import io.github.jimbozoomer.jugcraft.lair.abbot.GlassAbbotEntity.Attack;
import io.github.jimbozoomer.jugcraft.lair.abbot.GlassImageEntity;
import io.github.jimbozoomer.jugcraft.lair.abbot.JugcraftAbbot;
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
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Client game test for the Glass Abbot (docs/features/glass-abbot.md), in a real world, which has the lair dimensions: an
 * instance of the Glass Chapel opens and he kneels at his lectern; a player stepping into the nave wakes him; his Shard
 * Fan and his Prism Line wound up; his rule: two mirrors marked, the player turns each to the pair's stop, he goes back
 * to his place and gathers the sun, and his own light comes home and cracks his mantle; the Veil and his images; his
 * fall, the player's loot and advancement, and the Grey Mist in the apse; and a fresh chapel where he kneels again. A
 * picture of each, the world frozen for each so the pose holds. CI job {@code client}.
 */
public class GlassAbbotClientGameTests implements FabricClientGameTest {
	private static final Lair CHAPEL = Lair.GLASS_CHAPEL;
	/** A standing player's eyes over their feet, where the camera looks from. */
	private static final double EYE = 1.62;
	/** Where he is posed for his attacks (the template's x, z): in the nave, facing south down it. */
	private static final double POSE_X = 17.5;
	private static final double POSE_Z = 18.5;

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

			// An instance opens: the chapel is placed and he kneels at his lectern; the player arrives in the narthex.
			server.runOnServer(minecraft -> {
				LairInstance instance = Lairs.open(minecraft, CHAPEL);
				check(instance != null, "No Glass Chapel opened");
				check(Lairs.enter(player(minecraft), instance), "The player did not go in");
			});
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			BlockPos o = CHAPEL.origin(0);
			String dim = CHAPEL.dimension.identifier().toString();
			int stand = GlassChapel.STAND;
			Vec3 at = new Vec3(o.getX() + POSE_X, o.getY() + stand, o.getZ() + POSE_Z);
			server.runOnServer(minecraft -> {
				GlassAbbotEntity abbot = abbot(minecraft);
				check(abbot.phase() == GlassAbbotEntity.Phase.WAITING, "He is not kneeling but " + abbot.phase());
				check(abbot.position().distanceTo(abbot.place()) < 0.1, "He is not at his place but at " + abbot.position());
				check(abbot.action() == GlassAbbotEntity.Action.PRAY, "Kneeling, he is not praying");
			});
			shot(context, singleplayer, dim, o, 19.5, stand + 0.5, 14.5, GlassChapel.ABBOT_X, stand + 1.2, GlassChapel.ABBOT_Z, false,
					"jugcraft_glass_abbot_kneeling");

			// Stepping into the nave wakes him: the player, in survival in the nave, and he rises; caught as he stirs.
			server.runCommand("effect give @a minecraft:resistance infinite 4 true");
			server.runCommand("effect give @a minecraft:saturation infinite 0 true");
			server.runCommand(String.format(Locale.ROOT, "execute in %s run tp @a %.1f %d %.1f 180 0", dim, o.getX() + 17.5, o.getY() + stand,
					o.getZ() + 24.5));
			server.runCommand("gamemode survival @a");
			context.waitTicks(22);
			server.runCommand("tick freeze");
			server.runOnServer(minecraft -> check(abbot(minecraft).phase() == GlassAbbotEntity.Phase.WAKING,
					"Stepping into the nave did not wake him: " + abbot(minecraft).phase()));
			shot(context, singleplayer, dim, o, 19.5, stand + 0.5, 15.5, GlassChapel.ABBOT_X, stand + 1.6, GlassChapel.ABBOT_Z, true,
					"jugcraft_glass_abbot_wakes");
			// The player stands out of the fight (in creative) while he rises, so he has no foe; then back in for his attacks.
			server.runCommand("gamemode creative @a");
			server.runCommand("tick unfreeze");
			context.waitTicks(GlassAbbotEntity.WAKE_TICKS + 10);
			server.runOnServer(minecraft -> check(abbot(minecraft).phase() == GlassAbbotEntity.Phase.VESPERS,
					"He did not stand to fight: " + abbot(minecraft).phase()));
			server.runCommand("gamemode survival @a");

			// His attacks, each caught in its wind-up, he in the nave facing south, the player six blocks in front of him.
			pose(context, server, at, (abbot, level, player) -> abbot.begin(level, Attack.SHARD_FAN, player), Attack.SHARD_FAN.windup - 3);
			server.runOnServer(minecraft -> check(abbot(minecraft).action() == GlassAbbotEntity.Action.SHARD_WINDUP,
					"He did not raise his hand for the Shard Fan: " + abbot(minecraft).action()));
			shotFrom(context, singleplayer, dim, at, 4.0, 0.5, 5.0, 0.0, 2.0, 0.0, "jugcraft_glass_abbot_shard_fan");
			pose(context, server, at, (abbot, level, player) -> abbot.begin(level, Attack.PRISM_LINE, player), Attack.PRISM_LINE.windup - 4);
			server.runOnServer(minecraft -> check(abbot(minecraft).action() == GlassAbbotEntity.Action.PRISM_WINDUP,
					"He did not aim his Prism Line: " + abbot(minecraft).action()));
			shotFrom(context, singleplayer, dim, at, -4.0, 0.5, 4.0, 0.0, 1.8, 0.0, "jugcraft_glass_abbot_prism_line");

			// His rule: the east and south mirrors marked (their frames burn), the player turns each to the pair's stop; he goes
			// back to his place and gathers the sun, and his light comes home off both: his mantle cracks.
			pose(context, server, at, (abbot, level, player) -> {
				abbot.mark(level, Mirror.EAST, Mirror.SOUTH, Mirror.EAST);
				turnTo(level, o, Mirror.EAST, Mirror.EAST.stopFor(Mirror.SOUTH), player);
				turnTo(level, o, Mirror.SOUTH, Mirror.SOUTH.stopFor(Mirror.EAST), player);
			}, 10);
			server.runOnServer(minecraft -> {
				ServerLevel level = lair(minecraft);
				for (Mirror mirror : List.of(Mirror.EAST, Mirror.SOUTH)) {
					check(level.getBlockState(mirror.base(o)).getValue(ChapelMirrorBlock.MARKED), "The " + mirror.getSerializedName() + " mirror is not marked");
				}
				check(GlassChapel.comesHome(GlassChapel.stops(level, o), Mirror.EAST, Mirror.SOUTH), "The turned mirrors would not send his light home");
			});
			shot(context, singleplayer, dim, o, 23.5, stand + 1.0, 26.5, Mirror.EAST.middleX(), stand + 1.0, Mirror.EAST.middleZ(), true,
					"jugcraft_glass_abbot_marked");
			server.runCommand("tick unfreeze");
			server.runOnServer(minecraft -> player(minecraft).teleportTo(o.getX() + 17.5, o.getY() + stand, o.getZ() + 26.5));
			context.waitTicks(GlassAbbotEntity.MARK_TICKS - 5);
			server.runOnServer(minecraft -> check(abbot(minecraft).beam() == GlassAbbotEntity.Beam.GATHER,
					"He did not go back to his place to gather the sun: " + abbot(minecraft).beam()));
			server.runCommand("tick freeze");
			shot(context, singleplayer, dim, o, 21.5, stand + 0.5, 15.5, GlassChapel.ABBOT_X, stand + 2.0, GlassChapel.ABBOT_Z, true,
					"jugcraft_glass_abbot_gathers");
			server.runCommand("tick unfreeze");
			context.waitTicks(GlassAbbotEntity.GATHER_TICKS + 5);
			server.runCommand("tick freeze");
			server.runOnServer(minecraft -> {
				GlassAbbotEntity abbot = abbot(minecraft);
				check(abbot.cracked() && abbot.action() == GlassAbbotEntity.Action.DAZED, "His light did not come home and crack his mantle: " + abbot.beam());
				check(abbot.participants(lair(minecraft)).contains(player(minecraft)), "Turning his mirrors into place did not count as taking part");
			});
			shot(context, singleplayer, dim, o, 19.5, stand + 0.5, 13.5, GlassChapel.ABBOT_X, stand + 1.0, GlassChapel.ABBOT_Z, true,
					"jugcraft_glass_abbot_cracked");
			server.runCommand("tick unfreeze");
			context.waitTicks(GlassAbbotEntity.CRACK_TICKS + 5);

			// The Veil, at half his health: he folds his hands, unhurt, and his images stand up round him. The Veil waits for an
			// attack under way to end, so the player stands out of the fight (in creative): with no foe he breaks off at once.
			pose(context, server, at, (abbot, level, player) -> {
				player.setGameMode(GameType.CREATIVE);
				abbot.setHealth(abbot.getMaxHealth() * GlassAbbotEntity.VEIL_AT - 1.0F);
			}, 4);
			server.runOnServer(minecraft -> {
				GlassAbbotEntity abbot = abbot(minecraft);
				check(abbot.phase() == GlassAbbotEntity.Phase.VEILING, "The Veil did not fall: " + abbot.phase() + " (attack " + abbot.attack()
						+ ", beam " + abbot.beam() + ")");
			});
			server.runCommand("tick unfreeze");
			context.waitTicks(GlassAbbotEntity.VEIL_TICKS + 4);
			server.runCommand("tick freeze");
			server.runOnServer(minecraft -> {
				GlassAbbotEntity abbot = abbot(minecraft);
				check(abbot.phase() == GlassAbbotEntity.Phase.VEIL && abbot.images().size() == GlassAbbotEntity.IMAGES_VEIL,
						"The Veil did not stand up his images: " + abbot.images().size());
			});
			shotFrom(context, singleplayer, dim, at, 0.0, 2.5, 10.0, 0.0, 1.5, 0.0, "jugcraft_glass_abbot_veil");

			// He falls: the player's loot and advancement, his images gone with him, and the Grey Mist in the apse behind his
			// lectern. Beside the mist, a stand wears the Abbot's Cowl and holds the Lantern Lens.
			server.runCommand("tick unfreeze");
			server.runOnServer(minecraft -> {
				ServerLevel level = lair(minecraft);
				GlassAbbotEntity abbot = abbot(minecraft);
				check(AbbotLoot.firstKill(player(minecraft)), "The player had already won Seen Clearly");
				abbot.took(player(minecraft), 10.0F);
				abbot.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
			});
			server.runCommand(String.format(Locale.ROOT, "execute in %s run summon minecraft:armor_stand %.1f %d %.1f {ShowArms:1b,NoBasePlate:1b,"
					+ "Rotation:[180f,0f],equipment:{head:{id:\"jugcraft:abbots_cowl\",count:1},mainhand:{id:\"jugcraft:lantern_lens\",count:1}}}",
					dim, o.getX() + 20.5, o.getY() + stand, o.getZ() + 6.5));
			context.waitTicks(6);
			server.runCommand("tick freeze");
			server.runOnServer(minecraft -> {
				ServerPlayer player = player(minecraft);
				ServerLevel level = lair(minecraft);
				check(player.getInventory().countItem(JugcraftAbbot.PRISM_SHARD) >= 2, "No Prism Shards");
				check(player.getInventory().countItem(JugcraftAbbot.GLASS_RELIQUARY) == 1, "A first win must bring the Glass Reliquary");
				check(!AbbotLoot.firstKill(player), "Seen Clearly was not awarded");
				for (BlockPos cell : GlassAbbotEntity.fallExitCells()) {
					check(level.getBlockState(o.offset(cell)).is(JugcraftLairs.LAIR_EXIT), "No Grey Mist in the apse at " + cell);
				}
				check(level.getEntitiesOfClass(GlassImageEntity.class, slot()).isEmpty(), "His images outlived him");
			});
			shot(context, singleplayer, dim, o, 17.5, stand + 0.5, 13.5, 17.5, stand + 1.5, GlassAbbotEntity.FALL_EXIT_Z + 0.5, true,
					"jugcraft_glass_abbot_defeated");
			server.runCommand("tick unfreeze");
			server.runCommand(String.format(Locale.ROOT, "execute in %s run kill @e[type=minecraft:armor_stand]", dim));
			context.waitTicks(30);
			server.runOnServer(minecraft -> check(lair(minecraft).getEntitiesOfClass(GlassAbbotEntity.class, slot()).stream()
					.noneMatch(GlassAbbotEntity::isAlive), "He is still there"));

			// A fresh instance in his slot is placed anew: the Grey Mist he left is gone, and, once the player has gone in again
			// (into the narthex, far from him), he kneels at his lectern again.
			server.runOnServer(minecraft -> {
				LairInstance used = Lairs.instance(CHAPEL, 0);
				check(used != null, "His instance is not open");
				Lairs.close(minecraft, used);
				LairInstance fresh = Lairs.open(minecraft, CHAPEL);
				check(fresh != null && fresh.slot == 0, "No fresh instance opened in his slot");
				ServerLevel level = lair(minecraft);
				for (BlockPos cell : GlassAbbotEntity.fallExitCells()) {
					check(!level.getBlockState(o.offset(cell)).is(JugcraftLairs.LAIR_EXIT), "His Grey Mist outlived its instance at " + cell);
				}
				check(Lairs.enter(player(minecraft), fresh), "The player did not go into the fresh chapel");
			});
			context.waitTicks(40);
			server.runOnServer(minecraft -> {
				GlassAbbotEntity again = abbot(minecraft);
				check(again.phase() == GlassAbbotEntity.Phase.WAITING && again.position().distanceTo(again.place()) < 0.1,
						"He does not kneel at the fresh chapel's lectern: " + again.phase() + " at " + again.position());
			});
			System.out.println("[abbot] client game test: his fight passed end to end");
			server.runOnServer(minecraft -> Lairs.reset());
		}
	}

	@FunctionalInterface
	private interface Posing {
		void pose(GlassAbbotEntity abbot, ServerLevel level, ServerPlayer player);
	}

	/** {@code player} turns {@code mirror} of the chapel at {@code o} until it stands at {@code stop}. */
	private static void turnTo(ServerLevel level, BlockPos o, Mirror mirror, int stop, ServerPlayer player) {
		BlockPos base = mirror.base(o);
		for (int i = 0; i < Mirror.STOPS && ChapelMirrorBlock.stop(level, base) != stop; i++) {
			ChapelMirrorBlock.turn(level, base, player);
		}
	}

	/**
	 * Unfreezes the world, puts him at {@code spot} facing south and the player six blocks in front of him (so his attacks
	 * are wound up in view), does what {@code posing} asks, waits {@code ticks} for the pose and freezes the world on it.
	 * Every pose waits at least 10 ticks: the client learns where he was put only on the server's next updates, and a
	 * frozen world sends none.
	 */
	private static void pose(ClientGameTestContext context, TestServerContext server, Vec3 spot, Posing posing, int ticks) {
		server.runCommand("tick unfreeze");
		server.runOnServer(minecraft -> {
			ServerLevel level = lair(minecraft);
			GlassAbbotEntity abbot = abbot(minecraft);
			ServerPlayer player = player(minecraft);
			player.teleportTo(spot.x, spot.y, spot.z + 6.0);
			abbot.teleportTo(spot.x, spot.y, spot.z);
			abbot.setYRot(0.0F);
			abbot.setYBodyRot(0.0F);
			abbot.setYHeadRot(0.0F);
			posing.pose(abbot, level, player);
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

	/** A picture from (x, y, z) in the chapel's own coordinates (the camera's feet), turned to look at (ax, ay, az) in them. */
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
		return minecraft.getLevel(CHAPEL.dimension);
	}

	/** The first slot's chapel, with room round it. */
	private static AABB slot() {
		BlockPos o = CHAPEL.origin(0);
		return new AABB(o.getX() - 8, o.getY() - 16, o.getZ() - 8, o.getX() + CHAPEL.width + 8, o.getY() + CHAPEL.height + 16,
				o.getZ() + CHAPEL.length + 8);
	}

	/** He, alive, in the first slot's chapel. */
	private static GlassAbbotEntity abbot(MinecraftServer minecraft) {
		List<GlassAbbotEntity> found = lair(minecraft).getEntitiesOfClass(GlassAbbotEntity.class, slot(), GlassAbbotEntity::isAlive);
		check(!found.isEmpty(), "The Glass Abbot is not in his chapel");
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
