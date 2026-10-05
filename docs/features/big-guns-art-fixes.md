# Big guns, Landship, Diesel Walker and balloons: art fixes (5 October 2026)

Status: implemented (pending CI and review)
Proposal issue: none. The owner's feedback of 5 October 2026 is the scope approval.
Owner: jimbozoomer-byte
Target milestone and tier: no new tier. Cosmetic and client-motion fixes to existing content: the big guns (batch 51, petrochemical tier), the tower guns (batch 54), the Landship (batch 49), the Diesel Walker (batch 47), the Observation Balloon and the hot-air balloons and pibals (fall addition 29).
Primary specialty and supported player role: combat (artillery, vehicles) and scouting; no change to what any of them does.

## Player experience
The owner reported, among other things:
- "parts of the grand mortar are invisible / see through same with the barrels on most of the big guns, lots of their textures are conflicting";
- "the way the balloons rise animation looks it is like glitching upward also its texture on the balloon has tons of transparency";
- "landship has a bunch of textures which don't have backsides so you can see through it at a bunch of angles and the guns barrel is flashing because textures are colliding, landwalker has same issues";
- "icons for all the weapons should be made to look way cooler".

What changed, in game:

| Thing | Before | After |
| --- | --- | --- |
| Every gun barrel and muzzle | A framed, bolted panel (`dp_gunmetal`) cut by the 16-pixel world grid, so barrels read as stacks of crates, with a sooty square tile split into a light "+" over the bore | Plain barrel steel (`tg_tube`) with no frame or direction; the muzzle face carries one round dark bore (`tg_bore`), drawn whole on a plate 0.1 pixel proud of it |
| Yellow, khaki and olive housings, roofs and drums | Framed panels with bolts and chips (crate grid) | Seamless painted armour (`ar_yellow`, `ar_armor`, `ar_olive`: a flat coat with one weld seam a course) and seamless coursed steel (`tg_steel`) on roofs, housings and brakes |
| Port covers and hazard signs | Chopped into 2 to 12 fragments by the world grid | Each drawn whole on its own plate (`tg_port!`, `tg_warning!`) |
| Siege Mortar | A 1-pixel see-through slit under the whole deck; the deck's tread and hazard rim flickering; top rails crossing the cradle plates | The bearing ring meets the deck; the deck's top is the rim's tread cap (as on the tower guns); rails half a pixel higher; breech a pixel shorter so it clears the deck at full elevation |
| Triple Battery | Outer sleeves sliding in the plane of the drum's facets (flicker at every elevation); the housing's front in the drum's step plane at 0° | Sleeves a quarter pixel wider, housing front half a pixel further out |
| Bastion Autocannon, Fortress Rifle | Barrels and housings cutting through the gunhouse roof at every elevation | A mantlet slot in each roof that the housing rises through; the autocannon's sight moved behind it; the rifle's range-finder hub no longer shares the arm's planes |
| Self-Propelled Howitzer | The gun swung through the domed engine below about 44°; the cradle swallowed the exhausts; the left wheel hubs floated half a pixel off the hull | A low grille engine deck at the front right, exhausts at the back right behind the crew, a lower mounting ring, a shorter cradle and shield: nothing is crossed anywhere in the gun's 30° arc and -5° to 70°; hubs on both sides, standing a quarter pixel proud of the track links |
| Flak Gun | The cradle swung into the pedestal at full elevation; the sight shared a plane with its post | Shorter cradle, ammunition drums half a pixel forward, sight moved forward |
| Grand Mortar | Culled while its muzzle was still on screen at 85° | Its render box reaches 5 blocks up instead of 4 |
| Landship | A 1-pixel slit under the turret; the muzzle face flickering between the bore and the brass ring | The turret's brass ring reaches down onto the casemate's gilt band; the barrel is steel in a lacquered sleeve ending in a brass band with a steel face and a round bore; the sponson guns too |
| Diesel Walker | The thighs' outer faces sliding in the pelvis's side planes (flicker whenever it walks); the chest's side walls flush with the backrest | Thighs a quarter pixel narrower, side walls a quarter pixel proud, knee drums, toe bands and drill band nudged off their neighbours |
| Observation Balloon | A stair-stepped box envelope with tons of see-through gaps | A smooth closed envelope turned like the hot-air balloons', with three inflated tail lobes, on its own clean texture: doped canvas, gore seams, sewn rings, a darker nose, red and cream bands, rigging patches and the stencilled serial reading level on both flanks |
| Observation Balloon, pibals, hot-air balloons | The client snapped them to each position the server sent, so they stood still and then jumped (every 0.1 s for the Observation Balloon and pibals) | The client eases after each position over a few ticks (`SmoothFlight`), and the Observation Balloon and pibals get a position every tick |
| Hot-air balloon envelopes, mooring rope, burner flame | Opaque, but drawn through the translucent path, where Improved Transparency can show the far side through the near one | Drawn cut out, which 26.3 draws from both sides in the opaque pass: they look the same from the basket and solid from outside |
| Big-gun, shell, balloon and Range Finder icons | 16x16 sketches of flat blocks | 32x32 pixel art in the arms icons' rules: a dark outline, flat tones lit from the top left, three-quarter views with the barrel on the diagonal and a round bore at the muzzle |

Item and entity IDs, recipes, numbers, pivots, seats and hit boxes are unchanged.

## Connections
- Existing input producer: none new; the guns, vehicles and balloons are made exactly as before.
- Existing output consumer: none new.
- Technology connection: unchanged (see [big-guns.md](big-guns.md), [tower-guns.md](tower-guns.md), [landship.md](landship.md), [diesel-walker.md](diesel-walker.md)).
- Magic connection: none.
- Reachable entry path: unchanged.
- For infrastructure/cosmetics: this changes models, textures, icons and client-side smoothing only, so resource links do not apply.

