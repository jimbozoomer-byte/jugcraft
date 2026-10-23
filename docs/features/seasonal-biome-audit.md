# Seasonal biome audit (the newer biomes and the seasons)

Status: implemented on branch `claude/seasonal-biome-audit`. Passes the repository and mod data audits locally; CI (compile, server and client game tests) runs on the pull request. **Not yet played.**
Proposal issue: none. Owner-directed: this closes the MUST in [docs/TODO.md](../TODO.md) to audit seasonal content against every newer biome collection, done first in the Halloween work (owner, 23 October 2026), before the Pumpkin King.
Owner: @jimbozoomer-byte
Target milestone and tier: none; world atmosphere and seasonal content for every tier.
Primary specialty and supported player role: exploration, farming and foraging, seasonal events.

## Player experience
The newer biomes came after the seasons and the fall content. These are the landscape collection (`jugcraft:landscape_*`, [tools/landscape_content.py](../../tools/landscape_content.py)), the Alpine Spawn, the caverns and the lairs. They are in no vanilla or conventional biome tag, so everything that picks biomes by tag passed them over. A landscape maple wood never turned gold or took winter snow; no chestnut tree, heirloom pumpkin or chanterelle grew there; and no werewolf, squirrel or turkey lived there. After this change:

- **Seasons and winter snow.** 58 newer biomes turn with the date: temperate grassland, forests, taiga, hills, cliffs and swamps. Examples are the maple woods, the Pumpkin Patch, the Ominous Woods, the Siberian taiga and the sakura groves. The Alpine Spawn already did, and still does.
  - 54 of the 59 take winter snow with `seasons.snow=on`.
  - The bayou, floodplain, lavender field, Mediterranean forest and redwood forest have mild winters, so they keep the seasons' colours but get no snow.
  - Unchanged all year (as the vanilla ones are): the tropics, the dry lands, the always-frozen lands, waters and shores, caves, the End and the Nether. So are the biomes whose fixed colour is their look: the mystical ones, the Alpha islands, and the four Skylands, each of which is one season already.
- **The fall harvest grows wild there.** Each newer biome now carries the fall and Halloween wild patches of its kinds. There are 19 in all: wild mandrake, the gourds and squashes, cranberries, chestnut trees, the heirloom pumpkins, bottle gourds, mums, the autumn mushrooms and wolfsbane. A landscape maple wood grows chestnut trees, chanterelles and acorn squash; the Pumpkin Patch and Ominous Woods grow mandrake, red kuri pumpkins and jack-o'-lantern mushrooms. The graveyard flowers' wild patches (spider lilies, ghost pipes and the rest) follow too.
- **The fall's creatures go there.**
  - Brown werewolves: 39 newer forests and taigas, the Howling Totem's ground included.
  - Snow werewolves: 22 always-snowy lands.
  - Shadow werewolves: the Pumpkin Patch and the Ominous Woods.
  - Squirrels: the 39 forests and taigas.
  - Turkeys: 64 forests, taigas and plains, the Alpine Spawn included.
  - The Hag Stone: 6 swamps, bogs and marshes.
  - Halloween nights' wisps ([halloween-nights.md](halloween-nights.md)): over the newer swamps, as over vanilla's.
- **No werewolves at the start.** The Alpine Spawn counts as open grassland: it has turkeys and plains pumpkins, but no werewolves.

## Connections
- Existing input producer: world generation (the landscape collection and the Alpine Spawn) and the server's season clock ([seasons.md](seasons.md)).
- Existing output consumer: the seasons' colours and snow. Also the agriculture branch's fall harvest ([festival-crops.md](festival-crops.md), [halloween-harvest.md](halloween-harvest.md), [more-fall-additions.md](more-fall-additions.md), [graveyard-flora.md](graveyard-flora.md)). Also the turkeys ([more-fall-additions.md](more-fall-additions.md)), the werewolves and squirrels ([even-more-fall-additions.md](even-more-fall-additions.md)), the Alpha Werewolf's Howling Totem ([alpha-werewolf.md](alpha-werewolf.md)), and the Mire Hag's Hag Stone ([mire-hag.md](mire-hag.md)).
- Technology connection: none new.
- Magic connection: none new.
- Reachable entry path: the newer biomes are common in Jugcraft regions. Nothing is gated on them; they are more places to find what was already found elsewhere.
- Required vs optional: nothing here is required. Every crop, creature and lair stays where it already was, and these biomes are more of the same.
- The audit, `tools/seasonal_kinds.py`, holds every Jugcraft biome that `tools/biomes.py` does not: 177 of the 278. Each entry records:
  - whether it has four seasons;
  - whether winter rain falls there as snow;
  - its **kinds**, the conventional tags it would be in (`c:is_forest`, `c:is_taiga`, ...).
- The 37 landscape biomes that share a name with one of `tools/biomes.py` take that biome's seasons and tags, so the two read alike. The rest are classified by hand from each biome's climate and what grows there.
- The kinds become 14 tags, `#jugcraft:seasonal_kinds/is_<kind>`. Each holds the newer biomes of that kind, and only the seasonal and fall systems read them:
  - `season/SeasonalKinds` pairs each conventional tag with its kind's tag;
  - `JugcraftAgriculture.fallPatch` places the 19 fall patches through the pairs;
  - the graveyard flowers' patches and the wisps read the pairs too;
  - the generated habitat tags list the kind tags: `werewolf_haunts`, `snow_werewolf_haunts`, `shadow_werewolf_haunts`, `squirrel_habitat`, `turkey_habitat` and `hag_stone_lands`.
