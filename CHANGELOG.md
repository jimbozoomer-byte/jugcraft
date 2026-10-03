# Changelog

Every change to Jugcraft, newest first, grouped by pull request. For what each thing does in game, see [docs/TECH_TREE.md](docs/TECH_TREE.md). For a full inventory with APIs and file locations, see [docs/WHAT_EXISTS.md](docs/WHAT_EXISTS.md).

**Testing so far:** everything compiles in CI, and the in-game tests (from #14 on) pass on a headless server. Nothing has been play-tested in a client or with two players on a dedicated server yet.

## Unreleased

No numbered release yet. Everything below is on `main`.

### Unmerged: Electroplating, batch 34
- **Electroplating Bath:** plates a tool, weapon or piece of armor with an ingot in sulfuric acid, repairing it fully without experience. **Nickel** makes it half as durable again, **silver** gives a sword or axe Smite III, **gold** makes armor count as gold for piglins. Plating again with the same metal repairs it again.
- Advancement, handbook page, tooltips, game test. Record: [electroplating.md](docs/features/electroplating.md).

### Unmerged: Hydroponics, batch 33
- **Hydroponic Bay:** grows a seed or cutting into a harvest every 30 seconds on power and **nutrient solution** (fertilizer in water, from the chemical reactor), giving the seed back. It needs no soil, sunlight or water source, so it works underground or in any dimension. It grows the vanilla crops, cotton and every agriculture crop.
- Advancement, handbook page, game test. Record: [hydroponics.md](docs/features/hydroponics.md).

### Unmerged: Construction chemistry, batch 32
- **Foam Sprayer:** fills up to 12 open blocks (air, water, lava, plants) where you aim, from 16 blocks away, with construction foam from foam canisters (chemical reactor: plastic and ammonia). Bridge gaps, seal caves, stop floods and lava. Respects spawn protection and the walled town.
- **Cement** (calcite or bone block, clay and sand, smelted) sets foam into **concrete**; concrete also crafts from cement, gravel and water. **Blast-proof concrete** (concrete round a rebar) is as blast-proof as obsidian. Both come as slabs and stairs.
- Two advancements, two handbook pages, a game test. Record: [construction-chemistry.md](docs/features/construction-chemistry.md).

### Unmerged: Field chemistry, batch 31
- **Chemical grenades** for hand or launcher: **chlorine** (a cloud that hurts what breathes, through armor), **smoke** (mobs lose their target; players inside can't see), **thermite** (a burning pool that never lights blocks) and the **flashbang** (blinds players, staggers mobs, no damage). None breaks a block; other players only where PvP is on, never party members.
- **Gas mask:** keeps out chlorine, smoke and the flash; the filter wears in gas and is repaired with charcoal. A sealed scuba set also works while it has oxygen.
- **Medicines** from the chemical reactor: **first aid kit** (four hearts, 10 s cooldown), **antidote** (clears harmful effects, keeps good ones), **stimulant** (Speed II and Haste II for a minute, with hunger).
- Three advancements, three handbook pages, five game tests. Record: [field-chemistry.md](docs/features/field-chemistry.md).

### Unmerged: Pneumatic grapple, batch 30
- **Pneumatic Grapple:** a harpoon gun on compressed nitrogen (fill it at the air separation unit or a gas holder; 25 mB a shot). The hook flies up to 32 blocks: in a block it reels you in with no fall damage and a hop at the end; in a mob it drags the mob to you (not bosses or golems, never party members). Use it again to let go.
- Advancement, handbook page, four game tests, a screenshot. Record: [pneumatic-grapple.md](docs/features/pneumatic-grapple.md).

### Unmerged: Refinery upgrades, batch 29
- **Hydrotreater:** diesel + hydrogen → **premium diesel** (a quarter more power everywhere diesel burns) + hydrogen sulfide; gasoline + a tenth bioethanol → **premium gasoline** (448,000 JE a bucket in the gas turbine).
- **Sulfur recovery:** the chemical reactor turns 200 mB of hydrogen sulfide into a sulfur dust, so oil feeds the acid recipes.
- **Heat Recovery Unit:** set against a running diesel generator or gas turbine, it makes 30% of their power again from the exhaust, using water and a little lubricant.
- Two advancements, handbook pages, six game tests. Record: [refinery-upgrades.md](docs/features/refinery-upgrades.md).

### Unmerged: Room for the Retro Game Shop in every village (fix for #53)
- **Every new village gets its Retro Game Shop.** Village houses are built inside their street's plot, and some villages were laid out with no plot big enough for the 9 by 8 shop (roughly one village in thirty, going by CI runs and a model of the placer). Such a village is now laid out again, up to 8 layouts; a world seed still makes the same village.
- The village shop test now generates four rounds of the five village types (20 villages) instead of five. In the last round the shop is withheld from each village's first layout (a test-only switch), so every one of those villages must be laid out again and still get exactly one shop.

### The walled town and Jugs
- **A walled medieval town near the start of every new world**, 170–300 blocks away on the flattest dry ground: about 160 blocks across, with a stone curtain wall, 13 round towers and three gatehouses, a church with two spires, a market square with a fountain and six stalls, a bank, a town hall, and 44 half-timbered houses and shops. It is built into chunks as they load, levelled inside the wall and blended into the land outside.
- **Townsfolk:** 32 named, player-shaped townspeople in 18 original skins. Nobody can hurt them. They include shopkeepers, guards who fight monsters in town, strollers, a priest, the mayor, and decorators who change the town's decor for the seasons, Halloween, the Harvest Feast and December.
- **Protection:** players can't break or place blocks in the town, or use items on them; explosions, fire and pistons can't change it; hostile mobs don't spawn inside the wall. Operators in creative mode are exempt, and `town.protection=off` lifts it.
- **Jugs**, the town's credit, kept per player by the server: the General Store buys farm and mine goods, and the Seasonal Stall, Curiosities and the Florist sell decoration and fun items. **Jug Tellers** (ATMs) in the bank send Jugs to other players. There is no profit loop between the shops.
- Commands: `/jugcraft town`, `/jugcraft jugs`, and for operators `town place`, `town theme`, `jugs give|take`. Record: [walled-town.md](docs/features/walled-town.md).

### Unmerged: Parties finished (from #31)
- **Party screen** on the P key: members with online lights, the leader and you marked; LEAD, KICK, DISBAND or LEAVE; invite by name; accept or decline the latest invite. Every button runs the ordinary `/party` command.
- **Clickable [Accept] and [Decline]** on invites in chat.
- **`/party admin list | kick | leader | disband`** for operators (level 2), on any party.
- **Limits in the server config:** `parties.max_size`, `parties.invite_minutes`, `parties.invites_per_minute` (defaults 8, 5 and 10, as before).
- Commands and the shared party API are @Narvisius's from #31, already on main through #84.
### #53 Pixel Hollows and the Retro Trader
- **Pixel Hollows:** a rare cave biome deep under the driest land, lined with **circuitstone** and lit only by scattered, faintly glowing **pixel crystal clusters**, with an original chiptune hum. It holds 1.5× the usual copper and redstone (and tin). New building blocks: circuitstone, polished circuitstone, circuitstone bricks and the **pixel lamp**; clusters drop **pixel shards**. One mixin adds the biome to the Overworld (Fabric API has no Overworld biome API).
- **Retro Trader:** a villager profession at the new two-block-tall **arcade cabinet**. He sells a **Pixel Hollows Map** (use it to mark the nearest cave), circuitstone, lamps and shards, and buys shards back without any profit loop. Trades are 26.1+ data files.
- **Retro Game Shop:** a small storefront in every new village (one per village, all five village types; not zombie villages), with the cabinet and a villager inside.
- **Loot:** the cluster and cabinet use the 26.x loot format (#34 fixed the other tables on main), and new game tests check ore Silk Touch and Fortune and double slabs.
- **Dedicated-server check (CI):** a real client joins the game's own dedicated server, opens a machine and trades with the Retro Trader over the network, leaves and rejoins; and a world is saved and reopened with its trader, machine contents and cabinet intact. The two-client checklist is in [docs/TESTING.md](docs/TESTING.md#dedicated-server-and-two-clients).
- Seventeen game tests, eight client screenshots, a "Dead Pixels" advancement. Records: [pixel-hollows.md](docs/features/pixel-hollows.md), [retro-trader.md](docs/features/retro-trader.md).

### Agriculture: fall additions 10, face paint (pull request pending, stacked on ghost hunting)
- **Face Paint Kit:** a tin palette and brush, good for 16 faces, that paints one of six designs: a skull, a jack o'lantern, a black cat, a vampire, a witch or a scarecrow. Use it on a friend to paint them at once, hold use to paint yourself, sneak to turn the dial.
- The paint shows on the face for everyone who can see you and lasts until your head goes under water, or you die. A painted face counts as a costume for trick-or-treating and the costume contest.
- The paint is a Fabric data attachment on the player, set on the server and sent to the clients that see them. The checker compares the kit and designs with Java. New server game tests and a client test with a screenshot of each design.

### Agriculture: fall additions 9, ghost hunting (pull request pending, stacked on the corn maze)
- **Restless spirits** rise from gravestones and grave mounds at night (graves now take random ticks), a few at most near, and drift about their graves, unseen.
- **Spirit Lantern:** held in either hand, it reveals every spirit within 12 blocks to everyone near; so does a Revealing candle's glow. A revealed spirit fades into view, moans, and shies away from anyone close, though never out of its haunt, so it can be cornered. Blows pass through it; it fades at dawn.
- **Ectoplasm:** a glass bottle catches a revealed spirit (earning Ghost Hunter). Ectoplasm is the chandlery's new **Ghostly** scent: a Ghostly candle turns the players in its aura invisible, and the wax pot now hands back a scent's bottle.
- All decided on the server; clients are told only when a spirit shows, and draw it. The checker compares the spirits and the lantern with Java. New server game tests and a client test with screenshots.

### Agriculture: fall additions 8, the corn maze (pull request pending, stacked on the Harvest Feast Table)
- **Corn Maze Gate:** choose a size (tiny 7 by 7 to large 19 by 19) and use it holding corn kernels: it carves a new maze, one way through, and plants three-tall **maze corn** along its walls, a kernel a stalk, with a **finish post** at the exit. It never replaces a block.
- **Running it:** walk out through the gate to start the clock and reach the finish post to stop it. The server follows every runner and voids a run for flying, climbing over the corn, leaving the maze, taking too long or a shortcut. The best times go on the gate's board, with a prize ribbon the first time a runner places and the advancement A-maze-ing.
- Maze corn gives back its kernel. The checker compares the maze with Java. New server game tests (perfect mazes, planting, timed and voided runs, the corn) and a client test with screenshots.

### Agriculture: fall additions 7, the Harvest Feast Table (pull request pending, stacked on the sky lantern festival)
- **Harvest Feast Table:** a long trestle table built a length at a time; lengths end to end join into one table. Each length holds two dishes of up to eight servings of any food or drink, drawn heaped on their plates; eat a serving with an empty hand.
- **The feast:** different foods on the table plus everyone who has eaten there in the last two minutes. A good meal gives Regeneration, a feast Absorption, a harvest feast Haste and Luck, and a grand feast Health Boost and the advancement Harvest Home, shared with every recent diner close by.
- All decided on the server; clients only draw the dishes. The checker compares the table and its tiers with Java. New server game tests and a client test with screenshots.

### Agriculture: fall additions 6, the sky lantern festival (pull request pending, stacked on spooky fireworks)
- **Sky Lanterns:** paper lanterns, dyed any colour and named in an anvil to carry a wish. Let one go and it rises glowing, drifting on a wind every lantern shares, so lanterns let go together drift together; it burns out after two minutes or so. A blow puts one out.
- **The lantern festival:** eight lanterns let go within 32 blocks in two minutes, by one player or many, fill the sky: everyone near gets Luck for five minutes and the advancement A Sky Full of Wishes. Once a day in one place.
- **Mooncakes** (red bean, chestnut, pumpkin), baked four at a time in the Cooking Pot. Eaten outdoors on a full-moon night, they give Luck too.
- All decided on the server; clients only draw the lanterns. The checker compares the lanterns, festival and mooncakes with Java. New server game tests and a client test with screenshots.

### Agriculture: fall additions 5, spooky fireworks (pull request pending, stacked on crows and working scarecrows)
- **Spooky fireworks** that burst into pictures drawn in coloured sparks: a **bat**, a **jack o'lantern**, a **ghost** and a **skull**. Every player sees the picture the right way round, since each client draws it facing them. Crafted from paper, one to three gunpowder and the picture's ingredients; glowstone dust makes them twinkle. They hurt and break nothing, and dispensers fire them.
- **Show Launcher:** nine tubes of up to sixteen rockets each (spooky or vanilla), fired in sequence, in volleys of three or as a finale of all nine at once, fanned out across the sky. Start and stop it by hand or with redstone; hoppers can load it.
- All decided on the server; a burst is one small message to the players who can see it, and each client draws the sparks. The checker compares the rockets, launcher and pictures with Java and checks all 24 rocket recipes. New server game tests and a client test with screenshots.

### Agriculture: fall additions 4, crows and working scarecrows (pull request pending, stacked on the preserves pantry)
- **Crows** come to fields by day in flocks of two or three. They wheel over the field, cawing, then drop onto a ripe crop and peck it three growth stages back (only while the `mob_griefing` rule is on). Crops under a roof, tall crops, gourds and bushes are safe.
- **Scarecrows now work:** crows leave the crops within 4 blocks of one alone, 8 when it wears a pumpkin head, 12 when the head is lit. A crow after a crop takes flight when a scarecrow goes up beside it.
- Crows also fly off from a player who comes close (sneak to get closer), from a blow, and at nightfall. They drop feathers, for arrows and the Featherfall candle scent.
- All decided on the server; spawning follows the `spawn_mobs` rule, and nothing loads a chunk. The checker compares the crows' and scarecrows' numbers with Java. New server game tests and a client test with screenshots.

### Agriculture: fall additions 3, the preserves pantry (pull request pending, stacked on the cider mill)
- **Preserves in Mason Jars**, cooked in the Cooking Pot: sweet berry jam, apple butter (cooked down in cider), pumpkin butter (with mulling spices), cranberry preserves, glow berry jelly (Night Vision), and pickled beets, pickled peppers (Fire Resistance) and corn relish in new **Cider Vinegar**. A jar holds four servings, eaten one at a time; the last leaves the jar.
- **Unsealed jars spoil** three days after they were cooked. The **Canning Kettle** seals them: fill it with water, set it over a fire, stand up to four fresh jars in it, and twenty seconds at a rolling boil seals them. Sealed jars keep until opened, stack, and wear a gingham cap.
- **Pantry Shelf:** an open cupboard that shows off six jars.
- All decided on the server; the kettle's water and jars and the shelf's jars are drawn by each client. The Cooking Pot now stamps a jar with when it was cooked. The checker compares the pantry with Java. New server game tests and a client test with screenshots.

### Agriculture: fall additions 2, the cider mill (pull request pending, stacked on the chandlery)
- **Apple trees:** wild in plains and flower-rich places, or grown from **apple seeds**. Their leaves blossom and then hang with ripe apples to pick, about a Minecraft day apart, without the tree being cut down.
- **Cider Press:** load up to eight apples and turn the crank to grind them, one at a time, into pulp. Then turn the screw: four turns press the juice into the trough (a serving an apple) and knock out the pomace. Bottle the juice as **Sweet Cider**.
- **Cider Barrel:** pour sweet cider in; in a day it ferments into **Sparkling Cider**, in three it matures into **Aged Cider**, and a chalk mark on the barrel shows which. Broken, it keeps its cider, still ageing.
- **Mulled Cider** (sparkling cider and mulling spices in the Cooking Pot), **Apple Cider Donuts**, and **Apple Pomace** for pigs, compost and seeds. Each cider gives a short effect: Haste, Jump Boost, Absorption, Regeneration.
- All decided on the server; the press's apples, pulp, screw and juice are drawn by each client. The chestnut tree's fruiting now shares its code with the apple tree. The checker compares the press, barrel and tree with Java, and makes sure no bottle is made from nothing. New server game tests and a client test with screenshots.

### Agriculture: fall additions 1, the chandlery (pull request pending, stacked on batch 14)
- **Wax Melting Pot:** a copper pot set over a fire. Melt honeycomb (beeswax) or rotten flesh (tallow) in it, then stir in dyes (mixed as on leather), up to two scents, glowstone dust (a stronger aura, a faster burn) and redstone (a longer burn).
- **Aura Candles:** dip string in the wax to start a candle, and dip it again once each layer has cooled (dipped while warm, the layer slides off), up to four layers. Each layer makes it taller and brighter, widens its aura (5, 8, 12, 16 blocks) and adds its wax's colour, scents and burn time. Layers of different pots combine their scents; a third scent muddles the candle.
- **Lit, a candle works like a small beacon:** every four seconds it gives everyone in range its scents' effects (Speed, Jump Boost, Night Vision, Slow Falling, Water Breathing, Fire Resistance, Haste or Regeneration), or wards off monsters, makes creatures glow, or speeds up crops. It burns down as it burns and goes out for good; broken, it keeps what is left.
- All decided on the server; each client draws the wax, the candle's colour and height and its tinted flame. The checker compares the waxes, scents and numbers with Java. New server game tests and a client test with screenshots.

### Agriculture: more Halloween, batch 14, costumes (pull request pending, stacked on batch 13)
- **Six outfits**, worn on the head and drawn over the whole body: a **Vampire Cape** that flares as you walk and wraps round you when you sneak, **Mummy Wraps**, a **Skeleton Suit** whose bones glow in the dark, a **Werewolf Mask** with fur, claws and a tail, **Cat Ears and Tail** (the tail sways), and **Bat Wings** that spread and flap when you jump.
- Each counts as a trick-or-treat costume (and for the costume contest); costumed mobs wear them during the event.
- **Costume Trunk:** keeps nine costumes; use it with an empty hand to change into the next one.
- Changing is decided on the server; how outfits move is drawn by each client from boxes generated with their textures. The checker compares the outfits, the trunk and the boxes with Java. New server game tests and a client test with screenshots.

### Agriculture: more Halloween, batch 13, treats (pull request pending, stacked on batch 12)
- **Witch's Brew Punch Bowl:** brew glowing green punch from berries, under rolling dry-ice fog, and ladle it into bottles; a drink makes you glow.
- **Soul Cakes** and the **Barmbrack:** a fruit loaf eaten a slice at a time, one slice hiding a ring; every slice tells a fortune.
- **Pumpkin Spice Latte** (a burst of Speed), **Pumpkin Bread**, **Spiderweb Cupcakes** and **Bat-Wing Cookies**; the cakes, cupcakes and cookies count as treats for Candy Bowls and Candy Bags.
- **Giant Candy:** block-sized props of candy corn, a lollipop, a wrapped sweet and a gumdrop.
- All decided on the server; nothing ticks. The checker compares the bowl's servings, the barmbrack's slices and fortunes, the candy's designs and the drinks' effects with Java. New server game tests and a client test with screenshots.

### Agriculture: more Halloween, batch 12, night events (pull request pending, stacked on batch 11)
- **Trick-or-treaters at your door:** during the Halloween event, village children in costume knock at a Candy Bowl by a lit door; a full bowl earns thank-you gifts, an empty one gets your trees toilet-papered.
- **Toilet Paper Rolls:** throw them over trees and fences; the streamers wash off in the rain.
- **Haunted Hayride:** a four-seat hay wagon on rails; at night its riders hear spooky things from the dark.
- **Halloween Bonfire:** cooks what a campfire cooks, four at a time and twice as fast; toast **Marshmallows** on a stick over it (or a campfire), but not too long.
- All decided on the server; the flames, skewered food and wagon are drawn by each client. The checker compares the numbers, costumes and gifts with Java. New server game tests and a client test with screenshots.

### Agriculture: more Halloween, batch 11, party games (pull request pending, stacked on batch 10)
- **Jump-Scare Trap:** a crate that bursts open and throws up a shrieking ghost on a spring when someone walks up, or a tripwire fires.
- **Costume Contest:** walk the **Costume Runway** in costume while the **Judges' Table** has a round open; everyone else votes by using their favourite, and the most votes win a **Best Costume Ribbon**.
- **Pumpkin Bowling:** roll a **Bowling Pumpkin** down a lane of **Skeleton Pins**; the **Bowling Scoreboard** keeps ten-pin score and stands the pins up again.
- **Candy Cache:** a hollow stump that hides treats like a Candy Bowl, one a night for each finder.
- **Monster Mash Dance Floor:** tiles light up in pulsing Halloween colours from a playing jukebox or redstone; villagers on them dance.
- **Ghost Tag:** ring the **Ghost Bell**; whoever is the ghost glows and tags others by hitting them, harmlessly, with no tag-backs.
- **Fortune Teller's Table:** a tarot card turns, the planchette slides to YES, NO or GOODBYE, and you get one of twenty silly fortunes.
- All decided on the server; the moving parts are drawn by each client. The checker compares the numbers and messages with Java. New server game tests and a client test with screenshots.

### Agriculture: more Halloween, batch 10, lighting and glow (pull request pending, stacked on batch 9)
- **Black Light** and **Glow Paint:** paint skulls, bats, spiders, webs, handprints and eyes on any face; they blaze green-white under a black light nearby.
- **Witch Fire Brazier:** a brazier whose flame turns orange, green, purple or blue with a dye; it burns nothing.
- **Shadow Puppet Lamp:** its turning paper shade throws a bat, a cat and a witch round the walls of the room.
- **Mini Pumpkin Stack** and **Floating Witch Hat:** candle-lit, lit and snuffed like candles; the hats bob and turn in the air.
- The glow, flames, shade, shadows and hats are drawn by each client. The checker compares the numbers with Java. New server game tests and a client test with screenshots.

### Agriculture: more Halloween, batch 9, the yard and porch (pull request pending, stacked on batch 8)
- **Yard Inflatables:** a ghost, a black cat, a pumpkin stack and a spider, two blocks tall, that fill up on a click or redstone, wobble and glow, and sag flat when switched off.
- **Animatronic Porch Witch:** stirs her bubbling pot and follows you with her eyes; walk up and she throws her head back and cackles.
- **Grasping Hands:** rotting hands in a mound of dirt that snatch at the ankles of anything that steps on them (a short, harmless Slowness II); sneak past.
- **Poseable Skeleton:** use it to pose it sitting, waving, lounging or hanging.
- **Bone Wind Chimes:** bones and a little skull under a porch roof that swing and clack, more and louder in rain and storms.
- **Bat and Witch Weathervanes:** turn to point into one wind shared by the whole world, swinging about in storms.
- **Spooky Sign:** painted warnings (BEWARE, KEEP OUT, TURN BACK...) or your own words from a Name Tag or an anvil.
- **Haunted Archway** and **Dead Hollow Tree:** lantern-lit props of several blocks, placed and broken as one.
- The figures, the witch's arm and head, the chimes, the vanes and the sign's words are drawn by each client. The checker compares the numbers with Java. New server game tests and a client test with screenshots.

### Agriculture: more Halloween, batch 8, the mad scientist and monsters (pull request pending, stacked on batch 7)
- **Tesla Coil:** a two-block coil on the electric network (20 JE a tick) that hums, glows and throws harmless violet arcs to other running coils nearby.
- **Lab Table:** a two-block operating table whose sheeted patient sits bolt upright on a redstone signal, and twitches at night.
- **Specimen Jar:** glowing green fluid with an eye, a tentacle, a tiny pumpkin or a brain bobbing in it.
- **Mummy Sarcophagus:** a click or redstone and the lid grinds open, the mummy lurches out with its arms up, then goes back.
- **Raven on a Perch:** watches the nearest player, ruffles and croaks, caws and flaps when used.
- **Black Cat Figure:** swishes its tail, its eyes glow at night, and it arches its back and hisses at anyone who runs past.
- Arcs, the patient, the specimens, the lid and mummy, the raven and the cat's tail and eyes are drawn by each client. The checker compares the numbers with Java. New server game tests and a client test with screenshots.

### Agriculture: more Halloween, batch 7, the haunted house inside (pull request pending, stacked on decorations batch 6)
- **Haunted Chandelier:** eight candles on an iron ring that sways on its chain; at night a draft blows them all out and they relight one by one.
- **Phantom Pipe Organ:** a three-by-two organ, one prop, that plays the opening of Bach's Toccata and Fugue in D minor on a click or redstone, its keys going down by themselves; at night it sometimes plays alone.
- **Suit of Armor:** two blocks of plate on a stand whose helmet slowly turns to watch the nearest player; a red glow in its visor at night.
- **Dust Sheet:** drape it over a chair, stair, slab, chest or bookshelf; it keeps what is under it (a chest's contents too) until you pull it off. At night some sheets seem to breathe.
- **Spirit Mirror:** at night a pale face fades into the glass now and then.
- **Tattered Curtains:** cheesecloth drapes up to eight blocks long that open and shut together and sway in a draft.
- **Creepy Doll:** its head never moves while you watch, but it has turned every time you look back.
- The sway, flames, keys, helmet, sheets, face, curtains and doll's head are drawn by each client. The checker compares the numbers and the organ's tune with Java. New server game tests and a client test with screenshots.

### Agriculture: Halloween decorations, batch 6, the haunted house and yard (pull request pending, stacked on batch 5)
- **Rocking Chair:** sit in it and it rocks under you; at night, empty, it rocks on its own and creaks.
- **Lurking Eyes:** glowing eyes that peer out of a hedge at night, blink, and vanish when you come within four blocks.
- **Silhouette Window:** a bat, black cat or witch cut-out in orange paper that glows when a lamp lights the far side.
- **Spooky Music Box:** plays an original waltz on note-block sounds while powered by redstone, or once when wound by hand.
- **Giant Fake Spider:** a big hairy spider swaying on a silk thread from a ceiling, branch or cobweb; let its thread out up to four blocks.
- The chair's rocking, the eyes, the windows' glow and the spider's sway are drawn by the client. The checker compares the numbers and the tune with Java. New server game tests and a client test with screenshots.

### Agriculture: Halloween decorations, batch 5, the harvest party (pull request pending, stacked on batch 4)
- **Bobbing for Apples Tub:** drop apples in, then duck for one with an empty hand: one try in three catches an apple, and the tub splashes a moment between tries.
- **Pumpkin Crate:** shows up to four of your pumpkins, squash, gourds or melons.
- **Hay Bale Seat:** sit on it; it softens falls like a hay block.
- **Autumn Wreath:** chestnut leaves, ornamental corn and mums, for walls and doors; a mum changes its flowers.
- **Leaf Piles:** red, orange and yellow, heaped up to four layers; they soften falls and kick up leaves underfoot.
- The checker compares the numbers, colours and tags with Java and checks every state has a blockstate entry. New server game tests and a client test with screenshots.

### Agriculture: Halloween decorations, batch 4, the witch's cottage (pull request pending, stacked on batch 3)
- **Bubbling Cauldron:** fill it with a water bucket, then a spider eye, nether wart or glowstone (among others) turns it into a glowing green, purple or orange brew; over a fire it bubbles and steams. An empty bucket pours it out.
- **Apothecary Shelf:** wall shelves of corked jars, tinctures, a little skull and a candle; sneak-use to set them out four ways.
- **Crystal Ball:** a violet-misted orb on a gilt stand; gaze into it and it flares and tells you one of ten fortunes.
- **Grimoire Stand:** an open spellbook on a carved stand; use it to turn through four spreads. Its pages glow faintly and give off motes at night.
- **Witch's Broom:** a twig besom leaning on its bristles.
- The checker compares the numbers, fortunes, spreads and brew tags with Java and checks every state has a model. New server game tests and a client test with screenshots.

### Agriculture: Halloween decorations, batch 3, the graveyard (pull request pending, stacked on batch 2)
- **Wrought-Iron Cemetery Fence and Gate:** spear-topped iron pickets on finialed posts; a real fence and fence gate (vanilla's fence and gate blocks and tags).
- **Crypt set:** Crypt Stone, Chiseled Crypt Stone (a carved skull), Crypt Stone Pillars (also from the stonecutter) and a stone Crypt Door that opens by hand.
- **Grave Mound:** walk past and a zombie hand claws up out of the earth for three seconds; sneak past and it stays down; redstone holds it up.
- **Mourning Angel:** a two-block marble statue, head bowed into its hands; at night it weeps.
- **Pop-Up Skeleton:** a crate whose skeleton springs out at passers-by (or on a redstone signal).
- The checker compares the scare props' timings with Java, checks every state has a model and that the fence, gate and door are in vanilla's tags. New server game tests and a client test with screenshots.

### Agriculture: Halloween decorations, batch 2 (pull request pending, stacked on the first five)
- **Luminaria:** a paper bag weighted with sand round a candle, a jack-o'-lantern face cut in its sides. Lit like a candle (light 10); any dye colours it, and it keeps its colour when broken.
- **Floating Candles:** up to four candles hanging in the air, bobbing gently (drawn by the client); 3 light a candle while lit.
- **Skeleton Hand Sconce:** a torch held out from a wall by a bony hand (light 14); snuff it by hand, relight it with flint and steel.
- **Soul-Flame Carvings:** a soul torch lights hand-carved and giant pumpkins with an ice-blue glow (at most light 10) and comes back out as a soul torch. Hand-carved pumpkins gain a `soul` block state (old worlds load with a plain candle).
- **Bat Bunting:** orange and black pennants and paper bats strung between String Light Hooks like the string lights; hooks remember which strand they hold.
- The checker compares the new blocks' numbers and the floating candles' places with Java, checks every state has a model, that the bag has a face cut through its walls and a whole inside (so the face shines with the candlelit far wall), and that the soul-lit icon uses the client's soul colours. New server game tests and a client test with screenshots.

### Agriculture: Halloween decorations, the first five (pull request pending, stacked on Halloween nights)
- **Jack-o'-Lantern String Lights** strung between **String Light Hooks** (up to 16 blocks; hooks chain); a hook lights from redstone or 1 JE a tick from the electric network, and a strand glows while either end is lit.
- **Candy Bowl:** fill it with treats; each visitor takes one a night (saved on the server), its owner any time.
- **Coffin:** a two-block 27-slot chest whose lid lifts while it is open, and a bed you sneak-use to lie down in and set your spawn (it refuses where beds explode).
- **Haunted Portrait:** four sitters; the pupils follow each player's camera and glow red at night.
- **Fog Machine:** switched on by hand or redstone, it uses 16 JE a tick and rolls ground fog over a 4–16 block radius, drawn by clients with a per-tick cap.
- The checker compares the decorations' numbers and the portraits' eyes with Java and the textures, and checks every block state has a model and every result its message. New server game tests and a client test with screenshots.

### Agriculture: Halloween nights (pull request pending, stacked on the Halloween festivities)
- **Will-o'-wisps** (event nights): glowing wisps drift over swamps and cornfields and flee when you come near (sneak to get closer). A glass bottle catches one in a **Wisp in a Jar** (a lantern, light 13). Advancement **Bottled Light**.
- **Pumpkin Chunkin' Trebuchet** (all year): load a pumpkin, set the release angle (30°–60°), let fly about 50 blocks. The server measures where it lands; a marker shows the distance; a board of the three longest throws gives the Harvest Scale's ribbons once per thrower. Advancement **Pumpkin Chunkin'** at 50 blocks.
- **Candy Bag:** now holds treats like a bundle (candy and cookies only); trick-or-treating fills it, and its tooltip shows tonight's homes.
- **The Harvest Moon:** on the nights of 31 October (new option `halloween.harvest_moon`) during the event, crops and giant pumpkins grow twice as fast and lit carvings throw sparks.
- **The Headless Horseman:** summoned near midnight during the event at a Scarecrow with a lit pumpkin head. A boss with a boss bar, charges and flaming pumpkins that never break blocks; enraged at half health; keeps to his arena; rides off with nothing at dawn or when the event ends. Defeated, he drops the **Horseman's Lantern** and **Horseman's Cloak**. Advancement **Lost His Head**.
- Wisp spawning and sky checks use heightmaps (no light lookups). The checker compares the wisps, trebuchet, Harvest Moon and Horseman numbers with Java, checks every trebuchet state has a model, the ammunition tag matches the throwing factors, the Horseman's loot is player-kill only, every failed summoning has a message, and that no block model face reads outside a see-through texture (26.3 refuses to bake those). New server game tests (including the seasonal rules) and a client test with screenshots.

### Agriculture: the Halloween festivities (pull request pending, stacked on the pumpkin regatta)
- **The carving contest:** put a hand-carved pumpkin on a **Judging Stand** and enter it (its carver only). While the Halloween event runs, every player has one vote per Halloween for someone else's carving; sneak-use a stand for the standings. When the event ends, the top three carvers get the Harvest Scale's ribbons, once (offline winners on their next visit). Votes and prizes are saved with the world.
- **Costumed mobs** (during the event): 15 % of zombies, husks, skeletons, strays and zombie villagers wear a costume hat or carved pumpkin, and drop a sweet when a player kills them.
- **The Halloween Peddler** (during the event): wandering traders arrive in a Witch Hat with four extra Halloween wares for emeralds, from a data-driven trade set.
- **Decorations, all year:** **Rounded**, **Cross** and **Obelisk Gravestones** (stonecutter) engraved with a named Name Tag's name; **Spun Cobweb** (no slowing); **Hanging Ghost**; **Candle Skull** (lights like a candle).
- **Spooky sweets** from the Cooking Pot, eaten even when full: **Glow Gum** (Glowing), **Ghost Taffy** (3 s of invisibility), **Fizz Rocks** (Jump Boost), **Witch's Licorice** (Night Vision).
- The checker compares the contest, costumed mobs, Peddler, gravestones and sweets with Java, checks the Peddler only sells things with another route, and checks `villager_trade` tags. New server game tests (including the seasonal rules: on, off, a restart, no duplicate prizes, earned ribbons kept) and a client test with screenshots.

### Agriculture: the pumpkin regatta and trick-or-treating (pull request pending, stacked on the Halloween harvest)
- **Pumpkin boats:** sneak and use the Carving Knife on top of a giant pumpkin to hollow it out. A full-grown 3×3×3 one becomes a **Pumpkin Barge** (four seats; keeps its weight, carving and torch), a 2×2×2 one a **Pumpkin Racer** (one seat). Lighter boats are faster: racers 1.15–1.30× a boat, barges 0.70–0.95×. Broken, a boat gives back its item with everything it kept. Hollowing gives the guts and (full grown) the giant seeds instead of the pumpkins.
- **The regatta:** a **Regatta Flag** and numbered **Regatta Buoys** on the water make a course. From a pumpkin boat's driver's seat, use the flag: after a countdown, round the buoys in order and come back. The server times the run; the flag keeps a board of the three best times and gives the Harvest Scale's ribbons once per racer. Advancement **Pumpkin Regatta**.
- **Trick-or-treating**, only while the Halloween event runs (by default 20 October to 3 November; operators set `halloween.start`, `halloween.end`, `halloween.timezone` and `halloween.mode` in `config/jugcraft.properties`): use a **Candy Bag** on a villager's door between dusk and midnight, in costume, with a porch light by the door. Each home gives each player one treat a night (candy, caramel, cookies, popcorn balls, caramel apples, rarely a **King-Size Candy Bar**); knocking again gets a harmless prank. Ten homes in a night earn **Full Bag**. Who got what tonight is saved, so a restart or the event ending and starting again gives no second treat; treats and costumes stay after the event.
- **Costumes:** **Witch Hat**, **Ghost Sheet** (draped over the whole wearer, hood to knees, moving with them; you look out through its eye holes) and **Scarecrow Hat**, worn on the head. Hand-carved pumpkins can now be worn like vanilla's carved pumpkin (and fool endermen).
- The checker compares every boat, regatta and trick-or-treat number, tag and table with Java, and allows vanilla tab roots as advancement parents. New server game tests (including the seasonal rules: on, off, a restart, no duplicates, earned treats kept) and a client test with screenshots.

### Agriculture: the Halloween harvest (pull request pending, stacked on Pumpkin Carving)
- **Giant pumpkins:** Giant Pumpkin Seeds plant a vine that sets one fruit and grows it to 2×2×2, then 3×3×3 (faster on moist farmland and when watered; bone meal grows the vine, sets the fruit and feeds it). Full grown it weighs 100–120 kg and gains weight up to 1000 kg until carved. It is one prop: breaking it picks it up whole as a **Giant Pumpkin** item (keeping its size, weight and carving) that places the same pumpkin back; an axe chops it into 9 pumpkins and 1–3 giant seeds. Pistons can't move it.
- **A 48×48 carving face** on each side of a full-grown giant, carved with the Carving Knife through the same screen (smaller cells, bigger brushes, starter faces blown up); a torch lights every block of it.
- **Scooping:** the first cut into a pumpkin also gives 1–2 **Pumpkin Guts** (Pumpkin Soup in the Cooking Pot) and sometimes a giant pumpkin seed.
- **Harvest Scale:** weighs the giant beside it, keeps a board of the three heaviest, gives First, Second and Third Prize Ribbons (trophies) once per pumpkin, and drives a comparator.
- **Pumpkin stencils:** trace a carved side onto a Blank Stencil; hold the stencil in your other hand while carving to press it in.
- **Heirloom pumpkins:** White, Jarrahdale and Cinderella, grown from stems, found wild, carvable into their own hand-carved blocks, and baked into pumpkin pie.
- **Scarecrow** (dye its flannel shirt; it wears any pumpkin on its shoulders for a head, like an armor stand, carving and all, and a lit one lights it), **ornamental corn** (a tall crop; its ears tie into an **Ornamental Corn Bundle** for walls), **Corn Stalks** from tall corn and the **Corn Shock**.
- **Caramel**, **Caramel Apple** (you keep the stick) and **Popcorn Ball**.
- **Bottle Gourd** (found wild), dried into a **Gourd Birdhouse** (stands or hangs) or a **Gourd Canteen** (3 sips of water for giant pumpkins, farmland, fire or a cauldron).
- **Mums** in four colours: flowers for pots, dye and suspicious stew, in wild patches.
- Short grass now drops 24 kinds of seed, still 12.5 % overall. The carving payloads now carry a face size (16 or 48), checked before anything is read. The checker compares the giant pumpkin, scale, canteen, mum and heirloom numbers with Java and checks every giant and scarecrow state has a model. New server game tests and a client test with screenshots, which also carves a giant from a stencil through the screen.

### Agriculture: Pumpkin Carving (pull request pending, stacked on the Festival Crops)
- **Carving Knife** (an iron ingot over a stick): used on the side of a pumpkin, it opens a **16×16 carving screen** for that side, at Minecraft's own pixel size. Cut through, shave the skin or erase; brush sizes 1–3, mirror, four starter faces (Classic, Cat, Ghost, Spooky), undo (Ctrl+Z), reset, a candle preview and an actual-size preview.
- **Hand-Carved Pumpkin:** the first cut lets out the seeds (as shears do) and turns the pumpkin into one that keeps a face on each of its four sides. A knife can't put skin back: carving only goes deeper.
- **Light:** a torch inside lights it, from 4 up to 15 the more is carved out; an empty hand takes the torch back. Broken, it drops with its design and its torch.
- **Roasted Pumpkin Seeds** from the furnace, smoker or campfire.
- **Server checks:** the server checks every carving (the session from using the knife, the knife in hand, reach, build permission, a valid face that only goes deeper). Carvings record their last carver for operators, and `carving.free_draw=false` in `config/jugcraft.properties` allows only the starter faces.
- The checker also verifies the carving numbers against Java, the starter faces and every refusal message, and understands select item models. Ten new game tests, and a client test that carves through the real screen with the mouse and keyboard (after the owner's first play found that clicks on the grid did nothing: 26.3 numbers mouse buttons from 1, and the screen used the old numbers).

### Agriculture: Festival Crops (pull request pending, stacked on the Kitchen Garden)
- **Gourds on stems:** butternut squash, acorn squash and warty gourds grow from stems on farmland and place their gourd beside them, like pumpkins. Stems follow Jugcraft's growth rules, so squash next to beans grows 1.5× as fast. Gourds are blocks for fall displays; 1 gourd → 4 seeds.
- **Turnips**, and the **Turnip Lantern** (a turnip over a torch): a carved turnip that gives light 13, the original jack-o'-lantern.
- **Cranberries**, a bog crop: the bush stands in a water source one block deep over bog soil (block tag `jugcraft:bog_soil`), grows only with open air above, keeps its water when broken, and is picked like sweet berries.
- **The chestnut tree:** a chestnut plants a sapling that grows into a broad tree. Its leaves grow burs that ripen over about a day and are picked with a right-click, without cutting the tree. A chestnut wood set (logs, wood, stripped, planks, stairs, slab, fence, fence gate): any axe strips it, the sawmill saws a log into 6 planks, and it joins vanilla's wood tags, fuel and fire rules.
- **Food:** roasted chestnuts, baked acorn squash, squash pie, candy corn; in the Cooking Pot, butternut squash soup, harvest stew and cranberry sauce.
- **Finding them:** gourds lie on grass, ripe cranberry bushes stand in swamp shallows and chestnut trees grow in forests (new chunks); wild turnips grow in taiga and birch forest. Short grass drops all six new seeds (18 in all, still 12.5 % overall).
- The sickle also picks cranberries and cuts gourds off their stems. Nothing is seasonal: all of it stays after the Halloween and December events are added.
- The checker also verifies gourds, the cranberry and chestnut numbers, bog seeds and every new stage model. Twelve new game tests and a client screenshot test.

### Agriculture: Kitchen Garden (pull request pending, stacked on the Fall Harvest)
- **Trellis:** a square wooden lattice (2 from 5 sticks). It stacks, stands on farmland and blocks movement like a fence.
- **Tomatoes climb trellises**, two blocks tall, and are picked without cutting them down. They grow only into trellis, never into air, and breaking the plant gives back every trellis it grew in.
- **Peppers** grow as a 1-block bush that is picked the same way. **Onion, garlic, cabbage, oats and barley** are 1-block crops, each with a wild plant in fitting biomes.
- **Cooking Pot:** 5 iron ingots and 2 sticks. It cooks while a lit campfire, fire, lava or magma block is directly under it (block tag `jugcraft:heat_sources`). It has six ingredient slots in any order and four result slots, and supports batch cooking, hoppers and comparators. Recipes are data-driven (`jugcraft:pot_cooking`): tomato, onion, vegetable and mushroom barley soups, oat porridge, chili and cabbage rolls.
- **By hand:** garden salad, barley bread, and sauerkraut (cabbage plus Jugcraft salt).
- **Changed:** short grass now drops one Jugcraft seed 12.5 % of the time (vanilla wheat seeds' rate), chosen evenly from all twelve crops. It used to be 2 % per crop, which would have been 24 % with twelve crops.
- The checker also verifies climbing-crop seeds, the Cooking Pot's numbers and recipes (no two with the same ingredients), and loops through every agriculture recipe. Thirteen new game tests and a client screenshot test.

### Agriculture branch: Fall Harvest (pull request pending)
- **New branch:** [docs/branches/AGRICULTURE.md](docs/branches/AGRICULTURE.md) covers the whole Agriculture plan: the crop roster in eight slices, non-industrial farm equipment, connections to the other branches, and rules.
- **Corn grows 3 blocks tall.** You pick it with a right-click, and the stalk stays standing and grows new ears, so cornfields and corn mazes last. From 2 blocks tall it blocks movement like a hedge, which is what makes a maze.
- **Sunflowers** grow 2 blocks tall with a big yellow head, and are picked the same way.
- **Beans, sweet potatoes and flax** are one-block crops. Beans make the crops around them grow 1.5× as fast (legume bonus, block tag `jugcraft:nitrogen_fixing_crops`).
- **Food:** corn, roasted corn, popcorn, sweet potato, baked sweet potato, roasted sunflower seeds and Three Sisters Stew. 2 flax make 1 string.
- **Flint and Bronze Sickles** harvest and replant every ripe crop in 3×3 or 5×5 (vanilla crops too).
- **Seed sources:** wild plants in fitting biomes (new chunks), and short grass (2 % per crop) anywhere.
- Also: composting, pig, chicken and parrot feed, `c:` crop, seed and food tags, and a new `agriculture` feature switch.
- Original textures from `tools/crop_textures.py`. Twelve game tests, plus a client game test with screenshots of a corn maze, the fields and every growth stage.

### Unmerged: biomes branch, Jugcraft regions and nine batches of biomes (stacked on Alpine Spawn)
- **Remaking the Biomes O' Plenty catalog in Jugcraft**, with original art and names: the roster in [docs/branches/BIOMES.md](docs/branches/BIOMES.md) plans 98 biomes in nine batches.
- **Jugcraft regions:** about half the Overworld, in regions about 1 km across, grows the new biomes in place of some vanilla ones; the rest stays vanilla. New settings: `biomes.enabled`, `biomes.region_size`, `biomes.region_share`.
- **Batch 1, the seasonal forests:** Coniferous Forest, Snowy Coniferous Forest, Maple Woods, Seasonal Forest, Aspen Glade, Dead Forest, Tundra, Snowy Forest and Muskeg.
- **New trees and wood:** maples turn red, orange and gold in autumn and stand bare in winter; aspens turn gold; firs stay green; dead wood stands grey. Each has a full wood set (dead wood without a sapling), sawmill and tree farm recipes.
- **Region layouts:** Jugcraft regions come in four layouts (woodland, meadow, wetland, wild), so one vanilla climate can grow different Jugcraft biomes in different regions.
- **Batch 2, fields and meadows** (meadow layout): Field, Flower Meadow, Grassland, Heathland, Lavender Field, Lush Grassland, Prairie, Shrubland and Steppe.
- **New plants:** lavender and tall lavender, goldenrod, heather, orange cosmos (all make dye, the small ones go in flower pots) and clover. **New tree:** the jacaranda, in violet bloom all year, with a full wood set.
- **Batch 3, wetlands** (wetland layout; swamps also elsewhere): Bog (with wild cranberries), Dead Swamp, Lush Swamp, Swamp Woods, Bayou, Floodplain, Ghost Forest, Sludge Mire, Lush River, Fen, Lake District, Quagmire, Marsh and Wetland, with muddy ponds and mud.
- **More plants:** cattails, watergrass (under water) and duckweed (on water). **New tree:** the willow, with hanging leaves and vines, yellow in autumn and bare in winter.
- **Batch 4, warm and dry:** Dryland, Xeric Shrubland, Jacaranda Glade, Lush Desert, Bone Flats, Dry River, Cold Desert, Scrubland, Lush Savanna, Outback, Oasis, Wasteland, Burnt Forest, Mediterranean Forest and Orchard, with their own ground (red sand, coarse dirt, gravel, salt flats). **New trees:** palm and cypress.
- **Batch 5, big trees and rainforests** (mostly the woodland layout): Rainforest, Eucalyptus Forest, Tropics, Subtropics, Dense Forest, Redwood Forest, Temperate Rainforest and Woodland.
- **New trees:** redwood and mahogany (four saplings in a square grow a giant, two blocks wide) and eucalyptus (rainbow-streaked bark), each with a full wood set. **New flowers:** hibiscus and hydrangea.
- **Batch 6, mountains, coasts and volcanoes:** Volcano, Canyon, Highland, Basin, Shield, Karst Pinnacles and Hot Springs (wild layout), Gravel Beach, Dune Beach, Overgrown Beach and Flower Isle (wetland layout), Ice Sheet and Ocean Trench (both). **New plant:** sea oats, which grow on sand.
- **Fixed:** the Cold Desert is no longer buried in snow.
- **Batch 7, wonders and caves:** Cinder Barrens, Elder Vale, Frostlight Garden, Gilded Shrubland, Glimmer Grove, Gloomweald, Hallowed Bog, Highsun Meadow, Mycelial Jungle, Shrine Springs, Snowpetal Grove, Starlit Wood, Toadstool Field, Webwood and Wild Greens, and two cave biomes, the Glowcap Grotto and the Spider Nest. **New plants:** glowcaps and glimmerblooms (both glow), frost irises and snowpetals.
- **Batch 8, the Nether:** Ashfall Wastes, Blighted Sands, Frost Rift, Fungal Thicket, Magma Fields, Marrow Heap, Netherbrush, Quartz Rift and Withered Hollow, rarer than vanilla's five. **New plant:** brambles.
- **Batch 9, the End:** Chorus Reef, Ender Wilds, Outer Flats, Phantom Garden and Rotted Expanse on the outer islands. With it, every biome on the roster is built.

### Unmerged: Alpine Spawn, parts 1 and 2 (stacked on the agriculture pull requests, #53 and #80)
- **New worlds start in Alpine Spawn, at an alpine village.** Alpine Spawn is a large, cool alpine meadow on mountain plateaus; it takes the place of every vanilla meadow and of the cool plateau's forest and taiga. The server moves a new world's spawn to the alpine village nearest the origin, or into the biome when there is no alpine village within 6,400 blocks.
- **Alpine villages are common:** vanilla's taiga villages on a 16-chunk grid (vanilla's is 34), only in this biome.
- **Larches** (part 2): a conifer that changes with the seasons. Its needles are green in spring and summer, turn gold in autumn and fall in winter, leaving bare twigs, then bud green again; each block turns within a week either side, so crowns change gradually. Larches grow among spruces in Alpine Spawn. The tree drops larch saplings, and comes with a full larch wood set (logs, wood, stripped forms, planks, stairs, slab, fence and gate). Larch logs saw into planks in the sawmill, and saplings grow in the tree farm.
- It has seasonal colours and winter snow from the start. Seasonal flowers, bilberries and an alpine winter come in the next parts.
- New settings: `alpine_spawn.enabled` (generation, and the larch's hand recipes) and `alpine_spawn.start` (`on` or `off`: start there).
- Server game tests for its climate entries, tags and villages, and a client test in a real world that it is where the world starts.

### Unmerged: Powered exosuit, batch 28
- Four JE-powered armor pieces (netherite protection, unbreakable): night vision, an energy shield and jetpack, speed, and fall immunity with step assist.
- Two liveries: Vanguard (gunmetal with teal lights) and Ronin (crimson and silver, conical hat, red eyes), with 3D shoulder plates, skirts and hat. Smithing liveries switch between them and keep the charge.
- The crimson Ronin katana. Inspired by Mekanism's MekaSuit (MIT); looks follow the owner's reference images; all art original.

### Unmerged: Gear, weapons and plastic blocks, batch 27
- **Scuba mask and tank:** breathe under water on oxygen (8,000 mB, 400 s); fill the tank from a gas holder or machine.
- **Free runners:** boots with no fall damage and a one-block step.
- **Power katana and power bow:** JE-powered weapons charged at the charging station; the bow fires energy arrows without ammo.
- **Plastic blocks** in all sixteen dye colours, from plastic sheets.
- High-detail art: an animated 32x32 energy katana, and double-resolution scuba gear and free runners.
- Four advancements and handbook pages. Inspired by Mekanism and Mekanism: Additions (MIT); all code and art original.

### Unmerged: Four-ingot ore and bioethanol, batch 26
- **Acid leaching:** an ore and 250 mB of sulfuric acid in the chemical reactor give 4 washed ores (the best ore route).
- **Bioethanol:** 8 crops and a bucket of water ferment into 250 mB in the chemical reactor; it burns in the gas turbine and the advanced engine.
- Inspired by Mekanism (MIT); no new machines.

### Unmerged: Tools, armor and paxels, batch 25
- Bronze and steel swords, pickaxes, axes, shovels, hoes and armor (bronze iron-tier, steel between iron and diamond).
- Paxels (pickaxe, axe and shovel in one) for every tier from wood to netherite, bronze and steel.
- Bronze armor is steampunk (goggles, pressure gauge, boiler); steel armor is kaiserpunk (Pickelhaube, field-grey tunic, jackboots).
- Two advancements and handbook pages. Inspired by Mekanism: Tools (MIT); all code and art original.

### Unmerged: Fewer chemistry machines, batch 24
- Five single-job machines folded into ones that already exist (65 machines down to 60):
  - the distillation tower vacuum-distils heavy fuel oil (was the vacuum distillation unit);
  - the catalytic cracker reforms naphtha, using a catalyst (was the catalytic reformer);
  - the chemical reactor mixes brine and fracking fluid (was the chemical mixer);
  - the **Settling Plant** (the flowback treatment unit, renamed) separates oil sand and bitumen (was the oil sand extractor);
  - the arc furnace pulls silicon boules, with argon (was the crystal grower).
- New uses: the settling plant presses mud into clay; the electrolytic cell splits water into hydrogen and oxygen.

### #80 Seasons (colours, events and winter snow)
- **Grass and leaves change colour with the server's date** in every biome that has four seasons: plains, meadows, forests (dark, dappled and cherry groves included), taigas, windswept hills and swamps.
  - Winter is dull and dormant, spring is fresh green and summer is vanilla.
  - Autumn turns oak leaves gold, orange and red in patches, then russet.
  - Colours change a little each day.
- **Winter snow (opt-in, `seasons.snow=on`):**
  - From December to February, rain falls as snow in those biomes, and up to `seasons.snow_depth` layers settle.
  - The snow melts in spring.
  - It never freezes water, lies on farmland or touches snow you placed.
- **Events on one clock:**
  - the **Harvest Feast** (`harvest_feast`: the US Thanksgiving weekend by default, Canada's, or off);
  - **December** (`december`: 1 December to 6 January by default).
  - Both are announced in chat.
- **`/jugcraft season`** shows the season, day and events. Operators can set a season, preview a date or switch snow on or off until the server stops.
- **Server settings** in `config/jugcraft.properties`: `seasons.mode`, `seasons.hemisphere`, `seasons.timezone`, plus the snow and event settings above.
- The server decides everything; clients never use their own clock. Colours and events save nothing; seasonal snow melts away.
- Server and client game tests, with a screenshot per season and one of winter snow.

### Blueprints and test blocks (same draft PR)
- **Blueprint Table:**
  - LIBRARY of the mod's structures and imported blueprints, with a front view and materials; printing is free.
  - IMPORT takes pasted `.jugbp.json` text, checks it, saves it with the world and shares it with everyone.
- **Placing blueprints:** a hologram preview up to 32 blocks away, a Survey Stake screen (progress, materials, Personal/Party, rotate, remove), and drone building layer by layer.
- **Drone Tower tier 1** now also builds the Energy Exchange and Storage Exchange, adding seven new building blocks and two ports that feed the depot.
- **Seating:** Operator Chairs can be sat on.
- **Creative-only test blocks:** the Creative Energy Cell and the Creative Supply Crate.

### Drone Depot (draft PR, stacked on Parties)
- **New blocks:** Drone Depot Terminal, Control Screen Panel, Hologram Table, Cargo Packager, and the tower-placed Landing Platform, Landing Pad and Supply Pickup Plate (no recipes: the Drone Tower builds the depot).
- **Depot:** part of the Drone Tower. Tier 1 places the base floor, eight pads (5x5 plates forming a pad with a charger port), the supply pickup (3x3 plates with a lift hatch) and the terminal building. A terminal without a tower flies no drones.
- **Drones:** nine tiers, all craftable, each with its own 3D look in the tower's graphite-and-dull-red theme (tier 9 is the Superconducting Ring Lifter). Tiers 5–9 use new parts from real materials: neodymium motors, tilt-rotor nacelles, composite rotors, hydrogen lift cells (chemical reactor), ion emitters, superconducting tape, Stirling cryocoolers and superconducting lift fans.
- **Drone Tower ([docs](docs/features/drone-tower.md)):** a Tower Core on a 15×15 chiseled stone plinth builds the Command Post (tier 1, with a furnished command room). Tiers 2–9 are flown in tile by tile by the depot's own drones, using four kinds of tower module. Tower tier N unlocks drone tier N; the full tower holds 100 drones, each in its own hangar, with seven pickups. Adds 19 building materials (nine with stairs and slabs), five furniture blocks and a tower status screen. Drones dock round the pads, fly their routes, and winch crates up from the pickup (the hatch opens and a lift raises the crate).
- **Command room screens:** a hologram table (nine sections form one table projecting a live depot map) and a screen wall (six panels form one live display), both in the tower's command room. The terminal opens a sci-fi screen (OVERVIEW, FLEET, JOBS, POWER, and a PERSONAL/PARTY button) whose text is fitted to the panel.
- **Pooled power:** standby and working draw, cached. Low power slows flights and never drops cargo.
- **Flights:** timed flight records (not mobs) with terrain-following routes, at most 5 launches per tick, and reservations. Clients get a small snapshot when something changes and move the drones themselves.
- **`BuildJobs`:** the build-job interface for blueprints and later builders, plus a development-only `/dronetest` command.
- **Tests:** tower server and client game tests; server game tests (layout rules, docks, flight paths, and in-world depots that build blocks, re-form pads, respect Party mode and link screens) and a client game test with screenshots, plus checker rules keeping drone numbers in sync.

### Parties (draft PR; proposal #21)
- **`/party` commands:** create, invite, accept, decline, leave, kick, leader and disband.
  - Invites expire after 5 minutes, and each player can send 10 a minute.
  - Parties hold up to 8 members.
- **Shared API (`JugcraftParties`):**
  - `sameParty`, `isLeader`, `partyMembers`, change listeners.
  - `mayServe` with `UseMode` (Personal/Party), which every automated system will use.
- **Saving:** parties are saved in the world folder (`jugcraft/parties.txt`).
- **Feature switch:** `parties.enabled`.
- **Tests:** seven new game tests, plus a checker rule that every party result has a chat message.

### #81 Engineer's Handbook reorganised, batch 23
- The book fits the window; the chapter list is a scrollable contents list where the open chapter shows its pages, and long pages scroll (mouse wheel or arrow keys).
- New **Progression** chapter: the road through the mod in nine stages, each a plan and a numbered chain of the items to make in order.

### #78 Machine screens redesigned, batch 22
- Every machine screen has a themed look: dieselpunk amber, electric green or lab teal, after the machine's model.
- A control terminal says what the machine is for, what it is doing, its progress, power and power rate, and holds the side controls.

### #77 Solar tracker and heliostats, batch 21
- **Solar tracker:** a panel that tilts after the sun, 20 JE/t in one block.
- **Heliostats** and a **solar receiver**: 12 JE/t per heliostat under open sky in the field below the receiver (up to 48), boiling water.

### #76 Joined tanks, glass tanks and gauges, batch 20
- Tinplate and glass tanks touching each other join into one tank (up to 64), filling from the bottom.
- **Glass tank** shows its fluid; **tank gauge** shows any tank's or machine's level in eighths.

### #75 Turbocharger and flywheel, batch 19
- **Turbocharger** in the advanced engine's new slot, with coolant water in its new second tank: up to 1,536 KE/t and 10% more KE from each mB of fuel.
- **Flywheel:** stores up to 2,000,000 KE of rotation and drives its front shaft from it; friction runs it down slowly.

### #74 Explosive weapons, batch 18
- **Guncotton** (2 cotton + 250 mB nitric acid, chemical reactor).
- **Grenades**, thrown by hand, and the **grenade launcher**, which fires them further. The blast hurts living things only: up to 16 damage, walls shield, and no block, armor stand, frame or dropped item is ever touched.
- New switch `explosives.enabled`. An advancement, a handbook page and a game test.

### #73 Flow batteries, batch 17
- **Vanadium electrolyte:** two asphalt binder and a bucket of sulfuric acid in the chemical reactor.
- **Flow battery** (3×3×2): 1,000 JE per mB of electrolyte in it, up to 64,000,000 JE with 64 buckets; 8,192 JE/t in and out. Keeps its electrolyte when broken.
- An advancement, a handbook page and a game test.

### #72 Glass chemistry, batch 16
- **Tincal**, natural borax, in desert and badlands sand; **borax**.
- **Borosilicate glass** (2 sand + borax, alloy smelter) drawn into **optical fibre**, which can replace gold in processors.
- **Ferroboron** (iron + borax): with a rare earth oxide it makes **two** neodymium magnets.
- Multi-input recipes now try the one with the most ingredients first.

### #70 Chlorine and lye, batch 15
- **PVC:** refinery gas + chlorine → vinyl chloride (synthesis converter) → PVC resin (polymerization reactor) → two plastic sheets each (metal press).
- **Soap** from lye and rotten flesh; a bar washes off every status effect.
- Two advancements, a handbook page and game tests.

### #69 Rubber and polymers, batch 14
- **Butadiene** from naphtha (chemical reactor) and **synthetic rubber** from butadiene (polymerization reactor).
- **Gaskets** (rubber + steel plate); rubber belts; gasketed steel pipe, four for two plates.
- An advancement, a handbook page and a game test.

### #68 Oxygen-blown steel and argon, batch 13
- The air separation unit also makes **argon**.
- **Boost gases:** oxygen piped into the steel foundry, or argon into the crystal grower, doubles its speed.

### #65 Nitrogen chemistry, batch 12: air separation, ammonia and nitric acid
- **Air separation unit** (2×2, six tall): splits air into nitrogen and oxygen, four to one, needing only power.
- **Synthesis converter** (3×4×2): Haber–Bosch ammonia (hydrogen + nitrogen) and Ostwald nitric acid (ammonia + oxygen + water).
- New gases nitrogen, oxygen and ammonia; nitric acid with a bucket.
- Ammonia + phosphate → 6 fertilizer; nitric acid etches microchips with half the acid.
- Three advancements, a handbook section and game tests.

### #62 Surface deposits, batch 11
- **Coal, Iron, Copper and Tin Deposits:** flat patches in the top layer of stony hills (windswept hills, stony peaks, stony shores). Picks only break them, for nothing; each block holds 1,000 units.
- **Deposit drill** (3×3, two tall): takes one coal or raw ore of each kind every 15 seconds from the deposits under it and one block round it, and pushes them into a chest, pipe, conveyor or machine beside it. Empty deposit blocks turn to stone.
- `deposits.enabled` switch, an advancement, a handbook page and game tests.

### #60 Advanced power, batch 10: big solar, a four-cylinder engine and tanks that keep their fluid
- **Advanced solar panel:** a white pedestal carrying a 3×3 array of cells, 64 JE/t in full sun (eight solar panels).
- **Advanced combustion engine** (2 long): gasoline or diesel → up to 1,024 KE/t on a shaft; through a magnet dynamo, the best JE per mB of either fuel.
- **Tanks** have a new look (white with checker bands) and **keep their fluid when broken**: the item carries the fluid and amount, shown in its tooltip.
- **Fix:** loot tables now use the Minecraft 26.x format; the old keys were silently ignored, so ore drop counts, the charging station's upper half and slab doubles were wrong. A data check and a game test guard it.
- Two advancements, handbook pages and game tests.

### #58 Farming, batch 9: harvesters, sprinklers and cotton
- **Crop harvester** (1×2): harvests and replants the ripe crops in the 9×9 field in front of it.
- **Sprinkler:** pipe-fed water gives nearby crops extra growth ticks; it also spreads fertilizer from its hopper.
- **Cotton:** a new crop; seeds from sifting coarse dirt; cotton spins into string.
- Three advancements, a Farming handbook chapter and a game test for each.

### #57 Fluid logistics, batch 8: gas holders, valves and filters
- **Gas holder** (3×3×3 Horton sphere): 1,024 buckets of one gas, and only gases.
- **Fluid valve:** a steel pipe segment that a redstone signal closes, splitting the line in two.
- **Fluid filter:** a steel pipe segment whose neighbouring tanks and machines only receive its chosen fluid; set it with a bucket or from a tank beside it (for gases).
- Three advancements, handbook pages and a game test for each.

### #56 Electronics, batch 7: silicon, chips and the cyan look
- **The cyan look** for the electronics tier, following the owner's references: near-black casings with cyan seams, cyan glass that glows while working, cyan screens, violet conduits, and a beige retro computer.
- **Crystal grower** (1×2): 4 silicon + a phosphate dopant → a silicon boule; the sawmill cuts it into 8 **silicon wafers**.
- **Lithography station** (3×2×2, a cleanroom with a monitor bank): wafer + copper wire + sulfuric acid → 4 **microchips**.
- **Processors:** the third circuit tier (circuit assembler).
- **Network terminal:** a beige retro computer that reads out the power network it is cabled to.
- Four advancements, an Electronics handbook chapter and game tests for each.

### #54 Chemistry, batch 6: advanced materials
- **Titanium:** a new mined metal (deep ore, iron pickaxe, `titanium.enabled`). No furnace smelts it.
- **The Kroll process:** raw titanium + coke + 250 mB chlorine → titanium sponge (chemical reactor); the arc furnace melts it into ingots. Chlorine's first use.
- **Leaching:** lepidolite or monazite + sulfuric acid → 2 lithium carbonate or 2 rare earth oxide.
- **Lithium battery bank** (3×2×1, electric look): 32,000,000 JE, 16,384 JE/t out of its front; built from lithium cells and titanium.
- **Neodymium magnets** (alloy smelter), and the **magnet dynamo** and **magnet motor**: 95% each way, four times the copper-wound rates, cyan-banded.
- Four advancements, handbook pages and game tests for each.

### #52 Chemistry, batch 5: electrochemistry and acids
- **Brine** (chemical mixer: salt + water) and the **electrolytic cell** (3×3×2): brine → **chlorine**, **hydrogen** (gases) and **lye**, each out of its own row.
- **Chemical reactor** (2×2×2): sulfur dust + water → **sulfuric acid**.
- **Alumina and real aluminum:** bauxite + lye → 2 alumina; 2 alumina + coke → 2 aluminum ingots in the cell. Two ingots per bauxite, twice the arc furnace.
- **Fertilizer:** phosphate + sulfuric acid; ripens every crop in a 5×5 area.
- **Fuel cell** (one block, electric look): hydrogen → 128 JE/t.
- Five advancements, a Chemistry chapter in the handbook, and a metal audit for fluid recipes.

### #51 Oil line, batch 4: industry, and the electric look
- **Polymerization reactor** (2×2×3): refinery gas → plastic pellets; the metal press makes **plastic sheets**.
- **Asphalt**, **asphalt slab** and **asphalt road line**: walking on them is 1.3× as fast.
- **Diesel engine** (2×2×3): up to 512 KE/t into a shaft line from diesel or heavy fuel oil, burning only for what is used.
- Nine oil **advancements**, a **Fuel Values** handbook page, JEI categories for every **fluid machine**, and the oil audit in [docs/BALANCE.md](docs/BALANCE.md#oil).
- **Electric look** (owner request): cables are 6 px graphite with a glowing green core (emissive), with copper, silver or aluminum collars; the battery box, capacitor bank, charging station, solar panel, electric pump, electric motor and dynamo are restyled in graphite and green light.
- Fix: a dynamo took up to twice its 128 KE/t on a strong shaft line; it is now capped per tick.
- Screenshots: a power-gear scene, and the multi-block showroom spaced to fit the oil machines.

### #50 Oil line, batch 3: fracking and diesel power
- New fluids: **fracking fluid** and **flowback water** (with buckets).
- **Chemical mixer** (2×2×2): water + sand + dried kelp → fracking fluid.
- **Fracking rig** (3×3×5): over shale oil, pumps fracking fluid down and brings up crude oil, refinery gas and flowback water.
- **Flowback treatment unit** (3×1×2): flowback water → clean water (a quarter lost) + salt.
- **Diesel generator** (3×2×2): 256 JE/t from diesel (256 JE/mB) or heavy fuel oil (128 JE/mB).
- **Gas turbine** (4×2×2): 512 JE/t from gasoline (384 JE/mB) or refinery gas (192 JE/mB), with lubricant upkeep.
- Game tests for each.

### #49 Oil line, batch 2: refining
- **Steel fluid pipes** (1,000 mB/t) and the **heavy pump** (1,000 mB/t); a pipe line now carries as much as its slowest pipe.
- New fluids: **naphtha, diesel, heavy fuel oil, lubricant, gasoline** (with buckets) and **refinery gas** (a gas: tanks and pipes only).
- **Distillation tower** (2×2×7): crude oil → gas, naphtha, diesel and heavy fuel oil, each drawn off at its own height.
- **Catalytic cracker** (2×2×4): heavy fuel oil + steam + catalyst → diesel, naphtha and gas. **Cracking catalyst** from bauxite, sand and nickel.
- **Vacuum distillation unit** (2×2×3): heavy fuel oil → lubricant + asphalt binder.
- **Catalytic reformer** (3×2×2): naphtha → gasoline + gas.
- Game tests for each.

### #47 Oil line, batch 1: oil in the world
The first five commits of the dieselpunk Chemistry branch ([plan](docs/branches/CHEMISTRY.md#petrochemistry-the-dieselpunk-oil-line)).
- **Crude oil:** a real fluid with a bucket; slow, thick, never makes new sources; works in every tank and pipe.
- **Fluid processing machines:** machines with input and output tanks, data-driven fluid recipes and tank gauges on their screens.
- **Oil reservoirs:** hidden, finite oil under Overworld chunks (pumpable or shale), fixed by the seed; the prospector reports them.
- **Pumpjack:** a 1×3×3 dieselpunk nodding donkey that pumps crude oil from the reservoir under it.
- **Oil sand extractor:** a 2×2×2 hot-water plant that washes crude oil out of oil sand and bitumen.
- Game tests for each; a new Oil chapter in the handbook.

### #46 Balance review
- New [docs/BALANCE.md](docs/BALANCE.md): every generator, conversion, store and cost in one place, with the loops that were checked.
- Charcoal burns three quarters as long as coal in Jugcraft's generators and engines. This makes tree-farm wood power slightly weaker (net about +545 JE/t per tree farm, down from +737), as the owner chose. Vanilla furnaces are unchanged.

### #44 Conveyor slopes
- **Conveyor Slope:** carries items one block up or down; use it with an empty hand to switch. Slopes join conveyor runs, and items climb and descend them visibly.
- A game test and a client screenshot.

### #43 Advancements
- A **Jugcraft** advancement tab: 22 steps from the first tin to the rocket pack, earned by having each item. Goals for steel, the steel foundry and the large steam engine; a challenge for the rocket pack.
- The handbook's Getting Started chapter lists the steps on its Milestones pages.
- A game test checks the tree loads; the data checker checks every step's items, title and parent.

### #42 Tool upgrades and a 3D rocket pack
- **Upgrade modules** for the powered tools, fitted at the charging station: Overclock, Range (5×5 drilling), Capacity, Silk Touch and Fortune.
- The worn rocket pack is now a 3D model on the wearer's back.
- The mining drill sits higher in first person.
- Four game tests.

### #41 Dieselpunk steel machines
- The steel foundry, capacitor bank, steel tank, ore drill and high-pressure extractor now look dieselpunk: gunmetal and olive paint, hazard stripes, chrome, phosphor gauges and caged lamps, an exhaust stack on the foundry and a diesel motor on the drill. Same footprints and ports; looks only.

### #40 Powered tools (the first dieselpunk gear)
- **Mining Drill:** a JE pickaxe and shovel, faster than netherite; modes for one block, 3×3 or a whole ore vein.
- **Chainsaw:** a JE axe that also cuts leaves and fells whole trees.
- **Rocket Pack:** worn on the chest; hold jump in the air to fly.
- **Charging Station:** a two-block-tall station that charges the tool on its cradle from cables.
- The tools hold JE instead of wearing out; empty, they mine like a bare hand.
- New dieselpunk textures and detailed 3D item models; [docs/ART_DIRECTION.md](docs/ART_DIRECTION.md) records the rule that higher tiers look dieselpunk.
- Five game tests and three client screenshots.

### #38 Conveyors
- **Conveyor:** carries items (drawn riding on it) the way it faces, 2.5 blocks a second, while rotation drives it: 1 KE per conveyor per tick for a whole joined run. Pipes, hoppers, machines and dropped items load it; it unloads into the conveyor or inventory ahead, or onto the ground. It carries players and mobs too.
- **Conveyor Splitter:** sends items left, straight on and right in turn.
- Four game tests and a client screenshot.

### #37 Spinning shafts and closer screenshots
- Shafts, belt pulleys, the hand crank, the electric motor's shaft and the steam engine's flywheel now really spin (a block entity renderer) instead of scrolling a texture. Shafts placed with earlier builds need re-placing to spin.
- The client test photographs a belt-and-motor line and the multi-blocks from closer, in three views.

### #36 Belts and the Electric Motor
- **Belt Pulley** and **Leather Belt:** link two pulleys up to 16 blocks apart to carry rotation; the belt is drawn between them.
- **Electric Motor:** JE → KE at 75%, up to 96 KE/t.
- Three game tests.

### #35 Bigger machines, a spinning wind turbine, the Large Steam Engine and JEI
- Machines can now fill up to 64 blocks. Resized:
  - **Alloy Smelter:** 3×2×6, with a big copper crucible tank pouring into one funnel over the furnace.
  - **Geothermal Generator:** 2×2×2.
  - **Steel Foundry:** 2×2×5.
  - **Coke Oven:** 2×2×2, with its chimney in a block on top.
  - **Wind Turbine:** 9 tall, with a 7-block rotor that spins (block entity renderer); 12–72 JE/t.
- **Large Steam Engine** (2×2×2): 256 KE/t, four times the small one.
- Machine screens: amber energy readout without a shadow; vanilla tooltips on gauges.
- **JEI:** a recipe page per machine (optional; EMI has no 26.3 build yet).
- Multi-blocks placed with earlier builds need re-placing.

### #32 Polish
- Machines with a fire, and the steam engine, smoke and crackle while running.
- Hovering the energy bar or a tank gauge shows exact JE or mB.
- The eject button reads "Eject" (green on, gray off) instead of a cut-off "Eject: off".
- The CI screenshots no longer show the chat log.

### #30 Auto-Crafter
- **Auto-Crafter:** crafts any crafting-table recipe laid out in its 3×3 grid, one every 2 seconds.
  - Each grid slot keeps one item as the pattern, and pipes and hoppers only top up matching slots.
  - Remainders such as empty bottles get their own slot.
- A powered processor with upgrades, sides, eject, redstone and kinetic power; a new grid layout on its screen.
- Three game tests and a client screenshot of its screen.

### #29 Kinetic power
- A mechanical power layer in **KE** per tick. **Iron Shafts** carry it along their axis and **Brass Gearboxes** out of all six sides; both animate while turning.
- Sources:
  - **Hand Crank:** 16 KE/t while cranked.
  - **Steam Engine:** 64 KE/t from fuel and water, burning only while something takes the power.
- Every powered machine runs straight off a shaft (1 KE = 1 JE). The **Dynamo** bridges KE into JE cables at 75%.
- Three game tests, a client screenshot of a running line, and handbook pages under Power.

### #28 Renewable resources
- **Water Wheel** (2 tall): up to 24 JE/t from flowing or falling water beside its wheel, with no fuel.
- **Cobblestone Generator:** one cobblestone a second from water and lava touching it; neither is used up.
- **Tree Farm:** grows a sapling into six logs and gives the sapling back, sometimes with an extra (apple, cocoa beans, …). Recipes are data for all nine vanilla trees.
- Steampunk models (timber water wheel with a coil dynamo, cistern-and-crucible generator, open brass growth cabinet with a grow lamp) and classic models.
- The handbook gains a "Renewables" chapter; its chapter buttons are packed tighter to fit 11 chapters.
- Three game tests.

### #25 Mining and prospecting
- **Geo-Resonance Prospector:** a hand tool that surveys the 3×3 chunks around you. It opens a steampunk-digital screen: a brass instrument with an amber CRT, valve-tube signal bars, a sweeping scan line and a resonance needle gauge.
  - Readings are deliberately vague: 1–5 bars and shallow, middle or deep for each ore family, never a chunk or block.
- **Ore Drill** (2-tall derrick): mines every ore in a 9×9 column below it, down to the bottom of the world, and refills the holes with rock. It gives whole ore blocks, so ore processing still decides the yield. It has upgrades, side configuration, eject and redstone control.
- Two game tests, a prospector screenshot in the client test, and handbook pages under Materials.

### #24 Storage
- **Capacitor Bank** (2×2): 4,000,000 JE. It charges from any side and gives power out of its front sockets at 4,096 JE/t.
- **Steel Tank** (2×2 squat riveted tank): 128 buckets.
- **Item Crate:** 32 stacks of one item, with right-click in and out and support for pipes, hoppers and comparators.
- Steampunk models (Leyden-jar bank, domed tank, banded crate) and classic models.
- Three game tests, a handbook "Storage" chapter, and a feature record.

### #22 Transmitter tiers
- **Silver Cable** (1,024 JE/t) and **Aluminum Cable** (4,096 JE/t). All cable tiers join one network, which runs at its slowest cable.
- **High-Pressure Extractor** (steel): 32 items every 4 ticks, four times the brass extractor.
- There is no faster fluid pipe: pumps (100 mB/t) are the limit, not pipes.
- Two game tests; handbook pages.

### #20 Engineer's Handbook and in-game screenshots
- **Engineer's Handbook** (book + copper ingot): an in-game guide with 9 chapters and 36 pages. Each page gives what a block does, its power use, its crafting grid and example recipes, and you can hover over items.
- The content is generated from the mod's own tables, so it can't go out of date.
- **Client game tests:** CI starts a real game client, builds a showroom of every machine, opens a machine screen and the handbook, and saves screenshots as a build artifact.

### #19 Machine control
- **Upgrades:** every powered processing machine gets two upgrade slots.
  - **Speed Upgrade:** 4 cards make it 3× as fast for twice the energy per item.
  - **Efficiency Upgrade:** 4 cards bring it to 41% of the energy.
  - Both are made from steel.
- **Redstone mode button:** ignored, run with a signal, or run without one.
- **Comparators** read stored energy (generators, battery box) or how full a machine is, from any block of a multi-block.
- Energy readouts are shortened (for example "12.5k / 20k JE") to fit the new slots.
- Four new game tests.

### #18 Steel tier
- **Coke Oven** (2 tall) bakes coal into **Coal Coke**. Coke is a 3,200-tick generator fuel and the carbon for steel.
- **Steel Foundry** (3 tall) turns 1 iron ingot + 1 coke into 1 **steel ingot**.
- Both are unpowered brick multi-blocks with steampunk and classic models.
- New steel items: ingot, nugget, block, plate and gear.
- **Fix:** cables drew a connection arm to every face of a machine, even where no power goes in (for example all around the alloy smelter). Now the arm and the energy flow use the same check.
- Three new game tests, including one for the cable fix.

### Merged 30 September 2026: PRs #8–#17

These were built as a stack, each on the one before, and merged in order (#8 first).

#### #17 Changelog and "What Exists" guide
- Adds this `CHANGELOG.md` and [`docs/WHAT_EXISTS.md`](docs/WHAT_EXISTS.md). WHAT_EXISTS is a map of all content, shared APIs, data formats, file locations and check rules, so contributors and AI agents can build alongside the existing systems.
- Adds feature records for [item logistics](docs/features/item-logistics.md) and [ore processing](docs/features/ore-processing.md).

#### #16 Ore processing depth
- **New machines:**
  - **Pulverizer:** ore → 2 dust, plus a byproduct roll.
  - **Ore Washer:** ore + water → 3 washed ore, which the pulverizer grinds.
  - **Sieve:** gravel → flint; soul sand → soul soil; both with small finds.
  - **Sawmill:** log → 6 planks + sawdust; planks → 3 sticks.
- **Three ore routes:** smelt (×1), crush or pulverize (×2), wash then pulverize (×3).
- **New items:**
  - 10 metal dusts (`c:dusts/<metal>`), which smelt into ingots. Nickel, tungsten and uranium dust use the arc furnace.
  - 10 washed ores.
  - Sawdust (4 → paper).
- **Byproducts:**
  - Machine recipes can list byproducts: chance, count, and an optional feature switch.
  - The pulverizer, sieve and sawmill have two byproduct slots, and a machine waits rather than lose a byproduct.
  - Byproduct pairs follow real ores, for example copper → gold and lead → silver.
- **Looks:** steampunk models (ball mill, washing vat with a water wheel, shaker sieve, sawbench) and classic textures.
- **Balance checks:** only ores get a bonus (×3 at most); byproducts add at most 25% of the input's metal; renewable sieve finds average under a nugget per operation.
- Seven new game tests.

#### #15 Item logistics
- **Side configuration:** every processing machine's screen has six face buttons (front, back, left, right, top, bottom). Each cycles between In, Out, Both and Off. The defaults keep the old hopper behavior.
- **Eject:** when on, the machine pushes its results out of its Out faces, 16 items every 8 ticks.
- **New blocks and items:**
  - **Brass Item Pipe:** 6 px. Items go first to matching sorters, then round-robin to the other inventories.
  - **Pneumatic Extractor:** 16 items every 8 ticks; a redstone signal pauses it.
  - **Item Sorter:** 9-slot filter.
  - **Brass Wrench:** turns machines; sneak to dismantle.
- Three new game tests.
- Fix: a multi-block machine's eject never feeds back into its own other blocks.

#### #14 Machine recipes as data, and in-game tests
- Machine recipes are now real Minecraft recipe types (`jugcraft:crushing`, `alloying`, …), one JSON file each under `data/<ns>/recipe/<type>/`. Data packs can add, change or remove them, and ingredients may be tags.
- In-game tests (Fabric game test API) run in CI with `./gradlew build`.
- **Launch-blocking fixes found by those tests** (before this PR, `main` could not start a server):
  - Fabric Loader 0.18.4 → **0.19.3**, which Fabric API 0.161.0+26.3 requires.
  - Ore worldgen moved to the 26.x format: `worldgen/feature/`, no `config` wrapper, block states as plain IDs.

#### #13 Steampunk machines
- Every machine redrawn as a detailed steampunk model: brass, copper, riveted iron, gauges, gears, valve wheels, and fireboxes that glow while running. Includes 37 original textures.
- The previous look stays available as the built-in resource pack **Jugcraft: Classic Machines**, so the change can be reverted.

#### #12 2×2 alloy smelter and power sockets
- The alloy smelter becomes a 2×2 multi-block with a visible copper **power socket**, the only place cables connect to it.
- Clear power-connection rules for every machine: each one either has a socket or takes power on any face that touches a cable.

#### #11 Multi-block machines
- Machines can occupy several blocks and are placed as one item, in the style of Immersive Engineering. Breaking any part removes the whole machine.
- **Geothermal Generator** (2 wide, lava → 64 JE/t) and **Wind Turbine** (3 tall, 4–24 JE/t by height and weather).

#### #10 Fluids, and tech-mod style cables and pipes
- **Bronze Fluid Pipe**, **Tinplate Tank** (16 buckets) and **Electric Pump**.
- Cables and pipes are thin (4 px) and connect to each other and to anything with energy or fluid storage, as in other tech mods. Items show as 3D segments.

#### #9 Parts and circuits
- **Metal Press** (ingot → plate), **Wire Drawer** (ingot → 3 wires), **Circuit Assembler** (basic and advanced circuits).
- Gears are crafted from four plates.

#### #8 Alloy smelter
- **Alloy Smelter** makes bronze, **brass**, **invar** and **solder** at metal-conserving ratios.

### Merged earlier on 30 September 2026

#### #7 Solar panel and steam generator
- **Solar Panel** (8 JE/t in sun) and **Steam Generator** (coal or bitumen plus water → 64 JE/t), plus a machine roadmap.

#### #6 Electricity and the first machines
- Jugcraft Energy (JE) with a shared `EnergyStorage` API, **Copper Cable** networks, **Coal Generator**, **Battery Box**, **Electric Furnace**, **Crusher** (ore doubling) and the **Arc Furnace** 3×3×3 multiblock.

#### #5 Base materials
- Zinc, lead, silver, nickel, tungsten, uranium, aluminum (bauxite), salt, phosphate, lithium (lepidolite), rare earths (monazite), sulfur, silicon and oil sand (bitumen).
- Feature switches in `config/jugcraft.properties`.

#### #4 Fabric 26.3 scaffold, tin and bronze
- Pinned toolchain and the CI Build workflow.
- Tin ore, raw tin, ingots and blocks, and bronze from a 3:1 copper/tin blend.
- The offline data generator and checker (`tools/`).

#### #1 Vision and development setup
- Project vision, contribution rules, design and architecture documents, and owner-directed Fabric development.
