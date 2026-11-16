# November 14 local integration and Thanksgiving preparation

Implementation authors: Claude Code / Opus 5.5 for the delivered feature patches;
OpenAI Codex for local integration, conflict resolution, review and the fixes
recorded below. Base `8075c6d58b`, existing dirty
`feature/nov7-priorities-local` checkout. No commits, pushes, releases, launcher
profile changes, updater promotion or hosted server operations.

## Received and integrated for testing

- Pegasus: seven delivery patches, editable models, textures and flight controls.
- November 13 A–D: storage-cell LEDs and portable-cell tint; color-applicator
  face repair; connected tables/fountains and Christmas tree faces; aircraft
  seats/muzzles/turret transforms and responsive controls HUD; guest outfit,
  slim-arm held-item alignment and Activities pagination.
- Aircraft aim follow-up: cannon rounds follow the drawn barrel at the turret's
  pitch limit. The supplied fixed crossbow still shoots along the pilot's look;
  side-mount rest rotations remain an explicitly recorded limitation.
- E–H: unified encyclopedia route maps; General Store refusal of formable
  supplies to close the essence-to-currency loop; bounded raid approach retries
  and guard lookout positions; test-fixture cleanup and opt-in performance
  measurement tools. Benchmark tooling is not a measured performance result.

Archive hashes and ordered patch lists are retained under
`build/nov14-review/`. The candidate was assembled in a separate scratch tree,
then each current-file hash was checked before copying 178 changed files into
the checkout. Existing file versions were backed up in
`build/nov14-review/before-polish-integration/`. The Git index was not changed.

## Integration decisions and repairs

- Preserved the local raid origin injection, loaded-chunk checks, partial paths
  outside the city and combat handoff when an enemy is hidden behind a wall.
  The new guard lookout goal likewise yields to a visible opponent rather than
  abandoning its movement merely because a target exists behind a wall. It is
  registered before the existing gate-defense fallback at the same priority.
- Applied the reviewed connected-home join rule: isolated diagonal blocks do
  not join, and tables/stumps join their own material/block. This prevents an
  apron opening against incompatible geometry. Existing block IDs remain.
- Updated only the color-applicator runtime-copy provenance entries to describe
  the actual face repair; original owner assets remain unchanged.
- Merged test entrypoints and language keys, preserving Pegasus and existing
  local entries. Kept the current 6 GB test heap. The proposed CI workflow patch
  was not applied.
- Preserved newer documentation rather than replacing it with worker baselines.
- Local Pegasus testing found the missing vanilla saddle entity allowlist.
  Added the tag, its generator and a direct runtime assertion. Also prevented
  the flight/landing tests from succeeding before their flight sequence runs.

## Validation checkpoint

Combined common/client/game-test compilation now passes after fixing the guard
lookout's shadowed method calls and adapting two test APIs to pinned 26.3
(`PlayerAdvancements.clearTriggers`, registry lookup for coloured wool).
All eleven focused Python check files pass. The combined art audit passed:
38,798 block/item models, 3,786 quad parts and 184 models drawn together, with
its existing recorded allowlists. These are resource/geometry checks, not a
visual acceptance of every item.

Pegasus's 773 pure assertions pass. Actual server testing found the missing
saddle allowlist, premature landing success checks, a stale-packet assertion
that used the wrong sequence, and a repeated-Runnable scheduling bug: the
vanilla test scheduler keeps one time per Runnable identity, so the old helper
silently sent only its final input packet. Those have been repaired. All nine server tests passed (pegasus-r8.log),
and the real client key/flight/landing test passed (pegasus-client-r1.log).
The default empty fixture had a barrier roof; flight tests now use the open-sky
drone_tower arena. The client run is not a separate two-client observer test. Logs are under `build/nov14-review/pegasus-r*.log`.
The combined A–H candidate and all new holiday work are **not yet accepted**.
Focused resource, common/client compilation, server behavior and rendered client
checks must pass after integration. Original horse v2 files are downloaded and
previewed, but the five ground breeds are still art/proposals, not registered
gameplay; the giant Finnhorse rearing intersection remains open.

The four new holiday workers are tracked separately in
[the Thanksgiving craft package](thanksgiving-craft-package-nov14.md). A running
task or a delivered archive is not counted as completed gameplay.

