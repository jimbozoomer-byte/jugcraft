# Fall Additions

Status: the chandlery (addition 1) is implemented in source, not yet played. The Build workflow compiles it, and CI's game tests pass (recorded below).
Proposal issue: none; requested directly by the owner on 2 October 2026 ("ok lets build another 10 more thorough and well thought out festive halloween and fall additions, maybe for one we do candle making with an interesting process to make them allowing you to make a bunch of different combinations and then light them to give different cool effects to an aoe area like beacons do"). The ten additions ship one per pull request, each stacked on the one before:
1. the chandlery: the Wax Melting Pot and Aura Candles (this record, so far);
2. a cider mill (planned);
3. a preserves pantry (planned);
4. crows and working scarecrows (planned);
5. spooky fireworks (planned);
6. a sky lantern festival (planned);
7. a harvest feast table (planned);
8. a corn maze (planned);
9. ghost hunting (planned; its ectoplasm is to become a candle scent);
10. face paint (planned).

Owner: @jimbozoomer-byte
Target milestone and tier: the chandlery is Discovery tier: copper ingots (the pot), honeycomb or rotten flesh (wax), string (wicks), dyes, and vanilla items an early player can gather (sugar, a rabbit's foot, a golden carrot, a feather, a pufferfish, magma cream, an amethyst shard, a ghast tear, a fermented spider eye, bone meal, a glow ink sac, glowstone dust and redstone). The ghast tear and magma cream are Nether items; every other scent is from the Overworld.
Primary specialty and supported player role: crafting and support. Chandlers make candles for builders (light in any colour), farmers (the harvest aura), explorers and miners (night vision, water breathing, fire resistance, haste), and groups (one candle covers everyone near it). Candles are easy to trade: each one carries its own wax, colour, scents and burn time.

Nothing here depends on the Halloween event: candles are made and burned all year.

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

## Dependencies and assets
No new dependencies. Every texture is drawn by code (`tools/chandlery_textures.py`): the pot's hammered copper and dark inside, the brass dish, the wax (pale, tinted by its colour as it is drawn), the wax's surface in the pot, the flame (white at its heart, tinted by its scent) and the candle's item in two layers (its body, tinted by its dyed colour; its wick and dish, not). The models, blockstates, item model, names, tooltip, messages, loot and tags come from `tools/chandlery_data.py`; the numbers from `CHANDLERY` in `tools/agriculture.py`. The client's `WaxPotRenderer` draws the wax in the pot at its level and colour; `AuraCandleRenderer` draws the candle at its height, layers and colour and its flame; both share `TintedBoxes`. The item's colour is the vanilla `dyed_color` component, read by the item model's dye tint. Sounds are vanilla's (honeycomb waxing, dye use, brewing, a bottle filling, a honey slide, a bucket emptying, a candle going out).

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

## World and event applicability
- Candles and pots work anywhere, in every dimension, all year. Nothing is seasonal. The aura doesn't depend on biome, time or weather; harvest helps only plants that would grow there anyway.
- Revealing shows creatures through walls (Glowing), which can help find hostile mobs in caves; it gives no other information.

## Rollout and open questions
- The aura's area is a box reaching the radius in every direction, up and down too (a beacon's reaches the whole height of the world).
- Warding works on monsters (zombies, skeletons, creepers, spiders, endermen, witches and the like). Hostile mobs that aren't monsters to the game, such as slimes, magma cubes, phantoms, ghasts, shulkers and hoglins, aren't warded.
- Effects are given to players only, not to tamed animals or villagers.
- Mending gives Regeneration II when bright, to everyone in up to a 33-block box, for as long as the candle burns. That is stronger than a beacon's Regeneration I for a short while and is open to balance review in play.
- The "still warm" cooldown is the item cooldown, so it shows on every Aura Candle in the hotbar, not only the one just dipped.
- A dipped candle goes to the hand that held it if that hand is empty, else into the inventory, else it pops out of the top of the pot.
- A pot's wax is lost when the pot is broken; the pot drops itself.
