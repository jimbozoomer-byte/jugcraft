# Fall Garden & Porch

Status: part 1 (the fall garden's plants) implemented. Parts 2 to 4 are planned: planters, the porch's pieces, and string lights that light along their length. CI builds it and runs its game tests and client screenshots; not yet played by hand.
Proposal issue: none. The owner asked for it directly on 28 October 2026, as set 4 of the ten sets of fall and Halloween decor ("Then I want you to do 1-10 in parts in that order one by one"), drawn by their rules.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery tier; plants found in the wild or made from a seed or a flower.
Primary specialty and supported player role: farming and building. A grower plants a fall border and a builder dresses a porch with it. The wild plants are the biomes branch's kind (`tools/plants.py`), so they also fill out vanilla's plains and forests in the fall colours.

## Player experience

### Part 1: the fall garden's plants
Seven plants, each sculpted in the graveyard flora's hand-built style: stems of small boxes carrying cut-out leaves and petals at angles, with the flower heads, seed heads, plumes and berries as little boxes. Small ones turn at random where they stand, so a bed of them doesn't repeat.
- **New England Aster** (a small flower): a bushy clump 65 cm high, leafy all the way up, each of its seven stems crowned with a spray of violet daisies with golden eyes.
  - Found in plains and forests. Makes purple dye. In suspicious stew: Luck, 6 seconds.
- **Autumn Stonecrop** (a small flower; the 'Autumn Joy' sedum): thick stems set with round, fleshy grey-green leaves, under broad flat heads of florets turned brick-rose by the cold. 50 cm high.
  - Found in plains, on dry meadow ground. Makes pink dye. In suspicious stew: Resistance, 4 seconds.
- **Ornamental Kale** and **Ornamental Cabbage** (small flowers): rosettes like great flowers, 50 cm across and 20 cm high. Broad outer leaves rise at their tips into a heart of colour: kale's leaves are frilled, with a magenta heart in blue-green leaves; cabbage's are smooth, with a cream heart blushed pink in green leaves.
  - Garden plants, not wild ones: a cabbage seed and purple dye make a kale, and a cabbage seed and bone meal a cabbage.
  - They make magenta and white dye; in suspicious stew, Haste and Jump Boost, 5 seconds each.
- **Pampas Grass** (a tall flower, two blocks): a fountain of long arching blades, with five stems rising 2 m to feathery cream plumes.
  - Found in plains: it is a grassland plant, as the pampas are. Makes two white dye; bone meal drops a copy, as other tall flowers do.
- **Dried Sunflower** (a two-block plant): a sunflower left standing to dry. A stout brown stalk with its leaves hanging limp and withered, and its head, heavy with seed, bowed over: a seeded face in a rim of shrivelled petals, 37 cm across.
  - A sunflower makes one.
- **Bittersweet** (a vine): American bittersweet twining up a wall, with butter-yellow leaves and clusters of scarlet berries in split orange husks.
  - It covers any face of a block, several at once, as Creeping Ivy does; only shears take it, one for each face, and bone meal spreads it.
  - Found climbing logs and stone in forests.
- Every small flower has its potted form. All of them compost and burn like vanilla's flowers.

## Changes from the idea list
- The idea list put **goldenrod** and **cattails** in set 4. Both grow already (the biomes branch's batches 2 and 3), so they are not built again.
- The dried sunflower is **two blocks** tall, as a vanilla sunflower is, not three. A three-block plant would need a block of its own kind for one plant, and its head would reach into a block that could hold something else.
- Ornamental kale and cabbage are made from cabbage seeds (with purple dye or bone meal), not grown as crops of their own.

## How they are drawn (docs/ART_DIRECTION.md, docs/NATURAL_TEXTURES.md)
- Sculpted as the graveyard flora is (`tools/fall_garden_flora.py`, on `tools/flora_art.py`, written by `tools/flora_data.py`), each with its own 64 by 64 texture. They are painted in the clean style, with no per-pixel random tones.
- Sizes are real ones, at a block a metre.
- Each material is one colour stepped into tones:
  - the aster's violet rays and golden eyes;
  - the stonecrop's brick-rose heads and grey-green leaves;
  - the kale's magenta heart in blue-green leaves, the cabbage's cream heart in green ones;
  - the pampas plumes' cream, the blades' sage;
  - the sunflower's dried browns;
  - bittersweet's scarlet berries, orange husks and yellow leaves.
- **Marks follow the material.** The stonecrop's and the pampas plumes' marks are rows: a lit floret every other texel, and a lit row every other row. The sunflower's face is rings of seed, a darker ring every third, inside its petal rim.
- **The first previews**, all fixed before commit:
  - the asters were bare stems with a few tiny heads; they now have seven leafy stems with three heads each;
  - the kale and cabbage rosettes were 70 cm across, then too small and flat with a cube for a heart; they are now 50 cm across, in two rings round a cupped heart;
  - the sunflower's head tipped up to the sky, then read as a plank; it now bows face-down to the south, a seed head 12 cm deep with a domed back;
  - bittersweet's stem was five loose sticks; it is now one twining stem painted on its sheet, with the leaves and berries along it.

## Connections
- Existing input producer: the wild ones are found in vanilla's biomes. The kitchen garden's cabbage seeds, with purple dye or bone meal, make the ornamental cabbages, and a vanilla sunflower makes the dried one.
- Existing output consumer: dyes, suspicious stew, flower pots, composters, bees (they are flowers). Set 4's planters (part 2) will take them.
- Technology/magic connections (or infrastructure/cosmetic applicability): cosmetic, and the biomes branch's wild plants.
- Reachable entry path: from the first day: the wild plants are found, and cabbage seeds come from wild cabbage.
- Which connections are required vs optional; trade and solo routes: all optional, solo-found or solo-crafted, and tradeable like any item.
- How this specialty stays useful without mastering every other branch: they are plants for anyone's garden.

## Balance and automation
- The flowers give one dye each (pampas two), as vanilla's do.
- **No loop.** A kale costs a cabbage seed and a purple dye and gives one magenta dye; a cabbage costs a seed and a bone meal and gives one white dye. Neither takes the dye it gives, so no recipe leads back to itself (the audit refuses any loop).
- A dried sunflower costs a sunflower (two yellow dye) and gives no dye.

## Multiplayer and persistence
- Plain blocks, registered from the generated `/jugcraft/plants.json` with the other wild plants; nothing ticks but what vanilla's flowers and glow lichen do.
- IDs (stable from release): `aster`, `sedum`, `ornamental_kale`, `ornamental_cabbage` (each with its `potted_` block), `pampas_grass`, `dried_sunflower`, `bittersweet`. Placed features: `patch_aster`, `patch_sedum`, `patch_pampas_grass`, `patch_bittersweet`.
- The dye and stew recipes and the garden plants' recipes follow the biomes switch, the cabbages' the agriculture switch too (the seed is the kitchen garden's). The blocks stay registered when either is off.

## Dependencies and assets
- No new dependency. Models, blockstates and items are generated by `tools/flora_data.py` from `tools/fall_garden_flora.py`; textures by `tools/generate_textures.py`. Nothing is copied, traced or recoloured from Minecraft's textures.

## Verification
Local checks run on this branch, and their results:
- `python3 tools/check_mod_data.py`: PASS (8,105 material IDs, from 8,094), including the new `check_fall_garden` and the graveyard flora's checks of every sculpted plant (64 by 64 textures, turns block models allow, -16 to 32).
- `python3 scripts/check_repository.py`: PASS.
- `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
- `python3 tools/check_icon_maps.py`: PASS (273 maps, with bittersweet's; no new warning).
- `javac` parse (syntax only) of the new Java: OK. The full compile runs in CI.
- The first run of the checks failed, all fixed before commit:
  - The audit found:
    - bittersweet's item had no picture. It now has a 16 by 16 icon map (`tools/item_icons/bittersweet.txt`): a twining stem with two pointed yellow leaves and two scarlet berries under their husks. The icon check first refused its leaves as the main material, because that yellow reads as gold; they are now its second material, as the Sunpetal's are.
    - the garden plants' recipes followed the agriculture switch while the plants are the biomes branch's;
    - kale and cabbage made from the dye they give back formed recipe loops (the audit refuses any loop, even one that loses a seed);
    - the stonecrop's and pampas grass's biomes (mountains, savannas) have no landscape kind to scatter them in too;
    - three models left a face uncovered (the rosettes' bud, the plumes' tops).
  - Two unit tests failed alongside those (which two was not kept); after the fixes all 287 pass.
- The models were previewed offline, textured, from two sides (review aids, not committed); what the previews showed is under How they are drawn.

Game tests (CI only, not run locally), `FallGardenGameTests`:
- Every plant has its block and item:
  - the four small flowers are small flowers with potted forms;
  - pampas grass is a tall flower, and the dried sunflower a two-block plant;
  - their dye recipes and the garden plants' recipes load;
  - each stands on grass.
- Bittersweet holds to a wall of logs; a hand takes none of it, and shears take one for its face.

`FallGardenClientGameTests` takes two screenshots, from the front and close by: a fall border on grass, with asters and stonecrop behind ornamental kale and cabbage, pampas grass and dried sunflowers at its back against a wall of logs with bittersweet on it, and a potted aster.

Not run: a hand playtest, and a two-client playtest. Whether the patches generate is checked by data files, not by a world being made.

## World and event applicability
The wild plants generate in vanilla's biomes by conventional tag: asters in plains and forests, stonecrop and pampas grass in plains, bittersweet in forests. Not seasonal: they grow and are made all year.

## Rollout and open questions
- Part 2: planters (a window box, a half-barrel, a galvanized tub) that take these plants.
- Part 3: the porch: a porch swing on chains, a wheat stook, a woven corn dolly, a bittersweet wreath, a quilt over a rail.
- Part 4: string lights that light along their length.
- Open: whether a dried sunflower should give sunflower seeds when broken.
