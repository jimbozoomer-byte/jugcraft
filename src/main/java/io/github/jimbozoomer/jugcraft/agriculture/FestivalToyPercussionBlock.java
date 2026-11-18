package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Harvest Percussion Set (Thanksgiving festival toys, tools/thanksgiving_toys.py PERCUSSION;
 * docs/features/thanksgiving-toys.md): a laced drum, a washboard and a gourd shaker on a bench, each played by using
 * it. Where the use was aimed picks the instrument ({@link #aimed}): the gourd on the floor in front, else the drum to
 * the set's west, else the washboard. It plays that instrument's vanilla note-block sound (no new sound file) with a
 * note above it, and shows that instrument struck ({@link #STRUCK}) for {@value #COOLDOWN} ticks; until a scheduled
 * tick puts it back, nothing on the set plays again, so one set sounds at most once every {@value #COOLDOWN} ticks.
 *
 * <p>It has no block entity and holds nothing, so in a town playing it is a public interaction, like ringing a bell;
 * claimed land keeps it to the claim's members, and breaking it is protected everywhere as any block.
 */
public class FestivalToyPercussionBlock extends FacingDecorationBlock {
	public static final int COOLDOWN = 4;
	/** Where the aim divides the instruments, in pixels in the set's own frame facing north (the table's PERCUSSION). */
	public static final double SPLIT_X = 8.0;
	public static final double SHAKER_TOP = 4.0;
	public static final double SHAKER_Z = 4.5;

	/** Which instrument shows struck: none at rest. */
	public enum Instrument implements StringRepresentable {
		NONE("none", 1.0F), DRUM("drum", 1.0F), WASHBOARD("washboard", 0.8F), SHAKER("shaker", 1.4F);

		private final String name;
		private final float pitch;

		Instrument(String name, float pitch) {
			this.name = name;
			this.pitch = pitch;
		}

		public float pitch() {
			return pitch;
		}

		/** Its vanilla sound: the note block's bass drum, hat and snare. */
		public SoundEvent sound() {
			return switch (this) {
				case DRUM -> SoundEvents.NOTE_BLOCK_BASEDRUM.value();
				case WASHBOARD -> SoundEvents.NOTE_BLOCK_HAT.value();
				default -> SoundEvents.NOTE_BLOCK_SNARE.value();
			};
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public static final EnumProperty<Instrument> STRUCK = EnumProperty.create("struck", Instrument.class);

	/** {@code box}: its outline {x0, y0, z0, x1, y1, z1} in pixels, facing north. */
	public FestivalToyPercussionBlock(Properties properties, double[] box) {
		super(properties, box);
		registerDefaultState(defaultBlockState().setValue(STRUCK, Instrument.NONE));
	}

	/**
	 * The instrument a use aimed at {@code hit} (a point in the world) plays, for a set at {@code pos} facing
	 * {@code facing}: the point is taken into the set's own frame (pixels, facing north), then the gourd if it is low and
	 * in front, else the drum to the west of the middle, else the washboard.
	 */
	public static Instrument aimed(BlockPos pos, Direction facing, Vec3 hit) {
		double[] p = local(pos, facing, hit);
		if (p[1] < SHAKER_TOP && p[2] < SHAKER_Z) {
			return Instrument.SHAKER;
		}
		return p[0] < SPLIT_X ? Instrument.DRUM : Instrument.WASHBOARD;
	}

	/**
	 * A point in the world as pixels {x, y, z} in the frame of a block at {@code pos} turned to {@code facing}: the frame
	 * its model is drawn in facing north (the inverse of the blockstate's turn, LongDecorationBlock.turned).
	 */
	public static double[] local(BlockPos pos, Direction facing, Vec3 point) {
		double x = (point.x - pos.getX()) * 16.0, y = (point.y - pos.getY()) * 16.0, z = (point.z - pos.getZ()) * 16.0;
		return switch (facing) {
			case EAST -> new double[] {z, y, 16.0 - x};
			case SOUTH -> new double[] {16.0 - x, y, 16.0 - z};
			case WEST -> new double[] {16.0 - z, y, x};
			default -> new double[] {x, y, z};
		};
	}

	/** Plays the instrument aimed at, unless one was played within the cooldown. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level instanceof ServerLevel server) {
			play(server, pos, state, aimed(pos, state.getValue(FACING), hit.getLocation()));
		}
		return InteractionResult.SUCCESS;
	}

	/** Plays {@code instrument} now and shows it struck; returns false, playing nothing, while the set is cooling down. */
	public static boolean play(ServerLevel level, BlockPos pos, BlockState state, Instrument instrument) {
		if (!(state.getBlock() instanceof FestivalToyPercussionBlock set) || instrument == Instrument.NONE
				|| state.getValue(STRUCK) != Instrument.NONE) {
			return false;
		}
		level.setBlock(pos, state.setValue(STRUCK, instrument), Block.UPDATE_CLIENTS);
		level.scheduleTick(pos, set, COOLDOWN);
		level.playSound(null, pos, instrument.sound(), SoundSource.RECORDS, 1.0F, instrument.pitch());
		level.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, instrument.ordinal() / 4.0, 0.0, 0.0, 1.0);
		level.gameEvent(null, GameEvent.NOTE_BLOCK_PLAY, pos);
		return true;
	}

	/** The cooldown's end: the instrument comes to rest and the set can be played again. */
	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getValue(STRUCK) != Instrument.NONE) {
			level.setBlock(pos, state.setValue(STRUCK, Instrument.NONE), Block.UPDATE_CLIENTS);
		}
	}

	/** A set placed mid-stroke by other means still comes to rest. */
	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
		super.onPlace(state, level, pos, old, moved);
		if (!level.isClientSide() && !old.is(this) && state.getValue(STRUCK) != Instrument.NONE
				&& !level.getBlockTicks().hasScheduledTick(pos, this)) {
			level.scheduleTick(pos, this, COOLDOWN);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(STRUCK);
	}
}
