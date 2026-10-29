# Living spawn city — local integration

Owner-directed 28 October 2026. OpenAI Codex implements commissions, public interactions and preservation; the attached Claude session implements NPC routines, dialogue, animations and additive existing-block decorations. Work stays local pending combined validation and owner playtest. No live server or client release changes.

## Implemented locally

- A craftable City Commission Board with four illustrated supply cards: bakery wheat, watch arrows, Styx starflowers and workshop copper wire. No required progression unlock.
- Partial deliveries from ordinary main-inventory items only; named/modified items and worn gear are excluded. Delivery preview, explicit collection and bounded 1/1/1/2 Jugs rewards. No new currency. The watch's payout is below the shop cost of its arrows, preventing a buy-and-deliver profit loop.
- Personal quotas renew after a completed claim on the next 72,000 elapsed-world-tick cycle (60 running-server minutes). Sleep and changes to the sun clock do not reset them. Partial deliveries and unclaimed rewards carry forward. Joining/leaving parties does not reset quotas.
- Party totals recognize actual deliveries by current members whose first delivery was made in that party. They do not give free rewards to non-contributors. Each member keeps their own quotas/rewards. No shared-party quota implied.
- Claim receipts and Jugs balances share one SavedData serialization. Repeated claims, full purses and clock rollback are refused. Normal world-save consistency applies to inventory versus world data; not a crash-proof cross-file inventory transaction.
- Seasonal decorations preserve modified cells and persist their exclusion from later seasonal passes; inventories and custom block entities remain untouched. Plain authored banners can still change seasonally.
- Additive upgrades avoid the working area surrounding owner casino furniture. Casino menus remain usable with their own ownership/bet validation; storage/tanks remain private and personal ender chests remain usable.

## NPC and decoration half integrated

- Imported the attached Claude session's local patch (`685387096f9b50b006bf0c96f285ad58c74ea463`, patch SHA-256 `6c04eaa9b9846ca57dbbdc2a70120a95361da64eb18da04447f78e2eacccbba8`) without committing or pushing this checkout. No extra agent was spawned. See [NPC implementation](living-spawn-city.md).
- Residents work during the day, gather in the evening and go to bed after midnight. Blocked beds have a bounded fallback to resting; guards remain on duty and innkeepers offer essential supplies after shop closing.
- Job-specific gestures and dialogue reuse the existing resident models and skins. Names, resident indices, beds and equipment are preserved.
- Revision 4 adds 63 authored-air cells: existing Jugcraft benches, cafe furniture, produce crates, greenery, lanterns and the commission board at town-relative `104 1 112` (nine tiles extending east and upward, facing north). The entire church plot is excluded from additions.
- The frozen revision-3 blueprint matches the pre-change file byte-for-byte (SHA-256 `7b63d91925c6c8dc446e587c05d823ef6f4058d40963339c88019eb16db9f1b5`). `TownUpgrades.CURRENT` is now 4. Existing worlds use explicit `/jugcraft town upgrade preview` and `apply`; never regenerate the city. Live cells differing from the old blueprint are preserved, including objects occupying a proposed decoration's former-air cell.
- Preservation compares actual block states and protected entities; it cannot identify a player's edit that is exactly identical to the previous authored state. The new blueprint deliberately changes no church cells.

## Validation

Validation is local on Minecraft 26.3, Fabric Loader 0.19.5 and the pinned dependencies, with Java 25.0.4.1. The checkout is `feature/living-city-local`, based on `455feb9ec1c9907ece307d695954f6ba5c9ebbf8` (the same source tree as upstream `a5803a77e4dadb17559cc416b21ddb495b824b2f`), plus the uncommitted changes described here.

