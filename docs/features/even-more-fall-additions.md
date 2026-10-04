# Even More Fall Additions

Status: hex brews (addition 21), the flying broomstick (addition 22), full-moon werewolves (addition 23), squirrels and acorns (addition 24), the Pumpkling (addition 25), the fall fair midway (addition 26) and the Ferris wheel (addition 27) are implemented in source and pass CI's game tests. None is yet played by hand. Additions 28 to 30 are planned and not yet built. The Build workflow compiles what exists; CI's game tests and client screenshots are recorded below.
Proposal issue: none. The owner asked for these directly on 3 October 2026 ("Lets do even more fun fall and halloween stuff 10 well thought out good additions"). They follow the [more fall additions](more-fall-additions.md) and are numbered on from them. Each one is its own pull request, stacked on the one before, after the graveyard pack:
21. hex brews: the Bubbling Cauldron's brews, bubbling over a fire, take a hex ingredient and become the Shrinking Draught, the Giant's Draught or Flying Ointment;
22. the flying broomstick: a witch's broom anointed with Flying Ointment, ridden and steered by looking;
23. full-moon werewolves: they come out of the woods only on full-moon nights; wolfsbane wards them off and silver hurts them;
24. squirrels and acorns: squirrels in the woods gather and bury acorns, and some grow into oaks; acorns plant oaks and roast;
25. the Pumpkling: a carved pumpkin woken by a wisp or ectoplasm into a pet that wears its face, follows you and keeps crows off your crops;
26. the fall fair midway: a five-block High Striker rung with a Carnival Mallet, Ring Toss, and plush prizes;
27. the Ferris wheel: a fairground big wheel standing over its booth, eight cars of two seats, turned by kinetic power (one player can crank it for their friends). It takes the place of the planned ghost-train dark ride, which the Haunted Hayride and the Jump-Scare Trap already cover;
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

The Pumpkling is Discovery tier: a pumpkin carved with the Carving Knife, and a spark to wake it. A Wisp in a Jar (caught on Halloween nights) or a bottle of Ectoplasm (caught from a restless spirit at a grave, any night of the year) will do.

Squirrels and acorns are Discovery tier: squirrels come to any player in Overworld woods by day, and oak and dark oak leaves drop acorns (as they drop apples). Roasting needs a furnace, smoker or campfire.

The flying broomstick is Discovery tier too: the Witch's Broom (two sticks and a wheat), one Flying Ointment and two feathers.

The fall fair midway is Discovery tier: the High Striker is planks, two redstone lamps and a bell; its Carnival Mallet a log, two red wool and two sticks; Ring Toss six glass bottles over wooden slabs; and four Toss Rings two sticks and a string. The plushes are only won.

The Ferris wheel is Discovery tier to build (2 redstone lamps, 4 iron bars, an iron block and 2 wool) and turns on kinetic power, so it comes with the first shafts: a hand crank (planks and an iron shaft) turns it at full speed.

Primary specialty and supported player role: witchcraft and exploration. A witch brews hexes for others:
- **Shrinking:** gets a player into one-block gaps, under low ceilings and into cramped caves, and makes them a smaller target.
- **Giant:** gives a block more reach and half a block more step, for building and climbing, but makes the player a bigger target.
- **Flying ointment:** gives a safe fall from a cliff or a tower.

Draughts stack to 16 and keep, so a witch can trade them.

Werewolves are for fighters and hunters: a monthly danger in the woods, beaten by planning (wolfsbane about the homestead, silver in hand), and a reward in pelts. They give silver, until now only a cable metal, a use of its own.

The Pumpkling is for carvers and farmers: a pet that wears a face its owner carved, keeps crows off the crops it sits by, and can be lit to guard further. A carver can carve faces for others to wake.

Squirrels and acorns are for foresters, cooks and anyone who likes animals: acorns are an early snack and another way to plant oaks, and squirrels slowly plant the woods they live in. Tame-minded players can breed them with nuts.

The midway is for builders of fairs and anyone who likes a game: a showpiece that lights up when it's struck, a test of timing (a fully charged swing, better still a falling one) and aim (a soft toss over a bottle's neck from three blocks off), and a shelf of plush prizes to win, trade and decorate with. A fair needs someone to build it; anyone can play.

The Ferris wheel is for fair builders and engineers: the showpiece of a fair, real-life sized, and a first use for kinetic power that isn't a machine. One player cranks while friends ride; riding all the way round with someone beside you is a milestone for two (Two to a Car).

The flying broomstick is a witch's way to travel: slower than elytra but found long before them, good for scouting, crossing ravines and reaching rooftops, and fuelled by the ointment a witch brews.

None of this depends on the Halloween event: a cauldron brews hexes and a broom flies all year, werewolves come on every full moon, squirrels live in the woods all year, a Pumpkling can be woken with ectoplasm any night, and the midway plays all year.

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

### Squirrels and acorns
1. **Where and when:** squirrels live in the Overworld's **forests and taiga** (biome tag `jugcraft:squirrel_habitat`). By day, every 20 seconds, for each player there's a 25% chance one or two come out 20 to 40 blocks away, onto open earth. None come while 6 are within 64 blocks of the player or 30 are in the world, nor while mobs don't spawn.
2. **The squirrel:** small and quick (3 hearts, a third of a block long), **red or grey**, half and half. A slim body, a round head with tufted ears, and a great bushy tail that streams out as it bounds along and curls up over its back when it sits.
   - It **climbs tree trunks**: to a squirrel, a log it runs into is a ladder. It takes no fall damage.
   - It bolts when hurt, and looks round at players nearby.
3. **Acorns:** oak and dark oak leaves drop an **Acorn** 5% of the time they are broken or decay, as they drop apples.
   - **Plant one** on the top of grass, dirt or any other earth an oak sapling grows on, with open air above it, and it's an **oak sapling**.
   - **Roast it** in a furnace, smoker or campfire: **Roasted Acorns**, a snack (3 hunger).
   - It composts, and it counts as a seed (`c:seeds`).
