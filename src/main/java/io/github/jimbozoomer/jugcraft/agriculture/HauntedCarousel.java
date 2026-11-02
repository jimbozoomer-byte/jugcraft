package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Haunted Carousel (Haunted Midway, part 5; tools/haunted_midway.py CAROUSEL): the turning part of a travelling
 * merry-go-round {@value #WIDTH} blocks across, round its middle block ({@link HauntedCarouselBlock}), which it belongs to
 * and which drives it. Its deck turns with a mirrored column at its middle and a striped canopy over it, and on it, at
 * {@value #SLOTS} places round its outer ring, the Pumpkin Coach and black horses and bats by turns, with four more horses
 * on its inner ring beside the outer ones. The horses and bats ride up and down their poles {@value #BOBS} times a turn,
 * {@value #BOB} blocks either way, the bats beating their wings as they go. The client draws it
 * (client/HauntedCarouselRenderer.java) from tools/haunted_midway_data.py's quads.
 *
 * <p>The block hands it the kinetic energy it takes ({@link #power}): {@value #NEED} KE a tick turns it at full speed,
 * once round in {@value #TURN_TICKS} ticks, less turning it slower in proportion; it speeds up and slows down gently, as
 * the Ferris wheel does. While it turns its band organ plays a waltz of its own ({@link #MELODY}), a beat every
 * {@value #BEAT_TICKS} ticks (unless the block's organ is switched off), and while it turns or anyone rides, its lights
 * are on and its block gives light.
 *
 * <p>Riders sit on the mounts ({@link #SEATS} seats: two in the coach, one on each horse and bat): a player boards
 * through the block ({@link #board}) onto the free mount nearest them, and turns with it, facing the way it goes. Getting
 * off sets them down on the ground just outside their mount. If a block stands where a mount is about to go, the carousel
 * stops (it is jammed) until it is cleared.
 */
public class HauntedCarousel extends Entity {
	public static final int WIDTH = 7;
	public static final int SLOTS = 8;
	public static final double DECK = 0.1875;
	public static final double OUTER = 2.55;
	public static final double INNER = 1.45;
	/** The mounts round the outer ring from slot 0; the inner ring's horses are beside the outer ones at these slots. */
	public static final int COACH = 0;
	public static final int HORSE = 1;
	public static final int BAT = 2;
	private static final int[] OUTER_MOUNTS = {COACH, HORSE, BAT, HORSE, BAT, HORSE, BAT, HORSE};
	public static final int[] INNER_SLOTS = {1, 3, 5, 7};
	public static final int MOUNTS = SLOTS + INNER_SLOTS.length;
	/** Each kind's seat over the deck (blocks; a horse's or bat's at the middle of its ride), and back from its pole. */
	public static final double HORSE_SEAT = 1.25;
	public static final double BAT_SEAT = 1.15;
	public static final double COACH_SEAT = 0.45;
	public static final double SEAT_BACK = 0.125;
	/** The coach's two seats, either side of its middle across the ring. */
	public static final double COACH_ACROSS = 0.33;
	public static final int SEATS = MOUNTS + 1;
	public static final double BOB = 0.2;
	public static final int BOBS = 3;
	public static final double WING_REST = 10.0;
	public static final double WING_BEAT = 20.0;
	public static final long NEED = 8;
	public static final int TURN_TICKS = 240;
	public static final float FULL_SPEED = (float) (2.0 * Math.PI / TURN_TICKS);
	public static final float ACCEL = FULL_SPEED / 60.0F;
	public static final float DECEL = FULL_SPEED / 80.0F;
	public static final double BOARD_REACH = 5.0;
	public static final int BEAT_TICKS = 8;
	/**
	 * Its waltz, in E minor, three beats to a bar for eight bars: the flute's tune a note a beat (-1 a rest), and each
	 * bar's bass note (on its first beat) and chord note (on its second and third), in note-block steps (0 is F sharp).
	 */
	public static final int[] MELODY = {10, 13, 17, 22, -1, 21, 22, 18, 15, 17, -1, 13, 12, 15, 18, 17, 15, 12, 13, 12, 9, 10, -1, -1};
	public static final int[] BASS = {10, 10, 3, 10, 5, 5, 10, 10};
	public static final int[] CHORD = {13, 13, 18, 13, 21, 21, 13, 13};
	private static final int SYNC_EVERY = 20;
	private static final float MICRO = 1_000_000.0F;
	private static final float TURN = (float) (2.0 * Math.PI);

	private static final EntityDataAccessor<Integer> SPEED = SynchedEntityData.defineId(HauntedCarousel.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> ANGLE = SynchedEntityData.defineId(HauntedCarousel.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> JAMMED = SynchedEntityData.defineId(HauntedCarousel.class, EntityDataSerializers.BOOLEAN);
	/** Who sits in each seat: their entity id, or -1. */
	private static final List<EntityDataAccessor<Integer>> RIDERS = riders();

	private float angle;
	private float angleO;
	private float speed;
	private int seenAngle = Integer.MIN_VALUE;
	private long power;
	private int beat;
	/** The seat a player boarding is to take (server), and on the client the seat and angle the local rider was last turned at. */
	private int boarding = -1;
	private int steeredSeat = -1;
	private float steeredAngle;

	public HauntedCarousel(EntityType<? extends HauntedCarousel> type, Level level) {
		super(type, level);
		noPhysics = true;
		setNoGravity(true);
	}

	private static List<EntityDataAccessor<Integer>> riders() {
		List<EntityDataAccessor<Integer>> out = new ArrayList<>();
		for (int seat = 0; seat < SEATS; seat++) {
			out.add(SynchedEntityData.defineId(HauntedCarousel.class, EntityDataSerializers.INT));
		}
		return List.copyOf(out);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(SPEED, 0);
		builder.define(ANGLE, 0);
		builder.define(JAMMED, false);
		for (EntityDataAccessor<Integer> rider : RIDERS) {
			builder.define(rider, -1);
		}
	}

	/** Sets a carousel turning round the block at {@code centre}; returns it, or null if it couldn't be made. */
	public static @Nullable HauntedCarousel raise(ServerLevel level, BlockPos centre) {
		HauntedCarousel carousel = JugcraftAgriculture.HAUNTED_CAROUSEL.create(level, EntitySpawnReason.TRIGGERED);
		if (carousel == null) {
			return null;
		}
		carousel.setPos(centre.getX() + 0.5, centre.getY(), centre.getZ() + 0.5);
		return level.addFreshEntity(carousel) ? carousel : null;
	}

	// ---------------------------------------------------------------- state

	/** How far round it has turned, in radians (0 to 2 pi), between the last tick and this one. */
	public float angle(float partialTick) {
		float from = angleO;
		float to = angle;
		if (to < from - Math.PI) {
			to += TURN;
		}
		return Mth.lerp(partialTick, from, to);
	}

	public float angle() {
		return angle;
	}

	/** Its speed, in radians a tick (full speed {@link #FULL_SPEED}). */
	public float speed() {
		return level().isClientSide() ? entityData.get(SPEED) / MICRO : speed;
	}

	public boolean jammed() {
		return entityData.get(JAMMED);
	}

	/** Whether its lights are on: while it turns or anyone rides it. */
	public boolean lit() {
		return speed() > 0.0F || isVehicle();
	}

	/** Turns it to {@code angle} at once (tests and the client test's pictures). */
	public void setAngle(float angle) {
		this.angle = Mth.positiveModulo(angle, TURN);
		angleO = this.angle;
		entityData.set(ANGLE, Math.round(this.angle * MICRO));
	}

	/** Sets its speed at once (tests and the client test's pictures). */
	public void setSpeed(float speed) {
		this.speed = Mth.clamp(speed, 0.0F, FULL_SPEED);
		entityData.set(SPEED, Math.round(this.speed * MICRO));
	}

	/** The block hands over {@code ke} kinetic energy for this tick. */
	public void power(long ke) {
		power += ke;
	}

	/** Where the block it turns round is. */
	public BlockPos centre() {
		return BlockPos.containing(getX(), getY() + 0.01, getZ());
	}

	// ---------------------------------------------------------------- geometry

	/** Which slot round the ring mount {@code mount} stands at: the outer ring's 0 to 7, then the inner ring's. */
	public static int slot(int mount) {
		return mount < SLOTS ? mount : INNER_SLOTS[mount - SLOTS];
	}

	/** What mount {@code mount} is: {@link #COACH}, {@link #HORSE} or {@link #BAT}. */
	public static int kind(int mount) {
		return mount < SLOTS ? OUTER_MOUNTS[mount] : HORSE;
	}

	public static double radius(int mount) {
		return mount < SLOTS ? OUTER : INNER;
	}

	/** Which way slot {@code slot} lies from the middle with the carousel at {@code angle} (radians, counter-clockwise from above). */
	public static float slotAngle(int slot, float angle) {
		return angle + slot * TURN / SLOTS;
	}

	/** How far mount {@code mount} has ridden up (or down, below 0) its pole, in blocks, with the carousel at {@code angle}. */
	public static double bob(int mount, float angle) {
		if (kind(mount) == COACH) {
			return 0.0;
		}
		double phase = slot(mount) * Math.PI / 2.0 + (mount < SLOTS ? 0.0 : Math.PI);
		return BOB * Math.sin(BOBS * angle + phase);
	}

	/**
	 * A bat's wings, raised by this many degrees (lowered below 0) with the carousel at {@code angle}: held {@value #WING_REST}
	 * degrees up, they beat {@value #WING_BEAT} degrees either way, up as it rises and down as it sinks.
	 */
	public static float flap(int mount, float angle) {
		double phase = slot(mount) * Math.PI / 2.0 + (mount < SLOTS ? 0.0 : Math.PI);
		return (float) (WING_REST + WING_BEAT * Math.cos(BOBS * angle + phase));
	}

	/** How high over the carousel's foot mount {@code mount}'s seat is (its model's origin, but the coach's, which is on the deck). */
	public static double seatHeight(int mount, float angle) {
		double seat = switch (kind(mount)) {
			case HORSE -> HORSE_SEAT;
			case BAT -> BAT_SEAT;
			default -> COACH_SEAT;
		};
		return DECK + seat + bob(mount, angle);
	}

	/** Which mount seat {@code seat} is on: the coach's two, then the rest of the mounts one each. */
	public static int mountOf(int seat) {
		return seat < 2 ? 0 : seat - 1;
	}

	/** Where seat {@code seat}'s rider sits, from the carousel's foot, with it at {@code angle}. */
	public static Vec3 seat(int seat, float angle) {
		int mount = mountOf(seat);
		double out = radius(mount) + (seat == 0 ? -COACH_ACROSS : seat == 1 ? COACH_ACROSS : 0.0);
		return new Vec3(out, seatHeight(mount, angle), SEAT_BACK).yRot(slotAngle(slot(mount), angle));
	}

	/** The way a rider on slot {@code slot} faces (the way it goes round), as a yaw in degrees. */
	public static float facing(int slot, float angle) {
		return 180.0F - (float) Math.toDegrees(slotAngle(slot, angle));
	}

	// ---------------------------------------------------------------- riders

	/** Which seat {@code entity} sits in, or -1. */
	public int seatOf(Entity entity) {
		for (int seat = 0; seat < SEATS; seat++) {
			if (entityData.get(RIDERS.get(seat)) == entity.getId()) {
				return seat;
			}
		}
		return -1;
	}

	/** Who sits in {@code seat}, if anyone. */
	public @Nullable Entity rider(int seat) {
		int id = entityData.get(RIDERS.get(seat));
		if (id < 0) {
			return null;
		}
		for (Entity passenger : getPassengers()) {
			if (passenger.getId() == id) {
				return passenger;
			}
		}
		return null;
	}

	/** The free seat nearest {@code from} (a place in the world), or -1. */
	public int freeSeat(Vec3 from) {
		int best = -1;
		double nearest = Double.MAX_VALUE;
		for (int seat = 0; seat < SEATS; seat++) {
			if (entityData.get(RIDERS.get(seat)) >= 0) {
				continue;
			}
			double away = position().add(seat(seat, angle)).distanceToSqr(from);
			if (away < nearest) {
				nearest = away;
				best = seat;
			}
		}
		return best;
	}

	/**
	 * {@code player} boards the free mount nearest them if they are within {@value #BOARD_REACH} blocks of its middle and
	 * ride nothing already; returns whether they did. Server side only.
	 */
	public boolean board(ServerPlayer player) {
		if (player.isPassenger() || player.isSpectator() || player.distanceToSqr(Vec3.atCenterOf(centre())) > BOARD_REACH * BOARD_REACH) {
			return false;
		}
		int seat = freeSeat(player.position());
		if (seat < 0) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.haunted_carousel.full"));
			return false;
		}
		boarding = seat;
		boolean boarded = player.startRiding(this);
		boarding = -1;
		return boarded;
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return level().isClientSide() || passenger instanceof Player && freeSeat(passenger.position()) >= 0;
	}

	@Override
	protected void addPassenger(Entity passenger) {
		super.addPassenger(passenger);
		if (!level().isClientSide() && seatOf(passenger) < 0) {
			int seat = boarding >= 0 && entityData.get(RIDERS.get(boarding)) < 0 ? boarding : freeSeat(passenger.position());
			if (seat >= 0) {
				entityData.set(RIDERS.get(seat), passenger.getId());
			}
		}
	}

	@Override
	protected void removePassenger(Entity passenger) {
		int seat = seatOf(passenger);
		super.removePassenger(passenger);
		if (!level().isClientSide() && seat >= 0) {
			entityData.set(RIDERS.get(seat), -1);
		}
	}

	/**
	 * Each rider sits on their mount, wherever it has gone round and however high it has ridden. On the client the
	 * player riding is turned with their mount here too (this is worked out once a tick for each rider, after the rider's
	 * own tick, as a boat turns its riders), so they face the way it goes.
	 */
	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
		int seat = seatOf(passenger);
		if (seat < 0) {
			return new Vec3(0.0, 1.0, 0.0);
		}
		if (level().isClientSide() && passenger instanceof Player player && player.isLocalPlayer()) {
			steer(player, seat);
		}
		return seat(seat, angle);
	}

	/**
	 * Turns the local player riding seat {@code seat} by as far as the carousel has turned since they were last turned,
	 * or, on a new seat, to face the way their mount goes. Doing it again in the same tick turns them no further.
	 */
	private void steer(Player rider, int seat) {
		int slot = slot(mountOf(seat));
		if (seat != steeredSeat) {
			steeredSeat = seat;
			float yaw = facing(slot, angle);
			rider.setYRot(yaw);
			rider.setYHeadRot(yaw);
			rider.yBodyRot = yaw;
		} else {
			float turned = Mth.wrapDegrees((float) Math.toDegrees(angle - steeredAngle));
			rider.setYRot(rider.getYRot() - turned);
			rider.setYHeadRot(rider.getYHeadRot() - turned);
			rider.yBodyRot -= turned;
		}
		steeredAngle = angle;
	}

	/** Getting off, a rider is set down on the ground just outside their mount, or else outside the carousel. */
	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
		Vec3 out = passenger.position().subtract(position()).multiply(1.0, 0.0, 1.0);
		double far = out.length();
		Vec3 way = far < 1.0E-3 ? new Vec3(0.0, 0.0, -1.0) : out.scale(1.0 / far);
		for (double reach : new double[] {Math.min(far + 0.8, WIDTH / 2.0 - 0.4), WIDTH / 2.0 + 0.6}) {
			for (int up = 0; up <= 2; up++) {
				Vec3 spot = position().add(way.scale(reach)).add(0.0, up, 0.0);
				AABB box = passenger.getBoundingBox().move(spot.subtract(passenger.position()));
				if (level().noCollision(passenger, box)) {
					return spot;
				}
			}
		}
		return Vec3.atBottomCenterOf(centre().above());
	}

	// ---------------------------------------------------------------- turning

	@Override
	public void tick() {
		super.tick();
		angleO = angle;
		if (level() instanceof ServerLevel server) {
			serverTick(server);
		} else {
			int synced = entityData.get(ANGLE);
			if (synced != seenAngle) {
				seenAngle = synced;
				angle = synced / MICRO;
				angleO = angle;
			}
			angle = Mth.positiveModulo(angle + speed(), TURN);
			if (angle < angleO - Math.PI) {
				angleO -= TURN;
			}
			boolean riding = false;
			for (Entity passenger : getPassengers()) {
				riding |= passenger instanceof Player player && player.isLocalPlayer();
			}
			if (!riding) {
				steeredSeat = -1;
			}
		}
	}

	private void serverTick(ServerLevel level) {
		BlockState block = level.getBlockState(centre());
		if (!(block.getBlock() instanceof HauntedCarouselBlock)) {
			// Its block is gone (an explosion, a command): the carousel goes too.
			ejectPassengers();
			discard();
			return;
		}
		long given = power;
		power = 0;
		float target = FULL_SPEED * Math.min(1.0F, given / (float) NEED);
		float next = speed + Mth.clamp(target - speed, -DECEL, ACCEL);
		boolean jammed = false;
		if (next > 0.0F && blocked(level, angle + next)) {
			next = 0.0F;
			jammed = true;
		}
		if (jammed && !jammed()) {
			level.playSound(null, getX(), getY() + 1.0, getZ(), Midway.sound("block.anvil.place", SoundEvents.CHAIN_PLACE), SoundSource.BLOCKS, 0.5F, 0.6F);
			for (Entity passenger : getPassengers()) {
				if (passenger instanceof ServerPlayer rider) {
					rider.sendOverlayMessage(Component.translatable("message.jugcraft.haunted_carousel.jammed"));
				}
			}
		}
		entityData.set(JAMMED, jammed);
		if (Math.abs(next - speed) > 1.0E-7F || next == 0.0F && speed != 0.0F) {
			speed = next;
			entityData.set(SPEED, Math.round(speed * MICRO));
		}
		if (speed > 0.0F) {
			angle = Mth.positiveModulo(angle + speed, TURN);
		}
		if (tickCount % SYNC_EVERY == 0) {
			entityData.set(ANGLE, Math.round(angle * MICRO));
		}
		if (block.getValue(HauntedCarouselBlock.LIT) != lit()) {
			level.setBlock(centre(), block.setValue(HauntedCarouselBlock.LIT, lit()), Block.UPDATE_ALL);
		}
		if (speed > 0.0F && tickCount % BEAT_TICKS == 0 && block.getValue(HauntedCarouselBlock.ORGAN)) {
			play(level, beat);
			beat = (beat + 1) % MELODY.length;
		}
	}

	/** The organ plays beat {@code beat} of its waltz: the tune, the bass on a bar's first beat and the chord on the others. */
	private void play(ServerLevel level, int beat) {
		int bar = beat / 3;
		double y = getY() + 1.0;
		if (MELODY[beat] >= 0) {
			note(level, y, SoundEvents.NOTE_BLOCK_FLUTE.value(), MELODY[beat], 0.7F);
		}
		if (beat % 3 == 0) {
			note(level, y, SoundEvents.NOTE_BLOCK_BASS.value(), BASS[bar], 0.8F);
		} else {
			note(level, y, SoundEvents.NOTE_BLOCK_HARP.value(), CHORD[bar], 0.35F);
		}
	}

	private void note(ServerLevel level, double y, SoundEvent sound, int step, float volume) {
		float pitch = (float) Math.pow(2.0, (step - 12) / 12.0);
		level.playSound(null, getX(), y, getZ(), sound, SoundSource.RECORDS, volume, pitch);
	}

	/** The beat its organ will play next (tests). */
	public int beat() {
		return beat;
	}

	/** Turns it on by {@code step} radians at once (tests). */
	public void advance(float step) {
		angle = Mth.positiveModulo(angle + step, TURN);
	}

	/**
	 * Whether a block would stand in a mount (or its rider) with the carousel at {@code next}: at each mount, the deck, its
	 * seat and its rider's head are checked.
	 */
	public boolean blocked(Level level, float next) {
		BlockPos centre = centre();
		for (int mount = 0; mount < MOUNTS; mount++) {
			float a = slotAngle(slot(mount), next);
			double seat = seatHeight(mount, next);
			for (double up : new double[] {DECK, seat, seat + 1.0}) {
				BlockPos pos = BlockPos.containing(position().add(new Vec3(radius(mount), up, 0.0).yRot(a)));
				if (!pos.equals(centre) && !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	// ---------------------------------------------------------------- an entity apart

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	/** The space the whole carousel takes up, for the client to tell when any of it is in view. */
	public AABB extent() {
		double reach = WIDTH / 2.0;
		return new AABB(getX() - reach, getY(), getZ() - reach, getX() + reach, getY() + HauntedCarouselBlock.HEIGHT, getZ() + reach);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		setAngle(input.getFloatOr("angle", 0.0F));
		setSpeed(input.getFloatOr("speed", 0.0F));
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putFloat("angle", angle);
		output.putFloat("speed", speed);
	}
}
