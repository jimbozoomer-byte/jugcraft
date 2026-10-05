package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Giant's Beating Heart (Halloween decorations batch 17, the bigger jars): a giant's heart in a glass vat of red
 * murk, three blocks wide, tall and deep, placed and broken as one ({@link MultiDecorationBlock}, from the block aimed at
 * to the placer's right, up and away). It is a slow redstone clock like the Beating Heart Jar: it beats at one of
 * {@link #TEMPOS} beats a minute (use it to change), and each beat ({@link #BEAT}, on its first block) gives a
 * {@value #PULSE_TICKS}-tick signal of 15 from its first block to every side but the one below, with a deep heartbeat. A
 * redstone signal into any of its bottom blocks from below stops it. The beat is a scheduled tick on the first block,
 * so it does nothing between beats. The heart, swelling with each beat, is drawn by the client from the block entity
 * on its first block.
 */
public class GiantBeatingHeartBlock extends MultiDecorationBlock implements EntityBlock {
	public static final int SIZE = 3;
	private static final int[][] CELLS = box(SIZE, SIZE, SIZE);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** The part in the middle of the vat. */
	public static final int MIDDLE = 13;
	public static final int[] TEMPOS = {40, 50, 60, 72};
	public static final int PULSE_TICKS = 2;
	public static final BooleanProperty BEAT = BeatingHeartJarBlock.BEAT;
	public static final IntegerProperty TEMPO = BeatingHeartJarBlock.TEMPO;

	public GiantBeatingHeartBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(BEAT, false).setValue(TEMPO, 0));
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}

	/** The ticks from one beat to the next at {@code state}'s tempo. */
	public static int period(BlockState state) {
		return 1200 / TEMPOS[state.getValue(TEMPO)];
	}

	/** Whether a signal comes from below into any of the bottom blocks of the heart whose first block is {@code master}. */
	public boolean stopped(Level level, BlockPos master, Direction facing) {
		for (int part = 0; part < CELLS.length; part++) {
			if (CELLS[part][1] == 0 && level.hasSignal(partPos(master, facing, part).below(), Direction.DOWN)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
		super.onPlace(state, level, pos, old, moved);
		if (!old.is(this) && part(state) == MASTER) {
			level.scheduleTick(pos, this, period(state));
		}
	}

	/** Only the first block beats: on, then off after the pulse, then on again a beat later unless stopped. */
	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (part(state) != MASTER) {
			return;
		}
		if (state.getValue(BEAT)) {
			level.setBlock(pos, state.setValue(BEAT, false), Block.UPDATE_ALL);
			level.scheduleTick(pos, this, Math.max(1, period(state) - PULSE_TICKS));
			return;
		}
		if (stopped(level, pos, state.getValue(FACING))) {
			return;
		}
		level.setBlock(pos, state.setValue(BEAT, true), Block.UPDATE_ALL);
		level.playSound(null, partPos(pos, state.getValue(FACING), MIDDLE), SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 1.0F, 0.7F);
		level.scheduleTick(pos, this, PULSE_TICKS);
	}

	/** A signal going from under it starts it again. */
	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, @Nullable Orientation orientation, boolean moved) {
		super.neighborChanged(state, level, pos, neighbor, orientation, moved);
		if (level.isClientSide()) {
			return;
		}
		BlockPos master = masterPos(pos, state);
		BlockState first = level.getBlockState(master);
		if (first.is(this) && part(first) == MASTER && !stopped(level, master, first.getValue(FACING))
				&& !level.getBlockTicks().hasScheduledTick(master, this)) {
			level.scheduleTick(master, this, period(first));
		}
	}

	/** Use any block of it to change its tempo. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!player.getMainHandItem().isEmpty()) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			BlockPos master = masterPos(pos, state);
			BlockState first = level.getBlockState(master);
			if (!first.is(this) || part(first) != MASTER) {
				return InteractionResult.PASS;
			}
			BlockState next = first.setValue(TEMPO, (first.getValue(TEMPO) + 1) % TEMPOS.length);
			level.setBlock(master, next, Block.UPDATE_ALL);
			level.playSound(null, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.6F, 0.8F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, master);
			player.sendOverlayMessage(Component.translatable("message.jugcraft.giant_beating_heart.tempo", TEMPOS[next.getValue(TEMPO)]));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	/** Each beat from its first block to every side but the one below, so it can't power what stops it. */
	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return part(state) == MASTER && state.getValue(BEAT) && direction != Direction.UP ? 15 : 0;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}

	/** Its glass lets the light through, so the murk and the heart in its middle are lit like the room round it. */
	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new DecorationBlockEntity(JugcraftAgriculture.GIANT_HEART_ENTITY, pos, state) : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BEAT, TEMPO);
	}
}
