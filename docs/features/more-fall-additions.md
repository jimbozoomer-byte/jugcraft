# More Fall Additions

Status: the candy kitchen (addition 11), autumn foraging (addition 12), the Bat House (addition 13) and the Hay Golem (addition 14) are implemented in source, not yet played by hand. The Build workflow compiles them, and CI's game tests and client screenshots are recorded below.
Proposal issue: none; requested directly by the owner on 2 October 2026 ("lets do another 10 detailed halloween and fall themed additions", then "start them now stacked on #33"). They follow the ten [fall additions](fall-additions.md), numbered on from them, one per pull request, each stacked on the one before:
11. the candy kitchen: the Candy Kettle, its thermometer and the candy stages, the Candy Tray, and ten candies;
12. autumn foraging: five wild mushrooms that spread in the shade and sprout fairy rings under the full moon, the Foraging Basket, and four mushroom dishes;
13. the Bat House: a roost for bats that fly out at dusk and back at dawn, leaving guano, a fertilizer and a source of phosphate;
14. the Hay Golem: a straw man with a carved pumpkin head that guards crops from crows and tends them, carrying the harvest home;
15. knitting and sweaters (planned);
16. pie baking (planned);
17. the Spirit Board (planned);
18. wild turkeys (planned);
19. the theremin (planned);
20. the Día de Muertos ofrenda (planned).

Owner: @jimbozoomer-byte
Target milestone and tier: the candy kitchen is Discovery tier: copper ingots and a glass pane (the kettle), iron nuggets (the tray), sugar, a water bottle or a milk bucket, a campfire, dyes, sticks (for lollipops), and the flavours, all early: cocoa beans, sweet berries, glow berries, a honey bottle, and the Festival Crops' cranberries, the cider mill's mulling spices and roasted chestnuts. Autumn foraging is Discovery tier: the mushrooms are found on the forest floor from the first day, the basket is sugar cane and a stick, and the dishes cook in a furnace, smoker, campfire or the Cooking Pot. The Bat House is Discovery tier: seven planks and a stick. The Hay Golem is Discovery tier: four hay bales (36 wheat) and a carved pumpkin.
Primary specialty and supported player role: cooking and crafting. A candy maker turns sugar cane into the treats that fill Candy Bowls and Candy Bags, and flavoured candy is a pocketful of short effects (speed, haste, night vision, resistance, fire resistance, absorption, regeneration) to hand round before a dig or a fight. Candy is easy to trade: each piece says its flavours in its name. Autumn foraging is for explorers and cooks: mushrooms to find, farm in the shade and cook, and fairy rings to find or plant for a night's Luck before fishing or opening loot. The Bat House is for farmers: a free trickle of fertilizer from a block on a wall, and phosphate for a chemist with no phosphorite near. The Hay Golem is a farmhand: it keeps crows off a field and brings in the ripe crops, so a small farm runs while its farmer is away mining.

Nothing here depends on the Halloween event: the kettle boils candy all year, the mushrooms grow all year, and fairy rings bless on every full moon. Bats come and go every night, and a Hay Golem works all year.

## Player experience
### The Candy Kettle
1. A polished copper sugar pot with two handles and a round candy thermometer clipped to its front (seven copper ingots and a glass pane). It faces you when you place it. Set it over a heat source: a lit campfire, fire, soul fire, lava or a magma block (the same `jugcraft:heat_sources` the Cooking Pot uses).
2. **Fill it before it boils** (anything added at 100°C or hotter is refused: "put everything in before it boils"):
   - **a base:** a water bottle for sugar syrup (the bottle comes back), or a milk bucket for cream (the bucket comes back). One base a batch.
   - **sugar:** up to four. Each sugar makes two pieces of candy.
   - **flavours:** up to two, each once (listed below).
   - **dyes:** any number, mixed as on leather. Undyed candy takes its first flavour's colour, or its own.
3. **Heat it.** With a base and sugar in, over the heat it warms a degree every 4 ticks to the boil (100°C); slowly, a degree every 12 ticks, while its water boils off to 110°C; then a degree every 6 ticks, up to 190°C. Bubbles rise from it from the boil, steam past caramel, and smoke once it burns.
4. **Watch the thermometer.** The needle swings round a dial painted with the stages in their colours. A bell rings a note higher as the batch reaches each stage; burning hisses. With an empty hand, use the kettle to read it: "Candy Kettle: 117°C (Soft Ball). Sugar Syrup, 3 sugar, Chocolate. Pour now for Candy Corn".
5. **The hottest it has been decides the candy**, so taking it off the heat (putting the campfire out with a shovel, say) holds it at its stage while it cools. Off the heat it cools a degree every 8 ticks.
6. **Pour** by using it with an empty Candy Tray (one tray of a stack). Pouring empties the kettle and earns **Sweet Science**. Sneaking with an empty hand tips the batch out instead.
7. **Comparators** read the stage a batch has reached: 0 for syrup (or an empty kettle) up to 8 for burnt.

