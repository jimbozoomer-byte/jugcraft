# Companion garden work

Owner-requested on 7 October 2026, implemented locally with **OpenAI Codex (GPT-6)** on `peepo-companion`, from `2d03b7c7`. This extends the existing assignment, job, cargo and porter systems; no new dependency, inventory, item ID or art asset is introduced.

## Setting up a garden

1. Tame Peepo/Jughead and select it with the Companion Planner.
2. Right-click farmland (or a crop standing on it). That soil is the anchor of one garden assignment. It saves up to **eight edge-connected farmland blocks at the same height**, including the anchor. Diagonals, gaps, inaccessible/unloaded cells and soil already assigned to this companion are excluded. A larger field takes the nearest connected eight in a stable breadth-first order.
3. Hold the planner to see each saved soil cell outlined in green. The Jobs row shows `Garden (N blocks)` and the usual location, priority and live status. Left-click any member soil/crop to remove the whole plot; the row's remove button also works.
4. Repeat for up to four work slots: **four plots, at most 32 soil blocks** if every slot is used for gardening. Garden jobs and machine jobs share those four slots. A home, lunch source, Supply and Output remain separate assignments.
5. Put seeds in cargo or assign a **Supply** container containing the seeds you want planted. Assign **Output** for surplus harvests. Use **Work** mode; the separate Porter mode remains ordinary container-to-container transport.
6. Put a hoe in **Hand**, not a cargo slot, for faster, more energy-efficient work. The ordinary two-arm interaction clip is reused. There is no new gardening-specific tool animation.

Plot membership is saved, not rediscovered every tick: extending or reshaping a field requires removing/reassigning the plot. Restore a missing anchor or reassign the remaining soil if the original anchor is removed. Other missing member cells are skipped. `CompanionGarden.PLOT_LIMIT` is the single current balance constant (8); it is not a new user-facing config setting.

## Actual crop behavior and costs

Companions walk to a safe position on or beside a selected crop and work locally. Mature ordinary crops are harvested with their normal loot; one matching seed is retained from that harvest, or consumed from cargo, to replant the same crop. If neither exists, the crop is harvested and the empty soil waits for seeds. Empty cells use the first compatible seed in cargo; Supply searches its usual bounded storage views for a compatible seed when cargo cannot cover the empty cells. Put only desired seed varieties in Supply when controlling what fills an empty field.

Supported plants are vanilla `CropBlock` crops, Jugcraft's ordinary crops, and Jugcraft's corn, ornamental corn, sunflower, pepper and trellised tomato. Tall plants are picked back to their existing regrowth age without shortening or breaking the plant. Tomatoes need an existing trellis on the soil, and additional trellis above to grow normally. Tall crop tops are individually validated before picking. Leave reachable aisles around solid tall crops. Other growth systems, including adjacent stem fruits, berries, sugar cane and trees, are outside this first garden adapter. Soil must already be tilled; hoes do not expand the plot, till dirt, irrigate, apply fertilizer or accelerate crop growth.

| Work | Active time at 20 TPS | Reserve cost | Tool wear |
| --- | ---: | ---: | --- |
| Harvest/replant one mature ordinary crop, pick one tall plant, or plant one empty cell | 80 ticks / 4 seconds | 4 JE/t, 320 JE per uninterrupted action | None |
| Same action with a hoe in Hand | 40 ticks / 2 seconds | 4 JE/t, 160 JE per uninterrupted action | 1 durability per completed action, using normal enchantment handling |

Pathfinding/search delays and deliveries are additional. An interrupted action restarts; effort already spent is not refunded. Tool material does not add another speed multiplier. Food regeneration can offset the net reserve drain. Existing recovery thresholds, rest, shifts, eating, priorities and the settings-menu pause continue to apply. Transport retains its existing up-to-2-JE/t walking cost. This is early farming utility: seeds/soil/tools and companion food are inputs, and harvests feed cooking, lunch or later processing. It creates no electricity and bypasses no crop growth/food cost.

