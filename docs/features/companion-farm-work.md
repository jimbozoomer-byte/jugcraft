# Companion farm work and storage

Owner-requested 29 October 2026, implemented with OpenAI Codex (GPT-6). This extends existing companion garden zones, logistics, permissions and work budgets. No new assets, dependencies or progression gates.

## Squash and gourd gardens

All registered `GourdBlock` / `GourdStemBlock` families use existing garden zones and Supply/Output, including Turban Squash, Blue Hubbard Squash, Sweet Dumpling Squash, Long Island Cheese Pumpkin, Spoon Gourd and Swan Gourd. Select either the fruit or its seeds in the garden filter. Supply fetches actual matching seeds for empty selected or remembered planting positions; planting consumes one. Leave soil beside stems for fruit to grow.

Only fruit attached to the matching native stem is harvested. Both the fruit and stem must belong to the assignment and pass permissions; standalone decorative fruit is left alone. Picking preserves the mature stem to regrow naturally. Native fruit loot goes into real cargo before the block is removed and then follows the existing Output route. Full cargo leaves the fruit intact. Existing crop search limits, worker claims and energy costs remain in effect; there are no new world scans.

The lit Woodstove is included in the existing `peepo_companion:harmful_when_lit` tag. Both companions avoid its damaging top through the same path and work-position checks as the Kitchen Stove. An unlit Woodstove remains usable.

## Farm storage

Apple Bins (256 fruit), Picking Bags (16 fruit), Orchard Handcarts (128 fruit), Corn Cribs (256 corn), Feed Troughs (128 horse-food items) and Firewood Racks (16 logs) expose their real saved contents through the shared Fabric item-transfer API. Allowed items, item components, total capacities and native save keys are retained. Transfers update visible fill only after commit; aborted/nested probes restore contents without changing the display. The same access serves companion Supply/Output, stock counting and porter routes.

The Planner normalizes every part of a bin/cart/crib/trough to its master, avoiding duplicate endpoints and self-routes. Companion access checks permissions on every part; missing parts or unloaded neighboring chunks refuse transfers. The bounded native structures are checked directly, with no chunk tickets or global scans. Handcarts remain stationary storage, and troughs do not acquire new animal-feeding behavior. Grain Sacks, Milk Cans and decorative Apple Crates remain decorations.

## Quern and Butter Churn

Assign either station with the existing Planner. Both use the shared nine-item Filter, optional production stock targets, linked Supply/Output and the existing general two-arm work animation. One helper claims the station at a time. Work costs 4 reserve JE per working tick, uses native 10-tick stroke spacing, and preserves unfinished native progress across rest, cancellation and saves. Idle/full/blocked work costs nothing. Native manual interaction remains available.

Their inset bodies are navigation obstacles, so Supply/Output paths go around the equipment instead of trying to walk through its partial collision shape. Work uses safe adjacent ground positions, including a one-block-lower floor where appropriate.

- Quern: up to 16 of one compatible ingredient. Two native turns produce one Flour from Wheat, or two Paprika from one Dried Chili. Blank filters allow both; a product filter restricts incoming ingredients. A supplied batch is worked before ordinary output hauling interrupts it.
- Butter Churn: one Milk Bucket at a time. Filling returns one real Bucket to the output tray; eight native strokes produce two Butter. The bucket and butter use the usual bulk Output delivery. Whole two-butter batches can exceed an odd target by one.

Finished goods and returned buckets have a saved tray capped at 64 total items, not a new companion inventory. A full tray blocks work or new input. Transactions cover native input/progress, products and energy; same-tick duplicate calls do not advance work twice. Stock counts include stored outputs, the tray, loaded raw ingredients and deliveries already underway. Shift-empty-hand use retrieves tray contents, and breaking a station drops its remaining tray contents once. Existing station saves simply start with an empty tray and unlimited stock settings.

## Focused validation

The crop suite is `PeepoFarmCropsClientTests`: registered-family and filter compatibility, mismatched/decorative fruit, real autonomous seed pickup/planting/harvest/delivery for both companion variants, mature stem preservation, full cargo, zone boundaries and lit/unlit Woodstove safety. Results are recorded after the run; no human multiplayer playtest is implied.

29 October crop result: `build-local.ps1 -Tasks @('runClientGameTest','-PclientTests=PeepoFarmCropsClientTests')` passed **170 assertions** in a disposable flat world, including autonomous Peepo and Jughead routes. Evidence: `build/peepo-farm-crops-tests.log`. Only that client test entry point ran; the initial restricted attempt could not connect to Gradle's local daemon, and the normal local run succeeded.

Storage suite: `PeepoFarmStorageClientTests`, covering all six stores, capacities, allowed items, nested rollback, visible fill, native persistence, exact component variants, master aliases, incomplete structures and autonomous porter routes for both companions.

29 October storage result: `build-local.ps1 -Tasks @('runClientGameTest','-PclientTests=PeepoFarmStorageClientTests')` passed **124 assertions**. Evidence: `build/peepo-farm-storage-tests.log`. The first two runs exposed a test fixture issuing Work while its owner was a spectator; the corrected fixture asserts Work mode before enabling AI, and both autonomous trips passed.

Hand-work suite: `PeepoHandFarmClientTests`, covering native inputs, companion-menu Filter access and ghost selection, milk bucket conservation, rollback, same-tick effort/stroke deduplication, insufficient-energy rollback, worker exclusion, saved progress/output, revoked assignments, paprika yield and autonomous stock-target production/deliveries for both companions.

29 October hand-work result: `build-local.ps1 -Tasks @('runClientGameTest','-PclientTests=PeepoHandFarmClientTests')` passed **91 assertions**. Evidence: `build/peepo-hand-farm-tests.log`. The initial autonomous quern trips exposed paths through its inset collision shape; the final run includes the quern/churn pathfinding fix and both menu checks, and completes all four companion/station delivery combinations. Quern stock stops at three Flour; churn stock stops at four Butter and returns both Buckets while leaving the spare Milk Bucket in Supply.

All three focused suites passed, **385 assertions** in total. `python scripts/check_repository.py` passed repository structure and documentation-link validation; `git diff --check` passed. The full modpack game-test suite and a populated multiplayer server were not exercised.
