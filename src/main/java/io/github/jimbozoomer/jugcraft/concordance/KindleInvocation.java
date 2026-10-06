package io.github.jimbozoomer.jugcraft.concordance;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellExecution;
import net.spell_power.api.SpellPower;
import org.jspecify.annotations.Nullable;

/**
 * Kindle, the first invocation: a mote of steady light in the open block where the caster looks, up to
 * {@value #RANGE} blocks away, lasting {@value #MOTE_STEPS} steps of {@link LumenMoteBlock#STEP_TICKS} ticks.
 * <p>
 * The spell targets its caster and delivers directly, so Spell Engine never trusts a client's aim: this handler (the
 * spell's {@code CUSTOM} impact, server only) traces the caster's own view on the server. It re-checks the cast with
 * {@link ConcordanceSpells#refusal}, since a cast request may reach the impact by a path the gate did not see, and
 * lights through {@link Illumination}, the one way the Concordance changes world light. A cast that lights nothing
 * reports failure, so Spell Engine applies no cooldown and Jugcraft takes no Focus. Keep RANGE and MOTE_STEPS equal to
 * KINDLE_RANGE and KINDLE_MOTE_STEPS in tools/concordance.py.
 */
public final class KindleInvocation {
	public static final int RANGE = 16;
	public static final int MOTE_STEPS = 15;
	private static final SpellHandlers.ImpactResult FAILED = new SpellHandlers.ImpactResult(false, false);

	private KindleInvocation() {
	}

	/** Spell Engine's {@code CUSTOM} impact {@code jugcraft:kindle_light}. */
	static SpellHandlers.ImpactResult impact(Holder<Spell> spell, SpellPower.Result power, LivingEntity caster,
			@Nullable Entity target, SpellExecution.ImpactContext context) {
		try {
			if (!(caster instanceof ServerPlayer player)) {
				return FAILED;
			}
			ServerLevel level = player.level();
			if (ConcordanceSpells.refusal(player, spell) != null) {
				return FAILED;
			}
			Illumination.Result result = Illumination.kindle(level, player, aim(level, player), MOTE_STEPS);
			switch (result) {
				case NO_SPACE -> player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.kindle.no_space", RANGE));
				case NOT_ALLOWED -> player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.kindle.not_allowed"));
				default -> {
				}
			}
			return new SpellHandlers.ImpactResult(result.lit(), false);
		} catch (RuntimeException problem) {
			Jugcraft.LOGGER.error("Arcane Concordance: Kindle failed", problem);
			return FAILED;
		}
	}

	/**
	 * Where Kindle lights for this player now: the open side of the first block (or liquid surface) their view meets
	 * within {@value #RANGE} blocks, else the block at full range.
	 */
	public static BlockPos aim(ServerLevel level, ServerPlayer player) {
		Vec3 eye = player.getEyePosition();
		Vec3 end = eye.add(player.getViewVector(1.0F).scale(RANGE));
		BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
		if (hit.getType() == HitResult.Type.BLOCK) {
			return hit.getBlockPos().relative(hit.getDirection());
		}
		return BlockPos.containing(end);
	}
}
