package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Haunted Mailbox (Front-Yard Haunt Scenes, part 1; tools/front_yard.py MAILBOX): a large rural mailbox of black sheet
 * steel on a weathered post, one block across and two up ({@link MultiDecorationBlock}). An empty hand raises its red
 * flag or lowers it ({@link #FLAG}, on both blocks). It is haunted, in each player's own sight of it
 * (client/HauntedMailboxRenderer, with {@link HauntedMailboxBlockEntity}): every {@value #RATTLE_TICKS} ticks or so its
 * door rattles open a crack ({@link #lid}), and while the player is within {@value #STARE} blocks its door falls open
 * {@value #OPEN} degrees and two eyes stare out of the dark inside, glowing. Its blockstate's {@link #RUNNER} models 1
 * and 2 are the door and the eyes. It holds nothing. Its model is tools/front_yard_data.py haunted_mailbox().
 */
public class HauntedMailboxBlock extends MultiDecorationBlock implements EntityBlock {
	public static final int RATTLE_TICKS = 160;
	public static final double STARE = 4.0;
	public static final float OPEN = 75.0F;
	/** The door's hinge along its foot, across the box (pixels: height and depth, facing north). */
	public static final float HINGE_Y = 17.0F;
	public static final float HINGE_Z = 2.75F;
	/** How long the door takes to fall open or shut, and a rattle lasts (ticks). */
	public static final int SWING_TICKS = 6;
	public static final int RATTLE_LENGTH = 20;
	/** Its blocks: 1 across, 2 up, 1 deep (tools/front_yard.py MAILBOX["size"]). */
	private static final int[][] CELLS = box(1, 2, 1);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	public static final BooleanProperty FLAG = BooleanProperty.create("flag");
	/** 0 for the mailbox; 1 its door and 2 its eyes, for the renderer. */
	public static final IntegerProperty RUNNER = IntegerProperty.create("runner", 0, 2);
	/** The post, the arm and the box. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {7.25, 0.0, 8.0, 8.75, 15.5, 9.5},
			new double[] {7.0, 15.5, 4.0, 9.0, 17.0, 12.5}, new double[] {5.5, 17.0, 2.5, 11.0, 22.25, 13.0});

	public HauntedMailboxBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(FLAG, false).setValue(RUNNER, 0));
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

	/**
	 * How far open its door stands, in degrees: with someone near ({@code near}), falling open over
	 * {@value #SWING_TICKS} ticks {@code since} they came; with no one, falling shut over as long after they went and
	 * then, every {@value #RATTLE_TICKS} ticks of game time {@code time} (put off by {@code seed}, so no two boxes rattle
	 * together), rattling open a crack twice over {@value #RATTLE_LENGTH} ticks.
	 */
	public static float lid(boolean near, float since, float time, int seed) {
		float swung = Math.min(1.0F, Math.max(0.0F, since) / SWING_TICKS);
		float eased = swung * swung * (3.0F - 2.0F * swung);
		if (near) {
			return OPEN * eased;
		}
		if (swung < 1.0F) {
			return OPEN * (1.0F - eased);
		}
		float at = Math.floorMod((long) Math.floor(time) + seed, RATTLE_TICKS) + (time - (float) Math.floor(time));
		if (at >= RATTLE_LENGTH) {
			return 0.0F;
		}
		return 14.0F * (float) Math.abs(Math.sin(at * Math.PI / (RATTLE_LENGTH / 2.0F))) * (1.0F - at / RATTLE_LENGTH);
	}

	/** Whether the eyes show with the door open {@code lid} degrees: once it is well open, but for a blink now and then. */
	public static boolean eyes(float lid, float time, int seed) {
		return lid > 30.0F && Math.floorMod((long) Math.floor(time) + seed, 53) >= 3;
	}

	/** Raises or lowers its flag, on both blocks. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		BlockPos master = masterPos(pos, state);
		Direction facing = state.getValue(FACING);
		boolean raised = !state.getValue(FLAG);
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(master, facing, part);
			BlockState there = level.getBlockState(at);
			if (there.is(this) && part(there) == part && there.getValue(FACING) == facing) {
				level.setBlock(at, there.setValue(FLAG, raised), Block.UPDATE_ALL);
			}
		}
		level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.5F, raised ? 1.2F : 0.9F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new HauntedMailboxBlockEntity(pos, state) : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FLAG, RUNNER);
	}
}
