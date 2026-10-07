package io.github.jimbozoomer.jugcraft.client.guns;

import io.github.jimbozoomer.jugcraft.guns.GunItem;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * What a shot shows besides the gun's own animation (slice 6), on this client only:
 * <ul>
 * <li>the muzzle flash, drawn by {@link GunFlashLayer} for two ticks after each shot. Shots are known here
 * as they happen: the player's own as they fire (GunsClient), others' from the server's GunActionPayload;</li>
 * <li>the spent casing the owner's animations eject at their "eject_casing" cue ({@link #eject}): the round's
 * {@link JugcraftGuns#CASINGS} particle, thrown out to the gun's right. A paper cartridge leaves no case, so a
 * muzzle-loader's cue puffs a little smoke from its lock instead;</li>
 * <li>black powder's white cloud in front of the muzzle with each shot;</li>
 * <li>a bayonet's thrust (slice 7): the gun driven forward and back, in the stabber's hands and, seen from outside,
 * with their arms ({@link #thrust}).</li>
 * </ul>
 */
public final class GunEffects {
	/**
	 * Ticks after a shot until its flash is gone. The level's clock moves on in the tick the shot is fired, so the first
	 * frame drawn after it is a tick old: the flash shows full for its first tick and fades over its second.
	 */
	static final float FLASH_TICKS = 3.0F;
	/** A shot older than this is forgotten. */
	private static final long FORGET_TICKS = 20;
	/** Each shooter's last shot: entity id to game time. Pruned once it holds more than a few. */
	private static final Map<Integer, Long> SHOTS = new HashMap<>();
	/** Ticks a bayonet thrust takes, out and back (slice 7). */
	static final float THRUST_TICKS = 6.0F;
	/** Each stabber's last stab, the same way. */
	private static final Map<Integer, Long> STABS = new HashMap<>();
	/** Flashes drawn and casings (or puffs) thrown, counted for the client game tests. */
	private static long flashes;
	private static long ejected;

	private GunEffects() {
	}

	/** The gun this entity holds fired, now. */
	public static void shot(LivingEntity shooter) {
		long now = shooter.level().getGameTime();
		if (SHOTS.size() > 32) {
			SHOTS.values().removeIf(time -> now - time > FORGET_TICKS || time > now);
		}
		SHOTS.put(shooter.getId(), now);
		if (shooter.getMainHandItem().getItem() instanceof GunItem gun && gun.spec().ammo().equals("paper_cartridge")
				&& shooter.level() instanceof ClientLevel level) {
			Vec3 eye = shooter.getEyePosition();
			Vec3 look = shooter.getLookAngle();
			RandomSource random = shooter.getRandom();
			for (int i = 0; i < 4; i++) {
				double ahead = 0.9 + i * 0.25;
				level.addParticle(ParticleTypes.CLOUD, eye.x + look.x * ahead, eye.y + look.y * ahead - 0.15, eye.z + look.z * ahead,
						look.x * 0.04 + random.nextGaussian() * 0.01, 0.01 + random.nextGaussian() * 0.01,
						look.z * 0.04 + random.nextGaussian() * 0.01);
			}
		}
	}

	/** This entity stabbed with its gun's bayonet, now (slice 7). */
	public static void stabbed(LivingEntity stabber) {
		long now = stabber.level().getGameTime();
		if (STABS.size() > 32) {
			STABS.values().removeIf(time -> now - time > FORGET_TICKS || time > now);
		}
		STABS.put(stabber.getId(), now);
	}

	/** How far into its thrust this entity's bayonet is: 0 at rest, 1 at full reach, out and back over THRUST_TICKS. */
	public static float thrust(int entityId, long gameTime, float partialTick) {
		Long stab = STABS.get(entityId);
		if (stab == null || stab > gameTime) {
			return 0.0F;
		}
		float age = gameTime - stab + partialTick;
		return age < THRUST_TICKS ? (float) Math.sin(Math.PI * age / THRUST_TICKS) : 0.0F;
	}

	/** Ticks since this entity's last shot, if its flash still shows; otherwise -1. */
	static float flashAge(int entityId, long gameTime, float partialTick) {
		Long shot = SHOTS.get(entityId);
		if (shot == null || shot > gameTime) {
			return -1.0F;
		}
		float age = gameTime - shot + partialTick;
		return age < FLASH_TICKS ? age : -1.0F;
	}

	/** The game time of this entity's last shot (picks the flash's frame and turn), or 0. */
	static long lastShot(int entityId) {
		return SHOTS.getOrDefault(entityId, 0L);
	}

	static void flashDrawn() {
		flashes++;
	}

	/** Flashes drawn so far (for the client game tests). */
	public static long flashes() {
		return flashes;
	}

	/** Casings and lock puffs thrown so far (for the client game tests). */
	public static long ejected() {
		return ejected;
	}

	/**
	 * An animation's "eject_casing" cue on this gun, held by this entity (the player's own when unknown): the round's
	 * spent case flies from the ejection port out to the gun's right and a little up, tumbling.
	 */
	static void eject(GunItem gun, int ownerId) {
		Minecraft client = Minecraft.getInstance();
		ClientLevel level = client.level;
		if (level == null) {
			return;
		}
		Entity at = ownerId >= 0 ? level.getEntity(ownerId) : client.player;
		if (!(at instanceof LivingEntity holder)) {
			return;
		}
		Vec3 eye = holder.getEyePosition();
		Vec3 look = holder.getLookAngle();
		Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0));
		right = right.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : right.normalize();
		if (holder.getMainArm() == HumanoidArm.LEFT) {
			right = right.scale(-1.0);
		}
		Vec3 port = eye.add(look.scale(0.5)).add(right.scale(0.3)).add(0.0, -0.3, 0.0);
		RandomSource random = holder.getRandom();
		ejected++;
		SimpleParticleType casing = JugcraftGuns.CASINGS.get(gun.spec().ammo());
		if (casing == null) {
			level.addParticle(ParticleTypes.SMOKE, port.x, port.y + 0.1, port.z, 0.0, 0.03, 0.0);
			level.addParticle(ParticleTypes.SMOKE, port.x, port.y + 0.15, port.z, 0.0, 0.04, 0.0);
			return;
		}
		Vec3 moving = holder.getDeltaMovement();
		Vec3 throwTo = right.scale(0.12 + random.nextFloat() * 0.04).add(look.scale(-0.02))
				.add(moving.x, Math.max(0.0, moving.y), moving.z);
		level.addParticle(casing, port.x, port.y, port.z, throwTo.x + random.nextGaussian() * 0.01,
				0.14 + random.nextFloat() * 0.04, throwTo.z + random.nextGaussian() * 0.01);
	}
}
