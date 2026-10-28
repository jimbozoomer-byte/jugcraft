package io.github.jimbozoomer.jugcraft.lair.tyrant;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.rime.Cooling;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A thrown Quench Flask (docs/features/cinder-tyrant-concordance.md). Where it breaks, everything within
 * {@value #RADIUS} blocks is splashed:
 * <ul>
 * <li>a creature born of fire ({@code #jugcraft:quenchable}: a blaze, a magma cube, the Cinder Tyrant's Cinderlings)
 * takes {@value #FIRE_BORN_DAMAGE} from its thrower, and a Cinderling then gutters out as it would in a flooded trough
 * (its thrower has taken part in the Tyrant's fight);</li>
 * <li>every other creature is put out and Cooled ({@link Cooling}) for {@value #COOLED_TICKS} ticks: hot ground and
 * burning pass it by;</li>
 * <li>fire on the ground goes out, and so do the Cinder Tyrant's fire patches.</li>
 * </ul>
 * It never quenches the Tyrant himself: only his sluices' water does. Decided on the server.
 */
public class ThrownQuenchFlask extends ThrowableItemProjectile {
	public static final double RADIUS = 2.5;
	public static final int COOLED_TICKS = 200;
	public static final float FIRE_BORN_DAMAGE = 6.0F;
	public static final TagKey<EntityType<?>> QUENCHABLE = TagKey.create(Registries.ENTITY_TYPE, Jugcraft.id("quenchable"));

	/** What one splash did: creatures Cooled, fire-born creatures struck (Cinderlings guttered among them), fire put out. */
	public record Splash(int cooled, int struck, int doused) {
	}

	public ThrownQuenchFlask(EntityType<? extends ThrownQuenchFlask> type, Level level) {
		super(type, level);
	}

	public ThrownQuenchFlask(Level level, LivingEntity owner, ItemStack stack) {
		super(JugcraftTyrant.THROWN_QUENCH_FLASK, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return JugcraftTyrant.QUENCH_FLASK;
	}

	@Override
	protected double getDefaultGravity() {
		return 0.05;
	}

	@Override
	protected void onHit(HitResult hit) {
		super.onHit(hit);
		if (level() instanceof ServerLevel server && !isRemoved()) {
			splash(server, hit.getLocation(), this, getOwner());
			discard();
		}
	}

	/** Splashes everything within {@value #RADIUS} blocks of {@code at} (a flask from {@code owner}, if any). */
	public static Splash splash(ServerLevel level, Vec3 at, @Nullable Entity flask, @Nullable Entity owner) {
		int cooled = 0;
		int struck = 0;
		int doused = 0;
		AABB box = new AABB(at, at).inflate(RADIUS);
		for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box, other -> other.isAlive()
				&& other.distanceToSqr(at) <= RADIUS * RADIUS)) {
			if (living.is(QUENCHABLE)) {
				// The blow first, so a Cinderling's thrower has taken part in its Tyrant's fight; then it gutters out.
				living.hurtServer(level, level.damageSources().thrown(flask == null ? living : flask, owner), FIRE_BORN_DAMAGE);
				if (living instanceof CinderlingEntity cinderling && !cinderling.isRemoved()) {
					cinderling.gutter(level);
				}
				struck++;
				continue;
			}
			living.clearFire();
			if (Cooling.COOLED != null && living.addEffect(new MobEffectInstance(Cooling.COOLED, COOLED_TICKS), owner)) {
				cooled++;
			}
		}
		int reach = (int) Math.ceil(RADIUS);
		BlockPos centre = BlockPos.containing(at);
		for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-reach, -reach, -reach), centre.offset(reach, reach, reach))) {
			if (level.getBlockState(pos).is(BlockTags.FIRE) && Vec3.atCenterOf(pos).distanceToSqr(at) <= (RADIUS + 0.5) * (RADIUS + 0.5)) {
				level.removeBlock(pos, false);
				doused++;
			}
		}
		for (CinderTyrantEntity tyrant : level.getEntitiesOfClass(CinderTyrantEntity.class, box.inflate(48.0), CinderTyrantEntity::isAlive)) {
			doused += tyrant.douse(level, at, RADIUS);
		}
		level.sendParticles(ParticleTypes.SPLASH, at.x, at.y + 0.2, at.z, 40, RADIUS * 0.4, 0.2, RADIUS * 0.4, 0.1);
		level.sendParticles(ParticleTypes.SNOWFLAKE, at.x, at.y + 0.4, at.z, 20, RADIUS * 0.4, 0.3, RADIUS * 0.4, 0.02);
		level.sendParticles(ParticleTypes.CLOUD, at.x, at.y + 0.3, at.z, 10, RADIUS * 0.3, 0.2, RADIUS * 0.3, 0.02);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
		if (struck > 0 || doused > 0) {
			level.playSound(null, at.x, at.y, at.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 1.0F, 1.0F);
		}
		return new Splash(cooled, struck, doused);
	}
}
