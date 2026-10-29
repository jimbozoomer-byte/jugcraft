# Clockwork Animatronics

Status: part 1 (the Organ-Grinder Skeleton) implemented. CI builds it and runs its game tests and client screenshots; not yet played by hand. Parts 2 to 6 are planned (below).
Proposal issue: none. The owner asked for it directly on 28 October 2026, as set 9 of the ten sets of fall and Halloween decor ("Then I want you to do 1-10 in parts in that order one by one"): an organ-grinder skeleton with a hurdy-gurdy, an animatronic werewolf on a turntable, a ghost on a crank wire, a mechanical raven and a pumpkin-headed drummer, on the power network, brass and steam at first and a dieselpunk steel version later.
Owner: @jimbozoomer-byte
Target milestone and tier: the bronze age of kinetic power ([kinetic-power.md](kinetic-power.md)). The figures are made from vanilla things and a clock; they move on kinetic energy from a hand crank or a steam engine, through iron shafts and brass gearboxes, or from the JE network through an electric motor.
Primary specialty and supported player role: building, with engineering. A haunt's builder sets the figures up; an engineer runs the power to them. Nothing in it is needed to progress.

## Player experience
Halloween figures that move by clockwork. Each runs on kinetic power: a shaft, gearbox or source touching its lower block gives it what its clockwork needs. It runs only on all of that. Its power stopped, it runs down over half a second and comes to rest where it is.

