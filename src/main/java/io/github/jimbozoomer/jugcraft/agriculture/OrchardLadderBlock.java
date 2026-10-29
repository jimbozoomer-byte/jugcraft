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
 * The Orchard Ladder (Orchard & Apple Harvest, decor set 2, part 1; tools/orchard_harvest.py LADDER): a three-legged
 * orchard ladder about nine feet tall, its two rails tapering to a cap at the top and a single pole hinged there leaning
 * back to the ground. One block across, three up and two deep, placed and broken as one ({@link MultiDecorationBlock});
 * its rails stand towards the back of its front blocks, so a player against them is inside the ladder, which climbs like
 * a vanilla ladder (block tag {@code minecraft:climbable}, on every block of it). It has no lanterns ({@link #LIT} stays
 * on). Its model is tools/orchard_harvest_data.py ladder().
 */
public class OrchardLadderBlock extends MultiDecorationBlock {
	/** Its blocks: 1 across, 3 up, 2 deep (tools/orchard_harvest.py LADDER["size"]). */
	private static final int[][] CELLS = box(1, 3, 2);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** The rails and steps in three tapering lengths, the cap, and the pole in three lengths stepping back as it leans. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {0.75, 0.0, 9.0, 15.25, 16.0, 12.0},
			new double[] {2.5, 16.0, 9.0, 13.5, 32.0, 12.0}, new double[] {4.0, 32.0, 9.0, 12.0, 46.0, 12.5},
			new double[] {7.0, 32.0, 12.5, 9.0, 45.0, 19.0}, new double[] {7.0, 16.0, 18.5, 9.0, 32.0, 25.5},
			new double[] {7.0, 0.0, 25.0, 9.0, 16.0, 32.0});

	public OrchardLadderBlock(Properties properties) {
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
