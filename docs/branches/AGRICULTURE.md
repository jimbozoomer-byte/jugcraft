# Agriculture branch

Status: **three slices implemented in source: the Fall Harvest (crops, wild plants, sickles), the Kitchen Garden (trellises, seven crops, the Cooking Pot) and the Festival Crops (gourds on stems, turnips and Turnip Lanterns, cranberry bogs, the chestnut tree), plus pumpkin carving (the Carving Knife), the Halloween harvest (giant pumpkins, the Harvest Scale, scarecrows, heirloom pumpkins, ornamental corn and fall decorations), the pumpkin regatta and trick-or-treating, the Halloween festivities (the carving contest, costumed mobs, the Halloween Peddler, spooky decorations and sweets), and Halloween nights (will-o'-wisps, the Pumpkin Chunkin' Trebuchet, a bigger Candy Bag, the Harvest Moon and the Headless Horseman).** They compile in CI and have automated in-game tests, but nobody has played them yet. Everything marked *planned* below is a design proposal, not a promise. See [../features/fall-harvest.md](../features/fall-harvest.md), [../features/kitchen-garden.md](../features/kitchen-garden.md), [../features/festival-crops.md](../features/festival-crops.md), [../features/pumpkin-carving.md](../features/pumpkin-carving.md), [../features/halloween-harvest.md](../features/halloween-harvest.md) , [../features/pumpkin-regatta-and-trick-or-treat.md](../features/pumpkin-regatta-and-trick-or-treat.md) [../features/halloween-festivities.md](../features/halloween-festivities.md) and [../features/halloween-nights.md](../features/halloween-nights.md) for the implemented slices and their test evidence.

Agriculture is Jugcraft's third starting branch, alongside technology and magic ([DESIGN.md](../DESIGN.md)). A farmer can begin on day one with a hoe and whatever grows nearby, and never needs a machine or a spell. Industrial farming (powered harvesters, planters, sprinklers, crop processors) comes later and belongs to the engineering branch; see [Boundaries](#boundaries).

## What exists now: the Fall Harvest

| Crop | Grows | Plant with | Harvest | Uses now |
| --- | --- | --- | --- | --- |
| **Corn** | **3 blocks tall** on farmland | Corn Kernels | Right-click ripe corn to pick 2–3 ears; the stalk stays standing and regrows ears | Food (raw, roasted, popcorn), kernels, pig feed, Three Sisters Stew |
| **Sunflower** | **2 blocks tall**, a big yellow head when ripe | Sunflower Seeds | Right-click the ripe head for 2–4 seeds; the plant stays standing | Roasted seeds, chicken and parrot feed; oil later |
| **Beans** | 1 block, a bushy legume | Beans | Break when ripe (like potatoes) | **Speed up the crops around them**; Three Sisters Stew |
| **Sweet Potato** | 1 block of purple vines | Sweet Potato | Break when ripe | Food (raw, baked), pig feed |
| **Flax** | 1 block, blue flowers, then golden stems | Flax Seeds | Break when ripe (like wheat) | 2 flax → 1 string; chicken and parrot feed; linen and linseed oil later |

In the game: a corn maze and the Fall Harvest fields. These are real screenshots from the client game test that CI runs, using software rendering (small previews).

| A corn maze from above, beside a solid cornfield | At the maze entrance, in a wall of ripe corn |
| --- | --- |
| ![A corn maze from above](../images/ingame_corn_maze.jpg) | ![The maze entrance at eye level](../images/ingame_corn_maze_entrance.jpg) |
| **The farm:** corn, sunflowers, then rows of beans, sweet potatoes and flax | **A sunflower field** in bloom |
| ![The Fall Harvest farm](../images/ingame_fall_farm.jpg) | ![A sunflower field](../images/ingame_sunflowers.jpg) |

![Every growth stage in the game: flax in flower and ripe, beans, sweet potatoes, corn and sunflowers](../images/ingame_crop_stages.jpg)

The growth stages of the two tall crops, drawn from the generated textures (approximate renders, not game screenshots):

![Corn from sprout to ripe](../images/corn_stages.png)

![Sunflowers from sprout to bloom](../images/sunflower_stages.png)

### Cornfields and corn mazes

Corn is built for fields you want to look at and walk through.

- **Tall and dense.** Ripe corn is three blocks of leaves, ears and golden tassels. Planted side by side, the rows read as a solid wall of corn. Unlike vanilla crops, dense planting is not penalised.
- **It stays standing.** Harvesting is *picking*: right-click a ripe plant (any of its three blocks) and the ears drop, but the plant stays three blocks tall and grows new ears. A field or a maze never has to be torn down and regrown.
- **Walls you cannot walk through.** Once corn is two blocks tall it blocks movement like a hedge, and mobs path around it. Knee-high corn can still be walked through, like wheat. That is what makes a maze work: plant the walls, leave the paths as grass or dirt path, and wait.
- **No irrigation needed.** Corn keeps its farmland from drying back to dirt (the same rule as vanilla crops). Water nearby only makes it grow faster.
- **Easy to shape.** Corn only grows into air. A block above a plant stops it at that height, and breaking any block of a plant removes the whole plant and drops its kernel.

The maze in the screenshots above is 13 × 11 blocks with one-block paths, planted by the client game test in `AgricultureClientGameTests`.

### Getting your first seeds

Every crop has two independent entry points, so none is locked behind a biome, another branch or luck:

1. **Wild plants.** Patches of Wild Corn, Wild Sunflower, Wild Beans, Wild Sweet Potato and Wild Flax grow on grass where each crop comes from. Break one for 1–2 of its seeds; shears take the plant itself as decoration. They appear in chunks generated after this feature was added.

| Wild plant | Found in | Why there |
| --- | --- | --- |
| Wild Corn | plains, savanna | Corn's wild ancestor (teosinte) is a warm-grassland grass |
| Wild Sunflower | plains | Sunflowers are native to open prairies |
| Wild Beans | forest, jungle | Wild beans climb at forest edges |
| Wild Sweet Potato | savanna, jungle | A warm, tropical vine |
| Wild Flax | plains, flower-rich biomes | A meadow flower |

2. **Short grass.** Breaking short grass has a 12.5 % chance to drop one Jugcraft seed, as often as vanilla wheat seeds, chosen evenly from every Jugcraft crop (twenty-four so far). Adding crops never makes grass drop more seeds overall. This works in any biome and in worlds created before this feature.

