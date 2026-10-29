# Harvest Festival Grounds

Status: part 1 (the Giant Cornucopia) implemented. CI builds it and runs its game tests and client screenshots; it has not been played by hand. Parts 2 to 4 are planned.
Proposal issue: none. The owner asked for it directly on 28 October 2026, as set 10 of the ten sets of fall and Halloween decor ("Then I want you to do 1-10 in parts in that order one by one"): the grounds of a country harvest festival, with:
- a giant cornucopia;
- a giant vegetable show with prize-ribbon boards;
- a steam traction engine pulling a hay wagon;
- harvest bunting and sack-race sacks.

Owner: @jimbozoomer-byte
Target milestone and tier: early farming. The cornucopia is made from sticks, wheat, a pumpkin and an apple, and from the farm's own corn and butternut squash.
Primary specialty and supported player role: building, with farming. A farmer grows its harvest, and a builder sets out the festival's grounds. Nothing in it is needed to progress.

## Player experience

### Part 1: the Giant Cornucopia
- **Giant Cornucopia**: a festival horn of plenty of woven willow, 2.7 m long, lying on its side. Three blocks across, two up and two deep.
  - **Its mouth**: 1.25 m across, octagonal, behind a braided rim. Behind the mouth, the horn narrows in four lengths, then its tail curls up off the ground, stands up and turns back over the horn to its tip.
  - **The harvest spilling from its mouth**, each piece at its own size:
    - a big pumpkin, 50 cm across, on the ground before it, with a butternut squash lying on it;
    - a smaller pumpkin in the mouth, with an acorn squash on it;
    - a sheaf of wheat standing in the mouth, bound with twine;
    - three ears of corn in their husks, leaning out over the rim;
    - a bunch of grapes hanging over the rim;
    - a third pumpkin, two striped ornamental gourds and four apples rolled out over the ground.
  - **Placing it**: it faces whoever places it, its mouth to their right and its tail to their left. It needs all twelve of its blocks free.
  - **Using it** does nothing; it has nothing to light.
  - **Recipe**: two sticks either side of wheat, over a pumpkin, a butternut squash and an ear of corn, over two sticks either side of an apple.
  - **Breaking it**: any block of it brings the whole horn down, and it drops once.

### Planned: parts 2 to 4
- Part 2: a giant vegetable show. Prize-winning giant produce on show tables, and boards hung with the Blue, Red and Yellow Prize Ribbons the festival already gives (`first_prize_ribbon`, `second_prize_ribbon`, `third_prize_ribbon`).
- Part 3: a steam traction engine and a hay wagon behind it. Several blocks long, its flywheel and crank turning while it is fired.
- Part 4: harvest bunting, a new kind of strand on the café lights' string system, and sack-race sacks.

## How it is drawn (docs/ART_DIRECTION.md, docs/NATURAL_TEXTURES.md)
- Built from boxes at real size, one texel to a model pixel (`tools/harvest_festival_data.py`), in the Pumpkin Patch Market's manner. It shares that set's pumpkin stem and cut, and the Haunted Midway's pumpkin orange.
- **Willow** (the horn) is basket weave:
  - the weavers run round the horn in bands two texels wide, each passing over three stakes and under one;
  - where a stake shows, it is in shadow, and each band's stakes sit two texels on from the last band's;
  - a band's first column is lit, its second its own tone.
  - Inside the horn the same weave is two tones darker, so the mouth reads as deep.
- **The rim** is a braid on the slant, its strands lit, mid and dark by turns.
- **The mouth** is an octagon: four flat walls, and four corner walls turned 45 degrees, a block model's own turn. Each corner's outer face lies on the octagon's edge, its ends meeting the flat walls' outer faces.
- **The horn** is round in each of its lengths: two boxes crossed, each a fifth narrower at its corners than the length is across. Each length is narrower than the last.
- **The tail's** lengths are turned 22.5 and 45 degrees as it rises, stand up, then turn back by 45 and 22.5 to its tip.
- **The harvest**, each piece flat as fruit is and lit along its top:
  - a pumpkin's rind is ribbed up it, a rib lit and a groove in shadow, round in the same two crossed boxes;
  - an acorn squash has deep green ribs;
  - the gourds are striped yellow and green;
  - the corn is in rows of kernels, its husks pale straw;
  - the wheat is golden stalks under bearded ears;
  - the grapes are a bunch, lit and shaded by turns;
  - the apples are red, with a glint at the top.
