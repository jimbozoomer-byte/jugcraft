# Headless Horseman sightings

Status: implemented in source; **not yet through CI**. Nothing here has been compiled or run as a game test (below). It has not been played by hand, and the two-client dedicated-server playtest is still to do.
Proposal issue: none. On 27 October 2026 the owner asked for the Headless Horseman to target players in the Halloween season, not necessarily to kill them, "but I want them to see him", and for a lair where he lives once the season is over. They chose sightings that only frighten ("Scare only"), a few per player per season, and the lair for the off-season only. This is the first of those pieces; his lair, its fight and his loot follow in their own pull requests.
Owner: @jimbozoomer-byte

Target milestone and tier: Milestone 3 (first homestead), as Halloween nights are ([halloween-nights.md](halloween-nights.md)). Anyone can see him: nothing is crafted, needed or given but an advancement.
Primary specialty and supported player role: play and atmosphere, for every player; it brings the summoned Horseman's fight to players who would never call him.

## Player experience

A few times each Halloween season, every player sees the Headless Horseman ride out of the dark at them:

1. **Hoofbeats in the dark.** On a night in the season, out under the open sky in the Overworld, a cackle and a skeleton horse's cry come out of the dark, and "Hoofbeats in the dark..." shows over the hotbar.
2. **He rides out.** The Horseman, his lantern blazing, bursts out of the dark 26 blocks in front of them (within 70 degrees of where they look, where they can see him if any such ground is in sight) and gallops straight at them. Mist curls up round them as he comes.
3. **He swings round them.** Six blocks off he swerves and gallops half round them.
4. **He flings a pumpkin.** Halfway round, cackling, he flings a flaming pumpkin at their feet. It bursts in fire and smoke, but it is **harmless**: whoever is within 2 blocks is blinded for a moment (1.5 seconds) and thrown back a little. It hurts nobody, burns nobody and breaks or lights nothing.
5. **He rides off.** He gallops away from them and is gone in a puff of smoke two and a half seconds later, with a last cackle: "The Headless Horseman rides off into the night. You'll see him again before the season is out."

The whole sighting takes about seven seconds. Nobody comes to harm, and he cannot be fought: blows, arrows and spells pass through him, he cannot be pushed and he takes no harm. He rides through trees, fences and walls as the phantom he is. To fight him, call him at midnight with a lit scarecrow head, as before ([halloween-nights.md](halloween-nights.md)).

Everyone within 48 blocks when he flings his pumpkin has seen him too, so a group sees him together. Each of them earns **Sleepy Hollow** (in the Husbandry tab): "See the Headless Horseman ride out of the dark in the Halloween season".

**When he comes:**
- **Your first night of the season**, then again every **2 or 3 days** (the server's dates, as the season's; the gap is fixed for each sighting), and once more on **Halloween** itself, if you have not seen him yet that day. Over the season from 20 October to 3 November, that is about five or six sightings each.
- **On a Blood Moon night** too, if you have not seen him yet that day ([blood-moon.md](blood-moon.md)).
- Only at night (dusk to dawn), in the season, out under the open sky in the Overworld, awake, and once you have been in the game for half a minute. While you are due he comes at an unforeseen moment (about once a minute of being out at night, on average), never with another rider or the summoned Horseman within 128 blocks.
- **Only on nights that pass.** If the server has stopped time (`/gamerule advance_time false`), he never comes on his own.
- He comes only from ground that is loaded and not water, no more than 10 blocks above or below you; if there is none in front of you, he tries again later.

### Settings

In `config/jugcraft.properties`:
- `halloween.horseman_sightings` (`on` or `off`, default `on`): whether players see him at all.
- The season's own settings bound him as every Halloween event is bound: `halloween.start` and `halloween.end` (20 October to 3 November by default), `halloween.timezone` and `halloween.mode` (`auto`, `on` or `off`); `halloween.harvest_moon` is the Halloween day he always comes on.
- The `agriculture` switch off stops him too.

## Connections

- **Input producer:** none needed. He comes to every player in the season.
- **Output consumer:** the advancement Sleepy Hollow. He is the summoned Horseman's herald: a player who has seen him ride knows what midnight with a lit scarecrow head will bring.
- **Technology and magic:** none.
- **Required vs optional:** optional and harmless. Nothing in progression needs a sighting.
- **Trade and solo routes:** everyone sees him, alone or together.

## Balance and automation

- **No reward but an advancement:** a sighting gives no items, experience or candy, so there is nothing to farm.
- **Harmless:** his pumpkin only blinds and pushes back; he deals no damage and starts no fire. A blind moment near a cliff edge is the one risk, as with any knockback.
- **Bounded:** a player sees him at most once a day outside Halloween, and a few times a season.

