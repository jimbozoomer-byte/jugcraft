package io.github.jimbozoomer.jugcraft.lair.horseman;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import io.github.jimbozoomer.jugcraft.lair.LairBosses;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A phantom rider (the Headless Horseman's Phantom Riders, docs/features/hessian-horseman.md): the ghost of one of his
 * troop, horse and rider in blue fire, galloping down the line the blue fire ran along before it: across the hollow over
 * the ticks it is given, {@value HessianHorsemanEntity#RIDER_DAMAGE} to each player it rides through (within
 * {@value HessianHorsemanEntity#RIDER_HALF_WIDTH} blocks of its line), once each. It cannot be stopped or harmed, is never
 * saved, and fades at its line's end or without its Horseman.
 */
public class PhantomRiderEntity extends Entity implements GeoEntity {
	private static final RawAnimation GALLOP = RawAnimation.begin().thenLoop("animation.phantom_rider.gallop");

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private @Nullable UUID horseman;
	private Vec3 from = Vec3.ZERO;
	private Vec3 to = Vec3.ZERO;
	private int ticks = 1;
	private int ridden;
	private final Set<UUID> struck = new HashSet<>();

	public PhantomRiderEntity(EntityType<? extends PhantomRiderEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
		setNoGravity(true);
	}

	/** {@code horseman} sends a phantom rider galloping from {@code from} to {@code to} over {@code ticks}. */
	public static PhantomRiderEntity gallop(ServerLevel level, HessianHorsemanEntity horseman, Vec3 from, Vec3 to, int ticks) {
		PhantomRiderEntity rider = new PhantomRiderEntity(JugcraftHorseman.PHANTOM_RIDER, level);
		rider.horseman = horseman.getUUID();
		rider.from = from;
		rider.to = to;
		rider.ticks = Math.max(1, ticks);
		Vec3 way = to.subtract(from);
		float yaw = (float) (Math.toDegrees(Math.atan2(way.z, way.x)) - 90.0);
		rider.snapTo(from.x, from.y, from.z, yaw, 0.0F);
		level.addFreshEntity(rider);
		level.playSound(null, from.x, from.y, from.z, SoundEvents.SKELETON_HORSE_AMBIENT, SoundSource.HOSTILE, 2.0F, 1.4F);
		return rider;
	}

	/** Where it is {@code tick} ticks into its gallop. */
	public Vec3 along(int tick) {
		return from.add(to.subtract(from).scale(Math.min(1.0, tick / (double) ticks)));
	}

	public Vec3 from() {
		return from;
	}

	public Vec3 to() {
		return to;
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel level)) {
			return;
		}
		HessianHorsemanEntity owner = horseman != null && level.getEntity(horseman) instanceof HessianHorsemanEntity found && found.isAlive()
				? found : null;
		if (owner == null) {
			fade(level);
			return;
		}
		ridden++;
		Vec3 next = along(ridden);
		setPos(next.x, next.y, next.z);
		strike(level, owner, ridden);
		level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.2, getZ(), 4, 0.4, 0.6, 0.4, 0.02);
		if (ridden % 4 == 0) {
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_GALLOP, SoundSource.HOSTILE, 1.5F, 1.4F);
		}
		if (ridden >= ticks) {
			fade(level);
		}
	}

	/** It rides through whoever it has reached by {@code tick}: each takes its blow, once. Returns how many it struck now. */
	public int strike(ServerLevel level, HessianHorsemanEntity owner, int tick) {
		Vec3 way = to.subtract(from);
		double length = way.length();
		if (length < 1.0E-3) {
			return 0;
		}
		Vec3 unit = way.scale(1.0 / length);
		double reached = length * Math.min(1.0, tick / (double) ticks);
		int hit = 0;
		DamageSource source = damageSources().mobAttack(owner);
		for (ServerPlayer player : level.players()) {
			if (struck.contains(player.getUUID()) || !LairBosses.eligible(player)) {
				continue;
			}
			Vec3 rel = new Vec3(player.getX() - from.x, 0.0, player.getZ() - from.z);
			double ahead = rel.x * unit.x + rel.z * unit.z;
			double across = Math.abs(rel.x * unit.z - rel.z * unit.x);
			if (ahead < -0.5 || ahead > reached + 0.5 || across > HessianHorsemanEntity.RIDER_HALF_WIDTH + player.getBbWidth() / 2.0
					|| Math.abs(player.getY() - from.y) > 3.0) {
				continue;
			}
			struck.add(player.getUUID());
			if (player.hurtServer(level, source, HessianHorsemanEntity.damage(HessianHorsemanEntity.RIDER_DAMAGE))) {
				hit++;
			}
		}
		return hit;
	}

	/** It fades into blue smoke. */
	public void fade(ServerLevel level) {
		level.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.2, getZ(), 12, 0.5, 0.6, 0.5, 0.02);
		discard();
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<PhantomRiderEntity>("main", 0, test -> test.setAndContinue(GALLOP)));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}
}
