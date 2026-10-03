package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.diagonal.DiagonalConnections;
import java.lang.management.ManagementFactory;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import javax.management.ObjectName;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** PROBE (never to be merged): the walls PR's state count and heap measurement, run on main's code for a baseline. */
public class DiagonalBaselineGameTests {
	private static final Logger LOGGER = LoggerFactory.getLogger("jugcraft-diagonal-tests");

	private static Block vanilla(String id) {
		return BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(id));
	}

	/**
	 * Logs the block states that the diagonal properties add and what they weigh: the game's block states in all, those
	 * of blocks with diagonals and of walls, the heap after a full collection with the classes that hold block states,
	 * and the memory a wall's states take with and without the four properties (built here, apart from the game's).
	 * Nothing is asserted: these are numbers for the feature record.
	 */
	@GameTest
	public void theStatesDiagonalsAddAreCounted(GameTestHelper helper) {
		int withDiagonals = 0;
		int walls = 0;
		for (Block block : BuiltInRegistries.BLOCK) {
			int states = block.getStateDefinition().getPossibleStates().size();
			if (DiagonalConnections.hasDiagonals(block.defaultBlockState())) {
				withDiagonals += states;
			}
			if (block instanceof WallBlock) {
				walls += states;
			}
		}
		LOGGER.info("Block states: {} in all, {} in blocks with diagonals, {} in walls", Block.BLOCK_STATE_REGISTRY.size(), withDiagonals, walls);
		LOGGER.info("Heap after a full collection: {} MB; {}", heapAfterCollection() / (1024 * 1024), histogram());
		Property<?>[] wall = {WallBlock.UP, WallBlock.NORTH, WallBlock.EAST, WallBlock.SOUTH, WallBlock.WEST, WallBlock.WATERLOGGED};
		Property<?>[] diagonal = {DiagonalConnections.NORTH_EAST, DiagonalConnections.SOUTH_EAST, DiagonalConnections.SOUTH_WEST,
				DiagonalConnections.NORTH_WEST};
		Property<?>[] both = new Property<?>[10];
		System.arraycopy(wall, 0, both, 0, 6);
		System.arraycopy(diagonal, 0, both, 6, 4);
		LOGGER.info("A wall state takes about {} bytes without diagonals and {} bytes with them (states and their tables only)",
				bytesPerState(wall, 320), bytesPerState(both, 20));
		helper.succeed();
	}

	private static long heapAfterCollection() {
		Runtime runtime = Runtime.getRuntime();
		for (int i = 0; i < 3; i++) {
			System.gc();
		}
		return runtime.totalMemory() - runtime.freeMemory();
	}

	/** The bytes each block state takes, from the heap before and after building `copies` state definitions. */
	private static long bytesPerState(Property<?>[] properties, int copies) {
		List<StateDefinition<Block, BlockState>> kept = new ArrayList<>();
		long before = heapAfterCollection();
		int states = 0;
		for (int i = 0; i < copies; i++) {
			StateDefinition<Block, BlockState> definition = new StateDefinition.Builder<Block, BlockState>(vanilla("cobblestone_wall"))
					.add(properties).create(Block::defaultBlockState, BlockState::new);
			kept.add(definition);
			states += definition.getPossibleStates().size();
		}
		long after = heapAfterCollection();
		Reference.reachabilityFence(kept);
		return (after - before) / states;
	}

	/** The class histogram's lines for block states and their caches, and its total (a full collection runs first). */
	private static String histogram() {
		try {
			String text = (String) ManagementFactory.getPlatformMBeanServer().invoke(new ObjectName("com.sun.management:type=DiagnosticCommand"),
					"gcClassHistogram", new Object[] {null}, new String[] {String[].class.getName()});
			List<String> lines = new ArrayList<>();
			for (String line : text.split("\\R")) {
				String trimmed = line.trim();
				if (trimmed.endsWith(".level.block.state.BlockState") || trimmed.endsWith("BlockStateBase$Cache") || trimmed.startsWith("Total")) {
					lines.add(trimmed.replaceAll("\\s+", " "));
				}
			}
			return String.join("; ", lines);
		} catch (Exception exception) {
			return "no class histogram: " + exception;
		}
	}

}
