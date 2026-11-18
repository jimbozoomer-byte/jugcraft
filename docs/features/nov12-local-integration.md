# November 12 local integration

OpenAI Codex integrated the Claude Code review deliveries into the existing dirty `feature/nov7-priorities-local` checkout without committing, changing main, releasing or touching the live server. Claude reported Opus 5.5; it could not compile Minecraft in its cloud environment. These are local validation results, not player or server acceptance.

## Sources and preservation

- Texture regression: `8846560a5b`.
- Backpack/rope geometry: `2dff5e6011`.
- Aircraft banking helper and regression: `008ff48c6a` (no sign reversal added).
- Guest expansion and followup: `8d8800f3c7` through `fe782095be`, compared with reconciled snapshot `4357b8fe4c`.
- Aircraft server-test registration from coordinator `afbcbb26e5`.

Three-way file integration preserved existing local work; touched pre-integration files are backed up under `build/nov12-integration/before`. Shared test entrypoints were merged without duplicates, and language changes were merged by key. Guest roster conflict was resolved to the reviewed adult-only eligibility; held characters retain friendship activities. No unavailable identity was guessed. Historical TODO counts are not treated as current model defects.

## Validation results

- 19 focused Python tests passed: reported texture families, geometry holes, aircraft banking, guest skins/outfits (`build/nov12-integration/python.log`).
- Four existing model-finishing tests passed (`build/nov12-integration/geometry-existing.log`).
- Repository/document checks passed (`build/nov12-integration/repository.log`).
- Common, client and game-test Java compilation passed (6m25s); runtime results are below (`build/nov12-integration/compile.log`).

Storage-cell LED tint and remaining visual candidates are separate unresolved findings, not repaired by these patches.

The first combined server run found two failures: an old proposal test still allowed a held character to marry, and a race timeout incorrectly granted affection/completion rewards. Codex aligned the proposal assertions with the adult-only roster and made unfinished race timeouts end without rewards or completion milestones; the regression also checks that the daily reward remains unused. The next run exposed a reward fixture that incorrectly used race expiry as success; it now moves a racer to the finish. The final full server run passed all 3,795 required tests (`server-final.log`, 6m8s).

Full art audit passed: 38,798 block/item models, 3,786 quad parts and 184 combined models; existing allow-list entries remain. This does not replace rendered inspection.

Rendered client validation covered GuestClientGameTests, GuestWardrobeClientGameTests, AircraftFlightClientGameTests and NewestCollectionClientTests. The guest client regression now explicitly selects a friendship-only guest and opens the new Activities panel.

Client first pass: aircraft controls/banking/F5/sound-resource assertions and imported-model suite reached completion before the guest suite failed on an obsolete single-button OPTION assertion. The baked-model check covered 837,276 states / 742,135 distinct models. Adapted the assertion to feature-option slots. Screenshot review found Activities overlapping the proposal hint; moved it beside the Companionship heading and added a bounds assertion. Both guest suites passed after adapting the test; a final rerun after the layout fix also passed. Aircraft instruction HUD clipping at 854x480 is recorded in TODO. The first-run screenshots are retained under `build/nov12-integration/client-review/first-run`.

## Final local outcome

- All **3,795 required server tests passed**, including the integrated guest systems and aircraft regressions (`server-final.log`).
- Rendered aircraft and imported-collection suites completed in the first client run before the guest assertion failure. Only the affected guest suites were rerun; both passed, then passed again against the final UI layout and bounds assertion (`client-guests-final.log`, 3m5s). This is four selected client classes, not the entire repository client suite.
- The final client checks cover menu/errand clicks, gift help, the small GUI, Activities opening, proposal/divorce confirmation, cooldowns, friendship-only eligibility, the guest skin gallery and normal/winter/normal wardrobe switching.
- Visually reviewed biplane left/right banks, backpack side, Activities menu, representative normal/winter guest outfits and the corrected main menu. Final screenshots are in `build/nov12-integration/client-review/guest-final`; first-pass aircraft/model evidence remains in `first-run`.
- All 23 focused Python checks, compilation, repository/document validation and the full art audit passed. Repository check was repeated successfully after documentation updates.

Remaining: storage-cell LED tint; unavailable dedicated originals recorded by the texture worker; broader visual review of connected tables, fountains, Christmas bulbs, bathtub taps and storage-drive/color-applicator geometry; aircraft help HUD wrapping at small widths. All 128 guest skins passed asset checks, but not every seasonal skin/animation/lighting combination received visual review. No claim of public-server performance or human playtest acceptance. No commits, pushes, releases, personal launcher changes or live-server actions were performed.
