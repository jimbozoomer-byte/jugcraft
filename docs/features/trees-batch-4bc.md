# Tree generation batches 4b and 4c: bald cypress and magnolia

Status: implementation in progress on `codex/tree-generation-batch-4bc`, authorized by the owner on 16 October 2026. Built with OpenAI Codex (GPT-6), on the visually approved batch-4 branch (PR #389, including #388 and #386). No later tree batch is included.

## Player outcome and scope

Bald cypress brings seasonal needles, renewable saplings and its own ashen timber to Bayou, Lush Swamp, Swamp Woods and Webwood. Its tall branching trunk has compact flared roots and an airy crown with occasional shroud moss. Magnolia brings an evergreen, cream-flowered broad crown and its own olive-brown timber to Flower Isle.

Both are accessible by gathering natural wood and saplings, planting and bone meal, with no progression gate. Their timber uses existing building recipes, fuel, crafting and sawmill routes; the existing tree farm provides automation. Feature switches preserve saved registrations when acquisition is disabled. World generation only affects newly generated chunks.

## Approved placement targets

These are shares of placement attempts, not guaranteed standing-tree proportions. Existing attempt counts, climate and ground decoration remain unchanged.

| Biome | Tree mix |
| --- | --- |
| Bayou | bald cypress 60%, willow 25%, live oak 15% |
| Lush Swamp | bald cypress 25%, vine oak 35%, willow 30%, maple 10% |
| Swamp Woods | bald cypress 25%, willow 45%, vine oak 20%, azalea 10% |
| Webwood | bald cypress 15%, willow 57%, dead tree 20%, vine oak 8% |
| Flower Isle | magnolia 55%, oak bush 30%, apple 15% |

## Planned implementation and verification

Reuse the approved timber palettes and existing tree texture generators; inspect the owner asset catalog before creating new assets. Prototype native upward branching and mangrove root placement for the cypress, checking dry soil and water depths through three blocks without damaging surrounding terrain. Keep Webwood dry-ground placement. Magnolia uses a straight trunk and broad oval crown. Add complete wood sets, natural drops, sapling growth, seasonal cypress needles, evergreen magnolia leaves, recipe and tree-farm integration.

Verify deterministic shape variation, rooted support, bounded crowns, obstruction failure, leaf decay, seasonal changes, sapling renewal, recipes, feature switches, and unchanged earlier trees. Run data/repository checks, Python tests, the build and relevant server/client game tests. Deliver a normal inspection world with comparison trees, natural biome stops, matching pack and screenshots, and check save/reopen.

No new tests or gameplay validation have completed yet. No merge, release, server deployment or later-batch work is authorized by this record.
