# End biomes (biomes batch 9)

Status: in progress on branch `claude/biomes`. Awaiting CI. **Not yet played.**
Proposal issue: none. The owner asked on 2 October 2026 to remake the Biomes O' Plenty catalog's biomes ([branches/BIOMES.md](../branches/BIOMES.md)) and to carry on through every batch. Everything here is original: the catalog guided the concepts only, and its invented names are replaced by our own.
Owner: @jimbozoomer-byte
Target milestone and tier: the outer End (after the dragon).
Primary specialty and supported player role: exploration; building (coral, sandstone, moss, pale oak).

## Player experience
Five new biomes of the outer End islands. Four share the highlands with vanilla's End Highlands (each a quarter as likely as vanilla's), and one takes a third of their barrens. They keep vanilla's chorus and End cities where the End Highlands have them.

| Biome | What it is |
| --- | --- |
| **Chorus Reef** | a dry reef among the chorus: sand, sandstone and dead coral blocks over the end stone, dead corals and fans, still pools, sandstone pillars |
| **Ender Wilds** | moss over the end stone under violet jacarandas and azaleas, glowcaps and glimmerblooms, glinting motes |
| **Outer Flats** | low, wide barrens of end stone, sand and gravel at the islands' edges, with dead bushes |
| **Phantom Garden** | pale moss and its carpets, pale oaks and eyeblossoms in the End's dark |
| **Rotted Expanse** | coarse dirt and soul soil seeping through the end stone, dead trees, obsidian pillars and murky pools; no endermen |

- Biomes O' Plenty's names become ours: the End Reef becomes the Chorus Reef, the End Wilds the Ender Wilds, the End Flats the Outer Flats, the Spectral Garden the Phantom Garden and the End Corruption the Rotted Expanse (rot, not a fake glitch).
- No new blocks: the batch uses vanilla's and the earlier batches' (jacarandas, glowcaps, glimmerblooms, dead trees).

## Connections
- Input producer: the outer End.
- Output consumer: building (sand, sandstone, dead coral, moss, pale oak, jacaranda wood), light (glowcaps), saplings for trees in the End.
- Technology connection: none new.
- Magic connection: none yet.
- Reachable entry path: after the dragon, through an End gateway; each is a share of the outer islands.
- Required vs optional: nothing here is required.

## Balance and automation
- Everything here is vanilla material the Overworld gives too; trees in the End make wood easier to come by after the dragon, which vanilla's chorus already does for food. Obsidian pillars are a few small columns. The Rotted Expanse spawns no endermen, so it is no farm. No new conversions, no loops.

## Multiplayer and persistence
- Server authority: world generation and spawning are the server's.
- `biomes.enabled=false` (read at startup) leaves the End vanilla in new chunks. Every block, item and biome stays registered.
- Existing worlds: new chunks only.

## Dependencies and assets
- Placement: Fabric's End biome API (`TheEndBiomes.addHighlandsBiome`, `addBarrensBiome`), from the generated `/jugcraft/dimension_biomes.json` (`biome/JugcraftDimensions`).
- Ground: patches of each biome's blocks over the end stone (vanilla's vegetation patch feature, replacing only `#jugcraft:end_ground_replaceable`, which holds end stone).
- Features reused by reference: vanilla's pale oaks and pale garden flowers, chorus and End cities from the base; lakes, columns and simple blocks for pools, pillars and corals.

## Verification
Results are recorded under "Results" below after CI runs.
- `python3 tools/check_mod_data.py`: biomes and their features, the generated placements current, no End biome with region rules, an Overworld surface or seasons.
- Server game test (`BiomeGameTests.dimensionBiomesArePlaced`): the game test server's End has one fixed biome, so the test builds the End's biome source as a real world does; every Jugcraft End biome must be among its biomes, and their features must sort into one order (no feature order cycle).
- Client game test (`BiomeClientGameTests`): each End biome's nearest place to the origin, a standing spot on its ground, and a screenshot.
- Not run: play, a dedicated server, two clients.

### Results
Not yet run in CI.

## World and event applicability
- Biome fit: the outer End's highlands and barrens, beside vanilla's.
