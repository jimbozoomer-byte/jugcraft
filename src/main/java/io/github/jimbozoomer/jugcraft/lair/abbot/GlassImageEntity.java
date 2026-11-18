package io.github.jimbozoomer.jugcraft.lair.abbot;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.util.GeckoLibUtil;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A Glass Image of the Abbot (docs/features/glass-abbot.md): his likeness in glass, his model and his sheet, his name;
 * it plays what he plays, steps as he steps and faces his foe as he does, so his wind-ups are read on every one of
 * them alike. It never hurts anyone, and casts none of the rose window's shadow on the floor (only he does). One blow
 * of anything shatters it (whoever struck it has taken part in his fight); it fades by itself after
 * {@value GlassAbbotEntity#IMAGE_TICKS} ticks, and with him when he falls, kneels again or leaves. It is never saved.
 */
public class GlassImageEntity extends Monster implements GeoEntity {
	public static final float HEALTH = 1.0F;
	private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(GlassImageEntity.class, EntityDataSerializers.INT);

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private @Nullable UUID abbot;

	public GlassImageEntity(EntityType<? extends GlassImageEntity> type, Level level) {
		super(type, level);
		xpReward = 0;
		setPersistenceRequired();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
				.add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.STEP_HEIGHT, GlassAbbotEntity.STEP);
	}

	/** An image stands up out of the light at {@code at}, turned as {@code abbot} is. */
	public static GlassImageEntity spawn(ServerLevel level, GlassAbbotEntity abbot, Vec3 at) {
		GlassImageEntity image = new GlassImageEntity(JugcraftAbbot.GLASS_IMAGE, level);
		image.abbot = abbot.getUUID();
		image.snapTo(at.x, at.y, at.z, abbot.getYRot(), 0.0F);
		image.mirror(abbot);
		level.addFreshEntity(image);
		level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 1.4, at.z, 16, 0.4, 0.8, 0.4, 0.02);
		return image;
	}

	public GlassAbbotEntity.Action action() {
		int id = entityData.get(ACTION);
		return id >= 0 && id < GlassAbbotEntity.Action.values().length ? GlassAbbotEntity.Action.values()[id] : GlassAbbotEntity.Action.IDLE;
	}

	/** The Abbot it stands for, while he is here, alive and still calls it his. */
	public @Nullable GlassAbbotEntity abbot() {
		return abbot != null && level() instanceof ServerLevel level && level.getEntity(abbot) instanceof GlassAbbotEntity found
				&& found.isAlive() && found.calls(getUUID()) ? found : null;
	}

	/** It looks as he does: his body's action, and turned as he is. */
	private void mirror(GlassAbbotEntity found) {
		entityData.set(ACTION, found.action().ordinal());
		face(found.getYRot());
	}

	private void face(float yaw) {
		setYRot(yaw);
		setYBodyRot(yaw);
		setYHeadRot(yaw);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		GlassAbbotEntity found = abbot();
		if (found == null || tickCount >= GlassAbbotEntity.IMAGE_TICKS) {
			fade(level);
			return;
		}
		resetFallDistance();
		mirror(found);
		// It steps as he steps, so long as that keeps it in the nave, and faces his foe from where it stands.
		Vec3 step = found.getDeltaMovement();
		if (!found.inArena(position().add(step.x, 0.0, step.z))) {
			step = Vec3.ZERO;
		}
		setDeltaMovement(step.x, Math.min(getDeltaMovement().y, 0.0), step.z);
		Player foe = found.foe(level);
		if (foe != null) {
			Vec3 way = foe.position().subtract(position());
			if (way.x * way.x + way.z * way.z > 1.0E-4) {
				face((float) (Math.toDegrees(Math.atan2(way.z, way.x)) - 90.0));
			}
		}
	}

	/** It fades into light, unbroken. */
	public void fade(ServerLevel level) {
		level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.4, getZ(), 12, 0.4, 0.8, 0.4, 0.02);
		discard();
	}

	/** It shatters into glass. */
	public void shatter(ServerLevel level) {
		level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.4, getZ(), 24, 0.5, 1.0, 0.5, 0.08);
		level.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.4, getZ(), 12, 0.5, 1.0, 0.5, 0.1);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.5F, 1.2F);
		discard();
	}

	/** Anything striking it shatters it; a player striking it has taken part in his fight. */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return super.hurtServer(level, source, amount);
		}
		Entity striker = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
		if (striker == null) {
			return false;
		}
		GlassAbbotEntity found = abbot();
		if (found != null && striker instanceof ServerPlayer player) {
			found.took(player, Math.max(amount, 1.0F));
		}
		shatter(level);
		return true;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	protected boolean canRide(Entity vehicle) {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}

	@Override
	public boolean shouldDropExperience() {
		return false;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return SoundEvents.AMETHYST_BLOCK_RESONATE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.GLASS_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.GLASS_BREAK;
	}

	@Override
	public float getVoicePitch() {
		return 0.6F;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ACTION, GlassAbbotEntity.Action.IDLE.ordinal());
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		// His clips, played as he plays them.
		controllers.add(new AnimationController<GlassImageEntity>("body", 4, test -> {
			GlassAbbotEntity.Action action = test.animatable().action();
			return test.setAndContinue(action == GlassAbbotEntity.Action.IDLE && test.isMoving() ? GlassAbbotEntity.WALK
					: GlassAbbotEntity.BODY[action.ordinal()]);
		}));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}
}
