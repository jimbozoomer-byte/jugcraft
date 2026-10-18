# Tree batches 11, 11b and 11c

Status: implementation in progress; owner visual approval pending.

Requested by the owner on 18 October 2026. Implemented with OpenAI Codex (GPT-6).
Scope follows `docs/branches/TREES.md`: flame tree, sea grape, osmanthus,
shoreline driftwood, banyan/fig wood, and mango. Pandanus is omitted as recommended.
Batch 12 and later are outside this change.

The public upstream PR list and all five public forks' branch inventories were
checked before starting. No overlapping Batch 11 work was identified. Current
upstream main (`ffc7f7ba`) is already an ancestor of the approved Batch 10 branch.

## Intended behavior

- Flame trees use acacia timber and a low scarlet umbrella crown in Tropics and
  Jacaranda Glade; osmanthus uses olive timber and a small flowering crown in
  Karst Pinnacles. Both have their own leaves and saplings.
- Sea grape replaces the Overgrown Beach oak bushes with low fruiting shrubs.
  Dune Beach and Gravel Beach gain occasional supported horizontal driftwood.
- Banyans replace Shrine Springs' small oak share, retaining great oaks. Their
  wide crowns, narrow prop roots and hanging aerial roots use a new fig wood set.
- Mango replaces Lush Grassland's jungle-tree share and part of its jungle-bush
  share, keeping existing tree attempt counts and palms. It has a new wood set.
- Renewable sea grapes, mangoes and osmanthus flowers use shared harvest and food
  systems: jelly, chutney, fermentation, candy flavour and mooncake filling.

New generation affects new chunks only. Registration remains stable when the
biomes switch is disabled. No new dependencies or platform upgrades are planned.
Final sizes, probabilities, recipes, asset provenance and test evidence will be
recorded here after implementation and inspection-world verification.
