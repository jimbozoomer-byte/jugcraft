# Dieselpunk giants (batch 44)

Status: implemented (pending CI and review)
Proposal issue: none; the owner asked in chat for much bigger, very detailed workshops (2x2x1 up to 5x5x5, some long, not all square), in the weathered dieselpunk look of their reference pictures.
Owner: jimbozoomer-byte
Target milestone and tier: every tier that had one-block machines
Primary specialty and supported player role: industry and building (visual scale; no balance change)

## Player experience
Sixteen machines that used to be one block are now big multi-blocks in a weathered, patina-green dieselpunk style. They stand on skid feet, with riveted steel plate, perforated green covers, ribbed coil stacks, banded domes, copper and steel pipe runs with hex-nut fittings, red handwheels, blue caps, gauges and glowing amber windows. Placing the item builds the whole machine if the space is free (like the existing multi-blocks); breaking any block removes all of it and drops the one item.

| Machine | Size (wide x tall x deep) | What it looks like |
| --- | --- | --- |
| Coal Generator | 2 x 2 x 3 | Firebox and hopper, boiler barrel with steam dome, perforated dynamo, smokestack |
| Steam Generator | 3 x 3 x 2 | Boiler under a ribbed dome, steam main into a flanged turbine with an intake fan |
| Electric Furnace | 2 x 2 x 2 | Red crucible between two green induction coil stacks, hood fan, control cabinet |
| Crusher | 2 x 3 x 2 | Jaw crusher in a red frame, big hopper, flywheels, belted motor |
| Metal Press | 2 x 3 x 2 | Four-column hydraulic press, twin cylinders, red ram, pump |
| Wire Drawer | 4 x 1 x 2 | Long bench: pay-off reel, three die boxes, capstan |
| Circuit Assembler | 3 x 2 x 2 | Pick-and-place gantry over a belt, amber CRT console, reel cabinet |
| Pulverizer | 3 x 2 x 2 | Ball mill drum with ring gear, pinion motor, hopper and chute |
| Ore Washer | 2 x 2 x 4 | Rotary drum over a long sluice of tanks, spray bars, settling tank |
| Sieve | 2 x 2 x 3 | Shaker screen on coil springs, vibrator motor, two chutes |
| Sawmill | 2 x 2 x 5 | Log carriage on rails past a big guarded blade, dust duct |
| Fuel Cell | 2 x 2 x 2 | Clamped cell stack, gas manifolds, inverter cabinet |
| Hydroponic Bay | 3 x 2 x 3 | Open grow house: three crop trays under amber lamps, nutrient tank |
| Electroplating Bath | 4 x 2 x 2 | Three tanks under a gantry hoist, busbars, rectifier |
| Ammonia Chiller | 2 x 3 x 2 | Frosted condenser tower with fan, compressor, receiver drum |
| Rocket Workshop | 5 x 3 x 3 | Assembly hall: gantry crane over a rocket in cradles, workbench, console, propellant tank |

The battery box, solar panel, cobblestone generator, auto-crafter, tree farm and arc furnace controller stay one block: they work on what is right next to them (sky, water and lava, the planting area, the arc furnace casing), or are meant to be packed tightly.

## Connections
- Existing input producer / output consumer: unchanged; every recipe, slot, tank, side setting and upgrade is the same.
- Technology connection: the same machines. Cables, pipes, conveyors, hoppers and comparators reach the machine through any of its blocks (shared multi-block code).
- Magic connection: none.
- Reachable entry path: unchanged recipes.
- For infrastructure/cosmetics: this is a model and footprint change; resource links do not apply.

## Balance and automation
No recipe, speed, energy or capacity changes. The steam generator and ore washer still refill from a water source under their master block (the front left bottom block). Nothing new is produced, so no loop is possible.

## Multiplayer and persistence
Placement and forming run on the server (`LargeMachineBlock`). **Old worlds:** each enlarged block has a new `compact` property, default `true`. A machine saved before this change has no such property, so it loads as `compact=true`: one block, the old model, and exactly the old behaviour (`EnlargedMachineBlock.footprint` returns a single block). Placing the item always builds the full machine (`compact=false`). Commands and structures that place the default state also get the compact block, as before. A compact copy can still be turned with the wrench; the full machine cannot.

## Dependencies and assets
No dependencies. All textures are original and drawn by code (`tools/dieselrust_textures.py`, names `dr_*`); the models are in `tools/giant_models.py`. The classic style pack gets simple plinth-and-body versions (`large_machines.classic_giant`).

## Verification
Planned (CI): game tests `enlargedCrusherFormsAndOldCopiesKeepWorking` (all 12 blocks form, the top back block reaches the slots, the big crusher and a compact one both crush) and `breakingAnEnlargedMachineRemovesItAll` (breaking the far end of a sawmill removes every block). Every existing machine test still places the default (compact) state, so the compact path is covered by them. The client screenshot showroom builds every multi-block, now including the sixteen giants. Local: `check_mod_data.py` and offline model renders. Not done: survival play-test, dedicated server with two clients, performance measurement.

## World and event applicability
Not applicable: machines are placed by players only.

## Rollout and open questions
- A compact copy is not upgraded automatically; break it and place it again to get the big machine (that needs the free space).
- Sizes and looks are first passes for the owner to review.
