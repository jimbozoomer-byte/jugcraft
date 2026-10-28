# Cozy Fall Hearth

Status: parts 1 (the hearth) and 2 (the stove) implemented. CI builds them and runs their game tests and client screenshots; not yet played by hand. Parts 3 to 5 are planned below.
Proposal issue: none. The owner asked for it directly on 28 October 2026, as set 5 of the ten sets of fall and Halloween decor ("Then I want you to do 1-10 in parts in that order one by one"), drawn by their rules.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery tier; made from cobblestone, planks, a campfire and a stone slab, and (part 2) iron, a furnace, copper and the kitchen's mulling spices.
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

### Part 2: the stove
- **Cast-Iron Woodstove** (one block): a parlour box stove 78 cm wide, 62 cm deep and 1 m high, on four cast legs.
  - Its firebox door is glazed with two mica windows, with a nickel latch and iron hinges. Below it is the ash-pan door with its nickel draft knob.
  - Its sides are cast panels inside a raised bead. A nickel towel rail runs across its front, and its top plate has two round lids.
  - Its stovepipe leaves its back, through a collar and a damper, into the wall behind it through a thimble.
  - **It burns as the fireplace does.** Flint and steel or a fire charge lights it, and a shovel puts it out. While lit, the fire shows through the mica, moving in the fireplace's smooth bands, it crackles, and it gives light 13.
  - **It heats what stands on it**, as the Kitchen Stove and a campfire do (block tag `jugcraft:heat_sources`): a Cooking Pot (so mint tea, hot cocoa and mulled cider brew on it), a Skillet, a Canning or Candy Kettle, a Tea Kettle or a Simmer Pot.
  - Its hot top burns what stands on it, unless it is sneaking, as a magma block does.
  - It burns no fuel and cooks nothing itself: the Kitchen Stove's hob is the cooking range, and this is a heater.
  - Recipe: three iron ingots over an iron ingot, a furnace and an iron ingot, over two iron ingots at the corners (its legs).
- **Tea Kettle**: a copper kettle 28 cm across and 44 cm to the top of its handle. Its body steps in tier by tier to its lid, its spout rises from its side, and a brass handle with a turned wooden grip runs over it in line with the spout.
  - It stands on any floor, or on a heat source.
  - **Heated, it boils**: steam rises from its spout, it simmers aloud now and then, and **its lid rattles**, jumping and tipping on its rim unevenly, as a lid on a boil does.
  - It brews nothing: tea is the Cooking Pot's.
  - Recipe: an iron nugget over two copper ingots, over three copper ingots.
- **Simmer Pot**: a cream enamel saucepan 25 cm across with a cobalt rim and handle, of spiced water with apple slices, cinnamon sticks and cloves floating in it, set on the stove to scent the room.
  - It stands on any floor, or on a heat source.
  - **Heated, it simmers**: its surface ripples (an animated texture in smooth bands) and wisps of steam rise from it.
  - It never boils dry and makes nothing.
  - Recipe (shapeless): an iron ingot, a water bucket (the bucket is given back), an apple and mulling spices.
- The kettle and the pot come off when what they stand on goes.

### Planned parts
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

### Part 2's stove
- Built the same way, at real size (`tools/hearth_data.py` woodstove(), kettle(), simmer_pot()).
- **Cast iron**: a flat dark field inside a raised bead, lit along its top and left and shaded along its bottom and right, as a stove's panels are cast. The top plate has two round lids, each a darker ring round a lit boss.
- **Nickel**: flat bands, lit along one edge. **Mica**: smoky amber with a lighter diagonal sheen every fourth texel, as pressed sheets of mica catch the light.
- **The fire behind the mica is the fireplace's**: the same animated texture, full-bright, drawn only while lit, one texel to a pixel behind each pane.
- **The kettle**: copper in flat bands, each tier lit along its top, so the body reads as a turned dome; brass for the handle and knob, oak for the grip.
- **The pot**: cream enamel with a cobalt rim and handle. Its water is the brew's colour in rings round the middle. Simmering, it is an animated strip of rings spreading out a step each frame, the frames blended, so it ripples without flashing.
- **Moving parts move.** The kettle's lid is its own model, the blockstate's `runner`, drawn by `TeaKettleRenderer`. While the kettle is heated, the lid jumps up to 0.35 pixels and tips up to 5 degrees about its back edge, on two beats out of step, so it rattles unevenly.
- **The first previews**, all fixed before commit:
  - the kettle's body, two crossed boxes in a dimpled hammered pattern, read lumpy and noisy, like a pumpkin; it is now tiers stepping in to the lid, each one tone lit along its top;
  - its handle ran across the spout; it now runs in line with it, as a kettle's does;
  - the simmer pot's rim was a solid box that hid what was in it; it is now a ring.

