package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import org.jspecify.annotations.Nullable;

/**
 * Drops what the server sends to game-test mock players. A mock player's connection ends in Netty's EmbeddedChannel
 * (vanilla's {@code makeMockServerPlayerInLevel} and the tests that build their own player the same way), which keeps
 * every packet written to it until something reads it, and no test does. Without fixture cleanup, players stayed logged
 * in for the whole run, so every packet (its command tree and recipes on joining, then every later player's join
 * message and player-list entry) stayed in the heap until the server suite ran out of its 4 GB. Each tick this empties
 * those channels, and every 2,400 ticks logs how many packets it has released and the heap in use; a connection that
 * is not an EmbeddedChannel (a real or integrated client's) is never touched.
 * <p>
 * Fake players: every ServerPlayer made for an identity the player list has no tracker for leaves one in it, an
 * advancement tracker (a progress record for every advancement, vanilla's recipe unlocks and Jugcraft's 700 or so among
 * them, and listeners on every criterion trigger) and a statistics counter, until that identity leaves the game. A
 * fake player never joins, so never leaves: Fabric's FakePlayer only stops one taking a present player's tracker or
 * being awarded anything. Every test with a stand-in for an absent owner (concordance.Authority) or a fake with a
 * fresh UUID added one for the rest of the run, and the tracker holds its fake player, and through it the level. Every {@value #SWEEP} ticks
 * {@link #releaseAbsentStandIns} lets go of the trackers and counters of fake players whose identity nobody online
 * has. Tracker objects a fake player still holds keep working for it; a real player who joins later gets a new tracker.
 */
public final class MockPlayerChannels implements ModInitializer {
	private static final Field CONNECTION = field(ServerCommonPacketListenerImpl.class, Connection.class);
	private static final Field CHANNEL = field(Connection.class, Channel.class);
	/** Ticks between sweeps for absent fake players' trackers. */
	static final int SWEEP = 200;
	private static final @Nullable Field TRACKERS = mapOf(PlayerList.class, PlayerAdvancements.class);
	private static final @Nullable Field COUNTERS = mapOf(PlayerList.class, ServerStatsCounter.class);
	private static final @Nullable Field TRACKER_PLAYER = optional(PlayerAdvancements.class, ServerPlayer.class);
	private static long released;
	private static long standIns;

	/** Only embedded test connections, never the real client of a rendered/integrated test. */
	static boolean isMock(ServerPlayer player) {
		return channel(player) instanceof EmbeddedChannel;
	}

	/** Dispose a finished fixture's fake transport, including packets queued before the first tick. */
	static void dispose(ServerPlayer player) {
		if (channel(player) instanceof EmbeddedChannel embedded) {
			released += embedded.outboundMessages().size();
			embedded.finishAndReleaseAll();
		}
	}

