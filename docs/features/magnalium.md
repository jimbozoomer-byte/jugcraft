# Magnalium (aluminum-magnesium alloy)

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. TODO item: "Develop selected chromium/nickel stainless steel … and **aluminum-magnesium structural alloy** for frames/panels/vehicles on compatible shared alloy/forming equipment. …" This change delivers the aluminum-magnesium alloy. Stainless steel waits on the chromite region design.

**This change's choices:** the name (Magnalium, the real name for aluminum-magnesium alloys), the 3:1 ratio, the colour and the two uses, listed below for the owner to review.

Owner: jimbozoomer-byte
Target milestone and tier: after aluminum and the coastal magnesium cell
Primary specialty and supported player role: metals; builders and drone makers who want more panels and propellers from their aluminum

## Player experience

- **Magnalium Ingot.** The Alloy Smelter makes **four** from **three aluminum ingots and one magnesium ingot** (200 ticks).
- **The usual metal set:** nugget, ingot, storage block (9 to 1 each way) and **plate** (the Metal Press presses one ingot into one plate).
- **Uses (the stiffer alloy goes further):**
  - four magnalium plates craft **six aluminum cladding** (four aluminum plates make four);
  - four magnalium plates and a stick craft **two aluminum propeller sets** (four aluminum plates make one).

## Connections

- **Input producers:**
  - aluminum (the Bayer and electrolytic cell route, or the stand-ins);
  - magnesium ingots (the coastal seawater → brine → dry magnesium chloride → magnesium electrolysis cell chain).
- **Output consumers:**
  - aluminum cladding (building panels and the drone tower's shell tier);
  - aluminum propeller sets (drones).
- **Machines:** the existing Alloy Smelter and Metal Press, so no new machine.
- **Optional:** yes. The plain aluminum recipes stay.

## Balance

- **Metal accounting:** three aluminum and one magnesium (36 units) make four magnalium ingots (36 units), so no metal is created. `tools/check_mod_data.py` now counts a magnesium ingot as nine units of magnesium.
- **Cladding:** 3 aluminum + 1 magnesium → 4 plates → 6 cladding (0.5 aluminum each), against 1 aluminum each the plain way.
- **Propellers:** 3 aluminum + 1 magnesium → 4 plates → 2 sets, against 4 aluminum for one set.
- The saving costs the magnesium chain's coastal intake, power and chlorine handling.

## Multiplayer and persistence

- **New registrations,** stable from now on:
  - the items `jugcraft:magnalium_ingot`, `magnalium_nugget` and `magnalium_plate`;
  - the block `jugcraft:magnalium_block`;
  - the recipes `alloying/magnalium_ingot`, `pressing/magnalium_ingot`, the nugget/block recipes, `aluminum_cladding_from_magnalium` and `aluminum_propeller_set_from_magnalium`;
  - the tags `c:ingots/magnalium`, `c:nuggets/magnalium`, `c:plates/magnalium` and `c:storage_blocks/magnalium`.
- The metal is added after every existing alloy, so the other metals' textures are unchanged.

## Dependencies and assets

There are no new dependencies. The ingot, nugget, plate and block are drawn by the shared material icon and texture generators (`tools/material_icons.py`, `tools/generate_textures.py`) from a new faintly rosy silver ramp. It was moved until `check_mod_data.py` found it distinct from every other metal and from vanilla iron, gold and copper. No texture is copied.

## Verification

**Local checks** (no Gradle or game): `python tools/generate_material_data.py`, `python tools/check_mod_data.py` (including the metal-conservation and colour-distance audits), `python tools/check_icon_maps.py` and `python scripts/check_repository.py` passed. The icons were inspected at 8x beside aluminum's.

**Server game tests for CI** (`MagnaliumGameTests`):

| Test | What it checks |
| --- | --- |
| `alloySmelterMakesMagnalium` | Three aluminum and one magnesium make four magnalium ingots. |
| `metalPressMakesMagnaliumPlate` | The Metal Press presses an ingot into a plate. |
| `magnaliumPlatesMakeCladdingAndPropellers` | Four plates craft six cladding; four plates and a stick craft two propeller sets. |

### Results

Not yet run in CI.

## Rollout and open questions

- The name, ratio, colour and the two uses await the owner's review.
- Vehicle frames and other structural parts, and stainless steel, remain open.
