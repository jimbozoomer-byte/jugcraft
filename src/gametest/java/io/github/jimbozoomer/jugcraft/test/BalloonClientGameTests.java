package io.github.jimbozoomer.jugcraft.test;

import com.mojang.authlib.GameProfile;
import io.github.jimbozoomer.jugcraft.agriculture.BalloonControlPayload;
import io.github.jimbozoomer.jugcraft.agriculture.HotAirBalloon;
import io.github.jimbozoomer.jugcraft.agriculture.HotAirBalloonItem;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MooringPostBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PibalItem;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Client game test for the hot-air balloon fiesta (fall addition 29): three balloons over a field (Harvest Stripes on
 * the ground, the Jack-o'-Lantern moored to a post, Harvest Moon aloft) with pibals rising; a basket up close; the view
 * from the moored basket; its riders seen from outside; and the night glow, the burners firing. The balloons aloft are
 * piloted by server-side stand-ins (Fabric's FakePlayer), so their burners can fire while the camera is elsewhere. CI job
 * {@code client}.
 */
public class BalloonClientGameTests implements FabricClientGameTest {
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
			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			int x = origin.getX();
			int y = origin.getY();
			int z = origin.getZ();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("time set 1000");
			server.runCommand("weather clear");
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 24, y - 3, z - 34, x + 24, y - 1, z + 16));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 24, y, z - 34, x + 24, y + 30, z + 16));
			context.waitTicks(10);
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			// Pibals let go a second apart, rising at different heights.
			for (int i = 0; i < 3; i++) {
				int k = i;
				server.runOnServer(minecraft -> PibalItem.release(minecraft.overworld(), Vec3.atCenterOf(origin.offset(4 + k, 1, 2))));
				context.waitTicks(20);
			}
			context.waitTicks(20);
			singleplayer.getConnection().waitForChunksRender();

			shoot(context, singleplayer, x, y + 2, z + 14, 180, -18, "jugcraft_balloons");
			shoot(context, singleplayer, x - 8, y, z - 6, 180, -12, "jugcraft_balloon_basket");

			// Aboard the moored Jack-o'-Lantern beside its pilot, looking out over the field; then seen from outside.
			place(context, singleplayer, x + 2, y, z - 12, 180, 0);
			server.runCommand("ride @p mount @e[type=jugcraft:hot_air_balloon,limit=1,sort=nearest]");
			context.waitTicks(20);
			server.runCommand("rotate @p 220 5");
			context.waitTicks(20);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_balloon_ride");
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			server.runCommand("rotate @p 0 -10");
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_balloon_riders");
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));
			server.runCommand("ride @p dismount");
			context.waitTicks(10);

			// The night glow: the pilots fire their burners.
			server.runCommand("time set 18000");
			server.runOnServer(minecraft -> fire(minecraft.overworld(), origin, true));
			context.waitTicks(10);
			shoot(context, singleplayer, x, y + 2, z + 14, 180, -18, "jugcraft_balloons_night");
			server.runOnServer(minecraft -> fire(minecraft.overworld(), origin, false));
			server.runCommand("time set 1000");
		}
	}

	/** Stands the camera at (x, y, z) looking along yaw and pitch, and waits for the world to draw. */
	private static void place(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z, int yaw, int pitch) {
		TestServerContext server = singleplayer.getServer();
		server.runCommand("fill %d %d %d %d %d %d minecraft:barrier replace minecraft:air".formatted(x, y - 1, z, x, y - 1, z));
		server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f %d %d", x + 0.5, y, z + 0.5, yaw, pitch));
		context.waitTicks(20);
		singleplayer.getConnection().waitForChunksRender();
	}

	private static void shoot(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z, int yaw, int pitch,
			String name) {
		place(context, singleplayer, x, y, z, yaw, pitch);
		context.waitTicks(20);
		context.takeScreenshot(name);
	}

	/** Stand-in pilot {@code i}: the same one each time it is asked for. */
	private static FakePlayer pilot(ServerLevel level, int i) {
		return FakePlayer.get(level, new GameProfile(UUID.nameUUIDFromBytes(("jugcraft-balloon-pilot-" + i).getBytes()), "Pilot" + i));
	}

	/** Every balloon with a stand-in pilot fires its burner, or stops. */
	private static void fire(ServerLevel level, BlockPos origin, boolean on) {
		for (int i = 0; i < 2; i++) {
			BalloonControlPayload.apply(pilot(level, i), on, false);
		}
	}

	/**
	 * The fiesta field: Harvest Stripes cold on the ground; a Mooring Post with the Jack-o'-Lantern tied to it, a few
	 * blocks up; Harvest Moon aloft further off. The two aloft have stand-in pilots, fuel, and heat to float.
	 */
	private static void build(ServerLevel level, BlockPos origin) {
		BlockPos ground = origin.below();
		HotAirBalloonItem.setUp(level, ground.offset(-8, 0, -12), HotAirBalloon.Kind.HARVEST, 0.0F, 400);
		BlockPos post = origin.offset(-1, 0, -14);
		level.setBlock(post, JugcraftAgriculture.block(MooringPostBlock.ID).defaultBlockState(), Block.UPDATE_ALL);
		HotAirBalloon pumpkin = HotAirBalloonItem.setUp(level, ground.offset(2, 0, -16), HotAirBalloon.Kind.PUMPKIN, 0.0F, 6000);
		HotAirBalloon moon = HotAirBalloonItem.setUp(level, ground.offset(12, 0, -26), HotAirBalloon.Kind.MOON, 0.0F, 6000);
		List<HotAirBalloon> aloft = new java.util.ArrayList<>();
		if (pumpkin != null) {
			pumpkin.setPos(pumpkin.getX(), pumpkin.getY() + 4, pumpkin.getZ());
			pumpkin.moor(post);
			aloft.add(pumpkin);
		}
		if (moon != null) {
			moon.setPos(moon.getX(), moon.getY() + 9, moon.getZ());
			aloft.add(moon);
		}
		for (int i = 0; i < aloft.size(); i++) {
			HotAirBalloon balloon = aloft.get(i);
			balloon.setHeat(HotAirBalloon.NEUTRAL + 0.02F);
			pilot(level, i).startRiding(balloon);
		}
		// A few pumpkins and hay bales about the field.
		for (int i = 0; i < 5; i++) {
			level.setBlock(origin.offset(-14 + i * 7, 0, -4 - (i % 2) * 3), (i % 2 == 0 ? Blocks.HAY_BLOCK : Blocks.PUMPKIN).defaultBlockState(),
					Block.UPDATE_ALL);
		}
	}
}
