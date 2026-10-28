package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Ghost Train (Haunted Midway, part 4; tools/haunted_midway.py GHOST_TRAIN): a dark ride's front five blocks across
 * and four up ({@link MultiDecorationBlock}), a haunted house's clapboard wall lettered GHOST TRAIN, with two doorways at
 * track level, IN and OUT, each a block across and two high ({@link #DOORS}). The doorways are not its blocks, so a
 * track of rails runs through them. Each has double doors; every {@value #LOOK_TICKS} ticks its first block looks for
 * a minecart (any: a plain one or a Haunted Hayride) in each doorway or within {@value #REACH} block before or after it,
 * and while one is there that doorway's doors stand open ({@link #IN_OPEN}, {@link #OUT_OPEN}, on every block), shutting
 * {@value #HOLD_TICKS} ticks after the last has gone. Each opening and shutting is a block event its block entity marks,
 * for client/GhostTrainRenderer to swing the doors (the blockstate's {@link #RUNNER} models 1 and 2) over
 * {@value #SWING_TICKS} ticks, the IN doors in and the OUT doors out; as the IN doors open, a ghost (runner 3) springs
 * up over them for {@value #SPOOK_TICKS} ticks with a cackle. An empty hand lights its windows or puts them out
 * ({@link #LIT}; its upper middle row's blocks with windows give light {@value #LIGHT}). Its model is
 * tools/haunted_midway_data.py ghost_train().
 */
public class GhostTrainBlock extends MultiDecorationBlock implements EntityBlock {
	public static final int LOOK_TICKS = 4;
	public static final double REACH = 1.0;
	public static final int SWING_TICKS = 4;
	public static final int HOLD_TICKS = 20;
	public static final int SPOOK_TICKS = 40;
	public static final int LIGHT = 9;
	/** The doorways' blocks to the right of the first, as its cells count them: the IN, then the OUT. */
	public static final int[] DOORS = {1, 3};
	/** A door leaf's width and height, and the wall's depth from the front (pixels). */
	public static final float LEAF_W = 8.0F;
	public static final float LEAF_H = 30.0F;
	public static final float WALL_FROM = 2.0F;
	public static final float WALL_TO = 5.0F;
	private static final int ACROSS = 5;
	private static final int UP = 4;
	/** Its blocks, 5 across and 4 up less each doorway's lower two (tools/haunted_midway_data.py train_cells()). */
	private static final int[][] CELLS = cells(ACROSS, UP);
	private static final int DOOR = 1;
	private static final int SPOOK = 2;
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	public static final BooleanProperty IN_OPEN = BooleanProperty.create("in_open");
	public static final BooleanProperty OUT_OPEN = BooleanProperty.create("out_open");
	/** 0 for the front; 1 and 2 its IN doorway's left and right door leaves and 3 its ghost, for the renderer. */
	public static final IntegerProperty RUNNER = IntegerProperty.create("runner", 0, 3);
	/** The wall beside and between the doorways and over them, and the eaves and crest. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {0.0, 0.0, 0.0, 16.0, 32.0, WALL_TO},
			new double[] {32.0, 0.0, 0.0, 48.0, 32.0, WALL_TO}, new double[] {64.0, 0.0, 0.0, 80.0, 32.0, WALL_TO},
			new double[] {0.0, 32.0, 0.0, 80.0, 61.0, 6.0}, new double[] {34.0, 61.0, 1.0, 46.0, 64.0, 1.5});

	private static int[][] cells(int across, int up) {
		List<int[]> out = new ArrayList<>();
		for (int high = 0; high < up; high++) {
			for (int right = 0; right < across; right++) {
				if (high >= 2 || (right != DOORS[0] && right != DOORS[1])) {
					out.add(new int[] {right, high});
				}
			}
		}
		return out.toArray(int[][]::new);
	}

	public GhostTrainBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(IN_OPEN, false).setValue(OUT_OPEN, false).setValue(RUNNER, 0));
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}

	/** The light it gives: each block of its upper middle row with a window in it (the two ends and the middle), while lit. */
	public static int light(BlockState state) {
		int[] cell = CELLS[state.getValue(PART)];
		return state.getValue(LIT) && cell[1] == 2 && cell[0] % 2 == 0 ? LIGHT : 0;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING))[part(state)];
	}

	/** Where doorway {@code door}'s lowest block is, for the front whose first block is at {@code master}. */
	public static BlockPos doorway(BlockPos master, Direction facing, int door) {
		return master.relative(facing.getCounterClockWise(), DOORS[door]);
	}

	/** The room a cart must be in to open doorway {@code door}: the doorway, two high, and {@value #REACH} block before and after it. */
	public static AABB watched(BlockPos master, Direction facing, int door) {
		AABB box = new AABB(doorway(master, facing, door)).expandTowards(0.0, 1.0, 0.0);
		return facing.getAxis() == Direction.Axis.Z ? box.inflate(0.0, 0.0, REACH) : box.inflate(REACH, 0.0, 0.0);
	}

	/** How far open a doorway's doors are, 0 (shut) to 1 (open), {@code since} ticks (with the partial tick) after they last moved. */
	public static float swing(boolean open, float since) {
		float done = since < 0.0F ? 1.0F : Math.min(1.0F, since / SWING_TICKS);
		float eased = done * done * (3.0F - 2.0F * done);
		return open ? eased : 1.0F - eased;
	}

	/** How high the ghost has sprung {@code since} ticks after the IN doors opened, 0 (hidden) to 1, or 0 once it has gone. */
	public static float spook(float since) {
		if (since < 0.0F || since >= SPOOK_TICKS) {
			return 0.0F;
		}
		return Math.min(1.0F, Math.min(since, SPOOK_TICKS - since) / 5.0F);
	}

	private void setAll(Level level, BlockPos master, Direction facing, BooleanProperty property, boolean value) {
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(master, facing, part);
			BlockState there = level.getBlockState(at);
			if (there.is(this) && part(there) == part && there.getValue(FACING) == facing) {
				level.setBlock(at, there.setValue(property, value), Block.UPDATE_ALL);
			}
		}
	}

	/** Its first block's look at its doorways: open a doorway a cart has come to, shut one the last cart has long left. */
	public void look(ServerLevel level, BlockPos master, BlockState state, GhostTrainBlockEntity watch) {
		Direction facing = state.getValue(FACING);
		long now = level.getGameTime();
		for (int door = 0; door < 2; door++) {
			BooleanProperty open = door == 0 ? IN_OPEN : OUT_OPEN;
			if (!level.getEntitiesOfClass(AbstractMinecart.class, watched(master, facing, door)).isEmpty()) {
				watch.seen(door, now);
			}
			boolean wanted = watch.lastSeen(door) != Long.MIN_VALUE && now - watch.lastSeen(door) < HOLD_TICKS;
			if (wanted == state.getValue(open)) {
				continue;
			}
			setAll(level, master, facing, open, wanted);
			state = level.getBlockState(master);
			BlockPos at = doorway(master, facing, door);
			level.blockEvent(master, this, DOOR, door * 2 + (wanted ? 1 : 0));
			level.playSound(null, at, wanted ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 0.8F, 0.8F);
			if (wanted && door == 0) {
				level.blockEvent(master, this, SPOOK, 0);
				level.playSound(null, at.above(2), Midway.sound("entity.witch.celebrate", SoundEvents.WOOD_HIT), SoundSource.BLOCKS, 1.0F, 0.8F);
				level.sendParticles(ParticleTypes.SOUL, at.getX() + 0.5, at.getY() + 2.6, at.getZ() + 0.5, 6, 0.3, 0.2, 0.3, 0.02);
			}
		}
	}

	/** A door moving or the ghost springing: its block entity marks the time, on every side, for the renderer. */
	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		if (!(level.getBlockEntity(pos) instanceof GhostTrainBlockEntity watch)) {
			return false;
		}
		if (id == DOOR) {
			watch.changed(param / 2, level.getGameTime());
		} else if (id == SPOOK) {
			watch.mark(level.getGameTime());
		} else {
			return false;
		}
		return true;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new GhostTrainBlockEntity(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide() || part(state) != MASTER || type != JugcraftAgriculture.GHOST_TRAIN_ENTITY) {
			return null;
		}
		return (tickLevel, pos, tickState, entity) -> ((GhostTrainBlockEntity) entity).serverTick((ServerLevel) tickLevel);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(IN_OPEN, OUT_OPEN, RUNNER);
	}
}
