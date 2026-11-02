package io.github.jimbozoomer.jugcraft.walker;

import io.github.jimbozoomer.jugcraft.artillery.ArtilleryShell;
import io.github.jimbozoomer.jugcraft.artillery.JugcraftArtillery;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Armoured Walker (batch 58, docs/features/armoured-walker.md), after the owner's own Blender model: a squat walker
 * in riveted blue-grey plate with a hull cannon and a piston ram. It walks, turns, climbs, jumps and burns fuel exactly
 * as the {@link DieselWalker} does. Holding use fires the hull cannon at what the pilot looks at, a Heavy Shell every
 * {@value #CANNON_COOLDOWN} ticks from the pilot's own (none in creative): a damage-only blast, as every gun's. The
 * shell is lobbed so it comes down where the pilot's crosshair is (up to {@value #CANNON_REACH} blocks), allowing for
 * its fall ({@link #cannonVelocity}); the pilot sees its arc and landing before firing (docs/features/walker-cockpit.md).
 * Attack rams the piston arm into whatever is in front ({@value #RAM_DAMAGE} damage, thrown hard). The pilot stands in
 * the roof hatch, head and shoulders out, their eye about a third of a block over the roof. It is tougher than the
 * Diesel Walker ({@value #HEALTH} to knock down) but has no drill.
 */
public class ArmouredWalker extends DieselWalker {
	public static final int CANNON_COOLDOWN = 40;
	public static final double CANNON_SPEED = 2.4;
	public static final int RAM_DAMAGE = 16;
	public static final double RAM_KNOCKBACK = 1.6;
	public static final double RAM_REACH = 2.6;
	public static final int RAM_COOLDOWN = 24;
	public static final int HEALTH = 90;
	public static final float WIDTH = 2.6F;
	public static final float HEIGHT = 4.6F;
	/** The muzzle, in blocks before turning with the walker (MUZZLE in tools/armoured_walker.py, in pixels, over 16). */
	public static final Vec3 MUZZLE = new Vec3(0, 3.5, 1.8125);
	/**
	 * The pilot's place in the roof hatch (SEAT in tools/armoured_walker.py, over 16): their eye, 1.02 blocks over it,
	 * is about a third of a block over the roof, so the pilot looks out over the hull and not from inside it.
	 */
	private static final Vec3 SEAT = new Vec3(0, 3.855, -0.2);
	/** How far the pilot's look is followed to find what the cannon is laid on. */
	public static final int CANNON_REACH = 96;
	/** The steepest the cannon lobs down and up, in degrees. */
	public static final float CANNON_MIN_PITCH = -30.0F;
	public static final float CANNON_MAX_PITCH = 70.0F;
	public static final float CAMERA_DISTANCE = 7.5F;
	private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> CANNON_AT = net.minecraft.network.syncher.SynchedEntityData
			.defineId(ArmouredWalker.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
	private int lastShot = -CANNON_COOLDOWN;
	/** Client side: the tick the cannon last fired. */
	private int shotAt = -1000;
	private boolean warned;

	public ArmouredWalker(EntityType<? extends ArmouredWalker> type, Level level) {
		super(type, level);
	}

	/** Holding use: fire the cannon, when it is loaded, toward where the pilot looks. Never counts as drilling. */
	@Override
	protected boolean useHeld(ServerLevel level, ServerPlayer pilot) {
		if (tickCount - lastShot < CANNON_COOLDOWN) {
			return false;
		}
		if (!pilot.getAbilities().instabuild) {
			int slot = pilot.getInventory().findSlotMatchingItem(new ItemStack(JugcraftArtillery.HEAVY_SHELL_ITEM));
			if (slot < 0) {
				if (!warned) {
					pilot.sendOverlayMessage(Component.translatable("message.jugcraft.artillery.no_shells",
							new ItemStack(JugcraftArtillery.HEAVY_SHELL_ITEM).getHoverName()));
					warned = true;
				}
				return false;
			}
			pilot.getInventory().removeItem(slot, 1);
		}
		warned = false;
		lastShot = tickCount;
		entityData.set(CANNON_AT, tickCount);
		Vec3 muzzle = muzzle(1.0F);
		Vec3 aim = cannonVelocity(level, pilot, muzzle);
		ArtilleryShell shell = new ArtilleryShell(JugcraftArtillery.HEAVY_SHELL, level, pilot);
		shell.setPos(muzzle);
		shell.shoot(aim.x, aim.y, aim.z, (float) CANNON_SPEED, 0.0F); // No spread: it flies the arc the pilot is shown.
		level.addFreshEntity(shell);
		level.sendParticles(ParticleTypes.LARGE_SMOKE, muzzle.x, muzzle.y, muzzle.z, 10, 0.25, 0.25, 0.25, 0.03);
		level.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 5, 0.1, 0.1, 0.1, 0.02);
		level.playSound(null, muzzle.x, muzzle.y, muzzle.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.0F, 0.8F);
		return false;
	}

	/** The cannon's muzzle in the world, this frame. */
	public Vec3 muzzle(float partialTick) {
		return getPosition(partialTick).add(MUZZLE.yRot(-getYRot(partialTick) * Mth.DEG_TO_RAD));
	}

	/**
	 * The shell's velocity, in blocks a tick, that brings it down on what the pilot looks at (within
	 * {@value #CANNON_REACH} blocks): turned toward that point from the muzzle and raised for its fall
	 * ({@link io.github.jimbozoomer.jugcraft.artillery.Ballistics}, the low arc). With nothing in reach, or a point no arc
	 * reaches, it flies straight along the pilot's look, as it always did. The pilot's telegraph draws this very flight.
	 */
	public static Vec3 cannonVelocity(Level level, net.minecraft.world.entity.Entity pilot, Vec3 muzzle) {
		Vec3 look = Vec3.directionFromRotation(pilot.getXRot(), pilot.getYRot());
		Vec3 target = WalkerAim.lookedAt(level, pilot, CANNON_REACH);
		if (target != null) {
			Vec3 delta = target.subtract(muzzle);
			Float pitch = io.github.jimbozoomer.jugcraft.artillery.Ballistics.solve(CANNON_SPEED, JugcraftArtillery.HEAVY_GRAVITY,
					delta.horizontalDistance(), delta.y, false, CANNON_MIN_PITCH, CANNON_MAX_PITCH);
			if (pitch != null && delta.horizontalDistance() > 0.5) {
				float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
				return Vec3.directionFromRotation(-pitch, yaw).scale(CANNON_SPEED);
			}
		}
		return look.scale(CANNON_SPEED);
	}

	/** Client side: ticks since the cannon last fired, for the pilot's reload gauge. */
	public int sinceShot() {
		return tickCount - shotAt;
	}

	@Override
	public void onSyncedDataUpdated(net.minecraft.network.syncher.EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		// tickCount 0 is the data sent as the walker comes into view: an old shot, not a new one.
		if (CANNON_AT.equals(key) && level().isClientSide() && tickCount > 0) {
			shotAt = tickCount;
		}
	}

	/** The ram's box: {@value #RAM_REACH} blocks ahead, as the telegraph draws it. */
	@Override
	public AABB attackArea(Vec3 heading) {
		Vec3 centre = position().add(heading.scale(RAM_REACH)).add(0, 1.6, 0);
		return new AABB(centre, centre).inflate(1.5);
	}

	@Override
	public float cameraDistance() {
		return CAMERA_DISTANCE;
	}

	/** Attack: the piston ram, into everything in a box just in front. */
	@Override
	protected void attack(ServerLevel level, ServerPlayer pilot, Vec3 heading) {
		AABB reach = attackArea(heading);
		Vec3 centre = reach.getCenter();
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, reach, e -> e != pilot && e.isAlive())) {
			if (target.hurtServer(level, level.damageSources().playerAttack(pilot), RAM_DAMAGE)) {
				target.setDeltaMovement(target.getDeltaMovement().add(heading.x * RAM_KNOCKBACK, 0.5, heading.z * RAM_KNOCKBACK));
				if (target instanceof ServerPlayer hit) {
					hit.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(hit));
				}
			}
		}
		level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.PISTON_EXTEND, SoundSource.PLAYERS, 1.2F, 0.6F);
		level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5F, 0.7F);
	}

	@Override
	public int attackCooldown() {
		return RAM_COOLDOWN;
	}

	@Override
	protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(CANNON_AT, -1000);
	}

	@Override
	protected int health() {
		return HEALTH;
	}

	@Override
	protected ItemStack dropStack() {
		return new ItemStack(JugcraftWalkers.ARMOURED_WALKER_ITEM);
	}

	@Override
	protected Vec3 seat() {
		return SEAT;
	}
}
