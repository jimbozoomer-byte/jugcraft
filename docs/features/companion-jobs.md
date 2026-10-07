# Companion jobs, budgets, status, lunch and schedules

Owner-directed stages 1-4 of the companion roadmap, implemented locally with OpenAI Codex (GPT-6) on `peepo-companion`. This extends the existing wheel, inventory, planner and rest behavior. [Cooking Pot assistance and recipe plans](companion-cooking.md) and the processor helper teams below extend that shared job system.

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

## Processor assistance and helper teams

Owner-requested on 7 October 2026, implemented with OpenAI Codex (GPT-6), against base commit `4bc09ffd`. The four-machine pilot is now expanded to all **36 processing machine types** in `MachineBlockEntity`: standard item recipes, fluid recipes, special crafting/plating, mining, farming and continuous pumping/separation. Electrical generators, kinetic engines, batteries and tanks have no processing-speed job and remain unsupported. The Generator Wheel and Cooking Pot retain their separate existing jobs.

Use the Companion Planner to assign the same machine to each tamed companion. Clicking any linked machine part resolves to its controller. Each companion still has its own four-job list, priority, schedule, meals and recovery settings. Full multiblock machines (including the Arc Furnace's casing structure) have **two helper positions**, each adding up to **25% of ordinary processing speed**; one worker gives 25%, two give 50%. A physically single-block machine, including an old compact copy, has one position adding up to 50%. Provide separate reachable standing spaces at the front/sides and sufficient headroom for Jughead. Workers face the machine and use the existing two-arm INTERACT clip; the suggestions below are not implemented animations.

### Companion reserve drain and animation suggestions

These numbers are **per Peepo/Jughead**, in JE per productive server tick. At 20 TPS, 8 JE/t is 160 JE/s and 16 JE/t is 320 JE/s. Both large-machine helpers together drain 16 JE/t, matching the total effort of a single small-machine helper. Food regeneration can offset the net drain; passive regeneration stays suppressed while working. No effort is charged during a blocked, unpowered or idle production step. A final smaller reserve can be exhausted, and the existing recovery threshold ends the job.

The table describes full-size placed machines. **Compact legacy copies override their row to one helper at 16 JE/t.** The Arc Furnace uses two even though only its controller is a block entity.

| Machine | Maximum helpers | JE/t per helper | Suggested clip (not implemented) |
| --- | ---: | ---: | --- |
| Electric Furnace | 2 | 8 | Slide a tray with tongs; adjust the control dial |
| Crusher | 2 | 8 | Feed the hopper with a little shovel; pull a lever |
| Arc Furnace Controller | 2 | 8 | Long tongs at the hatch; a second helper checks the controls |
| Alloy Smelter | 2 | 8 | Tongs at the loading hatch; turn the pour-control handwheel |
| Metal Press | 2 | 8 | Pull the press lever; arrange parts on the feed tray |
| Wire Drawer | 2 | 8 | Turn a crank; guide wire onto a spool |
| Circuit Assembler | 2 | 8 | Solder a component; inspect it with a magnifier |
| Pulverizer | 2 | 8 | Scoop feed into a chute; work a crank |
| Ore Washer | 2 | 8 | Swish a sieve basket; scrub with a small brush |
| Sieve | 2 | 8 | Rock a screening tray side to side |
| Sawmill | 2 | 8 | Guide a board along the feed table; turn a feed wheel |
| Coke Oven | 2 | 8 | Shovel coal; work a long poker |
| Steel Foundry | 2 | 8 | Work the bellows/control lever; steady a long ladle |
| Ore Drill | 2 | 8 | Brace and turn the feed crank; check a gauge |
| Deposit Drill | 2 | 8 | Adjust a lever; tighten a fitting with a wrench |
| Cobblestone Generator | 1 | 16 | Tap and clear the output chute with a small hammer |
| Tree Farm | 1 | 16 | Prune a sapling; tend the seedling tray |
| Auto-Crafter | 1 | 16 | Pick, place and tap parts on a small work surface |
| Crop Harvester | 2 | 8 | Sort the collection tray; adjust the cutting-height lever |
| Hydroponic Bay | 2 | 8 | Water seedlings; inspect leaves |
| Electroplating Bath | 2 | 8 | Raise and lower a parts rack |
| Rocket Workshop | 2 | 8 | Turn a wrench; inspect a panel |
| Pumpjack | 2 | 8 | Operate the stroke lever; grease a bearing |
| Fracking Rig | 2 | 8 | Turn a pressure valve; watch the gauge |
| Air Separation Unit | 2 | 8 | Turn a cold-box valve; wipe a frosted gauge |
| Distillation Tower | 2 | 8 | Turn a valve; read a temperature gauge |
| Catalytic Cracker | 2 | 8 | Work a pump lever; adjust a valve |
| Settling Plant | 2 | 8 | Rake the settling tray; brush the filter |
| Polymerization Reactor | 2 | 8 | Work the mixing control; collect a scoop of pellets |
| Electrolytic Cell | 2 | 8 | Raise an electrode rack; adjust the controls |
| Chemical Reactor | 2 | 8 | Work a mixing lever; check the sight glass |
| Synthesis Converter | 2 | 8 | Lean into a large handwheel; check pressure |
| Hydrotreater | 2 | 8 | Turn a valve; inspect a pipe fitting |
| Lithography Station | 2 | 8 | Adjust a lens; inspect a wafer |
| Ammonia Chiller | 2 | 8 | Wipe frost; turn the coolant valve |
| Cryogenic Liquefier | 1 | 16 | Turn an insulated valve; watch the gauge |

The Cooking Pot remains one helper, +50%, up to 16 JE/t (320 JE/s). The Generator Wheel remains one runner extracting up to 64 JE/t (1,280 JE/s), depending on available buffer space. These rates apply equally to Peepo and Jughead.

For efficient animation work, start with three reusable clips: **turn a crank/valve**, **pull a lever**, and **handle a tray/tool**. Assign complementary roles to the two positions, offset their phases, and add sparse client-only particles where appropriate. Avoid per-frame server messages, real tool/item entities, or simulated fluid/block edits for the visual action.

### Processing and resource conservation

Each station position has an exclusive UUID reservation and a 100-tick renewable lease. The machine selects a worker's existing slot before offering a free one; a third companion sees Occupied. Positions cannot overlap, and releasing one worker leaves the other's reservation intact. Each helper refreshes only its participation request. **Only the machine's normal validated production step spends reserve and applies assistance.** A request alone never consumes energy or creates progress.

The shared hook considers at most two known UUIDs. One large-machine worker contributes a quarter-step per productive tick, or a small-machine worker contributes a half-step. Four accumulated quarters buy one bonus step; at most one bonus step is allowed in a machine tick. Extra steps spend the same electricity as ordinary steps, including existing speed/efficiency upgrades. Existing gas boosts remain separate/additive, rather than being multiplied for free. Recipe completion remains in the original path: required ingredients, water/nutrient solution, acid, containers, byproduct capacity, finite deposits, valid structure state and redstone gates are not bypassed. Short recipes, operation boundaries, resource shortages and discarded fractional effort can yield less than the nominal speed bonus.

Fluid recipes consume their ordinary batch inputs and create their ordinary outputs. Pumpjack/fracking bonus batches recheck output capacity and remaining reservoir, and fracking also consumes its extra fluid dose. Air separation makes extra nitrogen/oxygen and accumulates the corresponding fractional argon production; its small `CompanionArgonTicks` remainder is saved so reloads do not lose that byproduct share. Missing save keys start at zero. The main machine tick, item/fluid pushing, refill, source checks and world-search loops are never invoked a second time for assistance. Timed mining/harvesting only advances its existing work timer; completion still edits the original target once.

Incomplete structures, invalid recipes, insufficient inputs or output capacity, missing power for added work, disabled redstone, locks, lost assignments, off-shift/recovering workers and unloaded/removed stations stop useful assistance. A one-tick participation expiry plus per-worker/per-machine tick guards prevents stale or repeated contributions. Both current assignment and live town permission are checked before each reserve debit. Removal/unload releases reservations without teleporting helpers during entity tracking removal. Recipe ghost selection remains Cooking Pot-only; porter/supply transport remains future work.

### Multiplayer cost and validation

The adapter caches at most 18 nearby approach candidates per slot for 20 ticks (front positions and controller sides); clearance is cached for 20 ticks per companion height. It checks loaded positions and separates the two workers. There are no new machine-wide worker searches, global tickers, chunk tickets or offline production. Existing cross-dimension companion search/path budgets still govern travel. Readiness comes from each machine's ordinary tick; menu inspection and assistance do **not** repeat recipe searches. Unclaimed machines examine at most two empty reservation slots at their productive step. Existing machine world queries and operation completions retain their original algorithms; higher throughput still warrants profiling a real factory.

No new assets, libraries, currencies, items, recipes or progression bypasses. Upstream food supplies companion reserve; mining/farming/chemistry supplies existing inputs, and the same downstream recipes consume outputs. Assignment keys remain compatible. Both client and server should use the updated build; old compact machines keep one-helper behavior.

Validation: `build-local.ps1 -Tasks assemble` compiles/assembles without test tasks; `scripts/package_modrinth.py build` packages the launcher build. **No tests or gameplay runs were performed for this expansion, per the owner's instruction.** Earlier companion test results predate processor assistance. Manual checks should compare one/two/three workers, separate positions and all facings, compact/full variants, thresholds and priorities, speed/electricity/resource accounting, full outputs/remainders/byproducts, depleted reservoirs, redstone, removal and save/reload before server deployment.

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

Discovery-tier companion management: consumes existing crafted meals and uses existing planner, lunch sources, inventory and furniture. No new currency, dependency, recipe, resource gate or art is introduced. Food production feeds companion recovery; companions continue powering the existing wheel. Production assistance is limited to the Cooking Pot and the 36 processing machine types described above.

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
