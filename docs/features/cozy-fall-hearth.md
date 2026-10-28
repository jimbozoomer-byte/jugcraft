# Cozy Fall Hearth

Status: part 1 (the hearth) implemented. CI builds it and runs its game tests and client screenshots; not yet played by hand. Parts 2 to 5 are planned below.
Proposal issue: none. The owner asked for it directly on 28 October 2026, as set 5 of the ten sets of fall and Halloween decor ("Then I want you to do 1-10 in parts in that order one by one"), drawn by their rules.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery tier; made from cobblestone, planks, a campfire and a stone slab.
Primary specialty and supported player role: building. A builder furnishes a room round a fire for the fall; nothing in it is needed to progress.

## Player experience

### Part 1: the hearth
- **Stone Fireplace** (three blocks across, three up, one deep): a fieldstone chimney breast on a flagstone hearth, the size of a real one.
  - It stands 3 m high, floor to ceiling, and 62 cm deep, with its hearth running out 37 cm before it.
  - Its firebox opening is 1.25 m across and 78 cm high, lined with firebrick at its back. Two iron andirons stand in it with three logs laid across them.
  - An oak mantel shelf runs across it at 1.4 m on two corbels, and the stack steps in above the mantel to the ceiling.
  - Placed facing you, it runs from the block you aim at to your right and up; it needs all nine blocks free.
  - **It burns.** Flint and steel or a fire charge lights it (the flint wears, the charge is spent), and a shovel puts it out. While lit, a fire burns in its firebox: tongues of flame rising and falling in smooth bands, crackling, with a little smoke.
  - Lit, its firebox block gives light 15; the rest give none.
  - It is a decoration: it burns no fuel and cooks, smelts or heats nothing. With an empty hand it tells you how to light it.
  - Recipe: three cobblestone over planks, a campfire and planks, over cobblestone, a smooth stone slab and cobblestone.
- **Mantel Garland** (an item): autumn leaves, little pumpkins and pine cones.
  - Use it on a fireplace to drape its mantel: leaves along the shelf in three autumn colours, four swags of leaves hung along its front, three pumpkins and two cones.
  - Sneak and use the fireplace with an empty hand to take it back.
  - Recipe (shapeless): two leaves of any kind, a pumpkin and a string.
- Broken, the fireplace comes away whole, from whichever block is broken, and drops itself once and its garland if it had one.

