package io.github.jimbozoomer.jugcraft.lair;

import io.github.jimbozoomer.jugcraft.lair.GlassChapel.Mirror;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * A Chapel Mirror (docs/features/glass-chapel.md): a tall mirror in a brass frame on a turntable, two blocks high
 * ({@link #HALF}), one of the Glass Chapel's three ({@link #MIRROR}). A lair fixture: any player in the chapel turns it
 * by using either block of it, to its next {@link #STOP stop}, round; the angle each stop faces is the mirror's own
 * ({@link Mirror#step}). The Abbot (part 2) marks two mirrors before his beam ({@link #MARKED}: their frames burn gold)
 * and veils one ({@link #VEILED}: its glass clouds over, its turntable's pointer still showing where it faces). Both
 * blocks always carry the same state but their half; everything happens on the server.
 */
public class ChapelMirrorBlock extends Block {
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	public static final EnumProperty<Mirror> MIRROR = EnumProperty.create("mirror", Mirror.class);
	public static final IntegerProperty STOP = IntegerProperty.create("stop", 0, Mirror.STOPS - 1);
	public static final BooleanProperty MARKED = BooleanProperty.create("marked");
	public static final BooleanProperty VEILED = BooleanProperty.create("veiled");
	/** The light a marked mirror's frame gives. */
	public static final int MARKED_LIGHT = 7;
	// The turntable, frame and glass stand in the middle of the block, whichever way it faces.
	private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

	public ChapelMirrorBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(MIRROR, Mirror.EAST)
				.setValue(STOP, Mirror.REST).setValue(MARKED, false).setValue(VEILED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF, MIRROR, STOP, MARKED, VEILED);
	}

	/** The light a mirror's block gives: a marked one's frame burns. */
	public static int light(BlockState state) {
		return state.getValue(MARKED) ? MARKED_LIGHT : 0;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level instanceof ServerLevel server) {
			turn(server, pos, player);
		}
		return InteractionResult.SUCCESS;
	}

	/** The mirror's lower block, from either of its blocks, or null where {@code pos} is no mirror. */
	public static @Nullable BlockPos lower(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof ChapelMirrorBlock)) {
			return null;
		}
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
	}

	/** Turns the mirror that {@code pos} is part of to its next stop, for {@code player} (null for nobody); its new stop, or -1. */
	public static int turn(ServerLevel level, BlockPos pos, @Nullable Player player) {
		BlockPos base = lower(level, pos);
		if (base == null) {
			return -1;
		}
		int stop = (level.getBlockState(base).getValue(STOP) + 1) % Mirror.STOPS;
		set(level, base, STOP, stop);
		level.playSound(null, base.above(), SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.5F, 1.6F);
		level.playSound(null, base.above(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.2F);
		level.sendParticles(ParticleTypes.END_ROD, base.getX() + 0.5, base.getY() + 1.4, base.getZ() + 0.5, 4, 0.2, 0.5, 0.2, 0.01);
		return stop;
	}

	/** The stop the mirror whose lower block is {@code base} stands at, or -1. */
	public static int stop(Level level, BlockPos base) {
		BlockState state = level.getBlockState(base);
		return state.getBlock() instanceof ChapelMirrorBlock ? state.getValue(STOP) : -1;
	}

	/** Sets the mirror whose lower block is {@code base} to {@code stop}; whether it changed. */
	public static boolean setStop(Level level, BlockPos base, int stop) {
		return set(level, base, STOP, Math.floorMod(stop, Mirror.STOPS));
	}

	/** Marks or unmarks the mirror whose lower block is {@code base}; whether it changed. */
	public static boolean setMarked(Level level, BlockPos base, boolean marked) {
		return set(level, base, MARKED, marked);
	}

	/** Veils or unveils the mirror whose lower block is {@code base}; whether it changed. */
	public static boolean setVeiled(Level level, BlockPos base, boolean veiled) {
		return set(level, base, VEILED, veiled);
	}

	/** Every mirror of the instance at {@code origin} back to rest, unmarked and unveiled. */
	public static void reset(Level level, BlockPos origin) {
		for (Mirror mirror : Mirror.values()) {
			BlockPos base = mirror.base(origin);
			setStop(level, base, Mirror.REST);
			setMarked(level, base, false);
			setVeiled(level, base, false);
		}
	}

	private static <T extends Comparable<T>> boolean set(Level level, BlockPos base, Property<T> property,
			T value) {
		boolean changed = false;
		for (BlockPos pos : new BlockPos[] {base, base.above()}) {
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() instanceof ChapelMirrorBlock && !state.getValue(property).equals(value)) {
				level.setBlock(pos, state.setValue(property, value), Block.UPDATE_ALL);
				changed = true;
			}
		}
		return changed;
	}
}
