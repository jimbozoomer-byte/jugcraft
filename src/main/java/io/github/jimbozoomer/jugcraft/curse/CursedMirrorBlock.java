package io.github.jimbozoomer.jugcraft.curse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Cursed Mirror (docs/features/cursed-mirror.md): a gilt-framed standing mirror. It faces whoever places it, hangs
 * flat against the block behind it, and does nothing but stand there — until a player breaks it, which brings
 * {@value #CURSE_DAYS} days of vanilla Bad Luck ({@link MobEffects#UNLUCK}). At night a faint face flickers in the pane,
 * drawn only on the client in {@link #animateTick}. No saved state and nothing on a server tick: the curse is applied
 * once, on the break event, to the breaker alone.
 */
public class CursedMirrorBlock extends HorizontalDirectionalBlock {
	/** How many in-game days of Bad Luck breaking the mirror brings. */
	public static final int CURSE_DAYS = 7;
	/** The Bad Luck duration, in ticks ({@value #CURSE_DAYS} days of 24000 ticks). */
	public static final int CURSE_TICKS = CURSE_DAYS * 24000;
	private static final VoxelShape NORTH = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
	private static final VoxelShape SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
	private static final VoxelShape WEST = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape EAST = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);

	public CursedMirrorBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case SOUTH -> SOUTH;
			case WEST -> WEST;
			case EAST -> EAST;
			default -> NORTH;
		};
	}

	/**
	 * A player who shatters the mirror (not in creative) is given Bad Luck for {@value #CURSE_DAYS} days, once. The
	 * block still drops: super's behaviour is unchanged, so nothing is lost and no curse is applied on a tick.
	 */
	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (level instanceof ServerLevel server && player instanceof ServerPlayer breaker && !breaker.isCreative()
				&& !breaker.isSpectator()) {
			breaker.addEffect(new MobEffectInstance(MobEffects.UNLUCK, CURSE_TICKS, 0));
			server.playSound(null, pos, SoundType.GLASS.getBreakSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	/** Client only: in the dark a pale wisp of a face sometimes shows just in front of the pane, then is gone. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (level.isBrightOutside() || random.nextInt(40) != 0) {
			return;
		}
		Direction facing = state.getValue(FACING);
		double x = pos.getX() + 0.5 + facing.getStepX() * 0.15;
		double y = pos.getY() + 0.45 + random.nextDouble() * 0.5;
		double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.15;
		level.addParticle(ParticleTypes.SOUL, x, y, z, 0.0, 0.01, 0.0);
	}
}
