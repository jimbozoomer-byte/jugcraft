# Powered equipment rebalance and ore scanners

Owner-directed scope, November 2, 2026. Implemented with OpenAI Codex (GPT-6). No live server deployment or restart is authorized by this change.

## Progression and energy

Powered handheld tools, Power Katana and Power Bow start at 250,000 JE. Each Vanguard/Ronin exosuit piece starts at 500,000 JE. Capacity modules double storage twice (1x, 2x, 4x). Batteries, ammunition cells, other armor families and standalone jetpacks retain their own base capacities. Modules add storage, never charge. The existing shapeless Advanced Scanner, Diamond Drill and Iridium Drill recipes preserve their input tool components, including names, charge, modules and saved surveys. Existing above-cap saved energy can only be spent down; charging refuses it until it fits again. Energy bars clamp visually without truncating the saved balance.

Overclock adds 50% base mining speed and 50% base JE/block per level. The drill costs 60/90/120 JE per block; the chainsaw 40/60/80. Two levels remain the maximum. Open the [two-bay charging station](charging-station-menu.md) and use its + / - controls to fit or recover a module; discharge before lowering capacity. Newly fitted enchantment modules record provenance so removal cannot manufacture modules from ordinary loot enchantments; old enchantments remain intact.

Industrial, Diamond and Iridium drill variants have mining speeds 8, 16 and 20 respectively, giving their recipes a purpose with equal battery sizes. The native Mining Drill retains speed 14.

## Exosuit

No leggings or boots standby drain. Leggings provide +30% movement speed and spend 1 JE/t while input produces actual movement; blocked input, riding, swimming, gliding, flying and knockback do not incur that charge. Boots retain free full-block stepping while charged. Fall protection costs 100 JE/heart prevented (50 JE/damage point), once through the post-enchantment damage path; insufficient charge gives proportional mitigation. Other fall immunity prevents this hook from billing a harmless landing.

Helmet vision spends 2 JE/t while it supplies vision in darkness. Shift-use the held helmet to toggle. Other night vision is neither charged nor intentionally removed. The chestplate regenerates at most one shield point each ten ticks, for 1,000 JE/point, after three seconds without damage. Maximum shield is eight points/four hearts. Paid shield remainder is stored on the chestplate, removed from the player when unequipped, and restored when re-equipped without free regeneration. JE jetpacks consume 20 JE/t of thrust in gentle, cruise and full settings. Shift-use the held native pack/exosuit chestplate to cycle strength; industrial packs use Sprint+Sneak+use to preserve their existing ability toggle. A warning appears below 10% charge. Basic armor remains unbreakable and provides its existing protection.

## Tools and controls

Drill Shift-use retains the existing mode control. With Range: single, 3x3, 5x5, vein. Without Range: single, 3x3, vein. Sneaking while mining temporarily suppresses bulk breaking. Vein limits include the first block: 32 normally, 64 with Range. Bulk mining skips machines/containers, protected blocks and harder neighboring materials. The server remains authoritative for every break.

The [face-based drill correction](drill-face-mining.md) orients both area sizes and their previews to the face where mining starts, including horizontal floors/ceilings at shallow viewing angles.

Chainsaw Shift-use cycles tree felling, single log and leaf clearing. Sneak for a single block. Felling follows the same log type and stops at 128 total logs. Leaf clearing is bounded to 64 connected leaves and costs 10/15/20 JE per block. Cosmetic client previews use the existing blueprint outline geometry and cache at most 64 prospective bulk targets every ten ticks; permissions are rechecked when breaking.

The [chainsaw tree detector](chainsaw-tree-detection.md) checks the complete rooted trunk and natural canopy before felling; buildings and ambiguous timber fall back to one block, and the preview follows the same detector.

Electric Hoe Shift-use toggles single/3x3 tilling and pays 50 JE for each successful change. The leaf blower has gentle/medium/full strength; hold Sprint+Sneak and use to cycle it. Its default full-strength behavior and 4/6 JE/t blow/vacuum costs remain; lower strengths reduce range, force and cost. An offhand item filters vacuum collection. Rechargeable batteries can cycle held-tool/armor/inventory priority with Sprint+Sneak+use. Backpack battery tooltips use actual upgraded capacity and Shift-use selects bag/held-tool/armor priority. Mining-laser mode changes show their energy price and use shared geometry for a bounded client preview. Surface and paint tools show their sampled material/paint and application price while aiming. Katana and bow tooltips show remaining powered hits/shots.

## Survey instruments

Geo-Resonance Prospector stays unpowered, with its existing 3x3 regional ore-strength and coarse-depth readings. Existing Ore Scanner and Advanced Ore Scanner IDs, recipes and original art are preserved.

