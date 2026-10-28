# Halloween Party Table

Status: parts 1 (the candy: the Candy Corn Dispenser), 2 (the cake stands: the Spider Cupcake Tower and the Ghost Meringue Platter, and the Ghost Meringue) 3 (the trays: the Mummy Sausage Tray, the Witch-Finger Cauldron and the Eyeball Jelly Mould, and their foods) and 4 (the cake: the Pumpkin Layer Cake) implemented. CI builds them and runs their game tests and client screenshots; not yet played by hand. Part 5 is planned (below).
Proposal issue: none. The owner asked for it directly on 28 October 2026, as set 8 of the ten sets of fall and Halloween decor ("Then I want you to do 1-10 in parts in that order one by one"), drawn by their rules.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery tier; the dispenser is made from glass, iron and black dye, and filled with candy corn; the cupcake tower from iron nuggets and black dye, the meringue platter from clay and black dye, and Ghost Meringues from an egg, sugar and cocoa; the sausage tray from iron and a bowl, the witch-finger cauldron from iron, the jelly mould from clay and bone meal, and their foods from a cooked porkchop and pastry dough, wheat, sugar and green dye, and sugar, sweet berries, bone meal and a glow berry.
Primary specialty and supported player role: building. A host lays out a Halloween party, from the candy to the cake. Nothing in it is needed to progress.

## Player experience
The spread for a Halloween party, and what the guests help themselves to.

### Part 1: the candy
- **Candy Corn Dispenser**: a jumbo gumball machine 86 cm high, standing on the floor or the ground.
  - Its glass globe is 41 cm across, in five rings, on a black cast-iron pedestal banded in orange enamel over a flared foot.
  - A brass faceplate on the pedestal's front carries the crank, and below it the chute behind a brass flap. A black lid with a brass knob closes the globe's top.
  - **It stands facing whoever places it.**
  - **Candy corn used on it pours in**, as much of the stack as fits, up to 64 pieces. The globe shows how full it is, at four levels.
  - **An empty hand turns the crank** once round, in a second.
    - As it comes round, one piece of candy corn drops out of the chute onto the floor in front of it, and the flap swings out and back.
    - It turns again only once it has come round.
    - An empty dispenser only rattles.
  - Recipe: five glass over two iron ingots and black dye.
  - Broken, it drops itself and every piece it held.

### Part 2: the cake stands
Both are table pieces. **On a Feast Table**, whose top is 3 pixels below a block, each is set down onto the table's top rather than standing above it; it stands back up if the table goes.
- **Spider Cupcake Tower**: a party cupcake stand 50 cm high, three round tiers of black enamelled steel on a centre rod.
  - It holds 13 Spiderweb Cupcakes: eight round the bottom tier, four on the middle and one on top. Every third has a licorice spider sitting on its frosting.
  - **A Spiderweb Cupcake used on it goes on**, one at a time, filling it from the bottom tier up. **An empty hand takes one off**, from the top down, into the inventory.
  - Recipe: iron nuggets round black dye, over a rod and a foot of nuggets.
- **Ghost Meringue Platter**: an oval platter of black-glazed earthenware 35 cm long, on a low foot.
  - It holds 9 Ghost Meringues in three rows of three.
  - **A Ghost Meringue used on it goes on**, one at a time; **an empty hand takes one off**.
  - Recipe: two clay balls either side of black dye.
- **Ghost Meringue**: a food, a meringue piped into a ghost 9 cm high, its peak curled over, with two cocoa eyes.
  - It restores 1 hunger, 0.1 saturation, and composts like other sweets.
  - Six are made from an egg, two sugar and a cocoa bean.
  - It is candy, so a Candy Bag takes it.
- Broken, either stand drops itself and everything still on it. Anything else used on one changes nothing.

### Part 3: the trays
Table pieces too, set down onto a Feast Table as the cake stands are. Each holds a food of its own; that food used on it goes on, one at a time, and an empty hand takes one off.
- **Mummy Sausage Tray**: a black baking tray 40 cm by 28 cm with low sides and a white ramekin of mustard in its middle.
  - It holds 8 Mummy Sausages in two rows of four, either side of the mustard.
  - Recipe: iron nuggets either side of a bowl, over three iron ingots.
