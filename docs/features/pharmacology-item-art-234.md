# Pharmacy Art Batches 2–4 — advanced drugs, supplies and processing materials

Implementation tool: **OpenAI Codex (GPT-6 family)**, owner-directed continuation, 3–4 November 2026. The pinned Minecraft 26.3 / Java 25 / Loader 0.19.5 / Fabric API 0.161.0+26.3 toolchain and all framework dependencies remain unchanged.

## Delivered scope

This is the historical Art Batches 2–4 record. [Art Batch 5](pharmacology-item-art-5.md) continues the same canonical writer with all 32 formula plates.

The [selected batches](../plans/pharmacology-item-art/batches.md) add **97 distinct item textures**: 15 advanced finished drugs/alcohols, 32 ingredients/packaging/study supplies and 50 processing materials/fluid buckets. Together with [Art Batch 1](pharmacology-item-art-1.md), the canonical writer now owns 114 individual pharmacy sprites. The original 17 PNGs remain byte-identical. Formulas, HUD icons and the final machine/model consistency review remain Art Batches 5–6; vision-effects polish and original Clockwork Batch 7 are separate pending scopes.

`tools/pharmacy_art_catalog.py` lists the exact stable IDs per batch; these were checked against the existing assignment CSV. Each has an editable `tools/item_icons/pharmacy_<id>.txt` source and a dedicated `assets/jugcraft/textures/item/pharmacy/<id>.png`. `tools/pharmacy_item_art.py` renders the maps through the established palette/renderer and owns native item models/selectors. Its existing hooks in the pharmacy, material-data and texture writers cover all four delivered batches and prevent earlier generic aliases from reclaiming them. No new item registry, backend, rendering dependency or custom sprite renderer was introduced.

Batch 2 uses family-colored longer XR capsules, a peeled-corner Fentanyl patch, a small perforated LSD paper dose, distinct botanical/psychedelic doses and five alcohol bottle silhouettes. The capsule and paper sprites use restrained first-person scale. Patch activation retains the existing stable non-drinking use animation; alcohol remains a drink. The other medicines retain their existing eating/capsule use.

Batch 3 separates open starch/feed packets from sealed carriers, empty shells from closed finished capsules, two/three-layer matrices, blank backing from finished patch, culture dish from vial, die from circuit module, and three whole handled-jug states. Study papers retain eye/diamond/cog identities; the Folio has a spine and clasp. Natural resin, mushroom, fruit, mycelium and seeds use soft plant surfaces rather than hardware outlines. Existing crop registration, block textures, harvests and the Coffee Plant/Bean chain are unchanged.

Batch 4 keeps each drug's family palette and large symbol while changing the physical stage: crimped blend pouch → corked bulk stock jar → verified-active weighing boat → finished medicine. Exposed XR cores show layers and no capsule shell. Prepared patch sheets, substrate and oneiric prism have their own forms. Beverage concentrates are broad-neck process vessels; conditioned Masterwork has a solid seal/gold tie and unconditioned stock an open seal/grey tie. The three process fluids retain whole bucket silhouettes with separate contents/process marks, and remain usable existing fluid buckets.

## Packaging and gameplay

The sole production-recipe change in these batches is `perception_finish_lsd`: **four paper replace four Botanical Capsule Shells** as the paid packaging for four LSD paper doses. The existing four verified actives, 250 mB water, reusable LSD formula, 180 ticks, 96 JE/t, machine slots, capability and feature gates are preserved. This is a fictional Minecraft formulation, not real manufacturing guidance. The canonical handbook explains the paper route; THC/Psilocybin/Mescaline retain botanical shells and DMT retains advanced empty capsules.

LSD and the other capsule doses keep 0.8 seconds and no returned bottle. The Fentanyl Patch keeps 1.6 seconds, non-drinking activation and no bottle. Each of the five alcohols keeps its 1.6-second drink and one paid empty-glass return. Existing effects, onset, duration, strain, tolerance, family replacement, yields and profile authority are unchanged. Stable IDs, save/network formats and existing player worlds are preserved. No injury condition, drug effect or side-effect mechanic was added.

## Asset provenance

The shared library/catalog, magic asset guide and existing pharmacy/natural icon rules were consulted. Actual references inspected for this continuation were:

- `art/owner-library/originals/Blocks/farming and food textures/corn_seeds.png` — seed-cluster scale and natural rendering.
- `art/owner-library/originals/Blocks/biomes and tree blocks/tiny_cactus.png` — complete natural plant proportions.
- `art/owner-library/originals/Magic/assets/jymbaquary/textures/item/empty_potion_vial.png` — small glass vessel form.
- Existing `assets/jugcraft/textures/item/jimtafacts/civil/mushroom.png` — the earlier mushroom alias, inspected as a comparison.

The delivered artwork is newly authored in the established editable map system. No library or Mojang pixels were copied or traced; reference originals and existing generic texture files remain unchanged. No program from the supplied library was executed. Existing named pharmacy ramps are reused, together with the approved iron/copper/gold ramps for actual metal hardware. Declared open gaps belong to the slat frames and jug handles; declared soft bare edges belong to natural harvested surfaces. No checker rule was weakened.

