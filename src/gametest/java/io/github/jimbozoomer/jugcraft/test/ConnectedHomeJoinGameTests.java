package io.github.jimbozoomer.jugcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import io.github.jimbozoomer.jugcraft.menagerie.HouseholdBlock;

/**
 * OwnerHomeBlock.joined against cozyhome's own connection rule (AbstractHorizontalConnectingBlock, read with javap):
 * a diagonal needs both of its cardinal connections, and a table joins only its own block.
 */
public class ConnectedHomeJoinGameTests {
	private static Block block(String name) {
		return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("jugcraft", name));
	}

	private static void put(GameTestHelper h, BlockPos rel, String name) {
		var abs = h.absolutePos(rel);
		h.getLevel().setBlock(abs, Block.updateFromNeighbourShapes(block(name).defaultBlockState(), h.getLevel(), abs), Block.UPDATE_ALL);
	}

	private static String value(GameTestHelper h, BlockPos rel, String property) {
		return HouseholdBlock.value(h.getBlockState(rel), property);
	}

	@GameTest(maxTicks = 20)
	public void loneDiagonalNeighboursDoNotJoin(GameTestHelper h) {
		for (String name : new String[]{"homeworks_oak_table", "homeworks_andesite_fountain", "homeworks_oak_large_stump"}) {
			put(h, new BlockPos(1, 2, 1), name);
			put(h, new BlockPos(2, 2, 2), name);
			h.assertTrue(value(h, new BlockPos(2, 2, 2), "north_west").equals("false"), name + ": a lone diagonal is not a join");
			h.assertTrue(value(h, new BlockPos(2, 2, 2), "north").equals("false"), name + ": nor a cardinal one");
			h.setBlock(new BlockPos(1, 2, 1), net.minecraft.world.level.block.Blocks.AIR);
			h.setBlock(new BlockPos(2, 2, 2), net.minecraft.world.level.block.Blocks.AIR);
		}
		h.succeed();
	}

	@GameTest(maxTicks = 20)
	public void aFullSquareSetsTheLastCornersDiagonal(GameTestHelper h) {
		put(h, new BlockPos(1, 2, 1), "homeworks_oak_table");
		put(h, new BlockPos(2, 2, 1), "homeworks_oak_table");
		put(h, new BlockPos(1, 2, 2), "homeworks_oak_table");
		put(h, new BlockPos(2, 2, 2), "homeworks_oak_table");
		h.assertTrue(value(h, new BlockPos(2, 2, 2), "north_west").equals("true"), "north, west and the corner join");
		h.assertTrue(value(h, new BlockPos(2, 2, 1), "south_west").equals("true"), "updated by its south neighbour");
		h.succeed();
	}

	@GameTest(maxTicks = 20)
	public void tablesJoinOnlyTheirOwnBlock(GameTestHelper h) {
		put(h, new BlockPos(1, 2, 1), "homeworks_oak_table");
		put(h, new BlockPos(2, 2, 1), "homeworks_glass_table");
		put(h, new BlockPos(1, 2, 2), "homeworks_oak_table");
		h.assertTrue(value(h, new BlockPos(1, 2, 1), "east").equals("false"), "oak does not join glass");
		h.assertTrue(value(h, new BlockPos(1, 2, 1), "south").equals("true"), "oak joins oak");
		h.succeed();
	}

	@GameTest(maxTicks = 20)
	public void firstCornerRefreshesWhenTheDiagonalIsPlacedAndRemoved(GameTestHelper h) {
		String name = "homeworks_oak_table";
		BlockPos first = new BlockPos(1, 2, 1), diagonal = new BlockPos(2, 2, 2);
		put(h, first, name);
		put(h, new BlockPos(2, 2, 1), name);
		put(h, new BlockPos(1, 2, 2), name);
		put(h, diagonal, name);
		h.runAfterDelay(2, () -> {
			h.assertTrue(value(h, first, "south_east").equals("true"),
					"The first table refreshes when its diagonal is placed, without touching another block");
			h.setBlock(diagonal, net.minecraft.world.level.block.Blocks.AIR);
			h.runAfterDelay(2, () -> {
				h.assertTrue(value(h, first, "south_east").equals("false"),
						"Removing the diagonal also refreshes the first table");
				h.succeed();
			});
		});
	}
}
