# JEI integration

Status: implemented in source (PR #35); **not yet tried in a client with JEI installed**. The plugin compiles against the JEI API in CI, Concordance Chunk A's information pages (`addAcquisitionInfo`) included (#337's CI on `d4c07bf56` passed every job). JEI is loaded in the default development and test runs (below), but no test opens JEI or checks its pages, so nothing verifies the plugin at runtime yet.
Proposal issue: none. The owner asked for "JEI/EMI recipes" on 30 September 2026. EMI has no build for Minecraft 26.3 yet (checked on its maven), so this is JEI only.
Owner: @jimbozoomer-byte
Target milestone and tier: all tiers (it only displays recipes).
Primary specialty and supported player role: information and quality of life

## Player experience
With [JEI](https://github.com/mezz/JustEnoughItems) installed:
- every Jugcraft processing machine has its own recipe page, with inputs (tags show every matching item), the result, byproducts with their chances, and the time;
- the machine is listed as the "catalyst", so looking up a machine shows what it makes.

Machines covered: crusher, arc furnace, alloy smelter, metal press, wire drawer, circuit assembler, pulverizer, ore washer, sieve, sawmill, coke oven, steel foundry and tree farm.

Crafting-table recipes show in JEI's own crafting page when JEI's recipe sync reaches the client: always in a single-player world, and on a server only if JEI is installed there too. A server without JEI sends none, and JEI falls back to vanilla's own recipes (read in JEI's 31.9 source; the pin is 31.8.0.49).

**Information pages** (JEI's Information tab) cover what no recipe shows: the alchemy ingredients' properties and the mortar, how a Resonant Ring is forged, and, since Concordance Chunk A, where the things only a ritual or a lair boss gives come from (the Adept's Wand, and the Rooted Thallite Foundation (Concordance Chunk C, C-04; not merged; built in CI, the pages not seen in a client), and Vesperine's and Madame Tatterlace's loot; the Yeti King's and the Cinder Tyrant's since Concordance Chunk B), with the default data's chances ([concordance-first-loadout.md](concordance-first-loadout.md)).

**The Mulch Maw's compost** (Concordance Chunk C, C-07; not merged; built in CI, the pages not seen in a client): every item a Mulch Maw eats has an information page with its worth in quarters of a nutrient, the highest of its Maw tags as the Maw reads them. Each tag's worth comes from `recipe_view.json` `garden_compost` (`tools/concordance_ecology.py` `compost_view()`, the worths `Garden.MULCH` gives the tags), and its items from the client's copy of the server's tags, so an item a data pack adds to a Maw tag is shown too; whether JEI registers these pages after the client has the server's tags is not confirmed ([arcane-concordance-ecology.md](arcane-concordance-ecology.md)).

## How it works
- **Optional:** the plugin compiles against JEI's API only (`compileOnly`, maven.blamejared.com). The JEI mod itself is an optional entry of the framework lock, which `build.gradle` loads at runtime in development and test runs unless `-PjugcraftOptionalIntegrations=false` (as CI's "optional integrations absent" job runs). The plugin class is reached only through JEI's `jei_mod_plugin` entrypoint, so Jugcraft runs normally without JEI.
- **Recipes:** these are server data in 26.x and are not sent to clients. The plugin reads `assets/jugcraft/recipe_view.json` instead, which `tools/recipe_view.py` generates from the same tables as the recipe data.
  - A data pack that changes machine recipes on a server is not reflected in JEI; nor is one that changes a lair boss's loot table or a ritual's data (the Adept's Attunement, and Root and Rivet (Concordance Chunk C, C-04; not merged)), which is why those information pages say "with the default data".

## Dependencies and assets
- JEI 31.8.0.49 for 26.3 (MIT): its API at compile time; the mod optional (`distribution/frameworks.lock.json`).
- No JEI code or assets are shipped.

## Verification
- `tools/check_mod_data.py` passes.
- CI compiled the plugin against the JEI API, Concordance Chunk A's `addAcquisitionInfo` included (#337's CI on `d4c07bf56`, every job passed). Concordance Chunk B adds pages only through data (`recipe_view.json` rows and their words); the plugin is unchanged. Concordance Chunk C (C-07) adds `addCompostInfo`; it compiled in CI with unit 4 (run 38726242149, every job passed) and again in run 38774859294. No client test opens JEI, so its pages are not seen.
- `check_acquisition` (`tools/check_mod_data.py`) ties the information pages to `tools/acquisition.py` and the data on disk, and since Concordance Chunk B each boss's pages to its loot class's pools, first-victory trophy and payment through `LairRewards`. It reads data, and only the loot class's and the boss package's reward and deed lines; other drops coded in Java are not seen ([concordance-first-loadout.md](concordance-first-loadout.md), Known limits).
- Not run: a client with JEI installed. Nothing is play-tested.

## Rollout and open questions
- Add EMI when it publishes a 26.3 build.
- Syncing server recipes to JEI (so data packs show) would need a small packet.
