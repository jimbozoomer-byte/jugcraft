# Overworld quartz and starter silicon

Status: owner-authorized implementation in progress on `speedygroyper:codex/overworld-quartz-starter-silicon`, 21 October 2026. Not yet tested or approved for release. AI assistance: OpenAI Codex (GPT-6).

## Scope

Finite stone/deepslate quartz veins supply existing vanilla quartz before Nether travel. Primary surface biomes: Highland, Shield, Basin, Windswept Hills, Windswept Gravelly Hills, Windswept Forest, Stony Peaks and Stony Shore. Secondary biomes: Forest, Birch Forest, Taiga, Old Growth Pine Taiga, Old Growth Spruce Taiga, Woodland, Coniferous Forest, Maple Woods and Aspen Glade. Centers Y -16..96, triangular peak Y40. Primary size20 at one attempt per four eligible chunks; secondary size12 at one per twelve. Primary successful pockets attempt the existing supported stone outcrop one third of the time, exposing 3–6 quartz blocks. Size and attempt counts are not guaranteed ore yields. Stone pickaxe, ordinary Fortune/Silk Touch; new chunks only.

Extend the existing Small Crucible Furnace with a permanent heat insert (two brick blocks and a fired basic ceramic lining) and a grounded side-mounted bellows (four planks, two leather, one iron shaft). One quartz and one charcoal produce one silicon in 200 active ticks, consuming ordinary heating fuel separately. Manual pumping toggles while the operator keeps the menu open; shaft drive takes 8 KE/t, with equal speed/yield and no stacking. Missing airflow/fuel and blocked output pause without losing committed ingredients, progress or unused fuel. No wear or replacement upkeep. Existing alloys remain independent of the upgrade.

The five-second Blast Furnace silicon stand-in remains during preview review. Retire it only after the new route is tested and the owner approves the preview. The Arc Furnace retains two silicon per quartz. No waterwheel, gear mold, steel or other batch work is included.

## Integration and validation plan

Reuse surface-linked deposits, shared workshop recipes/work ledger, server permissions, kinetic consumers, native menus and existing material/model generators. Preserve registered identities, old workshop inventories and feature-disable behavior. Add handbook/JEI guidance and alchemy values. Check focused mining/worldgen/workshop/kinetic regressions, recipe and art data, compilation and rendered interaction. Inspect natural terrain across multiple seeds and export a separate world with deposits, cutaways, manual and shaft-driven examples. Record actual results and limitations here before delivery; no tests have run for this batch yet.

Related: [starter workshop](starter-workshop-batch-1.md), [selected planning](industrial-starter-workshop-plan.md), [supported mineral outcrops](spodumene-deposits.md).
