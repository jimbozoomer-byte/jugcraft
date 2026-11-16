package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Tabletop Corn Toss Set (Thanksgiving festival toys, tools/thanksgiving_toys.py CORN_TOSS;
 * docs/features/thanksgiving-toys.md): a little slanted board with a hole and four rust and four cream beanbags in
 * their racks, for two players nearby (or one playing both colours). A use tosses the next bag, rust and cream by turns:
 * <ul>
 * <li>from in front of the set, {@value #MIN_DISTANCE} to {@value #MAX_DISTANCE} blocks off (flat, eyes to the board's
 * middle), at most one toss each {@value #COOLDOWN} ticks; otherwise it says why and nothing changes;</li>
 * <li>the bag lands where the thrower's look crosses the board's height, scattered a little more the further off they
 * stand; the server works this out from its own view of the player, never from what a client reports;</li>
 * <li>in the hole it scores {@value #HOLE_POINTS}, on the board 1, off it nothing; a bag on the board shows there
 * (the blockstate's {@link #RUST_BOARD} and {@link #CREAM_BOARD}); the rack shows the bags still to toss
 * ({@link #TOSSED});</li>
 * <li>the players within {@value #HEAR} blocks are told the toss and the score;</li>
 * <li>after all {@value #TOSSES} tosses the round is over until someone sneaks and uses the set, which gathers the bags
 * and clears the score (a sneak-use gathers them at any time).</li>
 * </ul>
 * Nothing is wagered, won, paid or given: the score is only words. The score is kept by its block entity
 * ({@link FestivalToyCornTossBlockEntity}); being a block entity, a town keeps it to those who may build there, as
 * claimed land keeps it to the claim's members.
 */
public class FestivalToyCornTossBlock extends FacingDecorationBlock implements EntityBlock {
	public static final int BAGS = 4;
	public static final int TOSSES = 2 * BAGS;
	public static final int HOLE_POINTS = 3;
	public static final int BOARD_POINTS = 1;
	public static final int COOLDOWN = 10;
	public static final double MIN_DISTANCE = 2.0;
	public static final double MAX_DISTANCE = 5.0;
	public static final double FRONT_COS = 0.5;
	/** Blocks of scatter for each block of distance. */
	public static final double SCATTER = 0.04;
	public static final double HEAR = 8.0;
	/** The hole's middle and the board's extent seen from above, and the hole's height (pixels, facing north). */
	public static final double HOLE_X = 8.0;
	public static final double HOLE_Z = 10.5;
	public static final double HOLE_R = 1.5;
	public static final double BOARD_X0 = 3.5;
	public static final double BOARD_Z0 = 2.75;
	public static final double BOARD_X1 = 12.5;
	public static final double BOARD_Z1 = 13.25;
	public static final double AIM_HEIGHT = 5.7;
	public static final IntegerProperty TOSSED = IntegerProperty.create("tossed", 0, TOSSES);
	public static final IntegerProperty RUST_BOARD = IntegerProperty.create("rust_board", 0, BAGS);
	public static final IntegerProperty CREAM_BOARD = IntegerProperty.create("cream_board", 0, BAGS);

	/** What a use came to. */
	public enum Toss {
		HOLE, BOARD, MISS, TOO_CLOSE, TOO_FAR, NOT_IN_FRONT, COOLING_DOWN, ROUND_OVER;

		public boolean tossed() {
			return this == HOLE || this == BOARD || this == MISS;
		}
	}

	/** {@code box}: its outline {x0, y0, z0, x1, y1, z1} in pixels, facing north. */
	public FestivalToyCornTossBlock(Properties properties, double[] box) {
		super(properties, box);
		registerDefaultState(defaultBlockState().setValue(TOSSED, 0).setValue(RUST_BOARD, 0).setValue(CREAM_BOARD, 0));
	}

	/** Whether the next bag is rust's: rust tosses first, then they take turns. */
	public static boolean rustsTurn(BlockState state) {
		return state.getValue(TOSSED) % 2 == 0;
	}

	/** Where a bag landing at {@code mx}, {@code mz} (pixels in the set's frame, facing north) scores. */
	public static Toss landing(double mx, double mz) {
		double dx = mx - HOLE_X, dz = mz - HOLE_Z;
		if (dx * dx + dz * dz <= HOLE_R * HOLE_R) {
			return Toss.HOLE;
		}
		return mx >= BOARD_X0 && mx <= BOARD_X1 && mz >= BOARD_Z0 && mz <= BOARD_Z1 ? Toss.BOARD : Toss.MISS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level instanceof ServerLevel server)) {
			return InteractionResult.SUCCESS;
		}
		if (player.isSecondaryUseActive()) {
			gather(server, pos, state);
			return InteractionResult.SUCCESS;
		}
		RandomSource random = server.getRandom();
		toss(server, pos, state, player, random.nextGaussian(), random.nextGaussian());
		return InteractionResult.SUCCESS;
	}

	/**
	 * A toss by {@code player} at the set at {@code pos}, the bag's scatter {@code spreadX} and {@code spreadZ} standard
	 * deviations each way (the use passes random ones; a test may pass 0). Checks the round, the cooldown, the distance
	 * and that the player is in front; then lands the bag where the player looks, scores it, shows it and tells the
	 * players near. Returns what came of it.
	 */
	public static Toss toss(ServerLevel level, BlockPos pos, BlockState state, Player player, double spreadX, double spreadZ) {
		if (!(state.getBlock() instanceof FestivalToyCornTossBlock) || !(level.getBlockEntity(pos) instanceof FestivalToyCornTossBlockEntity game)) {
			return Toss.ROUND_OVER;
		}
		if (state.getValue(TOSSED) >= TOSSES) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.corn_toss.round_over", game.rust(), game.cream()));
			return Toss.ROUND_OVER;
		}
		long now = level.getGameTime();
		if (!game.ready(now)) {
			return Toss.COOLING_DOWN;
		}
		Vec3 eye = player.getEyePosition();
		double cx = pos.getX() + 0.5, cz = pos.getZ() + 0.5;
		double dx = eye.x - cx, dz = eye.z - cz;
		double distance = Math.sqrt(dx * dx + dz * dz);
		if (distance < MIN_DISTANCE) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.corn_toss.too_close"));
			return Toss.TOO_CLOSE;
		}
		if (distance > MAX_DISTANCE) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.corn_toss.too_far"));
			return Toss.TOO_FAR;
		}
		Direction facing = state.getValue(FACING);
		if ((dx * facing.getStepX() + dz * facing.getStepZ()) / distance < FRONT_COS) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.corn_toss.in_front"));
			return Toss.NOT_IN_FRONT;
		}
		Toss result = Toss.MISS;
		Vec3 look = player.getViewVector(1.0F);
		double plane = pos.getY() + AIM_HEIGHT / 16.0;
		if (look.y < -1.0E-4 && eye.y > plane) {
			double t = (plane - eye.y) / look.y;
			double spread = SCATTER * distance;
			Vec3 lands = new Vec3(eye.x + look.x * t + spreadX * spread, plane, eye.z + look.z * t + spreadZ * spread);
			double[] at = FestivalToyPercussionBlock.local(pos, facing, lands);
			result = landing(at[0], at[2]);
		}
		boolean rust = rustsTurn(state);
		int points = result == Toss.HOLE ? HOLE_POINTS : result == Toss.BOARD ? BOARD_POINTS : 0;
		game.scored(rust, points, now);
		BlockState next = state.setValue(TOSSED, state.getValue(TOSSED) + 1);
		if (result == Toss.BOARD) {
			next = rust ? next.setValue(RUST_BOARD, Math.min(BAGS, next.getValue(RUST_BOARD) + 1))
					: next.setValue(CREAM_BOARD, Math.min(BAGS, next.getValue(CREAM_BOARD) + 1));
		}
		level.setBlock(pos, next, Block.UPDATE_CLIENTS);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		switch (result) {
			case HOLE -> {
				level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8F, 0.8F);
				level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.5F, 0.6F);
			}
			case BOARD -> level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
			default -> level.playSound(null, pos, SoundEvents.WOOL_HIT, SoundSource.BLOCKS, 0.6F, 0.8F);
		}
		Component side = Component.translatable(rust ? "message.jugcraft.corn_toss.rust" : "message.jugcraft.corn_toss.cream");
		String key = "message.jugcraft.corn_toss." + (result == Toss.HOLE ? "hole" : result == Toss.BOARD ? "board" : "miss");
		tell(level, pos, Component.translatable(key, side, game.rust(), game.cream()));
		if (next.getValue(TOSSED) >= TOSSES) {
			tell(level, pos, Component.translatable("message.jugcraft.corn_toss.final", game.rust(), game.cream()));
		}
		return result;
	}

	/** Gathers the bags back into their racks and clears the score: a new round. */
	public static void gather(ServerLevel level, BlockPos pos, BlockState state) {
		if (!(state.getBlock() instanceof FestivalToyCornTossBlock)) {
			return;
		}
		level.setBlock(pos, state.setValue(TOSSED, 0).setValue(RUST_BOARD, 0).setValue(CREAM_BOARD, 0), Block.UPDATE_CLIENTS);
		if (level.getBlockEntity(pos) instanceof FestivalToyCornTossBlockEntity game) {
			game.reset();
		}
		level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 0.6F, 1.2F);
		tell(level, pos, Component.translatable("message.jugcraft.corn_toss.gathered"));
	}

	/** Tells the players within {@value #HEAR} blocks of the set, over their hotbars. */
	private static void tell(ServerLevel level, BlockPos pos, Component message) {
		Vec3 middle = Vec3.atCenterOf(pos);
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(middle) <= HEAR * HEAR) {
				player.sendOverlayMessage(message);
			}
		}
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FestivalToyCornTossBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(TOSSED, RUST_BOARD, CREAM_BOARD);
	}
}
