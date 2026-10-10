package io.github.jimbozoomer.jugcraft.client;

import eu.pb4.trinkets.api.client.renderer.element.TrinketRenderElements;
import io.github.jimbozoomer.jugcraft.client.trinket.UnlessCoveredTrinketElement;

/**
 * Wayfaring on the client: the render element its worn belt and boots are drawn through
 * ({@link UnlessCoveredTrinketElement}: not under covering armour, and only while this player's setting allows), given to
 * Trinkets before resources load, so the render definitions in assets/jugcraft/trinkets can name it.
 */
final class WayfaringClient {
	private WayfaringClient() {
	}

	static void register() {
		TrinketRenderElements.ID_MAPPER.put(UnlessCoveredTrinketElement.TYPE, UnlessCoveredTrinketElement.CODEC);
	}
}
