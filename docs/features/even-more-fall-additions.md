# Even More Fall Additions

Status: hex brews (addition 21) are implemented in source and pass CI's game tests; the flying broomstick (addition 22), full-moon werewolves (addition 23) and squirrels and acorns (addition 24) are implemented in source. None is yet played by hand. Additions 25 to 30 are planned and not yet built. The Build workflow compiles what exists; CI's game tests and client screenshots are recorded below.
Proposal issue: none. The owner asked for these directly on 3 October 2026 ("Lets do even more fun fall and halloween stuff 10 well thought out good additions"). They follow the [more fall additions](more-fall-additions.md) and are numbered on from them. Each one is its own pull request, stacked on the one before, after the graveyard pack:
21. hex brews: the Bubbling Cauldron's brews, bubbling over a fire, take a hex ingredient and become the Shrinking Draught, the Giant's Draught or Flying Ointment;
22. the flying broomstick: a witch's broom anointed with Flying Ointment, ridden and steered by looking;
23. full-moon werewolves: they come out of the woods only on full-moon nights; wolfsbane wards them off and silver hurts them;
24. squirrels and acorns: squirrels in the woods gather and bury acorns, and some grow into oaks; acorns plant oaks and roast;
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

Squirrels and acorns are Discovery tier: squirrels come to any player in Overworld woods by day, and oak and dark oak leaves drop acorns (as they drop apples). Roasting needs a furnace, smoker or campfire.

The flying broomstick is Discovery tier too: the Witch's Broom (two sticks and a wheat), one Flying Ointment and two feathers.

Primary specialty and supported player role: witchcraft and exploration. A witch brews hexes for others:
- **Shrinking:** gets a player into one-block gaps, under low ceilings and into cramped caves, and makes them a smaller target.
- **Giant:** gives a block more reach and half a block more step, for building and climbing, but makes the player a bigger target.
- **Flying ointment:** gives a safe fall from a cliff or a tower.

Draughts stack to 16 and keep, so a witch can trade them.

Werewolves are for fighters and hunters: a monthly danger in the woods, beaten by planning (wolfsbane about the homestead, silver in hand), and a reward in pelts. They give silver, until now only a cable metal, a use of its own.

Squirrels and acorns are for foresters, cooks and anyone who likes animals: acorns are an early snack and another way to plant oaks, and squirrels slowly plant the woods they live in. Tame-minded players can breed them with nuts.

The flying broomstick is a witch's way to travel: slower than elytra but found long before them, good for scouting, crossing ravines and reaching rooftops, and fuelled by the ointment a witch brews.

None of this depends on the Halloween event: a cauldron brews hexes and a broom flies all year, werewolves come on every full moon, and squirrels live in the woods all year.

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
1. **When and where:** only on **full-moon nights**, only in the Overworld's **forests and taiga** (biome tag `jugcraft:werewolf_haunts`). Every 10 seconds, for each player, there's a 30% chance a werewolf steps out of the woods 24 to 40 blocks away onto open woodland floor (grass, dirt, podzol and the like: earth a sapling could grow on), with a long, low **howl**. At most 2 near a player and 8 in the world. None come in peaceful or while mobs don't spawn.
2. **The werewolf:** a hulking wolf-man about 2.4 m tall, with a barrel chest hunched under a shaggy mane, a long snout, long clawed arms and legs bent like a wolf's.
   - It has 20 hearts, hits for 7 and is fast.
   - It leaps at its prey and hunts players and villagers.
   - Hunting, it hunches lower, its arms raised and its jaws open. With nothing to hunt, it howls at the moon.
3. **Its hide:** anything but silver does **half** its damage, and it heals half a heart every 2 seconds.
4. **Silver:** silver does **two and a half times** its damage and stops the healing for 5 seconds. Electric sparks fly where silver bites.
   - The **Silver Dagger** (a silver ingot and a stick): quick and light (5 damage, 2.2 attacks a second), easily enchanted, repaired with silver.
   - **Silver Arrows** (a silver nugget and four arrows make four): fired from any bow or crossbow.
   - Slaying a werewolf with silver earns **Silver Lining**.
