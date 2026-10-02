# Wetlands (biomes batch 3)

Status: in progress on branch `claude/biomes`, with the region engine ([biome-regions.md](biome-regions.md)). Awaiting CI. **Not yet played.**
Proposal issue: none. The owner asked on 2 October 2026 to remake the Biomes O' Plenty catalog's biomes ([branches/BIOMES.md](../branches/BIOMES.md)) and to carry on through every batch. Everything here is original: the catalog guided the concepts only.
Owner: @jimbozoomer-byte
Target milestone and tier: world generation, farming and building (Discovery).
Primary specialty and supported player role: exploration and farming (cranberries, mud), building (willow wood).

## Player experience
Fourteen wet biomes, most in the **wetland** layout of Jugcraft regions. The swamps and bayous also grow in the woodland and wild layouts, where nothing else claims those climates.

| Biome | Replaces | What grows |
| --- | --- | --- |
| **Bog** | cool swamps (one weirdness half) | reddish-orange grass, maple scrub, bushes and berry bushes; **cranberries** ripe in its shallow, muddy pools |
| **Dead Swamp** | cool swamps (the other half) | dark, muddy ponds, sparse dead trees, cattails; no animals; villages |
| **Lush Swamp** | temperate swamps (one half) | vibrant grass and blue water, tall oaks hung with vines, willows, cattails, ferns, berries |
| **Swamp Woods** | temperate swamps (the other half) | willows and vine-hung oaks over duckweed and lily pads; animals |
| **Bayou** | warm mangrove swamps | willows standing in the water, trailing vines like moss, cattails and ferns on mud; overcast |
| **Floodplain** | hot mangrove swamps | brushy oaks and tall grass, orange cosmos; lily pads and watergrass in the floods |
| **Ghost Forest** | dark forests (one half; wetland layout) | grey dead trees and dark oak scrub around many lakes |
| **Sludge Mire** | dark forests (the other half) | a dense canopy of dark oaks and big oaks over mud and sludgy, algae-covered pools; no animals; villages |
| **Lush River** | temperate and warm rivers (wetland layout) | duckweed, lily pads and watergrass; oak bushes on the banks |
| **Fen** | old-growth spruce taiga (wetland layout) | short firs and dark oaks over muddy pools, cattails and lily pads |
| **Lake District** | temperate forests (wetland layout) | oaks and spruces broken by many lakes with muddy shores |
| **Quagmire** | dry cool plains (wetland layout) | muddy flats and brown ponds, cattails; no trees, no animals |
| **Marsh** | cool plains (wetland layout) | a wide green plain of shallow lakes full of watergrass, ringed by cattails; no trees |
| **Wetland** | temperate plains (wetland layout) | grass and mud, ferns, spruces and willows, cattails, purple water; villages |

- Biomes O' Plenty's Deep Bayou merges into the Bayou, the Land of Lakes Marsh into the Marsh, and the Land of Lakes becomes the Lake District.
- Seasons: all fourteen change colour with the seasons; all but the Bayou, Floodplain and Lush River take winter snow when `seasons.snow` is on ([seasons.md](seasons.md)).
- **New plants:**
  - Cattail: a two-block reed of the water's edge.
  - Watergrass: grows under water like seagrass; only shears take it.
  - Duckweed: floats on still water like a lily pad.
- **New tree: the willow.** A short trunk under a broad crown whose leaves hang in long curtains, with vines trailing like moss. Its leaves follow the seasons: green from spring, yellow in autumn, bare in winter (the server's season clock, as for the maple). Full wood set; saplings.
- **New ground:** mud patches and muddy ponds (vanilla mud), and tall oaks hung with vines.

## Connections
- Input producer: world generation.
- Output consumer: agriculture (the Bog grows the agriculture branch's cranberries wild, ripe for picking: a reachable source besides planting), building (willow wood; vanilla mud to bricks), composting (the new plants).
- Technology connection: the sawmill and tree farm take willow like the other woods.
- Magic connection: none yet.
- Reachable entry path: swamps and mangrove swamps are common, and three of four layouts grow wetland swamps.
- Required vs optional: nothing here is required.

## Balance and automation
- Willow is ordinary wood (4 planks by hand, 6 in the sawmill). The plants compost like vanilla's seagrass and lily pads. Wild cranberries are the same block the agriculture branch farms; picking them works as on a farm.

## Multiplayer and persistence
- Server authority: world generation and the season clock are the server's.
- `biomes.enabled=false` stops the biomes in new chunks and the willow's hand recipes. Every block, item and biome stays registered.
- Existing worlds: new chunks only.

## Dependencies and assets
- Plants are registered from `/jugcraft/plants.json` (`tools/plants.py`): cattail is vanilla's DoublePlantBlock, watergrass `agriculture/WaterPlantBlock` (seagrass-like, our own), duckweed vanilla's lily pad block with our texture.
- Ponds are vanilla's lake feature filled with water and banked with mud; mud patches are vanilla's disk feature. Willows use vanilla's cherry foliage shape and vine decorator.
- Textures drawn by code in `tools/wild_textures.py`. No Mojang file is copied.

## Verification
Results are recorded under "Results" below after CI runs.
- `python3 tools/check_mod_data.py`: plants, the willow (leaf schedule against every season mode, the seasonal decorator), biomes and their features, and non-overlapping rules.
- Server game tests (`BiomeGameTests`):
  - `seasonalForestSaplingsGrow` now grows a willow too: all its leaves in autumn colours in autumn.
  - `wetlandPlantsWork`: watergrass stands only in water and keeps its water; duckweed floats only on water; a cattail takes two blocks and drops one.
  - `regionLayoutFollowsItsRules`: every batch 3 rule places its biome in each of its layouts.
- Client game test (`BiomeClientGameTests`): distances from the start of a real world (seed `jugcraft`), generated leaves in today's look, and screenshots of each wetland found.
- Not run: play, a dedicated server, two clients.

### Results
Not yet run in CI.

## World and event applicability
- Biome fit: each biome takes the climate of the vanilla biome it replaces.
- Seasons: see above.
