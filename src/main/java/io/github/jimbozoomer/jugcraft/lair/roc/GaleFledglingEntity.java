package io.github.jimbozoomer.jugcraft.lair.roc;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import io.github.jimbozoomer.jugcraft.lair.LairBosses;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A Gale Fledgling (docs/features/storm-roc.md): a half-grown roc the size of a big dog, slate feathers fluffed with grey
 * down, eyes as blue as his. They leave the Storm Roc's nest when the Squall Rises and when he screams for them: for
 * {@value #RISE} ticks one climbs out of the nest, unhurt; then it darts after his foe at {@value #SPEED}, hangs
 * {@value #HOVER} over them, and stoops to peck ({@value #PECK_DAMAGE}) every {@value #PECK_EVERY} ticks once within
 * {@value #REACH}. Whoever hurts one takes part in the Roc's fight. It flies back to the nest when the Roc falls, resets or
 * is gone; it drops nothing, gives no experience, and is never saved.
 */
public class GaleFledglingEntity extends Monster implements GeoEntity {
	public static final float HEALTH = 20.0F;
	public static final double SPEED = 0.32;
	public static final double HOVER = 2.5;
	public static final int RISE = 20;
	public static final int PECK_EVERY = 30;
	public static final float PECK_DAMAGE = 3.0F;
	public static final double REACH = 1.6;
	/** How long a peck is shown (ticks). */
	private static final int PECK_SHOWN = 10;
	/** How far it may stray from the summit's middle before it turns back. */
	private static final double ROAM = 32.0;

	private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(GaleFledglingEntity.class, EntityDataSerializers.INT);
	private static final RawAnimation RISING = RawAnimation.begin().thenLoop("animation.gale_fledgling.rise");
	private static final RawAnimation FLYING = RawAnimation.begin().thenLoop("animation.gale_fledgling.fly");
	private static final RawAnimation PECKING = RawAnimation.begin().thenPlayAndHold("animation.gale_fledgling.peck");

	/** What it is doing, synced for its animations. */
	public enum State {
		RISING, FLYING, PECKING
	}

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private @Nullable UUID roc;
	private Vec3 home = Vec3.ZERO;
	private int peck = PECK_EVERY;
	private int shown;
	private int lonely;

	public GaleFledglingEntity(EntityType<? extends GaleFledglingEntity> type, Level level) {
		super(type, level);
		xpReward = 0;
		setNoGravity(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.MOVEMENT_SPEED, SPEED)
				.add(Attributes.FLYING_SPEED, SPEED).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.ATTACK_DAMAGE, PECK_DAMAGE);
	}

	/** A Gale Fledgling leaves the nest at {@code at}, in {@code roc}'s service. */
	public static GaleFledglingEntity rise(ServerLevel level, StormRocEntity roc, Vec3 at) {
		GaleFledglingEntity fledgling = new GaleFledglingEntity(JugcraftRoc.GALE_FLEDGLING, level);
		fledgling.roc = roc.getUUID();
		fledgling.home = roc.centre();
		fledgling.snapTo(at.x, at.y, at.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
		fledgling.setState(State.RISING);
		level.addFreshEntity(fledgling);
		level.sendParticles(ParticleTypes.CLOUD, at.x, at.y + 0.3, at.z, 8, 0.4, 0.2, 0.4, 0.02);
		return fledgling;
	}

	public State state() {
		int id = entityData.get(STATE);
		return id >= 0 && id < State.values().length ? State.values()[id] : State.FLYING;
	}

	private void setState(State state) {
		entityData.set(STATE, state.ordinal());
	}

	/** The Roc it serves, if he is still about. */
	public @Nullable StormRocEntity roc(ServerLevel level) {
		return roc != null && level.getEntity(roc) instanceof StormRocEntity found && found.isAlive() ? found : null;
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel level)) {
			return;
		}
		StormRocEntity master = roc(level);
		if (tickCount % 20 == 0) {
			boolean serving = master != null && master.phase() != StormRocEntity.Phase.WAITING;
			lonely = serving ? 0 : lonely + 20;
			if (lonely > 40) {
				leave(level);
				return;
			}
		}
		if (state() == State.RISING) {
			setDeltaMovement(0.0, 0.12, 0.0);
			if (tickCount >= RISE) {
				setState(State.FLYING);
			}
			return;
		}
		if (shown > 0 && --shown == 0) {
			setState(State.FLYING);
		}
		Player target = master == null ? null : target(level, master);
		if (peck > 0) {
			peck--;
		}
		fly(target);
		if (target != null && peck == 0 && distanceTo(target) <= REACH + target.getBbWidth() / 2.0 + getBbWidth() / 2.0) {
			peck(level, target);
		}
	}

	/** Its foe: the Roc's, while near, else the nearest player who may fight him. */
	private @Nullable Player target(ServerLevel level, StormRocEntity master) {
		Player foe = master.foe(level);
		if (foe != null && LairBosses.eligible(foe) && foe.position().distanceTo(home) <= ROAM) {
			return foe;
		}
		Player best = null;
		double distance = ROAM * ROAM;
		for (Player player : level.players()) {
			double d = player.position().distanceToSqr(home);
			if (LairBosses.eligible(player) && d <= distance) {
				distance = d;
				best = player;
			}
		}
		return best;
	}

	/** Flies at {@value #SPEED}: to hang {@value #HOVER} over {@code target} while its peck rests, then stooping at them. */
	private void fly(@Nullable Player target) {
		Vec3 goal;
		if (target == null || position().distanceTo(home) > ROAM) {
			goal = home.add(0.0, HOVER + 2.0, 0.0);
		} else if (peck > 0) {
			goal = target.position().add(0.0, HOVER, 0.0);
		} else {
			goal = target.position().add(0.0, target.getBbHeight() * 0.6, 0.0);
		}
		Vec3 to = goal.subtract(position());
		double length = to.length();
		setDeltaMovement(length < 0.05 ? Vec3.ZERO : to.scale(Math.min(SPEED, length) / length));
		if (target != null) {
			Vec3 at = target.position().subtract(position());
			setYRot((float) (Math.toDegrees(Math.atan2(at.z, at.x)) - 90.0));
			setYBodyRot(getYRot());
			setYHeadRot(getYRot());
		}
	}

	/** It pecks {@code target}: {@value #PECK_DAMAGE}. Returns whether it hurt them. */
	public boolean peck(ServerLevel level, Player target) {
		peck = PECK_EVERY;
		shown = PECK_SHOWN;
		setState(State.PECKING);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.PARROT_HURT, SoundSource.HOSTILE, 1.0F, 0.6F);
		return target.hurtServer(level, damageSources().mobAttack(this), StormRocEntity.damage(PECK_DAMAGE));
	}

	/** It flies back to the nest: gone in a puff of down. */
	public void leave(ServerLevel level) {
		level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.4, getZ(), 10, 0.3, 0.3, 0.3, 0.05);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.PARROT_FLY, SoundSource.HOSTILE, 1.0F, 0.6F);
		discard();
	}

	/** Unhurt while it leaves the nest; whoever hurts it takes part in the Roc's fight. */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (state() == State.RISING) {
			return false;
		}
		boolean hurt = super.hurtServer(level, source, amount);
		StormRocEntity master = roc(level);
		if (hurt && master != null && source.getEntity() instanceof ServerPlayer player) {
			master.took(player, amount);
		}
		return hurt;
	}

	@Override
	public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}

	@Override
	public boolean shouldDropExperience() {
		return false;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.PARROT_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.PARROT_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PARROT_DEATH;
	}

	@Override
	public float getVoicePitch() {
		return 0.5F;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(STATE, State.FLYING.ordinal());
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (roc != null) {
			output.store("roc", UUIDUtil.CODEC, roc);
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		roc = input.read("roc", UUIDUtil.CODEC).orElse(null);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<GaleFledglingEntity>("main", 3, test -> test.setAndContinue(switch (test.animatable().state()) {
			case RISING -> RISING;
			case PECKING -> PECKING;
			case FLYING -> FLYING;
		})));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}
}
