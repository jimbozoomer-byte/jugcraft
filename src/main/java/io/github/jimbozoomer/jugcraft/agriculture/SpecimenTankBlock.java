package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * The Specimen Tank: a glass tank two blocks every way between iron corner posts, on a riveted plinth under a riveted lid
 * with a hatch ({@link SpecimenVesselBlock}), glowing at light {@value #LIGHT}.
 */
public class SpecimenTankBlock extends SpecimenVesselBlock {
	public static final int LIGHT = 10;
	private static final int[][] CELLS = box(2, 2, 2);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	private static final double[] FULL = {0.0, 0.0, 0.0, 16.0, 16.0, 16.0};
	private static final double[][] BOXES = {FULL, FULL, FULL, FULL, FULL, FULL, FULL, FULL};

	public SpecimenTankBlock(Properties properties) {
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
