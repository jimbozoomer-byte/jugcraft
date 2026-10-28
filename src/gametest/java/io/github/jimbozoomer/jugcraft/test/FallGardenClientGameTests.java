package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PlanterBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PlanterBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.WallDecorationBlock;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Client game test for Fall Garden & Porch, part 1 (decor set 4): a fall border on grass, asters and stonecrop behind
 * ornamental kale and cabbage, pampas grass and dried sunflowers at its back, a potted aster on a log, and bittersweet
 * climbing a wall of logs; and part 2's planters beside it, a Window Box of three flowers on the logs, a Half-Barrel of
 * kale and a Galvanized Tub of two flowers; and part 3's porch behind the cameras, under a plank roof: the Porch Swing
 * hung from it, a Bittersweet Wreath and a Corn Dolly on its back wall, Quilt Rails along its front and a Wheat Stook at
 * its corner; built in a real client and photographed from the front, close by, at the planters and at the porch, by day
 * (CI job {@code client}). The swing is drawn by client/PorchSwingRenderer. The planters' flowers are drawn by client/PlanterRenderer, so the pictures
 * show it draws them. The pictures go into docs/features/fall-garden-and-porch.md.
 */
public class FallGardenClientGameTests implements FabricClientGameTest {
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
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 4, y - 1, z - 4, x + 10, y - 1, z + 19));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 4, y, z - 4, x + 10, y + 6, z + 19));
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			shoot(context, singleplayer, x + 3, y + 1, z + 8, 180, 20, "jugcraft_fall_garden");
			shoot(context, singleplayer, x + 2, y + 1, z + 4, 180, 35, "jugcraft_fall_garden_close");
			shoot(context, singleplayer, x + 9, y + 1, z + 4, 180, 25, "jugcraft_fall_garden_planters");
			shoot(context, singleplayer, x + 4, y + 1, z + 18, 180, 10, "jugcraft_fall_garden_porch");
		}
	}

	/** Stands the player at a spot (on an invisible barrier) and takes a screenshot. */
	private static void shoot(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z,
			int yaw, int pitch, String name) {
		TestServerContext server = singleplayer.getServer();
		server.runCommand("setblock %d %d %d minecraft:barrier".formatted(x, y - 1, z));
		server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f %d %d", x + 0.5, y, z + 0.5, yaw, pitch));
		context.waitTicks(40);
		singleplayer.getConnection().waitForChunksRender();
		context.takeScreenshot(name);
	}

	private static void tall(ServerLevel level, String id, BlockPos at) {
		BlockState lower = JugcraftAgriculture.block(id).defaultBlockState();
		level.setBlock(at, lower.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), Block.UPDATE_CLIENTS);
		level.setBlock(at.above(), lower.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
	}

	private static void planter(ServerLevel level, String id, BlockPos at, Item... flowers) {
		level.setBlock(at, JugcraftAgriculture.block(id).defaultBlockState().setValue(PlanterBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
		if (level.getBlockEntity(at) instanceof PlanterBlockEntity planted) {
			for (Item flower : flowers) {
				planted.plant(new ItemStack(flower));
			}
		}
	}

	private static void build(ServerLevel level, BlockPos origin) {
		// The back of the border: pampas grass and dried sunflowers, against a wall of logs with bittersweet on it.
		for (int across = -1; across <= 7; across++) {
			for (int up = 0; up < 3; up++) {
				level.setBlock(origin.offset(across, up, -2), Blocks.SPRUCE_LOG.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		BlockState vine = JugcraftAgriculture.block("bittersweet").defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.NORTH), true);
		for (int across : new int[] {0, 1, 5, 6}) {
			for (int up = 0; up < (across % 5 == 0 ? 3 : 2); up++) {
				level.setBlock(origin.offset(across, up, -1), vine, Block.UPDATE_ALL);
			}
		}
		tall(level, "pampas_grass", origin.offset(2, 0, -1));
		tall(level, "pampas_grass", origin.offset(4, 0, -1));
		tall(level, "dried_sunflower", origin.offset(3, 0, -1));
		tall(level, "dried_sunflower", origin.offset(7, 0, -1));
		// The middle: asters and stonecrop; the front: kale and cabbage by turns.
		for (int across = 0; across <= 6; across++) {
			level.setBlock(origin.offset(across, 0, 0), JugcraftAgriculture.block(across % 3 == 1 ? "sedum" : "aster").defaultBlockState(),
					Block.UPDATE_ALL);
			level.setBlock(origin.offset(across, 0, 1),
					JugcraftAgriculture.block(across % 2 == 0 ? "ornamental_kale" : "ornamental_cabbage").defaultBlockState(), Block.UPDATE_ALL);
		}
		// Part 2, at the end of the border: the logs run on, a window box on them, and a half-barrel and a tub before it.
		for (int across = 8; across <= 10; across++) {
			for (int up = 0; up < 3; up++) {
				level.setBlock(origin.offset(across, up, -2), Blocks.SPRUCE_LOG.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		planter(level, "window_box", origin.offset(9, 1, -1), Items.POPPY, JugcraftAgriculture.item("aster"), JugcraftAgriculture.item("sedum"));
		planter(level, "half_barrel_planter", origin.offset(8, 0, 1), JugcraftAgriculture.item("ornamental_kale"));
		planter(level, "galvanized_tub_planter", origin.offset(10, 0, 1), Items.CORNFLOWER, JugcraftAgriculture.item("aster"));
		// Part 3, south of every other camera: a porch of plank roof on log posts, a plank back wall, the swing hung from the
		// roof facing south, the wreath and the dolly on the wall, quilt rails along the front and a stook at the corner.
		for (int across = 1; across <= 7; across++) {
			for (int deep = 10; deep <= 14; deep++) {
				level.setBlock(origin.offset(across, 2, deep), Blocks.SPRUCE_PLANKS.defaultBlockState(), Block.UPDATE_ALL);
			}
			for (int up = 0; up < 2; up++) {
				level.setBlock(origin.offset(across, up, 10), Blocks.SPRUCE_PLANKS.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		for (int up = 0; up < 2; up++) {
			level.setBlock(origin.offset(1, up, 14), Blocks.SPRUCE_LOG.defaultBlockState(), Block.UPDATE_ALL);
			level.setBlock(origin.offset(7, up, 14), Blocks.SPRUCE_LOG.defaultBlockState(), Block.UPDATE_ALL);
		}
		MultiDecorationBlock swing = (MultiDecorationBlock) JugcraftAgriculture.block("porch_swing");
		BlockState hung = swing.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.SOUTH);
		level.setBlock(origin.offset(3, 0, 12), hung, Block.UPDATE_ALL);
		swing.setPlacedBy(level, origin.offset(3, 0, 12), hung, null, new ItemStack(swing));
		level.setBlock(origin.offset(2, 1, 11), JugcraftAgriculture.block("bittersweet_wreath").defaultBlockState()
				.setValue(WallDecorationBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
		level.setBlock(origin.offset(6, 1, 11), JugcraftAgriculture.block("corn_dolly").defaultBlockState()
				.setValue(WallDecorationBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
		for (int across : new int[] {2, 6}) {
			level.setBlock(origin.offset(across, 0, 14), JugcraftAgriculture.block("quilt_rail").defaultBlockState()
					.setValue(FacingDecorationBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
		}
		level.setBlock(origin.offset(0, 0, 13), JugcraftAgriculture.block("wheat_stook").defaultBlockState(), Block.UPDATE_ALL);
		// A potted aster on a log by the border.
		level.setBlock(origin.offset(-1, 0, 1), Blocks.SPRUCE_LOG.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(origin.offset(-1, 1, 1), JugcraftAgriculture.block("potted_aster").defaultBlockState(), Block.UPDATE_ALL);
	}
}
