package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Apple Bin (Orchard & Apple Harvest, decor set 2, part 1; tools/orchard_harvest.py BIN): an orchard's bulk bin,
 * 1.2 m square on a pallet, its sides slats with gaps between, two blocks across and two deep. It holds up to
 * {@value #CAPACITY} apples and pears, the heap inside rising through {@link FruitBinBlock#STAGES} stages as it fills
 * ({@link FruitBinBlock}). Its model is tools/orchard_harvest_data.py apple_bin().
 */
public class AppleBinBlock extends FruitBinBlock {
	public static final int CAPACITY = 256;
	/** Its blocks: 2 across, 1 up, 2 deep (tools/orchard_harvest.py BIN["size"]). */
	private static final int[][] CELLS = box(2, 1, 2);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {6.5, 0.0, 6.5, 25.5, 14.0, 25.5});

	public AppleBinBlock(Properties properties) {
		super(properties);
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}

	@Override
	public int capacity() {
		return CAPACITY;
	}

	@Override
	protected Map<Direction, VoxelShape[]> shapes() {
		return SHAPES;
	}
}
