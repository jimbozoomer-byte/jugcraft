# More Halloween

Status: implemented in source for batch 7, not yet played. The Build workflow compiles it, and CI's game tests pass (recorded below). Batches 8 to 14 are planned and follow one pull request at a time.
Proposal issue: none; requested directly by the owner on 2 October 2026 ("I want to make more halloween content more decorations and fun festive content", then "Lets do those 45 by each category starting with Haunted House inside then Mad Scientist and Mosnters then Yard and Porch then Lighting and Glow then Party Games then Night Events then Treats then Costumes"). The 45 ideas ship one category per pull request, each stacked on the one before:
- batch 7, the haunted house inside: the Haunted Chandelier, the Phantom Pipe Organ, the Suit of Armor, the Dust Sheet, the Spirit Mirror, Tattered Curtains and the Creepy Doll;
- batch 8, the mad scientist and monsters (planned);
- batch 9, the yard and porch (planned);
- batch 10, lighting and glow (planned);
- batch 11, party games (planned);
- batch 12, night events (planned);
- batch 13, treats (planned);
- batch 14, costumes (planned).

Owner: @jimbozoomer-byte
Target milestone and tier: batch 7 is Discovery tier (iron, candles, a note block, bones, planks, an armor stand, white carpet, glass, gold nuggets, soul sand, string, clay and wool).
Primary specialty and supported player role: building and play; builders (haunted houses), groups (a shared haunted house to explore), and anyone who likes a prop that does something when you are not looking.

Event-only activities in later batches follow the rule the earlier Halloween work set: they run only in the Halloween window, and anything crafted or placed stays all year. Nothing in batch 7 depends on the event.

## Player experience
### Batch 7: the Haunted Chandelier
1. A wrought-iron ring of eight candles that hangs under a block or from a chain (it falls if that goes). Flint and steel or a fire charge lights every candle (3 light for every two that burn: 12 with all eight); an empty hand snuffs them and they stay out.
2. It sways gently on its chain, and each burning candle has a flickering flame.
3. **At night a draft nobody can find** now and then blows every candle out at once with a hiss and a wisp of smoke. Then they relight by themselves, one every three quarters of a second, until all eight burn again.

### Batch 7: the Phantom Pipe Organ
4. A carved organ three blocks wide and two tall: a keyboard over a pedalboard, a music desk with a sheet of old music, carved towers and thirteen pewter pipes. It is one prop, like a large machine: place it with its keyboard toward you, and breaking any block of it picks up the whole organ.
5. **Use any block of it, or give it a redstone signal,** and it plays the opening of Bach's Toccata and Fugue in D minor: the famous falling phrase three times, each an octave lower, then the rolled chord over a low D. The keys go down by themselves as it plays. Use it again to stop it.
6. **At night it sometimes starts playing on its own.**

### Batch 7: the Suit of Armor
7. A full suit of plate on a wooden stand, two blocks tall, holding a halberd. Its helmet slowly turns to follow the nearest player within ten blocks (up to 75 degrees either way), and looks ahead again when nobody is near. At night a red glow shows in its visor. Use it and it clanks.

### Batch 7: the Dust Sheet
8. An old white sheet (three white carpets). Use it on a chair, a stair or slab, a chest, a barrel, a bookshelf, a crafting table or another block of the `jugcraft:dust_sheet_coverable` tag, and it drapes over it, sagging a little in the middle and falling to a ragged hem on the floor. It goes over a rocking chair rather than sitting you down, and over a chest rather than opening it.
9. The sheet keeps what is under it: its shape (it still blocks the way and you can still stand on it), how long it takes to break, and a chest's contents.
10. **Use it with an empty hand to pull the sheet off:** the furniture is back as it was, contents and all, and the sheet goes back to you. Breaking a covered block drops the sheet and whatever the block would drop, and spills a chest's contents, as breaking the chest would.
11. **At night one sheet in three seems to breathe,** rising and falling as if something under it were asleep.

