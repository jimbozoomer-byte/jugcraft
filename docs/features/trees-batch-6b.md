# Tree generation batch 6b: holly

Status: owner-authorized implementation in progress, 16 October 2026. Public branch: `codex/tree-generation-batch-6b` in `speedygroyper/jugcraft`. Actual tool/model: OpenAI Codex (GPT-6). Based on the approved batch 6 branch (PR #399), already reconciled with main after #398. Batch 7 is excluded.

## Scope

Add a renewable holly tree, its nine-block ivory wood set, glossy evergreen leaves, sapling, decorative holly sprigs and Holly Wreath. Use the existing tree, seasonal foliage, wood, recipe, tree-farm and wall-wreath systems. No new dependency or progression gate.

Woodland receives 4% holly from its ordinary oak default, preserving hawthorn, elder, fallen oaks and the remaining 3.5% oak bushes. Dense Forest receives 5% holly from its roughly 7% oak-bush share, preserving the other trees. Counts remain 9–10 and 14–16 attempts respectively (the upper count in 10% of chunks). Soil and space checks can reject attempts.

The intended tree is an upright broadleaf oval, approximately 5–7 blocks high, with a straight trunk, connected dense foliage and seed variation. Use native placers, checking their actual dimensions rather than treating trunk settings as complete tree height. Keep holly separate from the conifer shapes.

Use the approved grey-green ivory wood `#b6baa1`, smooth grey-green bark, deep glossy green foliage and small scarlet berry clusters. Follow the existing deterministic 16×16 art tools and runtime native wood references where appropriate; preserve previous species and the owner library. The owner-library catalog contains no holly-specific assets.

## Sprigs and seasons

Keep the leaves green all year. Reuse the shared seasonal state and [100, 300, 335] schedule for appearance: autumn/winter foliage has scarlet berries, while one in three green-state model choices carries berries, including seasons-off. These berries are decorative, not a new food item.

Right-click supported natural leaves with shears to clip one sprig, damaging the shears once and visibly removing berries from that block. Clipped leaves regrow on random ticks in every calendar mode; repeated immediate use and player-placed leaves yield nothing. Ordinary shearing/Silk Touch still obtains the leaf block without bonus sprigs. Natural decay and renewable sapling drops remain intact.

Craft one Holly Wreath from eight sprigs around a central string. Reuse the Autumn Wreath's wall/door placement, facing and support-loss behavior, with a dark green ring and scarlet clusters. It is permanent decoration, available year-round; mums do not recolor it. Keep the original Autumn Wreath unchanged.

## Verification and delivery

Planned: deterministic generation and old-asset preservation checks; exact biome shares/counts; seeded shape and foliage-connectivity tests; sapling growth/obstructions; calendar modes, shearing, regrowth and duplicate-prevention tests; wood/sawmill/tree-farm recipes; wreath crafting, placement, support loss and save/reopen. Run the full build/server suite and a rendered client inspection covering a tree garden, native wood comparisons, foliage states, decoration and both natural biomes. Deliver the matching mod/pack and a separate cleaned inspection world. Test results and limits will be recorded here before delivery.

Open PR titles and public branches in upstream and all four listed forks were checked before starting. No separate holly implementation was identified; the upstream tree-roster branch is the existing plan. This is not approval to merge or deploy a server world.