- **Witch-Finger Cauldron**: a little cast-iron cauldron 25 cm across on three stubby legs, filled with cookie crumbs.
  - It holds 10 Witch Fingers poking up out of the crumbs at angles.
  - Recipe: a cauldron's shape in iron ingots, on two nugget legs.
- **Eyeball Jelly Mould**: a white plate 28 cm across, on which a ring of clear red jelly 22 cm across stands turned out of its mould.
  - It holds 8 segments round the ring's hole, each with an eyeball set in it looking out. An Eyeball Jelly used on it sets in the next segment.
  - Recipe: two clay balls either side of bone meal.
- **Their foods**:
  - **Mummy Sausage**: a sausage wound in strips of pastry, two mustard eyes. 3 hunger and 0.5 saturation; four from a cooked porkchop and pastry dough. Being meat, it doesn't compost.
  - **Witch Finger**: a finger of green shortbread with an almond nail and knuckle creases. 2 hunger and 0.1 saturation, a cookie's worth; eight, as cookies are, from two wheat, sugar and green dye.
  - **Eyeball Jelly**: a serving of clear red jelly with an eyeball in it. 2 hunger and 0.2 saturation; four from sugar, two sweet berries, bone meal for its gelatin and a glow berry for the eyes.

### Part 4: the cake
- **Pumpkin Layer Cake**: an eighth cake beside the [seven the owner drew](cakes.md), made and served as they are.
  - **Its look**: three layers of spiced pumpkin sponge with cream cheese between them, coated in orange buttercream, with black chocolate ganache poured over its top and dripping down its sides. Eight kernels of candy corn lie round its top, pointing in to a fondant pumpkin in its middle.
  - **Baking**: Cake Batter, a sugar, a pumpkin and candy corn make the Raw Pumpkin Layer Cake, baked in the Hearth Oven as the other cakes are; left in too long it comes out a Burnt Cake.
  - **Serving**: it is set down whole, a block wide and nine texels tall, as the others are. A hungry player eats a quarter; a knife cuts a quarter off as a Slice of Pumpkin Layer Cake (4 hunger, 0.6 saturation, as the other cakes' slices). A cut shows its layers, and the candy corn and the pumpkin are cut with the quarters.

### Planned
- **Part 5: the table**: a bat table runner, goodie bags and skull goblets.

## How it is drawn (docs/ART_DIRECTION.md, docs/NATURAL_TEXTURES.md)
- Built from boxes at real size, one texel to a model pixel (`tools/party_table_data.py`), in the Haunted Midway's manner, whose palettes it shares.
- **Cast iron**: black, lit along its top row and shaded along its foot. The pedestal's second row is orange enamel, lit, all the way round.
- **Brass** (the faceplate, crank, flap and knob): lit like a gold block, its top row bright, its left edge catching the light, its foot dark.
- **Glass**: clear and faint, with a streak of reflection a texel wide down the globe's north and west faces, brightest at its top. No pale rim marks the steps between the rings. The flat glass at each step covers only the step, so no glass lies inside the globe.
- **Candy corn**: kernels two texels wide and three tall, a white tip, an orange middle and a yellow foot, each kernel lit on its left and a tone deeper on its right. Each column of kernels sits a texel lower than the last, as they settle. They are counted down from the candy's surface, so they run on unbroken from one ring of the globe to the next.
- **Moving parts move**, from the blockstate's `runner` models: `CandyCornDispenserRenderer` turns the crank once round about its axle, clockwise as its front is seen, easing in and out, and then swings the flap out about its top and back.
- **The cupcakes** (part 2), 9 cm across: a pleated paper case, orange and black a texel at a time; chocolate frosting, lit along its top, with a strand of white web across its top and swirl; a licorice spider on every third, its body and its legs crossed in an X.
- **The meringue ghosts**: piped in a cone of four steps (foot, body, shoulders and a peak curled over to one side), cream and white ridges a texel wide down them as a star nozzle pipes, the top row brightest; two cocoa eyes on the front, each a texel.
- **The stands**: the tower's tiers are round, two boxes crossed with their corners cut, in black enamel lit along the top; the platter is an oval the same way, in black glaze with a glint every third texel along its top.
- **On a Feast Table** each piece's models are drawn from copies (`_table`) set 3 pixels lower, so the piece stands on the table's top.
- **The Ghost Meringue's icon** is a 16x16 map (`tools/item_icons/ghost_meringue.txt`) in a new flat cream ramp, `meringue` (`tools/icon_materials.py`), with cocoa eyes in `dark_wood`.
- **The sausages** (part 3), 12 cm long and 5 cm thick: pastry bands a texel wide across them, the sausage showing between, their ends sausage; two dots of mustard for eyes near the front end. The tray is black enamel, the ramekin white porcelain with a mustard surface.
- **The witch fingers**, 14 cm long and 3 cm thick, from the tip down: an almond nail, a knuckle crease a tone deeper, then green shortbread lit along its left. Each leans out of the crumbs 22.5 degrees, the only turn a block model takes short of 45, except the two in the middle, which stand straight. The crumbs are two tones by turns with a lit crumb every third texel; the cauldron is the dispenser's black cast iron, round (two boxes crossed), with a lip.
- **The jelly**: clear red, see-through, lit along its top row, each segment 1.1 pixels square and 9 cm high round the hole. The corner segments are drawn in from the ring's outside, so the ring reads round. Each eyeball is a white cube with a blue iris on the side facing out, seen through the jelly.
- **The new icons** (`tools/item_icons/mummy_sausage.txt`, `witch_finger.txt`, `eyeball_jelly.txt`) are 16x16 maps in the existing flat materials: the sausage in `pancake` pastry and `terracotta` with `butter` eyes, the finger in `leaf` with a `pancake` nail, the jelly in `red_paint` on a `paper` plate with a `dew` iris.
- **The cake** (part 4) is drawn in the manner of the owner's cakes, which `tools/cake_art.py` reads off their drawing; it is not on the drawing, so `painted_faces` paints its faces in the same rows:
  - its sides are orange buttercream, lit at the top and shaded to the foot, with a lighter spatula stroke every fourth column, and the ganache's edge along the top running down in drips of set lengths (a different run on its front and side);
  - its inside is the ganache, then pumpkin sponge (a regular crumb: a darker texel every fifth along a slant, a lighter every seventh), cream cheese, sponge, cream cheese and sponge, with the buttercream coat a texel deep at either edge;
  - its top is ganache, its rim a tone deeper, with a short arc of gloss near its back left;
  - each candy corn kernel is three texels (white tip, orange, yellow foot);
  - the fondant pumpkin is ribbed orange with a green stem in its middle.
  - Its slice icon is the owner's Slice of Cake recoloured, as the others' are, here with its sponge and ganache and a candy-corn topping. Its crumb is taken from the sponge's rows alone.
- **The part 4 previews**, fixed before commit:
  - the kernels paired up at the corners into L shapes, so they now lie two to a side either side of the cut, pointing in at the pumpkin;
  - the gloss, a long slanting streak, read as a crack, so it is now a short arc;
  - the slice icon's crumb was sampled from the whole inside, cream cheese with it, and showed white specks; it now comes from the sponge alone.
- **The part 3 previews**, fixed before commit:
  - the jelly ring was eight equal cubes and read as a square, so its corner segments are now drawn in;
  - the first icons were too plain: the sausage's eyes were lost against its pastry, the finger read as a twig and the jelly as a red box. The sausage is now thicker, with its face set on the sausage between the bands; the finger swells at two creased knuckles; the jelly is a fluted mould on a plate, with a gloss.
- **The part 2 previews**, fixed before commit:
  - the cupcakes' web strand, drawn corner to corner, covered the whole of a two-texel face, so the frosting read white; the frosting is now chocolate, with the web only on its top;
  - the meringue ghosts were three boxes and read as little houses; they are now piped in four steps up to a curled peak.
- **The first previews**, fixed before commit:
  - the glass read as solid in the offline preview, which drew every part-clear texel opaque; the preview now blends glass over what is behind it, as the game's translucent pass does;
  - the candy's dark gaps between kernels speckled it like noise; they are gone;
  - with every kernel upright and in line, and each ring's texture starting at a white row, the candy read as flat horizontal bands; each column of kernels is now a texel lower than the last, counted from the candy's surface.

## Connections
- Existing input producer: candy corn (agriculture's own candy), glass, iron and black dye; Spiderweb Cupcakes (agriculture's own Halloween food); eggs, sugar, cocoa beans, clay and iron nuggets.
- Existing system it joins: trick-or-treating's Candy Bag, which takes the Ghost Meringue as candy (`c:foods/candy`). The dispenser holds and hands out candy corn; the stands hold the food made elsewhere.
- Existing output consumer: whoever eats the food, or puts it in a Candy Bag or a candy bowl.
- Technology/magic connections (or infrastructure/cosmetic applicability): cosmetic.
- Reachable entry path: from the first days, with a little iron and sand.
- Which connections are required vs optional; trade and solo routes: all optional, crafted solo, and tradeable like any item.
- How this specialty stays useful without mastering every other branch: it is a party's decoration for anyone's build.

## Balance and automation
- The dispenser costs five glass, two iron ingots and black dye, and gives back only itself.
- Candy corn goes in and comes out one for one. Nothing is made, and nothing is lost: broken, it drops all it held. No loop.
- It hands out one piece a second at most, by hand.
- The cupcake tower costs six iron nuggets and black dye, the meringue platter two clay balls and black dye. Each gives back only itself, and what goes on comes off one for one.
- The sausage tray costs three iron ingots, two nuggets and a bowl; the cauldron five iron ingots and two nuggets; the jelly mould two clay balls and bone meal. Each gives back only itself.
- The Pumpkin Layer Cake costs what the other cakes cost (a Cake Batter, a sugar and its own two ingredients, here a pumpkin and candy corn) and gives what they give: four quarters of 4 hunger.
- The trays' foods are crafted as vanilla's foods are, sugar and pastry adding to what they are made from:
  - four Mummy Sausages (12 hunger) come from a cooked porkchop (8) and pastry dough;
  - eight Witch Fingers (16) come from two wheat, as eight cookies do;
  - four Eyeball Jellies (8) come from two sweet berries and a glow berry (6), with sugar and bone meal.
  - None of them turns back into what it was made from, so there is no loop. Balance pending, as for the other new items.
- A Ghost Meringue is a light sweet (1 hunger, 0.1 saturation), half a cookie's food; six from an egg, two sugar and a cocoa bean. Balance pending, as for the other new items.

## Multiplayer and persistence
- **What it holds is the server's**: its block entity (`candy_corn_dispenser`) keeps how many pieces it holds, up to 64, saves them with the world and drops them when it is broken. Its block's `fill` (0 to 4) shows every client how full it is; the count itself isn't sent.
- **Its crank is the server's**: an empty hand sets it turning and schedules the piece's drop 20 ticks later. A block event marks the time on every side, so every client draws the crank turning and the flap swinging; that mark isn't saved. While it turns, another use does nothing.
- Only candy corn goes in, by a player's use in reach, and only as much as fits.
- It can't be pushed by a piston.
- **What a stand holds is its block's**: `servings`, in its block state, changed only by a player's use (one at a time, only its own food) and saved with the world. Every client sees how many from the block state. Broken, its loot table drops itself and that many of its food.
- `on_table` follows the block under it, as any block's shape does. It changes only how the stand is drawn and its shape.
- A piston pops a stand off, and it drops as if broken.
- IDs (stable from release):
  - the block and item `candy_corn_dispenser` (`facing`, `fill` 0 to 4, model-only `runner` 0 to 2; block entity `candy_corn_dispenser`, saving `count`);
  - the blocks and items `spider_cupcake_tower` (`facing`, `on_table`, `servings` 0 to 13) and `ghost_meringue_platter` (`facing`, `on_table`, `servings` 0 to 9);
  - the item `ghost_meringue`;
  - the blocks and items `mummy_sausage_tray` (`servings` 0 to 8), `witch_finger_cauldron` (`servings` 0 to 10) and `eyeball_jelly_mould` (`servings` 0 to 8), each with `facing` and `on_table`;
  - the items `mummy_sausage`, `witch_finger` and `eyeball_jelly`;
  - the block and item `pumpkin_layer_cake` (a cake's `facing` and `bites`), the items `raw_pumpkin_layer_cake` and `pumpkin_layer_cake_slice`, and `PieFilling.PUMPKIN_LAYER_CAKE`, saved by the Hearth Oven by its place, last in the list.
- Their recipes follow the agriculture switch. The blocks and items stay registered when it is off.

## Dependencies and assets
- No new dependency.
- Models, blockstates, loot tables and textures are generated by `tools/party_table_data.py` from `tools/party_table.py`, through `tools/generate_material_data.py` and `tools/generate_textures.py`.
- Nothing is copied, traced or recoloured from Minecraft's textures.

## Verification
Local checks run on this branch, and their results:
- Part 1:
  - `python3 tools/check_mod_data.py`: PASS on the second run (8,146 material IDs), including the art check of every block and item model.
    - The first run failed: the dispenser's texture wasn't written, because `tools/party_table_data.py` wasn't yet hooked into `tools/crop_textures.py`. It is now.
    - Its new `check_party_table` checks the dispenser's Java against `tools/party_table.py`.
    - It checks its registration with its block entity, which saves what it holds and drops it when broken.
    - It checks that its renderer turns the crank by `crank` and swings the flap by `flap`.
    - It checks that its blockstate shows it at runner 0, its candy by `fill` there, and its crank and flap alone at runners 1 and 2.
    - It checks that its candy stands higher at each level, within the globe, that its globe is see-through, and its name.
  - `python3 scripts/check_repository.py`: PASS.
  - `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
  - `python3 tools/check_icon_maps.py`: PASS (no new map; its item is 3D).
  - `javac` scan of the new and changed Java for syntax errors: none. Every member the new Java calls is used elsewhere in the code already, or declared in it. The full compile runs in CI.
  - The model was previewed offline (a review aid, not committed); what the previews showed and what was fixed is under How it is drawn.
- Part 2:
  - `python3 tools/check_mod_data.py`: PASS on the second run (8,149 material IDs), including the art check of every block and item model.
    - The first run failed: the Ghost Meringue's item texture wasn't drawn, because `tools/party_table_data.py textures()` didn't yet draw its icon map. It does now.
    - `check_party_table` now also checks the stands' capacities and foods against `tools/party_table.py`, that each has a place for every serving, and their registration.
    - It checks that their blockstates add each serving while they hold more than that many, on a Feast Table and off it, at every facing.
    - It checks that each `_table` model is the table's drop lower, and that the drop brings a piece down onto the Feast Table's top.
    - It checks that their loot drops what they hold, and that the Ghost Meringue is the table's food and is candy.
  - `python3 scripts/check_repository.py`: PASS.
  - `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
  - `python3 tools/check_icon_maps.py`: PASS (278 maps, the new `ghost_meringue` among them). Its first draft had two outline errors at its foot, fixed.
  - `javac` scan of the new and changed Java for syntax errors: none. Every member the new Java calls is used elsewhere in the code already, or declared in it. The full compile runs in CI.
  - The models were previewed offline; what the previews showed and what was fixed is under How it is drawn.
  - **CI's first compile of part 2 failed**: with a full inventory, `PartyServingBlock` called `player.drop(stack, false)`, which in 26.3 takes a third argument. The member check matches names, not argument counts, so it missed this. The stand now pops the serving out with `Block.popResource`, as the Barmbrack does.
- Part 3, all on the first run:
  - `python3 tools/check_mod_data.py`: PASS (8,155 material IDs), including the art check of every block and item model. `check_party_table` now also checks:
    - the tray's, the cauldron's and the mould's capacities and foods, a place for every serving, their blockstates on and off a table, their `_table` models and their loot, as for the cake stands;
    - that every table piece holding a food is checked;
    - that the three foods are registered as `tools/party_table.py` says, the sausage as a meal that doesn't compost.
  - `python3 scripts/check_repository.py`: PASS.
  - `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
  - `python3 tools/check_icon_maps.py`: PASS (281 maps, the three new ones among them).
  - `javac` scan of the new and changed Java for syntax errors: none. Every member the new Java calls is used elsewhere in the code already, or declared in it, with the same arguments where the call is new. The full compile runs in CI.
  - The models and icons were previewed offline; what the previews showed and what was fixed is under How it is drawn.
- Part 4:
  - `python3 tools/check_mod_data.py`: PASS on the second run (8,158 material IDs), including the art check of every block and item model.
    - The first run failed before its checks: `tools/generate_textures.py` stopped with an `IndexError`, because the cake's new `SPONGE` palette in `tools/cake_art.py` replaced the owner's `SPONGE` of the same name. The cake's palettes are now named `PLC_`.
    - `check_pies_and_tarts` now leaves the painted cake out of the check that the owner's cakes are last on their drawing.
  - `python3 scripts/check_repository.py`: PASS.
  - `python3 -m unittest discover -s scripts/tests`: 287 tests, OK on the second run.
    - The first run failed one: `test_tree_batch_five_bc` checked that the Persimmon Pie was the very last filling. It now checks that the fillings before it are unchanged and it comes after them, so fillings added later don't break it.
  - `python3 tools/check_icon_maps.py`: PASS (281 maps; the cake's icons are drawn by `tools/cake_art.py`, not from maps).
  - After the slice icon's fix, textures generated again and `check_mod_data.py`, `check_repository.py` and `check_icon_maps.py` run on them: all PASS.
  - `javac` scan of the changed Java for syntax errors: none. Every member it calls is used elsewhere in the code already, with the same arguments. The full compile runs in CI.
  - The cake was previewed offline; what the previews showed and what was fixed is under How it is drawn.

`PartyTableGameTests` (run in CI):
- **The Candy Corn Dispenser fills and turns**:
  - it stands facing the player, empty, with its block entity;
  - an empty one only rattles;
  - candy corn pours in, its globe filling, as much as fits up to 64, and full it takes no more;
  - an empty hand sets its crank turning, and a second use while it turns does nothing;
  - as the crank comes round, one piece drops out of its chute;
  - broken, it drops itself and all it held.
- **The dispenser's crank turns once round**: half way at half time, easing in, then resting; its flap stays shut while the crank turns, then swings out 55 degrees and back; its globe's level follows its count; its chute is at its front, at either facing.
- **The cake stands serve their food** (parts 2 and 3), for each of the tower, the platter, the sausage tray, the witch-finger cauldron and the jelly mould:
  - it stands facing the player, empty and off any table;
  - anything but its food changes nothing;
  - its food goes on one at a time, up to its capacity, the rest refused;
  - an empty hand takes one off, into the player's inventory;
  - broken, it drops itself and what it held.
  - The Ghost Meringue is candy, so a Candy Bag takes it.
- **The cake stands stand on a Feast Table**: a platter placed on one is set down onto its top, and stands back up when the table goes.
- **Their data loads**: the four foods' recipes, the six pieces' recipes and loot tables, and the Pumpkin Layer Cake is a cake, set down as a `CakeBlock`, with its raw cake's recipe (part 4).
- The cakes' own tests (`CakeGameTests`, `CakeClientGameTests`) serve, slice, bake and photograph it with the owner's seven; their count of cakes is now eight.

`PartyTableClientGameTests` takes three screenshots of a party corner on a dark oak floor. A Feast Table six blocks long runs along its back with a full sausage tray, witch-finger cauldron, cupcake tower, meringue platter and jelly mould set down on its top. Three dispensers stand before it, half full, full and empty, with a Pumpkin Layer Cake on a block beside them:
- close by, at the dispensers' height;
- the full one's crank half way round, and the half-full one's flap swung out as its piece drops (drawn by `CandyCornDispenserRenderer`);
- from above the dispensers, looking down on the stands and trays on the table.

Not run: a hand playtest, and a two-client playtest.

## World and event applicability
Seasonal in look, not in use: it is made and works all year, and nothing about it ends with October. It doesn't generate in the world.

## Rollout and open questions
- Part 1 is the candy, part 2 the cake stands, part 3 the trays and part 4 the cake; part 5 follows, stacked on the last.
- Open: whether the dispenser should take other small candy, or a coin to turn; whether the stands should be set down onto other tables than the Feast Table (each lower top would need its own copies of the models); and whether the cakes, which stand a block high on any block, should be set down onto a Feast Table as the stands are (that would change all eight cakes' block).
