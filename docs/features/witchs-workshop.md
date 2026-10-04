# The Witch's Workshop

Status: implemented in source. Not yet played by hand. The Build workflow compiles it; CI's game tests and client screenshots are recorded below.
Proposal issue: none. The owner asked for it directly on 4 October 2026 ("Lets come up with another 20 very detailed well thought out additions to add for the halloween / fall season using the first 3 images for ideas of prop sets … lets do them piece by piece maybe 5 at a time"). These are prop sets 1–5 of the [Witching Season plan](witching-season.md), drawn from the reference pictures' cauldron, chandelier, broom and jars.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery tier for most sets; the Enchanted Broom needs Flying Ointment from the hex brews (a Bubbling Cauldron over a fire), and the Beating Heart Jar a brass nugget from the alloy smelter (Workshops tier).
Primary specialty and supported player role: building, with useful machines for farmers and brewers. These props furnish a witch's workshop: a brewing corner, candlelight, a broom closet, a collector's cabinet, a shelf of oddities.

## Player experience

### 1. The Horned Skull Cauldron
1. **Horned Skull Cauldron** (a cauldron, two iron ingots, a bone block and two goat horns; or two bones in place of the horns): a squat black-iron pot with a rounded, riveted belly on four clawed feet, and a bleached ram's skull bolted to its front, its horns curling out past the rim. It faces whoever places it.
2. **It holds potions as well as water.**
   - A water bucket fills it three deep, and a bucket takes a full pot of water back.
   - A potion pours in if the pot is empty or holds that same potion, up to three bottles.
   - A glass bottle draws a level back out, as the potion that went in.
   - What goes in comes out: nothing is gained or lost.
3. **The brew shows its colour.** The brew's surface is drawn in its potion's colour, and a potion glows: light 6 plus one a level, two more while heated. The skull's eye sockets glow in the same colour.
4. **It brews over heat.** Over a fire, campfire, magma, lava or an Ember Bed underneath:
   - the brew heaves and pops;
   - three translucent fume ribbons twist up out of it in the brew's colour, swaying and fading a block and a half up;
   - now and then a drop of brew spits over the rim.
5. **Waft it.** Stir a heated potion with the **Brew Ladle** (a bowl on two sticks). Up to four players within three blocks, the nearest first, each get its lasting effects for a quarter of their duration, and it uses one level. One bottle shared by four is still one bottle. Instant effects (healing, harming) don't waft.
6. **Things float in it.** Use one of a witch's ingredients on the brew to drop it in (the item tag `jugcraft:cauldron_floaters`):
   - from vanilla: apples, bones, spider eyes, mushrooms, roots and berries, eggs, feathers, slime, magma cream, ghast tears, nether wart, pufferfish, rabbits' feet, phantom membrane, amethyst shards and wither roses;
   - from Jugcraft: mandrake roots, garlic, acorns, chestnuts and cranberries.

   Up to three float half-sunk, bobbing and turning. An empty hand or the ladle fishes the last one out, and breaking the pot gives them back. Tools, weapons and containers never go in, so nothing is lost by a slip.
7. **Ember Bed** (four cobblestone round a coal or charcoal; two at a time): a block-high hearth of sooty fieldstones round glowing embers, with an arched draft hole in each side showing the fire (light 9).
   - It is a heat source for this cauldron, the Bubbling Cauldron and the Cooking Pot (`jugcraft:heat_sources`).
   - It spits sparks, smokes and crackles.
   - Like a magma block, it burns whoever walks on it without sneaking.

### 2. The wrought-iron candelabra
1. Four iron fittings (candles, iron nuggets and ingots), their candles drawn by the client:
   - **Floor Candelabrum** (two blocks tall): four scrolled feet, a twisted stem with knops, scroll brackets, and seven candles in two tiers and a crown;
   - **Table Candelabrum**: three candles on scrolled arms, standing on a block;
   - **Wall Girandole** (a tinted-glass looking-glass among its candles): three candles before a cast back-plate, a black mirror and a crest; it hangs on a wall;
   - **Branching Chandelier**: three tiers of arms reaching a block out on every side and down into the block below, sixteen candles, hung from its crown by chains. It hangs under a block or a chain and falls without it. It is different from the Haunted Chandelier's single ring.
