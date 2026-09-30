# Festival Crops: gourds on stems, turnips, cranberry bogs and the chestnut tree

Status: implemented in source; **not yet played**. The Build workflow compiles it, and CI's game tests pass (details below).
Proposal issue: none; requested directly by the owner on 30 September 2026 ("start the next agriculture slice"), following slice 3 of the plan in [branches/AGRICULTURE.md](../branches/AGRICULTURE.md#crop-roster-what-comes-next-planned).
Owner: @jimbozoomer-byte
Target milestone and tier: Milestone 3 (first homestead); Discovery tier. The sawmill recipe for chestnut logs belongs to the machines (Workshops) and is optional.
Primary specialty and supported player role: farming and cooking; supports builders (a new wood set, gourd and lantern decorations), animal breeders and every player who eats

## Player experience
Grow the crops of autumn and winter festivals, all year round:

- **butternut squash, acorn squash and warty gourds** from stems on farmland, like pumpkins, for food and fall displays;
- **turnips**, and **Turnip Lanterns** carved from them (the original jack-o'-lantern);
- **cranberries**, a bog crop that stands in shallow water;
- **the chestnut tree**, grown from a chestnut, whose leaves ripen spiny burs you pick without cutting the tree down, and a chestnut wood set.

Foods: roasted chestnuts, baked acorn squash, squash pie, candy corn, and in the Cooking Pot, butternut squash soup, harvest stew and cranberry sauce. The player guide is the Festival Crops section of [branches/AGRICULTURE.md](../branches/AGRICULTURE.md#what-exists-now-the-festival-crops).

Nothing is seasonal: every crop, block and recipe is available all year and stays in the world. The planned Halloween and December events ([CONTENT_BRANCHES.md](../CONTENT_BRANCHES.md#seasonal-content)) may build on these crops; none of them depends on an event.

## Connections
- Existing input producer: the Overworld (gourd patches on grass in plains, savanna, forest, taiga, swamp and dark forest; wild turnips in taiga and birch forest; ripe cranberry bushes in swamp shallows; chestnut trees in forests; short grass everywhere), vanilla farmland, water and mud, torches, sugar, eggs, honey, carrots, raw mutton, bowls. Corn comes from the Fall Harvest, onions and garlic from the Kitchen Garden.
- Existing output consumer: food for every player; the Cooking Pot (three new dishes); animal breeding through vanilla tags (`pig_food`: turnips, chestnuts; `rabbit_food`: turnips; `fox_food`: cranberries; `chicken_food` and `parrot_food`: gourd seeds); composters; building (chestnut wood in vanilla's `planks`, `logs_that_burn`, `wooden_stairs`, `wooden_slabs`, `wooden_fences` and `fence_gates` tags, so it makes sticks, crafting tables and chests and burns as fuel); `c:` convention tags (`c:crops/*`, `c:seeds/*`, `c:foods/*`).
- Technology connection: optional. The **sawmill** saws a chestnut log into 6 planks with sawdust (hand crafting gives 4), like vanilla logs.
- Magic connection: none yet.
- Reachable entry path: a hoe, farmland and a seed from grass or the wild; for cranberries, a bucket of water over dirt; for the tree, a chestnut on dirt. No crop needs another branch, a machine or an event.
- Required vs optional: nothing is required. Every dish is extra food; the sawmill recipe is optional.
- How this stays useful without other branches: food, animal feed, decorations and a wood set.

## Balance and automation
- **Gourds:** stems grow like Jugcraft crops (vanilla soil formula, ×1.5 next to beans, no crowding penalty) at wheat's pace, then place a gourd beside them, like pumpkins. 1 gourd → 4 seeds, like a pumpkin. A stem keeps growing gourds; harvest is breaking the gourd.
- **Turnips** follow vanilla carrots (the planted item back, a binomial bonus when ripe).
- **Cranberries:** one stage in 5 random ticks with light 9 or more and air above (vanilla sweet berries' rate); picking gives 2–3 and resets to flowering. Breaking a bush gives its planting berry back, plus 1–2 when ripe.
- **Chestnuts:** tree leaves with air below ripen one bur each, a stage in 10 random ticks (about a Minecraft day to ripe); picking gives 1–2. Only leaves the tree grew bear fruit, so placed leaves cannot be farmed in a compact wall. Broken leaves drop a chestnut like vanilla leaves drop saplings (5–10 %) plus the ripe bur's nuts.
- **Food values** stay within vanilla's range (full table in the branch document): squash pie equals pumpkin pie, harvest stew equals rabbit stew with five ingredients, cranberries equal sweet berries, and raw chestnuts are not food.
- **No positive-gain loops:** gourds → seeds, chestnut logs → planks, and every other recipe turns crops into food. `tools/check_mod_data.py` fails if any agriculture recipe forms a loop.
- **Grass seeds:** short grass still drops one Jugcraft seed 12.5 % of the time; it is now chosen from eighteen crops.
- **Automation:** the sickle picks ripe cranberries and cuts gourds off their stems (not gourds a player set down). There is no powered automation (planned for the engineering branch). Wood burns as fuel (logs and planks like oak, slabs half as long).

## Multiplayer and persistence
- All growth, planting, picking and stripping runs on the server. Planting uses vanilla block items (with the usual build checks), and the sickle checks `mayInteract` for each block it touches.
- Everything stores its state in block states: stem age and direction, bush age, leaf fruit. There are no block entities and no saved data.
- A cranberry bush holds a water source like seagrass: breaking it leaves the water, and it cannot be used to create or remove water.
- New IDs only. The only existing behaviour that changes is which seeds short grass can drop (18 instead of 12, at the same overall rate). The `agriculture` switch stops recipes, wild plants, gourd and cranberry patches and chestnut trees in new chunks, and grass drops. Registered blocks and items stay, so saved fields, bogs and trees survive.

## Dependencies and assets
No new dependencies. Uses Fabric API's flammable-block registry (content registries), loot, biome modification and creative tab APIs, which are already required. Every texture is drawn by `tools/festival_textures.py` from fixed seeds; no Mojang texture is read, traced or recolored. Models reuse vanilla's model templates (cross, cube, stairs, slab, fence, fence gate) with Jugcraft textures; the stem models are rebuilt without vanilla's biome tint.

## Verification
Actual results (30 September 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions):

VERIFICATION

## World and event applicability
Gourd patches appear in about 1 of 32 chunks of a matching biome, on grass. Cranberry patches are tried in about 1 of 4 swamp chunks and only fill water exactly one block deep over bog soil. Chestnut trees appear in about 1 of 3 forest chunks. All only in chunks generated after this feature; grass drops cover older worlds. There are no hostile or dangerous elements, and nothing is seasonal.
