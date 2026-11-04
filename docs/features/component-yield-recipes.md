# Component recipe yield choices

Status: implemented. The owner-requested follow-up also fixes the remaining PR 600 CI failures; see [the CI repair record](pr-600-ci-repairs.md).
Proposal: owner-requested casual component progression; no separate issue.
Owner: repository owner; contribution prepared with OpenAI Codex (GPT-6).
Scope: basic circuits, advanced circuits, microchips and electric motors.
Primary role: solo or shared industrial component production, from starter crafting through prepared materials.

## Player experience and recipes

Players can use simple ingredients for small batches, then prepare better materials to produce more components per operation. These are additional choices, not new component item tiers.

| Component | Ingredients per operation | Output | Station/time |
| --- | --- | ---: | --- |
| Basic circuit | 1 silicon + 1 copper ingot + 1 tin ingot | 1 | Inventory/table shapeless; also workshop/assembler |
| Basic circuit | 1 silicon + 3 copper wire + 1 tin ingot | 2 | Workshop/assembler, 200 ticks |
| Basic circuit | 1 silicon + 1 copper ingot + 1 solder ingot | 2 | Workshop/assembler, 200 ticks |
| Basic circuit | 1 silicon + 3 copper wire + 1 solder ingot | 4 | Workshop/assembler, 200 ticks |
| Advanced circuit | 2 basic circuits + 1 silver ingot + 1 invar ingot | 1 | Assembler, 300 ticks |
| Advanced circuit | 2 basic circuits + 3 silver wire + 1 invar ingot | 2 | Assembler, 300 ticks |
| Advanced circuit | 2 basic circuits + 1 silver ingot + 1 invar plate | 2 | Assembler, 300 ticks |
| Advanced circuit | 2 basic circuits + 3 silver wire + 1 invar plate | 4 | Assembler, 300 ticks |
| Microchip | 1 silicon wafer + 2 copper wire + 100 mB sulfuric acid | 4 | Lithography, 200 ticks (unchanged) |
| Microchip | 1 silicon wafer + 2 copper wire + 50 mB nitric acid | 8 | Lithography, 200 ticks |
| Electric motor | 4 iron plates + 3 copper wire + 1 iron shaft + 1 copper cable | 1 | Table, existing recipe |
| Electric motor | 2 iron plates + 1 copper wire + 2 precision bearings + 1 precision shaft + 2 copper cables + 1 porcelain insulator | 2 | Table, existing ceramics recipe |
| Electric motor | 2 precision bearings + 1 copper wire + 1 precision shaft + 1 neodymium magnet + 1 polymer housing + 2 copper cables + 1 porcelain insulator | 4 | Table, new ceramics recipe |

The raw basic circuit machine/workshop operation also takes 200 ticks. Workshop circuit operations retain their heat and reusable soldering-tool requirements. The inventory/table recipe requires neither a tool nor workshop heat. Powered assembler energy remains 32 JE/t; lithography remains 192 JE/t. With no upgrades, this is 6,400 JE per basic circuit batch, 9,600 JE per advanced circuit batch and 38,400 JE per microchip batch. Better ingredients increase both material efficiency and components per second without changing batch time.

## Connections and balance

The starter crucible already makes silicon from quartz and charcoal. Copper and tin are smelted directly; the manual one-circuit recipe needs no lead, wire drawer, assembler or component produced by itself. Existing raw motor crafting remains available before precision machining, polymer molding or rare-earth processing. Advanced circuit variants consume existing basic circuits and silver/invar materials. Wires come from the wire drawer; solder comes from tin and lead; plates come from pressing. Precision machining, polymer molding and rare-earth processing supply the four-motor route. Lithography retains its existing wafer/acid supply chains. These items continue feeding existing machines, upgrades, electronics and kinetic equipment; processor recipes are unchanged.

Prepared choices are optional. Players may produce them themselves or trade components/materials with other players. Existing ingot-to-wire/plate yields are unchanged. No reverse recipe from these four finished components to their ingredients is introduced. The shared alchemy catalogue must also follow the cheaper recipe costs: retaining the old component essence values would allow prepared batches to create essence. Component and dependent recipe-derived values are recalculated accordingly; existing stored essence balances are unchanged. The component feature changes these four recipe families and their derived workshop variants. The separate CI repairs restore missing native acquisition routes and expanded-catalogue generation. High-purity silicon, processor, casing and gasket recipes are outside this scope.

