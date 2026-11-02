package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * The Eyeball Jelly Mould (Halloween Party Table, part 3; tools/party_table.py JELLY_MOULD): a white plate 28 cm
 * across on which a ring of clear red jelly stands turned out of its mould, in up to {@value #CAPACITY} segments round
 * its hole, an eyeball set in each looking out ({@link PartyServingBlock}). An Eyeball Jelly used on it sets in the next
 * segment; an empty hand takes one. Its model is tools/party_table_data.py eyeball_jelly_mould().
 */
public class EyeballJellyMouldBlock extends PartyServingBlock {
	public static final int CAPACITY = 8;
	public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, CAPACITY);

	public EyeballJellyMouldBlock(Properties properties) {
		super(properties, new double[] {5.75, 0.0, 5.75, 10.25, 1.75, 10.25}, "eyeball_jelly");
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
