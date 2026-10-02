# Wonders and caves (biomes batch 7)

Status: in progress on branch `claude/biomes`, with the region engine ([biome-regions.md](biome-regions.md)). Awaiting CI. **Not yet played.**
Proposal issue: none. The owner asked on 2 October 2026 to remake the Biomes O' Plenty catalog's biomes ([branches/BIOMES.md](../branches/BIOMES.md)) and to carry on through every batch. Everything here is original: the catalog guided the concepts only, and its invented names are replaced by our own.
Owner: @jimbozoomer-byte
Target milestone and tier: world generation (Discovery).
Primary specialty and supported player role: exploration; decoration (glowing plants).

## Player experience
Fifteen rare, strange lands and two cave biomes, spread over all four layouts of Jugcraft regions on climates the earlier batches left free, so each layout has a few wonders:

| Biome | Layout: replaces | What it is |
| --- | --- | --- |
| **Cinder Barrens** | meadow: badlands | a burnt-out waste of ash-grey tuff and gravel, smouldering with magma, blood-red water, lava pools, ash in the air; no animals |
| **Elder Vale** | meadow: mushroom islands | a vale of the old world: bright, simple grass, plain oaks, poppies and dandelions under a clear sky; no monsters spawn |
| **Frostlight Garden** | woodland: dry snowy plains | firs and birches in the snow, frost irises and glimmerblooms, a cold, shimmering sky; no monsters spawn |
| **Gilded Shrubland** | wetland: savanna plateaus | golden grass and golden-leaved oak scrub, goldenrod, dry grass, little lakes |
| **Glimmer Grove** | wild: flower forests | jacarandas and giant red mushrooms, glimmerblooms, alliums, lilacs and hydrangeas under pink air, pink water; only witches spawn |
| **Gloomweald** | wild: dark forests | dark oaks, dead trees and giant mushrooms over leaf litter and toadstools, dark purple pools, a dim sky |
| **Glowcap Grotto** | wetland: dripstone caves | a cave of mud floors grown with **glowcaps** that light the dark, moss and glow lichen |
| **Hallowed Bog** | meadow: swamps | a pale, bright bog of willows and vine-hung oaks, lilies of the valley and daisies, clear blue water; no monsters spawn |
| **Highsun Meadow** | wetland: savannas, warm plains | sunny, golden-green grass, sunflowers, goldenrod and wildflowers, a few small oaks |
| **Mycelial Jungle** | wild: jungles | huge red and brown mushrooms, jungle bushes and oaks over grass and mycelium, toadstools, spore-green air; mooshrooms |
| **Shrine Springs** | wild: bamboo jungles | great oaks two blocks wide, warm pools banked with calcite, ferns, dark green grass |
| **Snowpetal Grove** | wild: dry snowy plains | blossoming cherries and birches in the snow, snowpetals and clover, mossy boulders, snowflakes on the air |
| **Spider Nest** | wild: dripstone caves | a cave strung with cobwebs from ceiling to floor; spiders and cave spiders |
| **Starlit Wood** | meadow: dark forests | soaring birches and tall firs, glimmerblooms and lilies of the valley, motes of light under a twilight-blue sky |
| **Toadstool Field** | woodland: mushroom islands | mycelium patched with grass, giant mushrooms, toadstools and glowcaps; mooshrooms, and no monsters, as on vanilla's mushroom islands |
| **Webwood** | meadow: mangrove swamps | willows, dead trees and vine-hung oaks strung with cobwebs, toadstools and cattails, grey air; spiders everywhere |
| **Wild Greens** | wetland: warm forests | a riot of tall grass, clover, ferns and wildflowers over swathes of coarse dirt; no trees; cattle, sheep and chickens |

