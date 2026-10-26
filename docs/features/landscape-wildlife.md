# Wildlife in the landscape biomes

Status: implemented on `claude/intelligent-mendel-yh3cui` ([#476](https://github.com/jimbozoomer-byte/jugcraft/pull/476)), stacked on the seasonal biome audit ([#454](https://github.com/jimbozoomer-byte/jugcraft/pull/454)); awaiting review. Its CI passed (below). **Not yet played.**
Proposal issue: none. This is the wildlife follow-up the seasonal biome audit recorded ([seasonal-biome-audit.md](seasonal-biome-audit.md#rollout-and-open-questions)). On 26 October 2026, offered it as the next work, the owner answered "Lets do the wildlife follow-up".
Owner: @jimbozoomer-byte
Target milestone and tier: world generation and ecology; no tier.
Primary specialty and supported player role: exploration, hunting, taming and riding; every player meets the creatures.

## What was found
- **The menagerie places its creatures by habitat** (`menagerie/Menagerie.habitat`): the wildlife, beasts, fables, companions, mounts, grove creatures, fauna, the rotten and the landscape collection's own cave creatures, 277 species in all. Each lives in one habitat (forest, swamp, ocean, desert and so on), which it finds through vanilla's biome tags (`#minecraft:is_forest`, `#minecraft:is_ocean` and the like) and a few biome names. Before anything else it asks the dimension's tag: `#minecraft:is_overworld`, `#minecraft:is_nether` or `#minecraft:is_end`.
- **The landscape collection carries none of those tags.** Its 159 biomes (`tools/landscape_kinds.py`) take about a quarter of each Jugcraft region, and their Nether and End biomes join those dimensions. Being in none of the dimension tags, they held **no menagerie species at all**: no deer in the landscape woods, no alligators in its swamps, no lions on its savannas, no whales in its ocean. Not even the landscape collection's own cave creatures (the gob, the garch and the rest) lived in its caves.
- **A correction to the audit's note:** it counted "13 species" reading `#c:is_swamp`. Those are the 47 `#jugcraft:wildlife_has_*` and `blacklist/wildlife_blacklist_*` tag files, which came with the wildlife and which no code reads. The menagerie never used them, so they are left as they were.
- **Also found, and not empty:** 95 of the 159 landscape biome files list no creatures or monsters of their own (`minecraft:gameplay/natural_mob_spawns`); among them the Sakura Grove, the Cloud Forest, the Lavender Forest and the Highlands. In game they are not empty: the game test's log shows 15 spawn entries in the Sakura Grove besides the menagerie's, as many as in vanilla's forest. They come from outside the biome's file.

## Player experience
The menagerie's creatures now come to the landscape biomes as they come to vanilla's and Jugcraft's own: 195 Overworld species by habitat, 14 Nether and 8 End species.
- **By each biome's kind:**

  | Landscape kind | Habitat | For example |
  | --- | --- | --- |
  | woods (forest, taiga, the Ominous Woods) | forest | deer, bears, boars, hedgehogs in the Maple Woods, the Redwood Forest, the Siberian Taiga |
  | open country | plains | rats and bison in the Field, the Prairie, the Steppe |
  | hills | mountain | eagles, griffons and gelada monkeys on the Crag, the Highlands, the Rocky Mountains |
  | swamps | swamp | alligators, capybaras, dragonflies in the Bayou, Bog, Marsh, Wetland |
  | tropics | jungle | tigers, komodo dragons, piranhas in the Tropics, the Rainforest, the Floodplain |
  | beaches | beach | crabs, clams, starfish on the Dune and Gravel Beaches |
  | the warm ocean | ocean and deep ocean | rays, jellyfish, whales, blobfish |
  | the warm river | river | ducks, bass, otters, hippos |
  | the Skylands | plains | as open country |
  | the landscape Nether and End | nether, end | as those dimensions |

- **Arid land, biome by biome:** open sand is desert (scorpions, lizards, snakes: the Desert Canyon, the Lush Desert, the Wasteland); dry grass and scattered trees are savanna (lions, elephants, giraffes, zebras: the Lush Savanna, the Brushland, the Scrubland); red and painted clay is badlands (vultures: the White Mesa, Bryce Canyon, the Painted Mountains). The Savanna Badlands is both savanna and badlands.
- **Magic land, by its ground:** the Mirage Isles, the Moonlight Grove and the Mystic Grove are woods; the Alpha Islands and the Origin Valleys open country; the Amethyst Canyon and Rainforest jungle; the Moonlight Valley and the Auroral Garden flowered (meadow).
- **Flowered land, as vanilla's meadow and flower forest:** the Blooming Plateau and Valley, the lavender biomes, the Sakura Valley and the Overgrown Greens take the meadow's butterflies, ladybugs and hummingbirds besides. The Forested Highlands are forest as well as mountain.
- **By the climate, as everywhere:** snowy creatures (mammoths, yetis, penguins, moose) in any landscape land below 0.15; cave creatures beneath any landscape land; the storm's creature in any landscape land in a thunderstorm. Volcanic land and the coastal cliffs hold these alone, as no vanilla kind matches them.
- **Nothing else changes:** the landscape biomes still lack vanilla's and the common kind tags, so trees, crops, structures and other mods' content there are as they were.

## Connections
- **Existing input producer:** the menagerie's species and their habitats (`jugcraft/menagerie/*.json`), and the landscape kinds from the seasonal biome audit.
- **Existing output consumer:** the biomes' natural spawns, which Fabric's biome modifications add the menagerie's creatures to, and so hunting, taming, riding and the creatures' drops (antlers, eggs, glow goop and the rest).
- **Technology and magic connections:** none new.
- **Reachable entry path:** none needed; the creatures spawn naturally.
- **Shared tags:** `tools/landscape_habitats.py` writes `#jugcraft:landscape_habitat/<habitat>` for the 15 habitats Menagerie reads from tags (forest, plains, meadow, mountain, swamp, jungle, beach, ocean, deep ocean, river, desert, savanna, badlands, nether, end) and `#jugcraft:landscape_habitat/overworld` for the 150 landscape biomes of the Overworld.

## Balance and automation
No item, recipe, cost or reward changes. Each species spawns in the landscape biomes with the weight and group size it already has elsewhere, under the game's own mob caps. The landscape biomes now hold creatures where before they held none of the menagerie's.

## Multiplayer and persistence
- Nothing new is saved and nothing new is sent. The spawns are the server's, chosen when it starts.
- Disabling agriculture (`agriculture.enabled=false`) still stops every menagerie spawn, here as elsewhere.
- Old chunks take the creatures at once: natural spawning reads the biome's spawn list, not the chunk.

## Dependencies and assets
No new dependencies or art.
- **Data:** `tools/landscape_habitats.py` (new), from which `tools/generate_material_data.py` writes the 16 `#jugcraft:landscape_habitat/*` tags.
- **Java:** `menagerie/Menagerie.habitat` reads those tags for the landscape biomes (`landscape(c, habitat)`); vanilla's and Jugcraft's own biomes are decided as before.

## Verification
- **Run locally (26 October 2026):**
  - `python3 tools/generate_material_data.py`: wrote the 16 tags.
  - `python3 tools/check_mod_data.py`: PASS (6240 material IDs).
  - **Deliberate breaks:** seven, each in a scratch run of `check_landscape_habitats`, all caught: an arid biome with no habitats, an untagged habitat, a tag that differs from the table, a non-landscape biome in the table, a species habitat neither tagged nor ruled, Menagerie not reading the tags, and Menagerie missing a landscape rule.
  - `python3 scripts/check_repository.py`: PASS. `python3 -m unittest discover -s scripts/tests`: 191 tests passed.
  - **Java:** a syntax parse only: 0 errors. Compilation and the game test run in CI.
- **Checks:** `tools/check_mod_data.py`'s new `check_landscape_habitats` checks that:
  - every landscape biome has its habitats, each one Menagerie reads from a tag;
  - the tags are written as `tools/landscape_habitats.py` says, and the landscape deep ocean is ocean too;
  - every habitat a menagerie species lives in is either tagged or one Menagerie decides by its own rule (snowy, cave, storm, summoned, mushroom), so a new habitat cannot be missed;
  - `Menagerie.habitat` reads the tags and has a rule for each of those habitats in the landscape biomes.
- **Game test** `LandscapeWildlifeGameTests.landscapeBiomesHoldTheirWildlife`: over the test, `/fillbiome` makes each of 14 biomes in turn, and the creatures that may spawn there (the biome's natural mob spawns) are read:
  - the Maple Woods: deer and the gob, not alligators or lions;
  - the Sakura Grove, whose file lists no creatures: deer;
  - the Bayou: alligators, not deer;
  - the Lavender Field: rats and butterflies, not deer;
  - the White Mesa, the Lush Savanna and the Desert Canyon: vultures, lions and scorpions, each not the others';
  - the Tropics: tigers;
  - the Deep Warm Ocean: whales and rays, not deer or the gob;
  - the Wintry Forest: deer and mammoths;
  - the Volcano: the gob, not deer or mammoths;
  - the Erupting Inferno and the End Wilds: the netherbat and the endergrade, not deer;
  - vanilla's forest: deer still, not alligators.
  - It logs, for each, how many spawn entries are the menagerie's and how many are not.
- **In CI** ([run 40363306807](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/40363306807), on e1a441dd1): every check passed on its first run: the `mod` job (the build and the server game tests), the job without the optional integrations, the repository check and all sixteen client shards.
  - **The `mod` job:** all 2264 required server game tests passed, `LandscapeWildlifeGameTests` among them.
  - **Spawn entries** (the menagerie's, then the others) in each biome the test made:

    | Biome | Menagerie | Others |
    | --- | --- | --- |
    | Maple Woods | 54 | 16 |
    | Sakura Grove | 54 | 15 |
    | Bayou | 33 | 14 |
    | Lavender Field | 29 | 17 |
    | White Mesa | 18 | 12 |
    | Lush Savanna | 30 | 22 |
    | Desert Canyon | 34 | 12 |
    | Tropics | 36 | 17 |
    | Deep Warm Ocean | 25 | 16 |
    | Wintry Forest | 66 | 17 |
    | Volcano | 16 | 10 |
    | Erupting Inferno (Nether) | 15 | 6 |
    | End Wilds | 8 | 0 |
    | vanilla's forest | 54 | 15 |

  - The landscape woods now hold as many of the menagerie's creatures as vanilla's forest (54 entries each).
- **Not run:** a world, a dedicated server and play.

## World and event applicability
- Creatures only; nothing generates differently.
- Seasonal and event creatures (werewolves, squirrels, turkeys, wisps) are the audit's and unchanged.
- The landscape caves hold cave creatures by the same rule as vanilla's caves: their spawn rules keep them below height 45.

## Rollout and open questions
- **Owner choices,** each one line in `tools/landscape_habitats.py`:
  - which arid biomes are desert, savanna or badlands;
  - which magic biomes are woods, open country, flowered or jungle;
  - which fields are flowered (meadow besides);
  - volcanic land and the coastal cliffs holding only the climate's creatures;
  - the Skylands as open country.
- **The 95 landscape biome files with no creatures or monsters of their own:** in game they have other spawns (15 entries in the Sakura Grove, as in vanilla's forest), so nothing is needed for them here. Giving them their own, as the other 64 have, is a separate choice.
- **The unused `#jugcraft:wildlife_has_*` tags:** left in place; they could be removed, or the menagerie could read them, in a later change.
- **Owner check:** walk a landscape wood, swamp and savanna and see their creatures.
