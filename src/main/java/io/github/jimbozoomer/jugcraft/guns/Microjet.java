package io.github.jimbozoomer.jugcraft.guns;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A Microjet in flight (the flying-microjets follow-up, docs/features/guns.md), fired by a jet gun
 * ({@link JugcraftGuns#JET}: the Maelstrom Gun, from the owner's Cyclone) at the gun's speed
 * ({@link JugcraftGuns#JET_SPEED}). It flies straight, untouched by gravity, and drives itself: whatever the air or water
 * takes off its speed in a tick, it makes up, so it holds the speed it was fired at. It stops at the first creature its
 * shooter may strike ({@link GunShots#target}, the same allies and mounts a bullet passes) or the first block, or once it
 * has flown the gun's range ({@link #arm}). The creature it hits takes the gun's damage as from a bullet, with the
 * gun's piercing, knockback, burning, bleeding and hit effect ({@link GunShots#land}), if other code lets its shooter
 * strike it ({@link GunShots#allowed}). It breaks no block and does not burst. A Microjet whose shooter is no longer a
 * player in its level harms nothing, and one not fired from a gun harms nothing.
 */
public class Microjet extends ThrowableItemProjectile {
	/** Ticks a Microjet flies at most, whatever its range (the owner's Cyclone's microjets live 50). */
	public static final int LIFETIME = 100;

	/** The gun that fired it ({@link JugcraftGuns#GUNS}): its hit's piercing, knockback and effects are that gun's. */
	private String gun = "";
	/** Its damage on what it hits: the gun's, with its attachments, as it was fired. */
	private float damage;
	/** Blocks it has left to fly before it stops. */
	private double left = Double.MAX_VALUE;
	/** The blocks a tick it holds to: the speed it was fired at, taken on its first tick (0 until then). */
	private double cruise;

	public Microjet(EntityType<? extends Microjet> type, Level level) {
		super(type, level);
	}

	public Microjet(Level level, LivingEntity owner, ItemStack stack) {
		super(JugcraftGuns.MICROJET, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return JugcraftGuns.ROUNDS.get("microjet");
	}

	/** Sets what it was fired from: the gun's name, the damage it deals and the blocks it may fly. */
	public void arm(String gun, float damage, double range) {
		this.gun = gun;
		this.damage = damage;
		this.left = range;
	}

	/** The name of the gun that fired it; empty for one not fired from a gun. */
	public String gun() {
		return gun;
	}

	/** The damage it deals to what it hits. */
	public float damage() {
		return damage;
	}

	/** The blocks it has left to fly before it stops. */
	public double left() {
		return left;
	}

	@Override
	protected double getDefaultGravity() {
		return 0.0;
	}

	@Override
	public void tick() {
		Vec3 from = position();
		if (cruise <= 0.0) {
			// Its first tick: it holds to the speed it was fired at (on the client, the speed it was sent with).
			cruise = getDeltaMovement().length();
		}
		super.tick();
		if (isRemoved()) {
			return;
		}
		Vec3 velocity = getDeltaMovement();
		if (cruise > 0.0 && velocity.lengthSqr() > 1.0E-8) {
			// Its motor makes up what the air (or water) took off its speed this tick.
			setDeltaMovement(velocity.scale(cruise / velocity.length()));
		}
		if (level().isClientSide()) {
			Vec3 back = position().subtract(getDeltaMovement().scale(0.5));
			level().addParticle(ParticleTypes.SMOKE, back.x, back.y, back.z, 0, 0.01, 0);
			level().addParticle(ParticleTypes.SMALL_FLAME, getX(), getY(), getZ(), 0, 0, 0);
			return;
		}
		left -= position().distanceTo(from);
		if (left <= 0.0 || tickCount >= LIFETIME) {
			// The end of its range: it burns out with a puff of smoke.
			((ServerLevel) level()).sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 3, 0.05, 0.05, 0.05, 0.01);
			discard();
		}
	}

	/** Only creatures, and of them only those its shooter may strike: it passes through the rest, as a bullet does. */
	@Override
	protected boolean canHitEntity(Entity entity) {
		if (!super.canHitEntity(entity) || !(entity instanceof LivingEntity foe)) {
			return false;
		}
		return !(getOwner() instanceof ServerPlayer player) || GunShots.target(player, foe);
	}

	@Override
	protected void onHitEntity(EntityHitResult hit) {
		super.onHitEntity(hit);
		if (!(level() instanceof ServerLevel level) || !(hit.getEntity() instanceof LivingEntity foe)) {
			return;
		}
		Vec3 at = hit.getLocation();
		level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 2, 0.05, 0.05, 0.05, 0.1);
		GunItem item = JugcraftGuns.GUNS.get(gun);
		if (item == null || damage <= 0.0F || !(getOwner() instanceof ServerPlayer player) || player.level() != level
				|| !GunShots.allowed(player, level, foe)) {
			return;
		}
		DamageSource source = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
				.getOrThrow(JugcraftGuns.BULLET), this, player);
		GunShots.land(level, foe, source, damage, player, JugcraftGuns.ignites(item), JugcraftGuns.knockback(item),
				JugcraftGuns.piercing(item), JugcraftGuns.bleed(item), JugcraftGuns.hitEffect(item), player.getRandom());
	}

	@Override
	protected void onHitBlock(BlockHitResult hit) {
		super.onHitBlock(hit);
		if (level() instanceof ServerLevel level) {
			Vec3 at = hit.getLocation();
			level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(hit.getBlockPos())), at.x, at.y, at.z,
					4, 0.05, 0.05, 0.05, 0.1);
			level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 1, 0.02, 0.02, 0.02, 0.01);
		}
	}

	/** It stops at whatever it hits. */
	@Override
	protected void onHit(HitResult hit) {
		super.onHit(hit);
		if (!level().isClientSide()) {
			discard();
		}
	}

	/** A Microjet saved in flight keeps its gun, damage, the range it has left and its speed. */
	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putString("gun", gun);
		output.putFloat("damage", damage);
		output.putDouble("left", left);
		output.putDouble("cruise", cruise);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		gun = input.getStringOr("gun", "");
		damage = input.getFloatOr("damage", 0.0F);
		left = input.getDoubleOr("left", Double.MAX_VALUE);
		cruise = input.getDoubleOr("cruise", 0.0);
	}
}