- Ore Scanner: 250,000 JE base; 500 JE/completed 3x3 survey, three-second cooldown. Distance bands are 0-16, 16-32, 32-64, 64-128, then 128-block bands. No compass bearing.
- Advanced: same base capacity, detailed 3x3 mode at 1,000 JE/five-second cooldown or wide 5x5 mode at 4,000 JE/ten-second cooldown. Shift-use to switch. Distances are rounded into eight-block bands, with an eight-point compass bearing or above/below reading. Approximate depth is reported in 32-block bands.
- Distances are three-dimensional distance from the survey origin to the nearest sampled matching block, not coordinates or a promise of the exact nearest vein. Ordinary sampling is every second column, detailed every column, wide every fourth column. Strength normalizes for sampled area. Bulk-deposit readings respect depletion. Reservoir distance is horizontal distance to a loaded reservoir chunk footprint, explicitly marked surface distance; the scanner does not fabricate an underground oil position.
- Mining-machine scanner slots retain their existing 100 JE/depth-survey cost and two/four-block standard/advanced lateral ranges; capacity upgrades do not turn a standard scanner into an advanced one.
- Only loaded terrain is examined. One pending job per player, at most eight pending jobs; bounded round-robin processing allows at most 16,384 block samples/server tick and 4,096/job. Changing held item, death, dimension or timeout cancels with no energy charge. Charge is checked again and deducted once on completion. An empty but valid scan still costs energy.
- Results display sampled coverage, origin, age, distance and (advanced) direction/depth. Pages and filters expose all readings. Five recent results are retained for the client session and cleared on disconnect. The latest complete survey is saved with its item through a bounded, versioned snapshot. Shift-use the standard scanner, or Sprint+Sneak+use the advanced scanner, to reopen that saved result for free.
- Server packets contain rounded readings and survey origin, never the sampled ore coordinates. The Survey Rocket keeps its wider regional role.

## Compatibility and verification

No platform or dependency changes. Existing native server damage, Chargeable, item custom-data, smithing component copying, survey registration and workshop screen systems are reused. The [Efficiency Module follow-up](tool-efficiency-module.md) and [two-bay charging menu](charging-station-menu.md) extend this original balance pass. Larger charging racks remain deferred.

Verified November 2, 2026, against base commit `235524e0c8` plus this working change: Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Loom 1.17.21, JDK 25.0.4.1+1. Worlds were newly generated disposable Fabric test worlds; the live server was untouched.

- Full server suite: **3,309/3,309 required tests passed**, including all 24 new equipment/scanner regressions. Final run: `BUILD SUCCESSFUL in 6m 48s`. Coverage includes actual survival fall damage, partial mitigation, shield detach/re-equip and potion expiry, external night vision, movement billing, low-charge blower operation, energy conservation, safe module removal, complete/cancelled surveys, saved packet round trips, and actual component-preserving upgrade recipes. Existing drill, chainsaw, charging station, mining-machine and quantum-chest tests ran too. The quantum-chest test now asserts the agreed shared 20 JE thrust price instead of its former 50 JE expectation.
- Rendered client: `PoweredEquipmentClientGameTests` passed, including actual server item use, packets, exact survey charges, ordinary distance versus advanced bearing, screen bounds at 320x240 GUI size, and 25 visible drill-preview targets without energy consumption. Final run: `BUILD SUCCESSFUL in 4m 15s`. All three screenshots were reviewed; the preview uses visible outlines after replacing insufficiently visible particles.
- `python scripts/check_repository.py`: passed repository structure and documentation links.
- `python tools/check_mod_data.py`: passed 15,300 material IDs, data/recipe checks, and the existing art audit (38,254 block/item models, 3,777 quad parts, 182 combined models). Generator/catalog/handbook consistency was also checked directly.
- `python -m unittest discover -s scripts/tests -p test_jimtafacts_gear.py -v`: 3 passed.
- `python -m unittest discover -s scripts/tests -p test_client_test_selection.py -v`: 12 passed.
- `git diff --check`: passed.

Local Gradle commands use the cached JDK and offline dependency cache. A local init script removes generated resource directories from compiler classpaths; it changes no source, assertions or test selection. Main resources were fully prepared once, then the four edited runtime resources (language, handbook, gear catalog and mixin manifest) were copied and checked by SHA-256 before subsequent runs reused that output with `-x processResources`.

```powershell
.\gradlew.bat runGameTest -I ..\ci600-compile.gradle -I ..\exo-focused.gradle -x processResources --offline --no-daemon --max-workers=2
.\gradlew.bat runClientGameTest -PclientTests=PoweredEquipmentClientGameTests -I ..\ci600-compile.gradle -x processResources --offline --no-daemon --max-workers=2
.\gradlew.bat runGameTest -I ..\ci600-compile.gradle -x processResources --offline --no-daemon --no-parallel --max-workers=2
.\gradlew.bat build -I ..\ci600-compile.gradle -x processResources -x runGameTest -x sourcesJar --offline --no-daemon --no-parallel --max-workers=2
```

Only the first command installs the `jugcraft-test:powered_equipment_game_tests_*` filter. The final server run has no feature/test filter. Packaging reuses that successful full test run and omits the optional source archive; remaining build checks still run. Playable JAR packaging result: **passed** (`BUILD SUCCESSFUL in 9m 35s`), including 79 offline updater and 393 scheduling assertions. Final archive integrity passed across 189,061 ZIP entries; four edited runtime resources match the final source bytes, required runtime classes match the compiled bytes, embedded libraries are present, and the test class is excluded. The verified playable JAR is 344,280,433 bytes; SHA-256: `da615a0bd4f89e7b08a56aca85346088940b52eba0392939a6b4f5f94af8e37b`.

No human multiplayer, production-load benchmark, live-world migration or full client-gallery result is claimed. Existing dependencies and the live server installation were not changed.
