package io.github.jimbozoomer.jugcraft.client.guns;

import io.github.jimbozoomer.jugcraft.guns.GunItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/** How far the player is into aiming down the sights: eases in and out over {@link #EASE_TICKS}. */
public final class GunView {
	public static final int EASE_TICKS = 4;
	private static float previous;
	private static float current;

	private GunView() {
	}

	/** Once a client tick. */
	public static void tick(LocalPlayer player) {
		previous = current;
		boolean aiming = player != null && GunItem.aiming(player, player.getMainHandItem())
				&& player.getMainHandItem().getItem() instanceof GunItem;
		current = Mth.clamp(current + (aiming ? 1.0F : -1.0F) / EASE_TICKS, 0.0F, 1.0F);
	}

	/** Between the last two ticks, eased. */
	public static float aim(float partialTick) {
		float t = Mth.lerp(partialTick, previous, current);
		return t * t * (3.0F - 2.0F * t);
	}
}
