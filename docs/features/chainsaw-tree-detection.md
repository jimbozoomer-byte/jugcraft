# Chainsaw natural-tree detection

Owner-directed safety correction, November 3, 2026. Implemented with OpenAI Codex (GPT-6), extending [powered equipment](powered-equipment-rebalance.md), [Efficiency](tool-efficiency-module.md), and the [drill face correction](drill-face-mining.md). Based on `235524e0c8` in the existing equipment working branch; earlier owner changes are preserved.

## Player behavior

Both the Chainsaw and Industrial Chainsaw recognize the complete connected log shape before breaking any additional log. A credible rooted tree retains upward felling, branches, normal drops and the 128-log limit. Buildings and uncertain shapes only lose the block the player deliberately cuts. Single-log mode, sneaking and explicit leaf-clearing mode retain their existing roles. Each successful log still costs 40 JE before Overclock/Efficiency; all capacities stay at the existing balance, including 250,000 JE for tools. No planting, recipe, module, unlock or research change is included.

The detector requires a raw, upright starting log; a trunk at least three logs tall; a one-log or complete 2x2 footprint on natural growing ground; two continuous upright basal layers; and a supported canopy of non-persistent, non-decaying leaves spread over multiple columns and heights. It rejects stripped/all-bark timber, explicitly non-natural rubber logs, mixed connected log types, low horizontal beams, sprawling basal structures and unsupported off-trunk branches. Adjacent planks, stairs, slabs, fences, gates, doors, trapdoors, glass and block entities reject bulk felling; a natural bee nest is allowed. Trunk/canopy geometry is validated before the first extra break, so a building connection cannot be discovered only after its beams have already been cut.

Rooted-ground and natural-canopy checks follow the established ordinary-axe `TreeFelling` approach. The stricter powered recognition lives in the common `ChainsawTree` helper and is shared with the chainsaw outline. The ordinary axe's separate queued/durability behavior is retained. No new rendering infrastructure or assets are introduced.

## Safety, bounds and compatibility

Recognition inspects the original log virtually because native post-break callbacks run after it disappears. It uses a local cache of at most 8,192 block states, at most 128 connected logs, ten blocks of horizontal reach from the target, and 48 blocks of height. It follows the component down as well as up to check the footing and detect attached structures, then returns only logs reachable through the original upward cut. Unloaded terrain, limits and incomplete shapes fail to a single block without forcing chunks to load. Client results are bounded cosmetic candidates; the existing outline displays at most 64.

Each extra server break still passes the native protection/drop/energy path. Felling stops on insufficient charge or the first rejected extra break. Skipped/protected blocks consume no extra JE. There is no new saved state, packet, placement tracker, global cache or background job, so existing worlds and tools need no migration.

Vanilla logs do not provide universal placement provenance. This is deliberately conservative structural recognition, not proof of who placed a log. Exact tree replicas embedded in natural foliage can be indistinguishable from grown trees. Leafless trees/fungi, heavily altered trees, mixed species touching, very wide/large trees, unsupported ground or ambiguous constructions require single cutting. A standing tree adjoining a house/treehouse also falls back to single cutting to protect the construction. Ordinary player-placed decorative leaves are persistent and cannot qualify as the living canopy.

Progression is unchanged: existing steel/electrical chainsaw acquisition and JE supply support logging for construction and production. Safety has no resource charge or new prerequisite. The work changes no platform, dependency, art or recipe data. It does not deploy or restart a live server.

## Validation

Pinned platform: Minecraft 26.3, Loader 0.19.5, Fabric API 0.161.0+26.3, Loom 1.17.21 and local JDK 25.0.4.1+1. Test worlds are disposable generated worlds. Automated tests are distinct from human multiplayer or a public-server load measurement.

Ten new server regressions cover both chainsaws on rooted branched trees, bare log buildings, persistent/decaying canopies, worked/horizontal/unrooted timber, buildings and beams adjoining living trees, exact/overflowing 128-log trunks, single/sneak overrides, low charge and upper-trunk stumps, vetoed breaks, real oak/birch/spruce sapling growth, and unloaded chunk boundaries. The two earlier log-cost/felling fixtures now include soil and a natural canopy; their log counts and exact JE assertions are retained. Leaf-clearing remains independently tested.

The rendered client regression checks tree/decorated-timber/treehouse/low-beam outlines and mines each through the normal attack-key/network path, awaiting confirmed server charge before checking surviving logs and building materials. Screenshots hide the HUD for a clear view of the outline.

- Full server suite: **3,344/3,344 required tests passed**, including the second-layer beam/low-canopy regression, without a test/feature filter. The final `runGameTest` completed in 10m 46s including compilation.
- Rendered client: **passed**, `runClientGameTest -PclientTests=ChainsawTreeClientGameTests,DrillFaceClientGameTests,PoweredEquipmentClientGameTests`, 7m 17s. All four chainsaw scenes passed their outline, real attack packets, surviving-log/material and exact server JE assertions (natural tree: six logs/240 JE; each building/beam: one log/40 JE). The prior drill/scanner checks passed too. The low-canopy camera aims below the foliage so it picks the intended log; its first expanded-fixture run timed out before attacking until that angle was corrected.
- Repository check: **passed**, `python scripts/check_repository.py`.
- Playable build: **passed**, 16m 54s, including 79 offline updater and 393 scheduling assertions. The final build used `build -x runGameTest -x sourcesJar` with the prepared-resource flags below: the complete server suite had already passed separately and the optional source archive was omitted.
- Archive verification: **passed**. The delivered `jugcraft-0.1.0-alpha.jar` is 344,307,454 bytes with 189,075 ZIP entries. Complete CRC verification passed; fourteen relevant main/client classes byte-matched the final compilation, both existing mixin manifests and the Efficiency recipe byte-matched source, and embedded-library entries were verified. Test classes are excluded. The delivered copy matches the finished build's SHA-256: `1aa9da7c372a511d4c1e2d9063afc5ac8eca6657d509937827f3582b6cfd1c24`. All four current chainsaw screenshots were reviewed.

Local Gradle runs use `-I ../ci600-compile.gradle` to remove prepared resource directories only from compiler classpaths, plus `-x processResources -x processClientResources` to reuse the previously prepared resource output. This correction changes Java only, so those runtime resources remain unchanged. The previous data/recipe/art audit is recorded in the Efficiency feature; no new data/art audit is claimed here.
