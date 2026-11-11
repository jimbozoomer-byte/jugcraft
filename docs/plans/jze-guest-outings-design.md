# JZE guest outings and home visits: design (worker C, November 11)

Plan item 3 of [the November 11 expansion](jze-guest-expansion-nov11.md). Companionship activities for every available
guest: friendly wording only, nothing romantic, no spouse-only variant (owner clarification, November 11). Code status:
written against the coordinator's seams (`claude/nov11-jze-api` at `dc4469ab72`); compiled and tested only by Codex.

## Classes (worker C's)

| Class | Job |
| --- | --- |
| `GuestOutings` | Numbers; the `outings` `GuestFeature`; begin, the once-a-second step, every ending, the return walk, restart recovery, Fabric hooks. |
| `GuestOutingPlaces` | Where an outing happens: open water, open ground, beside a table; what may be stood on; the protection predicate. |
| `GuestVisits` | The home visit spot: validation, `/guests visit spot` and `/guests visit clear`, the loaded path region check. |
| `GuestOutingState` | Saved data `jugcraft:town_guest_outings`: per player only (visit spot; per guest the day and kinds done). |
| `GuestOutingTesting` (gametest) | Test seam: begin at a given place, step one guest at a given time, recover, codec round trip, return watch. |
| `GuestOutingGameTests` (gametest) | Server tests with synthetic `test_c_*` guests. |

The one saved state per guest is the coordinator's `GuestActivity` (kinds `outing` and `visit`). Everything else per guest
(the outing kind, presence counted, stuck tracking, the return walk) lives in memory and is lost on a restart, which is
why a restart ends every outing (below).

## Flow

