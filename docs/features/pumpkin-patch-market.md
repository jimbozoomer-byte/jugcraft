# The Pumpkin Patch Market

Status: parts 1 (six squashes and gourds), 2 (the market's displays) and 3 (the Chalkboard Sign, Pumpkin Vine Cover and the Giant Warty Gourd) implemented; the set is complete. CI builds them and runs their game tests and client screenshots; not yet played by hand.
Proposal issue: none. The owner asked for it directly on 28 October 2026. They asked for big sets of Halloween and fall decor ("Pumpkins, Harvests, Halloween stuff") drawn by their rules, "not reverting back to the basic way". Then: "Then I want you to do 1-10 in parts in that order one by one". This is set 1 of the ten.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery tier. Seeds come from short grass and from wild patches; growing needs only farmland, and the recipes need nothing beyond the gourds themselves, sugar and an egg.
Primary specialty and supported player role: farming, with decor for builders. A grower fills a fall display or a market stall with fruit nobody else in the world has; a cook gets a pie pumpkin.

## Player experience

### Part 1: six new squashes and gourds
Each grows from its seeds on a stem, exactly as the existing squashes and gourds do. A stem on farmland grows through eight stages, then puts its fruit on a free block of soil beside it and bends towards it. Breaking the fruit lets the stem grow another. Each fruit is smaller than a whole block, faces the way it grew (or the player who placed it), and lets light and the faces of the blocks round it show past it.

- **Turban Squash**: a squat red-orange squash with ribs and green streaks running down from its shoulder. It wears a "turban" on top: cream with broad green stripes, banded red-orange round its foot, crowned with the dried blossom. Grows in plains and forests.
- **Blue Hubbard Squash**: a big slate-blue squash lying on its side, tapered at both ends, its back humped. It is set with pale raised warts, sparse as a real hubbard's, and has a stalk out of one end. Grows in taiga and snowy biomes. Its stem grows a quarter slower than the others, as befits a big squash.
- **Sweet Dumpling Squash**: a small cream squash, deeply ribbed, each groove a dark green stripe. Grows among flowers and birches. Its stem grows faster (0.85 of wheat's time a stage).
- **Long Island Cheese Pumpkin**: a flat, buff tan wheel of a pumpkin, ribbed like a cheese, with a thick corky stalk. The old American pie pumpkin, it bakes a **pumpkin pie** with sugar and an egg, as a pumpkin does. Grows in plains and on beaches.
- **Spoon Gourd**: a round yellow bowl with a green underside, on a straight green handle, lying on its side. Grows in savanna and jungle; its stem grows faster.
- **Swan Gourd**: a speckled dark green bulb whose long neck arches up and over and bows its head, the stalk at the tip of its "beak". Grows in swamps and jungles.

**Where the seeds come from**:
- **Short grass**: the six share the grass-seed chance with every other Jugcraft seed, so every one is reachable in any biome and in a world made before this.
- **Wild patches**: in the biomes above, and in the landscape collection's biomes of the same kinds.
- **The fruit itself**: four seeds from one fruit in a crafting grid.

**What they are for**:
- Fall displays, on their own or set in a **Pumpkin Crate** (which takes anything tagged `c:crops/squash`, `c:crops/pumpkin` or `c:crops/gourd`, as these are) or sold from a **Farm Stand**.
- **The Crowfield's harvest**: the harvest round the scarecrow that the Harvest Crown rite counts takes them (`#jugcraft:crowfield_harvest`).
- The three squashes are vegetables (`c:foods/vegetable`) for any recipe that takes one.
- The Long Island Cheese makes pumpkin pie.
- All six compost (medium), and their seeds feed chickens and parrots.

### Part 2: the market's displays
Three real-life-sized displays to set your produce out on. Each is several blocks placed and broken as one, facing whoever places it.
- **Pumpkin Wheelbarrow** (one block across, two long): a wooden wheelbarrow.
  - An iron-shod spoked wheel at the front on an axle between two long handle poles.
  - A flared plank tray with a rolled iron rim resting on the poles, two legs, and cord-wrapped grips at the back.
  - It holds **5** pieces: four in the tray and one heaped on top.
- **Pumpkin Wagon** (two across, two high, four long): a farm wagon.
  - Four iron-shod spoked wheels, the back pair taller, on two axles under bolsters joined by a reach pole.
  - A plank bed with barn-red slatted sides held by stakes, and a floor of loose hay.
  - It holds **28** pieces: eighteen on the hay in three files, then ten heaped between them.
- **Pumpkin Pyramid** (three blocks square): three stepped tiers of boards, each with a lip round its top edge, and corner posts at the bottom.
  - It holds **25** pieces: sixteen round the bottom step, eight round the middle, and one crowning the top.

**Using them:**
- Use any produce (anything the Pumpkin Crate takes: pumpkins, melons, and anything tagged `c:crops/pumpkin`, `c:crops/squash` or `c:crops/gourd`) on **any block** of a display to set one piece on its next free spot. When it is full, it says so and takes no more.
- Use it with an empty hand to take the last piece back.
- Each piece is drawn turned a little differently, so a full display doesn't look stamped out.
- Breaking any block takes the whole display down: it drops once and spills what it held.

**Recipes:**
- **Wheelbarrow:** five planks, two sticks and an iron ingot.
- **Wagon:** two sticks, a chest, two planks, red dye, two iron ingots and a hay bale.
- **Pyramid:** three wooden slabs and four planks.

### Part 3: the sign, the ground and the giant
- **Chalkboard Sign** (one block): an A-frame market board.
  - Two slate boards in wooden frames, hinged together at the top with two iron hinges, each leaning 22.5 degrees out from upright. Their side rails run down to the ground as legs.
  - The market's words are chalked on both boards: **PUMPKINS**, **FRESH PICKED**, **U-PICK**, **GOURDS**, **SQUASH**, **PIES**, **CIDER** and **OPEN**. Use it with an empty hand to chalk the next.
  - Or write your own, as on the Spooky Sign: use a Name Tag named in an anvil on it (the tag isn't used up), or rename the sign in an anvil before placing it. Your own words cover the market's; sneak-use with an empty hand wipes them off.
  - Chalking it needs build permission. Broken, it keeps its words (its own as its name).
  - Recipe: four sticks, planks and black dye.
- **Pumpkin Vine Cover**: a mat of pumpkin vine lying flat on the ground.
  - One vine across the block with tendrils curling off it, three broad lobed leaves on short stalks, and one open yellow flower.
  - Each is placed at one of four turns at random, so a spread of it doesn't tile.
  - It is walked through, not on, like leaf litter. It needs a solid top under it and comes off as an item when that goes.
  - Recipe: two vines and pumpkin seeds make six.
- **Giant Warty Gourd**: the Warty Gourd three times bigger.
  - Its body, neck and stalk are the Warty Gourd's own, on its own textures, nothing redesigned. It is two blocks wide and deep and three tall.
  - Like the displays, it is placed and broken as one, facing whoever places it, and drops once.
  - Recipe: nine Warty Gourds.

The bushel baskets first listed for this set were dropped: the woven Basket and the Pumpkin Crate already cover them.

## Changes from the idea list
- The "Warty Goblin" was dropped: the Warty Gourd already exists. The **Swan Gourd** takes its place.
- The Long Island Cheese is a lying squash shape, not a whole carvable block like the heirlooms. Carving and the heirloom tag (`#jugcraft:heirloom_pumpkins`) stay with the five carvable heirlooms. It gets its own pie recipe instead.
- The Giant Warty Pumpkin became the **Giant Warty Gourd**: the Warty Gourd itself three times bigger. The Giant Pumpkin given a warty skin would be a new design, not the same thing bigger (docs/ART_DIRECTION.md, "A bigger version is the same thing, bigger").
- The Chalkboard Sign chalks a set of market words and a player's own, rather than prices: a price would be a number nobody keeps true.
- The Pumpkin on a stump is left to set 5's chopping block, which a pumpkin or gourd can sit on; until then a pumpkin set on a log is the same thing.
- The pyramid is a stepped square pyramid three blocks across (ledges eight pixels wide, so a piece fits on each) rather than a narrow stack.

## How they are drawn (docs/ART_DIRECTION.md, docs/NATURAL_TEXTURES.md, docs/ITEM_ICONS.md)
- **Shape:** every fruit is built from boxes that give it its own silhouette (tools/pumpkin_patch.py SHAPES). The turban's knob and crown sit on a shoulder over a wide base. The hubbard's body tapers at each end under a hump. The spoon gourd's bowl sits on a handle. The swan gourd's neck rises, arches and bows its head. Every box is written low corner first, and no two faces of different boxes lie in one plane facing the same way where they overlap. The writers' coplanar pass and art check run over them as over every block model.
- **Textures:** 16×16, each fruit one colour stepped into six tones by lightness (`wood_style.ramp`, as the woods are), drawn by tools/pumpkin_patch_textures.py.
  - Most of a face is in the middle tones, laid as small clumps (`block_style.surface`), never a random shade at every texel.
  - Grooves run up and down every four texels, each with a lit ridge on its right, light from the top left. Four divides sixteen, so a skin carries on round its model's corners.
  - Tops have eight grooves running out to the edges from the stalk's scar.
  - The hubbard's warts and the swan gourd's specks sit on an even, staggered lattice, each wart lit at its top left with its shadow one tone under the ground: placed, not sprinkled.
  - No outline, no gradient, nothing pure black or white.
- **Seeds icons:** 16×16 maps in tools/item_icons/, drawn as vanilla's pumpkin and melon seeds are and told apart as they are, by the seeds' colour. Each is three outlined seeds lit from the top left, in an existing material of tools/icon_materials.py: stem, old bone, paper, canvas, straw and horn. tools/check_icon_maps.py passes them.
- Nothing is copied, traced or recoloured from Minecraft's textures. The owner's library has no squash or gourd art, so these are drawn fresh.
- **The displays** (tools/pumpkin_patch_data.py):
  - **Sizes:** real-life sized, each modelled whole in its frame and cut into its blocks' models. Each piece of a box cut at a block boundary keeps its share of the picture, so boards run on unbroken from block to block.
  - **Wheels:** an octagonal iron tyre of eight segments (four turned 45 degrees about the axle), eight spokes and a hub standing proud on both sides. Each kind of part is set in from the last across the wheel so none shares a plane with another. A wheel lies wholly in one block, as a turned box must.
  - **Hollows:** the tray and the wagon bed are walls round them with their insides drawn.
  - **Textures:** each side of every box is painted for that side at one texel to a model pixel, vanilla's density, so grain and boards run along each face's longer edge and nothing is squeezed.
    - Boards are a few flat tones, each lit along its top and seamed along its bottom, with a grain line one shade off every seven texels, staggered board by board.
    - Nails are two-texel heads lit at the top left; iron is flat bands; hay is straight stripes; the wagon's sides are barn-red slats with a dark gap.
    - No random shade at any texel.
  - **Atlas:** the pieces are packed into each display's 128 × 128 texture tallest first.
- **Part 3** (tools/pumpkin_patch_data.py):
  - **The Chalkboard Sign** is real-life sized: a board 12 pixels wide (75 cm) standing 15 high. Each board is a frame of boxes turned 22.5 degrees about the hinge, the slate recessed a tenth of a pixel inside it.
    - The slate is one flat dark green-black with two short eraser smears a tone lighter, placed.
    - The frame is the displays' wood, its grain along each rail; the hinges are flat iron.
    - The words are drawn by the client in chalk white, leaning with each board.
  - **The vine cover** is a 16×16 cut-out picture, one leaf green stepped into tones (`wood_style.ramp`), no outline.
    - Each leaf is lit up and to the left and shaded down and to the right in three even bands, a lighter vein from stalk to tip.
    - The cluster stands on its own at any of its four turns.
  - **The Giant Warty Gourd** is the Warty Gourd's model scaled three times (`decor17_data.grown`), each face keeping its picture, cut into its twelve blocks. Its item is the same gourd half as big.

## Connections
- Existing input producer: short grass (the grass-seed table) and world generation (wild patches); farmland and the existing stem growth (`GourdStemBlock`, `CropGrowth`, so beans beside a stem speed it as for every squash).
- Existing input producer (part 3): vines and pumpkin seeds (the vine cover), sticks, planks and black dye (the chalkboard), and the existing Warty Gourd (the giant).
- Existing output consumer: the Pumpkin Crate and Farm Stand displays (shared crop tags) and part 2's displays (the Pumpkin Crate's tag); the Crowfield harvest (`#jugcraft:crowfield_harvest`, counted by the Harvest Crown rite); pumpkin pie (Long Island Cheese); composters; chicken and parrot food (seeds).
- Technology connection: the Hydroponics machine grows each from its seeds (600 ticks a fruit, the seed returned), as it does every gourd (docs/features/hydroponics.md; its recipes are generated for every gourd seed).
- Magic connection: none.
- Reachable entry path: grass seeds from the first day in any biome; no unlock gates them.
- Which connections are required vs optional; trade and solo routes: all optional. Seeds can be traded between players like any item.
- How this specialty stays useful without mastering every other branch: they are decor and food on their own.
- For cosmetics: their purpose is variety for fall displays and the Crowfield; the crop tags tie them to the existing displays.

## Balance and automation
- Each fruit gives four seeds in a crafting grid (as every Jugcraft gourd), and a stem grows another fruit after one is taken. No conversion returns more than it costs.
- Growth time per stage, in wheat's time: Blue Hubbard 1.25; Turban and Long Island Cheese 1.0; Sweet Dumpling and Spoon Gourd 0.85; Swan Gourd 1.0.
- One pumpkin pie from one Long Island Cheese, sugar and an egg, the same as from a pumpkin or an heirloom.
- The displays only hold what is put on them, one piece a spot: they make, grow and change nothing, and give back exactly what went on.
- Part 3 is decor only. Nine Warty Gourds make one Giant Warty Gourd, which drops only itself. Two vines and pumpkin seeds make six vine covers, which drop only themselves. Nothing turns either back, so no loop gains.

## Multiplayer and persistence
- Part 1: plain blocks and items, server-side growth (the existing gourd stem code); no block entities.
- Part 2:
  - Each display's first block keeps what it holds (`ProduceDisplayBlockEntity`), saved with the world and sent to clients.
  - Pieces are set on and taken off on the server only, one a use, and only produce is taken.
  - Breaking it from any block takes the whole down and spills its pieces once.
  - Pistons can't move it (`PushReaction.IMMOVEABLE`; 26.3 has no `BLOCK`).
  - The client draws the pieces (`ProduceDisplayRenderer`), each lit as the block it stands in.
- Part 3:
  - The Chalkboard Sign's own words are kept as a Spooky Sign's are (`ChalkboardSignBlockEntity` extends `SpookySignBlockEntity`): plain text, cut to the gravestone's 50 characters with control characters dropped, saved with the world and sent to clients.
  - Words are changed on the server only, and only by a player who may build there.
  - The vine cover and the giant gourd keep nothing; the giant gourd can't be moved by pistons (`PushReaction.IMMOVEABLE`); a piston pops the vine cover off (`POPPED`).
- IDs (stable from release): part 3's `chalkboard_sign` (block entity `chalkboard_sign`), `pumpkin_vine_cover`, `giant_warty_gourd`; part 2's `pumpkin_wheelbarrow`, `pumpkin_wagon`, `pumpkin_pyramid` (block entity `produce_display`); part 1's `turban_squash`, `blue_hubbard_squash`, `sweet_dumpling_squash`, `long_island_cheese_pumpkin`, `spoon_gourd`, `swan_gourd`, each with `_seeds`, `_stem` and `attached_<id>_stem`.
- Every recipe and patch follows the agriculture feature switch, as the other gourds do. Turning agriculture off stops the recipes; the blocks stay registered.

## Dependencies and assets
- No new dependency. Models and textures are generated by tools/pumpkin_patch.py, tools/pumpkin_patch_textures.py, tools/pumpkin_patch_data.py and tools/festival_data.py; icons by tools/item_icons/*.txt.

## Verification
Local checks run on this branch, and their results:
- `python3 tools/check_icon_maps.py` on the six seeds maps: PASS (6 maps, 0 warnings).
- `python3 tools/check_mod_data.py`: PASS (part 1: 8,049 material IDs; with part 2: 8,052; with part 3: 8,055), including the art check over every block and item model (the six gourd models, the displays' 27 part models and 3 item models, and part 3's 14 block and 3 item models among them; no allow-list entry added) and `check_pumpkin_patch`.
- `python3 scripts/check_repository.py`: PASS.
- `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
- `javac` parse (syntax only) of the changed and new Java: OK. The full compile runs in CI.
- CI on `68c39b6` (part 2's first push): the build failed to compile, since 26.3 has no `PushReaction.BLOCK` (the displays now use `IMMOVEABLE`), so no game test or screenshot ran on it.
- Offline previews of the six gourd models, the three displays, the Chalkboard Sign, the vine cover (tiled at random turns) and the Giant Warty Gourd (its twelve part models put back together), textured and drawn with back faces culled, were looked at from front and back while drawing (a review aid, not committed).

Game tests (CI only, not run locally):
- `PumpkinPatchGameTests`:
  - each seed plants its stem;
  - each grown stem puts its own fruit beside it, facing away, and bends towards it;
  - each fruit drops itself, is smaller than a block and counts in the Crowfield's harvest;
  - each wild patch is placed.
- `AgricultureGameTests` grass seeds: the six are in the list of every grass seed, each of which must drop from 4,000 broken grass.
- `PumpkinPatchGameTests`, part 2 (one test per display):
  - it stands whole, facing its maker;
  - it takes produce through its farthest block, a piece a spot, up to its spots (5, 28, 25), and then no more;
  - it refuses a stick;
  - it gives the last piece back to an empty hand;
  - it remembers what it holds through a save;
  - it breaks whole, dropping once and spilling the rest.
- `PumpkinPatchGameTests`, part 3:
  - the Chalkboard Sign faces its maker saying PUMPKINS and chalks the next words in turn;
  - a player in adventure mode changes nothing;
  - a named Name Tag chalks its name and is kept, and an unnamed one chalks nothing;
  - with its own words, use changes nothing, and sneak-use wipes them;
  - broken, it drops once with its words and its own as its name;
  - the vine cover lies a pixel high with no collision, won't go where nothing holds it up, and comes off as an item when its ground goes;
  - the Giant Warty Gourd stands whole (12 blocks, each with its piece of the shape), facing its maker; use does nothing to it; broken at its top, it all goes and drops once.
- `PumpkinPatchClientGameTests`: seven screenshots:
  - the six in a row on boards, the row from behind, and the patch;
  - the three displays filled, from the front;
  - the wagon from the side;
  - part 3 set out in front of the displays;
  - the Chalkboard Signs close.
- `check_pumpkin_patch` in tools/check_mod_data.py: each display's Java sizes, spots and scale and the renderer's turns are the table's. A wagon spot changed by hand was caught. Part 3: the Chalkboard Sign's words (in order, with their text) and the renderer's boards, and the Giant Warty Gourd's size, are the table's.

Not run: a hand playtest, and a two-client playtest (none is needed beyond the existing gourd behaviour, which these reuse unchanged).

## World and event applicability
Wild patches in the overworld biomes listed above (and the landscape collection's of the same kinds), placed as the other gourds' are: one patch in about 32 matching chunks. They are not seasonal: they grow and are found all year, as the other gourds are.

## Rollout and open questions
- Open: whether the Halloween Peddler should sell the new seeds as it does the heirlooms'. It doesn't yet; grass and wild patches are enough to reach them.
