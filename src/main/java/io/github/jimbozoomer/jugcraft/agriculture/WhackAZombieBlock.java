package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Whack-a-Zombie (Haunted Midway, part 2; tools/haunted_midway.py WHACK): a game cabinet one block across and two up
 * ({@link MultiDecorationBlock}), its playfield at {@value #FIELD} pixels with five holes ({@link #HOLES_X},
 * {@link #HOLES_Z}) and a back board with {@value #LAMPS} score lamps. A Midway Ticket used on it starts a round of
 * {@value #ROUND_TICKS} ticks for whoever paid ({@link #LIT} while it runs; {@link WhackAZombieBlockEntity} keeps who
 * paid and when it ends). Square-faced zombie heads pop up one at a time, each from another hole than the last
 * ({@link #HEAD}, the hole's number from 1, or 0 for none, on both blocks): each stays up {@value #UP_TICKS} ticks, with
 * {@value #GAP_TICKS} between, on the first block's scheduled ticks, so nothing else ticks. A pop is a block event its
 * block entity marks, for client/WhackAZombieRenderer to raise the blockstate's {@link #RUNNER} model (a head) from that
 * hole over {@value #RISE_TICKS} ticks. The player who paid whacks an up head with a Carnival Mallet (left click,
 * {@link Midway#attack}, or use), from within {@value Midway#REACH} blocks: the server finds the hole nearest where they
 * hit, and if it is the head's, knocks it down and lights a lamp ({@link #SCORE}). All {@value #LAMPS} lit win a prize
 * from the midway's prize table ({@link Midway#prize}) and end the round; if its time runs out first it is lost. Its
 * model is tools/haunted_midway_data.py whack_a_zombie().
 */
public class WhackAZombieBlock extends MultiDecorationBlock implements EntityBlock {
	public static final float FIELD = 15.0F;
	public static final float HEAD_PIXELS = 4.0F;
	public static final int LAMPS = 8;
	public static final int ROUND_TICKS = 400;
	public static final int UP_TICKS = 20;
	public static final int GAP_TICKS = 10;
	public static final int RISE_TICKS = 4;
	/** The holes' middles on the playfield (pixels, facing north: across, and in from the front). */
	public static final float[] HOLES_X = {3.5F, 8.0F, 12.5F, 5.75F, 10.25F};
	public static final float[] HOLES_Z = {3.75F, 3.75F, 3.75F, 8.75F, 8.75F};
	/** How far from a hole's middle a whack still counts as on that hole, in pixels (half a head and its rim). */
	public static final double ON_HOLE = 2.5;
	/** Its blocks: 1 across, 2 up, 1 deep (tools/haunted_midway.py WHACK["size"]). */
	private static final int[][] CELLS = box(1, 2, 1);
	private static final int POP = 1;
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	public static final IntegerProperty HEAD = IntegerProperty.create("head", 0, HOLES_X.length);
	public static final IntegerProperty SCORE = IntegerProperty.create("score", 0, LAMPS);
	public static final BooleanProperty RUNNER = BooleanProperty.create("runner");
	/** The cabinet and playfield, and the back board; with a head up, that head too (so a whack can land on it). */
	private static final List<Map<Direction, VoxelShape[]>> SHAPES = new ArrayList<>();

	static {
		double[] cabinet = {0.5, 0.0, 0.5, 15.5, FIELD, 14.5};
		double[] board = {1.0, FIELD, 12.35, 15.0, 32.0, 14.75};
		SHAPES.add(shapesOf(CELLS, cabinet, board));
		double r = HEAD_PIXELS / 2.0;
		for (int hole = 0; hole < HOLES_X.length; hole++) {
			SHAPES.add(shapesOf(CELLS, cabinet, board,
					new double[] {HOLES_X[hole] - r, FIELD, HOLES_Z[hole] - r, HOLES_X[hole] + r, FIELD + HEAD_PIXELS, HOLES_Z[hole] + r}));
		}
	}

	public WhackAZombieBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(HEAD, 0).setValue(SCORE, 0).setValue(RUNNER, false));
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
		return SHAPES.get(state.getValue(HEAD)).get(state.getValue(FACING))[part(state)];
	}

	/** How far a head has risen {@code since} ticks (with the partial tick) after it popped up, from 0 (in its hole) to 1. */
	public static float rise(float since) {
		return since <= 0.0F ? 0.0F : Math.min(1.0F, since / RISE_TICKS);
	}

	/**
	 * Where {@code at} (a point in the world) is on the cabinet whose first block is at {@code master}, facing
	 * {@code facing}, in its model's pixels: {across, in from the front}, as its holes are given.
	 */
	public static double[] local(BlockPos master, Direction facing, Vec3 at) {
		double wx = (at.x - master.getX()) * 16.0;
		double wz = (at.z - master.getZ()) * 16.0;
		return switch (facing) {
			case EAST -> new double[] {wz, 16.0 - wx};
			case SOUTH -> new double[] {16.0 - wx, 16.0 - wz};
			case WEST -> new double[] {16.0 - wz, wx};
			default -> new double[] {wx, wz};
		};
	}

	/** The top of hole {@code hole}'s head, up, in the world, on the cabinet whose first block is at {@code master}. */
	public static Vec3 headTop(BlockPos master, Direction facing, int hole) {
		double x = HOLES_X[hole];
		double z = HOLES_Z[hole];
		double[] world = switch (facing) {
			case EAST -> new double[] {16.0 - z, x};
			case SOUTH -> new double[] {16.0 - x, 16.0 - z};
			case WEST -> new double[] {z, 16.0 - x};
			default -> new double[] {x, z};
		};
		return new Vec3(master.getX() + world[0] / 16.0, master.getY() + (FIELD + HEAD_PIXELS) / 16.0, master.getZ() + world[1] / 16.0);
	}

	/** The hole nearest {@code at} within {@value #ON_HOLE} pixels, or -1. */
	public static int holeAt(BlockPos master, Direction facing, Vec3 at) {
		double[] p = local(master, facing, at);
		int best = -1;
		double nearest = ON_HOLE * ON_HOLE;
		for (int hole = 0; hole < HOLES_X.length; hole++) {
			double dx = p[0] - HOLES_X[hole];
			double dz = p[1] - HOLES_Z[hole];
			if (dx * dx + dz * dz <= nearest) {
				nearest = dx * dx + dz * dz;
				best = hole;
			}
		}
		return best;
	}

	/** Sets {@code property} to {@code value} on every block of the cabinet whose first block is at {@code master}. */
	private <T extends Comparable<T>> void setAll(Level level, BlockPos master, Direction facing, Property<T> property, T value) {
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(master, facing, part);
			BlockState there = level.getBlockState(at);
			if (there.is(this) && part(there) == part && there.getValue(FACING) == facing) {
				level.setBlock(at, there.setValue(property, value), Block.UPDATE_ALL);
			}
		}
	}

	private @Nullable WhackAZombieBlockEntity round(Level level, BlockPos master) {
		return level.getBlockEntity(master) instanceof WhackAZombieBlockEntity round ? round : null;
	}

	/** A Midway Ticket starts a round; a Carnival Mallet used on it is a whack. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		boolean ticket = stack.is(JugcraftAgriculture.item("midway_ticket"));
		boolean mallet = stack.is(Midway.mallet());
		if (!ticket && !mallet) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer playing)) {
			return InteractionResult.SUCCESS;
		}
		if (mallet) {
			whack(server, pos, state, playing, hit.getLocation());
			return InteractionResult.SUCCESS;
		}
		if (state.getValue(LIT)) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.whack_a_zombie.playing"));
			return InteractionResult.CONSUME;
		}
		BlockPos master = masterPos(pos, state);
		WhackAZombieBlockEntity round = round(level, master);
		if (round == null || !level.mayInteract(player, master)) {
			return InteractionResult.CONSUME;
		}
		stack.consume(1, player);
		round.start(player.getUUID(), level.getGameTime() + ROUND_TICKS);
		Direction facing = state.getValue(FACING);
		setAll(level, master, facing, SCORE, 0);
		setAll(level, master, facing, HEAD, 0);
		setAll(level, master, facing, LIT, true);
		level.scheduleTick(master, this, GAP_TICKS);
		level.playSound(null, pos, Midway.sound("block.note_block.bell", SoundEvents.WOOD_HIT), SoundSource.BLOCKS, 0.8F, 1.2F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** With no round on, an empty hand is told how to start one. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!state.getValue(LIT) && !level.isClientSide()) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.whack_a_zombie.ticket"));
		}
		return InteractionResult.PASS;
	}

	/**
	 * {@code player} whacks the cabinet (any block of it, at {@code pos}) at {@code at}, a point in the world. Counts only
	 * for the round's player, within {@value Midway#REACH} blocks and allowed to use blocks there; a whack on the hole
	 * whose head is up knocks it down and lights a lamp. Returns whether it was a hit.
	 */
	public boolean whack(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player, Vec3 at) {
		BlockPos master = masterPos(pos, state);
		WhackAZombieBlockEntity round = round(level, master);
		if (!state.getValue(LIT) || round == null || !JugcraftConfig.isFeatureEnabled(JugcraftAgriculture.FEATURE)
				|| !level.mayInteract(player, master) || player.distanceToSqr(Vec3.atCenterOf(master)) > Midway.REACH * Midway.REACH) {
			return false;
		}
		if (!player.getUUID().equals(round.player())) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.whack_a_zombie.not_yours"));
			return false;
		}
		Direction facing = state.getValue(FACING);
		int up = state.getValue(HEAD) - 1;
		if (up < 0 || holeAt(master, facing, at) != up) {
			level.playSound(null, master, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.6F, 0.6F);
			return false;
		}
		int score = Math.min(LAMPS, state.getValue(SCORE) + 1);
		setAll(level, master, facing, HEAD, 0);
		setAll(level, master, facing, SCORE, score);
		Vec3 top = headTop(master, facing, up);
		level.playSound(null, top.x, top.y, top.z, Midway.sound("entity.zombie.hurt", SoundEvents.WOOD_HIT), SoundSource.BLOCKS, 0.7F, 1.4F);
		level.sendParticles(ParticleTypes.CRIT, top.x, top.y, top.z, 6, 0.1, 0.05, 0.1, 0.1);
		if (score >= LAMPS) {
			end(level, master, facing, round, true);
		}
		return true;
	}

	/** The round's clock: a head that was up goes down unwhacked; a new one pops up from another hole; time ends the round. */
	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		WhackAZombieBlockEntity round = round(level, pos);
		if (part(state) != MASTER || !state.getValue(LIT) || round == null) {
			return;
		}
		Direction facing = state.getValue(FACING);
		boolean over = level.getGameTime() >= round.ends();
		if (state.getValue(HEAD) > 0) {
			setAll(level, pos, facing, HEAD, 0);
			if (over) {
				end(level, pos, facing, round, false);
			} else {
				level.scheduleTick(pos, this, GAP_TICKS);
			}
			return;
		}
		if (over) {
			end(level, pos, facing, round, false);
			return;
		}
		int hole = random.nextInt(HOLES_X.length - (round.last() < 0 ? 0 : 1));
		if (round.last() >= 0 && hole >= round.last()) {
			hole++;
		}
		round.popped(hole);
		setAll(level, pos, facing, HEAD, hole + 1);
		level.blockEvent(pos, this, POP, hole);
		level.scheduleTick(pos, this, UP_TICKS);
	}

	/** Ends the round: a win gives its player a prize; either way the heads go down and the lamps show the score. */
	private void end(ServerLevel level, BlockPos master, Direction facing, WhackAZombieBlockEntity round, boolean won) {
		UUID payer = round.player();
		round.clear();
		setAll(level, master, facing, HEAD, 0);
		setAll(level, master, facing, LIT, false);
		ServerPlayer player = payer == null ? null : level.getServer().getPlayerList().getPlayer(payer);
		if (won) {
			level.playSound(null, master.above(), Midway.sound("block.bell.use", SoundEvents.WOOD_HIT), SoundSource.BLOCKS, 1.5F, 1.2F);
			if (player != null) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.whack_a_zombie.won"));
				Midway.prize(level, player, Vec3.atCenterOf(master.above()));
			}
		} else {
			level.playSound(null, master, Midway.sound("block.note_block.bass", SoundEvents.WOOD_HIT), SoundSource.BLOCKS, 0.8F, 0.6F);
			if (player != null) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.whack_a_zombie.lost"));
			}
		}
	}

	/** A head popped up: its block entity marks the time, for its rise. */
	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		if (id == POP && level.getBlockEntity(pos) instanceof DecorationBlockEntity cabinet) {
			cabinet.mark(level.getGameTime());
			return true;
		}
		return false;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new WhackAZombieBlockEntity(pos, state) : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HEAD, SCORE, RUNNER);
	}
}