4. **Gathering:** a grown squirrel with empty paws goes for an acorn lying within 10 blocks, takes it and carries it about in its forepaws. After 10 seconds it **buries it** in the earth it stands on (anything an oak sapling grows on), with a scatter of dirt. One in four buried acorns sprouts there as an **oak sapling**, if no sapling or log stands within 3 blocks. On stone it keeps its acorn until it finds earth. So squirrels slowly plant the woods they live in, and a player who drops acorns near them gets some of them planted.
5. **Nuts:** acorns, chestnuts and roasted chestnuts (item tag `jugcraft:squirrel_food`) tempt squirrels to follow, and breed two into a **kit**, red or grey after either parent. Breeding them earns **Nuts About Squirrels**.
6. **Drops:** a squirrel drops only the acorn it was carrying.

### The Pumpkling
1. **Wake one:** use a **Wisp in a Jar** or a bottle of **Ectoplasm** (item tag `jugcraft:pumpkling_sparks`) on a hand-carved pumpkin with a face cut in it (any of the four kinds). The pumpkin hops up as a **Pumpkling**, facing the way it faced, wearing the face you carved, lit if it was lit. The bottle comes back empty. Waking one earns **Little Jack**. A blank pumpkin doesn't wake, and nobody can wake one in adventure mode.
2. **The Pumpkling:** a pumpkin on two stubby twisted-vine legs, with leafy vine arms and a curling tendril on top. It hops along as it walks, swinging its arms, and turns its pumpkin to look about. It has 8 hearts.
3. **It's yours:** it follows whoever woke it, comes to them at once from more than 16 blocks off, and otherwise potters about. Use it with an empty hand to have it **sit and stay** (it squats with its legs out), as you would a tame wolf; again to have it follow.
4. **Light it:** use a **torch** on it and its face glows, at full brightness; a **soul torch** lights it blue. Use another torch on a lit one to take its torch back out.
5. **It guards crops:** crows keep away from crops near it, as from a scarecrow wearing that head: 8 blocks unlit, 12 lit. A sitting Pumpkling is a scarecrow that stays put; a following one guards wherever you go.
6. **Treats:** anyone can feed it pumpkin seeds, roasted pumpkin seeds, pumpkin pie or candy corn (`jugcraft:pumpkling_treats`) to heal it 2 hearts.
7. **Its owner can't hurt it.** To be rid of it, its owner sneak-uses it with a **glass bottle**: it settles back into its carved pumpkin, which drops face and all, and the bottle fills with the spark that woke it.
8. **Slain** by anything else, it drops its carved pumpkin, face and all; its spark goes out.

Only its owner can sit it, light it, put it out or settle it; anyone can feed it.

### The fall fair midway
1. **The High Striker:** a fairground strength test five blocks tall, placed as one, facing you.
   - It has a red-and-white tower with a brass rail up its front, two lamps to each block of it, and a brass bell under a crown sign on top. Its foot is a strike pad on a painted base.
   - Breaking any part breaks it all; it drops once.
2. **Strike it:** hit the base with a **Carnival Mallet** (left click).
   - The red puck climbs the rail, lighting a lamp at a time with a rising note, as far as the blow was strong. It rests at the top of its climb for a second and a half, then drops back.
   - Strength is how charged the swing is (as an attack's is) times a roll: a full swing is 70% to 100% strong. A **critical** swing (falling, as a critical hit is) is 15% stronger.
   - At 95% or more the puck hits the **bell**: it rings, fireworks spark above, and you win a **prize** and **Ring the Bell**. A plain full swing rings it about one time in six; a falling one about two in three.
   - A swing spends its charge, so swinging fast only taps the puck up a lamp. The puck must be back at the bottom before it can be struck again.
   - Lit lamps give light, more with the bell rung, so a striker in play lights up a fair at night.
3. **Ring Toss:** a slatted, painted crate of nine bottles (green, amber and milk glass), necks up in three rows.
   - **Toss Rings** are thrown softly (they arc), from your hand.
   - A ring that comes down on the crate within a pixel and a quarter of a bottle's neck, thrown from **3 or more blocks** off, is a **ringer**: it settles over that bottle for 3 seconds, and you win a prize and **Ringer!** The ringer is used up.
   - Any other landing drops the ring back on the ground, to be thrown again.
4. **Prizes:** each prize is one plush, one roll of the loot table `jugcraft:gameplay/midway_prize`: Pumpkin Plush (24 in 102), Ghost Plush (24), Bat Plush (20), Black Cat Plush (20), Squirrel Plush (9), Werewolf Plush (4), and the **Jumbo Pumpkin Plush** (1 in 102). Winning one earns **Step Right Up**, and the jumbo plush **Jackpot**.
5. **Plushes** are stuffed felt toys with stitched seams and embroidered faces. Each sits facing whoever places it, and squeaks (with a heart) when squeezed with an empty hand. They are only won, not crafted.

### The Ferris wheel
1. **Raise it:** place the **Ferris Wheel** (its booth) and the wheel rises over it, facing you: two A-frames of lattice steel on concrete footings, the hub 9½ blocks up, two red trussed rims 6 blocks round with 32 spokes, and **eight cars** hanging between the rims, in pumpkin, cranberry, mustard and spruce, each with a striped canopy, a tufted bench for two and a brass grab bar. It is about 15 blocks across, 16 high and 3 deep, and it needs that space clear (air, grass or flowers); otherwise it says so and isn't placed.
2. **The booth** at its foot is the loading platform: painted panels, a plank deck with a safety edge, and the operator's controls (a brass lever, a speed gauge and two buttons). It is the wheel's only real block: break it and the wheel comes down, setting any riders on the ground.
3. **Turn it** with kinetic power: put a hand crank, shaft or gearbox against the booth, as you would a machine. **12 KE a tick** turns it at full speed, **once round in 40 seconds**; less turns it slower in proportion. It eases up to speed in 2 seconds and coasts to a stop in 3 when the power stops. A **hand crank** (16 KE a tick) turns it at full speed, five seconds a crank, so one player can crank for their friends; an engine turns it all day.
4. **Ride it:** use the booth to climb into the car at the bottom, beside whoever is in it. If that car is full, wait for the next. You sit in your car as the wheel carries you round, and look where you like.
5. **Get off** (sneak) whenever you like: you're set down on the ground in front of the booth, however high your car was.
6. **Lights:** 128 bulbs on the rims and spokes light up while it turns or anyone rides.
7. **Jams:** if a block stands where a car is about to go, the wheel stops and says it is jammed, until the block is cleared. It never carries a rider into a block.
8. **Advancements:** **Round and Round** for riding all the way round; **Two to a Car** for riding all the way round with someone in the seat beside you.

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
- Pumpkling, input producer: pumpkin carving (the Carving Knife and stencils, any of the four pumpkins), will-o'-wisps (Wisp in a Jar) or ghost hunting (Ectoplasm), torches, and treats from the pumpkin patch and the candy kitchen.
- Pumpkling, output consumer: crop protection (crows and scarecrows); a companion that shows off a carving.
- Pumpkling, entry path: everything is Overworld and early: a pumpkin, a knife, and a spark. Ectoplasm is caught from a restless spirit at any grave on any night, so the season isn't needed.
- Pumpkling, required vs optional: optional; nothing is gated behind it. A carver can carve faces for others, and anyone can wake their own.
- Flying broomstick, input producer: the Witch's Broom (the witch's cottage decorations), Flying Ointment (hex brews), feathers, and the witch hat costume for speed.
- Flying broomstick, output consumer: travel and exploration; the full-moon night (mooncakes, fairy rings) for Over the Moon. Every witch's cottage piece now has a use: the cauldron brews the ointment that flies the broom.
- Flying broomstick, entry path: everything is Discovery tier; the first broom comes charged. Nothing needs the End.
- Flying broomstick, required vs optional: optional. A broom flies one player; anyone can anoint anyone's broom.

