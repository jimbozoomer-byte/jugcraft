# Halloween harvest: giant pumpkins, weigh-offs, scarecrows and fall decorations

Status: implemented in source, not yet played. The Build workflow compiles it, and CI's game tests are recorded below.
Proposal issue: none; requested directly by the owner on 1 October 2026 ("start the giant pumpkin next and the other fun halloween ideas we had left", about ten additions in one batch), after the Carving Knife.
Owner: @jimbozoomer-byte
Target milestone and tier: Milestone 3 (first homestead); Discovery tier (wood, iron, string, paper, leather, hay; no machines).
Primary specialty and supported player role: farming and decoration; supports builders (fall displays, light) and groups (weigh-offs between growers)

## Player experience
Eleven additions for a fall pumpkin patch. Nothing is seasonal: everything works all year and stays in the world.

1. **Giant pumpkins.** Giant Pumpkin Seeds plant a vine on farmland. It grows like a pumpkin stem, but 1.5× slower, then sets **one** small fruit on a free side and bends to it. While the vine holds it, the fruit grows on every random tick of its corner block: 1 point, +1 if the vine's farmland is moist, +1 if it was watered from a Gourd Canteen in the last day. Bone meal gives 4 points.
   - At 16 points it swells to **2×2×2**, at 48 to **3×3×3**, away from the vine and centred on it. It needs room (air, grass or flowers, never water) on ground fruit can lie on, or it waits.
   - Full grown, it weighs 100–120 kg and puts on 2 kg a point, up to **1000 kg**, until it is carved.
   - Breaking any block breaks the whole pumpkin once: one pumpkin per block of a seedling (1, 4 or 9) and, full grown, 1–3 Giant Pumpkin Seeds. The vine then straightens and sets another. Pistons cannot move it.
2. **A 48×48 carving face.** A full-grown giant's every side carves as one face, three times as fine as a pumpkin's, with the Carving Knife and the same screen (smaller cells, bigger brushes, starter faces blown up). A torch inside lights every block of it: 4, plus 1 for every 27 holes and every 108 shaved pixels, up to 15.
3. **Scooping.** The first cut into any plain pumpkin scoops it out: besides its seeds, **1–2 Pumpkin Guts** and, one time in ten, a **Giant Pumpkin Seed**. Guts cook into **Pumpkin Soup** in the Cooking Pot (bowl, 2 guts, an onion) or compost.
4. **The Harvest Scale** (a clock over iron and planks). Giant pumpkins can't be moved, so the scale goes to the pumpkin: put it beside (or under) a full-grown giant and use it with an empty hand.
   - It weighs the pumpkin, keeps the **three heaviest** it has weighed on its board (under the name of whoever weighed them), and tells you the weight and the board.
   - The first time a pumpkin places on a scale's board, the player weighing it gets a **First, Second or Third Prize Ribbon**. Ribbons are trophies, nothing more.
   - A comparator reads the last weight (15 at 1000 kg).
5. **Pumpkin stencils.** Use a Blank Stencil (two paper) on a carved side of a hand-carved pumpkin to trace it into a **Pumpkin Stencil**. Hold a stencil in your other hand while carving and the screen offers it next to the starter faces, blown up on a giant.
6. **Heirloom pumpkins:** **White**, **Jarrahdale** (slate blue) and **Cinderella** (deep red-orange). They grow from stems like squash, give 4 seeds each, bake into vanilla pumpkin pie, and the Carving Knife carves them like pumpkins into their own hand-carved blocks.
7. **The Scarecrow** (wool, hay, sticks): a straw figure on a post, two blocks tall, in a flannel shirt. Use any dye on it to change the shirt (16 colours). Its head is whatever you put on top: a pumpkin, a hand-carved one, a jack o'lantern.
8. **Ornamental corn and corn shocks.** Ornamental (flint) corn grows like corn, 3 blocks tall, and is picked for its red, purple, blue-black and gold ears; three ears and string make an **Ornamental Corn Bundle** to hang on a wall or door frame. Breaking any corn plant 3 blocks tall gives **Corn Stalks**; six stalks and string make a **Corn Shock**, a two-block stook.
9. **Caramel treats.** Sugar melts into **Caramel** in a furnace, smoker or campfire. An apple, caramel and a stick make a **Caramel Apple** (6 hunger; you keep the stick); two popcorn and caramel make a **Popcorn Ball** (5).
10. **Gourd crafts.** The **Bottle Gourd** grows from a stem like squash; a furnace, smoker or campfire dries it. A dried gourd and string make a **Gourd Birdhouse**, which stands or hangs like a lantern; a dried gourd and leather make a **Gourd Canteen**, which holds 3 sips of water: fill it at any water source, then water a giant pumpkin, moisten farmland, put out fire or add a level to a cauldron.
11. **Mums** in yellow, orange, red and purple: flowers that go in flower pots, make dye and suspicious stew, and grow wild in patches in flower forests, meadows and forests.

