# Haunted House contest

Status: implemented in source; **not yet through CI**. Nothing here has been compiled or run as a game test (below). It has not been played by hand, and the two-client dedicated-server playtest is still to do.
Proposal issue: none. On 27 October 2026 the owner asked for more Halloween ideas and chose the Haunted House contest ("trick-or-treating children score player-built haunted houses by their props; winners at the event's end"), asking that it run "for the entire season not just halloween and make it last until nov 3rd". This is one of the season pieces, each in its own pull request (the others: Headless Horseman sightings, the Blood Moon, costume disguises, the Graveyard Rising, and the Horseman's lair and its fight).
Owner: @jimbozoomer-byte

Target milestone and tier: Milestone 3 (first homestead), as trick-or-treaters at your door are. It needs what they need: a Candy Bowl with sweets in it, by a wooden door with a porch light. A higher score needs Halloween decorations, from carved pumpkins and cobwebs to the decoration batches' props.
Primary specialty and supported player role: building and decorating, for every player; a friendly contest between neighbours on a server.

## Player experience

All through the Halloween season (20 October to 3 November by default), every visit by trick-or-treaters is a judging:

1. **The children judge.** When a group of trick-or-treaters is answered at your Candy Bowl and takes at least one treat, they look round the house before they go. Everyone near the door, and you, are told: "The trick-or-treaters rate Alex's haunted house: 37 points (25 props of 6 kinds). Best this season: 41".
2. **How they score.** Every Halloween prop within 12 blocks of the door (across), 4 below and 10 above, counts: a point for each prop, at most 4 of a kind, and 2 more for each kind. Variety pays: ten kinds of prop beat forty of one. At 40 points or more the children shriek and run giggling from the door.
3. **Your entry.** Your best score of the season is your entry; a worse night never lowers it. Sneak-use your bowl with an empty hand to see it: "Alex's haunted house: best score 41, place 2 of 7 this season". The first judging earns **Haunted House** (Husbandry tab): "Have trick-or-treaters judge your haunted house".
4. **The winners.** When the season ends the server announces the contest's results to everyone. The three best houses win the Harvest Scale's **First**, **Second** and **Third Prize Ribbons**; a winner who is offline gets theirs on their next visit. First place earns **Scariest House on the Block** (a challenge): "Win first place in the Haunted House contest".

**What counts as a prop.** Carved pumpkins and jack o'lanterns (vanilla and hand-carved, every variety), cobwebs, skeleton, wither skeleton and zombie heads, soul lanterns, scarecrows, gravestones and the graveyard pack's headstones, the Wisp in a Jar and the Horseman's Lantern, and the Halloween decorations: the coffin, haunted portrait, fog machine and string lights; candles and sconces; the graveyard's fences, crypt stone, mounds and statues; the witch's cottage; the haunted house and yard, inside and out; the mad scientist and monsters; the yard and porch; lighting and glow; the jump scare trap, ghost bell and fortune teller's table; and the Halloween bonfire. The candy bowl itself, party games, treats, harvest decorations and toilet-paper streamers do not count.

**Who is judged.** The bowl's owner (whoever placed it), and only while they are on the server; the children never judge an empty bowl's house (they are too busy toilet-papering it). Many bowls make one entry: an owner's best house counts.

### Settings

In `config/jugcraft.properties`:
- `halloween.haunted_house` (`on` or `off`, default `on`): whether trick-or-treaters judge houses. Prizes already won are still handed out.
- The season's own settings bound it, as every Halloween event is bound: `halloween.start`, `halloween.end`, `halloween.timezone` and `halloween.mode`. The trick-or-treaters themselves come only between dusk and midnight in the season.
- The `agriculture` switch off stops it too.

## Connections

- **Input producer:** the trick-or-treaters (Candy Bowl, sweets, a porch light) and the Halloween decorations.
- **Output consumer:** the Harvest Scale's prize ribbons, the same the carving contest gives, and two advancements.
- **Technology and magic:** none.
- **Required vs optional:** optional; nothing in progression needs it.
- **Trade and solo routes:** a player alone can enter and win; on a server it is a contest between neighbours.

## Balance and automation

- **No new items or currency:** the prizes are the existing ribbons, three a season.
- **Bounded:** a score depends only on blocks the player built; it gives nothing but a place in the standings. Trick-or-treaters come at most six groups a night across the server.
- **Fair:** at most 4 of a kind count, so one prop placed many times cannot win; variety does.

## Multiplayer and persistence

- **Server authority:** the server counts the props, keeps the scores and hands out the prizes. The client sees only messages.
- **Bounded work:** a judging reads 25 by 15 by 25 blocks round the door, only from loaded chunks, once a visit. The end-of-season check runs every 200 ticks.
- **Saved state:** `data/jugcraft_haunted_house_contest.dat` in the Overworld: each Halloween's entrants (name and best score, at most 4,096) and whether it was awarded (the latest eight years), and the prizes still owed. Every contest is awarded once.
- **Chunks:** nothing is loaded.
- **Still to do:** the two-client dedicated-server playtest.

## Dependencies and assets

No new dependency, block, item, model or texture. The shriek's sound and particles are vanilla's.

New IDs (all under `jugcraft`):
- the block tag `haunted_props`;
- the advancements `haunted_house` and `scariest_house_on_the_block`;
- the saved data `haunted_house_contest`;
- the config option `halloween.haunted_house`.

Nothing is renamed or removed.

## Verification

Run locally (27 October 2026):

| Check | Result |
| --- | --- |
| `python3 tools/check_mod_data.py`'s agriculture checks (`check_agriculture`, which now also runs `check_haunted_house`) | Pass |
| Deliberate breaks of the Java, the table, the tag, the words and the config, one at a time (below) | 14 of 14 failed it |
| `python3 tools/generate_material_data.py`, then `git status` | Writes this feature's tag, advancements, words and resource list only (the files it rewrote for other features were put back) |
| `javac` on the new and changed Java and the test class, for syntax only | No syntax errors |
| `./gradlew build`, game tests and client game tests | Not run (the Fabric Maven is out of reach here); to be run by CI |

The audit checks that the numbers and names in `HauntedHouseContest.java` are `tools/agriculture.py`'s `HAUNTED_HOUSE`; that there are no more places than the Harvest Scale has ribbons; that it is registered and on by default; that children judge only when they took a treat, only in the season and only an owner on the server; that it reads no unloaded chunk, counts at most 4 of a kind, keeps at most 4,096 entrants and awards a contest once; that sneak-using a bowl tells its standing; and that the props tag holds exactly the table's props, all real blocks and never the bowl, with the advancements and words written.

The 14 deliberate breaks: the cap of a kind (number); the season check gone; props read from unloaded chunks; the cap of a kind gone; a contest awarded twice; a house judged when the bowl is empty; no standing at the bowl; the contest not registered; the option off by default; the bowl a prop (table and tag); a prop that is not a block (table and tag); a prop left out of the tag; a message's words missing; four places for three ribbons.

The game tests (`HauntedHouseContestGameTests`):
1. the children count a point a prop, at most four of a kind and two more a kind (six cobwebs, two carved pumpkins and a jack o'lantern: 13 points), plain blocks and a prop out of reach for nothing;
2. nobody is judged out of season or for a bowl with no owner; the first judging is the entry, a worse night keeps the best, a better one raises it, and Haunted House is earned;
3. a long-past contest is awarded: the three best of four houses get the First, Second and Third Prize Ribbons and the fourth nothing, only first place earns Scariest House on the Block, and checking again awards nothing twice.

Not run: all of the above game tests (CI will run them), a full trick-or-treat visit with judging in a running game, the two-client dedicated-server playtest, and play by hand.

## World and event applicability

Only in the Halloween season, wherever trick-or-treaters come. When the season ends the contest is decided; the ribbons, advancements and every prop stay.

## Rollout and open questions

- **Scoring:** a point a prop, 4 of a kind, 2 a kind; the owner may want lit props, a fog machine running or a sprung jump scare to count for more after playing.
- **Prizes:** the Harvest Scale's ribbons for now; a haunted-house trophy could follow.