### Systems

- **Tall crops ([`TallCropBlock`](../../src/main/java/io/github/jimbozoomer/jugcraft/agriculture/TallCropBlock.java)).** One block type per crop; a `section` property says which block of the plant it is, and every section shows its own slice of the same growth stage. Only the bottom block ticks and has loot, so a plant can never drop twice. Heights, pick yields and growth speed live in [`TallCrop`](../../src/main/java/io/github/jimbozoomer/jugcraft/agriculture/TallCrop.java) and [`tools/agriculture.py`](../../tools/agriculture.py); adding another tall crop is a table entry plus textures.
- **Picking.** Ripe tall crops go back to a younger stage *of the same height* when picked (corn and sunflowers go back to stage 5 of 7), so they regrow produce without shrinking.
- **Legumes feed their neighbours.** Crops next to beans (any of the 8 surrounding blocks) grow **1.5× as fast**. Beans themselves do not get the bonus, so the best field mixes crops. This is the classic *Three Sisters* planting (corn, beans and squash) that the stew is named after. The legume list is the block tag `jugcraft:nitrogen_fixing_crops`, so data packs and later crops can join it.
- **Growth.** Otherwise Jugcraft crops grow like vanilla crops: farmland under and around the plant, moisture, and light 9 or more. Corn takes 1.5× and sunflowers 1.25× as long as wheat per stage, because they are bigger plants that regrow after picking. Bone meal works: 1–2 stages on tall crops, as far as there is room above.
- **Sickles.** Right-click a crop with a sickle to harvest every *ripe* crop around it: 3×3 for the **Flint Sickle**, 5×5 for the **Bronze Sickle**, one block up or down. One-block crops (including vanilla wheat, carrots, potatoes and beetroots) are replanted automatically with a seed from their own drops; tall crops and cranberry bushes are picked and stay standing; squash and gourds still on their stems are cut off (gourds set down as decoration are left alone); unripe crops are left alone. Each use costs 1 durability. The sickle checks spawn protection and claims for every block it touches.
- **Composting and animal feed.** Every crop item composts at vanilla-like rates (seeds 30 %, produce 65 %, cooked food 85 %). Pigs eat corn, sweet potatoes, cabbage, turnips and chestnuts; rabbits eat cabbage and turnips; foxes eat cranberries; cows, sheep and goats eat oats and barley; horses eat oats; chickens and parrots eat every Jugcraft seed (vanilla animal-food tags, so breeding works).

### Food

| Food | Made from | Hunger | Saturation modifier | Vanilla comparison |
| --- | --- | --- | --- | --- |
| Corn (raw) | picked | 3 | 0.6 | Carrot |
| Roasted Corn | furnace, smoker or campfire | 5 | 0.6 | Baked potato |
| Popcorn | cook Corn Kernels | 2 | 0.3 | A snack |
| Sweet Potato (raw) | harvested | 2 | 0.3 | Slightly better than a raw potato |
| Baked Sweet Potato | furnace, smoker or campfire | 5 | 0.6 | Baked potato |
| Roasted Sunflower Seeds | cook Sunflower Seeds | 2 | 0.3 | A snack |
| Three Sisters Stew | bowl + corn + beans + pumpkin | 10 | 0.6 | Rabbit stew; returns the bowl |

1 Corn crafts into 2 Corn Kernels for replanting. Every cooked food uses vanilla times (furnace 200 ticks, smoker 100, campfire 600).

## What exists now: the Kitchen Garden

The second slice turns a farm into a kitchen: vegetables and grains for everyday meals, trellises for climbing crops, and a Cooking Pot for dishes with several ingredients.

| **Tomatoes on trellises**, ripe and ready to pick | **The kitchen garden:** trellis rows, oat and barley fields, and rows of every new crop |
| --- | --- |
| ![Rows of trellises with ripe red tomatoes](../images/ingame_tomato_trellis.jpg) | ![The kitchen garden from above](../images/ingame_kitchen_garden.jpg) |
| **Every growth stage:** tomatoes, peppers, cabbage, oats, barley, onions and garlic, youngest on the left, with the wild plants | **The Cooking Pot** on a campfire, cooking chili |
| ![The Kitchen Garden growth stages](../images/ingame_kitchen_stages.jpg) | ![A Cooking Pot on a campfire](../images/ingame_cooking_pot.jpg) |

