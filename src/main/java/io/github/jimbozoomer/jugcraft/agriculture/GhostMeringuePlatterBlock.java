package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * The Ghost Meringue Platter (Halloween Party Table, part 2; tools/party_table.py MERINGUE_PLATTER): an oval platter of
 * black-glazed earthenware 35 cm long on a low foot, holding up to {@value #CAPACITY} Ghost Meringues in three rows
 * ({@link PartyServingBlock}): piped meringue ghosts 9 cm high, their peaks curled over, two cocoa eyes on each. Its
 * model is tools/party_table_data.py ghost_meringue_platter().
 */
public class GhostMeringuePlatterBlock extends PartyServingBlock {
	public static final int CAPACITY = 9;
	public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, CAPACITY);

	public GhostMeringuePlatterBlock(Properties properties) {
		super(properties, new double[] {5.25, 0.0, 6.0, 10.75, 2.25, 10.0}, "ghost_meringue");
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
