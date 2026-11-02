package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * The Goodie Bag Tree (Halloween Party Table, part 5; tools/party_table.py GOODIE_TREE): a black wire tree 67 cm high
 * standing in an orange jack-o'-lantern pail full of candy corn, holding up to {@value #CAPACITY} Goodie Bags hung from
 * its branches by their ribbons ({@link PartyServingBlock}). Its model is tools/party_table_data.py goodie_bag_tree().
 */
public class GoodieBagTreeBlock extends PartyServingBlock {
	public static final int CAPACITY = 6;
	public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, CAPACITY);

	public GoodieBagTreeBlock(Properties properties) {
		super(properties, new double[] {3.25, 0.0, 3.25, 12.75, 10.75, 12.75}, "goodie_bag");
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
