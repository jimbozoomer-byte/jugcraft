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

Rowan uses native forking trunks (4 + 0–2 + 0–1) and an airy random-spread crown (radius 2, height 3, 40 attempts). Elder uses a single hidden stem and native bush foliage (radius 3, offset 1, height 2). Placement shares are attempts, not promised standing-tree proportions. Only new chunks change.

`SeasonalFruitLeavesBlock` composes the existing seasonal-leaf behavior with the established fruit property and harvest rules. Season changes preserve fruit stage, including ripe berries on bare winter twigs. Natural, supported leaves with air below ripen 0 → 1 → 2 on random ticks in every season, including seasons-off. Player-placed leaves never produce fruit; harvesting resets only fruit. Leaf decay, shears, Silk Touch, saplings and feature switches retain existing semantics. No per-tick world scans or new block entities.

Use one deterministic fruit placement across all three seasonal looks, with three fruit stages per look. Rowan follows roughly days 100/255/305; elder 88/283/328. Rowan berries yield a small bitter snack, Mason Jar jelly, the existing berry-candy flavour and Warding candle scent. Elderberries provide a snack, purple dye, berry-candy flavour and an existing witch-cauldron floater. These use current recipes and tags without new progression gates.

Fruitwood uses the roster's approved `#ab7d76` palette and the current native bark/reference approach. New foliage uses the established generators and connected bare twigs. Preserve the owner-approved pine and every other existing texture. Inspect the shared owner library before adding art.

## Verification plan

Exercise growth, shapes and obstruction/soil protection; all season/fruit combinations; season catch-up without fruit loss; persistent and unsupported leaves; repeat harvest, no duplicate harvest, decay and renewal; wood/food recipes and existing consumers; data and dependency checks. Render trees, timber, seasonal fruit and natural biome stops in a saved inspection world, then reopen it. Record actual results here before delivery. No tests have yet been run for batch 5.