- Biomes O' Plenty's names become ours: the Deadlands become the Cinder Barrens, the Origin Valley the Elder Vale, the Auroral Garden the Frostlight Garden, the Sublime Shrubland the Gilded Shrubland, the Mystic Grove the Glimmer Grove, the Ominous Woods the Gloomweald, the Glowing Grotto the Glowcap Grotto, the Blessed Bog the Hallowed Bog, the Majestic Meadow the Highsun Meadow, the Fungal Jungle the Mycelial Jungle, the Sacred Springs the Shrine Springs, the Snowblossom Grove the Snowpetal Grove, the Wondrous Woods the Starlit Wood, the Fungal Field and Fungi Forest the Toadstool Field, the Silkglade the Webwood and the Overgrown Greens the Wild Greens.
- Seasons: the Hallowed Bog and Wild Greens change colour with the seasons and take winter snow when `seasons.snow` is on. The others keep their own colours all year.
- **New plants:**
  - Glowcap: a cluster of pale mushrooms with glowing caps (light 10). It grows on any sturdy floor: stone, mud, deepslate or soil.
  - Glimmerbloom: a violet, star-shaped flower that glows softly (light 7). Magenta dye; suspicious stew gives glowing.
  - Frost iris: a pale icy-blue iris. Light blue dye; suspicious stew gives slow falling.
  - Snowpetals: white petals on the ground, like pink petals.
- **New tree shape:** the great oak, a trunk of vanilla oak two blocks wide under a huge round crown (no sapling grows it).

## Connections
- Input producer: world generation.
- Output consumer: decoration and light (glowcaps and glimmerblooms give light without power), dye, composting, mooshrooms (stew) in the Mycelial Jungle and Toadstool Field.
- Technology connection: none new.
- Magic connection: none yet. These are candidates for the magic branch's ingredients and sites ([CONTENT_BRANCHES.md](../CONTENT_BRANCHES.md)); nothing depends on them yet.
- Reachable entry path: each is rare by design, but every one replaces a vanilla climate that exists in most worlds. The two caves replace dripstone caves, which are common.
- Required vs optional: nothing here is required.

## Balance and automation
- Glowcaps and glimmerblooms are light sources found in the wild, dimmer than a torch (14). They are only light; nothing is crafted from them yet. Cobwebs in the Spider Nest and Webwood are vanilla cobwebs (a sword gives string, shears the web). No new conversions, no loops.
- Spawning: some biomes change their monster list (none in the Elder Vale, Frostlight Garden and Hallowed Bog; only witches in the Glimmer Grove; mostly spiders in the Spider Nest and Webwood). Mob caps are vanilla's.

## Multiplayer and persistence
- Server authority: world generation and spawning are the server's.
- `biomes.enabled=false` stops the biomes in new chunks and the new plants' recipes. Every block, item and biome stays registered.
- Existing worlds: new chunks only.

## Dependencies and assets
- Glowcaps are `agriculture/GlowPlantBlock` (any sturdy floor, gives light). Glimmerbloom and frost iris are vanilla's `FlowerBlock`, the glimmerbloom with a light level; snowpetals are `GroundCoverBlock`.
- Cave features follow vanilla's lush caves: floors and ceilings found by scanning from random heights. The grotto's mud is vanilla's vegetation patch feature, its moss vanilla's moss patch. Cobwebs are vanilla's simple block feature. Giant mushrooms are vanilla's, placed where an oak sapling could stand.
- Air colours, particles (vanilla's white ash, snowflakes and end rod motes) and water colours are biome attributes.
- Textures drawn by code in `tools/wild_textures.py`. No Mojang file is copied.

## Verification
Results are recorded under "Results" below after CI runs.
- `python3 tools/check_mod_data.py`: plants, biomes and their features, non-overlapping rules, generated rules and surfaces current.
- Server game tests (`BiomeGameTests`): `glowingPlantsWork` (glowcaps glow at 10 on bare stone; the glimmerbloom glows at 7 and the frost iris not at all, both with potted forms; four snowpetals drop four); `regionLayoutFollowsItsRules` checks every batch 7 rule.
- Client game test (`BiomeClientGameTests`): distances from the start of a real world (seed `jugcraft`), and screenshots of each surface biome found. The two caves are found by the 3D search but not photographed.
- Not run: play, a dedicated server, two clients.

### Results
Not yet run in CI.

## World and event applicability
- Biome fit: each biome takes the climate and land shape of the vanilla biome it replaces.
- Seasons: see above.
