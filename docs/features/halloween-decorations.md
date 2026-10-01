# Halloween decorations

Status: implemented in source, not yet played. The Build workflow compiles it, and CI's game tests are recorded below.
Proposal issue: none; requested directly by the owner on 1 October 2026 ("Come up with 30 halloween decorations and props and blocks that would be good ideas to make it more seasonal", then "after that lets do the 30 I just said"). They ship five at a time, one pull request per batch, like the earlier Halloween batches. This record covers the first five: the Jack-o'-Lantern String Lights, the Candy Bowl, the Coffin, the Haunted Portrait and the Fog Machine. The other 25 come in later batches and will be added here.
Owner: @jimbozoomer-byte
Target milestone and tier: Milestone 3 (first homestead) for the bowl, coffin and portrait (Discovery tier: terracotta, planks, wool, a chest, a painting); the string lights' hooks need iron; the Fog Machine needs the electric network (copper cable, a generator).
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

## Connections
- Existing input producer: the electric network and its cables (hooks, fog machine), redstone, the trick-or-treat night count, Halloween candy and cookies (the bowl), vanilla terracotta, planks, red wool, chests, paintings, gold nuggets, spider eyes, iron, glowstone and dye.
- Existing output consumer: decoration, light (hooks), storage (the coffin), a respawn point (the coffin), a way to share treats with other players.
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
- No conversion loops; nothing here makes items or energy.

## Multiplayer and persistence
- **Server authority.**
  - Every use goes through vanilla's block and item use paths, which check reach and spawn protection.
  - Stringing (both hooks, 16 blocks, same dimension, not already strung), filling and taking treats, opening the coffin, lying down, changing portraits and switching the fog machine are all decided on the server.
  - Who owns a bowl and who has had a treat tonight are recorded on the server, never claimed by clients.
- **Client only.** The strands, the portrait's pupils and the fog are drawn by each client. The pupils use only that client's camera, and the fog is only particles: it changes no block, hides nothing from the server and lets nobody through walls.
- **Saved state.**
  - A hook keeps its strand and energy; a bowl its treats, owner and visitors (with the night); the coffin its slots (in its head half); the fog machine its energy.
  - The fog machine's on/off and radius and the portrait's sitter are block states.
- **Bounded work.**
  - A hook looks at redstone and power every 10 ticks, and checks its strand's far hook every 100.
  - The fog machine reads redstone every 10 ticks and on neighbour changes.
  - Fog: at most 6 puffs a tick per machine and 24 a tick for all machines together on one client, only within 48 blocks of a player, each living 4 to 6 seconds.
  - A strand draws at most 3 segments per block of length plus its bulbs.
- New IDs only:
  - blocks with items: `string_light_hook`, `candy_bowl`, `coffin`, `haunted_portrait`, `fog_machine`;
  - item: `jack_o_lantern_string_lights`;
  - block entities of the same names (but the strand);
  - particle type: `fog`.
- **The `agriculture` switch** turns off their recipes; placed blocks stay and work.

## Dependencies and assets
No new dependencies. Every texture is drawn by code (`tools/decor_textures.py`), and the models, loot and tags come from `tools/decor_data.py`. The strand and pupils are drawn by the client (`StringLightsRenderer`, `HauntedPortraitRenderer`), and the fog by `FogParticle`.

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

## World and event applicability
- Decorations work all year, anywhere. The candy bowl uses the trick-or-treat night count but is not limited to the Halloween event.
- The coffin respects each dimension's bed rules.
- Fog is cosmetic and capped per client.

## Rollout and open questions
- Hooks hold one strand each; very long chains need a hook every 16 blocks.
- Strands give no block light of their own (only the hooks do); real light along the strand would need light blocks and is left out.
- The other 25 decorations follow in later batches.
