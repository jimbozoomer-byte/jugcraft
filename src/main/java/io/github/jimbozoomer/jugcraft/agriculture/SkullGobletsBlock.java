package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Skull Goblets (Halloween Party Table, part 5; tools/party_table.py SKULL_GOBLETS): a set of {@value #CAPACITY} goblets
 * 20 cm high, each a bone-white skull for its cup on a black stem and foot. Witch's Brew Punch used on them is poured
 * into the next empty goblet; an empty hand takes a filled one's punch ({@link PartyServingBlock}). The punch glows, so
 * while any goblet holds some they give a little light ({@link #light}). Its model is tools/party_table_data.py
 * skull_goblets().
 */
public class SkullGobletsBlock extends PartyServingBlock {
	public static final int CAPACITY = 4;
	public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, CAPACITY);
	/** The light while any goblet holds punch: tools/party_table.py SKULL_GOBLETS light. */
	public static final int LIGHT = 4;

	public SkullGobletsBlock(Properties properties) {
		super(properties, new double[] {4.5, 0.0, 4.5, 11.5, 3.5, 11.5}, "witchs_brew_punch");
	}

	/** {@link #LIGHT} while any goblet holds punch, else none. */
	public static int light(BlockState state) {
		return state.getValue(SERVINGS) > 0 ? LIGHT : 0;
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
