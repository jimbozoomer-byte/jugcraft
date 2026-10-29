package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Orchard Handcart (Orchard & Apple Harvest, decor set 2, part 2; tools/orchard_harvest.py CART): a two-wheeled
 * wooden handcart, its plank box on side beams that run forward as its handles, a rest under them and a spoked wheel
 * either side, one block across and two deep, the handles towards the player who placed it. It holds up to
 * {@value #CAPACITY} apples and pears, the load rising through {@link FruitBinBlock#STAGES} stages ({@link FruitBinBlock}).
 * Its model is tools/orchard_harvest_data.py handcart().
 */
public class OrchardHandcartBlock extends FruitBinBlock {
	public static final int CAPACITY = 128;
	/** Its blocks: 1 across, 1 up, 2 deep (tools/orchard_harvest.py CART["size"]). */
	private static final int[][] CELLS = box(1, 1, 2);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** The box, the wheels, the handles and the rest's two legs. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {2.0, 6.5, 14.0, 14.0, 11.5, 30.0},
			new double[] {0.0, 0.0, 17.0, 16.0, 10.0, 27.0}, new double[] {2.85, 5.35, 0.75, 13.15, 6.65, 14.0},
			new double[] {3.0, 0.0, 6.5, 4.0, 5.5, 7.5}, new double[] {12.0, 0.0, 6.5, 13.0, 5.5, 7.5});

	public OrchardHandcartBlock(Properties properties) {
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
