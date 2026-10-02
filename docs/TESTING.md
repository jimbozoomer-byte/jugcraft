# Testing and release evidence

## Current foundation

Run `python scripts/check_repository.py` with Python 3.11+. The Foundation / repository job checks required files, relative Markdown links, and the phase declaration. It is not a Java compiler, mod test, security audit, or gameplay approval. In the `bootstrap` phase, Java/Gradle sources are allowed. The Build workflow compiles the mod (`./gradlew build`), checks generated JSON is current and runs `tools/check_mod_data.py`, which validates material data and audits recipes offline. None of these is a game test.

## Gameplay PR evidence after bootstrap

Record commit SHA, exact client/server/dependency versions, test world origin, commands, observed results, and test date. Do not commit worlds or logs with player information; attach redacted evidence when needed.

- Build and relevant unit/game tests.
- Dedicated server startup and two independent clients joining.
- Actual survival acquisition path, recipes, progression locks, and automation.
- Concurrent use by two players, permissions, disconnect/reconnect, save and restart.
- Existing systems interacting with the new feature; dependency present/absent for optional integrations.
- Invalid requests, repeated actions, full inventories, interrupted crafts, chunk unloads, and duplicate reward attempts where relevant.
- Before/after performance under a stated workload: machine count, players, hardware, median/p95 tick time and memory observations. Agree per-feature limits before merge; do not claim support for an untested player count.
- Upgrade from the prior released world version and safe feature-disable behavior where persisted content changes.

## Dedicated server and two clients

