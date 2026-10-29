# Harvest Festival Grounds

Status: parts 1 (the Giant Cornucopia) and 2 (the Giant Vegetable Show and the Prize Ribbon Board) implemented. CI builds them and runs their game tests and client screenshots; they have not been played by hand. Parts 3 and 4 are planned.
Proposal issue: none. The owner asked for it directly on 28 October 2026, as set 10 of the ten sets of fall and Halloween decor ("Then I want you to do 1-10 in parts in that order one by one"): the grounds of a country harvest festival, with:
- a giant cornucopia;
- a giant vegetable show with prize-ribbon boards;
- a steam traction engine pulling a hay wagon;
- harvest bunting and sack-race sacks.

Owner: @jimbozoomer-byte
Target milestone and tier: early farming. The cornucopia is made from sticks, wheat, a pumpkin and an apple, and from the farm's own corn and butternut squash. Each show vegetable is grown from nine of its crop. The ribbon board is made from planks, green dye, sticks and a gold nugget, and shows the prize ribbons a player has won.
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

### Part 2: the Giant Vegetable Show
Four prize show vegetables at a show's sizes, each one block. Each stands facing whoever places it, with its entry card on a wire stake at its front corner.
- **Giant Cabbage**: its head 50 cm across, domed, sitting in its outer leaves, which spread 94 cm across. Four leaves cup up round it, and four lie flat between them.
- **Giant Marrow**: 94 cm long and 38 cm across, striped dark and pale green, lying on a bed of straw, its stalk at the back.
- **Giant Turnip**: its root 44 cm across, a globe a little wider than high on its taproot, purple shoulders over white, under five leaves 38 cm high.
- **Giant Carrot**: its root 25 cm across at the shoulder and 69 cm long, lying on the ground and tapering to its tip, its fronds rising from its shoulder.
- **Recipes**: nine of each crop grow one, as nine Warty Gourds make the Giant Warty Gourd:
  - nine Cabbages make the Giant Cabbage;
  - nine Zucchini make the Giant Marrow (a marrow is a zucchini grown on);
  - nine Turnips make the Giant Turnip;
  - nine Carrots make the Giant Carrot.
- Broken, each drops itself.

