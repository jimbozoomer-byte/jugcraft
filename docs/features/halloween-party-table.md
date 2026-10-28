# Halloween Party Table

Status: parts 1 (the candy: the Candy Corn Dispenser) and 2 (the cake stands: the Spider Cupcake Tower and the Ghost Meringue Platter, and the Ghost Meringue) implemented. CI builds them and runs their game tests and client screenshots; not yet played by hand. Parts 3 to 5 are planned (below).
Proposal issue: none. The owner asked for it directly on 28 October 2026, as set 8 of the ten sets of fall and Halloween decor ("Then I want you to do 1-10 in parts in that order one by one"), drawn by their rules.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery tier; the dispenser is made from glass, iron and black dye, and filled with candy corn; the cupcake tower from iron nuggets and black dye, the meringue platter from clay and black dye, and Ghost Meringues from an egg, sugar and cocoa.
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

### Planned
- **Part 3: the trays**: a mummy sausage tray, witch-finger cookies and an eyeball jelly mould.
- **Part 4: the cake**: a pumpkin layer cake that slices, as vanilla's cake does.
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
  - the item `ghost_meringue`.
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

`PartyTableGameTests` (run in CI):
- **The Candy Corn Dispenser fills and turns**:
  - it stands facing the player, empty, with its block entity;
  - an empty one only rattles;
  - candy corn pours in, its globe filling, as much as fits up to 64, and full it takes no more;
  - an empty hand sets its crank turning, and a second use while it turns does nothing;
  - as the crank comes round, one piece drops out of its chute;
  - broken, it drops itself and all it held.
- **The dispenser's crank turns once round**: half way at half time, easing in, then resting; its flap stays shut while the crank turns, then swings out 55 degrees and back; its globe's level follows its count; its chute is at its front, at either facing.
- **The cake stands serve their food** (part 2), for each of the tower and the platter:
  - it stands facing the player, empty and off any table;
  - anything but its food changes nothing;
  - its food goes on one at a time, up to its capacity, the rest refused;
  - an empty hand takes one off, into the player's inventory;
  - broken, it drops itself and what it held.
  - The Ghost Meringue is candy, so a Candy Bag takes it.
- **The cake stands stand on a Feast Table**: a platter placed on one is set down onto its top, and stands back up when the table goes.
- **Their data loads**: the Ghost Meringue's recipe, and the three pieces' recipes and loot tables.

`PartyTableClientGameTests` takes three screenshots of a party corner on a dark oak floor. A Feast Table runs along its back with a full cupcake tower and a full meringue platter set down on its top, and three dispensers stand before it, half full, full and empty:
- close by, at the dispensers' height;
- the full one's crank half way round, and the half-full one's flap swung out as its piece drops (drawn by `CandyCornDispenserRenderer`);
- from above the dispensers, looking down on the cake stands on the table.

Not run: a hand playtest, and a two-client playtest.

## World and event applicability
Seasonal in look, not in use: it is made and works all year, and nothing about it ends with October. It doesn't generate in the world.

## Rollout and open questions
- Part 1 is the candy and part 2 the cake stands; parts 3 to 5 follow, each stacked on the last.
- Open: whether the dispenser should take other small candy, or a coin to turn; whether the stands should be set down onto other tables than the Feast Table (each lower top would need its own copies of the models).
