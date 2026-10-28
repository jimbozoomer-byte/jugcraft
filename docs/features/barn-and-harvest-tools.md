# Barn & Harvest Tools

Status: parts 1 (the Steel Sickle, the Watering Can and the Seed Pouch), 2 (the Quern and the Butter Churn, with Flour, Butter and Pancakes) and 3 (the Drying Rack and the Corn Crib) implemented. Later parts are planned: the hay-loft pulley and the harvest bell; the barn's sacks, cans, racks, trough and lantern. CI builds it and runs its game tests and client screenshots; not yet played by hand.
Proposal issue: none. The owner asked for it directly on 28 October 2026, as set 3 of the ten sets of fall and Halloween decor ("Then I want you to do 1-10 in parts in that order one by one"), drawn by their rules.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery tier for the Watering Can, the Seed Pouch, the Quern, the Butter Churn, the Drying Rack and the Corn Crib; Workshops tier for the Steel Sickle (steel).
Primary specialty and supported player role: farming and cooking. Part 1 builds the hand tools the [Agriculture branch's farm equipment table](../branches/AGRICULTURE.md#farm-equipment-planned-not-industrial) planned: a grower waters, sows and reaps a field a square at a time. Part 2 builds its quern and churn, the first of the planned mill and dairy: a farmhouse grinds its wheat and churns its milk by hand.

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

### Part 2: the mill and the dairy
- **Quern**: a pair of hand millstones, 44 cm across, in a flour tray on a wooden stand 69 cm high. The upper stone (the runner) has its eye in the middle and a turned peg near its rim; flour leaves the tray by a spout at the front.
  - **Fill it** with wheat or dried chili: use it on the quern. Its eye holds up to **16** of one kind, shown heaped there. Another kind waits till the first is ground or taken out; the quern says so.
  - **Grind**: use it with an empty hand to turn the upper stone once round; it turns, easing in and out, over half a second, and the next turn waits for it. Every **two** turns grind one piece, which falls out of the spout:
    - wheat into **Flour**, one for one;
    - a dried chili into **two Paprika**, as the Mortar and Pestle grinds it.
  - Sneak-use with an empty hand to take the grain back. Broken, it spills its grain.
  - Recipe: a stick over three smooth stone slabs, over two planks.
- **Butter Churn**: a stave dash churn 38 cm across and 78 cm to its lid, with two iron hoops and a lid with a cleat; the dasher's handle stands up through the lid.
  - **Fill it** with a bucket of milk (the bucket comes back). It holds one; a second is refused.
  - **Churn**: use it with an empty hand to work the dasher down and back up over half a second; the next stroke waits for it. The **eighth** stroke churns the milk into **two Butter**, which fall out at its front.
  - Broken with milk in it, the milk is spilt: the bucket already came back, so nothing else comes out.
  - Recipe: a stick over planks round an iron nugget, over three planks.
- **Flour**: makes Wheat Dough as wheat does (three flour and a bucket of water make three).
- **Butter**: an ingredient.
- **Pancakes** (food 5, saturation 0.6, as bread): two flour, an egg and a butter make two.

### Part 3: drying and keeping the harvest
- **Drying Rack** (one block across, two up): a freestanding frame 1.9 m tall, two uprights on splayed feet joined by a lower rail and a top rail, with an iron hook under each rail at each end.
  - **Hang a bunch**: use herb sprigs (`#jugcraft:herbs`) or peppers on it. Up to **4** go on the first free hook, so it holds four bunches. Anything else does what it would on any block.
  - Each bunch shows on its hook: herbs tied head down, their leaves spreading; peppers strung on a string as a ristra, by turns either side of it.
  - **Drying**: a bunch dries in **2,400 ticks** (two minutes), whether anyone is near or not, and turns its dried colours. Herbs become **Dried Herbs** and peppers **Dried Chili**, one for one.
  - **Take one down**: use it with an empty hand. The first dried bunch comes down, dried; if none has dried, the last one hung comes back fresh.
  - Broken, it gives back each bunch, dried if it has dried.
  - Recipe: string over sticks, a frame of seven sticks.
- **Corn Crib** (two blocks across, two up, one deep): a slatted crib 2 m long on four short posts, under a corrugated tin gable roof with a ridge cap.
  - It holds **256** ears of corn (`#c:crops/corn`), four stacks. Use corn on any of its blocks to put in all you hold that fits, and an empty hand to take back the last stack, as the Apple Bin does.
  - The ears show through the gaps between its slats, rising through **four stages** as it fills.
  - It takes nothing but corn. Broken, it spills its corn.
  - Recipe: three wooden slabs over a chest between two sticks, over two planks.

## Changes from the idea list
- The idea list put a Bushel Basket in set 3. The soil slice's Wooden and Bamboo Baskets (`tools/soil.py`) are small produce storage already, so it is not built again.
- The planned tools were in the Agriculture branch's farm equipment table; they are built here as set 3's first part, so the barn has its tools before its furniture.
- The farm equipment table put the churn in the Workshops tier with a Cheese Press, from a husbandry slice. The churn is built here at the Discovery tier, from planks and an iron nugget, on vanilla milk; the cheese press is not built.
- The Drying Rack came in part 3, with the Corn Crib, rather than with the quern and churn.
- The farm equipment table's Drying Rack also retted flax and made jerky. This one dries herbs and peppers, the crops that have dried forms already; flax retting and jerky wait for their items.

## How they are drawn (docs/ART_DIRECTION.md, docs/ITEM_ICONS.md)
- All three are flat items whose icons are 16 by 16 maps in `tools/item_icons/` (`steel_sickle.txt`, `watering_can.txt`, `seed_pouch.txt`), drawn by `tools/item_icons.py` in the shared material palettes and checked by `tools/check_icon_maps.py`.
  - **Steel Sickle**: lies on the diagonal as the flint and bronze sickles do. A wooden handle at the lower left meets a broad crescent of steel hooking up and over to its point, lit along its upper, outer edge.
  - **Watering Can**: upright and seen a little from above. Its round tinplate body is lit at the upper left, the handle arches over its back, and the spout rises from its front to the sprinkler rose.
  - **Seed Pouch**: a round canvas pouch lit from the upper left, gathered at its neck by a leather cord whose two ends hang loose.
- One colour stepped into tones for each material, an outline that always touches a fill, no noise.
- **Part 2's foods** are maps too (`flour.txt`, `butter.txt`, `pancakes.txt`). Two materials were added to `tools/icon_materials.py` for them, both flat (no shine):
  - **butter**: a pale cream yellow, kept far enough from gold that it doesn't read as metal;
  - **pancake**: a golden brown.
  - **Flour**: a burlap sack, its mouth rolled open and heaped with white flour.
  - **Butter**: a block seen a little from above, its top lit.
  - **Pancakes**: a stack of three with a pat of butter on the top one.
- **The Quern and the Butter Churn** (`tools/barn_tools_data.py`) are built from boxes at real size, one texel to a model pixel, on the market displays' wood and iron.
  - Round things are drawn as a circle is in pixels: the stones are three boxes each (bands 3 by 7, 5 by 5 and 7 by 3), the churn two (4 by 6 and 6 by 4). `separated` keeps their coincident faces apart.
  - The stones are a grey-brown grit: their sides lit along the top and shaded along the bottom, their faces one flat tone so the bands read as one face. The runner's eye is the darkest tone at its middle.
  - The churn's staves are three-texel boards; its hoops stand a quarter pixel proud of them.
  - **Moving parts move.** Each block's model leaves its moving part out. The moving part is its own model, the blockstate's `runner` variant, which `HandWorkedRenderer` draws turned to the block's facing:
    - the quern's runner and peg turn once round the stone's middle in a turn, easing in and out;
    - the churn's dasher goes down 5 pixels and back up in a stroke.
    - Nothing ticks: the renderer works from when the last stroke began, which the block entity tells clients.
- **Part 3:**
  - **The Drying Rack** is built whole in its frame and cut into its two blocks (`pumpkin_patch_data.cut`). Each bunch is its own model, moved into the block whose rail it hangs from and added by the multipart blockstate.
    - Herbs are strands one texel wide in two tones by turns, darker under the string tie. Fresh herbs are green; dried, an olive grey-green.
    - The ristras are five chilies, each a two-pixel flat-toned box, red fresh and a dark brick red dried, on a string.
  - **The Corn Crib** is weathered grey wood: horizontal slats 1.5 pixels high with a pixel's gap between, corner posts and a stepped gable.
    - Its roof is corrugated tin, in stripes along the slope. Each side is two halves, one a block, turned 22.5 degrees up about its eave (just inside the frame, so the cut gives each half to its block) to meet the other under a tin ridge cap.
    - The ears inside are a box at each stage, painted as ears heaped lengthwise: rows of ears two texels high, six long with a dark texel between, each row half an ear along from the last, the kernels a two-tone checker lit along each ear's top.
- Nothing is copied, traced or recoloured from Minecraft's textures.

## Connections
- Existing input producer: tin and steel ingots (the shared `c:ingots` tags); a bucket; wool and string. The pouch takes any seed in the shared `c:seeds` tag that plants a block, Jugcraft's and other mods' alike.
  - Part 2: wheat; the spice garden's Dried Chili; vanilla milk and eggs; planks, smooth stone slabs and an iron nugget.
  - Part 3: the herb garden's sprigs and the kitchen garden's peppers; corn from the Fall Harvest; sticks, string, slabs, planks and a chest.
- Existing output consumer: the fields. The can's water is the Gourd Canteen's, at sixteen pours instead of three, and the pouch plants what the hoe prepared; the steel sickle reaps what they grew.
  - Part 2: Flour makes the kitchen's Wheat Dough (dumplings and the rest); Paprika is the spice garden's; Flour and Butter make Pancakes, a food.
  - Part 3: the rack's Dried Herbs and Dried Chili are the spice garden's (Dried Chili grinds into paprika at the quern or the mortar). The crib's corn goes back out as it came, for kernels, cooking and feed.
- Technology/magic connections: the Steel Sickle is the Workshops-tier sickle, made from the machines' steel.
- Reachable entry path: the can needs tin and a bucket; the pouch wool and string; both from the first days. The steel sickle comes with steel.
- Which connections are required vs optional; trade and solo routes: all optional; each is solo-craftable, and a sickle, can or pouch can be traded like any item.
- How this specialty stays useful without mastering every other branch: they speed up farming; everything they do can be done by hand.

## Balance and automation
- **The can** makes nothing. Wetting farmland is what water nearby does already. Its growth chance is one in six for each crop, at most nine crops a pour, and at most one step of growth a crop a pour; the crop's own growth rules apply (light, its growth odds). The 10-tick cooldown means a full can takes at least eight seconds to empty, and it must then be refilled.
- **The pouch** moves seeds; it makes, grows and doubles nothing. Each block sown spends one seed, as planting it by hand does. It holds four stacks, less than a bundle or a chest.
- **The steel sickle** is a bigger square of what the bronze one does. It harvests only ripe crops, replants from their own drops, and gives no extra yield.
- **The quern** grinds wheat into flour one for one and a chili into two paprika (the mortar's ratio). Flour makes dough exactly as wheat does, so it is no richer than wheat. A turn waits for the last, so a quern grinds at most one piece a second.
- **The churn** turns a bucket of milk into two butter in eight strokes (at least four seconds), giving the bucket back. Breaking it with milk in gives back nothing more, so it can't make buckets.
- **Pancakes** are bread's food value from two wheat, an egg and half a bucket of milk: no better than bread for the wheat, a way to use the dairy.
- **The rack** dries one for one, as the Herb Bundle (without its string) and the furnace (without its fuel) already do, but slower: two minutes a bunch, four bunches at a time.
- **The crib** keeps corn and gives back exactly what went in; it makes and changes nothing. It holds four stacks where a chest holds 27: its value is showing the harvest.
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
- Part 2: the quern keeps its grain and turns (`QuernBlockEntity`), the churn whether it holds milk and its strokes (`ButterChurnBlockEntity`), saved with the world.
  - Both are changed on the server only.
  - The quern shows its grain as a block-state property (`loaded`), the churn its milk (`full`). Each tells clients when its last stroke began, and nothing else, for drawing the moving part.
  - A stroke's rate is held on the server: a use before the last stroke is done does nothing.
  - Pistons can't move them.
- Part 3: the rack keeps its four bunches and when each was hung (`DryingRackBlockEntity`, on its first block), saved with the world.
  - Whether a bunch has dried is worked out from the game time, so nothing ticks. Hanging one schedules a single block tick for when it dries, which shows it dry (and schedules the next, if another is still drying).
  - Each block shows its rail's two bunches as block-state properties (`left`, `right`: empty, herbs, dried herbs, chili, dried chili), so clients draw them from the block states.
  - The crib's corn is kept as the Apple Bin's fruit is, by a `FruitStoreBlockEntity` of the crib's own type (`corn_crib`). The fruit store now asks its block what it takes, the Cider Press's fruit unless the block keeps something else, so the crib takes only corn.
  - Both are changed on the server only, break whole from any block, drop once, and can't be moved by pistons.
- IDs (stable from release): `steel_sickle`, `watering_can`, `seed_pouch`; the data component `watering_can_water`; part 2's blocks `quern` and `butter_churn` (and their block entities) and items `flour`, `butter`, `pancakes`; part 3's blocks `drying_rack` and `corn_crib` (and their block entities).
- The recipes follow the agriculture feature switch (and the can's the tin switch, the sickle's the machines switch). The items stay registered when it is off.

## Dependencies and assets
- No new dependency. Item models and lang are generated by `tools/barn_tools.py`, `tools/barn_tools_data.py` and (the sickle) `tools/agriculture.py`. The icons are drawn from their maps by `tools/generate_textures.py`.

## Verification
Local checks run on this branch, and their results:
- `python3 tools/check_mod_data.py`: PASS (8,080 material IDs, from 8,077), including the art check and the new `check_barn_tools`.
  - Part 2: PASS (8,085 material IDs). Its first run failed twice, both fixed before commit:
    - the quern's and churn's items had no names (their blocks were not in agriculture's block list);
    - the churn's recipe named a tag the audit doesn't know (`#c:nuggets/iron`); it now takes the iron nugget itself.
  - Part 3: PASS (8,087 material IDs). Its first run failed on my own check: `check_barn_tools` asked every barn block for a moving part's model, and only the quern and churn have one. The generator's first run also stopped on a path built wrongly for the crib's stage models. Both were fixed before commit.
- `python3 scripts/check_repository.py`: PASS.
- `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
  - Part 2's first run failed `test_survival_recipes`, fixed before commit: flour, butter and pancakes had no recorded source. The quern and churn are now native routes in `tools/survival_native_followup.py`.
  - Part 3: 287 tests, OK.
- `python3 tools/check_icon_maps.py`: PASS (the three new maps among 269; no new warning).
  - Part 2: PASS (272 maps, 51 declared materials with the two new ones; no new warning).
  - Part 3 adds no icon map (the rack and the crib are 3D items).
- `javac` parse (syntax only) of the new and changed Java: OK. The full compile runs in CI.
- CI's first run failed to compile (`SeedPouchItem.java:82`): 26.3's `LivingEntity.drop` takes a `Prediction`. Emptying the pouch now uses `Inventory.placeItemBackInInventory(stack, Prediction.SERVER_ONLY)`, as the Farm Stand does.
- CI's second run (`optional integrations absent`) failed one game test, `thePouchGathersSowsAndEmpties`: the field was sown but no seed was spent. The pouch asked `isCreative()`, which the test's mock player always answers yes; it now asks `getAbilities().instabuild`, which follows the game mode, as the rest of the mod does.
- The three icons were drawn and looked at at 1x and 10x while drawing (a review aid, not committed).
- Part 2's models were previewed offline, textured, from two sides and from above, with the runner and its wheat; the food icons at 1x and 10x (review aids, not committed). The first preview showed the stones' bands lit as separate plates and the staves striping hard; the stone faces are now one tone and the staves three texels wide.
- Part 3's models were previewed the same way: the rack with each kind of bunch, and the crib at each stage, also put back together from its four cut blocks. The previews showed the crib's roof halves turned the wrong way, and then dropped by the cut because they turned about a line outside the frame. The roof now turns the other way, about its eaves just inside the frame.

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

- **Part 2:**
  - **The quern**:
    - a stick isn't taken, and sixteen of twenty wheat go in and show;
    - a second turn at once is refused, and two turns grind one wheat, which falls out as a flour;
    - a dried chili gives two paprika;
    - sneaking, an empty hand takes the grain back;
    - broken, it spills its wheat and drops itself.
  - **The churn**:
    - the milk goes in and the bucket comes back, and a second bucket isn't taken;
    - a stroke at once after the last is refused, and the eighth stroke churns two butter;
    - broken with milk in, it drops only itself.

- **Part 3:**
  - **The rack**:
    - it stands whole, two blocks up, and a stick doesn't hang;
    - a bunch of four peppers and one of sage hang and show on the lower rail, and a tick is scheduled to show them dry;
    - before they dry, an empty hand takes the sage back down, fresh;
    - the peppers dry in their time and come down as four dried chilies;
    - broken, it gives back the peppers on it and drops once.
  - **The crib**:
    - it stands whole (four blocks), and apples don't go in;
    - it takes corn up to its 256 and shows itself full on every block;
    - an empty hand takes a stack back;
    - broken from its top block, it spills the rest and drops once.

`BarnToolsClientGameTests` takes three screenshots:
- two querns on a plank floor, one with wheat in its eye, and two churns, one holding milk, from the front and close by (the pictures show the renderer drawing the moving parts);
- the drying rack hung with peppers and sage, and the corn crib half full.

Not run: a hand playtest, and a two-client playtest. The can's growth chance is random and is not asserted by a test. The moving parts' motion is not caught in a still picture.

## World and event applicability
Items only; no world generation. Not seasonal: they are crafted and used all year.

## Rollout and open questions
- Part 4: a hay-loft pulley and hook, and a Harvest Bell on a post.
- Part 5: grain sacks, milk cans, a pitchfork and rake rack, a feed trough and a barn lantern on a hook.
- Open: whether hoppers should feed the quern and take its flour, and fill and empty the crib.
- Open: whether a dispenser should pour the can or sow from the pouch.
