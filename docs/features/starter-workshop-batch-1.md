# Starter workshop — Batch 1

Status: implemented and locally validated; draft PR #442 awaiting owner preview approval.
AI assistance: OpenAI Codex (GPT-6).

## Scope

Implement the first slice of the [industrial starter workshop plan](industrial-starter-workshop-plan.md): a two-block-wide Workshop Bench, a one-block Small Ceramic Kiln and a two-block-tall Small Crucible Furnace. The bench uses interchangeable, permanent hammer, wire cutters and fuel-heated soldering tools. No tool wear or installed-part maintenance.

Construction follows the accepted provisional costs: bench = crafting table, four planks, two cobblestone; hammer = two iron and a stick; cutters = two iron and two sticks; soldering tool = copper and a stick; kiln and furnace body each = four brick blocks and a furnace. Shape a lining from four clay balls and a crucible from seven, then fire them and install both in the furnace. One clay ball makes two insulator blanks, fired into the existing ceramic insulators.

## Processing contract

- Manual plates: iron, copper, tin, zinc, lead, bronze and brass, one ingot to one plate, 400 active-work ticks.
- Manual copper wire: one copper ingot to three wires, 400 active-work ticks.
- Manual basic circuit: existing silicon, three copper wires and solder to one circuit, 800 active-work ticks with ordinary heating fuel.
- Kiln: two insulator blanks in 200 ticks; lining in 400 ticks; crucible in 600 ticks.
- Furnace: three copper and one tin to four bronze, or three copper and one zinc to four brass, in 800 ticks; one tin and one lead to two solder in 480 ticks.
- Preserve machine yields and existing powered recipes, bronze-blend fallback, current copper cable and silicon routes. Add six wires, one tin and two insulators to six copper cables as an alternate starter recipe.

Select a recipe, insert ingredients and hold Work for manual production. Server-validated work pauses when released or the player leaves. Fuel credit, committed ingredients and partial progress survive saves; blocked output or missing requirements spend no fuel. Inventory automation never supplies player work or removes a tool during an active batch. Installed lining and crucible are reusable.

Registration remains stable with features disabled; acquisition uses the machines feature without requiring drones for early insulators. Use shared recipes, inventory, fuel and permission systems. New stations must handle interruption, multiple viewers and breaking either multiblock part without duplicate drops.

## Boundaries and overlap check

Current main, open pull requests and public fork branches were reviewed before implementation. The upstream starter-workshop branch contains planning documentation only. Advanced ceramics and powered-tool work are adjacent, not this implementation; retain their identities and recipes.

Batch 2 is excluded: no quartz world generation, silicon heat upgrade, bellows, reusable gear molds, mechanical waterwheel or existing machine cost changes. No terrain generation changes.

## Integration and assets

The workshop shares distinct-slot ingredient matching with powered machines and persists its reserved inputs/results in the existing `WorkLane`. Vanilla cooking-fuel components supply heat; unused credit is retained while idle. Vanilla menu buttons carry only recipe selection and held-work presence, with server-side distance, menu, feature and structure-permission checks. Hoppers can access either station part but cannot supply labor or remove installed tools/ceramics. Recipe reload invalidates open picker snapshots.

Zinc had no plate item on main. This batch adds it to the shared component registry, common plate tags and normal powered press generator, preserving the same one-ingot yield as the new manual route. Existing cable recipes remain; the alternate ceramic cable has a distinct recipe filename so the tower-cost audit continues to use the established canonical cable cost.

The owner asset library and complete catalog were inspected for hammer/cutter references. New native closed-cuboid models use existing Jugcraft `sp_wood`, `sp_iron`, `sp_copper`, firebrick and ceramic surfaces, plus vanilla clay. The zinc icon uses the existing plate renderer and zinc palette. No new dependency, copied external model or new animation runtime is introduced. Shared JEI recipe-view data and the Engineer's Handbook explain the recipes and reusable parts.

When the optional Diorama map is installed, its existing Fabric HUD element is wrapped after client startup to suppress it only while a workshop menu is open. This prevents overlap at small window sizes without mutating map preferences or linking optional Diorama classes into the workshop.

The inspection save is a new normal world with seed `jugcraft-starter-workshop-1`, with a five-station gallery near `512 240 512`. It is separate from existing owner worlds. Plates, wire, circuits, ceramics and alloys each have a prepared example and a supplies chest.

## Validation

20 October 2026, Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Java 25 and the unchanged pinned Complete-pack libraries:

- `runGameTest` filtered to `jugcraft-test:*starter*workshop*`: all 10 passed. Includes real crafting-grid selection (and preserved vanilla clay blocks), all nine manual receipts, three kiln receipts, three alloys, fuel remainders/credit, saved partial work, cancellation/backpressure, concurrent mock-player requests, permissions/feature disabling, Fabric inventory transfers and both multiblock halves' placement/removal/drops.
- `runGameTest` filtered to `jugcraft-test:machine_consolidation_game_tests*`: all 8 passed, covering the existing machines sharing the durable work ledger.
- `test_starter_workshop.py`: 2 passed; `test_survival_recipes.py`: 5 passed; `test_agriculture_recipe_loops.py`: 8 passed. The actual registry export adds exactly the 11 workshop entries and zinc plate, with no removals.
- `tools/check_mod_data.py`: passed, including 6,182 material IDs, recipes, tags, the tower-cost audit and model geometry. No new art exceptions. Generated resources refreshed with `tools/generate_material_data.py`.
- `scripts/check_repository.py`, `scripts/package_modrinth.py check` (21 locked libraries) and `git diff --check`: passed.

- `runClientGameTest -PclientTests=StarterWorkshopClientGameTests` and `jar`: passed. Actual mouse-held work completed an iron plate, releasing paused work, all three station menus rendered, and saving/reopening preserved a new partial plate, leftover fuel credit and four bronze ingots. Six screenshots were inspected; the optional minimap no longer obscures the workshop menus.
- Export cleanup verified 92 save files and scanned 2,591 saved region records. Only the known unused test datapack selections and five test registry-presence entries were removed from a separate delivery copy; all other saved data was preserved. The original export is retained.

Delivery packaging records the source commit, matching mod hash, locked library list and archive verification separately with the inspection world. This is not evidence of two independently authenticated clients, a clean external-launcher import, a production-size performance benchmark or the full game-test suite. Batch 2 remains excluded.
