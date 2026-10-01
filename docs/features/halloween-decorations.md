# Halloween decorations

Status: implemented in source, not yet played. The Build workflow compiles it, and CI's game tests are recorded below.
Proposal issue: none; requested directly by the owner on 1 October 2026 ("Come up with 30 halloween decorations and props and blocks that would be good ideas to make it more seasonal", then "after that lets do the 30 I just said"). They ship five at a time, one pull request per batch, like the earlier Halloween batches. This record covers the first ten. Batch 1: the Jack-o'-Lantern String Lights, the Candy Bowl, the Coffin, the Haunted Portrait and the Fog Machine. Batch 2: the Luminaria, Floating Candles, the Skeleton Hand Sconce, Soul-Flame Carvings and Bat Bunting. The other 20 come in later batches and will be added here.
Owner: @jimbozoomer-byte
Target milestone and tier: Milestone 3 (first homestead) for the bowl, coffin and portrait (Discovery tier: terracotta, planks, wool, a chest, a painting); the string lights' hooks need iron; the Fog Machine needs the electric network (copper cable, a generator). Batch 2 is all Discovery tier (paper, sand, candles, feathers, torches, bones, string, dye); soul-flame carvings need a soul torch (soul sand or soil, from the Nether or a soul sand valley).
Primary specialty and supported player role: building and play; supports groups (a candy bowl at your door for other players), builders (graveyards, haunted houses, yards) and engineers (powered lights and effects).

## Player experience
### Jack-o'-Lantern String Lights
1. **String Light Hooks** (an iron nugget over an iron ingot, four at a time) fix to a floor, wall or ceiling like a lever.
2. **Jack-o'-Lantern String Lights** (two string, glowstone dust and orange dye): use the strand on one hook, then on another up to 16 blocks away. A strand of tiny pumpkin bulbs sags between them. Each hook holds one strand of its own and any number may run to it, so hooks chain round a yard. Sneak-use a hook with an empty hand to take its strand down; breaking a hook drops its strand, and a strand whose far hook is gone comes down within five seconds.
3. **Lighting them.** A hook lights (light 10) while it has a redstone signal, or while the electric network feeds it a trickle (1 JE a tick; cables connect to hooks). A strand glows while either of its hooks is lit, so a lever, daylight sensor or a solar panel can switch the whole display.

### The Candy Bowl
4. An orange terracotta bowl with a jack-o'-lantern grin, for trick-or-treating at players' homes. Whoever places it owns it.
5. **Fill it** by using candy or cookies on it (up to 64; anyone may add). It looks emptier or fuller as the treats come and go.
6. **Take a treat** with an empty hand: each visitor gets one a night (the same nights as villager trick-or-treating), its owner one whenever they like. Sneak-use to see how many are left. It works all year. Breaking it spills the treats.

### The Coffin
7. Two blocks long, dark planks lined in red velvet, a brass cross on the lid. **Use it** to lift the lid on a 27-slot chest; the lid stays up on both halves while anyone has it open, creaking open and thudding shut. Hoppers and pipes reach it like a chest.
8. **Sneak-use it** with an empty hand to lie down in it like a bed: it sets your spawn and lets you sleep through the night. Where a bed would explode (the Nether, the End) the coffin only says nothing rests easy here.

### The Haunted Portrait
9. A gilt-framed painting (a painting, a gold nugget and a spider eye) that hangs on the side of a block and falls if that block goes. Four sitters: **the Lady in Black, the Old Captain, the Black Cat and the Owl**; sneak-use it to change.
10. **Its eyes follow you.** Each player sees the pupils turn toward their own camera. At night they glow red.

### The Fog Machine
11. A riveted dieselpunk cabinet with a brass fluid tank, a gauge, a lamp and a grille nozzle (iron, iron bars, copper cable and a bucket). Cables connect to it.
12. **Switch it on** with an empty hand, or with a redstone signal. While it has power (16 JE a tick) it runs, its lamp glows, and low fog rolls out of the nozzle and lies on the ground around it. Sneak-use it to set the fog's radius: 4, 8, 12 or 16 blocks.

