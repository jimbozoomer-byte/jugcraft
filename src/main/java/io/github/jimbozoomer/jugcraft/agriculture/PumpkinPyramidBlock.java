package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Pumpkin Pyramid (the Pumpkin Patch Market, decor set 1, part 2; tools/pumpkin_patch.py DISPLAYS["pyramid"]):
 * three blocks square: three stepped tiers of boards, sixteen pieces round the bottom step, eight round the middle and
 * one crowning the top. A {@link ProduceDisplayBlock}; its model is tools/pumpkin_patch_data.py pyramid().
 */
public class PumpkinPyramidBlock extends ProduceDisplayBlock {
	/** Its blocks: 3 across, 1 up, 3 deep (tools/pumpkin_patch.py DISPLAYS["pyramid"]["size"]). */
	private static final int[][] CELLS = box(3, 1, 3);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** Where its pieces stand, in pixels in its frame, filled in this order (tools/pumpkin_patch.py). */
	private static final float[][] SPOTS = {
			{4.75F, 4.0F, 4.75F},
			{14.25F, 4.0F, 4.75F},
			{24.0F, 4.0F, 4.75F},
			{33.75F, 4.0F, 4.75F},
			{43.25F, 4.0F, 4.75F},
			{4.75F, 4.0F, 43.25F},
			{14.25F, 4.0F, 43.25F},
			{24.0F, 4.0F, 43.25F},
			{33.75F, 4.0F, 43.25F},
			{43.25F, 4.0F, 43.25F},
			{4.75F, 4.0F, 14.25F},
			{4.75F, 4.0F, 24.0F},
			{4.75F, 4.0F, 33.75F},
			{43.25F, 4.0F, 14.25F},
			{43.25F, 4.0F, 24.0F},
			{43.25F, 4.0F, 33.75F},
			{12.5F, 10.0F, 12.5F},
			{24.0F, 10.0F, 12.5F},
			{35.5F, 10.0F, 12.5F},
			{12.5F, 10.0F, 35.5F},
			{24.0F, 10.0F, 35.5F},
			{35.5F, 10.0F, 35.5F},
			{12.5F, 10.0F, 24.0F},
			{35.5F, 10.0F, 24.0F},
			{24.0F, 16.0F, 24.0F}};
	private static final float SCALE = 0.8F;
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {0.0, 0.0, 0.0, 48.0, 4.75, 48.0}, new double[] {7.75, 4.0, 7.75, 40.25, 10.75, 40.25}, new double[] {15.75, 10.0, 15.75, 32.25, 16.0, 32.25});

	public PumpkinPyramidBlock(Properties properties) {
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
