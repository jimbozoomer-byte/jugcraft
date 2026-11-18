# Pharmacy Art Batch 6 — status icons and strain pills

Implementation tool: **OpenAI Codex (GPT-6 family)**, owner-directed continuation, 4 November 2026. The Minecraft 26.3 / Java 25 / Loader 0.19.5 / Fabric API 0.161.0+26.3 toolchain and framework pins are unchanged.

## Delivery

The [selected art batch](../plans/pharmacology-item-art/batches.md#art-batch-6--hud-models-and-final-consistency) gives all **39 existing pharmacy effects** dedicated 16×16 icons. Regular and XR symbols share family motifs; XR has a contrasting bead corner. Existing Jitters, Appetite, Fatigue, Drowsiness, Interference and Recovery have separate silhouettes. The five beverage markers intentionally match their finished bottle silhouettes. This adds no effects, injury conditions, side effects or new gameplay rules.

The owner moved the strain-pill row back **above the hunger bar** on 4 November 2026. The existing Fabric HUD callback now adds ten horizontal capsules with the same eight-pixel spacing as vanilla health/hunger icons. Each nine-pixel footprint contains a seven-by-five-pixel capsule, one-pixel dark outline, top highlight and central seam. Hearts, armor, hunger, air, XP, hotbar and the selected-item title retain their vanilla rendering and positions. The row follows the right-hand hunger/vehicle-health column, moving above visible air bubbles and mounted health rows; the selected-item title and native action-bar message lanes receive clearance when necessary. This is a small additive Fabric HUD element, not a replacement UI or new screen framework.

## Reading the pills

Pills fill from left to right, ten strain per pill. Small pixel increments show fractional decay. Above a full row, the next layer paints over the previous full row; falling strain removes the newest layer first.

| Actual strain | Base under the newest fill | Newest fill |
| --- | --- | --- |
| 0–100 | Empty dark capsule | Ivory |
| Above 100 through 125 | Full ivory | Amber |
| Above 125 through 150 | Full ivory | Orange |
| Above 150 through 200 | Full ivory | Red |
| Above 200 through 300 | Full red | Violet |
| Above 300 | Full red | Full violet; exact strain remains in the readout |

The server's existing thresholds stay inclusive at 100, 125, 150 and 200. While strain is positive, the numeric readout and current severity appear above 100 and while a formulation is held. Holding a drug also shows its server-authored next cost/total and a patterned ghost fill; the tooltip retains exact predicted severity and the combination-cost explanation. Patterned pixels are preview only, not already accumulated strain. Saturated bars retain their exact numeric forecast. At zero, the row shows ten empty pills for three seconds after depletion, then hides even if a benefit remains active or a drug is held. Starting at zero or holding/changing a drug at zero does not reveal it or extend that delay. The empty grace row has no ghost fill/readout; next-dose forecasting remains available in the tooltip. This monotonic presentation timer resets on disconnect and changes no saved or server clocks. F1, death, spectator and creative hide it with the survival HUD. Visual Off does not hide this gameplay meter or change penalties.

No network packet, dose, cost, recipe, timer, tolerance, decay, save migration or penalty behavior changes. The existing private `StrainStatus` feed remains the only source for the display, including combination surcharges. The client writes no drug accounting. The handbook describes the new meter.

## Sources and regeneration

- `tools/item_icons/pharmacy_effect_<effect>.txt`: all 39 editable status maps, using the existing named pharmacy material ramps and shared map renderer. Native status glyphs use a dark filled contour and colored strokes, documented explicitly in their map directives; open centers are intentional negative space. Beverage silhouettes retain their item-object outlines.
- `tools/hud_icons/strain_pill.txt`: the separate 9×9 native-HUD capsule source. It is not a 16×16 inventory item.
- `tools/pharmacy_hud_art.py`: canonical effect rendering and six-row/six-fill-stage 54×54 pill atlas. `pharmacy_item_art.generate_textures()` calls it last, after the older effect-alias writers and at the end of both main generators. Later regeneration therefore retains the new effect symbols. Java's preview uses the same explicitly checked palette.
- `src/main/resources/assets/jugcraft/textures/mob_effect/<effect>.png` and `textures/gui/strain_pills.png`: runtime assets.
- `StrainPillHud` renders the atlas and bounded patterned preview through native GUI extraction; `StrainClient` retains packet registration and tooltip handling.

This extends the delivered family artwork. It imports no owner-library, Mojang or third-party image pixels; suitable owner-library references and shared coffee/plant inputs were inspected in the earlier art batches. Source originals and unrelated shared icons remain intact. The nine block items retain their complete existing 3D models and display transforms. Native inventories, held views aimed away from placement targets and an empty-hand item-frame gallery confirm complete readable models. The intentional multiblock placement preview is not part of the held item model and is excluded from the frame review. Shader/vision polish and the original Clockwork dimension/trading batch remain deferred.

## Validation

Verified locally on `codex/pharmacology-caffeine`, base `f02bccb606c17a4305d5355a6ab6f75736da6c27`, preserving the owner's uncommitted work. **All 94 pharmacy server tests passed.** The focused native client checks pass for the final positive-strain HUD, actual item use/decay/combination accounting, all 39 status symbols and the zero-strain follow-up.

The native review covers exact layer boundaries and fractional/overflow math; actual ivory/empty capsule pixels; unchanged hearts, hunger, XP and hotbar regions (two channel levels allowed for native background-vignette interpolation); next-dose previews; decay; armor, extra maximum-health and real absorption hearts; clearance around selected-item and action-bar text; GUI scales 1/2/3; F1/spectator/creative visibility; and native resource reload. Death visibility also has a direct predicate check. The new zero-strain test consumes Caffeine, runs the real decay function until its strain empties while the benefit remains active, photographs empty pills during the three-second grace, changes the held drug during that interval, and confirms hiding afterward. Tooltip forecasts remain correct and a new dose reveals the row again. This is a presentation-only change to zero visibility.

All **39 registered effect symbols** are present in the seven native inventory-sidebar gallery captures. These are explicitly isolated client-only art fixtures: the real server correctly removes orphaned conditional side-effect markers when their dose is absent. The gallery checks assets and native presentation, rather than inventing a gameplay combination that keeps every marker active together. The separate 94-test server suite and actual-consumption client checks cover gameplay. All nine whole block items were reviewed in native inventory, held views and frames; no model geometry, texture, display camera or scale change was needed. The full 146-sprite catalog, stack overlays and shared Coffee/crop/input comparisons have native inventory captures. Earlier batch records retain their machine-tool, recipe-screen and production evidence; no manufacturing data changed here.

The 39 dedicated RGBA PNGs are distinct, 16×16 and equal to their canonical editable maps. The capsule atlas equals its source and Java forecast palette. Regeneration is byte-stable; all **648 bounded source/runtime resources** match. All **146 earlier item PNG hashes remain unchanged**, with exact coverage of the 147-entry matrix including the plinth. The shared checker reports zero errors and four advisory warnings for the retained two-pixel stopper details on the beverage silhouettes. Light/dark, 1×/2×/4×, greyscale and full-severity protan/deutan/tritan review sheets were inspected across the catalog and effects. The linear-sRGB simulation tables were checked against the [author-maintained colorspacious implementation](https://github.com/njsmith/colorspacious/blob/master/colorspacious/cvd.py); these are review aids, not an accessibility certification. Shape, seams and XR corners remain important where colors converge.

The existing local runner used these task arguments, with the pinned toolchain:

```text
runGameTest runClientGameTest -PpharmacyTestFilter=jugcraft-test:pharmacology* -PclientTests=PharmacologyHudArtClientGameTests,PharmacologyStrainClientGameTests -I ../early-pharmacy-focused.gradle -x processResources -x compileClientKotlin --offline --max-workers=2
```

The final positive-HUD/consumption pair passed in 6m28s; the complete conditional-icon gallery fixture passed in 5m21s. The owner then requested zero hiding: `PharmacologyHudZeroClientGameTests` passed in 4m26s with the same task settings and its selected class. `processResources` was replaced by the byte-verified bounded canonical overlay; Java compilation ran. Only the canonical strain chapter was refreshed for the zero-hiding follow-up, with the rest of the handbook byte-preserved. The initial strict image comparison caught a one-channel background-vignette difference; the corrected comparison retains a two-level tolerance and explicitly checks actual capsule pixels. Native inspection also caught and fixed pill/readout overlap with the existing held-drug action-bar message. Frame captures clear the player's hand to avoid the unrelated placement preview. No game rule was loosened to make conditional icons remain active.

Evidence lives outside Git at `../pharmacy-art6-2026-11-04/`: **50 art captures, five actual-consumption captures and six zero-visibility captures**, with logs and SHA-256 indexes under `evidence/`. `pill-hud.png` compares native states; `zero-delay.gif` assembles native stills to show the three-second empty-row interval. `effect-icons-labeled.png` shows every symbol with player-facing names; individual enlarged PNGs are under `previews/`, and `39-effect-icons.zip` contains the exact textures. Full-catalog, greyscale and color-vision sheets and the 648-resource manifest are retained there too. Changed documentation links and scoped source-whitespace checks pass.

The whole-catalog generator/data audit, optional-integrations-absent startup, shaderpack combinations and manual multiplayer were not rerun. This is local implementation, not a release/deployment. No live server, existing Pharmacy Flat Test save or owner client was stopped, replaced or restarted. Relaunch the local client to load the new HUD code; Survival/Adventure displays it with the vanilla hearts. F3+T alone can refresh art but cannot load the new Java code. Vision polish and Clockwork dimension/trading remain deferred.

## Hunger-bar placement follow-up

On 4 November 2026 the owner requested the row back above hunger. OpenAI Codex (GPT-6 family) moved its left anchor to the native hunger column (`guiWidth / 2 + 10`), mirrored its readouts to the right and removed the old dependency on left-hand health/armor rows. Visible air bubbles and mounted-health rows reserve their own native space; selected-item and action-bar text remain clear. Capsule artwork, fill direction, colors, forecasts, the three-second empty-row grace and all server rules are unchanged. The canonical handbook and current plans describe the new position.

The existing `PharmacologyHudArtClientGameTests` and `PharmacologyHudZeroClientGameTests` pass after recompilation (`BUILD SUCCESSFUL in 5m30s`). Native pixels confirm the pill row at the hunger anchor; the vanilla hearts, hunger, XP and hotbar comparison still passes. The underwater capture shows the pill row above actual air bubbles. The zero-strain test again confirms the three-second grace, tooltip forecast and hiding even with an active benefit or changed held drug. GUI scales, native resource reload and the catalog gallery also pass. Mounted-health clearance has direct layout checks against the pinned vanilla formula. This client-only placement change does not rerun the earlier 94-test server suite.

The 57 new native captures and exact log are outside Git at `../pharmacy-hunger-hud-2026-11-04/`; `hunger-hud.png` and `hunger-hud-underwater.png` show the new location. Earlier Batch 6 images remain historical evidence of the hearts placement. The canonical handbook matches its generator and runtime copy, changed documentation links resolve and scoped whitespace checks pass. No live server, owner client or saved test world was restarted or replaced.
