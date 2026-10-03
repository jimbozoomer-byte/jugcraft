# Even More Fall Additions

Status: hex brews (addition 21) are implemented in source and pass CI's game tests, not yet played by hand. Additions 22 to 30 are planned and not yet built. The Build workflow compiles what exists; CI's game tests and client screenshots are recorded below.
Proposal issue: none. The owner asked for these directly on 3 October 2026 ("Lets do even more fun fall and halloween stuff 10 well thought out good additions"). They follow the [more fall additions](more-fall-additions.md) and are numbered on from them. Each one is its own pull request, stacked on the one before, after the graveyard pack:
21. hex brews: the Bubbling Cauldron's brews, bubbling over a fire, take a hex ingredient and become the Shrinking Draught, the Giant's Draught or Flying Ointment;
22. the flying broomstick (planned);
23. full-moon werewolves, wolfsbane and silver (planned);
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

Primary specialty and supported player role: witchcraft and exploration. A witch brews hexes for others:
- **Shrinking:** gets a player into one-block gaps, under low ceilings and into cramped caves, and makes them a smaller target.
- **Giant:** gives a block more reach and half a block more step, for building and climbing, but makes the player a bigger target.
- **Flying ointment:** gives a safe fall from a cliff or a tower.

Draughts stack to 16 and keep, so a witch can trade them.

None of this depends on the Halloween event: a cauldron brews hexes all year.

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
- **Cost on the server:** one look at each online player per server tick (a duration and effect check; a collision check only when a shrinking is about to end). No block entity, no block ticks. The glyphs and bubbles are client particles.

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
- **Disable behaviour:** the hexes are part of the agriculture feature's blocks and stay registered.

## Dependencies and assets
- No new dependencies.
- Textures are drawn by code in `tools/decor4_textures.py`: each hex brew's liquid, the three draughts and the two effect icons (a mouse-sized figure and a towering one).
- The cauldron's models for each hex and level, the item models, names, tooltips, the message, the tags and the advancements come from `tools/decor4_data.py`. The numbers come from `HEX` in `tools/agriculture.py`.
- `tools/check_mod_data.py` compares them with Java.
- Sounds and particles are vanilla's: a brewing stand's brew pitched low, a bottle filling, witch's sparkles and enchanting glyphs.

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

## World and event applicability
- A cauldron brews hexes in any dimension, all year. Nothing is seasonal.
- The hex ingredients come from the Overworld: brown mushrooms, beans, and phantoms.

## Rollout and open questions
- A hex brew can't be drawn by hoppers or dispensers. Bottles are filled by hand.
- Breaking a cauldron loses its brew, as before.
- How big a giant is (×1.6) and how long each effect lasts are open to balance review once played. A giant is easier to hit, which is the trade for its reach.
- Mobs aren't affected: a splash or lingering hex is not made.
- A shrunk player's grace is 5 seconds at a time. A player who logs out shrunk under a low ceiling stays shrunk until there is room.
