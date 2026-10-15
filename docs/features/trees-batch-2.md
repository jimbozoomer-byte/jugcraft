# Trees batch 2: pine approval and warm/mountain tree generation

Status: owner-authorized implementation, 15 October 2026. Public branch `speedygroyper:codex/tree-generation-batch-2`. **The first checkpoint is the pine wood set only. The owner must approve its appearance before any tree shapes, needles, saplings or biome changes are implemented.** Actual tool: OpenAI Codex (GPT-6), directly requested by the owner.

## First checkpoint: pine wood

Match the quality of the existing woods, using the shared nine-block wood family: log, wood, stripped log, stripped wood, planks, stairs, slab, fence and fence gate. Preserve the current log-art convention: Minecraft texture references with Jugcraft tints, never bundled Minecraft images. Compare pine with existing Jugcraft and vanilla woods in an actual inspection world, including end grain, all log axes and a small building example.

The proposed colour is the tree roster's muted warm grey-tan, from owner painting 6, with the existing plank drawing method. Inspect the owner library's pine assets and current woods before selecting the final preview. Sources and any transformations will be recorded below. The initial set has Creative/inspection access and normal processing recipes; natural acquisition waits for the approved tree stage. This is an intentionally incomplete draft, not a survival release.

## Authorized work after pine approval

The owner approved the planning recommendations in this chat:

- Cottonwood: Floodplain 30%, Prairie 10%, Lush River 10%; target 12-18 blocks tall and 11-17 across, using aspen wood and seasonal leaves.
- Japanese maple: Hot Springs 30%; target 4-5 tall and 7-9 across, using seasonal maple.
- Live oak: Subtropics 30%, Bayou 15%; target 6-8 tall and 13-17 across, with sparse existing shroud moss.
- Windswept pine: Shield 25%, Karst Pinnacles 65%, Hot Springs 55%; target 6-9 tall and 7-11 across, with the approved pine wood and new needles/sapling.
- Big jacaranda: Jacaranda Glade and Glimmer Grove 15% each; target 10-14 tall and 9-13 across.
- Mallee: Outback 30%, with desert acacia 70%; target 4-6 tall and 5-7 across, with sand-compatible natural placement.
- Giant eucalyptus: Eucalyptus Forest 8%; target 28-42 tall and 7-9 across with a 2x2 trunk. Full mix: ordinary eucalyptus 47%, big eucalyptus 25%, oak bush 20%, giant 8%. Four eucalyptus saplings grow the giant.
- Preserve existing per-chunk attempt budgets and unrelated tree/fruit selections, including Shield's aspen/larch and Subtropics' citrus. Percentages are shares before placement failures; dimensions are visual targets to measure in generated samples.
- Lush Desert: desert acacia 80%, small palm 20%, plus scattered grass/dirt islands targeting 10-15% visible surface coverage after measurement.
- Flower Isle: apple 15%, oak bush 85%, retaining its separate wild apple patches.
- Existing-tree swaps: cypress 10% in Lavender Field; small palm 10% in Lush Grassland and 15% in Oasis; palm 20% and small palm 20% in Overgrown Beach; replace Mycelial Jungle's ordinary oak with mahogany. Defer Lavender's proposed winter-snow change to the later olive/climate pass.
- Use seeded variation, soil and clearance checks, shared seasons and wood systems. New chunks only; no retrogeneration. The other six variants initially remain natural shapes of existing woods.

Planning reference: [tree roster](../branches/TREES.md). The roster's old current-state table predates batch 1 and its final table includes future batches; neither is to be applied wholesale.

## Validation and delivery

Pending. Record only completed build/data/game tests and actual client screenshots. Deliver a matching preview JAR/pack and a saved pine comparison world for the owner's first checkpoint. World files and build outputs stay out of Git. Tree-generation testing and a natural batch-2 world follow only after pine approval.

## Compatibility and provenance

New saved IDs remain registered when acquisition is disabled. No dependency or platform upgrades, release, merge or server deployment are included. Asset sources, final palette and tested commit will be added with the first checkpoint.
