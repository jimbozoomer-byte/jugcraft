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
VERIFICATION_PENDING

## World and event applicability
- Decorations work all year, anywhere. The candy bowl uses the trick-or-treat night count but is not limited to the Halloween event.
- The coffin respects each dimension's bed rules.
- Fog is cosmetic and capped per client.

## Rollout and open questions
- Hooks hold one strand each; very long chains need a hook every 16 blocks.
- Strands give no block light of their own (only the hooks do); real light along the strand would need light blocks and is left out.
- The other 25 decorations follow in later batches.
