package io.github.jimbozoomer.jugcraft.guns;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Bleeding (the darts-and-bleeding follow-up, docs/features/guns.md; the owner's "lacerated"): a creature bleeding loses
 * {@link JugcraftGuns#BLEED_DAMAGE} every {@link JugcraftGuns#BLEED_TICKS} ticks, twice as often at each level above the
 * first, through its armour ({@link JugcraftGuns#BLEED_TYPE}), and can bleed to death. Some guns' hits give it
 * ({@link JugcraftGuns#BLEEDS}); milk stops it, as any effect.
 */
final class BleedingEffect extends MobEffect {
	BleedingEffect() {
		super(MobEffectCategory.HARMFUL, 0x8A0B0B);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		int every = JugcraftGuns.BLEED_TICKS >> amplifier;
		return every <= 0 || duration % every == 0;
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		entity.hurtServer(level, new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
				.getOrThrow(JugcraftGuns.BLEED_TYPE)), JugcraftGuns.BLEED_DAMAGE);
		return true;
	}
}
