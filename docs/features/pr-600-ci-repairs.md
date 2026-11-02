# PR 600 CI repairs

Owner request: fix the remaining checks on upstream PR 600, including the catalogue failures inherited from main. Prepared with OpenAI Codex (GPT-6). This supplements [component yield choices](component-yield-recipes.md).

## Generation and resource validation

The wall/tag patch failed because its exact old list no longer matched the expanded catalogue. Additive list patches now retain current members and reject conflicting definitions; replacements and removals still require their exact preconditions. Final language, sounds, tags and atlas additions run after their owning generators. Source archives remain unchanged.

The inverted supplied lantern shell is represented by inward-facing planes with the same surfaces and UVs. Source-backed multipart repairs remove internal or duplicate faces, preserve visible caps, and separate coincident grass planes deterministically. Shape keys have a stable order. Atlas validation resolves inherited materials and model references. Opaque caps are verified against their decoded pixels; the art gate still checks exposed geometry rather than exempting whole collections. CI prepares the pinned vanilla shape references before generation.

## Catalogue and acquisition

Refresh the actual loaded item-registry snapshot and explicit alchemy records for the expanded registered catalogue. The four component prices remain 160 basic circuit, 81 advanced circuit, 1 microchip and 1075/16 motor. The earlier scoped recalculation remains available. Imported salvage roots retain their recoverable metal budget, preventing a tin-can/goldfish route from depressing reversible nugget prices. Provisional alchemy values remain labelled as balance pending.

The acquisition graph now reads the native botanical, arcane, storage, fishing, flora, animal, trading and component-preserving crafting systems with their actual stations, catalysts, forge floors, essence sources and paid ingredients. Missing optional tag members do not become ingredients; missing required members still fail. Cycles and self-drops cannot create starting resources. Registered presentation and administrative items have explicit source-backed reasons; the previously deferred design count remains unchanged.

The separate Concordance product audit reads actual ritual results rather than their serializer and magic-circle IDs. A shared renderer name cannot join unrelated material routes. Grave Dust does have a real additional route through bone meal, Mundabitur Dust, Deorum and the newly paid tool rituals to retained cutting tools and sugar; its declaration includes that route. A negative regression removes the new ritual results and restores the previous Mendvetch-only route. Yeti Fur still reaches no product ingredient through recipes.

The remaining missing active recipes receive paid native routes through existing systems:

- Furniture variants use the existing family bills or consume a finished base plus glass, iron, bones or Echo Shards. Paired stockings consume two stockings; ghost and bat variants consume their base decoration and an extra ingredient. Trader cushions consume a cushion, blue dye and gold nugget.
- Three recovered codices consolidate into one higher codex. Their experience grants are 3×55 →160 and 3×160 →480, so consolidation cannot create experience. Memory crystals use the Enchanting Apparatus and 1,000 Source; the upgraded Wixie Cauldron consumes its existing cauldron, an apparatus and an enchanting table.
- Missing botanical armor/water recipes retain their supplied patterns. Armor upgrades preserve stack components; only a plain water bottle satisfies water crafting. Soulscribe converts an Ender Dagger with ink at the smithing table, keeping its name, enchantments and wear. Gaia heads require a skull and four boss Life Essences. Living roots, root blocks, pebbles, the Storm Lens and imported decorative rods have explicit paid recipes.
- Draco tools upgrade Netherite at forge tier 2 with Dragon Scales and Obsidiansteel. Reinforced Deorum tools/gavel require tier 3, Deorum and Stellarite. Mortem armor requires tier 3, leather armor, Dark Matter and a Soul Binding Crystal. These fifteen upgrades transmute the paid base stack, retaining names, enchantments and wear for damageable outputs; Mortem retains its native indestructible behavior. Ritual outputs accept both the existing string IDs and object stacks. Other missing arcane ingredients use tiers 1–4 with explicit essence costs and at most eight pedestal items. Corrupted crystals have an entry route outside their block/compression cycle.
- Aurum logs and wood use the shared stripping behavior. Existing skull degradation, obelisk formation and Edelwood milk/powder-snow collection are represented in the audit. Magical Farmland remains an in-world state whose actual loot returns dirt. The infinite Blessing administrator item and inert Sunblock Staff presentation are not Survival rewards.
- The supplied Largemouth Bass spawn data names Smallmouth Bass. A native Largemouth entry in rivers and swamps makes the distinct fish bucket capturable.

These routes use existing IDs, recipes, workshops and server authority. There are no new dependencies, registries or saved-state formats. Existing earned items remain valid. Reverting removes new acquisition recipes without deleting saved items. No live server or personal world is involved.

## Test infrastructure

The full server suite retained players created by direct vanilla helper calls, eventually exhausting its 4 GB heap. A test-only helper mixin registers those mock players for the existing per-test logout cleanup. Cleanup is idempotent and leaves real clients alone. Lifecycle regressions cover direct helpers, normal helpers, failure completion and repeated cleanup.

Bound peer-client startup, isolate tests that own separate clients, give disposable client runners swap, and retain a bounded 6 GB rendered-client heap. Server tests retain their 4 GB bound and every test. Lua tests wait for actual readiness. Tooltip previews receive a valid entity identity before rendering; third-person aiming uses the native interpolation conventions. These are existing CI fixes reused from the local integration repair branch; no companion feature is imported.

