package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Giant Warty Gourd (the Pumpkin Patch Market, decor set 1, part 3; tools/pumpkin_patch.py GIANT_WARTY): the Warty
 * Gourd made {@value #SCALE} times bigger, its body, neck and stalk on its own textures, nothing redesigned
 * (docs/ART_DIRECTION.md, "A bigger version is the same thing, bigger"): two blocks wide and deep and three tall, placed
 * and broken as one ({@link MultiDecorationBlock}); each block's shape is its piece of the gourd's boxes made as much
 * bigger. Its model is tools/pumpkin_patch_data.py giant_warty_gourd(). It has no lanterns ({@link #LIT} stays on).
 */
public class GiantWartyGourdBlock extends MultiDecorationBlock {
	/** How many times bigger than the Warty Gourd it is. */
	public static final int SCALE = 3;
	/** Its blocks: 2 across, 3 up, 2 deep (tools/pumpkin_patch.py GIANT_WARTY["size"]). */
	private static final int[][] CELLS = box(2, 3, 2);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** The Warty Gourd's body, neck and stalk (tools/festival_data.py), each made {@link #SCALE} times bigger. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, grown(new double[] {4.0, 0.0, 4.0, 12.0, 8.0, 12.0}, SCALE, 2, 2),
			grown(new double[] {6.0, 8.0, 6.0, 10.0, 12.0, 10.0}, SCALE, 2, 2), grown(new double[] {7.0, 12.0, 7.0, 9.0, 14.0, 9.0}, SCALE, 2, 2));

	public GiantWartyGourdBlock(Properties properties) {
		super(properties);
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

	/** It has no lanterns to light. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return InteractionResult.PASS;
	}
}
