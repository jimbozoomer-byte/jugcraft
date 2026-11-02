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
 * The Giant Cornucopia (the Harvest Festival Grounds, decor set 10, part 1; tools/harvest_festival.py CORNUCOPIA): a
 * woven willow horn of plenty 2.7 metres long lying on its side, its mouth 1.25 metres across and its tail curling up
 * behind it, spilling pumpkins, squashes, corn, wheat, grapes, gourds and apples, each at its own size. Three blocks
 * across, two up and two deep, placed and broken as one ({@link MultiDecorationBlock}); each block's shape is its piece
 * of the horn's boxes, stepping down as it narrows, and of the harvest on the ground before it. It has no lanterns
 * ({@link #LIT} stays on). Its model is tools/harvest_festival_data.py giant_cornucopia().
 */
public class GiantCornucopiaBlock extends MultiDecorationBlock {
	/** Its blocks: 3 across, 2 up, 2 deep (tools/harvest_festival.py CORNUCOPIA["size"]). */
	private static final int[][] CELLS = box(3, 2, 2);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** The mouth and its rim, the horn in three narrowing lengths, the tail standing up, and the harvest on the ground. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {2.0, 0.0, 6.0, 9.0, 22.0, 28.0},
			new double[] {9.0, 0.0, 8.0, 21.0, 18.5, 26.0}, new double[] {21.0, 0.0, 10.5, 31.0, 13.5, 23.5},
			new double[] {31.0, 0.0, 12.5, 42.0, 10.0, 21.5}, new double[] {40.0, 9.0, 14.5, 45.0, 23.0, 19.5},
			new double[] {0.0, 0.0, 1.0, 17.0, 5.0, 8.0});

	public GiantCornucopiaBlock(Properties properties) {
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