### Batch 2: the Luminaria
13. A **Luminaria** (two paper, sand and a candle) is a paper bag weighted with sand round a candle, a jack-o'-lantern face cut in each side. It stands on top of a block and falls if that block goes.
14. **Light it** with flint and steel or a fire charge, like a candle (light 10); the paper glows and the candle flickers inside. An empty hand snuffs it.
15. **Dye it:** any dye used on it colours the paper (16 colours, one dye each). Broken, it keeps its colour, and its item shows it.

### Batch 2: Floating Candles
16. A **Floating Candle** (a candle and a feather) hangs in the air where it is placed and needs nothing under it. Use more on it to put up to four together; each candle bobs gently, out of step with the others.
17. Light them with flint and steel or a fire charge (3 light a candle, like vanilla candles: 12 for four); an empty hand snuffs them all. Nothing walks into them. Broken, they drop one candle each.

### Batch 2: the Skeleton Hand Sconce
18. A **Skeleton Hand Sconce** (a torch and two bones): a bony forearm reaching out of an iron wall plate, its hand clutching a torch. It hangs on the side of a block like a wall torch and falls if the block goes.
19. It is placed burning (light 14, with a torch's flame and smoke). An empty hand snuffs it; flint and steel or a fire charge lights it again.

### Batch 2: Soul-Flame Carvings
20. A **soul torch** lights a hand-carved pumpkin (any of the four kinds) or a carved giant pumpkin just as a torch does, but with a cold blue flame: the carving glows ice-blue, at most light 10 (a soul torch's own) where a torch gives the carving's whole glow. Taking it out with an empty hand gives the soul torch back.
21. A soul-lit hand-carved pumpkin keeps its soul flame when broken (the hand-carved pumpkin's item shows a blue face; the heirloom kinds look like their pumpkin, as before); a giant pumpkin keeps it through saves.

### Batch 2: Bat Bunting
22. **Bat Bunting** (string, two paper, orange dye and black dye) is strung between String Light Hooks exactly like the string lights (the same rules, 16 blocks, one strand from each hook): a twine cord hung with orange and black pennants and little paper bats. A hook can hold bunting while lights run to it, so the two mix along a yard. It gives no light; taking it down or breaking its hook drops the bunting.

## Connections
- Existing input producer: the electric network and its cables (hooks, fog machine), redstone, the trick-or-treat night count, Halloween candy and cookies (the bowl), vanilla terracotta, planks, red wool, chests, paintings, gold nuggets, spider eyes, iron, glowstone and dye. Batch 2: paper, sand, candles, feathers, torches, soul torches, bones, string and dye; flint and steel or fire charges to light; the hand-carved and giant pumpkins of pumpkin carving and the Halloween harvest; the String Light Hooks of batch 1.
- Existing output consumer: decoration, light (hooks, luminarias, floating candles, the sconce, soul-lit carvings), storage (the coffin), a respawn point (the coffin), a way to share treats with other players.
- Technology connection: the hooks and the Fog Machine use the shared energy interface (`EnergyStorage.SIDED`), so any Jugcraft generator powers them, and the Fog Machine's recipe needs copper cable.
- Magic connection: none yet; the portrait is a natural hook for later haunting.
- Reachable entry path: everything is crafted from vanilla materials, plus copper cable (from the tin and bronze tier) for the Fog Machine.
- Required vs optional: all optional decoration; nothing in progression needs them.
- How this stays useful without other branches: builders get lights, storage and props; groups get a candy bowl; engineers get something to power.

## Balance and automation
- Hooks: 1 JE a tick each while powered by the network, 200 JE buffer, up to 20 JE a tick in. A redstone-lit hook uses nothing.
- Fog Machine: 16 JE a tick while running, 4,000 JE buffer, up to 64 JE a tick in. A solar panel (8 JE a tick) can't run it alone; a coal generator can run two.
- Candy Bowl: holds 64 treats; one per visitor a night, so a bowl can't be drained by one visitor; it remembers the last 256 visitors. It only moves treats that players put in; it never makes any.
- Coffin: 27 slots like a chest. Sleeping follows vanilla's bed rules.
- Batch 2 uses no energy. A luminaria costs two paper, sand and a candle; a floating candle a candle and a feather; the sconce a torch and two bones; bunting string, two paper and two dyes. Dyeing a luminaria uses one dye; lighting uses flint and steel (one durability) or a fire charge.
- Light: luminaria 10, floating candles 3 a candle (at most 12), the sconce 14 (a torch's), a soul-lit carving at most 10.
- Breaking gives back exactly what was placed: one luminaria (with its colour), one floating candle per candle, one sconce, one bunting. None of them gives back its ingredients.
- No conversion loops; nothing here makes items or energy.

## Multiplayer and persistence
- **Server authority.**
  - Every use goes through vanilla's block and item use paths, which check reach and spawn protection.
  - Stringing (both hooks, 16 blocks, same dimension, not already strung), filling and taking treats, opening the coffin, lying down, changing portraits and switching the fog machine are all decided on the server.
  - Who owns a bowl and who has had a treat tonight are recorded on the server, never claimed by clients.
- Batch 2: lighting, snuffing, dyeing, adding candles, putting a torch in a carving and stringing bunting all go through vanilla's block and item use paths and are decided on the server.
- **Client only.** The strands (and bunting), the portrait's pupils, the floating candles' bob and the fog are drawn by each client; the bob is worked out from the game time and the block's position, so every client sees the same. The pupils use only that client's camera, and the fog is only particles: it changes no block, hides nothing from the server and lets nobody through walls.
- **Saved state.**
  - A hook keeps its strand and energy; a bowl its treats, owner and visitors (with the night); the coffin its slots (in its head half); the fog machine its energy.
  - The fog machine's on/off and radius and the portrait's sitter are block states.
  - Batch 2: a luminaria's colour and flame, the floating candles' count and flame, the sconce's flame and a hand-carved pumpkin's soul flame are block states (`soul` is new on hand-carved pumpkins; worlds saved before it load with `soul=false`, a plain candle). A giant pumpkin's soul flame is saved as `soul` in its master block entity (missing means a plain candle). A hook saves which strand it holds as `strand` (`lights` or `bunting`; missing means lights).
- **Bounded work.**
  - A hook looks at redstone and power every 10 ticks, and checks its strand's far hook every 100.
  - The fog machine reads redstone every 10 ticks and on neighbour changes.
  - Fog: at most 6 puffs a tick per machine and 24 a tick for all machines together on one client, only within 48 blocks of a player, each living 4 to 6 seconds.
  - A strand draws at most 3 segments per block of length plus its bulbs.
  - Batch 2: floating candles redraw only while in view and run no server ticks; luminarias, the sconce and soul-lit carvings tick only on clients for their particles, like vanilla torches and candles.
- New IDs only:
  - blocks with items: `string_light_hook`, `candy_bowl`, `coffin`, `haunted_portrait`, `fog_machine`; batch 2: `luminaria`, `floating_candle`, `skeleton_hand_sconce`;
  - items: `jack_o_lantern_string_lights`; batch 2: `bat_bunting`;
  - block entities: `string_light_hook`, `candy_bowl`, `coffin`, `haunted_portrait`, `fog_machine`; batch 2: `floating_candle`;
  - particle type: `fog`.
  - Batch 2 adds the `soul` block state property to the four hand-carved pumpkins (default `false`).
- **The `agriculture` switch** turns off their recipes; placed blocks stay and work. Soul torches still light carvings with it off (it is the carvings' own use, like the torch).

## Dependencies and assets
No new dependencies. Every texture is drawn by code (`tools/decor_textures.py`, batch 2 `tools/decor2_textures.py`, and the soul-lit carving icon in `tools/carving_textures.py`), and the models, loot and tags come from `tools/decor_data.py` and `tools/decor2_data.py`. The strands and bunting and the pupils are drawn by the client (`StringLightsRenderer`, `HauntedPortraitRenderer`), the floating candles by `FloatingCandleRenderer`, the soul-lit carvings by `CarvingTextures` (blue colours), and the fog by `FogParticle`.

## Verification
Actual results (1 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions):

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares the hooks', bowl's, coffin's and fog machine's numbers with Java, the portraits' eyes with Java and with the painted textures, every block state with a model, and every result a player can be told with its text) | Pass, 445 IDs |
| `./gradlew build` on `853ebc9` (the code of this pull request; later commits only change docs and screenshots and remove a temporary probe workflow) | Pass |
| Game tests on the headless server, same commit: 209 in total, 8 of them new here (`DecorGameTests`) | **All 209 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `853ebc9`; no model, texture or particle errors in the log |

The 8 new game tests (`DecorGameTests`):
1. using the strand on two hooks strings them and uses one strand; the same hook twice, more than 16 blocks, or a pair already strung (either way round) are refused; when the first hook already holds a strand the second takes it, so hooks chain; sneaking with an empty hand takes a strand down; breaking a hook drops its strand;
2. a strand whose far hook is gone comes down within the next check and drops;
3. a hook is dark unpowered, lights (light 10) on electricity using 1 JE a tick, goes dark without it, and lights at once from a redstone signal using none; a strand glows if either end is lit; the energy interface finds the hook's buffer;
4. the bowl belongs to whoever placed it; dirt won't go in; treats fill it (up to 64) and its look follows; a visitor takes one treat a night and no second; its owner takes any time; the visitor ends up with exactly one treat; treats, owner and tonight's visitors survive a save and load; the next night the visitor may take another; breaking it spills every treat and drops the bowl;
5. placing a coffin puts the head beside the foot; using it opens a 27-slot chest and lifts the lid on both halves, closing lowers it; sneak-using it sets the player's spawn; breaking it spills what is inside and drops one coffin;
6. a portrait won't stand on the floor, hangs on the side of a block facing out, cycles through four sitters and back, keeps every eye inside its frame, and falls (dropping) when its wall is broken;
7. switched on without power the fog machine doesn't run; with power it runs (light 6) using 16 JE a tick; switched off it stops; a redstone signal switches it on; it stops when its energy runs out; sneak-use widens the fog by 4 blocks and comes round again; the energy interface finds its buffer;
8. the six recipes, five loot tables, tool tags and the fog particle load.

The client game test (`DecorClientGameTests`) builds string lights on three fence posts, three Candy Bowls (empty, half full, heaped), an open and a closed Coffin, the four portraits on a wall and a running Fog Machine, and photographs them by day, up close and at midnight: the strands glow at night, the open coffin shows its velvet, the pupils are dark by day and red at night and lean toward the camera from the side, and fog lies on the ground round the machine.

Found by CI and fixed before this record:
- 26.3 blocks no longer have codecs, `PushReaction.DESTROY` is now `POPPED`, and `PoseStack` turns with `rotateDegrees` (compile errors).
- Vanilla's bed rule makes beds explode in the Nether and the End; the coffin only refuses there, so 27 slots of belongings are never blown up.

**Not run:**
- a person playing it in a client;
- a dedicated server with two players (one filling a bowl, another taking a treat; two players opening one coffin);
- sleeping a whole night in a coffin, and respawning at one (the test checks that the spawn is set);
- the coffin in the Nether or End (the refusal is one override, not exercised);
- string lights powered through real cables from a generator, and the fog machine on a real network (the tests fill their buffers directly and check the energy interface finds them).

### Batch 2 verification

Pending: the CI results for batch 2 are added here when its build has run.

## World and event applicability
- Decorations work all year, anywhere. The candy bowl uses the trick-or-treat night count but is not limited to the Halloween event.
- The coffin respects each dimension's bed rules.
- Batch 2's decorations work anywhere; nothing about them depends on the Halloween event.
- Fog is cosmetic and capped per client.

## Rollout and open questions
- Hooks hold one strand each; very long chains need a hook every 16 blocks.
- Strands give no block light of their own (only the hooks do); real light along the strand would need light blocks and is left out.
- A soul-lit giant pumpkin hollowed into a Pumpkin Barge keeps its torch but glows with ordinary candlelight (the barge's saved data has no soul flame).
- Floating candles can't be waterlogged: placed in water, they replace it. Unlike vanilla candles, a burning arrow doesn't light them and a water splash doesn't snuff them.
- A dyed luminaria's item stacks only with bags of the same colour.
- The other 20 decorations follow in later batches.
