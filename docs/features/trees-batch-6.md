# Tree generation batch 6: evergreen scrub

Status: owner-authorized implementation in progress, 16 October 2026. Public branch: `codex/tree-generation-batch-6` in `speedygroyper/jugcraft`. Actual tool/model: OpenAI Codex (GPT-6). Based on the owner-approved tree stack through batch 5c, including the hawthorn harvest and recipe-view corrections; upstream integration PR #398 is still open. Batches 6b and 7 are excluded.

## Scope and acquisition

Add juniper trees (crooked, column and bush forms), flowering gorse and low sagebrush to the existing dry/open biomes. Juniper receives one nine-block wood set, evergreen needles, a renewable sapling and right-clickable berries. Gorse shares oak timber; sagebrush shares deadwood. All three have renewable foliage and saplings. Ordinary discovery and planting remain sufficient; no progression gate or new dependency is introduced.

Use the owner's approved muted red-violet juniper timber and grey-blue needles, muted yellow gorse over dark green, and silvery sagebrush. Extend the existing deterministic texture tools and runtime native log references. Preserve every prior species' textures and the owner library. No matching juniper, gorse or sagebrush files were found in the library inventory.

## Accepted placement plan

Percentages are absolute shares of placement attempts. Preserve existing attempt counts, except Steppe receives one attempt in 90% of chunks and two in 10%. Failed soil/space checks reduce actual tree counts.

| Biome | Batch 6 change |
| --- | --- |
| Canyon | 35% juniper, 25% juniper bush, 40% existing pine |
| Dryland | 30% juniper, 25% juniper bush, 45% existing pine |
| Xeric Shrubland | 25% juniper, 75% desert acacia |
| Dead Forest | Replace the approximately 10% oak share with juniper |
| Scrubland | 15% juniper, 85% oak bushes |
| Cold Desert | One lone-juniper attempt per six chunks, coarse dirt only |
| Field | Replace the 30% spruce share with column juniper |
| Prairie | 15% column juniper from oak bushes |
| Heathland | 60% gorse, 15% juniper bush, 25% existing pine |
| Shrubland | Fill the 30% reserved share with 20% gorse and 10% juniper bush |
| Steppe | 80% sagebrush, 20% juniper bush; exclude independent wild apple/peach patches |
| Tundra | 10% juniper bush from maple bushes |

Juniper sapling planting and generated-tree survival must agree on the supported dry soils. Steppe stays at most two blocks tall. Alpine Spawn's juniper share remains reserved for batch 8.

## Harvest and existing systems

Juniper berries use real empty/unripe/ripe states, side exposure for low bushes, one or two berries per harvest and natural regrowth, including seasons-off. Player-placed leaves do not regrow fruit; shears and Silk Touch do not duplicate it. Berries feed foxes and composters and substitute for sweet berries in an additional Mulling Spices recipe. Gorse flowers support bees. Sagebrush deliberately makes its single deadwood stem renewable.

Include wood crafting, stripping, tags, fuel, sawmill and appropriate tree-farm routes, with generated recipe-viewer entries. New chunks receive the biome changes; existing chunks and stable IDs remain intact, and disabling generation preserves registrations.

## Verification and delivery

Pending: generated-data and recipe audit, focused Python tests, complete build/server checks, rendered tree/biome inspection and save/reopen checks. Deliver a separate inspection save, matching mod and pack with a labelled garden, fruit/wood comparisons and natural biome stops. No test result or completed preview is claimed yet.
