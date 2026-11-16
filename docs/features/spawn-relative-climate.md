# Spawn-relative Overworld climate bands

Status: climate runtime and editor validated locally November 15, 2026. Owner correction: apply the same bands to the CURRENT server world for all future generation. Existing-world migration is being validated; it is not activated on the live server yet.
No commits, pushes or live-world changes.
Coordination and local review: OpenAI Codex. Four implementation/review workstreams
requested through the owner's existing Claude Code session; actual worker startup
and deliveries must be recorded below, not inferred from the assignment.

## Owner outcome

Travel north or south from the spawn/city origin to find predictable climate belts.
East/west travel stays within the same belt while retaining biome variety.

| Offset south of the saved origin | Climate |
| --- | --- |
| -2,500 blocks and farther north | Arctic/frigid only |
| Between -2,500 and +2,000 | Existing central biome mix |
| +2,000 to +4,000 | Hot desert, tropical and jungle |
| +4,000 to +5,500 | Temperate forests, grasslands and taiga |
| +5,500 and farther south | Arctic/frigid only |

North is decreasing Minecraft Z. These are approximate transition thresholds,
not radial distances from world zero. The outer Arctic limits must be strict;
any transition to those limits occurs on their inward side.

## Work ownership

| Workstream | Assigned responsibility | Current evidence |
| --- | --- | --- |
| Claude A | Immutable climate profile, deterministic biome resolver, compatible biome pools and pure tests | Delivered in the combined 18-patch series; integrated locally |
| Claude B | Serialized biome-source integration and explicit new-world opt-in, World Designer runtime hooks | Delivered in the combined 18-patch series; integrated locally |
| Claude C | Offline editor controls, climate overview, schema/export and JavaScript checks | Delivered in the combined 18-patch series; integrated locally |
| Claude D | Independent generation/codec/persistence audit and dedicated runtime tests | Delivered in the combined 18-patch series; integrated locally |
| Codex | Local baseline review, shared contract review, delivery integration, combined tests and migration notes | Integrated all 18 patches; corrected Minecraft 26.3 API use, reviewed lifecycle, added real-export registry regression and stable biome tie handling; combined validation passed |

Claude's cloud checkout does not contain the complete dirty Windows integration.
Review every delivery against the local files; do not replace this checkout with
the cloud tree. Existing Thanksgiving, horses, ships, food, UI and other local
changes remain intact. No commits, pushes, PR changes or deployment in this task.

Claude resumed after its earlier quota limit and completed all four workers. The
combined delivery is based on `8075c6d58b`, cloud tree `eeba172b`, 62 files. All
32 delivered SHA-256 entries verified locally. Patches were applied without Git
index/commit operations; the additive Gradle hook was adapted to this dirty tree.
Local copies and original touched-file backups are in `build/nov14-climate-review/`.

### Draft contract corrections sent to Claude

1. Do not equate vanilla's climate sampler spawn search with Jugcraft's actual
   spawn. `AlpineSpawn.moveWorldSpawn` performs a later alpine-village/biome search
   on `SERVER_STARTED`, and `TownPlanner` then searches around that settled spawn.
   A new wrapper also fails the current `instanceof MultiNoiseBiomeSource` guard.
   The initial implementation should use an explicit frozen authored spawn unless
   the procedural origin lifecycle is implemented and proved correct.
2. Empty climate pools must fail validation. In particular, a missing frigid pool
   cannot silently return a warm biome; ocean categories cannot fall back to land.
3. Unconditionally preserving surface mushroom/flower islands would violate
   strict Arctic belts. Remap them as land or clearly document an approved
   exception; the mandatory preservation is for underground/cave content.
4. Vanilla tags alone do not classify the imported landscape collection. Use its
   existing kind metadata and actual biome climate/precipitation data.
5. `JugcraftRegions` has pre-existing static seed/config state. Do not copy that
   pattern or claim the whole older engine is multiworld-isolated merely because
   the new climate profile is immutable.

