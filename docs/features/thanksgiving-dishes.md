# Thanksgiving side dishes (Thanksgiving batch 2)

Status: integrated locally and compiled. All six dishes pass their server regressions within the 440-test combined cohort, and the seasonal client gallery passes. Codex personally inspected all six actual Minecraft dishes on November 14; owner play review remains pending. See [combined evidence](thanksgiving-expansion-nov14.md). Nothing is published.
Proposal issue: none. The coordinator's Thanksgiving batch 2 assignment (six feast dishes) is the scope approval.
Owner: the project owner (request).

Implemented with Claude Code.
Target milestone and tier: seasonal food, no tier; craftable from the first day.
Primary specialty and supported player role: cooking and hosting (a feast table to set and eat from).

## Player experience

Six new side dishes for a Thanksgiving table. Each is a food crafted by hand from farm produce. Each is eaten like the menu's plated meals: a bowl food that gives the bowl back, one to a stack. A sneaking player sets one down on a block as its sculpted dish, facing them, and takes it back with an empty hand, exactly as the menu's dishes (`PlacedDishBlock`). Set down, it is only ever the one food it was: broken, it drops that food once; taken back, it is that food again. None needs research or follows the calendar.

| Dish | Food (hunger, saturation modifier) | Set down as |
|---|---|---|
| Glazed Carrots | 9, 0.7 | carrot batons heaped crosswise in three layers on the wooden plate, glistening in a pool of butter-and-sugar glaze, carrot coins in front, thyme leaves |
| Roasted Root Vegetables | 10, 0.7 | chunks of roast potato, carrot, beetroot, turnip and onion of every size on the wooden plate, a few askew, a second layer in the middle, rosemary sprigs |
| Scalloped Potatoes | 9, 0.7 | a ceramic baking dish (the glazed plate's cream and blue) filled with cream, three rows of round potato slices shingled along it with browned rims, browned cheese patches, thyme |
| Apple Cranberry Crisp | 8, 0.6 | the ceramic baking dish filled with fruit under a golden-brown crumble in deliberate clumps at three heights, apple slices and cranberries showing along the front, a round scoop of cream |
| Stuffed Mushrooms | 7, 0.6 | five brown caps on the glazed plate, the middle back one bigger, each filled with a golden crumb stuffing flecked with herbs, two parsley sprigs |
| Skillet Corn Cakes | 9, 0.7 | three round corn cakes in the owner's Skillet, two side by side in melted butter and one stacked and turned, browned rims, a pat of butter on top |

### The inventory: the candidates against existing content

The coordinator's six candidates were checked against what Jugcraft already has, so nothing is added twice.

| Candidate | Outcome | Why |
|---|---|---|
| Glazed carrots | New: Glazed Carrots | No glazed carrot dish exists. First claimed as `honey_glazed_carrots`; renamed so the recipe takes no honey bottle (a filled container, see Balance). |
| Roasted root vegetables | New: Roasted Root Vegetables | Herb-Roasted Potatoes is potatoes alone; the Root Vegetable Hamper is a decoration. |
| Maple baked squash | Substitute: Scalloped Potatoes | Baked Acorn Squash already exists (a baked half squash). Succotash was weighed for the slot and dropped: Three Sisters Stew already makes corn and beans into a dish. |
| Cranberry tart | Substitute: Apple Cranberry Crisp | The Hearth Oven already bakes a Cranberry Pie. |
| Stuffed mushrooms | New: Stuffed Mushrooms | No stuffed mushroom dish exists. On the glazed plate, as brown caps would vanish on wood. |
| Skillet corn cakes | New: Skillet Corn Cakes | Distinct from the Cornbread loaf, the Cornbread Stuffing's cubes and the Maple Pancakes (wheat). |

## How it is built

- `tools/thanksgiving_dishes.py`: the table (names, foods, shapes, recipes) and the balance numbers (`MOST_HUNGER`, `COOK_BONUS`).
- `tools/thanksgiving_dishes_data.py`: the models, textures, blockstates, items, loot and recipes. Every dish is sculpted as `docs/FOOD_MODELS.md` asks, through the menu's placed-food pipeline (`tools/menu_meal_models.py` for the dishware, piece helpers and the writer that closes models, drops buried faces and keeps faces apart; `tools/menu_meal_textures.py` for each dish's `<dish>_top` and `<dish>_side`). The dishes are lent to those modules' `MEALS` only while one is built, so the menu never sees them. The ceramic baking dish is built in this module, in the glazed plate's textures.
- `ThanksgivingDishes` (Java): registers each food in the food tab and its `PlacedDishBlock` of the same ID, set down in the pie's use-block phase, which runs after the default phase where claim and town protection decide.
- Hooks: `JugcraftAgriculture` calls `ThanksgivingDishes.register` after the menu's placed dishes; `tools/agriculture.py`, `agriculture_data.py` and `crop_textures.py` include the dishes so the full generator writes the same files.

### Colour provenance

Colours are read from the owner's own drawings in `art/owner-library/originals/Blocks/farming` and food textures (read only, never changed) and written out as hex:
- `carrot_crate_top.png` (carrot), `potato_crate_top.png` (potato), `beetroot_crate_top.png` (beetroot), `onion_crate_top.png` (onion);
- `brown_mushroom_colony_stage3.png` (mushroom caps);
- `apple_pie_inner.png` (apple filling and cranberry);
- `cornbread.png` (corn cakes, crumble and stuffing);
- `bowl.png` (the woods, as `tools/menu_meal_textures.py`).

The glaze, butter, cream, browned cheese, herbs and turnip are written beside the menu's own (`tools/menu_meal_textures.py` `steak_and_potatoes` butter and parsley).

### Set-down outlines

