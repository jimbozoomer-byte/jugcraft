# Powered-equipment PR validation repairs

Owner-requested follow-up to PR #609 on November 3, 2026, implemented with OpenAI Codex (GPT-6). This repairs the submission's generated-data and test-harness failures without changing the approved equipment balance, dependency pins or live installation.

## Reported failures and corrections

- Foundation's 335-test Python run found a stale encyclopedia index and atlas texture fixture. The new scanner chapter must appear in the generated encyclopedia, and the Efficiency Module's texture must be included in the resolved-material fixture used by the native atlas test.
- The mod job stopped at generated-resource consistency. Regeneration updates the scanner index, canonical handbook/language serialization and language ordering. The held-pose generator normalizes rounded zero translations so Windows and Linux cannot emit different signs for an equivalent zero.
- Client shard 6 completed the equipment gallery before the existing tree-batch-6 test rejected a small Cold Desert occurrence without a rare juniper. Returning no survey location lets its caller try the remaining bounded regional anchors. The caller still requires all twelve natural biome stops, and each chosen stop still has to contain the expected foliage. No tree is planted at survey sites and production generation is unchanged.
- Client shard 5 deadlocked while closing the designed mineral-sand world. Its thread dumps show the render thread waiting in native `IntegratedServer.halt`, the server parked at Fabric's phase barrier, and the test thread waiting for shutdown. Current main already contains `SingleplayerShutdownMixin` and its client configuration, but the test manifest did not register that configuration. The manifest now enables the existing client-only adapter; native player removal, saving and completed shutdown remain required. The adapter is not shipped in the mod.
- The optional-integrations-absent job failed before compilation because a dependency repository timed out. A fresh CI run retries the same pinned dependencies; no version or assertion is changed to work around that transient download failure.

Upstream main was fetched and inspected at `a279a133ba`, already an ancestor of the equipment branch. There was no newer main commit to merge. The existing shutdown adapter is reused directly.

## Validation

The original implementation and distribution evidence remains in the [balance](powered-equipment-rebalance.md), [held-equipment](held-equipment-poses.md), [armor](powered-armor-feedback.md) and [night-vision](night-vision-metering.md) records. These repairs are checked separately; previous results are not represented as new CI results.

Submission checks and observed CI results are recorded after execution below. No human multiplayer, production benchmark, live-world migration, deployment or restart is included. No item IDs, recipes, save schema, worldgen rules, assets, platform versions or progression entry paths are added by this follow-up. JE generation/charging and the existing equipment/mining consumers retain their approved behavior.

- Repository structure and documentation links: passed.
- Pinned-toolchain game-test compilation: passed in 1m 4s, using the previously documented local compiler init and prepared-resource exclusions.
- Canonical guide/language outputs: all three files match the generated Git blob hashes from failed Linux job `126223602262` in run `41723924456` exactly: encyclopedia `f4f29287ee`, handbook `fe11bfb32c`, language `f22657365a`. The partial local regeneration was discarded before applying those exact generated changes. The local whole-resource pass was stopped after an extended run; it is not claimed as passed. The fresh Linux CI run remains the regeneration check.
- Pose stability: all 23 profiles serialize identically with opposite near-zero floating-point perturbations, and rounded zero translations have one canonical sign. The client-only registration resolves to the existing shutdown adapter.
- At submission, the two previously failed data assertions, the full Python suite and the mineral-sand/tree client regressions are still running locally. The encyclopedia assertion has passed. The designed mineral-sand world now saves and closes, and the first natural seed passed all four depleted-position, inventory and remaining-reserve checks after reopening; this is partial progress, not a claimed complete client-suite result.

The native client run reuses verified committed runtime resources from `5b550dbd63` and the repaired test fixtures. The prepared-output reconciliation copied 1,855 changed resources and removed 41 obsolete resources relative to its earlier `235524e0c8` preparation, verifying every copied payload. It uses `runClientGameTest -PclientTests=MineralSandClientGameTests,TreeBatchSixClientGameTests` with the documented compiler init, offline cache and `-x processResources -x processClientResources`; it does not read the concurrently regenerated source files. The fresh CI run will validate the submitted resources together.
