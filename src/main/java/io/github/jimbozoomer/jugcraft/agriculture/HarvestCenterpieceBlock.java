package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Harvest Centerpiece (the Harvest Festival Grounds, part 5; tools/harvest_festival.py CENTERPIECE): the town square's
 * November centrepiece (Thanksgiving phase 6, docs/features/thanksgiving-festival-nov1.md). The Giant Cornucopia's horn
 * and harvest half as big again, in a harvest show of pumpkins, squashes, fruit and corn, with two corn shocks, two roped
 * straw bales and three brass lanterns. Five blocks across, three up and five deep, placed and broken as one
 * ({@link MultiDecorationBlock}); it has no block entity and keeps nothing. Its lanterns are lit or put out by using any
 * block ({@link #LIT}); while lit, the blocks holding a lantern's glass ({@link #LANTERNS}) give light {@value #LIGHT}.
 * Its model is tools/harvest_festival_data.py harvest_centerpiece(). The Giant Cornucopia itself is unchanged.
 */
public class HarvestCenterpieceBlock extends MultiDecorationBlock {
	/** Its blocks: 5 across, 3 up, 5 deep (tools/harvest_festival.py CENTERPIECE["size"]). */
	private static final int[][] CELLS = box(5, 3, 5);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** Each lantern's light while lit (tools/harvest_festival.py CENTERPIECE["light"]). */
	public static final int LIGHT = 13;
	/** The parts holding a lantern's glass (tools/harvest_festival_data.py centerpiece_parts()). */
	public static final Set<Integer> LANTERNS = Set.of(0, 4, 5, 62, 67);
	/**
	 * The horn (its mouth and rim, three narrowing lengths and its tail standing up, as the Giant Cornucopia's shapes,
	 * half as big again where its frame stands), the harvest before it, the two bales and three lanterns, the two corn
	 * shocks and what lies about the back bale.
	 */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS,
			new double[] {14.0, 0.0, 23.5, 24.5, 33.0, 56.5}, new double[] {24.5, 0.0, 26.5, 42.5, 27.75, 53.5},
			new double[] {42.5, 0.0, 30.25, 57.5, 20.25, 49.75}, new double[] {57.5, 0.0, 33.25, 74.0, 15.0, 46.75},
			new double[] {71.0, 13.5, 36.25, 78.5, 34.5, 43.75}, new double[] {11.0, 0.0, 16.0, 36.5, 7.5, 26.5},
			new double[] {8.5, 0.0, 2.0, 25.0, 13.0, 15.0}, new double[] {20.0, 0.0, 0.5, 60.0, 7.0, 20.0},
			new double[] {60.0, 0.0, 1.75, 76.0, 10.25, 12.25}, new double[] {63.45, 10.0, 2.45, 72.55, 26.9, 11.55},
			new double[] {0.95, 0.0, 3.95, 10.05, 16.9, 13.05},
			new double[] {3.0, 0.0, 60.0, 19.0, 45.0, 76.0}, new double[] {61.0, 0.0, 60.0, 77.0, 45.0, 76.0},
			new double[] {32.0, 0.0, 62.75, 48.0, 10.25, 73.25}, new double[] {35.45, 10.0, 63.45, 44.55, 26.9, 72.55},
			new double[] {15.5, 0.0, 55.5, 64.5, 6.5, 78.5});

	public HarvestCenterpieceBlock(Properties properties) {
		super(properties);
	}

	/** Its light: {@link #LIGHT} from a block holding a lantern's glass while its lanterns are lit, else none. */
	public static int light(BlockState state) {
		return state.getValue(LIT) && LANTERNS.contains(state.getValue(PART)) ? LIGHT : 0;
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
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING))[part(state)];
	}
}
