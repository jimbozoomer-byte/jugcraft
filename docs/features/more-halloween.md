# More Halloween

Status: implemented in source for batches 7 to 9, not yet played. The Build workflow compiles it, and CI's game tests pass (recorded below). Batches 10 to 14 are planned and follow one pull request at a time.
Proposal issue: none; requested directly by the owner on 2 October 2026 ("I want to make more halloween content more decorations and fun festive content", then "Lets do those 45 by each category starting with Haunted House inside then Mad Scientist and Mosnters then Yard and Porch then Lighting and Glow then Party Games then Night Events then Treats then Costumes"). The 45 ideas ship one category per pull request, each stacked on the one before:
- batch 7, the haunted house inside: the Haunted Chandelier, the Phantom Pipe Organ, the Suit of Armor, the Dust Sheet, the Spirit Mirror, Tattered Curtains and the Creepy Doll;
- batch 8, the mad scientist and monsters: the Tesla Coil, the Lab Table, the Specimen Jar, the Mummy Sarcophagus, the Raven on a Perch and the Black Cat Figure;
- batch 9, the yard and porch: the Yard Inflatables (a ghost, a black cat, a pumpkin stack and a spider), the Animatronic Porch Witch, Grasping Hands, the Poseable Skeleton, Bone Wind Chimes, the Bat and Witch Weathervanes, the Spooky Sign, the Haunted Archway and the Dead Hollow Tree;
- batch 10, lighting and glow (planned);
- batch 11, party games (planned);
- batch 12, night events (planned);
- batch 13, treats (planned);
- batch 14, costumes (planned).

