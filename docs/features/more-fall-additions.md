# More Fall Additions

Status: the candy kitchen (addition 11) is implemented in source, not yet played. The Build workflow compiles it, and CI's game tests are recorded below.
Proposal issue: none; requested directly by the owner on 2 October 2026 ("lets do another 10 detailed halloween and fall themed additions", then "start them now stacked on #33"). They follow the ten [fall additions](fall-additions.md), numbered on from them, one per pull request, each stacked on the one before:
11. the candy kitchen: the Candy Kettle, its thermometer and the candy stages, the Candy Tray, and ten candies;
12. autumn foraging (planned);
13. the Bat House and guano (planned);
14. the Hay Golem (planned);
15. knitting and sweaters (planned);
16. pie baking (planned);
17. the Spirit Board (planned);
18. wild turkeys (planned);
19. the theremin (planned);
20. the Día de Muertos ofrenda (planned).

Owner: @jimbozoomer-byte
Target milestone and tier: the candy kitchen is Discovery tier: copper ingots and a glass pane (the kettle), iron nuggets (the tray), sugar, a water bottle or a milk bucket, a campfire, dyes, sticks (for lollipops), and the flavours, all early: cocoa beans, sweet berries, glow berries, a honey bottle, and the Festival Crops' cranberries, the cider mill's mulling spices and roasted chestnuts.
Primary specialty and supported player role: cooking and crafting. A candy maker turns sugar cane into the treats that fill Candy Bowls and Candy Bags, and flavoured candy is a pocketful of short effects (speed, haste, night vision, resistance, fire resistance, absorption, regeneration) to hand round before a dig or a fight. Candy is easy to trade: each piece says its flavours in its name.

Nothing here depends on the Halloween event: the kettle boils candy all year.

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

## Balance and automation
- **Candy kitchen:**
  - Costs: a kettle of seven copper ingots and a glass pane; a tray of five iron nuggets (reused). A batch is a water bottle or a milk bucket (both containers come back), one to four sugar, and up to two flavour items and any dyes, used once a batch.
  - Units: degrees Celsius; ticks (20 a second); pieces (two a sugar, eight at most a batch, 24 at most on a tray of three candy corn layers).
  - Heating from room temperature (20°C): 320 ticks to the boil, 120 more to 110°C, then 30 ticks to each 5°C stage (6 ticks a degree): about 22 seconds to thread, 33 to hard crack, 41 to burnt. Cooling: 8 ticks a degree.
  - Setting: 100 ticks; rock candy 24,000 ticks; taffy pulled 4 times, 20 ticks each, within 600 ticks.
  - Effects are short (4 to 30 seconds a piece) and the same as vanilla potions'; candy gives no effect stronger than level I.
  - Nothing is made from nothing: candy comes only from sugar, and candy makes nothing else (caramel goes into caramel apples, as before). No loop.

## Multiplayer and persistence
- **Candy kitchen, server authority:** filling, reading, tipping out and pouring all go through vanilla's block use path (reach, spawn protection, adventure mode) and are decided on the server, which checks the base, the sugar and flavour limits, and the temperature. Pulling and breaking up a tray go through vanilla's item use; the server checks the tray's own record of when it was poured and how often it has been pulled, by its own game time.
- **Candy kitchen, what clients get:** the kettle's batch and temperature, to draw the syrup and the needle; the tray's batch, to draw it. Clients decide nothing.
- **Candy kitchen, concurrent use:** two players at one kettle share its batch; whoever pours first gets it, and the second finds it empty.
- **Candy kitchen, persistence:** the kettle saves its base, sugar, flavours, dye sums, temperature and the hottest it has reached; a tray keeps its batch (a data component), so trays of candy keep across a restart and in chests, and rock candy keeps growing by game time.
- **Candy kitchen, bounded work:** a kettle does nothing unless it is warmer than the room or heating a batch; a degree changes at most every 4 ticks, and only then is it sent to clients.
- **Candy kitchen, IDs:** blocks/items `candy_kettle`, `candy_tray`, `rock_candy`, `salt_water_taffy`, `hard_candy`, `lollipop`, `fudge`, `cream_caramel`, `toffee`, `burnt_sugar`; block entity `candy_kettle`; data component `jugcraft:candy_batch`; item tags `jugcraft:candy_flavours/*`; advancements `candy_maker` and `taffy_puller`; the kettle's and tray's recipes. All new. Candy corn's item model now takes its band colours from the vanilla `custom_model_data` component (plain candy corn keeps its white, orange and yellow).
- **Candy kitchen, disable behaviour:** with the agriculture feature off, the kettle's and tray's recipes don't load; kettles, trays and candy already made still work, and everything stays registered.

## Dependencies and assets
Candy kitchen:
- No new dependencies. Textures are drawn by code in `tools/candy_textures.py`: the kettle's polished copper and dark inside, its thermometer dial (the stages painted round it in their colours, a tick where each starts), the syrup's surface and the needle for the renderer, the tray empty and the candy on it, and each candy, drawn pale where it takes its colour, with its stick or wrapper in a layer of its own; candy corn in three bands and an outline.
- The models, blockstates, item models (each candy tinted by the vanilla `dyed_color` component; candy corn's bands by `custom_model_data`), names, tooltips, messages, loot and tags come from `tools/candy_data.py`; the numbers from `CANDY` in `tools/agriculture.py`.
- The client's `CandyKettleRenderer` draws the batch at the height of its sugar, browning past hard crack and black once burnt, and the needle on the dial.
- Sounds are vanilla's (a note block's bell at each stage, fire going out when it burns, sand, a bottle, a bucket, dye, honey, amethyst breaking for hard candy).

## Verification
### Candy kitchen verification

Pending CI (recorded when it runs).

## World and event applicability
- A Candy Kettle works anywhere there is heat under it, in every dimension, all year. Nothing is seasonal.

## Rollout and open questions
- Candy can't be poured by hoppers or dispensers; trays are filled and broken up by hand.
- A tray of candy doesn't stack, so a candy maker carries one tray a batch.
- The stages' temperatures are real candy makers' (roughly); the heating rates are compressed so a batch takes under a minute.
- Every effect is open to balance review in play, Absorption and Resistance in particular.