5. **Wolfsbane** wards them off. It's a tall flower of hooded violet-blue blooms that grows wild in taiga and forests, and can be planted or potted. Its suspicious stew poisons. A werewolf won't hunt anyone holding a sprig, or anyone within 6 blocks of growing or potted wolfsbane. A werewolf hunting someone who becomes warded drops them, slinks off whining, and leaves them alone for 10 seconds. Being saved by wolfsbane earns **Not Tonight**. Plant it round the homestead, and werewolves can't step out of the woods near it either.
6. **At dawn,** or whenever it finds it isn't a full-moon night, a werewolf is gone in a swirl of smoke.
7. **Drops:** a **Werewolf Pelt** and a bone or two. Two pelts make a **Werewolf Rug**: a pelt laid flat with its snarling head at one end, for the floor. A pelt can also be cut into two leather.

### Squirrels and acorns
1. **Where and when:** squirrels live in the Overworld's **forests and taiga** (biome tag `jugcraft:squirrel_habitat`). By day, every 20 seconds, for each player there's a 25% chance one or two come out 20 to 40 blocks away, onto open earth. None come while 6 are within 64 blocks of the player or 30 are in the world, nor while mobs don't spawn.
2. **The squirrel:** small and quick (3 hearts, a third of a block long), **red or grey**, half and half. A slim body, a round head with tufted ears, and a great bushy tail that streams out as it bounds along and curls up over its back when it sits.
   - It **climbs tree trunks**: to a squirrel, a log it runs into is a ladder. It takes no fall damage.
   - It bolts when hurt, and looks round at players nearby.
3. **Acorns:** oak and dark oak leaves drop an **Acorn** 5% of the time they are broken or decay, as they drop apples.
   - **Plant one** on the top of grass, dirt or other earth (block tag `minecraft:dirt`) with open air above it, and it's an **oak sapling**.
   - **Roast it** in a furnace, smoker or campfire: **Roasted Acorns**, a snack (3 hunger).
   - It composts, and it counts as a seed (`c:seeds`).
