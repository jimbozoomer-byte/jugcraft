# The Graveyard Pack

Status: pack 1 (headstones, weathering and the Stonemason's Chisel) is implemented in source, not yet played by hand. The Build workflow compiles it; CI's game tests and client screenshots are recorded below.
Proposal issue: none; requested directly by the owner on 3 October 2026 ("do a serious high quality graveyard pack with high quality detailed blocks and multiblocks"). It comes in four packs, one pull request each, each stacked on the one before:
1. headstones: nine life-sized memorials in four stones that weather with the years, and the Stonemason's Chisel that cuts their epitaphs;
2. monuments: an obelisk, a draped urn, an angel at a tomb, a trumpeting angel on a column, an iron mortsafe and a faithful hound;
3. buildings: a walk-in family mausoleum, a lych gate, grand cemetery gates and a columbarium wall;
4. grounds: kerbed grave plots, grave vases, lamp posts, a memorial bench and an open grave, and what keeping a graveyard does.

It goes beyond the Halloween decorations' plain gravestones and crypt set (batch 3): those stay as they are, and this pack's memorials are the serious, finely carved kind. Nothing in it depends on the Halloween event.

Owner: @jimbozoomer-byte
Target milestone and tier: Discovery tier. Every headstone is cut from a vanilla stone (calcite for marble, polished deepslate for slate, granite, smooth sandstone), in a stonecutter or a crafting table; the chisel is an iron ingot and a stick.
Primary specialty and supported player role: building. A builder lays out a churchyard of real-sized, carved memorials, each with its own epitaph, and decides how old it looks: kept clean and waxed, or left to moss and ivy. Ghost hunters gain a reason to let one go to ruin, since neglected graves raise more restless spirits.

## Player experience
### Headstones
1. **Gothic Headstone** (white marble; one calcite in a stonecutter): a pointed gothic head on a moulded plinth, 1.3 m tall, with slender colonnettes either side, a raised moulding following the arch, a carved trefoil in its head and a small cross for a finial. The epitaph is cut on its polished panel.
2. **Slate Headstone with Willow and Urn** (slate; polished deepslate): a thin New England slate with a round tympanum between two shoulders, set straight into the ground. The tympanum is carved with a weeping willow over a funerary urn, the shoulders with rosettes, and an incised border runs down each side.
3. **Slate Headstone with Winged Skull**: the same slate, its tympanum carved with the older death's head: a skull with hollow eyes, nose and teeth between spread feathered wings, an hourglass above.
4. **Lamb Headstone** (marble; calcite): a small rounded stone for a child, a lamb lying on its top with its head raised and turned to the front.
5. **Broken Column** (marble; three calcite in a column, two blocks tall): a fluted column snapped off on a pedestal, a shroud thrown over the break: a life cut short. The epitaph is cut on the pedestal's framed panel.
6. **Celtic High Cross** (granite; five polished granite in a cross, three blocks tall, 2.75 m): a stepped, rock-faced base whose die carries the epitaph, a shaft and arms faced with knotwork, a ring binding the arms, and bosses at the crossing.
7. **Rustic Scroll Headstone** (granite; a stonecutter): a rough boulder with a polished scroll unrolled down its face for the epitaph and a spray of carved fern across its shoulder.
8. **Table Tomb** (sandstone; five smooth sandstone as a table, two blocks long): a chest tomb with corner balusters, panelled sides carved with roundels, an urn at its foot, and an overhanging ledger slab whose top carries the epitaph, read from the foot.
9. **Ledger Stone** (sandstone; two smooth sandstone slabs side by side, two blocks long): a bevelled slab lying on the grave with an incised border, a cross carved at its head and the epitaph across its foot half.

Each faces whoever places it and needs all its blocks free. Breaking any block of a tall or long one breaks the whole and drops it once, keeping its epitaph.

### Weathering
- A headstone weathers in four stages: **clean**, **worn** (duller, grime run down from the top, chipped), **mossy** (moss creeping up from the ground in cushions, lichen rosettes, orange on granite and sandstone) and **overgrown** (green to a third of its height, crusted with lichen, ivy clumps at its foot and a trail climbing one side).
- Each random tick an unwaxed headstone takes the next stage 2% of the time, twice that with open sky above it: about an hour of play per stage, half that under open sky.
- A **Brush** scrubs a stage off and wears by one. Scrubbing an overgrown stone earns **Groundskeeper**.
- **Honeycomb** waxes it: it weathers no more, and bone meal does nothing to it. An **axe** scrapes the wax off, as on copper.
- **Bone Meal** ages it a stage at once, for builders who want an old churchyard now.
- Every block of a tall or long headstone weathers together.
- **The letters fade into the stone** as it weathers (by 18%, 36% and 52% of the way to the stone's colour), so a neglected stone grows hard to read until it is scrubbed.

### Epitaphs and the Stonemason's Chisel
- The **Stonemason's Chisel** (an iron ingot over a stick; 250 uses) used on a headstone opens the epitaph screen: a slab of slate with four lines to write, filled with what is cut there now. **Done** cuts them; **Cancel** changes nothing. Each epitaph cut wears the chisel by one, and the first earns **Here Lies…**.
- Each line is up to 24 characters and is cut **as large as it fits** its stone, so a short name stands out above the dates; if the lines together are too tall for the space, all shrink alike. Marble and sandstone take dark letters, slate and granite pale ones.
- A **named Name Tag** cuts its name as the first line and keeps the rest (the tag is kept). A headstone renamed in an anvil is placed with its name as the first line.
- A broken headstone keeps its epitaph on the item (shown in its tooltip) and brings it back when placed again.

### Neglected graves
Headstones are graves for ghost hunting: at night they stir restless spirits as the other graves do, but a quarter as often while clean, half as often when worn, as often when mossy and half as often again when overgrown. A well-kept churchyard is a quiet one.

## Connections
- Existing input producer: vanilla calcite, deepslate, granite, sandstone and iron; the Brush (vanilla archaeology), honeycomb (bees), bone meal.
- Existing output consumer: ghost hunting (restless spirits rise from the headstones, more from neglected ones; the Spirit Board, the ofrenda and the Spirit Lantern all work with them). Building: the batch 3 cemetery fence, crypt stone and mourning angel, the Halloween lights and candles.
- Technology connection: none needed; a stonecutter cuts most of them.
- Magic connection: the restless spirits of ghost hunting.
- Reachable entry path (prove no circular unlock): calcite, deepslate, granite and sandstone are found in any world from the first day; an iron ingot and a stick make the chisel. Nothing here is gated behind anything else in the pack.
- Which connections are required vs optional; trade and solo routes: all optional; one player can build a churchyard alone, and headstones (engraved or not) trade like any block.
- How this specialty stays useful without mastering every other branch: it is decoration with upkeep; no other branch needs it.
- For infrastructure/cosmetics, supported systems and reason resource links do not apply: decoration; the only effect on play is the spirits.

## Balance and automation
- Each headstone costs one to six vanilla stone blocks and returns only itself when broken: nothing breaks down into anything else, so there is no conversion loop.
- Weathering needs no fuel and makes nothing. Brushing costs brush wear; waxing costs a honeycomb; aging costs a bone meal.
- Spirits only rise at night, at most three within 16 blocks of a grave (ghost hunting's own cap); a churchyard of neglected headstones does not raise more than any other graves would, only more often up to that cap.

## Multiplayer and persistence
- **Server authority.** Everything that changes a headstone needs build rights (`mayBuild`), like editing a sign. The epitaph screen's lines are checked on the server before they are cut: the player must have opened a session on exactly that headstone with the chisel within the last 6,000 ticks (one session, one cut), still hold a chisel, be within reach, be allowed to use items there, and send at most 4 lines of 24 characters (the network codec refuses anything much larger before it is read). Control characters are dropped and lines trimmed. Sessions end when the player leaves or the server stops.
- The epitaph lives in the block entity of the headstone's first block (and its item); weathering and wax are block state on every block, so they survive restarts and chunk unloads.
- With the agriculture feature off, the recipes do not load and headstones stop weathering; placed ones keep their stage, wax and epitaph, and everything stays registered.
- New IDs only: blocks with items `gothic_headstone`, `willow_urn_headstone`, `winged_skull_headstone`, `lamb_headstone`, `broken_column`, `celtic_cross`, `rustic_scroll_headstone`, `table_tomb`, `ledger_stone`; item `stonemasons_chisel`; block entity `headstone`; data component `epitaph`; tags `jugcraft:headstones` (block and item); advancements `here_lies` and `groundskeeper`. No migration.

## Dependencies and assets
No new dependencies. Every texture is drawn by code in `tools/graveyard_textures.py` (the four stones at four stages, carved and rough faces, knotwork, ivy and the chisel). Models are generated by `tools/graveyard_models.py` (boxes, with the lamb sculpted by `tools/sculpt.py`) and `tools/graveyard_data.py`. Sounds are vanilla's (stonecutter, brush, honeycomb, axe, bone meal). The letters are drawn by the client with the game's own font (`HeadstoneRenderer`).

## Verification
| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py` (now also compares every headstone's style, cells, boxes and epitaph place, the weathering numbers, the stones' ink, the epitaph's limits, the chisel and its session with `tools/graveyard.py`; checks every part and stage has its model, every state a variant, and the loot, recipes, words, textures and advancements) | Pass, 1025 IDs; changing one value in Java makes it fail (tried) |
| Build workflow: `./gradlew build` | Pass (run 37137610198, commit d358fb5b) |
| Server game tests (`HeadstoneGameTests`, 4) | Pass: all 574 required tests passed in that run |
| Client game test (`HeadstoneClientGameTests`) | Pass (client shard 1 of that run); screenshots below |

The four game tests:
1. a Celtic cross placed from its item goes up three blocks and a table tomb back two, facing the player; with a block in the way nothing is placed; breaking the cross's top or the tomb's head breaks the whole and drops one;
2. the epitaph is refused without a session, with a session for another block, with five lines or a line too long, without build rights, without a chisel and from far off; using the chisel on the stone opens the session and the lines are cut, the chisel wears and Here Lies… is earned; a second cut needs a new session; control characters, spaces and blank last lines are dropped; a named Name Tag replaces the first line and keeps the rest; the broken headstone keeps its epitaph and brings it back when placed;
3. bone meal ages a slate to overgrown (a fourth is not used), a brush scrubs a stage off and wears (Groundskeeper), honeycomb waxes it, after which 2,000 random ticks and bone meal change nothing; an axe scrapes the wax off and 2,000 random ticks weather it to overgrown; both blocks of a broken column age together; a player without build rights changes nothing; the more weathered, the more often a grave stirs;
4. every headstone's recipe and loot table, the chisel's recipe and both advancements load.

The client game test builds a churchyard of all nine headstones, engraved and at mixed stages, between a gravel path and a wrought-iron fence with lanterns, and a row of four gothic headstones at the four stages; it photographs the whole by day and night, each group up close, the table tomb and ledger stone from their feet, the four stages, and the epitaph screen. It prints the epitaph each client block entity received.

**Not run:** a person playing it in a client (opening the epitaph screen with the chisel and typing; weathering over real hours); a dedicated server with two or more players cutting epitaphs at once.

### Pack 1 verification
Run [37137610198](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37137610198) on commit d358fb5b passed every job: the data check (1,025 IDs), the build and all 574 server game tests, and the three client shards. In the client game test:
- every headstone drew with its model and textures, and the log shows no missing model or texture for any of them (the only resource errors are the costume equipment files' known "Map must have contents", unrelated);
- the client received every epitaph, printed in the log, e.g. `gothic_headstone: [IN LOVING MEMORY, ELIZA THORNE, 1841 - 1899, AT REST]`, `table_tomb: [SIR EDMUND HALLOWAY, 1690 - 1761, REQUIESCAT IN PACE]`;
- the screenshots show the letters cut on each front, on the table tomb's and ledger's tops read from the foot, the four stages side by side with the letters fading, and the epitaph screen ([the guide](../branches/AGRICULTURE.md#the-graveyard-headstones) has them).

The commit after it changes only the client test's camera (it no longer leaves holes in the turf where it stood, and frames the whole churchyard closer) and these documents.

## World and event applicability
- No worldgen: headstones are only placed by players.
- Spirits rise only at night on the Overworld clock, as ghost hunting's do.
- Not seasonal: nothing here waits for Halloween, and nothing is removed after it.

## Rollout and open questions
- The statues of pack 2 and the buildings of pack 3 use the same block (`HeadstoneBlock`), so they weather, wax and take epitaphs the same way.
- Epitaphs are plain text: no colours or formatting codes.
- Weathering is by random ticks, so a churchyard far from any player does not age (as crops do not grow).
