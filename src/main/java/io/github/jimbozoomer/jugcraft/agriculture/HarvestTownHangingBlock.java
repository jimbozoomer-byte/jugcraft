package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A decoration hung from the underside of a block as the Hanging Ghost and the Acorn and Leaf Mobile are
 * ({@link HangingGhostBlock}: it needs a block above it to hang from, and drops when that goes), but longer one way than
 * the other, so its outline turns with the way it faces: the Hanging Crock and Herb Rack (Thanksgiving harvest town
 * details, tools/thanksgiving_town.py PROPS, kind "hanging"). Decoration only: it holds and dries nothing.
 */
public class HarvestTownHangingBlock extends HangingGhostBlock {
	private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

	/** {@code box}: its outline {x0, y0, z0, x1, y1, z1} in pixels, facing north. */
	public HarvestTownHangingBlock(Properties properties, double[] box) {
		super(properties, Shapes.block());
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			shapes.put(facing, LongDecorationBlock.shape(new double[][] {box}, facing));
		}
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shapes.get(state.getValue(FACING));
	}
}
