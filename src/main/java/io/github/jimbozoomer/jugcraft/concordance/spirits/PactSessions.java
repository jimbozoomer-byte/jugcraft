package io.github.jimbozoomer.jugcraft.concordance.spirits;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Concordance Chunk D, D-04 (D-S7, D-S9): the open conversations at pact perches, one a player, in memory only (invited
 * and negotiating are never stored; a restart forgets them). A conversation lives while its player is online with that
 * perch's screen still open and a request came within TIMEOUT_TICKS; it ends when they decline, close the screen, leave
 * the game, or let it time out. A visiting Stolas leaves when no conversation at his perch is live (anyone's: he stays
 * while any player's screen there is open, the perch's owner's or not). Server thread only.
 */
public final class PactSessions {
	/** An open conversation ends after this many ticks without a request (five minutes). */
	public static final long TIMEOUT_TICKS = 6000L;
	private static final Map<UUID, Session> SESSIONS = new HashMap<>();
	/**
	 * GC-02: how many times anyone has spoken at a perch (opened its screen or sent a request) since this server started;
	 * orders the speakers (memory only).
	 */
	private static long spoken;

	private record Session(GlobalPos perch, int containerId, long until, long order) {
	}

	private PactSessions() {
	}

	static void register() {
		// Forgotten as the player leaves the list (PlayerList.remove: a logout, or a test player logged out), as RateGate's
		// limits are, and on a disconnect; live() asks the player list too, so a stale entry keeps no visitor.
		ServerPlayerEvents.LEAVE.register(player -> SESSIONS.remove(player.getUUID()));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SESSIONS.remove(handler.getPlayer().getUUID()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SESSIONS.clear());
	}

	static void open(ServerPlayer player, GlobalPos perch, int containerId, long now) {
		SESSIONS.put(player.getUUID(), new Session(perch, containerId, now + TIMEOUT_TICKS, ++spoken));
	}

	static void touch(UUID player, int containerId, long now) {
		Session session = SESSIONS.get(player);
		if (session != null && session.containerId() == containerId) {
			SESSIONS.put(player, new Session(session.perch(), containerId, now + TIMEOUT_TICKS, ++spoken));
		}
	}

	static void close(UUID player, int containerId) {
		Session session = SESSIONS.get(player);
		if (session != null && session.containerId() == containerId) {
			SESSIONS.remove(player);
		}
	}

	/** Whether {@code player}'s conversation in menu {@code containerId} is over: closed, never opened, or idle past its time. */
	public static boolean expired(UUID player, int containerId, long now) {
		Session session = SESSIONS.get(player);
		return session == null || session.containerId() != containerId || now > session.until();
	}

	/** Whether a conversation is live at {@code perch}: its player online, that perch's screen still open, its time not past. */
	public static boolean live(MinecraftServer server, GlobalPos perch, long now) {
		for (Map.Entry<UUID, Session> entry : SESSIONS.entrySet()) {
			Session session = entry.getValue();
			if (session.perch().equals(perch) && now <= session.until()
					&& server.getPlayerList().getPlayer(entry.getKey()) instanceof ServerPlayer player
					&& player.containerMenu instanceof PactMenu menu && menu.containerId == session.containerId()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Concordance garden cohort, GC-02 (GC-S6): the player of the live conversation at {@code perch} who spoke last (opened
	 * its screen or sent a request), or null when none is live there (exactly when {@link #live} is false: the same tests).
	 */
	public static @Nullable ServerPlayer speaker(MinecraftServer server, GlobalPos perch, long now) {
		ServerPlayer latest = null;
		long order = Long.MIN_VALUE;
		for (Map.Entry<UUID, Session> entry : SESSIONS.entrySet()) {
			Session session = entry.getValue();
			if (session.order() > order && session.perch().equals(perch) && now <= session.until()
					&& server.getPlayerList().getPlayer(entry.getKey()) instanceof ServerPlayer player
					&& player.containerMenu instanceof PactMenu menu && menu.containerId == session.containerId()) {
				latest = player;
				order = session.order();
			}
		}
		return latest;
	}
}
