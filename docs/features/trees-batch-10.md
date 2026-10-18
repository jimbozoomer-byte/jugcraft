# Tree batch 10: baobabs and hanging fruit

Status: implementation in progress, owner-authorized 18 October 2026. Public branch `codex/tree-generation-batch-10` in `speedygroyper/jugcraft`. Built with OpenAI Codex (GPT-6), from approved PR #413 plus main `ffc7f7bae`. Earlier open tree PRs remain dependencies. No batch 11 work is included.

## Scope and player outcome

Implement the hanging-fruit batch in [the tree roster](../branches/TREES.md#batch-10-hanging-fruit): normal and giant baobabs, the nine-block baobab wood set, evergreen leaves, a sand-tolerant sapling, hanging pods, edible fruit and planting seeds; date palms using existing palm timber/fronds and renewable hanging date clusters. Reuse shared wood, sapling, food, harvest and fermentation systems. Existing chunks are not rewritten.

Outback keeps its current attempt budget with desert acacia 50%, mallee 30%, baobab 10% and giant baobab 10%. Oasis becomes palm 50%, small palm 15% and date palm 35%. Dry River fills its reserved 60% with date palms alongside 40% tamarisk, keeping its established one attempt per ten chunks. Lush Savanna remains unchanged; its optional lone giant was not selected.

Normal baobabs use a short straight trunk and spreading crown; four saplings grow a broad two-block giant with a sparse crown. Date palms are taller and straight with drooping fronds. Hanging fruit ripens on random ticks and right-click harvest resets the fruit, preserving the tree. Dates yield 2–4 and use the existing fermentable conversion. Fruit has no core progression gate. Soil, obstruction, harvesting, renewal, native biome generation and save/reopen will be tested before delivery.

## Verification

Implementation and tests pending. Main reconciliation retains the existing separate moss/path regression assertions and both new translation entries. No claim of completed gameplay or owner inspection yet.
