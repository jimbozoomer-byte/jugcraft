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
 * The Hay Wagon (the Harvest Festival Grounds, decor set 10, part 3; tools/harvest_festival.py WAGON): a four-wheeled farm
 * wagon 4 m long under a load of hay 2 m high, roped down, with hay ladders at either end and a pitchfork stuck in the
 * top. Three blocks across, two up and four deep, placed and broken as one ({@link MultiDecorationBlock}); placed behind
 * a Steam Traction Engine facing the same way, its drawbar reaches the engine's hook. It has no lanterns ({@link #LIT}
 * stays on). Its model is tools/harvest_festival_data.py hay_wagon().
 */
public class HayWagonBlock extends MultiDecorationBlock {
	/** Its blocks: 3 across, 2 up, 4 deep (tools/harvest_festival.py WAGON["size"]). */
	private static final int[][] CELLS = box(3, 2, 4);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** The wheels either side, the bed and its load, and the drawbar out to the front. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {7.5, 0.0, 5.0, 10.5, 16.0, 59.0},
			new double[] {37.5, 0.0, 5.0, 40.5, 16.0, 59.0}, new double[] {9.5, 7.5, 3.0, 38.5, 31.0, 61.0}, new double[] {22.75, 7.75, 0.0, 25.25, 9.75, 10.0});

	public HayWagonBlock(Properties properties) {
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
