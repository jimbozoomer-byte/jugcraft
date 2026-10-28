# Cozy Fall Hearth

Status: all five parts implemented: 1 (the hearth), 2 (the stove), 3 (the woodpile), 4 (the comforts) and 5 (the lights). CI builds them and runs their game tests and client screenshots; not yet played by hand.
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

### Part 3: the woodpile
- **Firewood Rack**: a wrought-iron log rack 87 cm wide, 37 cm deep and 94 cm high. It has two end frames (uprights under a top bar curled at its ends) on four feet, joined by a front and a back rail.
  - Use a log of any kind on it to stack it, up to 16; an empty hand takes back the one stacked last.
  - It shows each log as a split log, four to a layer from the bottom left, their ends to the front: pale end grain with the bark at one corner.
  - It keeps each log as it went in, so it gives back the kinds it was given. Broken, it drops them all.
  - A comparator reads how full it is.
  - Full, it says so and keeps the log in your hand.
  - Recipe: iron bars down the two sides, over three iron ingots.
- **Chopping Block**: a sawn round of oak 44 cm across and 45 cm high, its bark on and its rings on top, with a hatchet sunk in it.
  - **Use a log on it to split it**: the log is spent and its planks given, exactly what crafting it gives (the log's own crafting recipe, never more), with the chop's sound and chips of that wood.
  - **Its hatchet swings** each time, up about its grip and back down into the round.
  - Anything that isn't a log does what it would on any block.
  - Recipe (shapeless): a log and an iron axe (its hatchet).

### Part 4: the comforts
- **Blanket Ladder** (one block across, two up): two oak rails 1.75 m long and 62 cm apart, leaning 22.5 degrees back from their feet to the wall, with five turned rungs.
  - It leans on the wall you aim at: its upper block needs a sturdy wall behind it, and it falls when the wall goes.
  - **Use a wool carpet of any colour on it** to fold it over a rung as a throw, the top rung first, up to three. Each throw hangs a long fold in front and a short one behind, in a twill weave with a striped border and a fringe, in its carpet's colour.
  - An empty hand takes back the throw hung last. Broken, it gives them all back.
  - Full, it says so and keeps the carpet in your hand.
  - Recipe: planks down both sides, a stick across the middle.
- **Knitting Basket**: a square wicker basket 37 cm across and 19 cm high, with a bentwood handle over it.
  - **Use a ball of yarn on it** to put it in, up to four, each drawn in its own dyed colour: two in front on its floor, two piled behind them.
  - **Use knitting needles on it** to stick them in, crossed; they are kept as they are, worn or not.
  - An empty hand takes back the needles first, then the ball put in last. Broken, it gives it all back.
  - It knits nothing: the needles do that in the hand.
  - Recipe (shapeless): a foraging basket and a ball of yarn.

### Part 5: the lights
- **Hurricane Lamp**: a table oil lamp 37 cm high: a brass foot and stem under a brass font, its burner with a wick knob, and a glass chimney narrow at its neck, swelling round the flame and narrowing again at its top. It is the one for a table; set 3's Barn Lantern is the one for a wall.
  - Flint and steel or a fire charge lights it, and an empty hand turns its wick down.
  - Lit, its chimney glows from the flame inside and it gives light 12. It burns no oil.
  - Recipe: glass panes round a torch, over a copper ingot.
- **Mason-Jar Candles**: wax candles poured into half-gallon mason jars, 11 cm across and 22 cm high, each with a twine bow round its neck.
  - One to four stand in a block, and they are lit, put out and added to just as vanilla's candles are. Lit, their glass glows and each gives light 3.
  - Recipe (shapeless): a mason jar from the pantry, a candle and a string.

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

### Part 3's woodpile
- Built the same way, at real size (`tools/hearth_data.py` firewood_rack(), chopping_block()).
- **Split firewood**: each piece's end is three texels square. Its end grain is pale, in one of two tones by its place in the stack, so neighbours never match. The bark is at one outer corner and the heart a tone darker at the opposite one, as a quarter split from a round shows.
  - Its long sides are split wood or bark, along the piece.
  - The rack has a model for each count of logs, 0 to 16, all on one texture, so each log shows as it goes on.
- **The round**: oak bark in stripes up it, its top rings about a dark pith, with a check (a crack) from the pith to the bark, as a drying round splits.
- **The hatchet**: an iron head and a hickory handle, its grain along it. **Moving parts move.** It is its own model, the blockstate's `runner`, drawn by `ChoppingBlockRenderer`. On each chop it swings 55 degrees up about its grip and back down into the round over eight ticks, a smooth arc.
- The rack doesn't draw the vanilla logs' own models: shrunk to a piece of firewood they would put four texels to a pixel. Its firewood is its own, one texel to a pixel.
- **The first previews**, fixed before commit:
  - the split ends, with bark along two edges, read as a maze pattern; each end now has its bark at one corner;
  - the round's bark was the fireplace logs' charred one and read as a dark crate; it is now oak's grey-brown.

### Part 4's comforts
- Built the same way, at real size (`tools/hearth_data.py` blanket_ladder(), knitting_basket()).
- **The ladder**: oak, its grain along its rails and rungs. The rails are each one box turned 22.5 degrees back about its foot, and the rungs sit on the line of the lean.
- **Throws and yarn in their own colours.** Each is drawn by its renderer (`BlanketLadderRenderer`, `KnittingBasketRenderer`) over a texture in greys, tinted by the carpet's dye or the yarn's own colour, as the Aura Candle's wax is (`TintedBoxes`).
  - A throw is a twill whose diagonal steps a texel a row, with a border of two darker rows near the hem and a fringe on the hem, every other texel cut away. It is drawn in the same frame on every rung, so its border and fringe fall the same on each.
  - A ball of yarn is three crossed boxes, so its corners are rounded off, wound in strands two texels wide on a slant, in three tones by turns.
- **Wicker**: weavers a texel high passing over and under the stakes two texels at a time, each row a step along from the last.
- **The first previews**, fixed before commit:
  - the basket's walls hid its yarn, which read as flat slabs; the walls are now lower and the two balls at the back piled up behind the front two;
  - its first wicker, in long blocks, read as brickwork; it is now the finer over-and-under weave.

### Part 5's lights
- Built the same way, at real size (`tools/hearth_data.py` hurricane_lamp(), mason_jar_candles()).
- **Glass, as the Barn Lantern's**: cut out inside a pale rim with a lit streak. At real size the lamp's chimney and the jars are two or three texels across, too narrow to show a clear pane, so they read as pale glass.
- **Glows cut out, never flicker.** Lit, the glass is warm glass lit from inside, full-bright and unshaded, in steady bands: palest at its middle rows where the flame is, a tone deeper towards its ends and rim. A flame inside it couldn't be seen through glass that narrow, so the lit glass is the glow. The jars' flames are vanilla's candle flames, standing inside them.
- Brass in flat bands, cream wax, a dark wick, and twine round each jar's neck with its bow to the front.
- **The first previews**, fixed before commit: the glass, too narrow to see through, hid the lamp's flame when lit. The lit glass now glows instead.

## Connections
- Existing input producer: cobblestone, planks, a campfire and a smooth stone slab for the fireplace; leaves, a pumpkin and a string for the garland. All vanilla, from the first day.
- Existing output consumer: none for the fireplace and the garland. The woodstove heats the kitchen's pots: a Cooking Pot on it brews what it brews on a campfire (mint tea, hot cocoa, mulled cider), as do a Skillet, the Canning Kettle and the Candy Kettle.
- Part 5's inputs: glass panes, a torch and copper for the lamp; the pantry's mason jar, a candle and a string for the jar candles. Its outputs: light.
- Part 4's inputs: planks and a stick for the ladder; the foraging basket and a ball of yarn for the knitting basket. Its outputs: each gives back what it holds. The basket holds the knitting's yarn and needles, kept as they are.
- Part 3's inputs: iron bars and iron for the rack; a log and an iron axe for the chopping block. Its outputs: the rack gives back what it holds, and the chopping block gives a log's planks.
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
- The rack keeps the logs put on it and gives back exactly those, each kind as it went in. It splits, grows and multiplies nothing.
- The ladder and the basket keep what is put on or in them and give back exactly those: the same carpets; the same balls, each its colour; the same needles, as worn as they were.
- The lamp burns no oil; it gives light 12, a little less than a lantern's 15. The jar candles give a vanilla candle's light (3 each, 12 for four) and drop one for each jar.
- The chopping block gives exactly what crafting the log gives (it asks the game for the log's own crafting recipe), never more, so it saves no wood and makes no loop. It costs an iron axe, which stays in it as its hatchet; it doesn't come back when the block is broken.
- No loop: no recipe makes anything that leads back to itself (the audit refuses any loop).

## Multiplayer and persistence
- Plain blocks with no block entity: whether it is lit and dressed is in its blocks' state (`lit`, `garland`, on all nine). It is changed on the server only, by the player using it, and saved with the world. Nothing ticks.
- The crackle and smoke are each client's own, drawn near the firebox while lit.
- It can't be pushed by a piston.
- Part 2: the woodstove's `lit` is in its block state, as the fireplace's is. The kettle's and the pot's `heated` is worked out on the server from the block below, when either is placed and whenever that block changes (a stove lit or put out), so nothing ticks. The kettle's block entity (`tea_kettle`) keeps nothing: it is there for its renderer, which works out the lid's rattle from the game time. Steam and sounds are each client's own.
- Part 3: the rack's logs are kept by its block entity (`firewood_rack`, saved as `logs`), changed on the server only and saved with the world. What it shows is its block state (`logs`, 0 to 16), so nothing is sent but the state. Splitting a log is the server's: it checks the item is a log and looks up its own crafting recipe there. The chopping block's swing is a block event, so every client near it sees it. Its block entity (`chopping_block`) keeps nothing.
- Part 4: the ladder's throws (`blanket_ladder`, saved as `throws`, on its first block) and the basket's yarn and needles (`knitting_basket`, saved as `yarn` and `needles`) are kept by their block entities. They are changed on the server only, saved with the world and sent to clients, which draw them. The basket's needles also show in its block state (`needles`). The ladder needs its wall, checked on the server when placed and when the wall changes.
- Part 5: the lamp's `lit` is its block state. The jar candles are vanilla's candle (`candles`, `lit`, `waterlogged`; the block tag `minecraft:candles`), so they light, go out and take more jars just as vanilla's do. Nothing ticks; their flames are vanilla's particles.
- IDs (stable from release): the block and item `stone_fireplace`; its states `part` (0 to 8), `facing`, `lit`, `garland`. The item `mantel_garland`. Part 2's blocks and items `woodstove` (`facing`, `lit`), `tea_kettle` (`facing`, `heated`, model-only `runner`; block entity `tea_kettle`), `simmer_pot` (`facing`, `heated`); the woodstove is in `jugcraft:heat_sources`. Part 3's blocks and items `firewood_rack` (`facing`, `logs` 0 to 16; block entity `firewood_rack`, its saved `logs`) and `chopping_block` (`facing`, model-only `runner`; block entity `chopping_block`). Part 4's blocks and items `blanket_ladder` (`facing`, `part` 0 to 1; block entity `blanket_ladder`, its saved `throws`) and `knitting_basket` (`facing`, `needles`; block entity `knitting_basket`, its saved `yarn` and `needles`). Part 5's blocks and items `hurricane_lamp` (`facing`, `lit`) and `mason_jar_candle` (`candles` 1 to 4, `lit`, `waterlogged`).
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
- Part 3, all on the first run:
  - `python3 tools/check_mod_data.py`: PASS (8,121 material IDs). `check_hearth` now also checks the rack's capacity against `tools/hearth.py` and that it has a model for each count. It checks that the chopping block splits by the log's own crafting recipe, and that its hatchet is drawn by its renderer from its runner model, swung by the table's numbers about its grip.
  - `python3 scripts/check_repository.py`: PASS.
  - `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
  - `python3 tools/check_icon_maps.py`: PASS (no new map; the woodpile's items are 3D).
  - `javac` scan of the new and changed Java for syntax errors: none. The full compile runs in CI.
  - Part 3's first CI run failed to compile: in 26.3 `SoundEvents.AXE_STRIP` is a sound holder, not a sound, so `playSound` refused it. The chopping block now plays its value, as the Spooky Sign does.
- Part 4, all on the first run:
  - `python3 tools/check_mod_data.py`: PASS (8,123 material IDs). `check_hearth` now also checks the ladder's size, throws and rungs against `tools/hearth.py`, and that its renderer hangs the throws at the model's rungs. It checks the basket's capacity, both renderers' registration and their textures.
  - `python3 scripts/check_repository.py`: PASS.
  - `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
  - `python3 tools/check_icon_maps.py`: PASS (no new map; the comforts' items are 3D).
  - `javac` scan of the new and changed Java for syntax errors: none. The full compile runs in CI.
  - Part 4's first CI run failed to compile its tests: 26.3 has no `Items.RED_CARPET` (nor the other colours' fields). The tests now take their carpets from vanilla's `minecraft:wool_carpets` tag, the one the ladder takes, so they don't depend on what each colour's item is called.
- Part 5, all on the first run:
  - `python3 tools/check_mod_data.py`: PASS (8,125 material IDs). `check_hearth` now also checks the lamp's light, its registration and its lit chimney's glow. It checks the jars' sizes, flame height and layouts against `tools/hearth.py`, and every count's lit model.
  - `python3 scripts/check_repository.py`: PASS.
  - `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
  - `python3 tools/check_icon_maps.py`: PASS (no new map; the lights' items are 3D).
  - `javac` scan of the new and changed Java for syntax errors: none. The full compile runs in CI.

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
- **Part 3: the firewood rack holds sixteen logs**:
  - it stands empty, and a stick isn't stacked;
  - fifteen oak logs and a birch go on it, each spent, and a seventeenth doesn't;
  - a comparator reads it full (15);
  - an empty hand takes back the birch, the last stacked;
  - broken, it drops its fifteen oak logs and itself.
- **Part 3: the chopping block splits a log**: a spruce log is spent and splits into four spruce planks, as crafting it gives; cobblestone isn't split; the chop marks its hatchet's swing.
- **Part 4: the blanket ladder takes three throws**:
  - it won't stand without a wall to lean on, and leans on one, two blocks high, facing the player;
  - a stick isn't hung; three carpets from vanilla's `minecraft:wool_carpets` tag are, each spent, the first on the top rung; a fourth isn't;
  - an empty hand takes back the third;
  - its wall gone, it falls, dropping itself and the other two.
- **Part 4: the knitting basket holds yarn and needles**:
  - four balls of four colours go in, each kept in its colour, and a fifth doesn't;
  - worn needles go in and show;
  - an empty hand takes back the needles first, then the last ball;
  - broken, it gives back its three balls and itself.
- **Part 5: the hurricane lamp lights**: it stands out; flint and steel lights it, wearing the flint, and it gives light 12; an empty hand turns it down.
- **Part 5: the mason-jar candles light as candles**: a second jar joins the first; flint and steel lights them both, giving light 6 (three each); an empty hand puts them out.

`HearthClientGameTests` takes seven screenshots of a room's corner (a spruce floor and a plank back wall). It holds:
- a fireplace lit and dressed, and a second beside it out and bare;
- part 2's two woodstoves lit along the wall, a kettle boiling on one (its lid drawn by `TeaKettleRenderer`) and a simmer pot on the other;
- part 3's firewood rack full of logs and a chopping block before it (its hatchet drawn by `ChoppingBlockRenderer`);
- part 4's blanket ladder with three throws of different colours (carpets from vanilla's tag), and a knitting basket of four coloured balls with its needles. The throws and yarn are drawn by their renderers, so the picture shows their colours;
- part 5's hurricane lamp and three mason-jar candles lit on the floor before the fireplace.

The shots are from the room, close by, at the stoves, at the woodpile, at the comforts, at the lights, and by night.

Not run: a hand playtest, and a two-client playtest.

## World and event applicability
Not seasonal: it is made and burns all year. It doesn't generate in the world.

## Rollout and open questions
- Set 5 is complete with part 5. Set 6 (the Haunted Midway) follows.
- Open: whether a lit fireplace or woodstove should warm players near it once a cold-weather system exists; whether the fireplace should take other mantel dressings (a winter garland, candles); whether the kettle should give hot water for the kitchen's drinks.
