# Agriculture branch

Status: **first slice implemented in source: the Fall Harvest crops, wild plants and sickles.** It compiles in CI and has automated in-game tests, but nobody has played it yet. Everything marked *planned* below is a design proposal, not a promise. See [../features/fall-harvest.md](../features/fall-harvest.md) for the implemented slice and its test evidence.

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

2. **Short grass.** Breaking short grass has a 2 % chance per crop to drop its seed (about 1 in 10 grass blocks gives some Jugcraft seed; vanilla wheat seeds are 12.5 %). This works in any biome and in worlds created before this feature.

### Systems

- **Tall crops ([`TallCropBlock`](../../src/main/java/io/github/jimbozoomer/jugcraft/agriculture/TallCropBlock.java)).** One block type per crop; a `section` property says which block of the plant it is, and every section shows its own slice of the same growth stage. Only the bottom block ticks and has loot, so a plant can never drop twice. Heights, pick yields and growth speed live in [`TallCrop`](../../src/main/java/io/github/jimbozoomer/jugcraft/agriculture/TallCrop.java) and [`tools/agriculture.py`](../../tools/agriculture.py); adding another tall crop is a table entry plus textures.
- **Picking.** Ripe tall crops go back to a younger stage *of the same height* when picked (corn and sunflowers go back to stage 5 of 7), so they regrow produce without shrinking.
- **Legumes feed their neighbours.** Crops next to beans (any of the 8 surrounding blocks) grow **1.5× as fast**. Beans themselves do not get the bonus, so the best field mixes crops. This is the classic *Three Sisters* planting (corn, beans and squash) that the stew is named after. The legume list is the block tag `jugcraft:nitrogen_fixing_crops`, so data packs and later crops can join it.
- **Growth.** Otherwise Jugcraft crops grow like vanilla crops: farmland under and around the plant, moisture, and light 9 or more. Corn takes 1.5× and sunflowers 1.25× as long as wheat per stage, because they are bigger plants that regrow after picking. Bone meal works: 1–2 stages on tall crops, as far as there is room above.
- **Sickles.** Right-click a crop with a sickle to harvest every *ripe* crop around it: 3×3 for the **Flint Sickle**, 5×5 for the **Bronze Sickle**, one block up or down. One-block crops (including vanilla wheat, carrots, potatoes and beetroots) are replanted automatically with a seed from their own drops; tall crops are picked and stay standing; unripe crops are left alone. Each use costs 1 durability. The sickle checks spawn protection and claims for every block it touches.
- **Composting and animal feed.** Every crop item composts at vanilla-like rates (seeds 30 %, produce 65 %, cooked food 85 %). Pigs eat corn and sweet potatoes; chickens and parrots eat corn kernels, sunflower seeds and flax seeds (vanilla animal-food tags, so breeding works).

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

## Crop roster: what comes next (planned)

The branch grows in small slices that each stand on their own. Each crop needs a job: a food, a material another branch wants, or a farming mechanic. Proposals are welcome.

| Slice | Crops | New mechanic | Why it matters |
| --- | --- | --- | --- |
| **1. Fall Harvest** ✅ | Corn, sunflower, beans, sweet potato, flax | Tall crops, picking, legumes, wild plants, sickles | Starter farming; mazes and fields; string, feed and stew |
| **2. Kitchen garden** | Tomato, onion, garlic, cabbage, pepper, oats, barley | Trellises for climbing crops; the **Cooking Pot** for multi-ingredient meals | A real kitchen: soups, salads, porridge. Cabbage + salt → sauerkraut uses Jugcraft's salt. Garlic doubles as a ward against the planned Vampirism school |
| **3. Autumn & winter festivals** | Decorative gourds (butternut, acorn, warty), turnip, cranberry, chestnut tree | Gourds grow from stems like pumpkins; bog crops in shallow water | Halloween: carved turnip lanterns (the original jack-o'-lantern), gourd displays, candy corn. December: cranberry sauce, roasted chestnuts. Earned items stay after the season |
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
| Trellis (wood) | Supports climbing crops (tomatoes, grapes, hops, pole beans); also a garden decoration | Discovery |
| Scarecrow | Decoration for fields and Halloween; later keeps crop-eating birds away once those creatures exist | Discovery |
| Corn Shock and Bushel Basket | Decoration and small produce storage for fall porches and markets | Discovery |
| Compost Bin | Turns scraps into Compost, an organic fertilizer that makes farmland *fertile* for a while (faster growth); the no-chemistry route | Discovery |
| Quern (hand mill) | Grinds corn into cornmeal and grain into flour (cornbread, pancakes) | Discovery |
| Drying Rack | Retts flax into linen fibre, dries herbs and fruit, makes jerky | Workshops |
| Fruit and Seed Press | Apples into cider, grapes into juice, and **sunflower or flax seeds into plant oil at a low hand-press yield**, so engineers can buy early lubricant from farmers | Workshops |
| Butter Churn and Cheese Press | Dairy from the husbandry slice | Workshops |
| Cooking Pot | Multi-ingredient meals and expedition rations | Workshops |

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
- **Bounded server work.** Growth runs on vanilla random ticks. A tall crop ticks only in its bottom block and reads at most 17 block states per tick. A sickle touches at most 75 blocks per use (5×5×3). Nothing scans the world.
- **One source of truth.** IDs, heights, yields, foods and biomes live in [`tools/agriculture.py`](../../tools/agriculture.py); the Java must match it, and the checker compares them.
- **Stable IDs.** Crop blocks keep their IDs after release. The `agriculture` switch in `config/jugcraft.properties` turns off recipes, wild plants in new chunks and grass drops, but never removes registered blocks or items, so planted fields survive.

## Boundaries

- **Industrial farming belongs to engineering, later.** Powered harvesters, automatic planters, sprinklers on the fluid network, crop processors, oil press machines, fermenters and distilleries are engineering machines that *use* this branch's crops. They will live in the machine roadmap, not here.
- **No world seasons.** Minecraft has no seasons, and adding them would add chores. "Fall crops" grow all year. The Halloween and December events reuse these permanent crops.
- **Vanilla crops stay vanilla.** Sickles harvest vanilla crops, but the legume bonus applies only to Jugcraft crops, because changing vanilla growth would need a mixin.
