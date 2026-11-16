# October 29: imported art, aircraft banking and city raid approaches

Local follow-up to player reports against 20261029.1. Prepared with OpenAI Codex.
No dependency updates, live world edits, client-channel promotion or server restart.

## Original imported art

863 model-referenced textures were absent from the block/item atlas sources:
415 AE network textures, 363 spellbook textures, 51 construction textures,
21 entity sheets used by item/block models, six magnetizer faces, six nuclear
charge faces and one statue sheet. The PNGs already existed; their geometry and
UVs are retained. Explicit sprite sources make these available to the model
baker without stitching unrelated GUI sheets or all entity textures.

`tools/model_atlas.py` runs at the end of material generation so subsequent
imports cannot silently drop the atlas entries. It is additive and idempotent,
and fails if a newly referenced texture lacks a runtime file. A final metadata
pass supplies particle materials from existing face textures and the actual
Gecko skins. It also repairs Firefly Jar's inappropriate cube parent, the iron
fence's diagonal texture alias, and the renamed vanilla iron-chain reference.
The Mimic's vanilla chest sheet is explicitly stitched. The JE Load Tester uses
a byte-identical copy of its supplied texture in the block atlas, fixing the
client's rejection of a block that previously referenced the item atlas.

The offline item/blockstate graph has 38,048 roots, 38,764 reachable models,
and no missing model/parent files. The unused imported Lava Lily item model
references geometry absent from the supplied source, but no registered item or
blockstate reaches it; it is not replaced with invented artwork.

## Aircraft

Source meshes face positive Z; the pilot's left wing is positive X. A positive
Z roll raised that wing during a left turn. Reverse the body bank and the
warship envelope's extra roll. Retain smoothing, yaw controls, original rotor
and rudder animations, UVs, sounds and camera offsets.

## City raids

New raids gather along the east, south or west gate approaches. Ground troops
use exterior waypoints and the authored gate passages instead of a diagonal
line to the city center. Existing off-axis ground raids can use the same route
without changing saved raid data. The outer route stays west of the Styx estate,
and older raiders approaching from beyond it detour around its perimeter. Walkers stop in the open forecourt because
their 4.6-block height cannot clear the raised portcullis.

Navigation prefers road elevation under the roof, checks loaded destinations,
and no longer forces MoveControl into an unreachable wall. Failed paths retry
at bounded intervals. Melee troops ignore unreachable battlement targets. Foot and mecha guards move to the threatened gate; flak
crews keep their stations. Guards' local acquisition range is 48 blocks, and
an occluded mecha cannon target no longer prevents navigation until the next
target refresh. Existing invulnerability and civilian/player protection remain.

No building, wall, claim, decoration or owner-placed block is edited. Terrain
or player-built obstacles can still obstruct a route; this does not authorize
raiders to destroy or teleport through them.

## Validation

Local checks on October 29, Minecraft 26.3 / Fabric Loader 0.19.5, the pinned
Complete dependencies, Java 25.0.4.1:

- `compileJava compileClientJava compileGametestJava --offline --no-daemon`: passed.
- `runGameTest` filtered to `jugcraft-test:raid_approach_game_tests*`: both passed.
  Checks all three authored passages, 24 approach bearings, and actual infantry
  navigation through a cropped copy of the east gate (including its roof).
- `runGameTest` filtered to `jugcraft-test:city_defense_game_tests*`: all five passed.
  Covers bystander protection, invulnerable guards, flak and walker firing,
  animation/ballistics and bank access.
- Offline reference graph: 38,764 reachable models resolved. Atlas coverage:
  all 11,804 unique model texture references covered; generation is idempotent.
- `runClientGameTest -PclientTests=ImportedModelAtlasClientTests,AircraftFlightClientGameTests`:
  passed. Verified 11,805 runtime model texture IDs (including a test resource),
  a gallery of 22 reported items, both turn directions on all seven aircraft,
  engine-loop resources and both F5 views. The gallery and biplane left/right
  screenshots were visually inspected.