All six use `PlacedDishBlock.DishShape.PLATE` (1..15 across, 5.5 high). The two baking-dish dishes, Scalloped Potatoes and Apple Cranberry Crisp, were first planned as `BOWL` (3..13, 6.5 high); the ceramic baking dish is 14 by 12, so `PLATE` is the outline that covers it. The check measures every model inside its outline.

## Balance and automation

- **Food** follows `docs/BALANCE.md` as the menu's do: at most `COOK_BONUS` (3) more hunger than the most its ingredients could give (a potato counted as baked; Cornbread Batter as its two corn, egg and milk). Saturation modifiers 0.6 to 0.7: side dishes, a little under the menu's main plates (Steak and Potatoes 13, 0.8) and level with Three Sisters Stew (10, 0.6) and Baked Acorn Squash (6, 0.6).

  | Dish | Ingredients' most hunger | Cap | Food |
  |---|---|---|---|
  | Glazed Carrots | 9 | 12 | 9 |
  | Roasted Root Vegetables | 12 | 15 | 10 |
  | Scalloped Potatoes | 10 | 13 | 9 |
  | Apple Cranberry Crisp | 8 | 11 | 8 |
  | Stuffed Mushrooms | 5 | 8 | 7 |
  | Skillet Corn Cakes | 6 | 9 | 9 |

- **Recipes** are shapeless, made by hand, follow the agriculture switch only, make one dish, and take one bowl (the bowl eating it gives back). No recipe takes a filled container (a bucket or a bottle): `tools/build_alchemy_values.py` skips recipes that give a container back, and a dish valued by such a recipe would fall to the one-essence fallback. Nothing else takes or makes a dish.
- **Essence** (`data/jugcraft/alchemy/values.json`, six records added, none rewritten). Each is its recipe cost by `tools/build_alchemy_values.py`'s formula, with the source `recipe jugcraft:<dish>`:

  | Dish | Essence |
  |---|---|
  | Glazed Carrots | 598/3 (about 199.3) |
  | Roasted Root Vegetables | 201 |
  | Scalloped Potatoes | 137 |
  | Apple Cranberry Crisp | 415/3 (about 138.3) |
  | Stuffed Mushrooms | 144 |
  | Skillet Corn Cakes | 25/3 (about 8.3) |

  The dishes are food, not convenience decor, so the owner's rule for decor at 128 essence or less (an explicit tablet output) does not apply. No dish is formable (`jugcraft:tablet_outputs`) or sold or bought by a town shop, so neither essence nor Jugs can be made from one. Skillet Corn Cakes is low because two of its inputs (Cornbread Batter and Butter) are still at the one-essence fallback ("balance pending"). Its record is still exactly its inputs' cost, so it opens no loop. When those inputs are valued, rebuilding the values with `tools/build_alchemy_values.py` gives the dish its new cost.
- **Automation:** none. The dishes do nothing on their own.

## Multiplayer, persistence and protection

- A set-down dish is a plain block with a `facing` state and no block entity; it keeps nothing else.
- In a private claim, an outsider can neither set a dish down, take one, nor break one (`LandProtection` decides in the default phase, before the set-down phase).
- Town visitors cannot take back ordinary placed dish displays. Codex repaired the shared `TownProtection` handling; the actual protected-region regression passes for all six meals. Managed public feast serving retains its separate permission path.

## Dependencies and assets

- New IDs (claimed in the batch's ID list): `glazed_carrots`, `roasted_root_vegetables`, `scalloped_potatoes`, `apple_cranberry_crisp`, `stuffed_mushrooms`, `skillet_corn_cakes`, each an item and a block. Each has a block model, item model, blockstate, item definition, loot table, recipe and two textures (`<dish>_top`, `<dish>_side`).
- Ingredients are vanilla items and existing Jugcraft produce: Butter, Turnip, Onion, Rosemary, Garlic, Thyme, Cranberries, Flour and Cornbread Batter.
- All six dishes are included in `c:foods` by the shared source generator; runtime tag assertions pass.

## Verification

### Original worker checks (before local integration)

- `tools/thanksgiving_dishes_check.py`: 0 problems.
- `scripts/tests/test_thanksgiving_dishes.py`: 4 tests OK.
- `tools/art_check.py` `check_block_models` over the models in this sparse worktree, including the six dishes: 0 findings.
- `scripts/tests/test_thanksgiving_economy.py`, on an export of the full `data` tree with the new `values.json`: 6 tests OK.

### Original worker syntax checks

`ThanksgivingDishes.java`, `JugcraftAgriculture.java`, `ThanksgivingDishGameTests.java` and `ThanksgivingGalleryClientGameTests.java`: 0 errors.

### Written, not run in Minecraft

- `ThanksgivingDishGameTests`: foods, stack size and the bowl given back; set down facing the player, kept when used with something in hand, taken back with an empty hand, one drop when broken; a stranger in a private claim cannot set down, take or break, while the owner can; every recipe and loot table loads.
- `ThanksgivingGalleryClientGameTests`, the feast showroom: the six dishes on oak plank tables, the Spinning Top at rest, mid-spin and wobbling, and the Corn Toss Set before, during and after a round, with the server's ticks frozen while they are photographed; the toys' row close and from behind; the backdrop's bulbs off at noon.

### Original worker limitations, subsequently resolved locally

The sparse worker environment could not compile or run Minecraft or the complete data audit. Codex subsequently ran all three with the local pinned toolchain; the combined server, seasonal client and full data/model audit results pass as recorded above.

## Rollout and open questions

- Owner acceptance of the visuals from runtime screenshots (the client gallery).
- Local town-display protection and managed serving paths now pass combined runtime checks.
- Local source-generator `c:foods` membership now passes runtime assertions.
- Values for Cornbread Batter and Butter, which would raise Skillet Corn Cakes' essence.
