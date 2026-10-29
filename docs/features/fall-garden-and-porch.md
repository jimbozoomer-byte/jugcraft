# Fall Garden & Porch

Status: all four parts implemented: 1 (the fall garden's plants), 2 (planters), 3 (the porch) and 4 (café lights). CI builds it and runs its game tests and client screenshots; not yet played by hand.
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

### Part 2: planters
Three planters, each filled with soil, that take any small flower (`#minecraft:small_flowers`: vanilla's and these). Use a flower on one to plant it in the next free spot; use it with an empty hand to take back the one planted last. The flowers grow in it as themselves, smaller.
- **Window Box**: a box of nailed boards painted sage green, 94 cm long, 25 cm deep and 25 cm high, hung on a wall (it comes off when the wall goes). Three flowers in a row, at half their size.
  - Recipe: planks round dirt, over three planks.
- **Half-Barrel Planter**: an oak half-barrel, 62 cm across and 45 cm high, its staves upright under two iron hoops. One flower in the middle, nearly full size.
  - Recipe (shapeless): a barrel and two dirt make two, as a barrel sawn in two does.
- **Galvanized Tub Planter**: an oval zinc wash tub, 75 by 50 cm and 28 cm high, with a drop handle at each end. Two flowers side by side.
  - Recipe: iron ingots round dirt, over three iron ingots.
- A full planter says so, and keeps the flower in your hand. Anything that isn't a small flower does what it would on any block. Broken, a planter gives back its flowers.

### Part 3: the porch
- **Porch Swing** (two blocks across, two up): a slatted bench swing 1.5 m wide, its seat 50 cm up. It has a slatted back between two posts under a top rail, and arms on front posts.
  - At each end two chains rise from the arm's front and the back post to a ring, and one chain goes on up to a hook in the ceiling.
  - It hangs from a ceiling two blocks up: placed, it needs a sturdy ceiling over both its upper blocks, and it comes down whole when either goes.
  - **Two can sit on it**, one on each half: use either half, low or high, to sit on that half's seat.
  - **It sways on its chains** about the hooks, a degree and a half each way in the breeze, and eight with someone on it, a sway taking three and a half seconds.
  - Recipe: two iron chains over planks round a wooden slab, over two planks.
- **Wheat Stook**: six sheaves of wheat stood together to dry, 95 cm high: four leaning in round two upright ones, each tied a third of the way up under its heads of grain.
  - Recipe (shapeless): six wheat and a string.
- **Corn Dolly**: a plaited straw harvest knot hung on a wall or door, its bell stepping in to a neck, tied with a red ribbon bow under its loop, the ears of grain fanned below. 50 cm long.
  - Recipe (shapeless): three wheat and a red dye.
- **Bittersweet Wreath**: a thick ring of twisted bittersweet vine 69 cm across, thick with yellow leaves and scarlet berries in split orange husks, hung on a wall.
  - Recipe: eight bittersweet round an empty middle.
- **Quilt Rail**: a length of porch rail (two posts, a top and a bottom rail, three balusters) with a patchwork quilt hung over it to air.
  - Recipe (shapeless): a wooden fence and three wool.

### Part 4: café lights
- **Café String Lights**: warm Edison bulbs hanging on short drops from a black cord, about a bulb a block.
  - String them between two String Light Hooks as the other strands are, and light them the same way, with redstone or electricity at either hook.
  - **They light along their length.** While lit, the strand puts a light of 12 in the air about every two blocks along it, just under where it sags, and only where the air is empty (never over a block). When it goes dark, comes down or loses its hook, it puts those lights out again.
  - Recipe (shapeless): two string, a glowstone dust, a glass pane and a black dye.

## Changes from the idea list
- The idea list put **goldenrod** and **cattails** in set 4. Both grow already (the biomes branch's batches 2 and 3), so they are not built again.
- The dried sunflower is **two blocks** tall, as a vanilla sunflower is, not three. A three-block plant would need a block of its own kind for one plant, and its head would reach into a block that could hold something else.
- Ornamental kale and cabbage are made from cabbage seeds (with purple dye or bone meal), not grown as crops of their own.

## How they are drawn (docs/ART_DIRECTION.md, docs/NATURAL_TEXTURES.md)
### Part 2's planters
- Built from boxes at real size, one texel to a model pixel, in the market displays' manner (`tools/fall_garden_data.py`).
- Round and oval things are two crossed boxes, as the churn and the quern are. Their soil is the top of the body, with a rim of four thin boxes along the outline above it, so no box is left open.
- The marks follow each material:
  - the soil is turned clods in a steady diagonal run;
  - the barrel's staves stand upright, three texels wide, with a seam down each;
  - the zinc has the coating's spangle in a steady run and two pressed ribs round it.
- What grows in a planter is each flower's own block model, drawn by `PlanterRenderer` in its spot.
- The first previews showed the barrel's staves running sideways like a crate's, and the window box's two-pixel boards striping hard. Both were fixed before commit: the staves now stand upright, and each face of the box is one board.

### Part 3's porch
- Built the same way (`tools/fall_garden_data.py`), on the market displays' wood and iron, at real size.
- **The Porch Swing**:
  - Its chains are links seen edge-on and flat by turns. Each end's V rises at 22.5 degrees to a ring, under one chain straight up to the hook.
  - **Moving parts move.** The hooks are the block's own model, cut into its upper blocks. The swing is its own model, the blockstate's `runner` variant of its first block, which `PorchSwingRenderer` turns to the facing and swings about the line through the hooks.
- **The Wheat Stook**'s sheaves are the pumpkin patch's straw, its heads of grain a two-tone checker of kernels.
- **The Corn Dolly** is plaited straw in a herringbone of stalks, each row's run leaning the other way from the last.
- **The Bittersweet Wreath**: the vine is an octagon of lengths, as a ring is drawn in pixels.
- **The Quilt Rail**: the quilt's patches are four texels across in four fabrics (rust, mustard, cream and pine), in a steady order. Each has a darker seam at its lower and right edge.
- The first previews showed the wreath thin and sparse, and the dolly without its ears. The wreath's vine is now thicker, with ten clusters of berries and eight leaves, and the ears now fan out under the dolly.

### Part 4's café lights
- The strand's texture (`entity/cafe_string_lights`, 16 by 16) is the black cord in two tones along it, and an Edison bulb's warm glass, paler to its middle, with its filament a bright zigzag. The renderer draws each bulb full-bright while lit, and smoky while dark.
- The item's icon is a 16 by 16 map (`tools/item_icons/cafe_string_lights.txt`): a swag of black cord with three warm bulbs on drops below it. The first draft's bulbs crowded the cord and each other, leaving the outline doubled and pinholes; they now hang five apart.

### Part 1's plants
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
- Existing output consumer: dyes, suspicious stew, flower pots, composters, bees (they are flowers), and part 2's planters, which take every small flower, vanilla's too.
- Technology/magic connections (or infrastructure/cosmetic applicability): cosmetic, and the biomes branch's wild plants.
- Reachable entry path: from the first day: the wild plants are found, and cabbage seeds come from wild cabbage.
- Which connections are required vs optional; trade and solo routes: all optional, solo-found or solo-crafted, and tradeable like any item.
- How this specialty stays useful without mastering every other branch: they are plants for anyone's garden.

## Balance and automation
- The flowers give one dye each (pampas two), as vanilla's do.
- **No loop.** A kale costs a cabbage seed and a purple dye and gives one magenta dye; a cabbage costs a seed and a bone meal and gives one white dye. Neither takes the dye it gives, so no recipe leads back to itself (the audit refuses any loop).
- A dried sunflower costs a sunflower (two yellow dye) and gives no dye.
- The porch's pieces are decorations: a stook costs six wheat and a string, a dolly three wheat and a dye, a wreath eight bittersweet, a quilt rail a fence and three wool, and nothing turns them back.
- A planter keeps the flowers put in it and gives back exactly those. It grows, multiplies and spreads nothing. Two half-barrels cost a barrel and two dirt, and nothing turns them back.

## Multiplayer and persistence
- Plain blocks, registered from the generated `/jugcraft/plants.json` with the other wild plants; nothing ticks but what vanilla's flowers and glow lichen do.
- Part 2: a planter's flowers are kept by its block entity (`planter`), changed on the server only, saved with the world and sent to clients for drawing. Placing a flower checks the planter's own spots on the server. A full planter keeps the flower in the hand rather than placing it in front. Nothing ticks.
- Part 4: the strand's lights are vanilla light blocks, placed and removed by the hook that holds the strand, on the server only. The hook saves where it put them and removes only those that are still its own lights. It never places one over a block, or in a chunk that isn't loaded; one in a chunk that has unloaded is put out the next time that chunk is there. The work is bounded by the strand's length: at most eight spots, checked every ten ticks.
- Part 3: the swing keeps nothing; its sway is worked out from the game time and whether anyone sits on it, so nothing ticks. Sitting uses the shared `Seat`, one sitter to a seat, on the server. The swing needs its ceiling, the dolly and the wreath their wall, each checked on the server when placed and when a neighbour changes.
- IDs (stable from release): `aster`, `sedum`, `ornamental_kale`, `ornamental_cabbage` (each with its `potted_` block), `pampas_grass`, `dried_sunflower`, `bittersweet`. Placed features: `patch_aster`, `patch_sedum`, `patch_pampas_grass`, `patch_bittersweet`. Part 2's blocks `window_box`, `half_barrel_planter`, `galvanized_tub_planter`, and the block entity `planter`. Part 3's blocks `porch_swing` (and its block entity), `wheat_stook`, `corn_dolly`, `bittersweet_wreath`, `quilt_rail`. Part 4's item `cafe_string_lights` (strand `cafe`) and the hook's saved `lamps`.
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
- Part 2, all on the first run:
  - `python3 tools/check_mod_data.py`: PASS (8,108 material IDs). `check_fall_garden` now also checks the planters' Java numbers against `tools/fall_garden.py`, their renderer and their recipes.
  - `python3 scripts/check_repository.py`: PASS.
  - `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
  - `python3 tools/check_icon_maps.py`: PASS (no new map; the planters' items are 3D).
  - `javac` parse (syntax only) of the new and changed Java: OK.
  - Part 2's first CI run failed to compile: 26.3 has no `ItemTags.SMALL_FLOWERS` field. `PlanterBlock` now makes the `#minecraft:small_flowers` tag key itself (the tag is vanilla's, with set 4's flowers added).
- Part 3, all on the first run:
  - `python3 tools/check_mod_data.py`: PASS (8,113 material IDs). `check_fall_garden` now also checks the swing's size, seat and sway against `tools/fall_garden.py`, that its renderer turns it about the model's hooks, and that its runner variants name the swing.
  - `python3 scripts/check_repository.py`: PASS.
  - `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
  - `python3 tools/check_icon_maps.py`: PASS (no new map; the porch's items are 3D).
  - `javac` parse (syntax only) of the new and changed Java: OK.
- Part 4:
  - `python3 tools/check_mod_data.py`: PASS (8,114 material IDs). `check_fall_garden` now checks the café strand, its lamps' light and spacing against `tools/fall_garden.py`, its texture, its renderer and its item.
  - `python3 scripts/check_repository.py`: PASS.
  - `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
  - `python3 tools/check_icon_maps.py`: PASS (274 maps, with the café lights'; no new warning). Its first drafts failed the lint (doubled outlines and pinholes where the bulbs crowded the cord), and one passing draft was warned for standing on the wrong rows; the bulbs now hang five apart, in the compact family.
  - `javac` parse (syntax only) of the new and changed Java: OK.

Game tests (CI only, not run locally), `FallGardenGameTests`:
- Every plant has its block and item:
  - the four small flowers are small flowers with potted forms;
  - pampas grass is a tall flower, and the dried sunflower a two-block plant;
  - their dye recipes and the garden plants' recipes load;
  - each stands on grass.
- Bittersweet holds to a wall of logs; a hand takes none of it, and shears take one for its face.

- **Part 2:**
  - **The window box**:
    - it won't hang without a wall, and hangs on one facing out;
    - a stick isn't planted;
    - an aster, a poppy and a dandelion are, each spending one;
    - a fourth flower isn't planted and stays in the hand;
    - an empty hand takes back the dandelion;
    - without its wall it comes off, giving back itself and its flowers.
  - **The half-barrel and the tub**: they stand facing the player; the barrel takes one flower and the tub two, refusing more; broken, they give back their flowers and themselves.

- **Part 3:**
  - **The swing**:
    - it won't hang without a ceiling, and hangs whole (four blocks) from one;
    - one player sits on its first seat from below, and another on the second seat from its chains above;
    - without its ceiling it comes down whole, dropping once.
  - **The rest**: the dolly and the wreath hang on a wall and come off with it, and the stook and the rail stand facing the player; each drops itself.

- **Part 4: the café lights**, strung between two hooks on posts:
  - dark, they light nothing;
  - lit, they put a lamp of light 12 in each empty spot along them, and none over a block built in one;
  - dark again, their lamps go out;
  - lit again, and with a hook broken, the lamps go out and the strand drops.

`FallGardenClientGameTests` takes four screenshots:
- from the front and close by;
- at the planters (a window box of three flowers on the logs, a half-barrel of kale and a tub of two flowers, drawn by `PlanterRenderer`);
- at the porch: the swing under its plank roof (drawn by `PorchSwingRenderer`), the wreath and the dolly on its back wall, quilt rails along its front, a stook, and café lights strung along its front, lit by a redstone block set in the roof.

The first two show: a fall border on grass, with asters and stonecrop behind ornamental kale and cabbage, pampas grass and dried sunflowers at its back against a wall of logs with bittersweet on it, and a potted aster.

Not run: a hand playtest, and a two-client playtest. Whether the patches generate is checked by data files, not by a world being made.

## World and event applicability
The wild plants generate in vanilla's biomes by conventional tag: asters in plains and forests, stonecrop and pampas grass in plains, bittersweet in forests. Not seasonal: they grow and are made all year.

## Rollout and open questions
- Set 4 is complete with part 4. Set 5 (Cozy Fall Hearth) follows.
- Open: whether the other strands should light along their length too; whether a dried sunflower should give sunflower seeds when broken; whether planters should take tall flowers or saplings.
