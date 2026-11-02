package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Rising Zombie (Front-Yard Haunt Scenes, part 2; tools/front_yard.py RISING_ZOMBIE): a zombie clawing its way out of
 * a mound of dug earth, out to its waist, one block across and two up ({@link MultiDecorationBlock}). Its arms, raised and
 * leaning out from its shoulders (its blockstate's {@link #RUNNER} models 1 and 2, the left and the right), claw at the
 * air ({@link #claw}), and the nearer a player comes the higher they reach ({@link #lurch}), in each player's own sight of
 * it (client/RisingZombieRenderer). An empty hand makes it groan. It holds nothing. Its model is
 * tools/front_yard_data.py rising_zombie().
 */
public class RisingZombieBlock extends MultiDecorationBlock implements EntityBlock {
	/** Its arms claw to and fro this many degrees, once in {@value #CLAW_TICKS} ticks. */
	public static final float CLAW = 12.0F;
	public static final int CLAW_TICKS = 70;
	/** Within this many blocks its arms reach higher, up to {@value #LURCH} degrees nearer upright. */
	public static final double REACH = 5.0;
	public static final float LURCH = 20.0F;
	/** Its right shoulder (pixels: across, height and depth, facing north); the left's is its mirror. */
	public static final float SHOULDER_X = 11.5F;
	public static final float SHOULDER_Y = 10.5F;
	public static final float SHOULDER_Z = 8.0F;
	/** Its blocks: 1 across, 2 up, 1 deep (tools/front_yard.py RISING_ZOMBIE["size"]). */
	private static final int[][] CELLS = box(1, 2, 1);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** 0 for the zombie; 1 its left arm and 2 its right, for the renderer. */
	public static final IntegerProperty RUNNER = IntegerProperty.create("runner", 0, 2);
	/** The mound, the torso and the head. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {1.0, 0.0, 2.0, 15.0, 3.0, 14.0},
			new double[] {4.5, 3.0, 6.5, 11.5, 11.0, 9.5}, new double[] {6.0, 11.0, 6.5, 10.0, 15.5, 9.5});

	public RisingZombieBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(RUNNER, 0));
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
	 * Its {@code right} (or left) arm's claw at game time {@code time}, degrees forward of where it leans: to and fro
	 * {@value #CLAW} degrees once in {@value #CLAW_TICKS} ticks, the two arms out of step, put off by {@code seed} so no
	 * two zombies claw together.
	 */
	public static float claw(boolean right, float time, int seed) {
		double phase = (time + Math.floorMod(seed, CLAW_TICKS)) * 2.0 * Math.PI / CLAW_TICKS + (right ? Math.PI : 0.0);
		return CLAW * (float) Math.sin(phase);
	}

	/** How much higher its arms reach with a player {@code distance} blocks away: nothing beyond {@value #REACH}, rising to {@value #LURCH} degrees. */
	public static float lurch(double distance) {
		return LURCH * (float) Mth.clamp(1.0 - distance / REACH, 0.0, 1.0);
	}

	/** An empty hand makes it groan. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide()) {
			level.playSound(null, pos, SoundEvents.ZOMBIE_AMBIENT, SoundSource.BLOCKS, 0.8F, 0.8F + level.getRandom().nextFloat() * 0.2F);
			level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new DecorationBlockEntity(JugcraftAgriculture.RISING_ZOMBIE_ENTITY, pos, state) : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNER);
	}
}
