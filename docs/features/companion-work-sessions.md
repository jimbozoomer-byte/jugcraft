# Companion work sessions

Owner-requested companion scheduling improvement, implemented with OpenAI Codex (GPT-6), 8 October 2026. Extends the existing server-authoritative goal system, Fabric inventory transactions and shared server search/path budgets. No new dependency, asset, packet, recipe, unlock or saved-data format.

## Behavior

Garden actions release the completed cell's lease while retaining the assigned plot as the active job. On the next tick the worker checks the same bounded plot (at most eight cells) for another live planting/harvesting action, under the existing search budget. The crop state, inventory, permissions and cell reservation are validated again. There is no saved queue of stale crop actions. Action duration, hoe durability and energy charges are unchanged.

A productive session postpones ordinary same/lower-priority supply/output trips for at most 400 ticks (20 seconds at 20 TPS), or until only one empty cargo slot remains. This collects a useful harvest batch before leaving the garden. A plot with no ready work, full inventory, or another genuine blocker releases normally. Higher-priority delivery, urgent hearth tending, needed tools, food, exhaustion, schedules and owner commands retain their existing gates. Higher-priority productive jobs are still reconsidered at the existing bounded cadence. Assignment priority is not replaced with round-robin fairness.

The shared station routine retains movement ownership for up to 40 ticks during short idle, missing-input, missing-power, missing-heat or search-budget handoffs. It stops the work animation and does not perform productive work or charge work energy during that wait. Continuous cooking-pot, cutting-board, cider-press and processor work already stays active while ready; these jobs now also bridge short batch/resource gaps. Full generators/flywheels, blocked paths, unavailable claims, permission failures and unloaded/replaced blocks are not held. A failed handoff returns to the budgeted scheduler rather than waiting forever.

After a completed delivery, seed pickup or tool equip, transport keeps the companion stationary while requesting a search grant, for at most 40 ticks. It rechecks urgent oven tending, tools and assigned jobs in configured priority order. A productive station resumes work; otherwise it plans the next physical supply/output trip or porter batch. Hearth output can therefore lead directly into collecting the next raw pie and fuel, without an idle wander. Oven single-pie capacity, baking times, carried-item visuals, real inventories and transactional transfers remain unchanged. Delivering seeds invalidates the plot's idle scan delay immediately. Skillet, stove, kettle and other logistics adapters use this same delivery handoff.

## Server bounds and failure behavior

Optional idle sitting/sleeping also reconsiders assigned work at the existing priority-check cadence. A ready job can end a leisure break; `canWork` still blocks this while recovering, out of energy or off shift. Mounted workers leave their seat/pot before requesting a walking path from the safe exit, avoiding false blocked routes from an elevated mounted position.

Known job transitions (a finished plot or a newly missing input/heat source) wake the transport planner without bypassing its search budget. An empty-cargo delivery handoff yields when the companion needs a meal and has none carried, so continuous transport cannot starve food-search goals that share its priority.

Route-only porters in Work mode may collect food near their assigned containers, within the same existing radius as workstation workers. They no longer require a separate workstation assignment for that food-access check.

No global cooldown or server budget is reduced. Searches and paths retain their shared server limits; plot selection inspects at most eight saved cells. Delivery continuation reuses the bounded four-job/container/route planner once a grant is available. Idle jobs retain their normal staggered scan intervals. Short handoff deadlines and ordinary transport deferral are runtime-only state; reload resumes existing real cargo/assignments using the established lifecycle. No chunks are loaded and no inventories are remotely transferred. Player commands, permission checks and resource validation remain authoritative.

## Validation

Focused runtime coverage lives in `PeepoWorkSessionClientTests` (both companion variants, consecutive seed planting, consecutive hoe harvest/replant, deferred batch output, Stay interruption, idle-seat resumption, single/two-helper processor input gaps, crank full/restart behavior, and furnace/press/kettle supply-work-output) and the extended `PeepoDeliveryClientTests` (two consecutive oven batches without becoming idle between delivery and restocking, plus eating between porter batches). Existing delivery and hearth regressions cover conservation, blocked outputs, interruptions and saved cargo.

On 8 October 2026, the final `runClientGameTest -PclientTests=PeepoWorkSessionClientTests,PeepoDeliveryClientTests assemble` run passed: 45 session assertions and 60 delivery assertions, with a successful build. Evidence: local `build/companion-sessions-complete.log`. The companion hearth regression separately passed 10 groups / 159 assertions (`build/companion-sessions-tests.log`); the pot/cutting-board regression passed 1,096 assertions (`build/companion-sessions-final.log`). An initial idle-seat regression and a route-only porter meal regression exposed the issues corrected above; the final run includes both fixes. Tests use disposable flat integrated-server worlds on the pinned Minecraft 26.3 / Fabric stack. Dedicated two-client and large-server load testing were not run.
