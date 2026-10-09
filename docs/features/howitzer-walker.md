# The Howitzer Walker (model)

Status: a model and its animation clips, in the repository as a Blockbench project. Not in the game: by the owner's choice there is no entity, recipe, renderer or test yet ("I just want you to model stuff, don't implement", 9 October 2026).
Proposal issue: none. On 9 October 2026 the owner sent a render of a two-legged artillery walker they had modelled ("I want you to model this in blockbench next for the Jugcraft mod I just drew it"), asked for it to match the drawing's shapes and parts closely, then for "a walking animation ... and then a shooting animation with a cartoony blast", with "its animations all floaty and bouncy", and corrected the feet to point forward.
Owner: @jimbozoomer-byte
Target milestone and tier: none yet; a future walker in the Diesel and Armoured Walkers' family.
Primary specialty and supported player role: combat, when it is made playable.

## What it is
![The Howitzer Walker from three sides, then four frames of its walk and four of its shot (offline renders)](../images/howitzer_walker_preview.png)

The model follows the owner's render part by part, in Jugcraft's blocky style:

| Part | In the owner's model | Here |
| --- | --- | --- |
| Hull | A riveted olive box with rounded edges, cut away at its front top corner for the gun | A riveted olive box notched at its front top left, round bumper rails along every edge, dark recessed side panels, a chrome rail on brackets under the nose, a riveted plate and louvred vent on the back |
| Gun | A big howitzer lying in an open cradle, a thick jacket at its root and a flared muzzle | A trunnion hub in a cradle of a floor and two walls, open on top; a breech block and cap, the barrel with its jacket and a flared muzzle brake, recoil rods along the cradle; elevated 25 degrees |
| Pivot and linkage | A big axle with hubs under the nose and a wishbone down the right side | The front pivot axle with chrome hubs, and an A-arm of two bars from a hub on the hip up to a bracket on the hull's side |
| Lamps | Three ringed headlamps up the front's right, two small ones low on the left, an amber light at the top right | Three chrome-ringed amber lamps, two small lamps, the amber indicator, and one on the waist plate |
| Roof and sides | Grab hoops on the roof, handles on the side, a hatch | Two hoops on the roof's right, two handles on the right side, the pilot's hatch ring at the roof's rear left |
| Waist | A flat plate over big round hips | A riveted waist plate, a centre block, the hip axle through big chrome-capped hubs |
| Legs | Reverse-jointed: a flat thigh plate angled back, a knee hub, a shin angled forward, a coil spring on a shock absorber behind, a round ankle, broad flat feet pointing forward | The same: a hip drum, a broad flat thigh plate under its armour plate, a knee drum, a shin under a shin guard, a chrome shock absorber in a coil spring behind the thigh, an ankle housing, and a broad foot with a hinge strip, an upturned toe plate in front and a heel behind |

**Colours:** olive riveted plate with darker olive-grey recesses, brown-grey legs, gunmetal rails and barrel, chrome hubs, rods and lamp rings, amber lamps. No rust (the clean style).

### The animation clips
Both are in the Blockbench project, sampled from curves in `tools/howitzer_walker.py` so a renderer can draw exactly the same later.
- **walk** (one stride a second, looping): each leg swings at its hip and tucks at its knee as it comes forward. The hull bounces on the legs twice a stride, stretching as it rises and squashing as it lands, sways side to side and nods; the gun, grouped under the hull, rides the bounce and jiggles on its trunnion a beat behind.
- **shoot** (two seconds): the gun slams back along its barrel in two ticks, then runs out past its rest and wobbles to a stop; the hull rocks back and squats, then bounces to rest. A cartoon star burst (a yellow four-point star with its turned copy, an orange rim star and a tongue of flame) pops up at the muzzle in two ticks, spins and shrinks away by the eighth; three round smoke puffs grow, roll forward and up and shrink away over 24 ticks. The burst and puffs are groups under the gun that the clips scale from nothing; at rest in Blockbench's edit view they stand at full size.

## Connections
None yet. When the owner wants it playable, the Armoured Walker (batch 58) is the pattern: a `DieselWalker` subclass, a quad export of the parts, a renderer drawing these curves from the stride and the shot tick, a recipe upgrading the Armoured Walker, and game tests.

## Balance and automation
Not applicable: nothing is in the game.

## Multiplayer and persistence
Not applicable. No IDs are registered.

## Dependencies and assets
- No dependencies. The model is Jugcraft's own box model, made from the owner's render (their own work, shared for this) by `tools/howitzer_walker.py`: hull, lamps, gun, thigh, shin, flash and puff, from the steampunk helpers (stepped round prisms for every pipe, hub, barrel and spring coil). Textures drawn in the clean style by the same file: `hw_plate`, `hw_plate_dark`, `hw_leg`, and the flat cartoon colours `hw_flash`, `hw_flash_rim` and `hw_smoke`; the rails, hubs, lamps and bore reuse `dp_gunmetal`, `dp_chrome`, `aw_lamp` and `aw_bore`.
- The Blockbench project `art/howitzer_walker/howitzer_walker.bbmodel` is written by `tools/blockbench_export.py` (shared with the Pipeworks props): every box in groups with their joints as origins, the shins under the thighs, the gun under the hull with the burst and puffs at its muzzle, the textures embedded, and the two clips. `tools/box_preview.py` renders the previews. The Python model stays the source; a change made in Blockbench is carried back into it.

## Verification
- Done locally (9 October 2026): the project opens in Blockbench 5.2.1 on the owner's PC with its textures, groups and both clips, which play; the owner reviewed the walk there and corrected the feet; offline renders of the model from three sides and of eight animation frames were reviewed against the owner's render; `python scripts/check_repository.py` passes.
- Not applicable: the Gradle build, game tests and `check_mod_data.py`'s walker checks (nothing is registered).

## World and event applicability
Not applicable.

## Rollout and open questions
- The feet are part of the shins, so they tilt with the swing; a hinged foot would be a third part a leg.
- Making it playable is a separate piece of work the owner has not asked for.
