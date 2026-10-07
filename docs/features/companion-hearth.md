# Companion Hearth Oven automation

Owner-requested on 7 October 2026, implemented by OpenAI Codex (GPT-6) on `peepo-companion` from `bb2974a0`. Extends the existing planner, ghost recipe slots and [Supply/Output transport](companion-jobs.md#supply-and-output). No new recipes, dependency, assets or progression gates.

## Setup

1. Assign a Hearth Oven with the Companion Planner, plus separate Supply and Output containers. Leave a reachable standing space beside the oven and one free companion cargo slot.
2. Shift-right-click Peepo/Jughead and click the oven's ghost slot with a whole raw or baked pie. The cursor item stays yours. The slot displays the baked result and its raw-pie/fuel requirements. Supported fillings are apple, pumpkin cream, cranberry, sweet potato and chestnut; slices and unrelated items are rejected.
3. Supply the matching **prepared raw pies** and fuel: tagged hearth logs, charcoal, coal or coke. The oven does not craft raw pies from pastry/fruit/sugar. Coal blocks cannot fit its 3,200-tick fuel bank and are excluded.
4. Peepo checks Output has room before bringing a new pie. It loads one pie and supplies fuel when needed. Fuel is requested only for a real unfinished pie, targeting at least 600 banked burn ticks, rounded up to whole fuel items and limited by the oven's capacity. An empty oven is never continually refueled.
5. Once the loaded oven can heat/bake, the companion stays alongside it with the existing general two-arm interaction clip. When baked, it takes the pie directly into reserved physical cargo before walking to Output. A full chest after loading does not leave the finished pie cooking: it remains safely in cargo while delivery retries.

Right-click/empty-cursor clearing of the ghost slot stops supplying new pies. A pie already inside can still receive fuel, finish and be collected. Changing the plan does not replace or reset the current pie. All companions sharing the oven see its saved selection. Manual insertion, fuel and collection continue to work.

## Timing, interruptions and energy

The oven retains its original heat, fuel and burn rules. It holds one pie, heats to 100, starts baking at 50, finishes at 600 baking points and burns at 1,200. At sustained maximum heat, baking takes 15 seconds and another 15 seconds burns it, at 20 TPS. No speed bonus, free heat, remote collection or automatic cooling tray is added.

An active hot/finished pie is considered before ordinary workstation-priority transport, among at most four assigned workstations. Pending carried deliveries remain physical and finish/return first. Only one helper claims tending at a time; its UUID lease is refreshed while traveling/watching and expires after 100 ticks if abandoned. A new raw-pie/fuel delivery transitions directly to tending when heat is available, retaining the reachable standing position. An unheated pie releases tending so fuel can be fetched. Matching task priority applies again after the pie is collected.

An empty cargo slot is reserved while watching, excluding it from meal stocking/container returns. Collection rechecks the actual inventory in case the player fills that slot. Collection and cargo insertion commit in one Fabric transaction, with up to **16 JE** charged per pie collected; ordinary transport costs up to **2 JE per moving tick**. Waiting has no continuous energy charge and uses the normal passive/food recovery rules. Normal meals, shift changes, exhaustion, orders and access changes still interrupt the task.

Burning remains possible if the helper is removed, goes off shift, cannot reach the oven, loses access or has its cargo filled by the player. A resumed helper can collect a burnt pie as the real burnt result; it never converts it back. Keep storage close and avoid starting several manually loaded ovens with one helper if collection deadlines overlap. This implementation does not promise uninterrupted production while a worker is unavailable.

## Multiplayer, storage and saves

The companion-only input/output ports expose selected raw pies and valid fuel as inputs, and baked/burnt pies as outputs. Raw pies are never extracted as ordinary results. Output capacity probing rolls back; arrival rechecks recipe, need, target identity, loaded state, assignment and current storage permissions. The oven's burn/pie/progress and cargo changes are transactional. There is no public hopper/pipe registration or fake player.

Tending adds a bounded pass over the existing four work links within the existing search admission. It reuses transport path budgets, loaded-only checks, 64-block leg limit and travel deadlines. Waiting checks route validity every ten ticks and revalidates immediately before collection, without reopening external storage every waiting tick. At most one output view is inspected while tending. There is no extra oven ticker, global worker scan, chunk ticket, packet type or spawned helper entity. Arrival gives an 800-tick tending deadline; a stalled task releases/backoffs instead of holding indefinitely.

The optional `companionPie` field saves the filling's stable string id. Existing worlds default to no automatic pie supply and retain their old fire, heat, pie and baking counters. The transient tending lease is not saved; the actual oven pie and companion cargo retain their existing saved storage and are rediscovered/resumed after reload. The existing transport manifest remains the only delivery record and never recreates items. Menu size and slot indices are unchanged; its recipe flag now distinguishes Cooking Pot and Hearth Oven tooltips. Update both client and server.

The general INTERACT animation is reused without new props, textures or clips. Existing owner library files remain unchanged. The implementation uses the already-approved Fabric transfer APIs and existing vanilla menu/goal/rendering infrastructure.

## Validation

Common/client compilation and assembly, followed by launcher packaging. No automated tests or in-game checks were run, following the owner's instruction. Manual checks remain: each pie/fuel, rejected coal blocks and slices, ghost-item safety, cold/hot oven loading, fuel starvation, full Output, player-filled cargo, simultaneous helpers/manual use, interrupted tending and burnt results, recipe changes during trips, save/reload, locks/claims, unload/reload, dedicated-server concurrency and server-load measurements. Compilation is not gameplay verification.
