# Rocky-upland spodumene deposits

Status: owner-authorized implementation in progress, 13 October 2026, branch `speedygroyper:codex/spodumene-deposits`. Builds on the fluorite stone-outcrop update in [PR #343](https://github.com/jimbozoomer-byte/jugcraft/pull/343), commit `6256dd86`. This notice claims the work publicly before implementation. OpenAI Codex (GPT-6) is the actual implementation tool, explicitly authorized by the owner.

## Approved scope

- Seven surface biomes: `jugcraft:shield`, `jugcraft:highland`, `jugcraft:basin`, `minecraft:windswept_hills`, `minecraft:windswept_gravelly_hills`, `minecraft:windswept_forest`, `minecraft:stony_peaks`.
- Overworld deposit centers Y -16..64, triangular peak Y 24; native ore size 40; one attempt per twelve eligible chunks on average. Cave exposure allowed. Actual block yield must be sampled; size is not a block-count promise.
- Use the surface-biome selection shared with fluorite, including World Designer paint above cave biomes.
- One quarter of successfully placed pockets attempt the shared stone outcrop: 3-8 visible mineral blocks, roughly 4-6 blocks across, 1-2 blocks above local ground and 1-2 buried stone layers. Preserve bounded placement, fluid/structure/obstruction checks and finite mining.
- Stone/deepslate ore forms, one spodumene item, nine-item storage, iron-tier mining, standard Fortune/Silk Touch, shared crusher, prospector and ore drill.
- Preserve existing lepidolite acquisition and lithium processing. This delivers the natural resource and stockpiling slice before server world generation; the selected expanded spodumene refining chain remains future work under the [industrial plan](industrial-chemistry-and-fuels-plan.md#resource-reagent-and-residue-decisions-thirteenth-batch).
- Use the existing `lithium.enabled` acquisition switch; keep saved registrations. New chunks only, no retrogen. No dependency or platform changes.

## Validation and delivery

Pending implementation: server tests for mining/storage and survey/drill integration; real normal-seed and Designed-world generation; surface safety, persistence and disabled-acquisition checks; data/art/repository audits; a matching preview pack and saved inspection world. No test result is claimed by this initial notice.

## Art and provenance

The complete owner catalog was searched for spodumene, kunzite and hiddenite; no named matching asset was found. Reuse the established hand-authored mineral maps and layered stone/deepslate model with a distinct pale-green crystal palette. Preserve the original owner library and existing ore palettes/layouts. The shared outcrop uses vanilla stone by runtime registry reference. No Minecraft image bytes are copied.