The deliveries adopt these corrections. In particular, only the authored World
Designer path is enabled: a generic procedural preset is deferred until its
spawn-search lifecycle can be made correct. There is no automatic live-world migration.

## Integration constraints and review findings

- Reuse the existing serialized World Designer framework. A profile belongs to
  its biome source/world and survives codec round-trips; no global active profile.
- Missing optional climate fields must retain previous behavior exactly.
- Opt-in must be usable through the editor/export and actual new-world generator,
  not merely a standalone classification helper.
- Freeze the selected origin into the exported generator. Later `/setspawn`
  commands must not move climate belts and create unexplained generation seams.
- Resolve both `createResolver` and `createResolverForChunk` consistently. The
  existing multi-noise mixin already applies Jugcraft region and Adam-ondi-Ahman
  overrides; the new wrapper must not recurse or accidentally bypass those.
- `collectPossibleBiomes` must include every possible output before feature
  ordering and structure generation. Registry lookup must remain world-local.
- Preserve authored painted cells and pinned structure contracts deliberately;
  the editor must make any exception to procedural climate selection visible.
- Preserve land/ocean/deep-ocean/river/coast roles and mountain terrain. A warm
  ocean must not turn into a desert island merely to satisfy the hot belt.
- Preserve custom underground caves and dimensional sources. Y alone is not
  enough to identify every underground biome; inspect actual biome identities.
- Audit seasonal precipitation, temperature and foliage rules against the chosen
  custom pools. Do not claim seasonal compatibility from biome names alone.
- Default editor extent is only 2,048 blocks across. Show a separate broad climate
  overview so the +5,500 belt is inspectable without expanding the terrain grid.

## Acceptance before marking complete

- [x] Shared Java/JavaScript profile contract, defaults and malformed-input bounds agree.
- [x] Nonzero/negative origin, both hemispheres, east/west invariance, transition
  determinism and exact outer Arctic bounds tested.
- [x] Old JSON and disabled profiles preserve fallback behavior.
- [x] Ocean/river/coast, custom caves, painted cells, structures and possible-biome
  feature ordering covered by meaningful tests.
- [x] Two profiles/worlds and repeated codec save/load cannot share an anchor.
- [x] Actual generated chunks from an exported opt-in pack match the belts;
  inspected terrain, not just an in-memory helper's answers.
- [x] Reloaded world retains climate origin; Nether/End remain unchanged.
- [x] Offline editor preview/export reviewed and combined local checks pass.
- [x] Limitations and existing-world migration requirements recorded accurately.

## Existing-world rollout is separate

This task does not regenerate or edit the live world. Existing chunks keep their
blocks, biome data, builds and claims. A later deployment needs a backup and an
explicit saved-generator migration plan using the actual world spawn/city anchor.
Assess already explored coverage and old/new chunk seams on a disposable copy
before choosing an approach. Do not delete regions, trim chunks, move the city,
or automatically reinterpret an old world on load. Even a new-chunks-only
generator change needs that review; it does not repair existing terrain.

## Validation evidence

Focused combined validation passed on November 15; these results do not claim the
entire repository suite or production rollout.

- Main/client/gametest compilation passed against Minecraft 26.3 / Fabric 0.19.5.
- 29 focused server game tests passed, including the history-dependent biome tie regression.
- All three selected client suites passed together (`combined-r6.log`):
  - Design integration: 144 generated samples, the same 144 after reopening, and
    48 newly generated samples agree; authored spawn/city placement exercised.
  - Actual exporter fixture: registered preset loaded at registry startup,
    generated all five belts, saved/reopened and retained its climate origin after
    `/setworldspawn`. This loads the real exported data; manual ZIP drag/drop in
    the Data Packs UI is not claimed.
  - Independent dense audit: 109 requested sites, 24,960 distinct stored biome
    samples including 2,153 cave samples at/above Y0 in belts; 312 control-world noise
    columns and their water/land topology agree (624 terrain/wetness records). Both profiles passed strict-boundary
    sweeps (30,060 and 29,892 quarts), 576 fuzzed block-level boundary lookups
    passed, all four transition bands occurred correctly, and Nether/End stayed
    unchanged. Reopening retained all 24,960 samples, and 6,720 newly generated
    samples in reverse order matched first-session predictions.