- Final `ImportedModelAtlasClientTests` recheck: passed after the metadata fixes.
  Resolved 11,818 unique runtime textures and 360,887 Jimtafacts block states
  without missing baked faces. The expanded 27-item gallery was visually
  inspected. No missing-texture, unresolved-texture or rejected-block-model
  warnings remained in this run. Existing OS performance-counter, anisotropic
  option and optional RPG Series loot-tag warnings remain outside this fix.
- Final raid-route recheck after the Styx perimeter adjustment: both tests passed
  (2.113 seconds in-game; Gradle completed successfully).

Logs include `build/oct29-resource-recheck.log`, `build/oct29-client-fixes-tests.log`,
`build/oct29-defense-tests.log` and `build/oct29-raid-tests-final.log`; the graph summary is
`build/oct29-model-reference-audit.json`. JEI page numbers are not stable
identifiers; use the named item families and gallery as the regression scope.
This is focused local validation, not a full release suite or live-server test.


## November 14 integration note


## November 13: seats, shots, turrets, balloon lean and flight help

Claude Opus 5.5 (worker C, November 13 batch), on top of `claude/nov12-c-aircraft` (`008ff48c6a`). Local review branch
`claude/nov13-c-aircraft`; nothing is pushed, released or deployed. The November 12 bank sign is unchanged.

### Flight help

The pilot's help was a server action-bar line with hard-coded letters ("WASD steer · Space climb / Sprint descend ·
Attack weapon · Use boost"). In the default font it is about 440 GUI pixels wide (estimated from glyph widths), wider
than the 427-pixel GUI of an 854x480 window at its largest scale and than the smallest 320x240 GUI, so both ends were
cut off. `AircraftHud` (client) now draws it for the pilot: five pieces (fuel or pedalling, steering, climbing, weapon,
booster or telescope zoom), each naming its control by the `KeyMapping`'s current translated key name. The pieces are
packed onto centred lines that fit the GUI's width (`font.split` wraps a piece wider than the whole GUI), stacked
upward from above the action bar, and drawn smaller only when even that does not fit. The server now syncs whole
seconds of fuel instead of sending the line. Texts: `hud.jugcraft.aircraft.*`, with fallbacks in code.

### Coordinates

Each defect below was first shown by a transform test that failed on the old code (`scripts/tests/test_aircraft_frames.py`),
then fixed; the test's negative controls keep the old behaviour demonstrable. Distances are for the craft's real
flight attitude: about 18 degrees of pitch and bank for the biplane and bamboo hopper, 4 degrees of pitch and 2 to 5 of
bank for the others.

| Part | Evidence | Defect | Fix |
| --- | --- | --- | --- |
| Seats and weapon mounts | Vanilla's `Vec3.xRot(a)` is Rx(-a): a point ahead of an entity is `(0,0,1).xRot(-pitch).yRot(-yaw)`. The renderer draws the body with Ry(-yaw) Rx(+pitch) Rz(bank), and the supplied original (Immersive Aircraft's `VehicleEntity.getVehicleTransform`, read from the supplied class file) places seats and muzzles with that same matrix. | `seat.xRot(pitch).yRot(-yaw)` tipped seats and mounts the opposite way and ignored the bank: riders up to 0.74 blocks from the drawn seat (bamboo hopper; biplane 0.46, warship 0.14), mounts up to 2.80 (bamboo hopper wing mounts; biplane 1.10, warship 0.99). | `AircraftAttitude.place`: the renderer's own body quaternion, with the bank. |
| Shots | The drawn rotary cannon's barrels run from its roll pivot, 12.5 px above the mount; the drawn crossbow's stock top is 5 px up (the original uses 0.3 blocks). | Cannon rounds started at the mount's foot, about 0.8 blocks below the drawn barrels; bolts started at the crossbow's base, below its stock; a diving bamboo hopper dropped bombs inside its drawn bay. | Rounds start at the drawn barrels' muzzle (`AircraftAttitude.cannonBore`), bolts on the stock top 1.25 blocks out, bombs below the corrected mount. |
| Rotary cannon and telescope `yaw` | The supplied `RotationalManager` sets yaw = -atan2(x, z) in the craft's frame (positive to the right); their files turn by `-variable.yaw`. | The evaluator read `yaw` as -aimYaw: both swung left when the pilot looked right. | `yaw` reads +aimYaw. Their `pitch` was right. |
| Warship crossbow `turret_pitch` | The supplied `WarshipEntity` sets `turret_pitch` to minus the pitch; the turret sits in the gondola, which is turned half round. | Read as +aimPitch, the crossbow tilted down when the pilot looked up. | `turret_pitch` reads -aimPitch. `turret_yaw` was right. |
| Aim frame | The supplied `RotationalManager` turns the look into the craft's frame first. | Plain differences of the pilot's and craft's angles missed the look by up to the craft's pitch when looking off the nose (up to about 18 degrees on the aeroplanes). | `AircraftAttitude.aim` works in the body's frame. |
| Hot-air balloon lean | The balloon drifts in the world's frame (the wind aloft), and the lean is computed from that motion. | The lean was applied after the basket's facing: it followed the drift only for balloons placed at yaw 180, trailed it at 0 and leaned sideways at 90. | `aircraft/BalloonAttitude`: the lean in the world's frame, outside the basket's facing; the idle sway is unchanged. |

