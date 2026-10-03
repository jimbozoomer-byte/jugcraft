package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.LeafBlowerItem;
import io.github.jimbozoomer.jugcraft.agriculture.LeafPileBlock;
import io.github.jimbozoomer.jugcraft.tools.Chargeable;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Client game test for the leaf blower (fall addition 30): a lawn strewn with leaf piles; the blower held; blowing them
 * up the lawn, seen from behind; the heap they make; vacuuming them up; and the blower itself up close. CI job
 * {@code client}.
 */
public class LeafBlowerClientGameTests implements FabricClientGameTest {
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
			server.runCommand("time set 1000");
			server.runCommand("weather clear");
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 10, y - 3, z - 16, x + 10, y - 1, z + 6));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 10, y, z - 16, x + 10, y + 10, z + 6));
			context.waitTicks(10);
			server.runOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				build(level, origin);
				ServerPlayer player = minecraft.getPlayerList().getPlayers().get(0);
				player.getInventory().clearContent();
				ItemStack blower = new ItemStack(JugcraftAgriculture.item(LeafBlowerItem.ID));
				Chargeable.setEnergy(blower, LeafBlowerItem.CAPACITY);
				player.setItemInHand(InteractionHand.MAIN_HAND, blower);
			});
			context.waitTicks(20);

			// Held, with the hotbar showing its charge.
			place(context, singleplayer, x, y, z + 2, 180, 20);
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_leaf_blower");
			context.runOnClient(client -> {
				if (!client.gui.hud.isHidden()) {
					client.gui.hud.toggle();
				}
			});

			// Blowing the leaves up the lawn, seen from behind.
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
			context.getInput().holdKey(options -> options.keyUse);
			context.waitTicks(40);
			context.takeScreenshot("jugcraft_leaf_blower_blowing");
			context.waitTicks(100);
			context.getInput().releaseKey(options -> options.keyUse);
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));

			// The heap they make at the end of the lawn.
			place(context, singleplayer, x + 4, y + 2, z - 3, 225, 30);
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_leaf_blower_heap");

			// Vacuuming it up: sneak and blow.
			place(context, singleplayer, x, y, z - 6, 180, 30);
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
			context.getInput().holdKey(options -> options.keyShift);
			context.getInput().holdKey(options -> options.keyUse);
			context.waitTicks(30);
			context.takeScreenshot("jugcraft_leaf_blower_vacuum");
			context.getInput().releaseKey(options -> options.keyUse);
			context.getInput().releaseKey(options -> options.keyShift);
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));

			// The blower itself, three times life size on a stand.
			server.runCommand(String.format(Locale.ROOT,
					"summon minecraft:item_display %.1f %.1f %.1f {Rotation:[90f,0f],item:{id:\"jugcraft:%s\",count:1},item_display:\"fixed\","
							+ "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[3f,3f,3f]}}",
					x + 6.5, y + 1.5, z + 3.5, LeafBlowerItem.ID));
			place(context, singleplayer, x + 4, y, z + 3, -90, 10);
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_leaf_blower_model");
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

	/**
	 * The lawn: leaf piles of the three colours, one to three layers deep, strewn in front of the blower (north of it),
	 * a hedge of oak leaves at the far end and a hay bale with a pumpkin by the side.
	 */
	private static void build(ServerLevel level, BlockPos origin) {
		String[] colours = JugcraftAgriculture.LEAF_PILE_COLOURS.toArray(new String[0]);
		for (int dz = 1; dz <= 6; dz++) {
			for (int dx = -2; dx <= 2; dx++) {
				if ((dx * 7 + dz * 3) % 4 == 0) {
					continue;
				}
				Block pile = JugcraftAgriculture.block(colours[Math.floorMod(dx * 5 + dz, colours.length)] + "_leaf_pile");
				int layers = 1 + Math.floorMod(dx + dz * 2, 3);
				level.setBlock(origin.offset(dx, 0, -dz), pile.defaultBlockState().setValue(LeafPileBlock.LAYERS, layers), Block.UPDATE_ALL);
			}
		}
		for (int dx = -5; dx <= 5; dx++) {
			level.setBlock(origin.offset(dx, 0, -12), Blocks.OAK_LEAVES.defaultBlockState(), Block.UPDATE_ALL);
			level.setBlock(origin.offset(dx, 1, -12), Blocks.OAK_LEAVES.defaultBlockState(), Block.UPDATE_ALL);
		}
		level.setBlock(origin.offset(-5, 0, -4), Blocks.HAY_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(origin.offset(-5, 1, -4), Blocks.CARVED_PUMPKIN.defaultBlockState(), Block.UPDATE_ALL);
	}
}