### Batch 7: the Spirit Mirror
12. An old silvered mirror in an arched gilt frame, hung on a wall. By day it is only a mirror.
13. **At night a pale face shows in the glass** now and then: for four seconds in every thirty, fading in and out, to anyone standing in front of it within eight blocks. Each mirror keeps its own time.
14. Look into it (use it): by day "Just your reflection. Probably."; at night "For a moment, someone stands behind you in the glass."

### Batch 7: Tattered Curtains
15. Ragged, moth-eaten cheesecloth on an iron rod, for windows and doorways (three from three iron nuggets and six string). They hang at the back of their block, before a wall or window or under a lintel. Placed under one another they make one drape, up to eight blocks long, with the rod at the top and a ragged hem at the bottom.
16. **Use any of them to draw the whole drape open or shut.**
17. Closed, they sway in a draft that nobody can find, more toward the hem, and twice as much at night. Anyone walks through them.

### Batch 7: the Creepy Doll
18. A porcelain doll in a faded velvet dress, with ringlets, a red bow, big black eyes and a crack across its face. It sits on a floor, a shelf, a slab or a fence post.
19. **Its head never moves while you watch it.** Look away and look back, and it has turned: usually toward you, now and then (one time in three) far off to one side. Each player sees it turn for themselves.
20. Wind it (use it) and its music box plays a note.

## Connections
- Existing input producer: iron, candles, flint and steel or fire charges (the chandelier); iron, bone, a note block and planks, and redstone (the organ); iron and an armor stand (the suit); white carpet (the sheet); a glass pane, gold nuggets and soul sand (the mirror); iron nuggets and string (the curtains); clay, wool and string (the doll). The sheet covers the earlier batches' Rocking Chair, Hay Bale Seat, Crystal Ball and Grimoire Stand, this batch's doll and mirror, and vanilla furniture.
- Existing output consumer: decoration, light (the chandelier), music (the organ), storage kept under a sheet.
- Technology connection: none needed; the organ answers redstone.
- Magic connection: none yet.
- Reachable entry path: everything is crafted from vanilla materials an early player has.
- Required vs optional: all optional decoration; nothing in progression needs them.
- How this stays useful without other branches: builders get a furnished haunted house that does things by itself.

