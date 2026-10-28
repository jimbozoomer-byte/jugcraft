# Halloween Party Table

Status: part 1 (the candy: the Candy Corn Dispenser) implemented. CI builds it and runs its game tests and client screenshots; not yet played by hand. Parts 2 to 5 are planned (below).
Proposal issue: none. The owner asked for it directly on 28 October 2026, as set 8 of the ten sets of fall and Halloween decor ("Then I want you to do 1-10 in parts in that order one by one"), drawn by their rules.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery tier; the dispenser is made from glass, iron and black dye, and filled with candy corn.
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

### Planned
- **Part 2: the cake stands**: a spider cupcake tower and a ghost meringue platter.
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
- **The first previews**, fixed before commit:
  - the glass read as solid in the offline preview, which drew every part-clear texel opaque; the preview now blends glass over what is behind it, as the game's translucent pass does;
  - the candy's dark gaps between kernels speckled it like noise; they are gone;
  - with every kernel upright and in line, and each ring's texture starting at a white row, the candy read as flat horizontal bands; each column of kernels is now a texel lower than the last, counted from the candy's surface.

## Connections
- Existing input producer: candy corn (agriculture's own candy), glass, iron and black dye.
- Existing system it joins: none; it holds and hands out candy corn.
- Existing output consumer: whoever eats the candy corn, or puts it in a Candy Bag or a candy bowl.
- Technology/magic connections (or infrastructure/cosmetic applicability): cosmetic.
- Reachable entry path: from the first days, with a little iron and sand.
- Which connections are required vs optional; trade and solo routes: all optional, crafted solo, and tradeable like any item.
- How this specialty stays useful without mastering every other branch: it is a party's decoration for anyone's build.

## Balance and automation
- The dispenser costs five glass, two iron ingots and black dye, and gives back only itself.
- Candy corn goes in and comes out one for one. Nothing is made, and nothing is lost: broken, it drops all it held. No loop.
- It hands out one piece a second at most, by hand.

## Multiplayer and persistence
- **What it holds is the server's**: its block entity (`candy_corn_dispenser`) keeps how many pieces it holds, up to 64, saves them with the world and drops them when it is broken. Its block's `fill` (0 to 4) shows every client how full it is; the count itself isn't sent.
- **Its crank is the server's**: an empty hand sets it turning and schedules the piece's drop 20 ticks later. A block event marks the time on every side, so every client draws the crank turning and the flap swinging; that mark isn't saved. While it turns, another use does nothing.
- Only candy corn goes in, by a player's use in reach, and only as much as fits.
- It can't be pushed by a piston.
- IDs (stable from release): the block and item `candy_corn_dispenser` (`facing`, `fill` 0 to 4, model-only `runner` 0 to 2; block entity `candy_corn_dispenser`, saving `count`).
- Its recipe follows the agriculture switch. The block and item stay registered when it is off.

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

`PartyTableGameTests` (run in CI):
- **The Candy Corn Dispenser fills and turns**:
  - it stands facing the player, empty, with its block entity;
  - an empty one only rattles;
  - candy corn pours in, its globe filling, as much as fits up to 64, and full it takes no more;
  - an empty hand sets its crank turning, and a second use while it turns does nothing;
  - as the crank comes round, one piece drops out of its chute;
  - broken, it drops itself and all it held.
- **The dispenser's crank turns once round**: half way at half time, easing in, then resting; its flap stays shut while the crank turns, then swings out 55 degrees and back; its globe's level follows its count; its chute is at its front, at either facing.
- **Its data loads**: its recipe and loot table.

`PartyTableClientGameTests` takes two screenshots of a party corner on a dark oak floor, a Feast Table along its back and three dispensers before it, half full, full and empty:
- close by, at their height;
- the full one's crank half way round, and the half-full one's flap swung out as its piece drops (drawn by `CandyCornDispenserRenderer`).

Not run: a hand playtest, and a two-client playtest.

## World and event applicability
Seasonal in look, not in use: it is made and works all year, and nothing about it ends with October. It doesn't generate in the world.

## Rollout and open questions
- Part 1 is the candy; parts 2 to 5 follow, each stacked on the last.
- Open: whether the dispenser should take other small candy, or a coin to turn; whether the party pieces of later parts should sit lower on a Feast Table, whose top is at 13 pixels, so they stand on it rather than above it.
