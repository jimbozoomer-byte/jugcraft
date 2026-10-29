package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * The Mummy Sausage Tray (Halloween Party Table, part 3; tools/party_table.py MUMMY_TRAY): a black baking tray 40 cm by
 * 28 cm with low sides and a white ramekin of mustard in its middle, holding up to {@value #CAPACITY} Mummy Sausages in
 * two rows ({@link PartyServingBlock}): sausages wound in strips of pastry, two dots of mustard for eyes. Its model is
 * tools/party_table_data.py mummy_sausage_tray().
 */
public class MummySausageTrayBlock extends PartyServingBlock {
	public static final int CAPACITY = 8;
	public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, CAPACITY);

	public MummySausageTrayBlock(Properties properties) {
		super(properties, new double[] {4.75, 0.0, 5.75, 11.25, 1.0, 10.25}, "mummy_sausage");
	}

	@Override
	public IntegerProperty servings() {
		return SERVINGS;
	}

	@Override
	public int capacity() {
		return CAPACITY;
	}
}
