package io.github.jimbozoomer.jugcraft.building;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * A Pipeworks prop (batch 47, docs/features/pipeworks.md): a pipe run, rack, bridge, tank or drum of several blocks,
 * placed and broken as one, the way {@link io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock} places
 * the haunted archway, but with nothing to light. Every block of it is this block; its {@link #PART} says which cell of
 * the prop it fills, counted to the right of the player who placed it, up from the block they aimed at and away from
 * them (part = right + across x (up + up-count x away), as tools/pipeworks_models.py cells() counts them). Part 0 is the
 * master, which alone drops the prop. Breaking any block breaks it all. The prop faces the player who placed it
 * ({@link #FACING}), and its models are cut from one whole model, so a block's model is that block's slice.
 */
public class PipeworksBlock extends Block {
	public static final int MASTER = 0;
	/** Most blocks one prop may fill (PART_STATES in tools/model_writer.py). */
	public static final int MAX_PARTS = 64;
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, MAX_PARTS - 1);

	private final int across;
	private final int up;
	private final int away;
	private final Map<Direction, VoxelShape[]> shapes = new EnumMap<>(Direction.class);

	/**
	 * A prop {@code across} blocks wide, {@code up} tall and {@code away} deep, whose collision is {@code boxes}
	 * ({x0, y0, z0, x1, y1, z1} in pixels in the frame of the whole prop facing north: x across to the placer's left from
	 * its far block, y up, z away), cut at the block boundaries.
	 */
	public PipeworksBlock(Properties properties, int across, int up, int away, double[][] boxes) {
		super(properties);
		this.across = across;
		this.up = up;
		this.away = away;
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, MASTER));
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			VoxelShape[] parts = new VoxelShape[parts()];
			for (int part = 0; part < parts.length; part++) {
				int[] cell = cell(part);
				double[] origin = {16.0 * (across - 1 - cell[0]), 16.0 * cell[1], 16.0 * cell[2]};
				VoxelShape shape = Shapes.empty();
				for (double[] whole : boxes) {
					double[] box = new double[6];
					boolean empty = false;
					for (int k = 0; k < 3; k++) {
						box[k] = Math.max(0.0, whole[k] - origin[k]);
						box[k + 3] = Math.min(16.0, whole[k + 3] - origin[k]);
						empty |= box[k + 3] <= box[k];
					}
					if (!empty) {
						shape = Shapes.or(shape, turned(box, facing));
					}
				}
				parts[part] = shape.optimize();
			}
			shapes.put(facing, parts);
		}
	}

	/** A box {x0, y0, z0, x1, y1, z1} in pixels for a prop facing north, turned to {@code facing} as a blockstate turns its model. */
	private static VoxelShape turned(double[] b, Direction facing) {
		double x0 = b[0], z0 = b[2], x1 = b[3], z1 = b[5];
		int turns = switch (facing) {
			case EAST -> 1;
			case SOUTH -> 2;
			case WEST -> 3;
			default -> 0;
		};
		for (int i = 0; i < turns; i++) {
			double nx0 = 16 - z1, nx1 = 16 - z0, nz0 = x0, nz1 = x1;
			x0 = nx0;
			x1 = nx1;
			z0 = nz0;
			z1 = nz1;
		}
		return Block.box(x0, b[1], z0, x1, b[4], z1);
	}

	public int across() {
		return across;
	}

	public int up() {
		return up;
	}

	public int away() {
		return away;
	}

	/** How many blocks the prop fills. */
	public int parts() {
		return across * up * away;
	}

	/** Part {@code part}'s cell: {blocks to the placer's right, blocks up, blocks away}. */
	public int[] cell(int part) {
		return new int[] {part % across, part / across % up, part / (across * up)};
	}

	/** Where part {@code part} of the prop with its master at {@code master}, facing {@code facing}, is. */
	public BlockPos partPos(BlockPos master, Direction facing, int part) {
		int[] cell = cell(part);
		return master.relative(facing.getCounterClockWise(), cell[0]).above(cell[1]).relative(facing.getOpposite(), cell[2]);
	}

	/** Where the master of the prop this block belongs to is. */
	public BlockPos masterPos(BlockPos pos, BlockState state) {
		int[] cell = cell(state.getValue(PART));
		Direction facing = state.getValue(FACING);
		return pos.relative(facing.getCounterClockWise(), -cell[0]).below(cell[1]).relative(facing, cell[2]);
	}

	/** Placed facing the player, from the block they aimed at to their right, up and away: every block must be free. */
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction facing = context.getHorizontalDirection().getOpposite();
		Level level = context.getLevel();
		BlockPos master = context.getClickedPos();
		for (int part = 1; part < parts(); part++) {
			BlockPos pos = partPos(master, facing, part);
			if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)
					|| !level.getBlockState(pos).canBeReplaced(BlockPlaceContext.at(context, pos, Direction.UP))) {
				return null;
			}
		}
		return defaultBlockState().setValue(FACING, facing).setValue(PART, MASTER);
	}

	/** Fills in the rest of the prop. */
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level.isClientSide()) {
			return;
		}
		Direction facing = state.getValue(FACING);
		for (int part = 1; part < parts(); part++) {
			level.setBlock(partPos(pos, facing, part), state.setValue(PART, part), Block.UPDATE_ALL);
		}
	}

	/** Whether the block at {@code pos} is part {@code part} of a prop of this kind facing {@code facing}. */
	private boolean isPart(Level level, BlockPos pos, int part, Direction facing) {
		BlockState state = level.getBlockState(pos);
		return state.is(this) && state.getValue(PART) == part && state.getValue(FACING) == facing;
	}

	/** Breaking any block breaks the prop: the master drops it once, the rest go without drops. */
	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
		if (level.getBlockState(pos).is(this)) {
			return;
		}
		BlockPos master = masterPos(pos, state);
		Direction facing = state.getValue(FACING);
		if (state.getValue(PART) != MASTER) {
			if (isPart(level, master, MASTER, facing)) {
				level.destroyBlock(master, true);
			}
			return;
		}
		for (int part = 1; part < parts(); part++) {
			BlockPos partPos = partPos(master, facing, part);
			if (isPart(level, partPos, part, facing)) {
				level.removeBlock(partPos, false);
			}
		}
	}

	/** In creative, breaking any block takes the whole prop away without dropping it. */
	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && player.getAbilities().instabuild && state.getValue(PART) != MASTER) {
			BlockPos master = masterPos(pos, state);
			if (level.getBlockState(master).is(this)) {
				level.removeBlock(master, false);
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
		VoxelShape[] parts = shapes.get(state.getValue(FACING));
		int part = state.getValue(PART);
		return part < parts.length ? parts[part] : Shapes.empty();
	}

	/** Turning or mirroring one block alone would tear the prop apart, so structures leave it as placed. */
	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state;
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, PART);
	}
}
