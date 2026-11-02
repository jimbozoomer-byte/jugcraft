package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.world.RetroTrader;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.entity.EntityTypeTest;

/**
 * Beardshy up close, in a real client (docs/features/beardshy.md): from further off than he startles (head up), from
 * near (head bowed, shy), his face, three-quarters, and beside the Retro Trader (Beardson). Run alone with
 * {@code ./gradlew runClientGameTest -PclientTests=BeardshyClientGameTests}.
 */
public class BeardshyClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			int x = origin.getX();
			int y = origin.getY();
			int z = origin.getZ();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("gamerule sendCommandFeedback false");
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(x - 8, y - 1, z - 8, x + 8, y - 1, z + 8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 8, y, z - 8, x + 8, y + 6, z + 8));
			context.getInput().pressKey(options -> options.keyToggleGui);

			// He faces south (+z), towards the camera.
			server.runCommand("summon jugcraft:beardshy %d.5 %d %d.5 {NoAI:1b,Silent:1b,Rotation:[0f,0f]}".formatted(x, y, z));
			context.waitTicks(10);

			shoot(context, server, singleplayer, "tp @p %d.5 %d %s 180 8".formatted(x, y, (z + 5.6) + ""), "jugcraft_beardshy_look");
			shoot(context, server, singleplayer, "tp @p %d.5 %d %s 180 5".formatted(x, y, (z + 3.2) + ""), "jugcraft_beardshy_shy");
			// Crouched, as a player comes up to talk to him: low enough to see the face under his bowed head.
			context.getInput().holdKey(options -> options.keyShift);
			shoot(context, server, singleplayer, "tp @p %d.5 %d %s 180 -8".formatted(x, y, (z + 1.6) + ""), "jugcraft_beardshy_face");
			context.getInput().releaseKey(options -> options.keyShift);
			shoot(context, server, singleplayer, "tp @p %s %d %s 135 8".formatted((x + 2.6) + "", y, (z + 2.9) + ""), "jugcraft_beardshy_three_quarter");
			// From behind him (north-west of him, facing south-east).
			shoot(context, server, singleplayer, "tp @p %s %d %s -40 8".formatted((x - 2.4) + "", y, (z - 2.9) + ""), "jugcraft_beardshy_back");

			// Beardson, the Retro Trader, beside him.
			server.runCommand("summon minecraft:villager %d.5 %d %d.5 {NoAI:1b,Silent:1b,Rotation:[0f,0f]}".formatted(x + 2, y, z));
			server.runOnServer(minecraft -> makeTrader(minecraft.overworld()));
			context.waitTicks(10);
			shoot(context, server, singleplayer, "tp @p %s %d %s 180 8".formatted((x + 1.5) + "", y, (z + 5.6) + ""), "jugcraft_beardshy_and_beardson");
		}
	}

	private static void shoot(ClientGameTestContext context, TestServerContext server, TestSingleplayerContext singleplayer, String tp, String name) {
		server.runCommand(tp);
		context.waitTicks(15);
		singleplayer.getConnection().waitForChunksRender();
		context.takeScreenshot(name);
	}

	private static List<? extends Villager> villagers(ServerLevel level) {
		return level.getEntities(EntityTypeTest.forClass(Villager.class), Villager::isNoAi);
	}

	private static void makeTrader(ServerLevel level) {
		var profession = BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(RetroTrader.PROFESSION);
		for (Villager villager : villagers(level)) {
			villager.setVillagerData(villager.getVillagerData().withProfession(profession).withLevel(2));
		}
	}
}
