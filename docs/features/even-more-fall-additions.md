# Even More Fall Additions

Status: hex brews (addition 21), the flying broomstick (addition 22) and full-moon werewolves (addition 23) are implemented in source and pass CI's game tests. None is yet played by hand. Additions 24 to 30 are planned and not yet built. The Build workflow compiles what exists; CI's game tests and client screenshots are recorded below.
Proposal issue: none. The owner asked for these directly on 3 October 2026 ("Lets do even more fun fall and halloween stuff 10 well thought out good additions"). They follow the [more fall additions](more-fall-additions.md) and are numbered on from them. Each one is its own pull request, stacked on the one before, after the graveyard pack:
21. hex brews: the Bubbling Cauldron's brews, bubbling over a fire, take a hex ingredient and become the Shrinking Draught, the Giant's Draught or Flying Ointment;
22. the flying broomstick: a witch's broom anointed with Flying Ointment, ridden and steered by looking;
23. full-moon werewolves: they come out of the woods only on full-moon nights; wolfsbane wards them off and silver hurts them;
24. squirrels and acorns (planned);
25. the Pumpkling, a pet that wears your carved pumpkin's face (planned);
26. the fall fair midway: high striker, ring toss and plush prizes (planned);
27. the ghost-train dark ride (planned);
28. the piñata (planned);
29. the hot-air balloon fiesta (planned);
30. the leaf blower (planned).

Owner: @jimbozoomer-byte
Target milestone and tier: hex brews are Discovery tier. You need:
- the Bubbling Cauldron (a cauldron and two iron ingots);
- a water bucket and a brew ingredient;
- a heat source: a lit campfire, fire, magma or lava;
- the hex ingredients: a brown mushroom, beans (the Kitchen Garden) or a phantom membrane;
- glass bottles.

Every hex can be made without leaving the Overworld:
- the green brew comes from spider eyes or slime balls;
- the orange brew comes from the glowstone dust that witches drop;
- the purple brew comes from amethyst shards.

Werewolves are Discovery tier: they come to any player in Overworld woods on a full-moon night. Wolfsbane grows wild in taiga and forests; the silver dagger is a silver ingot and a stick, and silver arrows a silver nugget and four arrows (silver is mined from the first days).

The flying broomstick is Discovery tier too: the Witch's Broom (two sticks and a wheat), one Flying Ointment and two feathers.

Primary specialty and supported player role: witchcraft and exploration. A witch brews hexes for others:
- **Shrinking:** gets a player into one-block gaps, under low ceilings and into cramped caves, and makes them a smaller target.
- **Giant:** gives a block more reach and half a block more step, for building and climbing, but makes the player a bigger target.
- **Flying ointment:** gives a safe fall from a cliff or a tower.

Draughts stack to 16 and keep, so a witch can trade them.

Werewolves are for fighters and hunters: a monthly danger in the woods, beaten by planning (wolfsbane about the homestead, silver in hand), and a reward in pelts. They give silver, until now only a cable metal, a use of its own.

The flying broomstick is a witch's way to travel: slower than elytra but found long before them, good for scouting, crossing ravines and reaching rooftops, and fuelled by the ointment a witch brews.

None of this depends on the Halloween event: a cauldron brews hexes and a broom flies all year, and werewolves come on every full moon.

