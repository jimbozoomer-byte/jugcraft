package io.github.jimbozoomer.jugcraft.town.guests;

import net.minecraft.network.chat.Component;

/** What an action did, and what the player is told. {@code done} is false whenever nothing changed. */
public record GuestResult(boolean done, Component message) {
	public static GuestResult done(String key, String english, Object... args) {
		return new GuestResult(true, Component.translatableWithFallback(key, english, args));
	}

	public static GuestResult refused(String key, String english, Object... args) {
		return new GuestResult(false, Component.translatableWithFallback(key, english, args));
	}
}
