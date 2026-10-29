package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * The Spider Cupcake Tower (Halloween Party Table, part 2; tools/party_table.py CUPCAKE_TOWER): a party cupcake stand
 * 50 cm high, three round tiers of black enamelled steel on a centre rod, holding up to {@value #CAPACITY} Spiderweb
 * Cupcakes ({@link PartyServingBlock}): eight round the bottom tier, four on the middle and one on top, every third with a
 * licorice spider on its frosting. They go on from the bottom tier up and come off from the top down. Its model is
 * tools/party_table_data.py spider_cupcake_tower().
 */
public class SpiderCupcakeTowerBlock extends PartyServingBlock {
	public static final int CAPACITY = 13;
	public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, CAPACITY);

	public SpiderCupcakeTowerBlock(Properties properties) {
		super(properties, new double[] {5.0, 0.0, 5.0, 11.0, 8.25, 11.0}, "spiderweb_cupcake");
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
