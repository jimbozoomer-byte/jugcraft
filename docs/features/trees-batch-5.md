# Tree generation batch 5: rowan, elder and seasonal fruit

Status: in progress on `codex/tree-generation-batch-5`, authorized by the owner on 16 October 2026. OpenAI Codex (GPT-6). Builds on owner-approved PR #395 and its tree-batch prerequisites. The 41 open upstream PRs and public branches of upstream plus all four public forks were checked: no rowan, elder or seasonal-fruit implementation was identified. The existing tree-roster branch is the design source.

## Design published before implementation

Rowan brings the shared dusty-pink fruitwood set, seasonal leaves, renewable saplings and scarlet berries. Elder is a broad shrub with one hidden vanilla oak stem, its own seasonal leaves/sapling, cream flower clusters and dark berries. Batch 5b (hawthorn), 5c (persimmon) and later trees are excluded.

| Location | Placement target |
| --- | --- |
| Snowy Forest | Replace the remaining 36% vanilla-oak attempts with rowan; retain fir 30%, aspen 20%, maple 14% and 7/8 attempts |
| Lake District | Rowan 10%, oak 44%, fir 30%, larch 10%, fancy oak 6%; retain existing density |
| Highland | One rowan attempt per eight chunks on average, followed by its sapling survival check; no normal forest layer |
| Woodland | Elder 4% of existing attempts, drawn from the oak share; retain canopy density and other trees |
| Wetland | Elder 10% replacing the remaining spruce; retain swamp cedar 50%, willow 40% and density |

Rowan uses native forking trunks (4 + 0–2 + 0–1) and an airy random-spread crown (radius 2, height 3, 40 attempts). Elder uses a single hidden stem and native bush foliage (radius 3, offset 1, height 2). A bounded `fruiting_shrub` decorator removes four of that tree's own bottom-row leaf blocks, two blocks out from the stem, while preserving its soil and hidden stem. The initial unbroken bush had no exposed underside at all, so it could not ripen berries on flat ground; these four openings give middle-row leaves air below without weakening the shared fruit rules. It never removes foreign blocks or foliage outside the generated tree. Placement shares are attempts, not promised standing-tree proportions. Only new chunks change.

`SeasonalFruitLeavesBlock` composes the existing seasonal-leaf behavior with the established fruit property and harvest rules. Season changes preserve fruit stage, including ripe berries on bare winter twigs. Natural, supported leaves with air below ripen 0 → 1 → 2 on random ticks in every season, including seasons-off. Player-placed leaves never produce fruit; harvesting resets only fruit. Leaf decay, shears, Silk Touch, saplings and feature switches retain existing semantics. No per-tick world scans or new block entities.

Use one deterministic fruit placement across all three seasonal looks, with three fruit stages per look. Rowan follows days 98/255/305; elder 88/283/328. The roster's approximate rowan leaf-out day 100 moves two days earlier so the spring preset (day 105) is fully green even with the existing ±7-day position jitter. Rowan berries yield a small bitter snack, Mason Jar jelly, the existing berry-candy flavour and Warding candle scent. Elderberries provide a snack, purple dye, berry-candy flavour and an existing witch-cauldron floater. These use current recipes and tags without new progression gates.

Fruitwood uses the roster's approved `#ab7d76` palette and the current native bark/reference approach. New foliage uses the established generators and connected bare twigs. Preserve the owner-approved pine and every other existing texture. Inspect the shared owner library before adding art.

## Acquisition, uses and limits

Both trees are early-game, optional resources. Find them in new chunks, collect saplings from ordinary leaf drops and replant on normal sapling soil. Obstruction and unsuitable soil reject growth without destroying the sapling or obstacle. Rowan supplies fruitwood; elder keeps its one hidden vanilla oak stem. Existing chunks and IDs are preserved, and turning off `biomes.enabled` stops new Jugcraft biome generation without removing saved content. Fruitwood's crafting/sawing switch is `biomes`; the separate tree-farm routes retain the existing machines switch.

| Producer/input | Output/consumer |
| --- | --- |
| Rowan log/wood, including stripped forms | Four fruitwood planks by hand or six at the existing sawmill; the complete nine-block wood set uses existing recipes and tags |
| Rowan or elder sapling in the tree farm | Six logs of its existing timber and the same renewable sapling, through the shared 400-tick route |
| Supported natural leaf with air below | One fruit stage per ten random ticks on average; two stages to ripen, yielding one or two berries |
| Rowan berries | Small snack (1 hunger, 0.1 saturation modifier); berry candy; Warding candle scent |
| Six rowan berries, two sugar, one Mason Jar | Rowan jelly in the existing heated Cooking Pot, 300 ticks; existing serving and reusable-container behavior |
| Elderberries | Snack (2 hunger, 0.2 saturation modifier); one purple dye each; berry candy; existing witch-cauldron floater |

Fruit is never accelerated with bone meal; it renews with random ticks in every season. Existing medium compost values are reused for berries. Seasonal leaves continue random ticking while ripe or player-placed so they can change appearance; each tick has constant fruit work and a bounded catch-up of at most 128 connected leaves when the season changes. Harvest and growth remain server-authoritative. The shared fruit class also preserves the existing companion harvest integration through inheritance; no new companion behavior is added.

## Assets and shared systems

The owner library README and complete catalog were inspected for rowan, elder and fruitwood; no matching supplied asset was found. New 16×16 images come from the existing deterministic wood/foliage generators, the owner's approved fruitwood palette, and the roster's foliage ramps. Fruit clusters use separate species seeds and wrap at the texture edges, but keep their positions through the three seasonal looks. Bare foliage reuses the owner-approved connected twig generator with each tree's own palette. The native log reference treatment supplies fruitwood's bark and ends; no Mojang image is copied. All 135 pre-existing generated wood/foliage images compare pixel-for-pixel with announcement commit `558dd0136`.

No dependency or platform pins change: Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3 and the existing 18-entry companion pack lock. This feature uses native tree placers, existing seasonal and fruit rules, Mason Jars, Cooking Pot, sawmill, tree farm, candy/scents/floater tags and the established inspection-world harness. It does not add a separate renderer, inventory, progression gate or block entity.

## Verification plan

Exercise growth, shapes and obstruction/soil protection; all season/fruit combinations; season catch-up without fruit loss; persistent and unsupported leaves; repeat harvest, no duplicate harvest, decay and renewal; wood/food recipes and existing consumers; data and dependency checks. Render trees, timber, seasonal fruit and natural biome stops in a saved inspection world, then reopen it. Record actual results here before delivery. Final results are being recorded after the gameplay and rendered-client runs.
