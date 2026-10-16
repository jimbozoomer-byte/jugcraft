# Tree generation batches 5b and 5c: hawthorn and persimmon

Status: implemented on `codex/tree-generation-batch-5bc` in [draft PR #397](https://github.com/jimbozoomer-byte/jugcraft/pull/397); verification and inspection delivery in progress. Authorized by the owner on 16 October 2026 after visual approval of batch 5. Actual tool/model: OpenAI Codex (GPT-6). Builds on owner-approved, unmerged PR #396 and its tree prerequisites. Batch 6 and later are outside this change.

## Design published before implementation

Checked all 42 open upstream PRs and all public branches of upstream (65) and its four forks (45, 12, 8 and 2). No competing hawthorn or persimmon implementation was identified; the tree-roster branch supplies the existing design. This check cannot cover private or unpushed work.

Hawthorn uses the existing fruitwood, its own dense seasonal leaves and one renewable sapling. Its small wind-bent tree uses a native bending trunk (3 + 0–1, bend length 1) and flat blob crown (radius 2, height 2); the hedge form uses one hidden fruitwood log and native bush foliage (radius 2, offset 1, height 2). Saplings choose the bush three times as often as the tree; sheared leaves also allow deliberate hedge layouts. Haws drop from ordinary autumn and winter leaves at 10%, and green leaves at 2%, yielding one each. Persistent player-placed leaves cannot produce haws, and shears/Silk Touch recover foliage without fruit. Haws feed foxes, compost and Cooking Pot Haw Jelly (six haws, two sugar, one Mason Jar, 300 ticks).

Persimmon is an upright tree: native straight trunk 5 + 0–3, tall blob crown radius 2 and height 4. It supplies a full nine-block warm-charcoal wood set, stripping, hand crafting, sawmill and tree-farm routes. It reuses batch 5's seasonal fruit leaves, retaining fruit through autumn and bare winter; exposed supported natural leaves ripen in all calendar modes, including seasons-off, and right-click harvesting yields one or two persimmons and resets only fruit. The fruit is food, compostable and fermentable, feeds a Mason Jar preserve and a Hearth Oven pie. Append its filling to the existing enum so all 26 saved pie/cake filling ordinals remain unchanged.

| Biome | Placement attempts |
| --- | --- |
| Field | Hawthorn bush 55%, hawthorn tree 10%, spruce 30%, ordinary oak 3.5%, oak bush 1.5%; retain 2/3 attempts |
| Shrubland | Hawthorn bush 60%, oak bush 10%, open space 30%; retain 4/6 slots and existing ground vegetation |
| Woodland | Replace four percentage points of oak bush with hawthorn bush; preserve elder 4%, fancy oak 25%, fallen oak 2.025% and ordinary oak 61.475%; oak bush becomes 3.5% |
| Highland | Rare lone hawthorn: one attempt per sixteen chunks, beside the existing rowan's one per eight; retain open terrain |
| Seasonal Forest | Persimmon 10% drawn from ordinary oak, retaining every other selector share and 10/11 attempts |

Shrubland's unassigned 30% remains open rather than introducing a future-batch shrub. These are selector attempts, not guaranteed surviving tree proportions. New chunks only; no terrain rewrite. Existing IDs, feature switches and platform/dependency pins remain intact. New timber recipes follow `biomes`, existing machine routes retain their current switches.

Hawthorn uses the roster's green/ochre palettes, small red haws and connected fruitwood twigs, on days 85/280/325. Persimmon uses glossy green/wine-maroon foliage, five small orange fruit clusters and connected slate twigs. Its approximate leaf-out moves from day 110 to 98 so the shared spring preset at day 105 remains fully green with ±7-day jitter; autumn/bare transitions stay 280/315. Existing calendar and fruit rules are reused without per-tick world scans or new block entities.

The owner asset README, catalog and complete filename inventory were inspected; no hawthorn, haws or persimmon assets were found. New 16×16 assets extend existing deterministic generators and approved palettes, preserving existing seeds and all prior textures. Persimmon planks use the approved warm charcoal #3d3733; logs follow the owner's current native bark/end-reference treatment without copying Minecraft images. Inspect the result alongside dark oak, yew and fruitwood.

## Verification and delivery

All 54 Python tests, repository/link validation and the 18-entry dependency lock check pass. All 162 pre-existing images from the wood/foliage generator remain pixel-identical to public design commit `550129a7606e80887d4859bf9e2492e4c1102714`. The first focused server invocation selected no tests because the registered acronym spelling differs from the filter; the corrected invocation ran eight tests. Seven passed and the shape test identified a mistaken five-block persimmon crown expectation: the specified native radius-2/height-4 blob has a seven-block base. The assertion was corrected; the generator settings were unchanged. Twelve seeds measured hawthorn at 4–5 tall, 6 wide, and its bush at 2–3 tall, 5 wide. The complete build/server stage passed all **1,676 required tests**, including eight new batch-5bc tests. Persimmon measured 6–9 tall and 7 wide across all twelve seeds. The rendered client and full data audit are still in progress.

Verification plan: Verify real sapling/feature shapes, soil and obstruction protection, haws loot and renewable saplings, all fruit/season states, repeated harvesting, persistent-leaf safeguards, timber/food routes and saved pie ordinals. Run applicable server tests, generator/data and repository checks, and the rendered client inspection. Deliver a separate inspection save with natural biome stops, seasonal samples and wood comparisons, plus a matching pack and JAR. No test or owner approval of these batches is claimed yet.