Owner: @jimbozoomer-byte
Target milestone and tier: batch 7 is Discovery tier (iron, candles, a note block, bones, planks, an armor stand, white carpet, glass, gold nuggets, soul sand, string, clay and wool). Batch 8 is Discovery tier too (iron, glass, a slime ball, sandstone, paper, rotten flesh, feathers, sticks, black terracotta, glowstone dust), apart from the Tesla Coil, which needs copper and copper cable (the tin and bronze tier) and a generator to run. Batch 9 is Discovery tier (wool, dyes, iron, redstone, a cauldron, bones, rotten flesh, dirt, string, slabs, planks, sticks, mossy cobblestone, lanterns, logs and a jack o'lantern).
Primary specialty and supported player role: building and play; builders (haunted houses and Halloween yards), groups (a shared haunted house to explore, a street of decorated yards), and anyone who likes a prop that does something when you are not looking.

Event-only activities in later batches follow the rule the earlier Halloween work set: they run only in the Halloween window, and anything crafted or placed stays all year. Nothing in batches 7 to 9 depends on the event.

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

### Batch 8: the Tesla Coil
21. A mad scientist's coil two blocks tall: a riveted iron base, a copper primary, a tall copper winding and a polished toroid on top (a lightning rod, two copper ingots, copper cable and three iron ingots). Cables connect to its base.
22. **Use it to switch it on.** With power (20 JE a tick) it hums, its winding glows (light 8), sparks spit from the toroid, and every second or two it throws a crackling violet arc to another running coil within eight blocks, or into the air if it stands alone. The arcs are harmless: nothing is struck.

### Batch 8: the Lab Table
23. A riveted steel operating table two blocks long with leather straps, and on it a patient under a stained sheet, one grey hand slipped out from under it. Place it and it lies away from you; breaking either block picks up the whole table.
24. **Give it a redstone signal and the patient sits bolt upright**, with a crackle of sparks and a groan; it lies back down when the power goes. At night it twitches now and then.

### Batch 8: the Specimen Jar
25. A tall glass jar of glowing green fluid (light 7) under an iron lid, with an eye, a tentacle, a tiny pumpkin or a brain floating in it, bobbing and turning slowly while bubbles rise past. Sneak-use it to put in the next; broken, it keeps its specimen.

### Batch 8: the Mummy Sarcophagus
26. A painted sarcophagus two blocks tall, gold and lapis on sandstone, standing up. **Use it, or give it a redstone signal**, and its lid grinds open, the mummy lurches out at you with its arms coming up and a groan, and four seconds later it shuffles back and the lid shuts.

### Batch 8: the Raven on a Perch
27. A raven on a turned wooden perch. Its head turns to watch the nearest player within eight blocks; now and then it ruffles its feathers and croaks. Use it and it caws and beats its wings at you.

### Batch 8: the Black Cat Figure
28. A glazed black cat sitting tall, its tail swishing slowly. At night its eyes glow green.
29. **Run past it** (sprinting within three blocks) and it arches its back and hisses, for a second and a half; then it won't again for three seconds. Use it and it purrs.

### Batch 9: Yard Inflatables
30. Four big vinyl figures, two blocks tall, each on a little blower: a **ghost** with its arms out, a **black cat** with a bow, a **pumpkin stack** in a witch's hat and a **spider** on eight legs (each its own block).
31. **Use it, or give it a redstone signal**, and the blower fills it over two seconds: it rises off the lawn, flopping forward as it fills, then stands up straight, wobbling in the breeze (more in the rain) and glowing from inside (light 7). Switched off, it sags and folds flat over three seconds, a low pile you can walk over.

### Batch 9: the Animatronic Porch Witch
32. A green-faced witch two blocks tall in a tall pointed hat, stirring a pot of bubbling green brew with a long wooden spoon, slowly, round and round. Her head turns to follow the nearest player.
33. **Walk up to her** (within four blocks, when nobody was there) and she throws her head back and cackles, stirring hard, her eyes glowing green, for two seconds. Someone who stays doesn't set her off again: they have to go and come back, and she rests ten seconds between cackles. At night her eyes glow anyway.

### Batch 9: Grasping Hands
34. Two rotting hands clawing up out of a mound of dirt. **Step on them** without sneaking and they snatch at your ankles: a groan, and Slowness II for a second and a half. They stay up, clenched, for under a second, then sink back and rest two seconds before they can grab again. Sneak past and they leave you alone; mobs that blunder over them are grabbed too.

### Batch 9: the Poseable Skeleton
35. A life-sized skeleton. **Use it to pose it:** sitting on the ground with its legs out, standing and waving, lounging back with its hands behind its head and a knee up, or hanging by its hands from whatever is above it (a beam, a branch, a porch roof).

### Batch 9: Bone Wind Chimes
36. Five bones and a little skull striker hung on string from a wooden disc, under a block. They swing in the wind, a little on a still day, more in the rain and wildly in a thunderstorm, and clack together now and then: more often and louder as the weather worsens.

### Batch 9: the Weathervanes
37. A black iron vane on a pole over the four compass points (which always point north, east, south and west), with a **bat** or a **witch on a broomstick** on its tail. It stands on a roof, a post or a fence, and turns to point into the wind: the same wind everywhere in the world, slowly coming round over the days, so every vane in a town points the same way. In a storm it swings about.

### Batch 9: the Spooky Sign
38. A weathered board on a stake with a warning slapped on in dripping red paint. **Use it** to paint the next: BEWARE, KEEP OUT, TURN BACK, GO AWAY, NO TRESPASSING, ABANDON HOPE.
39. **Write your own**, as on a gravestone: use a Name Tag named in an anvil on it (the tag isn't used up), or rename the sign in an anvil before placing it. Your words cover the painted ones; sneak-use with an empty hand to wipe them off. Broken, it keeps both.

### Batch 9: the Haunted Archway
40. A gateway three blocks wide and three tall for the front of a yard or a graveyard: two mossy stone pillars, each with an iron lantern on its front (light 14), and a wrought-iron arch with a skull and crossbones at its crown and cobwebs in its corners. You walk through the middle. Placed from the block you aim at to your right; broken as one. Use it to put the lanterns out or light them.

### Batch 9: the Dead Hollow Tree
41. A dead, gnarled tree four blocks tall: a dark hollow at its foot, a face in its bark with glowing yellow eyes, and bare branches reaching out over the blocks round it, two of them hung with lanterns (light 13). Only the trunk is solid. Placed and broken as one; use it to put the lanterns out or light them.

## Connections
- Batch 9 inputs: wool, a dye or a spider eye, iron nuggets and redstone (the inflatables), and redstone to run them; purple and black wool, redstone and a cauldron (the witch); bones, rotten flesh and dirt (the hands); bones (the skeleton); an iron nugget, string, a wooden slab and bones (the chimes); iron and black or purple dye (the vanes); planks, red dye and a stick (the sign), and a Name Tag to write on it; lanterns, iron and mossy cobblestone (the archway); lanterns, a stick, logs and a jack o'lantern (the tree).
- Batch 8 inputs: a lightning rod, copper, copper cable and iron, and the electric network's power (the coil); iron, white wool and rotten flesh, and redstone (the table); glass, an iron nugget and a slime ball (the jar); sandstone, gold nuggets, paper and rotten flesh, and redstone (the sarcophagus); feathers, black dye and sticks (the raven); black terracotta and glowstone dust (the cat).
- Existing input producer: iron, candles, flint and steel or fire charges (the chandelier); iron, bone, a note block and planks, and redstone (the organ); iron and an armor stand (the suit); white carpet (the sheet); a glass pane, gold nuggets and soul sand (the mirror); iron nuggets and string (the curtains); clay, wool and string (the doll). The sheet covers the earlier batches' Rocking Chair, Hay Bale Seat, Crystal Ball and Grimoire Stand, this batch's doll and mirror, and vanilla furniture.
- Existing output consumer: decoration, light (the chandelier), music (the organ), storage kept under a sheet.
- Technology connection: none needed for batches 7 and 9; the organ and the inflatables answer redstone. Batch 8's Tesla Coil runs on the electric network through the shared energy interface (`EnergyStorage.SIDED`), so any Jugcraft generator powers it, and its recipe needs copper cable; the lab table and sarcophagus answer redstone.
- Magic connection: none yet.
- Reachable entry path: everything is crafted from vanilla materials an early player has.
- Required vs optional: all optional decoration; nothing in progression needs them.
- How this stays useful without other branches: builders get a furnished haunted house and a Halloween yard that do things by themselves.

## Balance and automation
- No energy. A chandelier costs two iron nuggets, an iron ingot and three candles; an organ three iron ingots, two bones, a note block and three planks; a suit of armor four iron ingots and an armor stand; a sheet three white carpets (two wool); a mirror seven gold nuggets, a glass pane and a soul sand; three curtains three iron nuggets and six string; a doll a clay ball, two wool and a string.
- Light: the chandelier 3 for every two burning candles (12 with all eight).
- Batch 8: the Tesla Coil uses 20 JE a tick while it runs (buffer 4,000 JE, up to 64 JE a tick in): a coal generator runs it easily, a solar panel (8 JE a tick) can't alone. Nothing else in batch 8 uses energy. A coil costs a lightning rod, two copper ingots, a copper cable and three iron ingots; a table two white wool, a rotten flesh and five iron ingots; a jar an iron nugget, three glass and a slime ball; a sarcophagus five sandstone, two gold nuggets, a paper and a rotten flesh; a raven two feathers, a black dye and four sticks; a cat five black terracotta and a glowstone dust. Light: a running coil 8, a jar 7.
- Batch 9 uses no energy. Inflatables cost five wool, a dye (a spider eye for the spider), two iron nuggets and a redstone; the witch a purple wool, two black wool, a redstone and a cauldron; the hands two bones, a rotten flesh and a dirt; the skeleton six bones; the chimes an iron nugget, two string, a wooden slab and three bones; a vane two iron nuggets, a dye and two iron ingots; two signs five planks, a red dye and a stick; the archway two lanterns, two iron ingots and four mossy cobblestone; the tree two lanterns, a stick, three logs and a jack o'lantern. Light: a blown-up inflatable 7, the witch's brew 6, the archway's lanterns 14, the tree's 13.
- The hands' Slowness is harmless (no damage) and can't be farmed into anything.
- **The Dust Sheet makes nothing.** It only moves a block (and that block's saved data) under itself and back. Breaking a sheeted block gives the covered block's own drops, with the tool the player is using: a sheet over stone stairs broken by hand gives only the sheet, as breaking stone stairs by hand gives nothing. A sheeted chest's contents spill once, from the sheet, never also from the chest.
- No conversion loops; nothing here makes items or energy.

## Multiplayer and persistence
- **Server authority.**
  - Lighting and snuffing the chandelier, playing and stopping the organ, covering and uncovering, opening and closing curtains, and using the mirror, suit and doll go through vanilla's block and item use paths (reach, spawn protection, adventure mode) and are decided on the server.
  - The chandelier's gusts and relighting, the organ's night playing and its notes run on the server, for everyone near.
  - **Covering a container closes it first** for anyone looking into it, and its contents move under the sheet before the chest is replaced, so nothing can be taken out of a covered chest or spilled twice. Clients are only told which block is under a sheet, never a chest's contents.
- **Batch 8, server authority.** Switching a coil, sitting the patient up, changing a specimen, opening the sarcophagus and using the raven and cat go through vanilla's block use path (reach, spawn protection) or redstone, and are decided on the server. A coil's power, running and arcs (and which coil it arcs to) are worked out on the server and sent as block events; the cat watches for runners on the server, from where players really are, every 5 ticks, at the level's player list. The raven's flap is a block event.
- **Batch 9, server authority.** Switching an inflatable, posing the skeleton, changing or writing on a sign, and lighting the archway and tree go through vanilla's block use path (reach, spawn protection; writing and wiping the sign also need build permission) or redstone, and are decided on the server. The witch watches the level's player list on the server every 5 ticks; the hands grab on the server when something steps on them, and their phases run on scheduled ticks. A sign's own words are cleaned (control characters dropped, at most 50 characters) before they are kept or sent.
- **Client only.** The chandelier's sway and flames, the organ's keys, the suit's helmet and visor glow, the sheet's drape and breathing, the mirror's face, the curtains' sway and the doll's head are drawn by each client from what it already has (the time, the block states, where players are, its own camera), so nothing about them is sent or trusted. Every client works out the same nearest player for the suit. The doll turns for each player alone, by when that player last saw it.
- **Batch 8, client only.** The arcs (from the block event), the patient sitting up (from the block state), the specimens bobbing, the lid and mummy, the raven's head and wings, and the cat's tail and eyes are drawn by each client from what it already has. The coil keeps a set of running coils per level, so finding a partner searches no blocks.
- **Batch 9, client only.** The inflatables filling, flopping and wobbling, the witch's arm and head, the chimes swinging and clacking, the vanes turning and the sign's lettering are drawn by each client from what it already has (the block states, the time, the weather, where players are). The wind is the same function of the time on every client, so every vane points the same way; the chimes' clacking is each client's own sound.
- **Saved state.**
  - Block states: the chandelier's `lit` and `burning`; the organ's `part`, `facing`, `playing` and `powered`; the suit's `facing` and `half`; the mirror's `facing`; the curtains' `facing`, `open` and `part`; the doll's `facing`.
  - The organ's master block entity saves where it is in its tune (`tick`) and when it started (`start_time`, for the keys).
  - A sheet's block entity saves the covered block (`covered`) and, if that block had a block entity, its full saved data with its type (`covered_data`).
  - Batch 8 block states: the coil's `facing`, `half`, `enabled` and `active`; the table's `facing`, `part` (`foot`, `head`) and `powered`; the jar's `specimen`; the sarcophagus's `facing`, `half`, `open` and `powered`; the raven's `facing`; the cat's `facing` and `hissing`. The coil's lower half saves its energy (`energy`); the cat saves when it calms and when it may hiss again (`calm_at`, `ready_at`). A sarcophagus shuts by a scheduled tick.
- Batch 9 saved state: block states (the inflatables' `facing`, `half`, `on`, `powered`; the witch's `facing`, `half`, `cackling`; the hands' `facing` and `phase`: rest, grab, recover; the skeleton's `facing`, `half`, `pose`; the sign's `facing` and `words`; the archway's and tree's `facing`, `lit` and `part`). The witch saves when she calms, when she may cackle again and whether anyone is near (`calm_at`, `ready_at`, `someone_near`); a sign saves its own words (`words`), which go with its item as its name. The hands' phases run on scheduled ticks.
- **Bounded work.**
  - Only a playing organ ticks, on its master block, for its 200 ticks. A lit chandelier and the organ's master take random ticks; a relighting chandelier schedules one tick per candle.
  - The renderers do a little arithmetic a frame. The suit looks through the level's player list; the curtains look up at most eight blocks above them; the suit and doll keep one small entry per block entity in a weak map.
  - Batch 8: a running coil ticks (its energy, and an arc every 15 to 40 ticks); a cat looks every 5 ticks; nothing else ticks. Arcs, the patient, the specimens, the mummy, the raven and the cat's tail are a little arithmetic a frame on each client; the raven looks through the level's player list.
- Batch 9 bounded work: the witch looks every 5 ticks; the hands only act when stepped on, and then schedule two ticks; nothing else ticks. The renderers do a little arithmetic a frame; the witch's head reads the level's player list.
- New IDs only:
  - blocks with items: `haunted_chandelier`, `phantom_pipe_organ`, `suit_of_armor`, `spirit_mirror`, `tattered_curtains`, `creepy_doll`;
  - the `dust_sheet` block and the `dust_sheet` item (an item, not a block item: it covers blocks rather than being placed);
  - block entities: `haunted_chandelier`, `suit_of_armor`, `spirit_mirror`, `tattered_curtains`, `creepy_doll` (empty, for the client's drawing), `phantom_pipe_organ` (the tune) and `dust_sheet` (what it covers);
  - block tag `jugcraft:dust_sheet_coverable`, so packs can let sheets cover more or fewer blocks.
- Batch 8 new IDs: blocks with items `tesla_coil`, `lab_table`, `specimen_jar`, `mummy_sarcophagus`, `raven_perch`, `black_cat_figure`; block entities of the same names (`tesla_coil` its energy, `black_cat_figure` its timers, the rest empty, for drawing).
- Batch 9 new IDs: blocks with items `inflatable_ghost`, `inflatable_cat`, `inflatable_pumpkin`, `inflatable_spider`, `porch_witch`, `grasping_hands`, `poseable_skeleton`, `bone_wind_chimes`, `bat_weathervane`, `witch_weathervane`, `spooky_sign`, `haunted_archway`, `dead_hollow_tree`; block entities `inflatable` (all four inflatables), `porch_witch` (her timers), `bone_wind_chimes`, `weathervane` (both vanes) and `spooky_sign` (its words); the inflatables, chimes and vanes' are empty, for drawing.
- **The `agriculture` switch** turns off their recipes; placed blocks stay and work, and sheets can still be pulled off.

## Dependencies and assets
No new dependencies. Every texture is drawn by code (`tools/decor7_textures.py`); the models, loot, tags and recipes come from `tools/decor7_data.py`. The organ and the suit are modelled whole and cut into one model per block. The chandelier, the suit's helmet and the doll's head are written out as quads to `assets/jugcraft/decor7_quads.json` for their renderers (`HauntedChandelierRenderer`, `SuitOfArmorRenderer`, `CreepyDollRenderer`, through `DecorQuads`); the organ's keys, the sheet, the mirror's face and the curtains are drawn by `PipeOrganRenderer`, `DustSheetRenderer`, `SpiritMirrorRenderer` and `TatteredCurtainsRenderer` (with `DecorDraw`).

Batch 8's textures are drawn by code in `tools/decor8_textures.py` and its models, loot, tags and quads come from `tools/decor8_data.py` (the quads in `assets/jugcraft/decor8_quads.json`); the renderers are `TeslaCoilRenderer`, `LabTableRenderer`, `SpecimenJarRenderer`, `MummySarcophagusRenderer`, `RavenRenderer` and `BlackCatRenderer`. 26.3 has no plain cat sounds (cats use sound variants), so the cat hisses with a creeper's fuse, pitched up, and purrs with a low fox sniff; the raven croaks and caws with a parrot's call pitched far down.

Batch 9's textures are drawn by code in `tools/decor9_textures.py` and its models, loot, tags and quads come from `tools/decor9_data.py` (the quads in `assets/jugcraft/decor9_quads.json`; the weathervanes' silhouettes are drawn cut out, a new `cutout` flag on a quad). The archway is cut into one model per block; the tree's boxes go whole to the block their middle is in, so its branches reach out over the air beside it. The renderers are `InflatableRenderer`, `PorchWitchRenderer`, `BoneWindChimesRenderer`, `WeathervaneRenderer` and `SpookySignRenderer`. The witch cackles with the vanilla witch's celebration sound; the chimes clack on the xylophone note-block sound.

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

### Batch 8 verification

Actual results (2 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/agriculture-halloween-decor-8` stacked on batch 7:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares the coil's power, range and arcs, the table's sitting and twitching, the jar's specimens, light and bob, the sarcophagus's timing and swing, the raven's watching and ruffling and the cat's reach, hiss and swish with Java; checks every block state has a blockstate entry, and that the quads and textures the client draws and the coil's and jar's messages exist) | Pass, 524 IDs |
| `./gradlew build` on `86cb7a5` and on `f413e66` (which only moves the client test's camera and coils; later commits only change docs and screenshots) | Pass |
| Game tests on the headless server, same commits: 288 in total, 7 of them new here (`Decor8GameTests`) | **All 288 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `86cb7a5` and `f413e66`; no model, texture or quad errors in the log |

The 7 new game tests (`Decor8GameTests`):
1. arc offsets pack and unpack; a coil stands two blocks tall, its power found on the lower half; using either half switches it on, but without power it doesn't run; with power it runs, glowing (light 8), using 20 JE a tick; two running coils three blocks apart arc to each other, not into the air; switched off, it stops;
2. a lab table lies away from the player, foot where aimed and head beyond, the patient drawn from the foot; a redstone block by the head sits the patient up and taking it away lays it down; it sits up to 70 degrees and lies back 6 degrees a tick; by day it lies still and at night twitches 4 ticks in each 97; breaking the head breaks the table, dropping it once;
3. a specimen jar glows (light 7) with an eye in it; sneak-use puts in the tentacle, pumpkin, brain, then the eye again; it never bobs more than its bob; broken, it drops once and keeps its tentacle;
4. a sarcophagus stands two tall facing the player; used, both halves open; the lid swings a little a tick; after its time it shuts; a redstone block opens it and it shuts again though the power stays;
5. a raven faces the player who placed it; it ruffles for its ticks of each period; used, it flaps;
6. a walking player beside the black cat doesn't upset it; a sprinting one makes it hiss, its tail still; it settles after its time and, with the runner still there, rests until its cooldown is over, then hisses again;
7. the six recipes and loot tables load.

The client game test (`Decor8ClientGameTests`) builds a stone-brick lab open at the front: two running Tesla Coils, a Lab Table over a block of redstone (the patient sitting up), a counter of Specimen Jars (one of each specimen), a Mummy Sarcophagus, a Raven on a Perch and two Black Cat Figures. It photographs them by day and at midnight, waiting for a coil to arc before photographing the coils, opening the sarcophagus and setting one cat hissing: the arcs show between the coils, the patient sits up under its sheet, the specimens float in their green fluid, the lid stands open with the mummy stepping out, one cat arches its back with its tail up, and at night the cats' eyes glow green.

Found by CI and fixed before this record:
- 26.3 has no `SoundEvents.CAT_PURR` or `CAT_HISS` (cats use sound variants), a compile error; the cat purrs with a fox sniff and hisses with a creeper's fuse instead.
- The first screenshots had a coil in front of the jars and the sarcophagus cut off at the bottom; the coils and camera were moved (`f413e66`).

**Not run (batch 8):**
- a person playing it in a client;
- a dedicated server with two players (one running past the cat while the other walks; both seeing the same arcs; the raven turning from one player to the other);
- a coil on a real generator and cable (the tests fill its buffer directly; it uses the same `EnergyStorage.SIDED` lookup as the other machines);
- the sounds (the CI client has no sound device);
- the patient's night twitching and the jars' bubbles in motion (the rules are tested; screenshots are still).

### Batch 9 verification

Actual results (2 October 2026, Minecraft 26.3, Fabric Loader 0.19.3, Fabric API 0.161.0+26.3, Temurin JDK 25.0.4, GitHub Actions), on `claude/agriculture-halloween-decor-9` stacked on batch 8:

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares the inflatables' filling, light and wobble, the witch's reach, cackle, rest and stirring, the hands' grab and rest, the chimes' swing and clacking, the vanes' turning and the archway's and tree's sizes and light with Java; checks the designs, poses, phases and words agree, every block state has a blockstate entry, block models turn only by 22.5 or 45 degrees, and the quads, textures and messages the client uses exist) | Pass, 537 IDs |
| `./gradlew build` on `57adfe9` and on `b819e48` (which only thickens the archway's arch and enlarges the sign's board and words) | Pass |
| Game tests on the headless server, same commits: 298 in total, 10 of them new here (`Decor9GameTests`) | **All 298 pass** |
| Client game test (real client, Mesa software rendering, CI job `client`) | **Passes** on `57adfe9` and `b819e48`; no model, texture or quad errors in the log |

The 10 new game tests (`Decor9GameTests`):
1. each inflatable design is its own block; one stands two blocks tall, flat and dark, nothing of it up high; used, its blower runs on both halves and it glows (light 7); used again it goes flat; a redstone block runs it and taking it away lets it go flat; it fills in 40 ticks and empties in 60; it never wobbles more than its wobble; breaking the upper half drops it once;
2. the porch witch only stirs with nobody near; a visitor walking up sets her cackling on both halves, and she settles after her time; one who stays doesn't set her off again even after her rest; one who goes and comes back does; her spoon keeps to its round, slow and fast;
3. grasping hands need a solid floor; something sneaking over them isn't grabbed; something walking over them is (Slowness II, at most 30 ticks); they sink back after their time and grab nothing while they do, then rest; without their floor they drop once;
4. a skeleton is placed sitting, nothing of it in the upper half; use poses it waving, lounging, hanging and sitting again on both halves, filling the upper half only standing or hanging; broken, it drops once;
5. wind chimes won't go on the floor, only under a block; they swing more, and clack more often and louder, in rain and more again in a storm; without the block above they drop once;
6. both weathervanes are blocks of their own; one stands on a fence post, not on air; the wind it points into is always 0 to 360 degrees, turns less than a fifth of a degree a tick, and comes round by more than 30 degrees in a day and a half; still weather adds no gusts and a storm's are bounded; without its post it drops once;
7. a spooky sign faces the player saying BEWARE; use paints the next warnings; an unnamed Name Tag paints nothing; a named one paints its name and is kept; then use changes nothing, and sneak-use wipes the words; broken, it drops once, with its own words as its name and its painted warning kept;
8. an archway won't go where a block is in its way; placed, its seven blocks go up from the block aimed at to the player's right, facing them, with an opening in the middle; its pillar lanterns light 14; using its crown puts them all out and using a pillar lights them again; breaking a corner breaks it all and drops one archway;
9. a dead tree goes up four blocks; its lanterns light 13 at its third block; use puts them out; breaking its top breaks it all and drops one;
10. the thirteen recipes and loot tables load.

The client game test (`Decor9ClientGameTests`) builds a house front with a porch and a lawn: the Porch Witch at her pot, Bone Wind Chimes under the porch roof, a Bat and a Witch Weathervane on the roof, the four inflatables blown up, Grasping Hands, three Spooky Signs (one with its own words), the skeleton sitting on the porch, waving, lounging and hanging from a gallows beam, the Haunted Archway at the gate and the Dead Hollow Tree. It photographs them by day and at midnight, setting the witch cackling before photographing her.

Found by CI and fixed before this record:
- 26.3 has `SoundEvents.AXE_STRIP` as a sound holder, not a sound (a compile error); the sign plays its value.
- In the first screenshots the arch was thin and the sign's words were small; both were made larger (`b819e48`).

**Not run (batch 9):**
- a person playing it in a client;
- a dedicated server with two players (one walking up to the witch while the other stays; both seeing every vane point the same way; one writing on a sign the other reads);
- something really walking onto the grasping hands (the test calls `stepOn`, as the game does when something walks on them);
- the chimes and vanes in real rain and thunder (their rules are tested; the screenshots are in clear weather);
- the sounds (the CI client has no sound device).

## World and event applicability
- Batches 7 to 9 work anywhere, all year. The chandelier's gusts, the organ's night playing, the suit's visor glow, the sheets' breathing, the mirror's face and the curtains' night draft follow the Overworld's clock (as the earlier decorations' night effects do); so do batch 8's twitching patient and glowing cat eyes and batch 9's witch's eyes. Batch 9's chimes and vanes follow the weather where the player is.

## Rollout and open questions
- Tesla Coils only arc to coils that are running; a coil in an unloaded chunk drops out of the set until it runs again.
- The Lab Table's patient and the sarcophagus's mummy are props: they can't be fought, and nothing comes out of them.
- The Dust Sheet covers only blocks in its tag, and only those that hold nothing a sheet could lose: no block entity, an empty one (the Jugcraft decorations'), or a container (chests, barrels, chiseled bookshelves). Shulker boxes, lecterns, jukeboxes and two-block things (doors, beds, the suit of armor) are left out on purpose.
- A sheeted chest that is half of a double chest becomes a single chest beside its other half; pulled off, the two join again.
- A sheet over a block keeps that block's shape but not its light, redstone or comparator output.
- The organ, like the giant pumpkin, is mirrored by structure mirroring as a turn, not a flip.
- The archway and tree, like the organ, are mirrored as a turn, and stop being placed where any block they need isn't free.
- The wind is one for the whole world (all dimensions); it doesn't depend on biome or height.
- An inflatable glows from inside at full brightness while it is blown up, day or night.
