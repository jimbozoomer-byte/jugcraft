package io.github.jimbozoomer.jugcraft.client.guns;

import io.github.jimbozoomer.jugcraft.guns.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * How far the player is into aiming down the sights: eases in and out over {@link #EASE_TICKS}. Aimed, the view
 * narrows by the gun's zoom ({@link GunLooks.Look#zoom}; slice 6), as far as the aim has come.
 */
public final class GunView {
	public static final int EASE_TICKS = 4;
	private static float previous;
	private static float current;
	/** The last field of view modifier the zoom was given and what it made of it (for the client game tests). */
	private static float lastIn = Float.NaN;
	private static float lastOut = Float.NaN;

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

	/**
	 * The player's field of view modifier (vanilla's: sprinting, flying, a drawn bow) narrowed while they aim down a gun's
	 * sights: times the gun's zoom, eased in and out with the aim. Called by GunFovMixin each time vanilla asks; the
	 * camera already smooths the modifier between ticks.
	 */
	public static float fov(AbstractClientPlayer player, float modifier) {
		if (player != Minecraft.getInstance().player || !(player.getMainHandItem().getItem() instanceof GunItem gun)) {
			return modifier;
		}
		float narrowed = modifier * Mth.lerp(aim(1.0F), 1.0F, GunLooks.of(gun.name()).zoom());
		lastIn = modifier;
		lastOut = narrowed;
		return narrowed;
	}

	/** The last modifier {@link #fov} was given, NaN before it ever ran (for the client game tests). */
	public static float lastFovIn() {
		return lastIn;
	}

	/** What {@link #fov} last made of it (for the client game tests). */
	public static float lastFovOut() {
		return lastOut;
	}
}
