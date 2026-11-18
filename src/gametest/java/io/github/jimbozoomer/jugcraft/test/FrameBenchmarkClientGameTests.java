package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.machine.JugcraftMachines;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * The opt-in client frame benchmark (docs/features/server-load-benchmark.md, "Client frame benchmark"): a fixed scene
 * drawn from a fixed camera, warmed up, then sampled frame by frame. Written, not run.
 * <p>
 * The scene is the server benchmark's factory (ServerLoadGameTests.factoryKinds: 80 machines in five rows) and its
 * crowd (24 costumed villains, standing still so every frame shows the same thing) on a stone floor at noon in clear
 * weather, seen from above one end. Fabric's client test framework runs the game in lockstep, one client tick and one
 * rendered frame at a time, with the integrated server's tick alongside, so a sample is the wall time of one
 * {@code waitTick}: a tick and a frame, as the framework paces them. The sample span runs twice for a repeat. Each span
 * records mean, median, p95 and maximum frame milliseconds, the render thread's allocation per frame
 * ({@link BenchmarkProbe}), and the client JVM's heap before and after; the environment line adds the window size,
 * render distance and frame-rate limit. Lines begin {@value #PREFIX} and are also appended to
 * {@code frame-benchmark.jsonl} in the client's run directory.
 * <p>
 * Only with the system property {@value #PROPERTY} set to true (Gradle: -PjugcraftFrameBenchmark=true, with
 * -PclientTests=FrameBenchmarkClientGameTests); in every other run it returns at once without opening a world. CI's
 * software renderer (Mesa) does not stand for a player's graphics card, so its figures are not a player's frame times.
 */
public class FrameBenchmarkClientGameTests implements FabricClientGameTest {
	static final String PROPERTY = "jugcraft.frameBenchmark";
	static final String PREFIX = "Frame benchmark | ";
	static final long SEED = 0x4A756763L;
	/** The render distance the scene is drawn at, whatever the options held; given back afterwards. */
	static final int RENDER_DISTANCE = 8;
	/** The frame-rate limit option's "unlimited" end, so the limiter does not pace the samples. */
	static final int UNLIMITED = 260;

	static boolean selected() {
		return Boolean.getBoolean(PROPERTY);
	}

	private static int bounded(String name, int fallback, int least, int most) {
		String value = System.getProperty(PROPERTY + "." + name);
		if (value == null || value.isBlank()) {
			return fallback;
		}
		try {
			return Math.max(least, Math.min(most, Integer.parseInt(value.trim())));
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		if (!selected()) {
			return;
		}
		List<String> wrong = BenchmarkStats.check();
		if (!wrong.isEmpty()) {
			throw new AssertionError("The benchmark arithmetic is wrong: " + wrong);
		}
		int warmup = bounded("warmup", 100, 20, 2_000);
		int samples = bounded("samples", 600, 100, 6_000);
		int trials = bounded("trials", 2, 1, 3);
		int renderDistance = context.computeOnClient(client -> client.options.renderDistance().get());
		int framerateLimit = context.computeOnClient(client -> client.options.framerateLimit().get());
		context.runOnClient(client -> {
			client.options.renderDistance().set(RENDER_DISTANCE);
			client.options.framerateLimit().set(UNLIMITED);
		});
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			int x = origin.getX();
			int y = origin.getY();
			int z = origin.getZ();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set 6000");
			server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(x - 4, y - 3, z - 4, x + 43, y - 1, z + 43));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 4, y, z - 4, x + 43, y + 11, z + 43));
			server.runOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				GameRules rules = level.getGameRules();
				rules.set(GameRules.ADVANCE_TIME, false, minecraft);
				rules.set(GameRules.ADVANCE_WEATHER, false, minecraft);
				rules.set(GameRules.SPAWN_MOBS, false, minecraft);
				build(level, origin);
			});
			// The camera: above the near end of the scene, looking down the rows.
			server.runCommand("setblock %d %d %d minecraft:barrier".formatted(x + 16, y + 7, z - 3));
			server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f 0 30", x + 16.5, y + 8, z - 2.5));
			context.waitTicks(20);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_frame_benchmark_scene");

			long clientThread = context.computeOnClient(client -> Thread.currentThread().threadId());
			Map<String, Object> environment = new LinkedHashMap<>();
			environment.put("record", "environment");
			environment.putAll(BenchmarkProbe.environment());
			environment.put("jugcraft", FabricLoader.getInstance().getModContainer("jugcraft")
					.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?"));
			environment.put("minecraft", FabricLoader.getInstance().getModContainer("minecraft")
					.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?"));
			environment.put("windowWidth", context.computeOnClient(client -> client.getWindow().getWidth()));
			environment.put("windowHeight", context.computeOnClient(client -> client.getWindow().getHeight()));
			environment.put("renderDistance", context.computeOnClient(client -> client.options.renderDistance().get()));
			environment.put("framerateLimit", context.computeOnClient(client -> client.options.framerateLimit().get()));
			environment.put("scene", "80 machines (16 each of 5 kinds) and 24 still villains on a 48 x 48 floor, noon, clear");
			environment.put("frame", "wall time of one waitTick: a client tick and a rendered frame in lockstep, the integrated server's tick alongside");
			environment.put("warmupFrames", warmup);
			environment.put("sampleFrames", samples);
			environment.put("trials", trials);
			emit(BenchmarkStats.json(environment));

			context.waitTicks(warmup);
			List<Double> means = new ArrayList<>();
			for (int trial = 1; trial <= trials; trial++) {
				long retainedBefore = BenchmarkProbe.heapRetained();
				long collections = BenchmarkProbe.collections();
				long collectionMillis = BenchmarkProbe.collectionMillis();
				long allocatedBefore = BenchmarkProbe.allocated(clientThread);
				long[] frames = new long[samples];
				for (int i = 0; i < samples; i++) {
					long began = System.nanoTime();
					context.waitTick();
					frames[i] = System.nanoTime() - began;
				}
				long allocatedAfter = BenchmarkProbe.allocated(clientThread);
				BenchmarkStats.Summary s = BenchmarkStats.of(frames, BenchmarkStats.TICK_NANOS);
				means.add(BenchmarkStats.round(s.meanMs()));
				Map<String, Object> out = new LinkedHashMap<>();
				out.put("record", "frames");
				out.put("trial", trial);
				out.put("frames", s.count());
				out.put("meanMs", BenchmarkStats.round(s.meanMs()));
				out.put("p50Ms", BenchmarkStats.round(s.p50Ms()));
				out.put("p95Ms", BenchmarkStats.round(s.p95Ms()));
				out.put("maxMs", BenchmarkStats.round(s.maxMs()));
				out.put("over50ms", s.over());
				out.put("framesPerSecondFromMean", s.meanMs() > 0 ? BenchmarkStats.round(1000.0 / s.meanMs()) : null);
				out.put("renderThreadAllocatedBytesPerFrame", allocatedBefore >= 0 && allocatedAfter >= 0
						? (allocatedAfter - allocatedBefore) / Math.max(1, s.count()) : null);
				out.put("heapRetainedBeforeMiB", retainedBefore >= 0 ? BenchmarkStats.round(BenchmarkStats.mib(retainedBefore)) : null);
				out.put("heapUsedAfterMiB", BenchmarkStats.round(BenchmarkStats.mib(BenchmarkProbe.heapUsed())));
				out.put("collections", BenchmarkProbe.collections() - collections);
				out.put("collectionMs", BenchmarkProbe.collectionMillis() - collectionMillis);
				emit(BenchmarkStats.json(out));
			}
			if (means.size() > 1) {
				double low = means.stream().mapToDouble(Double::doubleValue).min().orElse(0);
				double high = means.stream().mapToDouble(Double::doubleValue).max().orElse(0);
				Map<String, Object> out = new LinkedHashMap<>();
				out.put("record", "repeatability");
				out.put("meanMs", means);
				out.put("meansApart", BenchmarkStats.round(BenchmarkStats.apart(low, high)));
				emit(BenchmarkStats.json(out));
			}
		} finally {
			context.runOnClient(client -> {
				client.options.renderDistance().set(renderDistance);
				client.options.framerateLimit().set(framerateLimit);
			});
		}
	}

	/** The scene: the CI factory's five rows of machines and four rows of still villains behind them, seeded. */
	private static void build(ServerLevel level, BlockPos origin) {
		List<ServerLoadGameTests.Fed> kinds = ServerLoadGameTests.factoryKinds();
		for (int row = 0; row < kinds.size(); row++) {
			for (int i = 0; i < 16; i++) {
				level.setBlock(origin.offset(2 + i * 2, 0, 2 + row * 3),
						JugcraftMachines.MACHINES.get(kinds.get(row).kind()).defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		List<EntityType<? extends Mob>> villains = ServerLoadGameTests.villains();
		int n = 0;
		for (int kind = 0; kind < villains.size(); kind++) {
			for (int i = 0; i < 6; i++) {
				Mob mob = villains.get(kind).create(level, EntitySpawnReason.COMMAND);
				if (mob == null) {
					throw new AssertionError("Missing villain " + villains.get(kind));
				}
				mob.getRandom().setSeed(SEED + n++);
				mob.snapTo(origin.getX() + 4.5 + i * 6, origin.getY(), origin.getZ() + 20.5 + kind * 4, 180.0F, 0.0F);
				mob.setNoAi(true);
				mob.setPersistenceRequired();
				level.addFreshEntity(mob);
			}
		}
	}

	private static void emit(String line) {
		Jugcraft.LOGGER.info("{}{}", PREFIX, line);
		Path file = Path.of("frame-benchmark.jsonl");
		try {
			Files.writeString(file, line + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			Jugcraft.LOGGER.warn("{}could not append to {}: {}", PREFIX, file.toAbsolutePath(), e.toString());
		}
	}
}
