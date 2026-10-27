# Herobrine

Status: part 1 (encounters) implemented on `claude/herobrine-haunting`, pending a real compile, CI and review. Not merged, not deployed. The schedule is a separate piece of work (below), and until it is installed he appears only on an operator's command.

Owner: jimbozoomer-byte
Target milestone and tier: the Halloween event, any tier
Primary specialty and supported player role: exploration and atmosphere

## The owner's brief (27 October 2026)

- **Behaviour:** a watcher with harmless scares. He appears at a distance, stares, and vanishes when approached, stared at or hit. Now and then he jump-scares from behind with a sound and a moment of darkness. He never damages anyone.
- **Every player sees him, in rotation:** mostly at a distance, gone if a player gets too close.
- **Schedule:** extremely active now. High activity lasts through **3 November inclusive**, then becomes rarer from **4 November 2026, 00:00 America/New_York** (time zone and cutoff configurable). It does not taper straight after Halloween, and he still exists all year.
- **No chat:** he never speaks, and there is no chat impersonation.
- **World signs (later parts):** in unclaimed wilderness only he leaves redstone-torch shrines, small mossy and sand pyramids, cryptic signs, leafless groves and 2×2 tunnels, and rare massive brick pyramids near shorelines.
- **Look:** the classic Herobrine, which is the default player with glowing white eyes. The owner supplied their Planet Minecraft upload as the reference and allowed its pixels to be copied (see "His look" for what was done instead).

## Who builds what

The owner approved a plan shared between this branch and OpenAI Codex/Astra (coordination relayed by the owner on 27 October 2026):

- **Codex: the schedule and per-player progress.** This covers `io.github.jimbozoomer.jugcraft.herobrine.HerobrineSchedule` and `HerobrineScheduleDirector`, with versioned Overworld saved data. It owns the deterministic calendar (event mode through 3 November, quiet mode after), active-time accumulation, priority to unseen and oldest-overdue players, persistence across reconnect and restart, short retries after failed placement, reservations, rate caps, and crediting only confirmed sightings. Proposed pacing: in event mode, a first sighting within 5–10 active minutes, then roughly every 10–20; in quiet mode, a first sighting within 45–90 active minutes, then every 1–3 active hours; close encounters on their own long cooldown. These are targets, not guarantees: when there is nowhere visible to stand, nothing is shown.
- **This branch: everything else.** That is the entity and renderer, the live tick hook, placement, encounter content, terrain marks, structures and the links into the rest of Jugcraft.
- **The boundary is `creatures/herobrine/HerobrineDirector`.** Neither side builds a competing schedule. The installation line (`HerobrineEncounters.setDirector(...)`) is agreed before wiring.

## Part 1, implemented here

### The encounter

- **Distant.** Placement picks one of four variants, falling back to a watcher. Every variant stands on solid ground in a loaded chunk, with a clear line from the player's eyes to his, and turns to face the player.
  - **Watcher:** 28–52 blocks away under the open sky (14–28 underground), 20°–75° off where the player faces. Of 16 tries the best place wins: higher than the player (a ridge), open to the sky (a clearing), and in clear view of the other attentive players within 48 blocks, so friends together share one sighting.
  - **Follower** (a quarter of the time when the player is alone): he starts 18–22 blocks behind and walks the player's own path, keeping that distance. He freezes while on their screen. Every 6 ticks he walks, a faint step sounds for that player only, from 8 blocks behind them towards him (quiet sounds carry 16 blocks). When they stop, he and his steps stop.
  - **Second look** (a quarter of the time): once the player has seen him and then looked away for half a second, he is gone, or half the time standing somewhere else they could see.
  - **Tunnel** (tried first 60% of the time when the player is not under the open sky): he stands at the far end of the longest straight, covered, walkable run, 14–48 blocks, in a direction the player is not facing. The eyes wait down the tunnel behind them.

  He goes when anyone comes within 12 blocks, after the player has looked straight at him for 2 seconds (long enough to know what they saw), after 30 seconds, if he is hit, or if his player leaves. Operators can name a variant: `/jugcraft herobrine sighting <players> [watcher|follower|second_look|tunnel]`.
- **Close.** He stands 1.6 blocks behind the player. Only that player hears a heartbeat and a soul's sigh and gets 3 seconds of Darkness, which dims the screen and does nothing else. He goes once they have turned and looked at him for 0.6 seconds, or after 3 seconds.
- **Never harmful.** No attack, no damage, no loot, no world change. A blow or an arrow meets nothing and he is gone. He has no name tag or boss bar, is never saved with the world (`noSave`), and removes himself.

### Who counts as having seen him