## Connections
- Existing input producer: cobblestone, planks, a campfire and a smooth stone slab for the fireplace; leaves, a pumpkin and a string for the garland. All vanilla, from the first day.
- Existing output consumer: none for the fireplace and the garland. The woodstove heats the kitchen's pots: a Cooking Pot on it brews what it brews on a campfire (mint tea, hot cocoa, mulled cider), as do a Skillet, the Canning Kettle and the Candy Kettle.
- Part 2's inputs: iron and a furnace for the stove; copper and an iron nugget for the kettle; an iron ingot, a water bucket, an apple and the kitchen's mulling spices (from cinnamon or juniper) for the simmer pot.
- Technology/magic connections (or infrastructure/cosmetic applicability): cosmetic, and the kitchen's heat sources (part 2).
- Reachable entry path: from the first day, with no other branch.
- Which connections are required vs optional; trade and solo routes: all optional, crafted solo, and tradeable like any item.
- How this specialty stays useful without mastering every other branch: it is furniture for anyone's house.

## Balance and automation
- The fireplace costs five cobblestone, two planks, a campfire and a stone slab, and gives nothing back but itself.
- **It makes nothing.** Its fire burns no fuel and does no work. It doesn't cook, smelt, heat or spread fire.
- The garland costs two leaves, a pumpkin and a string. It is kept by the fireplace and given back whole, by hand or when the fireplace is broken. Nothing turns it back into what made it.
- The woodstove costs seven iron ingots and a furnace and burns no fuel, like a campfire or the Kitchen Stove. It heats a pot as a campfire does and no faster, so it adds a heat source without adding any output.
- The kettle and the simmer pot make nothing. The pot's water bucket is given back when it is made.
- No loop: no recipe makes anything that leads back to itself (the audit refuses any loop).

## Multiplayer and persistence
- Plain blocks with no block entity: whether it is lit and dressed is in its blocks' state (`lit`, `garland`, on all nine). It is changed on the server only, by the player using it, and saved with the world. Nothing ticks.
- The crackle and smoke are each client's own, drawn near the firebox while lit.
- It can't be pushed by a piston.
- Part 2: the woodstove's `lit` is in its block state, as the fireplace's is. The kettle's and the pot's `heated` is worked out on the server from the block below, when either is placed and whenever that block changes (a stove lit or put out), so nothing ticks. The kettle's block entity (`tea_kettle`) keeps nothing: it is there for its renderer, which works out the lid's rattle from the game time. Steam and sounds are each client's own.
- IDs (stable from release): the block and item `stone_fireplace`; its states `part` (0 to 8), `facing`, `lit`, `garland`. The item `mantel_garland`. Part 2's blocks and items `woodstove` (`facing`, `lit`), `tea_kettle` (`facing`, `heated`, model-only `runner`; block entity `tea_kettle`), `simmer_pot` (`facing`, `heated`); the woodstove is in `jugcraft:heat_sources`.
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
- Part 2, all on the first run:
  - `python3 tools/check_mod_data.py`: PASS (8,119 material IDs). `check_hearth` now also checks the woodstove's light and burn against `tools/hearth.py`, that it is a heat source, and that its fire is full-bright on the fireplace's texture. It checks the kettle's lid rattle and hinge against the table, its renderer and its runner model, and the simmer pot's animation.
  - `python3 scripts/check_repository.py`: PASS.
  - `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
  - `python3 tools/check_icon_maps.py`: PASS (no new map; the stove's items are 3D).
  - `javac` scan of the new and changed Java for syntax errors: none in them. The full compile runs in CI.

Game tests (CI only, not run locally), `HearthGameTests`:
- **The fireplace lights and takes its garland**:
  - it won't stand with its top corner taken, and stands whole, three across and three up, facing the player, out and bare, where there is room;
  - flint and steel lights every block, wearing the flint;
  - its firebox alone gives light 15;
  - a shovel puts it out;
  - a garland drapes every block and is spent, and a second isn't;
  - sneaking with an empty hand takes the garland back.
- **The fireplace comes away whole**: lit with a fire charge and dressed, then broken at its firebox, all nine blocks go, and it drops itself and its garland once.
- **Part 2: the woodstove heats what stands on it**:
  - two stand facing the player, out, and a kettle and a simmer pot stand on them, cold;
  - flint and steel lights them (wearing the flint twice), and lit they give light 13 and heat the kettle and the pot;
  - a shovel puts one out, and its kettle cools;
  - a Cooking Pot on the other, lit, counts it as heat;
  - broken, the stove drops itself, and the kettle comes off it and drops.
- **Part 2: the kettle stands on a floor or a fire**: on stone it stands cold; a simmer pot on a lit campfire simmers, and stops when the fire is put out; with its floor gone, the kettle comes off and drops.

`HearthClientGameTests` takes four screenshots of a room's corner (a spruce floor and a plank back wall). It holds a fireplace lit and dressed, a second beside it out and bare, and part 2's two woodstoves lit along the wall, a kettle boiling on one (its lid drawn by `TeaKettleRenderer`) and a simmer pot on the other. The shots are from the room, close by, at the stoves, and by night.

Not run: a hand playtest, and a two-client playtest.

## World and event applicability
Not seasonal: it is made and burns all year. It doesn't generate in the world.

## Rollout and open questions
- Part 3 (the woodpile: a firewood rack and a chopping block with its axe) follows.
- Open: whether a lit fireplace or woodstove should warm players near it once a cold-weather system exists; whether the fireplace should take other mantel dressings (a winter garland, candles); whether the kettle should give hot water for the kitchen's drinks.
