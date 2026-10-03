package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A werewolf (fall addition 23): a hulking wolf-man that comes out of the woods only on full-moon nights
 * ({@link Werewolves}) and turns back into nobody at all when the night ends, vanishing in a swirl of smoke.
 *
 * <p>Its hide shrugs off ordinary blows: everything but silver does {@value #HIDE_FACTOR} of its damage, and it heals
 * a point every {@value #REGEN_TICKS} ticks, unless silver has wounded it in the last {@value #SILVER_WOUND_TICKS}.
 * Silver (a weapon in {@code jugcraft:silver_weapons}, or a silver arrow) does {@value #SILVER_FACTOR} times its
 * damage. Slaying one with silver earns Silver Lining.
 *
 * <p>Wolfsbane wards it off ({@link Werewolves#warded}): someone holding a sprig, or near a planted or potted one, is
 * left alone. It drops its target, slinks away and won't take them again for a while. It hunts players and villagers,
 * leaps at them, and howls at the moon when it has nobody to hunt.
 */
public class Werewolf extends Monster {
	public static final float HIDE_FACTOR = 0.5F;
	public static final float SILVER_FACTOR = 2.5F;
	public static final int REGEN_TICKS = 40;
	public static final int SILVER_WOUND_TICKS = 100;
	public static final int WARD_CHECK_TICKS = 10;
	/** How long a warded target is left alone, in ticks. */
	public static final int SHUN_TICKS = 200;
	public static final int HOWL_MIN = 300;
	public static final int HOWL_MAX = 700;
	public static final double MAX_HEALTH = 40.0;
	public static final TagKey<Item> SILVER_WEAPONS = TagKey.create(Registries.ITEM, Jugcraft.id("silver_weapons"));
	/** Silver for a blade: soft (iron's drops, less durable than iron), quick, and enchanted easily. */
	public static final ToolMaterial SILVER = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 200, 6.0F, 1.5F, 20,
			TagKey.create(Registries.ITEM, Jugcraft.id("repairs_silver_gear")));

	private int lastSilverHit = -SILVER_WOUND_TICKS;
	private int nextHowl = HOWL_MIN;
	private @Nullable UUID shunned;
	private int shunnedUntil;

	public Werewolf(EntityType<? extends Werewolf> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.MOVEMENT_SPEED, 0.33)
				.add(Attributes.ATTACK_DAMAGE, 7.0).add(Attributes.ARMOR, 2.0).add(Attributes.FOLLOW_RANGE, 32.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.3).add(Attributes.STEP_HEIGHT, 1.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new FloatGoal(this));
		goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.45F));
		goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.25, false));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
	}

	/** Whether {@code source} is silver: a silver arrow, or a blow from someone holding a silver weapon. */
	public static boolean silver(DamageSource source) {
		Entity direct = source.getDirectEntity();
		if (direct instanceof AbstractArrow arrow) {
			return arrow.getPickupItemStackOrigin().is(JugcraftAgriculture.item(Werewolves.SILVER_ARROW));
		}
		return direct instanceof LivingEntity attacker && attacker.getMainHandItem().is(SILVER_WEAPONS);
	}

	/** The damage a werewolf takes from {@code amount} of {@code source}: more from silver, less from anything else. */
	public static float damageFrom(DamageSource source, float amount) {
		if (silver(source)) {
			return amount * SILVER_FACTOR;
		}
		return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? amount : amount * HIDE_FACTOR;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (silver(source)) {
			lastSilverHit = tickCount;
			level.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(1.0), getZ(), 6, 0.3, 0.4, 0.3, 0.05);
		}
		return super.hurtServer(level, source, damageFrom(source, amount));
	}

	/** Whether silver has wounded it lately, so it can't heal. */
	public boolean silverWounded() {
		return tickCount - lastSilverHit < SILVER_WOUND_TICKS;
	}

	@Override
	public void die(DamageSource source) {
		if (silver(source) && source.getEntity() instanceof ServerPlayer slayer) {
			TrickOrTreat.award(slayer, "silver_lining");
		}
		super.die(source);
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		if (shunned != null && target.getUUID().equals(shunned) && tickCount < shunnedUntil) {
			return false;
		}
		return super.canAttack(target);
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel level) || isRemoved()) {
			return;
		}
		if (tickCount % 20 == 0 && !Werewolves.fullMoon(level)) {
			turnBack(level);
			return;
		}
		if (tickCount % REGEN_TICKS == 0 && isAlive() && getHealth() < getMaxHealth() && !silverWounded()) {
			heal(1.0F);
		}
		if (tickCount % WARD_CHECK_TICKS == 0) {
			LivingEntity target = getTarget();
			if (target != null && Werewolves.warded(level, target)) {
				shun(target);
				if (target instanceof ServerPlayer player) {
					TrickOrTreat.award(player, "wolfsbane_ward");
				}
			}
		}
		if (getTarget() == null && --nextHowl <= 0) {
			howl(level);
			nextHowl = HOWL_MIN + random.nextInt(HOWL_MAX - HOWL_MIN + 1);
		}
	}

	/** Leaves {@code target} alone for {@value #SHUN_TICKS} ticks and slinks away from them. */
	public void shun(LivingEntity target) {
		shunned = target.getUUID();
		shunnedUntil = tickCount + SHUN_TICKS;
		setTarget(null);
		Vec3 away = position().subtract(target.position());
		away = away.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
		Vec3 to = position().add(away.scale(12.0));
		getNavigation().moveTo(to.x, to.y, to.z, 1.3);
		level().playSound(null, getX(), getY(), getZ(), sound("entity.wolf.whine", SoundEvents.ZOMBIE_AMBIENT), SoundSource.HOSTILE, 1.0F, 0.6F);
	}

	/** Whether it is leaving {@code target} alone. */
	public boolean shuns(LivingEntity target) {
		return shunned != null && target.getUUID().equals(shunned) && tickCount < shunnedUntil;
	}

	/** A long, low howl at the moon, heard far off. */
	public void howl(ServerLevel level) {
		level.playSound(null, getX(), getY(), getZ(), sound("entity.wolf.howl", SoundEvents.ZOMBIE_AMBIENT), SoundSource.HOSTILE, 4.0F, 0.55F);
	}

	/** The night is over: it is gone, in a swirl of smoke. */
	public void turnBack(ServerLevel level) {
		level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(0.5), getZ(), 20, 0.4, 0.8, 0.4, 0.02);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.8F, 0.5F);
		discard();
	}

	private static SoundEvent sound(String id, SoundEvent fallback) {
		return BuiltInRegistries.SOUND_EVENT.getOptional(Identifier.withDefaultNamespace(id)).orElse(fallback);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return sound("entity.wolf.growl", SoundEvents.ZOMBIE_AMBIENT);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return sound("entity.wolf.hurt", SoundEvents.ZOMBIE_HURT);
	}

	@Override
	protected SoundEvent getDeathSound() {
		return sound("entity.wolf.death", SoundEvents.ZOMBIE_DEATH);
	}

	@Override
	public float getVoicePitch() {
		return 0.6F + random.nextFloat() * 0.1F;
	}
}