- Pure Java climate checks: 7,928,159 assertions passed again on the final code (`final-export-r7.log`).
- Independent geometry/property audit: 32 groups, zero failures; 200,069 profiles,
  185,916,294 block-column comparisons, plus 3,069 editor profiles / 101,745 points.
- Editor/model: 22 tests passed, including all eight byte-identical export fixtures.
- Biome pool data audit: 13 passed. Client test-selection tooling: 28 passed.
- Offline editor reviewed in-browser: enabled overview and default belts, invalid
  typed value blocked export, valid correction restored export. Capture:
  `build/nov14-climate-review/editor-preview.png`.
- First compilation caught removed `RegistryCodecs`; corrected to pinned
  `Biome.LIST_CODEC`. Lifecycle review decoupled possible-biome enumeration from
  reverse holder tag membership; resolvers still validate all pools/categories.
- Runtime audit caught and corrected a test-domain omission (vanilla meadow,
  replaced in ordinary Jugcraft tables) and the real base-lookup tie below.
- Evidence: `build/nov14-climate-review/` contains verified delivery hashes,
  original touched-file backups, review, logs, property results and captures.
- Final local JAR built successfully (346,784,662 bytes), SHA-256
  `4009c9a2b220836569fe5938dfc1ccf87c78cbd5f4dfc09a9659ae2bd2e40334`.
  Archive inspection confirmed climate classes, matching access-widener metadata
  and exclusion of the test-only exported preset/classes. This is the whole dirty
  local checkout build, not a published release or player pack.
- Export regression passed again with screenshot-only settings changes (`visual-r9.log`).
  The final capture was inspected: frozen ocean, icebergs and retained open-water
  patches, without missing textures. `0000_jugcraft_climate_export_north.png` is
  saved beside the editor preview. An intermediate capture run (`visual-r8.log`)
  timed out after changing render distance mid-world; starting with the intended
  distance fixed the test setup. These captures use disposable test profiles,
  never the owner's launcher profile.
- Sources: Claude Code four workers; local integration/fixes/tests: OpenAI Codex.

Known limits: vanilla frozen/deep-frozen oceans retain their own rain/open-water
behavior; cold desert and Siberian taiga are dry. Two selected temperate custom
biomes lack seasonal foliage mappings; a full seasonal integration remains a
follow-up. Existing `supps_ocean_barnacles` feature calls logged far-chunk-write
warnings in the plain control as well as climate worlds; no climate test failed
from them, and this unrelated placement issue is recorded separately in TODO.

### Local generation audit correction, November 15

The first dense client audit exposed a pre-existing multi-noise lookup tie at
(-2596, 64, -2580): identical climate values could answer Alpine Spawn or Pixel
Hollows depending on the spatial tree's previous per-thread result. The climate
remapper correctly preserved its actual input, but later lookups could disagree
with generation. This is not accepted as deterministic climate output.

For opted-in climate sources only, `StableBiomeLookup` now starts each vanilla
and regional spatial-tree query without its previous winner. The same tree order
breaks ties consistently; the spatial index is retained (no brute-force scan).
The per-source opt-in is set before generation. Ordinary sources remain unchanged,
and no global climate/profile state is introduced. Two accessors and a single-class
26.3 `official` access widener expose the existing tree cache. A focused runtime
regression reproduces the old tie and verifies stable results after alternating
neighbour queries. The plain-world terrain control explicitly normalizes this
lookup tie behavior before comparing the climate transformation; its biome mapping
and terrain settings remain unmodified. The synthetic tie regression and all three real-world client suites passed with this correction.


## Existing server world — owner correction, November 15

The owner explicitly wants the CURRENT hosted world to use these bands for future
chunk generation, not only newly created World Designer worlds. This supersedes
the earlier new-world-only rollout scope. Existing chunks/builds remain intact.
OpenAI Codex is implementing and validating this migration.

