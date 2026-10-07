# Companion Cooking Pot assistance and recipe plans

Owner-requested addition on `peepo-companion`, implemented with OpenAI Codex (GPT-6). Extends the [shared jobs and budgets](companion-jobs.md), existing Cooking Pot processor, inventory menu and companion model rig. No dependency or platform changes.

## Use

1. Feed a Peepo or Jughead to tame it. Select it with the Companion Planner, then right-click a Cooking Pot to add it to the four ordered workstations. Work mode, priorities, schedules and recovery thresholds apply as they do to wheels.
2. Keep a reachable ground position beside the pot and space above/around it for the hanging stir animation. Jughead needs extra headroom for his jug. A pot above a campfire or other existing heat source still needs that source to be active.
3. Open the pot. Search or page through the recipes on the right, then select the output you want. The saved recipe and its counted ingredients appear below. Ingredient examples cycle through accepted alternatives; hover for names. These are previews, not inventory slots.
4. Add the displayed ingredients to the normal six input slots. With heat, a valid batch and result space, one assigned companion assists. The pot shows **Helper: +50% speed** and a green progress arrow. Other helpers cannot stack the bonus.
5. **Clear recipe / Auto** restores the old ingredient-driven cooking behavior. Changing the recipe resets progress but does not remove or replace items. Remove incompatible leftovers manually. With a recipe selected, manual insertion, shift-click, hoppers and the pot's existing container automation filter inputs against that recipe.

The recipe belongs to the pot, not to an individual helper, so changing helpers cannot change the supply order. Players who can use the unprotected pot may edit it; this does not introduce companion ownership as a new lock on ordinary machines. Town protections, existing container locks, live menu validity, distance and spectator checks apply to edits. Companions do not assist locked pots.

## Production and progression

Discovery/workshop convenience using existing farming inputs, crafted bowls/bottles and heat. Output remains the same meals/preserves for players, lunch crates and companion recovery. There is no new recipe unlock, spoon item, fuel, currency or required inventory slot.

- One reserved helper per pot. Assistance adds half a processing tick per useful server tick: one extra progress tick for every two ticks of assistance, approximately 1.5x throughput with uninterrupted supplies. Short batches and interruptions can round down fractional progress.
- Effort costs 16 JE per useful tick (320 JE/s), spending any final reserve smaller than 16 so exhaustion can still reach zero. This is one-quarter of a wheel's maximum 64 JE/t cost. Existing meal regeneration can sustain light cooking labor; no additional regeneration buff is created.
- No effort is accepted without heat, a matching recipe, enough result space, a valid claimant, current permission and permission to work under the companion's routine. Full output, missing inputs, no heat, unavailable selected recipe or obstruction makes it release/skip the job. The pot retains its ordinary unassisted cooking while no companion is present.
- Heat and recipe ingredients are still required. Recipe completion uses the existing batch-consumption, preserve timestamps and container-remainder handling. Assistance never invokes the entire block-entity tick twice.
- The same scheduler/search/path budgets and bounded priority retry rules apply. A Cooking Pot must be explicitly assigned; wild/unassigned companions do not take over cooking stations.

## Recipe and future supply contract

The block entity persists `CompanionRecipe` as a stable recipe identifier, not a list index or output-item ID. Missing keys preserve Auto for existing saves. If a datapack removes a selected recipe, the saved ID remains and the pot reports Recipe unavailable instead of silently cooking a different meal. Selecting Auto or another recipe recovers it.

`CookingPotBlockEntity.supplyPlan()` exposes the selected ID, exact counted `Ingredient` predicates, output and time as `CookingPotPlan`. Auto has no supply plan. A later porter can use the predicates/counts for ingredient choice and quantities; the method grants no access to storage and performs no transfer. **Porter/supply transport is not implemented in this change.**

The recipe catalogue is cached per server and invalidated on datapack reload/shutdown. Menu opening sends a bounded recipe snapshot from the actual server, independent of JEI's generated catalogue. The current pack contains 32 pot recipes, each with at most six ingredient parts. The selector protocol is capped at 512 recipes and 64 parts per recipe; the current screen previews up to eight parts and up to 32 alternatives per part. Custom datapacks beyond those UI limits need a larger browser. Production continues using the full server recipe definitions. Existing menus close on reload, so stale displayed indices cannot select a different recipe. Selection edits are rate-limited to one per two ticks, and all viewers receive changed selection state.

## Animation and multiplayer cost

`CompanionJob.animation()` defaults to the new reusable **INTERACT** action: both arms extend in front and rise/fall together, with a small head motion. The wheel keeps its existing running action. The pot chooses **STIR**: both hands meet the top of a wooden spoon tilted 15 degrees into the pot, the spoon/companion circle every four seconds, and the dangling legs move gently. Peepo, Jughead and their pumpkin costumes use the same poses. Equipped items remain stored and are visually hidden during two-handed work.

Uses the existing vanilla `EntityModel`/layer renderer, as permitted by the framework guidance for simple existing renderers. No replacement animation framework, GeckoLib migration or Player Animation Library ownership is introduced. Server action/target fields change only at transitions; the smooth phase is calculated from world ticks on clients. Normal entity movement packets keep the real mounted hitbox with the visible companion. There is no spoon entity, separate block renderer, per-frame packet or machine-wide worker scan.

Reservations store only a UUID and expire if abandoned. Entrance search is limited to eight local positions, cached for 20 ticks; full-sweep clearance is cached for 20 ticks per companion height. The current mount position is collision-checked as it moves. Boundary chunks must already be loaded. The helper uses no gravity while mounted and releases to its safe ground entrance on cancellation/removal/unload. Its persisted safe-exit marker also recovers a save made while stirring. No chunk tickets, offline work or remote inventory access.

## Assets and validation

The owner library/catalog was inspected for spoons and stirring clips; no matching rig/prop was found. The new closed spoon geometry and procedural arm/stir clips are authored for this feature. The spoon reuses Jugcraft's existing original `assets/jugcraft/textures/block/cider_press_wood.png`, generated by `tools/cider_textures.py`, unchanged. No library originals, base character geometry, textures or proportions are modified; no third-party assets are imported. The existing Blockbench files are unchanged; these new clips/prop currently live in the runtime model code.

Initial validation: common/client compilation and artifact assembly with `build-local.ps1 -Tasks assemble`; `.mrpack` packaging with `scripts/package_modrinth.py build`. Following the owner's 7 October 2026 testing request, the [companion-only suite passed 19 groups and 127 assertions](companion-tests.md). Both companions approached and stirred a pot; measured progress and energy matched 1.5x speed and 16 JE/t. Recipe/companion menus, save/reopen and a one-client dedicated-server reconnect passed. Runtime screenshots were inspected. Two-client concurrency and multiplayer-load measurements remain pending. Client and server both need the update because the pot menu and companion entity-data fields changed.

Manual acceptance: assign each companion/costume; compare a continuously supplied pot with/without one helper; add a second helper; fill outputs, remove heat, block the approach/headroom, reorder jobs, change shifts and force exhaustion. Select a recipe, filter incompatible items through UI/hopper, share an open pot between two players, restart during cooking/stirring, remove the pot, and reload a datapack that removes/replaces the selected recipe. Check spoon grip/tilt, jug clearance, visible hitbox, held-item restoration, normal sleeping/seating and wheel animation. No claim of measured multiplayer capacity is made.
