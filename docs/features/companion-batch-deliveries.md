# Companion batch deliveries

Owner-requested extension of the companion transport on `peepo-companion`, implemented with OpenAI Codex (GPT-6), 8 October 2026. Reuses existing Fabric storage transactions, server job/path budgets, inventory, workstation adapters and pie animation. No new dependency, unlock, item, recipe or client packet.

## Behavior

Companions collect multiple item types for a workstation in one physical trip, using all empty slots among their eight cargo slots. Existing personal cargo, meals, costumes and equipped tools are preserved. The old 32-item transport cap is removed: each collected stack can reach its item's normal stack limit, subject to the workstation's actual recipe needs and capacity. The eight-slot maximum is 512 items when all eight collected stacks can hold 64. A recipe delivery supplies the missing ingredients for the current recipe; it does not stockpile ingredients the adapter does not request.

A pickup batches the usable contents of the container being visited. Linked supplies remain physically separate visits; this does not remotely collect from other chests or add a multi-stop collection route. Ingredients spread across chests remain supported through successive trips. Cooking Pots now expand their request into balanced multi-recipe loads, as described in [bulk cooking](companion-cooking.md#bulk-cooking--12-october-2026). Generic porter routes also fill available cargo slots and continue to honor filters, Leave and Keep limits. Garden harvests already in inventory are grouped for delivery; seed fetching and tool pickup retain their specialized behavior.

The pickup planner simulates insertion of the whole batch, including combined output capacity, then rolls all destination changes back. Actual extraction and insertion into companion inventory commit together on the server thread. This allows one raw pie and its coal to be collected together, even if the chest lists fuel first: the simulated pie unlocks the oven's fuel requirement. The actual oven changes only when the companion reaches it. Unneeded ingredients are deferred while the remaining useful ingredients unload, then returned to their source. Turning Supply Off returns all collected ingredients. Blocked outputs retain the real cargo for retry.

Every in-flight cargo slot is reserved against eating or meal insertion. The original single-stack save fields remain supported; additional bounded Pending/Unused entries remember the other stacks and slots. No items are recreated from manifests. Real inventory remains authoritative; loading checks slot bounds, duplicate slot references and matching item components. Death drops the existing inventory as before. Invalidated assignments release the job reservations but retain items in inventory.

## Cutting-board batches — 26 October 2026

Implemented with **OpenAI Codex (GPT-6)**. The companion port accepts one matching stack, capped at 64 ingredients including the single visible workpiece. Additional pieces wait in one saved stack; every successful forty-tick cut advances the next piece. Existing Chef timing, bonus results, energy and knife wear still apply per cut. Output collection waits while the batch can keep working, including after the ordinary twenty-second work session, but higher-priority jobs, rest, missing tools and full output can interrupt it. A full result buffer is unloaded before work resumes.

Pickup simulation snapshots the queue alongside the visible workpiece and results. Production stock counts queued pieces as upcoming products and reduces pickup to whole recipes needed for the target. Empty-hand board retrieval returns queued inputs as well as finished products; breaking the board drops every real item once. Native player cutting continues to remove exactly one workpiece. Existing boards without a saved queue load empty. The queue adds no world search, background task, entity or per-tick inventory scan.

Focused coverage is in `PeepoCuttingBatchClientTests`: bounded pickup and rollback, save/load, native manual removal, full-output/tool interruption, target counts, and actual Supply-to-board-to-Output cycles for both companions, including batches longer than twenty seconds.

Validation on 26 October 2026, Minecraft 26.3 / Fabric Loader 0.19.5 / Java 25, using `52082f4f3` plus these local changes: `runClientGameTest -PclientTests=PeepoCuttingBatchClientTests -x processResources` passed **1,007 assertions** in the final run (`build/cutting-batch-verified.log`). The preceding combined run also passed `PeepoKitchenAnimationClientTests` (**1,256**) and `PeepoTaskDetailClientTests` (**2,494**). The batch-only rerun covers the final no-refill safeguard and failed-energy rollback. Native animation captures and the combined log are preserved under `build/cutting-brewing-evidence/`. Repository/documentation-link and whitespace checks passed. These used disposable integrated-server test worlds; no unrelated full-pack suite or multiplayer load test was run, and no distribution or player world was updated by this revision.

## Completed-batch delivery handoff — 31 October 2026

Implemented with **OpenAI Codex (GPT-6)**. Finishing a cutting-board or cooking-pot batch now leaves one short-lived hint for the transport planner to collect that workstation's finished goods next. The work routine keeps movement ownership while awaiting the existing search grant, for at most forty ticks. Reaching a stock target or filling the station's result buffer therefore cannot immediately send the worker to another job or a leisure seat before Output has been considered. Urgent hearth tending still wins; owner commands, recovery and normal work permissions still interrupt.

The hint is consumed by one normal, budgeted route-planning attempt. Output Off, a full or missing chest, removed assignments and permission failures use the existing route checks; no items move unless the companion reaches the station and the real transaction succeeds. A blocked delivery yields normally instead of indefinitely reserving the worker. The hint is runtime-only and stores one existing assignment plus a deadline. Search/path budgets, stock targets, batch sizes, inventory limits and energy costs are unchanged; no new inventory polling or saved queue is added.

Transport also waits for ground navigation to become ready after stepping down from a board or pot. A briefly airborne dismount no longer creates a false failed path and a ten-second route backoff.

Validation on Minecraft 26.3 / Fabric Loader 0.19.5 / Java 25: `PeepoCuttingBatchClientTests` passed **1,250 assertions**, `PeepoDeliveryClientTests` passed **59**, and `PeepoCookingBatchClientTests` passed **876**, with a successful build (**2,185 total**). The cutting regression observes the last-cut transition every server tick: collection must start within ten ticks and pickup within forty, before a ready lower-priority job or leisure. It covers both companions, ordinary/stock-target batches, counter-height dismounts, full/disabled Output, and successful delivery after restoring the endpoint. Existing conservation, stock accounting, rollback, batch-animation and shared delivery checks remain intact.

Evidence: `build/peepo-output-handoff-isolated-tests.log`. The focused `runClientGameTest -PclientTests=PeepoCuttingBatchClientTests,PeepoCookingBatchClientTests,PeepoDeliveryClientTests` run used a temporary init script with separate build outputs, project cache and disposable client worlds because another local build held the shared checkout's cache. `processResources` was excluded after those isolated resources had been fully prepared; `deleteGameTestRunDir` was excluded for the fresh isolated run directory to avoid touching the other build's test worlds. The earlier shared-cache attempt timed out before gameplay, and preparation/retry logs were retained. Repository/link and scoped whitespace checks passed. No full-pack suite, dedicated two-client/load test, distribution update or live-server restart was performed for this fix.

## Selected station batches — 31 October 2026

Implemented with **OpenAI Codex (GPT-6)**. The same loaded-batch and completion handoff rules now cover regular crafting tables, manual workshop benches, the standard item processors, querns, butter churns, cider presses and candy kettles. Existing Supply/Output links, Filter allow-lists, stock targets and the eight-slot companion inventory remain authoritative.

- Crafting tables and manual benches retain their existing maximum of eight recipes, finish the finite loaded batch before ordinary hauling, and collect its products and native remainders before beginning another supply cycle. Fresh pickups do not endlessly top up partly processed work; already-carried ingredients still unload normally.
- Standard item processors plan up to eight balanced recipes. Planning uses real available ingredient variants and components, native input space, and room for the result plus possible byproducts. Scarce ingredients and stock targets reduce the batch. The selected ingredient-to-slot mapping and quantity caps survive reload during a delivery; this guidance contains no additional inventory and is validated against native slots and recipes. Power, recipe time, installed tools, automation settings, helper limits and machine-based companion effort are unchanged. Special/fluid ports and expanded industrial forms retain their previous supply behavior.
- Querns keep their native sixteen-ingredient capacity. Churns accept up to eight real milk buckets across the current load and a saved queue; each load still requires its native eight strokes and produces two Butter plus its empty Bucket. Queued milk, tray contents and partially paid progress survive reload, participate in rollback and are recovered on removal.
- The cider press completes grinding/pressing, fetches real bottles, then delivers cider and pomace together where inventory space permits. It retains its native eight-apple/juice capacity; unavailable bottles or Output yield to other work normally.
- The candy kettle retains its native batch sizes, heating/pulling and passive setting/crystallization. Finished candy is handed to Output promptly; passive phases do not hold the worker at the kettle.

Jobs opt into the shared behavior with small readiness queries. A completion hint survives a physical finishing-supply trip or another output trip, and is revalidated against the current job before planning the next step. Urgent hearth tending still takes precedence. Rest, commands, permissions, blocked/full Output and unavailable ingredients retain the normal exits. Each hint expires after forty ticks if the existing scheduler cannot admit it; no recurring global scan, unbounded job queue or chunk loading is introduced.

Fresh pickups also respect the existing registered incoming deliveries when stock targets are disabled. The bounded reconciliation checks known couriers and cancellation records, preserves unloaded cargo and avoids scanning nearby entities. Standard processor batch search has one shared allowance of 8,192 candidate trials per preparation plus 256 for a single-recipe fallback, across the existing bounded recipe/source lists. Readiness checks do not run that search.

Focused regression coverage: `PeepoCraftingTableClientTests`, `PeepoWorkshopClientTests`, `PeepoProcessorBatchClientTests`, `PeepoHandFarmClientTests`, and `PeepoPressCandyBatchClientTests`, together with the existing cutting-board, cooking-pot and delivery regressions. Results are recorded after executing the final source; previous validation above applies to the earlier cutting-board/pot handoff.

Validation on 31 October 2026, Minecraft 26.3 / Fabric Loader 0.19.5 / Java 25, using `79277d1bf` plus these local changes:

| Focused client suite | Passed assertions |
| --- | ---: |
| `PeepoCraftingTableClientTests` | 2,886 |
| `PeepoWorkshopClientTests` | 3,310 |
| `PeepoProcessorBatchClientTests` | 6,668 |
| `PeepoHandFarmClientTests` | 1,174 |
| `PeepoPressCandyBatchClientTests` | 1,161 |
| `PeepoCuttingBatchClientTests` | 1,250 |
| `PeepoCookingBatchClientTests` | 876 |
| `PeepoDeliveryClientTests` | 59 |
| **Total** | **17,384** |

The combined selected run is recorded in `build/peepo-selected-batches-tests-2.log`. It passed the six other suites and an earlier processor revision before stopping on the new candy fixture's status expectation. A depleted worker correctly returns IDLE at job admission; the corrected test separately verifies that guard and RECOVERING from the native failed energy debit, with ingredients preserved and no remainder created. The final `PeepoPressCandyBatchClientTests` rerun passed with a successful build in `build/peepo-press-candy-batches-verified.log`.

The final processor revision, including persisted partial-delivery guidance and the shared search bound, passed separately with a successful build in `build/peepo-processor-batches-verified.log`. Its eight physical furnace/alloy cycles cover both companions and stock targets. Per-tick observations verify no partial-result hauling, actual assistance through the loaded batch, collection scheduled within ten ticks and pickup within forty when the helper is resident at completion. The surplus-source case verifies no mid-batch refill. Planning checks cover components, overlapping ingredients delivered in reverse order with a reload between them, in-flight reservations, rollback, automation sides, capacities and the bounded one-recipe fallback.

All runs used `build-local.ps1`, `runClientGameTest`, explicit `-PclientTests` selections, the isolated `build/peepo-handoff-gradle-cache` and `build/peepo-handoff-isolated.init.gradle`, and disposable worlds under `build/peepo-handoff-validation/run/batchJobsClientGameTest`. `processResources` was excluded because the isolated resources were already prepared and this change adds no companion assets; `deleteGameTestRunDir` was excluded to protect the concurrent build's run directory. Repository/documentation-link and scoped whitespace checks passed. No full-pack suite, dedicated two-client/load benchmark, distribution update, push or live-server restart was performed.

### CI expectation follow-up — 31 October 2026

Implemented with **OpenAI Codex (GPT-6)**. PR #593's companion client check exposed two older single-recipe expectations after the eight-recipe processor batching change. The automation regression now checks the exact eight-set ingredient limit, rollback during insertion probes, refusal of extra ingredients, and return of all eight real carried ingredients when Supply is disabled. Runtime behavior is unchanged.

Validation using `9aff1a79c` plus this test correction: `PeepoCompanionClientTests` passed **46 groups / 755 assertions / zero failures**, including physical transport, chunk unload/reload, and disposable dedicated-server menus/reconnect. The focused `runClientGameTest -PclientTests=PeepoCompanionClientTests` run used the same isolated build/cache and prepared resources described above, with `-x deleteGameTestRunDir -x processResources`, on Minecraft 26.3 / Fabric Loader 0.19.5 / Java 25. Build succeeded; evidence is in `build/peepo-ci-batch-fixtures-tests.log`. All other checks on the preceding PR commit passed. No full-pack suite, distribution update or live-server restart was performed locally for this correction.

## Energy and server bounds

While moving, transport spends `2 + max(0, stacks - 1) + max(0, ceil(items / 32) - 1)` JE per tick, based on the remaining delivery load. Empty trips and a single stack of up to 32 cost 2 JE/tick; a pie plus coal normally costs 3; two stacks totaling 128 cost 6; eight full 64-item stacks cost 24. Station work, rest, meals and regeneration retain their existing rules. Existing hearth loading/collection costs remain unchanged.

Batch planning runs only at physical pickup. It snapshots at most 128 source views and performs at most eight bounded passes, allowing dependencies such as pie-before-fuel without an unbounded search. Stock-limit totals remain bounded to 128 views and fail closed when incomplete. At most eight cargo entries are saved or processed, and delivery handles one entry per tick. Existing scheduled job searches, path budgets, loaded-chunk checks and fresh mutation-time permissions still apply. No background polling of every linked inventory is added.

## Validation

The focused `PeepoDeliveryClientTests` exercises batch planning rollback, eight-stack capacity, merging split source stacks, limits, real transport for both variants, personal/equipped item preservation, component-preserving manifest reload, full-output retention, mixed cold-hearth input, multi-ingredient cooking-pot supplies, redundant ingredients and Supply Off returns. It also checks actual load-scaled energy deductions, two competing helpers conserving 640 items, and full-stack garden output preserving replanting seeds. No two-client or large-server load claim is implied.

The accompanying hearth regression passed 10 groups / 159 assertions and the shared-supply regression passed 34 assertions (`build/companion-batch-tests.log`). The final garden-inclusive batch run passed 57 assertions, including actual energy deductions and competing carriers, and assembly succeeded (`build/companion-batch-verified.log`). Repository structure/link and whitespace checks passed. These were real client/integrated-server exercises in disposable flat worlds on Minecraft 26.3, Fabric Loader 0.19.5 and the pinned companion pack, on 8 October 2026. No player world or live server was modified; dedicated-server/two-client and large-scale load testing were not run for this change.
