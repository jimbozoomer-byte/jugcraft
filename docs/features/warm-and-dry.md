# Warm and dry lands (biomes batch 4)

Status: in progress on branch `claude/biomes`, with the region engine ([biome-regions.md](biome-regions.md)). Awaiting CI. **Not yet played.**
Proposal issue: none. The owner asked on 2 October 2026 to remake the Biomes O' Plenty catalog's biomes ([branches/BIOMES.md](../branches/BIOMES.md)) and to carry on through every batch. Everything here is original: the catalog guided the concepts only.
Owner: @jimbozoomer-byte
Target milestone and tier: world generation and building (Discovery).
Primary specialty and supported player role: exploration; building (palm and cypress wood).

## Player experience
Fifteen warm or dry biomes. The meadow layout takes the dry grasslands and sandy country, the wild layout the harsher deserts and scrub, and the woodland layout two warm forests:

| Biome | Layout: replaces | What grows |
| --- | --- | --- |
| **Dryland** | meadow: dry savanna | hot, dry grassland under a lilac sky: pines, oaks and brush oaks, bushes, cacti, pillars of bone; desert villages |
| **Xeric Shrubland** | meadow: savanna plateau | low hills of sand and grass, small acacias, cacti, dead shrubs, little lakes |
| **Jacaranda Glade** | meadow: warm forest | blossoming jacarandas with cherries, oaks with bees and azaleas; blue orchids and lilies of the valley |
| **Lush Desert** | meadow: desert (one half) | orange dunes of red sand that see rain: dune grass, acacia brush, bushes, wildflowers; villages |
| **Bone Flats** | meadow: desert (other half) | dry, coarse flats with pillars of bone, dead bushes and dry grass |
| **Dry River** | meadow: hot rivers | a river between sandy banks with dead bushes |
| **Cold Desert** | meadow: dry snowy plains | gravel and coarse dirt, dry grass, hidden powder snow; no animals; snowy villages |
| **Scrubland** | wild: dry savanna | flat scrub of dry grass and wildflowers, scattered oak bushes |
| **Lush Savanna** | wild: savanna | a field of poppies and rose bushes on grass blotched with coarse dirt; no trees, no animals |
| **Outback** | wild: desert (one half) | red sand patched with grass, tiny acacias and cacti, pools of water and lava; villages |
| **Oasis** | wild: desert (other half) | sand around pools of water, palms, grass and sugar cane |
| **Wasteland** | wild: badlands | dried salt flats (calcite) with rock-salt outcrops, dead trees and dead grass; no animals; husks |
| **Burnt Forest** | wild: warm forest | charred dead trunks on scorched grass and coarse dirt, ash drifting in the air |
| **Mediterranean Forest** | woodland: warm forest | tall cypresses, oaks and dark oaks, shrubs, peonies; villages |
| **Orchard** | woodland: temperate plains | the agriculture branch's chestnut trees, oaks with bees and flowering azaleas, rose bushes, daisies |

- Biomes O' Plenty's Seasonal Orchard merges into the Orchard (and its yellow trees are the Aspen Glade's, batch 1).
- Seasons: the Jacaranda Glade, Mediterranean Forest and Orchard change colour with the seasons (the Orchard also takes winter snow when `seasons.snow` is on); the rest are tropical, arid or frozen and do not.
- **New trees:**
  - The palm: a tall trunk that bends as it rises, a flat crown of fronds. It grows on sand as well as grass.
  - The cypress: a tall, narrow column of dark scale-leaves.
  - Both have full wood sets and saplings. Small desert acacias use vanilla acacia wood.
- **Ground:** these biomes lay their own ground where the climate calls for it: red sand and red sandstone, sand, coarse dirt, gravel with powder snow, and calcite salt flats.

## Connections
- Input producer: world generation.
- Output consumer: building (palm and cypress wood); rock salt (the salt ore) shows at the surface in the Wasteland (the same ore, which needs a stone pickaxe); the agriculture branch's chestnuts grow wild in the Orchard.
- Technology connection: the sawmill and tree farm take palm and cypress like the other woods.
- Magic connection: none yet.
- Reachable entry path: savannas and deserts are common; each layout is a quarter of Jugcraft regions.
- Required vs optional: nothing here is required. The Wasteland's surface salt is a convenience; salt also comes from rock salt underground and from brine, as before.

## Balance and automation
- Palm and cypress are ordinary wood. Rock salt outcrops are small and rare (a few blocks every few chunks of Wasteland) and drop what rock salt ore always drops. No new conversions, no loops.

## Multiplayer and persistence
- Server authority: world generation is the server's.
- `biomes.enabled=false` stops the biomes in new chunks and the new woods' hand recipes. Every block, item and biome stays registered.
- Existing worlds: new chunks only.

## Dependencies and assets
- **Surfaces:** Jugcraft's material rule (`jugcraft:overworld/surface`, generated from the biomes' "surface" in `tools/biomes.py`) sets the floor and the layer under it, with noise patches, in Jugcraft biomes that ask for it. It runs first through a small override of vanilla's top-level `minecraft:overworld` material rule, which lists vanilla's named parts by reference with Jugcraft's added. Another mod or data pack that also replaces `minecraft:overworld`'s top-level rule would conflict with this one (the last loaded wins); vanilla's own surface rules are not touched.
- Trees use vanilla's bending, forking and straight trunk placers and acacia and spruce foliage. Textures drawn by code in `tools/wild_textures.py`. No Mojang file is copied.

## Verification
Results are recorded under "Results" below after CI runs.
- `python3 tools/check_mod_data.py`: trees, biomes and their features, non-overlapping rules, generated rules and surfaces current.
- Server game tests (`BiomeGameTests`): `warmTreesGrow` grows a palm and a cypress from saplings (sizes logged); `regionLayoutFollowsItsRules` checks every batch 4 rule.
- Client game test (`BiomeClientGameTests`): distances from the start of a real world (seed `jugcraft`), and screenshots of each biome found, which show whether the surfaces generate.
- Not run: play, a dedicated server, two clients.

### Results
Not yet run in CI.

## World and event applicability
- Biome fit: each biome takes the climate of the vanilla biome it replaces.
