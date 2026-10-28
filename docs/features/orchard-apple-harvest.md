# Orchard & Apple Harvest

Status: part 1 (the Orchard Ladder, the Apple Bin and the Picking Bag) implemented. CI builds it and runs its game tests and client screenshots; not yet played by hand.
Proposal issue: none. The owner asked for it directly on 28 October 2026, as set 2 of the ten sets of fall and Halloween decor ("Then I want you to do 1-10 in parts in that order one by one"), drawn by their rules.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery tier. Everything is crafted from planks, sticks, slabs, wool, string and an iron nugget.
Primary specialty and supported player role: farming, with decor for builders. A grower picking an orchard gets a ladder to reach it, a bag to pick into and a bin to store the harvest beside the Cider Press.

## Player experience

### Part 1: the ladder, the bin and the bag
- **Orchard Ladder** (one block across, three up, two deep): a three-legged orchard ladder about nine feet tall.
  - Two rails taper from 87 cm apart at the foot to 37 cm at the top, stepping in a pixel at a time, with flat steps every 30 cm and iron shoes on their feet.
  - A cap joins the rails' tops. A single pole is hinged there with an iron strap and leans back 22.5 degrees to the ground behind.
  - **Climb it** like a vanilla ladder. Its rails stand towards the back of its front blocks, so a player against them is inside the ladder's blocks, which are climbable.
  - Placed and broken as one, facing whoever places it; it drops once.
- **Apple Bin** (two blocks across and two deep, one up): an orchard's bulk bin, 1.2 m square and 0.76 m deep, on a pallet base.
  - The pallet has three skids under five deck boards. Four corner posts hold four slats a side with a gap between each, round a board floor.
  - It holds **256** apples and pears (four stacks).
  - The heap inside rises through **four stages** as it fills, showing whichever fruit went in last: red apples or gold-green pears.
- **Picking Bag**: a canvas picking bag hung by its harness on an iron wall hook.
  - Its foot is reinforced, and its stiff iron hoop mouth stands open at the top.
  - It holds **16** fruit, showing them heaped in its mouth: empty, some, or full.
  - It hangs only on a solid wall and comes off when the wall goes.

**Using the bin and the bag:**
- Use apples or pears (the Cider Press's fruit, `#jugcraft:cider_apples`) on any block of the bin, or on the bag, to put in everything you hold that fits. When it is full, it says so and takes no more.
- Use it with an empty hand to take back the last stack put in (or what is left of it).
- Anything else in your hand does what it would on any block.
- Breaking it spills what it held.

**Recipes:**
- **Orchard Ladder:** six sticks and planks (the sticks as two rails, the planks between).
- **Apple Bin:** four wooden slabs and three planks.
- **Picking Bag:** two string and an iron nugget over three wool.

### Still to come
- Part 2: stacks of apple crates (red, green and golden), fallen apples on the ground, and an orchard handcart.
- Part 3: the Caramel Apple Stand, with a dipping pot and a rack of apples on sticks, animated.

## Changes from the idea list
- The ladder is one block across, three up and two deep. A pole leaning 22.5 degrees (the only lean a block model can give a box, short of 45) from a nine-foot cap reaches the ground two blocks back.
- The bin and the bag hold the Cider Press's fruit, apples and pears both, so a bin of picked fruit feeds the press by hand.

## How they are drawn (docs/ART_DIRECTION.md)
- **Real-life sizes.**
  - The ladder is 45 pixels (2.8 m) tall.
  - The bin is 19 pixels square and 14 tall on its pallet.
  - The bag is 7 pixels (45 cm) wide and 8 tall, hung from its hook.
- **Built whole, then cut into its blocks.** Each prop is modelled whole in its frame and cut into its blocks' models (`pumpkin_patch_data.cut`). Each piece of a box cut at a block boundary keeps its share of the picture.
- **The pole** is built of three turned boxes, one per block it crosses, each turned about its own foot so it lies in that block. They meet end to end along one straight line.
- **The taper** steps the rails in a pixel every nine pixels up, as a line is drawn in pixels; each length overlaps the next.
- **Hollows:** the bin and the bag are walls round their floors with their insides drawn.
- **Fruit:** the heap is a separate set of boxes for each stage and fruit, drawn inside the walls.
- **Textures:** one texel to a model pixel, vanilla's density.
  - Wood and iron are the Pumpkin Patch Market displays' (`tools/pumpkin_patch_data.py`): boards in flat tones lit along their top, grain lines one shade off and placed nail heads. The ladder's rails and pole carry their grain along their length.
  - Canvas is one flat tone, lit along its top row and shaded along its bottom one.
  - Fruit is heaped as a staggered lattice of round fruit four texels across. Each is one flat tone with a single lit texel at its top left and its lower right edge a tone darker, every other one shows its stalk, and the gaps between are in shadow.
  - No outline, no gradient, no random shade at any texel.
- Nothing is copied, traced or recoloured from Minecraft's textures; the apples are drawn here, not taken from the apple item.

## Connections
- Existing input producer: the apple tree and the Orchards' pear tree (`#jugcraft:cider_apples`, the Cider Press's tag).
- Existing output consumer: the Cider Press, by hand from the bin; the ladder reaches the fruiting leaves of the orchard trees.
- Technology/magic connections: none; farm gear and decor.
- Reachable entry path: crafted from the first day from wood, wool, string and an iron nugget; nothing gates them.
- Which connections are required vs optional; trade and solo routes: all optional and solo-craftable.
- How this specialty stays useful without mastering every other branch: they are storage and decor on their own.

