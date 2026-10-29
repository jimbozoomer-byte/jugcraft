package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * The Witch-Finger Cauldron (Halloween Party Table, part 3; tools/party_table.py FINGER_CAULDRON): a little cast-iron
 * cauldron 25 cm across on three stubby legs, filled with cookie crumbs, holding up to {@value #CAPACITY} Witch Fingers
 * poking up out of them at angles ({@link PartyServingBlock}): green shortbread with an almond nail and a knuckle crease.
 * Its model is tools/party_table_data.py witch_finger_cauldron().
 */
public class WitchFingerCauldronBlock extends PartyServingBlock {
	public static final int CAPACITY = 10;
	public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, CAPACITY);

	public WitchFingerCauldronBlock(Properties properties) {
		super(properties, new double[] {5.5, 0.0, 5.5, 10.5, 4.25, 10.5}, "witch_finger");
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
