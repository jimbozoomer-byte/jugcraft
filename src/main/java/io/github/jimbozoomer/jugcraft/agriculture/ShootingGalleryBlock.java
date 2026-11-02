package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Shooting Gallery (Haunted Midway, part 3; tools/haunted_midway.py GALLERY): a booth three blocks across and two up
 * ({@link MultiDecorationBlock}), a counter across its front, a night sky on its back wall and two gold rails across it.
 * On the upper rail {@value #PER_RAIL} tin bats run one way and on the lower {@value #PER_RAIL} tin ghosts the other,
 * at {@value #SPEED} pixels a tick, going round behind covers at the rails' ends ({@link #targetX}: by the game's clock
 * alone, so every client draws them where the server judges them). A Midway Ticket used on it starts a round of
 * {@value #ROUND_TICKS} ticks for whoever paid ({@link #LIT} while it runs, its bulbs lit;
 * {@link ShootingGalleryBlockEntity} keeps who paid, when it ends and which targets are down). A projectile of theirs
 * (an arrow, a snowball, anything shot or thrown) that reaches the back wall, shot from {@value #MIN_DISTANCE} blocks or
 * more, is followed back along its path to each rail, the nearer first: where it crossed a rail on a standing target
 * in sight, that target is knocked down (client/ShootingGalleryRenderer lays it flat). All {@value #TARGETS} down win
 * a prize from the midway's prize table ({@link Midway#prize}) and end the round; when the time runs out first, it is
 * lost and they stand again. Its two targets are the blockstate's {@link #RUNNER} models 1 (a bat) and 2 (a ghost),
 * which a placed gallery never takes. Its model is tools/haunted_midway_data.py shooting_gallery().
 */
public class ShootingGalleryBlock extends MultiDecorationBlock implements EntityBlock {
	public static final int PER_RAIL = 4;
	public static final int TARGETS = 2 * PER_RAIL;
	public static final float SPEED = 0.25F;
	public static final int ROUND_TICKS = 600;
	public static final double MIN_DISTANCE = 2.0;
	public static final float COUNTER = 14.0F;
	public static final float BOARD = 15.0F;
	/** The rails, the upper (bats) first: the height they stand on, their depth and which way they run (pixels). */
	public static final float[] RAIL_Y = {20.5F, 16.0F};
	public static final float[] RAIL_Z = {13.5F, 11.5F};
	public static final int[] WAYS = {1, -1};
	/** Each rail's targets' size, in pixels: a bat 6 across and 3 high, a ghost 5 and 5. */
	public static final float[] TARGET_W = {6.0F, 5.0F};
	public static final float[] TARGET_H = {3.0F, 5.0F};
	/** Where the targets run, across the frame (pixels), and the part of it in sight between the covers. */
	public static final float TRACK_FROM = 2.0F;
	public static final float TRACK_TO = 46.0F;
	public static final float SEEN_FROM = 5.0F;
	public static final float SEEN_TO = 43.0F;
	/** How long a target takes to go once round, in ticks. */
	public static final int PERIOD = Math.round((TRACK_TO - TRACK_FROM) / SPEED);
	/** Its blocks: 3 across, 2 up, 1 deep (tools/haunted_midway.py GALLERY["size"]). */
	private static final int[][] CELLS = box(3, 2, 1);
	private static final int ACROSS = 3;
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** 0 for the gallery; 1 and 2 are its targets' models (a bat, a ghost), for the renderer. */
	public static final IntegerProperty RUNNER = IntegerProperty.create("runner", 0, 2);
	/** The counter, the side walls, the back wall and the header and roof: the front between counter and header is open. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {0.0, 0.0, 0.0, 48.0, COUNTER, 4.5},
			new double[] {0.0, 0.0, 4.0, 1.0, 31.5, 15.0}, new double[] {47.0, 0.0, 4.0, 48.0, 31.5, 15.0},
			new double[] {1.0, 0.0, BOARD, 47.0, 32.0, 16.0}, new double[] {0.0, 25.0, 0.0, 48.0, 32.0, 0.5},
			new double[] {0.0, 31.5, 0.5, 48.0, 32.0, 16.0});

	public ShootingGalleryBlock(Properties properties) {
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
	 * Where target {@code target} on rail {@code rail} is at game time {@code time} plus {@code partial}, its middle across
	 * the frame in pixels: each rail's targets evenly spaced, running round its track {@link #PERIOD} ticks a turn.
	 */
	public static float targetX(int rail, int target, long time, float partial) {
		double length = TRACK_TO - TRACK_FROM;
		double along = target * length / PER_RAIL + WAYS[rail] * SPEED * (Math.floorMod(time, PERIOD) + partial);
		return (float) (TRACK_FROM + ((along % length) + length) % length);
	}

	/**
	 * {@code at} (a point in the world) in the gallery's frame, whose first block is at {@code master}, facing
	 * {@code facing}: {across (0 to 48 from its far end), up, in from its front}, in pixels.
	 */
	public static double[] frame(BlockPos master, Direction facing, Vec3 at) {
		double[] p = turned(facing, (at.x - master.getX()) * 16.0, (at.z - master.getZ()) * 16.0, true);
		return new double[] {p[0] + 16.0 * (ACROSS - 1), (at.y - master.getY()) * 16.0, p[1]};
	}

	/** A world vector {@code v} in the gallery's frame, as {@link #frame} turns points. */
	public static double[] frameVector(Direction facing, Vec3 v) {
		double[] p = turned(facing, v.x, v.z, false);
		return new double[] {p[0], v.y, p[1]};
	}

	/** World {@code x}, {@code z} (a point within a block's 16 pixels when {@code point}, else a direction) facing north. */
	private static double[] turned(Direction facing, double x, double z, boolean point) {
		double edge = point ? 16.0 : 0.0;
		return switch (facing) {
			case EAST -> new double[] {z, edge - x};
			case SOUTH -> new double[] {edge - x, edge - z};
			case WEST -> new double[] {edge - z, x};
			default -> new double[] {x, z};
		};
	}

	/**
	 * Which target a shot that reached the back wall at {@code hit} (frame pixels), travelling {@code motion} (frame), knocked
	 * at game time {@code time}, given which are already {@code down}: the nearer rail first, where its path crossed the
	 * rail's plane on a standing target in sight; -1 for none.
	 */
	public static int targetHit(double[] hit, double[] motion, long time, java.util.function.IntPredicate down) {
		for (int rail : new int[] {1, 0}) {
			double x = hit[0];
			double y = hit[1];
			if (motion[2] > 1.0E-6) {
				double back = (RAIL_Z[rail] - hit[2]) / motion[2];
				x += motion[0] * back;
				y += motion[1] * back;
			}
			double bottom = RAIL_Y[rail] + 0.5;
			if (y < bottom || y > bottom + TARGET_H[rail] || x < SEEN_FROM || x > SEEN_TO) {
				continue;
			}
			for (int k = 0; k < PER_RAIL; k++) {
				int target = rail * PER_RAIL + k;
				if (!down.test(target) && Math.abs(x - targetX(rail, k, time, 0.0F)) <= TARGET_W[rail] / 2.0) {
					return target;
				}
			}
		}
		return -1;
	}

	private void setLit(Level level, BlockPos master, Direction facing, boolean lit) {
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(master, facing, part);
			BlockState there = level.getBlockState(at);
			if (there.is(this) && part(there) == part && there.getValue(FACING) == facing) {
				level.setBlock(at, there.setValue(LIT, lit), Block.UPDATE_ALL);
			}
		}
	}

	private @Nullable ShootingGalleryBlockEntity round(Level level, BlockPos master) {
		return level.getBlockEntity(master) instanceof ShootingGalleryBlockEntity round ? round : null;
	}

	/** A Midway Ticket starts a round. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!stack.is(JugcraftAgriculture.item("midway_ticket"))) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (state.getValue(LIT)) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.shooting_gallery.playing"));
			return InteractionResult.CONSUME;
		}
		BlockPos master = masterPos(pos, state);
		ShootingGalleryBlockEntity round = round(level, master);
		if (round == null || !level.mayInteract(player, master)) {
			return InteractionResult.CONSUME;
		}
		stack.consume(1, player);
		round.start(player.getUUID(), level.getGameTime() + ROUND_TICKS);
		setLit(level, master, state.getValue(FACING), true);
		level.scheduleTick(master, this, ROUND_TICKS);
		level.playSound(null, pos, Midway.sound("block.note_block.bell", SoundEvents.WOOD_HIT), SoundSource.BLOCKS, 0.8F, 1.2F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** With no round on, an empty hand is told how to start one. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!state.getValue(LIT) && !level.isClientSide()) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.shooting_gallery.ticket"));
		}
		return InteractionResult.PASS;
	}

	/** A projectile reached the gallery: if it is the round's player's, at its back wall, it may knock a target down. */
	@Override
	protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
		if (level instanceof ServerLevel server && projectile.getOwner() instanceof ServerPlayer shooter) {
			shot(server, hit.getBlockPos(), state, shooter, hit.getLocation(), projectile.getDeltaMovement());
		}
	}

	/**
	 * {@code shooter}'s shot reached the gallery (any block of it, at {@code pos}) at {@code at}, travelling {@code motion}
	 * (both in the world). It counts only during a round, for its player, allowed to use blocks there, from
	 * {@value #MIN_DISTANCE} blocks or more and at the back wall. Returns the target knocked down, or -1.
	 */
	public int shot(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer shooter, Vec3 at, Vec3 motion) {
		BlockPos master = masterPos(pos, state);
		ShootingGalleryBlockEntity round = round(level, master);
		if (!state.getValue(LIT) || round == null || !shooter.getUUID().equals(round.player())
				|| !JugcraftConfig.isFeatureEnabled(JugcraftAgriculture.FEATURE) || !level.mayInteract(shooter, master)) {
			return -1;
		}
		if (shooter.position().distanceTo(at) < MIN_DISTANCE) {
			shooter.sendOverlayMessage(Component.translatable("message.jugcraft.shooting_gallery.too_close"));
			return -1;
		}
		Direction facing = state.getValue(FACING);
		double[] p = frame(master, facing, at);
		if (p[2] < BOARD - 0.1) {
			return -1;
		}
		int target = targetHit(p, frameVector(facing, motion), level.getGameTime(), round::isDown);
		if (target < 0 || !round.knock(target)) {
			return -1;
		}
		level.playSound(null, at.x, at.y, at.z, Midway.sound("block.note_block.chime", SoundEvents.WOOD_HIT), SoundSource.BLOCKS, 0.8F,
				1.4F + 0.05F * round.down());
		level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 6, 0.1, 0.1, 0.1, 0.1);
		if (round.down() >= TARGETS) {
			end(level, master, facing, round, true);
		}
		return target;
	}

	/** The round's clock: time up, it is lost (a round won early, or a later one, waits for its own end). */
	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		ShootingGalleryBlockEntity round = round(level, pos);
		if (part(state) != MASTER || !state.getValue(LIT) || round == null) {
			return;
		}
		long left = round.ends() - level.getGameTime();
		if (left > 0) {
			level.scheduleTick(pos, this, (int) Math.min(left, ROUND_TICKS));
			return;
		}
		end(level, pos, state.getValue(FACING), round, false);
	}

	/** Ends the round: a win gives its player a prize; either way the targets stand again and the bulbs go out. */
	private void end(ServerLevel level, BlockPos master, Direction facing, ShootingGalleryBlockEntity round, boolean won) {
		UUID payer = round.player();
		round.clear();
		setLit(level, master, facing, false);
		ServerPlayer player = payer == null ? null : level.getServer().getPlayerList().getPlayer(payer);
		if (won) {
			level.playSound(null, master.above(), Midway.sound("block.bell.use", SoundEvents.WOOD_HIT), SoundSource.BLOCKS, 1.5F, 1.2F);
			if (player != null) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.shooting_gallery.won"));
				Midway.prize(level, player, Vec3.atCenterOf(master.above()));
			}
		} else {
			level.playSound(null, master, Midway.sound("block.note_block.bass", SoundEvents.WOOD_HIT), SoundSource.BLOCKS, 0.8F, 0.6F);
			if (player != null) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.shooting_gallery.lost"));
			}
		}
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new ShootingGalleryBlockEntity(pos, state) : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNER);
	}
}