- Midway, input producer: planks, logs, sticks, string, red wool, glass bottles, redstone lamps (redstone and glowstone) and a bell (found in villages).
- Midway, output consumer: decoration (plushes, the lit striker), trading (plushes are rare prizes), and a reason to gather for a fair.
- Midway, entry path: everything is Overworld and early; a bell is found in villages.
- Midway, required vs optional: optional. Nothing is gated behind it. One player can build the fair and others play it.
- Ferris wheel, input producer: iron (bars and a block), redstone lamps, wool; and kinetic power from the tech branch (a hand crank, steam engine, electric motor or flywheel, through shafts, gearboxes and belts).
- Ferris wheel, output consumer: a fair's showpiece and a ride; it makes nothing. It is the first use of kinetic power that isn't a machine.
- Ferris wheel, entry path: everything is Overworld and early: a hand crank is planks and an iron shaft (the machines feature).
- Ferris wheel, required vs optional: optional. Nothing is gated behind it.

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
- **Squirrels and acorns:**
  - Units: a squirrel has 6 health and moves at 0.32 (a rabbit: 3, 0.3). Acorns drop at 0.05 from oak and dark oak leaves (vanilla's apples: 0.005 at no Fortune; saplings 0.05). Roasted acorns: 3 hunger, 0.4 saturation modifier (roasted chestnuts: 4, 0.6); 200 ticks in a furnace.
  - No gain: one acorn plants one sapling. A squirrel buries one acorn and, a quarter of the time, one sapling sprouts; the rest are lost. Roasting is one for one, and nothing turns roasted acorns back. Oak trees already drop saplings in vanilla, so acorns add a way, not a loop.
  - Automation: leaves that are broken or decay drop acorns, so a tree farm yields them; planting is by hand or by squirrel. Nothing plants acorns from a dispenser.
  - Cost on the server: the spawner tries one spot a player every 20 seconds, by day; it counts squirrels near the player (a 129-block box) and in the level. A squirrel with empty paws looks for acorns within 10 blocks every second or two. One carrying an acorn checks the block under it each tick after 10 seconds, and a sprouting check reads at most 7 × 4 × 7 blocks, once per buried acorn a quarter of the time. Climbing checks one block, only while it is pressed against something.
- **Pumpkling:**
  - Costs: one carved pumpkin and one spark (a Wisp in a Jar or Ectoplasm; the glass bottle comes back). Settling it gives both back (a Pumpkling woken by command, with no spark, gives Ectoplasm). Nothing is made or lost: no loop.
  - Units: 16 health, speed 0.3 (a wolf: 8 and 0.3 untamed); follows past 6 blocks, stops at 2.5, comes to its owner past 16; a treat heals 4 health. Guard radius 8 or 12 blocks, as a scarecrow's head (`Scarecrows.HEADED`, `LIT`).
  - Automation: none. Crows are the only thing it affects.
  - Cost on the server: a following Pumpkling paths to its owner about once a second while more than 2.5 blocks off, and when it comes to them tries each spot two or three blocks from them (40 at most, a block up or down), once a second at most. Crows ask about guards in an entity box around a crop; Hay Golems and Pumpklings now answer through one interface, `CropGuard`.
- **Midway:**
  - Costs: the striker is 6 planks, 2 redstone lamps and a bell; the mallet a log, 2 red wool and 2 sticks (a wooden tool: 59 uses, 1 damage over the hand, and slow: 0.6 swings a second, so a full charge takes about 1.7 seconds); Ring Toss 6 glass bottles and 3 wooden slabs; 4 rings 2 sticks and a string.
  - Units: lamps 8 (two a part), the bell the 9th step; up a lamp every 2 ticks, a rest of 30 ticks, down a lamp a tick. Strength: charge × 0.7 to 1.0, + 0.15 critical; 0.95 rings; less climbs that share of 9, at least 1 lamp. Ring Toss: necks at 3.5, 8 and 12.5 pixels across each way; a ringer within 1.25 pixels of one, from 3 blocks or more; a ringer stays 60 ticks. A ring is tossed at 0.75 blocks a tick.
  - Prizes cost a full swing and luck (a ring in six plain, two in three falling, every 3 seconds at best: the puck's climb and fall), or a ringer (a quarter string, and aim). Plushes are decoration and lead to nothing else, so there is no loop; the jumbo plush is 1 prize in 102.
  - Automation: none. Only a player's left click strikes, only a thrown ring lands, and the prize goes to the player who struck or threw.
  - Cost on the server: a struck striker sets its five blocks once a step (about 20 steps a strike); a ring checks the block it hits once. No ticking block entity: the striker's base runs on scheduled ticks only while its puck moves.
- **Ferris wheel:**
  - Costs: 2 redstone lamps, 4 iron bars, an iron block and 2 wool (any colour); and its power, all the while it turns.
  - Units: 12 KE a tick for full speed (2π/800 radians a tick, a turn in 800 ticks); it takes no more than 12 KE a tick from all sources, and shares a network's power as any machine does. Up to speed in 40 ticks, down in 60. Hub 9.5 blocks up, cars 6 blocks round, 8 cars of 2 seats; a rider sits 1.75 blocks under their car's pivot, 0.34 either side of its middle. Boarding within 4 blocks of the booth.
  - The power goes into the turning: nothing comes back out, so there is no loop. It gives nothing but the ride and two advancements.
  - Automation: none to speak of. Any kinetic source turns it; only players ride it.
  - Cost on the server: while it turns, a wheel checks 4 points of each of its 8 cars for blocks each tick (32 block reads) and counts its riders' way round; it syncs its speed when it changes and its angle once a second. Standing still it does neither. The booth's drive has no ticker and saves nothing.
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
- **Squirrels and acorns, server authority:** spawning, gathering, burying, sprouting and breeding are decided on the server. Planting an acorn goes through vanilla's item use on a block, needs build rights at the spot (spawn protection, adventure mode), and only happens on the server.
- **Squirrels and acorns, what clients get:** the squirrel's colour (synced entity data) and the acorn in its paws (vanilla's equipment sync), so every player sees a squirrel carrying one.
- **Squirrels and acorns, persistence:** a squirrel saves its colour, the acorn it holds and how long it has held it, and is saved with its chunk like any animal. Saplings are ordinary oak saplings.
- **Squirrels and acorns, IDs (all new):** entity `squirrel`; items `acorn`, `roasted_acorns`; item tag `jugcraft:squirrel_food`, biome tag `jugcraft:squirrel_habitat`; advancement `nuts_about_squirrels`; recipes `roasted_acorns`, `roasted_acorns_from_smoking`, `roasted_acorns_from_campfire_cooking`. Acorns join `c:seeds`, and roasted acorns `c:foods`.
- **Squirrels and acorns, disable behaviour:** with the agriculture feature off, squirrels stop coming, leaves stop dropping acorns and the recipes don't load; everything stays registered, and squirrels already in the world stay.
- **Pumpkling, server authority:** waking goes through vanilla's block use (reach, spawn protection) and needs build rights. Sitting, lighting, putting out, settling and feeding are entity interactions decided on the server, where its owner is known; the client only guesses which use it was, to swing the arm. Its owner's blows are refused on the server.
- **Pumpkling, what clients get:** the pumpkin it wears (with its carving and light, as synced entity data) and whether it sits; the carving draws through the same cached texture as a carved pumpkin's. The owner isn't sent to clients.
- **Pumpkling, persistence:** it saves its pumpkin, its spark, its owner and whether it sits, and never despawns. If its owner is offline or in another dimension it waits where it is.
- **Pumpkling, IDs (all new):** entity `pumpkling`; item tags `jugcraft:pumpkling_sparks`, `jugcraft:pumpkling_treats`; advancement `little_jack`. No new items or blocks.
- **Pumpkling, disable behaviour:** it is part of the agriculture feature and stays registered; Pumpklings already woken keep following.
- **Flying broomstick, server authority:**
  - As with a boat or a horse, the pilot's client flies the broom and sends its moves, and vanilla's vehicle checks run on the server. The broom has no gravity, so vanilla's floating kick doesn't apply.
  - The server burns the charge itself, and every 20 ticks compares where the broom is with where it was. If it went further than a broom can fly (three times its top speed, for lag), or climbed more than 2 blocks while dry since the last check, the rider is thrown off ("The broom bucks you off") and the broom is the server's again: it hovers or sinks.
  - Laying out, anointing, taking back and breaking are decided on the server. Use goes through vanilla's entity interaction, whose reach check runs first.
- **Flying broomstick, what clients get:** the broom's position and charge (synced entity data), so everyone sees it fly and a dry broom's grey straw. The pilot's keys stay on their own client.
- **Flying broomstick, persistence:** a broom saves its charge. Riding is vanilla's: a player who logs out riding is back on their broom when they log in. The item keeps its charge in `jugcraft:broom_charge`.
- **Flying broomstick, IDs (all new):** item and entity `flying_broomstick`; data component `jugcraft:broom_charge`; advancements `up_and_away`, `over_the_moon`; the recipe `flying_broomstick`.

- **Midway, server authority:** a strike is a left click the server sees itself (Fabric's block-attack event), checked for build rights there (spawn protection), within 6 blocks, the mallet in hand and the puck at rest; the swing's charge and whether it is falling come from the server's own view of the player. A ring's landing is judged where the server's ring came down and where the server saw it thrown from. Prizes are rolled on the server and go straight to the player who won.
- **Midway, what clients get:** the striker's lamps and puck and the ring on a bottle are block states; the thrown ring is an entity drawn as its item.
- **Midway, persistence:** the striker's base saves where its puck is going, whether it is falling and who struck it, so a strike in flight finishes after a restart (a prize then goes only to a striker who is still there). Plushes are blocks; a ringer's ring comes off on its scheduled tick.
- **Midway, IDs (all new):** blocks and items `high_striker`, `ring_toss`, `pumpkin_plush`, `ghost_plush`, `bat_plush`, `black_cat_plush`, `squirrel_plush`, `werewolf_plush`, `jumbo_pumpkin_plush`; items `carnival_mallet`, `toss_ring`; entity `toss_ring`; block entity `high_striker`; item tag `jugcraft:plushes`; loot table `gameplay/midway_prize`; advancements `step_right_up`, `ring_the_bell`, `ringer`, `jackpot`; recipes `high_striker`, `carnival_mallet`, `ring_toss`, `toss_ring`.
- **Midway, disable behaviour:** with the agriculture feature off the recipes don't load and the striker can't be struck; everything stays registered, and plushes stay where they are.
- **Ferris wheel, server authority:** placing the booth goes through vanilla's block placement (reach, build rights); the server checks the wheel's space. Boarding is the booth's use, decided on the server: within 4 blocks, not already riding, a seat free in the car at the bottom. Where riders sit, when the wheel turns and the advancements are all the server's; clients only draw it.
- **Ferris wheel, what clients get:** the wheel is an entity: its facing, speed, angle (once a second, as a correction; the client turns it in between), whether it is jammed, and who sits in each of its 16 seats (by entity id). The booth is a block with a facing.
- **Ferris wheel, persistence:** the wheel saves its facing, angle and speed; riders don't stay seated across a save (players aren't saved as passengers). If its booth is gone (an explosion, a command) the wheel takes itself down within a second; if the wheel is gone, using the booth raises it again.
- **Ferris wheel, IDs (all new):** block and item `ferris_wheel` (the booth), entity `ferris_wheel` (the wheel), block entity `ferris_wheel`; advancements `round_and_round`, `two_to_a_car`; recipe `ferris_wheel`.
- **Ferris wheel, disable behaviour:** with the agriculture feature off its recipe doesn't load; a wheel already standing keeps turning if driven. With the machines feature off there are no hand cranks to make, so it turns only from another kinetic source.

## Dependencies and assets
- No new dependencies.
- Textures are drawn by code in `tools/decor4_textures.py`: each hex brew's liquid, the three draughts and the two effect icons (a mouse-sized figure and a towering one).
- The cauldron's models for each hex and level, the item models, names, tooltips, the message, the tags and the advancements come from `tools/decor4_data.py`. The numbers come from `HEX` in `tools/agriculture.py`.
- `tools/check_mod_data.py` compares them with Java.
- Sounds and particles are vanilla's: a brewing stand's brew pitched low, a bottle filling, witch's sparkles and enchanting glyphs.

- Werewolves: no new dependencies.
  - `tools/werewolf_model.py` holds the model's parts and boxes as data. `client/WerewolfModel.java` builds the same boxes, and the checker compares the two box by box.
  - `tools/werewolf_textures.py` paints each kind's fur from those boxes, face by face, at four times the model's texture size (512 × 512 for its 128 × 128 layout), with `tools/fur_paint.py`: shaded faces, fur in locks with fine hairs over them, cut-out points for the shaggy mane, fangs and claws. It paints the glowing eyes alone too; each kind's rug and pelt at 64 × 64; and wolfsbane, the dagger and the arrow at 16 × 16.
  - The look follows reference pictures the owner gave (a white, a dark brown and a dark grey werewolf): hunched, long-snouted, open-jawed, tall-eared, long-clawed, wolf-legged, bushy-tailed, with red or yellow eyes. Every pixel is drawn by code; nothing is traced or copied from them.
  - `tools/werewolf_data.py` writes the flower's and rugs' models, the items, words, loot, tags and worldgen. The numbers come from `WEREWOLF` and `WOLFSBANE` in `tools/agriculture.py`, which the checker compares with Java. Sounds are vanilla's (a wolf's howl, growl, whine, hurt and death, pitched low), and particles vanilla's (electric sparks, large smoke).