## Player experience
### Hex brews
1. Fill a **Bubbling Cauldron** with water and turn it to a brew (green, purple or orange), as before.
2. Set it **over a heat source** (the Cooking Pot's: a lit campfire, fire, soul fire, lava or a magma block). Stir in the brew's **hex ingredient** (one is used):

   | Brew | Hex ingredient (item tag) | Hex brew | Draught |
   | --- | --- | --- | --- |
   | green | a brown mushroom (`jugcraft:hex/shrinking`) | shrinking brew, pale green | **Shrinking Draught** |
   | orange | beans (`jugcraft:hex/giant`) | giant's brew, deep amber | **Giant's Draught** |
   | purple | a phantom membrane (`jugcraft:hex/flying`) | flying brew, violet | **Flying Ointment** |

   The pot flashes with witch's sparkles and a low brewing sound. A cold pot ignores the hex ingredient, and so does the wrong brew.
3. A hex brew glows (light 7) and gives off enchanting glyphs as well as bubbles. Its level drops as it is drawn.
4. Each **glass bottle** draws one draught. A hex brew holds **three**, and the pot is empty after the last. Brew ingredients don't spoil a hex brew. An empty bucket pours it out as water, like any brew.
5. **Drink** a draught like a potion:
   - **Shrinking Draught:** Shrunk for 3 minutes. You are half your size (half as tall and half as wide), so you fit through one-block gaps. Earns **Drink Me**.
   - **Giant's Draught:** Giant for 3 minutes. You are 1.6 times your size, step half a block higher, and reach a block further for blocks and entities. Earns **Fee-Fi-Fo-Fum**. It is refused ("There is no room to grow here") where you would not fit at your new size. It is not used up, even if it was drunk to the end as the ceiling closed in.
   - **Flying Ointment:** rubbed on, Slow Falling for 30 seconds.
   - Shrinking and Giant cancel each other.
6. A shrunk player whose shrinking runs out where there is no room to grow back stays small another 5 seconds, as often as needed, with the same message. Nobody grows into a wall.

### The flying broomstick
1. **Make one** from a Witch's Broom, a Flying Ointment and two feathers (shapeless). A new broom holds one ointment's flight: **2 minutes**.
2. **Use it** to lay it out where you stand and get on. A dry broom (no ointment left) is only laid down, with a message.
3. **Fly by looking:**
   - **forward** flies the way you look, climbing as you look up and diving as you look down;
   - **back** brakes;
   - **left** and **right** drift sideways;
   - **jump** climbs;
   - with nothing pressed it slows to a hover.

   It tops out at about 10 blocks a second, a quarter faster in a **witch hat**. Witch's sparkles trail from the bristles. It tilts as it climbs and dives, and bobs gently at rest.
4. **Fuel:** flight burns the ointment, but only off the ground.
   - At 20 seconds left: "The ointment is wearing thin".
   - Run dry, it can't climb or speed up, and **sinks gently** to the ground, trailing smoke.
5. **Getting off:** sneak to get off. The broom **hovers where you left it** (a dry one sinks to the ground). Getting off in the air, you fall slowly for 5 seconds. You take no fall damage while riding.
6. **Using a broom out in the world:**
   - with **Flying Ointment**, it is anointed with 2 more minutes, up to 6 (the bottle comes back);
   - **sneak-use** takes it back as an item, with its flight;
   - a **blow** breaks a riderless broom back into its item.
7. **Advancements:** **Up and Away** for taking off; **Over the Moon** (a challenge) for flying 48 blocks above sea level on a full-moon night.

### Full-moon werewolves
1. **When and where:** only on **full-moon nights**, only in the Overworld's **forests, taiga and groves** (biome tag `jugcraft:werewolf_haunts`). Every 10 seconds, for each player, there's a 30% chance a werewolf steps out of the woods 24 to 40 blocks away onto open woodland floor (grass, dirt, podzol and the like: earth a sapling could grow on), with a long, low **howl**. At most 2 near a player and 8 in the world. None come in peaceful or while mobs don't spawn.
2. **The werewolf:** a hulking wolf-man about 2.4 m tall, hunched forward. It has:
   - a barrel chest under a shaggy hump of mane, with a ruff at the throat;
   - a long snout with open jaws full of teeth and fangs, cheek tufts and tall pointed ears;
   - long arms ending in four hooked claws, hanging below its knees;
   - legs bent like a wolf's hind legs, standing on clawed paws;
   - a bushy tail.

   Its eyes glow in the dark. It leaps at its prey and hunts players and villagers. Hunting, it hunches lower, raises its claws and snaps its jaws. With nothing to hunt, it howls at the moon.
3. **Three kinds, three tiers of danger.** Each has its own fur, its own pelt and its own rug.

   | Kind | Tier | Where | Health | Hits for | Armour | Speed | Size | Experience |
   | --- | --- | --- | --- | --- | --- | --- | --- | --- |
   | **Brown Werewolf** | I | any werewolf wood; the most common | 20 hearts | 6 | 2 | 0.33 | 2.4 m | 10 |
   | **Snow Werewolf** | II | snowy woods: snowy taiga, groves (`jugcraft:snow_werewolf_haunts`, `#c:is_snowy`) | 25 hearts | 8 | 4 | 0.32 | 2.5 m | 20 |
   | **Shadow Werewolf** | III | rare: 1 in 12 anywhere, 1 in 2 in its haunts (`jugcraft:shadow_werewolf_haunts`: dark forests, the pale garden, the Gloomweald, the ghost forest) | 40 hearts | 11 | 6 | 0.36 | 2.8 m | 40 |

   - **Brown, the pack hunter.** Dark brown, red-eyed.
     - It raids **livestock** as well as people: sheep, pigs, cows, chickens and rabbits it can see within 16 blocks (`jugcraft:werewolf_prey`). It still goes for a player who comes near.
     - When it finds new prey, its howl **calls the pack**: brown werewolves within 32 blocks that aren't hunting join the hunt (at most one call every 5 seconds).
     - Below a quarter of its health it **flees**, whining, from whoever is nearest. It won't hunt again until it has healed to half (about 20 seconds without silver).
   - **Snow, the stalker.** White fur, red eyes, a little bigger.
     - Its bite brings **frostbite**: Slowness II for 3 seconds, and 4 seconds' worth of freezing. Bitten again soon after, you freeze through and take freezing damage. **Leather** keeps the chill out (as in powder snow), not the slowing.
     - It never freezes itself, and on snow (a snow layer, a snow block or powder snow) it runs a **quarter faster**.
   - **Shadow, the alpha.** Near-black, amber-eyed, a head taller than the brown, with 2½ times its armour and nearly twice its bite.
     - **Shadow step:** when its prey is 6 or more blocks off, it vanishes in smoke and steps out **behind them**, onto solid ground with room to stand, at most every 10 seconds.
     - **Alpha howl:** on the hunt, at most every 30 seconds. It brings **Darkness** for 8 seconds to players within 16 blocks in survival or adventure (as the warden does). It drives every other werewolf within 24 blocks into a **frenzy**: Strength and Speed for 10 seconds, and its prey for any not hunting.
     - **A sprig of wolfsbane in hand won't stop it.** Only planted or potted wolfsbane wards it off.
     - Slaying one earns **Leader of the Pack** (a challenge).
4. **Its hide:** anything but silver does **half** its damage, and it heals half a heart every 2 seconds.
5. **Silver:** silver does **two and a half times** its damage and stops the healing for 5 seconds. Electric sparks fly where silver bites.
   - The **Silver Dagger** (a silver ingot and a stick): quick and light (5 damage, 2.2 attacks a second), easily enchanted, repaired with silver.
   - **Silver Arrows** (a silver nugget and four arrows make four): fired from any bow or crossbow.
   - Slaying a werewolf with silver earns **Silver Lining**.
6. **Wolfsbane** wards them off. It's a tall flower of hooded violet-blue blooms that grows wild in taiga and forests, and can be planted or potted. Its suspicious stew poisons. A werewolf won't hunt anyone holding a sprig (except a shadow werewolf), or anyone within 6 blocks of growing or potted wolfsbane. That includes livestock penned by wolfsbane. A werewolf hunting someone who becomes warded drops them, slinks off whining, and leaves them alone for 10 seconds. Being saved by wolfsbane earns **Not Tonight**. Plant it round the homestead, and werewolves can't step out of the woods near it either.
7. **At dawn,** or whenever it finds it isn't a full-moon night, a werewolf is gone in a swirl of smoke.
8. **Drops:** its kind's pelt (**Werewolf Pelt**, **Snow Werewolf Pelt** or **Shadow Werewolf Pelt**) and a bone or two. Two pelts of a kind make that kind's rug (**Werewolf Rug**, **Snow Werewolf Rug**, **Shadow Werewolf Rug**): a pelt laid flat with its snarling head at one end, for the floor. Any pelt can also be cut into two leather.

## Connections
- Hex brews, input producer:
  - the Bubbling Cauldron and its brews (the witch's cottage decorations);
  - the Cooking Pot's `jugcraft:heat_sources`;
  - brown mushrooms (vanilla's: swamps, dark forests, caves and the Nether; they spread in the dark);
  - the Kitchen Garden's beans;
  - phantoms (players who skip sleep);
  - glass bottles.
- Hex brews, output consumer:
  - exploration (tight caves, low tunnels, the walled town's alleys, cliffs);
  - building (a giant's reach);
  - trade (draughts stack and keep).
- Technology connection: none needed. The heat can come from any heat source, including a magma block in a factory.
- Magic connection: the first brew in Jugcraft that does something. It extends the decorative cauldron into the witch's craft, beside the aura candles' scents.
- Entry path: every input is early-game and reachable in the Overworld (see the tier above). No hex needs another hex.
- Required vs optional: optional, all year. Nothing is gated on a hex. One player can brew all three alone.
- Trade and solo routes: draughts stack to 16, so a witch can sell them. Anyone can brew their own.
- Werewolves, input producer: the full moon (the same moon as mooncakes and fairy rings); the woods; silver (mined ore, smelted).
- Werewolves, output consumer: pelts (rugs, leather), bones; the wolfsbane flower for gardens and pots, and as a mixed bouquet in a grave vase.
- Werewolves, technology connection: silver gets a use of its own beside cables.
- Werewolves, entry path: everything is early; a player can stay safe on a full-moon night with a sprig of wolfsbane in hand from the first one.
- Werewolves, required vs optional: optional; they are a danger to plan for, and nothing is gated behind them. A smith can make silver weapons for others.
- Flying broomstick, input producer: the Witch's Broom (the witch's cottage decorations), Flying Ointment (hex brews), feathers, and the witch hat costume for speed.
- Flying broomstick, output consumer: travel and exploration; the full-moon night (mooncakes, fairy rings) for Over the Moon. Every witch's cottage piece now has a use: the cauldron brews the ointment that flies the broom.
- Flying broomstick, entry path: everything is Discovery tier; the first broom comes charged. Nothing needs the End.
- Flying broomstick, required vs optional: optional. A broom flies one player; anyone can anoint anyone's broom.

## Balance and automation
- **Costs:** the cauldron is a cauldron and two iron ingots, reused. One brew is a water bucket (the bucket comes back), one brew ingredient, one hex ingredient and three glass bottles, which come back when drunk. That makes three draughts.
- **Units:** seconds of effect (Shrunk 180, Giant 180, Slow Falling 30); doses (3 a brew); attribute scale (×0.5 shrunk, ×1.6 giant); blocks (giant step +0.5, reach +1.0); ticks (the 100-tick grace while there is no room).
- **Effects:**
  - *Shrunk* changes only the player's scale, from which vanilla takes the player's size and eye height.
  - *Giant* adds scale, step height and interaction range.
  - Neither touches health, damage or speed.
  - Flying ointment is vanilla Slow Falling, as from a phantom-membrane potion, and shorter than the potion's 90 seconds.
- **No loops:** nothing turns a draught back into anything. Water is neither made nor lost: the bucket goes in and the pot empties as the last dose is drawn.
- **Automation:** none. Brewing and drawing are by hand (block use). Hoppers and dispensers don't fill bottles from the pot.
- **Werewolves:**
  - Tiers (a zombie: 20 health, 2 armour, hits for 3, speed 0.23):
    - brown: 40 health, 2 armour, hits for 6, speed 0.33;
    - snow: 50, 4, 8, 0.32;
    - shadow: 80, 6, 11, 0.36.

    Without silver it takes half damage and heals a point (half a heart) every 40 ticks, so an iron sword (6) does 3, and fighting one without silver is meant to be hard. With silver it takes 2.5 times: the silver dagger (5) does 12.5 (before armour). Four hits kill a brown werewolf, five a snow one and seven a shadow one.
  - A shadow werewolf can be fought without silver, but takes more than 30 iron-sword hits. It is meant for prepared groups and isn't needed for anything: its pelt and rug are decoration and leather, like the brown's.
  - The snow werewolf's frostbite adds 80 ticks of freezing a bite, capped at full freezing (140), which wears off at 2 a tick as vanilla's does.
  - The dagger: 200 uses (iron 250), enchantability 20, silver's repair. Silver arrows: four for a nugget and four arrows.
  - A pelt of any kind is two leather (a cow drops 0–2), and two pelts make a rug. Nothing makes werewolves or pelts, so there is no loop.
  - Cost on the server: the spawner tries one spot a player every 10 seconds, only on full-moon nights. A werewolf checks the moon once a second, and its target's ward every half second (a sprig in hand, or 13 × 7 × 13 blocks around the target). The spawner's ward check reads the same box once per attempt.
    - A brown werewolf with nothing to hunt looks for livestock within 16 blocks once a second. Its pack call looks for werewolves within 32 blocks once a hunt, at most every 5 seconds.
    - A snow werewolf reads the two blocks at its feet each tick.
    - A shadow werewolf on the hunt tries its step at most every 10 seconds (up to 9 spots). Its howl looks at the level's players and the werewolves within 24 blocks at most every 30 seconds.
- **Flying broomstick:**
  - Costs: a Witch's Broom (two sticks and a wheat), one Flying Ointment and two feathers. Each further 2 minutes of flight is one Flying Ointment, so one hex brew (one phantom membrane) flies a broom for 6 minutes.
  - Units: ticks of charge (2,400 an ointment, 7,200 at most, one a tick in the air); blocks a tick (push 0.05 forward, 0.025 sideways, 0.04 up; speed kept 0.91 a tick, 0.728 braking; top 0.6, 0.75 in a witch hat; dry, it sinks at up to 0.08).
  - Held forward it tops out about 0.51 blocks a tick (10 m/s, 12.6 in a hat): faster than a sprint (5.6 m/s) or a horse, far slower than elytra with rockets. It carries one player.
  - Nothing is made: the broom doesn't break down into anything, and its charge only burns.
  - Automation: none; anointing is by hand.
  - Cost on the server: one charge tick and a ground probe per ridden broom a tick, and a distance check every second. Riderless brooms run a short drift; dry ones a ground probe. The pilot's client does the flying.
- **Hex brews, cost on the server:** one look at each online player per server tick (a duration and effect check; a collision check only when a shrinking is about to end). No block entity, no block ticks. The glyphs and bubbles are client particles.

## Multiplayer and persistence
- **Server authority:**
  - Stirring in an ingredient, drawing a dose and pouring all go through vanilla's block use path (reach, spawn protection) and are decided on the server.
  - A player without build rights (adventure mode) can change nothing.
  - Drinking goes through vanilla's item use, and the server applies the effect.
  - The room check for the Giant's Draught runs on the server when the draught is started and again when it is finished.
- **What clients get:** the cauldron's block state (contents and doses) and the player's effects and attributes, which vanilla sends to every client. Every player sees a giant as a giant.
- **Concurrent use:** two players drawing from one pot draw one dose each, until it is empty.
- **Persistence:** the pot's brew and doses are block state. Effects are saved with the player by vanilla and keep across a restart.
- **IDs (all new):**
  - items `shrinking_draught`, `giants_draught`, `flying_ointment`;
  - mob effects `jugcraft:shrunk` and `jugcraft:giant`;
  - item tags `jugcraft:hex/shrinking`, `hex/giant`, `hex/flying`;
  - advancements `drink_me`, `fee_fi_fo_fum`.

  The cauldron's `contents` property gains `shrinking`, `giant` and `flying`, and a new property `doses` (1–3) is added. Cauldrons already placed keep their contents and take the default of 3 doses (not used until a hex is brewed).
- **Disable behaviour:** the hexes are part of the agriculture feature's blocks and stay registered. With the agriculture feature off, the broom's recipe doesn't load; brooms already made still fly and stay registered.
- **Werewolves, server authority:** spawning, the kind, damage, healing, wards, the pack call, fleeing, frostbite, the shadow step, the howl's effects and turning back are all decided on the server. Clients only draw the werewolf (its kind is synced entity data) and hear it. The ward is checked against the target's hands and the blocks around them on the server. The shadow step only lands on solid ground with room for the werewolf, so it can't step into walls.
- **Werewolves, persistence:** a werewolf is an ordinary monster: it despawns far from players, and one saved in a chunk turns back on its first full-moon check if the night has ended. It saves its kind (`kind`: `brown`, `snow` or `shadow`). One saved before there were kinds loads as brown, with the brown's attributes. Wolfsbane and the rugs are blocks; the dagger, arrows and pelts items. `/summon jugcraft:werewolf ~ ~ ~ {kind:"shadow"}` brings one of a kind at full health.
- **Werewolves, IDs (all new):**
  - entity `werewolf`;
  - blocks and items `wolfsbane` (and block `potted_wolfsbane`), `werewolf_rug`, `snow_werewolf_rug`, `shadow_werewolf_rug`;
  - items `silver_dagger`, `silver_arrow`, `werewolf_pelt`, `snow_werewolf_pelt`, `shadow_werewolf_pelt`;
  - tags `jugcraft:silver_weapons`, `jugcraft:repairs_silver_gear`, `jugcraft:werewolf_haunts`, `jugcraft:snow_werewolf_haunts`, `jugcraft:shadow_werewolf_haunts` (biome), `jugcraft:werewolf_prey` (entity type);
  - loot tables `entities/werewolf` (bones) and `entities/werewolf/brown`, `snow`, `shadow` (each kind's pelt, rolled when it dies, if mobs drop loot);
  - advancements `silver_lining`, `wolfsbane_ward`, `leader_of_the_pack`;
  - recipes `silver_dagger`, `silver_arrow`, one rug and one `leather_from_<pelt>` for each pelt;
  - worldgen `jugcraft:wolfsbane`, `jugcraft:patch_wolfsbane`;
  - the attribute modifier `jugcraft:snow_stride` (transient, never saved).
- **Werewolves, disable behaviour:** with the agriculture feature off, werewolves stop coming and the recipes don't load; everything stays registered. The silver recipes also need the silver feature.
- **Flying broomstick, server authority:**
  - As with a boat or a horse, the pilot's client flies the broom and sends its moves, and vanilla's vehicle checks run on the server. The broom has no gravity, so vanilla's floating kick doesn't apply.
  - The server burns the charge itself, and every 20 ticks compares where the broom is with where it was. If it went further than a broom can fly (three times its top speed, for lag), or climbed more than 2 blocks while dry since the last check, the rider is thrown off ("The broom bucks you off") and the broom is the server's again: it hovers or sinks.
  - Laying out, anointing, taking back and breaking are decided on the server. Use goes through vanilla's entity interaction, whose reach check runs first.
- **Flying broomstick, what clients get:** the broom's position and charge (synced entity data), so everyone sees it fly and a dry broom's grey straw. The pilot's keys stay on their own client.
- **Flying broomstick, persistence:** a broom saves its charge. Riding is vanilla's: a player who logs out riding is back on their broom when they log in. The item keeps its charge in `jugcraft:broom_charge`.
- **Flying broomstick, IDs (all new):** item and entity `flying_broomstick`; data component `jugcraft:broom_charge`; advancements `up_and_away`, `over_the_moon`; the recipe `flying_broomstick`.

## Dependencies and assets
- No new dependencies.
- Textures are drawn by code in `tools/decor4_textures.py`: each hex brew's liquid, the three draughts and the two effect icons (a mouse-sized figure and a towering one).
- The cauldron's models for each hex and level, the item models, names, tooltips, the message, the tags and the advancements come from `tools/decor4_data.py`. The numbers come from `HEX` in `tools/agriculture.py`.
- `tools/check_mod_data.py` compares them with Java.
- Sounds and particles are vanilla's: a brewing stand's brew pitched low, a bottle filling, witch's sparkles and enchanting glyphs.

- Werewolves: no new dependencies.
  - `tools/werewolf_model.py` holds the model's parts and boxes as data. `client/WerewolfModel.java` builds the same boxes, and the checker compares the two box by box.
  - `tools/werewolf_textures.py` paints each kind's fur from those boxes (128 × 128, face by face) and its glowing eyes alone; also wolfsbane, each kind's rug and pelt, the dagger and the arrow.
  - The look follows reference pictures the owner gave (a white, a dark brown and a dark grey werewolf): hunched, long-snouted, open-jawed, tall-eared, long-clawed, wolf-legged, bushy-tailed, with red or yellow eyes. Every pixel is drawn by code; nothing is traced or copied from them.
  - `tools/werewolf_data.py` writes the flower's and rugs' models, the items, words, loot, tags and worldgen. The numbers come from `WEREWOLF` and `WOLFSBANE` in `tools/agriculture.py`, which the checker compares with Java. Sounds are vanilla's (a wolf's howl, growl, whine, hurt and death, pitched low), and particles vanilla's (electric sparks, large smoke).
- The flying broomstick: no new dependencies. `tools/broom_textures.py` draws the item (a broom aslant, purple cord, fanned straw, sparkles) and the entity's pale grain, which `client/BroomstickRenderer.java` tints for the ash handle, the cord and the straw (greyer when dry) on boxes of its own. `tools/broom_data.py` writes the item model and words. The numbers come from `BROOMSTICK` in `tools/agriculture.py`, which the checker compares with `Broomstick.java`. Sounds and particles are vanilla's: a phantom's flap on take-off, a brewing stand when anointed, witch's sparkles and smoke.

## Verification
### Hex brews verification

Actual results (3 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/fall-21-hex-brews` stacked on graveyard pack 4:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares the hexes with Java: the doses, the room check's extension, each draught's scale, step, reach and time, and which brew each hex is made from; and checks the hex tags, the cauldron's models for every hex and dose, the draughts, the effects' icons and words, and the advancements) | Pass, 1045 IDs |
| `./gradlew build` on `ef65d069` (Build workflow run [37144213831](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37144213831)) | Pass |
| Game tests on the headless server, same run: 591 in total, 3 of them new here (`HexBrewGameTests`) | **All 591 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `ef65d069` (client shard 2 of that run) |

The 3 new game tests (`HexBrewGameTests`):
1. over a magma block, water, a spider eye and a brown mushroom make the shrinking brew with three doses, glowing; a spider eye then doesn't spoil it; an adventure-mode player draws nothing; three glass bottles draw three Shrinking Draughts and the third empties the pot; the same in a cold pot stays green; beans make the orange brew a giant's, a phantom membrane the purple brew flying ointment, and neither works on the wrong brew;
2. the Shrinking Draught halves the drinker (scale 0.5), leaves a glass bottle and earns Drink Me; under a two-block ceiling the Giant's Draught is refused (use fails) and isn't used up even when drunk to the end; a shrunk player whose shrinking is about to end with no room to grow keeps it longer; in the open the Giant's Draught cancels the shrinking, scales the drinker 1.6 with a block more reach and earns Fee-Fi-Fo-Fum; the Shrinking Draught cancels it again; Flying Ointment gives Slow Falling;
3. the hex tags, effects, draughts and advancements load.

Found by CI and fixed before this record:
- The first run failed to compile in graveyard pack 4, below this branch (26.3 has no `ItemTags.SMALL_FLOWERS`); fixed there.

The client test's first screenshots showed the cauldrons over campfires lost in smoke, and from too low to see into them. It now sets them over magma, looks down into the brews, and shows a shrunk player and a giant from in front.

Run [37145541815](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37145541815), the pull request's run on `2f716980` with that change, passed every job: the build, all 591 server game tests and the three client shards. Its screenshots show the three hex brews glowing over magma with their draughts framed above, the shrinking brew's pale green with its glyphs, the world from a shrunk player's height, a shrunk player beside the cauldrons at half size, and a giant towering over them ([the guide](../branches/AGRICULTURE.md#hex-brews) has them).

Not run: a two-client dedicated-server playtest, and any play by hand.

### Werewolves verification

Actual results (3 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/fall-23-werewolves` stacked on the flying broomstick:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares `WEREWOLF` and `WOLFSBANE` with Java: the hide and silver, healing, shunning, spawning and ward, the attributes and the silver blade; and checks the entity, flower, rug, items, words, loot, tags, worldgen, recipes and advancements) | Pass, 1052 IDs |
| `./gradlew build` on `b5b16e54` (Build workflow run [37148023636](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37148023636)) | Pass |
| Game tests on the headless server, same run: 601 in total, 5 of them new here (`WerewolfGameTests`) | **All 601 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `b5b16e54` (all three client shards of that run) |

The 5 new game tests (`WerewolfGameTests`):
1. an iron sword's 8 damage does 4 to a werewolf, the silver dagger's 20; a plain arrow 4, a silver arrow 20; silver stops its healing; slain with silver it earns Silver Lining;
2. a werewolf is gone within a second when it isn't a full-moon night, and stays when it is;
3. wolfsbane in hand or within reach wards a player, beyond reach doesn't; a werewolf keeps hunting a villager with nothing in hand, and drops one holding a sprig and won't attack them; a warded player is left alone and earns Not Tonight;
4. no werewolf comes past the cap near a player; grass is woodland floor for one to step onto, stone isn't;
5. the recipes, loot tables, advancements and tags load.

Found by CI and fixed before this record:
- The ward test failed twice: a werewolf never took the test's player as its target. A player who has only just joined can't be targeted yet (the Horseman's code notes the same), so setting the target never took. The ward check is now tested on villagers, which werewolves also hunt, and a player's shunning and Not Tonight through `wardedOff`, which the ward check calls.
- Werewolves could never have come out onto grass: the spawn ground had to be in `minecraft:dirt`, and the squirrel tests on the branch above failed on grass with the same check in 26.3. The ground is now any block an oak sapling could grow on (`Werewolves.woodlandFloor`), and the fourth test checks grass and stone.

The client test's screenshots (from run [37147539009](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37147539009) on `1244c712`, whose client test is the same) show wolfsbane growing wild and potted on a stump in a spruce clearing, the silver dagger, silver arrows and a pelt framed above the fire, and two werewolves in the clearing on the full-moon night, up close and from further off ([the guide](../branches/AGRICULTURE.md#full-moon-werewolves) has them). Its log has no missing model or texture. The rug lay at the picture's bottom edge, cut off; `1087fddd` moves the camera back for it, not yet run.

Not run: a werewolf hunting a player in play, which no automated test watches from start to end; a two-client dedicated-server playtest.

#### The three kinds (snow white, brown and dark grey)
Added after the record above, on the same branch. The model was rebuilt (128 × 128 texture: a deeper chest and mane, a longer snout with open jaws and fangs, taller ears, longer clawed arms and legs, a bushier tail) and the werewolf became three kinds, with the abilities above. Eight game tests are new (`WerewolfGameTests`):
1. each kind is tougher than the last (health, damage, armour, size, tier) and has its kind's attributes; an unknown kind reads as brown; snowy woods bring snow werewolves, its haunts a shadow werewolf half the time and elsewhere rarely; snowy taiga and dark forests are in the right biome tags;
2. a brown werewolf goes after a sheep; its howl calls a brown werewolf near to the hunt, not a snow one; a snow werewolf leaves livestock alone;
3. below a quarter of its health a brown werewolf drops its prey and flees until healed to half; a snow werewolf doesn't flee;
4. a snow werewolf's bite slows and chills a villager, and only slows one in leather boots; it can't freeze, a brown one can; on snow it runs a quarter faster, a brown one doesn't;
5. a shadow werewolf steps out two blocks behind prey seven blocks off, on the ground; not when close; a brown one never;
6. a shadow werewolf's howl gives a player near Darkness and a brown werewolf near Strength, Speed and its prey;
7. a sprig in hand wards off a brown werewolf, not a shadow one; wolfsbane planted by its prey wards off the shadow one;
8. each kind drops its own pelt; slaying a shadow werewolf earns Leader of the Pack, a brown one doesn't.

Locally, `python3 tools/check_mod_data.py` passes with 1071 IDs. It now also compares each kind's tier, attributes and abilities, the pelt tables and haunts, and every box of the model with `tools/werewolf_model.py`. `python3 scripts/check_repository.py` passes. The build, the game tests and the client test with the three kinds have not run yet.

### Flying broomstick verification

Actual results (3 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/fall-22-flying-broomstick` stacked on hex brews:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares `BROOMSTICK` with `Broomstick.java`: charge, flight, the server's checks; and checks the item, entity, component and callback are registered, the client steers and draws it, and its words, recipe, advancements and textures exist) | Pass, 1046 IDs |
| `./gradlew build` on `ada0c49a` (Build workflow run [37146789337](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37146789337)) | Pass |
| Game tests on the headless server, same run: 596 in total, 5 of them new here (`BroomstickGameTests`) | **All 596 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `ada0c49a` (client shard 0 of that run) |

The 5 new game tests (`BroomstickGameTests`):
1. forward flies along the look, climbing when looking up; it slows with nothing pressed and faster braking; jump climbs; strafe drifts; held forward it tops out under its top speed, a quarter faster in a witch hat; dry, it can't climb or speed up and sinks no faster than it may; the server's distance check allows a broom's top speed and no more;
2. the item lays out the broom and seats its user (Up and Away), used up; a rider can't lay out another; a dry broom is only laid out;
3. ointment anoints a broom up to 6 minutes, returning bottles, and no further; sneak-use takes it back with its charge; a blow breaks a riderless broom into its item, keeping its charge, but not a ridden one;
4. ridden in the air by a player in a witch hat, the server burns its charge until it is dry, and keeps its rider while it doesn't climb; moved up while dry, the next check throws its rider off, who falls slowly;
5. its recipe, advancements and entity load, and a new broom holds one ointment.

Found by CI and fixed before this record:
- The first run failed to compile: 26.3's client has no `screen` field; the open screen is `client.gui.screen()`.
- The next failed in the fourth test: a rider thrown off in the air didn't fall slowly. Slow falling was given from the broom's passenger removal, which the test never saw take effect. The broom now gives it itself: at once when it throws a rider, and the tick after any rider gets off, if they are alive, off the ground and not riding.

The client test's screenshots show the brooms by the cauldron of flying ointment (one hovering, one dry on the grass with its straw greyed, the item framed), a player in a witch hat riding one from behind (the bristles below them) and in front, and at midnight ([the guide](../branches/AGRICULTURE.md#the-flying-broomstick) has them). Its log has no missing model or texture. From in front, the rider hides most of the handle.

Not run: flying it by hand, which no automated test can do (the client test's rider has no keys pressed); a two-client dedicated-server playtest.

## World and event applicability
- A cauldron brews hexes in any dimension, all year. Nothing is seasonal.
- The hex ingredients come from the Overworld: brown mushrooms, beans, and phantoms.
- Werewolves come only to the Overworld's forests, taiga and groves, on full-moon nights. Snow werewolves only where it snows, shadow werewolves anywhere they come (most often in dark woods). Wild wolfsbane generates only in newly generated chunks of those biomes; existing chunks don't get it, but a sprig brought in can be planted anywhere.
- A broom flies in any dimension. Over the Moon is earned only in the Overworld. Nothing is seasonal.

## Rollout and open questions
- A hex brew can't be drawn by hoppers or dispensers. Bottles are filled by hand.
- Breaking a cauldron loses its brew, as before.
- How big a giant is (×1.6) and how long each effect lasts are open to balance review once played. A giant is easier to hit, which is the trade for its reach.
- Mobs aren't affected: a splash or lingering hex is not made.
- Werewolves don't break doors or blocks, and don't turn players into werewolves; there is no curse.
- Werewolves don't fight each other; a shadow werewolf's frenzy helps any kind.
- A brown werewolf killing a farm's livestock is intended: wolfsbane planted by the pen keeps them off.
- A werewolf ignores wolfsbane once it isn't hunting; the ward is about who it hunts and where it may come out, not where it may walk.
- How often they come and how hard they hit are open to balance review once played.
- A broom carries one rider and nothing else; it can't be led, put in a minecart or flown by mobs.
- A broom collides with blocks by its own small box, and stops moves that would put its rider's head in a block. Diving into a block is not slowed further than vanilla's collision.
- The top speed and the 2-minute charge are open to balance review once flown. A rider who loses connection mid-flight stays on their broom where it was.
- A shrunk player's grace is 5 seconds at a time. A player who logs out shrunk under a low ceiling stays shrunk until there is room.
