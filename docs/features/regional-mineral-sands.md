# Regional mineral-sand deposits

Status: owner-approved implementation in progress, 13 October 2026. Public branch `speedygroyper:codex/regional-mineral-sands`, based on the tested fluorite/spodumene work in PRs [#343](https://github.com/jimbozoomer-byte/jugcraft/pull/343) and [#351](https://github.com/jimbozoomer-byte/jugcraft/pull/351). Actual tool: OpenAI Codex (GPT-6), explicitly authorized by the owner.

The owner approved all four families in the [mineral-sands plan](mineral-sands-and-refining-plan.md), with shallow irregular sediment lenses, visible exposures, regional biome eligibility, terrain protection and finite reserves. This branch announces the work before implementation.

| Family | Length × width × thickness (blocks) | Buried top | Attempt frequency |
| --- | --- | --- | --- |
| Titanium | 24–40 × 12–24 × 3–6 | 0–2 | 1 per 48 eligible chunks |
| Zircon | 20–36 × 10–20 × 3–5 | 1–3 | 1 per 64 |
| Rare earth | 16–28 × 10–18 × 3–5 | 1–4 | 1 per 96 |
| Garnet | 20–32 × 10–18 × 2–4 | 0–2 | 1 per 48 |

Titanium favors Dune Beach, Overgrown Beach and Beach. Zircon favors Outback, Lush Desert, Dry River, Desert and sandy Badlands terraces. Rare earth favors Basin, Lake District, Lush River and forest/taiga river banks. Garnet favors Gravel Beach, Highland, Shield, Stony Shore and Windswept Gravelly Hills. Actual suitable ground is required; biome names do not establish flat terrain.

## Reserve contract

The saved mineral-bearing blocks are the reserve. Each block is one unit of unprocessed feed, recovered once by mining or powered extraction. Moving and replacing harvested feed moves that unit; it does not create another reserve or reset a hidden capacity. Ordinary sand and depleted terrain never replenish it. There is no 1,000-unit-per-block multiplier. World generation only adds reserves in new chunks.

Existing titanium/monazite routes remain available. Sand separation/refining machinery and a dedicated shaft-powered extractor are later work; this slice provides natural acquisition, stockpiling and compatible existing powered extraction. Exact mineral recovery yields are not selected here.

## Validation

Implementation and gameplay checks are pending. Planned checks cover all four families, regional placement, terrain safety, hand/powered depletion, full output, repeated extraction, save/reopen, disabled acquisition, normal and Designed world generation, and an exported inspection world. No test result is claimed by this scope announcement.
