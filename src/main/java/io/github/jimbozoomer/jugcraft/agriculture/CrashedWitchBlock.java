package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * The Crashed Witch (Front-Yard Haunt Scenes, part 4; tools/front_yard.py CRASHED_WITCH): a witch who flew her broom into
 * a tree or a wall, out to her hips in it, her ragged skirt bunched round the hole and her striped legs and pointed boots
 * sticking out, her snapped broom by her. She hangs on the side of a solid block or a log ({@link WallDecorationBlock}).
 * Her legs (its blockstate's {@link #RUNNER} models 1 and 2, the left and the right) splay {@value #SPLAY} degrees from her
 * hips and kick ({@link #kick}, client/CrashedWitchRenderer): in a burst now and then, the whole time while a player is
 * within {@value #NEAR} blocks, and for a while after she cackles. An empty hand makes her cackle, for everyone. It holds
 * nothing. Its model is tools/front_yard_data.py crashed_witch().
 */
public class CrashedWitchBlock extends WallDecorationBlock implements EntityBlock {
	/** Each kick, degrees either way, once in {@value #KICK_TICKS} ticks; a burst of {@value #BURST} ticks every {@value #EVERY}. */
	public static final float KICK = 25.0F;
	public static final int KICK_TICKS = 12;
	public static final int BURST = 36;
	public static final int EVERY = 160;
	public static final double NEAR = 6.0;
	/** Her hips at the wall (pixels: height and depth, facing north; across, either side of the middle), and her legs' splay. */
	public static final float HIP_Y = 9.5F;
	public static final float HIP_Z = 14.0F;
	public static final float HIP_X = 1.25F;
	public static final float SPLAY = 22.5F;
	/** 0 for the witch; 1 her left leg and 2 her right, for the renderer. */
	public static final IntegerProperty RUNNER = IntegerProperty.create("runner", 0, 2);
	private static final int CACKLE = 1;

	public CrashedWitchBlock(Properties properties) {
		super(properties, 14.0, 3.0, 13.5);
		registerDefaultState(defaultBlockState().setValue(RUNNER, 0));
	}

	/**
	 * Her {@code right} (or left) leg's kick at game time {@code time}, degrees up from straight out: {@value #KICK} either
	 * way once in {@value #KICK_TICKS} ticks, her legs out of step. She kicks in a burst of {@value #BURST} ticks every
	 * {@value #EVERY} (put off by {@code seed}, so no two witches kick together), the whole time while a player is
	 * {@code distance} blocks off within {@value #NEAR} (easing in over the last block), and for a burst's length
	 * {@code cackled} ticks after she cackles.
	 */
	public static float kick(boolean right, float time, int seed, double distance, float cackled) {
		float into = (time + Math.floorMod(seed, EVERY)) % EVERY;
		float burst = into < BURST ? (float) Math.sin(Math.PI * into / BURST) : 0.0F;
		float near = (float) Mth.clamp(NEAR + 1.0 - distance, 0.0, 1.0);
		float cackle = cackled >= 0.0F && cackled < BURST ? (float) Math.sin(Math.PI * cackled / BURST) : 0.0F;
		float amount = Math.max(burst, Math.max(near, cackle));
		return KICK * amount * (float) Math.sin(time * 2.0 * Math.PI / KICK_TICKS + (right ? Math.PI : 0.0));
	}

	/** An empty hand makes her cackle and kick, for everyone; not again until she has done. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide()) {
			long last = level.getBlockEntity(pos) instanceof DecorationBlockEntity watch ? watch.marked() : Long.MIN_VALUE;
			if (last == Long.MIN_VALUE || level.getGameTime() - last >= BURST) {
				level.playSound(null, pos, SoundEvents.WITCH_CELEBRATE, SoundSource.BLOCKS, 0.8F, 1.0F);
				level.blockEvent(pos, this, CACKLE, 0);
				level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
			}
		}
		return InteractionResult.SUCCESS;
	}

	/** A cackle: her block entity marks the time, on every side, for the renderer. */
	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		if (id != CACKLE || !(level.getBlockEntity(pos) instanceof DecorationBlockEntity watch)) {
			return false;
		}
		watch.mark(level.getGameTime());
		return true;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DecorationBlockEntity(JugcraftAgriculture.CRASHED_WITCH_ENTITY, pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNER);
	}
}
