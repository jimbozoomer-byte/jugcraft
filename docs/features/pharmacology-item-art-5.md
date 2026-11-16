# Pharmacy Art Batch 5 — formula plates

Implementation tool: **OpenAI Codex (GPT-6 family)**, owner-directed continuation, 4 November 2026. The Minecraft 26.3 / Java 25 / Loader 0.19.5 / Fabric API 0.161.0+26.3 toolchain and framework pins remain unchanged.

## Delivery

The [selected batch](../plans/pharmacology-item-art/batches.md#art-batch-5--formula-plates) gives **all 32 formula plates** dedicated 16×16 textures: the existing blank and 31 named formulas. It adds no Moonshine formula; that basic route retains its existing progression. Each named plate carries its finished drug family's palette and large symbol. All cards have the same whole, notched slate-gray portrait form, a lit silver upper border and a colored lower band. The blank has an empty diagram and no family band. XR cards have a double upper border and two bead marks; the Fentanyl Patch card has a window and peeled-corner mark instead of the ordinary Fentanyl bars. No tiny drug-name text is needed to distinguish them.

Each card has an editable `tools/item_icons/pharmacy_<id>.txt` map, a dedicated `assets/jugcraft/textures/item/pharmacy/<id>.png` and native item model/selector. `tools/pharmacy_art_catalog.py` assigns the exact Batch 5 IDs. `tools/pharmacy_item_art.py` extends its existing canonical writer/texture hooks to Batch 5 and gives plates a restrained 0.5 first-person scale in both hands. All earlier 114 sprite bytes remain unchanged. The shared writer now owns **146 distinct flat item sprites**, covering every flat item in the 147-row pharmacy matrix; the remaining row is the existing 3D Dream Study Plinth, assigned to Batch 6 review.

The scope is item art and its native views. No production Java, recipes, item registrations, language, drug effects, costs, yields, unlocks, tool authority, saved formats, network messages or dependencies change. Plates remain non-consumable reusable tools. HUD icons, nine block/model reviews and final consistency work remain Art Batch 6; the vision-effects polish and original Clockwork Batch 7 are still separate pending work.

## Shared systems and provenance

This extends the [Batch 1](pharmacology-item-art-1.md) and [Batches 2–4](pharmacology-item-art-234.md) editable pixel-map system. Family glyphs/palettes come from those established designs; the existing approved steel/iron ramps supply the slate-gray body and silver diagram/borders. No material or checker exemption was added.

The shared owner library/catalog, magic collection guide and item-icon rules were consulted. Actual references inspected were `art/owner-library/originals/Blocks/blueprint.png` and the previous `assets/jugcraft/textures/item/general_chip_pattern.png` alias. The approved notched portrait-card design and per-family diagrams are newly authored maps rather than copies of those landscape/generic forms. Reference/source originals, the generic chip sprite and unrelated items remain unchanged. No Mojang/library pixels were copied or traced, and no program from the owner library was executed. The enlarged preview labels/fonts are outside Git and are not embedded in the game textures.

## Machine interpretation

The Analyzer still turns the existing sample plus a paid blank plate into a named formula. The Press and Sterile Filler retain their matching formulas. **The Release Coater still uses the existing Release Control Die**, not a newly invented formula requirement; coated cores/patch stock are analyzed and their formulas are used at the downstream Press. The native review exercises those existing roles and distinguishes regular Fentanyl from its patch formula, and regular Caffeine from XR.

## Validation

Verified locally on branch `codex/pharmacology-caffeine`, base `f02bccb606c17a4305d5355a6ab6f75736da6c27`, preserving the owner's existing uncommitted work. **All 32 maps pass the shared icon checker with zero errors and zero warnings.** Exact assignment coverage, 146 unique sprite images and 96 refreshed resources (32 PNGs, 32 models, 32 item definitions) pass. Source/runtime bytes match. All 114 earlier PNG hashes remain unchanged. Every one of the 31 named formulas has its existing Analyzer-output and downstream tool references, and the relevant runtime recipe data equals source. The reference check covers all pharmacy, sterile, precision, controlled-release, perception and beverage branches.

`PharmacologyFormulaArtClientGameTests` compiled and **passed in 6m 56s**, producing **52 native screenshots**. It renders every plate held, mixed inventory at GUI scales 1 and 2, a regular/XR/patch comparison inventory and item-frame wall, and all 32 after native resource reload. It confirms each registered plate remains non-consumable. The test opens paused input/tool screens and selected native recipe pages, then actually completes five paid jobs with exact outputs and retained tools:

- Analyzer: `pharmacy_analyze_caffeine` → one Caffeine Formula Plate from its existing sample and blank.
- Press: `release_finish_caffeine_xr` → four Caffeine XR, retaining its XR formula.
- Sterile Filler: `precision_fill_fentanyl` → four Fentanyl, retaining its ordinary formula.
- Release Coater: `release_coat_fentanyl_patch` → four patch stock, retaining the existing Release Control Die.
- Press: `release_finish_fentanyl_patch` → four finished patches, retaining the distinct patch formula.

The fixture supplies the existing recipe inputs/fluids and replenishes test energy as needed; it does not bypass the machine's native job, paid-input or retained-tool checks. The plates' native inventory/stack overlays, comparison frames, selected recipe pages, machine slots, output views and post-reload rendering were reviewed. Enlarged individual sheets, old/new comparisons, light/dark slots at actual 1×/2× and greyscale were also reviewed. The Masterwork formula uses its established amber crown; tiny symbols are simplified deliberately at 16 pixels.

The exact local runner task arguments were:

```text
runClientGameTest -PclientTests=PharmacologyFormulaArtClientGameTests -I ../early-pharmacy-focused.gradle -x processResources -x compileClientKotlin --offline --max-workers=2
```

They were passed as the existing `build-local.ps1` string array. `processResources` was skipped in favor of the byte-verified 96-file canonical overlay; unrelated current resources were retained. The first helper import lacked its existing DejaVu font-directory environment setting; setting `JUGCRAFT_FONT_DIR` to the local installed font directory fixed that preparation step before the successful check. Its initial log is preserved. No gameplay assertion failed. The whole catalog generator/data audit, release packaging and 94-test headless pharmacy suite were not rerun for this asset-only batch; five representative real production jobs ran in the rendered client instead.

Evidence is outside Git at `../pharmacy-art5-2026-11-04/`: `evidence/screenshots` has all 52 native captures, and `evidence` contains exact logs, the 96-resource manifest and final validation output. Source/evidence SHA-256 indexes identify this delivery. `formula-plates-1.png` and `formula-plates-2.png` show all 32 finished sprites; `previews/<id>.png` and `textures/<id>.png` provide individual enlarged and exact 16×16 PNGs. `32-formula-plate-textures.zip` exports every finished plate. `old-and-new.html` shows the prior shared alias beside each new design. Changed documentation links and scoped source-whitespace checks pass.

Common color-vision simulations, shaderpack combinations, optional-integrations-absent startup, a separate full JEI review and human multiplayer were not performed. Existing recipe pages retain their current text-only reusable-tool line; the new plate art appears in native machine tool slots and item-bearing recipe inputs/outputs. No live server, existing Pharmacy Flat Test world or owner client was stopped, replaced or restarted, and no release JAR was published. A running local client can reload resources to pick up this texture-only change. Batch 6 and deferred vision/Clockwork work remain pending.
