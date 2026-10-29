package io.github.jimbozoomer.jugcraft.concordance.rime;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.effect.EffectKind;
import io.github.jimbozoomer.jugcraft.concordance.effect.EffectSpec;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Cooled ({@code jugcraft:cooled}; Concordance Chunk E, E3, docs/features/cinder-tyrant-concordance.md): the Rimekeepers'
 * heat buffering, which Quench puts on the creature it touches and a Quench Flask on everyone it splashes. While it lasts
 * the creature's burning is put out each tick, so it never burns, and hot ground (a magma block, the Cinder Kiln's molten
 * slag) does not hurt it. Fire itself, lava and a creature's fire (the Cinder Tyrant's breath, his spit and his wave) still
 * do: it buffers heat, it is no Fire Resistance. Timed; milk ends it. Registered whatever the Concordance switch, so a
 * saved Cooled keeps its id. Decided on the server.
 */
public final class Cooling {
	/** Its id, Quench's status (tools/concordance_rime.py COOLED). */
	public static final String STATUS = "jugcraft:cooled";
	public static Holder<MobEffect> COOLED;

	private Cooling() {
	}

	/** The status: helpful, no attribute change; each tick it puts out the creature's burning. */
	private static final class Effect extends MobEffect {
		Effect() {
			super(MobEffectCategory.BENEFICIAL, 0x9FD8E8);
		}

		@Override
		public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
			return true;
		}

		@Override
		public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
			if (entity.getRemainingFireTicks() > 0) {
				entity.clearFire();
			}
			return true; // it never ends itself: it lasts its time
		}
	}

	static void register() {
		COOLED = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Jugcraft.id("cooled"), new Effect());
		// Hot ground and burning pass a cooled creature by; everything else of fire still reaches it.
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !(cooled(entity)
				&& (source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.ON_FIRE))));
	}

	/**
	 * After a status took (ConcordanceEffects): Quench's Cooled puts the creature's burning out at once, not only from
	 * the status's next tick, so a friend quenched in fire is out the moment it takes. Any other status is left alone.
	 */
	public static void took(LivingEntity target, EffectSpec effect) {
		if (effect.kind() == EffectKind.STATUS && STATUS.equals(effect.status())) {
			target.clearFire();
		}
	}

	/** Whether {@code entity} is Cooled now. */
	public static boolean cooled(LivingEntity entity) {
		return COOLED != null && entity.hasEffect(COOLED);
	}
}
