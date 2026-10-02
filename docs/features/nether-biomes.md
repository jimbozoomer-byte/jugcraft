# Nether biomes (biomes batch 8)

Status: in progress on branch `claude/biomes`. Awaiting CI. **Not yet played.**
Proposal issue: none. The owner asked on 2 October 2026 to remake the Biomes O' Plenty catalog's biomes ([branches/BIOMES.md](../branches/BIOMES.md)) and to carry on through every batch. Everything here is original: the catalog guided the concepts only, and its invented names are replaced by our own.
Owner: @jimbozoomer-byte
Target milestone and tier: the Nether (after a Nether portal).
Primary specialty and supported player role: exploration; mining and building (Nether stone, ice, sulfur and cinnabar).

## Player experience
Nine new Nether biomes join vanilla's five. They are rarer than vanilla's: each takes its own corner of the Nether's climate, with an offset that keeps vanilla's biomes the commonest.

| Biome | What it is |
| --- | --- |
| **Ashfall Wastes** | ash-grey floors of tuff and gravel, smouldering with magma, under falling ash |
| **Blighted Sands** | dunes of soul sand and soul soil overgrown with **brambles**, soul fire flickering |
| **Frost Rift** | a frozen rift: floors of snow, packed ice and blue ice, a cold blue haze, snow on the air, no glowstone; strays among the piglins |
| **Fungal Thicket** | mycelium and crimson nylium under huge red and brown mushrooms, crimson fungi and glowcaps |
| **Magma Fields** | volcanic fields of sulfur and cinnabar, magma and blackstone, sulfur spikes, lava deltas and basalt, under an orange haze |
| **Marrow Heap** | heaps of bone and nether wart with spires of bone, under a dark red haze |
| **Netherbrush** | warped and crimson nylium thick with brambles, roots and warped fungi, spores in the green air |
| **Quartz Rift** | calcite floors seamed with quartz ore, quartz pillars, white sparks in a scarlet haze |
| **Withered Hollow** | near-black blackstone floors with obsidian and a little crying obsidian, no glowstone; endermen and skeletons |

- Biomes O' Plenty's names become ours: the Ashen Inferno becomes the Ashfall Wastes, the Corrupted Sands the Blighted Sands, the Polar Chasm the Frost Rift, the Nether Fungi Forest the Fungal Thicket, the Erupting Inferno the Magma Fields, the Visceral and Parasitic Heaps the Marrow Heap, the Undergrowth the Netherbrush, the Crystalline Chasm the Quartz Rift and the Withered Abyss the Withered Hollow. The Marrow Heap is bone and nether wart, not flesh.
- Nether fortresses and bastions generate as in vanilla's Nether biomes they are based on.
- **New plant:** the bramble, dark red thorny canes that grow on Nether floors (netherrack, soul sand, nylium) as well as stone and soil.

## Connections
- Input producer: the Nether.
- Output consumer: building (tuff, ice, sulfur, cinnabar, calcite, blackstone), mining (quartz ore at the surface in the Quartz Rift), composting (brambles).
- Technology connection: none new.
- Magic connection: none yet.
- Reachable entry path: a Nether portal; each biome is a share of the Nether.
- Required vs optional: nothing here is required.

## Balance and automation
- The ground blocks are vanilla blocks that the Nether or Overworld already give. Blue ice and crying obsidian appear only as small shares of the Frost Rift's and Withered Hollow's floors; quartz pillars as a few small spires. The Withered Hollow spawns skeletons, not wither skeletons, so it is no new source of wither skulls. No new conversions, no loops.

## Multiplayer and persistence
- Server authority: world generation and spawning are the server's.
- `biomes.enabled=false` (read at startup) leaves the Nether vanilla in new chunks. Every block, item and biome stays registered.
- Existing worlds: new chunks only.

## Dependencies and assets
- Placement: Fabric's Nether biome API (`NetherBiomes.addNetherBiome`), from the generated `/jugcraft/dimension_biomes.json` (`biome/JugcraftDimensions`).
- Ground: each biome lays patches of its blocks over the floors of every layer (vanilla's vegetation patch feature, before its other features), rather than replacing vanilla's Nether material rule, which has no parts to refer to and must not be copied.
- Features reused by reference: vanilla's huge mushrooms and sulfur spike clusters, and the Nether bases' own features. Brambles are `agriculture/FloorPlantBlock`.
- Textures drawn by code in `tools/wild_textures.py`. No Mojang file is copied.

## Verification
Results are recorded under "Results" below after CI runs.
- `python3 tools/check_mod_data.py`: biomes and their features, the generated placements current, Java placing them, no Nether biome with region rules, an Overworld surface or seasons.
- Server game test (`BiomeGameTests.dimensionBiomesArePlaced`): every Nether biome is in the Nether's biome source, and at least half are found within 3,200 blocks of the origin (distances logged).
- Client game test (`BiomeClientGameTests`): each Nether biome's nearest place to the origin, a standing spot on its floor, and a screenshot.
- Not run: play, a dedicated server, two clients.

### Results
Not yet run in CI.

## World and event applicability
- Biome fit: each takes a climate point in the Nether, next to vanilla's.
