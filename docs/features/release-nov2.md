# November 2 release preparation

Candidate: 0.1.0-alpha.20261102.1, based on the pending audit, visual cleanup, harvest/board-games, pets, server utilities and custom NPC batch. Owner authorized publication and server/client deployment on November 2. No unrelated open PRs are included.

## Validation and staging
- Previous focused evidence is recorded in the individual feature ledgers; it is not a full-suite pass.
- Repository documentation check and dependency/platform pin check passed.
- Canonical generation exposed a stale wall-tag patch precondition: 213 generated diagonal walls were added since that patch was recorded. Updated its exact before/after snapshots after checking that no prior wall was removed; retain the four shellstone repairs and all diagonal walls.
- Full canonical regeneration and release checks are in progress. No new release has been published and no client channel has been promoted.
- Modrinth currently reports Server Node Unavailable / circuit breaker open. No live files have been changed or restart performed.
- Explicit restart confirmation remains required after artifacts and backup are ready, followed by the full 30-second player warning.

## Held scope
Town-square cornucopia/decor transition remains held for owner approval after Halloween; November joke events are not included. Known visual/source limitations remain in the feature ledgers. Do not claim full collection completion from this release.

Prepared with OpenAI Codex; custom NPC implementation includes Claude collaboration as recorded in its feature ledger.

Release validation follow-up: canonical generation completed after preserving the native building importer and reconciling the wall-tag patch. All 30 packaging/updater contract tests passed and all 26 pinned libraries verified. The full Foundation run exercised 315 tests and exposed five failures: new-food essence records, grilled-salmon finishing parity, the expanded rendered-texture fixture, wearable provenance pointing at the superseded October 23 archive, and a legacy chain recipe ID. Corrections are being validated; this is not a passing full-suite claim. The texture fixture expansion was traced to 397 food-serving aliases plus three vanilla terracotta materials, with source PNGs checked. Modrinth recovered and the old server remains online; all ten manual backup slots are occupied.
