# Powered tool Efficiency Module

Owner-directed implementation, November 2, 2026, with OpenAI Codex (GPT-6). Extends [powered equipment](powered-equipment-rebalance.md) and the [two-bay charging station](charging-station-menu.md). No platform, dependency or live-server changes.

## Player behavior and balance

The Efficiency Module fits the Mining Drill, Industrial Drill, Diamond Drill, Iridium Drill and Chainsaw, matching the existing Overclock compatibility. Two modules fit. Level I reduces tool energy by 15%; level II by 30%, without changing speed or capacity. This is separate from the vanilla Efficiency enchantment. Weapons, armor, scanners and standalone jetpacks do not gain this mining-only discount.

Overclock is priced first, then the percentage discount is applied to each successfully mined block, rounding fractional JE upward. Costs never become zero. Extra area/vein/tree/leaf breaks use the same price as the first block. Unused tools have no new drain.

| Tool / Overclock | No Efficiency | Efficiency I | Efficiency II |
| --- | ---: | ---: | ---: |
| Drill / none | 60 | 51 | 42 |
| Drill / I | 90 | 77 | 63 |
| Drill / II | 120 | 102 | 84 |
| Chainsaw / none | 40 | 34 | 28 |
| Chainsaw / I | 60 | 51 | 42 |
| Chainsaw / II | 80 | 68 | 56 |
| Leaf clearing / none | 10 | 9 | 7 |
| Leaf clearing / I | 15 | 13 | 11 |
| Leaf clearing / II | 20 | 17 | 14 |

All entries are JE/block. The tool's energy tooltip and estimated remaining blocks automatically use the resulting price. Charging capacity and station throughput stay at the previously agreed values.

## Acquisition and module management

Craft one from four steel plates, four copper cables and an advanced circuit, using the shared machines feature condition. Its resources come from the existing steel/electrical workshop path; its consumers are mining, logging and exploration. The recipe has no new research or material gate. The creative tools tab, Tinkerer advancement and handbook include it. JEI receives the standard shaped crafting recipe.

The new item is `jugcraft:efficiency_module`; fitted levels persist and synchronize through `jugcraft:tool_efficiency`. Old equipment defaults to level zero. Existing component-copying drill tier recipes preserve Efficiency alongside charge, names and other modules. Removing/refitting a module changes its price without changing its current JE balance. Invalid component levels clamp to zero/two and cannot yield free or negative-cost operations.

Both charging bays list the sixth module row with native + / - controls, stock checks, fitting limits and atomic recovery. Six rows fit the original 308x232 panel; the player inventory coordinates and minimum 320x240 GUI support remain intact. The six-kind menu has 24 bounded button IDs. Existing module order is preserved, with Efficiency appended. Clients and servers must use the same updated build.

The recipe-derived essence value is 4382/3 under the current ingredient value table (including its existing balance-pending material values); the module is not left on the one-essence fallback. This follows the shared value builder's recipe-cost calculation and does not claim to close the broader economy backlog.

## Art and generators

The owner library and catalog were searched for a suitable efficiency/energy-upgrade cartridge. The located magical recipes/lens are different equipment. The icon instead extends Jugcraft's existing `tools/generate_textures.py` module-card generator with a leaf glyph and lime indicator, using the same gunmetal/chrome cartridge and circuit window as the other tool modules. No supplied original or existing runtime icon was altered. It is a deterministic 16x16 RGBA sprite, matching the original Overclock and Capacity module-card pixels outside its intended symbol/lamp changes.

The icon table is exposed as `module_icons()` for reproducible targeted generation. `tools/machines.py`, `tools/advancements.py`, the resource manifest, language and handbook sources describe the new item and recipe. Runtime item/model definitions, sprite, crafting recipe, advancement, handbook and value table are updated together.

## Validation

New server regressions cover all nine Overclock/Efficiency combinations for drilling, logging and leaves; unchanged full-charge speed; fitting/removal/limits; save/load; malformed component levels and low-charge operation; actual 3x3 mining, tree and leaf clearing; native acquisition/tier crafting; explicit recipe-based essence pricing; and independence from vanilla Efficiency enchantment. The existing rendered station test now installs Efficiency II on a drill and Efficiency I on an overclocked chainsaw through real widget packets, checks synchronized prices and recovery, and inspects all 24 control bounds at the smallest GUI size.

No human multiplayer or production-load evidence is claimed. Final commands and observed results are recorded below.


Local verification, November 2, 2026, against the existing powered-equipment working branch based on `235524e0c8`, Minecraft 26.3, Loader 0.19.5, Fabric API 0.161.0+26.3 and JDK 25.0.4.1+1:

- Full server suite: **3,328/3,328 required tests passed**, including all seven new Efficiency regressions. `BUILD SUCCESSFUL in 11m 34s`. No test/feature filter was used.
- Rendered client: **passed**, `BUILD SUCCESSFUL in 4m 56s`. Real widget packets installed Efficiency II on the drill and Efficiency I on the overclocked chainsaw, confirmed client-side prices 42 and 51 JE/block, recovered a module, and checked all 24 controls against the footer/panel at 320x240 GUI units. The icon, six-row layout and module counts were reviewed in screenshots.
- Repository structure/documentation links and whitespace checks: passed.
- Targeted recipe, advancement, language, handbook, native sprite and copied-runtime-resource generator consistency: passed.
- Data/recipe/art audit: passed for 15,301 material IDs, data files and recipes; art checks passed for 38,254 block/item models, 3,777 quad parts and 182 models drawn together.
- Final playable build and archive verification: **passed**, `BUILD SUCCESSFUL in 15m 5s`, including 79 offline updater and 393 scheduling assertions. CRC integrity passed across 189,070 entries; relevant classes match final compiled bytes, all eight changed resources match source, embedded libraries are present and test classes are excluded. Artifact: 344,296,588 bytes; SHA-256 `970a73bd1939dadd69b749db90aea13a8d81bd4689beda8091f1913923b7e27c`.

Local runs reused the previously prepared main resources; all eight changed runtime resources were copied and byte-verified. The local compiler init script excludes resource directories from compiler classpaths and changes no assertions/runtime behavior. Packaging reuses the completed full server run and omits the optional source archive.

```powershell
.\gradlew.bat runGameTest -I ..\ci600-compile.gradle -x processResources --offline --no-daemon --no-parallel --max-workers=2
.\gradlew.bat runClientGameTest -PclientTests=ChargingStationClientGameTests -I ..\ci600-compile.gradle -x processResources --offline --no-daemon --no-parallel --max-workers=2
python tools/check_mod_data.py
.\gradlew.bat build -I ..\ci600-compile.gradle -x processResources -x runGameTest -x sourcesJar --offline --no-daemon --no-parallel --max-workers=2
```