![The Cooking Pot's screen: six ingredients, the progress bar, the heat flame and two bowls of chili](../images/ingame_cooking_pot_screen.jpg)

*Real screenshots from the client game test that CI runs (`KitchenGardenClientGameTests`, software rendering, small previews, hotbar cropped).*

| Crop | Grows | Plant with | Harvest | Uses now |
| --- | --- | --- | --- | --- |
| **Tomato** | **Climbs a trellis, 2 blocks tall**: yellow flowers, green fruit, then red | Tomato Seeds, on a trellis standing on farmland | Right-click the ripe plant to pick 2–4 tomatoes; it stays on the trellis and flowers again | Raw, tomato soup, chili, garden salad; 1 tomato → 2 seeds |
| **Pepper** | 1-block bush, white flowers, then green and red chilies | Pepper Seeds | Right-click to pick 1–3 peppers; the bush flowers again | Raw, chili, garden salad; 1 pepper → 2 seeds |
| **Onion** | 1 block of hollow leaves; the bulb shows when ripe | Onion | Break when ripe (like carrots) | Most soups, cabbage rolls |
| **Garlic** | 1 block; a curly flower stalk when ripe | Garlic | Break when ripe | Onion soup, cabbage rolls |
| **Cabbage** | 1 block; a pale head forms in a ring of leaves | Cabbage Seeds | Break when ripe (like wheat): a head and seeds | Raw, sauerkraut, salad, vegetable soup, cabbage rolls; rabbit and pig feed |
| **Oats** | 1 block of grass with nodding grain | Oat Seeds | Break when ripe | Porridge; horse, cow, sheep and goat feed |
| **Barley** | 1 block of grass with long whiskered heads | Barley Seeds | Break when ripe | Barley bread, mushroom barley soup; cow, sheep and goat feed |

### Trellises and climbing crops

- **The trellis** is a square wooden lattice (2 trellises from 5 sticks in an X). It stands on farmland, on any solid top or on another trellis, so you can stack them. It blocks movement like a fence and keeps the farmland under it from drying out.
- **Planting.** Use Tomato Seeds on a trellis that stands on farmland. The plant grows inside it.
- **Climbing.** A climbing crop only grows into **trellis**, never into air: put a second trellis on top and the tomato climbs to two blocks; without one it stays small. Later climbing crops (grapes, hops, pole beans) will use the same rule.
- **Nothing is lost.** Picking keeps the plant. Breaking any block of the plant gives back its seed, its tomatoes if ripe, and **every trellis it grew in**.

### The Cooking Pot

- **Crafting:** 5 iron ingots in a U with 2 sticks as handles.
- **Heat:** it cooks only while a heat source is directly under it: a lit campfire (or soul campfire), fire, lava or a magma block. The heat sources are the block tag `jugcraft:heat_sources`, so data packs and other branches can add their own. Without heat, progress drains away like a furnace going out.
- **Cooking:** put the ingredients in any of the six slots, in any order. Each batch makes one dish; stack the ingredients to cook batch after batch, into four result slots (soups do not stack). A slot holding something the recipe does not use stops the pot, so nothing is cooked by mistake.
- **Automation:** hoppers put ingredients in from the top and sides and take meals out from the bottom; a comparator reads how full the result slots are. No power is involved.
- **Recipes** are data-driven (`jugcraft:pot_cooking`, in the same format as Jugcraft's multi-input machine recipes), so data packs can add dishes.

| Dish | Ingredients | Time (ticks) |
| --- | --- | --- |
| Tomato Soup | bowl, 2 tomatoes, onion | 200 |
| Onion Soup | bowl, 2 onions, garlic, bread | 200 |
| Vegetable Soup | bowl, cabbage, carrot, potato, onion | 200 |
| Mushroom Barley Soup | bowl, barley, brown mushroom, onion | 200 |
| Oat Porridge | bowl, 2 oats, sugar | 200 |
| Chili | bowl, beans, tomato, pepper, onion, raw beef | 300 |
| Cabbage Rolls (2) | cabbage, raw beef, onion, garlic | 300 |

Made by hand: **Garden Salad** (bowl, cabbage, tomato, pepper), **Sauerkraut** (2 cabbage and 1 Jugcraft salt make 2; salt comes from rock salt in the mining branch) and **Barley Bread** (3 barley in a row, like bread).

### Kitchen Garden food

| Food | Made from | Hunger | Saturation modifier | Vanilla comparison |
| --- | --- | --- | --- | --- |
| Tomato (raw) | picked | 3 | 0.3 | Between an apple and a carrot |
| Pepper (raw) | picked | 2 | 0.3 | A snack |
| Cabbage (raw) | harvested | 3 | 0.6 | Carrot |
| Barley Bread | 3 barley | 5 | 0.6 | Bread |
| Sauerkraut | 2 cabbage + salt (makes 2) | 4 | 0.6 | Between a carrot and bread |
| Garden Salad | bowl, cabbage, tomato, pepper | 7 | 0.6 | Between mushroom stew and rabbit stew; returns the bowl |
| Oat Porridge | Cooking Pot | 6 | 0.6 | Mushroom stew |
| Tomato, Onion and Mushroom Barley Soup | Cooking Pot | 8 | 0.6 | Between mushroom stew and rabbit stew |
| Vegetable Soup | Cooking Pot | 10 | 0.6 | Rabbit stew (five ingredients) |
| Chili | Cooking Pot | 10 | 0.8 | Rabbit stew with a steak's saturation: the best dish so far, six ingredients including beef |
| Cabbage Rolls | Cooking Pot (makes 2) | 6 | 0.8 | A cooked meat dish, like cooked mutton |

Every bowl dish returns its bowl and stacks to 1, like vanilla stews.

### New seed sources

| Wild plant | Found in | Why there |
| --- | --- | --- |
| Wild Tomato | jungle, savanna | Tomatoes come from the warm Andes and Central America |
| Wild Pepper | savanna, badlands | Chilies are native to dry, hot scrubland |
| Wild Onion | plains, hills | A steppe and meadow plant |
| Wild Garlic | forest, taiga | Wild garlic carpets woodland floors |
| Wild Cabbage | windswept hills, hills | Wild cabbage grows on windy coastal cliffs |
| Wild Oats | plains, taiga | A grass of cool fields |
| Wild Barley | savanna, hills | Barley's wild ancestor grows on dry hillsides |

Short grass drops these seeds too (see [Getting your first seeds](#getting-your-first-seeds)).

## What exists now: the Festival Crops

The third slice fills the autumn and winter table and yard: squash and gourds for fall displays, turnips carved into the original jack-o'-lanterns, cranberries from a bog, and a chestnut tree to roast from. **Nothing here is seasonal.** Everything grows all year and stays in the world; the planned Halloween and December events ([CONTENT_BRANCHES.md](../CONTENT_BRANCHES.md#seasonal-content)) can build on these permanent crops, never the other way round.

| **The festival harvest:** a gourd patch, a cranberry bog, chestnut trees and a chestnut-wood market stall | **A gourd patch:** butternut squash, acorn squash and warty gourds on their stems, a few stems still growing |
| --- | --- |
| ![The festival crops from above](../images/ingame_festival_harvest.jpg) | ![Rows of gourd stems with their gourds](../images/ingame_gourd_patch.jpg) |
| **A cranberry bog:** bushes standing in water one block deep over mud, ripe and in flower | **Chestnut trees** with burs under their leaves, most of them ripe |
| ![A shallow bog full of cranberry bushes](../images/ingame_cranberry_bog.jpg) | ![Two chestnut trees](../images/ingame_chestnut_trees.jpg) |
| **Every growth stage:** turnips, a butternut stem and cranberries in a trench, youngest on the left, then the sapling and a wild turnip | **Turnip Lanterns** on chestnut fence posts at midnight, in front of the market stall |
| ![The Festival Crops growth stages](../images/ingame_festival_stages.jpg) | ![Turnip Lanterns at night](../images/ingame_turnip_lanterns.jpg) |

*Real screenshots from the client game test that CI runs (`FestivalClientGameTests`, software rendering, small previews, hotbar cropped).*

| Crop | Grows | Plant with | Harvest | Uses now |
| --- | --- | --- | --- | --- |
| **Butternut Squash** | A stem on farmland that grows a squash on the ground beside it, like a pumpkin | Butternut Squash Seeds | Break the squash (or sweep a sickle); the stem grows another | Squash soup, squash pie; a fall decoration; 1 squash → 4 seeds |
| **Acorn Squash** | The same, a dark green ribbed squash | Acorn Squash Seeds | The same | Baked acorn squash; decoration; 1 squash → 4 seeds |
| **Warty Gourd** | The same, a knobbly orange-and-green gourd | Warty Gourd Seeds | The same | Decoration for fall displays; composting; 1 gourd → 4 seeds |
| **Turnip** | 1 block of lobed leaves; the purple shoulder shows when ripe | Turnip | Break when ripe (like carrots) | Raw, harvest stew, the **Turnip Lantern**; pig and rabbit feed |
| **Cranberry** | A low bush **standing in water one block deep** over bog soil; white-pink flowers, then red berries | Cranberries, used on the bottom of the water | Right-click (or sickle) to pick 2–3; the bush flowers again | Raw, cranberry sauce; fox feed |
| **Chestnut tree** | A sapling that grows into a broad tree; its leaves grow burs that ripen | Chestnut, planted like a sapling | Right-click a ripe (split, brown) bur to pick 1–2 chestnuts; the tree is never cut down | Roasted chestnuts; pig feed; a wood set |

### Gourds on stems

- **Planting.** Gourd seeds go on farmland. The stem grows through eight stages, then puts its gourd on a free block beside it that could hold a plant (grass, dirt, farmland, moss and so on) and bends towards it. Leave room around each stem.
- **Harvest.** Break the gourd, or sweep a sickle: the stem straightens and grows another. The stem itself stays.
- **The Three Sisters.** Stems grow by the same rules as Jugcraft crops, so squash planted next to beans grows **1.5× as fast**, and corn, beans and squash make the classic companion field.
- **Decoration.** Gourds are blocks that face the way you place them. Stack them on hay, line a porch or fill a market stall.

### Cranberry bogs

- **A bog crop.** A cranberry bush lives in a still **water source one block deep**, rooted in bog soil: dirt, mud, grass, sand, clay or gravel (block tag `jugcraft:bog_soil`). Plant it by using cranberries on the bottom of such water. It will not go on dry land or into deeper water.
- **Growth.** It grows only while there is **open air above the water** and light 9 or more, a stage in about 5 random ticks like sweet berries. Deep water or a block above stops it.
- **Its water.** The bush holds its own water, like seagrass: breaking it leaves the water behind, and it never drains a pond.
- **Where to find them.** Ripe bushes grow wild in swamp shallows, and grass drops cranberries anywhere.

### The chestnut tree

- **Planting.** A chestnut is the tree's seed: use it on dirt or grass to plant a chestnut sapling, which grows like a vanilla sapling (bone meal works) into a broad tree 6–8 blocks tall.
- **Fruit.** Leaves the tree grew itself, with air below them, grow a green spiny bur that ripens and splits open, about a Minecraft day in all. Right-click a ripe bur to pick 1–2 chestnuts; the leaves start again. Leaves a player places never fruit, so a hedge of chestnut leaves is only a hedge. Broken leaves drop chestnuts and sticks like oak leaves drop saplings.
- **Wood.** Chestnut logs, wood, stripped logs and wood, planks, stairs, slabs, fences and fence gates. Any axe strips a log, and the sawmill saws a log into 6 planks. The wood joins vanilla's wood tags, so it makes sticks, crafting tables and chests, burns as fuel and catches fire like oak.
- **Where to find it.** Chestnut trees grow wild in forests, and grass drops chestnuts anywhere.

### The Turnip Lantern

A turnip over a torch makes a **Turnip Lantern**: a hollowed turnip with a carved, candle-lit face, the jack-o'-lantern of the old autumn festivals before pumpkins came from America. It gives light 13 (a torch gives 14) and faces the player who placed it.

### Festival food

| Food | Made from | Hunger | Saturation modifier | Vanilla comparison |
| --- | --- | --- | --- | --- |
| Turnip (raw) | harvested | 3 | 0.6 | Carrot |
| Cranberries (raw) | picked | 2 | 0.1 | Sweet berries |
| Roasted Chestnuts | cook a chestnut (furnace, smoker or campfire) | 4 | 0.6 | Between a carrot and a baked potato |
| Baked Acorn Squash | cook an acorn squash | 6 | 0.6 | Cooked mutton's hunger |
| Squash Pie | butternut squash, sugar, egg | 8 | 0.3 | Pumpkin pie |
| Candy Corn (4) | corn, sugar, honey bottle | 2 | 0.1 | A sweet, like a cookie |
| Butternut Squash Soup | Cooking Pot: bowl, butternut squash, onion, garlic | 8 | 0.6 | Between mushroom stew and rabbit stew |
| Harvest Stew | Cooking Pot: bowl, turnip, carrot, onion, raw mutton | 10 | 0.6 | Rabbit stew (five ingredients) |
| Cranberry Sauce | Cooking Pot: bowl, 2 cranberries, sugar | 5 | 0.6 | A side dish |

Raw chestnuts are not food; roast them.

### Festival seed sources

| Wild source | Found in | Why there |
| --- | --- | --- |
| Butternut Squash | plains, savanna | Squash is a warm-country crop of the Americas |
| Acorn Squash | forest, taiga | Grown by the forest peoples of North America |
| Warty Gourd | swamp, spooky biomes (dark forest) | Gnarled gourds for the gloomiest places |
| Wild Turnip | taiga, birch forest | Turnips are a northern European field crop |
| Cranberry bushes | swamp shallows | Cranberries are bog plants |
| Chestnut trees | forest | A broadleaf woodland tree |

Gourds lie on grass like vanilla pumpkins (break one and craft it into seeds). Short grass drops all six new seeds too.

## What exists now: Pumpkin Carving

Carve any face into a pumpkin, a pixel at a time, at Minecraft's own pixel size. Nothing here is seasonal either: carved pumpkins stay all year.

| **The carving screen:** the Classic face pressed in, mirror on, candle preview lit | **A row of carved pumpkins** on hay bales by day: Classic, Cat, Ghost (shaved), Spooky, a bat and a star; the pumpkin at the left is carved on two sides |
| --- | --- |
| ![The carving screen with the Classic face](../images/ingame_carving_screen.jpg) | ![Carved pumpkins by day](../images/ingame_carved_pumpkins.jpg) |
| **The same row at midnight**, each with a torch inside | **A pumpkin carved through the screen** above, lit |
| ![Carved pumpkins lit at night](../images/ingame_carved_pumpkins_night.jpg) | ![A lit hand-carved pumpkin](../images/ingame_carved_pumpkin_close.jpg) |

*Real screenshots from the client game test that CI runs (`CarvingClientGameTests`, software rendering, small previews, hotbar cropped). The close-up pumpkin was carved by pressing the screen's buttons and Done; the server then held the face.*

### The Carving Knife

Craft it from an iron ingot over a stick. Use it on the **side** of a pumpkin (not the top) to open the carving screen for that side.

| On the screen | What it does |
| --- | --- |
| **Cut** | Carves right through: a hole the candle shines out of |
| **Shave** | Peels the skin only, so light glows softly through it |
| **Erase** (or right-drag) | Takes back this session's strokes |
| **Brush 1–3** | Carves one pixel, a 2×2 or a 3×3 square at a time |
| **Mirror** | Copies every stroke to the other half of the face |
| **Starter faces** | Classic, Cat, Ghost and Spooky, pressed in with **Apply** |
| **Candle** | Shows the face lit or dark |
| **Undo** (Ctrl+Z), **Reset** | Steps back, or back to how the side was when you opened it |
| **Done** | Carves it (one use of the knife's 238) |

A knife can't put skin back: what was carved before you opened the screen is fixed, and you can only carve deeper. Every side of a pumpkin can carry its own face.

The first cut opens the pumpkin: its 4 seeds fall out, as with shears, and it becomes a **Hand-Carved Pumpkin** facing the side you carved. Roast the seeds in a furnace, smoker or campfire for **Roasted Pumpkin Seeds** (2 hunger).

### Lighting it

Use a **torch** on a hand-carved pumpkin to put it inside. It glows more the more is carved out: 4, plus 1 for every 3 holes and every 12 shaved pixels, up to 15 (a face about the size of the vanilla jack-o'-lantern's gives 15). Use it with an empty hand to take the torch back out. Broken, a carved pumpkin drops with its design and its torch, and placed again it faces you with the same carving.

### On servers

The server checks every carving before anything changes (the knife in hand, reach, permission to build there, and that the carving only goes deeper). A carved pumpkin records who carved it last, for server operators. A server that wants no free drawing can set `carving.free_draw=false` in `config/jugcraft.properties`; then only the starter faces can be carved.

## What exists now: the Halloween harvest

Eleven additions for a fall pumpkin patch, all permanent. Details, numbers and test evidence: [../features/halloween-harvest.md](../features/halloween-harvest.md).

| **The pumpkin patch** from above: giant pumpkins, scarecrows, heirlooms, ornamental corn and the shed | **Giant pumpkins:** one carved with a jack o'lantern face, one by its Harvest Scale, a 2×2×2 and a seedling, with heirloom pumpkins on the right |
| --- | --- |
| ![The Halloween pumpkin patch from above](../images/ingame_halloween_harvest.jpg) | ![Giant pumpkins and the Harvest Scale](../images/ingame_giant_pumpkins.jpg) |
| **Scarecrows** in four shirts, wearing a hand-carved pumpkin, a carved white pumpkin, a jack o'lantern and a carved Cinderella pumpkin on their shoulders | **Heirloom pumpkins** and bottle gourds, hand-carved heirlooms on hay bales, corn shocks and ripe ornamental corn |
| ![Scarecrows](../images/ingame_scarecrows.jpg) | ![Heirloom pumpkins, corn shocks and ornamental corn](../images/ingame_heirloom_pumpkins.jpg) |
| **The shed:** ornamental corn bundles, gourd birdhouses under the eaves, potted mums and wild mums | **Carving a giant pumpkin:** the 48×48 screen with a stencil from the other hand pressed in |
| ![Corn bundles, birdhouses and mums](../images/ingame_harvest_decorations.jpg) | ![The giant carving screen](../images/ingame_carving_giant_screen.jpg) |
| **At midnight:** the carved giant with a torch inside, and the one carved from the stencil | **The scarecrows at midnight** |
| ![Giant pumpkins at night](../images/ingame_giant_pumpkin_night.jpg) | ![Scarecrows at night](../images/ingame_scarecrows_night.jpg) |
| **A scarecrow's head:** worn like an armor stand's pumpkin, bigger than a head, its carving facing out | **The same at midnight,** the carving lit and the scarecrow lit by it |
| ![A scarecrow wearing a hand-carved pumpkin](../images/ingame_scarecrow_head.jpg) | ![The scarecrow's head lit at midnight](../images/ingame_scarecrow_head_night.jpg) |

*Real screenshots from the client game test that CI runs (`HalloweenClientGameTests`, software rendering, small previews, hotbar cropped). The giant was carved from the stencil through the screen; the server then held the face.*

### Giant pumpkins

Plant **Giant Pumpkin Seeds** on farmland. The vine grows like a pumpkin stem (1.5× slower), then sets **one** small fruit beside it. While the vine holds it, the fruit grows: faster on moist farmland, faster still when watered from a **Gourd Canteen**. Bone meal works all the way: it grows the vine, sets the fruit on a full-grown vine, and feeds the fruit (given to the vine or the pumpkin). It swells to **2×2×2**, then **3×3×3**, away from its vine, if there is room on ground fruit can lie on. Full grown, it weighs 100–120 kg and keeps putting on weight, up to 1000 kg, until it is carved.

A giant pumpkin is **one prop**: break any block and you pick up the whole pumpkin as one **Giant Pumpkin** item that keeps its size, weight and carving; place it anywhere and the whole cube goes back down, reaching away from you. Chop it up with an **axe** instead for 9 pumpkins and 1–3 giant seeds when full grown. Pistons can't move it.

Every side of a full-grown giant carves with the Carving Knife as **one 48×48 face**, three times as fine as a pumpkin's; the screen shrinks its cells and offers bigger brushes, and the starter faces come blown up. A torch inside lights every block of it.

Where the seeds come from: the first cut into any pumpkin **scoops** it, and sometimes a giant seed comes out with the **Pumpkin Guts** (they cook into Pumpkin Soup). Short grass drops them too.

### The weigh-off

Put a **Harvest Scale** beside a full-grown giant pumpkin (or carry the pumpkin over and set it down beside the scale) and use it. It weighs the pumpkin and keeps the three heaviest it has weighed on its board. The first time a pumpkin places, whoever weighed it wins a **First, Second or Third Prize Ribbon**. A comparator reads the last weight.

### Fall decorations and treats

| Addition | How to get it | What it does |
| --- | --- | --- |
| **Pumpkin Stencil** | Use a Blank Stencil (two paper) on a carved side | Traces the design; hold it in your other hand while carving to press it in, on any pumpkin or a giant |
| **White, Jarrahdale and Cinderella pumpkins** | Wild in birch forests and snowy places, savannas and windswept hills, plains and flower forests; grass drops their seeds | Grow from stems; carve like pumpkins into their own hand-carved blocks; bake into pumpkin pie |
| **Scarecrow** | Wool over hay between sticks | Two blocks tall; dye its flannel shirt any colour; give it any pumpkin and it wears it for a head (a lit one lights it) |
| **Ornamental corn** | Wild Corn sometimes drops its kernels; grass drops them | Grows like corn; its multicoloured ears tie into an **Ornamental Corn Bundle** for walls and door frames |
| **Corn Shock** | Six Corn Stalks (from breaking any corn 3 blocks tall) and string | A two-block stook for porches |
| **Caramel, Caramel Apple, Popcorn Ball** | Smelt sugar; add an apple and a stick, or two popcorn | Treats (2, 6 and 5 hunger); the apple's stick comes back |
| **Bottle Gourd** | Wild in jungles and savannas; grass drops its seeds | Dry it in a furnace; with string it makes a **Gourd Birdhouse** (stands or hangs), with leather a **Gourd Canteen** (3 sips of water for giant pumpkins, farmland, fire or a cauldron) |
| **Mums** | Wild patches in flower forests, meadows and forests | Yellow, orange, red and purple flowers for pots, dye and suspicious stew |

## What exists now: the pumpkin regatta and trick-or-treating

Two things to do with the Halloween harvest. The regatta works all year; trick-or-treating only while the Halloween event runs. Details, numbers and test evidence: [../features/pumpkin-regatta-and-trick-or-treat.md](../features/pumpkin-regatta-and-trick-or-treat.md).

| **The regatta pond:** a carved Pumpkin Barge with two villagers aboard, a Pumpkin Racer, numbered buoys and the Regatta Flag on the shore | **The barge** close up: a 3×3×3 giant hollowed out, its carved face kept |
| --- | --- |
| ![The regatta pond](../images/ingame_pumpkin_regatta.jpg) | ![A Pumpkin Barge and a Pumpkin Racer](../images/ingame_pumpkin_barge.jpg) |
| **At midnight:** the barge's torch lights its carving | **Costumes:** a carved pumpkin, Witch Hat, Ghost Sheet, Scarecrow Hat and a hand-carved pumpkin, by a door with its jack o'lantern porch light |
| ![The barge at night](../images/ingame_pumpkin_barge_night.jpg) | ![Costumes on armor stands](../images/ingame_costumes.jpg) |
| **The Ghost Sheet** on its stand: a hood with eye holes, draped to the ground | **Worn by a player:** the sheet over the head, body and arms, moving with them |
| ![The Ghost Sheet on an armor stand](../images/ingame_ghost_sheet.jpg) | ![A player wearing the Ghost Sheet](../images/ingame_ghost_sheet_worn.jpg) |

*Real screenshots from the client game test that CI runs (`RegattaClientGameTests`, software rendering, small previews).*

### Pumpkin boats and the regatta

Sneak and use the Carving Knife on top of a giant pumpkin to **hollow it out into a boat**. A full-grown 3×3×3 giant makes a **Pumpkin Barge**: four seats, and it keeps its weight, carving and torch, so a carved barge glows on the water at night. A 2×2×2 giant (stop it there by cutting its vine or giving it no room) makes a **Pumpkin Racer** for one. Lighter boats are faster: a racer 1.15–1.30× a boat, a barge 0.70–0.95×. Hollowing gives the pumpkin's guts and, full grown, its giant seeds, but not its pumpkins.

For a race, put a **Regatta Flag** on the shore and **Regatta Buoys** on still water, and number them by using them (1–16; sneak to count down). Used on foot, the flag finds its course and shows the board. Used from a pumpkin boat's driver's seat, it starts a run: after a three-second countdown, pass within 5 blocks of each buoy in order and come back to the flag. The server times it. The flag keeps the three best times, one per racer, and the first time you place you get the Harvest Scale's ribbon for that place.

### Trick-or-treating

While the Halloween event runs (by default 20 October to 3 November; the server operator sets the dates), use a **Candy Bag** on a villager's wooden door between dusk and midnight. Wear a costume on your head (a carved pumpkin, any hand-carved one, or a **Witch Hat**, **Ghost Sheet** or **Scarecrow Hat**), and make sure a porch light burns by the door (a jack o'lantern, a turnip lantern, or a lit hand-carved or giant pumpkin). The villager whose bed is inside opens up and hands you a treat: candy, caramel, cookies, a popcorn ball, a caramel apple or, rarely, a **King-Size Candy Bar**. Each home gives each player one treat a night; knock again and you get a harmless prank. Ten homes in one night earn **Full Bag**. When the event ends, nobody answers, but every treat and costume stays.

## What exists now: the Halloween festivities

More to do around Halloween, and decorations and sweets for any time of year. Voting, costumed mobs and the Peddler only happen while the Halloween event runs. Details, numbers and test evidence: [../features/halloween-festivities.md](../features/halloween-festivities.md).

| **The festivities:** costumed mobs, the graveyard, a haunted arch, the Judging Stand, the sweets and the Peddler | **At midnight:** the Candle Skulls and the carved pumpkin on the stand glow |
| --- | --- |
| ![The Halloween festivities](../images/ingame_halloween_festivities.jpg) | ![The festivities at night](../images/ingame_halloween_festivities_night.jpg) |
| **The graveyard:** Rounded, Cross and Obelisk Gravestones, engraved, with Candle Skulls | **An engraving** up close: "Here lies Jack O'Lantern, carved too deep" |
| ![Gravestones and Candle Skulls](../images/ingame_gravestones.jpg) | ![An engraved gravestone](../images/ingame_gravestone_engraving.jpg) |
| **The Judging Stand** with a lit carving, the four sweets, and the Halloween Peddler in his Witch Hat | |
| ![The Judging Stand, the sweets and the Peddler](../images/ingame_judging_stand.jpg) | |

*Real screenshots from the client game test that CI runs (`FestivityClientGameTests`, software rendering, small previews).*

### The carving contest

Put a hand-carved pumpkin on a **Judging Stand** and use the stand with an empty hand to enter it (only its carver can). While the event runs, everyone else uses the stand to vote for its carver: one vote per player per Halloween, moved by voting elsewhere, never for yourself. Sneak-use a stand for the standings. When the event ends, the three carvers with the most votes get the Harvest Scale's ribbons, once.

### Costumed mobs and the Halloween Peddler

During the event, 15 % of zombies, husks, skeletons, strays and zombie villagers wear a Witch Hat, Ghost Sheet, Scarecrow Hat or carved pumpkin, and drop a sweet when a player kills them. Wandering traders arrive as the **Halloween Peddler**, in a Witch Hat, selling four Halloween goods for emeralds: pumpkin seeds (giant and heirloom), costumes, decorations and sweets.

### Spooky decorations and sweets

**Gravestones** (Rounded, Cross and Obelisk, from a stonecutter) take a name: use a Name Tag named in an anvil on one, and its name is engraved on the stone. The **Spun Cobweb** looks like a cobweb but never slows anyone; the **Hanging Ghost** hangs under a block; the **Candle Skull** lights and snuffs like a candle. The Cooking Pot boils sugar into four sweets that work even on a full stomach: **Glow Gum** (Glowing), **Ghost Taffy** (a moment of invisibility), **Fizz Rocks** (Jump Boost) and **Witch's Licorice** (Night Vision).

## What exists now: Halloween nights

Things that happen on Halloween nights, and a throwing contest for any time of year. Wisps, the Horseman and the Harvest Moon only come while the Halloween event runs; everything they leave behind stays. Details, numbers and test evidence: [../features/halloween-nights.md](../features/halloween-nights.md).

| **Halloween nights** by day: the cornfield and scarecrow, three trebuchets, jars and lanterns, the cloak | **At midnight:** wisps over the corn, the Horseman, glowing jars and lanterns |
| --- | --- |
| ![Halloween nights](../images/ingame_halloween_nights.jpg) | ![Halloween nights at midnight](../images/ingame_halloween_nights_night.jpg) |
| **Trebuchets:** loaded, ready and just thrown, with a landing marker | **Wisps in Jars and Horseman's Lanterns**, standing and hanging, and the cloak on an armor stand |
| ![Three trebuchets](../images/ingame_trebuchets.jpg) | ![Jars and lanterns](../images/ingame_wisp_jars_and_lanterns.jpg) |
| **Will-o'-wisps** over the corn at midnight | **The Headless Horseman** between two of his lanterns |
| ![Will-o'-wisps over corn](../images/ingame_wisps.jpg) | ![The Headless Horseman](../images/ingame_headless_horseman.jpg) |
| **The Harvest Moon:** sparks over lit carvings | |
| ![Harvest Moon sparks](../images/ingame_harvest_moon_sparks.jpg) | |

*Real screenshots from the client game test that CI runs (`NightClientGameTests`, software rendering, small previews).*

### Will-o'-wisps

On event nights, little glowing **will-o'-wisps** drift over swamps and cornfields and dart away when you come near (sneak to get close). Use a **glass bottle** on one to catch it in a **Wisp in a Jar**, a lantern that glows all year. At dawn they fade.

### The Pumpkin Chunkin' Trebuchet

Load a **Trebuchet** with a pumpkin (any kind, carved or not), sneak-use it to set the release angle (30°–60°), and use it with an empty hand to fling the pumpkin about 50 blocks. A marker shows where it landed and how far. The trebuchet keeps a board of the three longest throws and gives the Harvest Scale's ribbons once per thrower. Hollow carved pumpkins fly farthest. Throw 50 blocks for **Pumpkin Chunkin'**.

### The Candy Bag and the Harvest Moon

The **Candy Bag** now holds treats like a bundle; trick-or-treating fills it, and its tooltip counts tonight's homes. On the nights of 31 October (in the server's time zone) the **Harvest Moon** rises: crops and giant pumpkins grow twice as fast, and lit carvings throw sparks.

### The Headless Horseman

Near midnight during the event, give a Scarecrow a lit pumpkin for a head and sneak-use it under the open sky. The **Headless Horseman** rides in for his head: a boss with a boss bar who charges and throws flaming pumpkins (they burn creatures, never blocks), and throws three at once when enraged at half health. He keeps to his arena and rides off at dawn. Defeat him for the **Horseman's Lantern** and **Horseman's Cloak** and the **Lost His Head** advancement.

## Crop roster: what comes next (planned)

The branch grows in small slices that each stand on their own. Each crop needs a job: a food, a material another branch wants, or a farming mechanic. Proposals are welcome.

| Slice | Crops | New mechanic | Why it matters |
| --- | --- | --- | --- |
| **1. Fall Harvest** ✅ | Corn, sunflower, beans, sweet potato, flax | Tall crops, picking, legumes, wild plants, sickles | Starter farming; mazes and fields; string, feed and stew |
| **2. Kitchen garden** ✅ | Tomato, onion, garlic, cabbage, pepper, oats, barley | Trellises for climbing crops; the **Cooking Pot** for multi-ingredient meals | A real kitchen: soups, salads, porridge. Cabbage + salt → sauerkraut uses Jugcraft's salt. Garlic can later double as a ward against the planned Vampirism school (not built yet) |
| **3. Festival Crops** ✅ | Butternut and acorn squash, warty gourds, turnip, cranberry, chestnut tree | Gourds grow from stems like pumpkins; bog crops in shallow water; a fruit tree | Halloween: Turnip Lanterns (the original jack-o'-lantern), gourd displays, candy corn. December: cranberry sauce, roasted chestnuts. All permanent, so nothing is lost after a season |
| **4. Orchards and vines** | Apple, pear, peach, lemon and orange trees; grapes and hops on trellises; blueberries and raspberries | Fruit trees that grow from saplings and fruit every year without being cut down | Juices, pies, preserves; the cider press; long-term homestead beauty |
| **5. Fibre, oil and industrial crops** | Cotton, canola, sugar beet, rubber tree, indigo and madder | Tapping (rubber) and retting (flax to linen) | What engineers need from farmers: rubber for insulated cables and belts, plant oil for lubricant and biodiesel, sugar and corn for ethanol, fibres for canvas, dyes |
| **6. Magical botany** | One herb per magic school (proposal names: Emberroot, Frostcap, Stormreed, Stonebloom, Gravemoss, Bloodthorn, Nightshade, Moonpetal) | Attunement: an herb grows only near its school's influence or with a ritual catalyst | Reagents for the magic branch; see [CONTENT_BRANCHES.md](../CONTENT_BRANCHES.md#magical-workshops-and-schools) |
| **7. Rice and wet farming** | Rice, taro, water chestnut | Paddy crops that grow in one block of still water | A distinct regional farm; rice dishes |
| **8. Off-world farming** | Adapted crops for the Moon and beyond | Sealed greenhouse habitats | Expedition food; a shared engineering and agriculture milestone ([space](../CONTENT_BRANCHES.md#space-and-magical-realms)) |

## Farm equipment (planned, not industrial)

Hand tools and farmstead blocks, made from wood, stone and early metals. None needs power.

| Equipment | What it does | Tier |
| --- | --- | --- |
| **Flint and Bronze Sickles** ✅ | Harvest and replant ripe crops in 3×3 or 5×5 | Discovery |
| Steel Sickle | 7×7 once the steel tier lands | Workshops |
| Watering Can (tinplate) | Moistens farmland in a 3×3 area and gives crops a small, bounded growth chance per use; refills at any water | Discovery |
| Seed Pouch | Holds several seed types; right-click plants a 3×3 patch of farmland from it | Discovery |
| **Trellis (wood)** ✅ | Supports climbing crops (tomatoes now; grapes, hops and pole beans later); also a garden decoration | Discovery |
| **Carving Knife** ✅ | Carves any face into the sides of a pumpkin, a pixel at a time; see [Pumpkin Carving](#what-exists-now-pumpkin-carving) | Discovery |
| **Scarecrow** ✅ | Decoration for fields and Halloween, in a flannel shirt you dye, with any pumpkin for a head; see [the Halloween harvest](#what-exists-now-the-halloween-harvest). Later keeps crop-eating birds away once those creatures exist | Discovery |
| **Corn Shock** ✅ | A stook of corn stalks for fall porches and markets (decoration) | Discovery |
| **Harvest Scale** ✅ | Weighs giant pumpkins, keeps a board of the three heaviest and gives prize ribbons | Discovery |
| Bushel Basket | Small produce storage for markets | Discovery |
| Compost Bin | Turns scraps into Compost, an organic fertilizer that makes farmland *fertile* for a while (faster growth); the no-chemistry route | Discovery |
| Quern (hand mill) | Grinds corn into cornmeal and grain into flour (cornbread, pancakes) | Discovery |
| Drying Rack | Retts flax into linen fibre, dries herbs and fruit, makes jerky | Workshops |
| Fruit and Seed Press | Apples into cider, grapes into juice, and **sunflower or flax seeds into plant oil at a low hand-press yield**, so engineers can buy early lubricant from farmers | Workshops |
| Butter Churn and Cheese Press | Dairy from the husbandry slice | Workshops |
| **Cooking Pot** ✅ | Multi-ingredient meals over a campfire (expedition rations later) | Discovery (iron and a campfire; planned for Workshops, moved earlier because it needs no workshop) |

## Connections to other branches

The farmer's goods are useful elsewhere; nothing here *requires* another branch.

| Other branch | What agriculture gives | What it gets back |
| --- | --- | --- |
| Engineering | Plant oil (lubricant, biodiesel), ethanol (fuel), rubber, fibres, starch | Powered harvesters, planters, sprinklers and processors that automate the slow parts (later) |
| Chemistry ([CHEMISTRY.md](CHEMISTRY.md)) | Biomass, sugars, oils | Phosphate fertilizer: a stronger alternative to compost, never required |
| Magic | School herbs, flower essences, beeswax | Attunement catalysts that let rare herbs grow |
| Husbandry and creatures | Feed crops (corn, kernels, seeds) | Manure for compost; bees for pollination |
| Space | Expedition food and seeds | Greenhouse habitat modules |
| Seasons | Corn mazes, popcorn, gourds, cranberries and chestnuts | Seasonal recipes and decorations built on these permanent crops |

## Rules for this branch

- **Reachable from day one.** Every crop has a wild source and a grass-drop source. No crop needs another branch, a rare biome or a seasonal event to start.
- **No free loops.** Crops multiply through time and light, as in vanilla; recipes only convert downhill (corn → kernels, flax → string). `tools/check_mod_data.py` fails if agriculture recipes ever form a loop.
- **No chores.** Farmland under crops does not decay; crops do not die of neglect; fertilizer and irrigation speed things up but are never required.
- **Bounded server work.** Growth runs on vanilla random ticks. A tall crop ticks only in its bottom block and reads at most 17 block states per tick. A sickle touches at most 75 blocks per use (5×5×3). A Cooking Pot reads one block (its heat source) per tick and looks up its recipe only when its slots change. A gourd stem reads the same 17 block states plus 2 when it places a gourd; a cranberry bush reads the block above it; chestnut leaves read the block below them, and only leaves a tree grew tick at all (they tick all the time to grow burs, where vanilla leaves tick only while decaying). Carved pumpkins never tick; a carving checks its 256 pixels once (2,304 on a giant). Only a giant pumpkin's corner block ticks, reading at most 3 blocks, or 54 when it grows a size; breaking one removes at most 27 blocks. The Harvest Scale reads 5 neighbours when used. Nothing scans the world.
- **One source of truth.** IDs, heights, yields, foods and biomes live in [`tools/agriculture.py`](../../tools/agriculture.py); the Java must match it, and the checker compares them.
- **Stable IDs.** Crop blocks keep their IDs after release. The `agriculture` switch in `config/jugcraft.properties` turns off recipes, wild plants in new chunks and grass drops, but never removes registered blocks or items, so planted fields survive.

## Boundaries

- **Industrial farming belongs to engineering, later.** Powered harvesters, automatic planters, sprinklers on the fluid network, crop processors, oil press machines, fermenters and distilleries are engineering machines that *use* this branch's crops. They will live in the machine roadmap, not here.
- **No world seasons.** Minecraft has no seasons, and adding them would add chores. "Fall crops" grow all year. The Halloween and December events reuse these permanent crops.
- **Vanilla crops stay vanilla.** Sickles harvest vanilla crops, but the legume bonus applies only to Jugcraft crops, because changing vanilla growth would need a mixin.