**Automated (one client):** the client job's `JugcraftServerClientGameTests` starts the game's own dedicated server in-process (Fabric's client gametest API), joins it, opens a machine's screen and a Retro Trader's trades through the network, leaves and joins again, and checks the inventory and world. It also saves, closes and reopens a singleplayer world holding a trader, a machine and a cabinet. It is not `./gradlew runServer`, and it has one client.

**Manual (two clients):** needs two people or two accounts. Record the commit, exact versions and pass/fail for each line, with redacted screenshots or logs.

Setup:
1. `./gradlew build`; the mod is the jar in `build/libs/` without `-sources`.
2. A dedicated server for Minecraft 26.3 with Fabric Loader 0.19.3 (the official installer at https://fabricmc.net/use/server/), with Fabric API 0.161.0+26.3 and the Jugcraft jar in `mods/`. Accept the EULA and start it.
3. Two clients, each a Fabric 26.3 profile with the same Fabric API and Jugcraft jars, signed in as different accounts. Both join.

Checks:
- Both join without a registry or mod-mismatch disconnect, and see each other.
- Machines and power: both see the same machine run; one fills it and the other sees its screen update; both take output; nothing duplicates.
- Ores: a plain pickaxe gives raw ore, Fortune more, Silk Touch the ore block.
- Retro Trader: with a cabinet near an unemployed villager, both trade with the same trader in turn (vanilla lets one trade at a time); buy and use a Pixel Hollows map; the buyback and restock behave.
- Pixel Hollows: `/locate biome jugcraft:pixel_hollows`, both travel there, mine clusters with and without Silk Touch.
- Disconnect and reconnect in the middle of a trade and with a machine screen open.
- Stop and restart the server; both rejoin: inventories, machine contents, trader offers and marked maps are kept.
- Set `pixel_hollows.enabled=false` and `retro_trader.enabled=false` in `config/jugcraft.properties`, restart: existing blocks, caves and traders remain.

## Content-specific scenarios

Use the cases relevant to the feature; do not claim a scenario was run just because it appears here.

- Progression: start each affected specialty without advanced goods from itself; test required/optional connections, trade and documented solo routes.
- Agriculture: growth and yield loops, climate conditions, harvest automation, livestock population and outputs feeding both industry and magic.
- Biomes/dungeons: fresh-seed distribution, sensible transitions, old-chunk behavior, hazard signals, spawn caps and encounter containment.
- Pets/bosses: ownership, concurrent interaction, friendly fire, unload/reload, despawn/recovery, summon permissions and loot eligibility.
- Loot/spells: reward weights, duplicate handling, stacking/cooldowns, ability persistence, permission checks and conversion/healing feedback loops.
- Rockets/portals: outbound and return travel, invalid destinations, disconnect during transfer, destination access, stranded-player recovery and dimension/chunk budgets.
- Seasons: server timezone/manual override, activation/deactivation and restart at the boundary, reward replay prevention, and preservation of earned content and occupied destinations.
  - Alpine Spawn ([features/alpine-spawn.md](features/alpine-spawn.md)):
    - `AlpineGameTests` covers the climate table (no meadow left, the cool plateau's forest and taiga taken, lowland forest and taiga kept), tags and seasons, and the alpine village and its grid.
    - It also covers the larch: needles following each season mode (placed and natural), out-of-date needles catching up together, the gradual turn, a sapling grown in winter coming out bare, and the wood set.
    - `AlpineClientGameTests` creates a real world (seed `jugcraft`) and checks that it starts in Alpine Spawn at an alpine village. It logs the biome's share around the start and across a 16 km square, and takes screenshots, including larches grown in spring, autumn and winter.
    - Not covered: other seeds, a dedicated server's first start, needles changing over real days.
  - Biomes branch ([features/biome-regions.md](features/biome-regions.md), [features/seasonal-forests.md](features/seasonal-forests.md), [features/fields-and-meadows.md](features/fields-and-meadows.md), [features/wetlands.md](features/wetlands.md), [features/warm-and-dry.md](features/warm-and-dry.md), [features/big-trees-and-rainforests.md](features/big-trees-and-rainforests.md), [features/mountains-coasts-and-volcanoes.md](features/mountains-coasts-and-volcanoes.md), [features/wonders-and-caves.md](features/wonders-and-caves.md), [features/nether-biomes.md](features/nether-biomes.md)):
    - `BiomeGameTests` covers the recorded layouts (every rule places its biome in each of its layouts) and their unreachable listings, the regions' share, layouts and seed behaviour, maple, aspen, fir, willow, jacaranda, palm, cypress, redwood, eucalyptus, mahogany and dead trees growing, giant redwoods and mahoganies from four saplings, every seasonal tree's leaves in every season mode, the new woods, and the wild plants (flowers, tall lavender, clover; watergrass, duckweed and cattails; hibiscus and hydrangea; sea oats on sand; glowcaps, glimmerblooms, frost irises and snowpetals), and the Nether and End biomes in their dimensions' biome sources, found near the origin.
    - `BiomeClientGameTests` finds each Jugcraft biome, and vanilla taiga, forest and birch forest, from the start of a real world (seed `jugcraft`), logs the distances (and, for reference, how near vanilla's hot, mangrove, jungle and old-growth climates are), checks that the seasonal leaves generated around each biome are in today's look, and takes screenshots where each biome is on the surface and fills the camera's view.
    - Not covered: other seeds, a dedicated server, region borders in play, seasons over real days.
  - Seasons ([features/seasons.md](features/seasons.md)): `SeasonGameTests` covers the calendar, zones, overrides, palette, biome tags, events (US and Canadian Thanksgiving, December across New Year), the `/jugcraft season` command, and winter snow (lies, never on farmland, water or vanilla snow, melts in spring). `SeasonClientGameTests` switches each mode in a real client, checks the synced day and tints, then winter snow (falling on the client, snowy grass), and saves `jugcraft_season_<mode>` screenshots. Not covered: a dedicated server with two clients, a restart across midnight, a real winter and thaw.

## Release process

Maintainers test the combined candidate, not just individual PRs, on an isolated staging server. Publish a numbered release with checksums, exact requirements, changelog, known issues, and migration notes. Promote only explicitly approved artifacts. Never automatically deploy arbitrary main or PR builds to the viewer server.

Back up world, player data, configs, and exact previous artifacts before upgrading. Test restoration. If migration is not reversible, restoring the matching backup and old artifacts is the rollback; merely reverting a source commit or replacing a JAR may not restore the world.