- Repository structure/documentation validation passed.
- `python tools/check_mod_data.py` passed: 8,039 material IDs, data files and recipes; art checks covered 24,517 block/item models, 3,733 quad parts and 180 assemblies (`build/living-city-data-final.log`). The check initially caught the board's missing feature ownership, old loot-condition spelling and near-coplanar notices; all corrected in the source generators/runtime data.
- All 12 `test_town_architecture.py` tests passed (`build/living-city-architecture.log`). The revision-2 roster comparison now accepts added routine metadata while requiring every old field to remain identical.
- The first full native run completed 3,105 tests with one failure: the new chair loop reused the first seat's entity before its next-tick cleanup. The fixture now disposes only its own temporary seat between replacements. All commission and routine tests passed in that run (`build/living-city-combined-build.log`).
- The expanded client gallery initially used the old HUD API; corrected to 26.3's `client.gui.hud`. Idle routine gestures also now yield when a guard has a combat target.
- The corrected `gradlew.bat build --no-daemon` passed: **all 3,105 required native game tests**, including both halves, old-town upgrades, visitor interactions, crafted seating, owner preservation, schedules and delivery/replay/save/party cases (`build/living-city-validated-build.log`; 28 October, 20:50 local time). The initial failed run remains recorded above; it is superseded by this successful run.
- The packaged local JAR's board model, corrected loot table, revision-4 blueprint, frozen revision 3 and shop data match their source resources byte-for-byte.
- `runClientGameTest -PclientTests=LivingCityClientGameTests,TownClientGameTests` passed in disposable test worlds (`build/living-city-client.log`): commission delivery/collection synchronization, job-pose gallery, late-night essentials and complete town views. Both suites completed successfully, with 25 screenshots (28 October, 20:55). This was before the owner-requested full-size noticeboard redesign below. The owner controls their personal playtest.

## Asset reuse

The noticeboard uses vanilla oak planks, stripped oak, spruce framing and brick textures. The final owner-requested notice sheets use white/cream/blue vanilla block textures with modeled ink rules and copper/red/gold pins; the earlier rolled-parchment texture was replaced after rendered review. All geometry is authored in `tools/town_commissions.py`, with nine clipped world models and a separate full-assembly inventory model. No new raster images, external downloads or dependencies. The GUI retains Jugcraft TradeLook walnut, brass and slate panels with keyboard-accessible Plate controls.

## Full-size noticeboard revision — owner feedback

Replaced the miniature freestanding model with a real 3 × 3 × 1 block assembly: oak backing and posts, spruce framing, stepped brick roof and six pinned paper notices, inspired by the owner reference. One item places all nine tiles; any panel opens the existing commissions menu. Breaking a tile removes only its matching partners. All four horizontal directions are supported. Placement checks bounds, replaceability and claims across the full footprint. The recipe uses bricks, oak planks and oak fences.

Moved the board to the Town Hall market frontage at `114 1 110`, facing west into the square (footprint x114, y1–3, z108–110). The city generator keeps approach space clear. Upgrades preflight all nine cells before placing any tile, preserving owner edits and nearby casino furniture. This remains unpublished revision 4, with no regeneration or live-world edits.

Validation after the redesign:
- All eight commission/noticeboard native tests passed (`build/living-city-noticeboard-server.log`): four orientations, every part resolving to its root, no orphan panels, occupied upper-corner placement refusal and exactly one item returned by breaking a side panel, plus the delivery/party/save/public-interaction cases.
- All 13 town architecture tests passed, including the new nine-tile footprint, clear approach and retirement of the random old position (`build/living-city-noticeboard-architecture.log`).
- The full data/recipe audit passed for 8,039 IDs; art checks passed for 24,526 models, 3,733 quad parts and 180 assemblies (`build/living-city-noticeboard-data.log`). After that audit, the paper styling changed; targeted geometry/UV reference inspection found no overlapping faces or holes in all ten revised models.
- Both rendered town/commission suites passed (`build/living-city-noticeboard-client.log`). The first render led to the flat-paper visual adjustment above. The next visual review caught a bad vanilla texture reference (`smooth_sandstone` is a block, while the texture is `sandstone_top`). Corrected it and verified every referenced texture exists in the pinned 26.3 client archive. The corrected rendered town test passed (`build/living-city-noticeboard-render-verified.log`), with no noticeboard missing-texture warnings. The unobstructed reference gallery also passed (`build/living-city-noticeboard-gallery-final.log`); test walkers are briefly parked away from the camera and immediately restored. The final screenshot was visually inspected and saved as `build/living-city-local/noticeboard-final.png`. For this one texture-only rerun, the exact generated noticeboard assets were restaged into the already-processed resources, and `jar runClientGameTest -x processResources -PclientTests=TownClientGameTests` avoids repeating the full resource scan. All 13 final model/state/recipe/town resources were verified byte-for-byte against the local JAR (`build/living-city-noticeboard-jar-final.log`); SHA-256 `9e55038371c491a1bfed000ec7c20ae06047c10dd119d2a50d5f53c1bdfd2310`.
- The earlier full 3,105-test combined run remains the baseline; it was not rerun in full for this scoped visual/multiblock adjustment. No human multiplayer or live-world playtest is claimed.