## Balance and automation
- The bin and the bag hold fruit and give back exactly what went in. They make, grow and change nothing.
- The bin holds four stacks in a two-by-two footprint (a chest holds 27). Its value is that it shows the harvest, not that it stores densely.
- No hopper or pipe connection yet: fruit goes in and out by hand.

## Multiplayer and persistence
- The bin's first block and the bag keep their fruit (`FruitStoreBlockEntity`), saved with the world.
- Fruit is put in and taken out on the server only, and only `#jugcraft:cider_apples` is taken.
- The fill and the fruit on top are block-state properties (`fill`, `fruit`), set on every block of the bin, so clients draw them from the block states. The block entity sends nothing.
- Breaking a bin from any block takes the whole down and spills its fruit once. Pistons can't move the ladder or the bin (`PushReaction.IMMOVEABLE`); a piston pops the bag off.
- Climbable is a block tag, so every block of the ladder climbs, including the two behind its rails that the pole crosses.
- IDs (stable from release): `orchard_ladder`, `apple_bin`, `picking_bag`; block entity `fruit_store`.
- The recipes follow the agriculture feature switch; the blocks stay registered when it is off.

## Dependencies and assets
- No new dependency. Models and textures are generated by `tools/orchard_harvest.py` and `tools/orchard_harvest_data.py`.

## Verification
Local checks run on this branch, and their results:
- `python3 tools/check_mod_data.py`: PASS (8,058 material IDs). It includes the art check over every block and item model (part 1's 6 ladder, 36 bin and 5 bag block models and 3 item models among them; no allow-list entry added) and the new `check_orchard_harvest`.
- `python3 scripts/check_repository.py`: PASS.
- `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
- `javac` parse (syntax only) of the new and changed Java: OK. The full compile runs in CI.
- Offline previews, textured and drawn with back faces culled, looked at while drawing (a review aid, not committed):
  - the three models;
  - the ladder's and bin's cut part models put back together;
  - the fruit heap's texture.

Game tests (CI only, not run locally), `OrchardHarvestGameTests`:
- **The ladder** stands whole (six blocks), facing its maker. Every block is climbable and has its piece of the shape, with room in front of the rails. Use does nothing to it. Broken at its top, it all goes and drops once.
- **The bin** stands whole (four blocks) and keeps its fruit on its first block, up to 256:
  - a stack of apples through its far block fills it a quarter, shown on every block;
  - pears go in on top and show;
  - a stick isn't taken;
  - an empty hand takes the pears back;
  - four stacks fill it, and then it takes no more;
  - it remembers what it holds through a save;
  - broken, it drops once and spills 256 apples.
- **The bag** won't hang without a wall, and hangs facing out from one:
  - it takes the 16 of 20 apples that fit and shows full;
  - an empty hand takes them back;
  - five show it partly full;
  - without its wall it comes off, spilling them.

`OrchardHarvestClientGameTests` takes two screenshots: an apple tree with the ladder against it, a full apple bin, a half-full pear bin and the bag on the trunk, from the front and the side.

Not run: a hand playtest, and a two-client playtest.

## World and event applicability
Placed blocks only; no world generation. Not seasonal: they are crafted and used all year.

## Rollout and open questions
- Parts 2 and 3 follow on their own branches.
- Open: whether hoppers should fill and empty the bin (it would make it a dense apple buffer for an automated cider line).
