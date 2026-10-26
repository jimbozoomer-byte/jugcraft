package io.github.jimbozoomer.jugcraft.test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test-only diagnostics for the game-test servers (docs/TESTING.md, "When the server tests stall"). A server tick that
 * has run for thirty seconds gets the server thread's stack written to the log, so a stalled CI run names the code it is
 * stuck in. A tick slower than ten seconds, and every five minutes of game time, logs the entities loaded by type,
 * so a leak shows as it grows. It only reads and logs; it never changes a test or the world.
 */
public final class TickWatchdog implements ModInitializer {
	private static final Logger LOG = LoggerFactory.getLogger("jugcraft-test");
	private static final long SLOW_TICK_NANOS = 10_000_000_000L;
	private static final long STUCK_MILLIS = 30_000L;
	private static final long CHECK_MILLIS = 5_000L;
	private static final int MAX_DUMPS = 12;
	private static final int MAX_FRAMES = 160;

	private static volatile long tickStarted;
	private static volatile int tick;
	private static volatile boolean watching;

	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			tick = server.getTickCount();
			tickStarted = System.nanoTime();
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			long took = System.nanoTime() - tickStarted;
			tickStarted = 0L;
			if (took > SLOW_TICK_NANOS) {
				LOG.warn("[tick watchdog] tick {} took {} s; entities: {}", tick, took / 1_000_000_000L, census(server));
			} else if (server.getTickCount() % 6000 == 0) {
				LOG.info("[tick watchdog] tick {}; entities: {}", server.getTickCount(), census(server));
			}
		});
		// SERVER_STARTED runs on the server thread, the one to watch.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> watch(Thread.currentThread()));
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> watching = false);
	}

	private static void watch(Thread serverThread) {
		watching = true;
		Thread watchdog = new Thread(() -> {
			int dumps = 0;
			int lastTick = -1;
			long lastDump = 0L;
			while (watching && dumps < MAX_DUMPS) {
				try {
					Thread.sleep(CHECK_MILLIS);
				} catch (InterruptedException e) {
					return;
				}
				long started = tickStarted;
				if (started == 0L) {
					continue;
				}
				long millis = (System.nanoTime() - started) / 1_000_000L;
				long now = System.currentTimeMillis();
				// Thirty seconds into a stuck tick, then every minute while it stays stuck.
				if (millis >= STUCK_MILLIS && (tick != lastTick || now - lastDump >= 2 * STUCK_MILLIS)) {
					lastTick = tick;
					lastDump = now;
					dumps++;
					LOG.warn("[tick watchdog] tick {} has run for {} s; server thread:\n{}", tick, millis / 1000L, stack(serverThread));
				}
			}
		}, "Jugcraft test tick watchdog");
		watchdog.setDaemon(true);
		watchdog.start();
	}

	private static String stack(Thread thread) {
		StackTraceElement[] frames = thread.getStackTrace();
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < Math.min(frames.length, MAX_FRAMES); i++) {
			out.append("\tat ").append(frames[i]).append('\n');
		}
		if (frames.length > MAX_FRAMES) {
			out.append("\t... ").append(frames.length - MAX_FRAMES).append(" more\n");
		}
		return out.toString();
	}

	/** The ten most numerous entity types loaded across every level, and the total. */
	private static String census(MinecraftServer server) {
		Map<String, Integer> counts = new HashMap<>();
		int total = 0;
		for (ServerLevel level : server.getAllLevels()) {
			for (Entity entity : level.getAllEntities()) {
				counts.merge(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(), 1, Integer::sum);
				total++;
			}
		}
		return total + " total, " + counts.entrySet().stream()
				.sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
				.limit(10)
				.map(entry -> entry.getKey() + "=" + entry.getValue())
				.collect(Collectors.joining(", "));
	}
}
