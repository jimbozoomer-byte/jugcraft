package io.github.jimbozoomer.jugcraft.artillery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A crewed gun (batch 51): the siege mortar, the self-propelled howitzer and the flak gun. The server works the gun
 * from its gunner's keys ({@link ArtilleryInputPayload}) and view:
 * <ul>
 * <li>With a target marked by a Range Finder ({@link Spotting}), the gun turns to it and finds the elevation that
 * lands its shell there ({@link Ballistics}).</li>
 * <li>Otherwise it aims where the gunner looks; a gun that only fires high (the mortar) lobs at the block the gunner
 * looks at.</li>
 * </ul>
 * It traverses and elevates at a limited rate, and fires when the gunner presses attack (or, for an automatic gun,
 * holds it) once it is on target and reloaded, using a shell from the gunner's inventory (none in creative). Players
 * knock it down into its item.
 */
public abstract class CrewedGun extends Entity {
	private static final int INPUT_TIMEOUT = 10;
	private static final EntityDataAccessor<Float> AIM_YAW = SynchedEntityData.defineId(CrewedGun.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> AIM_PITCH = SynchedEntityData.defineId(CrewedGun.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> FIRED_AT = SynchedEntityData.defineId(CrewedGun.class, EntityDataSerializers.INT);

	protected int forward;
	protected int turn;
	private int fire;
	protected int inputTick = -INPUT_TIMEOUT - 1;
	private int lastShot = -1000;
	private boolean fireHeld;
	private boolean ordered;
	private boolean warned;
	private float damage;
	private int shotAt = -100;

	protected CrewedGun(EntityType<? extends CrewedGun> type, Level level) {
		super(type, level);
	}

	// ---- what each gun defines

	/** The shell it fires and the item it uses for one. */
	protected abstract EntityType<ArtilleryShell> shellType();

	protected abstract Item ammo();

	protected abstract double shellSpeed();

	protected abstract double shellGravity();

	protected abstract int cooldown();

	/** Degrees a tick it traverses and elevates. */
	protected abstract float traverse();

	/** Elevation limits in degrees (up is positive). */
	protected abstract float minPitch();

	protected abstract float maxPitch();

	/** Whether it fires on the high arc (a mortar). */
	protected abstract boolean highArc();

	/** Whether holding attack keeps firing. */
	protected boolean automatic() {
		return false;
	}

	/** The gun's pivot (the point the barrel turns about) above its feet, and the barrel's length, in blocks. */
	protected abstract double pivotHeight();

	protected abstract double barrelLength();

	protected abstract Item dropItem();

	protected abstract int health();

	protected abstract int seats();

	/** Where a passenger sits, before turning with the gun. */
	protected abstract Vec3 seat(int index);

	/** Limits a wanted aim yaw (for a gun that can't turn all the way round). */
	protected float limitYaw(float yaw) {
		return yaw;
	}

	/** Called every server tick before aiming, with whether the driver's keys are live. */
	protected void move(ServerLevel level, boolean listening) {
		Vec3 motion = getDeltaMovement();
		setDeltaMovement(0, onGround() ? 0 : Math.max(motion.y - 0.08, -1.0), 0);
		move(MoverType.SELF, getDeltaMovement());
	}

	// ---- shared

	public float aimYaw(float partialTick) {
		return entityData.get(AIM_YAW);
	}

	public float aimPitch() {
		return entityData.get(AIM_PITCH);
	}

	/** Client side: ticks since it last fired (for the recoil). */
	public int sinceShot() {
		return tickCount - shotAt;
	}

	public void steer(ServerPlayer player, int forward, int turn, int fire) {
		if (getFirstPassenger() != player) {
			return;
		}
		this.forward = Mth.clamp(forward, -1, 1);
		this.turn = Mth.clamp(turn, -1, 1);
		this.fire = Mth.clamp(fire, 0, 1);
		inputTick = tickCount;
	}

	/** Use: climb aboard. */
	public InteractionResult use(Player player, InteractionHand hand) {
		if (player.isPassenger() || getPassengers().size() >= seats()) {
			return InteractionResult.PASS;
		}
		if (level().isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		return player.startRiding(this) ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel level)) {
			return;
		}
		Entity gunner = getFirstPassenger();
		boolean listening = gunner instanceof ServerPlayer && tickCount - inputTick <= INPUT_TIMEOUT;
		move(level, listening);
		resetFallDistance();
		Vec3 pivot = position().add(0, pivotHeight(), 0);
		float[] wanted = gunner instanceof ServerPlayer player ? wantedAim(level, player, pivot) : null;
		if (wanted != null) {
			float yaw = entityData.get(AIM_YAW);
			float pitch = entityData.get(AIM_PITCH);
			float newYaw = yaw + Mth.clamp(Mth.wrapDegrees(wanted[0] - yaw), -traverse(), traverse());
			float newPitch = pitch + Mth.clamp(wanted[1] - pitch, -traverse(), traverse());
			entityData.set(AIM_YAW, Mth.wrapDegrees(newYaw));
			entityData.set(AIM_PITCH, newPitch);
			boolean onTarget = Math.abs(Mth.wrapDegrees(wanted[0] - newYaw)) < 1.0F && Math.abs(wanted[1] - newPitch) < 1.0F;
			boolean pressed = listening && fire > 0;
			// A press orders one shot, fired as soon as the gun is on target and reloaded; an automatic gun fires
			// for as long as attack is held.
			if (pressed && !fireHeld) {
				ordered = true;
			}
			fireHeld = pressed;
			if ((ordered || automatic() && pressed) && onTarget && tickCount - lastShot >= cooldown()) {
				ordered = false;
				fire(level, (ServerPlayer) gunner, pivot);
			}
		} else {
			ordered = false;
		}
		if (damage > 0 && tickCount % 10 == 0) {
			damage = Math.max(0, damage - 1);
		}
	}

	/** The yaw and elevation the gun should turn to, or null to hold still. */
	private float @Nullable [] wantedAim(ServerLevel level, ServerPlayer gunner, Vec3 pivot) {
		BlockPos target = Spotting.target(level, gunner, pivot);
		if (target == null && highArc()) {
			// A mortar lobs at the block its gunner looks at.
			Vec3 from = gunner.getEyePosition();
			BlockHitResult hit = level.clip(new ClipContext(from, from.add(gunner.getViewVector(1.0F).scale(JugcraftArtillery.MARK_RANGE)),
					ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, gunner));
			if (hit.getType() == HitResult.Type.BLOCK) {
				target = hit.getBlockPos();
			}
		}
		if (target == null) {
			// Direct fire where the gunner looks.
			return new float[] {limitYaw(gunner.getYRot()), Mth.clamp(-gunner.getXRot(), minPitch(), maxPitch())};
		}
		Vec3 aimAt = Vec3.atCenterOf(target).add(0, 0.5, 0);
		Vec3 delta = aimAt.subtract(pivot);
		float yaw = limitYaw((float) Math.toDegrees(Math.atan2(-delta.x, delta.z)));
		double distance = Math.sqrt(delta.x * delta.x + delta.z * delta.z) - barrelLength();
		Float pitch = Ballistics.solve(shellSpeed(), shellGravity(), distance, delta.y, highArc(), minPitch(), maxPitch());
		if (pitch == null && !highArc()) {
			pitch = Ballistics.solve(shellSpeed(), shellGravity(), distance, delta.y, true, minPitch(), maxPitch());
		}
		if (pitch == null) {
			if (!warned) {
				gunner.sendOverlayMessage(Component.translatable("message.jugcraft.artillery.out_of_range"));
				warned = true;
			}
			return null;
		}
		warned = false;
		return new float[] {yaw, pitch};
	}

	/** The way the barrel points. */
	public Vec3 barrelDirection() {
		double yaw = Math.toRadians(entityData.get(AIM_YAW));
		double pitch = Math.toRadians(entityData.get(AIM_PITCH));
		return new Vec3(-Math.sin(yaw) * Math.cos(pitch), Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
	}

	private void fire(ServerLevel level, ServerPlayer gunner, Vec3 pivot) {
		if (!gunner.getAbilities().instabuild && !takeAmmo(gunner.getInventory())) {
			gunner.sendOverlayMessage(Component.translatable("message.jugcraft.artillery.no_shells", new ItemStack(ammo()).getHoverName()));
			return;
		}
		lastShot = tickCount;
		entityData.set(FIRED_AT, tickCount);
		Vec3 dir = barrelDirection();
		Vec3 muzzle = pivot.add(dir.scale(barrelLength()));
		ArtilleryShell shell = new ArtilleryShell(shellType(), level, gunner);
		shell.setPos(muzzle);
		shell.shoot(dir.x, dir.y, dir.z, (float) shellSpeed(), 0.0F);
		level.addFreshEntity(shell);
		level.sendParticles(ParticleTypes.LARGE_SMOKE, muzzle.x, muzzle.y, muzzle.z, automatic() ? 2 : 12, 0.3, 0.3, 0.3, 0.03);
		level.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, automatic() ? 1 : 6, 0.1, 0.1, 0.1, 0.02);
		level.playSound(null, muzzle.x, muzzle.y, muzzle.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS,
				automatic() ? 0.6F : 3.0F, automatic() ? 1.9F : 0.7F);
	}

	private boolean takeAmmo(Inventory inventory) {
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.is(ammo())) {
				stack.shrink(1);
				return true;
			}
		}
		return false;
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (FIRED_AT.equals(key) && level().isClientSide() && entityData.get(FIRED_AT) >= 0) {
			shotAt = tickCount;
		}
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return getPassengers().size() < seats();
	}

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
		int index = Math.max(0, getPassengers().indexOf(passenger));
		return seat(index).yRot(-seatYaw(index) * Mth.DEG_TO_RAD);
	}

	/** The yaw a seat turns with: the gun's for a gunner on a turning mount. */
	protected float seatYaw(int index) {
		return entityData.get(AIM_YAW);
	}

	@Override
	public boolean isPickable() {
		return !isRemoved();
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (isRemoved() || !(source.getEntity() instanceof Player player) || player.getRootVehicle() == this) {
			return false;
		}
		if (player.getAbilities().instabuild) {
			discard();
			return true;
		}
		damage += amount;
		if (damage >= health()) {
			spawnAtLocation(level, new ItemStack(dropItem()));
			discard();
		}
		return true;
	}

	/** Faces a newly placed gun the way its placer faced. */
	public void face(float yaw) {
		setYRot(yaw);
		entityData.set(AIM_YAW, yaw);
		entityData.set(AIM_PITCH, Mth.clamp(highArc() ? 60.0F : 10.0F, minPitch(), maxPitch()));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(AIM_YAW, 0.0F);
		builder.define(AIM_PITCH, 20.0F);
		builder.define(FIRED_AT, -100);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		entityData.set(AIM_YAW, input.getFloatOr("aim_yaw", getYRot()));
		entityData.set(AIM_PITCH, input.getFloatOr("aim_pitch", 20.0F));
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putFloat("aim_yaw", entityData.get(AIM_YAW));
		output.putFloat("aim_pitch", entityData.get(AIM_PITCH));
	}
}