### Planned parts
- **Part 2: the woodstove.** A cast-iron parlour stove that heats the room, with a kettle that steams and a simmer pot. (The Kitchen Stove is the cooking range already; this is a heater.)
- **Part 3: the woodpile.** A firewood rack that fills with split logs, and a chopping block with its axe.
- **Part 4: the comforts.** A blanket ladder hung with folded blankets, and a knitting basket.
- **Part 5: the lights.** A hurricane lamp for the table (a different thing from set 3's Barn Lantern, which hangs on a wall), and mason-jar candles, made with the kitchen's mason jar.

## How it is drawn (docs/ART_DIRECTION.md, docs/NATURAL_TEXTURES.md)
- Built from boxes at real size, one texel to a model pixel, in the market displays' manner (`tools/hearth_data.py`), and cut into its nine blocks' models.
- Each material is one colour stepped into tones, with its marks placed the way it is made:
  - **Fieldstone**: stones in courses four texels high, of several lengths in turn so no two courses line up. Their corners are rounded off into a pale mortar, each stone takes one of three tones by its place so neighbours differ, and each is lit along its top row.
  - **Firebrick**: small bricks in a running bond, sooted a tone darker in the upper half.
  - **Flagstones**: slabs eight texels square with a darker joint between them.
  - **The mantel**: an oak beam, its grain along its length. **The logs**: bark along them, end grain at their ends in rings about a dark pith.
- **The fire is in smooth bands, and doesn't flash.** It is its own model, three sheets of flame one behind another over the logs, on its own animated texture (`stone_fireplace_fire`, eight 16 by 16 frames). Each frame is five tongues of flame, each a smooth rise from a pale root through yellow and orange to a deep orange tip. Their heights rise and fall on a slow wave, each tongue a step behind the last, and the game blends the frames. The fire is drawn full-bright and unshaded, and only while lit.
- **The garland**: its leaves in three autumn colours (crimson, rust and gold), each one colour stepped into tones. It is its own models, cut into the mantel's blocks, drawn only while dressed.
- The item is the whole fireplace, dressed and lit, at a third of its size. The garland's icon is a 16 by 16 map (`tools/item_icons/mantel_garland.txt`): a swag of autumn leaves, red and gold by turns, sagging between its ends, with a little orange pumpkin hung at its lowest point, in the compact family.
- **The first previews**, all fixed before commit:
  - the fieldstone, in stones of one length, read as bricks; it now lays stones of seven lengths in turn;
  - the garland's drapes hung straight down and read as candles; they are now swags that sag between two points.

## Connections
- Existing input producer: cobblestone, planks, a campfire and a smooth stone slab for the fireplace; leaves, a pumpkin and a string for the garland. All vanilla, from the first day.
- Existing output consumer: none. It is a decoration, and the room round it is the next parts'.
- Technology/magic connections (or infrastructure/cosmetic applicability): cosmetic.
- Reachable entry path: from the first day, with no other branch.
- Which connections are required vs optional; trade and solo routes: all optional, crafted solo, and tradeable like any item.
- How this specialty stays useful without mastering every other branch: it is furniture for anyone's house.

## Balance and automation
- The fireplace costs five cobblestone, two planks, a campfire and a stone slab, and gives nothing back but itself.
- **It makes nothing.** Its fire burns no fuel and does no work. It doesn't cook, smelt, heat or spread fire.
- The garland costs two leaves, a pumpkin and a string. It is kept by the fireplace and given back whole, by hand or when the fireplace is broken. Nothing turns it back into what made it.
- No loop: neither recipe makes anything that leads back to itself (the audit refuses any loop).

## Multiplayer and persistence
- Plain blocks with no block entity: whether it is lit and dressed is in its blocks' state (`lit`, `garland`, on all nine). It is changed on the server only, by the player using it, and saved with the world. Nothing ticks.
- The crackle and smoke are each client's own, drawn near the firebox while lit.
- It can't be pushed by a piston.
- IDs (stable from release): the block and item `stone_fireplace`; its states `part` (0 to 8), `facing`, `lit`, `garland`. The item `mantel_garland`.
- Its recipes follow the agriculture switch. The blocks and items stay registered when it is off.

## Dependencies and assets
- No new dependency. Models, blockstate, loot table and textures are generated by `tools/hearth_data.py` from `tools/hearth.py`, through `tools/generate_material_data.py` and `tools/generate_textures.py`. Nothing is copied, traced or recoloured from Minecraft's textures.

## Verification
Local checks run on this branch, and their results:
All on the first run:
- `python3 tools/check_mod_data.py`: PASS (8,116 material IDs, from 8,114), including the new `check_hearth`, and the art check of every block and item model.
- `python3 scripts/check_repository.py`: PASS.
- `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
- `python3 tools/check_icon_maps.py`: PASS (275 maps, with the garland's; no new warning).
- `javac` scan of the new and changed Java for syntax errors: none. The full compile runs in CI.
- The models were previewed offline, textured, from two sides (review aids, not committed); what the previews showed is under How it is drawn.

Game tests (CI only, not run locally), `HearthGameTests`:
- **The fireplace lights and takes its garland**:
  - it won't stand with its top corner taken, and stands whole, three across and three up, facing the player, out and bare, where there is room;
  - flint and steel lights every block, wearing the flint;
  - its firebox alone gives light 15;
  - a shovel puts it out;
  - a garland drapes every block and is spent, and a second isn't;
  - sneaking with an empty hand takes the garland back.
- **The fireplace comes away whole**: lit with a fire charge and dressed, then broken at its firebox, all nine blocks go, and it drops itself and its garland once.

`HearthClientGameTests` takes three screenshots of a room's corner (a spruce floor and a plank back wall): a fireplace lit and dressed, with a second beside it out and bare, from the room, close by, and by night.

Not run: a hand playtest, and a two-client playtest.

## World and event applicability
Not seasonal: it is made and burns all year. It doesn't generate in the world.

## Rollout and open questions
- Part 2 (the woodstove) follows.
- Open: whether a lit fireplace should warm players near it once a cold-weather system exists; whether it should take other mantel dressings (a winter garland, candles).