## Validation and evidence

Verified locally on branch `codex/pharmacology-caffeine`, base `f02bccb606c17a4305d5355a6ab6f75736da6c27`, preserving the owner's existing uncommitted work. Compilation passed (`compileGametestJava`, 50 seconds). Canonical writers generated **615 bounded runtime resources**, with source/runtime bytes checked; this includes all relevant models, selectors, textures, recipes, language and book pages. The full catalog generator/data audit and release packaging were not rerun.

**All 94 required native pharmacy server tests passed** at 23:54 on 3 November. These include the existing test that actually runs all 98 formed-machine item-output recipes, the new consumption/animation/return checks for all 15 Batch 2 products, and the four-paper LSD recipe's exact output, timing, power and retained formula. Existing shared dosing and effects regression checks also pass.

The complete `PharmacologySupplyArtClientGameTests` run passed (`BUILD SUCCESSFUL in 6m 52s`) with **124 native screenshots**: all 97 items held, the 15 new finished forms actually used, exact one-dose/zero-or-one-glass checks, all 32 finished formulations together, supplies/materials/chain views, an actual four-dose LSD Press job and native resource reload. This run also showed two presentation issues in the review fixture: hover tooltips covered inventory rows, and the three process buckets were shorter than vanilla's form. The final sprites correct only those three bucket silhouettes; the other 94 new textures and all 17 Batch 1 textures are byte-identical to the first run.

The final bounded client review passed at **00:06 on 4 November** (`BUILD SUCCESSFUL in 5m 1s`), producing **15 final native screenshots**. It moves the cursor clear of the inventory, captures the corrected buckets in hand and beside vanilla's bucket in item frames, shows the 32 finished formulations and complete supply/material/chain inventories, captures the real Press paused with paid active/paper inputs and after its four-dose output, and checks resource reload. Final inventory, frame, input/output and post-reload views were reviewed. Its temporary helper initially failed before testing because it named a task before registration; switching that helper to lazy task selection fixed the setup. That failed helper log is preserved separately. No gameplay assertion failed.

The 97 new editable maps have **zero icon-check errors** and eight reviewed qualitative tiny-part warnings for corks/caps and small granules; all 114 delivered maps have zero errors and 14 such warnings including Batch 1's six. No lint rule was weakened. All 114 sprites are distinct 16×16 RGBA images with dedicated canonical model mappings. Assignment coverage is exactly 15 + 32 + 50, and all original 17 texture hashes remain unchanged. The enlarged, actual 1×/2× light/dark-slot, family and greyscale sheets were reviewed. Tiny geometric motifs are deliberately simplified at native pixel size rather than relying on text or branded pill imprints.

The pinned local runner used the existing focused init script and explicitly refreshed resources, avoiding a needless whole-catalog regeneration:

```text
build-local.ps1 -Tasks compileGametestJava -I ../early-pharmacy-focused.gradle -x processResources -x compileClientKotlin --offline --max-workers=2
build-local.ps1 -Tasks runGameTest runClientGameTest -PpharmacyTestFilter=jugcraft-test:pharmacology* -PclientTests=PharmacologySupplyArtClientGameTests -I ../early-pharmacy-focused.gradle -x processResources -x compileClientKotlin --offline --max-workers=2
build-local.ps1 -Tasks runClientGameTest -PclientTests=PharmacologySupplyArtClientGameTests -I ../early-pharmacy-focused.gradle -I ../review-pharmacy-art234.gradle -x processResources -x compileClientKotlin --offline --max-workers=2
```

These lines describe the runner task arguments; in PowerShell pass them as the runner's string array, as in the preserved logs/helpers. The final helper enables the test-only `jugcraft.pharmacy.artInventoryOnly` capture mode and forces compilation of that revised capture fixture. No helper is a production dependency. Repository structure/local Markdown checks passed, with changed-document links and source whitespace checked again for the final delivery.

Evidence is outside Git at `../pharmacy-art234-2026-11-03/`: **139 native screenshots** across `evidence-first/screenshots` and `evidence-final/screenshots`, plus exact logs, resource manifest and source/evidence SHA-256 indexes. `batch-2-1.png`, `batch-3-1.png`, `batch-3-2.png`, `batch-4-1.png` and `batch-4-2.png` show every finished texture; `previews/<id>.png` and `textures/<id>.png` provide individual enlarged/exact sprites. `97-pharmacy-textures.zip` contains the 97 exact PNGs. `old-and-new.html` presents the preserved previous sprites beside the delivered ones.

The existing complete development client was exercised; common color-vision simulations, shaderpack combinations, optional-mod-absent startup, separate full JEI recipe-view review, human audio review and two-human-client playtesting were not performed. Native machine-screen coverage is a representative paper-dose Press job; all 98 formed-machine recipes are covered by the headless server check. No live server, existing Pharmacy Flat Test world or owner client was stopped, replaced or restarted. No release JAR was published. Restart a running local test client to load the new textures and recipe data. Art Batches 5–6 and deferred vision/Clockwork work remain pending.
