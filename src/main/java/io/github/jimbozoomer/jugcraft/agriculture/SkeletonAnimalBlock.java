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
 * A skeleton animal of the front yard (Front-Yard Haunt Scenes, part 2; tools/front_yard.py SKELETON_DOG, SKELETON_CAT
 * and SKELETON_CROW): the bleached bones of a big dog sitting, a cat arched with its tail up or a crow perched, at real
 * size, facing whoever places it. An empty hand rattles its bones. Each has one moving part, its blockstate's
 * {@link #RUNNER} model 1, drawn by client/SkeletonAnimalRenderer about its {@link Kind#pivot}, in each player's own sight
 * of it ({@link SkeletonAnimalBlockEntity}): the dog's tail sweeps the ground ({@link #wag}) while that player is within
 * {@value #NEAR} blocks; the cat's tail sways ({@link #sway}); the crow's skull turns to watch them within {@value #WATCH}
 * blocks, in quick snaps ({@link #watch}). It holds nothing. Its models are tools/front_yard_data.py skeleton_dog(),
 * skeleton_cat() and skeleton_crow().
 */
public class SkeletonAnimalBlock extends FacingDecorationBlock implements EntityBlock {
	/** The dog's tail: degrees either way, ticks a wag, and how near (blocks) someone must be for it to wag. */
	public static final float WAG = 25.0F;
	public static final int WAG_TICKS = 10;
	public static final double NEAR = 6.0;
	/** The cat's tail: degrees either way, and ticks a sway. */
	public static final float SWAY = 15.0F;
	public static final int SWAY_TICKS = 60;
	/** The crow: how near (blocks) it watches someone, how far its head turns either way, and the ticks between snaps. */
	public static final double WATCH = 8.0;
	public static final float TURN = 70.0F;
	public static final int SNAP_TICKS = 12;
	/** How long (ticks) the crow's head takes to snap round. */
	public static final float SNAP_LENGTH = 2.0F;
	/** 0 for the animal; 1 its moving part, for the renderer. */
	public static final IntegerProperty RUNNER = IntegerProperty.create("runner", 0, 1);

	/** Which animal, with its shape (pixels, facing north), its moving part's pivot (pixels) and its rattle's pitch. */
	public enum Kind {
		DOG(new double[] {5.75, 0.0, 2.0, 10.25, 11.0, 14.5}, new float[] {8.0F, 0.5F, 12.0F}, 0.8F),
		CAT(new double[] {6.75, 0.0, 2.0, 9.25, 9.0, 12.5}, new float[] {8.0F, 3.75F, 10.85F}, 1.2F),
		CROW(new double[] {6.5, 0.0, 2.5, 9.5, 7.0, 11.0}, new float[] {8.0F, 5.5F, 5.25F}, 1.6F);

		private final double[] box;
		private final float[] pivot;
		private final float pitch;

		Kind(double[] box, float[] pivot, float pitch) {
			this.box = box;
			this.pivot = pivot;
			this.pitch = pitch;
		}

		/** Its moving part's pivot {across, height, depth}, in pixels facing north. */
		public float[] pivot() {
			return pivot.clone();
		}
	}

	private final Kind kind;

	public SkeletonAnimalBlock(Properties properties, Kind kind) {
		super(properties, kind.box);
		this.kind = kind;
		registerDefaultState(defaultBlockState().setValue(RUNNER, 0));
	}

	public Kind kind() {
		return kind;
	}

	/**
	 * The dog's tail, degrees across the ground at game time {@code time}: with someone near ({@code near}), wagging
	 * {@value #WAG} degrees either way once in {@value #WAG_TICKS} ticks, its wag growing over the first wag {@code since}
	 * they came; with no one, dying away over a wag after they went, and then lying still.
	 */
	public static float wag(boolean near, float since, float time) {
		float eased = Math.min(1.0F, Math.max(0.0F, since) / WAG_TICKS);
		float amplitude = near ? eased : 1.0F - eased;
		return WAG * amplitude * (float) Math.sin(time * 2.0 * Math.PI / WAG_TICKS);
	}

	/** The cat's tail, degrees to one side at game time {@code time}: once each way in {@value #SWAY_TICKS} ticks, put off by {@code seed}. */
	public static float sway(float time, int seed) {
		return SWAY * (float) Math.sin((time + Math.floorMod(seed, SWAY_TICKS)) * 2.0 * Math.PI / SWAY_TICKS);
	}

	/**
	 * Where the crow's head turns at its {@code snap}th snap, degrees clockwise from straight ahead: toward whoever it is
	 * watching ({@code near}, {@code toward} being the way they are from it), as far as {@value #TURN} degrees either way;
	 * with no one near, a glance about, now this way, now that, put off by {@code seed}.
	 */
	public static float watch(boolean near, float toward, long snap, int seed) {
		if (near) {
			return Mth.clamp(toward, -TURN, TURN);
		}
		long hash = (snap * 0x9E3779B97F4A7C15L + seed) >>> 33;
		return (Math.floorMod(hash, 5) - 2) * 20.0F;
	}

	/** The crow's head {@code since} ticks into a snap from {@code from} to {@code to}: quick, then still. */
	public static float snapping(float from, float to, float since) {
		float t = Mth.clamp(since / SNAP_LENGTH, 0.0F, 1.0F);
		return from + (to - from) * t * (2.0F - t);
	}

	/** An empty hand rattles its bones. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide()) {
			level.playSound(null, pos, SoundEvents.BONE_BLOCK_HIT, SoundSource.BLOCKS, 0.8F, kind.pitch);
			level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SkeletonAnimalBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNER);
	}
}