2. **Six waxes**: use a dye to recolour every candle: ivory (white dye or bone meal), black, purple, green, orange or red.
3. **Four flames**: ordinary, soul (soul sand or soul soil), witchfire purple (an amethyst shard) or ghostfire green (a glow ink sac). Each flame flickers on its own, at full brightness.
4. **Lighting**:
   - flint and steel, a fire charge or a burning arrow lights them, and an empty hand snuffs them;
   - a redstone signal lights them while it lasts;
   - lit, they give light 9 (table, girandole), 14 (floor candelabrum, both halves) or 15 (chandelier).
5. **Wax drips** run down the candles while they burn and pool in their pans, in three stages over about three in-game days. Shears scrape them off and give nothing, so a burning candle is no wax farm.

### 3. The Enchanted Broom, Dustpan and Broom Rack
1. **Enchanted Broom** (a Witch's Broom, an amethyst shard and string): a besom of bound twigs on a crooked handle, an amethyst bound into it with a purple cord, standing on its bristles.
2. **Wake it with Flying Ointment** (the hex brew): rub it on and the bottle comes back. It wakes for three in-game days, and won't take more while still nearly full. Awake:
   - it sways and its bristle-tips glow;
   - once a second it looks for dropped items within four blocks (a box a block up and down);
   - it leans and swishes toward them, pushing up to sixteen at a time toward the nearest Dustpan in that box, which takes them in;
   - with no Dustpan, it sweeps them into a heap at its own feet.

   It moves only dropped items, and makes nothing.
3. **Dustpan** (an iron ingot and a copper ingot): a tin dustpan with a wooden handle.
   - It holds nine stacks; open it like a one-row chest, or empty it with a hopper or a pipe.
   - The heap of sweepings shows in the pan, and a comparator reads how full it is.
4. **Broom Rack** (three iron nuggets over three planks): a dark oak rail for a wall with three brass pegs. Each peg holds and shows one broom: Witch's Brooms, Enchanted Brooms or Flying Broomsticks (the item tag `jugcraft:brooms`). Use a peg with a broom to hang it and again to take it down.

### 4. The Cabinet of Curiosities
1. **Curiosity Cabinet** (dark oak planks, a gold nugget and glass panes; two blocks tall): a carved mahogany cabinet on bun feet, with three velvet-lined shelves behind two glazed doors and a crown with a carved moth-and-skull crest.
   - Aim at one of its nine places on the front and use any item to put one of it there; use it again to take the thing back, as with a chiseled bookshelf.
   - The doors swing open while you reach in, and a comparator reads how full it is.
2. **Bell Jar** (three glass and a wooden slab): a glass dome with a brass knob on a turned plinth. It shows one thing under glass, turning slowly once every twelve seconds.
3. **Moth Display Case** (an item frame, a glass pane and green dye): a shallow glazed case for a wall, holding a big moth and two smaller ones pinned over a hand-written label.
   - Use it to change the specimens: luna moths (pale green, eyespots, long tails), death's-head hawkmoths (with the skull on the thorax) or atlas moths (rust red with white windows).
   - **At night the moths come alive** and slowly open and close their wings.

### 5. The Oddity Jars
Squat glass jars on dark bases under iron lids, with paper labels:
1. **Jar of Eyeballs** (seven spider eyes, an iron nugget and glass): nine eyeballs in murky fluid. Each rolls to follow the nearest player within eight blocks, the later ones a little slower, so they never turn as one. With nobody near, they wander.
2. **Beating Heart Jar** (glass, an iron nugget, rotten flesh, redstone and a brass nugget): a heart on a brass stand with a little dial, in red fluid. **It is a redstone clock.**
   - It beats at 60, 80, 100 or 120 beats a minute; use it to change the tempo.
   - Each beat gives a two-tick signal of 15 to every side but the one below, and the heart swells with it.
   - A signal into its base stops it, and it starts again when that signal goes.
3. **Bat in a Jar** (glass, an iron nugget, phantom membrane and leather): a bat hanging asleep from the lid. When a player comes within three blocks it wakes with a flap, squeaks and flutters round the jar for five seconds, then settles. It wakes again for the next player to come close.
4. **Two-Headed Snake Jar** (glass, an iron nugget, string and a slime ball): a coiled snake in pale green fluid whose two heads sway this way and that. Use the jar and both heads flick their tongues and hiss.
5. **Hand in a Jar** (glass, an iron nugget, rotten flesh and a bone): a stitched hand floating in yellowed fluid, drumming its fingers one after another. While it has a redstone signal, it points at the nearest player.

## Changes from the plan
- The Branching Chandelier has sixteen candles, not thirteen: a candle in the middle of its crown would have stood in its own stem, so the top tier has four.
- Things are floated with a plain use, not sneaking (vanilla skips a block's own use while a player sneaks holding something), so what floats is a curated item tag rather than "any item".
- Recipes settled: two bones stand in for the goat horns, and the Jar of Eyeballs takes seven spider eyes (a crafting grid holds nine things in all).
- The Ember Bed is a full block, so the cauldron stands on its stones rather than floating over a low fire.

## Connections
- Existing input producer:
  - mob drops and vanilla materials: bone, iron, goat horns, spider eyes, rotten flesh, string, phantom membrane, leather, slime;
  - dyes and candles;
  - the hex brews' Flying Ointment, for the broom;
  - the alloy smelter's brass, for the heart jar;
  - the Witch's Broom.
- Existing output consumer:
  - brewers (wafting shares one bottle among friends; potions keep in the pot);
  - farmers and builders (the broom and Dustpan gather dropped items for hoppers and pipes);
  - redstone (the heart jar's clock, the cabinet's and bell jar's comparator signal);
  - building.
- Technology connection: redstone (the candelabra, the heart and the hand), hoppers and pipes (the Dustpan), brass. Magic connection: hex brews (the broom's ointment), potions (the cauldron).
- Reachable entry path: all but two sets need only early materials and a crafting table. The broom needs a Bubbling Cauldron over a fire, and the heart jar a brass nugget, both reachable and neither needing these props first.
- Required vs optional: everything is optional; nothing gates progression.

## Balance and automation
- **The cauldron**:
  - what goes in comes out: a bottle in, a bottle out of the same potion;
  - wafting divides one level among at most four players at a quarter duration each, so four players get one bottle's worth between them, no more;
  - instant effects don't waft;
  - floating things are kept and given back.
- **The candelabra**: drips are looks only and give nothing; shears clear them and are worn.
- **The broom**:
  - it only moves dropped items, at most sixteen a sweep, once a second, within four blocks; it makes nothing;
  - one Flying Ointment (three nights' brewing and a bottle) buys three in-game days;
  - asleep, it doesn't tick at all.
- **The heart jar** is a signal source and nothing more; its beats are scheduled ticks, one a beat.
- **The bat jar** looks for a player within three blocks every ten ticks, nearby only.
- **The client draws**: the eyes, the hand's pointing, the moths and the cabinet's doors are drawn by clients from what they already know. The server does nothing for them.
- **Recipes**: each takes materials in and gives the prop; breaking gives the prop back. The Ember Bed makes two from four cobblestone and a coal, worth less than it uses.

## Multiplayer and persistence
- **Server-side**: every change is decided on the server, through vanilla's paths:
  - filling, drawing, stirring and floating;
  - colouring, lighting and snuffing;
  - anointing, sweeping and racking;
  - placing things on show;
  - the heart's tempo.
- **Build rights**: changing a cauldron, a candelabrum or a moth case needs them (the player's build ability), so the lairs' protection will cover them later.
- **Saved**:
  - block state, and in block entities: the cauldron's potion and floating things, the broom's charge, the Dustpan's nine stacks, and the cabinet's, bell jar's and rack's things;
  - the cabinet's door swing and the snake's tongues are client animations, not saved.
- **Drops on breaking**: the cabinet, bell jar, rack, cauldron and Dustpan spill what they hold.
- **New IDs**:
  - blocks and items `horned_skull_cauldron`, `ember_bed`, `floor_candelabrum`, `table_candelabrum`, `wall_girandole`, `branching_chandelier`, `enchanted_broom`, `dustpan`, `broom_rack`, `curiosity_cabinet`, `bell_jar`, `moth_display_case`, `jar_of_eyeballs`, `beating_heart_jar`, `bat_in_a_jar`, `two_headed_snake_jar`, `hand_in_a_jar`;
  - the item `brew_ladle`;
  - block entity types `horned_skull_cauldron`, `candelabrum`, `enchanted_broom`, `dustpan`, `showcase`, `moth_display_case`, `oddity_jar`;
  - item tags `jugcraft:brooms` and `jugcraft:cauldron_floaters`;
  - their recipes.
- **Feature switch**: the recipes follow the agriculture feature switch.

## Dependencies and assets
- **No new dependencies.**
- **Models**: built on `tools/flora_art.py` by `tools/decor17_data.py`, which paints their textures by code (64 × 64, or 128 × 128 for the cauldron, candelabra, broom, cabinet and moth case).
- **The 128 × 128 rule**: the toolkit's texture size is now a choice. `docs/ART_DIRECTION.md` and the audit allow 128 × 128 for a sculpted prop that packs many pieces.
- **Client textures**: five small greyscale textures the client tints: brew, fume, wax, flame and glow.
- **Moving parts and layout**: the moving parts are in `assets/jugcraft/decor17_quads.json`, and the candelabra's candle layout in `/jugcraft/candelabra.json`, which Java reads.
- **Originality**: all original. The look follows the owner's reference pictures, and nothing is traced from them.

## Verification
Pending: CI's Build workflow (run 37217854334 on a6f125a0) is running the checks below; this section is updated with what it reports.
- `python3 scripts/check_repository.py` and `python3 tools/check_mod_data.py`: pass locally. The audit's new Witch's Workshop check:
  - compares the cauldron's, broom's, Dustpan's, rack's, cabinet's, bell jar's, jars' and candelabra's numbers, tempos, waxes and flames with `tools/decor17.py`;
  - checks the generated candle layout;
  - checks each block is registered, named, drops and has its recipe and texture;
  - checks that the Ember Bed is a heat source and the two item tags match;
  - checks that the quads, `DecorQuads` and all seven renderers are wired up.
- `WitchsWorkshopGameTests` (seven tests):
  1. the cauldron fills, empties, takes three of one potion and refuses a fourth or another, and gives the same potion back;
  2. over an Ember Bed it heats, and a ladle wafts long swiftness onto the four nearest of five players at a quarter duration, using one level (an instant potion doesn't waft);
  3. only ingredients float, three at most, an empty hand fishes one out, and breaking gives them back;
  4. the candelabra:
     - a table candelabrum takes purple wax and witchfire, lights at 9, snuffs, and follows redstone;
     - drips grow only while lit, and shears clear them giving nothing;
     - a floor candelabrum stands two tall and both halves light at 14;
     - a chandelier falls with its ceiling;
     - each fitting has 7, 3, 3 and 16 candles;
  5. Flying Ointment wakes the broom (the bottle comes back, a second refused), it sweeps apples by the Dustpan into it, and pushes a far bone toward it;
  6. the displays:
     - every one of the cabinet's nine places is picked by a hit on it, a skull goes onto the place used, a comparator reads it, and an empty hand takes it back;
     - the bell jar shows one thing;
     - the rack refuses an apple, hangs a broom on the peg used, and falls without its wall;
  7. the oddities:
     - the moth case changes its moths;
     - the heart's beat gives 15 to the sides but not below and ends after its pulse, its tempo changes with use, and it is stopped by power from below;
     - a player coming near wakes the bat;
     - the hand is powered by redstone;
  8. recipes and loot load.
- `WitchsWorkshopClientGameTests`: a dark oak workshop, by day and by candlelight: the whole room, the cauldrons close up, the cabinet with the moth cases and broom rack, the jars on their table, the broom and Dustpan, the candelabra at night, and the moths at night.

Not run:
- Building with them by hand.
- A two-player dedicated server (two players stirring one cauldron, both wafted).
- This environment can't run a game client interactively.

## World and event applicability
Placed by players only; no worldgen. They work all year, and nothing depends on the Halloween event.

## Rollout and open questions
- Open: whether the Enchanted Broom should also sweep into a hopper or a chest when there is no Dustpan.
- Open: whether wafting should also work for splash or lingering potions poured in, which it can't hold today.
