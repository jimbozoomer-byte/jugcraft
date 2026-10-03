package io.github.jimbozoomer.jugcraft.diagonal;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.mixin.WallBlockInvoker;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Diagonal connections: fences, glass panes, bars and walls join their diagonal neighbours as well as their straight
 * ones (docs/features/diagonal-connections.md).
 *
 * <p>Every {@link FenceBlock}, {@link IronBarsBlock} and {@link WallBlock} (vanilla's, Jugcraft's and other mods') has
 * four more properties, {@link #NORTH_EAST}, {@link #SOUTH_EAST}, {@link #SOUTH_WEST} and {@link #NORTH_WEST}, false by
 * default ({@code mixin/DiagonalStateMixin}). They are set only for blocks in {@link #TAG}, the blocks that have
 * diagonal arm models (tools/diagonal_connections.py writes the models and the tag), so no block joins diagonally
 * without an arm to show for it.
 *
 * <p>The rule, the same from either end: two blocks a diagonal step apart join when they would join if they were side
 * by side (fences of one kind with each other, panes and bars with each other, walls with walls), and neither has a
 * straight connection into the two blocks between them. So a corner that already joins straight, or a wall or solid
 * block in the corner, keeps them apart, and two diagonals can never cross. A wall's diagonals are always low, and a
 * wall that runs straight on along a diagonal drops its post as a straight wall does ({@link #withPost}).
 *
 * <p>Diagonal neighbours do not get the game's neighbour updates, so a fence, bars block or wall that is placed, broken
 * or changed asks its four diagonal neighbours to look again ({@link #updateDiagonalNeighbours}, through
 * {@code updateIndirectNeighbourShapes}, the game's hook for redstone dust's diagonal neighbours). Shapes ({@link Arms}),
 * rotation and mirroring follow the diagonals ({@code mixin/DiagonalShapeMixin}). The {@code diagonal_connections}
 * switch stops new diagonal joins; existing ones go as their blocks next update. The properties stay either way, so saved
 * blocks load.
 */
public final class DiagonalConnections {
	public static final String FEATURE = "diagonal_connections";
	/** Blocks that join diagonally: each has a diagonal arm model. */
	public static final TagKey<Block> TAG = TagKey.create(Registries.BLOCK, Jugcraft.id("connects_diagonally"));

	public static final BooleanProperty NORTH_EAST = BooleanProperty.create("north_east");
	public static final BooleanProperty SOUTH_EAST = BooleanProperty.create("south_east");
	public static final BooleanProperty SOUTH_WEST = BooleanProperty.create("south_west");
	public static final BooleanProperty NORTH_WEST = BooleanProperty.create("north_west");

	// Masks of a straight run along a diagonal (bits in Diagonal order; literals, so this class's set-up needs no Diagonal).
	private static final int RUN_NORTH_EAST = 0b0101;
	private static final int RUN_NORTH_WEST = 0b1010;
	private static final List<Direction> SIDES = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

	/** A diagonal: its property and the two straight directions it lies between. */
	public enum Diagonal {
		NORTH_EAST(DiagonalConnections.NORTH_EAST, Direction.NORTH, Direction.EAST),
		SOUTH_EAST(DiagonalConnections.SOUTH_EAST, Direction.SOUTH, Direction.EAST),
		SOUTH_WEST(DiagonalConnections.SOUTH_WEST, Direction.SOUTH, Direction.WEST),
		NORTH_WEST(DiagonalConnections.NORTH_WEST, Direction.NORTH, Direction.WEST);

		/** All four, in order (kept here, not in DiagonalConnections, so neither class's set-up needs the other's). */
		public static final List<Diagonal> ALL = List.of(values());

		public final BooleanProperty property;
		public final Direction northSouth;
		public final Direction eastWest;

		Diagonal(BooleanProperty property, Direction northSouth, Direction eastWest) {
			this.property = property;
			this.northSouth = northSouth;
			this.eastWest = eastWest;
		}

		public BlockPos from(BlockPos pos) {
			return pos.relative(northSouth).relative(eastWest);
		}

		public Diagonal opposite() {
			return of(northSouth.getOpposite(), eastWest.getOpposite());
		}

		/** The diagonal between two horizontal directions on different axes, given in either order. */
		public static Diagonal of(Direction first, Direction second) {
			Direction northSouth = first.getAxis() == Direction.Axis.Z ? first : second;
			Direction eastWest = northSouth == first ? second : first;
			for (Diagonal diagonal : values()) {
				if (diagonal.northSouth == northSouth && diagonal.eastWest == eastWest) {
					return diagonal;
				}
			}
			throw new IllegalArgumentException(first + " " + second);
		}
	}

	private DiagonalConnections() {
	}

	/** Whether the block takes diagonal properties (every fence, bars block and wall does). */
	public static boolean hasDiagonals(BlockState state) {
		return state.hasProperty(NORTH_EAST);
	}

	/** The diagonals that are joined, one bit each in {@link Diagonal} order. */
	public static int mask(BlockState state) {
		if (!hasDiagonals(state)) {
			return 0;
		}
		int mask = 0;
		for (Diagonal diagonal : Diagonal.ALL) {
			if (state.getValue(diagonal.property)) {
				mask |= 1 << diagonal.ordinal();
			}
		}
		return mask;
	}

	/** The state with no diagonals, as every fence, bars block and wall starts. */
	public static BlockState withoutDiagonals(BlockState state) {
		if (!hasDiagonals(state)) {
			return state;
		}
		for (Diagonal diagonal : Diagonal.ALL) {
			state = state.setValue(diagonal.property, false);
		}
		return state;
	}

	/**
	 * The state with its diagonals worked out from the world, its straight connections being already up to date. A wall
	 * that has or had diagonals also has its post worked out again.
	 */
	public static BlockState withDiagonals(BlockState state, BlockGetter level, BlockPos pos) {
		if (!hasDiagonals(state)) {
			return state;
		}
		int before = mask(state);
		boolean joins = state.is(TAG) && JugcraftConfig.isFeatureEnabled(FEATURE);
		for (Diagonal diagonal : Diagonal.ALL) {
			state = state.setValue(diagonal.property, joins && joins(state, level, pos, diagonal));
		}
		if (state.getBlock() instanceof WallBlock wall && (before != 0 || mask(state) != 0)) {
			state = withPost(wall, state, level, pos);
		}
		return state;
	}

	/**
	 * A wall's post, raised by vanilla's own rule with the diagonals counted. A wall with no straight sides and one pair
	 * of opposite diagonals runs straight on, so the rule sees it as a straight low wall: no post unless something above
	 * calls for one (a torch, a wall's post, a block over the middle). Any other wall with diagonals is an end, a corner or
	 * a junction, and the rule gives it its post. A wall whose diagonals have just gone gets vanilla's post back.
	 */
	private static BlockState withPost(WallBlock wall, BlockState state, BlockGetter level, BlockPos pos) {
		BlockPos abovePos = pos.above();
		BlockState above = level.getBlockState(abovePos);
		VoxelShape aboveShape = above.getCollisionShape(level, abovePos).getFaceShape(Direction.DOWN);
		BlockState seen = state;
		int mask = mask(state);
		if ((mask == RUN_NORTH_EAST || mask == RUN_NORTH_WEST) && SIDES.stream().noneMatch(side -> straight(state, side))) {
			seen = state.setValue(WallBlock.NORTH, WallSide.LOW).setValue(WallBlock.SOUTH, WallSide.LOW);
		}
		return state.setValue(WallBlock.UP, ((WallBlockInvoker) wall).jugcraft$shouldRaisePost(seen, above, aboveShape));
	}

	private static boolean joins(BlockState state, BlockGetter level, BlockPos pos, Diagonal diagonal) {
		if (straight(state, diagonal.northSouth) || straight(state, diagonal.eastWest)) {
			return false;
		}
		BlockState other = level.getBlockState(diagonal.from(pos));
		if (!hasDiagonals(other) || !other.is(TAG) || !sameKind(state, other, diagonal) || !sameKind(other, state, diagonal.opposite())) {
			return false;
		}
		return !straight(other, diagonal.northSouth.getOpposite()) && !straight(other, diagonal.eastWest.getOpposite());
	}

	/** Whether the block joins straight toward `direction`: a fence's or bars block's side, or a wall's, low or tall. */
	private static boolean straight(BlockState state, Direction direction) {
		if (state.getBlock() instanceof WallBlock) {
			return state.getValue(WallBlock.PROPERTY_BY_DIRECTION.get(direction)) != WallSide.NONE;
		}
		return state.getValue(CrossCollisionBlock.PROPERTY_BY_DIRECTION.get(direction));
	}

	/** The state joined straight toward `direction`, or not: a low side for a wall. */
	private static BlockState withStraight(BlockState state, Direction direction, boolean joined) {
		if (state.getBlock() instanceof WallBlock) {
			return state.setValue(WallBlock.PROPERTY_BY_DIRECTION.get(direction), joined ? WallSide.LOW : WallSide.NONE);
		}
		return state.setValue(CrossCollisionBlock.PROPERTY_BY_DIRECTION.get(direction), joined);
	}

	/**
	 * Whether `state` would join `other` if they were side by side: a fence of the same kind, any pane or bars, or any
	 * wall.
	 */
	private static boolean sameKind(BlockState state, BlockState other, Diagonal toward) {
		if (state.getBlock() instanceof FenceBlock fence) {
			return other.getBlock() instanceof FenceBlock && fence.connectsTo(other, false, toward.northSouth);
		}
		if (state.getBlock() instanceof IronBarsBlock bars) {
			return other.getBlock() instanceof IronBarsBlock && bars.attachsTo(other, false);
		}
		if (state.getBlock() instanceof WallBlock) {
			return other.getBlock() instanceof WallBlock;
		}
		return false;
	}

	/**
	 * A fence, bars block or wall at `pos` was placed, broken or changed: its diagonal neighbours look again, the way the game's
	 * shape updates reach straight neighbours (with the same flags and depth).
	 */
	public static void updateDiagonalNeighbours(LevelAccessor level, BlockPos pos, int flags, int recursionLeft) {
		for (Diagonal diagonal : Diagonal.ALL) {
			BlockPos other = diagonal.from(pos);
			BlockState state = level.getBlockState(other);
			if (hasDiagonals(state)) {
				BlockState updated = withDiagonals(state, level, other);
				if (updated != state) {
					Block.updateOrDestroy(state, updated, level, other, flags, recursionLeft);
				}
			}
		}
	}

	/** The rotated block's diagonals: `rotated` has the straight connections turned already. */
	public static BlockState rotate(BlockState rotated, BlockState original, Rotation rotation) {
		if (!hasDiagonals(original)) {
			return rotated;
		}
		for (Diagonal diagonal : Diagonal.ALL) {
			Diagonal turned = Diagonal.of(rotation.rotate(diagonal.northSouth), rotation.rotate(diagonal.eastWest));
			rotated = rotated.setValue(turned.property, original.getValue(diagonal.property));
		}
		return rotated;
	}

	/** The mirrored block's diagonals: `mirrored` has the straight connections mirrored already. */
	public static BlockState mirror(BlockState mirrored, BlockState original, Mirror mirror) {
		if (!hasDiagonals(original)) {
			return mirrored;
		}
		for (Diagonal diagonal : Diagonal.ALL) {
			Diagonal turned = Diagonal.of(mirror.mirror(diagonal.northSouth), mirror.mirror(diagonal.eastWest));
			mirrored = mirrored.setValue(turned.property, original.getValue(diagonal.property));
		}
		return mirrored;
	}

	/**
	 * The joined diagonals' arms in one of a block's shapes (its outline or its collision). Each arm is as wide and as
	 * high as the block's own straight arm in that shape, laid as a run of small boxes from the middle to the corner.
	 *
	 * <p>Every state that differs only in its diagonals gets the same shape object from the block's shape function
	 * ({@code mixin/DiagonalBlockMixin}), so the shapes with arms are kept for each of those shapes and each set of
	 * diagonals, and are shared by blocks whose shapes and arms are alike (every vanilla wall, every wooden fence).
	 */
	public static final class Arms {
		/** Shapes with arms, by the shape without them and the arm, for every block. */
		private static final Map<Key, AtomicReferenceArray<VoxelShape>> SHARED = new ConcurrentHashMap<>();
		/** Each arm's boxes for each set of diagonals, by the arm. */
		private static final Map<Geometry, AtomicReferenceArray<VoxelShape>> ARMS = new ConcurrentHashMap<>();

		/** This block's shapes with arms, by the shape without them (a VoxelShape is equal only to itself). */
		private final Map<VoxelShape, AtomicReferenceArray<VoxelShape>> byShape = new ConcurrentHashMap<>();
		/** This block's arm, worked out once from its shape function; empty if it has none. */
		private volatile Geometry geometry;

		/** The block's `shape` for `state`, from `shapes`, with the state's joined diagonals' arms added. */
		public VoxelShape apply(VoxelShape shape, BlockState state, Function<BlockState, VoxelShape> shapes) {
			int mask = mask(state);
			if (mask == 0) {
				return shape;
			}
			Geometry geometry = this.geometry;
			if (geometry == null) {
				this.geometry = geometry = Geometry.of(state, shapes);
			}
			if (geometry.empty()) {
				return shape;
			}
			Geometry arm = geometry;
			AtomicReferenceArray<VoxelShape> withArms = byShape.computeIfAbsent(shape,
					base -> SHARED.computeIfAbsent(new Key(base.toAabbs(), arm), key -> new AtomicReferenceArray<>(16)));
			VoxelShape result = withArms.get(mask);
			if (result == null) {
				result = Shapes.or(shape, arm.arms(mask)).optimize();
				withArms.set(mask, result);
			}
			return result;
		}

		private record Key(List<AABB> shape, Geometry arm) {
		}

		/** An arm: half its width, and its bottom and top, in blocks. */
		private record Geometry(double half, double low, double high) {
			private static final Geometry EMPTY = new Geometry(0, 0, 0);

			/** The block's straight north arm: its shape joined north less its shape joined nowhere, with no post. */
			static Geometry of(BlockState state, Function<BlockState, VoxelShape> shapes) {
				BlockState bare = withoutDiagonals(state);
				for (Direction side : SIDES) {
					bare = withStraight(bare, side, false);
				}
				if (bare.getBlock() instanceof WallBlock) {
					bare = bare.setValue(WallBlock.UP, false);
				}
				VoxelShape arm = Shapes.join(shapes.apply(withStraight(bare, Direction.NORTH, true)), shapes.apply(bare), BooleanOp.ONLY_FIRST);
				if (arm.isEmpty()) {
					return EMPTY;
				}
				AABB bounds = arm.bounds();
				return new Geometry((bounds.maxX - bounds.minX) / 2, bounds.minY, bounds.maxY);
			}

			boolean empty() {
				return half == 0;
			}

			/** This arm toward each diagonal in `mask`. */
			VoxelShape arms(int mask) {
				AtomicReferenceArray<VoxelShape> byMask = ARMS.computeIfAbsent(this, arm -> new AtomicReferenceArray<>(16));
				VoxelShape arms = byMask.get(mask);
				if (arms == null) {
					arms = Shapes.empty();
					for (Diagonal diagonal : Diagonal.ALL) {
						if ((mask & 1 << diagonal.ordinal()) != 0) {
							arms = Shapes.or(arms, arm(diagonal));
						}
					}
					arms = arms.optimize();
					byMask.set(mask, arms);
				}
				return arms;
			}

			/** One diagonal arm: boxes `half` either side of the diagonal, a pixel apart along it so they overlap. */
			private VoxelShape arm(Diagonal diagonal) {
				int dx = diagonal.eastWest.getStepX();
				int dz = diagonal.northSouth.getStepZ();
				VoxelShape arm = Shapes.empty();
				for (int step = 0; step < 8; step++) {
					double along = (step + 0.5) / 16.0;
					double x = 0.5 + dx * along;
					double z = 0.5 + dz * along;
					arm = Shapes.or(arm, Shapes.box(clamp(x - half), low, clamp(z - half), clamp(x + half), high, clamp(z + half)));
				}
				return arm;
			}

			private static double clamp(double value) {
				return Math.max(0.0, Math.min(1.0, value));
			}
		}
	}
}
