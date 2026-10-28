# The Pumpkin Patch Market

Status: part 1 implemented (six squashes and gourds). CI builds it and runs its game tests and client screenshots; not yet played by hand. Part 2 (the market's displays) is planned below.
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

### Part 2 (planned): the market's displays
- The **Pumpkin Wagon**: a farm wagon several blocks long on spoked wheels, its bed heaped with your own pumpkins and gourds.
- The **Pumpkin Pyramid**: a stepped display of boards, a block or more across, each step set with the fruit you put on it.
- The **Wheelbarrow** of pumpkins.
- The **Chalkboard Sign**: an A-frame you write the prices on.
- **Pumpkin-vine ground cover**.

The wagon, pyramid and wheelbarrow show what you put in them, as the Pumpkin Crate does, and take the same fruit. The bushel baskets first listed for this set were dropped: the woven Basket and the Pumpkin Crate already cover them.

## Changes from the idea list
- The "Warty Goblin" was dropped: the Warty Gourd already exists. The **Swan Gourd** takes its place.
- The Long Island Cheese is a lying squash shape, not a whole carvable block like the heirlooms. Carving and the heirloom tag (`#jugcraft:heirloom_pumpkins`) stay with the five carvable heirlooms. It gets its own pie recipe instead.
- The Giant Warty Pumpkin waits for part 2, so it can be the existing Giant Pumpkin scaled with the warty skin and nothing else (docs/ART_DIRECTION.md, "A bigger version is the same thing, bigger").

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

## Connections
- Existing input producer: short grass (the grass-seed table) and world generation (wild patches); farmland and the existing stem growth (`GourdStemBlock`, `CropGrowth`, so beans beside a stem speed it as for every squash).
- Existing output consumer: the Pumpkin Crate and Farm Stand displays (shared crop tags); the Crowfield harvest (`#jugcraft:crowfield_harvest`, counted by the Harvest Crown rite); pumpkin pie (Long Island Cheese); composters; chicken and parrot food (seeds).
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

## Multiplayer and persistence
- Plain blocks and items, server-side growth (the existing gourd stem code). No block entities, no saved data beyond the block states.
- IDs (stable from release): `turban_squash`, `blue_hubbard_squash`, `sweet_dumpling_squash`, `long_island_cheese_pumpkin`, `spoon_gourd`, `swan_gourd`. Each has `_seeds`, `_stem` and `attached_<id>_stem`.
- Every recipe and patch follows the agriculture feature switch, as the other gourds do. Turning agriculture off stops the recipes; the blocks stay registered.

## Dependencies and assets
- No new dependency. Models and textures are generated by tools/pumpkin_patch.py, tools/pumpkin_patch_textures.py and tools/festival_data.py; icons by tools/item_icons/*.txt.

## Verification
Local checks run on this branch, and their results:
- `python3 tools/check_icon_maps.py` on the six seeds maps: PASS (6 maps, 0 warnings).
- `python3 tools/check_mod_data.py`: PASS (8,049 material IDs, data files and recipe audit), including the art check over every block and item model (the six new models among them; no allow-list entry added).
- `python3 scripts/check_repository.py`: PASS.
- `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
- `javac` parse (syntax only) of the changed and new Java: OK. The full compile runs in CI.
- An offline preview of the six models, textured and drawn with back faces culled, was looked at from front and back while drawing (a review aid, not committed).

Game tests (CI only, not run locally):
- `PumpkinPatchGameTests`:
  - each seed plants its stem;
  - each grown stem puts its own fruit beside it, facing away, and bends towards it;
  - each fruit drops itself, is smaller than a block and counts in the Crowfield's harvest;
  - each wild patch is placed.
- `AgricultureGameTests` grass seeds: the six are in the list of every grass seed, each of which must drop from 4,000 broken grass.
- `PumpkinPatchClientGameTests`: three screenshots: the six in a row on boards, the row from behind, and the patch.

Not run: a hand playtest, and a two-client playtest (none is needed beyond the existing gourd behaviour, which these reuse unchanged).

## World and event applicability
Wild patches in the overworld biomes listed above (and the landscape collection's of the same kinds), placed as the other gourds' are: one patch in about 32 matching chunks. They are not seasonal: they grow and are found all year, as the other gourds are.

## Rollout and open questions
- Part 2 (the displays) follows on its own branch.
- Open: whether the Halloween Peddler should sell the new seeds as it does the heirlooms'. It doesn't yet; grass and wild patches are enough to reach them.
