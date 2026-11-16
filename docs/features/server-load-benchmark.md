# Server load benchmark

Status: implemented (pending CI and review). It measures, it does not change the game.
Proposal issue: none. The TODO asks to "Benchmark server tick time, memory and client frame times under representative player load". On 26 October 2026 the owner asked for the suggested items in order ("then do 2-9 in order"); the eighth was "server load benchmark". This is the server tick part.

Owner: jimbozoomer-byte
Target milestone and tier: project health, no tier
Primary specialty and supported player role: not applicable (hosting)

## What it measures

A server game test, `ServerLoadGameTests.measuredServerLoad`, builds a representative load in the empty 48 × 48 `tatterlace_arena` test area and times what a tick of it costs on the server:

| Load | What it is | How it is timed |
| --- | --- | --- |
| A factory | 16 each of five working processing machines (electric furnaces, crushers, pulverizers, sawmills and sieves), 80 in all, each charged full and fed a stack of something it works | each machine's own block entity ticker, as the level calls it |
| A crowd | 6 each of the four costumed villains, 24 in all, free to think and move | each mob's own tick |

Each load is warmed up for 20 rounds, then timed with `System.nanoTime` over 100 rounds. A round is one tick of the whole load, on top of the ticks the server gives it anyway. The test logs one line:

```
Server load: factory of 80 machines <us> us a tick (<us> us a machine, <n> of them working or done), crowd of 24 costumed villains <us> us a tick (<us> us a mob), over 100 timed rounds after 20 warm-up rounds
```

It asserts that each load's round stays within a whole server tick (50 ms), and that some machines are working. The bound is there to catch a change that makes the work grow many times over, not to time the runner.

This follows the Concordance's workload test ([arcane-concordance-delivery.md](arcane-concordance-delivery.md)): the work is timed directly, so the other tests the server runs at the same time count less than they would in the server's own tick times.

## Opt-in workload benchmark

`ServerLoadGameTests.workloadBenchmark` (12 November 2026, Claude Opus 5.5, written, not run) measures more than the CI line above, and only when asked: it passes at once in every ordinary run, and it has a test environment of its own (`jugcraft-test:workload_benchmark`), so it never shares a batch with other tests. Its driver is `WorkloadBenchmark`; the arithmetic is `BenchmarkStats` and the JVM readings `BenchmarkProbe` (neither names a Minecraft type).

Run it alone:

```sh
./gradlew runGameTest -PjugcraftBenchmark=all
# or any of machines,crowd,raid; and optionally
#   -PjugcraftBenchmarkWarmup=100 -PjugcraftBenchmarkSamples=600 -PjugcraftBenchmarkTrials=2 -PjugcraftBenchmarkScale=1
```

The Gradle property passes `-Djugcraft.benchmark=...` and Fabric's filter (`jugcraft-test:server_load_game_tests_workload_benchmark`) to the test server. Without Gradle's help, set both in `JAVA_TOOL_OPTIONS`, as for the Concordance load cards ([TESTING.md](../TESTING.md)).

| Span | What it is |
| --- | --- |
| `baseline` | the same 48 × 48 stone yard, ringed with barriers three high, empty |
| `machines` | the factory above, `scale` times over (80, 160 or 240 machines), recharged and refed at each 100-tick window by the driver, whose work is taken off the ticks |
| `crowd` | the four costumed villains, 6 × `scale` of each, free to think and move |
| `raid` | per `scale`: 6 grunts, 4 grenadiers, 2 officers, 4 gunners (each given the Garrison Rifle, so no gun is drawn at random), an uncrewed walker and a blimp, each set on one of 6 town guards across the yard. The guards have no AI and are combat-invulnerable as always, so the fight keeps its size; raid behaviour itself is not changed |

Every span is built at fixed positions, each mob's random seeded from `0x4A756763` and its place in the list, warmed up (default 100 ticks) and then sampled (default 600 ticks, in 100-tick windows). Each sampled tick is the server's own tick time from `MinecraftServer`'s ring of the last 100, less the driver's own work in that tick, as the Concordance load cards read it. The yard's chunks are force-loaded (the game test server's simulation distance is 0); time, weather and mob spawning are held and given back afterwards. After sampling, the span is cleared away (machines removed, every entity but players in and above the yard discarded), and after 40 ticks a second sweep must find the yard empty. All of it runs `trials` times (default 2), each trial with its own baseline.

Each line in the log begins `Workload benchmark | ` and is one JSON object; the same lines are appended to `workload-benchmark.jsonl` in the test server's run directory (`build/run/gameTest`):

