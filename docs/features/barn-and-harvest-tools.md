# Barn & Harvest Tools

Status: part 1 (the Steel Sickle, the Watering Can and the Seed Pouch) implemented. Parts 2 to 4 (the quern, drying rack and butter churn; the corn crib, hay-loft pulley and harvest bell; the barn's sacks, cans, racks, trough and lantern) are planned. CI builds it and runs its game tests; not yet played by hand.
Proposal issue: none. The owner asked for it directly on 28 October 2026, as set 3 of the ten sets of fall and Halloween decor ("Then I want you to do 1-10 in parts in that order one by one"), drawn by their rules.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery tier for the Watering Can and the Seed Pouch; Workshops tier for the Steel Sickle (steel).
Primary specialty and supported player role: farming. Part 1 builds the hand tools the [Agriculture branch's farm equipment table](../branches/AGRICULTURE.md#farm-equipment-planned-not-industrial) planned: a grower waters, sows and reaps a field a square at a time.

## Player experience

### Part 1: the planned hand tools
- **Steel Sickle**: the third sickle, beside the flint (3 by 3) and bronze (5 by 5) ones. Right-click a crop to harvest every ripe crop in the **7 by 7** square round it, one block up or down, replanting one-block crops from their own drops, as the other sickles do.
  - 900 uses; each use that harvests anything costs one.
  - Recipe: the sickle pattern with three steel ingots and a stick (steel comes with the machines; the recipe follows that switch).
- **Watering Can**: a tinplate watering can that holds **16 pours**.
  - **Fill it** at any water source: use it while looking at the water.
  - **Pour** on farmland, or on a crop growing on farmland: every farmland in the 3 by 3 square round it is wetted (moisture 7). Each crop on that farmland gets one chance in six of growing a step, as a random moment of growth would.
  - Each pour spends one, and the can waits half a second (10 ticks) before the next.
  - The bar under its icon shows the water left, and its tooltip the pours.
  - Recipe: four tin ingots round a bucket.
- **Seed Pouch**: a wool drawstring pouch that holds up to **four kinds of seed**, a stack of each.
  - **Use it in the air** to gather the seeds you carry into it: anything tagged `c:seeds` that plants a block. Other items stay where they are.
  - **Use it on farmland** (or a crop on farmland) to sow every empty farmland in the 3 by 3 square round it, one seed a block, from its first kind first. A seed is spent only where it plants.
  - **Sneak-use it in the air** to empty it back into your inventory; what doesn't fit drops at your feet.
  - Its tooltip shows how many kinds and seeds it holds.
  - Recipe: a string over five wool.

## Changes from the idea list
- The idea list put a Bushel Basket in set 3. The soil slice's Wooden and Bamboo Baskets (`tools/soil.py`) are small produce storage already, so it is not built again.
- The planned tools were in the Agriculture branch's farm equipment table; they are built here as set 3's first part, so the barn has its tools before its furniture.

## How they are drawn (docs/ART_DIRECTION.md, docs/ITEM_ICONS.md)
- All three are flat items whose icons are 16 by 16 maps in `tools/item_icons/` (`steel_sickle.txt`, `watering_can.txt`, `seed_pouch.txt`), drawn by `tools/item_icons.py` in the shared material palettes and checked by `tools/check_icon_maps.py`.
  - **Steel Sickle**: lies on the diagonal as the flint and bronze sickles do. A wooden handle at the lower left meets a broad crescent of steel hooking up and over to its point, lit along its upper, outer edge.
  - **Watering Can**: upright and seen a little from above. Its round tinplate body is lit at the upper left, the handle arches over its back, and the spout rises from its front to the sprinkler rose.
  - **Seed Pouch**: a round canvas pouch lit from the upper left, gathered at its neck by a leather cord whose two ends hang loose.
- One colour stepped into tones for each material, an outline that always touches a fill, no noise.
- Nothing is copied, traced or recoloured from Minecraft's textures.

## Connections
- Existing input producer: tin and steel ingots (the shared `c:ingots` tags); a bucket; wool and string. The pouch takes any seed in the shared `c:seeds` tag that plants a block, Jugcraft's and other mods' alike.
- Existing output consumer: the fields. The can's water is the Gourd Canteen's, at sixteen pours instead of three, and the pouch plants what the hoe prepared; the steel sickle reaps what they grew.
- Technology/magic connections: the Steel Sickle is the Workshops-tier sickle, made from the machines' steel.
- Reachable entry path: the can needs tin and a bucket; the pouch wool and string; both from the first days. The steel sickle comes with steel.
- Which connections are required vs optional; trade and solo routes: all optional; each is solo-craftable, and a sickle, can or pouch can be traded like any item.
- How this specialty stays useful without mastering every other branch: they speed up farming; everything they do can be done by hand.

## Balance and automation
- **The can** makes nothing. Wetting farmland is what water nearby does already. Its growth chance is one in six for each crop, at most nine crops a pour, and at most one step of growth a crop a pour; the crop's own growth rules apply (light, its growth odds). The 10-tick cooldown means a full can takes at least eight seconds to empty, and it must then be refilled.
- **The pouch** moves seeds; it makes, grows and doubles nothing. Each block sown spends one seed, as planting it by hand does. It holds four stacks, less than a bundle or a chest.
- **The steel sickle** is a bigger square of what the bronze one does. It harvests only ripe crops, replants from their own drops, and gives no extra yield.
- No new conversion and no loop: no recipe turns any of these back into their materials.

## Multiplayer and persistence
- Every change happens on the server.
  - The can's water and the pouch's seeds are item components. They are saved with the item and synced by the server.
  - The can's water is bounded by its capacity in its codec.
  - The pouch uses the vanilla container component.
- Each farmland the can wets, each block the pouch sows and each crop the sickle reaps is checked against the player's right to build there (`mayInteract`), and the block used on with `mayUseItemAt`.
- The pouch gathers from and empties into the server's own inventory; the client only guesses whether to swing an arm.
- The can's cooldown is the vanilla item cooldown, checked by the server before any use.
- Per-use work is bounded: nine blocks for the can and the pouch, the sickle's 7 by 7 by 3.
- IDs (stable from release): `steel_sickle`, `watering_can`, `seed_pouch`; the data component `watering_can_water`.
- The recipes follow the agriculture feature switch (and the can's the tin switch, the sickle's the machines switch). The items stay registered when it is off.

## Dependencies and assets
- No new dependency. Item models and lang are generated by `tools/barn_tools.py`, `tools/barn_tools_data.py` and (the sickle) `tools/agriculture.py`. The icons are drawn from their maps by `tools/generate_textures.py`.

## Verification
Local checks run on this branch, and their results:
- `python3 tools/check_mod_data.py`: PASS (8,080 material IDs, from 8,077), including the art check and the new `check_barn_tools`.
- `python3 scripts/check_repository.py`: PASS.
- `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
- `python3 tools/check_icon_maps.py`: PASS (the three new maps among 269; no new warning).
- `javac` parse (syntax only) of the new and changed Java: OK. The full compile runs in CI.
- CI's first run failed to compile (`SeedPouchItem.java:82`): 26.3's `LivingEntity.drop` takes a `Prediction`. Emptying the pouch now uses `Inventory.placeItemBackInInventory(stack, Prediction.SERVER_ONLY)`, as the Farm Stand does.
- The three icons were drawn and looked at at 1x and 10x while drawing (a review aid, not committed).

Game tests (CI only, not run locally), `BarnToolsGameTests`:
- **The can** fills at a water source the player looks down at, to 16:
  - a pour on the middle of a 5 by 5 field wets the 3 by 3 round it and leaves the ring outside dry;
  - a pour spends one and starts the cooldown, and another use while it waits pours nothing;
  - an empty can doesn't pour;
  - a pour on a crop wets the farmland under it.
- **The pouch** gathers 10 flax seeds and 5 beans from the inventory and leaves the dirt:
  - used on a 3 by 3 field with one bean crop growing, it sows flax on the other eight and spends eight seeds;
  - on the sown field it does nothing;
  - sneak-used, it empties its 2 flax seeds and 5 beans back.
- **The Steel Sickle** reaps and replants a row of ripe beans 3 blocks to each side of the one clicked, and leaves the one 4 away; it wears 1.

Not run: a hand playtest, and a two-client playtest. The can's growth chance is random and is not asserted by a test.

## World and event applicability
Items only; no world generation. Not seasonal: they are crafted and used all year.

## Rollout and open questions
- Part 2: the Quern, the Drying Rack and the Butter Churn.
- Part 3: the Corn Crib, a hay-loft pulley and hook, and a Harvest Bell on a post.
- Part 4: grain sacks, milk cans, a pitchfork and rake rack, a feed trough and a barn lantern on a hook.
- Open: whether a dispenser should pour the can or sow from the pouch.
