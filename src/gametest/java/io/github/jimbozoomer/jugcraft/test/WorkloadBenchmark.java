package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.energy.EnergyStorage;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import io.github.jimbozoomer.jugcraft.machine.JugcraftMachines;
import io.github.jimbozoomer.jugcraft.machine.MachineBlockEntity;
import io.github.jimbozoomer.jugcraft.raiders.JugcraftRaiders;
import io.github.jimbozoomer.jugcraft.town.JugcraftTown;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SystemReport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The opt-in workload benchmark (docs/features/server-load-benchmark.md, "Opt-in workload benchmark"), run by
 * ServerLoadGameTests.workloadBenchmark. It measures three representative loads of Jugcraft's own server work over real
 * server ticks, with a baseline of the same empty yard before them:
 * <ul>
 * <li><b>machines</b>: the CI factory ({@link ServerLoadGameTests#factoryKinds}) at {@code scale} times its 80 machines,
 * kept charged and fed;</li>
 * <li><b>crowd</b>: the four costumed villains, {@code 24 x scale}, free to think and move;</li>
 * <li><b>raid</b>: a raid-like fight with the existing raider entities only (grunts, grenadiers, officers, gunners with
 * the Garrison Rifle, an uncrewed walker and a blimp), each hunting one of a line of town guards. The guards stand still
 * (no AI) and are combat-invulnerable as ever, so the fight keeps its size for the whole sample. Raid behaviour itself is
 * unchanged.</li>
 * </ul>
 * Each span is built at fixed positions with fixed random seeds, warmed up, then sampled: each sampled tick's time is the
 * server's own (MinecraftServer's ring of the last 100 tick times) less this driver's own work in that tick. The span
 * records the heap after a requested full collection before it, the heap in use after it, and the heap after a collection
 * once the span is cleared away; collections; and the server thread's allocation where the JVM measures it
 * ({@link BenchmarkProbe}). Every span runs {@code trials} times, so each figure has a repeat beside it. Everything is
 * written to the log as {@value #PREFIX}... lines and appended to {@code workload-benchmark.jsonl} in the server's run
 * directory.
 * <p>
 * Selected only by the system property {@value #PROPERTY} ({@code all}, or any of {@code machines,crowd,raid}); in every
 * other run the test passes at once. It has a test environment of its own, so it never shares a batch, and its yard's
 * chunks are force-loaded while it runs (the game test server's simulation distance is 0, so only forced chunks tick).
 * Time, weather and mob spawning are held for the run and given back at the end.
 */
final class WorkloadBenchmark {
	static final String PROPERTY = "jugcraft.benchmark";
	/** Fabric's id for ServerLoadGameTests.workloadBenchmark, for the filter that makes it the only test. */
	static final String ID = "jugcraft-test:server_load_game_tests_workload_benchmark";
	static final String ENVIRONMENT = "jugcraft-test:workload_benchmark";
	static final String PREFIX = "Workload benchmark | ";
	static final int MAX_TICKS = 40_000;
	/** MinecraftServer keeps the last 100 ticks' times (tickTimesNanos). */
	static final int WINDOW = 100;
	/** The yard is the arena: 48 x 48, a stone floor at y 0 and a ring of barriers three high round its edge. */
	static final int SIZE = 48;
	/** Ticks between a span's clearing away and the collection that reads what it left behind. */
	static final int SETTLE = 40;
	static final long SEED = 0x4A75_6763L;

	enum Workload {
		BASELINE, MACHINES, CROWD, RAID;

		String id() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	/** The run's shape, read from system properties with bounds: warm-up and sampled ticks, trials and scale. */
	record Config(int warmup, int samples, int trials, int scale) {
		static Config read() {
			return new Config(bounded("warmup", 100, 20, 2_000), bounded("samples", 600, WINDOW, 6_000) / WINDOW * WINDOW,
					bounded("trials", 2, 1, 3), bounded("scale", 1, 1, 3));
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
	}

	/** The workloads the property names, in order; empty (the test passes at once) when it is unset. */
	static Set<Workload> selected() {
		String value = System.getProperty(PROPERTY);
		Set<Workload> out = EnumSet.noneOf(Workload.class);
		if (value == null || value.isBlank() || value.trim().equalsIgnoreCase("false")) {
			return out;
		}
		for (String part : value.toLowerCase(Locale.ROOT).split("\\s*,\\s*")) {
			switch (part.trim()) {
				case "all", "true" -> out.addAll(EnumSet.of(Workload.MACHINES, Workload.CROWD, Workload.RAID));
				case "machines", "factory" -> out.add(Workload.MACHINES);
				case "crowd" -> out.add(Workload.CROWD);
				case "raid" -> out.add(Workload.RAID);
				default -> {
					// An unknown name selects nothing; the environment line lists what ran.
				}
			}
		}
		return out;
	}

	private enum Phase {
		WARMUP, SAMPLE, SETTLE
	}

	/** One span: a workload in one trial, and what was measured of it. */
	private static final class Span {
		final Workload workload;
		final int trial;
		final long[] raw;
		final long[] net;
		int start;
		int count;
		long ownNanos;
		long allocatedStart = -1;
		long allocated = -1;
		long retainedBefore;
		long usedAfter;
		long retainedAfterCleanup;
		long collections;
		long collectionMillis;
		int chunks;
		int leftover;
		String state = "";
		BenchmarkStats.Summary summary;

		Span(Workload workload, int trial, int samples) {
			this.workload = workload;
			this.trial = trial;
			this.raw = new long[samples];
			this.net = new long[samples];
		}
	}

	private final GameTestHelper helper;
	private final ServerLevel level;
	private final MinecraftServer server;
	private final Config config = Config.read();
	private final Set<Workload> workloads = selected();
	private final List<Span> plan = new ArrayList<>();
	private final long[] own = new long[WINDOW];
	private final List<ChunkPos> forced = new ArrayList<>();
	private final List<BlockPos> machines = new ArrayList<>();
	private final List<Entity> spawned = new ArrayList<>();
	private final List<Townsfolk> guards = new ArrayList<>();
	private final Map<String, Object> environment = new LinkedHashMap<>();
	private int index = -1;
	private Span current;
	private Phase phase;
	private int phaseTicks;
	private int expected = -1;
	private boolean advanceTime;
	private boolean advanceWeather;
	private boolean spawnMobs;
	private boolean spawnTraders;
	/** Whether {@link #hold} has taken the rules, so {@link #release} gives back what was there. */
	private boolean held;

	WorkloadBenchmark(GameTestHelper helper) {
		this.helper = helper;
		this.level = helper.getLevel();
		this.server = level.getServer();
	}

	void begin() {
		List<String> wrong = BenchmarkStats.check();
		helper.assertTrue(wrong.isEmpty(), "The benchmark arithmetic is right: " + wrong);
		for (int trial = 1; trial <= config.trials(); trial++) {
			plan.add(new Span(Workload.BASELINE, trial, config.samples()));
			for (Workload workload : workloads) {
				plan.add(new Span(workload, trial, config.samples()));
			}
		}
		int ticks = plan.size() * (config.warmup() + config.samples() + SETTLE + 2) + 20;
		helper.assertTrue(ticks < MAX_TICKS, "The benchmark fits its " + MAX_TICKS + " ticks: " + ticks + " needed; ask for fewer samples or trials");
		describe();
		hold();
		force();
		yard();
		next();
		helper.runAfterDelay(1, this::tick);
	}

	// ------------------------------------------------------------------ the driver, once a tick

	private void tick() {
		long began = System.nanoTime();
		int now = server.getTickCount();
		try {
			helper.assertTrue(expected < 0 || now == expected, "The driver ran every tick, so the windows line up: " + now + " after " + (expected - 1));
			expected = now + 1;
			phaseTicks++;
			boolean done = switch (phase) {
				case WARMUP -> {
					if (phaseTicks >= config.warmup()) {
						open(now);
					}
					yield false;
				}
				case SAMPLE -> sample(now);
				case SETTLE -> settle();
			};
			if (done) {
				finish();
				return;
			}
		} catch (RuntimeException | AssertionError e) {
			release();
			throw e;
		}
		own[Math.floorMod(now, WINDOW)] = System.nanoTime() - began;
		helper.runAfterDelay(1, this::tick);
	}

	/** Starts sampling after this tick, which is not sampled: a full collection first, for the heap the span starts from. */
	private void open(int now) {
		current.retainedBefore = BenchmarkProbe.heapRetained();
		current.collections = -BenchmarkProbe.collections();
		current.collectionMillis = -BenchmarkProbe.collectionMillis();
		current.start = now + 1;
		phase = Phase.SAMPLE;
	}

	/** At each window's end, the ring's 100 ticks less this driver's own work in each; true once every span is done. */
	private boolean sample(int now) {
		int since = now - current.start;
		if (since == 0) {
			current.allocatedStart = BenchmarkProbe.allocatedHere();
		}
		if (since <= 0 || since % WINDOW != 0) {
			return false;
		}
		long[] ring = server.getTickTimesNanos();
		for (int tick = now - WINDOW; tick < now; tick++) {
			int slot = Math.floorMod(tick, WINDOW);
			int at = tick - current.start;
			current.raw[at] = ring[slot];
			current.net[at] = Math.max(0L, ring[slot] - own[slot]);
			current.ownNanos += own[slot];
		}
		current.count = since;
		if (current.workload == Workload.MACHINES) {
			// Keep the factory working: charge and feed (counted as the driver's own work, so taken off the ticks).
			feed();
		}
		if (current.count < config.samples()) {
			return false;
		}
		long allocatedEnd = BenchmarkProbe.allocatedHere();
		current.allocated = current.allocatedStart >= 0 && allocatedEnd >= 0 ? allocatedEnd - current.allocatedStart : -1L;
		current.collections += BenchmarkProbe.collections();
		current.collectionMillis += BenchmarkProbe.collectionMillis();
		current.usedAfter = BenchmarkProbe.heapUsed();
		current.chunks = level.getChunkSource().getLoadedChunksCount();
		current.state = state();
		current.summary = BenchmarkStats.of(current.net, BenchmarkStats.TICK_NANOS);
		clear();
		phase = Phase.SETTLE;
		phaseTicks = 0;
		return false;
	}

	/** Once the cleared span has settled: what it left behind, then the next span (true when there is none). */
	private boolean settle() {
		if (phaseTicks < SETTLE) {
			return false;
		}
		current.leftover = clear();
		int after = clear();
		helper.assertTrue(after == 0, "Clearing " + current.workload.id() + " empties the yard: " + after + " left");
		current.retainedAfterCleanup = BenchmarkProbe.heapRetained();
		report(current);
		return !next();
	}

	private boolean next() {
		index++;
		if (index >= plan.size()) {
			return false;
		}
		current = plan.get(index);
		build(current.workload);
		phase = Phase.WARMUP;
		phaseTicks = 0;
		return true;
	}

	// ------------------------------------------------------------------ the workloads

	private void build(Workload workload) {
		switch (workload) {
			case BASELINE -> {
			}
			case MACHINES -> factory();
			case CROWD -> crowd();
			case RAID -> raid();
		}
	}

	/** The CI factory's five rows, {@code scale} times over: 16 machines a row, each charged full and fed a stack. */
	private void factory() {
		List<ServerLoadGameTests.Fed> kinds = ServerLoadGameTests.factoryKinds();
		for (int row = 0; row < kinds.size() * config.scale(); row++) {
			ServerLoadGameTests.Fed fed = kinds.get(row % kinds.size());
			for (int i = 0; i < 16; i++) {
				BlockPos pos = new BlockPos(2 + i * 2, 1, 2 + row * 3);
				helper.setBlock(pos, JugcraftMachines.MACHINES.get(fed.kind()).defaultBlockState());
				machines.add(helper.absolutePos(pos));
			}
		}
		feed();
	}

	/** Every machine charged full and given back a full stack of what it works; a filling output emptied. */
	private void feed() {
		List<ServerLoadGameTests.Fed> kinds = ServerLoadGameTests.factoryKinds();
		for (int n = 0; n < machines.size(); n++) {
			BlockPos pos = machines.get(n);
			ServerLoadGameTests.Fed fed = kinds.get((n / 16) % kinds.size());
			EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, Direction.UP);
			if (storage instanceof SimpleEnergyStorage simple) {
				simple.setAmount(storage.getCapacity());
			}
			if (level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
				if (machine.getItem(0).getCount() < 64) {
					machine.setItem(0, new ItemStack(fed.input(), 64));
				}
				int out = machine.kind().outputSlot();
				if (machine.getItem(out).getCount() > 32) {
					machine.setItem(out, ItemStack.EMPTY);
				}
			}
		}
	}

	/** The four costumed villains, 6 x scale of each, in rows across the yard, free to think and move. */
	private void crowd() {
		List<EntityType<? extends Mob>> villains = ServerLoadGameTests.villains();
		int n = 0;
		for (int kind = 0; kind < villains.size(); kind++) {
			for (int row = 0; row < config.scale(); row++) {
				for (int i = 0; i < 6; i++) {
					Mob mob = helper.spawn(villains.get(kind), new BlockPos(4 + i * 6, 1, 6 + (kind * config.scale() + row) * 3));
					mob.setPersistenceRequired();
					mob.getRandom().setSeed(SEED + n++);
					spawned.add(mob);
				}
			}
		}
	}

	/**
	 * A raid-like fight: per scale, 6 grunts, 4 grenadiers, 2 officers and 4 gunners in a line, an uncrewed walker and a
	 * blimp, each hunting one of 6 still, combat-invulnerable town guards across the yard.
	 */
	private void raid() {
		for (int row = 0; row < config.scale(); row++) {
			for (int i = 0; i < 6; i++) {
				BlockPos at = new BlockPos(4 + i * 7, 1, 40 + row * 2);
				Townsfolk guard = new Townsfolk(JugcraftTown.TOWNSFOLK, level);
				guard.setUp(new TownData.Spot(-1, "guard", null, "Benchmark guard " + (guards.size() + 1), "guard_blue", BlockPos.ZERO),
						helper.absolutePos(at));
				guard.setNoAi(true);
				guard.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(at)));
				level.addFreshEntity(guard);
				guards.add(guard);
				spawned.add(guard);
			}
		}
		List<EntityType<? extends Mob>> infantry = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			infantry.add(JugcraftRaiders.GRUNT);
		}
		for (int i = 0; i < 4; i++) {
			infantry.add(JugcraftRaiders.GRENADIER);
		}
		for (int i = 0; i < 2; i++) {
			infantry.add(JugcraftRaiders.OFFICER);
		}
		for (int i = 0; i < 4; i++) {
			infantry.add(JugcraftRaiders.GUNNER);
		}
		var rifle = JugcraftGuns.GUNS.get("garrison_rifle");
		int n = 0;
		for (int row = 0; row < config.scale(); row++) {
			for (int i = 0; i < infantry.size(); i++) {
				Mob raider = helper.spawn(infantry.get(i), new BlockPos(4 + i * 2, 1, 4 + row * 3));
				if (infantry.get(i) == JugcraftRaiders.GUNNER && rifle != null) {
					// The same gun every run: a gunner otherwise draws its gun at random when it is made.
					raider.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(rifle));
				}
				enlist(raider, n++);
			}
			enlist(helper.spawn(JugcraftRaiders.WALKER, new BlockPos(40, 1, 6 + row * 12)), n++);
			enlist(helper.spawn(JugcraftRaiders.BLIMP, new BlockPos(24, 12, 6 + row * 12)), n++);
		}
	}

	private void enlist(Mob raider, int n) {
		raider.setPersistenceRequired();
		raider.getRandom().setSeed(SEED + 1_000 + n);
		raider.setTarget(guards.get(n % guards.size()));
		spawned.add(raider);
	}

	/** What the span ended with, the same in every trial when the work is deterministic. */
	private String state() {
		return switch (current.workload) {
			case BASELINE -> "empty yard";
			case MACHINES -> {
				int busy = 0;
				long progress = 0;
				for (BlockPos pos : machines) {
					if (level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
						progress += machine.processingProgress();
						if (machine.processingProgress() > 0 || !machine.getItem(machine.kind().outputSlot()).isEmpty()) {
							busy++;
						}
					}
				}
				helper.assertTrue(busy > 0, "Some of the factory's machines are working or done");
				yield machines.size() + " machines, " + busy + " working or done, progress " + progress;
			}
			case CROWD -> {
				int alive = (int) spawned.stream().filter(Entity::isAlive).count();
				helper.assertTrue(alive > 0, "The crowd is still there");
				yield alive + " of " + spawned.size() + " villains alive";
			}
			case RAID -> {
				int raiders = 0;
				int hunting = 0;
				for (Entity entity : spawned) {
					if (entity instanceof Mob mob && !(entity instanceof Townsfolk) && mob.isAlive()) {
						raiders++;
						if (mob.getTarget() != null) {
							hunting++;
						}
					}
				}
				long standing = guards.stream().filter(Entity::isAlive).count();
				helper.assertTrue(raiders > 0, "The raiders are still there");
				yield raiders + " raiders alive, " + hunting + " with a target, " + standing + " of " + guards.size() + " guards standing";
			}
		};
	}

	/** Takes every machine and every entity but players out of the yard (and above it, for the blimp); how many it found. */
	private int clear() {
		int found = 0;
		for (BlockPos pos : machines) {
			if (!level.getBlockState(pos).isAir()) {
				level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
				found++;
			}
		}
		machines.clear();
		for (Entity entity : level.getEntitiesOfClass(Entity.class, area(), entity -> !(entity instanceof Player))) {
			entity.discard();
			found++;
		}
		spawned.clear();
		guards.clear();
		return found;
	}

	private AABB area() {
		BlockPos low = helper.absolutePos(new BlockPos(-8, -2, -8));
		BlockPos high = helper.absolutePos(new BlockPos(SIZE + 8, 48, SIZE + 8));
		// AABB orders each axis's two values itself.
		return new AABB(Vec3.atLowerCornerOf(low), Vec3.atLowerCornerOf(high));
	}

	// ------------------------------------------------------------------ the yard and the world held still

	private void yard() {
		for (int x = 0; x < SIZE; x++) {
			for (int z = 0; z < SIZE; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
				if (x == 0 || z == 0 || x == SIZE - 1 || z == SIZE - 1) {
					for (int y = 1; y <= 3; y++) {
						helper.setBlock(new BlockPos(x, y, z), Blocks.BARRIER);
					}
				}
			}
		}
	}

	/** Noon held, weather held and no mobs spawning, so every span sees the same world; given back by {@link #release}. */
	private void hold() {
		GameRules rules = level.getGameRules();
		advanceTime = rules.get(GameRules.ADVANCE_TIME);
		advanceWeather = rules.get(GameRules.ADVANCE_WEATHER);
		spawnMobs = rules.get(GameRules.SPAWN_MOBS);
		spawnTraders = rules.get(GameRules.SPAWN_WANDERING_TRADERS);
		rules.set(GameRules.ADVANCE_TIME, false, server);
		rules.set(GameRules.ADVANCE_WEATHER, false, server);
		rules.set(GameRules.SPAWN_MOBS, false, server);
		rules.set(GameRules.SPAWN_WANDERING_TRADERS, false, server);
		held = true;
		helper.setTime(6000L);
	}

	/** Every chunk under the yard ticks, as it would round a player. */
	private void force() {
		BlockPos low = helper.absolutePos(new BlockPos(0, 0, 0));
		BlockPos high = helper.absolutePos(new BlockPos(SIZE - 1, 0, SIZE - 1));
		for (int cx = Math.min(low.getX(), high.getX()) >> 4; cx <= Math.max(low.getX(), high.getX()) >> 4; cx++) {
			for (int cz = Math.min(low.getZ(), high.getZ()) >> 4; cz <= Math.max(low.getZ(), high.getZ()) >> 4; cz++) {
				ChunkPos chunk = new ChunkPos(cx, cz);
				if (!level.getChunkSource().getForceLoadedChunks().contains(chunk.pack())) {
					level.setChunkForced(cx, cz, true);
					forced.add(chunk);
				}
			}
		}
	}

	/** Gives back the rules and the forced chunks, and clears the yard; safe to call twice. */
	private void release() {
		clear();
		GameRules rules = level.getGameRules();
		if (held) {
			held = false;
			rules.set(GameRules.ADVANCE_TIME, advanceTime, server);
			rules.set(GameRules.ADVANCE_WEATHER, advanceWeather, server);
			rules.set(GameRules.SPAWN_MOBS, spawnMobs, server);
			rules.set(GameRules.SPAWN_WANDERING_TRADERS, spawnTraders, server);
		}
		for (ChunkPos chunk : forced) {
			level.setChunkForced(chunk.x(), chunk.z(), false);
		}
		forced.clear();
	}

	private void finish() {
		release();
		summarise();
		helper.succeed();
	}

	// ------------------------------------------------------------------ the record

	private void describe() {
		environment.put("record", "environment");
		environment.putAll(BenchmarkProbe.environment());
		String report = new SystemReport().toLineSeparatedString();
		for (String key : List.of("Minecraft Version", "Processor Name", "Number of logical CPUs")) {
			report.lines().filter(line -> line.startsWith(key + ": ")).findFirst()
					.ifPresent(line -> environment.put(key, line.substring(key.length() + 2).trim()));
		}
		environment.put("jugcraft", FabricLoader.getInstance().getModContainer("jugcraft")
				.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?"));
		environment.put("workloads", workloads.stream().map(Workload::id).toList());
		environment.put("warmupTicks", config.warmup());
		environment.put("sampleTicks", config.samples());
		environment.put("trials", config.trials());
		environment.put("scale", config.scale());
		environment.put("seed", SEED);
		environment.put("tick", "server tick ms from MinecraftServer's tick-time ring, less the benchmark driver's own work");
		emit(BenchmarkStats.json(environment));
	}

	private void report(Span span) {
		BenchmarkStats.Summary s = span.summary;
		Span base = baseline(span.trial);
		Map<String, Object> out = new LinkedHashMap<>();
		out.put("record", "span");
		out.put("workload", span.workload.id());
		out.put("trial", span.trial);
		out.put("ticks", s.count());
		out.put("meanMs", BenchmarkStats.round(s.meanMs()));
		out.put("p50Ms", BenchmarkStats.round(s.p50Ms()));
		out.put("p95Ms", BenchmarkStats.round(s.p95Ms()));
		out.put("maxMs", BenchmarkStats.round(s.maxMs()));
		out.put("over50ms", s.over());
		out.put("rawMeanMs", BenchmarkStats.round(BenchmarkStats.of(span.raw, BenchmarkStats.TICK_NANOS).meanMs()));
		out.put("driverMsPerTick", BenchmarkStats.round(BenchmarkStats.ms((double) span.ownNanos / Math.max(1, s.count()))));
		boolean compared = base != null && base != span && base.summary != null;
		out.put("deltaMeanMs", compared ? BenchmarkStats.round(s.meanMs() - base.summary.meanMs()) : null);
		out.put("deltaP95Ms", compared ? BenchmarkStats.round(s.p95Ms() - base.summary.p95Ms()) : null);
		out.put("allocatedBytesPerTick", span.allocated >= 0 ? span.allocated / Math.max(1, s.count()) : null);
		out.put("deltaAllocatedBytesPerTick", compared && span.allocated >= 0 && base.allocated >= 0
				? span.allocated / Math.max(1, s.count()) - base.allocated / Math.max(1, base.summary.count()) : null);
		out.put("heapRetainedBeforeMiB", span.retainedBefore >= 0 ? BenchmarkStats.round(BenchmarkStats.mib(span.retainedBefore)) : null);
		out.put("heapUsedAfterMiB", BenchmarkStats.round(BenchmarkStats.mib(span.usedAfter)));
		out.put("heapRetainedAfterCleanupMiB", span.retainedAfterCleanup >= 0
				? BenchmarkStats.round(BenchmarkStats.mib(span.retainedAfterCleanup)) : null);
		out.put("collections", span.collections);
		out.put("collectionMs", span.collectionMillis);
		out.put("loadedChunks", span.chunks);
		out.put("clearedAtCleanup", span.leftover);
		out.put("state", span.state);
		emit(BenchmarkStats.json(out));
	}

	private Span baseline(int trial) {
		for (Span span : plan) {
			if (span.workload == Workload.BASELINE && span.trial == trial) {
				return span;
			}
		}
		return null;
	}

	/** Each workload's trials beside each other: their means, p95s, how far apart, and whether they ended the same. */
	private void summarise() {
		Map<Workload, List<Span>> by = new EnumMap<>(Workload.class);
		for (Span span : plan) {
			by.computeIfAbsent(span.workload, w -> new ArrayList<>()).add(span);
		}
		for (Map.Entry<Workload, List<Span>> entry : by.entrySet()) {
			List<Span> spans = entry.getValue();
			double low = Double.MAX_VALUE;
			double high = 0;
			List<Double> means = new ArrayList<>();
			List<Double> p95s = new ArrayList<>();
			boolean same = true;
			for (Span span : spans) {
				means.add(BenchmarkStats.round(span.summary.meanMs()));
				p95s.add(BenchmarkStats.round(span.summary.p95Ms()));
				low = Math.min(low, span.summary.meanMs());
				high = Math.max(high, span.summary.meanMs());
				same &= span.state.equals(spans.get(0).state);
			}
			Map<String, Object> out = new LinkedHashMap<>();
			out.put("record", "repeatability");
			out.put("workload", entry.getKey().id());
			out.put("trials", spans.size());
			out.put("meanMs", means);
			out.put("p95Ms", p95s);
			out.put("meansApart", spans.size() > 1 ? BenchmarkStats.round(BenchmarkStats.apart(low, high)) : null);
			out.put("sameEndState", spans.size() > 1 ? same : null);
			emit(BenchmarkStats.json(out));
		}
	}

	private void emit(String line) {
		Jugcraft.LOGGER.info("{}{}", PREFIX, line);
		Path file = Path.of("workload-benchmark.jsonl");
		try {
			Files.writeString(file, line + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			Jugcraft.LOGGER.warn("{}could not append to {}: {}", PREFIX, file.toAbsolutePath(), e.toString());
		}
	}
}