- **Witnesses must be attentive.** A player counts as a witness only when he has been on their screen, unobstructed, within 96 blocks, for 10 ticks (or they hit him), and they are calm, awake and not idle. A camera that merely faces him is not enough. Any player can be a witness, not only the one he came for, so a shared sighting credits everyone who really saw him.
- **Calm** means no menu open, not dying or below 6 health, not hurt by a mob in the last 10 seconds, and not burning, in lava, falling or short of air.
- **Idle** means not having moved or looked round for 90 seconds.
- **Active players.** The tick hook calls a player active when they are in the Overworld, alive, not spectating or asleep, a minute past joining or respawning, and not idle.
- **Close encounters are limited.** Unless forced by an operator, one needs a calm player who has already seen him at a distance since the server started, so nobody's first sight of him is a jump-scare.
- **One encounter per player at a time.**

### The seam (`HerobrineDirector`)

Once a second, with `herobrine.haunting=on`, the hook calls `due(server, candidates)`. Each candidate is `(player, active, calm)`. The hook attempts each returned `Request(player, kind, reservation)` and reports back:

- `placed(request, encounter)`
- `failed(request, failure)`, with `TARGET_GONE`, `TARGET_UNSAFE`, `ALREADY_HAUNTED` or `NO_VISIBLE_SPOT`. Nothing was shown, so nothing should be consumed.
- `seen(request, encounter, player)`, once per player per encounter.
- `ended(request, encounter, outcome, witnesses)`, with `STARED_DOWN`, `APPROACHED`, `HIT`, `EXPIRED`, `TARGET_LOST` or `REMOVED` (including chunk unload).

`HerobrineEncounters.attempt` (any variant of the kind), `attemptVariant` and `attemptAt` (a chosen place) are the placement calls. An operator's forced encounter uses reservation `FORCED` (-1).

### Operators and settings

- `/jugcraft herobrine status` shows whether the haunting is on, encounters under way and players online. It also says when no schedule is installed.
- `/jugcraft herobrine sighting <players> [variant]` and `/jugcraft herobrine scare <players>` force an encounter, reporting where none could be placed.
- `herobrine.haunting` in `config/jugcraft.properties` (`on` by default; `off` stops scheduled encounters, and the commands still work).

### His look

The owner's upload is the default player's skin with white eyes: its palette is the default skin's colour for colour. The repository's licence policy forbids copying or recolouring game textures, so no skin was copied. `HerobrineRenderer` draws the game's own default player texture (`DefaultPlayerSkin.getDefaultTexture()`), read from each player's installed game at run time. Over it goes `textures/entity/herobrine_eyes.png`, drawn by `tools/herobrine.py`: clear except the four eye pixels, white and full-bright, so the eyes glow in the dark. The result looks like the upload. `tools/check_mod_data.py` fails if a `herobrine.png` skin is ever added.

## Tests

Server game tests (`HerobrineGameTests`, in the 48-block arena with padding) cover:

- he vanishes when someone comes close, ending unseen;
- staring him down ends the sighting and reports the player once;
- a blow makes him vanish, hurts nobody and credits the attacker;
- a close encounter stands behind, darkens and never hurts, and goes unseen and uncredited if the player never turns;
- a first close encounter is refused and one encounter runs at a time, with both failures reported;
- only attentive witnesses are credited (a badly hurt player facing him is not);
- a sighting spot is ahead of the player, at the right distance, on solid ground;
- a walled-in player gets `NO_VISIBLE_SPOT` and nothing is placed;
- he is never saved, and players under a minute in are not active;
- a follower walks the player's own path at its distance, and stops when they stop;
- a second look finds him gone or moved;
- a tunnel encounter stands at the far end of a tunnel behind the player;
- a shared sighting credits both players who saw him, and not the one facing away.

Not yet verified: a real compile and CI run, rendering in a client (the eyes layer), and real two-client play. The local structure parser checks syntax only.

## Still to build (owner-approved plan, not implemented)

- **Encounter refinements:** cave entrances as watcher places, and testing the variants in a real client: whether the footsteps are audible, and whether the eyes show down a dark tunnel.
- **World clues (part 2):**
  - leafless natural groves, straight 2×2 tunnels, sand and mossy pyramids, redstone-torch shrines, and cryptic signs ("YOU TOOK THE LONG WAY", "I WAS HERE FIRST", "COUNT THE WINDOWS", "YOU BROUGHT SOMEONE");
  - some linked into short investigations: a sighting across a river leads to a leafless grove, then a torch-lined tunnel, then a sign pointing to a shoreline pyramid.
- **Massive brick pyramids near shorelines (part 3):**
  - explorable authored interiors with modest cosmetic, decor or journal rewards, and no farmable containers;
  - fully checked reserved wilderness sites, built incrementally with revalidation.
- **Terrain safeguards:**
  - unclaimed does not mean untouched;
  - protect claims, towns, unclaimed builds, player-placed vegetation where tracked, containers, machines, valuable blocks, fluids, bedrock and players;
  - bounded work in already-loaded chunks only;
  - event IDs and reversible placement records, where undo only touches blocks still as placed.
- **Jugcraft connections:**
  - a rare Cursed Mirror reflection, through a narrow seam with the Cursed Mirror (https://github.com/jimbozoomer-byte/jugcraft/pull/498);
  - guards briefly looking toward him without targeting or firing;
  - a Styx observatory clue where Styx recognises the phenomenon but refuses to explain it.
