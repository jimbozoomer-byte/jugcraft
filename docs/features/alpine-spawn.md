# Alpine Spawn

Status: in progress on branch `claude/alpine-spawn`, which is stacked on the agriculture pull requests, #53 and #80. Part 1 (the biome, its placement, villages and the world start) is implemented and awaiting CI. Later parts are planned (see Rollout). **Not yet played.**
Proposal issue: none. On 2 October 2026 the owner asked for an "Alpine Spawn" biome:
- a seasonal alpine biome where the world starts;
- "pretty large";
- one that should "encourage or if possible always have villages";
- one that "from the get go offers extra seasonal support in as many ways as we can".

They chose to build it after the seasons upgrade (#80).
Owner: @jimbozoomer-byte
Target milestone and tier: the start of every new world (Discovery).
Primary specialty and supported player role: exploration and settling; every player starts here.

## Player experience
- **New worlds start in Alpine Spawn**: a large, cool alpine meadow on mountain plateaus, with scattered spruces. When a village stands in the biome near the start, the world starts in that village.
- **Villages are common.** Alpine villages use vanilla's taiga village pieces (spruce houses, and with #53 a Retro Game Shop) on their own grid of 16 chunks; vanilla's grid is 34.
- **Seasons from day one.** Its grass and leaves follow the season colours (#80), and winter snow covers it when `seasons.snow` is on. Later parts add its own seasonal life (see Rollout).
- Like vanilla's meadow it has mineshafts, strongholds nearby, trial chambers, abandoned camps and mountain ruined portals. It has donkeys, rabbits and sheep, and foxes as well.

## Connections
- Input producer: world generation; vanilla's cool meadows become Alpine Spawn.
- Output consumer: settling. It is the world's starting area, with villages to trade with (vanilla trades, and the Retro Trader with #53). Everything vanilla's meadow gives is here too: ores, stone and wood.
- Technology connection: none directly; the usual early resources.
- Magic connection: none yet.
- Reachable entry path: it is where the world starts, so nothing is needed to reach it.
- Required vs optional connections; trade and solo routes: villages are optional. Nothing in it is required or unique in part 1.
- How this stays useful without other branches: it needs none.

## Balance and automation
- Nothing new to obtain in part 1.
- Villages are more common inside the biome only. Villagers already exist everywhere, so trading gains no new route.

## Multiplayer and persistence
- **Server authority.** The server places the biome (world generation) and moves the world spawn, once, on a new world's first start (game time 0), before anyone joins. It uses vanilla's `/setworldspawn`, so saving and respawning are vanilla's.
- **The start search.**
  - It finds the nearest Alpine Spawn within 6,400 blocks of the origin, in 64-block steps, and moves inward until the biome reaches 48 blocks on all four sides.
  - It then looks for a village in the biome within 24 chunks.
  - This runs once, at world creation, and only when the Overworld uses vanilla's multi-noise biomes (not superflat or single-biome worlds).
  - If there is no Alpine Spawn in reach, the spawn stays vanilla's (logged).
- **Settings:**
  - `alpine_spawn.enabled=false` stops new Alpine Spawn generation. Cool meadows are vanilla meadows again in new chunks, and the spawn move is skipped. The biome stays registered, so old chunks keep it.
  - `alpine_spawn.start=off` keeps vanilla's spawn but still generates the biome.
- **Existing worlds:**
  - Old chunks are never rewritten. New chunks where cool meadows would have generated become Alpine Spawn, which may leave seams next to old meadow chunks.
  - An existing world's spawn never moves (its game time is past 0).

## Dependencies and assets
- **Hook.** The biome is placed through #53's `mixin/OverworldBiomeBuilderMixin`, which now also wraps the builder's output, because Fabric API cannot place new Overworld biomes.
- **Copied layouts.** The biome's layout, features and mobs follow vanilla 26.3's meadow, and the village uses vanilla's taiga pieces by name. No Mojang file is copied.
- No new dependencies and no textures in part 1.

## Verification
Results are recorded under "Results" below after CI runs.
- `python3 tools/check_mod_data.py` checks:
  - Java's placement and spawn numbers match `tools/alpine.py`;
  - the biome file's climate and its own placed features;
  - the village structure's biome tag and its grid;
  - the feature switch;
  - the start option;
  - the biome's name;
  - that it has seasons.
- Server game tests (`AlpineGameTests`):
  - every cool meadow in the Overworld climate table became Alpine Spawn, while temperate meadows remain (the numbers are logged);
  - the replacement rule on its own;
  - the biome has seasonal colours, winter snow and the mountain tags, and is cooler than a meadow;
  - the alpine village is in `#minecraft:village`, generates in Alpine Spawn, and has a grid tighter than vanilla's.
- `PixelHollowsGameTests.overworldFeatureOrderHasNoCycle` now includes Alpine Spawn's features too.
- Client game test (`AlpineClientGameTests`):
  - creates a real, normally generated world (seed `jugcraft`, structures on);
  - checks that the player starts in Alpine Spawn;
  - logs the nearest village;
  - takes screenshots of the start and of the view from 40 blocks above it.
- Not run:
  - other seeds;
  - how often the search finds no Alpine Spawn;
  - a dedicated server's first start;
  - a two-client test;
  - the biome's share of a whole world;
  - performance of the one-off start search.

### Results
Not yet run in CI.

## World and event applicability
- **Biome fit.** A cool, wet mountain meadow takes vanilla's cool meadow climate, so it borders taiga, forest and the mountain slopes as meadows did.
- **Existing worlds.** Old chunks are untouched; new chunks may form seams with old meadow chunks.
- **Seasons.** The biome is in `#jugcraft:has_seasons` and `#jugcraft:has_winter_snow`, and follows #80's rules: activation, deactivation and nothing earned is lost.

## Rollout and open questions
- **Part 1 (this):** the biome, its placement, alpine villages and the world start.
- **Planned parts, all seasonal:**
  - **Larch trees:** a deciduous conifer, green in spring and summer and gold in autumn. It drops its needles to stand bare in winter, then buds again in spring. It has a larch wood set, built on the agriculture branch's wood-set data.
  - **Seasonal flowers:** crocuses that bloom in spring, edelweiss and gentians in summer. Each is green the rest of the year.
  - **Bilberries:** bushes that ripen in late summer. When seasons are off, they ripen on a timer instead.
  - **Alpine winter:**
    - snow on by default in this biome only;
    - a snow line that comes down in autumn and melts back up the slopes through spring;
    - lakes that freeze in winter and thaw back to water.
- **Open questions:**
  - Should other mods' world presets or data packs that change the Overworld also get the spawn move? Today only the vanilla multi-noise Overworld does.
  - Would a pillager outpost near the start be too harsh? Vanilla's meadows allow outposts, and Alpine Spawn joins `#minecraft:is_mountain`. Villages exclude outposts nearby.
