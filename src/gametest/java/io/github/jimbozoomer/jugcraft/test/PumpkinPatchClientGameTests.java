package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.GourdStemBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.ProduceDisplayBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ProduceDisplayBlockEntity;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Client game test for the Pumpkin Patch Market's squashes and gourds (decor set 1, part 1): the six laid out in a row on
 * a market's boards, and a patch of them growing on their stems, built in a real client and photographed close, from
 * behind and over the patch, by day; and part 2's displays (the Pumpkin Wheelbarrow, Wagon and Pyramid) filled with them,
 * from the front and the side (CI job {@code client}). The pictures go into docs/features/pumpkin-patch-market.md.
 */
public class PumpkinPatchClientGameTests implements FabricClientGameTest {
	/** The six, as tools/pumpkin_patch.py VARIETIES lists them. */
	private static final String[] VARIETIES = {"turban_squash", "blue_hubbard_squash", "sweet_dumpling_squash", "long_island_cheese_pumpkin",
			"spoon_gourd", "swan_gourd"};

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
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 4, y - 1, z - 18, x + 16, y - 1, z + 26));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 4, y, z - 18, x + 16, y + 10, z + 26));
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();

			shoot(context, singleplayer, x + 5, y + 1, z + 4, 180, 30, "jugcraft_pumpkin_patch_varieties");
			shoot(context, singleplayer, x + 5, y + 1, z - 4, 0, 30, "jugcraft_pumpkin_patch_varieties_back");
			shoot(context, singleplayer, x + 6, y + 6, z - 3, 180, 50, "jugcraft_pumpkin_patch_field");
			server.runOnServer(minecraft -> displays(minecraft.overworld(), origin.offset(0, 0, 12)));
			context.waitTicks(20);
			shoot(context, singleplayer, x + 5, y + 3, z + 21, 180, 18, "jugcraft_pumpkin_patch_displays");
			shoot(context, singleplayer, x + 13, y + 3, z + 9, 90, 22, "jugcraft_pumpkin_patch_wagon_side");
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

	private static void set(ServerLevel level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state, Block.UPDATE_ALL);
	}

	private static BlockState gourd(String id, Direction facing) {
		return JugcraftAgriculture.block(id).defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
	}

	private static void build(ServerLevel level, BlockPos origin) {
		// The market row: the six on spruce boards, two apart, the lying ones turned along the row.
		for (int i = 0; i < VARIETIES.length; i++) {
			BlockPos spot = origin.offset(i * 2, 0, 0);
			set(level, spot.below(), Blocks.SPRUCE_PLANKS.defaultBlockState());
			set(level, spot, gourd(VARIETIES[i], Direction.SOUTH));
		}
		// The patch: a row of stems on farmland for each, each grown stem bent to the gourd it grew beside it.
		for (int i = 0; i < VARIETIES.length; i++) {
			int dz = -8 - (i / 3) * 5;
			for (int k = 0; k < 2; k++) {
				BlockPos stem = origin.offset((i % 3) * 5 + k * 2, 0, dz);
				set(level, stem.below(), Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 7));
				GourdStemBlock block = (GourdStemBlock) JugcraftAgriculture.block(VARIETIES[i] + "_stem");
				set(level, stem, block.defaultBlockState().setValue(GourdStemBlock.AGE, GourdStemBlock.MAX_AGE));
				block.growGourd(level, stem, k == 0 ? Direction.NORTH : Direction.SOUTH);
			}
		}
	}

	/** The three displays in a row, facing south (towards the camera), each filled with the six and vanilla pumpkins. */
	private static void displays(ServerLevel level, BlockPos at) {
		String[] ids = {"pumpkin_wheelbarrow", "pumpkin_wagon", "pumpkin_pyramid"};
		int[] xs = {0, 3, 7};
		for (int i = 0; i < ids.length; i++) {
			ProduceDisplayBlock block = (ProduceDisplayBlock) JugcraftAgriculture.block(ids[i]);
			BlockPos master = at.offset(xs[i], 0, 0);
			BlockState state = block.defaultBlockState().setValue(ProduceDisplayBlock.FACING, Direction.SOUTH);
			set(level, master, state);
			block.setPlacedBy(level, master, state, null, new ItemStack(block));
			if (level.getBlockEntity(master) instanceof ProduceDisplayBlockEntity display) {
				for (int piece = 0; piece < display.capacity(); piece++) {
					display.add(piece % 7 == 6 ? new ItemStack(Items.PUMPKIN) : new ItemStack(JugcraftAgriculture.item(VARIETIES[piece % 7 % VARIETIES.length])));
				}
			}
		}
	}
}
