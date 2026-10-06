package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * The Tall Specimen Jar: the Specimen Jar drawn out two blocks tall on a wider foot ({@link SpecimenVesselBlock}), glowing
 * at light {@value #LIGHT}.
 */
public class TallSpecimenJarBlock extends SpecimenVesselBlock {
	public static final int LIGHT = 8;
	private static final int[][] CELLS = box(1, 2, 1);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	private static final double[][] BOXES = {{2.5, 0.0, 2.5, 13.5, 16.0, 13.5}, {2.75, 0.0, 2.75, 13.25, 14.5, 13.25}};

	public TallSpecimenJarBlock(Properties properties) {
		super(properties, BOXES);
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}
}
