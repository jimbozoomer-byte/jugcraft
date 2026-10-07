# Companion Cooking Pot assistance and recipe plans

Owner-requested addition on `peepo-companion`, implemented with OpenAI Codex (GPT-6). Extends the [shared jobs and budgets](companion-jobs.md), existing Cooking Pot processor, inventory menu and companion model rig. No dependency or platform changes.

## Use

1. Feed a Peepo or Jughead to tame it. Select it with the Companion Planner, then right-click a Cooking Pot to add it to the four ordered workstations. Work mode, priorities, schedules and recovery thresholds apply as they do to wheels.
2. Keep a reachable ground position beside the pot and space above the rim for the standing stir animation. Jughead needs extra headroom for his jug. A pot above a campfire or other existing heat source still needs that source to be active.
3. Shift-right-click the companion. Each assigned, loaded Cooking Pot has a ghost recipe slot beside its Work row. Pick up the finished food/preserve item from your inventory and left-click the ghost slot. The real stack stays on your cursor; only a one-item preview appears. Hover the preview for ingredient counts. Repeated clicks with the same output cycle recipes when a datapack provides several for that item.
4. Add the displayed ingredients to the pot's normal six input slots. With heat, a valid batch and result space, one assigned companion assists. The compact pot GUI shows +50% speed and a green progress arrow while assisted. Other helpers cannot stack the bonus.
5. Right-click the ghost slot, or left-click with an empty cursor, to restore automatic ingredient-driven cooking. Changing the recipe resets progress but does not remove or replace items. Remove incompatible leftovers manually. With a recipe selected, manual insertion, shift-click, hoppers and the pot's existing container automation filter inputs against that recipe.

The recipe stays on the workstation, so all companions assigned to that pot see the same plan. Existing saved selections remain intact. Editing now requires the companion owner or an authorized party member, a valid nearby companion menu, the current assignment in the same dimension, a loaded workstation within 64 blocks of the companion, and current town/lock permission. It does not load chunks. The pot GUI has no recipe-selection controls or recipe-edit button handler.

Ghost slots are display copies, separate from the eight cargo and two equipment slots. They reject shift-click, drag, hotbar swaps, creative cloning and dropping; closing the menu cannot give or drop the preview. Invalid output items leave the existing recipe intact. Slots follow their workstation when priorities change and disappear when an assignment is removed. Other workstation types keep their existing behavior; wheels have no recipe slot.

## Production and progression

Discovery/workshop convenience using existing farming inputs, crafted bowls/bottles and heat. Output remains the same meals/preserves for players, lunch crates and companion recovery. There is no new recipe unlock, spoon item, fuel, currency or required inventory slot.

- One reserved helper per pot. Assistance adds half a processing tick per useful server tick: one extra progress tick for every two ticks of assistance, approximately 1.5x throughput with uninterrupted supplies. Short batches and interruptions can round down fractional progress.
- Effort costs 16 JE per useful tick (320 JE/s), spending any final reserve smaller than 16 so exhaustion can still reach zero. This is one-quarter of a wheel's maximum 64 JE/t cost. Existing meal regeneration can sustain light cooking labor; no additional regeneration buff is created.
- No effort is accepted without heat, a matching recipe, enough result space, a valid claimant, current permission and permission to work under the companion's routine. Full output, missing inputs, no heat, unavailable selected recipe or obstruction makes it release/skip the job. The pot retains its ordinary unassisted cooking while no companion is present.
- Heat and recipe ingredients are still required. Recipe completion uses the existing batch-consumption, preserve timestamps and container-remainder handling. Assistance never invokes the entire block-entity tick twice.
- The same scheduler/search/path budgets and bounded priority retry rules apply. A Cooking Pot must be explicitly assigned; wild/unassigned companions do not take over cooking stations.

## Recipe and supply contract

The block entity persists `CompanionRecipe` as a stable recipe identifier, not a list index or output-item ID. Missing keys preserve Auto for existing saves. If a datapack removes a selected recipe, the saved ID remains and the pot reports Recipe unavailable instead of silently cooking a different meal. Selecting Auto or another recipe recovers it.