## Balance and automation
- No energy. A chandelier costs two iron nuggets, an iron ingot and three candles; an organ three iron ingots, two bones, a note block and three planks; a suit of armor four iron ingots and an armor stand; a sheet three white carpets (two wool); a mirror seven gold nuggets, a glass pane and a soul sand; three curtains three iron nuggets and six string; a doll a clay ball, two wool and a string.
- Light: the chandelier 3 for every two burning candles (12 with all eight).
- **The Dust Sheet makes nothing.** It only moves a block (and that block's saved data) under itself and back. Breaking a sheeted block gives the covered block's own drops, with the tool the player is using: a sheet over stone stairs broken by hand gives only the sheet, as breaking stone stairs by hand gives nothing. A sheeted chest's contents spill once, from the sheet, never also from the chest.
- No conversion loops; nothing here makes items or energy.

## Multiplayer and persistence
- **Server authority.**
  - Lighting and snuffing the chandelier, playing and stopping the organ, covering and uncovering, opening and closing curtains, and using the mirror, suit and doll go through vanilla's block and item use paths (reach, spawn protection, adventure mode) and are decided on the server.
  - The chandelier's gusts and relighting, the organ's night playing and its notes run on the server, for everyone near.
  - **Covering a container closes it first** for anyone looking into it, and its contents move under the sheet before the chest is replaced, so nothing can be taken out of a covered chest or spilled twice. Clients are only told which block is under a sheet, never a chest's contents.
- **Client only.** The chandelier's sway and flames, the organ's keys, the suit's helmet and visor glow, the sheet's drape and breathing, the mirror's face, the curtains' sway and the doll's head are drawn by each client from what it already has (the time, the block states, where players are, its own camera), so nothing about them is sent or trusted. Every client works out the same nearest player for the suit. The doll turns for each player alone, by when that player last saw it.
- **Saved state.**
  - Block states: the chandelier's `lit` and `burning`; the organ's `part`, `facing`, `playing` and `powered`; the suit's `facing` and `half`; the mirror's `facing`; the curtains' `facing`, `open` and `part`; the doll's `facing`.
  - The organ's master block entity saves where it is in its tune (`tick`) and when it started (`start_time`, for the keys).
  - A sheet's block entity saves the covered block (`covered`) and, if that block had a block entity, its full saved data with its type (`covered_data`).
- **Bounded work.**
  - Only a playing organ ticks, on its master block, for its 200 ticks. A lit chandelier and the organ's master take random ticks; a relighting chandelier schedules one tick per candle.
  - The renderers do a little arithmetic a frame. The suit looks through the level's player list; the curtains look up at most eight blocks above them; the suit and doll keep one small entry per block entity in a weak map.
- New IDs only:
  - blocks with items: `haunted_chandelier`, `phantom_pipe_organ`, `suit_of_armor`, `spirit_mirror`, `tattered_curtains`, `creepy_doll`;
  - the `dust_sheet` block and the `dust_sheet` item (an item, not a block item: it covers blocks rather than being placed);
  - block entities: `haunted_chandelier`, `suit_of_armor`, `spirit_mirror`, `tattered_curtains`, `creepy_doll` (empty, for the client's drawing), `phantom_pipe_organ` (the tune) and `dust_sheet` (what it covers);
  - block tag `jugcraft:dust_sheet_coverable`, so packs can let sheets cover more or fewer blocks.
- **The `agriculture` switch** turns off their recipes; placed blocks stay and work, and sheets can still be pulled off.

## Dependencies and assets
No new dependencies. Every texture is drawn by code (`tools/decor7_textures.py`); the models, loot, tags and recipes come from `tools/decor7_data.py`. The organ and the suit are modelled whole and cut into one model per block. The chandelier, the suit's helmet and the doll's head are written out as quads to `assets/jugcraft/decor7_quads.json` for their renderers (`HauntedChandelierRenderer`, `SuitOfArmorRenderer`, `CreepyDollRenderer`, through `DecorQuads`); the organ's keys, the sheet, the mirror's face and the curtains are drawn by `PipeOrganRenderer`, `DustSheetRenderer`, `SpiritMirrorRenderer` and `TatteredCurtainsRenderer` (with `DecorDraw`).

The organ's tune is the opening of J. S. Bach's Toccata and Fugue in D minor, BWV 565, which is in the public domain; the arrangement for note-block sounds is written here.

## Verification
### Batch 7 verification

Actual results (2 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/agriculture-halloween-decor-7` stacked on decorations batch 6:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares the chandelier's candles, gusts, relighting, sway and ring, the organ's size, tune length and night chance, the suit's watching, the sheet's breathing and tag, the mirror's face, the curtains' drape and sway and the doll's glances with Java; checks every note of the organ's tune is in range, every state has a blockstate entry, and that the quads and textures the client draws exist) | Pass, 511 IDs |
| `./gradlew build` on `1d94dda` (later commits only change docs and screenshots) | Pass |
| Game tests on the headless server, same commit: 278 in total, 9 of them new here (`Decor7GameTests`) | **All 278 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `1d94dda`; no model, texture or quad errors in the log |

The 9 new game tests (`Decor7GameTests`):
1. a chandelier hangs under a stone ceiling but not from the open air, and is placed unlit; flint and steel lights all eight candles (light 12); a gust blows them all out; three relighting ticks later some but not all burn, and after nine all eight; an empty hand snuffs them and they stay out; it never sways more than its sway; it falls with its ceiling, dropping once;
2. an organ won't go where a block is in its way; placed, its six blocks are where they should be, facing the player, each knowing its master, which holds the tune; breaking a corner breaks all six and drops one organ;
3. using any block of the organ plays the tune from the top (its start time is the game time), using it again stops it; every note is in the tune and a note block's range and on the keyboard, the flute above the harp above the bass; a redstone block at a corner plays it; half way through it is still playing, and at the end it falls quiet;
4. a suit of armor stands two blocks tall facing the player, its helmet's block entity on the upper half; its helmet looks straight at a player ahead, 45 degrees toward one ahead and to its left, no more than 75 degrees, straight ahead at a player in front of an east-facing suit, and ahead with nobody near; it turns 4 degrees a tick and stops where it looks; broken, it drops once;
5. a dust sheet won't cover plain stone; used on a chest holding diamonds and apples it covers it instead of opening it, takes one sheet, keeps the chest and its data, spills nothing and takes the chest's shape; an empty hand pulls it off, giving back the chest with five diamonds and three apples and the sheet to the player; covered again and broken, it drops the sheet, the chest, five diamonds and three apples;
6. a dust sheet covers a rocking chair instead of sitting the player in it, and pulled off, the chair is back with its block entity;
7. a spirit mirror hangs on a wall facing out; by day it never shows a face; at night it shows one for 79 or 80 ticks of each 600, fully in the middle; it falls with its wall, dropping once;
8. three curtains placed down a wall make one drape (top, middle, bottom, facing out); using the middle one opens all three, and using the bottom one closes them; the cloth doesn't move at the rod, and sways at the hem no more than its sway, more at night;
9. a creepy doll sits on a fence post; of 300 glances about one in three find it looking elsewhere and the rest straight at the viewer, never turning more than it can; it falls with its post, dropping once; the seven recipes and loot tables load, and the sheet's tag holds chests and slabs but not stone.

The client game test (`Decor7ClientGameTests`) builds a dark-oak room open at the front: a lit Haunted Chandelier under the ceiling, the Phantom Pipe Organ playing against the back wall, Tattered Curtains at two windows (one drape drawn open), a Suit of Armor by the left wall, Dust Sheets over a rocking chair, a chest and a stair, a Spirit Mirror on the right wall and the Creepy Doll on a bookshelf. It photographs them by day and at midnight, then waits in front of the mirror until its face shows: the sheets drape over their furniture, the organ's keys and a note show as it plays, the chandelier's flames burn, the suit's visor glows red, and the face looks out of the mirror. The doll had looked away from the camera when it was photographed.

Found by CI and fixed before this record:
- 26.3 names `PushReaction.DESTROY` and `BLOCK` `POPPED` and `IMMOVEABLE` (a compile error).

**Not run (batch 7):**
- a person playing it in a client;
- a dedicated server with two players (each watched by the suit of armor in turn; both seeing the doll turn differently; one covering a chest the other has open);
- the chandelier's gust and the organ's night playing from real random ticks at night (the tests call the gust directly; the organ's night start is one random-tick check);
- the organ's sound (the CI client has no sound device);
- the doll turning as a real player looks away and back (its rule is tested; the screenshot shows it once).

## World and event applicability
- Batch 7 works anywhere, all year. The chandelier's gusts, the organ's night playing, the suit's visor glow, the sheets' breathing, the mirror's face and the curtains' night draft follow the Overworld's clock (as the earlier decorations' night effects do).

## Rollout and open questions
- The Dust Sheet covers only blocks in its tag, and only those that hold nothing a sheet could lose: no block entity, an empty one (the Jugcraft decorations'), or a container (chests, barrels, chiseled bookshelves). Shulker boxes, lecterns, jukeboxes and two-block things (doors, beds, the suit of armor) are left out on purpose.
- A sheeted chest that is half of a double chest becomes a single chest beside its other half; pulled off, the two join again.
- A sheet over a block keeps that block's shape but not its light, redstone or comparator output.
- The organ, like the giant pumpkin, is mirrored by structure mirroring as a turn, not a flip.