Update the twelve-recipe starter-workshop expectation, validate real encyclopedia page destinations, account for the native custom serializers in JEI coverage, and separate the colliding scaffold/portcullis crafting patterns. Existing CI gates remain enabled.

The first fresh CI run passed Foundation, canonical generation, the full material audit, packaging, both complete 3,291-test server suites (with and without optional integrations), and fourteen of sixteen client shards. A rendering fixture stopped its empty-Filter search after 32 pages (8,192 recipes), before reaching vanilla recipes in the expanded catalogue. It now allows exactly the loaded catalogue's page count, using the unchanged 256-recipe page budget. Two headless regressions check complete discovery without consuming Supply and refusal to invent a plan from empty Supply. The loaded catalogue contains 11,704 recipes; the available plan is on page 44 of 46. Both regressions and the rendered crafting-table test (104 assertions, including physical Peepo/Jughead work and container remainders) pass locally. No crafting or work limit is increased.

The other failing shard's biome tour crossed UTC midnight from November 1 to November 2 with random ticks disabled: leaves generated on season day 305 were compared against day 306. The fixture now freezes the season clock before world creation, including spawn generation, and restores it after the disposable world closes. This test-only mixin is absent from the distributed mod, retains the configured zone, and leaves operator settings unchanged. The zero-stale-leaf assertion remains enabled. A server regression exercises both sides of that midnight, nested scope restoration and return to the real clock.

The next CI run's shard 14 lost its runner during startup: GitHub's annotation reports `No space left on device` in the runner's own diagnostic log, with no test log or artifact uploaded. Before restoring Gradle caches, disposable client jobs now reclaim the image's unused Android, .NET and Haskell SDK directories. The script refuses to clean local or self-hosted machines, uses fixed paths, preserves the game/dependency cache, and propagates cleanup errors. Four mocked shell regressions cover those boundaries without deleting any installed SDK. The 8 GB swap and JVM/test bounds remain unchanged.

## Verification

On `98934e575f`, all sixteen client shards and the aggregate client check pass, including the fixed biome tour and crafting table. Foundation passes all 387 Python and fourteen installer contract tests, and the optional-absent server suite passes all 3,294 tests. The main server run exposed a separate lighting race: the garden-colony test read both light levels as 15 at its fixed tick-40 check. A local 200-tick poll reproduced the stale values. The fixture now waits for Minecraft's actual pending light work in the enclosure/control chunks, pumping server tasks with a ten-second deadline, before requiring both light thresholds. The sequence remains bounded at 200 ticks and executes every growth and bone-meal assertion once. A delayed-roof case exercises lighting changes after the old cutoff; worker/growth errors are terminal and no game behavior or test is skipped.

The final lighting repair compiles and passes all eleven `GardenGameTests` locally, including the roof added at tick 60. The focused run uses compiled classes and verified prepared resources in a disposable server; production mushroom behavior and the complete committed test list remain unchanged. Repository/documentation links and whitespace checks pass.

Pinned platform: Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Java 25. Common, client and game-test compilation passes. Pack pins, repository structure and documentation links pass. The source writers produce the paid recipes from the same inputs consumed by the game.

Local verification on November 1, 2026:

- All 383 Python tests pass. The Windows run used Linux-equivalent UTF-8/LF text output and normal process access for the graphics-setup fixtures.
- All 36 selected server regressions pass across Component Yield, Starter Workshop, Acquisition Repairs, Test Players, Peer Client Startup, Lua and Survival Recipes. The server saved and shut down successfully. These were focused runs using compiled classes and verified copied resources; the committed metadata retains every server test.
- The follow-up passes all 34 focused season, biome/tree-generation and crafting-catalogue server tests. The rendered crafting-table fixture passes all 104 assertions. Game-test compilation, repository/documentation links and whitespace checks pass again. The full rendered biome tour remains for fresh upstream CI.
- All 23 loaded component-preserving equipment rituals match their paid bases and pedestal ingredients, reject a wrong base, preserve names/enchantments, and preserve wear where applicable. Mortem remains indestructible. Source strings and object outputs both work.
- A fresh game export exactly matches the 16,879-ID registry snapshot, including 15,091 Jugcraft IDs. The acquisition graph has no unresolved active items or invalid ingredients and retains the existing 78 deferred designs.
- Canonical generation completes. Shared and standalone acquisition writers reproduce all 87 affected resources, including native loot predicates. Source archives are unchanged.
- The complete model pass checks 38,254 block/item models, 3,777 quad parts and 182 assemblies without art errors. Full Concordance workshop-product and winter audit families pass, including the real Grave Dust reward route and the negative Yeti Fur constraints.
- Pack pins, repository structure/documentation links and all twelve World Designer/pack-compiler tests pass.

The [upstream PR 600 checks](https://github.com/jimbozoomer-byte/jugcraft/pull/600/checks) record the fresh generated-data comparison, complete material audit, full server suites with/without optional integrations and rendered clients. Automated results do not constitute human gameplay, independent two-client or restart testing. No live server or personal world was changed.
