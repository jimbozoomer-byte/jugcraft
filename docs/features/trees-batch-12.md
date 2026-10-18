# Tree batch 12: rainforest

Status: implementation in progress on `codex/tree-generation-batch-12`.
Requested by the owner on 18 October 2026; actual tool/model: OpenAI Codex (GPT-6).
Based on the owner-approved Batch 11 branch in PR #418. Batch 13 and later are excluded.

## Scope and coordination

The open upstream PR list and upstream/all five public forks' branch inventories
were checked on 18 October. No overlapping public implementation was identified.
Upstream main `ffc7f7ba` is already included in this branch.

- Add renewable tree ferns to Rainforest (9% of tree attempts), Eucalyptus Forest
  (5%) and Temperate Rainforest (3%), preserving each biome's attempt budget.
- Use the existing banana stem, orchard leaves, pup and harvest route. Place
  bananas in Lush Grassland and reconcile the roster's intended absolute shares
  in Rainforest and Tropics without adding duplicate banana blocks.
- Prototype the roster's tall mahogany with native mangrove roots, checking all
  four trunk columns for support. Use the documented taller, rooted-in-soil
  mahogany fallback if the one-column root placer leaves a floating 2x2 trunk.
- Rubber-tree placement depends on agriculture slice 5, which is still absent
  from current code. Pending the owner's scope clarification, preserve its
  existing placeholder trees rather than invent a second rubber processing line.

## Compatibility and validation plan

New chunks and future sapling growth use the new features; existing chunks are
not rewritten. Existing fruit, timber and recipes retain their IDs. No dependency
or platform change is planned. Ferns are decorative renewable plants with no
plank conversion. Bananas continue feeding existing cooking and fermentation.

Validate generation over varied seeds, survival and obstacle behavior, renewable
acquisition, banana harvests, generated biome shares, and ordinary-world natural
distribution. Export a separate inspection garden with a natural-biome tour,
save/reopen it and package a matching client world. Results are pending.
