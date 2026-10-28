package io.github.jimbozoomer.jugcraft.lair.horseman;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import io.github.jimbozoomer.jugcraft.lair.LairBosses;
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
 * What the Headless Horseman throws from his saddle (docs/features/hessian-horseman.md): a flaming pumpkin (his Pumpkin
 * Toss, {@code jugcraft:hessian_pumpkin}) or the blazing jack-o'-lantern he carries for a head (his Head Hurl,
 * {@code jugcraft:hessian_head}). It arcs from his saddle onto the mark he showed, over the ticks it is given, and bursts
 * there: the pumpkin {@value HessianHorsemanEntity#TOSS_DAMAGE} to each player within
 * {@value HessianHorsemanEntity#TOSS_SPLASH} blocks, set alight {@value HessianHorsemanEntity#TOSS_FIRE_SECONDS} seconds;
 * the head {@value HessianHorsemanEntity#HURL_DAMAGE} within {@value HessianHorsemanEntity#HURL_SPLASH}, set alight
 * {@value HessianHorsemanEntity#HURL_FIRE_SECONDS}. It never breaks or lights a block, cannot be stopped or harmed, and is
 * never saved; without its Horseman it burns out.
 */
public class HessianPumpkinEntity extends Entity implements GeoEntity {
	private static final RawAnimation TUMBLE_PUMPKIN = RawAnimation.begin().thenLoop("animation.hessian_pumpkin.tumble");
	private static final RawAnimation TUMBLE_HEAD = RawAnimation.begin().thenLoop("animation.hessian_head.tumble");
	/** How high its arc rises over the straight line from his saddle to the mark, at the middle. */
	private static final double ARC = 4.0;

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private @Nullable UUID horseman;
	private Vec3 from = Vec3.ZERO;
	private Vec3 to = Vec3.ZERO;
	private int flight = 1;
	private int flown;

	public HessianPumpkinEntity(EntityType<? extends HessianPumpkinEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
		setNoGravity(true);
	}

	/** {@code horseman} throws {@code kind} from {@code from} onto {@code to}, landing {@code ticks} ticks later. */
	public static HessianPumpkinEntity toss(ServerLevel level, EntityType<HessianPumpkinEntity> kind, HessianHorsemanEntity horseman, Vec3 from,
			Vec3 to, int ticks) {
		HessianPumpkinEntity pumpkin = new HessianPumpkinEntity(kind, level);
		pumpkin.horseman = horseman.getUUID();
		pumpkin.from = from;
		pumpkin.to = to;
		pumpkin.flight = Math.max(1, ticks);
		pumpkin.snapTo(from.x, from.y, from.z, horseman.getYRot(), 0.0F);
		level.addFreshEntity(pumpkin);
		level.playSound(null, from.x, from.y, from.z, HessianHorsemanEntity.sound("entity.blaze.shoot", SoundEvents.FIRECHARGE_USE),
				SoundSource.HOSTILE, 1.5F, pumpkin.head() ? 0.5F : 0.7F);
		return pumpkin;
	}

	/** Whether this is his lantern head (the Head Hurl), not a pumpkin from his saddle. */
	public boolean head() {
		return getType() == JugcraftHorseman.HESSIAN_HEAD;
	}

	public float damage() {
		return head() ? HessianHorsemanEntity.HURL_DAMAGE : HessianHorsemanEntity.TOSS_DAMAGE;
	}

	public double splash() {
		return head() ? HessianHorsemanEntity.HURL_SPLASH : HessianHorsemanEntity.TOSS_SPLASH;
	}

	public int fireSeconds() {
		return head() ? HessianHorsemanEntity.HURL_FIRE_SECONDS : HessianHorsemanEntity.TOSS_FIRE_SECONDS;
	}

	/** Where it is {@code tick} ticks into its flight. */
	public Vec3 along(int tick) {
		double share = Math.min(1.0, tick / (double) flight);
		Vec3 at = from.add(to.subtract(from).scale(share));
		return at.add(0.0, ARC * 4.0 * share * (1.0 - share), 0.0);
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
			level.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.3, getZ(), 6, 0.2, 0.2, 0.2, 0.02);
			discard();
			return;
		}
		flown++;
		Vec3 next = along(flown);
		setPos(next.x, next.y, next.z);
		level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.4, getZ(), head() ? 4 : 2, 0.15, 0.15, 0.15, 0.01);
		if (flown >= flight) {
			burst(level, owner);
			discard();
		}
	}

	/** It bursts on the mark: each player near is hurt and set alight; returns how many. */
	public int burst(ServerLevel level, HessianHorsemanEntity owner) {
		int hit = 0;
		DamageSource source = damageSources().mobProjectile(this, owner);
		for (ServerPlayer player : level.players()) {
			double flat = Math.hypot(player.getX() - to.x, player.getZ() - to.z);
			if (!LairBosses.eligible(player) || flat > splash() + player.getBbWidth() / 2.0 || Math.abs(player.getY() - to.y) > 3.0) {
				continue;
			}
			if (player.hurtServer(level, source, HessianHorsemanEntity.damage(damage()))) {
				hit++;
				player.igniteForSeconds(fireSeconds());
			}
		}
		level.sendParticles(ParticleTypes.FLAME, to.x, to.y + 0.3, to.z, head() ? 60 : 40, 1.0, 0.4, 1.0, 0.05);
		level.sendParticles(ParticleTypes.LARGE_SMOKE, to.x, to.y + 0.3, to.z, 10, 0.6, 0.3, 0.6, 0.02);
		level.playSound(null, to.x, to.y, to.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, head() ? 1.2F : 0.8F, 1.3F);
		return hit;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
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
		controllers.add(new AnimationController<HessianPumpkinEntity>("main", 0,
				test -> test.setAndContinue(test.animatable().head() ? TUMBLE_HEAD : TUMBLE_PUMPKIN)));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}
}
