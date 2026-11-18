package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * A piece of homestead furniture two blocks across and two up (Thanksgiving homestead, tools/thanksgiving_homestead.py
 * PROPS, kind "furniture"): the Carved Dish Dresser. A {@link MultiDecorationBlock}: placed facing the player from the
 * block they aim at to their right and up, only where all four blocks are free (it never replaces anything, nor takes
 * the place of water, nor closes round an entity); breaking any block breaks it all, and it drops once, from its first
 * block. It is decoration: using it does nothing (it has no
 * lanterns, so its {@code lit} state is never changed) and it stores nothing. Its outline is the table's boxes, in
 * pixels in its frame facing north, cut into its blocks. Its models are tools/thanksgiving_homestead_data.py's.
 */
public class HomesteadFurnitureBlock extends MultiDecorationBlock {
	/** Its blocks: 2 across, 2 up, 1 deep (tools/thanksgiving_homestead.py PROPS "size"). */
	private static final int[][] CELLS = box(2, 2, 1);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	private final Map<Direction, VoxelShape[]> shapes;

	/** {@code boxes}: its outline, boxes {x0, y0, z0, x1, y1, z1} in pixels in its frame, facing north. */
	public HomesteadFurnitureBlock(Properties properties, double[]... boxes) {
		super(properties);
		shapes = shapesOf(CELLS, boxes);
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}

	/** Whether {@code part} is one of its two lower blocks (parts 0 and 1). */
	public static boolean lower(int part) {
		return part < 2;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shapes.get(state.getValue(FACING))[part(state)];
	}

	/**
	 * Every block it needs must be free, as {@link MultiDecorationBlock} asks, and more: no fluid in it and clear of
	 * entities, so it never swallows water or closes round a player or a sitter. Otherwise nothing is placed.
	 */
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		if (state == null) {
			return null;
		}
		Level level = context.getLevel();
		Direction facing = state.getValue(FACING);
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(context.getClickedPos(), facing, part);
			if (!level.getFluidState(at).isEmpty() || !level.isUnobstructed(state.setValue(PART, part), at, CollisionContext.empty())) {
				return null;
			}
		}
		return state;
	}

	/** Decoration: an empty hand does nothing to it (MultiDecorationBlock would light lanterns it does not have). */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return InteractionResult.PASS;
	}
}
