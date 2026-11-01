# Component recipe yield choices

Status: implemented; validation recorded below.
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

Prepared choices are optional. Players may produce them themselves or trade components/materials with other players. Existing ingot-to-wire/plate yields are unchanged. No reverse recipe from these four finished components to their ingredients is introduced. The shared alchemy catalogue must also follow the cheaper recipe costs: retaining the old component essence values would allow prepared batches to create essence. Component and dependent recipe-derived values are recalculated accordingly; existing stored essence balances are unchanged. Only the four selected component recipe families and their derived workshop variants change. High-purity silicon, processor, casing and gasket recipes are outside this scope.

The value builder's repeated `--affected-item` option updates these four existing items and their transitive recipe consumers while preserving all unrelated ingredient prices and catalogue membership. This update changes 418 existing records and increases no redemption price. Basic circuits are 160 essence, advanced circuits 81, microchips retain their existing provisional 1, and motors are 1075/16. It does not enable the uncatalogued collection items tracked separately in the main-branch TODO.

The raw circuit crafting route also changes five source estimates in the Drone Tower's steel-ingot-equivalent cost table; these are refreshed against the actual crafting recipes. Existing serialized tower geometry and upgrade quotes are untouched. The prepared motor connects the existing Cydonian-shard-to-rare-earth route to electric motors and reusable cutting tools. The recipe graph conservatively counts retained tools as inputs, so its declared boss reward routes now include Mendvetch and sugar through that manufacturing path. No boss loot or product recipe changes.

## Shared systems, compatibility and provenance

Uses the existing server-owned crafting, multi-input assembler, lithography and workshop systems. Circuits still take three ingredient slots in any order. Generated recipe previews and handbook data use the same authoritative recipes. Workshop recipe names now honor explicit source names so variants cannot overwrite the original prepared recipe. Original prepared recipe IDs remain stable; new IDs are additive. Machines keep their existing stack-capacity, energy, automation and persistence behavior. No new saved state or migration is required. Rollback restores earlier yields and derived essence costs and removes the additional recipe choices; produced components are ordinary existing items.

Existing feature load conditions apply to each ingredient family. No dependencies, assets, copied code, registry entries, networking, ownership rules or platform versions change. Existing alchemy uses the revised recipe-derived values; no new magic content, world generation, dimensions, entities, pets, bosses or seasonal behavior is involved. Industrial recipes are available wherever their existing stations can operate. Dedicated multiplayer and restart behavior use the existing systems; no new two-client or restart claim is made.

## Verification

Target platform: Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Java 25.

`ComponentYieldGameTests` checks all eight circuit ingredient combinations across all six slot permutations, exact consumption counts, rejection of insufficient wire, all four workshop choices, inventory circuit crafting, all three motor crafting tiers, real assembler output backpressure/consumption, and real sulfuric/nitric lithography batches. The existing nitric lithography regression now expects eight microchips.

`AlchemyValueTests.test_component_yield_choices_cannot_multiply_essence` checks the actual generated recipes against the shared catalogue, resolving ingredient tags and batch counts. For lithography it conservatively requires the solid inputs alone to cover the chip output, without assigning free recoverable acid value.

Local verification on November 1, 2026, based on upstream `dcf6b249fa`:

- Common, client and game-test sources compiled successfully with Java 25 (`compileGametestJava`, with resource processing excluded for this compile-only check).
- All 18 affected recipes were rebuilt through the existing recipe, ceramics and workshop source writers and checked against their emitted JSON. The handbook, encyclopedia, recipe-view cache and manifest were refreshed from the shared sources. The manifest adds 11 recipe files.
- Four scoped value-builder tests pass, including a complete temporary-catalogue build that preserves unrelated prices and excludes new items.
- The component essence-conservation regression passes for all 18 recipe choices. A comparison with the previous catalogue confirms identical membership and no increased redemption value.
- All three workshop Python tests pass, including the reachable starter-production path and the updated variant-count/yield regression.
- Repository structure/documentation-link check passed.
- All four focused server game tests pass: 48 circuit slot/ingredient matches, all workshop variants, raw inventory crafting, all three motor tiers, output backpressure/consumption and both acid batches. The disposable test server saved and shut down successfully. The focused run reused verified compiled classes and copied resources while bypassing Gradle's lengthy resource-cache bookkeeping; 23 component/registry files were compared byte-for-byte and required dependency metadata checked before the run.
- Two additional connection regressions pass: tower source costs match the revised recipes, and the declared Cydonian reward route exists only when the new prepared motor recipe is present.
- The full data audit reported 708 existing model diagnostics and six new non-art diagnostics (five tower estimates and the motor's reward-route declaration). The six component-related findings were corrected and their complete tower/product audit families rechecked. Model geometry is unchanged; this is not a full art/data audit pass.

The broader Python sweep ran 295 tests and reported 14 failures. The affected old workshop expectation was corrected and its three-test suite passed on rerun. Six graphics-setup fixtures failed because the restricted Windows process could not start MSYS Bash; all six passed with normal local process access. Seven unrelated failures remain: missing existing alchemy catalogue IDs, Candy Cavity regeneration repeatability, an encyclopedia link expectation, unaccounted custom recipe serializers, overlapping Jimtafacts crafting bills, an existing furniture essence budget, and the survival-audit registry snapshot. These do not represent a full-suite green result; the main-branch catalogue/audit follow-ups remain open.

Full canonical generation is blocked on main's stale wall-tag patch precondition. Temporarily refreshing that precondition exposed a second existing blocker in an imported Newest model: an inverted cuboid (`[13,9,13]` to `[3,0,3]`). Neither the wall patch nor imported-model changes are included here. Selected component data was generated using the same shared source writers, and unrelated partial-generation output was restored.

Upstream PR #600 CI independently ran 295 Python cases on the implementation commit: 289 passed, including the component/value/workshop checks, and six failed on the existing catalogue/registry/recipe expectations listed above. Candy repeatability and all six graphics fixtures passed on Linux. The generated-data job reproduced the wall-tag patch failure. [Foundation evidence](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/41348569542), [generation evidence](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/41348569545/job/124973587118). These runs preceded the two final connection regressions; their local results are recorded above.

Manual gameplay, rendered client inspection, the complete server suite and independent two-client testing have not been performed for this recipe change. No live server or personal world was modified.