### Part 1: the Organ-Grinder Skeleton
- **Organ-Grinder Skeleton**: a skeleton 1.75 m tall in a battered black top hat and a moth-eaten red waistcoat, standing behind a street barrel organ (a hurdy-gurdy, as an organ-grinder's is called) on a two-wheeled cart. Two blocks tall.
  - The organ is a mahogany case 47 cm wide, bound in brass, its front a row of gilded pipes on red cloth, with a crank on its right side. The cart has spoked wheels 37 cm across, a prop under its front and two handles.
  - The skeleton's left arm hangs at its side, a tin cup for coins on a finger.
  - **It stands facing whoever places it**, and needs the block above free.
  - **It runs on 4 KE a tick**, taken from a shaft, gearbox or kinetic source touching its lower block: a hand crank turned beside it, a steam engine's shaft line, or an electric motor.
  - **While it runs**:
    - its right hand turns the organ's crank, once round a second, the arm bending at the elbow to follow the handle;
    - the organ plays its waltz, an original tune in A minor, eight bars long, the melody on the flute and the bass on the first beat of each bar;
    - its head nods once a bar, and its jaw drops on each note of the melody.
  - **Its power stopped**, it keeps going for half a second, then the crank runs down and stops where it is, its head and jaw settle, and the organ falls silent. Started again, the organ plays from the top.
  - Recipe: black wool for the hat over a note block between two bone blocks, over a clock between two planks.
  - Broken, either half brings the whole figure down, and it drops once.

### Planned
- **Part 2: the werewolf**: an animatronic werewolf on a brass turntable, turning slowly, raising its claws and throwing back its head to howl.
- **Part 3: the ghost**: a ghost on a crank wire, a cheesecloth ghost lifted and swung by a wire crank on a post above it.
- **Part 4: the raven**: a mechanical raven on a brass perch, its wings flapping, its head turning and its beak opening as it caws.
- **Part 5: the drummer**: a pumpkin-headed drummer beating a bass drum in time.
- **Part 6: the dieselpunk tier**: a steel show controller on the JE network, which drives every figure near it in time, without shafts, in riveted steel and dieselpunk dials.

## How it is drawn (docs/ART_DIRECTION.md, docs/NATURAL_TEXTURES.md)
- Built from boxes at real size, one texel to a model pixel (`tools/clockwork_data.py`), in the Haunted Midway's and the Front-Yard Haunt's manner, whose palettes and bones it shares.
- **Mahogany** (the organ's case and lid): deep red-brown, a grain line a tone darker every third texel, its top row lit. Its sides are bound in brass down both edges.
- **The organ's front**: a mahogany frame, a brass moulding under its top rail and another above its foot rail. On that one the gilded pipes stand in a row a texel each against red cloth, rising to the middle, each lit on alternate columns, its top catching the light.
- **Brass** (the crank and its boss, the waistcoat's buttons): lit like a gold block, its top row bright, its left edge catching the light, its foot dark.
- **Bone**: its middle tone, lit along its upper or left edge and shaded along the other, its knuckles a tone lighter. The spine and neck are vertebrae, a knob every other texel.
  - The ribcage is a rib a texel high on every other row, the gaps between them dark, the breastbone down the middle of its front.
  - The pelvis is its two wings, the sacrum behind them and the pubic arch in front.
  - The skull's face has square sockets, a nose and a row of upper teeth; its jaw is a separate piece with the lower teeth.
- **Cloth** (the hat, its band and the waistcoat): flat, as cloth is. The hat is black felt lit along its top with a dark red band. The waistcoat is red, its top row lit, with three moth holes eaten in its back at fixed places, never at random.
- **The cart**: spoked wheels with iron tyres and hubs (the Pumpkin Patch Market's wheel), a plank bed and wooden handles.
- **Moving parts move**, drawn by `OrganGrinderSkeletonRenderer` from quads (`assets/jugcraft/clockwork_quads.json`) cut from the same boxes as the block model:
  - the crank turns about its axle;
  - the right arm is two bones: the upper arm swings about the shoulder and the forearm about the elbow, so the hand stays on the handle all the way round, the elbow kept back;
  - the head nods about its neck, and the jaw drops about its hinge.
- **The figure's block model** is the whole figure but its moving parts, in its lower block's model, two blocks tall; its upper block's model is empty.
- **The item** shows it with its hand on the crank, as near as a block model's turns allow: the crank turned 112.5 degrees, the upper arm back 22.5 and the forearm forward 45.
- **The previews**, fixed before commit:
  - the organ's pipes, each with a dark mouth texel near its foot, read as a face with a black bar for a mouth; the mouths are gone and the pipes stand on a brass moulding;
  - the waistcoat's moth holes, a texel each on panels two texels wide, read as black blotches; they are now only on its back;
  - the pelvis, one box, read as a block; it is now its wings, sacrum and pubic arch;
  - the item's right arm hung straight down; it now holds the crank.
  - The crank was previewed at three angles from the east, to check that the hand stays on the handle (the preview turns the parts as the renderer does).

## Connections
- Existing input producer: kinetic power, from the hand crank, the steam engine and the electric motor (which takes JE), through iron shafts, brass gearboxes and belts ([kinetic-power.md](kinetic-power.md)).
- Recipe inputs: bone blocks, a note block, black wool, planks and a clock.
- Existing output consumer: none; a figure is a power sink, like a machine.
- Technology/magic connections (or infrastructure/cosmetic applicability): technology. It is decoration that runs on the kinetic network, a use for a hand crank or a small steam engine before electricity.
- Reachable entry path: a hand crank (planks and an iron shaft), from the first iron. The figure's recipe needs a clock (gold and redstone).
- Which connections are required vs optional; trade and solo routes: all optional, made solo, tradeable.
- How this specialty stays useful without mastering every other branch: a figure stands as decoration unpowered; one hand crank runs it for twenty seconds at a time.

## Balance and automation
- **Units**: KE per tick, as the kinetic network's.
- The Organ-Grinder Skeleton takes 4 KE a tick while it runs, and only on all 4. A hand crank (16 KE a tick) runs four of them; a steam engine (64) sixteen.
- The energy goes into the clockwork: nothing comes back out, and it stores nothing. No loop.
- Its recipe gives back only itself.

## Multiplayer and persistence
- **The server runs it**: its block entity, in its lower block, takes power only from the kinetic network's push, counts what it took each tick, and sets its block's `running` (on both halves) only when that changes. It plays the waltz from the server, as sounds every nearby player hears, from the top each time it starts.
- **The client draws it**: from `running` and its own clock. Each client keeps its own count of the tune from when it saw the figure start, so the crank, the arm, the nod and the jaw move in time with what it hears.
- It saves nothing: whether it runs comes again from its power on the first tick after a reload.
- It can't be pushed by a piston.
- IDs (stable from release): the block and item `organ_grinder_skeleton` (`facing`, `half`, `running`), and its block entity `organ_grinder_skeleton`.
- Its recipe follows the agriculture switch. The block and item stay registered when it is off. It runs on the machines feature's kinetic power: with that off, no hand crank or shaft exists to drive it, and it stands still.

## Dependencies and assets
- No new dependency.
- Models, blockstates, loot tables, quads and textures are generated by `tools/clockwork_data.py` from `tools/clockwork.py`, through `tools/generate_material_data.py` and `tools/generate_textures.py`.
- The waltz is original to this set.
- Nothing is copied, traced or recoloured from Minecraft's textures.

## Verification
Local checks run on this branch, and their results:
- Part 1:
  - `python3 tools/check_mod_data.py`: PASS on the second run (8,163 material IDs), including the art check of every block and item model. Its new `check_clockwork` checks:
    - the Organ-Grinder Skeleton's Java against `tools/clockwork.py`: its power, timing, waltz and loudness;
    - that its block entity takes kinetic energy, up to its need a tick;
    - its registration with its block entity, its two-block item and its renderer;
    - that the renderer's crank, shoulder, arm, neck and jaw are the table's, and that the arm reaches the crank's handle all the way round;
    - that its moving parts' quads are written to `clockwork_quads.json`, which `DecorQuads` reads and the renderer draws;
    - that its blockstate covers every facing, half and `running`, its upper half's model is empty, and it drops once, from its lower half;
    - its name.
  - The first run failed in `tools/generate_material_data.py`: the cart's wheels were 0.75 pixel thick, and the shared wheel's spokes, set in 0.4 from each side, came out inside out (`model_writer` refuses such a box). The wheels are now 1 pixel thick, 6 cm, as a cart wheel's rim is. Because generation stopped, that run's audit and unit tests failed on files it hadn't written. Both pass on the second run.
  - `python3 scripts/check_repository.py`: PASS.
  - `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
  - `python3 tools/check_icon_maps.py`: PASS (283 maps; the figure's item is its 3D model, not a map).
  - `javac` scan of the new and changed Java for syntax errors: none. Every member the new Java calls is used elsewhere in the code already, or declared in it, with the same arguments where the call is new. The full compile runs in CI.
  - The model was previewed offline, with the moving parts turned as the renderer turns them; what the previews showed and what was fixed is under How it is drawn.

`ClockworkGameTests` (run in CI):
- **The Organ-Grinder runs on its clockwork**:
  - placed, it stands two blocks tall, facing the player, at rest;
  - given less than its need in a tick, it doesn't run;
  - fed each tick, it takes its need and no more (a second offer in the same tick, from another side, gets nothing), runs on both halves and plays;
  - its power stopped, it runs down and stops, its tune back at the top.
- **A hand crank drives it**: a hand crank beside its lower block, turning, sets it running.
- **It breaks whole**: broken at its upper half, both halves go and it drops once.
- **Its data loads**: its recipe and loot table.

`ClockworkClientGameTests` takes three screenshots of two Organ-Grinder Skeletons on a dark oak floor, one facing the camera and one facing east, each with a hand crank against the back of its lower block:
- at rest, from the front;
- running, their cranks turned, from the front;
- running, from the east of the one facing the camera, where its crank and its right hand are.

Not run: a hand playtest, and a two-client playtest.

## World and event applicability
Seasonal in look, not in use: it is made and works all year. It doesn't generate in the world.

## Rollout and open questions
- Part 1 is the Organ-Grinder Skeleton; parts 2 to 6 follow, each stacked on the last.
- Open: whether a figure should also take a redstone signal to start and stop (a power switch), and whether the show controller (part 6) should let a builder set each figure's start in a sequence.