- The Pumpkling: no new dependencies. `tools/pumpkling_textures.py` draws its vine body (32 × 32, laid out as `client/PumpklingModel.java`'s boxes); the pumpkin is the carved pumpkin's own item model, with the carving drawn over it as the Hay Golem's head is (`client/PumpklingRenderer.java`, using `CarvingTextures`). `tools/pumpkling_data.py` writes its name and tags. The numbers come from `PUMPKLING` in `tools/agriculture.py`, which the checker compares with `Pumpkling.java`. Sounds are vanilla's (an amethyst chime as it wakes, wood as it's hurt), particles vanilla's (soul fire, hearts).
- Squirrels and acorns: no new dependencies. `tools/squirrel_textures.py` draws the red and grey squirrels (32 × 32, laid out as `client/SquirrelModel.java`'s boxes: soft fur, a cream belly, a bushy tail paler at its tip), the acorn and roasted acorns. `tools/squirrel_data.py` writes the acorn's item model, words and tags; roasted acorns, the recipes and the advancement come from `ITEMS`, `COOKING` and `ADVANCEMENTS` in `tools/agriculture.py`. The numbers come from `SQUIRRELS` in `tools/agriculture.py`, which the checker compares with Java. Sounds are a fox's, pitched high; particles are the earth's own block dust.
- The flying broomstick: no new dependencies. `tools/broom_textures.py` draws the item (a broom aslant, purple cord, fanned straw, sparkles) and the entity's pale grain, which `client/BroomstickRenderer.java` tints for the ash handle, the cord and the straw (greyer when dry) on boxes of its own. `tools/broom_data.py` writes the item model and words. The numbers come from `BROOMSTICK` in `tools/agriculture.py`, which the checker compares with `Broomstick.java`. Sounds and particles are vanilla's: a phantom's flap on take-off, a brewing stand when anointed, witch's sparkles and smoke.

- The fall fair midway: no new dependencies.
  - `tools/midway.py` holds the numbers (`HIGH_STRIKER`, `RING_TOSS`, `PLUSHES`), the advancements and the recipes; the checker compares them with Java.
  - `tools/midway_data.py` writes the models: the striker's base and pad, its tower with each lamp lit and unlit and the puck where it is, the bell rung or not; the crate with a ring over each bottle in turn; each plush. It also writes the blockstates, words, loot and tags.
  - `tools/midway_textures.py` paints every texture at 64 × 64 (docs/ART_DIRECTION.md, "High resolution"), with `tools/fur_paint.py`: painted boards, lacquer and gold pinstripes, brass, glowing lamps, glass bottles, felt with stitched seams and embroidered faces, and the three item icons.
- The Ferris wheel: no new dependencies.
  - `tools/ferris_wheel.py` holds the numbers (`FERRIS_WHEEL`), the cars' colours, the advancements and the recipe; the checker compares them with Java.
  - `tools/ferris_wheel_data.py` writes the booth's model and the quads the client draws the wheel from (`assets/jugcraft/ferris_wheel_quads.json`): the frame; a sixteenth of the wheel (both rims' outer and inner rings, their ties and a spoke), drawn sixteen times round; the hub plates; a car's pivot bar; a sixteenth's bulbs; and a car in each colour. The same boxes make the preview the art was checked against.
  - `tools/ferris_wheel_textures.py` paints every texture at 64 × 64 (docs/ART_DIRECTION.md, "High resolution"), with `tools/fur_paint.py`: lattice steel cut out between its bracing, cream enamel with rivets, red rims with a gold pinstripe, a brass sunburst hub, bulbs, four cars with leaf cartouches, striped canopies and scalloped valances, tufted leather, planks, the booth's panels, deck, controls and gauge, and the item. All original; nothing is copied or traced.
  - Sounds and particles are vanilla's: an anvil's clang pitched up for the strike, note-block bits rising with the puck, a bell, glass and an amethyst chime for a ringer; crits, fireworks, hearts and happy-villager sparks.

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

Locally, `python3 tools/check_mod_data.py` passes with 1071 IDs. It now also compares each kind's tier, attributes and abilities, the pelt tables and haunts, and every box of the model with `tools/werewolf_model.py`. `python3 scripts/check_repository.py` passes.

Then the textures were painted again at four times the detail (512 × 512, docs/ART_DIRECTION.md, "High resolution"): fur in locks and strands, ragged edges, pointed fangs and claws. Actual results:

| Check | Result |
| --- | --- |
| `./gradlew build` on `d979eaf1` (run [37153477545](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37153477545)) | Pass |
| Game tests, same run, including the 13 in `WerewolfGameTests` | **All pass** |
| Client test, same run | **Passes**, but its picture showed the shadow werewolf red: the test had stood it in a spruce trunk, where it suffocated and flashed with hurt. Its texture is dark grey. `ebf5b4ec` stands it clear. |
| `./gradlew build` on `ebf5b4ec` (run [37154692054](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37154692054)) | Pass |
| Game tests on the headless server, same run: 619 in total | **All 619 pass** |
| Client test, same run (all three shards) | **Passes** |

The screenshots in [the guide](../branches/AGRICULTURE.md#full-moon-werewolves) come from run 37154692054: wolfsbane in the clearing; the silver framed above the fire with the three rugs (brown, snow and shadow); the snow werewolf facing the camera with the shadow werewolf snarling beside it; and the three kinds in the clearing. They are small software-rendered previews; the fur's finer detail shows best up close in a real client.

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

### Squirrels verification

Actual results (3 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/fall-24-squirrels` stacked on full-moon werewolves:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares `SQUIRRELS` with `Squirrel` and `Squirrels`: the squirrel, its gathering, burying and sprouting, spawning and acorns from leaves; and checks the entity, acorn and roasted acorns are registered and drawn, and their words, tags, recipes, advancement and textures) | Pass, 1054 IDs |
| `./gradlew build` on `e71302ed` (Build workflow run [37148025075](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37148025075)) | Pass |
| Game tests on the headless server, same run: 606 in total, 5 of them new here (`SquirrelGameTests`) | **All 606 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `e71302ed` (all three client shards of that run) |
| Again on `da7b7277`, with the werewolves' new art merged in (run [37153498026](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37153498026)): data check, build, game tests and client test | Pass, 1073 IDs; **all 624 game tests pass**; all three client shards pass |

The 5 new game tests (`SquirrelGameTests`):
1. a squirrel goes for an acorn lying several blocks off and takes it, and the acorn is gone from the ground;
2. open earth has room for an oak to sprout, stone has none, and nor has earth beside a log; a squirrel on grass buries its acorn, one on stone keeps it;
3. an acorn used on the top of grass plants an oak sapling and is used up; on stone, nothing;
4. acorns and chestnuts are squirrel food, wheat isn't; two squirrels in love breed a kit, earning Nuts About Squirrels;
5. the roasting recipes and the advancement load, and roasted chestnuts are squirrel food.

Found by CI and fixed before this record:
- The first run failed in two of these tests at once, on grass: burying and planting asked for `minecraft:dirt`, which grass didn't pass in 26.3. They (and where squirrels come out) now ask whether an oak sapling could grow there (`Werewolves.woodlandFloor`). The same check had kept werewolves off grass; that was fixed on the branch below.
- The bury test also checked for room to sprout after a squirrel had buried an acorn, which sprouts a sapling one time in four and leaves no room near it: it now checks the room first.

The client test's first screenshots showed a squirrel at rest with its tail standing straight up behind its head, a column from the front. At rest the tail now sweeps up and back and its tip curls forward over the back (`2845d4ad`), and the test adds a side-on picture. The screenshots in the [agriculture guide](../branches/AGRICULTURE.md#squirrels-and-acorns) come from run 37153498026: three squirrels by a stump with acorns about (one on the stump holding an acorn), one side on with its tail curled over its back, and acorns and roasted acorns in frames.

Not run: squirrels burying acorns over a long time in play, and a sapling sprouting from one (a 1 in 4 chance, which no test waits for); a two-client dedicated-server playtest.

### Pumpkling verification

Actual results (3 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/fall-25-pumpkling` stacked on squirrels and acorns:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares `PUMPKLING` with Java: health, speed, following, the treats' heal, the guard; and checks the entity, its tags, advancement and textures) | Pass, 1073 IDs |
| `./gradlew build` on `450e3ab9` (Build workflow run [37156381277](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37156381277)) | Pass |
| Game tests on the headless server, same run: 630 in total, 6 of them new here (`PumpklingGameTests`) | **All 630 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `450e3ab9` (all three client shards) |

The 6 new game tests (`PumpklingGameTests`):
1. a Wisp in a Jar used on a lit carved pumpkin with a face wakes a Pumpkling there, owned by its waker, wearing the same face and lit, gives back a glass bottle and earns Little Jack; ectoplasm doesn't wake a blank pumpkin, nor for a player in adventure mode;
2. its owner, with an empty hand, has it sit; a stranger can't change that; its owner has it get up; it finds a spot beside its owner;
3. unlit it guards 8 blocks, lit 12, and a crop six blocks off is guarded;
4. a stranger can't light it; its owner's torch lights it, another torch takes its torch back, an empty hand has it sit rather than lighting it, a soul torch lights it blue; a stranger's pumpkin seeds heal it; its owner's blow doesn't hurt it; a glass bottle settles it, filling with its spark, and its pumpkin drops with its face;
5. slain by a stranger, it drops its pumpkin with its face;
6. the advancement and tags load.

Found by CI and fixed before this record:
- Comparing an item stack with a block doesn't compile in 26.3; the tests and the drop compare with the carved pumpkin's item.
- A Pumpkling's tempt goal needs the `tempt_range` attribute, which a plain mob's attributes lack; it now has 10 blocks.
- The sit test stood its players beyond entity reach (3 blocks); they now stand within it.
- The sit test still failed: the owner's use came back PASS. Its report showed why: test players start out holding the Creative Tower Guide, so the "empty hand" held a book. The tests now empty their hands first. Along the way the controls changed to a tame wolf's: an empty hand sits it (it was a sneaking, empty-handed use), and a torch used on a lit Pumpkling takes its torch back (it was an empty hand).

Found later: Build workflow run [37158111759](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37158111759), on the piñata's branch stacked above this one, failed the sit test's last step, "It finds a spot by its owner". A Pumpkling coming to its owner tried 12 spots at random within three blocks of them. In the test its owner stands near the edge of an 8 × 8 floor, where only 16 of the 49 spots have ground, so about one run in a hundred all 12 missed; in play it would have failed the same way by a cliff or a wall. It now tries every spot two or three blocks off, in a fresh random order, so it fails only when none has room. With that change (`94e9bd05`), Build workflow run [37158502445](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37158502445) passed: the build, all 630 server game tests and the three client shards.

The client test's screenshots (run 37156381277), in [the guide](../branches/AGRICULTURE.md#the-pumpkling): three Pumpklings in a pumpkin patch wearing three stencils' faces (lit, soul-lit, and unlit sitting) beside a carved pumpkin not yet woken on a hay bale and a Wisp in a Jar; up close; and at nightfall. Its log has no missing model or texture.

Not run: a Pumpkling following its owner about in play (its path-finding), and crows turning from its crops (the guard check is tested directly); a two-client dedicated-server playtest.

### Midway verification

Actual results (3 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/fall-26-midway` stacked on the Pumpkling:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares `tools/midway.py` with Java: the striker's lamps, steps and strengths, the necks and ringer distances, the plushes and prize weights; and checks the blocks, items, entity, tags, loot, advancements and textures) | Pass, 1084 IDs |
| `./gradlew build` on `8c39c033` (Build workflow run [37157678923](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37157678923)) | Pass |
| Game tests on the headless server, same run: 638 in total, 8 of them new here (`MidwayGameTests`) | **All 638 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `8c39c033` (all three client shards) |

The 8 new game tests (`MidwayGameTests`):
1. placed from its item, the striker stands five blocks tall and its base keeps the puck; breaking its fourth block breaks it all and drops it once;
2. a full swing's strongest roll rings the bell, its weakest climbs more than half way; a swing with no charge climbs a lamp; a critical swing rings it from a middling roll, a plain one doesn't;
3. struck to the top, the puck climbs a lamp at a time, reaches the bell, lights the tower, wins its striker a plush, Ring the Bell and Step Right Up, and falls back to the bottom; struck again in flight, nothing happens;
4. a left click with dirt in hand passes and strikes nothing; with the mallet it strikes; a click on the tower isn't a strike; from 8 blocks off, nothing;
5. necks are numbered row by row, and between two is none; a ring between the necks, or tossed from a block off, isn't a ringer; over the middle neck from 5 blocks off it is: it settles over the fifth bottle, wins a plush and Ringer!, and comes off after 3 seconds;
6. a Toss Ring thrown from the hand flies, owned by its thrower, one fewer in the stack;
7. every plush squeaks when squeezed and is in `jugcraft:plushes`; 20 prizes are 20 plushes, one each, given to the winner;
8. the recipes, advancements and prize table load.

`MidwayClientGameTests` takes screenshots: two High Strikers (one half way, one rung), a prize booth under a striped awning, the plushes, and Ring Toss with a ringer; the prizes up close; Ring Toss; and at dusk.

Found by CI and fixed before this record:
- 26.3 names push reactions `IMMOVEABLE` and `POPPED` (not `BLOCK` and `DESTROY`), and `SoundEvents.ANVIL_LAND` didn't compile as a fallback sound. The striker and plushes use the new names; the strike and bell sounds fall back on `WOOD_HIT` and `BELL_BLOCK`, which the mod already uses.
- The client test's awning used a wool constant 26.3 doesn't have; it looks the wool up by ID.
- The first screenshots framed the midway too far off to see the prizes; the cameras moved closer (`8c39c033`).

The client test's screenshots (run 37157678923), in [the guide](../branches/AGRICULTURE.md#the-fall-fair-midway): the prize booth under its striped awning with the plushes on its counter, two High Strikers and Ring Toss on a plank floor; the prizes up close; Ring Toss's crates of bottles; and the midway at dusk. Its log has no missing model or texture.

Not run: swinging a mallet and tossing rings by hand, which no automated test can do; a two-client dedicated-server playtest.

### Ferris wheel verification

Actual results (3 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/fall-27-ferris-wheel` stacked on the midway:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares `tools/ferris_wheel.py` with Java: the hub, radius, cars and seats, the seat's place, the power, speed and reach; and checks the booth, entity, quads, tags, loot, advancements and textures) | Pass, 1085 IDs |
| `./gradlew build` on `c4679261` (Build workflow run [37156864812](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37156864812)) | Pass |
| Game tests on the headless server, same run: 645 in total, 7 of them new here (`FerrisWheelGameTests`) | **All 645 pass** on `c4679261` |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `c4679261` (all three client shards) |
| The same on `27eb6841`: the fixes below, with the midway's branch merged (Build workflow run [37159101161](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37159101161)) | **Pass:** the build, all 645 server game tests and the three client shards |

The wheel's look was checked in a render of its quads before any game run. The first run's screenshots then showed two faults the tests couldn't, fixed in `75fff507`:
- **The wheel drew black**, frame, wheel and cars, though the booth beside it was lit. The wheel entity stands in its booth block, where there is no light, and took its light from there. The frame now takes its light from just above the booth, the wheel from its hub, and each car from where it hangs.
- **A rider's head came up through the canopy:** the view from a car was its canopy from above. A seated player's eyes are about a block over their seat, and the seat was under a block (0.94) below the pivot. The cars' tubs now hang 12 pixels lower, a seat 1.75 blocks under the pivot, with a rider's head under the canopy (checked in a render with two player-sized figures seated). The hub stands at 9½ blocks, a block higher, so the bottom car's floor still clears the booth, a step above its deck. The jam check also looks where the riders' heads are.

The 7 new game tests (`FerrisWheelGameTests`), in a 44 by 44 empty arena:
1. placed from its item where there is room, the booth faces whoever placed it, one wheel stands over it facing the same way, and its drive is there; with a block in its way, a second can't be placed and the item is kept;
2. the booth takes 12 KE in a tick and no more; a real hand crank beside it, cranked for 60 ticks, brings the wheel up to full speed, turned and lit; when the crank stops, the wheel stops;
3. from 10 blocks off a player can't board; within reach they board the car at the bottom, a second sits beside them, a third finds the car full; a rider sits just over the booth; getting off sets them down in front of the booth, and their seat is free again;
4. nothing is in a new wheel's way; a block where a car hangs blocks it; driven, it stays jammed; cleared, it turns again;
5. half way round earns nothing; all the way round earns Round and Round, and with a friend beside you, Two to a Car;
6. breaking the booth takes the wheel down, sets its rider off and drops the booth once;
7. the recipe, advancements and loot load.

`FerrisWheelClientGameTests` takes screenshots: the wheel at a fair, turned by a hand crank, a High Striker and plushes beside it; its foot, the booth and crank close up; the view from a car at the top; and at night, lit.

Its screenshots from run 37159101161, in [the guide](../branches/AGRICULTURE.md#the-ferris-wheel): the wheel lit by the day, its cream lattice A-frames, red rims and the cars in their four colours, beside a High Striker and plushes; its foot, the booth with the cars hanging over it; the view from a car near the top, over its grab bar and between its brass posts to the field below; and the wheel at night, its bulbs lit round both rims. Its log has no missing model or texture.

Not run: riding it in play for a whole turn (the tests turn it directly), and a two-client dedicated-server playtest (two riders in one car, seen from the ground).

## World and event applicability
- A cauldron brews hexes in any dimension, all year. Nothing is seasonal.
- The hex ingredients come from the Overworld: brown mushrooms, beans, and phantoms.
- Werewolves come only to the Overworld's forests, taiga and groves, on full-moon nights. Snow werewolves only where it snows, shadow werewolves anywhere they come (most often in dark woods). Wild wolfsbane generates only in newly generated chunks of those biomes; existing chunks don't get it, but a sprig brought in can be planted anywhere.
- A broom flies in any dimension. Over the Moon is earned only in the Overworld. Nothing is seasonal.
- A Pumpkling can be woken anywhere a carved pumpkin can be placed, all year (with Ectoplasm; wisps come only on Halloween nights). It follows its owner within a dimension, not through portals.
- Squirrels come only to the Overworld's forests and taiga, by day, all year. Acorns drop from oak and dark oak leaves anywhere, and plant on any grass or dirt.

- The midway works in any dimension, all year. Nothing generates in the world.
- The Ferris wheel works in any dimension with room for it, all year. Nothing generates in the world.

## Rollout and open questions
- A Ferris wheel has no collision of its own: players and mobs walk through its frame and cars. Only its booth is solid. Its space is checked when it is placed, and after that it stops for blocks in its cars' way rather than breaking them.
- Getting off a Ferris wheel anywhere sets you down at the booth, as if the operator brought your car down; there is no stepping off at the top.
- Riders don't stay seated across a save or a log-out. A wheel larger or smaller, or with more cars, is not made; its size and speed are open to balance review once played.
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
- A squirrel buries every acorn it gathers; it doesn't dig them up again or keep a store. A squirrel can't be tamed or led by anything but nuts.
- How often squirrels come and how often a buried acorn sprouts are open to balance review once played.
- A Pumpkling doesn't fight, give light to the world (its glow is drawn, not block light) or follow through portals. A giant carved pumpkin can't be woken. It can't be handed to another owner.
- A shrunk player's grace is 5 seconds at a time. A player who logs out shrunk under a low ceiling stays shrunk until there is room.
- The High Striker only takes the Carnival Mallet, and only from a player; a ring is judged by where it lands, not how it flies. How often the bell rings and how near a ringer must come are open to balance review once played.
- Plushes can't be crafted, dyed or combined; a prize booth to put them on is any counter.