- **Its blocks' models**: it is modelled whole in its frame (48 by 32 by 32 pixels) and cut into its blocks' models, so its picture runs on unbroken across them. Nothing of it lies outside its frame, so no box is cut away.
- **The item** is the whole horn at a third of its size.

## Connections
- Existing input producer: farming. Corn ([fall-harvest.md](fall-harvest.md)), butternut squash ([festival-crops.md](festival-crops.md)), wheat, pumpkins and apples.
- Recipe inputs: sticks, wheat, a pumpkin, a butternut squash, an ear of corn and an apple.
- Existing output consumer: none; it is decoration.
- Technology/magic connections (or infrastructure/cosmetic applicability): cosmetic. It is a festival display for a farm's harvest.
- Reachable entry path: corn and butternut squash grow from their seeds, found or traded early. Everything else is vanilla.
- Which connections are required vs optional; trade and solo routes: all optional, made solo, tradeable.
- How this specialty stays useful without mastering every other branch: it needs nothing but a farm.

## Balance and automation
- Decor only. One recipe makes one Giant Cornucopia, which drops only itself. Nothing turns it back into its harvest, so no loop gains.

## Multiplayer and persistence
- It has no block entity and keeps nothing; its blocks' `facing` and `part` are all it is.
- **The server places and breaks it whole**, as every prop of several blocks does (`MultiDecorationBlock`). Placing it needs every one of its blocks free. Breaking any of them takes it all down, dropping it once from its first block.
- It can't be pushed by a piston.
- IDs (stable from release): the block and item `giant_cornucopia` (`facing`, `lit`, `part` 0 to 11; `lit` is always on, as it has no lanterns).
- Its recipe follows the agriculture switch. The block and item stay registered when it is off.

## Dependencies and assets
- No new dependency.
- Its models, blockstate, loot table and texture are generated by `tools/harvest_festival_data.py` from `tools/harvest_festival.py`, through `tools/generate_material_data.py` and `tools/generate_textures.py`.
- Nothing is copied, traced or recoloured from Minecraft's textures.

## Verification
Local checks run on this branch, and their results:
- Part 1:
  - `python3 tools/check_mod_data.py`: PASS on the second run (8,169 material IDs), including the art check of every block and item model. The first run failed twice: the texture was 256 pixels square (now 128), and the horn's length was measured from the butternut squash, not the rim. Its new `check_harvest_festival` checks:
    - that the Giant Cornucopia's Java block is a `MultiDecorationBlock` of the table's size, with nothing to light, and that its shapes lie in its frame;
    - its registration with its item;
    - that its whole model lies in its frame, its mouth is the table's and wider than the horn behind it, the horn narrows to its tail, and it is the table's length from rim to tail;
    - that its blockstate covers every facing, part and `lit`, with every part's model written;
    - that it drops once, from its first block;
    - that its recipe takes the farm's corn and butternut squash, and that its name has its text.
  - `python3 scripts/check_repository.py`: PASS. The tools' unit tests (`python3 -m unittest discover -s scripts/tests`): 287 tests, OK. `python3 tools/check_icon_maps.py`: PASS, with its existing warnings.
  - Not run locally: `./gradlew build` and the game tests (CI runs them).

Game tests (`HarvestFestivalGameTests`, run by CI's `gametest` job):
- `theGiantCornucopiaStandsWhole`: placed, it stands facing the player, all twelve of its blocks there in order. Use does nothing to it. Broken at its tail, it all goes, and it drops itself once.
- `harvestFestivalDataLoads`: its recipe and loot load.

Client screenshots (`HarvestFestivalClientGameTests`, CI's `client` job):
- the Giant Cornucopia on the grass, facing the camera, from the front;
- from the west, into its mouth and the harvest spilling out of it;
- from the north-east, over its tail.

Not run: a hand playtest, and a two-client playtest.

## World and event applicability
Seasonal in look, not in use: it is made and stands all year. It doesn't generate in the world.

## Rollout and open questions
- The parts, each stacked on the last: the Giant Cornucopia; the giant vegetable show; the steam traction engine and hay wagon; the bunting and sack-race sacks.
- Open: whether a cornucopia should take real produce to fill its mouth, as the Pumpkin Patch Market's displays do.