1. **Invite** (`invite_fishing`, `invite_fair`, `invite_board_game`, `invite_home`). The click arrives checked by the
   menu; `act` checks again: guest in the roster and available; player alive, not a spectator, not sleeping, same level,
   within `GuestBalance.INTERACT_RANGE` of the guest's eyes-to-body; the entity alive, carrying this guest id and the
   ledger's canonical entity (a copy is refused); the player holds no activity with any guest; the guest is free
   (`GuestActivity.current == null`); this kind not done today with this guest; the guest does not hold a fixed post
   (an operator's patrol or wander 0, whose `PatrolGoal` ignores the activity anchor: see the requested patch).
2. **Place**, loaded chunks only (`level.isLoaded`), searched outward from the player, nearest first, bounded box
   (radius 12, height 4): fishing beside open surface water (3+ source blocks in its 3x3), the fair on open ground (a
   standable spot with 3 standable neighbours, 2+ blocks from the player), the board game beside a table-like block
   (the board-game `TableBlock` or any block whose id ends in `table`), else the player's own spot. A spot is standable
   (sturdy floor, empty feet and head, no fluid or hazard, body fits) and free (no other entity, no other guest's
   activity anchor within 1.5 blocks). Visits use the saved spot, revalidated, within 128 blocks of the guest, every
   chunk along the straight line loaded.
3. **Protected for neither party**: every block the outing uses (stand spot, its floor, the water or table) must pass,
   for the inviting player, `!TownProtection.shieldsBlock` (town and operator regions, no creative exemption),
   `LandRegistry.mayBuildFor` (no admin bypass), `level.mayInteract` (spawn protection) and finally
   `Authority.mayChange` (at most 16 such final calls per search). Consequence to note: the spawn city interior is
   town-protected, so an outing invited deep in town finds no place unless open, unprotected ground lies within 12 blocks
   of the player. That is the literal rule; one predicate (`GuestOutingPlaces.open`) to change if the owner wants
   public town ground allowed.
4. **Begin**: `GuestActivity.begin(guest, Activity(kind, player, overworldGameTime, dimension, place))`, an in-memory
   tracker, a friendly message. The guest walks there by its usual goals (Townsfolk.anchor follows the activity).
5. **Step**, every 20 ticks with the overworld game time: presence (player within 16 blocks of the guest, same level)
   adds the ticks since the last step to "together" time; arrival is the guest within 6.5 blocks of the anchor
   (`GuestActivity.RANGE` 2 + the return goal's slack 4 + 0.5).
6. **Completion**: at the kind's duration. Affection only if the guest arrived and together time is at least three
   quarters of the duration; then the kind is recorded done for today and `GuestRewards.grant` adds at most the reward
   within the shared daily budget. An outing that fell short is ended without reward and not recorded (it may be tried
   again; it gave nothing).

## Endings (every one ends the `GuestActivity`, then the guest walks back to its post)

| Cause | Detected by |
| --- | --- |
| Completion, `end_outing` / `end_visit` | step; the option (beside the guest, as every option) |
| Logout | `ServerPlayConnectionEvents.DISCONNECT`; step (player not in the player list, which also covers `TestPlayers.leave`) |
| Death | `ServerLivingEntityEvents.AFTER_DEATH`; step (not alive) |
| Spectator mode | step |
| Dimension change | step (player's level is not the activity's) |
| Too far | step: player beyond 32 blocks of the guest; after arrival, guest beyond 16 blocks of the anchor |
| Chunk unload | `ServerEntityEvents.ENTITY_UNLOAD` of the canonical guest (any reason); step: guest not loaded, anchor not loaded |
| Stuck path | step: not arrived and no 1-block progress in 300 ticks (15 s) |
| Guest gone | unload with a destroying reason; step: entity not the canonical one |
| Stale | step's sweep: an `outing`/`visit` activity with no tracker (orphan) or older than its bound |
| Restart | `SERVER_STARTED`: every `outing`/`visit` activity (`GuestActivity.all()`) is ended: trackers are memory only and the shutdown logged every player out; same sweep as above |

**Return walk.** After an ending the guest's anchor is its post again. A return watch (memory) checks each second: home
when within the guest's range + 4.5 blocks of its post. With no 1-block progress in 300 ticks, it is teleported to its
post only if the post's chunks are loaded, the body fits there without collision and there is no liquid; otherwise it
keeps waiting (the watch lapses after 6000 ticks). A guest not loaded when its activity ended is watched again when it
next loads in this server session. Nothing is loaded, cloned, placed, moved or opened.

## Numbers (first choices for owner review)

| | Fishing | Fair | Board game | Home visit |
| --- | --- | --- | --- | --- |
| Duration | 3600 ticks (3 min) | 3600 (3 min) | 3000 (2.5 min) | 4800 (4 min, walk included) |
| Stale bound (duration + 1 min) | 4800 | 4800 | 4200 | 6000 |
| Affection, via `GuestRewards.grant` | 12 | 12 | 12 | 15 |

Once per kind per day per player and guest (day = the favors' day, `GuestFavors.day` with its high-water, never earlier
than this state's own high-water). Together time needed: 3/4 of the duration. All rewards share the 75-a-day budget
with gifts and quests; at most 51 a day from these four if the budget allows.

## Visit spot

`set_visit_spot` (beside a guest) or `/guests visit spot` by the player themselves; `/guests visit clear` forgets it.
The spot is the player's feet block in their current level: standable, free apart from the player, loaded, not shielded
by the town or an operator region (feet, head and floor), and either in the player's own claim or where they may build
(`LandRegistry.mayBuildFor`, no admin bypass), and passing `level.mayInteract` and `Authority.mayChange`. Nothing is
placed. Stored per player (dimension, position) in `GuestOutingState`; revalidated at every invitation with the same
checks except `Authority.mayChange`, which is asked only while the player stands there (one of its listeners judges
reach from the player).

## Requested coordinator patches (not made)

1. `CustomNpcs.PatrolGoal.patrolling()`: return null while `GuestActivity.anchor(level, npc) != null`, so a patrolling
   or wander-0 guest follows an activity. Until then such guests are refused an outing.
2. `GuestCityPlacement.register`: add `.requires(CustomNpcs::isOperator)` to the `city` literal. Brigadier keeps the
   requirement of whichever `guests` root registers first; `GuestOutings.register()` runs before the coordinator's
   command callback, so the root becomes usable by players (for `/guests visit`), and every operator child must carry its
   own requirement (place, roster, state, repair already do).
3. en_us.json: the keys listed in the report.
4. Optional: `Townsfolk.ReturnToPlaceGoal.slack()` returns 1 while `GuestActivity.anchor(level, this) != null`. With the
   current slack of 4 a held guest whose place is within 6 blocks only strolls there (about every 10 s on average)
   rather than walking straight over. Outings work either way (arrival is 6.5 blocks).

## Tests (written, not run)

`GuestOutingGameTests`, ten server tests with synthetic `test_c_*` guests, each within one tick: refusals while busy
and out of range; begin and end with no block change; the watched walk home (never into a block); logout; unload,
codec round trip and restart recovery; stuck path; visit spots against claims and protected regions, and
`/guests visit spot` by a non-operator; the shared budget and once per kind a day; idle and too-far endings; every
entry-point check, an unbound duplicate included.

## Open question for the owner

Whether outings may use public, town-protected ground (lawns, squares) when nothing is built or changed there. The
current rule refuses it.