- The conventional and vanilla tags are left as they were. Ores, raider camps, the Menagerie's wildlife and the other wild crops read those, and folding the newer biomes into them changes far more than the seasons (see Rollout and open questions).

## Balance and automation
- No recipe, item, drop rate or patch rarity changes. The newer biomes get the same patches, at the same rarity, as the vanilla biomes of their kind, and the same spawn weights.
- More of the world now has the fall's wild crops and creatures. It is the same content over more ground, not a new source.
- Looks (colours and snow) are cosmetic, as in [seasons.md](seasons.md).

## Multiplayer and persistence
- Server authority: world generation, spawning and the season clock are the server's. The kind tags are data that every client and server loads alike.
- Existing worlds:
  - Colours and winter snow change at once in every newer biome that now has seasons. Snow follows the `seasons.snow` rules in [seasons.md](seasons.md): it settles over the winter months and melts in spring.
  - Wild patches and the Hag Stone's placement apply to new chunks only; chunks already generated keep what they have.
  - Creature spawns and the Howling Totem and Hag Stone's ground follow the tags from the first start with this version.
- No block, item or saved data changes. Removing this would only stop new chunks gaining the patches and the newer biomes' seasons; nothing earned is lost.
- Disable behaviour as before: `seasons.mode=off`, `seasons.snow=off` and the agriculture switch work as they did. The kind tags and `SeasonalKinds` are always registered.

## Dependencies and assets
- None new. Fabric API's conventional biome tags (`ConventionalBiomeTags`) and biome modification API, already used.
- No textures or models. The tags are generated by `tools/generate_material_data.py` from `tools/seasonal_kinds.py`.

## Verification
- `tools/check_mod_data.py` (`check_seasonal_kinds`, from `check_seasons`) checks:
  - every Jugcraft biome is in `tools/biomes.py` or the audit, never both, and the audit names no biome that does not exist, so a new biome collection cannot ship unclassified;
  - kinds are known, and winter snow implies seasons;
  - the season tags and the kind tags equal the table;
  - `SeasonalKinds` pairs each conventional tag with its own kind and reads both;
  - all 19 fall patches, and only they, are placed with `fallPatch`, and `fallPatch` reads the pairs;
  - the graveyard flowers' patches and the wisps read the pairs, and each habitat tag lists its kinds.
- The older patch checks parse both `wildPatch` and `fallPatch` calls, so the fall patches keep their biome and gating checks.
- Deliberate breaks, each in a scratch copy of the tree: 24, all caught by the audit. They covered an unclassified biome, a made-up one, a double entry, an unknown kind, snow without seasons, stale season or kind tags, a mispaired or missing Java kind, the selectors reading one tag, a fall patch placed the old way, another crop sent through the kinds, the graveyard flowers or the wisps reading one tag, and each habitat losing its kind.
- Server game tests (`SeasonalBiomeGameTests`):
  - `newerBiomesTakeTheSeasonsByTheirKinds`: the maple woods and Pumpkin Patch have seasons, snow and their kinds; the desert canyon and wintry forest have neither seasons nor snow; the canyon has no kind; vanilla biomes still read through the conventional tags; an unpaired tag is refused.
  - `newerBiomesReachTheFallsCreatures`: newer biomes are in the werewolf, squirrel, turkey and Hag Stone tags; the Alpine Spawn is in the turkeys' but not the werewolves'; the desert canyon is in none.

### Results
- Run locally on this branch:
  - `python3 scripts/check_repository.py`: passed.
  - `python3 tools/check_mod_data.py`: passed.
  - `python3 -m unittest discover -s scripts/tests`: passed.
  - The 24 breaks: all caught.
  - `javac` parse (syntax only) of the changed and new Java: passed.
- CI (compile and the server and client game tests): pending on the pull request.
- Not run: the client and a world. Nobody has seen a newer biome turn gold, a chestnut tree in a landscape maple wood, or a werewolf there. The game tests check the tags and kinds, not where features land: the game-test world is not a generated Overworld. A two-client dedicated-server check is also not run; nothing here is per-player.

## World and event applicability
- Halloween and the Witching Season: the wild patches and creatures above keep their own rules (their hours, nights and dates) in the newer biomes, as elsewhere. Nothing new is seasonal-only, and nothing earned depends on an event.
- The Horseman's summoning, trick-or-treating, the Witching Season bosses, December and the Harvest Feast read no biome tag, so they work in the newer biomes as anywhere.
- Town decorations: the town is in the Alpine Spawn, which already had seasons.
- Caves, lairs, caverns and custom dimensions: no seasons (no sky, or a fixed hour), and no kinds.

## Rollout and open questions
- **The owner's choice: the wider tags.** The newer biomes are still in no `minecraft:` or `c:` tag (not even `#minecraft:is_overworld`), which leaves gaps outside the seasons. The Menagerie's wildlife skips them, and so do the other wild crops, the orchards' trees, ores that pick by biome tag, the raider camps, and the Cinder Kiln's volcanic lands (the landscape volcanoes). Villages and mineshafts would need `has_structure` tags. Folding the kinds into the conventional tags would fix all of these at once, but it changes spawn, ore and structure placement across the whole world and needs its own review. This audit changes only the seasonal and fall systems.
- The classification is a judgement from each biome's climate and plants: `tools/seasonal_kinds.py` is the one place to change it.