Read-only downloads from Modrinth confirmed Minecraft 26.3's split metadata:
`world/data/minecraft/world_gen_settings.dat` contains the generation settings;
`world/level.dat` contains the spawn. The Overworld is `minecraft:noise` with the
`minecraft:multi_noise` Overworld preset. Saved spawn is (1400,92,395); the
permanent town origin is (1296,114,400). The town origin fixes the climate anchor
at X1296/Z400. No later spawn move changes it.

- North frigid boundary: Z -2100.
- Hot/tropical start: Z 2400.
- Temperate/forest/taiga start: Z 4400.
- South frigid boundary: Z 5900.
- The existing 256-block transition rules apply inward from strict Arctic edges.
- Cave biomes keep their existing category preservation; Nether/End are unchanged.

`tools/prepare_climate_migration.py` prepares a separate bundle and original-file
backup. It refuses unexpected versions/generators/authored sources, conflicting
existing profiles and invalid town origins. It changes only the Overworld's
`generator.biome_source`, wrapping the original in `jugcraft:climate`. Seed,
terrain/noise settings and all other dimension settings are identical after
unwrapping. It never opens region files or installs into a world. Re-running on
the same profile is byte-idempotent. Eight Python regression tests passed.

Adam-ondi-Ahman's source identity now unwraps this climate layer when loading its
existing procedural state, retaining the saved start valley. Designed worlds
remain excluded. The real existing-save test checks old biome samples, a player
build/container, town/spawn/valley state, new generation and two reopenings.
Validation results are recorded below when complete.

Local preparation: `build/nov15-climate-migration/`. The live generation metadata
SHA-256 is `79159495400c476a731db8f41e13ad43e7c3c30ce575d63fdb002643635e316c`;
prepared metadata SHA-256 is
`549cd7b6ec4a5ca801f849572db473db55c9e58632ae60af770e6a751941f833`.
No server files have been changed, stopped or restarted. Activation needs a
climate-capable matching build and an approved maintenance restart with the full
30-second player warning. Before install, take a fresh backup, stop the server,
download/recheck current metadata and town origin, then replace ONLY generation
metadata and the reviewed mod/release files. Never install an old level.dat or
copy the sampled town/player data back. Retain the original metadata for rollback.
The dirty local all-feature JAR is not automatically an approved release artifact.

### November 15 isolated release readiness

Prepared a climate-only checkout at `../jugcraft-climate-server-nov15`, based on
the deployed/main commit `8075c6d58b34f12bd7748ec814832bec20d3d16b`.
Candidate commit `17da8a149cd7b323258570f16e19bdfee873e364` is under review in
[PR #613](https://github.com/jimbozoomer-byte/jugcraft/pull/613). The original dirty
integration remains local and is not included in this release.

The isolated candidate passed 7,928,159 pure assertions, five focused server
tests, ten migration Python tests, thirteen pool-data tests, repository checks,
and the existing-world client migration test using the sampled live generation
metadata. The client test retained 4,608 old biome samples, buildings, chest
contents, town/spawn/valley state and verified fresh climate generation across
two reopenings. It used a disposable world, not a full live-world clone.

Release `0.1.0-alpha.20261115.1` is assembled locally in that checkout's
`build/release-20261115.1/`. Six artifact hashes/sizes were verified; client and
server mod JARs match. `build/server-patch-20261115.1/upload/` contains only the
managed JAR, release metadata and separately prepared world-generation metadata.
Re-download and recheck the latter after the approved stop before installing.

GitHub Foundation passed; full Build and client shards remain running as of the
last check. Nothing is merged, published, promoted or installed live. Requested
owner approval for a restart after checks pass with the 30-second warning, and
for replacing only the October 22 10:04 AM city-defenses manual backup because
all ten slots are full. Both approvals remain pending. Preserve the May archive
and all newer manual backups. No server action or backup deletion has occurred.
