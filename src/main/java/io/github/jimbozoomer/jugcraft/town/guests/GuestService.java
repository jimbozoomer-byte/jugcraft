package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

/** The town guests' entry points (coordinator's seam): registration, talking to a guest, and its identity check. */
public final class GuestService {
	public static ExtendedMenuType<GuestMenu, GuestMenu.Opening> MENU;
	private static final Map<UUID, Session> SESSIONS = new HashMap<>();

	/**
	 * A player's place in the menu protocol, kept across menus while they are connected (in memory, dropped when they
	 * leave or the server stops: no daily limit or other protection is kept here, so a reconnect resets none).
	 */
	public static final class Session {
		/** The revision the player's next click must carry; a new session starts at a random one. */
		int revision = java.util.concurrent.ThreadLocalRandom.current().nextInt(GuestMenu.REVISIONS);
		/** The game time of the player's last action taken. */
		long lastAction = -1_000L;

		/** Moves the revision on: every click carrying an earlier one is refused. */
		void invalidate() {
			revision = (revision + 1) % GuestMenu.REVISIONS;
		}
	}

	private GuestService() {
	}

	/** Called once from {@code JugcraftTown.register}. */
	public static void register() {
		MENU = Registry.register(BuiltInRegistries.MENU, Jugcraft.id("town_guest"),
				new ExtendedMenuType<>(GuestMenu::new, GuestMenu.Opening.STREAM_CODEC));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SESSIONS.clear());
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SESSIONS.remove(handler.player.getUUID()));
		GuestLedger.register();
		GuestFavors.register();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			GuestRepairCommand.register(dispatcher);
			GuestPlacement.register(dispatcher);
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
			if (entity instanceof Townsfolk npc && npc.guest() != null && entity.getRemovalReason() != null
					&& entity.getRemovalReason().shouldDestroy()) {
				// Killed or removed for good: the ledger keeps the identity, spouse and affection (ignored for a duplicate).
				GuestLedger.get(level.getServer()).lost(npc.guest(), npc.getUUID());
			}
		});
	}

	/** The player's session, made on first use. Server thread only. */
	public static Session session(UUID player) {
		return SESSIONS.computeIfAbsent(player, id -> new Session());
	}

	/** A player talks to a guest: its menu, or why it cannot talk yet. */
	public static InteractionResult talk(Townsfolk npc, ServerPlayer player) {
		GuestRoster.Guest guest = GuestRoster.get(npc.guest());
		if (guest == null || !guest.available()) {
			player.sendSystemMessage(Component.translatableWithFallback("message.jugcraft.guest.unavailable",
					"%s is not receiving visitors yet.", npc.getDisplayName()));
			return InteractionResult.SUCCESS;
		}
		GuestMenu.open(player, npc, guest.id());
		return InteractionResult.SUCCESS;
	}

	/**
	 * Once a second for each loaded guest: a duplicate of an identity another entity holds leaves (true), never freeing
	 * anything; the canonical one records where it is. A guest id no longer in the roster is left alone, inert.
	 */
	public static boolean tick(ServerLevel level, Townsfolk npc) {
		String guest = npc.guest();
		if (guest == null || GuestRoster.get(guest) == null) {
			return false;
		}
		GuestLedger ledger = GuestLedger.get(level.getServer());
		if (ledger.verify(guest, npc.getUUID()) == GuestLedger.Claim.DUPLICATE) {
			npc.discard();
			return true;
		}
		ledger.seen(guest, npc.getUUID(), level.dimension(), npc.blockPosition());
		return false;
	}
}