`CookingPotBlockEntity.supplyPlan()` exposes the selected ID, exact counted `Ingredient` predicates, output and time as `CookingPotPlan`. Auto has no supply plan. The porter now uses those predicates/counts for ingredient choice and quantities; the method itself grants no access to storage and performs no transfer. The separate [Supply/Output jobs](companion-jobs.md#supply-and-output) validate storage and physically carry the ingredients and finished products.

The recipe catalogue is cached per server and invalidated on datapack reload/shutdown. Output matching uses the live server catalogue; no recipe index or client-provided item is trusted. The server reads the real menu cursor stack and sends only the four ghost previews through ordinary slot synchronization. Current ingredient previews show one example per ingredient predicate; supply jobs retain the complete predicates. Edits are limited to once per two ticks. While the menu is open, previews refresh at most twice per second, plus immediately after an edit/reorder. No recipe catalogue is sent when opening a pot.

## Animation and multiplayer cost

`CompanionJob.animation()` defaults to the new reusable **INTERACT** action: both arms extend in front and rise/fall together, with a small head motion. The wheel keeps its existing running action. The pot chooses **STIR**: the companion stands on the nearest accessible rim, with both feet planted at the actual rim height (8.5/16 block). Both hands move together in a four-second stirring circle around a shortened wooden spoon tilted 15 degrees into the pot. The spoon reaches the contents while the body stays still. Peepo, Jughead and their pumpkin costumes use the same poses. Equipped items remain stored and are visually hidden during two-handed work.

Uses the existing vanilla `EntityModel`/layer renderer, as permitted by the framework guidance for simple existing renderers. No replacement animation framework, GeckoLib migration or Player Animation Library ownership is introduced. Server action/target fields change only at transitions; the smooth phase is calculated from world ticks on clients. The mounted hitbox stays on the rim; spoon movement needs no entity position updates. Displacement is corrected only when needed. There is no spoon entity, separate block renderer, per-frame packet or machine-wide worker scan.

Reservations store only a UUID and expire if abandoned. Entrance search is limited to eight local positions, cached for 20 ticks; stationary mount clearance is cached for 20 ticks per companion height and invalidated when the entrance changes. Boundary chunks must already be loaded. The helper uses no gravity while mounted and releases to its safe ground entrance on cancellation/removal/unload. Its persisted safe-exit marker also recovers a save made while stirring. No chunk tickets, offline work or remote inventory access.

## Assets and validation

The owner library/catalog was inspected for spoons and stirring clips; no matching rig/prop was found. The closed spoon geometry and procedural arm/stir clips are authored for this feature, now adjusted for standing on the rim. The spoon reuses Jugcraft's existing original `assets/jugcraft/textures/block/cider_press_wood.png`, generated by `tools/cider_textures.py`, unchanged. No library originals, base character geometry, textures or proportions are modified; no third-party assets are imported. The existing Blockbench files are unchanged; these new clips/prop currently live in the runtime model code.

Initial validation: common/client compilation and artifact assembly with `build-local.ps1 -Tasks assemble`; `.mrpack` packaging with `scripts/package_modrinth.py build`. Following the owner's 7 October 2026 testing request, the [companion-only suite passed 19 groups and 143 assertions](companion-tests.md). Both companions approached, stood on the rim and stirred a pot with moving arms and planted feet; measured progress and energy matched 1.5x speed and 16 JE/t. Ghost recipe setting/clearing without item consumption, blocked preview extraction, ordinary pot/companion menus, save/reopen and a one-client dedicated-server reconnect passed. Runtime screenshots were inspected. Two-client concurrency and multiplayer-load measurements remain pending. Client and server both need the update because the pot menu and companion entity-data fields changed.

Manual acceptance: assign each companion/costume; compare a continuously supplied pot with/without one helper; add a second helper; fill outputs, remove heat, block the approach/headroom, reorder jobs, change shifts and force exhaustion. Select a recipe, filter incompatible items through UI/hopper, share an open pot between two players, restart during cooking/stirring, remove the pot, and reload a datapack that removes/replaces the selected recipe. Check spoon grip/tilt, jug clearance, visible hitbox, held-item restoration, normal sleeping/seating and wheel animation. No claim of measured multiplayer capacity is made.
