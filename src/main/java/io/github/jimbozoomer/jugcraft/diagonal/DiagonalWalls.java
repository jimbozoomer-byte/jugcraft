package io.github.jimbozoomer.jugcraft.diagonal;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.mixin.WallBlockInvoker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Walls that join diagonally (docs/features/diagonal-connections.md). Vanilla's walls keep their own states; each has a
 * {@link DiagonalWallBlock}, {@code jugcraft:diagonal_<wall>}, that it becomes while it joins a wall diagonally and turns
 * back from when its last diagonal goes ({@link #settle}, called wherever vanilla works out a wall's state:
 * {@code mixin/DiagonalWallMixin}).
 */
public final class DiagonalWalls {
	private static final List<Direction> HORIZONTAL = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
	// Masks of a straight run along a diagonal (bits in Diagonal order).
	private static final int RUN_NORTH_EAST = 0b0101;
	private static final int RUN_NORTH_WEST = 0b1010;
	private static final Map<Block, DiagonalWallBlock> BY_WALL = new IdentityHashMap<>();

	private DiagonalWalls() {
	}

	/** Registers a diagonal wall for each of vanilla's walls: {@code jugcraft:diagonal_cobblestone_wall} and so on. */
	public static void register() {
		List<WallBlock> walls = new ArrayList<>();
		for (Block block : BuiltInRegistries.BLOCK) {
			if (block instanceof WallBlock wall && BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("minecraft")) {
				walls.add(wall);
			}
		}
		for (WallBlock wall : walls) {
			ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Jugcraft.id("diagonal_" + BuiltInRegistries.BLOCK.getKey(wall).getPath()));
			BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(wall).overrideLootTable(wall.getLootTable())
					.overrideDescription(wall.getDescriptionId()).setId(key);
			BY_WALL.put(wall, Registry.register(BuiltInRegistries.BLOCK, key, new DiagonalWallBlock(wall, properties)));
		}
	}

	/** Every diagonal wall. */
	public static Collection<DiagonalWallBlock> all() {
		return Collections.unmodifiableCollection(BY_WALL.values());
	}

	/** The diagonal wall a vanilla wall becomes, or null. */
	public static DiagonalWallBlock of(Block wall) {
		return BY_WALL.get(wall);
	}

	/** Whether the block is a wall, vanilla's or a diagonal one. */
	public static boolean isWall(BlockState state) {
		return state.getBlock() instanceof WallBlock || state.getBlock() instanceof DiagonalWallBlock;
	}

	/**
	 * A vanilla wall's state, as vanilla worked it out, or the diagonal wall it becomes if it joins a wall diagonally: its
	 * sides joined where vanilla's are (low or tall), its diagonals, and its post by vanilla's rule ({@link #post}).
	 */
	public static BlockState settle(BlockState wallState, BlockGetter level, BlockPos pos) {
		DiagonalWallBlock diagonal = BY_WALL.get(wallState.getBlock());
		if (diagonal == null || !wallState.is(DiagonalConnections.TAG) || !JugcraftConfig.isFeatureEnabled(DiagonalConnections.FEATURE)) {
			return wallState;
		}
		int mask = DiagonalConnections.diagonals(wallState, level, pos);
		if (mask == 0) {
			return wallState;
		}
		BlockState state = diagonal.defaultBlockState().setValue(DiagonalWallBlock.WATERLOGGED, wallState.getValue(WallBlock.WATERLOGGED));
		for (Direction side : HORIZONTAL) {
			state = state.setValue(DiagonalWallBlock.SIDES.get(side), wallState.getValue(WallBlock.PROPERTY_BY_DIRECTION.get(side)) != WallSide.NONE);
		}
		for (DiagonalConnections.Diagonal each : DiagonalConnections.Diagonal.ALL) {
			state = state.setValue(each.property, (mask & 1 << each.ordinal()) != 0);
		}
		return state.setValue(DiagonalWallBlock.UP, post((WallBlock) wallState.getBlock(), wallState, mask, level, pos));
	}

	/**
	 * The vanilla wall a diagonal wall stands for, its sides low where joined. Vanilla works out which are tall from the
	 * block above when it next updates the wall.
	 */
	public static BlockState toWall(BlockState state) {
		DiagonalWallBlock diagonal = (DiagonalWallBlock) state.getBlock();
		BlockState wall = diagonal.wall().defaultBlockState().setValue(WallBlock.UP, state.getValue(DiagonalWallBlock.UP))
				.setValue(WallBlock.WATERLOGGED, state.getValue(DiagonalWallBlock.WATERLOGGED));
		for (Direction side : HORIZONTAL) {
			wall = wall.setValue(WallBlock.PROPERTY_BY_DIRECTION.get(side), state.getValue(DiagonalWallBlock.SIDES.get(side)) ? WallSide.LOW : WallSide.NONE);
		}
		return wall;
	}

	/**
	 * A diagonal wall's post, raised by vanilla's own rule with the diagonals counted. A wall with no straight sides and
	 * one pair of opposite diagonals runs straight on, so the rule sees it as a straight low wall: no post unless something
	 * above calls for one (a torch, a wall's post, a block over the middle). Any other wall with diagonals is an end, a
	 * corner or a junction, and the rule gives it its post.
	 */
	private static boolean post(WallBlock wall, BlockState wallState, int mask, BlockGetter level, BlockPos pos) {
		BlockPos abovePos = pos.above();
		BlockState above = level.getBlockState(abovePos);
		VoxelShape aboveShape = above.getCollisionShape(level, abovePos).getFaceShape(Direction.DOWN);
		BlockState seen = wallState;
		boolean noSides = HORIZONTAL.stream().allMatch(side -> wallState.getValue(WallBlock.PROPERTY_BY_DIRECTION.get(side)) == WallSide.NONE);
		if ((mask == RUN_NORTH_EAST || mask == RUN_NORTH_WEST) && noSides) {
			seen = wallState.setValue(WallBlock.NORTH, WallSide.LOW).setValue(WallBlock.SOUTH, WallSide.LOW);
		}
		return ((WallBlockInvoker) wall).jugcraft$shouldRaisePost(seen, above, aboveShape);
	}
}
