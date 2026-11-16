package io.github.jimbozoomer.jugcraft.pegasus;

import io.github.jimbozoomer.jugcraft.horses.Saddlebags;
import io.github.jimbozoomer.jugcraft.pegasus.PegasusFlight.State;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Pegasus (docs/features/pegasus.md): a vanilla horse in every way it is tamed, saddled, owned, fed, saved and
 * ridden on the ground, that flies when its pilot holds Ascend.
 *
 * <p><b>Who decides what.</b> The server owns the flight state ({@link PegasusFlight.State}, synced in {@link #FLIGHT}
 * with the climb/descend intent in {@link #VERTICAL}): it changes it only from the controlling passenger's validated
 * input ({@link #acceptInput}) and from where its own copy of the pegasus is (on the ground, in water, under a
 * ceiling), and sets {@code NoGravity} from it. As for every vanilla ridden horse, the movement itself is simulated by
 * the pilot's client and checked by vanilla's vehicle move checks on the server; the client flies only while the
 * synced state says so ({@link #travel}), and the server also checks every {@value PegasusFlight#CHECK_TICKS} ticks
 * that the flight was no faster than a pegasus can fly ({@link PegasusFlight#plausible}), throwing a rider off if not.
 * With nobody in control the server simulates the pegasus itself, as a slow glide to the ground.
 *
 * <p>No fall damage while it flies or within {@value PegasusFlight#LANDING_GRACE_TICKS} ticks of landing; on the
 * ground it falls as any horse. A rider who gets off in the air falls by vanilla's rules. Horse armour does not fit over
 * the wings.
 *
 * <p>Breeding (docs/features/horse-breeds.md#crossbreeding): a Pegasus pairs only with a Pegasus, and their foal is a
 * Pegasus (the one rule by which a foal flies); it never pairs with a ground breed, a vanilla horse or a donkey. Its
 * light cargo: a small pack strapped behind the saddle (horses/Saddlebags, one row), fitted with a chest while saddled.
 */
public class Pegasus extends Horse {
	private static final EntityDataAccessor<Integer> FLIGHT = SynchedEntityData.defineId(Pegasus.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> VERTICAL = SynchedEntityData.defineId(Pegasus.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> BAGS = SynchedEntityData.defineId(Pegasus.class, EntityDataSerializers.BOOLEAN);
	/** The light pack: one row of nine, on the saddle (HorseBreed.PEGASUS.cargoRows). */
	public static final int PACK_ROWS = 1;
	/** Saved: whether it was flying or gliding, so a pegasus unloaded in the air comes back gliding, not falling. */
	public static final String AIRBORNE_TAG = "PegasusAirborne";
	/** A sturdy exploration mount: fixed, not rolled like a wild horse's. */
	public static final double HEALTH = 30.0;
	public static final double SPEED = 0.3;
	public static final double JUMP = 0.7;
	private static final int SATURATED = 1_000_000;

	/** What became of a pilot's input packet. */
	public enum InputResult {
		ACCEPTED, NOT_RIDING, WRONG_VEHICLE, NOT_CONTROLLER, MALFORMED, STALE
	}

	// Server: the pilot's last accepted input, and the state machine's counters.
	private int input;
	private int inputAge = SATURATED;
	private int lastSequence;
	private @Nullable UUID inputFrom;
	private int ascendHeld;
	private int stateTicks;
	private int ticksSinceFlight = SATURATED;
	private @Nullable Vec3 checkFrom;
	private @Nullable Vec3 lastServerPos;
	private int checkTicks;
	// Client: the local pilot's keys (set by the client each tick) and the smoothed wings.
	private boolean pilotAscend;
	private boolean pilotDescend;
	private double wingOpen;
	private double wingOpenO;
	private double wingAmp;
	private double wingAmpO;
	private double wingRate = PegasusFlight.RATE_GLIDE;
	private double wingPhase;
	private double wingPhaseO;
	// Its light pack (horses/Saddlebags): stored, saved and dropped with the pegasus.
	private final Saddlebags pack = new Saddlebags(this, fitted -> entityData.set(BAGS, fitted));

	public Pegasus(EntityType<? extends Horse> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Horse.createBaseHorseAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.MOVEMENT_SPEED, SPEED)
				.add(Attributes.JUMP_STRENGTH, JUMP);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(FLIGHT, State.GROUNDED.ordinal());
		builder.define(VERTICAL, 0);
		builder.define(BAGS, false);
	}

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
			@Nullable SpawnGroupData group) {
		SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, group);
		getAttribute(Attributes.MAX_HEALTH).setBaseValue(HEALTH);
		getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(SPEED);
		getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(JUMP);
		setHealth(getMaxHealth());
		return data;
	}

	// ------------------------------------------------------------------------------------------------ synced state

	public State flightState() {
		return State.of(entityData.get(FLIGHT));
	}

	/** The server's accepted climb (1), descend (-1) or level (0) intent, for every observer's wings. */
	public int flightVertical() {
		return entityData.get(VERTICAL);
	}

	public boolean flightActive() {
		return flightState().airborne();
	}

	/** Server: enter {@code state}; gravity follows it. */
	void setFlight(State state) {
		if (flightState() != state) {
			entityData.set(FLIGHT, state.ordinal());
			stateTicks = 0;
			setNoGravity(state.airborne());
		}
		if (!state.airborne()) {
			entityData.set(VERTICAL, 0);
		}
	}

	// ------------------------------------------------------------------------------------------------ input

	/**
	 * The server's handling of a pilot packet, after {@link JugcraftPegasus#receive} matched the vehicle: only this
	 * pegasus's controlling passenger, only the two key bits, only a sequence newer than the last one from that pilot.
	 */
	public InputResult acceptInput(ServerPlayer player, int value, int sequence) {
		if (getControllingPassenger() != player) {
			return InputResult.NOT_CONTROLLER;
		}
		if (!PegasusFlight.valid(value)) {
			return InputResult.MALFORMED;
		}
		if (player.getUUID().equals(inputFrom) && !PegasusFlight.newer(sequence, lastSequence)) {
			return InputResult.STALE;
		}
		input = value;
		inputAge = 0;
		lastSequence = sequence;
		inputFrom = player.getUUID();
		return InputResult.ACCEPTED;
	}

	/** Forgets the pilot's input: on mounting, dismounting, loading and death. */
	void clearInput() {
		input = 0;
		inputAge = SATURATED;
		inputFrom = null;
		ascendHeld = 0;
	}

	/** The input the server would act on now (0 when stale or from someone else), for tests and the wing intent. */
	public int activeInput() {
		LivingEntity controller = getControllingPassenger();
		return controller != null && controller.getUUID().equals(inputFrom) && inputAge <= PegasusFlight.INPUT_TIMEOUT_TICKS ? input : 0;
	}

	/** Client: the local pilot's keys this tick (PegasusClient), used by {@link #travel} and the pilot's own wings. */
	public void pilotInput(boolean ascend, boolean descend) {
		pilotAscend = ascend;
		pilotDescend = descend;
	}

	// ------------------------------------------------------------------------------------------------ ticking

	@Override
	public void tick() {
		super.tick();
		if (level() instanceof ServerLevel) {
			serverFlightTick();
		} else {
			clientWingTick();
		}
		if (flightActive()) {
			resetFallDistance();
			ticksSinceFlight = 0;
		} else if (ticksSinceFlight < SATURATED) {
			ticksSinceFlight++;
		}
	}

	private void serverFlightTick() {
		LivingEntity controller = getControllingPassenger();
		boolean piloted = controller instanceof ServerPlayer;
		boolean fresh = piloted && controller.getUUID().equals(inputFrom) && inputAge <= PegasusFlight.INPUT_TIMEOUT_TICKS;
		boolean ascend = fresh && PegasusFlight.ascend(input);
		boolean descend = fresh && PegasusFlight.descend(input);
		ascendHeld = ascend ? Math.min(ascendHeld + 1, SATURATED) : 0;
		State state = flightState();
		// The room overhead matters only to a takeoff; look for it only then (two collision queries).
		boolean blocked = state == State.GROUNDED && ascend && blockedAbove(controller);
		State next = PegasusFlight.next(state, stateTicks, new PegasusFlight.Conditions(piloted, fresh, ascend, descend, ascendHeld,
				onGround(), isInWater() || isInLava(), blocked));
		stateTicks = Math.min(stateTicks + 1, SATURATED);
		setFlight(next);
		entityData.set(VERTICAL, next == State.TAKEOFF ? 1 : next == State.FLYING ? PegasusFlight.vertical(fresh ? input : 0) : 0);
		if (inputAge < SATURATED) {
			inputAge++;
		}
		checkPlausible(controller);
	}

	/** Every CHECK_TICKS of piloted flight: a flight faster than a pegasus flies throws its rider off. */
	private void checkPlausible(@Nullable LivingEntity controller) {
		Vec3 now = position();
		if (lastServerPos != null && now.distanceTo(lastServerPos) > PegasusFlight.TELEPORT_STEP) {
			checkFrom = null;                     // a teleport (vanilla refuses a client vehicle move this long)
		}
		lastServerPos = now;
		if (!(controller instanceof ServerPlayer pilot) || !flightActive()) {
			checkFrom = null;
			return;
		}
		if (checkFrom == null) {
			checkFrom = now;
			checkTicks = 0;
			return;
		}
		if (++checkTicks < PegasusFlight.CHECK_TICKS) {
			return;
		}
		Vec3 moved = now.subtract(checkFrom);
		checkFrom = now;
		checkTicks = 0;
		if (!PegasusFlight.plausible(moved.x, moved.y, moved.z, PegasusFlight.CHECK_TICKS)) {
			pilot.sendOverlayMessage(Component.translatable("message.jugcraft.pegasus.bucked"));
			ejectPassengers();
		}
	}

	/** No room to climb: a block over the pegasus or its rider, or the build limit. */
	boolean blockedAbove(@Nullable Entity rider) {
		return headroom(rider) < 0.1 || !level().noCollision(this, getBoundingBox().move(0.0, 0.1, 0.0))
				|| rider != null && !level().noCollision(rider, rider.getBoundingBox().move(0.0, 0.1, 0.0));
	}

	/** How much higher it may still go: to the top of the level's build limit, for the pegasus and its rider both. */
	double headroom(@Nullable Entity rider) {
		double top = getBoundingBox().maxY;
		if (rider != null) {
			top = Math.max(top, rider.getBoundingBox().maxY);
		}
		return level().getMaxY() + 1.0 - top;
	}

	private void clientWingTick() {
		wingOpenO = wingOpen;
		wingAmpO = wingAmp;
		wingPhaseO = wingPhase;
		State state = flightState();
		int vertical = flightVertical();
		if (state == State.FLYING && getControllingPassenger() instanceof Player pilot && pilot.isLocalPlayer()) {
			vertical = PegasusFlight.vertical(PegasusFlight.encode(pilotAscend, pilotDescend));   // the pilot sees its own keys at once
		}
		PegasusFlight.WingTargets target = PegasusFlight.wingTargets(state, vertical, isVehicle());
		wingOpen = PegasusFlight.toward(wingOpen, target.open(), PegasusFlight.WING_OPEN_RATE);
		wingAmp = PegasusFlight.toward(wingAmp, target.amp(), PegasusFlight.WING_AMP_RATE);
		wingRate = PegasusFlight.toward(wingRate, target.rate(), PegasusFlight.WING_RATE_RATE);
		wingPhase += wingRate;
		if (wingPhase > Math.PI * 64) {
			wingPhase -= Math.PI * 64;
			wingPhaseO -= Math.PI * 64;
		}
	}

	/** The smoothed wing values between ticks, for the renderer. */
	public float wingOpen(float partial) {
		return (float) (wingOpenO + (wingOpen - wingOpenO) * partial);
	}

	public float wingAmp(float partial) {
		return (float) (wingAmpO + (wingAmp - wingAmpO) * partial);
	}

	public float wingPhase(float partial) {
		return (float) (wingPhaseO + (wingPhase - wingPhaseO) * partial);
	}

	// ------------------------------------------------------------------------------------------------ movement

	/**
	 * Where it is simulated (its pilot's client, or the server when nobody controls it): a normal horse unless the
	 * synced state flies, then {@link #flyTravel}.
	 */
	@Override
	public void travel(Vec3 movement) {
		if (!flightActive()) {
			super.travel(movement);
			return;
		}
		LivingEntity controller = getControllingPassenger();
		boolean piloting = controller instanceof Player && level().isClientSide();
		flyTravel(movement, piloting && pilotAscend, piloting && pilotDescend, controller);
	}

	/**
	 * One tick of flight from the horse's ridden input (strafe, forward) and the pilot's keys: horizontal speed eased
	 * towards the steered target, vertical speed eased towards the state's target, no climbing into a ceiling or past the
	 * build limit (for the rider too), no flying out of the world border. Public so tests can stand in for a pilot's client.
	 */
	public void flyTravel(Vec3 movement, boolean ascend, boolean descend, @Nullable Entity rider) {
		State state = flightState();
		Vec3 motion = getDeltaMovement();
		double vx;
		double vz;
		if (rider == null) {
			vx = motion.x * PegasusFlight.RIDERLESS_DRAG;
			vz = motion.z * PegasusFlight.RIDERLESS_DRAG;
		} else {
			double speed = state == State.GLIDING ? PegasusFlight.GLIDE_SPEED : PegasusFlight.FLY_SPEED;
			double[] target = PegasusFlight.horizontalTarget(movement.x, movement.z, getYRot(), speed);
			double[] next = PegasusFlight.approach(motion.x, motion.z, target[0], target[1], PegasusFlight.H_ACCEL, PegasusFlight.H_DECEL);
			vx = next[0];
			vz = next[1];
		}
		boolean groundNear = !level().noCollision(this, getBoundingBox().move(0.0, -PegasusFlight.GROUND_NEAR, 0.0));
		double vy = PegasusFlight.ease(motion.y, PegasusFlight.verticalTarget(state, ascend, descend, groundNear), PegasusFlight.V_ACCEL);
		vy = PegasusFlight.clampClimb(vy, headroom(rider));
		if (vy > 0 && (!level().noCollision(this, getBoundingBox().move(0.0, vy, 0.0))
				|| rider != null && !level().noCollision(rider, rider.getBoundingBox().move(0.0, vy, 0.0)))) {
			vy = 0.0;                             // a ceiling over the pegasus or its rider's head
		}
		AABB ahead = getBoundingBox().move(vx, 0.0, vz);
		if (!level().getWorldBorder().isWithinBounds(ahead) && level().getWorldBorder().isWithinBounds(getBoundingBox())) {
			vx = 0.0;                             // no flying out through the border (a border closing in may still pass it)
			vz = 0.0;
		}
		Vec3 step = new Vec3(vx, vy, vz);
		setDeltaMovement(step);
		move(MoverType.SELF, step);
		if (verticalCollision) {
			setDeltaMovement(getDeltaMovement().x, 0.0, getDeltaMovement().z);
		}
		calculateEntityAnimation(false);
		resetFallDistance();
	}

	/** While flying, the vanilla charged jump (and its rearing) is off; on the ground it is the normal horse jump. */
	@Override
	public boolean canJump() {
		return super.canJump() && !flightActive();
	}

	@Override
	public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
		if (flightActive() || ticksSinceFlight <= PegasusFlight.LANDING_GRACE_TICKS) {
			return false;                         // a controlled landing; a grounded pegasus falls as any horse
		}
		return super.causeFallDamage(distance, multiplier, source);
	}

	// ------------------------------------------------------------------------------------------------ riders

	@Override
	protected void addPassenger(Entity passenger) {
		super.addPassenger(passenger);
		if (!level().isClientSide()) {
			clearInput();
		}
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		if (level().isClientSide()) {
			pilotInput(false, false);
			return;
		}
		clearInput();
		checkFrom = null;
		if (flightActive()) {
			setFlight(State.GLIDING);             // the pegasus glides down; the rider falls by vanilla's rules
		}
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (!level().isClientSide()) {
			clearInput();
			setFlight(State.GROUNDED);
		}
	}

	// ------------------------------------------------------------------------------------------------ horse rules

	/**
	 * A Pegasus pairs only with a Pegasus (HorseLineage.mayPair); vanilla's own conditions (tamed, adult, in love) still
	 * apply. A vanilla horse or donkey that wants it is refused by HorseLineageMixin.
	 */
	@Override
	public boolean canMate(Animal other) {
		return other instanceof Pegasus && super.canMate(other);
	}

	/** Two Pegasi have a Pegasus foal, with the line's fixed stats; nothing else has one. */
	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {
		return mate instanceof Pegasus ? JugcraftPegasus.PEGASUS.create(level, EntitySpawnReason.BREEDING) : null;
	}

	// ------------------------------------------------------------------------------------------------ the light pack

	public Saddlebags pack() {
		return pack;
	}

	/** Whether the pack is fitted (synced; drawn while the saddle is on too). */
	public boolean packFitted() {
		return entityData.get(BAGS);
	}

	/** The pack first (a chest fits it while saddled; sneak-use with a stick opens it); everything else is a horse's. */
	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		InteractionResult cargo = pack.interact(player, hand, PACK_ROWS, true,
				Component.translatable("container.jugcraft.saddlebags", getDisplayName()));
		return cargo != null ? cargo : super.mobInteract(player, hand);
	}

	/** A real death drops the pack and its contents once (a pet knock-out cancels the death and drops nothing). */
	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
		super.dropCustomDeathLoot(level, source, recentlyHit);
		pack.dropAll(level);
	}

	/** Horse armour does not fit over the wings (no body slot); saddle and everything else as a horse. */
	@Override
	public boolean canUseSlot(EquipmentSlot slot) {
		return slot != EquipmentSlot.BODY && super.canUseSlot(slot);
	}

	// ------------------------------------------------------------------------------------------------ saving

	@Override
	protected void addAdditionalSaveData(ValueOutput saved) {
		super.addAdditionalSaveData(saved);
		saved.putBoolean(AIRBORNE_TAG, flightActive());
		pack.save(saved);
	}

	/** Powered flight and input never survive a reload: in the air it comes back gliding, otherwise grounded. */
	@Override
	protected void readAdditionalSaveData(ValueInput saved) {
		super.readAdditionalSaveData(saved);
		clearInput();
		State state = saved.getBooleanOr(AIRBORNE_TAG, false) ? State.GLIDING : State.GROUNDED;
		entityData.set(FLIGHT, state.ordinal());
		entityData.set(VERTICAL, 0);
		stateTicks = 0;
		setNoGravity(state.airborne());
		pack.load(saved);
	}
}