Checked and left unchanged:

- The bank sign, `turret_yaw` and the cannon and telescope `pitch` were already right.
- The crossbow fires along the pilot's look, lifted 0.1, at any pitch, as the feature record describes ("weapons follow
  the pilot's aim"); the supplied crossbow has no aim joint, so the drawn one does not turn, and the supplied original
  fires it along the nose. A gameplay choice, not a frame defect; `test_aircraft_frames.py` pins it.
- Follow-up (from Codex's review): Aircraft clamps the synced aim pitch to 80 degrees either way in the body's frame,
  and the drawn cannon reads the clamped aim, but rounds flew along the look. Past the limit the round left the drawn
  barrel by the look's excess (up to 9 degrees at 89) and up to 0.157 blocks from the muzzle; inside it nothing
  differed. Rounds now fly along the drawn barrels (`AircraftAttitude.cannonDirection`): the look inside the turret's
  travel, its limit past it. The telescope fires nothing and is drawn on the same clamped aim.
- Weapon models' `roll` node reads the body bank and spins the head about its own barrel; the supplied original's value
  is always 0. Cosmetic; unchanged.
- The supplied mounts' own `yaw`, `pitch` and `roll` (the warship's side cannons, the airship's side crossbows) are
  ignored by both the renderer and the firing code, so shots match what is drawn, but the drawn orientation differs
  from the supplied original. Left for owner review.
- The warship turret is not clamped (the supplied original limits it to 75 degrees of yaw and 30 of pitch).

### Validation (November 13)

- Ran: `python3 -I scripts/tests/test_aircraft_frames.py`. Before the fix, 9 of 14 tests failed (11 failures counting
  subtests) and the 5 negative controls passed; after it, all 14 pass. `scripts/tests/test_aircraft_banking.py`: 6 pass.
- Ran: tree-sitter syntax check of every changed Java file: no errors. No Minecraft compile is available here.
- Follow-up: `test_aircraft_frames.py` gained turret-limit cases (aim pitches 79 to 89 either way, 27 attitudes, 5 aim
  yaws). Ran: before, 2 of 21 tests failed, both past the limit; after, all 21 pass.
- Written, not run: `AircraftFrameGameTests` (server game test: every aircraft flown with the real code from five
  headings, turning both ways, climbing and diving; every seat of every layout, every mount firing every weapon, cannon
  and telescope aim, the warship turret, `Vec3.xRot`'s direction, and balloons placed facing five ways, set drifting and
  left to drift on the wind; the follow-up adds the cannon, telescope and crossbow at aim pitches 76, 84 and 88 up and down). `AircraftHelpHudClientGameTests` (client: the help at 854x480 at GUI scale 1 and at the
  largest scale, and at 640x480, inside the window and above the action bar, each piece whole, each control named by
  its binding, and a rebound key shown).
- Owner in-game confirmation remains the acceptance step.
