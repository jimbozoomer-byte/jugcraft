# Kitchen Garden: trellises, seven crops and the Cooking Pot

Status: implemented in source; **not yet played**. The Build workflow compiles it, and CI's game tests pass (details below).
Proposal issue: none; requested directly by the owner on 30 September 2026 ("start the next agriculture slice"), following slice 2 of the plan in [branches/AGRICULTURE.md](../branches/AGRICULTURE.md#crop-roster-what-comes-next-planned).
Owner: @jimbozoomer-byte
Target milestone and tier: Milestone 3 (first homestead); Discovery tier. The Cooking Pot needs iron and a campfire; sauerkraut optionally uses salt from the mining branch.
Primary specialty and supported player role: farming and cooking; supports animal breeders, builders (trellis gardens) and every player who eats

## Player experience
Grow a kitchen garden next to the Fall Harvest fields:

- **tomatoes that climb trellises**, two blocks tall, and are picked without cutting them down;
- **pepper bushes**, picked the same way;
- **onions, garlic, cabbage, oats and barley**, one-block crops that follow vanilla rules.

Stand trellises on farmland (stack two for tomatoes), plant tomato seeds on them, and pick red tomatoes all season. Put an iron **Cooking Pot** on a campfire and cook soups, porridge, chili and cabbage rolls from six ingredient slots. Make garden salad, sauerkraut (with Jugcraft salt) and barley bread by hand. The player guide is the Kitchen Garden section of [branches/AGRICULTURE.md](../branches/AGRICULTURE.md#what-exists-now-the-kitchen-garden).

## Connections
- Existing input producer: the Overworld (seven new wild plants in jungle, savanna, badlands, plains, hills, forest, taiga and windswept biomes; short grass everywhere), vanilla farmland, campfires, bowls, sticks, iron, bread, carrots, potatoes, sugar, brown mushrooms and raw beef. Beans come from the Fall Harvest. Salt comes from the mining branch's rock salt and is only needed for sauerkraut.
- Existing output consumer: food for every player. Animal breeding through vanilla tags (`rabbit_food`, `pig_food`, `cow_food`, `sheep_food`, `goat_food`, `horse_food`, `chicken_food`, `parrot_food`). Composters. `c:` convention tags (`c:crops/*`, `c:seeds/*`, `c:foods/*`) let other mods use the crops and dishes.
- Technology connection: optional. The pot's heat sources are a block tag (`jugcraft:heat_sources`), so a later engineering heater can join it without code. Cooking Pot recipes use the same data format as Jugcraft's multi-input machine recipes.
- Magic connection: none yet. Garlic is noted in the plan as a future ward for the planned Vampirism school; nothing is built for it.
- Reachable entry path: a hoe, farmland and a seed from a wild plant or grass; for tomatoes, 5 sticks for two trellises; for the pot, 5 iron ingots, 2 sticks and a campfire. No crop needs another branch, a machine or an event.
- Required vs optional: nothing is required. Every Cooking Pot dish is extra food; the salt recipe is one optional dish.
- How this stays useful without other branches: food with more variety and value than vanilla, animal feed, and garden decoration.

## Balance and automation
- **Growth:** the same rules as the Fall Harvest (vanilla soil formula, ×1.5 next to beans, no crowding penalty). Tomatoes and peppers take 1.25× wheat's time per stage and are picked back to stage 5 of 7.
- **Yields:** picking gives 2–4 tomatoes or 1–3 peppers per regrowth. Onions and garlic follow vanilla carrots; cabbage, oats and barley follow vanilla wheat. Fortune applies to broken crops.
- **Food values** stay within vanilla's range: raw vegetables like a carrot or apple, bread like bread, soups between mushroom stew and rabbit stew, and chili (six ingredients including beef) at rabbit stew's hunger with a steak's saturation. Full table in the branch document.
- **No positive-gain loops:** seeds come from produce (1 tomato → 2 seeds, 1 pepper → 2 seeds), and every other recipe turns crops into food. `tools/check_mod_data.py` fails if any agriculture recipe (crafting, cooking or Cooking Pot) forms a loop, and if two Cooking Pot recipes share the same ingredients.
- **Grass seeds:** short grass now drops one Jugcraft seed 12.5 % of the time, chosen evenly from all twelve crops. That is vanilla wheat seeds' rate, and it no longer grows as crops are added. With the Fall Harvest's 2 % per crop, twelve crops would have made grass drop a Jugcraft seed 24 % of the time.
- **Automation:** hoppers fill the pot from the top and sides and empty it from the bottom, and a comparator reads its result slots. There is no powered automation (planned for the engineering branch). The sickle harvests the new crops: it picks tomatoes and peppers and replants the others.

## Multiplayer and persistence
- All growth, planting, picking and cooking runs on the server. Planting on a trellis checks `mayBuild` and `mayInteract` (spawn protection and claims). The pot's screen syncs progress and heat through vanilla container data; clients send only vanilla slot clicks.
- Crops and trellises store everything in block states. The Cooking Pot has a block entity that saves its ten slots and its cooking progress (a save and restart has not been tested), and it drops its contents when broken.
- The trellis rule prevents duplication: a climbing crop grows only into trellis blocks it replaces, and each of its blocks drops exactly that trellis again.
- New IDs only. The only existing behaviour that changes is the short-grass seed drop (see Balance). The `agriculture` switch stops recipes (crafting and Cooking Pot), wild plants in new chunks and grass drops. Registered blocks and items stay, so saved gardens and pots survive.

## Dependencies and assets
No new dependencies. Uses Fabric API's menu, block entity, loot, biome modification and creative tab APIs, which are already required. Every texture is drawn by `tools/kitchen_textures.py` from fixed seeds; no Mojang texture is read, traced or recolored. The Cooking Pot screen reuses Jugcraft's own machine screen background.

## Verification
To be filled in from this pull request's CI run.

## World and event applicability
The wild plants follow each crop's real origin (tomatoes and peppers in warm biomes, garlic in woodland, cabbage on windy hills, grains in fields and dry hills). Patches appear in about 1 of 24 chunks of a matching biome, on grass only. There are no hostile or dangerous elements. Nothing is seasonal.