### The stages
| Stage | From | Sugar syrup sets into | Cream sets into |
| --- | --- | --- | --- |
| Syrup | below 110°C | not ready | not ready |
| Thread | 110°C | **Rock Candy** | not ready (too runny) |
| Soft Ball | 115°C | **Candy Corn** | **Fudge** |
| Firm Ball | 120°C | **Candy Corn** | **Cream Caramel** |
| Hard Ball | 125°C | **Salt Water Taffy** | **Cream Caramel** |
| Soft Crack | 132°C | **Salt Water Taffy** | **Toffee** |
| Hard Crack | 145°C | **Hard Candy** (or lollipops) | **Toffee** |
| Caramel | 155°C | **Caramel** (the Halloween harvest's, for caramel apples) | **Burnt Sugar** |
| Burnt | 175°C | **Burnt Sugar** | **Burnt Sugar** |

From the boil, each stage lasts between 30 ticks (thread, soft ball, firm ball) and 120 ticks (caramel) of heating, so a cook has a second or more to pour at the narrowest, or can take the kettle off the heat to stop it.

### The Candy Tray
1. A tin tray of five iron nuggets, stacking to 16; a tray of candy doesn't stack. It shows its candy in the candy's colour and is named for it: "Tray of Chocolate Fudge".
2. **Setting:** use a tray of candy to break it into its pieces once it has set (five seconds), and the tray is empty again. Until then: "Still setting".
3. **Rock candy** grows its crystals for a whole day (24,000 ticks) before it can be broken up.
4. **Taffy** must be pulled while it is warm (for 30 seconds after pouring): hold use for a second to pull it, four times. Pulled ("light and chewy"), it cuts into Salt Water Taffy, and earns **Pulling Power**. Left to go cold before it's pulled, it sets hard: it breaks up as hard candy.
5. **Hard candy** broken up with sticks in your other hand makes a lollipop a stick (the sticks are used), and hard candy with the rest.
6. **Candy corn** is poured in layers: pour another batch of candy corn onto a tray of it, up to three layers, each its own colour. The pieces have three bands, tip to base: one layer colours all three, two the tip and the rest, three one band each. An undyed layer takes the classic colour of its band (white, orange, yellow), so three undyed layers make plain candy corn, which stacks with candy corn from trick-or-treating. Two flavours at most across the layers.

### Candy
- **Rock Candy** (2 food), **Salt Water Taffy** (2), **Hard Candy** (1), **Lollipop** (2), **Fudge** (3), **Cream Caramel** (2), **Toffee** (2) and **Burnt Sugar** (1), plus the Halloween harvest's **Candy Corn** and **Caramel**.
- Candy is eaten quickly (0.8 seconds), and even on a full stomach. Every candy counts as candy (`c:foods/candy`), so Candy Bowls, Candy Caches and Candy Bags take it.
- **Flavoured candy** is named for its flavours ("Chocolate Fudge", "Glow Berry and Honey Lollipop"), lists them in its tooltip, and gives each flavour's effect when eaten:

| Flavour | Stirred in with | Effect when eaten | Colour (undyed) |
| --- | --- | --- | --- |
| Chocolate | cocoa beans | Speed, 10 s | dark brown |
| Berry | sweet berries | Regeneration, 4 s | red |
| Glow Berry | glow berries | Night Vision, 30 s | gold |
| Honey | a honey bottle (the bottle comes back) | Absorption, 10 s | amber |
| Cranberry | cranberries | Resistance, 10 s | crimson |
| Spiced | mulling spices | Fire Resistance, 15 s | cinnamon |
| Chestnut | roasted chestnuts | Haste, 15 s | chestnut brown |

- Burnt sugar keeps no flavour: it tastes of nothing but burning. Caramel keeps its flavours but not its dye.

### Wild mushrooms
1. Five wild autumn mushrooms grow in patches on the forest floor, each in its own biomes:

| Mushroom | Found in | Use |
| --- | --- | --- |
| **Chanterelle** (golden, frilled) | forests, birch forests | Sautéed Chanterelles, Forager's Stew |
| **Porcini** (a fat brown cap on a pale stem) | taiga, forests | Roasted Porcini, Forager's Stew |
| **Puffball** (a white ball) | plains, forests | Fried Puffball, Forager's Stew; bursts in a cloud of spores when picked |
| **Fly Agaric** (red with white spots) | birch forests, taiga | not food; fairy rings and decoration |
| **Jack o'Lantern Mushroom** (an orange cluster) | spooky biomes, forests | not food; it glows (light 9) |

2. They grow on soil: grass, dirt, coarse or rooted dirt, podzol, mycelium and moss (the block tag `jugcraft:mushroom_soil`), not on stone or sand.
3. **They spread in the shade.** One random tick in 25, a mushroom with fewer than five of its kind within four blocks puts out another nearby (two blocks across, a block up or down), where the light is below 13. Under trees a patch fills in; in the open it doesn't.
4. **Bone meal** makes it spread at once, in any light, still up to five.
5. Picked, each drops itself; they go in a composter.

### Fairy rings
1. **A fairy ring** is eight or more wild mushrooms in a circle between 2.5 and 3.6 blocks from a centre (a block up or down counts), of any kinds.
2. **Mushrooms sprout rings under a full moon.** On a full-moon night, one random tick in 40 a wild mushroom plants a ring of its own kind: a circle of radius three that it stands on. A mushroom goes wherever the ground takes one, and nothing sprouts where a ring already stands. Anyone can plant a ring by hand too.
3. **Stand in its centre on a full-moon night** (in the Overworld) and you are blessed, once a night: Luck II for five minutes, a chime, a shimmer round the ring, "You dance in the fairy ring under the full moon. You feel lucky", and the advancement **Away with the Fairies**. Luck improves fishing and loot from chests.

### The Foraging Basket
1. A wicker basket (five sugar cane and a stick). It holds forage, like a bundle: wild mushrooms, vanilla mushrooms, sweet berries, glow berries, apples, cocoa beans, chestnuts and cranberries (the item tag `jugcraft:forage`), and nothing else.
2. **Pick mushrooms with it in either hand** and they go straight into it (what doesn't fit drops as usual).
3. A basket holding all five wild mushrooms earns **Forager**.

### Mushroom dishes
- **Sautéed Chanterelles** (5 food), **Roasted Porcini** (6) and **Fried Puffball** (4): the mushroom cooked in a furnace, smoker or campfire.
- **Forager's Stew** (10 food): a chanterelle, a porcini, a puffball, a potato and a bowl in the Cooking Pot.
- All four are tagged `c:foods`, so the Harvest Feast Table counts each as a different food.

### The Bat House
1. A slatted wooden roost with a little roof and a landing tray below it (seven planks and a stick). Hang it on the side of a block (it faces out from it) or stand it on top of one (it faces you).
2. **Bats roost in it by day.** At **dusk** every roosting bat pours out of it, a puff and the sound of wings, and anyone within 16 blocks earns **Night Shift**. Bats let out are ordinary bats, tagged as the house's.
3. **At dawn** the bats nearest it, within 32 blocks (its own and wild ones), come in to roost, up to four. Each leaves a **Bat Guano** on the tray, up to 16 waiting. The tray shows none, a little, more or a pile.
4. **A new house fills by itself:** at dusk, while mobs spawn (the `spawn_mobs` game rule), a house with room has an even chance of a bat moving in first, so a house with no bats near still has residents in a few days.
5. **Use it with an empty hand** to scoop up the guano ("Bat House: 3 of 4 bats roosting. You scoop up 7 guano"), or just to see how many bats roost.
6. **Comparators** read the roosting bats: 0 for none, 15 for a full house.
7. Broken, it lets its bats out (none are lost) and drops its guano and itself.

### Bat Guano
- **A fertilizer:** use it on a crop or the ground, and every crop in the 3x3 patch round it (a block up or down too) gets a dose of bone meal, by its own rules. One guano is used if anything grew. Only crops (`minecraft:crops`), never grass or saplings. It is the superphosphate fertilizer's rule over a smaller area and one dose.
- **Phosphate:** four guano make a phosphate (crafting), the same phosphate that is mined from phosphorite ore.
- It goes in a composter.

### The Hay Golem
1. **Build it like an iron golem, from hay:** two hay bales stacked, a hay bale either side of the upper one for arms (nothing under the arms), then use a carved head on the top of the upper bale: a carved pumpkin, a jack o'lantern, or a hand-carved pumpkin with a face cut (any of the `jugcraft:hay_golem_heads`). The hay comes to life as a straw man two and a half blocks tall, wearing the head and facing its builder, and the builder earns **Man of Straw**. A plain pumpkin, an uncut one, a missing arm or a block under an arm leaves it as hay, so pumpkins can still decorate hay bales.
2. **Its post:** it keeps to where it was built, wandering within eight blocks. Hold wheat near it and it follows you; where you leave it is its new post.
3. **A walking scarecrow:** crows won't raid crops within eight blocks of it, or twelve if its head is lit (a jack o'lantern, or a hand-carved pumpkin with a torch), as a scarecrow wearing that head would guard.
4. **It tends the crops round its post:** every five seconds it looks for a ripe crop within eight blocks of its post (two up or down). It walks to it, bends over it a moment and harvests it, replanting it from its own drops when they include its seed (wheat seeds, a carrot, a potato, a beetroot seed, Jugcraft's seeds). The rest goes in its pouch of nine stacks.
5. **It carries the harvest home:** with 32 items or more, or once nothing has been ripe for ten seconds, it walks back to its post and puts its pouch into the chest, barrel or hopper under it, or sets it down there if there is none. If it can't get home in fifteen seconds it sets its load down where it is.
6. It harvests only while the `mob_griefing` game rule is on, as crows only peck while it is.
7. **Looking after it:** wheat heals it 4 health. Shears take it apart again: its four hay bales, its head and its pouch. Fire hurts it twice as much. It never despawns. Killed, it drops two to five wheat, its head and its pouch.

## Connections
- Candy kitchen, input producer: sugar cane (sugar); bottles of water and cows (milk); cocoa, sweet berries, glow berries and bees; the Festival Crops (cranberries, roasted chestnuts) and the cider mill (mulling spices); dyes; sticks; vanilla copper and iron; Jugcraft's `jugcraft:heat_sources`.
- Candy kitchen, output consumer:
  - Candy Bowls, Candy Caches and Candy Bags (`c:foods/candy`);
  - caramel for caramel apples (the Halloween harvest);
  - short effects for anyone (miners, explorers, fighters);
  - food for the Harvest Feast Table, which counts every kind of candy as a different food.
- Candy kitchen, technology connection: comparators read the stage, so redstone can watch a kettle.
- Candy kitchen, entry path: everything is early-game; no candy needs another candy, and the kettle needs no other Jugcraft block.
- Candy kitchen, required vs optional: optional and all year. One cook can make every candy alone.
- Candy kitchen, trade and solo routes: candy stacks (identical pieces) and says what it is, so a candy maker can sell to others.
- Autumn foraging, input producer: the world (forest, taiga, plains and spooky biomes), bone meal, and sugar cane and sticks for the basket.
- Autumn foraging, output consumer:
  - the four dishes, and through them the Harvest Feast Table (`c:foods`);
  - Luck for fishing and loot;
  - the Foraging Basket carries the Festival Crops' chestnuts and cranberries and the cider mill's apples too;
  - the jack o'lantern mushroom is a soft light for spooky builds.
- Autumn foraging, entry path: found from the first day; nothing needs another Jugcraft item.
- Autumn foraging, required vs optional: optional; nothing else needs a mushroom. A player who finds none can grow a patch from one by bone meal and shade, or trade for them.
- Autumn foraging, trade and solo routes: mushrooms and dishes stack and trade; a forager can plant fairy rings for others.
- Bat House, input producer: planks and a stick; vanilla bats (from caves, or moving in by themselves).
- Bat House, output consumer: farms (guano grows crops); phosphate for the existing recipes that use it (superphosphate fertilizer in the chemical reactor, silicon boules); composters.
- Bat House, technology connection: comparators read the bats; guano is a second source of phosphate, so a player with no phosphorite near can still make superphosphate.
- Bat House, entry path: two early items; no other Jugcraft block needed.
- Bat House, required vs optional: optional. Phosphate is still mined from phosphorite as before.
- Bat House, trade and solo routes: guano stacks and trades; anyone can hang houses alone.
- Hay Golem, input producer: wheat (hay bales); the carved pumpkins of the Halloween harvest and pumpkin carving.
- Hay Golem, output consumer: the crops it harvests (into a chest, barrel or hopper, and on into any storage or machine line); crows' working scarecrows (it guards as one does).
- Hay Golem, technology connection: a hopper under its post feeds the harvest into storage or machines.
- Hay Golem, entry path: early-game wheat and a carved pumpkin; no other Jugcraft block needed.
- Hay Golem, required vs optional: optional; farming by hand is unchanged.
- Hay Golem, trade and solo routes: anyone can build one alone; it harvests for whoever's farm it stands in.

## Balance and automation
- **Candy kitchen:**
  - Costs: a kettle of seven copper ingots and a glass pane; a tray of five iron nuggets (reused). A batch is a water bottle or a milk bucket (both containers come back), one to four sugar, and up to two flavour items and any dyes, used once a batch.
  - Units: degrees Celsius; ticks (20 a second); pieces (two a sugar, eight at most a batch, 24 at most on a tray of three candy corn layers).
  - Heating from room temperature (20°C): 320 ticks to the boil, 120 more to 110°C, then 30 ticks to each 5°C stage (6 ticks a degree): about 22 seconds to thread, 33 to hard crack, 41 to burnt. Cooling: 8 ticks a degree.
  - Setting: 100 ticks; rock candy 24,000 ticks; taffy pulled 4 times, 20 ticks each, within 600 ticks.
  - Effects are short (4 to 30 seconds a piece) and the same as vanilla potions'; candy gives no effect stronger than level I.
  - Nothing is made from nothing: candy comes only from sugar, and candy makes nothing else (caramel goes into caramel apples, as before). No loop.
- **Autumn foraging:**
  - Costs: the basket is five sugar cane and a stick. A dish is one mushroom (Forager's Stew three and a potato). Bone meal, once a try.
  - Units: random ticks (vanilla's random tick speed); blocks; ticks for Luck (6,000, five minutes).
  - Spreading: one random tick in 25, at most five of a kind within four blocks, only below light 13 (bone meal in any light). A shady patch fills in over a few in-game days; it never covers a forest.
  - Worldgen: each mushroom's patch is placed once in 16 chunks of its biomes, 12 tries over a 9 by 9 area, on soil only.
  - Fairy rings: one random tick in 40 on a full-moon night (one night in eight). The blessing is once a night a player, Luck II for five minutes.
  - The food values sit with vanilla's cooked foods (a baked potato is 5, the stew is a mushroom stew and more).
  - Mushrooms only multiply in place, a few at a time, and nothing converts them into more of anything. No loop.
- **Bat House:**
  - Costs: seven planks and a stick. Guano costs nothing but time.
  - Units: bats (four a house at most), guano (one a bat a dawn, 16 waiting at most), blocks, ticks.
  - Yield: at most four guano a house a day. Four guano make one phosphate, so a full house gives at most one phosphate a day; a phosphorite ore drops one to three.
  - Fertilizer: one guano grows each crop in a 3x3 patch by one bone-meal dose, against superphosphate's 5x5 and two doses: a guano is about a fifth of a superphosphate.
  - New bats: an even chance a dusk for a house with room, only while mobs spawn.
  - No loop: guano and phosphate make nothing that makes bats or houses.
- **Hay Golem:**
  - Costs: four hay bales (36 wheat) and a carved pumpkin; shears give back all of it.
  - Units: blocks, ticks, items.
  - Work: at most one crop every few seconds (it walks to each and works at it half a second); villager farmers do the same kind of work.
  - It only replants from the crop's own drops: no seed is made from nothing, and a crop whose drops hold no seed isn't replanted.
  - No loop: a golem makes nothing that makes golems; its wheat harvest could build another, as wheat grown by hand could.

## Multiplayer and persistence
- **Candy kitchen, server authority:** filling, reading, tipping out and pouring all go through vanilla's block use path (reach, spawn protection, adventure mode) and are decided on the server, which checks the base, the sugar and flavour limits, and the temperature. Pulling and breaking up a tray go through vanilla's item use; the server checks the tray's own record of when it was poured and how often it has been pulled, by its own game time.
- **Candy kitchen, what clients get:** the kettle's batch and temperature, to draw the syrup and the needle; the tray's batch, to draw it. Clients decide nothing.
- **Candy kitchen, concurrent use:** two players at one kettle share its batch; whoever pours first gets it, and the second finds it empty.
- **Candy kitchen, persistence:** the kettle saves its base, sugar, flavours, dye sums, temperature and the hottest it has reached; a tray keeps its batch (a data component), so trays of candy keep across a restart and in chests, and rock candy keeps growing by game time.
- **Candy kitchen, bounded work:** a kettle does nothing unless it is warmer than the room or heating a batch; a degree changes at most every 4 ticks, and only then is it sent to clients.
- **Candy kitchen, IDs:** blocks/items `candy_kettle`, `candy_tray`, `rock_candy`, `salt_water_taffy`, `hard_candy`, `lollipop`, `fudge`, `cream_caramel`, `toffee`, `burnt_sugar`; block entity `candy_kettle`; data component `jugcraft:candy_batch`; item tags `jugcraft:candy_flavours/*`; advancements `candy_maker` and `taffy_puller`; the kettle's and tray's recipes. All new. Candy corn's item model now takes its band colours from the vanilla `custom_model_data` component (plain candy corn keeps its white, orange and yellow).
- **Candy kitchen, disable behaviour:** with the agriculture feature off, the kettle's and tray's recipes don't load; kettles, trays and candy already made still work, and everything stays registered.
- **Autumn foraging, server authority:** spreading, sprouting rings and blessing are the server's (random ticks and a server tick). Picking goes through vanilla's block breaking; the basket fill happens on the server, from the block's own drops. The blessing checks the player's own position on the server; nothing is asked of the client.
- **Autumn foraging, what clients get:** the blocks and the basket's contents (vanilla's bundle component); particles and sounds.
- **Autumn foraging, persistence:** mushrooms are blocks. Who was blessed tonight is kept in the server's memory only: a restart in the middle of a full-moon night lets players be blessed once more that night.
- **Autumn foraging, bounded work:** a random tick looks at the 243 blocks within four blocks and up to four spots. The server looks at each Overworld player every 20 ticks, only on full-moon nights, reading the blocks of the ring round them (the columns between 2.5 and 3.6 blocks off, three blocks high).
- **Autumn foraging, IDs:** blocks/items `chanterelle`, `porcini`, `puffball`, `fly_agaric`, `jack_o_lantern_mushroom`; items `foraging_basket`, `sauteed_chanterelles`, `roasted_porcini`, `fried_puffball`, `foragers_stew`; placed features `patch_<mushroom>`; block tags `jugcraft:mushroom_soil` and `jugcraft:wild_mushrooms`; item tags `jugcraft:forage` and `jugcraft:wild_mushrooms`; advancements `forager` and `fairy_ring`; their recipes. All new.
- **Autumn foraging, disable behaviour:** with the agriculture feature off, the recipes don't load; the mushrooms still generate and grow, the basket still works, and everything stays registered.
- **Bat House, server authority:** dusk, dawn and moving in are the server's, by the Overworld clock. Scooping goes through vanilla's block use path (reach, spawn protection, adventure mode) and is decided on the server.
- **Bat House, what clients get:** the block state (facing, how much guano shows), and the house's bats and guano for the status message; vanilla bats as ever.
- **Bat House, persistence:** the house saves its roosting bats, its guano and whether it last saw night, so a restart at dusk or dawn neither loses nor doubles the change. Bats out at night are ordinary bats, saved as vanilla saves them; a house doesn't keep track of which bats are its own.
- **Bat House, bounded work:** a house looks at the clock every 20 ticks. At dawn it looks for bats within 32 blocks once, through the entity lookup (it never loads a chunk), and takes at most four.
- **Bat House, IDs:** block and item `bat_house`, item `bat_guano`, block entity `bat_house`, entity tag `jugcraft.bat_house`, advancement `night_shift`, recipes `bat_house` and `phosphate_from_bat_guano`. All new. `FertilizerItem` now takes its area and doses (superphosphate's are as before).
- **Bat House, disable behaviour:** with the agriculture feature off, the recipes don't load and no bat moves in; bats still roost and leave guano, and everything stays registered. With the phosphate feature off, the phosphate recipe doesn't load.
- **Hay Golem, server authority:** building is checked on the server: the T of hay, a head with a face, the builder able to build there (adventure mode can't), through vanilla's block use path (reach, spawn protection). Its tending, guarding, healing and shearing are the server's.
- **Hay Golem, what clients get:** the golem, its head (an item stack, to draw), and whether it is bent over a crop.
- **Hay Golem, persistence:** the golem saves its head, its post and its pouch; it never despawns.
- **Hay Golem, bounded work:** it looks for a ripe crop at most every 100 ticks (and right after a harvest), over the circle of eight blocks round its post, five blocks high, in loaded chunks only. A crow looking for a guard asks for golems within twelve blocks through the entity lookup.
- **Hay Golem, IDs:** entity `hay_golem`, item tag `jugcraft:hay_golem_heads`, loot table `jugcraft:entities/hay_golem`, advancement `man_of_straw`. All new.
- **Hay Golem, disable behaviour:** with the agriculture feature off, no new golem can be built and golems don't harvest; they still stand and guard, and everything stays registered.

## Dependencies and assets
Candy kitchen:
- No new dependencies. Textures are drawn by code in `tools/candy_textures.py`: the kettle's polished copper and dark inside, its thermometer dial (the stages painted round it in their colours, a tick where each starts), the syrup's surface and the needle for the renderer, the tray empty and the candy on it, and each candy, drawn pale where it takes its colour, with its stick or wrapper in a layer of its own; candy corn in three bands and an outline.
- The models, blockstates, item models (each candy tinted by the vanilla `dyed_color` component; candy corn's bands by `custom_model_data`), names, tooltips, messages, loot and tags come from `tools/candy_data.py`; the numbers from `CANDY` in `tools/agriculture.py`.
- The client's `CandyKettleRenderer` draws the batch at the height of its sugar, browning past hard crack and black once burnt, and the needle on the dial.
- Sounds are vanilla's (a note block's bell at each stage, fire going out when it burns, sand, a bottle, a bucket, dye, honey, amethyst breaking for hard candy).

Autumn foraging:
- No new dependencies. Textures are drawn by code in `tools/foraging_textures.py`: each mushroom's cross-shaped plant (gills and spots, a cluster for the jack o'lantern mushroom), the basket (a red cloth and a chanterelle peeking out), and the dishes.
- Models, item models, names, loot, recipes, tags, worldgen and advancements come from `tools/foraging_data.py`; the numbers and biomes from `FORAGING` in `tools/agriculture.py`. Each mushroom's patch is added to its biomes by Fabric's biome modification API.
- Sounds and particles are vanilla's (wool breaking and white ash for a bursting puffball, an amethyst chime and end-rod sparkles at a fairy ring).

Bat House:
- No new dependencies. Textures are drawn by code in `tools/bat_textures.py`: weathered planks, the slatted front, the guano on the tray, and the guano item.
- Models (the house and its tray with one, two or three piles), blockstates, names, messages, loot and tags come from `tools/bat_data.py`; the numbers from `BATS` in `tools/agriculture.py`. The recipes are in its shaped and shapeless tables.
- Sounds and particles are vanilla's (a bat taking off and smoke at dusk).

Hay Golem:
- No new dependencies. Its texture is drawn by code in `tools/hay_golem_textures.py`: golden straw running along each bundle, twine bands round the body, wrists and ankles, a sackcloth patch on the chest, and loose straw at the collar, wrists and ankles.
- The client's `HayGolemModel` is a hay-bale body with a straw collar, straw-bundle arms and legs with tufts at the ends; it walks with a stiff swing, sways standing, and bends with its arms down to harvest. `HayGolemRenderer` draws the head it wears on the collar: the pumpkin's own item model, and a hand-carved pumpkin's carving over it (lit at full brightness), as the scarecrow draws its head.
- Its name, loot, head tag and advancement come from `tools/hay_golem_data.py`; the numbers from `HAY_GOLEM` in `tools/agriculture.py`.
- Sounds are vanilla's (grass for its steps, hurt and death; shearing; hay placed when it is built).

## Verification
### Candy kitchen verification

Actual results (2 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/agriculture-halloween-decor-25` stacked on #33:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares the candy kitchen with Java: the batch limits, temperatures and heating rates; the tray's setting, pulling, crystal and layer times; the eating time and candy corn's bands; each stage and where it starts; which candy each base sets into at each stage; each kind's colour; each flavour's effect, time and colour; the candies' food; and checks every candy, stage, base and flavour has its words, textures and tags, and the recipes and advancements exist) | Pass, 631 IDs |
| `./gradlew build` on `fdbbbae` (Build workflow run 37067878969) | Pass |
| Game tests on the headless server, same run: 396 in total, 7 of them new here (`CandyGameTests`) | **All 396 pass**. They also all passed on `f4235ef` (run 37066473716) |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `fdbbbae` (run 37067878969), with the screenshots in [AGRICULTURE.md](../branches/AGRICULTURE.md#the-candy-kitchen) |

The 7 new game tests (`CandyGameTests`):
1. a water bottle sets the base and leaves its bottle, and milk is then refused; sugar goes in up to four, two flavours at most and each once, and dyes mix; near the boil nothing more goes in; sneaking with an empty hand tips it all out;
2. over a fire a batch heats a degree every four ticks below the boil, and a kettle with sugar but no base doesn't; off the heat it cools but keeps the stage it reached, which comparators read;
3. syrup at soft ball pours candy corn and cream fudge; cream at thread is too runny to pour; syrup past 175°C is burnt; pouring fills one tray (two pieces a sugar, with the batch's flavours and colour), empties the kettle and earns Sweet Science;
4. a tray breaks up into its named, flavoured pieces once set, and not while still setting; hard candy with sticks in the other hand makes a lollipop a stick; rock candy takes a day to grow;
5. warm taffy pulled four times earns Pulling Power and cuts into taffy; left to go cold unpulled, it sets hard, as hard candy;
6. candy corn takes up to three layers, each its own colour; three undyed layers make plain candy corn, which stacks with any other, and a dyed one keeps its bands;
7. flavoured candy is named for its flavours and gives their effects when eaten, even on a full stomach; burnt sugar keeps no flavour; the candies count as candy, the flavours' items are tagged, and the recipes load.

Found by CI and fixed before this record:
- 26.3's `LivingEntity.drop` takes different arguments: a tray's spare pieces now go to the ground with `Block.popResource` (`d1f4304`).
- 26.3 has no `Items` field for each dye: the tests look the dyes up by ID (`1ac7690`).
- In 26.3 every item has a name and lore component by default, so two assertions that a piece had its own name were always true; they now compare with a plain stack (`f4235ef`).
- The first screenshots showed the kettles floating over their campfires with no syrup in them: the needle's turn leaked into the syrup's pose. The needle is now drawn in its own pose, and the kettle stands on four iron trivet legs (`fdbbbae`).

Not run: a two-client dedicated-server playtest, and any play by hand.

### Autumn foraging verification

Actual results (2 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/agriculture-halloween-decor-26` stacked on the candy kitchen:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares autumn foraging with Java: spreading, the fairy ring's size, chance, check interval and blessing; checks the mushrooms (the jack o'lantern mushroom's light), the basket and the foods are registered, every mushroom has its texture, words, loot and worldgen, the tags hold the mushrooms and the forage, the cooked foods have their recipes, and the two advancements exist) | Pass, 641 IDs |
| `./gradlew build` on `2e14ff6` (Build workflow run 37069783476) | Pass |
| Game tests on the headless server, same run: 400 in total, 4 of them new here (`ForagingGameTests`) | **All 400 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `c42f88d` (run 37067986302) and `2e14ff6` (run 37069783476), with the screenshots in [AGRICULTURE.md](../branches/AGRICULTURE.md#autumn-foraging) from the first |

The 4 new game tests (`ForagingGameTests`):
1. a porcini stands on grass but not on stone; spreading (as bone meal makes it) it puts out another of its kind, one at a time, until five are near, and no more; the jack o'lantern mushroom glows (light 9) and the others don't;
2. seven fly agarics round a circle are no ring and eight are; a player at the centre is blessed with Luck II for five minutes and earns Away with the Fairies, once a night; rings bless on full-moon nights only; one chanterelle sprouts a ring of its kind round it;
3. the basket refuses stone and takes berries; each of the five mushrooms picked with the basket in the other hand goes straight into it; all five earn Forager;
4. the dishes', basket's and stew's recipes and every mushroom's patch load.

Found by CI and fixed before this record:
- 26.3's `playerDestroy` takes a `ServerLevel` and a `ServerPlayer` (`d9d3786`, `a3efb90`).
- 26.3's `minecraft:dirt` block tag doesn't include grass, so a mushroom couldn't stand on a grass block: wild mushrooms now grow on a tag of their own, `jugcraft:mushroom_soil`, which their patches use too (`c42f88d`).
- `main`'s `PetroGameTests.heliostatsHeatASolarReceiver` failed twice on this branch's `c42f88d` (run 37067986302 and its re-run: "The receiver made 48 JE/t, expected 36 on tick 25"). The solar receiver counts its heliostats on its first tick, before the test's stone roof has cut off the sky light, and again only when the game time is a multiple of 100, so the test passes or fails by the game time it starts at. `main` has already fixed the test (it gives the receiver water only after its next count); this branch carries the same change (`f23f040`), and the test passes since.

Not run: a two-client dedicated-server playtest, and any play by hand.


### Bat House verification

Actual results (2 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/agriculture-halloween-decor-27` stacked on autumn foraging:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares the Bat House with Java: its room, guano cap, return range, moving-in chance, check interval, the bats' tag, and guano's area and doses; checks the house and guano are registered (guano as a fertilizer), the house has a model for each guano level, its words, loot, recipe and advancement, and phosphate takes four guano) | Pass, 643 IDs |
| `./gradlew build` on `b1a7915` (Build workflow run 37071059060) | Pass |
| Game tests on the headless server, same run: 402 in total, 2 of them new here (`BatHouseGameTests`) | **All 402 pass**. They also all passed on `2217bd2` (run 37068610115) |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `b1a7915` (run 37071059060), with the screenshots in [AGRICULTURE.md](../branches/AGRICULTURE.md#the-bat-house), and on `2217bd2` (run 37068610115) |

The 2 new game tests (`BatHouseGameTests`):
1. at dusk a house's three roosting bats fly out, tagged as its bats, and a player near earns Night Shift; at dawn the four nearest bats come in, wild ones too, two are left outside, each leaves a guano and the tray shows it; a full house reads 15 on a comparator; an empty house gains bats at dusk while mobs spawn;
2. an empty hand scoops up the house's seven guano; one guano grows the nine crops of a 3x3 patch and none beyond it; the house's and phosphate's recipes load.

Found by CI and fixed before this record:
- 26.3's `BlockPos` has no `getCenter`: the house finds the nearest bats with `Vec3.atCenterOf` (`2217bd2`).
- The first screenshots showed dark houses on a dark wall with the guano hard to see; the wall is now pale birch and a second shot looks down into the trays (`b1a7915`). The guano piles are still small at this size.

Not run: a two-client dedicated-server playtest, and any play by hand.

### Hay Golem verification

Actual results (2 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/agriculture-halloween-decor-28` stacked on the Bat House:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares the Hay Golem with Java: health, speed, post radius, search height, tending, work, give-up and idle times, pouch, carry, wheat's healing, fire, hay bales, reach and leading range; checks it is registered with its size, attributes and building callback, scarecrows count golems, it is named, drops wheat, has its head tag, texture and advancement, and its model's boxes sit where its texture paints them) | Pass, 643 IDs (the golem is an entity, not an item or block) |
| `./gradlew build` on `709a0c2` (Build workflow run 37071260396) | Pass |
| Game tests on the headless server, same run: 408 in total, 6 of them new here (`HayGolemGameTests`) | **All 408 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `709a0c2` (run 37071260396), with the screenshots in [AGRICULTURE.md](../branches/AGRICULTURE.md#the-hay-golem) |

The 6 new game tests (`HayGolemGameTests`):
1. carved pumpkins and jack o'lanterns are heads, a plain pumpkin and an uncut hand-carved one are not; one arm short, or with stone under an arm, the hay stays hay; a carved head on a full T makes one golem where the legs stood, facing its builder, the hay gone and the head used, and the builder earns Man of Straw;
2. a golem in a carved pumpkin guards seven blocks off (eight at most), twelve in a jack o'lantern, four headless (and not six); a crow won't raid ripe wheat a golem guards;
3. four ripe carrots harvested are each replanted from a carrot, the rest in its pouch; unripe carrots are left; unloading puts the carrots in the chest under its post, or with no chest sets them on the ground; the pouch holds nine stacks and no more;
4. with its own AI a golem walks five blocks to ripe carrots and harvests and replants them;
5. fire hurts it double; a wheat heals it four; shears take it apart into four hay bales, its jack o'lantern and its carrots;
6. killed by a player it drops two to five wheat and its head; its loot table, head tag and advancement load.

Found by CI and fixed before this record:
- 26.3's `Entity.invulnerableTime` is private, so the damage test couldn't reset it between hits; the test now burns the golem first and heals it after (`709a0c2`). The golem's own code compiled first time.

Not run: a two-client dedicated-server playtest, and any play by hand. The golem's walking and harvesting were seen only in the game test, on a flat stone floor.

## World and event applicability
- A Candy Kettle works anywhere there is heat under it, in every dimension, all year. Nothing is seasonal.
- Wild mushrooms generate only in newly generated Overworld chunks of their biomes; existing chunks don't get them, but one mushroom brought in spreads. They can be planted and spread in any dimension with soil and shade. Fairy rings bless only in the Overworld, on full-moon nights (by the Overworld's moon).
- A Bat House keeps the Overworld's dusk and dawn wherever it is hung.
- A Hay Golem works in any dimension.

## Rollout and open questions
- Candy can't be poured by hoppers or dispensers; trays are filled and broken up by hand.
- A tray of candy doesn't stack, so a candy maker carries one tray a batch.
- The stages' temperatures are real candy makers' (roughly); the heating rates are compressed so a batch takes under a minute.
- Every effect is open to balance review in play, Absorption and Resistance in particular.
- The fairy-ring blessing isn't saved: a restart during a full-moon night lets a player be blessed again that night.
- Mushrooms aren't food raw. The fly agaric and the jack o'lantern mushroom aren't cooked into anything yet; a later addition could use them (dyes, a potion).
- How often patches generate is a first guess, open to review once worlds are explored.
- Bats out at night may despawn as vanilla bats do far from players; the house fills again by moving in at dusk.
- Hoppers don't take guano from a Bat House; it is scooped by hand.
- A Hay Golem harvests only single-block crops (`CropBlock`s); tall crops, gourds, berries and the giant pumpkin are left to the farmer, as crows leave them.
- Following wheat moves its post: a farmer walking past with wheat in hand will lead it off. Holding something else keeps it where it is.
- A golem doesn't open doors or cross fences; build its field where it can walk.
