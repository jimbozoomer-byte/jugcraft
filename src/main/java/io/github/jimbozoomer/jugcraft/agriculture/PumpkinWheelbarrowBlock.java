package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Pumpkin Wheelbarrow (the Pumpkin Patch Market, decor set 1, part 2; tools/pumpkin_patch.py
 * DISPLAYS["wheelbarrow"]): one block across and two long: a wooden wheelbarrow, its iron-shod wheel at the front,
 * heaped with up to four pieces and one on top. A {@link ProduceDisplayBlock}; its model is tools/pumpkin_patch_data.py
 * wheelbarrow().
 */
public class PumpkinWheelbarrowBlock extends ProduceDisplayBlock {
	/** Its blocks: 1 across, 1 up, 2 deep (tools/pumpkin_patch.py DISPLAYS["wheelbarrow"]["size"]). */
	private static final int[][] CELLS = box(1, 1, 2);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** Where its pieces stand, in pixels in its frame, filled in this order (tools/pumpkin_patch.py). */
	private static final float[][] SPOTS = {
			{5.75F, 7.5F, 15.5F},
			{10.25F, 7.5F, 15.5F},
			{5.75F, 7.5F, 22.5F},
			{10.25F, 7.5F, 22.5F},
			{8.0F, 10.0F, 19.0F}};
	private static final float SCALE = 0.625F;
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {1.75, 0.0, 0.5, 14.25, 12.25, 31.7});

	public PumpkinWheelbarrowBlock(Properties properties) {
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