	@Override
	public void onInitialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			int mock = 0;
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (channel(player) instanceof EmbeddedChannel embedded) {
					mock++;
					released += embedded.outboundMessages().size();
					embedded.releaseOutbound();
				}
			}
			if (server.getTickCount() % SWEEP == 0) {
				standIns += releaseAbsentStandIns(server);
			}
			if (server.getTickCount() % 2400 == 0) {
				// The heap in use beside it shows whether anything else still grows over the run; the player list's
				// trackers should stay close to the players online, and the loaded chunks should not keep climbing.
				Runtime runtime = Runtime.getRuntime();
				Jugcraft.LOGGER.info("[mock-player-channels] {} mock players online of {}; {} packets to them released so far; {} advancement "
						+ "trackers and {} statistics counters held, {} absent fake players' released so far; {} overworld chunks loaded; "
						+ "heap {} of {} MB in use", mock, server.getPlayerList().getPlayers().size(), released, size(TRACKERS, server.getPlayerList()),
						size(COUNTERS, server.getPlayerList()), standIns, server.overworld().getChunkSource().getLoadedChunksCount(),
						(runtime.totalMemory() - runtime.freeMemory()) >> 20, runtime.maxMemory() >> 20);
			}
		});
	}

	/**
	 * Lets go of what the player list keeps for fake players whose identity is not in the game: stops their advancement
	 * trackers listening and forgets them and their statistics counters. A tracker whose identity is online (a stand-in
	 * for a present owner shares the owner's) and every real or mock player's are left alone. Returns how many it
	 * released; 0 if this game's player list is not laid out as expected (then nothing is touched).
	 */
	@SuppressWarnings("unchecked")
	static int releaseAbsentStandIns(MinecraftServer server) {
		if (TRACKERS == null || TRACKER_PLAYER == null) {
			return 0;
		}
		PlayerList list = server.getPlayerList();
		int count = 0;
		try {
			Map<UUID, PlayerAdvancements> trackers = (Map<UUID, PlayerAdvancements>) TRACKERS.get(list);
			Map<UUID, ?> counters = COUNTERS == null ? null : (Map<UUID, ?>) COUNTERS.get(list);
			for (Iterator<Map.Entry<UUID, PlayerAdvancements>> it = trackers.entrySet().iterator(); it.hasNext();) {
				Map.Entry<UUID, PlayerAdvancements> entry = it.next();
				if (list.getPlayer(entry.getKey()) != null || !(TRACKER_PLAYER.get(entry.getValue()) instanceof FakePlayer)) {
					continue;
				}
				entry.getValue().clearTriggers();
				it.remove();
				if (counters != null) {
					counters.remove(entry.getKey());
				}
				count++;
			}
		} catch (IllegalAccessException | ClassCastException e) {
			Jugcraft.LOGGER.warn("[mock-player-channels] could not release absent fake players' trackers", e);
		}
		return count;
	}

	/** Whether {@code player}'s fake transport is still open (false for a closed one and for a real client's). */
	static boolean transportOpen(ServerPlayer player) {
		return channel(player) instanceof EmbeddedChannel embedded && embedded.isOpen();
	}

	/** The player list's advancement trackers by identity (tests), or null when it is not laid out as expected. */
	static @Nullable Map<?, ?> trackers(PlayerList list) {
		try {
			return TRACKERS == null ? null : (Map<?, ?>) TRACKERS.get(list);
		} catch (IllegalAccessException | ClassCastException e) {
			return null;
		}
	}

	private static int size(@Nullable Field map, PlayerList list) {
		try {
			return map == null ? -1 : ((Map<?, ?>) map.get(list)).size();
		} catch (IllegalAccessException | ClassCastException e) {
			return -1;
		}
	}

	private static Channel channel(ServerPlayer player) {
		if (player.connection == null) {
			return null;
		}
		try {
			return (Channel) CHANNEL.get(CONNECTION.get(player.connection));
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Field field(Class<?> owner, Class<?> type) {
		for (Field field : owner.getDeclaredFields()) {
			if (field.getType() == type) {
				field.setAccessible(true);
				return field;
			}
		}
		throw new IllegalStateException(owner.getName() + " has no " + type.getSimpleName() + " field");
	}

	/** The field of this type, or null (with a warning) when the class has none: the sweep is then skipped, never the run. */
	private static @Nullable Field optional(Class<?> owner, Class<?> type) {
		try {
			return field(owner, type);
		} catch (RuntimeException e) {
			Jugcraft.LOGGER.warn("[mock-player-channels] {}; absent fake players' trackers will not be released", e.getMessage());
			return null;
		}
	}

	/** The {@code Map<UUID, value>} field of {@code owner}, or null (with a warning). */
	private static @Nullable Field mapOf(Class<?> owner, Class<?> value) {
		for (Field field : owner.getDeclaredFields()) {
			Type generic = field.getGenericType();
			if (Map.class.isAssignableFrom(field.getType()) && generic instanceof ParameterizedType parameterized
					&& parameterized.getActualTypeArguments().length == 2 && parameterized.getActualTypeArguments()[0] == UUID.class
					&& parameterized.getActualTypeArguments()[1] == value) {
				try {
					field.setAccessible(true);
					return field;
				} catch (RuntimeException e) {
					break;
				}
			}
		}
		Jugcraft.LOGGER.warn("[mock-player-channels] {} has no Map<UUID, {}> field; absent fake players' trackers will not be released",
				owner.getName(), value.getSimpleName());
		return null;
	}
}
