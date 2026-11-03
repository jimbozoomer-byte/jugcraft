# Two-bay charging station and module menu

Owner-directed improvement, November 2, 2026. Implemented with OpenAI Codex (GPT-6). Extends the [powered equipment balance](powered-equipment-rebalance.md), using the existing charging station block, recipes, JE storage, module rules and electric machine screen skin. No platform/dependency or live-server changes.

## Player behavior

Right-click either half to open a native inventory menu, including when holding equipment or a module. Two equipment bays each show the item, charge, installed upgrade levels and a row per module kind. Shift-click moves eligible equipment between the bays and player inventory. Closing the menu leaves equipment in the station.

Each module row has + and - controls. + consumes one matching module from the cursor first or player inventory. - recovers one fitted module to the cursor or available inventory space; a matching spare module is unnecessary. Recovering from one bay to the cursor and fitting it into the other is supported. Tooltips identify the module and bay and explain recovery restrictions. Missing stock, incompatible modules, upgrade limits and conflicts disable insertion; unsafe capacity removal and untracked enchantments disable recovery. Server feedback explains a rejected request if state changes while the screen is open.

Capacity removal requires the existing charge to fit the reduced capacity. Silk Touch and Fortune remain mutually exclusive. Existing ordinary enchantments are visible but cannot be manufactured into modules; only tracked fitted levels can be recovered. Failed operations consume neither equipment, modules nor energy. If the cursor and inventory cannot accept a recovered module, the installed module stays in place.

The two bays fairly share the existing **512 JE/t total**. A single item, or one with a full/empty neighbor, gets the full rate. With two accepting items, each normally gets 256 JE/t. One-JE remainder priority alternates each tick. The station buffer holds 100,000 JE; cable input remains limited to 1,024 JE/t. Stored charge plus transferred charge is conserved. Both world display items and the charging lamps are synchronized.

## Persistence and authority

The saved container uses the original slot-zero format, expanded to two slots. Old one-item saves retain their item, name, energy, modules and station buffer; slot one starts empty. New saves retain both full item stacks. Before returning to an older one-bay build, empty both bays or restore a backup; older code does not read slot one. Breaking either half drops both items once. The station's acquisition recipe and dependency path remain unchanged: existing steel/electrical workshop inputs feed equipment used in mining, farming and exploration.

The native menu synchronizes item components and station energy. Twenty-four bounded native menu button IDs address two bays, six module kinds and two operations; no new custom packet or client-claimed item state is used. Every menu mutation rechecks the live container, player location, build permissions and protection on both halves. Spectators, distant players, invalid IDs, revoked claims and stale menus cannot change equipment. Concurrent viewers operate on current server inventory. Insertion/removal is atomic on the server thread.

The screen uses the existing native container, ScreenKit and electric machine theme; no new art or interface dependency was introduced. The complete panel fits 320x240 GUI units and preserves standard inventory navigation and item tooltips. Charge text is abbreviated on the cards; item tooltips retain the exact JE balance.

## Initial two-bay verification (50,000 JE)

Verified November 2, 2026 with Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3 and JDK 25.0.4.1+1, on the existing powered-equipment working branch based on `235524e0c8`. Test worlds were disposable generated worlds. The new server suite covers opening both halves with held items, fair charging and buffer conservation, cursor/inventory module fitting, removal without a spare module, overfull capacity, full inventories, enchantment provenance, old/new save round trips, both-half drops, permissions/distance, invalid requests and simultaneous viewers. The rendered client test opens the upper half through normal item use, inserts both tools using real inventory clicks, sends the actual + / - widget packets, checks charge/module conservation, closes/reopens and captures the minimum-size layout.

Local Gradle uses the previously prepared main resource output with the two changed language/handbook files copied and byte-verified, and a local init script that excludes resource directories from compiler classpaths. It does not alter runtime behavior or assertions. The full server run had no test/feature filter. The packaging command reuses that successful full test run and omits the optional source archive.