- **Prize Ribbon Board**: a show's results board, two blocks tall.
  - It is green-painted planks 94 cm across and 69 cm high, edged in dull gold, on two square posts 1.6 m tall.
  - Its three brass cup hooks are numbered 1, 2 and 3 from the left as you face it, each number painted over its hook.
  - **Hanging a ribbon**: use a First, Second or Third Prize Ribbon on it, and the ribbon hangs on its own hook, wherever you aim.
    - These are the ribbons the Harvest Scale gives for a prize pumpkin, and the corn maze gives for a place on its board.
    - The ribbon is used up.
    - A second ribbon of a kind already hanging there is refused and kept.
  - **Taking one back**: use it with an empty hand, and it gives back the hung ribbon nearest the hook you aim at.
  - Hanging and taking need build permission.
  - It stands facing whoever places it, and needs the block above free.
  - **Recipe**: planks either side of green dye, over planks either side of a gold nugget (the hooks' brass), over two sticks.
  - **Breaking it**: either half brings it down, and it drops itself and every ribbon hanging on it.

### Planned: parts 3 and 4
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
- **The show vegetables** (part 2): there are no small 3D models of these crops to scale up (their items are flat pictures), so each is modelled at a show's size in its crop's colours. Each is round in two crossed boxes where it is round.
  - **The cabbage's** outer leaves are blue-green. Each has its midrib pale down its middle, veins a tone lighter running out from it on the slant every third texel, and a lighter edge. The cupped leaves are turned up 22.5 degrees; the flat ones lie crossed at 45.
  - **The cabbage's head** is paler. Its wrapped leaves run in bands three texels high, each band's edge lit and a shadow under it, with a vein every fourth column. Its top is folded leaves in rings.
  - **The marrow** is dark green, with pale stripes along it every third texel round, each broken for a texel every fifth along. Its ends are dark, with a scar in the middle. Its straw is the Pumpkin Patch Market's hay.
  - **The turnip** has purple shoulders over white. They blush down into the white in streaks of a fixed run of lengths, column by column. The white has a faint ring every third row, and its foot is in shadow. Its leaves have a pale stalk, a lit midrib and lobes notched into their edges every third row.
  - **The carrot** is orange, its upper row lit, ringed by short dark dashes every third texel along, each ring's dashes two round from the last's. It tapers in five lengths to its tip. Its shoulder is a ring of orange round the green crown. Its fronds are a fishbone of leaflets by turns lit and dark.
  - **The entry card** is white card, its top row lit, its number inked at its foot, on an iron wire stake.
- **The Prize Ribbon Board** (part 2):
  - Its boards are painted green, each three texels high, lit along its top and seamed along its foot, no grain showing through the paint. A dull gold line runs round its edge.
  - Its numbers are cream, three texels wide and five high, each digit's left column lit.
  - Its posts are green-painted timber with caps.
  - **Each rosette** is 25 cm across: its pleated ring a texel to a pleat, lit and shaded by turns, round in two crossed boxes. It has a cream and gold button in its middle, and two tails hanging in a V below it, turned 22.5 degrees each way. Its colours are the ribbon items': blue for first, red for second, white for third.
  - Each rosette is its own model, added to the board by its blockstate while it hangs.

## Connections
- Existing input producer: farming. Corn ([fall-harvest.md](fall-harvest.md)), butternut squash and turnips ([festival-crops.md](festival-crops.md)), cabbages ([garden-crops.md](garden-crops.md)), zucchini ([vegetables-herbs-and-spices.md](vegetables-herbs-and-spices.md)), carrots, wheat, pumpkins and apples. The prize ribbons come from the Harvest Scale and the corn maze ([fall-additions.md](fall-additions.md)).
- Recipe inputs:
  - the cornucopia: sticks, wheat, a pumpkin, a butternut squash, an ear of corn and an apple;
  - each show vegetable: nine of its crop;
  - the ribbon board: planks, green dye, a gold nugget and sticks.
- Existing output consumer: none; it is decoration. The ribbon board gives a player's prize ribbons somewhere to be shown.
- Technology/magic connections (or infrastructure/cosmetic applicability): cosmetic. It is a festival display for a farm's harvest.
- Reachable entry path: corn and butternut squash grow from their seeds, found or traded early. Everything else is vanilla.
- Which connections are required vs optional; trade and solo routes: all optional, made solo, tradeable.
- How this specialty stays useful without mastering every other branch: it needs nothing but a farm.

## Balance and automation
- Decor only. One recipe makes one Giant Cornucopia, which drops only itself. Nothing turns it back into its harvest, so no loop gains.
- Nine of a crop make one show vegetable, which drops only itself and turns back into nothing.
- The ribbon board makes no ribbons. It holds at most one of each, and gives back exactly the ones hung on it, taken off or when it is broken.

## Multiplayer and persistence
- The cornucopia has no block entity and keeps nothing; its blocks' `facing` and `part` are all it is.
- **The server places and breaks it whole**, as every prop of several blocks does (`MultiDecorationBlock`). Placing it needs every one of its blocks free. Breaking any of them takes it all down, dropping it once from its first block.
- It can't be pushed by a piston.
- **The show vegetables** keep nothing but their `facing`. A piston pops them off, dropping them.
- **The Prize Ribbon Board** keeps which ribbons hang on it in its block state (`first`, `second`, `third`), on both halves, so it saves with the world and needs no block entity.
  - The server hangs and takes ribbons, only for a player who may build there.
  - Its loot table drops the board and each hung ribbon from its lower half. Breaking the upper half brings the lower down with its drops. The ribbons drop even in an explosion.
  - Broken in creative, nothing drops, as with any block.
  - A piston can't push it.
- IDs (stable from release):
  - the block and item `giant_cornucopia` (`facing`, `lit`, `part` 0 to 11; `lit` is always on, as it has no lanterns);
  - the blocks and items `giant_cabbage`, `giant_marrow`, `giant_turnip` and `giant_carrot` (`facing`);
  - the block and item `prize_ribbon_board` (`facing`, `half`, `first`, `second`, `third`).
- Their recipes follow the agriculture switch. The blocks and items stay registered when it is off.

## Dependencies and assets
- No new dependency.
- Their models, blockstates, loot tables and textures are generated by `tools/harvest_festival_data.py` from `tools/harvest_festival.py`, through `tools/generate_material_data.py` and `tools/generate_textures.py`.
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
- Part 2:
  - `python3 tools/check_mod_data.py`: PASS on the first run (8,174 material IDs), including the art check of every block and item model. Its `check_harvest_festival` now also checks:
    - that each show vegetable is a `FacingDecorationBlock` whose model lies in its block, drawn at every facing, made from nine of its crop, dropping itself, its name its text;
    - that the Prize Ribbon Board's hooks take the Harvest Scale's ribbons in their order;
    - that its Java keeps a hook for each on both halves, only for a player who may build, and is registered with its two-block item;
    - that hook 1 is at the left as you face it;
    - that its blockstate draws the board, an empty upper half and each rosette while it hangs;
    - that broken, it drops itself once and each ribbon hanging on it, and its recipe has its paint and hooks;
    - that its texts are there.
  - `python3 scripts/check_repository.py`: PASS. The tools' unit tests: 287 tests, OK. `python3 tools/check_icon_maps.py`: PASS, with its existing warnings.
  - Not run locally: `./gradlew build` and the game tests (CI runs them).

Game tests (`HarvestFestivalGameTests`, run by CI's `gametest` job):
- `theGiantCornucopiaStandsWhole`: placed, it stands facing the player, all twelve of its blocks there in order. Use does nothing to it. Broken at its tail, it all goes, and it drops itself once.
- `theShowVegetablesStandAndDropThemselves` (part 2): each of the four, placed, stands facing the player; broken, it drops itself once.
- `thePrizeRibbonBoardHangsRibbons` (part 2):
  - placed, it stands two blocks tall, facing the player;
  - a First Prize Ribbon hangs on its first hook on both halves, wherever it is aimed, and is used up; a second First is refused and kept;
  - an apple hangs on nothing, and a player who may not build (adventure mode) hangs nothing;
  - a Third hangs on the third hook;
  - an empty hand aimed at the third hook takes back the Third, and only it; aimed there again, it takes the nearest ribbon left, the First;
  - broken at its top with a First hanging, it all goes and drops itself once and the First, and no ribbon that wasn't hanging.
- `harvestFestivalDataLoads`: every block's recipe and loot load.

Client screenshots (`HarvestFestivalClientGameTests`, CI's `client` job):
- the Giant Cornucopia on the grass, facing the camera, from the front;
- from the west, into its mouth and the harvest spilling out of it;
- from the north-east, over its tail;
- the show (part 2): the four vegetables in a row facing the camera, the Prize Ribbon Board behind them with all three ribbons hung;
- close on the board.

Not run: a hand playtest, and a two-client playtest.

## World and event applicability
Seasonal in look, not in use: it is made and stands all year. It doesn't generate in the world.

## Rollout and open questions
- The parts, each stacked on the last: the Giant Cornucopia; the giant vegetable show; the steam traction engine and hay wagon; the bunting and sack-race sacks.
- Open: whether a cornucopia should take real produce to fill its mouth, as the Pumpkin Patch Market's displays do.
