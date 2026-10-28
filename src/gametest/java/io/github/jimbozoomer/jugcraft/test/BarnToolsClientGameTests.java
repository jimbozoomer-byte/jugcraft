package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.ButterChurnBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ButterChurnBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.DryingRackBlock;
import io.github.jimbozoomer.jugcraft.agriculture.DryingRackBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.FruitStoreBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.HayPulleyBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HandWorkedBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.QuernBlock;
import io.github.jimbozoomer.jugcraft.agriculture.QuernBlockEntity;
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
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client game test for Barn & Harvest Tools, part 2 (decor set 3): two Querns on a plank floor, one with wheat in its
 * eye, and two Butter Churns, one holding milk; and part 3's Drying Rack hung with peppers and sage and a Corn Crib half
 * full; and part 4's Hay-Loft Pulley on a wall, its rope let down three blocks, and the Harvest Bell; built in a real
 * client and photographed from the front, close by, at the rack and crib and at the pulley and bell, by day (CI job
 * {@code client}). The moving parts (the quern's upper stone and peg, the churn's dasher) are drawn by their
 * renderer, so the pictures show it draws them. The pictures go into docs/features/barn-and-harvest-tools.md.
 */
public class BarnToolsClientGameTests implements FabricClientGameTest {
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
			server.runCommand("fill %d %d %d %d %d %d minecraft:spruce_planks".formatted(x - 4, y - 1, z - 4, x + 12, y - 1, z + 17));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 4, y, z - 4, x + 12, y + 6, z + 17));
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			shoot(context, singleplayer, x + 2, y + 1, z + 6, 180, 25, "jugcraft_barn_tools");
			shoot(context, singleplayer, x + 1, y + 1, z + 3, 180, 40, "jugcraft_barn_tools_close");
			shoot(context, singleplayer, x + 2, y + 2, z + 15, 180, 15, "jugcraft_barn_tools_storage");
			shoot(context, singleplayer, x + 8, y + 2, z + 17, 180, 10, "jugcraft_barn_tools_loft");
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

	/** A station facing south, towards the camera. */
	private static BlockState facingSouth(String id) {
		return JugcraftAgriculture.block(id).defaultBlockState().setValue(HandWorkedBlock.FACING, Direction.SOUTH);
	}

	/** Places a prop of several blocks facing south (towards the camera) with its first block at {@code master}. */
	private static void prop(ServerLevel level, String id, BlockPos master) {
		MultiDecorationBlock block = (MultiDecorationBlock) JugcraftAgriculture.block(id);
		BlockState state = block.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.SOUTH);
		level.setBlock(master, state, Block.UPDATE_ALL);
		block.setPlacedBy(level, master, state, null, new ItemStack(block));
	}

	private static void build(ServerLevel level, BlockPos origin) {
		BlockPos loaded = origin.offset(0, 0, 1);
		level.setBlock(loaded, facingSouth("quern").setValue(QuernBlock.LOADED, true), Block.UPDATE_ALL);
		if (level.getBlockEntity(loaded) instanceof QuernBlockEntity quern) {
			quern.add(new ItemStack(Items.WHEAT, 12));
		}
		level.setBlock(origin.offset(1, 0, 1), facingSouth("quern"), Block.UPDATE_ALL);
		BlockPos full = origin.offset(3, 0, 1);
		level.setBlock(full, facingSouth("butter_churn").setValue(ButterChurnBlock.FULL, true), Block.UPDATE_ALL);
		if (level.getBlockEntity(full) instanceof ButterChurnBlockEntity churn) {
			churn.fill();
		}
		level.setBlock(origin.offset(4, 0, 1), facingSouth("butter_churn"), Block.UPDATE_ALL);
		// Part 3, behind the first two pictures' cameras: the Drying Rack, a bunch of peppers and one of sage on its lower rail and peppers above; the Corn Crib half full.
		BlockPos rack = origin.offset(-2, 0, 9);
		prop(level, "drying_rack", rack);
		if (level.getBlockEntity(rack) instanceof DryingRackBlockEntity hooks) {
			hooks.hang(new ItemStack(JugcraftAgriculture.item("pepper"), 4), level.getGameTime());
			hooks.hang(new ItemStack(JugcraftAgriculture.item("sage"), 4), level.getGameTime());
			hooks.hang(new ItemStack(JugcraftAgriculture.item("pepper"), 4), level.getGameTime());
		}
		((DryingRackBlock) JugcraftAgriculture.block("drying_rack")).show(level, rack);
		BlockPos crib = origin.offset(4, 0, 9);
		prop(level, "corn_crib", crib);
		if (level.getBlockEntity(crib) instanceof FruitStoreBlockEntity store) {
			store.add(new ItemStack(JugcraftAgriculture.item("corn"), 64));
			store.add(new ItemStack(JugcraftAgriculture.item("corn"), 64));
		}
		// Part 4: the Harvest Bell, and the Hay-Loft Pulley on a wall of planks, its rope three blocks down to hay bales.
		prop(level, "harvest_bell", origin.offset(6, 0, 12));
		for (int up = 0; up < 4; up++) {
			level.setBlock(origin.offset(9, up, 11), Blocks.SPRUCE_PLANKS.defaultBlockState(), Block.UPDATE_ALL);
		}
		level.setBlock(origin.offset(9, 3, 12), JugcraftAgriculture.block("hay_pulley").defaultBlockState()
				.setValue(HayPulleyBlock.FACING, Direction.SOUTH).setValue(HayPulleyBlock.DROP, 3), Block.UPDATE_ALL);
		level.setBlock(origin.offset(9, 0, 13), Blocks.HAY_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(origin.offset(10, 0, 13), Blocks.HAY_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
		// A hay bale and a barrel behind them, for scale.
		level.setBlock(origin.offset(2, 0, -1), Blocks.HAY_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(origin.offset(5, 0, -1), Blocks.BARREL.defaultBlockState(), Block.UPDATE_ALL);
	}
}
