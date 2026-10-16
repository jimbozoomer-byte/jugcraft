# Tree generation batch 7: olive

Status: implementation in progress, owner-authorized on 16 October 2026. Public branch: `codex/tree-generation-batch-7` in `speedygroyper/jugcraft`. Actual tool/model: OpenAI Codex (GPT-6). Based on owner-approved batch 6b (PR #404), including batch 6 (PR #399), with main at `2804ba015`. Later batches are excluded.

## Approved scope

Add a short, low-forking olive with a broad irregular silver-green crown (target 5–7 blocks tall, 6–9 wide), the same tree with a 5% native beehive decorator, a squat ancient olive grown only from four saplings (target 6–8 tall, 9–11 wide), and permanently fruitless wild olive bushes (2–3 tall, about five wide). Measure actual shapes and connected foliage during testing rather than treating placer parameters as dimensions.

Keep the existing placement-attempt budgets. Mediterranean Forest receives 25% olive and 15% olive bushes, preserving cypress, lemon and orange shares and reducing oak. Lavender Field receives 30% olive with bees, 10% oak with bees, 10% cypress and 50% jacaranda; disable its winter snow. Scrubland becomes 60% olive bushes, 25% oak bushes and 15% juniper. No olive generation in Orchard or Xeric Shrubland; no wild ancient olives.

Include the nine-block muted green-gold wood set, furrowed silver-grey bark, renewable saplings, evergreen foliage with unripe green and ripe purple-black fruit, right-click harvest and regrowth, and pickled olives (one Mason Jar, four olives, one cider vinegar) through the existing preserving system. Preserve existing wood processing, stripping, leaf loot and tree-farm integration. Wild bushes must remain fruitless after ticking and save/reload, rather than merely starting unripe. Player-placed leaves and repeated clicks must not yield instant repeat harvests.

The shared plant-oil system is absent at this starting revision, so oil pressing remains deferred. No olive-only fluid or new dependency is introduced. Olives and preserves are optional early-game food and building content, with no new mandatory technology or magic gate. Existing chunks are not rewritten; registered content remains when biome generation is disabled.

## Validation plan

Run the build and server tests, focused seeded shape/clearance/sapling/harvest/preserving regressions, generated-data and tree-mix checks, and a rendered client inspection with save/reopen. Compare olive wood beside bamboo, birch, oak, willow, chestnut, crimson and warped. Provide a matching inspection world and pack with individual trees, harvest demonstrations and untouched natural biome stops. Record actual results and limitations here before delivery; no tests are claimed yet.

## Coordination and provenance

Open upstream PRs and public branches of all four listed forks were checked before implementation; no separate olive implementation was identified. The branch continues the owner's approved tree work and remains separate from later batches. The owner-library catalog contains no olive-named asset. Extend existing deterministic natural-texture generators using the approved `#9c9951` palette from owner painting 3; use native texture references for cut/stripped wood without bundling Mojang images. Preserve all supplied originals.