## Supply, output and conservation

- Each garden row uses the existing **Supply Auto/On/Off** and **Output Auto/On/Off** buttons. Gardens have no external machine item port to detect, so Auto permits the companion's transport. There is no ghost recipe requirement for a garden.
- Supply brings only enough plantable seeds for that plot's current empty cells after considering carried seeds. Pickup occupies a real cargo slot and reserves that stack while travelling. Arrival releases the seed stack for planting; it does not insert a second copy into an invented garden inventory. Unneeded seeds return using the existing return route.
- Harvests occupy the existing eight cargo slots. They keep per-slot, per-plot provenance, so a plot with Output disabled cannot have its harvests exported by another enabled plot. Player-given food, tools and unrelated cargo are never selected as garden output. Editing a cargo stack manually clears that slot's harvest provenance, handing control of that stack back to the player.
- Harvests of the same variant from the same plot may stack; they do not automatically merge into unmarked player cargo or another plot's marked cargo. Ordinary seeds retain a buffer equal to the companion's total assigned garden cells, at most 32 per seed variant; only surplus marked seeds are exported. Carrots/potatoes can still be eaten under the existing food policy.
- Output adopts an already-carried harvest stack and walks it to the container. It does not require an extra empty cargo slot, so full cargo can still unload. At most 32 items travel per trip. Container permissions, sided access, capacity, assignment identity and load state are rechecked through the existing transport implementation.
- Crop loot and seed changes use inventory/provenance snapshots; if the complete harvest will not fit, the crop stays intact. Failed block changes roll back inventory changes. Full cargo causes a cooldown instead of dropping item entities. No custom Fortune bonus is added by the hoe.
- Invalidated assignments/routes leave undelivered items in cargo rather than deleting or rerouting them. The existing in-flight transport manifest is saved along with the plot identity. Death drops the same real cargo once; there is no second garden inventory to drop.

## Server authority and performance

Planner edits retain owner/party, player reach, dimension, rate, loaded-chunk and town protection checks. Garden discovery is a bounded horizontal traversal (eight accepted cells, at most 29 visited positions); it never forces chunks. The assignment snapshot has at most four lists of eight positions. Save/sync input is capped, deduplicated and constrained to an anchor-connected horizontal list.

Readiness scans check at most eight saved cells per plot and use the existing server-wide search budget, with an 80-99 tick refresh while idle. Active work rechecks only its selected soil/crop; tall harvests touch at most three plant sections. Navigation uses the shared path budget, timeouts and unreachable backoff. A 100-tick renewable per-soil UUID lease prevents overlapping companions from working the same crop simultaneously. Leases hold no entity references, are released by the normal job lifecycle, and have a bounded per-level table with rotating cleanup of expired entries. No entity/world scans, crop tick acceleration or chunk loaders are added.

Every modified soil/crop must remain loaded and outside protected towns, and vanilla `mobGriefing` must be enabled. The GUI reports growing crops, missing seeds, missing soil, access/path problems, full cargo and a disabled gamerule. Clearing/reordering assignments, food, rest, schedule changes and unload use the existing cancellation lifecycle. Garden save keys are additive; old targets and transport routes without a plot list load normally. Client and server should use the same build for outlines and the appended status IDs.

## Validation

**No automated tests or in-game tests were run, as requested.** Common/client compilation and JAR assembly passed via `build-local.ps1 -Tasks assemble`; the complete Modrinth pack was then rebuilt. Compilation/packaging is not evidence that navigation, crop interactions or multiplayer gameplay passed.

Pending manual checks: irregular/oversized fields and four-plot limits; vanilla/Jugcraft crops and tall plants; hoe timing/wear; mixed seed supplies; output full/off and completely full cargo; player cargo edits; two workers on overlapping plots; lower-priority jobs; rest/menu interruption; save/restart while carrying seeds or harvests; unloaded chunks, removed anchors, protected cells and `mobGriefing` disabled.
