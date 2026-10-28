package io.github.jimbozoomer.jugcraft.weapons;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import org.jspecify.annotations.Nullable;

/**
 * The Dread boon (the Hollowflail and the Hessian Sabre, the Headless Horseman's trophies, docs/features/hessian-horseman.md):
 * a struck monster (a creature that hunts players, never a boss: {@code #jugcraft:frightless} names the bosses) is filled
 * with dread ({@code jugcraft:dread}) for {@value #TICKS} ticks, and every blow on it from anything does {@value #SHARE}
 * more (counted after its armor; mixin/DreadDamageMixin asks {@link #taken}). Each foe can be filled with dread once every
 * {@value #COOLDOWN} ticks; a hit meanwhile does nothing more. At most {@value #MAX} foes are remembered as lately filled,
 * and only while the server runs; tools/arms_variants.py holds the numbers and tools/check_mod_data.py checks them.
 */
public final class DreadBoon {
	public static final int TICKS = 80;
	public static final int COOLDOWN = 160;
	public static final float SHARE = 0.2F;
	public static final int MAX = 256;
	/** Dread: no more than what {@link #taken} adds. */
	public static @Nullable Holder<MobEffect> DREAD;
	/** When each foe was last filled with dread (forgotten once its cooldown is over). */
	private static final Map<UUID, Long> LAST = new HashMap<>();

	private DreadBoon() {
	}

	/** Dread's effect: nothing of its own; the blows on its holder do more (DreadDamageMixin). */
	private static final class DreadEffect extends MobEffect {
		DreadEffect() {
			super(MobEffectCategory.HARMFUL, 0x3B1F4A);
		}
	}

	public static void register() {
		DREAD = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Jugcraft.id("dread"), new DreadEffect());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> LAST.clear());
	}

	/** Whether the Dread boon can fill {@code target} with dread: a monster, never a boss. */
	public static boolean dreads(LivingEntity target) {
		return target instanceof Enemy && !target.is(FrightBoon.FRIGHTLESS);
	}

	/** The arm has struck {@code target}: a monster not filled with dread lately is filled with it now. */
	static void struck(ServerLevel level, LivingEntity target, LivingEntity attacker) {
		if (DREAD == null || !dreads(target)) {
			return;
		}
		long now = level.getGameTime();
		LAST.values().removeIf(at -> now - at >= COOLDOWN || now < at);
		if (LAST.containsKey(target.getUUID()) || LAST.size() >= MAX) {
			return;
		}
		if (target.addEffect(new MobEffectInstance(DREAD, TICKS, 0), attacker)) {
			LAST.put(target.getUUID(), now);
			level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + target.getBbHeight() + 0.2, target.getZ(), 6, 0.3, 0.2, 0.3, 0.02);
			level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SKELETON_HORSE_AMBIENT, SoundSource.PLAYERS, 0.8F, 0.6F);
		}
	}

	/** What a blow of {@code amount} (after armor) on {@code target} comes to: {@value #SHARE} more while it is in dread. */
	public static float taken(LivingEntity target, float amount) {
		return DREAD != null && amount > 0.0F && target.hasEffect(DREAD) ? amount * (1.0F + SHARE) : amount;
	}
}