The value builder's repeated `--affected-item` option preserves unrelated prices and catalogue membership for a bounded recipe change. The original component update reduced 418 existing records without increasing any redemption price. The CI follow-up also refreshes explicit values for the expanded authored catalogue, as required by the existing checks. Basic circuits remain 160 essence, advanced circuits 81, microchips their provisional 1, and motors 1075/16; the selected component prices are unchanged by that refresh.

The raw circuit crafting route also changes five source estimates in the Drone Tower's steel-ingot-equivalent cost table; these are refreshed against the actual crafting recipes. Existing serialized tower geometry and upgrade quotes are untouched. The prepared motor connects the existing Cydonian-shard-to-rare-earth route to electric motors and reusable cutting tools. The recipe graph conservatively counts retained tools as inputs, so its declared boss reward routes now include Mendvetch and sugar through that manufacturing path. No boss loot or product recipe changes.

## Shared systems, compatibility and provenance

Uses the existing server-owned crafting, multi-input assembler, lithography and workshop systems. Circuits still take three ingredient slots in any order. Generated recipe previews and handbook data use the same authoritative recipes. Workshop recipe names now honor explicit source names so variants cannot overwrite the original prepared recipe. Original prepared recipe IDs remain stable; new IDs are additive. Machines keep their existing stack-capacity, energy, automation and persistence behavior. No new saved state or migration is required. Rollback restores earlier yields and derived essence costs and removes the additional recipe choices; produced components are ordinary existing items.

Existing feature load conditions apply to each ingredient family. No dependencies, assets, copied code, registry entries, networking, ownership rules or platform versions change. Existing alchemy uses the revised recipe-derived values. The component feature itself adds no magic content, world generation, dimensions, entities, pets, bosses or seasonal behavior; the separate CI acquisition repairs are described in their feature record. Industrial recipes are available wherever their existing stations can operate. Dedicated multiplayer and restart behavior use the existing systems; no new two-client or restart claim is made.

## Verification

Target platform: Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Java 25.

`ComponentYieldGameTests` checks all eight circuit ingredient combinations across all six slot permutations, exact consumption counts, rejection of insufficient wire, all four workshop choices, inventory circuit crafting, all three motor crafting tiers, real assembler output backpressure/consumption, and real sulfuric/nitric lithography batches. The existing nitric lithography regression now expects eight microchips.

`AlchemyValueTests.test_component_yield_choices_cannot_multiply_essence` checks the actual generated recipes against the shared catalogue, resolving ingredient tags and batch counts. For lithography it conservatively requires the solid inputs alone to cover the chip output, without assigning free recoverable acid value.

Local verification on November 1, 2026, based on upstream `dcf6b249fa`:

- Common, client and game-test sources compile with the pinned Java 25 toolchain.
- All four component gameplay tests pass within a 36-test focused CI-regression run. This covers all 48 circuit ingredient/slot combinations, workshop variants, inventory crafting, motor tiers, both acid batches and real output backpressure/consumption. The disposable server saved and shut down successfully.
- All 383 Python checks pass, including component conservation, scoped recalculation, workshop, tower costs, reward-route and ritual-metadata regressions. Additional material-data evidence is recorded in the [CI repair record](pr-600-ci-repairs.md).
- Canonical generation completes with the repaired resource writers. The previous wall-tag, inverted-cuboid and model-audit blockers are addressed by the CI follow-up.
- Pack pins, repository structure/documentation links and all twelve World Designer/pack-compiler tests pass.

The focused server run selected seven test classes in the disposable prepared test mod and reused compiled classes with verified copied resources. Committed test metadata and CI retain the full suite. The fresh registry export is identical to the checked-in snapshot. The [upstream PR 600 checks](https://github.com/jimbozoomer-byte/jugcraft/pull/600/checks) track the complete server and rendered-client runs.

Human gameplay, independent two-client testing and live-server restart testing are not claimed. No live server or personal world was modified.
