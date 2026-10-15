# Trees batch 4: snowpetal cherry and shimmer birch

Design note, 15 October 2026, before implementation. Owner-authorized remaining tree roster; actual tool: OpenAI Codex (GPT-6).

## Separate the tree from its timber

`agriculture.TREES` will accept an optional `wood` key and a separate display name. Existing entries default to their own name, preserving their blocks, recipes and seasonal behavior. `trees.SHAPES.tree` will select foliage independently of its log wood. Its default remains the existing wood key. Explicit `log` support is reserved for later special stems; this batch needs only vanilla cherry and birch logs.

Java will register foliage/saplings independently of wood sets. Existing trees explicitly register both. Snowpetal cherry and shimmer birch register only their leaves and saplings, with no invented cherry or birch wood-set IDs. Their timber uses vanilla recipes, tags and processing. Feature switches follow the real timber where Jugcraft owns it; trees on vanilla timber carry their own `biomes` switch. The data audit checks both log and foliage ownership for every placement source and rejects accidental wood-set blocks for trees without a wood set.

The tree farm uses each tree's actual log output and an explicit optional yield, defaulting to the existing six logs. Later one-log shrubs can specify one or two logs. No existing tree's farm output or cost changes. Sapling survival uses the tree's own sapling unless a shape explicitly asks for another ground rule.

## Player outcome

- Snowpetal cherry: native cherry shape and cherry logs, dense near-white blossom with lilac shadows and white falling particles, evergreen. Replaces the 75% vanilla-cherry share in Snowpetal Grove; aspen remains 25%.
- Shimmer birch: vanilla birch logs under mint foliage with sparse pink-lilac clusters. Normal straight trunk 5 (+2), blob radius 2 / height 3; tall trunk 10 (+4), blob radius 2 / height 5. Evergreen. Frostlight Garden 40%, Starlit Wood 45% tall form alongside the retained 30% vanilla super-birch bee trees, Glimmer Grove 20%. Other shares will be converted from target percentages to conditional selector chances.

Both are Discovery-stage exploration/building content. Natural leaves yield their own saplings; replanted trees yield established vanilla timber. There is no new crafting gate, required specialty dependency or fruit tick loop. Failed growth must preserve saplings. Existing chunks remain untouched.

## Validation to perform

Exercise both normal saplings and all three shapes over separated seeds; verify matching logs/leaves, obstacles, unsuitable soil, replanting drops and tree-farm output. Audit generated registry/model/recipe data, exact biome shares, switch coverage and absence of phantom wood sets. Compare existing generated tree outputs to their previous bytes. Run the full build/server suite, focused client inspection, and save/reopen a normal generated world with naturally located biome stops. Record measured dimensions, screenshots and actual limits here before calling the batch complete.