## Connections
- Existing input producer: vanilla pumpkins (scooping, carving), Jugcraft corn and popcorn, onions (soup), the Cooking Pot, the Carving Knife; iron, wood, wool, hay, string, paper, leather, sugar and apples.
- Existing output consumer: Cooking Pot soup, vanilla pumpkin pie, dyes, composting, `c:` crop, seed and food tags; decoration and light for builders.
- Technology connection: none needed. Comparators read the Harvest Scale.
- Magic connection: none yet.
- Reachable entry path: every crop has a wild source and a grass-drop source (docs/branches/AGRICULTURE.md):
  - heirloom pumpkins and bottle gourds lie on grass in their biomes (new chunks), and short grass drops their seeds;
  - giant pumpkin seeds come out of any pumpkin carved for the first time (vanilla pumpkins grow wild), and from short grass;
  - ornamental corn kernels drop from Wild Corn now and then (one in ten, without shears), and from short grass;
  - mums grow wild in patches.
- Required vs optional: all optional; nothing in progression needs any of it.
- How this stays useful without other branches: food, decoration, light and a weigh-off on its own.

## Balance and automation
- A giant pumpkin is a long project, not a pumpkin farm: about 20–30 minutes of growth on moist, watered farmland to full size, then hours (or about 110 bone meal) to approach 1000 kg. It gives 9 pumpkins and 1–3 seeds, about what 9 pumpkins grown separately would.
- Carving stops a giant's growth, so a weigh-off pumpkin is weighed before it is carved.
- Scooping gives guts and a 10 % giant seed once per pumpkin: hand-carved pumpkins never turn back into plain ones, so there is no loop.
- Ribbons have no use or value; the scale remembers the last 64 pumpkins it gave ribbons to, so weighing a pumpkin again gives no second one.
- Recipe audit: no loop among the new recipes (`check_mod_data.py`). Corn stalks only come from breaking plants; caramel only from sugar.
- Food: Caramel 2, Caramel Apple 6, Popcorn Ball 5, Pumpkin Soup 8 (like the other soups).
- The canteen only moves water, which is free in the world already.
- No automation: everything is by hand. The giant pumpkin and its vine are farmland crops; nothing chunk-loads them.

## Multiplayer and persistence
- **Server authority.** Clients only draw. The giant's carving goes through the same checked carving session as a pumpkin's, now for a face of a known size: the client sends the size, then exactly that many ints; any other size is refused before anything is read. The server also checks that the session was opened for a face of that size, at the giant's corner block, with more reach (3.5 blocks more, as the corner can be up to three blocks from the side carved). The scale reads the weight from the pumpkin on the server; stencils are traced from the block on the server; the canteen changes blocks only where the player may build.
- **Saved state.**
  - The giant's corner block keeps its growth points, weight, watering, lit torch, a UUID (for the scale's board), its four 48×48 faces and who carved it (sent to clients without the carver).
  - The scale keeps its board, its ribbon list and the last weight.
  - Stencils keep their design in the `jugcraft:stencil` component; canteens their water in `jugcraft:canteen_water`.
  - The scarecrow's shirt and the giant's size, part and light are block states.
- **Bounded work.**
  - Only a giant's corner block ticks; a tick reads at most 3 blocks, a growth step at most 54.
  - Breaking a giant removes at most 27 blocks.
  - The scale reads 5 neighbours.
  - The client keeps one texture per distinct giant design, in the same 256-entry cache as carved pumpkins.
- New IDs only. Nothing existing changes, except that corn plants 3 blocks tall also drop stalks, Wild Corn sometimes drops ornamental kernels, short grass drops six more kinds of seed (the same 12.5 % chance in all), and the first cut into a pumpkin also scoops it. The `agriculture` switch turns off the new recipes, wild patches and grass drops; placed blocks and items stay.

## Dependencies and assets
No new dependencies. Every texture is drawn by code (`tools/halloween_textures.py`): giant pumpkin sides and tops as one big picture per size cut into 16×16 tiles, the heirloom skins, flannel in 16 dye colours, the scale, straw, stalks, flint corn, mums and every item icon. Models use vanilla's templates by reference where the shape is the same (cube column, cross, flower pot, pumpkin for a seedling giant).

