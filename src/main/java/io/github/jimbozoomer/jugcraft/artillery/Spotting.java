package io.github.jimbozoomer.jugcraft.artillery;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Target marks made with a Range Finder (batch 51). Each player has at most one, kept on the server for
 * {@value JugcraftArtillery#MARK_TTL} ticks. A gun fires at its gunner's own mark, else at the nearest other player's
 * mark made within {@value JugcraftArtillery#MARK_RANGE} blocks of the gun, so a spotter in an observation balloon can
 * direct a battery below. Marks are not saved: they are fire orders, not part of the world.
 */
public final class Spotting {
	private record Mark(ResourceKey<Level> dimension, BlockPos target, Vec3 spotter, long time) {
	}

	private static final Map<UUID, Mark> MARKS = new HashMap<>();

	private Spotting() {
	}

	public static void mark(Player player, BlockPos target) {
		MARKS.put(player.getUUID(), new Mark(player.level().dimension(), target.immutable(), player.position(),
				player.level().getGameTime()));
	}

	public static void clear(Player player) {
		MARKS.remove(player.getUUID());
	}

	/** The player's own mark, if they have one in this dimension that has not run out. */
	public static @Nullable BlockPos own(ServerLevel level, Player player) {
		Mark mark = MARKS.get(player.getUUID());
		return mark == null || mark.dimension() != level.dimension() || level.getGameTime() - mark.time() > JugcraftArtillery.MARK_TTL
				? null : mark.target();
	}

	/** The target a gun at {@code gun} crewed by {@code gunner} should fire at, or null if there is none. */
	public static @Nullable BlockPos target(ServerLevel level, @Nullable Entity gunner, Vec3 gun) {
		long now = level.getGameTime();
		MARKS.values().removeIf(mark -> now - mark.time() > JugcraftArtillery.MARK_TTL);
		if (gunner != null) {
			Mark own = MARKS.get(gunner.getUUID());
			if (own != null && own.dimension() == level.dimension()) {
				return own.target();
			}
		}
		Mark best = null;
		double bestDistance = (double) JugcraftArtillery.MARK_RANGE * JugcraftArtillery.MARK_RANGE;
		for (Mark mark : MARKS.values()) {
			double distance = mark.spotter().distanceToSqr(gun);
			if (mark.dimension() == level.dimension() && distance <= bestDistance) {
				best = mark;
				bestDistance = distance;
			}
		}
		return best == null ? null : best.target();
	}
}