4. **Gathering:** a grown squirrel with empty paws goes for an acorn lying within 10 blocks, takes it and carries it about in its forepaws. After 10 seconds it **buries it** in the earth it stands on, with a scatter of dirt. One in four buried acorns sprouts there as an **oak sapling**, if no sapling or log stands within 3 blocks. On stone it keeps its acorn until it finds earth. So squirrels slowly plant the woods they live in, and a player who drops acorns near them gets some of them planted.
5. **Nuts:** acorns, chestnuts and roasted chestnuts (item tag `jugcraft:squirrel_food`) tempt squirrels to follow, and breed two into a **kit**, red or grey after either parent. Breeding them earns **Nuts About Squirrels**.
6. **Drops:** a squirrel drops only the acorn it was carrying.

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
- Squirrels and acorns, input producer: oak and dark oak trees (wild, or a player's tree farm) for acorns; the Festival Crops' chestnut tree for chestnuts to tempt and breed squirrels; any furnace, smoker or campfire.
- Squirrels and acorns, output consumer: oak saplings (forestry and tree farms), Roasted Acorns (food), compost; acorns are seeds for anything that takes `c:seeds`.
- Squirrels and acorns, entry path: everything is in the Overworld from the first day.
- Squirrels and acorns, required vs optional: optional; acorns are one more way to get oak saplings and food, and nothing is gated behind squirrels.
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
  - A werewolf has 40 health, 2 armour, hits for 7 and moves at 0.33 (a zombie: 20, 2, 3, 0.23). Without silver it takes half damage and heals a point (half a heart) every 40 ticks, so an iron sword (6) does 3, and fighting one without silver is meant to be hard. With silver it takes 2.5 times: the silver dagger (5) does 12.5, and four hits kill it.
  - The dagger: 200 uses (iron 250), enchantability 20, silver's repair. Silver arrows: four for a nugget and four arrows.
  - A pelt is two leather (a cow drops 0–2), and two pelts make a rug. Nothing makes werewolves or pelts, so there is no loop.
  - Cost on the server: the spawner tries one spot a player every 10 seconds, only on full-moon nights. A werewolf checks the moon once a second, and its target's ward every half second (a sprig in hand, or 13 × 7 × 13 blocks around the target). The spawner's ward check reads the same box once per attempt.
- **Flying broomstick:**
  - Costs: a Witch's Broom (two sticks and a wheat), one Flying Ointment and two feathers. Each further 2 minutes of flight is one Flying Ointment, so one hex brew (one phantom membrane) flies a broom for 6 minutes.
  - Units: ticks of charge (2,400 an ointment, 7,200 at most, one a tick in the air); blocks a tick (push 0.05 forward, 0.025 sideways, 0.04 up; speed kept 0.91 a tick, 0.728 braking; top 0.6, 0.75 in a witch hat; dry, it sinks at up to 0.08).
  - Held forward it tops out about 0.51 blocks a tick (10 m/s, 12.6 in a hat): faster than a sprint (5.6 m/s) or a horse, far slower than elytra with rockets. It carries one player.
  - Nothing is made: the broom doesn't break down into anything, and its charge only burns.
  - Automation: none; anointing is by hand.
  - Cost on the server: one charge tick and a ground probe per ridden broom a tick, and a distance check every second. Riderless brooms run a short drift; dry ones a ground probe. The pilot's client does the flying.
- **Squirrels and acorns:**
  - Units: a squirrel has 6 health and moves at 0.32 (a rabbit: 3, 0.3). Acorns drop at 0.05 from oak and dark oak leaves (vanilla's apples: 0.005 at no Fortune; saplings 0.05). Roasted acorns: 3 hunger, 0.4 saturation modifier (roasted chestnuts: 4, 0.6); 200 ticks in a furnace.
  - No gain: one acorn plants one sapling. A squirrel buries one acorn and, a quarter of the time, one sapling sprouts; the rest are lost. Roasting is one for one, and nothing turns roasted acorns back. Oak trees already drop saplings in vanilla, so acorns add a way, not a loop.
  - Automation: leaves that are broken or decay drop acorns, so a tree farm yields them; planting is by hand or by squirrel. Nothing plants acorns from a dispenser.
  - Cost on the server: the spawner tries one spot a player every 20 seconds, by day; it counts squirrels near the player (a 129-block box) and in the level. A squirrel with empty paws looks for acorns within 10 blocks every second or two. One carrying an acorn checks the block under it each tick after 10 seconds, and a sprouting check reads at most 7 × 4 × 7 blocks, once per buried acorn a quarter of the time. Climbing checks one block, only while it is pressed against something.
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
- **Werewolves, server authority:** spawning, damage, healing, wards and turning back are all decided on the server; clients only draw the werewolf and hear it. The ward is checked against the target's hands and the blocks around them on the server.
- **Werewolves, persistence:** a werewolf is an ordinary monster: it despawns far from players, and one saved in a chunk turns back on its first full-moon check if the night has ended. Wolfsbane and the rug are blocks; the dagger and arrows items.
- **Werewolves, IDs (all new):** entity `werewolf`; blocks and items `wolfsbane` (and block `potted_wolfsbane`), `werewolf_rug`; items `silver_dagger`, `silver_arrow`, `werewolf_pelt`; tags `jugcraft:silver_weapons`, `jugcraft:repairs_silver_gear`, `jugcraft:werewolf_haunts` (biome); advancements `silver_lining`, `wolfsbane_ward`; recipes `silver_dagger`, `silver_arrow`, `werewolf_rug`, `leather_from_werewolf_pelt`; worldgen `jugcraft:wolfsbane`, `jugcraft:patch_wolfsbane`.
- **Werewolves, disable behaviour:** with the agriculture feature off, werewolves stop coming and the recipes don't load; everything stays registered. The silver recipes also need the silver feature.
- **Squirrels and acorns, server authority:** spawning, gathering, burying, sprouting and breeding are decided on the server. Planting an acorn goes through vanilla's item use on a block, needs build rights at the spot (spawn protection, adventure mode), and only happens on the server.
- **Squirrels and acorns, what clients get:** the squirrel's colour (synced entity data) and the acorn in its paws (vanilla's equipment sync), so every player sees a squirrel carrying one.
- **Squirrels and acorns, persistence:** a squirrel saves its colour, the acorn it holds and how long it has held it, and is saved with its chunk like any animal. Saplings are ordinary oak saplings.
- **Squirrels and acorns, IDs (all new):** entity `squirrel`; items `acorn`, `roasted_acorns`; item tag `jugcraft:squirrel_food`, biome tag `jugcraft:squirrel_habitat`; advancement `nuts_about_squirrels`; recipes `roasted_acorns`, `roasted_acorns_from_smoking`, `roasted_acorns_from_campfire_cooking`. Acorns join `c:seeds`, and roasted acorns `c:foods`.
- **Squirrels and acorns, disable behaviour:** with the agriculture feature off, squirrels stop coming, leaves stop dropping acorns and the recipes don't load; everything stays registered, and squirrels already in the world stay.
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

- Werewolves: no new dependencies. `tools/werewolf_textures.py` draws the werewolf (128 × 64, laid out as `client/WerewolfModel.java`'s boxes), wolfsbane, the rug, the dagger, the arrow and the pelt. `tools/werewolf_data.py` writes the flower's and rug's models, the items, words, loot, tags and worldgen. The numbers come from `WEREWOLF` and `WOLFSBANE` in `tools/agriculture.py`, which the checker compares with Java. Sounds are vanilla's (a wolf's howl, growl, whine, hurt and death, pitched low), and particles vanilla's (electric sparks, large smoke).
- Squirrels and acorns: no new dependencies. `tools/squirrel_textures.py` draws the red and grey squirrels (32 × 32, laid out as `client/SquirrelModel.java`'s boxes: soft fur, a cream belly, a bushy tail paler at its tip), the acorn and roasted acorns. `tools/squirrel_data.py` writes the acorn's item model, words and tags; roasted acorns, the recipes and the advancement come from `ITEMS`, `COOKING` and `ADVANCEMENTS` in `tools/agriculture.py`. The numbers come from `SQUIRRELS` in `tools/agriculture.py`, which the checker compares with Java. Sounds are a fox's, pitched high; particles are the earth's own block dust.
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

Not run yet: CI has not built this branch. The planned checks are:
- `WerewolfGameTests`, 5 game tests:
  1. an iron sword's 8 damage does 4 to a werewolf, the silver dagger's 20; a plain arrow 4, a silver arrow 20; silver stops its healing; slain with silver it earns Silver Lining;
  2. a werewolf is gone within a second when it isn't a full-moon night, and stays when it is;
  3. wolfsbane in hand or within reach wards a player, beyond reach doesn't; a werewolf keeps hunting a villager with nothing in hand, and drops one holding a sprig and won't attack them; a warded player is left alone and earns Not Tonight;
  4. no werewolf comes past the cap near a player; grass is woodland floor for one to step onto, stone isn't;
  5. the recipes, loot tables, advancements and tags load.
- `WerewolfClientGameTests` takes screenshots: wolfsbane wild and potted in a spruce clearing, the rug before a fire with the silver dagger, arrows and pelt framed, and two werewolves in the clearing on the full-moon night.

Not run: a werewolf hunting a player in play, which no automated test watches from start to end; a two-client dedicated-server playtest.

### Flying broomstick verification

Not run yet: CI has not built this branch. The planned checks are:
- `BroomstickGameTests`, 5 game tests:
  1. forward flies along the look, climbing when looking up; it slows with nothing pressed and faster braking; jump climbs; strafe drifts; held forward it tops out under its top speed, a quarter faster in a witch hat; dry, it can't climb or speed up and sinks no faster than it may; the server's distance check allows a broom's top speed and no more;
  2. the item lays out the broom and seats its user (Up and Away), used up; a rider can't lay out another; a dry broom is only laid out;
  3. ointment anoints a broom up to 6 minutes, returning bottles, and no further; sneak-use takes it back with its charge; a blow breaks a riderless broom into its item, keeping its charge, but not a ridden one;
  4. ridden in the air, the server burns its charge until it is dry; climbing while dry throws its rider off, slow falling;
  5. its recipe, advancements and entity load, and a new broom holds one ointment.
- `BroomstickClientGameTests` takes screenshots: brooms by a cauldron of flying ointment (one hovering, one dry on the grass, the item framed), the player in a witch hat riding one, from behind and the front, and at midnight.

Not run: flying it by hand, which no automated test can do (the client test's rider has no keys pressed); a two-client dedicated-server playtest.

### Squirrels verification

Not run yet: CI has not built this branch. The planned checks are:
- `SquirrelGameTests`, 5 game tests:
  1. a squirrel goes for an acorn lying several blocks off and takes it, and the acorn is gone from the ground;
  2. a squirrel on grass buries its acorn, one on stone keeps it; open earth has room for an oak to sprout, stone has none, and nor has earth beside a log;
  3. an acorn used on the top of grass plants an oak sapling and is used up; on stone, nothing;
  4. acorns and chestnuts are squirrel food, wheat isn't; two squirrels in love breed a kit, earning Nuts About Squirrels;
  5. the roasting recipes and the advancement load, and roasted chestnuts are squirrel food.
- `SquirrelClientGameTests` takes screenshots: in an oak wood, a red squirrel sitting on a stump with an acorn in its paws, a grey one and a kit with acorns lying about, up close and from further off; then acorns and roasted acorns in frames by a smoker.

Not run: squirrels burying acorns over a long time in play, and a sapling sprouting from one (a 1 in 4 chance, which no test waits for); a two-client dedicated-server playtest.

## World and event applicability
- A cauldron brews hexes in any dimension, all year. Nothing is seasonal.
- The hex ingredients come from the Overworld: brown mushrooms, beans, and phantoms.
- Werewolves come only to the Overworld's forests and taiga, on full-moon nights. Wild wolfsbane generates only in newly generated chunks of those biomes; existing chunks don't get it, but a sprig brought in can be planted anywhere.
- A broom flies in any dimension. Over the Moon is earned only in the Overworld. Nothing is seasonal.
- Squirrels come only to the Overworld's forests and taiga, by day, all year. Acorns drop from oak and dark oak leaves anywhere, and plant on any grass or dirt.

## Rollout and open questions
- A hex brew can't be drawn by hoppers or dispensers. Bottles are filled by hand.
- Breaking a cauldron loses its brew, as before.
- How big a giant is (×1.6) and how long each effect lasts are open to balance review once played. A giant is easier to hit, which is the trade for its reach.
- Mobs aren't affected: a splash or lingering hex is not made.
- Werewolves don't break doors or blocks, and don't turn players into werewolves; there is no curse.
- A werewolf ignores wolfsbane once it isn't hunting; the ward is about who it hunts and where it may come out, not where it may walk.
- How often they come and how hard they hit are open to balance review once played.
- A broom carries one rider and nothing else; it can't be led, put in a minecart or flown by mobs.
- A broom collides with blocks by its own small box, and stops moves that would put its rider's head in a block. Diving into a block is not slowed further than vanilla's collision.
- The top speed and the 2-minute charge are open to balance review once flown. A rider who loses connection mid-flight stays on their broom where it was.
- A squirrel buries every acorn it gathers; it doesn't dig them up again or keep a store. A squirrel can't be tamed or led by anything but nuts.
- How often squirrels come and how often a buried acorn sprouts are open to balance review once played.
- A shrunk player's grace is 5 seconds at a time. A player who logs out shrunk under a low ceiling stays shrunk until there is room.
