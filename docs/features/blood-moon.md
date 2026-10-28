# The Blood Moon

Status: implemented in source; **not yet through CI**. Nothing here has been compiled or run as a game test (below). It has not been played by hand, and the two-client dedicated-server playtest is still to do.
Proposal issue: none. On 27 October 2026 the owner chose a Blood Moon among the Halloween ideas, and asked for it to run "for the entire season not just halloween". It is built on the Horseman's sightings ([horseman-sightings.md](horseman-sightings.md)), which it calls on.
Owner: @jimbozoomer-byte

Target milestone and tier: Milestone 3 (first homestead), as Halloween nights are ([halloween-nights.md](halloween-nights.md)). Nothing is needed to see it.
Primary specialty and supported player role: play and atmosphere, and fighters and candy hunters on its nights.

## Player experience

Through the Halloween season (20 October to 3 November by default), **every fourth night** and **Halloween night itself**, a Blood Moon rises:

- **"A Blood Moon rises: the dead walk thick tonight, and the Horseman rides"**, to every player at dusk, and "The Blood Moon sets" at dawn.
- **The night sky turns blood red**, deepening as night falls and fading back at dawn, and a thin red haze hangs about everyone out under it.
- **The dead dress up.** Zombies, husks, skeletons, strays and zombie villagers that come into the world put on a costume 40% of the time instead of 15%.
- **Candy comes thick.** A costumed mob a player kills drops two rolls of candy instead of one.
- **Will-o'-wisps gather.** The wisps of swamps and cornfields come every time the server looks, not half the time, and up to 8 gather near a player instead of 4.
- **The Horseman rides.** Everyone out under it who has not yet seen the Headless Horseman that day sees him ride by ([horseman-sightings.md](horseman-sightings.md)), harmlessly as ever.
- Everyone in the Overworld when it rises earns **Bad Moon Rising** (in the Husbandry tab): "Be out in the Overworld when a Blood Moon rises".

On Halloween night the Harvest Moon is up too, so crops grow fast under a blood-red sky.

### Settings

In `config/jugcraft.properties`:
- `halloween.blood_moon` (`on` or `off`, default `on`): whether it rises at all.
- The season's own settings bound it: `halloween.start`, `halloween.end`, `halloween.timezone`, `halloween.mode` and `halloween.harvest_moon` (the Halloween day it always rises on).
- The `agriculture` switch off stops it too.

## Connections

- **Input producer:** none needed.
- **Output consumer:** the costumed mobs' candy (the Halloween treats, the costume contest's costumes), the wisps (Wisps in Jars), the Horseman's sightings and the advancement Bad Moon Rising.
- **Technology and magic:** none.
- **Required vs optional:** optional. Nothing in progression needs it; it only makes its nights livelier.
- **Trade and solo routes:** everyone sees it.

## Balance and automation

- **It only adds to what the season already brings:** more costumes, candy and wisps, never anything new to farm. The audit holds each number above the season's own and the wisps under their world cap (64).
- **Bounded:** at most one night in four, and Halloween's; the candy's extra roll is the costumed-mob table's own.

## Multiplayer and persistence

- **Server authority:** the server works out whether it is up (every 100 ticks), tells players, dresses mobs, rolls candy and sends wisps and the Horseman. Clients only turn their sky red when the server says it is up.
- **Bounded work:** every 100 ticks a date and clock check; every 40 ticks while it is up, six red motes for each player out under it.
- **Saved state:** none. It is worked out from the date and the overworld's clock, and a player who joins is told whether it is up.
- **Chunks:** nothing is loaded.
- **Ending the season:** it stops rising. The costumes and candy already handed out stay, as every Halloween event's do.

## Dependencies and assets

No new dependency, texture or sound. The sky is the vanilla sky with its colour turned red by one client mixin (`BloodMoonSkyMixin`, at the end of the sky's render state, the same hook the Jimtafacts realm sky uses); only the colour the sky pass is given changes. The moon itself keeps its colour: its disc has no colour of its own in the sky state. The haze is vanilla dust particles.

New IDs (all under `jugcraft`):
- the payload `blood_moon` (server to client);
- the advancement `bad_moon_rising`;
- the config option `halloween.blood_moon`.

## Verification

Run locally (27 October 2026):

| Check | Result |
| --- | --- |
| `python3 tools/check_mod_data.py`'s agriculture checks (`check_agriculture`; `check_night` now also checks the Blood Moon) | Pass |
| Deliberate breaks, one at a time (below) | 5 of 5 failed it |
| `python3 tools/generate_material_data.py`, then `git status` | Writes this feature's words, advancement and resource list only (other features' drift put back) |
| `javac` on the new and changed Java and the test classes, for syntax only | No syntax errors |
| `./gradlew build`, game tests and client game tests | Not run (the Fabric Maven is out of reach here); to be run by CI |

The audit now checks: the Blood Moon's numbers against `tools/agriculture.py`'s `BLOOD_MOON`; that it rises at most every other night and only adds (more costumes, candy and wisps, never past the wisps' world cap); that it is wired into the costumes, the candy, the wisps, the sightings, the client's sky and its mixin; that it is on by default, with its advancement and words.

The 5 deliberate breaks: fewer costumes than the season's own (table and Java); the candy rolled once; the sky mixin unregistered; the Horseman not sent under it; the haze's count in the Java.

The game tests (`BloodMoonGameTests`):
1. it rises on every fourth night of the season, not the night after, never by day, from dusk to dawn; on Halloween whatever the night; never out of season;
2. under it, 40% of mobs dress up; without it, 15% as ever;
3. the sky it turns is blood red at night and untouched by day.

The client test (`NightClientGameTests`) runs on Halloween night, a Blood Moon's, so its night pictures now show the red sky, and it adds one looking up at it (`jugcraft_blood_moon`).

Not run: all of the above game and client tests (CI will run them), the two-client dedicated-server playtest, and play by hand.

## World and event applicability

Only in the Halloween season, only in the Overworld's sky. Ending the season stops it; the advancement stays.

## Rollout and open questions

- **How often:** every fourth night. The owner may want it rarer or more often after playing.
- **The moon's own colour:** turning the moon's disc red would take a texture swap or a deeper renderer hook; the sky's colour carries it for now.
