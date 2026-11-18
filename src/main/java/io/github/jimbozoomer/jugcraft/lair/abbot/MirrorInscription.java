package io.github.jimbozoomer.jugcraft.lair.abbot;

import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceEffects;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

/**
 * The Mirror Inscription (docs/features/glass-abbot.md): a hand mirror of the Abbot's glass in a gold frame, a line of
 * amethyst script round its rim. Held in the offhand by someone a ward protects (who has absorption hearts, from a Strata
 * ward, a golden apple or anything else), it turns the next shot that would strike them for {@value #MAX} or less (an
 * arrow, a trident, a fireball) back at its shooter: the shot is spent, and the shooter takes what it would have done, as
 * magic, if the holder may harm them (no harm to a party member, nor to a player where players may not fight). Then it
 * rests {@value #REST} ticks (its cooldown shows on it). Decided on the server.
 */
public final class MirrorInscription {
	public static final float MAX = 8.0F;
	public static final int REST = 600;

	private MirrorInscription() {
	}

	static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !(entity instanceof ServerPlayer player
				&& source.is(DamageTypeTags.IS_PROJECTILE) && player.level() instanceof ServerLevel level && turnBack(level, player, source, amount)));
	}

	/** Whether the inscription in {@code player}'s offhand turns {@code source} back now; if it does, it rests. */
	public static boolean turnBack(ServerLevel level, ServerPlayer player, DamageSource source, float amount) {
		ItemStack offhand = player.getOffhandItem();
		if (!offhand.is(JugcraftAbbot.MIRROR_INSCRIPTION) || player.getCooldowns().isOnCooldown(offhand) || player.getAbsorptionAmount() <= 0.0F
				|| amount > MAX) {
			return false;
		}
		player.getCooldowns().addCooldown(offhand, REST);
		if (source.getDirectEntity() instanceof Projectile shot) {
			shot.discard();
		}
		if (source.getEntity() instanceof LivingEntity shooter && shooter.isAlive() && ConcordanceEffects.mayHarm(player, shooter)
				&& Authority.mayStrike(level, player, null, shooter)) {
			shooter.hurtServer(level, level.damageSources().indirectMagic(player, player), amount);
			level.sendParticles(ParticleTypes.END_ROD, shooter.getX(), shooter.getY() + shooter.getBbHeight() / 2.0, shooter.getZ(), 8, 0.3, 0.4, 0.3,
					0.05);
		}
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.6F);
		level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.4, 0.5, 0.4, 0.05);
		player.sendOverlayMessage(Component.translatable("message.jugcraft.glass_abbot.inscription"));
		return true;
	}
}