## Follow-up: stall access, currency balance and aspen foliage

Owner feedback supersedes the earlier market-frontage placement: it blocked a stall even though the board itself had a clear approach. The board now stands on the **south edge of the square at 104 1 112, facing north**, footprint x104–106/y1–3/z112. This is outside the stalls, the south road and the church plot. The furniture planner reserves three clear approach rows. Revision 4 remains unpublished, now 63 authored-air additions; no live-world changes.

Economy anchor: **1 gold ingot = 1 Jug**. The General Store previously bought four gold for eight Jugs; now four gold pays four Jugs. Bulk crop sales (wheat, carrots, potatoes, beetroot, cane, berries, cocoa and melon slices) pay one Jug per 64. Other renewable/mining buy prices are reduced to match; shop selling prices and existing balances are preserved. No diamond exchange is added: the owner has not settled 25 versus 50 Jugs. Commission rewards are 32 wheat → 1 Jug, 32 arrows → 1 Jug, 8 starflowers → 1 Jug and 16 copper wire → 2 Jugs. Quotas renew on 72,000 elapsed-world-tick boundaries, so all four requests award at most five Jugs per fixed server-hour cycle, not a rolling one-hour limit. Partial/unclaimed deliveries, party credit and replay protection are preserved. Ordinary shop sales remain repeatable, and long-term inflation under large automated farms still needs live economic monitoring; these rates do not claim to solve unlimited currency issuance.

Aspen keeps its green/gold/bare saved block states, but the winter/bare state now resolves to the existing green foliage model instead of bare twigs. Gold autumn foliage stays. Generator and runtime blockstate are changed together, with no block ID migration, new texture or world regeneration. Original texture files remain intact.

Validation for this follow-up (OpenAI Codex; no additional agent delegation):
- 13 town architecture tests, 56 tree regression tests and two focused economy/aspen resource checks passed. Source/runtime prices agree, the 1:1 gold anchor is asserted, and all three saved aspen states select foliage textures.
- Eight commission/multiblock native tests passed (`build/living-city-balance-server.log`), including the one-Jug wheat payout, five-Jug total/hour quota, replay/save/party cases and occupied-footprint protection.
- Eight existing native town tests passed with the new shop prices, followed by both TownClientGameTests and LivingCityClientGameTests under the owner's BSL 10.1.8 archive in a disposable test instance (`build/living-city-balance-bsl-combined.log`). Shop transactions and commission menu synchronization passed. The relocated board image was visually inspected and saved to `build/living-city-local/noticeboard-square-edge-bsl.png`.
- The first foliage gallery showed only proper leaves, but placement had applied today's season to some samples. The fixture now explicitly reapplies each seasonal state after onPlace and asserts it, with random ticking disabled; the final repeat is recorded below.
- Repository/link checks passed after correcting one new TODO relative link. The earlier log records that resolved documentation error. No personal profile, live server, GitHub or updater changes.

- Final corrected LivingCityClientGameTests BSL run passed (`build/living-city-aspen-bsl-final.log`, 28 October 22:54). The explicit green/gold/bare sample gallery was visually inspected: green/gold/green foliage, no twig/web variant. Snapshot: `build/living-city-local/aspen-foliage-bsl.png`. Singular one-Jug UI labels also corrected. Final local JAR source/resource comparison passed; SHA-256 `93fbb0328a362a25b294f2a89398f0c1c7c581f41b91c3fc879698ce27f85b12` (`build/living-city-balance-jar-final.log`). This does not resolve the separate intermittent black-line report.


## Shared shader compatibility follow-up

Fixed GeckoLib's Windows emissive pipeline lacking Iris entity/shadow associations, after reproducing the exact missing-pipeline error from the owner's log. The local adapter uses the pinned public API, retains existing mappings and stays unloaded without Iris. BSL 10.1.8, Bliss 2.1.2 and the optional-integrations-absent day/night rendered checks all passed. The original live black streaks still require affected-scene confirmation: the baseline reproduced the error but not the exact visual symptom. Details and logs: [rendering investigation](rendering-lines-oct28.md).

This supersedes the earlier pre-adapter JAR hash: SHA-256 `700c56b439c85545f3c283af4f4d9e02fe1071697bcb53e24e069480f313513d`. Adapter class, client entrypoint and current aspen/shop resources were verified inside the local package. Everything remains local; no personal profile, server, release or updater changes.
