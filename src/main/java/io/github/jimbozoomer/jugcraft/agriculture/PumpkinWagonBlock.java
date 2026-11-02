package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Pumpkin Wagon (the Pumpkin Patch Market, decor set 1, part 2; tools/pumpkin_patch.py DISPLAYS["wagon"]): two
 * blocks across, two high and four long: a farm wagon with barn-red slatted sides on four spoked wheels, eighteen
 * pieces on its hay floor and ten heaped between them. A {@link ProduceDisplayBlock}; its model is
 * tools/pumpkin_patch_data.py wagon().
 */
public class PumpkinWagonBlock extends ProduceDisplayBlock {
	/** Its blocks: 2 across, 2 up, 4 deep (tools/pumpkin_patch.py DISPLAYS["wagon"]["size"]). */
	private static final int[][] CELLS = box(2, 2, 4);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** Where its pieces stand, in pixels in its frame, filled in this order (tools/pumpkin_patch.py). */
	private static final float[][] SPOTS = {
			{9.0F, 12.5F, 10.0F},
			{16.0F, 12.5F, 10.0F},
			{23.0F, 12.5F, 10.0F},
			{9.0F, 12.5F, 19.0F},
			{16.0F, 12.5F, 19.0F},
			{23.0F, 12.5F, 19.0F},
			{9.0F, 12.5F, 28.0F},
			{16.0F, 12.5F, 28.0F},
			{23.0F, 12.5F, 28.0F},
			{9.0F, 12.5F, 37.0F},
			{16.0F, 12.5F, 37.0F},
			{23.0F, 12.5F, 37.0F},
			{9.0F, 12.5F, 46.0F},
			{16.0F, 12.5F, 46.0F},
			{23.0F, 12.5F, 46.0F},
			{9.0F, 12.5F, 55.0F},
			{16.0F, 12.5F, 55.0F},
			{23.0F, 12.5F, 55.0F},
			{12.5F, 17.5F, 14.5F},
			{19.5F, 17.5F, 14.5F},
			{12.5F, 17.5F, 23.5F},
			{19.5F, 17.5F, 23.5F},
			{12.5F, 17.5F, 32.5F},
			{19.5F, 17.5F, 32.5F},
			{12.5F, 17.5F, 41.5F},
			{19.5F, 17.5F, 41.5F},
			{12.5F, 17.5F, 50.5F},
			{19.5F, 17.5F, 50.5F}};
	private static final float SCALE = 1.0F;
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {0.0, 0.0, 1.25, 32.0, 12.5, 62.75}, new double[] {1.9, 12.5, 4.0, 30.1, 21.0, 60.0});

	public PumpkinWagonBlock(Properties properties) {
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
	public float[][] spots() {
		return SPOTS;
	}

	@Override
	public float scale() {
		return SCALE;
	}

	@Override
	protected Map<Direction, VoxelShape[]> shapes() {
		return SHAPES;
	}
}