## Balance and automation
No recipes, costs, damage, ranges, fuel use or rates change. The server still moves every balloon; the client only eases its own copy toward the server's position, so nothing a client does can move a balloon or change what it reaches.

## Multiplayer and persistence
- **Server authority:** unchanged. `SmoothFlight` runs on the client only (in each balloon's client tick) and changes nothing the server sees.
- **Network:** the Observation Balloon and the pibal send their position every tick instead of every second tick, as the hot-air balloon already did; a few bytes a tick for slow entities, negligible.
- **Saves:** IDs, saved fields and entity sizes are unchanged, so placed guns, vehicles and balloons load as before and just look new.
- **Disable behaviour:** unchanged.

## Dependencies and assets
- No new dependencies. All art is original, drawn by the generators:
  - `tools/tower_guns.py`: the shared gun steel `tg_tube`, `tg_steel`, `tg_soot` and the `tg_bore` decal, and the `bore()` and `port_plate()` helpers;
  - `tools/artillery.py`: `ar_yellow`, `ar_armor`, `ar_olive` (seamless painted armour), the Observation Balloon's envelope mesh and `textures/entity/observation_balloon/envelope.png` (192x128);
  - `tools/gun_icons.py`: the 32x32 icons of the eight guns, the balloon, the Range Finder and the three shells.
- `dp_gunmetal` (used by hundreds of block models) is untouched; the guns stopped using it.
- Java: `SmoothFlight` (new), the three balloons' `createInterpolationHandler()` overrides and client ticks, two update intervals and the tower guns' render box.
- **UNVERIFIED API:** `Entity#createInterpolationHandler()`, `LinearInterpolationHandler(Entity, int)` and `Entity#getInterpolation()` / `InterpolationHandler#interpolate()` have no other use in the repo. `LinearInterpolationHandler` and its `DEFAULT_INTERPOLATION_STEPS` appear in the pinned Fabric API's own client game tests; the rest is taken from 26.3 mods outside the repo. They are confined to `SmoothFlight` and the three overrides, so a compile error is fixed there.

## Verification
Offline (no Minecraft here), with the render core's exact hidden-face removal simulated by its prototype:
- An independent ray cast of every exported part of the eight guns, the balloon's basket, the Landship and the Diesel Walker (4,000 rays a part): 0.00% see-through and 0.00% z-fighting on all 33 parts (the shipped quads: up to 20.6% see-through and 25.1% z-fighting).
- No same-plane overlap inside any part, and none between any two parts at any sampled pose: every gun at 2° steps of elevation over its range and several traverses (the howitzer every 5° over its whole ±30° arc with its track links, the flak head every 30° all the way round), the Landship's turret every 45° at -25°, -12°, 0° and 10° with its track links, and the Diesel Walker at rest and at full stride both ways.
- The one overlap left: where the track bends, the corners of neighbouring track links (`landship_link`, shared with the howitzer) overlap by about 0.1 px² in their side planes. Both are the same texture, so nothing changes colour there; shortening the links to ±2.4 pixels would remove it at the cost of wider gaps between plates.
- No moving part inside a fixed part's boxes at any of those poses (the flak head also every 5° of traverse and 1° of elevation), except the Triple Battery's housing, which is meant to sit inside its drum.
- The Observation Balloon's envelope: a closed mesh (every edge matched by its neighbour's), every quad wound to face out, its texture fully opaque.
- Renders of every gun, vehicle and the balloon from several sides and close-ups of every muzzle, decal, slot and joint.

Guards (`tools/check_mod_data.py`, `check_gun_art`, using `tools/gun_poses.py`): every gun is posed every 5° over its travel and fails the check if a moving part shares a plane with, or sinks into, its mount; every quad texture must exist and solid ones be opaque; decals must be drawn whole; the balloon's envelope must stay closed and outward-facing; and nothing opaque may use the translucent "nocull" path. Run on the shipped models before these fixes, the pose check reports 172 problems; after them, none.

CI (planned; not run here): `BigGunsClientGameTests` takes close screenshots against the sky (`jugcraft_tower_guns_close`, `jugcraft_tower_guns_low`, `jugcraft_big_guns_close`, `jugcraft_landship_close`, `jugcraft_diesel_walker_close`, `jugcraft_observation_balloon`), then rides the Observation Balloon up, logs its server and client heights each tick, and fails unless the client's balloon, a pibal and a hot-air balloon ease with a linear handler, the client stays within a block of the server, and the climb has no stalls or jumps in more than a tenth of its ticks. The existing server tests (`observationBalloonRisesWithItsSpotter`, the hot-air balloon tests) cover the unchanged server motion.

## World and event applicability
Not applicable: no world generation, mobs, loot or seasons change.

## Rollout and open questions
- The see-through holes themselves come from the shared quad exporter (`zeppelin.tiled_quads`), which the render-core change replaces with exact hidden-face removal. These fixes are the per-model part: geometry that has to move, decals and textures. Both are needed.
- The **Armoured Walker** (PR #193, not on main) needs the same treatment there: its hip drum `x -3.75..3.75` (it shares the skirt core's side plane, `armoured_walker.py:125`), recommended plate belts from `y 35.75` (`:72-73`), optional gunmetal ankle `x ±4.25` (`:134`) and an optional gunmetal muzzle-ring cap; then regenerate `armoured_walker_quads.json` and `raider_quads.json`.
- The Zeppelin, Landship, Diesel Walker and the howitzer are moved by the server alone too; the owner has not reported their motion, so they keep stepping for now. `SmoothFlight` would serve them the same way.
- The gun icons follow the arms icons' rules; the owner may want them bolder still.
