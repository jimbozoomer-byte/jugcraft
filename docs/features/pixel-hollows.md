# Pixel Hollows

Status: implemented in source; **not yet played**. Compiles in CI; game tests cover it, and the client test screenshots its blocks and a cave lined by its worldgen feature.
Proposal issue: none. The owner supplied the proposal ("Pixel Hollows: a retro-electronics cave biome (first slice)") and asked for it to be implemented on 1 October 2026.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery → Workshops. Iron tools and a light source are enough.
Primary specialty and supported player role: dangerous underground / exploration; supports cozy building.

## Player experience
Deep under dry land the player breaks into a cave that looks like the inside of an old games console.
- The walls are **circuitstone**: dark slate streaked with copper traces, pads and vias.
- **Pixel crystal clusters** grow in scattered patches on the walls, floor and ceiling: blocky, square-faceted crystals that glow faintly, like dead pixels on an old screen.
- The cave hums: an original, quiet chiptune drone, the odd bleep, and the usual cave mood sounds. The fog is a dim violet.
- It holds **more copper and redstone** than ordinary caves (and more tin while tin is enabled).
- Builders get a palette: **circuitstone**, **polished circuitstone** (a little chip on two traces), **circuitstone bricks** and the **pixel lamp**, a full-light block of coloured LED pixels.
- It is still a cave: crystal light is sparse, so the dark pockets between patches spawn the usual hostile mobs.

The theme is original: no real consoles, games, brands or characters.

## Connections
- Existing input producer: world generation. Nothing is consumed.
- Existing output consumer:
  - Factory engineering: the extra copper and redstone (and tin, for bronze) feed the early machines and circuits.
  - Cozy building: the circuitstone set and pixel lamps.
  - The [Retro Trader](retro-trader.md) buys shards back and sells maps to the cave.
- Technology connection: copper, redstone and tin for machines; circuitstone runs on the stonecutter like any stone.
- Magic connection: none yet. Pixel shards could later become an inscription or illusion reagent; that needs its own proposal.
- Reachable entry path (prove no circular unlock): vanilla mining finds the cave, and an iron (even wooden) pickaxe mines everything in it. No Jugcraft item is needed to enter, harvest or craft its blocks.
- Which connections are required vs optional; trade and solo routes:

| Connection | Status |
| --- | --- |
| Vanilla copper and redstone | Required, already exist |
| Tin (`tin` switch) | Optional: the tin bonus is added only while tin is enabled |
| The Retro Trader's map | Optional: the cave can be found by exploring |
| Trade | Optional: explorers can sell shards, ore and decor to builders and engineers |
| Solo route | Fully soloable; nothing here gates core progression |

- How this specialty stays useful without mastering every other branch: it is a mining destination and a decor source; nothing else requires it.

## Balance and automation
- **Where:** a cave biome in the Overworld's climate table (`PixelHollows.PARAMETERS`): the driest land (humidity −1.0 to −0.6), coast or inland (continentalness −0.11 to 1.0), at depth 0.3 to 0.9, which is about 40 to 115 blocks below the surface. On ordinary terrain that is mostly below Y 32 and in the deepslate band. The game test `pixelHollowsDistribution` measures its share against the dripstone and lush caves on ten seeds; see Verification.
- **Ore bonus:** the biome lists vanilla's copper and redstone features, plus extra attempts of the same vanilla features with half the vanilla count and the same height spread, filtered to the biome (`pixel_hollows_copper`: 8 × `ore_copper_small`, trapezoid −16..112; `pixel_hollows_redstone`: 2 × `ore_redstone`, uniform −64..15; `pixel_hollows_redstone_lower`: 4, trapezoid −96..−32). Inside the biome that is 1.5× the usual average. Tin likewise (`pixel_hollows_tin`: 4 × `jugcraft:ore_tin`, trapezoid −32..96) while `tin` is on. No new ore.
- **Other vanilla features kept:** lava lakes, amethyst geodes, monster rooms, the vanilla ores (coal, iron, gold, redstone, diamond, lapis, copper) and water and lava springs, in vanilla's order. Left out on purpose: stone-variety blobs (the walls are circuitstone), glow lichen (the only light is the crystals) and surface plants.
- **Lining:** once per chunk, the feature `jugcraft:pixel_hollows_lining` turns every stone or deepslate face open to air or water inside the biome into circuitstone. Ores are left alone, so exposed ore still shows. About one 4×4×4 cell in 7 of the lining is a crystal patch, where one open face in 4 grows a cluster pointing away from the wall.
- **Pixel crystal clusters:** static: no growth, no random ticks, no block entity. Light 3. They drop 1–2 pixel shards (Fortune adds up to one per level); Silk Touch takes the cluster. Clusters are finite, so shards are finite.
- **Recipes (data-driven):**
  - Stonecutter: circuitstone → polished circuitstone or circuitstone bricks, and polished → bricks, 1:1.
  - Crafting: 4 circuitstone → 4 polished; 4 polished → 4 bricks (like deepslate).
  - 4 pixel shards + 1 glass → 1 pixel lamp.
  - Nothing makes pixel shards; the data checker fails any recipe that does.
