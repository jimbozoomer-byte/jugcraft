# Companion jobs, budgets, status, lunch and schedules

Owner-directed stages 1-4 of the companion roadmap, implemented locally with OpenAI Codex (GPT-6) on `peepo-companion`. This extends the existing wheel, inventory, planner and rest behavior. [Cooking Pot assistance and recipe plans](companion-cooking.md) and the standard-processor pilot below extend that shared job system.

## Player use

- Shift-right-click your Peepo or Jughead to open its inventory. The Jobs panel retains home, four ordered workstations and their remove/priority buttons. It now includes a separate Lunch row.
- Select the companion with the Companion Planner, then right-click a lunch crate or the actual lunch cover block to assign its food source. Left-click with the planner, or use the Lunch row's x, to clear it. A lunch source does not consume a workstation slot. Existing loaded/dimension/reach and owner/party checks remain.
- Assignment text is colored by server status. Hover a row for its full location, dimension, priority and live status; the detail line also shows the hovered status. The left panel shows the companion's overall activity. Unimplemented machines explicitly report Unsupported job.
- Use the Routine button to switch the right panel to settings while keeping storage, costume, hand and player inventory accessible.
- Schedule cycles Auto, Day shift, Night shift. Auto preserves energy-driven work at any time. Day and Night use the server's Overworld clock even in other dimensions; night is ticks 13000-22999. Off-shift workers go home/rest. Sleeping remains night-only in the Overworld; other dimensions use sitting/passive recovery, avoiding an endless sleep in a fixed-dark dimension.
- Break at starts recovery at or below the chosen energy percentage. Resume at ends it. Defaults remain 0% and 80%. Break is adjustable from 0-70%; resume up to 95%, always at least 10 percentage points above break. Settings persist and are server-validated.
- Best meal favors the existing food regeneration quality score. Small meal favors lower-quality meals. Both apply to carried meals and lunch-container selection. Neither consumes the equipped hand or costume slot.
- Carry meals cycles 0-4 (default 2). With a bound lunch source, a companion can refill its eight cargo slots even when healthy. Player-supplied food also works; the reserve is a refill target, not a limit on what a player can give it.
- Food already carried is eaten before searching for loose food or lunch, under the existing health/energy/digestion rules. Meals still heal and give their existing energy regeneration buff; no stacking bonus was added.
- Bowls/bottles go back to a nearby source when possible, otherwise into cargo and back to the lunch source on a later visit. If cargo is completely full, the remainder drops rather than being lost. Full return destinations cause a 30-second retry backoff. Containers removed by a player are not recreated.
- Alerts default off. Enabled alerts play a quiet local chime on a new problem transition, at most once per 20 seconds. They do not send chat messages or poll remote players.

Follow and Stay retain their movement restrictions. A lunch assignment is not permission to abandon those orders or access somebody else's storage. An unavailable assigned source does not fall back to another lunch container; unassigned companions keep the bounded nearby-source behavior.

## Shared productive job contract

`CompanionJob` extends the existing station lifecycle and adds cheap readiness/status, a bounded productive work operation, and a worth-starting predicate. `CompanionJobs` resolves a loaded block directly or through explicitly registered adapters. The routine owns navigation, priority, claiming, travel timeouts, renewal and cancellation; the job owns its actual recipe/energy transaction.

The Generator Wheel is the first productive adapter. Its existing transaction still inserts exactly the energy debited from the companion, capped at 64 JE/t. The 128000 JE reserve, regeneration, food buffs, recipes and output faces are unchanged. A new visit requires at least 640 JE of free buffer so a draining near-full wheel does not cause constant tiny trips; an already occupied wheel keeps working while it can accept energy.

The existing per-station exclusive reservations remain the authority. Travel renews the claim; release occurs on food, orders, shift end, recovery, invalid target, failed path, removal or unload. Productive adapters must report the canonical controller position, remain available to their current claimant, expire stale claims, check their own resource/permission conditions, and return Working only after useful work commits. Future jobs must preserve power/fuel/input costs, output capacity, inventory components and recipe unlocks. Never call a whole block-entity tick twice to accelerate it.

## Standard-processor assistance pilot

Owner-requested on 7 October 2026, implemented with OpenAI Codex (GPT-6), against base commit `3acef105`. This rollout enables only four existing processors:

| Machine | Representative processing path | Existing production role |
| --- | --- | --- |
| Electric Furnace | Vanilla smelting recipe | Smelts ores and other furnace inputs |
| Crusher | One input, one main output | Supplies crushed materials for production |
| Alloy Smelter | Multiple counted inputs | Produces alloys for downstream machines and components |
| Pulverizer | Main output plus probabilistic byproducts | Processes materials while preserving secondary outputs |

Feed a Peepo/Jughead once, select it with the Companion Planner and assign the machine. Clicking any part resolves to its existing controller. Work mode, the four ordered jobs, day/night shifts, food breaks, energy recovery and ownership rules apply. Leave a reachable solid-floor position beside the bottom controller and headroom for the companion (including Jughead's jug). The companion stands beside the machine facing it and uses the existing two-arm INTERACT animation. Compact legacy copies and enlarged machines resolve through the same controller adapter.

One helper contributes a half processing tick per accepted tick, up to +50% speed, costing up to 16 JE/t of companion reserve. A final smaller reserve can still be exhausted. Every whole bonus step also spends the machine's normal upgrade-adjusted electrical cost. With sustained power this gives roughly 1.5x throughput and 1.5x electrical demand, preserving electrical cost per completed recipe. It does not generate electricity or replace the machine's power supply. Fractional effort is transient and discarded on a pause, release or changed input; short/interrupted jobs can achieve less than 50% improvement. Existing speed/efficiency upgrades remain effective.

`MachineKind.supportsCompanionAssistance()` is an explicit allowlist. `MachineBlockEntity` publishes cheap readiness from its ordinary processor tick and exposes one assistance entry point; the shared `advanceProcessor` path handles both normal and bonus progress, input consumption, output and byproduct creation. The full machine tick is never called twice: ejection, refilling, gas boosts and other maintenance retain their normal cadence. Each active assistance call revalidates the current recipe, output/byproduct capacity, electricity, redstone mode, lock, loaded target, owner assignment and town protection before spending reserve. A per-machine tick guard prevents repeated assistance in the same tick. Only the reserved living companion within the work position can contribute.

Each enabled machine has one `ProcessorJob` reservation holding a UUID, with a 100-tick renewable lease. The existing routine releases it on interruption/removal/unload. Helpers remain on the ground, so unload cleanup performs no teleport. Approach lookup checks at most eight positions beside the controller and is cached for 20 ticks; standing clearance is cached for 20 ticks per companion height. No machine scans for workers, no new global ticker/index is added, and there are no chunk tickets or offline work. Unsupported machines allocate no job object. Search/path admission remains under the existing cross-dimension companion budgets. Status/menu inspection reads the cached readiness; only an actively contributing helper performs the additional recipe lookup (at most one per machine per tick).

The companion GUI reports missing inputs, full output/byproducts, no power, blocked approach, exclusive occupancy or **Disabled by redstone**. Machine recipes still come from their normal inputs and datapacks. The Cooking Pot's ghost recipe slots are unchanged; processor recipe planning/supply transport is not implemented here. Generators, drills, fluid processors, special crafters, fuel/gas/structure-dependent processors and all other machines remain disabled for assistance until their processing and resource costs are reviewed explicitly.

No new recipes, unlock bypasses, items, assets, dependencies or saved assignment fields are introduced. This is convenience at the existing machines' progression tiers: upstream mining/material processing and food production supply the work, and existing factory recipes consume its unchanged outputs. Both client and server should use this build to display the appended redstone status correctly. Reuses the existing vanilla model/INTERACT clip; no new animation infrastructure or asset import.

Validation for this addition: `build-local.ps1 -Tasks assemble` compiles and assembles without running test tasks; `scripts/package_modrinth.py build` builds the launcher pack. **No tests were run for this change, as requested.** Previous companion-suite results predate processor assistance and do not validate this addition. Runtime appearance, production accounting, multiplayer concurrency and capacity remain unverified.

Manual acceptance for the pilot: assign one helper to each machine and compare supplied production with/without assistance; check electrical consumption and byproduct capacity, two companions competing for one controller, missing power/inputs, redstone disable, locks, priorities, exhaustion, all machine facings, compact/full footprints, and removal/unload/reload. Broader rollout should wait for those results.

## Multiplayer cost and authority

- `CompanionBudget` is keyed by MinecraftServer and shares separate FIFO admission lanes across all dimensions. It stores UUIDs, not entity/world references, and clears on shutdown. Entity unload releases current stations and removes queued requests.
- Existing `config/jugcraft.properties` gains `companions.searches_per_tick=4` (clamped 1-32) and `companions.paths_per_tick=8` (clamped 1-64). These are counts of admitted operations, not measured millisecond guarantees. Restart after config changes.
- The budget gates explicit work/rest/food searches, command and food path requests, seat path attempts, and random strolling. Expired requesters are removed, queues are capped at 4096 per lane, and deferred goals retry rather than interpreting a budget delay as an unreachable path. Vanilla navigation's own internal maintenance and the base entity simulation are not replaced.
- Explicit work links are considered first. When a usable assigned station exists, no ambient machine discovery is needed. Fallback discovery reuses the loaded-chunk index and reads at most 128 candidate positions; surface searches retain their 128-block batches. The index itself still rebuilds from loaded chunk block entities and should be included in profiling dense factories.
- Routine searches retain staggered 80-99 tick intervals, local path-attempt limits and 10-second unreachable cooldown. Productive travel is bounded to 64 blocks with a 30-second deadline. Higher-priority job rechecks are throttled and keep the current job if no better reachable station can be claimed.
- Lunch searches inspect at most four candidate stores per attempt; each store inspects at most 128 accessible views. Assignment avoids that ambient search. Refill is capped to four items per visit. Food pickup caps line-of-sight checks to 32 candidates; the existing spatial entity query still needs profiling around large dropped-item piles.
- Refill and returned-container transfers join source/destination changes in Fabric transactions. The cargo snapshot only includes slots 0-7. A failed insertion rolls back extraction; no overflow meal is dropped just because refill storage is full.
- Job status is cached server-side once a second. The open menu sends changed numeric status/preferences; there is no new per-tick status broadcast. Existing energy/eating synchronization remains unchanged and is another profiling target at large populations.
- Only the owner or authorized party can mutate preferences or assignments. Menu edits validate live entity, dimension, reach and ownership and are rate-limited. Lunch access checks current sharing, locks and town protection on use. Work and food cannot act in unloaded chunks, and there are no chunk tickets or offline generation.
- Operator command `/peepobudget` reports admitted/deferred search and path totals for the running server. It is diagnostic only; no telemetry or external reporting.

## Progression, compatibility and assets

Discovery-tier companion management: consumes existing crafted meals and uses existing planner, lunch sources, inventory and furniture. No new currency, dependency, recipe, resource gate or art is introduced. Food production feeds companion recovery; companions continue powering the existing wheel. Production assistance is limited to the Cooking Pot and the four explicitly enabled standard processors described above.

Existing Assignment0-4 retain home/job order. Assignment5 is the optional lunch source. Missing preferences load defaults matching prior recovery/work behavior. Meal-container return bookkeeping is bounded and saved separately; the physical items remain in the existing inventory. UI positions change, but slot indices and inventory save keys do not. Upgrade both client and server because the companion menu has new data fields.

Uses the existing Fabric event/transaction APIs, vanilla container UI and goals. No optional-library dependency is added. No textures, models or sounds were imported; alerts use an existing vanilla sound.

## Validation

Common/client compilation and packaging use `build-local.ps1 -Tasks assemble`. The initial implementation skipped automated tests at the owner's request. On 7 October 2026 the owner requested companion-only testing: [19 groups and 127 assertions passed](companion-tests.md), including gameplay, menus, save/reopen and one-client dedicated-server reconnect. Two-client concurrency and representative server-load performance remain unverified.

Entity-unload cleanup defers safe-exit movement until a later active tick/load. Moving between entity sections from Minecraft's tracking-removal callback can reenter tracking and crash shutdown; ordinary command/food/job release still exits immediately.

Manual acceptance before deployment:

1. Select each companion type; bind home, four wheels and a lunch crate/cover; reorder jobs and restart. Check no loss of existing inventory, ownership or saved assignments.
2. Fill/block/occupy/remove a wheel. Confirm state labels, priority fallback, release and recovery. Revoke party access while another player has the menu open.
3. Feed from cargo while holding a torch/tool. Refill with nearly full cargo, empty/locked lunch storage, different item components, competing companions and hoppers. Confirm bowl return, full-destination backoff, no duplicates and no lost meals.
4. Change shift at dusk/dawn, recovery thresholds while working, and dimension. Verify the 10-point recovery gap, sleeping restrictions and seated rest without forced loading.
5. Enable alerts, leave a persistent failure, and reopen the GUI. Confirm no continuous chime and correct live status.
6. Profile representative companion populations alongside the intended machines and dimensions; inspect tick time, navigation, packets and `/peepobudget`. The default limits are initial conservative settings, not a tested server-capacity claim.
