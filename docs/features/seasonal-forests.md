# Seasonal forests (biomes batch 1)

Status: in progress on branch `claude/biomes`, with the region engine ([biome-regions.md](biome-regions.md)). Awaiting CI. **Not yet played.**
Proposal issue: none. The owner asked on 2 October 2026 to remake the Biomes O' Plenty catalog's biomes ([branches/BIOMES.md](../branches/BIOMES.md)) and chose the seasonal forests as the first batch. Everything here is original: the catalog guided the concepts only.
Owner: @jimbozoomer-byte
Target milestone and tier: world generation and building (Discovery).
Primary specialty and supported player role: exploration and building; every player meets them.

## Player experience
Nine biomes grow in Jugcraft regions, each in place of the vanilla biome with the same climate:

| Biome | Replaces (in Jugcraft regions) | What grows |
| --- | --- | --- |
| **Coniferous Forest** | cool taiga | dense firs, with tall firs towering over them, some spruces, fallen fir logs |
| **Snowy Coniferous Forest** | snowy taiga | firs under permanent snow |
| **Maple Woods** | cool forest | maples (and big maples), some spruces |
| **Seasonal Forest** | temperate forest | oaks, maples and aspens mixed, with leaf litter and dense pumpkin patches |
| **Aspen Glade** | birch forests | tall, slender aspens, a few maples |
| **Dead Forest** | dry cool plains | grey dead trees, a few spruces and oaks, dead bushes, sparse brown grass; rabbits and wolves only |
| **Tundra** | cool plains | no trees: maple scrub, mossy boulders, dead bushes, ferns; rabbits, foxes and wolves |
| **Snowy Forest** | snowy plains (moister) | snow-covered oaks, firs and maples |
| **Muskeg** | snowy plains | dead trees and stunted firs in wet, snowy flats; rabbits only |

**The trees follow the seasons** (the server's season clock, [seasons.md](seasons.md)):
- **Maples** are green in spring and summer, turn red, orange and gold in autumn (each leaf block its own colour, so crowns are fiery mixes), and stand bare in winter.
- **Aspens** turn gold and then drop their leaves.
- **Firs** stay green all year.
- Each block turns within a week either side of its date, so a forest changes gradually; with seasons off, every tree stays green.
- The six four-season biomes (all but the snowy three) also change grass and leaf colours with the seasons, and take winter snow when `seasons.snow` is on.

**New wood:** maple (grey bark, rosy planks), aspen (white bark with black eyes, pale planks), fir (dark bark, golden planks) and dead wood (weathered grey). Each has a full wood set: log, wood, stripped log and wood, planks, stairs, slab, fence and fence gate. Maple, aspen and fir drop saplings from their leaves.

## Connections
- Input producer: world generation (Jugcraft regions).
- Output consumer: building (four new wood sets); the sawmill turns any of their logs into 6 planks with sawdust; the tree farm grows maple, aspen and fir saplings into 6 logs; every vanilla wood recipe and fuel use takes the new wood through vanilla's tags.
- Agriculture: the Seasonal Forest's pumpkin patches; the forests join `#minecraft:is_forest` or `#minecraft:is_taiga`, so the agriculture branch's wild crops and chestnut trees that grow in forests grow here too.
- Technology connection: the sawmill and tree farm (above).
- Magic connection: none yet.
- Reachable entry path: Jugcraft regions are common; each biome replaces a common vanilla one.
- Required vs optional: nothing here is required; it is wood, scenery and food.

## Balance and automation
- The new woods are ordinary wood: 4 planks per log by hand, 6 in the sawmill, a sapling in gives 6 logs and the sapling back in the tree farm, as for vanilla trees. No new conversions, no loops.
- Dead wood has no sapling: it comes only from Dead Forests and Muskegs.
- The leaves' look is cosmetic; drops, decay and fire are vanilla's in every season.

## Multiplayer and persistence
- Server authority: world generation and the season clock are the server's; clients only see block states.
- Seasonal leaves update on random ticks, a changed leaf bringing up to 128 touching leaves in loaded chunks with it; placed or grown leaves start in today's look (as for the larch, [alpine-spawn.md](alpine-spawn.md)).
- `biomes.enabled=false` stops the biomes in new chunks (no Jugcraft regions) and the new woods' hand recipes. Every block, item and biome stays registered, so old chunks and inventories keep them.
- Existing worlds: new chunks only; see [biome-regions.md](biome-regions.md).

## Dependencies and assets
- Textures drawn by code in `tools/forest_textures.py` (wood, leaves in every look, saplings); nothing read, traced or recoloured.
- Biomes start from the vanilla biome they replace, by reference (`tools/biome_bases.py`: its ore, cave, lake and spring features, mobs, music and sky), with their own trees and plants (`tools/biomes.py`, `tools/trees.py`). No Mojang file is copied.
- The larch's one-off seasonal leaves became a general `SeasonalLeavesBlock`, shared by larch, maple and aspen; the larch's blocks, states and generated files are unchanged.

## Verification
Results are recorded under "Results" below after CI runs.
- `python3 tools/check_mod_data.py`: every tree, wood set and leaf schedule is registered as the data says; every fixed season mode shows only its own look for every tree, whatever the jitter; every biome's features, tree picks, name, tags and seasons exist; the region rules match.
- Server game tests (`BiomeGameTests`): maple, aspen and fir saplings grow (sizes logged), grown in autumn all their leaves in autumn colours (the fir's evergreen); the dead tree grows with no leaves; every seasonal tree's leaves follow every season mode; the new logs strip with an axe and the woods are in vanilla's tags. The larch's tests (`AlpineGameTests`) now run on the shared leaves.
- Client game test (`BiomeClientGameTests`): distances from the start of a real world (seed `jugcraft`) to each biome (logged); screenshots of Maple Woods, Seasonal Forest, Aspen Glade, Coniferous Forest, Dead Forest and Tundra from above.
- Not run: play, a dedicated server, two clients, the look of each season in play (only fixed modes are tested).

### Results
Not yet run in CI.

## World and event applicability
- Biome fit: each biome takes the climate of the vanilla biome it replaces, so it borders what that biome bordered.
- Seasons: Coniferous Forest, Maple Woods, Seasonal Forest, Aspen Glade, Dead Forest and Tundra are four-season biomes (`#jugcraft:has_seasons`, `#jugcraft:has_winter_snow`); the snowy three are always frozen.
- Biomes O' Plenty's snowy variants of temperate biomes come from Jugcraft's winter instead (see the roster).

## Rollout and open questions
- Planned for later in this batch or the next: muskeg ponds that freeze in winter; toadstools; violets.
- Open: should Dead Forests have no animals at all, as the catalog says? (They have rabbits and wolves here.)