## Multiplayer and persistence

- **Server authority:** the server decides who is due, when and where he comes, his whole ride, the pumpkin's fright and who has seen him. Clients only draw him (with the summoned Horseman's model) and the particles and sounds the server sends.
- **Bounded work:** every 20 ticks, on passing nights in the season only, a look at each player in the Overworld; when a player is due and the chance comes up, a check for riders within 128 blocks and at most five spots read. A rider's ride reads one column of the heightmap a tick, only from loaded chunks. At most one rider per 128 blocks.
- **Saved state:** each player's last sighting day (an attachment, `jugcraft:horseman_seen`, kept through death). The rider is never saved: a ride cut off by a restart or an unloaded chunk is simply gone, and the player is due again as before.
- **Chunks:** nothing is loaded. He comes only from chunks that are loaded and ticking, and his ride stays within a few dozen blocks of the player.
- **No griefing:** he breaks, lights and takes nothing, and leaves nothing behind.
- **Still to do:** the two-client dedicated-server playtest.

## Dependencies and assets

No new dependency. He is drawn with the summoned Horseman's model, texture and glowing eyes (`HorsemanRenderer` now draws both). The particles (mist, fire, smoke) and sounds (the witch's cackle, the skeleton horse, galloping, the fire charge, the burst) are vanilla's.

New IDs (all under `jugcraft`):
- the entity `horseman_rider` (never saved);
- the player attachment `horseman_seen`;
- the advancement `sleepy_hollow`;
- the config option `halloween.horseman_sightings`.

The flaming pumpkin (`flaming_pumpkin`) gains a saved flag, `harmless`; pumpkins saved before this change load as the harmful kind they were. Nothing is renamed.

## Verification

Run locally (27 October 2026):

| Check | Result |
| --- | --- |
| `python3 tools/check_mod_data.py`'s agriculture checks (`check_agriculture`, which now also checks the sightings in `check_night`) | Pass |
| Deliberate breaks of the Java, the table and the config, one at a time (below) | 9 of 9 failed it |
| `python3 tools/generate_material_data.py`, then `git status` | Writes this feature's words, advancement and resource list only (the files it rewrote for other features were put back) |
| `javac` on the new and changed Java and the test classes, for syntax only | No syntax errors |
| `./gradlew build`, game tests and client game tests | Not run (the Fabric Maven is out of reach here); to be run by CI |

The audit now checks:
- that the rider's and the director's numbers, and the pumpkin's fright, are `tools/agriculture.py`'s `HORSEMAN_SIGHTINGS`;
- that the table makes sense: the gaps in order, the pumpkin flung while he circles, his whole ride within its limit, his start inside the witness range, and that inside the one-at-a-time range;
- that he comes only on passing Halloween nights when switched on, never reads a chunk that is not loaded and ticking, flings only harmless pumpkins and takes no harm;
- that he is registered as a never-saved creature, drawn as the Horseman, on by default, with his advancement and his words.

The 9 deliberate breaks: the start chance in the Java; the rider saved; a harmful pumpkin; the rider able to be hurt; no renderer; the option off by default; the pumpkin flung after the circle (table and Java); sightings on a stopped clock; spots read from unloaded chunks.

The game tests (`HorsemanSightingsGameTests`):
1. a player is due on their first night, not twice a day, after two or three days, on Halloween once, and again if the clock is set back; both gaps come up;
2. sent at a player in a field-sized arena, he starts 26 blocks in front of them, nothing hurts him and he cannot be struck; he comes within 7.5 blocks, flings his pumpkin and is gone; the player is never hurt, has seen him today and earns Sleepy Hollow;
3. his pumpkin only frightens: a player is blinded and thrown back but not hurt or set alight, and a cow beside them is untouched;
4. everyone within 48 blocks has seen him, and a player farther off has not;
5. a player just come into the game is left to settle, and a spectator is never sent him.

The client test (`NightClientGameTests`) now stops time at midnight (so no rider comes on his own into its other pictures), and ends with a sighting: the Horseman sent riding at the player, photographed as he comes (`jugcraft_horseman_sighting`).

Not run: all of the above game and client tests (CI will run them), the two-client dedicated-server playtest, and play by hand.

## World and event applicability

Only in the Halloween season, only in the Overworld. Ending the season stops him; the advancement stays. His lair, where he lives out of season, is the next piece ([branches/BOSSES.md](../branches/BOSSES.md)).

## Rollout and open questions

- **How often:** every 2 or 3 days, and about once a minute out at night while due. The owner may want him rarer or more often after playing.
- **Next:** his lair and its fight, for the off-season, then the Blood Moon, the Graveyard Rising, the Haunted House contest and costume disguises.