- **Automation:** only what vanilla mining allows. There are no machines or conversions here, so no power or essence loops.

## Multiplayer and persistence
- Server-side only: world generation, drops and recipes. No custom packets; clients get models, textures and sounds.
- Ordinary blocks: no ownership or concurrent-use rules needed.
- **Stable IDs:** `jugcraft:pixel_hollows` (biome), `jugcraft:circuitstone`, `jugcraft:polished_circuitstone`, `jugcraft:circuitstone_bricks`, `jugcraft:pixel_crystal_cluster`, `jugcraft:pixel_shard`, `jugcraft:pixel_lamp`; worldgen `jugcraft:pixel_hollows_lining`, `jugcraft:pixel_hollows_copper`, `…_redstone`, `…_redstone_lower`, `…_tin`.
- **Existing worlds:** the biome appears only in newly generated chunks. Generated chunks are never rewritten, so where old and new chunks meet underground a cave can end abruptly at the old chunk's edge.
- **Disable switch:** `pixel_hollows.enabled=false` in `config/jugcraft.properties` stops new generation (the biome leaves the climate table and the tin bonus is not added) and its recipes. The blocks, items and the biome stay registered, so builds and generated caves survive. (The proposal kept the recipes on when disabled; Jugcraft's rule is that a switch stops acquisition, recipes included.)

## Dependencies and assets
- No new dependency. Fabric API places features but has no way to add an Overworld biome, so one small mixin, `mixin/OverworldBiomeBuilderMixin`, appends the biome's climate entry after vanilla's (`jugcraft.mixins.json`). No TerraBlender or Biolith.
- Original assets: textures drawn by `tools/pixel_hollows_textures.py` and models built in `tools/retro_models.py`; the ambient loop synthesised by `tools/pixel_hollows_sound.py` (written to an Ogg file with ffmpeg). The bleeps reuse the game's own note-block "bit" sound by name in `sounds.json`; no Mojang file is copied. MIT.

## Verification
Results are recorded in the PR. The checks:
- `python3 tools/check_mod_data.py`: IDs, assets, loot, tags, the biome's feature references, and that no recipe makes shards.
- Game tests (`PixelHollowsGameTests`, run by `./gradlew build` on a headless server):
  - `pixelCrystalClusterDrops`: 1–2 shards, Silk Touch takes the cluster, Fortune III at most 5.
  - `pixelHollowsBlocksDropThemselves`: circuitstone needs a pickaxe and drops itself; lamp light 15; cluster light 3 and no random ticks.
  - `pixelHollowsRecipes`: lamp (4 shards + glass → 1), cabinet, stonecutting.
  - `pixelHollowsJoinsTheOverworld`: the biome is registered and in the Overworld's climate table.
  - `overworldFeatureOrderHasNoCycle`: every Overworld biome's features sort into one order (a mistake here would crash world generation).
  - `pixelHollowsDistribution`: on ten fixed seeds, the biome's share of 21,125 underground samples per seed against the dripstone and lush caves, and the map search from the origin with its time. It logs lines starting `[pixel-hollows]`.
- Client test screenshots: `jugcraft_pixel_hollows_blocks` and `jugcraft_pixel_hollows_cave` (a carved cavity whose biome is set to the Pixel Hollows, lined by the real `pixel_hollows_lining` feature).
- **Not run:** a survival playthrough; finding the biome in naturally generated terrain in a client; the old-chunk upgrade test; hostile-spawn checks in dark pockets; a dedicated server with two clients; chunk-generation timing with and without the feature.

## World and event applicability
- Biome fit: a deep cave under dry land; the surface above is desert, savanna, badlands or dry plains, so the cave has no surface tell. It is not under oceans.
- Danger: hostile mobs spawn as in other caves (the biome's monster list is vanilla's cave list); the crystals light only small patches.
- Not seasonal; no pets, bosses or dimensions.

## Rollout and open questions
- The lining checks each 4×4×4 biome cell and the open faces in it. Its cost per chunk has not been measured.
- The climate numbers are a first guess, tuned by the distribution test; they may need adjusting once someone has played it.
- A magic use for pixel shards, hostile cave creatures, an abandoned-arcade ruin and a boss are later proposals.
