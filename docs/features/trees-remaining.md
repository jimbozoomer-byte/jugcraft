# Remaining tree roster

Owner authorization, 15 October 2026: after completing tree-generation batch 2, implement all the other trees. Actual tool: OpenAI Codex (GPT-6).

**Scope update, 15 October:** the owner subsequently instructed: "Finish batch 3 and don't start the next batch." Complete yew only. The batch-4 design note was published before that instruction; no batch-4 implementation was started. All later stages below remain unstarted and require renewed owner direction.

The owner approved the new wood palettes on 6 October, and the original pine textures on 15 October. Follow the current runtime vanilla-log treatment for other woods and preserve pine originals. The roster defaults apply unless later code or owner instructions supersede them. Existing orchard, banana and rubber content must be extended without duplicate IDs or replacing their existing routes.

Implementation follows [the roster](../branches/TREES.md) in dependency order. Each wood remains a focused change with its own feature record and review evidence. Batch 2 is kept separate in PR #386. New tree shapes and biomes are for new chunks; no terrain rewriting, release, merge or server deployment is included.

| Stage | Scope | Status |
|---|---|---|
| 3 | Yew and ancient yew, leaf drawing options, Gloomweald canopy | Implemented/tested; owner inspection next |
| 4 | Tree/wood split; snowpetal cherry and shimmer birch | Pending |
| 4b-4c | Bald cypress roots; magnolia | Pending |
| 5-5c | Seasonal fruit; rowan, elder, hawthorn, persimmon | Pending |
| 6-6b | Juniper, gorse, sagebrush, holly | Pending |
| 7-8c | Olive, stone pine, wattle, tamarisk, Joshua tree | Pending |
| 9-9b | Giant fungi, bramble, dragonblood | Pending |
| 10 | Baobab and date palm with hanging fruit | Pending |
| 11-11c | Warm/coastal trees, driftwood, banyan, mango | Pending |
| 12 | Existing banana/rubber integration, tree fern, buttressed mahogany | Pending |
| 13-14 | Walnut, bristlecone, existing orchard wood/biome updates | Pending |

Publish short shared-system notes before the tree/wood split and seasonal-fruit changes. Test growth, soil, obstacles, drops/replanting, seasons/fruit where relevant, and real new-world distribution. Keep inspection-world evidence distinct from artificial sample plots. Record any deliberate differences from the planning document and actual test limits.
