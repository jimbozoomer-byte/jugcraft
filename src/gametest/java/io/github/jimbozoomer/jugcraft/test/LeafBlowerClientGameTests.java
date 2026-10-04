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
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Client game test for the leaf blower (fall addition 30): a lawn strewn with leaf piles in front of a fence; the
 * blower held, seen from the side; blowing, the piles herded up the lawn; the heap against the fence; vacuuming it up;
 * and the blower itself up close. The player stays where they blow while the camera watches from an invisible armor
 * stand. CI job {@code client}.
 */
public class LeafBlowerClientGameTests implements FabricClientGameTest {
	/** The side view: east of the lawn, looking west across the player and up to the fence. */
	private static final Vec3 SIDE = new Vec3(7.5, 0.5, -2.5);

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
			// Granted now, so its toast is gone before the shots.
			server.runCommand("advancement grant @a only jugcraft:gone_with_the_wind");
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 10, y - 3, z - 16, x + 10, y - 1, z + 8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 10, y, z - 16, x + 10, y + 10, z + 8));
			context.waitTicks(10);
			server.runOnServer(minecraft -> {
				build(minecraft.overworld(), origin);
				ServerPlayer player = minecraft.getPlayerList().getPlayers().get(0);
				player.getInventory().clearContent();
				ItemStack blower = new ItemStack(JugcraftAgriculture.item(LeafBlowerItem.ID));
				Chargeable.setEnergy(blower, LeafBlowerItem.CAPACITY);
				player.setItemInHand(InteractionHand.MAIN_HAND, blower);
			});
			// The player stands behind the lawn, facing up it (north), the blower aimed a little down.
			stand(context, singleplayer, x, y, z + 1, 180, 25);
			context.waitTicks(100);

			watchFrom(context, singleplayer, origin, SIDE, 90.0F, 10.0F, "jugcraft_leaf_blower");

			// Blowing: hold use. Seen from the side partway through, then the heap it makes against the fence.
			context.getInput().holdKey(options -> options.keyUse);
			context.waitTicks(40);
			watchFrom(context, singleplayer, origin, SIDE, 90.0F, 10.0F, "jugcraft_leaf_blower_blowing");
			context.waitTicks(80);
			context.getInput().releaseKey(options -> options.keyUse);
			watchFrom(context, singleplayer, origin, new Vec3(4.5, 1.5, -1.5), 135.0F, 30.0F, "jugcraft_leaf_blower_heap");

			// Vacuuming it up: step up to the heap, sneak and hold use.
			stand(context, singleplayer, x, y, z - 2, 180, 35);
			context.getInput().holdKey(options -> options.keyShift);
			context.getInput().holdKey(options -> options.keyUse);
			context.waitTicks(30);
			watchFrom(context, singleplayer, origin, new Vec3(6.5, 0.5, -3.5), 90.0F, 15.0F, "jugcraft_leaf_blower_vacuum");
			context.getInput().releaseKey(options -> options.keyUse);
			context.getInput().releaseKey(options -> options.keyShift);

			// The blower itself, twice life size, laid on its side; seen from the side.
			server.runCommand(String.format(Locale.ROOT,
					"summon minecraft:item_display %.1f %.1f %.1f {item:{id:\"jugcraft:%s\",count:1},item_display:\"fixed\","
							+ "transformation:{left_rotation:[-0.7071f,0f,0f,0.7071f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[2f,2f,2f]}}",
					x - 6.5, y + 1.8, z + 4.5, LeafBlowerItem.ID));
			watchFrom(context, singleplayer, origin, new Vec3(-3.0, 0.0, 4.5), 90.0F, 0.0F, "jugcraft_leaf_blower_model");
		}
	}

	/** Stands the player at (x, y, z) looking along yaw and pitch, and waits for the world to draw. */
	private static void stand(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z, int yaw, int pitch) {
		singleplayer.getServer().runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f %d %d", x + 0.5, y, z + 0.5, yaw, pitch));
		context.waitTicks(20);
		singleplayer.getConnection().waitForChunksRender();
	}

	/**
	 * Looks through an invisible armor stand at {@code at} (from the origin) along yaw and pitch, so the player can go on
	 * blowing while the camera stands elsewhere; then gives the camera back.
	 */
	private static void watchFrom(ClientGameTestContext context, TestSingleplayerContext singleplayer, BlockPos origin, Vec3 at, float yaw,
			float pitch, String name) {
		TestServerContext server = singleplayer.getServer();
		int id = server.computeOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			ArmorStand stand = new ArmorStand(level, origin.getX() + at.x, origin.getY() + at.y, origin.getZ() + at.z);
			stand.snapTo(stand.getX(), stand.getY(), stand.getZ(), yaw, pitch);
			stand.setYHeadRot(yaw);
			stand.setInvisible(true);
			stand.setNoGravity(true);
			level.addFreshEntity(stand);
			return stand.getId();
		});
		context.waitTicks(5);
		context.runOnClient(client -> {
			Entity stand = client.level.getEntity(id);
			if (stand != null) {
				client.setCameraEntity(stand);
			}
		});
		context.waitTicks(10);
		singleplayer.getConnection().waitForChunksRender();
		context.takeScreenshot(name);
		context.runOnClient(client -> client.setCameraEntity(client.player));
		server.runOnServer(minecraft -> {
			Entity stand = minecraft.overworld().getEntity(id);
			if (stand != null) {
				stand.discard();
			}
		});
	}

	/**
	 * The lawn: leaf piles in a checkerboard up the four rows in front of the player (north of them), a column of each
	 * colour so they heap as they go; an oak fence across the far end for them to heap against; a hay bale with a carved
	 * pumpkin by the side.
	 */
	private static void build(ServerLevel level, BlockPos origin) {
		String[] colours = JugcraftAgriculture.LEAF_PILE_COLOURS.toArray(new String[0]);
		for (int dz = 1; dz <= 4; dz++) {
			for (int dx = -2; dx <= 2; dx++) {
				if (Math.floorMod(dx + dz, 2) != 0) {
					continue;
				}
				Block pile = JugcraftAgriculture.block(colours[Math.floorMod(dx, colours.length)] + "_leaf_pile");
				int layers = 1 + Math.floorMod(dz, 2);
				level.setBlock(origin.offset(dx, 0, -dz), pile.defaultBlockState().setValue(LeafPileBlock.LAYERS, layers), Block.UPDATE_ALL);
			}
		}
		for (int dx = -3; dx <= 3; dx++) {
			level.setBlock(origin.offset(dx, 0, -7), Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
		}
		for (int dx = -5; dx <= 5; dx++) {
			level.setBlock(origin.offset(dx, 0, -10), Blocks.OAK_LEAVES.defaultBlockState(), Block.UPDATE_ALL);
			level.setBlock(origin.offset(dx, 1, -10), Blocks.OAK_LEAVES.defaultBlockState(), Block.UPDATE_ALL);
		}
		level.setBlock(origin.offset(-5, 0, -3), Blocks.HAY_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(origin.offset(-5, 1, -3), Blocks.CARVED_PUMPKIN.defaultBlockState(), Block.UPDATE_ALL);
	}
}