- `{"record":"environment", ...}`: Java runtime and VM, `heapMaxMiB`, `cores`, OS, collectors, the memory and collector flags (paths left out), how allocation is measured (or `unavailable: <why>`), the Minecraft version and processor from the system report, the Jugcraft version, the workloads, warm-up and sample ticks, trials, scale and seed.
- `{"record":"span","workload":...,"trial":...}`: `ticks`, `meanMs`, `p50Ms`, `p95Ms` (nearest rank), `maxMs`, `over50ms`, `rawMeanMs` (before the driver's work is taken off), `driverMsPerTick`, `deltaMeanMs` and `deltaP95Ms` against the same trial's baseline, `allocatedBytesPerTick` and `deltaAllocatedBytesPerTick` (the server thread's, from `com.sun.management.ThreadMXBean.getThreadAllocatedBytes`; `null` when the JVM cannot measure it), `heapRetainedBeforeMiB` (after a requested full collection), `heapUsedAfterMiB` (no collection), `heapRetainedAfterCleanupMiB` (after clearing, settling and a collection; `null` when the JVM ignores `System.gc()`), `collections`, `collectionMs`, `loadedChunks`, `clearedAtCleanup` and `state` (what the span ended with: machines working, mobs alive, raiders hunting).
- `{"record":"repeatability","workload":...}`: each trial's mean and p95, `meansApart` (the difference of the lowest and highest mean as a share of the highest) and `sameEndState`.

It asserts only that the measurement is real: the arithmetic check, a driver call every tick, machines working, mobs and raiders still there, and an empty yard after clearing. Timing is reported, never asserted. Off-thread work (chunk generation, lighting, saving) is outside the tick times and the server thread's allocation.

`ServerLoadGameTests.benchmarkArithmetic` checks the statistics and the JSON writer in every ordinary run.

## Client frame benchmark

`FrameBenchmarkClientGameTests` (12 November 2026, Claude Opus 5.5, written, not run) is a client game test that measures frame times only when asked. It needs one line added to the test mod's `fabric-client-gametest` list before it can run. When the property is unset it returns at once, without opening a world.

```sh
./gradlew runClientGameTest -PclientTests=FrameBenchmarkClientGameTests -PjugcraftFrameBenchmark=true
# optionally -PjugcraftFrameBenchmarkWarmup=100 -PjugcraftFrameBenchmarkSamples=600 -PjugcraftFrameBenchmarkTrials=2
```

- **Scene:** the factory's 80 machines in five rows and the 24 villains in four rows, standing still so that every frame shows the same thing. They stand on a 48 × 48 stone floor at noon in clear weather, with time, weather and spawning held. The camera stands above the near end, looking down the rows at 30°.
- **Settings:** render distance 8 and an unlimited frame rate while it runs. The previous settings are given back afterwards. One screenshot (`jugcraft_frame_benchmark_scene`) shows what was drawn.
- **Samples:** Fabric's client test framework runs the game in lockstep, one client tick and one frame at a time, with the integrated server's tick alongside. A sample is therefore the wall time of one `waitTick`. The warm-up is 100 frames by default, and each trial samples 600 frames.
- **Output:** lines begin `Frame benchmark | ` and are also appended to `frame-benchmark.jsonl` in the client's run directory. The `environment` record adds the window size, render distance and frame-rate limit to `BenchmarkProbe`'s. Each `frames` record gives `meanMs`, `p50Ms`, `p95Ms`, `maxMs`, `over50ms`, `framesPerSecondFromMean`, `renderThreadAllocatedBytesPerFrame`, the heap before and after, and collections. A `repeatability` record compares the trials.

These figures come from the machine that runs the test. CI's clients draw with Mesa's software renderer, so their figures are not a player's. A player's frame times still need a run on real graphics hardware.

## Full-suite memory

The [TODO](../TODO.md) records that the complete local server sweep needed a 6 GB heap and that the default 4 GB run ran out in the large nuclear fixtures. PRs 607 and 608 log out the mocks made by vanilla's helper and `TestPlayers`, and close their fake transports when each test ends. Reading the code on 12 November 2026 (Claude Opus 5.5) found three causes they did not reach, and this change fixes them. It is written, not run: whether the unmodified 4 GB suite now passes is for a Java 25 run to show.

1. **Fixtures that joined players themselves.** Ten helpers built a `ServerPlayer` and called `PlayerList.placeNewPlayer` directly, bypassing `TestPlayers`: `JimtaRobotGameTests.player` (about 70 Jimta tests), `ConnectedAlchemyGameTests.player` and `AlchemyPlayabilityGameTests.fresh` (about 47), `AlchemyArtifactGameTests.player` (20), `WildenStalkerGameTests.player` (11), `ServerDefenseGameTests.player` (9), `MenagerieGameTests.survivalPlayer` (9), and those in `TownGameTests`, `LivingCityCommissionGameTests` and `ClaimCombatGameTests`. Most of their players stayed in the game for the rest of the run, with an advancement tracker each, an inventory and chunk tickets round their test, and every broadcast sent to them. Those logged out by hand kept an open fake transport, so Fabric never ended their network session: its receiver registry, and the per-player maps that Jugcraft clears on disconnect, kept them. This fits the 184 plain `ServerPlayer`s that PR 608's heap histogram still found. Each helper now hands its player to `TestPlayers.track` right after joining, so the player leaves, and its transport closes, when its own test ends. `TestPlayersGameTests` checks two of the shared helpers, and a hand-removed player. `scripts/tests/test_game_test_isolation.py` fails if any server test joins a player without tracking it; the two opt-in load cards are listed as exceptions.
2. **Fake players for absent identities.** A `ServerPlayer` built for an identity the player list has no tracker for adds an advancement tracker and a statistics counter. They go only when that identity leaves the game, and a fake player never joins. Each stand-in for an absent owner (`concordance.Authority`) and each fake with a fresh UUID (the Exchange, Cinder Tyrant, Yeti King and deposit tests) therefore kept a tracker for the rest of the run. Every tracker holds a progress record for every advancement, listeners on every criterion trigger, its fake player and that player's level. Every 200 ticks, `MockPlayerChannels` now stops and forgets the trackers and counters of fake players whose identity is not online. This happens in tests only; on a real server the stand-ins are limited to the owners there are.
3. **Six whole-plant builds in one batch.** All the tests in one test environment share batches. The six `NuclearPlantGameTests` that build whole plants sat in the default batch: the small plant, the medium and large plants together, and the medium plant four more times. They built about 2.2 million blocks in the same tick, with their chunks, block entities and lighting, on top of the other tests in the batch. Each now has an empty test environment of its own (`jugcraft-test:nuclear_plant_*`), as the season and truce tests have, so it runs alone. The largest single peak is now the medium and large plants of the shielding test (724,889 blocks). No assertion, plant or tick limit changed.

The `[mock-player-channels]` line, written every 2,400 ticks, now also gives the players online, the player list's advancement trackers and statistics counters, the absent fake players released so far and the overworld's loaded chunks. Over a healthy run the trackers stay close to the players online, and the chunks do not keep climbing.

Local switches for `runGameTest`, all off in CI: `-PjugcraftGameTestHeap=6G` sets a local budget in place of the 4 GB (a local run that needs it is not an unmodified CI pass), `-PjugcraftHeapDump=<dir>` writes a heap dump if the heap runs out, and `-PjugcraftGcLog=gc.log` logs every collection to `build/run/gameTest/gc.log`.

## Results

CI's numbers go here once the pull request's `mod` job has run; see the PR. They are a CI runner's, shared with every other test running at once: an indication, not a profile.

## What it does not measure

- **Memory:** not in the CI line. A heap figure taken inside a test server running hundreds of other tests would not say what Jugcraft holds. The opt-in workload benchmark above measures it in a server of its own.
- **Client frame times:** not in CI. The client game tests draw with software rendering (Mesa), so their frame times would not stand for a player's. The opt-in client frame benchmark above is for a run on real graphics hardware.
- **Players:** no real players. A hosted multiplayer test with real clients is still needed to set a player limit ([TODO](../TODO.md)).
- **The world's own ticking:** chunk loading, lighting, vanilla mobs and redstone are not in these numbers.

## Connections, balance and persistence

Not applicable: it adds one server game test and changes no game code, data or saves.

## Verification

- Local, without Gradle or a game: the Java syntax parse and the repository checks; see the PR.
- Full-suite memory (12 November 2026): written, not run. `python3 -m unittest scripts/tests/test_game_test_isolation.py` passes here and fails against the previous revision's helpers. The three new `TestPlayersGameTests`, the sweep and the batch change still need a Java 25 build, then the full server suite in its default 4 GB with and without the optional integrations, before anyone claims an unmodified CI pass.
- Client frame benchmark (12 November 2026): written, not run, and not yet registered in the test mod (a coordinator change).
- Opt-in workload benchmark (12 November 2026): written, not run. Only its two Minecraft-free classes were compiled and checked, with Java 21, outside Gradle; the game test, the Gradle property and every figure await a Java 25 run.
- CI: the `mod` job runs it with the rest of the server game tests.
- Not run: a dedicated server with real players.

## Rollout and open questions

- If the owner wants more of the mod in the numbers, the same test can take more loads: drones flying a depot, item pipes and conveyors, trains, town folk.
- Client frame times need a hardware-rendered client run of the frame benchmark, outside CI.
