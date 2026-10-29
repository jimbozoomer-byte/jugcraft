package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Porch Swing (Fall Garden & Porch, part 3; tools/fall_garden.py SWING): a slatted bench swing two blocks across,
 * hung by chains from two hooks in the ceiling two blocks up ({@link MultiDecorationBlock}). It needs a sturdy ceiling
 * over both its upper blocks, and comes down whole when either goes. Two can sit on it, one on each lower block
 * ({@link Seat}). It sways on its chains about the hooks, {@value #BREEZE_SWING} degrees each way in the breeze and
 * {@value #SITTER_SWING} with someone on it: client/PorchSwingRenderer draws the swing from the blockstate's
 * {@link #RUNNER} model of its first block, which a placed swing never takes; the block's own models are the hooks.
 */
public class PorchSwingBlock extends MultiDecorationBlock implements EntityBlock, Seat.Sittable {
	/** Its blocks: 2 across, 2 up, 1 deep (tools/fall_garden.py SWING["size"]); parts 0 and 1 are the seat's. */
	private static final int[][] CELLS = box(2, 2, 1);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	public static final BooleanProperty RUNNER = BooleanProperty.create("runner");
	/** How high the seat is above the floor, in blocks. */
	public static final double SEAT_HEIGHT = 0.5;
	/** The sway: degrees each way, empty and with a sitter, and ticks a sway takes. */
	public static final float BREEZE_SWING = 1.5F;
	public static final float SITTER_SWING = 8.0F;
	public static final float SWING_TICKS = 70.0F;
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {4.0, 5.5, 3.75, 28.0, 8.0, 12.0},
			new double[] {4.0, 8.0, 11.0, 28.0, 17.5, 12.5}, new double[] {2.0, 8.0, 7.5, 3.0, 31.5, 8.5}, new double[] {29.0, 8.0, 7.5, 30.0, 31.5, 8.5});

	public PorchSwingBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(RUNNER, false));
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
	public double seatHeight(BlockState state) {
		return SEAT_HEIGHT;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING))[part(state)];
	}

	private static boolean upper(int part) {
		return part >= 2;
	}

	/** Whether the ceiling over an upper block at {@code pos} will hold a hook. */
	private static boolean hung(LevelReader level, BlockPos pos) {
		BlockPos ceiling = pos.above();
		return level.getBlockState(ceiling).isFaceSturdy(level, ceiling, Direction.DOWN);
	}

	/** Placed as any prop is, if the ceiling over both its upper blocks is sturdy. */
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState placed = super.getStateForPlacement(context);
		if (placed == null) {
			return null;
		}
		Direction facing = placed.getValue(FACING);
		for (int part = 0; part < CELLS.length; part++) {
			if (upper(part) && !hung(context.getLevel(), partPos(context.getClickedPos(), facing, part))) {
				return null;
			}
		}
		return placed;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return !upper(part(state)) || hung(level, pos);
	}

	/** It comes down whole when the ceiling over its hooks goes. */
	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighbor, RandomSource random) {
		return direction == Direction.UP && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
				: super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
	}

	/** Sits the player down on the seat below where they used it (it has no lanterns to light). */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		}
		BlockPos master = masterPos(pos, state);
		BlockPos seat = partPos(master, state.getValue(FACING), part(state) % 2);
		if (level instanceof ServerLevel server && !Seat.sit(server, seat, level.getBlockState(seat), player)) {
			return InteractionResult.PASS;
		}
		return InteractionResult.SUCCESS;
	}

	/** How far the swing leans on its chains at {@code time} (ticks, with the partial tick), in degrees. */
	public static float swing(BlockPos master, boolean occupied, float time) {
		float most = occupied ? SITTER_SWING : BREEZE_SWING;
		float phase = (master.hashCode() & 0xFF) / 256.0F;
		return most * (float) Math.sin((time / SWING_TICKS + phase) * Math.PI * 2);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new DecorationBlockEntity(JugcraftAgriculture.PORCH_SWING_ENTITY, pos, state) : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNER);
	}
}
