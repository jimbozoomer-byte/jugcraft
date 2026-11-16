# Encyclopedia route maps: Technology and Magic progression routes

Status: implemented locally, **unvalidated** (no Minecraft build or game/client test has run; see Verification).
Proposal issue: none; owner-approved batch (worker E, November 12), from [TODO](../TODO.md#unified-encyclopedia-and-npc-mentors--october-29-local)
"Future expansion: deeper per-item coverage, route graphs and optional quests/rewards" and the
[Encyclopedia list](../TODO.md#jugcraft-encyclopedia) item "Design route graphs with readable connections, grouped nodes and
prominent milestones. Distinguish actual required materials/capabilities from recommendations, trade alternatives and
suggested quest order."
Owner: jimbozoomer-byte. Implemented with Claude Opus 5.5.
Target milestone and tier: guidance from the first session to the tier after first power (basic circuits, steel), and
from a first luminous specimen to the first spell, Source Gem and transmutation.
Primary specialty and supported player role: every player; engineers and mages each get their own route.

## Player experience

Open the Encyclopedia (key or inventory button), choose Technology or Magic, and pick the new **Map** tab beside Topics
and Milestones. The left page is the route as a graph: groups in order, each under its heading (a star marks a milestone
group), each step an item tile, and lines from each step to the earlier steps it needs. Selecting a step (click, or
Up/Down) draws its own lines solid (what it needs, with an arrowhead) or dashed (what it may use instead); the rest stay
faint so the map stays readable at every GUI size.

The right page says, for the selected step: its weight (required step, recommended, alternative or milestone), where it
is made (crafted at, smelted in, processed in, worked at, imbued in, gathered from) with the exact station, its inputs
with counts and where each comes from (a step above, a vanilla item, or the machine its own recipe uses), its outputs
with counts, the steps it comes after, other ways to make it, a short tip, and **this server's live recipe** (the grid
or process the server sent for that step, or "This server has no recipe for this step").

Two buttons follow the step: **Open entry** opens its existing Encyclopedia entry (an Engineer's Handbook page, which
closes back to the map; an Encyclopedia lesson; or an Arcane Concordance codex category), and **Live recipe** opens the
new route page at that step's topic. Encyclopedia pages opened from a route now carry a **< Back to the route** button
that returns to the same tab and step. Enter on a focused tile opens its entry, as Enter does on the other tabs.

The same routes are also two ordinary Encyclopedia pages, **Technology Route Map** and **Magic Route Map**, one topic per
step, each with the server's crafting grid, or (new) a furnace, machine or starter-workshop process grid, the step's
required inputs, other ways (marked Alternative), live town sales of its item and inputs where a town sells them today,
and its tip.

Weights, in shape as well as colour: a solid frame is required; a dotted blue frame recommended; orange corner brackets an
alternative; a gold double frame with a star a milestone. On the pages, Alternative rows carry a new fork mark.

## The routes

The route **selection and order** are hand-written in `tools/encyclopedia_progression.py`. Everything else is derived
from the data: each step's station, inputs (one per distinct ingredient, with counts) and outputs from the recipe it
names (`data/*/recipe`, or `data/jugcraft/nouveau_recipe` for Jug Nouveau), gathered materials from their block's loot
table, connections from which earlier step makes an input, station or tool, and entries from the generated Engineer's
Handbook (the page that crafts the item, else the page with its icon) unless a step names one. Inputs no step makes say
how they are made from a chosen recipe (also read from the data) or that they are vanilla items.

The bottlenecks come from the existing guides: the starter technology lesson and handbook (casing, first power, cable to
the right socket, first powered furnace, crusher as an optional upgrade, steel as a parallel capability), the starter
workshop chapter (unpowered plates and alloys), the magic and alchemy lessons and the Jug Nouveau guide (wand, Lampwright
Bench, Imbuement Chamber, Source Gem, Dominion Wand, Source Jar, Scale before Stone, table after Dark Matter), and the Ley
Pylon's record (JE or Radiance into Ley Charge).

#### Technology route map

| Group | Step | Weight | Made by | Station | Recipe or source | Needs |
| --- | --- | --- | --- | --- | --- | --- |
| Mine and smelt tin and zinc | raw_tin | required | gathering | - | breaks from `jugcraft:tin_ore` | - |
| | tin_ingot | required | smelting | furnace | `jugcraft:tin_ingot_from_smelting_raw_tin` (other way: blasting) | raw_tin |
| | raw_zinc | required | gathering | - | breaks from `jugcraft:zinc_ore` | - |
| | zinc_ingot | required | smelting | furnace | `jugcraft:zinc_ingot_from_smelting_raw_zinc` (other way: blasting) | raw_zinc |
| Bronze and the machine casing ★ | bronze_blend | required | crafting | crafting table | `jugcraft:bronze_blend` | tin_ingot |
| | bronze_ingot | required | smelting | furnace | `jugcraft:bronze_ingot_from_smelting_bronze_blend` (other ways: crucible, Alloy Smelter) | bronze_blend |
| | machine_casing | required | crafting | crafting table | `jugcraft:machine_casing` | bronze_ingot, zinc_ingot |
| Your first power ★ | copper_cable | required | crafting | crafting table | `jugcraft:copper_cable` | tin_ingot |
| | coal_generator | required | crafting | crafting table | `jugcraft:coal_generator` | bronze_ingot, copper_cable, machine_casing |
| | Power is flowing | milestone | | | | copper_cable, coal_generator |
| Your first powered machines ★ | electric_furnace | required | crafting | crafting table | `jugcraft:electric_furnace` | bronze_ingot, copper_cable, machine_casing |
| | crusher | recommended | crafting | crafting table | `jugcraft:crusher` | copper_cable, machine_casing, bronze_ingot |
| | crushed_iron (2 raw iron) | recommended | machine | Crusher | `jugcraft:crushing/iron_ore` | crusher |
| | A working production line | milestone | | | | first_power, electric_furnace |
| The pre-electric workshop (parallel) | workshop_bench | required | crafting | crafting table | `jugcraft:starter_workshop/workshop_bench` | - |
| | workshop_hammer | required | crafting | crafting table | `jugcraft:starter_workshop/workshop_hammer` | - |
| | iron_plate | required | workshop | Workshop Bench (hammer) | `jugcraft:starter_workshop/iron_plate` (other way: Metal Press) | workshop_bench, workshop_hammer |
| Plates, wire and circuits ★ | alloy_smelter | required | crafting | crafting table | `jugcraft:alloy_smelter` | bronze_ingot, copper_cable, machine_casing |
| | crucible_furnace | alternative | crafting | crafting table | `jugcraft:starter_workshop/small_crucible_furnace` | - |
| | brass_ingot | required | machine | Alloy Smelter | `jugcraft:alloying/brass_ingot` (other way: crucible) | zinc_ingot, alloy_smelter (or crucible_furnace) |
| | metal_press | required | crafting | crafting table | `jugcraft:metal_press` | bronze_ingot, copper_cable, machine_casing |
| | wire_drawer | required | crafting | crafting table | `jugcraft:wire_drawer` | brass_ingot, copper_cable, machine_casing |
| | circuit_assembler | required | crafting | crafting table | `jugcraft:circuit_assembler` | tin_ingot, metal_press, copper_cable, machine_casing |
| | basic_circuit | required | machine | Circuit Assembler | `jugcraft:circuit_assembly/basic_circuit` | wire_drawer, tin_ingot, alloy_smelter, circuit_assembler |
| | Basic circuits | milestone | | | | basic_circuit |
| Steel, a parallel capability ★ | coke_oven | required | crafting | crafting table | `jugcraft:coke_oven` | - |
| | coke | required | machine | Coke Oven | `jugcraft:coking/coal` | coke_oven |
| | steel_foundry | required | crafting | crafting table | `jugcraft:steel_foundry` | iron_plate |
| | steel_ingot | required | machine | Steel Foundry | `jugcraft:steelmaking/steel_ingot` | coke, steel_foundry |
| | Steel | milestone | | | | steel_ingot |

Off-route inputs and the recipe that describes them: tin plate (`pressing/tin_ingot`, Metal Press), bronze gear
(`bronze_gear`, crafting), silicon (`silicon_from_blasting_quartz`, blast furnace), copper wire
(`wire_drawing/copper_ingot`, Wire Drawer), solder (`alloying/solder_ingot`, Alloy Smelter).

#### Magic route map

| Group | Step | Weight | Made by | Station | Recipe or source | Needs |
| --- | --- | --- | --- | --- | --- | --- |
| Find something that glows | amethyst_shard | required | gathering | - | breaks from amethyst clusters (vanilla) | - |
| | glowstone_dust | alternative | gathering | - | breaks from glowstone (vanilla) | - |
| | Examine a glow | milestone | | | | amethyst_shard |
| Casting: your first instrument ★ | initiate_wand | required | crafting | crafting table | `jugcraft:initiate_wand` | amethyst_shard (or glowstone_dust) |
| | lampwright_bench | recommended | crafting | crafting table | `jugcraft:lampwright_bench` | amethyst_shard (or glowstone_dust) |
| | Your first spell | milestone | | | | initiate_wand, first_light |
| A magical workshop ★ | novice_spell_book | recommended | crafting | crafting table | `jugcraft:nouveau/novice_spell_book` | - |
| | archwood_log | required | gathering | - | breaks from `jugcraft:nouveau_green_archwood_log` | - |
| | archwood_planks | required | crafting | crafting table | `jugcraft:nouveau/archwood_planks` | archwood_log |
| | imbuement_chamber | required | crafting | crafting table | `jugcraft:nouveau/imbuement_chamber` | archwood_planks |
| | source_gem | required | imbuement | Imbuement Chamber | `jugcraft:nouveau/imbuement_amethyst` | amethyst_shard, imbuement_chamber |
| | dominion_wand | recommended | crafting | crafting table | `jugcraft:dominion_wand` | amethyst_shard |
| | source_jar | recommended | crafting | crafting table | `jugcraft:nouveau/source_jar` | archwood_planks |
| | Your first Source Gem | milestone | | | | imbuement_chamber, source_gem |
| Shared charge with technology | ley_pylon | recommended | crafting | crafting table | `jugcraft:ley_pylon` | amethyst_shard |
| Transmutation without casting ★ | assayers_scale | required | crafting | crafting table | `jugcraft:assayers_scale` | - |
| | philosophers_stone | required | crafting | crafting table | `jugcraft:philosophers_stone` | assayers_scale |
| | transmutation_table | recommended | crafting | crafting table | `jugcraft:transmutation_table` (Dark Matter: `jugcraft:dark_matter`) | - |

`python3 tools/encyclopedia_progression.py` regenerates `src/main/resources/jugcraft/progression_routes.json` and prints
the full derived table.

## Connections

- Existing input producer: the recipe, tag and loot data the routes are derived from; the Engineer's Handbook pages,
  codex categories and Encyclopedia lessons they link to; the server's live recipe manager, Jug Nouveau's loaded recipes
  and today's town sales.
- Existing output consumer: none. This is guidance only.
- Technology and Magic connection: each has its own route; the Ley Pylon step names the shared JE-to-Ley Charge bridge.
- Reachable entry path: each route starts with steps that need nothing on the route (ore, a glowing specimen, the
  workshop bench, the Assayer's Scale). The data test enforces that every connection points to an earlier step and that a
  required step needs only required steps, so no optional step becomes a hidden gate.
- Required vs optional: required (on the route's main line with the recipe shown), recommended (helpful; nothing on the
  main line needs it), alternative (another way: a different recipe or unpowered station, or another luminous specimen),
  and trade (only live town sales). Steel and the starter workshop are shown as parallel to electricity.
- No research gates, equipment locks, rewards, quests or currency. Nothing is granted, spent or recorded by reading.

## Implementation

- `tools/encyclopedia_progression.py`: hand-written selection; derivation; `LANG`; `generate(write)` (called from
  `starter_guidance.generate`, which `generate_material_data.py` already runs) and `lang(entries)` (called from
  `encyclopedia_routes.lang`). `tools/encyclopedia.py` gains the two page ids and the Alternative weight;
  `tools/data/generated-material-resources.json` lists the new generated file.
- `guide/encyclopedia/ProgressionRoutes.java` (common): reads the file; writes the `route_technology` and `route_magic`
  pages, one step row per step in the file's order; Jug Nouveau steps are read from `NouveauRecipes`.
- `PageWriter`: furnace, Jugcraft machine and starter-workshop recipes now draw as grids (`Grid.SMELTING`, `MACHINE`,
  `WORKSHOP`; one slot per item used); `absent()`; `MAX_GRIDS` 12 -> 32. `EncyclopediaPayload.EdgeKind.ALTERNATIVE`
  (appended, ordinals unchanged).
- Client: `EncyclopediaRouteMap` (layout, canvas, tiles), the Map tab and details in `EncyclopediaRoutesScreen`,
  `EncyclopediaScreen.showStep`/`returnTo`, counted item rows, process grid names and the fork mark in
  `EncyclopediaWidgets`.

## Multiplayer and persistence

No new packet, codec field, saved data or registration. The request/answer path, its rate limit and its page and row
bounds are unchanged; the two pages fit those bounds (the data test checks rows < `MAX_ROWS` and grids <= `MAX_GRIDS`
from the Java). The client's map is static data from the mod jar; everything live comes from the server's pages. A
server whose data pack removes a route recipe shows that step as not available and draws no stale grid. Disabling Jug
Nouveau shows its Source Gem step as unavailable.

## Verification

Ran here (no Minecraft toolchain: Java 21 only, Fabric/Mojang hosts blocked):

- `python3 -m unittest scripts/tests/test_encyclopedia_progression.py` (`/usr/bin/python3` 3.13 and `/usr/local/bin/python3`
  3.11): 10 of 11 pass on the branch; `test_language_file_has_the_route_words` fails until the coordinator merges the
  language keys. With those keys applied to a temporary copy of the language file, all 11 pass on both interpreters.
- The repository's own `check_encyclopedia` and `check_encyclopedia_routes` audits (extracted from
  `tools/check_mod_data.py`, which cannot import in this sparse checkout), with the language keys applied: 0 errors.
- Java syntax check of every touched and new Java file: 0 errors.

Written, not run: `ProgressionRoutesGameTests` (server: registry, live recipe manager outputs/stations/ingredient counts,
alternatives and off-route recipes, Jug Nouveau, one step row per map step with live grids, a recipe-less server,
nothing taken, codec round trip) and `EncyclopediaProgressionClientGameTests` (key, Technology Map tiles, keyboard
selection, Enter to the handbook entry and Esc back, Live recipe at the right topic and back, Magic Map lesson link and
back, 320x240 bounds, screenshots). `EncyclopediaGameTests.aNewcomerSeesOnlyTheFirstSteps` now expects the two route
pages. Compilation, the full suites and rendered review are Codex's to run.

## Rollout and open questions

- Not a pan/zoom canvas: the map is a scrolling, grouped column of tiles, readable at 320x240 but not a free-form graph.
- Vanilla recipes (copper, iron, furnace, vanilla loot) are named as vanilla items rather than drawn as route steps.
- The routes are curated starters, not every item; later tiers (oil, chemistry, electronics, schools) can be added as
  groups in the same file without UI changes.
- Optional quests and rewards remain out of scope: none exist for these steps, and none were invented.
- The item registry used by the data test is the November 2 game export (`docs/audits/survival-item-registry.json`).