- Full server suite: **3,320/3,320 required tests passed**, including the eleven new station tests and the existing powered-equipment tests. `BUILD SUCCESSFUL in 8m 59s`.
- Rendered client: `ChargingStationClientGameTests` **passed**, `BUILD SUCCESSFUL in 4m 21s`. Both item transfers, real module-button packets, exact JE/module conservation, closing/reopening and the 320x240 GUI bounds passed. Both clear screenshots were reviewed. The fixture waits for synchronized controls before pressing them and clears advancement toasts before photographs; no runtime assertion was removed.
- Repository structure/documentation links: passed.
- Client selection/sharding unit tests: 12 passed.
- Language and handbook generator consistency, and copied test-resource byte comparisons: passed.
- `git diff --check`: passed.
- Final playable JAR build: **passed**, `BUILD SUCCESSFUL in 13m 34s`, including 79 offline updater and 393 scheduling assertions. Final archive integrity passed across 189,066 ZIP entries. Relevant runtime classes match the compiled bytes, the four edited runtime resources match source, embedded libraries are present and test classes are excluded. Playable JAR: 344,294,600 bytes; SHA-256 `b7c57e407bf585693846c1eba4e9f156f531e8f82a1b0588fb30ce6b5e77297d`.

```powershell
.\gradlew.bat runGameTest -I ..\ci600-compile.gradle -x processResources --offline --no-daemon --no-parallel --max-workers=2
.\gradlew.bat runClientGameTest -PclientTests=ChargingStationClientGameTests -I ..\ci600-compile.gradle -x processResources --offline --no-daemon --no-parallel --max-workers=2
.\gradlew.bat build -I ..\ci600-compile.gradle -x processResources -x runGameTest -x sourcesJar --offline --no-daemon --no-parallel --max-workers=2
```

No human two-client or live-world playtest is claimed. The live installation was not changed.


## 100,000 JE buffer follow-up

The owner increased the buffer to 100,000 JE. Charging remains 512 JE/t total with 1,024 JE/t cable input. Existing saved charge is preserved without adding energy. The menu now synchronizes two signed 16-bit words so balances above 65,535 JE display correctly; the screen reads its maximum from the shared capacity constant. The handbook is updated.

- Charging-station server regressions: **12/12 passed**, `BUILD SUCCESSFUL in 4m 41s`, including preserving a saved 100,000 JE buffer and signed-word synchronization at 50,000, 65,535, 65,536, 99,999 and 100,000 JE.
- Real client menu: **passed**, `BUILD SUCCESSFUL in 3m 28s`. The client received exactly 100,000 JE before equipment insertion, then passed the existing charging, module and conservation checks with the larger buffer. The full-buffer screenshot was reviewed at 320x240 GUI units.
- Handbook generator/runtime-copy consistency and whitespace checks: passed.
- Updated playable JAR: **passed**, `BUILD SUCCESSFUL in 11m 42s`, including 79 offline updater and 393 scheduling assertions. Final archive integrity passed across 189,066 ZIP entries; relevant compiled classes and packaged resources match the final outputs. Artifact: 344,294,643 bytes; SHA-256 `dc87c2b25a009c48cc57ed4edf52724ff624cf16fa72ee4ce38d10cc22e66ad4`.

The focused server command adds `-I ..\station-capacity-focused.gradle`, which selects only `jugcraft-test:charging_station_game_tests_*`; other commands use the same local resource/compiler setup listed above. The earlier complete-suite result and artifact describe the original 50,000 JE implementation, not a new complete-suite run for this adjustment.

Update client and server to the same build because the menu synchronization layout changed. Existing saves retain their current charge when upgrading. Before reverting to a 50,000 JE build, drain the buffer to at most 50,000 JE or restore a backup.


The [Efficiency Module](tool-efficiency-module.md) adds a sixth row to each bay. The compact row spacing and footer placement preserve the original menu dimensions.
