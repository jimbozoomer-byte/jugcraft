# Seasonal colours

Status: implemented on branch `claude/seasons`. Compiles and passes in-game tests in CI (see Verification). **Not yet played.**
Proposal issue: none. On 1 October 2026 the owner asked for seasonal biomes that change colour with the real date, and chose "Seasonal colours first" on a new branch.
Owner: @jimbozoomer-byte
Target milestone and tier: none; world atmosphere for every tier.
Primary specialty and supported player role: exploration and building (looks only)

## Player experience
- Grass and tree leaves in temperate biomes change colour through the year, following the **server's** date:
  - **Winter** (around January): dull, olive-brown and dormant.
  - **Spring** (March to May): first green, then a fresh, bright green.
  - **Summer** (July): the vanilla colours.
  - **Autumn** (September to November): leaves yellow from September. In late October they turn gold, orange and red in patches about 12 blocks across, with smaller speckles, so neighbouring trees turn different colours. By late November they are russet. Grass goes straw-coloured.
- Colours change a little each day (no sudden jumps) and blend smoothly into neighbouring biomes that have no seasons.
- Biomes with seasons (`#jugcraft:has_seasons`, listed in `tools/seasons.py`):
  - plains, sunflower plains and meadow;
  - forest, flower forest, birch forest, old growth birch forest and dark forest;
  - taiga, old growth pine taiga and old growth spruce taiga;
  - windswept forest, windswept hills and windswept gravelly hills;
  - river.
- Deserts, jungles, savannas, swamps, snowy biomes, oceans, and biomes with fixed colours (cherry grove, pale garden, badlands) stay the same all year.

## Connections
- Input producer: the server's clock. Nothing in the game feeds it.
- Output consumer: none. Seasons are colours only: no item, block, recipe, drop, spawn or progression step depends on them.
- Technology connection: none.
- Magic connection: none yet. A shared season clock (`JugcraftSeasons.today()`) is ready for later seasonal content, such as the Halloween branches, to share.
- Reachable entry path: nothing to unlock; colours show from the first join.
- Required vs optional connections; trade and solo routes: not applicable (cosmetic).
- How this stays useful without other branches: it needs none.
- Cosmetics: works in every Overworld biome in the tag. Other dimensions and modded biomes are unchanged unless a data pack adds them to `#jugcraft:has_seasons`.

## Balance and automation
None: no resources, units, conversions or rewards.

## Multiplayer and persistence
- **Server authority.** The server works out the season day and sends it to each player when they join (`SeasonPayload`, one number). It checks the date once a minute (`JugcraftSeasons.CHECK_TICKS` = 1,200) and tells everyone again when the day changes. The client never reads its own clock.
- **Operator settings** in `config/jugcraft.properties` (read at startup):
  - `seasons.mode`:
    - `auto` (default) follows the date.
    - `spring`, `summer`, `autumn` or `winter` hold that season, for testing and off-season worlds.
    - `off` gives vanilla colours.
  - `seasons.hemisphere`: `north` (default) or `south`. The south is half a year on: October there is spring.
  - `seasons.timezone`: an IANA zone such as `Europe/London`; default `UTC`. It decides when the day turns over.
  - An unreadable value falls back to its default and logs a warning.
- **Client.** A client mixin (`mixin/client/ClientLevelSeasonMixin`) adjusts each biome sample of grass and foliage tints. The level's tint cache keeps the results, so this costs nothing per frame. When the season day changes, the client clears the tint cache and rebuilds the chunk meshes once (about once a day).
- A client without the server's mod, or a server without Jugcraft, simply shows vanilla colours. Leaving a server resets the colours.
- **Nothing is saved.** No world data, block, item or player data is written, so there is nothing to migrate, back up or lose. Switching seasons off returns vanilla colours at once.

## Dependencies and assets
- Fabric API only: networking, lifecycle events and MixinExtras, which Fabric Loader bundles.
- No textures. Colours are code (`season/SeasonPalette`).
- No new dependencies.

## Verification
Results are recorded under "Results" below after CI runs.
- `python3 tools/check_mod_data.py` (offline):
  - the biome tag matches `tools/seasons.py`;
  - palette keyframe days rise within the year;
  - the fixed-mode days lie within the year;
  - the three `seasons.*` options exist.
- Server game tests (`SeasonGameTests`):
  - calendar days, including the northern and southern hemispheres, leap years, and New Year;
  - every day of a leap year is within 1–365;
  - the mode overrides;
  - the time zone deciding the day at midnight;
  - a runtime override is not written to the config file, so a restart goes back to the file;
  - summer is vanilla and off is vanilla;
  - autumn leaves are redder, with alpha kept;
  - no day-to-day jump bigger than 4 levels per colour channel (New Year included);
  - autumn patches include both red and gold;
  - the biome tag includes plains and forest and excludes desert, jungle, swamp, cherry grove and snowy plains.
- Client game test (`SeasonClientGameTests`):
  - a real client joins a flat world;
  - it checks that the client received today's season day on joining;
  - it builds a grove of oaks;
  - for each mode (summer, spring, autumn, winter, off and today's date) it sets the mode on the server, checks that the client's day matches, reads the leaf and grass tints back from the client's level and takes a screenshot `jugcraft_season_<mode>`;
  - it asserts:
    - summer equals off (vanilla);
    - autumn leaves are clearly redder and autumn grass differs;
    - spring and winter differ from summer.
- Not run:
  - a dedicated server with two clients;
  - a server restart across midnight;
  - real biomes in a normal world (the test uses the flat world's plains).

### Results
Not yet run in CI.

## World and event applicability
- **Seasonal rules** ([CONTENT_BRANCHES.md](../CONTENT_BRANCHES.md)):
  - **Activation** and **deactivation**: tested by switching modes, including `off`.
  - **Timezone and manual override**: tested.
  - **Restart across the boundary**: the season is worked out from the date on every check and nothing is stored, so a restart simply works out the current day again. The test covers that overrides do not persist.
  - **Duplicate rewards**: none exist.
  - **Earned content**: none is created, so none can be lost.
- Old chunks are not rewritten: only their colours change while they are drawn.

## Rollout and open questions
- **Default on.** `seasons.mode=auto` is the default, as the owner asked for colours that follow the real date. Set `seasons.mode=off` for vanilla colours.
- **Fixed leaf colours.** Birch, spruce, cherry, mangrove and azalea leaves have fixed colours in vanilla, so they do not change. Their biomes still change grass, and oak and dark oak leaves change.
- **No change to snow, weather or crops.** This is only colour. Autumn-themed biomes (such as the planned Amberwood) and seasonal crops would be separate features.
- **Data packs that change tags** (`/reload`) are picked up the next time tags reach the client.
- A `/jugcraft season` operator command could change the mode without a restart. It is not built: the config file is the override for now.
