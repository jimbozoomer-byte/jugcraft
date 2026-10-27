package io.github.jimbozoomer.jugcraft.lair.abbot;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * The Lantern Lens (docs/features/glass-abbot.md): a lens of clear glass in a gold rim, a lantern's flame caught in it.
 * Held in the offhand, every {@value #EVERY} ticks it outlines in light, for its holder alone, every invisible creature
 * within {@value #RANGE} blocks of them, and the real Glass Abbot among his images. Nobody else sees it, and nothing seen
 * is changed: unlike the Hag's Eye, it sets no glow on anyone. Decided on the server; the work is bounded: one look round
 * each holder every {@value #EVERY} ticks, and a few particles a creature.
 */
public final class LanternLens {
	public static final int EVERY = 20;
	public static final double RANGE = 12.0;
	private static final DustParticleOptions LIGHT = new DustParticleOptions(0xFFE7A0, 0.9F);

	private LanternLens() {
	}

	static void register() {
		ServerTickEvents.END_SERVER_TICK.register(LanternLens::tick);
	}

	/** Whether {@code player} holds a Lantern Lens in their offhand. */
	public static boolean holds(ServerPlayer player) {
		return JugcraftAbbot.LANTERN_LENS != null && player.getOffhandItem().is(JugcraftAbbot.LANTERN_LENS);
	}

	private static void tick(MinecraftServer server) {
		if (server.getTickCount() % EVERY != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!player.isSpectator() && holds(player)) {
				see(player);
			}
		}
	}

	/** Whether the lens shows {@code living} to {@code holder}: invisible, or the Glass Abbot while his images stand. */
	public static boolean shows(ServerPlayer holder, LivingEntity living) {
		if (living == holder || living.isSpectator() || !living.isAlive() || living.distanceToSqr(holder) > RANGE * RANGE) {
			return false;
		}
		return living.isInvisible() || (living instanceof GlassAbbotEntity abbot && !abbot.images().isEmpty());
	}

	/** Outlines, for {@code holder} alone, everything the lens shows them; returns how many. */
	public static int see(ServerPlayer holder) {
		if (!(holder.level() instanceof ServerLevel level)) {
			return 0;
		}
		int seen = 0;
		for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, holder.getBoundingBox().inflate(RANGE), other -> shows(holder, other))) {
			outline(level, holder, living.getBoundingBox());
			seen++;
		}
		return seen;
	}

	/** Light at the corners and the middle of each edge of {@code box}, sent to {@code holder} only. */
	private static void outline(ServerLevel level, ServerPlayer holder, AABB box) {
		double[] xs = {box.minX, (box.minX + box.maxX) / 2.0, box.maxX};
		double[] ys = {box.minY, (box.minY + box.maxY) / 2.0, box.maxY};
		double[] zs = {box.minZ, (box.minZ + box.maxZ) / 2.0, box.maxZ};
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 3; k++) {
					// On the box's edges: at least two of the three on a face.
					int faces = (i != 1 ? 1 : 0) + (j != 1 ? 1 : 0) + (k != 1 ? 1 : 0);
					if (faces >= 2) {
						level.sendParticles(holder, LIGHT, true, false, xs[i], ys[j], zs[k], 1, 0.0, 0.0, 0.0, 0.0);
					}
				}
			}
		}
	}
}
