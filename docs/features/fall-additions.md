# Fall Additions

Status: the chandlery (addition 1) and the cider mill (addition 2) are implemented in source, not yet played. The Build workflow compiles it, and CI's game tests pass (recorded below).
Proposal issue: none; requested directly by the owner on 2 October 2026 ("ok lets build another 10 more thorough and well thought out festive halloween and fall additions, maybe for one we do candle making with an interesting process to make them allowing you to make a bunch of different combinations and then light them to give different cool effects to an aoe area like beacons do"). The ten additions ship one per pull request, each stacked on the one before:
1. the chandlery: the Wax Melting Pot and Aura Candles;
2. the cider mill: apple trees, the Cider Press, the Cider Barrel and four ciders;
3. a preserves pantry (planned);
4. crows and working scarecrows (planned);
5. spooky fireworks (planned);
6. a sky lantern festival (planned);
7. a harvest feast table (planned);
8. a corn maze (planned);
9. ghost hunting (planned; its ectoplasm is to become a candle scent);
10. face paint (planned).

Owner: @jimbozoomer-byte
Target milestone and tier: the chandlery is Discovery tier: copper ingots (the pot), honeycomb or rotten flesh (wax), string (wicks), dyes, and vanilla items an early player can gather (sugar, a rabbit's foot, a golden carrot, a feather, a pufferfish, magma cream, an amethyst shard, a ghast tear, a fermented spider eye, bone meal, a glow ink sac, glowstone dust and redstone). The ghast tear and magma cream are Nether items; every other scent is from the Overworld. The cider mill is Discovery tier too: planks, an iron ingot, a grindstone and wooden slabs (the press); a barrel, iron and gold nuggets and sticks (the barrel); apples (from oak leaves, and then apple trees), glass bottles, and for the extras sugar, sweet berries, cocoa beans, wheat and an egg.
Primary specialty and supported player role: crafting and support. Chandlers make candles for builders (light in any colour), farmers (the harvest aura), explorers and miners (night vision, water breathing, fire resistance, haste), and groups (one candle covers everyone near it). Candles are easy to trade: each one carries its own wax, colour, scents and burn time. The cider mill is for farmers and cooks: orchard keepers grow apples, and a cider maker presses them and ages the cider. Its drinks are for anyone (haste for miners, jump boost, absorption before a fight, regeneration after), and a barrel of aged cider is the centrepiece of a harvest party.

Nothing here depends on the Halloween event: candles are made and burned, and cider pressed and aged, all year.

## Player experience
### The Wax Melting Pot
1. A hammered copper pot with a rolled rim, a pouring lip and two handles (seven copper ingots, shaped like a cauldron). Set it over a heat source: a lit campfire, fire, soul fire, lava or a magma block (the same `jugcraft:heat_sources` the Cooking Pot uses).
2. **Put wax in:** honeycomb is beeswax (two measures a comb); rotten flesh renders down to tallow (one measure). A pot holds one wax at a time, up to eight measures. Over the heat a measure melts every five seconds, and the wax rises in the pot. Away from heat, molten wax sets again, a measure every fifteen seconds.
3. **Mix the molten wax:**
   - **Dyes** colour it. Several dyes mix as they do on leather (red and blue make purple), so any colour a leather tunic can take, a candle can.
   - **Scents** give the candle its aura. Two at most go in a pot; a third is refused, and so is one already in it. The eleven scents and what stirs them in are listed below.
   - **Glowstone dust** brightens it: a stronger aura (level II), but the candle burns twice as fast.
   - **Redstone** makes it burn half as long again.
   - Each of these lasts until the pot is empty, so one scent item flavours up to eight layers.
   Coloured scent wisps drift up from the pot, and the wax bubbles. Use it with an empty hand to see what is in it ("Beeswax: 6 of 8 measures molten, Swiftness, Moonlight, brightened"); sneak with an empty hand to pour it all away.
4. **Dip a candle.** Use string on the molten wax and it comes out as a one-layer candle of that wax, colour and mixture. That uses a measure.
5. **Dip it again to build it up,** but only once its last layer has cooled: two seconds, shown as a cooldown on the hotbar. Dipped while still warm, the new layer slides off and its wax is lost. Each layer:
   - makes the candle taller and its aura wider (5, 8, 12, then 16 blocks);
   - makes its light stronger (8, 10, 12, then 14);
   - adds its own burn time;
   - turns its outside the new wax's colour;
   - adds the new wax's scents to the ones the candle already carries.
   Four layers is as big as a candle gets.
6. **Combinations:** a candle carries every scent of every layer it was dipped in. Two scents work together; a third muddles it ("Muddled Beeswax Candle"): it lights, but gives no aura. So the art is in dipping different pots in the right order: a layer of swiftness wax under a layer of moonlight wax makes a candle of both. With 11 scents, 2 waxes, 16 dyes (and their mixes), brightening, extending and four sizes, there are thousands of candles to make.
7. A candle is named for what it carries ("Beeswax Candle of Swiftness and Moonlight", in the scents' colours). Its tooltip shows the aura's reach, how long it burns, its layers and whether it is brightened or long-burning.

### The scents
| Scent | Stirred in with | Aura |
| --- | --- | --- |
| Swiftness | sugar | players get Speed |
| Leaping | a rabbit's foot | players get Jump Boost |
| Moonlight | a golden carrot | players get Night Vision |
| Featherfall | a feather | players get Slow Falling |
| Tide | a pufferfish | players get Water Breathing |
| Ember | magma cream | players get Fire Resistance |
| Diligence | an amethyst shard | players get Haste |
| Mending | a ghast tear | players get Regeneration |
| Warding | a fermented spider eye | hostile mobs are slowed and weakened |
| Harvest | bone meal | growing plants get extra growth ticks |
| Revealing | a glow ink sac | other creatures glow, so they show through walls |

Each scent is an item tag (`jugcraft:candle_scents/<scent>`), so packs can add items to it.

### Aura Candles
8. Place a candle on any block with a solid top: it stands on a brass dish with a finger ring. Light it with flint and steel or a fire charge; use it with an empty hand to snuff it, keeping what is left.
9. **Lit, it works like a small beacon:**
   - Every four seconds its aura pulses over a box reaching its radius in every direction. Players inside get each scent's effect for nine seconds, so it never runs out while they stay.
   - A ring of the scents' colours shows the edge of the aura at each pulse, and their wisps rise from the flame.
   - The flame is tinted by the first scent (a warm yellow for an unscented or muddled candle), and a tallow candle smokes a little.
10. **It burns down as it burns:** it shrinks to a quarter of its height, then goes out for good in a puff of smoke. A layer of beeswax burns four minutes; a layer of tallow two. Brightening halves that; redstone makes it half as long again. A long-burning beeswax candle of four layers burns 24 minutes.
11. Broken, a candle drops itself as it is, part-burned, with its name, colour and scents, to be placed and lit again later. Burned out, it is gone.

### The apple tree
12. **Apple trees** grow wild in plains and flower-rich places (meadows, flower forests, sunflower plains): a short oak trunk under a rounded crown of apple leaves.
13. Their leaves (the ones the tree grew, with air below them) **blossom** white and pink and then **hang with ripe red apples**, about a Minecraft day from bare leaves to ripe. Use ripe leaves with an empty hand to pick them: 1 to 3 apples fall, and the leaves start again. Leaves a player placed never fruit.
14. **Apple seeds** plant an apple sapling on dirt or grass, which grows like any sapling (bone meal works). Broken apple leaves drop seeds now and then (as oak leaves drop saplings), sticks, and now and then an apple. The pomace from the press gives seeds too, so vanilla apples alone are enough to start an orchard.

### The Cider Press
15. A slatted oak basket bound with iron, on a trough, under a beam with an iron screw. Behind it is the grinder (the scratter): a drum under a hopper, with a crank. It faces you when placed.
16. **Grind:** put apples in the hopper, one at a time, up to eight. Use the press with an empty hand to **turn the crank**: an apple is ground into pulp in the basket, with a crunch and a splash. The crank turns once every 8 ticks, however fast anyone clicks.
17. **Press:** once the hopper is empty, an empty hand **turns the screw** instead.
    - The plate comes down on the pulp (the "cheese"), and each of four turns squeezes out a quarter of its juice into the trough, a serving for every apple.
    - The capstan bar on the beam turns a quarter turn each time, and the pulp squashes lower.
    - Nothing more goes in until the cheese is pressed out.
    - The last turn frees the screw and **knocks out the pomace** (one for every two apples).
18. **Bottle it:** a glass bottle draws a serving of juice from the trough: **Sweet Cider**. The trough holds eight servings; the screw won't turn while a turn's juice wouldn't fit. Sneak with an empty hand to see what is in the press. Comparators read the juice.

### The Cider Barrel
19. An oak cask on its side in a cradle, with iron hoops, a brass tap and a bung. Pour **Sweet Cider** in by the bottle (the bottles come back), up to sixteen servings.
20. **It ages by itself:**
    - A day after the last fresh serving went in, the batch is **Sparkling Cider** (it fizzes at the bung); three days after, **Aged Cider**.
    - A chalk mark on its head shows the stage (one stroke, two, three).
    - A batch that has begun to ferment takes no more fresh juice, so new juice can't be slipped into an old batch; topping up a sweet batch starts its day again.
21. A glass bottle draws off a serving of whatever it has become. Use it with an empty hand to see how much is in it and how long to the next stage. Comparators read how full it is.
22. Broken, a barrel keeps its cider, and the cider keeps ageing while it is carried.

### Ciders and what they make
| Drink | How it's made | Food | Effect |
| --- | --- | --- | --- |
| Sweet Cider | bottled from the press | 3 | Haste, 30 s |
| Sparkling Cider | a day in the barrel | 3 | Jump Boost, 60 s |
| Aged Cider | three days in the barrel | 4 | Absorption, 120 s |
| Mulled Cider | sparkling cider and mulling spices in the Cooking Pot | 6 | Regeneration, 15 s |

23. Every cider is drunk even on a full stomach and leaves its bottle.
24. **Mulling Spices:** sugar, sweet berries and cocoa beans make two.
25. **Apple Cider Donuts:** two wheat, sugar, an egg and a Sweet Cider make four cinnamon-sugar donuts (3 food each); the cider's bottle comes back. They count as candy, for Candy Bowls and Candy Bags.
26. **Apple Pomace** feeds pigs and goes in the composter; crafted, it gives apple seeds.

## Connections
- Existing input producer: vanilla copper (the pot), bees (honeycomb), zombies (rotten flesh), spiders (string), dyes, and the scent items above; Jugcraft's `jugcraft:heat_sources` (the Cooking Pot's heat).
- Existing output consumer:
  - Light in any colour and size, for builders.
  - Status effects for every player in range.
  - Growth for farms: the harvest aura speeds up every plant that takes random ticks and bone meal, including Jugcraft's own crops (corn and the other tall crops, gourds, cranberries and giant pumpkins).
  - Candles go back into the pot to be built up.
- Technology connection: none needed. The pot uses the same heat rule as the Cooking Pot, so any heat source added to that tag later heats both.
- Magic connection: none yet. The planned ghost hunting addition (9) is to add a scent made from ectoplasm, through the same `jugcraft:candle_scents/*` tags.
- Reachable entry path: copper, honeycomb or rotten flesh, string and a campfire are all early-game; no candle needs another candle or anything from later tiers. Tallow candles need nothing from bees.
- Required vs optional: all optional; nothing in progression needs a candle. Nothing here is gated by the Halloween event.
- Trade and solo routes: a solo player can make every candle; candles also stack (16) and carry everything they are, so a chandler can make them for others.
- How this stays useful without other branches: every scent's effect is useful on its own, and the harvest aura helps any farm.
- Cider mill, input producer: vanilla apples (oak leaves, villagers, chests) and then apple trees; the Cooking Pot (mulled cider) with its heat sources; wheat, sugar, eggs, sweet berries and cocoa beans; glass bottles.
- Cider mill, output consumer:
  - drinks for everyone;
  - donuts for Candy Bowls and Candy Bags (the existing `c:foods/candy` tag);
  - pomace for pigs (the vanilla `minecraft:pig_food` tag) and composters;
  - seeds for orchards.
- Cider mill, entry path: apples drop from vanilla oak leaves, and a pressed apple's pomace gives apple seeds, so the first orchard needs no wild apple tree. The press and barrel are crafted from vanilla materials.
- Cider mill and the plans:
  - [AGRICULTURE.md](../branches/AGRICULTURE.md) plans orchards (slice 4) and a Fruit and Seed Press (apples into cider, grapes into juice, seed oil for engineers).
  - The apple tree is the first orchard tree, on the chestnut tree's pattern (now a shared `FruitingLeavesBlock`).
  - The Cider Press is the apple half of that press. Grapes and seed oil are not built.

## Balance and automation
- No energy. A pot costs seven copper ingots. A candle costs one string and one measure of wax a layer: half a honeycomb of beeswax, or one rotten flesh of tallow. A dye, a scent, a glowstone dust or a redstone is used once per pot of wax, however many layers that wax makes (up to eight).
- Units: wax in **measures** (a pot holds 8); burn time in ticks (20 a second).

| | Beeswax layer | Tallow layer |
| --- | --- | --- |
| Measures from one item | 2 (a honeycomb) | 1 (a rotten flesh) |
| Burn time | 4,800 ticks (4 min) | 2,400 ticks (2 min) |
| Brightened (×0.5) | 2,400 ticks | 1,200 ticks |
| Long-burning (×1.5) | 7,200 ticks | 3,600 ticks |
| Both | 3,600 ticks | 1,800 ticks |

- A candle's burn time is the sum of its layers' times, so a candle of mixed layers burns for each layer's own time.
- Radius and light by layers: 5, 8, 12 and 16 blocks; light 8, 10, 12 and 14.
- Melting: a measure every 100 ticks over heat; setting: a measure every 300 ticks without heat; cooling between dips: 40 ticks.
- **Effects:**
  - Every 80 ticks, a lit candle gives players in range 180 ticks of each scent's effect: level I, or level II when brightened.
  - Warding gives hostile mobs Slowness (level II when bright) and Weakness I.
  - Revealing gives other creatures Glowing.
  - None of these stack between candles: two candles of the same scent only refresh the same effect.
  - Compared with a beacon, a candle reaches less far (16 blocks at most against a beacon's 20 to 50), lasts minutes rather than for ever, and costs wax and string each time. In return it needs no pyramid and gives two effects at level II from one candle.
- **Harvest:**
  - Each pulse gives radius² ÷ 4 random ticks (twice as many when bright) to random spots within the radius and two blocks above or below the candle. A spot is ticked only if it holds a plant that takes bone meal and random ticks.
  - That is 6, 16, 36 or 64 a pulse by size, spread over the area. Each plant in range gets roughly a fifth more growth ticks than the world gives it (about two fifths when bright), at any size of candle.
  - Several harvest candles add up, each burning its own wax.
- **No positive-gain loop.**
  - A candle never turns back into wax, scents or anything else; burned out, it is gone. The pot makes nothing but candles.
  - The harvest aura only speeds growth that bone meal could give anyway: vanilla already lets crops be composted into bone meal. A candle spends wax and string to spread that over time, and gives no item of its own.
- Automation: candles are dipped by hand (a player's use and cooldown), so the pot can't be automated. Lit candles need no attention until they burn out.

- **Cider mill:**
  - Costs:
    - the press: four planks, an iron ingot, a grindstone and three wooden slabs;
    - the barrel: a barrel, two iron nuggets, a gold nugget and two sticks.
  - Units: juice and cider in **servings** (a bottle each). An apple gives one serving of juice, and the press a pomace for every two apples (rounded up). The trough holds 8 servings, the barrel 16.
  - Time:
    - a crank or screw turn every 8 ticks at most; a cheese takes 4 turns;
    - fruit: a stage every 10 random ticks on average, two stages (about a Minecraft day), 1 to 3 apples a pick;
    - the barrel: sparkling at 24,000 ticks (a day, 20 minutes) after the last fresh serving, aged at 72,000 (three days, an hour).
  - Food:
    - a raw apple is 4 food; its serving of Sweet Cider is 3 (with 30 s of Haste) and half a pomace;
    - ageing adds food (aged cider 4) and a better effect for time, not material.
  - Effects are level I. Aged Cider's two minutes of Absorption match a golden apple's Absorption (without its Regeneration). A barrel makes sixteen, but only after three days and only from apples. This is open to balance review in play.
  - **Bottles are never made from nothing:**
    - the press turns a bottle into a cider;
    - the barrel gives a bottle back for each serving poured in and takes one for each drawn off;
    - every cider leaves its bottle when drunk.
    - Only Sweet Cider gives its bottle back in a recipe (the donuts). Sparkling Cider doesn't, because the Cooking Pot hands back crafting remainders and the mulled cider keeps that bottle.
    - The checker refuses a drink that gives its bottle back and cooks into another bottled drink.
  - **No positive-gain loop.** Nothing turns back into apples. Pomace makes one seed; seeds make trees, which need land and time as any orchard does.
  - Automation: grinding, pressing and bottling are by hand (a player's use); a barrel ages by itself. Comparators read both.

## Multiplayer and persistence
- **Server authority:**
  - Putting things in the pot, dipping, pouring, lighting and snuffing all go through vanilla's block use path (reach, spawn protection, adventure mode) and are decided on the server.
  - The server checks the wax kind and the pot's room, that the wax is molten, the two-scent limit, and whether brightener or extender is already in.
  - The cooldown ("still warm") is the server's own record of the player's item cooldowns. A client can't dip faster, or keep a warm layer, by claiming otherwise.
- **The aura is worked out on the server** at each pulse, from the candle's saved mixture, over a bounded box. Clients are sent the candle's mixture (to draw its colour, height and flame) and a block event for the ring; they decide nothing.
- **Concurrent use:** two players at one pot each take their own measure; the pot's room and molten count are checked on every use. The cooldown is shared by all of a player's Aura Candles (it is per item), so a player dips one candle at a time.
- **Persistence:**
  - The pot saves its wax, set and molten measures, melting progress, dye sums, scents, brightener and extender.
  - A candle saves its mixture (`mix`: wax, layers, colour, scents, bright, lasting, burn and burned) and keeps it as the item component `jugcraft:candle` when it is broken, along with its colour and name.
  - A pot's wax is lost when the pot is broken.
- **Chunk unload:** a candle in an unloaded chunk doesn't tick, so it neither burns nor pulses until the chunk loads again. No chunk loading.
- **Bounded work:**
  - An unlit candle doesn't tick.
  - A lit one counts down each tick and pulses every 80 ticks: one entity query over at most a 33-block box, plus at most 128 block lookups for a bright harvest candle.
  - A pot ticks only while it holds wax.
- **IDs:**
  - Blocks with items: `jugcraft:wax_melting_pot`, `jugcraft:aura_candle`.
  - Block entities of the same names.
  - The data component `jugcraft:candle`.
  - Item tags: `jugcraft:candle_wax/beeswax`, `jugcraft:candle_wax/tallow`, `jugcraft:candle_scents/<scent>` (eleven), `jugcraft:candle_brighteners`, `jugcraft:candle_extenders`.
  - All are new; nothing earlier is renamed.
- **Disable behaviour:** with the agriculture feature disabled the pot's recipe doesn't load; the blocks, items, component and block entities stay registered, so placed pots and candles stay in the world.

- **Cider mill, server authority:**
  - Putting apples in, cranking, turning the screw, bottling, filling and drawing the barrel go through vanilla's block use path (reach, spawn protection, adventure mode) and are decided on the server.
  - The press checks room, that it isn't mid-pressing, that the trough has room for the turn, and its own pacing. The pacing is per press, not per player: two players can't grind faster than one.
  - Picking apples goes through the same path, as the chestnut's does.
- **Cider mill, persistence:**
  - The press saves its apples, pulp, turns, cheese and juice, and sends them to clients for the renderer (they decide nothing).
  - The barrel saves its servings and the game time its batch started; a broken barrel keeps both as the item component `jugcraft:barrel_cider`.
  - Ageing is worked out from the world's game time, which every dimension shares. So a barrel ages while unloaded or carried, and needs no ticking; it only looks every 20 ticks to change its chalk mark (one comparison).
- **Cider mill, IDs:**
  - blocks with items `apple_leaves`, `cider_press` and `cider_barrel`; the block `apple_sapling` (planted from `apple_seeds`);
  - items `apple_seeds`, `apple_pomace`, `sweet_cider`, `sparkling_cider`, `aged_cider`, `mulled_cider`, `mulling_spices` and `apple_cider_donut`;
  - block entities `cider_press` and `cider_barrel`; the data component `jugcraft:barrel_cider`;
  - item tags `jugcraft:cider_apples` and `c:seeds/apple`;
  - worldgen feature `jugcraft:apple_tree` and placed feature `jugcraft:patch_apple_tree`;
  - the Cooking Pot recipe `jugcraft:pot_cooking/mulled_cider`.
  - All are new. The chestnut leaves' fruiting moved into the shared `FruitingLeavesBlock` with no change of ID, state or behaviour (the festival tests still check it).
- **Cider mill, disable behaviour:** with the agriculture feature off, the recipes don't load and wild apple trees don't generate; the blocks, items, component and block entities stay registered.

## Dependencies and assets
No new dependencies. Every texture is drawn by code (`tools/chandlery_textures.py`): the pot's hammered copper and dark inside, the brass dish, the wax (pale, tinted by its colour as it is drawn), the wax's surface in the pot, the flame (white at its heart, tinted by its scent) and the candle's item in two layers (its body, tinted by its dyed colour; its wick and dish, not). The models, blockstates, item model, names, tooltip, messages, loot and tags come from `tools/chandlery_data.py`; the numbers from `CHANDLERY` in `tools/agriculture.py`. The client's `WaxPotRenderer` draws the wax in the pot at its level and colour; `AuraCandleRenderer` draws the candle at its height, layers and colour and its flame; both share `TintedBoxes`. The item's colour is the vanilla `dyed_color` component, read by the item model's dye tint. Sounds are vanilla's (honeycomb waxing, dye use, brewing, a bottle filling, a honey slide, a bucket emptying, a candle going out).

The cider mill:
- Textures are drawn by code in `tools/cider_textures.py`: the leaves at each stage, the sapling, the press's oak, slats and iron, the barrel's staves along and across, its head with each chalk mark, its brass, the pulp, juice and apple skin for the renderer, and the items. The bottles share the treats' bottle shape.
- The models, blockstates, loot, tags, worldgen and words come from `tools/cider_data.py`; the numbers from `CIDER` in `tools/agriculture.py`.
- The client's `CiderPressRenderer` draws the press's apples, pulp, plate, screw, capstan bar and juice (with `TintedBoxes`). The barrel is a block model with a variant for each chalk mark.
- Sounds are vanilla's (an item frame's click, a grindstone, a trapdoor's creak, a bucket, bottles).

## Verification
### Chandlery verification

Actual results (2 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/agriculture-halloween-decor-15` stacked on batch 14:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares the chandlery's numbers, waxes and scents with Java: capacity, timings, scent limit, burn factors, layers, pulse and effect time, harvest divisor, radii and light by layers, each wax's measures, burn and colour, each scent's effect and colour; checks every wax and scent tag, and the brightener and extender tags, hold `tools/agriculture.py`'s items; that the candle's blockstate has a variant for every state; and that every pot message, candle tooltip, candle name, wax and scent has its words) | Pass, 581 IDs |
| `./gradlew build` on `1cdcf09` (Build workflow run 37021364812) | Pass on its second attempt (see the next row) |
| Game tests on the headless server, same run: 336 in total, 8 of them new here (`ChandleryGameTests`) | **All 336 pass** on the second attempt. The first attempt failed one test from `main`, `PetroGameTests.heliostatsHeatASolarReceiver` ("The receiver made 48 JE/t, expected 36 on tick 25"), which this branch doesn't touch; the same tests had all passed on `f56071b` (run 37020041265), and the failed job was re-run once |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `1cdcf09`, with the screenshots above. No model or texture errors for the chandlery in the log |

The 8 new game tests (`ChandleryGameTests`):
1. honeycomb melts over a campfire and stays set on stone; a pot of beeswax takes no tallow;
2. molten wax takes red, then blue dye (and mixes them), two scents but not a third, glowstone dust and redstone once each;
3. string starts a scented one-layer candle and uses a measure; dipped again while warm, the layer slides off and its wax is lost; a cool candle gains a layer and its burn time adds up; layering a third scent muddles a candle;
4. a pulse gives players within five blocks both effects and a player six blocks off none; a bright candle gives Speed II; a muddled candle gives nothing;
5. warding slows and weakens a zombie; revealing makes the zombie glow, but not a player;
6. a bright harvest candle's pulses grow the wheat round it;
7. flint and steel lights a candle (light 8 for one layer); a lit candle burns down and goes out for good, its block entity with it; broken part-burned, a candle drops itself with its mixture and burn;
8. the pot's recipe and both loot tables load.

Found by CI and fixed before this record:
- 26.3's `LivingEntity.drop` takes a third argument; a candle that doesn't fit in the inventory pops out of the pot instead (`e61fa32`).
- 26.3 has no `RenderTypes.entityCutoutNoCull`; the renderers use `entityCutout`, wind each face to face along its normal, and draw a flame's plane from both sides (`7ee4afb`).
- 26.3's entity types are constants of `EntityTypes`, not `EntityType` (`5935bc2`).
- A test swapped the glowstone dust in hand for redstone before counting it (a test bug, not the pot's); each is now checked while it is held (`f56071b`).
- The first screenshots showed the flames as thin slivers in the scent's colour, lost against wax of the same colour; they are now about twice the size with a warm-white heart (`1cdcf09`).

**Not run:**
- a person playing it in a client: dipping by hand, watching the cooldown, walking in and out of an aura;
- a dedicated server with two players at one pot, or in one aura;
- how long a harvest candle takes to grow a real farm (the test drives 400 pulses at once);
- the sounds (the CI client has no sound device).

### Cider mill verification

Actual results (2 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/agriculture-halloween-decor-16` stacked on the chandlery:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares the cider mill with Java: the apple leaves' fruiting and picking, the press's capacity, trough, turns, pacing and pomace, the barrel's capacity, ageing times and stages; checks the apple tag, the press's, barrel's and leaves' block states, the barrel's chalk-mark states, every message, the barrel's loot keeping its cider, the apple tree's biomes and that apple seeds plant a known sapling; and refuses a drink that gives its bottle back but cooks into another bottled drink) | Pass, 593 IDs |
| `./gradlew build` on `c01a4a2` (Build workflow run 37025266440) | Pass |
| Game tests on the headless server, same run: 345 in total, 9 of them new here (`CiderGameTests`) | **All 345 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `c01a4a2`, with the screenshots in [AGRICULTURE.md](../branches/AGRICULTURE.md#the-cider-mill). No model or texture errors for the cider mill in the log. Like every Jugcraft machine and Cooking Pot recipe, the mulled cider recipe is logged as one the vanilla recipe book can't place (the recipe book only knows vanilla recipe types) |

The 9 new game tests (`CiderGameTests`):
1. an apple sapling grows a tree: an oak trunk and a crown of apple leaves;
2. tree-grown leaves blossom and ripen; placed leaves don't; picking drops apples and leaves bare leaves;
3. eight apples go in and not a ninth; a crank turn grinds one, a second at once does nothing, and once free the crank grinds another;
4. four screw turns press a full cheese: a quarter of the juice each, no apples in while pressing, four pomace knocked out, the screw free again; a bottle draws sweet cider; comparators read the juice;
5. a cheese of three apples gives exactly three servings and two pomace;
6. sweet cider goes into a barrel and the bottles come back; after a day it is sparkling (the chalk mark follows) and takes no fresh juice; a bottle draws sparkling cider; after three days it is aged; broken, it keeps its servings and start time;
7. topping up a sweet batch starts its day again;
8. every cider is drunk on a full stomach, leaves its bottle and gives its effect; sweet cider gives its bottle back in a recipe and sparkling cider doesn't;
9. the recipes (mulled cider in the Cooking Pot among them), loot tables and the apple tree's placed feature load, and sparkling cider with spices finds the mulled cider recipe.

Found by CI and fixed before this record:
- The press started with its last work at `Long.MIN_VALUE`, so "now minus then" overflowed and a new press never turned its crank. The first run's `pressGrindsApplesIntoPulp` caught it (344 of 345 passed); it now starts a work interval in the past (`3d1751a`).
- The first screenshots showed the barrels as square crates; three stepped boxes now round their profile (`c01a4a2`).

**Not run (cider mill):**
- a person playing it in a client: grinding and pressing by hand, drinking;
- a dedicated server with two players at one press;
- a real day of ageing (the tests set the barrel's start time back);
- wild apple trees in a new world (the test grows one from a sapling, and checks the placed feature loads);
- the sounds.

## World and event applicability
- Candles and pots work anywhere, in every dimension, all year. Nothing is seasonal. The aura doesn't depend on biome, time or weather; harvest helps only plants that would grow there anyway.
- Revealing shows creatures through walls (Glowing), which can help find hostile mobs in caves; it gives no other information.

- Wild apple trees grow in plains and flower-rich biomes (`c:is_plains`, `c:is_floral`), in new chunks only, about one patch in twelve chunks there.
- A barrel ages by game time, so sleeping through the night doesn't age it (sleeping skips the time of day, not game time).

## Rollout and open questions
- The aura's area is a box reaching the radius in every direction, up and down too (a beacon's reaches the whole height of the world).
- Warding works on monsters (zombies, skeletons, creepers, spiders, endermen, witches and the like). Hostile mobs that aren't monsters to the game, such as slimes, magma cubes, phantoms, ghasts, shulkers and hoglins, aren't warded.
- Effects are given to players only, not to tamed animals or villagers.
- Mending gives Regeneration II when bright, to everyone in up to a 33-block box, for as long as the candle burns. That is stronger than a beacon's Regeneration I for a short while and is open to balance review in play.
- The "still warm" cooldown is the item cooldown, so it shows on every Aura Candle in the hotbar, not only the one just dipped.
- A dipped candle goes to the hand that held it if that hand is empty, else into the inventory, else it pops out of the top of the pot.
- A pot's wax is lost when the pot is broken; the pot drops itself.
- Apple leaves take random ticks but not bone meal, so a harvest candle's aura doesn't speed their fruiting.
- The press can't be fed or emptied by hoppers or pipes yet; grinding, pressing and bottling are by hand.
- The press's pacing is per press: a second player at the same press waits as the first does.
- A barrel holds one batch; a fermenting batch takes no fresh juice, so a cider maker uses several barrels.
- Grapes, juices and the seed oil of the planned Fruit and Seed Press are not built.