### Combined runtime review

The first combined A–H/Pegasus server run passed 52 of 55 tests. Three raid
cases failed: the east-gate town scenario, unreachable melee target selection,
and the bounded march timeout. They remain under investigation; no release
acceptance. The new ordinary table placement/removal regression passed, so the
client gallery no longer touches blocks above every table to force updates.
The data audit found only an unrecognized Pegasus saddle-tag entry; its
validator now derives the entity ID from JugcraftPegasus registration. A
focused tag audit passed with zero errors; final combined data audit remains to run.

The fourth focused raid run now passes the city scenario and bounded march test.
The city fixture used an unloaded height query and placed the town roughly
64 blocks below surrounding terrain; it now loads the origin before sampling
the surface. The long march fixture now has an observing test player so raid
withdrawal at 2,400 ticks cannot silently end a recovery test. MarchGoal's
repath interval is now measured in actual server ticks. The city run defeated
all ten raiders with no stuck raiders or wall/tower pileups. One fence-target
case remains under investigation: navigation can return the active march path
after a target probe, so melee acquisition must verify the returned path's
destination as well as canReach. The fifth run tests that correction with the
homestead additions; it is not yet reported as passing.

The fifth run completed successfully: **all 30 required server tests passed**,
including the 22 raid/crew/approach/handoff scenarios and eight homestead cases
(`raid-homestead-r5.log`). Target acquisition now checks the returned path's
destination; a successful path to an old march objective is not accepted as
reachability of a defender. The final broader combined run is still pending.

### Final combined review repairs

The Thanksgiving integration is now part of the same runtime suite. The first
103-test run exposed four fixture defects: earlier dismounted players obstructed
the next facing's furniture placement; the planter helper deleted a returned
poppy by replacing its selected hand with air; a claim test used vanilla's
permanently creative mock; and remote drop assertions ran before their chunks
were entity-ticking. The fixtures now preserve items, clear prior players, use
real survival players and load/unload their remote test chunks explicitly. All
four cases passed the next combined run.

That next run exposed an intermittent holding-position defect: after a raider
gave up its march, its ordinary idle stroll could carry it away. Infantry and
walkers now suspend idle strolling while holding, without blocking their combat
goals. The aircraft turret-limit fixture also accumulated hundreds of manual
flight ticks outside loaded chunks; it now resets only translation between shots,
retaining the real attitude, velocity, aim and firing code. These corrections
are being tested with the full selected local cohort, including guest wardrobes.

### Combined server result — November 14

**All 118 required tests passed** across 19 selected classes in
`build/nov14-review/final-combined-server-r3.log`. This includes the complete
Thanksgiving groups, canonical companion/player settle seats, cross-boundary
multiblock protection, Pegasus, aircraft frames, storage-cell LEDs, home joins,
raid approach/combat/crew/recovery and city scenarios, economy, progression,
guest wardrobes and mock-player cleanup. Compilation passed as part of the run.
The three focused Python modules for aircraft frames, city raid approaches and
fixture isolation also pass all **37 tests** (`final-targeted-python.log`).
This is a selected combined local cohort, not the entire repository test suite.
The six selected rendered-client classes are now running; no final visual
acceptance is claimed until their captures are inspected.

All **six selected client suites passed together** (`final-combined-client-r1.log`):
Thanksgiving gallery, Pegasus key/flight/landing, guest wardrobes/held-item
alignment, aircraft HUD sizes/rebinding, connected decoration geometry, and
encyclopedia tech/magic routes with keyboard/back navigation and minimum GUI.
The 105 original captures and client log are preserved in
`build/nov14-review/client-r1/`. Codex inspected representative rendered views
from every suite. A second bounded capture pass adds small-craft close-ups and
clears welcome chat from HUD screenshots; it changes only test presentation.

The final gallery/HUD isolation rerun passes (`final-gallery-closeups-r3.log`),
with clean controls captures and all 26 holiday gallery views preserved in
`client-final/`. Personal close-up review is complete. The selected local
combined candidate is accepted for owner play review; separate observer/load
validation and the five ground breeds' gameplay remain unfinished. Nothing
has been published or deployed.