## Verification
Actual results (1 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions):

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares the giant pumpkin, scale, canteen, mum, heirloom and scarecrow numbers with Java, checks every giant pumpkin and scarecrow state has a model, and fails on a recipe category its recipe type doesn't have) | Pass, 383 IDs |
| `./gradlew build`, compile, on `5472054` (with Pumpkin Carving, the Festival Crops, Kitchen Garden, Fall Harvest and `main` after #51 merged in) | Pass |
| Game tests on the headless server, same commit: 142 in total, 14 of them new here (`HalloweenGameTests`) | **All 142 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`), including carving a giant from a stencil through the screen | **Passes** on `5472054`; see below for the one model error it found |

The 14 new game tests (`HalloweenGameTests`):
1. Giant Pumpkin Seeds plant a vine on farmland, not on grass;
2. a grown vine sets one fruit and bends to it; ticks give 2 points on moist farmland and 3 when watered; the fruit swells to 2×2×2 and 3×3×3 away from the vine with its master at the lowest north-west corner, weighs 100–120 kg full grown, stays attached and turns further points into weight;
3. stone in the way on both sides stops it growing, room on one side lets it grow that way, and cut from its vine it stops growing;
4. breaking one block breaks all 27, drops 9 pumpkins and giant seeds once, and the vine straightens;
5. a 48×48 face carves a full-grown giant's side through its master block; a 16×16 face is refused; a torch lights every block to the carving's glow; carved, it stops putting on weight;
6. a white pumpkin carves into a hand-carved white pumpkin that keeps its face and drops itself; the first cut lets out 4 white pumpkin seeds and 1–2 pumpkin guts;
7. the Harvest Scale weighs the giant beside it, lists it at its weight, gives one First Prize Ribbon, drives a comparator, updates the entry when weighed again without a second ribbon, and weighs nothing with no pumpkin beside it;
8. a Blank Stencil does nothing on an uncarved side, traces a carved one into a Pumpkin Stencil (using one blank), and the carving screen is offered the stencil from the other hand;
9. a scarecrow placed from its item stands two blocks tall in the default shirt; blue dye (looked up by its ID) recolours both halves and is used up; breaking the top half with a pumpkin head on it drops one scarecrow;
10. a corn shock stands two tall; an ornamental corn bundle hangs on a wall and drops when the wall goes; a gourd birdhouse hangs under a beam;
11. an orange mum goes into a flower pot, which drops the pot and the mum; the four dye recipes load;
12. ornamental corn is a tall crop picked for its ears; ripe corn and ornamental corn drop stalks and their ears when broken;
13. caramel, caramel apples, popcorn balls and pumpkin soup restore 2, 6, 5 and 8; eating a caramel apple leaves its stick; all 18 new recipes load; pumpkin guts cook into soup in the Cooking Pot; the five new wild patches load;
14. the Gourd Canteen fills at a water source, moistens farmland, fills a cauldron a level and puts out fire, one sip each, does nothing when empty, and waters a giant pumpkin.

The client game test (`HalloweenClientGameTests`):
- builds a pumpkin patch: two full-grown giant pumpkins (one carved with the Classic face blown up, one beside a Harvest Scale), a 2×2×2 giant, a seedling and the vine's growth stages; four scarecrows in red, blue, green and purple shirts with carved heads; heirloom pumpkins and bottle gourds, hand-carved heirlooms on hay bales; ripe ornamental corn and corn shocks; a shed hung with corn bundles and gourd birdhouses, potted and wild mums. The log reads `[halloween test] giant pumpkin blocks: 63` (27 + 27 + 8 + 1);
- photographs it by day and, with the carved giant and the carved heads lit, at midnight;
- then opens the giant carving screen as the knife does, with a Pumpkin Stencil (the Spooky face) in the player's other hand, presses Apply (the stencil is offered and selected), turns the candle preview on, photographs the screen and presses Done. The server must then hold the stencil blown up to 48×48 on the giant's south side. The log reads `[halloween test] giant carved from a stencil over the network: true`.

Found by CI and fixed before this record:
- `PushReaction.BLOCK` and `DESTROY` are `IMMOVEABLE` and `POPPED` in 26.3 (found by compiling, `fc3d9fd`).
- 26.3 has no `Items.BLUE_DYE` field; the dye is looked up by its ID, as the scarecrow does (`76afe24`).
- The game test server refused to load the datapack: three crafting recipes used the category `food`, which only cooking recipes have (`8855eb4`). The checker now fails on any such category.
- The client log showed one model error: the hidden middle block of a 3×3×3 giant had a cube with no faces, which 26.3 refuses (`Expected between 1 and 6 unique faces, got 0`). It now has no element at all, and the checker fails on any model element without faces. Nothing showed, as that block is inside the pumpkin.

**Not run:**
- a person playing it in a client (the client test drives the real carving screen with simulated input);
- a dedicated server with two players, including a weigh-off between two growers;
- save and restart with growing and carved giants in the world;
- worldgen in fresh chunks (the placed features load; their placement was not looked at in a new world);
- performance with many carved giants in view.

## World and event applicability
Wild heirloom pumpkins, bottle gourds and mums appear in chunks generated after this feature. Nothing depends on a date or an event; a later Halloween event could hold weigh-offs and carving contests on top.
