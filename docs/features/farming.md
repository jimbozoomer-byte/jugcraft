# Farming: harvesters, sprinklers and cotton

Status: in progress (batch 9)
Proposal issue: owner request, 1 October 2026 ("merge it and start the next batch"), following suggestion 6 from batch 5 ("Farming: greenhouses, a powered harvester and sprinklers that use fertilizer, plus new crops")
Owner: jimbozoomer-byte
Target milestone and tier: steel tier, beside the chemistry line's fertilizer
Primary specialty and supported player role: farming and automation

## Plan

| # | Commit | What it adds |
| --- | --- | --- |
| 46 | Crop harvester | A two-block gantry that harvests and replants ripe crops in the 9×9 field in front of it. |
| 47 | Sprinkler | Pipe-fed water that speeds the crops around it, and spreads fertilizer from its slot. |
| 48 | Cotton | A new crop: seeds from sieving coarse dirt, cotton into string. |
| 49 | Docs and PR | Advancements, handbook, docs. |

Greenhouses and rubber trees are left for later: rubber trees need worldgen, and a greenhouse needs its own climate rules.

## Player experience

### Crop harvester (commit 46)
- **Two blocks tall** (dieselpunk look): an olive engine cabinet with a grain hopper, a caged lamp and gauge, and a mast carrying a red reel of bats over a cutter bar, with a beacon on top.
- **Works through the 9×9 field in front of it**, starting the block in front, at its own height (where crops on farmland sit). Each ripe crop takes 20 powered ticks at 24 JE/t (480 JE).
- It keeps the drops in three result slots, **less one seed, which it plants again**. When the drops would not all fit, it waits. Unripe crops are left alone.
- Any `CropBlock` counts: wheat, carrots, potatoes, beetroot, and cotton (commit 48).
- Side configuration, eject, redstone control and upgrades work as on other machines.
- Recipe: steel gears, shears, two hoppers, a machine casing, steel plates and a basic circuit.

## Connections
- Existing input producer: vanilla crops on farmland; fertilizer (batch 5) ripens them faster.
- Existing output consumer: food and seeds for players, the auto-crafter, and anything that takes wheat.
- Technology connection: power, item logistics.
- Magic connection: none.
- Reachable entry path: steel tier; no circular unlock.
- Required vs optional: optional automation.

## Balance and automation
- The harvester only gathers what vanilla crops grow on their own; it makes nothing from power. 480 JE a crop.

## Multiplayer and persistence
Server-side machine; its scan position saves with its block entity like the ore drill's. It only touches crop blocks in its loaded field.

## Dependencies and assets
No new dependencies. Textures and models are original (`tools/dieselpunk_models.py`).

## Verification
- Game test `cropHarvesterHarvestsAndReplants` (JugcraftGameTests): a ripe wheat crop in its field is harvested, replanted at age 0, and the wheat kept.

## World and event applicability
Not applicable: no worldgen, mobs or dimensions.
