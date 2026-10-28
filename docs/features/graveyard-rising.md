# Graveyard Rising

Status: implemented in source; **not yet through CI**. Nothing here has been compiled or run as a game test (below). It has not been played by hand, and the two-client dedicated-server playtest is still to do.
Proposal issue: none. On 27 October 2026 the owner asked for more Halloween ideas and chose the Graveyard Rising ("a raid-style wave defence at a village graveyard"), asking that it run "for the entire season not just halloween and make it last until nov 3rd". This is one of the season pieces, each in its own pull request (the others: Headless Horseman sightings, the Blood Moon, costume disguises, the Haunted House contest, and the Horseman's lair and its fight).
Owner: @jimbozoomer-byte

Target milestone and tier: Milestone 3 (first homestead), as Halloween nights are ([halloween-nights.md](halloween-nights.md)). It needs a bell and six graves: gravestones cut in a stonecutter, grave mounds crafted from dirt and rotten flesh, or the graveyard pack's headstones and memorials, the memorial bench among them ([graveyard.md](graveyard.md)); a headstone of several blocks counts once.
Primary specialty and supported player role: combat and village defence, for a group or alone; it gives builders' graveyards something to do on Halloween nights.

## Player experience

Every night of the Halloween season, a graveyard by a bell can be woken:

1. **Stir the graves.** At night, in the Overworld, use a bell with at least **six graves** within 16 blocks of it (and no more than 6 blocks above or below). The ground rumbles and "The graves stir... Ring the bell again to wake the dead" shows over the hotbar. Every village has a bell at its meeting point, so a graveyard built by the village bell is the easiest to wake; a bell can also be taken with a pickaxe to a graveyard of your own.
2. **Wake the dead.** Use the bell again, between one and ten seconds later. The bell resonates, "The dead are rising from their graves! Lay them back to rest" goes to everyone within 48 blocks, and a purple bar, **Graveyard Rising: wave 1 of 3**, shows how many of the risen are still walking.
3. **The waves.** Three seconds later the dead climb out of the ground beside the graves, in a burst of earth and soul fire:
   - wave 1: 4 zombies;
   - wave 2: 3 zombies and 3 skeletons with bows;
   - wave 3: 3 zombies, 2 skeletons, 2 husks and 1 stray (with a bow).

   Each wave comes five seconds after the last is laid to rest. Every player taking part after the first brings one more zombie a wave (at most four more). The risen hunt the players and the villagers, as any undead do.
4. **The graveyard rests.** Lay the last of the third wave to rest and "The graveyard rests. The dead lie still, and leave you a token of thanks" goes to everyone taking part. Each of them gets the graves' thanks: three handfuls of Halloween sweets or bones, and one time in five a Skeleton Suit or Mummy Wraps. They earn **Graveyard Shift** (a challenge in the Husbandry tab): "Lay the dead of a Graveyard Rising back to rest".

Taking part means being alive, not spectating, and within 48 blocks of the bell.

**How it can fail.** The dead sink back into their graves, with a puff of smoke, and nobody is thanked:
- at dawn ("Dawn breaks, and the dead sink back into their graves");
- when nobody has been taking part for 30 seconds;
- when the graves have no room for at least half of a wave to climb out (walled in or under water);
- when the server switches it off.

One of the risen that strays more than 40 blocks from the bell sinks back, and another climbs out of a grave in its place.

**Limits.**
- Each bell's graveyard rises once a night. Each player is thanked once a night, whichever graveyard they defended: a second rising the same night can be fought but gives them nothing more.
- At most four risings at once on the server, and none within 64 blocks of another.
- Not in Peaceful. Ringing a bell out of the season, by day with no graveyard, or with no graves near it is just ringing a bell; nothing is said.
- The risen are the vanilla monsters with their usual drops and experience, and they do not break doors. A villager a risen zombie kills may become a zombie villager, as with any zombie.

### Settings

In `config/jugcraft.properties`:
- `halloween.graveyard_rising` (`on` or `off`, default `on`).
- The season's own settings bound it as every Halloween event is bound: `halloween.start` and `halloween.end` (20 October to 3 November by default), `halloween.timezone` and `halloween.mode`. A rising started on the season's last night runs on until dawn.
- The `agriculture` switch off stops it too.

## Connections

- **Input producer:** the graves (the stonecutter's gravestones, the grave mound's recipe, the graveyard pack's headstones) and a village bell.
- **Output consumer:** candy (for the candy bag and trick-or-treating's sweets), bones, and a second route to two costumes (both also craftable). Graveyard Shift.
- **Technology and magic:** none.
- **Required vs optional:** optional. Nothing in progression needs it, and every reward has another route all year.
- **Trade and solo routes:** solo or in a group; a group meets a few more of the dead.

## Balance and automation

- **Rewards bounded:** a player is thanked at most once a night (the night is saved with them), so it cannot be farmed by ringing bell after bell.
- **No new items or currency:** the gift is existing sweets, bones and costumes.
- **Monster loot:** 20 to 32 monsters a rising, each dropping what it always does, about one Minecraft night of fighting for each bell; less than any mob farm.
- **Bounded harm:** the risen break no blocks and leave at dawn.

## Multiplayer and persistence

- **Server authority:** the server decides everything: who rang, the graves, the waves, who takes part and who is thanked. The bell's ring is checked where the server already allows the use.
- **Bounded work:** a player's ring reads at most 33 by 13 by 33 blocks for graves, at most once a second each, only from loaded chunks. A rising under way is looked at every 20 ticks: the players within 48 blocks and its own risen (at most 12, plus four). Nothing runs while no rising is under way.
- **Saved state:** each player's last thanked night (an attachment, `jugcraft:graveyard_rested`, kept through death). Risings are not saved: a restart ends one, any of the risen left in the world sink back when they load, and which bells have risen tonight is forgotten.
- **Chunks:** nothing is loaded. The dead climb out only in loaded chunks.
- **Still to do:** the two-client dedicated-server playtest.

## Dependencies and assets

No new dependency, block, item, model or texture. The monsters, particles (earth, soul fire, smoke) and sounds (the bell's resonance, rooted earth breaking) are vanilla's.

New IDs (all under `jugcraft`):
- the block tag `graves` (the gravestones, the grave mound and `#jugcraft:headstones`);
- the gift loot table `gameplay/graveyard_rising`;
- the advancement `graveyard_shift`;
- the player attachment `graveyard_rested`;
- the config option `halloween.graveyard_rising`.

Nothing is renamed or removed.

## Verification

Run locally (27 October 2026):

| Check | Result |
| --- | --- |
| `python3 tools/check_mod_data.py`'s agriculture checks (`check_agriculture`, which now also runs `check_graveyard_rising`) | Pass |
| Deliberate breaks of the Java, the table, the tag, the loot table, the words and the config, one at a time (below) | 13 of 13 failed it |
| `python3 tools/generate_material_data.py`, then `git status` | Writes this feature's tag, loot table, advancement, words and resource list only (the files it rewrote for other features were put back) |
| `javac` on the new and changed Java and the test class, for syntax only | No syntax errors |
| `./gradlew build`, game tests and client game tests | Not run (the Fabric Maven is out of reach here); to be run by CI |

The audit checks that the numbers, waves, archers and names in `GraveyardRising.java` are `tools/agriculture.py`'s `GRAVEYARD_RISING`; that the table makes sense (the leash inside the range, the second ring after the first's cooldown and inside its window, risings too far apart to share a grave) and that no boss rises; that it is registered and on by default; that it starts only in the season and at night, ends at dawn, refuses Peaceful, reads no unloaded chunk, leaves the bell to ring, rings once per use, sends the risen left by a restart back, fails a wave the graves have no room for, and thanks a player once a night; and that its graves tag, gift loot table, advancement and words are written.

The 13 deliberate breaks: the graves needed; a wave's size; the season check gone; the bell's use swallowed; a player thanked twice a night; the option off by default; a boss in a wave (table and Java); the headstones left out of the graves tag; a keepsake changed in the loot table; the rising not registered; a message's words missing; the leash past the range (table and Java); graves read from unloaded chunks.

The game tests (`GraveyardRisingGameTests`):
1. ringing a bell out of season does nothing; with five graves it says too few, and a second ring the same moment is ignored; with six, by day it says the dead sleep; at midnight the first ring stirs the graves, the second wakes them and a rising is under way at the bell (stopped at once, cleanly); a third ring that night says the graves have risen already;
2. stepped through by hand: nothing climbs out at once; each wave brings its number (more with more players near), every one marked and beside a grave, archers with bows and skeletons from the second wave; a pause between waves; the last wave cleared, the graveyard rests, the player is thanked with items and Graveyard Shift; a second rising that night is won but thanks nobody twice;
3. dawn ends a rising and every one of the risen sinks back; graves walled in fail it.

Not run: all of the above game tests (CI will run them), the two-client dedicated-server playtest, and play by hand.

## World and event applicability

Only in the Halloween season (a rising started on its last night runs to dawn), only in the Overworld. Ending the season stops new risings; the graves, the gifts and the advancement stay.

## Rollout and open questions

- **Waves:** three waves of vanilla undead to start with; a Blood Moon could add a fourth, and a boss-like Gravekeeper could lead the last, once played.
- **Village graveyards:** villages don't build graveyards; a village graveyard is one players build by its bell. A village structure piece with graves could come later.
